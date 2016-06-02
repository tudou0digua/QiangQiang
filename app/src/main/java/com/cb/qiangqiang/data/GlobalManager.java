package com.cb.qiangqiang.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.cb.greendao.dao.DaoMaster;
import com.cb.greendao.dao.DaoSession;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cb on 2016/1/14.
 */
public class GlobalManager {
    private static GlobalManager mManager;
    private Map<String, String> mBaseParams;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    public GlobalManager() {
        initData();
    }

    private void initData() {
        mBaseParams = new HashMap<>();
        mBaseParams.put("charset", "utf-8");
        mBaseParams.put("version", "3");
        mBaseParams.put("mobile", "no");
    }

    public static GlobalManager getInstance() {
        if (mManager == null) {
            mManager = new GlobalManager();
        }
        return mManager;
    }

    public static String getBaseParams(String key) {
        //没有返回null
        return GlobalManager.getInstance().mBaseParams.get(key);
    }

    /**
     * 取得DaoMaster
     *
     * @param context
     * @return
     */
    public static DaoMaster getDaoMaster(Context context) {
        if (daoMaster == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "qiangqiang-db", null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return daoMaster;
    }

    /**
     * 取得DaoSession
     *
     * @param context
     * @return
     */
    public static DaoSession getDaoSession(Context context) {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster(context);
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }

    /**
     * 设置webViewloadUrl的cookie
     *
     * @param context
     * @param url
     */
    public static void sycWebViewCookie(Context context, String url) {
        try {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();// 移除
            cookieManager.removeAllCookie();

            SharedPreferences pref = context.getSharedPreferences("login_data", Activity.MODE_PRIVATE);
            String cookiepre = pref.getString("cookiepre", " ");
            String auth = pref.getString("auth", " ");
            String saltkey = pref.getString("saltkey", " ");

            StringBuilder builder = new StringBuilder();
            builder.append(cookiepre);
            builder.append("saltkey=");
            builder.append(saltkey);
            builder.append("; ");
            builder.append("domian=qiangqiang5.com; path=/");
            String cookieValue = builder.toString();
            cookieManager.setCookie(url, cookieValue);

            builder = new StringBuilder();
            builder.append(cookiepre);
            builder.append("auth=");
            //auth要进行转义
            builder.append(URLEncoder.encode(auth, "utf-8"));
            builder.append("; ");
            builder.append("domian=qiangqiang5.com; path=/");
            cookieManager.setCookie(url, builder.toString());

            String cookies = cookieManager.getCookie(url);

            cookieSyncManager.sync();
        } catch (Exception e) {
            //            Log.d("Nat: webView.syncCookie failed",e.toString());
        }
    }

    /**
     * 设置Volley请求的cookie
     *
     * @param context
     */
    public static void setRequestCookie(Context context) {
        //        CookieManager manager = new CookieManager();
        //        CookieHandler.setDefault(manager);
        /*SharedPreferences pref = context.getSharedPreferences("login_data", Activity.MODE_PRIVATE);
        String cookiepre = pref.getString("cookiepre", " ");
        String auth = pref.getString("auth", " ");
        String saltkey = pref.getString("saltkey", " ");

        StringBuilder builder = new StringBuilder();
        builder.append(cookiepre);
        builder.append("saltkey=");
        builder.append(saltkey);
        builder.append(";");
        builder.append(cookiepre);
        builder.append("auth=");
        try {
            builder.append(URLEncoder.encode(auth, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        RequestManager.setCookie(new BasicClientCookie2("cookies", builder.toString()));*/
    }

}
