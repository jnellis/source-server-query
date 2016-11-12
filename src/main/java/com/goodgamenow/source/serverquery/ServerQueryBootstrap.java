package com.goodgamenow.source.serverquery;

import com.goodgamenow.source.serverquery.response.QueryResult;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * User: Joe Nellis
 * Date: 3/16/2016
 * Time: 1:12 PM
 */
public class ServerQueryBootstrap extends Bootstrap {

  private static final Logger logger = LogManager.getLogger();

  private final ServerQueryChannelInitializer initializer;

  public ServerQueryBootstrap(EventLoopGroup eventLoopGroup) {
    this(eventLoopGroup, null);
  }

  public ServerQueryBootstrap(EventLoopGroup eventLoopGroup,
                              ChannelHandlerContext parentContext) {

    this.initializer = new ServerQueryChannelInitializer(parentContext);
    this.group(eventLoopGroup)
        .channel(NioDatagramChannel.class)
        .option(ChannelOption.SO_BROADCAST, true)
        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .option(ChannelOption.SO_SNDBUF, 1024 * 1024 * 8)
        .option(ChannelOption.SO_RCVBUF, 1024 * 1024 * 16)
        .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
        .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024)
        .handler(initializer)
    ;

  }

  /**
   * Creates a list of ServerQuery objects from a list of ip/port addresses.
   * Configures ServerQuery objects with default ServerQuery constructor.
   *
   * @param addressStrings list of ip:port's of game servers
   * @return list of ServerQuery objects, one for each server address.
   */
  public static List<ServerQuery> createQueryList(List<String> addressStrings) {

    return addressStrings.parallelStream()
                         .map(ServerQueryBootstrap::createSocketAddressFromString)
                         .filter(Optional::isPresent)
                         .map(Optional::get)
                         .map(ServerQuery::new)
                         .collect(Collectors.toList());
  }

  /**
   * Tries to create an InetSocketAddress from a given address string in either
   * format:
   * <PRE>
   * domainname:port
   * XXX.XXX.XXX.XXX:port
   * </PRE>
   * <p>
   * If there is an error with the address string, it gets ignored and a empty
   * Optional is returned.
   * Turn on debug logging to see source of formatting error of addressString.
   *
   * @param addressString
   * @return Returns an Optional result in case there were errors.
   */
  static Optional<InetSocketAddress>
  createSocketAddressFromString(String addressString) {

    InetSocketAddress socketAddress = null;
    String[] hostPort = addressString.split(":");
    if (hostPort.length != 2) {
      logger.debug("Address string, \"{}\", format is invalid. Expected " +
                       "<domainname>:<port>, or <XXX.XXX.XXX.XXX>:<port>",
                   addressString);
    } else {
      try {
        socketAddress = new InetSocketAddress(hostPort[0],
                                              Integer.decode(hostPort[1]));
      } catch (NumberFormatException nfe) {
        logger.debug("Unable to parse port number of " + addressString);
      } catch (IllegalArgumentException iae) {
        logger.debug("Port number not between 0-65535 of " + addressString);
      } catch (SecurityException se) {
        logger.debug("Unable to resolve host address of " + addressString);
      }
    }
    return Optional.ofNullable(socketAddress);
  }

  public boolean reconciliationMapsAreEmpty() {
    return initializer.reconciliationMapsAreEmpty();
  }

  public Collection<QueryResult> getQueryResults() {
    return initializer.getQueryResults();
  }

}
