package com.bematechus.kdsstatistic;

import com.bematechus.kdslib.KDSSocketInterface;
import com.bematechus.kdslib.KDSSocketManager;
import com.bematechus.kdslib.KDSSocketMessageHandler;
import com.bematechus.kdslib.KDSStationActived;
import com.bematechus.kdslib.KDSStationConnection;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSStationsConnection;
import com.bematechus.kdslib.NoConnectionDataBuffers;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/28 0028.
 */
//2015-12-28
public class SOSStationsConnection extends KDSStationsConnection {


    //ArrayList<KDSStationConnection> m_arRouterStations = new ArrayList<>() ; //POS connects to this station

    public SOSStationsConnection(KDSSocketManager manager, KDSSocketMessageHandler handler)
    {
        super(manager, handler);
//        m_socketManager = manager;
//        m_socketEventHandler = handler;
    }

    public  boolean isExistedIpConnection(ArrayList<KDSStationConnection> ar, String ip)
    {
        int ncount = ar.size();
        for (int i=0; i< ncount ; i++)
        {
            if (ar.get(i).getIP().equals(ip))
                return true;
        }
        return false;
    }

    public ArrayList<KDSStationConnection> getRouterConnections()
    {
        ArrayList<KDSStationConnection> ar = new ArrayList<KDSStationConnection>();

        synchronized (m_connectionLocker) {
            int ncount = m_arConnection.size();
            for (int i=0; i< ncount; i++)
            {
                if (m_arConnection.get(i).getConnectionType() == KDSStationConnection.ConnectionType.Router_Station)
                {
                    if (m_arConnection.get(i).isConnected())
                    {
                        if (!isExistedIpConnection(ar, m_arConnection.get(i).getIP()))
                            ar.add(m_arConnection.get(i));
                    }
                }
            }

        }
        return ar;
    }

    public boolean connectToRouter(String routerID)
    {
        //1. find out the router ip
        KDSStationActived station =  this.findActivedStationByID(routerID);
        if (station == null)
            return false;
        KDSStationConnection conn = this.connectToStation(station);
        if (conn != null)
            conn.setConnectionType(KDSStationConnection.ConnectionType.Router_Station);
        return true;

    }
    public  KDSStationConnection connectWithData(KDSStationIP station, String strXml)
    {
        KDSStationConnection conn = KDSStationConnection.fromIPStation(station);
        m_buffersForWaitingConnection.add(station.getID(), strXml, NoConnectionDataBuffers.MAX_BACKUP_DATA_COUNT);
        //conn.addBufferedData(strXml);

        if (connectConnection(conn)) {
            synchronized (m_connectionLocker) {
                //KDSStationConnection connExisted = findConnectionByID(station.getID());
                m_arConnection.add(conn);
            }
        }
        return conn;
    }
    public boolean connectToRouterWithData(String routerID, String xmlData)
    {
        //1. find out the router ip
        KDSStationActived station =  this.findActivedStationByID(routerID);
        if (station == null)
            return false;
        KDSStationConnection conn = this.connectWithData(station, xmlData);
        if (conn != null)
            conn.setConnectionType(KDSStationConnection.ConnectionType.Router_Station);
        return true;

    }

    /**
     * 2015-12-29
     * @param sock
     * @param sockClient
     */
    public void onAcceptRouterConnection(KDSSocketInterface sock, Object sockClient)
    {
        KDSStationConnection conn = onAcceptStationConnection(sock, sockClient);
        if (conn != null)
            conn.setConnectionType(KDSStationConnection.ConnectionType.Router_Station);
//
//        KDSSocketTCPSideServer c = (KDSSocketTCPSideServer)sockClient;
//        String ip = c.getSavedRemoteIP();
//        KDSStationConnection station = new KDSStationConnection();
//        station.setIP(ip);
//        station.setSock(c);
//
//        ArrayList<KDSStationConnection> ar = null;
//
//        ar = m_arRouterStations;//.add(station);
//
//
//        synchronized (m_connectionLocker) {
//            KDSStationConnection s = findStationByIP(ar, ip);
//            if (s == null)
//                ar.add(station);
//            else {
//                if (s.getSock() != null)
//                    s.getSock().close();
//                s.setSock(c);
//            }
//        }
    }

}
