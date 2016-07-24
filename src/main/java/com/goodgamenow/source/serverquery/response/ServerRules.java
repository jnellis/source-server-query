package com.goodgamenow.source.serverquery.response;

import com.goodgamenow.source.serverquery.ServerQuery;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Properties;

/**
 * User: Joe Nellis
 * Date: 6/26/2015
 * Time: 2:23 PM
 */
public class ServerRules extends ServerResponse {

  public final Properties properties = new Properties();

  public ServerRules(InetSocketAddress from) {
    super(from);
  }

  @Override
  public String toString() {
    return properties.toString();
  }

//  @Override
//  public ResultMerger resultMerger() {
//    return resultMap ->
//        resultMap.merge(
//            from(),
//            new QueryResult(this),
//            (orig, nu) -> orig.serverRules(nu.serverRules().get()));
//  }
//
//  @Override
//  public QueryUpdater queryUpdater() {
//    return query -> new ServerQuery(query.address,
//                                    query.serverInfoRequest,
//                                    query.playerInfoRequest,
//                                    ServerQuery.ServerRulesRequest.NOT_NEEDED,
//                                    ServerQuery.Retries.MAX_RETRIES,
//                                    query.challenge);
//  }

  @Override
  public ServerQuery update(ServerQuery query) {
    return new ServerQuery(query.address,
                           query.queryId,
                           query.serverInfoRequest,
                           query.playerInfoRequest,
                           ServerQuery.ServerRulesRequest.NOT_NEEDED,
                           ServerQuery.Retries.MAX_RETRIES,
                           query.challenge);
  }

  @Override
  public QueryResult mergeInto(Map<InetSocketAddress, QueryResult> resultMap) {
    return resultMap.merge(
        from(),
        new QueryResult(this),
        (orig, nu) -> orig.serverRules(nu.serverRules().get()));
  }
}
