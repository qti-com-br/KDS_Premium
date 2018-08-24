package com.bematechus.kdsstatistic;

//import com.bematechus.kdslib.ConditionStatistic;
import com.bematechus.kdslib.KDSXML;
import com.bematechus.kdslib.SOSReportCondition;

/**
 * Created by Administrator on 2016/8/29.
 * Format:

 */
public class SOSXMLParserAskReport {


    public static String DBXML_ELEMENT_SOS_REPORT = "SOSCondition";
    static public SOSReportCondition parseReportCondition(KDSXML xml)
    {
        SOSReportCondition condition = new SOSReportCondition();
        condition.importFromXml(xml);
        return condition;
    }

    static public SOSReportCondition parseReportCondition(String strXml)
    {
        KDSXML xml = new KDSXML();
        xml.loadString(strXml);
        return parseReportCondition(xml);
    }
    /**
     *      <KDSCommand>
     *      <Code></Code>
     *      <Param Station="1" IP="192.168.1.1" MAC="092341823-48", P0="123", P1="2348"...>
     *          params
     *      </Param>
     *  <KDSCommand>
     */
}
