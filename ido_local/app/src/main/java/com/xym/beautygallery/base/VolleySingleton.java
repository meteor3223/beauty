package com.xym.beautygallery.base;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouzhiyong on 14-9-7.
 */
public class VolleySingleton {

    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private VolleySingleton(Context context) {

        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return this.mRequestQueue;
    }

    public StringRequest startQuery(String url, Response.Listener<String> listener, Response.ErrorListener error) {
        StringRequest request = new StringRequest(Request.Method.GET, url, listener, error);
        mRequestQueue.add(request);
        return request;
    }

    public StringRequest startPost(String url, Response.Listener<String> listener, Response.ErrorListener error) {
        StringRequest request = new StringRequest(Request.Method.POST, url, listener, error);
        mRequestQueue.add(request);
        return request;
    }

    public JsonRequest startPost(String url, String postBody, Response.Listener<String> listener, Response.ErrorListener error) {
        JsonRequest<String> request = new JsonRequest<String>(Request.Method.POST, url, postBody, listener, error) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse networkResponse) {
                String je = null;
                try {
                    je = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return Response.success(je, HttpHeaderParser.parseCacheHeaders(networkResponse));
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");

                return headers;
            }
        };
        mRequestQueue.add(request);
        return request;
    }
}
