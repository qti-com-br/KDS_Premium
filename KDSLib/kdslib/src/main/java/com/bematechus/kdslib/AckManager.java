package com.bematechus.kdslib;

import android.util.Log;

import com.bematechus.kdslib.AckDataStation;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by David.Wong on 2019/6/5.
 * Rev:
 */
public class AckManager {
    final String TAG = "AckManager";
    //Vector<AckDataStation> m_arStationsAck = new Vector<>();
    public static final int ACK_TIMEOUT = 20000;// 20 seconds
    public static final int MAX_ACK_COUNT = 1000;

    KDSDBOffline m_ackDB = KDSDBOffline.open(KDSApplication.getContext());


    /**
     *
     * @param stationID
     * @param strXml
     * @return
     *  <ACKGUID>92348529384592834598234</ACKGUID>
     *  <Transaction></Transaction>
     */
    public String add(String stationID, String strXml)
    {
        //Log.i(TAG, "Waiting ACK:" + stationID );
//
//        AckDataStation station = getStation(stationID);
//        if (station == null)
//        {
//            station = new AckDataStation(stationID);
//            m_arStationsAck.add(station);
//        }
//        AckData data = station.add(strXml);
//        if (data == null)
//            return strXml;
        int ncount =m_ackDB.ackCount(stationID);
        //Log.i(TAG, "Waiting ACK: station=" + stationID +",count=" + ncount );
//        if (KDSConst._DEBUG)
//            return strXml;

        if (ncount > MAX_ACK_COUNT) {
            KDSLog.e(TAG, KDSLog._FUNCLINE_() + "ACK size > MAX_ACK_COUNT");
            return strXml;
        }
        AckData data = new AckData(strXml);
        m_ackDB.ackAdd(stationID, data); //save to database
        return data.getWithAckXmlData();
        //String s = ACKGUID + data.getGuid() + ACKGUID_END;
        //s += strXml;
        //return s;

    }

//    public AckDataStation getStation(String stationID)
//    {
//
//        for (int i=0; i< m_arStationsAck.size(); i++)
//        {
//            if (m_arStationsAck.get(i).getStationID().equals(stationID))
//                return m_arStationsAck.get(i);
//        }
//        return null;
//    }
//
//    public int getWaitingAckCount()
//    {
//        if (m_arStationsAck.size() <=0)
//            return 0;
//        int ncount = 0;
//        for (int i=0; i< m_arStationsAck.size(); i++)
//        {
//            ncount += m_arStationsAck.get(i).getData().size();
//        }
//        return ncount;
//    }
//    public int getStationsCount()
//    {
//        return m_arStationsAck.size();
//    }

    public ArrayList<String> getAckStations()
    {
        return m_ackDB.ackGetStations();
    }

//    public Vector<AckDataStation> getStations()
//    {
//        return m_arStationsAck;
//    }

//    public boolean isWaitingAck()
//    {
//        if (m_arStationsAck.size() <=0)
//            return false;
//        int ncount = 0;
//        for (int i=0; i< m_arStationsAck.size(); i++)
//        {
//            if (m_arStationsAck.get(i).getData().size() >0)
//                return true;
//        }
//        return false;
//    }

    public boolean isWaitingAck()
    {
        return m_ackDB.ackIsWaiting();
    }

    public void removeAck(String stationID, String ackGuid)
    {
//        AckDataStation station =  getStation(stationID);
//        station.remove(ackGuid);
        m_ackDB.ackRemove(ackGuid);
    }

    public AckDataStation getTimeoutAck(String stationID, int nMaxCout)
    {
        return m_ackDB.ackGetTimeout(stationID, ACK_TIMEOUT, nMaxCout);
    }

    public boolean resetAckTime(String ackGuid)
    {
        return m_ackDB.ackResetTime(ackGuid);
    }

    public void clear()
    {
        m_ackDB.clearAck();
    }
}
