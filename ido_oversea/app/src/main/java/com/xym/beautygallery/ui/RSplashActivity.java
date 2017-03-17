package com.xym.beautygallery.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.xym.beautygallery.R;
import com.xym.beautygallery.base.AppConfigMgr;
import com.xym.beautygallery.base.CommonHandler;
import com.xym.beautygallery.base.Constants;

/**
 * Created by root on 2/27/17.
 */
public class RSplashActivity extends Activity implements InterstitialAdListener, CommonHandler.MessageHandler {
    private InterstitialAd interstitialAd;
    private final static int MSG_AD_TIMEOUT = 1;
    private final static int MAX_AD_SHOW_TIME = 5000;
    private CommonHandler mHandler;
    /**
     * 当设置开屏可点击时，需要等待跳转页面关闭后，再切换至您的主窗口。故此时需要增加canJumpImmediately判断。 另外，点击开屏还需要在onResume中调用jumpWhenCanClick接口。
     */
    public boolean canJumpImmediately = false;

    private void jumpWhenCanClick() {
        if (canJumpImmediately) {
            this.startActivity(new Intent(RSplashActivity.this, MainActivity.class));
            this.finish();
        } else {
            canJumpImmediately = true;
        }

    }

    /**
     * 不可点击的开屏，使用该jump方法，而不是用jumpWhenCanClick
     */
    private void jump() {
        this.startActivity(new Intent(RSplashActivity.this, MainActivity.class));
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AdSettings.addTestDevice("7bb869238fe7926abcb500adf7f001bc");
        addShortcut();
        setContentView(R.layout.splash);
        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }
        mHandler = new CommonHandler(this);
        // Create the interstitial unit with a placement ID (generate your own on the Facebook app settings).
        // Use different ID for each ad placement in your app.
        interstitialAd = new InterstitialAd(RSplashActivity.this, "229449484183052_229453167516017");

        // Set a listener to get notified on changes or when the user interact with the ad.
        interstitialAd.setAdListener(this);

        // Load a new interstitial.
        interstitialAd.loadAd();

        mHandler.sendEmptyMessageDelayed(MSG_AD_TIMEOUT, MAX_AD_SHOW_TIME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canJumpImmediately) {
            jumpWhenCanClick();
        }
        canJumpImmediately = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        canJumpImmediately = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        interstitialAd.destroy();
    }

    @Override
    public void onInterstitialDisplayed(Ad ad) {
    }

    @Override
    public void onInterstitialDismissed(Ad ad) {
        jumpWhenCanClick();
    }

    @Override
    public void onError(Ad ad, AdError adError) {
        Log.e("xym", "onError:" + adError.getErrorCode());
        jump();
    }

    @Override
    public void onAdLoaded(Ad ad) {
        // Ad was loaded, show it!
        interstitialAd.show();
    }

    @Override
    public void onAdClicked(Ad ad) {
        jumpWhenCanClick();
    }

    private void addShortcut() {
        if (!AppConfigMgr.isShortcutSetup(this)) {
            Intent addShortcutIntent = new Intent(Constants.ACTION_ADD_SHORTCUT);
            // 不允许重复创建
            addShortcutIntent.putExtra("duplicate", false);// 经测试不是根据快捷方式的名字判断重复的
            // 应该是根据快链的Intent来判断是否重复的,即Intent.EXTRA_SHORTCUT_INTENT字段的value
            // 但是名称不同时，虽然有的手机系统会显示Toast提示重复，仍然会建立快链
            // 屏幕上没有空间时会提示
            // 注意：重复创建的行为MIUI和三星手机上不太一样，小米上似乎不能重复创建快捷方式

            // 名字
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));

            // 图标
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this,
                            R.mipmap.ic_launcher));

            // 设置关联程序
            Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
            launcherIntent.setClass(this, RSplashActivity.class);
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            addShortcutIntent
                    .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
            // 发送广播
            this.sendBroadcast(addShortcutIntent);
            AppConfigMgr.setShortcutSetup(this, true);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_AD_TIMEOUT:
                jump();
                break;
        }
    }
}
