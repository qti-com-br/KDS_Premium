package com.bematechus.kds;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.TimeDog;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class TableTracker extends Handler implements KDSHttp.KDSHttpEvent {
    static public String TAG = "TableTracker->";
    public enum TT_Command
    {
        None,
        Authentication,
        Online,
        Active_Orders,
        Active_Order,
        Clear_Orders,
        Page_Order,
        Remove_Order,
    }
    public interface TT_Event
    {
        void onTTFindTT(String ip, int port);
        void onTTRetrievedAuthentication(String strAuthen);
        void onTTGatewayStatusChanged(boolean bOnline);
        void onTTRetrievedActiveOrders(ArrayList<TTOrder> ar);
        void onTTRetrievedActiveOrder(TTOrder order);
        void onTTNotifyActiveOrders(ArrayList<TTOrder> ar);
        void onTTNotifyOrderChanged(TTOrder order);
        void onTTError(String strError);
        void onTTResponseHttpError(HttpRequest request,int nHttpResponseCode, String errorMsg);
        void onTTReturnCode(HttpRequest request, TTReturnCode rc);
        void onTTOrderPaged(HttpRequest request);
        void onTTOrderRemoved(HttpRequest request);
        void onTTShowMessage(String strMsg);

    }

    TT_Event m_receiver = null;

    KDSWebSocketClient m_webSocket = null;
    String m_ipAddress = "";
    int m_nPort = 8000;
    final String TRACKER_DISCOVERY_TAG = "_tracker-http._tcp";

    static public final String TT_Authentication = "/api/v2/accesstoken/generate";
    static public final String TT_Online = "/api/v2";
    static public final String TT_Active_Orders = "/api/v2/activeorders";
    static public final String TT_Active_Order = "/api/v2/activeorders/";
    static public final String TT_Clear_Orders = "/api/v2/activeorders";
    static public final String TT_Page_Order = "/api/v2/activeorders/";
    static public final String TT_Remove_Order = "/api/v2/activeorders/";
    //final String TRACKER_DISCOVERY_TAG ="_ipp._tcp";// "_http._tcp";


    TimeDog m_tdStartSearchTT = new TimeDog();
    TimeDog m_tdStartRetrieveAuth = new TimeDog();

    KDSHttp m_http = new KDSHttp();

    MyResolveListener m_resolveListener = new MyResolveListener();
    String m_strAuthentication = "";

    public KDSHttp getHttp()
    {
        return m_http;
    }
    public TableTracker()
    {
        m_http.setReceiver(this);
    }

    public void setIPAddress(String ip)
    {
        m_ipAddress = ip;
    }

    static final int SHOW_MSG = 1;
    static final int CONNECT_WEBSOCKET = 2;
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what)
        {
            case SHOW_MSG:
            {
                String s =(String) msg.obj;
                showMessage(s);
            }
            break;
            case CONNECT_WEBSOCKET:
            {
                this.connectNotification();
            }
            break;
            default:
                break;
        }

    }
    public void showMessage(String strMsg)
    {
        if (m_receiver != null)
            m_receiver.onTTShowMessage(strMsg);
    }
    public void setTTReceiver(TT_Event receiver)
    {
        m_receiver = receiver;
    }

    public String getAuthentication()
    {
        return m_strAuthentication;
    }

    private Context getApplicationContext()
    {
        return KDSApplication.getContext();
    }
    NsdManager.DiscoveryListener m_nsDicListener = null;

    public boolean isDiscoverTTStarted()
    {
        return (m_nsDicListener != null);
    }

    public boolean isFoundTT()
    {
        return (!m_ipAddress.isEmpty());
    }
    public void discoverService() {
        log2File( TAG + KDSLog._FUNCLINE_()+"Enter");
        if (m_nsDicListener != null) {
            log2File(TAG + KDSLog._FUNCLINE_() + "It has started");
            return;
        }

        m_tdStartSearchTT.reset();

        m_nsDicListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                m_nsDicListener = null;

                TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Stop Failed: type=" + serviceType + ", erroCode" + KDSUtil.convertIntToString(errorCode));

                Toast.makeText(getApplicationContext(), R.string.tt_discorvery_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                m_nsDicListener = null;
                TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Start Failed: type=" + serviceType + ", erroCode" + KDSUtil.convertIntToString(errorCode));
                Toast.makeText(getApplicationContext(),
                        R.string.tt_start_discovery_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                m_nsDicListener = null;
                TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Lost:" + serviceInfo.toString());
                Toast.makeText(getApplicationContext(), R.string.tt_service_lost, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {

                //TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Found: " + serviceInfo.toString());
                NsdManager nsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
                nsdManager.resolveService(serviceInfo,m_resolveListener );
                m_tdStartRetrieveAuth.reset();

                Toast.makeText(getApplicationContext(), R.string.tt_gateway_find, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                NsdManager nsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);

                m_nsDicListener = null;
                TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Stop Event: type=" + serviceType);
                Toast.makeText(getApplicationContext(), R.string.tt_discovery_stop, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Start Event: type=" + serviceType);
                Toast.makeText(getApplicationContext(), R.string.tt_discovery_started, Toast.LENGTH_SHORT).show();
            }
        };
        NsdManager nsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);

        nsdManager.discoverServices(TRACKER_DISCOVERY_TAG, NsdManager.PROTOCOL_DNS_SD,m_nsDicListener);

        log2File( TAG +KDSLog._FUNCLINE_()+"Exit");
    }
    protected void finalize()
    {
        NsdManager nsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
        if (m_nsDicListener != null)
            nsdManager.stopServiceDiscovery(m_nsDicListener);
    }
    public void clearAuthentication()
    {
        m_strAuthentication = "";
    }
    private void debug_authen()
    {
        m_http.setDebug(KDSHttp.Debug_Response.Authen);
    }


    public void retrieveAuthentication()
    {

        if (m_ipAddress.isEmpty()) {
            log2File(TAG +KDSLog._FUNCLINE_()+ "IP is empty");
            return;
        }
        HttpRequest r = new HttpRequest();
        r.m_url = String.format("http://%s:%d%s", m_ipAddress, m_nPort,TT_Authentication);
        r.methodGET();
        r.m_ttCommand = TT_Command.Authentication;

        m_http.request(r);


    }

    public void pageOrder(String trackerID)
    {
        log2File(TAG +KDSLog._FUNCLINE_()+ "---------guest_paging Order Enter----");
        if (!isValidAuthentication()) {
            log2File(TAG +KDSLog._FUNCLINE_()+ "Authen is empty");

            return;
        }
        if (m_ipAddress.isEmpty()) return;
        String strUrl = String.format("http://%s:%d%s", m_ipAddress,m_nPort,TT_Page_Order + trackerID + "/guest_paging" );
        HttpRequest r = new HttpRequest();
        r.methodPOST();
        r.m_authen = m_strAuthentication;
        r.m_ttCommand = TT_Command.Page_Order;
        r.m_url = strUrl;
        r.m_params = trackerID;
        m_http.request(r);
    }

    /**
     * DELETE /api/v2/activeorders/{name or UUID} [SECURED]
     * @param trackerID
     */
    public void removeOrder(String trackerID)
    {
        log2File(TAG +KDSLog._FUNCLINE_()+ "---------remove Order Enter----");
        if (!isValidAuthentication()) {
            log2File(TAG +KDSLog._FUNCLINE_()+ "Authen is empty");

            return;
        }
        if (m_ipAddress.isEmpty()) return;
        String strUrl = String.format("http://%s:%d%s", m_ipAddress,m_nPort,TT_Remove_Order + trackerID  );
        HttpRequest r = new HttpRequest();
        r.methodDELETE();
        r.m_authen = m_strAuthentication;
        r.m_ttCommand = TT_Command.Remove_Order;
        r.m_url = strUrl;
        r.m_params = trackerID;
        m_http.request(r);
    }

//    private void debug_orders()
//    {
//        String s = "{\n" +
//                "     \"status\": 200,\n" +
//                "     \"returnCode\": 5,\n" +
//                "     \"count\": 2,\n" +
//                "     \"items\": [\n" +
//                "     {\n" +
//                "     \"name\": \"2\",\n" +
//                "     \"uuid\": \"f8853c42-3c10-4636-81e3-a3aeb65d9219\",\n" +
//                "     \"orderType\": \"ON_PREMISE\",\n" +
//                "     \"locationName\": \"7\",\n" +
//                "     \"state\": \"started\",\n" +
//                "     \"created\": \"2016-09-22T19:08:52\",\n" +
//                "     \"stateChanged\": \"2016-09-22T19:08:52\",\n" +
//                "     \"paged\": false,\n" +
//                "     \"elapsedTime\": 0\n" +
//                "     },\n" +
//                "     {\n" +
//                "     \"name\": \"6\",\n" +
//                "     \"uuid\": \"98a3c77c-9aa0-4eea-a17a-93b2f905a6f1\",\n" +
//                "     \"orderType\": \"ON_PREMISE\",\n" +
//                "     \"locationName\": \"\",\n" +
//                "     \"state\": \"started\",\n" +
//                "     \"created\": \"2016-09-22T19:08:56\",\n" +
//                "     \"stateChanged\": \"2016-09-22T19:08:56\",\n" +
//                "     \"paged\": false,\n" +
//                "     \"elapsedTime\": 0\n" +
//                "     }\n" +
//                "     ]\n" +
//                "     }";
//
//        ArrayList<TTOrder> ar = TableTrackerJSON.parseOrdersJSON(s);
//        if (m_receiver != null)
//            m_receiver.onTTRetrievedActiveOrders(ar);
//    }

    public void retrieveActiveOrders()
    {

        //debug_orders();

        if (!isValidAuthentication()) {
            log2File(TAG +KDSLog._FUNCLINE_()+ "Authen is empty");

            return;
        }
        if (m_ipAddress.isEmpty()) return;

        HttpRequest r = new HttpRequest();
        r.methodGET();
        r.m_authen = m_strAuthentication;
        r.m_ttCommand = TT_Command.Active_Orders;
        r.m_url =  String.format("http://%s:%d%s", m_ipAddress, m_nPort,TT_Active_Orders );;

        m_http.request(r);


    }

    public void clearActiveOrders()
    {
        log2File(TAG +KDSLog._FUNCLINE_()+ "---------Clear all Enter----");
        if (!isValidAuthentication()) {
            log2File(TAG +KDSLog._FUNCLINE_()+ "Authen is empty");

            return;
        }
        if (m_ipAddress.isEmpty()) return;
        HttpRequest r = new HttpRequest();
        r.reset();
        r.methodDELETE();
        r.m_ttCommand = TT_Command.Clear_Orders;
        r.m_authen = m_strAuthentication;

        r.m_url = String.format("http://%s:%d%s", m_ipAddress, m_nPort,TT_Clear_Orders );
        m_http.request(r);

    }

    private boolean isValidAuthentication()
    {
        return (!m_strAuthentication.isEmpty());
    }


    final int CHECK_ONLINE_TIMEOUT = 5000;
    /**
     * this was called in kdsrouter class
     */
    public void onTimer()
    {
        if (m_checkOnlineTimeDog.is_timeout(CHECK_ONLINE_TIMEOUT))
            m_checkOnlineTimeDog.reset();
        else
            return;
        checkOnline();
        if (!isFoundTT())
        {
            showCanNotFindTT();
        }
        else {
            // if (!m_bOnline) return;
            if (m_strAuthentication.isEmpty()) {

                if (m_http.isRunning()) return;
                retrieveAuthentication();
                showCanNotGetTTAuth();
            } else {

            }
        }

    }

    final int SHOW_CANNOT_FIND_TT_TIMEOUT = 10000;
    private void showCanNotFindTT()
    {
        if (!isFoundTT()) {
            if (m_tdStartSearchTT.is_timeout(SHOW_CANNOT_FIND_TT_TIMEOUT)) {
                m_tdStartSearchTT.reset();
                Toast.makeText(KDSApplication.getContext(), R.string.tracker_can_not_find, Toast.LENGTH_SHORT).show();
            }
        }

    }

    final int SHOW_CANNOT_GET_TT_AUTH_TIMEOUT = 10000;
    private void showCanNotGetTTAuth()
    {
        if (m_strAuthentication.isEmpty()) {
            if (m_tdStartRetrieveAuth.is_timeout(SHOW_CANNOT_GET_TT_AUTH_TIMEOUT)) {
                m_tdStartRetrieveAuth.reset();
                Toast.makeText(KDSApplication.getContext(), R.string.tt_need_reset, Toast.LENGTH_LONG).show();
            }
        }

    }

    TimeDog m_responeTimeDog = new TimeDog();
    boolean m_bOnline = true;

    TimeDog m_checkOnlineTimeDog = new TimeDog();
    public void checkOnline()
    {
        if (m_responeTimeDog.is_timeout(10*1000)) {
            //m_bOnline = false;
            if (m_bOnline) {
                m_bOnline = false;
                if (m_receiver != null)
                    m_receiver.onTTGatewayStatusChanged(m_bOnline);

            }
        }
        if (!m_checkOnlineTimeDog.is_timeout(5*1000))
            return; //5 secs check once

        if (m_ipAddress.isEmpty()) return;
        if (m_http.isRunning()) return;
        m_checkOnlineTimeDog.reset();

        HttpRequest r = new HttpRequest();
        r.methodGET();
        r.m_ttCommand = TT_Command.Online;
        r.m_url = String.format("http://%s:%d%s", m_ipAddress, m_nPort,TT_Online );

        m_http.request(r);


    }

    /**
     * see return code: https://table-tracker.readme.io/docs/api-code-glossary
     * @param http
     * @param request
     */
    public void onHttpResponse(KDSHttp http, HttpRequest request)
    {
        //KDSSettings.saveTrackerAuthen("");
        m_responeTimeDog.reset();
       // boolean bTTSendOutError = false;
        String strResponse = request.m_result;
        int nHttpResponseCode = request.m_httpResponseCode;
        TTReturnCode rc = TableTrackerJSON.parseTTReturnCode(strResponse);
        if (rc!= null)
        {
            log2File(TAG +KDSLog._FUNCLINE_()+"Return return code message:"+ strResponse);
            if (m_receiver != null) {
               // bTTSendOutError = true;
                m_receiver.onTTReturnCode(request, rc);
            }
            if (rc.m_returnCode ==-1 || rc.m_returnCode == -2 || rc.m_returnCode == -3 ||rc.m_returnCode == -4)
            {//token error
                m_strAuthentication = "";
                KDSSettings.saveTrackerAuthen("");
            }
            if (!KDSHttp.isSuccessResponseCode( rc.m_status))
                return;
        }
        if (nHttpResponseCode == KDSHttp.HTTP_Exception)
        {
            if (m_receiver != null)
                m_receiver.onTTResponseHttpError(request,nHttpResponseCode, strResponse);
            return;
        }

        //now, I just use http to get authentication
        if (!KDSHttp.isSuccessResponseCode( nHttpResponseCode)) {


                String s = String.format("TableTracker http Error=%d", nHttpResponseCode);
                log2File(TAG + KDSLog._FUNCLINE_() + s);
                Toast.makeText(KDSApplication.getContext(), s, Toast.LENGTH_SHORT).show();
                if (m_receiver != null)
                    m_receiver.onTTResponseHttpError(request, nHttpResponseCode, "");


        }
        else {
            log2File(TAG +KDSLog._FUNCLINE_()+"Received Response:\n");
            log2File(TAG +KDSLog._FUNCLINE_()+strResponse);

            doResponse(request);

        }

    }
    public void doResponse(HttpRequest request)
    {
        if (!m_bOnline) {
            m_bOnline = true;
            if (m_receiver != null)
                m_receiver.onTTGatewayStatusChanged(m_bOnline);

        }
        String strResponse = request.m_result;
        switch (request.m_ttCommand)
        {
            case Online:
            {
                log2File(TAG +KDSLog._FUNCLINE_()+"CheckOnline result:"+ strResponse);
            }
            break;
            case Authentication:
            {
                log2File(TAG +KDSLog._FUNCLINE_()+"Authen Response: " + strResponse);
                String authen =  TableTrackerJSON.parseAuthenticationJSON(strResponse);
                log2File(TAG +KDSLog._FUNCLINE_()+"Authen Parsed result:"+ authen);
                if (authen.isEmpty()) return;
                if (m_strAuthentication.equals(authen))
                    return;
                else{
                    m_strAuthentication = authen;
                    if (m_receiver != null)
                        m_receiver.onTTRetrievedAuthentication(authen);
                }
            }
            break;
            case Active_Orders:
            {

                log2File(TAG +KDSLog._FUNCLINE_()+"Orders Response: " + strResponse);
                ArrayList<TTOrder> ar =  TableTrackerJSON.parseOrdersJSON(strResponse);
                log2File(TAG +KDSLog._FUNCLINE_()+"Orders Parsed result:"+ KDSUtil.convertIntToString( ar.size() ));
                String s = "";
                for (int i=0; i< ar.size(); i++)
                {
                    s += ar.get(i).toString();
                    s +="    \n";
                }
                log2File(TAG +KDSLog._FUNCLINE_()+s);

                if (m_receiver != null)
                    m_receiver.onTTRetrievedActiveOrders(ar);
            }
            break;
            case Active_Order:
            {
                log2File(TAG +KDSLog._FUNCLINE_()+"Order Response: " + strResponse);
                TTOrder order =  TableTrackerJSON.parseOrderJSON(strResponse);
                log2File(TAG +KDSLog._FUNCLINE_()+"Ordes Parsed result:"+ order.toString());

                if (m_receiver != null)
                    m_receiver.onTTRetrievedActiveOrder(order);
            }
            break;
            case Clear_Orders:
            {
                if (m_receiver != null)
                    m_receiver.onTTRetrievedActiveOrders(null); //clear

            }
            break;
            case Page_Order:
            {
                if (m_receiver != null)
                {
                    m_receiver.onTTOrderPaged(request);
                }
            }
            break;
            case Remove_Order:
            {
                if (m_receiver != null)
                {
                    m_receiver.onTTOrderRemoved(request);
                }
            }
            break;
            default:
                break;
        }
    }



    public boolean connectNotification()
    {

        try {
            String strUrl = String.format("ws://%s:8000/api/v2/websocket", m_ipAddress);
            URI url = new URI(strUrl);
            if (m_webSocket != null)
            {
                m_webSocket.setStopMe(true);
                m_webSocket.close();
            }
            m_webSocket = null;
            if (m_webSocket == null) {
                m_webSocket = new KDSWebSocketClient(url);
                m_webSocket.setAuthentication(m_strAuthentication);
            }
            else
            {
                if (m_webSocket.isOpen()) return true;
                if (m_webSocket.isConnecting()) return true;
                if (m_webSocket.isClosing()) return false;

                m_webSocket.close();
            }

            m_webSocket.connect();
            return true;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //log2File(TAG +KDSLog._FUNCLINE_()+"Error: "+e.toString());
            e.printStackTrace();
        }
        return false;
    }

    public boolean stopNotification()
    {
        if (m_webSocket!= null) {
            m_webSocket.setStopMe(true);
            m_webSocket.close();
        }
        return true;
    }

    public boolean onNotification(String strNotification)
    {
        //if (strNotification.indexOf(""))
        TableTrackerJSON.NotificationType notifyType =  TableTrackerJSON.parseNotificationType(strNotification);
        switch (notifyType)
        {

            case Unknown:
                if (m_receiver!= null)
                    m_receiver.onTTError("Notification content error");
                break;
            case Orders:
                ArrayList<TTOrder> ar = TableTrackerJSON.parseNotificationOrdersJSON(strNotification);
                if (m_receiver != null)
                    m_receiver.onTTNotifyActiveOrders(ar);
                break;
            case Created:
            case Modified:
            case Paged:
                TTOrder order = TableTrackerJSON.parseNotificationOrderJSON(strNotification);
                if (m_receiver != null)
                    m_receiver.onTTNotifyOrderChanged(order);
                break;
            case Error:
                if (m_receiver!= null)
                    m_receiver.onTTError(strNotification);
                break;
        }
        return true;
    }
    static public void log2File(String info)
    {

        KDSLog.d(TAG, info);
    }

    public void loadSavedAuthen()
    {
        m_strAuthentication = KDSSettings.loadTrackerAuthen();
    }


    public class KDSWebSocketClient extends WebSocketClient
    {
        String m_strAuthentication = "";
        boolean m_bStopMe = false;

        public KDSWebSocketClient(URI serverUri , Draft draft ) {
            super( serverUri, draft );
        }

        public KDSWebSocketClient( URI serverURI ) {
            super( serverURI );
        }
        final int RECEIVED_NOTIFY = 1;
        Handler m_handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RECEIVED_NOTIFY: {
                        TableTracker.this.onNotification((String) msg.obj);
                    }
                    break;
                    default:
                        break;
                }

            }
        };
        public void setStopMe(boolean bStop)
        {
            m_bStopMe = bStop;
        }
        public void setAuthentication(String strAuthentication)
        {
            m_strAuthentication = strAuthentication;
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {

            sendAuthentication();
            log2File(TAG +KDSLog._FUNCLINE_()+"KDSWebSocketClient: onOpen: " + serverHandshake.toString());
        }

        @Override
        public void onMessage(String s) {
            log2File(TAG +KDSLog._FUNCLINE_()+"KDSWebSocketClient: onMessage: \n" + s);
            Message m = new Message();
            m.what = RECEIVED_NOTIFY;
            m.obj = s;
            m_handler.sendMessage(m);
            //TableTracker.this.onNotification(s);
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            log2File(TAG +KDSLog._FUNCLINE_()+"KDSWebSocketClient: onClose:" + s);
            if (!m_bStopMe) {
                log2File(TAG +KDSLog._FUNCLINE_()+"KDSWebSocketClient: onClose: start to connect again");
                //TableTracker.this.connectNotification();
                try {
                    Thread.sleep(5000);
                }catch (Exception e)
                {
                    KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);
                }
                Message msg = new Message();
                msg.what = CONNECT_WEBSOCKET;
                TableTracker.this.sendMessage(msg);
            }
        }

        @Override
        public void onError(Exception e) {
            log2File(TAG +KDSLog._FUNCLINE_()+"KDSWebSocketClient: onError:" + e.toString());
            e.printStackTrace();
        }

        public void sendAuthentication(String strAuthentication)
        {
            String s = String.format("{\"accesstoken\": \"%s\"}", strAuthentication);
            this.send(s);
        }
        private void sendAuthentication()
        {
            sendAuthentication(m_strAuthentication);
        }
    }
    final int MSG_FIND_TT = 1;
    class MyResolveListener extends Handler implements NsdManager.ResolveListener
    {


        @Override
        public void handleMessage(Message msg) {

            switch (msg.what)
            {
                case MSG_FIND_TT:
                {
                    if (m_receiver != null)
                        m_receiver.onTTFindTT(m_ipAddress, m_nPort);

                }
                break;

                default:
                    break;
            }
        }

        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Called when the resolve fails.  Use the error code to debug.
            KDSLog.d(TAG, "Resolve failed" + errorCode);
        }


        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            KDSLog.d(TAG, "Resolve Succeeded. " + serviceInfo);

            if (serviceInfo.getServiceName().equals(m_ipAddress)) {
                KDSLog.d(TAG, "Same IP.");
                return;
            }
            //service = serviceInfo;
            int nPort = serviceInfo.getPort();

            String ip = serviceInfo.getHost().getHostAddress();
            if (nPort == m_nPort && m_ipAddress.equals(ip))
                return;
            m_nPort = serviceInfo.getPort();
            m_ipAddress = serviceInfo.getHost().getHostAddress(); // getHost() will work now
            Message msg = new Message();
            msg.what = MSG_FIND_TT;
            this.sendMessage(msg);

        }


    }
}
