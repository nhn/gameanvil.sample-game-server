package com.nhn.tardis.taptap.space.game.multi.roommatch;

import com.nhn.tardis.taptap.space.game.multi.roommatch.model.UnlimitedTapRoomInfo;
import com.nhnent.tardis.console.match.RoomMatchMaker;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 룸 매치 로직 처리
 */
public class UnlimitedTapRoomMatchMaker extends RoomMatchMaker<UnlimitedTapRoomInfo> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public UnlimitedTapRoomInfo match(UnlimitedTapRoomInfo terms, Object... args) {
        logger.info("match {}", terms);
        String bypassRoomId = terms.getRoomId();
        logger.info("match - args : {}", args);
        List<UnlimitedTapRoomInfo> rooms = getRooms();
        logger.info("match - rooms : {}", rooms.size());
        // rooms는 인원수가 적은 순서로 정렬되어있음.
        // roomId 가 bypassRoomId이 아닌 첫번째 room을 선택.
        for (UnlimitedTapRoomInfo info : rooms) {
            if (info.getRoomId().equals(bypassRoomId)) {
                // moveRoom 옵션이 true 일 경우 참여중인 방은 제외하기
                logger.info("match - bypass : {}", bypassRoomId);
                continue;
            }

            // 최대 인원수가 terms와 다르면 pass!
            if (info.getUserMaxCount() != terms.getUserMaxCount()) {
                logger.info("match - userCountMax : {}", info.getUserMaxCount());
                continue;
            }

            // 꽉 찼으면 pass!
            if (info.getUserMaxCount() == info.getUserCurrentCount()) {
                logger.info("match - userCountCurr : {}", info.getUserMaxCount());
                continue;
            }

            // 매칭 성공!
            logger.info("match : {}", info.getRoomId());
            return info;
        }

        // 매칭할 방이 없어 매칭 실패.
        // create 옵션이 true일 경우 자동으로 방을 생성하면서 매칭 성공.
        return null;
    }

    @Override
    public Comparator<UnlimitedTapRoomInfo> getComparator() {
        return new Comparator<UnlimitedTapRoomInfo>() {
            @Override
            public int compare(UnlimitedTapRoomInfo o1, UnlimitedTapRoomInfo o2) {
                logger.info("match - compare : {}, {}", o1, o2);
                return o1.getUserMaxCount() - o2.getUserMaxCount();
            }
        };
    }
}
