package com.bematechus.kdslib;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by David.Wong on 2019/6/5.
 * Rev:
 */
public class AckDataStation {

    String m_strStationID = "";
    Vector<AckData> m_arData = new Vector<>();
    Object m_locker = new Object();

    public AckDataStation(String stationID)
    {
        setStationID(stationID);
    }
    public String getStationID()
    {
        return m_strStationID;
    }
    public void setStationID(String stationID)
    {
        m_strStationID = stationID;
    }
    public Vector<AckData> getData()
    {
        return m_arData;
    }

    public AckData add(String strXml)
    {
        synchronized (m_locker) {
            if (m_arData.size() > AckManager.MAX_ACK_COUNT)
                return null;
        }
        AckData data = new AckData(strXml);
        //String guid = data.getGuid();
        synchronized (m_locker) {
            getData().add(data);
        }
        return data;
    }
    public boolean remove(String ackGuid)
    {
        synchronized (m_locker)
        {
            for (int i=0; i< m_arData.size(); i++)
            {
                if (m_arData.get(i).getGuid().equals(ackGuid)) {
                    m_arData.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param arReturn
     * @param nMaxReturnCount
     *  -1: no limit
     * @return
     */
    public int findTimeoutData(ArrayList<AckData> arReturn, int nMaxReturnCount)
    {
        synchronized (m_locker)
        {
            for (int i=0; i< m_arData.size(); i++)
            {
                if (m_arData.get(i).isTimeout()) {
                    arReturn.add(m_arData.get(i));
                    if (nMaxReturnCount >0) {
                        if (arReturn.size() >= nMaxReturnCount)
                            return arReturn.size();
                    }

                }
            }
        }
        return arReturn.size();
    }
}
