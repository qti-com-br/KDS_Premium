package com.bematechus.kdsrouter;

/**
 *  >>>>>>>  DIFFERENT WITH KDS APP FILE <<<<<<<
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * android os boot receiver.
 * We need to start app while os boot.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(context);
            boolean bEnabled = pre.getBoolean("general_router_enabled", false);
            if (!bEnabled) return;

            Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
            mainActivityIntent.setAction("android.intent.action.MAIN");
            mainActivityIntent.addCategory("android.intent.category.LAUNCHER");
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
        }
    }
}