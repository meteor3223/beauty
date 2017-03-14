package com.example.re.getui_sdk.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.igexin.sdk.PushConsts;

import org.json.JSONException;
import org.json.JSONObject;

public class PushReceiver extends BroadcastReceiver {
    /**
     * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息(此时 GetuiSdkDemoActivity.tLogView ==
     * null)
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA:
                // 获取透传数据
                final byte[] payload = bundle.getByteArray("payload");
                // smartPush第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
                if (payload != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String data = new String(payload);
                            PushBean pushBean = new PushBean();
                            try {
                                JSONObject jsonObj = new JSONObject(data);
                                pushBean.setP(jsonObj.getString("P"));
                                pushBean.setB(jsonObj.getString("B"));
                                pushBean.setU(jsonObj.getString("U"));
                                pushBean.setW(jsonObj.getString("W"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (pushBean != null) {
                                try {
                                    Intent dintent = new Intent();
                                    dintent.setAction("android.intent.action.VIEW");
                                    dintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Uri uri = Uri.parse(pushBean.getU());
                                    dintent.setData(uri);
                                    context.startActivity(dintent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    try {
                                        Intent wintent = new Intent();
                                        wintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        wintent.setAction("android.intent.action.VIEW");
                                        wintent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                                        Uri url = Uri.parse(pushBean.getB());
                                        wintent.setData(url);
                                        context.startActivity(wintent);
                                    } catch (Exception ee) {
                                        ee.printStackTrace();
                                        Intent wintent = new Intent();
                                        wintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        wintent.setAction("android.intent.action.VIEW");
                                        Uri url = Uri.parse(pushBean.getB());
                                        wintent.setData(url);
                                        context.startActivity(wintent);
                                    }
                                }
                            }
                        }
                    }).start();
                }
                break;
            default:
                break;
        }
    }
}
