/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bematechus.kdslib;
import java.util.HashMap;

/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

/**
 *
 * @author David.Wong
 *
 *       * format:
 *  <KDSCommand>
 *      <Code></Code>
 *      <Param Station="1" IP="192.168.1.1" MAC="092341823-48",PSize="2", P0="123", P1="2348"...>
 *         String parameters
 *      </Param>
 *  <KDSCommand>
 */

public class KDSXMLParserCommand {

    public static final String DBXML_ELEMENT_COMMAND = "KDSCommand";

    KDSCommand m_command = KDSCommand.Nothing;
    HashMap m_params = new HashMap();


    public enum KDSCommand
    {
        Nothing,
        // Require_Station_Info,
        Require_Station_Statistic_DB,
        Require_Station_Daily_DB,
        Require_Station_Configuration,
        Broadcast_Station_Configuration,
        Broadcast_Station_Active,//station --> all, tell others station I am actived
        Broadcast_All_Configurations, //6
        Station_Add_New_Order,
        Station_Bump_Order,
        Station_Unbump_Order,
        Station_Cancel_Order,//10
        Station_Modify_Order,
        Station_Bump_Item,
        Station_Unbump_Item, //13
        Station_Modified_Item,
        Station_Transfer_Order, //transfer oder to given station
        Station_Transfer_Order_ACK, //16 given station send ACK code to verify order received. ??? MAYBE, don't need it.

        DBSupport_Sql, //this sql will update/insert data to support database
        DBCurrent_Sql,//the database will changed by this sql.
        DBSync_Broadcast_Current_Db_Updated, //the current has run this sql,
        DBSync_Broadcast_Station_Sql_Sync_Updated, //20 the station has run same sql as current db, and update the syncflag as current db too.
        Broadcast_Ask_Active_Info,
        DB_Ask_Status, //ask support database status, empty or not empty.
        DB_Return_Status, //return support database is empty status
        DB_Copy_Current_To_Support, //while just open/started app, backup can not find if main is running, so it copy all support to current,
        // if main and backup is same database, we just reverse it
        Station_Add_New_Park_Order,
        Station_Order_Parked,
        Station_Order_Unpark,
        ////////////////////
        ////////////////////
        //for Router
        ROUTER_ASK_DB_STATUS,
        ROUTER_FEEDBACK_DB_STATUS,
        ROUTER_DB_SQL,
        ROUTER_ASK_DB_DATA,
        ROUTER_UPDATE_CHANGES_FLAG,//2015-12-31
        ROUTER_SQL_SYNC, //sync router database in primary and backup routers.

        Expo_Bump_Order, //expo and normal station has different steps, those are the expo operations and will inform to its backup/mirror
        Expo_Unbump_Order,
        Expo_Bump_Item,
        Expo_Unbump_Item,
        Station_Update_Order, //by xml command

        Statistic_Ask_DB_Data,
        DBStatistic_Sql,
        Schedule_Item_Ready_Qty_Changed,

        Queue_Ready,
        Queue_Unready,
        Queue_Pickup,

        Sync_Settings_Queue_Expo_Double_Bump, //global Queue expo double bump settings, it will same in whole kds stations.
        Tracker_Bump_Order,
        Station_Cook_Started,

        Statistic_Request_Report,
        Station_Return_Report,

        //sos
        SOS_Request_Report,
        SOS_Return_Report,
    }
    private static final String COMMAND = "KDSCommand";
    private static final String CODE = "Code";
    private static final String PARAM = "Param";

    public void setCode(String strCode)
    {
        int n = KDSUtil.convertStringToInt(strCode, 0);
        KDSCommand k = KDSCommand.values()[n];
        m_command = k;
    }
    public KDSCommand getCode()
    {
        return m_command;
    }

    public void addParam(String name , String val)
    {
        m_params.put(name, val);
    }

    public String getParam()
    {
        return getParam(PARAM, "");



    }
    public String getParam(String name, String strDefault)
    {
        Object obj = m_params.get(name);
        if (obj == null)
            return strDefault;
        return (String)obj;
    }

    static public String createAskSupportDBStatus(String strStationID, String ipAddress, String macAddress)
    {
        return createCommandXmlString(KDSCommand.DB_Ask_Status.ordinal(),
                strStationID, ipAddress, macAddress, "");

    }

    /**
     * * Manager send this to stations
     * ask station send back its configuration
     * @param fromIP
     * @return
     */
    static public String createRequireConfiguration(String fromIP)
    {
        return createCommandXmlString(KDSCommand.Require_Station_Configuration.ordinal(),
                "", fromIP, "", "");

    }

    static public String createBroadConfiguration(String strSettings)
    {
        return createCommandXmlString(KDSCommand.Broadcast_Station_Configuration.ordinal(),
                "", "", "", strSettings);

    }


    /**
     * format:
     *  <KDSCommand>
     *      <Code></Code>
     *      <Param Station="1" IP="192.168.1.1" MAC="092341823-48">
     *             Order xml....
     *      </Param>
     *  <KDSCommand>
     * @param strStationID
     * @param ipAddress
     * @param macAddress
     * @param
     * @return
     */
    static  public String createOrderBumpNotification(String strStationID, String ipAddress, String macAddress, String orderXML)
    {

        return createCommandXmlString(KDSCommand.Station_Bump_Order.ordinal(),
                strStationID, ipAddress, macAddress, orderXML);


    }



    static  public String createCommandXmlString(KDSCommand command, String strStationID, String ipAddress, String macAddress, String orderXML)
    {

        return createCommandXmlString(command.ordinal(),
                strStationID, ipAddress, macAddress, orderXML);


    }

    static  public String createOrderCanceledNotification(String strStationID, String ipAddress, String macAddress, String orderXML)
    {

        return createCommandXmlString(KDSCommand.Station_Cancel_Order.ordinal(),
                strStationID, ipAddress, macAddress, orderXML);


    }

    static  public String createOrderModifiedNotification(String strStationID, String ipAddress, String macAddress, String orderXML)
    {

        return createCommandXmlString(KDSCommand.Station_Modify_Order.ordinal(),
                strStationID, ipAddress, macAddress, orderXML);


    }



    /**
     * format:
     *  <KDSCommand>
     *      <Code></Code>
     *      <Param Station="1" IP="192.168.1.1" MAC="092341823-48">
     *             Order xml....
     *      </Param>
     *  <KDSCommand>
     * @param strStationID
     * @param ipAddress
     * @param macAddress
     * @param
     * @return
     */
    static  public String createNewOrderNotification(String strStationID, String ipAddress, String macAddress, String orderXML)
    {

        return createCommandXmlString(KDSCommand.Station_Add_New_Order.ordinal(),
                strStationID, ipAddress, macAddress, orderXML);


    }



    /**
     *
     * @param strStationID
     * @param ipAddress
     * @param macAddress
     * @param orderName
     *          OrderID,
     * @param itemName
     *          Item ID, it is not item description <ID> tag
     * @return
     */
    static  public String createItemBumpNotification(String strStationID, String ipAddress, String macAddress, String orderName, String itemName)
    {
        java.util.ArrayList ar = new java.util.ArrayList();
        ar.add(orderName);
        ar.add(itemName);
        return createShortCommandXmlString(KDSCommand.Station_Bump_Item.ordinal(), strStationID, ipAddress, macAddress, ar);



    }
    /**
     *
     * @param strStationID
     * @param ipAddress
     * @param macAddress
     * @param orderName
     * @param itemName
     * @return
     */
    static  public String createItemUnbumpNotification(String strStationID, String ipAddress, String macAddress, String orderName, String itemName)
    {
        java.util.ArrayList ar = new java.util.ArrayList();
        ar.add(orderName);
        ar.add(itemName);
        return createShortCommandXmlString(KDSCommand.Station_Unbump_Item.ordinal(), strStationID, ipAddress, macAddress, ar);

    }

    static  public String createItemNotification(KDSCommand command, String strStationID, String ipAddress, String macAddress, String orderName, String itemName)
    {
        java.util.ArrayList ar = new java.util.ArrayList();
        ar.add(orderName);
        ar.add(itemName);
        return createShortCommandXmlString(command.ordinal(), strStationID, ipAddress, macAddress, ar);

    }

    static  public String createOrderNotification(KDSCommand command, String strStationID, String ipAddress, String macAddress, String orderName)
    {

        return createCommandXmlString(command.ordinal(), strStationID, ipAddress, macAddress, orderName);

    }

    static  public String createItemModifiedNotification(String strStationID, String ipAddress, String macAddress, String orderName, String itemXml)
    {
        java.util.ArrayList ar = new java.util.ArrayList();
        ar.add(orderName);
        ar.add(itemXml);
        return createShortCommandXmlString(KDSCommand.Station_Modified_Item.ordinal(), strStationID, ipAddress, macAddress, ar);

    }



    static  public String createScheduleItemReadyQtyChangedNotification(String strStationID, String ipAddress, String macAddress, String orderName, String itemXml)
    {
        java.util.ArrayList ar = new java.util.ArrayList();
        ar.add(orderName);
        ar.add(itemXml);
        return createShortCommandXmlString(KDSCommand.Schedule_Item_Ready_Qty_Changed.ordinal(), strStationID, ipAddress, macAddress, ar);

    }


        /**
     * format:
     *  <KDSCommand>
     *      <Code></Code>
     *      <Param Station="1" IP="192.168.1.1" MAC="092341823-48">
     *             Order xml....
     *      </Param>
     *  <KDSCommand>
     * @param strStationID
     * @param ipAddress
     * @param macAddress
     * @param
     * @return
     */
    static  public String createOrderUnbumpNotification(String strStationID, String ipAddress, String macAddress, String orderXML)
    {

        return createCommandXmlString(KDSCommand.Station_Unbump_Order.ordinal(),
                strStationID, ipAddress, macAddress, orderXML);


    }
    static  public String createCommandXmlString(int nCommand, String strStationID, String ipAddress, String macAddress, String strParam)
    {
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root(COMMAND);
        xml.back_to_root();
        String code = KDSUtil.convertIntToString(nCommand);

        xml.newGroup(CODE, code, false);
        xml.newGroup(PARAM, true);
        xml.newAttribute(KDSConst.KDS_Str_Station, strStationID);// KDSUtil.convertIntToString(nStation));
        xml.newAttribute(KDSConst.KDS_Str_IP, ipAddress);
        xml.newAttribute(KDSConst.KDS_Str_MAC, macAddress);
        xml.setGroupValue(strParam);

        return xml.get_xml_string();
    }

    /**
     * As the param is too show, I just add them to param attribute
     * @param nCommand
     * @param strStationID
     * @param ipAddress
     * @param macAddress
     * @param params
     *      String array, all params in it.
     * @return
     *  XML string
     *       * format:
     *  <KDSCommand>
     *      <Code></Code>
     *      <Param Station="1" IP="192.168.1.1" MAC="092341823-48", P0="123", P1="2348"...>
     *          params
     *      </Param>
     *  <KDSCommand>
     */
    static  public String createCommandXmlString(int nCommand, String strStationID, String ipAddress, String macAddress, java.util.ArrayList attributes, String params)
    {
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root(COMMAND);
        xml.back_to_root();
        String code = KDSUtil.convertIntToString(nCommand);

        xml.newGroup(CODE, code, false);
        xml.newGroup(PARAM, params, true);
        xml.newAttribute(KDSConst.KDS_Str_Station,strStationID);// KDSUtil.convertIntToString(nStation));
        xml.newAttribute(KDSConst.KDS_Str_IP, ipAddress);
        xml.newAttribute(KDSConst.KDS_Str_MAC, macAddress);

        xml.newAttribute(KDSConst.KDS_Str_PSize, KDSUtil.convertIntToString(attributes.size()));
        for (int i=0; i< attributes.size() ; i++)
        {
            String s = "P" + KDSUtil.convertIntToString(i);
            xml.newAttribute(s, (String)attributes.get(i));
        }


        return xml.get_xml_string();
    }
    /**
     * As the param is too show, I just add them to param attribute
     * @param nCommand
     * @param strStationID
     * @param ipAddress
     * @param macAddress
     * @param params
     *      String array, all params in it.
     * @return
     *  XML string
     *       * format:
     *  <KDSCommand>
     *      <Code></Code>
     *      <Param Station="1" IP="192.168.1.1" MAC="092341823-48", P0="123", P1="2348"...>
     *      </Param>
     *  <KDSCommand>
     */
    static  public String createShortCommandXmlString(int nCommand, String strStationID, String ipAddress, String macAddress, java.util.ArrayList params)
    {
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root(COMMAND);
        xml.back_to_root();
        String code = KDSUtil.convertIntToString(nCommand);

        xml.newGroup(CODE, code, false);
        xml.newGroup(PARAM, true);
        xml.newAttribute(KDSConst.KDS_Str_Station, strStationID);//KDSUtil.convertIntToString(nStation));
        xml.newAttribute(KDSConst.KDS_Str_IP, ipAddress);
        xml.newAttribute(KDSConst.KDS_Str_MAC, macAddress);

        xml.newAttribute(KDSConst.KDS_Str_PSize,  KDSUtil.convertIntToString(params.size()));
        for (int i=0; i< params.size() ; i++)
        {
            String s = "P" + KDSUtil.convertIntToString(i);
            xml.newAttribute(s, (String)params.get(i));
        }


        return xml.get_xml_string();
    }

    static public String createOrderTransferNotification(String strStationID, String ipAddress, String macAddress, String orderXML)
    {
        return createCommandXmlString(KDSCommand.Station_Transfer_Order.ordinal(),
                strStationID, ipAddress, macAddress, orderXML);
    }

    static public KDSXMLParserCommand parseCommand(KDSXML xml)
    {
        if (!xml.back_to_root())
            return null;
        KDSXMLParserCommand c = new KDSXMLParserCommand();
        xml.getFirstGroup(CODE);
        String code = xml.getCurrentGroupValue();
        xml.back_to_parent();
        c.setCode(code);
        KDSCommand command = c.getCode();
        if (command == KDSCommand.Nothing)
            return null;
        xml.getFirstGroup(PARAM);
        String s = xml.getAttribute(KDSConst.KDS_Str_Station, "");
        c.addParam(KDSConst.KDS_Str_Station,s );
        s = xml.getAttribute( KDSConst.KDS_Str_IP, "");
        c.addParam(KDSConst.KDS_Str_IP,s );
        s = xml.getAttribute(KDSConst.KDS_Str_MAC, "");
        c.addParam(KDSConst.KDS_Str_MAC,s );

        s = xml.getAttribute( KDSConst.KDS_Str_PSize, "0");
        if (!s.equals("0"))
        {
            c.addParam(KDSConst.KDS_Str_PSize,s );

            int n = KDSUtil.convertStringToInt(s, 0);
            if (n >0)
            {
                for (int i=0; i< n; i++)
                {
                    String str = "P" + KDSUtil.convertIntToString(i);
                    s = xml.getAttribute(str, "");
                    c.addParam(str,s );
                }
            }
        }
        s = xml.getCurrentGroupValue();//.getGroupValue("Settings", "");

        c.addParam(KDSConst.KDS_Str_Param,s );


        return c;
    }



    /**
     *
     * @param strStationID
     * @param ipAddress
     * @param macAddress
     * @param sql
     *     The sql will run in support database
     * @return
     */
    static  public String createSqlSupportDB(String strStationID, String ipAddress, String macAddress, String sql)
    {

        return createCommandXmlString(KDSCommand.DBSupport_Sql.ordinal(),
                strStationID, ipAddress, macAddress, sql);

    }

    static  public String createSqlCurrentDB(String strStationID, String ipAddress, String macAddress, String sql)
    {

        return createCommandXmlString(KDSCommand.DBCurrent_Sql.ordinal(),
                strStationID, ipAddress, macAddress,sql);

    }

    static  public String createSqlStatisticDB(String strStationID, String ipAddress, String macAddress, String sql, long totalSize, long sendSize)
    {

        java.util.ArrayList ar = new java.util.ArrayList();
        ar.add(KDSUtil.convertIntToString(totalSize));
        ar.add(KDSUtil.convertIntToString(sendSize));

        return createCommandXmlString(KDSCommand.DBStatistic_Sql.ordinal(),
                strStationID, ipAddress, macAddress, ar, sql);

    }

    /**
     *      * P0: currentDB orders count
     * p1: supportDB order count
     * P2:currentDB last timestamp
     * P3: support DB last timestamp
     * p4:showing orders names.
     * p5: currentDB guids
     * p6: support DB guids
     * @param strStationID
     * @param ipAddress
     * @param macAddress
     * @param nCurrentDBCount
     * @param nSupportDBCount
     * @param currentDBTimeStamp
     * @param supportDBTimeStamp
     * @param showingOrders
     * @param currentDBGuids
     * @param supportDBGuids
     * @return
     */
    static  public String createDatabaseStatusNotification(String strStationID, String ipAddress, String macAddress, int nCurrentDBCount,
                                                           int nSupportDBCount, String currentDBTimeStamp, String supportDBTimeStamp, String showingOrders,
                                                           String currentDBGuids, String supportDBGuids)
    {
        java.util.ArrayList ar = new java.util.ArrayList();
        ar.add(KDSUtil.convertIntToString(nCurrentDBCount));
        ar.add(KDSUtil.convertIntToString(nSupportDBCount));
        ar.add(currentDBTimeStamp);
        ar.add(supportDBTimeStamp);
        ar.add(showingOrders);
        ar.add(currentDBGuids);
        ar.add(supportDBGuids);


        return createShortCommandXmlString(KDSCommand.DB_Return_Status.ordinal(),
                strStationID, ipAddress, macAddress, ar);

    }


    static public String createReturnStatisticReportCommand(String strStationID, String ipAddress, String macAddress, String reportXml)
    {
        String param =reportXml;// KDSUtil.convertIntToString(nLastTimeStamp);

        return createCommandXmlString(KDSCommand.Station_Return_Report.ordinal(),strStationID, ipAddress, macAddress,param);
    }

    static public String createReturnSOSReportCommand(String strStationID, String ipAddress, String macAddress, String reportXml)
    {
        String param =reportXml;// KDSUtil.convertIntToString(nLastTimeStamp);

        return createCommandXmlString(KDSCommand.SOS_Return_Report.ordinal(),strStationID, ipAddress, macAddress,param);
    }
}
