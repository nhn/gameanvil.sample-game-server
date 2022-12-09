package com.nhn.gameanvil.sample.game.user._handler;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhn.gameanvil.sample.game.GameNode;
import com.nhn.gameanvil.sample.game.user.GameUser;
import com.nhn.gameanvil.sample.protocol.Result.ErrorCode;
import com.nhn.gameanvil.sample.protocol.User;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 유저가 닉네임 변경
 * <p>
 * 서버인스턴스 닉네임 변경, DB저장, request 형식으로 전달되어 서버에서 처리후 reply 처리
 */
public class _ChangeNicknameReq implements PacketHandler<GameUser> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(GameUser gameUser, Packet packet) throws SuspendExecution {
        ErrorCode resultCode = ErrorCode.UNKNOWN;
        User.ChangeNicknameRes.Builder changeNicknameRes = User.ChangeNicknameRes.newBuilder();
        try {
            logger.info("userId : {}", gameUser.getUserId());
            String checkNickname = null;

            // 닉네임 변경 요청 처리
            User.ChangeNicknameReq changeNicknameReq = User.ChangeNicknameReq.parseFrom(packet.getStream());
            if (changeNicknameReq == null || changeNicknameReq.getNickname() == null || changeNicknameReq.getNickname().isEmpty()) {
                resultCode = ErrorCode.PARAMETER_IS_EMPTY;
                logger.error("_ChangeNicknameReq::execute() shuffleDeckReq is null or empty!!");
            } else {
                // 닉네임 ng word 확인 -- 필요한 로직 구현 필요
                checkNickname = changeNicknameReq.getNickname();
                resultCode = ErrorCode.NONE;

                // 닉네임 저장
                if (resultCode == ErrorCode.NONE) {
                    int dbResultCount = -1;
                    // 유저 덱 변경 저장
//                    if (GameConstants.USE_DB_JASYNC_SQL) {
//                        // JAsyncSql
//                        dbResultCount = ((GameNode)gameUser.getBaseGameNode()).getJAsyncSqlManager().updateUserNickname(gameUser.getGameUserInfo().getUuid(), checkNickname);
//                    } else {
//                        // Mybatis
//                        dbResultCount = UserDbHelperService.getInstance().updateUserNickname(gameUser.getGameUserInfo().getUuid(), checkNickname);
//                    }

                    // xdev api
                    dbResultCount = ((GameNode)gameUser.getBaseGameNode()).getUserDbHelper().updateUserNickname(gameUser.getGameUserInfo().getUuid(), checkNickname);

                    if (dbResultCount == 1) {   // 정상 저장되었을 경우에 응답 데이터 설정
                        gameUser.getGameUserInfo().setNickname(checkNickname);

                        // 유저 데이터 응답
                        changeNicknameRes.setUserData(gameUser.getUserDataByProto());
                    } else {
                        resultCode = ErrorCode.DB_ERROR;
                    }
                }
            }

        } catch (IOException | TimeoutException e) {
            logger.error("_ChangeNicknameReq::execute()", e);
            resultCode = ErrorCode.UNKNOWN;
        }

        changeNicknameRes.setResultCode(resultCode);
        logger.info("changeNicknameRes - {}", changeNicknameRes);
        gameUser.reply(new Packet(changeNicknameRes.build()));
    }
}
