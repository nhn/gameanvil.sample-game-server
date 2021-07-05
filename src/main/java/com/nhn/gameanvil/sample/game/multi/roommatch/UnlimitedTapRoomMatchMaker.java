package com.nhn.gameanvil.sample.game.multi.roommatch;

import com.nhn.gameanvil.annotation.RoomType;
import com.nhn.gameanvil.annotation.ServiceName;
import com.nhn.gameanvil.node.game.data.RoomMatchResultCode;
import com.nhn.gameanvil.node.match.BaseRoomMatchMaker;
import com.nhn.gameanvil.sample.common.GameConstants;
import com.nhn.gameanvil.sample.game.multi.roommatch.model.UnlimitedTapRoomMatchForm;
import com.nhn.gameanvil.sample.game.multi.roommatch.model.UnlimitedTapRoomMatchInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 룸 매치 로직 처리
 */
@ServiceName(GameConstants.GAME_NAME)
@RoomType(GameConstants.GAME_ROOM_TYPE_MULTI_ROOM_MATCH)
public class UnlimitedTapRoomMatchMaker extends BaseRoomMatchMaker<UnlimitedTapRoomMatchForm, UnlimitedTapRoomMatchInfo> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public RoomMatchResultCode onPreMatch(UnlimitedTapRoomMatchForm baseRoomMatchForm, Object... args) {
        return RoomMatchResultCode.SUCCESS;
    }

    @Override
    public boolean canMatch(UnlimitedTapRoomMatchForm baseRoomMatchForm, UnlimitedTapRoomMatchInfo baseRoomMatchInfo, Object... args) {
        return true;
    }

    @Override
    public int compare(UnlimitedTapRoomMatchInfo o1, UnlimitedTapRoomMatchInfo o2) {
        if (o1.getCreateTime() > o2.getCreateTime()) {
            return -1;
        } else if (o1.getCreateTime() < o2.getCreateTime()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void onPostMatch(UnlimitedTapRoomMatchForm baseRoomMatchForm, UnlimitedTapRoomMatchInfo baseRoomMatchInfo, Object... args) {

    }

    @Override
    public void onIncreaseUserCount(int roomId, String matchingUserCategory, int currentUserCount) {

    }

    @Override
    public void onDecreaseUserCount(int roomId, String matchingUserCategory, int currentUserCount) {

    }
}
