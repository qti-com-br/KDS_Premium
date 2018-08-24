package com.bematechus.kdslib;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 *One station data.
 */
public class SOSReportOneStation {



    String m_reportID = "";
    String m_strStationID = "";

    SOSReportTimeSlotData m_realTimeData = new SOSReportTimeSlotData();

    ArrayList<SOSReportTimeSlotData> m_arData = new ArrayList<>();

    //for report showing
    Date m_dtTimeslotStartShowing = new Date();
    SOSReportCondition m_conditionShowing = new SOSReportCondition();



    //////////////////////////////////////////////////////////////////////////////////////////

    public Date getTimeslotStartShowingDate()
    {
        return m_dtTimeslotStartShowing;
    }

    public SOSReportCondition getConditionForShowing()
    {
        return m_conditionShowing;
    }
    public void setConditionForShowing(SOSReportCondition c)
    {
        m_conditionShowing = c;
    }

    public String getTimeslotLabelForGraph(int nIndex)
    {
        int nIntervalSeconds = m_conditionShowing.getGraphPrepTimeInterval();
        int n = nIntervalSeconds * nIndex;
        Date dt = new Date();
        dt.setTime(getTimeslotStartShowingDate().getTime() - n *1000);
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        int nMinutes = c.get(Calendar.MINUTE);
        int nn =(nMinutes %10);
        if ( nn != 0 && nn !=5)
            return "";
        return getDateLabelString(dt);

    }
    public String getDateLabelString(Date dt)
    {
        return KDSUtil.convertTimeToShortString(dt);//convertTimeToDbString(dt);
    }
    public String getReportID()
    {
        return m_reportID;
    }
    public void setReportID(String guid)
    {
        m_reportID = guid;
    }

    public String getStationID()
    {
        return m_strStationID;
    }
    public void setStationID(String stationID)
    {
        m_strStationID = stationID;
    }


    public ArrayList<SOSReportTimeSlotData> getTimeslots()
    {
        return m_arData;
    }
//    public String getTimeSlotsFrom()
//    {
//        return m_timeslotStart;
//    }
//    public void setTimeSlotsFrom(String strTimeSlot)
//    {
//        m_timeslotStart = strTimeSlot;
//    }
    public void add( float ordersCount, int bumpSeconds)
    {
        SOSReportTimeSlotData d = new SOSReportTimeSlotData( ordersCount, bumpSeconds);
        m_arData.add(d);
    }

    public void add(SOSReportTimeSlotData d )
    {

        m_arData.add(d);
    }


//    public String getCounterText(int nIndex)
//    {
//        if (nIndex >=m_arData.size()) return "";
//        float flt = m_arData.get(nIndex).getCount();
//        int n = (int)flt;
//        if (n ==0) return "";
//        return KDSUtil.convertIntToString(n);
//
//    }
//
//    public String getBumpTimeText(int nIndex)
//    {
//        float seconds =  m_arData.get(nIndex).getBumpTimeSeconds();
//        int n = (int)seconds;
//        if (n ==0) return "";
//        return KDSUtil.convertIntToString(n);
//
//    }
    public float getAverageBumpTime(int nIndex)
    {
        return m_arData.get(nIndex).getAverageBumpTime();

    }
    public String getAverageBumpTimeText(int nIndex)
    {
        return m_arData.get(nIndex).getAverageBumpTimeString();
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
            flt += m_arData.get(i).getCount();
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
        return m_arData.get(nIndex).getCount();
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
            flt = m_arData.get(i).getCount();
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
//
//    public String getPrepTimeXmlText()
//    {
//        float flt = 0;
//        String s = "";
//        String strReturn = "";
//        for (int i=0; i< getSize(); i++)
//        {
//            flt = m_arData.get(i).getBumpTimeMinutes();
//            s  = KDSUtil.convertFloatToShortString(flt);
//            if (strReturn.isEmpty())
//                strReturn = s;
//            else
//            {
//                strReturn += ",";
//                strReturn += s;
//            }
//        }
//        return strReturn;
//    }

    static final String STR_SOSReport = "SOSReport";
    static final String STR_ID = "ID";
    static final String STR_Station = "Station";
    static final String STR_RealTime = "RealTime";
    static final String STR_Count = "Count";
    static final String STR_BumpTime = "BumpTime";
    static final String STR_Timeslot = "Timeslot";

    static final String STR_OverTargetCount = "OverCount";
    static final String STR_OverTargetSeconds = "OverSeconds";


    /**
     * xml format:
     *  <SOSReport id=3902939dl39i0 station=4>
     *      <RealTime Count=3 BumpTime=89></RealTime>
     *      <TimeSlot count=3 BumpTime=4>
     *      <TimeSlot count=3 BumpTime=4>
     *  </SOSReport>
     * @return
     */
    public String export2Xml()
    {
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root(STR_SOSReport);
        xml.newAttribute(STR_ID, getReportID());
        xml.newAttribute(STR_Station, getStationID());

        xml.newGroup(STR_RealTime, true);
        xml.newAttribute(STR_Count, m_realTimeData.getCountString());
        xml.newAttribute(STR_BumpTime, m_realTimeData.getBumpTimeString());
        xml.newAttribute(STR_OverTargetCount, m_realTimeData.getOverTargetCountString());
        xml.newAttribute(STR_OverTargetSeconds, m_realTimeData.getOverTargetSecondsString());

        xml.back_to_parent();
        for (int i=0; i< m_arData.size(); i++)
        {
            xml.newGroup(STR_Timeslot, true);
            xml.newAttribute(STR_Count, m_arData.get(i).getCountString());
            xml.newAttribute(STR_BumpTime, m_arData.get(i).getBumpTimeString());
            xml.back_to_parent();
        }

        return xml.get_xml_string();


    }

    public SOSReportTimeSlotData getRealTime()
    {
        return m_realTimeData;
    }

    /**
     *
     * @param strXml
     *  for its format see export2Xml function.
     * @return
     */
    static public SOSReportOneStation importFromXml(String strXml)
    {

        SOSReportOneStation report = new SOSReportOneStation();
        KDSXML xml = new KDSXML();
        xml.loadString(strXml);

        xml.back_to_root();//        .new_doc_with_root("StatisticReport");
        String id =  xml.getAttribute(STR_ID,"");// getCondition().getReportType().toString());
        if (id.isEmpty()) return null;
        report.setReportID(id);

        String s = "";
        s = xml.getAttribute(STR_Station, "");
        report.setStationID(s);


        xml.getFirstGroup(STR_RealTime);
        s = xml.getAttribute(STR_Count, "0");
        report.getRealTime().setCount(KDSUtil.convertStringToInt(s, 0));
        s = xml.getAttribute(STR_BumpTime, "0");
        report.getRealTime().setBumpTimeSeconds(KDSUtil.convertStringToInt(s, 0));

        //2.0.23
        s = xml.getAttribute(STR_OverTargetCount, "0");
        report.getRealTime().setOverTargetCount(KDSUtil.convertStringToInt(s, 0));

        s = xml.getAttribute(STR_OverTargetSeconds, "0");
        report.getRealTime().setOverTargetSeconds(KDSUtil.convertStringToInt(s, 0));

        xml.back_to_root();

        if (xml.getFirstGroup(STR_Timeslot))
        {
            do {

                SOSReportTimeSlotData entry = new SOSReportTimeSlotData();
                s = xml.getAttribute(STR_Count, "0");
               entry.setCount(KDSUtil.convertStringToInt(s, 0));
                s = xml.getAttribute(STR_BumpTime, "0");
                entry.setBumpTimeSeconds(KDSUtil.convertStringToInt(s, 0));

                report.getTimeslots().add(entry);
            }while (xml.getNextGroup(STR_Timeslot));
        }

        xml.back_to_parent();

        return report;
    }

    public boolean copyTo(SOSReportOneStation r)
    {

        r.setStationID(m_strStationID);
        r.setReportID(m_reportID );

        r.m_realTimeData = m_realTimeData.clone();
        r.m_arData.clear();
        for (int i=0; i< m_arData.size(); i++)
        {
            r.m_arData.add(m_arData.get(i).clone());
        }
        return true;
    }

    public SOSReportOneStation clone()
    {
        SOSReportOneStation r = new SOSReportOneStation();
        this.copyTo(r);
        return r;
    }

    /**
     * for showing this report
     * As the time we show it and the report received time is different,
     * we need to arrange its data again.
     *
     * The m_arData sequence: low: current time, high: old time.
     * @param condition
     *   The condition that used to create this station report
     * @param showEndTime
     */
    public void rebuildForStartTimeChanged(SOSReportCondition condition, Date showEndTime)
    {
        //check real time
        int nRealPeriod = condition.getRealPrepTimePeriodSeconds();

        if (showEndTime.getTime() - condition.getDeadline().getTime() > (nRealPeriod * 1000)) {
            this.m_realTimeData.reset();

        }

        int nGraphInterval = condition.getGraphPrepTimeInterval();
        if (nGraphInterval ==0) nGraphInterval = 1;
        long n = showEndTime.getTime() - condition.getDeadline().getTime();
        if (n > 0) {//current showing time is newer than created time.
            float fltSeconds = n / 1000;//to seconds
            int nTimeoutCount = (int) (Math.round(fltSeconds) / nGraphInterval);
            if (nTimeoutCount > 0) {
                //remove timeout data
                for (int i = 0; i < nTimeoutCount; i++) {
                    if (m_arData.size() > 0)
                        m_arData.remove(0);
                }
                //add zero values to it
                for (int i = 0; i < nTimeoutCount; i++) {
                    m_arData.add(new SOSReportTimeSlotData());
                }
            }
        }
        m_dtTimeslotStartShowing = showEndTime;
        m_conditionShowing = condition;

    }

    public String getRealTimePrepTimeString()
    {
        return m_realTimeData.getAverageBumpTimeString();
    }
}
