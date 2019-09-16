package com.bematechus.kds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by David.Wong on 2019/9/16.
 * Rev:
 */
public class SysTimeChangedReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.TIME_SET";
    public interface sysTimeChangedEvent
    {
        public void onSysTimeChanged();
    }

    public SysTimeChangedReceiver()
    {

    }
    public SysTimeChangedReceiver(sysTimeChangedEvent receiver)
    {
        setReceiver(receiver);
    }

    sysTimeChangedEvent m_receiver = null;
    public void setReceiver(sysTimeChangedEvent rec)
    {
        m_receiver = rec;
    }
    @Override
    public void onReceive(Context context, Intent intent) {

        if (ACTION.equals(intent.getAction())) {
            if (m_receiver != null)
                m_receiver.onSysTimeChanged();

        }

    }

}
