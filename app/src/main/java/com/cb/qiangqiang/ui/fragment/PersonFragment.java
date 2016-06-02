package com.cb.qiangqiang.ui.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.cb.qiangqiang.R;
import com.cb.qiangqiang.data.Api;
import com.cb.qiangqiang.data.Constants;
import com.cb.qiangqiang.data.VolleyRequest;
import com.cb.qiangqiang.model.PersonInfo;
import com.cb.qiangqiang.ui.activity.CollectionActivity;
import com.cb.qiangqiang.ui.activity.LoginActivity;
import com.cb.qiangqiang.ui.activity.TieZiListActivity;
import com.cb.qiangqiang.util.PreferencesUtils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class PersonFragment extends Fragment implements View.OnClickListener{
    //常量----------------------------
    private static String TAG = "PersonFragment";
    private static final int REFRESH_VIEW = 100;

    //UI------------------------------
    private RelativeLayout avatarAndNameContainer;
    private CircleImageView avatar;
    private TextView name;
    private TextView loginOrRegister;
    private LinearLayout llCollection;
    private LinearLayout llTieZi;
    private LinearLayout llMsg;
    private TextView collectionNum;
    private TextView tieZiNum;
    private TextView msgNum;

    //Data----------------------------
    private Context mContext;
    private PersonInfo mData;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REFRESH_VIEW:
                    refreshView();
                    break;
            }
        }
    };

    public PersonFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_person, container, false);
        initView(view);
        init();
        return view;
    }

    private void initView(View view) {
        avatarAndNameContainer = (RelativeLayout) view.findViewById(R.id.rl_avatar_name);
        avatar = (CircleImageView) view.findViewById(R.id.iv_avatar);
        name = (TextView) view.findViewById(R.id.tv_name);
        loginOrRegister = (TextView) view.findViewById(R.id.tv_login_or_register);
        collectionNum = (TextView) view.findViewById(R.id.tv_collection);
        tieZiNum = (TextView) view.findViewById(R.id.tv_tei_zi);
        msgNum = (TextView) view.findViewById(R.id.tv_msg);
        llCollection = (LinearLayout) view.findViewById(R.id.ll_collection);
        llTieZi = (LinearLayout) view.findViewById(R.id.ll_tie_zi);
        llMsg = (LinearLayout) view.findViewById(R.id.ll_msg);

        avatarAndNameContainer.setOnClickListener(this);
        llCollection.setOnClickListener(this);
        llTieZi.setOnClickListener(this);
        llMsg.setOnClickListener(this);
        loginOrRegister.setOnClickListener(this);
    }

    private void init() {
        mData = new PersonInfo();
        String uid = PreferencesUtils.getString(mContext, "member_uid");
        if (uid == null || uid.equals("0")){
            gotoLoginActivity();
            return;
        }
        getPersonInfo(uid);
    }

    private void getPersonInfo(String uid) {
        String url = String.format(Api.PERSONAL, uid);
        VolleyRequest.get(url, mContext, new VolleyRequest.IVolleyStringResponse() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                parseData(response);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());
            }

            @Override
            public Response<String> parseNetworkResponse(NetworkResponse response) {
                return null;
            }
        });
    }

    private void parseData(String response) {
        if (response == null || TextUtils.isEmpty(response)) return;
        try {
            JSONObject object = new JSONObject(response);
            JSONObject variables = object.getJSONObject("Variables");
            JSONObject space = variables.getJSONObject("space");
            mData = new Gson().fromJson(space.toString(), PersonInfo.class);
            if (variables.getString("member_avatar") != null){
                mData.avatarUrl = variables.getString("member_avatar");
            }
            if (space.getString("favthreads") != null){
                mData.collectionNum = space.getString("favthreads");
            }
            if (space.getString("threads") != null){
                mData.tieZiNum = space.getString("threads");
            }
            JSONObject group = space.getJSONObject("group");
            if (group.getString("grouptitle") != null){
                mData.groupTitle = group.getString("grouptitle");
            }
            Message msg = new Message();
            msg.what = REFRESH_VIEW;
            mHandler.sendMessage(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void refreshView() {
        Glide.with(mContext)
                .load(mData.avatarUrl)
                .centerCrop()
                .dontAnimate()
                .placeholder(R.drawable.default_icon)
                .error(R.drawable.default_icon)
                .into(avatar);
        name.setText(mData.username);
        name.setVisibility(View.VISIBLE);
        loginOrRegister.setVisibility(View.GONE);

        collectionNum.setText(mData.collectionNum);
        tieZiNum.setText(mData.tieZiNum);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_avatar_name:

                break;
            case R.id.ll_collection:
                Intent intent = new Intent(mContext, CollectionActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_tie_zi:
                Intent intent1 = new Intent(mContext, TieZiListActivity.class);
                intent1.putExtra(Constants.TIE_ZI_TITLE, "我的帖子");
                intent1.putExtra(Constants.TIE_ZI_TYPE, Constants.MY_TIE_ZI);
                startActivity(intent1);
                break;
            case R.id.ll_msg:

                break;
            case  R.id.tv_login_or_register:
                gotoLoginActivity();
                break;
        }
    }

    private void gotoLoginActivity() {
        Intent intent = new Intent(mContext, LoginActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //从loginActivity返回
        if (resultCode == 200){
            String uid = PreferencesUtils.getString(mContext, "member_uid");
            getPersonInfo(uid);
        }
    }
}
