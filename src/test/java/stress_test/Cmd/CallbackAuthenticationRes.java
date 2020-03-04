package stress_test.Cmd;

import static org.junit.Assert.assertTrue;

import com.nhn.tardis.taptap.common.GameConstants;
import com.nhn.tardis.taptap.protocol.Authentication;
import com.nhn.tardis.taptap.protocol.Authentication.LoginType;
import com.nhnent.tardis.connector.callback.parent.IDispatchPacket;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.protocol.result.AuthenticationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stress_test.SampleUserClass;

public class CallbackAuthenticationRes implements IDispatchPacket<SampleUserClass> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {
        //응답확인
        AuthenticationResult result = user.parseAuthenticationResult(packet);
        assertTrue(result.isSuccess());

        Authentication.LoginReq.Builder loginReq = Authentication.LoginReq.newBuilder();
        loginReq.setUuid(user.getDeviceId());
        loginReq.setLoginType(LoginType.LOGIN_GUEST);
        loginReq.setAppVersion("0.0.1");
        loginReq.setAppStore("NONE");
        loginReq.setDeviceModel("PC");
        loginReq.setDeviceCountry("KR");
        loginReq.setDeviceLanguage("ko");
        user.login(GameConstants.SPACE_USER_TYPE, "", loginReq);
    }

}
