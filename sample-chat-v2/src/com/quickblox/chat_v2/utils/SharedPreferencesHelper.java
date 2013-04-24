package com.quickblox.chat_v2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author: Andrew Dmitrenko Date: 08.04.13
 */
public class SharedPreferencesHelper {

    public static String getLogin(Context context) {
        return getSharedPreferences(context).getString(GlobalConsts.LOGIN, "");
    }

    public static void setLogin(Context context, String login) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(GlobalConsts.LOGIN, login);
        editor.commit();
    }

    public static String getPassword(Context context) {
        return getSharedPreferences(context).getString(GlobalConsts.PASSWORD, "");
    }

    public static void setPassword(Context context, String password) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(GlobalConsts.PASSWORD, password);
        editor.commit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }


}
