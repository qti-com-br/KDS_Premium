package com.bematechus.kds;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSBase;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDBBase;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSPosNotificationFactory;
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
import com.bematechus.kdslib.KDSState;
import com.bematechus.kdslib.KDSStationActived;
import com.bematechus.kdslib.KDSStationConnection;
import com.bematechus.kdslib.KDSStationDataBuffered;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSStationsConnection;
import com.bematechus.kdslib.KDSStationsRelation;
import com.bematechus.kdslib.KDSToStation;
import com.bematechus.kdslib.KDSToStations;
import com.bematechus.kdslib.KDSToast;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXMLParserCommand;
import com.bematechus.kdslib.ScheduleProcessOrder;
import com.bematechus.kdslib.SettingsBase;
import com.bematechus.kdslib.TimeDog;

import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The KDS app main interface.
 *  It charge the network, printer ...
 */
public class KDS extends KDSBase implements KDSSocketEventReceiver, Runnable {

    final static String TAG = "KDSMain";
    public enum RefreshViewParam
    {
        None,
        Focus_First,
    }
    public enum MessageType
    {
        Normal,
        Toast,
    }


    public interface KDSEvents
    {
        void onStationConnected(String ip, KDSStationConnection conn);
        void onStationDisconnected(String ip);
        void onAcceptIP(String ip);
        void onRefreshView(KDSUser.USER userID, KDSDataOrders orders, RefreshViewParam nParam); //nParam: 1: move focus to first order.
        void onRetrieveNewConfigFromOtherStation();
        void onShowMessage(MessageType msgType, String message);
        void onRefreshSummary(KDSUser.USER userID);
        void onAskOrderState(Object objSource, String orderName);
        void onSetFocusToOrder(String orderGuid); //set focus to this order
        void onXmlCommandBumpOrder(String orderGuid);
        void onTTBumpOrder(String orderName);
        void onReceiveNewRelations();
        void onReceiveRelationsDifferent();
//        void onItemQtyChanged(KDSDataOrder order, KDSDataItem item);
//        void onOrderStatusChanged(KDSDataOrder order, int nOldStatus);
        //void onShowToastMessage(String message);
    }

    final int DEFAULT_POS_IP_PORT = 3000;
    final int DEFAULT_STATION_IP_PORT = 3001;
    final int DEFAULT_STATISTIC_TCP_PORT = 6001;


    public interface StationAnnounceEvents
    {
        void onReceivedStationAnnounce(KDSStationIP stationReceived);//String stationID, String ip, String port, String mac);
    }

    /********************************************************************************************/

    String m_strKDSStationID = "";
    int m_nPOSPort = DEFAULT_POS_IP_PORT;
    int m_nStationsPort = DEFAULT_STATION_IP_PORT;
    int m_nStatisticPort = DEFAULT_STATISTIC_TCP_PORT;


    KDSDBCurrent m_dbCurrent = null;
    KDSDBStatistic m_dbStatistic = null;
    KDSDBSupport m_dbSupport = null;
    // broadcast udp message
    KDSSocketUDP m_udpStationAnnouncer = new KDSSocketUDP();

    //all socket checking
    KDSSocketManager m_socksManager = new KDSSocketManager();
    //listen which pos want to connect to me
    KDSSocketTCPListen m_listenPOS;
    KDSSocketTCPListen m_listenStatistic;
    //list which normal kds station connect to me.
    KDSSocketTCPListen m_listenStations;
    //use it receive event, and
    KDSSocketMessageHandler m_sockEventsMessageHandler  = new KDSSocketMessageHandler(this);; //socket events

    Context m_context = null;

    Object m_locker = new Object();
    //Object m_activePulseLocker = new Object(); //for lock the active announce array

    ArrayList<KDSEvents> m_arKdsEventsReceiver = new ArrayList<KDSEvents>();//null; //KDS events

    StationAnnounceEvents m_stationAnnounceEvents = null;

    KDSStationsConnection m_stationsConnection = new KDSStationsConnection(m_socksManager,m_sockEventsMessageHandler );
   // ArrayList<KDSStationsRelation> m_arStationsRelations = new ArrayList<KDSStationsRelation>();
   KDSSocketTCPCommandBuffer m_udpBuffer = new KDSSocketTCPCommandBuffer();

    KDSSettings m_settings = null; //this the root all others settings pointer

    KDSSMBDataSource m_smbDataSource = new KDSSMBDataSource(m_sockEventsMessageHandler);

    KDSState m_kdsState = new KDSState();

    String m_strLocalIP = "";
    String m_strLocalMAC = "";

    KDSBumpBarFunctions m_bumpbarFunctions = new KDSBumpBarFunctions();

    KDSUsers m_users = new KDSUsers(this);

    KDSPrinter m_printer = new KDSPrinter(this);

    Date m_dtStartToRequireRelations = new Date();

    SoundManager m_soundManager = new SoundManager();

    PagerManager m_pagerManager = new PagerManager();

    KDSDBBase.DBEvents m_dbEventsReceiver = null;

    Broadcaster m_broadcaster = new Broadcaster(this);
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void setDBEventsReceiver(KDSDBBase.DBEvents evReceiver )
    {
        m_dbEventsReceiver = evReceiver;
        resetDatabasesEventsReceiver();
    }

    public void resetDatabasesEventsReceiver()
    {
        if (m_dbCurrent != null)
            m_dbCurrent.setDBEventsReceiver(m_dbEventsReceiver);
        if (m_dbSupport != null)
            m_dbSupport.setDBEventsReceiver(m_dbEventsReceiver);
        if (m_dbStatistic != null)
            m_dbStatistic.setDBEventsReceiver(m_dbEventsReceiver);
    }

    public PagerManager getPagerManager()
    {
        return m_pagerManager;
    }

    public KDSSettings.StationFunc getStationFunction()
    {
        return m_settings.getStationFunc();

    }
    public KDSStationsConnection getStationsConnections()
    {
        return m_stationsConnection;
    }

    public KDSDBSupport getSupportDB()
    {
        return m_dbSupport;
    }
    public KDSDBCurrent getCurrentDB()
    {
        return m_dbCurrent;
    }
    public KDSDBStatistic getStatisticDB()
    {
        return m_dbStatistic;
    }

    public KDS()
    {

    }


    public void reopenCurrentDB(Context context)
    {
        m_dbCurrent = KDSDBCurrent.open(context);
        resetDatabasesEventsReceiver();
    }

    public void reopenSupportDB(Context context)
    {
        m_dbSupport = KDSDBSupport.open(context);
        resetDatabasesEventsReceiver();
    }
    public void reopenStatisticDB(Context context)
    {
        m_dbStatistic = KDSDBStatistic.open(context);
        resetDatabasesEventsReceiver();
    }

    /**
     *
     * @param context
     * application context
     */
    public KDS(Context context)
    {
        setContext(context);

        m_listenPOS = new KDSSocketTCPListen();
        m_listenPOS.setEventHandler(m_sockEventsMessageHandler);
        m_listenStations = new KDSSocketTCPListen();
        m_listenStations.setEventHandler(m_sockEventsMessageHandler);

        m_listenStatistic = new KDSSocketTCPListen();
        m_listenStatistic.setEventHandler(m_sockEventsMessageHandler);

    }

    public void setContext(Context context)
    {
        m_settings = new KDSSettings(context);// settings;
        m_settings.loadSettings(context);
        m_context = context;

        //database
        m_dbCurrent = KDSDBCurrent.open(context);
        m_dbSupport = KDSDBSupport.open(context);
        m_dbStatistic = KDSDBStatistic.open(context);
        resetDatabasesEventsReceiver();

    }

    public KDSUsers getUsers()
    {
        return m_users;
    }
    /**
     * set single/multple users mode
     * @param settings
     */
    public void updateUsers(KDSSettings settings)
    {
        int n = settings.getInt(KDSSettings.ID.Users_Mode);
        KDSSettings.KDSUserMode mode = KDSSettings.KDSUserMode.values()[n];
        if (mode == KDSSettings.KDSUserMode.Single)
        {
            m_users.setSingleUserMode();
        }
        else if (mode == KDSSettings.KDSUserMode.Multiple)
        {
            m_users.setTwoUserMode();
        }

        m_users.updateSettings(settings);
    }

    public void updateSettings(Context context)
    {
        if (m_settings == null)
            m_settings =new KDSSettings(context);
        m_settings.loadSettings(context);
        updateSettings(m_settings);

    }

    public  void updateSettings(KDSSettings settings)
    {

        KDSLog.setLogLevel(settings.getInt(KDSSettings.ID.Log_mode));

        String stationOldID = m_strKDSStationID;
        m_strKDSStationID = settings.getString(KDSSettings.ID.KDS_ID);
        if (!m_strKDSStationID.equals(stationOldID))
        {
            m_stationsConnection.clearAllActiveAnnouncer(); //reset it.
            getBroadcaster().broadcastStationAnnounceInThread2();
            getBroadcaster().broadcastRequireStationsUDPInThread();
            m_stationsConnection.getRelations().refreshRelations(m_context,getStationID());
            m_stationsConnection.closeAllStationsConnections();

        }
        int nPort = settings.getInt(KDSSettings.ID.KDS_Data_TCP_Port);
        boolean bPosPortChanged = false;
        if (nPort != m_nPOSPort)
            bPosPortChanged = true;
        m_nPOSPort = nPort;
        nPort = settings.getInt(KDSSettings.ID.KDS_Station_Port);
        boolean bStationPortChanged = false;
        if (nPort != m_nStationsPort)
            bStationPortChanged = true;
        m_nStationsPort = nPort;

        updateStationFunction();

        if (bPosPortChanged)
        {
            if (m_listenPOS.isListening())
            {
                m_listenPOS.stop();

                m_stationsConnection.disconnectPOSConnections();
                m_listenPOS.startServer(m_nPOSPort, m_socksManager, m_sockEventsMessageHandler );

            }
        }
        startPOSListener();


        if (bStationPortChanged)
        {
            if (m_listenStations.isListening())
            {

                m_listenStations.stop();
                //disconnectStations(m_arConnectMeStations);
                m_stationsConnection.closeAllStationsConnections();//.disconnectAllStationsConnectedToMe();
                m_listenStations.startServer(m_nStationsPort, m_socksManager, m_sockEventsMessageHandler );

            }
        }
        startRemoteFolderDataSource(settings);

        updateUsers(settings);

        m_bumpbarFunctions.updateSettings(this.getSettings());

        m_printer.updateSettings(settings);

    }
    public void updateStationFunction()
    {
        KDSSettings.StationFunc func =m_stationsConnection.getRelations().getStationFunction(getStationID(), "");

        m_settings.setStationFunc(func);
    }

//    public KDSSettings.StationFunc getOriginalStationFunc()
//    {
//        KDSSettings.StationFunc func =m_stationsConnection.getRelations().getStationFunction(getStationID(), "");
//        return func;
//    }
    public KDSPrinter getPrinter()
    {
        return m_printer;
    }
    public void setEventReceiver(KDSEvents receiver)
    {
        int ncount = m_arKdsEventsReceiver.size();
        for (int i= 0; i< ncount; i++)
        {
            if (m_arKdsEventsReceiver.get(i) == receiver)
                return;
        }

        m_arKdsEventsReceiver.add(receiver);
        //m_eventReceiver = receiver;
    }
    public void removeEventReceiver(KDSEvents receiver)
    {
        int ncount = m_arKdsEventsReceiver.size();
        for (int i=ncount -1; i>=0; i--)
        {
            if (m_arKdsEventsReceiver.get(i) == receiver)
                m_arKdsEventsReceiver.remove(i);
        }
    }

    TimeDog m_clearDbTimeDog = new TimeDog();
    /**
     * check connection in a loop
     */
    public void on1sTimer()
    {

        if (getSettings().getBoolean(KDSSettings.ID.Pager_enabled)) {
            if (this.isExpeditorStation() || isQueueExpo() || isQueueExpoView())
                getPagerManager().onTime();
        }

        if (m_clearDbTimeDog.is_timeout(1800000)) //30x60x1000, 30mins
        {
            remove_statistic_old_data();
            m_clearDbTimeDog.reset();
        }
    }

    public boolean startPOSListener()
    {
        int n  = m_settings.getInt(KDSSettings.ID.KDS_Data_Source);
        KDSSettings.KDSDataSource srcType = KDSSettings.KDSDataSource.values()[n];
        if (srcType == KDSSettings.KDSDataSource.TCPIP)
        {
            stopPOSListener();
            m_listenPOS.startServer(m_nPOSPort, m_socksManager, m_sockEventsMessageHandler);
        }
        else
        {
            stopPOSListener();
        }

        return true;
    }
    public void stopPOSListener()
    {
        m_listenPOS.stop();
    }


    public boolean startStatisticListener()
    {
        m_listenStatistic.startServer(m_nStatisticPort, m_socksManager, m_sockEventsMessageHandler);
        return true;
    }
    public void stopStatisticListener()
    {
        m_listenStatistic.stop();
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
        //Log.d(TAG, "StopNetwork Enter");
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Enter");
        m_socksManager.stopThread();
        m_udpStationAnnouncer.close();
        stopPOSListener();;
        m_listenStations.stop();

        m_stationsConnection.closeAllStationsConnections();
        m_stationsConnection.disconnectPOSConnections();
        stopPingThread();
        if (!isQueueStation() && !isTrackerStation() && !isQueueExpo())
            m_printer.close();
        m_smbDataSource.stop();

        //Log.d(TAG, "StopNetwork Exit");
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Exit");
    }

    public boolean isNetworkRunning()
    {
        if (m_socksManager == null)
            return false;
        return (m_socksManager.isThreadRunning());

    }

    /**
     * prevent the UDP dead. (Don't find reason until now).
     * 20160929: I comment it.
     *          As after long time running, UDP happen EACCES error, I doubt this reset procedure
     *          make udp file handle error!
     *          Just keep created instance after while kds.start();
     *
     */
    public void udpListenerReset()
    {
        return;
        //m_udpStationAnnouncer.close();
        //m_udpStationAnnouncer.start(KDSSettings.UDP_ANNOUNCER_PORT, m_sockEventsMessageHandler, m_socksManager);

    }

    public void startNetwork()
    {
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Enter");
        if (m_socksManager.isThreadRunning()) //the network is running
            return;
        refreshIPandMAC();

        m_socksManager.startThread();

        m_udpStationAnnouncer.start(KDSSettings.UDP_ANNOUNCER_PORT, m_sockEventsMessageHandler, m_socksManager);

        //let others stations know me as soon as possible.
        //this.broadcastStationAnnounceInThread();
        this.getBroadcaster().broadcastStationAnnounceInThread2();
        this.getBroadcaster().broadcastRequireStationsUDPInThread();

        startPOSListener();
        startStatisticListener();

        m_listenStations.startServer(m_nStationsPort, m_socksManager, m_sockEventsMessageHandler);

        this.getBroadcaster().broadcastRequireStationsUDPInThread();

        //connectAllStations();
        m_stationsConnection.connectAllStations();

        startPingThread();
        this.getBroadcaster().broadcastRequireStationsUDPInThread();
        //if the datat source is SMB folder, start thread.
        startRemoteFolderDataSource(m_settings);

        //let others stations know me as soon as possible.
        //this.broadcastStationAnnounceInThread();
        this.getBroadcaster().broadcastStationAnnounceInThread2();
        m_printer.open(false); //from gui
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Exit");
    }


    public boolean start( )//KDSSettings settings)
    {
        refreshIPandMAC();
        updateSettings(m_settings);
        //load all stations settings.
        checkStationsSettingChanged(m_context);
        startNetwork();

        return true;

    }

    private void startRemoteFolderDataSource(KDSSettings settings)
    {
        //if the datat source is SMB folder, start thread.
        KDSSettings.KDSDataSource source =KDSSettings.KDSDataSource.values ()[settings.getInt(KDSSettings.ID.KDS_Data_Source)];
        if (source == KDSSettings.KDSDataSource.Folder)
        {
            String remoteFolder = settings.getString(KDSSettings.ID.KDS_Data_Folder);
            m_smbDataSource.setRemoteFolder(remoteFolder);

            m_smbDataSource.start();
        }
        else
        {
            m_smbDataSource.stop();
        }
    }

    public KDSSMBDataSource getSmbDataSource()
    {
        return m_smbDataSource;
    }


    public void stop()
    {
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Enter");
        stopWithoutDBClose();


        if (m_dbCurrent != null) {
            m_dbCurrent.close();
            m_dbCurrent = null;
        }
        if (m_dbSupport != null) {
            m_dbSupport.close();
            m_dbSupport = null;
        }
        if (m_dbStatistic != null) {
            m_dbStatistic.close();
            m_dbStatistic = null;
        }
        m_arKdsEventsReceiver.clear();
        if (m_errorToast != null)
            m_errorToast.cancel();
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Exit");
//        m_users.stop();
    }

    public void stopWithoutDBClose()
    {
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Enter");
        stopPingThread();
        synchronized (m_locker) {
            stopNetwork();
        }

   //     m_arKdsEventsReceiver.clear();
        m_users.stop();
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Exit");
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
        return m_strLocalIP;
    }
    public String getLocalMacAddress()
    {
        return m_strLocalMAC;
    }
//    public void broadcastStationAnnounce()
//    {
//        ByteBuffer buf = makeStationAnnounceBuffer();
//        m_udpStationAnnouncer.broadcastData(buf);
//        broadcastAnnounceToRouter();
//    }

    public int getAllItemsCount()
    {
        if (this == null) return 0;
        if (this.getCurrentDB() == null) return 0;
        return this.getCurrentDB().orderGetAllItemsCount();
    }


    public ByteBuffer makeStationAnnounceBuffer()
    {
        int port = this.m_nStationsPort;
        String strport = KDSUtil.convertIntToString(port);
        int nItemsCount = getAllItemsCount();
        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildReturnStationIPCommand2(getStationID(),m_strLocalIP, strport, getLocalMacAddress(), nItemsCount, getSettings().getInt(KDSSettings.ID.Users_Mode));
        return buf;
    }
//    public void broadcastStationAnnounceInThread2()
//    {
//
//
//        (new KDSBroadcastThread(m_udpStationAnnouncer, makeStationAnnounceBuffer())).start();
//
//       // ByteBuffer buf = makeAnnounceToRouterBuffer();
//        (new KDSBroadcastThread(m_udpStationAnnouncer,KDSSettings.UDP_ROUTER_ANNOUNCER_PORT, makeStationAnnounceBuffer())).start();
//
//    }

//    public ByteBuffer makeQueueExpoBumpSettingsBuffer(boolean bEnabled)
//    {
//
//        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildQueueExpoBumpSettingCommand( bEnabled);
//        return buf;
//    }
//    public void broadcastQueueExpoDoubleBumpValue(boolean bEnabled)
//    {
//        (new KDSBroadcastThread(m_udpStationAnnouncer, makeQueueExpoBumpSettingsBuffer(bEnabled))).start();
//
//    }

//    public ByteBuffer makeAnnounceToRouterBuffer()
//    {
//        int port = this.m_nPOSPort;
//        String strport = KDSUtil.convertIntToString(port);
//        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildReturnStationIPCommand(getStationID(), m_strLocalIP, strport, getLocalMacAddress());
//        return buf;
//    }
//    /**
//     * tell router I am here.
//     */
//    private void broadcastAnnounceToRouter()
//    {
//
////        int port = this.m_nPOSPort;
////        String strport = KDSUtil.convertIntToString(port);
////        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildReturnStationIPCommand(getStationID(), m_strLocalIP, strport, getLocalMacAddress());
//        ByteBuffer buf = makeAnnounceToRouterBuffer();
//
//        m_udpStationAnnouncer.broadcastData(KDSSettings.UDP_ROUTER_ANNOUNCER_PORT, buf);
//    }

//    public void broadcastRelations(String relations)
//    {
//        String s = "<Relations>";
//        s += relations;
//        s += "</Relations>";
//        ByteBuffer buf =  KDSSocketTCPCommandBuffer.buildXMLCommand(s);
//
//        m_udpStationAnnouncer.broadcastData(buf);
//
//        m_udpStationAnnouncer.broadcastData(KDSSettings.UDP_ROUTER_ANNOUNCER_PORT, buf);
//
//
//    }

//    /**
//     *
//     * @param relations
//     * @param toIP
//     * Formart: /ip:port
//     *
//     */
//    public void broadcastRequireRelations(String relations, String toIP)
//    {
//        String s = "<RelationsRet>";
//        s += relations;
//        s += "</RelationsRet>";
//        ByteBuffer buf =  KDSSocketTCPCommandBuffer.buildXMLCommand(s);
//        String ip = parseRemoteUDPIP(toIP);
//        String port = parseRemoteUDPPort(toIP);
//        int nport = KDSSettings.UDP_ANNOUNCER_PORT;
//        if (!port.isEmpty())
//            nport = KDSUtil.convertStringToInt(port,KDSSettings.UDP_ANNOUNCER_PORT );
//        m_udpStationAnnouncer.broadcastData(ip, nport, buf);
//
//
//    }

//    public void broadcastShowStationID()
//    {
//        broadcastShowStationIDCommand();
//        //broadcastShowStationIDCommandToRouter();
//    }

    public void udpAskRelations(String stationID)
    {
        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildAskStationsRelationsCommand();
        KDSStationActived station =  this.getStationsConnections().findActivedStationByID(stationID);
        if (station == null) return;

        m_udpStationAnnouncer.broadcastData(station.getIP(), buf);
    }

//    public void broadcastRequireRelationsCommand()
//    {
//
//        m_dtStartToRequireRelations = new Date();
//
//        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildRequireRelationsCommand();
//        m_udpStationAnnouncer.broadcastData(buf);
//        m_udpStationAnnouncer.broadcastData(KDSSettings.UDP_ROUTER_ANNOUNCER_PORT, buf);
//    }

//    public boolean isRequireRelationsFinished()
//    {
//        TimeDog t = new TimeDog(m_dtStartToRequireRelations);
//        return (t.is_timeout(5000));
//
//    }

//    private void broadcastShowStationIDCommand()
//    {
//
//        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildShowStationIDCommand();
//        m_udpStationAnnouncer.broadcastData(buf);
//
//    }
//    private void broadcastShowStationIDCommandToRouter()
//    {
//        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildShowStationIDCommand();
//        m_udpStationAnnouncer.broadcastData(KDSSettings.UDP_ROUTER_ANNOUNCER_PORT, buf);
//
//    }

//    public void broadcastClearDBCommand()
//    {
//        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildClearDBCommand();
//        m_udpStationAnnouncer.broadcastData(buf);
//
//    }


//    /**
//     * for preparation time mode
//     * @param orderName
//     * @param itemNames
//     * @param bBumped
//     */
//    public void broadcastItemBumpUnbump(String orderName, ArrayList<String> itemNames, boolean bBumped)
//    {
//        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildItemBumpUnbumpUdpCommand(orderName,itemNames, bBumped );
//
//        (new KDSBroadcastThread(m_udpStationAnnouncer, buf)).start();
//
//
//    }

//    public void broadcastItemBumpUnbump(String orderName, String itemName, boolean bBumped)
//    {
//        ArrayList<String> ar = new ArrayList<>();
//        ar.add(itemName);
//        broadcastItemBumpUnbump(orderName, ar, bBumped);
//
//    }
//
//    public void broadcastItemBumpUnbump(KDSDataOrder order, boolean bBumped)
//    {
//        ArrayList<String> ar = new ArrayList<>();
//        for (int i=0; i< order.getItems().getCount(); i++)
//            ar.add(order.getItems().getItem(i).getItemName());
//        broadcastItemBumpUnbump(order.getOrderName(), ar, bBumped);
//
//    }

//    public ByteBuffer makeTrackerBumpAnnounceBuffer(String ordername)
//    {
//
//        String s = "<TTBump>" + ordername;
//
//        ByteBuffer buf =  KDSSocketTCPCommandBuffer.buildXMLCommand(s);
//        return buf;
//    }
//
//    public void broadcastTrackerBump(String orderName)
//    {
//        (new KDSBroadcastThread(m_udpStationAnnouncer, makeTrackerBumpAnnounceBuffer(orderName))).start();
//
//    }



    public void onShowStationID()
    {
        String stationid = this.getStationID();
      //  showMessage(stationid);
        KDSToast.showStationID(this.m_context, stationid);
    }

    public void onClearDB()
    {
        clearAll();
    }

    public String getRelationsXml()
    {
        String strRelations = getSettings().loadStationsRelationString(m_context);
        // Object[] ar = new Object[]{s};
        String s = "<Relations>";
        s += strRelations;
        s += "</Relations>";
        return s;
    }

    public void onOtherAskRelations(String fromStationIP, String fromStationPort)
    {

        String s = getRelationsXml();
        ByteBuffer buf =  KDSSocketTCPCommandBuffer.buildXMLCommand(s);
        int port = KDSUtil.convertStringToInt(fromStationPort, 0);
        Object[] ar = new Object[]{fromStationIP,port, buf};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String ip =(String) params[0];
                int port = (int)params[1];
                ByteBuffer buf = (ByteBuffer)params[2];
                if (port >0)
                    m_udpStationAnnouncer.broadcastData(ip,port, buf);
                else
                    m_udpStationAnnouncer.broadcastData(ip,buf);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ar);



    }

    private void doUdpReceiveData(KDSSocketInterface sock,String remoteIP,  ByteBuffer buffer, int nLength)
    {
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
                    if (ncommand_end == 0)  return; //need more data

                    byte[] bytes = m_udpBuffer.station_info_command_data();
                    m_udpBuffer.remove(ncommand_end);
                    String utf8 = KDSUtil.convertUtf8BytesToString(bytes);
                    onUdpReceiveStationAnnounce(utf8);

                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_REQ_STATION: {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data
                    m_udpBuffer.remove(ncommand_end);
                    //broadcastStationAnnounceInThread();
                    getBroadcaster().broadcastStationAnnounceInThread2();

                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_STATISTIC_REQ_STATION:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data
                    byte b0, b1, b2, b3;
                    b0 = m_udpBuffer.buffer().get(2);
                    b1 = m_udpBuffer.buffer().get(3);
                    b2 = m_udpBuffer.buffer().get(4);
                    b3 = m_udpBuffer.buffer().get(5);
                    m_udpBuffer.remove(ncommand_end);

                    onStatisticAppRequireStationAnnounceInThread2(b0, b1, b2, b3);

                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_SHOW_ID:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data
                    m_udpBuffer.remove(ncommand_end);
                    onShowStationID();
                }
                break;
                case KDSSocketTCPCommandBuffer.XML_COMMAND:
                {//
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data

                    byte[] bytes = m_udpBuffer.xml_command_data();
                    m_udpBuffer.remove(ncommand_end);
                    String utf8 = KDSUtil.convertUtf8BytesToString(bytes);
                    doUdpXmlCommand(utf8, remoteIP);
                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_CLEAR_DB:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data
                    m_udpBuffer.remove(ncommand_end);
                    onClearDB();
                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_ASK_RELATIONS:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data
                    m_udpBuffer.remove(ncommand_end);

                    String ip = parseRemoteUDPIP(remoteIP);
                    String port = parseRemoteUDPPort(remoteIP);
                    onOtherAskRelations(ip, port);
                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_ASK_BROADCAST_RELATIONS:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data
                    m_udpBuffer.remove(ncommand_end);
                    String strRelations = getSettings().loadStationsRelationString(m_context);
                    Object[] ar = new Object[]{strRelations, remoteIP};
                    //m_nLoadThreadCounter ++;

                    AsyncTask task = new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] params) {
                            String str =(String) params[0];
                            String ip = (String) params[1];
                            getBroadcaster().broadcastRequireRelations(str, ip);
                            return null;
                        }

                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ar);

                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_QUEUE_EXPO_BUMP:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data
                    byte n = m_udpBuffer.buffer().get(2);
                    boolean bEnabled = false;
                    if (n == 1)
                        bEnabled = true;
                    m_udpBuffer.remove(ncommand_end);
                    onSyncStationQueueExpoBumpSettingChanged(bEnabled);
                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_SMART_MODE_ENABLED:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data
                    byte n = m_udpBuffer.buffer().get(2);
                    int nVal = (int)n;
                    //KDSSettings.SmartMode m = KDSSettings.SmartMode.values()[nVal];

//                    boolean bEnabled = false;
//                    if (n == 1)
//                        bEnabled = true;
                    m_udpBuffer.remove(ncommand_end);
                    onSyncStationSmartModeEnabledSettingChanged(nVal);
                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_ITEM_BUMPED:
                case KDSSocketTCPCommandBuffer.UDP_ITEM_UNBUMPED: {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0) return; //need more data
                    byte[] bytes = m_udpBuffer.xml_command_data();
                    m_udpBuffer.remove(ncommand_end);
                    String utf8 = KDSUtil.convertUtf8BytesToString(bytes);

                    onPreparationTimeModeItemBumpUnbumped(utf8, (command ==KDSSocketTCPCommandBuffer.UDP_ITEM_BUMPED) );
                }
                    break;
                default: {
                    m_udpBuffer.remove(1);//bad command, check next loop
                    KDSLog.d(TAG,KDSLog._FUNCLINE_() + "Get bad UDP command data!");

                    break;
                }
            }
        }
    }



    public void onStatisticAppRequireStationAnnounceInThread2(byte b0,byte  b1, byte b2, byte b3)
    {
        String statistic_ip = String.format("%d.%d.%d.%d", KDSUtil.byteToUnsignedInt(b0), KDSUtil.byteToUnsignedInt(b1), KDSUtil.byteToUnsignedInt(b2), KDSUtil.byteToUnsignedInt(b3));

        int port = this.m_nStationsPort;
        String strport = KDSUtil.convertIntToString(port);
        int nItemsCount = getAllItemsCount();
        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildReturnStationIPCommand2(getStationID(),m_strLocalIP, strport, getLocalMacAddress(), nItemsCount, getSettings().getInt(KDSSettings.ID.Users_Mode));
        //send data to statistic station.
         (new KDSBroadcastThread(m_udpStationAnnouncer, statistic_ip,KDSSettings.UDP_STATISTIC_ANNOUNCER_PORT, buf )).start();
    }


    /**
     *
     * @param sock
     * @param remoteIP
     *  Format:
     *      /IP:port
     * @param buffer
     * @param nLength
     */
    public void onUdpReceiveData(KDSSocketInterface sock,String remoteIP,  ByteBuffer buffer, int nLength) {
        try {

            doUdpReceiveData(sock, remoteIP, buffer, nLength);
        }
        catch (Exception err)
        {
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + err.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_(), err );
        }

    }


    public String parseRemoteUDPIP(String remoteIP)
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

    public String parseRemoteUDPPort(String remoteIP)
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


    boolean m_bRelationsDifferentErrorShown = false;
    /**
     *  UDP get xml command string
     * @param xmlCommand
     *  Format:
     *      <Relations>Strings .... </Relations>
     */
    public void doUdpXmlCommand(String xmlCommand, String remoteIP)
    {
        if (xmlCommand.indexOf("<Relations>") >= 0)
        {
            String ip = parseRemoteUDPIP(remoteIP);
            String port = parseRemoteUDPPort(remoteIP);
            if (ip.equals(getLocalIpAddress()))
            {
                if (port.equals(KDSUtil.convertIntToString(KDSSettings.UDP_ANNOUNCER_PORT)))
                    return;
            }
            String s = xmlCommand;
            s = s.replace("<Relations>", "");
            s = s.replace("</Relations>", "");
            if (s.isEmpty()) return;
            KDSSettings.saveStationsRelation(m_context, s);
            //update the station ID.
            this.updateSettings(m_context);

            for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
                m_arKdsEventsReceiver.get(i).onReceiveNewRelations();

        }
        else if (xmlCommand.indexOf("<RelationsRet>") >= 0)
        { //ask the relations, and others return.
         //   if (isRequireRelationsFinished()) return;
            String s = xmlCommand;
            s = s.replace("<RelationsRet>", "");
            s = s.replace("</RelationsRet>", "");
           // if (s.isEmpty()) return;
            ArrayList<KDSStationsRelation> ar =KDSSettings.parseStationsRelations(s);
            if (isDifferentRelationsWithMyLocal(ar))
            {
                if (m_bRelationsDifferentErrorShown) return;
                m_bRelationsDifferentErrorShown = true;
               for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
                   m_arKdsEventsReceiver.get(i).onReceiveRelationsDifferent();

            }
        }
        else if (xmlCommand.indexOf("<TTBump>") >=0)
        {
            String s = xmlCommand;
            s = s.replace("<TTBump>", "");
            for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
                m_arKdsEventsReceiver.get(i).onTTBumpOrder(s);


        }
    }
    KDSStationsRelation findStation( List<KDSStationsRelation> lst, String stationID)
    {
        int ncount = lst.size();
        for (int i=0; i< ncount; i++)
        {
            if (lst.get(i).getID().equals(stationID))
                return lst.get(i);
        }
        return null;
    }

    private boolean isDifferentRelationsWithMyLocal(ArrayList<KDSStationsRelation> arReceived)
    {
        ArrayList<KDSStationsRelation> arLocal = KDSSettings.loadStationsRelation(m_context);
        if (arReceived.size() != arLocal.size())
            return true;
        for (int i=0; i< arReceived.size(); i++) {
            KDSStationsRelation r = arReceived.get(i);

            KDSStationsRelation relationOriginal = findStation(arLocal, r.getID());
            if (relationOriginal == null)
                return true;
            if  (!relationOriginal.isRelationEqual(r))
                return true;

        }
        return false;

    }

    //check how long time don't receive udp data.
    TimeDog m_annoucerTimeDog = new TimeDog();
    /**
     * id,ip,port string
     * @param strInfo
     */
    private void onUdpReceiveStationAnnounce(String strInfo)
    {

        doStationAnnounceInThread(strInfo);

//
//        m_annoucerTimeDog.reset();
//        boolean bNewStation = false;
//        //remove all failed stations.
//        m_stationsConnection.checkAllNoResponseStations();
//
//        ArrayList<String> ar = KDSUtil.spliteString(strInfo, ",");
//        if (ar.size() < 4)
//            return;
//        String id = ar.get(0);
//        String ip = ar.get(1);
//        String port = ar.get(2);
//        String mac = ar.get(3);
//        String itemsCount ="";
//        if (ar.size() >=5) //add orders count
//            itemsCount = ar.get(4);
//
//        int nUserMode = 0;
//
//        if (ar.size() >=6)
//        {
//            int n = KDSUtil.convertStringToInt( ar.get(5),0 );
//            if (n <0 || n>1)
//                n = 0;
//            nUserMode = n;
//
//
//        }
//
//        KDSStationActived station =m_stationsConnection.findActivedStationByMac(mac);//id);
//        if (station == null) {
//            station = new KDSStationActived();
//            //m_arActivedStations.add(station);
//            m_stationsConnection.addActiveStation(station);
//            bNewStation = true;
//        }
//        station.setID(id);
//        station.setIP(ip);
//        station.setPort(port);
//        station.setMac(mac);
//        station.setStationContainItemsCount(itemsCount);
//        station.setUserMode(nUserMode);
//        station.updatePulseTime();//record last received time
//
//        //some connection don't have the station ID in it. use this function to update them.
//        //comment it for debuging the connect with data function.
//        m_stationsConnection.refreshAllExistedConnectionInfo();
//
//        if (m_stationAnnounceEvents != null)
//            m_stationAnnounceEvents.onReceivedStationAnnounce(station);//id, ip, port, mac);
//        if (bNewStation)
//            announce_restore_pulse(id, ip);

    }
    public KDSStationConnection getSocketConnection(KDSSocketInterface sock)
    {
        return m_stationsConnection.getConnection(sock);
    }
    /**
     * tcp client connect to server
     * @param sock
     */
    public void sockevent_onTCPConnected(KDSSocketInterface sock)
    {

        //if (m_eventReceiver != null) {
        String ip = getSocketIP(sock);

        m_stationsConnection.onIPConnected(this, ip);
        KDSStationConnection conn = getSocketConnection(sock);
            //KDSSocketTCPSideClient c = (KDSSocketTCPSideClient)sock;
            for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
                m_arKdsEventsReceiver.get(i).onStationConnected(ip, conn);
            //m_eventReceiver.onStationConnected(ip);

        //}
    }

    /**
     * call back from  m_stationsConnection.onIPConnected
     * @param
     * @param
     */
    public void onFinishSendBufferedData(KDSStationDataBuffered bufferedData)//String strDescription, String orderGuid)
    {
        if (!bufferedData.getOrderGuid().isEmpty() && bufferedData.getItemGuid().isEmpty())
        { //it is for transfering order
            //remove this transfered order
            String orderGuid = bufferedData.getOrderGuid();
            String strDescription = bufferedData.getDescription();
            KDSDataOrder order = m_users.getOrderByGUID(orderGuid);// getOrders().getOrderByGUID(orderGuid);
            if (order == null) return;
            //getOrders().removeComponent(order);
            m_users.orderRemove(order);
            getCurrentDB().orderDelete(order.getGUID());
            refreshView();
            showMessage(strDescription);
        }
        else if (!bufferedData.getItemGuid().isEmpty())
        {
            String orderGuid = bufferedData.getOrderGuid();
            String strDescription = bufferedData.getDescription();
            KDSDataOrder order = m_users.getOrderByGUID(orderGuid);// getOrders().getOrderByGUID(orderGuid);
            if (order == null) return;
            int n= order.getItems().getItemIndexByGUID(bufferedData.getItemGuid());
            order.getItems().removeComponent(n);

            getCurrentDB().deleteItem(bufferedData.getItemGuid());
            refreshView();
            showMessage(strDescription);
        }
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
        m_stationsConnection.onIPDisconnected(sock, ip);

        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
            m_arKdsEventsReceiver.get(i).onStationDisconnected(ip);

    }

    public void sockevent_onUDPReceiveXml(KDSSocketInterface sock, String xmlData)
    {

    }

    public void sockevent_onWroteDataDone(KDSSocketInterface sock, String remoteIP,  int nLength)
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
        //check if this ip is my primary of mirror/backup,
        //if it is , stop read remote folder thread. and wait for primary ask latest data.

        boolean bPrimaryBackupLost = getStationsConnections().isMyPrimaryBackupStation(stationID);
        if (bPrimaryBackupLost )
        {//my primary mirror/backup stations are lost, start my
            //this.m_smbDataSource.start();
            if (!m_kdsState.getPrimaryOfBackupLost()) {
                startRemoteFolderDataSource(m_settings);
                m_kdsState.setPrimaryOfBackupLost(true);
                onPrimaryBackupStationLost(stationID, stationIP);
            }
            return;
        }

        boolean bPrimaryMirrorLost = getStationsConnections().isMyPrimaryMirrorStation(stationID);
        if ( bPrimaryMirrorLost)
        {//my primary mirror/backup stations are lost, start my

            startRemoteFolderDataSource(m_settings);

        }
        else
        {

        }

        boolean bMyBackupStationLost = getStationsConnections().isMyBackupStation(stationID);
        if (bMyBackupStationLost)
        {
            String s = "My backup sation #" + stationID + " lost";
            showMessage(s);
        }

    }



    /**
     * My station serve as backup of this stationID
     */
    public void onPrimaryBackupStationRestored(String stationID, String ip)
    {
        String s = "My primary station #" + stationID + " back";
        showMessage(s);
        m_kdsState.setPrimaryOfBackupLost(false);
    }

    public void onPrimaryMirrorStationRestored(String stationID, String ip)
    {

    }
    /**
     * ask if its support data is empty
     * 1. empty:
     *
     * @param stationID
     * @param ip
     */
    public void onMyBackupStationRestore(String stationID, String ip)
    {
        String s = "My backup station #" + stationID + " back";
        showMessage(s);
        askStationDatabaseStatus(stationID, ip);
    }

    private void askStationDatabaseStatus(String stationID, String ip )
    {
        //check if the "support" database is empty first.
        String strXml = KDSXMLParserCommand.createAskSupportDBStatus(this.getStationID(), this.getLocalIpAddress(), "");
        KDSStationConnection conn = m_stationsConnection.findConnectionByID(stationID);
        if (conn != null) {
            if (conn.getSock().isConnected())
                conn.getSock().writeXmlTextCommand(strXml);
            else
                conn.addBufferedData(strXml);
        }
        else
        {
            KDSStationIP station = new KDSStationIP();
            station.setID(stationID);
            station.setIP(ip);
            station.setPort(m_settings.getString(KDSSettings.ID.KDS_Station_Port));

            m_stationsConnection.connectStationWithData(station, strXml);
        }
    }
    /**
     * my mirror station restored
     * Ask status, then decide to do what work.
     * See return status function  KDSStationFunc.doStationReturnDbStatus
     * @param stationID
     * @param ip
     */
    public void onMyMirrorStationRestore(String stationID, String ip)
    {
        String s = "My mirror station #" + stationID + " back";
        showMessage(s);
        askStationDatabaseStatus(stationID, ip);
    }


    /**
     * The primary backup station lost.
     * Do following work:
     * copy backup station "support" data to "current", clear "support" data.
     */
    public void onPrimaryBackupStationLost(String stationID, String stationIP)
    {
        String s = "My primary station #" + stationID + " lost";
        showMessage(s);
        if (!m_dbCurrent.combineDatabaseOrders(m_dbSupport)) {
            KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Can not copy support db to current");
            return;
        }
        m_dbSupport.clear();
        loadAllActiveOrders();

        setFocusAfterReceiveOrder();
        setFocusToDefault();
        refreshView();
        KDSLog.d(TAG,KDSLog._FUNCLINE_()+ "After onPrimaryBackupStationLost ");
        getSoundManager().playSound(KDSSettings.ID.Sound_backup_station_activation);

    }

    /**
     * The station/ip don't receive the udp announce in 10secs
     * @param stationID
     * @param stationIP
     */
    public void announce_restore_pulse(String stationID, String stationIP)
    {
        KDSLog.i(TAG, KDSLog._FUNCLINE_() + "Station=" + stationID + ",IP=" + stationIP);

        boolean bPrimaryBackupRestored = getStationsConnections().isMyPrimaryBackupStation(stationID);
        if (bPrimaryBackupRestored )
        {//my primary mirror/backup stations are back, stop my reading thread.
            //waiting for restore primary data
            KDSLog.d(TAG, KDSLog._FUNCLINE_() + "bPrimaryBackupRestored=true");
            this.m_smbDataSource.stop();
            onPrimaryBackupStationRestored(stationID, stationIP);
            return;
        }

        boolean bPrimaryMirrorRestored = getStationsConnections().isMyPrimaryMirrorStation(stationID);

        if ( bPrimaryMirrorRestored)
        {//my primary mirror/backup stations are back, stop my reading thread.
            //
            KDSLog.d(TAG, KDSLog._FUNCLINE_() + "bPrimaryMirrorRestored=true");
            this.m_smbDataSource.stop();
            onPrimaryMirrorStationRestored(stationID, stationIP);
            return;
        }
        if (getStationsConnections().isMyBackupStation(stationID))
        {
            KDSLog.d(TAG, KDSLog._FUNCLINE_() + "isMyBackupStation=true");
            onMyBackupStationRestore(stationID, stationIP);
           // showMessage("After check backup database status");
        }

        if (getStationsConnections().isMyMirrorStation(stationID))
        {
            KDSLog.d(TAG, KDSLog._FUNCLINE_() + "isMyMirrorStation=true");
            onMyMirrorStationRestore(stationID, stationIP);
        }

    }



    /**
     * This function was call in socket thread, if there are any GUI drawing function, please change to main thread.
     * @param smb
     * @param xmlData
     */
    public void smbevent_onSMBReceiveXml(KDSSMBDataSource smb,String smbFileName, String xmlData)
    {
        if (xmlData.indexOf(KDSSMBDataSource.TAG_SMBERROR_START)>=0)
        {
            //doSmbError(xmlData); //20170612
            Message msg = new Message();
            msg.obj = xmlData;
            msg.what = MESSAGE_TO_MAIN.SMB_ERROR.ordinal();
            //m_smbErrorHandler.sendMessage(msg);
            m_refreshHandler.sendMessage(msg);

        }
        else {
            doOrderXml(null, xmlData,smbFileName, true); //2.0.34
        }
        //Log.d("SMB Text", xmlData);
    }

    public Toast m_errorToast = null;
    public void doSmbError(String xmlData)
    {
        String s = xmlData;
        s = s.replace(KDSSMBDataSource.TAG_SMBERROR_START,  "");
        s = s.replace(KDSSMBDataSource.TAG_SMBERROR_END,  "");
        showMessage(s);
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

    /**
     * it is in socket thread call this function,
     * so, if update the gui, please change data to main thread first.
     * @param sock
     * @param xmlData
     */
    public void sockevent_onTCPReceiveXml(KDSSocketInterface sock, String xmlData)
    {

        if (!isEnabled()) return;
        KDSXMLParser.XMLType ntype = checkXmlType(xmlData);

        switch (ntype)
        {
            case Unknown:
                return;
            case Order:
                //doOrderXml(sock, xmlData);
                Message msgOrder = new Message();
                msgOrder.what = MESSAGE_TO_MAIN.Order.ordinal();
                MessageParam paramOrder = new MessageParam();
                paramOrder.obj0 = sock;
                paramOrder.obj1 = xmlData;

                msgOrder.obj = paramOrder;

                m_refreshHandler.sendMessage(msgOrder);
                break;
            case Command:
                Message msg = new Message();
                msg.what = MESSAGE_TO_MAIN.COMMAND_XML.ordinal();
                MessageParam p = new MessageParam();
                p.obj0 = sock;
                p.obj1 = xmlData;
                msg.obj = p;
                m_refreshHandler.sendMessage(msg);
                //doCommandXml(sock, xmlData); //20170612
                break;
        }
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
        if (sock == m_listenPOS)
            m_stationsConnection.onAcceptPOSConnection(sock, sockClient);
            //ar = m_arPOSStations;//.add(station);
        else if (sock == m_listenStations)
            m_stationsConnection.onAcceptStationConnection(sock, sockClient);
        else if (sock == m_listenStatistic)
        {
            m_stationsConnection.onAcceptStatisticConnection(sock, sockClient);
        }
            //ar = m_arConnectMeStations;//.add(station);

        if (!ip.isEmpty()) {
            for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
                m_arKdsEventsReceiver.get(i).onAcceptIP(ip);

        }



    }



    public void checkStationsSettingChanged(Context context)
    {
        m_stationsConnection.refreshRelations(context, this.getStationID());
        updateStationFunction();
    }


    private KDSXMLParser.XMLType checkXmlType(String strxml)
    {
        return KDSXMLParser.checkXmlType(strxml);
    }


    /**
     *
     * @param objSource
     *  where we get this order xml data.
     *  It maybe KDSSocketInterface and KDSSMBDataSource
     *  We need it to write back the status xml file.
     * @param order
     */
    private void doAskOrderState(Object objSource, KDSDataOrder order)
    {
       String orderName = order.getOrderName();
        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
            m_arKdsEventsReceiver.get(i).onAskOrderState(objSource, orderName);

    }

    private void changeTrackerIDByUserInfo(KDSDataOrder order)
    {

        int n = KDSSettings.getEnumIndexValues(getSettings(),KDSSettings.TrackerPager_ID_From_Tag.class,KDSSettings.ID.Tracker_use_userinfo_guesttable  );
        KDSSettings.TrackerPager_ID_From_Tag tag = KDSSettings.TrackerPager_ID_From_Tag.values()[n];

        switch (tag)
        {

            case None:
                break;
            case UserInfo:
                if (order.getTrackerID().isEmpty()) {
                    order.setTrackerID(order.getCustomMsg());
                    order.setCustomMsg("");
                }
                break;
            case GuestTable:
                if (order.getTrackerID().isEmpty()) {
                    order.setTrackerID(order.getToTable());
                    order.setToTable("");
                }
                break;
        }

    }

    private void changePagerIDByUserInfo(KDSDataOrder order)
    {
        if (!getSettings().getBoolean(KDSSettings.ID.Pager_enabled))
            return;

        int n = KDSSettings.getEnumIndexValues(getSettings(),KDSSettings.TrackerPager_ID_From_Tag.class,KDSSettings.ID.Pager_use_userinfo_guesttable  );
        KDSSettings.TrackerPager_ID_From_Tag tag = KDSSettings.TrackerPager_ID_From_Tag.values()[n];
        switch (tag)
        {

            case None:
                break;
            case UserInfo:
                if (order.getPagerID().isEmpty()) {
                    order.setPagerID(order.getCustomMsg());
                    order.setCustomMsg("");
                }
                break;
            case GuestTable:
                if (order.getPagerID().isEmpty()) {
                    order.setPagerID(order.getToTable());
                    order.setToTable("");
                }
                break;
        }
    }

    private enum MESSAGE_TO_MAIN {
        REFRESH_A,
        REFRESH_B,
        REFRESH_ALL,
        ASK_ORDER_STATUS,
        SMB_ERROR,
        COMMAND_XML,
        Order,
    }

    Handler m_refreshHandler = new Handler(){
        public void handleMessage(Message msg) {
            int n = msg.what;
            MESSAGE_TO_MAIN w = MESSAGE_TO_MAIN.values()[n];
            switch (w) {
                case REFRESH_ALL:
                    KDS.this.doRefreshView();
                    break;
                case REFRESH_A:
                    KDS.this.doRefreshView(KDSUser.USER.USER_A, (RefreshViewParam) msg.obj);
                    break;
                case REFRESH_B:
                    KDS.this.doRefreshView(KDSUser.USER.USER_B, (RefreshViewParam) msg.obj);
                    break;
                case ASK_ORDER_STATUS:
                    MessageParam a = (MessageParam)msg.obj;
                    Object obj =  a.obj0;
                    KDSDataOrder order = (KDSDataOrder)a.obj1;
                    KDS.this.doAskOrderState(obj, order);
                    break;
                case SMB_ERROR:
                    String strXml = (String)msg.obj;
                    KDS.this.doSmbError(strXml);
                    break;
                case COMMAND_XML:
                    MessageParam x = (MessageParam)msg.obj;

                    doCommandXml((KDSSocketInterface) x.obj0, (String)x.obj1);
                    break;
                case Order:
                    MessageParam xcode = (MessageParam)msg.obj;

                    doOrderXml((KDSSocketInterface) xcode.obj0, (String)xcode.obj1, "",false); //2.0.34
                    break;

            }
        }
    };


    class MessageParam
    {
        Object obj0 = null;
        Object obj1 = null;
        Object obj2 = null;

    }
    /**
     *   Rev.
     *  2.0.34
     *      , boolean bForceAcceptThisOrder,
     *             For setup without router, right now if you send order to station,
     *             it still requires <KDSStation> tag in order for order to display.
     *             Let’s remove this limitation, which means when KDS receive the xml,
     *             it will display all items on this xml. And please let us know if we apply this, will this affect any features?
     *
     * @param objSource
     *  where we get this order xml data.
     *  It maybe KDSSocketInterface and KDSSMBDataSource
     * @param xmlData
     */
    //private void doOrderXml(KDSSocketInterface sock, String xmlData)
    public void doOrderXml(Object objSource, String xmlData,String originalFileName, boolean bForceAcceptThisOrder)
    {
        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(getStationID(), xmlData);

        //2.0.39
        if (order == null) {
            if (KDSConst.ENABLE_FEATURE_ORDER_ACKNOWLEDGEMENT) {
                if (this.getSettings().getBoolean(KDSSettings.ID.Notification_order_acknowledgement)) {
                    doOrderAcknowledgement(objSource, order, 0, originalFileName, xmlData, KDSUtil.createNewGUID(), KDSPosNotificationFactory.ACK_ERR_BAD, false);
                }
            }
            return;
        }
        else
        {//2.0.44
            if (KDSConst.ENABLE_FEATURE_ORDER_ACKNOWLEDGEMENT) {
                if (this.getSettings().getBoolean(KDSSettings.ID.Notification_order_acknowledgement)) {//ack error ack
                    KDSPosNotificationFactory.OrderParamError error = KDSPosNotificationFactory.checkOrderParameters(order);
                    if (error != KDSPosNotificationFactory.OrderParamError.OK) {
                        String errorCode = KDSPosNotificationFactory.getOrderParamErrorCode(error);
                        if (objSource != null) {
                            ArrayList<KDSToStation> ar = KDSPosNotificationFactory.getOrderTargetStations(order);
                            if (KDSToStations.findInToStationsArray(ar, getStationID()) != KDSToStations.PrimarySlaveStation.Unknown)
                               doOrderAcknowledgement(objSource, order, 1, originalFileName, xmlData, KDSUtil.createNewGUID(), errorCode, false);
                        }
                        return;
                    }
                }
            }
        }
        //preparation time mode
        //boolean bPrepEnabled = (KDSSettings.SmartMode.values()[ this.getSettings().getInt(KDSSettings.ID.Smart_mode)] == KDSSettings.SmartMode.Advanced );
        boolean bSmartEnabled = this.getSettings().getBoolean(KDSSettings.ID.Smart_Order_Enabled);

        //if (this.getSettings().getBoolean(KDSSettings.ID.Prep_mode_enabled))
        //if (bPrepEnabled)
          if (bSmartEnabled)
            this.getCurrentDB().prep_add_order_items(order); //keep full order items for preparation time mode.
//
        if (isTrackerStation() || isTrackerView())
        {
            changeTrackerIDByUserInfo(order);

            if (order.getOrderType().equals(KDSDataOrder.ORDER_TYPE_SCHEDULE))
                return; //tracker don't need schedule
        }

        //for pager feature
        if (isExpeditorStation() ||isQueueExpo() || isQueueExpoView())
            changePagerIDByUserInfo(order);

        int nAcceptItemsCount = 0;
        if (order.getTransType() == KDSDataOrder.TRANSTYPE_ASK_STATUS) {

            //doAskOrderState(objSource, order); //20170612
            Message msg = new Message();
            MessageParam a = new MessageParam();
            a.obj0 = objSource;
            a.obj1 = order;

            msg.obj = a;
            msg.what = MESSAGE_TO_MAIN.ASK_ORDER_STATUS.ordinal();
            //m_askStatusHandler.sendMessage(msg);
            m_refreshHandler.sendMessage(msg);
        }
        else {
            //update the hidden option accroding to my station ID.
            order.setItemHiddenOptionAfterGetNewOrder(getStationID());

            nAcceptItemsCount = doOrderFilter(order, bForceAcceptThisOrder);
            schedule_process_update_after_receive_new_order();
        }

        refreshView();
        if (this.getSettings().getBoolean(KDSSettings.ID.Notification_order_acknowledgement))
        {
            doOrderAcknowledgement(objSource,order,nAcceptItemsCount,originalFileName, xmlData, order.getOrderName(),KDSPosNotificationFactory.ACK_ERR_OK, true);
        }


    }

    private String smbFullPathToFileName(String smbFileName)
    {
        String s = smbFileName;

        int n = s.lastIndexOf("/");
        if (n<0) return smbFileName;
        String f = s.substring(n+1);
        return f;
    }

    /**
     * notification pos order acknowledgement.
     * @param objSource
     * @param xmlData
     */
    private void doOrderAcknowledgement(Object objSource,KDSDataOrder order, int nAcceptItemsCount,String smbFileName, String xmlData,String orderName,String errorCode, boolean bGoodOrder)
    {
        if (!KDSConst.ENABLE_FEATURE_ORDER_ACKNOWLEDGEMENT) return;
        if (nAcceptItemsCount <=0) {//2.0.43
//            KDSSettings.KDSDataSource source = KDSSettings.KDSDataSource.values()[getSettings().getInt(KDSSettings.ID.KDS_Data_Source)];
//            if (source == KDSSettings.KDSDataSource.TCPIP)
                return;
        }
        SettingsBase.StationFunc func = getStationFunction();

        if (func == SettingsBase.StationFunc.Queue ||
                func == SettingsBase.StationFunc.TableTracker ||
                func == SettingsBase.StationFunc.Queue_Expo ||
                func == SettingsBase.StationFunc.Duplicate ||
                func == SettingsBase.StationFunc.Mirror ||
                func == SettingsBase.StationFunc.Workload)
            return;
        if (func == SettingsBase.StationFunc.Backup)
        {
            //check if my primary is active
            ArrayList<KDSStationIP> ar = m_stationsConnection.getRelations().getPrimaryStationsWhoUseMeAsBackup();
            for (int i=0; i< ar.size(); i++)
            {
                if (m_stationsConnection.isActiveStation(ar.get(i)) )
                    return;
            }
        }

        String fileName = KDSPosNotificationFactory.createOrderAckFileName(order, nAcceptItemsCount, smbFileName, orderName, bGoodOrder);
        String s = KDSPosNotificationFactory.createOrderAcknowledgement(getStationID(), order,nAcceptItemsCount ,smbFileName, xmlData, orderName, errorCode, bGoodOrder, fileName);
        Object objSrc = objSource;

        writeXmlToPOSOrderAcknowledgement(objSrc, s, fileName);
    }


    /**
     * receive the command xml
     * @param sock
     * @param xmlData
     */
    private void doCommandXml(KDSSocketInterface sock, String xmlData)
    {
        KDSXMLParserCommand command = (KDSXMLParserCommand)KDSXMLParser.parseXml(this.getStationID(), xmlData);
        KDSXMLParserCommand.KDSCommand code = command.getCode();
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        if (fromStationID.equals(this.getStationID()) &&
                code != KDSXMLParserCommand.KDSCommand.Station_Transfer_Order)
            return; //don't do loop
        String orderGuidDoOperation = "";
        //showMessage("receive command="+KDSUtil.convertIntToString(code.ordinal()));
        switch (code)
        {

            case Nothing:
                break;
            case Require_Station_Statistic_DB:
                break;
            case Require_Station_Daily_DB:
                break;
            case Require_Station_Configuration: //other station ask configuration
                doConfigRequireXmlCommand(sock, command, xmlData);
                break;
            case Broadcast_Station_Configuration: //I received configuation from other
                doConfigReceivedXmlCommand( command, xmlData);
                break;
            case Broadcast_Station_Active:
                break;
            case Broadcast_All_Configurations:
                break;
            case Station_Add_New_Order:
                KDSStationFunc.doSyncCommandOrderNew(this, command, xmlData);
                setFocusAfterReceiveOrder();
                schedule_process_update_after_receive_new_order();
                break;
            case Station_Bump_Order:

                checkLostFocusAfterSyncBumpOrderName(command, xmlData);
                KDSStationFunc.doSyncCommandOrderBumped(this,command, xmlData);
                schedule_process_update_after_receive_new_order();
                sortOrderForMoveFinishedToFront();
                break;
            case Station_Unbump_Order:
                KDSStationFunc.doSyncCommandOrderUnbumped(this,command, xmlData);
                schedule_process_update_to_be_prepare_qty();
                schedule_process_update_after_receive_new_order();
                sortOrderForMoveFinishedToFront();
                break;
            case Station_Cancel_Order:
                KDSStationFunc.doSyncCommandOrderCanceled(this,command, xmlData);
                break;
            case Station_Modify_Order:
                KDSStationFunc. doSyncCommandOrderModified(this,command, xmlData);
                break;
            case Station_Bump_Item:

                KDSStationFunc.doSyncCommandItemBumped(this,command, xmlData);
                sortOrderForMoveFinishedToFront();
                break;
            case Station_Unbump_Item:
                KDSStationFunc.doSyncCommandItemUnbumped(this,command, xmlData);
                sortOrderForMoveFinishedToFront();
                schedule_process_update_to_be_prepare_qty();
                break;
            case Station_Modified_Item:
                KDSStationFunc.doSyncCommandItemModified(this,command, xmlData);
                break;
            case Station_Transfer_Order://order transfered to me
                KDSStationFunc.doSyncCommandOrderTransfer(this, command, xmlData);
                setFocusAfterReceiveOrder();
                getSoundManager().playSound(KDSSettings.ID.Sound_transfer_order);
                break;
            case Station_Transfer_Order_ACK:
                break;
            case DBSupport_Sql: //get support database sql from others station, e.g: the primary backup station.
                //showMessage("receive support db sql");
                KDSStationFunc.doSqlSupportDB(this, command, xmlData);
                break;
            case DBCurrent_Sql:
                KDSStationFunc.doSqlCurrentDB(this, command, xmlData);

                break;
            case DBSync_Broadcast_Current_Db_Updated:
                break;
            case DBSync_Broadcast_Station_Sql_Sync_Updated:
                break;
            case Broadcast_Ask_Active_Info:
                break;
            case DB_Ask_Status: //ask support database status, empty or not empty.
            {
                KDSStationFunc.doAskDBStatus(this, command, xmlData);
                //showMessage("Ask db status");
            }
            break;
            case DB_Return_Status: //return support database is empty status
            {
                KDSStationFunc.doStationReturnDbStatus(this, command, xmlData);
                //loadAllActiveOrders();
                //refreshView();
            }
            break;
            case DB_Copy_Current_To_Support: //reverse the data again, it is in backup station.
            {
                this.getSupportDB().clear();
                this.getSupportDB().copyDB(this.getCurrentDB());
                this.getCurrentDB().clear();
                loadAllActiveOrders();
                refreshView();

            }
            break;
            case Expo_Bump_Order: //expo and normal station has different steps, those are the expo operations and will inform to its backup/mirror
            {
                KDSStationFunc.doSyncCommandExpoOrderBumped(this, command, xmlData);
            }
            break;
            case Expo_Unbump_Order:
            {
                KDSStationFunc.doSyncCommandExpoOrderUnbumped(this, command, xmlData);
            }
            break;
            case Expo_Bump_Item:
            {
                KDSStationFunc.doSyncCommandExpoItemBumped(this, command, xmlData);
            }
            break;
            case Expo_Unbump_Item:
            {
                KDSStationFunc.doSyncCommandExpoItemUnbumped(this, command, xmlData);
            }
            break;
            case Statistic_Ask_DB_Data:
            {

//                String timestamp = command.getParam(KDSConst.KDS_Str_Param, "0");
//                String existedGuidInTimeStamp = command.getParam("P0", "");
//                onStatisticAskData2(timestamp, sock, this, command, xmlData, existedGuidInTimeStamp);
            }
            break;
            case Schedule_Item_Ready_Qty_Changed:
            {//the schedule read qty changed.From normal station that contains schedule order, this station just input ready qty.
                KDSStationFunc.doSyncCommandScheduleItemQtyChanged(this,command, xmlData);
            }
            break;
            case Queue_Ready:
            {
                KDSStationFunc.doSyncCommandQueueOrderReady(this, command, xmlData);
            }
            break;
            case Queue_Unready:
            {
                KDSStationFunc.doSyncCommandQueueOrderUnready(this, command, xmlData);
            }
            break;
            case Queue_Pickup:
            {

                queuePickup(command, xmlData);
            }
            break;
            case Sync_Settings_Queue_Expo_Double_Bump:
            {
                KDSStationFunc.sync_settings_queue_expo_double_bump(this, command, xmlData);
            }
            break;
            case Tracker_Bump_Order:
            {

                KDSStationFunc.doSyncCommandTrackerOrderBumped(this, command);
            }
            break;
            case Station_Cook_Started:
            {
                KDSStationFunc.doSyncCommandCookStarted(this, command);


            }
            break;
            case Statistic_Request_Report:
            {
                doStatisticReport(sock, command);
            }
            break;
            //2.0.10, create the sos report
            case SOS_Request_Report:
            {
                doSosReport(sock, command);
            }
            break;


        }
    }

    public void queuePickup(KDSXMLParserCommand command ,String strXmlCommand)
    {
        KDSStationExpeditor.queue_sync_expo_order_pickup(this, command);
        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
            m_arKdsEventsReceiver.get(i).onSetFocusToOrder("");

    }
    /**
     *
     *
     * csv string for guids
     */
    public void checkLostFocusAfterSyncBumpOrderName( KDSXMLParserCommand command ,String strXmlCommand)
    {

        if (isExpeditorStation()) return;//2.0.15

        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(getStationID(), strXml);
        if (order == null) return;
        String orderName = order.getOrderName();
        KDSDataOrder existedOrder =this.getUsers().getUserA().getOrders().getOrderByName(orderName);

        String userAGuid = "";
        if (existedOrder!= null)
            userAGuid =existedOrder.getGUID();
        String userBGuid = "";
        String guids = userAGuid;
        if (isMultpleUsersMode())
        {
            existedOrder  =  this.getUsers().getUserB().getOrders().getOrderByName(orderName);
            if (existedOrder != null)
                userBGuid =  existedOrder.getGUID();
            if (!guids.isEmpty())
                guids += ",";
            guids += userBGuid;


        }
        if (guids.isEmpty()) return;


        for (int i=0; i< m_arKdsEventsReceiver.size(); i++) {
            m_arKdsEventsReceiver.get(i).onXmlCommandBumpOrder(guids);
        }
    }



    public void onStatisticAskData2(String timestamp,KDSSocketInterface sock, KDS kds,KDSXMLParserCommand command, String strOrinalData, String existedInStatisticDb)
    {
        Object[] params={timestamp, sock, kds,existedInStatisticDb};
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                String tm = (String)objects[0];
                KDSSocketInterface sk = (KDSSocketInterface)objects[1];
                KDS mKDS = (KDS)objects[2];
                String existed = (String)objects[3];

                KDSStationFunc.doStatisticAppAskData3(sk, mKDS, null,null, tm, existed);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params );
    }

    /**
     * 1.       For setup without router, right now if you send order to station, it still requires <KDSStation> tag in order for order to display. Let’s remove this limitation, which means when KDS receive the xml, it will display all items on this xml. And please let us know if we apply this, will this affect any features?
     [david]: I have implement this feature.
     It will not affect others features. But, please notice, this will make it will accept any order when kds get it from remote folder.

     [ec]  Note that when KDS receive the xml data.  If KDSStation tag is not found, then it will accept the order, assuming that it is the default <KDSStation n> where n is the number of the KDS station receiving the order.   If KDSStation tag is found, then it must process the tag accordingly.  That is, check whether the station address match itself and also check split screen and multiple station info.

     * @param order
     * @param bForceAcceptThisOrderNoStationIDItems
     */
    private void assignStationIDAsOrderFromRemoteFolder(KDSDataOrder order, boolean bForceAcceptThisOrderNoStationIDItems)
    {
        if (!bForceAcceptThisOrderNoStationIDItems) return;
        for (int i=0; i< order.getItems().getCount(); i++)
        {
            KDSDataItem item = order.getItems().getItem(i);
            if (item.getToStations().getCount()<=0)
            {
                item.getToStations().addStation(this.getStationID());
            }
        }
        return;
    }
    /**
     * Rev.
     *  2.0.34
     *      , boolean bForceAcceptThisOrder,
     *             For setup without router, right now if you send order to station,
     *             it still requires <KDSStation> tag in order for order to display.
     *             Let’s remove this limitation, which means when KDS receive the xml,
     *             it will display all items on this xml. And please let us know if we apply this, will this affect any features?
     * check if this order is for this station
     * 2.0.36
     *   The tag limitation only works in remote folder now, please also apply this change to TCP/IP orders.
     *   Notice: I don't agree this change.
     *
     *   return accept items count
     * @param order
     *
     */
    public int  doOrderFilter(KDSDataOrder order, boolean bForceAcceptThisOrderNoStationIDItems)
    {
        bForceAcceptThisOrderNoStationIDItems = true;//2.0.36
        //if (bForceAcceptThisOrderNoStationIDItems)//2.0.36
            assignStationIDAsOrderFromRemoteFolder(order, bForceAcceptThisOrderNoStationIDItems);

        order = justKeepMyStationItems(order);

        if (order == null) return 0;
        int nItemsCount = order.getItems().getCount();

        if (order.isScheduleOrder()) //schedule order
        {
            doScheduleOrderFilter(order);
        }
        else { //normal order

            if (m_stationsConnection.getRelations().getWorkLoadStations().size() > 0) {
                if (writeOrderToWorkLoad(order))
                    return nItemsCount;
            }
            filterInNormalStation(order);

        }
        return nItemsCount;
    }

    public void doScheduleOrderFilter(KDSDataOrder order)
    {

        //I comment it as we need expo receive item through the <KDSStation> tag.
//        order = justKeepMyStationItems(order);
//        if (order == null) return;

        ArrayList<ScheduleProcessOrder> arScheduleOrders = KDSDataOrder.splitForSchedule(order);
        for (int i=0; i<arScheduleOrders.size(); i++) {
            KDSDataOrder schOrder = arScheduleOrders.get(i);
            if (m_stationsConnection.getRelations().getWorkLoadStations().size() > 0) {
                if (writeOrderToWorkLoad(schOrder))
                    continue;
            }
            filterInNormalStation(schOrder);

        }

    }

    public boolean writeOrderToWorkLoad(KDSDataOrder order)
    {
        if (m_stationsConnection.getRelations().getWorkLoadStations().size()<=0 ) return false;
        KDSStationIP workLoadStation = m_stationsConnection.getRelations().getWorkLoadStations().get(0);
        KDSStationActived activedStation = m_stationsConnection.findActivedStationByID(workLoadStation.getID());
        if (activedStation == null) return false;
        if (activedStation.getStationContainItemsCount() >= this.getAllItemsCount())
            return false;

        for (int i=0; i< order.getItems().getCount(); i++)
        {
            order.getItems().getItem(i).setToStationsString(workLoadStation.getID());
        }
        m_stationsConnection.writeDataToStationOrItsBackup(workLoadStation, order.createXml());


        return true;
    }
    public boolean writeOrderToDuplicated(KDSDataOrder order)
    {
        if (m_stationsConnection.getRelations().getDuplicatedStations().size()<=0 ) return false;
        KDSStationIP duplicatedStation = m_stationsConnection.getRelations().getDuplicatedStations().get(0);
        if (duplicatedStation == null) return false;

        for (int i=0; i< order.getItems().getCount(); i++)
        {
            order.getItems().getItem(i).setToStationsString(duplicatedStation.getID());
        }
        m_stationsConnection.writeDataToStationOrItsBackup(duplicatedStation, order.createXml());
        return true;
    }

    public boolean isExpeditorStation()
    {
        return (getStationFunction() == KDSSettings.StationFunc.Expeditor);// ||
                //getStationFunction() == KDSSettings.StationFunc.Queue_Expo);
    }

    public boolean isPrepStation()
    {
        return (getStationFunction() == SettingsBase.StationFunc.Normal);
    }
    public boolean isQueueStation()
    {

        return (getStationFunction() == KDSSettings.StationFunc.Queue);

    }

    public boolean isQueueView()
    {
        return (m_settings.getFuncView() == KDSSettings.StationFunc.Queue );


    }


    public boolean isQueueExpo()
    {
        return (getStationFunction() == KDSSettings.StationFunc.Queue_Expo);
    }
    public boolean isQueueExpoView()
    {
        return  (m_settings.getFuncView() == KDSSettings.StationFunc.Queue_Expo);
    }

    public boolean isTrackerStation()
    {

        return (getStationFunction() == KDSSettings.StationFunc.TableTracker);

    }

    public boolean isTrackerView()
    {

        return (m_settings.getFuncView() == KDSSettings.StationFunc.TableTracker);

    }

    public boolean isPanelsView()
    {

        return ((!isQueueView()) && (!isTrackerView()) &&(!isQueueExpoView())) ;

    }


    /**
     * 2.0.33
     *   The KDS expo group doesn’t work in latest version,
     *   e.g.: 5 stations with 1,2,3 prep, and 4,5 expo and setup station relation as 12 go to 4 and 3 go to 5.
     *   If I send order to 12, only expo 4 shows the order, the order and if I send order to 3,
     *   then only expo 5 shows the order. But right now, both stations shows the order. (Do this fix first)
     * @param order
     * @return
     */
    private KDSDataOrder keepExpoItemsAccordingToStationsSetting(KDSDataOrder order)
    {
        if (!this.isExpeditorStation())
            return order;

        ArrayList<KDSStationIP> arPrepWhoUseMeAsExpo = this.getStationsConnections().getRelations().getPrepStationsWhoUseMeAsExpo(getStationID());
        KDSStationIP myStation = new KDSStationIP();
        myStation.setID(getStationID());
        arPrepWhoUseMeAsExpo.add(myStation);

        int ncount = order.getItems().getCount();
        for (int i=ncount-1; i>=0; i--) {
            KDSDataItem item =order.getItems().getItem(i);
            if (item.isExpitem())
                continue;
            if (item.getToStations().getCount()<=0)
                continue;
            if (!isItemToStations(item, arPrepWhoUseMeAsExpo))
            {
                order.getItems().removeComponent(i);
            }
        }
        return order;
    }

    private boolean isItemToStations(KDSDataItem item, ArrayList<KDSStationIP> arMyPrepStations)
    {

        for (int i=0; i< arMyPrepStations.size(); i++)
        {
            KDSToStations.PrimarySlaveStation r = item.getToStations().findStation(arMyPrepStations.get(i).getID());
            if (r != KDSToStations.PrimarySlaveStation.Unknown)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * rev.
     *  2.0.33
     *  The KDS expo group doesn’t work in latest version,
     *   e.g.: 5 stations with 1,2,3 prep, and 4,5 expo and setup station relation as 12 go to 4 and 3 go to 5.
     *   If I send order to 12, only expo 4 shows the order, the order and if I send order to 3,
     *   then only expo 5 shows the order. But right now, both stations shows the order. (Do this fix first)
     * @param order
     * @return
     */
    public KDSDataOrder justKeepMyStationItems(KDSDataOrder order)
    {
        //2.0.33
        if (this.isExpeditorStation())
            keepExpoItemsAccordingToStationsSetting(order);

        //20160418, keep all items if i am expo station
        //One bug I will need you to fix before moving to new project: Setup 2 station, 1 normal,
        // 1expo with router, send order to normal, both station get order, disconnect normal, expo will not receive any order(sample order attached).
        if (this.isExpeditorStation() ||
               // this.isQueueStation() ||
                this.isTrackerStation() ||
                this.isQueueExpo()) return order;

        //2.0.18
        // If it is expo queue, accept this order.
        //if it is prep queue, check check its prep items
        String queuePrepID = "";
        if (isQueueStation())
        {
            queuePrepID =this.getStationsConnections().getRelations().getQueueAttachedPrepStationID(getStationID());
            if (queuePrepID.isEmpty())
                return order;
        }
//        if (order.getTransType() == KDSDataOrder.TRANSTYPE_MODIFY ||
//                order.getTransType() == KDSDataOrder.TRANSTYPE_DELETE)
//            return order;

        int ncount = order.getItems().getCount();
        for (int i=ncount-1; i>=0; i--)
        {
            if (order.getItems().getItem(i).isExpitem())
            {
                if ( (!isExpeditorStation()) && (!isQueueStation()) &&(!isTrackerStation())&&(!isQueueExpo()) )
                    order.getItems().removeComponent(i);
                continue;

            }

            KDSToStations.PrimarySlaveStation tostation = order.getItems().getItem(i).getToStations().findStation( getStationID());
            //2.0.18
            if (tostation ==KDSToStations.PrimarySlaveStation.Unknown ) //don't find my ID
            {//check this queue station's prep station.
                if (isQueueStation() && (!queuePrepID.isEmpty()))
                    tostation =  order.getItems().getItem(i).getToStations().findStation( queuePrepID);
            }
            //
            if (tostation == KDSToStations.PrimarySlaveStation.Unknown) {
                //keep modify item. The expo station don't need these type items, as the stations will send items to it.
                if ( (!this.isQueueExpo()) && (!this.isExpeditorStation())) {
                    if (order.getItems().getItem(i).getTransType() == KDSDataOrder.TRANSTYPE_MODIFY ||
                            order.getItems().getItem(i).getTransType() == KDSDataOrder.TRANSTYPE_DELETE)
                        continue;
                }
                //remove this unassigned station
                order.getItems().removeComponent(i);
            }
            else
            { //send to this station, keep this item.
              continue;
            }
        }
        return order;
    }

    /**
     * in order to prevent the order hide, but screen is empty issue.
     * test the screen order showing.
     */
    public void setFocusAfterReceiveOrder()
    {

        setFocusAfterReceiveOrder(KDSUser.USER.USER_A);
        if (this.isMultpleUsersMode())
            setFocusAfterReceiveOrder(KDSUser.USER.USER_B);


    }

    public void setFocusAfterReceiveOrder(KDSUser.USER userID)
    {
        KDSUser user = this.getUsers().getUser(userID);
        if (user == null) return;
        if (user.getOrders().getCount()<=0) return;
        if (user.getOrders().getCount() == 1) {
            user.getOrders().get(0); //make it load data
            SettingsBase.StationFunc funcView = getSettings().getFuncView(); //current use what view to show orders.

            if (funcView != SettingsBase.StationFunc.Queue_Expo)//(! isQueueExpo())
                setFocusToOrder(user.getOrders().getFirstOrderGuid());
        }
        else
        {// > 1
            setFocusToOrder(KDSConst.RESET_ORDERS_LAYOUT);

        }

    }

    public void setFocusToDefault()
    {

        setFocusToOrder("");

    }

    private void resetOrdersForSaveMemoryAfterGetNewOrder( ArrayList<KDSDataOrder> orders)
    {
        for (int i=0;i< orders.size(); i++)
        {

            KDSDataOrdersDynamic.resetNewOrderItemsForSaveMemory( orders.get(i));
        }
    }

    /**
     *
     * @param order
     * @return
     *  items count
     */
    public int filterInNormalStation(KDSDataOrder order)
    {
        int nItemsCount = 0;
        if (order != null)
            nItemsCount  = order.getItems().getCount();//2.0.40

        int ntrans = order.getTransType();
        if (ntrans == KDSDataOrder.TRANSTYPE_MODIFY)
        {//if order don't existed, add it.
            if (m_users.getOrderByName(order.getOrderName()) == null) {
                if (!order.isAllItemsNotForNew())//if just single item withe "del/modify", it will cause add a new item ugly
                    ntrans = KDSDataOrder.TRANSTYPE_ADD;
            }
        }
        switch (ntrans)
        {
            case KDSDataOrder.TRANSTYPE_UNKNOWN: {
                break;
            }
            case KDSDataOrder. TRANSTYPE_ADD:{
                if (order.getItems().getCount()<=0) return nItemsCount;
               //KDSStationFunc.orderAdd(this, order, true, true);
                //TimeDog t = new TimeDog();
                ArrayList<KDSDataOrder> ordersAdded =  m_users.orderAdd(order, true);//////
                //set the preparation time mode sorts
                for (int i=0; i< ordersAdded.size(); i++)
                    ordersAdded.get(i).prep_set_sorts(order.prep_get_sorts());
                //t.debug_print_Duration("TRANSTYPE_ADD1");
                //beep
                if (this.getSettings().getBoolean(KDSSettings.ID.Beeper_Enabled)) {
                    KDSBeeper.BeeperType beeperType = KDSBeeper.BeeperType.values()[ getSettings().getInt(KDSSettings.ID.Beeper_Type)];
                    if (beeperType == KDSBeeper.BeeperType.Any)
                        KDSBeeper.beep();
                    else if (beeperType == KDSBeeper.BeeperType.Rush)
                    {
                        for (int i = 0; i < ordersAdded.size(); i++) {
                            if (ordersAdded.get(i).isRush())
                                KDSBeeper.beep();
                        }
                    }
                }
                if (getStationsConnections().isBackupOfOthers())
                {
                    if (m_kdsState.getPrimaryOfBackupLost())
                        getSoundManager().playSound(KDSSettings.ID.Sound_backup_station_orders_received);
                }
                else
                    getSoundManager().playSound(KDSSettings.ID.Sound_new_order);
                //t.debug_print_Duration("TRANSTYPE_ADD2");
                //print it
                if (getSettings().getBoolean(KDSSettings.ID.Printer_Enabled)) {
                    KDSPrinter.HowToPrintOrder howtoprint = KDSPrinter.HowToPrintOrder.values()[(getSettings().getInt(KDSSettings.ID.Printer_howtoprint))];
                    if (howtoprint == KDSPrinter.HowToPrintOrder.WhileReceive) {
                        for (int i = 0; i < ordersAdded.size(); i++) {
                            getPrinter().printOrder(ordersAdded.get(i));
                        }
                    }
                }
                //t.debug_print_Duration("TRANSTYPE_ADD3");
                this.getCurrentDB().clearExpiredBumpedOrders( getSettings().getBumpReservedCount());
//                if (ordersAdded.size() == 1)
//                { //focus first one

                schedule_process_update_to_be_prepare_qty();
                setFocusAfterReceiveOrder();

                resetOrdersForSaveMemoryAfterGetNewOrder(ordersAdded);


            }
            break;
            case KDSDataOrder. TRANSTYPE_DELETE:{
                //delete order by xml command
                m_users.orderCancel(order);
                this.getCurrentDB().clearExpiredBumpedOrders( getSettings().getBumpReservedCount());
                //KDSStationFunc.orderCancel(this, order);
                getSoundManager().playSound(KDSSettings.ID.Sound_bump_order);
                break;
            }
            case KDSDataOrder. TRANSTYPE_MODIFY:{
                //KDSStationFunc.orderInfoModify(this, order);
                m_users.orderInfoModify(order, false);
                schedule_process_update_to_be_prepare_qty();
                getSoundManager().playSound(KDSSettings.ID.Sound_modify_order);
                break;
            }
            case KDSDataOrder.TRANSTYPE_TRANSFER:{
                break;
            }
            case KDSDataOrder.TRANSTYPE_ASK_STATUS:{
                break;
            }
            case KDSDataOrder.TRANSTYPE_UPDATE_ORDER:
            {//use new data to update existed order,
                //This transtype is new in android.
                if (order.getItems().getCount()<=0) return nItemsCount;

                //TimeDog t = new TimeDog();
                ArrayList<KDSDataOrder> ordersAdded =  m_users.orderUpdate(order, true);//////

                //beep
                if (this.getSettings().getBoolean(KDSSettings.ID.Beeper_Enabled)) {
                    KDSBeeper.BeeperType beeperType = KDSBeeper.BeeperType.values()[ getSettings().getInt(KDSSettings.ID.Beeper_Type)];
                    if (beeperType == KDSBeeper.BeeperType.Any)
                        KDSBeeper.beep();
                    else if (beeperType == KDSBeeper.BeeperType.Rush)
                    {
                        for (int i = 0; i < ordersAdded.size(); i++) {
                            if (ordersAdded.get(i).isRush())
                                KDSBeeper.beep();
                        }
                    }
                }
                getSoundManager().playSound(KDSSettings.ID.Sound_modify_order);
                //print it
                if (getSettings().getBoolean(KDSSettings.ID.Printer_Enabled)) {
                    KDSPrinter.HowToPrintOrder howtoprint = KDSPrinter.HowToPrintOrder.values()[(getSettings().getInt(KDSSettings.ID.Printer_howtoprint))];
                    if (howtoprint == KDSPrinter.HowToPrintOrder.WhileReceive) {
                        for (int i = 0; i < ordersAdded.size(); i++) {
                            getPrinter().printOrder(ordersAdded.get(i));
                        }
                    }
                }

                this.getCurrentDB().clearExpiredBumpedOrders( getSettings().getBumpReservedCount());

                setFocusAfterReceiveOrder();

            }
            break;
            default:
                return nItemsCount;

        }
        return nItemsCount;

    }


    public void setFocusToOrder(String orderGuid)
    {
        for (int i=0; i< m_arKdsEventsReceiver.size(); i++) {
            m_arKdsEventsReceiver.get(i).onSetFocusToOrder(orderGuid);
        }
    }

    public boolean isDataLoaded()
    {
        if (m_users.getUserA() != null) {
            if (m_users.getUserA().getOrders().getCount()>0)
                return true;

            if (m_users.getUserA().getParkedOrders().getCount()>0)
                return true;

        }
        if (m_users.getUserB() != null) {
            if (m_users.getUserB().getOrders().getCount()>0)
                return true;
            if (m_users.getUserB().getParkedOrders().getCount() >0)
                return true;
        }
        return false;
    }

    public void loadAllActiveOrders()
    {

        try {
            loadAllActiveOrdersInfo();
        }
        catch (Exception e)
        {
            //KDSLog.e(TAG, e.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e );
        }

    }

    public void loadAllActiveOrdersInfo()
    {

        if (m_stationsConnection.isMirrorOfOthers()) //I am a mirror of other stations
        {
            ArrayList<String> arStationID = new ArrayList<>();
            arStationID.add(getStationID());
            ArrayList<KDSStationIP> primaryMirrors = m_stationsConnection.getRelations().getPrimaryStationsWhoUseMeAsMirror();
            for (int i=0; i< primaryMirrors.size(); i++)
                arStationID.add(primaryMirrors.get(i).getID());

            if (m_users.getUserA() != null) {
                m_users.getUserA().setOrders(m_dbCurrent.ordersLoadAllJustInfo(arStationID, KDSSettings.KDSScreen.Screen_A.ordinal(), false));
                m_users.getUserA().setParkedOrders(m_dbCurrent.ordersLoadAllJustInfo(arStationID, KDSSettings.KDSScreen.Screen_A.ordinal(), true));

            }
            if (m_users.getUserB() != null) {
                m_users.getUserB().setOrders(m_dbCurrent.ordersLoadAllJustInfo(arStationID, KDSSettings.KDSScreen.Screen_B.ordinal(), false));
                m_users.getUserB().setParkedOrders(m_dbCurrent.ordersLoadAllJustInfo(arStationID, KDSSettings.KDSScreen.Screen_B.ordinal(), true));

            }
        }
        else {

            if (m_users.getUserA() != null) {
                if (m_dbCurrent == null) return;
                m_users.getUserA().setOrders(m_dbCurrent.ordersLoadAllJustInfo(getStationID(), KDSSettings.KDSScreen.Screen_A.ordinal(), false));
                m_users.getUserA().setParkedOrders(m_dbCurrent.ordersLoadAllJustInfo(getStationID(), KDSSettings.KDSScreen.Screen_A.ordinal(), true));
            }
            if (m_users.getUserB() != null) {
                if (m_dbCurrent == null) return;
                m_users.getUserB().setOrders(m_dbCurrent.ordersLoadAllJustInfo(getStationID(), KDSSettings.KDSScreen.Screen_B.ordinal(), false));
                m_users.getUserB().setParkedOrders(m_dbCurrent.ordersLoadAllJustInfo(getStationID(), KDSSettings.KDSScreen.Screen_B.ordinal(), true));
            }


        }

    }

    public String getStationID()
    {
        return this.getSettings().getString(KDSSettings.ID.KDS_ID);
        //return m_strKDSStationID;
    }

    public int getScreen()
    {
        return  KDSSettings.KDSScreen.Screen_A.ordinal();
    }

    public void refreshView()
    {


        Message msg = new Message();
        msg.what = MESSAGE_TO_MAIN.REFRESH_ALL.ordinal();
        m_refreshHandler.sendMessage(msg);
    }

    public void doRefreshView()
    {
        KDSLog.d(TAG, KDSUtil.getCurrentTimeForLog()+ " refreshView");
        for (int i=0; i< m_arKdsEventsReceiver.size(); i++) {
            m_arKdsEventsReceiver.get(i).onRefreshView(KDSUser.USER.USER_A, m_users.getUserA().getOrders(), RefreshViewParam.None);
            m_arKdsEventsReceiver.get(i).onRefreshSummary(KDSUser.USER.USER_A);
            if (isValidUser(KDSUser.USER.USER_B)) {
                m_arKdsEventsReceiver.get(i).onRefreshView(KDSUser.USER.USER_B, m_users.getUserB().getOrders(), RefreshViewParam.None);
                m_arKdsEventsReceiver.get(i).onRefreshSummary(KDSUser.USER.USER_B);
            }
        }

    }

    public void refreshView(KDSUser.USER userID, RefreshViewParam nParam)
    {

        Message msg = new Message();
        switch (userID)
        {
            case USER_A:
                msg.what = MESSAGE_TO_MAIN.REFRESH_A.ordinal();
                break;
            case USER_B:
                msg.what = MESSAGE_TO_MAIN.REFRESH_B.ordinal();
                break;
        }
        //msg.what = userID.ordinal();
        msg.obj = nParam;
        m_refreshHandler.sendMessage(msg);

    }

    public void doRefreshView(KDSUser.USER userID, RefreshViewParam nParam)
    {
        KDSLog.d(TAG, KDSUtil.getCurrentTimeForLog()+ " refreshView");
        if (!isValidUser(userID)) return;
        KDSDataOrders orders = m_users.getUser(userID).getOrders();


        for (int i=0; i< m_arKdsEventsReceiver.size(); i++) {
            m_arKdsEventsReceiver.get(i).onRefreshView(userID, orders,nParam);// RefreshViewParam.None);
            m_arKdsEventsReceiver.get(i).onRefreshSummary(userID);
        }

    }


    /**
     *
     */
    public void checkPingThreadAfterResume()
    {
       if (m_threadPing == null ||
               (!m_threadPing.isAlive())) {
           stopPingThread();
           startPingThread();
       }
    }


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

        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Enter");
        if (m_threadPing != null) {
            m_bRunning = false;
            try {
                m_threadPing.join(1000);
            } catch (Exception e) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);
            }
        }
        m_threadPing = null;
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Exit");
        //Log.d(TAG, "stopPingThread exit");
        return true;
    }


    TimeDog m_dogAnnounce = new TimeDog();

    /**
     * PING thread
     */
    public void run()
    {
        KDSLog.i(TAG, KDSLog._FUNCLINE_()+"Enter");
        while (m_bRunning) {

            if (m_dogAnnounce.is_timeout(KDSConst.ACTIVE_PLUS_FREQUENCE))
            {
                m_dogAnnounce.reset();
                m_stationsConnection.checkAllNoResponseStations();
                //this.broadcastStationAnnounce();
                this.m_broadcaster.broadcastStationAnnounce();
                //this.broadcastStationAnnounceInThread();
                checkLostStationInThread();
                //this.broadcastRequireStationsUDP(); //get all station's ip address.
            }

            m_stationsConnection.connectAllStations();

            m_printer.onPing();

            try {
                Thread.sleep(KDSConst.PING_THREAD_SLEEP);
            }
            catch (Exception ex)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_(),ex);
            }

        }
        m_bRunning = false;
        KDSLog.i(TAG, KDSLog._FUNCLINE_()+"Exit");
        //Log.d(TAG, "ping thread exit");


    }
   // TimeDog dogTest = new TimeDog();
    private void checkLostStationInThread()
    {
        ArrayList<KDSStationIP> arStations = this.getStationsConnections().getRelations().getAllValidStations();
        for (int i=0; i< arStations.size(); i++)
        {
            KDSStationIP station = arStations.get(i);
            if (this.getStationsConnections().findActivedStationByID(station.getID()) == null)
                this.m_sockEventsMessageHandler.sendLostAnnouncePulseMessage(station.getID(), station.getIP());

        }

        try {
            if (this.getStationsConnections().getActiveStationsCount() == 0) {
                if (m_annoucerTimeDog.is_timeout(10000)) {//the udp maybe is dead, just reset it.
                    m_annoucerTimeDog.reset();
                    udpListenerReset();

                }
            }
        }
        catch (Exception err)
        {
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + err.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , err );
        }
    }


//    public void broadcastRequireStationsUDP()
//    {
//
//        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildRequireStationsCommand();
//        m_udpStationAnnouncer.broadcastData(buf);
//
//
//    }

//    public void broadcastRequireStationsUDPInThread()
//    {
//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] params) {
//                KDS.this.getBroadcaster().broadcastRequireStationsUDP();
//                return null;
//            }
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//    }

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
    public void doConfigRequireXmlCommand(KDSSocketInterface sock, KDSXMLParserCommand command,String xmlOriginalData)
    {
        String s = m_settings.outputXmlText(this.m_context);
        s = KDSXMLParserCommand.createBroadConfiguration(s);
        if (sock instanceof KDSSocketTCPSideBase)
        {
            KDSSocketTCPSideBase tcp = (KDSSocketTCPSideBase)sock;
            tcp.writeXmlTextCommand(s);
        }

    }


    public void doConfigReceivedXmlCommand( KDSXMLParserCommand command,String xmlOriginalData)
    {
        String strConfig = command.getParam(KDSConst.KDS_Str_Param, "");
        loadSettingsXml(strConfig);


    }

    public void loadSettingsXml(String strConfig)
    {
        if (strConfig.isEmpty())
            return;
        m_settings.parseXmlText(m_context, strConfig);
        m_settings.save(m_context);

        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
            m_arKdsEventsReceiver.get(i).onRetrieveNewConfigFromOtherStation();
        this.m_stationsConnection.refreshRelations(m_context, this.getStationID());
    }

    public void loadSettingsXmlAll(String strConfig)
    {
        if (strConfig.isEmpty())
            return;
        m_settings.parseXmlTextAll(m_context, strConfig);
        m_settings.save(m_context);

        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
            m_arKdsEventsReceiver.get(i).onRetrieveNewConfigFromOtherStation();
        this.m_stationsConnection.refreshRelations(m_context, this.getStationID());
    }
    /**
     * ask station configuration from given station.
     * @param stationID
     * return:
     *  0: Station is not actived
     */
    public int retrieveConfigFromStation(String stationID, TextView txtInfo)
    {
        this.m_stationsConnection.checkAllNoResponseStations();

        KDSStationConnection conn = this.m_stationsConnection.findConnectionClientSideByID(stationID);
        String s = KDSXMLParserCommand.createRequireConfiguration(""); //don't need the fromip
        if (conn != null) {
            if (conn.getSock().isConnected()) {
                conn.getSock().writeXmlTextCommand(s);
                if (txtInfo != null)
                    txtInfo.setText(txtInfo.getContext().getString(R.string.waiting_for_config_data));// "Waiting for config data...");
            }
            else {
                conn.addBufferedData(s);
                if (txtInfo != null)
                    txtInfo.setText(txtInfo.getContext().getString(R.string.waiting_for_new_connection));//"Waiting for new connection...");
            }
        }
        else
        {//connect it.

            KDSStationActived stationActive = this.m_stationsConnection.findActivedStationByID(stationID);
            if (stationActive == null) {
                txtInfo.setText(txtInfo.getContext().getString(R.string.station_is_not_active));//"Station is not active...");
                return 0;
            }
            KDSStationConnection willConn = this.m_stationsConnection.connectToStation(stationActive);
            if (willConn.getSock().isConnected()) {
                willConn.getSock().writeXmlTextCommand(s);
                if (txtInfo != null)
                    txtInfo.setText(txtInfo.getContext().getString(R.string.waiting_for_config_data));//"Waiting for config data...");
            }
            else {
                willConn.addBufferedData(s);
                if (txtInfo != null)
                    txtInfo.setText(txtInfo.getContext().getString(R.string.waiting_for_connecting));//"Waiting for connecting...");
            }


        }
        return 1;
    }

    public void  operationTransferItem(KDSUser.USER userID, String toStationID,int nScreen, KDSDataOrder orderWillTransfer)
    {
        KDSStationFunc.TransferingStatus result =  KDSStationFunc.itemTransfer(this.getUsers().getUser(userID), orderWillTransfer, toStationID, nScreen);
        switch (result)
        {
            case Success:
                //the message was shown in  KDSStationFunc.orderTransfer
                //showMessage("Transfer order done");
                break;
            case Connecting:
                showMessage("Connecting target station");
                break;
            case Connected_Sending:
                showMessage("Sending data");
                break;
            case Failed:
                showMessage("Transfer order failed");
                break;
            case Invalid_Order:
                showMessage("Invalid order");
                break;
            case Error_Station:
                showMessage("Target station error.Try again.");
                break;
        }
    }
    /**
     * transfer selected order to given station.
     * @param toStationID
     */
    public void operationTransferSelectedOrder(KDSUser.USER userID, String toStationID,int nScreen, String orderGuid)
    {
        KDSStationFunc.TransferingStatus result =  KDSStationFunc.orderTransfer(this.getUsers().getUser(userID), orderGuid, toStationID, nScreen);
        switch (result)
        {
            case Success:
                //the message was shown in  KDSStationFunc.orderTransfer
                //showMessage("Transfer order done");
                break;
            case Connecting:
                showMessage("Connecting target station");
                break;
            case Connected_Sending:
                showMessage("Sending data");
                break;
            case Failed:
                showMessage("Transfer order failed");
                break;
            case Invalid_Order:
                showMessage("Invalid order");
                break;
            case Error_Station:
                showMessage("Target station error.Try again.");
                break;
        }


    }

    public void showMessage(String msg)
    {
        int ncount = m_arKdsEventsReceiver.size();
        for (int i=0; i< ncount; i++)
        {
            m_arKdsEventsReceiver.get(i).onShowMessage(MessageType.Normal, msg);
        }
    }


    public ArrayList<KDSSummaryItem> summary(KDSUser.USER userID)
    {
        if (getSettings().getBoolean(KDSSettings.ID.AdvSum_enabled))
        {
            return summaryAdvanced(userID);
        }
        else
        {
            return summaryNormally(userID);
        }

    }

    public ArrayList<KDSSummaryItem> summaryAdvanced(KDSUser.USER userID)
    {
        if (!this.isValidUser(userID))
            return null;
        KDSDataOrders orders = m_users.getUser(userID).getOrders();// this.getOrders();
        int n = getSettings().getInt(KDSSettings.ID.Sum_Type);
        KDSSettings.SumType sumType = KDSSettings.SumType.values()[n];

        n = getSettings().getInt(KDSSettings.ID.Sum_order_by);
        KDSSettings.SumOrderBy sumOrderBy = KDSSettings.SumOrderBy.values()[n];

        String s = getSettings().getString(KDSSettings.ID.AdvSum_items);
        ArrayList<String> arFilter = PreferenceFragmentAdvSum.parseSumItems(s);

        boolean bSmartEnabled = getSettings().getBoolean(KDSSettings.ID.Smart_Order_Enabled);
        //boolean bSmartEnabled =  getSettings().getBoolean(KDSSettings.ID.Smart_Order_Enabled);// (KDSSettings.SmartMode.values()[ getSettings().getInt(KDSSettings.ID.Smart_mode)] == KDSSettings.SmartMode.Normal );
        ArrayList<KDSSummaryItem> ar = this.getCurrentDB().summaryItemsAdvanced(this.getStationID(),userID.ordinal(),orders.getAllOrderGUID(), false, (sumOrderBy == KDSSettings.SumOrderBy.Ascend), arFilter,bSmartEnabled);//  KDSConst.Screen.SCREEN_A.ordinal(),orders.getAllOrderGUID(), false );
        //check filter again, add default description.
        //make the advanced summary items always showing.
        ArrayList<KDSSummaryItem> arReturn = new ArrayList<>();
        for (int i=0; i< arFilter.size(); i++)
        {
            ArrayList<KDSSummaryItem> advSumItems = findAdvSumItem(ar, arFilter.get(i).replace(KDSSummaryItem.CONDIMENT_TAG, ""));

            if (advSumItems.size()>0)//the filtered item is existed in summary
            {
                arReturn.addAll(advSumItems);//.add(sumItem);
                ar.removeAll(advSumItems);
            }
            else //no filtered item, add a zero item
            {

                KDSSummaryItem sumItem = new KDSSummaryItem();
                sumItem.setDescription(arFilter.get(i));
                sumItem.setQty(0);
                arReturn.add(sumItem);

            }
        }
        return arReturn;

    }

    private ArrayList<KDSSummaryItem> findAdvSumItem(ArrayList<KDSSummaryItem> ar, String name)
    {
        ArrayList<KDSSummaryItem> arReturn = new ArrayList<>();

        for (int i=0; i< ar.size(); i++)
        {
            //if (ar.get(i).getDescription().equals(name))
            if (ar.get(i).isShowingItem(name))
                arReturn.add(ar.get(i));
        }
        return arReturn;
    }

    public ArrayList<KDSSummaryItem> summaryNormally(KDSUser.USER userID)
    {
        if (!this.isValidUser(userID))
            return null;
        KDSDataOrders orders = m_users.getUser(userID).getOrders();// this.getOrders();
        int n = getSettings().getInt(KDSSettings.ID.Sum_Type);
        KDSSettings.SumType sumType = KDSSettings.SumType.values()[n];

        n = getSettings().getInt(KDSSettings.ID.Sum_order_by);
        KDSSettings.SumOrderBy sumOrderBy = KDSSettings.SumOrderBy.values()[n];

        if (sumType == KDSSettings.SumType.ItemWithoutCondiments)
            return this.getCurrentDB().summaryItems(this.getStationID(),userID.ordinal(),null, false, (sumOrderBy == KDSSettings.SumOrderBy.Ascend));//  KDSConst.Screen.SCREEN_A.ordinal(),orders.getAllOrderGUID(), false );
            //return this.getCurrentDB().summaryItems(this.getStationID(),userID.ordinal(),orders.getAllOrderGUID(), false);//  KDSConst.Screen.SCREEN_A.ordinal(),orders.getAllOrderGUID(), false );
        else
            return this.getCurrentDB().summaryItems(this.getStationID(),userID.ordinal(),orders.getAllOrderGUID(), true, (sumOrderBy == KDSSettings.SumOrderBy.Ascend));//  KDSConst.Screen.SCREEN_A.ordinal(),orders.getAllOrderGUID(), false );

    }


    public void clearAll()
    {

        //this.getOrders().clear();
        m_users.ordersClear();
        this.getCurrentDB().clear();
        this.getSupportDB().clear();
        //this.getStatisticDB().clear();
        this.refreshView();
    }

    public KDSSettings getSettings()
    {
        return m_settings; //this settings is saved in layout
    }

    public KDSSettings.ID checkKDSKbdEvent(KeyEvent ev, KDSKbdRecorder kbd)
    {
        return m_bumpbarFunctions.getKDSKeyboardEvent(ev, kbd);
    }

    public KDSSettings.ID checkQExpoKbdEvent(KeyEvent ev, KDSKbdRecorder kbd)
    {
        return m_bumpbarFunctions.getQexpoKeyboardEvent(ev, kbd);
    }

    public KDSSettings.ID checkKDSDlgKbdEvent(KeyEvent ev, KDSKbdRecorder kbd)
    {
        return m_bumpbarFunctions.getKDSDlgEvent(ev, kbd);
    }

    public KDSBumpBarFunctions getBumpbarKeysFunc()
    {
        return m_bumpbarFunctions;
    }

    public boolean isValidUser(KDSUser.USER userID)
    {
        if (userID == KDSUser.USER.USER_B)
        {
            return (m_users.getUserB() != null);
        }
        return true;
    }

    public boolean isSingleUserMode()
    {
        return (m_users.getUsersCount() ==1);
    }

    public boolean isMultpleUsersMode()
    {
        return (m_users.getUsersCount() ==2);
    }



    public boolean isEnabled()
    {
        KDSStationsRelation r = m_stationsConnection.getRelations().getMyRelations();
        if (r == null) return true;
        return (r.getStatus() == KDSSettings.StationStatus.Enabled);


    }

    public boolean writeXmlToPOSOrderInfo(Object objPOS, String strXml,String toFileName)
    {
        return writeXmlToPOS(objPOS, strXml, KDSConst.SMB_FOLDER_ORDER_INFO, toFileName);



    }

    public boolean writeXmlToPOSOrderAcknowledgement(Object objPOS, String strXml,String toFileName)
    {
        return writeXmlToPOSData( strXml, KDSConst.SMB_FOLDER_ACKNOWLEDGEMENT, toFileName);

    }

//    public boolean writeXmlToPOSNotification(Object objPOS, String strXml,String toFileName)
//    {
//        return writeXmlToPOS(objPOS, strXml, KDSConst.SMB_FOLDER_NOTIFICATION, toFileName);
//
//    }
    public boolean writeXmlToPOSNotification( String strXml,String toFileName)
    {
        return writeXmlToPOSData(strXml, KDSConst.SMB_FOLDER_NOTIFICATION, toFileName );

//        try {
//            KDSSettings.KDSDataSource source = KDSSettings.KDSDataSource.values()[getSettings().getInt(KDSSettings.ID.KDS_Data_Source)];
//            if (source == KDSSettings.KDSDataSource.Folder) {
//                writeXmlToPOS(m_smbDataSource, strXml, KDSConst.SMB_FOLDER_NOTIFICATION, toFileName);
//            } else {
//                int ncount = m_stationsConnection.getPosStations().size();
//                for (int i = 0; i < ncount; i++) {
//                    writeXmlToPOS(m_stationsConnection.getPosStations().get(i), strXml, KDSConst.SMB_FOLDER_NOTIFICATION, toFileName);
//                }
//            }
//
//            return true;
//        }
//        catch (Exception err)
//        {
//            //KDSLog.e(TAG, KDSLog._FUNCLINE_()+err.toString());
//            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,err );
//        }
//        return false;

    }

    public boolean writeXmlToPOSData( String strXml,String folderName, String toFileName)
    {
        try {
            KDSSettings.KDSDataSource source = KDSSettings.KDSDataSource.values()[getSettings().getInt(KDSSettings.ID.KDS_Data_Source)];
            if (source == KDSSettings.KDSDataSource.Folder) {
                writeXmlToPOS(m_smbDataSource, strXml,folderName, toFileName);
            } else {
                int ncount = m_stationsConnection.getPosStations().size();
                for (int i = 0; i < ncount; i++) {
                    writeXmlToPOS(m_stationsConnection.getPosStations().get(i), strXml, folderName, toFileName);
                }
            }

            return true;
        }
        catch (Exception err)
        {
            //KDSLog.e(TAG, KDSLog._FUNCLINE_()+err.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,err );
        }
        return false;

    }


    public boolean writeXmlToPOS(Object objPOS, String strXml,String toFolderForSMB, String fileNameForSMB)
    {
        Object objSrc =objPOS;
        if (objSrc != null) {
            if (objSrc instanceof KDSSocketInterface) {
                if (objSrc instanceof KDSSocketTCPSideClient)
                    ((KDSSocketTCPSideClient) objSrc).writeXmlTextCommand(strXml);


                if (objSrc instanceof KDSSocketTCPSideServer)
                    ((KDSSocketTCPSideServer) objSrc).writeXmlTextCommand(strXml);

            } else if (objSrc instanceof KDSSMBDataSource) {
                String fileName = fileNameForSMB;
                ((KDSSMBDataSource) objSrc).uploadSmbFile(toFolderForSMB, fileName, strXml);
            }
            else if (objSrc instanceof KDSStationConnection)
            {
                ((KDSStationConnection)objSrc).getSock().writeXmlTextCommand(strXml);
            }
            return true;
        }
        return false;
    }

    public boolean firePOSNotification(KDSDataOrder order, KDSDataItem item, KDSPosNotificationFactory.BumpUnbumpType notificationType)
    {
        if (order == null) return false;
        boolean bNotification = this.getSettings().getBoolean(KDSSettings.ID.POS_notification_enabled);

        if (!bNotification) return true;

        return sendNotificationToPos(order, item, notificationType);


//        String fileName = "";
//        if (item == null)
//            fileName = KDSPosNotificationFactory.createNewBumpNotifyFileName( getLocalIpAddress(), order.getOrderName(), "", notificationType );
//        else
//            fileName = KDSPosNotificationFactory.createNewBumpNotifyFileName( getLocalIpAddress(), order.getOrderName(), item.getItemName(), notificationType );
//
//        String strXml = KDSPosNotificationFactory.createBumpNotification(getLocalIpAddress(), order, item, notificationType, fileName);
//
//
//        return writeXmlToPOSNotification(strXml, fileName);

    }

    public boolean firePOSNotification_OrderItem(KDSDataOrder order, KDSDataItem item, KDSPosNotificationFactory.BumpUnbumpType notificationType)
    {
        if (notificationType == KDSPosNotificationFactory.BumpUnbumpType.Order_status_changed)
        {
            boolean bNotification = this.getSettings().getBoolean(KDSSettings.ID.Notification_order_status);
            if (!bNotification)
                return true;
        }
        else if (notificationType == KDSPosNotificationFactory.BumpUnbumpType.Item_qty_changed)
        {
            boolean bNotification = this.getSettings().getBoolean(KDSSettings.ID.Notification_item_qty);
            if (!bNotification)
                return true;
        }
        else
            return false;


        return sendNotificationToPos(order, item, notificationType);


    }

    public boolean sendNotificationToPos(KDSDataOrder order, KDSDataItem item, KDSPosNotificationFactory.BumpUnbumpType notificationType)
    {
        String fileName = "";
        if (item == null)
            fileName = KDSPosNotificationFactory.createNewBumpNotifyFileName( getLocalIpAddress(), order.getOrderName(), "", notificationType );
        else
            fileName = KDSPosNotificationFactory.createNewBumpNotifyFileName( getLocalIpAddress(), order.getOrderName(), item.getItemName(), notificationType );

        String strXml = KDSPosNotificationFactory.createBumpNotification(getLocalIpAddress(), order, item, notificationType, fileName);


        return writeXmlToPOSNotification(strXml, fileName);

    }

    public void schedule_process_update_after_receive_new_order()
    {
        schedule_process_update_after_receive_new_order(KDSUser.USER.USER_A);
        if (this.isMultpleUsersMode())
            schedule_process_update_after_receive_new_order(KDSUser.USER.USER_B);
    }
    /**
     * call it after a new order come in.
     * This will update "item" ready status.
     * @param userID
     */
    public void schedule_process_update_after_receive_new_order(KDSUser.USER userID)
    {
        if (this.getUsers().getUser(userID) == null) return;
        KDSDataOrdersDynamic orders =  this.getUsers().getUser(userID).getOrders();
        if (orders == null) return;
        for (int i=0; i< orders.getCount(); i++)
        {
            if (orders.getOrderByIndexWithoutLoadData(i) instanceof ScheduleProcessOrder)
            {
                schedule_process_ready_qty_changed(userID, (ScheduleProcessOrder)orders.get(i));
            }
        }

    }

    public void schedule_process_ready_qty_changed(KDSUser.USER userID, ScheduleProcessOrder pScheduleOrder)
    {
        KDSDataItem pItem = ScheduleProcessOrder.get_prepare_item(pScheduleOrder);
        String strCategory = pItem.getCategory();
        String strItemDescription = pItem.getDescription();
        KDSDataOrders porders = getUsers().getUser(userID).getOrders();


        int ncount = porders.getCount();
        ArrayList<String> arItemGUID = new ArrayList<>();

        //CStringArray arItemSerialID;

        int nready = pScheduleOrder.get_ready_qty();
        boolean bchanged = false;
        for (int i=0; i<ncount; i++)
        {
            nready = pScheduleOrder.get_ready_qty();
            KDSDataOrder pNormalOrder = porders.get(i);

            if (pNormalOrder.is_schedule_process_order()) continue;

            arItemGUID.clear();
            //arItemSerialID.RemoveAll();
            int nqty = pNormalOrder.find_no_condiments_not_ready_item(strCategory, strItemDescription, arItemGUID);//, &arItemSerialID);
            if (nqty <=0) continue;
            String orderGUID = pNormalOrder.getGUID();//->GetOrderID();
            //need to check if the same item go to multiple stations.
//            for (int k=0; k<arItemGUID.size(); k++)
//            {
//                String itemGUID = arItemGUID.get(k);
//                KDSDataItem pNormalItem = pNormalOrder.getItems().getItemByGUID(itemGUID);
//                if (pNormalItem == null) continue;
//                int nNormalQty =(int) pNormalItem.getQty();
//                bool bMultipleReady = pdb->schedule_item_is_ready_in_multiple_station(g_arExpeditorStations,orderID, itemID, nNormalQty );
//
//                if (bMultipleReady)
//                {//it has been cooked(ready) in others station. Just mark it, don't change ctargetorder qty.
//                    //AfxMessageBox(_T("multiple ready"));
//                    pNormalItem->set_item_schedule_ready();
//                    //change db data
//                    pdb->item_schedule_set_ready(nStation, get_user_id(),orderID, itemID, true );
//                    nqty -= nNormalQty; //decrease the "other station cooked" qty.
//                    bchanged = true;
//                }
//            }

            //mark items which qty< readyqty
            if (nqty > nready) continue;

            ////////////////////////////////////////////

            //change ctargetorder qty
            int nnewready = pScheduleOrder.get_ready_qty() - nqty;
            pScheduleOrder.set_ready_qty(nnewready);
            //this.getCurrentDB().schedule_process_set_item_ready_qty(pScheduleOrder);//->m_nInStation,pOrder->m_nUser, orderID, pOrder->get_prepare_item()->GetID(), nnewready );
//            if (pOrder->m_nInStation != nStation) //prevent refresh twice
//                ((CLS6KDS*)(((CLS6000*)pLs6kParent))->GetLs6KDSPointer()) ->refresh_station_window(pOrder->m_nInStation);


            ///change normal item "cooked(ready)" status
            for (int j=0; j<arItemGUID.size(); j++)
            {
                KDSDataItem pitem = pNormalOrder.getItems().getItemByGUID(arItemGUID.get(j));

                if (pitem.getLocalBumped()) continue; //2012-12-12 recover it, old is commented

                pitem.setLocalBumped(true);//->set_item_schedule_ready();
                //change db data
                this.getCurrentDB().itemSetLocalBumped(pitem);
                //pdb->item_schedule_set_ready(nStation, get_user_id(),orderID, arItemID.GetAt(j), true );
                //sync expediter station
                KDSStationFunc.sync_with_stations(this, KDSXMLParserCommand.KDSCommand.Station_Bump_Item, pNormalOrder, pitem);

                //sync_order_operation_inform_expeditor(pLs6kParent, nStation, pNormalOrder, pitem, Station_Inform_Exp_Schedule_Item_Ready);
                //sync_order_operation_inform_parent(pLs6kParent, get_user_id() , ORDER_OPERATION_ITEM_SET_READY,pNormalOrder->GetOrderID(),
                 //       pitem->GetID(), pNormalOrder, pitem);
                bchanged = true;
            }

        }
        this.getCurrentDB().schedule_process_set_item_ready_qty(pScheduleOrder);//->m_nInStation,pOrder->m_nUser, orderID, pOrder->get_prepare_item()->GetID(), nnewready );
        schedule_process_update_to_be_prepare_qty();
      //  if (bchanged) refreshView();
    }

    /**
     * check all schedule order, and update its to_be_prepared qty
     */
    public void schedule_process_update_to_be_prepare_qty()
    {
        schedule_process_update_to_be_prepare_qty(KDSUser.USER.USER_A);
        if (isMultpleUsersMode())
            schedule_process_update_to_be_prepare_qty(KDSUser.USER.USER_B);
    }

    /**
     * Here: bug
     *      It load all items to buffer, this will slow down the showing speed
     * @param userID
     */
    public void schedule_process_update_to_be_prepare_qty(KDSUser.USER userID)
    {
        if (getUsers() == null) return;
        if (getUsers().getUser(userID) == null) return;
        KDSDataOrdersDynamic orders = getUsers().getUser(userID).getOrders();
      //  boolean bchanged = false;
        for (int i=0; i< orders.getCount(); i++)
        {
           // if (!orders.get(i).is_schedule_process_order()) continue;
            KDSDataOrder order = orders.getOrderByIndexWithoutLoadData(i);
            if (order == null) return;
            if (!orders.getOrderByIndexWithoutLoadData(i).is_schedule_process_order()) continue;

            if (getCurrentDB().schedule_order_update_not_ready_qty((ScheduleProcessOrder) orders.get(i)))
            {//changed,
                KDSStationFunc.sync_with_stations(this, KDSXMLParserCommand.KDSCommand.Schedule_Item_Ready_Qty_Changed,  orders.get(i), orders.get(i).getItems().getItem(0));
                //bchanged = true;
            }

        }
  //      if (bchanged)
            refreshView();
    }

    public SoundManager getSoundManager()
    {
        return m_soundManager;
    }

    public  void onSyncStationQueueExpoBumpSettingChanged(boolean bEnabled)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        getSettings().setPrefValue(pref, "bool_bump_double_queue", bEnabled);

        getSettings().loadSettings(KDSApplication.getContext());
    }
    public  void onSyncStationSmartModeEnabledSettingChanged(int nMode)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        getSettings().setPrefValue(pref, "bool_smartorder_enabled",(nMode==1));

        getSettings().loadSettings(KDSApplication.getContext());
    }

    public boolean isDbEmpty()
    {
        if (!m_dbCurrent.isEmpty())
            return false;
        if (!m_dbStatistic.isEmpty())
            return false;
        if (!m_dbSupport.isEmpty())
            return false;
        return true;
    }

    /**
     * 2.0.10
     * @param sock
     * @param command
     */
    public void doSosReport(KDSSocketInterface sock, KDSXMLParserCommand command)
    {
        StatisticThread t = new StatisticThread();
        t.m_sock = sock;
        t.m_command = command;
        t.m_kds = this;
        Thread c = new Thread(t);
        c.start();
    }

    public void doStatisticReport(KDSSocketInterface sock, KDSXMLParserCommand command)
    {

        StatisticThread t = new StatisticThread();
        t.m_sock = sock;
        t.m_command = command;
        t.m_kds = this;
        Thread c = new Thread(t);
        c.start();


    }

    public void remove_statistic_old_data()
    {
        int nDays = getSettings().getInt(KDSSettings.ID.Statistic_keep_days);
        if (nDays <0)
            nDays = 0;
        KDSDBStatistic db =  this.getStatisticDB();
        if (db == null) return;
        db.removeData(nDays);
    }

    /**
     *
     * @param command
     *  format: orderName, itemName, itemName, ....
     */
    public void onPreparationTimeModeItemBumpUnbumped(String command, boolean bBumped)
    {
        ArrayList<String> ar = KDSUtil.spliteString(command, ",");
        if (ar.size()<2)
            return;
        String orderName = ar.get(0);
        for (int i=1; i< ar.size(); i++)
        {
            String itemName = ar.get(i);
            if (bBumped)
                prep_other_station_item_bumped(orderName, itemName);
            else
                prep_other_station_item_unbumped(orderName, itemName);
        }
    }

    public void prep_other_station_item_bumped(String orderName,String itemName)
    {
        getUsers().getUser(KDSUser.USER.USER_A).prep_other_station_item_bumped(orderName, itemName);
        if (isMultpleUsersMode())
            getUsers().getUser(KDSUser.USER.USER_B).prep_other_station_item_bumped(orderName, itemName);
    }

    public void prep_other_station_item_unbumped(String orderName,String itemName)
    {
        getUsers().getUser(KDSUser.USER.USER_A).prep_other_station_item_unbumped(orderName, itemName);
        if (isMultpleUsersMode())
            getUsers().getUser(KDSUser.USER.USER_B).prep_other_station_item_unbumped(orderName, itemName);
    }

    public int getOpenedOrderSourceIpPort()
    {
        return m_nPOSPort;
    }

    public KDSSocketUDP getUDP()
    {
        return m_udpStationAnnouncer;
    }
    public int getOpenedStationsCommunicatingPort()
    {
        return m_nStationsPort;
    }

    public Broadcaster getBroadcaster()
    {
        return m_broadcaster;
    }

    /**
     * 2.0.14
     */
    public void sortOrderForMoveFinishedToFront()
    {

        if (!getSettings().getBoolean(KDSSettings.ID.Orders_sort_finished_front))
            return;

        getUsers().getUser(KDSUser.USER.USER_A).getOrders().sortOrders();
        if (isMultpleUsersMode())
            getUsers().getUser(KDSUser.USER.USER_B).getOrders().sortOrders();

        setFocusToOrder(KDSConst.RESET_ORDERS_LAYOUT);

        refreshView();

    }

    private void doStationAnnounceInThread(String strInfo)
    {
        StationAnnounceRunnable r = new StationAnnounceRunnable(strInfo);
        Thread t = new Thread(r);
        t.start();

    }

    private void doStationAnnounce(String strInfo)
    {
        m_annoucerTimeDog.reset();
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
        String itemsCount ="";
        if (ar.size() >=5) //add orders count
            itemsCount = ar.get(4);

        int nUserMode = 0;

        if (ar.size() >=6)
        {
            int n = KDSUtil.convertStringToInt( ar.get(5),0 );
            if (n <0 || n>1)
                n = 0;
            nUserMode = n;


        }

        KDSStationActived station =m_stationsConnection.findActivedStationByMac(mac);//id);
        if (station == null) {
            station = new KDSStationActived();
            //m_arActivedStations.add(station);
            m_stationsConnection.addActiveStation(station);
            bNewStation = true;
        }
        station.setID(id);
        station.setIP(ip);
        station.setPort(port);
        station.setMac(mac);
        station.setStationContainItemsCount(itemsCount);
        station.setUserMode(nUserMode);
        station.updatePulseTime();//record last received time

        //some connection don't have the station ID in it. use this function to update them.
        //comment it for debuging the connect with data function.
        m_stationsConnection.refreshAllExistedConnectionInfo();


        //if (m_stationAnnounceEvents != null)
        //    m_stationAnnounceEvents.onReceivedStationAnnounce(station);//id, ip, port, mac);
        Message msg = new Message();
        msg.what = ANNOUNCE_MSG_SEND_EVENT;
        msg.obj = station;
        m_announceHander.sendMessage(msg);

        if (bNewStation) {
            //announce_restore_pulse(id, ip);
            msg = new Message();
            msg.what = ANNOUNCE_MSG_STATION_RESTORE;
            msg.obj = station;
            m_announceHander.sendMessage(msg);
        }
    }
    final int ANNOUNCE_MSG_SEND_EVENT = 0;
    final int ANNOUNCE_MSG_STATION_RESTORE = 1;
    Handler m_announceHander = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            KDSStationActived station = (KDSStationActived)msg.obj;
            if (msg.what == ANNOUNCE_MSG_SEND_EVENT) {
                if (m_stationAnnounceEvents != null)
                    m_stationAnnounceEvents.onReceivedStationAnnounce(station);//id, ip, port, mac);
            }
            else if (msg.what == ANNOUNCE_MSG_STATION_RESTORE)
            {
                announce_restore_pulse(station.getID(),station.getIP());
            }
            return false;
        }
    });


    class StationAnnounceRunnable implements Runnable
    {
        String m_strStationAnnounce = "";

        public StationAnnounceRunnable(String strAnnounce)
        {
            setAnnounce(strAnnounce);
        }
        public void setAnnounce(String strAnnounce)
        {
            m_strStationAnnounce = strAnnounce;
        }


        public void run()
        {
            doStationAnnounce(m_strStationAnnounce);
        }
    }


}
