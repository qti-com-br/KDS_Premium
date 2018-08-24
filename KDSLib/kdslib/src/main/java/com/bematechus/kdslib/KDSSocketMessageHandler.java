package com.bematechus.kdslib;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 *
 * For thread safe, use this handler to do socket events
 *  Android don't allow gui work in thread.
 */
public class KDSSocketMessageHandler extends Handler
{
    static final public int SOCK_MSG_CONNECTED = 1;
    static final public int SOCK_MSG_DISCONNECTED = 2;
    static final public int SOCK_MSG_RECEIVE = 3;
    static final public int SOCK_MSG_XML = 4;
    static final public int SOCK_MSG_ACCEPT = 5;
    static final public int SMB_MSG_XML = 6;
    static final public int Anounce_Lost_Pulse = 7;
    static final public int SOCK_INFO = 8;
    static final public int SOCK_MSG_WRITE_DONE = 9;

    KDSSocketEventReceiver m_eventReceiver = null;
    public  KDSSocketMessageHandler(KDSSocketEventReceiver receiver)
    {
        m_eventReceiver = receiver;
    }
    public void sendConnectedMessage(Object objParam)
    {
        Message m = new Message();
        m.what =  SOCK_MSG_CONNECTED;
        m.obj = objParam;
        this.sendMessage(m);
    }

    public void sendWriteDoneMessage(Object objSock, String remoteIP, int nWrote)
    {
        Message m = new Message();
        m.what =  SOCK_MSG_WRITE_DONE;
        m.obj = objSock;
        Bundle b = new Bundle();
        b.putInt("len", nWrote);
        b.putString("ip", remoteIP);
        m.setData(b);

        this.sendMessage(m);
    }

    public void sendInformation(String objStr)
    {
        Message m = new Message();
        m.what =  SOCK_INFO;
        m.obj = objStr;
        this.sendMessage(m);
    }

    public void sendDisconnectedMessage(Object objParam)
    {
        Message m = new Message();
        m.what =  SOCK_MSG_DISCONNECTED;
        m.obj = objParam;
        this.sendMessage(m);
    }

    /**
     *
     * @param objServer
     *  Who accept connection
     * @param objClient
     *  Create what new socket to do talk.
     */
    public void sendAccept(Object objServer, Object objClient)
    {
        Message m = new Message();
        m.what =  SOCK_MSG_ACCEPT;
        Bundle b = new Bundle();
        ArrayList list = new ArrayList();//use this list to pass data
        ArrayList<Object> ar = new ArrayList<>();
        ar.add(objClient);
        list.add(ar);
        b.putParcelableArrayList("data", list);
        m.obj = objServer;
        m.setData(b);

        this.sendMessage(m);
    }

    public void sendReceiveDataMessage(Object objParam, String remoteIP, ByteBuffer buffer, int nLength)
    {
        Message m = new Message();
        m.what =  SOCK_MSG_RECEIVE;
        m.obj = objParam;
        Bundle b = new Bundle();
        b.putInt("len", nLength);
        b.putByteArray("data", buffer.array());
        b.putString("ip", remoteIP);
        m.setData(b);
        this.sendMessage(m);
    }


    /**
     * 20170612, use this thread to do order operation!!!!!
     * @param objParam
     * @param strXml
     */
    public void sendReceiveXmlMessage(Object objParam, String strXml)
    {
        Message m = new Message();
        m.what =  SOCK_MSG_XML;
        m.obj = objParam;
        Bundle b = new Bundle();
        b.putString("xml", strXml);
        m.setData(b);
        this.sendMessage(m);
    }

    public void sendLostAnnouncePulseMessage(String stationID, String stationIP)
    {
        Message m = new Message();
        m.what = Anounce_Lost_Pulse;

        Bundle b = new Bundle();
        b.putString("id", stationID);
        b.putString("ip", stationIP);
        m.setData(b);
        this.sendMessage(m);
    }

    /**
     * 20170612
     * @param objParam
     * @param strXml
     */
    public void sendReceiveSmbXmlMessage(Object objParam,String smbFileName, String strXml)
    {
        Message m = new Message();
        m.what =  SMB_MSG_XML;
        m.obj = objParam;
        Bundle b = new Bundle();
        b.putString("smbfile", smbFileName);
        b.putString("xml", strXml);
        m.setData(b);
        this.sendMessage(m);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SOCK_MSG_CONNECTED:
            {
                Object obj = msg.obj;
                m_eventReceiver.sockevent_onTCPConnected((KDSSocketInterface)obj);
            }
            break;
            case SOCK_MSG_DISCONNECTED:
            {
                Object obj = msg.obj;
                m_eventReceiver.sockevent_onTCPDisconnected((KDSSocketInterface) obj);
            }
            break;
            case SOCK_MSG_RECEIVE:
            {
                Object obj = msg.obj;
                Bundle b = msg.getData();
                int nLength = b.getInt("len");
                byte[] bytes = b.getByteArray("data");
                String ip = b.getString("ip");
                ByteBuffer buf = ByteBuffer.wrap(bytes);
                m_eventReceiver.sockevent_onReceiveData((KDSSocketInterface) obj, ip, buf, nLength );

            }
            break;
            case SOCK_MSG_XML:
            {
                Object obj = msg.obj;
                Bundle b = msg.getData();
                String xml = b.getString("xml");
                m_eventReceiver.sockevent_onTCPReceiveXml((KDSSocketInterface) obj, xml);
            }
            break;
            case SMB_MSG_XML:
            {
                Object obj = msg.obj;
                Bundle b = msg.getData();
                String xml = b.getString("xml");
                String smbFileName = b.getString("smbfile");

                m_eventReceiver.smbevent_onSMBReceiveXml((KDSSMBDataSource) obj,smbFileName, xml);
            }
            break;
            case SOCK_MSG_ACCEPT:
            {
                Object objServer = msg.obj;
                Bundle b = msg.getData();
                ArrayList list = b.getParcelableArrayList("data");
                ArrayList<Object> ar =(ArrayList<Object>) list.get(0);//
                Object objClient = ar.get(0);//
                m_eventReceiver.sockevent_onTCPAccept((KDSSocketInterface) objServer, objClient);
            }
            break;
            case Anounce_Lost_Pulse:
            {
                Bundle b = msg.getData();
                String id = b.getString("id");
                String ip = b.getString("ip");
                m_eventReceiver.announce_lost_pulse(id, ip);

            }
            break;
            case SOCK_INFO:
            {
                String info = (String)(msg.obj);

                m_eventReceiver.one_socket_information(info);

            }
            break;
            case SOCK_MSG_WRITE_DONE:
            {
                Object obj = msg.obj;
                Bundle b = msg.getData();
                int nLength = b.getInt("len");

                String ip = b.getString("ip");

                m_eventReceiver.sockevent_onWroteDataDone((KDSSocketInterface) obj, ip, nLength );
            }
            default:
                break;

        }
    }
}
