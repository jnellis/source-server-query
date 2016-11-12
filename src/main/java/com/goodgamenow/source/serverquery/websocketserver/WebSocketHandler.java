package com.goodgamenow.source.serverquery.websocketserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodgamenow.source.serverquery.MasterQuery;
import com.goodgamenow.source.serverquery.MasterQuery.Region;
import com.goodgamenow.source.serverquery.ServerQuery;
import com.goodgamenow.source.serverquery.response.QueryResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;

import static com.goodgamenow.source.serverquery.MasterQuery.Region.WORLD;

/**
 * WebSockets proxy for game server info. Default homepage provides
 * form functions to query via web sockets.
 */
public class WebSocketHandler
    extends MessageToMessageCodec<TextWebSocketFrame, QueryResult> {

  private static Logger logger = LogManager.getLogger();

  ObjectMapper mapper = new ObjectMapper();

  @Override
  protected void encode(ChannelHandlerContext ctx,
                        QueryResult result,
                        List<Object> out) throws Exception {
    String json = result.toJson(mapper);
    TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(json);
    out.add(textWebSocketFrame);
  }


  @Override
  protected void decode(ChannelHandlerContext ctx,
                        TextWebSocketFrame msg,
                        List<Object> out) throws Exception {

    // parse request type and id and whether server queries from the results
    // of this master query should request the server rules as well.
    JsonNode params = mapper.readTree(msg.text());
    boolean needsRules = false;
    String serverRequestType = params.get("type").asText("unknown");
    String queryId = params.get("id").asText(ServerQuery.NO_ID);

    switch (serverRequestType) {
      case "master":
        Region region = Region.valueOf(params.get("region")
                                             .asText(WORLD.name()));
        String filter = params.get("filter").asText();
        needsRules = params.get("needsRules").asBoolean();
        ServerQuery template =
            new ServerQuery(new InetSocketAddress("127.0.0.1", 0),
                            queryId,
                            ServerQuery.ServerInfoRequest.NEEDED,
                            ServerQuery.PlayerInfoRequest.NEEDED,
                            needsRules ?
                            ServerQuery.ServerRulesRequest.NEEDED :
                            ServerQuery.ServerRulesRequest.NOT_NEEDED,
                            ServerQuery.Retries.MAX_RETRIES,
                            ServerQuery.Challenge.RESET);
        MasterQuery mq = new MasterQuery(region, filter, template);

        ctx.fireChannelRead(mq);
        break;

      case "server":
//        List<String> addresses = Arrays.asList(params[1].split(","));
//        ctx.fireChannelRead(addresses);
        break;

      default:
        ctx.writeAndFlush(new TextWebSocketFrame(
            serverRequestType + " is not a valid request type."
        ));
    }

  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
  }

}
