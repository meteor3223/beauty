package com.xym.beautygallery.appinfo;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.SparseArray;

import com.xym.beautygallery.base.AppConfigMgr;
import com.xym.beautygallery.base.FeatureConfig;
import com.xym.beautygallery.base.stats.StatsReportConstants;
import com.xym.beautygallery.base.stats.StatsWrapper;
import com.xym.beautygallery.module.DataManager;
import com.xym.beautygallery.utils.Logger;
import com.xym.beautygallery.utils.PackageUtils;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 获取已安装App的信息
 *
 * @see AppInfoSnapshot
 */
public final class AppManager {
    private static final String TAG = "AppManager";
    private static final boolean DEBUG = FeatureConfig.DEBUG;
    private volatile static AppManager sInstance;
    private Context mContext;
    private PackageManager mPm;
    private String mMyselfPkgName;
    private Drawable mDefaultIcon; // load the icon when needed
    private String mLocale;
    private AtomicBoolean mReceiverRegistered;
    private HashMap<String, AppInfoSnapshot> mAllApps;
    // <UID, package names> mappings
    private SparseArray<HashSet<String>> mUidToPkgNamesMappings;
    private HashMap<String, Integer> mPkgNameToUidMappings;
    /**
     * If one package of the uid is system app, then we take the uid as system uid app
     */
    private HashSet<Integer> mSystemAppsUids;
    private ArrayList<ListenerInfo> mListeners;
    private IAppsInfoCacheDatabase mCacheDb;
    private HandlerThread mWorkThread;
    private Handler mHandler;

    private ArrayList<AppRecommend> mAppRecommendList;
    private AppRecommend mCurrentAppRecommend = null;
    private long mzituAlbumCountConfig = 0;
    private boolean mWaitApkInstall = false;
    private AppsFilter appsFiler;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            processSystemBroadcasts(context, intent);
        }
    };

    private AppManager(Context appContext) {
        mContext = appContext.getApplicationContext();
        mPm = mContext.getPackageManager();
        mMyselfPkgName = mContext.getPackageName();
        mReceiverRegistered = new AtomicBoolean(false);

        mAllApps = new HashMap<String, AppInfoSnapshot>();
        mUidToPkgNamesMappings = new SparseArray<HashSet<String>>();
        mPkgNameToUidMappings = new HashMap<String, Integer>();
        mSystemAppsUids = new HashSet<Integer>();

        mListeners = new ArrayList<ListenerInfo>();

        // Use a worker thread to notify all the listeners. So that all the "notify" jobs
        // will be executed in same thread and executed in a serial manner.
        mWorkThread = new HandlerThread("AppManagerWorker");
        mWorkThread.start();
        mHandler = new Handler(mWorkThread.getLooper());
        mAppRecommendList = new ArrayList<AppRecommend>();
        mzituAlbumCountConfig = 0;
        appsFiler = new AppsFilter();
    }

    /**
     * get the AppManager instance
     *
     * @return
     */
    public static AppManager getInstance(Context cxt) {
        if (sInstance == null) {
            synchronized (AppManager.class) {
                if (sInstance == null) {
                    sInstance = new AppManager(cxt.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    /**
     * Listen on system broadcasts about apps change events.</p>
     * This method must be called when the application starts up. For example,
     * you can invoke it in Application#onCreate().
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public void listenSystemBroadcasts() {
        if (!mReceiverRegistered.compareAndSet(false, true)) {
            // already registered
            return;
        }

        if (DEBUG) Logger.d(TAG, "listen system broadcasts");

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        // TODO: we should add bad_removal support??? maybe for few devices
        //filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        mContext.registerReceiver(mReceiver, filter);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter2.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter2.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter2.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter2.addDataScheme("package");
        mContext.registerReceiver(mReceiver, filter2);
    }

    private void processSystemBroadcasts(Context context, Intent intent) {
        String action = intent.getAction();
        if (DEBUG) Logger.d(TAG, "receive " + action);

        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            String packageName = intent.getDataString();
            AppRecommend tempApp = AppManager.getInstance(mContext).getmCurrentAppRecommend();
            if (tempApp != null && packageName.contains(tempApp.pkgName)
                    && this.ismWaitApkInstall() == true) {
                this.setmWaitApkInstall(false);
                AppConfigMgr.setAppAdLast(mContext, true);
                PackageUtils.startupApp(mContext, tempApp.pkgName);
                StatsWrapper.onEvent(mContext, StatsReportConstants.CUSTOM_APP_AD_EVENT_ID,
                        StatsReportConstants.CUSTOM_APP_AD_LABEL_START);

            }
        }

        if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action) ||
                Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
            onExternalAppsChanged(context, intent);
        } else {
            String pkgName = URI.create(intent.getDataString()).getSchemeSpecificPart();
            boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
            if (Intent.ACTION_PACKAGE_ADDED.equals(action) && !replacing) {
                onPackageAdded(pkgName, uid);
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action) && !replacing) {
                onPackageRemoved(pkgName, uid);
            } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                onPackageReplaced(pkgName, uid);
            } else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                if (DEBUG) {
                    String[] components = intent.getStringArrayExtra(
                            Intent.EXTRA_CHANGED_COMPONENT_NAME_LIST);
                    Logger.d(TAG, "changed components: " + Arrays.toString(components));
                }
                onPackageStateChanged(pkgName, uid);
            }
        }
    }

    public Drawable getDefaultAppIcon() {
        // No need to add lock here. It's OK to create more than one objects in rarely cases.
        if (mDefaultIcon == null) {
            mDefaultIcon = mContext.getResources().getDrawable(
                    android.R.drawable.sym_def_app_icon);
        }
        return mDefaultIcon;
    }

    /**
     * Set the default app icon.
     *
     * @param resId
     * @return Return the previous default app icon
     */
    public Drawable setDefaultAppIcon(int resId) {
        Drawable oldDefIcon = mDefaultIcon;
        mDefaultIcon = mContext.getResources().getDrawable(resId);
        return oldDefIcon;
    }

    private void initAppsLocked() {
        if (mAllApps.size() == 0) {
            if (DEBUG) Logger.d(TAG, "init apps list");
            if (mLocale == null) {
                mLocale = mContext.getResources().getConfiguration().locale.toString();
            }
            List<PackageInfo> installedApps = mPm.getInstalledPackages(0);
            for (PackageInfo pkgInfo : installedApps) {
                AppInfoSnapshot appInfo = new AppInfoSnapshot(mContext, pkgInfo);
                mAllApps.put(pkgInfo.packageName, appInfo);
                addAppToMappingsLocked(pkgInfo.packageName, appInfo, true);
            }
            // The flag 'PackageManager.GET_UNINSTALLED_PACKAGES' may cause less information
            // about currently installed applications to be returned!
            // Such as, install time & update time, and so on.
            installedApps = mPm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            for (PackageInfo pkgInfo : installedApps) {
                if (!mAllApps.containsKey(pkgInfo.packageName)) {
                    // unmounted app
                    AppInfoSnapshot appInfo = new AppInfoSnapshot(mContext, pkgInfo);
                    mAllApps.put(pkgInfo.packageName, appInfo);
                    addAppToMappingsLocked(pkgInfo.packageName, appInfo, true);
                }
            }
        }
    }

    private void addAppToMappingsLocked(String pkgName, AppInfoSnapshot appInfo, boolean checkSystem) {
        int uid = appInfo.getUid();
        if (uid != -1) {
            HashSet<String> pkgNames = mUidToPkgNamesMappings.get(uid);
            if (pkgNames == null) {
                pkgNames = new HashSet<String>();
                mUidToPkgNamesMappings.append(uid, pkgNames);
            }
            pkgNames.add(pkgName);
            mPkgNameToUidMappings.put(pkgName, uid);
            if (checkSystem && appInfo.isSystemApp()) {
                mSystemAppsUids.add(uid);
            }
        }
    }

    private void removeAppFromMappingsLocked(String pkgName, int uid) {
        HashSet<String> pkgNamesSet = mUidToPkgNamesMappings.get(uid);
        if (pkgNamesSet != null) {
            pkgNamesSet.remove(pkgName);
            // don't clear the "pkgName/uid" mapping
        } else {
            // should never go here
            if (DEBUG) Logger.w(TAG, "uid not found when remove: " + uid + ", pkg: " + pkgName);
        }
    }

    /**
     * @return -1 will be returned if the app not found or the UID not available
     */
    public int getUidFromPkgName(String pkgName) {
        synchronized (mAllApps) {
            initAppsLocked();
            Integer uid = mPkgNameToUidMappings.get(pkgName);
            if (uid == null) return -1;
            return uid;
        }
    }

    /**
     * @return null will be returned if no installed package with the specified UID
     */
    public String[] getPkgNamesFromUid(int uid) {
        String[] pkgNamesList = null;
        synchronized (mAllApps) {
            initAppsLocked();
            HashSet<String> pkgNamesSet = mUidToPkgNamesMappings.get(uid);
            if (pkgNamesSet != null && pkgNamesSet.size() > 0) {
                pkgNamesList = pkgNamesSet.toArray(new String[pkgNamesSet.size()]);
            }
        }
        return pkgNamesList;
    }

    /**
     * Check if the UID is a system app UID</p>
     * If one package of the UID is a system app, then we take the UID as a system app UID.</p>
     *
     * @param uid
     * @return
     */
    public boolean isSystemAppUid(int uid) {
        if (uid < Process.FIRST_APPLICATION_UID) return true;
        synchronized (mAllApps) {
            initAppsLocked();
            return mSystemAppsUids.contains(uid);
        }
    }

    /**
     * Check if the app is a exist app </p>
     *
     * @param appName
     * @return
     */
    public boolean isExistApp(String appName) {
        synchronized (mAllApps) {
            initAppsLocked();
            for (AppInfoSnapshot appInfo : mAllApps.values()) {
                if (appInfo.mounted && (!appInfo.isSystemApp()) && (!appInfo.isUpdatedSystemApp())) {
                    if (appInfo.getLabel().equals(appName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get the applications list</p>
     *
     * @param filter The apps filter config
     * @return if no apps, an empty list will be returned
     */
    public ArrayList<AppInfoSnapshot> getApps(AppsFilter filter) {
        ArrayList<AppInfoSnapshot> results = new ArrayList<AppInfoSnapshot>();
        synchronized (mAllApps) {
            initAppsLocked();
            for (AppInfoSnapshot appInfo : mAllApps.values()) {
                if (filter.onlyMounted && !appInfo.mounted) {
                    continue;
                }
                if (filter.onlyEnabled && !appInfo.isEnabled()) {
                    continue;
                }
                if (!filter.includeSysApp && appInfo.isSystemApp()) {
                    if (!filter.includeUpdatedSysApp) {
                        continue; // don't keep any system app and it's system app
                    } else if (!appInfo.isUpdatedSystemApp()) {
                        continue; // only keep updated system app and it's not updated system app
                    }
                }
                if (!filter.includeMyself && appInfo.pkgName.equals(mMyselfPkgName)) {
                    continue;
                }
                results.add(appInfo);
            }
        }
        return results;
    }

    /**
     * Get the applications list</p>
     *
     * @param onlyMounted
     * @param onlyEnabled
     * @return if no apps, an empty list will be returned
     */
    public ArrayList<AppInfoSnapshot> getApps(boolean onlyMounted, boolean onlyEnabled) {
        ArrayList<AppInfoSnapshot> results = new ArrayList<AppInfoSnapshot>();
        synchronized (mAllApps) {
            initAppsLocked();
            for (AppInfoSnapshot appInfo : mAllApps.values()) {
                if (onlyMounted && !appInfo.mounted) {
                    continue;
                }
                if (onlyEnabled && !appInfo.isEnabled()) {
                    continue;
                }
                results.add(appInfo);
            }
        }
        return results;
    }

    public void loadAppData() {
        long startTime = System.currentTimeMillis();
        List<AppInfoSnapshot> appInfoSnapshots = this.getApps(true, true);
        if (appInfoSnapshots != null) {
            for (AppInfoSnapshot snap : appInfoSnapshots) {
                AppInfoSnapshot tempApp = this.getAppInfo(snap.getPackageName());
                tempApp.getLabel();
            }
        }
        appInfoSnapshots.clear();
        if (DEBUG) {
            Logger.d(TAG, " scanTime = " + (System.currentTimeMillis() - startTime));
        }
        DataManager.getInstance(mContext).requestAppRecommendDataFromServer();
    }

    /**
     * Get the applications list, including unmounted (uninstalled) and disabled apps.</p>
     * Same to {@link #getApps(boolean, boolean)} with arguments (false, false).</p>
     *
     * @return
     * @see #getApps(boolean, boolean)
     */
    public ArrayList<AppInfoSnapshot> getAllApps() {
        return getApps(false, false);
    }

    /**
     * Get the mounted (installed) applications list.</p>
     * Same to {@link #getApps(boolean, boolean)} with arguments (true, onlyEnabled).</p>
     *
     * @param onlyEnabled
     * @return
     * @see #getApps(boolean, boolean)
     */
    public ArrayList<AppInfoSnapshot> getMountedApps(boolean onlyEnabled) {
        return getApps(true, onlyEnabled);
    }

    /**
     * Get the mounted (installed) applications list, including disabled apps.</p>
     * Same to {@link #getApps(boolean, boolean)} with arguments (true, false).</p>
     *
     * @return
     * @see #getMountedApps(boolean)
     */
    public ArrayList<AppInfoSnapshot> getMountedApps() {
        return getMountedApps(false);
    }

    /**
     * Get the application info snapshot.
     *
     * @param pkgName
     * @param onlyMounted if true, only check mounted apps
     * @param onlyEnabled if true, only check enabled apps
     * @return null if the app not found
     */
    public AppInfoSnapshot getAppInfo(String pkgName, boolean onlyMounted,
                                      boolean onlyEnabled) {
        synchronized (mAllApps) {
            initAppsLocked();
            AppInfoSnapshot appInfo = mAllApps.get(pkgName);
            if (appInfo != null) {
                if (onlyMounted && !appInfo.mounted) {
                    return null;
                } else if (onlyEnabled && !appInfo.isEnabled()) {
                    return null;
                }
                return appInfo;
            }
            return null;
        }
    }

    /**
     * Get the application info snapshot, include unmounted (uninstalled) or disabled app.</p>
     * Same to {@link #getAppInfo(String, boolean, boolean)} with arguments (pkgName, false, false).</p>
     *
     * @param pkgName
     * @return null if the app not found
     * @see #getAppInfo(String, boolean, boolean)
     */
    public AppInfoSnapshot getAppInfo(String pkgName) {
        return getAppInfo(pkgName, false, false);
    }

    /**
     * Get the application info snapshot, exclude unmounted (uninstalled) app.</p>
     * Same to {@link #getAppInfo(String, boolean, boolean)} with arguments (pkgName, true, onlyEnabled).</p>
     *
     * @param pkgName
     * @param onlyEnabled
     * @return null if the app not found
     */
    public AppInfoSnapshot getMountedAppInfo(String pkgName, boolean onlyEnabled) {
        return getAppInfo(pkgName, true, onlyEnabled);
    }

    /**
     * Get the application info snapshot, include disabled app but exclude unmounted (uninstalled) app.</p>
     * Same to {@link #getAppInfo(String, boolean, boolean)} with arguments (pkgName, true, false).</p>
     *
     * @param pkgName
     * @return null if the app not found
     * @see #getAppInfo(String, boolean, boolean)
     */
    public AppInfoSnapshot getMountedAppInfo(String pkgName) {
        return getAppInfo(pkgName, true, false);
    }

    /**
     * Get the application info snapshot.</p>
     * Same to {@link #getAppInfo(String, boolean, boolean)}, except that this method
     * use exception to report app not found.</p>
     *
     * @param pkgName
     * @param onlyMounted
     * @param onlyEnabled
     * @return never be null
     * @throws NameNotFoundException
     * @see #getAppInfo(String, boolean, boolean)
     */
    public AppInfoSnapshot getAppInfoWithThrow(String pkgName, boolean onlyMounted,
                                               boolean onlyEnabled) throws NameNotFoundException {
        AppInfoSnapshot appInfo = getAppInfo(pkgName, onlyMounted, onlyEnabled);
        if (appInfo == null) throw new NameNotFoundException(pkgName + " not found");
        return appInfo;
    }

    /**
     * Get the application info snapshot, include unmounted (uninstalled) or disabled app.</p>
     * Same to {@link #getAppInfoWithThrow(String, boolean, boolean)}
     * with arguments (pkgName, false, false)</p>
     *
     * @param pkgName
     * @return never be null
     * @throws NameNotFoundException
     * @see {@link #getAppInfoWithThrow(String, boolean, boolean)}
     */
    public AppInfoSnapshot getAppInfoWithThrow(String pkgName) throws NameNotFoundException {
        return getAppInfoWithThrow(pkgName, false, false);
    }

    /**
     * Get the application info snapshot, include disabled app but exclude unmounted (uninstalled) app.</p>
     * Same to {@link #getAppInfoWithThrow(String, boolean, boolean)}
     * with arguments (pkgName, true, false)</p>
     *
     * @param pkgName
     * @return never be null
     * @throws NameNotFoundException
     * @see {@link #getAppInfoWithThrow(String, boolean, boolean)}
     */
    public AppInfoSnapshot getMountedAppInfoWithThrow(String pkgName) throws NameNotFoundException {
        return getAppInfoWithThrow(pkgName, true, false);
    }

    /**
     * You can use {@link #getAppInfo(String, boolean, boolean)} instead</p>
     * Similar to #getAppInfo(pkgName, false, false)</p>
     *
     * @deprecated Will be removed later
     */
    public AppInfoSnapshot getAppInfoNoFail(String pkgName) {
        return getAppInfoNoFail(pkgName, false);
    }

    /**
     * You can use {@link #getAppInfo(String, boolean, boolean)} instead.</p>
     * Similar to #getAppInfo(pkgName, onlyMounted, false)</p>
     *
     * @deprecated Will be removed later
     */
    public AppInfoSnapshot getAppInfoNoFail(String pkgName, boolean onlyMounted) {
        AppInfoSnapshot info = getAppInfo(pkgName, onlyMounted, false);
        if (info == null) info = AppInfoSnapshot.createInvalidAppInfo(mContext, pkgName);
        return info;
    }

    public void registerListener(ChangedListener listener) {
        if (listener == null) {
            Logger.w(TAG, "null listener not allowed");
            if (DEBUG) Thread.dumpStack();
            return;
        }
        synchronized (mListeners) {
            for (ListenerInfo l : mListeners) {
                if (l.holder.get() == listener) return;
            }
            mListeners.add(new ListenerInfo(listener));
        }
    }

    public void unregisterListener(ChangedListener listener) {
        if (listener == null) {
            Logger.w(TAG, "null listener not allowed");
            if (DEBUG) Thread.dumpStack();
            return;
        }
        synchronized (mListeners) {
            final int N = mListeners.size();
            for (int i = 0; i < N; i++) {
                ListenerInfo listenerInfo = mListeners.get(i);
                if (listenerInfo.holder.get() == listener) {
                    // this object may be held by #doNotifyChanged()
                    listenerInfo.unregistered = true;
                    mListeners.remove(i);
                    return;
                }
            }
        }
    }

    private void notifyChanged(final ChangedInfo info) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                doNotifyChanged(info);
            }
        });
    }

    private void doNotifyChanged(ChangedInfo info) {
        if (DEBUG) Logger.d(TAG, "notifyChanged: " + info);
        ArrayList<ListenerInfo> curListeners = new ArrayList<ListenerInfo>();
        synchronized (mListeners) {
            for (int i = 0; i < mListeners.size(); ) {
                ListenerInfo listenerInfo = mListeners.get(i);
                ChangedListener l = listenerInfo.holder.get();
                if (l == null) {
                    if (DEBUG) Logger.e(TAG, "listener leak found: " + listenerInfo.className);
                    mListeners.remove(i);
                } else {
                    if (DEBUG) Logger.d(TAG, "notify: " + listenerInfo.className);
                    curListeners.add(listenerInfo);
                    i++;
                }
            }
            if (DEBUG) Logger.d(TAG, "notify done, cur size: " + mListeners.size());
        }
        for (ListenerInfo listenerInfo : curListeners) {
            if (!listenerInfo.unregistered) {
                ChangedListener l = listenerInfo.holder.get();
                if (l != null) {
                    l.onChanged(info);
                }
            }
        }
    }

    /**
     * When locale changes, this method should be called to refresh the apps information
     * (such as application name).
     */
    public void onLocaleChanged(Locale newLocale) {
        if (DEBUG) Logger.d(TAG, "onLocaleChanged: " + newLocale);
        String loc = newLocale.toString();
        if (loc.equals(mLocale)) {
            return; // no change
        }
        mLocale = loc;

        // No need to initialize the apps list!
        synchronized (mAllApps) {
            for (AppInfoSnapshot info : mAllApps.values()) {
                info.onLocaleChanged();
            }
        }

        ChangedInfo info = new ChangedInfo();
        info.type = ChangedInfo.TYPE_LOCALE_CHANGED;
        notifyChanged(info);
    }

    /**
     * @return null may be returned
     */
    private AppInfoSnapshot fetchAppInfoFromSystem(String pkgName) {
        try {
            PackageInfo pkgInfo = mPm.getPackageInfo(pkgName, 0);
            return new AppInfoSnapshot(mContext, pkgInfo);
        } catch (NameNotFoundException e) {
            // The flag 'PackageManager.GET_UNINSTALLED_PACKAGES' may cause less information
            // about currently installed applications to be returned!
            // Such as, install time & update time, and so on.
            try {
                PackageInfo pkgInfo = mPm.getPackageInfo(pkgName,
                        PackageManager.GET_UNINSTALLED_PACKAGES);
                return new AppInfoSnapshot(mContext, pkgInfo);
            } catch (NameNotFoundException e1) {
                if (DEBUG) Logger.w(TAG, "failed to get package info from system: " + pkgName, e);
            }
        }
        return null;
    }

    private void onPackageAdded(String pkgName, int uid) {
        if (DEBUG) Logger.d(TAG, "onPackageAdded: " + pkgName);

        AppInfoSnapshot appInfo = fetchAppInfoFromSystem(pkgName);
        if (appInfo == null) {
            Logger.e(TAG, "Cannot get package info when added: " + pkgName);
            return;
        }

        synchronized (mAllApps) {
            initAppsLocked();
            mAllApps.put(pkgName, appInfo);
            addAppToMappingsLocked(pkgName, appInfo, false);
        }

        notifyChanged(AppChangedInfo.create(ChangedInfo.TYPE_ADDED, pkgName, uid));
    }

    private void onPackageRemoved(String pkgName, int uid) {
        if (DEBUG) Logger.d(TAG, "onPackageRemoved: " + pkgName);

        synchronized (mAllApps) {
            initAppsLocked();
            mAllApps.remove(pkgName);
            removeAppFromMappingsLocked(pkgName, uid);
            if (mCacheDb != null) {
                mCacheDb.onPackageRemoved(pkgName);
            }
        }

        notifyChanged(AppChangedInfo.create(ChangedInfo.TYPE_REMOVED, pkgName, uid));
    }

    private void onPackageReplaced(String pkgName, int uid) {
        if (DEBUG) Logger.d(TAG, "onPackageReplaced: " + pkgName);

        AppInfoSnapshot appInfo = fetchAppInfoFromSystem(pkgName);
        if (appInfo == null) {
            Logger.e(TAG, "Cannot get package info when replaced: " + pkgName);
            return;
        }

        synchronized (mAllApps) {
            initAppsLocked();
            mAllApps.put(pkgName, appInfo);
            if (mCacheDb != null) {
                mCacheDb.clearApkMd5(pkgName);
                mCacheDb.clearSignatureMd5(pkgName);
            }
        }

        notifyChanged(AppChangedInfo.create(ChangedInfo.TYPE_REPLACED, pkgName, uid));
    }

    private void onPackageStateChanged(String pkgName, int uid) {
        if (DEBUG) Logger.d(TAG, "onPackageStateChanged: " + pkgName);

        AppInfoSnapshot appInfo = null;
        synchronized (mAllApps) {
            appInfo = mAllApps.get(pkgName);
        }
        if (appInfo == null) {
            Logger.e(TAG, "Cannot get package info when changed: " + pkgName);
            return;
        }

        appInfo.refreshState();

        notifyChanged(AppChangedInfo.create(ChangedInfo.TYPE_STATE_CHANGED, pkgName, uid));
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    private void onExternalAppsChanged(Context cxt, Intent intent) {
        String[] pkgNames = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
        int[] uids = intent.getIntArrayExtra(Intent.EXTRA_CHANGED_UID_LIST);
        if (pkgNames == null || pkgNames.length == 0 || uids == null || uids.length == 0) {
            Logger.w(TAG, "external apps changed, but no apps: " + Arrays.toString(pkgNames)
                    + ", uids: " + Arrays.toString(uids));
            return;
        }

        boolean available = Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(intent.getAction());
        synchronized (mAllApps) {
            initAppsLocked();
            for (String pkg : pkgNames) {
                if (available) {
                    // recreate the object to refresh the app information
                    // (label, icon, install time, update time, and so on)
                    AppInfoSnapshot appInfo = fetchAppInfoFromSystem(pkg);
                    if (appInfo != null) {
                        mAllApps.put(pkg, appInfo);
                        addAppToMappingsLocked(pkg, appInfo, false);
                        if (DEBUG && !appInfo.mounted) {
                            Logger.e(TAG, pkg + " to available, but with unmounted: " + appInfo);
                        }
                    } else {
                        if (DEBUG) {
                            Logger.w(TAG, "failed to fetch package info when available: " + pkg);
                        }
                    }
                } else {
                    AppInfoSnapshot appInfo = mAllApps.get(pkg);
                    if (appInfo != null) {
                        appInfo.mounted = false;
                    } else {
                        if (DEBUG) {
                            Logger.w(TAG, "no package info found when unavaible: " + pkg);
                        }
                    }
                }
            }
        }

        ExternalAppsChangedInfo info = new ExternalAppsChangedInfo();
        info.type = ChangedInfo.TYPE_EXTERNAL_APPS_CHANGED;
        info.available = available;
        info.pkgNames = pkgNames;
        info.uids = uids;
        notifyChanged(info);
    }

    /**
     * Get cache DB of apps info
     *
     * @return null if no cache DB set
     */
    public IAppsInfoCacheDatabase getCacheDatabase() {
        return mCacheDb;
    }

    /**
     * Set cache DB of apps info
     *
     * @param cacheDb
     */
    public void setCacheDatabase(IAppsInfoCacheDatabase cacheDb) {
        mCacheDb = cacheDb;
    }

    /**
     * The listener used to listen on AppManager content change.
     */
    public interface ChangedListener {
        /**
         * When AppManager content changed, this callback will be invokded.</p>
         * Note: this method will be invoked on a worker thread (not UI thread)!</p>
         *
         * @param info The change info, an instance of {@link ChangedInfo},
         *             or {@link AppChangedInfo}, or {@link ExternalAppsChangedInfo}
         * @see ChangedInfo
         * @see AppChangedInfo
         * @see ExternalAppsChangedInfo
         */
        public void onChanged(ChangedInfo info);
    }

    public static class AppsFilter {
        /**
         * Only get mounted apps (true by default).
         */
        public boolean onlyMounted = true;
        /**
         * Only get enabled apps (false by default).
         */
        public boolean onlyEnabled = false;
        /**
         * Include updated system apps (true by default).
         * Note: this config will be ignored if {@link AppsFilter#includeSysApp} is true.
         */
        public boolean includeUpdatedSysApp = true;
        /**
         * Include all system apps (true by default).
         * Note: if this config is true, {@link #includeUpdatedSysApp} will be ignored;
         * otherwise, {@link #includeUpdatedSysApp} will be checked.
         */
        public boolean includeSysApp = true;
        /**
         * Include myself (false by default).
         */
        public boolean includeMyself = false;
    }

    /**
     * Basic class for AppManager change info.</p>
     * The following AppManager change events use this class:</br>
     * {@link #TYPE_LOCALE_CHANGED}</p>
     *
     * @see AppChangedInfo
     * @see ExternalAppsChangedInfo
     */
    public static class ChangedInfo {
        /**
         * Not a valid type
         */
        public static final int TYPE_INVALID = 0;
        /**
         * Locale changed
         *
         * @see ChangedInfo
         */
        public static final int TYPE_LOCALE_CHANGED = 1;
        /**
         * New app installed
         *
         * @see AppChangedInfo
         */
        public static final int TYPE_ADDED = 2;
        /**
         * App removed
         *
         * @see AppChangedInfo
         */
        public static final int TYPE_REMOVED = 3;
        /**
         * App replaced
         *
         * @see AppChangedInfo
         */
        public static final int TYPE_REPLACED = 4;
        /**
         * App components enabled or disabled
         *
         * @see AppChangedInfo
         */
        public static final int TYPE_STATE_CHANGED = 5;
        /**
         * Apps become available or unavailable because of external storage mounted or unmounted
         *
         * @see ExternalAppsChangedInfo
         */
        public static final int TYPE_EXTERNAL_APPS_CHANGED = 6;

        /**
         * The AppManager change type, will be one of following values:</br>
         * {@link #TYPE_LOCALE_CHANGED}</br>
         * {@link #TYPE_ADDED}</br>
         * {@link #TYPE_REMOVED}</br>
         * {@link #TYPE_REPLACED}</br>
         * {@link #TYPE_STATE_CHANGED}</br>
         * {@link #TYPE_EXTERNAL_APPS_CHANGED}
         */
        public int type;

        @Override
        public String toString() {
            return "type: " + type;
        }
    }

    /**
     * Change info for app change events (add, remove, replace or state change).</p>
     * The following AppManager change events use this class:</br>
     * {@link #TYPE_ADDED}</br>
     * {@link #TYPE_REMOVED}</br>
     * {@link #TYPE_REPLACED}</br>
     * {@link #TYPE_STATE_CHANGED}
     */
    public static class AppChangedInfo extends ChangedInfo {
        public String pkgName;
        public int uid;

        public static AppChangedInfo create(int type, String pkgName, int uid) {
            AppChangedInfo info = new AppChangedInfo();
            info.type = type;
            info.pkgName = pkgName;
            info.uid = uid;
            return info;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("type: ").append(type);
            sb.append(", pkgName: ").append(pkgName);
            sb.append(", uid: ").append(uid);
            return sb.toString();
        }
    }

    /**
     * Change info for external apps change event ({@link ChangedInfo#TYPE_EXTERNAL_APPS_CHANGED})
     */
    public static class ExternalAppsChangedInfo extends ChangedInfo {
        public boolean available;
        public String[] pkgNames;
        public int[] uids;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("type: ").append(type);
            sb.append(", available: ").append(available);
            sb.append(", pkgNames: ").append(Arrays.toString(pkgNames));
            sb.append(", uid: ").append(Arrays.toString(uids));
            return sb.toString();
        }
    }

    private static class ListenerInfo {
        String className;
        WeakReference<ChangedListener> holder;
        boolean unregistered = false;

        ListenerInfo(ChangedListener listener) {
            className = listener.getClass().getName();
            holder = new WeakReference<ChangedListener>(listener);
        }
    }

    public long getMzituAlbumCountConfig() {
        return mzituAlbumCountConfig;
    }

    public void setMzituAlbumCountConfig(long mzituAlbumCountConfig) {
        this.mzituAlbumCountConfig = mzituAlbumCountConfig;
    }

    public ArrayList<AppRecommend> getmAppRecommendList() {
        return mAppRecommendList;
    }

    public void addmAppRecommendList(AppRecommend addAppRecommend) {
        this.mAppRecommendList.add(addAppRecommend);
    }

    public AppRecommend getmCurrentAppRecommend() {
        return mCurrentAppRecommend;
    }

    private boolean checkAppExist(final List<AppInfoSnapshot> appInfoList, String appPkg) {
        for (int j = 0; j < appInfoList.size(); j++) {
            if (appInfoList.get(j).getPackageName().equals(appPkg)) {
                return true;
            }
        }
        return false;
    }

    public void findCurrentAppRecommend() {
        int N = mAppRecommendList.size();
        final List<AppInfoSnapshot> appInfoList = this.getApps(appsFiler);
        for (int i = 0; i < N; i++) {
            String pkgName = mAppRecommendList.get(i).pkgName;
            if (checkAppExist(appInfoList, pkgName) == false) {
                mCurrentAppRecommend = mAppRecommendList.get(i);
                return;
            }
        }
        mCurrentAppRecommend = null;
    }

    public boolean ismWaitApkInstall() {
        return mWaitApkInstall;
    }

    public void setmWaitApkInstall(boolean mWaitApkInstall) {
        this.mWaitApkInstall = mWaitApkInstall;
    }
}
