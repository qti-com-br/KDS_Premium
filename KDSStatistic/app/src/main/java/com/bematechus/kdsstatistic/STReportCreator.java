package com.bematechus.kdsstatistic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.bematechus.kdslib.ConditionStatistic;
import com.bematechus.kdslib.ConditionBase;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.TimeSlotEntry;
import com.bematechus.kdslib.TimeSlotEntryDetail;
import com.bematechus.kdslib.TimeSlotOrderReport;

/**
 * Created by Administrator on 2017/11/7.
 */
public class STReportCreator implements STKDSStatistic.KDSStatisticRemoteEvents{


    HashMap<String, Boolean> m_stationsStatus = new HashMap<>(); //record stations report status.

    STReportCreatorEvents m_receiver = null;

    ConditionStatistic m_conditionOriginal = null;
    TimeSlotOrderReport m_report = null;
    ConditionStatistic.OrderReportContent m_reportContent = ConditionStatistic.OrderReportContent.Counter;

    Object m_tag = null;

    public void setTag(Object obj)
    {
        m_tag = obj;
    }
    public Object getTag()
    {
        return m_tag;
    }
    public boolean isCreating()
    {
        return (m_conditionOriginal != null &&
                m_stationsStatus.size()>0);

    }
    public void reset()
    {
        m_tag = null;
        m_conditionOriginal = null;
        m_report = null;
        resetStations();
    }

    public void setEventsReceiver(STReportCreatorEvents receiver)
    {
        m_receiver = receiver;
    }

    ConditionStatistic getCondition()
    {
        return m_conditionOriginal;
    }
    public void resetStations()
    {
        m_stationsStatus.clear();
    }

    public void addStations(ArrayList<String> arStations)
    {
        for (int i=0; i<arStations.size(); i++)
        {
            m_stationsStatus.put(arStations.get(i), false);
        }
    }

    public void setStationReceivedReport(String stationID)
    {
        m_stationsStatus.put(stationID, true);
    }
    public boolean isAllStationsReceivedReport()
    {

        for (Map.Entry<String, Boolean> entry : m_stationsStatus.entrySet()) {

            if (!entry.getValue())
                return false;
        }
        return true;
    }

    private ConditionBase.ReportMode m_reportMode = ConditionBase.ReportMode.Order;
    public ConditionBase.ReportMode getReportMode()
    {
        return m_reportMode;
    }
    static public void saveConditionProfile(String fileName, ConditionStatistic condition)
    {
        ConditionStatistic.exportProfileToFile(condition, fileName);


    }

    public TimeSlotOrderReport getReport()
    {
        return m_report;
    }

    /**
     *
     * @param fileName
     */
    static public ConditionStatistic loadConditionProfile(String fileName)
    {
        String filePath = ConditionStatistic.getProfileFolderFullPath() + fileName;

        ConditionStatistic condition = new ConditionStatistic();
        condition.importFile(filePath);
        return condition;
    }

    /**
     *
     * @param condition
     * @return
     * How many stations will do report
     */
    public ArrayList<String>   refreshReport(ConditionStatistic condition)
    {

//        if (m_conditionOriginal != null)
//            m_conditionOriginal.copyDateTime(condition);
//        else {
        m_conditionOriginal = condition;
        m_conditionOriginal.adjustMonthWeekFirstDay();
//        }

        resetStations();

        ArrayList<String> ar = new ArrayList<>();
        int nFrom =  KDSUtil.convertStringToInt(m_conditionOriginal.getStationFrom(), 0);
        int nTo =  KDSUtil.convertStringToInt(m_conditionOriginal.getStationTo(), 0);
        for (int i=nFrom; i<= nTo; i++)
            ar.add(KDSUtil.convertIntToString(i));
        STGlobalVariables.getKDS().setRemoteReportEventsReceiver(this);
        ArrayList<String> arStations = STGlobalVariables.getKDS().requestStatisticReport(ar, condition);
        addStations(arStations);

        if (m_receiver != null)
            m_receiver.onReportCreatorStartCreateReport(this,condition);
        return arStations;
//        if (ncount >0)
//            showProgressDialog(KDSApplication.getContext().getString(R.string.waiting_collecting), KDSApplication.getContext().getString(R.string.waiting_for_statistic) , ncount, ar);
//        else {
//            KDSToast t = new KDSToast();
//            t.showMessage(this.getActivity(),this.getActivity().getString(R.string.no_active_stations));
//        }

    }

    public  void onReceiveReport(String stationID, TimeSlotOrderReport report)
    {
        if (!isCreating()) return;
        if (!m_conditionOriginal.isSameCondition(report.getCondition())) {
            if (!m_conditionOriginal.isSameCondition(report.getCondition()))
                return;
            return;
        }
        report.getCondition().setStationFrom(m_conditionOriginal.getStationFrom());
        report.getCondition().setStationTo(m_conditionOriginal.getStationTo());
        setStationReceivedReport(stationID);

        if (getReport() == null)
            m_report = report;
        else
        {
            combineReport(report);
        }

        if (m_receiver != null) {
            m_receiver.onReportCreatorReceiveStationReport(this,stationID, report.getCondition(), report);
            if (isAllStationsReceivedReport())
                m_receiver.onReportCreatorReportCreated(this,report.getCondition());
        }

    }
    /**
     *
     * @param report
     */
    public void combineReport(TimeSlotOrderReport report)
    {

        if (m_report == null ||
                (! m_report.getCondition().isSameCondition(report.getCondition())) ||
                m_report.getData().size() <=0) {
            m_report = report;

            return;
        }
        if (report.getData().size() <=0) return;
        TimeSlotOrderReport.removeTotal(m_report);
        if (report.getCondition().getReportMode() == ConditionBase.ReportMode.Order)
            combineOrderReport(report);
        else
        {
            combineItemReport(report);
        }
        TimeSlotOrderReport.addTotal(m_report);

        m_report.getCondition().setOrderReportContent(m_reportContent);
//        //report.revertRowCol();
//        int ncols = m_report.getNeedCols();
//        this.setCols(ncols);
//        showTitles(m_report);
//        ((MyAdapter)m_listItems.getAdapter()).setReportContnet( m_report.getCondition().getOrderReportContent());
//        ((MyAdapter)m_listItems.getAdapter()).setListData(m_report.getData());
//        ((MyAdapter) m_listItems.getAdapter()).notifyDataSetChanged();
    }
    private void combineOrderReport(TimeSlotOrderReport report)
    {
        int existedStationIndex = -1;

        String dataStationID =  report.getData().get(0).getData().get(0).getStationID();//.getCondition().getStationFrom();
        for (int i=0; i< m_report.getData().get(0).getData().size(); i++)
        {
            if (m_report.getData().get(0).getData().get(i).getStationID().equals(dataStationID))
                existedStationIndex = i;
        }
        if (existedStationIndex ==-1) {
            for (int i = 0; i < m_report.getData().size(); i++) {
                m_report.getData().get(i).getData().add(report.getData().get(i).getData().get(0));
            }
        }
        else
        {
            for (int i = 0; i < m_report.getData().size(); i++) {
                m_report.getData().get(i).getData().set(existedStationIndex, report.getData().get(i).getData().get(0));
            }
        }
    }

    private void combineItemReport(TimeSlotOrderReport report)
    {
        int existedStationIndex = -1;

        String dataStationID =  report.getData().get(0).getData().get(0).getStationID();//.getCondition().getStationFrom();
        for (int i=0; i< m_report.getData().get(0).getData().size(); i++)
        {
            if (m_report.getData().get(0).getData().get(i).getStationID().equals(dataStationID))
                existedStationIndex = i;
        }

        int stationsCount = m_report.getData().get(0).getData().size();
        if (existedStationIndex ==-1) {//this is a new station
            stationsCount ++;
            for (int i = 0; i < report.getData().size(); i++) {
                if ( report.getData().get(i).getData().get(0).getStationID().equals("-1")) continue;
                String dataText = report.getData().get(i).getFixedText();
                int nExistItemIndex = findItemInReport(m_report, dataText);

                appendReportCell(m_report,nExistItemIndex, report.getData().get(i).getData().get(0) ,stationsCount,dataText);

            }
            checkEmptyCellAfterAddNewStation(m_report, stationsCount, dataStationID);
        }
        else
        {
            //
            for (int i = 0; i < report.getData().size(); i++) {
                if ( report.getData().get(i).getData().get(0).getStationID().equals("-1")) continue;
                String dataText = report.getData().get(i).getFixedText();
                if (dataText.isEmpty()) continue;
                int nExistItemIndex = findItemInReport(m_report, dataText);
                //
                addOrReplaceCell(m_report,existedStationIndex,nExistItemIndex, report.getData().get(i).getData().get(0),stationsCount,dataText );
            }
        }
        removeEmptyRow(m_report);
    }

    private int findItemInReport(TimeSlotOrderReport report, String itemDescription)
    {
        for (int i=0; i< report.getData().size(); i++)
        {
            if (report.getData().get(i).getFixedText().equals(itemDescription))
                return i;
        }
        return -1;
    }


    /**
     *
     * @param report
     * @param nRow
     * @param detailCell
     * @param stationCount
     * @param dataText
     */
    private void appendReportCell(TimeSlotOrderReport report, int nRow,TimeSlotEntryDetail detailCell, int stationCount ,String dataText)
    {
        if (nRow >=0)
            report.getData().get(nRow).getData().add(detailCell);
        else {
            ///
            TimeSlotEntry t = new TimeSlotEntry();
            appendRowCell(report, t, stationCount);
            t.getData().set(stationCount-1,detailCell);
            t.setFixedText(dataText);
            report.getData().add(t);
        }
    }

    private void appendRowCell(TimeSlotOrderReport report,TimeSlotEntry entry, int stationCount)
    {
        //for (int i=0; i<report.getData().get(0).getData().size(); i++)
        for (int i=0; i< stationCount; i++)
        {
            TimeSlotEntryDetail t = null;
            if (i>= report.getData().get(0).getData().size())
                entry.add("255", 0, 0);
            else
            {
                t = report.getData().get(0).getData().get(i);

                entry.add(t.getStationID(), 0, 0);
            }
        }

    }

    private void addOrReplaceCell(TimeSlotOrderReport report,int nCol, int nRow,TimeSlotEntryDetail detailCell,int stationCount,String dataText )
    {
        if (nCol < 0) return;
        if (nRow >=0)
        { //this new data is existed in old table.
            report.getData().get(nRow).getData().set(nCol, detailCell);
        }
        else
        {//add new data row
            TimeSlotEntry t = new TimeSlotEntry();
            appendRowCell(report, t, stationCount);
            t.getData().set(nCol, detailCell);
            report.getData().add(t);
        }

    }

    private void checkEmptyCellAfterAddNewStation(TimeSlotOrderReport  report, int nStationsCount, String newStationID)
    {
        for (int i=0; i< report.getData().size(); i++)
        {
            if (report.getData().get(i).getData().size()<nStationsCount)
            {
                report.getData().get(i).getData().add(new TimeSlotEntryDetail(newStationID, 0, 0));
            }
        }
    }

    /**
     * in item report, in order to show null report, I use "" as empty data row.
     * While combine report, we have to remove this null row
     * @param report
     */
    private void removeEmptyRow(TimeSlotOrderReport  report)
    {
        for (int i=report.getData().size()-1; i>=0 ; i--)
        {
            if (report.getData().get(i).getFixedText().isEmpty())
            {
                report.getData().remove(i);
            }
        }
    }

}
