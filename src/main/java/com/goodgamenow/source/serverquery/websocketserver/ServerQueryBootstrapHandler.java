package com.goodgamenow.source.serverquery.websocketserver;

import com.goodgamenow.source.serverquery.ServerQuery;
import com.goodgamenow.source.serverquery.ServerQueryBootstrap;
import io.netty.channel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: Joe Nellis
 * Date: 7/6/2016
 * Time: 10:24 AM
 */
public class ServerQueryBootstrapHandler
    extends SimpleChannelInboundHandler<ServerQuery> {

  private static Logger logger = LogManager.getLogger();

  private final EventLoopGroup udpLoop;

  private ServerQueryBootstrap bootstrap;

  private Channel serverQueryChannel;

  public ServerQueryBootstrapHandler(EventLoopGroup udpLoop) {
    this.udpLoop = udpLoop;
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    bootstrap = new ServerQueryBootstrap(udpLoop, ctx);
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    bootstrap = null;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    if (serverQueryChannel == null || !serverQueryChannel.isOpen()) {
      serverQueryChannel = bootstrap.bind(0).sync().channel();
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    ChannelPromise promise = serverQueryChannel
                                .newPromise()
                                .addListener(f -> serverQueryChannel = null);

    serverQueryChannel.close(promise);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
      throws Exception {
    logger.error(cause);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ServerQuery query)
      throws Exception {
    // must flush each query one at a time.
    serverQueryChannel.writeAndFlush(query);
  }
}
