package com.nhn.gameanvil.sample.gateway;

import co.paralleluniverse.fibers.SuspendExecution;
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
        if (logger.isDebugEnabled()) {
            logger.debug("onInit");
        }
    }

    @Override
    public void onPrepare() throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onPrepare");
        }
        setReady();
    }

    @Override
    public void onReady() throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onReady");
        }
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onDispatch : {}", packet.getMsgName());
        }
    }

    @Override
    public void onPause(Payload payload) throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onPause");
        }
    }

    @Override
    public void onResume(Payload payload) throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onResume");
        }
        setReady();
    }

    @Override
    public void onShutdown() throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onShutdown");
        }
    }

    @Override
    public void onTimer(Timer timerObject, Object arg) throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onTimer - message : {}", arg);
        }
    }
}
