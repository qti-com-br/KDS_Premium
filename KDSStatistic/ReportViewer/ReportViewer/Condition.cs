using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ReportViewer
{
    public class Condition
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

        public enum ReportArrangement
        {
            FullDate,
            PerMonth,
            PerWeek,
        }
        public enum ReportType
        {
            Daily,
            Weekly,
            Monthly,
            OneTime,
        }
        public enum OrderReportContent
        {
            Counter,
            PrepTime,//from start to finished time.
        }

        String m_stationFrom = "0";
        String m_stationTo = "5";

        ReportType m_reportType = ReportType.Daily;

        TimeSlot m_timeSlot = TimeSlot.hr1;
        String m_dtFrom = "";
        String m_dtTo = "";
        
        String m_tmFrom ="";
        String m_tmTo = "";

        bool m_bDayOfWeek;
        DayOfWeek m_nDayOfWeek;
        ReportArrangement m_nReportArrangement;

        public void setDayOfWeek(DayOfWeek dw)
        {

            m_nDayOfWeek = dw;
        }
        public DayOfWeek getDayOfWeek()
        {

            return m_nDayOfWeek;
        }

        public void setStationFrom(String stationID)
        {

            m_stationFrom = stationID;
        }
        public String getStationFrom()
        {

            return m_stationFrom;
        }

        public void setStationTo(String stationID)
        {

            m_stationTo = stationID;
        }
        public String getStationTo()
        {

            return m_stationTo;
        }
      
        private ReportMode m_reportMode = ReportMode.Order;

        public void setReportType(ReportType rt)
        {

            m_reportType = rt;
        }
        public ReportType getReportType()
        {

            return m_reportType;
        }

        public void setReportMode(ReportMode mode)
        {
            m_reportMode = mode;
        }
        public ReportMode getReportMode()
        {
            return m_reportMode;
        }

        public void setDateFrom(String dt)
        {

            m_dtFrom = dt;
        }
        public String getDateFrom()
        {
            return m_dtFrom;
        }

        public void setDateTo(String dt)
        {

            m_dtTo = dt;
        }
        public String getDateTo()
        {
            return m_dtTo;
        }


        public void setTimeFrom(String dt)
        {

            m_tmFrom = dt;
        }
        public String getTimeFrom()
        {
            return m_tmFrom;
        }

        public void setTimeTo(String dt)
        {

            m_tmTo = dt;
        }
        public String getTimeTo()
        {
            return m_tmTo;
        }

        public void setTimeSlot(TimeSlot ts)
        {
            m_timeSlot = ts;
        }
        public TimeSlot getTimeSlot()
        {

            return m_timeSlot;
        }

        public void setEnableDayOfWeek(bool bEnable)
        {

            m_bDayOfWeek = bEnable;
        }
        public bool setEnableDayOfWeek()
        {

            return m_bDayOfWeek;
        }
        public void setReportArrange(ReportArrangement ra)
        {
            m_nReportArrangement = ra;

        }
        public ReportArrangement getReportArrange()
        {

            return m_nReportArrangement;
        }

        public String export2XmlString()
        {
            KDSXML xml = new KDSXML();
            xml.new_doc_with_root("Condition");
           
            xml.new_attribute("rptype", ((int)m_reportType).ToString());
            xml.new_attribute("stationfrom", m_stationFrom);
            xml.new_attribute("stationto", m_stationTo);
            xml.new_attribute("dtfrom",  m_dtFrom);
            xml.new_attribute("dtto", m_dtTo);
            xml.new_attribute("timeslot", ((int)m_timeSlot).ToString());
            xml.new_attribute("tmfrom", m_tmFrom);
            xml.new_attribute("tmto", m_tmTo);
            xml.new_attribute("arrange", ((int)m_nReportArrangement).ToString());

            xml.new_attribute("dayofweekenabled", m_bDayOfWeek ? "1" : "0");
            xml.new_attribute("dayofweek", ((int)m_nDayOfWeek).ToString());
            return xml.get_xml_string();


        }

        public void importFromXmlString(KDSXML xml)
        {

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
        //public DateTime setHourTo(DateTime dt, DateTime dtTime)
        //{

        //    DateTime dttm = new DateTime(dt.Year, dt.Month, dt.Day, dtTime.Hour, dtTime.Minute, dtTime.Second);
        //    return dttm;

        //}
        //public DateTime setHourTo0(DateTime dt)
        //{
        //    DateTime dttm = new DateTime(dt.Year, dt.Month, dt.Day, 0, 0, 0);
        //    return dttm;

        //}

        //public DateTime setHourTo23(DateTime dt)
        //{

        //    DateTime dttm = new DateTime(dt.Year, dt.Month, dt.Day, 23, 59, 59);
        //    return dttm;

        //}
        //public DateTime getDayEnd(DateTime dt)
        //{

        //    return setHourTo23(dt);

        //}
        //static public String getWeekDayString(int nWeekDay)
        //{
        //    DayOfWeek wd = (DayOfWeek)(nWeekDay);
        //    return wd.ToString();


        //}

        //static public String getWeekDayString(DateTime dt)
        //{
        //    return dt.DayOfWeek.ToString();


        //}

        //static public String getMonthDayString(DateTime dt)
        //{



        //    int nMonth = dt.Month;
        //    String s = getMonthName(nMonth);
        //    int nDay = dt.Day;
        //    s += " ";
        //    s += nDay.ToString();
        //    return s;
        //}

        //static public String getMonthName(int nCalendarMonth)
        //{
        //    String strMonth = "";
        //    switch (nCalendarMonth)
        //    {
        //        case 1:
        //            strMonth = "Jan.";
        //            break;
        //        case 2:
        //            strMonth = "Feb.";
        //            break;
        //        case 3:
        //            strMonth = "Mar.";
        //            break;
        //        case 4:
        //            strMonth = "Apr.";
        //            break;
        //        case 5:
        //            strMonth = "May";
        //            break;
        //        case 6:
        //            strMonth = "Jun.";
        //            break;
        //        case 7:
        //            strMonth = "Jul.";
        //            break;
        //        case 8:
        //            strMonth = "Aug.";
        //            break;
        //        case 9:
        //            strMonth = "Sept.";
        //            break;
        //        case 10:
        //            strMonth = "Oct.";
        //            break;
        //        case 11:
        //            strMonth = "Nov.";
        //            break;
        //        case 12:
        //            strMonth = "Dec.";
        //            break;


        //    }
        //    return strMonth;
        //}

        //static public int getTimeSlotCount(TimeSlot ts)
        //{
        //    switch (ts)
        //    {

        //        case TimeSlot.mins15:
        //            return 96;

        //        case TimeSlot.mins30:
        //            return 48;

        //        case TimeSlot.hr1:
        //            return 24;

        //        case TimeSlot.hr8:
        //            return 3;

        //        case TimeSlot.hr12:
        //            return 2;
        //        default:
        //            return 24;
        //    }
        //}

        //static public int getTimeSlotCount(TimeSlot ts, DateTime tmFrom, DateTime tmTo)
        //{
        //    long ms = tmTo.Millisecond - tmFrom.Millisecond;
        //    long seconds = ms / 1000;

        //    switch (ts)
        //    {

        //        case TimeSlot.mins15:
        //            {
        //                long l = seconds / 900;
        //                if ((seconds % 900) > 0)
        //                    l++;
        //                return (int)l;
        //            }

        //        case TimeSlot.mins30:
        //            {
        //                long l = seconds / 1800;
        //                if ((seconds % 1800) > 0)
        //                    l++;
        //                return (int)l;
        //            }

        //        case TimeSlot.hr1:
        //            {
        //                long l = seconds / 3600;
        //                if ((seconds % 3600) > 0)
        //                    l++;
        //                return (int)l;
        //            }

        //        case TimeSlot.hr8:
        //            {
        //                long l = seconds / 28800;
        //                if ((seconds % 28800) > 0)
        //                    l++;
        //                return (int)l;
        //            }

        //        case TimeSlot.hr12:
        //            {
        //                long l = seconds / 43200;
        //                if ((seconds % 43200) > 0)
        //                    l++;
        //                return (int)l;
        //            }
        //        default:
        //            {
        //                long l = seconds / 3600;
        //                if ((seconds % 3600) > 0)
        //                    l++;
        //                return (int)l;
        //            }
        //    }
        //}

        
    }
}
