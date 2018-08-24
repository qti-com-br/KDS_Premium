package com.bematechus.kdslib;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/8/20.
 */
public class TimeSlotEntry {

    protected String m_timeslotStart = "";
    protected ArrayList<TimeSlotEntryDetail> m_arData = new ArrayList<>();


    String m_strText = "";

    public ArrayList<TimeSlotEntryDetail> getData()
    {
        return m_arData;
    }
    public String getTimeSlotsFrom()
    {
        return m_timeslotStart;
    }
    public void setTimeSlotsFrom(String strTimeSlot)
    {
        m_timeslotStart = strTimeSlot;
    }
    public void add(String stationID, float ordersCount, int bumpSeconds)
    {
        TimeSlotEntryDetail d = new TimeSlotEntryDetail(stationID, ordersCount, bumpSeconds);
        m_arData.add(d);

    }

    public void add(TimeSlotEntryDetail d )
    {

        m_arData.add(d);
    }

    public void setFixedText(String text)
    {
        m_strText = text;
    }
    public String getFixedText()
    {
        return m_strText;
    }

    public String getCounterText(int nIndex)
    {
        if (nIndex >=m_arData.size()) return "";
        float flt = m_arData.get(nIndex).getCounter();
        int n = (int)flt;
        if (n ==0) return "";
        return KDSUtil.convertIntToString(n);

    }

    public String getBumpTimeMinsText(int nIndex)
    {
        float flt =  ((float) m_arData.get(nIndex).getBumpTimeSeconds())/60;
        if (flt == 0)
            return "";
        String s =  KDSUtil.convertFloatToShortString(flt);
        return s;
    }

    public int getSize()
    {
        return m_arData.size();
    }
    public int getTotalOrdersCount()
    {
        float flt = 0;
        for (int i=0; i< getSize(); i++)
        {
            flt += m_arData.get(i).getCounter();
        }
        return (int)flt;
    }

    public int getTotalBumpTime()
    {
        float flt = 0;
        for (int i=0; i< getSize(); i++)
        {
            flt += m_arData.get(i).getBumpTimeSeconds();
        }
        return (int)flt;
    }

    public float getOrderCount(int nIndex)
    {
        if (nIndex >=m_arData.size()) return 0;
        return m_arData.get(nIndex).getCounter();
    }
    public float getOrderBumpTimeSeconds(int nIndex)
    {
        if (nIndex >=m_arData.size()) return 0;
        return m_arData.get(nIndex).getBumpTimeSeconds();
    }

    public String getOrderCounterXmlText()
    {
        float flt = 0;
        String s = "";
        String strReturn = "";
        for (int i=0; i< getSize(); i++)
        {
            flt = m_arData.get(i).getCounter();
            s  = KDSUtil.convertIntToString((int)flt);
            if (strReturn.isEmpty())
                strReturn = s;
            else
            {
                strReturn += ",";
                strReturn += s;
            }
        }
        return strReturn;
    }

    public String getPrepTimeXmlText()
    {
        float flt = 0;
        String s = "";
        String strReturn = "";
        for (int i=0; i< getSize(); i++)
        {
            flt = m_arData.get(i).getBumpTimeMinutes();
            s  = KDSUtil.convertFloatToShortString(flt);
            if (strReturn.isEmpty())
                strReturn = s;
            else
            {
                strReturn += ",";
                strReturn += s;
            }
        }
        return strReturn;
    }
}
