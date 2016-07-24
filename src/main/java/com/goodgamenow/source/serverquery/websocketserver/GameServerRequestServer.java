package com.goodgamenow.source.serverquery.websocketserver;

import com.google.common.util.concurrent.AbstractIdleService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.logging.Logger;

/**
 * A local run server that provides game server statuses. Provides
 * a query request page to allow a user to search and filter game
 * servers on the steam network.
 */
public class GameServerRequestServer extends AbstractIdleService {

  public static final boolean NOSSL = System.getProperty("NOSSL") == null;

  static final int port = Integer.getInteger("PORT", NOSSL ? 80 : 443);

  private static final Logger logger =
      Logger.getLogger(GameServerRequestServer.class.toString());

  private Channel webSocketServerChannel;

  private EventLoopGroup group;

  public static void main(String[] args) throws Exception {
    GameServerRequestServer gsrs = new GameServerRequestServer();

    Runtime.getRuntime().addShutdownHook(new Thread(gsrs::shutDown));

    gsrs.startAsync().awaitTerminated();
  }

  /**
   * Start the service.
   */
  @Override
  protected void startUp() throws CertificateException, SSLException {
    // Configure SSL.
    SelfSignedCertificate ssc = new SelfSignedCertificate();
    SslContext sslCtx =  NOSSL ? null :SslContext.newServerContext(ssc.certificate(), ssc.privateKey());

    this.group = new NioEventLoopGroup();

    this.webSocketServerChannel = new ServerBootstrap()
        .group(this.group)
        .channel(NioServerSocketChannel.class)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new WebSocketServerChannelInitializer(sslCtx,"/ws"))
        .bind(port)
        .channel();

    String startupMsg = "Open browser to " + (NOSSL ? "http" : "https") +
        "://127.0.0.1:" + port + '/';
    logger.info(startupMsg);

  }


  /**
   * Stop the service.
   */
  @Override
  protected void shutDown() {
    logger.info("Shutting down GameServerRequestServer...");
    if (webSocketServerChannel != null) {
      webSocketServerChannel.eventLoop().shutdownGracefully();
    }
  }

}
