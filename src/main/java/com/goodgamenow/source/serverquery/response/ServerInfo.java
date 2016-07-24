package com.goodgamenow.source.serverquery.response;

import com.goodgamenow.source.serverquery.ServerQuery;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * The result of a SourceServerQuery, a snapshot of a Source server.
 * User: Joe Nellis
 * Date: 5/24/2015
 * Time: 9:32 AM
 */
public class ServerInfo extends ServerResponse{

  byte protocol;

  String name;

  String map;

  String folder;

  String game;

  short appId;

  byte players;

  byte maxPlayers;

  byte bots;

  byte serverType;

  byte os;

  byte visibility;

  byte vac;

  // the Ship specific fields
  byte mode;

  byte witness;

  byte duration;

  String version;

  byte extraDataFlag;

  // potential fields based on extraDataFlag
  short gamePort;

  long serverSteamId;

  short specPort;

  String specName;

  String keywords;

  long gameId;

  public ServerInfo(InetSocketAddress from) {
    super(from);
  }

  public byte protocol() {
    return protocol;
  }

  public ServerInfo protocol(byte protocol) {
    this.protocol = protocol;
    return this;
  }

  public String name() {
    return name;
  }

  public ServerInfo name(String name) {
    this.name = name;
    return this;
  }

  public String map() {
    return map;
  }

  public ServerInfo map(String map) {
    this.map = map;
    return this;
  }

  public String folder() {
    return folder;
  }

  public ServerInfo folder(String folder) {
    this.folder = folder;
    return this;
  }

  public String game() {
    return game;
  }

  public ServerInfo game(String game) {
    this.game = game;
    return this;
  }

  public short appId() {
    return appId;
  }

  public ServerInfo appId(short appId) {
    this.appId = appId;
    return this;
  }

  public ServerInfo players(byte players) {
    this.players = players;
    return this;
  }

  public byte maxPlayers() {
    return maxPlayers;
  }

  public ServerInfo maxPlayers(byte maxPlayers) {
    this.maxPlayers = maxPlayers;
    return this;
  }

  public byte bots() {
    return bots;
  }

  public ServerInfo bots(byte bots) {
    this.bots = bots;
    return this;
  }

  public byte serverType() {
    return serverType;
  }

  public ServerInfo serverType(byte serverType) {
    this.serverType = serverType;
    return this;
  }

  public byte os() {
    return os;
  }

  public ServerInfo os(byte os) {
    this.os = os;
    return this;
  }

  public byte visibility() {
    return visibility;
  }

  public ServerInfo visibility(byte visibility) {
    this.visibility = visibility;
    return this;
  }

  public byte vac() {
    return vac;
  }

  public ServerInfo vac(byte vac) {
    this.vac = vac;
    return this;
  }

  public byte mode() {
    return mode;
  }

  public ServerInfo mode(byte mode) {
    this.mode = mode;
    return this;
  }

  public byte witness() {
    return witness;
  }

  public ServerInfo witness(byte witness) {
    this.witness = witness;
    return this;
  }

  public byte duration() {
    return duration;
  }

  public ServerInfo duration(byte duration) {
    this.duration = duration;
    return this;
  }

  public String version() {
    return version;
  }

  public ServerInfo version(String version) {
    this.version = version;
    return this;
  }

  public byte extraDataFlag() {
    return extraDataFlag;
  }

  public ServerInfo extraDataFlag(byte extraDataFlag) {
    this.extraDataFlag = extraDataFlag;
    return this;
  }

  public short gamePort() {
    return gamePort;
  }

  public ServerInfo gamePort(short gamePort) {
    this.gamePort = gamePort;
    return this;
  }

  public long serverSteamId() {
    return serverSteamId;
  }

  public ServerInfo serverSteamId(long serverSteamId) {
    this.serverSteamId = serverSteamId;
    return this;
  }

  public short specPort() {
    return specPort;
  }

  public ServerInfo specPort(short specPort) {
    this.specPort = specPort;
    return this;
  }

  public String specName() {
    return specName;
  }

  public ServerInfo specName(String specName) {
    this.specName = specName;
    return this;
  }

  public String keywords() {
    return keywords;
  }

  public ServerInfo keywords(String keywords) {
    this.keywords = keywords;
    return this;
  }

  public long gameId() {
    return gameId;
  }

  public ServerInfo gameId(long gameId) {
    this.gameId = gameId;
    return this;
  }

//  @Override
//  public ResultMerger resultMerger() {
//    return resultMap ->
//        resultMap.merge(from(),
//                        new QueryResult(this),
//                        (orig, nu) -> orig.serverInfo(nu.serverInfo().get()));
//  }

//  @Override
//  public QueryUpdater queryUpdater() {
//    return query -> {
//      // if the server has no players we can turn off playerInfo request
//      ServerQuery.PlayerInfoRequest playerInfoRequest =
//          (players() < 1) ? ServerQuery.PlayerInfoRequest.NOT_NEEDED
//                          : query.playerInfoRequest;
//
//      return new ServerQuery(query.address,
//                             ServerQuery.ServerInfoRequest.NOT_NEEDED,
//                             playerInfoRequest,
//                             query.serverRulesRequest,
//                             ServerQuery.Retries.MAX_RETRIES,   // reset retries
//                             query.challenge);
//    };
//  }

  @Override
  public ServerQuery update(ServerQuery query) {
    // if the server has no players we can turn off playerInfo request
    ServerQuery.PlayerInfoRequest playerInfoRequest =
        (players() < 1) ? ServerQuery.PlayerInfoRequest.NOT_NEEDED
                        : query.playerInfoRequest;

    return new ServerQuery(query.address,
                           query.queryId,
                           ServerQuery.ServerInfoRequest.NOT_NEEDED,
                           playerInfoRequest,
                           query.serverRulesRequest,
                           ServerQuery.Retries.MAX_RETRIES,   // reset retries
                           query.challenge);
  }

  @Override
  public QueryResult mergeInto(Map<InetSocketAddress, QueryResult> resultMap) {
    return resultMap.merge(
        from(),
        new QueryResult(this),
        (orig, nu) -> orig.serverInfo(nu.serverInfo().get()));
  }


  public byte players() {
    return players;
  }

  @Override
  public String toString() {
    return "ServerInfo{" + "from=" + from() + ", timeReceived=" +
        timeReceived() + ", protocol=" + protocol + ", name='" + name + '\''
        + ", map='" + map + '\'' + ", folder='" + folder + '\'' + ", game='"
        + game + '\'' + ", appId=" + appId + ", players=" + players + ", " +
        "maxPlayers=" + maxPlayers + ", bots=" + bots + ", serverType=" +
        serverType + ", os=" + os + ", visibility=" + visibility + ", vac=" +
        vac + ", mode=" + mode + ", witness=" + witness + ", duration=" +
        duration + ", version='" + version + '\'' + ", extraDataFlag=" +
        extraDataFlag + ", gamePort=" + gamePort + ", serverSteamId=" +
        serverSteamId + ", specPort=" + specPort + ", specName='" + specName
        + '\'' + ", keywords='" + keywords + '\'' + ", gameId=" + gameId + ',' +
        " latency=" + latency() + '}';
  }

}