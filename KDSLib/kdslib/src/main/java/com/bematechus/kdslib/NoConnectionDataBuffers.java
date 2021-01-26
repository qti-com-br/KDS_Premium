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

    public static final int MAX_BACKUP_DATA_COUNT = 1000; //-1: no limitation
    //Vector<NoConnectionDataBuffer> m_arBufferForWaitingConnection = new Vector<>(); //KPP1-Coke
    KDSDBOffline m_offlineDB = KDSDBOffline.open(KDSApplication.getContext());

    public boolean add(String stationID, String strXml, int nMaxBufferCount)
    {
        if (nMaxBufferCount >0)
        {
            if (m_offlineDB.offlineDataCount(stationID) >nMaxBufferCount)
                return false;
        }
        return m_offlineDB.offlineAdd(stationID, strXml);

//
//        NoConnectionDataBuffer buffer = findStation(stationID);
//        if (buffer == null) {
//            buffer = new NoConnectionDataBuffer(stationID);
//            m_arBufferForWaitingConnection.add(buffer);
//        }
//        KDSStationDataBuffered data = buffer.addBufferedData(strXml, nMaxBufferCount);
//
//        return data;


    }

    /**
     *
     * @param stationID
     * @param strXml
     * @param orderGuid
     *  use it in onFinishSendBufferedData function.
     *   item/order transfering
     * @param itemGuid
     * @param nMaxBufferCount
     * @return
     */
    public boolean add(String stationID, String strXml,String orderGuid, String itemGuid,String strDescription, int nMaxBufferCount) {
        if (nMaxBufferCount > 0) {
            if (m_offlineDB.offlineDataCount(stationID) > nMaxBufferCount)
                return false;
        }
        return m_offlineDB.offlineAdd(stationID, strXml, orderGuid, itemGuid, strDescription);
    }

    public NoConnectionDataBuffer findStation(String stationID)
    {
        return m_offlineDB.offlineGet(stationID);
//        for (int i=0; i< m_arBufferForWaitingConnection.size(); i++)
//        {
//            if (m_arBufferForWaitingConnection.get(i).getStationID().equals(stationID))
//                return m_arBufferForWaitingConnection.get(i);
//        }
//        return null;
    }
    public boolean remove(String stationID)
    {
        return m_offlineDB.offlineRemove(stationID);
    }

    public ArrayList<String> getStationsOfflineAndAck()
    {
        return m_offlineDB.offlineAckGetStations();
    }

    public void clear()
    {
        m_offlineDB.clearOffline();

    }

    public boolean stationHasOfflineData(String stationID)
    {
        return m_offlineDB.offlineContains(stationID);
    }
}
