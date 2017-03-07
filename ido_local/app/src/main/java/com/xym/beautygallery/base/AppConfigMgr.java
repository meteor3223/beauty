package com.xym.beautygallery.base;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zhouzhiyong on 15/3/14.
 */
public class AppConfigMgr {
    private static final String PREFS_FILE_NAME = "config";


    private static final String PREFS_RETRY_MSG_SIZE = "retry_msg_size";
    private static final String PREFS_STATE_SHORTCUT_SETUP = "state_shortcut_setup";
    private static final String PREFS_STATE_ACCESS_PERMISSION = "state_access_permission";
    private static final String PREFS_STATE_NEED_RATE = "state_need_rate";
    private static final String PREFS_COUNT_TIMES = "count_times";

    private static final String PREFS_APP_AD_LAST = "app_ad_last";
    private static final String PREFS_APP_AD_COUNT_TIMES = "app_ad_count_times";

    private static final String PREFS_CHANNEL_INDEX = "channel_index";

    private static SharedPreferences sPrefs;

    private static void initSharePrefences(Context ctx) {
        if (sPrefs == null) {
            sPrefs = ctx.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        }
    }

    /**
     * 设置未读取消息个数（原始未读取+新内容）
     *
     * @param size
     * @return
     */
    public static void setReplySize(Context context, int size) {
        initSharePrefences(context);
        sPrefs.edit().putInt(PREFS_RETRY_MSG_SIZE, size).commit();
    }

    public static int getReplySize(Context context) {
        initSharePrefences(context);
        return sPrefs.getInt(PREFS_RETRY_MSG_SIZE, 0);
    }

    public static void setShortcutSetup(Context ctx, boolean value) {
        initSharePrefences(ctx);
        sPrefs.edit().putBoolean(PREFS_STATE_SHORTCUT_SETUP, value).apply();
    }

    public static boolean isShortcutSetup(Context ctx) {
        initSharePrefences(ctx);
        return sPrefs.getBoolean(PREFS_STATE_SHORTCUT_SETUP, false);
    }

    public static void setAccessPermission(Context ctx, int versionCode) {
        initSharePrefences(ctx);
        sPrefs.edit().putInt(PREFS_STATE_ACCESS_PERMISSION, versionCode).apply();
    }

    public static int getAccessPermission(Context ctx) {
        initSharePrefences(ctx);
        return sPrefs.getInt(PREFS_STATE_ACCESS_PERMISSION, 0);
    }

    public static void setNeedRate(Context ctx, boolean need) {
        initSharePrefences(ctx);
        sPrefs.edit().putBoolean(PREFS_STATE_NEED_RATE, need).apply();
    }

    public static boolean getNeedRate(Context ctx) {
        initSharePrefences(ctx);
        return sPrefs.getBoolean(PREFS_STATE_NEED_RATE, true);
    }

    public static void setCountTimes(Context ctx, long times) {
        initSharePrefences(ctx);
        sPrefs.edit().putLong(PREFS_COUNT_TIMES, times).apply();
    }

    public static long getCountTimes(Context ctx) {
        initSharePrefences(ctx);
        return sPrefs.getLong(PREFS_COUNT_TIMES, 1);
    }

    public static void setAppAdLast(Context ctx, boolean last) {
        initSharePrefences(ctx);
        sPrefs.edit().putBoolean(PREFS_APP_AD_LAST, last).apply();
    }

    public static boolean getAppAdLast(Context ctx) {
        initSharePrefences(ctx);
        return sPrefs.getBoolean(PREFS_APP_AD_LAST, true);
    }

    public static void setAppAdCountTimes(Context ctx, long count) {
        initSharePrefences(ctx);
        sPrefs.edit().putLong(PREFS_APP_AD_COUNT_TIMES, count).apply();
    }

    public static long getAppAdCountTimes(Context ctx) {
        initSharePrefences(ctx);
        return sPrefs.getLong(PREFS_APP_AD_COUNT_TIMES, 0);
    }

    public static void setChannelIndex(Context ctx, int index) {
        initSharePrefences(ctx);
        sPrefs.edit().putInt(PREFS_CHANNEL_INDEX, index).apply();
    }

    public static int getChannelIndex(Context ctx) {
        initSharePrefences(ctx);
        return sPrefs.getInt(PREFS_CHANNEL_INDEX, 0);
    }
}