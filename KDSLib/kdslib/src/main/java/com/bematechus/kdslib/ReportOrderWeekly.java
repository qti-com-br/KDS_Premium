package com.bematechus.kdslib;

import java.util.Date;

/**
 * Created by Administrator on 2016/8/12.
 */
public class ReportOrderWeekly extends TimeSlotOrderReport{//} StatisticOrderReport {


    public void next()
    {
        this.getCondition().getWeeklyCondition().nextWeek();

    }
    public void prev()
    {
        this.getCondition().getWeeklyCondition().prevWeek();
    }

    public String getTitleString()
    {

        String s = KDSApplication.getContext().getString(R.string.weekly_report);
        s += " - ";
        return s;
    }
    public String getFixedColString()
    {
        if (getCondition().getReportMode() == ConditionBase.ReportMode.Order)
            return KDSApplication.getContext().getString(R.string.day_of_week);// "Day of Week";
        else
        {
            return KDSApplication.getContext().getString(R.string.item_description);
        }
    }

    public void addDateGroup2Xml(KDSXML xml)
    {
        Date dt =  getCondition().getWeeklyCondition().getWeekFirstDayDate();
        String dtFrom = KDSUtil.convertDateToShortString(dt);
        dt = getCondition().getWeeklyCondition().getWeekLastDayDate();
        String dtTo = KDSUtil.convertDateToShortString(dt);

        xml.newGroup("Date", true);
        xml.newAttribute("from", dtFrom);
        xml.newAttribute("to", dtTo);
        xml.back_to_parent();
    }
    public void addTimeGroup2Xml(KDSXML xml)
    {
        Date dt =  getCondition().getWeeklyCondition().getTimeFrom();
        String tmFrom = KDSUtil.convertTimeToShortString(dt);

        dt =  getCondition().getWeeklyCondition().getTimeTo();
        String tmTo = KDSUtil.convertTimeToShortString(dt);

        xml.newGroup("Time", true);
        xml.newAttribute("from", tmFrom);
        xml.newAttribute("to", tmTo);
        xml.back_to_parent();
    }

    protected String addDateGroup2CSV(String csv)
    {
        Date dt =  getCondition().getWeeklyCondition().getWeekFirstDayDate();
        String dtFrom = KDSUtil.convertDateToShortString(dt);
        dt = getCondition().getWeeklyCondition().getWeekLastDayDate();
        String dtTo = KDSUtil.convertDateToShortString(dt);

        csv += "Date from " +  dtFrom;
        csv += " to " + dtTo;

        return csv;
    }

    protected String addTimeGroup2CSV(String csv)
    {
        Date dt =  getCondition().getWeeklyCondition().getTimeFrom();
        String tmFrom = KDSUtil.convertTimeToShortString(dt);

        dt =  getCondition().getWeeklyCondition().getTimeTo();
        String tmTo = KDSUtil.convertTimeToShortString(dt);

        csv += "Time from " + tmFrom;
        csv += " to " + tmTo;

        return csv;
    }


    /**
     * weekly_date_date.xml
     * @return
     */
    public String getReportFileName()
    {
        String s = super.getReportFileName();
        Date dt =  getCondition().getWeeklyCondition().getWeekFirstDayDate();
        String dtFrom = KDSUtil.convertDateToDbString(dt);
        dt = getCondition().getWeeklyCondition().getWeekLastDayDate();
        String dtTo = KDSUtil.convertDateToDbString(dt);
        s += "_" + dtFrom;
        s += "_"+dtTo;

        dt =  getCondition().getWeeklyCondition().getTimeFrom();
        String tmFrom = KDSUtil.convertTimeToFileNameString(dt);

        dt =  getCondition().getWeeklyCondition().getTimeTo();
        String tmTo = KDSUtil.convertTimeToFileNameString(dt);
        s += "_"+tmFrom;
        s += "_" + tmTo;
        s += ".xml";
        return s;
    }


    protected void importDateGroup2Xml(KDSXML xml)
    {
        xml.getFirstGroup("Date");
        String s = xml.getAttribute("from", "");
        this.getCondition().getWeeklyCondition().setWeekFirstDay(KDSUtil.convertShortStringToDate(s));


        xml.back_to_parent();
    }

    protected void importTimeGroup2Xml(KDSXML xml)
    {
        xml.getFirstGroup("Time" );
        String s = xml.getAttribute("from", "");
        getCondition().getWeeklyCondition().setTimeFromString(s);
        s = xml.getAttribute("to", "");
        getCondition().getWeeklyCondition().setTimeToString(s);
        xml.back_to_parent();
    }

    protected void importTimeSlotGroup2Xml(KDSXML xml)
    {

    }
}
