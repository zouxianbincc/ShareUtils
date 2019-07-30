package com.zouxianbin.share.login.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.zouxianbin.share.ShareLogger;
import com.zouxianbin.share.ShareManager;
import com.zouxianbin.share.login.LoginListener;
import com.zouxianbin.share.login.LoginPlatform;
import com.zouxianbin.share.login.LoginResult;
import com.zouxianbin.share.login.result.BaseToken;
import com.zouxianbin.share.login.result.WeiboToken;
import com.zouxianbin.share.login.result.WeiboUser;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import io.reactivex.Observable;
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

public class WeiboLoginInstance extends LoginInstance {

    private static final String USER_INFO = "https://api.weibo.com/2/users/show.json";

    private SsoHandler mSsoHandler;

    private LoginListener mLoginListener;

    public WeiboLoginInstance(Activity activity, LoginListener listener, boolean fetchUserInfo) {
        super(activity, listener, fetchUserInfo);
        AuthInfo authInfo = new AuthInfo(activity, ShareManager.CONFIG.getWeiboId(),
                ShareManager.CONFIG.getWeiboRedirectUrl(), ShareManager.CONFIG.getWeiboScope());
        mSsoHandler = new SsoHandler(activity, authInfo);
        mLoginListener = listener;
    }

    @Override
    public void doLogin(Activity activity, final LoginListener listener,
                        final boolean fetchUserInfo) {
        mSsoHandler.authorize(new WeiboAuthListener() {
            @Override
            public void onComplete(Bundle bundle) {
                Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(bundle);
                WeiboToken weiboToken = WeiboToken.parse(accessToken);
                if (fetchUserInfo) {
                    listener.beforeFetchUserInfo(weiboToken);
                    fetchUserInfo(weiboToken);
                } else {
                    listener.loginSuccess(new LoginResult(LoginPlatform.WEIBO, weiboToken));
                }
            }

            @Override
            public void onWeiboException(WeiboException e) {
                ShareLogger.i(ShareLogger.INFO.WEIBO_AUTH_ERROR);
                listener.loginFailure(e);
            }

            @Override
            public void onCancel() {
                ShareLogger.i(ShareLogger.INFO.AUTH_CANCEL);
                listener.loginCancel();
            }
        });
    }

    @Override
    public void fetchUserInfo(final BaseToken token) {

        Observable.create(new ObservableOnSubscribe<WeiboUser>() {

            @Override
            public void subscribe(ObservableEmitter<WeiboUser> emitter) throws Exception {
                OkHttpClient client = new OkHttpClient();
                Request request =
                        new Request.Builder().url(buildUserInfoUrl(token, USER_INFO)).build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    WeiboUser user = WeiboUser.parse(jsonObject);
                    emitter.onNext(user);
                    emitter.onComplete();
                } catch (IOException | JSONException e) {
                    ShareLogger.e(ShareLogger.INFO.FETCH_USER_INOF_ERROR);
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeiboUser>() {
                    @Override
                    public void accept(WeiboUser weiboUser) throws Exception {
                        mLoginListener.loginSuccess(
                                new LoginResult(LoginPlatform.WEIBO, token, weiboUser));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mLoginListener.loginFailure(new Exception(throwable));
                    }
                });


    }

    private String buildUserInfoUrl(BaseToken token, String baseUrl) {
        return baseUrl + "?access_token=" + token.getAccessToken() + "&uid=" + token.getOpenid();
    }

    @Override
    public void handleResult(int requestCode, int resultCode, Intent data) {
        mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
    }

    @Override
    public boolean isInstall(Context context) {
        IWeiboShareAPI shareAPI =
                WeiboShareSDK.createWeiboAPI(context, ShareManager.CONFIG.getWeiboId());
        return shareAPI.isWeiboAppInstalled();
    }

    @Override
    public void recycle() {
        mSsoHandler = null;
        mLoginListener = null;
    }
}
