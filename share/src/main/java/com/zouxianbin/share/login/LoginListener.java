package com.zouxianbin.share.login;


import com.zouxianbin.share.login.result.BaseToken;

/**
 * Created by zouxainbin on 2016/12/2.
 */

public abstract class LoginListener {

    public abstract void loginSuccess(LoginResult result);

    public void beforeFetchUserInfo(BaseToken token) {
    }

    public abstract void loginFailure(Exception e);

    public abstract void loginCancel();
}
