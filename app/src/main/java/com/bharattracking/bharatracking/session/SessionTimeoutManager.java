package com.bharattracking.bharatracking.session;

import android.app.Application;

import com.bharattracking.bharatracking.Constants;

import java.util.Timer;
import java.util.TimerTask;

public class SessionTimeoutManager extends Application {

    private Timer timer;
    private TimeOutListner listner;

    public void registerUserSessionListner(TimeOutListner listner) {
        this.listner = listner;
    }
    public void     onUserInteracted() {
        startUserSession();
    }

    public void cancelTimer() {
        if (timer != null) timer.cancel();
    }

    public void startUserSession() {
        cancelTimer();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                listner.onSessionTimeOut();
            }
        }, Constants.SESSION_TIMEOUT_DELAY);
    }
}
