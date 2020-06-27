package com.nhn.gameflex.sample.gamebase.rest;

import java.util.List;

/**
 * Gamebase 인증 응답 객체
 */
public class AuthenticationResponse {
    private HeaderResponse header;
    private Member member;

    public HeaderResponse getHeader() {
        return header;
    }

    public void setHeader(HeaderResponse header) {
        this.header = header;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    // 인증 정보 리스트
    public static class AuthList {
        String userId;
        String authSystem;
        String idPCode;
        String authKey;
        String regDate;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getAuthSystem() {
            return authSystem;
        }

        public void setAuthSystem(String authSystem) {
            this.authSystem = authSystem;
        }

        public String getIdPCode() {
            return idPCode;
        }

        public void setIdPCode(String idPCode) {
            this.idPCode = idPCode;
        }

        public String getAuthKey() {
            return authKey;
        }

        public void setAuthKey(String authKey) {
            this.authKey = authKey;
        }

        public String getRegDate() {
            return regDate;
        }

        public void setRegDate(String regDate) {
            this.regDate = regDate;
        }
    }

    // 유저 정보
    public static class Member {
        String userId;
        String valid;
        String appId;
        String regDate;
        String lastLoginDate;
        List<AuthList> authList;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getValid() {
            return valid;
        }

        public void setValid(String valid) {
            this.valid = valid;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getRegDate() {
            return regDate;
        }

        public void setRegDate(String regDate) {
            this.regDate = regDate;
        }

        public String getLastLoginDate() {
            return lastLoginDate;
        }

        public void setLastLoginDate(String lastLoginDate) {
            this.lastLoginDate = lastLoginDate;
        }

        public List<AuthList> getAuthList() {
            return authList;
        }

        public void setAuthList(List<AuthList> authList) {
            this.authList = authList;
        }
    }
}
