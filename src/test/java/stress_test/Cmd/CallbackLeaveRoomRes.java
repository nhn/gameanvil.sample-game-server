package stress_test.Cmd;

import static org.junit.Assert.assertTrue;

import com.nhnent.tardis.connector.callback.parent.IDispatchPacket;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.protocol.result.LeaveRoomResult;
import stress_test.SampleUserClass;

public class CallbackLeaveRoomRes implements IDispatchPacket<SampleUserClass> {

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {
        LeaveRoomResult leaveRoomResult = user.parseLeaveRoomResult(packet);
        assertTrue(leaveRoomResult.isSuccess());
        if (leaveRoomResult.isSuccess()) {
            user.logout();
        } else {
            user.leaveRoom();
        }
    }

}
