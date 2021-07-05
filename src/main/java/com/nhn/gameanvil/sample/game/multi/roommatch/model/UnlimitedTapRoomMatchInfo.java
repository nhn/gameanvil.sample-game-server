package com.nhn.gameanvil.sample.game.multi.roommatch.model;

import com.nhn.gameanvil.node.match.BaseRoomMatchInfo;
import java.io.Serializable;

/**
 * 무제한 탭 게임 룸 정보
 */
public class UnlimitedTapRoomMatchInfo extends BaseRoomMatchInfo implements Serializable {
    private static final int MAX_USER = 4;
    private long createTime = 0L;

    public UnlimitedTapRoomMatchInfo(int roomId) {
        super(roomId, MAX_USER);
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getCreateTime() {
        return createTime;
    }
}
