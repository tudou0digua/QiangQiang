package com.cb.qiangqiang.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cb.qiangqiang.R;
import com.cb.qiangqiang.data.Constants;
import com.cb.qiangqiang.data.RequestManager;
import com.cb.qiangqiang.data.VolleyErrorHelper;
import com.cb.qiangqiang.model.LoginData;
import com.cb.qiangqiang.util.PreferencesUtils;
import com.google.gson.Gson;
import com.zhy.autolayout.AutoLayoutActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AutoLayoutActivity{
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private static final String LOGIN_SUCESS = "login_succeed";


    private String mLoginStatus;
    private String mLoginMessage = " ";
    private Pattern mPattern = Pattern.compile(EMAIL_PATTERN);
    // UI references.
    private AutoCompleteTextView mUserNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        mUserNameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);

        mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mUserNameView.setText("tudou0digua");
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);



        // Store values at the time of the login attempt.
        String username = mUserNameView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            mUserNameView.setError("用户名不能为空");
            focusView = mUserNameView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("密码不能为空");
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            postLogin(username, password);
        }
    }

    private void postLogin(final String username, final String password) {
        String url = "http://www.qiangqiang5.com/api/mobile/index.php?loginfield=auto&charset=utf-8&version=3&loginsubmit=yes&mobile=no&module=login";


/*        JsonObjectRequest request = new JsonObjectRequest(url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseJson(response);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showProgress(false);
                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                        VolleyErrorHelper.getMessage(error, LoginActivity.this);
                    }
                }
        );*/

/*        Map<String, String> params = new HashMap<>();
        params.put("charset","utf-8");
        params.put("username",username);
        params.put("password",password);*/

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseJson(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showProgress(false);
                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                        VolleyErrorHelper.getMessage(error, LoginActivity.this);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("charset","utf-8");
                params.put("username",username);
                params.put("password",password);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 1, 1.0f));
        RequestManager.addRequest(request, LoginActivity.this);
    }

    private void parseJson(String strJson) {
        LoginData loginData = null;
        try {
            JSONObject response = new JSONObject(strJson);
            JSONObject message = response.getJSONObject("Message");
            mLoginStatus = message.getString("messageval");
            mLoginMessage = message.getString("messagestr");

            if (mLoginStatus.trim().equals(LOGIN_SUCESS)){
                JSONObject variables = response.getJSONObject("Variables");
                loginData = new Gson().fromJson(variables.toString(), LoginData.class);
                if (loginData != null){
                    saveLoginData(loginData);
                }

                Toast.makeText(LoginActivity.this, mLoginMessage, Toast.LENGTH_SHORT).show();
                setResult(200);
                finish();
            } else if (mLoginStatus.trim().equals(Constants.SUBMIT_SECODE_INVALID)){
                String username = mUserNameView.getText().toString().trim();
                String password = mPasswordView.getText().toString().trim();
                Intent intent = new Intent(LoginActivity.this, VerifyActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("password", password);
                //进入输入验证码页面
                startActivityForResult(intent, 100);
            } else {
                showProgress(false);
                Toast.makeText(LoginActivity.this, mLoginMessage, Toast.LENGTH_SHORT).show();
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
        PreferencesUtils.putString(LoginActivity.this, "member_uid", loginData.memberUid);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.SUCESS){
            finish();
        }
    }

    private boolean isEmailValid(String email){
        Matcher matcher = mPattern.matcher(email);
        return matcher.matches();
    }

    private boolean isUserNameValid(String userName) {
        return userName.length() >= 3 && userName.length() <= 15 ;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 7;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUserNameView.setAdapter(adapter);
    }
}

