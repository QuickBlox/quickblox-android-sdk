package com.quickblox.sample.groupchatwebrtc.util;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class NetworkConnectionChecker {

    private final ConnectivityManager connectivityManager;

    private Set<OnConnectivityChangedListener> listeners = new CopyOnWriteArraySet<>();

    public NetworkConnectionChecker(Application context) {
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(new NetworkStateReceiver(), intentFilter);
    }

    public void registerListener(OnConnectivityChangedListener listener) {
        listeners.add(listener);
        listener.connectivityChanged(isConnectedNow());
    }

    public void unregisterListener(OnConnectivityChangedListener listener) {
        listeners.remove(listener);
    }

    public boolean isConnectedNow() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public interface OnConnectivityChangedListener {

        void connectivityChanged(boolean availableNow);

    }

    private class NetworkStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnectedNow = isConnectedNow();

            for (OnConnectivityChangedListener listener : listeners) {
                listener.connectivityChanged(isConnectedNow);
            }
        }
    }
}
