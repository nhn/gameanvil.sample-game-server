package stress._handler;

import com.nhn.gameflexcore.connector.callback.parent.IDispatchPacket;
import com.nhn.gameflexcore.connector.protocol.Packet;
import com.nhn.gameflexcore.connector.protocol.result.LogoutResult;
import stress.SampleUserClass;

import static org.junit.Assert.assertTrue;

public class CallbackLogout implements IDispatchPacket<SampleUserClass> {

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        LogoutResult result = user.parseLogoutResult(packet);
        assertTrue(result.isSuccess());

        // 만약, 시나리오의 마지막 단계라면 finish 처리 해 줍니다.
        user.finish();
    }
}
