package com.bematechus.kdslib;

import android.os.Bundle;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 *
 * While write data to socket, we will save data to this buffer first.
 * Then, send them in "OnWrite" socket event.
 */
public class KDSSocketFIFOBuffer {

    private Object m_locker = new Object();
    ArrayList<Bundle> m_lstData = new ArrayList<Bundle>();

    public KDSSocketFIFOBuffer()
    {
        this.clear();
    }

    public void add(ByteBuffer buf)
    {
        synchronized (m_locker)
        {
            Bundle b = new Bundle();
            b.putByteArray("data", buf.duplicate().array());
            m_lstData.add(b);
        }
    }
    public void add(String strDestIP, ByteBuffer buf)
    {
        synchronized (m_locker)
        {
            Bundle b = new Bundle();
            b.putByteArray("data", buf.duplicate().array());
            b.putString("ip", strDestIP);
            m_lstData.add(b);
        }
    }
    public ByteBuffer popup()
    {
        synchronized (m_locker)
        {
            if (m_lstData.size() <=0)
                return null;
            Bundle b = m_lstData.get(0);
            m_lstData.remove(0);
            return ByteBuffer.wrap(b.getByteArray("data"));
        }
    }

    public Bundle popupBundle()
    {
        synchronized (m_locker)
        {
            if (m_lstData.size() <=0)
                return null;
            Bundle b = m_lstData.get(0);
            m_lstData.remove(0);
            return b;

        }
    }

    public int count()
    {
        synchronized (m_locker)
        {
           return m_lstData.size();
        }
    }
    public void clear()
    {
        synchronized (m_locker)
        {
            m_lstData.clear();
        }
    }


}
