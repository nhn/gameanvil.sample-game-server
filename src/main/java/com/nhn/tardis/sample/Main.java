package com.nhn.tardis.sample;

import com.nhn.tardis.sample.common.GameConstants;
import com.nhn.tardis.sample.protocol.Authentication;
import com.nhn.tardis.sample.protocol.GameMulti;
import com.nhn.tardis.sample.protocol.GameSingle;
import com.nhn.tardis.sample.protocol.Result;
import com.nhn.tardis.sample.protocol.User;
import com.nhn.tardis.sample.service.LaunchingService;
import com.nhn.tardis.sample.session.GameSession;
import com.nhn.tardis.sample.session.GameSessionNode;
import com.nhn.tardis.sample.session.GameSessionUser;
import com.nhn.tardis.sample.space.GameNode;
import com.nhn.tardis.sample.space.game.multi.roommatch.UnlimitedTapRoom;
import com.nhn.tardis.sample.space.game.multi.roommatch.UnlimitedTapRoomMatchMaker;
import com.nhn.tardis.sample.space.game.multi.roommatch.model.UnlimitedTapRoomInfo;
import com.nhn.tardis.sample.space.game.multi.usermatch.SnakeRoom;
import com.nhn.tardis.sample.space.game.multi.usermatch.SnakeRoomMatchMaker;
import com.nhn.tardis.sample.space.game.multi.usermatch.model.SnakeRoomInfo;
import com.nhn.tardis.sample.space.game.single.SingleGameRoom;
import com.nhn.tardis.sample.space.user.GameUser;
import com.nhnent.tardis.console.TardisBootstrap;

public class Main {

    public static void main(String[] args) {
        TardisBootstrap bootstrap = TardisBootstrap.getInstance();

        // 클라이언트와 전송할 프로토콜 정의 - 순서는 클라이언트와 동일 해야 한다.
        bootstrap.addProtoBufClass(0, Authentication.getDescriptor());
        bootstrap.addProtoBufClass(1, GameMulti.getDescriptor());
        bootstrap.addProtoBufClass(2, GameSingle.getDescriptor());
        bootstrap.addProtoBufClass(3, Result.getDescriptor());
        bootstrap.addProtoBufClass(4, User.getDescriptor());

        // 게임에서 사용하는 DB 쓰레드풀 지정
        bootstrap.createExcutorService(GameConstants.DB_THREAD_POOL, 20);
        // 게임에서 사용하는 레디스 쓰레드풀 지정
        bootstrap.createExcutorService(GameConstants.REDIS_THREAD_POOL, 20);

        // 세션설정
        bootstrap.setSession()
            .session(GameSession.class)
            .user(GameSessionUser.class)
            .node(GameSessionNode.class)
            .enableWhiteModules();

        // 게임 스페이스 설정
        bootstrap.setSpace(GameConstants.GAME_SPACE_NAME)
            .node(GameNode.class)

            // 싱글 게임
            .user(GameConstants.SPACE_USER_TYPE, GameUser.class)
            .room(GameConstants.SPACE_ROOM_TYPE_SINGLE, SingleGameRoom.class)

            // 룸 매치 멀티게임 - 방에 들어가서 게임 : 무제한 탭
            .room(GameConstants.SPACE_ROOM_TYPE_MULTI_ROOM_MATCH, UnlimitedTapRoom.class)
            .roomMatchMaker(GameConstants.SPACE_ROOM_TYPE_MULTI_ROOM_MATCH, UnlimitedTapRoomMatchMaker.class, UnlimitedTapRoomInfo.class)

            // 유저 매치 멀티게임 - 유저들 매칭으로 인해 게임동시 입장 : 스테이크 게임
            .room(GameConstants.SPACE_ROOM_TYPE_MULTI_USER_MATCH, SnakeRoom.class)
            .userMatchMaker(GameConstants.SPACE_ROOM_TYPE_MULTI_USER_MATCH, SnakeRoomMatchMaker.class, SnakeRoomInfo.class);

        // 서비스 설정
        bootstrap.setService(GameConstants.SERVICE_NAME_LAUNCHING)
            .node(LaunchingService.class);

        bootstrap.run();
    }
}