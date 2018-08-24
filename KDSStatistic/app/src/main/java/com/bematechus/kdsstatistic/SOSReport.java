package com.bematechus.kdsstatistic;

import com.bematechus.kdslib.SOSReportCondition;
import com.bematechus.kdslib.SOSReportOneStation;
import com.bematechus.kdslib.SOSReportTimeSlotData;

import java.util.ArrayList;

//import com.bematechus.kdslib.ConditionBase;
//import com.bematechus.kdslib.ConditionStatistic;
//import com.bematechus.kdslib.KDSApplication;
//import com.bematechus.kdslib.KDSUtil;
//import com.bematechus.kdslib.KDSXML;
//import com.bematechus.kdslib.ReportOrderDaily;
//import com.bematechus.kdslib.ReportOrderMonthly;
//import com.bematechus.kdslib.ReportOrderOneTime;
//import com.bematechus.kdslib.ReportOrderWeekly;
//import com.bematechus.kdslib.TimeSlotOrderReport;

/**
 *
 */
public class SOSReport {


    ArrayList<SOSReportOneStation> m_arData = new ArrayList<>();

    SOSReportCondition m_condition = new SOSReportCondition();


    public int getNeedCols()
    {
        if (this.getData().size()<=0)
            return 0;
        int n = this.getData().get(0).getTimeslots().size();
        return n + 1 ;

//        int nFrom = KDSUtil.convertStringToInt( m_condition.getStationFrom(), 0);
//        int nTo = KDSUtil.convertStringToInt( m_condition.getStationTo(), 0);
//        return nTo - nFrom + 1+1+1; //fixed col, total col

    }

    public void setCondition(SOSReportCondition condition)
    {
        m_condition = condition;
    }
    public SOSReportCondition getCondition()
    {
        if (m_condition == null)
            m_condition = new SOSReportCondition();
        return m_condition;
    }

    public void add(SOSReportOneStation entry)
    {
        m_arData.add(entry);
    }


    public ArrayList<SOSReportOneStation> getData()
    {
        return m_arData;
    }




    public int getStationTotalCounter(int nIndex)
    {
        float flt =0;
        for (int i=0; i< m_arData.size(); i++)
        {
            flt += m_arData.get(i).getOrderCount(nIndex);
        }
        return (int)flt;
    }
    public int getStationTotalBumpTimeSeconds(int nIndex)
    {
        int flt =0;
        for (int i=0; i< m_arData.size(); i++)
        {
            flt += m_arData.get(i).getOrderBumpTimeSeconds(nIndex);
        }
        return flt;
    }

    public int getTotalOrderCount()
    {
        if (m_arData.size()<=0) return 0;
        SOSReportOneStation entry = m_arData.get(m_arData.size()-1); //last cell is the total
        SOSReportTimeSlotData detail = entry.getTimeslots().get(entry.getTimeslots().size()-1);
        return (int)detail.getCount();


    }

    /**
     * unit mins
     * @return
     */
    public float getTotalBumpTime()
    {
        if (m_arData.size()<=0) return 0;
        SOSReportOneStation entry = m_arData.get(m_arData.size()-1);//last cell is the total
        SOSReportTimeSlotData detail = entry.getTimeslots().get(entry.getTimeslots().size()-1);
        float flt = detail.getBumpTimeSeconds();

        return (flt / 60); //unit is minutes


    }


    public float getAverageOrderCountPerTimeslot() {
        int nTotalOrderCount = getTotalOrderCount();
        int n = m_arData.size() - 1;
        if (n <= 0) return 0;
        return nTotalOrderCount / n;
    }

    /**
     * unit minutes
     * @return
     */
    public float getAverageOrderPrepTime() {
        int nTotalOrderCount = getTotalOrderCount();
        float nMins = getTotalBumpTime();


        if (nTotalOrderCount <= 0) return 0;
        return nMins / nTotalOrderCount;
    }

    public boolean isExistedStation(String stationID)
    {
        for (int i=0; i< m_arData.size(); i++)
        {
            if (m_arData.get(i).getStationID().equals(stationID))
                return true;
        }
        return false;
    }

    public SOSReportOneStation findStationReport(String stationID)
    {
        for (int i=0; i< m_arData.size(); i++)
        {
            if (m_arData.get(i).getStationID().equals(stationID))
                return m_arData.get(i);
        }
        return null;
    }
//    /**
//     * xml format:
//     *  <StatisticReport reporttype=1>
//     *      <Station from= to=></Station>
//     *      <Date from=2016-01-02 to 2017-12-12></Date>
//     *      <Time from= to=></Dtime>
//     *      <TimeSlot>1</TimeSlot>
//     *      <TotalOrderCount></TotalOrderCount>
//     *      <TotalPrepTime></TotalPrepTime>
//     *      <AverageCountEachTimeSlot></AverageCountEachTimeSlot>
//     *      <AveragePrepTime></AveragePrepTime>
//     *
//     *      <OrdersCounter>
//     *          <TimeSlot from=12:10>2,4,5,7</TimeSlot>
//     *          ...
//     *      </OrdersCounter>
//     *      <PrepTime>
//     *          <T12:00>0.2,4.0,5.3,7.0</T12:00>
//     *          ...
//     *      </PrepTime>
//     *
//     *
//     *  </StatisticReport>
//     * @return
//     */
//    public String export2Xml()
//    {
//        KDSXML xml = new KDSXML();
//        xml.new_doc_with_root("StatisticReport");
//        xml.newAttribute("ReportType", getCondition().getReportType().toString());
//        xml.newAttribute("ReportMode", getCondition().getReportMode().toString());
//        xml.newAttribute("Item", getCondition().getItemDescription());
//        //String str =  xml.get_xml_string();
//
//        xml.newGroup("Station", true);
//        xml.newAttribute("from", getCondition().getStationFrom());
//        xml.newAttribute("to", getCondition().getStationTo());
//        xml.back_to_parent();
//        //str =  xml.get_xml_string();
//
//
//        addDateGroup2Xml(xml);
//        addTimeGroup2Xml(xml);
//        addTimeSlotGroup2Xml(xml);
//
//        xml.newGroup("TotalOrderCount",KDSUtil.convertIntToString(getTotalOrderCount()), false);
//        xml.newGroup("TotalPrepTime",KDSUtil.convertFloatToShortString(getTotalBumpTime()), false);
//        xml.newGroup("AverageCountEachTimeSlot",KDSUtil.convertFloatToShortString(getAverageOrderCountPerTimeslot()), false);
//        xml.newGroup("AveragePrepTime",KDSUtil.convertFloatToShortString(getAverageOrderPrepTime()), false);
//
//        xml.newGroup("OrdersCounter", true);
//        for (int i=0; i< m_arData.size(); i++)
//        {
//            String s = m_arData.get(i).getOrderCounterXmlText();
//            xml.newGroup("TimeSlot", s , true);
//            xml.newAttribute("from", m_arData.get(i).getFixedText());
//            xml.back_to_parent();
//        }
//        xml.back_to_parent();
//
//        xml.newGroup("PrepTime", true);
//        for (int i=0; i< m_arData.size(); i++)
//        {
//            String s = m_arData.get(i).getPrepTimeXmlText();
//            xml.newGroup("TimeSlot", s , true);
//            xml.newAttribute("from", m_arData.get(i).getFixedText());
//            xml.back_to_parent();
//        }
//        xml.back_to_parent();
//
//        return xml.get_xml_string();
//
//
//    }

//    public String getReportFileName()
//    {
//        return this.getCondition().getReportType().toString();
//    }
//    protected void addDateGroup2Xml(KDSXML xml)
//    {
//
//    }
//
//    protected void addTimeGroup2Xml(KDSXML xml)
//    {
//
//    }
//
//    protected void addTimeSlotGroup2Xml(KDSXML xml)
//    {
//
//    }

//    static public TimeSlotOrderReport importFromXml(String strXml)
//    {
//        return null;
////        TimeSlotOrderReport report = null;
////        KDSXML xml = new KDSXML();
////        xml.loadString(strXml);
////
////        xml.back_to_root();//        .new_doc_with_root("StatisticReport");
////        String reportType =  xml.getAttribute("ReportType","");// getCondition().getReportType().toString());
////        if (reportType.isEmpty()) return null;
////        ConditionStatistic.ReportType rt = ConditionStatistic.ReportType.valueOf(reportType);
////
////        switch (rt)
////        {
////            case Daily:
////            {
////                report = new ReportOrderDaily();
////            }
////            break;
////            case Weekly:
////                report = new ReportOrderWeekly();
////                break;
////            case Monthly:
////                report = new ReportOrderMonthly();
////                break;
////            case OneTime:
////                report = new ReportOrderOneTime();
////                break;
////        }
////        report.getCondition().setReportType(rt);
////        //String str =  xml.get_xml_string();
////        String s = "";
////        s = xml.getAttribute("ReportMode", "Order");
////        if (s.equals("Order"))
////            report.getCondition().setReportMode(ConditionBase.ReportMode.Order);
////        else if (s.equals("Item"))
////            report.getCondition().setReportMode(ConditionBase.ReportMode.Item);
////        s = xml.getAttribute("Item", "");
////        report.getCondition().setItemDescription(s);
////
////        xml.getFirstGroup("Station");
////
////        s = xml.getAttribute("from", "");
////        int nStationFrom = KDSUtil.convertStringToInt(s, 0);
////
////        report.getCondition().setStationFrom(s);
////
////        s = xml.getAttribute("to", "");
////        int nStationTo = KDSUtil.convertStringToInt(s, 0);
////
////        report.getCondition().setStationTo(s);
////
////        xml.back_to_parent();
////        //str =  xml.get_xml_string();
////
////        report.importDateGroup2Xml(xml);
////        //addDateGroup2Xml(xml);
////        report.importTimeGroup2Xml(xml);
////        report.importTimeSlotGroup2Xml(xml);
////
////        xml.getFirstGroup("OrdersCounter");
////        if (xml.getFirstGroup("TimeSlot"))
////        {
////            do {
////                s = xml.getCurrentGroupValue();
////                SOSReportEntry entry = new SOSReportEntry();
////                entry.setFixedText(xml.getAttribute("from", ""));
////                ArrayList<String> ar =  KDSUtil.spliteString(s, ",");
////                for (int i= nStationFrom; i<= nStationTo; i++)
////                {
////                    entry.add(KDSUtil.convertIntToString(i),KDSUtil.convertStringToInt(ar.get(i - nStationFrom), 0), 0 );
////                }
////                report.add(entry);
////            }while (xml.getNextGroup("TimeSlot"));
////        }
////
////
////        xml.back_to_parent();
////
////        int nIndex = 0;
////        xml.getFirstGroup("PrepTime");
////        if (xml.getFirstGroup("TimeSlot"))
////        {
////            do {
////                s = xml.getCurrentGroupValue();
//////                SOSReportEntry entry = report.get new SOSReportEntry();
//////                entry.setFixedText(xml.getAttribute("from", ""));
////                ArrayList<String> ar =  KDSUtil.spliteString(s, ",");
////                SOSReportEntry entry = report.getData().get(nIndex);
////                nIndex ++;
////                for (int i= nStationFrom; i<= nStationTo; i++)
////                {
////                    entry.getData().get(i - nStationFrom).setBumpTimeSeconds(KDSUtil.convertStringToFloat(ar.get(i-nStationFrom), 0));
////
////
////                }
////
////            }while (xml.getNextGroup("TimeSlot"));
////        }
////        xml.back_to_parent();
////
////        addTotal(report);
////
////        return report;
//    }

//    static public void addTotal(TimeSlotOrderReport report)
//    {
//        addStationSummary(report);
//        addTimeSlotSummary(report);
//    }
//
//    static public void removeTotal(TimeSlotOrderReport report)
//    {
//        removeTimeSlotSummary(report);
//        removeStationSummary(report);
//    }

//    static private void removeTimeSlotSummary(TimeSlotOrderReport report)
//    {
////        for (int i=0; i<report.getData().size(); i++)
////        {
////            SOSReportEntry entry = report.getData().get(i);
////            entry.getData().remove(entry.getData().size()-1);
////            //report.getData().get(i).add("-1",entry.getTotalOrdersCount(), entry.getTotalBumpTime() );
////        }
//    }

//    static  private void removeStationSummary(TimeSlotOrderReport report)
//    {
//        if (report.getData().size()<=0) return;
//        report.getData().remove(report.getData().size() -1);
//
//    }

//    /**
//     * add a new col
//     * @param report
//     */
//    static private void addTimeSlotSummary(TimeSlotOrderReport report)
//    {
////        for (int i=0; i<report.getData().size(); i++)
////        {
////            SOSReportEntry entry = report.getData().get(i);
////            report.getData().get(i).add("-1",entry.getTotalOrdersCount(), entry.getTotalBumpTime() );
////        }
//    }
//
//    static  public void addStationSummary(TimeSlotOrderReport report)
//    {
////        if (report.getData().size()<=0) return;
////        SOSReportEntry entry = new SOSReportEntry();
////        entry.setFixedText(KDSApplication.getContext().getString(R.string.total));//"Total");
////        int nStationFrom = KDSUtil.convertStringToInt( report.getCondition().getStationFrom(), 0);
////        int nStationTo = KDSUtil.convertStringToInt( report.getCondition().getStationTo(), 0);
////        int size = report.getData().get(0).getSize();
////
////        //for (int i=nStationFrom; i<=nStationTo; i++)
////        for (int i=0; i< size; i++)
////        {
////            int stationIndex = i;// i - nStationFrom;
////            entry.add("-1", report.getStationTotalCounter(stationIndex), report.getStationTotalBumpTimeSeconds(stationIndex));
////        }
////        report.getData().add(entry);
//    }

//    private static String FOLDER_NAME = "KDSStatisticReport";
//    static public String getStatisticFolder() {
//        return Environment.getExternalStorageDirectory() + "/" + FOLDER_NAME;
//    }
//    static public String getStatisticFolderFullPath() {
//
//        return getStatisticFolder() + "/";
//
//    }
//
//    static public boolean exportToFile(TimeSlotOrderReport report)
//    {
//        String s = report.export2Xml();
//        KDSUtil.createFolder(TimeSlotOrderReport.getStatisticFolder());
//        String filename = report.getReportFileName();
//        filename = TimeSlotOrderReport.getStatisticFolderFullPath() + filename;
//        return KDSUtil.fileWrite(filename, s);
//    }

//    protected void importDateGroup2Xml(KDSXML xml)
//    {
//
//    }
//
//    protected void importTimeGroup2Xml(KDSXML xml)
//    {
//
//    }
//
//    protected void importTimeSlotGroup2Xml(KDSXML xml)
//    {
//
//    }
}
