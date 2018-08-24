package com.bematechus.kdslib;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * SAME AS KDS FILE
 */

/**
 * weekly report condition
 */
public class ConditionWeekly {


    Date m_dtWeekFirstDay;
    Date m_tmFrom = new Date();
    Date m_tmTo = new Date();

    public ConditionWeekly()
    {
        setToCurrentWeek();
    }

    public void setTimeFrom(Date dt)
    {
        m_tmFrom = dt;
    }
    public Date getTimeFrom()
    {
        return m_tmFrom;
    }
    public String getTimeFromString()
    {
        return KDSUtil.convertTimeToDbString(getTimeFrom());
    }
    public void setTimeTo(Date dt)
    {
        m_tmTo = dt;
    }
    public Date getTimeTo()
    {
        return m_tmTo;
    }

    public String getTimeToString()
    {
         return KDSUtil.convertTimeToDbString(getTimeTo());
    }

    public void setTimeFromString(String strTime)
    {
        String s = "2016-01-01 " + strTime + ":00";
        Date dt = KDSUtil.convertStringToDate(s);
        m_tmFrom = dt;

    }


    public void setTimeToString(String strTime)
    {
        String s = "2016-01-01 " + strTime + ":00";
        Date dt = KDSUtil.convertStringToDate(s);
        m_tmTo = dt;

    }
    public void setWeekFirstDay(Date dt) {
        m_dtWeekFirstDay = dt;
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        int nFirstDayOfWeek = c.getFirstDayOfWeek();
        c.set(Calendar.DAY_OF_WEEK,nFirstDayOfWeek);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        m_dtWeekFirstDay = c.getTime();
    }

    public Date getWeekFirstDayDate()
    {
        return m_dtWeekFirstDay;
    }

    public String getWeekFirstDayDateString()
    {
        return KDSUtil.convertDateToString( m_dtWeekFirstDay);
    }
    public Date getWeekLastDayDate()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(m_dtWeekFirstDay);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.add(Calendar.DAY_OF_YEAR, 7);
        return c.getTime();
    }

    public String getWeekLastDayDateString()
    {
        return KDSUtil.convertDateToString( getWeekLastDayDate());
    }
    /**
     * @param nWhichWeekDay 1 -- 7
     * @return
     */
    public Date getWeek_Day_Start(int nWhichWeekDay) {
        Calendar c = Calendar.getInstance();
        c.setTime(m_dtWeekFirstDay);
        c.add(Calendar.DAY_OF_YEAR, nWhichWeekDay - 1);
        return c.getTime();
    }

    public Date getWeek_Day_End(int nWhichWeekDay) {
        Date dt = getWeek_Day_Start(nWhichWeekDay);
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }

    public void setToCurrentWeek() {
        Calendar currentWeekFirstDay = Calendar.getInstance();
        int nFirstDayOfWeek = currentWeekFirstDay.getFirstDayOfWeek();

        currentWeekFirstDay.set(Calendar.DAY_OF_WEEK,nFirstDayOfWeek);
        this.setWeekFirstDay(currentWeekFirstDay.getTime());
    }

    public boolean isFirstDayOfWeek()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(m_dtWeekFirstDay);

        int nFirstDayOfWeek = c.getFirstDayOfWeek();
        if (c.get(Calendar.DAY_OF_WEEK) == nFirstDayOfWeek)
            return true;
        return false;
    }
    public int getWeekDaySlots(ArrayList<String> arFrom, ArrayList<String> arTo)
    {
        if (!isFirstDayOfWeek())
            setToCurrentWeek();

        for (int i=1; i< 8; i++ )
        {
            String from =  KDSUtil.convertDateToString( getWeek_Day_Start(i));
            String to =  KDSUtil.convertDateToString(getWeek_Day_End(i));
            arFrom.add(from);
            arTo.add(to);
        }

        return arFrom.size();
    }

    public boolean nextWeek()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(m_dtWeekFirstDay);
        c.add(Calendar.DATE, 7);
        m_dtWeekFirstDay = c.getTime();
        return true;
    }

    public boolean prevWeek()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(m_dtWeekFirstDay);
        c.add(Calendar.DATE, -7);
        m_dtWeekFirstDay = c.getTime();
        return true;
    }

    public void export2Xml(KDSXML xml)
    {

        xml.newAttribute("dtfrom", KDSUtil.convertDateToDbString(m_dtWeekFirstDay));
        xml.newAttribute("tmfrom",KDSUtil.convertTimeToShortString( m_tmFrom));
        xml.newAttribute("tmto", KDSUtil.convertTimeToShortString( m_tmTo));


    }

    public void importFromXml(KDSXML xml)
    {
        xml.back_to_root();

        String s = "";
        s = xml.getAttribute("dtfrom", "");
        m_dtWeekFirstDay = KDSUtil.convertDbStringToDate((s));

        s = xml.getAttribute("tmfrom", "");
        m_tmFrom =  KDSUtil.convertShortStringToTime(s);

        s = xml.getAttribute("tmto", "");
        m_tmTo =  KDSUtil.convertShortStringToTime(s);


    }
}
