package com.nhn.gameanvil.sample.support;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.node.support.BaseSupportNode;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.packet.RestPacketDispatcher;
import com.nhn.gameanvil.rest.RestObject;
import com.nhn.gameanvil.sample.support._handler.rest._Launching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 타디스 서버 접속 전에 rest 로 접속 할수 있는 서비스
 */
public class LaunchingSupport extends BaseSupportNode {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static RestPacketDispatcher restMsgHandler = new RestPacketDispatcher();

    static {
        // launching
        restMsgHandler.registerMsg("/launching", RestObject.GET, _Launching.class);
    }

    @Override
    public void onInit() {
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
    public boolean onDispatch(RestObject restObject) throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onDispatch : {}", restObject.getOriginUrl());
        }
        if (restMsgHandler.isRegisteredMessage(restObject)) {
            restMsgHandler.dispatch(this, restObject);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onPause(Payload payload) throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onPause");
        }
    }

    @Override
    public void onShutdown() throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onShutdown");
        }
    }

    @Override
    public void onResume(final Payload payload) throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onResume");
        }
        setReady();
    }
}
