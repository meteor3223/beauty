package com.example.re.getui_sdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.net.URLDecoder;

public class AdActivity extends Activity {

    private boolean isInited;
    private WebView webView;
    private TextView back;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);
        init();
        loadUrl();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }


    private void init() {
        System.out.println("init view ########");
        webView = (WebView) findViewById(R.id.webView);
        isInited = true;
        back= ((TextView) findViewById(R.id.back));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {  //表示按返回键
                    webView.goBack();   //后退
                }else{
                    AdActivity.this.finish();
                }
            }
        });
    }

    private void loadUrl() {
        Intent intent = getIntent();// 该demo应用包名: com.getui.demo.notifaction
        Uri uri = intent.getData();// lbscomgetuidemonotifaction://browser?url=http%3A%2F%2Fwww.baidu.com

        if (uri != null) {
            String urlStr = uri.toString();// url 格式 :
            // lbscomgetuidemonotifaction://browser?url=http%3A%2F%2Fwww.baidu.com

            String packageName=this.getPackageName().replaceAll("\\.","");

            if (!TextUtils.isEmpty(urlStr) && urlStr.startsWith("lbs"+packageName)) {
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setSupportZoom(true);
                webView.requestFocus();



                webView.loadUrl(URLDecoder.decode(urlStr.replace("lbs"+packageName+"://browser?url=", "")));

                webView.setWebViewClient(new MyWebViewClient());

                webView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onProgressChanged(WebView view, int newProgress) {
                        // TODO Auto-generated method stub
                        if (newProgress == 100) {
                            // 网页加载完成


                        } else {
                            // 加载中

                        }

                    }
                });
            } else {
                Log.e("AdActivity", "url = " + urlStr + " is invalid ##########");
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        System.out.println("onNewIntent ########");

        if (!isInited) {
            init();
        }

        loadUrl();
    }

    class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);

            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {  //表示按返回键

                webView.goBack();
                return true;
            }else{
                this.finish();
            }
        }
        return false;
    }
}

