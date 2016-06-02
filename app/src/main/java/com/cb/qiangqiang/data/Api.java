package com.cb.qiangqiang.data;

/**
 * Created by cb on 2016/1/14.
 */
public class Api {
    //域名
    public static final String DOMAIN =
            "http://www.qiangqiang5.com/";
    //客户端URL域名
    public static final String DOMAIN_CLIENT =
            "http://www.qiangqiang5.com/api/mobile/index.php?";
    //热帖列表
    public static final String HOT_LIST =
            "http://www.qiangqiang5.com/api/mobile/index.php?charset=%s&version=%s&mobile=%s&module=%s";
    //热帖列表-上拉更多
    public static final String HOT_LIST_MORE =
            "http://www.qiangqiang5.com/api/mobile/index.php?charset=%s&version=%s&mobile=%s&page=%s&module=%s";
    //帖子详情（网页）
    public static final String TIE_ZI_DETAIL =
            "http://www.qiangqiang5.com/api/mobile/index.php?module=%s&page=%s&charset=%s&image=%s&ppp=%s&debug=%s&tid=%s&mobile=%s&version=%s";
    //我的收藏列表
    public static final String COLLECTION_LIST =
            "http://www.qiangqiang5.com/api/mobile/index.php?charset=%s&version=%s&mobile=%s&page=%s&module=%s";
    //收藏帖子
    public static final String COLLECTION_TIE_ZI =
            "http://www.qiangqiang5.com/api/mobile/index.php?charset=%s&version=%s&mobile=%s&id=%s&module=%s&favoritesubmit=%s";
    //删除收藏的帖子（单帖）
    public static final String DELETE_COLLECTION_TIE_ZI =
            "http://www.qiangqiang5.com/home.php?mod=%s&ac=%s&op=%s&favid=%s&type=%s&inajax=%s";
    //签到（手机版网页）
    public static final String CHECK_IN =
            "http://www.qiangqiang5.com/plugin.php?id=%s";
    //搜索帖子之前的请求（得到searchid）
    public static final String SEARCH_TIE_ZI_PRE =
            "http://www.qiangqiang5.com/search.php?mod=%s&mobile=%s";
    //搜索帖子（带关键词）
    public static final String SEARCH_TIE_ZI =
            "http://www.qiangqiang5.com/search.php?mod=%s&searchid=%s&orderby=%s&ascdesc=%s&searchsubmit=%s&kw=%s&mobile=%s";
    //搜索帖子---下一页
    public static final String SEARCH_TIE_ZI_NEXT_PAGE =
            "http://www.qiangqiang5.com/search.php?mod=forum&searchid=%s&orderby=lastpost&ascdesc=desc&searchsubmit=yes&page=%s&mobile=2";
    //板块
    public static final String BAN_KUAI =
            "http://www.qiangqiang5.com/api/mobile/index.php?charset=%s&version=%s&mobile=%s&module=forumindex";
    //板块帖子列表
    public static final String TIE_ZI_LIST =
            "http://www.qiangqiang5.com/api/mobile/index.php?fid=%s&submodule=checkpost&charset=utf-8&version=3&mobile=no&tpp=10&module=forumdisplay";
    //板块帖子列表-上拉更多
    public static final String TIE_ZI_LIST_MORE =
            "http://www.qiangqiang5.com/api/mobile/index.php?fid=%s&submodule=checkpost&charset=utf-8&version=3&mobile=no&page=%s&tpp=10&module=forumdisplay";
    //个人中心
    public static final String PERSONAL =
            DOMAIN_CLIENT + "uid=%s&charset=utf-8&module=profile&mobile=no&version=3";
    //我的帖子列表
    public static final String MY_TIE_ZI =
            DOMAIN_CLIENT + "charset=utf-8&module=mythread&mobile=no&version=3";
    //我的帖子列表上拉
    public static final String MY_TIE_ZI_MORE =
            DOMAIN_CLIENT + "charset=utf-8&module=mythread&mobile=no&version=3&page=%s";
}
