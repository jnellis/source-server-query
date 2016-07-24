package com.goodgamenow.source.serverquery.response;

import com.goodgamenow.source.serverquery.ServerQuery;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * User: Joe Nellis
 * Date: 6/24/2015
 * Time: 6:30 PM
 */
public class PlayerInfos extends ServerResponse {

  private List<PlayerInfo> players = new ArrayList<>();

  public PlayerInfos(InetSocketAddress from) {
    super(from);
  }

//  @Override
//  public ResultMerger resultMerger() {
//    return resultMap ->
//        resultMap.merge(from(),                // lookup key
//                        new QueryResult(this), // nu value, never null
//                        (orig, nu) -> orig.playerInfos(nu.playerInfos().get()));
//  }
//
//  @Override
//  public QueryUpdater queryUpdater() {
//    return query ->
//        new ServerQuery(query.address,
//                        query.serverInfoRequest,
//                        ServerQuery.PlayerInfoRequest.NOT_NEEDED,
//                        query.serverRulesRequest,
//                        ServerQuery.Retries.MAX_RETRIES,   // reset retries
//                        query.challenge);
//  }

  @Override
  public ServerQuery update(ServerQuery query) {
    return new ServerQuery(query.address,
                           query.queryId,
                           query.serverInfoRequest,
                           ServerQuery.PlayerInfoRequest.NOT_NEEDED,
                           query.serverRulesRequest,
                           ServerQuery.Retries.MAX_RETRIES,   // reset retries
                           query.challenge);
  }

  @Override
  public QueryResult mergeInto(Map<InetSocketAddress, QueryResult> resultMap) {
    return resultMap.merge(
        from(),                // lookup key
        new QueryResult(this), // nu value, never null
        (orig, nu) -> orig.playerInfos(nu.playerInfos().get()));
  }

  public List<PlayerInfo> players() {
    return players;
  }

  public PlayerInfos players(List<PlayerInfo> players) {
    this.players = new ArrayList<>(players);
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("PlayerInfos{");
    sb.append("players=").append(players);
    sb.append('}');
    return sb.toString();
  }

  public Stream<PlayerInfo> stream() {
    return players.stream();
  }
}
