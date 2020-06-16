package com.nhn.tardis.sample.space.game.multi.roommatch.cmd;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.tardis.sample.protocol.GameMulti;
import com.nhn.tardis.sample.space.game.multi.roommatch.UnlimitedTapRoom;
import com.nhn.tardis.sample.space.user.GameUser;
import com.nhnent.tardis.node.game.RoomPacketHandler;
import com.nhnent.tardis.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 룸안에서 유저가 점수 획득했을때 전송, send 로 받아서 처리후 모든 유저에게 send 로 broadcast 처리
 */
public class CmdScoreUpMsg implements RoomPacketHandler<UnlimitedTapRoom, GameUser> {
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
                logger.error("tapMsg is null!!!");
            }
        } catch (Exception e) {
            logger.error("execute()", e);
        }
    }
}
