package com.uniberg.sessionDbEvaluator;
import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.*;

public class CassandraSessionHandler implements SessionHandler {

    //CREATE KEYSPACE sessdb WITH replication = {'class': 'NetworkTopologyStrategy', 'DC1': 3, 'DC2': 3};
    //use sessdb;
    //CREATE TABLE session_state ( session_id varchar PRIMARY KEY, data varchar);

    private com.datastax.driver.core.Session conn;
    private PreparedStatement pstGet;
    private PreparedStatement pstUpsert;
    private PreparedStatement pstDelete;
    private Config config;
    private Integer activeCluster;

    public static CassandraSessionHandler init(Config config) throws Exception {
        return new CassandraSessionHandler(config);
    }

    private void setupCluster() throws Exception{
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception ex) {
                System.out.println("Cluster disconnect failed");
            }
        }
        QueryOptions opts = new QueryOptions();
        opts.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        activeCluster = activeCluster + 1;
        if (activeCluster > 10) {
            throw new Exception("To many failOvers");
        }
        try {
            Cluster cluster = null;
            if (activeCluster % 2 == 1) {
                cluster = Cluster.builder()
                        .addContactPoint(config.getConfig("db.hosta1"))
                        .addContactPoint(config.getConfig("db.hosta2"))
                        .addContactPoint(config.getConfig("db.hosta3"))
                        .withQueryOptions(opts)
                        .withSpeculativeExecutionPolicy(new ConstantSpeculativeExecutionPolicy(150, 3))
                        .withLoadBalancingPolicy(DCAwareRoundRobinPolicy.builder().withLocalDc("DC1").build())
                        .build();
                conn = cluster.connect(config.getConfig("cassandra.keyspace"));
            } else {
                cluster = Cluster.builder()
                        .addContactPoint(config.getConfig("db.hostb1"))
                        .addContactPoint(config.getConfig("db.hostb2"))
                        .addContactPoint(config.getConfig("db.hostb3"))
                        .withQueryOptions(opts)
                        .withSpeculativeExecutionPolicy(new ConstantSpeculativeExecutionPolicy(150, 3))
                        .withLoadBalancingPolicy(DCAwareRoundRobinPolicy.builder().withLocalDc("DC2").build())
                        .build();
                conn = cluster.connect(config.getConfig("cassandra.keyspace"));
            }
            pstGet = conn.prepare("SELECT data FROM session_state WHERE session_id=?");
            pstUpsert = conn.prepare("UPDATE session_state SET data=? WHERE session_id=?");
            pstDelete = conn.prepare("DELETE FROM session_state WHERE session_id=?");
            System.out.println("Flapped... " + activeCluster.toString());
            for (Host host : cluster.getMetadata().getAllHosts())
            {
                System.out.printf("Data Center: %s; Rack: %s; Host: %s\n",
                        host.getDatacenter(), host.getRack(), host.getAddress());
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            System.out.println(ex.getStackTrace());
            if (activeCluster < 3) {
                setupCluster();
            }
        }
    }

    private CassandraSessionHandler(Config config) throws Exception {
        this.config = config;
        conn = null;
        activeCluster = Integer.parseInt(config.getConfig("cluster"));
        setupCluster();
    }

    public void createSession(com.uniberg.sessionDbEvaluator.Session session){
        try {
            conn.execute(pstUpsert.bind(session.to_Base64(), session.getSessionId()));
        } catch (Exception ex) {
            try {
                System.out.println(ex.toString());
                System.out.println(ex.getStackTrace());
                setupCluster();
                createSession(session);
            } catch (Exception e) {
                System.out.println(e.toString());
                System.out.println(e.getStackTrace());
            }
        }
    }

    public void updateSession(com.uniberg.sessionDbEvaluator.Session session){
        this.createSession(session);
    }

    public void deleteSession(String key){
        try {
            conn.execute(pstDelete.bind(key));
        } catch (Exception ex) {
            try {
                System.out.println(ex.toString());
                System.out.println(ex.getStackTrace());
                setupCluster();
                deleteSession(key);
            } catch (Exception e) {
                System.out.println(e.toString());
                System.out.println(e.getStackTrace());
            }
        }
    }

    public com.uniberg.sessionDbEvaluator.Session getSession(String key){
        try {
            Row row = conn.execute(pstGet.bind(key)).one();
            return Session.from_Base64(row.getString("data"));
        } catch (Exception ex) {
            try {
                System.out.println(ex.toString());
                System.out.println(ex.getStackTrace());
                setupCluster();
                return getSession(key);
            } catch (Exception e) {
                System.out.println(e.toString());
                System.out.println(e.getStackTrace());
            }
        }
        return null;
    }
}