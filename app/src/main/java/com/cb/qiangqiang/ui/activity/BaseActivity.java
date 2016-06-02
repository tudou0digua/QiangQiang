package com.cb.qiangqiang.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cb.qiangqiang.data.RequestManager;
import com.cb.qiangqiang.data.VolleyErrorHelper;
import com.zhy.autolayout.AutoLayoutActivity;

/**
 * Created by cb on 2016/1/1.
 */
public class BaseActivity extends AutoLayoutActivity {
    protected Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
    }

    @Override
    public void onStop() {
        super.onStop();
        RequestManager.cancelAll(this);
    }

    protected void executeRequest(Request<?> request) {
        RequestManager.addRequest(request, this);
    }

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity,
                        VolleyErrorHelper.getMessage(error, activity),
                        Toast.LENGTH_LONG)
                        .show();
            }
        };
    }
}
