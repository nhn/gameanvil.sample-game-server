package com.nhn.tardis.sample.space.game.single;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.tardis.sample.mybatis.UserDbHelperService;
import com.nhn.tardis.sample.protocol.GameSingle;
import com.nhn.tardis.sample.protocol.Result;
import com.nhn.tardis.sample.protocol.Result.ErrorCode;
import com.nhn.tardis.sample.redis.RedisHelperService;
import com.nhn.tardis.sample.space.game.single.cmd.CmdTapMsg;
import com.nhn.tardis.sample.space.game.single.model.SingleTapGameInfo;
import com.nhn.tardis.sample.space.user.GameUser;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.ITimerHandler;
import com.nhnent.tardis.common.internal.ITimerObject;
import com.nhnent.tardis.common.serializer.KryoSerializer;
import com.nhnent.tardis.console.space.IRoom;
import com.nhnent.tardis.console.space.RoomAgent;
import com.nhnent.tardis.console.space.RoomPacketDispatcher;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 게임 혼자 하는 싱글룸 처리
 */
public class SingleGameRoom extends RoomAgent implements IRoom<GameUser>, ITimerHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static RoomPacketDispatcher dispatcher = new RoomPacketDispatcher();

    // 게임에서 사용 할 데이터
    private SingleTapGameInfo singleGameData = new SingleTapGameInfo();

    static {
        dispatcher.registerMsg(GameSingle.TapMsg.getDescriptor(), CmdTapMsg.class);
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
    public void onDispatch(GameUser spaceUser, Packet packet) throws SuspendExecution {
        logger.info("onDispatch : RoomId : {}, UserId : {}, {}",
            getId(),
            spaceUser.getUserId(),
            packet.getMsgName());
        dispatcher.dispatch(this, spaceUser, packet);
    }

    /**
     * 룸생성 및 입장
     *
     * @param gameUser   : 로그인된 게임 유저 객체
     * @param inPayload  : Room 생성 요청시 client 에서 보낸 data
     * @param outPayload : Room 생성 요청시 client 로 전달할 data
     * @return 룸 생성 성공여부
     * @throws SuspendExecution
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
            logger.error("onCreateRoom fail!! startGamePacket is null!!");
        } else {
            try {
                // 게임시작 데이터 확인
                GameSingle.StartGameReq startGameReq = GameSingle.StartGameReq.parseFrom(startGamePacket.getStream());
                logger.debug("onCreateRoom - startGameReq : {}", startGameReq);

                if (startGameReq == null || startGameReq.getDeck().isEmpty()) {
                    resultCode = Result.ErrorCode.PARAMETER_IS_EMPTY;
                    logger.error("onCreateRoom fail!! startGameReq is null!!");
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

        return true;
    }

    /**
     * 룸 나가기
     *
     * @param gameUser   : 로그인된 게임 유저 객체
     * @param inPayload  : Room 나가기 요청시 client 에서 보낸 data
     * @param outPayload : Room 나가기 요청을 한 client 에게 보낼 data
     * @return
     * @throws SuspendExecution
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
            int dbSuccessCount = UserDbHelperService.getInstance().updateUserHigScore(gameUser.getGameUserInfo().getUuid(), singleGameData.getScore());
            logger.info("DB update Result : {}", dbSuccessCount);
            if (dbSuccessCount == 1) {
                isSuccess = true;
            }

            if (isSuccess) {
                isSuccess = RedisHelperService.getInstance().setSingleScore(gameUser.getGameUserInfo().getUuid(), singleGameData.getScore());
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
            logger.error("onLeaveRoom fail!! endGamePacket is null!!");
        } else {
            try {
                // 게임 종료 데이터 확인
                GameSingle.EndGameReq endGameReq = GameSingle.EndGameReq.parseFrom(endGamePacket.getStream());
                logger.debug("onLeaveRoom - endGameReq : {}", endGameReq);

                if (endGameReq == null) {
                    resultCode = Result.ErrorCode.PARAMETER_IS_EMPTY;
                    logger.error("onLeaveRoom fail!! endGameReq is null!!");
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
    public void onPostLeaveRoom(GameUser gameUser) throws SuspendExecution {
        logger.info("onPostLeaveRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
    }

    @Override
    public void onRejoinRoom(GameUser gameUser, Payload outPayload) throws SuspendExecution {
        logger.info("onRejoinRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
    }

    @Override
    public ByteBuffer onTransferOut() throws SuspendExecution {
        return KryoSerializer.write(singleGameData);
    }

    @Override
    public void onTransferIn(List<GameUser> userList, InputStream inputStream) throws SuspendExecution {
        try {
            singleGameData = (SingleTapGameInfo)KryoSerializer.read(inputStream);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public boolean canTransfer() throws SuspendExecution {
        logger.info("canTransfer - RoomId : {}", getId());
        return false;
    }

    @Override
    public void onTimer(ITimerObject iTimerObject, Object arg) throws SuspendExecution {
        logger.info("onTimer - RoomId : {}", getId());
    }

    public SingleTapGameInfo getSingleGameData() {
        return singleGameData;
    }
}
