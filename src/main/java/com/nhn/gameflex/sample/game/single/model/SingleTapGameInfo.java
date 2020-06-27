package com.nhn.gameflex.sample.game.single.model;

import java.io.Serializable;

/**
 * 싱글 룸에서 사용 하는 데이터 객체
 */
public class SingleTapGameInfo implements Serializable {
    private String deck;
    private int difficulty;
    private int score;
    private long totalScore;
    private int combo;

    public String getDeck() {
        return deck;
    }

    public void setDeck(String deck) {
        this.deck = deck;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public long getTotalScore() {
//        totalScore = score * 100;
//        return totalScore;
        return score;
    }

    public void setCombo(int combo) {
        this.combo = combo;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int addScore() {
        return score++;
    }

    public void setTotalScore(long totalScore) {
        this.totalScore = totalScore;
    }

    public int getCombo() {
        return combo;
    }

    @Override
    public String toString() {
        return "SingleTapGameInfo{" +
            "deck='" + deck + '\'' +
            ", difficulty=" + difficulty +
            ", score=" + score +
            ", totalScore=" + totalScore +
            ", combo=" + combo +
            '}';
    }
}
