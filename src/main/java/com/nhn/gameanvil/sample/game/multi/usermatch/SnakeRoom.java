package com.nhn.gameanvil.sample.game.multi.usermatch;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.node.game.BaseRoom;
import com.nhn.gameanvil.node.game.RoomPacketDispatcher;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.sample.game.multi.usermatch._handler._SnakeRemoveFoodMsg;
import com.nhn.gameanvil.sample.game.multi.usermatch._handler._SnakeUserMsg;
import com.nhn.gameanvil.sample.game.multi.usermatch.model.SnakePositionInfo;
import com.nhn.gameanvil.sample.game.multi.usermatch.model.SnakeRoomTransferInfo;
import com.nhn.gameanvil.sample.game.user.GameUser;
import com.nhn.gameanvil.sample.protocol.GameMulti;
import com.nhn.gameanvil.sample.protocol.GameMulti.SnakeFoodMsg;
import com.nhn.gameanvil.sample.protocol.GameMulti.SnakePositionData;
import com.nhn.gameanvil.sample.protocol.GameMulti.SnakeUserData;
import com.nhn.gameanvil.sample.protocol.User.RoomType;
import com.nhn.gameanvil.serializer.TransferPack;
import com.nhn.gameanvil.timer.Timer;
import com.nhn.gameanvil.timer.TimerHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 유저 매치 snake 게임, 2인 플레이
 */
public class SnakeRoom extends BaseRoom<GameUser> {
    private static final int FOOD_MAX_COUNT = 200;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static RoomPacketDispatcher dispatcher = new RoomPacketDispatcher();

    // 서버에서 생성되는 food 리스트
    private List<SnakePositionInfo> foodList;
    // 방안에 있는 유저
    private Map<Integer, GameUser> gameUserMap;
    // 방안에 있는 점수
    private Map<String, Integer> gameUserScoreMap;

    // 방크기 설정
    private int boarderLeft = -35;
    private int boarderRight = 35;
    private int boarderBottom = -25;
    private int boarderTop = 25;

    // food 시작 인덱스 설정
    private int foodIndex = 0;

    static {
        dispatcher.registerMsg(GameMulti.SnakeUserMsg.getDescriptor(), _SnakeUserMsg.class);  // 유저 위치 정보
        dispatcher.registerMsg(GameMulti.SnakeFoodMsg.getDescriptor(), _SnakeRemoveFoodMsg.class);  // food 삭제 정보처리
    }

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("onInit - RoomId : {}", getId());

        // 데이터 초기화
        foodList = new ArrayList<>();
        gameUserMap = new TreeMap<>();
        gameUserScoreMap = new HashMap<>();
        foodIndex = 0;
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
     * 룸 생성및 입장, 첫번째 유저
     *
     * @param gameUser   : 룸생성 하는 유저 객체
     * @param inPayload  : Room 생성 요청시 client 에서 보낸 data
     * @param outPayload : Room 생성 요청시 client 로 전달할 data
     * @return 성공 여부
     * @throws SuspendExecution
     */
    @Override
    public boolean onCreateRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("onCreateRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        foodIndex = 0;

        try {
            // 첫번째 유저 위치 지정
            gameUser.getSnakePositionInfoList().clear();
            gameUser.getSnakePositionInfoList().add(new SnakePositionInfo(0, boarderLeft + 5, boarderTop - 5));

            gameUserMap.put(gameUser.getUserId(), gameUser);
            gameUserScoreMap.put(gameUser.getGameUserInfo().getUuid(), 0);
            outPayload.add(new Packet(gameUser.getRoomInfoMsgByProto(RoomType.ROOM_SNAKE)));
            return true;
        } catch (Exception e) {
            logger.error("onCreateRoom()", e);
            return false;
        }
    }

    /**
     * 두번쨰 유저 입장
     *
     * @param gameUser   : 입장하는 유저 객체
     * @param inPayload  : Room 입장 요청시 client에서 보낸 data
     * @param outPayload :  Room 생성 요청을 한  client 에게 전달할 data
     * @return 성공여부
     * @throws SuspendExecution
     */
    @Override
    public boolean onJoinRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("onJoinRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        boolean isSuccess = false;
        try {
            if (gameUserMap.containsKey(gameUser.getUserId())) {
                logger.info("Already Joined User " + gameUser.getUserId());
            } else {
                // 두번째 유저 설정
                gameUser.getSnakePositionInfoList().clear();
                gameUser.getSnakePositionInfoList().add(new SnakePositionInfo(1, boarderLeft + 5, boarderBottom + 5));

                gameUserMap.put(gameUser.getUserId(), gameUser);
                gameUserScoreMap.put(gameUser.getGameUserInfo().getUuid(), 0);
                if (gameUserMap.size() == 2) {  // 두명이들어왔을때 게임 시작
                    for (GameUser user : gameUserMap.values()) {
                        logger.info("onJoinRoom - UserId : {}, SnakeGamInfo : {}", getId(), getSnakeGameInfoMsgByProto());
                        user.send(new Packet(getSnakeGameInfoMsgByProto()));    // 두병 모두 들어왔을때 두유저에게 게임 정보 전송
                    }

                    // 1초에 한번씩 food 생성
                    addTimer(1, TimeUnit.SECONDS, 0, "SnakeGameTimerHandler", false);
                }
                isSuccess = true;
                outPayload.add(new Packet(gameUser.getRoomInfoMsgByProto(RoomType.ROOM_SNAKE)));
            }
        } catch (Exception e) {
            gameUserMap.remove(gameUser.getUserId());
            gameUserScoreMap.remove(gameUser.getGameUserInfo().getUuid());
            logger.error("onJoinRoom()", e);
        }
        return isSuccess;
    }

    @Override
    public boolean onLeaveRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("onLeaveRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        return true;
    }

    /**
     * 유저가 방을 떠나고 나서 처리
     *
     * @param gameUser 방을 나가는 유저 객체
     * @throws SuspendExecution
     */
    @Override
    public void onPostLeaveRoom(GameUser gameUser) throws SuspendExecution {
        logger.info("onPostLeaveRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());

        gameUserMap.remove(gameUser.getUserId());
        gameUserScoreMap.remove(gameUser.getGameUserInfo().getUuid());
        // 방나가고 난후  타이머 해제
        removeAllTimer();

        for (GameUser user : gameUserMap.values()) {    // 방에 있는 모두를 방에서 내보낸다.
            if (user.isJoinedRoom()) {
                logger.info("kickoutRoom - RoomId : {}, UserId : {}", getId(), user.getUserId());
                user.kickoutRoom();
            }
        }
    }

    @Override
    public void onRejoinRoom(GameUser gameUser, Payload outPayload) throws SuspendExecution {
        logger.info("onRejoinRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
        outPayload.add(new Packet(gameUser.getRoomInfoMsgByProto(RoomType.ROOM_SNAKE)));
    }

    @Override
    public boolean canTransfer() throws SuspendExecution {
        logger.info("canTransfer - RoomId : {}", getId());
        return false;
    }

    @Override
    public void onTransferOut(TransferPack transferPack) throws SuspendExecution {
        logger.info("onTransferOut - RoomId : {}", getId());
        SnakeRoomTransferInfo snakeRoomTransferInfo = new SnakeRoomTransferInfo();
        snakeRoomTransferInfo.setFoodList(foodList);
        snakeRoomTransferInfo.setGameUserMap(gameUserMap);
        snakeRoomTransferInfo.setGameUserScoreMap(gameUserScoreMap);
        snakeRoomTransferInfo.setBoarderLeft(boarderLeft);
        snakeRoomTransferInfo.setBoarderRight(boarderRight);
        snakeRoomTransferInfo.setBoarderTop(boarderTop);
        snakeRoomTransferInfo.setBoarderBottom(boarderBottom);
        snakeRoomTransferInfo.setFoodIndex(foodIndex);

        transferPack.put("SnakeRoomTransferInfo", snakeRoomTransferInfo);
    }

    @Override
    public void onTransferIn(List<GameUser> userList, TransferPack transferPack) throws SuspendExecution {
        logger.info("onTransferIn - RoomId : {}", getId());
        SnakeRoomTransferInfo snakeRoomTransferInfo = (SnakeRoomTransferInfo)transferPack.get("SnakeRoomTransferInfo");
        foodList = snakeRoomTransferInfo.getFoodList();
        gameUserMap = snakeRoomTransferInfo.getGameUserMap();
        gameUserScoreMap = snakeRoomTransferInfo.getGameUserScoreMap();
        boarderLeft = snakeRoomTransferInfo.getBoarderLeft();
        boarderRight = snakeRoomTransferInfo.getBoarderRight();
        boarderTop = snakeRoomTransferInfo.getBoarderTop();
        boarderBottom = snakeRoomTransferInfo.getBoarderBottom();
        foodIndex = snakeRoomTransferInfo.getFoodIndex();
    }

    @Override
    public void onRegisterTimerHandler() {
        registerTimerHandler("SnakeGameTimerHandler", (timerObject, object) -> {
            if (foodList.size() < FOOD_MAX_COUNT) {
                SnakePositionInfo newFood = null;
                boolean isDuplicate = false;
                do {
                    // 중복되지않게 서버에서 food 생성
                    isDuplicate = false;
                    newFood = new SnakePositionInfo(foodIndex + 1, ThreadLocalRandom.current().nextInt(boarderLeft, boarderRight + 1), ThreadLocalRandom.current().nextInt(boarderBottom, boarderTop + 1));
                    for (SnakePositionInfo food : foodList) {
                        if (food.getX() == newFood.getX() && food.getY() == newFood.getY()) {
                            isDuplicate = true;
                            logger.info("onTimer - new Food is isDuplicate : {}, foodIndex {}, newFood {}", getId(), foodIndex + 1, newFood);
                            break;
                        }
                    }

                    if (isDuplicate == false) {
                        newFood.setIdx(foodIndex++);
                        foodList.add(newFood);
                    }
                }
                while (isDuplicate);

                SnakeFoodMsg.Builder snakeNewFoodMsg = SnakeFoodMsg.newBuilder();
                SnakePositionData.Builder snakePositionData = SnakePositionData.newBuilder();
                snakePositionData.setIdx(newFood.getIdx());
                snakePositionData.setX(newFood.getX());
                snakePositionData.setY(newFood.getY());
                snakeNewFoodMsg.setFoodData(snakePositionData);

                // 유저에게 food 리스트 싱크
                for (GameUser gameUser : getGameUserMap().values()) {
                    gameUser.send(new Packet(snakeNewFoodMsg));
                }

                logger.info("onTimer - RoomId : {}, foodIndex {}, newFood {}", getId(), foodIndex, newFood);
            }
        });
    }

    public Map<Integer, GameUser> getGameUserMap() {
        return gameUserMap;
    }

    public List<SnakePositionInfo> getFoodList() {
        return foodList;
    }

    public Map<String, Integer> getGameUserScoreMap() {
        return gameUserScoreMap;
    }

    public List<GameMulti.SnakePositionData> getSnakePositionDataByProto(int userId) {
        List<SnakePositionData> snakePositionListData = new ArrayList<>();
        List<SnakePositionInfo> positionInfoList = null;
        if (userId == 0) {
            positionInfoList = foodList;
        } else {
            positionInfoList = gameUserMap.get(userId).getSnakePositionInfoList();
        }
        for (SnakePositionInfo food : positionInfoList) {
            SnakePositionData.Builder snakePositionData = SnakePositionData.newBuilder();
            snakePositionData.setIdx(food.getIdx());
            snakePositionData.setX(food.getX());
            snakePositionData.setY(food.getY());
            snakePositionListData.add(snakePositionData.build());
        }

        return snakePositionListData;
    }

    public GameMulti.SnakeGameInfoMsg getSnakeGameInfoMsgByProto() {
        GameMulti.SnakeGameInfoMsg.Builder snakeGameInfoMsg = GameMulti.SnakeGameInfoMsg.newBuilder();
        snakeGameInfoMsg.setBoarderLeft(boarderLeft);
        snakeGameInfoMsg.setBoarderRight(boarderRight);
        snakeGameInfoMsg.setBoarderBottom(boarderBottom);
        snakeGameInfoMsg.setBoarderTop(boarderTop);

        for (GameUser user : gameUserMap.values()) {
            SnakeUserData.Builder snakeUserData = SnakeUserData.newBuilder();
            snakeUserData.setBaseData(user.getRoomUserDataByProto(gameUserScoreMap.get(user.getGameUserInfo().getUuid())));
            snakeUserData.addAllUserPositionListData(getSnakePositionDataByProto(user.getUserId()));
            snakeGameInfoMsg.addUsers(snakeUserData.build());
        }

        return snakeGameInfoMsg.build();
    }
}


