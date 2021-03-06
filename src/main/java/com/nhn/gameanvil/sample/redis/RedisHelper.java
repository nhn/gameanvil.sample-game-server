package com.nhn.gameanvil.sample.redis;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.GameAnvilUtil;
import com.nhn.gameanvil.async.redis.Lettuce;
import com.nhn.gameanvil.sample.common.GameConstants;
import com.nhn.gameanvil.sample.game.user.model.GameUserInfo;
import com.nhn.gameanvil.sample.game.user.model.SingleRankingInfo;
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
 * 레디스 처리 하는 helper 클래스
 */
public class RedisHelper {
    private static final Logger logger = getLogger(RedisHelper.class);

    // 싱글 점수 키
    public static final String REDIS_SINGLE_SCORE_KEY = "taptap_single_score";
    // 유저 데이터 키
    public static final String REDIS_USER_DATA_KEY = "taptap_user_data";

    private RedisClusterClient clusterClient;
    private StatefulRedisClusterConnection<String, String> clusterConnection;
    private RedisAdvancedClusterAsyncCommands<String, String> clusterAsyncCommands;

    /**
     * 레디스 연결 처리, 사용하기 전에 최초에 한번 호출해서 연결 필요
     *
     * @param url  접속 url
     * @param port 점속 port
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    public void connect(String url, int port) throws SuspendExecution {
        // 레디스 연결 처리
        RedisURI clusterURI = RedisURI.Builder.redis(url, port).build();

        // 패스워드가 필요한 경우 에는 패스워드 설청을 추가해서 RedisURI를 생성한다. 
//        RedisURI clusterURI = RedisURI.Builder.redis(url, port).withPassword("password").build();
        this.clusterClient = RedisClusterClient.create(Collections.singletonList(clusterURI));
        this.clusterConnection = Lettuce.connect(GameConstants.REDIS_THREAD_POOL, clusterClient);

        if (this.clusterConnection.isOpen()) {
            logger.info("============= Connected to Redis using Lettuce =============");
        }

        this.clusterAsyncCommands = clusterConnection.async();
    }

    /**
     * 서버가 종료될 때 내려가기전에 호출 처리 필요
     */
    public void shutdown() throws SuspendExecution {
        clusterConnection.close();
        clusterClient.shutdown();

        if (logger.isTraceEnabled()) {
            logger.trace("onShutdown");
        }
    }

    /**
     * 유저 데이터 저장, 유저의 uuid 키로 데이터 저장
     *
     * @param gameUserInfo 유저 정보
     * @return 저장 성고 여부 반환
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    public boolean setUserData(GameUserInfo gameUserInfo) throws SuspendExecution {
        String value = GameAnvilUtil.Gson().toJson(gameUserInfo);

        boolean isSuccess = false;
        try {
            Lettuce.awaitFuture(clusterAsyncCommands.hset(REDIS_USER_DATA_KEY, gameUserInfo.getUuid(), value)); // 해당 리턴값은 최초에 set 할때만 true 이고 있는값갱신시에는 false 응답
            isSuccess = true;
        } catch (TimeoutException e) {
            logger.error("RedisHelper::setUserData()", e);
        }
        return isSuccess;
    }

    /**
     * 유저 리스트 정보 얻기
     *
     * @param uuidList 검색할 유저의 유니크 식별자 리스트
     * @return 검색된 유저 리스트 반환
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
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
                    GameUserInfo userData = GameAnvilUtil.Gson().fromJson(data.getValue(), GameUserInfo.class);
                    userDataList.add(userData);
                    logger.info("userData {}", userData);
                }
            }
            return userDataList;
        } catch (TimeoutException e) {
            logger.error("RedisHelper::getUserData()", e);
            return null;
        }
    }

    /**
     * 랭킹용으로 싱글 점수 저장
     *
     * @param key   랭킹에 사용할 키
     * @param value 저장할 점수
     * @return 싱글 점수 저장 성공 여부 반환
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    public boolean setSingleScore(String key, double value) throws SuspendExecution {
        logger.info("setSingleScore - key : {}, value1 : {}, value2 : {}", REDIS_SINGLE_SCORE_KEY, value, key);
        boolean isSuccess = false;
        try {
            Lettuce.awaitFuture(clusterAsyncCommands.zadd(REDIS_SINGLE_SCORE_KEY, value, key));
            isSuccess = true;
        } catch (TimeoutException e) {
            logger.error("RedisHelper::setSingleScore()", e);
        }
        return isSuccess;
    }

    /**
     * 싱글 점수 랭킹 리스트
     *
     * @param start 시작 등수
     * @param end   마지막 등수
     * @return 맵 형태로 싱글 랭키 정보 반환
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
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
            logger.error("RedisHelper::getSingleRanking()", e);
            return null;
        }
    }
}