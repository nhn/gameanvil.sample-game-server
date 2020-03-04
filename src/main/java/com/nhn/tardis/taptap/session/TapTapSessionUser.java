package com.nhn.tardis.taptap.session;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.PacketDispatcher;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.session.ISessionUser;
import com.nhnent.tardis.console.session.SessionUserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 세션유저
 */
public class TapTapSessionUser extends SessionUserAgent implements ISessionUser {

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
