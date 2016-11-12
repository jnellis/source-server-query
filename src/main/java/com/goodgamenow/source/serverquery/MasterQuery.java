/*
 * MasterQuery.java
 *
 * Copyright (c) 2016.  Joe Nellis
 * Distributed under MIT License. See accompanying file License.txt or at
 * http://opensource.org/licenses/MIT
 *
 */

package com.goodgamenow.source.serverquery;

import net.jcip.annotations.Immutable;

import java.util.Objects;
import java.util.Optional;


/**
 * A data structure to hold query specifics of the Valve Master Server
 * Protocol.
 * <p>
 * MasterQuery is thread safe.
 */
@Immutable
public class MasterQuery {

  /**
   * Search the world(other) region by default.
   */
  private static final Region DEFAULT_REGION = Region.WORLD;

  /**
   * Default query filter.
   */
  private static final String DEFAULT_FILTER = "";




  /**
   * Region request field.
   */
  public final Region region;

  /**
   * A '\' delimited collection of filter codes in string form.
   */
  public final String filter;

  /**
   * template to use when creating server query objects from results.
   */
  public final ServerQuery template;


  /**
   * Same as MasterQuery but with
   *
   * @param region           One of the server regions in the world
   * @param filter           A filter to narrow down the search.
   * @param template         A template to model new server queries from
   *                         the results.
   */
  public MasterQuery(Region region,
                     String filter,
                     ServerQuery template) {
    this.region = Optional.ofNullable(region)
                          .orElse(DEFAULT_REGION);

    this.filter = Optional.ofNullable(filter)
                          .orElse(DEFAULT_FILTER);

    this.template = template;
  }

  /**
   * Query objects are immutable as to the region and type of query(filter.)
   *
   * @param region One of the server regions in the world
   * @param filter A filter to narrow down the search.
   */
  public MasterQuery(Region region, String filter) {
    this.region = Optional.ofNullable(region)
                          .orElse(DEFAULT_REGION);

    this.filter = Optional.ofNullable(filter)
                          .orElse(DEFAULT_FILTER);

    this.template = ServerQuery.DEFAULT_SERVER_QUERY_TEMPLATE;

  }

  @Override
  public int hashCode() {
    return Objects.hash(region, filter);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MasterQuery)) {
      return false;
    }
    MasterQuery query = (MasterQuery) o;
    return (region == query.region) && Objects.equals(filter, query.filter);
  }

  /**
   * Region Codes
   */
  public enum Region {
    USEAST(0),
    USWEST(1),
    SAMERICA(2),
    EUROPE(3),
    ASIA(4),
    AUSTRALIA(5),
    MIDEAST(6),
    AFRICA(7),
    WORLD(0xFF);

    public final int code;

    Region(int regionCode) {
      this.code = regionCode;
    }

  }

}
