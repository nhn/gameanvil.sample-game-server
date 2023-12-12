package com.nhn.gameanvil.sample.gateway;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.annotation.GatewayNode;
import com.nhn.gameanvil.node.BaseObject;
import com.nhn.gameanvil.node.gateway.BaseGatewayNode;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.packet.message.MessageDispatcher;
import com.nhn.gameanvil.timer.Timer;
import com.nhn.gameanvil.timer.TimerHandler;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 게임 게이트웨이 노드
 */
@GatewayNode()
public class GameGatewayNode extends BaseGatewayNode implements TimerHandler {
    private static final Logger logger = getLogger(GameGatewayNode.class);

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("onInit");
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
    }

    @Override
    public void onTimer(Timer timerObject, Object arg) throws SuspendExecution {
        logger.info("onTimer - message : {}", arg);
    }

    @Override
    public MessageDispatcher<? extends BaseObject> getMessageDispatcher() {
        return null;
    }
}
