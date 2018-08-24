package com.bematechus.kdslib;

import android.app.Instrumentation;
import android.os.AsyncTask;
import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

/**
 * for bump bar keyboard event.
 */
public class KDSKbdRecorder {

    private static final String TAG = "KDSKbdRecorder";
    ArrayList<Integer> m_arKeyDown = new ArrayList<>();

    long m_lLastKeyupTime = 0;
    boolean m_bWaitForAllKeysUp = false;
    public boolean isDown(int nKeycode)
    {

        if (m_arKeyDown.size() <=0)
            return false;
        for (int i=0; i< m_arKeyDown.size(); i++)
        {
            if (m_arKeyDown.get(i) == nKeycode)
                return true;
        }
        return false;

    }

    public  boolean isAnyKeyDown()
    {
        return (m_arKeyDown.size()>0);
    }
    public void reset()
    {

        this.clear();

    }
    public void clear()
    {
        m_arKeyDown.clear();
        setWaitAllKeysUp(false);
    }
    static final int MAX_KEYS = 2;
    public void onKeyDown(int nKeycode)
    {
        m_arKeyDown.add(nKeycode);
        if (m_arKeyDown.size() >MAX_KEYS)
        {
           int n = m_arKeyDown.size() - MAX_KEYS;
            for (int i=0; i< n; i++)
                m_arKeyDown.remove(0);
        }

    }

    public void onKeyUp(int nKeycode)
    {
        m_lLastKeyupTime = System.currentTimeMillis();

        int ncount = m_arKeyDown.size();
        for (int i=ncount-1; i>=0; i--)
        {
            if (m_arKeyDown.get(i) == nKeycode)
                m_arKeyDown.remove(i);

        }

        if (isAllUp())
            m_bWaitForAllKeysUp = false;

    }

    public void setWaitAllKeysUp(boolean bWait)
    {
        m_bWaitForAllKeysUp = bWait;
    }
    private boolean isAllUp()
    {
        return (m_arKeyDown.size() == 0);

    }


    public void debug(String preText)
    {

        for (int i=0; i< m_arKeyDown.size(); i++)
        {
            KDSLog.d(TAG,KDSLog._FUNCLINE_()+preText+" down:"+m_arKeyDown.get(i) );

        }
    }

    static public  void convertKeyEvent(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT ||
                keyCode == KeyEvent.KEYCODE_CTRL_RIGHT)
        {

            KDSKbdRecorder.sendKbdEvent(KeyEvent.KEYCODE_ESCAPE);
            KDSKbdRecorder.sendKbdEvent(KeyEvent.KEYCODE_BACK);
        }

        if (keyCode == KeyEvent.KEYCODE_NUMPAD_SUBTRACT)
        {
            KDSKbdRecorder.sendKbdEvent(KeyEvent.KEYCODE_DPAD_UP);
        }
    }


    static public void sendKbdEvent(int nKeycode)
    {
        Object objs[] = new Object[]{nKeycode};

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    int keycode =(int) objects[0];
                    Instrumentation inst=new Instrumentation();
                    inst.sendCharacterSync(keycode);

                } catch(Exception e) {
                    KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
                    //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
                }


                return null;

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, objs);


    }





}
