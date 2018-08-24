package com.bematechus.kds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;

/**
 *
 */
public class ApkActionsReceiver extends BroadcastReceiver {
    static final String TAG = "ApkActionsReceiver";
    static final String ACTION_ADDED = "android.intent.action.PACKAGE_ADDED";
    static final String ACTION_REMOVED = "android.intent.action.PACKAGE_REMOVED";
    static final String ACTION_REPLACED = "android.intent.action.PACKAGE_REPLACED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_REPLACED)) {
            if (intent != null && intent.getData() != null && context.getPackageName().equals(intent.getData().getSchemeSpecificPart()))
            {
                KDSLog.d(TAG, KDSLog._FUNCLINE_()+"My app was updated");
                Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mainActivityIntent);
            }

        }
    }
}
