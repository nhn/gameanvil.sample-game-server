package com.nhn.tardis.sample.service.cmd;

import co.paralleluniverse.fibers.SuspendExecution;
import com.google.gson.JsonObject;
import com.nhnent.tardis.assist.TardisUtil;
import com.nhnent.tardis.packet.RestPacketHandler;
import com.nhnent.tardis.rest.RestObject;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 클라이언트가 처음 접속하여, 버전 정보 점검 정보 등을 획득
 */

public class CmdLaunching implements RestPacketHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(Object target, RestObject restObject) throws SuspendExecution {
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
            jsonObject.addProperty("port", 11200);
        } else {
            stateCode = 400;
            responseMessage = "Invalid Parameters : platform, appStore, appVersion, deviceId ";
        }

        // 응답 코드와 메세지 정의
        jsonObject.addProperty("status", stateCode);
        jsonObject.addProperty("message", responseMessage);

//        restObject.setResponseStatus(HttpResponseStatus.BAD_REQUEST);

        // 결과 응답
        restObject.writeString(TardisUtil.Gson().toJson(jsonObject));   // tardis에서 제공하는 Gson사용
    }
}
