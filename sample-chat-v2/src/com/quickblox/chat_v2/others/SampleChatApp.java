package com.quickblox.chat_v2.others;

import android.app.Application;
import com.quickblox.module.chat.smack.SmackAndroid;

/**
 * Created with IntelliJ IDEA.
 * User: nickolas
 * Date: 29.03.13
 * Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public class SampleChatApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SmackAndroid.init(this);
    }
}
