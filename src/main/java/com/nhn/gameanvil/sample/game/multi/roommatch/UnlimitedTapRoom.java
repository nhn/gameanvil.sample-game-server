package com.nhn.gameanvil.sample.game.multi.roommatch;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.node.game.BaseRoom;
import com.nhn.gameanvil.node.game.RoomPacketDispatcher;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.sample.game.multi.roommatch._handler._ScoreUpMsg;
import com.nhn.gameanvil.sample.game.multi.roommatch.model.UnlimitedTapRoomInfo;
import com.nhn.gameanvil.sample.game.user.GameUser;
import com.nhn.gameanvil.sample.protocol.GameMulti;
import com.nhn.gameanvil.sample.protocol.GameMulti.TapBirdUserData;
import com.nhn.gameanvil.sample.protocol.User.RoomType;
import com.nhn.gameanvil.serializer.TransferPack;
import com.nhn.gameanvil.timer.Timer;
import com.nhn.gameanvil.timer.TimerHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 멀티 룸매치 룸 - 4인 게임 설정, 혼자서도 할수 있고 4명까지도 할수 있다, 계속 게임을 하는 방
 */
public class UnlimitedTapRoom extends BaseRoom<GameUser> implements TimerHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static RoomPacketDispatcher dispatcher = new RoomPacketDispatcher();

    private UnlimitedTapRoomInfo unlimitedTapRoomInfo = new UnlimitedTapRoomInfo();

    static {
        dispatcher.registerMsg(GameMulti.ScoreUpMsg.getDescriptor(), _ScoreUpMsg.class);
    }

    // 방에 등어온 유저 관리
    private Map<Integer, GameUser> gameUserMap;
    // 방안에 있는 점수
    private Map<String, Integer> gameUserScoreMap;

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("onInit - RoomId : {}", getId());

        gameUserMap = new TreeMap<>();
        gameUserScoreMap = new HashMap<>();
    }

    @Override
    public void onDestroy() throws SuspendExecution {
        logger.info("onDestroy - RoomId : {}", getId());
    }

    @Override
    public void onDispatch(GameUser gameUser, Packet packet) throws SuspendExecution {
        logger.info("onDispatch : RoomId : {}, UserId : {}, {}",
            getId(),
            gameUser.getUserId(),
            packet.getMsgName());
        dispatcher.dispatch(this, gameUser, packet);
    }

    /**
     * 방생성 및 입장
     *
     * @param gameUser   : 현재 방을 만드는 유저 객체
     * @param inPayload  : Room 생성 요청시 client 에서 보낸 data
     * @param outPayload : Room 생성 요청시 client 로 전달할 data
     * @return
     * @throws SuspendExecution
     */
    @Override
    public boolean onCreateRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("onCreateRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());

        try {
            gameUserMap.put(gameUser.getUserId(), gameUser);
            gameUserScoreMap.put(gameUser.getGameUserInfo().getUuid(), 0);

            // 현재 방에 입장한 유저 장보와 명수 업데이트
            unlimitedTapRoomInfo.setRoomId(getId());
            unlimitedTapRoomInfo.setUserCurrentCount(gameUserMap.size());
            updateRoomMatchInfo(unlimitedTapRoomInfo);

            outPayload.add(new Packet(gameUser.getRoomInfoMsgByProto(RoomType.ROOM_TAP)));
            return true;
        } catch (Exception e) {
            logger.error("onCreateRoom()", e);
            return false;
        }
    }


    /**
     * 방입장, 방이 만들어 져 있으면 입장
     *
     * @param gameUser   : 현재 방에 들어오는 유저 객체
     * @param inPayload  : Room 입장 요청시 client에서 보낸 data
     * @param outPayload : Room 생성 요청을 한  client 에게 전달할 data
     * @return
     * @throws SuspendExecution
     */
    @Override
    public boolean onJoinRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("onJoinRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        boolean isSuccess = false;
        try {
            logger.info("onJoinRoom - roomMatchMaking");
            if (gameUserMap.containsKey(gameUser.getUserId())) {
                logger.info("Already Joined User " + gameUser.getUserId());
            } else {

                gameUserMap.put(gameUser.getUserId(), gameUser);
                gameUserScoreMap.put(gameUser.getGameUserInfo().getUuid(), 0);

                // 유저 입장에 대한 방정보 업데이트 처리
                unlimitedTapRoomInfo.setUserCurrentCount(gameUserMap.size());
                updateRoomMatchInfo(unlimitedTapRoomInfo);

                isSuccess = true;
                outPayload.add(new Packet(gameUser.getRoomInfoMsgByProto(RoomType.ROOM_TAP)));
            }
        } catch (Exception e) {
            gameUserMap.remove(gameUser.getUserId());
            gameUserScoreMap.remove(gameUser.getGameUserInfo().getUuid());

            unlimitedTapRoomInfo.setUserCurrentCount(gameUserMap.size());
            logger.error("onJoinRoom()", e);
        }
        return isSuccess;
    }

    /**
     * 방 나갈때 처리
     *
     * @param gameUser   : 방나가는 유저 객체
     * @param inPayload  : Room 나가기 요청시 client 에서 보낸 data
     * @param outPayload : Room 나가기 요청을 한 client 에게 보낼 data
     * @return
     * @throws SuspendExecution
     */
    @Override
    public boolean onLeaveRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("onLeaveRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        try {
            gameUserMap.remove(gameUser.getUserId());
            gameUserScoreMap.remove(gameUser.getGameUserInfo().getUuid());

            // 방 정보 변경에대한 정보 업데이트
            unlimitedTapRoomInfo.setUserCurrentCount(gameUserMap.size());
            updateRoomMatchInfo(unlimitedTapRoomInfo);
            return true;
        } catch (Exception e) {
            gameUserMap.put(gameUser.getUserId(), gameUser);
            unlimitedTapRoomInfo.setUserCurrentCount(gameUserMap.size());
            return false;
        }
    }

    @Override
    public void onPostLeaveRoom(GameUser gameUser) throws SuspendExecution {
        logger.info("onPostLeaveRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
    }

    @Override
    public void onRejoinRoom(GameUser gameUser, Payload outPayload) throws SuspendExecution {
        logger.info("onRejoinRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
        outPayload.add(new Packet(gameUser.getRoomInfoMsgByProto(RoomType.ROOM_TAP)));
    }

    @Override
    public void onTransferOut(TransferPack transferPack) throws SuspendExecution {
        logger.info("onTransferOut - RoomId : {}", getId());
    }

    @Override
    public void onTransferIn(List<GameUser> userList, TransferPack transferPack) throws SuspendExecution {
        logger.info("onTransferIn - RoomId : {}", getId());
        this.gameUserMap.clear();
        for (GameUser user : userList) {
            this.gameUserMap.put(user.getUserId(), user);
        }
    }

    @Override
    public boolean canTransfer() throws SuspendExecution {
        logger.info("canTransfer - RoomId : {}", getId());
        return false;
    }

    @Override
    public void onTimer(Timer timer, Object arg) throws SuspendExecution {
        logger.info("onTimer - RoomId : {}", getId());
    }

    public Map<Integer, GameUser> getGameUserMap() {
        return gameUserMap;
    }

    public Map<String, Integer> getGameUserScoreMap() {
        return gameUserScoreMap;
    }

    public void setGameUserScoreMap(Map<String, Integer> gameUserScoreMap) {
        this.gameUserScoreMap = gameUserScoreMap;
    }

    /**
     * 유저들에게 전송할 룸에 있는 모든 유저 정보 프로토 패킷 생성
     *
     * @return 전체유저에게 전달할 프로토콜
     */
    public GameMulti.BroadcastTapBirdMsg getBroadcastMsgByProto() {
        List<TapBirdUserData> tapBirdDataList = new ArrayList<>();
        for (GameUser user : gameUserMap.values()) {
            TapBirdUserData.Builder tapBirdUserData = TapBirdUserData.newBuilder();
            tapBirdUserData.setUserData(user.getRoomUserDataByProto(gameUserScoreMap.get(user.getGameUserInfo().getUuid())));
            tapBirdDataList.add(tapBirdUserData.build());
        }

        GameMulti.BroadcastTapBirdMsg.Builder broadcastTapBirdMsg = GameMulti.BroadcastTapBirdMsg.newBuilder();
        broadcastTapBirdMsg.addAllTapBirdData(tapBirdDataList);

        return broadcastTapBirdMsg.build();
    }
}


