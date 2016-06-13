package com.goodgamenow.source.serverquery;

/**
 * Transforms a ServerQuery. Used when piecing together server responses.
 * User: Joe Nellis
 * Date: 11/2/2015
 * Time: 6:46 PM
 */
@FunctionalInterface
public interface QueryUpdater {
  ServerQuery update(ServerQuery query);
}
