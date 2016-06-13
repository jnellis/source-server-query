package com.goodgamenow.source.serverquery;

import java.net.InetSocketAddress;

/**
 * Represents a Source Server Query. The query can be one of three types:
 * server info, player info, or server rules.
 * <p>
 * User: Joe Nellis
 * Date: 5/21/2015
 * Time: 11:55 PM
 */
public class ServerQuery {

  private static final long DEFAULT_TIMEOUT = 500L;

  public final InetSocketAddress address;

  public final ServerInfoRequest serverInfoRequest;

  public final PlayerInfoRequest playerInfoRequest;

  public final ServerRulesRequest serverRulesRequest;

  public final Retries retries;

  public final Challenge challenge;

  public final StartTime startTime;


  /**
   * Constructor that defaults query options to retrieve server info and if
   * it has players, retrieve player info. The number of retries is set to
   * three, the maximum.
   *
   * @param address socket address of
   */
  public ServerQuery(InetSocketAddress address) {
    this(address,
         ServerInfoRequest.NEEDED,
         PlayerInfoRequest.NEEDED,
         ServerRulesRequest.NOT_NEEDED,
         Retries.MAX_RETRIES,
         Challenge.RESET);
  }

  /**
   * Creates a fully detailed query.
   *
   * @param address           socket address
   * @param serverInfoNeeded  if a server info request is attempted
   * @param playerInfoNeeded  if a player info request is attempted
   * @param serverRulesNeeded if server environment variables are requested
   * @param retries           number of retries left.
   * @param challenge         the challenge number assigned from the server
   */
  public ServerQuery(InetSocketAddress address,
                     ServerInfoRequest serverInfoNeeded,
                     PlayerInfoRequest playerInfoNeeded,
                     ServerRulesRequest serverRulesNeeded,
                     Retries retries,
                     Challenge challenge) {
    this.address = address;
    this.serverInfoRequest = serverInfoNeeded;
    this.playerInfoRequest = playerInfoNeeded;
    this.serverRulesRequest = serverRulesNeeded;
    this.retries = retries;
    this.challenge = challenge;
    this.startTime = StartTime.now();  // all query timers start at creation
  }

  @Override
  public int hashCode() {
    return address.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ServerQuery)) {
      return false;
    }

    ServerQuery that = (ServerQuery) o;

    return address.equals(that.address);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ServerQuery{");
    sb.append("address=").append(address);
    sb.append(", serverInfoRequest=").append(serverInfoRequest.isNeeded());
    sb.append(", playerInfoRequest=").append(playerInfoRequest.isNeeded());
    sb.append(", serverRulesRequest=").append(serverRulesRequest.isNeeded());
    sb.append(", retries=").append(retries.remaining());
    sb.append(", challenge=").append(challenge.number());
    sb.append(", startTime=").append(startTime.inMillis());
    sb.append('}');
    return sb.toString();
  }

  public boolean isFinished() {
    return !(serverInfoRequest.isNeeded()
        || playerInfoRequest.isNeeded()
        || serverRulesRequest.isNeeded());
  }

  public boolean isTimedOut() {
    return isTimedOut(DEFAULT_TIMEOUT);
  }

  public boolean isTimedOut(long limitInMillis) {
    long now = System.currentTimeMillis();
    //noinspection UnnecessaryParentheses
    return (now - startTime.inMillis()) > limitInMillis;
  }

  @FunctionalInterface
  public interface BooleanParameter<T> {

    boolean isNeeded();

  }

  @FunctionalInterface
  public interface ServerInfoRequest extends BooleanParameter {

    ServerInfoRequest NEEDED = () -> true;

    ServerInfoRequest NOT_NEEDED = () -> false;
  }

  @FunctionalInterface
  public interface PlayerInfoRequest extends BooleanParameter {

    PlayerInfoRequest NEEDED = () -> true;

    PlayerInfoRequest NOT_NEEDED = () -> false;
  }


  @FunctionalInterface
  public interface ServerRulesRequest extends BooleanParameter {

    ServerRulesRequest NEEDED = () -> true;

    ServerRulesRequest NOT_NEEDED = () -> false;
  }

  @FunctionalInterface
  public interface Retries {

    Retries MAX_RETRIES = () -> 3;

    default Retries decrement() {
      return () -> remaining() - 1;
    }

    int remaining();
  }

  @FunctionalInterface
  public interface Challenge {

    Challenge RESET = () -> 0;

    int number();
  }

  @FunctionalInterface
  public interface StartTime {

    static StartTime now() {
      long now = System.currentTimeMillis();
      return () -> now;
    }

    long inMillis();
  }

}
