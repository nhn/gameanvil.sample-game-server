package com.nhn.tardis.sample.space.user;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.tardis.sample.mybatis.UserDbHelperService;
import com.nhn.tardis.sample.protocol.Authentication;
import com.nhn.tardis.sample.protocol.GameMulti;
import com.nhn.tardis.sample.protocol.GameMulti.RoomUserData;
import com.nhn.tardis.sample.protocol.GameSingle;
import com.nhn.tardis.sample.protocol.Result;
import com.nhn.tardis.sample.protocol.Result.ErrorCode;
import com.nhn.tardis.sample.protocol.User;
import com.nhn.tardis.sample.space.GameNode;
import com.nhn.tardis.sample.space.game.multi.roommatch.model.UnlimitedTapRoomInfo;
import com.nhn.tardis.sample.space.game.multi.usermatch.model.SnakePositionInfo;
import com.nhn.tardis.sample.space.game.multi.usermatch.model.SnakeRoomInfo;
import com.nhn.tardis.sample.space.user.cmd.CmdChangeNicknameReq;
import com.nhn.tardis.sample.space.user.cmd.CmdShuffleDeckReq;
import com.nhn.tardis.sample.space.user.cmd.CmdSingleScoreRankingReq;
import com.nhn.tardis.sample.space.user.model.GameUserInfo;
import com.nhn.tardis.sample.space.user.model.GameUserTransferInfo;
import com.nhnent.tardis.define.PauseType;
import com.nhnent.tardis.node.game.BaseUser;
import com.nhnent.tardis.node.game.data.MatchRoomResult;
import com.nhnent.tardis.packet.Packet;
import com.nhnent.tardis.packet.PacketDispatcher;
import com.nhnent.tardis.packet.Payload;
import com.nhnent.tardis.serializer.KryoSerializer;
import com.nhnent.tardis.timer.Timer;
import com.nhnent.tardis.timer.TimerHandler;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 게임에서 사용 하는 게임 유저 객체
 */
public class GameUser extends BaseUser implements TimerHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    static private PacketDispatcher packetDispatcher = new PacketDispatcher();

    // 유저 필요한 데이터 객체
    private GameUserInfo gameUserInfo = new GameUserInfo();

    // 유저의 위치 리스트 객체
    private List<SnakePositionInfo> snakePositionInfoList = new ArrayList<>();

    static {
        packetDispatcher.registerMsg(User.ChangeNicknameReq.getDescriptor(), CmdChangeNicknameReq.class);           // 닉네임 변경 프로토콜
        packetDispatcher.registerMsg(User.ShuffleDeckReq.getDescriptor(), CmdShuffleDeckReq.class);                 // 덱 셔플 프로토콜
        packetDispatcher.registerMsg(GameSingle.ScoreRankingReq.getDescriptor(), CmdSingleScoreRankingReq.class);   // 싱글 점수 랭킹
    }

    /**
     * 유저 로그인 처리
     *
     * @param payload        : client 에서 전달된 data
     * @param accountPayload : session 의 onPreLogin 함수에서 전달된 data 값이다.
     * @param outPayload     : client 로 전달되는 data
     * @return 로그인 성공 실패
     * @throws SuspendExecution 이 메서드는 파이버가 suspend될 수 있다
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
            logger.error("onLogin fail!! loginPacket is null!!");
        } else {
            try {
                // 로그인 패킷
                Authentication.LoginReq loginReq = Authentication.LoginReq.parseFrom(loginPacket.getStream());
                logger.debug("onLogin - loginReq : {}", loginReq);

                if (loginReq == null || loginReq.getUuid().isEmpty()) {
                    resultCode = ErrorCode.PARAMETER_IS_EMPTY;
                    logger.error("onLogin fail!! loginReq is null!!");
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

                    // DB에서 유저 데이터 검색
                    GameUserInfo dbGameUserInfo = UserDbHelperService.getInstance().selectUserByUuid(gameUserInfo.getUuid());

                    if (dbGameUserInfo == null) {   // DB에 데이터가 없으므로 신규
                        // 게임 데이터 설정
                        gameUserInfo.setNickname("Tardis-" + getUserId().substring(getUserId().length() - 6));
                        gameUserInfo.setHeart(3);
                        gameUserInfo.setCoin(100);
                        gameUserInfo.setRuby(0);
                        gameUserInfo.setLevel(1);
                        gameUserInfo.setExp(0);
                        gameUserInfo.setHighScore(0);
                        gameUserInfo.setCurrentDeck("sushi");

                        // 신규 DB 저장
                        int dbResultCount = UserDbHelperService.getInstance().insertUser(gameUserInfo);
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
            kickoutRoom();
        }
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        packetDispatcher.dispatch(this, packet);
    }

    @Override
    public void onPause(PauseType pauseType) throws SuspendExecution {
        logger.debug("onPause - UserId : {}, pauseType : {}", getUserId(), pauseType);
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
    public boolean canTransfer() throws SuspendExecution {
        logger.debug("canTransfer - UserId : {}", getUserId());
        return false;
    }

    @Override
    public void onTimer(Timer timer, Object o) throws SuspendExecution {
        logger.debug("onTimer - UserId : {}", getUserId());
    }

    @Override
    public ByteBuffer onTransferOut() throws SuspendExecution {
        logger.debug("onTransferOut - UserId : {}", getUserId());
        GameUserTransferInfo gameUserTransferInfo = new GameUserTransferInfo(); // 트랜스터용 객체 생성
        gameUserTransferInfo.setGameUserInfo(gameUserInfo);
        gameUserTransferInfo.setGetSnakePositionInfoList(snakePositionInfoList);
        return KryoSerializer.write(gameUserTransferInfo);
    }

    @Override
    public void onTransferIn(final InputStream inputStream) throws SuspendExecution {
        logger.debug("onTransferIn - UserId : {}", getUserId());
        try {
            GameUserTransferInfo gameUserTransferInfo = (GameUserTransferInfo)KryoSerializer.read(inputStream);
            gameUserInfo = gameUserTransferInfo.getGameUserInfo();
            snakePositionInfoList = gameUserTransferInfo.getGetSnakePositionInfoList();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * client 에서 MatchRoom 을 요청했을 경우 발생하는 callback
     *
     * @param roomType : 매칭되는 roommatch 의 type
     * @param payload  : client 의 요청시 추가적으로 전달되는 data
     * @return matching 된 roommatch 의 정보 , null 을 반환 할시  client 요청 옵션에 따라서 새로운 방이 생성되거나,요청 실패 처리 된다.
     * @throws SuspendExecution
     */
    @Override
    public MatchRoomResult onMatchRoom(String roomType, Payload payload) throws SuspendExecution {
        logger.info("onMatchRoom - UserId : {}, RoomIdBeforeMove : {}", getUserId(), getRoomIdBeforeMove());
        try {
            UnlimitedTapRoomInfo terms = new UnlimitedTapRoomInfo();
            // moveRoom 옵션이 true일 경우 room에 참여중인 상태에서도 matchRoom 이 가능하다.
            // 이 경우 지금 참여중인 room에서 나간 후 다시 매칭 프로세스를 거치게 되며
            // 참여 가능한 game 목록을 요청할 때 방금 나온 room도 목록에 포함된다.
            // 따라서 이동하기 전 방금 나온 game 을 구분하기 위해 getRoomIdBeforeMove()로 roomId를 얻어와 terms 에 넣어준다.
            // 원래 있던 room에 다시 join 하는것을 허용하거나, moveRoom 옵션이 false 일 경우에는 하지 않아도 된다.
            //terms.setRoomId(getRoomIdBeforeMove());
            String matchingGroup = getChannelId();

            return matchRoom(matchingGroup, roomType, terms);
        } catch (Exception e) {
            logger.error("GameUser::onMatchRoom()", e);
            return MatchRoomResult.createFailure();
        }
    }

    /**
     * client 에서 MatchUserStart 를 요청했을 경우 호출되는 callback server에서는 UserMatchInfo를 저장하고 주기적으로 UserMatchMaker의 match() 함수를 호출함 UserMatchMaker의 match()함수에서는 getMatchRequests()를 호출하여 저장된 UserMatchInfo 목록을 가져오고 이 목록중에 조건에 맞는 UserMatchInfo를 찾아 매칭를 완료하게 됨. 여기에서 성공은 매칭의 성공 여부가 아닌 매칭 요청의 성공여부를 의미함.
     *
     * @param roomType   : 매칭되는 roommatch 의 type
     * @param payload    : client 의 요청시 추가적으로 전달되는 data
     * @param outPayload : 서버에서 client 로 전달되는 data
     * @return : true: usermatch matching 요청 성공,false: usermatch matching 요청 실패
     * @throws SuspendExecution
     */
    @Override
    public boolean onMatchUser(final String roomType, final Payload payload, Payload outPayload) throws SuspendExecution {
        logger.info("onMatchUser - UserId : {}", getUserId());
        try {
            String matchingGroup = getChannelId();
            SnakeRoomInfo term = new SnakeRoomInfo(getUserId(), 100);
            return matchUser(matchingGroup, roomType, term, payload);
        } catch (Exception e) {
            logger.error("onMatchUser()", e);
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
}
