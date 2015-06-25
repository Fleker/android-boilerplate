package com.felkertech.n.weatherdelta.utils;

import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

/**
 * Created by N on 3/4/2015.
 */
public abstract class AppUtils {
    public static boolean isWearEnabled(Context mContext) {
        PackageManager pm = mContext.getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo("com.google.android.wearable.app", PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
    public static boolean isTV(Context mContext) {
        UiModeManager uiModeManager = (UiModeManager) mContext.getSystemService(mContext.UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }
}
