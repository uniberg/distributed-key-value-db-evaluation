package com.uniberg.sessionDbEvaluator;

public interface SessionHandler {
    void createSession(Session session);
    void updateSession(Session session);
    void deleteSession(String key);
    Session getSession(String key);
}
