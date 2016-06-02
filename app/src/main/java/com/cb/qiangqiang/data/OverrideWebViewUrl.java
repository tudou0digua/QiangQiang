package com.cb.qiangqiang.data;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;

import com.cb.qiangqiang.ui.activity.TieZiDetailActivity;

/**
 * Webview自定义url的跳转
 * Created by cb on 2016/1/19.
 */
public class OverrideWebViewUrl {

    @Nullable
    public static Boolean overrideUrlLoading(final WebView webView, String url){
        Context context = webView.getContext();
        Handler handler = new Handler(context.getMainLooper());

        if (url.contains("http://www.qiangqiang5.com/thread")) {
            Intent intent = new Intent(context, TieZiDetailActivity.class);
            String[] strs = url.split("-");
            String tid = strs[1];
            intent.putExtra("tid", tid);
            context.startActivity(intent);
            return true;
        }
        if (url.contains("http://www.qiangqiang5.com/forum.php?mod=viewthread")) {
            Intent intent = new Intent(context, TieZiDetailActivity.class);
            String[] strs = url.split("&");
            String tid = "";
            for (String str : strs){
                if (str.contains("tid=")){
                    tid = str.replace("tid=","");
                    break;
                }
            }
            intent.putExtra("tid", tid);
            context.startActivity(intent);
            return true;
        }
        if (url.contains("http://www.qiangqiang5.com/forum.php?")) {
            return false;
        }

        if (url.contains("ju.taobao.com")) {
            if (!hasAppInstalled(context, "com.taobao.ju.android")){
                return false;
            }
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri uri = Uri.parse(url);
            intent.setData(uri);
            intent.setPackage("com.taobao.ju.android");
            context.startActivity(intent);
            return true;
        }

        if (url.contains("item.taobao.com") ||
                url.contains("taobao.com/market")) {
            if (!hasAppInstalled(context, "com.taobao.taobao")){
                return false;
            }
            avoidNullPage(webView, handler);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri uri = Uri.parse(url);
            intent.setData(uri);
            intent.setPackage("com.taobao.taobao");
            context.startActivity(intent);
            return true;
        }
        if (url.contains("tmall.com") || url.contains("taoquan.taobao.com")) {
            if (!hasAppInstalled(context, "com.tmall.wireless")){
                return false;
            }
            avoidNullPage(webView, handler);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri uri = Uri.parse(url);
            intent.setData(uri);
            intent.setPackage("com.tmall.wireless");
            context.startActivity(intent);

            return true;
        }

        return null;
    }

    /**
     * 避免返回时出现空白页
     * @param webView
     * @param handler
     */
    private static void avoidNullPage(final WebView webView, Handler handler) {

        if (webView == null){
            return;
        }
        //淘宝链接判断
        if (webView.canGoBack()){
            WebBackForwardList webBackForwardList = webView.copyBackForwardList();
            String historyUrl = webBackForwardList.getItemAtIndex(webBackForwardList.getCurrentIndex()).getUrl();
            if (historyUrl.contains("s.click.taobao.com")){
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        webView.goBack();
                    }
                }, 2000);
            }
        }
    }

    public static boolean hasAppInstalled(Context context, String packageName){
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            return false;
        }else {
            return true;
        }
    }
}
