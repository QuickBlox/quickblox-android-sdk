package com.quickblox.sample.videochat.conference.java.managers;

import android.util.Log;

import com.quickblox.conference.ConferenceSession;


public class WebRtcSessionManager {
    private static final String TAG = WebRtcSessionManager.class.getSimpleName();

    private static WebRtcSessionManager instance;
    private static ConferenceSession currentSession;

    private WebRtcSessionManager() {

    }

    public static WebRtcSessionManager getInstance() {
        if (instance == null) {
            Log.d(TAG, "New Instance of WebRtcSessionManager");
            instance = new WebRtcSessionManager();
        }

        return instance;
    }

    public ConferenceSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(ConferenceSession qbCurrentSession) {
        currentSession = qbCurrentSession;
    }
}