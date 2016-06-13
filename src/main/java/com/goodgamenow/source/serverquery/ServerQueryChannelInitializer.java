package com.goodgamenow.source.serverquery;

import com.goodgamenow.source.serverquery.response.QueryResult;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * User: Joe Nellis
 * Date: 5/23/2015
 * Time: 10:49 PM
 */
public class ServerQueryChannelInitializer
    extends ChannelInitializer<DatagramChannel> {


  private static final long DEFAULT_TIMEOUT = 500;

  private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

  private final Map<Channel, Map<InetSocketAddress, ServerQuery>> reconcileMaps;

  private final Map<InetSocketAddress, QueryResult> queryResultMap;

  // singleton message handlers
  private final ServerQueryPump serverQueryPump;


  public ServerQueryChannelInitializer() {
    this.reconcileMaps = new ConcurrentHashMap<>();
    this.queryResultMap = new ConcurrentHashMap<>();
    serverQueryPump = new ServerQueryPump(reconcileMaps);
  }


  @Override
  protected void initChannel(DatagramChannel ch) throws Exception {
    Map<InetSocketAddress, ServerQuery> reconciliationMap =
        new ConcurrentHashMap<>();
    reconcileMaps.put(ch, reconciliationMap);

    ch.pipeline()
      .addLast("logging", new LoggingHandler(LogLevel.DEBUG))
      .addLast("query-codec", new ServerQueryCodec())
      .addLast("idle-event-maker", new IdleStateHandler(DEFAULT_TIMEOUT,
                                                        DEFAULT_TIMEOUT, 0,
                                                        DEFAULT_TIME_UNIT))
      .addLast("pump", serverQueryPump)
      .addLast("query-result", new QueryResultHandler(reconciliationMap,
                                                      queryResultMap))
    ;
  }


  public Collection<QueryResult> getQueryResults() {
    return queryResultMap.values();
  }

  public boolean reconciliationMapsAreEmpty() {
    return reconcileMaps.values().stream().allMatch(Map::isEmpty);
  }
}
