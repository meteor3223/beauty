package com.xym.beautygallery.base;

import android.os.Environment;

import com.xym.beautygallery.utils.Utils;

/**
 * Created by root on 7/12/16.
 */
public class Constants {
    public static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    private static final String BASE_ACTION = BeautyApplication.getInstance().getPackageName() + ".base";

    public static String ACTION_CHANNEL_REFRESH = BASE_ACTION + ".action_channel_refresh";
    public static String ACTION_MZITU_REFRESH = BASE_ACTION + ".action_mzitu_refresh";
    public static String ACTION_TAG_LIST_REFRESH = BASE_ACTION + ".action_tag_list_refresh";
    public static String ACTION_TAG_REFRESH = BASE_ACTION + ".action_tag_refresh";
    public static String ACTION_FAV_REFRESH = BASE_ACTION + ".action_fav_refresh";
    public static String ACTION_BROWSE_REFRESH = BASE_ACTION + ".action_browse_refresh";
    public static String SERVER_TOTAL_PIC_BASE_URL = "http://beautygallery.file.alimmdn.com/v4/";

    public static String SERVER_CHANNEL_URL = SERVER_TOTAL_PIC_BASE_URL + "channel_url_multi";
    public static String SERVER_APP_RECOMMEND_URL = SERVER_TOTAL_PIC_BASE_URL + "appRecommend";

    public static final String APP_PATH = Environment.getExternalStorageDirectory().getPath()
            + "/beauty_gallery/";
    public static final String PIC_SAVE_PATH = APP_PATH
            + "beauty_download/";

    public static final String DOWNLOAD_PTH = Environment.getExternalStorageDirectory().getPath() + "/apk_download/";

    public static final String SERVER_DATA_CACHE_FILE = "beautygirl_url_list.txt";
    public static final String LOCAL_URL_ASSETS = "file:///android_asset/html/beauty_1.html";

    //server json key
    public static final String SERVER_JSON_ROOT_KEY = "data";
    public static final String SERVER_JSON_URL_DES_KEY = "des";
    public static final String SERVER_JSON_URL_ADDRESS_KEY = "address";
    public static final String SERVER_JSON_ADV_HIDE_KEY = "hide";
    public static final String SERVER_JSON_PIC_TOTAL_KEY = "pic_total";
    public static final String SERVER_JSON_PIC_SUMMARIZE_KEY = "summarize";
    public static final String SERVER_JSON_PIC_URL_KEY = "pic_url";
    public static final String SERVER_JSON_PIC_NAME_KEY = "pic_name";
    public static final String SERVER_JSON_PIC_WIDTH_KEY = "width";
    public static final String SERVER_JSON_PIC_HEIGHT_KEY = "height";

    public static final String SERVER_JSON_ALBUM_NAME = "album_name";
    public static final String SERVER_JSON_ALBUM_ADDRESS = "album_address";
    public static final String SERVER_JSON_ALBUM_THUMB = "album_thumb";
    public static final String SERVER_JSON_ALBUM_PICS = "album_pics";
    public static final String SERVER_JSON_ALBUM_WIDTH = "album_width";
    public static final String SERVER_JSON_ALBUM_HEIGHT = "album_height";

    public static final String SERVER_JSON_TAG_NAME = "tag_name";
    public static final String SERVER_JSON_TAG_ADDRESS_ROOT = "tag_address_root";
    public static final String SERVER_JSON_TAG_PAGE_NUM = "tag_page_num";
    public static final String SERVER_JSON_TAG_CLASSIFY = "classify";
    public static final String SERVER_JSON_TAG_DES = "tag_des";

    public static final String CLASSIFY = "分类";

    public static final int PIC_LOADING_WAIT_TIME = 1000;
    public static final int PIC_PUSH_CACHE_NUMBER = 21;

    public static final int ACTION_MAIN = 0;
    public static final int ACTION_SETTING = 1;
//    public static final int ACTION_SUGGESTION = 1;
//    public static final int ACTION_FAVORITE = 2;
//    public static final int ACTION_SETTING = 3;

    public static final int LOAD_AD_DELAY_COUNT = 3;

    public static final int INTERSTITIAL_AD_WIDTH = 600;
    public static final int INTERSTITIAL_AD_HEIFHT = 500;

    public static final int DEFAULT_ITEM_DECORATION = 4;

    public static final String ACTION_DOWNLOAD_STATUS_UPDATE = "download_status_update";
    public static final String ACTION_DOWNLOAD_PERCENT_UPDATE = "download_percent_update";
}
