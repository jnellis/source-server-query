package com.goodgamenow.source.serverquery;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Assumes an operation of one query per channel at a time.
 * <p>
 * User: Joe Nellis
 * Date: 1/24/2016
 * Time: 6:58 PM
 */
public class MasterClientBootstrap extends Bootstrap {

  private final List<String> results =
      Collections.synchronizedList(new ArrayList<>());

  private final MasterQueryHandler queryHandler;

  public MasterClientBootstrap(EventLoopGroup eventLoopGroup,
                               MasterQuery query) {

    this.queryHandler = new MasterQueryHandler(query, results);

    this.group(eventLoopGroup)
        .channel(NioDatagramChannel.class)
        .option(ChannelOption.SO_BROADCAST, true)
        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .handler(new ChannelInitializer<DatagramChannel>() {
          @Override
          protected void initChannel(DatagramChannel ch) {
            ch.pipeline()
              //    .addLast(new LoggingHandler(LogLevel.INFO))
              .addLast(queryHandler)
            ;
          }
        })
    ;
  }

  public List<String> getResults() {
    return Collections.unmodifiableList(results);
  }

  public long getRuntime() {
    return queryHandler.getRuntime();
  }
}