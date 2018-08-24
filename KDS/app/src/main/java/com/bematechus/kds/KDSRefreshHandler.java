package com.bematechus.kds;

import android.os.Message;

import android.os.Handler;

/**
 * Don't draw anything in thread, this will get thread message then do drawing work.
 */
public class KDSRefreshHandler extends Handler {
    public interface KDSRefreshEventReceiver {

        public void threadrefresh_FocusFirst();
    }
    static final public int KDS_MSG_FOCUS_FIRST = 1;


    KDSRefreshEventReceiver m_receiver = null;

    public KDSRefreshHandler(KDSRefreshEventReceiver receiver)
    {
        m_receiver = receiver;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case KDS_MSG_FOCUS_FIRST: {
                m_receiver.threadrefresh_FocusFirst();
            }
            break;
        }
    }

    public void sendFocusFirstMessage()
    {
        Message m = new Message();
        m.what =  KDS_MSG_FOCUS_FIRST;

        this.sendMessage(m);
    }

}
