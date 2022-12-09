package com.nhn.gameanvil.sample.game.user;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.annotation.ServiceName;
import com.nhn.gameanvil.annotation.UserType;
import com.nhn.gameanvil.exceptions.GameAnvilException;
import com.nhn.gameanvil.node.game.BaseUser;
import com.nhn.gameanvil.node.game.data.RoomMatchResult;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketDispatcher;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.sample.common.GameConstants;
import com.nhn.gameanvil.sample.game.GameNode;
import com.nhn.gameanvil.sample.game.multi.roommatch.model.UnlimitedTapRoomMatchForm;
import com.nhn.gameanvil.sample.game.multi.usermatch.model.SnakePositionInfo;
import com.nhn.gameanvil.sample.game.multi.usermatch.model.SnakeUserMatchInfo;
import com.nhn.gameanvil.sample.game.user._handler._ChangeNicknameReq;
import com.nhn.gameanvil.sample.game.user._handler._ShuffleDeckReq;
import com.nhn.gameanvil.sample.game.user._handler._SingleScoreRankingReq;
import com.nhn.gameanvil.sample.game.user.model.GameUserInfo;
import com.nhn.gameanvil.sample.game.user.model.GameUserTransferInfo;
import com.nhn.gameanvil.sample.protocol.Authentication;
import com.nhn.gameanvil.sample.protocol.GameMulti;
import com.nhn.gameanvil.sample.protocol.GameMulti.RoomUserData;
import com.nhn.gameanvil.sample.protocol.GameSingle;
import com.nhn.gameanvil.sample.protocol.Result;
import com.nhn.gameanvil.sample.protocol.Result.ErrorCode;
import com.nhn.gameanvil.sample.protocol.User;
import com.nhn.gameanvil.sample.protocol.User.RoomGameType;
import com.nhn.gameanvil.serializer.TransferPack;
import com.nhn.gameanvil.timer.Timer;
import com.nhn.gameanvil.timer.TimerHandler;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 게임에서 사용 하는 게임 유저
 */
@ServiceName(GameConstants.GAME_NAME)
@UserType(GameConstants.GAME_USER_TYPE)
public class GameUser extends BaseUser implements TimerHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    static private PacketDispatcher packetDispatcher = new PacketDispatcher();

    // 유저 필요한 데이터 객체
    private GameUserInfo gameUserInfo = new GameUserInfo();

    // 유저의 위치 리스트 객체
    private List<SnakePositionInfo> snakePositionInfoList = new ArrayList<>();

    static {
        packetDispatcher.registerMsg(User.ChangeNicknameReq.getDescriptor(), _ChangeNicknameReq.class);           // 닉네임 변경 프로토콜
        packetDispatcher.registerMsg(User.ShuffleDeckReq.getDescriptor(), _ShuffleDeckReq.class);                 // 덱 셔플 프로토콜
        packetDispatcher.registerMsg(GameSingle.ScoreRankingReq.getDescriptor(), _SingleScoreRankingReq.class);   // 싱글 점수 랭킹
    }

    /**
     * 유저 로그인 처리
     *
     * @param payload        로그인요청시 전달된 정보
     * @param accountPayload 게이트웨이의 전달된 정보
     * @param outPayload     로그인요청 완료시 응답으로 전달되는 정보
     * @return 로그인 성공 실패 반환
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    @Override
    public boolean onLogin(Payload payload, Payload accountPayload, Payload outPayload) throws SuspendExecution {
        logger.debug("onLogin - UserId : {}", getUserId());

        Result.ErrorCode resultCode = ErrorCode.UNKNOWN;
        boolean isSuccess = false;

        // 유저가 로그인시에 전달한 프로토콜 처리
        Packet loginPacket = payload.getPacket(Authentication.LoginReq.getDescriptor());

        if (loginPacket == null) {
            resultCode = ErrorCode.PARAMETER_IS_EMPTY;
            logger.error("GameUser::onLogin() onLogin fail!! loginPacket is null!!");
        } else {
            try {
                // 로그인 패킷
                Authentication.LoginReq loginReq = Authentication.LoginReq.parseFrom(loginPacket.getStream());
                logger.debug("onLogin - loginReq : {}", loginReq);

                if (loginReq == null || loginReq.getUuid().isEmpty()) {
                    resultCode = ErrorCode.PARAMETER_IS_EMPTY;
                    logger.error("GameUser::onLogin() onLogin fail!! loginReq is null!!");
                } else {
                    // 유저 고유 번호 설정
                    gameUserInfo.setUuid(loginReq.getUuid());

                    // 클라이언트에 전달 받는 디바이스 정보 설정
                    gameUserInfo.setLoginType(loginReq.getLoginTypeValue());
                    gameUserInfo.setAppVersion(loginReq.getAppVersion());
                    gameUserInfo.setAppStore(loginReq.getAppStore());
                    gameUserInfo.setDeviceModel(loginReq.getDeviceModel());
                    gameUserInfo.setDeviceCountry(loginReq.getDeviceCountry());
                    gameUserInfo.setDeviceLanguage(loginReq.getDeviceLanguage());

                    GameUserInfo dbGameUserInfo = null;

                    // DB에서 유저 데이터 검색
//                    if (GameConstants.USE_DB_JASYNC_SQL) {
//                        // JAsyncSql
//                        dbGameUserInfo = ((GameNode)getBaseGameNode()).getJAsyncSqlManager().selectUserByUuid(gameUserInfo.getUuid());
//                    } else {
//                        // Mybatis
//                        dbGameUserInfo = UserDbHelperService.getInstance().selectUserByUuid(gameUserInfo.getUuid());
//                    }
                    // xdev api
                    dbGameUserInfo = ((GameNode)getBaseGameNode()).getUserDbHelper().selectUserByUuid(gameUserInfo.getUuid());

                    if (dbGameUserInfo == null) {   // DB에 데이터가 없으므로 신규
                        // 게임 데이터 설정
                        gameUserInfo.setNickname("GameAnvil-" + getUserId());
                        gameUserInfo.setHeart(3);
                        gameUserInfo.setCoin(10000);
                        gameUserInfo.setRuby(100);
                        gameUserInfo.setLevel(1);
                        gameUserInfo.setExp(0);
                        gameUserInfo.setHighScore(0);
                        gameUserInfo.setCurrentDeck("sushi");

                        int dbResultCount = -1;
                        // 신규 DB 저장
//                        if (GameConstants.USE_DB_JASYNC_SQL) {
//                            // JAsyncSql
//                            dbResultCount = ((GameNode)getBaseGameNode()).getJAsyncSqlManager().insertUser(gameUserInfo);
//                        } else {
//                            // Mybatis
//                            dbResultCount = UserDbHelperService.getInstance().insertUser(gameUserInfo);
//                        }
                        // xdev api
                        dbResultCount = ((GameNode)getBaseGameNode()).getUserDbHelper().insertUser(gameUserInfo);

                        logger.info("DB User Insert {} ", dbResultCount);
                    } else {
                        // DB에서 가져온 유저 데이터 설정
                        gameUserInfo.setNickname(dbGameUserInfo.getNickname());
                        gameUserInfo.setHeart(dbGameUserInfo.getHeart());
                        gameUserInfo.setCoin(dbGameUserInfo.getCoin());
                        gameUserInfo.setRuby(dbGameUserInfo.getRuby());
                        gameUserInfo.setLevel(dbGameUserInfo.getLevel());
                        gameUserInfo.setExp(dbGameUserInfo.getExp());
                        gameUserInfo.setHighScore(dbGameUserInfo.getHighScore());
                        gameUserInfo.setCurrentDeck(dbGameUserInfo.getCurrentDeck());
                    }

                    logger.info("onLogin Success. userInfo:{}", gameUserInfo);
                    resultCode = ErrorCode.NONE;
                    isSuccess = true;

                    // 로그인한 유저 데이터 레디스에 세팅

                    boolean isRedisSuccess = ((GameNode)GameNode.getInstance()).getRedisHelper().setUserData(gameUserInfo);

                    if (!isRedisSuccess) {
                        logger.warn("Redis setUserData fail!!! {} ", gameUserInfo);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 로그인 처리후 클라이언트에 응답 프로토콜 작성
        Authentication.LoginRes.Builder loginRes = Authentication.LoginRes.newBuilder();
        loginRes.setErrorCode(resultCode);
        if (resultCode == ErrorCode.NONE) {
            loginRes.setUserdata(getUserDataByProto()); // 유저 데이터
        }

        outPayload.add(new Packet(loginRes));
        return isSuccess;
    }

    @Override
    public void onPostLogin() throws SuspendExecution {
        logger.debug("onPostLogin - UserId : {}", getUserId());
    }

    @Override
    public boolean onReLogin(Payload payload, Payload accountPayload, Payload outPayload) throws SuspendExecution {
        logger.debug("onReLogin - UserId : {}, payload {}, accountPayload {}, outPayload{}", getUserId(), payload, accountPayload, outPayload);

        // 로그인 처리후 클라이언트에 응답 프로토콜 작성
        Authentication.LoginRes.Builder loginRes = Authentication.LoginRes.newBuilder();
        loginRes.setUserdata(getUserDataByProto()); // 유저 데이터

        outPayload.add(new Packet(loginRes));
        return true;
    }

    @Override
    public void onDisconnect() throws SuspendExecution {
        logger.debug("onDisconnect - UserId : {}", getUserId());

        // 사용자가 접속이 끊길때 유저가 방안에 있다면 방에서 나오기
        if (isJoinedRoom()) {
            logger.info("kickoutRoom - UserId : {}", getUserType());
            try {
                kickoutRoom();
            } catch (GameAnvilException e) {
                logger.error("GameUser::onDisconnect()", e);
            }
        }
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        packetDispatcher.dispatch(this, packet);
    }

    @Override
    public void onPause() throws SuspendExecution {
        if (logger.isTraceEnabled()) {
            logger.trace("onPause");
        }

        if (!isJoinedRoom()) {
            try {
                kickout();
            } catch (GameAnvilException e) {
                logger.error("GameUser::onPause()", e);
            }
        }
    }

    @Override
    public void onResume() throws SuspendExecution {
        logger.debug("onResume - UserId : {}", getUserId());
    }

    @Override
    public void onLogout(Payload payload, Payload outPayload) throws SuspendExecution {
        logger.debug("onLogout - UserId : {}, payload : {}, outPayload : {}", getUserId(), payload, outPayload);
    }

    @Override
    public boolean canLogout() throws SuspendExecution {
        logger.debug("canLogout - UserId : {}", getUserId());
        return true;
    }

    @Override
    public void onPostLeaveRoom() throws SuspendExecution {
        logger.debug("onPostLeaveRoom - UserId: {}", getUserId());
    }

    @Override
    public boolean canTransfer() throws SuspendExecution {
        logger.debug("canTransfer - UserId : {}", getUserId());
        return false;
    }

    @Override
    public void onTimer(Timer timer, Object o) throws SuspendExecution {
        logger.debug("onTimer - UserId : {}", getUserId());
    }

    @Override
    public void onTransferOut(TransferPack transferPack) throws SuspendExecution {
        logger.debug("onTransferOut - UserId : {}", getUserId());
        GameUserTransferInfo gameUserTransferInfo = new GameUserTransferInfo(); // 트랜스퍼용 객체 생성
        gameUserTransferInfo.setGameUserInfo(gameUserInfo);
        gameUserTransferInfo.setGetSnakePositionInfoList(snakePositionInfoList);
        transferPack.put("GameUserInfo", gameUserTransferInfo);
    }

    @Override
    public void onTransferIn(TransferPack transferPack) throws SuspendExecution {
        logger.debug("onTransferIn - UserId : {}", getUserId());
        GameUserTransferInfo gameUserTransferInfo = (GameUserTransferInfo)transferPack.get("GameUserInfo");
        gameUserInfo = gameUserTransferInfo.getGameUserInfo();
        snakePositionInfoList = gameUserTransferInfo.getGetSnakePositionInfoList();
    }

    /**
     * 클라이언트 에서 룸매치 요청했을 경우 발생하는 경우 처리
     *
     * @param roomType 매칭되는 룸매치의 타입
     * @param payload  룸매치 요청시 전달되는 정보
     * @return 굴매치의 결과 정보 반환, null 을 반환 할시 클라이언트 요청 옵션에 따라서 새로운 방이 생성되거나 요청 실패 처리
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    @Override
    public final RoomMatchResult onMatchRoom(final String roomType, final String matchingGroup, final String matchingUserCategory, final Payload payload) throws SuspendExecution {
        logger.info("onMatchRoom - UserId : {}, RoomIdBeforeMove : {}", getUserId(), getRoomIdBeforeMove());
        try {
            UnlimitedTapRoomMatchForm unlimitedTapRoomForm = new UnlimitedTapRoomMatchForm();

            return matchRoom(matchingGroup, roomType, unlimitedTapRoomForm);
        } catch (Exception e) {
            logger.error("GameUser::onMatchRoom()", e);
            return RoomMatchResult.FAILED;
        }
    }

    /**
     * 클라이언트에서 우저 매치를 요청했을 경우 호출되는 처리
     *
     * @param roomType   유저 매치의 타입
     * @param payload    유저매치 요청시 전달되는 정보
     * @param outPayload 유저매치 요청 응답시에 전달되는 정보
     * @return 유저매치 요청의 성공여부 반환
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    @Override
    public boolean onMatchUser(final String roomType, final String matchingGroup, final Payload payload, Payload outPayload) throws SuspendExecution {
        logger.info("onMatchUser - UserId : {}", getUserId());
        try {
            SnakeUserMatchInfo term = new SnakeUserMatchInfo(getUserId(), 100);
            return matchUser(matchingGroup, roomType, term, payload);
        } catch (Exception e) {
            logger.error("GameUser::onMatchUser()", e);
        }
        return false;
    }


    public User.UserData getUserDataByProto() {     // 프로토콜 용 데이터 생성
        User.UserData.Builder userData = User.UserData.newBuilder();
        userData.setNickname(this.gameUserInfo.getNickname());
        userData.setHeart(this.gameUserInfo.getHeart());
        userData.setCoin(this.gameUserInfo.getCoin());
        userData.setRuby((this.gameUserInfo.getRuby()));
        userData.setLevel(this.gameUserInfo.getLevel());
        userData.setExp(this.gameUserInfo.getExp());
        userData.setHighScore(this.gameUserInfo.getHighScore());
        userData.setCurrentDeck(this.gameUserInfo.getCurrentDeck());

        return userData.build();
    }

    public GameMulti.RoomUserData getRoomUserDataByProto(long score) {    // 프로토콜용 데이터 생성
        RoomUserData.Builder roomUserData = RoomUserData.newBuilder();
        roomUserData.setId(this.gameUserInfo.getUuid());
        roomUserData.setNickName(this.gameUserInfo.getNickname());
        roomUserData.setScore(score);

        return roomUserData.build();
    }

    public GameUserInfo getGameUserInfo() {
        return gameUserInfo;
    }

    public List<SnakePositionInfo> getSnakePositionInfoList() {
        return snakePositionInfoList;
    }

    public User.RoomInfoMsg getRoomInfoMsgByProto(RoomGameType roomType) {
        User.RoomInfoMsg.Builder roomInfoMsg = User.RoomInfoMsg.newBuilder();
        roomInfoMsg.setRoomGameType(roomType);
        return roomInfoMsg.build();
    }
}
