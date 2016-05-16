package com.quickblox.sample.groupchatwebrtc.utils;

import com.quickblox.videochat.webrtc.QBRTCSession;

/**
 * Created by tereha on 16.05.16.
 */
public class WebRtcSessionManager {
    private static final String TAG = WebRtcSessionManager.class.getSimpleName();

    private static QBRTCSession currentSession;

    public static QBRTCSession getCurrentSession() {
        return currentSession;
    }

    public static void setCurrentSession(QBRTCSession qbCurrentSession) {
        currentSession = qbCurrentSession;
    }
}
