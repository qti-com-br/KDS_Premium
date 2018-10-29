package com.bematechus.kdslib;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/14.
 */
public class KDSBase {

    static final String TAG = "KDSBase";

    public interface StationAnnounceEvents
    {
        void onReceivedStationAnnounce(KDSStationIP stationReceived);//String stationID, String ip, String port, String mac);
    }

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
            m_stationAnnounceThread = new Thread(m_stationAnnounceRunnable);
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
}
