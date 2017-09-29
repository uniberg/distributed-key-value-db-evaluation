package com.uniberg.sessionDbEvaluator;
import io.prometheus.client.Histogram;
import io.prometheus.client.Counter;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.PushGateway;

import java.io.IOException;

// This Class is used to implement the time-logging functionality
public class SessionHandlerWrapper {
    private static CollectorRegistry registry = new CollectorRegistry();
    private static final double[] buckets = new double[]{.001, .002, .005, .01, .015, .02, .025, .05, .075, .1, .2, .3, .4, .5, .6, .7, .8, .9, 1, 1.5, 2};
    private static final Histogram latency = Histogram.build().buckets(buckets).name("sessDbEval_request_latency_seconds").help("Request Latency in seconds").labelNames("method").register(registry);
    private static final Counter requests = Counter.build().name("sessDbEval_request_counter").help("Requests").labelNames("method").register(registry);
    private static final Counter failedRequests = Counter.build().name("sessDbEval_failed_request_counter").help("Requests").register(registry);

    private SessionHandler sessHandler;
    private String prometheus_jobname;
    private PushGateway pg;
    private Integer failed_requests = 0;

    public Integer getFailedRequests() {
        return failed_requests;
    }

    public SessionHandlerWrapper(Config config) throws Exception {
        int dbselected = Integer.parseInt(config.getConfig("db.selected"));
        switch (dbselected) {
            case 1:
                sessHandler = RedisSessionHandler.init(config);
                break;
            case 2:
                sessHandler = CassandraSessionHandler.init(config);
                break;
            case 3:
                sessHandler = CouchbaseSessionHandler.init(config);
                break;
            default:
                sessHandler = null;
        }

        if (sessHandler == null) {
            throw new Exception("Config Setting db.selected not set or connection failed");
        }

        prometheus_jobname = config.getConfig("prometheus.jobname");
        pg = new PushGateway(config.getConfig("prometheus.pushgateway"));
    }

    public boolean createSession(Session sess) throws Exception {
        Histogram.Timer timer = latency.labels("create").startTimer();
        requests.labels("create").inc();
        try {
            sessHandler.createSession(sess);
        } catch (Exception ex){
            failedRequests.inc();
            failed_requests++;
            return false;
        } finally {
            timer.observeDuration();
            pushData();
        }
        return true;
    }

    public boolean updateSession(Session sess) throws Exception {
        Histogram.Timer timer = latency.labels("update").startTimer();
        requests.labels("update").inc();
        try {
            sessHandler.updateSession(sess);
        } catch (Exception ex){
            failedRequests.inc();
            failed_requests++;
            return false;
        } finally {
            timer.observeDuration();
            pushData();
        }
        return true;
    }

    public boolean deleteSession(String key) throws Exception {
        Histogram.Timer timer = latency.labels("delete").startTimer();
        requests.labels("delete").inc();
        try {
            sessHandler.deleteSession(key);
        } catch (Exception ex){
            failedRequests.inc();
            failed_requests++;
            return false;
        } finally {
            timer.observeDuration();
            pushData();
        }
        return true;
    }

    public Session getSession(String key, Session old_session) throws Exception {
        Histogram.Timer timer = latency.labels("get").startTimer();
        requests.labels("get").inc();
        Session getted = null;
        Boolean already_increased = false;
        try {
            getted = sessHandler.getSession(key);
        } catch (Exception ex){
            failedRequests.inc();
            failed_requests++;
            already_increased = true;
            return null;
        } finally {
            timer.observeDuration();
            if (getted == null || getted.getLastUpdate() != old_session.getLastUpdate()){
                if (getted != null) {
                    System.out.println("Get: " + getted.toString());
                }
                System.out.println("Old: " + old_session.toString());
                if (already_increased == false) {
                    failedRequests.inc();
                    failed_requests++;
                }
            }
            pushData();
        }
        return getted;
    }

    private void pushData() throws Exception {
        try {
            pg.pushAdd(registry, prometheus_jobname);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (failed_requests > 99){
            throw new Exception("There were " + failed_requests.toString() + " failed Requests. Terminating");
        }
    }
}
