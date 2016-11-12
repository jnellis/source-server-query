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
public class ServerInfo extends ServerResponse {

  private byte protocol;

  private String name;

  private String map;

  private String folder;

  private String game;

  private short appId;

  private byte players;

  private byte maxPlayers;

  private byte bots;

  private byte serverType;

  private byte os;

  private byte visibility;

  private byte vac;

  // the Ship specific fields
  private byte mode;

  private byte witness;

  private byte duration;

  private String version;

  private byte extraDataFlag;

  // potential fields based on extraDataFlag
  private short gamePort;

  private long serverSteamId;

  private short specPort;

  private String specName;

  private String keywords;

  private long gameId;

  public ServerInfo(InetSocketAddress from) {
    super(from);
  }

  public String getFolder() {
    return folder;
  }

  public String getGame() {
    return game;
  }

  public short getAppId() {
    return appId;
  }

  public byte getPlayers() {
    return players;
  }

  public byte getMaxPlayers() {
    return maxPlayers;
  }

  public byte getBots() {
    return bots;
  }

  public byte getServerType() {
    return serverType;
  }

  public byte getOs() {
    return os;
  }

  public byte getVisibility() {
    return visibility;
  }

  public byte getVac() {
    return vac;
  }

  public byte getMode() {
    return mode;
  }

  public byte getWitness() {
    return witness;
  }

  public byte getDuration() {
    return duration;
  }

  public String getVersion() {
    return version;
  }

  public byte getExtraDataFlag() {
    return extraDataFlag;
  }

  public short getGamePort() {
    return gamePort;
  }

  public long getServerSteamId() {
    return serverSteamId;
  }

  public short getSpecPort() {
    return specPort;
  }

  public String getSpecName() {
    return specName;
  }

  public String getKeywords() {
    return keywords;
  }

  public long getGameId() {
    return gameId;
  }

  public byte getProtocol() {
    return protocol;
  }

  public ServerInfo protocol(byte protocol) {
    this.protocol = protocol;
    return this;
  }

  public String getName() {
    return name;
  }

  public ServerInfo name(String name) {
    this.name = name;
    return this;
  }

  public String getMap() {
    return map;
  }

  public ServerInfo map(String map) {
    this.map = map;
    return this;
  }


  public ServerInfo folder(String folder) {
    this.folder = folder;
    return this;
  }


  public ServerInfo game(String game) {
    this.game = game;
    return this;
  }

  public ServerInfo appId(short appId) {
    this.appId = appId;
    return this;
  }

  public ServerInfo players(byte players) {
    this.players = players;
    return this;
  }


  public ServerInfo maxPlayers(byte maxPlayers) {
    this.maxPlayers = maxPlayers;
    return this;
  }


  public ServerInfo bots(byte bots) {
    this.bots = bots;
    return this;
  }

  public ServerInfo serverType(byte serverType) {
    this.serverType = serverType;
    return this;
  }


  public ServerInfo os(byte os) {
    this.os = os;
    return this;
  }


  public ServerInfo visibility(byte visibility) {
    this.visibility = visibility;
    return this;
  }


  public ServerInfo vac(byte vac) {
    this.vac = vac;
    return this;
  }


  public ServerInfo mode(byte mode) {
    this.mode = mode;
    return this;
  }


  public ServerInfo witness(byte witness) {
    this.witness = witness;
    return this;
  }


  public ServerInfo duration(byte duration) {
    this.duration = duration;
    return this;
  }


  public ServerInfo version(String version) {
    this.version = version;
    return this;
  }


  public ServerInfo extraDataFlag(byte extraDataFlag) {
    this.extraDataFlag = extraDataFlag;
    return this;
  }


  public ServerInfo gamePort(short gamePort) {
    this.gamePort = gamePort;
    return this;
  }


  public ServerInfo serverSteamId(long serverSteamId) {
    this.serverSteamId = serverSteamId;
    return this;
  }


  public ServerInfo specPort(short specPort) {
    this.specPort = specPort;
    return this;
  }


  public ServerInfo specName(String specName) {
    this.specName = specName;
    return this;
  }


  public ServerInfo keywords(String keywords) {
    this.keywords = keywords;
    return this;
  }

  public ServerInfo gameId(long gameId) {
    this.gameId = gameId;
    return this;
  }


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
        (orig, nu) -> orig.serverInfo(nu.getServerInfo().get()));
  }


  private byte players() {
    return players;
  }

  @Override
  public String toString() {
    return "{\"_class\":\"ServerInfo\", " +
        "\"protocol\":\"" + protocol + "\"" + ", " +
        "\"name\":" + (name == null ? "null" : "\"" + name + "\"") + ", " +
        "\"map\":" + (map == null ? "null" : "\"" + map + "\"") + ", " +
        "\"folder\":" + (folder == null ? "null"
                                        : "\"" + folder + "\"") + ", " +
        "\"game\":" + (game == null ? "null" : "\"" + game + "\"") + ", " +
        "\"appId\":\"" + appId + "\"" + ", " +
        "\"players\":\"" + players + "\"" + ", " +
        "\"maxPlayers\":\"" + maxPlayers + "\"" + ", " +
        "\"bots\":\"" + bots + "\"" + ", " +
        "\"serverType\":\"" + serverType + "\"" + ", " +
        "\"os\":\"" + os + "\"" + ", " +
        "\"visibility\":\"" + visibility + "\"" + ", " +
        "\"vac\":\"" + vac + "\"" + ", " +
        "\"mode\":\"" + mode + "\"" + ", " +
        "\"witness\":\"" + witness + "\"" + ", " +
        "\"duration\":\"" + duration + "\"" + ", " +
        "\"version\":" + (version == null ? "null"
                                          : "\"" + version + "\"") + ", " +
        "\"extraDataFlag\":\"" + extraDataFlag + "\"" + ", " +
        "\"gamePort\":\"" + gamePort + "\"" + ", " +
        "\"serverSteamId\":\"" + serverSteamId + "\"" + ", " +
        "\"specPort\":\"" + specPort + "\"" + ", " +
        "\"specName\":" + (specName == null ? "null"
                                            : "\"" + specName + "\"") + ", " +
        "\"keywords\":" + (keywords == null ? "null"
                                            : "\"" + keywords + "\"") + ", " +
        "\"gameId\":\"" + gameId + "\"" +
        "}";
  }

}