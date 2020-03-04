package stress_test.Cmd;

import static org.junit.Assert.assertEquals;

import com.nhn.tardis.taptap.common.GameConstants;
import com.nhn.tardis.taptap.protocol.GameSingle;
import com.nhn.tardis.taptap.protocol.GameSingle.DifficultyType;
import com.nhnent.tardis.connector.callback.parent.IDispatchPacket;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.protocol.result.LoginResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stress_test.SampleUserClass;

public class CallbackLoginRes implements IDispatchPacket<SampleUserClass> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        LoginResult result = user.parseLoginResult(packet);
        assertEquals(true, result.isSuccess());

        user.setSendCount(0);

        GameSingle.StartGameReq.Builder startGameReq = GameSingle.StartGameReq.newBuilder();
        startGameReq.setDeck("sushi");
        startGameReq.setDifficulty(DifficultyType.DIFFICULTY_NORMAL);
        user.createRoom(GameConstants.SPACE_ROOM_TYPE_SINGLE, startGameReq);
    }

}
