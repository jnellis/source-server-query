package com.goodgamenow.source.serverquery.response;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * User: Joe Nellis
 * Date: 6/26/2015
 * Time: 2:21 PM
 */
public class QueryResult extends ResultBasics{

  private ServerInfo serverInfo;

  private PlayerInfos playerInfos;

  private ServerRules serverRules;

  private ChallengeResponse challenge;

  public QueryResult(ServerInfo serverInfo) {
    super(serverInfo);
    this.serverInfo(serverInfo);
  }

  public final QueryResult serverInfo(ServerInfo serverInfo) {
    this.serverInfo = serverInfo;
    this.latencies[0] = serverInfo.latency();
    return this;
  }

  public QueryResult(PlayerInfos playerInfos) {
    super(playerInfos);
    this.playerInfos(playerInfos);
  }

  public final QueryResult playerInfos(PlayerInfos playerInfos) {
    this.playerInfos = playerInfos;
    latencies[1] = playerInfos.latency();
    return this;
  }

  public QueryResult(ServerRules serverRules) {

    super(serverRules);
    this.serverRules(serverRules);
  }

  public final QueryResult serverRules(ServerRules serverRules) {
    this.serverRules = serverRules;
    latencies[2] = serverRules.latency();
    return this;
  }

  public QueryResult(ChallengeResponse challenge) {
    super(challenge);
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

  public Optional<ServerInfo> getServerInfo() {
    return Optional.ofNullable(serverInfo);
  }

  public Optional<PlayerInfos> playerInfos() {
    return Optional.ofNullable(playerInfos);
  }

  public Optional<ServerRules> serverRules() {
    return Optional.ofNullable(serverRules);
  }

  public String toJson(ObjectMapper mapper) {
    try {
      StringWriter writer = new StringWriter();
      writer.append("{")
            .append("\"from\":\"")
            .append(this.from.toString())
            .append("\"");


      if (serverInfo != null) {
        writer.append(", \"serverInfo\":");
        mapper.writeValue(writer, serverInfo);
      }
      if (playerInfos != null) {
        writer.append(", \"playerInfos\":");
        mapper.writeValue(writer, playerInfos);
      }
      if (serverRules != null) {
        writer.append(", \"serverRules\":");
        mapper.writeValue(writer, serverRules);
      }
      OptionalDouble avgLatency = averageLatency();
      if (avgLatency.isPresent()) {
        writer.append(", \"avgLatency\":");
        mapper.writeValue(writer, avgLatency.getAsDouble());
      }
      writer.append("}").flush();
      return writer.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }

  public OptionalDouble averageLatency() {
    return Arrays.stream(latencies)
                 .filter(latency -> latency > 0)
                 .average();

  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("QueryResult{");
    sb.append("getServerInfo=").append(serverInfo);
    sb.append(", playerInfos=").append(playerInfos);
    sb.append('}');
    return sb.toString();
  }
}

class ResultBasics {
  long[] latencies = new long[4];
  InetSocketAddress from;
  ResultBasics(ServerResponse response){
    this.from = response.from();
  }
}