package stress._handler;

import static org.junit.Assert.assertEquals;

import com.nhn.gameflex.sample.common.GameConstants;
import com.nhn.gameflex.sample.protocol.GameSingle;
import com.nhn.gameflex.sample.protocol.GameSingle.DifficultyType;
import com.nhn.gameflexcore.connector.callback.parent.IDispatchPacket;
import com.nhn.gameflexcore.connector.protocol.Packet;
import com.nhn.gameflexcore.connector.protocol.result.LoginResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stress.SampleUserClass;

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
        user.createRoom(GameConstants.GAME_ROOM_TYPE_SINGLE, startGameReq);
    }

}
