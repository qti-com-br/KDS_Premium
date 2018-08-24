using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ReportViewer
{
public class TimeSlotEntry {

    String m_timeslotStart = "";
    List<TimeSlotEntryDetail> m_arData = new List<TimeSlotEntryDetail>();


    String m_strText = "";

    public List<TimeSlotEntryDetail> getData()
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
        m_arData.Add(d);



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
        float flt = m_arData[nIndex].getCounter();
        int n = (int)flt;
        if (n ==0) return "";
        return n.ToString();

    }

    public String getBumpTimeText(int nIndex)
    {
        float seconds =  m_arData[nIndex].getBumpTimeSeconds();
        int n = (int)seconds;
        if (n ==0) return "";
        return n.ToString();

    }
    public float getAverageBumpTime(int nIndex)
    {
        return m_arData[nIndex].getAverageBumpTime();

    }
    public String getAverageBumpTimeText(int nIndex)
    {
        return m_arData[nIndex].getAverageBumpTimeString();
    }
    public String getBumpTimeMinsText(int nIndex)
    {
        float flt =  ((float) m_arData[nIndex].getBumpTimeSeconds())/60;
        if (flt == 0)
            return "";
        String s =  KDSUtil.convertFloatToShortString(flt);
        return s;
    }

    public int getSize()
    {
        return m_arData.Count;
    }
    public int getTotalOrdersCount()
    {
        float flt = 0;
        for (int i=0; i< getSize(); i++)
        {
            flt += m_arData[i].getCounter();
        }
        return (int)flt;
    }

    public int getTotalBumpTime()
    {
        float flt = 0;
        for (int i=0; i< getSize(); i++)
        {
            flt += m_arData[i].getBumpTimeSeconds();
        }
        return (int)flt;
    }

    public float getOrderCount(int nIndex)
    {
        return m_arData[nIndex].getCounter();
    }
    public float getOrderBumpTimeSeconds(int nIndex)
    {
        return m_arData[nIndex].getBumpTimeSeconds();
    }

    public String getOrderCounterXmlText()
    {
        float flt = 0;
        String s = "";
        String strReturn = "";
        for (int i=0; i< getSize(); i++)
        {
            flt = m_arData[i].getCounter();
            s  = ((int)flt).ToString();
            if (strReturn.Length == 0)
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
            flt = m_arData[i].getBumpTimeSeconds();
            s  = KDSUtil.convertFloatToShortString(flt);
            if (strReturn.Length == 0)
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

}