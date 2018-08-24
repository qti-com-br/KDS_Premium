package com.bematechus.kdslib;
//package pckds;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

/**
 *
 * @author David.Wong
 * Revise:
 *  20160721, fix the connected event fired twice issue
 */
public class KDSSocketManager implements Runnable {
    final static String TAG = "KDSSocketManager";
    private Selector m_selector = null;
    private Thread m_threadSocket = null;
    private boolean m_bRunning = false;
    final private Object m_locker = new Object();

    public KDSSocketManager() {
        try {
            m_threadSocket = null;
            m_selector = Selector.open();
        } catch (Exception e) {
            m_selector = null;
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
    }

    public Selector getSelector() {
        return m_selector;
    }

    public boolean addChannelReadWrite(DatagramChannel channel, Object objAttached) {
        try {
            if (channel == null) return false;
            channel.configureBlocking(false);
            synchronized (m_locker) {
                channel.register(m_selector, SelectionKey.OP_READ| SelectionKey.OP_WRITE, objAttached);
            }
            return true;
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }

    public boolean removeChannel(Object objAttached) {
        synchronized (m_locker) {
            Iterator<SelectionKey> iter = m_selector.keys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = (SelectionKey) iter.next();
                if (key.attachment() == objAttached) {
                    key.cancel();
                    return true;

                }
            }
            return false;
        }
    }

//    public boolean addChannelRead(SocketChannel channel, Object objAttached) {
//        try {
//            channel.configureBlocking(false);
//            synchronized (m_locker) {
//                channel.register(m_selector, SelectionKey.OP_READ, objAttached);
//            }
//            return true;
//        } catch (IOException e) {
//            Log.e(TAG,KDSLog._FUNCLINE_() + e.toString());
//            Log.e(TAG, KDSUtil.error( e));
//            return false;
//        }
//    }

    public boolean addChannelAccept(ServerSocketChannel channel, Object objAttached) {
        try {
            channel.configureBlocking(false);
            synchronized (m_locker) {
                channel.register(m_selector, SelectionKey.OP_ACCEPT, objAttached);
            }
            return true;
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString() );
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
            return false;
        }
    }

    public boolean addChannelConnectReadWrite(SocketChannel channel, Object objAttached) {
        try {
            channel.configureBlocking(false);
            synchronized (m_locker) {
                channel.register(m_selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE, objAttached);
            }
            return true;
        } catch (IOException e) {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);// +e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
            return false;
        }
    }

    /**
     * *****************************************************************************************
     */
    private final int SELECT_TIMEOUT = 100;

    private void threadSelect() {
        while (m_bRunning) {
           // boolean bSleep =true;// false; force it sleep 100 ms
            try {
                synchronized (m_locker) {
//                    if (m_selector.keys().size() <= 0) {
//                        bSleep = true;
//                    } else {
                        if (m_selector.select(SELECT_TIMEOUT) > 0) {
                            //Thread.sleep(100);
                            Iterator<SelectionKey> keyIterator = m_selector.selectedKeys().iterator();
                            while (keyIterator.hasNext()) {
                                SelectionKey key = keyIterator.next();
                                if (key ==null) continue;
                                if (key.isValid()) {
                                    KDSSocketInterface obj = (KDSSocketInterface) key.attachment();
                                    if (obj == null) continue;
                                    if (obj.interface_isUDP()) {
                                        handleUDP(key);
                                    }
                                    else if (obj.interface_isTCPListen()) {
                                        handleTCPServer(key);
                                    }
                                    else if (obj.interface_isTCPClient()) {
                                        handleTCPClient(key);
                                    }
                                } else {
                                    key.channel().close();//close the socket
                                    key.cancel();

                                }
                                keyIterator.remove();

                            }
                        }
//                        else {
//                            bSleep = true;
//                        }
                 //   }
                }
               // if (bSleep) {
                    Thread.sleep(100);
                   // handleFreeTime();
                //}
            } catch (Exception err) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_(),err);// + err.toString());
                //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(err) );
                //err.printStackTrace();
            }

        }
    }

    /**
     * *****************************************************************************************
     */
    private void handleUDP(SelectionKey key) {
        try {
            //check read
            if (key.isReadable()/* || key.isWritable()*/) {
                DatagramChannel channel = (DatagramChannel) key.channel();
                //final Handle handle = map.get(socketChannel);
                KDSSocketInterface obj = (KDSSocketInterface) key.attachment();
                obj.interface_OnUDPRead(channel);
            }

            if (key.isWritable()) {
                DatagramChannel channel = (DatagramChannel) key.channel();
                //final Handle handle = map.get(socketChannel);
                KDSSocketInterface obj = (KDSSocketInterface) key.attachment();
                obj.interface_OnUDPWrite(channel);
            }
        }
        catch (Exception err)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),err);// + err.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(err) );
        }
    }

    /**
     * Tcp server get new connection requirement.
     *
     * @param key
     */
    private void handleTCPServer(SelectionKey key) {

        try
        {
            KDSLog.i(TAG,KDSLog._FUNCLINE_() + "handleTCPServer");
            //check read
            if (key.isAcceptable()) {
                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                KDSSocketInterface obj = (KDSSocketInterface) key.attachment();
                //TCPSideServer client = obj.OnTCPServerAccept(channel);
                KDSSocketInterface client = obj.interface_OnTCPServerAccept(channel);
                if (client != null) {
                    client.interface_addToSocketManager(this);
                }
                key.interestOps(SelectionKey.OP_ACCEPT);
            }
        }
        catch (Exception err)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),err);// + err.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(err) );
        }
    }


    private boolean handleTCPClient(SelectionKey key) {

        try {
            if (!key.isValid()) return false;

            SocketChannel channel = (SocketChannel) key.channel();
            if (channel == null) return false;

            KDSSocketInterface obj = (KDSSocketInterface) key.attachment();
            if (obj == null) return false;

            if (channel.socket() == null) return false;

            //check read
            if (key.isReadable()) {
                obj.interface_OnTCPClientRead(channel);
            }
            if ( key.isWritable()) {
                obj.interface_OnTCPClientWrite(channel);
            }
            if (key.isConnectable()) {
                if (channel.isConnectionPending()) {
                    channel.finishConnect();
                    //}//20160721, comment it. Otherwise, the connected event happened twice!!!!
                    obj.interface_OnTCPClientConnected(channel);
                }
            }
            return true;
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
            return false;


        }
    }

//    private void handleFreeTime() {
//
//        synchronized (m_locker) {
//            Iterator<SelectionKey> keyIterator = m_selector.keys().iterator();
//            while (keyIterator.hasNext()) {
//                SelectionKey key = keyIterator.next();
//                if (key.isValid()) {
//
//                    KDSSocketInterface obj = (KDSSocketInterface) key.attachment();
//                    obj.interface_OnSockFreeTime();
//                }
//
//            }
//        }
//
//    }


    public boolean startThread() {
        m_bRunning = true;
        m_threadSocket = new Thread(this, "SocketManager");
        m_threadSocket.start();
        return true;

    }

    public boolean stopThread() {
        if (m_threadSocket != null) {
            m_bRunning = false;
            try {
                m_threadSocket.join(1000);
            } catch (Exception e) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
                //KDSLog.e(TAG, KDSUtil.error( e));
            }
        }
        m_threadSocket = null;
        return true;
    }

    public boolean isThreadRunning()
    {
        return (m_threadSocket != null);
    }

    static public boolean isNetworkActived(Context context) {
        String service = Context.CONNECTIVITY_SERVICE;
        ConnectivityManager cm = (ConnectivityManager) (context.getSystemService(service));
        NetworkInfo ni = cm.getActiveNetworkInfo();
        // showMsg(ni.toString());

        if (ni != null && ni.isConnectedOrConnecting())
            return true;
        return false;
    }

    static public boolean isIPV6Address(String strIP) {
        return (strIP.indexOf(":") >= 0);

    }

    public static String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer(b.length);
        String stmp = "";
        int len = b.length;
        for (int n = 0; n < len; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1)
                hs = hs.append("0").append(stmp);
            else {
                hs = hs.append(stmp);
            }
        }
        return String.valueOf(hs);
    }

    static public String getLocalIpAddress() {
        ArrayList<String> ar = getLocalIpAddressWithMac();
        if (ar.size() <=0) return "";
        return ar.get(0);
    }

    /**
     *
     * @return
     *   array: 0: ipaddress
     *          1: mac
     */
    static public ArrayList<String> getLocalIpAddressWithMac() {
        ArrayList<String> ar = new ArrayList<>();
        try {
            String strResult = "";
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            for (; en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();

                String s1 = intf.getDisplayName();

                if (intf.isVirtual()) continue;
                if (intf.isPointToPoint()) continue;
                if (!intf.isUp()) continue;
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {

                        strResult = inetAddress.getHostAddress().toString();

                        if (!isIPV6Address(strResult)) {
                            ar.add(strResult);
                            byte[] mac = intf.getHardwareAddress();
                            String strMac = byte2hex(mac);
                            ar.add(strMac);
                            return ar;
                        }

                    }
                }
            }
            return ar;
        } catch (Exception ex) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),ex);// + ex.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(ex) );
        }
        return ar;
    }

    static public ArrayList<String> getLocalAllMac() {
        ArrayList<String> ar = new ArrayList<>();
        try {
            //String strResult = "";
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            for (; en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();

                String s1 = intf.getDisplayName();

                if (intf.isVirtual()) continue;
                if (intf.isPointToPoint()) continue;
                if (!intf.isUp()) continue;

                byte[] mac = intf.getHardwareAddress();
                if (mac != null && mac.length >0) {
                    String strMac = byte2hex(mac);
                    ar.add(strMac);
                }

//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress()) {
//
////                        if (!isIPV6Address(strResult))
////                        {
//                            byte[] mac = intf.getHardwareAddress();
//                            String strMac = byte2hex(mac);
//                            ar.add(strMac);
//                        //}
//                    }
//                }
            }
            return ar;
        } catch (Exception ex) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),ex);// + ex.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(ex) );
        }
        return ar;
    }


     public void run() 
     {

         try {
             threadSelect();
             KDSLog.d(TAG,KDSLog._FUNCLINE_() + "Sockets select thread exit!!!");
         }
         catch (Exception err)
         {

             KDSLog.e(TAG, KDSLog._FUNCLINE_(),err);// +err.toString());
             //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(err) );

         }

     }

    /*
* Load file content to String
*/
    public static String loadFileAsString(String filePath) throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    /*
     * Get the STB MacAddress
     */
    public static String getMacAddressFromFile(){
        try {
            String s =  loadFileAsString("/sys/class/net/eth0/address")
                    .toUpperCase().substring(0, 17);

            s = s.replace(":", "");
            return s;
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return "";
        }
    }




    static public NetworkInterface getLocalNetworkInterface()
    {
        try
        {
            String strResult = "";
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            for (; en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();

                String s1 = intf.getDisplayName();
                if (intf.isVirtual()) continue;
                if (intf.isPointToPoint()) continue;
                if (!intf.isUp()) continue;
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        return intf;
                    }
                }
            }
            return null;
        }
        catch (Exception ex)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),ex);// + ex.toString());
            //KDSLog.e(TAG, KDSUtil.error( ex));
        }
        return null;
    }


    public static boolean isReachableLocalIp(String ip, int msTimeout)
    {
        try{
            InetAddress address = InetAddress.getByName(ip);//ping this IP
            NetworkInterface ni = getLocalNetworkInterface();

            if(address.isReachable(ni, 25, msTimeout)){
                return true;
            }else{
                //System.out.println("FAILURE - ping " + ip);
                return false;
            }
        }
        catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            //System.out.println("error occurs.");
            //e.printStackTrace();
            return false;
        }
    }



}
