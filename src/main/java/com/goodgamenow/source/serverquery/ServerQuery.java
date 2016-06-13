/*
 * ServerQuery.java
 *
 * Copyright (c) 2016.  Joe Nellis
 * Distributed under MIT License. See accompanying file License.txt or at
 * http://opensource.org/licenses/MIT
 *
 */

package com.goodgamenow.source.serverquery;

import net.jcip.annotations.Immutable;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * Represents a Source Server Query that indicates need for
 * server info, player info, or server rules.
 * <p>
 * ServerQuery objects are immutable and are created instead of updated
 * via the constructor.
 */
@Immutable
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
   * Constructor to create a fully detailed query.
   * <p>
   * Example of updating a query after no players were found from the
   * serverInfo response but serverRules still need to be requested:
   * <pre>{@code
   * ServerQuery updated = new ServerQuery(query.address,
   *                                       ServerInfoRequest.NOT_NEEDED,
   *                                       PlayerInfoRequest.NOT_NEEDED,
   *                                       query.serverRulesRequest,
   *                                       Retries.MAX_RETRIES,
   *                                       query.challenge);
   * }</pre>
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
    Objects.requireNonNull(address, "Socket address can't be null");
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
    return Objects.hash(address, startTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if ((null == o) || (getClass() != o.getClass())) {
      return false;
    }
    ServerQuery that = (ServerQuery) o;
    return Objects.equals(address, that.address) &&
        Objects.equals(startTime, that.startTime);
  }

  @Override
  public String toString() {
    return "ServerQuery{" +
        "address=" + address +
        ", serverInfoRequest=" + serverInfoRequest +
        ", playerInfoRequest=" + playerInfoRequest +
        ", serverRulesRequest=" + serverRulesRequest +
        ", retries=" + retries +
        ", challenge=" + challenge +
        ", startTime=" + startTime +
        '}';
  }

  /**
   * Returns true is ServerInfoRequest, PlayerInfoRequest and ServerRulesRequest
   * are not needed anymore.
   *
   * @return true if done.
   */
  public boolean isFinished() {
    return !(serverInfoRequest.isNeeded()
        || playerInfoRequest.isNeeded()
        || serverRulesRequest.isNeeded());
  }

  /**
   * Compares this query object's construction timestamp with now and the
   * default
   * timeout limit. Use {@link #isTimedOut(long)} to specify a timeout limit.
   *
   * @return true if this query has not finished by now.
   */
  public boolean isTimedOut() {
    return isTimedOut(DEFAULT_TIMEOUT);
  }

  public boolean isTimedOut(long limitInMillis) {
    long now = System.currentTimeMillis();
    //noinspection UnnecessaryParentheses
    return (now - startTime.inMillis()) > limitInMillis;
  }

  @FunctionalInterface
  public interface BooleanParameter {

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
