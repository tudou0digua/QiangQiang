package com.cb.qiangqiang.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cb.greendao.dao.DaoSession;
import com.cb.greendao.dao.SearchHistory;
import com.cb.greendao.dao.SearchHistoryDao;
import com.cb.qiangqiang.R;
import com.cb.qiangqiang.data.Api;
import com.cb.qiangqiang.data.Constants;
import com.cb.qiangqiang.data.GlobalManager;
import com.cb.qiangqiang.data.VolleyRequest;
import com.cb.qiangqiang.model.SearchResult;
import com.cb.qiangqiang.util.PreferencesUtils;
import com.cb.qiangqiang.util.Utils;
import com.zhy.autolayout.AutoFrameLayout;
import com.zhy.autolayout.utils.AutoUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class SearchActivity extends BaseSwipeBackActivity {
    //常量-----------------------
    private static final int timeout = 5 * 1000;
    private static final int LOAD_HTML_DATA = 10;
    private static final int SHOW_SEARCH_RESULT = 11;
    private static final int SHOW_PROGRESS_LOAD_MORE = 12;
    private static final int HIDE_PROGRESS_LOAD_MORE = 13;
    //UI--------------------------
    private EditText mEditText;
    private Button mBtnSearch;
    private AutoFrameLayout mLoadingLayout;
    private ListView mListView;
    private ImageView mCancelView;
    private View listViewFooter;
    private TextView footerTextView;
    private MaterialProgressBar progressBarLoadMore;
    //Data------------------------
    private boolean isLoadMore = false;
    private int mCurrentPage = 1;
    private int mTotalNum;
    private String mKeyword = "";
    private DaoSession mDaoSession;
    private SearchHistoryDao mSearchHistoryDao;
    private IDeleteSearchHistory iDeleteSearchHistory;
    private List<SearchResult> mDatas;
    private SearchAdapter mSearchAdapter;
    private List<SearchHistory> mHistoryDatas;
    private HistoryAdapter mHistoryAdapter;
    private Context mContext;
    private int searchId;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Constants.HIDE_LOADING_CIRCLE:
                    mLoadingLayout.setVisibility(View.GONE);
                    break;
                case LOAD_HTML_DATA:

                    break;
                case SHOW_SEARCH_RESULT:
                    mLoadingLayout.setVisibility(View.GONE);
                    mSearchAdapter.addData(mDatas, mKeyword);
                    if (mListView.getFooterViewsCount() > 0){
                        mListView.removeFooterView(listViewFooter);
                    }
                    if (mTotalNum > 1){
                        mListView.addFooterView(listViewFooter);
                    }
                    mListView.setAdapter(mSearchAdapter);
                    break;
                case Constants.LOAD_MORE_DATA:
                    footerTextView.setVisibility(View.VISIBLE);
                    progressBarLoadMore.setVisibility(View.INVISIBLE);
                    mSearchAdapter.addMoreData((List<SearchResult>)msg.obj);
                    if (mCurrentPage == mTotalNum){
                        if (mListView.getFooterViewsCount() > 0){
                            mListView.removeFooterView(listViewFooter);
                        }
                    }
                    break;
                case SHOW_PROGRESS_LOAD_MORE:
                    footerTextView.setVisibility(View.INVISIBLE);
                    progressBarLoadMore.setVisibility(View.VISIBLE);
                    break;
                case HIDE_PROGRESS_LOAD_MORE:
                    footerTextView.setVisibility(View.VISIBLE);
                    progressBarLoadMore.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initData();
        initView();
        init();
    }

    private void init() {
        querySearchResult();
    }

    private void querySearchResult() {
        mHistoryDatas = mSearchHistoryDao.queryBuilder()
                .orderDesc(SearchHistoryDao.Properties.Date)
                .list();
        mHistoryAdapter.addData(mHistoryDatas);
        mListView.setAdapter(mHistoryAdapter);
    }

    private void initData() {
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        View top = findViewById(R.id.top);
        Utils.initStatusAfterSetContentView(this, top);
        mContext = this;
        mDatas = new ArrayList<>();
        mHistoryDatas = new ArrayList<>();
        mSearchAdapter = new SearchAdapter(mContext);

        initDB();

        iDeleteSearchHistory = new IDeleteSearchHistory() {
            @Override
            public void deleteSearchHistory(int position) {
                mSearchHistoryDao.deleteByKey(mHistoryDatas.get(position).getId());
                mHistoryDatas.remove(position);
            }
        };
        mHistoryAdapter = new HistoryAdapter(mContext, iDeleteSearchHistory);
    }

    private void initDB() {
        mDaoSession = GlobalManager.getDaoSession(mContext);
        mSearchHistoryDao = mDaoSession.getSearchHistoryDao();
    }

    private void initView() {
        listViewFooter = LayoutInflater.from(mContext).inflate(R.layout.search_result_foot, null);
        footerTextView = (TextView) listViewFooter.findViewById(R.id.tv_next_page);
        progressBarLoadMore = (MaterialProgressBar) listViewFooter.findViewById(R.id.progress_load_more);

        mEditText = (EditText) findViewById(R.id.et_search);
        mLoadingLayout = (AutoFrameLayout) findViewById(R.id.auto_fl_load);

        mBtnSearch = (Button) findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = mEditText.getText().toString().trim();
                requestSearch(keyword);
            }
        });

        mCancelView = (ImageView) findViewById(R.id.iv_cancel);
        mCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.setText("");
            }
        });

        mListView = (ListView) findViewById(R.id.list_view_search);

        initEditText();
        initListView();
    }

    private void initListView() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                BaseAdapter adapter = (BaseAdapter) parent.getAdapter();
                if (parent.getAdapter() instanceof HistoryAdapter) {
                    String keyword = mHistoryDatas.get(position).getContent();
                    mEditText.setText(keyword);
                    mEditText.setSelection(keyword.length());
                    requestSearch(keyword);
//                } else if (adapter instanceof SearchAdapter) {
                } else {
                    if (position == mDatas.size()) {
                        if (!isLoadMore){
                            loadNextPage();
                        }
                        return;
                    }
                    String url = mDatas.get(position).url;
                    if (url.contains("http://www.qiangqiang5.com/forum.php?mod=viewthread")) {
                        Intent intent = new Intent(mContext, TieZiDetailActivity.class);
                        String[] strs = url.split("&");
                        String tid = "";
                        for (String str : strs) {
                            if (str.contains("tid=")) {
                                tid = str.replace("tid=", "");
                                break;
                            }
                        }
                        intent.putExtra("tid", tid);
                        startActivity(intent);
                    }
                }
            }
        });
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utils.hideSystemSofeKeyboard(true, mContext, mEditText);
                return false;
            }
        });
    }

    private void initEditText() {
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = mEditText.getText().toString().trim();
                    requestSearch(keyword);
                }
                return false;
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                int length = str.length();
                if (length > 0) {
                    mCancelView.setVisibility(View.VISIBLE);
                } else {
                    mCancelView.setVisibility(View.INVISIBLE);
                    if (mListView.getFooterViewsCount() > 0) {
                        mListView.removeFooterView(listViewFooter);
                    }
                    querySearchResult();
                    mEditText.requestFocus();
                    Utils.hideSystemSofeKeyboard(false, mContext, mEditText);
                }
            }
        });
    }

    private void requestSearch(String keyword) {
        if (keyword.equals("")){
            Toast.makeText(mContext, "搜索内容不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        Utils.hideSystemSofeKeyboard(true, mContext, mEditText);
        mLoadingLayout.setVisibility(View.VISIBLE);

        mKeyword = keyword;
        mCurrentPage = 1;
        SearchHistory history = new SearchHistory(null, keyword, new Date(System.currentTimeMillis()));
        List<SearchHistory> temp = mSearchHistoryDao.queryBuilder()
                .where(SearchHistoryDao.Properties.Content.eq(keyword))
                .list();
        if (temp != null && temp.size() > 0){
            SearchHistory newHistory = temp.get(0);
            newHistory.setDate(new Date(System.currentTimeMillis()));
            mSearchHistoryDao.update(newHistory);
        }else {
            mSearchHistoryDao.insert(history);
        }

        String url = Api.SEARCH_TIE_ZI_PRE;
        url = String.format(url, "forum", "2");
        Map<String, String> params = new HashMap<>();
        params.put("formhash", PreferencesUtils.getString(mContext, "formhash"));
        params.put("srchtxt",keyword);
        params.put("searchsubmit", "yes");

        VolleyRequest.stringRequest(VolleyRequest.POST, url, timeout, 1, mContext, params, mContext,
                new VolleyRequest.IVolleyStringResponse() {
                    @Override
                    public void onResponse(String response) {
                        List<SearchResult> data = parseData(response);
                        Message msg = new Message();
                        if (data == null) {
                            msg.what = Constants.HIDE_LOADING_CIRCLE;
                        } else {
                            mDatas = data;
                            msg.what = SHOW_SEARCH_RESULT;
                        }
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message msg = new Message();
                        msg.what = Constants.HIDE_LOADING_CIRCLE;
                        mHandler.sendMessage(msg);
                        Toast.makeText(mContext, "请求搜索结果错误，请重试！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public Response<String> parseNetworkResponse(NetworkResponse response) {
                        return null;
                    }
                });
    }

    private void loadNextPage(){
        Message msg = new Message();
        msg.what = SHOW_PROGRESS_LOAD_MORE;
        mHandler.sendMessage(msg);

        isLoadMore = true;

        String url = Api.SEARCH_TIE_ZI_NEXT_PAGE;
        url = String.format(url, searchId, mCurrentPage + 1);
        VolleyRequest.stringRequest(VolleyRequest.GET, url, timeout, 1, mContext, null, mContext,
                new VolleyRequest.IVolleyStringResponse() {
                    @Override
                    public void onResponse(String response) {
                        isLoadMore = false;

                        List<SearchResult> data = parseLoadMoreData(response);
                        Message msg = new Message();

                        if (data == null || data.size() < 1){
                            msg.what = HIDE_PROGRESS_LOAD_MORE;
                        }else {
                            mCurrentPage++;
                            for (SearchResult item : data) {
                                mDatas.add(item);
                            }
                            msg.what = Constants.LOAD_MORE_DATA;
                            msg.obj = data;
                        }
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message msg = new Message();
                        msg.what = HIDE_PROGRESS_LOAD_MORE;
                        mHandler.sendMessage(msg);
                        isLoadMore = false;
                    }

                    @Override
                    public Response<String> parseNetworkResponse(NetworkResponse response) {
                        return null;
                    }
                });
    }

    private List<SearchResult> parseLoadMoreData(String response) {
        List<SearchResult> data = new ArrayList<>();
        Document document = Jsoup.parse(response);
        Element threadList = document.select("div.threadlist").first();
        if (threadList == null){
            return null;
        }
        Elements list = threadList.select("ul li a");
        int size = list.size();
        for (int i = 0; i < size; i++){
            SearchResult result = new SearchResult();
            Element item = list.get(i);
            String content = item.text();
            String url = item.attr("href");
            url = Api.DOMAIN + url;
            result.content = content;
            result.url = url;
            data.add(result);
        }
        return data;
    }

    private List<SearchResult> parseData(String response) {
        List<SearchResult> data = new ArrayList<>();
        Document document = Jsoup.parse(response);

        Element lessThanTenSeconds = document.select("div.jump_c").first();
        if (lessThanTenSeconds != null ){
            Element msg = lessThanTenSeconds.select("p").first();
            if (msg != null){
                Toast.makeText(mContext, msg.text(), Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(mContext, "抱歉，您在 10 秒内只能进行一次搜索！", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        Element threadList = document.select("div.threadlist").first();
        if (threadList == null){
            return null;
        }
        Element pg = threadList.select("div.pg").first();
        if (pg == null){
            mTotalNum = 1;
        }else {
            Element linkElement = pg.select("a").first();
            if (linkElement != null){
                String linkStr = linkElement.attr("href");
                String[] strs = linkStr.split("&");
                for (String str : strs){
                    if (str.contains("searchid=")){
                        searchId = Integer.valueOf(str.replace("searchid=", "").trim());
                        break;
                    }
                }
            }

            Element totalNum = pg.select("label span").first();
            if (totalNum!= null){
                String numStr = totalNum.attr("title");
                numStr = numStr.substring(numStr.indexOf("共") + "共".length(), numStr.indexOf("页")).trim();
                mTotalNum = Integer.valueOf(numStr);
            }else {
                mTotalNum = 1;
            }
        }
        Elements list = threadList.select("ul li a");
        int size = list.size();
        for (int i = 0; i < size; i++){
            SearchResult result = new SearchResult();
            Element item = list.get(i);
            String content = item.text();
            String url = item.attr("href");
            url = Api.DOMAIN + url;
            result.content = content;
            result.url = url;
            data.add(result);
        }
        return data;
    }

    public class HistoryAdapter extends BaseAdapter{
        private Context mContext;
        private List<SearchHistory> mDatas;
        private LayoutInflater mInflater;
        private IDeleteSearchHistory iDeleteHistory;

        public HistoryAdapter(Context context, IDeleteSearchHistory iDeleteHistory){
            mContext = context;
            this.iDeleteHistory = iDeleteHistory;
            mInflater = LayoutInflater.from(context);
            mDatas = new ArrayList<>();
        }

        public void addData(List<SearchHistory> data){
            if (mDatas.size() > 0){
                mDatas.clear();
            }
            for (SearchHistory item : data) {
                mDatas.add(item);
            }
            notifyDataSetChanged();
        }

        public void removeData(int position){
            mDatas.remove(position);
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
        public View getView(final int position, View convertView, final ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null){
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.search_history_item, null);
                holder.content = (TextView) convertView.findViewById(R.id.tv_content);
                holder.delete = (ImageView) convertView.findViewById(R.id.iv_delete_history);
                convertView.setTag(holder);
                AutoUtils.autoSize(convertView);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            SearchHistory item = mDatas.get(position);
            holder.content.setText(item.getContent());
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeData(position);
                    iDeleteHistory.deleteSearchHistory(position);
                }
            });
            return convertView;
        }

        class ViewHolder{
            TextView content;
            ImageView delete;
        }
    }

    public class SearchAdapter extends BaseAdapter{
        private LayoutInflater inflater;
        private Context mContext;
        private List<SearchResult> mDatas;
        private String keyword = "";

        public SearchAdapter(Context context){
            mContext = context;
            inflater = LayoutInflater.from(mContext);
            mDatas = new ArrayList<>();
        }

        public void addData(List<SearchResult> datas, String keyword){
            this.keyword = keyword;
            mDatas.clear();
            for (SearchResult item : datas) {
                mDatas.add(item);
            }
            notifyDataSetChanged();
        }

        public void addMoreData(List<SearchResult> datas){
            for (SearchResult item : datas) {
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null){
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.search_result_item, null);
                holder.content = (TextView) convertView.findViewById(R.id.tv_content);
                convertView.setTag(holder);
                AutoUtils.autoSize(convertView);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            SearchResult item = mDatas.get(position);
            String content = item.content;
            SpannableString span = new SpannableString(content);
            if (content.contains(keyword)){
                int start = content.indexOf(keyword);
                int end = start + keyword.length();
                span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red_dark)), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            holder.content.setText(span);
            return convertView;
        }
        class ViewHolder{
            TextView content;
        }
    }

    public interface IDeleteSearchHistory{
        void deleteSearchHistory(int position);
    }
}
