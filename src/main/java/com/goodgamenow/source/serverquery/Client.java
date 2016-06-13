package com.goodgamenow.source.serverquery;

import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;


/**
 * User: Joe Nellis
 * Date: 4/24/2016
 * Time: 1:21 AM
 */
public class Client {

  private static final Logger logger = LogManager.getLogger();

  public static void main(String[] args) {

    System.out.println("Source Server Query");
    if (args.length < 2) {
      usage();
      return;
    }
    MasterQuery.Region region = MasterQuery.Region.valueOf(args[0]);
    assert region.equals(MasterQuery.Region.USWEST);
    String queryFilter = args[1];

    MasterQuery query = new MasterQuery(region, queryFilter);
    NioEventLoopGroup group = new NioEventLoopGroup();
    MasterClientBootstrap bootstrap = new MasterClientBootstrap(group, query);
    try {
      bootstrap.bind(0).channel().closeFuture().await(3000);
    } catch (InterruptedException e) {
      System.out.println("Master server response timed out.");
    } finally {
      group.shutdownGracefully();
    }
    List<String> results = bootstrap.getResults();
    results.forEach(System.out::println);
    System.out.println(results.size() + " results in " +
                           bootstrap.getRuntime() + "ms.");

  }

  public static void usage() {
    System.out.println("usage:\n" +
                           "Client region queryFilter\n" +
                           "example:\n" +
                           "Client USWEST \\appId\\440");
    return;
  }

  public static Optional<InetSocketAddress>
  createSocketAddressFromString(String addressString) {
    String[] hostPort = addressString.split(":");
    if (hostPort.length != 2) {
      logger.warn("Address string, \"" + addressString + "\" format is " +
                      "invalid. Expected domainname:port, or xxx.xxx.xxx" +
                      ".xxx:port");
    }
    InetSocketAddress socketAddress = null;
    try {
      socketAddress = new InetSocketAddress(hostPort[0],
                                            Integer.decode(hostPort[1]));
    } catch (NumberFormatException nfe) {
      logger.warn("Unable to parse port number of " + addressString);
    } catch (IllegalArgumentException iae) {
      logger.warn("Port number not between 0-65535 of " + addressString);
    } catch (SecurityException se) {
      logger.warn("Unable to resolve host address of " + addressString);
    }

    return Optional.ofNullable(socketAddress);
  }
}
