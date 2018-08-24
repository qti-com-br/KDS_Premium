package com.bematechus.kdslib;

import java.util.Date;

/**
 * Created by Administrator on 2016/8/12.
 */
public class ReportOrderDaily extends TimeSlotOrderReport{//} StatisticOrderReport {


    public void next()
    {
        this.getCondition().getDailyReportCondition().moveToNextDay();

    }
    public void prev()
    {
        this.getCondition().getDailyReportCondition().moveToPrevDay();
    }

    public String getTitleString()
    {

        String s = KDSApplication.getContext().getString(R.string.daily_report);
        s += " - ";
        return s;
    }

    public void addDateGroup2Xml(KDSXML xml)
    {
        Date dt =  getCondition().getDailyReportCondition().getDate();
        String s = KDSUtil.convertDateToShortString(dt);
        xml.newGroup("Date", true);
        xml.newAttribute("from", s);
        xml.newAttribute("to", s);
        xml.back_to_parent();
    }
    public void addTimeGroup2Xml(KDSXML xml)
    {
        Date dt =  getCondition().getDailyReportCondition().getTimeFrom();
        String tmFrom = KDSUtil.convertTimeToShortString(dt);

        dt =  getCondition().getDailyReportCondition().getTimeTo();
        String tmTo = KDSUtil.convertTimeToShortString(dt);

        xml.newGroup("Time", true);
        xml.newAttribute("from", tmFrom);
        xml.newAttribute("to", tmTo);
        xml.back_to_parent();
    }

    public void addTimeSlotGroup2Xml(KDSXML xml)
    {
        ConditionBase.TimeSlot ts  =  getCondition().getDailyReportCondition().getTimeSlot();

        String s = ConditionBase.getTimeSlotString(ts);

        xml.newGroup("TimeSlot",s, false);

    }


    protected String addDateGroup2CSV(String csv)
    {
        Date dt =  getCondition().getDailyReportCondition().getDate();
        String s = KDSUtil.convertDateToShortString(dt);
        csv += "Date from " + s;
        csv += " to " + s;
        return csv;
    }

    protected String addTimeGroup2CSV(String csv)
    {
        Date dt =  getCondition().getDailyReportCondition().getTimeFrom();
        String tmFrom = KDSUtil.convertTimeToShortString(dt);

        dt =  getCondition().getDailyReportCondition().getTimeTo();
        String tmTo = KDSUtil.convertTimeToShortString(dt);

        csv += "Time from " + tmFrom;
        csv += " to " + tmTo;

        return csv;
    }

    protected String addTimeSlotGroup2CSV(String csv)
    {
        ConditionBase.TimeSlot ts  =  getCondition().getDailyReportCondition().getTimeSlot();

        String s = ConditionBase.getTimeSlotString(ts);

        csv += "TimeSlot " +s ;
        return csv;
    }

    protected void importDateGroup2Xml(KDSXML xml)
    {
        xml.getFirstGroup("Date");
        String s = xml.getAttribute("from", "");
        this.getCondition().getDailyReportCondition().setDate(KDSUtil.convertShortStringToDate(s));

        xml.back_to_parent();
    }

    protected void importTimeGroup2Xml(KDSXML xml)
    {
        xml.getFirstGroup("Time" );
        String s = xml.getAttribute("from", "");
        getCondition().getDailyReportCondition().setTimeFromString(s);
        s = xml.getAttribute("to", "");
        getCondition().getDailyReportCondition().setTimeToString(s);
        xml.back_to_parent();

    }

    protected void importTimeSlotGroup2Xml(KDSXML xml)
    {
        xml.getFirstGroup("TimeSlot");
        String s = xml.getCurrentGroupValue();
        getCondition().getDailyReportCondition().setTimeSlot( ConditionBase.getTimeSlotFromString(s) );
        xml.back_to_parent();
    }

    /**
     * daily_date
     * @return
     */
    public String getReportFileName()
    {
        String s = super.getReportFileName();
        Date dt =  getCondition().getDailyReportCondition().getDate();
        s +="_" + KDSUtil.convertDateToDbString(dt);
        s += ".xml";
        return s;
    }

}