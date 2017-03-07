package com.xym.beautygallery.appinfo;

/**
 * Created by root on 12/5/16.
 */
public class AppRecommend {
    public String appName;
    public String pkgName;
    public String iconUrl;
    public String pkgUrl;
    public String pkgDes;
    public String version;
    public long pkgSize;

    @Override
    public String toString() {
        return "AppRecommend{" +
                ", appName='" + appName + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", pkgUrl='" + pkgUrl + '\'' +
                ", pkgDes='" + pkgDes + '\'' +
                ", version='" + version + '\'' +
                ", pkgSize=" + pkgSize +
                '}';
    }
}
