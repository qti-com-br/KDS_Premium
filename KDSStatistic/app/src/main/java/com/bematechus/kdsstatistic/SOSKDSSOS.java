package com.bematechus.kdsstatistic;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSBase;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSSMBDataSource;
import com.bematechus.kdslib.KDSSocketEventReceiver;
import com.bematechus.kdslib.KDSSocketInterface;
import com.bematechus.kdslib.KDSSocketManager;
import com.bematechus.kdslib.KDSSocketMessageHandler;
import com.bematechus.kdslib.KDSSocketTCPCommandBuffer;
import com.bematechus.kdslib.KDSSocketTCPListen;
import com.bematechus.kdslib.KDSSocketTCPSideClient;
import com.bematechus.kdslib.KDSSocketTCPSideServer;
import com.bematechus.kdslib.KDSSocketUDP;
import com.bematechus.kdslib.KDSStationActived;
import com.bematechus.kdslib.KDSStationConnection;
import com.bematechus.kdslib.KDSStationDataBuffered;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSStationsConnection;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXMLParserCommand;
import com.bematechus.kdslib.SOSReportCondition;
import com.bematechus.kdslib.SOSReportOneStation;
import com.bematechus.kdslib.SOSStationConfig;
import com.bematechus.kdslib.TimeDog;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

//import com.bematechus.kdslib.ConditionStatistic;
//import com.bematechus.kdslib.KDSKbdRecorder;

/**
 * Created by Administrator on 2015/12/20 0020.
 */
public class SOSKDSSOS extends KDSBase implements KDSSocketEventReceiver, Runnable,SOSReportCreatorEvents {

    private final String TAG = "KDSSOS";

    public interface KDSSOSEvents
    {
        void onStationConnected(String ip, KDSStationConnection conn);
        void onStationDisconnected(String ip);
        /**
         * tell my parent, I received report.
         * Mainly, the gui will use this to refresh gui.
         */
        void onReceiveReport(String stationID, SOSReportOneStation report);

        void onWaitingReport(String stationID);
    }


//    public interface KDSSOSRemoteEvents
//    {
//
//    }

    public interface StationAnnounceEvents
    {
        void onReceivedStationAnnounce(String stationID, String ip, String port, String mac);
    }

//    ArrayList<KDSSOSRemoteEvents> m_remoteReportEventsReceivers = new ArrayList<>();
//
//    public void setRemoteReportEventsReceiver(KDSSOSRemoteEvents receiver)
//    {
//        for (int i=0; i< m_remoteReportEventsReceivers.size(); i++)
//        {
//            if (m_remoteReportEventsReceivers.get(i) == receiver) return;
//        }
//        m_remoteReportEventsReceivers.add(receiver);
//    }

    public String getStationID()
    {
        return "Statistic";
    }

    /********************************************************************************************/

    int m_nListenViewerPort = SOSSettings.TCP_DEFAULT_REPORT_VIEWER_PORT;//
    // broadcast udp message
    KDSSocketUDP m_udpStationAnnouncer = new KDSSocketUDP();
    //all socket checking
    KDSSocketManager m_socksManager = new KDSSocketManager();
    //listen which pos want to connect to me
    KDSSocketTCPListen m_listenViewer;
    //use it receive event, and
    KDSSocketMessageHandler m_sockEventsMessageHandler  = new KDSSocketMessageHandler(this);; //socket events

    Context m_context = null; //application context

    Object m_locker = new Object();

    ArrayList<KDSSOSEvents> m_arEventsReceiver = new ArrayList<>();//null; //KDS events
    StationAnnounceEvents m_stationAnnounceEvents = null;
    SOSStationsConnection m_stationsConnection = new SOSStationsConnection(m_socksManager,m_sockEventsMessageHandler );
    KDSSocketTCPCommandBuffer m_udpBuffer = new KDSSocketTCPCommandBuffer();

    SOSSettings m_settings = null; //this the root all others settings pointer

    String m_strLocalIP = "";
    String m_strLocalMAC = "";
    boolean m_bConnectAllActiveStations = false;

//    ReportCreator m_reportAutoCreator = new ReportCreator();
//    ReportCreator m_reportRemoteCreator = new ReportCreator();

    SOSReportCreators m_reportCreators = new SOSReportCreators();


    public SOSReportCreators getReportCreators()
    {
        return m_reportCreators;
    }
    public KDSStationsConnection getStationsConnections()
    {
        return m_stationsConnection;
    }

    /**
     *
     * @param context
     * application context
     */
    public SOSKDSSOS(Context context)
    {
        m_settings = new SOSSettings(context);// settings;
        m_settings.loadSettings(context);
        m_context = context;

        m_listenViewer = new KDSSocketTCPListen();
        m_listenViewer.setEventHandler(m_sockEventsMessageHandler);

        m_reportCreators.setEventsReceiver(this);

        updateSettings(m_settings);

    }

    public void updateSettings()
    {
        m_settings.loadSettings(m_context);
        updateSettings(m_settings);
    }


    public  void updateSettings(SOSSettings settings)
    {

        KDSLog.setLogLevel(settings.getInt(SOSSettings.ID.Log_mode));

        m_stationsConnection.clearAllActiveAnnouncer(); //reset it.
        broadcastRequireStationsUDPInThread();
        m_stationsConnection.closeAllStationsConnections();
        int nPort = settings.getInt(SOSSettings.ID.Data_Viewer_IPPort);
        boolean bPosPortChanged = false;
        if (nPort != m_nListenViewerPort)
            bPosPortChanged = true;
        m_nListenViewerPort = nPort;
        if (bPosPortChanged)
        {
            if (m_listenViewer.isListening())
            {
                m_listenViewer.stop();
                m_stationsConnection.disconnectPOSConnections();
            }
        }

        startPOSListener();

    }
    public void setEventReceiver(KDSSOSEvents receiver)
    {
        int ncount = m_arEventsReceiver.size();
        for (int i= 0; i< ncount; i++)
        {
            if (m_arEventsReceiver.get(i) == receiver)
                return;
        }

        m_arEventsReceiver.add(receiver);

    }



    /**
     * check connection in a loop
     */
    public void on1sTimer()
    {
        checkNetworkState();

    }

    boolean m_bNetworkActived = true;
    private void checkNetworkState()
    {
        if (KDSSocketManager.isNetworkActived(this.m_context)) {
            if (!m_bNetworkActived)
                onNetworkRestored();
            m_bNetworkActived = true;
        }
        else{

            if (m_bNetworkActived)
                onNetworkLost();
            m_bNetworkActived = false;
        }
    }

    private void onNetworkRestored()
    {
        this.startNetwork();

    }
    private void onNetworkLost()
    {
        this.stopNetwork();

    }

    public boolean startPOSListener()
    {

        stopPOSListener();
        m_listenViewer.startServer(m_nListenViewerPort, m_socksManager, m_sockEventsMessageHandler);
        return true;
    }
    public void stopPOSListener()
    {
        m_listenViewer.stop();
    }

    public void refreshIPandMAC()
    {
        ArrayList<String> ar = KDSSocketManager.getLocalIpAddressWithMac();
        if (ar.size() <=0) {
            m_strLocalIP = "";
            m_strLocalMAC = "";
        }
        else
        {
            m_strLocalIP = ar.get(0);
            m_strLocalMAC = ar.get(1);
        }
    }

    public void stopNetwork()
    {
        m_socksManager.stopThread();
        m_udpStationAnnouncer.close();
        stopPOSListener();;

        m_stationsConnection.closeAllStationsConnections();
        m_stationsConnection.disconnectPOSConnections();
        stopPingThread();
    }


    public void startNetwork()
    {
        refreshIPandMAC();

        m_socksManager.startThread();
        m_udpStationAnnouncer.start(SOSSettings.UDP_STATISTIC_RECEIVE_ALIVE_ANNOUNCER_PORT, m_sockEventsMessageHandler, m_socksManager);

        //let others stations know me as soon as possible.
        this.broadcastRequireStationsUDPInThread();
        startPOSListener();
        this.broadcastRequireStationsUDPInThread();

        startPingThread();
        this.broadcastRequireStationsUDPInThread();


    }

    public boolean start( )//KDSSettings settings)
    {
        if (isRunning()) return true;
        Log.d(TAG, KDSLog._FUNCLINE_()+"start enter");
        refreshIPandMAC();

        updateSettings(m_settings);

        checkStationsSettingChanged(m_context);
        startNetwork();

        Log.d(TAG, KDSLog._FUNCLINE_()+"start exit");
        return true;

    }


    public void stop()
    {
        stopPingThread();
        synchronized (m_locker) {
            m_listenViewer.stop();
            m_stationsConnection.closeAllStationsConnections();
            m_stationsConnection.disconnectPOSConnections();
        }

        stopNetwork();

    }
    public boolean isRunning()
    {
        return m_bRunning;
    }
    /**
     * UDP receive data here.
     * STX, stationID, stationIP, port, ETX
     * @param sock
     * @param remoteIP
     * @param buffer
     * @param nLength
     */
    public void sockevent_onReceiveData(KDSSocketInterface sock, String remoteIP, ByteBuffer buffer, int nLength)
    {
        if (sock instanceof KDSSocketUDP)
            onUdpReceiveData(sock, remoteIP, buffer, nLength);
    }

    public String getLocalIpAddress()
    {
        if (m_strLocalIP.isEmpty())
            refreshIPandMAC();
        return m_strLocalIP;
    }

    public ArrayList<Byte> getLocalIp()
    {
        ArrayList<Byte> ip = new ArrayList<>();

        String s = getLocalIpAddress();

        ArrayList<String> ar = KDSUtil.spliteString(s, "\\.");
        for (int i=0; i< ar.size(); i++)
        {
            String str = ar.get(i);
            int n = KDSUtil.convertStringToInt(str, -1);
            ip.add((byte)n);

        }
        return ip;

    }

    public String getLocalMacAddress()
    {
        return m_strLocalMAC;
    }

    public void broadcastRequireStationsUDPInThread()
    {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                SOSKDSSOS.this.broadcastStatisticRequireStationsUDP();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void onUdpReceiveData(KDSSocketInterface sock, String remoteIP, ByteBuffer buffer, int nLength) {
        m_udpBuffer.appendData(buffer, nLength);

        while (true) {
            m_udpBuffer.skip_to_STX();
            if (m_udpBuffer.length() <= 0)
                return;

            byte command = m_udpBuffer.command();
            if (command == 0)
                return;
            switch (command) {
                case KDSSocketTCPCommandBuffer.UDP_RET_STATION: {//the command send by xml format
                    //1. parse the xml text
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)
                        return; //need more data

                    byte[] bytes = m_udpBuffer.station_info_command_data();
                    m_udpBuffer.remove(ncommand_end);
                    String utf8 = KDSUtil.convertUtf8BytesToString(bytes);
                    onUdpReceiveStationAnnounce(utf8);

                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_REQ_STATION: {
                    int ncommand_end = m_udpBuffer.command_end();
                    m_udpBuffer.remove(ncommand_end);

                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_SHOW_ID:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    m_udpBuffer.remove(ncommand_end);

                }
                break;
                case KDSSocketTCPCommandBuffer.XML_COMMAND:
                {//
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)
                        return; //need more data

                    byte[] bytes = m_udpBuffer.xml_command_data();
                    m_udpBuffer.remove(ncommand_end);
                    String utf8 = KDSUtil.convertUtf8BytesToString(bytes);
                    doUdpXmlCommand(utf8);
                }
                break;
//                case KDSSocketTCPCommandBuffer.UDP_ASK_RELATIONS:
//                {
//
//                }
//                break;
//                case KDSSocketTCPCommandBuffer.UDP_ASK_BROADCAST_RELATIONS:
//                {
//                }
//                break;
                default: {
                    m_udpBuffer.remove(1);
                    break;
                }
            }
        }
    }

    /**
     *  UDP get xml command string
     * @param xmlCommand
     *  Format:
     *      <Relations>Strings .... </Relations>
     */
    public void doUdpXmlCommand(String xmlCommand)
    {

    }



    /**
     * id,ip,port string
     * @param strInfo
     */
    private void onUdpReceiveStationAnnounce(String strInfo)
    {
        boolean bNewStation = false;
        //remove all failed stations.
        m_stationsConnection.checkAllNoResponseStations();

        ArrayList<String> ar = KDSUtil.spliteString(strInfo, ",");
        if (ar.size() < 4)
            return;
        String id = ar.get(0);
        String ip = ar.get(1);
        String port = ar.get(2);
        String mac = ar.get(3);

        // *********************** IMPORTANT ************************
        //As router will check router and kds two app, we have to use port to find it.
        KDSStationActived station =m_stationsConnection.findActivedStationByMacAndPort(mac, port);//id); ///IMPORTANT
        if (station == null) {
            station = new KDSStationActived();
            station.setID(id);
            station.setIP(ip);
            station.setPort(port);
            station.setMac(mac);
            station.updatePulseTime();//record last received time
            //m_arActivedStations.add(station);
            m_stationsConnection.addActiveStation(station);
            bNewStation = true;
        }
        station.setID(id);
        station.setIP(ip);
        station.setPort(port);
        station.setMac(mac);
        station.updatePulseTime();//record last received time

        //some connection don't have the station ID in it. use this function to update them.
        //comment it for debuging the connect with data function.
        m_stationsConnection.refreshAllExistedConnectionInfo();

        if (m_stationAnnounceEvents != null)
            m_stationAnnounceEvents.onReceivedStationAnnounce(id, ip, port, mac);
        if (bNewStation)
            announce_restore_pulse(id, ip);

    }

    /**
     * tcp client connect to server
     * @param sock
     */
    public void sockevent_onTCPConnected(KDSSocketInterface sock)
    {

        String ip = getSocketIP(sock);

        KDSStationConnection conn = getSocketConnection(sock);
        for (int i=0; i< m_arEventsReceiver.size(); i++)
            m_arEventsReceiver.get(i).onStationConnected(ip, conn);

        m_stationsConnection.onIPConnected(this, ip);

    }
    public KDSStationConnection getSocketConnection(KDSSocketInterface sock)
    {
        return m_stationsConnection.getConnection(sock);
    }
    /**
     * call back from  m_stationsConnection.onIPConnected
     */
    public void onFinishSendBufferedData(KDSStationDataBuffered data)//(String strDescription, String orderGuid)
    {
//        if (!orderGuid.isEmpty())
//        { //it is for transfering order
//
//        }
    }

    public void setStationAnnounceEventsReceiver(StationAnnounceEvents receiver)
    {
        m_stationAnnounceEvents = receiver;
    }

    public void sockevent_onTCPDisconnected(KDSSocketInterface sock)
    {

        String ip = getSocketIP(sock);

        if (ip.isEmpty())
            return;
        m_stationsConnection.onIPDisconnected(sock,ip);

        for (int i=0; i< m_arEventsReceiver.size(); i++)
            m_arEventsReceiver.get(i).onStationDisconnected(ip);
    }

    public void sockevent_onUDPReceiveXml(KDSSocketInterface sock, String xmlData)
    {

    }
    public void sockevent_onWroteDataDone(KDSSocketInterface sock, String remoteIP,  int nLength)
    {

    }
    public void smbevent_onSMBReceiveXml(KDSSMBDataSource smb,String smbFileName, String xmlData)
    {

    }
    public void one_socket_information(String strInfo)
    {

        this.showMessage(strInfo);
    }
    /**
     * interface implement
     * ip lost its announce pulse
     * @param stationID
     * @param stationIP
     */
    public void announce_lost_pulse(String stationID, String stationIP)
    {
        if (stationID.isEmpty()) return;
        onStationLost(stationID, stationIP);


    }

    /**
     * 2015-12-30
     * @param stationID
     * @param stationIP
     */
    public void onStationLost(String stationID, String stationIP)
    {
        String s = m_context.getString(R.string.station_lost);// "Station #" + stationID + " lost";
        s = s.replace("#", stationID);
        showMessage(s);
    }

    /**
     * 2015-12-30
     * @param stationID
     * @param stationIP
     */
    public void onStationRestore(String stationID, String stationIP)
    {
        //String s = "Station #" + stationID + " restored";
        String s = m_context.getString(R.string.station_restored);//
        s = s.replace("#", stationID);
        showMessage(s);
    }


    /** 2015-12-30
     * The station/ip don't receive the udp announce in 10secs
     * @param stationID
     * @param stationIP
     */
    public void announce_restore_pulse(String stationID, String stationIP)
    {

        onStationRestore(stationID, stationIP);

    }

    private String getSocketIP(KDSSocketInterface sock)
    {
        String ip = "";
        if (sock instanceof KDSSocketTCPSideClient) {
            KDSSocketTCPSideClient c = (KDSSocketTCPSideClient) sock;
            ip = c.getConnectToWhatIP();

        }
        else if (sock instanceof KDSSocketTCPSideServer)
        {
            KDSSocketTCPSideServer c = (KDSSocketTCPSideServer) sock;
            ip = c.getSavedRemoteIP();

        }
        return ip;
    }
    public void sockevent_onTCPReceiveXml(KDSSocketInterface sock, String xmlData)
    {
        KDSXMLParser.XMLType ntype = checkXmlType(xmlData);

        switch (ntype)
        {
            case Unknown:
                return;
            case Order:
                //doOrderXml(sock, xmlData);
                break;

            case Command:
                doCommandXml(sock, xmlData);
                break;
            case Feedback_OrderStatus:
            {
                //doFeedbackOrderStatus(xmlData);
            }
            break;
            case Report_Requirement: //PC viewer ask report
            {
                doRequireReport(sock, xmlData);
            }
            default:
                break;
        }
    }


    /**
     * POS ask the sos report
     * @param sock
     * @param xmlData
     */
    public void doRequireReport(KDSSocketInterface sock, String xmlData)
    {
        SOSReportCondition condition = SOSXMLParserAskReport.parseReportCondition(xmlData);
        condition.setTag(sock);
        //m_reportCreator.setTag(sock);
        m_reportCreators.createReport(condition);

    }


    /**
     *
     * @param sock
     *  The listen server
     * @param sockClient
     *   The accept kdsockettcpsideserver
     */
    public void sockevent_onTCPAccept(KDSSocketInterface sock, Object sockClient)
    {
        KDSSocketTCPSideServer c = (KDSSocketTCPSideServer)sockClient;
        String ip = c.getSavedRemoteIP();

        if (sock == m_listenViewer) {
            m_stationsConnection.onAcceptPOSConnection(sock, sockClient);
            c.setListenPort(this.getSettings().getInt(SOSSettings.ID.Data_Viewer_IPPort));
        }



    }

    public void checkStationsSettingChanged(Context context)
    {
        m_stationsConnection.refreshRelations(context, this.getStationID());

    }



    private KDSXMLParser.XMLType checkXmlType(String strxml)
    {
        return KDSXMLParser.checkXmlType(strxml);

    }

    private void doCommandXml(KDSSocketInterface sock, String xmlData) {
        KDSXMLParserCommand command = (KDSXMLParserCommand) KDSXMLParser.parseXml(this.getStationID(), xmlData);
        KDSXMLParserCommand.KDSCommand code = command.getCode();
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        if (fromStationID.equals(this.getStationID()))
            return; //don't do loop

        //showMessage("receive command="+KDSUtil.convertIntToString(code.ordinal()));
        switch (code) {

            case ROUTER_ASK_DB_STATUS:
            case ROUTER_FEEDBACK_DB_STATUS:
            case ROUTER_DB_SQL:
            case ROUTER_ASK_DB_DATA:
            case Require_Station_Configuration:
            case Broadcast_Station_Configuration:
            case ROUTER_SQL_SYNC:
            case ROUTER_UPDATE_CHANGES_FLAG:
            case DBStatistic_Sql:
            case Station_Return_Report:
                break;
            case SOS_Return_Report:
            {
                commandReturnSOSReport(command, xmlData);
            }
            default:
                return;
        }
    }

//    public void commandUpdateDBChangesGuid(KDSXMLParserCommand command, String strOrinalData)
//    {
//        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
//        String fromStationIP = command.getParam(KDSConst.KDS_Str_IP, "");
//
//        String guid = command.getParam("P0", "");
//        //this.getStatisticDB().setChangesGuid(guid);
//    }

//    /**
//     * 2015-12-29
//     * @param changesFlag
//     * guid _ time
//     * @return
//     */
//    private String getGuidFromDbChangesFlag(String changesFlag)
//    {
//        if (changesFlag.isEmpty()) return "";
//        int index = changesFlag.indexOf("_");
//        if (index<0) return changesFlag;
//        return changesFlag.substring(0,index-1);
//    }
//
//    /**
//     * 2015-12-29
//     * @param changesFlag
//     * guid _ time
//     * @return
//     */
//    private long getTimeFromDbChangesFlag(String changesFlag)
//    {
//        if (changesFlag.isEmpty()) return 0;
//        int index = changesFlag.indexOf("_");
//        if (index<0) return 0;
//        String s= changesFlag.substring(index+1);
//        return KDSUtil.convertStringToLong(s, 0);
//    }

    public Context getContext()
    {
        if (m_context != null)
            return m_context;
        else
            return KDSApplication.getContext();
    }
//    /**
//     * most likely, this function was called in primary router.
//     * Primary router check slave database, then it will decide transfer database or not.
//     * @param sock
//     * @param command
//     * @param strOrinalData
//     */
//    public void commandReturnRouterDbStatus(KDSSocketInterface sock,KDSXMLParserCommand command, String strOrinalData)
//    {
//        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
//        String fromStationIP = command.getParam(KDSConst.KDS_Str_IP, "");
//
//        String strFromRemoteRouter = command.getParam("P0", "0");
//        String strMyCurrent = "";// getStatisticDB().getDbChangesFlag();
//
//        String strLocalGuidFlag = getGuidFromDbChangesFlag(strMyCurrent);
//        long timeLocal = getTimeFromDbChangesFlag(strMyCurrent);
//
//        String strRemoteGuidFlag = getGuidFromDbChangesFlag(strFromRemoteRouter);
//        long timeRemote = getTimeFromDbChangesFlag(strFromRemoteRouter);
//
//
//        if (strLocalGuidFlag.equals(strRemoteGuidFlag))
//            return;
//        if (timeLocal > timeRemote)
//        {
//            showMessage(getContext().getString(R.string.transfering_db_to_other_router));//."Transfering database to other router.");
//            copyRouterDatabaseToOther(sock);
//        }
//        else {
//            showMessage(getContext().getString(R.string.updating_my_db_from_other_router));//"Updating my database from other router.");
//            copyRouterDatabaseFromOther(sock);
//        }
//
//
//
//    }

//    /**
//     * copy remote database to me
//     * 1. send command to thit station, let that station send sql to me.
//     * 2. In doCommandXml function, deal with the received sql.
//     * @param sock
//     */
//    public void copyRouterDatabaseFromOther(KDSSocketInterface sock)
//    {
//
//        if (sock == null) return;
//        String xmlData = KDSStatisticXMLParserCommand.createAskDatabaseData(getStationID(), getLocalIpAddress(), getLocalMacAddress());
//        KDSSocketTCPSideBase tcp = (KDSSocketTCPSideBase)sock;
//        tcp.writeXmlTextCommand(xmlData);
//
//
//    }


//    /**
//     * copy database to this station.
//     * Use the thread.
//     * @param sock
//     */
//    public void copyRouterDatabaseToOther(KDSSocketInterface sock)
//    {
//    }


//    /**
//     * write data to each POS
//     * @param xmlData
//     */
//    public void writeToAllViewer(String xmlData)
//    {
//        //2015-12-26
//        ArrayList<KDSStationConnection> conns = m_stationsConnection.getPosStations();
//        int ncount = conns.size();
//        for (int i=0; i< ncount  ; i++)
//        {
//            KDSStationConnection conn = conns.get(i);
//            Log.d(TAG, KDSUtil._FUNCLINE_()+"Write to POS #" +conn.getID() + ",ip="+conn.getIP() + ", port="+conn.getPort() +",length="+xmlData.length());
//            conn.getSock().writeXmlTextCommand(xmlData);
//        }
//    }

//    /**
//     * find each station, and check its status.
//     *  If active, write to it. If it is not active, write to its backup station.
//     * @param xmlData
//     */
//    public void writeToAllStations(String xmlData)
//    {
//        int ncount = m_stationsConnection.getRelations().getRelationsSettings().size();
//        for (int i=0; i< ncount; i++)
//        {
//            KDSStationsRelation stationRelation = m_stationsConnection.getRelations().getRelationsSettings().get(i);
//            //write to the expo stations too!!!! 20160222
//            //Just let expo receive item through the <kdsstation> tag
//            //if (stationRelation.getFunction() == KDSStatisticSettings.StationFunc.Expeditor)
//            //    continue;
//            String stationID = stationRelation.getID();
//
//            KDSStationIP station = stationRelation.getStationIP();
//
//            Log.d(TAG,KDSUtil._FUNCLINE_()+ "Write to KDSStation #" +station.getID() + ",ip=" +station.getIP() + ", port="+station.getPort() +",length="+xmlData.length());
//
//            //if (m_stationsConnection.findActivedStationByID(stationID) != null)
//            m_stationsConnection.writeDataToStationOrItsBackup(station, xmlData);
//        }
//
//    }

    private Thread m_threadPing = null;
    private boolean m_bRunning = false;
    final private Object m_pingLocker = new Object();

    /**
     * Ping connected IP address, if can not get them, this station is down.
     *
     * @return
     */
    public boolean startPingThread() {


        m_bRunning = true;
        m_threadPing = new Thread(this, "SocketPing");
        m_threadPing.start();
        return true;

    }

    /**
     *
     * @return
     */
    public boolean stopPingThread() {
        if (m_threadPing != null) {
            m_bRunning = false;
            try {
                m_threadPing.join(1000);
            } catch (Exception e) {

            }
        }
        m_threadPing = null;
        return true;
    }


    TimeDog m_dogAnnounce = new TimeDog();


    boolean DEBUG = true;

    /**
     * PING thread
     */
    public void run()
    {

        while (m_bRunning) {

            if (m_dogAnnounce.is_timeout(KDSConst.ACTIVE_PLUS_FREQUENCE))
            {
                m_dogAnnounce.reset();
                //if (!DEBUG)
                m_stationsConnection.checkAllNoResponseStations();
                this.broadcastStatisticRequireStationsUDP();
                checkLostStationInThread();
                //this.broadcastRequireStationsUDP(); //get all station's ip address.
            }
            if (m_bConnectAllActiveStations)
                m_stationsConnection.connectAllActivedStations();

            try {
                Thread.sleep(KDSConst.PING_THREAD_SLEEP);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }


    }
    private void checkLostStationInThread()
    {
        ArrayList<KDSStationIP> arStations = this.getStationsConnections().getRelations().getAllValidStations();


        for (int i=0; i< arStations.size(); i++)
        {
            KDSStationIP station = arStations.get(i);
            if (this.getStationsConnections().findActivedStationByID(station.getID()) == null)
                this.m_sockEventsMessageHandler.sendLostAnnouncePulseMessage(station.getID(), station.getIP());

        }


    }


    static final int PING_TIMES = 2;
//    /**
//     * -1: disconnected
//     * 0: do nothing
//     * 1: ping it, and get it.
//     * Just check connected stations
//     * @param station
//     */
//    private int pingStation(KDSStationConnection station)
//    {
//        if (station == null)
//            return 0;
//        String ip =m_stationsConnection.getStationIP(station);//.getIP();
//
//        if (station.getSock() == null)
//            return -1;
//        if (!station.getSock().isConnected())
//            return 0;
//
//        if (!KDSSocketTCPSideBase.ping(ip,PING_TIMES))
//        {
//            station.getSock().close();
//            m_stationsConnection.closeConnection(station.getIP());
//            return -1;
//        }
//        return 1;
//    }

    public void broadcastStatisticRequireStationsUDP()
    {
        ArrayList<Byte> ip = getLocalIp();
        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildStatisticRequireStationsCommand(ip);

        m_udpStationAnnouncer.broadcastData(SOSSettings.UDP_STATION_RECEIVE_ALIVE_ANNOUNCER_PORT, buf);
    }

    public KDSStationsConnection getAllActiveConnections()
    {
        return m_stationsConnection;
    }

//    /**
//     * Others computer ask the kds settings
//     * @param command
//     * @param xmlOriginalData
//     *  The xml don't been parsed to command
//     */
//    public void commandRequireConfigurations(KDSSocketInterface sock, KDSXMLParserCommand command, String xmlOriginalData)
//    {
//
//
//        String s = m_settings.outputXmlText(this.m_context);
//        s = KDSXMLParserCommand.createBroadConfiguration(s);
//        if (sock instanceof KDSSocketTCPSideBase)
//        {
//            KDSSocketTCPSideBase tcp = (KDSSocketTCPSideBase)sock;
//            tcp.writeXmlTextCommand(s);
//        }
//
//        //copy database to it
//        //copyRouterDatabaseToOther(sock);
//
//
//    }

//
//    public void commandReceiveConfigurations(KDSSocketInterface sock,  KDSXMLParserCommand command,String xmlOriginalData)
//    {
//        String strConfig = command.getParam(KDSConst.KDS_Str_Param, "");
//        if (strConfig.isEmpty())
//            return;
//        m_settings.parseXmlText(m_context, strConfig);
//        m_settings.save(m_context);
//
//    }



    public void showMessage(String msg)
    {
    }

    public SOSSettings getSettings()
    {
        return m_settings; //this settings is saved in layout
    }


    public  boolean isEnabledStation(ArrayList<SOSStationConfig> arStationEnabled, String stationID)
    {
        for (int i=0; i< arStationEnabled.size(); i++)
        {
            if (arStationEnabled.get(i).getStationID().equals(stationID))
                return true;
        }
        return false;
    }

    public SOSStationConfig getStationConfig(ArrayList<SOSStationConfig> arStationsEnabled, String stationID)
    {
        for (int i=0; i< arStationsEnabled.size(); i++)
        {
            if (arStationsEnabled.get(i).getStationID().equals(stationID))
                return arStationsEnabled.get(i);
        }
        return null;
    }
    /**
     * create report from remote station
     * @param arStationsID
     * @param condition
     */
    public  ArrayList<String> requestSOSReport(ArrayList<SOSStationConfig> arStationsID, SOSReportCondition condition)
    {



        ArrayList<KDSStationIP> ar =  m_stationsConnection.getAllActiveStations();
        ArrayList<String> validStations = new ArrayList<>();
        int ncounter = 0;
        for (int i=0; i< ar.size(); i++)
        {
            KDSStationIP station = ar.get(i);
            SOSStationConfig config =  getStationConfig(arStationsID, station.getID());


            //if (!isEnabledStation(arStationsID, station.getID()))
            if (config == null)
                continue;

            condition.setTargetSeconds(Math.round(config.getTargetPrepTime()*60));

            String reportConditionString = condition.export2String();
            String command = SOSXMLParserCommand.createRequestSOSReportCommand(this.getStationID(),this.getLocalIpAddress(), this.getLocalMacAddress(),reportConditionString );
            m_stationsConnection.connectStationWithData(station, command);
            //m_stationsConnection.writeDataToStationOrItsBackup(station, command);
            ncounter ++;
            validStations.add(station.getID());

        }
        return validStations;

    }

    final int AUTO_REPORT_INTERVAL = 30;//seconds
    private Date m_dtAutoCreated = KDSUtil.createInvalidDate();
    public boolean isTimeToCreateAutoReport()
    {

        int nTimeout = AUTO_REPORT_INTERVAL;// m_settings.getInt(KDSSOSSettings.ID.Auto_Report_Interval);
        TimeDog t = new TimeDog(m_dtAutoCreated);
        if (t.is_timeout(nTimeout * 1000))
        {
            m_dtAutoCreated = new Date();
            return true;
        }
        else
            return false;

    }
    public void createAutoReport(ArrayList<SOSStationConfig> arStations)
    {
        SOSReportCondition condition = SOSReportCreator.createCondition(getSettings(), arStations);

        m_reportCreators.createReport(condition);

    }



    /**
     * KDS station return the sos to me.
     * @param command
     * @param strOriginalData
     */
    public void commandReturnSOSReport(KDSXMLParserCommand command, String strOriginalData)
    {

        String station = command.getParam(KDSConst.KDS_Str_Station,"" );
//        String txt = KDSApplication.getContext().getString(R.string.receive_report);
//        txt = txt.replace("#", "#" + station);
//
//        Toast.makeText(KDSApplication.getContext(), txt, Toast.LENGTH_LONG).show();
//

        String s =  command.getParam(KDSConst.KDS_Str_Param, "");

        if (s.isEmpty()) return;

        //////////////////////////////////////////////////////////////////
        //parse this report
        SOSReportOneStation r = SOSReportOneStation.importFromXml(s);
        if (r == null) return; //failed

        //tell creators this report returned.
        m_reportCreators.onReceiveReport(station, r);

        if (m_arEventsReceiver.size()>0) {
            for (int i=0; i<m_arEventsReceiver.size(); i++)
                m_arEventsReceiver.get(i).onReceiveReport(station, r);
        }

    }

    /**
     * creator fire this event
     * @param creator
     */
    public void onReportCreatorStartCreateReport(SOSReportCreator creator,ArrayList<SOSStationConfig> arStations)
    {
        for  (int i=0 ; i< m_arEventsReceiver.size() ; i++)
        {
            for (int j=0; j<arStations.size(); j++)
                m_arEventsReceiver.get(i).onWaitingReport(arStations.get(j).getStationID());
        }
    }
    public void onReportCreatorReceiveStationReport(SOSReportCreator creator, String stationID )
    {
        for  (int i=0 ; i< m_arEventsReceiver.size() ; i++)
        {
            m_arEventsReceiver.get(i).onReceiveReport(stationID, creator.getReport().findStationReport(stationID));
        }
    }
    public void onReportCreatorReportReceivedAll(SOSReportCreator creator)
    {
//        if (creator == m_reportAutoCreator) {
//            if (!m_reportAutoCreator.isCreating()) return;
//            TimeSlotOrderReport.exportToFile(m_reportAutoCreator.getReport());
//            m_reportAutoCreator.reset();
//        }
//        else if (creator == m_reportRemoteCreator)
//        {
//            if (!m_reportRemoteCreator.isCreating()) return;
//            TimeSlotOrderReport report = m_reportRemoteCreator.getReport();
//            String s = report.export2Xml();
//            Object sock = m_reportRemoteCreator.getTag();
//            if (sock instanceof KDSSocketTCPSideServer)
//            {
//                ((KDSSocketTCPSideServer)sock).writeXmlTextCommand(s);
//            }
//            m_reportRemoteCreator.reset();
//        }
    }

}
