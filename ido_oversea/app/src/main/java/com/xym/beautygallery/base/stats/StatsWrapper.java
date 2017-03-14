package com.xym.beautygallery.base.stats;

import android.app.Activity;
import android.content.Context;

import com.tendcloud.tenddata.TCAgent;
import com.umeng.analytics.MobclickAgent;
import com.xym.beautygallery.utils.Utils;

import java.util.Map;

/**
 * Created by zhouzhiyong on 15/12/3.
 */
public class StatsWrapper {

    public static void initStatsSDK(Context context) {
        TCAgent.LOG_ON = false;
        TCAgent.init(context, "7F6C7F20DC87498E8E818232994626BC", Utils.getChannelId(context));
        TCAgent.setReportUncaughtExceptions(true);

        MobclickAgent.setCatchUncaughtExceptions(false);
    }

    public static void onEvent(Context context, String eventId) {
        TCAgent.onEvent(context, eventId);

        MobclickAgent.onEvent(context, eventId);
    }

    public static void onEvent(Context context, String eventId, String eventLabel) {
        TCAgent.onEvent(context, eventId, eventLabel);

        MobclickAgent.onEvent(context, eventId, eventLabel);
    }


    public static void onEvent(Context context, String eventId, String eventLabel, Map<String, String> map) {
        TCAgent.onEvent(context, eventId, eventLabel, map);

        MobclickAgent.onEvent(context, eventLabel, map);
    }

    public static void onPageStart(Context context, String pageName) {
        TCAgent.onPageStart(context, pageName);

        MobclickAgent.onPageStart(pageName);
    }

    public static void onPageEnd(Context context, String pageName) {
        TCAgent.onPageEnd(context, pageName);

        MobclickAgent.onPageEnd(pageName);
    }

    public static void onError(Context context, Throwable e) {
        TCAgent.onError(context, e);

        MobclickAgent.reportError(context, e);
    }

    public static void onResume(Activity activity) {
        TCAgent.onResume(activity);

        MobclickAgent.onResume(activity);
    }

    public static void onPause(Activity activity) {
        TCAgent.onPause(activity);

        MobclickAgent.onPause(activity);
    }
}
