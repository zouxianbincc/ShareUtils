package com.zouxianbin.share.login.result;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zouxainbin on 2016/12/3.
 */

public class QQToken extends BaseToken {

    public static QQToken parse(JSONObject jsonObject) throws JSONException {
        QQToken token = new QQToken();
        token.setAccessToken(jsonObject.getString("access_token"));
        token.setOpenid(jsonObject.getString("openid"));
        return token;
    }

}
