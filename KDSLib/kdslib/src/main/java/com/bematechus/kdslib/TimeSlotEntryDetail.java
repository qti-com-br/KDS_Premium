package com.bematechus.kdslib;

/**
 * Created by Administrator on 2016/8/20.
 */
public class TimeSlotEntryDetail {

    String m_stationID;
    float m_nCounter;
    float m_bumpTimeSeconds;

    public TimeSlotEntryDetail(String stationID, float counter, int bumpTime)
    {
        m_stationID = stationID;
        m_nCounter = counter;
        m_bumpTimeSeconds = bumpTime;
    }
    public void setStationID(String stationID)
    {
        m_stationID = stationID;
    }
    public String getStationID()
    {
        return m_stationID;
    }
    public void setCounter(float counter)
    {
        m_nCounter = counter;
    }
    public float getCounter()
    {
        return m_nCounter;
    }
    public void setBumpTimeSeconds(float nSeconds)
    {
        m_bumpTimeSeconds = nSeconds;
    }
    public float getBumpTimeSeconds()
    {
        return m_bumpTimeSeconds;
    }
    public float getBumpTimeMinutes()
    {
        return (m_bumpTimeSeconds/(float) 60.0);
    }

}
