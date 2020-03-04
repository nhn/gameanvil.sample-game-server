package com.nhn.tardis.taptap.space.user.cmd;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.tardis.taptap.protocol.GameSingle;
import com.nhn.tardis.taptap.protocol.Result;
import com.nhn.tardis.taptap.protocol.Result.ErrorCode;
import com.nhn.tardis.taptap.redis.RedisHelperService;
import com.nhn.tardis.taptap.space.user.GameUser;
import com.nhn.tardis.taptap.space.user.model.GameUserInfo;
import com.nhn.tardis.taptap.space.user.model.SingleRankingInfo;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 싱글 점수 랭킹 목록 응답
 */
public class CmdSingleScoreRankingReq implements IPacketHandler<GameUser> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(GameUser gameUser, Packet packet) throws SuspendExecution {
        Result.ErrorCode resultCode = ErrorCode.UNKNOWN;

        GameSingle.ScoreRankingRes.Builder scoreRankingRes = GameSingle.ScoreRankingRes.newBuilder();
        try {
            // 유저가 랭키이 리스트
            GameSingle.ScoreRankingReq scoreRankingReq = GameSingle.ScoreRankingReq.parseFrom(packet.getStream());
            if (scoreRankingReq != null) {
                logger.info("scoreRankingReq  : {}", scoreRankingReq);
                int start = scoreRankingReq.getStart() - 1;
                if (start < 0) {
                    start = 0;
                }

                // 랭킹 리스트
                Map<String, SingleRankingInfo> rankingInfoMap = RedisHelperService.getInstance().getSingleRanking(start, scoreRankingReq.getEnd() - 1);

                if (rankingInfoMap != null) {
                    // 랭킹 유저 정보 리스트
                    List<GameUserInfo> userDataList = RedisHelperService.getInstance().getUserData(new ArrayList<>(rankingInfoMap.keySet()));

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
                logger.error("TapMsg tapMsg is null!!!");
                resultCode = ErrorCode.PARAMETER_IS_EMPTY;
            }
        } catch (
            Exception e) {
            logger.error("execute()", e);
        }

        scoreRankingRes.setResultCode(resultCode);
        logger.info("scoreRankingRes - {}", scoreRankingRes);
        gameUser.reply(new Packet(scoreRankingRes.build()));
    }
}
