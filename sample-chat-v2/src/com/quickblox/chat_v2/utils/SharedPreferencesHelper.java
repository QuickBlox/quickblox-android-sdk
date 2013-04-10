package com.quickblox.chat_v2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.quickblox.chat_v2.activitys.MainActivity;

/**
 * @author: Andrew Dmitrenko Date: 08.04.13
 */
public class SharedPreferencesHelper {
	
	
	public static String getUserPicURL() {
		return getSharedPreferences().getString(GlobalConsts.USERPICURL, "");
	}
	
	public static void setUserPicURL(String userPicURL) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(GlobalConsts.USERPICURL, userPicURL);
		editor.commit();
	}
	
	public static int getUserPicID() {
		return getSharedPreferences().getInt(GlobalConsts.USERPICID, 0);
	}
	
	public static void setUserPicID(int userPicID) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putInt(GlobalConsts.USERPICID, userPicID);
		editor.commit();
	}
	
	public static String getFBUsername() {
		return getSharedPreferences().getString(GlobalConsts.FBUSERNAME, "");
	}
	
	public static void setFbUsername(String fbUsername) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(GlobalConsts.FBUSERNAME, fbUsername);
		editor.commit();
	}

	public static String getLogin() {
		return getSharedPreferences().getString(GlobalConsts.LOGIN, "");
	}
	
	public static void setLogin(String login) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(GlobalConsts.LOGIN, login);
		editor.commit();
	}
	
	public static String getPassword() {
		return getSharedPreferences().getString(GlobalConsts.PASSWORD, "");
	}
	
	public static void setPassword(String password) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(GlobalConsts.PASSWORD, password);
		editor.commit();
	}
	
	private static SharedPreferences getSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	private static SharedPreferences getSharedPreferences() {
		return getSharedPreferences(MainActivity.getContext());
	}
	
}
