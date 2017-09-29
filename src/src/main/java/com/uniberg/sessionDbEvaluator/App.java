package com.uniberg.sessionDbEvaluator;
import java.util.*;

public class App 
{
    public static void main( String[] args )
    {
        try {
            final Config config = new Config();
            final SessionHandlerWrapper sessHandler = new SessionHandlerWrapper(config);
            System.out.println(genPayload().length());
            System.out.println(Long.toString(System.currentTimeMillis()));

            final Integer sessCount = Integer.parseInt(config.getConfig("app.sessionCounter"));
            final String sessionPrefix = config.getConfig("app.sessionPrefix");

            for(Integer i = 0; i < sessCount; i++){
                Session sess = new Session(sessionPrefix + i.toString(), genPayload());
                sessHandler.createSession(sess);
                State.getInstance().sessionList.add(sess);
                Thread.sleep(10);
            }
            System.out.println("Init done");


            final Random random = new Random();

            final Timer mainloop = new Timer();
            mainloop.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        // 50% only getSession
                        // 45% GetSession + UpdateSession
                        // 5% Deletesessin + NewSession
                        Integer sessid = random.nextInt(sessCount);
                        Session sess = sessHandler.getSession(sessionPrefix + sessid.toString(), State.getInstance().sessionList.get(sessid));
                        Boolean success = true;
                        if (sess != null) {
                            switch (random.nextInt(20)) {
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                    sess.setStateInfo(genPayload());
                                    success = sessHandler.updateSession(sess);
                                    break;
                                case 19:
                                    success = sessHandler.deleteSession(sess.getSessionId());
                                    if (success) {
                                        sess = new Session(sessionPrefix + sessid.toString(), genPayload());
                                        success = sessHandler.createSession(sess);
                                    }
                                    break;
                                default:
                                    break;
                            }
                            if (success) {
                                State.getInstance().sessionList.remove(State.getInstance().sessionList.get(sessid));
                                State.getInstance().sessionList.add(sessid, sess);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Failed Requests: " + sessHandler.getFailedRequests().toString());
                        System.exit(1);
                    }
                }
            }, 0, 20); //20 MS -> 50 Exec/s -> 78 Req/s
            //15k Sessions per Instance -> Every Session will be accessed after around 300s (5m)
            //60 Instances for 900k Sessions

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        System.out.println("End");
                        mainloop.cancel();
                        for (Integer i = 0; i < sessCount; i++) {
                            sessHandler.getSession(sessionPrefix + i.toString(), State.getInstance().sessionList.get(i));
                            sessHandler.deleteSession(sessionPrefix + i.toString());
                            Thread.sleep(10);
                        }
                        System.out.println("Failed Requests: " + sessHandler.getFailedRequests().toString());
                        System.exit(0);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("Failed Requests: " + sessHandler.getFailedRequests().toString());
                        System.exit(1);
                    }
                }
            }, Long.parseLong(config.getConfig("app.testDurationInSeconds")) * 1000, 3600000);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String genPayload(){
        String payload = Long.toString(System.currentTimeMillis());
        payload = payload + payload + payload + payload + payload + payload + payload + payload + payload + payload + payload;
        payload = payload + payload + payload + payload + payload + payload + payload + payload + payload + payload + payload;
        return payload;
    }
}
