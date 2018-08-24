package com.bematechus.kdsstatistic;

import com.bematechus.kdslib.SOSReportCondition;
import com.bematechus.kdslib.SOSReportOneStation;
import com.bematechus.kdslib.SOSReportTimeSlotData;
import com.bematechus.kdslib.SOSStationConfig;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2018/3/8.
 */
public class SOSReportCreators {//implements KDSSOS.KDSSOSRemoteEvents {

    static public String TAG = "RPCreator";
    SOSReportCreatorEvents m_receiver = null; //events receiver, all sub-creator will send message to this receiver.
    ArrayList<SOSReportCreator> m_reportCreators = new ArrayList<>();

    public void setEventsReceiver(SOSReportCreatorEvents receiver)
    {
        m_receiver = receiver;
    }

    public  void onReceiveReport(String stationID, SOSReportOneStation reportOneStation)
    {
        for (int i=0; i< m_reportCreators.size(); i++)
        {
            m_reportCreators.get(i).onReceiveReport(stationID, reportOneStation);
        }
    }

    final int MAX_KEEP_COUNT  =30;
    public SOSReportCreator createReport(SOSReportCondition condition)
    {
        SOSReportCreator c = new SOSReportCreator();
        c.setEventsReceiver(m_receiver);

        c.refreshReport(condition);

        m_reportCreators.add(c);

        //remove old data.
        if (m_reportCreators.size() >MAX_KEEP_COUNT)
        {
            int n = m_reportCreators.size() - MAX_KEEP_COUNT;
            for (int i=0; i< n; i++)
                m_reportCreators.remove(0);
        }
        return c;
    }

    public SOSReportCreator findReport(String reportID)
    {
        for (int i=0; i< m_reportCreators.size(); i++)
        {
            if (m_reportCreators.get(i).getReportID().equals(reportID))
                return m_reportCreators.get(i);
        }
        return null;
    }
    /**
     * remove all report data from latestreportguid
     * @param latestReportID
     * @return
     */
    public int removeOldData(String latestReportID)
    {
        SOSReportCreator c = findReport(latestReportID);
        if (c == null)
            return 0;
        return removeOldData(c.getCondition().getDeadline());
    }
    public  int removeOldData(Date dtLatest)
    {
        ArrayList<SOSReportCreator> ar = new ArrayList<>();

        for (int i=0; i< m_reportCreators.size(); i++)
        {
            if (m_reportCreators.get(i).getCondition().getDeadline().getTime() < dtLatest.getTime())
                ar.add(m_reportCreators.get(i));

        }
        for (int i=0; i< ar.size(); i++)
        {
            m_reportCreators.remove(ar.get(i));
        }
        return ar.size();
    }

    private SOSReportCreator findLatestStationCreator(String stationID)
    {

        SOSReportCreator latestCreator = null;
        for (int i=0; i< m_reportCreators.size(); i++)
        {
            if (m_reportCreators.get(i).isExistedStation(stationID))
            {
                if (latestCreator == null)
                    latestCreator = m_reportCreators.get(i);
                else
                {
                    Date dtLatest =latestCreator.getCondition().getCreatedDate();//.getDeadline();
                    Date dtLoop =m_reportCreators.get(i).getCondition().getCreatedDate();//.getDeadline();
                    if ( dtLatest.getTime() < dtLoop.getTime() )
                    {
                        latestCreator = m_reportCreators.get(i);
                    }
                }
            }
        }
        return latestCreator;
    }


    /**
     *
     * @param stationID
     * @param dtStart
     * @return
     */
    public SOSReportOneStation getLatestStationReport(String stationID, Date dtStart)
    {

        SOSReportCreator creator = findLatestStationCreator(stationID);
        if (creator == null) return null;
        //Log.d(TAG,"Latest creator: Date=" + KDSUtil.convertDateToString( creator.getCondition().getCreatedDate()));
        //Log.d(TAG,"Latest creator: ReportID=" +  creator.getCondition().getReportID());

        SOSReportOneStation reportStation = creator.getReport().findStationReport(stationID);
        SOSReportOneStation reportShow = reportStation.clone();
        reportShow.rebuildForStartTimeChanged(creator.getCondition(), dtStart);
        //Log.d(TAG, reportShow.export2Xml());

        return reportShow;


    }
    public SOSReportOneStation getOverallStationReport(ArrayList<SOSStationConfig> arStations, Date dtStart)
    {
        SOSReportOneStation reportOverall = new SOSReportOneStation();

        for (int i=0; i< arStations.size(); i++)
        {
            SOSStationConfig station = arStations.get(i);
            if (!station.getIncludedInOverall()) continue;
            SOSReportOneStation r = getLatestStationReport(station.getStationID(), dtStart);
            if (r != null)
                buildOverallReport(reportOverall, r);

        }
        return reportOverall;

    }
    private boolean buildOverallReport(SOSReportOneStation reportOverall,SOSReportOneStation reportStation)
    {
        reportOverall.getRealTime().setCount(reportOverall.getRealTime().getCount() + reportStation.getRealTime().getCount());
        reportOverall.getRealTime().setBumpTimeSeconds(reportOverall.getRealTime().getBumpTimeSeconds() + reportStation.getRealTime().getBumpTimeSeconds());
        reportOverall.getRealTime().setOverTargetCount(reportOverall.getRealTime().getOverTargetCount() + reportStation.getRealTime().getOverTargetCount());
        reportOverall.getRealTime().setOverTargetSeconds(reportOverall.getRealTime().getOverTargetSeconds() + reportStation.getRealTime().getOverTargetSeconds());

        if (reportOverall.getTimeslots().size() <=0)
            reportOverall.getTimeslots().addAll(reportStation.getTimeslots());
        else
        {
            for (int i=0; i< reportStation.getTimeslots().size(); i++)
            {
                SOSReportTimeSlotData t = reportStation.getTimeslots().get(i);
                if (reportOverall.getTimeslots().size() <=i)
                    reportOverall.getTimeslots().add(t);
                else {
                    SOSReportTimeSlotData ts = reportOverall.getTimeslots().get(i);
                    ts.setCount(ts.getCount() + t.getCount());
                    ts.setBumpTimeSeconds(ts.getBumpTimeSeconds() + t.getBumpTimeSeconds());

                }
            }
        }
        return true;
    }
}
