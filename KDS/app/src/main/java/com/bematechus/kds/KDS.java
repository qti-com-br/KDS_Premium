package com.bematechus.kds;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.bematechus.kdslib.AckDataStation;
import com.bematechus.kdslib.Activation;
import com.bematechus.kdslib.BuildVer;
import com.bematechus.kdslib.ActivationRequest;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSBase;
import com.bematechus.kdslib.KDSCallback;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDBBase;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataItems;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSLogOrderFile;
import com.bematechus.kdslib.KDSPOSMessage;
import com.bematechus.kdslib.KDSPOSMessages;
import com.bematechus.kdslib.KDSPosNotificationFactory;
import com.bematechus.kdslib.KDSSMBDataSource;
import com.bematechus.kdslib.KDSSmbFile;
import com.bematechus.kdslib.KDSSmbFile2;
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
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXML;
import com.bematechus.kdslib.KDSXMLParserCommand;
import com.bematechus.kdslib.KDSXMLParserOrder;
import com.bematechus.kdslib.KDSXMLParserPOSMessage;
import com.bematechus.kdslib.NoConnectionDataBuffers;
import com.bematechus.kdslib.PrepSorts;
import com.bematechus.kdslib.ScheduleProcessOrder;
import com.bematechus.kdslib.SettingsBase;
import com.bematechus.kdslib.StationAnnounceEvents;
import com.bematechus.kdslib.TimeDog;

import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * The KDS app main interface.
 *  It charge the network, printer ...
 */
public class KDS extends KDSBase implements KDSSocketEventReceiver,
        Runnable,
        KDSCallback
        {

    final static String TAG = "KDSMain";
//    public enum RefreshViewParam
//    {
//        None,
//        Focus_First,
//    }
//    public enum MessageType
//    {
//        Normal,
//        Toast,
//    }


//    public interface KDSEvents
//    {
//        void onStationConnected(String ip, KDSStationConnection conn);
//        void onStationDisconnected(String ip);
//        void onAcceptIP(String ip);
//        void onRefreshView(KDSUser.USER userID, KDSDataOrders orders, RefreshViewParam nParam); //nParam: 1: move focus to first order.
//        void onRetrieveNewConfigFromOtherStation();
//        void onShowMessage(MessageType msgType, String message);
//        void onRefreshSummary(KDSUser.USER userID);
//        void onAskOrderState(Object objSource, String orderName);
//        void onSetFocusToOrder(String orderGuid); //set focus to this order
//        void onXmlCommandBumpOrder(String orderGuid);
//        void onTTBumpOrder(String orderName);
//        void onReceiveNewRelations();
//        void onReceiveRelationsDifferent();
////        void onItemQtyChanged(KDSDataOrder order, KDSDataItem item);
////        void onOrderStatusChanged(KDSDataOrder order, int nOldStatus);
//        //void onShowToastMessage(String message);
//    }

    final int DEFAULT_STATION_DATASOURCE_IP_PORT = KDSApplication.getContext().getResources().getInteger(R.integer.default_stations_datasource_tcpip_port);// 3000;
    final int DEFAULT_STATION_INTERNAL_IP_PORT =KDSApplication.getContext().getResources().getInteger(R.integer.default_stations_internal_tcpip_port); //3001;
            //don't support statistic anymore
    //final int DEFAULT_STATISTIC_TCP_PORT = KDSApplication.getContext().getResources().getInteger(R.integer.default_statistic_connect_stations_tcpip_port); //6001; //unused!

//
//    public interface StationAnnounceEvents
//    {
//        void onReceivedStationAnnounce(KDSStationIP stationReceived);//String stationID, String ip, String port, String mac);
//    }



    String m_strKDSStationID = "";
    int m_nDataSourceIpPort = DEFAULT_STATION_DATASOURCE_IP_PORT;
    int m_nStationsInternalIpPort = DEFAULT_STATION_INTERNAL_IP_PORT;
    //int m_nStatisticPort = DEFAULT_STATISTIC_TCP_PORT;//don't support statistic anymore


    KDSDBCurrent m_dbCurrent = null;
    KDSDBStatistic m_dbStatistic = null;
    KDSDBSupport m_dbSupport = null;
    // broadcast udp message
    KDSSocketUDP m_udpStationAnnouncer = new KDSSocketUDP();

    //all socket checking
    KDSSocketManager m_socksManager = new KDSSocketManager();
    //listen which pos want to connect to me
    KDSSocketTCPListen m_listenPOS;
    //KDSSocketTCPListen m_listenStatistic; //don't support statistic anymore
    //list which normal kds station connect to me.
    KDSSocketTCPListen m_listenStations;
    //use it receive event, and
    KDSSocketMessageHandler m_sockEventsMessageHandler  = new KDSSocketMessageHandler(this);; //socket events

    Context m_context = null;

    Object m_locker = new Object();
    //Object m_activePulseLocker = new Object(); //for lock the active announce array

    ArrayList<KDSEvents> m_arKdsEventsReceiver = new ArrayList<KDSEvents>();//null; //KDS events

//    StationAnnounceEvents m_stationAnnounceEvents = null;

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

    Activation m_activationHTTP = null; //sms feature. 2.1.10

            //kp-102
    KDSPOSMessages mPOSMessages = new KDSPOSMessages();
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * for sms feature.
     * send sms through activation class
     * @param a
     */
    public void setSMSActivation(Activation a)
    {
        m_activationHTTP = a;
    }

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

        //don't support statistic anymore
//        m_listenStatistic = new KDSSocketTCPListen();
//        m_listenStatistic.setEventHandler(m_sockEventsMessageHandler);

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
            m_users.setSingleUserMode(true);
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
        KDSLogOrderFile.setEnabled(m_settings.getBoolean(KDSSettings.ID.Log_orders));
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
        if (nPort != m_nDataSourceIpPort)
            bPosPortChanged = true;
        m_nDataSourceIpPort = nPort;
        nPort = settings.getInt(KDSSettings.ID.KDS_Station_Port);
        boolean bStationPortChanged = false;
        if (nPort != m_nStationsInternalIpPort)
            bStationPortChanged = true;
        m_nStationsInternalIpPort = nPort;

        updateStationFunction();

        if (bPosPortChanged)
        {
            if (m_listenPOS.isListening())
            {
                m_listenPOS.stop();

                m_stationsConnection.disconnectPOSConnections();
                // kpp1-312, show error message.
                String error = m_listenPOS.startServer(m_nDataSourceIpPort, m_socksManager, m_sockEventsMessageHandler );
                fireTcpListenServerErrorEvent(m_nDataSourceIpPort, error);
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
                String error = m_listenStations.startServer(m_nStationsInternalIpPort, m_socksManager, m_sockEventsMessageHandler );
                fireTcpListenServerErrorEvent(m_nStationsInternalIpPort, error);

            }
        }
        startRemoteFolderDataSource(settings);

        updateUsers(settings);

        m_bumpbarFunctions.updateSettings(this.getSettings());
        //use new dialog class
        KDSUIDialogBase.init_kbd_keys(m_bumpbarFunctions.getKbdType(),
                                        m_bumpbarFunctions.getKeySettings(KDSSettings.ID.Bumpbar_OK),
                                        m_bumpbarFunctions.getKeySettings(KDSSettings.ID.Bumpbar_Cancel));
        KDSUIDialogBase.init_navigation_bar_settings(settings.getBoolean(KDSSettings.ID.Hide_navigation_bar));

        m_printer.updateSettings(settings);

        //kp1-25
        PrepSorts.m_bSmartCategoryEnabled = false;
        if (isRunnerStation())
            PrepSorts.m_bSmartCategoryEnabled = true;
        else
        {
            if (getStationsConnections().getRelations().isRunnerAsMyExpo(getStationID()))
            {
                PrepSorts.m_bSmartCategoryEnabled = true;
            }
        }
        //kp-121, manually start cooking.
        PrepSorts.m_bStartItemManually =  settings.getBoolean(KDSSettings.ID.Runner_start_item_manually);

        ScreenLogoDraw.m_logoFilesManager.updateSettings(settings);
    }
    public void updateStationFunction()
    {
        //KDSSettings.StationFunc func =m_stationsConnection.getRelations().getStationFunction(getStationID(), "");
        KDSSettings.StationFunc func =m_stationsConnection.getRelations().getStationFunctionForBackoffice(getStationID());

        SettingsBase.StationFunc old = m_settings.getStationFunc();
        KDSSettings.StationFunc funcRealWorkFor =m_stationsConnection.getRelations().getStationFunction(getStationID(), "");
        m_settings.setStationFunc(funcRealWorkFor);
        if (old != func)
        { //update the activation backoffice
            if (m_activationHTTP != null) {
                if (!getStationID().isEmpty())//kpp1-309 Expeditor and Queue deleted at logout on premium
                {
                    String name = m_stationsConnection.getRelations().getStationFunctionNameForBackoffice(getStationID());
                    //m_activationHTTP.postNewStationInfo2Web(getStationID(), func.toString());
                    m_activationHTTP.postNewStationInfo2Web(getStationID(), name);
                }
            }
        }

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
        if (receiver instanceof MainActivity)
        { //I see two MainActivity in it once.
            for (int i= 0; i< ncount; i++) {
                if (m_arKdsEventsReceiver.get(i) instanceof MainActivity) {
                    m_arKdsEventsReceiver.remove(i);
                    break;
                }
            }
        }
        ncount = m_arKdsEventsReceiver.size();
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

    //TimeDog m_clearDbTimeDog = new TimeDog();
    /**
     * check connection in a loop
     */
    public void on1sTimer()
    {

        if (getSettings().getBoolean(KDSSettings.ID.Pager_enabled)) {
            if (this.isExpeditorStation() || isQueueExpo() || isQueueExpoView() ||
                this.isRunnerStation() || isSummaryStation())
                getPagerManager().onTime();
        }

        restoreWorkloadStationOfflineOrderBack();//kp-96
//        //if (m_clearDbTimeDog.is_timeout(1800000)) //30x60x1000, 30mins
//        if (m_clearDbTimeDog.is_timeout(18000)) //30x60x1000, 30mins //debug
//        {
//            remove_statistic_old_data();
//            m_clearDbTimeDog.reset();
//        }
        checkBackgroundImagesAutoSwitch();
    }

    public boolean startPOSListener()
    {
        int n  = m_settings.getInt(KDSSettings.ID.KDS_Data_Source);
        KDSSettings.KDSDataSource srcType = KDSSettings.KDSDataSource.values()[n];
        if (srcType == KDSSettings.KDSDataSource.TCPIP)
        {
            stopPOSListener();
            String error = m_listenPOS.startServer(m_nDataSourceIpPort, m_socksManager, m_sockEventsMessageHandler);
            fireTcpListenServerErrorEvent(m_nDataSourceIpPort, error);
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


            /**
             * don't support statistic app anymore.
             * @return
             */
    public boolean startStatisticListener()
    {
//        stopStatisticListener(); //stop it first.
//        m_listenStatistic.startServer(m_nStatisticPort, m_socksManager, m_sockEventsMessageHandler);
        return true;
    }
    public void stopStatisticListener()
    {
//        m_listenStatistic.stop(); //don't support statistic anymore
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
            //In cell phone,if I just open data , no wifi, app crash.
            if (ar.size() >1)
                m_strLocalMAC = ar.get(1);
            else
                m_strLocalMAC = "";
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

        m_udpStationAnnouncer.start(KDSSettings.UDP_STATION_ANNOUNCER_UDP_PORT, m_sockEventsMessageHandler, m_socksManager);

        //let others stations know me as soon as possible.
        //this.broadcastStationAnnounceInThread();
        this.getBroadcaster().broadcastStationAnnounceInThread2();
        this.getBroadcaster().broadcastRequireStationsUDPInThread();

        startPOSListener();
//        startStatisticListener(); //don't support statistic anymore

        String error = m_listenStations.startServer(m_nStationsInternalIpPort, m_socksManager, m_sockEventsMessageHandler);
        //test kpp1-312
        //String error = m_listenStations.startServer(80, m_socksManager, m_sockEventsMessageHandler);
        fireTcpListenServerErrorEvent(m_nStationsInternalIpPort, error);


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

            //2.0.51
            boolean bEnableSmbV2 = settings.getBoolean(KDSSettings.ID.Enable_smbv2);
            KDSSmbFile.smb_setEnableSmbV2(bEnableSmbV2);

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
//        if (m_errorToast != null)
//            m_errorToast.cancel();
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
    	if (m_strLocalIP.isEmpty()) {
    		KDSLog.e("KDS", "IP not found! Trying to refresh IP and Mac...");
			refreshIPandMAC();
		}
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
        int port = this.m_nStationsInternalIpPort;
        String strport = KDSUtil.convertIntToString(port);
        int nItemsCount = getAllItemsCount();
        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildReturnStationIPCommand2(getStationID(),m_strLocalIP, strport, getLocalMacAddress(), nItemsCount, getSettings().getInt(KDSSettings.ID.Users_Mode), Activation.getStoreGuid());
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
        String strRelations = getSettings().loadStationsRelationString(m_context, true);
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

    private boolean isMyStoreIP(String ip)
    {
        KDSStationActived station =m_stationsConnection.findActivedStationByIP(ip);//id);
        return (station != null);

    }

    private void doUdpReceiveData(KDSSocketInterface sock,String remoteIP,  ByteBuffer buffer, int nLength)
    {
        //m_udpBuffer.appendData(buffer, nLength);
        m_udpBuffer.replaceBuffer(buffer, nLength);//for speed.
        String remoteStationIP =KDSUtil.parseRemoteUDPIP(remoteIP);


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

//                    byte[] bytes = m_udpBuffer.station_info_command_data();
//                    m_udpBuffer.remove(ncommand_end);
//                    String utf8 = KDSUtil.convertUtf8BytesToString(bytes);
                    String utf8 = m_udpBuffer.station_info_string();
                    onUdpReceiveStationAnnounce(utf8);
                    return;
                }
                //break;
                case KDSSocketTCPCommandBuffer.UDP_REQ_STATION: {

                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data
                    m_udpBuffer.remove(ncommand_end);
                    if (!isMyStoreIP(remoteStationIP)) return;
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
                    if (!isMyStoreIP(remoteStationIP)) return;

                    onStatisticAppRequireStationAnnounceInThread2(b0, b1, b2, b3);

                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_SHOW_ID:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data
                    m_udpBuffer.remove(ncommand_end);
                    if (!isMyStoreIP(remoteStationIP)) return;
                    onShowStationID();
                }
                break;
                case KDSSocketTCPCommandBuffer.XML_COMMAND:
                {//
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data

                    byte[] bytes = m_udpBuffer.xml_command_data();
                    m_udpBuffer.remove(ncommand_end);
                    if (!isMyStoreIP(remoteStationIP)) return;
                    String utf8 = KDSUtil.convertUtf8BytesToString(bytes);
                    doUdpXmlCommand(utf8, remoteIP);
                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_CLEAR_DB:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data
                    m_udpBuffer.remove(ncommand_end);
                    if (!isMyStoreIP(remoteStationIP)) return;
                    onClearDB();
                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_ASK_RELATIONS:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data
                    m_udpBuffer.remove(ncommand_end);
                    if (!isMyStoreIP(remoteStationIP)) return;

                    String ip = KDSUtil.parseRemoteUDPIP(remoteIP);
                    String port = KDSUtil.parseRemoteUDPPort(remoteIP);
                    onOtherAskRelations(ip, port);
                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_ASK_BROADCAST_RELATIONS:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)  return; //need more data
                    m_udpBuffer.remove(ncommand_end);
                    if (!isMyStoreIP(remoteStationIP)) return;
                    String strRelations = getSettings().loadStationsRelationString(m_context,true);
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
                    if (!isMyStoreIP(remoteStationIP)) return;

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
                    if (!isMyStoreIP(remoteStationIP)) return;

                    onSyncStationSmartModeEnabledSettingChanged(nVal);
                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_ITEM_BUMPED:
                case KDSSocketTCPCommandBuffer.UDP_ITEM_UNBUMPED: {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0) return; //need more data
                    byte[] bytes = m_udpBuffer.xml_command_data();
                    m_udpBuffer.remove(ncommand_end);
                    if (!isMyStoreIP(remoteStationIP)) return;

                    if (!this.getSettings().getBoolean(KDSSettings.ID.Smart_Order_Enabled))
                        break; //just smart mode needs this.
                    String utf8 = KDSUtil.convertUtf8BytesToString(bytes);

                    onSmartOrderModeItemBumpUnbumped(utf8, (command ==KDSSocketTCPCommandBuffer.UDP_ITEM_BUMPED) );
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
        //don't support statistic anymore
//        String statistic_ip = String.format("%d.%d.%d.%d", KDSUtil.byteToUnsignedInt(b0), KDSUtil.byteToUnsignedInt(b1), KDSUtil.byteToUnsignedInt(b2), KDSUtil.byteToUnsignedInt(b3));
//
//        int port = this.m_nStationsPort;
//        String strport = KDSUtil.convertIntToString(port);
//        int nItemsCount = getAllItemsCount();
//        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildReturnStationIPCommand2(getStationID(),m_strLocalIP, strport, getLocalMacAddress(), nItemsCount, getSettings().getInt(KDSSettings.ID.Users_Mode), Activation.getStoreGuid());
//        //send data to statistic station.
//         (new KDSBroadcastThread(m_udpStationAnnouncer, statistic_ip,KDSSettings.UDP_STATISTIC_ANNOUNCER_PORT, buf )).start();
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


//    public String parseRemoteUDPIP(String remoteIP)
//    {
//        String str = remoteIP;
//        String port = "";
//        String ip = "";
//        str = str.replace("/", "");
//        int n = str.indexOf(":");
//        if (n >0) {
//            ip = str.substring(0, n);
//            port = str.substring(n+1);
//        }
//        return ip;
//    }
//
//    public String parseRemoteUDPPort(String remoteIP)
//    {
//        String str = remoteIP;
//        String port = "";
//        String ip = "";
//        str = str.replace("/", "");
//        int n = str.indexOf(":");
//        if (n >0) {
//            ip = str.substring(0, n);
//            port = str.substring(n+1);
//        }
//        return port;
//    }


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
            String ip = KDSUtil.parseRemoteUDPIP(remoteIP);
            String port =KDSUtil.parseRemoteUDPPort(remoteIP);
            if (ip.equals(getLocalIpAddress()))
            {
                if (port.equals(KDSUtil.convertIntToString(KDSSettings.UDP_STATION_ANNOUNCER_UDP_PORT)))
                    return;
            }
            String s = xmlCommand;
            s = s.replace("<Relations>", "");
            s = s.replace("</Relations>", "");
            if (s.isEmpty()) return;
            SettingsBase.StationFunc oldFunc = getSettings().getStationFunc();

            KDSSettings.saveStationsRelation(m_context, s);
            //update the station ID.
            this.updateSettings(m_context);

            for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
                m_arKdsEventsReceiver.get(i).onReceiveNewRelations();
            if (getSettings().getStationFunc() != oldFunc)
            {
                onMyFunctionChanged(oldFunc, getSettings().getStationFunc());

            }

        }
        else if (xmlCommand.indexOf("<RelationsRet>") >= 0)
        { //ask the relations, and others return. Someone ask all station feedback broadcast their relations.


         //   if (isRequireRelationsFinished()) return;
            if (SettingsBase.isNoCheckRelationWhenAppStart(KDSApplication.getContext()))
                return;
            String s = xmlCommand;
            s = s.replace("<RelationsRet>", "");
            s = s.replace("</RelationsRet>", "");
           // if (s.isEmpty()) return;
            ArrayList<KDSStationsRelation> ar =KDSSettings.parseStationsRelations(s);
            SettingsBase.removeRelationNoCheckOptionStation(ar);

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
        else if (xmlCommand.indexOf("<SMSStationState>")>=0)
        {
            String s = xmlCommand;
            s = s.replace("<SMSStationState>", "");
            s = s.replace("</SMSStationState>", "");
            onReceiveSMSStationStateChanged(s);
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
        ArrayList<KDSStationsRelation> arLocal = KDSSettings.loadStationsRelation(m_context, false);
        if (arReceived.size() != arLocal.size())
            return true;
        for (int i=0; i< arReceived.size(); i++) {
            KDSStationsRelation r = arReceived.get(i);
            if (r == null) //there is bug here
                continue;
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
     * stationID, ip,port,mac,ordersCount,usermode, storeguid
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
//        if (ar.size() < 4) //In old version, this is a bug. Old code:if (ar.size() <= 4)
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

//    public void setStationAnnounceEventsReceiver(StationAnnounceEvents receiver)
//    {
//        m_stationAnnounceEvents = receiver;
//    }

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
        writeStringToStation(stationID, ip, strXml); //2.0.50

//        KDSStationConnection conn = m_stationsConnection.findConnectionByID(stationID);
//        if (conn != null) {
//            if (conn.getSock().isConnected())
//                conn.getSock().writeXmlTextCommand(strXml);
//            else
//                conn.addBufferedData(strXml);
//        }
//        else
//        {
//            KDSStationIP station = new KDSStationIP();
//            station.setID(stationID);
//            station.setIP(ip);
//            station.setPort(m_settings.getString(KDSSettings.ID.KDS_Station_Port));
//
//            m_stationsConnection.connectStationWithData(station, strXml);
//        }
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
    @Override
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

        //KPP1-7
        if (getStationsConnections().isMyQueueStation(stationID))
        {
            KDSLog.d(TAG, KDSLog._FUNCLINE_() + "isMyQueueStation=true");
            onMyQueueStationRestore(stationID, stationIP);
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
            msg.obj = new String(xmlData);
            msg.what = MESSAGE_TO_MAIN.SMB_ERROR.ordinal();
            //m_smbErrorHandler.sendMessage(msg);
            m_refreshHandler.sendMessage(msg);

        }
        else {
            KDSXMLParser.XMLType ntype = checkXmlType(xmlData);
            if (ntype == KDSXMLParser.XMLType.Order)
                doOrderXmlInThread(MESSAGE_TO_MAIN.Order, null, xmlData,smbFileName, true); //2.0.34
            else if (ntype == KDSXMLParser.XMLType.POS_Info)
                doPOSMessage(xmlData);
        }
        //Log.d("SMB Text", xmlData);
    }

    //public Toast m_errorToast = null;
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
     * Rev:
     *  Use the handle to call this function. So it is in main thread now.
     *
     * @param sock
     * @param xmlData
     */
    public void sockevent_onTCPReceiveXml(KDSSocketInterface sock, String xmlData)
    {

        if (!isEnabled()) return;
        //do ack for xml, KPP1-93
        xmlData = m_stationsConnection.responseAck(this.getStationID(), this.getLocalIpAddress(), this.getLocalMacAddress(),sock, xmlData);

        KDSXMLParser.XMLType ntype = checkXmlType(xmlData);
//        if (BuildVer.isDebug())
//            Log.i(TAG, "XML Type=" + ntype.toString());
        switch (ntype)
        {
            case Unknown:
                return;
                //kpp1-363
            case App_Sock_ID:
            {
                setAppSocketID(sock, xmlData);
            }
            break;
            case Order: {
                //doOrderXml(sock, xmlData);
                doOrderXmlInThread(MESSAGE_TO_MAIN.Order, sock, xmlData, "", false); //2.0.34
//                Message msgOrder = new Message();
//                msgOrder.what = MESSAGE_TO_MAIN.Order.ordinal();
//                MessageParam paramOrder = new MessageParam();
//                paramOrder.obj0 = sock;
//                paramOrder.obj1 = new String(xmlData);
//                msgOrder.obj = paramOrder;

//                KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(getStationID(), xmlData);
//                Log.i(TAG, "receive order: " + order.getOrderName());
//

//                m_refreshHandler.sendMessage(msgOrder);
            }
            break;
            case Command: {
                if (!doCommandXmlInMainUI(sock, xmlData))
                    doOrderXmlInThread(MESSAGE_TO_MAIN.COMMAND_XML, sock, xmlData, "", false); //2.0.34

//                Message msg = new Message();
//                msg.what = MESSAGE_TO_MAIN.COMMAND_XML.ordinal();
//                MessageParam p = new MessageParam();
//                p.obj0 = sock;
//                p.obj1 = xmlData;
//                msg.obj = p;
//                m_refreshHandler.sendMessage(msg);
                //doCommandXml(sock, xmlData); //20170612
            }
            break;
            case POS_Info: //kp-102
            {
                doPOSMessage(xmlData);
            }
            break;
        }
//        try {
//            Thread.sleep(500);
//        }
//        catch (Exception e)
//        {
//            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + KDSLog.getStackTrace(e));
//        }
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
//        else if (sock == m_listenStatistic) ////don't support statistic anymore
//        {
//            m_stationsConnection.onAcceptStatisticConnection(sock, sockClient);
//        }
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
        Reset_Focus_after_new_order, //20190403, as the reset focus will calculate all items, it is slow. I move it out of doxml thread.
        Toast_msg,
        Refresh_POS_messages,
    }

    Handler m_refreshHandler = new Handler(){
        public void handleMessage(Message msg) {
            int n = msg.what;
            MESSAGE_TO_MAIN w = MESSAGE_TO_MAIN.values()[n];
//            if (BuildVer.isDebug())
//                Log.i(TAG, "Message =" + w.toString());
            switch (w) {
                case REFRESH_ALL:
                    m_refreshHandler.removeMessages(MESSAGE_TO_MAIN.REFRESH_ALL.ordinal());
                    KDS.this.doRefreshView();
                    break;
                case REFRESH_A:
                    m_refreshHandler.removeMessages(MESSAGE_TO_MAIN.REFRESH_A.ordinal());
                    KDS.this.doRefreshView(KDSUser.USER.USER_A, (RefreshViewParam) msg.obj);
                    break;
                case REFRESH_B:
                    m_refreshHandler.removeMessages(MESSAGE_TO_MAIN.REFRESH_B.ordinal());
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

                    //doCommandXml((KDSSocketInterface) x.obj0, (String)x.obj1);
                    if (!doCommandXmlInMainUI((KDSSocketInterface) x.obj0, (String) x.obj1))
                        doOrderXmlInThread(MESSAGE_TO_MAIN.COMMAND_XML, (KDSSocketInterface) x.obj0, (String) x.obj1, "", false); //2.0.34

                    break;
                case Order:

                    MessageParam xcode = (MessageParam)msg.obj;
//                    String s = (String)xcode.obj1;
//                    KDSDataOrder data =(KDSDataOrder) KDSXMLParser.parseXml(getStationID(), s);
//                    Log.i(TAG, "receive order: " + data.getOrderName());

                    doOrderXmlInThread(MESSAGE_TO_MAIN.Order, (KDSSocketInterface) xcode.obj0, (String)xcode.obj1, "",false); //2.0.34
                    break;
                case Reset_Focus_after_new_order:
                {
                    m_refreshHandler.removeMessages(MESSAGE_TO_MAIN.Reset_Focus_after_new_order.ordinal());
                    setFocusAfterReceiveOrder();
                }
                break;
                case Toast_msg:
                {
                    String s = (String) msg.obj;
                    KDSToast.showMessage(KDSApplication.getContext(), s); //for test
                }
                break;
                case Refresh_POS_messages:
                {
                    for (int i=0;i< m_arKdsEventsReceiver.size(); i++)
                    {
                        m_arKdsEventsReceiver.get(i).onKDSEvent(KDSEventType.Refresh_pos_message, null);
                    }
                }
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
     *             Let???s remove this limitation, which means when KDS receive the xml,
     *             it will display all items on this xml. And please let us know if we apply this, will this affect any features?
     *
     * @param objSource
     *  where we get this order xml data.
     *  It maybe KDSSocketInterface and KDSSMBDataSource
     * @param xmlData
     */
    //private void doOrderXml(KDSSocketInterface sock, String xmlData)
    public KDSDataOrder doOrderXml(Object objSource, String xmlData,String originalFileName, boolean bForceAcceptThisOrder, boolean bRefreshView)
    {

        KDSLogOrderFile.i(TAG, KDSLogOrderFile.formatOrderLog(xmlData));//kpp1-223

        Object obj = KDSXMLParser.parseXml(getStationID(), xmlData);
        if (obj == null) return null;
        if (!(obj instanceof  KDSDataOrder))
            return null;
        KDSDataOrder order =(KDSDataOrder) obj;// KDSXMLParser.parseXml(getStationID(), xmlData);

        //Log.i(TAG, "receive order: " + order.getOrderName());
        //2.0.39
        if (order == null) {
            //if (KDSConst.ENABLE_FEATURE_ORDER_ACKNOWLEDGEMENT) {
                if (this.getSettings().getBoolean(KDSSettings.ID.Notification_order_acknowledgement)) {
                    doOrderAcknowledgement(objSource, order, 0, originalFileName, xmlData, KDSUtil.createNewGUID(), KDSPosNotificationFactory.ACK_ERR_BAD, false);
                }
            //}
            return null;
        }
        else
        {//2.0.44
//            if (KDSConst.ENABLE_FEATURE_ORDER_ACKNOWLEDGEMENT)
//            {
                if (this.getSettings().getBoolean(KDSSettings.ID.Notification_order_acknowledgement)) {//ack error ack
                    KDSPosNotificationFactory.OrderParamError error = KDSPosNotificationFactory.checkOrderParameters(order);
                    if (error != KDSPosNotificationFactory.OrderParamError.OK) {
                        String errorCode = KDSPosNotificationFactory.getOrderParamErrorCode(error);
                        if (objSource != null) {
                            ArrayList<KDSToStation> ar = KDSDataOrder.getOrderTargetStations(order);
                            if (KDSToStations.findInToStationsArray(ar, getStationID()) != KDSToStations.PrimarySlaveStation.Unknown)
                               doOrderAcknowledgement(objSource, order, 1, originalFileName, xmlData, KDSUtil.createNewGUID(), errorCode, false);
                        }
                        return null;
                    }
                }
            //}
        }
        //preparation time mode
        //boolean bPrepEnabled = (KDSSettings.SmartMode.values()[ this.getSettings().getInt(KDSSettings.ID.Smart_mode)] == KDSSettings.SmartMode.Advanced );
        boolean bSmartEnabled = this.getSettings().getBoolean(KDSSettings.ID.Smart_Order_Enabled);

        //if (this.getSettings().getBoolean(KDSSettings.ID.Prep_mode_enabled))
        //if (bPrepEnabled)
          if (bSmartEnabled)
            this.getCurrentDB().smart_add_order_items(order); //keep full order items for preparation time mode.
//
        //tacker is removed.
//        if (isTrackerStation() || isTrackerView())
//        {
//            changeTrackerIDByUserInfo(order);
//
//            if (order.getOrderType().equals(KDSDataOrder.ORDER_TYPE_SCHEDULE))
//                return null; //tracker don't need schedule
//        }

        //for pager feature
        if (isExpeditorStation() ||isQueueExpo() || isQueueExpoView() ||
            isRunnerStation() || isSummaryStation())
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

            nAcceptItemsCount = doOrderFilter(objSource, order, xmlData, bForceAcceptThisOrder,false, bRefreshView);
            if (bSmartEnabled)
                this.getCurrentDB().smart_runner_category_init(order, order.smart_get_sorts());
            if (bRefreshView)
                schedule_process_update_after_receive_new_order();

        }
        if (bRefreshView)
            refreshView();
        if (this.getSettings().getBoolean(KDSSettings.ID.Notification_order_acknowledgement))
        {
            doOrderAcknowledgement(objSource,order,nAcceptItemsCount,originalFileName, xmlData, order.getOrderName(),KDSPosNotificationFactory.ACK_ERR_OK, true);
        }
        return order;


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

    private boolean doNotHandleThisCommand(String xmlData)
    {
        return false;
//        if (KDSConst.ENABLE_ADD_NEW_ANNOUNCE) return false;
//        KDSXMLParserCommand.KDSCommand code = KDSXMLParser.quickGetCodeFromString(xmlData);
//        return doNotHandleThisCommand(code);

//        if (code == KDSXMLParserCommand.KDSCommand.Nothing)
//            return true;
//        if (code == KDSXMLParserCommand.KDSCommand.Station_Add_New_Order)
//        {
//            if (this.isQueueExpo() || this.isExpeditorStation() || this.isTrackerView())
//                return true;
//        }
//        return false;
    }

    /**
     * receive the command xml
     * @param sock
     * @param xmlData
     */
    private void doCommandXml(KDSSocketInterface sock, String xmlData)
    {

        KDSXMLParserCommand command = (KDSXMLParserCommand)KDSXMLParser.parseXml(this.getStationID(), xmlData);
        if (command == null) return;//different version cause command messed.
        KDSXMLParserCommand.KDSCommand code = command.getCode();
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        if (fromStationID.equals(this.getStationID()) &&
                code != KDSXMLParserCommand.KDSCommand.Station_Transfer_Order)
            return; //don't do loop
        //String orderGuidDoOperation = "";
        String orderGuid = "";
        //if (BuildVer.isDebug())
        //    System.out.println("code="+KDSUtil.convertIntToString(code.ordinal()) + ",from=" + fromStationID);
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
                doConfigReceivedXmlCommand( command, xmlData); //it need UI.
                break;
            case Broadcast_Station_Active:
                break;
            case Broadcast_All_Configurations:
                break;
            case Station_Add_New_Order: //in thread
            {
                //Just for coke 24 stations. As they use smb data source, we don't need this.
                //And, it can cause expo stack issue.
                if (doNotHandleThisCommand(xmlData))
                    break;
//                if (this.isQueueExpo() || this.isExpeditorStation() || this.isTrackerView())
//                    break;
                ArrayList<Boolean> ordersExisted = new ArrayList<>();
                ArrayList<KDSDataOrder> ordersChanged = new ArrayList<>();
                //order is parsed order, that is received order
                KDSDataOrder order = KDSStationFunc.doSyncCommandOrderNew(this, command, xmlData, ordersExisted, ordersChanged);
                setFocusAfterReceiveOrder();
                schedule_process_update_after_receive_new_order();
                //
                //syncOrderToWebDatabase(order, ActivationRequest.iOSOrderState.New, ActivationRequest.SyncDataFromOperation.New);

                //if order is not null, it is expo station returned.
                if (order != null)//kpp1-333
                {
                    if (getStationFunction() == KDSSettings.StationFunc.Expeditor ||
                            getStationFunction() == KDSSettings.StationFunc.Queue_Expo ||
                            getStationFunction() == KDSSettings.StationFunc.Runner) {
                        if (getSettings().getBoolean(KDSSettings.ID.Printer_Enabled)) {
                            KDSPrinter.HowToPrintOrder howtoprint = KDSPrinter.HowToPrintOrder.values()[(getSettings().getInt(KDSSettings.ID.Printer_howtoprint))];
                            if (howtoprint == KDSPrinter.HowToPrintOrder.WhileReceive) {
                                boolean bExisted = true;
                                if (ordersExisted.size() > 0)
                                    bExisted = ordersExisted.get(0);
                                if ((!bExisted) || (!isSameChangedOrder(order, ordersChanged))) {
                                    for (int i = 0; i < ordersChanged.size(); i++) {
                                        if (ordersChanged.get(i) != null)
                                            getPrinter().printOrder(ordersChanged.get(i));
                                    }

                                }

                            }
                        }
                    }
                    //kp-152 Report not showing for expo. They use same guid. Fix it.
                    for (int i = 0; i < ordersChanged.size(); i++) {
                        if (ordersChanged.get(i) != null)
                            syncOrderToWebDatabase(ordersChanged.get(i), ActivationRequest.iOSOrderState.New, ActivationRequest.SyncDataFromOperation.New);

                    }
                }

            }
            break;
            case Station_Bump_Order://in thread
            {
                //Please notice the xmldata just contains the order/item id.
                checkLostFocusAfterSyncBumpOrderName(command, xmlData);
                //kpp1-407, save my orginal order
                KDSDataOrder receivedOrder = parseReceivedOrder(command);
                KDSDataOrder existedOrder = null;
                if (receivedOrder != null) {
                    existedOrder = this.getUsers().getOrderByName(receivedOrder.getOrderName());

                }
                //
                ArrayList<KDSDataItem> arChangedItems = new ArrayList<>(); //retrieve changed items.
                orderGuid = KDSStationFunc.doSyncCommandOrderBumped(this, command, xmlData, arChangedItems);
                if (!orderGuid.isEmpty()) {
                    schedule_process_update_after_receive_new_order();
                    sortOrderForMoveFinishedToFront();
                    checkSMS(orderGuid, false); //2.1.10
                }
                if (arChangedItems.size() > 0) //just expo save data to this array
                {//kpp1-62, kpp1-74
                    syncWebBackofficeExpoItemBumpsPreparationTime(orderGuid, arChangedItems, Activation.ItemJobFromOperations.Expo_sync_prep_bump_order);
                }

                //kpp1-407
                mirrorStationSyncWebDatabase(code, command, existedOrder, arChangedItems);
                //comment this code, as unbump can not been sync.
//                if (this.isSummaryStation())
//                {//If we don't remove it, the same ID order can not come in.

//                    if (existedOrder.isAllItemsFinishedForSumStation())
//                    {
//                        getUsers().orderRemove(existedOrder);
//                    }
//                }
            }
            break;
            case Station_Unbump_Order:
                KDSStationFunc.doSyncCommandOrderUnbumped(this,command, xmlData);
                schedule_process_update_to_be_prepare_qty(true);
                schedule_process_update_after_receive_new_order();
                sortOrderForMoveFinishedToFront();
                break;
            case Station_Cancel_Order:
                KDSStationFunc.doSyncCommandOrderCanceled(this,command, xmlData);
                break;
            case Station_Modify_Order:
                KDSStationFunc. doSyncCommandOrderModified(this,command, xmlData);
                break;
            case Station_Bump_Item: {
                prepGetStationBumpedItemCommand(command, code, xmlData);
//                //kpp1-407, save my orginal order
//                String strOrderName = command.getParam("P0", "");
//                KDSDataOrder orderExisted = this.getUsers().getOrderByName(strOrderName);
//                //
//                ArrayList<KDSDataItem> arChangedItem = new ArrayList<>(); //retrieve changed items.
//                orderGuid = KDSStationFunc.doSyncCommandItemBumped(this, command, xmlData, arChangedItem);
//                sortOrderForMoveFinishedToFront();
//                checkSMS(orderGuid, false); //2.1.10
//                if (arChangedItem.size() > 0) //just expo save data to this array
//                {//kpp1-62, kpp1-74
//                    syncWebBackofficeExpoItemBumpsPreparationTime(orderGuid, arChangedItem, Activation.ItemJobFromOperations.Expo_sync_prep_bump_item);
//                }
//                //kpp1-407
//                mirrorStationSyncWebDatabase(code, command, orderExisted, arChangedItem);
            }
                break;
            case Station_Unbump_Item: {
                prepGetStationUnbumpItemCommand(command, code, xmlData);
//                //kpp1-407, save my orginal order
//                String orderName = command.getParam("P0", "");
//                KDSDataOrder myOrder = this.getUsers().getOrderByName(orderName);
//                ;
//
//                //
//                ArrayList<KDSDataItem> arUnbumpItems = new ArrayList<>();
//                orderGuid = KDSStationFunc.doSyncCommandItemUnbumped(this, command, xmlData, arUnbumpItems);
//                sortOrderForMoveFinishedToFront();
//                schedule_process_update_to_be_prepare_qty(true);
//
//                checkSMS(orderGuid, false); //2.1.10
//                //kpp1-407
//                mirrorStationSyncWebDatabase(code, command, myOrder, arUnbumpItems);
            }
                break;
            case Station_Modified_Item:
                KDSStationFunc.doSyncCommandItemModified(this,command, xmlData);
                break;
            case Station_Transfer_Order://order transfered to me
            {
                KDSDataOrder orderTransferIn = KDSStationFunc.doSyncCommandOrderTransfer(this, command, xmlData, fromStationID);
                setFocusAfterReceiveOrder();
                getSoundManager().playSound(KDSSettings.ID.Sound_transfer_order);
                //
                if (orderTransferIn != null) {
                    syncOrderToWebDatabase(orderTransferIn, ActivationRequest.iOSOrderState.New, ActivationRequest.SyncDataFromOperation.New);
                }
            }
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

                //NCR
                String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
                if (strXml.isEmpty())
                    break;
                KDSDataOrder bumpedOrder =(KDSDataOrder) KDSXMLParser.parseXml(getStationID(), strXml);
                if (bumpedOrder == null) break;
                KDSDataOrder myOrder =  this.getUsers().getOrderByName(bumpedOrder.getOrderName());
                //
                KDSStationFunc.doSyncCommandExpoOrderBumped(this, command, xmlData);

                //NCR URGENT
                if (myOrder != null)
                {
                    syncOrderToWebDatabase(myOrder, ActivationRequest.iOSOrderState.Done, ActivationRequest.SyncDataFromOperation.Bump);
                }
            }
            break;
            case Expo_Unbump_Order:
            {
                KDSStationFunc.doSyncCommandExpoOrderUnbumped(this, command, xmlData);
            }
            break;
            case Expo_Bump_Item:
            {
                //
                if (getStationFunction() == SettingsBase.StationFunc.Prep) {
                    prepGetStationBumpedItemCommand(command, code, xmlData);
                }
                /////////////////
                else {
                    KDSStationFunc.doSyncCommandExpoItemBumped(this, command, xmlData);
                }

            }
            break;
            case Expo_Unbump_Item:
            {
                if (getStationFunction() == SettingsBase.StationFunc.Prep)
                    prepGetStationUnbumpItemCommand(command, code, xmlData);
                else
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
            case ACK_XML:
            {
                commandAckXml(fromStationID, command, xmlData);
            }
            break;
            case Queue_ask_sync_from_prep:
            {
                syncOrdersStatusWithQueue(sock, fromStationID, fromIP);
            }
            break;
            case Prep_sync_to_queue:
            {
                onQueueReceivePrepSyncOrdersStatusData(sock, command, fromStationID, fromIP);
            }
            break;
            case Queue_ask_sync_new_orders_from_prep:
            {
                onQueueAskSyncNewOrdersFromPrep(sock, command, fromStationID, fromIP);
            }
            break;
            case Prep_sync_new_order_to_queue:
            {
                KDSStationFunc.doSyncCommandOrderTransfer(this, command, xmlData, "");
            }
            break;
            case Runner_show_category: //kp1-25
            {
                onRunnerChangedCategory(this, command, xmlData);
            }
            break;
            case Runner_start_cook_item:
            {
                onRunnerStartCookManually(this, command, xmlData);
            }
            break;
            case Prep_expo_transfer_order:
            {//kp-116 Transfer Prep -> Transfer Expo.
                //The prep's expo receive this command.
                onPrepOfExpoHasTransferOrder(this, command, xmlData, fromStationID);

            }
            break;
            case Sync_input_message_with_queue:
            {
                onPrepSyncInputMessageWithQueue(this, command, xmlData, fromStationID);
            }
            break;
            case Station_Order_Parked:
            {
                onSyncPrepOrderParked(this, command, xmlData, fromStationID);
            }
            break;
            case Station_Order_Unpark:
            {
                onSyncPrepOrderUnparked(this, command, xmlData, fromStationID);
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

        if (isExpeditorStation() ||
            isRunnerStation() ||
            isSummaryStation()) return;//2.0.15

        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        //KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(getStationID(), strXml);
        //if (order == null) return;
        //String orderName = order.getOrderName();
        String orderName = KDSXMLParser.quickGetOrderIDFromString(strXml);

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
     * 1.       For setup without router, right now if you send order to station, it still requires <KDSStation> tag in order for order to display. Let???s remove this limitation, which means when KDS receive the xml, it will display all items on this xml. And please let us know if we apply this, will this affect any features?
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
     *             Let???s remove this limitation, which means when KDS receive the xml,
     *             it will display all items on this xml. And please let us know if we apply this, will this affect any features?
     * check if this order is for this station
     * 2.0.36
     *   The tag limitation only works in remote folder now, please also apply this change to TCP/IP orders.
     *   Notice: I don't agree this change.
     *
     * kpp1-363, add Object objSource, parameter
     *
     * ----------------
     *   return accept items count
     * @param objSource
     *      if null: order from remote folder.
     *      It maybe KDSSocketInterface and KDSSMBDataSource
     * @param order
     * @param xmlData
     * @param bForceAcceptThisOrderNoStationIDItems
     * @param bForceDeliverToExpo
     *  For order test
     * @param bRefreshView
     * @return
     */
    public int  doOrderFilter(Object objSource,KDSDataOrder order,String xmlData, boolean bForceAcceptThisOrderNoStationIDItems, boolean bForceDeliverToExpo,boolean bRefreshView)
    {


        bForceAcceptThisOrderNoStationIDItems = true;//2.0.36
        //if (bForceAcceptThisOrderNoStationIDItems)//2.0.36
        assignStationIDAsOrderFromRemoteFolder(order, bForceAcceptThisOrderNoStationIDItems);

        //save it for sms feature.
        ArrayList<KDSToStation> arTargetStations = KDSDataOrder.getOrderTargetStations(order);
        order.setSmsOriginalToStations(arTargetStations);
        ArrayList<KDSDataItem> removedItems = new ArrayList<>();
        order = justKeepMyStationItems(order, removedItems);

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
            //TimeDog td = new TimeDog();
            String manipulatedXmlData = manipulateOrderXml(xmlData, removedItems);

            filterInNormalStation(objSource, order,manipulatedXmlData, arTargetStations,bForceDeliverToExpo, bRefreshView);
            //td.debug_print_Duration("filterInNormalStation=");

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
            filterInNormalStation(null, schOrder, "", null, false,true);

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
        //kp-135,if the workload station has expo, remove this order in my expo station, here.
        //


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
        return (getStationFunction() == SettingsBase.StationFunc.Prep);
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
     *   The KDS expo group doesn???t work in latest version,
     *   e.g.: 5 stations with 1,2,3 prep, and 4,5 expo and setup station relation as 12 go to 4 and 3 go to 5.
     *   If I send order to 12, only expo 4 shows the order, the order and if I send order to 3,
     *   then only expo 5 shows the order. But right now, both stations shows the order. (Do this fix first)
     * @param order
     * @return
     */
    private KDSDataOrder keepExpoItemsAccordingToStationsSetting(KDSDataOrder order, ArrayList<KDSDataItem> removedItems)
    {

        //if (!this.isExpeditorStation() && !this.isQueueExpo() && !this.isRunnerStation() &&!this.isSummaryStation())
        if (!isExpoTypeStation())
            return order;
        //KKPP1-152
        if (order.getTransType() == KDSDataOrder.TRANSTYPE_DELETE ||
                //order.getTransType() == KDSDataOrder.TRANSTYPE_MODIFY ||//If the order is not existed, modify will change to new.
                                                                            //So, we should check expo settings in below code.
                order.getTransType() == KDSDataOrder.TRANSTYPE_UPDATE_ORDER)
            return order;

        //kp-16 Expo not receiving order from backup prep station
        //ArrayList<KDSStationIP> arPrepWhoUseMeAsExpo = this.getStationsConnections().getRelations().getPrepStationsWhoUseMeAsExpo(getStationID());
        ArrayList<KDSStationIP> arPrepWhoUseMeAsExpo = this.getStationsConnections().getRelations().getStationsWhoUseMeAsExpo(getStationID());
        //
        KDSStationIP myStation = new KDSStationIP();
        myStation.setID(getStationID());
        arPrepWhoUseMeAsExpo.add(myStation);//add myself

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
                removedItems.add(item);
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
     *  The KDS expo group doesn???t work in latest version,
     *   e.g.: 5 stations with 1,2,3 prep, and 4,5 expo and setup station relation as 12 go to 4 and 3 go to 5.
     *   If I send order to 12, only expo 4 shows the order, the order and if I send order to 3,
     *   then only expo 5 shows the order. But right now, both stations shows the order. (Do this fix first)
     * @param order
     * @return
     */
    public KDSDataOrder justKeepMyStationItems(KDSDataOrder order,ArrayList<KDSDataItem> removedItems )
    {


        //2.0.33
        //KPP1-37
        //Queue expo supports certain stations
        //just keep the items which target station uses I as expo/expo-queue.
        //For KPP1-37, I add queue-expo filter at here.
        //if (this.isExpeditorStation() || this.isQueueExpo() || this.isRunnerStation() || this.isSummaryStation()) //2.1.15.3, KPP1-37
        if (isExpoTypeStation())
            keepExpoItemsAccordingToStationsSetting(order,removedItems);


        //20160418, keep all items if i am expo station
        //One bug I will need you to fix before moving to new project: Setup 2 station, 1 normal,
        // 1expo with router, send order to normal, both station get order, disconnect normal, expo will not receive any order(sample order attached).

//        if (this.isExpeditorStation() ||
//               // this.isQueueStation() ||
//                this.isTrackerStation() ||
//                this.isQueueExpo() ||
//                this.isRunnerStation()||
//                this.isSummaryStation()) return order;
        if (this.isExpoTypeStation() || isTrackerStation())
            return order;
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
//                if ( (!isExpeditorStation()) && (!isQueueStation()) &&(!isTrackerStation())&&(!isQueueExpo()) &&
//                        (!isRunnerStation()) &&
//                        (!isSummaryStation()))
                if (!isExpoTypeStation() && (!isTrackerStation()) && (!isQueueStation()))
                {
                    removedItems.add(order.getItems().getItem(i));
                    order.getItems().removeComponent(i);
                }
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
                //if ( (!this.isQueueExpo()) && (!this.isExpeditorStation()) &&(!isRunnerStation()) &&(!isSummaryStation()))
                if (!isExpoTypeStation())
                {
                    if (order.getItems().getItem(i).getTransType() == KDSDataOrder.TRANSTYPE_MODIFY ||
                            order.getItems().getItem(i).getTransType() == KDSDataOrder.TRANSTYPE_DELETE)
                        continue;
                }
                //remove this unassigned station
                removedItems.add(order.getItems().getItem(i));
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
        //TimeDog td = new TimeDog();
        setFocusAfterReceiveOrder(KDSUser.USER.USER_A);
        if (this.isMultpleUsersMode())
            setFocusAfterReceiveOrder(KDSUser.USER.USER_B);
        //td.debug_print_Duration("setFocusAfterReceiveOrder duration");
        for (int i = 0; i< m_arKdsEventsReceiver.size(); i++)
        {
            m_arKdsEventsReceiver.get(i).onKDSEvent(KDSEventType.On_receive_new_order_to_sort, null);
        }
    }

    /**
     * Rev.:
     *      kpp1-437, if focused order is in next page, this function will cause page changed after get new order.
     *
     * @param userID
     */
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
            //kpp1-437, just comment it.
            //setFocusToOrder(KDSConst.RESET_ORDERS_LAYOUT);

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
     * rev.:
     *  kpp1-363. Missing Orders on Expo.
     *          I doubt order was send to expo while send order to prep by tcp/ip directly.
     *          So, add datasourceobj parameter. If it is null, this order is from remote folder.
     *               If it is not null, it is from tcp/ip, then auto send order to expo.
     * -----------------------
     * @param objSource
     *       null,KDSSocketInterface or KDSSMBDataSource
     *       Use it to identify order source.
     * @param order
     * @param arOriginalTargetStations
     *  For sms feature, just send one sms when order goes to multiple stations.(No expo existed).
     *  It can been null!!!! Please Notice!!!
     * @return
     *  items count
     */
    public int filterInNormalStation(Object objSource, KDSDataOrder order,String xmlData, ArrayList<KDSToStation> arOriginalTargetStations,boolean bForceDeliverToExpo, boolean bRefreshView)
    {
        int nItemsCount = 0;
        if (order != null)
            nItemsCount  = order.getItems().getCount();//2.0.40

        int ntrans = order.getTransType();
        if (ntrans == KDSDataOrder.TRANSTYPE_MODIFY)
        {//if order don't existed, add it.
            if (m_users.getOrderByNameIncludeParked(order.getOrderName()) == null) { //kpp1-393, check parked orders too.
                if (!order.isAllItemsNotForNew())//if just single item withe "del/modify", it will cause add a new item ugly
                    ntrans = KDSDataOrder.TRANSTYPE_ADD;
            }
            //kpp1-409, order name is empty, but its guid is not.
            if (order.getOrderName().isEmpty() && (m_users.getOrderByGUID(order.getGUID()) != null))
            {//restore old transtype

               ntrans = KDSDataOrder.TRANSTYPE_MODIFY;
            }


        }
        switch (ntrans)
        {
            case KDSDataOrder.TRANSTYPE_UNKNOWN: {
                break;
            }
            case KDSDataOrder. TRANSTYPE_ADD: {
                KDSLog.order("", "Order ID:[" + order.getOrderName() + "] New" );

                if (order.getItems().getCount() <= 0) return nItemsCount;
                //KDSStationFunc.orderAdd(this, order, true, true);
                //TimeDog t = new TimeDog();
                boolean bDeliverToExpo = getSettings().getBoolean(KDSSettings.ID.Deliver_new_order_to_slave_expo);
                if (bForceDeliverToExpo)
                    bDeliverToExpo = true; //for test button
                //kpp1-363,
                if (!bDeliverToExpo) {
                    if (objSource != null) {
                        if (objSource instanceof KDSSocketTCPSideBase) {
                            if (!((KDSSocketTCPSideBase) objSource).getAppSocketID().equals(KDSConst.ROUTER_SOCKET_ID))
                                bDeliverToExpo = true; //force order go to expo.
                        }
                    }
                }
                // printer debug
                //KDSLog.e(TAG, KDSLog._FUNCLINE_() + "Before add to db: items count=" + KDSUtil.convertIntToString(order.getItems().getCount()));


                ///
                ArrayList<KDSDataOrder> ordersAdded = m_users.users_orderAdd(order, xmlData,true, bDeliverToExpo, bRefreshView);//////
                //kpp1-310 Rush orders creating previous page
                if (!checkRushOrderReceivedThenChangeFirstShowingOrder(ordersAdded) )
                {
//                    for (int i = 0; i< m_arKdsEventsReceiver.size(); i++)
//                    {
//                        m_arKdsEventsReceiver.get(i).onKDSEvent(KDSEventType.On_receive_new_order, null);
//                    }
                }
                //printer debug.
//                for (int i = 0; i < ordersAdded.size(); i++) {
//                    KDSLog.e(TAG, KDSLog._FUNCLINE_() + "After add to db: items count=" + KDSUtil.convertIntToString(ordersAdded.get(i).getItems().getCount()));
//                }

                //t.debug_print_Duration("orderAdd");
                //set the preparation time mode sorts
                for (int i = 0; i < ordersAdded.size(); i++) {
                    ordersAdded.get(i).smart_set_sorts(order.smart_get_sorts());
                    //send sms
                    if (i == 0) {

                        checkSMS(ordersAdded.get(i), false, arOriginalTargetStations); //2.1.10
                    }
                }
                //t.debug_print_Duration("TRANSTYPE_ADD1");
                //beep
                if (this.getSettings().getBoolean(KDSSettings.ID.Beeper_Enabled)) {
                    KDSBeeper.BeeperType beeperType = KDSBeeper.BeeperType.values()[getSettings().getInt(KDSSettings.ID.Beeper_Type)];
                    if (beeperType == KDSBeeper.BeeperType.Any)
                        KDSBeeper.beep();
                    else if (beeperType == KDSBeeper.BeeperType.Rush) {
                        for (int i = 0; i < ordersAdded.size(); i++) {
                            if (ordersAdded.get(i).isRush())
                                KDSBeeper.beep();
                        }
                    }
                }
                //check if all items hidden
                //Hide order with no visible items
                int nHiddenOrders = 0;
                for (int i=0; i< ordersAdded.size(); i++)
                {
                    if (isAllItemsHidden(ordersAdded.get(i)))
                    {
                        this.getUsers().orderCancel(ordersAdded.get(i), false);
                        nHiddenOrders ++;
                    }
                }
                if (nHiddenOrders == ordersAdded.size())
                    break;
                /////////////////

                if (getStationsConnections().isBackupOfOthers()) {
                    if (m_kdsState.getPrimaryOfBackupLost())
                        getSoundManager().playSound(KDSSettings.ID.Sound_backup_station_orders_received);
                } else
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
                if (bRefreshView)
                {
                    this.getCurrentDB().clearExpiredBumpedOrders(getSettings().getBumpReservedCount());
//                if (ordersAdded.size() == 1)
//                { //focus first one
                    //t.debug_print_Duration("TRANSTYPE_ADD4");

                    schedule_process_update_to_be_prepare_qty(bRefreshView);

                    setFocusAfterReceiveOrder();
                }
                //do it before reset all items for save memory.
                //sync with backoffice
                for (int i = 0; i < ordersAdded.size(); i++) {
                    syncOrderToWebDatabase(ordersAdded.get(i), ActivationRequest.iOSOrderState.New, ActivationRequest.SyncDataFromOperation.New);
                }
                //t.debug_print_Duration("TRANSTYPE_ADD5");
                //20190619
                // in coke branch, it have ghost orders existed. I comment this function.
                //It will prevent the empty order existed.
                //resetOrdersForSaveMemoryAfterGetNewOrder(ordersAdded);
                //t.debug_print_Duration("TRANSTYPE_ADD6");

            }
            break;
            case KDSDataOrder. TRANSTYPE_DELETE:{
                KDSLog.order("", "Order ID:[" + order.getOrderName() + "] Void" );
                //delete order by xml command
                ArrayList<KDSDataOrder> canceledOrders = m_users.orderCancel(order);
                this.getCurrentDB().clearExpiredBumpedOrders( getSettings().getBumpReservedCount());
                //KDSStationFunc.orderCancel(this, order);
                getSoundManager().playSound(KDSSettings.ID.Sound_bump_order);
                //NCR-URGENT
                for (int i=0; i< canceledOrders.size(); i++)
                    syncOrderToWebDatabase(canceledOrders.get(i), ActivationRequest.iOSOrderState.Done, ActivationRequest.SyncDataFromOperation.Bump);

                break;
            }
            case KDSDataOrder. TRANSTYPE_MODIFY:{
                KDSLog.order("", "Order ID:[" + order.getOrderName() + "] Modify" );
                //KDSStationFunc.orderInfoModify(this, order);
                m_users.orderInfoModify(order, false);
                schedule_process_update_to_be_prepare_qty(true);
                getSoundManager().playSound(KDSSettings.ID.Sound_modify_order);
                //send sms
                checkSMS(order, false, null); //2.1.10
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
                KDSLog.order("", "Order ID:[" + order.getOrderName() + "] Update" );
                if (order.getItems().getCount()<=0) return nItemsCount;

                //TimeDog t = new TimeDog();
                ArrayList<KDSDataOrder> ordersAdded =  m_users.orderUpdate(order, true);//////
                //send sms
                for (int i=0; i< ordersAdded.size(); i++) {
                    //send sms
                    checkSMS(ordersAdded.get(0), false,null); //2.1.10
                    break;
                }
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
            loadAllActiveOrdersInfo(false);
        }
        catch (Exception e)
        {
            //KDSLog.e(TAG, e.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e );
        }

    }

    /**
     *
     * @param bLoadAllToUserA
     *      For queue
     */
    public void loadAllActiveOrdersInfo(boolean bLoadAllToUserA)
    {

        if (m_stationsConnection.isMirrorOfOthers()) //I am a mirror of other stations
        {
            ArrayList<String> arStationID = new ArrayList<>();
            arStationID.add(getStationID());
            ArrayList<KDSStationIP> primaryMirrors = m_stationsConnection.getRelations().getPrimaryStationsWhoUseMeAsMirror();
            for (int i=0; i< primaryMirrors.size(); i++)
                arStationID.add(primaryMirrors.get(i).getID());

            if (m_users.getUserA() != null) {
                int userid = KDSSettings.KDSScreen.Screen_A.ordinal();
                if (bLoadAllToUserA)
                    userid = -1;
                m_users.getUserA().setOrders(m_dbCurrent.ordersLoadAllJustInfo(arStationID, userid, false));
                m_users.getUserA().setParkedOrders(m_dbCurrent.ordersLoadAllJustInfo(arStationID, userid, true));

            }
            if (!bLoadAllToUserA) {
                if (m_users.getUserB() != null) {
                    m_users.getUserB().setOrders(m_dbCurrent.ordersLoadAllJustInfo(arStationID, KDSSettings.KDSScreen.Screen_B.ordinal(), false));
                    m_users.getUserB().setParkedOrders(m_dbCurrent.ordersLoadAllJustInfo(arStationID, KDSSettings.KDSScreen.Screen_B.ordinal(), true));

                }
            }
        }
        else {

            if (m_users.getUserA() != null) {
                if (m_dbCurrent == null) return;
                int userid = KDSSettings.KDSScreen.Screen_A.ordinal();
                if (bLoadAllToUserA)
                    userid = -1;
                m_users.getUserA().setOrders(m_dbCurrent.ordersLoadAllJustInfo(getStationID(), userid, false));
                m_users.getUserA().setParkedOrders(m_dbCurrent.ordersLoadAllJustInfo(getStationID(), userid, true));
            }
            if (!bLoadAllToUserA) {
                if (m_users.getUserB() != null) {
                    if (m_dbCurrent == null) return;
                    m_users.getUserB().setOrders(m_dbCurrent.ordersLoadAllJustInfo(getStationID(), KDSSettings.KDSScreen.Screen_B.ordinal(), false));
                    m_users.getUserB().setParkedOrders(m_dbCurrent.ordersLoadAllJustInfo(getStationID(), KDSSettings.KDSScreen.Screen_B.ordinal(), true));
                }
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

        //KDSLog.d(TAG, KDSUtil.getCurrentTimeForLog()+ " refreshView");
        m_refreshHandler.removeMessages(MESSAGE_TO_MAIN.REFRESH_ALL.ordinal());
        for (int i=0; i< m_arKdsEventsReceiver.size(); i++) {
            m_arKdsEventsReceiver.get(i).onRefreshView(KDSUser.USER.USER_A.ordinal(), m_users.getUserA().getOrders(), RefreshViewParam.None);
            m_arKdsEventsReceiver.get(i).onRefreshSummary(KDSUser.USER.USER_A.ordinal());
            if (isValidUser(KDSUser.USER.USER_B)) {
                m_arKdsEventsReceiver.get(i).onRefreshView(KDSUser.USER.USER_B.ordinal(), m_users.getUserB().getOrders(), RefreshViewParam.None);
                m_arKdsEventsReceiver.get(i).onRefreshSummary(KDSUser.USER.USER_B.ordinal());
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
            m_arKdsEventsReceiver.get(i).onRefreshView(userID.ordinal(), orders,nParam);// RefreshViewParam.None);
            m_arKdsEventsReceiver.get(i).onRefreshSummary(userID.ordinal());
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

            if (m_threadPing != Thread.currentThread()) return;
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
            //check dirty offline data.
            checkDirtyOfflineData();
            //m_printer.onPing();

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
        //Log.i(TAG, "--->get new settings <----");
        if (KDSGlobalVariables.getMainActivity() != null)
            KDSGlobalVariables.getMainActivity().suspendChangedEvent(true);
        String strConfig = command.getParam(KDSConst.KDS_Str_Param, "");
        loadSettingsXml(strConfig);
        //kpp1-212
        if (KDSGlobalVariables.getMainActivity() != null) {
            KDSGlobalVariables.getMainActivity().suspendChangedEvent(false);
            KDSGlobalVariables.getMainActivity().onSharedPreferenceChanged(null, "");
        }

    }

    public void loadSettingsXml(String strConfig)
    {
        if (strConfig.isEmpty())
            return;
        SettingsBase.StationFunc oldFunc = m_settings.getStationFunc();
        Log.i(TAG, "old func=" + oldFunc);
        m_settings.parseXmlText(m_context, strConfig, true, true);
        m_settings.save(m_context);

        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
            m_arKdsEventsReceiver.get(i).onRetrieveNewConfigFromOtherStation();
        this.m_stationsConnection.refreshRelations(m_context, this.getStationID());
        //don't save old data.
        SettingsBase.StationFunc newFunc = m_settings.getStationFunc();
        Log.i(TAG, "new func=" + newFunc);
        if (oldFunc != newFunc)
            onMyFunctionChanged(oldFunc, newFunc);
    }

    public void loadSettingsXmlAll(String strConfig)
    {
        if (strConfig.isEmpty())
            return;
        //m_settings.parseXmlTextAll(m_context, strConfig);
        m_settings.parseXmlText(m_context, strConfig, true, false);
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
                //conn.addBufferedData(s);
                m_stationsConnection.getNoConnectionBuffer().add(stationID, s, NoConnectionDataBuffers.MAX_BACKUP_DATA_COUNT);
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
                //willConn.addBufferedData(s);
                m_stationsConnection.getNoConnectionBuffer().add(stationActive.getID(), s, NoConnectionDataBuffers.MAX_BACKUP_DATA_COUNT);
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
        KDSDataOrder order = this.getUsers().getOrderByGUID(orderGuid);

        KDSStationFunc.TransferingStatus result =  KDSStationFunc.orderTransfer(this.getUsers().getUser(userID), orderGuid, toStationID, nScreen);
        switch (result)
        {
            case Success: {
                //the message was shown in  KDSStationFunc.orderTransfer
                //showMessage("Transfer order done");
                //sync with back office.
                if (order != null)
                    syncOrderToWebDatabase(order, ActivationRequest.iOSOrderState.New, ActivationRequest.SyncDataFromOperation.Transfer_go);
            }
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
            return this.getCurrentDB().summaryItems(this.getStationID(),userID.ordinal(),false, false, (sumOrderBy == KDSSettings.SumOrderBy.Ascend));//  KDSConst.Screen.SCREEN_A.ordinal(),orders.getAllOrderGUID(), false );
            //return this.getCurrentDB().summaryItems(this.getStationID(),userID.ordinal(),orders.getAllOrderGUID(), false);//  KDSConst.Screen.SCREEN_A.ordinal(),orders.getAllOrderGUID(), false );
        else if (sumType == KDSSettings.SumType.CondimentsOnly)
        {
            return this.getCurrentDB().summaryOnlyCondiments(userID.ordinal(),(sumOrderBy == KDSSettings.SumOrderBy.Ascend), false);//  KDSConst.Screen.SCREEN_A.ordinal(),orders.getAllOrderGUID(), false );
        }
        else //with condiments, kpp1-415
            return this.getCurrentDB().summaryItems(this.getStationID(),userID.ordinal(),false, true, (sumOrderBy == KDSSettings.SumOrderBy.Ascend));//  KDSConst.Screen.SCREEN_A.ordinal(),orders.getAllOrderGUID(), false );
            //return this.getCurrentDB().summaryItems(this.getStationID(),userID.ordinal(),orders.getAllOrderGUID(), true, (sumOrderBy == KDSSettings.SumOrderBy.Ascend));//  KDSConst.Screen.SCREEN_A.ordinal(),orders.getAllOrderGUID(), false );


    }


    public void clearAll()
    {

        //this.getOrders().clear();
        m_users.ordersClear();
        this.getCurrentDB().clear();
        this.getSupportDB().clear();
        this.getStationsConnections().clear();//clear buffered data
        m_xmlDataBuffer.clear(); //For speed.
        m_refreshHandler.removeMessages(MESSAGE_TO_MAIN.Order.ordinal());
        this.getStatisticDB().clear(); //clear this data too, as we don't need it any more.
        this.refreshView();
    }

    public void clearStatisticData()
    {

        this.getStatisticDB().clear();
    }
    public KDSSettings getSettings()
    {
        return m_settings; //this settings is saved in layout
    }

    public KDSSettings.ID checkKDSKbdEvent(KeyEvent ev, KDSKbdRecorder kbd)
    {
        boolean bCleanEnabled = getSettings().getBoolean(KDSSettings.ID.cleaning_enable_alert);
        return m_bumpbarFunctions.getKDSKeyboardEvent(ev, kbd, bCleanEnabled);
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
        return writeXmlToPOSData(KDSXML.formatXml(strXml), KDSConst.SMB_FOLDER_NOTIFICATION, toFileName );
        //return writeXmlToPOSData(strXml, KDSConst.SMB_FOLDER_NOTIFICATION, toFileName );

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
                KDSStationFunc.sync_with_stations(this, KDSXMLParserCommand.KDSCommand.Station_Bump_Item, pNormalOrder, pitem, "");

                //sync_order_operation_inform_expeditor(pLs6kParent, nStation, pNormalOrder, pitem, Station_Inform_Exp_Schedule_Item_Ready);
                //sync_order_operation_inform_parent(pLs6kParent, get_user_id() , ORDER_OPERATION_ITEM_SET_READY,pNormalOrder->GetOrderID(),
                 //       pitem->GetID(), pNormalOrder, pitem);
                bchanged = true;
            }

        }
        this.getCurrentDB().schedule_process_set_item_ready_qty(pScheduleOrder);//->m_nInStation,pOrder->m_nUser, orderID, pOrder->get_prepare_item()->GetID(), nnewready );
        schedule_process_update_to_be_prepare_qty(true);
      //  if (bchanged) refreshView();
    }

    /**
     * check all schedule order, and update its to_be_prepared qty
     */
    public void schedule_process_update_to_be_prepare_qty(boolean bRefreshView)
    {
        schedule_process_update_to_be_prepare_qty(KDSUser.USER.USER_A, bRefreshView);
        if (isMultpleUsersMode())
            schedule_process_update_to_be_prepare_qty(KDSUser.USER.USER_B, bRefreshView);
    }

    /**
     * Here: bug
     *      It load all items to buffer, this will slow down the showing speed
     * @param userID
     */
    public void schedule_process_update_to_be_prepare_qty(KDSUser.USER userID, boolean bRefreshView) {
        if (getUsers() == null) return;
        if (getUsers().getUser(userID) == null) return;
        KDSDataOrdersDynamic orders = getUsers().getUser(userID).getOrders();
        //  boolean bchanged = false;
        //synchronized (orders.m_locker) { //there is java.lang.NullPointerException.
        //this locker will slow down the doxml speed, just use try --catch.
        try {
            for (int i = 0; i < orders.getCount(); i++) {
                // if (!orders.get(i).is_schedule_process_order()) continue;

                KDSDataOrder order = orders.getOrderByIndexWithoutLoadData(i);
                if (order == null) break;
                if (!order.is_schedule_process_order()) continue;

                if (getCurrentDB().schedule_order_update_not_ready_qty((ScheduleProcessOrder) orders.get(i))) {//changed,
                    KDSStationFunc.sync_with_stations(this, KDSXMLParserCommand.KDSCommand.Schedule_Item_Ready_Qty_Changed, orders.get(i), orders.get(i).getItems().getItem(0), "");
                    //bchanged = true;
                }

            }

    }
    catch (Exception e) {
    }
  //      if (bchanged)
        if (bRefreshView)
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
        c.setName("SosReport");
        c.start();
    }

    public void doStatisticReport(KDSSocketInterface sock, KDSXMLParserCommand command)
    {

        StatisticThread t = new StatisticThread();
        t.m_sock = sock;
        t.m_command = command;
        t.m_kds = this;
        Thread c = new Thread(t);
        c.setName("StatisticReport");
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
    public void onSmartOrderModeItemBumpUnbumped(String command, boolean bBumped)
    {
        ArrayList<String> ar = KDSUtil.spliteString(command, ",");
        if (ar.size()<2)
            return;
        String orderName = ar.get(0);
        for (int i=1; i< ar.size(); i++)
        {
            String itemName = ar.get(i);
            if (bBumped)
                smart_other_station_item_bumped(orderName, itemName);
            else
                smart_other_station_item_unbumped(orderName, itemName);
        }
    }

    public void smart_other_station_item_bumped(String orderName,String itemName)
    {
        getUsers().getUser(KDSUser.USER.USER_A).smart_other_station_item_bumped(orderName, itemName);
        if (isMultpleUsersMode())
            getUsers().getUser(KDSUser.USER.USER_B).smart_other_station_item_bumped(orderName, itemName);
    }

    public void smart_other_station_item_unbumped(String orderName,String itemName)
    {
        getUsers().getUser(KDSUser.USER.USER_A).smart_other_station_item_unbumped(orderName, itemName);
        if (isMultpleUsersMode())
            getUsers().getUser(KDSUser.USER.USER_B).smart_other_station_item_unbumped(orderName, itemName);
    }

    public int getOpenedOrderSourceIpPort()
    {
        return m_nDataSourceIpPort;
    }

    public KDSSocketUDP getUDP()
    {
        return m_udpStationAnnouncer;
    }
    public int getOpenedStationsCommunicatingPort()
    {
        return m_nStationsInternalIpPort;
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



    /**
     *
     * @param order
     * @param bOrderBumped
     * @param arOrignalTargetStations
     *  just the min station send out sms of expo is not existed.
     */
    public void checkSMS(KDSDataOrder order, boolean bOrderBumped, ArrayList<KDSToStation> arOrignalTargetStations)
    {
        if (!getSettings().getBoolean(KDSSettings.ID.SMS_enabled))
            return;

        if (this.isPrepStation())
        {//if expo existed, send nothing.
            if (this.getStationsConnections().getRelations().getAllExpoStations().size()>0)
                return;
            else //just min station send SMS.
            {
                if (!bOrderBumped && (arOrignalTargetStations!=null) ) { //new order
                    String minStationID = order.findSMSMinStationID();
                    if (!minStationID.isEmpty()) {
                        if (!minStationID.equals(getStationID())) {
                            return; //just min send sms
                        }
                    }
                }
                else if (!bOrderBumped && (arOrignalTargetStations == null))
                {//item bump/unbump
                    if (!order.isAllItemsFinished()) return;
                    if (!order.isSMSAllOtherStationsDone(getStationID()))
                        return;
                }
                else//bump order
                {
                    if (!order.isSMSAllOtherStationsDone(getStationID()))
                        return; //If all others finished same order part, send SMS
                    if (order.isSMSStationsDone(getStationID()))
                        return; //I have do broadcast "done" udp, but other is not finished.
                                 //Don't send SMS here. Other station will do it.
                }
            }
        }
        if (order.isSMSStateChanged(this.isExpeditorStation() || this.isRunnerStation()||this.isSummaryStation(), bOrderBumped))
        {
            if (m_activationHTTP != null) {
                int nSMSState = order.getSMSCurrentState(this.isExpeditorStation()|| this.isRunnerStation()||this.isSummaryStation(), bOrderBumped);
                m_activationHTTP.postSMS( order, nSMSState);

                showToastMessage("SMS:" + KDSDataOrder.getSMSStateString(nSMSState));
                //KDSToast.showMessage(KDSApplication.getContext(), "SMS:" + KDSUtil.convertIntToString(nSMSState)); //for test
            }
        }
    }

//    private String findMinStationID(ArrayList<KDSToStation> arOrignalTargetStations)
//    {
//        if (arOrignalTargetStations == null)
//            return "";
//        String minStationID = "";
//        for (int i=0; i< arOrignalTargetStations.size(); i++)
//        {
//            String stationID = arOrignalTargetStations.get(i).getPrimaryStation();
//            if (minStationID.isEmpty())
//                minStationID = stationID;
//            else
//            {
//                if (stationID.compareTo(minStationID)<0)
//                    minStationID = stationID;
//
//            }
//        }
//        return minStationID;
//    }

    public void checkSMS(String orderGuid, boolean bOrderBumped)
    {
        if (!getSettings().getBoolean(KDSSettings.ID.SMS_enabled))
            return;
        KDSDataOrder order = getUsers().getOrderByGUID(orderGuid);
        if (order == null) return;


        checkSMS(order, bOrderBumped, null);


    }

    public void onSMSSuccess(String orderGuid, int smsState)
    {

        KDSDataOrder order = getUsers().getOrderByGUID(orderGuid);
        if (order != null) {
            order.setSMSLastSendState(smsState);
            getCurrentDB().setSMSState(orderGuid, smsState);
        }

    }

    /**
     *
     * @param orderGuid
     * @param bOrderBumped
     */
    /**
     * Don't check unbump operation. It can not rollback.
     * @param orderGuid
     *      When order bump, the order has deleted, we can find it again.
     *      So, pass orderName to me.
     * @param orderName
     *      When item bump, the orderName is empty
     *      When order bump, the order has deleted, we can find it again.
     *      So, pass orderName to me.
     * @param bOrderBumped
     */
    public void checkBroadcastSMSStationStateChanged(String orderGuid,String orderName,boolean bAllItemsFinished, boolean bOrderBumped)
    {
        if (!getSettings().getBoolean(KDSSettings.ID.SMS_enabled))
            return;
        if (orderName.isEmpty()) {
            KDSDataOrder order = getUsers().getOrderByGUID(orderGuid);
            if (order == null) return;
            orderName = order.getOrderName();
        }

        if (this.isPrepStation())
        {//if expo existed, send nothing.
            if (this.getStationsConnections().getRelations().getAllExpoStations().size()>0)
                return;
            else //just min station send SMS.
            {
                if (bAllItemsFinished || bOrderBumped)
                    m_broadcaster.broadcastSmsStationStateChanged(getStationID(), orderName, true);


            }
        }



    }

    /**
     *
     * @param stationState
     *  Format:
     *      StationID, orderName, Done
     */
    public void onReceiveSMSStationStateChanged(String stationState)
    {
        ArrayList<String> ar = KDSUtil.spliteString(stationState, ",");
        if (ar.size() != 3) return;
        String stationID = ar.get(0);
        String orderName = ar.get(1);
        String isDone = ar.get(2);
        KDSDataOrder order =  this.getUsers().getOrderByName(orderName);
        if (order == null)
            return;
        order.setSmsOriginalToStationState(stationID, true);
        this.getCurrentDB().setSMSStationsState(order.getGUID(), order.getSmsOriginalOrderGoToStations());
    }

//    Thread m_stationAnnounceThread = null;
//    StationAnnounceRunnable m_stationAnnounceRunnable = null;
//    private void doStationAnnounceInThread(String strInfo)
//    {
////        StationAnnounceRunnable r = new StationAnnounceRunnable(strInfo);
////        Thread t = new Thread(r);
////
////        t.start();
//        if (m_stationAnnounceThread == null || (!m_stationAnnounceThread.isAlive()))
//        {
//
//            Log.d(TAG, "start announce thread");
//
//            m_stationAnnounceRunnable = new StationAnnounceRunnable(strInfo);
//            m_stationAnnounceThread = new Thread(m_stationAnnounceRunnable);
//            m_stationAnnounceThread.start();
//        }
//        else
//        {
//
//            m_stationAnnounceRunnable.append(strInfo);
// //       }
//    }

    @Override
    protected void doStationAnnounce(String strInfo)
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
        if (ar.size() >4) //add orders count
            itemsCount = ar.get(4);

        int nUserMode = 0;

        if (ar.size() >5)
        {
            int n = KDSUtil.convertStringToInt( ar.get(5),0 );
            if (n <0 || n>1)
                n = 0;
            nUserMode = n;
        }
        String storeGuid = "";
        if (ar.size() >6) {
            storeGuid = ar.get(6);
            if (!KDSConst._DEBUG) {
                if (storeGuid.isEmpty()) //don't need empty store.
                    return;
            }
        }

        //check the store guid, different store can run in same ethernet.
        if (!storeGuid.equals(Activation.getStoreGuid()))
            return; //it is not my store station

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
        station.setStoreGuid(storeGuid);

        station.updatePulseTime();//record last received time

        //some connection don't have the station ID in it. use this function to update them.
        //comment it for debuging the connect with data function.
        m_stationsConnection.refreshAllExistedConnectionInfo();


        //if (m_stationAnnounceEvents != null)
        //    m_stationAnnounceEvents.onReceivedStationAnnounce(station);//id, ip, port, mac);
        if (m_stationAnnounceEvents != null) {
            Message msg = new Message();
            msg.what = ANNOUNCE_MSG_SEND_EVENT;
            msg.obj = station;
            m_announceHander.sendMessage(msg);
        }
        if (bNewStation) {
            //announce_restore_pulse(id, ip);
            Message msg = new Message();
            msg.what = ANNOUNCE_MSG_STATION_RESTORE;
            msg.obj = station;
            m_announceHander.sendMessage(msg);
        }
    }

    public boolean isThreadRunning()
    {
        return m_bRunning;
    }
    Thread m_threadOrdersXml = null;
    Object m_lockerForOrdersThread = new Object();
    Vector<DoOrdersXmlThreadBuffer> m_xmlDataBuffer = new Vector<>();

    final int BATCH_MAX_COUNT = 5;
    /**
     * 20190222
     * Release UI works.
     * @param objSource
     * @param xmlData
     * @param originalFileName
     * @param bForceAcceptThisOrder
     */
    public void doOrderXmlInThread(MESSAGE_TO_MAIN xmlType, Object objSource, String xmlData,String originalFileName, boolean bForceAcceptThisOrder)
    {


        if (xmlType == MESSAGE_TO_MAIN.COMMAND_XML &&
                doNotHandleThisCommand(xmlData))
            return;
        DoOrdersXmlThreadBuffer data = new DoOrdersXmlThreadBuffer();
        data.m_objSource = objSource;
        data.m_originalFileName = originalFileName;
        data.m_xmlData = xmlData;
        data.m_bForceAcceptThisOrder = bForceAcceptThisOrder;
        data.m_xmlType = xmlType;

        synchronized (m_lockerForOrdersThread) {
            m_xmlDataBuffer.add(data);
        }

        if (m_threadOrdersXml == null ||
                !m_threadOrdersXml.isAlive())
        {

            m_threadOrdersXml = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (m_threadOrdersXml != Thread.currentThread())
                            return;
                        Vector<DoOrdersXmlThreadBuffer> arDone = new Vector<>();
                        //List<DoOrdersXmlThreadBuffer> arDoing = null;
                        while (m_bRunning) {
                            int ncount = m_xmlDataBuffer.size();
                            //Log.i(TAG, KDSUtil.convertIntToString(ncount));
                            if (ncount <= 0) {
                                try {
                                    Thread.sleep(50);
                                    continue;
                                } catch (Exception e) {
                                }

                            }
                            if (BuildVer.isDebug())
                                Log.i(TAG, "Waiting order xml=" + KDSUtil.convertIntToString(ncount));
                            if (ncount > BATCH_MAX_COUNT) ncount = BATCH_MAX_COUNT;
                            arDone.clear();
                            //arDoing = m_xmlDataBuffer.subList(0, ncount);
                            //DoOrdersXmlThreadBuffer data = null;
                            boolean bAddNew = false;
                            for (int i = 0; i < ncount; i++) {
                                try {
                                    DoOrdersXmlThreadBuffer data = m_xmlDataBuffer.get(i);
                                    switch (data.m_xmlType) {
                                        case Order:

                                            //TimeDog td = new TimeDog();
                                            //td.debug_print_Duration("-------------------------");
                                            KDSDataOrder order = doOrderXml(data.m_objSource, data.m_xmlData, data.m_originalFileName, data.m_bForceAcceptThisOrder, false);
                                            if (order != null)
                                            {
                                                if (order.getTransType() == KDSDataOrder.TRANSTYPE_ADD) {
                                                    bAddNew = true;
                                                    //syncOrderToWebDatabase(order, ActivationRequest.iOSOrderState.New, ActivationRequest.SyncDataFromOperation.New);
                                                }
                                                order = null;
                                            }
                                            //td.debug_print_Duration("Order duration:");
                                            break;
                                        case COMMAND_XML:
                                            //TimeDog td1 = new TimeDog();
                                            doCommandXml((KDSSocketInterface) data.m_objSource, data.m_xmlData);
                                            //td1.debug_print_Duration("command duration:");

                                            break;
                                        default:
                                            break;
                                    }
                                    arDone.add(data);
                                    //Thread.sleep(10);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            //remove finished xml
                            synchronized (m_lockerForOrdersThread) {
                                //arDoing.clear();
                                m_xmlDataBuffer.removeAll(arDone);
                            }
                            arDone.clear();
                            try {
                                if (bAddNew) {
                                    //TimeDog td2 = new TimeDog();
                                    getCurrentDB().clearExpiredBumpedOrders( getSettings().getBumpReservedCount());
                                    //td2.debug_print_Duration("others1:");
                                    schedule_process_update_to_be_prepare_qty(false);
                                    //td2.debug_print_Duration("others2:");
                                    //setFocusAfterReceiveOrder();
                                    setFocusAfterReceiveOrderThroughMessage();
                                    //td2.debug_print_Duration("others duration:");
                                }
                            }
                            catch ( Exception e)
                            {
                                e.printStackTrace();
                            }
                            refreshView();
                            try {
                                Thread.sleep(5);

                            } catch (Exception e) {
                            }

                        }
                    }
                    catch (Exception e)
                    {
                        KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
                    }
                }
            });
            m_threadOrdersXml.setName("DoXml");
            m_threadOrdersXml.setPriority(Thread.MAX_PRIORITY);
            m_threadOrdersXml.start();
        }
    }

    static class DoOrdersXmlThreadBuffer
    {
        Object m_objSource = null;
        String m_originalFileName = null;
        String m_xmlData = null;
        boolean m_bForceAcceptThisOrder = false;
        MESSAGE_TO_MAIN m_xmlType = null;//MESSAGE_TO_MAIN.REFRESH_A;
    }

    private boolean doCommandXmlInMainUI(KDSSocketInterface sock, String xmlData)
    {
//        String s = xmlData;
//        int nStart = s.indexOf("<Code>");
//        nStart += 6;
//
//        int nEnd = s.indexOf("</Code>");
//        //nEnd --;
//
//        s = s.substring(nStart, nEnd);
//        int n = KDSUtil.convertStringToInt(s, -1);
//        if (n <0 ) return true;
//
//        KDSXMLParserCommand.KDSCommand code = KDSXMLParserCommand.KDSCommand.values()[n];
//
        KDSXMLParserCommand.KDSCommand code = KDSXMLParser.quickGetCodeFromString(xmlData);

        switch (code)
        {
            case Station_Bump_Order:
            case Station_Add_New_Order: //Please notice: add_new, if local don't have this order, expo will add a new one.
            case Queue_Pickup: //for queue,
            case Queue_Unready:
            case Queue_Ready:
            case Expo_Bump_Order:
            case Expo_Bump_Item:
            case Expo_Unbump_Item:
            case Expo_Unbump_Order: //end for queue
            //case ACK_XML:
            case Prep_sync_to_queue://20190729, this can cause queue station freeze, so I move it to thread.
            case Prep_expo_transfer_order:
            case Sync_input_message_with_queue:
                return false;
            case ACK_XML:
            {
                doAck(xmlData);//do it as quick as possible.
                return true;
            }
            default: {
                doCommandXml(sock, xmlData);
                return true;
            }

        }


    }

    TimeDog m_clearDbTimeDog = new TimeDog();
    /**
     * Call it in mainactiviy checking thread.
     */
    public void checkRemovingStatisticExpiredData()
    {
        if (m_clearDbTimeDog.is_timeout(1800000)) //30x60x1000, 30mins
        //if (m_clearDbTimeDog.is_timeout(18000)) //30x60x1000, 30mins
        {
            remove_statistic_old_data();
            m_clearDbTimeDog.reset();
        }
    }


    /**
     * KPP1-7
     * @param stationID
     * @param ip
     */
    private void onMyQueueStationRestore(String stationID, String ip)
    {
        String s = "My queue station #" + stationID + " restored";
        showMessage(s);
        syncOrdersStatusWithQueue(null, stationID, ip);
    }

    /**
     * KPP1-7
     * @param sock
     * @param stationID
     * @param ip
     */
    private void syncOrdersStatusWithQueue(KDSSocketInterface sock,String stationID, String ip)
    {
        String orderItemsStatus = this.getUsers().getUserA().getOrders().outputBumpedItemsCountForSyncToQueue();
        if (this.isMultpleUsersMode())
            orderItemsStatus += this.getUsers().getUserB().getOrders().outputBumpedItemsCountForSyncToQueue();

        String strXml = KDSXMLParserCommand.createPrepSyncOrdersStatusToQueue(this.getStationID(), this.getLocalIpAddress(), "", orderItemsStatus);
        writeDataThroughSocket(sock, stationID, ip, strXml);

//        if (sock == null) {
//            writeStringToStation(stationID, ip, strXml);
//        }
//        else
//        {
//            if (sock instanceof KDSSocketTCPSideBase)
//            {
//                KDSSocketTCPSideBase tcp = (KDSSocketTCPSideBase)sock;
//                tcp.writeXmlTextCommand(strXml);
//            }
//        }


    }

    /**
     * for write data in command
     * @param stationID
     * @param ip
     * @param data
     */
    private void writeStringToStation(String stationID,String ip, String data)
    {
        KDSStationConnection conn = m_stationsConnection.findConnectionByID(stationID);
        if (conn != null) {
            if (conn.getSock().isConnected())
                conn.getSock().writeXmlTextCommand(data);
            else
                m_stationsConnection.getNoConnectionBuffer().add(stationID, data, NoConnectionDataBuffers.MAX_BACKUP_DATA_COUNT);
                //conn.addBufferedData(data);
        }
        else
        {
            KDSStationIP station = new KDSStationIP();
            station.setID(stationID);
            station.setIP(ip);
            station.setPort(m_settings.getString(KDSSettings.ID.KDS_Station_Port));

            m_stationsConnection.connectStationWithData(station, data);
        }
    }

    /**
     * 2.0.50,
     * KPP1-7
     * This station should been in queue mode.
     * Prep --> Queue.
     * rev.
     *  2.0.51
     * @param sock
     * @param command
     */
    private void onQueueReceivePrepSyncOrdersStatusData(KDSSocketInterface sock,  KDSXMLParserCommand command, String stationID, String ip)
    {
        String strOrdersItemsStatus = command.getParam(KDSConst.KDS_Str_Param, "");

        //retrieve all changed order information,
        //see format: guid,ordername,queue_ready,bumped_item_name ...( "-1" is all bumped).
        ArrayList<String> arChangedOrders = new ArrayList<>();//format: guid,ordername,queue_ready,bumped_item_name ...( "-1" is all bumped).

        //parse data.
        ArrayList<String> arOrdersWillBumped =  this.getUsers().getUserA().getOrders().queueSetOrderItemsBumped(strOrdersItemsStatus, arChangedOrders);
        this.getCurrentDB().queueSetOrderItemsBumped(arChangedOrders);//2.0.51
        int nMissedOrdersStartIndex  = -1;
        for (int i=0; i< arOrdersWillBumped.size(); i++)
        {
            String orderName = arOrdersWillBumped.get(i);
            if (orderName.equals("+"))
            {//some new orders in prep
                nMissedOrdersStartIndex = i;
                break;
            }
            else {
                //do remove bumped orders
                KDSStationExpeditor.exp_order_bumped_in_other_expo_station(this, this.getCurrentDB(), this.getUsers().getUserA().getOrders(), "", "", arOrdersWillBumped.get(i));
            }

        }

        //do add new orders
        ArrayList<String> arNewOrdersInPrep = arOrdersWillBumped;
        if (nMissedOrdersStartIndex !=-1)
        {
            for (int i=0; i<= nMissedOrdersStartIndex; i++)
            {
                arNewOrdersInPrep.remove(0);
            }
        }
        if (arNewOrdersInPrep.size() >0)
        {
            String s = "";
            for (int i=0; i< arNewOrdersInPrep.size(); i++)
            {
                if (i>0)
                    s += ",";
                s += arNewOrdersInPrep.get(i);
            }
            String strXmlCommand = KDSXMLParserCommand.createQueueAskSyncNewOrders(getStationID(),getLocalIpAddress(), "", s);
            writeDataThroughSocket(sock, stationID, ip, strXmlCommand);
        }

    }

    private void writeDataThroughSocket(KDSSocketInterface sock, String stationID, String ip, String strData)
    {
        if (sock == null) {
            writeStringToStation(stationID, ip, strData);
        }
        else
        {
            if (sock instanceof KDSSocketTCPSideBase)
            {
                KDSSocketTCPSideBase tcp = (KDSSocketTCPSideBase)sock;
                tcp.writeXmlTextCommand(strData);
            }
        }
    }

    private void onQueueAskSyncNewOrdersFromPrep(KDSSocketInterface sock,  KDSXMLParserCommand command, String stationID, String ip)
    {
        String strOrders = command.getParam(KDSConst.KDS_Str_Param, "");
        ArrayList<String> ar = KDSUtil.spliteString(strOrders, ",");
        for (int i=0; i< ar.size(); i++)
        {
            String orderName = ar.get(i);
            KDSDataOrder order = this.getUsers().getUserA().getOrders().getOrderByName(orderName);
            if (order == null) continue;
            String s = order.createXml();
            s = KDSXMLParserCommand.createPrepAckQueueSyncNewOrders(getStationID(), getLocalIpAddress(), "", s);
            writeDataThroughSocket(sock, stationID, ip, s);

        }
    }
    /**
     * change the order xml
     * As KDSDataOrder output xml is too slow,I change original xml directly.
     *
     * @param originalXml
     * @param removedItems
     * @return
     */
    private String manipulateOrderXml(String originalXml, ArrayList<KDSDataItem> removedItems)
    {
        for (int i=0; i< removedItems.size(); i++) {

            originalXml = removeItemFromOrderXml(originalXml, removedItems.get(i));
        }
        return originalXml;
    }
    private String removeItemFromOrderXml(String originalXml, KDSDataItem removedItem)
    {
        String strID = removedItem.getItemName();
        String itemStartTag = "<" + KDSXMLParserOrder.DBXML_ELEMENT_ITEM + ">";
        String itemEndTag =  "</" + KDSXMLParserOrder.DBXML_ELEMENT_ITEM + ">";
        String idStartTag =  "<" + KDSXMLParserOrder.DBXML_ELEMENT_ID + ">";
        String idEndTag =  "</" + KDSXMLParserOrder.DBXML_ELEMENT_ID + ">";


        int nItemStartIndex = 0;
        int nItemEndIndex = originalXml.length()-1;
        int nIDStartIndex = -1;
        int nIDEndIndex = -1;
        while ( (nItemStartIndex = originalXml.indexOf(itemStartTag,nItemStartIndex )) >=0)
        {
            nItemEndIndex = originalXml.indexOf(itemEndTag, nItemStartIndex);
            if (nItemEndIndex <0) break;
            nIDStartIndex = originalXml.indexOf(idStartTag, nItemStartIndex);
            nIDEndIndex = originalXml.indexOf(idEndTag, nItemStartIndex);
            if (nIDStartIndex > nItemStartIndex && nIDStartIndex < nItemEndIndex &&
                nIDEndIndex >nItemStartIndex && nIDEndIndex <nItemEndIndex)
            { //id is between item tag
                String id = originalXml.substring(nIDStartIndex + idStartTag.length(), nIDEndIndex);
                if (strID.equals(id))
                {
                    originalXml = removeString(originalXml, nItemStartIndex, nItemEndIndex + itemEndTag.length());
                    break;
                }
            }
            nItemStartIndex = nItemEndIndex;
        }
        return originalXml;
    }
    private String removeString(String original, int fromIndex, int toIndex)
    {
        String s = original.substring(0, fromIndex);
        String s1 = original.substring(toIndex);
        return s + s1;
    }

    public void setFocusAfterReceiveOrderThroughMessage()
    {

        m_refreshHandler.sendEmptyMessage(MESSAGE_TO_MAIN.Reset_Focus_after_new_order.ordinal());
//        setFocusAfterReceiveOrder(KDSUser.USER.USER_A);
//        if (this.isMultpleUsersMode())
//            setFocusAfterReceiveOrder(KDSUser.USER.USER_B);


    }

    private boolean doNotHandleThisCommand(KDSXMLParserCommand.KDSCommand code)
    {
        return false;
//
//        if (code == KDSXMLParserCommand.KDSCommand.Nothing)
//            return true;
//        if (code == KDSXMLParserCommand.KDSCommand.Station_Add_New_Order)
//        {
//            if (KDSConst.ENABLE_ADD_NEW_ANNOUNCE) return false; //handle it.
//            if (this.isQueueExpo() || this.isExpeditorStation() || this.isTrackerView())
//                return true;
//        }
//        return false;
    }

    /**
     * KPP1-41
     * @param order
     */
    public boolean syncOrderToWebDatabase(KDSDataOrder order, ActivationRequest.iOSOrderState iosState, ActivationRequest.SyncDataFromOperation fromOperation)
    {
        if (m_activationHTTP == null)
            return false;
        m_activationHTTP.setStationID(getStationID());
        m_activationHTTP.setStationFunc(getStationFunction());

        String name = m_stationsConnection.getRelations().getStationFunctionNameForBackoffice(getStationID());
        Activation.setStationFunctionNameInBackoffice(name);

        m_activationHTTP.postOrderRequest(order, iosState, fromOperation);

        KDSLog.order("", "Order ID:[" + order.getOrderName() + "] Sent to the Backoffice. State=" + iosState.toString() );

        return true;

    }

    public boolean syncItemBumpUnbumpToWebDatabase(KDSDataOrder order,KDSDataItem item, boolean bBumped)
    {
        if (order == null) return false;
        if (m_activationHTTP == null)
            return false;
        m_activationHTTP.setStationID(getStationID());
        m_activationHTTP.setStationFunc(getStationFunction());

        String name = m_stationsConnection.getRelations().getStationFunctionNameForBackoffice(getStationID());
        Activation.setStationFunctionNameInBackoffice(name);

        if (item == null) return false;
        Activation.ItemJobFromOperations opt = Activation.ItemJobFromOperations.Local_bump_item;
        if (!bBumped)
            opt = Activation.ItemJobFromOperations.Local_unbump_item;
        m_activationHTTP.postItemBumpRequest(getStationID(), order, item ,
                this.isExpeditorStation()||this.isRunnerStation()||this.isSummaryStation(),
                        bBumped,   opt);
        return true;

    }

    public void showToastMessage(String msg)
    {
        Message m = new Message();
        m.what = MESSAGE_TO_MAIN.Toast_msg.ordinal();
        m.obj = msg;
        m_refreshHandler.sendMessage(m);
    }

    /**
     * return ack from remote station
     * Format:
     *  the parameter just is the ackguid value.
     * @param fromStationID
     * @param command
     * @param xmlData
     */
    public void commandAckXml(String fromStationID, KDSXMLParserCommand command,String xmlData)
    {
        String ackguid = command.getParam();
        m_stationsConnection.onReceiveAckXml(fromStationID, ackguid);

    }

    public void onMyFunctionChanged(SettingsBase.StationFunc oldFunc, SettingsBase.StationFunc newFunc)
    {
        this.clearAll();
    }

    Thread m_threadACK = null;
    Vector<String> m_arReceivedAck = new Vector<>();
    Object m_ackLocker = new Object();

    final int MAX_DO_ACK_COUNT = 10;
    /**
     * Move some timer functions to here.
     * Just release main UI.
     * All feature in this thread are no ui drawing request.
     * And, in checkautobumping function, it use message to refresh UI.
     */
    private void startACKThread()
    {
        if (m_threadACK == null ||
                !m_threadACK.isAlive())
        {
            m_threadACK = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isThreadRunning())
                    {
                        try {
                            if (m_threadACK != Thread.currentThread())
                                return;
                           doACKInThread();
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {

                            }
                        }
                        catch ( Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            });
            m_threadACK.setName("RecACK");
            m_threadACK.start();
        }
    }

    private void doACKInThread()
    {
        int ncount = 0;
        synchronized (m_ackLocker)
        {
            ncount = m_arReceivedAck.size();
        }
        if (ncount <=0) return;

        if (ncount >MAX_DO_ACK_COUNT)
            ncount = MAX_DO_ACK_COUNT;

        Vector<String> ack = new Vector<>();

        for (int i=0; i< ncount; i++)
        {
            ack.add(m_arReceivedAck.get(i));

        }
        synchronized (m_ackLocker)
        {
            m_arReceivedAck.removeAll(ack);
        }

        for (int i=0; i< ncount ; i++)
        {
            String xmlData = ack.get(i);
            KDSXMLParserCommand command = (KDSXMLParserCommand)KDSXMLParser.parseXml(this.getStationID(), xmlData);
            if (command == null) continue;//different version cause command messed.
            KDSXMLParserCommand.KDSCommand code = command.getCode();
            String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");

            if (fromStationID.equals(this.getStationID()))
                continue; //don't do loop

            commandAckXml(fromStationID, command, xmlData);
        }

    }

    private void doAck(String ackXml)
    {
        synchronized (m_ackLocker)
        {
            m_arReceivedAck.add(ackXml);
        }
        startACKThread();
    }

    /**
     * Use this function to broadcast new relationships.
     * It called by preferencedfragmentstations.java nad mainactivity.java.
     *
     */
    static public void broadcastStationsRelations()
    {
        String s = KDSSettings.loadStationsRelationString(KDSApplication.getContext(), true);
        Object[] arParams = new Object[]{s};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String strData =(String) params[0];
                KDSGlobalVariables.getKDS().getBroadcaster().broadcastRelations(strData);
                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arParams);
    }

    /**
     * KDSCallback
     * @param receiver
     */
    public void call_setStationAnnounceEventsReceiver(StationAnnounceEvents receiver)
    {
        this.setStationAnnounceEventsReceiver(receiver);
    }
    public void call_broadcastRequireStationsUDP()
    {
        this.getBroadcaster().broadcastRequireStationsUDP();
    }
    public String call_getStationID()
    {
        return this.getStationID();
    }

    public void call_removeEventReceiver(KDSBase.KDSEvents receiver)
    {
        this.removeEventReceiver(receiver);
    }

    public void call_setEventReceiver(KDSBase.KDSEvents receiver)
    {
        this.setEventReceiver(receiver);
    }

    public int call_retrieveConfigFromStation(String stationID, TextView txtInfo)
    {
        return this.retrieveConfigFromStation(stationID, txtInfo);
    }

    public String call_getLocalMacAddress()
    {
        return this.getLocalMacAddress();
    }
    public String call_getBackupRouterPort()
    {
        return "";
    }

    public void call_broadcastShowStationID()
    {
        this.getBroadcaster().broadcastShowStationID();
    }
    public void call_udpAskRelations(String stationID)
    {
        this.udpAskRelations(stationID);
    }
    public void call_broadcastRelations(String relationsData)
    {
        this.getBroadcaster().broadcastRelations(relationsData);
    }
    public KDSStationActived call_findActivedStationByID(String stationID)
    {
        return this.getStationsConnections().findActivedStationByID(stationID);
    }
    public int call_findActivedStationCountByID(String stationID)
    {
        return this.getStationsConnections().findActivedStationCountByID(stationID);
    }
    public void call_broadcastStationsRelations()
    {
        broadcastStationsRelations();
    }

    public String call_loadStationsRelationString(boolean bNeedNoCheckOption)
    {
        return KDSSettings.loadStationsRelationString(KDSApplication.getContext(), bNeedNoCheckOption);
    }

    /**
     * //kpp1-62, kpp1-74
     * when expo receive item/order bumped, expo need update its preparation time in item_bumps table.
     * @param arChangedItems
     */
    private void syncWebBackofficeExpoItemBumpsPreparationTime(String orderGuid, ArrayList<KDSDataItem> arChangedItems, Activation.ItemJobFromOperations fromOperations)
    {
        if (arChangedItems.size() <=0) return;
        if (!this.isExpeditorStation() && (!this.isRunnerStation()) && (!this.isSummaryStation())) return;


        KDSDataOrder order = this.getUsers().getOrderByGUID(orderGuid);

        if (order == null) return ;
        if (m_activationHTTP == null)
            return ;
        m_activationHTTP.setStationID(getStationID());
        m_activationHTTP.setStationFunc(getStationFunction());
        String name = m_stationsConnection.getRelations().getStationFunctionNameForBackoffice(getStationID());
        Activation.setStationFunctionNameInBackoffice(name);


        for (int i=0; i< arChangedItems.size(); i++) {
            KDSDataItem item =arChangedItems.get(i);
            if (item != null)
                m_activationHTTP.postItemBumpRequest(this.getStationID(), order, item,true, item.getLocalBumped(), fromOperations);
        }
    }

    private void syncWebBackofficeExpoItemBumpsPreparationTime(String orderGuid, KDSDataItems items, Activation.ItemJobFromOperations fromOperations)
    {
        ArrayList<KDSDataItem> ar = new ArrayList<>();
        for (int i=0; i< items.getCount(); i++)
            ar.add(items.getItem(i));
        syncWebBackofficeExpoItemBumpsPreparationTime(orderGuid, ar, fromOperations);
    }

    public void loadAllActiveOrdersNoMatterUsers()
    {

        try {
            loadAllActiveOrdersInfo(true);
        }
        catch (Exception e)
        {
            //KDSLog.e(TAG, e.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e );
        }

    }

    /**
     * If move rush to front, call this function, and reset the first showing order.
     *
     * @param arOrdersAdded
     *  index 0: order add to user A
     *  index 1: order add to user B
     */
    private boolean checkRushOrderReceivedThenChangeFirstShowingOrder(ArrayList<KDSDataOrder> arOrdersAdded)
    {
        if (!getSettings().getBoolean(KDSSettings.ID.Orders_sort_rush_front))
            return false;

        if (arOrdersAdded.size()<=0) return false;

        for (int i=0; i< arOrdersAdded.size(); i++)
        {
            KDSDataOrder order = arOrdersAdded.get(i);
            if (order.getOrderType().toUpperCase().equals(KDSDataOrder.ORDER_TYPE_RUSH))
            {
                if (m_arKdsEventsReceiver != null)
                {
                    ArrayList<Object> arOrders = new ArrayList<>();
                    arOrders.addAll(arOrdersAdded);
                    for (int j = 0; j< m_arKdsEventsReceiver.size(); j++)
                    {
                        m_arKdsEventsReceiver.get(j).onKDSEvent(KDSEventType.Received_rush_order, arOrders);
                    }
                }
            }
        }
        return true;
    }

            /**
             * kpp1-312 Cannot receive orders on expo
             * @param nListenPort
             * @param errorMessage
             */
    private void fireTcpListenServerErrorEvent(int nListenPort, String errorMessage)
    {
        fireTcpListenServerErrorEvent(m_arKdsEventsReceiver,nListenPort,  errorMessage);

    }

    /**
     * It is for expo print "station" order.
     * If prep get order without router, and expo set printing order when received,
     *  we will call this function.
     *
     *  This function will compare received order and changed order.
     *  Check if they are same one.
     *  Same: don't print it.
     *  No-same, print it.
     * @param orderReceived
     * @param ordersChanged
     * @return
     */
    private boolean isSameChangedOrder(KDSDataOrder orderReceived, ArrayList<KDSDataOrder> ordersChanged)
    {
        if (orderReceived == null) return true;

        if (ordersChanged.size() ==1)
        {
            return (orderReceived.getItems().getCount() == orderReceived.getItems().getCount());
        }
        else
        {
            int n = 0;
            for (int i=0; i< ordersChanged.size() ; i++)
            {
                n += ordersChanged.get(i).getItems().getCount();
            }
            return (n == orderReceived.getItems().getCount());
        }
    }

    public void fireOrderBumpedInOther(String orderGuid)
    {
        ArrayList<Object> arOrders = new ArrayList<>();
        arOrders.add(orderGuid);
        for (int i = 0; i< m_arKdsEventsReceiver.size(); i++)
        {
            m_arKdsEventsReceiver.get(i).onKDSEvent(KDSEventType.Order_Bumped_By_Other_Expo_Or_Station, arOrders);
        }
    }

    public void clearAllBufferedOrders()
    {
        m_users.ordersClear();
    }

            /**
             * kpp1-299-1
             */
    public void clearRelationshipSettings()
    {

        this.getSettings().clearRelationshipData();
        this.updateSettings(m_context);
    }

    /**
     * kpp1-407
     * @param command
     *
     * @return
     */
    private boolean mirrorStationSyncWebDatabase(KDSXMLParserCommand.KDSCommand code ,
                                                 KDSXMLParserCommand command ,
                                                 KDSDataOrder existedOrder,
                                                 ArrayList<KDSDataItem> arChangedItem)
    {

        if (existedOrder == null)
            return false;

        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");

        if (this.getStationsConnections().getRelations().isMyMirrorStation(fromStationID) || //station is my mirror.
                this.getStationsConnections().getRelations().isMirrorOfStation(fromStationID, getStationID()) ) // I am station's mirror
        {

            switch (code) {
                case Station_Bump_Order:
                    syncOrderToWebDatabase(existedOrder, ActivationRequest.iOSOrderState.Done, ActivationRequest.SyncDataFromOperation.Bump);
                    break;
                case Station_Bump_Item:
                    for (int i=0; i< arChangedItem.size(); i++) {
                        if (arChangedItem.get(i) != null)
                            syncItemBumpUnbumpToWebDatabase(existedOrder, arChangedItem.get(i), true);
                    }
                    break;
                case Station_Unbump_Item:
                    for (int i=0; i< arChangedItem.size(); i++) {
                        if (arChangedItem.get(i) != null)
                            syncItemBumpUnbumpToWebDatabase(existedOrder, arChangedItem.get(i), false);
                    }
                    break;
            }
        }
        return true;
    }

    /**
     * kpp1-407
     * @param command
     * @return
     */
    private KDSDataOrder parseReceivedOrder(KDSXMLParserCommand command)
    {
        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strXml.isEmpty())
            return null;
//        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
//        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(getStationID(), strXml);

        return order;
    }


    /**
     * kpp1-363, just for router app.
     */
    private void setAppSocketID(KDSSocketInterface sock,String xmlData)
    {
        if (sock instanceof KDSSocketTCPSideBase)
        {
            KDSSocketTCPSideBase s =  (KDSSocketTCPSideBase)sock;
            String str = xmlData;
            str =  str.replace(KDSConst.APP_ID_START, "");
            str =  str.replace(KDSConst.APP_ID_END, "");
            s.setAppSocketID(str);
        }
    }

    public boolean isRunnerStation()
    {
        return (getStationFunction() == KDSSettings.StationFunc.Runner);
    }

    public boolean isSummaryStation()
    {
        return (getStationFunction() == KDSSettings.StationFunc.Summary);
    }

    /**
     *
     * @param kds
     * @param command
     * @param strOrinalData
     */
//    public void onRunnerChangedCategory2(KDS kds, KDSXMLParserCommand command, String strOrinalData)
//    {
//        String orderName = command.getParam("P0", "");
//        String category = command.getParam("P1", "");
//        KDSDataOrder order = this.getUsers().getOrderByName(orderName);
//        if (order == null) return; //kp-43 Prep stations crashing
//        String guid = order.getGUID();
//        this.getCurrentDB().smartRunnerCategoryAddShowingCategory(guid, category);
//        order.smart_get_sorts().runnerSetShowingCategory(this.getCurrentDB().smartCategoryGetShowingCategories(guid));
//
//        //set the focus the just showing category.
//        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
//        {
//            ArrayList<Object> ar = new ArrayList<>();
//            ar.add(guid);
//            m_arKdsEventsReceiver.get(i).onKDSEvent(KDSEventType.Runner_LineItems_Show_New_Category, ar);
//
//        }
//        this.refreshView();
//    }

//    public void onRunnerChangedCategory3(KDS kds, KDSXMLParserCommand command, String strOrinalData)
//    {
//        String orderName = command.getParam("P0", "");
//        String categories = command.getParam("P1", "");
//        ArrayList<String> arCategories = KDSUtil.spliteString(categories, "\n");
//
//        KDSDataOrder order = this.getUsers().getOrderByName(orderName);
//        if (order == null) return; //kp-43 Prep stations crashing
//        String guid = order.getGUID();
//        //this.getCurrentDB().smartRunnerCategoryAddShowingCategory(guid, category);
//        this.getCurrentDB().smartRunnerCategoryAddShowingCategories(guid,arCategories );
//        order.smart_get_sorts().runnerSetShowingCategory(this.getCurrentDB().smartCategoryGetShowingCategories(guid));
//
//        //set the focus the just showing category.
//        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
//        {
//            ArrayList<Object> ar = new ArrayList<>();
//            ar.add(guid);
//            m_arKdsEventsReceiver.get(i).onKDSEvent(KDSEventType.Runner_LineItems_Show_New_Category, ar);
//
//        }
//        this.refreshView();
//    }

    /**
     * Runner passed the last catdelay value to prep station
     * @param kds
     * @param command
     * @param strOrinalData
     */
    public void onRunnerChangedCategory(KDS kds, KDSXMLParserCommand command, String strOrinalData)
    {
        String orderName = command.getParam("P0", "");
        String categories = command.getParam("P1", "");
        String lastCatDelay = categories;

        //ArrayList<String> arCategories = KDSUtil.spliteString(categories, "\n");

        KDSDataOrder order = this.getUsers().getOrderByName(orderName);
        if (order == null) return; //kp-43 Prep stations crashing
        String guid = order.getGUID();
        //this.getCurrentDB().smartRunnerCategoryAddShowingCategory(guid, category);
        TimeDog td = new TimeDog(order.getStartTime());
        long ms = td.duration();
        this.getCurrentDB().runnerSetLastShowingCatDelay(guid, KDSUtil.convertStringToFloat(lastCatDelay,0), ms);// CategoryAddShowingCategories(guid,arCategories );
        order.smart_get_sorts().runnerSetLastShowingCatDelay(KDSUtil.convertStringToFloat(lastCatDelay,0), ms);

        //set the focus the just showing category.
        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
        {
            ArrayList<Object> ar = new ArrayList<>();
            ar.add(guid);
            m_arKdsEventsReceiver.get(i).onKDSEvent(KDSEventType.Runner_LineItems_Show_New_Category, ar);

        }
        this.refreshView();
    }
    /**
     * kpp1-447
     * @param command
     * @param code
     * @param xmlData
     */
    private void prepGetStationBumpedItemCommand(KDSXMLParserCommand command,KDSXMLParserCommand.KDSCommand code, String xmlData)
    {
        //kpp1-407, save my orginal order
        String strOrderName = command.getParam("P0", "");
        KDSDataOrder orderExisted = this.getUsers().getOrderByName(strOrderName);
        //
        ArrayList<KDSDataItem> arChangedItem = new ArrayList<>(); //retrieve changed items.
        String orderGuid = KDSStationFunc.doSyncCommandItemBumped(this,command, xmlData, arChangedItem);
        sortOrderForMoveFinishedToFront();
        checkSMS(orderGuid, false); //2.1.10
        if (arChangedItem.size() >0) //just expo save data to this array
        {//kpp1-62, kpp1-74
            syncWebBackofficeExpoItemBumpsPreparationTime(orderGuid, arChangedItem, Activation.ItemJobFromOperations.Expo_sync_prep_bump_item);
        }
        //kpp1-407
        mirrorStationSyncWebDatabase(code,command, orderExisted, arChangedItem);
        //kp-159
        getStationsConnections().writeToSummary(getStationID(), xmlData);

    }

    /**
     * kpp1-447
     * @param command
     * @param code
     * @param xmlData
     */
    private void prepGetStationUnbumpItemCommand(KDSXMLParserCommand command,KDSXMLParserCommand.KDSCommand code, String xmlData)
    {
        //kpp1-407, save my orginal order
        String orderName = command.getParam("P0", "");
        KDSDataOrder myOrder = this.getUsers().getOrderByName(orderName);


        //
        ArrayList<KDSDataItem> arUnbumpItems = new ArrayList<>();
        String orderGuid = KDSStationFunc.doSyncCommandItemUnbumped(this, command, xmlData, arUnbumpItems);
        sortOrderForMoveFinishedToFront();
        schedule_process_update_to_be_prepare_qty(true);

        checkSMS(orderGuid, false); //2.1.10
        //kpp1-407
        mirrorStationSyncWebDatabase(code, command, myOrder, arUnbumpItems);

        //kp-159
        getStationsConnections().writeToSummary(getStationID(), xmlData);

    }

    final int WORKLOAD_RESTORE_TIMEOUT = 5000;
    /**
     * if the workload station offline, and it don't restart in given seconds,
     *      move workload offline order to my station.
     */
    private void restoreWorkloadStationOfflineOrderBack()
    {
        if (this.getStationsConnections().getRelations().getWorkLoadStations().size()<=0)
            return;
        for (int i=0; i< this.getStationsConnections().getRelations().getWorkLoadStations().size(); i++) {
            KDSStationIP station = this.getStationsConnections().getRelations().getWorkLoadStations().get(i);
            AckDataStation ackData = this.getStationsConnections().popupTimeoutAck(station.getID(),1,  WORKLOAD_RESTORE_TIMEOUT);
            sendBackWorkloadStationOrders(ackData);

        }
    }

    /**
     * kp-96
     * @param data
     * @return
     */
    private boolean sendBackWorkloadStationOrders(AckDataStation data)
    {
        for (int i=0; i< data.getData().size(); i++)
        {
            String xml = data.getData().get(i).getXmlData();
            KDSXMLParser.XMLType ntype = checkXmlType(xml);

            switch (ntype)
            {
                case Unknown:
                    break;
                case Order: {
                    KDSDataOrder order = (KDSDataOrder) KDSXMLParser.parseXml(getStationID(), xml);
                    for (int j = 0; j < order.getItems().getCount(); j++) {
                        order.getItems().getItem(j).setToStationsString(getStationID());
                    }
                    xml = order.createXml();
                    doOrderXmlInThread(MESSAGE_TO_MAIN.Order, null, xml, "", false); //2.0.34
                }
                break;
                case Command:
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private void doPOSMessage(String xmlData)
    {
        KDSPOSMessage posmsg =(KDSPOSMessage) KDSXMLParser.parseXml("", xmlData);
        if (posmsg == null)
            return;
        mPOSMessages.doMessage(posmsg);
        m_refreshHandler.sendEmptyMessage(MESSAGE_TO_MAIN.Refresh_POS_messages.ordinal());
    }

    public KDSPOSMessages getPOSMessages()
    {
        return mPOSMessages;
    }

    /**
     * data saved in offline db, but there are connection existed.
     */
    public void checkDirtyOfflineData()
    {
        m_stationsConnection.checkDirtyOfflineData(this);
    }

    public void onRunnerStartCookManually(KDS kds, KDSXMLParserCommand command, String strOrinalData)
    {
        String orderName = command.getParam("P0", "");
        String itemNames = command.getParam("P1", "");
        ArrayList<String> arItemNames = KDSUtil.spliteString(itemNames, ",");
        if (arItemNames.size() <=0) return;

        KDSDataOrder order = this.getUsers().getOrderByName(orderName);
        if (order == null) return; //kp-43 Prep stations crashing
        String guid = order.getGUID();

        for (int i=0; i< arItemNames.size(); i++) {
            String itemName = arItemNames.get(i);
            if (order.getItems().getItemByName(itemName) == null)
                continue;

            PrepSorts.PrepItem smartItem = order.smart_get_sorts().findItem(itemName);
            if (smartItem!= null) {
                smartItem.ItemStartedManually = true;
                this.getCurrentDB().smart_set_item_started(guid, itemName, true, order.getStartTime());
            }
        }
        //set the focus the just showing category.
        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
        {
            ArrayList<Object> ar = new ArrayList<>();
            ar.add(guid);
            m_arKdsEventsReceiver.get(i).onKDSEvent(KDSEventType.Runner_LineItems_Show_New_Category, ar);

        }
        this.refreshView();
    }

    private boolean isExpoTypeStation()
    {
        return (KDSBase.isExpoTypeStation(getStationFunction()));
//        if (this.isQueueExpo() ||
//                this.isExpeditorStation()||
//                isRunnerStation() ||
//                isSummaryStation() )
//
//            return true;
//        return false;
    }

    /**
     * whether all items were hidden in this station.
     * The <HidenStation> </HidenStation> tag.
     *  If all items were hidden, remove this order.
     *
     * @param order
     * @return
     */
    private boolean isAllItemsHidden(KDSDataOrder order)
    {
        if (this.isExpoTypeStation()) return false;

        String stationID = this.getStationID();
        int ncounter = 0;
        for (int i=0; i< order.getItems().getCount(); i++)
        {
            KDSDataItem item = order.getItems().getItem(i);
            if (item.getToStations().findStation(stationID) != KDSToStations.PrimarySlaveStation.Unknown) {

                if (item.isHiddenStation(stationID))
                {
                    ncounter ++;
                }
            }
        }
        return (ncounter == order.getItems().getCount());

    }

    /**
     * Expo receive this command xml after my prep transfered order.
     * Remove all data in expo station.
     * @param kds
     * @param command
     * @param strOrinalData
     */
    private void onPrepOfExpoHasTransferOrder(KDS kds, KDSXMLParserCommand command, String strOrinalData, String fromStationID)
    {
//        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
//        if (strXml.isEmpty())
//            return;
//        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(kds.getStationID(), strXml);
//        KDSUser.USER userID = KDSUser.USER.values()[ order.getScreen()];
//        if (order == null)
//            return;
        KDSStationExpeditor.exp_sync_prep_transfer_order(kds, command, strOrinalData);
    }

    private void checkBackgroundImagesAutoSwitch()
    {
        if (!ScreenLogoDraw.m_logoFilesManager.getNextFileName().equals(ScreenLogoDraw.m_logoCurrentFileName))
        {
            refreshView();
        }
    }

    /**
     * KP-137
     * send the input message to its queue stations.
     * In queue, it has option to show this message.
     * KP-147. Sync data with the expo stations too.
     *
     * @param orderName
     * @param inputMessage
     */
    public void syncInputMessageWithQueue(String orderName, String inputMessage )
    {
        String strXml = KDSXMLParserCommand.createSyncInputMessageWithQueue(this.getStationID(), this.getLocalIpAddress(),
                "", orderName, inputMessage);
        this.getStationsConnections().writeToQueue(this.getStationID(), strXml);
        //KP-147 Input message-not shared between stations
        this.getStationsConnections().writeToExps(this.getStationID(), strXml);
    }

    /**
     *
     * After prep input message, it will send message to queue,
     * Queue station will enter this function.
     * rev.
     *  The expo station will get this command too.
     * @param kds
     * @param command
     * @param strOrinalData
     * @param fromStationID
     */
    private void onPrepSyncInputMessageWithQueue(KDS kds, KDSXMLParserCommand command, String strOrinalData, String fromStationID)
    {
        String orderName = command.getParam("P0", "");
        String inputMessage = command.getParam("P1", "");


        KDSDataOrder order = this.getUsers().getOrderByName(orderName);
        if (order == null) return;
        order.setInputMessage(inputMessage);
        this.getCurrentDB().orderSetInputMessage(order.getGUID(), inputMessage);

        this.refreshView();
    }
    Handler m_kdsEventsHandler =  new Handler()
    {
        public void handleMessage(Message msg) {
            if (msg.what ==1) {
                ArrayList<Object> ar = (ArrayList<Object>) msg.obj;
                int n = msg.arg1;
                KDSEventType evt = KDSEventType.values()[n];
                for (int i = 0; i < m_arKdsEventsReceiver.size(); i++) {

                    m_arKdsEventsReceiver.get(i).onKDSEvent(evt, ar);
                }
            }
        }
    };

//    private boolean isBinStation()
//    {
//        isExpoTypeStation()
//        if (this.getStationFunction() == SettingsBase.StationFunc.Summary)
//        {
//            int n = (this.getSettings().getInt(KDSSettings.ID.SumStn_mode));
//            KDSSettings.SumStationMode mode = KDSSettings.SumStationMode.values()[n];
//            if (mode == KDSSettings.SumStationMode.Bin)
//                return true;
//        }
//        return false;
//    }
    private void onSyncPrepOrderParked(KDS kds, KDSXMLParserCommand command, String strOrinalData, String fromStationID)
    {

        if (!isExpoTypeStation()) return;

        String orderName = command.getParam("P0", "");

        KDSDataOrder order = this.getUsers().getOrderByName(orderName);
        if (order == null) {
            return;
        }



        String orderGuid = order.getGUID();
        ArrayList<Object> arParams = new ArrayList<>();
        arParams.add(orderGuid);
        Message m = new Message();
        m.what = 1;
        m.arg1 = KDSEventType.Prep_park_order.ordinal();
        m.obj = arParams;
        m_kdsEventsHandler.sendMessage(m);



    }

    private void onSyncPrepOrderUnparked(KDS kds, KDSXMLParserCommand command, String strOrinalData, String fromStationID)
    {
        if (!isExpoTypeStation()) return;
        String orderName = command.getParam("P0", "");

        KDSDataOrder order = this.getUsers().getOrderByName(orderName);
        if (order == null) {
            order = this.getUsers().getUserA().getParkedOrders().getOrderByName(orderName);
            if (order == null)
            {
                if (this.isMultpleUsersMode())
                {
                    order = this.getUsers().getUserB().getParkedOrders().getOrderByName(orderName);
                }
            }
            if (order == null)
                return;
        }


        String orderGuid = order.getGUID();
        ArrayList<Object> arParams = new ArrayList<>();
        arParams.add(orderGuid);
        Message m = new Message();
        m.what = 1;
        m.arg1 = KDSEventType.Prep_unpark_order.ordinal();
        m.obj = arParams;
        m_kdsEventsHandler.sendMessage(m);
    }

    public void syncPrepOrderParked(String orderName )
    {
        String strXml = KDSXMLParserCommand.createSyncOrderParked(this.getStationID(), this.getLocalIpAddress(),
                "", orderName);
        this.getStationsConnections().writeToExps(this.getStationID(), strXml);

    }

    public void syncPrepOrderUnparked(String orderName )
    {
        String strXml = KDSXMLParserCommand.createSyncOrderUnparked(this.getStationID(), this.getLocalIpAddress(),
                "", orderName);
        this.getStationsConnections().writeToExps(this.getStationID(), strXml);

    }

}
