package com.cb.qiangqiang.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cb.qiangqiang.R;
import com.cb.qiangqiang.data.Api;
import com.cb.qiangqiang.data.Constants;
import com.cb.qiangqiang.data.GlobalManager;
import com.cb.qiangqiang.data.VolleyRequest;
import com.cb.qiangqiang.model.BanKuai;
import com.cb.qiangqiang.model.BanKuaiItem;
import com.cb.qiangqiang.model.BanKuaiList;
import com.cb.qiangqiang.ui.activity.TieZiListActivity;
import com.cb.qiangqiang.util.PreferencesUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.autolayout.utils.AutoUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * A simple {@link Fragment} subclass.
 */
public class BanKuaiFragment extends Fragment {
    //常量----------------------------

    //UI------------------------------
    private ListView mListView;
    private MaterialProgressBar mProgressBar;

    //Data----------------------------
    private Context mContext;
    private BanKuaiAdapter mAdapter;
    private Map<String, BanKuaiItem> itemDataMap;
    private List<BanKuaiItem> itemDatas;
    private List<BanKuaiList> listDatas;
    private List<BanKuai> mDatas;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Constants.HIDE_LOADING_CIRCLE:
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case Constants.FINISH_LOAD_DATA:
                    mProgressBar.setVisibility(View.GONE);
                    mAdapter.addData(mDatas);
                    mListView.setAdapter(mAdapter);
                    break;
            }
        }
    };

    public BanKuaiFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        mContext = getActivity();
        itemDataMap = new HashMap<>();
        itemDatas = new ArrayList<>();
        listDatas = new ArrayList<>();
        mDatas = new ArrayList<>();
        mAdapter = new BanKuaiAdapter(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bankuai, container, false);
        mListView = (ListView) view.findViewById(R.id.list_view_bankuai);
        mProgressBar = (MaterialProgressBar) view.findViewById(R.id.progress_load);
        initView();
        init();
        return view;
    }

    private void initView() {
        initListView();
    }

    private void initListView() {

    }

    private void init(){
        mProgressBar.setVisibility(View.VISIBLE);
        String url = Api.BAN_KUAI;
        url = String.format(url,
                GlobalManager.getBaseParams("charset"),
                GlobalManager.getBaseParams("version"),
                GlobalManager.getBaseParams("mobile")
        );
        VolleyRequest.stringRequest(VolleyRequest.GET, url, Constants.TIMEOUT_SHORT, 2, mContext, null, mContext,
                new VolleyRequest.IVolleyStringResponse() {
                    @Override
                    public void onResponse(String response) {
                        parseData(response);
                        Message msg = new Message();
                        msg.what = Constants.FINISH_LOAD_DATA;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message msg = new Message();
                        msg.what = Constants.HIDE_LOADING_CIRCLE;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public Response<String> parseNetworkResponse(NetworkResponse response) {
                        return null;
                    }
                });
    }

    private void parseData(String response) {
        try {
            JSONObject root = new JSONObject(response);
            JSONObject variables = root.getJSONObject("Variables");


            String formhash = variables.getString("formhash");
            PreferencesUtils.putString(mContext, "formhash", formhash);

            JSONArray catlist = variables.getJSONArray("catlist");
            listDatas.clear();
            if (catlist != null){
                int catListSize = catlist.length();
                for (int i = 0; i < catListSize; i++){
                    BanKuaiList list = new BanKuaiList();
                    JSONObject object = (JSONObject) catlist.get(i);
                    list.fid = object.getString("fid");
                    list.name = object.getString("name");
                    JSONArray array = object.getJSONArray("forums");
                    for (int j = 0; j < array.length(); j++){
                        list.forums.add(array.get(j).toString());
                    }
                    listDatas.add(list);
                }
            }

            String forumList = variables.getString("forumlist");
            if (forumList != null){
                Type type = new TypeToken<ArrayList<BanKuaiItem>>(){}.getType();
                itemDatas = new Gson().fromJson(forumList, type);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (itemDatas.size() > 0 && listDatas.size() > 0){
            mDatas.clear();
            itemDataMap.clear();
            for (BanKuaiItem item : itemDatas) {
                itemDataMap.put(item.fid, item);
            }
            int size = listDatas.size();
            for (int i = 0; i < size; i++){
                BanKuaiList item =listDatas.get(i);
                int sizeFourms = item.forums.size();
                for (int j = 0; j < sizeFourms; j = j + 2){
                    BanKuai data = new BanKuai();
                    if (j == 0){
                        data.isFirst = true;
                    }else {
                        data.isFirst = false;
                    }
                    data.name = item.name;
                    BanKuaiItem left = itemDataMap.get(item.forums.get(j).trim());
                    if ( left != null){
                        data.left = left;
                    }
                    if (j + 1 < sizeFourms){
                        BanKuaiItem right = itemDataMap.get(item.forums.get(j + 1).trim());
                        if (right != null){
                            data.right = right;
                        }
                    }
                    mDatas.add(data);
                }
            }
        }

    }

    public class BanKuaiAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        private List<BanKuai> mDatas;

        public BanKuaiAdapter(Context context){
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mDatas = new ArrayList<>();
        }

        public void addData(List<BanKuai> data){
            if (mDatas.size() > 0){
                mDatas.clear();
            }
            for (BanKuai item : data) {
                mDatas.add(item);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null){
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.ban_kuai_item, null);
                holder.title = (TextView) convertView.findViewById(R.id.tv_title);
                holder.bkLeft = (TextView) convertView.findViewById(R.id.tv_left);
                holder.bkRight = (TextView) convertView.findViewById(R.id.tv_right);
                convertView.setTag(holder);
                AutoUtils.autoSize(convertView);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            BanKuai item = mDatas.get(position);
            if (item.isFirst){
                holder.title.setVisibility(View.VISIBLE);
                holder.title.setText(item.name);
            }else {
                holder.title.setVisibility(View.GONE);
            }

            holder.bkLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    jumpToTieZiList(position, true);
                }
            });
            holder.bkLeft.setText(item.left.name);
            holder.bkRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    jumpToTieZiList(position, false);
                }
            });
            if (item.right == null){
                holder.bkRight.setVisibility(View.INVISIBLE);
            }else {
                holder.bkRight.setVisibility(View.VISIBLE);
                holder.bkRight.setText(item.right.name);
            }
            return convertView;
        }

        private void jumpToTieZiList(int position, boolean isLeft) {
            String title;
            String fid;
            if (isLeft){
                title = mDatas.get(position).left.name;
                fid = mDatas.get(position).left.fid;
            }else {
                title = mDatas.get(position).right.name;
                fid = mDatas.get(position).right.fid;
            }
            Intent intent = new Intent(mContext, TieZiListActivity.class);
            intent.putExtra(Constants.TIE_ZI_TITLE, title);
            intent.putExtra(Constants.TIE_ZI_FID, fid);
            intent.putExtra(Constants.TIE_ZI_TYPE, Constants.BAN_KUAI);
            startActivity(intent);
        }

        public class ViewHolder{
            TextView title;
            TextView bkLeft;
            TextView bkRight;
        }

    }

}
