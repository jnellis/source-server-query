package com.goodgamenow.source.serverquery;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * User: Joe Nellis
 * Date: 1/21/2016
 * Time: 12:46 PM
 */
@FunctionalInterface
public interface QueryFilter extends Supplier<String> {

  // All servers
  QueryFilter allServers = () -> "";

  // Servers running the specified map (ex. cs_italy)
  default QueryFilter map(String mapName) {
    return () -> get() +   "\\map\\" + mapName ;
  }

  // Servers running on a Linux platform
  default QueryFilter linux() {
    return () -> get() + "\\linux\\1";
  }

  // Servers that are spectator proxies
  default QueryFilter proxy() {
    return () -> get() + "\\proxy\\1";
  }

  // Servers that are running game [appid]
  default QueryFilter appid(int appId) {
    return () -> get() + "\\appid\\" + appId;
  }

  // Servers that are NOT running game [appid] (This was introduced to block
  // Left 4 Dead games from the Steam Server Browser)
  default QueryFilter napp(int appId) {
    return () -> get() + "\\napp\\" + appId;
  }

  // Servers that are empty
  default QueryFilter noplayers() {
    return () -> get() + "\\noplayers\\1";
  }

  // Servers that are whitelisted
  default QueryFilter whitelisted() {
    return () -> get() + "\\white\\1";
  }

  // Servers with all of the given tag(s) in sv_tags
  default QueryFilter gametype(Collection<String> tags) {
    return () -> get() + "\\gametype\\" + String.join(",", tags);
  }

  // Servers with all of the given tag(s) in their 'hidden' tags (L4D2)
  default QueryFilter gamedata(Collection<String> tags) {
    return () -> get() + "\\gamedata\\" + String.join(",", tags);
  }

  // Servers with any of the given tag(s) in their 'hidden' tags (L4D2)
  default QueryFilter anygamedata(Collection<String> tags) {
    return () -> get() + "\\gamedataor\\" + String.join(",", tags);
  }

  // Servers with their hostname matching [hostname] (can use * as a wildcard)
  default QueryFilter name_match(String hostName) {
    return () -> get() + "\\name_match\\" + hostName;
  }

  // Servers running version [version] (can use * as a wildcard)
  default QueryFilter version_match(String version) {
    return () -> get() + "\\version_match\\" + version;
  }

  // Return only one server for each unique IP address matched
  default QueryFilter collapse_addr_hash() {
    return () -> get() + "\\collapse_addr_hash\\1";
  }

  // Return only servers on the specified IP address
  // (port supported and optional)
  default QueryFilter gameaddr(String ipAddr) {
    return () -> get() + "\\gameaddr\\" + ipAddr;
  }

  // Servers using anti-cheat technology (VAC, but potentially others as well)
  default QueryFilter secure() {
    return () -> get() + "\\secure\\1";
  }

  // Servers running the specified modification (ex. cstrike)
  default QueryFilter gamedir(String mod) {
    return () -> get() + "\\gamedir\\" + mod;
  }

  // Servers running dedicated
  default QueryFilter dedicated() {
    return () -> get() + "\\type\\d";
  }

  // A special filter, specifies that servers matching any of the following [x]
  // conditions should not be returned
  default QueryFilter nor() {
    return () -> get() + "\\nor\\";
  }

  // A special filter, specifies that servers matching all of the following [x]
  // conditions should not be returned
  default QueryFilter nand() {
    return () -> get() + "\\nand\\";
  }

  // Servers that are NOT empty
  default QueryFilter notEmpty() {
    return () -> get() + "\\empty\\1";
  }

  // Servers that are NOT full
  default QueryFilter notFull() {
    return () -> get() + "\\full\\1";
  }


}
