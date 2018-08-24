using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ReportViewer
{
    class ConditionDailyReport
    {
        ConditionBase.TimeSlot m_timeSlot = ConditionBase.TimeSlot.hr1;
        DateTime m_dt = new DateTime();
        DateTime m_tmFrom = new DateTime();
        DateTime m_tmTo = new DateTime();

        public void setTimeSlot(ConditionBase.TimeSlot t)
        {
            m_timeSlot = t;
        }
        public ConditionBase.TimeSlot getTimeSlot()
        {
            return m_timeSlot;
        }

        public void setDate(DateTime dt)
        {
            m_dt = dt;
        }
        public DateTime getDate()
        {
            return m_dt;
        }

        public void setTimeFrom(DateTime dt)
        {
            m_tmFrom = dt;
        }
        public DateTime getTimeFrom()
        {
            return m_tmFrom;
        }

        public void setTimeTo(DateTime dt)
        {
            m_tmTo = dt;
        }
        public DateTime getTimeTo()
        {
            return m_tmTo;
        }

        public void setTimeFromString(String strTime)
        {
            String s = "2016-01-01 " + strTime + ":00";
            m_tmFrom = DateTime.Parse(s);
            

        }


        public void setTimeToString(String strTime)
        {
            String s = "2016-01-01 " + strTime + ":00";
            m_tmTo = DateTime.Parse(s);

        }



     

        public void export2Xml(KDSXML xml)
        {
            //xml.newGroup("daily", true);
            xml.new_attribute("dtfrom", KDSUtil.convertDateToDbString(m_dt));
            xml.new_attribute("timeslot", (((int)m_timeSlot).ToString()));

            xml.new_attribute("tmfrom", KDSUtil.convertTimeToShortString(m_tmFrom));
            xml.new_attribute("tmto", KDSUtil.convertTimeToShortString(m_tmTo));

            //xml.back_to_parent();
        }

        public void importFromXml(KDSXML xml)
        {
            xml.back_to_root();
            //xml.getFirstGroup("Daily");
            String s = "";
            s = xml.get_attribute("dtfrom", "");
            m_dt = KDSUtil.convertShortStringToDate((s));
            s = xml.getAttribute("timeslot", "");
            int n = KDSUtil.convertStringToInt(s, 0);
            m_timeSlot = ConditionBase.TimeSlot.values()[n];

            s = xml.get_attribute("tmfrom", "");
            m_tmFrom = KDSUtil.convertShortStringToTime(s);

            s = xml.get_attribute("tmto", "");
            m_tmTo = KDSUtil.convertShortStringToTime(s);

            //xml.back_to_parent();


        }

    }
}
