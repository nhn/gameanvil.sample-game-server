package com.nhn.gameanvil.sample.game.multi.usermatch.model;

import com.nhn.gameanvil.node.match.BaseUserMatchInfo;
import java.io.Serializable;

/**
 * 유저 매치 하는 방정보
 */
public class SnakeUserMatchInfo extends BaseUserMatchInfo implements Serializable {

    private int id;
    private int rating;
    private int maxUserCount = 2;

    public SnakeUserMatchInfo() {
    }

    public SnakeUserMatchInfo(int id, int rating) {
        this.id = id;
        this.rating = rating;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getPartySize() {
        return 0;
    }

    public int getRating() {
        return rating;
    }

    public int getMaxUserCount() {
        return maxUserCount;
    }

    @Override
    public String toString() {
        return "SnakeUserMatchInfo{" +
            "id=" + id +
            ", rating=" + rating +
            ", maxUserCount=" + maxUserCount +
            '}';
    }
}
