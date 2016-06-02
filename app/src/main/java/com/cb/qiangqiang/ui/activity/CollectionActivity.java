package com.cb.qiangqiang.ui.activity;

import android.os.Bundle;
import android.view.View;

import com.cb.qiangqiang.R;
import com.cb.qiangqiang.util.Utils;

import me.imid.swipebacklayout.lib.SwipeBackLayout;

public class CollectionActivity extends BaseSwipeBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        View top = findViewById(R.id.top);
        Utils.initStatusAfterSetContentView(this, top);
    }
}
