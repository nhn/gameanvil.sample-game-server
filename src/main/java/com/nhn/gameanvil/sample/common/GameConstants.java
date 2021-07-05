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
    public static final String REDIS_URL = "10.77.14.22"; // GameAnvil 팀 서버
    public static final int REDIS_PORT = 7500;
    public static final String REDIS_PASSWORD = "password";

    // 디비 쓰레드 풀
    public static final String DB_THREAD_POOL = "DB_THREAD_POOL";

    // DB 접속 정보
    public static final String DB_USERNAME = "taptap";
    public static final String DB_HOST = "10.77.14.22"; // GameAnvil 팀 서버
    public static final int DB_PORT = 3306;
    public static final String DB_PASSWORD = "nhn!@#123";
    public static final String DB_DATABASE = "taptap";
    public static final int MAX_ACTIVE_CONNECTION = 30;

    // 게임베이스 기본 url
    public static final String GAMEBASE_DEFAULT_URL = "https://api-gamebase.cloud.toast.com";

    // 게임베이스 시크릿키
    public static final String GAMEBASE_SECRET_KEY = "oNQMhNXW";

    public static final boolean USE_DB_JASYNC_SQL = true;

}
