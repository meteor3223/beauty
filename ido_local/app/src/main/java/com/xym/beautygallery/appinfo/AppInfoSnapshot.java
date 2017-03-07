package com.xym.beautygallery.appinfo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;


import com.xym.beautygallery.base.FeatureConfig;
import com.xym.beautygallery.utils.Logger;
import com.xym.beautygallery.utils.Utils;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * This class is used to obtain application information snapshot.</p>
 * Don't hold this class object in your code directly;
 * just use it to obtain the info you need.</p>
 */
public class AppInfoSnapshot {
    private static final String TAG = "AppInfoSnapshot";
    private static final boolean DEBUG = FeatureConfig.DEBUG;
    private static final boolean DUMP_CACHE = false;

    private static final int STATE_ENABLED = 1;
    private static final int STATE_DISABLED = -1;
    private static final int STATE_UNKNOWN = 0;

    // Internal flag
    private static final int FLAG_FORWARD_LOCK = 1 << 29;

    /* package */ String pkgName;
    /* package */ int uid = -1;
    /* package */ String sharedUserId;

    /* package */ String versionName;
    /* package */ int versionCode;

    /**
     * sourceDir maybe null
     */
    /* package */ String sourceDir;
    /* package */ boolean mounted = false;
    /* package */ long installTime = 0;
    /* package */ long updateTime = 0;

    private Context mContext;
    private boolean mIsValid = true;

    private int mState = STATE_UNKNOWN;
    private int mFlags = 0;
    private boolean mMove2SdSupported = false;

    private String mLabel; // lazy init
    private String mPinYin;
//    private WeakReference<Drawable> mIcon; // lazy init and cache
    private Drawable mIcon; // lazy init and cache
    private WeakReference<Signature[]> mSignaturesHolder; // lazy init and cache
    private String mSigatureSha1; // lazy init
    private String mTapasApkMd5; // lazy init
    private String mSFMd5; //lazy init

    private AppInfoSnapshot(Context cxt, String pkg) {
        mContext = cxt;
        pkgName = pkg;
        mIsValid = false;
    }

    AppInfoSnapshot(Context cxt, PackageInfo info) {
        mContext = cxt;
        loadAppInfo(info);
    }

    /**
     * @deprecated will be removed later
     */
    static AppInfoSnapshot createInvalidAppInfo(Context cxt, String pkgName) {
        AppInfoSnapshot appInfo = new AppInfoSnapshot(cxt, pkgName);
        appInfo.mIsValid = false;
        return appInfo;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void loadAppInfo(PackageInfo info) {
        pkgName = info.packageName;
        uid = info.applicationInfo.uid; // may be -1 if the app unmounted
        sharedUserId = !TextUtils.isEmpty(info.sharedUserId) ? info.sharedUserId : null;

        versionName = info.versionName; // may be null if the app unmounted
        versionCode = info.versionCode; // may be 0 if the app unmounted

        sourceDir = info.applicationInfo.sourceDir;
        if (null == sourceDir) {
            //build a apk path for temp
            //when app move to sdcard, and remove sdcard then the apkpath may be null
            sourceDir = "/data/app/" + pkgName + "-fix.apk";
        }
        File apkFile = new File(sourceDir);
        mounted = apkFile.exists();
        if (Build.VERSION.SDK_INT < 9) {
            if (mounted) {
                installTime = apkFile.lastModified();
            } else {
                installTime = 0;
            }
            updateTime = installTime;
        } else {
            // may be 0 if the app unmounted or use 'PackageManager.GET_UNINSTALLED_PACKAGES' flag
            installTime = info.firstInstallTime;
            updateTime = info.lastUpdateTime;
        }
        refreshState();

        mFlags = info.applicationInfo.flags;
        //mMove2SdSupported = PackageUtils.moveToSDAdvice(info);
        //TODO
        mMove2SdSupported = false;
    }

    /* package */
    void refreshState() {
        int state = getApplicationEnabledSetting(pkgName);
        /**
         * we don't call info.applicationInfo.enabled directly here because in some system,
         * this value always return true.
         */
        mState = (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT ||
                state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) ?
                STATE_ENABLED : STATE_DISABLED;
    }

    private int getApplicationEnabledSetting(String pkgName) {
        try {
            return mContext.getPackageManager().getApplicationEnabledSetting(pkgName);
        } catch (IllegalArgumentException e) {
            // the app uninstalled!
            return PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        }
    }

    /* package */
    void onLocaleChanged() {
        mLabel = null; // load it when needed
    }

    /**
     * Package name of the app
     */
    public String getPackageName() {
        return pkgName;
    }

    /**
     * UID of the app
     *
     * @return -1 may be returned if the app is unmounted
     */
    public int getUid() {
        return uid;
    }

    /**
     * Shared UID of the app
     *
     * @return null may be returned if no shared UID or the app is unmounted
     */
    public String getSharedUserId() {
        return sharedUserId;
    }

    /**
     * Version name
     *
     * @return null may be returned if the app is unmounted
     */
    public String getVersionName() {
        return versionName;
    }

    /**
     * Version code
     *
     * @return 0 may be returned if the app is unmounted
     */
    public int getVersionCode() {
        return versionCode;
    }

    public String getApkFilePath() {
        return sourceDir;
    }

    /**
     * Check if the application is enabled or not
     *
     * @return true if enabled, otherwise false
     */
    public boolean isEnabled() {
        return mState == STATE_ENABLED;
    }

    /**
     * Check if the app is mounted (available)
     */
    public boolean isMounted() {
        return mounted;
    }

    /**
     * Install time
     *
     * @return 0 may be returned if the app is unmounted
     */
    public long getInstallTime() {
        return installTime;
    }

    /**
     * Update time
     *
     * @return 0 may be returned if the app is unmounted
     */
    public long getUpdateTime() {
        return updateTime;
    }

    /**
     * Get the application name.</p>
     * Note:</br>
     * It's better to call it in non-UI thread.</p>
     *
     * @return The application name or package name
     */
    public String getLabel() {
        if (!mIsValid) {
            return pkgName;
        }

        if (mLabel == null) {
            try {
                PackageManager pm = mContext.getPackageManager();
                PackageInfo info = pm.getPackageInfo(pkgName, 0);
                mLabel = Utils.trimAppName(info.applicationInfo.loadLabel(pm).toString());
            } catch (NameNotFoundException e) {
                if (DEBUG) Logger.w(TAG, "failed to load label: " + pkgName, e);
                return pkgName; // don't update 'mLabel'
            }
        }
        return mLabel;
    }

    /**
     * Get the application icon.</p>
     * Note:</br>
     * It's better to call it in non-UI thread.</p>
     *
     * @return The application icon or the system default application icon
     */
    public Drawable getIcon() {
        return getIcon(false, null);
    }

    /**
     * Get the application icon.</p>
     * Note:</br>
     * It's better to call it in non-UI thread.</p>
     *
     * @param defaultIcon The default icon to be returned if the app not installed
     * @return The application icon or the provided default icon
     */
    public Drawable getIcon(Drawable defaultIcon) {
        return getIcon(true, defaultIcon);
    }

    private Drawable getIcon(boolean useCustomizedDefIcon, Drawable defaultIcon) {
        if (!mIsValid) {
            return AppManager.getInstance(mContext).getDefaultAppIcon();
        }

//        Drawable icon = null;
//        if (mIcon != null) {
//            icon = mIcon.get();
//        }
//
//        if (icon == null) {
//            try {
//                PackageManager pm = mContext.getPackageManager();
//                PackageInfo info = pm.getPackageInfo(pkgName, 0);
//                icon = info.applicationInfo.loadIcon(pm);
//                mIcon = new WeakReference<Drawable>(icon);
//            } catch (NameNotFoundException e) {
//                if (DEBUG) Logger.w(TAG, "failed to load icon: " + pkgName, e);
//                // don't update 'mIcon'
//                if (useCustomizedDefIcon) {
//                    icon = defaultIcon;
//                } else {
//                    icon = AppManager.getInstance(mContext).getDefaultAppIcon();
//                }
//            }
//        }
//
//        return icon;
        if (mIcon == null) {
            try {
                PackageManager pm = mContext.getPackageManager();
                PackageInfo info = pm.getPackageInfo(pkgName, 0);
                mIcon = info.applicationInfo.loadIcon(pm);
            } catch (NameNotFoundException e) {
                if (DEBUG) Logger.w(TAG, "failed to load icon: " + pkgName, e);
                if (useCustomizedDefIcon) {
                    mIcon = defaultIcon;
                } else {
                    mIcon = AppManager.getInstance(mContext).getDefaultAppIcon();
                }
            }
        }
        return mIcon;
    }

    /**
     * @return true if the app is installed in the device's system image, otherwise false
     */
    public boolean isSystemApp() {
        return (mFlags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
    }

    /**
     * @return true if the app has been installed as an update to a build-in system app, otherwise false
     */
    public boolean isUpdatedSystemApp() {
        return (mFlags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) ==
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
    }

    /**
     * @return true if the app has flag "FLAG_FORWARD_LOCK"
     */
    public boolean isForwarLocked() {
        return (mFlags & FLAG_FORWARD_LOCK) == FLAG_FORWARD_LOCK;
    }

    /**
     * @return true if the app is installed on external storage, otherwise false
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public boolean isOnExternalStorage() {
        return (mFlags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) ==
                ApplicationInfo.FLAG_EXTERNAL_STORAGE;
    }

    /**
     * @return true if the app supports Move2Sd feature, otherwise false
     */
    public boolean supportMoveToExternalStorage() {
        return mMove2SdSupported;
    }

    /**
     * Get signatures of the application.</p>
     * Note:</br>
     * 1) Don't use this method when possible.</br>
     * 2) It's better to call it in non-UI thread.</p>
     *
     * @return null will be returned if the app not found
     */
    public Signature[] getSignatures(Context cxt) {
        if (mSignaturesHolder != null) {
            Signature[] signs = mSignaturesHolder.get();
            if (signs != null) {
                return signs;
            }
        }

        try {
            PackageInfo pkgInfo = cxt.getPackageManager().getPackageInfo(pkgName,
                    PackageManager.GET_SIGNATURES);
            if (pkgInfo.signatures != null && pkgInfo.signatures.length > 0) {
                mSignaturesHolder = new WeakReference<Signature[]>(pkgInfo.signatures);
            } else {
                if (DEBUG) Logger.w(TAG, "failed to get signatures: " + pkgName);
            }
            return pkgInfo.signatures;
        } catch (NameNotFoundException e) {
            if (DEBUG) Logger.w(TAG, "failed to get signature: " + pkgName, e);
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("pkgName: ").append(pkgName);
        sb.append(", uid: ").append(uid);
        sb.append(", sharedUserId: ").append(sharedUserId);
        sb.append(", verName: ").append(versionName);
        sb.append(", verCode: ").append(versionCode);
        sb.append(", apk: ").append(sourceDir);
        sb.append(", mounted: ").append(mounted);
        sb.append(", enabled: ").append(isEnabled());
        sb.append(", appName: ").append(mLabel);
        sb.append(", installTime: ").append(installTime);
        sb.append(", updateTime: ").append(updateTime);
        sb.append(", flags: ").append(Integer.toHexString(mFlags));
        return sb.toString();
    }

}
