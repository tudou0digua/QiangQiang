package com.cb.qiangqiang.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cb on 2016/1/8.
 */
public class LoginData {
    public String  cookiepre;
    public String  auth;
    public String  saltkey;
    @SerializedName("member_uid")
    public String  memberUid;
    @SerializedName("member_avatar")
    public String  memberAvatar;
    @SerializedName("member_username")
    public String  memberUserName;
    public String  groupid;
    public String  formhash;
    public String  readaccess;
}
