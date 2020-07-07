package com.nhn.gameanvil.sample.gateway;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.define.PauseType;
import com.nhn.gameanvil.node.gateway.BaseGatewayNode;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.timer.Timer;
import com.nhn.gameanvil.timer.TimerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 세션과 세션 유저 관리 처리
 */
public class GameGatewayNode extends BaseGatewayNode implements TimerHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("onInit");
    }

    @Override
    public void onPrepare() throws SuspendExecution {
        logger.info("onPrepare");
        setReady();
    }

    @Override
    public void onReady() throws SuspendExecution {
        logger.info("onReady");
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("onDispatch : {}", packet.getMsgName());
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
    }

    @Override
    public void onTimer(Timer timerObject, Object arg) throws SuspendExecution {
        logger.info("onTimer - message : {}", arg);
    }
}
