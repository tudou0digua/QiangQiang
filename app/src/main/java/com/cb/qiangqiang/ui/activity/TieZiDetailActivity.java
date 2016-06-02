package com.cb.qiangqiang.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cb.qiangqiang.R;
import com.cb.qiangqiang.data.Api;
import com.cb.qiangqiang.data.Constants;
import com.cb.qiangqiang.data.GlobalManager;
import com.cb.qiangqiang.data.OverrideWebViewUrl;
import com.cb.qiangqiang.data.RequestManager;
import com.cb.qiangqiang.util.PreferencesUtils;
import com.cb.qiangqiang.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.imid.swipebacklayout.lib.SwipeBackLayout;

public class TieZiDetailActivity extends BaseSwipeBackActivity implements SwipeRefreshLayout.OnRefreshListener{
    //常量--------------------
    private final int timeout = 15 * 1000;
    //设置标题
    private final int SET_TITLE = 0;
    private final int LOAD_WEBVIEW_TIMEOUT = 1;
    private final int SET_TIE_ZI_UNCOLLECTED = 2;
    private final int SET_TIE_ZI_COLLECTED = 3;
    //UI--------------------
    private SwipeBackLayout mSwipeBackLayout;
    private SwipeRefreshLayout mRefreshLayout;
    private TextView mTitle;
    private WebView mWebView;
    private TextView mCollectTieZi;
    private TextView mCollected;
    //Data--------------------
    private int mReRequestNum = 1;
    private String mCollectStatus = "";
    private String mCollectMessage = "";
    private String mTid;
    private Context mContext;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SET_TITLE:
                    mTitle.setText(msg.obj.toString());
                    break;
                case LOAD_WEBVIEW_TIMEOUT:
                    mRefreshLayout.setRefreshing(false);
                    Toast.makeText(mContext, "网络不给力，可下拉刷新！", Toast.LENGTH_LONG).show();
                    break;
                case SET_TIE_ZI_UNCOLLECTED:
                    mCollected.setVisibility(View.GONE);
                    mCollectTieZi.setVisibility(View.VISIBLE);
                    break;
                case SET_TIE_ZI_COLLECTED:
                    mCollected.setVisibility(View.VISIBLE);
                    mCollectTieZi.setVisibility(View.GONE);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tie_zi_detail);
        initData();
        initView();
        //进入页面刷新
        mRefreshLayout.setRefreshing(true);
        onRefresh();
    }

    private void initData() {
        mTid = getIntent().getStringExtra("tid");
        mContext = this;
        View top = findViewById(R.id.auto_ll_navi);
        Utils.initStatusAfterSetContentView(this, top);
        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
    }

    private void initView() {
        mCollected = (TextView) findViewById(R.id.tv_collected);
        mCollectTieZi = (TextView) findViewById(R.id.tv_collect);
        mCollectTieZi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = SET_TIE_ZI_COLLECTED;
                mHandler.sendMessage(msg);
                sendCollectRequest();
            }
        });

        mTitle = (TextView) findViewById(R.id.tv_title);

        initRefreshLayout();

        initWebView();
    }

    private void sendCollectRequest() {
        String url = Api.COLLECTION_TIE_ZI;
        url = String.format(url,
                GlobalManager.getBaseParams("charset"),
                GlobalManager.getBaseParams("version"),
                GlobalManager.getBaseParams("mobile"),
                mTid,
                "favthread",
                "true");
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseCollectionData(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext, "收藏帖子出错，请重试!", Toast.LENGTH_LONG).show();
                        Message msg = new Message();
                        msg.what = SET_TIE_ZI_UNCOLLECTED;
                        mHandler.sendMessage(msg);
                    }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("formhash",PreferencesUtils.getString(mContext,"formhash"));
                params.put("charset","utf-8");
                return params;
            }
        };
        //设置请求cookie
        GlobalManager.setRequestCookie(mContext);
        //设置超时时间
        request.setRetryPolicy(new DefaultRetryPolicy(timeout, 1, 1.0f));
        //请求数据
        RequestManager.addRequest(request, mContext);

    }

    private void parseCollectionData(String response) {
        try {
            JSONObject root = new JSONObject(response);

            JSONObject message = root.getJSONObject("Message");
            mCollectStatus = message.getString("messageval");
            mCollectMessage = message.getString("messagestr");

            if (mCollectStatus.contains(Constants.TO_LOGIN)) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
                Message msg = new Message();
                msg.what = SET_TIE_ZI_UNCOLLECTED;
                mHandler.sendMessageDelayed(msg, 1000);
            }else if (mCollectStatus.equals(Constants.LOGIN_SUCCESS)){
                Toast.makeText(mContext, mCollectMessage, Toast.LENGTH_SHORT).show();
            }else if (mCollectStatus.equals(Constants.SUBMIT_INVALID)){
                JSONObject variables = root.getJSONObject("Variables");
                String formhash = variables.getString("formhash");
                PreferencesUtils.putString(mContext, "formhash", formhash);
                if (mReRequestNum >= 2){
                    Message msg = new Message();
                    msg.what = SET_TIE_ZI_UNCOLLECTED;
                    mHandler.sendMessage(msg);
                    Toast.makeText(mContext, mCollectMessage, Toast.LENGTH_SHORT).show();
                }else {
                    mReRequestNum++;
                    sendCollectRequest();
                }
            }else{
                Toast.makeText(mContext, mCollectMessage, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initRefreshLayout() {
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        //进入页面可以显示加载圈圈
        int homepage_refresh_spacing = 20;
        mRefreshLayout.setProgressViewOffset(false, -homepage_refresh_spacing * 2, homepage_refresh_spacing);
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.top_title_bg));
        mRefreshLayout.setOnRefreshListener(this);
    }

    private void initWebView() {
        mWebView = (WebView) findViewById(R.id.web_view);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
        //WebView图片后加载
        loadImageLater();
        mWebView.addJavascriptInterface(new TieZiDetailInterface(), "AppInterface");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                //设置WebView的超时操作
                Message msg = new Message();
                msg.what = LOAD_WEBVIEW_TIMEOUT;
                mHandler.sendMessageDelayed(msg, timeout);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mHandler.removeMessages(LOAD_WEBVIEW_TIMEOUT);
                mRefreshLayout.setRefreshing(false);
                //开始图片加载
                if (!mWebView.getSettings().getLoadsImagesAutomatically()) {
                    mWebView.getSettings().setLoadsImagesAutomatically(true);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("URL", url);
                Boolean isOverride = OverrideWebViewUrl.overrideUrlLoading(view, url);
                if (isOverride != null)
                    return isOverride;
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                //自定义出错页面
                //清除掉默认错误页内容
                //                mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
                //显示自已的出错页面
                //TODO
            }
        });
        //android 4.4.4 jsoup中的touchend方法不触发，手动触发touchend方法
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

    private void loadImageLater() {
        //4.4以上系统在onPageFinished时再恢复图片加载时,如果存在多张图片引用的是相同的src时
        // 会只有一个image标签得到加载，因而对于这样的系统我们就先直接加载
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            mWebView.getSettings().setLoadsImagesAutomatically(true);
        }else {
            mWebView.getSettings().setLoadsImagesAutomatically(false);
        }
    }
    //android 4.4.4 jsoup中的touchend方法不触发，手动触发touchend方法
    private void loadMore() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
            mWebView.evaluateJavascript("javascript:window.dispatchEvent(new CustomEvent('touchend'))",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                        }
                    });
        }
    }

    @Override
    public void onRefresh() {
        mHandler.removeMessages(LOAD_WEBVIEW_TIMEOUT);
        String url = getUrl();
        //TODO
        //Android 4.2.2 url要是domaim, setCookie才会全部设置，不漏
        GlobalManager.sycWebViewCookie(mContext, url);
//        Log.d("tag", url);
        mWebView.loadUrl(url);
    }

    private String getUrl(){
        String url = Api.TIE_ZI_DETAIL;
        String charset = GlobalManager.getBaseParams("charset");
        String version = GlobalManager.getBaseParams("version");
        String mobile = GlobalManager.getBaseParams("mobile");
        url = String.format(url,
                "viewthread",
                "1",
                charset,
                "1",
                "10",
                "1",
                mTid,
                mobile,
                version);
        return url;
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
        mHandler.removeMessages(LOAD_WEBVIEW_TIMEOUT);
        mWebView.removeAllViews();
        mWebView.destroy();
    }

    /**
     * js回调接口
     */
    public class TieZiDetailInterface{
        @JavascriptInterface
        public void notifyForum(String forumname){
            Message msg = new Message();
            msg.what = SET_TITLE;
            msg.obj = forumname;
            mHandler.sendMessage(msg);
        }
        @JavascriptInterface
        public void viewUser(String userId){
            Toast.makeText(mContext, userId, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void replayPost(String postId){
            Toast.makeText(mContext, postId, Toast.LENGTH_SHORT).show();
        }
    }
}
