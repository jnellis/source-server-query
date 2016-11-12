package com.goodgamenow.source.serverquery.response;

import com.goodgamenow.source.serverquery.ServerQuery;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * User: Joe Nellis
 * Date: 6/27/2015
 * Time: 2:00 PM
 */
public class ChallengeResponse extends ServerResponse {

  private int challenge = 0;

  public ChallengeResponse(InetSocketAddress from) {
    super(from);
  }

  public int challenge() {
    return challenge;
  }

  public ChallengeResponse challenge(int challenge) {
    this.challenge = challenge;
    return this;
  }

  //the challenge response isn't really a result of any value other than
  //to facilitate the next request. It is only merged into the query result
  //as another data point for latency.
//  @Override
//  public ResultMerger resultMerger() {
//    return resultMap ->
//        resultMap.merge(from(),
//                        new QueryResult(this),
//                        (orig, nu) -> orig.challenge(nu.challenge().get()));
//  }
//
//  @Override
//  public QueryUpdater queryUpdater() {
//    return query -> new ServerQuery(query.address,
//                                    query.serverInfoRequest,
//                                    query.playerInfoRequest,
//                                    query.serverRulesRequest,
//                                    ServerQuery.Retries.MAX_RETRIES,
//                                    this::challenge);
//  }

  @Override
  public ServerQuery update(ServerQuery query) {
    return new ServerQuery(query.address,
                           query.queryId,
                           query.serverInfoRequest,
                           query.playerInfoRequest,
                           query.serverRulesRequest,
                           ServerQuery.Retries.MAX_RETRIES,
                           this::challenge);
  }

  @Override
  public QueryResult mergeInto(Map<InetSocketAddress, QueryResult> resultMap) {
    return resultMap.merge(
        from(),
        new QueryResult(this),
        (orig, nu) -> orig.challenge(nu.challenge().get()));
  }
}
