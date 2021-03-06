package com.bematechus.kdslib;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Administrator on 2015/9/29 0029.
 */
public class KDSStationsConnection {

    final String TAG =  "KDSStationsConnection";
    KDSMyStationRelations m_stationsRelations = new KDSMyStationRelations();

    ArrayList<KDSStationConnection> m_arPOSStations = new ArrayList<>() ; //POS connects to this station
    ArrayList<KDSStationConnection> m_arStatisticApp = new ArrayList<>() ; //POS connects to this station

    protected ArrayList<KDSStationConnection> m_arConnection = new ArrayList<>(); //connections
    //ArrayList<KDSStationConnection> m_arTimeoutConnecting = new ArrayList<>(); //connections can not connected(timeout). mainly purpose is offline data. Now we use m_buffersForWaitingConnection to save offline data
                                                                                 //so I comment it.

    protected NoConnectionDataBuffers m_buffersForWaitingConnection = new NoConnectionDataBuffers(); //KPP1-coke

    AckManager m_ackManager = new AckManager();//for ack of each xml file.

    KDSSocketManager m_socketManager = null;
    KDSSocketMessageHandler m_socketEventHandler = null;

    private Object m_activeStationsLocker = new Object();
    ArrayList<KDSStationActived> m_arActivedStations = new ArrayList<>(); //all actived station save here.

    //ArrayList<RouterStation> m_arRouterStations = new ArrayList<>(); //all actived station save here, * Used in Router app

    protected Object m_connectionLocker = new Object();

    Object m_activeLocker = new Object();
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    public KDSStationsConnection(KDSSocketManager manager, KDSSocketMessageHandler handler)
    {
        m_socketManager = manager;
        m_socketEventHandler = handler;
    }


    public KDSMyStationRelations getRelations()
    {
        return m_stationsRelations;
    }
    public void refreshRelations(Context context, String stationID)
    {
        m_stationsRelations.refreshRelations(context, stationID);

    }

    public ArrayList<KDSStationConnection> getPosStations()
    {
        return m_arPOSStations;
    }


    public ArrayList<KDSStationConnection> getStatisticAppStations()
    {
        return m_arStatisticApp;
    }


    private void disconnectStations(ArrayList<KDSStationConnection> stations)
    {
        int ncount = stations.size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationConnection station = stations.get(i);
            if (station.getSock() != null) {
                KDSLog.i(TAG, KDSLog._FUNCLINE_()+ "disconnectStations");
                station.getSock().close();
            }
        }

    }


    private void connectStations(ArrayList<KDSStationIP> stations)
    {
        if (stations == null) return;
        int ncount = stations.size();
        for (int i=0; i< ncount; i++)
        {
            if (stations.get(i) != null) //I find here can get null station. just filter it.
                connectToStation(stations.get(i));
        }
    }

    /**
     * get existed connection, this connection has two status: Connected, or connecting.
     * @param station
     * @return
     */
    public KDSStationConnection getConnection(KDSStationIP station)
    {
        synchronized (m_connectionLocker) {
            int ncount = m_arConnection.size();
            for (int i = 0; i < ncount; i++) {

                if (m_arConnection.get(i).getID().equals(station.getID()))
                    return m_arConnection.get(i);
            }
            return null;
        }
    }

    public  ArrayList<KDSStationConnection> getAllConnections()
    {
        return m_arConnection;
    }

    private KDSStationConnection getConnection(String ip)
    {
        synchronized (m_connectionLocker) {
            int ncount = m_arConnection.size();
            for (int i = 0; i < ncount; i++) {
                if (m_arConnection.get(i).getIP().equals(ip))
                    return m_arConnection.get(i);
            }
            return null;
        }
    }



    private void debug_info(String str)
    {
        m_socketEventHandler.sendInformation(str);
    }
    public KDSStationConnection connectToStation(KDSStationIP station)
    {

        if (station == null)
            return null;

        KDSStationConnection tcp = getConnection(station);
        if (tcp == null)
        {

            tcp = KDSStationConnection.fromIPStation(station);
            //copy the buffered data
//            KDSStationConnection timeoutConn =findStationByID(m_arTimeoutConnecting, station.getID());
//            if (timeoutConn != null)
//            {
////                if (timeoutConn.getBufferedCount() >0) //I use kdsstationsconnection-->m_buffersForWaitingConnection to save offline data.
////                {
////                    tcp.copyBufferedData(timeoutConn);
////                }
//                m_arTimeoutConnecting.remove(timeoutConn);
//            }
            //connect this station.
            if (connectConnection(tcp)) {
                //debug_info( " (connectStation(tcp)), add connection");
                synchronized (m_connectionLocker) {
                    m_arConnection.add(tcp);
                }
            }

        }
        else
        {
            //debug_info("find old connection");
            if (tcp.getSock().isConnected()) {
                // debug_info("old connection is connected");
                return tcp;
            }
            else
            {//connecting
                 //debug_info("old connection is connecting");
                if (tcp.getSock() instanceof KDSSocketTCPSideClient)
                {

                    if (tcp.isConnectionTimeout())
                    {
                         debug_info("old client connecting is timeout");
                        tcp.getSock().close();
                        if (connectConnection(tcp))
                        {
                            debug_info( "After timeout, connection build: #"+station.getID() + " ip="+station.getIP());
                            synchronized (m_connectionLocker) {
                                m_arConnection.add(tcp);
                            }
                        }
                    }
                }
                else if (tcp.getSock() instanceof KDSSocketTCPSideServer)
                {
                    // debug_info("old server connecting is timeout");
                    closeConnection(station);
                    tcp = KDSStationConnection.fromIPStation(station);
                    if (connectConnection(tcp)) {
                        synchronized (m_connectionLocker) {
                            m_arConnection.add(tcp);
                        }
                    }
                }
            }
        }
        return tcp;

    }
    public void closeConnection(KDSStationIP station)
    {
        closeConnection(station.getIP());

    }

    public void closeConnection(String ip)
    {
        KDSStationConnection tcp = getConnection(ip);
        if (tcp == null)
            return;
        KDSLog.i(TAG, KDSLog._FUNCLINE_()+"closeConnection");
        tcp.getSock().close();
        synchronized (m_connectionLocker) {
            m_arConnection.remove(tcp);
        }
    }
    //2015-12-29 change privte to protected
    protected boolean connectConnection(KDSStationConnection station)
    {

        String ip = station.getIP();// getStationIP(station);// station.getIP();
        String port = station.getPort();// getStationPort(station);

        //use active station settings. This is the latest setting
        KDSStationActived activeStation = this.findActivedStationByID(station.getID());
        if (activeStation != null) {
            ip = activeStation.getIP();
            port = activeStation.getPort();
            //update the station connection data.
            station.setIP(ip);
            station.setPort(port);

        }
        // debug_info("#" + station.getNumberString() + " ip=" + station.getIP() +" port="+station.getPort());

        if (port.isEmpty())
            return false;
        if (ip.isEmpty())
            return false;
        int nport = KDSUtil.convertStringToInt(port, -1);
        if (nport<0) return false;
        KDSSocketTCPSideClient client =null;
        boolean bCreateNew = false;
        if (station.getSock() != null) {
            client = (KDSSocketTCPSideClient) station.getSock();
        }
        else {
            bCreateNew = true;
            client = new KDSSocketTCPSideClient();
            client.setEventHandler(this.m_socketEventHandler);
            station.setSock(client);
        }
        if (client.isConnected())
            return true;
            //if (client.isConnecting())
        else //if it is not connected, it is connecting
        {
            if (!bCreateNew) //it is a existed station
            {
                if (station.isConnectionTimeout()) {
                    KDSLog.i(TAG, KDSLog._FUNCLINE_()+"connectConnection, station.isConnectionTimeout()");
                    station.getSock().close();
                }
                else
                    return true;
            }
        }

        station.setStartConnectionTime();
        client.connectTo(m_socketManager, ip, nport);
        return true;



    }


    /**
     *
     * @return
     *  the removed stations ID
     */
    public ArrayList<KDSStationActived> removeInactiveStations()
    {
        ArrayList<KDSStationActived> ar = new ArrayList<>();

        int ncount = 0;
        synchronized (m_activeLocker) {
            ncount = m_arActivedStations.size();
            for (int i = ncount - 1; i >= 0; i--) {
                if (m_arActivedStations.get(i).isTimeout(KDSConst.ACTIVE_PLUS_TIMEOUT)) {
                    ar.add(m_arActivedStations.get(i)); //backup it.
                    m_arActivedStations.remove(i);
                }

            }
        }

        for (int i=0; i< ar.size(); i++)
        {
            String id = ar.get(i).getID();
            String ip = ar.get(i).getIP();
            KDSLog.d(TAG,KDSLog._FUNCLINE_()+ "Fire lost active station event id=" +id +",ip=" + ip );
            if (m_socketEventHandler != null)
                m_socketEventHandler.sendLostAnnouncePulseMessage(id, ip);
            else
            {
                KDSLog.d(TAG,KDSLog._FUNCLINE_()+ "eventhandler == null, while fire lost active station event id=" +id +",ip=" + ip );
            }

        }

        return ar;
    }

    public void checkAllNoResponseStations()
    {
        int ncount = 0;
        ArrayList<KDSStationActived> arLostStations = removeInactiveStations();

        //check all connecting timeout station
        synchronized (m_connectionLocker)
        {
            ncount = m_arConnection.size();
            for (int i = ncount - 1; i >= 0; i--) {
                KDSStationConnection conn = m_arConnection.get(i);
                int nErrorCode = conn.isConnectionTimeoutWithErrorCode();

                String loginfo = "ID=" + conn.getID() + ",ip=" + conn.getIP()+",port="+ conn.getPort();
                //if (m_arConnection.get(i).isConnectionTimeout()) {
                if (nErrorCode != 0) {
                    //debug_info("remove timeout(" +KDSUtil.convertIntToString(nErrorCode) +") " + m_arConnection.get(i).getIP());

                    KDSLog.e(TAG, KDSLog._FUNCLINE_() + "connect timeout, remove it:"+loginfo);
                    conn.getSock().close();
//                    if (conn.getBufferedCount() >0)
//                        m_arTimeoutConnecting.add(conn); //save it, for next use. mainly purpose is offline data.
                    conn.getSock().freeCommandBuffer();
                    m_arConnection.remove(i);
                }
                else if (findActiveStationByIP(arLostStations, conn.getIP()) != null)
                {//lost
                    KDSLog.e(TAG, KDSLog._FUNCLINE_() + "station inactive, close it:"+loginfo);
                    conn.getSock().close();
//                    if (conn.getBufferedCount() >0)
//                        m_arTimeoutConnecting.add(conn); //save it, for later.mainly purpose is offline data.
                    //debug_info("remove "+m_arConnection.get(i).getIP() + " station lost");
                    conn.getSock().freeCommandBuffer();
                    m_arConnection.remove(i);
                }

            }
        }
    }

    public void clearAllActiveAnnouncer()
    {
        synchronized (m_activeLocker) {
            m_arActivedStations.clear();

        }
    }

    public KDSStationActived findActivedStationByID(String stationID)
    {
        //if (!m_stationsRelations.isEnabled(stationID)) return null;
        synchronized (m_activeLocker)  {
            int ncount = m_arActivedStations.size();
            for (int i = 0; i < ncount; i++) {
                if (m_arActivedStations.get(i).getID().equals(stationID))
                    return m_arActivedStations.get(i);
            }
            return null;
        }
    }

    public int findActivedStationCountByID(String stationID)
    {
        synchronized (m_activeLocker)  {
            int counter = 0;
            int ncount = m_arActivedStations.size();
            for (int i = 0; i < ncount; i++) {
                if (m_arActivedStations.get(i).getID().equals(stationID))
                    counter ++;
            }
            return counter;
        }
    }

    public KDSStationActived findActivedStationByIP(String ip)
    {

        synchronized (m_activeLocker)  {
            int ncount = m_arActivedStations.size();
            for (int i = 0; i < ncount; i++) {
                if (m_arActivedStations.get(i).getIP().equals(ip))
                    return m_arActivedStations.get(i);
            }
            return null;
        }
    }

    public KDSStationActived findActivedStationByIPAndPort(String ip, String strPort)
    {

        synchronized (m_activeLocker)  {
            int ncount = m_arActivedStations.size();
            for (int i = 0; i < ncount; i++) {
                if (m_arActivedStations.get(i).getIP().equals(ip) &&
                        m_arActivedStations.get(i).getPort().equals(strPort) )
                    return m_arActivedStations.get(i);
            }
            return null;
        }
    }

    public KDSStationActived findActivedStationByMac(String mac)
    {

        synchronized (m_activeLocker)  {
            int ncount = m_arActivedStations.size();
            for (int i = 0; i < ncount; i++) {
                if (m_arActivedStations.get(i).getMac().equals(mac))
                    return m_arActivedStations.get(i);
            }
            return null;
        }
    }
    public int getActiveStationsCount()
    {
        synchronized (m_activeLocker)  {
            return m_arActivedStations.size();


        }
    }

    public void addActiveStation(KDSStationActived station)
    {
        m_arActivedStations.add(station);
    }

    public void disconnectPOSConnections()
    {
        disconnectStations(m_arPOSStations);
    }




    public void connectAllStations()
    {
        // m_socksManager.stopThread();
        synchronized (m_connectionLocker) {
            //add offline stations in offlie database
            ArrayList<String> arOffline = m_buffersForWaitingConnection.getStationsOfflineAndAck();
            ArrayList<KDSStationIP> ar =m_stationsRelations.getAllStationsNeedToConnect(arOffline);

            connectStations(ar); //please notice the ar can contains null station!!!
        }
    }

    public  void closeAllStationsConnections()
    {
        synchronized (m_connectionLocker) {
            disconnectStations(m_arConnection);
            m_arConnection.clear();

        }

    }

    KDSStationActived findActiveStationByIP(ArrayList<KDSStationActived> ar, String ip)
    {
        if (ar == null)
            return null;
        synchronized (m_activeLocker) {
            int ncount = ar.size();
            for (int i = 0; i < ncount; i++) {
                if (ar.get(i).getIP().equals(ip))
                    return ar.get(i);
            }
            return null;
        }
    }

    KDSStationConnection findStationByIP(ArrayList<KDSStationConnection> ar, String ip)
    {
        if (ar == null)
            return null;
        int ncount = ar.size();
        for (int i=0; i< ncount; i++)
        {
            if (ar.get(i).getIP().equals(ip))
                return ar.get(i);
        }
        return null;
    }

    KDSStationConnection findStationByID(ArrayList<KDSStationConnection> ar, String strStationID)
    {
        if (ar == null)
            return null;
        int ncount = ar.size();
        for (int i=0; i< ncount; i++)
        {
            if (ar.get(i).getID().equals( strStationID))
                return ar.get(i);
        }
        return null;
    }

    public KDSStationConnection findConnectionByIP(String ip)
    {
        KDSStationConnection station = null;
        synchronized (m_connectionLocker) {
            station = findStationByIP(m_arConnection, ip);
        }
        if (station != null)
            return station;


        station = findStationByIP(m_arPOSStations, ip);
        if (station != null)
            return station;

        station = findStationByIP(m_arStatisticApp, ip);
        if (station != null)
            return station;

        return null;
    }


    /**
     *
     * @param strStationID
     * @return
     */
    public KDSStationConnection findConnectionByID(String strStationID)
    {


        synchronized (m_connectionLocker) {
            KDSStationConnection station = null;
            station = findStationByID(m_arConnection, strStationID);
            return station;
        }

    }

    /**
     * while connect to myself, the server side and client side is same IP
     * So, create this function
     * @param strStationID
     * @return
     */
//    KDSStationConnection findConnectionServerSideByID(String strStationID)
//    {
//        KDSStationConnection station = null;
//
//        synchronized (m_connectionLocker) {
//            if (m_arConnection == null)
//                return null;
//            int ncount = m_arConnection.size();
//            for (int i = 0; i < ncount; i++) {
//                if (m_arConnection.get(i).getID().equals(strStationID) &&
//                        (m_arConnection.get(i).getSock() instanceof KDSSocketTCPSideServer))
//                    return m_arConnection.get(i);
//            }
//            return null;
//        }
//
//    }

    /**
     *  * while connect to myself, the server side and client side is same IP
     * So, create this function
     * @param ip
     * @return
     */
//    KDSStationConnection findConnectionServerSideByIP(String ip)
//    {
//        KDSStationConnection station = null;
//
//        synchronized (m_connectionLocker) {
//            if (m_arConnection == null)
//                return null;
//            int ncount = m_arConnection.size();
//            for (int i = 0; i < ncount; i++) {
//                if (m_arConnection.get(i).getIP().equals(ip) &&
//                        (m_arConnection.get(i).getSock() instanceof KDSSocketTCPSideServer))
//                    return m_arConnection.get(i);
//            }
//            return null;
//        }
//    }

    KDSStationConnection findConnectionServerSideByIpAndPort(String ip, int nPort)
    {
        KDSStationConnection station = null;

        synchronized (m_connectionLocker) {
            if (m_arConnection == null)
                return null;
            int ncount = m_arConnection.size();
            for (int i = 0; i < ncount; i++) {
                if (m_arConnection.get(i).getIP().equals(ip) &&
                        (m_arConnection.get(i).getSock() instanceof KDSSocketTCPSideServer)) {
                    if ( ((KDSSocketTCPSideServer) m_arConnection.get(i).getSock()).getListenPort() == nPort)
                        return m_arConnection.get(i);
                }
            }
            return null;
        }
    }
//    /**
//     * find out how many connections for this IP.
//     * @param ip
//     * @return
//     */
//    int getConnectionCount(String ip)
//    {
//        synchronized (m_connectionLocker) {
//            int ncounter =0;
//            if (m_arConnection == null)
//                return ncounter;
//            int ncount = m_arConnection.size();
//            for (int i = 0; i < ncount; i++) {
//                if (m_arConnection.get(i).getIP().equals(ip) )
//                    ncounter++;
//
//            }
//            return ncounter;
//        }
//    }

    /**
     *  * while connect to myself, the server side and client side is same IP
     * So, create this function
     * @param strStationID
     * @return
     */
    public KDSStationConnection findConnectionClientSideByID(String strStationID)
    {
        KDSStationConnection station = null;
        synchronized (m_connectionLocker) {

            if (m_arConnection == null)
                return null;
            int ncount = m_arConnection.size();
            for (int i = 0; i < ncount; i++) {
                if (m_arConnection.get(i).getID().equals(strStationID) &&
                        (m_arConnection.get(i).getSock() instanceof KDSSocketTCPSideClient))
                    return m_arConnection.get(i);
            }
            return null;
        }

    }

    /**
     *  * while connect to myself, the server side and client side is same IP
     * So, create this function
     * @param ip
     * @return
     */
    KDSStationConnection findConnectionClientSideByIP(String ip)
    {
        KDSStationConnection station = null;
        synchronized (m_connectionLocker) {
            if (m_arConnection == null)
                return null;
            int ncount = m_arConnection.size();
            for (int i = 0; i < ncount; i++) {
                KDSStationConnection conn = m_arConnection.get(i);

                if (conn.getIP().equals(ip)) {
                    if (conn.getSock() instanceof KDSSocketTCPSideClient)
                        return m_arConnection.get(i);
                }
            }
            return null;
        }

    }


    /**
     * the connection just was build.
     * If there are any data buffered, send them.
     * @param kds
     *  for call back.
     * @param ip
     *
     */
    public void onIPConnected(KDSBase kds, String ip)
    {
        KDSStationConnection station = this.findConnectionClientSideByIP(ip);
        if (station == null)
            return;
        if (station.getSock().isConnected())
        {
            //send all no ack data to station,
            if (m_ackManager.isWaitingAck())
            {//send all again
                //AckDataStation ackStation = m_ackManager.getStation(station.getID());
                AckDataStation ackStation = m_ackManager.getTimeoutAck(station.getID(), -1);
                if (ackStation != null)
                {
                    synchronized (ackStation.m_locker)
                    {
                        try {
                            int ncount = ackStation.getData().size();
                            for (int i = 0; i < ncount; i++) {
                                station.getSock().writeXmlTextCommand(ackStation.getData().get(i).getWithAckXmlData());
                                ackStation.getData().get(i).resetTimer();
                            }
                        }catch (Exception e)
                        {
                            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
                        }
                    }
                }
            }
            startRestoreDataThread(kds, station, ip);

//            //send all offline data to station
//            NoConnectionDataBuffer buf = m_buffersForWaitingConnection.findStation(station.getID());
//
//            if (buf != null && buf.getData().size()>0)
//            {
//                //station.appendBufferedData(buf.getData());
//                //buf.getData().clear();
//                int n = buf.getData().size();//station.getBufferedCount();
//                for (int i=0; i< n; i++) {
//                    KDSStationDataBuffered data = buf.popup();//.getData().get(i);// station.popupStationBufferedData();
//                    if (data == null) break;
//                    if (!data.getData().isEmpty()) {
//                        String strXml = data.getData();
//                        String withAckXml = m_ackManager.add(station.getID(), strXml);
//                        station.getSock().writeXmlTextCommand(withAckXml);
//                        //station.getSock().writeXmlTextCommand(data.getData());
//                        kds.showMessage("write buffered to "+ ip);
//                        if (!data.getDescription().isEmpty() || data.isTransferOrderOfflineData())
//                        {
//                            kds.onFinishSendBufferedData(data);//.getDescription(), data.getOrderGuid());
//                        }
//                    }
//
//                }
//            }
//            m_buffersForWaitingConnection.remove(station.getID());
        }

        startCheckingAckThread();
    }

    KDSStationConnection findStationBySock(ArrayList<KDSStationConnection> ar,KDSSocketInterface sock )
    {
        if (ar == null)
            return null;
        int ncount = ar.size();
        for (int i=0; i< ncount; i++)
        {
            if (ar.get(i).getSock() == sock)
                return ar.get(i);
        }
        return null;
    }


    public void onIPDisconnected(KDSSocketInterface sock, String ip)
    {
        if (ip.isEmpty())
            return;
        KDSStationConnection station = null;
        synchronized (m_connectionLocker) {
            //find its station

            station = findStationBySock(m_arConnection, sock);//  findStationByIP(m_arConnection, ip);
            if (station != null) {
                station.getSock().close();
                KDSLog.i(TAG,KDSLog._FUNCLINE_()+ "onIPDisconnected1");
                m_arConnection.remove(station);
            }
            //remove the active stations,
            // 20180306 add this part code.
            if (station != null) {
                KDSStationIP activeStation = findActivedStationByIPAndPort(station.getIP(), station.getPort());
                if (activeStation != null)
                {
                    m_arActivedStations.remove(activeStation);
                }
            }
        }

        //accepted stations
        station =findStationBySock(m_arPOSStations, sock);// findStationByIP(m_arPOSStations, ip);
        if (station != null) {
            KDSLog.i(TAG, KDSLog._FUNCLINE_()+"onIPDisconnected2");
            station.getSock().close();
            m_arPOSStations.remove(station);
        }

        //accepted stations
        station =findStationBySock(m_arStatisticApp, sock);//
        if (station != null) {
            KDSLog.i(TAG, KDSLog._FUNCLINE_()+"onIPDisconnected3");
            station.getSock().close();
            m_arStatisticApp.remove(station);
        }
    }

    public void onAcceptPOSConnection(KDSSocketInterface sock, Object sockClient)
    {
        KDSSocketTCPSideServer c = (KDSSocketTCPSideServer)sockClient;
        String ip = c.getSavedRemoteIP();
        KDSStationConnection station = new KDSStationConnection();
        station.setIP(ip);
        station.setSock(c);

        ArrayList<KDSStationConnection> ar = null;

        ar = m_arPOSStations;//.add(station);


        synchronized (m_connectionLocker) {
            KDSStationConnection s = findStationByIP(ar, ip);
            if (s == null)
                ar.add(station);
            else {
                if (s.getSock() != null)
                    s.getSock().close();
                s.setSock(c);
            }
        }
    }


    public void onAcceptStatisticConnection(KDSSocketInterface sock, Object sockClient)
    {
        KDSSocketTCPSideServer c = (KDSSocketTCPSideServer)sockClient;
        String ip = c.getSavedRemoteIP();
        KDSStationConnection station = new KDSStationConnection();
        station.setIP(ip);
        station.setSock(c);

        ArrayList<KDSStationConnection> ar = null;

        ar = m_arStatisticApp;


        synchronized (m_connectionLocker) {
            KDSStationConnection s = findStationByIP(ar, ip);
            if (s == null)
                ar.add(station);
            else {
                if (s.getSock() != null)
                    s.getSock().close();
                s.setSock(c);
            }
        }
    }

    /**
     * 2015-12-29, changed it's return type from void to kdsstationconnection
     * While accept, the activestations array maybe contains some not timeout stations.
     * Check all activestations array, if no others connection of this station, remove this active station
     * And, let next pulse fire event.
     * @param sock
     * @param sockClient
     */
    public KDSStationConnection onAcceptStationConnection(KDSSocketInterface sock, Object sockClient)
    {
        KDSSocketTCPSideServer c = (KDSSocketTCPSideServer)sockClient;
        String ip = c.getSavedRemoteIP();

        KDSStationConnection station = new KDSStationConnection();
        station.setIP(ip);
        station.setSock(c);
        //2015-12-29
        station.setConnectionType(KDSStationConnection.ConnectionType.KDS_Station);
        //It can not find out its station ID, so don't set it at here.
        //ArrayList<KDSStationConnection> ar = null;

        // ar = m_arConnection;//.add(station);

        boolean bJustRestoreThisStation = false;
        KDSStationConnection s = null;
        synchronized (m_connectionLocker) {
            //the server and client side all in same array. At here, just check the server side.

            s =this.findConnectionServerSideByIpAndPort(ip, c.getListenPort());// findStation(ar, ip);

            if (s == null) {
                m_arConnection.add(station);
                s = station;
            }
            else {
                if (s.getSock() != null) {
                    if (s.getSock() != c) {
                        KDSLog.i(TAG,KDSLog._FUNCLINE_()+ "onAcceptStationConnection");
                        s.getSock().close();
                    }
                }
                s.setSock(c);
            }
            //set its ID
            KDSStationActived activeStation =  findActivedStationByIP(ip);
//            if (activeStation == null)
//                  bJustRestoreThisStation = true;
//            else
            if (activeStation != null)
                s.setID(activeStation.getID());

        }

        return s;


    }




    /**
     ** Used in Router app
     *  It seems is same as getFirstActiveBackupStation,
     *   Just keep this difference.
     * @param stationID
     *        Find this station's backup station.
     * @return
     *      return the ip,port .. TCPStation class
     *      Use it to do connection
     */
    private KDSStationIP getFirstActiveSlaveStation(String stationID)
    {
        String id = stationID;
        KDSStationIP station = null;
        while( true) {
            ArrayList<KDSStationIP> ar = KDSStationsRelation.findSlaveOfStation( m_stationsRelations.getRelationsSettings(), id);
            if (ar == null || ar.size() <=0) break;
            for (int i=0; i< ar.size(); i++) {
                String backupID =  ar.get(i).getID();

                if (this.findActivedStationByID(backupID) != null)
                {
                    return ar.get(i);
                }
                id = backupID;
            }


        }
        return station;
    }
    public boolean isActiveStation(KDSStationIP station)
    {
        return (findActivedStationByID(station.getID()) != null);

    }

    public  void connectStationWithData(KDSStationIP station, String strXml)
    {
        connectStationWithData(station, strXml, -1);
////        KDSStationConnection conn = KDSStationConnection.fromIPStation(station);
////
////        conn.addBufferedData(strXml);
////
////        if (connectConnection(conn)) {
////            synchronized (m_connectionLocker) {
////                //KDSStationConnection connExisted = findConnectionByID(station.getID());
////                m_arConnection.add(conn);
////            }
////        }
//
//
//        KDSStationConnection connection = getConnection(station);
//        if (connection == null)
//        {//this station is closed
//            if (this.isActiveStation(station ))
//            { //this station is active, but don't connect
//                KDSStationConnection conn = KDSStationConnection.fromIPStation(station);
//
//                conn.addBufferedData(strXml);
//
//                if (connectConnection(conn)) {
//                    synchronized (m_connectionLocker) {
//                        //KDSStationConnection connExisted = findConnectionByID(station.getID());
//                        m_arConnection.add(conn);
//                    }
//                }
//            }
//
//        }
//        else if (connection.getSock().isConnected())
//        {
//
//            connection.getSock().writeXmlTextCommand(strXml);
//
//        }
//        else if (!connection.getSock().isConnected())
//        {
//            connection.addBufferedData(strXml);
//        }

    }


    public boolean writeDataToStationOrItsBackup(KDSStationIP station, String strXml)
    {
        return writeDataToStationOrItsBackup(station, strXml,NoConnectionDataBuffers.MAX_BACKUP_DATA_COUNT);
//
//        KDSStationConnection connection = getConnection(station);
//        if (connection == null)
//        {//this station is closed
//            if (this.isActiveStation(station )
//                    && m_stationsRelations.isEnabled(station.getID())) //for enable
//            { //this station is active, but don't connect
//                connectStationWithData(station, strXml);
//            }
//            else
//            {//this station is not active, check its backup station.
//                String stationID = station.getID();
//                KDSStationIP backupStation =  getFirstActiveBackupStation(stationID);// getFirstActiveSlaveStation(stationID);
//                if (backupStation == null) return false;
//                if (!m_stationsRelations.isEnabled(backupStation.getID() ) )
//                    return false;
//                //find out its backup station and it is actived.
//                //check if there are existed connection.
//                KDSStationConnection backupConnection = findConnectionByID(backupStation.getID());
//                if (backupConnection == null)
//                {
//                    connectStationWithData(backupStation, strXml);
//                }
//                else if (!backupConnection.getSock().isConnected())
//                {
//                    backupConnection.addBufferedData(strXml);
//
//                }
//                else if (backupConnection.getSock().isConnected()) {
//                    backupConnection.getSock().writeXmlTextCommand(strXml);
//                }
//
//            }
//
//        }
//        else if (connection.getSock().isConnected())
//        {
//            if (m_stationsRelations.isEnabled(station.getID() ) )
//                connection.getSock().writeXmlTextCommand(strXml);
//            else
//            {
//                String stationID = station.getID();
//                KDSStationIP backupStation =getFirstActiveBackupStation(stationID);// getFirstActiveSlaveStation(stationID);
//                if (backupStation == null) return false;
//                if (!m_stationsRelations.isEnabled(backupStation.getID() ) )
//                    return false;
//                //find out its backup station and it is actived.
//                //check if there are existed connection.
//                KDSStationConnection backupConnection = findConnectionByID(backupStation.getID());
//                if (backupConnection == null)
//                {
//                    connectStationWithData(backupStation, strXml);
//                }
//                else if (!backupConnection.getSock().isConnected())
//                {
//                    backupConnection.addBufferedData(strXml);
//
//                }
//                else if (backupConnection.getSock().isConnected()) {
//                    backupConnection.getSock().writeXmlTextCommand(strXml);
//                }
//            }
//        }
//        else if (!connection.getSock().isConnected())
//        {
//            connection.addBufferedData(strXml);
//        }
//        return true;

    }


    /**
     * Used in Router APP,
     *  But it seems is same as writeDataToStationOrItsBackup. In Relations, one slave just have one
     *  role.
     *  For safe, I keep it.
     * @param station
     * @param strXml
     * @return
     */
    public boolean writeDataToStationOrItsSlave(KDSStationIP station, String strXml)
    {
        Log.i(TAG, "writeDataToStationOrItsSlave");
        return writeDataToStationOrItsSlave(station, strXml, -1);
//
//        KDSStationConnection connection = getConnection(station);
//        if (connection == null)
//        {//this station is closed
//            if (this.isActiveStation(station )
//                    && m_stationsRelations.isEnabled(station.getID())) //for enable
//            { //this station is active, but don't connect
//                connectStationWithData(station, strXml);
//            }
//            else
//            {//this station is not active, check its backup station.
//                String stationID = station.getID();
//                KDSStationIP backupStation = getFirstActiveSlaveStation(stationID);
//                if (backupStation == null) return false;
//                if (!m_stationsRelations.isEnabled(backupStation.getID() ) )
//                    return false;
//                //find out its backup station and it is actived.
//                //check if there are existed connection.
//                KDSStationConnection backupConnection = findConnectionByID(backupStation.getID());
//                if (backupConnection == null)
//                {
//                    connectStationWithData(backupStation, strXml);
//                }
//                else if (!backupConnection.getSock().isConnected())
//                {
//                    backupConnection.addBufferedData(strXml);
//
//                }
//                else if (backupConnection.getSock().isConnected()) {
//                    backupConnection.getSock().writeXmlTextCommand(strXml);
//                }
//
//            }
//
//        }
//        else if (connection.getSock().isConnected())
//        {
//            if (m_stationsRelations.isEnabled(station.getID() ) )
//                connection.getSock().writeXmlTextCommand(strXml);
//            else
//            {
//                String stationID = station.getID();
//                KDSStationIP backupStation = getFirstActiveSlaveStation(stationID);
//                if (backupStation == null) return false;
//                if (!m_stationsRelations.isEnabled(backupStation.getID() ) )
//                    return false;
//                //find out its backup station and it is actived.
//                //check if there are existed connection.
//                KDSStationConnection backupConnection = findConnectionByID(backupStation.getID());
//                if (backupConnection == null)
//                {
//                    connectStationWithData(backupStation, strXml);
//                }
//                else if (!backupConnection.getSock().isConnected())
//                {
//
//                    backupConnection.addBufferedData(strXml);
//
//                }
//                else if (backupConnection.getSock().isConnected()) {
//                    backupConnection.getSock().writeXmlTextCommand(strXml);
//                }
//            }
//        }
//        else if (!connection.getSock().isConnected())
//        {
//            connection.addBufferedData(strXml);
//        }
//        return true;

    }

    public boolean writeToExps(String myStationID, String strXml)
    {
        int ncount =m_stationsRelations.getExpStations().size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationIP station =  m_stationsRelations.getExpStations().get(i);
            if (station.getID().equals(myStationID)) continue;
            writeDataToStationOrItsBackup(station, strXml);
        }
        return true;
    }

    public boolean writeToMirrors(String myStationID, String strXml)
    {
        int ncount =m_stationsRelations.getMirrorStations().size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationIP station =  m_stationsRelations.getMirrorStations().get(i);
            if (station.getID().equals(myStationID)) continue;
            writeDataToStationOrItsBackup(station, strXml);
        }
        return true;
    }

    public boolean writeToBackups(String myStationID,String strXml)
    {
        int ncount =m_stationsRelations.getBackupStations().size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationIP station =  m_stationsRelations.getBackupStations().get(i);
            if (station.getID().equals(myStationID)) continue;
            writeDataToStationOrItsBackup(station, strXml);
        }
        return true;
    }

    public boolean writeToQueue(String myStationID, String strXml)
    {
        int ncount =m_stationsRelations.getQueueStations().size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationIP station =  m_stationsRelations.getQueueStations().get(i);
            if (station.getID().equals(myStationID)) continue;
            writeDataToStationOrItsBackup(station, strXml);
        }

        ncount =m_stationsRelations.getQueueExpoStations().size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationIP station =  m_stationsRelations.getQueueExpoStations().get(i);
            if (station.getID().equals(myStationID)) continue;
            writeDataToStationOrItsBackup(station, strXml);
        }

        return true;
    }

    public boolean writeToTT(String myStationID, String strXml)
    {
        int ncount =m_stationsRelations.getTTStations().size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationIP station =  m_stationsRelations.getTTStations().get(i);
            if (station.getID().equals(myStationID)) continue;
            writeDataToStationOrItsBackup(station, strXml);
        }
        return true;
    }

    public boolean writeToPrimaryMirror(String myStationID,String strXml)
    {
        int ncount =m_stationsRelations.getPrimaryStationsWhoUseMeAsMirror().size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationIP station =  m_stationsRelations.getPrimaryStationsWhoUseMeAsMirror().get(i);
            if (station.getID().equals(myStationID)) continue;
            writeDataToStationOrItsBackup(station, strXml);
        }
        return true;
    }

    /**
     *
     *  //If my primary backup station is the primary mirror of others,
     //check if this primary station is offline. If so, write data to the slave mirror of primary.
     * @param myStationID
     * @param strXml
     * @return
     */
    public boolean writeToMirrorOfPrimaryBackup(String myStationID, String toPrimaryOfBackupStationID, String strXml)
    {
        if (findConnectionByID(toPrimaryOfBackupStationID) != null) //primary of backup is active, don't send anything.
            return false;
        //check if it is the primary of mirror, if so, write to its mirror
        ArrayList<KDSStationIP> primaryBackupStations = this.getRelations().getItsMirrorStation(toPrimaryOfBackupStationID);

        int ncount = primaryBackupStations.size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationIP station = primaryBackupStations.get(i);
            writeDataToStationOrItsBackup(station, strXml);
        }
        return true;

    }

    public boolean writeToWorkLoan(String myStationID, String strXml)
    {
        int ncount =m_stationsRelations.getWorkLoadStations().size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationIP station =  m_stationsRelations.getWorkLoadStations().get(i);
            if (station.getID().equals(myStationID)) continue;
            writeDataToStationOrItsBackup(station, strXml);
        }
        return true;
    }

    public boolean writeToDuplicated(String myStationID, String strXml)
    {
        int ncount =m_stationsRelations.getDuplicatedStations().size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationIP station =  m_stationsRelations.getDuplicatedStations().get(i);
            if (station.getID().equals(myStationID)) continue;
            writeDataToStationOrItsBackup(station, strXml);
        }
        return true;
    }

    /**
     * check if there are any primary of backup is active
     * @return
     */
    public boolean isPrimaryBackupActive()
    {
        int ncount = m_stationsRelations.getPrimaryStationsWhoUseMeAsBackup().size();
        for (int i=0; i< ncount; i++)
        {
            String id = m_stationsRelations.getPrimaryStationsWhoUseMeAsBackup().get(i).getID();
            if (findActivedStationByID(id) != null)
                return true;
        }
        return false;
    }

    public boolean isMyPrimaryBackupStation(String stationID)
    {
        int ncount = m_stationsRelations.getPrimaryStationsWhoUseMeAsBackup().size();
        for (int i=0; i< ncount; i++)
        {
            String id = m_stationsRelations.getPrimaryStationsWhoUseMeAsBackup().get(i).getID();
            if (id.equals(stationID))
                return true;
        }
        return false;
    }
    public boolean isMyPrimaryMirrorStation(String stationID)
    {
        int ncount = m_stationsRelations.getPrimaryStationsWhoUseMeAsMirror().size();
        for (int i=0; i< ncount; i++)
        {
            String id = m_stationsRelations.getPrimaryStationsWhoUseMeAsMirror().get(i).getID();
            if (id.equals(stationID))
                return true;
        }
        return false;
    }

    public boolean isMyBackupStation(String stationID)
    {
        int ncount = m_stationsRelations.getBackupStations().size();
        for (int i=0; i< ncount; i++)
        {
            String id = m_stationsRelations.getBackupStations().get(i).getID();
            if (id.equals(stationID))
                return true;
        }
        return false;
    }
    public boolean isMyMirrorStation(String stationID)
    {
        int ncount = m_stationsRelations.getMirrorStations().size();
        for (int i=0; i< ncount; i++)
        {
            String id = m_stationsRelations.getMirrorStations().get(i).getID();
            if (id.equals(stationID))
                return true;
        }
        return false;
    }

    public boolean isMyWorkLoadStation(String stationID)
    {
        int ncount = m_stationsRelations.getWorkLoadStations().size();
        for (int i=0; i< ncount; i++)
        {
            String id = m_stationsRelations.getWorkLoadStations().get(i).getID();
            if (id.equals(stationID))
                return true;
        }
        return false;
    }

    public boolean isMyDuplicatedStation(String stationID)
    {
        int ncount = m_stationsRelations.getDuplicatedStations().size();
        for (int i=0; i< ncount; i++)
        {
            String id = m_stationsRelations.getDuplicatedStations().get(i).getID();
            if (id.equals(stationID))
                return true;
        }
        return false;
    }

    public boolean isMyQueueDisplayStationsExisted()
    {
        int ncount =m_stationsRelations.getQueueStations().size();
        if  (ncount >0)
            return true;
        ncount = m_stationsRelations.getQueueExpoStations().size();
        return (ncount >0);

    }

    /**
     * KPP1-7
     * @param stationID
     * @return
     */
    public boolean isMyQueueStation(String stationID)
    {
        int ncount = m_stationsRelations.getQueueStations().size();
        for (int i=0; i< ncount; i++)
        {
            String id = m_stationsRelations.getQueueStations().get(i).getID();
            if (id.equals(stationID))
                return true;
        }
        return false;
    }

    public boolean isMirrorOfOthers()
    {
        int ncount = m_stationsRelations.getPrimaryStationsWhoUseMeAsMirror().size();
        return (ncount >0);

    }

    public boolean isBackupOfOthers()
    {
        int ncount = m_stationsRelations.getPrimaryStationsWhoUseMeAsBackup().size();
        return (ncount >0);

    }

    public boolean isWorkLoadOfOthers()
    {
        int ncount = m_stationsRelations.getPrimaryStationsWhoUseMeAsWorkLoad().size();
        return (ncount >0);

    }
    public boolean isDuplicatedOfOthers()
    {
        int ncount = m_stationsRelations.getPrimaryStationsWhoUseMeAsDuplicated().size();
        return (ncount >0);

    }

    /**
     * check if there are any primary of mirror is active
     * @return
     */
    public boolean isPrimaryWhoUseMeAsMirrorActive()
    {
        int ncount = m_stationsRelations.getPrimaryStationsWhoUseMeAsMirror().size();
        for (int i=0; i< ncount; i++)
        {
            String id = m_stationsRelations.getPrimaryStationsWhoUseMeAsMirror().get(i).getID();
            if (findActivedStationByID(id) != null)
                return true;
        }
        return false;
    }

    /**
     * update the station number, port number and so on information in
     * connections array.
     */
    public void refreshAllExistedConnectionInfo()
    {
        synchronized (m_connectionLocker)
        {

            int ncount = m_arConnection.size();
            for (int i=0; i< ncount; i++)
            {
                KDSStationConnection conn = m_arConnection.get(i);
                if (conn.getID().isEmpty())
                {
                    String ip = conn.getIP();
                    KDSStationActived activeAnnounce =  findActivedStationByIP(ip);
                    if (activeAnnounce == null)
                        continue;
                    conn.setID(activeAnnounce.getID());

                }
            }
        }
    }

    /**
     *
     * @param stationID
     *        Find this station's backup station.
     * @return
     *      return the ip,port .. TCPStation class
     *      Use it to do connection
     */
    public ArrayList<String> getActivedBackupStations(String stationID)
    {
        String id = stationID;

        ArrayList<String> arBackupStations = new ArrayList<>();


        ArrayList<KDSStationIP> ar = KDSStationsRelation.findSlaveOfStation( m_stationsRelations.getRelationsSettings(), id);
        if (ar == null || ar.size() <=0) return arBackupStations;
        for (int i=0; i< ar.size(); i++) {
            String backupID =  ar.get(i).getID();

            if (this.findActivedStationByID(backupID) != null)
            {
                arBackupStations.add(backupID);
            }

        }

        return arBackupStations;
    }

    public KDSStationActived findActivedStationByMacAndPort(String mac,String port)
    {
        synchronized (m_activeLocker)  {
            int ncount = m_arActivedStations.size();
            for (int i = 0; i < ncount; i++) {
                if (m_arActivedStations.get(i).getMac().equals(mac)) {
                    if (m_arActivedStations.get(i).getPort().equals(port))
                        return m_arActivedStations.get(i);
                }
            }
            return null;
        }
    }




    private boolean isCheckedID(ArrayList<String> arCheckedID, String stationID)
    {
        for (int i=0; i< arCheckedID.size();i++)
        {
            if (arCheckedID.get(i).equals(stationID))
                return true;
        }
        return false;
    }
    //    /**
//     *
//     * @param stationID
//     *        Find this station's backup station.
//     * @return
//     *      return the ip,port .. TCPStation class
//     *      Use it to do connection
//     */
    private KDSStationIP getFirstActiveBackupStation(String stationID)
    {
        String id = stationID;
        KDSStationIP station = null;
        ArrayList<String> arCheckedID = new ArrayList<>();

        for (int nloop=0; nloop< 100; nloop++)
        {
            if (isCheckedID(arCheckedID, id)) //loop
                return null;
            ArrayList<KDSStationIP> ar = KDSStationsRelation.findBackupOfStation( m_stationsRelations.getRelationsSettings(), id);
            if (ar == null || ar.size() <=0) break;
            for (int i=0; i< ar.size(); i++) {
                String backupID =  ar.get(i).getID();

                if (this.findActivedStationByID(backupID) != null)
                {
                    return ar.get(i);
                }
                arCheckedID.add(id);

                id = backupID;
                if (id.equals(stationID))
                    return null; //loop
            }


        }
        return station;
    }
    public KDSStationConnection getConnection(KDSSocketInterface sock)
    {

        synchronized (m_connectionLocker) {
            int ncount = m_arConnection.size();
            for (int i = 0; i < ncount; i++) {
                if (m_arConnection.get(i).getSock() == sock)
                    return m_arConnection.get(i);
            }

        }
        return null;
    }


    public int getWhoUseMeAsExpo(String expoStationID)
    {
        int ncount = m_stationsRelations.getPrepStationsWhoUseMeAsExpo(expoStationID).size();
        return ncount;


    }

    public ArrayList<KDSStationIP> getAllActiveStations()
    {
        ArrayList<KDSStationIP> ar = new ArrayList<>();
        synchronized (m_activeLocker) {
            for (int i = 0; i < m_arActivedStations.size(); i++) {
                ar.add(m_arActivedStations.get(i));
            }
        }
        return ar;
    }

    public void connectAllActivedStations()
    {
        // m_socksManager.stopThread();
        synchronized (m_connectionLocker) {
            ArrayList<KDSStationIP> ar =m_stationsRelations.getAllStationsNeedToConnect();
            connectStations(ar);
        }
    }

    /**
     *
     * @param station
     * @param strXml
     * @param nMaxBufferCount
     *  -1: no limitation
     * @return
     */
    public boolean writeDataToStationOrItsBackup(KDSStationIP station, String strXml, int nMaxBufferCount)
    {
        KDSStationConnection connection = getConnection(station);
        if (connection == null)
        {//this station is closed
            if (this.isActiveStation(station )
                    && m_stationsRelations.isEnabled(station.getID())) //for enable
            { //this station is active, but don't connect
                connectStationWithData(station, strXml,nMaxBufferCount);
            }
            else
            {//this station is not active, check its backup station.
                String stationID = station.getID();
                KDSStationIP backupStation =  getFirstActiveBackupStation(stationID);// getFirstActiveSlaveStation(stationID);
                //check if slave is queue station, if it is queue/expo, backup offline orders. 20190531
                if (backupStation != null) {
                    if (m_stationsRelations.isQueueExpoStation(backupStation.getID()))
                        m_buffersForWaitingConnection.add(station.getID(), strXml, nMaxBufferCount);
                }

                if (backupStation == null) {
                    //still write to primary station
                    Log.i(TAG, "Write to station: " + station);
                    connectStationWithData(station, strXml,nMaxBufferCount);//KPP1-Coke
                    return false;
                }
                if (!m_stationsRelations.isEnabled(backupStation.getID() ) )
                    return false;
                //find out its backup station and it is actived.
                //check if there are existed connection.
                KDSStationConnection backupConnection = findConnectionByID(backupStation.getID());
                if (backupConnection == null)
                {
                    connectStationWithData(backupStation, strXml,nMaxBufferCount);
                    //check if slave is queue station, if it is queue/expo, backup offline orders.20190531
//                    if (m_stationsRelations.isQueueExpoStation(backupStation.getID()))
//                        m_buffersForWaitingConnection.add(station.getID(), strXml, nMaxBufferCount);

                }
                else if (!backupConnection.getSock().isConnected())
                {
                    m_buffersForWaitingConnection.add(backupStation.getID(), strXml, nMaxBufferCount);
                    //backupConnection.addBufferedData(strXml,nMaxBufferCount);

                }
                else if (backupConnection.getSock().isConnected()) {
                    String withAckXml = m_ackManager.add(backupStation.getID(), strXml);
                    backupConnection.getSock().writeXmlTextCommand(withAckXml);
                    //backupConnection.getSock().writeXmlTextCommand(strXml);
                    //check if slave is queue station, if it is queue/expo, backup offline orders. 20190531
//                    if (m_stationsRelations.isQueueExpoStation(backupStation.getID()))
//                        m_buffersForWaitingConnection.add(station.getID(), strXml, nMaxBufferCount);
                }

            }

        }
        else if (connection.getSock().isConnected())
        {
            if (m_stationsRelations.isEnabled(station.getID() ) ||
                    (!m_stationsRelations.isExistedInRelationshipTable(station.getID())) //kpp1-171,active, but it is not in relation table.
            ) {
                String withAckXml = m_ackManager.add(station.getID(), strXml);
                connection.getSock().writeXmlTextCommand(withAckXml);
                //connection.getSock().writeXmlTextCommand(strXml);
            }
            else
            {
                String stationID = station.getID();
                KDSStationIP backupStation =getFirstActiveBackupStation(stationID);// getFirstActiveSlaveStation(stationID);
                if (backupStation == null) return false;
                if (!m_stationsRelations.isEnabled(backupStation.getID() ) )
                    return false;
                //find out its backup station and it is actived.
                //check if there are existed connection.
                KDSStationConnection backupConnection = findConnectionByID(backupStation.getID());
                if (backupConnection == null)
                {
                    connectStationWithData(backupStation, strXml,nMaxBufferCount);
                }
                else if (!backupConnection.getSock().isConnected())
                {
                    m_buffersForWaitingConnection.add(backupStation.getID(), strXml, nMaxBufferCount);
                    //backupConnection.addBufferedData(strXml,nMaxBufferCount);

                }
                else if (backupConnection.getSock().isConnected()) {
                    String withAckXml = m_ackManager.add(backupStation.getID(), strXml);
                    backupConnection.getSock().writeXmlTextCommand(withAckXml);
                    //backupConnection.getSock().writeXmlTextCommand(strXml);
                }
            }
        }
        else if (!connection.getSock().isConnected())
        {
            m_buffersForWaitingConnection.add(station.getID(), strXml, nMaxBufferCount);
            //connection.addBufferedData(strXml,nMaxBufferCount);
        }
        return true;

    }

    /**
     *
     * @param station
     * @param strXml
     * @param nMaxBufferCount
     *  -1: no limitation
     */
    public  void connectStationWithData(KDSStationIP station, String strXml, int nMaxBufferCount)
    {

        KDSStationConnection connection = getConnection(station);
        if (connection == null)
        {//this station is closed
            if (this.isActiveStation(station )) //KPP1-Coke, No matter station existed or not, just buffer data.
            { //this station is active, but don't connect
                KDSStationConnection conn = KDSStationConnection.fromIPStation(station);
                m_buffersForWaitingConnection.add(station.getID(), strXml, nMaxBufferCount);
                //conn.addBufferedData(strXml,nMaxBufferCount);

                if (connectConnection(conn)) {
                    synchronized (m_connectionLocker) {
                        //KDSStationConnection connExisted = findConnectionByID(station.getID());
                        m_arConnection.add(conn);
                    }
                }
            }
            else
            {//just buffer data
                Log.i(TAG, "backup data :" + station);
                m_buffersForWaitingConnection.add(station.getID(), strXml, nMaxBufferCount);
            }

        }
        else if (connection.getSock().isConnected())
        {
            String withAckXml = m_ackManager.add(station.getID(), strXml);
            connection.getSock().writeXmlTextCommand(withAckXml);
            //connection.getSock().writeXmlTextCommand(strXml);

        }
        else if (!connection.getSock().isConnected())
        {
            //connection.addBufferedData(strXml,nMaxBufferCount);
            m_buffersForWaitingConnection.add(station.getID(), strXml, nMaxBufferCount);
        }

    }


    /**
     * it should not used
     * @param station
     * @param strXml
     * @param nMaxBufferCount
     * @return
     */
    public boolean writeDataToStationOrItsSlave(KDSStationIP station, String strXml, int nMaxBufferCount)
    {
        KDSStationConnection connection = getConnection(station);
        if (connection == null)
        {//this station is closed
            Log.i(TAG, "connection == null");
            if (this.isActiveStation(station )
                    && m_stationsRelations.isEnabled(station.getID())) //for enable
            { //this station is active, but don't connect
                Log.i(TAG, "connectStationWithData");
                connectStationWithData(station, strXml,nMaxBufferCount);
            }
            else
            {//this station is not active, check its backup station.
                Log.i(TAG, "it is not active station");
                String stationID = station.getID();
                KDSStationIP backupStation = getFirstActiveSlaveStation(stationID);
                if (backupStation == null) {
                    m_buffersForWaitingConnection.add(backupStation.getID(), strXml, nMaxBufferCount);
                    return false;
                }
                if (!m_stationsRelations.isEnabled(backupStation.getID() ) )
                    return false;
                //find out its backup station and it is actived.
                //check if there are existed connection.
                KDSStationConnection backupConnection = findConnectionByID(backupStation.getID());
                if (backupConnection == null)
                {
                    connectStationWithData(backupStation, strXml ,nMaxBufferCount);
                }
                else if (!backupConnection.getSock().isConnected())
                {
                    m_buffersForWaitingConnection.add(backupStation.getID(), strXml, nMaxBufferCount);
                    //backupConnection.addBufferedData(strXml);

                }
                else if (backupConnection.getSock().isConnected()) {
                    String withAckXml = m_ackManager.add(backupStation.getID(), strXml);
                    backupConnection.getSock().writeXmlTextCommand(withAckXml);
                    //backupConnection.getSock().writeXmlTextCommand(strXml);
                }

            }

        }
        else if (connection.getSock().isConnected())
        {
            Log.i(TAG, "connection.getSock().isConnected()");
            if (m_stationsRelations.isEnabled(station.getID() ) ) {
                Log.i(TAG, "connection != null 1");
                String withAckXml = m_ackManager.add(station.getID(), strXml);
                connection.getSock().writeXmlTextCommand(withAckXml);
                //connection.getSock().writeXmlTextCommand(strXml);
            }
            else
            {
                Log.i(TAG, "connection != null 2");
                String stationID = station.getID();
                KDSStationIP backupStation = getFirstActiveSlaveStation(stationID);
                if (backupStation == null) return false;
                if (!m_stationsRelations.isEnabled(backupStation.getID() ) )
                    return false;
                //find out its backup station and it is actived.
                //check if there are existed connection.
                KDSStationConnection backupConnection = findConnectionByID(backupStation.getID());
                if (backupConnection == null)
                {
                    connectStationWithData(backupStation, strXml ,nMaxBufferCount);
                }
                else if (!backupConnection.getSock().isConnected())
                {
                    m_buffersForWaitingConnection.add(backupStation.getID(), strXml, nMaxBufferCount);
                    //backupConnection.addBufferedData(strXml);

                }
                else if (backupConnection.getSock().isConnected()) {
                    String withAckXml = m_ackManager.add(backupStation.getID(), strXml);
                    backupConnection.getSock().writeXmlTextCommand(withAckXml);
                    //backupConnection.getSock().writeXmlTextCommand(strXml);
                }
            }
        }
        else if (!connection.getSock().isConnected())
        {
            Log.i(TAG, "(!connection.getSock().isConnected())");
            m_buffersForWaitingConnection.add(station.getID(), strXml, nMaxBufferCount);
            //connection.addBufferedData(strXml);
        }
        return true;

    }


//    /**
//     * * Used in Router app
//     */
//    public void routerReset()
//    {
//        m_arRouterStations.clear();
//    }
//
//    /**
//     * * Used in Router app
//     * @param router
//     */
//    public void routerAdd(RouterStation router)
//    {
//        m_arRouterStations.add(router);
//    }
//
//    /**
//     * * Used in Router app
//     * @param routerID
//     * @return
//     */
//    public boolean routerExisted(String routerID)
//    {
//        for (int i=0; i< m_arRouterStations.size(); i++)
//        {
//            if (m_arRouterStations.get(i).getID().equals(routerID))
//                return true;
//        }
//        return false;
//    }
//
//    /**
//     * * Used in Router app
//     * @param routerID
//     * @param ip
//     * @return
//     */
//    public RouterStation routerFind(String routerID, String ip)
//    {
//        for (int i=0; i< m_arRouterStations.size(); i++)
//        {
//            if (m_arRouterStations.get(i).getID().equals(routerID) &&
//                    m_arRouterStations.get(i).getIP().equals(ip) )
//                return m_arRouterStations.get(i);
//        }
//        return null;
//    }
//
//    /**
//     * * Used in Router app
//     * @return
//     */
//    public int routerCount()
//    {
//        return m_arRouterStations.size();
//    }
//
//    /**
//     * * Used in Router app
//     */
//    public void routerRemoveInactive()
//    {
//       // ArrayList<KDSStationActived> ar = new ArrayList<>();
//
//        int ncount = 0;
//        //synchronized (m_activeLocker)
//        {
//            ncount = m_arRouterStations.size();
//            for (int i = ncount - 1; i >= 0; i--) {
//                if (m_arRouterStations.get(i).isTimeout(KDSConst.ACTIVE_PLUS_TIMEOUT)) {
//                    //ar.add(m_arRouterStations.get(i)); //backup it.
//                    m_arRouterStations.remove(i);
//                }
//
//            }
//        }
//
//
//    }
//
//    /**
//     * * Used in Router app
//     */
//    public void routerCheckAllNoResponse()
//    {
//        routerRemoveInactive();
//
//    }
//
//    /**
//     * * Used in Router app
//     * @param myStationIP
//     * @return
//     */
//    public boolean routerOtherEnabled(String myStationIP)
//    {
//        routerCheckAllNoResponse();
//        for (int i=0; i< m_arRouterStations.size(); i++)
//        {
//            if (m_arRouterStations.get(i).getIP().equals(myStationIP) )
//                continue;
//            if (m_arRouterStations.get(i).getBackupMode())
//                continue;
//            if (m_arRouterStations.get(i).getEnabled())
//                return true;
//
//        }
//        return false;
//    }

    public NoConnectionDataBuffers getNoConnectionBuffer()
    {
        return m_buffersForWaitingConnection;
    }

    public void onReceiveAckXml(String stationID, String ackGuid)
    {
        //Log.i(TAG, "onReceiveAckXml:" + stationID +","+ackGuid);
        m_ackManager.removeAck(stationID, ackGuid);

//        AckDataStation station =  m_ackManager.getStation(stationID);
//        station.remove(ackGuid);
        //Log.i(TAG, "onReceiveAckXml:count=" + station.getData().size());
    }

    /**
     *
     * @param sock
     * @param xmlData
     * @return
     *  return string that removed ack guid
     */
    public String responseAck(String myStationID, String myIPAddress, String myMac, KDSSocketInterface sock, String xmlData)
    {
        //<ACKGUID>92348529384592834598234</ACKGUID>
        String ackguid = AckData.parseAckGuid(xmlData);
        if (ackguid.isEmpty())
            return xmlData;

        xmlData = AckData.removeAckGuid(xmlData, ackguid);
        KDSStationConnection conn = getConnection(sock);
        if (conn == null)
        {
            conn = getPOSConnection(sock); //try server listen (POS) connection again.
        }
        if (conn!= null)
        {

            //conn.getSock().writeXmlTextCommand(KDSXMLParserCommand.createXmlAck(myStationID, myIPAddress, myMac, ackguid));
            conn.getSock().writeXmlTextCommand(KDSXMLParserCommand.createQuickXmlAck(myStationID, myIPAddress, myMac, ackguid));
        }

        return xmlData;
    }

    public KDSStationConnection getPOSConnection(KDSSocketInterface sock)
    {

        synchronized (m_connectionLocker) {
            int ncount = m_arPOSStations.size();
            for (int i = 0; i < ncount; i++) {
                if (m_arPOSStations.get(i).getSock() == sock)
                    return m_arPOSStations.get(i);
            }

        }
        return null;
    }

    Thread m_threadCheckingAck = null;

    /**
     * Move some timer functions to here.
     * Just release main UI.
     * All feature in this thread are no ui drawing request.
     * And, in checkautobumping function, it use message to refresh UI.
     */
    public void startCheckingAckThread()
    {
        if (m_threadCheckingAck == null ||
                !m_threadCheckingAck.isAlive())
        {
            m_threadCheckingAck = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true)
                    {
                        if (m_threadCheckingAck != Thread.currentThread())
                            return;
                        if (!m_ackManager.isWaitingAck() )
                        {
                            try {
                                Thread.sleep(1000);
                                continue;
                            } catch (Exception e) {

                            }
                        }
                        try {

                            checkTimeoutAck();
                            Thread.sleep(1000);

                        }
                        catch ( Exception e)
                        {
                            KDSLog.e(TAG,KDSLog._FUNCLINE_(), e);
                            //e.printStackTrace();
                        }

                    }
                }
            });
            m_threadCheckingAck.setName("CheckingAck");
            m_threadCheckingAck.start();
        }
    }

    final int BATCH_ACK_COUNT = 3;
//    private void checkTimeoutAck()
//    {
//        ArrayList<AckData> arTimeout = new ArrayList<>();
//        for (int i=0; i< m_ackManager.getStationsCount(); i++)
//        {
//            AckDataStation station = m_ackManager.getStations().get(i);
//            arTimeout.clear();
//            station.findTimeoutData(arTimeout, BATCH_ACK_COUNT);
//            if (arTimeout.size() >0)
//            {
//                resendXmlData(station, arTimeout);
//            }
//            arTimeout.clear();
//
//        }
//    }

    private void checkTimeoutAck()
    {
        //ArrayList<AckData> arTimeout = new ArrayList<>();
        ArrayList<String> arStations = m_ackManager.getAckStations();

        for (int i=0; i< arStations.size(); i++)
        {
            String stationID = arStations.get(i);

            AckDataStation station = m_ackManager.getTimeoutAck(stationID, BATCH_ACK_COUNT);

            if (station.getData().size() >0)
            {
               // Log.i(TAG, "--->Resend data <---");
               // Log.i(TAG, station.getStationID());
                resendXmlData(station);
            }


        }
    }

    private void resendXmlData(AckDataStation station, ArrayList<AckData> arTimeout)
    {
        String stationID = station.getStationID();
        KDSStationConnection conn = findConnectionByID(stationID);
        if (conn == null)
            return;
        if (!conn.isConnected())
            return;
        for (int i=0; i< arTimeout.size(); i++) {
            conn.getSock().writeXmlTextCommand(arTimeout.get(i).getWithAckXmlData());
            arTimeout.get(i).resetTimer();

        }
    }

    private void resendXmlData(AckDataStation station)
    {

        String stationID = station.getStationID();
        KDSStationConnection conn = findConnectionByID(stationID);
        if (conn == null)
            return;
        if (!conn.isConnected())
            return;
        for (int i=0; i< station.getData().size(); i++) {
            conn.getSock().writeXmlTextCommand(station.getData().get(i).getWithAckXmlData());
            m_ackManager.resetAckTime(station.getData().get(i).getGuid());
        }

        station.getData().clear();

    }


    ArrayList<RestoreData> m_arRestoring = new ArrayList<>();
    /**
     * If there are many data waiting the connection, it will lock others operation.
     * So, I move them to a new thread.
     */
    public void startRestoreDataThread(KDSBase kds, KDSStationConnection station, String ip)
    {
        synchronized (m_arRestoring) {
            for (int i = 0; i < m_arRestoring.size(); i++) {
                if (m_arRestoring.get(i).getStationID().equals(station.getID()))
                    return;
            }
        }
        RestoreData d = new RestoreData(kds, station, ip);
        synchronized (m_arRestoring) {
            m_arRestoring.add(d);
        }
        Thread t = new Thread(d);
        t.setName("RstData#" + station.getID());
        t.start();

    }

    class RestoreData implements Runnable
    {
        KDSStationConnection m_station = null;
        KDSBase m_kds = null;
        String m_ip = "";
        public String getStationID()
        {
            if (m_station == null) return "";
            return m_station.getID();
        }
        public RestoreData(KDSBase kds, KDSStationConnection station, String ip)
        {
            m_ip = ip;
            m_kds = kds;
            m_station = station;
        }

        @Override
        public void run() {


                if (m_station == null || m_kds == null)
                    return;
                try {
//                    //send all no ack data to station,
//                    if (m_ackManager.isWaitingAck()) {//send all again
//                        //AckDataStation ackStation = m_ackManager.getStation(station.getID());
//                        AckDataStation ackStation = m_ackManager.getTimeoutAck(m_station.getID(), -1);
//                        if (ackStation != null) {
//                            synchronized (ackStation.m_locker) {
//                                try {
//                                    int ncount = ackStation.getData().size();
//                                    for (int i = 0; i < ncount; i++) {
//                                        m_station.getSock().writeXmlTextCommand(ackStation.getData().get(i).getWithAckXmlData());
//                                        ackStation.getData().get(i).resetTimer();
//                                    }
//                                } catch (Exception e) {
//                                    KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
//                                }
//                            }
//                        }
//                    }
                    //send all offline data to station
                    NoConnectionDataBuffer buf = m_buffersForWaitingConnection.findStation(m_station.getID());

                    if (buf != null && buf.getData().size() > 0) {
                        //station.appendBufferedData(buf.getData());
                        //buf.getData().clear();
                        int n = buf.getData().size();//station.getBufferedCount();
                        for (int i = 0; i < n; i++) {
                            KDSStationDataBuffered data = buf.popup();//.getData().get(i);// station.popupStationBufferedData();
                            if (data == null) break;
                            if (!data.getData().isEmpty()) {
                                String strXml = data.getData();
                                String withAckXml = m_ackManager.add(m_station.getID(), strXml);
                                m_station.getSock().writeXmlTextCommand(withAckXml);
                                //station.getSock().writeXmlTextCommand(data.getData());
                                m_kds.showMessage("write buffered to " + m_ip);
                                if (!data.getDescription().isEmpty() || data.isTransferOrderOfflineData()) {
                                    m_kds.onFinishSendBufferedData(data);//.getDescription(), data.getOrderGuid());
                                }
                            }

                        }
                    }
                    m_buffersForWaitingConnection.remove(m_station.getID());
                }
                catch (Exception e)
                {
                    KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
                }
                finally {
                    synchronized (m_arRestoring) {
                        m_arRestoring.remove(this);
                    }
                }

        }
    }

    public  KDSStationIP getMyQueueStation()
    {
        KDSStationIP station = null;
        int ncount =m_stationsRelations.getQueueStations().size();
        if  (ncount <= 0) {
            ncount = m_stationsRelations.getQueueExpoStations().size();
            if (ncount <=0)
                return null;
            else
                station =  m_stationsRelations.getQueueExpoStations().get(0);
        }
        else {
            station = m_stationsRelations.getQueueStations().get(0);
        }
        return station;
    }

    public boolean isMyQueueOnline()
    {
        KDSStationIP station = getMyQueueStation();
        if (station == null) return false;

        if (this.getConnection(station) == null)
            return false;
        boolean bOnline =  (this.getConnection(station).isConnected());
        return bOnline;


    }
    public boolean isThereOfflineData(String stationID)
    {
        NoConnectionDataBuffer buf = m_buffersForWaitingConnection.findStation(stationID);
        if (buf == null) return false;
        return (buf.getSize() >0);

    }

    /**
     * clear all buffered offline data
     * KPP1-144
     * @return
     */
    public boolean clear()
    {
        m_ackManager.clear();
        m_buffersForWaitingConnection.clear();
        return true;
    }
    public boolean writeToStations(String myStationID,ArrayList<KDSStationIP> arStations, String strXml)
    {
        int ncount = arStations.size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationIP station =  arStations.get(i);
            if (station.getID().equals(myStationID)) continue;
            writeDataToStationOrItsBackup(station, strXml);
        }
        return true;
    }

    public boolean writeGhostStionDataToBuffer(String toStationID, String xmlData, int nMaxBufferCount)
    {
        m_buffersForWaitingConnection.add(toStationID, xmlData, nMaxBufferCount);
        return true;
    }

    public boolean dataIsWaitingConnection(String stationID)
    {
        return m_buffersForWaitingConnection.stationHasOfflineData(stationID);
    }

    /**
     * kpp1-449,
     * @param myStationID
     * @param strXml
     * @return
     */
    public boolean writeToPrimaryOfExpo(String myStationID,String strXml)
    {
        ArrayList<KDSStationIP> arPrep =m_stationsRelations.getPrepStationsWhoUseMeAsExpo(myStationID);

        for (int i=0; i< arPrep.size(); i++)
        {
            KDSStationIP station =  arPrep.get(i);
            if (station.getID().equals(myStationID)) continue;
            writeDataToStationOrItsBackup(station, strXml);
        }
        return true;
    }

    /**
     * kp-96
     * @param stationID
     * @param nMaxCount
     * @param nTimeout
     * @return
     */
    public AckDataStation popupTimeoutAck(String stationID,  int nMaxCount ,int nTimeout)
    {
        AckDataStation data = m_ackManager.getTimeoutAck(stationID, nMaxCount, nTimeout);
        for (int i=0; i< data.getData().size(); i++)
            m_ackManager.removeAck(stationID, data.getData().get(i).getGuid());
        return data;

    }

    /**
     *
     * @param kds
     */
    public void checkDirtyOfflineData(KDSBase kds)
    {
        ArrayList<String> arOffline = m_buffersForWaitingConnection.getStationsOffline();//.getStationsOfflineAndAck();
        if (arOffline.size() <=0) return;
        ArrayList<KDSStationIP> ar =m_stationsRelations.getStations(arOffline);

        int ncount = ar.size();
        for (int i=0; i< ncount; i++)
        {
            if (ar.get(i) != null) //I find here can get null station. just filter it.
            {
                KDSStationConnection tcp = getConnection(ar.get(i).getIP());
                if (tcp == null)
                    continue;
                if (tcp.isConnected()) {
                    Log.i(TAG, "dirty offline data");
                    startRestoreDataThread(kds, tcp, tcp.getIP());
                }
            }
        }
    }

    public boolean writeToSummary(String myStationID, String strXml)
    {
        ArrayList<KDSStationIP> arSummary = m_stationsRelations.getSummaryStations();

        int ncount = arSummary.size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationIP station =  arSummary.get(i);
            if (station.getID().equals(myStationID)) continue;
            writeDataToStationOrItsBackup(station, strXml);
        }

        return true;
    }
}


