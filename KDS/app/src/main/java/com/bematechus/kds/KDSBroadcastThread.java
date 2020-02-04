package com.bematechus.kds;

import com.bematechus.kdslib.KDSSocketUDP;

import java.nio.ByteBuffer;

/**
 *
 * The asynctask has the pool limit, so I use this to broadcast data
 */
public class KDSBroadcastThread implements Runnable {

    KDSSocketUDP m_udp = null;
    ByteBuffer m_buffer = null;
    String m_ip = "";
    int m_port = -1;
    KDSBroadcastThread(KDSSocketUDP udp, ByteBuffer buffer)
    {
        m_udp = udp;
        m_buffer = buffer;
    }

    KDSBroadcastThread(KDSSocketUDP udp,int nport, ByteBuffer buffer)
    {
        m_udp = udp;
        m_buffer = buffer;
        m_port = nport;
    }
    KDSBroadcastThread(KDSSocketUDP udp,String ip, int nport, ByteBuffer buffer)
    {
        m_udp = udp;
        m_buffer = buffer;
        m_ip = ip;
        m_port = nport;
    }
    public void start()
    {
        (new Thread(this, "Broadcast")).start();
    }
    @Override
    public void run() {
        if (m_port<=0)
        {
            if (m_ip.isEmpty())
                m_udp.broadcastData(m_buffer);
            else
                m_udp.broadcastData(m_ip,m_buffer);
        }
        else
        {
            if (m_ip.isEmpty())
                m_udp.broadcastData(m_port, m_buffer);
            else
                m_udp.broadcastData(m_ip,m_port, m_buffer);
        }
    }
}
