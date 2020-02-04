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
    public KDSStationDataBuffered addBufferedData(String data, int nMaxCount) {

        if (data.isEmpty()) return null;
        if (nMaxCount >0 && m_arBufferedData.size() >nMaxCount) return null; //just keep first 100 data
        KDSStationDataBuffered d =KDSStationDataBuffered.create(data);
        m_arBufferedData.add(d);
        return d;
//        if (nMaxCount<0) return;
//        if (m_arBufferedData.size() >nMaxCount)
//        {
//            int ncount = m_arBufferedData.size() - nMaxCount;
//            for (int i=0; i< ncount; i++)
//            {
//                m_arBufferedData.remove(0);
//            }
//        }

    }

    public KDSStationDataBuffered popup() {
        if (m_arBufferedData.size() > 0) {
            KDSStationDataBuffered data = m_arBufferedData.get(0);
            m_arBufferedData.remove(0);
            return data;

        }
        else
            return null;
    }

    public int getSize()
    {
        return m_arBufferedData.size();
    }
}
