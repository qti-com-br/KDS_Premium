package com.bematechus.kdslib;

/**
 * Timeslot data for report.
 */
public class SOSReportTimeSlotData {

    //String m_stationID;
    float m_nCount = 0;
    float m_bumpTimeSeconds = 0;

    float m_nOverTargetCount = 0;
    float m_nOverTargetSeconds = 0;

    public SOSReportTimeSlotData( )
    {

    }

        public SOSReportTimeSlotData(float counter, int bumpTime)
    {

        m_nCount = counter;
        m_bumpTimeSeconds = bumpTime;
    }
    public void setCount(float counter)
    {
        m_nCount = counter;
    }
    public float getCount()
    {
        return m_nCount;
    }

    public void setOverTargetCount(float nCount)
    {
        m_nOverTargetCount = nCount;
    }
    public float getOverTargetCount()
    {
        return m_nOverTargetCount;
    }

    public void setOverTargetSeconds(float nSeconds)
    {
        m_nOverTargetSeconds = nSeconds;
    }
    public float getOverTargetSeconds()
    {
        return m_nOverTargetSeconds;
    }

    public String getOverTargetCountString()
    {
        return KDSUtil.convertIntToString((int)getOverTargetCount());
    }

    public String getOverTargetSecondsString()
    {
        return KDSUtil.convertIntToString((int)getOverTargetSeconds());
    }

    public String getCountString()
    {
        return KDSUtil.convertIntToString((int)getCount());
    }
    public String getBumpTimeString()
    {
        return KDSUtil.convertIntToString((int)getBumpTimeSeconds());
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
    public float getAverageBumpTime()
    {
        if (m_nCount == 0)
            return 0;
        float flt = (m_bumpTimeSeconds/60 / m_nCount);
        //float flt = (m_bumpTimeSeconds/ m_nCount);

        return flt;
    }

    public String getAverageBumpTimeString()
    {
        float flt = getAverageBumpTime();
//        if (flt == 0)
//            return "";
        return KDSUtil.convertFloatToShortString(flt);
    }

    public void copyTo(SOSReportTimeSlotData data)
    {
        data.m_bumpTimeSeconds = m_bumpTimeSeconds;
        data.m_nCount = m_nCount;
        data.m_nOverTargetCount = m_nOverTargetCount;
        data.m_nOverTargetSeconds = m_nOverTargetSeconds;

    }
    public SOSReportTimeSlotData clone()
    {
        SOSReportTimeSlotData data = new SOSReportTimeSlotData();
        this.copyTo(data);
        return data;
    }

    public void reset()
    {
        m_nCount = 0;
        m_bumpTimeSeconds = 0;
        m_nOverTargetCount = 0;
        m_nOverTargetSeconds = 0;
    }
}
