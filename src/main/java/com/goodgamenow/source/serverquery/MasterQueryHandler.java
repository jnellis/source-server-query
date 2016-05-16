package com.goodgamenow.source.serverquery;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Handles sending a query and receiving game server data with the
 * Source Master Server.
 * <p>
 * Sends the query when the channel is bound and becomes active. Decodes
 * responses into a list which can be retrieved with a call to
 * #getResults.
 *
 * @see MasterQuery#MasterQuery(MasterQuery.Region, String)
 * @see MasterResponse#MasterResponse(String, String, long)
 */
class MasterQueryHandler
    extends SimpleChannelInboundHandler<DatagramPacket> {

  private final MasterQuery query;

  private final List<String> results;

  private String lastAddress;

  private long startTime, finishTime;

  private final InetSocketAddress masterAddress;

  public MasterQueryHandler(MasterQuery query,
                            List<String> results) {
    this.query = query;
    this.lastAddress = MasterQuery.DEFAULT_IP;
    this.results = results;
    this.masterAddress = new InetSocketAddress(MasterQuery.MASTER_SERVER,
                                               MasterQuery.MASTER_SERVER_PORT);
  }

  public List<String> getResults() {
    return results;
  }

  @Override
  protected final void channelRead0(ChannelHandlerContext ctx,
                                    DatagramPacket msg)
      throws UnsupportedEncodingException {

    ByteBuf buf = msg.content();

    // decode response header
    String header = decodeIpAddress(buf);
    assert MasterQuery.EXPECTED_HEADER_STRING.equals(header);

    while (buf.isReadable(6)) {
      lastAddress = decodeIpAddress(buf);
      // A last address of 0.0.0.0:0 denotes the end of transmission.
      if (MasterQuery.DEFAULT_IP.equals(lastAddress)) {
        ctx.flush();
        ctx.close();
        finishTime = System.currentTimeMillis();
        return;
      }
      results.add(lastAddress);
    }
    assert buf.readableBytes() == 0;
    // ask for more results
    this.channelActive(ctx);
  }

  private static String decodeIpAddress(ByteBuf buf) {
    assert 0 == (buf.readableBytes() % 6);

    return String.valueOf(decodeAddress(buf) + ':' + decodePort(buf));
  }

  long getRuntime() {
    long result = finishTime - startTime;
    if (0L > result) {
      result = 0L;
    }
    return result;
  }

  @Override
  public final void channelActive(ChannelHandlerContext ctx)
      throws UnsupportedEncodingException {

    if (0L == startTime) {
      startTime = System.currentTimeMillis();
    }

    // create the query buffer
    ByteBuf buf = ctx.alloc().buffer()
                     .writeByte(MasterQuery.MSG_TYPE)
                     .writeByte(query.region.code)
                     .writeBytes(lastAddress.getBytes("US-ASCII"))
                     .writeByte(MasterQuery.NULL_TERMINATOR)
                     .writeBytes(query.filter.getBytes("UTF-8"))
                     .writeByte(MasterQuery.NULL_TERMINATOR);

    // Master server results are paged, sending last address received
    // back to master will give us another page.
    ctx.writeAndFlush(new DatagramPacket(buf, masterAddress));
  }

  private static String decodeAddress(ByteBuf buf) {
    return "" +
        Byte.toUnsignedInt(buf.readByte()) + '.' +
        Byte.toUnsignedInt(buf.readByte()) + '.' +
        Byte.toUnsignedInt(buf.readByte()) + '.' +
        Byte.toUnsignedInt(buf.readByte());
  }

  private static int decodePort(ByteBuf buf) {
    return buf.readShort();
  }

}