package com.bematechus.kdslib;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * SAME AS KDS FILE
 */

/**
 * Daily report condition
 */
public class ConditionDailyReport {

    ConditionOneTime.TimeSlot m_timeSlot = ConditionOneTime.TimeSlot.hr1;
    Date m_dt = new Date();
    Date m_tmFrom = new Date();
    Date m_tmTo = new Date();

    public void setTimeSlot(ConditionOneTime.TimeSlot t)
    {
        m_timeSlot = t;
    }
    public ConditionOneTime.TimeSlot getTimeSlot()
    {
        return m_timeSlot;
    }

    public void setDate(Date dt)
    {
        m_dt = dt;
    }
    public Date getDate()
    {
        return m_dt;
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


    public String getDateTimeFrom()
    {
        Calendar dateTimeFrom = Calendar.getInstance();
        dateTimeFrom.setTime(getDate());
        Calendar tmFrom = Calendar.getInstance();
        tmFrom.setTime(m_tmFrom);

        dateTimeFrom.set(dateTimeFrom.get(Calendar.YEAR), dateTimeFrom.get(Calendar.MONTH), dateTimeFrom.get(Calendar.DAY_OF_MONTH),
                tmFrom.get(Calendar.HOUR_OF_DAY),tmFrom.get(Calendar.MINUTE),tmFrom.get(Calendar.SECOND) );

        return KDSUtil.convertDateToString(dateTimeFrom.getTime());

    }
    public String getDateTimeTo()
    {
        Calendar dateTimeFrom = Calendar.getInstance();
        dateTimeFrom.setTime(getDate());
        Calendar tm = Calendar.getInstance();
        tm.setTime(m_tmTo);

        dateTimeFrom.set(dateTimeFrom.get(Calendar.YEAR), dateTimeFrom.get(Calendar.MONTH), dateTimeFrom.get(Calendar.DAY_OF_MONTH),
                tm.get(Calendar.HOUR_OF_DAY),tm.get(Calendar.MINUTE),tm.get(Calendar.SECOND) );

        return KDSUtil.convertDateToString(dateTimeFrom.getTime());
    }

    public int getTimeSlots(ArrayList<String> arFrom, ArrayList<String> arTo)
    {
        Date dt = getDate();

        Calendar date = Calendar.getInstance();
        date.setTime(dt);
        Calendar tmFrom = Calendar.getInstance();
        tmFrom.setTime(m_tmFrom);
        date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH),
                tmFrom.get(Calendar.HOUR_OF_DAY),tmFrom.get(Calendar.MINUTE),tmFrom.get(Calendar.SECOND) );

        int ncount = ConditionBase.getTimeSlotCount(getTimeSlot(), m_tmFrom, m_tmTo);
        for (int i=0; i< ncount; i++)
        {
            String strFrom = KDSUtil.convertDateToString(date.getTime());
            date = ConditionBase.getNextCalendar(date, getTimeSlot());
            String strTo = KDSUtil.convertDateToString(date.getTime());
            arFrom.add(strFrom);
            arTo.add(strTo);

        }
        return arFrom.size();

    }
    public boolean moveToNextDay()
    {
        Calendar date = Calendar.getInstance();
        date.setTime(m_dt);
        date.add(Calendar.DAY_OF_YEAR, 1);
        m_dt = date.getTime();
        return true;
    }

    public boolean moveToPrevDay()
    {
        Calendar date = Calendar.getInstance();
        date.setTime(m_dt);
        date.add(Calendar.DAY_OF_YEAR, -1);
        m_dt = date.getTime();
        return true;
    }

    public void export2Xml(KDSXML xml)
    {

        xml.newAttribute("dtfrom", KDSUtil.convertDateToDbString(m_dt));
        xml.newAttribute("timeslot", KDSUtil.convertIntToString(m_timeSlot.ordinal()));

        xml.newAttribute("tmfrom",KDSUtil.convertTimeToShortString( m_tmFrom));
        xml.newAttribute("tmto", KDSUtil.convertTimeToShortString( m_tmTo));

    }

    public void importFromXml(KDSXML xml)
    {
        xml.back_to_root();

        String s = "";
        s = xml.getAttribute("dtfrom", "");
        m_dt = KDSUtil.convertDbStringToDate((s));
        s = xml.getAttribute("timeslot", "");
        int n = KDSUtil.convertStringToInt(s, 0);
        m_timeSlot = ConditionBase.TimeSlot.values()[n];

        s = xml.getAttribute("tmfrom", "");
        m_tmFrom =  KDSUtil.convertShortStringToTime(s);

        s = xml.getAttribute("tmto", "");
        m_tmTo =  KDSUtil.convertShortStringToTime(s);


    }



}
