package com.nhn.gameanvil.sample.game.user.model;

import java.io.Serializable;


/**
 * 싱글 랭킹 정보 데이터 객체
 */
public class SingleRankingInfo implements Serializable {
    private String uuid;
    private String nickName;
    private double score;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "SingleRankingInfo{" +
            "uuid='" + uuid + '\'' +
            ", nickName='" + nickName + '\'' +
            ", score=" + score +
            '}';
    }
}
