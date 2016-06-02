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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.cb.qiangqiang.data.RequestManager;
import com.cb.qiangqiang.model.CollectionItem;
import com.cb.qiangqiang.ui.activity.TieZiDetailActivity;
import com.cb.qiangqiang.util.DateUtil;
import com.cb.qiangqiang.util.PreferencesUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maimengmami.waveswiperefreshlayout.WaveSwipeRefreshLayout;
import com.zhy.autolayout.AutoFrameLayout;
import com.zhy.autolayout.utils.AutoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class CollectionFragment extends Fragment implements
        View.OnClickListener, WaveSwipeRefreshLayout.OnRefreshListener{
    //常量-------------------
    private final int timeout = 5 * 1000;
    //UI---------------------
    private WaveSwipeRefreshLayout mSwipeLayout;
    private ListView mListView;
    private TextView mEdit;
    private TextView mDone;
    private AutoFrameLayout mLoadingCircleLayout;
    //Data-------------------
    private IDelColletion mDelColletion;
    private boolean canLoadMore = true;
    private boolean canRefresh = true;
    private List<CollectionItem> mDatas;
    private CollectionAdapter mAdapter;
    private Context mContext;
    private int mCurrentPage = 1;
    private Fragment mFragment;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Constants.REFRESH_DATA:
                    mAdapter.refreshData((List<CollectionItem>) msg.obj);
                    mSwipeLayout.setRefreshing(false);
                    break;
                case Constants.LOAD_MORE_DATA:
                    mAdapter.addData((List<CollectionItem>) msg.obj);
                    mSwipeLayout.setLoading(false);
                    break;
                case Constants.SHOW_LOADING_CIRCLE:
                    mLoadingCircleLayout.setVisibility(View.VISIBLE);
                    break;
                case Constants.HIDE_LOADING_CIRCLE:
                    mLoadingCircleLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };

    public CollectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        mContext = getActivity();
        mFragment = this;
        mDatas = new ArrayList<>();
        mDelColletion = new IDelColletion() {
            @Override
            public void removeDelCollectionItem(int position) {
                mDatas.remove(position);
            }
        };
        mAdapter = new CollectionAdapter(mContext, false, mHandler, mDelColletion);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);
        mSwipeLayout = (WaveSwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mListView = (ListView) view.findViewById(R.id.collection_listview);
        mEdit = (TextView) view.findViewById(R.id.tv_edit);
        mDone = (TextView) view.findViewById(R.id.tv_finish_edit);
        mLoadingCircleLayout = (AutoFrameLayout) view.findViewById(R.id.auto_fl_load);
        initView();
        return view;
    }

    private void initView() {
        mLoadingCircleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //什么都不做，避免删除收藏时，发生其他操作
            }
        });

        mEdit.setOnClickListener(this);
        mDone.setOnClickListener(this);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, TieZiDetailActivity.class);
                intent.putExtra("tid", mDatas.get(position).id);
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
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onRefresh();
            }
        }, 1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_edit:
                mEdit.setVisibility(View.GONE);
                mDone.setVisibility(View.VISIBLE);
                canRefresh = false;
                canLoadMore = false;
                mAdapter.setEditeStatus(true);
                break;
            case R.id.tv_finish_edit:
                mEdit.setVisibility(View.VISIBLE);
                mDone.setVisibility(View.GONE);
                canRefresh = true;
                canLoadMore = true;
                mAdapter.setEditeStatus(false);
                break;
        }
    }

    @Override
    public void onRefresh() {
        String url = getUrl(Constants.REFRESH_DATA);
        refreshData(url);
    }

    private void refreshData(String url) {
        StringRequest request = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        List<CollectionItem> temp = parseData(response);
                        if (temp == null || temp.size() < 1){
                            mSwipeLayout.setRefreshing(false);
                            return;
                        }
                        mDatas = temp;
                        mCurrentPage = 1;
                        Message msg = new Message();
                        msg.what = Constants.REFRESH_DATA;
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

    private String getUrl(int status) {
        String url = "";
        String charset = GlobalManager.getBaseParams("charset");
        String version = GlobalManager.getBaseParams("version");
        String mobile = GlobalManager.getBaseParams("mobile");
        String page = "1";
        String module = "myfavthread";
        url = Api.COLLECTION_LIST;
        if (status == Constants.REFRESH_DATA){
            page = "1";
        }else if (status == Constants.LOAD_MORE_DATA){
            page = String.valueOf(mCurrentPage + 1);
        }
        url = String.format(url, charset, version, mobile, page, module);
        return url;
    }

    @Override
    public void onLoad() {
        String url = getUrl(Constants.LOAD_MORE_DATA);
        loadMoreData(url);
    }

    private void loadMoreData(String url) {
        StringRequest request = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        List<CollectionItem> temp = parseData(response);
                        if (temp == null || temp.size() < 1){
                            mSwipeLayout.setLoading(false);
                            Toast.makeText(mContext, "亲，暂无更多数据 ￣□￣｜｜.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        for (CollectionItem item : temp) {
                            mDatas.add(item);
                        }
                        mCurrentPage++;
                        Message msg = new Message();
                        msg.what = Constants.LOAD_MORE_DATA;
                        msg.obj = temp;
                        mHandler.sendMessage(msg);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSwipeLayout.setLoading(false);
                    }
                });
        //设置请求cookie
//        GlobalManager.setRequestCookie(mContext);
        //设置超时时间
        request.setRetryPolicy(new DefaultRetryPolicy(timeout, 2, 1.0f));
        //请求数据
        RequestManager.addRequest(request, getActivity());
    }

    @Override
    public boolean canLoadMore() {
        return canLoadMore;
    }

    @Override
    public boolean canRefresh() {
        return canRefresh;
    }

    private List<CollectionItem> parseData(String strJson) {
        List<CollectionItem> datas = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(strJson);
            JSONObject variables = root.getJSONObject("Variables");
            String formhash = variables.getString("formhash");
            PreferencesUtils.putString(mContext, "formhash", formhash);
            String data = variables.getString("list");
            if (data == null || data.length() <= 0 ){
                return datas;
            }
            Type type = new TypeToken<ArrayList<CollectionItem>>(){}.getType();
            datas = new Gson().fromJson(data, type);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return datas;
    }

    public class CollectionAdapter extends BaseAdapter{
        private List<CollectionItem> data;
        private LayoutInflater mInflater;
        private Context mContext;
        private boolean isEidt;
        private Handler mHandler;
        private IDelColletion mDelColletion;

        public CollectionAdapter(Context context, boolean isEidt, Handler handler, IDelColletion delColletion){
            mInflater = LayoutInflater.from(context);
            data = new ArrayList<>();
            this.isEidt = isEidt;
            mContext = context;
            mHandler = handler;
            mDelColletion = delColletion;
        }

        public void refreshData(List<CollectionItem> data){
            if(this.data.size() > 0){
                this.data.clear();
            }
            for (CollectionItem item : data) {
                this.data.add(item);
            }
            notifyDataSetChanged();
        }

        public void addData(List<CollectionItem> data){
            for (CollectionItem item : data) {
                this.data.add(item);
            }
            notifyDataSetChanged();
        }

        public void setEditeStatus(boolean isEidt){
            this.isEidt = isEidt;
            notifyDataSetChanged();
        }

        public void deleteCollection(int position){
            data.remove(position);
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.collection_item, null);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.author = (TextView) convertView.findViewById(R.id.creator);
                holder.time = (TextView) convertView.findViewById(R.id.publish_time);
                holder.comments = (TextView) convertView.findViewById(R.id.comment_num);
                holder.cancel = (RelativeLayout) convertView.findViewById(R.id.rl_cancel);
                holder.cancelBtn = (Button) convertView.findViewById(R.id.btn_cancel);
                convertView.setTag(holder);
                AutoUtils.autoSize(convertView);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            final CollectionItem item = data.get(position);
            holder.content.setText(item.title);
            holder.author.setText(item.author);

            long collectionTime = Long.parseLong(item.dateline.trim()) * 1000;
            holder.time.setText(DateUtil.getPassedTime(collectionTime));

            holder.comments.setText(item.replies);

            if (isEidt){
                holder.cancel.setVisibility(View.VISIBLE);
            }else {
                holder.cancel.setVisibility(View.GONE);
            }

            holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = new Message();
                    msg.what = Constants.SHOW_LOADING_CIRCLE;
                    mHandler.sendMessage(msg);
                    sendDeleteRequest(position, item);
                }
            });

            return convertView;
        }

        private void sendDeleteRequest(final int position, final CollectionItem item) {
            String url = Api.DELETE_COLLECTION_TIE_ZI;
            url = String.format(url,
                    "spacecp",
                    "favorite",
                    "delete",
                    item.favid,
                    "all",
                    "1");
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            parseDeleteData(response, position);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(mContext, "亲，删除失败了哟！", Toast.LENGTH_SHORT).show();
                            Message msg = new Message();
                            msg.what = Constants.HIDE_LOADING_CIRCLE;
                            mHandler.sendMessage(msg);
                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("deletesubmit","true");
                    params.put("handlekey","a_delete_" + item.favid);
                    params.put("deletesubmitbtn","true");
                    params.put("formhash", PreferencesUtils.getString(mContext,"formhash"));
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

        private void parseDeleteData(String response, int position) {
            if (response.contains("成功")){
                deleteCollection(position);
                mDelColletion.removeDelCollectionItem(position);
            }else {
                Toast.makeText(mContext, "亲，删除失败了哟！", Toast.LENGTH_SHORT).show();
            }
            Message msg = new Message();
            msg.what = Constants.HIDE_LOADING_CIRCLE;
            mHandler.sendMessage(msg);
        }

        class ViewHolder{
            TextView content;
            TextView author;
            TextView time;
            TextView comments;
            RelativeLayout cancel;
            Button cancelBtn;
        }
    }

    public interface IDelColletion{
        void removeDelCollectionItem(int position);
    }

}
