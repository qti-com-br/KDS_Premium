package com.bematechus.kdslib;

import android.os.Bundle;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 *
 * While write data to socket, we will save data to this buffer first.
 * Then, send them in "OnWrite" socket event.
 */
public class KDSSocketFIFOBuffer {

    private Object m_locker = new Object();
    //ArrayList<Bundle> m_lstData = new ArrayList<Bundle>();
    ArrayList<BufferData> m_lstData = new ArrayList<>();

    public KDSSocketFIFOBuffer()
    {
        this.clear();
    }

    public void add(ByteBuffer buf)
    {
        synchronized (m_locker)
        {
//            Bundle b = new Bundle();
//            b.putByteArray("data", buf.duplicate().array());
//            m_lstData.add(b);
            BufferData b = new BufferData();
            b.bytes = buf.duplicate().array();
            m_lstData.add(b);

        }
    }
    public void add(String strDestIP, ByteBuffer buf)
    {
        synchronized (m_locker)
        {
//            Bundle b = new Bundle();
//            b.putByteArray("data", buf.duplicate().array());
//            b.putString("ip", strDestIP);
//            m_lstData.add(b);
            BufferData b = new BufferData();
            b.bytes = buf.duplicate().array();
            b.ip = strDestIP;
            m_lstData.add(b);
        }
    }
    public ByteBuffer popup()
    {
        synchronized (m_locker)
        {
            if (m_lstData.size() <=0)
                return null;
//            Bundle b = m_lstData.get(0);
//            m_lstData.remove(0);
//            return ByteBuffer.wrap(b.getByteArray("data"));
            BufferData b = m_lstData.get(0);
            m_lstData.remove(0);
            ByteBuffer buf = ByteBuffer.wrap(b.bytes);
            b.clear();
            return buf;
        }
    }

    public BufferData popupBundle()
    {
        synchronized (m_locker)
        {
            if (m_lstData.size() <=0)
                return null;
//            Bundle b = m_lstData.get(0);
            BufferData b = m_lstData.get(0);
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

    static class BufferData
    {
        public byte bytes[] = new byte[0];
        //public ByteBuffer bytes = null;
        public String ip = "";
        public void clear()
        {
            bytes = null;
            ip = "";
        }
    }

    final int OneKB = 1024;
    public ArrayList<ByteBuffer> popup1KB()
    {
        synchronized (m_locker)
        {
            ArrayList<ByteBuffer> ar = new ArrayList<>();

            if (m_lstData.size() <=0)
                return ar;
//            Bundle b = m_lstData.get(0);
//            m_lstData.remove(0);
//            return ByteBuffer.wrap(b.getByteArray("data"));
            int nSize = 0;
            while (nSize < OneKB) {
                if (m_lstData.size() <=0)
                    return ar;
                BufferData b = m_lstData.get(0);
                nSize += b.bytes.length;
                m_lstData.remove(0);
                ByteBuffer buf = ByteBuffer.wrap(b.bytes);
                b.clear();
                ar.add(buf);
                if (nSize > OneKB)
                    return ar;
            }
            return ar;
        }
    }

}
