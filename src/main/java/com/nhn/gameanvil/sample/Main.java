package com.nhn.gameanvil.sample;

import com.nhn.gameanvil.GameAnvilServer;
import com.nhn.gameanvil.sample.common.GameConstants;
import com.nhn.gameanvil.sample.protocol.*;

public class Main {

    public static void main(String[] args) {
        GameAnvilServer gameAnvilServer = GameAnvilServer.getInstance();

        // 클라이언트와 전송할 프로토콜 정의 - 순서는 클라이언트와 동일 해야 한다.
        gameAnvilServer.addProtoBufClass(Authentication.class);
        gameAnvilServer.addProtoBufClass(GameMulti.class);
        gameAnvilServer.addProtoBufClass(GameSingle.class);
        gameAnvilServer.addProtoBufClass(Result.class);
        gameAnvilServer.addProtoBufClass(User.class);

        // 게임에서 사용하는 DB 쓰레드풀 지정
        gameAnvilServer.createExecutorService(GameConstants.DB_THREAD_POOL, 100);
        // 게임에서 사용하는 레디스 쓰레드풀 지정
        gameAnvilServer.createExecutorService(GameConstants.REDIS_THREAD_POOL, 100);

        // annotation 클래스 등록 처리를 위해 scan package 지정
        gameAnvilServer.addPackageToScan("com.nhn.gameanvil.sample");

        // 서버 실행
        gameAnvilServer.run();

        // 서버가 정지 하고 나서 이후 부분이 처리가 된다.
    }
}