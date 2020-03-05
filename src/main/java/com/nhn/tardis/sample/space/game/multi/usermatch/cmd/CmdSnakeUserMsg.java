package com.nhn.tardis.sample.space.game.multi.usermatch.cmd;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.tardis.sample.protocol.GameMulti;
import com.nhn.tardis.sample.protocol.GameMulti.SnakePositionData;
import com.nhn.tardis.sample.space.game.multi.usermatch.SnakeRoom;
import com.nhn.tardis.sample.space.game.multi.usermatch.model.SnakePositionInfo;
import com.nhn.tardis.sample.space.user.GameUser;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.space.IRoomPacketHandler;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * snake 게임에서 유저의 위치 변화 데이터 전송, 상대방에게 전달
 */
public class CmdSnakeUserMsg implements IRoomPacketHandler<SnakeRoom, GameUser> {
    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public void execute(SnakeRoom room, GameUser user, Packet packet) throws SuspendExecution {
        try {
            GameMulti.SnakeUserMsg snakeUserMsg = GameMulti.SnakeUserMsg.parseFrom(packet.getStream());
            if (snakeUserMsg != null) {
                logger.info("SnakeUserMsg  : {} : {}", user.getUserId(), snakeUserMsg);
                // 스코어 저장
                room.getGameUserScoreMap().put(user.getGameUserInfo().getUuid(), (int)snakeUserMsg.getUserData().getBaseData().getScore());

                // 유저 위치 정보 지정
                List<SnakePositionData> snakePositionDataList = snakeUserMsg.getUserData().getUserPositionListDataList();
                if (snakePositionDataList != null) {
                    room.getGameUserMap().get(user.getUserId()).getSnakePositionInfoList().clear();
                    for (SnakePositionData snakePositionData : snakePositionDataList) {
                        SnakePositionInfo snakePositionInfo = new SnakePositionInfo(snakePositionData.getIdx(), snakePositionData.getX(), snakePositionData.getY());
                        room.getGameUserMap().get(user.getUserId()).getSnakePositionInfoList().add(snakePositionInfo);
                    }

                    // 방에있는 유저들에게 메세지 전송.
                    for (GameUser gameUser : room.getGameUserMap().values()) {
                        if (gameUser.getUserId().equals(user.getUserId())) {   // 나에게는 전송하지많음
                            continue;
                        }
                        logger.info("snakeUserMsg relay : {} : {}", gameUser.getUserId(), snakeUserMsg);

                        gameUser.send(new Packet(snakeUserMsg));
                    }
                }
            } else {
                logger.error("SnakeUserMsg is null!!!");
            }
        } catch (Exception e) {
            logger.error("execute()", e);
        }
    }
}
