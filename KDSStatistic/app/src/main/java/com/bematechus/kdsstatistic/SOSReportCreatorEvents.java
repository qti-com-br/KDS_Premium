package com.bematechus.kdsstatistic;

//import com.bematechus.kdslib.ConditionStatistic;
//import com.bematechus.kdslib.TimeSlotOrderReport;

import com.bematechus.kdslib.SOSStationConfig;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/8.
 */


public interface SOSReportCreatorEvents {
    void onReportCreatorStartCreateReport(SOSReportCreator creator, ArrayList<SOSStationConfig> arStations);
    void onReportCreatorReceiveStationReport(SOSReportCreator creator, String stationID);
    void onReportCreatorReportReceivedAll(SOSReportCreator creator);
}
