package com.cb.qiangqiang.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cb.qiangqiang.R;
import com.cb.qiangqiang.data.RequestManager;
import com.cb.qiangqiang.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import cn.bingoogolapple.refreshlayout.BGARefreshViewHolder;
import me.imid.swipebacklayout.lib.SwipeBackLayout;

public class WebActivity extends BaseSwipeBackActivity implements BGARefreshLayout.BGARefreshLayoutDelegate{
    private BGARefreshLayout mRefreshLayout;
    private WebView mWebView;
    private String mTid;
    private String mRootHtml = "";
    private int mPages;
    private int mCurrentPages = 1;
    private SwipeBackLayout mSwipeBackLayout;
    private TextView mTitle;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
//                    mRootHtml = msg.obj.toString();
//                    mRootHtml = mRootHtml.replace("页面加载中...","点击加载更多");
                    mWebView.loadData(mRootHtml, "text/html;charset=UTF-8", null);
//                    mWebView.loadData(mRootHtml,"text/html", "gbk");
                    /*try {
                        String firstHtml = new String(((String)msg.obj).getBytes("gbk"),"gbk");
                        mWebView.loadData(msg.obj.toString(), "text/html", "gbk");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }*/

                    mRefreshLayout.endRefreshing();
                    break;
                case 1:
                    String moreHtml = parseLoadMoreData(msg.obj.toString());
                    mWebView.loadData(moreHtml, "text/html;charset=UTF-8", null);
                    mRefreshLayout.endLoadingMore();
                    break;

                case 2:
//                    mWebView.evaluateJavascript();
                    break;
                case 3:
                    mTitle.setText(msg.obj.toString());

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        mTid = getIntent().getStringExtra("tid");
        String pages = getIntent().getStringExtra("pages");
        if (pages == null || pages.trim() == ""){
            mPages = 0;
        }else{
            mPages = Integer.parseInt(pages.trim());
        }
        View top = findViewById(R.id.auto_ll_navi);
        Utils.initStatusAfterSetContentView(this, top);
        intiView();
    }

    private void intiView() {
        mTitle = (TextView) findViewById(R.id.tv_title);
        mWebView = (WebView) findViewById(R.id.web_view);
        mRefreshLayout = (BGARefreshLayout) findViewById(R.id.refresh_layout);
        initWebView();
        initRefreshLayout();
    }


    private void initWebView() {


        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
        mWebView.addJavascriptInterface(new AppInterface(), "AppInterface");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mRefreshLayout.endRefreshing();


            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("http://www.qiangqiang5.com/thread")) {
                    Intent intent = new Intent(WebActivity.this, WebActivity.class);
                    String[] strs = url.split("-");
                    String tid = strs[1];
                    intent.putExtra("tid", tid);
                    startActivity(intent);
                    return true;
                } else if (url.contains("http://www.qiangqiang5.com/forum.php?")) {
                    //                    Intent intent = new Intent(WebActivity.this, WebActivity.class);
                    //                    String[] strs = url.split("&");
                    //                    String tid = "";
                    //                    for (int i = 0; i < strs.length; i++) {
                    //                        if (strs[i].contains("tid")) {
                    //                            String[] tids = strs[i].split("=");
                    //                            tid = tids[1];
                    //                            break;
                    //                        }
                    //                    }
                    Log.d("aaaa", url);
                    //                    Log.d("aaaa", tid);
                    //                    intent.putExtra("tid", tid);
                    //                    startActivity(intent);
                    return false;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    loadMore();
                }
                return false;
            }
        });
    }


    private void initRefreshLayout() {
        mRefreshLayout.setDelegate(this);
        BGARefreshViewHolder refreshViewHolder = new BGANormalRefreshViewHolder(this, true);
        mRefreshLayout.setRefreshViewHolder(refreshViewHolder);
        mRefreshLayout.beginRefreshing();
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout bgaRefreshLayout) {
        refreshWebView();

    }

    private void refreshWebView() {
        String url = "http://www.qiangqiang5.com/api/mobile/index.php?module=viewthread&page=1&charset=utf-8&image=1&ppp=10&debug=1&tid=" + mTid + "&mobile=no&version=3";
        //Android 4.2.2 url要是domaim, setCookie才会全部设置，不漏
        syncCookie(mWebView.getContext(), url);
        mWebView.loadUrl(url);

    }

    private void syncCookie(Context context, String url){
        try{
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();// 移除
            cookieManager.removeAllCookie();

            SharedPreferences pref = getSharedPreferences("login_data", Activity.MODE_PRIVATE);
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
            builder.append(URLEncoder.encode(auth,"utf-8"));
            builder.append("; ");
            builder.append("domian=qiangqiang5.com; path=/");
            cookieManager.setCookie(url, builder.toString());

            String cookies = cookieManager.getCookie(url);

            cookieSyncManager.sync();
        }catch(Exception e){
//            Log.e("Nat: webView.syncCookie failed", e.toString());
        }
    }


    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout bgaRefreshLayout) {
        loadMore();
        return true;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void loadMore() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            //            Toast.makeText(getActivity(), "loading more...", Toast.LENGTH_SHORT).show();
            mWebView.evaluateJavascript("javascript:window.dispatchEvent(new CustomEvent('touchend'))",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
        }

    }

    private void requestData(String url, final int status) {
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Message msg = new Message();
                if (status == 1){
                    mCurrentPages++;
                }else if (status == 0){
                    mRootHtml = response;
                }
                msg.what = status;

                msg.obj = response;
                mHandler.sendMessage(msg);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestManager.addRequest(request,"post_load_more");
    }
    private String parseLoadMoreData(String str){
        String finalHtml = mRootHtml;
        String moreData = "<a name=\"bottom\"></a>";

        try {
            JSONObject root = new JSONObject(str);
            JSONObject variable = root.getJSONObject("Variables");
            moreData = variable.getString("webview_page");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        finalHtml = finalHtml.replace("<a name=\"bottom\"></a>", moreData);
        return finalHtml;
    }

    public class AppInterface{
        @JavascriptInterface
        public void notifyForum(String forumname){
            Message msg = new Message();
            msg.what = 3;
            msg.obj = forumname;
            mHandler.sendMessage(msg);
//            Toast.makeText(WebActivity.this, forumname, Toast.LENGTH_SHORT).show();
        }
        @JavascriptInterface
        public void viewUser(String userId){
            Toast.makeText(WebActivity.this, userId, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void replayPost(String postId){
            Toast.makeText(WebActivity.this, postId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.removeAllViews();
        mWebView.destroy();
    }
}
