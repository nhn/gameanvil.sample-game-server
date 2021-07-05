package com.nhn.gameanvil.sample.game.multi.usermatch._handler;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.node.game.RoomPacketHandler;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.sample.game.multi.usermatch.SnakeRoom;
import com.nhn.gameanvil.sample.game.multi.usermatch.model.SnakePositionInfo;
import com.nhn.gameanvil.sample.game.user.GameUser;
import com.nhn.gameanvil.sample.protocol.GameMulti;
import com.nhn.gameanvil.sample.protocol.GameMulti.SnakePositionData;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * snake 게임에서 유저가 food를 먹어서 삭제 했을때 상대방에게도 처리된 내용 전달 처리
 */
public class _SnakeRemoveFoodMsg implements RoomPacketHandler<SnakeRoom, GameUser> {
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
                        if (gameUser.getUserId() == user.getUserId()) {   // 나에게는 전송하지많음
                            continue;
                        }
                        logger.info("snakeRemoveFoodMsg relay : {} : {}", gameUser.getUserId(), snakeRemoveFoodMsg);

                        gameUser.send(new Packet(snakeRemoveFoodMsg));
                    }
                }
            } else {
                logger.error("_SnakeRemoveFoodMsg::execute() SnakeUserMsg is null!!!");
            }
        } catch (IOException e) {
            logger.error("_SnakeRemoveFoodMsg::execute()", e);
        }
    }
}
