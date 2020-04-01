package com.nhn.tardis.sample.redis;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.tardis.sample.common.GameConstants;
import com.nhn.tardis.sample.space.user.model.GameUserInfo;
import com.nhn.tardis.sample.space.user.model.SingleRankingInfo;
import com.nhnent.tardis.common.util.TardisUtil;
import com.nhnent.tardis.console.sonic.Lettuce;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;

/**
 * 레디스 처리 하는 서비스
 */
public class RedisHelper {
    private static final Logger logger = getLogger(RedisHelper.class);

    public static final String REDIS_SINGLE_SCORE_KEY = "taptap_single_score";
    public static final String REDIS_USER_DATA_KEY = "taptap_user_data";

    private RedisClusterClient clusterClient;
    private StatefulRedisClusterConnection<String, String> clusterConnection;
    private RedisAdvancedClusterAsyncCommands<String, String> clusterAsyncCommands;

    /**
     * 레디스 연결, 사용하기전에 최초에 한번 호출해서 연결 해야 한다.
     *
     * @param url  접속 url
     * @param port 점속 port
     * @throws SuspendExecution
     */
    public void connect(String url, int port) throws SuspendExecution {
        // 레디스 연결 처리
        RedisURI clusterURI = RedisURI.Builder.redis(url, port).build();
        this.clusterClient = RedisClusterClient.create(Collections.singletonList(clusterURI));
        this.clusterConnection = Lettuce.connect(GameConstants.REDIS_THREAD_POOL, clusterClient);

        if (this.clusterConnection.isOpen()) {
            logger.info("============= Connected to Redis using Lettuce =============");
        }

        this.clusterAsyncCommands = clusterConnection.async();
    }

    /**
     * 접속 종료 서버가 내려가기전에 호출되어야 한다,
     */
    public void shutdown() {
        clusterConnection.close();
        clusterClient.shutdown();

        if (logger.isTraceEnabled()) {
            logger.trace("onShutdown");
        }
    }

    /**
     * 유저 데이터 레디스에 저장
     *
     * @param gameUserInfo 유저 정보
     * @return 저장 성고 여부
     * @throws SuspendExecution
     */
    public boolean setUserData(GameUserInfo gameUserInfo) throws SuspendExecution {
        String value = TardisUtil.Gson().toJson(gameUserInfo);

        boolean isSuccess = false;
        try {
            Lettuce.awaitFuture(clusterAsyncCommands.hset(REDIS_USER_DATA_KEY, gameUserInfo.getUuid(), value)); // 해당 리턴값은 최초에 set 할때만 true 이고 있는값갱신시에는 false 응답
            isSuccess = true;
        } catch (TimeoutException e) {
            logger.error("setUserData - timeout", e);
        }
        return isSuccess;
    }

    /**
     * 유저 리스트 정보 얻기
     *
     * @param uuidList 검색할 유저의 유니크 식별자 리스트
     * @return 검색된 유저 리스스     * @throws SuspendExecution
     */
    public List<GameUserInfo> getUserData(List<String> uuidList) throws SuspendExecution {
        logger.info("getUserData - uuidList : {}", uuidList);

        String[] stringArray = new String[uuidList.size()];
        uuidList.toArray(stringArray);

        try {
            List<KeyValue<String, String>> redisUserList = Lettuce.awaitFuture(clusterAsyncCommands.hmget(REDIS_USER_DATA_KEY, stringArray));
            List<GameUserInfo> userDataList = new ArrayList<>();
            for (KeyValue<String, String> data : redisUserList) {
                if (data.hasValue()) {
                    GameUserInfo userData = TardisUtil.Gson().fromJson(data.getValue(), GameUserInfo.class);
                    userDataList.add(userData);
                    logger.info("userData {}", userData);
                }
            }
            return userDataList;
        } catch (TimeoutException e) {
            logger.error("getUserData - timeout", e);
            return null;
        }
    }

    /**
     * 랭킹용으로 싱글 점수 저장
     *
     * @param key   랭킹에 사용할 키
     * @param value 저장할 점수
     * @return
     * @throws SuspendExecution
     */
    public boolean setSingleScore(String key, double value) throws SuspendExecution {
        logger.info("setSingleScore - key : {}, value1 : {}, value2 : {}", REDIS_SINGLE_SCORE_KEY, value, key);
        boolean isSuccess = false;
        try {
            Lettuce.awaitFuture(clusterAsyncCommands.zadd(REDIS_SINGLE_SCORE_KEY, value, key));
            isSuccess = true;
        } catch (TimeoutException e) {
            logger.error("setSingleScore - timeout", e);
        }
        return isSuccess;
    }

    /**
     * 싱글 점수 랭킹 리스트
     *
     * @param start 시작 등수
     * @param end   마지막 등수
     * @return
     * @throws SuspendExecution
     */
    public Map<String, SingleRankingInfo> getSingleRanking(int start, int end) throws SuspendExecution {
        RedisFuture<List<ScoredValue<String>>> future = clusterAsyncCommands.zrevrangeWithScores(REDIS_SINGLE_SCORE_KEY, start, end);
        CompletionStage<Map<String, SingleRankingInfo>> cs = future.thenApplyAsync(r -> {
            Map<String, SingleRankingInfo> rankingInfoMap = new LinkedHashMap<>();
            for (ScoredValue data : r) {
                SingleRankingInfo singleRankingInfo = new SingleRankingInfo();
                singleRankingInfo.setUuid(data.getValue().toString());
                singleRankingInfo.setScore(data.getScore());
                rankingInfoMap.put(singleRankingInfo.getUuid(), singleRankingInfo);
                logger.info("getSingleRanking  =====> singleRankingInfo: {}", singleRankingInfo.toString());
            }

            return rankingInfoMap;
        });

        try {
            return Lettuce.awaitFuture(cs);
        } catch (TimeoutException e) {
            logger.error("getSingleRanking ", e);
            return null;
        }
    }
}