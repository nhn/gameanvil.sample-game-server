package com.nhn.tardis.taptap.space.game.multi.usermatch.cmd;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.tardis.taptap.protocol.GameMulti;
import com.nhn.tardis.taptap.protocol.GameMulti.SnakePositionData;
import com.nhn.tardis.taptap.space.game.multi.usermatch.SnakeRoom;
import com.nhn.tardis.taptap.space.game.multi.usermatch.model.SnakePositionInfo;
import com.nhn.tardis.taptap.space.user.GameUser;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.space.IRoomPacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * snake 게임에서 유저가 food 먹어서 삭제 했을때 상대방에게도 싱크 처리
 */
public class CmdSnakeRemoveFoodMsg implements IRoomPacketHandler<SnakeRoom, GameUser> {
    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public void execute(SnakeRoom room, GameUser user, Packet packet) throws SuspendExecution {
        try {
            GameMulti.SnakeFoodMsg snakeRemoveFoodMsg = GameMulti.SnakeFoodMsg.parseFrom(packet.getStream());
            if (snakeRemoveFoodMsg != null) {
                logger.info("snakeRemoveFoodMsg  : {} : {}", user.getUserId(), snakeRemoveFoodMsg);

                if (snakeRemoveFoodMsg.getIsDelete()) { // 삭제
                    // 삭제할 food 위치
                    SnakePositionData removeFoodData = snakeRemoveFoodMsg.getFoodData();
                    if (removeFoodData != null) {
                        for (SnakePositionInfo deleteFood : room.getFoodList())
                            if (deleteFood.getIdx() == removeFoodData.getIdx()) {
                                room.getFoodList().remove(deleteFood);
                                break;
                            }
                    }

                    // 방에있는 유저들에게 메세지 전송.
                    for (GameUser gameUser : room.getGameUserMap().values()) {
                        if (gameUser.getUserId().equals(user.getUserId())) {   // 나에게는 전송하지많음
                            continue;
                        }
                        logger.info("snakeRemoveFoodMsg relay : {} : {}", gameUser.getUserId(), snakeRemoveFoodMsg);

                        gameUser.send(new Packet(snakeRemoveFoodMsg));
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
