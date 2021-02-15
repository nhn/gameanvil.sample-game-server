package com.nhn.gameanvil.sample.db;

import co.paralleluniverse.fibers.SuspendExecution;
import com.github.jasync.sql.db.Configuration;
import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.ConnectionPoolConfiguration;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.RowData;
import com.github.jasync.sql.db.mysql.pool.MySQLConnectionFactory;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.nhn.gameanvil.async.Async;
import com.nhn.gameanvil.sample.game.user.model.GameUserInfo;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDbHelperJasyncSql {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Connection connection;

    public UserDbHelperJasyncSql(Configuration configuration) throws SuspendExecution {
        ConnectionPoolConfiguration connectionPoolConfiguration = new ConnectionPoolConfiguration(
            configuration.getHost(),
            configuration.getPort(),
            configuration.getDatabase(),
            configuration.getUsername(),
            configuration.getPassword(),
            10,
            TimeUnit.MINUTES.toMillis(15),  // maxIdle
            10000,                         // maxQueueSize
            TimeUnit.SECONDS.toMillis(30));

        connection = new ConnectionPool<>(new MySQLConnectionFactory(configuration), connectionPoolConfiguration);

        try {
            connection.connect().get();
            logger.warn("-----> jasync-sql UserDbHelperJasyncSql !!");
        } catch (InterruptedException | ExecutionException e) {
            logger.error("UserDbHelperJasyncSql::UserDbHelperJasyncSql()", e);
        }
//        CompletableFuture<QueryResult> future = connection.sendPreparedStatement("SELECT * FROM users LIMIT 5");
//        QueryResult result = null;
//        try {
//            result = Async.awaitFuture(future);
//            ResultSet resultSet = result.getRows();
//
//            for (Iterator<RowData> iterator = resultSet.iterator(); iterator.hasNext(); ) {
//                RowData row = iterator.next();
//                GameUserInfo gameUserInfo = new GameUserInfo();
//                gameUserInfo.setUuid(row.getString("uuid"));
//                gameUserInfo.setLoginType(row.getInt("login_type"));
//                gameUserInfo.setAppVersion(row.getString("app_version"));
//                gameUserInfo.setAppStore(row.getString("app_store"));
//                gameUserInfo.setDeviceModel(row.getString("device_model"));
//                gameUserInfo.setDeviceCountry(row.getString("device_country"));
//                gameUserInfo.setDeviceLanguage(row.getString("device_language"));
//                gameUserInfo.setNickname(row.getString("nickname"));
//                gameUserInfo.setHeart(row.getInt("heart"));
//                gameUserInfo.setCoin(row.getLong("coin"));
//                gameUserInfo.setRuby(row.getLong("ruby"));
//                gameUserInfo.setLevel(row.getInt("level"));
//                gameUserInfo.setExp(row.getLong("exp"));
//                gameUserInfo.setHighScore(row.getLong("high_score"));
//                gameUserInfo.setCurrentDeck(row.getString("current_deck"));
//                logger.debug("-----> mysql jasync-sql  : {},", gameUserInfo);
//            }
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }

    }

    /**
     * uuid로 유저 DB에서 유저 객체를 조회
     *
     * @param uuid 유저 유니크 식별자
     * @return 유저 데이터
     * @throws TimeoutException
     * @throws SuspendExecution
     */
    public GameUserInfo selectUserByUuid(String uuid) throws TimeoutException, SuspendExecution {
        // raw sql
        String sql = "SELECT * FROM users WHERE uuid = '" + uuid + "'";

        // stored procedure
//        String sql = "CALL sp_Users_Select_Uuid('" + uuid + "')";
        CompletableFuture<QueryResult> future = connection.sendQuery(sql);

        // InTransaction
//        CompletableFuture<QueryResult> future = connection.inTransaction(connection -> connection.sendQuery(sql).thenApplyAsync(queryResult -> {
//            connection.sendQuery("UPDATE users SET current_deck = 'test' WHERE uuid = '" + uuid + "'");
//            return queryResult;
//        }));

        // PreparedStatement
//        String sql = "SELECT * FROM users WHERE uuid = ?";
//        List<String> param = new ArrayList<>();
//        param.add(uuid);
//        CompletableFuture<QueryResult> future = getConnection().sendPreparedStatement(sql, param, true);

        GameUserInfo gameUserInfo = null;
        try {
            QueryResult result = Async.awaitFuture(future);

            if (result.getRows().size() == 1) {
                ResultSet resultSet = result.getRows();
                RowData row = resultSet.get(0);
                gameUserInfo = new GameUserInfo();
                gameUserInfo.setUuid(row.getString("uuid"));
                gameUserInfo.setLoginType(row.getInt("login_type"));
                gameUserInfo.setAppVersion(row.getString("app_version"));
                gameUserInfo.setAppStore(row.getString("app_store"));
                gameUserInfo.setDeviceModel(row.getString("device_model"));
                gameUserInfo.setDeviceCountry(row.getString("device_country"));
                gameUserInfo.setDeviceLanguage(row.getString("device_language"));
                gameUserInfo.setNickname(row.getString("nickname"));
                gameUserInfo.setHeart(row.getInt("heart"));
                gameUserInfo.setCoin(row.getLong("coin"));
                gameUserInfo.setRuby(row.getLong("ruby"));
                gameUserInfo.setLevel(row.getInt("level"));
                gameUserInfo.setExp(row.getLong("exp"));
                gameUserInfo.setHighScore(row.getLong("high_score"));
                gameUserInfo.setCurrentDeck(row.getString("current_deck"));

                if (logger.isDebugEnabled()) {
                    logger.debug("-----> mysql jasync-sql selectUserByUuid : {},", gameUserInfo);
                }
            }
        } catch (TimeoutException e) {
            logger.error("UserDbHelperJasyncSql::selectUserByUuid()", e);
        }

        return gameUserInfo;
    }

    /**
     * 유저 정보 DB에 저장
     *
     * @param gameUserInfo 유저 정보 전달
     * @return 저장된 레토드 수
     * @throws TimeoutException
     * @throws SuspendExecution
     */
    public int insertUser(GameUserInfo gameUserInfo) throws TimeoutException, SuspendExecution {
        // raw sql
        CompletableFuture<QueryResult> future = connection.sendQuery("INSERT INTO users (uuid, login_type, app_version, app_store, device_model, device_country, device_language, nickname, heart, coin, ruby, level, exp, high_score, current_deck, create_date, update_date) VALUES (" +
            gameUserInfo.getUuid() + ",'" +
            gameUserInfo.getLoginType() + "','" +
            gameUserInfo.getAppVersion() + "','" +
            gameUserInfo.getAppStore() + "','" +
            gameUserInfo.getDeviceModel() + "','" +
            gameUserInfo.getDeviceCountry() + "','" +
            gameUserInfo.getDeviceLanguage() + "','" +
            gameUserInfo.getNickname() + "'," +
            gameUserInfo.getHeart() + "," +
            gameUserInfo.getCoin() + "," +
            gameUserInfo.getRuby() + "," +
            gameUserInfo.getLevel() + "," +
            gameUserInfo.getExp() + "," +
            gameUserInfo.getHighScore() + ",'" +
            gameUserInfo.getCurrentDeck() + "', NOW(), NOW())");

        // stored procedure
//        CompletableFuture<QueryResult> future = connection.sendQuery(
//            "CALL sp_Users_Insert (" +
//                gameUserInfo.getUuid() + ",'" +
//                gameUserInfo.getLoginType() + "','" +
//                gameUserInfo.getAppVersion() + "','" +
//                gameUserInfo.getAppStore() + "','" +
//                gameUserInfo.getDeviceModel() + "','" +
//                gameUserInfo.getDeviceCountry() + "','" +
//                gameUserInfo.getDeviceLanguage() + "','" +
//                gameUserInfo.getNickname() + "'," +
//                gameUserInfo.getHeart() + "," +
//                gameUserInfo.getCoin() + "," +
//                gameUserInfo.getRuby() + "," +
//                gameUserInfo.getLevel() + "," +
//                gameUserInfo.getExp() + "," +
//                gameUserInfo.getHighScore() + ",'" +
//                gameUserInfo.getCurrentDeck() + "')");

        try {
            QueryResult result = Async.awaitFuture(future);

            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql jasync-sql insertUser {} : {},", result.getRowsAffected(), gameUserInfo);
            }
            return (int)result.getRowsAffected();
        } catch (TimeoutException e) {
            logger.error("UserDbHelperJasyncSql::insertUser()", e);
            return 0;
        }
    }

    /**
     * 유저 현제 가지고 있는 덱정보 저장
     *
     * @param uuid        유저 유니크 식별자
     * @param currentDeck 저장할 덱이름
     * @return 저장된 레코드 수
     * @throws TimeoutException
     * @throws SuspendExecution
     */
    public int updateUserCurrentDeck(String uuid, String currentDeck) throws TimeoutException, SuspendExecution {
        // raw sql
        CompletableFuture<QueryResult> future = connection.sendQuery("UPDATE users SET current_deck = '" + currentDeck + "' WHERE uuid = '" + uuid + "'");

        // stored procedure
//        CompletableFuture<QueryResult> future = connection.sendQuery("CALL sp_Users_Update_CurrentDeck('" + uuid + "', '" + currentDeck + "')");

        // InTransaction
//        CompletableFuture<QueryResult> future = connection.inTransaction(conn -> conn.sendQuery("UPDATE users SET current_deck = '" + currentDeck + "' WHERE uuid = '" + uuid + "'").thenApplyAsync(queryResult -> {
//        }));
//        CompletableFuture<QueryResult> future = connection.inTransaction(conn -> conn.sendQuery("UPDATE users SET current_deck = '" + currentDeck + "' WHERE uuid = '" + uuid + "'").thenApplyAsync(s -> {
//            connection.sendQuery("UPDATE users SET high_Score = 1000 WHERE uuid = '" + uuid + "'");
//            return s;
//        }));

        try {
            QueryResult result = Async.awaitFuture(future);

            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql Jasync-sql updateUserCurrentDeck {} : uuid {}, deck {},", result.getRowsAffected(), uuid, currentDeck);
            }
//            return (int)result.getRowsAffected();
            return 1;
        } catch (TimeoutException e) {
            logger.error("UserDbHelperJasyncSql::updateUserCurrentDeck()", e);
            return 0;
        }
    }

    /**
     * 유저 닉네임 수정
     *
     * @param uuid     유저 유니크 식별자
     * @param nickname 저장할 데이터
     * @return 저장된 레코드 수
     * @throws TimeoutException
     * @throws SuspendExecution
     */
    public int updateUserNickname(String uuid, String nickname) throws TimeoutException, SuspendExecution {
        // raw sql
        CompletableFuture<QueryResult> future = connection.sendQuery("UPDATE users SET nickname = '" + nickname + "' WHERE uuid = '" + uuid + "'");

        // stored procedure
//        CompletableFuture<QueryResult> future = connection.sendQuery("CALL sp_Users_Update_Nickname('" + uuid + "', '" + nickname + "')");
        try {
            QueryResult result = Async.awaitFuture(future);

            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql jasync-sql updateUserNickname {} : uuid {}, nickname {},", result.getRowsAffected(), uuid, nickname);
            }
            return (int)result.getRowsAffected();
        } catch (TimeoutException e) {
            logger.error("UserDbHelperJasyncSql::updateUserNickname()", e);
            return 0;
        }
    }

    /**
     * 유저의 최고 점수 저장
     *
     * @param uuid      유저 유니크 식별자
     * @param highScore 저장할 데이터
     * @return 저장된 레코드 수
     * @throws TimeoutException
     * @throws SuspendExecution
     */
    public int updateUserHigScore(String uuid, int highScore) throws SuspendExecution {
        // raw sql
        CompletableFuture<QueryResult> future = connection.sendQuery("UPDATE users SET high_Score = '" + highScore + "' WHERE uuid = '" + uuid + "'");

        // stored procedure
//        CompletableFuture<QueryResult> future = connection.sendQuery("CALL sp_Users_Update_HighScore('" + uuid + "', " + highScore + ")");

        // InTransaction
//        CompletableFuture<QueryResult> future = connection.inTransaction(conn -> conn.sendQuery("UPDATE users SET high_Score = '" + highScore + "' WHERE uuid = '" + uuid + "'"));

        try {
            QueryResult result = Async.awaitFuture(future);

            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql jasync-sql updateUserHigScore {} : uuid {}, highScore {},", result.getRowsAffected(), uuid, highScore);
            }
//            return (int)result.getRowsAffected();
            return 1;
        } catch (TimeoutException e) {
            logger.error("UserDbHelperJasyncSql::updateUserHigScore()", e);
            return 0;
        }
    }

    public void disconnect() {
        try {
            connection.disconnect().get();
        } catch (InterruptedException e) {
            logger.error("UserDbHelperJasyncSql::disconnect()", e);
        } catch (ExecutionException e) {
            logger.error("UserDbHelperJasyncSql::disconnect()", e);
        }
    }
}

