package com.goodgamenow.source.serverquery.websocketserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression
    .WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: Joe Nellis
 * Date: 2/6/2016
 * Time: 5:44 PM
 */
public class WebSocketServerChannelInitializer
    extends ChannelInitializer<SocketChannel> {

  private static Logger logger = LogManager.getLogger();

  private final SslContext sslContext;

  private final String webSocketPath;

  private final EventLoopGroup udpLoop;

  public WebSocketServerChannelInitializer(SslContext sslContext,
                                           String webSocketPath) {
    this.sslContext = sslContext;
    this.webSocketPath = webSocketPath;
    this.udpLoop = new NioEventLoopGroup();

  }

  @Override
  protected final void initChannel(SocketChannel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    if (null != sslContext) {
      pipeline.addLast(sslContext.newHandler(ch.alloc()));
    }
    pipeline.addLast(new HttpServerCodec())
            .addLast(new HttpObjectAggregator(65536))
            .addLast(new WebSocketServerCompressionHandler())
            .addLast(new WebSocketServerProtocolHandler(webSocketPath, null,
                                                        true))
            .addLast(new WebSocketIndexPageHandler(webSocketPath))
            .addLast(new WebSocketHandler())
            .addLast(new MasterQueryBootstrapHandler(udpLoop))
       //     .addLast(new LoggingHandler(LogLevel.INFO))
            .addLast(new ServerQueryBootstrapHandler(udpLoop))
    ;
  }
}
