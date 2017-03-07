package com.xym.beautygallery.module;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.xym.beautygallery.appinfo.AppInfoSnapshot;
import com.xym.beautygallery.appinfo.AppManager;
import com.xym.beautygallery.appinfo.AppRecommend;
import com.xym.beautygallery.base.AppConfigMgr;
import com.xym.beautygallery.base.Constants;
import com.xym.beautygallery.base.FeatureConfig;
import com.xym.beautygallery.base.StringRequestTtf8;
import com.xym.beautygallery.base.VolleySingleton;
import com.xym.beautygallery.download.DownloadService;
import com.xym.beautygallery.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by root on 7/12/16.
 */
public class DataManager {
    private VolleySingleton mVolleyInstance;
    private String myTag = "myTag";
    private final static boolean DEBUG = FeatureConfig.DEBUG;
    private final static String TAG = "DataManager";
    private static DataManager mInstance;
    private Context mContext;
    private static int MAX_PIC_CACHE_NUM = 105;

    private HashMap<String, TagInfo> tagInfoHashMap;
    private HashMap<String, List<String>> tagNameList;

    private int channelIndex = 0;
    private boolean isMultiMode = false;
    private List<String> channelListName;
    private List<String> channelListNameEn;
    private List<Integer> channelListUrl;
    private boolean isChannelPulled = false;
    private boolean mzituRefreshed = false;
    private boolean mTagRefreshed = false;
    private boolean mAppRecommendRefreshed = false;

    private int mMzituAlbumIndex = 0;
    private TagInfo mPicMzituAlbumListData;
    private List<AlbumInfo> mPicMzituTotalListData;
    private boolean isMzituRequestingCache = false;
    private List<AlbumInfo> mPicMzituCacheListData;

    private String mCurrentTag;
    private StringRequestTtf8 mCurrentTagRequest = null;
    private int mTagAlbumIndex = 0;
    private TagInfo mPicTagAlbumListData;
    private List<AlbumInfo> mPicTagTotalListData;
    private boolean isTagRequestingCache = false;
    private List<AlbumInfo> mPicTagCacheListData;

    private List<AlbumInfo> mPicFavListData;

    private AlbumInfo mCurrentAlbum;
    private StringRequestTtf8 mCurrentAlbumRequest = null;
    private List<PicInfo> mPicListBrowseData;
    private int mCurrentIndex;
    private boolean isFromFav;
    private boolean isNeedQuit;
    private AppManager mAppManager;
    private AppManager.AppsFilter appsFiler;
    private DownloadService mDownloadService;

    public static DataManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (DataManager.class) {
                if (mInstance == null) {
                    mInstance = new DataManager(context);
                }
            }
        }
        return mInstance;
    }

    private DataManager(Context context) {
        mContext = context.getApplicationContext();

        mVolleyInstance = VolleySingleton.getInstance(mContext);

        tagInfoHashMap = new HashMap<String, TagInfo>();
        tagNameList = new HashMap<String, List<String>>();
        mPicMzituAlbumListData = new TagInfo();
        mPicMzituTotalListData = new ArrayList<>();
        mPicMzituCacheListData = new ArrayList<>();

        mPicTagAlbumListData = new TagInfo();
        mPicTagTotalListData = new ArrayList<>();
        mPicTagCacheListData = new ArrayList<>();

        mPicFavListData = new ArrayList<>();
        mPicFavListData = AlbumFavoriteDB.getInstance(mContext).queryFavoriteData();

        mPicListBrowseData = new ArrayList<>();
        Collections.sort(mPicFavListData, new LoveComparator());

        channelListName = new ArrayList<>();
        channelListNameEn = new ArrayList<>();
        channelListUrl = new ArrayList<>();
        appsFiler = new AppManager.AppsFilter();
        mAppManager = AppManager.getInstance(mContext);
        mDownloadService = DownloadService.getInstance(mContext);
    }

    public HashMap<String, TagInfo> getTagInfoHashMap() {
        return tagInfoHashMap;
    }

    public TagInfo getTagInfoFromHashMap(String tagName) {
        return tagInfoHashMap.get(tagName);
    }

    public HashMap<String, List<String>> getTagNameList() {
        return tagNameList;
    }

    //mzitu
    public int picMzituPushCache(int number) {
        int tempNum = (number < mPicMzituCacheListData.size()) ? number : mPicMzituCacheListData.size();

        Iterator<AlbumInfo> iter = mPicMzituCacheListData.iterator();
        int picCount = 0;
        while (iter.hasNext()) {
            AlbumInfo newsTemp = iter.next();
            if (picCount < tempNum) {
                AlbumInfo tempPic = new AlbumInfo(newsTemp);
                iter.remove();
                if (isPicFav(tempPic.album_address)) {
                    tempPic.is_love = 1;
                } else {
                    tempPic.is_love = 0;
                }
                mPicMzituTotalListData.add(tempPic);
                picCount++;
            } else {
                break;
            }
        }
        requestMzituPicAlbumDataFromServer();
        return tempNum;
    }

    public List<AlbumInfo> getmPicMzituListData() {
        return mPicMzituTotalListData;
    }

    public void parseMzituPicAlbumData(String infoStr) {
        try {
            if (!TextUtils.isEmpty(infoStr)) {
                JSONArray data = new JSONArray(infoStr);

                int N = data.length();

                for (int i = 0; i < N; i++) {
                    JSONObject appJson = data.getJSONObject(i);
                    AlbumInfo albumInfo = new AlbumInfo();
                    albumInfo.album_name = appJson.optString(Constants.SERVER_JSON_ALBUM_NAME);
                    albumInfo.album_address = appJson.optString(Constants.SERVER_JSON_ALBUM_ADDRESS);
                    albumInfo.album_thumb = appJson.optString(Constants.SERVER_JSON_ALBUM_THUMB);
                    albumInfo.album_pics = appJson.optString(Constants.SERVER_JSON_ALBUM_PICS);
                    albumInfo.album_width = appJson.optInt(Constants.SERVER_JSON_ALBUM_WIDTH);
                    albumInfo.album_height = appJson.optInt(Constants.SERVER_JSON_ALBUM_HEIGHT);
                    mPicMzituCacheListData.add(albumInfo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
        }
    }


    //预先缓存图片地址
    public void requestMzituPicAlbumDataFromServer() {
        if (mzituRefreshed == false) {
            if (mPicMzituCacheListData.size() > 0) {
                sendMzituRefreshReceiver();
                mzituRefreshed = true;
            }
        }
        if (mMzituAlbumIndex < mPicMzituAlbumListData.tag_page_num && (isMzituRequestingCache == false) && (mPicMzituCacheListData.size() < MAX_PIC_CACHE_NUM)) {
            isMzituRequestingCache = true;
            if (Utils.isNetworkAvaialble(mContext)) {
                int pageIndex = mMzituAlbumIndex + 1;
                String url = Constants.SERVER_TOTAL_PIC_BASE_URL + "list_" + String.valueOf(channelIndex) + "/" + mPicMzituAlbumListData.tag_address_root + pageIndex;
                StringRequestTtf8 objRequest = new StringRequestTtf8(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                parseMzituPicAlbumData(s);
                                isMzituRequestingCache = false;
                                mMzituAlbumIndex++;
                                requestMzituPicAlbumDataFromServer();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //server file not exist
                        if (volleyError instanceof ServerError) {
                            mMzituAlbumIndex++;
                        }
                        isMzituRequestingCache = false;
                        requestMzituPicAlbumDataFromServer();
                    }
                });
                objRequest.setShouldCache(false);
                objRequest.setTag(myTag);
                mVolleyInstance.getRequestQueue().add(objRequest);
            }
        }
    }

    public void parseMzituTotalPicAlbumData(String infoStr) {
        try {
            if (!TextUtils.isEmpty(infoStr)) {
                tagInfoHashMap.clear();
                tagNameList.clear();
                JSONArray data = new JSONArray(infoStr);
                int N = data.length();

                for (int i = 0; i < N; i++) {
                    JSONObject tagJson = data.getJSONObject(i);
                    TagInfo tagInfo = new TagInfo();
                    tagInfo.tag_name = tagJson.optString(Constants.SERVER_JSON_TAG_NAME);
                    tagInfo.tag_address_root = tagJson.optString(Constants.SERVER_JSON_TAG_ADDRESS_ROOT);
                    tagInfo.tag_page_num = tagJson.optInt(Constants.SERVER_JSON_TAG_PAGE_NUM);
                    tagInfo.classify = tagJson.optString(Constants.SERVER_JSON_TAG_CLASSIFY);
                    tagInfo.tag_des = tagJson.optString(Constants.SERVER_JSON_TAG_DES);
                    tagInfoHashMap.put(tagInfo.tag_name, tagInfo);
                    mPicMzituAlbumListData = tagInfoHashMap.get("所有");
                    if (tagInfo.classify.equals("null")) {
                        if (!tagInfo.tag_name.equals("所有")) {
                            List<String> classifyList = tagNameList.get(Constants.CLASSIFY);
                            if (classifyList == null) {
                                List<String> tempList = new ArrayList<>();
                                tempList.add(tagInfo.tag_name);
                                tagNameList.put(Constants.CLASSIFY, tempList);
                            } else {
                                classifyList.add(tagInfo.tag_name);
                            }
                        }
                    } else {
                        List<String> classifyList = tagNameList.get(tagInfo.classify);
                        if (classifyList == null) {
                            List<String> tempList = new ArrayList<>();
                            tempList.add(tagInfo.tag_name);
                            tagNameList.put(tagInfo.classify, tempList);
                        } else {
                            classifyList.add(tagInfo.tag_name);
                        }
                    }
                }

                requestMzituPicAlbumDataFromServer();
                sendTagListRefreshReceiver();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestMzituTotalPicAlbumDataFromServer(int index) {
        if (Utils.isNetworkAvaialble(mContext)) {
            String url = Constants.SERVER_TOTAL_PIC_BASE_URL + "list_" + String.valueOf(index) + "/mzitu_album_list";

            StringRequestTtf8 objRequest = new StringRequestTtf8(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            parseMzituTotalPicAlbumData(s);
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                }
            });
            objRequest.setShouldCache(false);
            objRequest.setTag(myTag);
            mVolleyInstance.getRequestQueue().add(objRequest);
        } else {

        }
    }

    //tag
    public String getmCurrentTag() {
        return mCurrentTag;
    }

    public TagInfo getmPicTagAlbumListData() {
        return mPicTagAlbumListData;
    }

    public void setmCurrentTag(String mCurrentTag) {
        this.mCurrentTag = new String(mCurrentTag);
    }

    public void setmPicTagAlbumListData(TagInfo mPicTagAlbumListData) {
        this.mPicTagAlbumListData = mPicTagAlbumListData;
    }

    public int picTagPushCache(int number) {
        int tempNum = (number < mPicTagCacheListData.size()) ? number : mPicTagCacheListData.size();

        Iterator<AlbumInfo> iter = mPicTagCacheListData.iterator();
        int picCount = 0;
        while (iter.hasNext()) {
            AlbumInfo newsTemp = iter.next();
            if (picCount < tempNum) {
                AlbumInfo tempPic = new AlbumInfo(newsTemp);
                iter.remove();
                if (isPicFav(tempPic.album_address)) {
                    tempPic.is_love = 1;
                } else {
                    tempPic.is_love = 0;
                }
                mPicTagTotalListData.add(tempPic);
                picCount++;
            } else {
                break;
            }
        }
        requestTagPicAlbumDataFromServer();
        return tempNum;
    }

    public List<AlbumInfo> getmPicTagListData() {
        return mPicTagTotalListData;
    }

    public void parseTagPicAlbumData(String infoStr) {
        try {
            if (!TextUtils.isEmpty(infoStr)) {
                JSONArray data = new JSONArray(infoStr);
                int N = data.length();

                for (int i = 0; i < N; i++) {
                    JSONObject appJson = data.getJSONObject(i);
                    AlbumInfo albumInfo = new AlbumInfo();
                    albumInfo.album_name = appJson.optString(Constants.SERVER_JSON_ALBUM_NAME);
                    albumInfo.album_address = appJson.optString(Constants.SERVER_JSON_ALBUM_ADDRESS);
                    albumInfo.album_thumb = appJson.optString(Constants.SERVER_JSON_ALBUM_THUMB);
                    albumInfo.album_pics = appJson.optString(Constants.SERVER_JSON_ALBUM_PICS);
                    albumInfo.album_width = appJson.optInt(Constants.SERVER_JSON_ALBUM_WIDTH);
                    albumInfo.album_height = appJson.optInt(Constants.SERVER_JSON_ALBUM_HEIGHT);

                    mPicTagCacheListData.add(albumInfo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
        }
    }


    //预先缓存图片地址
    public void requestTagPicAlbumDataFromServer() {
        if (mTagRefreshed == false) {
            if (mPicTagCacheListData.size() > 0) {
                sendTagRefreshReceiver();
                mTagRefreshed = true;
            }
        }

        if (mTagAlbumIndex < mPicTagAlbumListData.tag_page_num && (isTagRequestingCache == false) && (mPicTagCacheListData.size() < MAX_PIC_CACHE_NUM)) {
            isTagRequestingCache = true;
            if (Utils.isNetworkAvaialble(mContext)) {
                int pageIndex = mTagAlbumIndex + 1;
                String url = Constants.SERVER_TOTAL_PIC_BASE_URL + "list_" + String.valueOf(channelIndex) + "/" + mPicTagAlbumListData.tag_address_root + pageIndex;
                StringRequestTtf8 objRequest = new StringRequestTtf8(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                parseTagPicAlbumData(s);
                                isTagRequestingCache = false;
                                mCurrentTagRequest = null;
                                mTagAlbumIndex++;
                                requestTagPicAlbumDataFromServer();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //server file not exist
                        if (volleyError instanceof ServerError) {
                            mTagAlbumIndex++;
                        }
                        isTagRequestingCache = false;
                        mCurrentTagRequest = null;
                        requestTagPicAlbumDataFromServer();
                    }
                });
                mCurrentTagRequest = objRequest;
                objRequest.setShouldCache(false);
                objRequest.setTag(myTag);
                mVolleyInstance.getRequestQueue().add(objRequest);
            }
        }
    }

    public void requestTagPicAlbumDataFromServerStart() {
        mTagAlbumIndex = 0;
        isTagRequestingCache = false;
        if (mCurrentTagRequest != null) {
            mCurrentTagRequest.cancel();
        }
        mPicTagTotalListData.clear();
        mPicTagCacheListData.clear();
        requestTagPicAlbumDataFromServer();
    }

    public void requestTagPicAlbumDataFromServerStop() {
        mTagAlbumIndex = 0;
        mCurrentTag = null;
        isTagRequestingCache = false;
        if (mCurrentTagRequest != null) {
            mCurrentTagRequest.cancel();
        }
        mPicTagTotalListData.clear();
        mPicTagCacheListData.clear();
    }

    //browse activity
    public AlbumInfo getmCurrentAlbum() {
        return mCurrentAlbum;
    }

    public void setmCurrentAlbum(AlbumInfo mCurrentAlbum) {
        this.mCurrentAlbum = mCurrentAlbum;
    }

    public List<PicInfo> getmPicListBrowseData() {
        return mPicListBrowseData;
    }

    public int getmCurrentIndex() {
        return mCurrentIndex;
    }

    public void setmCurrentIndex(int mCurrentIndex) {
        this.mCurrentIndex = mCurrentIndex;
    }

    public boolean isFromFav() {
        return isFromFav;
    }

    public void setIsFromFav(boolean isFromFav) {
        this.isFromFav = isFromFav;
    }

    public boolean isNeedQuit() {
        return isNeedQuit;
    }

    public void setIsNeedQuit(boolean isNeedQuit) {
        this.isNeedQuit = isNeedQuit;
    }

    public void parseBrowseData(String infoStr) {
        try {
            if (!TextUtils.isEmpty(infoStr)) {
                JSONObject root = new JSONObject(infoStr);
                JSONArray data = root.optJSONArray(Constants.SERVER_JSON_PIC_TOTAL_KEY);

                int N = data.length();
                for (int i = 0; i < N; i++) {
                    JSONObject appJson = data.getJSONObject(i);
                    PicInfo url = new PicInfo();
                    url.pic_url_address = appJson.optString(Constants.SERVER_JSON_PIC_URL_KEY);
                    url.pic_detail = appJson.optString(Constants.SERVER_JSON_PIC_NAME_KEY);
                    url.pic_width = appJson.optInt(Constants.SERVER_JSON_PIC_WIDTH_KEY);
                    url.pic_height = appJson.optInt(Constants.SERVER_JSON_PIC_HEIGHT_KEY);
                    mPicListBrowseData.add(url);
                }
            }
        } catch (JSONException e) {


        }
    }

    public void requestBrowseDataFromServer() {
        String url = Constants.SERVER_TOTAL_PIC_BASE_URL + mCurrentAlbum.album_address;
        StringRequestTtf8 objRequest = new StringRequestTtf8(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        parseBrowseData(s);
                        sendBrowseRefreshReceiver();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                //server file not exist
                sendBrowseRefreshReceiver();
            }
        });
        mCurrentAlbumRequest = objRequest;
        objRequest.setTag(myTag);
        mVolleyInstance.getRequestQueue().add(objRequest);
    }

    public void requestBrowseDataFromServerStart() {
        if (mCurrentAlbumRequest != null) {
            mCurrentAlbumRequest.cancel();
        }
        mPicListBrowseData.clear();
        if (mCurrentAlbum != null) {
            requestBrowseDataFromServer();
        }
    }

    public void requestBrowseDataFromServerStop() {
        if (mCurrentAlbumRequest != null) {
            mCurrentAlbumRequest.cancel();
        }
        mPicListBrowseData.clear();
    }

    public void setChannelIndex(int channelIndex) {
        this.channelIndex = channelIndex;
    }

    public int getChannelIndex() {
        return channelIndex;
    }

    public boolean isMultiMode() {
        return isMultiMode;
    }

    public List<String> getChannelListName() {
        if (Utils.isZh(mContext)) {
            return channelListName;
        } else {
            return channelListNameEn;
        }
    }

    public List<Integer> getChannelListUrl() {
        return channelListUrl;
    }

    private int parseChannelUrl(String s) {
        String currentChannel = Utils.getChannelId(mContext);
        if (TextUtils.isEmpty(s)) return 1;

        try {
            int urlConfig = 1;
            JSONObject root = new JSONObject(s);
            JSONArray data = root.optJSONArray("CONFIG");
            int N = data.length();
            for (int i = 0; i < N; i++) {
                JSONObject channelJson = data.getJSONObject(i);
                String channelName = channelJson.getString("CHANNEL");
                urlConfig = channelJson.getInt("URL");
                if (currentChannel.equals(channelName)) {
                    break;
                }
            }
            JSONArray multi = root.optJSONArray("MULTI");
            N = multi.length();
            channelListName.clear();
            channelListNameEn.clear();
            channelListUrl.clear();
            for (int i = 0; i < N; i++) {
                JSONObject channelJson = multi.getJSONObject(i);
                String channelName = channelJson.getString("NAME");
                String channelNameEn = channelJson.getString("NAME_EN");
                int channelUrl = channelJson.getInt("URL");
                channelListName.add(channelName);
                channelListNameEn.add(channelNameEn);

                channelListUrl.add(channelUrl);
            }
            if (urlConfig == 99) {
                isMultiMode = true;
                channelIndex = AppConfigMgr.getChannelIndex(mContext);
                if (channelIndex == 0) {
                    long currentTime = System.currentTimeMillis();
                    currentTime = currentTime % N;
                    int defaultIndex = (int) currentTime;
                    channelIndex = channelListUrl.get(defaultIndex);
                    AppConfigMgr.setChannelIndex(mContext, channelIndex);
                }
                sendChannelRefreshReceiver();
                return channelIndex;
            } else {
                return urlConfig;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public void requestChannelDataFromServer() {
        if (Utils.isNetworkAvaialble(mContext)) {
            String url = Constants.SERVER_CHANNEL_URL;

            StringRequestTtf8 objRequest = new StringRequestTtf8(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            int index = parseChannelUrl(s);
                            isChannelPulled = true;
                            channelIndex = index;
                            requestMzituTotalPicAlbumDataFromServer(index);
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                }
            });
            objRequest.setShouldCache(false);
            objRequest.setTag(myTag);
            mVolleyInstance.getRequestQueue().add(objRequest);
        } else {

        }
    }

    private boolean checkAppExist(final List<AppInfoSnapshot> appInfoList, String appPkg) {
        for (int j = 0; j < appInfoList.size(); j++) {
            if (appInfoList.get(j).getPackageName().equals(appPkg)) {
                return true;
            }
        }
        return false;
    }

    private void parseAppRecommendData(String s) {
        if (TextUtils.isEmpty(s)) return;
        try {
            final List<AppInfoSnapshot> appInfoList = mAppManager.getApps(appsFiler);
            JSONObject root = new JSONObject(s);
            long shwoCount = root.optLong("count");
            JSONArray data = root.optJSONArray("ad");
            JSONObject appJson;
            int N = data.length();
            for (int i = 0; i < N; i++) {
                AppRecommend tempApp = new AppRecommend();
                appJson = data.getJSONObject(i);
                tempApp.appName = appJson.optString("name");
                tempApp.pkgName = appJson.optString("pkgName");
                tempApp.iconUrl = appJson.optString("iconurl");
                tempApp.pkgUrl = appJson.optString("downurl");
                tempApp.pkgDes = appJson.optString("dec");
                tempApp.version = appJson.optString("version");
                tempApp.pkgSize = appJson.optLong("size");
                AppManager.getInstance(mContext).addmAppRecommendList(tempApp);
            }
            AppManager.getInstance(mContext).setMzituAlbumCountConfig(shwoCount);
            AppManager.getInstance(mContext).findCurrentAppRecommend();
            AppRecommend tempApp = AppManager.getInstance(mContext).getmCurrentAppRecommend();
            if (shwoCount > 0) {
                if (tempApp != null) {
                    int status = mDownloadService.getTaskStatus(tempApp.pkgUrl, tempApp.pkgName);
                    if (status == mDownloadService.STATUS_DOWNLOAD) {
                        mDownloadService.startCommand(tempApp.pkgUrl,
                                null, false);
                    }
                }
            }
            if (AppConfigMgr.getAppAdLast(mContext)) {
                AppConfigMgr.setAppAdLast(mContext, false);
                AppConfigMgr.setAppAdCountTimes(mContext, 0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void requestAppRecommendDataFromServer() {
        if (Utils.isNetworkAvaialble(mContext)) {
            String url = Constants.SERVER_APP_RECOMMEND_URL;

            StringRequestTtf8 objRequest = new StringRequestTtf8(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            mAppRecommendRefreshed = true;
                            parseAppRecommendData(s);
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                }
            });
            objRequest.setShouldCache(false);
            objRequest.setTag(myTag);
            mVolleyInstance.getRequestQueue().add(objRequest);
        } else {

        }
    }

    public boolean isChannelPulled() {
        return isChannelPulled;
    }

    public void sendChannelRefreshReceiver() {
        Intent intent = new Intent(Constants.ACTION_CHANNEL_REFRESH);
        mContext.sendBroadcast(intent);
    }

    public void sendMzituRefreshReceiver() {
        Intent intent = new Intent(Constants.ACTION_MZITU_REFRESH);
        mContext.sendBroadcast(intent);
    }

    public void sendTagListRefreshReceiver() {
        Intent intent = new Intent(Constants.ACTION_TAG_LIST_REFRESH);
        mContext.sendBroadcast(intent);
    }

    public void sendTagRefreshReceiver() {
        Intent intent = new Intent(Constants.ACTION_TAG_REFRESH);
        mContext.sendBroadcast(intent);
    }

    public void sendFavRefreshReceiver() {
        Intent intent = new Intent(Constants.ACTION_FAV_REFRESH);
        mContext.sendBroadcast(intent);
    }

    public void sendBrowseRefreshReceiver() {
        Intent intent = new Intent(Constants.ACTION_BROWSE_REFRESH);
        mContext.sendBroadcast(intent);
    }

    public void requestDataAgain() {
        if (isChannelPulled == false) {
            mzituRefreshed = false;
            mTagRefreshed = false;
            requestChannelDataFromServer();
        } else if (mPicMzituAlbumListData != null && mPicMzituAlbumListData.tag_page_num == 0) {
            mzituRefreshed = false;
            requestMzituTotalPicAlbumDataFromServer(channelIndex);
        } else {
            sendMzituRefreshReceiver();
            sendTagListRefreshReceiver();
            sendTagRefreshReceiver();
            sendBrowseRefreshReceiver();
        }

        if (mAppRecommendRefreshed == false) {
            requestAppRecommendDataFromServer();
        }
    }

    public void switchChannel(int channelIndex) {
        mVolleyInstance.getRequestQueue().cancelAll(myTag);
        mzituRefreshed = false;
        mTagRefreshed = false;
        mMzituAlbumIndex = 0;

        mPicMzituTotalListData.clear();
        isMzituRequestingCache = false;
        mPicMzituCacheListData.clear();

        requestMzituTotalPicAlbumDataFromServer(channelIndex);
    }

    public List<AlbumInfo> getmPicFavListData() {
        return mPicFavListData;
    }

    public boolean isPicFav(String url) {
        boolean ret = false;
        int N = mPicFavListData.size();
        for (int i = 0; i < N; i++) {
            if (mPicFavListData.get(i).album_address.equals(url)) {
                return true;
            }
        }
        return ret;
    }

    public int setFavoriteStatus(AlbumInfo albumInfo) {
        int N = mPicFavListData.size();
        int i = 0;
        for (i = 0; i < N; i++) {
            if (mPicFavListData.get(i).album_address.equals(albumInfo.album_address)) {
                mPicFavListData.get(i).is_love = albumInfo.is_love;
                break;
            }
        }

        if (i == N) {
            //add the first pos
            i = 0;
            mPicFavListData.add(i, albumInfo);
        } else {
            if (albumInfo.is_love == 0) {
                mPicFavListData.remove(i);
            }
        }

        AlbumFavoriteDB.getInstance(mContext).addAndUpdateFavoriteData(albumInfo.album_name, albumInfo.album_address, albumInfo.album_thumb,
                albumInfo.album_pics, albumInfo.is_love, albumInfo.album_width, albumInfo.album_height, albumInfo.love_time);
        return i;
    }

    public void removeFavoriteStatus(AlbumInfo albumInfo) {
        AlbumFavoriteDB.getInstance(mContext).updateFavoriteData(albumInfo.album_name, albumInfo.album_address, albumInfo.album_thumb,
                albumInfo.album_pics, albumInfo.is_love, albumInfo.album_width, albumInfo.album_height, albumInfo.love_time);
    }

    public int removeLovePicMzituList(String url) {
        int N = mPicMzituTotalListData.size();
        int i;
        int ret = -1;
        for (i = 0; i < N; i++) {
            if (mPicMzituTotalListData.get(i).album_address.equals(url)) {
                mPicMzituTotalListData.get(i).is_love = 0;
                ret = i;
                break;
            }
        }
        return ret;
    }
}
