package com.bematechus.kdsstatistic;

import com.bematechus.kdslib.KDSXMLParserCommand;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/29 0029.
 */
public class STXMLParserCommand extends KDSXMLParserCommand {

    static public String createAskDatabaseData(String strStationID, String ipAddress, String macAddress)
    {
        return createCommandXmlString(KDSCommand.ROUTER_ASK_DB_DATA.ordinal(),
                strStationID, ipAddress, macAddress, "");
    }

    static public String createRequestStatisticReportCommand(String strStationID, String ipAddress, String macAddress, String conditionXml)
    {
        String param =conditionXml;//

        return createCommandXmlString(KDSCommand.Statistic_Request_Report.ordinal(),strStationID, ipAddress, macAddress,param);
    }

}
