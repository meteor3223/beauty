package com.xym.beautygallery.module;

import android.webkit.WebView;

/**
 * Created by root on 7/21/16.
 */
public class BeautyJSController {
    public String insertContentTitle(String picData, String summarize) {
        return "javascript:insertBeautyItem('" + picData + "','" + summarize + "')";
    }

    public void execJS(WebView webView, String js) {
        if (webView != null) {
            webView.loadUrl(js);
            webView.postInvalidate();
        }
    }
}
