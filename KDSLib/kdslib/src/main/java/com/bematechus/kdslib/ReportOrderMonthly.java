package com.bematechus.kdslib;

import java.util.Date;

/**
 * Created by Administrator on 2016/8/12.
 */
public class ReportOrderMonthly extends TimeSlotOrderReport{



    public void next()
    {
        this.getCondition().getMonthlyCondition().nextMonth();

    }
    public void prev()
    {
        this.getCondition().getMonthlyCondition().prevMonth();
    }

    public String getTitleString()
    {

        String s = KDSApplication.getContext().getString(R.string.monthly_report);
        s += " - ";
        return s;

    }

    public String getFixedColString()
    {
        if (getCondition().getReportMode() == ConditionBase.ReportMode.Order)
            return KDSApplication.getContext().getString(R.string.day_of_month);// "Day of Month";
        else
        {
            return KDSApplication.getContext().getString(R.string.item_description);// "Day of Month";
        }
    }

    public void addDateGroup2Xml(KDSXML xml)
    {
        Date dt =  getCondition().getMonthlyCondition().getMonthFirstDay();
        String dtFrom = KDSUtil.convertDateToShortString(dt);
        dt = getCondition().getMonthlyCondition().getMonthLastDay();
        String dtTo = KDSUtil.convertDateToShortString(dt);

        xml.newGroup("Date", true);
        xml.newAttribute("from", dtFrom);
        xml.newAttribute("to", dtTo);
        xml.back_to_parent();
    }
    public void addTimeGroup2Xml(KDSXML xml)
    {
        Date dt =  getCondition().getMonthlyCondition().getTimeFrom();
        String tmFrom = KDSUtil.convertTimeToShortString(dt);

        dt =  getCondition().getMonthlyCondition().getTimeTo();
        String tmTo = KDSUtil.convertTimeToShortString(dt);

        xml.newGroup("Time", true);
        xml.newAttribute("from", tmFrom);
        xml.newAttribute("to", tmTo);
        xml.back_to_parent();
    }

    protected String addDateGroup2CSV(String csv)
    {
        Date dt =  getCondition().getMonthlyCondition().getMonthFirstDay();
        String dtFrom = KDSUtil.convertDateToShortString(dt);
        dt = getCondition().getMonthlyCondition().getMonthLastDay();
        String dtTo = KDSUtil.convertDateToShortString(dt);

        csv += "Date from " + dtFrom;
        csv += " to " + dtTo;

        return csv;
    }

    protected String addTimeGroup2CSV(String csv)
    {
        Date dt =  getCondition().getMonthlyCondition().getTimeFrom();
        String tmFrom = KDSUtil.convertTimeToShortString(dt);

        dt =  getCondition().getMonthlyCondition().getTimeTo();
        String tmTo = KDSUtil.convertTimeToShortString(dt);

        csv += "Time from " + tmFrom;
        csv += " to " + tmTo;

        return csv;
    }


    /**
     * monthly_date_date.xml
     * @return
     */
    public String getReportFileName()
    {
        String s = super.getReportFileName();
        Date dt =  getCondition().getMonthlyCondition().getMonthFirstDay();
        String dtFrom = KDSUtil.convertDateToDbString(dt);
        dt = getCondition().getMonthlyCondition().getMonthLastDay();
        String dtTo = KDSUtil.convertDateToDbString(dt);
        s += "_" + dtFrom;
        s += "_"+dtTo;

        dt =  getCondition().getMonthlyCondition().getTimeFrom();
        String tmFrom = KDSUtil.convertTimeToFileNameString(dt);

        dt =  getCondition().getMonthlyCondition().getTimeTo();
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
        this.getCondition().getMonthlyCondition().setMonthFirstDay(KDSUtil.convertShortStringToDate(s));



        xml.back_to_parent();
    }

    protected void importTimeGroup2Xml(KDSXML xml)
    {
        xml.getFirstGroup("Time" );
        String s = xml.getAttribute("from", "");
        getCondition().getMonthlyCondition().setTimeFromString(s);
        s = xml.getAttribute("to", "");
        getCondition().getMonthlyCondition().setTimeToString(s);
        xml.back_to_parent();
    }

    protected void importTimeSlotGroup2Xml(KDSXML xml)
    {

    }
}
