package com.nhn.tardis.sample.space.game.multi.roommatch.model;

import com.nhnent.tardis.node.match.RoomMatchInfo;
import java.io.Serializable;
import org.joda.time.DateTime;

/**
 * 무제한 탭 게임 룸 정보
 */
public class UnlimitedTapRoomInfo implements Serializable, RoomMatchInfo {
    private String roomId = "";
    private int userCurrentCount = 0;
    private int userMaxCount = 4;
    private DateTime createTime;

    public UnlimitedTapRoomInfo() {
        createTime = DateTime.now();
    }

    @Override
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getUserCurrentCount() {
        return userCurrentCount;
    }

    public void setUserCurrentCount(int userCurrentCount) {
        this.userCurrentCount = userCurrentCount;
    }

    public int getUserMaxCount() {
        return userMaxCount;
    }

    @Override
    public String toString() {
        return "UnlimitedTapRoomInfo{" +
            "roomId='" + roomId + '\'' +
            ", userCurrentCount=" + userCurrentCount +
            ", userMaxCount=" + userMaxCount +
            '}';
    }
}
