package com.nhn.gameanvil.sample.mybatis;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.async.Async;
import com.nhn.gameanvil.async.Callable;
import com.nhn.gameanvil.sample.common.GameConstants;
import com.nhn.gameanvil.sample.game.user.model.GameUserInfo;
import com.nhn.gameanvil.sample.mybatis.dto.UserDto;
import com.nhn.gameanvil.sample.mybatis.mappers.UserDataMapper;
import java.util.concurrent.TimeoutException;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 유저 DB처리하는 서비스
 */
public enum UserDbHelperService {
    INSTANCE;

    public static UserDbHelperService getInstance() {
        return INSTANCE;
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * uuid로 유저 DB에서 유저 객체를 조회
     *
     * @param uuid 유저 유니크 식별자
     * @return 유저 데이터
     * @throws TimeoutException
     * @throws SuspendExecution
     */
    public GameUserInfo selectUserByUuid(String uuid) throws TimeoutException, SuspendExecution {
//        Session session = GameSessionFactory.getSession();
//
//        // Row SQL
//        CompletableFuture<SqlResult> future = session.sql("SELECT * FROM users WHERE uuid = " + uuid).executeAsync();
//        try {
//            SqlResult result = Async.awaitFuture(future);
//
//            Row row = result.fetchOne();
//            if (row == null) {
//                return null;
//            }
//            GameUserInfo gameUserInfo = new GameUserInfo();
//            gameUserInfo.setUuid(row.getString("uuid"));
//            gameUserInfo.setLoginType(row.getInt("login_type"));
//            gameUserInfo.setAppVersion(row.getString("app_version"));
//            gameUserInfo.setAppStore(row.getString("app_store"));
//            gameUserInfo.setDeviceModel(row.getString("device_model"));
//            gameUserInfo.setDeviceCountry(row.getString("device_country"));
//            gameUserInfo.setDeviceLanguage(row.getString("device_language"));
//            gameUserInfo.setNickname(row.getString("nickname"));
//            gameUserInfo.setHeart(row.getInt("heart"));
//            gameUserInfo.setCoin(row.getInt("coin"));
//            gameUserInfo.setRuby(row.getLong("ruby"));
//            gameUserInfo.setLevel(row.getInt("level"));
//            gameUserInfo.setExp(row.getLong("exp"));
//            gameUserInfo.setHighScore(row.getLong("high_score"));
//            gameUserInfo.setCurrentDeck(row.getString("current_deck"));
//            if (logger.isDebugEnabled()) {
//                logger.debug("-----> mysql x-dev api selectUserByUuid{} : {},", uuid, gameUserInfo);
//            }
//            return gameUserInfo;
//        } catch (TimeoutException e) {
//            logger.error("UserDbHelperService::selectUserByUuid()", e);
//            return null;
//        } finally {
//            session.close();
//        }

        // Callable 형태로 Async 실행하고 결과 리턴.
        GameUserInfo gameUserInfo = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<GameUserInfo>() {
            @Override
            public GameUserInfo call() throws Exception {
                SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
                try {
                    UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
                    UserDto userDto = userDataMapper.selectUserByUuid(uuid);
//                    UserDto userDto = userDataMapper.selectUserByUuidSP(uuid);
//                    logger.info("Mybatis selectUserByUuid userDto {}", userDto);
                    if (userDto == null) {
                        return null;
                    } else {
                        return userDto.toGameUserInfo();
                    }
                } finally {
                    sqlSession.close();
                }
            }
        });
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
//        Session session = GameSessionFactory.getSession();
//
//        // Row SQL
//        CompletableFuture<SqlResult> future = session.sql(
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
//
//        try {
//            SqlResult result = Async.awaitFuture(future);
//
//            if (logger.isDebugEnabled()) {
//                logger.debug("-----> mysql x-dev api insertUser {} : {},", result.getAffectedItemsCount(), gameUserInfo);
//            }
//            session.commit();
//            return (int)result.getAffectedItemsCount();
//        } catch (TimeoutException e) {
//            logger.error("UserDbHelperService::insertUser()", e);
//            session.rollback();
//            return 0;
//        } finally {
//            session.close();
//        }

        // Callable 형태로 Async 실행하고 결과 리턴.
        Integer resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
                try {
                    UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
                    int resultCount = userDataMapper.insertUser(gameUserInfo.toDtoUser());
//                    int resultCount = userDataMapper.insertUserSP(gameUserInfo.toDtoUser());
//                    logger.info("Mybatis insertUser resultCount {}", resultCount);
                    if (resultCount == 1) { // 단건 저장이기에 1개면 정상으로 디비 commit
                        sqlSession.commit();
                    }
                    return resultCount;
                } finally {
                    sqlSession.close();
                }
            }
        });
        return resultCount;
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
//        Session session = GameSessionFactory.getSession();
//
//        // Row SQL
//        CompletableFuture<SqlResult> future = session.sql(
//            "UPDATE users SET current_deck = '" + currentDeck + "' WHERE uuid = '" + uuid + "'").executeAsync();
//        try {
//            SqlResult result = Async.awaitFuture(future);
//
//            if (logger.isDebugEnabled()) {
//                logger.debug("-----> mysql x-dev api updateUserCurrentDeck {} : uuid {}, deck {},", result.getAffectedItemsCount(), uuid, currentDeck);
//            }
//            session.commit();
//            return (int)result.getAffectedItemsCount();
//        } catch (TimeoutException e) {
//            logger.error("UserDbHelperService::updateUserCurrentDeck()", e);
//            session.rollback();
//            return 0;
//        } finally {
//            session.close();
//        }

        // Callable 형태로 Async 실행하고 결과 리턴.
        Integer resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
                UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
                try {
                    int resultCount = userDataMapper.updateUserCurrentDeck(uuid, currentDeck);
//                    int resultCount = userDataMapper.updateUserCurrentDeckSP(uuid, currentDeck);
 //                   logger.info("Mybatis updateUserCurrentDeck resultCount {}", resultCount);

                    if (resultCount == 1) {
                        sqlSession.commit();
                    }
                    return resultCount;
                } finally {
                    sqlSession.close();
                }
            }
        });
        return resultCount;
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
//        Session session = GameSessionFactory.getSession();
//
//        // Row SQL
//        CompletableFuture<SqlResult> future = session.sql(
//            "UPDATE users SET nickname = '" + nickname + "' WHERE uuid = '" + uuid + "'").executeAsync();
//        try {
//            SqlResult result = Async.awaitFuture(future);
//
//            if (logger.isDebugEnabled()) {
//                logger.debug("-----> mysql x-dev api updateUserNickname {} : uuid {}, nickname {},", result.getAffectedItemsCount(), uuid, nickname);
//            }
//            session.commit();
//            return (int)result.getAffectedItemsCount();
//        } catch (TimeoutException e) {
//            logger.error("UserDbHelperService::updateUserNickname()", e);
//            session.rollback();
//            return 0;
//        } finally {
//            session.close();
//        }

        // Callable 형태로 Async 실행하고 결과 리턴.
        Integer resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
                UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
                try {
                    int resultCount = userDataMapper.updateUserNickname(uuid, nickname);
//                    int resultCount = userDataMapper.updateUserNicknameSP(uuid, nickname);
//                    logger.info("Mybatis updateUserNickname resultCount {}", resultCount);

                    if (resultCount == 1) {
                        sqlSession.commit();
                    }
                    return resultCount;
                } finally {
                    sqlSession.close();
                }
            }
        });
        return resultCount;
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
//        Session session = GameSessionFactory.getSession();
//
//        // Row SQL
//        CompletableFuture<SqlResult> future = session.sql(
//            "UPDATE users SET high_Score = '" + highScore + "' WHERE uuid = '" + uuid + "'").executeAsync();
//        try {
//            SqlResult result = Async.awaitFuture(future);
//
//            if (logger.isDebugEnabled()) {
//                logger.debug("-----> mysql x-dev api updateUserHigScore {} : uuid {}, highScore {},", result.getAffectedItemsCount(), uuid, highScore);
//            }
//            session.commit();
//            return (int)result.getAffectedItemsCount();
//        } catch (TimeoutException e) {
//            logger.error("UserDbHelperService::updateUserHigScore()", e);
//            session.rollback();
//            return 0;
//        } finally {
//            session.close();
//        }

        // Callable 형태로 Async 실행하고 결과 리턴.
        Integer resultCount = 0;

        try {
            resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
                    UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
                    try {
                        int resultCount = userDataMapper.updateUserHighScore(uuid, highScore);
//                        int resultCount = userDataMapper.updateUserHighScoreSP(uuid, highScore);
//                        logger.info("Mybatis updateUserHigScore resultCount {}", resultCount);

                        if (resultCount == 1) {
                            sqlSession.commit();
                        }
//                        return resultCount;
                        return 1;
                    } finally {
                        sqlSession.close();
                    }
                }
            });
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return resultCount;
    }
}
