package com.cb.qiangqiang.ui.fragment;


import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.cb.qiangqiang.R;
import com.cb.qiangqiang.view.ScrollWebView;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    private ScrollWebView mWebView;
    private SwipeRefreshLayout mRefreshLayout;

    public TestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        mWebView = (ScrollWebView) view.findViewById(R.id.web_view);
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        initRefreshLayout() ;
        initWebView();

        return view;
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(this);
    }

    private void initWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setOnCustomScroolChangeListener(new ScrollWebView.ScrollInterface() {
            @Override
            public void onSChanged(int l, int t, int oldl, int oldt) {
//                Toast.makeText(getActivity(), "loading more...", Toast.LENGTH_SHORT).show();
              /*  //WebView的总高度
                float webViewContentHeight=mWebView.getContentHeight() * mWebView.getScale();
                //WebView的现高度
                float webViewCurrentHeight=(mWebView.getHeight() + mWebView.getScrollY());
                System.out.println("webViewContentHeight="+webViewContentHeight);
                System.out.println("webViewCurrentHeight="+webViewCurrentHeight);
                if ((webViewContentHeight-webViewCurrentHeight) <= 1) {
//                    Toast.makeText(getActivity(), "loading more...", Toast.LENGTH_SHORT).show();
                    loadMore();
                }*/
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            WebView.setWebContentsDebuggingEnabled(true);
        }
        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    loadMore();
                }
                return false;
            }
        });
        String url = "http://www.qiangqiang5.com/api/mobile/index.php?module=viewthread&page=1&charset=utf-8&image=1&ppp=10&debug=1&tid=73427&mobile=no&version=3";
        mWebView.loadUrl(url);
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

    @Override
    public void onRefresh() {
        String url = "http://www.qiangqiang5.com/api/mobile/index.php?module=viewthread&page=1&charset=utf-8&image=1&ppp=10&debug=1&tid=73427&mobile=no&version=3";
        mWebView.loadUrl(url);
        mRefreshLayout.setRefreshing(false);
    }
}
