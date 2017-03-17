package com.xym.beautygallery.module;

import com.facebook.ads.NativeAd;

/**
 * Created by root on 3/15/17.
 */
public class MapPicAd {
    public boolean isAd;
    public int picIndex;
    public NativeAd nativeAd;

    public MapPicAd() {
        isAd = false;
        picIndex = 0;
        nativeAd = null;
    }

    @Override
    public String toString() {
        return "MapPicAd{" +
                "isAd=" + isAd +
                ", picIndex=" + picIndex +
                ", nativeAd=" + nativeAd +
                '}';
    }
}
