package com.nhn.gameanvil.sample.gateway;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.annotation.Connection;
import com.nhn.gameanvil.node.gateway.BaseConnection;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.packet.message.MessageDispatcher;
import com.nhn.gameanvil.sample.protocol.Authentication;
import com.nhn.gameanvil.sample.protocol.Result;
import com.nhn.gameanvil.sample.protocol.Result.ErrorCode;
import org.slf4j.Logger;

/**
 * 게임 커넥션
 */
@Connection()
public class GameConnection extends BaseConnection<GameSession> {

    private static final Logger logger = getLogger(GameConnection.class);
    private static final MessageDispatcher<GameConnection> packetDispatcher = new MessageDispatcher<>();

    static {
        // 패킷 핸들러 등록 위치
    }

    @Override
    public MessageDispatcher<GameConnection> getMessageDispatcher() {
        return packetDispatcher;
    }

    /**
     * 인증 처리 요청
     *
     * @param accountId  계정 아이디
     * @param password   패스워드
     * @param deviceId   디바이스 아이디
     * @param payload    인증 요청시 전달된 정보
     * @param outPayload 인증 요청 응답시 전달 할 정보
     * @return 인증 성공여부 반환
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
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

            // payload 로부터 인증요청 Packet 가져오기.
            Authentication.AuthenticationReq authenticationReq = payload.getProtoBuffer(Authentication.AuthenticationReq.getDescriptor());
            if (authenticationReq == null) { // 인증을 검증하기 위한 패킷이 없는경우
                resultCode = ErrorCode.PARAMETER_IS_EMPTY;
                logger.error("GameConnection::onAuthenticate() fail!! authenticatePacket is null!!");
            } else {
                // 인증 검증할 토큰을 가지고 검증처리
                if (authenticationReq.getAccessToken() == null) {
                    resultCode = ErrorCode.TOKEN_IS_EMPTY;
                    logger.error("GameConnection::onAuthenticate() fail!! authenticationReq.getAccessToken() is empty!!");
                } else {
                    logger.info("onAuthenticate Success. token:{}", authenticationReq.getAccessToken());

                    if (authenticationReq.getAccessToken().startsWith("TapTap_AccessToken")) {
                        // 플랫폼 테스트용 토큰 - 검증없이 정상 처리
                        resultCode = ErrorCode.NONE;
                    } else {
                        logger.error("GameConnection::onAuthenticate() fail!! AccessToken Error!! {}", authenticationReq.getAccessToken());
                    }
                }
            }
        } else {
            logger.error("GameConnection::onAuthenticate() fail. password must be same as accountId. id:{}, pw:{}, device:{}",
                accountId, password, deviceId);
            resultCode = ErrorCode.PASSWORD_NOT_MATCHED;
        }

        // 클라이언트로 응답 패킷 전달
        Authentication.AuthenticationRes.Builder authenticationRes = Authentication.AuthenticationRes.newBuilder();
        authenticationRes.setErrorCode(resultCode);
        outPayload.add(authenticationRes);
        if (resultCode == ErrorCode.NONE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onPause() throws SuspendExecution {
        logger.info("onPause : {}", getAccountId());
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
