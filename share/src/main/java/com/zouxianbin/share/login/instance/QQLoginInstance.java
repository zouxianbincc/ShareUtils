package com.zouxianbin.share.login.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;


import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.zouxianbin.share.ShareLogger;
import com.zouxianbin.share.ShareManager;
import com.zouxianbin.share.login.LoginListener;
import com.zouxianbin.share.login.LoginPlatform;
import com.zouxianbin.share.login.LoginResult;
import com.zouxianbin.share.login.result.BaseToken;
import com.zouxianbin.share.login.result.QQToken;
import com.zouxianbin.share.login.result.QQUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Observable;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by zouxainbin on 2016/12/1.
 */

public class QQLoginInstance extends LoginInstance {

    private static final String SCOPE = "get_simple_userinfo";

    private static final String URL = "https://graph.qq.com/user/get_user_info";

    private Tencent mTencent;

    private IUiListener mIUiListener;

    private LoginListener mLoginListener;

    public QQLoginInstance(Activity activity, final LoginListener listener,
                           final boolean fetchUserInfo) {
        super(activity, listener, fetchUserInfo);
        mTencent = Tencent.createInstance(ShareManager.CONFIG.getQqId(),
                activity.getApplicationContext());
        mLoginListener = listener;
        mIUiListener = new IUiListener() {
            @Override
            public void onComplete(Object o) {
                ShareLogger.i(ShareLogger.INFO.QQ_AUTH_SUCCESS);
                try {
                    QQToken token = QQToken.parse((JSONObject) o);
                    if (fetchUserInfo) {
                        listener.beforeFetchUserInfo(token);
                        fetchUserInfo(token);
                    } else {
                        listener.loginSuccess(new LoginResult(LoginPlatform.QQ, token));
                    }
                } catch (JSONException e) {
                    ShareLogger.i(ShareLogger.INFO.ILLEGAL_TOKEN);
                    mLoginListener.loginFailure(e);
                }
            }

            @Override
            public void onError(UiError uiError) {
                ShareLogger.i(ShareLogger.INFO.QQ_LOGIN_ERROR);
                listener.loginFailure(
                        new Exception("QQError: " + uiError.errorCode + uiError.errorDetail));
            }

            @Override
            public void onCancel() {
                ShareLogger.i(ShareLogger.INFO.AUTH_CANCEL);
                listener.loginCancel();
            }
        };
    }

    @Override
    public void doLogin(Activity activity, final LoginListener listener, boolean fetchUserInfo) {
        mTencent.login(activity, SCOPE, mIUiListener);
    }

    @Override
    public void fetchUserInfo(final BaseToken token) {

        io.reactivex.Observable.create(new ObservableOnSubscribe<QQUser>() {

            @Override
            public void subscribe(ObservableEmitter<QQUser> emitter) throws Exception {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(buildUserInfoUrl(token, URL)).build();

                try {
                    Response response = client.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    QQUser user = QQUser.parse(token.getOpenid(), jsonObject);
                    emitter.onNext(user);
                    emitter.onComplete();
                } catch (IOException | JSONException e) {
                    ShareLogger.e(ShareLogger.INFO.FETCH_USER_INOF_ERROR);
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<QQUser>() {
                    @Override
                    public void accept(QQUser qqUser) throws Exception {
                        mLoginListener.loginSuccess(
                                new LoginResult(LoginPlatform.QQ, token, qqUser));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mLoginListener.loginFailure(new Exception(throwable));
                    }
                });


    }

    private String buildUserInfoUrl(BaseToken token, String base) {
        return base
                + "?access_token="
                + token.getAccessToken()
                + "&oauth_consumer_key="
                + ShareManager.CONFIG.getQqId()
                + "&openid="
                + token.getOpenid();
    }

    @Override
    public void handleResult(int requestCode, int resultCode, Intent data) {
        Tencent.handleResultData(data, mIUiListener);
    }

    @Override
    public boolean isInstall(Context context) {
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return false;
        }

        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
        for (PackageInfo info : packageInfos) {
            if (TextUtils.equals(info.packageName.toLowerCase(), "com.tencent.mobileqq")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void recycle() {
        mTencent.releaseResource();
        mIUiListener = null;
        mLoginListener = null;
        mTencent = null;
    }
}
