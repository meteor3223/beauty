package com.xym.beautygallery.ad;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.xym.beautygallery.ad.OnlinePrefsManager.requestAllPrefs;

public class AdManager implements OnlinePrefsManager.ConfigRequestListener {

    private class NativeAdControl {
        public boolean enable;
        public int gap;
        public List<NativeAd.AdType> allowType;

        @Override
        public String toString() {
            return "NativeAdControl{" +
                    "enable=" + enable +
                    ", gap=" + gap +
                    ", allowType=" + allowType +
                    '}';
        }
    }

    private static AdManager mManager;
    private Context mCtx;
    private Map<NativeAd.AdType, List<NativeAd>> mAdInfos;
    private Map<String, NativeAdControl> mNativeAdControl;
    private AtomicBoolean isRequesting = new AtomicBoolean(false);
    private static final String AD_KEY = "native_ad";
    private static final String AD_GAP_KEY = "native_ad_control";
    private static final int DEFAULT_GAP = 5;

    public static AdManager getInstance(Context context) {
        if (mManager == null) {
            synchronized (AdManager.class) {
                if (mManager == null) {
                    mManager = new AdManager(context);
                }
            }
        }
        return mManager;
    }

    private AdManager(Context context) {
        mCtx = context;
        mAdInfos = new HashMap<>();
        mNativeAdControl = new HashMap<>();
    }

    public void init() {
        if (isRequesting.compareAndSet(false, true)) {
            requestAllPrefs(mCtx, this);
            parse();
            isRequesting.set(true);
        }
    }

    private void parse() {
        String json = OnlinePrefsManager.getDataByKey(mCtx, AD_KEY);
        if (!TextUtils.isEmpty(json)) {
            try {
                mAdInfos.clear();
                JSONObject jsonObject = new JSONObject(json);
                JSONArray jsonArray = jsonObject.optJSONArray("ad");
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject adJson = jsonArray.getJSONObject(i);
                        NativeAd adInfo = new NativeAd();
                        adInfo.adType = NativeAd.AdType.valueOf(adJson.optString("type").toUpperCase());
                        adInfo.imgUrl = adJson.optString("img");
                        adInfo.desc = adJson.optString("desc");
                        adInfo.id = adJson.optString("id");
                        JSONObject extraJson = adJson.optJSONObject("extras");
                        if (extraJson != null && extraJson.length() > 0) {
                            Map<String, String> extraMaps = new HashMap<>();
                            Iterator<String> keys = extraJson.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                extraMaps.put(key, extraJson.optString(key));
                            }
                            adInfo.extras = extraMaps;
                        }
                        List<NativeAd> adInfos = mAdInfos.get(adInfo.adType);
                        if (adInfos == null) {
                            adInfos = new ArrayList<>();
                        }
                        adInfos.add(adInfo);
                        mAdInfos.put(adInfo.adType, adInfos);
                    }
                }
            } catch (JSONException e) {
            }
        }
        json = OnlinePrefsManager.getDataByKey(mCtx, AD_GAP_KEY);
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject != null && jsonObject.length() > 0) {
                    Iterator<String> keys = jsonObject.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject controlJson = jsonObject.optJSONObject(key);
                        NativeAdControl control = new NativeAdControl();
                        control.gap = controlJson.getInt("gap");
                        control.enable = controlJson.getBoolean("enable");
                        JSONArray allowTypeJson = controlJson.getJSONArray("allowType");
                        if (allowTypeJson != null && allowTypeJson.length() > 0) {
                            control.allowType = new ArrayList<>();
                            for (int i = 0; i < allowTypeJson.length(); i++) {
                                control.allowType.add(NativeAd.AdType.valueOf(allowTypeJson.getString(i).toUpperCase()));
                            }
                        }
                        mNativeAdControl.put(key, control);
                    }
                }
            } catch (JSONException e) {
            }
        }
    }

    public NativeAd randomAdInfo(NativeAd.AdType type) {
        List<NativeAd> info = mAdInfos.get(type);
        if (info != null) {
            Random random = new Random();
            int index = random.nextInt(info.size());
            return info.get(index);
        }
        return null;
    }

    public NativeAd randomAdInfo() {
        Random random = new Random();
        int index = random.nextInt(mAdInfos.size());
        NativeAd.AdType type = (NativeAd.AdType) mAdInfos.keySet().toArray()[index];
        return randomAdInfo(type);
    }

    public NativeAd randomAdInfo(String id) {
        NativeAdControl control = mNativeAdControl.get(id);
        if (control != null && control.allowType != null && control.allowType.size() > 0) {
            Random random = new Random();
            int index = random.nextInt(control.allowType.size());
            NativeAd.AdType type = control.allowType.get(index);
            return randomAdInfo(type);
        } else {
            return randomAdInfo();
        }
    }

    public boolean hasNativeAd() {
        return mAdInfos.size() > 0;
    }

    public boolean hasNativeAd(NativeAd.AdType type) {
        if (mAdInfos.containsKey(type)) {
            return mAdInfos.get(type).size() > 0;
        }
        return false;
    }

    @Override
    public void onDataReceived() {
        parse();
    }

    public int getNativeAdGap(String id) {
        if (mNativeAdControl.containsKey(id)) {
            return mNativeAdControl.get(id).gap;
        } else {
            return DEFAULT_GAP;
        }
    }

    public boolean enableAd(String id) {
        if (mNativeAdControl.containsKey(id)) {
            return mNativeAdControl.get(id).enable;
        } else {
            return false;
        }
    }
}
