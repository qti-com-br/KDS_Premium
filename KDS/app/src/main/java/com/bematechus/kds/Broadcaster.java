package com.bematechus.kds;

import android.os.AsyncTask;

import com.bematechus.kdslib.Activation;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSSocketTCPCommandBuffer;
import com.bematechus.kdslib.KDSSocketUDP;
import com.bematechus.kdslib.KDSUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

/**
 * Broadcast global information or command
 */
public class Broadcaster {
    KDS m_kds = null;//use socket in KDS class

    public Broadcaster(KDS kds)
    {
        m_kds = kds;
    }

    public void setKDS(KDS kds)
    {
        m_kds = kds;
    }
    public KDS getKDS()
    {
        return m_kds;
    }

    public KDSSocketUDP getUDP()
    {
        return getKDS().getUDP();
    }
    public ByteBuffer makeAnnounceToRouterBuffer()
    {
        int port =getKDS().getOpenedOrderSourceIpPort();
        String strport = KDSUtil.convertIntToString(port);
        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildReturnStationIPCommand2(getKDS().getStationID(),getKDS().getLocalIpAddress(), strport,getKDS().getLocalMacAddress(),0, 0, Activation.getStoreGuid());
        return buf;
    }
    /**
     * tell router I am here.
     */
    private void broadcastAnnounceToRouter()
    {
//        int port = this.m_nPOSPort;
//        String strport = KDSUtil.convertIntToString(port);
//        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildReturnStationIPCommand(getStationID(), m_strLocalIP, strport, getLocalMacAddress());
        ByteBuffer buf = makeAnnounceToRouterBuffer();

        getUDP().broadcastData(KDSSettings.UDP_ROUTER_ANNOUNCER_PORT, buf);
    }

    public void broadcastClearDBCommand()
    {
        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildClearDBCommand();
        getUDP().broadcastData(buf);

    }

    public void broadcastItemBumpUnbump(KDSDataOrder order, boolean bBumped)
    {
        ArrayList<String> ar = new ArrayList<>();
        if (order == null) return;
        for (int i=0; i< order.getItems().getCount(); i++)
            ar.add(order.getItems().getItem(i).getItemName());
        broadcastItemBumpUnbump(order.getOrderName(), ar, bBumped);

    }
    public void broadcastItemBumpUnbump(String orderName, String itemName, boolean bBumped)
    {
        ArrayList<String> ar = new ArrayList<>();
        ar.add(itemName);
        broadcastItemBumpUnbump(orderName, ar, bBumped);

    }

    /**
     * for preparation time mode
     * @param orderName
     * @param itemNames
     * @param bBumped
     */
    public void broadcastItemBumpUnbump(String orderName, ArrayList<String> itemNames, boolean bBumped)
    {
        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildItemBumpUnbumpUdpCommand(orderName,itemNames, bBumped );

        (new KDSBroadcastThread( getUDP(), buf)).start();


    }

    public void broadcastQueueExpoDoubleBumpValue(boolean bEnabled)
    {
        (new KDSBroadcastThread(getUDP(), makeQueueExpoBumpSettingsBuffer(bEnabled))).start();

    }

    public ByteBuffer makeQueueExpoBumpSettingsBuffer(boolean bEnabled)
    {

        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildQueueExpoBumpSettingCommand( bEnabled);
        return buf;
    }

    public void broadcastRelations(String relations)
    {
        String s = "<Relations>";
        s += relations;
        s += "</Relations>";
        ByteBuffer buf =  KDSSocketTCPCommandBuffer.buildXMLCommand(s);

        getKDS().getUDP().broadcastData(buf);

        getKDS().getUDP().broadcastData(KDSSettings.UDP_ROUTER_ANNOUNCER_PORT, buf);


    }

    public Date broadcastRequireRelationsCommand()
    {

        Date dt = new Date();

        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildRequireRelationsCommand();
        getUDP().broadcastData(buf);
        getUDP().broadcastData(KDSSettings.UDP_ROUTER_ANNOUNCER_PORT, buf);
        return dt;
    }

    public void broadcastRequireStationsUDP()
    {

        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildRequireStationsCommand();
        getUDP().broadcastData(buf);


    }

    public void broadcastRequireStationsUDPInThread()
    {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                Broadcaster.this.broadcastRequireStationsUDP();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void broadcastShowStationID()
    {
        broadcastShowStationIDCommand();
        //broadcastShowStationIDCommandToRouter();
    }

    private void broadcastShowStationIDCommand()
    {

        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildShowStationIDCommand();
        getUDP().broadcastData(buf);

    }

    public void broadcastStationAnnounce()
    {
        ByteBuffer buf = makeStationAnnounceBuffer();
        getUDP().broadcastData(buf);
        broadcastAnnounceToRouter();
    }

    public ByteBuffer makeStationAnnounceBuffer()
    {
        int port = getKDS().getOpenedStationsCommunicatingPort();
        String strport = KDSUtil.convertIntToString(port);
        int nItemsCount = getKDS().getAllItemsCount();
        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildReturnStationIPCommand2(getKDS().getStationID(),getKDS().getLocalIpAddress(), strport, getKDS().getLocalMacAddress(),
                                                                                    nItemsCount, getKDS().getSettings().getInt(KDSSettings.ID.Users_Mode), Activation.getStoreGuid());
        return buf;
    }

    public void broadcastStationAnnounceInThread2()
    {


        (new KDSBroadcastThread(getUDP(), makeStationAnnounceBuffer())).start();

        // ByteBuffer buf = makeAnnounceToRouterBuffer();
        (new KDSBroadcastThread(getUDP(),KDSSettings.UDP_ROUTER_ANNOUNCER_PORT, makeAnnounceToRouterBuffer())).start();

    }

    public void broadcastTrackerBump(String orderName)
    {
        (new KDSBroadcastThread(getUDP(), makeTrackerBumpAnnounceBuffer(orderName))).start();

    }

    public ByteBuffer makeTrackerBumpAnnounceBuffer(String ordername)
    {

        String s = "<TTBump>" + ordername;

        ByteBuffer buf =  KDSSocketTCPCommandBuffer.buildXMLCommand(s);
        return buf;
    }

    /**
     *
     * @param relations
     * @param toIP
     * Formart: /ip:port
     *
     */
    public void broadcastRequireRelations(String relations, String toIP)
    {
        String s = "<RelationsRet>";
        s += relations;
        s += "</RelationsRet>";
        ByteBuffer buf =  KDSSocketTCPCommandBuffer.buildXMLCommand(s);
        String ip = parseRemoteUDPIP(toIP);
        String port = parseRemoteUDPPort(toIP);
        int nport = KDSSettings.UDP_ANNOUNCER_PORT;
        if (!port.isEmpty())
            nport = KDSUtil.convertStringToInt(port,KDSSettings.UDP_ANNOUNCER_PORT );
        getUDP().broadcastData(ip, nport, buf);


    }
    static public String parseRemoteUDPIP(String remoteIP)
    {
        String str = remoteIP;
        String port = "";
        String ip = "";
        str = str.replace("/", "");
        int n = str.indexOf(":");
        if (n >0) {
            ip = str.substring(0, n);
            port = str.substring(n+1);
        }
        return ip;
    }

    static public String parseRemoteUDPPort(String remoteIP)
    {
        String str = remoteIP;
        String port = "";
        String ip = "";
        str = str.replace("/", "");
        int n = str.indexOf(":");
        if (n >0) {
            ip = str.substring(0, n);
            port = str.substring(n+1);
        }
        return port;
    }


    public void broadcastSmartOrderEnabled(boolean bSmartEnabled)
    {
        (new KDSBroadcastThread(getUDP(), makeSmartModeEnabledSettingsBuffer(bSmartEnabled))).start();

    }

    public ByteBuffer makeSmartModeEnabledSettingsBuffer(boolean bSmartEnabled)
    {

        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildSmartModeEnabledSettingCommand( bSmartEnabled?1:0);
        return buf;
    }
}
