package com.xym.beautygallery.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.xym.beautygallery.R;


/**
 * Created by zhouzhiyong on 15/11/15.
 */
public class ShareUtils {

    public static final String WX_PKG = "com.tencent.mm";

    public static void shareMsgWithChooser(Context context, String activityTitle, String msgTitle, String msgText) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, getShareString(context));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, activityTitle));
    }

    public static void shareToApp(Context context, String pkg, String msg) {
        if (PackageUtils.isApkInstalled(context, pkg)) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setPackage(pkg);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, getShareString(context));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, 100);
            } else {
                context.startActivity(intent);
            }
        } else {
            Toast.makeText(context, "该应用不存在，无法进行分享，请先安装!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void sendSMS(Context context, String smsBody) {
        Uri smsToUri = Uri.parse("smsto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", getShareString(context));
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, 100);
        } else {
            context.startActivity(intent);
        }
    }

    public static String getShareString(Context context) {
        return context.getString(R.string.beauty_share_text) + "https://play.google.com/store/apps/details?id=com.redphx.deviceid" + context.getString(R.string.beauty_share_from) + context.getString(R.string.app_name);
    }
}
