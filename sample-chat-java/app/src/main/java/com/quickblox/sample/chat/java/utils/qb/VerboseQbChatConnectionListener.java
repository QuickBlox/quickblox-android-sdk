package com.quickblox.sample.chat.java.utils.qb;

import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.quickblox.sample.chat.java.App;
import com.quickblox.sample.chat.java.R;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

public class VerboseQbChatConnectionListener implements ConnectionListener {
    private static final String TAG = VerboseQbChatConnectionListener.class.getSimpleName();
    private View rootView;
    private Snackbar snackbar;

    public VerboseQbChatConnectionListener(View rootView) {
        this.rootView = rootView;
    }

    @Override
    public void connected(XMPPConnection connection) {
        Log.i(TAG, "connected()");
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean authenticated) {
        Log.i(TAG, "authenticated()");
    }

    @Override
    public void connectionClosed() {
        Log.i(TAG, "connectionClosed()");
    }

    @Override
    public void connectionClosedOnError(final Exception e) {
        Log.i(TAG, "connectionClosedOnError(): " + e.getLocalizedMessage());
        snackbar = Snackbar.make(rootView, App.getInstance().getString(R.string.connection_error), Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    @Override
    public void reconnectingIn(final int seconds) {
        if (seconds % 5 == 0 && seconds != 0) {
            Log.i(TAG, "reconnectingIn(): " + seconds);
            snackbar = Snackbar.make(rootView, App.getInstance().getString(R.string.reconnect_alert, String.valueOf(seconds)), Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    @Override
    public void reconnectionSuccessful() {
        Log.i(TAG, "reconnectionSuccessful()");
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    @Override
    public void reconnectionFailed(final Exception error) {
        Log.i(TAG, "reconnectionFailed(): " + error.getLocalizedMessage());
    }
}