package com.nhn.gameanvil.sample.db.mybatis;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.async.Async;
import com.nhn.gameanvil.async.Callable;
import com.nhn.gameanvil.sample.common.GameConstants;
import com.nhn.gameanvil.sample.db.mybatis.dto.UserDto;
import com.nhn.gameanvil.sample.db.mybatis.mappers.UserDataMapper;
import com.nhn.gameanvil.sample.game.user.model.GameUserInfo;
import java.util.concurrent.TimeoutException;
import org.apache.ibatis.session.SqlSession;

/**
 * 유저 DB처리하는 싱글톤 서비스
 */
public enum UserDbHelperService {
    INSTANCE;

    public static UserDbHelperService getInstance() {
        return INSTANCE;
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
        // Callable 형태로 Async 실행하고 결과 리턴.
        GameUserInfo gameUserInfo = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<GameUserInfo>() {
            @Override
            public GameUserInfo call() throws Exception {
                SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
                try {
                    UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
                    UserDto userDto = userDataMapper.selectUserByUuid(uuid);
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
     * @return 저장된 레코드 수 반환
     * @throws TimeoutException 해당 호출에 대해 timeout 이 발생 할 수 있음을 의미
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    public int insertUser(GameUserInfo gameUserInfo) throws TimeoutException, SuspendExecution {
        // Callable 형태로 Async 실행하고 결과 리턴.
        Integer resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
                try {
                    UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
                    int resultCount = userDataMapper.insertUser(gameUserInfo.toDtoUser());
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
     * @param currentDeck 수정 할 덱이름
     * @return 수정된 레코드 수 반환
     * @throws TimeoutException 해당 호출에 대해 timeout 이 발생 할 수 있음을 의미
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    public int updateUserCurrentDeck(String uuid, String currentDeck) throws TimeoutException, SuspendExecution {
        // Callable 형태로 Async 실행하고 결과 리턴.
        Integer resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
                UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
                try {
                    int resultCount = userDataMapper.updateUserCurrentDeck(uuid, currentDeck);
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
     * @param nickname 수정 할 닉네임
     * @return 수정된 레코드 수
     * @throws TimeoutException 해당 호출에 대해 timeout 이 발생 할 수 있음을 의미
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    public int updateUserNickname(String uuid, String nickname) throws TimeoutException, SuspendExecution {
        // Callable 형태로 Async 실행하고 결과 리턴.
        Integer resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
                UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
                try {
                    int resultCount = userDataMapper.updateUserNickname(uuid, nickname);
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
     * @param highScore 수정할 최고 점수
     * @return 수정된 레코드 수 반환
     * @throws TimeoutException 해당 호출에 대해 timeout 이 발생 할 수 있음을 의미
     * @throws SuspendExecution 이 메서드는 파이버를 suspend할 수 있음을 의미
     */
    public int updateUserHigScore(String uuid, int highScore) throws TimeoutException, SuspendExecution {
        // Callable 형태로 Async 실행하고 결과 리턴.
        Integer resultCount = 0;

        resultCount = Async.callBlocking(GameConstants.DB_THREAD_POOL, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                SqlSession sqlSession = GameSqlSessionFactory.getSqlSession();
                UserDataMapper userDataMapper = sqlSession.getMapper(UserDataMapper.class);
                try {
                    int resultCount = userDataMapper.updateUserHighScore(uuid, highScore);
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
}
