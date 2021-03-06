package com.cb.qiangqiang;

import android.app.Application;

import com.cb.qiangqiang.data.RequestManager;
import com.orhanobut.logger.AndroidLogTool;
import com.orhanobut.logger.Logger;

/**
 * Created by cb on 2016/1/1.
 */
public class ApplicationController extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        RequestManager.init(this);
        Logger
                .init("QiangQiang")
                .logTool(new AndroidLogTool());
    }
}
