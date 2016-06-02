package com.cb.qiangqiang.ui.activity;

import android.os.Bundle;
import android.view.View;

import com.cb.qiangqiang.R;
import com.cb.qiangqiang.data.Constants;
import com.cb.qiangqiang.ui.fragment.TieZiListFragment;
import com.cb.qiangqiang.util.Utils;

import me.imid.swipebacklayout.lib.SwipeBackLayout;

public class TieZiListActivity extends BaseSwipeBackActivity {
    //UI---------------------

    //Data-------------------
    private TieZiListFragment mFragment;
    private String mTitle = "";
    private String mFid = "";
    private int mTieZiType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tie_zi_list);
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        View top = findViewById(R.id.top);
        Utils.initStatusAfterSetContentView(this, top);
        initView();
        initData();
    }

    private void initData() {
        if (getIntent().getStringExtra(Constants.TIE_ZI_TITLE) != null) mTitle = getIntent().getStringExtra(Constants.TIE_ZI_TITLE);
        if (getIntent().getStringExtra(Constants.TIE_ZI_FID) != null) mFid = getIntent().getStringExtra(Constants.TIE_ZI_FID);
        mTieZiType = getIntent().getIntExtra(Constants.TIE_ZI_TYPE, 0);
        mFragment = (TieZiListFragment) TieZiListFragment.newInstance(mTieZiType, mTitle, mFid);
        getSupportFragmentManager().beginTransaction().add(R.id.frame_container,mFragment, null).commit();
    }

    private void initView() {

    }
}
