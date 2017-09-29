package com.uniberg.sessionDbEvaluator;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

public class RedisSessionHandler implements SessionHandler{
    private JedisCluster jc;

    public static RedisSessionHandler init(Config config){
        return new RedisSessionHandler(config);
    }

    private RedisSessionHandler(Config config){
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        jedisClusterNodes.add(new HostAndPort(config.getConfig("db.hosta1"), 7000));
        jedisClusterNodes.add(new HostAndPort(config.getConfig("db.hosta2"), 7000));
        jedisClusterNodes.add(new HostAndPort(config.getConfig("db.hosta3"), 7000));
        jedisClusterNodes.add(new HostAndPort(config.getConfig("db.hostb1"), 7000));
        jedisClusterNodes.add(new HostAndPort(config.getConfig("db.hostb2"), 7000));
        jedisClusterNodes.add(new HostAndPort(config.getConfig("db.hostb3"), 7000));
        jc = new JedisCluster(jedisClusterNodes);
    }

    public void createSession(Session session){
        jc.set(session.getSessionId(), session.to_Base64());
    }

    public void updateSession(Session session){
        this.createSession(session);
    }

    public void deleteSession(String key){
        jc.del(key);
    }

    public Session getSession(String key){
        return Session.from_Base64(jc.get(key));
    }
}