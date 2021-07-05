package com.nhn.gameanvil.sample.game;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.annotation.ServiceName;
import com.nhn.gameanvil.node.game.BaseGameNode;
import com.nhn.gameanvil.node.game.data.BaseChannelRoomInfo;
import com.nhn.gameanvil.node.game.data.BaseChannelUserInfo;
import com.nhn.gameanvil.node.game.data.ChannelUpdateType;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.sample.common.GameConstants;
import com.nhn.gameanvil.sample.db.jasyncsql.JAsyncSqlManager;
import com.nhn.gameanvil.sample.redis.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 게임 노드
 */
@ServiceName(GameConstants.GAME_NAME)
public class GameNode extends BaseGameNode {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private RedisHelper redisHelper;
    private JAsyncSqlManager jAsyncSqlManager;

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("onInit");

        // FIXME RedisClient 노드마다 생성, singleton 으로 만들어서 접근 하면 안된다.
        // 레디스 생성
        redisHelper = new RedisHelper();
        // 레디스 연결 처리
        redisHelper.connect(GameConstants.REDIS_URL, GameConstants.REDIS_PORT);

        jAsyncSqlManager = new JAsyncSqlManager(GameConstants.DB_USERNAME, GameConstants.DB_HOST, GameConstants.DB_PORT, GameConstants.DB_PASSWORD, GameConstants.DB_DATABASE, GameConstants.MAX_ACTIVE_CONNECTION);
    }

    @Override
    public void onPrepare() throws SuspendExecution {
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
    public void onPause(Payload payload) throws SuspendExecution {
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
        jAsyncSqlManager.close();
    }

    @Override
    public boolean onNonStopPatchSrcStart() throws SuspendExecution {
        return true;
    }

    @Override
    public boolean onNonStopPatchSrcEnd() throws SuspendExecution {
        return true;
    }

    @Override
    public boolean canNonStopPatchSrcEnd() throws SuspendExecution {
        return true;
    }

    @Override
    public boolean onNonStopPatchDstStart() throws SuspendExecution {
        return true;
    }

    @Override
    public boolean onNonStopPatchDstEnd() throws SuspendExecution {
        return true;
    }

    @Override
    public boolean canNonStopPatchDstEnd() throws SuspendExecution {
        return true;
    }

    @Override
    public void onChannelUserInfoUpdate(ChannelUpdateType type, BaseChannelUserInfo channelUserInfo, int userId, String accountId) throws SuspendExecution {
        logger.info("onChannelUserInfoUpdate");
    }

    @Override
    public void onChannelRoomInfoUpdate(ChannelUpdateType type, BaseChannelRoomInfo channelRoomInfo, int roomId) throws SuspendExecution {
        logger.info("onChannelRoomInfoUpdate");
    }

    @Override
    public void onChannelInfo(Payload outPayload) throws SuspendExecution {
        logger.info("onChannelInfo");
    }

    public RedisHelper getRedisHelper() {
        return redisHelper;
    }

    public JAsyncSqlManager getJAsyncSqlManager() {
        return jAsyncSqlManager;
    }
}
