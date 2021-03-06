package com.nhn.gameanvil.sample.game.multi.roommatch._handler;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.node.game.RoomPacketHandler;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.sample.game.multi.roommatch.UnlimitedTapRoom;
import com.nhn.gameanvil.sample.game.user.GameUser;
import com.nhn.gameanvil.sample.protocol.GameMulti;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 룸안에서 유저가 점수 획득했을때 전송, send 로 받아서 처리후 모든 유저에게 send 로 broadcast 처리
 */
public class _ScoreUpMsg implements RoomPacketHandler<UnlimitedTapRoom, GameUser> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(UnlimitedTapRoom room, GameUser user, Packet packet) throws SuspendExecution {
        try {
            GameMulti.ScoreUpMsg scoreUpMsg = GameMulti.ScoreUpMsg.parseFrom(packet.getStream());
            if (scoreUpMsg != null) {
                logger.info("ScoreUpMsg userId {} : {}", user.getUserId(), scoreUpMsg);

                // 전달 받은 스코어 저장
                room.getGameUserScoreMap().put(user.getGameUserInfo().getUuid(), (int)scoreUpMsg.getScore());

                // 전달 메세지 제작
                GameMulti.BroadcastTapBirdMsg broadcastMsg = room.getBroadcastMsgByProto();

                // 방에있는 유저들에게 메세지 전송.
                for (GameUser gameUser : room.getGameUserMap().values()) {
                    logger.info("BroadcastTapBirdMsg userId {} : {}", gameUser.getUserId(), broadcastMsg);
                    gameUser.send(new Packet(broadcastMsg));
                }
            } else {
                logger.error("_ScoreUpMsg::execute() tapMsg is null!!!");
            }
        } catch (IOException e) {
            logger.error("_ScoreUpMsg::execute()", e);
        }
    }
}
