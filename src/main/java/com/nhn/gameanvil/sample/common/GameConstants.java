package com.nhn.gameanvil.sample.common;

/**
 * 게임에서 사용하는 상수값들 정의
 */
public class GameConstants {
    // 게임서비스 스테이스 이름
    public static final String GAME_NAME = "TapTap";

    // 게임스페이스에서 사용할 유저 타입
    public static final String GAME_USER_TYPE = "TapTapUser";

    // 게임스페이스의 싱글룸
    public static final String GAME_ROOM_TYPE_SINGLE = "SingleTapRoom";

    // 게임 스페이스의 멀티룸 - 무제한 탭 게임
    public static final String GAME_ROOM_TYPE_MULTI_ROOM_MATCH = "UnlimitedTapRoom";

    // 게임 스페이스의 멀티룸 - 스네이크 게임
    public static final String GAME_ROOM_TYPE_MULTI_USER_MATCH = "SnakeRoom";

    // 서비스 이름 - 런칭
    public static final String SUPPORT_NAME_LAUNCHING = "Launching";

    // 레디스 쓰레드 풀
    public static final String REDIS_THREAD_POOL = "REDIS_THREAD_POOL";

    // 레디스 접속 정보
    public static final String REDIS_URL = "레디스 아이피 정보";        // TODO 레디스 접속 IP나 URL 정보 변경 설정
    public static final int REDIS_PORT = 0000;                       // TODO 레디스 접속 포트 변경 설정
    public static final String REDIS_PASSWORD = "레디스 패스워드 정보"; // TODO 레디스 접속 패스워드 변경 설정

    // 디비 쓰레드 풀
    public static final String DB_THREAD_POOL = "DB_THREAD_POOL";

    // DB 접속 정보
    public static final String DB_HOST = "localhost";           // TODO 구축한 DB 접속 정보 변경 필요
    public static final int DB_PORT = 3306;                     // 구축한 DB 접속 포트
    public static final String DB_DATABASE = "taptap";          // 구축한 DB 의 이름 정보
    public static final String DB_USERNAME = "계정이름";         // TODO 구축한 DB 의 계정 정보 변경 필요
    public static final String DB_PASSWORD = "패스워드";         // TODO 구축한 DB 의 패스워드 정보 변경 필요
    public static final int MAX_ACTIVE_CONNECTION = 30;

    // 게임베이스 기본 url
    public static final String GAMEBASE_DEFAULT_URL = "https://api-gamebase.cloud.toast.com";

    // 게임베이스 시크릿키
    public static final String GAMEBASE_SECRET_KEY = "oNQMhNXW";

    public static final boolean USE_DB_JASYNC_SQL = true;

}
