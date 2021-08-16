package com.bematechus.kdslib;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/14.
 */
public class KDSBase {

    static final String TAG = "KDSBase";
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

    /**
     *  for onKDSEvent
     */
    public enum KDSEventType
    {
        Received_rush_order, //params: 0: useA new order. 1: userB new order.
        TCP_listen_port_error, //params: 0: error message.
        Order_Bumped_By_Other_Expo_Or_Station, //kpp1-286
        Runner_LineItems_Show_New_Category,
        Refresh_pos_message,
        Network_state,
        Prep_park_order,
        Prep_unpark_order,
    }
    public interface KDSEvents {
        void onStationConnected(String ip, KDSStationConnection conn);
        void onStationDisconnected(String ip);
        void onAcceptIP(String ip);
        void onRetrieveNewConfigFromOtherStation();
        void onShowMessage(String message);
        void onReceiveNewRelations();
        void onReceiveRelationsDifferent();
        void onShowStationStateMessage(String stationID, int nState);
        //KDSRouter doesn't need following functions
        void onAskOrderState(Object objSource, String orderName);
        void onRefreshView(int nUserID, KDSDataOrders orders,KDSBase.RefreshViewParam nParam);//KDSUser.USER userID, KDSDataOrders orders, RefreshViewParam nParam); //nParam: 1: move focus to first order.
        void onShowMessage(KDSBase.MessageType msgType, String message);
        void onRefreshSummary(int nUserID);//KDSUser.USER userID);
        void onSetFocusToOrder(String orderGuid); //set focus to this order
        void onXmlCommandBumpOrder(String orderGuid);
        /**
         * In KDSRouter app
         * KPP1-305.Remove license restriction from Router
         * While network restored, check activation again.
         *  Use this function to get network restored event,
         *  I don't want to add new event function in router app.
         * @param orderName
         */
        void onTTBumpOrder(String orderName);

        /**
         * One events common definition, we don't need to add too many functions again.
         * @param evt
         * @param arParams
         * @return
         */
        Object onKDSEvent(KDSBase.KDSEventType evt, ArrayList<Object> arParams);

    }

//    public interface StationAnnounceEvents
//    {
//        void onReceivedStationAnnounce(KDSStationIP stationReceived);//String stationID, String ip, String port, String mac);
//    }

    protected StationAnnounceEvents m_stationAnnounceEvents = null;

    public void setStationAnnounceEventsReceiver(StationAnnounceEvents receiver)
    {
        m_stationAnnounceEvents = receiver;
    }

    protected void fireStationAnnounceReceivedEvent(KDSStationActived station)
    {
        if (m_stationAnnounceEvents != null)
            m_stationAnnounceEvents.onReceivedStationAnnounce(station);//id, ip, port, mac);
    }


    /**
     * call back from  m_stationsConnection.onIPConnected
     * @param
     * @param
     */
    public void onFinishSendBufferedData(KDSStationDataBuffered bufferedData)
    {

    }

    public void showMessage(String msg)
    {

    }

    protected void doStationAnnounce(String strAnnounce)
    {

    }

    public void announce_restore_pulse(String stationID, String stationIP)
    {

    }

    protected final int ANNOUNCE_MSG_SEND_EVENT = 0;
    protected final int ANNOUNCE_MSG_STATION_RESTORE = 1;
    protected Handler m_announceHander = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            KDSStationActived station = (KDSStationActived)msg.obj;
            if (msg.what == ANNOUNCE_MSG_SEND_EVENT) {
                fireStationAnnounceReceivedEvent(station);
//                if (m_stationAnnounceEvents != null)
//                    m_stationAnnounceEvents.onReceivedStationAnnounce(station);//id, ip, port, mac);
            }
            else if (msg.what == ANNOUNCE_MSG_STATION_RESTORE)
            {
                announce_restore_pulse(station.getID(),station.getIP());
            }
            return false;
        }
    });

    Thread m_stationAnnounceThread = null;
    StationAnnounceRunnable m_stationAnnounceRunnable = null;
    protected void doStationAnnounceInThread(String strInfo)
    {
        //2.1.15.1, use this code, as it has been tested
//        StationAnnounceRunnable r = new StationAnnounceRunnable(strInfo);
//        Thread t = new Thread(r);
//        t.start();
        //2.1.15.2
        if (m_stationAnnounceThread == null || (!m_stationAnnounceThread.isAlive()))
        {

            Log.d(TAG, "start announce thread");

            m_stationAnnounceRunnable = new StationAnnounceRunnable(strInfo);
            m_stationAnnounceThread = new Thread(m_stationAnnounceRunnable, "DoAnnounce");
            m_stationAnnounceThread.setPriority(Thread.MAX_PRIORITY-1);
            m_stationAnnounceThread.start();
        }
        else
        {

            m_stationAnnounceRunnable.append(strInfo);
        }

    }
    protected  class StationAnnounceRunnable implements Runnable
    {
//        String m_strStationAnnounce = "";
//
//        public StationAnnounceRunnable(String strAnnounce)
//        {
//            setAnnounce(strAnnounce);
//        }
//        public void setAnnounce(String strAnnounce)
//        {
//            m_strStationAnnounce = strAnnounce;
//        }
//
//
//        public void run()
//        {
//            doStationAnnounce(m_strStationAnnounce);
//        }


        ArrayList<String> m_arStationAnnounces = new ArrayList<>();
        private Object m_locker = new Object();

        public boolean append(String strAnnounce)
        {
            synchronized (m_locker) {
                if (KDSUtil.isExistedInArray(m_arStationAnnounces, strAnnounce))
                    return false;
                m_arStationAnnounces.add(strAnnounce);
                return true;
            }
        }

        public StationAnnounceRunnable(String strAnnounce)
        {
            append(strAnnounce);
        }

        public void run()
        {
            int ncount = 0;
            while (true) {

                synchronized (m_locker) {
                    ncount = m_arStationAnnounces.size();
                }
                if (ncount <= 0) {
                    try {

                        Thread.sleep(200);
                    } catch (Exception e) {

                    }
                    continue;
                }
                //deal with the announces
                for (int i = 0; i < ncount; i++) {
                    //doStationAnnounce(m_strStationAnnounce);
                    String s = m_arStationAnnounces.get(0);
                    synchronized (m_locker) {
                        m_arStationAnnounces.remove(0);
                    }
                    try {
                        doStationAnnounce(s);
                        Thread.sleep(10);
                    } catch (Exception e) {
                        KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
                    }
                }

            }

        }

    }

    /**
     * kpp1-312 Cannot receive orders on expo
     * @param nListenPort
     * @param errorMessage
     */
    protected void fireTcpListenServerErrorEvent(ArrayList<KDSEvents> evReceivers, int nListenPort, String errorMessage)
    {
        if (errorMessage.isEmpty()) return;

        String s = String.format("Errors listen TCP port: %d,  %s", nListenPort, errorMessage);
        if (evReceivers.size() >0) {
            ArrayList<Object> ar = new ArrayList<>();
            ar.add(s);
            for (int i = 0; i < evReceivers.size(); i++) {
                evReceivers.get(i).onKDSEvent(KDSBase.KDSEventType.TCP_listen_port_error, ar);
            }
        }
        else
        { //if no receiver, just show error, as this messaga is urgent.
            showToastMessage(s, Toast.LENGTH_LONG);

        }

    }

    static Toast m_toast = null;

    /**
     *  see https://stackoverflow.com/questions/51956971/illegalstateexception-of-toast-view-on-android-p-preview
     *  It will show IllegalStateException of toast View on Android P
     * @param message
     */
    static public void showToastMessage(String message, int duration) {
        //int duration = Toast.LENGTH_LONG;

        // cancel previous toast
        try {
            if (m_toast == null)
                m_toast = Toast.makeText(KDSApplication.getContext(), message, duration);
            else {
                // cancel same toast only on Android P and above, to avoid IllegalStateException on addView
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && m_toast.getView().isShown()) {
                    m_toast.cancel();
                }
                m_toast.setDuration(duration);
                m_toast.setText(message);
            }
            m_toast.show();
        } catch (Exception e) {
            //e.printStackTrace();
            m_toast = null;
        }

//        try {
//            if (m_toast == null)
//                m_toast = Toast.makeText(KDSApplication.getContext(), message, duration);
//            else
//                m_toast.setText(message);
//            if (m_toast != null)
//                m_toast.show();
//        }catch (Exception e)
//        {
//
//        }


    }

    static public boolean isExpoTypeStation(SettingsBase.StationFunc func)
    {
        if (func == SettingsBase.StationFunc.Expeditor ||
                func == SettingsBase.StationFunc.Queue_Expo ||
                func == SettingsBase.StationFunc.Runner ||
                func == SettingsBase.StationFunc.Summary
        )
            return true;
        else
            return false;
    }


}
