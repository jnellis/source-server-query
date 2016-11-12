package com.goodgamenow.source.serverquery;


import net.jcip.annotations.Immutable;

import java.net.InetSocketAddress;

/**
 * A Source Master Server response object. Contains
 */
@Immutable
class MasterResponse {

  public final String queryFilter;

  public final String serverAddress;

  public final long queryTime;

  public MasterResponse(String serverAddress,
                        String queryFilter,
                        long queryTime) {
    this.serverAddress = serverAddress;
    this.queryFilter = queryFilter;
    this.queryTime = queryTime;
  }

  public final InetSocketAddress getInetSocketAddress() {
    String[] addy = serverAddress.split(":");
    int port = Integer.parseInt(addy[1]);
    return InetSocketAddress.createUnresolved(addy[0], port);
  }

}
