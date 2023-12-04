package com.nhn.gameanvil.sample.game.user._handler;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.message.MessageHandler;
import com.nhn.gameanvil.sample.game.GameNode;
import com.nhn.gameanvil.sample.game.user.GameUser;
import com.nhn.gameanvil.sample.game.user.model.GameUserInfo;
import com.nhn.gameanvil.sample.game.user.model.SingleRankingInfo;
import com.nhn.gameanvil.sample.protocol.GameSingle;
import com.nhn.gameanvil.sample.protocol.Result;
import com.nhn.gameanvil.sample.protocol.Result.ErrorCode;
import com.nhn.gameanvil.sample.redis.RedisHelper;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 싱글 점수 랭킹 목록 응답
 */
public class _SingleScoreRankingReq implements MessageHandler<GameUser, GameSingle.ScoreRankingReq> {
    private static final Logger logger = getLogger(_SingleScoreRankingReq.class);

    @Override
    public void execute(GameUser gameUser, GameSingle.ScoreRankingReq scoreRankingReq) throws SuspendExecution {
        Result.ErrorCode resultCode = ErrorCode.UNKNOWN;

        GameSingle.ScoreRankingRes.Builder scoreRankingRes = GameSingle.ScoreRankingRes.newBuilder();

        RedisHelper redisHelper = ((GameNode)GameNode.getInstance()).getRedisHelper();
        // 유저가 랭키이 리스트
        if (scoreRankingReq != null) {
            logger.info("scoreRankingReq  : {}", scoreRankingReq);
            int start = scoreRankingReq.getStart() - 1;
            if (start < 0) {
                start = 0;
            }

            // 랭킹 리스트
            Map<String, SingleRankingInfo> rankingInfoMap = redisHelper.getSingleRanking(start, scoreRankingReq.getEnd() - 1);

            if (rankingInfoMap != null && !rankingInfoMap.isEmpty()) {
                // 랭킹 유저 정보 리스트
                List<GameUserInfo> userDataList = redisHelper.getUserData(new ArrayList<>(rankingInfoMap.keySet()));

                // 랭킹 리스트와 유저 정보를 가지고 응답용 랭킹 리스트 작성
                for (Entry<String, SingleRankingInfo> singleRankingInfoEntry : rankingInfoMap.entrySet()) {
//                        logger.info("singleRankingInfoEntry  : {}", singleRankingInfoEntry.toString());

                    GameSingle.ScoreRankingData.Builder rankingData = GameSingle.ScoreRankingData.newBuilder();
                    if (userDataList != null) {
                        for (GameUserInfo user : userDataList) {
                            if (singleRankingInfoEntry.getValue().getUuid().equals(user.getUuid()) && user.getNickname() != null) {
                                rankingData.setNickname(user.getNickname());
                                break;
                            }
                        }
                    }
                    rankingData.setUuid(singleRankingInfoEntry.getValue().getUuid());
                    rankingData.setScore(singleRankingInfoEntry.getValue().getScore());

                    scoreRankingRes.addRankings(rankingData);
                }
            }
            resultCode = ErrorCode.NONE;
        } else {
            logger.error("_SingleScoreRankingReq::execute() tapMsg is null!!!");
            resultCode = ErrorCode.PARAMETER_IS_EMPTY;
        }

        scoreRankingRes.setResultCode(resultCode);
        logger.info("scoreRankingRes - {}", scoreRankingRes);
        gameUser.reply(scoreRankingRes);
    }
}
