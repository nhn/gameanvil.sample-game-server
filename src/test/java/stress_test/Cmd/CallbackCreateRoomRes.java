package stress_test.Cmd;

import static org.junit.Assert.assertTrue;

import com.nhn.tardis.taptap.protocol.GameSingle;
import com.nhn.tardis.taptap.protocol.GameSingle.EndType;
import com.nhn.tardis.taptap.protocol.GameSingle.TapMsg;
import com.nhnent.tardis.connector.callback.parent.IDispatchPacket;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.protocol.result.CreateRoomResult;
import stress_test.SampleUserClass;

public class CallbackCreateRoomRes implements IDispatchPacket<SampleUserClass> {

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {
        CreateRoomResult createRoomResult = user.parseCreateRoomResult(packet);
        assertTrue(createRoomResult.isSuccess());

        // 게임 플레이 탭한 정보 전달
        TapMsg.Builder tapMsg = TapMsg.newBuilder();
        for (int i = 1; i < 5; i++) {
            tapMsg.setSelectCardName("sushi_0" + i);
            tapMsg.setCombo(i);
            tapMsg.setTapScore(100 * i);
            user.send(tapMsg);
        }

        // 게임 방나가기
        GameSingle.EndGameReq.Builder endGameReq = GameSingle.EndGameReq.newBuilder();
        endGameReq.setEndType(EndType.GAME_END_TIME_UP);
        user.leaveRoom(endGameReq);
    }
}
