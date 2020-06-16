package com.nhn.tardis.sample.session;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.node.gateway.BaseSession;
import com.nhnent.tardis.packet.Packet;
import com.nhnent.tardis.packet.PacketDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 세션유저
 */
public class GameSessionUser extends BaseSession {

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
