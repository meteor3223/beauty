package com.xckevin.download;

import java.util.List;

public interface DownloadProvider {

    public void saveDownloadTask(DownloadTask task);

    public void updateDownloadTask(DownloadTask task);

    public DownloadTask findDownloadTaskByUrl(String url);

    public DownloadTask findDownloadTask(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy);

    public List<DownloadTask> getAllDownloadTask();

    public void notifyDownloadStatusChanged(DownloadTask task);

    public void deleteOldData(String url);
}