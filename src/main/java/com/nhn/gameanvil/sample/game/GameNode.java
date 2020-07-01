package com.nhn.gameanvil.sample.game;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.sample.redis.RedisHelper;
import com.nhn.gameanvil.sample.common.GameConstants;
import com.nhn.gameanvil.define.PauseType;
import com.nhn.gameanvil.node.game.BaseGameNode;
import com.nhn.gameanvil.node.game.data.ChannelUpdateType;
import com.nhn.gameanvil.node.game.data.ChannelUserInfo;
import com.nhn.gameanvil.node.game.data.RoomInfo;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameNode extends BaseGameNode {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private RedisHelper redisHelper;

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("onInit");

        // FIXME RedisClient 노드마다 생성, singleton 으로 만들어서 접근 하면 안된다.
        // 레디스 생성
        redisHelper = new RedisHelper();
        // 레디스 연결 처리
        redisHelper.connect(GameConstants.REDIS_URL, GameConstants.REDIS_PORT);
    }

    @Override
    public void onPrepare() throws SuspendExecution {
        setReady();
        logger.info("onPrepare");
    }

    @Override
    public void onReady() throws SuspendExecution {
        logger.info("onReady");
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("onDispatch");
    }

    @Override
    public void onPause(PauseType type, Payload payload) throws SuspendExecution {
        logger.info("onPause");
    }

    @Override
    public void onResume(Payload payload) throws SuspendExecution {
        logger.info("onResume");
    }

    @Override
    public void onShutdown() throws SuspendExecution {
        logger.info("onShutdown");

        redisHelper.shutdown();
    }

    @Override
    public void onChannelUserUpdate(ChannelUpdateType type, ChannelUserInfo channelUserInfo, int userId, String accountId) throws SuspendExecution {
        logger.info("onChannelUserUpdate");
    }

    @Override
    public void onChannelRoomUpdate(ChannelUpdateType type, RoomInfo channelRoomInfo, int roomId) throws SuspendExecution {
        logger.info("onChannelRoomUpdate");
    }

    @Override
    public void onChannelInfo(Payload outPayload) throws SuspendExecution {
        logger.info("onChannelInfo");
    }

    public RedisHelper getRedisHelper() {
        return redisHelper;
    }
}
