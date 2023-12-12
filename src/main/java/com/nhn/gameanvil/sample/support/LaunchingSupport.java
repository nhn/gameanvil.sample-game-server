package com.nhn.gameanvil.sample.support;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.annotation.ServiceName;
import com.nhn.gameanvil.node.BaseObject;
import com.nhn.gameanvil.node.support.BaseSupportNode;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.packet.message.MessageDispatcher;
import com.nhn.gameanvil.packet.message.RestMessageDispatcher;
import com.nhn.gameanvil.rest.RestObject;
import com.nhn.gameanvil.sample.common.GameConstants;
import com.nhn.gameanvil.sample.support._handler.rest._Launching;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * GameAnvil 서버 접속 전에 rest 로 접속 할수 있는 서비스
 */
@ServiceName(GameConstants.SUPPORT_NAME_LAUNCHING)  // support 노드 이름 지정
public class LaunchingSupport extends BaseSupportNode {
    private static final Logger logger = getLogger(LaunchingSupport.class);
    private static final RestMessageDispatcher<LaunchingSupport> restMsgHandler = new RestMessageDispatcher<>();

    static {
        // launching
        restMsgHandler.registerMsg("/launching", RestObject.GET, _Launching.class);
    }

    @Override
    public MessageDispatcher<? extends BaseObject> getMessageDispatcher() {
        return null;
    }

    @Override
    public RestMessageDispatcher<LaunchingSupport> getRestMessageDispatcher() {
        return restMsgHandler;
    }

    @Override
    public void onInit() {
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
    public void onShutdown() throws SuspendExecution {
        logger.info("onShutdown");
    }

    @Override
    public void onResume(final Payload payload) throws SuspendExecution {
        logger.info("onResume");
    }
}
