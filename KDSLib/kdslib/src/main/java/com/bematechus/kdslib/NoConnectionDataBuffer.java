package com.bematechus.kdslib;

import java.util.ArrayList;

/**
 * Created by David.Wong on 2019/2/15.
 * Rev:
 */
public class NoConnectionDataBuffer {
    String m_strStationID = "";
    ArrayList<KDSStationDataBuffered> m_arBufferedData = new ArrayList<>(); ///these data need to been send out after connection build.

    public NoConnectionDataBuffer(String stationID)
    {
        m_strStationID = stationID;
    }

    public ArrayList<KDSStationDataBuffered> getData()
    {
        return m_arBufferedData;
    }
    public String getStationID()
    {
        return m_strStationID;
    }

    /**
     *  For KPP1-Coke
     * @param data
     * @param nMaxCount
     *  If size more than this value, remove old data.
     *  -1: No limitation
     */
    public void addBufferedData(String data, int nMaxCount) {

        if (data.isEmpty()) return;

        m_arBufferedData.add(KDSStationDataBuffered.create(data));
        if (nMaxCount<0) return;
        if (m_arBufferedData.size() >nMaxCount)
        {
            int ncount = m_arBufferedData.size() - nMaxCount;
            for (int i=0; i< ncount; i++)
            {
                m_arBufferedData.remove(0);
            }
        }

    }
}
