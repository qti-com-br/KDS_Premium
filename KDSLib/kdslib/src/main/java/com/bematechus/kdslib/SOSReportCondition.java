
package com.bematechus.kdslib;

import android.os.Environment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;



/**
 * For create the SOS prep time report
 */
public class SOSReportCondition extends ConditionBase {

    static public Date g_dtReportStart = new Date(); //the whole started date.
    enum ReportContent
    {
        Order_PrepTime

    }

    //GUID for identify this report. As we can not get report from kitched station in real time.
    String m_strReportID = KDSUtil.createNewGUID(); //for which report, it should same with SOSReport reportID.
    ArrayList<SOSStationConfig> m_arStations = new ArrayList<>(); //which station needs to return report.

    Date m_dtDeadline = new Date();//from this date back to period

    Date m_dtCreatedDate = new Date(); //the created date is different with the deadline time.
    //String m_strToStation="";
    ReportContent m_orderReportContent = ReportContent.Order_PrepTime;

    //unit is seconds
    int m_nRealPrepTimePeriod = 0; //for real time average prep time
    int m_nGraphPrepTimeInterval = 0;
    int m_nGraphPrepTimeDuration = 0;

    int m_nTargetSeconds = 0;

    Object m_tag = null;

    /**
     *
     * @return
     *  The date that this condition created.
     */
    public Date getCreatedDate()
    {
        return m_dtCreatedDate;
    }

    /**
     *
     * @param obj
     */
    public void setTag(Object obj)
    {
        m_tag = obj;
    }

    public Object getTag()
    {
        return m_tag;
    }

    public Date getDeadline()
    {
        return m_dtDeadline;
    }
    public void setDeadlineToNow()
    {
        m_dtDeadline = new Date();

    }

    static public Date getGraphEndTime()
    {
        Calendar calEnd = Calendar.getInstance();
        //calEnd.add(Calendar.HOUR_OF_DAY, 1);
        int nMinutes = calEnd.get(Calendar.MINUTE);
        nMinutes = nMinutes /10;
        nMinutes *= 10;

        calEnd.set(Calendar.MINUTE, nMinutes);
        calEnd.set(Calendar.SECOND,0);
        return calEnd.getTime();
    }
    /**
     *
     *
     *  unit: seconds
     *  rev.
     *      1.1.4, from 00 to show report.
     */
    public void autoCreateDeadline()
    {
        //long dtStart = g_dtReportStart.getTime();
        //long dtNow = (new Date()).getTime();

//        Calendar calEnd = Calendar.getInstance();
//        calEnd.add(Calendar.HOUR_OF_DAY, 1);
//        calEnd.set(Calendar.MINUTE, 0);
//        calEnd.set(Calendar.SECOND,0);

        long dtNow = getGraphEndTime().getTime();// calEnd.getTime().getTime();
        //dtNow += getGraphPrepTimeInterval() * 1000; //the last dot data from future.

        //long dtStart = dtNow - getGraphPrepTimeDuration() * 1000;

//        while (dtStart < dtNow)
//        {
//            dtStart += (m_nGraphPrepTimeInterval*1000);
//        }

        //m_dtDeadline.setTime(dtStart);
        m_dtDeadline.setTime(dtNow);
    }

    public void setReportID(String strID)
    {
        m_strReportID = strID;
    }
    public String getReportID()
    {
        return m_strReportID;
    }

    public void setToStations(ArrayList<SOSStationConfig>  stationIDs)
    {
        m_arStations.clear();
        m_arStations.addAll(stationIDs);

    }
    public ArrayList<SOSStationConfig>  getToStations()
    {
        return m_arStations;
    }

    public void setOrderReportContent(ReportContent m)
    {
        m_orderReportContent = m;
    }
    public ReportContent getOrderReportContent()
    {
        return m_orderReportContent;
    }

    public int getRealPrepTimePeriodSeconds()
    {
        return m_nRealPrepTimePeriod;
    }
    public void setRealPrepTimePeriodSeconds(int periodSeconds)
    {
        m_nRealPrepTimePeriod = periodSeconds;
    }

    public int getGraphPrepTimeInterval()
    {
        return m_nGraphPrepTimeInterval;
    }
    public void setGraphPrepTimeInterval(int seconds)
    {
        m_nGraphPrepTimeInterval = seconds;
    }


    public int getGraphPrepTimeDuration()
    {
        return m_nGraphPrepTimeDuration;
    }
    public void setGraphPrepTimeDuration(int seconds)
    {
        m_nGraphPrepTimeDuration = seconds;
    }

    public int getTargetSeconds()
    {
        return m_nTargetSeconds;
    }
    public void setTargetSeconds(int seconds)
    {
        m_nTargetSeconds = seconds;
    }


    public static String SOS_REPORT_CONDITION = "SOSCondition";
    public static String SOS_REPORT_GUID = "rpguid";
    public static String SOS_RPTYPE = "rptype";
    public static String SOS_RPMODE = "rpmode";
    public static String SOS_DEADLINE = "deadline";
    public static String SOS_GRAPH_DURATION = "graghduration";
    public static String SOS_GRAPH_INTERVAL = "graghinterval";
    public static String SOS_REAL_PERIOD = "realperiod";
    public static String SOS_TARGET_SECONDS = "target";

    /**
     * format:
     *  <SOSCondition rptype=0 rpmode=0 deadline=2018-12-12 10:20:02 graphduration=30 graphinerval=10 realperiod=39>
     *
     *  </SOSCondition>
     * @param xml
     */
    public void export2Xml(KDSXML xml)
    {
        xml.new_doc_with_root(SOS_REPORT_CONDITION);
        xml.newAttribute(SOS_RPTYPE, KDSUtil.convertIntToString(m_orderReportContent.ordinal()));
        xml.newAttribute(SOS_RPMODE, KDSUtil.convertIntToString(getReportMode().ordinal()));
        xml.newAttribute(SOS_REPORT_GUID, getReportID());

        xml.newAttribute(SOS_DEADLINE, KDSUtil.convertDateToString(m_dtDeadline));
        xml.newAttribute(SOS_GRAPH_DURATION,  KDSUtil.convertIntToString(m_nGraphPrepTimeDuration));
        xml.newAttribute(SOS_GRAPH_INTERVAL, KDSUtil.convertIntToString(m_nGraphPrepTimeInterval));
        xml.newAttribute(SOS_REAL_PERIOD, KDSUtil.convertIntToString(m_nRealPrepTimePeriod));

        xml.newAttribute(SOS_TARGET_SECONDS, KDSUtil.convertIntToString(m_nTargetSeconds));

        xml.back_to_parent();

    }

    public void importFromXml(KDSXML xml)
    {
        xml.back_to_root();
        String s = xml.getAttribute(SOS_RPTYPE, "0");
        m_orderReportContent = ReportContent.values()[ KDSUtil.convertStringToInt(s, 0)];

        s = xml.getAttribute(SOS_RPMODE, "0");
        m_reportMode = ReportMode.values()[ KDSUtil.convertStringToInt(s, 0)];

        s = xml.getAttribute(SOS_REPORT_GUID, "");
        m_strReportID = s;

        s = xml.getAttribute(SOS_DEADLINE, "");
        m_dtDeadline = KDSUtil.convertStringToDate(s, KDSUtil.createInvalidDate());

        s = xml.getAttribute(SOS_GRAPH_DURATION, "0");
        m_nGraphPrepTimeDuration = KDSUtil.convertStringToInt(s, 0);


        s = xml.getAttribute(SOS_GRAPH_INTERVAL, "0");
        m_nGraphPrepTimeInterval = KDSUtil.convertStringToInt(s, 0);

        s = xml.getAttribute(SOS_REAL_PERIOD, "0");
        m_nRealPrepTimePeriod = KDSUtil.convertStringToInt(s, 0);

        s = xml.getAttribute(SOS_TARGET_SECONDS, "0");
        m_nTargetSeconds = KDSUtil.convertStringToInt(s, 0);


        xml.back_to_parent();



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

//    static public boolean exportToFile(TimeSlotOrderReport report)
//    {
//        String s = report.export2Xml();
//        KDSUtil.createFolder(TimeSlotOrderReport.getStatisticFolder());
//        String filename = report.getReportFileName();
//        filename = TimeSlotOrderReport.getStatisticFolderFullPath() + filename;
//        return KDSUtil.fileWrite(filename, s);
//    }

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
    static public boolean exportProfileToFile(SOSReportCondition condition, String fileName)
    {
        String s = condition.export2String();
        KDSUtil.createFolder(SOSReportCondition.getProfileFolder());
        String filename = condition.getProfileNewName();
        if (!fileName.isEmpty())
            filename = fileName;
        filename = SOSReportCondition.getProfileFolderFullPath() + filename;
        return KDSUtil.fileWrite(filename, s);
    }


    public String getProfileNewName()
    {
        String s =  "Profile_sos_";

        Date dt = new Date();
        s +="_" + KDSUtil.convertDateToDbString(dt);
        s += ".xml";
        return s;


    }


    static public SOSReportCondition importXmlString(String strXml)
    {
        SOSReportCondition c = new SOSReportCondition();
        KDSXML xml = new KDSXML();

        xml.loadString(strXml);
        c.importFromXml(xml);

        return c;
    }

    public boolean isSameCondition(SOSReportCondition condition)
    {

        if (!condition.getReportID().equals(condition.getReportID()))
            return false;
        if (condition.getReportMode() != this.getReportMode())
            return false;
        if (condition.getOrderReportContent() != this.getOrderReportContent())
            return false;
        if (condition.getGraphPrepTimeDuration() != this.getGraphPrepTimeDuration())
            return false;
        if (condition.getGraphPrepTimeInterval() != this.getGraphPrepTimeInterval())
            return false;
        if (condition.getRealPrepTimePeriodSeconds() != this.getRealPrepTimePeriodSeconds())
            return false;
        return true;
    }

    public boolean copyFrom(SOSReportCondition condition)
    {

        this.setReportMode(condition.getReportMode());
        this.setOrderReportContent(condition.getOrderReportContent());

        this.setGraphPrepTimeDuration(condition.getGraphPrepTimeDuration());
        this.setGraphPrepTimeInterval(condition.getGraphPrepTimeInterval());
        this.setRealPrepTimePeriodSeconds(condition.getRealPrepTimePeriodSeconds());
        return true;
    }

}
