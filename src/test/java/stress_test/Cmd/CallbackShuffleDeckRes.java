package stress_test.Cmd;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.nhn.tardis.sample.common.GameConstants;
import com.nhn.tardis.sample.protocol.GameSingle;
import com.nhn.tardis.sample.protocol.GameSingle.DifficultyType;
import com.nhn.tardis.sample.protocol.Result.ErrorCode;
import com.nhn.tardis.sample.protocol.User;
import com.nhnent.tardis.connector.callback.parent.IDispatchPacket;
import com.nhnent.tardis.connector.protocol.Packet;
import java.io.IOException;
import stress_test.SampleUserClass;

public class CallbackShuffleDeckRes implements IDispatchPacket<SampleUserClass> {

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        try {
            User.ShuffleDeckRes shuffleDeckRes = User.ShuffleDeckRes.parseFrom(packet.getStream());
            assertTrue(shuffleDeckRes.getResultCode() == ErrorCode.NONE);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }

        GameSingle.StartGameReq.Builder startGameReq = GameSingle.StartGameReq.newBuilder();
        startGameReq.setDeck("sushi");
        startGameReq.setDifficulty(DifficultyType.DIFFICULTY_NORMAL);
        user.createRoom(GameConstants.SPACE_ROOM_TYPE_SINGLE, startGameReq);
    }
}
