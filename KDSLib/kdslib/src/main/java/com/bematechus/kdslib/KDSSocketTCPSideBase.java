/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bematechus.kdslib;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


/**
 *
 * @author David.Wong
 * The TCP data chain client side, server side and client side all use this socket
 * Revise:
 * 20160721, comment it. The <0, it is not disconnected
 */
public class KDSSocketTCPSideBase implements KDSSocketInterface{

    static final String TAG = "KDSSocketTCPSideBase";

    protected SocketChannel m_socketChannel = null  ;
    protected KDSSocketManager m_socketManager = null;
    protected KDSSocketTCPCommandBuffer m_commandBuffer = new KDSSocketTCPCommandBuffer();
    protected KDSSocketFIFOBuffer m_writeBuffer = new KDSSocketFIFOBuffer();


    protected  KDSSocketMessageHandler m_eventHandler = null;

    protected Object m_attachedObj = null; //in multiple transfering, we need this to identify each stations.

    public KDSSocketTCPSideBase()
    {
      m_writeBuffer.clear();
    }

    public KDSSocketTCPSideBase(SocketChannel socket)
    {
      setSocketChannel(socket);

    }

    public void freeCommandBuffer()
    {
        m_commandBuffer.freeBuffer();
    }

    public void setAttachedObj(Object obj)
    {
      m_attachedObj = obj;
    }
    public String getRemoteIP()
    {
      if (m_socketChannel == null)
          return "";
      SocketAddress addr = m_socketChannel.socket().getRemoteSocketAddress();
      if (addr == null)
          return "";
      String s = m_socketChannel.socket().getInetAddress().getHostAddress();

      return s;

    }
      public boolean isConnected()
      {
          if (m_socketChannel == null)
              return false;
          return m_socketChannel.isConnected();
      }

//    public boolean canConnectToServer()
//    {
//        try
//        {
//            if (m_socketChannel != null)
//            {
//                m_socketChannel.socket().sendUrgentData(0xff);
//
//            }
//        } catch (IOException e)
//        {
//            Log.e(TAG, KDSLog._FUNCLINE_()+e.toString());
//            Log.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
//            //e.printStackTrace();
//            return false;
//        }
//        catch (Exception e){
//            Log.e(TAG,KDSLog._FUNCLINE_() + e.toString());
//            Log.e(TAG, KDSUtil.error( e));
//            //e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
    public void reset()
    {

    }
      
    public SocketChannel getSocketChannel()
    {
      return m_socketChannel;
    }
    public void setSocketChannel(SocketChannel channel)
    {
      m_socketChannel = channel;
      initSocket();
    }

    public void initSocket()
    {
        try {
            m_socketChannel.socket().setSoLinger(true, 0);
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);//+e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
        }
    }
    

    public void setEventHandler(KDSSocketMessageHandler handler)
    {
        m_eventHandler = handler;
    }
    /*******************************************************************
     *
    */
    public boolean interface_addToSocketManager(KDSSocketManager manager)
    {
       manager.addChannelConnectReadWrite( m_socketChannel, this);
       m_socketManager = manager;
       return true;
    }
    /*******************************************************************
     *
    */

    public void interface_OnUDPRead(DatagramChannel channel)
    {

    }
    public void interface_OnUDPWrite(DatagramChannel channel)
    {

    }

    public void interface_OnSockFreeTime()
    {
    }
    public void interface_OnSocketDisconnected(SocketChannel channel){ interface_OnTCPClientDisconnected(channel);}

    public KDSSocketInterface interface_OnTCPServerAccept(ServerSocketChannel channel)
    {
        return null;
    }

    public void interface_OnTCPClientConnected(SocketChannel channel)
    {

        if (channel.isConnected()) {
            if (m_eventHandler != null)
                m_eventHandler.sendConnectedMessage(this);
        }
    }
    // @Override
    public void interface_OnTCPClientWrite(SocketChannel channel)
    {

        if (!this.isConnected()) return;
        ByteBuffer buf = m_writeBuffer.popup();
        if (buf != null) {
            write_to_socket(buf);

        }
    }
    public boolean interface_isUDP(){return false;}
    public boolean interface_isTCPListen(){return false;}
    public boolean interface_isTCPClient(){return true;}

    protected  boolean write_to_socket(ByteBuffer buf)
    {
        try
        {
            //buf.rewind();
            int nwrite =  m_socketChannel.write(buf);
            if (nwrite >0) {
                if (m_eventHandler != null)
                    m_eventHandler.sendWriteDoneMessage(this,this.getRemoteIP(), nwrite );
            }
            return true;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);//+e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
            //e.printStackTrace();
            return false;
        }
    }
    protected boolean write( ByteBuffer buf)
    {
        if (!isConnected())
            return false;
        m_writeBuffer.add(buf);
        return true;

    }

    protected  boolean write(String strText)
    {

        byte[] bytes = KDSUtil.convertStringToUtf8Bytes(strText);
        ByteBuffer buf = ByteBuffer.wrap(bytes);

        return write(buf);

    }

    public boolean writeXmlTextCommand(String strXml)
    {


        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildXMLCommand(strXml);
        return write(buf);

    }

    protected void debug_buffer(ByteBuffer buf)
    {

    //            System.out.println(buf.toString());
        int  ncount = buf.position();
        String s = "";
        for (int i=0; i< ncount; i++)
        {
            byte b = buf.get(i);
            s += KDSUtil.convertIntToString(b);
            s += " ";

        }
        KDSLog.i(TAG, KDSLog._FUNCLINE_()+s);
        //System.out.println(s);


    }
    public void interface_OnTCPClientDisconnected(SocketChannel channel)
    {
          try
        {
            boolean bConnected = false;
            KDSLog.i(TAG,KDSLog._FUNCLINE_()+ "Disconnected");
            if (m_socketChannel != null) {
                bConnected = isConnected();
                //m_socketChannel.close();
                if (bConnected) {
                    m_socketChannel.socket().shutdownInput();
                    m_socketChannel.socket().shutdownOutput();
                    m_socketChannel.socket().getInputStream().close();
                    m_socketChannel.socket().getOutputStream().close();
                }
            }

        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        finally {
            boolean bConnected = isConnected();
              try {
                  m_socketChannel.socket().close();

                  m_socketChannel.close();
              }
              catch (Exception err)
              {
                  KDSLog.e(TAG,KDSLog._FUNCLINE_(),err);// + KDSLog.getStackTrace(err));
              }

            if (m_socketManager != null)
            {
                m_socketManager.removeChannel(this);

            }
            if (m_eventHandler!= null) {
                if (bConnected)
                    m_eventHandler.sendDisconnectedMessage(this);
            }
        }

    }

    static final int READ_BUFFER_SIZE = 10240; //10k
    private ByteBuffer m_bufRead =  ByteBuffer.allocate(READ_BUFFER_SIZE);;
    /**
     * About read return -1,
     * In this app, most return -1 case happens in "Thread do gui refreshing work". Please notice.
     * 转载地址http://blog.csdn.net/cao478208248/article/details/41648359

     当socketChannel为阻塞方式时（默认就是阻塞方式）read函数，不会返回0，阻塞方式的socketChannel，若没有数据可读，或者缓冲区满了，就会阻塞，直到满足读的条件，所以一般阻塞方式的read是比较简单的，不过阻塞方式的socketChannel的问题也是显而易见的。这里我结合基于NIO 写ftp服务器调试过程中碰到的问题，总结一下非阻塞场景下的read碰到的问题。注意：这里的场景都是基于客户端以阻塞socket的方式发送数据。

     1、read什么时候返回-1

     read返回-1说明客户端的数据发送完毕，并且主动的close socket。所以在这种场景下，（服务器程序）你需要关闭socketChannel并且取消key，最好是退出当前函数。注意，这个时候服务端要是继续使用该socketChannel进行读操作的话，就会抛出“远程主机强迫关闭一个现有的连接”的IO异常。

     2、read什么时候返回0

     其实read返回0有3种情况，一是某一时刻socketChannel中当前（注意是当前）没有数据可以读，这时会返回0，其次是bytebuffer的position等于limit了，即bytebuffer的remaining等于0，这个时候也会返回0，最后一种情况就是客户端的数据发送完毕了（注意看后面的程序里有这样子的代码），这个时候客户端想获取服务端的反馈调用了recv函数，若服务端继续read，这个时候就会返回0。

     -------------------------------------------------------------------------------------------------

     实际写代码过程中观察发现，如果客户端发送数据后不关闭channel，同时服务端收到数据后反倒再次发给客户端，那么此时客户端read方法永远返回0.
     * server and client side all go into this function.
     * @param channel
     */
    @Override
    public void interface_OnTCPClientRead(SocketChannel channel)
    {
        if (channel.socket() == null) return;
    //            if (this.m_bServerSide)
    //                KDSUtil.debug(" ----------Server read------------");
    //            else
    //                KDSUtil.debug("------------Client read------------");
       // if (m_bufRead == null)
       //     m_bufRead = ByteBuffer.allocate(KDSSocketTCPCommandBuffer.BUFFER_SIZE);
        m_bufRead.clear();
        //ByteBuffer buf = ByteBuffer.allocate(KDSSocketTCPCommandBuffer.BUFFER_SIZE);
        //buf.clear();
        try
        {

            int nread = channel.read(m_bufRead);

            ////////////////////
            if (nread ==0) return; //The number of bytes read, possibly zero, or -1 if the channel has reached end-of-stream
            if (nread <0) //disconnected, 20160721, comment it. The <0, it is not disconnected
            {//20160930, use it again, as unormal close kds app, we don't know connect closed.
                //it cause kds app start again, can not been reconnected
                //if (channel.socket() == null) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_()+ "read data <0");
                    interface_OnTCPClientDisconnected(channel);
                    return;
                //}
            }

           // KDSUtil.debug("socket read=" + KDSUtil.convertIntToString(nread));
//            debug_buffer(buf);
//            //debug
//            //echo
            m_bufRead.position(0);
            m_bufRead.limit(nread);
//            channel.write(buf);
//            String s = "ABCDEF";
//            write(s);

            //if (m_eventHandler != null)
            //    m_eventHandler.sendReceiveDataMessage(this, buf, nread);
    //                if (!m_bTransferingData)
            //{
            synchronized (m_bufferLocker) {
                m_commandBuffer.appendData(m_bufRead, nread);
            }
            m_bufRead.clear();
            doCommand();
            //}
    //                else
    //                {
    //                    //if (!this.m_bServerSide) //just client side do this
    //                    if (m_dataDirection == DataDirection.Receiving_File)
    //                        doReceiveFileData(buf, nread);
    //                }

        }
        catch (NotYetConnectedException erC)
        {
            //Log.e(TAG, KDSLog._FUNCLINE_()+erC.toString());
            //Log.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(erC) );
           // erC.printStackTrace();
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
            //KDSLog.e(TAG,KDSLog._FUNCLINE_()+ e.toString());
            //Log.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
            //e.printStackTrace();
            this.interface_OnTCPClientDisconnected(channel);

        }

    }
    Object m_bufferLocker = new Object();

    /**********************************************************************/
    /**
     *
     */
    protected void doCommand()
    {
        synchronized (m_bufferLocker) {
            while (true) {
                m_commandBuffer.skip_to_STX();
                if (m_commandBuffer.fill() <= 1)
                    return;
                byte command = m_commandBuffer.command();
                if (command == 0)
                    return;
                switch (command) {
                    case KDSSocketTCPCommandBuffer.XML_COMMAND: {//the command send by xml format
                        //1. parse the xml text
                        int ncommand_end = m_commandBuffer.command_end();
                        if (ncommand_end == 0)
                            return; //need more data

                        byte[] bytes = m_commandBuffer.xml_command_data();
                        m_commandBuffer.remove(ncommand_end);
                        String utf8 = KDSUtil.convertUtf8BytesToString(bytes);
                        doXmlCommand(utf8);

                    }
                    break;
                    case KDSSocketTCPCommandBuffer.XML_COMMAND_WIN_KDS://STX, Command, LEN0,LEN1,d0,d1,d2 ... ETX
                    {
                        int ncommand_end = m_commandBuffer.command_end();
                        if (ncommand_end == 0)
                            return; //need more data

                        byte[] bytes = m_commandBuffer.xml_winkds_command_data();
                        m_commandBuffer.remove(ncommand_end);
                        String utf8 = KDSUtil.convertUtf8BytesToString(bytes);
                        doXmlCommand(utf8);
                    }
                    break;
                    default: {
                        m_commandBuffer.remove(1);
                        break;
                    }
                }
            }
        }
    }

    protected void doXmlCommand(String strXml)
    {
        if (m_eventHandler != null)
            m_eventHandler.sendReceiveXmlMessage(this, strXml);
        //slow down socket speed, let the gui drawing
        try {
            Thread.sleep(500);
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + KDSLog.getStackTrace(e));
        }
    }

    public void closeChannel(SocketChannel channel)
    {
        try
        {
            boolean bConnected = false;
            KDSLog.i(TAG, KDSLog._FUNCLINE_()+"Disconnected");
            if (m_socketChannel != null) {
                bConnected = isConnected();
                if (!m_socketChannel.socket().isInputShutdown())
                    m_socketChannel.socket().shutdownInput();
                if (!m_socketChannel.socket().isOutputShutdown())
                    m_socketChannel.socket().shutdownOutput();


                m_socketChannel.socket().close();

                m_socketChannel.close();
            }

        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);//+e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
            //e.printStackTrace();
        }
        finally {
            if (m_socketManager != null)
            {
                m_socketManager.removeChannel(this);

            }
        }
    }

    public void close()
    {
        KDSLog.i(TAG, KDSLog._FUNCLINE_()+"close socket");


        closeChannel(this.getSocketChannel());

        m_commandBuffer.reset();
        m_bufRead.clear();
        this.interface_OnTCPClientDisconnected(this.getSocketChannel());

    }

    static public boolean ping(String strIP, int pingNum)
    {
        String result = "";
        try {
            Process p = Runtime.getRuntime().exec("/system/bin/ping -W 1 -c " + pingNum + " " + strIP); // 10.83.50.111  m_strForNetAddress
            int status = p.waitFor();

            BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String str = new String();
            String strInfo = "";
            //read all info
            while ((str = buf.readLine()) != null) {
                str = str + "\r\n";
                strInfo+=str;
            }
            KDSLog.i(TAG,KDSLog._FUNCLINE_()+ strInfo);
            if (strInfo.indexOf("time=") >=0)
                return true;
            return false;//strInfo;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);//+e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
        }
        return false;
    }


}

