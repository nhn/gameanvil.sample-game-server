package com.nhn.gameanvil.sample.db;

import com.mysql.cj.xdevapi.Client;
import com.mysql.cj.xdevapi.ClientFactory;
import com.mysql.cj.xdevapi.Schema;
import com.mysql.cj.xdevapi.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 스레드 세이프 하지않아서 해당 방식은 주석 처리
//public class GameSessionFactory {
//    private static Logger logger = LoggerFactory.getLogger(GameSessionFactory.class);
//
//    private static Client cli;
//
//    static {
//        // 접속 정보를 명시하고 있는 XML의 경로 읽기
//        ClientFactory cf = new ClientFactory();
//
////        String connectionUrl = "mysqlx://10.77.14.245:33060/taptap?user=kevin&password=nhn!%40%23123";
////        String connectionUrl = "mysqlx://localhost:33060/test?xdev.api.ssl-mode=DISABLED&user=root&password=1234";
//        String connectionUrl = "mysqlx://localhost:33060/test?xdevapi.ssl-mode=DISABLED&user=root&password=1234";
//
//        String clientPropsJson = "{\"pooling\":{\"enabled\":true, \"maxSize\":100, \"maxIdleTime\":30000, \"queueTimeout\":10000} }";
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("GameSessionFactory connectionUrl : {}, clientPropsJson {}", connectionUrl, clientPropsJson);
//        }
//
//        cli = cf.getClient(connectionUrl, clientPropsJson);
//    }
//
//    public static Session getSession() {
//        return cli.getSession();
//    }
//
//    public static Schema getSchema(String name) {
//        return cli.getSession().getSchema(name);
//    }
//}
