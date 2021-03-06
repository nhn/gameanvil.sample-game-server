package com.nhn.gameanvil.sample.gateway;

import co.paralleluniverse.fibers.SuspendExecution;
import com.google.gson.JsonArray;
import com.nhn.gameanvil.GameAnvilUtil;
import com.nhn.gameanvil.annotation.Connection;
import com.nhn.gameanvil.async.http.HttpRequest;
import com.nhn.gameanvil.async.http.HttpResponse;
import com.nhn.gameanvil.node.gateway.BaseConnection;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketDispatcher;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.sample.common.GameConstants;
import com.nhn.gameanvil.sample.gamebase.rest.AuthenticationResponse;
import com.nhn.gameanvil.sample.protocol.Authentication;
import com.nhn.gameanvil.sample.protocol.Result;
import com.nhn.gameanvil.sample.protocol.Result.ErrorCode;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 게임 커넥션
 */
@Connection()
public class GameConnection extends BaseConnection<GameSession> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static PacketDispatcher packetDispatcher = new PacketDispatcher();

    static {
        // 패킷 핸들러 등록 위치
    }

    /**
     * 인증 처리 요청
     * @param accountId 계정 아이디
     * @param password 패스워드
     * @param deviceId 디바이스 아이디
     * @param payload 인증 요청시 전달된 정보
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
            Packet authenticatePacket = payload.getPacket(Authentication.AuthenticationReq.getDescriptor());
            if (authenticatePacket == null) { // 인증을 검증하기 위한 패킷이 없는경우
                resultCode = ErrorCode.PARAMETER_IS_EMPTY;
                logger.error("GameConnection::onAuthenticate() fail!! authenticatePacket is null!!");
            } else {
                try {
                    // 인증 검증할 토큰을 가지고 검증처리
                    Authentication.AuthenticationReq authenticationReq = Authentication.AuthenticationReq.parseFrom(authenticatePacket.getStream());
                    if (authenticationReq == null) {
                        resultCode = ErrorCode.PARAMETER_IS_EMPTY;
                        logger.error("GameConnection::onAuthenticate() fail!! authenticationReq is null!!");
                    } else if (authenticationReq.getAccessToken() == null) {
                        resultCode = ErrorCode.TOKEN_IS_EMPTY;
                        logger.error("GameConnection::onAuthenticate() fail!! authenticationReq.getAccessToken() is empty!!");
                    } else {
                        logger.info("onAuthenticate Success. token:{}", authenticationReq.getAccessToken());

                        if (authenticationReq.getAccessToken().startsWith("TapTap_AccessToken")) {
                            // 플랫폼 테스트용 토큰 - 검증없이 정상 처리
                            resultCode = ErrorCode.NONE;
                        } else {
                            // Gamebse 인증
                            //----------------------------------- 토큰 유효한지에 대한 검증 Gamebase
                            String gamebaseUrl = String.format(GameConstants.GAMEBASE_DEFAULT_URL + "/tcgb-gateway/v1.2/apps/X2bqX5du/members/%s/tokens/%s", accountId, authenticationReq.getAccessToken());
                            HttpRequest httpRequest = new HttpRequest(gamebaseUrl);
                            httpRequest.getBuilder().addHeader("Content-Type", "application/json");
                            httpRequest.getBuilder().addHeader("X-Secret-Key", GameConstants.GAMEBASE_SECRET_KEY);
                            logger.info("httpRequest url [{}]", gamebaseUrl);
                            HttpResponse response = httpRequest.GET();
                            logger.info("httpRequest response:[{}]", response.toString());

                            // Gamebase 응답 json 데이터 객체 파싱
                            AuthenticationResponse gamebaseResponse = response.getContents(AuthenticationResponse.class);
                            if (gamebaseResponse.getHeader().isSuccessful()) {
                                resultCode = ErrorCode.NONE;
                            } else {
                                resultCode = ErrorCode.TOKEN_NOT_VALIDATED;
                            }

                            String testurl = String.format(GameConstants.GAMEBASE_DEFAULT_URL + "/tcgb-member/v1.2/apps/X2bqX5du/members");
                            HttpRequest httpRequestPost = new HttpRequest(testurl);
                            httpRequestPost.getBuilder().addHeader("Content-Type", "application/json");
                            httpRequestPost.getBuilder().addHeader("X-Secret-Key", GameConstants.GAMEBASE_SECRET_KEY);

                            JsonArray userIds = new JsonArray();
                            userIds.add(accountId);

                            httpRequestPost.getBuilder().setBody(GameAnvilUtil.Gson().toJson(userIds));
                            HttpResponse responsePost = httpRequestPost.POST();
                            logger.info("httpRequestPost:[{}] , getResponseBody[{}]", responsePost.toString(), responsePost.getResponse().getResponseBody());

                            logger.info("gamebaseResponse response:[{}]", gamebaseResponse.getHeader().getResultCode());
                            //------------------------------------
                        }
                    }
                } catch (IOException | TimeoutException e) {
                    logger.error("GameConnection::onAuthenticate() fail!! AuthenticationReq.parseFrom ERROR", e);
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
