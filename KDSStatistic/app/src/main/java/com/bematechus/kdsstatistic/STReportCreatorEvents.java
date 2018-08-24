package com.bematechus.kdsstatistic;

import com.bematechus.kdslib.ConditionStatistic;
import com.bematechus.kdslib.TimeSlotOrderReport;

/**
 * Created by Administrator on 2017/11/8.
 */


public interface STReportCreatorEvents {
    void onReportCreatorStartCreateReport(STReportCreator creator, ConditionStatistic condition);
    void onReportCreatorReceiveStationReport(STReportCreator creator, String stationID,ConditionStatistic condition, TimeSlotOrderReport report );
    void onReportCreatorReportCreated(STReportCreator creator, ConditionStatistic condition);
}
