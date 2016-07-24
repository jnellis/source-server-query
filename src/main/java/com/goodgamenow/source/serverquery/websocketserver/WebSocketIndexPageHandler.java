package com.goodgamenow.source.serverquery.websocketserver;

import groovy.lang.Writable;
import groovy.text.Template;
import groovy.text.markup.MarkupTemplateEngine;
import groovy.text.markup.TemplateConfiguration;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * User: Joe Nellis
 * Date: 7/4/2016
 * Time: 5:31 PM
 */
public class WebSocketIndexPageHandler
    extends SimpleChannelInboundHandler<FullHttpRequest> {

  private static Logger logger = LogManager.getLogger();

  private final String webSocketPath;

  private final MarkupTemplateEngine engine;

  public WebSocketIndexPageHandler(String webSocketPath) {

    this.webSocketPath = webSocketPath;

    TemplateConfiguration configuration = new TemplateConfiguration();
    configuration.setAutoIndent(true);
    configuration.setCacheTemplates(false);
    configuration.setExpandEmptyElements(true);
    engine = new MarkupTemplateEngine(configuration);


  }

  @Override
  protected void channelRead0(
      ChannelHandlerContext ctx,
      FullHttpRequest req)
      throws Exception {


    // Handle a bad request.
    if (!req.decoderResult().isSuccess()) {
      sendHttpResponse(ctx, req,
                       new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
      return;
    }

    // Allow only GET methods.
    if (req.method() != GET) {
      sendHttpResponse(ctx, req,
                       new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
      return;
    }

    // Send the demo page and favicon.ico
    if ("/".equals(req.uri())) {

      Template template = engine.createTemplateByPath("index.tpl");
      HashMap<String, Object> model = new HashMap<>();
      model.put("webSocketLocation", getWebSocketLocation(req));
      Writable writable = template.make(model);
      Writer writer = writable.writeTo(new StringWriter());

      ByteBuf content = Unpooled
          .copiedBuffer(writer.toString().getBytes(CharsetUtil.UTF_8));
      FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

      res.headers()
         .set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8")
         .set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

      sendHttpResponse(ctx, req, res);
      return;
    }
    if ("/favicon.ico".equals(req.uri())) {
      FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
      sendHttpResponse(ctx, req, res);
      return;
    }
    sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
                                                           NOT_FOUND));

  }

  private void sendHttpResponse(
      ChannelHandlerContext ctx,
      FullHttpRequest req,
      FullHttpResponse res) {

    // Generate an error page if response getStatus code is not OK (200).
    if (res.status().code() != 200) {
      ByteBuf buf = Unpooled
          .copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
      res.content().writeBytes(buf);
      buf.release();
      res.headers()
         .set(HttpHeaderNames.CONTENT_LENGTH, res.content().readableBytes());
    }

    // Send the response and close the connection if necessary.
    ChannelFuture f = ctx.channel().writeAndFlush(res);
    if (!isKeepAlive(req) || res.status().code() != 200) {
      f.addListener(ChannelFutureListener.CLOSE);
    }
  }

  private String getWebSocketLocation(FullHttpRequest req) {
    String location = req.headers().get(HttpHeaderNames.HOST) + webSocketPath;
    if (GameServerRequestServer.NOSSL) {
      return "ws://" + location;
    } else {
      return "wss://" + location;
    }
  }

  private boolean isKeepAlive(FullHttpRequest req) {
    String connection = req.headers().get(HttpHeaderNames.CONNECTION);
    return HttpHeaderValues.KEEP_ALIVE.equals(connection);
  }
}
