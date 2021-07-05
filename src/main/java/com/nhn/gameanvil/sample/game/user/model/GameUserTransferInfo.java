package com.nhn.gameanvil.sample.game.user.model;

import com.nhn.gameanvil.sample.game.multi.usermatch.model.SnakePositionInfo;
import java.io.Serializable;
import java.util.List;

/**
 *  유저 트랜스퍼용 클래스
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

    @Override
    public String toString() {
        return "GameUserTransferInfo{" +
            "gameUserInfo=" + gameUserInfo +
            ", getSnakePositionInfoList=" + getSnakePositionInfoList +
            '}';
    }
}
