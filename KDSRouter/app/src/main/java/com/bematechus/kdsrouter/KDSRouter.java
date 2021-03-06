package com.bematechus.kdsrouter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.bematechus.kdslib.Activation;
import com.bematechus.kdslib.DebugInfo;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSBase;
import com.bematechus.kdslib.KDSBroadcastThread;
import com.bematechus.kdslib.KDSCallback;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDBBase;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataModifier;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSDataSumNames;
import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSLogOrderFile;
import com.bematechus.kdslib.KDSPOSMessage;
import com.bematechus.kdslib.KDSPosNotificationFactory;
import com.bematechus.kdslib.KDSSMBDataSource;
import com.bematechus.kdslib.KDSSmbFile;
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
import com.bematechus.kdslib.KDSTimer;
import com.bematechus.kdslib.KDSToStation;
import com.bematechus.kdslib.KDSToStations;
import com.bematechus.kdslib.KDSToast;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXML;
import com.bematechus.kdslib.KDSXMLParserCommand;
import com.bematechus.kdslib.KDSXMLParserOrder;
import com.bematechus.kdslib.NoConnectionDataBuffers;
import com.bematechus.kdslib.SettingsBase;
import com.bematechus.kdslib.StationAnnounceEvents;
import com.bematechus.kdslib.TimeDog;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class KDSRouter extends KDSBase implements KDSSocketEventReceiver,
        Runnable,
        KDSSMBDataSource.BufferStateChecker,
        KDSCallback,
        KDSTimer.KDSTimerInterface
{

    private final String TAG = "KDSRouter";
    private final int MAX_OFFLINE_ORDERS_COUNT = 200;
//    public interface KDSRouterEvents
//    {
//        void onStationConnected(String ip, KDSStationConnection conn);
//        void onStationDisconnected(String ip);
//        void onAcceptIP(String ip);
//        void onRetrieveNewConfigFromOtherStation();
//        void onShowMessage(String message);
//        void onAskOrderState(Object objSource, String orderName);
//        void onReceiveNewRelations();
//        void onReceiveRelationsDifferent();
//        void onShowStationStateMessage(String stationID, int nState);
//    }

//    public interface StationAnnounceEvents
//    {
//        void onReceivedStationAnnounce(KDSStationIP stationReceived);//String stationID, String ip, String port, String mac);
//    }

    /********************************************************************************************/

    int m_nPOSPort = KDSRouterSettings.DEFAULT_ROUTER_DATASOURCE_TCPIP_PORT;// 4000;

    int m_nRouterBackupPort = KDSRouterSettings.DEFAULT_ROUTER_BACKUP_TCP_PORT;// 4001;

    String m_primaryRouterID = "";//2015-12-29
    String m_slaveRouterID = "";//2015-12-29

    KDSDBRouter m_dbRouter = null;

    // broadcast udp message
    KDSSocketUDP m_udpStationAnnouncer = new KDSSocketUDP();

    //all socket checking
    KDSSocketManager m_socksManager = new KDSSocketManager();
    //listen which pos want to connect to me
    KDSSocketTCPListen m_listenPOS;
    //listen the router backup/primary, 2015-12-28
    KDSSocketTCPListen m_listenRouters;

    //use it receive event, and
    KDSSocketMessageHandler m_sockEventsMessageHandler  = new KDSSocketMessageHandler(this);; //socket events

    Context m_context = null; //application context

    Object m_locker = new Object();

    ArrayList<KDSEvents> m_arKdsEventsReceiver = new ArrayList<>();//null; //KDS events

//    StationAnnounceEvents m_stationAnnounceEvents = null;

    //2015-12-28
    KDSRouterStationsConnection m_stationsConnection = new KDSRouterStationsConnection(m_socksManager,m_sockEventsMessageHandler );

    KDSSocketTCPCommandBuffer m_udpBuffer = new KDSSocketTCPCommandBuffer();

    KDSRouterSettings m_settings = null; //this the root all others settings pointer

    //KDSSMBDataSource m_smbDataSource = new KDSSMBDataSource(m_sockEventsMessageHandler);
    KDSSMBDataSource m_smbDataSource = new KDSSMBDataSource(m_sockEventsMessageHandler, this);

    KDSState m_kdsState = new KDSState();

    String m_strLocalIP = "";
    String m_strLocalMAC = "";

    String m_strStationID = ""; //for backup of router

    Schedule m_schedule = new Schedule();

    RouterAcks m_acks = new RouterAcks(); //2.0.15

    KDSTimer m_timer = null;// new KDSTimer();

    public KDSStationsConnection getStationsConnections()
    {
        return m_stationsConnection;
    }

    public KDSDBRouter getRouterDB()
    {
        return m_dbRouter;
    }


    public KDSRouter()
    {
        KDSGlobalVariables.setKDSRouter(this);
    }


    /**
     *
     * @param context
     * application context
     */
    public KDSRouter(Context context)
    {
        KDSGlobalVariables.setKDSRouter(this);
        m_settings = new KDSRouterSettings(context);// settings;
        m_settings.loadSettings(context);
        m_context = context;
        //database
        m_dbRouter = KDSDBRouter.open(context);

        m_listenPOS = new KDSSocketTCPListen();
        m_listenPOS.setEventHandler(m_sockEventsMessageHandler);

        //listen router backup
        m_listenRouters = new KDSSocketTCPListen();
        m_listenRouters.setEventHandler(m_sockEventsMessageHandler);

        updateSettings(m_settings);

    }
    public KDSRouterSettings.ID checkKDSDlgKbdEvent(KeyEvent ev, KDSKbdRecorder kbd)
    {
        return KDSRouterSettings.ID.NULL;

    }
    public void updateSettings()
    {
        m_settings.loadSettings(m_context);


        updateSettings(m_settings);
    }


    public  void updateSettings(KDSRouterSettings settings)
    {
        KDSLog.setLogLevel(m_settings.getInt(KDSSettings.ID.Log_mode));
        KDSLogOrderFile.setEnabled(m_settings.getBoolean(KDSSettings.ID.Log_orders));

        String stationOldID = getStationID();// m_strKDSStationID;
        m_strStationID = settings.getString(KDSRouterSettings.ID.KDSRouter_ID);
        boolean bEnabled = settings.getBoolean(KDSRouterSettings.ID.KDSRouter_Enabled);
        //if (!m_strStationID.equals(stationOldID))
        //{
        m_stationsConnection.clearAllActiveAnnouncer(); //reset it.
        if (bEnabled) {
            broadcastStationAnnounceInThread();
            broadcastRequireStationsUDPInThread();
        }
        m_stationsConnection.getRelations().refreshRelations(m_context, m_strStationID);
        m_stationsConnection.closeAllStationsConnections();


        m_primaryRouterID = settings.getString(KDSRouterSettings.ID.KDSRouter_Primary_Router);
        boolean bBackupMode = settings.getBoolean(KDSRouterSettings.ID.KDSRouter_Backup);
        if (!bBackupMode)
            m_primaryRouterID = "";

        m_slaveRouterID = settings.getString(KDSRouterSettings.ID.KDSRouter_Slave_Router);

        //}
        int nPort = settings.getInt(KDSRouterSettings.ID.KDSRouter_Data_POS_IPPort);
        boolean bPosPortChanged = false;
        if (nPort != m_nPOSPort)
            bPosPortChanged = true;
        m_nPOSPort = nPort;


        //router backukp.
        nPort = settings.getInt(KDSRouterSettings.ID.KDSRouter_Backup_IPPort);
        boolean bBackupPortChanged = false;
        if (nPort != m_nRouterBackupPort)
            bBackupPortChanged = true;
        m_nRouterBackupPort = nPort;

        if (bPosPortChanged)
        {
            if (m_listenPOS.isListening())
            {
                m_listenPOS.stop();
                m_stationsConnection.disconnectPOSConnections();

            }
        }
        if (bEnabled)
            startPOSListener();

        //router backup
        if (bBackupPortChanged)
        {
            if (m_listenRouters.isListening())
            {

                m_listenRouters.stop();
                //disconnectStations(m_arConnectMeStations);
                m_stationsConnection.closeAllStationsConnections();//.disconnectAllStationsConnectedToMe();
                if (bEnabled) {
                   String error = m_listenRouters.startServer(m_nRouterBackupPort, m_socksManager, m_sockEventsMessageHandler);
                   fireTcpListenServerErrorEvent(m_nRouterBackupPort, error);

                }

            }
        }
        if (bEnabled)
            startRemoteFolderDataSource(settings);

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


    /**
     * check connection in a loop
     */
    public void on1sTimer()
    {

    }
    public void onTime()
    {
        checkNetworkState();
        if (m_schedule.isTimeToCheckEvent())
        {
            ArrayList<String> ar = m_schedule.checkEventToFire();
            scheduleFireEvent(ar);
        }
        checkRouterUnique();

        if (getSettings().getBoolean(KDSRouterSettings.ID.Order_ack)) {
            ArrayList<RouterAck> timeoutAck = m_acks.checkTimeoutAck();

            for (int i=0; i< timeoutAck.size(); i++)
            {
                sendRouterAck(timeoutAck.get(i));
            }
            timeoutAck.clear();
        }


    }
    private void scheduleFireEvent(ArrayList<String> ar)
    {
        if (ar.size() <=0) return;
        for (int i=0; i< ar.size(); i++)
        {
            doOrderXml(null, "",ar.get(i));
        }
    }

    boolean m_bNetworkActived = true;
    private void checkNetworkState()
    {
        if (KDSSocketManager.isNetworkActived(this.m_context)) {
          if (!m_bNetworkActived)
                onNetworkRestored();
            m_bNetworkActived = true;
            fireNetworkStateResult(true);
        }
        else{
            if (KDSSocketManager.m_nLostNetworkCount >4) {
                if (KDSBackofficeNotification.isHeartbeatLost()) {
                    if (m_bNetworkActived)
                        onNetworkLost();
                    m_bNetworkActived = false;
                }
            }
            fireNetworkStateResult(false);
        }
    }

    private void onNetworkRestored()
    {
        KDSLog.e(TAG, KDSLog._FUNCLINE_() + "enter");
        this.startNetwork();
        //KPP1-305, Remove license restriction from Router
        //let main ui know network restored.
        //I don't want to add new event function, just use unused one.
        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
            m_arKdsEventsReceiver.get(i).onTTBumpOrder("");
        KDSLog.e(TAG, KDSLog._FUNCLINE_() + "exit");
    }
    private void onNetworkLost()
    {
        KDSLog.e(TAG, KDSLog._FUNCLINE_() + "enter");
        this.stopNetwork();
        KDSLog.e(TAG, KDSLog._FUNCLINE_() + "exit");

    }

    public boolean startPOSListener()
    {


        int n  = m_settings.getInt(KDSRouterSettings.ID.KDSRouter_Data_Source);
        KDSRouterSettings.KDSDataSource srcType = KDSRouterSettings.KDSDataSource.values()[n];
        if (srcType == KDSRouterSettings.KDSDataSource.TCPIP)
        {
            stopPOSListener();
            String error = m_listenPOS.startServer(m_nPOSPort, m_socksManager, m_sockEventsMessageHandler);
            //kpp1-312 test
            //String error = m_listenPOS.startServer(80, m_socksManager, m_sockEventsMessageHandler);
            fireTcpListenServerErrorEvent(m_nPOSPort, error);
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
        KDSLog.e(TAG, KDSLog._FUNCLINE_() +"stopNetwork() enter");
        m_socksManager.stopThread();
        m_udpStationAnnouncer.close();
        stopPOSListener();;
        m_listenRouters.stop();

        m_stationsConnection.closeAllStationsConnections();
        m_stationsConnection.disconnectPOSConnections();
        stopPingThread();

        m_smbDataSource.stop();
        KDSLog.e(TAG, KDSLog._FUNCLINE_() +"stopNetwork() exit");
    }


    public void startNetwork()
    {
        KDSLog.e(TAG, KDSLog._FUNCLINE_() +"startNetwork() enter");
        m_bHasShowRouterUniqueError = false;
        refreshIPandMAC();

        m_socksManager.startThread();
        m_udpStationAnnouncer.start(KDSRouterSettings.UDP_ROUTER_ANNOUNCER_UDP_PORT, m_sockEventsMessageHandler, m_socksManager);

        //let others stations know me as soon as possible.
        this.broadcastStationAnnounceInThread();
        this.broadcastRequireStationsUDPInThread();

        startPOSListener();


        String error = m_listenRouters.startServer(m_nRouterBackupPort, m_socksManager, m_sockEventsMessageHandler);
        fireTcpListenServerErrorEvent(m_nRouterBackupPort, error);

        this.broadcastRequireStationsUDPInThread();


        m_stationsConnection.connectAllStations();

        startPingThread();
        this.broadcastRequireStationsUDPInThread();
        //if the datat source is SMB folder, start thread.
        if (m_primaryRouterID.isEmpty()) //don't set its primary router, just read files
            startRemoteFolderDataSource(m_settings);

        //let others stations know me as soon as possible.
        this.broadcastStationAnnounceInThread();

        broadcastAskRoutersInThread();
        KDSLog.e(TAG, KDSLog._FUNCLINE_() +"startNetwork() exit");
    }


    private void startRemoteFolderDataSource(KDSRouterSettings settings)
    {
        KDSLog.e(TAG, KDSLog._FUNCLINE_() + "enter");

        //if the datat source is SMB folder, start thread.
        KDSRouterSettings.KDSDataSource source =KDSRouterSettings.KDSDataSource.values ()[settings.getInt(KDSRouterSettings.ID.KDSRouter_Data_Source)];
        if (source == KDSRouterSettings.KDSDataSource.Folder)
        {
            String remoteFolder = settings.getString(KDSRouterSettings.ID.KDSRouter_Data_Folder);
            m_smbDataSource.setRemoteFolder(remoteFolder);
            //2.0.20
            boolean bEnableSmbV2 = settings.getBoolean(KDSRouterSettings.ID.Enable_smbv2);
            KDSSmbFile.smb_setEnableSmbV2(bEnableSmbV2);

            m_smbDataSource.start();
        }
        else
        {
            m_smbDataSource.stop();
        }
        KDSLog.e(TAG, KDSLog._FUNCLINE_() + "exit");
    }

    private void stopRemoteFolderDataSource()
    {
        m_smbDataSource.stop();
    }

    public KDSSMBDataSource getSmbDataSource()
    {
        return m_smbDataSource;
    }


    public void setContext(Context context)
    {
        m_context = context;
    }
    public boolean start()
    {
//        if (isRunning()) {
//            m_settings.loadSettings(m_context);
//            updateSettings(m_settings);
//            return true;
//        }
        KDSLog.d(TAG, "start enter");
        refreshIPandMAC();

        KDSGlobalVariables.setKDSRouter(this);

        m_settings.loadSettings(m_context);

        updateSettings(m_settings);

        //load all stations settings.
        checkStationsSettingChanged(m_context);
        startNetwork();
        refreshView();
        m_schedule.refresh();
        if (m_timer == null) {
            m_timer = new KDSTimer();
            m_timer.start(null, this, 1000);
        }

        KDSLog.d(TAG, "start exit");
        return true;

    }


    public void stop()
    {
        KDSLog.e(TAG, KDSLog._FUNCLINE_() + "enter");
        stopPingThread();
        synchronized (m_locker) {
            m_listenPOS.stop();
            //m_listenStations.stop();
            m_stationsConnection.closeAllStationsConnections();

            m_stationsConnection.disconnectPOSConnections();
            m_smbDataSource.stop();
//                m_printer.close();
        }
        if (m_errorToast != null)
            m_errorToast.cancel();
        //20160319
        stopNetwork();
        KDSLog.e(TAG, KDSLog._FUNCLINE_() + "exit");
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

        try {
            if (sock instanceof KDSSocketUDP)
                onUdpReceiveData(sock, remoteIP, buffer, nLength);
        }
        catch ( Exception e)
        {

        }

    }

    public void sockevent_onWroteDataDone(KDSSocketInterface sock,String remoteIP,  int nLength)
    {
        //20190531, comment it.
        //The SocketChannel-->write is not correct, its return value is not final data length.
        // I doubt socket save data to buffer. So,don't use "write" to identify station alive.
//        if (sock instanceof KDSSocketTCPSideClient)
//        {
//            KDSSocketTCPSideClient c = (KDSSocketTCPSideClient)sock;
//            int nport = getSettings().getInt(KDSRouterSettings.ID.KDSRouter_Connect_Station_IPPort);
//
//            KDSStationActived station =  m_stationsConnection.findActivedStationByIPAndPort(remoteIP, KDSUtil.convertIntToString(nport));
//            if (station != null) {
////                if (station.getID().equals("1"))
////                { //DEBUG
////                    Log.i(TAG, "Update plus !!!!!");
////                }
//                station.updatePulseTime();
//            }
//        }

        KDSLog.d(TAG, "Wrote data finished "+ remoteIP + " len="+ KDSUtil.convertIntToString(nLength));


    }

    public String getLocalIpAddress()
    {
        return m_strLocalIP;
    }
    public String getLocalMacAddress()
    {
        return m_strLocalMAC;
    }

    ByteBuffer m_bufferRouterBroadcast = ByteBuffer.allocate(100);
    /**
     * broadcast this router
     */
    public void broadcastStationAnnounce()
    {
        int port =  this.m_nRouterBackupPort; //this station opened this port for TCP/IP connection
        String strport = KDSUtil.convertIntToString(port);

        if (m_strStationID.isEmpty() || m_strLocalIP.isEmpty() || strport.isEmpty()) return;
        //int nlenght = KDSSocketTCPCommandBuffer.getReturnStationIPCommandLength(m_strStationID,m_strLocalIP, strport, getLocalMacAddress());
        m_bufferRouterBroadcast.clear();
//        if (nlenght != m_bufferRouterBroadcast.capacity())
//            m_bufferRouterBroadcast = ByteBuffer.allocate(nlenght);
        //ByteBuffer buf = KDSSocketTCPCommandBuffer.buildReturnStationIPCommand(m_strStationID,m_strLocalIP, strport, getLocalMacAddress(), m_bufferRouterBroadcast);
        KDSSocketTCPCommandBuffer.buildReturnStationIPCommand2(m_strStationID,m_strLocalIP, strport, getLocalMacAddress(), 0,0, Activation.getStoreGuid(),m_bufferRouterBroadcast); // TODO: Conflicts!!!
        m_udpStationAnnouncer.broadcastData(m_bufferRouterBroadcast);
    }

    /**
     * don't need this function.
     * The station announce is broadcast in thread.
     */
    public void broadcastStationAnnounceInThread()
    {

//        if (!BuildVer.isDebug()) {
//            AsyncTask task = new AsyncTask() {
//                @Override
//                protected Object doInBackground(Object[] params) {
//                    KDSRouter.this.broadcastStationAnnounce();
//                    return null;
//                }
//            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        }
    }

    public void broadcastRequireStationsUDPInThread()
    {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                KDSRouter.this.broadcastRequireStationsUDP();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }



    public ByteBuffer makeAnnounceToRouterBuffer()
    {
        int port = KDSSettings.UDP_ROUTER_ANNOUNCER_UDP_PORT;
        String strport = KDSUtil.convertIntToString(port);
        boolean bEnabled = getSettings().getBoolean(KDSRouterSettings.ID.KDSRouter_Enabled);
        boolean bBackupMode = getSettings().getBoolean(KDSRouterSettings.ID.KDSRouter_Backup);
        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildRouterStationAnnounceCommand(getStationID(), m_strLocalIP, strport, getLocalMacAddress(), bEnabled, bBackupMode, Activation.getStoreGuid());
        return buf;
    }

    public void broadcastRouterAnnounceInThread()
    {

        (new KDSBroadcastThread(m_udpStationAnnouncer,KDSSettings.UDP_ROUTER_ANNOUNCER_UDP_PORT, makeAnnounceToRouterBuffer())).start();

    }

    public void broadcastAskRoutersInThread()
    {
        ByteBuffer buf =  KDSSocketTCPCommandBuffer.buildAskRoutersCommand();
        (new KDSBroadcastThread(m_udpStationAnnouncer,KDSSettings.UDP_ROUTER_ANNOUNCER_UDP_PORT, buf)).start();

    }


    public void onUdpReceiveData(KDSSocketInterface sock,String remoteIP,  ByteBuffer buffer, int nLength) {
        //m_udpBuffer.appendData(buffer, nLength);
        m_udpBuffer.freeBuffer();
        m_udpBuffer.reset();
        m_udpBuffer.replaceBuffer(buffer, nLength);
        String remoteStationIP = KDSUtil.parseRemoteUDPIP(remoteIP);

        while (true) {
            m_udpBuffer.skip_to_STX();
            if (m_udpBuffer.length() <= 0)
                return;

            byte command = m_udpBuffer.command();
            if (command == 0)
                return;
            switch (command) {
                case KDSSocketTCPCommandBuffer.UDP_RET_STATION: {//the command send by xml format
                    //station announce arrived
                    //1. parse the xml text
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)
                        return; //need more data

                    //byte[] bytes = m_udpBuffer.station_info_command_data();
                    //m_udpBuffer.remove(ncommand_end);
                    //it don't check if data is my store condition. It will been check in following function.
                    //String utf8 = KDSUtil.convertUtf8BytesToString(bytes);
                    String utf8 = m_udpBuffer.station_info_string();
                    //m_udpBuffer.remove(ncommand_end);
                    onUdpReceiveStationAnnounce(utf8);
                    return; //the UDP just one package, just return, As use "replacebuffer".

                }
                //break;
                case KDSSocketTCPCommandBuffer.UDP_RET_ROUTER:
                { //the router announce arrived
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)
                        return; //need more data

                    //byte[] bytes = m_udpBuffer.router_info_command_data();
                    //m_udpBuffer.remove(ncommand_end);
                    //String utf8 = KDSUtil.convertUtf8BytesToString(bytes);
                    String utf8 = m_udpBuffer.station_info_string();
                    //m_udpBuffer.remove(ncommand_end);
                    onUdpReceiveRouterAnnounce(utf8);
                    return; //the UDP just one package, just return, As use "replacebuffer".
                }
                //break;
                case KDSSocketTCPCommandBuffer.UDP_REQ_STATION: {
                    int ncommand_end = m_udpBuffer.command_end();
                    m_udpBuffer.remove(ncommand_end);
                    if (!isMyStoreIP(remoteStationIP)) return;
                    broadcastStationAnnounce();

                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_SHOW_ID:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    m_udpBuffer.remove(ncommand_end);
                    if (!isMyStoreIP(remoteStationIP)) return;
                    onShowStationID();
                }
                break;
                case KDSSocketTCPCommandBuffer.XML_COMMAND:
                {//
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)
                        return; //need more data

                    byte[] bytes = m_udpBuffer.xml_command_data();
                    m_udpBuffer.remove(ncommand_end);
                    if (!isMyStoreIP(remoteStationIP)) return;
                    String utf8 = KDSUtil.convertUtf8BytesToString(bytes);
                    doUdpXmlCommand(utf8);
                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_ASK_RELATIONS:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    m_udpBuffer.remove(ncommand_end);
                    if (!isMyStoreIP(remoteStationIP)) return;
                    String str = remoteIP;
                    String port = "";
                    String ip = "";
                    str = str.replace("/", "");
                    int n = str.indexOf(":");
                    if (n >0) {
                        ip = str.substring(0, n);
                        port = str.substring(n+1);
                    }
                    onOtherAskRelations(ip, port);
                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_ASK_BROADCAST_RELATIONS:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    m_udpBuffer.remove(ncommand_end);
                    if (!isMyStoreIP(remoteStationIP)) return;
                    String strRelations = getSettings().loadStationsRelationString(m_context, true);

                    Object[] ar = new Object[]{strRelations};

                    AsyncTask task = new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] params) {
                            String str =(String) params[0];
                            broadcastRequireRelations(str);
                            return null;
                        }

                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ar);

                }
                break;
                    //don't check store guid here, it will been check in following function.

                case KDSSocketTCPCommandBuffer.UDP_ASK_ROUTER:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    if (ncommand_end == 0)
                        return; //need more data
                    m_udpBuffer.remove(ncommand_end);
                   // if (!isMyStoreIP(remoteStationIP)) return;

                    broadcastRouterAnnounceInThread();

                }
                break;
                case KDSSocketTCPCommandBuffer.UDP_CLEAR_DB:
                {
                    int ncommand_end = m_udpBuffer.command_end();
                    m_udpBuffer.remove(ncommand_end);
                    this.clearAll();
                }
                break;
                default: {
                    m_udpBuffer.remove(1);
                    break;
                }
            }
        }
    }

    public void onOtherAskRelations(String fromStationIP, String fromStationPort)
    {
        String strRelations = getSettings().loadStationsRelationString(m_context, true);
        // Object[] ar = new Object[]{s};
        String s = "<Relations>";
        s += strRelations;
        s += "</Relations>";
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

    boolean m_bRelationsDifferentErrorShown = false;
    /**
     *  UDP get xml command string
     * @param xmlCommand
     *  Format:
     *      <Relations>Strings .... </Relations>
     */
    public void doUdpXmlCommand(String xmlCommand)
    {
        if (xmlCommand.indexOf("<Relations>") >= 0)
        {
            String s = xmlCommand;
            s = s.replace("<Relations>", "");
            s = s.replace("</Relations>", "");
            if (s.isEmpty()) return;
            KDSRouterSettings.saveStationsRelation(m_context, s);
            this.updateSettings();//(m_context);
            for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
                m_arKdsEventsReceiver.get(i).onReceiveNewRelations();
        }
        else if (xmlCommand.indexOf("<RelationsRet>") >= 0)
        {
            if (SettingsBase.isNoCheckRelationWhenAppStart(KDSApplication.getContext()))
                return;
            String s = xmlCommand;
            s = s.replace("<RelationsRet>", "");
            s = s.replace("</RelationsRet>", "");
            //if (s.isEmpty()) return;
            ArrayList<KDSStationsRelation> ar =KDSRouterSettings.parseStationsRelations(s);
            SettingsBase.removeRelationNoCheckOptionStation(ar);
            if (isDifferentRelationsWithMyLocal(ar))
            {
                if (m_bRelationsDifferentErrorShown) return;
                m_bRelationsDifferentErrorShown = true;
                for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
                    m_arKdsEventsReceiver.get(i).onReceiveRelationsDifferent();

            }
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
        ArrayList<KDSStationsRelation> arLocal = KDSRouterSettings.loadStationsRelation(m_context, false);
        if (arReceived == null) return false;
        KDSRouterSettings.removeRelationNoCheckOptionStation(arReceived);

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

    /**
     * Active stations just contain my store stations.
     *
     * stationID, ip,port,mac,ordersCount,usermode, storeguid
     * @param strInfo
     */
    private void onUdpReceiveStationAnnounce(String strInfo)
    {
//        TimeDog d = new TimeDog();
//       // for (int i=0; i< 30; i++)
        doStationAnnounceInThread(strInfo);
//        d.debug_print_Duration("duration "+strInfo);


//
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
//        // *********************** IMPORTANT ************************
//        //As router will check router and kds two app, we have to use port to find it.
//        KDSStationActived station =m_stationsConnection.findActivedStationByMacAndPort(mac, port);//id); ///IMPORTANT
//        if (station == null) {
//            station = new KDSStationActived();
//            m_stationsConnection.addActiveStation(station);
//            bNewStation = true;
//        }
//        else
//        {//check if the connection existed,2.0.8
//            KDSStationConnection conn = m_stationsConnection.findConnectionByID(station.getID());
//            if (conn != null)
//            {
//                if (conn.isConnecting())
//                    bNewStation = true;
//            }
//            else
//            {
//                bNewStation = true;
//            }
//        }
//        station.setID(id);
//        station.setIP(ip);
//        station.setPort(port);
//        station.setMac(mac);
//
//        station.updatePulseTime();//record last received time
//
//        //some connection don't have the station ID in it. use this function to update them.
//        //comment it for debuging the connect with data function.
//        m_stationsConnection.refreshAllExistedConnectionInfo();
//
//        if (m_stationAnnounceEvents != null)
//            m_stationAnnounceEvents.onReceivedStationAnnounce(station);//id, ip, port, mac);
//        if (bNewStation ||
//                id.equals(m_primaryRouterID) ||
//                id.equals(m_slaveRouterID))
//            announce_restore_pulse(id, ip);
//
//       // d.debug_print_Duration("Deal with station announce duration");

    }

    /**
     * call this function in thread
     * @param strInfo
     */
    @Override
    protected void doStationAnnounce(String strInfo)
    {

        //remove all failed stations.
        m_stationsConnection.checkAllNoResponseStations();

        ArrayList<String> ar = KDSUtil.spliteString(strInfo, ",");
        if (ar.size() < 4) //In old version, this is a bug. Old code:if (ar.size() <= 4)
            return;
        String id = ar.get(0);
        String ip = ar.get(1);
        String port = ar.get(2);
        String mac = ar.get(3);
//        String itemsCount ="";
//        if (ar.size() >=5) //add orders count
//            itemsCount = ar.get(4);

        if (id.equals(getStationID()) && ip.equals(getLocalIpAddress()) )
            return; //it is myself

        boolean bNewStation = false;
        String userMode = "0";
        if (ar.size() >5)
            userMode = ar.get(5);
        String storeGuid = "";
        if (ar.size() >6)
            storeGuid = ar.get(6);
        if (!storeGuid.equals(Activation.getStoreGuid()))
            return; //it is not my store station

        // *********************** IMPORTANT ************************
        //As router will check router and kds two app, we have to use port to find it.
        KDSStationActived station =m_stationsConnection.findActivedStationByMacAndPort(mac, port);//id); ///IMPORTANT
        if (station == null) {
            station = new KDSStationActived();
            m_stationsConnection.addActiveStation(station);
            bNewStation = true;
            station.setID(id);
            station.setIP(ip);
            station.setPort(port);
            station.setMac(mac);
        }
        else
        {//check if the connection existed,2.0.8
            station.setID(id);
            station.setIP(ip);

            KDSStationConnection conn = m_stationsConnection.findConnectionByID(station.getID());
            if (conn != null)
            {
                if (conn.isConnecting())
                    bNewStation = true;
            }
            else
            {
                bNewStation = true;
            }
        }
//        station.setID(id);
//        station.setIP(ip);
//        station.setPort(port);
//        station.setMac(mac);
        station.setStoreGuid(storeGuid);
        //station.setStationContainItemsCount(itemsCount);
        station.setUserMode(KDSUtil.convertStringToInt(userMode, 0));

        station.updatePulseTime();//record last received time

        //some connection don't have the station ID in it. use this function to update them.
        //comment it for debuging the connect with data function.
        m_stationsConnection.refreshAllExistedConnectionInfo();
        //as this is in thread.
        if ( m_stationAnnounceEvents != null) {
            Message msg = new Message();
            msg.what = ANNOUNCE_MSG_SEND_EVENT;
            msg.obj = station;
            m_announceHander.sendMessage(msg);
        }
//        if (m_stationAnnounceEvents != null)
//            m_stationAnnounceEvents.onReceivedStationAnnounce(station);//id, ip, port, mac);
        if (bNewStation ||
                id.equals(m_primaryRouterID) ||
                id.equals(m_slaveRouterID)) {
            Message msg = new Message();
            msg.what = ANNOUNCE_MSG_STATION_RESTORE;
            msg.obj = station;
            m_announceHander.sendMessage(msg);
            //announce_restore_pulse(id, ip);

        }

    }

//    final int ANNOUNCE_MSG_SEND_EVENT = 0;
//    final int ANNOUNCE_MSG_STATION_RESTORE = 1;
//    Handler m_announceHander = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            KDSStationActived station = (KDSStationActived)msg.obj;
//            if (msg.what == ANNOUNCE_MSG_SEND_EVENT) {
//                if (m_stationAnnounceEvents != null)
//                    m_stationAnnounceEvents.onReceivedStationAnnounce(station);//id, ip, port, mac);
//            }
//            else if (msg.what == ANNOUNCE_MSG_STATION_RESTORE)
//            {
//                announce_restore_pulse(station.getID(),station.getIP());
//            }
//            return false;
//        }
//    });

//    Thread m_stationAnnounceThread = null;
//    StationAnnounceRunnable m_stationAnnounceRunnable = null;
//    private void doStationAnnounceInThread(String strInfo)
//    {
////        StationAnnounceRunnable r = new StationAnnounceRunnable(strInfo);
////        Thread t = new Thread(r);
////        t.start();
//
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
//        }
//
//    }

    /**
     *
     * @param strInfo
     *  Format:
     *      station_id, ip, port, mac, enabled, backup_mode,store_guid
     */
    private void onUdpReceiveRouterAnnounce(String strInfo)
    {
        boolean bNewStation = false;
        //remove all failed stations.
        m_stationsConnection.checkAllNoResponseStations();

        ArrayList<String> ar = KDSUtil.spliteString(strInfo, ",");
        if (ar.size() < 6)
            return;
        String id = ar.get(0);
        String ip = ar.get(1);
        String port = ar.get(2);
        String mac = ar.get(3);
        String strEnabled = ar.get(4);
        String strBackupMode = ar.get(5);
        if (ip.equals(getLocalIpAddress()))
            return; //don't care myself

        String storeGuid = "";
        if (ar.size() > 6)
            storeGuid = ar.get(6);
        if (!storeGuid.equals(Activation.getStoreGuid()))
            return; //it is not my store router.
        // *********************** IMPORTANT ************************
        //As router will check router and kds two app, we have to use port to find it.
        RouterStation router =m_stationsConnection.routerFind(id, ip);//id); ///IMPORTANT
        if (router == null) {
            router = new RouterStation();
            m_stationsConnection.routerAdd(router);
            bNewStation = true;
        }
        router.setID(id);
        router.setIP(ip);
        router.setPort(port);
        router.setMac(mac);
        router.setEnabled( (strEnabled.indexOf("1")>=0) );
        router.setBackupMode( (strBackupMode.indexOf("1")>=0) );
        router.updatePulseTime();//record last received time

    }

    /**
     * rev.:
     *  kpp1-363, router send a id to premium. That will tell station router is existed.
     * tcp client connect to server
     * @param sock
     */
    public void sockevent_onTCPConnected(KDSSocketInterface sock)
    {

        //if (m_eventReceiver != null) {
        //kpp1-363
        if (sock instanceof KDSSocketTCPSideClient)
        {
            KDSSocketTCPSideClient c = (KDSSocketTCPSideClient)sock;
            c.writeXmlTextCommand(buildAppSockIDCommandXml());
        }
        //
        String ip = getSocketIP(sock);
        KDSStationConnection conn = getSocketConnection(sock);
        //KDSSocketTCPSideClient c = (KDSSocketTCPSideClient)sock;
        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
            m_arKdsEventsReceiver.get(i).onStationConnected(ip, conn);
        //m_eventReceiver.onStationConnected(ip);
        m_stationsConnection.onIPConnected(this, ip);


        //Don't use this restoring, the KDSStationsConnection-->m_buffersForWaitingConnection do this.
//        KDSStationIP station = KDSStationIP.fromConnection(conn);// m_stationsConnection.findConnectionClientSideByIP(ip);
//        if (station == null) return;
//        restoreOfflineOrderXml(station);
        //}
    }

    private void restoreOfflineOrderXml(KDSStationIP station)
    {
        ArrayList<Integer> arIDs = new ArrayList<>();
        ArrayList<String> arOrdersXml = new ArrayList<>();

        int ncount = this.getRouterDB().getLostStationsXmlOrders(station.getID(), arIDs, arOrdersXml);
        if (ncount <=0) return;
        for (int i=0; i< ncount; i++)
        {
            m_stationsConnection.writeDataToStationOrItsBackup(station, arOrdersXml.get(i));
        }
        this.getRouterDB().deleteLostStationsXmlOrders(arIDs);
    }

    public KDSStationConnection getSocketConnection(KDSSocketInterface sock)
    {
        return m_stationsConnection.getConnection(sock);
    }
    /**
     * call back from  m_stationsConnection.onIPConnected
     */
    public void onFinishSendBufferedData(KDSStationDataBuffered data )
    {
        if (!data.getOrderGuid().isEmpty())
        { //it is for transfering order
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
        m_stationsConnection.onIPDisconnected(sock,ip);

        for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
            m_arKdsEventsReceiver.get(i).onStationDisconnected(ip);
    }

    public void sockevent_onUDPReceiveXml(KDSSocketInterface sock, String xmlData)
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
        if (stationID.equals(m_primaryRouterID)) //this primary router lost
        {
            this.onPrimaryBackupStationLost(stationID, stationIP);
        }
        else if (stationID.equals(m_slaveRouterID))
        {
            this.onMyBackupRouterLost(stationID, stationIP);
        }
        else
        {
            onStationLost(stationID, stationIP);
        }

    }



    /**
     * My station serve as backup of this stationID
     */
    public void onPrimaryBackupStationRestored(String stationID, String ip)
    {
        m_nPrimaryLostCounter  = 0;
        boolean bOldLost =m_kdsState.getPrimaryOfBackupLost();
        if (bOldLost) { //old state is existed
            String s = m_context.getString(R.string.primary_station_restored);// "My primary station #" + stationID + " restored";
            s = s.replace("#", "#" + stationID);
            showMessage(s);
        }
        m_kdsState.setPrimaryOfBackupLost(false);

        if (bOldLost)//primary lost, so I am working. Now, stop it.
            stopRemoteFolderDataSource();

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
        String s = m_context.getString(R.string.backup_station_restored);// "My backup station #" + stationID + " restored";
        s = s.replace("#", "#" + stationID);
        showMessage(s);
        askStationDatabaseStatus(stationID, ip);
    }

    /**
     * After the station found, connect it.
     * This is for the "bump notification" feature.
     * If we don't connect it, the notification can not send back.
     * @param stationID
     * @param ip
     */
    private void connectRestoredStation(String stationID, String ip)
    {
        //kpp1-387, don't check validation.
        if (!m_stationsConnection.getRelations().isValidNormalStationID(stationID)) {
            if (!m_stationsConnection.dataIsWaitingConnection(stationID)) //kpp1-387, just offline data valid, connect to station.
                return;
        }
        KDSStationConnection conn = m_stationsConnection.findConnectionByID(stationID);

        if (conn != null) {//2.0.8
            if (conn.isConnecting()) {
                int n = conn.isConnectingTimeout();
                if (n == 1 || n == 3) {
                    conn.getSock().close();
                    m_stationsConnection.getAllConnections().remove(conn);

                }
                else
                    return;

            }
            if (conn.isConnected()) {//reconnect it
                return;
//                conn.getSock().close();
//                m_stationsConnection.getAllConnections().remove(conn);
            }
        }
      //  if (conn == null) //make sure connect again.
        {
            KDSStationIP station = new KDSStationIP();
            station.setID(stationID);
            station.setIP(ip);
            station.setPort(m_settings.getString(KDSRouterSettings.ID.KDSRouter_Connect_Station_IPPort));

            m_stationsConnection.connectToStation(station);
            KDSLog.d(TAG, new DebugInfo() + "Connecting to #" + stationID+",ip="+ip);
        }
    }
    private void askStationDatabaseStatus(String stationID, String ip )
    {
        //check if the "support" database is empty first.
        String strXml = KDSRouterXMLParserCommand.createAskDBStatus(this.getStationID(), this.getLocalIpAddress(), "");
        KDSStationConnection conn = m_stationsConnection.findConnectionByID(stationID);
        if (conn != null) {
            if (conn.getSock().isConnected())
                conn.getSock().writeXmlTextCommand(strXml);
            else {
                m_stationsConnection.getNoConnectionBuffer().add(stationID, strXml, NoConnectionDataBuffers.MAX_BACKUP_DATA_COUNT);
                //conn.addBufferedData(strXml); //kdsstationsconnection-->m_buffersForWaitingConnection to save offline data
            }
        }
        else
        {
            KDSStationIP station = new KDSStationIP();
            station.setID(stationID);
            station.setIP(ip);
            station.setPort(m_settings.getString(KDSRouterSettings.ID.KDSRouter_Connect_Station_IPPort));

            m_stationsConnection.connectStationWithData(station, strXml);
        }
    }

    final int MAX_LOST_COUNT = 3;
    int m_nPrimaryLostCounter = 0;
    /**
     * The primary backup station lost.
     * Do following work:
     * copy backup station "support" data to "current", clear "support" data.
     */
    public void onPrimaryBackupStationLost(String stationID, String stationIP)
    {
        m_nPrimaryLostCounter ++;
        if (m_nPrimaryLostCounter <MAX_LOST_COUNT) return;
        m_kdsState.setPrimaryOfBackupLost(true);

        String s = getContext().getString(R.string.primary_router_lost);// "My primary station #" + stationID + " lost";
        s = s.replace("#", "#" + stationID);
        showMessage(s);

        startRemoteFolderDataSource(getSettings());

    }

    /**
     * 2015-12-30
     * @param stationID
     * @param stationIP
     */
    public void onStationLost(String stationID, String stationIP)
    {
        //2.0.8
//        String s =  getContext().getString(R.string.station_lost);// m_context.getString(R.string.station_lost);// "Station #" + stationID + " restored";
//        s = s.replace("#", "#" + stationID);
//        showMessage(s);

        int ncount = m_arKdsEventsReceiver.size();
        for (int i=0; i< ncount; i++)
        {
            m_arKdsEventsReceiver.get(i).onShowStationStateMessage(stationID, 0);
        }
    }

    /**
     * 2015-12-30
     * @param stationID
     * @param stationIP
     */
    public void onStationRestore(String stationID, String stationIP)
    {
        if (stationID.equals(getStationID())) return;
//        String s = m_context.getString(R.string.station_restored);// "Station #" + stationID + " restored";
//        s = s.replace("#", "#" + stationID);

        int ncount = m_arKdsEventsReceiver.size();
        for (int i=0; i< ncount; i++)
        {
            m_arKdsEventsReceiver.get(i).onShowStationStateMessage(stationID, 1);
        }
        //showMessage(s);
        //20160720, for bumping notification
        connectRestoredStation(stationID, stationIP);
    }

    //2015-12-30

    /**
     * If the backup router lost, just show a message to main activity
     * @param stationID
     * @param stationIP
     */
    public void onMyBackupRouterLost(String stationID, String stationIP)
    {
        String s =getContext().getString(R.string.backup_router_lost);// "My backup router #" + stationID + " lost";
        s = s.replace("#", stationID);
        showMessage(s);

    }

    /** 2015-12-30
     * The station/ip don't receive the udp announce in 10secs
     * @param stationID
     * @param stationIP
     */
    @Override
    public void announce_restore_pulse(String stationID, String stationIP)
    {

        if (stationID.equals(m_primaryRouterID) &&
                (!m_primaryRouterID.isEmpty()))
        {
            onPrimaryBackupStationRestored(stationID, stationIP);
        }
        else if (stationID.equals(m_slaveRouterID)&&
                (!m_slaveRouterID.isEmpty()))
        {
            onMyBackupStationRestore(stationID, stationIP);
        }
        else
        {
            onStationRestore(stationID, stationIP);
        }


    }

    public void smbevent_onSMBReceiveXml(KDSSMBDataSource smb,String smbFileName, String xmlData)
    {
        if (xmlData.indexOf(KDSSMBDataSource.TAG_SMBERROR_START)>=0)
        {
            doSmbError(xmlData);
        }
        else {
            doOrderXmlInThread(smb,smbFileName, xmlData, null);//
        }

    }

    public Toast m_errorToast = null;
    public void doSmbError(String xmlData)
    {
        String s = xmlData;
        s = s.replace(KDSSMBDataSource.TAG_SMBERROR_START,  "");
        s = s.replace(KDSSMBDataSource.TAG_SMBERROR_END,  "");

        if (m_errorToast == null)
            m_errorToast = Toast.makeText(  m_context, s, Toast.LENGTH_LONG);
        else
            m_errorToast.setText(s);
        m_errorToast.show();

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
        //do ack for xml
        xmlData = m_stationsConnection.responseAck(this.getStationID(), this.getLocalIpAddress(), this.getLocalMacAddress(),sock, xmlData);

        KDSXMLParser.XMLType ntype = checkXmlType(xmlData);

        switch (ntype)
        {
            case Unknown:
                return;
            case Order:
            case POS_Info:
                doOrderXmlInThread(sock, "",xmlData, null);
                break;

            case Command:

                    doCommandXml(sock, xmlData);
                break;
            case Feedback_OrderStatus:
            {

                doFeedbackOrderStatus(xmlData);
            }
            break;
            case Notification:
            {

                doNotificationXmlInThread(sock, xmlData);
            }
            break;
            case Acknowledgement:
            {

                doOrderAcknowledgement(sock, xmlData);
            }
            break;
//            case POS_Info:
//            {
//                doPOSMessage(xmlData);
//            }
//            break;
            default:
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
        if (sock == m_listenPOS) {
            KDSLog.d(TAG, "sockevent_onTCPAccept, listenPOS accept ");
            m_stationsConnection.onAcceptPOSConnection(sock, sockClient);
            String strPort = this.getSettings().getString(KDSRouterSettings.ID.KDSRouter_Data_POS_IPPort);
            c.setListenPort(KDSUtil.convertStringToInt(strPort,KDSRouterSettings.DEFAULT_ROUTER_DATASOURCE_TCPIP_PORT ));
            //bFireEvent = true;
        }
        else if (sock == m_listenRouters) {
            KDSLog.d(TAG,new DebugInfo()+ "sockevent_onTCPAccept, listenRouters accept ");
            m_stationsConnection.onAcceptRouterConnection(sock, sockClient);
            String strPort = this.getSettings().getString(KDSRouterSettings.ID.KDSRouter_Backup_IPPort);
            c.setListenPort(KDSUtil.convertStringToInt(strPort,KDSRouterSettings.DEFAULT_ROUTER_BACKUP_TCP_PORT ));
        }


        if (!ip.isEmpty()) {
            //if (!bFireEvent) return;
            for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
                m_arKdsEventsReceiver.get(i).onAcceptIP(ip);
            // m_eventReceiver.onAcceptIP(ip);
        }



    }

    /**
     * return the order status from station.
     * Format:
     *
     *  <Transaction>
     <Order>
     <ID>**<ID>              //Order ID
     <KitchenStation>1</KitchenStation>   //For request order status
     <KitchenStatus>Delivering to Counter</KitchenStatus> //for request order status

     </Order>
     </Transaction>
     *
     * write this string to all POS terminal
     * @param xmlData
     */
    public void doFeedbackOrderStatus(String xmlData)
    {
        int nsrcType = this.getSettings().getInt(KDSRouterSettings.ID.KDSRouter_Data_Source);
        KDSRouterSettings.KDSDataSource dataSource =  KDSRouterSettings.KDSDataSource.values()[nsrcType];
        switch (dataSource)
        {

            case TCPIP:
                writeToAllPOS(xmlData);
                break;
            case Folder:
                //2015-12-26
                String fileName = makeNewSmbFileNameForOrderStatus(xmlData);// m_kds.getStationID() + "_" + d.getOrderName() + "_" + strStatus+".xml";
                this.m_smbDataSource.uploadSmbFile(KDSConst.SMB_FOLDER_ORDER_INFO,fileName, xmlData );
                break;
        }
    }

    /** 2015-12-26
     *
     * @param xmlData
     *  The data from KDS station, it contains the order status information.
     * Format:
     *
     * <Transaction>
    <Order>
    <ID>**<ID>              //Order ID
    <KitchenStation>1</KitchenStation>   //For request order status
    <KitchenStatus>Delivering to Counter</KitchenStatus> //for request order status

    </Order>
    </Transaction>
     *
     * @return
     * The new file name,
     * kdsstation_OrderName_statusinfo.xml
     */
    private String makeNewSmbFileNameForOrderStatus(String xmlData)
    {
        KDSXML xml = new KDSXML();
        xml.loadString(xmlData);
        xml.back_to_root();
        if (!xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_ORDER))
            return "";
        String orderName = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_ID, "");
        String station = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_KDS_STATION, "");
        String status = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_FEEDBACK_ORDER_STATUS, "");
        return station+"_"+orderName+"_"+status+".xml";

    }
    public void checkStationsSettingChanged(Context context)
    {
        m_stationsConnection.refreshRelations(context, this.getStationID());

    }

    private KDSXMLParser.XMLType checkXmlType(String strxml)
    {
        return KDSXMLParser.checkXmlType(strxml);

    }


    /**
     * just send the command to all stations
     * @param objSource
     *  where we get this order xml data.
     *  It maybe KDSSocketInterface and KDSSMBDataSource
     *  We need it to write back the status xml file.
     * @param order
     */
    private void doAskOrderState(Object objSource, KDSDataOrder order, String xmlData)
    {
        this.writeToAllStations(xmlData);

    }

    /**
     * 2.0.13
     * @param objSource
     * @param xmlData
     */
    public void doOrderAcknowledgement(Object objSource, String xmlData)
    {
        KDSXML xml = new KDSXML();
        xml.loadString(xmlData);
        xml.back_to_root();
        String fileName = xml.getAttribute(NOTIFY_FILE_NAME, "");

        String fromStation = xml.getAttribute(KDSPosNotificationFactory.STATION, "");
        String errorCode =  xml.getAttribute(KDSPosNotificationFactory.ERROR, "");
        String orderID =  xml.getAttribute(KDSPosNotificationFactory.ORDER_ID, "");

        writeXmlToPOSOrderAck(xmlData,fileName);

        ArrayList<KDSStationIP> ar = m_stationsConnection.getAllActiveStations();
        if (getSettings().getBoolean(KDSRouterSettings.ID.Order_ack))
        {
            ArrayList<RouterAck> timeoutAck = m_acks.checkTimeoutAck();
            if (m_acks.receivedAckFromStation(fromStation, orderID, errorCode, ar))
            {
                RouterAck ack = m_acks.findAck(orderID);
                m_acks.getArray().remove(ack);
                sendRouterAck(ack);

            }
            for (int i=0; i< timeoutAck.size(); i++)
            {
                sendRouterAck(timeoutAck.get(i));
            }
            timeoutAck.clear();
        }

    }

    private void sendRouterAck(RouterAck ack)
    {
        RouterAck ackFileName = new RouterAck();
        String s = ack.createRouterAck(getStationID(), ackFileName);
        String routerAckFileName = ackFileName.getXmlText();
        writeXmlToPOSOrderAck(s,routerAckFileName);
    }

    private boolean writeXmlToPOSOrderAck( String strXml,String toFileName) {
        return writeXmlToPOSData(strXml, KDSConst.SMB_FOLDER_ACKNOWLEDGEMENT, toFileName);
    }
        /**
         * receive notification from stations
         * @param objSource
         * @param xmlData
         */
    public void doNotificationXml(Object objSource, String xmlData)
    {
        writePOSNotification(xmlData);
    }
    private boolean writeXmlToPOSNotification( String strXml,String toFileName)
    {
        return writeXmlToPOSData(KDSXML.formatXml(strXml),KDSConst.SMB_FOLDER_NOTIFICATION, toFileName );
        //return writeXmlToPOSData(strXml,KDSConst.SMB_FOLDER_NOTIFICATION, toFileName );

//        try {
//
//            KDSSettings.KDSDataSource source = KDSSettings.KDSDataSource.values()[getSettings().getInt(KDSSettings.ID.KDSRouter_Data_Source)];
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
//        catch (Exception e)
//        {
//            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
//            //KDSLog.e(TAG, KDSUtil.error( e));
//        }
//        return false;

    }

    private boolean writeXmlToPOSData( String strXml,String folderName, String toFileName)
    {
        try {

            KDSSettings.KDSDataSource source = KDSSettings.KDSDataSource.values()[getSettings().getInt(KDSSettings.ID.KDSRouter_Data_Source)];
            if (source == KDSSettings.KDSDataSource.Folder) {
                writeXmlToPOS(m_smbDataSource, strXml, folderName, toFileName);
            } else {
                int ncount = m_stationsConnection.getPosStations().size();
                for (int i = 0; i < ncount; i++) {
                    writeXmlToPOS(m_stationsConnection.getPosStations().get(i), strXml, folderName, toFileName);
                }
            }

            return true;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        return false;

    }

    private boolean writeXmlToPOS(Object objPOS, String strXml,String toFolderForSMB, String fileNameForSMB)
    {
        Object objSrc =objPOS;// d.getData();
        if (objSrc != null) {
            if (objSrc instanceof KDSSocketInterface) {
                if (objSrc instanceof KDSSocketTCPSideClient)
                    ((KDSSocketTCPSideClient) objSrc).writeXmlTextCommand(strXml);
                //((KDSSocketTCPSideClient)objSrc).write(strXml);

                if (objSrc instanceof KDSSocketTCPSideServer)
                    ((KDSSocketTCPSideServer) objSrc).writeXmlTextCommand(strXml);
                //((KDSSocketTCPSideServer)objSrc).write(strXml);
            } else if (objSrc instanceof KDSSMBDataSource) {
                String fileName = fileNameForSMB;//m_kds.getStationID() + "_" + d.getOrderName() + "_" + strStatus + ".xml";
                ((KDSSMBDataSource) objSrc).uploadSmbFile(toFolderForSMB, fileName, strXml);
            }
            else if (objSrc instanceof KDSStationConnection)
            {
                if (((KDSStationConnection)objSrc).getSock() instanceof  KDSSocketTCPSideClient)
                {
                    KDSSocketTCPSideBase sockBase = ((KDSStationConnection)objSrc).getSock();

                    ((KDSSocketTCPSideClient)sockBase).writeXmlTextCommand(strXml);
                }
                else if (((KDSStationConnection)objSrc).getSock() instanceof  KDSSocketTCPSideServer)
                {
                    KDSSocketTCPSideBase sockBase = ((KDSStationConnection)objSrc).getSock();
                    ((KDSSocketTCPSideServer)sockBase).writeXmlTextCommand(strXml);
                }
            }
            return true;
        }
        return false;
    }
    static final private String NOTIFY_FILE_NAME = "FileName";
    private boolean writePOSNotification(String xmlData)
    {
        String s = xmlData;
        int nindex = s.indexOf(NOTIFY_FILE_NAME);
        if (nindex <0) return false;
        int nStart = s.indexOf("\"", nindex);
        if (nStart < 0) return false;
        nStart ++;
        int nEnd = s.indexOf("\"", nStart);
        if (nEnd <0) return false;

       // nEnd --;
        if (nEnd<=nStart) return false;
        String fileName = s.substring(nStart, nEnd);

//        KDSXML xml = new KDSXML();
//        xml.loadString(xmlData);
//        xml.back_to_root();
//        String fileName = xml.getAttribute(NOTIFY_FILE_NAME, "");


        return writeXmlToPOSNotification(xmlData, fileName);

    }
    private boolean writePOSOrderAck(String xmlData)
    {

        KDSXML xml = new KDSXML();
        xml.loadString(xmlData);
        xml.back_to_root();
        String fileName = xml.getAttribute(NOTIFY_FILE_NAME, "");


        return writeXmlToPOSOrderAck(xmlData, fileName);

    }

    /**
     *
     * @param objSource
     *  where we get this order xml data.
     *  It maybe KDSSocketInterface and KDSSMBDataSource
     * @param xmlData
     */
    //private void doOrderXml(KDSSocketInterface sock, String xmlData)
    public void doOrderXml(Object objSource,String originalFileName, String xmlData)
    {
        KDSLogOrderFile.i(TAG,KDSLogOrderFile.formatOrderLog(xmlData));

        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(m_strStationID, xmlData);

        if (order == null) {
            if (this.getSettings().getBoolean(KDSSettings.ID.Order_ack))
            {//ack error ack
                if (objSource!=null)
                    doOrderAcknowledgement(objSource,order, 0, originalFileName,xmlData, KDSUtil.createNewGUID() , KDSPosNotificationFactory.ACK_ERR_BAD, false);
            }

            return;
        }
        else
        {//2.0.18
            if (this.getSettings().getBoolean(KDSSettings.ID.Order_ack))
            {//ack error ack
                KDSPosNotificationFactory.OrderParamError error = KDSPosNotificationFactory.checkOrderParameters(order);
                if (error != KDSPosNotificationFactory.OrderParamError.OK)
                {
                    String errorCode =  KDSPosNotificationFactory.getOrderParamErrorCode(error);
                    if (objSource!=null)
                        doOrderAcknowledgement(objSource,order, 0, originalFileName,xmlData, KDSUtil.createNewGUID() , errorCode, false);
                    return ;
                }
            }
        }
        KDSLog.d(TAG, "Receive order $" + order.getOrderName());


        if (order.getTransType() == KDSDataOrder.TRANSTYPE_ASK_STATUS) {

            doAskOrderState(objSource, order, xmlData);
        }
        else {
            //if (!KDSConst._DEBUG) //no heap size increasing issue, it should in doOrderFilter function
            doOrderFilter(order, xmlData, originalFileName);
        }
        String ip = "";
        String msg = "";//Get order #" + order.getOrderName();
        if (objSource == null)
        {
            msg = getContext().getString(R.string.get_order);
            //msg = msg.replace("#", order.getOrderName());
            //msg += (" from schedule" );
        }
        else if (objSource instanceof KDSSocketTCPSideClient)
        {
            ip =((KDSSocketTCPSideClient) objSource).getConnectToWhatIP();
            msg = getContext().getString(R.string.get_order_from_client_ip);
            //msg = msg.replace("#", "#"+order.getOrderName());
            //msg = msg.replace("$", "[" + ip + "]");

            //msg += (" from client ip=" + ip );
        }
        else if (objSource instanceof KDSSocketTCPSideServer)
        {
            ip =((KDSSocketTCPSideServer) objSource).getSavedRemoteIP();
            //msg += (" from server ip=" + ip + ", " + ((KDSSocketTCPSideServer) objSource).getListenPort());
            msg = getContext().getString(R.string.get_order_from_server_ip);

            //msg = msg.replace("#", "#" + order.getOrderName());
            //msg = msg.replace("$", "[" + ip + "]");
        }
        else if (objSource instanceof KDSSMBDataSource)
        {
            //msg += " from remote folder";
            msg = getContext().getString(R.string.get_order_from_remote_folder);
            //msg = msg.replace("#", "#" + order.getOrderName());

        }
        msg = buildOrderLog(msg, ip, order);
        showMessage(msg);
        KDSLog.d(TAG, KDSLog._FUNCLINE_() +  msg);
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
                commandAskDBStatus(sock, command, xmlData);
                break;
            case ROUTER_FEEDBACK_DB_STATUS:

                commandReturnRouterDbStatus(sock, command, xmlData);
                break;
            case ROUTER_DB_SQL:
                commandSqlRouterDB(command, xmlData);
                break;
            case ROUTER_ASK_DB_DATA:
                commandAskDbData(sock, command, xmlData);
            case Require_Station_Configuration:
                commandRequireConfigurations(sock, command, xmlData);
                break;
            case Broadcast_Station_Configuration:
                commandReceiveConfigurations(sock, command, xmlData);
                break;
            case ROUTER_SQL_SYNC:
                commandSyncSql(sock, command, xmlData);
                break;
            case ROUTER_UPDATE_CHANGES_FLAG:
                commandUpdateDBChangesGuid(command, xmlData);
                break;
            case ACK_XML:
                commandAckXml(fromStationID, command, xmlData);
                break;

            default:
                return;
        }
    }

    public void commandSyncSql(KDSSocketInterface sock, KDSXMLParserCommand command, String strOrinalData)
    {
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromStationIP = command.getParam(KDSConst.KDS_Str_IP, "");

        String sql = command.getParam("P0", "");
        String beforeChangesGuid = command.getParam("P1", "");
        String afterChangesGuid = command.getParam("P2", "");

        String localChangesGuid = this.getRouterDB().getChangesGUID();
        if (localChangesGuid.equals(beforeChangesGuid)) {
            this.getRouterDB().executeDML(sql);
            this.getRouterDB().setChangesGuid(afterChangesGuid);
        }
        else
        {
            this.copyRouterDatabaseFromOther(sock);
        }

    }

    public void commandUpdateDBChangesGuid(KDSXMLParserCommand command, String strOrinalData)
    {
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromStationIP = command.getParam(KDSConst.KDS_Str_IP, "");

        String guid = command.getParam("P0", "");
        this.getRouterDB().setChangesGuid(guid);
    }

    final int KB = 1024;
    /**
     * other router ask me to transfer my data to him
     * @param command
     * @param strOrinalData
     */
    public void commandAskDbData(KDSSocketInterface sock, KDSXMLParserCommand command, String strOrinalData)
    {
        copyRouterDatabaseToOther(sock);

    }
    /**
     * run this command in "current" database.
     * Sql format:
     *      insert ...; delete ...;
     * @param command
     * @param strOrinalData
     */
    public void commandSqlRouterDB(KDSXMLParserCommand command, String strOrinalData)
    {
        String sql = command.getParam(KDSConst.KDS_Str_Param, "");
        if (sql.isEmpty())
            return;
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        //kds.getCurrentDB().executeDML(sql);
        getRouterDB().exeSqlBatchString(sql);
        //exeSqlBatchString(kds.getCurrentDB(), sql);

    }

    /**
     * 2015-12-29
     * @param changesFlag
     * guid _ time
     * @return
     */
    private String getGuidFromDbChangesFlag(String changesFlag)
    {
        if (changesFlag.isEmpty()) return "";
        int index = changesFlag.indexOf("_");
        if (index<0) return changesFlag;
        return changesFlag.substring(0,index-1);
    }

    /**
     * 2015-12-29
     * @param changesFlag
     * guid _ time
     * @return
     */
    private long getTimeFromDbChangesFlag(String changesFlag)
    {
        if (changesFlag.isEmpty()) return 0;
        int index = changesFlag.indexOf("_");
        if (index<0) return 0;
        String s= changesFlag.substring(index+1);
        return KDSUtil.convertStringToLong(s, 0);
    }

    /**
     * most likely, this function was called in primary router.
     * Primary router check slave database, then it will decide transfer database or not.
     * @param sock
     * @param command
     * @param strOrinalData
     */
    public void commandReturnRouterDbStatus(KDSSocketInterface sock,KDSXMLParserCommand command, String strOrinalData)
    {
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromStationIP = command.getParam(KDSConst.KDS_Str_IP, "");

        String strFromRemoteRouter = command.getParam("P0", "0");
        String  strMyCurrent = getRouterDB().getDbChangesFlag();

        String strLocalGuidFlag = getGuidFromDbChangesFlag(strMyCurrent);
        long timeLocal = getTimeFromDbChangesFlag(strMyCurrent);

        String strRemoteGuidFlag = getGuidFromDbChangesFlag(strFromRemoteRouter);
        long timeRemote = getTimeFromDbChangesFlag(strFromRemoteRouter);


        if (strLocalGuidFlag.equals(strRemoteGuidFlag))
            return;
        if (timeLocal > timeRemote)
        {
            showMessage(getContext().getString(R.string.transfering_db_to_other_router));//"Transfering database to other router.");
            copyRouterDatabaseToOther(sock);
        }
        else {
            showMessage(getContext().getString(R.string.updating_db_from_other_router));//"Updating my database from other router.");
            copyRouterDatabaseFromOther(sock);
        }



    }

    public Context getContext()
    {
        if (m_context != null)
            return m_context;
        else
            return KDSApplication.getContext();
    }
    /**
     * copy remote database to me
     * 1. send command to thit station, let that station send sql to me.
     * 2. In doCommandXml function, deal with the received sql.
     * @param sock
     */
    public void copyRouterDatabaseFromOther(KDSSocketInterface sock)
    {

        if (sock == null) return;
        String xmlData = KDSRouterXMLParserCommand.createAskDatabaseData(getStationID(), getLocalIpAddress(), getLocalMacAddress());
        KDSSocketTCPSideBase tcp = (KDSSocketTCPSideBase)sock;
        tcp.writeXmlTextCommand(xmlData);


    }

    /**
     * while database changed, copy databaswe to backup station
     * call it in FilterActivity::onDesctroy function.
     */
    public void updateMyBackupDatabase()
    {
        if (m_slaveRouterID.isEmpty()) return;
        KDSStationConnection conn =  this.getStationsConnections().findConnectionByID(m_slaveRouterID);
        if (conn == null) return;
        if (!conn.isConnected()) return;
        copyRouterDatabaseToOther(conn.getSock());

    }

    private boolean m_bCopyingDBToOther = false;
    public void setCopyingDBToOther(boolean bCopying)
    {
        m_bCopyingDBToOther = bCopying;
    }
    //2015-12-30

    /**
     * copy database to this station.
     * Use the thread.
     * @param sock
     */
    public void copyRouterDatabaseToOther(KDSSocketInterface sock)
    {
        if (m_bCopyingDBToOther) return; //don't do it twice
        setCopyingDBToOther(true);
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                KDSRouter router = (KDSRouter)objects[0];
                try {

                    ArrayList<String> sqls = router.getRouterDB().outputDatabaseSqlStrings();
                    int ncount  =sqls.size();
                    String strOutput = "";
                    for (int i=0; i< ncount; i++) {
                        strOutput += sqls.get(i);
                        strOutput += KDSDBBase.SQL_SEPARATOR;// "\n";
                        if (strOutput.length() >KB ||
                                i >=(ncount-1))
                        {
                            String xmlData = KDSRouterXMLParserCommand.createSqlRouterDB(getStationID(), getLocalIpAddress(), getLocalMacAddress()
                                    , strOutput);
                            KDSSocketTCPSideBase tcp = (KDSSocketTCPSideBase) objects[1];
                            tcp.writeXmlTextCommand(xmlData);
                        }
                    }
                    //update its changes guid, make sure it is same as mine
                    String changesGuid = router.getRouterDB().getChangesGUID();
                    String xmlData = KDSRouterXMLParserCommand.createUpdateRouterChangesGUID(getStationID(), getLocalIpAddress(), getLocalMacAddress()
                            , changesGuid);
                    KDSSocketTCPSideBase tcp = (KDSSocketTCPSideBase) objects[1];
                    tcp.writeXmlTextCommand(xmlData);
                    router.setCopyingDBToOther(false);

                } catch (Exception ex) {
                    router.setCopyingDBToOther(false);
                    KDSLog.e(TAG,KDSLog._FUNCLINE_(),ex);// + ex.toString());
                    //KDSLog.e(TAG, KDSUtil.error( ex));
                }

                return null;

            }
        };
        Object[] objs = new Object[]{this, sock};
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, objs);


    }
    /**
     * remote station ask my current and support database status, for restoreing.
     * return support database active orders count and current database active orders count
     * @param command
     * @param strOrinalData
     */
    public void commandAskDBStatus(KDSSocketInterface sock,KDSXMLParserCommand command, String strOrinalData)
    {
        if (sock == null) return;
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");

        String strFlag = getRouterDB().getDbChangesFlag();


        String strXml = KDSRouterXMLParserCommand.createRouterDBStatusNotification(getStationID(), getLocalIpAddress(),
                getLocalMacAddress(), strFlag);
        KDSSocketTCPSideBase tcp = (KDSSocketTCPSideBase)sock;
        tcp.writeXmlTextCommand(strXml);


    }

    public void scheduleRefresh()
    {
        m_schedule.refresh();
    }

    /**
     * get all ExpItems and build a new order
     * @param order
     * @return
     */
    public KDSDataOrder buildExpItemsOrderAndRemoveExpItemFromOrignal(KDSDataOrder order)
    {
//        if (!orderHaveExpItems(order))
//            return null;
        KDSDataOrder orderExp = new KDSDataOrder();
        order.copyOrderInfoTo(orderExp);//
        ArrayList<KDSDataItem> ar = new ArrayList<>();

        int ncount = order.getItems().getCount();
        for (int i= 0 ; i< ncount; i++) {
            KDSDataItem item = order.getItems().getItem(i);
            if (item.getItemType() == KDSDataItem.ITEM_TYPE.Exp)
            {
                KDSDataItem expitem = new KDSDataItem();
                item.copyTo(expitem);
                orderExp.getItems().addComponent(expitem);
                ar.add(item);
            }

        }
        //remove the exp items from orignal order
        for (int i=0; i< ar.size(); i++)
        {
            order.getItems().removeComponent(ar.get(i));
        }
        return orderExp;
    }

    public boolean orderHaveExpItems(KDSDataOrder order)
    {
        int ncount = order.getItems().getCount();
        for (int i= 0 ; i< ncount; i++) {
            KDSDataItem item = order.getItems().getItem(i);
            if (item.getItemType() == KDSDataItem.ITEM_TYPE.Exp)
                return true;
        }
        return false;
    }

    /**
     * check if this order is for this station
     * @param order
     */
    public void  doOrderFilter(KDSDataOrder order,String xmlData, String originalFileName)
    {

        //if there are item for expeditor station, pass them
        if (orderHaveExpItems(order))
        {
            KDSLog.d(TAG, "order contains expitem, send them to expeditors");
            //The orignal order was changed too.
            KDSDataOrder orderExp = buildExpItemsOrderAndRemoveExpItemFromOrignal(order);
            if (orderExp != null)
            {
                String xmlExp = orderExp.createXml();
                checkExpLostAndSaveToOffline(xmlExp);
                writeToAllExp(xmlExp);

            }
        }

        assignMediaToOrder(order, xmlData);
        assignSummaryTranslateToOrder(order, xmlData);
        assignModifierPreparationTime(order, xmlData);
        assignSmartTimeToOrder(order, xmlData);
        assignItemsPrintable(order, xmlData);

        boolean bAllAssignedStation = false;
        if (isAllItemsAssignedStation(order))
        {
            bAllAssignedStation = true;
            //String toStationWithScreen = this.getRouterDB().itemGetToStationWithScreen(category, description,strDefaultStationID);// m_strDefaultToStation);
            //assignSmartTimeToOrder(order, xmlData);
            //checkAssignStationsActive(order, xmlData);
            //String xmlOrder = rebuildOrderXml(order, xmlData);
            //checkLostStationsAndSaveToOffline(order, xmlOrder);
            //writeToAllStations(xmlOrder);
            //writeToAllStations(xmlData);
        }
        else {
            assignStationsToOrder(order, xmlData);
            //2015-12-26
            //assignSmartTimeToOrder(order, xmlData);
            //checkAssignStationsActive(order, xmlData);
            //String xmlOrder = rebuildOrderXml(order, xmlData);
            //checkLostStationsAndSaveToOffline(order, xmlOrder);
            //writeToAllStations(xmlOrder);
        }
        checkAssignStationsActive(order, xmlData);

        assignColorToOrder(order, xmlData); //2.1.15.4

        String xmlOrder = rebuildOrderXml(order, xmlData);
        //don't use this for restoring, KDSStationsConnection-->m_buffersForWaitingConnection do this work.
        //checkLostStationsAndSaveToOffline(order, xmlOrder);

        if (getSettings().getBoolean(KDSRouterSettings.ID.Order_ack))
        {//This ack is the notification in ack folder. Router need to wait all stations return notification, then notify user.
            RouterAck ack = m_acks.add(order, xmlData, originalFileName); //just wait given stations's ack notification.
            ArrayList<KDSToStation> arToStations = getAllAcceptAnyItemsStations(); ////kpp1-279, expo need to receive data too, but if it is not existed in order file,router don't need to wait notification
            arToStations.addAll(ack.getTargetStations());
            //ArrayList<String> ar = ack.getSendToStations();
            ArrayList<String> ar =  RouterAck.getSendToStations(arToStations);
            writeToAllStations(xmlOrder, ar);

        }
        else {
            //log
            String info = "";
            if (order.getTransType() == KDSDataOrder.TRANSTYPE_DELETE)
                info = "Delete";
            else if (order.getTransType() == KDSDataOrder.TRANSTYPE_MODIFY)
                info = "Modify";
            else if (order.getTransType() == KDSDataOrder.TRANSTYPE_UPDATE_ORDER)
                info = "Update";
            else
                info = "New";
            KDSLog.order("", "Order ID ["+order.getOrderName() + "] " + info);

            if (order.getTransType() == KDSDataOrder.TRANSTYPE_DELETE ||  //KPP1-152
                    ( (order.getTransType() == KDSDataOrder.TRANSTYPE_MODIFY) && (!bAllAssignedStation)) ||
                    order.getTransType() == KDSDataOrder.TRANSTYPE_UPDATE_ORDER) {

                writeToAllStationsWithOrderLog(xmlOrder, order.getOrderName());

            }
            else {
                ArrayList<KDSToStation> ar = KDSDataOrder.getOrderTargetStations(order);
                ar.addAll(getAllAcceptAnyItemsStations(ar));
                ArrayList<String> arToStations = RouterAck.getSendToStations(ar);//it will remove repeated stations.
                //send order xml data to necessary stations, for 24 stations lost certain prep issue
                //if (!KDSConst._DEBUG) //heap size issue in this function
                //writeToAllStations(xmlOrder, arToStations);
                writeToAllStationsWithOrderLog(xmlOrder,order.getOrderName(), arToStations);
                //writeToAllStations(xmlOrder);
            }
        }


    }

    private  void checkExpLostAndSaveToOffline(String orderXml)
    {
        int ncount = m_stationsConnection.getRelations().getRelationsSettings().size();
        for (int i=0; i< ncount; i++) {
            KDSStationsRelation stationRelation = m_stationsConnection.getRelations().getRelationsSettings().get(i);
            if (stationRelation.getFunction() != KDSRouterSettings.StationFunc.Expeditor &&
                    stationRelation.getFunction() != KDSRouterSettings.StationFunc.Queue_Expo &&
                    stationRelation.getFunction() != KDSRouterSettings.StationFunc.Runner &&
                    stationRelation.getFunction() != KDSRouterSettings.StationFunc.Summary
            )
                continue;
            String stationID = stationRelation.getID();
            if (!isActivedStation(stationID)) {
                this.getRouterDB().keepMaxLostStationisXmlOrders(MAX_OFFLINE_ORDERS_COUNT);
                this.getRouterDB().addLostStationXmlOrder(stationID, orderXml);
            }
        }

    }
    /**
     * if the given station is offline, save to offlineorders table.
     * @param order
     *  the order
     * @param orderXml
     *  The changed (add some parameters) xml data, save this to table.
     */
    public void checkLostStationsAndSaveToOffline(KDSDataOrder order, String orderXml)
    {
        ArrayList<String> savedStations = new ArrayList<>();
        int ncount = order.getItems().getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item =  order.getItems().getItem(i);
            KDSToStations toStations = item.getToStations();

            checkStationOfflineWithOrderXml(toStations, orderXml,savedStations);
        }
    }
    public boolean checkStationOfflineWithOrderXml(KDSToStations toStations, String orderXml, ArrayList<String> savedStations)
    {
        int ncount = toStations.getCount();
        //ArrayList<String> savedStations = new ArrayList<>();

        for (int i=0; i< ncount ; i++)
        {
            String primary = toStations.getToStation(i).getPrimaryStation();
            if (isActivedStation(primary))
                continue;
            //primary is inactive.
            String slave =  toStations.getToStation(i).getSlaveStation();
            if (isActivedStation(slave))
                continue;
            if (KDSUtil.isExistedInArray(savedStations, primary))
                continue;
            this.getRouterDB().keepMaxLostStationisXmlOrders(MAX_OFFLINE_ORDERS_COUNT);
            this.getRouterDB().addLostStationXmlOrder(primary, orderXml);
            savedStations.add(primary);

        }
        return true;
    }

    public void assignMediaToOrder(KDSDataOrder order, String xmlData)
    {
        int ncount = order.getItems().getCount();
        ArrayList<String> arMedia = new ArrayList<>();

        for (int i=ncount-1; i>=0; i--)
        {
            KDSDataItem item = order.getItems().getItem(i);
            if ( (!item.getTrainingVideo().toCSV().isEmpty()) &&
                    (!item.getBuildCard().toCSV().isEmpty()) )
                continue;

            String description = item.getDescription();
            String category = item.getCategory();
            arMedia.clear();
            int n = this.getRouterDB().itemGetBuildCardsTrainingVideo(category, description, arMedia);
            if (n == 0) continue;
            if (item.getBuildCard().toCSV().isEmpty())
                item.setBuildCard(arMedia.get(0));
            if (item.getTrainingVideo().toCSV().isEmpty())
                item.setTrainingVideo(arMedia.get(1));
        }

    }

    /**
     * set the summary translate setting to order.
     * <Item>
         <SumTrans enabled="0" >Beef medium(0.00)
         Water medium(0.00)
         user /n as seperator, (0.00) is the qty
         </SumTrans>
     * </Item>
     * @param order
     * @param xmlData
     */
    public void assignSummaryTranslateToOrder(KDSDataOrder order, String xmlData)
    {
        int ncount = order.getItems().getCount();

        for (int i=ncount-1; i>=0; i--)
        {
            KDSDataItem item = order.getItems().getItem(i);

            String description = item.getDescription();
            String category = item.getCategory();
            boolean bSumNameEnabled = this.getRouterDB().sumnameGetEnabled(category, description);

            KDSDataSumNames sumNames = this.getRouterDB().sumnamesGet(category, description);

            item.setSumNamesEnabled(bSumNameEnabled);
            item.setSumNames(sumNames);

        }

    }

    public void assignModifierPreparationTime(KDSDataOrder order, String xmlData)
    {
        int ncount = order.getItems().getCount();
        boolean bAutoAddModifier = getSettings().getBoolean(KDSRouterSettings.ID.Modifier_auto_add);

        for (int i = ncount - 1; i >= 0; i--) {
            KDSDataItem item = order.getItems().getItem(i);
            if (item.getItemType() == KDSDataItem.ITEM_TYPE.Exp)
                continue;
            if (item.getModifiers().getCount() <= 0)
                continue;
            String itemDescription = item.getDescription();
            for (int j=0; j<item.getModifiers().getCount(); j++)
            {
                KDSDataModifier modifier = item.getModifiers().getModifier(j);
                String modifierDescription = modifier.getDescription();
                if (modifier.getPrepTime()>0)
                    continue;
                int nPrepTime = this.getRouterDB().modifierGetPrepTime(item.getCategory(), item.getDescription(), modifier.getDescription(), bAutoAddModifier); //unit is seconds.
                //float flt = KDSUtil.convertSecondsToMins(nPrepTime);

                if (nPrepTime >0)
                    modifier.setPrepTime(nPrepTime);
            }

        }


    }

    public void writeToAllExp(String xmlData)
    {
        int ncount = m_stationsConnection.getRelations().getRelationsSettings().size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationsRelation stationRelation = m_stationsConnection.getRelations().getRelationsSettings().get(i);
            if (stationRelation.getFunction() != KDSRouterSettings.StationFunc.Expeditor &&
                    stationRelation.getFunction() != KDSRouterSettings.StationFunc.Queue_Expo &&
                    stationRelation.getFunction() != KDSRouterSettings.StationFunc.Runner &&
                    stationRelation.getFunction() != KDSRouterSettings.StationFunc.Summary
            )
                continue;
            String stationID = stationRelation.getID();


            KDSStationIP station = stationRelation.getStationIP();

            station.setPort(getSettings().getString(KDSRouterSettings.ID.KDSRouter_Connect_Station_IPPort));

            KDSLog.d(TAG, "Write to expeditor #" + station.getID());// + ",ip="+station.getIP() + ", port="+station.getPort() +",length="+xmlData.length());

            //if (m_stationsConnection.findActivedStationByID(stationID) != null)
            m_stationsConnection.writeDataToStationOrItsBackup(station, xmlData);

        }

    }

    /**2015-12-26
     * check all item in orders, find same category items,
     * and get the max preparation time value for this category.
     * (The range is in this order)
     * @param order
     * @param item
     * @return
     */
    private float getItemCookTypeMaxPreparationTime(KDSDataOrder order, KDSDataItem item)
    {


        float maxVal = item.getPreparationTime();
        String category = item.getCategory();
        for (int i=0; i< order.getItems().getCount(); i ++)
        {
            KDSDataItem entry = order.getItems().getItem(i);
            if (!entry.getCategory().equals(category)) continue;
            float fltPreparationTime = entry.getPreparationTime();
            if (fltPreparationTime > maxVal)
                maxVal = fltPreparationTime;

        }

        return maxVal;
    }

    /**
     * write data to each POS
     * @param xmlData
     */
    public void writeToAllPOS(String xmlData)
    {
        //2015-12-26
        ArrayList<KDSStationConnection> conns = m_stationsConnection.getPosStations();
        int ncount = conns.size();
        for (int i=0; i< ncount  ; i++)
        {
            KDSStationConnection conn = conns.get(i);
            KDSLog.d(TAG, "Write to POS #" +conn.getID() + ",ip="+conn.getIP() + ", port="+conn.getPort() +",length="+xmlData.length());
            conn.getSock().writeXmlTextCommand(xmlData);
        }
    }


    /**
     * find each station, and check its status.
     *  If active, write to it. If it is not active, write to its backup station.
     * @param xmlData
     */
    public void writeToAllStations(String xmlData)
    {
        int ncount = m_stationsConnection.getRelations().getRelationsSettings().size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationsRelation stationRelation = m_stationsConnection.getRelations().getRelationsSettings().get(i);
            //kpp1-426, double qty issue.
            if (stationRelation.getFunction() == SettingsBase.StationFunc.Backup)
                continue;
            //write to the expo stations too!!!! 20160222
            //Just let expo receive item through the <kdsstation> tag
            //if (stationRelation.getFunction() == KDSRouterSettings.StationFunc.Expeditor)
            //    continue;
            //String stationID = stationRelation.getID();

            KDSStationIP station = stationRelation.getStationIP();
            //in relations settings, the station port is 3001 for communication of each other.
            //So, I have to change its port before send data.
            station.setPort(getSettings().getString(KDSRouterSettings.ID.KDSRouter_Connect_Station_IPPort));

            KDSLog.d(TAG, "Write to KDSStation #" +station.getID() + ",ip=" +station.getIP() + ", port="+station.getPort() +",length="+xmlData.length());

            //if (m_stationsConnection.findActivedStationByID(stationID) != null)
            m_stationsConnection.writeDataToStationOrItsBackup(station, xmlData);
        }

    }

    final int MAX_BUFFER_DATA_COUNT_FOR_WAITING_CONNECTION = 100;
    /**
     * 2.0.17
     * for order ack
     * @param xmlData
     * @param toStations
     */
    public void writeToAllStations2(String xmlData, ArrayList<String> toStations)
    {
        int ncount = m_stationsConnection.getRelations().getRelationsSettings().size();

        for (int i=0; i< ncount; i++)
        {
            KDSStationsRelation stationRelation = m_stationsConnection.getRelations().getRelationsSettings().get(i);
            //write to the expo stations too!!!! 20160222
            //Just let expo receive item through the <kdsstation> tag
            //if (stationRelation.getFunction() == KDSRouterSettings.StationFunc.Expeditor)
            //    continue;
            String stationID = stationRelation.getID();

            KDSStationIP station = stationRelation.getStationIP();
            //Here, it the xml is "delete" order command, it don't contain any stations target.
            //so I just send it to any station
            if (toStations.size() >0)
                if (!RouterAck.isExistedInArray(toStations, stationID)) continue; //
            //in relations settings, the station port is 3001 for communication of each other.
            //So, I have to change its port before send data.
            station.setPort(getSettings().getString(KDSRouterSettings.ID.KDSRouter_Connect_Station_IPPort));

            KDSLog.d(TAG, "Write to KDSStation #" +station.getID() + ",ip=" +station.getIP() + ", port="+station.getPort() +",length="+xmlData.length());

            //if (m_stationsConnection.findActivedStationByID(stationID) != null)
            //if (!KDSConst._DEBUG) //heap size issue is here
            m_stationsConnection.writeDataToStationOrItsBackup(station, xmlData,MAX_BUFFER_DATA_COUNT_FOR_WAITING_CONNECTION);
        }
        //check active stations again. As stations is alive, but it is not existed in relationship table.
        //kpp1-171
        //check stations that is not existed in relationship table.
        try {
            ArrayList<KDSStationIP> arActived = m_stationsConnection.getAllActiveStations();
            for (int i = 0; i < arActived.size(); i++) {
                KDSStationIP station = arActived.get(i);
                if (m_stationsConnection.getRelations().isExistedInRelationshipTable(station.getID()))
                    continue;
                if (toStations.size() > 0)
                    if (!RouterAck.isExistedInArray(toStations, station.getID())) continue; //
                //this is new station, just send data to it.
                m_stationsConnection.writeDataToStationOrItsBackup(station, xmlData, MAX_BUFFER_DATA_COUNT_FOR_WAITING_CONNECTION);
            }
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
        }
    }

    /**
     * kpp1-387, write data even if the station is not existed.
     *              If relationship is not set, and station is offline, router still need to backup its orders.
     * @param xmlData
     * @param toStations
     */
    public void writeToAllStations(String xmlData, ArrayList<String> toStations)
    {
        int ncount = toStations.size();// m_stationsConnection.getRelations().getRelationsSettings().size();

        for (int i=0; i< ncount; i++)
        {
            String stationID = toStations.get(i);//stationRelation.getID();
            KDSStationIP station  = null;
            station = m_stationsConnection.getRelations().findStationInRelationshipByID(stationID);
            if (station == null)
            {
                station = m_stationsConnection.findActivedStationByID(stationID);
            }
            if (station == null)
            {
                //kpp1-387
                //save it. Station is not existed in table, and it is not online.
                // Just save its data, maybe this station will come back soon.
                m_stationsConnection.writeGhostStionDataToBuffer(stationID, xmlData, MAX_BUFFER_DATA_COUNT_FOR_WAITING_CONNECTION);
            }
            else {
                station.setPort(getSettings().getString(KDSRouterSettings.ID.KDSRouter_Connect_Station_IPPort));
                KDSLog.d(TAG, "Write to KDSStation #" + station.getID() + ",ip=" + station.getIP() + ", port=" + station.getPort() + ",length=" + xmlData.length());
                m_stationsConnection.writeDataToStationOrItsBackup(station, xmlData, MAX_BUFFER_DATA_COUNT_FOR_WAITING_CONNECTION);

            }
        }

    }

    /**
     * according to the order station assignment to rebuild the xmlData.
     * Just write the tostation tag to xmlData string.
     *  I will rebuild xml base on the original string, as the order can not sure what is existed/changed after I parsed it.
     * @param order
     * @param xmlData
     * @return
     * the new string for given order.
     */
    public String rebuildOrderXml(KDSDataOrder order,String xmlData)
    {
        KDSXML xml = new KDSXML();
        xml.loadString(xmlData);
        xml.back_to_root();
        xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_TRANSACTION);
        xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_ORDER);

        //set kdsguid
        String kdsguid = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_KDSGUID, "");
        if (kdsguid.isEmpty())
            xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_KDSGUID, order.getKDSGuid(), false);

        //
        if (xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_ITEM)) {
            do {
                //xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_ID);
                String itemName = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_ID, "");

                String itemDescription = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_NAME, "");
                KDSDataItem item = order.getItems().getItemByName(itemName);
                if (item == null) continue;

                String category = item.getCategory();
                //to station tag
                String xmlToStation = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_KDSSTATION, "");


                    xmlToStation = item.getToStationsString();
                    if (!xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_KDSSTATION)) {
                        xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_KDSSTATION, xmlToStation, false);
                    } else {
                        xml.setGroupValue(xmlToStation);
                        xml.back_to_parent();
                    }

                //according to preparation time, to setup the delay tag
                //
                String xmlPreparationTime = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_PREPARATION_TIME, "");
                xmlPreparationTime = xmlPreparationTime.trim();
                if (xmlPreparationTime.isEmpty())
                {
                    xmlPreparationTime = KDSUtil.convertFloatToString( item.getPreparationTime());
                    if (!xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_PREPARATION_TIME)){
                        xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_PREPARATION_TIME, xmlPreparationTime, false);
                    }
                    else {
                        xml.setGroupValue(xmlPreparationTime);
                        xml.back_to_parent();
                    }
                }
                //item delay tag
                String xmlItemDelay = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_ITEM_DELAY, "");
                xmlItemDelay = xmlItemDelay.trim();
                if (xmlItemDelay.isEmpty())
                {
                    xmlItemDelay = KDSUtil.convertFloatToString(item.getItemDelay());
                    if (!xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_ITEM_DELAY)){
                        xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ITEM_DELAY, xmlItemDelay, false);
                    }
                    else {
                        xml.setGroupValue(xmlItemDelay);
                        xml.back_to_parent();
                    }
                }


                //category delay tag
                String xmlCategoryDelay = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_CATEGORY_DELAY, "");
                xmlCategoryDelay = xmlCategoryDelay.trim();
                if (xmlCategoryDelay.isEmpty())
                {
                    xmlCategoryDelay = KDSUtil.convertFloatToString(item.getCategoryDelay());
                    if (!xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_CATEGORY_DELAY)){
                        xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_CATEGORY_DELAY, xmlCategoryDelay, false);
                    }
                    else {
                        xml.setGroupValue(xmlCategoryDelay);
                        xml.back_to_parent();
                    }
                }
                //item printable
                String xmlItemPrintable = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_PRINTABLE, "");
                xmlItemPrintable = xmlItemPrintable.trim();
                if (xmlItemPrintable.isEmpty())
                {
                    xmlItemPrintable = (item.getPrintable()?"1":"0");

                    if (!xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_PRINTABLE)){
                        xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_PRINTABLE, xmlItemPrintable, false);
                    }
                    else {
                        xml.setGroupValue(xmlItemPrintable);
                        xml.back_to_parent();
                    }
                }


                //build cards
                String xmlBuildCards = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_BUILD_CARD, "");
                xmlBuildCards = xmlBuildCards.trim();
                if (xmlBuildCards.isEmpty())
                {
                    xmlBuildCards = item.getBuildCard().toCSV();//
                    if (!xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_BUILD_CARD)){
                        xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_BUILD_CARD, xmlBuildCards, false);
                    }
                    else {
                        xml.setGroupValue(xmlBuildCards);
                        xml.back_to_parent();
                    }
                }


                //training video
                String xmlTrainingVideo = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_TRAINING_VIDEO, "");
                xmlTrainingVideo = xmlTrainingVideo.trim();
                if (xmlTrainingVideo.isEmpty())
                {
                    xmlTrainingVideo = item.getTrainingVideo().toCSV();//
                    if (!xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_TRAINING_VIDEO)){
                        xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TRAINING_VIDEO, xmlTrainingVideo, false);
                    }
                    else {
                        xml.setGroupValue(xmlTrainingVideo);
                        xml.back_to_parent();
                    }
                }

                //color, 2.1.15.4
                int bg = item.getBG();
                int fg = item.getFG();
                if ( bg != fg ) {
                    //use RGBColor
                    if (xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_COLOR)) {
                        xml.back_to_parent();
                        xml.delSubGroup(KDSXMLParserOrder.DBXML_ELEMENT_COLOR);
                        //xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_KDSSTATION, xmlToStation, false);
                    }
                    if (!xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_RGBCOLOR)) {
                        xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_RGBCOLOR, true);
                    }
                    xml.setAttribute(KDSXMLParserOrder.DBXML_ELEMENT_COLOR_BG, KDSUtil.convertIntToString(bg));
                    xml.setAttribute(KDSXMLParserOrder.DBXML_ELEMENT_COLOR_FG, KDSUtil.convertIntToString(fg));
                    xml.back_to_parent();
                }


                //sumnames
                /**
                 *           <SumTrans enabled="0" >Beef medium(0.00)
                 Water medium(0.00)
                 user /n as seperator, (0.00) is the qty
                 </SumTrans>
                 */

                String xmlSumNames = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_SUMMARY_TRANSLATE, "");
                xmlSumNames = xmlSumNames.trim();
                if (xmlSumNames.isEmpty() && item.getSumNamesEnabled())
                {
                    KDSDataSumNames sumNames = item.getSumNames();
                    //if (sumNames.getCount() >0) {
                    if (item.getSumNamesEnabled()){
                        if (!xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_SUMMARY_TRANSLATE)) {
                            xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_SUMMARY_TRANSLATE, sumNames.toString(), true);
                            xml.setAttribute(KDSXMLParserOrder.DBXML_ELEMENT_SUMMARY_ENABLED,"1" );
                            xml.back_to_parent();
                        } else {
                            xml.setGroupValue(sumNames.toString());
                            xml.setAttribute(KDSXMLParserOrder.DBXML_ELEMENT_SUMMARY_ENABLED,"1" );
                            xml.back_to_parent();
                        }
                    }
                }

                //modifiers preparation time
                if (xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_MODIFIER)) {
                    do {
                        String modifierDescription = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_NAME, "");
                        String xmlModifierPrepTime = xml.getSubGrouValue(KDSXMLParserOrder.DBXML_ELEMENT_PREPARATION_TIME, "");
                        xmlModifierPrepTime = xmlModifierPrepTime.trim();
                        if (xmlModifierPrepTime.isEmpty())
                        {
                            KDSDataModifier modifier = item.getModifiers().findModifier(modifierDescription);
                            if (modifier != null) {
                                xmlModifierPrepTime = KDSUtil.convertFloatToString(KDSUtil.convertSecondsToMins( modifier.getPrepTime()) );//use unit minutes
                                if (!xml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_PREPARATION_TIME)) {
                                    xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_PREPARATION_TIME, xmlModifierPrepTime, false);
                                } else {
                                    xml.setGroupValue(xmlModifierPrepTime);
                                    xml.back_to_parent();
                                }
                            }
                        }

                    }
                    while (xml.getNextGroup(KDSXMLParserOrder.DBXML_ELEMENT_MODIFIER));


                    xml.back_to_parent();
                }

            }
            while (xml.getNextGroup(KDSXMLParserOrder.DBXML_ELEMENT_ITEM));
        }

        return xml.get_xml_string();
    }
    //boolean m_bAutoAddItem = true;
    /**
     * if all assigned stations, just pass it.
     * @param order
     * @return
     */
    public boolean isAllItemsAssignedStation(KDSDataOrder order)
    {
        boolean bAutoAddItem = getSettings().getBoolean(KDSRouterSettings.ID.Item_auto_add);

        int ncount = order.getItems().getCount();
        if (ncount <=0) return false;
        for (int i=ncount-1; i>=0; i--)
        {
            KDSDataItem item = order.getItems().getItem(i);
            if (bAutoAddItem)
                this.getRouterDB().itemAddToFilter(item);

            if (item.getItemType() == KDSDataItem.ITEM_TYPE.Exp)
                continue;
            String toStations = item.getToStations().getString();
            if (toStations.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * if the assigned station is offline, check the backup stations.
     * call this function after all item has been assigned station.
     * @param order
     * @param xmlData
     */
    public void checkAssignStationsActive(KDSDataOrder order, String xmlData)
    {
        int ncount = order.getItems().getCount();
        for (int i=ncount-1; i>=0; i--)
        {
            KDSDataItem item = order.getItems().getItem(i);
            if (item.getItemType() == KDSDataItem.ITEM_TYPE.Exp)
                continue; //to all expo stations, the backup stations should included in it.
            if (item.getToStations().getCount()>0) {
                for (int n = 0; n< item.getToStations().getCount(); n++ ) {
                    KDSToStation toStation = item.getToStations().getToStation(n);
                    String stationID = toStation.getPrimaryStation();
                    if (isActivedStation(stationID))
                        continue;
                    else
                    {//check its backup station
                        ArrayList<String>  backups = m_stationsConnection.getActivedBackupStations(stationID);

                        if (backups.size() >0)
                        {
                            boolean changeStation = true;
                            for (int j=0; j< backups.size(); j++)
                            {
                                if (m_stationsConnection.getRelations().isQueueExpoStation(backups.get(j)))
                                    changeStation = false;
                            }
                            if (changeStation) {
                                String toBackups = KDSStationsRelation.makeStationsString(backups);
                                item.setToStationsString(toBackups);
                                item.setStationChangedToBackup(true);
                            }
                        }
                    }
                }
            }

        }
    }

    boolean isActivedStation(String stationID)
    {
        return (m_stationsConnection.findActivedStationByID(stationID) != null);
    }
    /**
     * filter the items according the database settings.
     * Here, I will not update the backup router, as this function just add item to database,
     * don't set its destination kds station.
     * And, if we update database again and again, it will slow down the kds system speed.
     *
     * @param order
     * @param xmlData
     */
    public void assignStationsToOrder(KDSDataOrder order, String xmlData)
    {

        //for check if the database changed.
//        String strBeforeChangesGuid = getRouterDB().getChangesGUID();
        String strDefaultStationID = getSettings().getString(KDSRouterSettings.ID.KDSRouter_Default_Station);
        if (strDefaultStationID.isEmpty()) strDefaultStationID = "1";

        int ncount = order.getItems().getCount();
        for (int i=ncount-1; i>=0; i--)
        {
            KDSDataItem item = order.getItems().getItem(i);

            if (item.getItemType() == KDSDataItem.ITEM_TYPE.Exp)
                continue;
            if (!item.getToStations().getString().isEmpty())
                continue;
            String description = item.getDescription();
            String category = item.getCategory();
            String toStationWithScreen = this.getRouterDB().itemGetToStationWithScreen(category, description,strDefaultStationID);// m_strDefaultToStation);

            if (toStationWithScreen.isEmpty())
                toStationWithScreen =strDefaultStationID;// m_strDefaultToStation;
            item.setToStationsString(toStationWithScreen);

        }


    }

    /**
     * 2015-12-26
     * load the preparation time from database.
     * set the item delay tag
     * Rev.
     *    20210220, KP-27, add delay/preparation for both category and item.
     * @param order
     * @param xmlData
     */
    public void assignSmartTimeToOrder(KDSDataOrder order, String xmlData) {

        int ncount = order.getItems().getCount();
        for (int i = ncount - 1; i >= 0; i--) {
            KDSDataItem item = order.getItems().getItem(i);
            if (item.getItemType() == KDSDataItem.ITEM_TYPE.Exp)
                continue;
            if (item.getPreparationTime() > 0)
                continue;
            String description = item.getDescription();

            float flt = this.getRouterDB().itemGetPreparationTime(description);
            if (flt >0) {
                item.setPreparationTime(flt);
                continue;
            }
            //kp-27
            flt = this.getRouterDB().categoryGetPreparationTime(description);
            if (flt >0)
                item.setPreparationTime(flt);


        }

        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = order.getItems().getItem(i);
            if (item.getItemType() == KDSDataItem.ITEM_TYPE.Exp)
                continue;
            //if (item.getItemDelay() > 0)//KPP1-410, bug here.
            if (item.getCategoryDelay() > 0)//KPP1-410, xml data in high priority.
                continue;

            float categoryDelay = this.getRouterDB().categoryGetDelay(item.getCategory());

            //float maxTypePreparationTime = this.getItemCookTypeMaxPreparationTime(order, item);
            //item.setItemDelay(typeDelay + maxTypePreparationTime); //just for smart order
            //the preparation time is for whole order, it is not for category group !!!!
            //see email:
            //Right now, the smart order only works for items that are in the same category.
            // It is the wrong concept, please change it so the <PreparationTime> doesn???t limit to same category but work for all categories.

            //item.setItemDelay(categoryDelay); //just for smart order, the normal smart mode is removed, just keep advanced.
            if (categoryDelay >0)
                item.setCategoryDelay(categoryDelay);//for preparation time mode.
            //

        }

        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = order.getItems().getItem(i);
            if (item.getItemType() == KDSDataItem.ITEM_TYPE.Exp)
                continue;
            //if (item.getItemDelay() > 0)//KPP1-410, bug here.
            if (item.getItemDelay() > 0)//KPP1-410, xml data in high priority.
                continue;

            float delay = this.getRouterDB().itemGetDelay(item.getDescription());

            if (delay >0)
                item.setItemDelay(delay);//for preparation time mode.
            //

        }
    }



    public String getStationID()
    {
        return m_strStationID;
    }


    public void refreshView()
    {

    }

    private Thread m_threadPing = null;
    private boolean m_bRunning = false;

    /**
     * Ping connected IP address, if can not get them, this station is down.
     *
     * @return
     */
    public boolean startPingThread() {
        KDSLog.e(TAG, KDSLog._FUNCLINE_() + "Enter");

        m_bRunning = true;
        m_threadPing = new Thread(this, "SocketPing");
        m_threadPing.start();
        KDSLog.e(TAG, KDSLog._FUNCLINE_() + "Exit");
        return true;

    }

    /**
     *
     * @return
     */
    public boolean stopPingThread() {
        KDSLog.e(TAG, KDSLog._FUNCLINE_() + "Enter");
        if (m_threadPing != null) {
            m_bRunning = false;
            try {
                m_threadPing.join(1000);
            } catch (Exception e) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
                //KDSLog.e(TAG, KDSUtil.error( e));
            }
        }
        m_threadPing = null;
        KDSLog.e(TAG, KDSLog._FUNCLINE_() + "Exit");
        return true;
    }


    TimeDog m_dogAnnounce = new TimeDog();


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
                this.broadcastStationAnnounce();
                checkLostStationInThread();
                    //broadcastRouterAnnounceInThread();
                broadcastRouterAnnounce();

                //this.broadcastRequireStationsUDP(); //get all station's ip address.
            }

            if (this.isEnabled())
                m_stationsConnection.connectAllStations();

            try {
                Thread.sleep(KDSConst.PING_THREAD_SLEEP);
            }
            catch (Exception e)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
                //KDSLog.e(TAG, KDSUtil.error( e));
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
        //2015-12-30\
        //primary router
        if (!m_primaryRouterID.isEmpty()) {
            KDSStationIP stationip = this.getStationsConnections().findActivedStationByID(m_primaryRouterID);
            if (stationip == null)
            {
                this.m_sockEventsMessageHandler.sendLostAnnouncePulseMessage(m_primaryRouterID, "");
            }
        }
        //2015-12-30
        //slave router
        if (!m_slaveRouterID.isEmpty()) {
            KDSStationIP stationip = this.getStationsConnections().findActivedStationByID(m_slaveRouterID);
            if (stationip == null)
            {
                this.m_sockEventsMessageHandler.sendLostAnnouncePulseMessage(m_slaveRouterID, "");
            }
        }

    }

    public void broadcastRequireStationsUDP()
    {
        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildRequireStationsCommand();

        m_udpStationAnnouncer.broadcastData(KDSRouterSettings.UDP_STATIONS_ANNOUNCER_UDP_PORT, buf);
    }

    /**
     * Others computer ask the kds settings
     * @param command
     * @param xmlOriginalData
     *  The xml don't been parsed to command
     */
    public void commandRequireConfigurations(KDSSocketInterface sock, KDSXMLParserCommand command,String xmlOriginalData)
    {


        String s = m_settings.outputXmlText(this.m_context);
        s = KDSXMLParserCommand.createBroadConfiguration(s);
        if (sock instanceof KDSSocketTCPSideBase)
        {
            KDSSocketTCPSideBase tcp = (KDSSocketTCPSideBase)sock;
            tcp.writeXmlTextCommand(s);
        }

        //copy database to it
        copyRouterDatabaseToOther(sock);


    }


    public void commandReceiveConfigurations(KDSSocketInterface sock,  KDSXMLParserCommand command,String xmlOriginalData)
    {
        String strConfig = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strConfig.isEmpty())
            return;
        m_settings.parseXmlText(m_context, strConfig);
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

        KDSStationConnection conn = this.m_stationsConnection.findConnectionByID(stationID);//.findConnectionClientSideByID(stationID);
        String s = KDSXMLParserCommand.createRequireConfiguration(""); //don't need the fromip
        if (conn != null) {
            if (conn.getSock().isConnected()) {
                conn.getSock().writeXmlTextCommand(s);
                if (txtInfo != null)
                    txtInfo.setText( txtInfo.getContext().getString(R.string.wait_for_config_data));//"Waiting for config data...");
            }
            else {
                //conn.addBufferedData(s); ////use kdsstationsconnection-->m_buffersForWaitingConnection to save offline data
                this.m_stationsConnection.getNoConnectionBuffer().add(stationID, s, NoConnectionDataBuffers.MAX_BACKUP_DATA_COUNT);
                if (txtInfo != null)
                    txtInfo.setText(txtInfo.getContext().getString(R.string.wait_for_connection));//"Waiting for new connection...");
            }
        }
        else
        {//connect it.

            KDSStationActived stationActive = this.m_stationsConnection.findActivedStationByID(stationID);
            if (stationActive == null) {
                txtInfo.setText(txtInfo.getContext().getString(R.string.station_is_inactive));// "Station is not active...");
                return 0;
            }
            KDSStationConnection willConn = this.m_stationsConnection.connectToStation(stationActive);
            if (willConn.getSock().isConnected()) {
                willConn.getSock().writeXmlTextCommand(s);
                if (txtInfo != null)
                    txtInfo.setText(txtInfo.getContext().getString(R.string.wait_for_config_data));//"Waiting for config data...");
            }
            else {
                this.m_stationsConnection.getNoConnectionBuffer().add(stationID, s, NoConnectionDataBuffers.MAX_BACKUP_DATA_COUNT);
                //willConn.addBufferedData(s);//use kdsstationsconnection-->m_buffersForWaitingConnection to save offline data
                if (txtInfo != null)
                    txtInfo.setText(txtInfo.getContext().getString(R.string.waiting_for_connecting));// "Waiting for connecting...");
            }


        }
        return 1;
    }
    android.os.Handler m_msgHandler = new android.os.Handler(new android.os.Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            showMessageToMainUI((String)msg.obj);
            return true;
        }

    });
    public void showMessage(String msg)
    {
        Message m = new Message();
        m.what = 0;
        m.obj = msg;
        m_msgHandler.sendMessage(m);

//        int ncount = m_arKdsEventsReceiver.size();
//        for (int i=0; i< ncount; i++)
//        {
//            m_arKdsEventsReceiver.get(i).onShowMessage(msg);
//        }
    }
    public void showMessageToMainUI(String msg)
    {
        int ncount = m_arKdsEventsReceiver.size();
        for (int i=0; i< ncount; i++)
        {
            m_arKdsEventsReceiver.get(i).onShowMessage(msg);
        }
    }

    public KDSRouterSettings getSettings()
    {
        return m_settings; //this settings is saved in layout
    }

    public void broadcastRelations(String relations)
    {
        String s = "<Relations>";
        s += relations;
        s += "</Relations>";
        ByteBuffer buf =  KDSSocketTCPCommandBuffer.buildXMLCommand(s);

        m_udpStationAnnouncer.broadcastData(KDSRouterSettings.UDP_STATIONS_ANNOUNCER_UDP_PORT, buf);

        m_udpStationAnnouncer.broadcastData(KDSRouterSettings.UDP_ROUTER_ANNOUNCER_UDP_PORT, buf);


    }

    public void broadcastRequireRelations(String relations)
    {
        String s = "<RelationsRet>";
        s += relations;
        s += "</RelationsRet>";
        ByteBuffer buf =  KDSSocketTCPCommandBuffer.buildXMLCommand(s);

        m_udpStationAnnouncer.broadcastData(KDSRouterSettings.UDP_STATIONS_ANNOUNCER_UDP_PORT,buf);
        m_udpStationAnnouncer.broadcastData(KDSRouterSettings.UDP_ROUTER_ANNOUNCER_UDP_PORT, buf);

    }

    public void broadcastRequireRelationsCommand()
    {

        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildRequireRelationsCommand();
        m_udpStationAnnouncer.broadcastData(KDSRouterSettings.UDP_STATIONS_ANNOUNCER_UDP_PORT,buf);
        m_udpStationAnnouncer.broadcastData(KDSRouterSettings.UDP_ROUTER_ANNOUNCER_UDP_PORT, buf);
    }

    public void udpAskRelations(String stationID)
    {
        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildAskStationsRelationsCommand();
        KDSStationActived station =  this.getStationsConnections().findActivedStationByID(stationID);
        if (station == null) return;
        int nport = KDSUtil.convertStringToInt(station.getPort(), 0);
        int udpPort = KDSRouterSettings.UDP_STATIONS_ANNOUNCER_UDP_PORT;
        if (nport == getSettings().getInt(KDSRouterSettings.ID.KDSRouter_Backup_IPPort))
            udpPort = KDSRouterSettings.UDP_ROUTER_ANNOUNCER_UDP_PORT;

        m_udpStationAnnouncer.broadcastData(station.getIP(), udpPort, buf);

    }

    public void broadcastShowStationID()
    {
        broadcastShowStationIDCommand();

    }

    private void broadcastShowStationIDCommand()
    {

        ByteBuffer buf = KDSSocketTCPCommandBuffer.buildShowStationIDCommand();
        m_udpStationAnnouncer.broadcastData(KDSRouterSettings.UDP_STATIONS_ANNOUNCER_UDP_PORT, buf);

    }


    public void onShowStationID()
    {
        String stationid = this.getStationID();
        //  showMessage(stationid);
        KDSToast.showStationID(this.m_context, stationid);
    }
//
    public boolean routerOtherEnabled()
    {
        return m_stationsConnection.routerOtherEnabled(getLocalIpAddress());

    }

    public boolean isEnabled()
    {

        return this.getSettings().getBoolean(KDSRouterSettings.ID.KDSRouter_Enabled);
    }
    boolean m_bHasShowRouterUniqueError = false;
    public void checkRouterUnique()
    {
        if (m_bHasShowRouterUniqueError) return;
        if (isEnabled()) {
            if (getSettings().getBoolean(KDSRouterSettings.ID.KDSRouter_Backup))
                return;
            if (routerOtherEnabled()) {
                m_bHasShowRouterUniqueError = true;
                showOtherRouterEnabledError();
            }
        }
    }
    static public final String ROUTER_UNIQUE = "ROUTER_UNIQUE";

    public void  showOtherRouterEnabledError()
    {
        String strError ="";// KDSApplication.getContext().getString(R.string.error_other_router_enabled);

        strError = ROUTER_UNIQUE;
        if (m_arKdsEventsReceiver != null) {
            for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
                m_arKdsEventsReceiver.get(i).onShowMessage(strError);
        }

    }


    /**
     * check if this ip station is included in my store.
     * If we can find it in active station, it is my store station.
     * Active stations just contain my store stations.
     * @param ip
     * @return
     */
    private boolean isMyStoreIP(String ip)
    {
        KDSStationActived station =m_stationsConnection.findActivedStationByIP(ip);//id);
        return (station != null);

    }


    /**
     * notification pos order acknowledgement.
     * @param objSource
     * @param xmlData
     */
    private void doOrderAcknowledgement(Object objSource,KDSDataOrder order, int nAcceptItemsCount,String smbFileName, String xmlData,String orderName,String errorCode, boolean bGoodOrder)
    {
        if (!KDSConst.ENABLE_FEATURE_ORDER_ACKNOWLEDGEMENT) return;

        String fileName = KDSPosNotificationFactory.createOrderAckFileName(order, nAcceptItemsCount, smbFileName, orderName, bGoodOrder);
        String s = KDSPosNotificationFactory.createOrderAcknowledgement(getStationID(), order,nAcceptItemsCount ,smbFileName, xmlData, orderName, errorCode, bGoodOrder, fileName);
        Object objSrc = objSource;

        writeXmlToPOSOrderAck( s, fileName);
    }

    public void assignColorToOrder(KDSDataOrder order, String xmlData)
    {
        int ncount = order.getItems().getCount();


        for (int i=ncount-1; i>=0; i--)
        {
            KDSDataItem item = order.getItems().getItem(i);
            if ( item.getBG() != item.getFG())
                continue;

            String description = item.getDescription();
            String category = item.getCategory();
            KDSRouterDataItem colorItem =  getRouterDB().itemGetBGFG(category, description);
            if (colorItem.getBG()==0 && colorItem.getFG() ==0)
                continue;

            item.setBG(colorItem.getBG());
            item.setFG(colorItem.getFG());
        }

    }
    /**
     * The expo, track, and queue accept any items

     * @return
     */
    private ArrayList<KDSToStation> getAllAcceptAnyItemsStations()
    {
        int ncount = m_stationsConnection.getRelations().getRelationsSettings().size();

        ArrayList<KDSToStation> ar = new ArrayList<>();


        for (int i=0; i< ncount; i++) {
            KDSStationsRelation stationRelation = m_stationsConnection.getRelations().getRelationsSettings().get(i);
            if (stationRelation.getFunction() == KDSRouterSettings.StationFunc.Expeditor ||
                    stationRelation.getFunction() == KDSRouterSettings.StationFunc.Queue_Expo ||
                    stationRelation.getFunction() == KDSRouterSettings.StationFunc.TableTracker ||
                    stationRelation.getFunction() == KDSRouterSettings.StationFunc.Runner ||
                    stationRelation.getFunction() == KDSRouterSettings.StationFunc.Summary
            )
            {
                if (stationRelation.getFunction() == SettingsBase.StationFunc.Expeditor)
                {//workload: prep and workload all with expo, we don't sen order to expo.
                    //let the prep to handle this order.
                    if (isExpoInPrepWithWorkload(stationRelation.getID()))
                        continue;
                }
                KDSToStation toStation = new KDSToStation();
                toStation.setPrimaryStation(stationRelation.getID());
                ar.add(toStation);
            }

        }
        return ar;

    }

    final int MAX_SOCK_BUFFER_SIZE = 10; //the buffer has save these data, don't save more.
    /**
     * 20190215 KPP1-Coke
     * As I save all data to buffer, this cause router haust all resources.
     * Check if there is any alive station buffer too many data.
     * If so, don't read more data.
     * For Coke company
     * @return
     */
    public boolean isEthernetBufferedTooManyData()
    {
        ArrayList<KDSStationIP> ar = m_stationsConnection.getAllActiveStations();
        for (int i=0; i< ar.size(); i++)
        {
            if (i >= ar.size()) return false;
            KDSStationIP station = ar.get(i);
            if (station == null) return false;
            KDSStationConnection connection = m_stationsConnection.getConnection(station);
            //just check connected station
            if (connection == null ||
                    !connection.isConnected() ||
                    connection.isConnecting()
                    )
                continue;
            if (connection.getSock().isBufferTooManyWritingData(MAX_SOCK_BUFFER_SIZE))
                return true;

        }
        return false;
    }

    public boolean bufferCheckerIsTooManyDataBuffered()
    {
        return (m_xmlDataBuffer.size() > 5);//MAX_SOCK_BUFFER_SIZE );

        //return isEthernetBufferedTooManyData();
    }
    ///////////////////////// 20190121 ///////////////////
    Thread m_threadOrdersXml = null;
    Object m_lockerForOrdersThread = new Object();
    ArrayList<DoOrdersXmlThreadBuffer> m_xmlDataBuffer = new ArrayList<>();

    /**
     *
     * @param objSource
     * @param originalFileName
     * @param xmlData
     * @param ordersFromFCM
     */
    public void doOrderXmlInThread(final Object objSource, final String originalFileName, final String xmlData, final KDSDataOrders ordersFromFCM)
    {
        DoOrdersXmlThreadBuffer data = new DoOrdersXmlThreadBuffer();
        data.m_objSource = objSource;
        data.m_originalFileName = originalFileName;
        data.m_xmlData = xmlData;
        data.m_fcmOrders = ordersFromFCM;

        synchronized (m_lockerForOrdersThread) {
            m_xmlDataBuffer.add(data);
        }

        if (m_threadOrdersXml == null ||
            !m_threadOrdersXml.isAlive())
        {
            m_threadOrdersXml = new Thread(new Runnable() {
                @Override
                public void run() {
                    DoOrdersXmlThreadBuffer data = null;
                    ArrayList<DoOrdersXmlThreadBuffer> arDone = new ArrayList<>();
                        while (m_bRunning) {
                            int ncount = m_xmlDataBuffer.size();
                            if (ncount <=0) {
                                try {
                                    Thread.sleep(500);
                                    continue;
                                }
                                catch (Exception e){}

                            }

                            for (int i = 0; i < ncount; i++) {
                                try {
                                    synchronized (m_lockerForOrdersThread) {
                                        data = m_xmlDataBuffer.get(i);
                                        arDone.add(data);
                                        //m_xmlDataBuffer.remove(0);
                                    }
                                    if (data.m_fcmOrders != null)
                                        doReceivedFCMOrders(data.m_fcmOrders);
                                    else {
                                        KDSXMLParser.XMLType ntype = checkXmlType(data.m_xmlData);
                                        if (ntype == KDSXMLParser.XMLType.Order)
                                            doOrderXml(data.m_objSource, data.m_originalFileName, data.m_xmlData);
                                        else if (ntype == KDSXMLParser.XMLType.POS_Info)
                                        {
                                            doPOSMessage(data.m_xmlData);
                                        }
                                    }
                                    data.clear();
                                    Thread.sleep(200);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            synchronized (m_lockerForOrdersThread) {
                                m_xmlDataBuffer.removeAll(arDone);
                            }
                            arDone.clear();

                        }
                }
            });
            m_threadOrdersXml.setName("DoXml");
            m_threadOrdersXml.start();
        }


    }

    Thread m_threadNotificationXml = null;
    Object m_lockerForNotificationThread = new Object();
    ArrayList<String> m_xmlDataNotificationBuffer = new ArrayList<>();

    public void doNotificationXmlInThread(final Object objSource, String xmlData)
    {
        synchronized (m_lockerForNotificationThread) {
            m_xmlDataNotificationBuffer.add(xmlData);
        }

        if (m_threadNotificationXml == null ||
                !m_threadNotificationXml.isAlive())
        {
            m_threadNotificationXml = new Thread(new Runnable() {
                @Override
                public void run() {

                    ArrayList<String> arDone = new ArrayList<>();
                    String data = "";
                    while (m_bRunning) {
                        int ncount = m_xmlDataNotificationBuffer.size();
                        if (ncount <=0) {
                            try {
                                Thread.sleep(500);
                                continue;
                            }
                            catch (Exception e){}

                        }

                        arDone.clear();
                        for (int i = 0; i < ncount; i++) {
                            try {

                                synchronized (m_lockerForNotificationThread) {
                                    data = m_xmlDataNotificationBuffer.get(i);
                                    arDone.add(data);
                                    //m_xmlDataNotificationBuffer.remove(0);
                                }
                                doNotificationXml(null, data);

                                Thread.sleep(50);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        synchronized (m_lockerForNotificationThread) {
                            m_xmlDataNotificationBuffer.removeAll(arDone);

                        }
                        arDone.clear();
                    }
                }
            });
            m_threadNotificationXml.setName("DoNotification");
            m_threadNotificationXml.start();
        }

    }

    static class DoOrdersXmlThreadBuffer
    {
        Object m_objSource = null;
        String m_originalFileName = "";
        String m_xmlData = "";
        KDSDataOrders m_fcmOrders = null;
        public void clear()
        {
            m_objSource = null;
            m_originalFileName = "";
            m_xmlData = "";
            m_fcmOrders = null;
        }
    }

    ByteBuffer m_bufferForRouterAnnounce = ByteBuffer.allocate(100);
    private void broadcastRouterAnnounce()
    {
        m_bufferForRouterAnnounce.clear();
        m_udpStationAnnouncer.broadcastData(KDSSettings.UDP_ROUTER_ANNOUNCER_UDP_PORT, makeAnnounceToRouterBuffer(m_bufferForRouterAnnounce));
    }

    private ByteBuffer makeAnnounceToRouterBuffer(ByteBuffer buf)
    {
        int port = KDSSettings.UDP_ROUTER_ANNOUNCER_UDP_PORT;
        String strport = KDSUtil.convertIntToString(port);
        boolean bEnabled = getSettings().getBoolean(KDSRouterSettings.ID.KDSRouter_Enabled);
        boolean bBackupMode = getSettings().getBoolean(KDSRouterSettings.ID.KDSRouter_Backup);
        return KDSSocketTCPCommandBuffer.buildRouterStationAnnounceCommand(getStationID(), m_strLocalIP, strport, getLocalMacAddress(), bEnabled, bBackupMode, buf);

    }

    public boolean isThreadRunning()
    {
        return m_bRunning;
    }

    public int removeNotifications()
    {

        KDSSettings.KDSDataSource source = KDSSettings.KDSDataSource.values()[getSettings().getInt(KDSSettings.ID.KDSRouter_Data_Source)];

        if (source == KDSSettings.KDSDataSource.Folder) {
            int nMinutes = getSettings().getInt(KDSRouterSettings.ID.notification_minutes);
            if (nMinutes <=0) return 0;
            return m_smbDataSource.removeTimeoutNotificationFiles(KDSConst.SMB_FOLDER_NOTIFICATION, nMinutes);
        }
        return 0;

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

    /**
     * clear router buffered ACK and offline data.
     */
    public void clearAll()
    {
        m_stationsConnection.clear();
    }

    /**
     * KDSCallback interface
     * @param receiver
     */
    public void call_setStationAnnounceEventsReceiver(StationAnnounceEvents receiver) {
        this.setStationAnnounceEventsReceiver(receiver);
    }
    public void call_broadcastRequireStationsUDP() {
        this.broadcastRequireStationsUDP();
    }
    public String call_getStationID() {
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
        return this.getSettings().getString(KDSRouterSettings.ID.KDSRouter_Backup_IPPort);
    }


    public void call_broadcastShowStationID()
    {
        this.broadcastShowStationID();
    }
    public void call_udpAskRelations(String stationID)
    {
        this.udpAskRelations(stationID);
    }
    public void call_broadcastRelations(String relationsData)
    {
        this.broadcastRelations(relationsData);
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
                KDSGlobalVariables.getKDS().broadcastRelations(strData);
                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arParams);
    }

    /**
     * KPP1-299
     * @return
     */
    public boolean isDbEmpty()
    {
        return m_dbRouter.isEmpty();

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
     *
     * @param orders
     */
    public void onFCMReceivedOrders(KDSDataOrders orders)
    {
        doOrderXmlInThread(null, "", "", orders);

//        for (int i=0; i< orders.getCount(); i++)
//        {
//            doReceivedFCMOrder(orders.get(i));
//        }
    }

    public void doReceivedFCMOrders( KDSDataOrders orders)
    {
        for (int i=0; i< orders.getCount(); i++)
        {
            doReceivedFCMOrder(orders.get(i));
        }
    }
    /**
     *
     * @param order
     */
    public void doReceivedFCMOrder( KDSDataOrder order)
    {
        KDSLogOrderFile.i(TAG,KDSLogOrderFile.formatOrderLog(order.getOrderName()));

        order.setPCKDSNumber(m_strStationID);
        KDSLog.d(TAG, "Receive order $" + order.getOrderName());
        String xmlData = order.createXml();
        doOrderFilter(order, xmlData, "");
        String msg = "";//Get order #" + order.getOrderName();
        msg = m_context.getString(R.string.get_fcm_order);//getContext().getString(R.string.get_order);
        msg = msg.replace("#", order.getOrderName());

        showMessage(msg);

    }

    private String buildAppSockIDCommandXml()
    {
        return KDSConst.APP_ID_START + KDSConst.ROUTER_SOCKET_ID + KDSConst.APP_ID_END;
    }

    private void doPOSMessage(String xmlData)
    {
        KDSPOSMessage msg = (KDSPOSMessage) KDSXMLParser.parseXml("", xmlData);
        if (msg == null) return;
        String station = msg.getStation();
        if (station.equals("-1"))
        {
            this.writeToAllStations(xmlData);
        }
        else {
            ArrayList<String> stations = new ArrayList<>();
            stations.add(station);
            this.writeToAllStations(xmlData, stations);
        }

    }

    public void assignItemsPrintable(KDSDataOrder order, String xmlData)
    {

        int ncount = order.getItems().getCount();
        for (int i=ncount-1; i>=0; i--)
        {
            KDSDataItem item = order.getItems().getItem(i);
            //set printable here
            boolean bPrintable = getRouterDB().itemGetPrintable(item.getCategory(), item.getDescription());
            item.setPrintable(bPrintable);

        }


    }

    private void fireNetworkStateResult(boolean bAvailable)
    {
        ArrayList<Object> arData = new ArrayList<>();
        arData.add(bAvailable);
        if (m_arKdsEventsReceiver != null) {
            for (int i=0; i< m_arKdsEventsReceiver.size(); i++)
                m_arKdsEventsReceiver.get(i).onKDSEvent(KDSEventType.Network_state, arData);
        }
    }

    /**
     *
     *
     *   Workload -- expo
     *    |
     *   Prep     -- expo
     *
     * Check if expo is in above structure.
     * If it is, don't send order to expo
     * @return
     */
    private boolean isExpoInPrepWithWorkload(String expoStationID)
    {
        ArrayList<KDSStationIP> arPrep = m_stationsConnection.getRelations().getStationsWhoUseMeAsExpo(expoStationID);
        for (int i=0; i< arPrep.size(); i++)
        {
            KDSStationIP station = arPrep.get(i);
            SettingsBase.StationFunc func = m_stationsConnection.getRelations().getStationFunctionForBackoffice(station.getID());
            if (func == SettingsBase.StationFunc.Prep)
            {
                //check if its workload has expo
                ArrayList<KDSStationIP> arWorkload = m_stationsConnection.getRelations().getItsWorkLoadStation(station.getID());
                for (int j=0; j< arWorkload.size(); j++)
                {
                    if (m_stationsConnection.getRelations().getItsExpoStation(arWorkload.get(j).getID()).size()>0)
                        return true;
                }

            }
            else if (func == SettingsBase.StationFunc.Workload)
            {
                //check if its primary(prep) has expo
                ArrayList<String> arPrimary = m_stationsConnection.getRelations().getStationsWhoUseMeAsWorkload(station.getID());
                for (int j=0; j< arPrimary.size(); j++)
                {
                    if (m_stationsConnection.getRelations().getItsExpoStation(arPrimary.get(j)).size()>0)
                        return true;
                }
            }
            else
            {
                return false;
            }
        }
        return false;
    }

    /**
     *
     * @param logTemplate
     *  Contains #: order id
     *           $: for ip.
     *
     * @param ip
     *      For which ip. For tcp/ip data source.
     * @param order
     *      Received this order.
     * @return
     *  The new string with all parameters value.
     */
    public String buildOrderLog(String logTemplate,String ip,  KDSDataOrder order)
    {
        String msg = logTemplate;
        if (order != null)
            msg = msg.replace("#", "#"+order.getOrderName());
        if (!ip.isEmpty())
            msg = msg.replace("$", "[" + ip + "]");

        String s = "";
        if (order != null) {
            ArrayList<KDSToStation> ar = KDSDataOrder.getOrderTargetStations(order);

            if (ar.size() > 0) {
                for (int i = 0; i < ar.size(); i++) {
                    if (!s.isEmpty())
                        s += ",";
                    s += ar.get(i).getString();
                }
            }
        }
        
        if (!s.isEmpty())
            msg += (",go to stations:[" + s + "]");
        return msg;
    }

    /**
     * REturn expo type stations that relate to given stations.
     * @param arPrepStations
     * @return
     */
    private ArrayList<KDSToStation> getAllAcceptAnyItemsStations(ArrayList<KDSToStation> arPrepStations)
    {
        int ncount = m_stationsConnection.getRelations().getRelationsSettings().size();

        ArrayList<KDSToStation> ar = new ArrayList<>();


        for (int i=0; i< ncount; i++) {
            KDSStationsRelation stationRelation = m_stationsConnection.getRelations().getRelationsSettings().get(i);
            if (stationRelation.getFunction() == KDSRouterSettings.StationFunc.Expeditor ||
                    stationRelation.getFunction() == KDSRouterSettings.StationFunc.Queue_Expo ||
                    stationRelation.getFunction() == KDSRouterSettings.StationFunc.TableTracker ||
                    stationRelation.getFunction() == KDSRouterSettings.StationFunc.Runner ||
                    stationRelation.getFunction() == KDSRouterSettings.StationFunc.Summary
            )
            {
                if (stationRelation.getFunction() == SettingsBase.StationFunc.Expeditor)
                {//workload: prep and workload all with expo, we don't sen order to expo.
                    //let the prep to handle this order.
                    if (isExpoInPrepWithWorkload(stationRelation.getID()))
                        continue;
                }

                String expoTypeStationID = stationRelation.getID();
                ArrayList<KDSStationIP> prepStations = m_stationsConnection.getRelations().getPrepStationsWhoUseMeAsExpo(expoTypeStationID);
                if (prepStationIsInArray(arPrepStations, prepStations)) {

                    KDSToStation toStation = new KDSToStation();
                    toStation.setPrimaryStation(stationRelation.getID());
                    ar.add(toStation);
                }
            }

        }
        return ar;

    }

    private boolean prepStationIsInArray(ArrayList<KDSToStation> arPrepStations,
                                         ArrayList<KDSStationIP> prepStations)
    {
        for (int i=0; i< arPrepStations.size(); i++)
        {
            KDSToStation toStation = arPrepStations.get(i);
            for (int j=0; j< prepStations.size(); j++)
            {
                KDSStationIP stationIP = prepStations.get(j);
                if (stationIP.getID().equals(toStation.getPrimaryStation()) ||
                        stationIP.getID().equals(toStation.getSlaveStation()))
                    return true;
            }
        }
        return false;
    }

    public void writeToAllStationsWithOrderLog(String xmlData,String orderName)
    {
        int ncount = m_stationsConnection.getRelations().getRelationsSettings().size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationsRelation stationRelation = m_stationsConnection.getRelations().getRelationsSettings().get(i);
            //kpp1-426, double qty issue.
            if (stationRelation.getFunction() == SettingsBase.StationFunc.Backup)
                continue;
            //write to the expo stations too!!!! 20160222
            //Just let expo receive item through the <kdsstation> tag
            //if (stationRelation.getFunction() == KDSRouterSettings.StationFunc.Expeditor)
            //    continue;
            //String stationID = stationRelation.getID();

            KDSStationIP station = stationRelation.getStationIP();
            //in relations settings, the station port is 3001 for communication of each other.
            //So, I have to change its port before send data.
            station.setPort(getSettings().getString(KDSRouterSettings.ID.KDSRouter_Connect_Station_IPPort));

            KDSLog.d(TAG, "Write to KDSStation #" +station.getID() + ",ip=" +station.getIP() + ", port="+station.getPort() +",length="+xmlData.length());

            String info = String.format("Order ID [%s] Sent to KDS Station ID [%s] IP=[%s]", orderName, station.getID(), station.getIP());

            KDSLog.order("",info );

            //if (m_stationsConnection.findActivedStationByID(stationID) != null)
            m_stationsConnection.writeDataToStationOrItsBackup(station, xmlData);
        }

    }

    public void writeToAllStationsWithOrderLog(String xmlData,String orderName, ArrayList<String> toStations)
    {
        int ncount = toStations.size();// m_stationsConnection.getRelations().getRelationsSettings().size();

        for (int i=0; i< ncount; i++)
        {
            String stationID = toStations.get(i);//stationRelation.getID();
            KDSStationIP station  = null;
            station = m_stationsConnection.getRelations().findStationInRelationshipByID(stationID);
            if (station == null)
            {
                station = m_stationsConnection.findActivedStationByID(stationID);
            }
            if (station == null)
            {
                String s = String.format("Order ID [%s] target station [%s] is not existed", orderName, stationID);
                KDSLog.order("",s);

                //kpp1-387
                //save it. Station is not existed in table, and it is not online.
                // Just save its data, maybe this station will come back soon.
                m_stationsConnection.writeGhostStionDataToBuffer(stationID, xmlData, MAX_BUFFER_DATA_COUNT_FOR_WAITING_CONNECTION);
            }
            else {
                station.setPort(getSettings().getString(KDSRouterSettings.ID.KDSRouter_Connect_Station_IPPort));
                KDSLog.d(TAG, "Write to KDSStation #" + station.getID() + ",ip=" + station.getIP() + ", port=" + station.getPort() + ",length=" + xmlData.length());
                m_stationsConnection.writeDataToStationOrItsBackup(station, xmlData, MAX_BUFFER_DATA_COUNT_FOR_WAITING_CONNECTION);
                String info = String.format("Order ID [%s] Sent to KDS Station ID [%s] IP=[%s]", orderName, station.getID(), station.getIP());
                KDSLog.order("",info );
            }

        }

    }

}
