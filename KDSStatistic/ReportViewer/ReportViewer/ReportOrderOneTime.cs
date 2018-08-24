using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ReportViewer
{
public class ReportOrderOneTime : TimeSlotOrderReport{//} StatisticOrderReport {


    //public String getMonthName(int nCalendarMonth)
    //{
    //    return ConditionBase.getMonthName(nCalendarMonth);

    //}
    //public String getDayString(Calendar calendar)
    //{
    //    switch (this.getCondition().getOneTimeCondition().getReportArrangement())
    //    {

    //        case FullDate:
    //            return KDSUtil.convertDateToShortString(calendar.getTime());

    //        case PerMonth: {
    //            int y = calendar.get(Calendar.YEAR);
    //            int m = calendar.get(Calendar.MONTH);
    //            String s = String.format("%d %s", y, getMonthName(m));
    //            return s;

    //        }
    //        case PerWeek:
    //            String strDate = KDSUtil.convertDateToShortString(calendar.getTime());
    //            return strDate;

    //    }
    //   return "";
    //}

    public String getTitleString()
    {
        String s =  "Statistic Report - ";// +getCondition().getOrderReportContentString();//;
        //s += KDSUtil.convertDateToShortString( getCondition().getOneTimeCondition().getDateFrom());
        //s += " To ";
        //s += KDSUtil.convertDateToShortString( getCondition().getOneTimeCondition().getDateTo());
        return s;

    }
    public void addDateGroup2Xml(KDSXML xml)
    {
        //Date dt =  getCondition().getOneTimeCondition().getDateFrom();
        //String dtFrom = KDSUtil.convertDateToShortString(dt);
        //dt = getCondition().getOneTimeCondition().getDateTo();
        //String dtTo = KDSUtil.convertDateToShortString(dt);

        //xml.newGroup("Date", true);
        //xml.newAttribute("from", dtFrom);
        //xml.newAttribute("to", dtTo);
        //xml.back_to_parent();
    }
    public void addTimeGroup2Xml(KDSXML xml)
    {
        //Date dt =  getCondition().getOneTimeCondition().getTimeFrom();
        //String tmFrom = KDSUtil.convertTimeToShortString(dt);

        //dt =  getCondition().getOneTimeCondition().getTimeTo();
        //String tmTo = KDSUtil.convertTimeToShortString(dt);

        //xml.newGroup("Time", true);
        //xml.newAttribute("from", tmFrom);
        //xml.newAttribute("to", tmTo);
        //xml.back_to_parent();
    }

    public void addTimeSlotGroup2Xml(KDSXML xml)
    {
        //ConditionBase.TimeSlot ts  =  getCondition().getOneTimeCondition().getTimeSlot();

        //String s = ConditionBase.getTimeSlotString(ts);

        //xml.newGroup("TimeSlot",s, false);

    }
    /**
     * onetime_date_date.xml
     * @return
     */
    public String getReportFileName()
    {
        //String s = super.getReportFileName();
        //Date dt =  getCondition().getOneTimeCondition().getDateFrom();
        //String dtFrom = KDSUtil.convertDateToDbString(dt);
        //dt = getCondition().getOneTimeCondition().getDateTo();
        //String dtTo = KDSUtil.convertDateToDbString(dt);
        //s += "_" + dtFrom;
        //s += "_"+dtTo;

        //dt =  getCondition().getOneTimeCondition().getTimeFrom();
        //String tmFrom = KDSUtil.convertTimeToFileNameString(dt);

        //dt =  getCondition().getOneTimeCondition().getTimeTo();
        //String tmTo = KDSUtil.convertTimeToFileNameString(dt);
        //s += "_"+tmFrom;
        //s += "_" + tmTo;
        //s += ".xml";
        //return s;
        return "";
    }

}


}