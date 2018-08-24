package com.bematechus.kdsstatistic;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.ConditionStatistic;
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
import com.bematechus.kdslib.KDSSocketTCPSideBase;
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
import com.bematechus.kdslib.TimeDog;
import com.bematechus.kdslib.TimeSlotOrderReport;


/**
 *
 */
public class STKDSStatistic extends KDSBase implements KDSSocketEventReceiver, Runnable,STReportCreatorEvents {

    private final String TAG = "KDSStatistic";

    public interface KDSStatisticEvents
    {
        void onStationConnected(String ip,KDSStationConnection conn);
        void onStationDisconnected(String ip);
    }

    public interface KDSStatisticRemoteEvents
    {
        void onReceiveReport(String stationID,TimeSlotOrderReport report);
    }

    public interface StationAnnounceEvents
    {
        void onReceivedStationAnnounce(String stationID, String ip, String port, String mac);
    }

    ArrayList<KDSStatisticRemoteEvents> m_remoteReportEventsReceivers = new ArrayList<>();

    public void setRemoteReportEventsReceiver(KDSStatisticRemoteEvents receiver)
    {
        for (int i=0; i< m_remoteReportEventsReceivers.size(); i++)
        {
            if (m_remoteReportEventsReceivers.get(i) == receiver) return;
        }
        m_remoteReportEventsReceivers.add(receiver);
    }

    public String getStationID()
    {
        return "Statistic";
    }

    /********************************************************************************************/

    int m_nListenViewerPort = STSettings.TCP_DEFAULT_REPORT_VIEWER_PORT;//
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

    ArrayList<KDSStatisticEvents> m_arKdsEventsReceiver = new ArrayList<>();//null; //KDS events
    StationAnnounceEvents m_stationAnnounceEvents = null;
    STStationsConnection m_stationsConnection = new STStationsConnection(m_socksManager,m_sockEventsMessageHandler );
    KDSSocketTCPCommandBuffer m_udpBuffer = new KDSSocketTCPCommandBuffer();

    STSettings m_settings = null; //this the root all others settings pointer

    String m_strLocalIP = "";
    String m_strLocalMAC = "";
    boolean m_bConnectAllActiveStations = false;

    STReportCreator m_reportAutoCreator = new STReportCreator();
    STReportCreator m_reportRemoteCreator = new STReportCreator();

    public KDSStationsConnection getStationsConnections()
    {
        return m_stationsConnection;
    }

    /**
     *
     * @param context
     * application context
     */
    public STKDSStatistic(Context context)
    {
        m_settings = new STSettings(context);// settings;
        m_settings.loadSettings(context);
        m_context = context;

        m_listenViewer = new KDSSocketTCPListen();
        m_listenViewer.setEventHandler(m_sockEventsMessageHandler);

        m_reportAutoCreator.setEventsReceiver(this);
        m_reportRemoteCreator.setEventsReceiver(this);
        updateSettings(m_settings);

    }

    public void updateSettings()
    {
        m_settings.loadSettings(m_context);
        updateSettings(m_settings);
    }


    public  void updateSettings(STSettings settings)
    {
        KDSLog.setLogLevel(m_settings.getInt(STKDSSettings.ID.Log_mode));
        m_stationsConnection.clearAllActiveAnnouncer(); //reset it.
        broadcastRequireStationsUDPInThread();
        m_stationsConnection.closeAllStationsConnections();
        int nPort = settings.getInt(STSettings.ID.KDSStatistic_Data_Viewer_IPPort);
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
    public void setEventReceiver(KDSStatisticEvents receiver)
    {
        int ncount = m_arKdsEventsReceiver.size();
        for (int i= 0; i< ncount; i++)
        {
            if (m_arKdsEventsReceiver.get(i) == receiver)
                return;
        }

        m_arKdsEventsReceiver.add(receiver);

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
        m_udpStationAnnouncer.start(STSettings.UDP_STATISTIC_RECEIVE_ALIVE_ANNOUNCER_PORT, m_sockEventsMessageHandler, m_socksManager);

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
        Log.d(TAG, KDSUtil._FUNCLINE_()+"start enter");
        refreshIPandMAC();

        updateSettings(m_settings);

        checkStationsSettingChanged(m_context);
        startNetwork();

        Log.d(TAG, KDSUtil._FUNCLINE_()+"start exit");
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
                STKDSStatistic.this.broadcastStatisticRequireStationsUDP();
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
        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
            m_arKdsEventsReceiver.get(i).onStationConnected(ip, conn);

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

        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
            m_arKdsEventsReceiver.get(i).onStationDisconnected(ip);
    }

    public void sockevent_onUDPReceiveXml(KDSSocketInterface sock, String xmlData)
    {

    }
    public void sockevent_onWroteDataDone(KDSSocketInterface sock, String remoteIP,  int nLength)
    {

    }
    public void smbevent_onSMBReceiveXml(KDSSMBDataSource smb, String smbFileName,String xmlData)
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
     *
     * @param sock
     * @param xmlData
     */
    public void doRequireReport(KDSSocketInterface sock, String xmlData)
    {
        ConditionStatistic condition = STXMLParserAskReport.parseReportCondition(xmlData);
        m_reportRemoteCreator.setTag(sock);
        m_reportRemoteCreator.refreshReport(condition);

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
            c.setListenPort(this.getSettings().getInt(STSettings.ID.KDSStatistic_Data_Viewer_IPPort));
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
                //commandAskDBStatus(sock, command, xmlData);
                break;
            case ROUTER_FEEDBACK_DB_STATUS:

                //commandReturnRouterDbStatus(sock, command, xmlData);
                break;
            case ROUTER_DB_SQL:
                //commandSqlRouterDB(command, xmlData);
                break;
            case ROUTER_ASK_DB_DATA:
                //commandAskDbData(sock, command, xmlData);
            case Require_Station_Configuration:
                commandRequireConfigurations(sock, command, xmlData);
                break;
            case Broadcast_Station_Configuration:
                commandReceiveConfigurations(sock, command, xmlData);
                break;
            case ROUTER_SQL_SYNC:
                //commandSyncSql(sock, command, xmlData);
                break;
            case ROUTER_UPDATE_CHANGES_FLAG:
                //commandUpdateDBChangesGuid(command, xmlData);
                break;
            case DBStatistic_Sql:
               // commandStatisticDBSql(command, xmlData);
                break;

            case Station_Return_Report:
            {
                commandReturnReport(command, xmlData);
            }
            break;
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

        m_udpStationAnnouncer.broadcastData(STSettings.UDP_STATION_RECEIVE_ALIVE_ANNOUNCER_PORT, buf);
    }

    public KDSStationsConnection getAllActiveConnections()
    {
        return m_stationsConnection;
    }

    /**
     * Others computer ask the kds settings
     * @param command
     * @param xmlOriginalData
     *  The xml don't been parsed to command
     */
    public void commandRequireConfigurations(KDSSocketInterface sock, KDSXMLParserCommand command, String xmlOriginalData)
    {


        String s = m_settings.outputXmlText(this.m_context);
        s = KDSXMLParserCommand.createBroadConfiguration(s);
        if (sock instanceof KDSSocketTCPSideBase)
        {
            KDSSocketTCPSideBase tcp = (KDSSocketTCPSideBase)sock;
            tcp.writeXmlTextCommand(s);
        }

        //copy database to it
        //copyRouterDatabaseToOther(sock);


    }


    public void commandReceiveConfigurations(KDSSocketInterface sock,  KDSXMLParserCommand command,String xmlOriginalData)
    {
        String strConfig = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strConfig.isEmpty())
            return;
        m_settings.parseXmlText(m_context, strConfig);
        m_settings.save(m_context);

    }



    public void showMessage(String msg)
    {
    }

    public STSettings getSettings()
    {
        return m_settings; //this settings is saved in layout
    }



    /**
     * create report from remote station
     * @param arStationsID
     * @param condition
     */
    public  ArrayList<String> requestStatisticReport(ArrayList<String> arStationsID, ConditionStatistic condition)
    {

        String reportCondition = condition.export2String();

        ArrayList<KDSStationIP> ar =  m_stationsConnection.getAllActiveStations();
        ArrayList<String> validStations = new ArrayList<>();
        int ncounter = 0;
        for (int i=0; i< ar.size(); i++)
        {
            KDSStationIP station = ar.get(i);
            if (!KDSUtil.isExistedInArray(arStationsID, station.getID()))
                continue;
            String command = STXMLParserCommand.createRequestStatisticReportCommand(this.getStationID(),this.getLocalIpAddress(), this.getLocalMacAddress(),reportCondition );
            m_stationsConnection.connectStationWithData(station, command);
            //m_stationsConnection.writeDataToStationOrItsBackup(station, command);
            ncounter ++;
            validStations.add(station.getID());

        }
        return validStations;

    }

    private Date m_dtAutoCreated = KDSUtil.createInvalidDate();
    public boolean isTimeToCreateAutoReport()
    {
        boolean bEnabled = m_settings.getBoolean(STSettings.ID.Enable_Auto_Report);
        if (!bEnabled) return false;
        String s = m_settings.getString(STSettings.ID.Auto_Report_Time);
        String[] pieces=s.split(":");

        int nhour = (Integer.parseInt(pieces[0]));
        int nmins = (Integer.parseInt(pieces[1]));
        Calendar c = Calendar.getInstance();
        Date dtNow = c.getTime();

        c.set(Calendar.HOUR_OF_DAY, nhour);
        c.set(Calendar.MINUTE, nmins);
        c.set(Calendar.SECOND, 0);

        Date dtDeadLine = c.getTime();
        boolean isTimeCreate =  (dtNow.getTime() - dtDeadLine.getTime() >0);
        if (isTimeCreate)
        {
            //check if it created.
            String strCreatedDate =  KDSUtil.convertDateToShortString(m_dtAutoCreated);
            String strNowDate =   KDSUtil.convertDateToShortString(dtNow);

            return (!strCreatedDate.equals(strNowDate));
        }
        return isTimeCreate;
    }
    public void createAutoReport()
    {
        m_dtAutoCreated = new Date();
        int n = m_settings.getInt(STSettings.ID.Auto_Report_Type);
        ConditionStatistic.ReportType rt = ConditionStatistic.ReportType.values()[n];
        ConditionStatistic condition = getAutoCondition(rt);
        m_reportAutoCreator.refreshReport(condition);
    }

    public ConditionStatistic getAutoCondition( ConditionStatistic.ReportType rt)
    {
        //format: hh:mm
        String tmFrom = m_settings.getString(STSettings.ID.Auto_Report_Timeslot_from);
        String tmTo = m_settings.getString(STSettings.ID.Auto_Report_Timeslot_to);

        String stationFrom = m_settings.getString(STSettings.ID.Auto_Report_Station_From);
        String stationTo = m_settings.getString(STSettings.ID.Auto_Report_Station_To);

        ConditionStatistic condition = new ConditionStatistic();
        condition.setStationFrom(stationFrom);
        condition.setStationTo(stationTo);
        condition.setReportType(rt);

        switch (rt)
        {

            case Daily:

                condition.getDailyReportCondition().setTimeFromString(tmFrom);
                condition.getDailyReportCondition().setTimeToString(tmTo);
                break;
            case Weekly:
                condition.getWeeklyCondition().setTimeFromString(tmFrom);
                condition.getWeeklyCondition().setTimeToString(tmTo);
                break;
            case Monthly:
                condition.getMonthlyCondition().setTimeFromString(tmFrom);
                condition.getMonthlyCondition().setTimeToString(tmTo);
                break;
            case OneTime:
                break;
        }
        return condition;
    }

    private void commandReturnReport(KDSXMLParserCommand command, String strOrinalData)
    {
        String station = command.getParam(KDSConst.KDS_Str_Station,"" );
        String txt = KDSApplication.getContext().getString(R.string.receive_report);
        txt = txt.replace("#", "#" + station);

        Toast.makeText(KDSApplication.getContext(), txt, Toast.LENGTH_LONG).show();
        String s =  command.getParam(KDSConst.KDS_Str_Param, "");

        TimeSlotOrderReport r = TimeSlotOrderReport.importFromXml(s);

        m_reportAutoCreator.onReceiveReport(station, r);
        m_reportRemoteCreator.onReceiveReport(station, r);

        if (m_remoteReportEventsReceivers.size()>0) {
            for (int i=0; i<m_remoteReportEventsReceivers.size(); i++)
                m_remoteReportEventsReceivers.get(i).onReceiveReport(station, r);
        }

    }

    public void onReportCreatorStartCreateReport(STReportCreator creator, ConditionStatistic condition)
    {

    }
    public void onReportCreatorReceiveStationReport(STReportCreator creator, String stationID,ConditionStatistic condition, TimeSlotOrderReport report )
    {

    }

    KDSSMBDataSource m_smbRemoteFolder = new KDSSMBDataSource(m_sockEventsMessageHandler);

    public void onReportCreatorReportCreated(STReportCreator creator, ConditionStatistic condition)
    {
        if (creator == m_reportAutoCreator) {
            if (!m_reportAutoCreator.isCreating()) return;

            int n = m_settings.getInt(STSettings.ID.Auto_report_save_to);
            STSettings.ReportSaveTo saveTo = STSettings.ReportSaveTo.values()[n];
            if (saveTo == STSettings.ReportSaveTo.Remote)
            {
                String s = m_settings.getString(STSettings.ID.Remote_folder);
                m_smbRemoteFolder.setRemoteFolder(s);
                s =  TimeSlotOrderReport.exportToString(m_reportAutoCreator.getReport());
                String fileName = m_reportAutoCreator.getReport().getReportFileName();//TimeSlotOrderReport.createNewFileName(m_reportAutoCreator.getReport());
                m_smbRemoteFolder.uploadSmbFile("auto_report", fileName, s);

            }
            else {
                TimeSlotOrderReport.exportToFile(m_reportAutoCreator.getReport());
            }
            m_reportAutoCreator.reset();
        }
        else if (creator == m_reportRemoteCreator)
        {
            if (!m_reportRemoteCreator.isCreating()) return;
            TimeSlotOrderReport report = m_reportRemoteCreator.getReport();
            String s = report.export2Xml();
            Object sock = m_reportRemoteCreator.getTag();
            if (sock instanceof KDSSocketTCPSideServer)
            {
                ((KDSSocketTCPSideServer)sock).writeXmlTextCommand(s);
            }
            m_reportRemoteCreator.reset();
        }
    }

}
