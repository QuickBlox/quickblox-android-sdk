package com.quickblox.chat_v2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.quickblox.chat_v2.activitys.MainActivity;

/**
 * @author: Andrew Dmitrenko Date: 08.04.13
 */
public class SharedPreferencesHelper {
	

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
