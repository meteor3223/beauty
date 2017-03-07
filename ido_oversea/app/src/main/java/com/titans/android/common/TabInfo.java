package com.titans.android.common;

import android.os.Bundle;
import android.support.v4.app.Fragment;


public final class TabInfo {
    public final String tag;
    public final Class<?> clss;
    public final String title;
    public final int icon;
    public Bundle data;
    public Fragment fragment;
    public int titleColor;

    public TabInfo(String tag, Class<?> clss, String title, int icon, Bundle data) {
        this.tag = tag;
        this.clss = clss;
        this.title = title;
        this.icon = icon;
        this.data = data;
        if (this.data == null) {
            this.data = new Bundle();
            this.data.putString("tag", tag);
        }
    }
}