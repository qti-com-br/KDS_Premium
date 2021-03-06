/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bematechus.kdslib;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;


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

    protected String m_appSocketID = ""; //if it is from router. kpp1-363

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

        ArrayList<ByteBuffer> bufs = m_writeBuffer.popup1KB();
        if (bufs.size()<=0) return;
        for (int i=0; i< bufs.size(); i++)
        {

            write_to_socket(bufs.get(i));

        }
        bufs.clear();
//        ByteBuffer buf = m_writeBuffer.popup();
//        if (buf != null) {
//
//            write_to_socket(buf);
//
//        }
    }
    public boolean interface_isUDP(){return false;}
    public boolean interface_isTCPListen(){return false;}
    public boolean interface_isTCPClient(){return true;}
    //ByteBuffer m_directByteBuffer = ByteBuffer.allocateDirect(10240); //10k

    protected  boolean write_to_socket(ByteBuffer buf)
    {


        try {
            //buf.rewind();
//            if (false)
//            {
//                int nwrite = buf.capacity();
//
//                if (nwrite > 0) {
//                    if (m_eventHandler != null)
//                        m_eventHandler.sendWriteDoneMessage(this, this.getRemoteIP(), nwrite);
//                }
//                return true;
//            }
//            else
                {
            //Unsafe unsafe = GetUsafeInstance.getUnsafeInstance();

//                if (m_directByteBuffer.capacity() < buf.capacity()) {
//                    m_directByteBuffer = ByteBuffer.allocateDirect(buf.capacity());
//                }
//                //ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buf.capacity());
//                System.arraycopy(buf.array(), 0, m_directByteBuffer.array(), 0, buf.capacity());
//                m_directByteBuffer.limit(buf.limit());
//                m_directByteBuffer.position(buf.position());
                int nwrite = 0;

                for (int i=0; i< 500; i++) {
                    if (buf.hasRemaining() ) {
                        nwrite += m_socketChannel.write(buf); //memory leak

                        Thread.sleep(50);
                    }
                    else
                        break;
//                        break;
//                    if (m_directByteBuffer.hasRemaining() ) {
//                        nwrite += m_socketChannel.write(m_directByteBuffer); //memory leak
//
//                        Thread.sleep(200);
//                    }
//                    else
//                        break;
                }

                buf.clear();

                //m_directByteBuffer.clear();


                if (nwrite > 0) {
                    if (m_eventHandler != null)
                        m_eventHandler.sendWriteDoneMessage(this, this.getRemoteIP(), nwrite);
                }

                return true;
            }

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

    public   boolean write(String strText)
    {


        byte[] bytes = KDSUtil.convertStringToUtf8Bytes(strText);
        ByteBuffer buf = ByteBuffer.wrap(bytes);

        return write(buf);


    }

    public boolean writeXmlTextCommand(String strXml)
    {

        //if (KDSConst._DEBUG) return true; //heap size issue here

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
     * ????????????http://blog.csdn.net/cao478208248/article/details/41648359

     ???socketChannel????????????????????????????????????????????????read?????????????????????0??????????????????socketChannel????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????read??????????????????????????????????????????socketChannel??????????????????????????????????????????????????????NIO ???ftp???????????????????????????????????????????????????????????????????????????read????????????????????????????????????????????????????????????????????????socket????????????????????????

     1???read??????????????????-1

     read??????-1??????????????????????????????????????????????????????close socket??????????????????????????????????????????????????????????????????socketChannel????????????key????????????????????????????????????????????????????????????????????????????????????socketChannel??????????????????????????????????????????????????????????????????????????????????????????IO?????????

     2???read??????????????????0

     ??????read??????0???3??????????????????????????????socketChannel?????????????????????????????????????????????????????????????????????0????????????bytebuffer???position??????limit?????????bytebuffer???remaining??????0???????????????????????????0??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????recv???????????????????????????read???????????????????????????0???

     -------------------------------------------------------------------------------------------------

     ??????????????????????????????????????????????????????????????????????????????channel????????????????????????????????????????????????????????????????????????????????????read??????????????????0.
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

                String remote_ip = "unkown ip";

                if (channel.socket() != null)
                    remote_ip = channel.socket().getInetAddress().getHostAddress() + ",local port=" +KDSUtil.convertIntToString(channel.socket().getLocalPort());
                KDSLog.e(TAG,KDSLog._FUNCLINE_()+ "read from ["+remote_ip + "] data <0");
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
            String remote_ip = "unkown ip";
            if (channel.socket() != null)
                remote_ip = channel.socket().getInetAddress().getHostAddress() + ", local port=" +KDSUtil.convertIntToString(channel.socket().getLocalPort());
            KDSLog.e(TAG,KDSLog._FUNCLINE_() +"Exception when read from: "+remote_ip);//+ KDSLog.getStackTrace(e));
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
    protected void doCommand_backup()
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

    protected void doCommand()
    {
        synchronized (m_bufferLocker) {
            while (true) {
                //kp-64 we wanted to make it work with both (TCP/IP only):
                //1) STX 0x02 + COMMAND 0x05 + DATA_LEN_HIGH + DATA_LEN_LOW + PAYLOAD + ETX 0x03
                //2) PAYLOAD
                int nTextLength = m_commandBuffer.skip_to_STX(m_commandBuffer.getTextBuffer());
                if (nTextLength >0)
                {
                    doOriginalXmlCommand(m_commandBuffer.getTextBuffer(), nTextLength);
                }
                //
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
        //don't need this, we have pass most command xml to thread.
//        if (!KDSApplication.isRouterApp())
//        {
//            try {
//                //Thread.sleep(500);
//            }
//            catch (Exception e)
//            {
//                KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + KDSLog.getStackTrace(e));
//            }
//        }
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

            InputStreamReader ir = new InputStreamReader(p.getInputStream());
            BufferedReader buf = new BufferedReader(ir);//new InputStreamReader(p.getInputStream()));

            String str = new String();
            String strInfo = "";
            //read all info
            while ((str = buf.readLine()) != null) {
                str = str + "\r\n";
                strInfo+=str;
            }
            //KDSLog.i(TAG,KDSLog._FUNCLINE_()+ strInfo);
            buf.close();
            ir.close();
            if (strInfo.indexOf("time=") >=0)
                return true;
            KDSLog.i(TAG,KDSLog._FUNCLINE_()+ strInfo);
            return false;//strInfo;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);//+e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
        }
        return false;
    }


    public boolean isBufferTooManyWritingData(int nMaxCount)
    {
        return (m_writeBuffer.count() > nMaxCount);

    }

    final int MAX_WRITE_BUFFER_SIZE = 30;
    public boolean interface_WriteBufferIsFull()
    {
        return isBufferTooManyWritingData(MAX_WRITE_BUFFER_SIZE);
    }

    /**
     * kpp1-363
     * @param id
     */
    public void setAppSocketID(String id)
    {
        m_appSocketID = id;
    }

    /**
     * kpp1-363
     * @return
     */
    public String getAppSocketID()
    {
        return m_appSocketID;
    }

    String mXmlReceived = "";
    String START_TAG = "<Transaction";
    String END_TAG = "</Transaction>";
    /**
     * KP-62 As a reseller, I want to be able to send orders to Allee and Premium using the same format in TCP/IP so I don't need to change my POS
     * User will send original xml file text to here.
     * Just need to change text to utf8.
     * It is no header (STX) and end tag (ETX).
     * @param buffer
     * @param nlen
     */
    private void doOriginalXmlCommand(byte[] buffer, int nlen)
    {
        byte[] bytes = buffer;// buffer.array();
        String utf8 = KDSUtil.convertUtf8BytesToString(bytes, 0, nlen);
        //Log.d(TAG, "-----------------------");
        //Log.d(TAG, utf8);
        mXmlReceived += utf8;

        //buffer.clear();
        String start = START_TAG;// "<" + KDSXMLParserOrder.DBXML_ELEMENT_TRANSACTION + ">";
        String end = END_TAG;// "</" + KDSXMLParserOrder.DBXML_ELEMENT_TRANSACTION + ">";

        for (int i=0; i< 50; i++) {
            int nstart = mXmlReceived.indexOf(start);
            int nend = mXmlReceived.indexOf(end);
            if (nstart >= 0 && nend > 0) {
                String xmlOrder = mXmlReceived.substring(nstart, nend + end.length());
                //Log.d(TAG, "-----------------------");
                //Log.d(TAG, xmlOrder);
                mXmlReceived = mXmlReceived.substring(nend + end.length());
                doXmlCommand(xmlOrder);
                try {
                    Thread.sleep(10);
                }
                catch (Exception e)
                {}
            } else
                break;
        }


    }
}

