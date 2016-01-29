package com.quickblox.simplesample.messages.gcm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(), PushGcmListenerService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
    }
}