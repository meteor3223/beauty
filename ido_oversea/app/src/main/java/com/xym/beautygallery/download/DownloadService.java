package com.xym.beautygallery.download;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.xckevin.download.DownloadListener;
import com.xckevin.download.DownloadManager;
import com.xckevin.download.DownloadTask;
import com.xym.beautygallery.R;
import com.xym.beautygallery.appinfo.AppInfoSnapshot;
import com.xym.beautygallery.appinfo.AppManager;
import com.xym.beautygallery.base.Constants;
import com.xym.beautygallery.utils.PackageUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhouzhiyong on 15-9-22.
 */
public class DownloadService {
    private class MyDownloadListener implements DownloadListener {
        @Override
        public void onDownloadStart(DownloadTask task) {
            mHandler.sendEmptyMessage(MSG_START_DOWNLOAD);
        }

        @Override
        public void onDownloadUpdated(DownloadTask task, long finishedSize, long trafficSpeed) {
            Message msg = mHandler.obtainMessage();
            msg.arg1 = (int) ((float) (task.getDownloadFinishedSize() / (task.getDownloadTotalSize() * 1.0)) * 100);
            msg.what = MSG_PROGRESS_UPDATE;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onDownloadPaused(DownloadTask task) {

        }

        @Override
        public void onDownloadResumed(DownloadTask task) {
            mHandler.sendEmptyMessage(MSG_START_DOWNLOAD);
        }

        @Override
        public void onDownloadSuccessed(DownloadTask task) {
            boolean isSlient = mDownloadList.get(task.getUrl()) == null || mDownloadList.get(task.getUrl());
            if (!isSlient) {
                Message msg = mHandler.obtainMessage();
                msg.obj = task.getDownloadSavePath();
                msg.what = MSG_DOWNLOAD_SUCCESS;
                mHandler.sendMessage(msg);
            }
        }

        @Override
        public void onDownloadCanceled(DownloadTask task) {

        }

        @Override
        public void onDownloadFailed(DownloadTask task) {
        }

        @Override
        public void onDownloadRetry(DownloadTask task) {

        }
    }

    private volatile static DownloadService singleton;
    public final static String ACTION_START_DOWNLOAD = "action_start_download";

    private static final int MSG_START_DOWNLOAD = 1;
    private static final int MSG_PROGRESS_UPDATE = 2;
    private static final int MSG_DOWNLOAD_SUCCESS = 3;
    private static final int MSG_DOWNLOAD_FAILED = 4;
    private static final int MSG_INSTALLED = 5;
    private DownloadManager mDownloadManager;
    private HashMap<String, Boolean> mDownloadList;
    private MyDownloadListener mMyDownloadListener;
    private Context mContext;
    private AppManager mAppManager;
    private AppManager.AppsFilter appsFiler;


    private DownloadService(Context appContext) {
        mContext = appContext;
        mDownloadManager = DownloadManager.getInstance();
        mMyDownloadListener = new MyDownloadListener();
        mDownloadList = new HashMap<>();
        appsFiler = new AppManager.AppsFilter();
        appsFiler.onlyMounted = true;
        appsFiler.includeUpdatedSysApp = false;
        appsFiler.includeSysApp = false;
        mAppManager = AppManager.getInstance(mContext);
    }

    public static DownloadService getInstance(Context cxt) {
        if (singleton == null) {
            synchronized (DownloadService.class) {
                if (singleton == null) {
                    singleton = new DownloadService(cxt.getApplicationContext());
                }
            }
        }
        return singleton;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_DOWNLOAD:
//                    updateProgress(0);
                    sendDownloadStatusUpdateReceiver();
                    break;
                case MSG_PROGRESS_UPDATE:
//                    updateProgress(msg.arg1);
                    sendDownloadPerUpdateReceiver(msg.arg1);
                    break;
                case MSG_DOWNLOAD_FAILED:
                    Toast.makeText(mContext, R.string.apk_download_failed, Toast.LENGTH_SHORT).show();
                    sendDownloadStatusUpdateReceiver();
                    break;
                case MSG_DOWNLOAD_SUCCESS:
                    String path = (String) msg.obj;
//                    if (app != null && app.name != null) {
//                        Toast.makeText(DownloadService.this, app.name+" 可以帮助ROOT手机，点击安装：）", Toast.LENGTH_LONG).show();
//                    }
                    sendDownloadStatusUpdateReceiver();
                    if (AppManager.getInstance(mContext).ismWaitApkInstall()) {
                        PackageUtils.openInstaller(mContext, path);
                    }
                    break;
            }
        }
    };

    public void sendDownloadStatusUpdateReceiver() {
        Intent intent = new Intent(Constants.ACTION_DOWNLOAD_STATUS_UPDATE);
        mContext.sendBroadcast(intent);
    }

    public void sendDownloadPerUpdateReceiver(int per) {
        Intent intent = new Intent(Constants.ACTION_DOWNLOAD_PERCENT_UPDATE);
        intent.putExtra("percent", per);
        mContext.sendBroadcast(intent);
    }

    public static int STATUS_ERROR = 0;
    public static int STATUS_DOWNLOAD = 1;
    public static int STATUS_DOWNLOADING = 2;
    public static int STATUS_INSTALL = 3;
    public static int STATUS_START = 4;

    public int getTaskStatus(String url, String apk_name) {
        DownloadTask task = mDownloadManager.findDownloadTaskByUrl(url);
        final List<AppInfoSnapshot> appInfoList = mAppManager.getApps(appsFiler);
        for (int i = 0; i < appInfoList.size(); i++) {
            if (appInfoList.get(i).getPackageName().equals(apk_name)) {
                return STATUS_START;
            }
        }

        if (task == null) {
            return STATUS_DOWNLOAD;
        }
        int status = task.getStatus();
        if (status == DownloadTask.STATUS_FINISHED && new File(task.getDownloadSavePath()).exists()) {
            return STATUS_INSTALL;
        } else if (status == DownloadTask.STATUS_PAUSED) {
            return STATUS_DOWNLOAD;
        } else if (status == DownloadTask.STATUS_RUNNING || status == DownloadTask.STATUS_PENDDING) {
            return STATUS_DOWNLOADING;
        } else {
            return STATUS_DOWNLOAD;
        }
    }

    public void startCommand(String url, String pkgName, boolean isSilent) {
        if (!TextUtils.isEmpty(url)) {
            DownloadTask task = mDownloadManager.findDownloadTaskByUrl(url);
            if (task == null) {
                task = new DownloadTask();
                task.setUrl(url);
            }
            if (task.getStatus() == DownloadTask.STATUS_FINISHED && new File(task.getDownloadSavePath()).exists()) {
                PackageUtils.openInstaller(mContext, task.getDownloadSavePath());
            } else if (task.getStatus() == DownloadTask.STATUS_PAUSED) {
                DownloadManager.getInstance().resumeDownload(task);
                DownloadManager.getInstance().updateDownloadTaskListener(task, mMyDownloadListener);
            } else if (task.getStatus() == DownloadTask.STATUS_RUNNING) {

            } else {
                DownloadManager.getInstance().addDownloadTask(task, mMyDownloadListener);
            }
            if (mDownloadList.get(task.getUrl()) == null || mDownloadList.get(task.getUrl())) {
                mDownloadList.put(url, isSilent);
            }
        }
    }
}
