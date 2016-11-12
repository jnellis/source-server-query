package com.goodgamenow.source.serverquery.response;

/**
 * User: Joe Nellis
 * Date: 6/28/2015
 * Time: 12:49 PM
 */
public class PlayerInfo {
  final int index;
  final String name;
  final int score;
  final float duration;

  public int getIndex() {
    return index;
  }

  public String getName() {
    return name;
  }

  public int getScore() {
    return score;
  }

  public float getDuration() {
    return duration;
  }

  public PlayerInfo(int index, String name, int score, float duration){
    this.index = index;
    this.name = name;
    this.score = score;
    this.duration = duration;
  }

  @Override
  public final String toString() {
    return "PlayerInfo{" + "index=" + index + ", name='" + name + '\'' + ", " +
        "score=" + score + ", duration=" + duration + '}';
  }
}
