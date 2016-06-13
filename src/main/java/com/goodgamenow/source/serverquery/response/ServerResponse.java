package com.goodgamenow.source.serverquery.response;

import com.goodgamenow.source.serverquery.ServerQuery;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Marker interface for a query response.
 * <p>
 * User: Joe Nellis
 * Date: 6/27/2015
 * Time: 1:58 PM
 */
public abstract class ServerResponse {

  private final InetSocketAddress from;

  private final long timeReceived;

  private long latency;

  protected ServerResponse(InetSocketAddress from) {
    this.from = from;
    this.timeReceived = System.currentTimeMillis();
  }

  public abstract ServerQuery update(ServerQuery query);

  public abstract QueryResult mergeInto(
      Map<InetSocketAddress, QueryResult> resultMap);

  public long timeReceived() {
    return timeReceived;
  }

  public InetSocketAddress from() {
    return from;
  }

  public long latency() {
    return latency;
  }

  public ServerResponse latency(long latency) {
    this.latency = latency;
    return this;
  }
}
