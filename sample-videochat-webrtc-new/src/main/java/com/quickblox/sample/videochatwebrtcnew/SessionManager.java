package com.quickblox.sample.videochatwebrtcnew;

import com.quickblox.videochat.webrtc.QBRTCSession;

/**
 * Created by tereha on 10.07.15.
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
