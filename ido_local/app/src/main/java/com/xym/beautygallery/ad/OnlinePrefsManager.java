package com.xym.beautygallery.ad;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.xym.beautygallery.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class OnlinePrefsManager {

    public interface ConfigRequestListener {
        void onDataReceived();
    }

    public static final String BASE_URL = "http://config.91root.cn:8200/interface/get_config";
    public static final String APP_ID = "gwgirl";

    public static void requestAllPrefs(final Context context, final ConfigRequestListener listener) {
        if (!Utils.isNetworkAvaialble(context)) return;
        new Thread() {
            @Override
            public void run() {
                String data = AdUtils.requestJson(appendUrl(context));
                if (!TextUtils.isEmpty(data)) {
                    try {
                        JSONObject js = new JSONObject(data);
                        String configs = js.optString("configs");
                        AdConfigMgr.putString(context, "configs", configs);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (listener != null) {
                    listener.onDataReceived();
                }
            }
        }.start();

    }

    public static String getDataByKey(Context context, String key) {
        String config = AdConfigMgr.getString(context, "configs", "");
        if (!TextUtils.isEmpty(config)) {
            try {
                JSONObject js = new JSONObject(config);
                return js.optString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String appendUrl(Context context) {
        int version = 1;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (info != null) {
                version = info.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return BASE_URL + "?" + "appid=" + APP_ID + "&sdkv=" + Build.VERSION.SDK_INT + "&version=" + version + "&channel=" + Utils.getChannelId(context) + "&multi=" + 1;
    }

}
