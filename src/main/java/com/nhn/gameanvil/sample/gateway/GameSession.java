package com.nhn.gameanvil.sample.gateway;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.annotation.Session;
import com.nhn.gameanvil.node.BaseObject;
import com.nhn.gameanvil.node.gateway.BaseSession;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.packet.message.MessageDispatcher;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 게임 세션
 */
@Session
public class GameSession extends BaseSession {

    private static final Logger logger = getLogger(GameSession.class);
    private static final MessageDispatcher<GameSession> packetDispatcher = new MessageDispatcher<>();

    static {
    }

    @Override
    public MessageDispatcher<GameSession> getMessageDispatcher() {
        return packetDispatcher;
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
