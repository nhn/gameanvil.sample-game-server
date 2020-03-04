package com.nhn.tardis.taptap.session;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.tardis.taptap.protocol.Authentication;
import com.nhn.tardis.taptap.protocol.Result;
import com.nhn.tardis.taptap.protocol.Result.ErrorCode;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.console.PacketDispatcher;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.session.ISession;
import com.nhnent.tardis.console.session.SessionAgent;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 인증 처리
 */
public class TapTapSession extends SessionAgent implements ISession<TapTapSessionUser> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static PacketDispatcher packetDispatcher = new PacketDispatcher();

    static {
        // 패킷 핸들러 등록 위치
    }

    @Override
    public boolean onAuthenticate(String accountId, String password, String deviceId,
        Payload payload, Payload outPayload) throws SuspendExecution {
        // 클라이언트로 무터 전달 받은 데이터 출력
        logger.info(
            "onAuthenticate - accountId : {}, password : {}, deviceId : {}",
            accountId, password, deviceId);

        // 응답 코드 기본값 지정
        Result.ErrorCode resultCode = ErrorCode.UNKNOWN;

        // 인증 로직 구현부. 여기서는 accountId와 password가 일치 할 경우 인증 성공.
        if (accountId.equals(password)) {
            logger.info("onAuthenticate. id:{}, pw:{}, device:{}", accountId, password,
                deviceId);

            // payload 로부터 인증요청 Packet 가져오기.
            Packet authenticatePacket = payload.getPacket(Authentication.AuthenticationReq.getDescriptor());
            if (authenticatePacket == null) { // 인증을 검증하기 위한 패킷이 없는경우
                resultCode = ErrorCode.PARAMETER_IS_EMPTY;
                logger.error("onAuthenticate fail!! authenticatePacket is null!!");
            } else {
                try {
                    // 인증 검증할 토큰을 가지고 검증처리
                    Authentication.AuthenticationReq authenticationReq = Authentication.AuthenticationReq.parseFrom(authenticatePacket.getStream());
                    if (authenticationReq == null) {
                        resultCode = ErrorCode.PARAMETER_IS_EMPTY;
                        logger.error("onAuthenticate fail!! authenticationReq is null!!");
                    } else if (authenticationReq.getAccessToken() == null) {
                        resultCode = ErrorCode.TOKEN_IS_EMPTY;
                        logger.error("onAuthenticate fail!! authenticationReq.getAccessToken() is empty!!");
                    } else {
                        logger.info("onAuthenticate Success. token:{}", authenticationReq.getAccessToken());

                        //----------------------------------- 토큰 유효한지에 대한 처리가 필요

                        resultCode = ErrorCode.NONE;
                    }
                } catch (IOException e) {
                    logger.error("onAuthenticate fail!! AuthenticationReq.parseFrom ERROR {}", e);
                    e.printStackTrace();
                }
            }
        } else {
            logger.error(
                "onAuthenticate fail. password must be same as accountId. id:{}, pw:{}, device:{}",
                accountId, password, deviceId);
            resultCode = ErrorCode.PASSWORD_NOT_MATCHED;
        }

        // 클라이언트로 응답 패킷 전달
        Authentication.AuthenticationRes.Builder authenticationRes = Authentication.AuthenticationRes.newBuilder();
        authenticationRes.setErrorCode(resultCode);
        outPayload.add(new Packet(authenticationRes));
        if (resultCode == ErrorCode.NONE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("onDispatch : {}", packet.getMsgName());
        packetDispatcher.dispatch(this, packet);
    }

    @Override
    public void onPreLogin(Payload outPayload) throws SuspendExecution {
        logger.info("onPreLogin");
    }

    @Override
    public void onPostLogin(TapTapSessionUser session) throws SuspendExecution {
        logger.info("onPostLogin : {}", session.getUserId());
    }

    @Override
    public void onPostLogout(TapTapSessionUser session) throws SuspendExecution {
        logger.info("onPostLogout : {}", session.getUserId());
    }

    @Override
    public void onPause(PauseType type) throws SuspendExecution {
        logger.info("onPause : {}", type);
    }

    @Override
    public void onResume() throws SuspendExecution {
        logger.info("onResume");
    }

    @Override
    public void onDisconnect() throws SuspendExecution {
        logger.info("onDisconnect");
    }
}
