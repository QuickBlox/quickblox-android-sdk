package com.quickblox.chat_v2;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.quickblox.chat_v2.gcm.GCMHelper;
import com.quickblox.chat_v2.gcm.GCMParser;
import com.quickblox.chat_v2.gcm.GCMSubscriber;

/**
 * Created with IntelliJ IDEA.
 * User: Nikolay Dymura
 * Date: 5/27/13
 * E-mail: nikolay.dymura@gmail.com
 */
public class GCMIntentService extends GCMBaseIntentService {
    static final String LOG_TAG = GCMIntentService.class.getSimpleName();

    private GCMParser gcmParser;
    private GCMSubscriber gcmSubscriber;
    private Handler handler;

    public GCMIntentService() {
        super(GCMHelper.SENDER_ID);
        gcmParser = GCMParser.newInstance();
        gcmSubscriber = GCMSubscriber.newInstance();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.e(LOG_TAG, "onError: " + errorId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.d(LOG_TAG, "onMessage");
        gcmParser.parseMessage(context, intent.getExtras());
    }

    @Override
    public void onRegistered(Context context, final String registrationId) {
        Log.d(LOG_TAG, "onRegistered() registrationId=" + registrationId);
        handler.post(new Runnable() {
            @Override
            public void run() {
                gcmSubscriber.subscribe(registrationId, getDeviceId());
            }
        });
    }

    @Override
    public void onUnregistered(Context context, String arg1) {
        Log.d(LOG_TAG, "onUnregistered");
        handler.post(new Runnable() {

            @Override
            public void run() {
                gcmSubscriber.unsubscribe(getDeviceId());
            }
        });
    }


    private String getDeviceId() {
        return ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }
}
