package com.quickblox.sample.groupchatwebrtc.utils;

import android.util.Log;

import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacksImpl;

/**
 * Created by tereha on 16.05.16.
 */
public class WebRtcSessionManager extends QBRTCClientSessionCallbacksImpl {
    private static final String TAG = WebRtcSessionManager.class.getSimpleName();

    public static WebRtcSessionManager instance;

    private static QBRTCSession currentSession;

    public static WebRtcSessionManager getInstance(){
        if (instance == null){
            instance = new WebRtcSessionManager();
        }

        return instance;
    }

    public QBRTCSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(QBRTCSession qbCurrentSession) {
        currentSession = qbCurrentSession;
    }

    @Override
    public void onReceiveNewSession(QBRTCSession session) {
        super.onReceiveNewSession(session);
        Log.d(TAG, "onReceiveNewSession to WebRtcSessionManager");

        if (currentSession == null){
            setCurrentSession(session);
        }
    }

    @Override
    public void onSessionClosed(QBRTCSession session) {
        super.onSessionClosed(session);
        Log.d(TAG, "onSessionClosed WebRtcSessionManager");

        if (session.equals(getCurrentSession())){
            setCurrentSession(null);
        }
    }
}
