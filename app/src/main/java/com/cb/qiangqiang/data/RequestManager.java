package com.cb.qiangqiang.data;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie2;

import java.net.CookieHandler;
import java.net.CookiePolicy;

/**
 * Created by cb on 2016/1/1.
 */
public class RequestManager {
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;
    private static DefaultHttpClient mHttpClient;

    private RequestManager(){

    }

    public static void init(Context context){
//        mHttpClient = new DefaultHttpClient();
//        mRequestQueue = Volley.newRequestQueue(context, new HttpClientStack(mHttpClient));

        java.net.CookieManager cookieManager = new java.net.CookieManager(
                new PersistentCookieStore(context), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        mRequestQueue = Volley.newRequestQueue(context);

        //这个取单个应用最大使用内存的1/8
//        int maxSize=(int)Runtime.getRuntime().maxMemory()/8;

        int memClass = ((ActivityManager)context.
                getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        int cacheSize = 1024 * 1024 * 10;
        Log.d("Tag", cacheSize + " ");
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapCache(cacheSize));
    }

    public static RequestQueue getRequestQueue(){
        if (mRequestQueue != null){
            return mRequestQueue;
        }else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    public static void setCookie(BasicClientCookie2 cookie){
        org.apache.http.client.CookieStore cookieStore = mHttpClient.getCookieStore();
        cookieStore.addCookie(cookie);

    }

    public static void addRequest(Request<?> request, Object tag){
        if (tag != null){
            request.setTag(tag);
        }
        mRequestQueue.add(request);
    }

    public static void cancelAll(Object tag){
        mRequestQueue.cancelAll(tag);
    }

    public static ImageLoader getImageLoader() {
        if (mImageLoader != null) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }

}
