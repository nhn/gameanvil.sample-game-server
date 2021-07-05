package com.nhn.gameanvil.sample.db.jasyncsql;

import co.paralleluniverse.fibers.SuspendExecution;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.RowData;
import com.nhn.gameanvil.async.db.JAsyncSql;
import com.nhn.gameanvil.sample.db.mybatis.dto.UserDto;
import com.nhn.gameanvil.sample.game.user.model.GameUserInfo;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAsyncSQL 사용하여 처리
 */
public class JAsyncSqlManager {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private JAsyncSql jAsyncSql;

    // JAsyncSQL 연결
    public JAsyncSqlManager(String username, String host, int port, String password, String database, int maxActiveConnection) {
        jAsyncSql = new JAsyncSql(new com.github.jasync.sql.db.Configuration(
            username,
            host,
            port,
            password,
            database), maxActiveConnection);

        logger.info("JAsyncSqlManager::JAsyncSql connect");
    }

    /**
     * uuid로 유저 DB에서 유저 데이터를 조회
     *
     * @param uuid 유저 유니크 식별자
     * @return 유저 정보 반환
     * @throws TimeoutException 해당 호출에 대해 timeout 이 발생 할 수 있음을 의미
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    public GameUserInfo selectUserByUuid(String uuid) throws TimeoutException, SuspendExecution {
        String sql = "CALL sp_users_select_uuid( '"
            + uuid + "')";

        QueryResult queryResult = jAsyncSql.execute(sql);
        UserDto userDto = null;

        if (queryResult != null && queryResult.getRows().size() == 1) {
            RowData row = queryResult.getRows().get(0);
            userDto = new UserDto();
            if (row != null) {
                userDto.setUuid(row.getString("uuid"));
                userDto.setLoginType(row.getInt("login_type"));
                userDto.setAppVersion(row.getString("app_version"));
                userDto.setAppStore(row.getString("app_store"));
                userDto.setDeviceModel(row.getString("device_model"));
                userDto.setDeviceCountry(row.getString("device_country"));
                userDto.setDeviceLanguage(row.getString("device_language"));
                userDto.setNickname(row.getString("nickname"));
                userDto.setHeart(row.getInt("heart"));
                userDto.setCoin(row.getLong("coin"));
                userDto.setRuby(row.getLong("ruby"));
                userDto.setLevel(row.getInt("level"));
                userDto.setExp(row.getLong("exp"));
                userDto.setCurrentDeck(row.getString("current_deck"));
                userDto.setCreateDate(row.getDate("create_date").toDate());
                userDto.setUpdateDate(row.getDate("update_date").toDate());
            }
        }

        if (userDto == null) {
            return null;
        } else {
            return userDto.toGameUserInfo();
        }
    }

    /**
     * 유저 정보 DB에 저장
     *
     * @param gameUserInfo 유저 정보 전달
     * @return 저장된 레코드 수 반환
     * @throws TimeoutException 해당 호출에 대해 timeout 이 발생 할 수 있음을 의미
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    public int insertUser(GameUserInfo gameUserInfo) throws TimeoutException, SuspendExecution {
        String sql = "CALL sp_users_insert( '"
            + gameUserInfo.getUuid() + "', "
            + gameUserInfo.getLoginType() + ", '"
            + gameUserInfo.getAppVersion() + "', '"
            + gameUserInfo.getAppStore() + "', '"
            + gameUserInfo.getDeviceModel() + "', '"
            + gameUserInfo.getDeviceCountry() + "', '"
            + gameUserInfo.getDeviceLanguage() + "', '"
            + gameUserInfo.getNickname() + "', "
            + gameUserInfo.getHeart() + ", "
            + gameUserInfo.getCoin() + ", "
            + gameUserInfo.getRuby() + ", "
            + gameUserInfo.getLevel() + ", "
            + gameUserInfo.getExp() + ", "
            + gameUserInfo.getHighScore() + ", '"
            + gameUserInfo.getCurrentDeck() + "')";

        QueryResult queryResult = jAsyncSql.execute(sql);

        return getStoredProcedureRowCount(queryResult);
    }

    /**
     * 유저 현제 가지고 있는 덱정보 저장
     *
     * @param uuid        유저 유니크 식별자
     * @param currentDeck 수정 할 덱이름
     * @return 수정된 레코드 수 반환
     * @throws TimeoutException 해당 호출에 대해 timeout 이 발생 할 수 있음을 의미
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    public int updateUserCurrentDeck(String uuid, String currentDeck) throws TimeoutException, SuspendExecution {
        String sql = "CALL sp_users_update_current_deck( '"
            + uuid + "', '"
            + currentDeck + "')";

        QueryResult queryResult = jAsyncSql.execute(sql);

        return getStoredProcedureRowCount(queryResult);
    }

    /**
     * 유저 닉네임 수정
     *
     * @param uuid     유저 유니크 식별자
     * @param nickname 수정 할 닉네임
     * @return 수정된 레코드 수
     * @throws TimeoutException 해당 호출에 대해 timeout 이 발생 할 수 있음을 의미
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    public int updateUserNickname(String uuid, String nickname) throws TimeoutException, SuspendExecution {
        String sql = "CALL sp_users_update_nickname( '"
            + uuid + "', '"
            + nickname + "')";
        QueryResult queryResult = jAsyncSql.execute(sql);

        return getStoredProcedureRowCount(queryResult);
    }

    /**
     * 유저의 최고 점수 저장
     *
     * @param uuid      유저 유니크 식별자
     * @param highScore 수정할 최고 점수
     * @return 수정된 레코드 수 반환
     * @throws TimeoutException 해당 호출에 대해 timeout 이 발생 할 수 있음을 의미
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    public int updateUserHigScore(String uuid, int highScore) throws TimeoutException, SuspendExecution {
        String sql = "CALL sp_users_update_high_score( '"
            + uuid + "', "
            + highScore + ")";
        QueryResult queryResult = jAsyncSql.execute(sql);

        return getStoredProcedureRowCount(queryResult);

    }

    private int getStoredProcedureRowCount(QueryResult queryResult) {
        int result = -1;

        RowData row = queryResult.getRows().get(0);
        if (row != null) {
            Long rowCount = row.getLong("ROW_COUNT()");
            if (rowCount != null) {
                result = rowCount.intValue();
            }
        }
        return result;
    }

    public void close() {
        jAsyncSql.disconnect();
    }
}
