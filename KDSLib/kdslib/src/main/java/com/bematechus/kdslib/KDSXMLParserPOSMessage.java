/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bematechus.kdslib;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */
/**
 *
 * @author David.Wong
 * 
 * <StationInfo>
	<StationID>1</StationID>
	<!-- 0...200 station number -->
	<!-- "-1" all stations -->
	<User>0</User>
	<!-- under two user mode, this define which user screen show following information -->
	<!-- 0: top user -->
		<!-- Under single user mode, its value should be 0 -->
	<!-- 1: bottom user -->
	
	
	<Info>Please Close station</Info>
	<!-- The information will be shown on given KDS station -->
</StationInfo>
 */
public class KDSXMLParserPOSMessage {
    public final static String DBXML_ELEMENT_STATIONINFO = ("StationInfo");
    
    protected final static String DBXML_ELEMENT_STATIONID	= ("StationID");
    protected final static String DBXML_ELEMENT_INFO	= ("Info");
    protected final static String DBXML_ELEMENT_USER	= ("User");
    
    static public KDSPOSMessage parsePOSMessage(KDSXML xml)
    {
        if (!xml.back_to_root())
            return null;
        KDSPOSMessage c = new KDSPOSMessage();
//        if (!xml.getFirstGroup(DBXML_ELEMENT_ORDER))
//            return null;
        //go through the order xml file
              
        if (!xml.moveToFirstChild())
            return null;
        do
        {
            String name = xml.getCurrentName();
            doStationInfoSubGroup(xml, name, c);
        }
        while (xml.slidingNext());
        
        return c;
    }
      
      protected static void  doStationInfoSubGroup(KDSXML xml, String grpName, KDSPOSMessage msg)
      {
            String strVal = xml.getCurrentGroupValue();
            switch (grpName)
            {
   
                case  DBXML_ELEMENT_STATIONID:
                {
                    int n = KDSUtil.convertStringToInt(strVal, -1);
                    if (n >=0)
                    {
                        msg.setStation(n);
                        msg.setXmlFieldValid(KDSPOSMessage.VALID_POSMSG_XML_FIELD.Station);
                        
                    }
                    
                }
                break;
                case  DBXML_ELEMENT_USER:
                {
                    int n = KDSUtil.convertStringToInt(strVal, -1);
                    if (n >=0)
                    {
                        msg.setScreen(n);
                        msg.setXmlFieldValid(KDSPOSMessage.VALID_POSMSG_XML_FIELD.Screen);
                        
                    }
                }
                break;
                case  DBXML_ELEMENT_INFO:
                {
                    
                    msg.setMessage(strVal);
                    msg.setXmlFieldValid(KDSPOSMessage.VALID_POSMSG_XML_FIELD.Message);
                }
                break;
            }
        
      }
}
