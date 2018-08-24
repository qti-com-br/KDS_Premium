/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bematechus.kdslib;

import java.nio.channels.SocketChannel;

/**
 *
 * @author David.Wong
 * Use it to accept tcp connection
 */
public class KDSSocketTCPSideServer extends KDSSocketTCPSideBase {

    String m_strRemoteIP = "";
    int m_nListenPort = 0; //listen which port number and get this accepting socket.
    public KDSSocketTCPSideServer(SocketChannel socket)
    {
        m_socketChannel = socket;

    }

    public void refreshRemoteIP()
    {
        m_strRemoteIP = this.getRemoteIP();
    }

    public String getSavedRemoteIP()
    {
        return m_strRemoteIP;
    }

    public void setListenPort(int nPort)
    {
        m_nListenPort = nPort;
    }
    public int getListenPort()
    {
        return m_nListenPort;
    }

}
