/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bematechus.kdslib;

import android.util.Log;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 *
 * @author David.Wong
 *
 * Use it to connect to other station
 */
public class KDSSocketTCPSideClient extends KDSSocketTCPSideBase implements Runnable {

    private static final String TAG = "KDSSocketTCPSideClient";

    private String m_strDestIP = "";
    private int m_nDestPort = 0;
    public boolean connectTo(KDSSocketManager manager, String ip, int nPort)
      {
          try
          {
              m_strDestIP = ip;
              m_nDestPort = nPort;

              m_socketChannel = SocketChannel.open();

              m_socketChannel.configureBlocking(false);

              interface_addToSocketManager(manager);
             // SocketAddress address = new InetSocketAddress(ip , nPort) ;
              // m_socketChannel.connect(address);
               m_socketManager = manager;
              (new Thread(this)).start();
               //this.run();
              return true;
          }
          catch (Exception e)
          {
              KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
              //KDSLog.e(TAG, KDSUtil.error( e));
              return false;
          }
      }
    private boolean connect(String ip, int nPort)
    {
        try
        {
            if (m_socketChannel.isConnected() ||
                    m_socketChannel.isConnectionPending())
                return true;
            if (!KDSSocketTCPSideBase.ping(ip, 2))
                return false;
            //long start = System.currentTimeMillis();
            //打开Socket通道
           // m_socketChannel = SocketChannel.open();
            //设置为非阻塞模式
            //m_socketChannel.configureBlocking(false);
           // interface_addToSocketManager(manager);
            SocketAddress address = new InetSocketAddress(ip , nPort) ;
            if (address == null)
                return false;
            boolean b = m_socketChannel.connect(address);
            if (b)
            {
                m_socketChannel.finishConnect();
                m_eventHandler.sendConnectedMessage(this);
            }

            //m_socketManager = manager;
            return true;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }
    @Override
    public void run() {

        this.connect(m_strDestIP,m_nDestPort);

    }

    public String getConnectToWhatIP()
    {
        return m_strDestIP;
    }

    public boolean isConnecting()
    {
        if (m_socketChannel == null)
            return false;
        return m_socketChannel.isConnectionPending();
    }

}
