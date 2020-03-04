package com.nhn.tardis.sample.space;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.tardis.sample.common.GameConstants;
import com.nhn.tardis.sample.redis.RedisHelperService;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.console.space.ChannelUpdateType;
import com.nhnent.tardis.console.space.IChannelUserInfo;
import com.nhnent.tardis.console.space.IRoomInfo;
import com.nhnent.tardis.console.space.ISpaceNode;
import com.nhnent.tardis.console.space.SpaceNodeAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameNode extends SpaceNodeAgent implements ISpaceNode {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("onInit");

        // 레디스 연결 처리
        RedisHelperService.getInstance().connect(GameConstants.REDIS_URL, GameConstants.REDIS_PORT);
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

        RedisHelperService.getInstance().shutdown();
    }

    @Override
    public void onChannelUserUpdate(ChannelUpdateType type, IChannelUserInfo channelUserInfo, String userId) throws SuspendExecution {
        logger.info("onChannelUserUpdate");
    }

    @Override
    public void onChannelRoomUpdate(ChannelUpdateType type, IRoomInfo channelRoomInfo, String roomId) throws SuspendExecution {
        logger.info("onChannelRoomUpdate");
    }
}
