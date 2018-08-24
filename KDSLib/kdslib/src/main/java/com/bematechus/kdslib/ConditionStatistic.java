
package com.bematechus.kdslib;

import android.os.Environment;

import java.util.Date;

/**
 * SAME AS KDS FILE
 */

/**
 * All condition is here
 */
public class ConditionStatistic extends ConditionBase{

    public enum StationMode
    {
        Individual,
        Combine,
    }
    public enum OrderReportContent
    {
        Counter,
        PrepTime,//from start to finished time.
    }
    public enum ReportType
    {
        Daily,
        Weekly,
        Monthly,
        OneTime,
    }



    String m_strStationFrom;
    String m_strStationTo;

    String m_strItemDescription = "";


    OrderReportContent m_orderReportContent = OrderReportContent.Counter;

    ReportType m_nReportType = ReportType.Daily;
    ConditionOneTime m_onetimeCondition = new ConditionOneTime();
    ConditionDailyReport m_dailyCondition = new ConditionDailyReport();
    ConditionWeekly m_weeklyCondition = new ConditionWeekly();
    ConditionMonthly m_monthlyCondition = new ConditionMonthly();


    public void setItemDescription(String strDescription)
    {
        m_strItemDescription = strDescription;
    }

    public String getItemDescription()
    {
        return m_strItemDescription;
    }

    public ConditionMonthly getMonthlyCondition()
    {
        return m_monthlyCondition;
    }

    public ConditionWeekly getWeeklyCondition()
    {
        return m_weeklyCondition;
    }

    public ConditionDailyReport getDailyReportCondition()
    {
        return m_dailyCondition;
    }
    public ConditionOneTime getOneTimeCondition()
    {
        return m_onetimeCondition;
    }

    public void setStationFrom(String stationID)
    {
        m_strStationFrom = stationID;
    }
    public String getStationFrom()
    {
        return m_strStationFrom;
    }

    public void setStationTo(String stationID)
    {
        m_strStationTo = stationID;
    }
    public String getStationTo()
    {
        return m_strStationTo;
    }

    public void setOrderReportContent(OrderReportContent m)
    {
        m_orderReportContent = m;
    }
    public OrderReportContent getOrderReportContent()
    {
        return m_orderReportContent;
    }


    public void setReportType(ReportType rt)
    {
        m_nReportType = rt;
    }
    public ReportType getReportType()
    {
        return m_nReportType;
    }


    public void export2Xml(KDSXML xml)
    {
        xml.new_doc_with_root("Condition");
        xml.newAttribute("rptype", KDSUtil.convertIntToString(m_nReportType.ordinal()));
        xml.newAttribute("rpmode", KDSUtil.convertIntToString(getReportMode().ordinal()));
        xml.newAttribute("stationfrom", m_strStationFrom);
        xml.newAttribute("stationto", m_strStationTo);
        xml.newAttribute("item", m_strItemDescription);
        xml.back_to_parent();
        switch (m_nReportType)
        {

            case Daily:
                m_dailyCondition.export2Xml(xml);
                break;
            case Weekly:
                m_weeklyCondition.export2Xml(xml);
                break;
            case Monthly:
                m_monthlyCondition.export2Xml(xml);
                break;
            case OneTime:
                m_onetimeCondition.export2Xml(xml);
                break;
        }
    }

    public void importFromXml(KDSXML xml)
    {
        xml.back_to_root();
        String s = xml.getAttribute("rptype", "0");
        m_nReportType = ReportType.values()[ KDSUtil.convertStringToInt(s, 0)];

        s = xml.getAttribute("rpmode", "0");
        m_reportMode = ReportMode.values()[ KDSUtil.convertStringToInt(s, 0)];
        m_orderReportContent = OrderReportContent.values()[ 0 ];

        s = xml.getAttribute("stationfrom", "0");
        m_strStationFrom = s;
        s = xml.getAttribute("stationto", "5");
        m_strStationTo = s;

        s = xml.getAttribute("item", "");
        m_strItemDescription = s;

        xml.back_to_parent();

        switch (m_nReportType)
        {
            case Daily:
                m_dailyCondition.importFromXml(xml);
                break;
            case Weekly:
                m_weeklyCondition.importFromXml(xml);
                break;
            case Monthly:
                m_monthlyCondition.importFromXml(xml);
                break;
            case OneTime:
                m_onetimeCondition.importFromXml(xml);
                break;
        }

    }


    public String export2String()
    {
        KDSXML xml = new KDSXML();
        this.export2Xml(xml);
        String s = xml.get_xml_string();
        return s;
    }

    private static String FOLDER_NAME = "KDSStatisticReport/Profile";
    static public String getProfileFolder() {
        return Environment.getExternalStorageDirectory() + "/" + FOLDER_NAME;
    }
    static public String getProfileFolderFullPath() {

        return getProfileFolder() + "/";

    }

    static public boolean exportToFile(TimeSlotOrderReport report)
    {
        String s = report.export2Xml();
        KDSUtil.createFolder(TimeSlotOrderReport.getStatisticFolder());
        String filename = report.getReportFileName();
        filename = TimeSlotOrderReport.getStatisticFolderFullPath() + filename;
        return KDSUtil.fileWrite(filename, s);
    }

    public boolean importFile(String filePathName)
    {
        String filePath =filePathName;
        KDSXML xml = new KDSXML();
        String s = KDSUtil.readFile(filePath);
        xml.loadString(s);
        this.importFromXml(xml);

        return true;
    }


    /**
     *
     * @param condition
     * @param fileName
     *  just the name, no path in it.
     * @return
     */
    static public boolean exportProfileToFile(ConditionStatistic condition, String fileName)
    {
        String s = condition.export2String();
        KDSUtil.createFolder(ConditionStatistic.getProfileFolder());
        String filename = condition.getProfileNewName();
        if (!fileName.isEmpty())
            filename = fileName;
        filename = ConditionStatistic.getProfileFolderFullPath() + filename;
        return KDSUtil.fileWrite(filename, s);
    }


    public String getProfileNewName()
    {
        String s =  "Profile_" + getReportType().toString();
        switch (m_nReportType)
        {

            case Daily:
            {

                Date dt = getDailyReportCondition().getDate();
                s +="_" + KDSUtil.convertDateToDbString(dt);
                s += ".xml";
                return s;
            }

            case Weekly:
            {

                Date dt =  getWeeklyCondition().getWeekFirstDayDate();
                String dtFrom = KDSUtil.convertDateToDbString(dt);
                dt = getWeeklyCondition().getWeekLastDayDate();
                String dtTo = KDSUtil.convertDateToDbString(dt);
                s += "_" + dtFrom;
                s += "_"+dtTo;

                dt =  getWeeklyCondition().getTimeFrom();
                String tmFrom = KDSUtil.convertTimeToFileNameString(dt);

                dt =  getWeeklyCondition().getTimeTo();
                String tmTo = KDSUtil.convertTimeToFileNameString(dt);
                s += "_"+tmFrom;
                s += "_" + tmTo;
                s += ".xml";
                return s;
            }

            case Monthly:
            {

                Date dt = getMonthlyCondition().getMonthFirstDay();
                String dtFrom = KDSUtil.convertDateToDbString(dt);
                dt = getMonthlyCondition().getMonthLastDay();
                String dtTo = KDSUtil.convertDateToDbString(dt);
                s += "_" + dtFrom;
                s += "_"+dtTo;

                dt = getMonthlyCondition().getTimeFrom();
                String tmFrom = KDSUtil.convertTimeToFileNameString(dt);

                dt = getMonthlyCondition().getTimeTo();
                String tmTo = KDSUtil.convertTimeToFileNameString(dt);
                s += "_"+tmFrom;
                s += "_" + tmTo;
                s += ".xml";
                return s;
            }

            case OneTime:
            {

                Date dt = getOneTimeCondition().getDateFrom();
                String dtFrom = KDSUtil.convertDateToDbString(dt);
                dt = getOneTimeCondition().getDateTo();
                String dtTo = KDSUtil.convertDateToDbString(dt);
                s += "_" + dtFrom;
                s += "_"+dtTo;

                dt = getOneTimeCondition().getTimeFrom();
                String tmFrom = KDSUtil.convertTimeToFileNameString(dt);

                dt = getOneTimeCondition().getTimeTo();
                String tmTo = KDSUtil.convertTimeToFileNameString(dt);
                s += "_"+tmFrom;
                s += "_" + tmTo;
                s += ".xml";
                return s;
            }

        }
        return "";
    }


    static public ConditionStatistic importXmlString(String strXml)
    {
        ConditionStatistic c = new ConditionStatistic();
        KDSXML xml = new KDSXML();

        xml.loadString(strXml);
        c.importFromXml(xml);

        return c;
    }

    public boolean isSameCondition(ConditionStatistic condition)
    {
        if (condition.getReportType() != this.getReportType())
            return false;
        if (condition.getReportMode() != this.getReportMode())
            return false;
        switch (condition.getReportType())
        {

            case Daily:
                if ( !KDSUtil.isSameDate( this.getDailyReportCondition().getDate(), condition.getDailyReportCondition().getDate()))
                    return false;
                if (this.getDailyReportCondition().getTimeSlot() != condition.getDailyReportCondition().getTimeSlot())
                    return false;
                if (!KDSUtil.isSameTimeHM( this.getDailyReportCondition().getTimeFrom(), condition.getDailyReportCondition().getTimeFrom()))
                    return false;
                if (!KDSUtil.isSameTimeHM( this.getDailyReportCondition().getTimeTo(), condition.getDailyReportCondition().getTimeTo()))
                    return false;
                break;
            case Weekly:
                if (!KDSUtil.isSameDate(this.getWeeklyCondition().getWeekFirstDayDate() , condition.getWeeklyCondition().getWeekFirstDayDate()))
                    return false;

                if (!KDSUtil.isSameTimeHM( this.getWeeklyCondition().getTimeFrom(), condition.getWeeklyCondition().getTimeFrom()))
                    return false;
                if (!KDSUtil.isSameTimeHM( this.getWeeklyCondition().getTimeTo() , condition.getWeeklyCondition().getTimeTo()))
                    return false;
                break;
            case Monthly:
                if (!KDSUtil.isSameDate(this.getMonthlyCondition().getMonthFirstDay() , condition.getMonthlyCondition().getMonthFirstDay()))
                    return false;

                if (!KDSUtil.isSameTimeHM( this.getMonthlyCondition().getTimeFrom() , condition.getMonthlyCondition().getTimeFrom()))
                    return false;
                if (!KDSUtil.isSameTimeHM( this.getMonthlyCondition().getTimeTo(), condition.getMonthlyCondition().getTimeTo()))
                    return false;
                break;
            case OneTime:
                if (!KDSUtil.isSameDate(this.getOneTimeCondition().getDateFrom() , condition.getOneTimeCondition().getDateFrom()))
                    return false;
                if (!KDSUtil.isSameDate(this.getOneTimeCondition().getDateTo() , condition.getOneTimeCondition().getDateTo()))
                    return false;
                if (this.getOneTimeCondition().getTimeSlot() != condition.getOneTimeCondition().getTimeSlot())
                    return false;
                if (!KDSUtil.isSameTimeHM( this.getOneTimeCondition().getTimeFrom() , condition.getOneTimeCondition().getTimeFrom()))
                    return false;
                if (!KDSUtil.isSameTimeHM( this.getOneTimeCondition().getTimeTo() ,condition.getOneTimeCondition().getTimeTo()))
                    return false;
                if (this.getOneTimeCondition().getEnableDayOfWeek() != condition.getOneTimeCondition().getEnableDayOfWeek())
                    return false;
                break;
        }
        return true;
    }
    public boolean copyDateTime(ConditionStatistic condition)
    {
        switch (condition.getReportType())
        {

            case Daily:
                this.getDailyReportCondition().setDate( condition.getDailyReportCondition().getDate());
                this.getDailyReportCondition().setTimeFrom( condition.getDailyReportCondition().getTimeFrom());
                this.getDailyReportCondition().setTimeTo(condition.getDailyReportCondition().getTimeTo());

                break;
            case Weekly:
                this.getWeeklyCondition().setWeekFirstDay(condition.getWeeklyCondition().getWeekFirstDayDate());
                this.getWeeklyCondition().setTimeFrom( condition.getWeeklyCondition().getTimeFrom());
                this.getWeeklyCondition().setTimeTo( condition.getWeeklyCondition().getTimeTo());
                break;
            case Monthly:
                this.getMonthlyCondition().setMonthFirstDay( condition.getMonthlyCondition().getMonthFirstDay());
                this.getMonthlyCondition().setTimeFrom( condition.getMonthlyCondition().getTimeFrom());
                this.getMonthlyCondition().setTimeTo( condition.getMonthlyCondition().getTimeTo());
                break;
            case OneTime:
                this.getOneTimeCondition().setDateFrom( condition.getOneTimeCondition().getDateFrom());
                this.getOneTimeCondition().setDateTo( condition.getOneTimeCondition().getDateTo());
                this.getOneTimeCondition().setTimeFrom( condition.getOneTimeCondition().getTimeFrom());
                this.getOneTimeCondition().setTimeTo(condition.getOneTimeCondition().getTimeTo());
                break;
        }
        return true;
    }

    public void adjustMonthWeekFirstDay()
    {
        switch (this.getReportType())
        {

            case Daily:

                break;
            case Weekly:
                this.getWeeklyCondition().setWeekFirstDay(this.getWeeklyCondition().getWeekFirstDayDate());
                break;
            case Monthly:
                this.getMonthlyCondition().setMonthFirstDay( this.getMonthlyCondition().getMonthFirstDay());


                break;
            case OneTime:
                break;
        }

    }

}
