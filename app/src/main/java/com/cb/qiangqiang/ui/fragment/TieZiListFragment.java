package com.cb.qiangqiang.ui.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.cb.qiangqiang.R;
import com.cb.qiangqiang.data.Api;
import com.cb.qiangqiang.data.Constants;
import com.cb.qiangqiang.data.GlobalManager;
import com.cb.qiangqiang.data.RequestManager;
import com.cb.qiangqiang.model.TieZiItem;
import com.cb.qiangqiang.ui.activity.BrowserActivity;
import com.cb.qiangqiang.ui.activity.SearchActivity;
import com.cb.qiangqiang.ui.activity.TieZiDetailActivity;
import com.cb.qiangqiang.util.PreferencesUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maimengmami.waveswiperefreshlayout.WaveSwipeRefreshLayout;
import com.orhanobut.logger.Logger;
import com.zhy.autolayout.utils.AutoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 帖子列表
 */
public class TieZiListFragment extends Fragment implements WaveSwipeRefreshLayout.OnRefreshListener{
    //常量-------------------------
    private final String TAG = "TieZiListFragment";
    private final int timeout = 5 * 1000;
    private final int REFRESHING = 0;
    private final int LOADING_MORE = 1;
    private final int FINISH_REFRESH = 0;
    private final int FINISH_LOADING_MORE = 1;
    private final int REQUEST_TIMEOUT = 2;
    //Data-------------------------
    private String mTitle;
    private String fid;
    private int tieZiType = 0;//帖子类型：热帖，板块的帖子，我的帖子
    private int mCurrentPage = 1;
    private List<TieZiItem> mData;
    private TieZiListAdapter mAdapter;
    private Fragment mFragment;
    private Context mContext;
    private TieZiListHandler mHandler;
    //UI----------------------------
    private LinearLayout mNaviLayout;
    private TextView mTitleView;
    private RelativeLayout mSearchLayout;
    private TextView mCheckIn;
    private LinearLayout hotTopTitle;
    private WaveSwipeRefreshLayout mSwipeLayout;
    private ListView mListView;

    public TieZiListFragment() {

    }

    public static Fragment newInstance(int tieZiType){
        TieZiListFragment fragment = new TieZiListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.TIE_ZI_TYPE, tieZiType);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static Fragment newInstance(int tieZiType, String title, String fid){
        TieZiListFragment fragment = new TieZiListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.TIE_ZI_TYPE, tieZiType);
        bundle.putString(Constants.TIE_ZI_TITLE, title);
        bundle.putString(Constants.TIE_ZI_FID, fid);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            this.tieZiType = getArguments().getInt(Constants.TIE_ZI_TYPE);
            this.mTitle = getArguments().getString(Constants.TIE_ZI_TITLE);
            this.fid = getArguments().getString(Constants.TIE_ZI_FID);
        }
        initData();
    }

    private void initData() {
        mFragment = this;
        mContext = getActivity();
        mData = new ArrayList<>();
        mAdapter = new TieZiListAdapter(mContext);
        mHandler = new TieZiListHandler();
        mCurrentPage = 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tie_zi, container, false);
        mSwipeLayout = (WaveSwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mListView = (ListView) view.findViewById(R.id.tie_zi_list);
        hotTopTitle = (LinearLayout) view.findViewById(R.id.hot_top_title);
        mCheckIn = (TextView) view.findViewById(R.id.tv_check_in);
        mSearchLayout = (RelativeLayout) view.findViewById(R.id.rl_search);
        mNaviLayout = (LinearLayout) view.findViewById(R.id.ll_navi);
        mTitleView = (TextView) view.findViewById(R.id.tv_title);
        initView();
        return view;
    }

    private void initView() {
        //初始化搜索栏等
        if (tieZiType == Constants.RE_TIE){
            mNaviLayout.setVisibility(View.GONE);
            hotTopTitle.setVisibility(View.VISIBLE);
            mCheckIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, BrowserActivity.class);
                    String url = Api.CHECK_IN;
                    url = String.format(url, "dsu_paulsign:sign");
                    intent.putExtra("url", url);
                    startActivity(intent);
                }
            });
            mSearchLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, SearchActivity.class);
                    startActivity(intent);
                }
            });
        }else if (tieZiType == Constants.BAN_KUAI || tieZiType == Constants.MY_TIE_ZI){
            hotTopTitle.setVisibility(View.GONE);
            mNaviLayout.setVisibility(View.VISIBLE);
            mTitleView.setText(mTitle);
        }
        //初始化ListView
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, TieZiDetailActivity.class);
                intent.putExtra("tid", mData.get(position).tid);
                startActivity(intent);
            }
        });
        //初始化下拉刷新和上拉加载布局
        int homepage_refresh_spacing = 20;
        mSwipeLayout.setProgressViewOffset(false, -homepage_refresh_spacing * 2, homepage_refresh_spacing);
        mSwipeLayout.setColorSchemeColors(getResources().getColor(R.color.top_title_bg));
        mSwipeLayout.setOnRefreshListener(this);
        //进入页面时，刷新数据
        mSwipeLayout.setRefreshing(true);
        onRefresh();
    }

    //WaveSwipeRefreshLayout.OnRefreshListener
    @Override
    public void onRefresh() {
        String url = getUrl(REFRESHING);
        refreshData(url);
    }

    private void refreshData(String url) {
        StringRequest request = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Logger.json(response);
                        List<TieZiItem> temp = parseData(response);
                        mData = temp;
                        mCurrentPage = 1;
                        Message msg = new Message();
                        msg.what = FINISH_REFRESH;
                        msg.obj = temp;
                        mHandler.sendMessage(msg);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSwipeLayout.setRefreshing(false);
                    }
        });
        //设置请求cookie
        GlobalManager.setRequestCookie(mContext);
        //设置超时时间
        request.setRetryPolicy(new DefaultRetryPolicy(timeout, 2, 1.0f));
        //请求数据
        RequestManager.addRequest(request, getActivity());
    }

    @Override
    public void onLoad() {
        String url = getUrl(LOADING_MORE);
        loadMoreData(url);
    }

    private String getUrl(int status) {
        String url = "";
        String charset = GlobalManager.getBaseParams("charset");
        String version = GlobalManager.getBaseParams("version");
        String mobile = GlobalManager.getBaseParams("mobile");
        String page = String.valueOf(mCurrentPage + 1);
        String module = "hotthread";
        if (status == REFRESHING){
            if (tieZiType == Constants.RE_TIE){
                url = Api.HOT_LIST;
                url = String.format(url, charset, version, mobile, module);
            }else if (tieZiType == Constants.BAN_KUAI){
                url = Api.TIE_ZI_LIST;
                url = String.format(url,fid);
            }else if (tieZiType == Constants.MY_TIE_ZI){
                url = Api.MY_TIE_ZI;
            }

        }else if (status == LOADING_MORE){
            if (tieZiType == Constants.RE_TIE){
                url = Api.HOT_LIST_MORE;
                url = String.format(url, charset, version, mobile, page, module);
            }else if (tieZiType == Constants.BAN_KUAI){
                url = Api.TIE_ZI_LIST_MORE;
                url = String.format(url,fid,page);
            }else if (tieZiType == Constants.MY_TIE_ZI){
                url = Api.MY_TIE_ZI_MORE;
                url = String.format(url,page);
            }
        }
        return url;
    }

    private void loadMoreData(String url) {
        Log.d(TAG, url);
        StringRequest request = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        List<TieZiItem> temp = parseData(response);
                        if (temp == null || temp.size() < 1){
                            mSwipeLayout.setLoading(false);
                            Toast.makeText(mContext, "亲，到底了 ￣□￣｜｜.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        for (TieZiItem item : temp) {
                            mData.add(item);
                        }
                        mCurrentPage++;
                        Message msg = new Message();
                        msg.what = FINISH_LOADING_MORE;
                        msg.obj = temp;
                        mHandler.sendMessage(msg);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                        mSwipeLayout.setLoading(false);
                    }
                });

        //设置超时时间
        request.setRetryPolicy(new DefaultRetryPolicy(timeout, 2, 1.0f));
        //请求数据
        RequestManager.addRequest(request, getActivity());
    }

    @Override
    public boolean canLoadMore() {
        return true;
    }
    @Override
    public boolean canRefresh() {
        return true;
    }

    private List<TieZiItem> parseData(String strJson) {
        List<TieZiItem> datas = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(strJson);
            JSONObject variables = root.getJSONObject("Variables");
            String formhash = variables.getString("formhash");
            PreferencesUtils.putString(mContext, "formhash", formhash);
            if (variables.getString("member_uid") != null){
                String uid = variables.getString("member_uid");
                PreferencesUtils.putString(mContext, "member_uid", uid);
            }
            String data = "";
            if (tieZiType == Constants.RE_TIE || tieZiType == Constants.MY_TIE_ZI){
                data = variables.getString("data");
            }else if (tieZiType == Constants.BAN_KUAI){
                data = variables.getString("forum_threadlist");
            }
            if (data == null || data.length() <= 0 ){
                return datas;
            }
            Type type = new TypeToken<ArrayList<TieZiItem>>(){}.getType();
            datas = new Gson().fromJson(data, type);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return datas;
    }

    private String formatIconUrl(String iconUrl){
        if(iconUrl.contains("avatar_big")){
            iconUrl = iconUrl.replace("avatar_big", "avatar_small");
        }else if(iconUrl.contains("avatar_middle")){
            iconUrl = iconUrl.replace("avatar_middle", "avatar_small");
        }
        return iconUrl;
    }

    public class TieZiListHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case FINISH_REFRESH:
                    mSwipeLayout.setRefreshing(false);
                    mAdapter.refreshData((List<TieZiItem>) msg.obj);
                    break;
                case FINISH_LOADING_MORE:
                    mAdapter.addData((List<TieZiItem>) msg.obj);
                    mSwipeLayout.setLoading(false);
                    break;


            }
        }
    }

    public class TieZiListAdapter extends BaseAdapter {
        private List<TieZiItem> data;
        private LayoutInflater mInflater;

        public TieZiListAdapter(Context context){
            mInflater = LayoutInflater.from(context);
            data = new ArrayList<>();
        }

        public void refreshData(List<TieZiItem> data){
            if(this.data.size() > 0){
                this.data.clear();
            }
            for (TieZiItem item : data) {
                this.data.add(item);
            }
            notifyDataSetChanged();
        }

        public void addData(List<TieZiItem> data){
            for (TieZiItem item : data) {
                this.data.add(item);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.tiez_zi_item, null);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.icon = (CircleImageView) convertView.findViewById(R.id.icon_publish);
                holder.author = (TextView) convertView.findViewById(R.id.name_publish);
                holder.time = (TextView) convertView.findViewById(R.id.time_last_reply);
                holder.hasPic = (ImageView) convertView.findViewById(R.id.has_pic);
                holder.comments = (TextView) convertView.findViewById(R.id.comment_num);
                holder.views = (TextView) convertView.findViewById(R.id.view_num);
                convertView.setTag(holder);
                AutoUtils.autoSize(convertView);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            TieZiItem item = data.get(position);
            holder.content.setText(item.subject);
            holder.author.setText(item.author);
            String timeLastReply = item.lastpost.replace("&nbsp;","");
            holder.time.setText(timeLastReply);
            if (item.attachment.equals("2")){
                holder.hasPic.setVisibility(View.VISIBLE);
            }else{
                holder.hasPic.setVisibility(View.INVISIBLE);
            }
            holder.views.setText(item.views);
            holder.comments.setText(item.replies);

            if (tieZiType == Constants.BAN_KUAI || tieZiType == Constants.MY_TIE_ZI){
                holder.icon.setVisibility(View.GONE);
            }else {
                String iconUrl = item.avatar;
                iconUrl = formatIconUrl(iconUrl);
                //            Log.d("aaa", "position" + position + " list Url : " + iconUrl);
                Glide.with(mFragment)
                        .load(iconUrl)
                        .centerCrop()
                        .dontAnimate()
                        .placeholder(R.drawable.default_icon)
                        .error(R.drawable.default_icon)
                        .into(holder.icon);
            }

            return convertView;
        }

        class ViewHolder{
            TextView content;
            TextView author;
            TextView time;
            CircleImageView icon;
            ImageView hasPic;
            TextView views;
            TextView comments;
        }
    }
}
