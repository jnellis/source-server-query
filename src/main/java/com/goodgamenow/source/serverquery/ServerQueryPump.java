package com.goodgamenow.source.serverquery;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * User: Joe Nellis
 * Date: 6/8/2015
 * Time: 8:56 PM
 */
@ChannelHandler.Sharable
public class ServerQueryPump extends ChannelDuplexHandler {

  private final Map<Channel, Map<InetSocketAddress, ServerQuery>> reconcileMaps;

  private final Semaphore available = new Semaphore(1, true);

  private static final Logger logger = LogManager.getLogger();

  public ServerQueryPump(
      Map<Channel, Map<InetSocketAddress, ServerQuery>> reconcileMaps) {
    this.reconcileMaps = reconcileMaps;
  }

  public Map<Channel, Map<InetSocketAddress, ServerQuery>> getReconcileMaps() {
    return reconcileMaps;
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
      throws Exception {
    if (evt instanceof IdleStateEvent) {
      switch (((IdleStateEvent) evt).state()){
        case READER_IDLE:
          logger.trace("Reprocessing timeouts.");
          // Don't bother us if another thread is currently reprocessing timeouts
          if (available.tryAcquire()) {
            try {
              reprocessTimeouts(ctx);
            } catch (Throwable t) {
              logger.warn("reprocessTimeouts has thrown.", t);
              throw t;
            } finally {
              available.release();
            }
          }
          // all others fall through and do nothing
          break;
        case WRITER_IDLE: doNothing();
          break;
        default:
      }
    }
  }

  private void doNothing(){}

  /**
   * Go through all outstanding queries, pick the ones that have timed out
   * because of no response yet and resend them.
   */
  private void reprocessTimeouts(ChannelHandlerContext ctx)  {
    // for each channel specific reconciliation map
    reconcileMaps.forEach((channel, map) -> {
      // copy server queries to avoid concurrent map operation error
      List<ServerQuery> outstandingQueries = new ArrayList<>(map.values());

      // get a list of addresses that have timed out queries
      outstandingQueries
          .stream()
          .filter(ServerQuery::isTimedOut)
          .forEach(q -> map.computeIfPresent(
              q.address,
              (InetSocketAddress addr, ServerQuery query) -> {
                // remove finished queries or those without retries left.
                if ((query.retries.remaining() < 1) || query.isFinished()) {
                  return null;  // returning null removes the mapping.
                }
                ServerQuery updatedQuery =
                    new ServerQuery(query.address,
                                    query.serverInfoRequest,
                                    query.playerInfoRequest,
                                    query.serverRulesRequest,
                                    query.retries.decrement(),
                                    query.challenge);
                // resend it.
                channel.pipeline().writeAndFlush(updatedQuery);
                return updatedQuery;
              }
          ));
    });
  }

  /**
   * Intercept outgoing ServerQuery requests and make a note of them
   * in case we don't hear back after a timeout.
   *
   * @param ctx
   * @param msg
   * @param promise
   */
  @Override
  public void write(ChannelHandlerContext ctx,
                    Object msg,
                    ChannelPromise promise) throws Exception {
    if (msg instanceof ServerQuery) {
      promise.addListener(
          future -> {
            ServerQuery query = (ServerQuery) msg;
            reconcileMaps.get(ctx.channel()).put(query.address, query);
          }
      );
    }
    super.write(ctx, msg, promise);
  }
}