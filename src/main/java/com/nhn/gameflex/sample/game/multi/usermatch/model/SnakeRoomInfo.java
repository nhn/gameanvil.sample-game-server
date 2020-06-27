package com.nhn.gameflex.sample.game.multi.usermatch.model;

import com.nhn.gameflex.node.match.UserMatchInfo;
import java.io.Serializable;

/**
 * 유저 매치 하는 방정보
 */
public class SnakeRoomInfo extends UserMatchInfo implements Serializable {

    private int id;
    private int rating;
    private int maxUserCount = 2;

    public SnakeRoomInfo() {
    }

    public SnakeRoomInfo(int id, int rating) {
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
}
