package com.quickblox.sample.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.quickblox.sample.core.CoreApp;

public class SharedPrefsHelper {
    private static final String SHARED_PREFS_NAME = "qb";

    private static SharedPrefsHelper instance;

    private SharedPreferences sharedPreferences;

    public static synchronized SharedPrefsHelper getInstance() {
        if (instance == null) {
            instance = new SharedPrefsHelper();
        }

        return instance;
    }

    private SharedPrefsHelper() {
        instance = this;
        sharedPreferences = CoreApp.getInstance().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void delete(String key) {
        if (sharedPreferences.contains(key)) {
            getEditor().remove(key).commit();
        }
    }

    public void savePref(String key, Object value) {
        if (value instanceof Boolean) {
            getEditor().putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            getEditor().putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            getEditor().putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            getEditor().putLong(key, (Long) value);
        } else if (value instanceof String) {
            getEditor().putString(key, (String) value);
        } else if (value instanceof Enum) {
            getEditor().putString(key, value.toString());
        } else if (value != null) {
            throw new RuntimeException("Attempting to save non-supported preference");
        }

        getEditor().commit();
    }

    @SuppressWarnings("unchecked")
    public <T> T getPref(String key) {
        return (T) sharedPreferences.getAll().get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPref(String key, T defValue) {
        T returnValue = (T) sharedPreferences.getAll().get(key);
        return returnValue == null ? defValue : returnValue;
    }

    public boolean has(String key) {
        return sharedPreferences.contains(key);
    }

    private SharedPreferences.Editor getEditor() {
        return sharedPreferences.edit();
    }
}
