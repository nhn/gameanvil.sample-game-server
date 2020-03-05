package com.nhn.tardis.sample.space.game.multi.usermatch.model;

import java.io.Serializable;

/**
 * 위치 정보 객체
 */
public class SnakePositionInfo implements Serializable {
    private int idx;
    private int x;
    private int y;

    public SnakePositionInfo(){

    }

    public SnakePositionInfo(int idx, int x, int y) {
        this.idx = idx;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    @Override
    public String toString() {
        return "SnakePositionInfo{" +
            "idx=" + idx +
            ", x=" + x +
            ", y=" + y +
            '}';
    }
}
