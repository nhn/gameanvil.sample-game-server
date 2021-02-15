package com.nhn.gameanvil.sample.db;

import co.paralleluniverse.fibers.SuspendExecution;
import com.mysql.cj.xdevapi.Result;
import com.mysql.cj.xdevapi.Row;
import com.mysql.cj.xdevapi.Session;
import com.mysql.cj.xdevapi.SessionFactory;
import com.mysql.cj.xdevapi.SqlResult;
import com.nhn.gameanvil.async.Async;
import com.nhn.gameanvil.async.Callable;
import com.nhn.gameanvil.sample.game.user.model.GameUserInfo;
import com.nhn.gameanvilcore.GameAnvilInternalThreadPool;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDbHelperMySqlX {
    private Logger logger = LoggerFactory.getLogger(getClass());

    // 커넥션풀 사용
//    private Client client;
//    private Session session;

    // 자체 커넥션풀 설정
    private final int MAX_SESSION = 10;
    private int idx = 0;
    private SessionFactory sessionFactory = new SessionFactory();
    private List<Session> sessions = new ArrayList<>();

    public UserDbHelperMySqlX(String connectionUrl) {
        // 커넥션 풀 사용 생성
//        ClientFactory cf = new ClientFactory();
//        client = cf.getClient(connectionUrl, "{\"pooling\":{\"enabled\":true, \"maxSize\":10, \"maxIdleTime\":30000, \"queueTimeout\":10000} }");
//        session = client.getSession();

//        Session session = new SessionFactory().getSession(connectionUrl);
//        sessions.add(session);
//        sessionUpdate = new SessionFactory().getSession(connectionUrl);
        logger.warn("-----> mysql x-dev api UserDbHelperMysqlX !!");

        // 커넥션 생성
        for (int i = 0; i < MAX_SESSION; ++i) {
            sessions.add(sessionFactory.getSession(connectionUrl));
        }
    }

    public Session getSession() {
        return sessions.get(idx++ % MAX_SESSION);
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
        Session session = getSession();

        // Row SQL
//        CompletableFuture<SqlResult> future = getSession().sql("SELECT * FROM users WHERE uuid = '" + uuid + "'").executeAsync();

        try {

//            SqlResult result = Async.callBlocking(GameAnvilInternalThreadPool.get(), new Callable<SqlResult>() {
//                @Override
//                public SqlResult call() throws Exception {
////                    SqlResult sqlResult = session.sql("SELECT * FROM users WHERE uuid = '" + uuid + "'").executeAsync().get();
////                    return sqlResult;
//                    return session.sql("CALL sp_Users_Select_Uuid('" + uuid + "')").executeAsync().get();
//                }
//            });

            //
            CompletableFuture<SqlResult> future = Async.callBlocking(GameAnvilInternalThreadPool.get(), new Callable<CompletableFuture<SqlResult>>() {
                @Override
                public CompletableFuture<SqlResult> call() {
                    // raw sql
//                    return session.sql("SELECT * FROM users WHERE uuid = '" + uuid + "'").executeAsync(); // Note> 이름은 Async이지만 코드 내부에서 스레드 블러킹을 유발한다! 미친!
                    // stored procedure
                    return session.sql("CALL sp_Users_Select_Uuid('" + uuid + "')").executeAsync();
                }
            });

            SqlResult result = Async.awaitFuture(future);

            Row row = result.fetchOne();
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

        // TODO case 3 : 기존 mybatis
        // Callable 형태로 Async 실행하고 결과 리턴.
//        GameUserInfo gameUserInfo = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<GameUserInfo>() {
//            @Override
//            public GameUserInfo call() throws Exception {
//                SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
//                try {
//                    UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
//                    UserDto userDto = userDataMapper.selectUserByUuid(uuid);
//                    if (userDto == null) {
//                        return null;
//                    } else {
//                        return userDto.toGameUserInfo();
//                    }
//                } finally {
//                    sqlSession.close();
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
        // Row SQL
//        CompletableFuture<SqlResult> future = getSession().sql(
//            "INSERT INTO users (uuid, login_type, app_version, app_store, device_model, device_country, device_language, nickname, heart, coin, ruby, level, exp, high_score, current_deck, create_date, update_date) VALUES (" +
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
//                gameUserInfo.getCurrentDeck() + "', NOW(), NOW())").executeAsync();
        Session session = getSession();
        try {
            CompletableFuture<SqlResult> future = Async.callBlocking(GameAnvilInternalThreadPool.get(), new Callable<CompletableFuture<SqlResult>>() {
                @Override
                public CompletableFuture<SqlResult> call() throws Exception {
                    // raw sql
//                    return session.sql(
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
//                            gameUserInfo.getCurrentDeck() + "', NOW(), NOW())").executeAsync(); // Note> 이름은 Async이지만 코드 내부에서 스레드 블러킹을 유발한다! 미친!

                    // stored procedure
                    return session.sql(
                        "CALL sp_Users_Insert (" +
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
                            gameUserInfo.getCurrentDeck() + "')").executeAsync();
                }
            });

            SqlResult result = Async.awaitFuture(future);

//            SqlResult result = Async.callBlocking(GameAnvilInternalThreadPool.get(), new Callable<SqlResult>() {
//                @Override
//                public SqlResult call() throws Exception {
////                    return session.sql(
////                        "INSERT INTO users (uuid, login_type, app_version, app_store, device_model, device_country, device_language, nickname, heart, coin, ruby, level, exp, high_score, current_deck, create_date, update_date) VALUES (" +
////                            gameUserInfo.getUuid() + ",'" +
////                            gameUserInfo.getLoginType() + "','" +
////                            gameUserInfo.getAppVersion() + "','" +
////                            gameUserInfo.getAppStore() + "','" +
////                            gameUserInfo.getDeviceModel() + "','" +
////                            gameUserInfo.getDeviceCountry() + "','" +
////                            gameUserInfo.getDeviceLanguage() + "','" +
////                            gameUserInfo.getNickname() + "'," +
////                            gameUserInfo.getHeart() + "," +
////                            gameUserInfo.getCoin() + "," +
////                            gameUserInfo.getRuby() + "," +
////                            gameUserInfo.getLevel() + "," +
////                            gameUserInfo.getExp() + "," +
////                            gameUserInfo.getHighScore() + ",'" +
////                            gameUserInfo.getCurrentDeck() + "', NOW(), NOW())").executeAsync().get(); // Note> 이름은 Async이지만 코드 내부에서 스레드 블러킹을 유발한다! 미친!
//
//                    return session.sql(
//                        "CALL sp_Users_Insert (" +
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
//                            gameUserInfo.getCurrentDeck() + "')").executeAsync().get(); // Note> 이름은 Async이지만 코드 내부에서 스레드 블러킹을 유발한다! 미친!
//
//                }
//            });

            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql x-dev api insertUser {} : {},", result.getAffectedItemsCount(), gameUserInfo);
            }
            return (int)result.getAffectedItemsCount();
        } catch (TimeoutException e) {
            logger.error("UserDbHelperService::insertUser()", e);
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

        // TODO case 3 : 기존 mybatis
        // Callable 형태로 Async 실행하고 결과 리턴.
//        Integer resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
//                try {
//                    UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
//                    int resultCount = userDataMapper.insertUser(gameUserInfo.toDtoUser());
//                    if (resultCount == 1) { // 단건 저장이기에 1개면 정상으로 디비 commit
//                        sqlSession.commit();
//                    }
//                    return resultCount;
//                } finally {
//                    sqlSession.close();
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
        Session session = getSession();

        // Row SQL
//        CompletableFuture<SqlResult> future = getSession().sql(
//            "UPDATE users SET current_deck = '" + currentDeck + "' WHERE uuid = '" + uuid + "'").executeAsync();
        try {
//            SqlResult result = Async.callBlocking(GameAnvilInternalThreadPool.get(), new Callable<SqlResult>() {
//                @Override
//                public SqlResult call() throws Exception {
////                    return session.sql(query).executeAsync(); // Note> 이름은 Async이지만 코드 내부에서 스레드 블러킹을 유발한다! 미친!
////                    try {
////                        SqlResult sqlResult = AsyncCompletionStage.get(session.sql(query).executeAsync());
////                        session.sql("BEGIN").executeAsync().get();
//                        SqlResult sqlResult = session.sql("UPDATE users SET current_deck = '" + currentDeck + "' WHERE uuid = '" + uuid + "'").executeAsync().get();
//
////                    SqlResult sqlResult = session.sql("CALL sp_Users_Update_CurrentDeck('" + uuid + "', '" + currentDeck + "')").executeAsync().get();
//
//                    //                        session.sql("COMMIT").executeAsync().get();
//                    return sqlResult;
////                    } catch (Exception e) {
////                        session.sql("ROLLBACK").executeAsync().get();
////                        throw new Exception(e);
////                    }
//                }
//            });

            CompletableFuture<SqlResult> future = Async.callBlocking(GameAnvilInternalThreadPool.get(), new Callable<CompletableFuture<SqlResult>>() {
                @Override
                public CompletableFuture<SqlResult> call() throws Exception {
//                    return session.sql("UPDATE users SET current_deck = '" + currentDeck + "' WHERE uuid = '" + uuid + "'").executeAsync(); // Note> 이름은 Async이지만 코드 내부에서 스레드 블러킹을 유발한다! 미친!
                    return session.sql("CALL sp_Users_Update_CurrentDeck('" + uuid + "', '" + currentDeck + "')").executeAsync();
                }
            });

            SqlResult result = Async.awaitFuture(future);

            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql x-dev api updateUserCurrentDeck {} : uuid {}, deck {},", result.getAffectedItemsCount(), uuid, currentDeck);
            }
//            return (int)result.getAffectedItemsCount();
            return 1;
        } catch (TimeoutException e) {
            logger.error("UserDbHelperService::updateUserCurrentDeck()", e);
            return 0;
        }

////                SqlResult result = CompletableFuture.
////                    runAsync(() -> session.sql("BEGIN").executeAsync()).
////                    thenApply(aVoid -> session.sql("UPDATE users SET current_deck = '" + currentDeck + "' WHERE uuid = '" + uuid + "'").executeAsync()).
////                    thenApply(sqlResult -> {
////                        session.sql("COMMIT").executeAsync();
////                        return sqlResult;
////                    }).exceptionally(throwable -> {
////                    session.sql("ROLLBACK").executeAsync();
////                    return null;
////                }).get().get();

        // TODO case 3 : 기존 mybatis
        // Callable 형태로 Async 실행하고 결과 리턴.
//        Integer resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
//                UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
//                try {
//                    int resultCount = userDataMapper.updateUserCurrentDeck(uuid, currentDeck);
//                    if (resultCount == 1) {
//                        sqlSession.commit();
//                    }
//                    return resultCount;
//                } finally {
//                    sqlSession.close();
//                }
//            }
//        });
//        return resultCount;
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
        // Row SQL
//        CompletableFuture<SqlResult> future = getSession().sql(
//            "UPDATE users SET nickname = '" + nickname + "' WHERE uuid = '" + uuid + "'").executeAsync();

        Session session = getSession();
        try {
            CompletableFuture<SqlResult> future = Async.callBlocking(GameAnvilInternalThreadPool.get(), new Callable<CompletableFuture<SqlResult>>() {
                @Override
                public CompletableFuture<SqlResult> call() throws Exception {
//                    return session.sql("UPDATE users SET nickname = '" + nickname + "' WHERE uuid = '" + uuid + "'").executeAsync(); // Note> 이름은 Async이지만 코드 내부에서 스레드 블러킹을 유발한다! 미친!
                    return session.sql("CALL sp_Users_Update_Nickname('" + uuid + "', '" + nickname + "')").executeAsync();
                }
            });

            SqlResult result = Async.awaitFuture(future);

//            SqlResult result = Async.callBlocking(GameAnvilInternalThreadPool.get(), new Callable<SqlResult>() {
//                @Override
//                public SqlResult call() throws Exception {
////                    return session.sql("UPDATE users SET nickname = '" + nickname + "' WHERE uuid = '" + uuid + "'").executeAsync().get(); // Note> 이름은 Async이지만 코드 내부에서 스레드 블러킹을 유발한다! 미친!
//                    return session.sql("CALL sp_Users_Update_Nickname('" + uuid + "', '" + nickname + "')").executeAsync().get(); // Note> 이름은 Async이지만 코드 내부에서 스레드 블러킹을 유발한다! 미친!
//                }
//            });

            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql x-dev api updateUserNickname {} : uuid {}, nickname {},", result.getAffectedItemsCount(), uuid, nickname);
            }
            return (int)result.getAffectedItemsCount();
        } catch (TimeoutException e) {
            logger.error("UserDbHelperService::updateUserNickname()", e);
            return 0;
        }

        // TODO case 3 : 기존 mybatis
        // Callable 형태로 Async 실행하고 결과 리턴.
//        Integer resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
//                UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
//                try {
//                    int resultCount = userDataMapper.updateUserNickname(uuid, nickname);
//                    if (resultCount == 1) {
//                        sqlSession.commit();
//                    }
//                    return resultCount;
//                } finally {
//                    sqlSession.close();
//                }
//            }
//        });
//        return resultCount;
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

        // Row SQL
//        CompletableFuture<SqlResult> future = getSession().sql("UPDATE users SET high_Score = '" + highScore + "' WHERE uuid = '" + uuid + "'").executeAsync();

//        Table table = session.getDefaultSchema().getTable("users");
//        CompletableFuture<Result> future = table.update().set("high_Score", Integer.valueOf(highScore)).where("uuid = :uuid").bind("uuid", uuid).executeAsync();

        Session session = getSession();
        try {
            CompletableFuture<SqlResult> future = Async.callBlocking(GameAnvilInternalThreadPool.get(), new Callable<CompletableFuture<SqlResult>>() {
                @Override
                public CompletableFuture<SqlResult> call() throws Exception {
//                    return session.sql("UPDATE users SET high_Score = '" + highScore + "' WHERE uuid = '" + uuid + "'").executeAsync(); // Note> 이름은 Async이지만 코드 내부에서 스레드 블러킹을 유발한다! 미친!
                    return session.sql("CALL sp_Users_Update_HighScore('" + uuid + "', " + highScore + ")").executeAsync();
                }
            });

            Result result = Async.awaitFuture(future);

//            SqlResult result = Async.callBlocking(GameAnvilInternalThreadPool.get(), new Callable<SqlResult>() {
//                @Override
//                public SqlResult call() throws Exception {
////                    return session.sql("UPDATE users SET high_Score = '" + highScore + "' WHERE uuid = '" + uuid + "'").executeAsync().get(); // Note> 이름은 Async이지만 코드 내부에서 스레드 블러킹을 유발한다! 미친!
//                    return session.sql("CALL sp_Users_Update_HighScore('" + uuid + "', " + highScore + ")").executeAsync().get(); // Note> 이름은 Async이지만 코드 내부에서 스레드 블러킹을 유발한다! 미친!
//
//                }
//            });

            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql x-dev api updateUserHigScore {} : uuid {}, highScore {},", result.getAffectedItemsCount(), uuid, highScore);
            }
            return (int)result.getAffectedItemsCount();
        } catch (TimeoutException e) {
            logger.error("UserDbHelperService::updateUserHigScore()", e);
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

        // TODO case 3 : 기존 mybatis
        // Callable 형태로 Async 실행하고 결과 리턴.
//        Integer resultCount = 0;
//
//        try {
//            resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
//                @Override
//                public Integer call() throws Exception {
//                    SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
//                    UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
//                    try {
//                        int resultCount = userDataMapper.updateUserHighScore(uuid, highScore);
//                        if (resultCount == 1) {
//                            sqlSession.commit();
//                        }
//                        return resultCount;
//                    } finally {
//                        sqlSession.close();
//                    }
//                }
//            });
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }
//
//        return resultCount;
    }


    public void closeClient() {
//        session.close();
//        client.close();

        for (int i = 0; i < MAX_SESSION; ++i) {
            sessions.get(i).close();
        }
    }
}
