/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bematechus.kdslib;

/**
 *
 * @author David.Wong
 * Item go to which station
 */
public class KDSToStation {
    public static int INVALID_VALUE = -1;
    private String m_strPrimaryStation;
    private int m_nPrimaryScreen;
    private String m_strSlaveStation;
    private int m_nSlaveScreen;
    
    public KDSToStation()
    {
        reset();
    }
    
    public void reset()
    {
        m_strPrimaryStation = "";
        m_nPrimaryScreen = INVALID_VALUE;
        m_strSlaveStation = "";
        m_nSlaveScreen = INVALID_VALUE;
    }
    public String getPrimaryStation()
    {
        return m_strPrimaryStation;
    }
    public void setPrimaryStation(String stationID)
    {
        m_strPrimaryStation = stationID;
    }
    
    public int getPrimaryScreen()
    {
        return m_nPrimaryScreen;
    }
    public void setPrimaryScreen(int nScreen)
    {
        m_nPrimaryScreen = nScreen;
    }
    
    public String getSlaveStation()
    {
        return m_strSlaveStation;
    }
    public void setSlaveStation(String stationID)
    {
        m_strSlaveStation = stationID;
    }
    
    public int getSlaveScreen()
    {
        return m_nSlaveScreen;
    }
    public void setSlaveScreen(int nScreen)
    {
        m_nSlaveScreen = nScreen;
    }
    
    public boolean containStation(String stationID)
    {
        if (stationID.isEmpty())
            return false;
        if (m_strPrimaryStation.equals(stationID) ||
            m_strSlaveStation.equals(stationID))
            return true;
        return false;
    }
    /***************************************************************************
     * 
     * @param strToStation
     *      format: [primary]:[screen]-[slave]:[screen]
     *      e.g: 1:0-3:1, 2, 5
     * @return 
     */
    public boolean parseString(String strToStation)
    {
        reset();
        if (strToStation.length()<=0)
            return false;
        strToStation = strToStation.trim();
        
        if (strToStation.equals("-1"))
            return true;
        String[] ar = strToStation.split("-");
        
        
        if (ar.length <=0)
            return false;
        String primary = ar[0];
        String slave = "";
        if (ar.length >1)
            slave = ar[1];
        String[] arPrimary = primary.split(":");
        if (arPrimary.length <=0)
            return false;
        m_strPrimaryStation =arPrimary[0];// KDSUtil.convertStringToInt(arPrimary[0], INVALID_VALUE);
        if (arPrimary.length >1)
            m_nPrimaryScreen = KDSUtil.convertStringToInt(arPrimary[1], INVALID_VALUE);
        if (slave.length()>0)
        {
            String[] arSlave = slave.split(":");
            if (arSlave.length <=0)
                return false;
            m_strSlaveStation =arSlave[0];// KDSUtil.convertStringToInt(arSlave[0],INVALID_VALUE);
            if (arSlave.length >1)
                m_nSlaveScreen = KDSUtil.convertStringToInt(arSlave[1],INVALID_VALUE);
        }
        return true;
    }
    /***************************************************************************
     * Primary-Screen:Slave-screen,
     * @return 
     */
    public String getString()
    {
        if (m_strPrimaryStation.isEmpty())// == INVALID_VALUE)
            return "";
        String s = m_strPrimaryStation;// KDSUtil.convertIntToString(m_nPrimaryStation);
        if (m_nPrimaryScreen != INVALID_VALUE)
            s += (":" + KDSUtil.convertIntToString(m_nPrimaryScreen) );
        if (!m_strSlaveStation.isEmpty())// != INVALID_VALUE)
            s += ("-" +m_strSlaveStation);// KDSUtil.convertIntToString(m_nSlaveStation) );
        if (m_nSlaveScreen != INVALID_VALUE)
            s += (":" + KDSUtil.convertIntToString(m_nSlaveScreen) );
        return s;
    }


            
}
