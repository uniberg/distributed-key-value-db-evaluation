package com.uniberg.sessionDbEvaluator;

import java.util.ArrayList;
import java.util.List;

public class State {
    private static State instance = null;

    public List<Session> sessionList = null;

    private State() {
        sessionList = new ArrayList<Session>();
    }

    public static State getInstance() {
        if (instance == null) {
            instance = new State();
        }
        return instance;
    }
}
