package com.nhn.gameanvil.sample.game;

import co.paralleluniverse.fibers.SuspendExecution;
import com.mysql.cj.xdevapi.Client;
import com.mysql.cj.xdevapi.ClientFactory;
import com.mysql.cj.xdevapi.Row;
import com.mysql.cj.xdevapi.Session;
import com.mysql.cj.xdevapi.SqlResult;
import com.nhn.gameanvil.async.Async;
import com.nhn.gameanvil.node.game.BaseGameNode;
import com.nhn.gameanvil.node.game.data.ChannelUpdateType;
import com.nhn.gameanvil.node.game.data.ChannelUserInfo;
import com.nhn.gameanvil.node.game.data.RoomInfo;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.sample.common.GameConstants;
import com.nhn.gameanvil.sample.db.UserDbHelper;
import com.nhn.gameanvil.sample.redis.RedisHelper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameNode extends BaseGameNode {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private RedisHelper redisHelper;
    private UserDbHelper userDbHelper;

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("onInit");

        // FIXME RedisClient 노드마다 생성, singleton 으로 만들어서 접근 하면 안된다.
        // 레디스 생성
        redisHelper = new RedisHelper();
        // 레디스 연결 처리
        redisHelper.connect(GameConstants.REDIS_URL, GameConstants.REDIS_PORT);

        // TODO - xdevapi 게임 노드에서 생성
        userDbHelper = new UserDbHelper("mysqlx://10.77.14.245:33060/taptap?user=kevin&password=kevin1234");  // 빌드머신 mysql 8.0
//        userDbHelper = new UserDbHelper("mysqlx://localhost:33060/taptap?user=kevin&password=kevin1234");  // 로컬 mysql 8.0
//        userDbHelper = new UserDbHelper("mysqlx://localhost:33060/taptap?xdevapi.ssl-mode=DISABLED&user=root&password=1234"); // 로컬 mysql. 5.7

//        ClientFactory cf = new ClientFactory();
//        String connectionUrl = "mysqlx://mcjeon:dodanto@10.77.14.245:33060/world";  // Note> connection string 두 방식 모두 가능
////        String connectionUrl = "mysqlx://10.77.14.245:33060/world?user=mcjeon&password=dodanto"; // Note> connection string 두 방식 모두 가능
//        Client cli = cf.getClient(connectionUrl, "{\"pooling\":{\"enabled\":true, \"maxSize\":100, \"maxIdleTime\":30000, \"queueTimeout\":10000} }");

//        Session session = cli.getSession();
//        //Session session = new SessionFactory().getSession(connectionUrl); // Note> connection pooling 없이 사용하는 방법
//
//        //session.sql("USE world"); // Note> Java에서는 이런식으로 schema 변경 가능
//
//        // Note> sql() 사용
//        CompletableFuture<SqlResult> future = session.sql("select * from city").executeAsync();
//
//        try {
//            SqlResult result = Async.awaitFuture(future);
//            List<Row> rows = result.fetchAll();
//
//            for (Row row : rows)
//                logger.warn("-----> mysql x-dev api: {}, {}, {}, {}", row.getString("ID"), row.getString("Name"), row.getString("District"), row.getString("Population"));
//
//        } catch (TimeoutException e) {
//            logger.error("GameSpaceNode::onInit()", e);
//        }

        // 성능 시간 비교 테스트
//        ClientFactory cf = new ClientFactory();
//        String connectionUrl = "mysqlx://10.77.14.245:33060/taptap?user=kevin&password=kevin1234"; // 빌드머신2 mysql 8.0
////        String connectionUrl = "mysqlx://10.77.14.245:33060/taptap?user=root&password=Tardis"; // 빌드머신2 mysql 8.0
////        String connectionUrl = "mysqlx://localhost:33060/taptap?user=kevin&password=kevin1234"; // 로컬 mysql 8.0
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
//            Connection conn = DriverManager.getConnection("jdbc:mysql://10.77.14.245/taptap?serverTimezone=UTC", "kevin", "kevin1234");
////            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/taptap?serverTimezone=UTC", "kevin", "kevin1234");
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
//            Connection conn = DriverManager.getConnection("jdbc:mysql://10.77.14.245/taptap?serverTimezone=UTC", "kevin", "kevin1234");
////            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/taptap?serverTimezone=UTC", "kevin", "kevin1234");
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

    @Override
    public void onPrepare() throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onPrepare");
        }
        setReady();
    }

    @Override
    public void onReady() throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onReady");
        }
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onDispatch");
        }
    }

    @Override
    public void onPause(Payload payload) throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onPause");
        }
    }

    @Override
    public void onResume(Payload payload) throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onResume");
        }
        setReady();
    }

    @Override
    public void onShutdown() throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onShutdown");
        }

        redisHelper.shutdown();
        userDbHelper.closeClient();
    }

    @Override
    public boolean onNonStopPatchSrcStart() throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onNonStopPatchSrcStart");
        }
        return true;
    }

    @Override
    public boolean onNonStopPatchSrcEnd() throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onNonStopPatchSrcEnd");
        }
        return true;
    }

    @Override
    public boolean canNonStopPatchSrcEnd() throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("canNonStopPatchSrcEnd");
        }
        return true;
    }

    @Override
    public boolean onNonStopPatchDstStart() throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onNonStopPatchDstStart");
        }
        return true;
    }

    @Override
    public boolean onNonStopPatchDstEnd() throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onNonStopPatchDstEnd");
        }
        return true;
    }

    @Override
    public boolean canNonStopPatchDstEnd() throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("canNonStopPatchDstEnd");
        }
        return true;
    }

    @Override
    public void onChannelUserUpdate(ChannelUpdateType type, ChannelUserInfo channelUserInfo, int userId, String accountId) throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onChannelUserUpdate");
        }
    }

    @Override
    public void onChannelRoomUpdate(ChannelUpdateType type, RoomInfo channelRoomInfo, int roomId) throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onChannelRoomUpdate");
        }
    }

    @Override
    public void onChannelInfo(Payload outPayload) throws SuspendExecution {
        if (logger.isDebugEnabled()) {
            logger.debug("onChannelInfo");
        }
    }

    public RedisHelper getRedisHelper() {
        return redisHelper;
    }

    public UserDbHelper getUserDbHelper() {
        return userDbHelper;
    }
}
