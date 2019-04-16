package com.bematechus.kdslib;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2015/10/8 0008.
 */
public class KDSStationIP {
    String m_strID="";
    String m_strIP="";
    String m_strPort="";
    String m_strMac = "";
    int m_nStationContainItemsCount = 0; //-1: unknown
    int m_nUserMode = 0; //it is same as KDSSettings.KDSUserMode

    int m_nScreen = 0;//for multiple users

    String m_storeGuid = "";

    public void setStoreGuid(String storeGuid)
    {
        m_storeGuid = storeGuid;
    }
    public String getStoreGuid()
    {
        return m_storeGuid;
    }

    public void setScreen(int nScreen)
    {
        m_nScreen = nScreen;
    }
    public int getScreen()
    {
        return m_nScreen;
    }
    public void setUserMode(int nUserMode)
    {
        m_nUserMode = nUserMode;
    }
    public int getUserMode()
    {
        return m_nUserMode;
    }

    public void setStationContainItemsCount(int nCount)
    {
        m_nStationContainItemsCount = nCount;
    }
    public void setStationContainItemsCount(String strCount)
    {
        m_nStationContainItemsCount = KDSUtil.convertStringToInt(strCount, 0);
    }
    public int getStationContainItemsCount()
    {
        return m_nStationContainItemsCount;
    }
    public void setID(String id)
    {
        m_strID = id;
    }
    public String getID()
    {
        return m_strID;
    }
    public void setMac(String mac)
    {
        m_strMac = mac;
    }
    public String getMac()
    {
        return m_strMac;
    }

    public void setIP(String ip)
    {
        m_strIP = ip;
    }
    public String getIP()
    {
        return m_strIP;
    }

    public void setPort(String port)
    {
        m_strPort = port;
    }
    public String getPort()
    {
        return m_strPort;
    }

    public void copyFrom(KDSStationIP station)
    {
        this.setID(station.getID());
        this.setIP(station.getIP());
        this.setPort(station.getPort());
        this.setMac(station.getMac());
        this.setStationContainItemsCount(station.getStationContainItemsCount());
        this.setUserMode(station.getUserMode());
        this.setStoreGuid(station.getStoreGuid()); //kpp1-21

    }

    /**
     * stationID, ip,port,mac,ordersCount,usermode, storeguid
     * @return
     */
    @Override
    public String toString()
    {
        return m_strID + "," + m_strIP + ","+m_strPort + ","+m_strMac+","+KDSUtil.convertIntToString(m_nStationContainItemsCount) +","+ KDSUtil.convertIntToString(m_nUserMode) +","+m_storeGuid;
    }

    /**
     * stationID, ip,port,mac,ordersCount,usermode, storeguid
     * @param str
     * @return
     */
    static public  KDSStationIP parseString(String str)
    {
        KDSStationIP ff = new KDSStationIP();
        String s = new String(str);

        //station ID
        int n = s.indexOf(",", 0);
        if (n <0) return ff;
        ff.m_strID = s.substring(0, n);
        s = s.substring(n + 1);
        //ip
        n = s.indexOf(",", 0);
        if (n <0) return ff;
        ff.m_strIP =(s.substring(0, n ));
        s = s.substring(n + 1);
        //port
        n = s.indexOf(",", 0);
        if (n <0) return ff;
        ff.m_strPort =(s.substring(0, n ));
        s = s.substring(n + 1);
        //mac
        n = s.indexOf(",", 0);
        if (n <0) return ff;
        ff.m_strMac =(s.substring(0, n ));
        s = s.substring(n + 1);
        //items count
        n = s.indexOf(",", 0);
        if (n <0) return ff;
        ff.setStationContainItemsCount(s.substring(0, n ));
        s = s.substring(n + 1);
        //store guid
        ff.m_storeGuid = s;

        return ff;
    }

    public void mergeStation(KDSStationIP station)
    {
        if (!this.getIP().equals(station.getIP()))
        {
            if (this.getIP().isEmpty() && (!station.getIP().isEmpty()))
                this.setIP(station.getIP());
        }

        if (!this.getPort().equals(station.getPort()))
        {
            if (this.getPort().isEmpty() && (!station.getPort().isEmpty()))
                this.setPort(station.getPort());
        }
    }

    public boolean isEqual(KDSStationIP station)
    {
        if (!m_strMac.equals(station.getMac())) return false;
        if (!m_strIP.equals(station.getIP())) return false;
        if (!m_strPort.equals(station.getPort())) return false;
        if (!m_strID.equals(station.getID())) return false;
        return  true;
    }
    public boolean isRelationEqual(KDSStationIP station)
    {
        if (!m_strMac.equals(station.getMac())) return false;
        //if (!m_strIP.equals(station.getIP())) return false;
        //if (!m_strPort.equals(station.getPort())) return false;
        if (!m_strID.equals(station.getID())) return false;
        return  true;
    }
    static public KDSStationIP fromConnection(KDSStationConnection conn)
    {


        KDSStationIP station = new KDSStationIP();
        if (conn == null) return null;
        station.setPort(conn.getPort());
        station.setIP(conn.getIP());
        station.setID(conn.getID());
        return station;
    }

    static public void sortStations(List<KDSStationIP> ar)
    {
        Collections.sort(ar, new Comparator<KDSStationIP>() {
            @Override
            public int compare(KDSStationIP o1, KDSStationIP o2) {

                return (o1.getID().compareTo(o2.getID()));

            }
        });

    }
}
