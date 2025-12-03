package com.example.cualma.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "CualMaSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_CARNET = "userCarnet";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String carnet) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_CARNET, carnet);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getCarnet() {
        return pref.getString(KEY_CARNET, null);
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
    }
}