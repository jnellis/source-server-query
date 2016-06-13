package com.goodgamenow.source.serverquery;

import com.goodgamenow.source.serverquery.response.QueryResult;
import com.goodgamenow.source.serverquery.response.ServerResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * User: Joe Nellis
 * Date: 11/2/2015
 * Time: 6:23 PM
 */
public class  QueryResultHandler
    extends SimpleChannelInboundHandler<ServerResponse> {
  private final Logger logger = LogManager.getLogger();

  private final Map<InetSocketAddress, ServerQuery> reconcileMap;

  private final Map<InetSocketAddress, QueryResult> resultMap;

  public QueryResultHandler(Map<InetSocketAddress, ServerQuery> reconcileMap,
                            Map<InetSocketAddress, QueryResult> resultMap) {
    this.reconcileMap = reconcileMap;
    this.resultMap = resultMap;
  }


  @Override
  protected void channelRead0(ChannelHandlerContext ctx,
                              ServerResponse response) throws Exception {

    InetSocketAddress addrKey = response.from();
    //update or remove finished queries.
    reconcileMap.computeIfPresent(addrKey, (notUsed, query) -> {
      // record latency
      response.latency(response.timeReceived() - query.startTime.inMillis());
      logger.debug("latency {} - {}ms", addrKey, response.latency() );

      ServerQuery updatedQuery = response.update(query);

      if (updatedQuery.isFinished()) {
        return null;  // returning null removes mapping from reconcileMap
      }
      // resend this query so it can finish other requests.
      ctx.writeAndFlush(updatedQuery);
      return updatedQuery;
    });

    // update the query result.
    response.mergeInto(resultMap);
  }
}
