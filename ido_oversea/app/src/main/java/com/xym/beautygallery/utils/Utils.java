package com.xym.beautygallery.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import com.xym.beautygallery.base.AppConfigMgr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static ConnectivityManager sCM;
    private volatile static boolean channel_adv = false;

    /**
     * 格式化字节单位，如1024b=1KB，1024KB＝1M
     *
     * @param b
     * @return
     */
    public static String formatFlowNumWithUnit(long b) {
        if (b < 1024) {
            return b + "B";
        } else {
            b = b / 1024;
            if (b < 1024) {
                return b + "KB";
            } else {
                return (((float) Math.round((b / 1024.0) * 100)) / 100) + "M";
            }
        }
    }

    public static String formatDownCount(long b) {
        if (b < 10000) {
            return b + "次下载";
        } else {
            b = b / 10000;
            return b + "万次下载";
        }
    }

    /**
     * 判断当前网络是否可用
     *
     * @param ctx
     * @return
     */
    public static boolean isNetworkAvaialble(Context ctx) {
        ConnectivityManager connMgr = getConnectivityManager(ctx);
        if (connMgr == null) {
            return false;
        }
        NetworkInfo network = connMgr.getActiveNetworkInfo();
        return network != null && network.isConnected();
    }

    public static boolean isNetworkMobile(Context ctx) {
        ConnectivityManager cm = getConnectivityManager(ctx);
        if (cm == null) {
            return false;
        }
        NetworkInfo networkInfo = null;
        try {
            networkInfo = cm.getActiveNetworkInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (networkInfo != null) {
            return networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        }
        return false;
    }

    private static ConnectivityManager getConnectivityManager(Context cxt) {
        if (sCM == null) {
            sCM = (ConnectivityManager) cxt.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
        }
        return sCM;
    }

    /**
     * 通过系统默认浏览器打开一个网页
     */
    public static void startWebViewWithSystemBrower(Context ctx, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        ctx.startActivity(intent);
    }

    /**
     * 通过指定浏览器打开网页
     *
     * @param ctx
     * @param url
     * @param pkg 指定浏览器包名
     * @param ac
     */
    public static void startWebPageWithThirdBrower(Context ctx, String url, String pkg, String ac) {
        if (TextUtils.isEmpty(pkg) || TextUtils.isEmpty(ac)) {
            startWebViewWithSystemBrower(ctx, url);
            return;
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(pkg, ac));
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        ctx.startActivity(intent);
    }

    /**
     * dp转到px
     *
     * @param cxt
     * @param dp
     * @return
     */
    public static float dp2px(Context cxt, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                cxt.getResources().getDisplayMetrics());
    }

    /**
     * 格式化时间
     *
     * @param time
     * @return
     */
    public static String formatTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(time));
    }

    public static String formatTimeNoTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(time));
    }

    /**
     * 打开网络设置
     *
     * @param ctx
     */
    public static void openSettingNetwork(Context ctx) {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        ctx.startActivity(intent);
    }

    /**
     * 从assets下读取资源
     *
     * @param ctx
     * @param assetId 资源id
     * @return 资源内容
     */
    public static String getLocalData(Context ctx, String assetId) {
        if (TextUtils.isEmpty(assetId)) return null;
        InputStream is = null;
        try {
            is = ctx.getAssets().open(assetId);
        } catch (IOException e) {
            return null;
        }
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[1024];
        try {
            for (int n; (n = is.read(b)) != -1; ) {
                out.append(new String(b, 0, n));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    /**
     * 从Assets下解压资源
     *
     * @param ctx
     * @param name     资源名
     * @param savePath 存储路径
     * @return
     */
    public static boolean extraFromAssets(Context ctx, String name, File savePath) {
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            is = ctx.getAssets().open(name);
            fos = new FileOutputStream(savePath);
            byte[] buffer = new byte[4096];
            int length = 0;
            while ((length = is.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
            }
            fos.flush();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static List<String> readLineFromString(String str) {
        List<String> result = new ArrayList<String>();
        BufferedReader br = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(str.getBytes());
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 按行从文件读取内容
     *
     * @param file
     * @return
     */
    public static List<String> readLineFromFile(File file) {
        List<String> result = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 按行String到文件
     *
     * @param file
     * @param list
     */
    public static void writeLineToFile(File file, List<String> list) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < list.size(); i++) {
                bw.write(list.get(i) + "\n");
            }
            bw.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取渠道号
     *
     * @param ctx
     * @return
     */
    public static String getChannelId(Context ctx) {
        ApplicationInfo appInfo;
        try {
            appInfo = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(),
                    PackageManager.GET_META_DATA);
            return appInfo.metaData.getString("TD_CHANNEL_ID");
        } catch (PackageManager.NameNotFoundException e) {

        }
        return "default";
    }

    /**
     * 是不是小米MIUI ROM
     *
     * @return
     */
    public static boolean isMIUI() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean hasLauncherEntry(Context cxt, String pkgName) {
        PackageManager pm = cxt.getPackageManager();
        if (pm == null) {
            return false;
        }
        return pm.getLaunchIntentForPackage(pkgName) != null;
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {

                return appProcess.processName;
            }
        }
        return null;
    }

    public static boolean isMainProcess(Context context) {
        String currentProcessName = getCurProcessName(context);
        return context.getPackageName().equals(currentProcessName);
    }

    public static void killSelfProcess(Context context, String proceeName) {
        List<ActivityManager.RunningAppProcessInfo> runningApps = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();

        if (runningApps == null) {
            return;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.processName.equals(proceeName)) {
                android.os.Process.killProcess(procInfo.pid);
                break;
            }

        }
    }

    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

    public static boolean isOverSea(Context context) {
        String pkgName = context.getPackageName();
        if (pkgName.equals("com.iwansy.gw.gameassistant")) {
            return true;
        }
        return false;
    }

    public static void setAdSwitch(boolean adSwitch) {
        channel_adv = adSwitch;
    }

    public static boolean getAdSwitch(Context context) {
        return channel_adv;
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static float getScreenDensity(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        return dm.density;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean isStatAccessPermissionSet(Context c) {
        AppOpsManager appOps = (AppOpsManager) c.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), c.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    //check if new version
    public static boolean isNeedAccessPermission(Context c) {
        boolean ret = false;
        int versionCode = 0;
        try {
            versionCode = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionCode = AppConfigMgr.getAccessPermission(c);
        } finally {
            if (AppConfigMgr.getAccessPermission(c) == 0) {
                ret = true;
            } else if (AppConfigMgr.getAccessPermission(c) != versionCode) {
                ret = true;
            }
            if (ret) {
                AppConfigMgr.setAccessPermission(c, versionCode);
                AppConfigMgr.setNeedRate(c, true);
                AppConfigMgr.setCountTimes(c, 1);
            }
            return ret;
        }
    }

    public static String trimAppName(String str) {
        int length = str.length();
        int index = 0;
        while (index < length && (str.charAt(index) <= '\u0020' || str.charAt(index) == '\u00a0'))
            index++;
        if (index > 0)
            return str.substring(index);
        return str;
    }
}
