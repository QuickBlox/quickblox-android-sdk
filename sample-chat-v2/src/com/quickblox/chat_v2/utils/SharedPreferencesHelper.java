package com.quickblox.chat_v2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author: Andrew Dmitrenko Date: 08.04.13
 */
public class SharedPreferencesHelper {


    public static String getUserPicURL(Context context) {
        return getSharedPreferences(context).getString(GlobalConsts.USERPICURL, "");
    }

    public static void setUserPicURL(Context context, String userPicURL) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(GlobalConsts.USERPICURL, userPicURL);
        editor.commit();
    }

    public static int getUserPicID(Context context) {
        return getSharedPreferences(context).getInt(GlobalConsts.USERPICID, 0);
    }

    public static void setUserPicID(Context context, int userPicID) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(GlobalConsts.USERPICID, userPicID);
        editor.commit();
    }

    public static String getFBUsername(Context context) {
        return getSharedPreferences(context).getString(GlobalConsts.FBUSERNAME, "");
    }

    public static void setFbUsername(Context context, String fbUsername) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(GlobalConsts.FBUSERNAME, fbUsername);
        editor.commit();
    }

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
