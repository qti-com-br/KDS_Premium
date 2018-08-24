using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ReportViewer
{
public class ReportOrderWeekly : TimeSlotOrderReport{//} StatisticOrderReport {

    //public String getWeekDayString(int nWeekDay)
    //{
    //    return Condition.getWeekDayString(nWeekDay);


    //}
    //public void next()
    //{
    //    this.getCondition().getWeeklyCondition().nextWeek();

    //}
    //public void prev()
    //{
    //    this.getCondition().getWeeklyCondition().prevWeek();
    //}

    public String getTitleString()
    {
        return "Weekly Report - ";//+ getCondition().getOrderReportContentString();//KDSUtil.convertDateToShortString( getCondition().getWeeklyCondition().getWeekFirstDayDate());

    }
    public String getFixedColString()
    {
        return "Day of Week";
    }

    public void addDateGroup2Xml(KDSXML xml)
    {
        
        
    }
    public void addTimeGroup2Xml(KDSXML xml)
    {
        
    }

    /**
     * weekly_date_date.xml
     * @return
     */
    public String getReportFileName()
    {
        //String s = super.getReportFileName();
        //Date dt =  getCondition().getWeeklyCondition().getWeekFirstDayDate();
        //String dtFrom = KDSUtil.convertDateToDbString(dt);
        //dt = getCondition().getWeeklyCondition().getWeekLastDayDate();
        //String dtTo = KDSUtil.convertDateToDbString(dt);
        //s += "_" + dtFrom;
        //s += "_"+dtTo;

        //dt =  getCondition().getWeeklyCondition().getTimeFrom();
        //String tmFrom = KDSUtil.convertTimeToFileNameString(dt);

        //dt =  getCondition().getWeeklyCondition().getTimeTo();
        //String tmTo = KDSUtil.convertTimeToFileNameString(dt);
        //s += "_"+tmFrom;
        //s += "_" + tmTo;
        //s += ".xml";
        //return s;
        return "";
    }

}
}