package com.nhn.gameanvil.sample.game.user.model;

import com.nhn.gameanvil.sample.db.mybatis.dto.UserDto;
import java.io.Serializable;

/**
 * 유저 데이터 클래스
 */
public class GameUserInfo implements Serializable {
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

    public int useHeart() {
        return this.heart--;
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

    public long useCoin(long useCoin) {
        return coin -= useCoin;
    }

    public long useRuby(long useRuby) {
        return ruby -= useRuby;
    }

    public UserDto toDtoUser() {
        UserDto userDto = new UserDto();
        userDto.setUuid(uuid);
        userDto.setLoginType(loginType);
        userDto.setAppVersion(appVersion);
        userDto.setAppStore(appStore);
        userDto.setDeviceModel(deviceModel);
        userDto.setDeviceCountry(deviceCountry);
        userDto.setDeviceLanguage(deviceLanguage);
        userDto.setNickname(nickname);
        userDto.setHeart(heart);
        userDto.setCoin(coin);
        userDto.setRuby(ruby);
        userDto.setLevel(level);
        userDto.setExp(exp);
        userDto.setHighScore(highScore);
        userDto.setCurrentDeck(currentDeck);
        return userDto;
    }

    @Override
    public String toString() {
        return "GameUserInfo{" +
            "uuid='" + uuid + '\'' +
            ", loginType=" + loginType +
            ", appVersion='" + appVersion + '\'' +
            ", appStore='" + appStore + '\'' +
            ", deviceModel='" + deviceModel + '\'' +
            ", deviceCountry='" + deviceCountry + '\'' +
            ", deviceLanguage='" + deviceLanguage + '\'' +
            ", nickname='" + nickname + '\'' +
            ", heart=" + heart +
            ", coin=" + coin +
            ", ruby=" + ruby +
            ", level=" + level +
            ", exp=" + exp +
            ", highScore=" + highScore +
            ", currentDeck='" + currentDeck + '\'' +
            '}';
    }
}
