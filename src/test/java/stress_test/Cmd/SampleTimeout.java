package stress_test.Cmd;

import com.nhnent.tardis.connector.callback.parent.IDispatchPacketTimeout;
import com.nhnent.tardis.connector.tcp.agent.parent.IAsyncConnectorUser;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SampleTimeout implements IDispatchPacketTimeout {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void dispatch(String recvPacketName, IAsyncConnectorUser iUser) {

        System.out.println(simpleDateFormat.format(new Date()) + " Timeout: [" + recvPacketName + "], [" + iUser.getUserId() + "]");

        // 여기서는 시나리오를 처음부터 시작하도록, recv 패킷 종류 관계없이 finish 처리 해주고 있다.
        iUser.finish();

        // 그러나 이 경우, RPS 시나리오 초기 프로토콜(Auth REQ) 부터 다시 시작하게 되므로,
        // 만약 타임아웃이 발생한 경우가 매칭과 같이 서버내부로직에서 일정시간 다른패킷을 제대로 처리할 수 없는 경우라면,
        // finish 보다는 다른 적절한 분기로 진행하는것이 좋다.
    }

}
