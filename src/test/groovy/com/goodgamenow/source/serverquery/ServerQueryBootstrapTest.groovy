package com.goodgamenow.source.serverquery

import io.netty.channel.nio.NioEventLoopGroup
import io.netty.util.ResourceLeakDetector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import spock.lang.Specification

import static com.goodgamenow.source.serverquery.ServerQueryBootstrap.createSocketAddressFromString

/**
 * User: Joe Nellis
 * Date: 3/25/2016 
 * Time: 3:47 PM
 *
 */
class ServerQueryBootstrapTest extends Specification {

  NioEventLoopGroup group
  Logger logger

  void setup() {
    group = new NioEventLoopGroup()
    logger = LogManager.logger
    ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID)
  }

  def "Create socket from invalid address"() {
    expect:
    createSocketAddressFromString("asdfdf").equals(Optional.empty())

  }

  def "Create socket from null address"(){
    when:
    createSocketAddressFromString(null)
    then:
    thrown NullPointerException
  }

  def "Create socket with bad port number"(){
    expect:
    createSocketAddressFromString("asdfdf:99999").equals(Optional.empty())
    createSocketAddressFromString("asdfdf:ZZ").equals(Optional.empty())
  }

  def "Perform server info query"() {
    def ipaddr = new InetSocketAddress("68.232.164.27", 27015)
    def bootstrap = new ServerQueryBootstrap(group)
    def future = bootstrap.bind(0).sync()
    def serverQuery =  new ServerQuery(ipaddr,
            "noid",
            ServerQuery.ServerInfoRequest.NEEDED,
            ServerQuery.PlayerInfoRequest.NEEDED,
            ServerQuery.ServerRulesRequest.NEEDED,
            ServerQuery.Retries.MAX_RETRIES,
            ServerQuery.Challenge.RESET)

    future.channel().writeAndFlush(serverQuery).sync()

    future.channel().closeFuture().await(2500)
    bootstrap.getQueryResults().forEach { logger.debug it }


    expect:
    !bootstrap.getQueryResults().isEmpty()
  }


  void cleanup() {
    group?.shutdownGracefully()
  }
}
