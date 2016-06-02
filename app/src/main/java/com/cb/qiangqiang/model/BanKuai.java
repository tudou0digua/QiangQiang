package com.cb.qiangqiang.model;

/**
 * Created by cb on 2016/2/3.
 */
public class BanKuai {
    public boolean isFirst = false;
    public BanKuaiItem left = null;
    public BanKuaiItem right = null;
    public String name = "";

    public BanKuai(){}

    public BanKuai(boolean isFirst, BanKuaiItem left, BanKuaiItem right) {
        this.isFirst = isFirst;
        this.left = left;
        this.right = right;
    }
}
