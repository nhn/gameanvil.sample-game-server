package com.nhn.gameanvil.sample.game.user._handler;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.sample.mybatis.UserDbHelperService;
import com.nhn.gameanvil.sample.protocol.Result;
import com.nhn.gameanvil.sample.protocol.Result.ErrorCode;
import com.nhn.gameanvil.sample.protocol.User;
import com.nhn.gameanvil.sample.protocol.User.CurrencyType;
import com.nhn.gameanvil.sample.game.user.GameUser;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 유저가 가지고 있는 현제 덱정보 서버갱신 저장, request 형식으로 전달되어 서버에서 처리후 reply 처리가 되어야 한다.
 */
public class _ShuffleDeckReq implements PacketHandler<GameUser> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String[] DECK_LIST = {"africa", "alpha", "america", "animal", "asia", "candy", "dessert", "europe", "sushi", "tamastory", "western"};

    @Override
    public void execute(GameUser gameUser, Packet packet) throws SuspendExecution {
        Result.ErrorCode resultCode = ErrorCode.UNKNOWN;
        User.ShuffleDeckRes.Builder shuffleDeckRes = User.ShuffleDeckRes.newBuilder();
        try {
            logger.info("_ShuffleDeckReq - userId : {}", gameUser.getUserId());

            // 덱 셔플 처리
            User.ShuffleDeckReq shuffleDeckReq = User.ShuffleDeckReq.parseFrom(packet.getStream());
            if (shuffleDeckReq == null || shuffleDeckReq.getCurrencyType() == CurrencyType.CURRENCY_NONE || shuffleDeckReq.getUsage() == 0) {
                resultCode = ErrorCode.PARAMETER_IS_EMPTY;
                logger.error("_ShuffleDeckReq fail!! shuffleDeckReq is null!!");
            } else if (shuffleDeckReq.getCurrencyType() == CurrencyType.CURRENCY_COIN) { // 코인 확인
                if (gameUser.getGameUserInfo().getCoin() < shuffleDeckReq.getUsage()) {
                    resultCode = ErrorCode.NOT_ENOUGH_COIN;
                    logger.warn("_ShuffleDeckReq - NOT_ENOUGH_COIN : {}", gameUser.getGameUserInfo().getCoin());
                } else {
                    gameUser.getGameUserInfo().useCoin(shuffleDeckReq.getUsage());
                    resultCode = ErrorCode.NONE;
                }
            } else if (shuffleDeckReq.getCurrencyType() == CurrencyType.CURRENCY_RUBY) { // 루비 확인
                if (gameUser.getGameUserInfo().getRuby() < shuffleDeckReq.getUsage()) {
                    resultCode = ErrorCode.NOT_ENOUGH_RUBY;
                    logger.warn("_ShuffleDeckReq - NOT_ENOUGH_RUBY : {}", gameUser.getGameUserInfo().getRuby());
                } else {
                    gameUser.getGameUserInfo().useRuby(shuffleDeckReq.getUsage());
                    resultCode = ErrorCode.NONE;
                }
            }

            // 덱변경
            if (resultCode == ErrorCode.NONE) {
                ArrayList<String> shuffleDeckList = new ArrayList<>(Arrays.asList(DECK_LIST));
                shuffleDeckList.remove(gameUser.getGameUserInfo().getCurrentDeck());
                logger.info("_ShuffleDeckReq - shuffleDeckList : {}", shuffleDeckList);

                String nextDeck = shuffleDeckList.get(new Random().nextInt(shuffleDeckList.size()));
                logger.info("_ShuffleDeckReq - nextDeck : {}", nextDeck);

                // 유저 덱 변경 저장
                int dbResultCount = UserDbHelperService.getInstance().updateUserCurrentDeck(gameUser.getGameUserInfo().getUuid(), nextDeck);
                if (dbResultCount == 1) {   // 정상 저장되었을 경우에 응답 데이터 설정
                    gameUser.getGameUserInfo().setCurrentDeck(nextDeck);

                    shuffleDeckRes.setDeck(nextDeck);
                    shuffleDeckRes.setBalanceCoin(gameUser.getGameUserInfo().getCoin());
                    shuffleDeckRes.setBalanceRuby(gameUser.getGameUserInfo().getRuby());
                } else {
                    resultCode = ErrorCode.DB_ERROR;
                }
            }
        } catch (Exception e) {
            logger.error("_ShuffleDeckReq::execute()", e);
            resultCode = ErrorCode.UNKNOWN;
        }

        shuffleDeckRes.setResultCode(resultCode);
        logger.info("shuffleDeckRes - {}", shuffleDeckRes);
        gameUser.reply(new Packet(shuffleDeckRes.build()));
    }
}
