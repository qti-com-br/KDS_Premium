package com.bematechus.kdsstatistic;

import com.bematechus.kdslib.ConditionStatistic;
import com.bematechus.kdslib.KDSXML;

/**
 * Created by Administrator on 2016/8/29.
 * Format:
 *  <Condition rptype=0 Object=0 StationFrom=2 StationTo=3 TimeSlot=0 DtFrom=2001-03-05 DtTo=2013-02-12 TmFrom=9:00  TmTo=12:00 Arrange=0 WeekEnable=0 WeekDay=0
 *  </Condition>
 */
public class STXMLParserAskReport {

    public static String DBXML_ELEMENT_REPORT = "Condition";

    static public ConditionStatistic parseReportCondition(KDSXML xml)
    {
        ConditionStatistic condition = new ConditionStatistic();
        condition.importFromXml(xml);
        return condition;
    }

    static public ConditionStatistic parseReportCondition(String strXml)
    {
        KDSXML xml = new KDSXML();
        xml.loadString(strXml);
        return parseReportCondition(xml);
    }
}
