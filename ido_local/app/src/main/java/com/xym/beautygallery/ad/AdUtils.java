package com.xym.beautygallery.ad;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.xym.beautygallery.utils.PackageUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdUtils {

    public static final String TAOBAO_PKG = "com.taobao.taobao";

    public static String requestJson(String urlStr) {
        String result = null;
        BufferedReader rd = null;
        HttpURLConnection conn = null;
        try {
// Construct data
            StringBuffer data = new StringBuffer();
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            rd = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rd != null) {
                try {
                    rd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            conn.disconnect();
        }
        return result;
    }

    public static int convertPos(int pos, int gap) {
        if (gap == 0) return pos;
        return pos - (pos + 1) / gap;
    }

    public static boolean isAdItem(int pos, int gap) {
        if (gap == 0) return false;
        return (pos + 1) % gap == 0;
    }

    public static void startTaobao(Context ctx, String url) {
        Intent intent = new Intent();
        intent.setPackage(TAOBAO_PKG);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        if (PackageUtils.isActivityAvailable(ctx, intent)) {
            ctx.startActivity(intent);
        } else {
            startWebViewWithSystemBrower(ctx, url);
        }
    }

    public static void startWebViewWithSystemBrower(Context ctx, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        ctx.startActivity(intent);
    }
}
