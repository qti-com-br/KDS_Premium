package com.bematechus.kdslib;

import android.os.Environment;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/8/20.
 */
public class TimeSlotOrderReport {

    ArrayList<TimeSlotEntry> m_arData = new ArrayList<>();
    ConditionStatistic m_condition = new ConditionStatistic();


    public int getNeedCols()
    {
        if (this.getData().size()<=0)
            return 0;
        int n = this.getData().get(0).getData().size();
        return n + 1 ;

    }

    public void setCondition(ConditionStatistic condition)
    {
        m_condition = condition;
    }
    public ConditionStatistic getCondition()
    {
        if (m_condition == null)
            m_condition = new ConditionStatistic();
        return m_condition;
    }

    public void add(TimeSlotEntry entry)
    {
        m_arData.add(entry);
    }



    public ArrayList<TimeSlotEntry> getData()
    {
        return m_arData;
    }

    public void next()
    {

    }
    public void prev()
    {

    }
    public String getTitleString()
    {
        return "";
    }


    public String getFixedColString()
    {
        if (getCondition().getReportMode() == ConditionBase.ReportMode.Order)
            return KDSApplication.getContext().getString(R.string.time);// "Time";
        else {
            return KDSApplication.getContext().getString(R.string.item_description);
        }
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
        TimeSlotEntry entry = m_arData.get(m_arData.size()-1); //last cell is the total
        TimeSlotEntryDetail detail = entry.getData().get(entry.getData().size()-1);
        return (int)detail.getCounter();


    }

    /**
     * unit mins
     * @return
     */
    public float getTotalBumpTime()
    {
        if (m_arData.size()<=0) return 0;
        TimeSlotEntry entry = m_arData.get(m_arData.size()-1);//last cell is the total
        TimeSlotEntryDetail detail = entry.getData().get(entry.getData().size()-1);
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

    /**
     * xml format:
     *  <StatisticReport reporttype=1>
     *      <Station from= to=></Station>
     *      <Date from=2016-01-02 to 2017-12-12></Date>
     *      <Time from= to=></Dtime>
     *      <TimeSlot>1</TimeSlot>
     *      <TotalOrderCount></TotalOrderCount>
     *      <TotalPrepTime></TotalPrepTime>
     *      <AverageCountEachTimeSlot></AverageCountEachTimeSlot>
     *      <AveragePrepTime></AveragePrepTime>
     *      <Headers>#1,#3,#4,#2</Headers>
     *      <OrdersCounter>
     *          <TimeSlot from=12:10>2,4,5,7</TimeSlot>
     *          ...
     *      </OrdersCounter>
     *      <PrepTime>
     *          <T12:00>0.2,4.0,5.3,7.0</T12:00>
     *          ...
     *      </PrepTime>
     *
     *
     *  </StatisticReport>
     * @return
     */
    public String export2Xml()
    {
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root("StatisticReport");
        xml.newAttribute("ReportType", getCondition().getReportType().toString());
        xml.newAttribute("ReportMode", getCondition().getReportMode().toString());
        xml.newAttribute("Item", getCondition().getItemDescription());
        //String str =  xml.get_xml_string();

        xml.newGroup("Station", true);
        xml.newAttribute("from", getCondition().getStationFrom());
        xml.newAttribute("to", getCondition().getStationTo());
        xml.back_to_parent();
        //str =  xml.get_xml_string();


        addDateGroup2Xml(xml);
        addTimeGroup2Xml(xml);
        addTimeSlotGroup2Xml(xml);

        xml.newGroup("TotalOrderCount",KDSUtil.convertIntToString(getTotalOrderCount()), false);
        xml.newGroup("TotalPrepTime",KDSUtil.convertFloatToShortString(getTotalBumpTime()), false);
        xml.newGroup("AverageCountEachTimeSlot",KDSUtil.convertFloatToShortString(getAverageOrderCountPerTimeslot()), false);
        xml.newGroup("AveragePrepTime",KDSUtil.convertFloatToShortString(getAverageOrderPrepTime()), false);

        if (getData().size()>0) {
            String headers = "";
            for (int i = 0; i < getData().get(0).getData().size() - 1; i++) {

                String stationID = getData().get(0).getData().get(i).getStationID();
                String s = "Station #" + stationID;
                if (!headers.isEmpty())
                    headers += ",";
                headers += s;
                //v.setText(s);
            }
            headers += ",Total";
            xml.newGroup("Headers",headers, false);
        }
        xml.newGroup("OrdersCounter", true);
        for (int i=0; i< m_arData.size(); i++)
        {
            String s = m_arData.get(i).getOrderCounterXmlText();

            xml.newGroup("TimeSlot", s , true);
            xml.newAttribute("from", m_arData.get(i).getFixedText());
            xml.back_to_parent();
        }
        xml.back_to_parent();

        xml.newGroup("PrepTime", true);
        for (int i=0; i< m_arData.size(); i++)
        {
            String s = m_arData.get(i).getPrepTimeXmlText();
            xml.newGroup("TimeSlot", s , true);
            xml.newAttribute("from", m_arData.get(i).getFixedText());
            xml.back_to_parent();
        }
        xml.back_to_parent();

        return xml.get_xml_string();


    }


    public String export2CSV()
    {
        String csv = "";
        String LF = "\n";

        csv += "Statistic Report" + LF;

        csv +="Report Type :";
        csv += getCondition().getReportType().toString();
        csv += LF;

        csv += "Report Mode:" + getCondition().getReportMode().toString();
        csv += LF;
        csv += "Item: "+ getCondition().getItemDescription();

        csv +=LF;
        csv += "Station from "+ getCondition().getStationFrom();
        csv += " to " + getCondition().getStationTo();
        csv += LF;

        csv = addDateGroup2CSV(csv);
        csv += LF;
        csv = addTimeGroup2CSV(csv);
        csv += LF;
        csv = addTimeSlotGroup2CSV(csv);
        csv += LF;

        csv += "Total Order Count: " + KDSUtil.convertIntToString(getTotalOrderCount());
        csv += LF;
        csv += "Total Prep Time: " + KDSUtil.convertFloatToShortString(getTotalBumpTime());
        csv += LF;
        csv += "Average Count Each Time Slot:" + KDSUtil.convertFloatToShortString(getAverageOrderCountPerTimeslot());
        csv += LF;
        csv += "Average Prep Time:"  + KDSUtil.convertFloatToShortString(getAverageOrderPrepTime());
        csv += LF;


        if (getData().size()>0) {
            String headers = "";
            for (int i = 0; i < getData().get(0).getData().size() - 1; i++) {

                String stationID = getData().get(0).getData().get(i).getStationID();
                String s = "Station #" + stationID;
                if (!headers.isEmpty())
                    headers += ",";
                headers += s;
                //v.setText(s);
            }
            headers += ",Total";
            csv += "Headers:" + headers;
            csv += LF;
        }
        csv += "OrdersCounter";
        csv += LF;
        for (int i=0; i< m_arData.size(); i++)
        {
            String s = m_arData.get(i).getOrderCounterXmlText();

            csv += "TimeSlot from " +m_arData.get(i).getFixedText();
            csv += " Data:";
            csv += s ;
            csv += LF;

        }


        csv += "PrepTime";
        csv += LF;
        for (int i=0; i< m_arData.size(); i++)
        {
            String s = m_arData.get(i).getPrepTimeXmlText();
            csv += "TimeSlot from " +m_arData.get(i).getFixedText();
            csv += " Data:";
            csv += s ;
            csv += LF;

        }

        return csv;


    }


    public String getReportFileName()
    {
        return this.getCondition().getReportType().toString();
    }
    protected void addDateGroup2Xml(KDSXML xml)
    {

    }

    protected void addTimeGroup2Xml(KDSXML xml)
    {

    }

    protected void addTimeSlotGroup2Xml(KDSXML xml)
    {

    }

    protected String addDateGroup2CSV(String csv)
    {
        return csv;
    }

    protected String addTimeGroup2CSV(String csv)
    {
        return csv;
    }

    protected String addTimeSlotGroup2CSV(String csv)
    {
        return csv;
    }

    static public TimeSlotOrderReport importFromXml(String strXml)
    {
        TimeSlotOrderReport report = null;
        KDSXML xml = new KDSXML();
        xml.loadString(strXml);

        xml.back_to_root();//        .new_doc_with_root("StatisticReport");
        String reportType =  xml.getAttribute("ReportType","");// getCondition().getReportType().toString());
        if (reportType.isEmpty()) return null;
        ConditionStatistic.ReportType rt = ConditionStatistic.ReportType.valueOf(reportType);

        switch (rt)
        {
            case Daily:
            {
                report = new ReportOrderDaily();
            }
            break;
            case Weekly:
                report = new ReportOrderWeekly();
                break;
            case Monthly:
                report = new ReportOrderMonthly();
                break;
            case OneTime:
                report = new ReportOrderOneTime();
                break;
        }
        report.getCondition().setReportType(rt);
        //String str =  xml.get_xml_string();
        String s = "";
        s = xml.getAttribute("ReportMode", "Order");
        if (s.equals("Order"))
            report.getCondition().setReportMode(ConditionBase.ReportMode.Order);
        else if (s.equals("Item"))
            report.getCondition().setReportMode(ConditionBase.ReportMode.Item);
        s = xml.getAttribute("Item", "");
        report.getCondition().setItemDescription(s);

        xml.getFirstGroup("Station");

        s = xml.getAttribute("from", "");
        int nStationFrom = KDSUtil.convertStringToInt(s, 0);

        report.getCondition().setStationFrom(s);

        s = xml.getAttribute("to", "");
        int nStationTo = KDSUtil.convertStringToInt(s, 0);

        report.getCondition().setStationTo(s);

        xml.back_to_parent();
        //str =  xml.get_xml_string();

        report.importDateGroup2Xml(xml);
        //addDateGroup2Xml(xml);
        report.importTimeGroup2Xml(xml);
        report.importTimeSlotGroup2Xml(xml);

        xml.getFirstGroup("OrdersCounter");
        if (xml.getFirstGroup("TimeSlot"))
        {
            do {
                s = xml.getCurrentGroupValue();
                TimeSlotEntry entry = new TimeSlotEntry();
                entry.setFixedText(xml.getAttribute("from", ""));
                ArrayList<String> ar =  KDSUtil.spliteString(s, ",");
                for (int i= nStationFrom; i<= nStationTo; i++)
                {
                    entry.add(KDSUtil.convertIntToString(i),KDSUtil.convertStringToInt(ar.get(i - nStationFrom), 0), 0 );
                }
                report.add(entry);
            }while (xml.getNextGroup("TimeSlot"));
        }


        xml.back_to_parent();

        int nIndex = 0;
        xml.getFirstGroup("PrepTime");
        if (xml.getFirstGroup("TimeSlot"))
        {
            do {
                s = xml.getCurrentGroupValue();
                ArrayList<String> ar =  KDSUtil.spliteString(s, ",");
                TimeSlotEntry entry = report.getData().get(nIndex);
                nIndex ++;
                for (int i= nStationFrom; i<= nStationTo; i++)
                {
                    entry.getData().get(i - nStationFrom).setBumpTimeSeconds(KDSUtil.convertStringToFloat(ar.get(i-nStationFrom), 0));


                }

            }while (xml.getNextGroup("TimeSlot"));
        }
        xml.back_to_parent();

        addTotal(report);

        return report;
    }

    static public void addTotal(TimeSlotOrderReport report)
    {
        addStationSummary(report);
        addTimeSlotSummary(report);
    }

    static public void removeTotal(TimeSlotOrderReport report)
    {
        removeTimeSlotSummary(report);
        removeStationSummary(report);
    }

    static private void removeTimeSlotSummary(TimeSlotOrderReport report)
    {
        for (int i=0; i<report.getData().size(); i++)
        {
            TimeSlotEntry entry = report.getData().get(i);
            entry.getData().remove(entry.getData().size()-1);

        }
    }

    static  private void removeStationSummary(TimeSlotOrderReport report)
    {
        if (report.getData().size()<=0) return;
        report.getData().remove(report.getData().size() -1);

    }

    /**
     * add a new col
     * @param report
     */
    static private void addTimeSlotSummary(TimeSlotOrderReport report)
    {
        for (int i=0; i<report.getData().size(); i++)
        {
            TimeSlotEntry entry = report.getData().get(i);
            report.getData().get(i).add("-1",entry.getTotalOrdersCount(), entry.getTotalBumpTime() );
        }
    }

    static  public void addStationSummary(TimeSlotOrderReport report)
    {
        if (report.getData().size()<=0) return;
        TimeSlotEntry entry = new TimeSlotEntry();
        entry.setFixedText(KDSApplication.getContext().getString(R.string.total));//"Total");
        int nStationFrom = KDSUtil.convertStringToInt( report.getCondition().getStationFrom(), 0);
        int nStationTo = KDSUtil.convertStringToInt( report.getCondition().getStationTo(), 0);
        int size = report.getData().get(0).getSize();

        //for (int i=nStationFrom; i<=nStationTo; i++)
        for (int i=0; i< size; i++)
        {
            int stationIndex = i;// i - nStationFrom;
            entry.add("-1", report.getStationTotalCounter(stationIndex), report.getStationTotalBumpTimeSeconds(stationIndex));
        }
        report.getData().add(entry);
    }

    private static String FOLDER_NAME = "KDSStatisticReport";
    static public String getStatisticFolder() {
        return Environment.getExternalStorageDirectory() + "/" + FOLDER_NAME;
    }
    static public String getStatisticFolderFullPath() {

        return getStatisticFolder() + "/";

    }

    static public boolean exportToFile(TimeSlotOrderReport report)
    {
        String s = report.export2Xml();
        KDSUtil.createFolder(TimeSlotOrderReport.getStatisticFolder());
        String filename =createNewFileName(report);//  report.getReportFileName();
        //filename = TimeSlotOrderReport.getStatisticFolderFullPath() + filename;
        return KDSUtil.fileWrite(filename, s);
    }

    static public String createNewFileName(TimeSlotOrderReport report)
    {
        String filename = report.getReportFileName();
        filename = TimeSlotOrderReport.getStatisticFolderFullPath() + filename;
        return filename;
    }
    static public String exportToString(TimeSlotOrderReport report)
    {
        String s = report.export2Xml();
        return s;


    }

    static public boolean exportToCSV(TimeSlotOrderReport report)
    {
        String s = report.export2CSV();
        KDSUtil.createFolder(TimeSlotOrderReport.getStatisticFolder());
        String filename = report.getReportFileName();
        filename = filename.replace(".xml", ".txt");
        filename = TimeSlotOrderReport.getStatisticFolderFullPath() + filename;
        return KDSUtil.fileWrite(filename, s);

    }

    protected void importDateGroup2Xml(KDSXML xml)
    {

    }

    protected void importTimeGroup2Xml(KDSXML xml)
    {

    }

    protected void importTimeSlotGroup2Xml(KDSXML xml)
    {

    }
}
