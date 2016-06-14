/*
 * MasterQueryHandler.java
 *
 * Copyright (c) 2016.  Joe Nellis
 * Distributed under MIT License. See accompanying file License.txt or at
 * http://opensource.org/licenses/MIT
 *
 */

package com.goodgamenow.source.serverquery;

import com.goodgamenow.source.serverquery.MasterQuery.Region;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles sending a query and receiving game server data with the
 * Source Master Server.
 * <p>
 * Sends the query when the channel is bound and becomes active. Decodes
 * responses into a list which can be retrieved with a call to
 * #getResults.
 *
 * @see MasterQuery#MasterQuery(Region, String)
 * @see MasterResponse#MasterResponse(String, String, long)
 */
class MasterQueryHandler
    extends SimpleChannelInboundHandler<DatagramPacket> {

  /**
   * Master server location
   */
  private static final String MASTER_SERVER = "hl2master.steampowered.com";

  /**
   * Master server port
   */
  private static final int MASTER_SERVER_PORT = 27011;

  /**
   * Initial ip of the request packet and
   * marker string for when the last query result is received.
   */
  private static final String DEFAULT_IP = "0.0.0.0:0";

  /**
   * First byte of the request packet.
   */
  private static final int MSG_TYPE = 0x31;

  /**
   * Master server strings are null terminated
   */
  private static final int NULL_TERMINATOR = 0;

  /**
   * The expected response header string
   */
  private static final String EXPECTED_HEADER_STRING = "255.255.255.255:26122";

  private final MasterQuery query;

  private final List<String> results;

  private final InetSocketAddress masterAddress;

  private String lastAddress;

  private long startTime;

  private long finishTime;

  MasterQueryHandler(MasterQuery query) {
    this.query = query;
    this.lastAddress = DEFAULT_IP;
    this.results = new ArrayList<>();
    this.masterAddress = new InetSocketAddress(MASTER_SERVER,
                                               MASTER_SERVER_PORT);
  }

  public List<String> getResults() {
    return Collections.unmodifiableList(results);
  }

  /**
   * Decodes a master server response datagram packet into a list of
   * game server addresses.
   *
   * @param ctx  channel handler context
   * @param msg  master server response packet
   * @exception UnsupportedEncodingException
   */
  @Override
  protected void channelRead0(ChannelHandlerContext ctx,
                              DatagramPacket msg)
      throws UnsupportedEncodingException {
    ByteBuf buf = msg.content();

    // sanity check
    int ADDR_WIDTH = 6;
    assert (buf.readableBytes() % ADDR_WIDTH) == 0 :
        "Master response byte count is not 6 byte aligned.";

    // decode response header
    String header = decodeIpAddress(buf);
    assert EXPECTED_HEADER_STRING.equals(header);

    while (buf.isReadable(ADDR_WIDTH)) {
      lastAddress = decodeIpAddress(buf);
      // A last address of 0.0.0.0:0 denotes the end of transmission.
      if (DEFAULT_IP.equals(lastAddress)) {
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


  /**
   * Decodes the address and port from a six byte representation
   * 001.002.003.004:00056
   *
   * @param buf master server response buffer
   * @return IpAddress:port representation
   */
  private static String decodeIpAddress(ByteBuf buf) {
    assert 0 == (buf.readableBytes() % 6);

    return String.valueOf(decodeAddress(buf) + ':' + decodePort(buf));
  }

  /**
   * Fires a Datagram packet with its associated query to the master server.
   *
   * @param ctx  channel handler context
   * @exception UnsupportedEncodingException
   */
  @Override
  public void channelActive(ChannelHandlerContext ctx)
      throws UnsupportedEncodingException {

    if (0L == startTime) {
      startTime = System.currentTimeMillis();
    }

    // create the query buffer
    ByteBuf buf = ctx.alloc().buffer()
                     .writeByte(MSG_TYPE)
                     .writeByte(query.region.code)
                     .writeBytes(lastAddress.getBytes("US-ASCII"))
                     .writeByte(NULL_TERMINATOR)
                     .writeBytes(query.filter.getBytes("UTF-8"))
                     .writeByte(NULL_TERMINATOR);

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


  long getRuntime() {
    long result = finishTime - startTime;
    if (0L > result) {
      result = 0L;
    }
    return result;
  }

}