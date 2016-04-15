package com.quickblox.sample.chat;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.quickblox.core.QBSettings;
import com.quickblox.sample.chat.ui.activities.ShopActivity;

import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.billing.Prices;
import vc908.stickerfactory.utils.Utils;

public class ApplicationSingleton extends Application {
    private static final String TAG = ApplicationSingleton.class.getSimpleName();

    public static final String APP_ID = "92";
    public static final String AUTH_KEY = "wJHdOcQSxXQGWx5";
    public static final String AUTH_SECRET = "BTFsj7Rtt27DAmT";
    public static final String ACCOUNT_KEY = "rz2sXxBt5xgSxGjALDW6";

    public static final String STICKER_API_KEY = "847b82c49db21ecec88c510e377b452c";

    public static final String USER_LOGIN = "igorquickblox44";
    public static final String USER_PASSWORD = "igorquickblox44";

    private static ApplicationSingleton instance;
    public static ApplicationSingleton getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        instance = this;

        // Initialise QuickBlox SDK
        //
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);


        // Initialise Stickers sdk
        //
        StickersManager.initialize(STICKER_API_KEY, this);

        // set current user id
        // now it device id, and it means,
        // that all purchases will be bound to current device
        StickersManager.setUserID(Utils.getDeviceId(this));
        // register our shop activity for inner currency payment
        StickersManager.setShopClass(ShopActivity.class);
        // set prices
        StickersManager.setPrices(new Prices()
                .setPricePointB("$0.99", 0.99f)
                .setPricePointC("$1.99", 1.99f));
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
