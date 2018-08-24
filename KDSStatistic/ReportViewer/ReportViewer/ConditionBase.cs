using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Globalization;

namespace ReportViewer
{
    class ConditionBase
    {

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

        //private enum WeekDay
        //{
        //    Sun,
        //    Mon,
        //    Tue,
        //    Wed,
        //    Thu,
        //    Fri,
        //    Sat,
        //}

        private ReportMode m_reportMode = ReportMode.Order;
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

                case TimeSlot.mins15:
                    return "15 mins";

                case TimeSlot.mins30:
                    return "30 mins";
                case TimeSlot.hr1:
                    return "1 hr";

                case TimeSlot.hr8:
                    return "8 hrs";

                case TimeSlot.hr12:
                    return "12 hrs";

            }
            return "";
        }
        public DateTime setHourTo(DateTime dt, DateTime dtTime)
        {

            DateTime dttm = new DateTime(dt.Year, dt.Month, dt.Day, dtTime.Hour, dtTime.Minute, dtTime.Second);
            return dttm;

        }
        public DateTime setHourTo0(DateTime dt)
        {
            DateTime dttm = new DateTime(dt.Year, dt.Month, dt.Day,0,0,0);
            return dttm;

        }

        public DateTime setHourTo23(DateTime dt)
        {

            DateTime dttm = new DateTime(dt.Year, dt.Month, dt.Day, 23, 59, 59);
            return dttm;

        }
        public DateTime getDayEnd(DateTime dt)
        {

            return setHourTo23(dt);

        }
        static public String getWeekDayString(int nWeekDay)
        {
            DayOfWeek wd = (DayOfWeek)(nWeekDay);
            return wd.ToString();

            
        }

        static public String getWeekDayString(DateTime dt)
        {
            return dt.DayOfWeek.ToString();


        }

        static public String getMonthDayString(DateTime dt)
        {
            
            
            
            int nMonth = dt.Month;
            String s = getMonthName(nMonth);
            int nDay = dt.Day;
            s += " ";
            s +=nDay.ToString();
            return s;
        }

        static public String getMonthName(int nCalendarMonth)
        {
            String strMonth = "";
            switch (nCalendarMonth)
            {
                case 1:
                    strMonth = "Jan.";
                    break;
                case 2:
                    strMonth = "Feb.";
                    break;
                case 3:
                    strMonth = "Mar.";
                    break;
                case 4:
                    strMonth = "Apr.";
                    break;
                case 5:
                    strMonth = "May";
                    break;
                case 6:
                    strMonth = "Jun.";
                    break;
                case 7:
                    strMonth = "Jul.";
                    break;
                case 8:
                    strMonth = "Aug.";
                    break;
                case 9:
                    strMonth = "Sep.";
                    break;
                case 10:
                    strMonth = "Oct.";
                    break;
                case 11:
                    strMonth = "Nov.";
                    break;
                case 12:
                    strMonth = "Dec.";
                    break;


            }
            return strMonth;
        }

        static public int getTimeSlotCount(TimeSlot ts)
        {
            switch (ts)
            {

                case TimeSlot.mins15:
                    return 96;

                case TimeSlot.mins30:
                    return 48;

                case TimeSlot.hr1:
                    return 24;

                case TimeSlot.hr8:
                    return 3;

                case TimeSlot.hr12:
                    return 2;
                default:
                    return 24;
            }
        }

        static public int getTimeSlotCount(TimeSlot ts, DateTime tmFrom, DateTime tmTo)
        {
            long ms = tmTo.Millisecond - tmFrom.Millisecond;
            long seconds = ms / 1000;

            switch (ts)
            {

                case TimeSlot.mins15:
                    {
                        long l = seconds / 900;
                        if ((seconds % 900) > 0)
                            l++;
                        return (int)l;
                    }

                case TimeSlot.mins30:
                    {
                        long l = seconds / 1800;
                        if ((seconds % 1800) > 0)
                            l++;
                        return (int)l;
                    }

                case TimeSlot.hr1:
                    {
                        long l = seconds / 3600;
                        if ((seconds % 3600) > 0)
                            l++;
                        return (int)l;
                    }

                case TimeSlot.hr8:
                    {
                        long l = seconds / 28800;
                        if ((seconds % 28800) > 0)
                            l++;
                        return (int)l;
                    }

                case TimeSlot.hr12:
                    {
                        long l = seconds / 43200;
                        if ((seconds % 43200) > 0)
                            l++;
                        return (int)l;
                    }
                default:
                    {
                        long l = seconds / 3600;
                        if ((seconds % 3600) > 0)
                            l++;
                        return (int)l;
                    }
            }
        }
        
        public void export2Xml(KDSXML xml)
        {

        }

        public void importFromXml(KDSXML xml)
        {

        }
    }
}
