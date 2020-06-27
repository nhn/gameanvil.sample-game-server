package com.nhn.gameflex.sample.mybatis.dto;

import com.nhn.gameflex.sample.game.user.model.GameUserInfo;
import java.util.Date;

/**
 * 유저 DB에 사용되는 객체
 */
public class UserDto {
    private String uuid;
    private int loginType;
    private String appVersion;
    private String appStore;
    private String deviceModel;
    private String deviceCountry;
    private String deviceLanguage;
    private String nickname;
    private int heart;
    private long coin;
    private long ruby;
    private int level;
    private long exp;
    private long highScore;
    private String currentDeck;
    private Date createDate;
    private Date updateDate;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getLoginType() {
        return loginType;
    }

    public void setLoginType(int loginType) {
        this.loginType = loginType;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppStore() {
        return appStore;
    }

    public void setAppStore(String appStore) {
        this.appStore = appStore;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceCountry() {
        return deviceCountry;
    }

    public void setDeviceCountry(String deviceCountry) {
        this.deviceCountry = deviceCountry;
    }

    public String getDeviceLanguage() {
        return deviceLanguage;
    }

    public void setDeviceLanguage(String deviceLanguage) {
        this.deviceLanguage = deviceLanguage;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getHeart() {
        return heart;
    }

    public void setHeart(int heart) {
        this.heart = heart;
    }

    public long getCoin() {
        return coin;
    }

    public void setCoin(long coin) {
        this.coin = coin;
    }

    public long getRuby() {
        return ruby;
    }

    public void setRuby(long ruby) {
        this.ruby = ruby;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public long getHighScore() {
        return highScore;
    }

    public void setHighScore(long highScore) {
        this.highScore = highScore;
    }

    public String getCurrentDeck() {
        return currentDeck;
    }

    public void setCurrentDeck(String currentDeck) {
        this.currentDeck = currentDeck;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public GameUserInfo toGameUserInfo() {
        GameUserInfo gameUserInfo = new GameUserInfo();
        gameUserInfo.setUuid(this.getUuid());
        gameUserInfo.setLoginType(this.getLoginType());
        gameUserInfo.setAppVersion(this.getAppVersion());
        gameUserInfo.setAppStore(this.getAppStore());
        gameUserInfo.setDeviceModel(this.getDeviceModel());
        gameUserInfo.setDeviceCountry(this.getDeviceCountry());
        gameUserInfo.setDeviceLanguage(this.getDeviceLanguage());
        gameUserInfo.setNickname(this.getNickname());
        gameUserInfo.setHeart(this.getHeart());
        gameUserInfo.setCoin(this.getCoin());
        gameUserInfo.setRuby(this.getRuby());
        gameUserInfo.setLevel(this.getLevel());
        gameUserInfo.setExp(this.getExp());
        gameUserInfo.setHighScore(this.getHighScore());
        gameUserInfo.setCurrentDeck(this.getCurrentDeck());
        return gameUserInfo;
    }
}
