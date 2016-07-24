package com.goodgamenow.source.serverquery.websocketserver;

import com.goodgamenow.source.serverquery.MasterClientBootstrap;
import com.goodgamenow.source.serverquery.MasterQuery;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Joe Nellis
 * Date: 7/6/2016
 * Time: 10:13 AM
 */
public class MasterQueryBootstrapHandler
    extends SimpleChannelInboundHandler<MasterQuery> {

  private static Logger logger = LogManager.getLogger();

  private final EventLoopGroup udpLoop;

  private final List<Bootstrap> unfinished = new ArrayList<>();

  public MasterQueryBootstrapHandler(EventLoopGroup udpLoop) {
    this.udpLoop = udpLoop;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, MasterQuery msg)
      throws Exception {

    Bootstrap bootstrap = new MasterClientBootstrap(udpLoop, msg, ctx);
    unfinished.add(bootstrap);
    bootstrap.bind(0)
             .channel().closeFuture()
             .addListener(future -> unfinished.remove(bootstrap));
  }

}
