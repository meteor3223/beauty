package com.xym.beautygallery.ad;

import android.view.View;

import com.xym.beautygallery.base.stats.StatsReportConstants;
import com.xym.beautygallery.base.stats.StatsWrapper;

import java.util.HashMap;
import java.util.Map;

public class NativeAd {

    public enum AdType {
        APP,
        SHOP,
        LIVE
    }

    public String id;
    public String imgUrl;
    public String desc;
    public AdType adType;
    public Map<String, String> extras;

    @Override
    public String toString() {
        return "AdInfo{" +
                "imgUrl='" + imgUrl + '\'' +
                ", desc='" + desc + '\'' +
                ", adType=" + adType +
                ", extras=" + extras +
                '}';
    }

    public void performClick(final View adView, final String naid) {
        if (adView != null) {
            final HashMap<String, String> extra = new HashMap<>();
            extra.put("adid", id);
            StatsWrapper.onEvent(adView.getContext(), StatsReportConstants.NATIVE_AD_SHOW, naid, extra);
            adView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatsWrapper.onEvent(adView.getContext(), StatsReportConstants.NATIVE_AD_CLICK, naid, extra);
                    switch (adType) {
                        case APP:
                            break;
                        case SHOP:
                            AdUtils.startTaobao(adView.getContext(), extras.get("url"));
                            break;
                    }
                }
            });
        }
    }
}
