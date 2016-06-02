package com.cb.qiangqiang.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.cb.qiangqiang.R;
import com.cb.qiangqiang.data.Constants;
import com.cb.qiangqiang.data.GlobalManager;
import com.cb.qiangqiang.data.RequestManager;
import com.cb.qiangqiang.model.LoginData;
import com.google.gson.Gson;
import com.zhy.autolayout.AutoLayoutActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class VerifyActivity extends AutoLayoutActivity {
    private final int timeout = 10 *1000;

    private MaterialProgressBar mProgressBar;
    private ImageView mImageView;
    private TextView mRefreshVerify;
    private EditText mEditText;
    private Button mSubmit;
    private String urlRefreshVerifyImage;
    private String urlForVerify;
    private String urlLoginWifhVerify;
    private String secHash;
    private String userName;
    private String passWord;
    private String mLoginStatus;
    private String mLoginMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        userName = getIntent().getStringExtra("username");
        passWord = getIntent().getStringExtra("password");
        initData();
        initView();
        requestVerifyData();
    }

    private void requestVerifyData() {
        String url = urlForVerify;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseVerifyData(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        //设置超时时间
        request.setRetryPolicy(new DefaultRetryPolicy(timeout, 2, 1.0f));
        //请求数据
        RequestManager.addRequest(request, this);
    }

    private void parseVerifyData(String response) {
        try {
            JSONObject root = new JSONObject(response);
            JSONObject variables = root.getJSONObject("Variables");
            secHash = variables.getString("sechash");
            urlRefreshVerifyImage = variables.getString("seccode");
            loadVerifyImage(urlRefreshVerifyImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        urlRefreshVerifyImage = "";
        urlForVerify = "http://www.qiangqiang5.com/api/mobile/index.php?charset=utf-8&version=3&force=1&secversion=3&mobile=no&debug=1&type=login&module=secure";
        urlLoginWifhVerify = "http://www.qiangqiang5.com/api/mobile/index.php?sechash=&s&loginfield=&s&charset=&s&seccodeverify=&s&secqaahash=&s&version=&s&seccodehash=&s&loginsubmit=&s&mobile=&s&module=&s";
        secHash = "";
        mLoginStatus = "";
        mLoginMessage = "";
    }

    private void initView() {
        mProgressBar = (MaterialProgressBar) findViewById(R.id.progress_circle);

        mImageView = (ImageView) findViewById(R.id.iv_verify);

        mRefreshVerify = (TextView) findViewById(R.id.tv_refresh_image);
        mRefreshVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadVerifyImage(urlRefreshVerifyImage);
            }
        });

        mEditText = (EditText) findViewById(R.id.et_verify);

        mSubmit = (Button) findViewById(R.id.btn_submit);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                loginWithVerify();
            }
        });

    }

    private void loginWithVerify() {
        String temp = mEditText.getText().toString().trim();
        if (temp != null && temp != ""){
            String url = String.format(urlLoginWifhVerify,
                    secHash,
                    "auto",
                    GlobalManager.getBaseParams("charset"),
                    temp,
                    secHash,
                    GlobalManager.getBaseParams("version"),
                    secHash,
                    "yes",
                    GlobalManager.getBaseParams("mobile"),
                    "login");
            sengRequest(url);
        }else {
            Toast.makeText(this, "验证码不能为空", Toast.LENGTH_LONG).show();
        }
    }

    private void sengRequest(String url) {
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        parseData(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("charset","utf-8");
                params.put("username",userName);
                params.put("password",passWord);
                return params;
            }
        };
        //设置超时时间
        request.setRetryPolicy(new DefaultRetryPolicy(timeout, 2, 1.0f));
        //请求数据
        RequestManager.addRequest(request, this);
    }

    private void parseData(String strJson) {
        LoginData loginData = null;
        try {
            JSONObject response = new JSONObject(strJson);
            JSONObject message = response.getJSONObject("Message");
            mLoginStatus = message.getString("messageval");
            mLoginMessage = message.getString("messagestr");

            if (mLoginStatus.trim().equals(Constants.LOGIN_SUCCESS)){
                JSONObject variables = response.getJSONObject("Variables");
                loginData = new Gson().fromJson(variables.toString(), LoginData.class);
                if (loginData != null){
                    saveLoginData(loginData);
                }

                Toast.makeText(VerifyActivity.this, mLoginMessage, Toast.LENGTH_SHORT).show();
                setResult(Constants.SUCESS);
                finish();
            } else {
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(VerifyActivity.this, mLoginMessage, Toast.LENGTH_SHORT).show();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private void saveLoginData(LoginData loginData) {
        SharedPreferences.Editor editor = getSharedPreferences("login_data", MODE_PRIVATE).edit();
        editor.putString("cookiepre", loginData.cookiepre);
        editor.putString("auth", loginData.auth);
        editor.putString("saltkey", loginData.saltkey);
        editor.putString("formhash", loginData.formhash);
        editor.commit();
    }
    private void loadVerifyImage(String refreshVerifyImageURL) {
        if ((refreshVerifyImageURL != null) && (refreshVerifyImageURL.trim() != "")) {
            Glide.with(VerifyActivity.this)
                    .load(refreshVerifyImageURL)
                    .into(mImageView);
        }
    }
}
