package com.zouxianbin.share;

/**
 * Created by zouxainbin on 2016/12/5.
 */

public class ShareManager {

    private static boolean isInit = false;

    public static ShareConfig CONFIG;

    public static void init(ShareConfig config) {
        isInit = true;
        CONFIG = config;
    }
}
