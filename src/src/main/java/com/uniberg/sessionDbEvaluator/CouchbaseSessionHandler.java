package com.uniberg.sessionDbEvaluator;

import com.couchbase.client.java.*;
import com.couchbase.client.java.document.*;
import com.couchbase.client.java.document.json.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CouchbaseSessionHandler implements SessionHandler{
    private Cluster cluster;
    private Bucket defaultBucket;
    private Config config;
    private Integer activeCluster;

    public static CouchbaseSessionHandler init(Config config){
        return new CouchbaseSessionHandler(config);
    }

    private void setupCluster(){
        if (cluster != null) {
            try {
                cluster.disconnect();
            } catch (Exception ex) {
                System.out.println("Cluster disconnect failed");
            }
        }
        Integer failOverCluster = activeCluster + 1;
        List<String> dbhosts = new ArrayList<String>();
        if (failOverCluster % 2 == 1) {
            dbhosts.add(config.getConfig("db.hosta1"));
            dbhosts.add(config.getConfig("db.hosta2"));
            dbhosts.add(config.getConfig("db.hosta3"));
        } else {
            dbhosts.add(config.getConfig("db.hostb1"));
            dbhosts.add(config.getConfig("db.hostb2"));
            dbhosts.add(config.getConfig("db.hostb3"));
        }
        cluster = CouchbaseCluster.create(dbhosts);
        defaultBucket = cluster.openBucket("default");
        activeCluster = failOverCluster;
    }

    private CouchbaseSessionHandler(Config config){
        cluster = null;
        activeCluster = Integer.parseInt(config.getConfig("cluster"));
        this.config = config;

        Logger logger = Logger.getLogger("com.couchbase.client");
        logger.setLevel(Level.WARNING);
        for(Handler h : logger.getParent().getHandlers()) {
            if(h instanceof ConsoleHandler){
                h.setLevel(Level.WARNING);
            }
        }

        setupCluster();
    }

    public void createSession(Session session){
        try {
            defaultBucket.upsert(JsonDocument.create(session.getSessionId(), JsonObject.create().put("state", session.to_Base64())));
        } catch (Exception ex) { // Failover to other Cluster
            System.out.println("UPSERT: Failover to other Cluster");
            setupCluster(); // Switch Cluster
            defaultBucket.upsert(JsonDocument.create(session.getSessionId(), JsonObject.create().put("state", session.to_Base64())));
        }
    }

    public void updateSession(Session session){
        this.createSession(session);
    }

    public void deleteSession(String key){
        try {
            defaultBucket.remove(key);
        } catch (Exception ex) { // Failover to other Cluster
            System.out.println("DELETE: Failover to other Cluster");
            setupCluster(); // Switch Cluster
            defaultBucket.remove(key);
        }
    }

    public Session getSession(String key){
        String sessString = null;
        try{
            sessString = defaultBucket.get(key).content().getString("state");
        } catch (Exception ex) { // Failover to Replica
            System.out.println("GET: Failover to Replica");
            try {
                Iterator<JsonDocument> lJson = defaultBucket.getFromReplica(key);
                if (lJson.hasNext()) {
                    sessString = lJson.next().content().getString("state");
                }
            } catch (Exception ex2) { // Failover to other Cluster
                System.out.println("GET: Failover to other Cluster");
                setupCluster(); // Switch Cluster
                sessString = defaultBucket.get(key).content().getString("state");
            }
        }
        return Session.from_Base64(sessString);
    }
}
