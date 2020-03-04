package com.nhn.tardis.sample.session;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.ITimerHandler;
import com.nhnent.tardis.common.internal.ITimerObject;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.console.session.ISessionNode;
import com.nhnent.tardis.console.session.SessionNodeAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 세션과 세션 유저 관리 처리
 */
public class GameSessionNode extends SessionNodeAgent implements ISessionNode, ITimerHandler {

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
    public void onTimer(ITimerObject timerObject, Object arg) throws SuspendExecution {
        logger.info("onTimer - message : {}", arg);
    }
}
