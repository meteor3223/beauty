package com.xym.beautygallery.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;


import com.xym.beautygallery.base.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class PackageUtils {

    public static boolean isApkInstalled(Context ctx, String pkgName) {
        if (TextUtils.isEmpty(pkgName)) return false;
        try {
            PackageInfo pkgInfo = ctx.getPackageManager().getPackageInfo(pkgName, 0);
            if (pkgInfo != null) return true;
        } catch (NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public static boolean startupApp(Context context, String pkgName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
        }

        return false;
    }

    public static boolean isActivityAvailable(Context cxt, Intent intent) {
        List<ResolveInfo> list = cxt.getPackageManager().queryIntentActivities(intent, 0);
        return list != null && list.size() > 0;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void openInstaller(Context cxt, String filepath) {
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE, Uri.fromFile(new File(filepath)));// api
// level 14
        if (!isActivityAvailable(cxt, intent)) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(filepath)),
                    "application/vnd.android.package-archive");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        cxt.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void openUninstaller(Context context, String pkgName, boolean newTask) {
        Uri pkgUri = Uri.parse("package:" + pkgName);
        Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, pkgUri);// api level 14
        if (!isActivityAvailable(context, uninstallIntent)) {
            uninstallIntent.setAction(Intent.ACTION_DELETE);// api level 1
        }
        if (newTask) {
            uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (isActivityAvailable(context, uninstallIntent)) {
            context.startActivity(uninstallIntent);
        }
    }

    public static String getPkgLuancherActivity(Context ctx, String pkgName) {
        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(pkgName);
        if (intent == null) return null;
        return intent.getComponent().getClassName();
    }

    public static String loadApkFile(Context ctx, int id, String name) {
        try {
            InputStream is = ctx.getResources().openRawResource(id);
            File apkFile = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File parentFile = new File(Constants.DOWNLOAD_PTH);
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                apkFile = new File(parentFile, name);
            } else {
                apkFile = new File(ctx.getFilesDir(), name);
            }
            FileOutputStream fos = new FileOutputStream(apkFile);
            byte[] buffer = new byte[4096];
            int i = 0;
            while ((i = is.read(buffer)) != -1) {
                fos.write(buffer, 0, i);
            }
            fos.flush();
            fos.close();
            is.close();
            return apkFile.toString();
        } catch (IOException e) {
            return null;
        }
    }
}
