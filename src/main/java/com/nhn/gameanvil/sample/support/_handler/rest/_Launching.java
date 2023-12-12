package com.nhn.gameanvil.sample.support._handler.rest;

import co.paralleluniverse.fibers.SuspendExecution;
import com.google.gson.JsonObject;
import com.nhn.gameanvil.GameAnvilUtil;
import com.nhn.gameanvil.packet.message.RestMessageHandler;
import com.nhn.gameanvil.rest.RestObject;
import com.nhn.gameanvil.sample.support.LaunchingSupport;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 클라이언트가 정보를 전달 받고, 서버의 접속 정보 응답 처리
 *
 * 정보를 제대로 전달 하지않으면 400 으로 응답처리
 */

public class _Launching implements RestMessageHandler<LaunchingSupport> {
    private static final Logger logger = getLogger(_Launching.class);

    @Override
    public void execute(LaunchingSupport target, RestObject restObject) throws SuspendExecution {
        Map<String, List<String>> queryMap = restObject.getRequestParameters();

        logger.info("execute restObject url {}, parameter {} ", restObject.getOriginUrl(), restObject.getRequestParameters());
        String responseMessage = "SUCCESS";
        int stateCode = 200;

        // 파라미터 파싱
        List<String> list = queryMap.get("platform");
        String platform = CollectionUtils.isNotEmpty(list) ? list.get(0).toUpperCase() : null;

        list = queryMap.get("appStore");
        String appStore = CollectionUtils.isNotEmpty(list) ? list.get(0).toUpperCase() : null;

        list = queryMap.get("appVersion");
        String appVersion = CollectionUtils.isNotEmpty(list) ? list.get(0) : null;

        list = queryMap.get("deviceId");
        String deviceId = CollectionUtils.isNotEmpty(list) ? list.get(0) : null;

        // 응답 json
        JsonObject jsonObject = new JsonObject();
        if (platform != null && appStore != null && appVersion != null && deviceId != null) {
            //파라미터가 이상없을때 세션 서버 정보 응답
            jsonObject.addProperty("serverUrl", "127.0.0.1");
            jsonObject.addProperty("port", 18200);
        } else {
            stateCode = 400;
            responseMessage = "Invalid Parameters : platform, appStore, appVersion, deviceId ";
        }

        // 응답 코드와 메세지 정의
        jsonObject.addProperty("status", stateCode);
        jsonObject.addProperty("message", responseMessage);

//        restObject.setResponseStatus(HttpResponseStatus.BAD_REQUEST);

        // 결과 응답
        restObject.writeString(GameAnvilUtil.Gson().toJson(jsonObject));   // GameAnvil 에서 제공하는 Gson사용
    }
}
