package com.goodgamenow.source.serverquery

import spock.lang.Specification

/**
 * User: Joe Nellis
 * Date: 6/11/2016 
 * Time: 2:25 PM
 *
 */
class ClientTest extends Specification {
  def "test main"() {
    expect:
    def filter = QueryFilter.allServers.notEmpty().secure().dedicated().appid(440).get()
    String[] args = ["USWEST", filter]
    Client.main(args)
  }
}