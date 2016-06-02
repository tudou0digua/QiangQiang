package com.cb.qiangqiang.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cb.qiangqiang.R;
import com.cb.qiangqiang.data.RequestManager;
import com.cb.qiangqiang.data.VolleyErrorHelper;
import com.cb.qiangqiang.model.HotPost;
import com.cb.qiangqiang.ui.activity.WebActivity;
import com.cb.qiangqiang.util.ImageUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.autolayout.utils.AutoUtils;

import org.apache.http.impl.cookie.BasicClientCookie2;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import cn.bingoogolapple.refreshlayout.BGARefreshViewHolder;
import de.hdodenhof.circleimageview.CircleImageView;

public class HotFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        BGARefreshLayout.BGARefreshLayoutDelegate{
    private static final int REFRESH_FINISHED = 0;
    private static final int REQUEST_TIEMOUT = 1;
    private static final int LOADING_MORE_FINISHED = 2;
    private static final String REFRESHING_DATA = "refreshing_data";
    private static final String LOADING_DATA_MORE = "loading_data_more";
    private static final int timeout = 5 * 1000;

    private int firstSeeItem;
    private int totalSeeItem;

    private Fragment mFragment;

    private int mCurrentPage = 1;
    private boolean isLoadingMore = false;
//    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BGARefreshLayout mRefreshLayout;
    private ListView mListView;
    private Context mContext;
    private List<HotPost> mData;
    private HotListAdapter mAdapter;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REFRESH_FINISHED:
                    List<HotPost> data = (List<HotPost>) msg.obj;
                    mAdapter.refreshData(data);
//                    mSwipeRefreshLayout.setRefreshing(false);
                    mRefreshLayout.endRefreshing();
                    Log.d("aaa", "finish refresh" + mListView.getFirstVisiblePosition() + " " + mListView.getLastVisiblePosition());
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startLoadImage(mListView.getFirstVisiblePosition(),
                                    mListView.getLastVisiblePosition() - mListView.getFirstVisiblePosition() + 1);
                        }
                    }, 300);
                    break;
                case REQUEST_TIEMOUT:
//                    mSwipeRefreshLayout.setRefreshing(false);
                    mRefreshLayout.endRefreshing();
                    mRefreshLayout.endLoadingMore();
                    break;
                case LOADING_MORE_FINISHED:
                    List<HotPost> dataMore = (List<HotPost>) msg.obj;
                    mRefreshLayout.endLoadingMore();
                    mAdapter.addData(dataMore);
                    break;
            }
        }
    };

    public HotFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.hot, container, false);
//        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mRefreshLayout = (BGARefreshLayout) view.findViewById(R.id.swipe_container);
        mListView = (ListView) view.findViewById(R.id.list_hot);
        initData();
        return view;
    }
    private void initData() {
        mFragment = this;
        mContext = getActivity();
        mData = new ArrayList<>();
        mAdapter = new HotListAdapter(mContext);
        mListView.setAdapter(mAdapter);
        initRefreshLayout();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, WebActivity.class);
                intent.putExtra("tid", mData.get(position).tid);
                intent.putExtra("pages", mData.get(position).pages);
                startActivity(intent);
            }
        });
       /* mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    startLoadImage(firstSeeItem, totalSeeItem);

                } else {
                    //TODO 取消图片下载
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.d("aaa", firstVisibleItem + " " + totalItemCount);
                firstSeeItem = firstVisibleItem;
                totalSeeItem = visibleItemCount;


                //                Log.d("TAG", firstVisibleItem + " " + visibleItemCount + " " + totalItemCount);
*//*                if (!mSwipeRefreshLayout.isRefreshing() && !isLoadingMore
                        && (firstVisibleItem + visibleItemCount == totalItemCount)) {
                    isLoadingMore = true;
                    String url = "http://www.qiangqiang5.com/api/mobile/index.php?charset=utf-8&version=3&mobile=no&" +
                            "page=" + (mCurrentPage + 1) + "&module=hotthread";
                    requestData(url, 1);
                }*//*

            }
        });*/
    }

    private void initRefreshLayout() {
        /*mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        }, 500);
        onRefresh();*/
        mRefreshLayout.setDelegate(this);
        BGARefreshViewHolder refreshViewHolder = new BGANormalRefreshViewHolder(mContext, true);
//        BGAStickinessRefreshViewHolder refreshViewHolder = new BGAStickinessRefreshViewHolder(mContext, true);
        mRefreshLayout.setRefreshViewHolder(refreshViewHolder);
        mRefreshLayout.beginRefreshing();
    }

    private void startLoadImage(int firstVisibleItem, int totalVisibaleItem) {
        for (int i = firstVisibleItem; i < firstVisibleItem + totalVisibaleItem; i++) {
            //避免滑动过快时，mData out of bound index
            if (mData.size() > i) {
                String url = formatIconUrl(mData.get(i).avatar);
                Log.d("aaa", "position" + i + " scroll Url : " + url);
                ImageUtils.loadImage(url, mListView, R.drawable.default_icon, R.drawable.default_icon);
            }
        }
    }

    @Override
    public void onRefresh() {
        if (!isLoadingMore){
            String url = "http://www.qiangqiang5.com/api/mobile/index.php?charset=utf-8&version=3&mobile=no&module=hotthread";
            requestData(url, 0);
        }
    }

    /**
     *
     * @param url 请求url
     * @param status 0：下拉刷新；1：上拉加载
     */
    private void requestData(String url, final int status) {
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                parseData(response);
                isLoadingMore = false;

                List<HotPost> temp = parseData(response);
                if(temp == null || temp.size() < 1){
                    Toast.makeText(mContext, "暂无更多数据!", Toast.LENGTH_SHORT).show();
                    mRefreshLayout.endLoadingMore();
                }else {
                    Message msg = new Message();
                    if(status == 0){
                        if (mData.size() > 0){
                            mData.clear();
                        }
                        mData = temp;
                        msg.what = REFRESH_FINISHED;
                        mCurrentPage = 1;
                    }else if (status == 1){
                        for (HotPost item : temp) {
                            mData.add(item);
                        }
                        msg.what = LOADING_MORE_FINISHED;
                        mCurrentPage++;
                    }
                    msg.obj = temp;
                    mHandler.sendMessage(msg);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext,
                        VolleyErrorHelper.getMessage(error, mContext),
                        Toast.LENGTH_SHORT)
                        .show();
                isLoadingMore = false;
                Message msg = new Message();
                msg.what = REQUEST_TIEMOUT;
                mHandler.sendMessage(msg);
            }
        });
//        setRequestCookie();
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(timeout, 1, 1.0f));
        String tag = "default";
        if(status == 0){
            tag = REFRESHING_DATA;
        }else if(status == 1){
            tag = LOADING_DATA_MORE;
        }

        RequestManager.addRequest(stringRequest,tag);
    }

    private void setRequestCookie() {
        SharedPreferences pref = getActivity().getSharedPreferences("login_data", Activity.MODE_PRIVATE);
        String cookiepre = pref.getString("cookiepre", " ");
        String auth = pref.getString("auth", " ");
        String saltkey = pref.getString("saltkey", " ");

        StringBuilder builder = new StringBuilder();
        builder.append(cookiepre);
        builder.append("saltkey=");
        builder.append(saltkey);
        builder.append(";");
        builder.append(cookiepre);
        builder.append("auth=");
        builder.append(auth);
        RequestManager.setCookie(new BasicClientCookie2("cookies", builder.toString()));
    }

    private List<HotPost> parseData(String strJson) {
        List<HotPost> hotData = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(strJson);
            JSONObject variables = root.getJSONObject("Variables");
            String data = variables.getString("data");
            if (data == null || data.length() <= 0 ){
                return hotData;
            }
            Type type = new TypeToken<ArrayList<HotPost>>(){}.getType();
            hotData = new Gson().fromJson(data, type);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return hotData;
    }

    private String formatIconUrl(String iconUrl){
        if(iconUrl.contains("avatar_big")){
            iconUrl = iconUrl.replace("avatar_big", "avatar_small");
        }else if(iconUrl.contains("avatar_middle")){
            iconUrl = iconUrl.replace("avatar_middle", "avatar_small");
        }
        return iconUrl;
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout bgaRefreshLayout) {
        String url = "http://www.qiangqiang5.com/api/mobile/index.php?charset=utf-8&version=3&mobile=no&module=hotthread";
        requestData(url, 0);
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout bgaRefreshLayout) {
        String url = "http://www.qiangqiang5.com/api/mobile/index.php?charset=utf-8&version=3&mobile=no&" +
                "page=" + (mCurrentPage + 1) + "&module=hotthread";
        requestData(url, 1);

        return true;
    }

    public class HotListAdapter extends BaseAdapter{
        private List<HotPost> data;
        private LayoutInflater mInflater;

        public HotListAdapter(Context context){
            mInflater = LayoutInflater.from(context);
            data = new ArrayList<>();
        }

        public void refreshData(List<HotPost> data){
            if(this.data.size() > 0){
                this.data.clear();
            }
            for (HotPost item : data) {
                this.data.add(item);
            }
            notifyDataSetChanged();
        }

        public void addData(List<HotPost> data){
            for (HotPost item : data) {
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
                convertView = mInflater.inflate(R.layout.hot_item, null);
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
            HotPost item = data.get(position);
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

            String iconUrl = item.avatar;
            iconUrl = formatIconUrl(iconUrl);

            Log.d("aaa", "position" + position + " list Url : " + iconUrl);

           /* Glide.with(mFragment)
                    .load(iconUrl)
                    .centerCrop()
                    .placeholder(R.drawable.default_icon)
                    .error(R.drawable.default_icon)
                    .crossFade()
                    .into(holder.icon);*/

//            holder.icon.setImageResource(R.drawable.default_icon);
//            if (holder.icon.getTag() == null){
//                holder.icon.setTag(iconUrl);
//                ImageUtils.loadImage(iconUrl, mListView, R.drawable.default_icon, R.drawable.default_icon);
//            }
//            holder.icon.setTag(iconUrl);

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
