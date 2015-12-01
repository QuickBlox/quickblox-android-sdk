package com.quickblox.sample.chat.utils.chat;

import android.util.Log;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

public class VerboseQbChatConnectionListener implements ConnectionListener {
    private static final String TAG = VerboseQbChatConnectionListener.class.getSimpleName();

    @Override
    public void connected(XMPPConnection connection) {
        Log.i(TAG, "connected()");
    }

    @Override
    public void authenticated(XMPPConnection connection) {
        Log.i(TAG, "authenticated()");
    }

    @Override
    public void connectionClosed() {
        Log.i(TAG, "connectionClosed()");
    }

    @Override
    public void connectionClosedOnError(final Exception e) {
        Log.i(TAG, "connectionClosedOnError(): " + e.getLocalizedMessage());
    }

    @Override
    public void reconnectingIn(final int seconds) {
        if (seconds % 5 == 0) {
            Log.i(TAG, "reconnectingIn(): " + seconds);
        }
    }

    @Override
    public void reconnectionSuccessful() {
        Log.i(TAG, "reconnectionSuccessful()");
    }

    @Override
    public void reconnectionFailed(final Exception error) {
        Log.i(TAG, "reconnectionFailed(): " + error.getLocalizedMessage());
    }
}
