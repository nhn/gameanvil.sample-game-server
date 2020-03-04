package com.nhn.tardis.taptap.space.user.model;

import com.nhn.tardis.taptap.space.game.multi.usermatch.model.SnakePositionInfo;
import java.io.Serializable;
import java.util.List;

/**
 *  유저 트랜스퍼용 객체
 */
public class GameUserTransferInfo implements Serializable {
    private GameUserInfo gameUserInfo;
    private List<SnakePositionInfo> getSnakePositionInfoList;

    public GameUserInfo getGameUserInfo() {
        return gameUserInfo;
    }

    public void setGameUserInfo(GameUserInfo gameUserInfo) {
        this.gameUserInfo = gameUserInfo;
    }

    public List<SnakePositionInfo> getGetSnakePositionInfoList() {
        return getSnakePositionInfoList;
    }

    public void setGetSnakePositionInfoList(List<SnakePositionInfo> getSnakePositionInfoList) {
        this.getSnakePositionInfoList = getSnakePositionInfoList;
    }
}
