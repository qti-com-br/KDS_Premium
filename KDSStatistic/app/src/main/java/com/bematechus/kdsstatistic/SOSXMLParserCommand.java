package com.bematechus.kdsstatistic;

import com.bematechus.kdslib.KDSXMLParserCommand;

/**
 * Created by Administrator on 2015/12/29 0029.
 */
public class SOSXMLParserCommand extends KDSXMLParserCommand {


    static public String createRequestSOSReportCommand(String strStationID, String ipAddress, String macAddress, String conditionXml)
    {
        String param =conditionXml;//

        return createCommandXmlString(KDSCommand.SOS_Request_Report.ordinal(),strStationID, ipAddress, macAddress,param);
    }

}
