package com.nhn.gameanvil.sample.game.single;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.annotation.RoomType;
import com.nhn.gameanvil.annotation.ServiceName;
import com.nhn.gameanvil.node.game.BaseRoom;
import com.nhn.gameanvil.node.game.RoomPacketDispatcher;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.sample.common.GameConstants;
import com.nhn.gameanvil.sample.game.GameNode;
import com.nhn.gameanvil.sample.game.single._handler._TapMsg;
import com.nhn.gameanvil.sample.game.single.model.SingleTapGameInfo;
import com.nhn.gameanvil.sample.game.user.GameUser;
import com.nhn.gameanvil.sample.protocol.GameSingle;
import com.nhn.gameanvil.sample.protocol.Result;
import com.nhn.gameanvil.sample.protocol.Result.ErrorCode;
import com.nhn.gameanvil.sample.protocol.User.RoomGameType;
import com.nhn.gameanvil.serializer.TransferPack;
import com.nhn.gameanvil.timer.Timer;
import com.nhn.gameanvil.timer.TimerHandler;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 게임 혼자 하는 싱글룸 처리
 */
@ServiceName(GameConstants.GAME_NAME)
@RoomType(GameConstants.GAME_ROOM_TYPE_SINGLE)
public class SingleGameRoom extends BaseRoom<GameUser> implements TimerHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static RoomPacketDispatcher dispatcher = new RoomPacketDispatcher();

    // 게임에서 사용 할 데이터
    private SingleTapGameInfo singleGameData = new SingleTapGameInfo();

    static {
        dispatcher.registerMsg(GameSingle.TapMsg.getDescriptor(), _TapMsg.class);
    }

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("onInit - RoomId : {}", getId());
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
     * @param gameUser   방생성 및 입장 요정한 게임 유저 객체
     * @param inPayload  방생성 요청시 받은 정보
     * @param outPayload 방생성 요청에 응답을 보낼 정보
     * @return 룸 생성 성공여부 반환
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    @Override
    public boolean onCreateRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        // 싱글게임 처리로 혼자서 방을 만들고 게임을 한다.
        logger.info("onCreateRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());

        // 에러코드 기본값 설정
        Result.ErrorCode resultCode = Result.ErrorCode.UNKNOWN;

        // 게임시작 패킷 확인
        Packet startGamePacket = inPayload.getPacket(GameSingle.StartGameReq.getDescriptor());

        if (startGamePacket == null) {
            resultCode = Result.ErrorCode.PARAMETER_IS_EMPTY;
            logger.error("SingleGameRoom::onCreateRoom() fail!! startGamePacket is null!!");
        } else {
            try {
                // 게임시작 데이터 확인
                GameSingle.StartGameReq startGameReq = GameSingle.StartGameReq.parseFrom(startGamePacket.getStream());
                logger.debug("onCreateRoom - startGameReq : {}", startGameReq);

                if (startGameReq == null || startGameReq.getDeck().isEmpty()) {
                    resultCode = Result.ErrorCode.PARAMETER_IS_EMPTY;
                    logger.error("SingleGameRoom::onCreateRoom() fail!! startGameReq is null!!");
                } else if (gameUser.getGameUserInfo().getHeart() < 1) {
                    resultCode = Result.ErrorCode.NOT_ENOUGH_HEART;
                    logger.info("onCreateRoom fail!! NOT_ENOUGH_HEART is null!! : {}", gameUser.getGameUserInfo().getHeart());
                } else {
                    //--------------------------------- 게임 입장 로직 처리

                    gameUser.getGameUserInfo().useHeart();

                    // 게임에서 사용할 데이터 설정
                    singleGameData.setDeck(startGameReq.getDeck());
                    singleGameData.setDifficulty(startGameReq.getDifficultyValue());

                    logger.info("onCreateRoom Success. singleGameData:{}", singleGameData);
                    resultCode = Result.ErrorCode.NONE;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 게임 시작에 대한 응답
        GameSingle.StartGameRes.Builder startGameRes = GameSingle.StartGameRes.newBuilder();
        startGameRes.setErrorCode(resultCode);

        if (resultCode == Result.ErrorCode.NONE) {
            outPayload.add(new Packet(startGameRes));
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean onJoinRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("onJoinRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
        outPayload.add(new Packet(gameUser.getRoomInfoMsgByProto(RoomGameType.ROOM_SINGLE)));
        return true;
    }

    /**
     * 빙 나가기
     *
     * @param gameUser   방나가기 요청한 게임 유저 객체
     * @param inPayload  방 나가는 요청시 받은 정보
     * @param outPayload 방 나가는 요청에 응답을 보낼 정보
     * @return 방 나가기 성공 여부 반환
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    @Override
    public boolean onLeaveRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        // 게임이 종료되고 게임을 나갈때 처리
        logger.info("onLeaveRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());

        // 에러 코드 기본값 설정
        Result.ErrorCode resultCode = Result.ErrorCode.UNKNOWN;
        // 게임 종료 응답 데이터 설정정
        GameSingle.EndGameRes.Builder endGameRes = GameSingle.EndGameRes.newBuilder();

        // 게임 나가기 전에 레디스에 최고 스코어 저장
        if (gameUser.getGameUserInfo().getHighScore() < singleGameData.getScore()) {
            gameUser.getGameUserInfo().setHighScore(singleGameData.getScore());
            boolean isSuccess = false;
            int dbSuccessCount = -1;
//            try {
//                if (GameConstants.USE_DB_JASYNC_SQL) {
//                    // JAsyncSql
//                    dbSuccessCount = ((GameNode)getBaseGameNode()).getJAsyncSqlManager().updateUserHigScore(gameUser.getGameUserInfo().getUuid(), singleGameData.getScore());
//                } else {
//                    // Mybatis
//                    dbSuccessCount = UserDbHelperService.getInstance().updateUserHigScore(gameUser.getGameUserInfo().getUuid(), singleGameData.getScore());
//                }
//            } catch (TimeoutException e) {
//                logger.error("SingleGameRoom::onLeaveRoom()", e);
//            }
            // xdev api
            dbSuccessCount = ((GameNode)getBaseGameNode()).getUserDbHelper().updateUserHigScore(gameUser.getGameUserInfo().getUuid(), singleGameData.getScore());

            logger.info("DB update Result : {}", dbSuccessCount);
            if (dbSuccessCount == 1) {
                isSuccess = true;
            }

            if (isSuccess) {
                isSuccess = ((GameNode)GameNode.getInstance()).getRedisHelper().setSingleScore(gameUser.getGameUserInfo().getUuid(), singleGameData.getScore());
                logger.info("Redis set Result : {}", isSuccess);
            }

            if (!isSuccess) {
                resultCode = ErrorCode.DB_ERROR;
                endGameRes.setErrorCode(resultCode);
                outPayload.add(new Packet(endGameRes));
                return false;
            }
        }

        // 게임 종료 패킷 확인
        Packet endGamePacket = inPayload.getPacket(GameSingle.EndGameReq.getDescriptor());

        if (endGamePacket == null) {
            resultCode = Result.ErrorCode.PARAMETER_IS_EMPTY;
            logger.error("SingleGameRoom::onLeaveRoom() fail!! endGamePacket is null!!");
        } else {
            try {
                // 게임 종료 데이터 확인
                GameSingle.EndGameReq endGameReq = GameSingle.EndGameReq.parseFrom(endGamePacket.getStream());
                logger.debug("onLeaveRoom - endGameReq : {}", endGameReq);

                if (endGameReq == null) {
                    resultCode = Result.ErrorCode.PARAMETER_IS_EMPTY;
                    logger.error("SingleGameRoom::onLeaveRoom()  fail!! endGameReq is null!!");
                } else {
                    //--------------------------------- 게임 종료 로직 처리

                    logger.info("onLeaveRoom Success. singleGameData:{}", singleGameData);
                    resultCode = Result.ErrorCode.NONE;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        endGameRes.setErrorCode(resultCode);
        if (resultCode == Result.ErrorCode.NONE) {
            endGameRes.setUserData(gameUser.getUserDataByProto());
            endGameRes.setTotalScore(singleGameData.getTotalScore());
            logger.info("onLeaveRoom endGameRes:{}", endGameRes);

            outPayload.add(new Packet(endGameRes));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onLeaveRoom(GameUser gameUser) throws SuspendExecution {
        logger.info("onLeaveRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
    }

    @Override
    public void onPostLeaveRoom() throws SuspendExecution {
        logger.info("onPostLeaveRoom - RoomId : {}", getId());
    }

    @Override
    public void onRejoinRoom(GameUser gameUser, Payload outPayload) throws SuspendExecution {
        logger.info("onRejoinRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
        outPayload.add(new Packet(gameUser.getRoomInfoMsgByProto(RoomGameType.ROOM_SINGLE)));
    }

    @Override
    public void onTransferOut(TransferPack transferPack) throws SuspendExecution {
        transferPack.put("SingleGameData", singleGameData);
    }

    @Override
    public void onTransferIn(List<GameUser> userList, TransferPack transferPack) throws SuspendExecution {
        singleGameData = (SingleTapGameInfo)transferPack.get("SingleGameData");
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

    public SingleTapGameInfo getSingleGameData() {
        return singleGameData;
    }
}
