package com.example.music_player.utils;

import android.app.Activity;
import android.os.Build;

public class ActivityUtils {

    /**
     * 判断Activity是否Destroy
     */
    public static boolean isDestroy(Activity mActivity) {
        if (mActivity== null || mActivity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mActivity.isDestroyed())) {
            return true;
        } else {
            return false;
        }
    }
}
