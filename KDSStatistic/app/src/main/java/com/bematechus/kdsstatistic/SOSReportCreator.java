package com.bematechus.kdsstatistic;

//import com.bematechus.kdslib.ConditionBase;
//import com.bematechus.kdslib.ConditionStatistic;
//import com.bematechus.kdslib.KDSUtil;
//import com.bematechus.kdslib.TimeSlotEntry;
//import com.bematechus.kdslib.TimeSlotEntryDetail;
//import com.bematechus.kdslib.TimeSlotOrderReport;

import com.bematechus.kdslib.SOSReportCondition;
import com.bematechus.kdslib.SOSReportOneStation;
import com.bematechus.kdslib.SOSStationConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Each time, we let kds app create report, sos app will create this class.
 *  1. The report is async mode, we don't know when report return.
 *  2. The existed station can been lost.
 *
 */
public class SOSReportCreator {//implements KDSSOS.KDSSOSRemoteEvents{




    HashMap<String, Boolean> m_stationsStatus = new HashMap<>(); //record stations report status.

    SOSReportCreatorEvents m_receiver = null; //events

    SOSReportCondition m_conditionOriginal = new SOSReportCondition();
    SOSReport m_report =null; //use it to save all reports that waiting data.


    Object m_tag = null;

    public String getReportID()
    {
        return m_conditionOriginal.getReportID();
    }

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
//        return (m_conditionOriginal != null &&
//                m_stationsStatus.size()>0);
        return (m_conditionOriginal != null);
    }
    public void reset()
    {
        m_tag = null;
        m_conditionOriginal = null;
        m_report = null;//.clear();
        resetStations();
    }

    public void setEventsReceiver(SOSReportCreatorEvents receiver)
    {
        m_receiver = receiver;
    }

    SOSReportCondition getCondition()
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

//    private ConditionBase.ReportMode m_reportMode = ConditionBase.ReportMode.Order;
//    public ConditionBase.ReportMode getReportMode()
//    {
//        return m_reportMode;
//    }
    static public void saveConditionProfile(String fileName, SOSReportCondition condition)
    {
        SOSReportCondition.exportProfileToFile(condition, fileName);


    }

    public SOSReport getReport()
    {
        return m_report;
    }

    /**
     *
     * @param fileName
     */
    static public SOSReportCondition loadConditionProfile(String fileName)
    {
        String filePath = SOSReportCondition.getProfileFolderFullPath() + fileName;

        SOSReportCondition condition = new SOSReportCondition();
        condition.importFile(filePath);
        return condition;
    }

    /**
     *
     * @param condition
     * @return
     * How many stations will do report
     */
    public ArrayList<String>   refreshReport(SOSReportCondition condition)
    {

        m_conditionOriginal = condition;

        resetStations();

        ArrayList<SOSStationConfig> ar = new ArrayList<>();
        ar.addAll(condition.getToStations());

        createReport(condition);

        //KDSGlobalVariables.getKDS().setRemoteReportEventsReceiver(this);
        //send out command
        ArrayList<String> arStations = SOSKDSGlobalVariables.getKDS().requestSOSReport(ar, condition);
        addStations(arStations);

        if (m_receiver != null)
            m_receiver.onReportCreatorStartCreateReport(this,ar);
        return arStations;
//        if (ncount >0)
//            showProgressDialog(KDSApplication.getContext().getString(R.string.waiting_collecting), KDSApplication.getContext().getString(R.string.waiting_for_statistic) , ncount, ar);
//        else {
//            KDSToast t = new KDSToast();
//            t.showMessage(this.getActivity(),this.getActivity().getString(R.string.no_active_stations));
//        }

    }

    private SOSReport createReport(SOSReportCondition condition)
    {
        m_report = new SOSReport();
        m_report.setCondition(condition);

        return m_report;
    }

    public  void onReceiveReport(String stationID, SOSReportOneStation reportOneStation)
    {
        if (!isCreating()) return;
        if (!m_conditionOriginal.getReportID().equals(reportOneStation.getReportID())) {

            return;
        }

        setStationReceivedReport(stationID);

       if (m_report == null) return; //this is not possible
        m_report.add(reportOneStation);

        if (m_receiver != null) {
            m_receiver.onReportCreatorReceiveStationReport(this,stationID);
            if (isAllStationsReceivedReport())
                m_receiver.onReportCreatorReportReceivedAll(this);
        }

    }

    public boolean isExistedStation(String stationID)
    {
        return m_report.isExistedStation(stationID);
    }

    static public SOSReportCondition createCondition(SOSSettings settings,ArrayList<SOSStationConfig> arStations)
    {
        String s = settings.getString(SOSSettings.ID.SOS_Stations);
        //ArrayList<SOSStationConfig> stations =  KDSSOSSettings.parseStations(s);

//        int ncount = arStations.size();
//        for (int i=ncount-1; i>=0; i--)
//        {
//            if (stations.get(i).getIncludedInOverall() || stations.get(i).getShowIndividual())
//                continue;
//            stations.remove(i); //remove disabled
//        }

        SOSReportCondition condition = new SOSReportCondition();

        condition.setToStations(arStations);
        int nduration = settings.getInt(SOSSettings.ID.Graph_duration);
        condition.setGraphPrepTimeDuration(nduration);
        //condition.setGraphPrepTimeInterval(settings.getInt(KDSSOSSettings.ID.Graph_interval));
        condition.setGraphPrepTimeInterval( calculateGraphInterval(nduration));
        condition.setRealPrepTimePeriodSeconds(settings.getInt(SOSSettings.ID.Real_time_period));
        //condition.setDeadlineToNow();
        condition.autoCreateDeadline();
        //in order to create one more point for from 0 hour time, change the duration
        condition.setGraphPrepTimeDuration(nduration + condition.getGraphPrepTimeInterval());

        return condition;

    }

    public static final int MAX_DOTS_COUNT = 60;
    static private int calculateGraphInterval(int graphDuration)
    {
        int n =Math.round( graphDuration/MAX_DOTS_COUNT);
        return n;
    }

}
