package com.bematechus.kdslib;

import java.util.Calendar;
import java.util.Date;

/**
 * SAME AS KDS FILE
 */

/**
 *  In KDS, it needs to create statistic report in local station. Then send report to statistic app.
 *  This class is the report condition base class, some common function is here.
 *  This class is from statistic app.
 */
public class ConditionBase {


    public enum ReportMode
    {
        Order, //summary order
        Item, //summary items
    }

    public enum TimeSlot
    {
        mins15,
        mins30,
        hr1,
        hr8,
        hr12,
    }

    protected ReportMode m_reportMode = ReportMode.Order;
    public void setReportMode(ReportMode mode)
    {
        m_reportMode = mode;
    }
    public ReportMode getReportMode()
    {
        return m_reportMode;
    }

    static public String getTimeSlotString(TimeSlot ts)
    {
        switch (ts)
        {

            case mins15:
                return KDSApplication.getContext().getString(R.string.mins_15);//"15 mins";

            case mins30:
                return KDSApplication.getContext().getString(R.string.mins_30);//"30 mins";
            case hr1:
                return KDSApplication.getContext().getString(R.string.hour_1);//"1 hr";

            case hr8:
                return KDSApplication.getContext().getString(R.string.hours_8);//"8 hrs";

            case hr12:
                return KDSApplication.getContext().getString(R.string.hours_12);//"12 hrs";

        }
        return "";
    }

    static public TimeSlot getTimeSlotFromString(String s)
    {
        if (s.equals( KDSApplication.getContext().getString(R.string.mins_15)))
            return TimeSlot.mins15;
        else if (s.equals( KDSApplication.getContext().getString(R.string.mins_30)))
            return TimeSlot.mins30;

        else if (s.equals(KDSApplication.getContext().getString(R.string.hour_1)))
            return TimeSlot.hr1;
        else if (s.equals(KDSApplication.getContext().getString(R.string.hours_8)))
            return TimeSlot.hr8;
        else if (s.equals( KDSApplication.getContext().getString(R.string.hours_12)))
            return TimeSlot.hr12;
        return TimeSlot.hr1;
    }

    public Date setHourTo(Date dt, Date dtTime)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dtTime);

        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),calendar.get(Calendar.SECOND));
        return c.getTime();
    }
    public Date setHourTo0(Date dt) {

        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

        return c.getTime();
    }

    public Date setHourTo23(Date dt) {

        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        return c.getTime();

    }
    public Date getDayEnd(Date dt) {

        return setHourTo23(dt);

    }
    static public String getWeekDayString(int nWeekDay)
    {
        switch (nWeekDay)
        {
            case Calendar.SUNDAY:
                return KDSApplication.getContext().getString(R.string.sunday);//"Sun";
            case Calendar.MONDAY:
                return KDSApplication.getContext().getString(R.string.monday);//"Mon";
            case Calendar.TUESDAY:
                return KDSApplication.getContext().getString(R.string.tuesday);//"Tue";
            case Calendar.WEDNESDAY:
                return KDSApplication.getContext().getString(R.string.wednesday);//"Wed";
            case Calendar.THURSDAY:
                return KDSApplication.getContext().getString(R.string.thursday);//"Thu";
            case Calendar.FRIDAY:
                return KDSApplication.getContext().getString(R.string.friday);//"Fri";
            case Calendar.SATURDAY:
                return KDSApplication.getContext().getString(R.string.saturday);//"Sat";

        }
        return "";
    }

    static public String getWeekDayString(Date dt)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        int nWeekDay = c.get(Calendar.DAY_OF_WEEK);
        return getWeekDayString(nWeekDay);

    }

    static public String getMonthDayString(Date dt)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        int nMonth = c.get(Calendar.MONTH);
        String s = getMonthName(nMonth);
        int nDay = c.get(Calendar.DAY_OF_MONTH);
        s += " ";
        s += KDSUtil.convertIntToString(nDay);
        return s;
    }

    static public String getMonthName(int nCalendarMonth)
    {
        String strMonth = "";
        switch (nCalendarMonth)
        {
            case Calendar.JANUARY:
                strMonth =KDSApplication.getContext().getString(R.string.jan);// "Jan.";
                break;
            case Calendar.FEBRUARY:
                strMonth = KDSApplication.getContext().getString(R.string.feb);//"Feb.";
                break;
            case Calendar.MARCH:
                strMonth = KDSApplication.getContext().getString(R.string.mar);//"Mar.";
                break;
            case Calendar.APRIL:
                strMonth = KDSApplication.getContext().getString(R.string.apr);//"Apr.";
                break;
            case Calendar.MAY:
                strMonth = KDSApplication.getContext().getString(R.string.may);//"May";
                break;
            case Calendar.JUNE:
                strMonth = KDSApplication.getContext().getString(R.string.jun);//"Jun.";
                break;
            case Calendar.JULY:
                strMonth = KDSApplication.getContext().getString(R.string.jul);//"Jul.";
                break;
            case Calendar.AUGUST:
                strMonth = KDSApplication.getContext().getString(R.string.aug);//"Aug.";
                break;
            case Calendar.SEPTEMBER:
                strMonth = KDSApplication.getContext().getString(R.string.sep);//"Sep.";
                break;
            case Calendar.OCTOBER:
                strMonth = KDSApplication.getContext().getString(R.string.oct);//"Oct.";
                break;
            case Calendar.NOVEMBER:
                strMonth = KDSApplication.getContext().getString(R.string.nov);//"Nov.";
                break;
            case Calendar.DECEMBER:
                strMonth = KDSApplication.getContext().getString(R.string.dec);//"Dec.";
                break;
        }
        return strMonth;
    }

    static public int getTimeSlotCount(TimeSlot ts)
    {
        switch (ts)
        {

            case mins15:
                return 96;

            case mins30:
                return 48;

            case hr1:
                return 24;

            case hr8:
                return 3;

            case hr12:
                return 2;
            default:
                return 24;
        }
    }

    static public int getTimeSlotCount(TimeSlot ts, Date tmFrom, Date tmTo)
    {
        long ms = tmTo.getTime() - tmFrom.getTime();
        long seconds = ms/1000;

        switch (ts)
        {

            case mins15: {
                long l= seconds / 900;
                if ((seconds%900)>0)
                    l ++;
                return (int)l;
            }

            case mins30: {
                long l= seconds / 1800;
                if ((seconds%1800)>0)
                    l ++;
                return (int)l;
            }

            case hr1: {
                long l= seconds / 3600;
                if ((seconds%3600)>0)
                    l ++;
                return (int)l;
            }

            case hr8: {
                long l= seconds / 28800;
                if ((seconds%28800)>0)
                    l ++;
                return (int)l;
            }

            case hr12:{
                long l= seconds / 43200;
                if ((seconds%43200)>0)
                    l ++;
                return (int)l;
            }
            default: {
                long l= seconds / 3600;
                if ((seconds%3600)>0)
                    l ++;
                return (int)l;
            }
        }
    }
    static public Calendar getNextCalendar(Calendar c,  TimeSlot ts)
    {
        switch (ts)
        {

            case mins15: {
                c.add(Calendar.MINUTE, 15);
                return c;
            }
            case mins30: {
                c.add(Calendar.MINUTE, 30);
                return c;
            }

            case hr1:
            {
                c.add(Calendar.HOUR_OF_DAY, 1);
                return c;
            }

            case hr8:
            {
                c.add(Calendar.HOUR_OF_DAY, 8);
                return c;
            }

            case hr12: {
                c.add(Calendar.HOUR_OF_DAY, 12);
                return c;
            }
            default:
                return c;

        }

    }

    public void export2Xml(KDSXML xml)
    {

    }

    public void importFromXml(KDSXML xml)
    {

    }

}
