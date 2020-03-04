package com.nhn.tardis.sample.mybatis;

import java.io.IOException;
import java.io.Reader;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * 게임에서 사용하는 DB 연결 객체
 */
public class GameSqlSessionFactory {
    private static SqlSessionFactory sqlSessionFactory;

    /** XML에 명시된 접속 정보를 읽어들인다. */
    // 클래스 초기화 블럭 : 클래스 변수의 복잡한 초기화에 사용된다.
    // 클래스가 처음 로딩될 때 한번만 수행된다.
    static {
        // 접속 정보를 명시하고 있는 XML의 경로 읽기
        try {
            // mybatis_config.xml 파일의 경로 지정
            Reader reader = Resources.getResourceAsReader("mybatis/mybatis-taptap-config.xml");

            // sqlSessionFactory가 존재하지 않는다면 생성한다.
            if (sqlSessionFactory == null) {
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 데이터베이스 접속 객체를 통해 DATABASE에 접속한 세션를 리턴한다.
     */
    public static SqlSession getSqlSession() {
        return sqlSessionFactory.openSession();
    }
}
