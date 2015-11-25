package com.quickblox.simplesample.messages;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.quickblox.core.QBSettings;
import com.quickblox.simplesample.messages.main.Consts;

public class App extends Application {

    private static App instance;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initApplication();
    }

    private void initApplication() {
        instance = this;

        // Set application credentials
        //
        QBSettings.getInstance().fastConfigInit(String.valueOf(Consts.APP_ID), Consts.AUTH_KEY,
                Consts.AUTH_SECRET);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
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