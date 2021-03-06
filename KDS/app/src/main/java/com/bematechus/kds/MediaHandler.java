package com.bematechus.kds;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Administrator on 2016/9/12.
 */
public class MediaHandler extends Handler {
    static final public int EVENT_UPDATE_VIDEO_PROGRESS = 1;
    static final public int EVENT_SMB_DOWNLOADED = 2;
    static final public int EVENT_HTTP_BITMAP_DOWNLOADED = 3;

    public interface MediaEventReceiver
    {

        public void medaievent_onSmbFileDownloaded(String localFileName);
        public void medaievent_onHttpBitmapFileDownloaded(Bitmap bmp);

    }
    MediaEventReceiver m_receiver = null;
    public  MediaHandler(MediaEventReceiver receiver)
    {
        m_receiver = receiver;
    }

    public void setReceiver(MediaEventReceiver r)
    {
        m_receiver = r;
    }

    public void sendSmbDownloadedMessage(String localFileName)
    {
        Message m = new Message();
        m.what =  EVENT_SMB_DOWNLOADED;
        m.obj = localFileName;

        this.sendMessage(m);
    }
    public void sendHttpBitmapDownloadedMessage(Bitmap bmp)
    {
        Message m = new Message();
        m.what =  EVENT_HTTP_BITMAP_DOWNLOADED;
        m.obj = bmp;
        this.sendMessage(m);
    }
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_UPDATE_VIDEO_PROGRESS: {
                //Object obj = msg.obj;
               // m_receiver.mediaevent_onUpdateVideoProgress();
            }
            break;
            case EVENT_SMB_DOWNLOADED: {
                Object obj = msg.obj;
                if (m_receiver!= null)
                    m_receiver.medaievent_onSmbFileDownloaded((String)obj);
            }
            break;
            case EVENT_HTTP_BITMAP_DOWNLOADED:
            {
                Object obj = msg.obj;
                if (obj != null) {
                    if (m_receiver != null)
                        m_receiver.medaievent_onHttpBitmapFileDownloaded((Bitmap) obj);
                }
            }
            break;
        }
    }

}
