package com.bematechus.kdslib;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * SAME AS KDS FILE
 */

/**
 * For create one time statistic report.
 */
public class ConditionOneTime extends ConditionBase {

    public enum WeekDay
    {
        Sun,
        Mon,
        Tue,
        Wed,
        Thu,
        Fri,
        Sat,
        Unknown
    }

    public enum ReportArrangement
    {
        FullDate,
        PerMonth,
        PerWeek,
    }

    Date m_dtFrom;
    Date m_dtTo;

    boolean m_bDayOfWeek;
    WeekDay m_nDayOfWeek;
    ReportArrangement m_nReportArrangement;
    Date m_tmFrom;
    Date m_tmTo;

    TimeSlot m_nTimeSlot;

    public void setDateFrom(Date dt)
    {
        m_dtFrom = dt;
    }

    /**
     *
     * @param strDate
     *      yyyy-mm-dd
     */
    public void setDateFromString(String strDate)
    {
        String s = strDate;
        s += " 00:00:00";
        Date dt = KDSUtil.convertStringToDate(s);
        m_dtFrom = dt;
    }
    public Date getDateFrom()
    {
        return m_dtFrom;
    }

    public String getDateFromString()
    {
        return KDSUtil.convertDateToString(getDateFrom());
    }

    public String getDateToString()
    {
        return KDSUtil.convertDateToString(getDateTo());
    }

    public String getTimeFromString()
    {
        return KDSUtil.convertTimeToDbString(getTimeFrom());
    }
    public String getTimeToString()
    {
        return KDSUtil.convertTimeToDbString(getTimeTo());
    }

    public void setDateTo(Date dt)
    {
        m_dtTo = dt;
    }

    public void setDateToString(String strDate)
    {
        String s = strDate;
        s += " 23:59:59";
        Date dt = KDSUtil.convertStringToDate(s);

        m_dtTo = dt;
    }
    public Date getDateTo()
    {
        return m_dtTo;
    }

    public void setEnableDayOfWeek(boolean bEnable)
    {
        m_bDayOfWeek = bEnable;
    }
    public boolean getEnableDayOfWeek()
    {
        return m_bDayOfWeek;
    }

    public WeekDay getDayOfWeek()
    {
        return m_nDayOfWeek;
    }

    public void setDayOfWeek(WeekDay d)
    {
        m_nDayOfWeek = d;
    }

    public void setReportArrangement(ReportArrangement r)
    {
        m_nReportArrangement = r;
    }
    public ReportArrangement getReportArrangement()
    {
        return m_nReportArrangement;
    }

    public void setTimeFrom(Date from)
    {
        m_tmFrom = from;
    }

    /**
     *
     * @param strTime
     *      hh:mm
     */
    public void setTimeFromString(String strTime)
    {
        String s = "2016-01-01 " + strTime + ":00";
        Date dt = KDSUtil.convertStringToDate(s);
        m_tmFrom = dt;

    }

    public Date getTimeFrom()
    {
        return m_tmFrom;
    }

    public void setTimeTo(Date to)
    {
        m_tmTo = to;
    }

    public void setTimeToString(String strTime)
    {
        String s = "2016-01-01 " + strTime + ":00";
        Date dt = KDSUtil.convertStringToDate(s);
        m_tmTo = dt;

    }

    public Date getTimeTo()
    {
        return m_tmTo;
    }

    public void setTimeSlot(TimeSlot ts)
    {
        m_nTimeSlot = ts;
    }
    public TimeSlot getTimeSlot()
    {
        return m_nTimeSlot;
    }

    public Date setTime(Date dtOriginal, Date tm)
    {
        Calendar dt = Calendar.getInstance();
        dt.setTime(dtOriginal);
        Calendar toTm = Calendar.getInstance();
        toTm.setTime(tm);
        dt.set(Calendar.HOUR_OF_DAY, toTm.get(Calendar.HOUR_OF_DAY));
        dt.set(Calendar.MINUTE, toTm.get(Calendar.MINUTE));
        dt.set(Calendar.SECOND, toTm.get(Calendar.SECOND));
        return dt.getTime();
    }

    public boolean isWeekDay(Date dt, WeekDay weekDay)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        int n = c.get(Calendar.DAY_OF_WEEK);
        return (n == weekDay.ordinal()+1);

    }

    public int getDateSlots_FullDate(ArrayList<String> arFrom, ArrayList<String> arTo)
    {
        m_dtFrom = setHourTo0(m_dtFrom);
        m_dtFrom = setHourTo(m_dtFrom, m_tmFrom);
        Calendar dtStart = Calendar.getInstance();
        dtStart.setTime(m_dtFrom);

        m_dtTo = setHourTo23(m_dtTo);
        m_dtTo = setHourTo(m_dtTo, m_tmTo);
        Calendar dtEnd = Calendar.getInstance();
        dtEnd.setTime(m_dtTo);

        while (dtStart.getTimeInMillis() <dtEnd.getTimeInMillis() )
        {
            Date dt = dtStart.getTime();
            if (m_bDayOfWeek)
            {
                if (!isWeekDay(dt, m_nDayOfWeek)) {
                    dtStart.add(Calendar.DATE, 1);
                    continue;
                }
            }
            arFrom.add( KDSUtil.convertDateToString( dt));
            Date dtTo = dt;
            dtTo = setHourTo(dtTo, m_tmTo);
            arTo.add( KDSUtil.convertDateToString( getDayEnd(dtTo) ));
            dtStart.add(Calendar.DATE, 1);
        }
        return arFrom.size();
    }

    public int getDateSlots_PerMonth(ArrayList<String> arFrom, ArrayList<String> arTo)
    {
        m_dtFrom = setHourTo0(m_dtFrom);
        Calendar dtStart = Calendar.getInstance();
        dtStart.setTime(m_dtFrom);
        dtStart.set(Calendar.DAY_OF_MONTH, 1); //set to first day of month

        m_dtTo = setHourTo23(m_dtTo);
        Calendar dtEnd = Calendar.getInstance();
        dtEnd.setTime(m_dtTo);
        dtEnd.set(Calendar.DAY_OF_MONTH, dtEnd.getActualMaximum(Calendar.DAY_OF_MONTH)); //to end of month

        while (dtStart.getTimeInMillis() <dtEnd.getTimeInMillis() )
        {
            Date dt = dtStart.getTime();

            arFrom.add( KDSUtil.convertDateToString( dt));

            dtStart.set(Calendar.DAY_OF_MONTH, dtStart.getActualMaximum(Calendar.DAY_OF_MONTH));
            dt = dtStart.getTime();

            arTo.add( KDSUtil.convertDateToString( getDayEnd(dt) ));
            dtStart.set(Calendar.DAY_OF_MONTH, 1);//next month
            dtStart.add(Calendar.MONTH, 1);
        }
        return arFrom.size();
    }

    public int getDateSlots_PerMonth_DayOfWeek(ArrayList<DateSlots> dateSlots)
    {
        m_dtFrom = setHourTo0(m_dtFrom);
        Calendar dtStart = Calendar.getInstance();
        dtStart.setTime(m_dtFrom);
        dtStart.set(Calendar.DAY_OF_MONTH, 1); //set to first day of month

        m_dtTo = setHourTo23(m_dtTo);
        Calendar dtEnd = Calendar.getInstance();
        dtEnd.setTime(m_dtTo);
        dtEnd.set(Calendar.DAY_OF_MONTH, dtEnd.getActualMaximum(Calendar.DAY_OF_MONTH)); //to end of month

        Calendar c = Calendar.getInstance();

        while (dtStart.getTimeInMillis() <dtEnd.getTimeInMillis() )
        {
            Date dt = dtStart.getTime(); //month start

            DateSlots slot = new DateSlots();//for this month
            slot.setDateStart(dt);
            c.setTime(dt);
            //move to the first day_of_week.
            for (int i=0; i< 7; i++)
            {
                if (!isWeekDay(c.getTime(), m_nDayOfWeek))
                    c.add(Calendar.DAY_OF_MONTH, 1);
                else
                    break;
            }
            //max 5 week one month
            for (int i=0; i< 6; i++) {
                String from = KDSUtil.convertDateToString(c.getTime());
                String to = KDSUtil.convertDateToString(getDayEnd(c.getTime()));
                slot.add(from, to);

                c.add(Calendar.DAY_OF_MONTH, 7);
                if (c.get(Calendar.MONTH) != dtStart.get(Calendar.MONTH))
                    break;
            }
            dateSlots.add(slot); //this week.
            dtStart.add(Calendar.MONTH, 1);

        }
        return dateSlots.size();
    }


    public int getDateSlots_PerWeek(ArrayList<String> arFrom, ArrayList<String> arTo)
    {
        m_dtFrom = setHourTo0(m_dtFrom);
        Calendar dtStart = Calendar.getInstance();
        dtStart.setTime(m_dtFrom);
        int nFirstDayOfWeek = dtStart.getFirstDayOfWeek();
        dtStart.set(Calendar.DAY_OF_WEEK,nFirstDayOfWeek);



        m_dtTo = setHourTo23(m_dtTo);
        Calendar dtEnd = Calendar.getInstance();
        dtEnd.setTime(m_dtTo);
        dtEnd.set(Calendar.DAY_OF_WEEK,nFirstDayOfWeek);
        dtEnd.add(Calendar.DAY_OF_YEAR, 6);

        while (dtStart.getTimeInMillis() <dtEnd.getTimeInMillis() )
        {
            Date dt = dtStart.getTime();

            arFrom.add( KDSUtil.convertDateToString( dt));

            dtStart.add(Calendar.DAY_OF_YEAR, 6); //end of week
            dt = dtStart.getTime();

            arTo.add( KDSUtil.convertDateToString( getDayEnd(dt) ));

            dtStart.add(Calendar.DAY_OF_YEAR, -6);//restore
            dtStart.add(Calendar.WEEK_OF_YEAR, 1);//next week
        }
        return arFrom.size();
    }


    public int getDateSlots_PerWeek_DayOfWeek(ArrayList<DateSlots> dateSlots)
    {
        m_dtFrom = setHourTo0(m_dtFrom);
        Calendar dtStart = Calendar.getInstance();
        dtStart.setTime(m_dtFrom);
        int nFirstDayOfWeek = dtStart.getFirstDayOfWeek();
        dtStart.set(Calendar.DAY_OF_WEEK,nFirstDayOfWeek);

        m_dtTo = setHourTo23(m_dtTo);
        Calendar dtEnd = Calendar.getInstance();
        dtEnd.setTime(m_dtTo);
        dtEnd.set(Calendar.DAY_OF_WEEK,nFirstDayOfWeek);
        dtEnd.add(Calendar.DAY_OF_YEAR, 6);

        Calendar c = Calendar.getInstance();
        while (dtStart.getTimeInMillis() <dtEnd.getTimeInMillis() )
        {
            Date dt = dtStart.getTime();
            DateSlots slot = new DateSlots();
            slot.setDateStart(dt);
            c.setTime(dt);
            c.add(Calendar.DAY_OF_WEEK, m_nDayOfWeek.ordinal()); //just in USA

            String from = KDSUtil.convertDateToString(c.getTime());
            String to = KDSUtil.convertDateToString( getDayEnd(c.getTime()));
            slot.add(from, to);
            dateSlots.add(slot); //this week.
            dtStart.add(Calendar.WEEK_OF_YEAR, 1);//next week
        }
        return dateSlots.size();
    }


    public WeekDay getConditionWeekDay()
    {
        if (m_bDayOfWeek)
            return WeekDay.Unknown;
        else
            return m_nDayOfWeek;
    }

    public void export2Xml(KDSXML xml)
    {

        xml.newAttribute("dtfrom", KDSUtil.convertDateToDbString(m_dtFrom));
        xml.newAttribute("dtto", KDSUtil.convertDateToDbString(m_dtTo));
        xml.newAttribute("arrange", KDSUtil.convertIntToString(m_nReportArrangement.ordinal()));
        xml.newAttribute("timeslot", KDSUtil.convertIntToString(m_nTimeSlot.ordinal()));
        xml.newAttribute("tmfrom",KDSUtil.convertTimeToShortString( m_tmFrom));
        xml.newAttribute("tmto", KDSUtil.convertTimeToShortString( m_tmTo));
        xml.newAttribute("dayofweekenabled",KDSUtil.convertBoolToString( m_bDayOfWeek));
        xml.newAttribute("dayofweek", KDSUtil.convertIntToString( m_nDayOfWeek.ordinal()));



    }

    public void importFromXml(KDSXML xml)
    {
        xml.back_to_root();

        String s = "";
        s = xml.getAttribute("dtfrom", "");
        m_dtFrom = KDSUtil.convertDbStringToDate((s));

        s = xml.getAttribute("dtto", "");
        m_dtTo = KDSUtil.convertDbStringToDate((s));

        s = xml.getAttribute("arrange", "");
        int n = KDSUtil.convertStringToInt(s, 0);
        m_nReportArrangement = ReportArrangement.values()[n];
        s = xml.getAttribute("timeslot", "");
        n = KDSUtil.convertStringToInt(s, 0);
        m_nTimeSlot = TimeSlot.values()[n];

        s = xml.getAttribute("tmfrom", "");
        m_tmFrom =  KDSUtil.convertShortStringToTime(s);

        s = xml.getAttribute("tmto", "");
        m_tmTo =  KDSUtil.convertShortStringToTime(s);

        s = xml.getAttribute("dayofweekenabled", "");
        m_bDayOfWeek = KDSUtil.convertStringToBool(s,false);


        s = xml.getAttribute("dayofweek", "");
        n = KDSUtil.convertStringToInt(s, 0);
        m_nDayOfWeek = WeekDay.values()[n];
        //xml.back_to_parent();
    }

}
