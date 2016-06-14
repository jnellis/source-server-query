/*
 * MasterClientBootstrap.java
 *
 * Copyright (c) 2016.  Joe Nellis
 * Distributed under MIT License. See accompanying file License.txt or at
 * http://opensource.org/licenses/MIT
 *
 */

package com.goodgamenow.source.serverquery;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.util.List;

/**
 * Creates a bootstrap to bind a channel to the Source master server and
 * serve a request for game servers matching a filter.
 *
 * Assumes an operation of one query per channel at a time.
 *
 * Usage:
 * <pre>{@code
 *   Bootstrap mcb = new MasterClientBootstrap(eventLoop, query);
 *   ChannelFuture future = mcb.bind(0);
 *   future.channel().closeFuture().addListener( (fut)-> {
 *      List addresses = mcb.getResults();
 *      // do something with these.
 *   });
 * }</pre>
 */
public class MasterClientBootstrap extends Bootstrap {

  private final MasterQueryHandler queryHandler;

  public MasterClientBootstrap(EventLoopGroup eventLoopGroup,
                               MasterQuery query) {

    this.queryHandler = new MasterQueryHandler(query);

    this.group(eventLoopGroup)
        .channel(NioDatagramChannel.class)
        .option(ChannelOption.SO_BROADCAST, Boolean.TRUE)
        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .handler(queryHandler)
    ;
  }

  public List<String> getResults() {
    return queryHandler.getResults();
  }

  public long getRuntime() {
    return queryHandler.getRuntime();
  }
}