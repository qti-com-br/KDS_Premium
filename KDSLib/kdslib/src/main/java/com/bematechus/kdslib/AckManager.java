package com.bematechus.kdslib;

import android.util.Log;

import com.bematechus.kdslib.AckDataStation;

import java.util.Vector;

/**
 * Created by David.Wong on 2019/6/5.
 * Rev:
 */
public class AckManager {
    final String TAG = "AckManager";
    Vector<AckDataStation> m_arStationsAck = new Vector<>();


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
        Log.i(TAG, "Waiting ACK:" + stationID );
        AckDataStation station = getStation(stationID);
        if (station == null)
        {
            station = new AckDataStation(stationID);
            m_arStationsAck.add(station);
        }
        AckData data = station.add(strXml);
        if (data == null)
            return strXml;
        return data.getWithAckXmlData();
        //String s = ACKGUID + data.getGuid() + ACKGUID_END;
        //s += strXml;
        //return s;

    }

    public AckDataStation getStation(String stationID)
    {
        for (int i=0; i< m_arStationsAck.size(); i++)
        {
            if (m_arStationsAck.get(i).getStationID().equals(stationID))
                return m_arStationsAck.get(i);
        }
        return null;
    }

    public int getWaitingAckCount()
    {
        if (m_arStationsAck.size() <=0)
            return 0;
        int ncount = 0;
        for (int i=0; i< m_arStationsAck.size(); i++)
        {
            ncount += m_arStationsAck.get(i).getData().size();
        }
        return ncount;
    }
    public int getStationsCount()
    {
        return m_arStationsAck.size();
    }

    public Vector<AckDataStation> getStations()
    {
        return m_arStationsAck;
    }
}
