package com.goodgamenow.source.serverquery

import io.netty.channel.ChannelFuture
import io.netty.channel.nio.NioEventLoopGroup
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import spock.lang.Specification


/**
 * User: Joe Nellis
 * Date: 5/15/2016 
 * Time: 10:46 PM
 *
 */
class MasterAndServerQueryTest extends Specification {
  NioEventLoopGroup group

  Logger logger

  void setup() {
    group = new NioEventLoopGroup()
    logger = LogManager.logger
  }

  void cleanup() {
    group?.shutdownGracefully()
  }

  def "Perform MasterQuery, then ServerQuery on the results."() {
    MasterQuery.Region region = MasterQuery.Region.USWEST
    QueryFilter filter = QueryFilter.allServers
                                    .notEmpty()
                                    .secure()
                                    .dedicated()
                                    .appid(440)

    MasterQuery query = new MasterQuery(region, filter.get())
    MasterClientBootstrap bootstrap = new MasterClientBootstrap(group, query)
    ChannelFuture future = bootstrap.bind(0).sync()

    future.channel().closeFuture().await(10000)

    def responseList = bootstrap.results

    def serverQueryBootstrap = new ServerQueryBootstrap(group)
    def channel = serverQueryBootstrap.bind(0).sync().channel()

    def queries = ServerQueryBootstrap.createQueryList(responseList)

    queries.forEach { channel.writeAndFlush(it).sync() }

    channel.closeFuture().await(2500);

    expect:

    !serverQueryBootstrap.getQueryResults().isEmpty()

    serverQueryBootstrap.getQueryResults().forEach { logger.debug(it) }

  }
}
