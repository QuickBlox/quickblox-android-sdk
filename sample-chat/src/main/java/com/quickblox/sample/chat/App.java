package com.quickblox.sample.chat;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.LogLevel;
import com.quickblox.core.QBSettings;
import com.quickblox.sample.chat.utils.Consts;

import vc908.stickerfactory.StickersManager;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();

    private static App instance;

    public synchronized static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialise QuickBlox SDK and Stickers SDK
        QBSettings.getInstance().fastConfigInit(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET);
        StickersManager.initialize(Consts.STICKER_API_KEY, this);

        if (BuildConfig.DEBUG) {
            QBChatService.setDebugEnabled(true);
            QBSettings.getInstance().setLogLevel(LogLevel.DEBUG);
        } else {
            QBChatService.setDebugEnabled(false);
            QBSettings.getInstance().setLogLevel(LogLevel.NOTHING);
        }
    }

    public int getAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
