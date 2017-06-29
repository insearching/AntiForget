package com.antiforget.antiforget;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefsHelper implements ISharedPrefsHelper {

    private static final String PREFS_NAME = "prefs";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private static final String NO_DEVICE = "no_device";
    private static final String SEARCH_INTERVAL = "search_interval";

    public SharedPrefsHelper(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = preferences.edit();
    }

    @Override
    public void setNoDeviceState(boolean flag) {
        editor.putBoolean(NO_DEVICE, flag).apply();
    }

    @Override
    public boolean isNoDevice() {
        return preferences.getBoolean(NO_DEVICE, false);
    }

    @Override
    public void clearAllMocks() {
        setNoDeviceState(false);
        setSearchInterval(0);
    }

    @Override
    public void setSearchInterval(int intervalSeconds) {
        editor.putLong(SEARCH_INTERVAL, intervalSeconds * 1000).apply();
    }

    @Override
    public long getSearchInterval() {
        return preferences.getLong(SEARCH_INTERVAL, 0);
    }
}
