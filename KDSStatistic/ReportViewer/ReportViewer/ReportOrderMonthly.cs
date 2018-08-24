using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ReportViewer
{
public class ReportOrderMonthly : TimeSlotOrderReport{//} StatisticOrderReport {

//    public void resetFixedColText()
//    {
//        if (m_arData.size() <=0) return;
//        ReportOrderEntry entryOriginal =  m_arData.get(0);
//
//        for (int i=0; i< m_arReversedData.size(); i++) {
//            ReportOrderEntry entry = m_arReversedData.get(i);
//            String strFrom = entryOriginal.getTimeSlotsFrom().get(i);
//            Date dt = KDSUtil.convertStringToDate(strFrom);
//            Calendar c = Calendar.getInstance();
//            c.setTime(dt);
//            //int nmonthday = c.get(Calendar.DAY_OF_MONTH);
//
//            String text = getMonthDayString(c);
//            entry.setFixedText(text);
//        }
//    }
    //public String getMonthDayString(Calendar calendar)
    //{
    //    int nMonth = calendar.get(Calendar.MONTH);
    //    int nDay = calendar.get(Calendar.DAY_OF_MONTH);
    //    String strMonth = ConditionBase.getMonthName(nMonth);;

    //    return strMonth + " " + KDSUtil.convertIntToString(nDay);

    ////}

    //public void next()
    //{
    //    this.getCondition().getMonthlyCondition().nextMonth();

    //}
    //public void prev()
    //{
    //    this.getCondition().getMonthlyCondition().prevMonth();
    //}

    public String getTitleString()
    {
        return "Monthly Report - ";// + getCondition().getOrderReportContentString();//KDSUtil.convertDateToShortString( getCondition().getMonthlyCondition().getMonthFirstDay());

    }

    public String getFixedColString()
    {
        return "Day of Month";
    }

    public void addDateGroup2Xml(KDSXML xml)
    {
        //Date dt =  getCondition().getMonthlyCondition().getMonthFirstDay();
        //String dtFrom = KDSUtil.convertDateToShortString(dt);
        //dt = getCondition().getMonthlyCondition().getMonthLastDay();
        //String dtTo = KDSUtil.convertDateToShortString(dt);

        //xml.newGroup("Date", true);
        //xml.newAttribute("from", dtFrom);
        //xml.newAttribute("to", dtTo);
        //xml.back_to_parent();
    }
    public void addTimeGroup2Xml(KDSXML xml)
    {
        //Date dt =  getCondition().getMonthlyCondition().getTimeFrom();
        //String tmFrom = KDSUtil.convertTimeToShortString(dt);

        //dt =  getCondition().getMonthlyCondition().getTimeTo();
        //String tmTo = KDSUtil.convertTimeToShortString(dt);

        //xml.newGroup("Time", true);
        //xml.newAttribute("from", tmFrom);
        //xml.newAttribute("to", tmTo);
        //xml.back_to_parent();
    }

    /**
     * monthly_date_date.xml
     * @return
     */
    public String getReportFileName()
    {
        //String s = super.getReportFileName();
        //Date dt =  getCondition().getMonthlyCondition().getMonthFirstDay();
        //String dtFrom = KDSUtil.convertDateToDbString(dt);
        //dt = getCondition().getMonthlyCondition().getMonthLastDay();
        //String dtTo = KDSUtil.convertDateToDbString(dt);
        //s += "_" + dtFrom;
        //s += "_"+dtTo;

        //dt =  getCondition().getMonthlyCondition().getTimeFrom();
        //String tmFrom = KDSUtil.convertTimeToFileNameString(dt);

        //dt =  getCondition().getMonthlyCondition().getTimeTo();
        //String tmTo = KDSUtil.convertTimeToFileNameString(dt);
        //s += "_"+tmFrom;
        //s += "_" + tmTo;
        //s += ".xml";
        //return s;
        return "";
    }

}
}