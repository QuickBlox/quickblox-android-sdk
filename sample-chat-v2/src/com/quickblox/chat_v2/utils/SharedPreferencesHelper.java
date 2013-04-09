package com.quickblox.chat_v2.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.quickblox.chat_v2.activitys.MainActivity;

/**
 * @author: Andrew Dmitrenko
 * Date: 08.04.13
 */
public class SharedPreferencesHelper {


    public static String getAccessToken() {
        return getSharedPreferences().getString(GlobalConsts.ACCESS_TOKEN, "");
    }

    public static void setAccessToken(String accessToken) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(GlobalConsts.ACCESS_TOKEN, accessToken);
        editor.commit();
    }

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
    }

}
