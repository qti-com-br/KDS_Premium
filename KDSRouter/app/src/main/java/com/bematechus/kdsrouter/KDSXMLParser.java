/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bematechus.kdsrouter;

/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSPOSMessage;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXML;
import com.bematechus.kdslib.KDSXMLParserCommand;
import com.bematechus.kdslib.KDSXMLParserOrder;
import com.bematechus.kdslib.KDSXMLParserPOSMessage;

/**
 *
 * @author David.Wong
 */
public class KDSXMLParser  {

    public static final String TAG = "KDSXMLParser";

     public enum XMLType
    {
        Unknown,
        Order,
        POS_Info,
        Command,
        Feedback_OrderStatus,
        Notification,
        Acknowledgement,//2.0.13
        
    }
    static final private String  NOTIFY_TYPE = ("NotifyType");
    protected static XMLType checkXmlType(String strText)
    {
        if (strText.indexOf(KDSXMLParserOrder.DBXML_ELEMENT_FEEDBACK_ORDER_STATUS) >=0)
            return XMLType.Feedback_OrderStatus;
        KDSXML x = new KDSXML();
        if (!x.loadString(strText))
            return XMLType.Unknown;
       return checkXmlType(x);
    }
    
    protected static XMLType checkXmlType(KDSXML x)
    {
        x.back_to_root();
        String strName = x.getCurrentName();
        if (strName.equals(KDSXMLParserPOSMessage.DBXML_ELEMENT_STATIONINFO))
            return XMLType.POS_Info;
        else if (strName.equals(KDSXMLParserOrder.DBXML_ELEMENT_TRANSACTION)) {
            String s = x.getAttribute(NOTIFY_TYPE, "");
            if (s.isEmpty())
                return XMLType.Order;
            else
                return XMLType.Notification;
        }
        else if (strName.equals(KDSXMLParserCommand.DBXML_ELEMENT_COMMAND))
            return XMLType.Command;
        else if (strName.equals(KDSXMLParserOrder.DBXML_ELEMENT_ORDER_ACK)) //2.0.13
            return XMLType.Acknowledgement;
        else
            return XMLType.Unknown;
    }
    
    public static Object parseXmlFile(String kdsStation, String strFileName)
    {
        String strText = KDSUtil.readUtf8TextFile(strFileName);
        return parseXml(kdsStation, strText);
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
        if (t == XMLType.Order)
            return parseXmlOrder(kdsStation, x);
        else if (t == XMLType.POS_Info)
            return parseXmlPOSInfo(x);
        else if (t == XMLType.Command)
            return parseXmlCommand(x);
        else
            return null;
    }
    protected static KDSPOSMessage parseXmlPOSInfo(KDSXML x)
    {
        return KDSXMLParserPOSMessage.parsePOSMessage(x);
        
    }
    /**
     * 
     * @param kdsStation
     *      My station number
     * @param xml
     * @return 
     */
    public static KDSDataOrder parseXmlOrder(String kdsStation, KDSXML xml)
    {
        try {
            return KDSXMLParserOrder.parseXmlOrder(kdsStation, xml);
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            return null;
        }
    }
    
     protected static KDSXMLParserCommand parseXmlCommand(KDSXML xml)
    {
        return KDSXMLParserCommand.parseCommand(xml);
    }
}
