package com.xckevin.download;

import android.os.Handler;
import android.text.TextUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager {

    private static final String TAG = "DownloadManager";

    //	static {
    //		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
    //			DOWNLOAD_DIR = Env.ROOT_DIR + File.separator + "download";
    //		} else {
    //			DOWNLOAD_DIR = Environment.getDataDirectory() + File.separator + "com.huaqian" + File.separator + "databases";
    //		}
    //	}

    private static DownloadManager instance;

    private DownloadConfig config;

    private HashMap<DownloadTask, DownloadOperator> taskOperators = new HashMap<DownloadTask, DownloadOperator>();

    private HashMap<String, WeakReference<DownloadListener>> taskListeners = new HashMap<>();

    private LinkedList<DownloadObserver> taskObservers = new LinkedList<DownloadObserver>();

    private DownloadProvider provider;

    private static Handler handler = new Handler();

    private ExecutorService pool;

    private DownloadManager() {

    }

    public static synchronized DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }

        return instance;
    }

    public void init() {
        config = DownloadConfig.getDefaultDownloadConfig(this);
        provider = config.getProvider(this);
        pool = Executors.newFixedThreadPool(config.getMaxDownloadThread());
    }

    public void init(DownloadConfig config) {
        if (config == null) {
            init();
            return;
        }
        this.config = config;
        provider = config.getProvider(this);
        pool = Executors.newFixedThreadPool(config.getMaxDownloadThread());
    }

    public DownloadConfig getConfig() {
        return config;
    }

    public void setConfig(DownloadConfig config) {
        this.config = config;
    }

    public void addDownloadTask(DownloadTask task) {
        addDownloadTask(task, null);
    }

    public void addDownloadTask(DownloadTask task, DownloadListener listener) {
        if (TextUtils.isEmpty(task.getUrl())) {
            throw new IllegalArgumentException("task's url cannot be empty");
        }
        if (taskOperators.containsKey(task)) {
            return;
        }
        DownloadOperator operator = new DownloadOperator(this, task);
        taskOperators.put(task, operator);
        if (listener != null) {
            taskListeners.put(task.getUrl(), new WeakReference<>(listener));
        }

        task.setStatus(DownloadTask.STATUS_PENDDING);
        DownloadTask historyTask = provider.findDownloadTaskByUrl(task.getUrl());
        if (historyTask == null) {
            task.setTaskId(config.getCreator().createId(task));
            provider.saveDownloadTask(task);
        } else {
            provider.updateDownloadTask(task);
        }

        //		new Thread(operator).start();

        pool.submit(operator);
    }

    public DownloadListener getDownloadListenerForTask(DownloadTask task) {
        if (task == null) {
            return null;
        }

        WeakReference<DownloadListener> listener = taskListeners.get(task.getUrl());
        if (listener != null) {
            return listener.get();
        }
        return null;
    }

    public void updateDownloadTaskListener(DownloadTask task, DownloadListener listener) {
        if (task == null || !taskOperators.containsKey(task)) {
            return;
        }

        taskListeners.put(task.getUrl(), new WeakReference<DownloadListener>(listener));
    }

    public void removeDownloadTaskListener(DownloadTask task) {
        if (task == null || !taskListeners.containsKey(task.getUrl())) {
            return;
        }

        taskListeners.remove(task.getUrl());
    }

    public void pauseDownload(DownloadTask task) {
        DownloadOperator operator = taskOperators.get(task);
        if (operator != null) {
            operator.pauseDownload();
        }
    }

    public boolean resumeDownload(DownloadTask task) {
        DownloadOperator operator = taskOperators.get(task);
        if (operator != null) {
            operator.resumeDownload();
            return true;
        } else {
            addDownloadTask(task);
            return false;
        }
    }

    public void cancelDownload(DownloadTask task) {
        if (task.getStatus() != DownloadTask.STATUS_FINISHED) {
            DownloadOperator operator = taskOperators.get(task);
            if (operator != null) {
                operator.cancelDownload();
            } else {
                task.setStatus(DownloadTask.STATUS_CANCELED);
                provider.updateDownloadTask(task);
            }
        }
    }

    public DownloadTask findDownloadTaskByUrl(String url) {

        Iterator<DownloadTask> iterator = taskOperators.keySet().iterator();

        while (iterator.hasNext()) {
            DownloadTask task = iterator.next();
            if (task.getUrl().equals(url)) {
                return task;
            }
        }

        DownloadTask downloadTask = provider.findDownloadTaskByUrl(url);
        if (downloadTask != null) {
            if (downloadTask.getDownloadSavePath() == null || !new File(downloadTask.getDownloadSavePath()).exists()) {
                provider.deleteOldData(url);
                return null;
            }
        }
        return downloadTask;
    }

    public List<DownloadTask> getAllDownloadTask() {
        List<DownloadTask> downloadTasks = provider.getAllDownloadTask();
        Iterator<DownloadTask> iterator = downloadTasks.iterator();
        while (iterator.hasNext()) {
            DownloadTask task = iterator.next();
            if (task != null) {
                if (!new File(task.getDownloadSavePath()).exists()) {
                    provider.deleteOldData(task.getTaskId());
                    iterator.remove();
                }
            }
        }
        return downloadTasks;
    }

    public void registerDownloadObserver(DownloadObserver observer) {
        if (observer == null) {
            return;
        }
        taskObservers.add(observer);
    }

    public void unregisterDownloadObserver(DownloadObserver observer) {
        if (observer == null) {
            return;
        }
        taskObservers.remove(observer);
    }

    public void close() {
        pool.shutdownNow();
    }

    public void notifyDownloadTaskStatusChanged(final DownloadTask task) {
        handler.post(new Runnable() {

            public void run() {
                for (DownloadObserver observer : taskObservers) {
                    observer.onDownloadTaskStatusChanged(task);
                }
            }
        });
    }

    void updateDownloadTask(final DownloadTask task, final long finishedSize, final long trafficSpeed) {
        task.setStatus(DownloadTask.STATUS_RUNNING);

        handler.post(new Runnable() {

            @Override
            public void run() {
                WeakReference<DownloadListener> listenerRef = taskListeners.get(task.getUrl());
                DownloadListener listener = null;
                if (listenerRef != null) {
                    listener = listenerRef.get();
                }
                provider.updateDownloadTask(task);
                if (listener != null) {
                    listener.onDownloadUpdated(task, finishedSize, trafficSpeed);
                }
            }

        });
    }

    void onDownloadStarted(final DownloadTask task) {
        task.setStatus(DownloadTask.STATUS_RUNNING);
        handler.post(new Runnable() {

            @Override
            public void run() {
                provider.updateDownloadTask(task);
                WeakReference<DownloadListener> listenerRef = taskListeners.get(task.getUrl());
                DownloadListener listener = null;
                if (listenerRef != null) {
                    listener = listenerRef.get();
                }
                if (listener != null) {
                    listener.onDownloadStart(task);
                }
            }
        });
    }


    void onDownloadPaused(final DownloadTask task) {
        task.setStatus(DownloadTask.STATUS_PAUSED);
        handler.post(new Runnable() {

            @Override
            public void run() {
                provider.updateDownloadTask(task);
                WeakReference<DownloadListener> listenerRef = taskListeners.get(task.getUrl());
                DownloadListener listener = null;
                if (listenerRef != null) {
                    listener = listenerRef.get();
                }
                if (listener != null) {
                    listener.onDownloadPaused(task);
                }
            }
        });
    }

    void onDownloadResumed(final DownloadTask task) {
        task.setStatus(DownloadTask.STATUS_RUNNING);
        handler.post(new Runnable() {

            @Override
            public void run() {
                provider.updateDownloadTask(task);
                WeakReference<DownloadListener> listenerRef = taskListeners.get(task.getUrl());
                DownloadListener listener = null;
                if (listenerRef != null) {
                    listener = listenerRef.get();
                }
                if (listener != null) {
                    listener.onDownloadResumed(task);
                }
            }
        });
    }

    void onDownloadCanceled(final DownloadTask task) {
        task.setStatus(DownloadTask.STATUS_CANCELED);
        handler.post(new Runnable() {

            @Override
            public void run() {
                WeakReference<DownloadListener> listenerRef = taskListeners.get(task.getUrl());
                DownloadListener listener = null;
                if (listenerRef != null) {
                    listener = listenerRef.get();
                }
                provider.updateDownloadTask(task);
                if (listener != null) {
                    listener.onDownloadCanceled(task);
                }
                removeTask(task);
            }
        });
    }

    void onDownloadSuccessed(final DownloadTask task) {
        task.setStatus(DownloadTask.STATUS_FINISHED);
        handler.post(new Runnable() {

            @Override
            public void run() {
                provider.updateDownloadTask(task);
                WeakReference<DownloadListener> listenerRef = taskListeners.get(task.getUrl());
                DownloadListener listener = null;
                if (listenerRef != null) {
                    listener = listenerRef.get();
                }
                if (listener != null) {
                    listener.onDownloadSuccessed(task);
                }
                removeTask(task);
            }

        });
    }

    void onDownloadFailed(final DownloadTask task) {
        task.setStatus(DownloadTask.STATUS_ERROR);
        handler.post(new Runnable() {

            @Override
            public void run() {
                WeakReference<DownloadListener> listenerRef = taskListeners.get(task.getUrl());
                DownloadListener listener = null;
                if (listenerRef != null) {
                    listener = listenerRef.get();
                }
                provider.updateDownloadTask(task);
                if (listener != null) {
                    listener.onDownloadFailed(task);
                }
                removeTask(task);
            }
        });
    }

    void onDownloadRetry(final DownloadTask task) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                WeakReference<DownloadListener> listenerRef = taskListeners.get(task.getUrl());
                DownloadListener listener = null;
                if (listenerRef != null) {
                    listener = listenerRef.get();
                }
                if (listener != null) {
                    listener.onDownloadRetry(task);
                }
            }
        });
    }

    private void removeTask(DownloadTask task) {
        taskOperators.remove(task);
        taskListeners.remove(task.getUrl());
    }

}
