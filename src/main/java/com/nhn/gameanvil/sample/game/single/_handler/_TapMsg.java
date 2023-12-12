package com.nhn.gameanvil.sample.game.single._handler;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.node.game.RoomMessageHandler;
import com.nhn.gameanvil.sample.game.single.SingleGameRoom;
import com.nhn.gameanvil.sample.game.user.GameUser;
import com.nhn.gameanvil.sample.protocol.GameSingle;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 유저가 점수를 얻었을때 서버로 전송 하는 프로토콜, send 형태로 서버로 수신되어 별도의 응답 처리 없음
 */
public class _TapMsg implements RoomMessageHandler<SingleGameRoom, GameUser, GameSingle.TapMsg> {
    private static final Logger logger = getLogger(_TapMsg.class);

    @Override
    public void execute(SingleGameRoom singleRoom, GameUser gameUser, GameSingle.TapMsg tapMsg) throws SuspendExecution {
        // 유저가 탭한 정보
        if (tapMsg != null) {
            singleRoom.getSingleGameData().setCombo(tapMsg.getCombo());
            singleRoom.getSingleGameData().addScore();
            logger.info("TapMsg  : {}, score {}", tapMsg, singleRoom.getSingleGameData().getScore());
        } else {
            logger.error("_TapMsg::execute() tapMsg is null");
        }
    }
}
