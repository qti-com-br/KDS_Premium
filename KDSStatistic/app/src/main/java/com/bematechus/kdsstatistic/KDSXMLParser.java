/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bematechus.kdsstatistic;

//

import com.bematechus.kdslib.KDSXML;
import com.bematechus.kdslib.KDSXMLParserCommand;

/**
 *
 * @author David.Wong
 */
public class KDSXMLParser {
    
     public enum XMLType
    {
        Unknown,
        Order,
        POS_Info,
        Command,
        Feedback_OrderStatus,
        Report_Requirement,
    }
    
    protected static XMLType checkXmlType(String strText)
    {

        KDSXML x = new KDSXML();
        if (!x.loadString(strText))
            return XMLType.Unknown;
       return checkXmlType(x);
    }
    
    protected static XMLType checkXmlType(KDSXML x)
    {
        String strName = x.getCurrentName();

        if (strName.equals(KDSXMLParserCommand.DBXML_ELEMENT_COMMAND))
            return XMLType.Command;
        else if (strName.equals(STXMLParserAskReport.DBXML_ELEMENT_REPORT))
            return XMLType.Report_Requirement;
        else
            return XMLType.Unknown;
    }
    

    /**
     * 
     * @param kdsStation
     *      My station number, string type.
     * @param strText
     * @return 
     */
    public static Object parseXml(String kdsStation, String strText)
    {
        KDSXML x = new KDSXML();
        if (!x.loadString(strText))
            return null;
        XMLType t = checkXmlType(x);

        if (t == XMLType.Command)
            return parseXmlCommand(x);
        else
            return null;
    }

     protected static KDSXMLParserCommand parseXmlCommand(KDSXML xml)
    {
        return KDSXMLParserCommand.parseCommand(xml);
    }
}
