package unit_test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhn.tardis.sample.common.GameConstants;
import com.nhn.tardis.sample.protocol.Authentication;
import com.nhn.tardis.sample.protocol.Authentication.LoginType;
import com.nhn.tardis.sample.protocol.GameMulti;
import com.nhn.tardis.sample.protocol.GameMulti.TapBirdUserData;
import com.nhn.tardis.sample.protocol.GameSingle;
import com.nhn.tardis.sample.protocol.GameSingle.DifficultyType;
import com.nhn.tardis.sample.protocol.GameSingle.EndType;
import com.nhn.tardis.sample.protocol.GameSingle.TapMsg;
import com.nhn.tardis.sample.protocol.Result;
import com.nhn.tardis.sample.protocol.Result.ErrorCode;
import com.nhn.tardis.sample.protocol.User;
import com.nhn.tardis.sample.protocol.User.CurrencyType;
import com.nhnent.tardis.common.protocol.Base;
import com.nhnent.tardis.common.protocol.Base.ResultCodeMatchRoom;
import com.nhnent.tardis.connector.common.Config;
import com.nhnent.tardis.connector.protocol.result.AuthenticationResult;
import com.nhnent.tardis.connector.protocol.result.CreateRoomResult;
import com.nhnent.tardis.connector.protocol.result.LeaveRoomResult;
import com.nhnent.tardis.connector.protocol.result.LoginResult;
import com.nhnent.tardis.connector.protocol.result.MatchRoomResult;
import com.nhnent.tardis.connector.protocol.result.MatchUserStartResult;
import com.nhnent.tardis.connector.tcp.ConnectorSession;
import com.nhnent.tardis.connector.tcp.ConnectorUser;
import com.nhnent.tardis.connector.tcp.TardisConnector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CheckGame {
    private static TardisConnector connector;
    private List<ConnectorUser> users = new ArrayList<>();

    //-------------------------------------------------------------------------------------

    @BeforeClass
    public static void configuration() {

        // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
        Config.addRemoteInfo("10.77.35.47", 11200);
        Config.WAIT_RECV_TIMEOUT_MSEC = 3000;

        // 커넥터와, Base 프로토콜 사용 편의를 위해 Helper 를 생성합니다.
        connector = TardisConnector.getInstance();

        // 컨텐츠 프로토콜 등록.
        connector.addProtoBufClass(0, Authentication.class);
        connector.addProtoBufClass(1, GameMulti.class);
        connector.addProtoBufClass(2, GameSingle.class);
        connector.addProtoBufClass(3, Result.class);
        connector.addProtoBufClass(4, User.class);

        // 컨텐츠 서비스 등록.
        connector.addService(0, GameConstants.GAME_SPACE_NAME);
    }

    //-------------------------------------------------------------------------------------

    @Before
    public void setUp() throws TimeoutException {
        for (int i = 0; i < 4; ++i) {
            // 커넥션을 생성하고 세션 정보가 담긴 객체를 리턴.
            ConnectorSession session = connector.addSession(connector.getIncrementedValue("user_"), connector.makeUniqueId());

            // 인증 검증을 위한 토큰 정보 생성
            Authentication.AuthenticationReq.Builder authenticationReq = Authentication.AuthenticationReq.newBuilder().setAccessToken("TapTap_accessToken!!!!");

            // 인증을 진행.
            AuthenticationResult authResult = session.authentication(session.getAccountId(), authenticationReq);
            assertTrue(authResult.isSuccess());

            // 세션에 유저를 등록하고, 각종 ID 정보가 담긴 유저 객체를 리턴.
            ConnectorUser user = session.addUser(GameConstants.GAME_SPACE_NAME);

            // 로그인을 진행.
            Authentication.LoginReq.Builder loginReq = Authentication.LoginReq.newBuilder();
            loginReq.setUuid(session.getDeviceId());
            loginReq.setLoginType(LoginType.LOGIN_GUEST);
            loginReq.setAppVersion("0.0.1");
            loginReq.setAppStore("NONE");
            loginReq.setDeviceModel("PC");
            loginReq.setDeviceCountry("KR");
            loginReq.setDeviceLanguage("ko");
            LoginResult loginResult = user.login(GameConstants.SPACE_USER_TYPE, "", loginReq);
            assertTrue(loginResult.isSuccess());

            // Test 단계에서 활용하도록 준비합니다.
            users.add(user);
        }
    }

    @After
    public void tearDown() throws TimeoutException {
        for (ConnectorUser user : users) {
            user.logout();
            user.getSession().disconnect();
        }
    }

    //-------------------------------------------------------------------------------------

    @Test
    public void login() throws TimeoutException {
        // 기본 로그인
    }

    @Test
    public void deckShuffle() throws TimeoutException {
        // 게임룸에 없을때 유저를 통해서 요청 응답 처리
        ConnectorUser gameUser = users.get(0);

        User.ShuffleDeckReq.Builder shuffleDeckReq = User.ShuffleDeckReq.newBuilder();
        shuffleDeckReq.setCurrencyType(CurrencyType.CURRENCY_COIN);
        shuffleDeckReq.setUsage(1);

        User.ShuffleDeckRes shuffleDeckRes = gameUser.requestProto(shuffleDeckReq, User.ShuffleDeckRes.class);
        assertTrue(shuffleDeckRes.getResultCode() == ErrorCode.NONE);
    }

    @Test
    public void singleGamePlay() throws TimeoutException {
        ConnectorUser gameUser = users.get(0);

        // 싱글게임 입장
        GameSingle.StartGameReq.Builder startGameReq = GameSingle.StartGameReq.newBuilder();
        startGameReq.setDeck("sushi");
        startGameReq.setDifficulty(DifficultyType.DIFFICULTY_NORMAL);
        CreateRoomResult createRoomResult = gameUser.createRoom(GameConstants.SPACE_ROOM_TYPE_SINGLE, startGameReq);
        assertTrue(createRoomResult.isSuccess());

        // 게임 플레이 탭한 정보 전달
        TapMsg.Builder tapMsg = TapMsg.newBuilder();
        for (int i = 1; i < 5; i++) {
            tapMsg.setSelectCardName("sushi_0" + i);
            tapMsg.setCombo(i);
            tapMsg.setTapScore(100 * i);
            gameUser.send(tapMsg);
        }

        // 게임 방나가기
        GameSingle.EndGameReq.Builder endGameReq = GameSingle.EndGameReq.newBuilder();
        endGameReq.setEndType(EndType.GAME_END_TIME_UP);
        LeaveRoomResult leaveRoomResult = gameUser.leaveRoom(endGameReq);
        assertTrue(leaveRoomResult.isSuccess());
    }

    @Test
    public void UnlimitedTapGamePlay() throws TimeoutException {
        List<ConnectorUser> members = new ArrayList<>();
        int score = 0;
        for (ConnectorUser user : users) {
            score++;
            MatchRoomResult matchRoomResult = user.matchRoom(GameConstants.SPACE_ROOM_TYPE_MULTI_ROOM_MATCH);
            assertEquals(ResultCodeMatchRoom.MATCH_ROOM_SUCCESS, ResultCodeMatchRoom.forNumber(matchRoomResult.getResultCode()));
            members.add(user);

            GameMulti.ScoreUpMsg.Builder scoreUpMsg = GameMulti.ScoreUpMsg.newBuilder();
            scoreUpMsg.setScore(score);
            user.send(scoreUpMsg);

            for (ConnectorUser member : members) {
                GameMulti.BroadcastTapBirdMsg message = member.waitProtoPacketByFirstReceived(1, TimeUnit.SECONDS, GameMulti.BroadcastTapBirdMsg.class);
                assertTrue(message != null);
                for (TapBirdUserData userData : message.getTapBirdDataList()) {
                    assertFalse(userData.getUserData().getId().isEmpty());
                }
            }
        }
    }

    @Test
    public void SnakeGamePlay() throws TimeoutException {
        List<ConnectorUser> members = users.subList(0, 2);

        int score = 0;
        for (ConnectorUser user : members) {
            score++;
            MatchUserStartResult matchUserStartResult1 = user.matchUserStart(GameConstants.SPACE_ROOM_TYPE_MULTI_USER_MATCH);
            assertTrue(matchUserStartResult1.isSuccess());
        }

        for (ConnectorUser user : members) {
            user.waitProtoPacket(1, TimeUnit.SECONDS, Base.MatchUserDone.class);
        }

        for (ConnectorUser member : members) {
            GameMulti.SnakeFoodMsg message = member.waitProtoPacketByFirstReceived(2, TimeUnit.SECONDS, GameMulti.SnakeFoodMsg.class);
            assertTrue(message != null);
            assertFalse(message.getFoodData().getIdx() < 0);
        }

        for (ConnectorUser user : members) {
            score++;
            GameMulti.RoomUserData.Builder roomUserData = GameMulti.RoomUserData.newBuilder();
            roomUserData.setScore(score);
            roomUserData.setId(user.getUserId());

            GameMulti.SnakeUserData.Builder snakeUserData = GameMulti.SnakeUserData.newBuilder();
            snakeUserData.setBaseData(roomUserData);

            for (int i = 1; i < 3; i++) {
                GameMulti.SnakePositionData.Builder snakePositionData = GameMulti.SnakePositionData.newBuilder();
                snakePositionData.setIdx(i);
                snakePositionData.setX(i + 10);
                snakePositionData.setY(i + 20);
                snakeUserData.addUserPositionListData(snakePositionData);
            }

            GameMulti.SnakeUserMsg.Builder snakeUserMsg = GameMulti.SnakeUserMsg.newBuilder();
            snakeUserMsg.setUserData(snakeUserData);
            user.send(snakeUserMsg);
        }

        for (ConnectorUser member : members) {
            GameMulti.SnakeUserMsg message = member.waitProtoPacketByFirstReceived(1, TimeUnit.SECONDS, GameMulti.SnakeUserMsg.class);
            assertTrue(message != null);
            assertFalse(message.getUserData().getBaseData().getId().isEmpty());
        }
    }
}
