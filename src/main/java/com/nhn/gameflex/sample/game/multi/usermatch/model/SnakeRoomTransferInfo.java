package com.nhn.gameflex.sample.game.multi.usermatch.model;

import com.nhn.gameflex.sample.game.user.GameUser;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 룸 트랜스퍼용 객체
 */
public class SnakeRoomTransferInfo implements Serializable {
    // 서버에서 생성되는 food 리스트
    private List<SnakePositionInfo> foodList;
    // 방안에 있는 유저
    private Map<Integer, GameUser> gameUserMap;

    private Map<String, Integer> gameUserScoreMap;

    // 방크기 설정
    private int boarderLeft;
    private int boarderRight;
    private int boarderBottom;
    private int boarderTop;

    // food 시작 인덱스 설정
    private int foodIndex = 0;

    public List<SnakePositionInfo> getFoodList() {
        return foodList;
    }

    public void setFoodList(List<SnakePositionInfo> foodList) {
        this.foodList = foodList;
    }

    public Map<Integer, GameUser> getGameUserMap() {
        return gameUserMap;
    }

    public void setGameUserMap(Map<Integer, GameUser> gameUserMap) {
        this.gameUserMap = gameUserMap;
    }

    public Map<String, Integer> getGameUserScoreMap() {
        return gameUserScoreMap;
    }

    public void setGameUserScoreMap(Map<String, Integer> gameUserScoreMap) {
        this.gameUserScoreMap = gameUserScoreMap;
    }

    public int getBoarderLeft() {
        return boarderLeft;
    }

    public void setBoarderLeft(int boarderLeft) {
        this.boarderLeft = boarderLeft;
    }

    public int getBoarderRight() {
        return boarderRight;
    }

    public void setBoarderRight(int boarderRight) {
        this.boarderRight = boarderRight;
    }

    public int getBoarderBottom() {
        return boarderBottom;
    }

    public void setBoarderBottom(int boarderBottom) {
        this.boarderBottom = boarderBottom;
    }

    public int getBoarderTop() {
        return boarderTop;
    }

    public void setBoarderTop(int boarderTop) {
        this.boarderTop = boarderTop;
    }

    public int getFoodIndex() {
        return foodIndex;
    }

    public void setFoodIndex(int foodIndex) {
        this.foodIndex = foodIndex;
    }
}
