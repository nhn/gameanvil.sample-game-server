package stress_test;

import com.nhn.tardis.sample.common.GameConstants;
import com.nhn.tardis.sample.protocol.Authentication;
import com.nhn.tardis.sample.protocol.GameMulti;
import com.nhn.tardis.sample.protocol.GameSingle;
import com.nhn.tardis.sample.protocol.Result;
import com.nhn.tardis.sample.protocol.User;
import com.nhnent.tardis.connector.common.Config;
import com.nhnent.tardis.connector.tcp.ConnectorSession;
import com.nhnent.tardis.connector.tcp.TardisConnector;
import com.nhnent.tardis.connector.tcp.agent.parent.IAsyncConnectorUser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stress_test.Cmd.CallbackAuthenticationRes;
import stress_test.Cmd.CallbackCreateRoomRes;
import stress_test.Cmd.CallbackLeaveRoomRes;
import stress_test.Cmd.CallbackLoginRes;
import stress_test.Cmd.CallbackLogout;
import stress_test.Cmd.CallbackShuffleDeckRes;
import stress_test.Cmd.SampleTimeout;

public class Stress {
    private static TardisConnector connector;
    private Logger logger = LoggerFactory.getLogger(getClass());

    //-------------------------------------------------------------------------------------

    @BeforeClass
    public static void configuration() {

        // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
//        Config.addRemoteInfo("10.77.35.47", 11200);
        Config.addRemoteInfo("127.0.0.1", 11200);

        // 패킷 수신에 대한 타임아웃 시간을 지정합니다. (밀리초)
        Config.WAIT_RECV_TIMEOUT_MSEC = 5000; // [default 3000]

        // 커넥터의 run 매서드에 대한 강제종료 시간을 설정합니다. (초)
        Config.FORCE_EXIT_TIMEOUT_SEC = 300; // [default 300]

        // Ping 주기를 설정합니다. (밀리초)
        Config.PING_INTERVAL_MSEC = 3000; // [default 3000]

        Config.CONCURRENT_USER = 10;

        // 부하 테스트 시작시, Bot 유저들에 딜레이를 두고 런칭 시킬 수 있습니다.
        //Config.RAMP_UP_DELAY_MSEC = 5; // [default 0]

        // 커넥터를 생성합니다.
        connector = TardisConnector.getInstance();

        // 컨텐츠 프로토콜 등록.
        connector.addProtoBufClass(0, Authentication.class);
        connector.addProtoBufClass(1, GameMulti.class);
        connector.addProtoBufClass(2, GameSingle.class);
        connector.addProtoBufClass(3, Result.class);
        connector.addProtoBufClass(4, User.class);

        // 컨텐츠 서비스 등록.
        connector.addService(0, GameConstants.GAME_SPACE_NAME);

        // 콜백 목록을 등록합니다.
        connector.addPacketCallbackAuthentication(new CallbackAuthenticationRes());
        connector.addPacketCallbackLogin(new CallbackLoginRes());
        connector.addPacketCallback(User.ShuffleDeckRes.class, new CallbackShuffleDeckRes(), 10, TimeUnit.MILLISECONDS); // 해당 콜백을 딜레이 시켜서 호출하고자 할 경우 파라미터로 옵션값을 지정할 수 있습니다.
        connector.addPacketCallbackCreateRoom(new CallbackCreateRoomRes());
        connector.addPacketCallbackLeaveRoom(new CallbackLeaveRoomRes(), 10); // 해당 콜백을 딜레이 시켜서 호출하고자 할 경우 파라미터로 옵션값을 지정할 수 있습니다.
        connector.addPacketCallbackLogout(new CallbackLogout());

        connector.addPacketTimeoutCallback(new SampleTimeout());
    }

    //-------------------------------------------------------------------------------------

    @Test
    public void runMultiUser() throws TimeoutException {

        for (int i = 0; i < Config.CONCURRENT_USER; ++i) {

            // 커넥션을 생성하고 세션 정보가 담긴 객체를 리턴 받습니다.

            ConnectorSession session = connector.addSession(connector.getHostIncrementedValue("account"), connector.makeUniqueId());

            // 세션에 대해 유저를 등록하고, 각종 ID 정보가 담긴 유저객체를 리턴 받습니다.
            // 하나의 세션에 여러 유저를 등록할 수 있습니다.
            Authentication.AuthenticationReq.Builder authenticationReq = Authentication.AuthenticationReq.newBuilder().setAccessToken("TapTap_accessToken!!!!");

            // 인증을 진행.
            session.authentication(session.getAccountId(), authenticationReq);

            // 여기서는 커스텀 클래스를 지정하여, 등록한 콜백에서 쉽게 활용할 수 있도록 합니다.
            SampleUserClass sampleUser = session.addUser(GameConstants.GAME_SPACE_NAME, SampleUserClass.class);
        }

        //connector.repeatByEntire(/* ... */);
        connector.repeatByIndividual(new TardisConnector.InitialProtocol() {
            @Override
            public void send(IAsyncConnectorUser iUser) {
                Authentication.AuthenticationReq.Builder authenticationReq = Authentication.AuthenticationReq.newBuilder().setAccessToken("TapTap_accessToken!!!!");
                iUser.authentication(iUser.getAccountId(), authenticationReq);
            }
        }, 3);
    }

}
