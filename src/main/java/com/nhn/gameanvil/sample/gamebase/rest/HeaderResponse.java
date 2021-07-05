package com.nhn.gameanvil.sample.gamebase.rest;

/**
 * Gamebase 기본 헤더 응답 클래스
 */
public class HeaderResponse {
    private String transactionId;
    private boolean isSuccessful;
    private int resultCode;
    private String resultMessage;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setIsSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    @Override
    public String toString() {
        return "HeaderResponse{" +
            "transactionId='" + transactionId + '\'' +
            ", isSuccessful=" + isSuccessful +
            ", resultCode=" + resultCode +
            ", resultMessage='" + resultMessage + '\'' +
            '}';
    }
}
