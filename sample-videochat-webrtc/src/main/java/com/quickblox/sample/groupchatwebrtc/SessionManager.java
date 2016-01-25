package com.quickblox.sample.groupchatwebrtc;

import com.quickblox.videochat.webrtc.QBRTCSession;

/**
 * Created by igorkhomenko on 1/21/16.
 */
public class SessionManager {
    private static QBRTCSession currentSession;

    public static QBRTCSession getCurrentSession() {
        return currentSession;
    }

    public static void setCurrentSession(QBRTCSession qbCurrentSession) {
        currentSession = qbCurrentSession;
    }
}
