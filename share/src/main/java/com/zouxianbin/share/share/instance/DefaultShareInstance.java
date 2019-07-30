package com.zouxianbin.share.share.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.zouxianbin.share.R;
import com.zouxianbin.share.share.ImageDecoder;
import com.zouxianbin.share.share.ShareImageObject;
import com.zouxianbin.share.share.ShareListener;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by zouxainbin on 2016/11/18.
 */

public class DefaultShareInstance implements ShareInstance {

    @Override
    public void shareText(int platform, String text, Activity activity, ShareListener listener) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent,
                activity.getResources().getString(R.string.vista_share_title)));
    }

    @Override
    public void shareMedia(int platform, String title, String targetUrl, String summary,
                           ShareImageObject shareImageObject, Activity activity, ShareListener listener) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format("%s %s", title, targetUrl));
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent,
                activity.getResources().getString(R.string.vista_share_title)));
    }

    @Override
    public void shareImage(int platform, final ShareImageObject shareImageObject,
                           final Activity activity, final ShareListener listener) {

        Observable.create(new ObservableOnSubscribe<Uri>() {

            @Override
            public void subscribe(ObservableEmitter<Uri> emitter) throws Exception {
                try {
                    Uri uri =
                            Uri.fromFile(new File(ImageDecoder.decode(activity, shareImageObject)));
                    emitter.onNext(uri);
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        listener.shareRequest();
                    }
                }).subscribe(new Consumer<Uri>() {
            @Override
            public void accept(Uri uri) throws Exception {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("image/jpeg");
                activity.startActivity(Intent.createChooser(shareIntent,
                        activity.getResources().getText(R.string.vista_share_title)));
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                listener.shareFailure(new Exception(throwable));
            }
        });


    }

    @Override
    public void handleResult(Intent data) {
        // Default share, do nothing
    }

    @Override
    public boolean isInstall(Context context) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        return context.getPackageManager()
                .resolveActivity(shareIntent, PackageManager.MATCH_DEFAULT_ONLY) != null;
    }

    @Override
    public void recycle() {

    }
}
