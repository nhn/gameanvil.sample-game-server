package com.nhn.tardis.taptap.service;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.tardis.taptap.service.cmd.CmdLaunching;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.common.internal.RestObject;
import com.nhnent.tardis.console.RestPacketDispatcher;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.service.IServiceNode;
import com.nhnent.tardis.console.service.ServiceNodeAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 타디스 서버 접속 전에 rest 로 접속 할수 있는 서비스
 */
public class LaunchingService extends ServiceNodeAgent implements IServiceNode {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static RestPacketDispatcher restMsgHandler = new RestPacketDispatcher();

    static {
        // launching
        restMsgHandler.registerMsg("/launching", RestObject.GET, CmdLaunching.class);
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
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("onDispatch : {}", packet.getMsgName());
    }

    @Override
    public boolean onDispatch(RestObject restObject) throws SuspendExecution {
        logger.info("onDispatch : {}", restObject.getOrginUrl());
        if (restMsgHandler.isRegisteredMessage(restObject)) {
            restMsgHandler.dispatch(this, restObject);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onPause(PauseType pauseType, Payload payload) throws SuspendExecution {
        logger.info("onPause : {}", pauseType);
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
