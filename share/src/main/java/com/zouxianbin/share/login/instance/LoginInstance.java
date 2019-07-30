package com.zouxianbin.share.login.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.zouxianbin.share.login.LoginListener;
import com.zouxianbin.share.login.result.BaseToken;

/**
 * Created by zouxainbin on 2016/12/1.
 */

public abstract class LoginInstance {

    public LoginInstance(Activity activity, LoginListener listener, boolean fetchUserInfo) {

    }

    public abstract void doLogin(Activity activity, LoginListener listener, boolean fetchUserInfo);

    public abstract void fetchUserInfo(BaseToken token);

    public abstract void handleResult(int requestCode, int resultCode, Intent data);

    public abstract boolean isInstall(Context context);

    public abstract void recycle();
}
