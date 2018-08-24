
package com.bematechus.kdslib;


import android.os.Bundle;
import android.util.Log;

import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;

//import com.bematechus.kds.KDSSocketInterface;
//import java.net.StandardSocketOptions;
//import java.nio.channels.MembershipKey;
//import java.net.*;

/**
 *
 * @author David.Wong
 *
 * UDP
 */
public class KDSSocketUDP implements KDSSocketInterface{
    static final String TAG = "KDSSocketUDP";

    static String m_strBroadcaseAddress = "";
    Object m_locker = new Object();
    DatagramChannel m_channelControl; //send/receive control command to Master
    String m_strControlIP;//
    int	m_nControlPort;//server's TCP port

    KDSSocketMessageHandler m_eventHandler = null;
    KDSSocketManager m_socketManager;
    protected KDSSocketFIFOBuffer m_writeBuffer = new KDSSocketFIFOBuffer();

    public KDSSocketUDP()
    {
        m_strBroadcaseAddress = loadBroadcastAddress();

    }
    static public String getBroadcastAddress()
    {
        return "255.255.255.255";//m_strBroadcaseAddress;
    }
    public boolean start( int nPort, KDSSocketMessageHandler h, KDSSocketManager manager)
    {
        try {
            m_eventHandler = h;
            setOpt(nPort);
            this.init_control_udp();
            interface_addToSocketManager(manager);
            //this.init_transfer_udp();
        }catch (Exception err)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),err);//+err.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(err) );
            //err.printStackTrace();
        }
        return true;
    }
    public boolean close()
    {
         if (!closeChannels())
             return false;
         if (m_socketManager != null)
            m_socketManager.removeChannel(this);
         return true;
    }
    private void setOpt(int nTCPPort)
    {

        m_nControlPort = nTCPPort;
        m_strControlIP = getLocalIPAddr();
    }
    protected String getLocalIPAddr()
    {
        try
        {
            // 获取计算机名
            //String name = InetAddress.getLocalHost().getHostName();
            // 获取IP地址
            String ip = KDSSocketManager.getLocalIpAddress();// InetAddress.getLocalHost().getHostAddress();
            return ip;
//                System.out.println("计算机名："+name);
//                System.out.println("IP地址："+ip);
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
            //System.out.println("异常：" + e);
            //e.printStackTrace();
        }

        return "";
    }
    private boolean closeChannels()
    {
        try
        {
            synchronized (m_locker) {
                if (m_channelControl != null) {
                    m_channelControl.disconnect();

                    m_channelControl.socket().close();
                    m_channelControl.close();

                }
                return true;
            }
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);//+ e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
            return false;
        }

    }

    /******************************************************************
     * the old initTCPSocket function
     */
    protected boolean init_control_udp()
    {
        try
        {
            synchronized (m_locker) {
                m_channelControl = DatagramChannel.open();//
                //don't bind , for the port reuse 20161030
                //must bind
                m_channelControl.socket().setReuseAddress(true);
                m_channelControl.socket().bind(new InetSocketAddress(m_nControlPort));

                m_channelControl.socket().setReceiveBufferSize(Integer.MAX_VALUE);
                m_channelControl.socket().setSendBufferSize(Integer.MAX_VALUE);
                //m_channelControl.setOption(StandardSocketOptions.SO_BROADCAST, true);
                m_channelControl.socket().setBroadcast(true);

                //m_channelControl.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true); //debug
                //channel.setOption( StandardSocketOptions.SO_RCVBUF,16*1024);
                //channel.setOption(StandardSocketOptions.IP_MULTICAST_IF,ni);
                //InetAddress group = InetAddress.getByName( MFTPCommon.MFTP_GROUP_IP);// "225.4.5.6");
            }
        }
        catch(Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);//+e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
            return false;
        }


        return true;
    }
    public boolean reset()
    {
        try {
            close();
            return init_control_udp();
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);//+e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
        }
        return false;

    }

    public boolean interface_addToSocketManager(KDSSocketManager manager)
    {
        //manager.addChannelRead( m_channelGroup, this);
        synchronized (m_locker) {
            manager.addChannelReadWrite(m_channelControl, this);
            m_socketManager = manager; //for remove this socket.
        }
        return true;
    }
    public boolean interface_isUDP()
    {
        return true;
    }
    public boolean interface_isTCPListen()
    {
        return false;
    }
    public boolean interface_isTCPClient()
    {
        return false;
    }

    ByteBuffer m_bufferRead = ByteBuffer.allocate(MIN_BUFFER_SIZE);
    /*******************************************************************
     *
     */
    @Override
    public void interface_OnUDPRead(DatagramChannel channel)
    {
        m_bufferRead.clear();

        try {
            SocketAddress client = null;
            synchronized (m_locker){
                client = m_channelControl.receive(m_bufferRead);
            }
            if (client == null) return;
            int nlength = m_bufferRead.position();
            if (nlength <=0) return;
            ByteBuffer buffer = ByteBuffer.allocate(nlength);
            for (int i=0; i< nlength;i++)
                buffer.put(m_bufferRead.get(i));
            m_bufferRead.clear();
            m_eventHandler.sendReceiveDataMessage(this, client.toString(), buffer, nlength);

        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);//+ e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
        }

    }


    @Override
    public void interface_OnUDPWrite(DatagramChannel channel)
    {
        try
        {
            if (m_writeBuffer.count() <=0)
                return;
            Bundle b = m_writeBuffer.popupBundle();
            ByteBuffer buf = ByteBuffer.wrap(b.getByteArray("data"));
            String ip = b.getString("ip");
            writeData(ip, buf);

        }catch(Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);//+e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
        }
    }

    @Override
    public void interface_OnSockFreeTime()
    {

    }
    @Override
    public KDSSocketInterface interface_OnTCPServerAccept(ServerSocketChannel channel) { return null;}
    @Override
    public void interface_OnTCPClientWrite(SocketChannel channel){  }
    @Override
    public void interface_OnTCPClientRead(SocketChannel channel) { }
    @Override
    public void interface_OnTCPClientConnected(SocketChannel channel){ }
    public void interface_OnSocketDisconnected(SocketChannel channel){}

    /**********************************************************************************************/
    static public int MIN_BUFFER_SIZE = 1024;//large than packetsize
    protected void onControlSocketRead()
    {

        ByteBuffer buffer = ByteBuffer.allocate(MIN_BUFFER_SIZE);
        try
        {
            SocketAddress client = null;
            synchronized (m_locker) {
                client = m_channelControl.receive(buffer);
            }
            if (client == null) return;
            int nlength = buffer.position();
            m_eventHandler.sendReceiveDataMessage(this, client.toString(), buffer, nlength);


        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);//+e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
        }

    }

    public boolean broadcastData( ByteBuffer buffer)
    {
        return broadcastData(getBroadcastAddress(), buffer);
    }

    public boolean broadcastData(int nPort,  ByteBuffer buffer)
    {
        return broadcastData(getBroadcastAddress(),nPort,  buffer);
    }

    public boolean broadcastData(String ipAddress, ByteBuffer buffer)
    {
        try
        {
            buffer.position(0);
            this.writeData(ipAddress, buffer);
            return true;
        }catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);//+ e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
            return false;
        }
    }

    public boolean broadcastData(String ipAddress, int nPort, ByteBuffer buffer)
    {
        try
        {
            buffer.position(0);
            this.writeData(ipAddress,nPort, buffer);
            return true;
        }catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }

    public boolean writeData(String ipAddress, ByteBuffer buffer)
    {
        return writeData(ipAddress, m_nControlPort, buffer);

    }

    public boolean writeData(String ipAddress, int nPort, ByteBuffer buffer)
    {
        try
        {
            if (nPort ==0) return true;

            InetSocketAddress hostAddress = new InetSocketAddress(ipAddress,nPort);
            if (hostAddress == null) return true;
            buffer.position(0);
            synchronized (m_locker) {
                if (m_channelControl == null) return true;
                if (!m_channelControl.isOpen()) return true;
                int nSend = m_channelControl.send(buffer, hostAddress);

                return (nSend > 0);
            }
        }
        catch (SocketException se)
        {
            String strerr = se.getMessage();
            strerr = strerr.toUpperCase();
            if (strerr.indexOf("EACCES")>=0)
                reset();
            if (strerr.indexOf("ENETUNREACH") <0)
            {

                KDSLog.e(TAG, KDSLog._FUNCLINE_() ,se);//+ se.toString());
                //KDSLog.e(TAG, KDSLog._FUNCLINE_() + KDSUtil.error(se));
            }
            return false;
        }
        catch (ClosedChannelException ce)
        {
            return false;
        }
        catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);//+ e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
            return false;
        }
    }

    public static String loadBroadcastAddress()  {
        try {
            System.setProperty("java.net.preferIPv4Stack", "true");
            for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements(); ) {
                NetworkInterface ni = niEnum.nextElement();
                if (!ni.isLoopback()) {
                    for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                        if (interfaceAddress.getBroadcast() != null) {
                            return interfaceAddress.getBroadcast().toString().substring(1);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        return null;
    }

}
