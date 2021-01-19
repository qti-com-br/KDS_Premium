package com.bematechus.kds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bematechus.kdslib.ActivityLogin;

/**
 * android os boot receiver.
 * We need to start app while os boot.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            if (ActivityLogin.m_instance != null) return; //kpp1-434, this function will create a new MainActivity.
                                                            //This will cause ActivityLogin was overlap.
            Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
        }
    }
}