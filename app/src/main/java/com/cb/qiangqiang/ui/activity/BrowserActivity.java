package com.cb.qiangqiang.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.cb.qiangqiang.R;
import com.cb.qiangqiang.data.Constants;
import com.cb.qiangqiang.data.GlobalManager;
import com.cb.qiangqiang.util.Utils;
import com.zhy.autolayout.AutoFrameLayout;

import me.imid.swipebacklayout.lib.SwipeBackLayout;

public class BrowserActivity extends BaseSwipeBackActivity {
    //常量-----------------------

    //UI--------------------------
    private TextView mTitle;
    private WebView mWebView;
    private AutoFrameLayout mLoadingLayout;
    //Data------------------------
    private Context mContext;
    private String url = "";
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Constants.SET_TITLE:
                    mTitle.setText(msg.obj.toString());
                    break;
                case Constants.SHOW_LOADING_CIRCLE:
                    mLoadingLayout.setVisibility(View.VISIBLE);
                    break;
                case Constants.HIDE_LOADING_CIRCLE:
                    mLoadingLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT | SwipeBackLayout.EDGE_RIGHT);
        View top = findViewById(R.id.top);
        Utils.initStatusAfterSetContentView(this, top);
        initData();
        initView();
    }

    private void initData() {
        url = getIntent().getStringExtra("url");
        mContext = this;
    }

    private void initView() {
        mTitle = (TextView) findViewById(R.id.tv_title);
        mWebView = (WebView) findViewById(R.id.web_view);
        mLoadingLayout = (AutoFrameLayout) findViewById(R.id.auto_fl_load);

        initWebView();

        mLoadingLayout.setVisibility(View.VISIBLE);

        //Android 4.2.2 url要是domaim, setCookie才会全部设置，不漏
        GlobalManager.sycWebViewCookie(mContext, url);
        mWebView.loadUrl(url);
    }

    private void initWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress >= 90){
                    Message msg = new Message();
                    msg.what = Constants.HIDE_LOADING_CIRCLE;
                    mHandler.sendMessage(msg);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                Message msg = new Message();
                msg.what = Constants.SET_TITLE;
                msg.obj = title;
                mHandler.sendMessage(msg);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
