package com.xym.beautygallery.base.stats;

/**
 * Created by zhouzhiyong on 15/12/3.
 */
public class StatsReportConstants {
    //cleaner
    //此处定义常量，上报的key尽量简写
    public static final String REPORT_EXAMPLE = "rt";

    /**
     * page swap count.
     */
    public static final String ENTRY_PAGE_INTRODUCE_ACTIVITY = "IntroPg";
    public static final String ENTRY_PAGE_SETTING_ACTIVITY = "SetPg";
    public static final String ENTRY_PAGE_MZITU_FRAGMENT = "MzituFraPg";
    public static final String ENTRY_PAGE_CLASSIFY_FRAGMENT = "ClassFraPg";
    public static final String ENTRY_PAGE_FAVORITE_FRAGMENT = "FavPg";
    public static final String ENTRY_PAGE_CLASSIFY_DETAIL_ACTIVITY = "ClassDetPg";
    public static final String ENTRY_PAGE_BROWSE_ACTIVITY = "BrowPg";
    public static final String ENTRY_PAGE_BROWSE_ALBUM_ACTIVITY = "BrowAlbumPg";

    /**
     * eventid.
     */
    public static final String ADD_FAVORITE_EVENT_ID = "add_love";
    public static final String REMOVE_FAVORITE_EVENT_ID = "remove_love";
    public static final String CLICK_TO_VIEW_EVENT_ID = "click_view";

    public static final String BAIDU_MSSP_EVENT_ID = "mssp";
    public static final String IMAGE_DOWNLOAD_EVENT_ID = "image_download";
    public static final String RATE_US_EVENT_ID = "rate_us";

    public static final String  ALBUM_ACTIVITY_SHOW_EVENT_ID = "album_activity_show";
    public static final String  CUSTOM_APP_AD_EVENT_ID = "custom_app_ad";

    /**
     * label
     */
    public static final String URL_ADDRESS_LABEL = "url";


    public static final String BAIDU_MSSP_LABEL_REQUEST = "request";
    public static final String BAIDU_MSSP_LABEL_PRESENT = "present";
    public static final String BAIDU_MSSP_LABEL_DISMISSED = "dismissed";
    public static final String BAIDU_MSSP_LABEL_FAILED = "failed";
    public static final String BAIDU_MSSP_LABEL_CLICK = "click";
    public static final String BAIDU_MSSP_LABEL_USER_CLOSE = "close";

    public static final String  CUSTOM_APP_AD_LABEL_SHOW = "show";
    public static final String  CUSTOM_APP_AD_LABEL_DOWNLOAD = "download";
    public static final String  CUSTOM_APP_AD_LABEL_DOWNLOAD_SUCCESS = "download_success";
    public static final String  CUSTOM_APP_AD_LABEL_INSTALL = "install";
    public static final String  CUSTOM_APP_AD_LABEL_START = "start";

    public static final String NATIVE_AD_SHOW = "nats";
    public static final String NATIVE_AD_CLICK = "natc";
}
