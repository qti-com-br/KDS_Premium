package com.bematechus.kdslib;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/3/15.
 */
public class SOSStationConfig {

    String m_strStationID = "";
    float m_fltTargetPrepTime = 0;
    boolean m_bShowIndividual = true;
    boolean m_bIncludedInOverall = true;

    public SOSStationConfig()
    {

    }
    public SOSStationConfig(String stationID)
    {
        setStationID( stationID );
    }

    public void setStationID(String stationID)
    {
        m_strStationID = stationID;
    }

    public String getStationID()
    {
        return m_strStationID ;
    }

    public void setTargetPrepTime(float flt)
    {
        m_fltTargetPrepTime = flt;
    }

    public float getTargetPrepTime()
    {
        return m_fltTargetPrepTime;//mins
    }

    public String getTargetPrepTimeString()
    {
        return KDSUtil.convertFloatToString( m_fltTargetPrepTime);
    }

    public void setShowIndividual(boolean bShow)
    {
        m_bShowIndividual = bShow;
    }
    public boolean getShowIndividual()
    {
        return m_bShowIndividual;
    }

    public void setIncludedInOverall(boolean bIncluded)
    {
        m_bIncludedInOverall = bIncluded;
    }
    public boolean getIncludedInOverall()
    {
        return m_bIncludedInOverall;
    }
    public String toString()
    {
        String s = getStationID();
        s += ",";
        s += KDSUtil.convertFloatToString(m_fltTargetPrepTime);
        s += ",";
        s += m_bShowIndividual?"1":"0";
        s += ",";
        s += m_bIncludedInOverall?"1":"0";
        return s;
    }
    static public SOSStationConfig parse(String str)
    {
        ArrayList<String> ar = KDSUtil.spliteString(str, ",");
        if (ar.size() != 4) return null;
        SOSStationConfig c = new SOSStationConfig();
        c.setStationID(ar.get(0));
        c.setTargetPrepTime( KDSUtil.convertStringToFloat(ar.get(1), 0));
        c.setShowIndividual(ar.get(2).equals("1"));
        c.setIncludedInOverall(ar.get(3).equals("1"));
        return c;

    }

}
