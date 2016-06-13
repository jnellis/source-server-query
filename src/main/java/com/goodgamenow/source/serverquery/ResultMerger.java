package com.goodgamenow.source.serverquery;

import com.goodgamenow.source.serverquery.response.QueryResult;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * User: Joe Nellis
 * Date: 11/3/2015
 * Time: 6:13 PM
 */
@FunctionalInterface
public interface ResultMerger {
  QueryResult mergeInto(Map<InetSocketAddress, QueryResult> resultMap);
}
