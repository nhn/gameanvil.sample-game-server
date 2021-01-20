package com.nhn.gameanvil.sample.db;

import co.paralleluniverse.fibers.SuspendExecution;
import com.mysql.cj.xdevapi.Client;
import com.mysql.cj.xdevapi.ClientFactory;
import com.mysql.cj.xdevapi.Row;
import com.mysql.cj.xdevapi.Session;
import com.mysql.cj.xdevapi.SqlResult;
import com.nhn.gameanvil.async.Async;
import com.nhn.gameanvil.sample.game.user.model.GameUserInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDbHelper {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String connectionUrl;
    private Client client;

    public UserDbHelper(String connectionUrl) {
        this.connectionUrl = connectionUrl;

        ClientFactory cf = new ClientFactory();
        client = cf.getClient(connectionUrl, "{\"pooling\":{\"enabled\":true, \"maxSize\":300, \"maxIdleTime\":30000, \"queueTimeout\":10000} }");
//        List<Session> sessions = new ArrayList<>();
//        for (int i = 0; i < 300; ++i) {
//            sessions.add(client.getSession());
//        }
//
//        for (Session session : sessions) {
//            session.close();
//        }

        logger.warn("-----> mysql x-dev api UserDbHelper !!");
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
        Session session = client.getSession();

        // Row SQL
        CompletableFuture<SqlResult> future = session.sql("SELECT * FROM users WHERE uuid = '" + uuid + "'").executeAsync();
        try {
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
        } finally {
            session.close();
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
        Session session = client.getSession();

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
        } finally {
            session.close();
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
        Session session = client.getSession();

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
        } finally {
            session.close();
        }

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
        Session session = client.getSession();

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
        } finally {
            session.close();
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
        Session session = client.getSession();

        // Row SQL
        CompletableFuture<SqlResult> future = session.sql(
            "UPDATE users SET high_Score = '" + highScore + "' WHERE uuid = '" + uuid + "'").executeAsync();
        try {
            SqlResult result = Async.awaitFuture(future);

            if (logger.isDebugEnabled()) {
                logger.debug("-----> mysql x-dev api updateUserHigScore {} : uuid {}, highScore {},", result.getAffectedItemsCount(), uuid, highScore);
            }
            session.commit();
            return (int)result.getAffectedItemsCount();
        } catch (TimeoutException e) {
            logger.error("UserDbHelperService::updateUserHigScore()", e);
            session.rollback();
            return 0;
        } finally {
            session.close();
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
        client.close();
    }
}
