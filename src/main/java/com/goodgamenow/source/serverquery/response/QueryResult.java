package com.goodgamenow.source.serverquery.response;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * User: Joe Nellis
 * Date: 6/26/2015
 * Time: 2:21 PM
 */
public class QueryResult {

  private ServerInfo serverInfo;

  private PlayerInfos playerInfos;

  private ServerRules serverRules;

  private ChallengeResponse challenge;

  private long[] latencies = new long[4];

  public QueryResult(ServerInfo serverInfo) {
    this.serverInfo(serverInfo);
  }

  public final QueryResult serverInfo(ServerInfo serverInfo) {
    this.serverInfo = serverInfo;
    latencies[0] = serverInfo.latency();
    return this;
  }

  public QueryResult(PlayerInfos playerInfos) {
    this.playerInfos(playerInfos);
  }

  public final QueryResult playerInfos(PlayerInfos playerInfos) {
    this.playerInfos = playerInfos;
    latencies[1] = playerInfos.latency();
    return this;
  }

  public QueryResult(ServerRules serverRules) {
    this.serverRules(serverRules);
  }

  public final QueryResult serverRules(ServerRules serverRules) {
    this.serverRules = serverRules;
    latencies[2] = serverRules.latency();
    return this;
  }

  public QueryResult(ChallengeResponse challenge) {
    this.challenge(challenge);
  }

  public final QueryResult challenge(ChallengeResponse challenge) {
    this.challenge = challenge;
    latencies[3] = challenge.latency();
    return this;
  }

  public Optional<ChallengeResponse> challenge() {
    return Optional.ofNullable(challenge);
  }

  public Optional<ServerInfo> serverInfo() {
    return Optional.ofNullable(serverInfo);
  }

  public Optional<PlayerInfos> playerInfos() {
    return Optional.ofNullable(playerInfos);
  }

  public Optional<ServerRules> serverRules() {
    return Optional.ofNullable(serverRules);
  }

  public OptionalDouble averageLatency() {
    return Arrays.stream(latencies)
                 .filter(l -> l > 0)
                 .average();

  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("QueryResult{");
    sb.append("serverInfo=").append(serverInfo);
    sb.append(", playerInfos=").append(playerInfos);
    sb.append('}');
    return sb.toString();
  }
}
