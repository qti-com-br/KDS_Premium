package com.bematechus.kdslib;

import java.util.ArrayList;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

/**
 * Created by Administrator on 2015/9/1 0001.
 */
public class KDSStationConnection {
    public enum Stations_Type {

        For_Mirror,
        For_Primary,
        For_Slave,
        For_Exp
    } ;

    /**
     * 2015-12-29
     * For POS, or for normal KDS station. or it is the router staiton in kds routers
     */
    public enum ConnectionType{
        KDS_Station,
        POS_Station,
        Router_Station,
    };
    ConnectionType m_connectionType = ConnectionType.KDS_Station;

    ///////
    String m_strID = "";
    String m_strIP = "";
    String m_strPort = "";
    KDSSocketTCPSideBase m_sock;
    TimeDog m_timeStartConnecting = new TimeDog();

    //ArrayList<String> m_arBufferedData = new ArrayList<>(); ///these data need to been send out after connection build.
    ArrayList<KDSStationDataBuffered> m_arBufferedData = new ArrayList<>(); ///these data need to been send out after connection build.

    /********************************************************************************************/
    public KDSStationConnection() {

    }

    /**
     * 2015-12-29
     * @param t
     */
    public void setConnectionType(ConnectionType t)
    {
        m_connectionType = t;
    }

    /**
     * 2015-12-29
     * @return
     */
    public ConnectionType getConnectionType()
    {
        return m_connectionType;
    }

    public void addBufferedData(String data) {
        if (data.isEmpty()) return;
        m_arBufferedData.add(KDSStationDataBuffered.create(data));

    }

    public KDSStationDataBuffered addBufferedData(String data, String description) {
        if (data.isEmpty()) return null;
        KDSStationDataBuffered d = KDSStationDataBuffered.create(data, description);
        m_arBufferedData.add(d);
        return d;

    }

    public String popupBufferedData() {
        if (m_arBufferedData.size() > 0) {
            KDSStationDataBuffered data = m_arBufferedData.get(0);
            m_arBufferedData.remove(0);
            return data.getData();

        }
        else
            return "";

    }

    public KDSStationDataBuffered popupStationBufferedData() {
        if (m_arBufferedData.size() > 0) {
            KDSStationDataBuffered data = m_arBufferedData.get(0);
            m_arBufferedData.remove(0);
            return data;

        }
        else
            return null;
    }

    public int getBufferedCount() {
        return m_arBufferedData.size();
    }

    /**
     *
     * @param conn
     */
    public void copyBufferedData(KDSStationConnection conn)
    {
        int ncount = conn.getBufferedCount();
        for (int i=0; i< ncount; i++) {
            String s = conn.popupBufferedData();
            if (!s.isEmpty())
                this.addBufferedData(s);
        }
    }

    public void setID(String strID) {
        m_strID = strID;
    }

    public String getID() {
        return m_strID;
    }



    public void setIP(String strIP) {
        m_strIP = strIP;
    }

    public String getIP() {
        return m_strIP;
    }

    public void setPort(String strPort) {
        m_strPort = strPort;
    }

    public String getPort() {
        return m_strPort;
    }

    public void copyFrom(KDSStationConnection station) {
        m_strID = station.getID();
        m_strIP = station.getIP();
        m_strPort = station.getPort();
    }

    public KDSSocketTCPSideBase getSock() {
        return m_sock;
    }

    public void setSock(KDSSocketTCPSideBase sock) {
        m_sock = sock;
    }

    public void setStartConnectionTime() {
        m_timeStartConnecting.reset();
    }

    public boolean isConnectionTimeout() {
        if (getSock() == null)
            return true;
        if (getSock().isConnected())
            return false;
        if (getSock().getSocketChannel().isConnectionPending() ||
                (!getSock().isConnected())   )
            return (m_timeStartConnecting.is_timeout( KDSConst.CONNECTING_TIMEOUT));
        else
            return false;

    }

    /**
     *
     * @return
     * 0: it is not timeout
     * -1: socket is null
     *-2: pending timeout
     * -3: not connect
     * -4: pending and not connect
     * -5: unknow eeror
     */
    public int isConnectionTimeoutWithErrorCode() {
        if (getSock() == null)
            return -1;
        if (getSock().isConnected())
            return 0;
        boolean bPending = getSock().getSocketChannel().isConnectionPending();
        boolean bConnected = getSock().isConnected();
        if (bPending || (!bConnected)) {

            boolean b= (m_timeStartConnecting.is_timeout(KDSConst.CONNECTING_TIMEOUT));
            if (!b) return 0;
            if (bPending) return -2;
            if (!bConnected) return -3;
            if (bPending &&(!bConnected)) return -4;
            return -5;
        }
        else {
            return 0;
        }

    }

    /**
     *
     * @return
     * -1: socket null
     * 0: connected
     * 1: timeout
     * 2: it is not timeout
     * 3: others
     */
    public int isConnectingTimeout() {
        if (getSock() == null)
            return -1;
        if (getSock().isConnected())
            return 0;
        if (getSock().getSocketChannel().isConnectionPending() ||
                (!getSock().isConnected())   ) {
            boolean b = (m_timeStartConnecting.is_timeout(KDSConst.CONNECTING_TIMEOUT));
            if (b)
                return 1; //timout
            else
                return 2; //it is not timeout

        }
        else
            return 3;

    }

    /**
     * 2015-12-29
     * @return
     */
    public boolean isConnected()
    {
        if (getSock() == null)
            return false;
        return (getSock().isConnected());

    }

    public boolean isConnecting()
    {
        if (getSock() == null)
            return false;
        if (getSock().isConnected())
            return false;
        if (getSock() instanceof KDSSocketTCPSideClient)
        {
            return ((KDSSocketTCPSideClient)getSock()).isConnecting();
        }
        return (m_timeStartConnecting.is_timeout(5000));
    }
    public boolean isTimeout(int nms)
    {

        return (m_timeStartConnecting.is_timeout(nms));
    }
    @Override
    public String toString()
    {
        return m_strID + "," + m_strIP + ","+m_strPort;
    }
    static public  KDSStationConnection parseString(String str)
    {
        KDSStationConnection ff = new KDSStationConnection();
        String s = new String(str);
        int n = s.indexOf(",", 0);
        if (n <0) return ff;
        ff.m_strID = s.substring(0, n);
        s = s.substring(n + 1);

        n = s.indexOf(",", 0);
        if (n <0) return ff;
        ff.m_strIP =(s.substring(0, n ));
        s = s.substring(n + 1);


        ff.m_strPort = (s);

        return ff;
    }

    static public String getKey(KDSStationConnection.Stations_Type nType)
    {
        switch (nType)
        {
            case For_Mirror:
                return "stations_mirror";

            case For_Primary:
                return "stations_primary";

            case For_Slave:
                return "stations_slave";

            case For_Exp:
                return "stations_exp";
            default:
                return "";
        }
    }


    static public KDSStationConnection fromIPStation(KDSStationIP station)
    {
        KDSStationConnection s = new KDSStationConnection();
        s.setID(station.getID());
        s.setIP(station.getIP());
        s.setPort(station.getPort());
        return s;
    }

}
