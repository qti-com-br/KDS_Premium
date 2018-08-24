/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bematechus.kdslib;
import android.util.Log;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 *
 * @author David.Wong
 *
 * The socket listen
 * Listen station connections
 */
public class KDSSocketTCPListen implements KDSSocketInterface{
    static final String TAG = "KDSSocketTCPListen";
    ServerSocketChannel m_serverChannel = null;

    KDSSocketMessageHandler m_eventHandler = null;
    KDSSocketManager m_socketManager = null;

    int m_nListenPort = 0;

      
    public KDSSocketTCPListen()
    {

    }

     public void setEventHandler(KDSSocketMessageHandler h)
     {
         m_eventHandler = h;
     }
    /**
    * Build the server
    * @param nport
    * @return
    */
    private boolean buildServer(int nport)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_()+ "buildServer");
       try {
           m_serverChannel = ServerSocketChannel.open() ;
           ServerSocket ss = m_serverChannel.socket() ;
           ss.setReuseAddress(true);
           InetSocketAddress address = new InetSocketAddress(nport) ;
           m_serverChannel.configureBlocking(false) ;
           ss.bind(address) ;
           return true;


       } catch (Exception e) {

           KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
           //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e));
           //e.printStackTrace();
           return false;
       }
    }
    public boolean startServer(int nPort,KDSSocketManager manager, KDSSocketMessageHandler h )
    {
        m_nListenPort = nPort;
        this.setEventHandler(h);
        buildServer(nPort);
        interface_addToSocketManager(manager);
        return true;
    }

    public void stop()
    {
        try
        {

            if (m_serverChannel != null) {
                m_serverChannel.socket().close();
                m_serverChannel.close();

            }
            if (m_socketManager != null)
            {
                m_socketManager.removeChannel(this);
            }

        }
        catch (Exception e)
        {
            //KDSLog.e(TAG, e.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + KDSUtil.error(e) );
            //e.printStackTrace();
        }
    }

    public boolean isListening()
    {
        if (m_serverChannel == null)
            return false;
        return m_serverChannel.isOpen();
    }
    /*******************************************************************
     *
    */
    public boolean interface_addToSocketManager(KDSSocketManager manager)
    {
       manager.addChannelAccept(m_serverChannel, this);
       return true;
    }

    public boolean interface_isUDP()
    {
        return false;
    }
    public boolean interface_isTCPListen()
    {
        return true;
    }
    public boolean interface_isTCPClient()
    {
        return false;
    }
    /*******************************************************************
     *
    */
    @Override
    public void interface_OnUDPRead(DatagramChannel channel)
    {
    }
    @Override
    public void interface_OnUDPWrite(DatagramChannel channel)
    {
    }
    @Override
    public void interface_OnSockFreeTime()
    {
    }
    public void interface_OnSocketDisconnected(SocketChannel channel){}
    /**
     *
     * New connection requirement comes in.
     * @param channel
     */
    @Override
    public KDSSocketInterface interface_OnTCPServerAccept(ServerSocketChannel channel)
    {
        try
        {
            KDSLog.i(TAG,KDSLog._FUNCLINE_()+ "Accept");
            SocketChannel client = channel.accept();

            KDSLog.i(TAG,KDSLog._FUNCLINE_()+ client.socket().getRemoteSocketAddress().toString());

            KDSSocketTCPSideServer sock = new KDSSocketTCPSideServer(client);
            client.configureBlocking(false) ;

            sock.setEventHandler(m_eventHandler);
            sock.refreshRemoteIP();
            sock.setListenPort(m_nListenPort);

            if (m_eventHandler != null)
                m_eventHandler.sendAccept(this, sock);
            return sock;
        }
        catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return null;
        }
    }
    @Override
    public void interface_OnTCPClientWrite(SocketChannel channel)
    {

    }
    @Override
    public void interface_OnTCPClientRead(SocketChannel channel)
    {

    }
    @Override
    public void interface_OnTCPClientConnected(SocketChannel channel)
    {

    }
}
