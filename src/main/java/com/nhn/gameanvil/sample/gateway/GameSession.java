package com.nhn.gameanvil.sample.gateway;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.annotation.Session;
import com.nhn.gameanvil.node.gateway.BaseSession;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketDispatcher;
import com.nhn.gameanvil.packet.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 게임 세션
 */
@Session
public class GameSession extends BaseSession {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private static PacketDispatcher packetDispatcher = new PacketDispatcher();

    static {
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("onDispatch : {}", packet.getMsgName());

        packetDispatcher.dispatch(this, packet);
    }

    @Override
    public void onPreLogin(Payload outPayload) throws SuspendExecution {
        logger.info("onPreLogin {}", getAccountId());
    }

    @Override
    public void onPostLogin() throws SuspendExecution {
        logger.info("onPostLogin {}", getAccountId());
    }

    @Override
    public void onPostLogout() throws SuspendExecution {
        logger.info("onPostLogout {}", getAccountId());
    }
}
