package com.nhn.gameanvil.sample.game.multi.usermatch;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import com.nhn.gameanvil.node.match.UserMatchMaker;
import com.nhn.gameanvil.sample.game.multi.usermatch.model.SnakeRoomInfo;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 유저 매치 2인 처리, 동시에 게임 처리, 한명이 방에 나가면 게임 종료
 */
public class SnakeRoomMatchMaker extends UserMatchMaker<SnakeRoomInfo> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Multiset<SnakeRoomInfo> ratingSet = TreeMultiset.create();

    private final int matchPoolFactorMax = 1; // match 정원의 몇 배수까지 인원을 모은 후에 rating 별로 정렬해서 매칭할 것인가?
    private int currentMatchPoolFactor = matchPoolFactorMax;
    private long lastMatchTime = System.currentTimeMillis();
    private int totalMatchMakings = 0;

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
            if (System.currentTimeMillis() - lastMatchTime >= 10000) {
                // 1000 ms 동안  leastAmount를 체우지 못한 경우
                // currentMatchPoolFactor를 조정하여leastAmount의 크기를 줄인다.
                currentMatchPoolFactor = Math.max(--currentMatchPoolFactor, 1);
            }
            return;
        }

        ratingSet.clear();
        ratingSet.addAll(matchRequests);

        if (ratingSet.size() >= matchSize) {

            // ratingSet의 순서대로 matchingAmount*matchSize 만큼 항목들을 소비API
            int matchingAmount = matchSingles(ratingSet);

            if (matchingAmount > 0) {
                totalMatchMakings += matchingAmount;
                logger.info("{} match(s) made (total: {}) - {}", matchingAmount, totalMatchMakings, this.getMatchingGroup());

                lastMatchTime = System.currentTimeMillis();
                currentMatchPoolFactor = matchPoolFactorMax;
            }
        }
    }

    @Override
    public boolean refill(SnakeRoomInfo req) {
        if (logger.isDebugEnabled()) {
            logger.debug("SnakeRoomMatchMaker.refill()");
        }

        try {
            List<SnakeRoomInfo> refillRequests = getRefillRequests();

            if (refillRequests.isEmpty()) {
                return false;
            }

            for (SnakeRoomInfo refillInfo : refillRequests) {
                // 100점 이상 차이나지 않으면 리필
                if (Math.abs(req.getRating() - refillInfo.getRating()) < 100) {
                    if (refillRoom(req, refillInfo)) { // 해당 매칭 요청을 리필이 필요한 방으로 매칭
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("UserMatchMaker2User::refill()", e);
        }
        return false;
    }
}