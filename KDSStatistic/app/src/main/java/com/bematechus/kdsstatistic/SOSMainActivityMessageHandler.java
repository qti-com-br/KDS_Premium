package com.bematechus.kdsstatistic;

import android.os.Handler;
import android.os.Message;

/**
 * Created by Administrator on 2016/8/26.
 */
public class SOSMainActivityMessageHandler extends Handler {

    static final public int MSG_AUTO_REPORT = 1;

    public interface OnMainActivityMessage
    {
        public void onMessageTimeForCreateAutoReport();
    }

    private OnMainActivityMessage m_eventReceiver = null;

    public SOSMainActivityMessageHandler(OnMainActivityMessage receiver)
    {
        m_eventReceiver = receiver;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_AUTO_REPORT:
            {
                Object obj = msg.obj;
                m_eventReceiver.onMessageTimeForCreateAutoReport();
            }
            break;

            default:
                break;

        }
    }
}
