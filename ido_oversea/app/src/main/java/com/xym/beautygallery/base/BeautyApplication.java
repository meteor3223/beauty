package com.xym.beautygallery.base;

import android.app.Application;
import android.os.Environment;

import com.xckevin.download.DownloadConfig;
import com.xckevin.download.DownloadManager;
import com.xym.beautygallery.appinfo.AppManager;
import com.xym.beautygallery.base.stats.StatsWrapper;
import com.xym.beautygallery.module.DataManager;
import com.xym.beautygallery.utils.Utils;

import java.io.File;

/**
 * Created by root on 7/12/16.
 */
public class BeautyApplication extends Application {
    private static BeautyApplication mInstancce;
    private RefreshInterface mRefreshCallBack;
    private LoveRefreshInterface mLoveRefreshCallBack;
    private MzituLoveRefreshInterface mMzituLoveRefreshCallBack;
    private ZipaiLoveRefreshInterface mZipaiLoveRefreshCallBack;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstancce = this;
        onAppStart();
    }

    private void onAppStart() {
        StatsWrapper.initStatsSDK(this);
        if (Utils.isMainProcess(this)) {
            doInMainProcess();
        }
    }

    private void doInMainProcess() {
        AppManager.getInstance(this).listenSystemBroadcasts();
        initDownloadManager();
        doAsyncTask();
        DataManager.getInstance(this).requestChannelDataFromServer();
    }

    private void doAsyncTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AppManager.getInstance(BeautyApplication.this).loadAppData();
            }
        }).start();
    }

    public static BeautyApplication getInstance() {
        return mInstancce;
    }

    //ImageDetailsActivity中的callback
    public void handleRefreshCallBack() {
        if (mRefreshCallBack != null) {
            mRefreshCallBack.doRefresh();
        }
    }

    public void setRefreshCallBack(RefreshInterface callback) {
        this.mRefreshCallBack = callback;
    }

    public interface RefreshInterface {
        void doRefresh();
    }

    //其他页面刷新收藏页面中的callback
    public void handleLoveRefreshCallBack(int pos, int isLove) {
        if (mLoveRefreshCallBack != null) {
            mLoveRefreshCallBack.doLoveRefresh(pos, isLove);
        }
    }

    public void setLoveRefreshCallBack(LoveRefreshInterface callback) {
        this.mLoveRefreshCallBack = callback;
    }

    public interface LoveRefreshInterface {
        void doLoveRefresh(int pos, int isLove);
    }

    //收藏页面去除收藏时，mzitu页面更新收藏
    public void handleMzituLoveRefreshCallBack(int pos, int isLove) {
        if (mMzituLoveRefreshCallBack != null) {
            mMzituLoveRefreshCallBack.doMzituLoveRefresh(pos, isLove);
        }
    }

    public void setMzituLoveRefreshCallBack(MzituLoveRefreshInterface callback) {
        this.mMzituLoveRefreshCallBack = callback;
    }

    public interface MzituLoveRefreshInterface {
        void doMzituLoveRefresh(int pos, int isLove);
    }

    public interface ZipaiLoveRefreshInterface {
        void doZipaiLoveRefresh(int pos, int isLove);
    }

    private void initDownloadManager() {
        DownloadManager downloadMgr = DownloadManager.getInstance();
        // custom configuration
        DownloadConfig.Builder builder = new DownloadConfig.Builder(this);
        String downloadPath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "apk_download";
        } else {
            downloadPath = this.getFilesDir() + File.separator + "apk_download";
        }
        File downloadFile = new File(downloadPath);
        if (!downloadFile.isDirectory() && !downloadFile.mkdirs()) {
            //throw new IllegalAccessError(" cannot create download folder");
        }
        builder.setDatabasePath(getFilesDir().getAbsolutePath());
        builder.setDownloadSavePath(downloadPath);
        builder.setMaxDownloadThread(5);

        downloadMgr.init(builder.build());
    }
}
