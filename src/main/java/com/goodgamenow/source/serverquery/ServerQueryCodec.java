package com.goodgamenow.source.serverquery;

import com.goodgamenow.source.serverquery.response.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.itadaki.bzip2.BZip2InputStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.CRC32;

/**
 * User: Joe Nellis
 * Date: 5/27/2015
 * Time: 2:31 PM
 */
class ServerQueryCodec
    extends MessageToMessageCodec<DatagramPacket, ServerQuery> {

  private static final int MAX_PLAYERS = 128;

  // response packet headers
  private static final int PACKET_HEADER = -1;

  private static final int SPLIT_PACKET_INDICATOR = -2;

  // response header codes
  private static final byte CHALLENGE_REPLY_HEADER_CODE = 0x41;

  private static final byte SERVERINFO_REPLY_HEADER_CODE = 0x49;

  private static final byte PLAYERINFO_REPLY_HEADER_CODE = 0x44;

  private static final byte SERVERRULES_REPLY_HEADER_CODE = 0x45;

  // request headers
  private static final byte[] A2S_INFO_REQ_HEADER
      = {-1, -1, -1, -1, 0x54
      , 0x53, 0x6F, 0x75, 0x72, 0x63, 0x65, 0x20, 0x45, 0x6E, 0x67, 0x69
      , 0x6E, 0x65, 0x20, 0x51, 0x75, 0x65, 0x72, 0x79, 0x00};

  private static final byte[] A2S_PLAYER_REQ_HEADER = {-1, -1, -1, -1, 0x55};

  private static final byte[] A2S_RULES_REQ_HEADER = {-1, -1, -1, -1, 0x56};

  private static final byte NULL_TERMINATOR = 0;

  private static final int MAX_SPLIT_PACKETS = 20;

  private static final int MAX_DECODED_STRING_LENGTH = 280;

  private static final Logger logger = LogManager.getLogger();

  private final ConcurrentHashMap<InetSocketAddress, CompositeByteBuf>
      splitPackets = new ConcurrentHashMap<>();

  public static String getHostNameAndPort(InetSocketAddress address) {
    return address.getHostString() + ':' + address.getPort();
  }

  @Override
  protected void encode(ChannelHandlerContext ctx,
                        ServerQuery query,
                        List<Object> out) throws Exception {

    ByteBuf buf = ctx.alloc().buffer();

    logger.debug("query is " + query);
    InetSocketAddress address = query.address;

    // Handle server info first
    if (query.serverInfoRequest.isNeeded()) {
      buf.writeBytes(A2S_INFO_REQ_HEADER);
      logger.trace("Sending server info request");

    } else if (query.playerInfoRequest.isNeeded()) {
      buf.writeBytes(A2S_PLAYER_REQ_HEADER);
      buf.writeInt(query.challenge.number());
      logger.trace("Sending player info request");

    } else if (query.serverRulesRequest.isNeeded()) {
      buf.writeBytes(A2S_RULES_REQ_HEADER);
      buf.writeInt(query.challenge.number());
    }

    out.add(new DatagramPacket(buf, address));
  }

  @Override
  protected void decode(ChannelHandlerContext ctx,
                        DatagramPacket packet,
                        List<Object> out) throws Exception {
    logger.debug("decoding packet...");
    ByteBuf buf = packet.content().order(ByteOrder.LITTLE_ENDIAN);

    int type = buf.readInt();
    switch (type) {
      case PACKET_HEADER:
        Optional.ofNullable(decodePayload(ctx, packet))
                .ifPresent(out::add);
        break;
      case SPLIT_PACKET_INDICATOR:
        Optional.ofNullable(resolveSplitPacket(ctx, packet))
                .ifPresent(out::add);
        break;
      default:
        logger.warn("unknown packet type: {}", buf);
        // not our packet, not our business
        out.add(packet);
    }
  }

  private ServerResponse decodePayload(ChannelHandlerContext ctx,
                                       DatagramPacket packet) {

    logger.trace("decoding packet payload...");
    ByteBuf buf = packet.content().order(ByteOrder.LITTLE_ENDIAN);

    byte headerType = buf.readByte();
    BiFunction<InetSocketAddress, ByteBuf, ServerResponse> decoder;
    switch (headerType) {
      case SERVERINFO_REPLY_HEADER_CODE:
        decoder = this::decodeServerInfo;
        break;
      case PLAYERINFO_REPLY_HEADER_CODE:
        decoder = this::decodePlayerInfos;
        break;
      case CHALLENGE_REPLY_HEADER_CODE:
        decoder = this::decodeChallenge;
        break;
      case SERVERRULES_REPLY_HEADER_CODE:
        decoder = this::decodeServerRules;
        break;
      default:
        throw new IllegalStateException("Unexpected payload header type: " +
                                            Integer.toHexString(headerType));
    }
    return decoder.apply(packet.sender(), buf);
  }

  private ServerResponse resolveSplitPacket(ChannelHandlerContext ctx,
                                            DatagramPacket packet) {
    logger.trace("decoding split packet...");
    // decode the split packet header
    ByteBuf buf = packet.content().order(ByteOrder.LITTLE_ENDIAN);
    long packetGroup = buf.readInt();
    boolean isCompressed = packetGroup >> 31 == 1;
    int totalPackets = buf.readByte();
    int packetNumber = buf.readByte();
    /* short size = */
    buf.readShort();  // may be used later.
    InetSocketAddress key = packet.sender();

    // bail out on ridiculous amount of split packets
    if (totalPackets > MAX_SPLIT_PACKETS) {
      throw new IllegalStateException("Too many split packets from " + key);
    }

    // retrieve the composite buffer and add the new packet buffer
    CompositeByteBuf cbuf = splitPackets.compute(key, (k, v) ->
        Optional.ofNullable(v)
                .orElseGet(() -> buf.alloc().compositeBuffer(totalPackets))
                .addComponent(packetNumber, buf.slice().retain()));
    cbuf.writerIndex(cbuf.capacity());

    // if the composite buffer is full, send the reassembled response.
    if (cbuf.numComponents() == cbuf.maxNumComponents()) {
      ByteBuf result;
      if (isCompressed) {
        result = decodeBZip2Packet(cbuf);
      } else {
        result = cbuf.slice().retain();
      }
      DatagramPacket composedPacket = new DatagramPacket(result,
                                                         packet.recipient(),
                                                         packet.sender());
      composedPacket.content().readInt(); // consume the PACKET_HEADER

      return decodePayload(ctx, composedPacket);
    }

    return null;
  }

  private ByteBuf decodeBZip2Packet(ByteBuf buf) {
    int uncompressedLength = buf.readInt();
    int expectedCrc32 = buf.readInt();
    ByteBuf writeBuf = buf.alloc().buffer(uncompressedLength)
                          .order(ByteOrder.LITTLE_ENDIAN);

    try (BZip2InputStream inputStream =
             new BZip2InputStream(new ByteBufInputStream(buf), false);
         ByteBufOutputStream outputStream =
             new ByteBufOutputStream(writeBuf)) {
      int bytesRead;
      byte[] decoded = new byte[uncompressedLength];
      CRC32 crc32 = new CRC32();
      while ((bytesRead = inputStream.read(decoded)) != -1) {
        outputStream.write(decoded, 0, bytesRead);
        crc32.update(decoded);
      }
      assert crc32.getValue() == expectedCrc32;
      buf.release();
    } catch (IOException ex) {
      logger.catching(ex);
    }
    return writeBuf;
  }

  private ServerInfo decodeServerInfo(InetSocketAddress from, ByteBuf buf) {
    logger.trace("Receiving server info from {}", from);
    ServerInfo serverInfo = new ServerInfo(from)
        .protocol(buf.readByte())
        .name(decodeString(buf))
        .map(decodeString(buf))
        .folder(decodeString(buf))
        .game(decodeString(buf))
        .appId(buf.readShort())
        .players(buf.readByte())
        .maxPlayers(buf.readByte())
        .bots(buf.readByte())
        .serverType(buf.readByte())
        .os(buf.readByte())
        .visibility(buf.readByte())
        .vac(buf.readByte());

    // handle The Ship stuff here
    if (serverInfo.getAppId() >= 2400 &&
        serverInfo.getAppId() <= 2403 ||
        serverInfo.getAppId() == 2412) {
      serverInfo.mode(buf.readByte())
                .witness(buf.readByte())
                .duration(buf.readByte());
    }
    serverInfo.version(decodeString(buf));

    byte edf = buf.getByte(buf.readerIndex());

    byte PORT_MASK = (byte) 0x80;
    byte STEAMID_MASK = 0x10;
    byte SPECPORT_MASK = 0x40;
    byte KEYWORDS_MASK = 0x20;
    byte GAMEID_MASK = 0x01;

    if ((edf & PORT_MASK) > 0) {
      serverInfo.gamePort(buf.readShort());
    }
    if ((edf & STEAMID_MASK) > 0) {
      if (buf.isReadable(8)) {
        serverInfo.serverSteamId(buf.readLong());
      }
    }
    if ((edf & SPECPORT_MASK) > 0) {
      if (buf.isReadable(2)) {
        serverInfo.specPort(buf.readShort())
                  .specName(decodeString(buf));
      }
    }
    if ((edf & KEYWORDS_MASK) > 0) {
      if (buf.isReadable()) {
        serverInfo.keywords(decodeString(buf));
      }
    }
    if ((edf & GAMEID_MASK) > 0) {
      if (buf.isReadable()) {
        serverInfo.gameId(buf.readLong());
      }
    }

    return serverInfo;
  }

  private String decodeString(ByteBuf buf) {

    int len = buf.bytesBefore(NULL_TERMINATOR);
    if (-1 == len) {
      throw new IllegalStateException(
          "No null terminator found when decoding string");
    }
    // check for unexpected string size
    if (len > MAX_DECODED_STRING_LENGTH) {
      throw new IllegalStateException("Unusually large string detected.");
    }
    int index = buf.readerIndex();
    //todo: check for malformed utf
    String result = buf.toString(index,len,Charset.forName("UTF-8"));
    buf.skipBytes(len+1);//skip null terminator
    return result;
  }

  private PlayerInfos decodePlayerInfos(InetSocketAddress from, ByteBuf buf) {
    logger.trace("Receiving player info from {}", from);
    PlayerInfos playerInfos = new PlayerInfos(from);

    int numPlayers = buf.readByte();
    assertNotTooManyPlayers(numPlayers);

    List<PlayerInfo> players =
        IntStream.range(0, numPlayers)
                 .mapToObj(i -> {
                   int index = buf.readByte();
                   String name = decodeString(buf);
                   int score = buf.readInt();
                   float duration = buf.readFloat();
                   return new PlayerInfo(index, name, score, duration);
                 })
                 .collect(Collectors.toList());

    playerInfos.players(players);

    return playerInfos;
  }

  private void assertNotTooManyPlayers(int numPlayers) {
    if (numPlayers > MAX_PLAYERS) {
      throw new DecodeException("Too many players:" + numPlayers);
    }
  }

  private ChallengeResponse decodeChallenge(InetSocketAddress from,
                                            ByteBuf buf) {
    logger.trace("Receiving challenge response from {}", from);

    return new ChallengeResponse(from)
        .challenge(buf.order(ByteOrder.BIG_ENDIAN).readInt());
  }

  private ServerRules decodeServerRules(InetSocketAddress from, ByteBuf buf) {
    logger.trace("decoding server rules..");
    assert buf.order() == ByteOrder.LITTLE_ENDIAN;
    int ruleCount = buf.readShort();
    ServerRules serverRules = new ServerRules(from);
    while (ruleCount-- > 0) {
      String name = decodeString(buf);
      String rule = decodeString(buf);
      serverRules.properties.setProperty(name, rule);
    }

    return serverRules;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
      throws Exception {
    logger.warn(cause.toString());
    super.exceptionCaught(ctx, cause);
  }

  private static class DecodeException extends RuntimeException {

    private DecodeException(String s) {
      super(s);
    }
  }

}
