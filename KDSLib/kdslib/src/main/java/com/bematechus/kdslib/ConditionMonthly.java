package com.bematechus.kdslib;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * SAME AS KDS FILE
 */

/**
 * Monthly report condition
 */
public class ConditionMonthly {


    Date m_dtMonthFirstDay;
    Date m_tmFrom = new Date();
    Date m_tmTo = new Date();

    public ConditionMonthly()
    {
        setToCurrentMonth();
    }

    public void setTimeFrom(Date dt)
    {
        m_tmFrom = dt;
    }
    public Date getTimeFrom()
    {
        return m_tmFrom;
    }

    public void setTimeTo(Date dt)
    {
        m_tmTo = dt;
    }
    public Date getTimeTo()
    {
        return m_tmTo;
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
    public void setMonthFirstDay(Date dt) {
        m_dtMonthFirstDay = dt;
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.set(Calendar.DAY_OF_MONTH,1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        m_dtMonthFirstDay = c.getTime();
    }

    public Date getMonthFirstDay()
    {
        return m_dtMonthFirstDay;
    }
    public String getMonthFirstDayString()
    {
        return KDSUtil.convertDateToString(getMonthFirstDay());
    }

    public String getMonthLastDayString()
    {
        return KDSUtil.convertDateToString(getMonthLastDay());
    }

    public String getTimeFromString()
    {
        return KDSUtil.convertTimeToDbString(getTimeFrom());
    }
    public String getTimeToString()
    {
        return KDSUtil.convertTimeToDbString(getTimeTo());
    }

    public Date getMonthLastDay()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(m_dtMonthFirstDay);
        c.add(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH)-1);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }

    public int getMonthDays()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(m_dtMonthFirstDay);
        int day=c.getActualMaximum(Calendar.DATE);
        return day;
    }
    /**
     */
    public Date getMonth_Day_Start(int nWhichMonthDay) {
        Calendar c = Calendar.getInstance();
        c.setTime(m_dtMonthFirstDay);

        c.add(Calendar.DAY_OF_MONTH, nWhichMonthDay - 1);
        return c.getTime();
    }

    public Date getMonth_Day_End(int nWhichWeekDay) {
        Date dt = getMonth_Day_Start(nWhichWeekDay);
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }

    public void setToCurrentMonth() {
        Calendar currentMonthFirstDay = Calendar.getInstance();


        currentMonthFirstDay.set(Calendar.DAY_OF_MONTH,1);
        this.setMonthFirstDay(currentMonthFirstDay.getTime());
    }

    public boolean isFirstDayOfMonth()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(m_dtMonthFirstDay);


        int nFirstDayOfWeek = c.getFirstDayOfWeek();
        if (c.get(Calendar.DAY_OF_MONTH) == 1)
            return true;
        return false;
    }
    public int getMonthDaySlots(ArrayList<String> arFrom, ArrayList<String> arTo)
    {
        if (!isFirstDayOfMonth())
            setToCurrentMonth();


        for (int i=1; i< getMonthDays()+1; i++ )
        {
            String from =  KDSUtil.convertDateToString( getMonth_Day_Start(i));
            String to =  KDSUtil.convertDateToString(getMonth_Day_End(i));
            arFrom.add(from);
            arTo.add(to);
        }

        return arFrom.size();
    }

    public boolean nextMonth()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(m_dtMonthFirstDay);
        c.add(Calendar.MONTH, 1);
        m_dtMonthFirstDay = c.getTime();
        return true;
    }

    public boolean prevMonth()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(m_dtMonthFirstDay);
        c.add(Calendar.MONTH, -1);
        m_dtMonthFirstDay = c.getTime();
        return true;
    }

    public void export2Xml(KDSXML xml)
    {

        xml.newAttribute("dtfrom", KDSUtil.convertDateToDbString(m_dtMonthFirstDay));
        xml.newAttribute("tmfrom",KDSUtil.convertTimeToShortString(m_tmFrom));
        xml.newAttribute("tmto", KDSUtil.convertTimeToShortString( m_tmTo));

    }

    public void importFromXml(KDSXML xml)
    {
        xml.back_to_root();

        String s = "";
        s = xml.getAttribute("dtfrom", "");
        m_dtMonthFirstDay = KDSUtil.convertDbStringToDate((s));

        s = xml.getAttribute("tmfrom", "");
        m_tmFrom =  KDSUtil.convertShortStringToTime(s);

        s = xml.getAttribute("tmto", "");
        m_tmTo =  KDSUtil.convertShortStringToTime(s);


    }

}
