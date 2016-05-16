package com.goodgamenow.source.serverquery;

import net.jcip.annotations.Immutable;

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
   * Master server location
   */
  public static final String MASTER_SERVER = "hl2master.steampowered.com";

  /**
   * Master server port
   */
  public static final int MASTER_SERVER_PORT = 27011;

  /**
   * First byte of the request packet.
   */
  public static final int MSG_TYPE = 0x31;

  /**
   * Initial ip of the request packet and
   * marker string for when the last query result is received.
   */
  public static final String DEFAULT_IP = "0.0.0.0:0";

  /**
   * Master server strings are null terminated
   */
  public static final int NULL_TERMINATOR = 0;

  /**
   * The expected response header string
   */
  public static final String EXPECTED_HEADER_STRING = "255.255.255.255:26122";

  /**
   * Search the world(other) region by default.
   */
  private static final Region DEFAULT_REGION = Region.WORLD;

  // Request defaults

  /**
   * Default query filter.
   */
  private static final String DEFAULT_FILTER = "";

  /**
   * Index in query buffer where the IP:Port information begins. This is
   * used to change the buffer to the last address received so the query
   * can be resent for more paged results.
   */
  static final int LAST_ADDRESS_INDEX = 2;

  /**
   * Region request field.
   */
  public final Region region;

  /**
   * A '\' delimited collection of filter codes in string form.
   */
  public final String filter;

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
  }

  @Override
  public int hashCode() {
    int result = region.hashCode();
    result = 31 * result + filter.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof MasterQuery))
      return false;

    MasterQuery that = (MasterQuery) o;

    if (region != that.region)
      return false;
    return filter.equals(that.filter);

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
