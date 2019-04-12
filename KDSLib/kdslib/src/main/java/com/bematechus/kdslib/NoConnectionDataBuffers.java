package com.bematechus.kdslib;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by David.Wong on 2019/2/15.
 * Use a given buffer to save no connection station data, these data will send again when connection build.
 * In old version, I save data in KDSStatioinConnection, but its instance released, all data lost.
 * Rev:
 */
public class NoConnectionDataBuffers {
    Vector<NoConnectionDataBuffer> m_arBufferForWaitingConnection = new Vector<>(); //KPP1-Coke

    public KDSStationDataBuffered add(String stationID, String strXml, int nMaxBufferCount)
    {
        NoConnectionDataBuffer buffer = findStation(stationID);
        if (buffer == null) {
            buffer = new NoConnectionDataBuffer(stationID);
            m_arBufferForWaitingConnection.add(buffer);
        }
        return buffer.addBufferedData(strXml, nMaxBufferCount);


    }
    public NoConnectionDataBuffer findStation(String stationID)
    {
        for (int i=0; i< m_arBufferForWaitingConnection.size(); i++)
        {
            if (m_arBufferForWaitingConnection.get(i).getStationID().equals(stationID))
                return m_arBufferForWaitingConnection.get(i);
        }
        return null;
    }
}
