using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ReportViewer
{
public class ReportOrderDaily : TimeSlotOrderReport{//} StatisticOrderReport {

//    public void resetFixedColText()
//    {
//        if (m_arData.size() <=0) return;
//        ReportOrderEntry entryOriginal =  m_arData.get(0);
//
//        for (int i=0; i< m_arReversedData.size(); i++) {
//            ReportOrderEntry entry = m_arReversedData.get(i);
//            String strFrom = entryOriginal.getTimeSlotsFrom().get(i);
//            Date dt = KDSUtil.convertStringToDate(strFrom);
//            String text = KDSUtil.convertTimeToShortString(dt);
//            entry.setFixedText(text);
//        }
//    }

    //public void next()
    //{
    //    this.getCondition().getDailyReportCondition().moveToNextDay();

    //}
    //public void prev()
    //{
    //    this.getCondition().getDailyReportCondition().moveToPrevDay();
    //}

    public String getTitleString()
    {
        return "Daily Report - ";//+getCondition().getOrderReportContentString();
    }

    public void addDateGroup2Xml(KDSXML xml)
    {
        String dt =  getCondition().getDateFrom();
        
        xml.new_group("Date", true);
        xml.new_attribute("from", dt);
        xml.new_attribute("to", dt);
        xml.back_to_parent();
    }
    public void addTimeGroup2Xml(KDSXML xml)
    {
        String tmFrom = getCondition().getTimeFrom();
     
        String tmTo = getCondition().getTimeTo();

        xml.new_group("Time", true);
        xml.new_attribute("from", tmFrom);
        xml.new_attribute("to", tmTo);
        xml.back_to_parent();
    }

    public void addTimeSlotGroup2Xml(KDSXML xml)
    {
        Condition.TimeSlot ts  =  getCondition().getTimeSlot();

        String s = Condition.getTimeSlotString(ts);

        xml.new_group("TimeSlot", s, false);

    }

    /**
     * daily_date
     * @return
     */
    public String getReportFileName()
    {
        String s = base.getReportFileName();
        String dtFrom =  getCondition().getDateFrom();
        s +="_" + dtFrom;
        s += ".xml";
        return s;
    }

}
}