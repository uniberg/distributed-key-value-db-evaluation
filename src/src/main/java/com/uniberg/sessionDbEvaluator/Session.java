package com.uniberg.sessionDbEvaluator;

import java.io.IOException;
import java.util.*;
import java.io.*;

public class Session implements java.io.Serializable{
    private String sessionId;
    private int sessionUpdateCounter;
    private long lastUpdate;
    private String stateInfo;

    public Session(String sessionId, String stateInfo) {
        this.sessionId = sessionId;
        this.stateInfo = stateInfo;
        this.lastUpdate = System.currentTimeMillis();
        this.sessionUpdateCounter = 0;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getSessionUpdateCounter() {
        return sessionUpdateCounter;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
        this.sessionUpdateCounter = sessionUpdateCounter + 1;
    }

    public String toString() {
        String str = "ID: " + this.sessionId + " (";
        str += "Info: " + this.stateInfo;
        str += " Updated: " + this.lastUpdate;
        str += " Counter: " + this.sessionUpdateCounter;
        return str + ")";
    }

    public String to_Base64() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException ioex) {
            return "";
        }
    }

    public static Session from_Base64(String s){
        Session sess;
        try {
            byte[] data = Base64.getDecoder().decode(s);
            ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(data));
            sess = (Session) ois.readObject();
            ois.close();
        } catch (Exception ex) {
            sess = null;
        }
        return sess;
    }
}
