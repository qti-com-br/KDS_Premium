package com.bematechus.kdslib;

import java.util.Date;

/**
 * Created by Administrator on 2016/8/12.
 */
public class ReportOrderOneTime extends TimeSlotOrderReport{//} StatisticOrderReport {

    public String getMonthName(int nCalendarMonth)
    {
        return ConditionBase.getMonthName(nCalendarMonth);

    }


    public String getTitleString()
    {

        String s = KDSApplication.getContext().getString(R.string.statistic_report);
        s += " - ";
        return s;

    }
    public void addDateGroup2Xml(KDSXML xml)
    {
        Date dt =  getCondition().getOneTimeCondition().getDateFrom();
        String dtFrom = KDSUtil.convertDateToShortString(dt);
        dt = getCondition().getOneTimeCondition().getDateTo();
        String dtTo = KDSUtil.convertDateToShortString(dt);

        xml.newGroup("Date", true);
        xml.newAttribute("from", dtFrom);
        xml.newAttribute("to", dtTo);
        xml.back_to_parent();
    }
    public void addTimeGroup2Xml(KDSXML xml)
    {
        Date dt =  getCondition().getOneTimeCondition().getTimeFrom();
        String tmFrom = KDSUtil.convertTimeToShortString(dt);

        dt =  getCondition().getOneTimeCondition().getTimeTo();
        String tmTo = KDSUtil.convertTimeToShortString(dt);

        xml.newGroup("Time", true);
        xml.newAttribute("from", tmFrom);
        xml.newAttribute("to", tmTo);
        xml.back_to_parent();
    }

    public void addTimeSlotGroup2Xml(KDSXML xml)
    {
        ConditionBase.TimeSlot ts  =  getCondition().getOneTimeCondition().getTimeSlot();

        String s = ConditionBase.getTimeSlotString(ts);

        xml.newGroup("TimeSlot",s, false);

    }

    protected String addDateGroup2CSV(String csv)
    {
        Date dt =  getCondition().getOneTimeCondition().getDateFrom();
        String dtFrom = KDSUtil.convertDateToShortString(dt);
        dt = getCondition().getOneTimeCondition().getDateTo();
        String dtTo = KDSUtil.convertDateToShortString(dt);

        csv += "Date from " + dtFrom;
        csv += " to " + dtTo;

        return csv;
    }

    protected String addTimeGroup2CSV(String csv)
    {
        Date dt =  getCondition().getOneTimeCondition().getTimeFrom();
        String tmFrom = KDSUtil.convertTimeToShortString(dt);

        dt =  getCondition().getOneTimeCondition().getTimeTo();
        String tmTo = KDSUtil.convertTimeToShortString(dt);

        csv += "Time from " + tmFrom;
        csv += " to " + tmTo;

        return csv;
    }

    protected String addTimeSlotGroup2CSV(String csv)
    {
        ConditionBase.TimeSlot ts  =  getCondition().getOneTimeCondition().getTimeSlot();

        String s = ConditionBase.getTimeSlotString(ts);

        csv += "TimeSlot " + s;
        return csv;
    }


    /**
     * onetime_date_date.xml
     * @return
     */
    public String getReportFileName()
    {
        String s = super.getReportFileName();
        Date dt =  getCondition().getOneTimeCondition().getDateFrom();
        String dtFrom = KDSUtil.convertDateToDbString(dt);
        dt = getCondition().getOneTimeCondition().getDateTo();
        String dtTo = KDSUtil.convertDateToDbString(dt);
        s += "_" + dtFrom;
        s += "_"+dtTo;

        dt =  getCondition().getOneTimeCondition().getTimeFrom();
        String tmFrom = KDSUtil.convertTimeToFileNameString(dt);

        dt =  getCondition().getOneTimeCondition().getTimeTo();
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
        this.getCondition().getOneTimeCondition().setDateFromString(s);

        s = xml.getAttribute("to", "");
        this.getCondition().getOneTimeCondition().setDateToString(s);


        xml.back_to_parent();
    }

    protected void importTimeGroup2Xml(KDSXML xml)
    {
        xml.getFirstGroup("Time" );
        String s = xml.getAttribute("from", "");
        getCondition().getOneTimeCondition().setTimeFromString(s);
        s = xml.getAttribute("to", "");
        getCondition().getOneTimeCondition().setTimeToString(s);
        xml.back_to_parent();
    }

    protected void importTimeSlotGroup2Xml(KDSXML xml)
    {
        xml.getFirstGroup("TimeSlot");
        String s = xml.getCurrentGroupValue();
        getCondition().getOneTimeCondition().setTimeSlot( ConditionBase.getTimeSlotFromString(s) );
        xml.back_to_parent();
    }
}
