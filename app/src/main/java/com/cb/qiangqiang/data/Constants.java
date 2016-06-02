package com.cb.qiangqiang.data;

/**
 * Created by cb on 2016/1/14.
 */
public class Constants {
    public static final String TIE_ZI_TYPE = "teiZiType";
    public static final String TIE_ZI_TITLE = "tieZiTitle";
    public static final String TIE_ZI_FID = "tidZiFid";
    //帖子类型
    public static final int RE_TIE = 0;
    public static final int BAN_KUAI = 1;
    public static final int MY_TIE_ZI = 2;

    //常用---------------------------
    public static final int REFRESH_DATA = 0;
    public static final int LOAD_MORE_DATA = 1;
    public static final int NO_DATA = 2;
    public static final int SHOW_LOADING_CIRCLE = 3;
    public static final int HIDE_LOADING_CIRCLE = 4;
    public static final int SET_TITLE = 5;
    public static final int FINISH_LOAD_DATA = 6;

    //超时时间------------------------
    public static final int TIMEOUT_SHORT = 5 * 1000;
    public static final int TIMEOUT_MIDDLE = 10 * 1000;
    public static final int TIMEOUT_LONG = 15 * 1000;

    //登录页面------------------------
    public static final String LOGIN_SUCCESS = "login_succeed";//登录成功
    public static final String SUBMIT_SECODE_INVALID = "submit_seccode_invalid";//验证码错误
    public static final String LOGIN_INVALID = "login_invalid";//登录失败（账号、密码错误）

    //startActivityForResult Code
    public static final int SUCESS = 0;
    public static final int FAILED = -1;

    //收藏帖子-------------------------
    public static final String FAVORITE_DO_SUCESS = "favorite_do_success";
    public static final String TO_LOGIN = "to_login";//服务器返回为to_login//1
    public static final String FAVORITE_REPEAT = "favorite_repeat";
    public static final String SUBMIT_INVALID = "submit_invalid";

}
