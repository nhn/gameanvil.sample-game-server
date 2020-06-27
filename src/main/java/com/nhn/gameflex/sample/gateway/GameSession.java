package com.nhn.gameflex.sample.gateway;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameflex.node.gateway.BaseSession;
import com.nhn.gameflex.packet.Packet;
import com.nhn.gameflex.packet.PacketDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 세션유저
 */
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
}
