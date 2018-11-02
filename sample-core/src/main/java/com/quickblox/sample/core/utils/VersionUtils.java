package com.quickblox.sample.core.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.quickblox.sample.core.CoreApp;

public class VersionUtils {

    public static int getAppVersion() {
        return getAppPackageInfo().versionCode;
    }

    public static String getAppVersionName() {
        return getAppPackageInfo().versionName;
    }

    public static String getAppId() {
        return CoreApp.getInstance().getQbConfigs().getAppId();
    }

    public static String getAuthorizationKey() {
        return CoreApp.getInstance().getQbConfigs().getAuthKey();
    }

    public static String getAuthorizationSecret() {
        return CoreApp.getInstance().getQbConfigs().getAuthSecret();
    }

    public static String getAccountKey() {
        return CoreApp.getInstance().getQbConfigs().getAccountKey();
    }

    public static String getApiDomain() {
        return CoreApp.getInstance().getQbConfigs().getApiDomain();
    }

    public static String getChatDomain() {
        return CoreApp.getInstance().getQbConfigs().getChatDomain();
    }

    public static String getJanusServerURL() {
        return CoreApp.getInstance().getQbConfigs().getJanusServerUrl();
    }

    private static PackageInfo getAppPackageInfo() {
        Context context = CoreApp.getInstance();
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}