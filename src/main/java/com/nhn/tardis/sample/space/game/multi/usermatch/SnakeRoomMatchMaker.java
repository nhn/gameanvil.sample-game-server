package com.nhn.tardis.sample.space.game.multi.usermatch;

import com.nhn.tardis.sample.space.game.multi.usermatch.model.SnakeRoomInfo;
import com.nhnent.tardis.console.match.UserMatchMaker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 유저 매치 2인 처리, 동시에 게임 처리, 한명이 방에 나가면 게임 종료
 */
public class SnakeRoomMatchMaker extends UserMatchMaker<SnakeRoomInfo> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final int matchPoolFactorMax = 1; // match 정원의 몇 배수까지 인원을 모은 후에 rating 별로 정렬해서 매칭할 것인가?
    private int currentMatchPoolFactor = matchPoolFactorMax;
    private long lastMatchTime = System.currentTimeMillis();

    public SnakeRoomMatchMaker() {
        super(
            2, // 매치될 Room 인원 수
            10000 // Timeout
        );
    }

    @Override
    public void match() {
        // matchSize : 매치될 Room 인원 수
        // leastAmount : 매칭 계산에 필요한 인원 수.
        int leastAmount = matchSize * currentMatchPoolFactor;
        List<SnakeRoomInfo> matchRequests = getMatchRequests(leastAmount);
        if (matchRequests == null) {
            // getMatchRequests : 매칭 요청자의 총 수가 leastAmount보다 적을 경우 null을 리턴한다.
            if (System.currentTimeMillis() - lastMatchTime >= 1000) {
                // 1000 ms 동안  leastAmount를 체우지 못한 경우
                // currentMatchPoolFactor를 조정하여leastAmount의 크기를 줄인다.
                if (currentMatchPoolFactor > 1) {
                    currentMatchPoolFactor = Math.max(currentMatchPoolFactor / 2, 1);
                    logger.info("match() - reduce currentMatchPoolFactor: {}", currentMatchPoolFactor);
                }
            }
            return;
        }

        // 조건에 맞는 요청들을 모아 매칭시킨다.
        // 여기에서는 rating을 100 단위 그룹으로 묶어 매칭을 시킨다.
        // 0~99, 100~199, 200~299 ...
        // 요청이 많을 경우 여기에서 그룹을 묶는 작업을 하는 것이 서버에 부하가 될 수 있다.
        // 그룹 별로 RoomType을 나누어 별도의 MatchMaker를 사용하는 방법도 고려해 보자.
        Map<Integer, List<SnakeRoomInfo>> entries = new TreeMap<>();
        for (SnakeRoomInfo info : matchRequests) {
            int ratingGroup = info.getRating() / 100;
            if (!entries.containsKey(ratingGroup)) {
                entries.put(ratingGroup, new ArrayList<>());
            }

            List<SnakeRoomInfo> subEntries = entries.get(ratingGroup);
            subEntries.add(info);
        }

        entries.forEach((ratingGroup, subEntries) -> {
            int matchingAmount = matchSingles(subEntries);
            if (matchingAmount > 0) {
                logger.info("match() - {} match(s) made for RatingGroup {}", matchingAmount, ratingGroup);
                lastMatchTime = System.currentTimeMillis();
                currentMatchPoolFactor = matchPoolFactorMax;
            }
        });
    }

    @Override
    public boolean refill(SnakeRoomInfo req) {
        logger.info("SnakeRoomMatchMaker.refill()");
        // 리필 사용 하지않음
        return false;
    }
}