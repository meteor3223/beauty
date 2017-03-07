package com.xym.beautygallery.ad;

import android.content.Context;
import android.content.SharedPreferences;

public class AdConfigMgr {

    private static final String PREFS_FILE_NAME = "adconfig";
    private static final String PREFS_AD_CONFIG = "ad_config";

    private static SharedPreferences sPrefs;

    private static void initSharePrefences(Context ctx) {
        if (sPrefs == null) {
            sPrefs = ctx.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        }
    }

    public static void setAdConfig(Context context, String config) {
        initSharePrefences(context);
        sPrefs.edit().putString(PREFS_AD_CONFIG, config).commit();
    }

    public static String getAdConfig(Context context) {
        return sPrefs.getString(PREFS_AD_CONFIG, "");
    }


    public static String getString(Context context, String key, String def) {
        initSharePrefences(context);
        return sPrefs.getString(key, def);
    }

    public static void putString(Context context, String key, String value) {
        initSharePrefences(context);
        sPrefs.edit().putString(key, value).apply();
    }

    public static long getLong(Context context, String key, long def) {
        initSharePrefences(context);
        return sPrefs.getLong(key, def);
    }

    public static void putLong(Context context, String key, long value) {
        initSharePrefences(context);
        sPrefs.edit().putLong(key, value).apply();
    }


    public static boolean getBoolean(Context context, String key, boolean def) {
        initSharePrefences(context);
        return sPrefs.getBoolean(key, def);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        initSharePrefences(context);
        sPrefs.edit().putBoolean(key, value).apply();
    }

    public static int getInt(Context context, String key, int def) {
        initSharePrefences(context);
        return sPrefs.getInt(key, def);
    }

    public static void putInt(Context context, String key, int value) {
        initSharePrefences(context);
        sPrefs.edit().putInt(key, value).apply();
    }

}
