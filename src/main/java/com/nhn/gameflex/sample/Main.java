package com.nhn.gameflex.sample;

import com.nhn.gameflex.sample.game.GameNode;
import com.nhn.gameflex.sample.common.GameConstants;
import com.nhn.gameflex.sample.protocol.Authentication;
import com.nhn.gameflex.sample.protocol.GameMulti;
import com.nhn.gameflex.sample.protocol.GameSingle;
import com.nhn.gameflex.sample.protocol.Result;
import com.nhn.gameflex.sample.protocol.User;
import com.nhn.gameflex.sample.support.LaunchingSupport;
import com.nhn.gameflex.sample.gateway.GameConnection;
import com.nhn.gameflex.sample.gateway.GameGatewayNode;
import com.nhn.gameflex.sample.gateway.GameSession;
import com.nhn.gameflex.sample.game.multi.roommatch.UnlimitedTapRoom;
import com.nhn.gameflex.sample.game.multi.roommatch.UnlimitedTapRoomMatchMaker;
import com.nhn.gameflex.sample.game.multi.roommatch.model.UnlimitedTapRoomInfo;
import com.nhn.gameflex.sample.game.multi.usermatch.SnakeRoom;
import com.nhn.gameflex.sample.game.multi.usermatch.SnakeRoomMatchMaker;
import com.nhn.gameflex.sample.game.multi.usermatch.model.SnakeRoomInfo;
import com.nhn.gameflex.sample.game.single.SingleGameRoom;
import com.nhn.gameflex.sample.game.user.GameUser;
import com.nhn.gameflex.GameflexBootstrap;

public class Main {

    public static void main(String[] args) {
        GameflexBootstrap bootstrap = GameflexBootstrap.getInstance();

        // 클라이언트와 전송할 프로토콜 정의 - 순서는 클라이언트와 동일 해야 한다.
        bootstrap.addProtoBufClass(0, Authentication.getDescriptor());
        bootstrap.addProtoBufClass(1, GameMulti.getDescriptor());
        bootstrap.addProtoBufClass(2, GameSingle.getDescriptor());
        bootstrap.addProtoBufClass(3, Result.getDescriptor());
        bootstrap.addProtoBufClass(4, User.getDescriptor());

        // 게임에서 사용하는 DB 쓰레드풀 지정
        bootstrap.createExecutorService(GameConstants.DB_THREAD_POOL, 50);
        // 게임에서 사용하는 레디스 쓰레드풀 지정
        bootstrap.createExecutorService(GameConstants.REDIS_THREAD_POOL, 50);


        // 세션설정
        bootstrap.setGateway()
            .connection(GameConnection.class)
            .session(GameSession.class)
            .node(GameGatewayNode.class)
            .enableWhiteModules();

        // 게임 스페이스 설정
        bootstrap.setGame(GameConstants.GAME_NAME)
            .node(GameNode.class)

            // 싱글 게임
            .user(GameConstants.GAME_USER_TYPE, GameUser.class)
            .room(GameConstants.GAME_ROOM_TYPE_SINGLE, SingleGameRoom.class)

            // 룸 매치 멀티게임 - 방에 들어가서 게임 : 무제한 탭
            .room(GameConstants.GAME_ROOM_TYPE_MULTI_ROOM_MATCH, UnlimitedTapRoom.class)
            .roomMatchMaker(GameConstants.GAME_ROOM_TYPE_MULTI_ROOM_MATCH, UnlimitedTapRoomMatchMaker.class, UnlimitedTapRoomInfo.class)

            // 유저 매치 멀티게임 - 유저들 매칭으로 인해 게임동시 입장 : 스테이크 게임
            .room(GameConstants.GAME_ROOM_TYPE_MULTI_USER_MATCH, SnakeRoom.class)
            .userMatchMaker(GameConstants.GAME_ROOM_TYPE_MULTI_USER_MATCH, SnakeRoomMatchMaker.class, SnakeRoomInfo.class);

        // 서비스 설정
        bootstrap.setSupport(GameConstants.SUPPORT_NAME_LAUNCHING)
            .node(LaunchingSupport.class);

        bootstrap.run();
    }
}