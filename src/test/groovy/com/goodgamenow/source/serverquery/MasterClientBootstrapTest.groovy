/*
 * MasterClientBootstrapTest.groovy
 *
 * Copyright (c) 2016.  Joe Nellis
 * Distributed under MIT License. See accompanying file License.txt or at
 * http://opensource.org/licenses/MIT
 *
 */

package com.goodgamenow.source.serverquery

import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import spock.lang.Specification

/**
 * User: Joe Nellis
 * Date: 4/24/2016 
 * Time: 7:14 PM
 */
class MasterClientBootstrapTest extends Specification {
  EventLoopGroup eventLoopGroup

  void setup() {
    eventLoopGroup = new NioEventLoopGroup()
  }

  void cleanup() {
    eventLoopGroup?.shutdownGracefully()
  }

  def "Region match"() {
    expect:
    MasterQuery.Region.valueOf("USWEST").equals(MasterQuery.Region.USWEST);
    MasterQuery.Region.valueOf("USEAST").equals(MasterQuery.Region.USEAST);
    MasterQuery.Region.valueOf("SAMERICA").equals(MasterQuery.Region.SAMERICA);
    MasterQuery.Region.valueOf("EUROPE").equals(MasterQuery.Region.EUROPE);
    MasterQuery.Region.valueOf("ASIA").equals(MasterQuery.Region.ASIA);
    MasterQuery.Region.valueOf("AUSTRALIA").equals(MasterQuery.Region.AUSTRALIA);
    MasterQuery.Region.valueOf("MIDEAST").equals(MasterQuery.Region.MIDEAST);
    MasterQuery.Region.valueOf("AFRICA").equals(MasterQuery.Region.AFRICA);
  }

  def "MasterQuery: Tf2, not empty, secure, dedicated"() {
    MasterQuery.Region region = MasterQuery.Region.USWEST
    QueryFilter filter = QueryFilter.allServers
                                    .notEmpty()
                                    .secure()
                                    .dedicated()
                                    .appid(440)

    MasterQuery query = new MasterQuery(region, filter.get())
    MasterClientBootstrap bootstrap = new MasterClientBootstrap(eventLoopGroup, query)
    ChannelFuture future = bootstrap.bind(0).sync()

    future.channel().closeFuture().await(10000)
    expect:
    !bootstrap.results.isEmpty()
    //bootstrap.results.each{println(it)}
    println "${bootstrap.results.size()} results in  ${bootstrap.getRuntime()}ms"
  }

  def "two queries at same time"() {
    MasterQuery.Region region = MasterQuery.Region.USWEST
    QueryFilter filter = QueryFilter.allServers
                                    .secure()
                                    .appid(440)

    QueryFilter filter2 = QueryFilter.allServers
                                     .secure()
                                     .dedicated()
                                     .linux()
                                     .notEmpty()
                                     .appid(730)

    MasterQuery query = new MasterQuery(region, filter.get())
    MasterQuery query2 = new MasterQuery(region, filter2.get())
    MasterClientBootstrap bootstrap = new MasterClientBootstrap(eventLoopGroup, query)
    MasterClientBootstrap bootstrap2 = new MasterClientBootstrap(eventLoopGroup, query2)
    ChannelFuture future = bootstrap.bind(0)
    ChannelFuture future2 = bootstrap2.bind(0)

    future.channel().closeFuture().await(10000)
    future2.channel().closeFuture().await(10000)

    expect:
    !bootstrap.getResults().isEmpty()
    !bootstrap2.getResults().isEmpty()
    println "${bootstrap.results.size()} results in ${bootstrap.getRuntime()}ms"
    println "${bootstrap2.results.size()} results in ${bootstrap2.getRuntime()}ms"
  }


}
