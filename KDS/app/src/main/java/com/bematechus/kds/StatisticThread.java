package com.bematechus.kds;

import android.nfc.Tag;
import android.util.Log;

import com.bematechus.kdslib.ConditionStatistic;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSSocketInterface;
import com.bematechus.kdslib.KDSSocketTCPSideClient;
import com.bematechus.kdslib.KDSSocketTCPSideServer;
import com.bematechus.kdslib.KDSXMLParserCommand;
import com.bematechus.kdslib.SOSReportCondition;
import com.bematechus.kdslib.SOSReportOneStation;
import com.bematechus.kdslib.TimeSlotOrderReport;

/**
 * Created by Administrator on 2017/7/13.
 * History:
 *  2.0.10
 *      Create the sos report at here too.
 */
public class StatisticThread implements Runnable {
    public KDSSocketInterface m_sock;
    public KDSXMLParserCommand m_command;
    public KDS m_kds;
    @Override
    public void run() {
        if (m_command.getCode() == KDSXMLParserCommand.KDSCommand.SOS_Request_Report)
        {
            doSosReport();
        }
        else if (m_command.getCode() == KDSXMLParserCommand.KDSCommand.Statistic_Request_Report)
        {
            doStatisticReport();
        }
//        String param = m_command.getParam();
//        ConditionStatistic c = ConditionStatistic.importXmlString(param);
//        c.setStationFrom(m_kds.getStationID());
//        c.setStationTo(m_kds.getStationID());
//
//        TimeSlotOrderReport report =  m_kds.getStatisticDB().createOrderReport(c);
//        String s = report.export2Xml();
//        s = KDSXMLParserCommand.createReturnStatisticReportCommand(m_kds.getStationID(), m_kds.getLocalIpAddress(), m_kds.getLocalMacAddress(), s);
//        if (m_sock instanceof KDSSocketTCPSideClient)
//        {
//            ((KDSSocketTCPSideClient)m_sock).writeXmlTextCommand(s);
//        }
//        else if (m_sock instanceof KDSSocketTCPSideServer)
//        {
//            ((KDSSocketTCPSideServer)m_sock).writeXmlTextCommand(s);
//        }

    }
    protected void doStatisticReport()
    {
        if (! KDSConst.ENABLE_FEATURE_STATISTIC)
            return;
        String param = m_command.getParam();
        ConditionStatistic c = ConditionStatistic.importXmlString(param);
        c.setStationFrom(m_kds.getStationID());
        c.setStationTo(m_kds.getStationID());

        TimeSlotOrderReport report =  m_kds.getStatisticDB().createOrderReport(c);
        String s = report.export2Xml();
        s = KDSXMLParserCommand.createReturnStatisticReportCommand(m_kds.getStationID(), m_kds.getLocalIpAddress(), m_kds.getLocalMacAddress(), s);
        if (m_sock instanceof KDSSocketTCPSideClient)
        {
            ((KDSSocketTCPSideClient)m_sock).writeXmlTextCommand(s);
        }
        else if (m_sock instanceof KDSSocketTCPSideServer)
        {
            ((KDSSocketTCPSideServer)m_sock).writeXmlTextCommand(s);
        }
    }

    protected void doSosReport()
    {
        if (!KDSConst.ENABLE_FEATURE_STATISTIC) return;
        String param = m_command.getParam();
        SOSReportCondition c = SOSReportCondition.importXmlString(param);


        SOSReportOneStation report =  m_kds.getStatisticDB().createSOSReport(m_kds.getStationID(), c);


        String s = report.export2Xml();

        Log.d("SOSRP", s);


        s = KDSXMLParserCommand.createReturnSOSReportCommand(m_kds.getStationID(), m_kds.getLocalIpAddress(), m_kds.getLocalMacAddress(), s);
        if (m_sock instanceof KDSSocketTCPSideClient)
        {
            ((KDSSocketTCPSideClient)m_sock).writeXmlTextCommand(s);
        }
        else if (m_sock instanceof KDSSocketTCPSideServer)
        {
            ((KDSSocketTCPSideServer)m_sock).writeXmlTextCommand(s);
        }
    }
}
