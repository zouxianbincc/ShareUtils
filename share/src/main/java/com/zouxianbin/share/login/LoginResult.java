package com.zouxianbin.share.login;

import com.zouxianbin.share.login.result.BaseToken;
import com.zouxianbin.share.login.result.BaseUser;

/**
 * Created by zouxainbin on 2016/12/3.
 */

public class LoginResult {

    private BaseToken mToken;

    private BaseUser mUserInfo;

    private int mPlatform;

    public LoginResult(int platform, BaseToken token) {
        mPlatform = platform;
        mToken = token;
    }

    public LoginResult(int platform, BaseToken token, BaseUser userInfo) {
        mPlatform = platform;
        mToken = token;
        mUserInfo = userInfo;
    }

    public int getPlatform() {
        return mPlatform;
    }

    public void setPlatform(int platform) {
        this.mPlatform = platform;
    }

    public BaseToken getToken() {
        return mToken;
    }

    public void setToken(BaseToken token) {
        mToken = token;
    }

    public BaseUser getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(BaseUser userInfo) {
        mUserInfo = userInfo;
    }
}
