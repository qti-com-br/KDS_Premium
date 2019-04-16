package com.bematechus.kdsrouter;

import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSSocketInterface;
import com.bematechus.kdslib.KDSSocketManager;
import com.bematechus.kdslib.KDSSocketMessageHandler;
import com.bematechus.kdslib.KDSStationActived;
import com.bematechus.kdslib.KDSStationConnection;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSStationsConnection;


import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/28 0028.
 */
//2015-12-28
public class KDSRouterStationsConnection extends KDSStationsConnection {

    ArrayList<RouterStation> m_arRouterStations = new ArrayList<>(); //all actived station save here, * Used in Router app

    public KDSRouterStationsConnection(KDSSocketManager manager, KDSSocketMessageHandler handler)
    {
        super(manager, handler);

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
        ArrayList<KDSStationConnection> ar = new  ArrayList<KDSStationConnection>();

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

        //conn.addBufferedData(strXml);////use kdsstationsconnection-->m_buffersForWaitingConnection to save offline data
        this.getNoConnectionBuffer().add(station.getID(), strXml, KDSStationsConnection.MAX_BACKUP_DATA_COUNT);

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

    }

    /**
     * * Used in Router app
     */
    public void routerReset()
    {
        m_arRouterStations.clear();
    }

    /**
     * * Used in Router app
     * @param router
     */
    public void routerAdd(RouterStation router)
    {
        m_arRouterStations.add(router);
    }

    /**
     * * Used in Router app
     * @param routerID
     * @return
     */
    public boolean routerExisted(String routerID)
    {
        for (int i=0; i< m_arRouterStations.size(); i++)
        {
            if (m_arRouterStations.get(i).getID().equals(routerID))
                return true;
        }
        return false;
    }

    /**
     * * Used in Router app
     * @param routerID
     * @param ip
     * @return
     */
    public RouterStation routerFind(String routerID, String ip)
    {
        for (int i=0; i< m_arRouterStations.size(); i++)
        {
            if (m_arRouterStations.get(i).getID().equals(routerID) &&
                    m_arRouterStations.get(i).getIP().equals(ip) )
                return m_arRouterStations.get(i);
        }
        return null;
    }

    /**
     * * Used in Router app
     * @return
     */
    public int routerCount()
    {
        return m_arRouterStations.size();
    }

    /**
     * * Used in Router app
     */
    public void routerRemoveInactive()
    {
        // ArrayList<KDSStationActived> ar = new ArrayList<>();

        int ncount = 0;
        //synchronized (m_activeLocker)
        {
            ncount = m_arRouterStations.size();
            for (int i = ncount - 1; i >= 0; i--) {
                if (m_arRouterStations.get(i).isTimeout(KDSConst.ACTIVE_PLUS_TIMEOUT)) {
                    //ar.add(m_arRouterStations.get(i)); //backup it.
                    m_arRouterStations.remove(i);
                }

            }
        }


    }

    /**
     * * Used in Router app
     */
    public void routerCheckAllNoResponse()
    {
        routerRemoveInactive();

    }

    /**
     * * Used in Router app
     * @param myStationIP
     * @return
     */
    public boolean routerOtherEnabled(String myStationIP)
    {
        routerCheckAllNoResponse();
        for (int i=0; i< m_arRouterStations.size(); i++)
        {
            if (m_arRouterStations.get(i).getIP().equals(myStationIP) )
                continue;
            if (m_arRouterStations.get(i).getBackupMode())
                continue;
            if (m_arRouterStations.get(i).getEnabled())
                return true;

        }
        return false;
    }


}
