package com.bematechus.kdsrouter;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by Administrator on 2016/9/20.
 */
public class KDSSmbExplorerHandler extends Handler {

    static final public int REFRESH_LIST = 1;
    public interface interfaceSmbGotAllFiles
    {
        public void onSmbGetAllFiles();
    }

    interfaceSmbGotAllFiles m_receiver = null;

    public KDSSmbExplorerHandler(interfaceSmbGotAllFiles receiver)
    {
        setReceiver(receiver);
    }

    public void setReceiver(interfaceSmbGotAllFiles receiver)
    {
        m_receiver = receiver;
    }
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case REFRESH_LIST: {
                if (m_receiver!= null)
                    m_receiver.onSmbGetAllFiles();

            }
            break;
            default:
                break;
        }
    }

    public void sendRefreshMessage()
    {
        Message m = new Message();
        m.what =  REFRESH_LIST;

        this.sendMessage(m);
    }
}
