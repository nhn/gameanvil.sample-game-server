package com.nhn.gameflex.sample.game.single._handler;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameflex.sample.game.single.SingleGameRoom;
import com.nhn.gameflex.sample.game.user.GameUser;
import com.nhn.gameflex.sample.protocol.GameSingle;
import com.nhn.gameflex.node.game.RoomPacketHandler;
import com.nhn.gameflex.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 유저가 점수를 얻었을때 서버로 전송 하는 프로토콜, send 형태로 서버로 수신되어 별도의 응답 처리가 없다.
 */
public class _TapMsg implements RoomPacketHandler<SingleGameRoom, GameUser> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(SingleGameRoom singleRoom, GameUser gameUser, Packet packet) throws SuspendExecution {
        try {
            // 유저가 탭한 정보
            GameSingle.TapMsg tapMsg = GameSingle.TapMsg.parseFrom(packet.getStream());
            if (tapMsg != null) {
                singleRoom.getSingleGameData().setCombo(tapMsg.getCombo());
                singleRoom.getSingleGameData().addScore();
                logger.info("TapMsg  : {}, score {}", tapMsg, singleRoom.getSingleGameData().getScore());
            } else {
                logger.error("TapMsg tapMsg is null!!!");
            }
        } catch (Exception e) {
            logger.error("execute()", e);
        }
    }
}
