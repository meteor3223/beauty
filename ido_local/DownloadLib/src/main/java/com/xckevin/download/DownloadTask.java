package com.xckevin.download;

import android.text.TextUtils;

public class DownloadTask {

    public static final String ID = "_id";
    public static final String URL = "a";
    public static final String MIMETYPE = "b";
    public static final String SAVEPATH = "c";
    public static final String FINISHEDSIZE = "d";
    public static final String TOTALSIZE = "e";
    public static final String NAME = "f";
    public static final String STATUS = "g";

    public static final int STATUS_PENDDING = 1 << 0;

    public static final int STATUS_RUNNING = 1 << 1;

    public static final int STATUS_PAUSED = 1 << 2;

    public static final int STATUS_CANCELED = 1 << 3;

    public static final int STATUS_FINISHED = 1 << 4;

    public static final int STATUS_ERROR = 1 << 5;

    private String taskId;

    private String filename;

    private String url;

    private String mimeType;

    private String downloadSavePath;

    private long downloadFinishedSize;

    private long downloadTotalSize;

    // @Transparent no need to persist
    private long downloadSpeed;

    private int status;

    public DownloadTask() {
        downloadFinishedSize = 0;
        downloadTotalSize = 0;
        status = STATUS_PENDDING;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof DownloadTask)) {
            return false;
        }
        DownloadTask task = (DownloadTask) o;
        if (this.filename == null || this.downloadSavePath == null) {
            if(this.url!=null) {
                return this.url.equals(task.url);
            }else {
                return false;
            }
        }
        //return this.filename.equals(task.filename) && this.url.equals(task.url) && this.downloadSavePath.equals(task.downloadSavePath);
        return this.url.equals(task.url);
    }

    @Override
    public int hashCode() {
        if (!TextUtils.isEmpty(url) || filename != null) {
            final int prime = 31;
            int result = 1;
            //result = prime * result + ((filename == null) ? 0 : filename.hashCode());
            result = prime * result + ((url == null) ? 0 : url.hashCode());
            return result;
        } else {
            return super.hashCode();
        }
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String id) {
        this.taskId = id;
    }

    public String getFileName() {
        return filename;
    }

    public void setFileName(String name) {
        this.filename = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDownloadSavePath() {
        return downloadSavePath;
    }

    public void setDownloadSavePath(String downloadSavePath) {
        this.downloadSavePath = downloadSavePath;
    }

    public long getDownloadFinishedSize() {
        return downloadFinishedSize;
    }

    public void setDownloadFinishedSize(long downloadFinishedSize) {
        this.downloadFinishedSize = downloadFinishedSize;
    }

    public long getDownloadTotalSize() {
        return downloadTotalSize;
    }

    public void setDownloadTotalSize(long downloadTotalSize) {
        this.downloadTotalSize = downloadTotalSize;
    }

    public long getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(long downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
