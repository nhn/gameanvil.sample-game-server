package com.nhn.gameanvil.sample.db.xdevapi;

import co.paralleluniverse.fibers.SuspendExecution;
import com.mysql.cj.xdevapi.Client;
import com.mysql.cj.xdevapi.ClientFactory;
import com.mysql.cj.xdevapi.Result;
import com.mysql.cj.xdevapi.Row;
import com.mysql.cj.xdevapi.RowResult;
import com.mysql.cj.xdevapi.Session;
import com.mysql.cj.xdevapi.SqlResult;
import com.mysql.cj.xdevapi.Table;
import com.nhn.gameanvil.async.Async;
import com.nhn.gameanvil.sample.game.user.model.GameUserInfo;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDbHelper {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String connectionUrl;
    private Client client;
    private Session session;

    public UserDbHelper(String connectionUrl) {
//        String connectionUrl = "mysqlx://kevin:kevin1234@localhost:33060/taptap";               // Note> connection string 두 방식 모두 가능
//        String connectionUrl = "mysqlx://localhost:33060/taptap?user=kevin&password=kevin1234"; // Note> connection string 두 방식 모두 가능
        this.connectionUrl = connectionUrl;

        ClientFactory cf = new ClientFactory();
        client = cf.getClient(connectionUrl, "{\"pooling\":{\"enabled\":true, \"maxSize\":25, \"maxIdleTime\":30000, \"queueTimeout\":10000} }");
        session = client.getSession();
//        Session session = new SessionFactory().getSession(connectionUrl); // Note> connection pooling 없이 사용하는 방법
        logger.warn("-----> mysql x-dev api UserDbHelper !!");

//        //session.sql("USE taptap"); // Note> Java에서는 이런식으로 schema 변경 가능
//
//        // Note> sql() 사용
//        CompletableFuture<SqlResult> future = session.sql("select * from users").executeAsync();
//
//        try {
//            SqlResult result = Async.awaitFuture(future);
//            List<Row> rows = result.fetchAll();
//
//            for (Row row : rows)
//                logger.warn("-----> mysql x-dev api: {}, {}, {}, {}", row.getString("uuid"), row.getString("app_version"), row.getString("device_country"), row.getString("nickname"));
//
//        } catch (TimeoutException e) {
//            logger.error("GameNode::onInit()", e);
//        }

// 성능 시간 비교 테스트
//        ClientFactory cf = new ClientFactory();
//        String connectionUrl = "mysqlx://localhost:33060/taptap?user=kevin&password=kevin1234"; // 로컬 mysql 8.0
////        String connectionUrl = "mysqlx://localhost:33060/taptap?xdevapi.ssl-mode=DISABLED&user=root&password=1234";  // 로컬 mysql 5.7
//        Client cli = cf.getClient(connectionUrl, "{\"pooling\":{\"enabled\":true, \"maxSize\":100, \"maxIdleTime\":30000, \"queueTimeout\":10000} }");
//
//        // case 1 : x dev api async UPDATE 1건 -------------------------------------------------------------
//        long startTime = System.currentTimeMillis();
//        Session session = cli.getSession();
//
//        // Row SQL
//        CompletableFuture<SqlResult> future = session.sql("SELECT * FROM users LIMIT 1").executeAsync();
//        try {
//            SqlResult result = Async.awaitFuture(future);
//
//            List<Row> row = result.fetchAll();
//            long endTime = System.currentTimeMillis() - startTime;
//            logger.warn("--> mysql x dev api executeAsync selectUser : result {}, elapsed {} sec", row.size(), endTime / 1000.0);
//        } catch (TimeoutException e) {
//            logger.error("UserDbHelperService::selectUserByUuid()", e);
//        }
//        // ------------------------------------------------------------------------------------------------
//
//        // case 2 : x dev api sync SELECT 1건 -------------------------------------------------------------
//        startTime = System.currentTimeMillis();
//        SqlResult sqlResult = session.sql("SELECT * FROM users LIMIT 1").execute();
//        List<Row> row = sqlResult.fetchAll();
//        long endTime = System.currentTimeMillis() - startTime;
//        logger.warn("--> mysql x dev api execute selectUser : result {}, elapsed {} sec", row.size(), endTime / 1000.0);
//        // ------------------------------------------------------------------------------------------------
//
//        // case 3 : x dev api async UPDATE 1건 ------------------------------------------------------------
//        startTime = System.currentTimeMillis();
//        int highScore = (int)(startTime % 1000);
//        session.startTransaction();
//        // Row SQL
//        future = session.sql("UPDATE users SET high_Score = " + highScore + " WHERE uuid = '022fe8bf-5dd6-11ea-829b-10e7c63d98c01'").executeAsync();
//        try {
//            SqlResult result = Async.awaitFuture(future);
//            session.commit();
//            endTime = System.currentTimeMillis() - startTime;
//            logger.warn("--> mysql x dev api executeAsync updateUserHigScore : result {}, elapsed {} sec", result.getAffectedItemsCount(), endTime / 1000.0);
//        } catch (TimeoutException e) {
//            logger.error("UserDbHelperService::updateUserHigScore()", e);
//            session.rollback();
//        }
//        // ------------------------------------------------------------------------------------------------
//
//        // case 4 : x dev api sync UPDATE 1건 -------------------------------------------------------------
//        startTime = System.currentTimeMillis();
//        session.startTransaction();
//        highScore = (int)(startTime % 1000);
//        // Row SQL
//        sqlResult = session.sql("UPDATE users SET high_Score = " + highScore + " WHERE uuid = '022fe8bf-5dd6-11ea-829b-10e7c63d98c01'").execute();
//        session.commit();
//        endTime = System.currentTimeMillis() - startTime;
//        logger.warn("--> mysql x dev api execute updateUserHigScore : result {}, elapsed {} sec", sqlResult.getAffectedItemsCount(), endTime / 1000.0);
//        // ------------------------------------------------------------------------------------------------
//
//        // case 5 : 기존 jdbc sync SELECT 1건 -------------------------------------------------------------
//        try {
//            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/taptap?serverTimezone=UTC", "kevin", "kevin1234");
//            startTime = System.currentTimeMillis();
//            Statement stmt = conn.createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT * FROM users LIMIT 1");
//            endTime = System.currentTimeMillis() - startTime;
//            int resultCount = 0;
//            while (rs.next()) {
//                resultCount++;
//            }
//            logger.warn("--> mysql selectUser : result {}, elapsed {} sec", resultCount, endTime / 1000.0);
//            stmt.close();
//            conn.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        // ------------------------------------------------------------------------------------------------
//
//        // case 6 : 기존 jdbc sync UPDATE 1건 -------------------------------------------------------------
//        try {
//            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/taptap?serverTimezone=UTC", "kevin", "kevin1234");
//            startTime = System.currentTimeMillis();
//            Statement stmt = conn.createStatement();
//            highScore = (int)(startTime % 1000);
//            int resultCount = stmt.executeUpdate("UPDATE users SET high_Score = " + highScore + " WHERE uuid = '022fe8bf-5dd6-11ea-829b-10e7c63d98c01'");
//            endTime = System.currentTimeMillis() - startTime;
//            logger.warn("--> mysql updateUserHigScore : result {}, elapsed {} sec", resultCount, endTime / 1000.0);
////            conn.commit();
//            stmt.close();
//            conn.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        // ------------------------------------------------------------------------------------------------
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
        // TODO case1 : x dev api async 처리

        Table table = session.getDefaultSchema().getTable("users");
        CompletableFuture<RowResult> future = table.select().where("uuid = :param").bind("param", uuid).executeAsync();

        // Row SQL
//        CompletableFuture<SqlResult> future = session.sql("SELECT * FROM users WHERE uuid = '" + uuid + "'").executeAsync();
        try {
//            SqlResult result = Async.awaitFuture(future);

            RowResult rowResult = Async.awaitFuture(future);

            //          Row row = result.fetchOne();
            Row row = rowResult.fetchOne();
            if (row == null) {
                return null;
            }
            GameUserInfo gameUserInfo = new GameUserInfo();
            gameUserInfo.setUuid(row.getString("uuid"));
            gameUserInfo.setLoginType(row.getInt("login_type"));
            gameUserInfo.setAppVersion(row.getString("app_version"));
            gameUserInfo.setAppStore(row.getString("app_store"));
            gameUserInfo.setDeviceModel(row.getString("device_model"));
            gameUserInfo.setDeviceCountry(row.getString("device_country"));
            gameUserInfo.setDeviceLanguage(row.getString("device_language"));
            gameUserInfo.setNickname(row.getString("nickname"));
            gameUserInfo.setHeart(row.getInt("heart"));
            gameUserInfo.setCoin(row.getInt("coin"));
            gameUserInfo.setRuby(row.getLong("ruby"));
            gameUserInfo.setLevel(row.getInt("level"));
            gameUserInfo.setExp(row.getLong("exp"));
            gameUserInfo.setHighScore(row.getLong("high_score"));
            gameUserInfo.setCurrentDeck(row.getString("current_deck"));
            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql x-dev api selectUserByUuid{} : {},", uuid, gameUserInfo);
            }
            return gameUserInfo;
        } catch (TimeoutException e) {
            logger.error("UserDbHelperService::selectUserByUuid()", e);
            return null;
        }

        // TODO case 2 : x dev api sync
//        GameUserInfo gameUserInfo = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<GameUserInfo>() {
//            @Override
//            public GameUserInfo call() throws Exception {
//                ClientFactory cf = new ClientFactory();
//                String connectionUrl = "mysqlx://localhost:33060/taptap?xdevapi.ssl-mode=DISABLED&user=root&password=1234";  // Note> connection string 두 방식 모두 가능
//                Client cli = cf.getClient(connectionUrl, "{\"pooling\":{\"enabled\":true, \"maxSize\":100, \"maxIdleTime\":30000, \"queueTimeout\":10000} }");
//
//                Session session = cli.getSession();
//
//                // Row SQL
//                try {
//                    SqlResult result = session.sql("SELECT * FROM users WHERE uuid = " + uuid).execute();
//
//                    Row row = result.fetchOne();
//                    if (row == null) {
//                        return null;
//                    }
//                    GameUserInfo gameUserInfo = new GameUserInfo();
//                    gameUserInfo.setUuid(row.getString("uuid"));
//                    gameUserInfo.setLoginType(row.getInt("login_type"));
//                    gameUserInfo.setAppVersion(row.getString("app_version"));
//                    gameUserInfo.setAppStore(row.getString("app_store"));
//                    gameUserInfo.setDeviceModel(row.getString("device_model"));
//                    gameUserInfo.setDeviceCountry(row.getString("device_country"));
//                    gameUserInfo.setDeviceLanguage(row.getString("device_language"));
//                    gameUserInfo.setNickname(row.getString("nickname"));
//                    gameUserInfo.setHeart(row.getInt("heart"));
//                    gameUserInfo.setCoin(row.getInt("coin"));
//                    gameUserInfo.setRuby(row.getLong("ruby"));
//                    gameUserInfo.setLevel(row.getInt("level"));
//                    gameUserInfo.setExp(row.getLong("exp"));
//                    gameUserInfo.setHighScore(row.getLong("high_score"));
//                    gameUserInfo.setCurrentDeck(row.getString("current_deck"));
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("-----> mysql x-dev api selectUserByUuid{} : {},", uuid, gameUserInfo);
//                    }
//                    return gameUserInfo;
//                } finally {
//                    session.close();
//                }
//            }
//        });
//        return gameUserInfo;
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
        // TODO case1 : x dev api async 처리

        session.startTransaction();
        // Row SQL
        CompletableFuture<SqlResult> future = session.sql(
            "INSERT INTO users (uuid, login_type, app_version, app_store, device_model, device_country, device_language, nickname, heart, coin, ruby, level, exp, high_score, current_deck, create_date, update_date) VALUES (" +
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
                gameUserInfo.getCurrentDeck() + "', NOW(), NOW())").executeAsync();

        try {
            SqlResult result = Async.awaitFuture(future);

            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql x-dev api insertUser {} : {},", result.getAffectedItemsCount(), gameUserInfo);
            }
            session.commit();
            return (int)result.getAffectedItemsCount();
        } catch (TimeoutException e) {
            logger.error("UserDbHelperService::insertUser()", e);
            session.rollback();
            return 0;
        }

        // TODO case 2 : x dev api sync
//        Integer resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                Session session = client.getSession();
//
//                // Row SQL
//                try {
//                    SqlResult result = session.sql(
//                        "INSERT INTO users (uuid, login_type, app_version, app_store, device_model, device_country, device_language, nickname, heart, coin, ruby, level, exp, high_score, current_deck, create_date, update_date) VALUES (" +
//                            gameUserInfo.getUuid() + ",'" +
//                            gameUserInfo.getLoginType() + "','" +
//                            gameUserInfo.getAppVersion() + "','" +
//                            gameUserInfo.getAppStore() + "','" +
//                            gameUserInfo.getDeviceModel() + "','" +
//                            gameUserInfo.getDeviceCountry() + "','" +
//                            gameUserInfo.getDeviceLanguage() + "','" +
//                            gameUserInfo.getNickname() + "'," +
//                            gameUserInfo.getHeart() + "," +
//                            gameUserInfo.getCoin() + "," +
//                            gameUserInfo.getRuby() + "," +
//                            gameUserInfo.getLevel() + "," +
//                            gameUserInfo.getExp() + "," +
//                            gameUserInfo.getHighScore() + ",'" +
//                            gameUserInfo.getCurrentDeck() + "', NOW(), NOW())").execute();
//
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("-----> mysql x-dev api insertUser {} : {},", result.getAffectedItemsCount(), gameUserInfo);
//                    }
//                    session.commit();
//                    return (int)result.getAffectedItemsCount();
//                } finally {
//                    session.close();
//                }
//            }
//        });
//        return resultCount;
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
        // TODO case1 : x dev api async 처리

        session.startTransaction();
        // Row SQL
        CompletableFuture<SqlResult> future = session.sql(
            "UPDATE users SET current_deck = '" + currentDeck + "' WHERE uuid = '" + uuid + "'").executeAsync();
        try {
            SqlResult result = Async.awaitFuture(future);

            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql x-dev api updateUserCurrentDeck {} : uuid {}, deck {},", result.getAffectedItemsCount(), uuid, currentDeck);
            }
            session.commit();
            return (int)result.getAffectedItemsCount();
        } catch (TimeoutException e) {
            logger.error("UserDbHelperService::updateUserCurrentDeck()", e);
            session.rollback();
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
        // TODO case1 : x dev api async 처리

        session.startTransaction();
        // Row SQL
        CompletableFuture<SqlResult> future = session.sql(
            "UPDATE users SET nickname = '" + nickname + "' WHERE uuid = '" + uuid + "'").executeAsync();
        try {
            SqlResult result = Async.awaitFuture(future);

            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql x-dev api updateUserNickname {} : uuid {}, nickname {},", result.getAffectedItemsCount(), uuid, nickname);
            }
            session.commit();
            return (int)result.getAffectedItemsCount();
        } catch (TimeoutException e) {
            logger.error("UserDbHelperService::updateUserNickname()", e);
            session.rollback();
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
        // TODO case1 : x dev api async 처리

        session.startTransaction();
        // Row SQL
//        CompletableFuture<SqlResult> future = session.sql(
//            "UPDATE users SET high_Score = '" + highScore + "' WHERE uuid = '" + uuid + "'").executeAsync();

        Table table = session.getDefaultSchema().getTable("users");
        CompletableFuture<Result> future = table.update().set("high_Score", Integer.valueOf(highScore)).where("uuid = :uuid").bind("uuid", uuid).executeAsync();

        try {
//            SqlResult result = Async.awaitFuture(future);
            Result result = Async.awaitFuture(future);
            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql x-dev api updateUserHigScore {} : uuid {}, highScore {},", result.getAffectedItemsCount(), uuid, highScore);
            }
            session.commit();
            return (int)result.getAffectedItemsCount();
        } catch (TimeoutException e) {
            logger.error("UserDbHelperService::updateUserHigScore()", e);
            session.rollback();
            return 0;
        }

        // TODO case 2 : x dev api sync
//        Integer resultCount = 0;
//        try {
//            resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
//                @Override
//                public Integer call() throws Exception {
//                    ClientFactory cf = new ClientFactory();
//                    String connectionUrl = "mysqlx://localhost:33060/taptap?xdevapi.ssl-mode=DISABLED&user=root&password=1234";  // Note> connection string 두 방식 모두 가능
//                    Client cli = cf.getClient(connectionUrl, "{\"pooling\":{\"enabled\":true, \"maxSize\":100, \"maxIdleTime\":30000, \"queueTimeout\":10000} }");
//
//                    Session session = cli.getSession();
///
//                    // Row SQL
//                    try {
//                        SqlResult result = session.sql(
//                            "UPDATE users SET high_Score = '" + highScore + "' WHERE uuid = '" + uuid + "'").execute();
//
//                        if (logger.isDebugEnabled()) {
//                            logger.debug("-----> mysql x-dev api updateUserHigScore {} : uuid {}, highScore {},", result.getAffectedItemsCount(), uuid, highScore);
//                        }
//                        session.commit();
//                        return (int)result.getAffectedItemsCount();
//                    } finally {
//                        session.close();
//                    }
//                }
//            });
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }
//        return resultCount;
    }

    public Session getSession() {
        return session;
    }

    public void closeClient() {
        session.close();
        client.close();
    }
}