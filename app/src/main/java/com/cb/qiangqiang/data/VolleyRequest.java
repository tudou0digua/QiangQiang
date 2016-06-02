package com.cb.qiangqiang.data;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

/**
 * Created by cb on 2016/1/22.
 */
public class VolleyRequest {
    public static final int GET = Request.Method.GET;
    public static final int POST = Request.Method.POST;
    public static final int DEFAULT_TIMEOUT = 10 * 1000;

    public static void get(String url, Context context, IVolleyStringResponse volleyStringRespons){
        stringRequest(GET, url, DEFAULT_TIMEOUT, 1, context, null, context, volleyStringRespons);
    }

    public static void post(String url, Map<String, String> params, Context context,
                            IVolleyStringResponse volleyStringRespons){
        stringRequest(POST, url, DEFAULT_TIMEOUT, 1, context, params, context, volleyStringRespons);
    }

    /**
     * 封装Volley StringRequest请求
     * @param method 请求方式（GET/POST）
     * @param url
     * @param timeout
     * @param retryNum
     * @param context
     * @param params post请求参数（body）
     * @param volleyStringResponse 请求回调接口
     * @param tag addRequest时的Tag（一般设为Activity）
     */
    public static void stringRequest(int method, String url, int timeout, int retryNum,
                                     Context context, final Map<String, String> params,
                                     Object tag, final IVolleyStringResponse volleyStringResponse){
        StringRequest request = new StringRequest(method, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        volleyStringResponse.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        volleyStringResponse.onErrorResponse(error);
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (params != null && params.size() > 0){
                    return params;
                }
                return super.getParams();
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                Response<String> selfResponse = volleyStringResponse.parseNetworkResponse(response);
                if (selfResponse != null){
                    return selfResponse;
                }
                return super.parseNetworkResponse(response);
            }
        };
        //设置超时时间
        if (timeout <= 0 ){
            timeout = DEFAULT_TIMEOUT;
        }
        request.setRetryPolicy(new DefaultRetryPolicy(timeout, retryNum, 1.0f));
        //请求数据
        RequestManager.addRequest(request, tag);
    }

    /**
     * StringRequest回调接口
     */
    public interface IVolleyStringResponse {
        /**
         * 请求成功
         * @param response
         */
        void onResponse(String response);

        /**
         * 请求失败
         * @param error
         */
        void onErrorResponse(VolleyError error);

        /**
         * 请求响应头,不需要设置，返回null即可
         * @param response 请求响应
         * @return
         */
        Response<String> parseNetworkResponse(NetworkResponse response);
    }
}
