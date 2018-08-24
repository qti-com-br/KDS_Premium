
package com.bematechus.kdslib;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author David.Wong
 *
 * The order data in memory.
 */
public class KDSDataOrder extends KDSData {

    public enum CookState
    {
        Unknown,
        Started,
        Count,
    }
    public final int SCREEN_A = 0;
    public final int SCREEN_B = 1;

    static public final String ORDER_TYPE_NORMAL = "";
    static public final String ORDER_TYPE_RUSH = "RUSH";
    static public final String ORDER_TYPE_FIRE = "FIRE";
    static public final String ORDER_TYPE_SCHEDULE = "PREP";
    
    public final int VALUE_FALSE = 0;
    public final int VALUE_TRUE = 1;
    
    //order status
    static public final int ORDER_STATUS_UNKNOWN = -1;
    static public final int ORDER_STATUS_UNPAID = 0;
    static public final int ORDER_STATUS_PAID = 1;

    //transaction type
    public static final int TRANSTYPE_UNKNOWN = -1;
    public static final int TRANSTYPE_ADD = 1;
    public static final int TRANSTYPE_DELETE = 2;
    public static final int TRANSTYPE_MODIFY = 3;
    public static final int TRANSTYPE_TRANSFER = 4;
    public static final int TRANSTYPE_ASK_STATUS = 5;
    public static final int TRANSTYPE_UPDATE_ORDER = 6; //20160712, use new data to modify existed order.
    //variables
    protected String m_strOrderName = "";
    protected String m_strWaiterName = "";
    protected Date m_dtStartTime = new Date();
    protected String m_strToTable = "";
    protected String m_strPCKDSNumber = "";// text(16)," we should use int!!! TODO
    protected int m_nScreen = SCREEN_A;
    protected String m_strFromPOS = "";
    protected String m_strOrderType = ORDER_TYPE_NORMAL;
    protected String m_strDestination = "";
    protected String m_strCustomMsg = "";
    protected String m_strQueueMessage = ""; //for queue display mode.
    protected String m_strTrackerID = ""; //for table-tracker
    protected String m_strPagerID = ""; //for table-tracker

    protected int m_nParked = VALUE_FALSE;
    protected int m_nIconIdx = -1;
    protected int m_nOrderEvtFired = VALUE_FALSE;
    protected Date m_dtPreparationStartTime = KDSUtil.createInvalidDate();// new Date(99,9,9, 0,0,0);//UNUSED !!!
    protected int m_nStatus = ORDER_STATUS_PAID;
    protected int m_nSortIdx = -1;
    protected float m_fltOrderDelay = 0; //smart order
    protected  boolean m_bFromPrimaryOfBackup = false;

    protected KDSDataMessages m_messages = new KDSDataMessages();
    protected KDSDataItems m_items = new KDSDataItems();
    protected Object m_objTag = null;


    protected  boolean m_bQueueReady = false;  //for expo double bumping while Queue enabled

    protected boolean m_soundAlert1Fired = false;
    protected boolean m_soundAlert2Fired = false;
    protected boolean m_soundAlert3Fired = false;

    //for TT
    boolean m_bTTReceiveExpoBumpNotification  = false;
    Date m_dtTTReceiveExpoBumpNotification = new Date();
    boolean m_bTTAllItemsBumped = false;
    Date m_dtTTAllItemsBumped = new Date();
    boolean m_bTTFindMyTrackerID = false;

    CookState m_cookState = CookState.Unknown; //for expo station.

    PrepSorts m_prepSorts = new PrepSorts(); //for preparation time mode.
    //In smart mode, if the items all are hidden,don't show timer.
    //the timer started from first item showing.
    int m_nSmartTimerDelayShowing = 0;


    //2.0.34
    Date m_dtQueueStateTime = new Date();

    /**
     * When auto-bump is enable, after the time reaches, it is impossible to “unbump” an order to
     * current screen anymore since it has time > auto bump time. So for this, when auto – bump is enable,
     * the time to “auto - bump” again will be when this order reappear on the screen to set auto bump time,
     * note: you don’t need to reset the timer.
     * E.G.: Order 1 time start from 0, Auto bump time = 10;
     * after 15 min, recall order 1,
     * then “Auto bump” order 1 will be at 25 min,
     * and time for order 1 = 25:00.
     */
    Date m_dtAutoBumpStartCountTime = new Date();
    /***************************************************************************/
    // for modiy, delete ...
    protected int m_nTransactionType = TRANSTYPE_ADD; //see above definition, don't save it to database
    
    public enum VALID_ORDER_XML_FIELD
    {
        Name,
        Waiter_Name,
        To_Table,
        Screen,
        From_POS,
        Order_Type,
        Destination,
        Custom_Message,
        Parked,
        Icon_Index,
        Status,
        Messages,
        Order_Delay,
        Queue_Message,
        TrackerID,
        PagerID,
        Count
    };


    protected boolean[] m_arValidFields;
    
    /***************************************************************************/
    
    public KDSDataOrder()
    {
        //m_nComponentType = ComponentType.Order;
        m_arValidFields = new boolean[VALID_ORDER_XML_FIELD.Count.ordinal()];
       
        resetXmlFieldsValidFlag();
    }
    public KDSDataOrder(String guid)
    {
        super(guid);

        m_arValidFields = new boolean[VALID_ORDER_XML_FIELD.Count.ordinal()];



    }
    public KDSDataOrder(int nStation)
    {

        m_arValidFields = new boolean[VALID_ORDER_XML_FIELD.Count.ordinal()];
       
        resetXmlFieldsValidFlag();
        m_strPCKDSNumber = KDSUtil.convertIntToString(nStation);
        
    }

    public void setCookState(CookState state)
    {
        m_cookState = state;
    }
    public void setCookState(int nState)
    {
        if (nState <0 ||
               nState >= CookState.Count.ordinal())
            m_cookState = CookState.Unknown;
        else
            m_cookState = CookState.values()[nState];

    }
    public CookState getCookState()
    {
        return m_cookState;
    }

    
    public void resetXmlFieldsValidFlag()
    {
         for (int i = 0; i< VALID_ORDER_XML_FIELD.Count.ordinal(); i++ )
        {
            m_arValidFields[i] = false;
        }
    }
    public void setXmlFieldValid(VALID_ORDER_XML_FIELD field)
    {
        m_arValidFields[field.ordinal()] = true;
    }
    public boolean getXmlFieldValid(VALID_ORDER_XML_FIELD field)
    {
        return m_arValidFields[field.ordinal()];
    }
    public KDSDataMessages getOrderMessages()
    {
        return m_messages;
    }
    public void setOrderMessages(KDSDataMessages msg)
    {
        m_messages = msg;
    }
    
    public void setTransType(int nType)
    {
        m_nTransactionType = nType;
    }
    public int getTransType()
    {
        return m_nTransactionType;
    }
    
    public void setItems(KDSDataItems items)
    {
        m_items = items;
    }
    public KDSDataItems getItems()
    {
        return m_items;
    }
    
    public void setOrderName(String name)
    {
        m_strOrderName = name;
    }
    public String getOrderName()
    {
        return m_strOrderName;
    }
    
    public void setWaiterName(String waiterName)
    {
        m_strWaiterName = waiterName;
    }
    public String getWaiterName()
    {
        return m_strWaiterName;
    }
    
    public void setStartTime(Date startTime)
    {
        m_dtStartTime = startTime;
        setAutoBumpStartCountTime(startTime);
    }

    public void setStartTime(long nStartTime)
    {
        m_dtStartTime.setTime(nStartTime);
        setAutoBumpStartCountTime(m_dtStartTime);

    }
    /**
     * the time we receive this order
     * @return
     */
    public Date getStartTime()
    {
        return m_dtStartTime;
    }

    public Date getStartToCookTime()
    {
        long lrec = getStartTime().getTime();
        long ldelay = getOrderDelayValueLongType();

        Date dt = new Date(lrec + ldelay);
        return dt;

    }

    public  boolean isTimeForAddon()
    {
        TimeDog t = new TimeDog(getStartTime());

        return (t.is_timeout(1000));
    }
    public void setToTable(String toTable)
    {
        m_strToTable = toTable;
    }
    public String getToTable()
    {
        return m_strToTable;
    }
     public void setPCKDSNumber(String pckdsNumber)
    {
        m_strPCKDSNumber = pckdsNumber;
    }
    public String getPCKDSNumber()
    {
        return m_strPCKDSNumber;
    }
    
    public void setScreen(int nScreen)
    {
        m_nScreen = nScreen;
    }
    
    public int getScreen()
    {
        return m_nScreen;
    }
    public void setFromPOSNumber(String posNumber)
    {
        m_strFromPOS = posNumber;
    }
    public String getFromPOSNumber()
    {
        return m_strFromPOS;
    }
    
    public void setOrderType(String strType)
    {
        m_strOrderType = strType;
    }
    public String getOrderType()
    {
        return m_strOrderType;
    }
    
     public void setDestination(String strDestination)
    {
        m_strDestination = strDestination;
    }
    public String getDestination()
    {
        return m_strDestination;
    }
    
    public void setCustomMsg(String strCustomMsg)
    {
        m_strCustomMsg = strCustomMsg;
    }
    public String getCustomMsg()
    {
        return m_strCustomMsg;
    }

    public void setQueueMessage(String strCustomMsg)
    {
        m_strQueueMessage = strCustomMsg;
    }
    public String getQueueMessage()
    {
        return m_strQueueMessage;
    }
    public void setTrackerID(String strTrackerID)
    {
        m_strTrackerID = strTrackerID;
    }
    public String getTrackerID()
    {
        return m_strTrackerID;
    }


    public void setPagerID(String strPagerID)
    {
        m_strPagerID = strPagerID;
    }
    public String getPagerID()
    {
        return m_strPagerID;
    }


    public void setParked(boolean bParked)
    {
        if (bParked)
            m_nParked = 1;
        else
            m_nParked = 0;
    }
    public int getParked()
    {
        return m_nParked;
    }

    public void setIconIdx(int nIconIdx)
    {
        m_nIconIdx = nIconIdx;
    }
    public int getIconIdx()
    {
        return m_nIconIdx;
    }
    public void setOrderEvtFired(int nFired)
    {
        m_nOrderEvtFired = nFired;
    }
    public int getOrderEvtFired()
    {
        return m_nOrderEvtFired;
    }
     public void setPreparationStartTime(Date dtPreparationStartTime)
    {
        m_dtPreparationStartTime = dtPreparationStartTime;
    }

//    public void setPreparationStartTime(long dtPreparationStartTime)
//    {
//        if (dtPreparationStartTime >0)
//            m_dtPreparationStartTime.setTime(dtPreparationStartTime);
//    }

    public Date getPreparationStartTime()
    {
        return m_dtPreparationStartTime;
    }
    
    public void setStatus(int nStatus)
    {
        m_nStatus = nStatus;
    }
    public int getStatus()
    {
        return m_nStatus;
    }
    
    public void setSortIdx(int nSortIdx)
    {
        m_nSortIdx = nSortIdx;
    }
    public int getSortIdx()
    {
        return m_nSortIdx;
    }
    public void copyOrderInfoTo(KDSData component)
    {
        KDSDataOrder obj = (KDSDataOrder) component;
        super.copyTo(obj); 
        obj.m_strOrderName=m_strOrderName;
        obj.m_strWaiterName=m_strWaiterName;
        obj.m_dtStartTime=m_dtStartTime;
        obj.m_strToTable=m_strToTable;
        obj.m_strPCKDSNumber=m_strPCKDSNumber;
        obj.m_nScreen=m_nScreen;
        obj.m_strFromPOS=m_strFromPOS;
        obj.m_strOrderType=m_strOrderType;
        obj.m_strDestination=m_strDestination;
        obj.m_strCustomMsg=m_strCustomMsg;

        obj.m_nParked=m_nParked;
        obj.m_nIconIdx=m_nIconIdx;
        obj.m_nOrderEvtFired=m_nOrderEvtFired;
        obj.m_dtPreparationStartTime=m_dtPreparationStartTime;
        obj.m_nStatus=m_nStatus;
        obj.m_nSortIdx=m_nSortIdx;
        obj.m_fltOrderDelay = m_fltOrderDelay;

        obj.m_bFromPrimaryOfBackup = m_bFromPrimaryOfBackup;
        obj.m_bQueueReady = m_bQueueReady;

        obj.m_strQueueMessage = m_strQueueMessage;
        obj.m_strTrackerID = m_strTrackerID;
        obj.m_strPagerID = m_strPagerID;
        obj.m_cookState = m_cookState;

        obj.m_soundAlert1Fired = m_soundAlert1Fired;
        obj.m_soundAlert2Fired = m_soundAlert2Fired;
        obj.m_soundAlert3Fired = m_soundAlert3Fired;

        obj.m_prepSorts = m_prepSorts;
        obj.m_nSmartTimerDelayShowing = m_nSmartTimerDelayShowing;//for smart hidden.

        this.getOrderMessages().copyTo(obj.getOrderMessages());
    }
    /***************************************************************************
     * 
     * @param component 
     */
    public void copyTo(KDSData component)
    {

        copyOrderInfoTo(component);
        KDSDataOrder obj = (KDSDataOrder) component;

        this.getItems().copyTo(obj.getItems());
        
        
    }
    public KDSData clone()
    {
        KDSDataOrder order = new KDSDataOrder();
        copyTo(order);
        return order;
    }
    
    public String sqlAddNew(String tblName)
    {
        if (tblName.isEmpty())
            tblName = "orders";
        String sql = "insert into "
            + tblName    
            + " ("
            + "GUID,Name,Waiter,Start,ToTbl,"
            + "Station,Screen,POS,OrderType,Dest,"
            + "CustMsg,QueueMsg,TrackerID,PagerID,CookState,Parked,IconIdx,EvtFired,PrepStart,"
            + "Status,SortIdx,OrderDelay,fromprimary,bumpedtime,r0)"
            + " values ("
            + "'" + getGUID() + "'"
            + ",'" + fixSqliteSingleQuotationIssue( getOrderName()) + "'"
            + ",'" + fixSqliteSingleQuotationIssue( getWaiterName()) + "'"
            + ",'" + KDSUtil.convertDateToString( getStartTime()) +"'"
            + ",'" + fixSqliteSingleQuotationIssue(  getToTable()) +"'"
      
            + ",'" + fixSqliteSingleQuotationIssue(  getPCKDSNumber() )+ "'"
            + ","+ KDSUtil.convertIntToString(getScreen())
            + ",'" +fixSqliteSingleQuotationIssue(  getFromPOSNumber() )+"'"
            + ",'" + fixSqliteSingleQuotationIssue(  getOrderType()) + "'"
            + ",'" +fixSqliteSingleQuotationIssue(  getDestination()) + "'"
                
            + ",'" +fixSqliteSingleQuotationIssue(  getCustomMsg()) + "'"
            + ",'" +fixSqliteSingleQuotationIssue(  getQueueMessage()) + "'"
            + ",'" +fixSqliteSingleQuotationIssue(  getTrackerID()) + "'"
            + ",'" +fixSqliteSingleQuotationIssue(  getPagerID()) + "'"
            + "," + KDSUtil.convertIntToString(getCookState().ordinal())
            + "," + KDSUtil.convertIntToString(getParked())
            + "," +  KDSUtil.convertIntToString(getIconIdx())
            + "," +  KDSUtil.convertIntToString(getOrderEvtFired())
            + ",'" + KDSUtil.convertDateToString(getPreparationStartTime()) +"'"
            + "," + KDSUtil.convertIntToString(getStatus())
            + "," + KDSUtil.convertIntToString(getSortIdx())
            + "," + KDSUtil.convertFloatToString(getOrderDelay())
            + ",'" + KDSUtil.convertDateToString( getStartTime()) +"'"//2.0.8, for auto bump time
            + ",'" + KDSUtil.convertDateToString( getStartTime()) +"'"//2.0.34, for queue state time
            + ",0 )";
        return sql;
         
        
    }

    public String sqlAddNewForStatistic(String tblName)
    {
        if (tblName.isEmpty())
            tblName = "orders";
        String sql = "insert into "
                + tblName
                + " ("
                + "GUID,Name,Waiter,Start,ToTbl,"
                + "Station,Screen,POS,OrderType,Dest,"
                + "CustMsg,QueueMsg,Parked,IconIdx,EvtFired,PrepStart,"
                + "Status,SortIdx,OrderDelay,fromprimary)"
                + " values ("
                + "'" + getGUID() + "'"
                + ",'" + fixSqliteSingleQuotationIssue( getOrderName()) + "'"
                + ",'" + fixSqliteSingleQuotationIssue( getWaiterName()) + "'"
                + ",'" + KDSUtil.convertDateToString( getStartTime()) +"'"
                + ",'" + fixSqliteSingleQuotationIssue(  getToTable()) +"'"

                + ",'" + fixSqliteSingleQuotationIssue(  getPCKDSNumber() )+ "'"
                + ","+ KDSUtil.convertIntToString(getScreen())
                + ",'" +fixSqliteSingleQuotationIssue(  getFromPOSNumber() )+"'"
                + ",'" + fixSqliteSingleQuotationIssue(  getOrderType()) + "'"
                + ",'" +fixSqliteSingleQuotationIssue(  getDestination()) + "'"

                + ",'" +fixSqliteSingleQuotationIssue(  getCustomMsg()) + "'"
                + ",'" +fixSqliteSingleQuotationIssue(  getQueueMessage()) + "'"

                + "," + KDSUtil.convertIntToString(getParked())
                + "," +  KDSUtil.convertIntToString(getIconIdx())
                + "," +  KDSUtil.convertIntToString(getOrderEvtFired())
                + ",'" + KDSUtil.convertDateToString(getPreparationStartTime()) +"'"
                + "," + KDSUtil.convertIntToString(getStatus())
                + "," + KDSUtil.convertIntToString(getSortIdx())
                + "," + KDSUtil.convertFloatToString(getOrderDelay())
                + ",0 )";
        return sql;


    }

//    public String sqlUpdate()
//    {
//        String sql = "update orders set "
//
//                + " Name='" +fixSqliteSingleQuotationIssue(  getOrderName()) + "',"
//                + "Waiter='" + fixSqliteSingleQuotationIssue( getWaiterName()) +"',"
//                + "Start='"+ KDSUtil.convertDateToString(getStartTime()) + "',"
//                + "ToTbl='" + fixSqliteSingleQuotationIssue( getToTable()) +"',"
//                + "Station='" + fixSqliteSingleQuotationIssue(  getPCKDSNumber()) + "',"
//                + "Screen="+  KDSUtil.convertIntToString(getScreen()) +","
//                + "POS='" + fixSqliteSingleQuotationIssue( getFromPOSNumber()) +"',"
//                + "OrderType=" + fixSqliteSingleQuotationIssue(  getOrderType() )+"',"
//                + "Dest='"+fixSqliteSingleQuotationIssue(getDestination()) + "',"
//                + "CustMsg='"+ fixSqliteSingleQuotationIssue( getCustomMsg()) + "',"
//                + "QueueMsg='"+ fixSqliteSingleQuotationIssue( getQueueMessage()) + "',"
//                + "TrackerID='"+ fixSqliteSingleQuotationIssue( getTrackerID()) + "',"
//                + "PagerID='"+ fixSqliteSingleQuotationIssue( getPagerID()) + "',"
//                + "CookState="+ KDSUtil.convertIntToString( getCookState().ordinal()) + ","
//                + "Parked=" + KDSUtil.convertIntToString(getParked()) +","
//                + "IconIdx="+  KDSUtil.convertIntToString(getIconIdx()) + ","
//                + "EvtFired=" +  KDSUtil.convertIntToString(getOrderEvtFired()) + ","
//                + "PrepStart='"+ KDSUtil.convertDateToString(getPreparationStartTime()) +"',"
//                + "Status="+ KDSUtil.convertIntToString(getStatus()) +","
//                + "SortIdx="  + KDSUtil.convertIntToString(getSortIdx())  +","
//                + "OrderDelay="  + KDSUtil.convertFloatToString(getOrderDelay())  +","
//                + "DBTimeStamp=" + KDSUtil.convertDateToString(getTimeStamp())
//                //+ " where id=" + Common.KDSUtil.ConvertIntToString(getID());
//                + " where guid='" + getGUID() + "'";
//
//        return sql;
//
//
//    }


    /**
     * modify order, use the "name" as the order id
     * @return
     */
    public String sqlModify()
    {
        String sql = "update orders set ";

        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Name))
                sql += " Name='" +fixSqliteSingleQuotationIssue(  getOrderName()) + "',";
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Waiter_Name))
            sql +=  "Waiter='" + fixSqliteSingleQuotationIssue( getWaiterName()) +"',";
        //if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.))
        //    sql +=  "Start='"+ KDSUtil.convertDateToString(getStartTime()) + "',";
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.To_Table))
            sql +=  "ToTbl='" + fixSqliteSingleQuotationIssue( getToTable()) +"',";
        //if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Waiter_Name))
        //    sql +=  "Station='" + fixSqliteSingleQuotationIssue(  getPCKDSNumber()) + "',"
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Screen))
            sql +=  "Screen="+  KDSUtil.convertIntToString(getScreen()) +",";
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.From_POS))
            sql +=  "POS='" + fixSqliteSingleQuotationIssue(getFromPOSNumber()) +"',";
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Order_Type))
            sql +=  "OrderType='" + fixSqliteSingleQuotationIssue(  getOrderType() )+"',";
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Destination))
            sql +=  "Dest='"+fixSqliteSingleQuotationIssue(  getDestination()) + "',";
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Custom_Message))
            sql +=  "CustMsg='"+ fixSqliteSingleQuotationIssue( getCustomMsg()) + "',";
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Queue_Message))
            sql +=  "QueueMsg='"+ fixSqliteSingleQuotationIssue( getQueueMessage()) + "',";
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.TrackerID))
            sql +=  "TrackerID='"+ fixSqliteSingleQuotationIssue( getTrackerID()) + "',";
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.PagerID))
            sql +=  "PagerID='"+ fixSqliteSingleQuotationIssue( getPagerID()) + "',";
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Parked))
            sql +=  "Parked=" + KDSUtil.convertIntToString(getParked()) +",";
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Icon_Index))
            sql +=  "IconIdx="+  KDSUtil.convertIntToString(getIconIdx()) + ",";
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Order_Delay))
            sql +=  "OrderDelay="+  KDSUtil.convertFloatToString(getOrderDelay()) + ",";

        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Status))
            sql +=  "Status="+ KDSUtil.convertIntToString(getStatus()) +",";

        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Waiter_Name))
            sql +=  "DBTimeStamp=" + KDSUtil.convertDateToString(getTimeStamp());
        //add the station number to it, just for last ",".
        sql +=  " Station='" + fixSqliteSingleQuotationIssue(  getPCKDSNumber()) + "' ";
        sql +=  " where name='" + getOrderName() + "'";
        return sql;


    }

//    /**
//     * use the guid to control it
//     * @return
//     */
//    public String sqlDelete(String tblName)
//    {
//
//        if (tblName.isEmpty())
//            tblName = "orders";
//        String sql = "delete from " + tblName+" where guid='" + getGUID() + "'";
//
//        return sql;
//    }

    public static String sqlDelete(String tblName, String strGUID)
    {
        if (tblName.isEmpty())
            tblName = "orders";
        String sql = "delete from " + tblName + " where guid='" + strGUID + "'";
        return sql;
    }

//    /**
//     * just set
//     *  Bumped, BumpedTime.
//     *  don't delete from database
//     * @param strGUID
//     * @return
//     */
//    public static String sqlBumped(String tblName, String strGUID, boolean bBumped)
//    {
//        if (tblName.isEmpty())
//            tblName = "orders";
//        Date dt = new Date();
//        String strdt = KDSUtil.convertDateToString(dt);
//        String sql = "update orders set bumped=1, BumpedTime='"+ strdt +"' where where guid='" + strGUID + "'";
//        if (!bBumped)
//            sql = "update orders set bumped=0, BumpedTime='"+ strdt +"' where where guid='" + strGUID + "'";
//        return sql;
//    }
//
  
    
    ///////////////////////////////////////////////////////////////////////

    /**
     *
     * @param orderName
     * @param nItemsCount
     * @return
     *  3 + nItemsCount * 6;
     */
    public static KDSDataOrder createTestOrder(String orderName, int nItemsCount, String toStations)
    {
        KDSDataOrder c = new KDSDataOrder();
        c.setCustomMsg("Customer message");
        c.setQueueMessage("Queue message");
        c.setDestination("Fast food");
        c.setFromPOSNumber("5");
        c.setOrderName(orderName);
        c.setOrderType("RUSH");
        c.setPCKDSNumber("1");
        c.setPreparationStartTime(new Date());
        c.setScreen(0);
        c.setWaiterName("David Wong");
        c.setSortIdx(-1);
        c.setStartTime(new Date());
        c.setStatus(0);
        c.setToTable("Tbl #4");
        c.setTrackerID("2");
        c.setPagerID("12");
        c.setIconIdx(1);

        KDSDataMessages msg = new KDSDataMessages();
        for (int n=0; n<1; n++)
        {
            KDSDataMessage m = new KDSDataMessage();
            m.setComponentGUID(c.getGUID());
            m.setForComponentType(KDSDataMessage.FOR_Order);

            m.setMessage("Order Message " + KDSUtil.convertIntToString(n));
            msg.addComponent(m);
        }
        c.setOrderMessages(msg);
        
        for (int i=0; i< nItemsCount; i++)
        {
            KDSDataItem item = new KDSDataItem(c.getGUID());
            item.setAddOnGroup(-1);
//            if (i ==2)
//                item.setHidden(true); //test
            //item.setBG(Color.white);
            //item.setFG(Color.BLACK);
            item.setCategory("Category #2");
            //item.setDescription("item #" + KDSUtil.convertIntToString(i));
            item.setDescription("item #" + KDSUtil.convertIntToString(i));
            item.setItemName("itemname" + KDSUtil.convertIntToString(i));
            item.setToStationsString(toStations);
            item.setOrderID(-1);
            item.setQty(2);
            //item.setToStationsString("");
            KDSDataMessages msgs = new KDSDataMessages();
            for (int n=0; n<1; n++)
            {
                KDSDataMessage m = new KDSDataMessage();
                m.setComponentGUID(item.getGUID());
                m.setForComponentType(KDSDataMessage.FOR_Item);

                m.setMessage("Item #"+ KDSUtil.convertIntToString(i) +" Message " + KDSUtil.convertIntToString(n));
                msgs.addComponent(m);
            }
            item.setMessages(msgs);

            //condiments
            for (int j=0; j<2; j++)
            {
                KDSDataCondiment d = new KDSDataCondiment(item.getGUID());
                d.setCondimentName("condiment $" + KDSUtil.convertIntToString(j));
                d.setDescription(item.getDescription() + " condiment #" + KDSUtil.convertIntToString(j));
                item.getCondiments().addComponent(d);
            }
            c.getItems().addComponent(item);
            
        }
        return c;
    }

    /**
     *
     * This is of testing !!!!
     *
     * @param orderName
     * @param nItemsCount
     * @return
     *  3 + nItemsCount * 6;
     */
    public static KDSDataOrder createTestScheduleOrder(String orderName, int nItemsCount, String toStations)
    {
        KDSDataOrder c = new KDSDataOrder();
        c.setCustomMsg("Customer message");
        c.setQueueMessage("Queue message");
        c.setDestination("Fast food");
        c.setFromPOSNumber("5");
        c.setOrderName(orderName);
        c.setOrderType(ORDER_TYPE_SCHEDULE);
        c.setPCKDSNumber("1");
        c.setPreparationStartTime(new Date());
        c.setScreen(0);
        c.setWaiterName("David Wong");
        c.setSortIdx(-1);
        c.setStartTime(new Date());
        c.setStatus(0);
        c.setToTable("Tbl #4");
//        KDSDataMessages msg = new KDSDataMessages();
//        for (int n=0; n<1; n++)
//        {
//            KDSDataMessage m = new KDSDataMessage();
//            m.setComponentGUID(c.getGUID());
//            m.setForComponentType(KDSDataMessage.FOR_Order);
//
//            m.setMessage("Order Message " + KDSUtil.convertIntToString(n));
//            msg.addComponent(m);
//        }
//        c.setOrderMessages(msg);

        for (int i=0; i< nItemsCount; i++)
        {
            KDSDataItem item = new KDSDataItem(c.getGUID());
            item.setAddOnGroup(-1);
            //item.setBG(Color.white);
            //item.setFG(Color.BLACK);
            item.setCategory("Category #2");
            //item.setDescription("item #" + KDSUtil.convertIntToString(i));
            item.setDescription("item #" + KDSUtil.convertIntToString(i));
            item.setItemName("item-" + KDSUtil.convertIntToString(i));
            item.setToStationsString(toStations);
            item.setOrderID(-1);
            item.setQty(5);
            //item.setToStationsString("");
//            KDSDataMessages msgs = new KDSDataMessages();
//            for (int n=0; n<1; n++)
//            {
//                KDSDataMessage m = new KDSDataMessage();
//                m.setComponentGUID(item.getGUID());
//                m.setForComponentType(KDSDataMessage.FOR_Item);
//
//                m.setMessage("Item #"+ KDSUtil.convertIntToString(i) +" Message " + KDSUtil.convertIntToString(n));
//                msgs.addComponent(m);
//            }
//            item.setMessages(msgs);

            //condiments
//            for (int j=0; j<2; j++)
//            {
//                KDSDataCondiment d = new KDSDataCondiment(item.getGUID());
//                d.setCondimentName("condiment $" + KDSUtil.convertIntToString(j));
//                d.setDescription(item.getDescription() + " condiment #" + KDSUtil.convertIntToString(j));
//                item.getCondiments().addComponent(d);
//            }
            c.getItems().addComponent(item);

        }
        return c;
    }


    /**
     * This is for testing !!!!
     * @param orderName
     * @param nItemsCount
     * @param toStations
     * @return
     */
    public static KDSDataOrder createAddonTestOrder(String orderName, int nItemsCount, String toStations)
    {
        KDSDataOrder c = new KDSDataOrder();
        c.setCustomMsg("Customer message");
        c.setQueueMessage("Queue message");
        c.setDestination("Fast food");
        c.setFromPOSNumber("5");
        c.setOrderName(orderName);
        c.setOrderType("");
        c.setPCKDSNumber("1");
        c.setPreparationStartTime(new Date());
        c.setScreen(0);
        c.setWaiterName("David Wong");
        c.setSortIdx(-1);
        c.setStartTime(new Date());
        c.setStatus(0);
        c.setToTable("Tbl #4");
        KDSDataMessages msg = new KDSDataMessages();
        for (int n=0; n<3; n++)
        {
            KDSDataMessage m = new KDSDataMessage();
            m.setComponentGUID(c.getGUID());
            m.setForComponentType(KDSDataMessage.FOR_Order);

            m.setMessage("Order Message " + KDSUtil.convertIntToString(n));
            msg.addComponent(m);
        }
        c.setOrderMessages(msg);

        for (int i=100; i< 100+nItemsCount; i++)
        {
            KDSDataItem item = new KDSDataItem(c.getGUID());
            item.setAddOnGroup(-1);
            //item.setBG(Color.white);
            //item.setFG(Color.BLACK);
            item.setCategory("Category #2");
            //item.setDescription("item #" + KDSUtil.convertIntToString(i));
            item.setDescription("item #" + KDSUtil.convertIntToString(i));
            item.setItemName("itemname" + KDSUtil.convertIntToString(i));
            item.setToStationsString(toStations);
            item.setOrderID(-1);
            item.setQty(2);
            //item.setToStationsString("");
            KDSDataMessages msgs = new KDSDataMessages();
            for (int n=0; n<1; n++)
            {
                KDSDataMessage m = new KDSDataMessage();
                m.setComponentGUID(item.getGUID());
                m.setForComponentType(KDSDataMessage.FOR_Item);

                m.setMessage("Item Message " + KDSUtil.convertIntToString(n));
                msgs.addComponent(m);
            }
            item.setMessages(msgs);

            //condiments
            for (int j=0; j<1; j++)
            {
                KDSDataCondiment d = new KDSDataCondiment(item.getGUID());
                d.setCondimentName("condiment $" + KDSUtil.convertIntToString(j));
                d.setDescription(item.getDescription() + " condiment #" + KDSUtil.convertIntToString(j));
                item.getCondiments().addComponent(d);
            }
            c.getItems().addComponent(item);

        }
        return c;
    }

    ///////////////////////////////////////////////////////////////////////

      /**
     *
       * this is for testing!!!
     * @param orderName
     * @param nItemsCount
     * @return
     *  3 + nItemsCount * 6;
     */
    public static KDSDataOrder createTestSmartOrder(String orderName, int nItemsCount, String toStations)
    {
        KDSDataOrder c = new KDSDataOrder();
        c.setCustomMsg("Customer message");
        c.setQueueMessage("Queue message");
        c.setDestination("Fast food");
        c.setFromPOSNumber("5");
        c.setOrderName(orderName);
        c.setOrderType("RUSH");
        c.setPCKDSNumber("1");
        c.setPreparationStartTime(new Date());
        c.setScreen(0);
        c.setWaiterName("David Wong");
        c.setSortIdx(-1);
        c.setStartTime(new Date());
        c.setStatus(0);
        c.setToTable("Tbl #4");
        c.setOrderDelay((float)0.1);//6 sec

        KDSDataMessages msg = new KDSDataMessages();
        for (int n=0; n<3; n++)
        {
            KDSDataMessage m = new KDSDataMessage();
            m.setComponentGUID(c.getGUID());
            m.setForComponentType(KDSDataMessage.FOR_Order);

            m.setMessage("Order Message " + KDSUtil.convertIntToString(n));
            msg.addComponent(m);
        }
        c.setOrderMessages(msg);

        for (int i=0; i< nItemsCount; i++)
        {
            KDSDataItem item = new KDSDataItem(c.getGUID());
            item.setAddOnGroup(-1);
            //item.setBG(Color.white);
            //item.setFG(Color.BLACK);
            item.setCategory("Category #2");
            //item.setDescription("item #" + KDSUtil.convertIntToString(i));
            item.setDescription("item #" + KDSUtil.convertIntToString(i));
            item.setItemName("itemname" + KDSUtil.convertIntToString(i));
            item.setToStationsString(toStations);
            item.setOrderID(-1);
            item.setQty(2);
            item.setItemDelay((float) 0.1*i);
            item.setPreparationTime((float)0.1*(i+1));
            //item.setToStationsString("");
            KDSDataMessages msgs = new KDSDataMessages();
            for (int n=0; n<3; n++)
            {
                KDSDataMessage m = new KDSDataMessage();
                m.setComponentGUID(item.getGUID());
                m.setForComponentType(KDSDataMessage.FOR_Item);

                m.setMessage("Item Message " + KDSUtil.convertIntToString(n));
                msgs.addComponent(m);
            }
            item.setMessages(msgs);

            //condiments
            for (int j=0; j<2; j++)
            {
                KDSDataCondiment d = new KDSDataCondiment(item.getGUID());
                d.setCondimentName("condiment $" + KDSUtil.convertIntToString(j));
                d.setDescription(item.getDescription() + " condiment #" + KDSUtil.convertIntToString(j));
                item.getCondiments().addComponent(d);
            }
            c.getItems().addComponent(item);

        }
        return c;
    }

    /**
     *
     * this is for testing!!!
     * @param orderName
     * @param nItemsCount
     * @return
     *  3 + nItemsCount * 6;
     */
    public static KDSDataOrder createTestPrepOrder(String orderName, int nItemsCount, String toStations)
    {
        KDSDataOrder c = new KDSDataOrder();
        c.setCustomMsg("Customer message");
        c.setQueueMessage("Queue message");
        c.setDestination("Fast food");
        c.setFromPOSNumber("5");
        c.setOrderName(orderName);
        c.setOrderType("RUSH");
        c.setPCKDSNumber("1");
        c.setPreparationStartTime(new Date());
        c.setScreen(0);
        c.setWaiterName("David Wong");
        c.setSortIdx(-1);
        c.setStartTime(new Date());
        c.setStatus(0);
        c.setToTable("Tbl #4");

        float base = 0.1f;

        c.setOrderDelay((float)base);//6 sec

        KDSDataMessages msg = new KDSDataMessages();
        for (int n=0; n<3; n++)
        {
            KDSDataMessage m = new KDSDataMessage();
            m.setComponentGUID(c.getGUID());
            m.setForComponentType(KDSDataMessage.FOR_Order);

            m.setMessage("Order Message " + KDSUtil.convertIntToString(n));
            msg.addComponent(m);
        }
        c.setOrderMessages(msg);

        for (int i=0; i< 2; i++)
        {
            KDSDataItem item = new KDSDataItem(c.getGUID());
            item.setAddOnGroup(-1);
            //item.setBG(Color.white);
            //item.setFG(Color.BLACK);
            item.setCategory("Category #2");
            //item.setDescription("item #" + KDSUtil.convertIntToString(i));
            item.setDescription("item #" + KDSUtil.convertIntToString(i));
            item.setItemName("itemname" + KDSUtil.convertIntToString(i));
            item.setToStationsString(toStations);
            item.setOrderID(-1);
            item.setQty(2);
            item.setItemDelay((float) base*i);
            item.setPreparationTime((float)base*(i+1));
            item.setDescription("item # " + KDSUtil.convertIntToString(i) + "->"+KDSUtil.convertIntToString( (long)(c.getOrderDelay()*60))+","+KDSUtil.convertIntToString( (long)(item.getItemDelay()*60)) +","+KDSUtil.convertIntToString( (long)(item.getPreparationTime()*60)));
            //item.setToStationsString("");
            KDSDataMessages msgs = new KDSDataMessages();
            for (int n=0; n<3; n++)
            {
                KDSDataMessage m = new KDSDataMessage();
                m.setComponentGUID(item.getGUID());
                m.setForComponentType(KDSDataMessage.FOR_Item);

                m.setMessage("Item Message " + KDSUtil.convertIntToString(n));
                msgs.addComponent(m);
            }
            item.setMessages(msgs);

            //condiments
            for (int j=0; j<2; j++)
            {
                KDSDataCondiment d = new KDSDataCondiment(item.getGUID());
                d.setCondimentName("condiment $" + KDSUtil.convertIntToString(j));
                d.setDescription(item.getDescription() + " condiment #" + KDSUtil.convertIntToString(j));
                item.getCondiments().addComponent(d);
            }
            c.getItems().addComponent(item);

        }
        return c;
    }


    /**
     * * this is for testing!!!
     * @param orderName
     * @return
     */
    public static String createAskOrderStatusXml(String orderName)
    {
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root("Transaction");
        xml.newGroup("Order", true);
        xml.newGroup("ID", orderName, false);
        xml.newGroup("TransType", "5", false);
        return xml.get_xml_string();


    }


    /**
     *
     * * this is for testing!!!
     * @param orderName
     * @param nItemsCount
     * @return
     *  3 + nItemsCount * 6;
     */
    public static KDSDataOrder createTestOrderConsolidate(String orderName, int nItemsCount, String toStations)
    {
        KDSDataOrder c = new KDSDataOrder();
        c.setCustomMsg("Customer message");
        c.setQueueMessage("Queue message");
        c.setDestination("Fast food");
        c.setFromPOSNumber("5");
        c.setOrderName(orderName);
        c.setOrderType("");
        c.setPCKDSNumber("1");
        c.setPreparationStartTime(new Date());
        c.setScreen(0);
        c.setWaiterName("David Wong");
        c.setSortIdx(-1);
        c.setStartTime(new Date());
        c.setStatus(0);
        c.setToTable("Tbl #4");
        KDSDataMessages msg = new KDSDataMessages();
        for (int n=0; n<3; n++)
        {
            KDSDataMessage m = new KDSDataMessage();
            m.setComponentGUID(c.getGUID());
            m.setForComponentType(KDSDataMessage.FOR_Order);

            m.setMessage("Order Message " + KDSUtil.convertIntToString(n));
            msg.addComponent(m);
        }
        c.setOrderMessages(msg);

        for (int i=0; i< nItemsCount; i++)
        {
            KDSDataItem item = new KDSDataItem(c.getGUID());
            item.setAddOnGroup(-1);
            //item.setBG(Color.white);
            //item.setFG(Color.BLACK);
            item.setCategory("Category #2");
            //item.setDescription("item #" + KDSUtil.convertIntToString(i));
            item.setDescription("item #" + KDSUtil.convertIntToString(0));
            item.setItemName("itemname" + KDSUtil.convertIntToString(i));
            item.setToStationsString(toStations);
            item.setOrderID(-1);
            item.setQty(2);
            //item.setToStationsString("");
            KDSDataMessages msgs = new KDSDataMessages();
            for (int n=0; n<3; n++)
            {
                KDSDataMessage m = new KDSDataMessage();
                m.setComponentGUID(item.getGUID());
                m.setForComponentType(KDSDataMessage.FOR_Item);

                m.setMessage("Item Message " + KDSUtil.convertIntToString(n));
                msgs.addComponent(m);
            }
            item.setMessages(msgs);

            //condiments
            for (int j=0; j<2; j++)
            {
                KDSDataCondiment d = new KDSDataCondiment(item.getGUID());
                d.setCondimentName("condiment $" + KDSUtil.convertIntToString(j));
                d.setDescription(item.getDescription() + " condiment #" + KDSUtil.convertIntToString(j));
                item.getCondiments().addComponent(d);
            }
            c.getItems().addComponent(item);

        }
        return c;
    }

    /**
     * 
     * @param orderReceived
     * @return 
     * true: order modified.
     * false: nothing to change
     */
    public boolean modify(KDSDataOrder orderReceived)
    {
        boolean bResult = false;
        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.Custom_Message))
        {
            this.setCustomMsg(orderReceived.getCustomMsg());
            bResult =true;
        }

        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.Queue_Message))
        {
            this.setQueueMessage(orderReceived.getQueueMessage());
            bResult =true;
        }

        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.TrackerID))
        {
            this.setTrackerID(orderReceived.getTrackerID());
            bResult =true;
        }

        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.PagerID))
        {
            this.setPagerID(orderReceived.getPagerID());
            bResult =true;
        }


        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.Destination))
        {
            this.setDestination(orderReceived.getDestination());
            bResult =true;
        }
        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.From_POS))
        {
            this.setFromPOSNumber(orderReceived.getFromPOSNumber());
            bResult =true;
        }
        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.Icon_Index))
        {
            this.setIconIdx(orderReceived.getIconIdx());
            bResult =true;
        }

        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.Messages))
        {
            orderReceived.getOrderMessages().copyTo(this.getOrderMessages());
            bResult =true;
        }

        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.Order_Type))
        {
            this.setOrderType(orderReceived.getOrderType());
            bResult =true;
        }

        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.Parked))
        {
            this.setParked( (orderReceived.getParked()==1) );
            bResult =true;
        }

        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.Screen))
        {
            this.setScreen(orderReceived.getScreen());
            bResult =true;
        }
        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.Waiter_Name))
        {
            this.setWaiterName(orderReceived.getWaiterName());
            bResult =true;
        }
        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.Status))
        {
            this.setStatus(orderReceived.getStatus());
            bResult =true;
        }
        if (orderReceived.getXmlFieldValid(VALID_ORDER_XML_FIELD.To_Table))
        {
            this.setToTable(orderReceived.getToTable());
            bResult =true;
        }
        return bResult;
    }

    /**
     * 
     * create the xml text for this order
     * 
     * @return 
     */
    public String createXml()
    {
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root( KDSConst.KDS_Str_Transaction);
        outputOrderInformationToXML(xml, true);
        this.getItems().outputXml(xml);
        return xml.get_xml_string();
    }

//    public boolean outputToXml(KDSXML xml)
//    {
//        xml.new_doc_with_root( KDSConst.KDS_Str_Transaction);
//        return outputDataToXml(xml);
//
//    }

    public boolean outputDataToXml(KDSXML xml)
    {

        outputOrderInformationToXML(xml, true);
        this.getItems().outputXml(xml);
        return true;
    }
    /************************************************************************/
    /* bHasTransType:
            In bump notification, we don't need the transtype tag.
                                                                         */
    /************************************************************************/
    public boolean outputOrderInformationToXML(KDSXML pxml, boolean bHasTransType)
    {
            if (pxml == null) return false;

            pxml.back_to_root();
            pxml.getFirstGroup(KDSConst.KDS_Str_Transaction);

            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ORDER, true);
            //pxml->xmj_getFrstGroup(_T("Order"));
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ID,this.getOrderName(), false);
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_GUID,this.getGUID(), false);
            String s;
            
            s =this.getFromPOSNumber();
            
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TERMINAL,s, false);
            if (bHasTransType)
            {
                String transType = KDSUtil.convertIntToString( this.getTransType());
                if (this.getItems().getCount()==0)
                {
                    transType = KDSConst.KDS_Transaction_Type_Modify;
                }

                pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TRANSTYPE, transType , false); //add
                //20180301 comments it, we need the original transtype value.
//                if (this.getItems().getCount()>0)
//                {
//                        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TRANSTYPE,KDSConst.KDS_Transaction_Type_New, false); //add
//                }
//                else
//                {
//                       pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TRANSTYPE,KDSConst.KDS_Transaction_Type_Modify, false); //modify
//                }

            }
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ORDERSTATUS, KDSUtil.convertIntToString(this.getStatus() ), false);

            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ORDERTYPE, this.getOrderType() , false);
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_OPERATOR, this.getWaiterName(), false);
            
            
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_DESTINATION,this.getDestination(), false);
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TABLE,this.getToTable(), false);
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TOSCREEN,KDSUtil.convertIntToString(this.getScreen()), false);

           // pxml.newGroup("UserInfo",this.getCustomMsg(), false);
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_USERINFO,this.getCustomMsg(), false);
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_QUEUEMSG,this.getQueueMessage(), false);

            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TRACKERID,this.getTrackerID(), false);
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_PAGERID,this.getPagerID(), false);
            //2.5.4.19 add received time and restore time
            s = KDSUtil.convertDateToString(this.getStartTime());
            pxml.newGroup("Received_Time",s, false);

            Date dtNow = new Date();
            
            s = KDSUtil.convertDateToString(dtNow);
            pxml.newGroup("Bumpoff_Time",s, false);
            //////
            outputKDSMessages(pxml, this.getOrderMessages(), "OrderMessages");

            return true;
    }
    
    static public boolean outputKDSMessages(KDSXML pxml, KDSDataMessages messages, String grpName)
    {
         if (messages.getCount()>0)
        {
            pxml.newGroup(grpName, true);
            String s = KDSUtil.convertIntToString( messages.getCount());

            pxml.newGroup("Count", s, false);
            for (int i=0; i<messages.getCount(); i++)
            {
                    s = "S" + KDSUtil.convertIntToString(i);
                    KDSDataMessage msg =(KDSDataMessage) messages.get(i);
                    pxml.newGroup(s, msg.getMessage(), false);

            }
            pxml.back_to_parent();
        }
         return true;
    }
    
    public String makeDurationString()
    {
        Date begin =getStartTime();
        return makeDurationString(begin);

//        Date end = new Date();
//
//        long between=(end.getTime()-begin.getTime())/1000;//除以1000是为了转换成秒
//        //long days=between/(24*3600);
//        long hours= between/3600;// between%(24*3600)/3600;
//        long minutes=(between%3600)/60;
//        long seconds=(between%60);///60;
//        String s = "";
//        if (hours >0)
//        {
//            s += KDSUtil.convertIntToString(hours);
//            s += ":";
//        }
//
//        s += String.format("%02d", minutes);//Common.KDSUtil.ConvertIntToString(minutes);
//        s += ":";
//        s += String.format("%02d", seconds);//Common.KDSUtil.ConvertIntToString(seconds);
//        return s;
            
    }

    static public String makeDurationString(Date dtStart)
    {
        Date begin = dtStart;// getStartTime();
        Date end = new Date();

        long between=(end.getTime()-begin.getTime())/1000;//除以1000是为了转换成秒
        //long days=between/(24*3600);
        long hours= between/3600;// between%(24*3600)/3600;
        long minutes=(between%3600)/60;
        long seconds=(between%60);///60;
        String s = "";
        if (hours >0)
        {
            s += KDSUtil.convertIntToString(hours);
            s += ":";
        }

        s += String.format("%02d", minutes);//Common.KDSUtil.ConvertIntToString(minutes);
        s += ":";
        s += String.format("%02d", seconds);//Common.KDSUtil.ConvertIntToString(seconds);
        return s;

    }

    public int getDurationSeconds()
    {
        Date begin = getStartTime();
        Date end = new Date();

        long between=(end.getTime()-begin.getTime())/1000;//除以1000是为了转换成秒
        int n = (int)between;
        return n;
    }
    
//    public float getWaitingDurationMins()
//    {
//         Date begin =getStartTime();
//        Date end = new Date();
//
//        long between=(end.getTime()-begin.getTime())/1000;// convert to seconds
//
//        float flt =  ((float)between)/((float)60);
//        return flt;
//
//    }
//
    public boolean isPaid()
    {
        int n = this.getStatus();
        if (n == ORDER_STATUS_PAID )
            return true;
        return false;
    }
    
//    public boolean isAllReady()
//    {
//        KDSDataItems items =  this.getItems();
//        int ncount = items.getCount();
//        for (int i=0; i< ncount; i++)
//        {
//            if (items.getItem(i).getReady() == KDSConst.INT_FALSE )
//                return false;
//        }
//        return true;
//    }
    
    public boolean isRush()
    {
        String s = this.getOrderType();
        s = s.toUpperCase();
        return (s.equals(ORDER_TYPE_RUSH));
            
    }

    public boolean isFire()
    {
        String s = this.getOrderType();
        s = s.toUpperCase();
        return (s.equals(ORDER_TYPE_FIRE));

    }

    public boolean isScheduleOrder()
    {
        String s = this.getOrderType();
        s = s.toUpperCase();
        return (s.equals(ORDER_TYPE_SCHEDULE));
    }



    /**
     * Each item will create one new order
     * @param order
     * @return
     */
    public static ArrayList<ScheduleProcessOrder> splitForSchedule(KDSDataOrder order)
    {
        ArrayList<ScheduleProcessOrder> ar = new ArrayList<>();

        for (int i=0; i< order.getItems().getCount(); i++)
        {
            KDSDataItem item =  order.getItems().getItem(i);
            ScheduleProcessOrder schOrder = new ScheduleProcessOrder();
            schOrder.create_from_order_item(order, item);

            ar.add(schOrder);
        }
        return ar;
    }
//    public boolean isParked()
//    {
//        if (this.getParked() == 0)
//            return false;
//        return true;
//    }
    
//    public boolean expSyncOrderBump(String fromStation, KDSDataOrder orderBumped)
//    {
//        KDSDataItems items = orderBumped.getItems();
//        int ncount = items.getCount();
//        for (int i=0; i< ncount; i++)
//        {
//            KDSDataItem item = items.getItem(i);
//            KDSDataItem itemLocal = this.getItems().getItemByName(item.getItemName());
//            if (itemLocal == null) continue;
//            itemLocal.setReady(KDSConst.INT_TRUE);
//
//        }
//        return true;
//    }
    
//    public boolean expSyncOrderUnbump(String fromStation, KDSDataOrder orderUnbumped)
//    {
//         KDSDataItems items = orderUnbumped.getItems();
//        int ncount = items.getCount();
//        for (int i=0; i< ncount; i++)
//        {
//            KDSDataItem item = items.getItem(i);
//            KDSDataItem itemLocal = this.getItems().getItemByName(item.getItemName());
//            if (itemLocal == null) continue;
//            if (item.isMarked())
//                itemLocal.setReady(KDSConst.INT_TRUE);
//            else
//                itemLocal.setReady(KDSConst.INT_FALSE);
//
//
//        }
//        return true;
//    }

    /**
     * add received order to this order, and make it as addon items
     * just add unique name items.
     * @param order
     */
    public void appendAddon(KDSDataOrder order)
    {
        int ngroup = this.getItems().createNewAddonGroup();
        int ncount = order.getItems().getCount();

        ArrayList<KDSDataItem> arExisted = new ArrayList<>();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItemByName( order.getItems().getItem(i).getItemName());
            if (item == null) {
                this.getItems().addItemToAddonGroup(ngroup, order.getItems().getItem(i));
            }
            else
            {
                arExisted.add( order.getItems().getItem(i));
            }

        }
        //remove same items from original order
        ncount = arExisted.size();
        for (int i=0; i< ncount; i++)
        {
            order.getItems().removeComponent(arExisted.get(i));
        }
        arExisted.clear();


    }

//    public boolean appendAddon(int nGroupID, KDSDataItem item)
//    {
//        int ngroup = nGroupID;
//
//       // ArrayList<KDSDataItem> arExisted = new ArrayList<>();
//        KDSDataItem itemExisted = this.getItems().getItemByName( item.getItemName());
//        if (itemExisted == null) {
//            this.getItems().addItemToAddonGroup(ngroup,item);
//            return true;
//        }
//        return false;
//
//    }

    /**
     * don't check if there are same item existed.
     * Just add this new item.
     * @param nGroupID
     * @param item
     * @return
     */
    public boolean appendAddonNoCheckingSame(int nGroupID, KDSDataItem item)
    {
        int ngroup = nGroupID;

         this.getItems().addItemToAddonGroup(ngroup,item);
         return true;

    }


    /**
     * * just add unique name items.
     * @param order
     */
    public void appendItems(KDSDataOrder order)
    {
        //int ngroup = this.getItems().createNewAddonGroup();
        int ncount = order.getItems().getCount();
        ArrayList<KDSDataItem> arExisted = new ArrayList<>();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItemByName( order.getItems().getItem(i).getItemName());
            //if (this.getItems().getItemByName( order.getItems().getItem(i).getItemName()) == null) {
            if (item == null) {
                this.getItems().addComponent(order.getItems().getItem(i));
                order.getItems().getItem(i).setOrderGUID(this.getGUID());
            }
            else
            {
                arExisted.add(order.getItems().getItem(i));
            }

        }
        //remove same items from original order
        ncount = arExisted.size();
        for (int i=0; i< ncount; i++)
        {
            order.getItems().removeComponent(arExisted.get(i));
        }
        arExisted.clear();

    }

//    public boolean appendItem(KDSDataItem itemReceived)
//    {
//
//        KDSDataItem item = this.getItems().getItemByName( itemReceived.getItemName());
//        //if (this.getItems().getItemByName( order.getItems().getItem(i).getItemName()) == null) {
//        if (item == null) {
//            this.getItems().addComponent(itemReceived);
//            itemReceived.setOrderGUID(this.getGUID());
//            return true;
//        }
//        return false;
//
//    }

    public boolean appendItemNoCheckingSame(KDSDataItem itemReceived)
    {
        this.getItems().addComponent(itemReceived);
        itemReceived.setOrderGUID(this.getGUID());
        return true;

    }

    public boolean isAddonOrder()
    {
        for (int i=0; i< getItems().getCount(); i++)
        {
            KDSDataItem item = getItems().getItem(i);

            if (item.isAddonItem())
                return true;

        }
        return false;

    }

    public boolean isVoidOrder()
    {

        for (int i=0; i< getItems().getCount(); i++)
        {
            KDSDataItem item = getItems().getItem(i);

            if (item.getQty() == 0 &&item.getChangedQty() != 0)
                return true;

        }
        return false;

    }

    public void setOrderDelay(float fltDelay)
    {
        m_fltOrderDelay = fltDelay;
    }

    public float getOrderDelay()
    {
        return m_fltOrderDelay;
    }

    public long getOrderDelayValueLongType()
    {
        return (long)(m_fltOrderDelay * 60 *1000);
    }

    /**
     *  Use the received time to calculate the starting cook time.
     *
     * @return
     */
    public Date smartOrderGetStartToCookTime()
    {
        float fltOrderDelay = this.getOrderDelay(); //unit: minutes
        float fltMsDelay = fltOrderDelay * 60 * 1000;
        long msDelay = (long)fltMsDelay;
        Date dt = new Date(this.getStartTime().getTime());
        dt.setTime(dt.getTime() + msDelay);
        return dt;

    }

    public boolean smartOrderIsTimeToStartCook()
    {
        Date dtNow = new Date(System.currentTimeMillis());
        Date dtOrderStartCook = this.smartOrderGetStartToCookTime();
        return ( dtNow.getTime() >dtOrderStartCook.getTime()  );

    }

    /**
     * The item preparation time has been add the modifiers time when save to database
     * @param item
     * @return
     */
    public boolean prepItemIsTimeToCook(KDSDataItem item)
    {
        //float fltItemDelay = item.getItemDelay();
        float fltOrderDelay = this.getOrderDelay();

        return m_prepSorts.is_cooking_time(item.getItemName(), this.getStartTime(), fltOrderDelay);

    }

    /**
     *
     * @param item
     * @return
     *  How many seconds later, this item will start to cook.
     */
    public int prepItemGetStartTime(KDSDataItem item)
    {
        float fltOrderDelay = this.getOrderDelay();

        return m_prepSorts.item_start_cooking_time_seconds(item.getItemName(), this.getStartTime(), fltOrderDelay);

    }

//    /**
//     *
//     * @return
//     */
//    public boolean smartItemIsTimeToCook(KDSDataItem item, KDSDataItem maxPrepTimeItem)
//    {
//        float fltItemDelay = item.getItemDelay();
//        float fltOrderDelay = this.getOrderDelay();
//        float fltItemPreparation = item.getTotalPrepTime();
//        Date dtRecevied = this.getStartTime();
//        return smartItemIsTimeToCook(fltItemDelay, fltOrderDelay, fltItemPreparation, dtRecevied, maxPrepTimeItem);
//
//
//    }

//    static public boolean smartItemIsTimeToCook(float fltItemDelay, float fltOrderDelay,float fltItemPreparation,Date dtOrderRecevied, KDSDataItem maxPrepTimeItem )
//    {
//
//
//        float flt = fltOrderDelay + fltItemDelay + fltItemPreparation;
//        flt = flt * 60 * 1000;
//        long nTimeout = (long)flt;
//        if (maxPrepTimeItem != null)
//        {
//            float fltMaxItemDelay = maxPrepTimeItem.getItemDelay()+ fltOrderDelay +maxPrepTimeItem.getTotalPrepTime();
//            long nMaxItemDelay =(long) (fltMaxItemDelay * 60 * 1000);
//            nTimeout = nMaxItemDelay - nTimeout;
//        }
//        Date dtNow = new Date(System.currentTimeMillis());
//        Date dtRecevied = dtOrderRecevied;
//        return ( (dtNow.getTime() - dtRecevied.getTime()) > nTimeout  );
//    }

    /**
     * this is for exp station.
     * @return
     */
    public boolean isItemsAllBumpedInExp()
    {

        int ncount = this.getItems().getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if (item.isExpitem()) continue;
            //nNormalItemsCount ++;
            if (item.getBumpedStationsString().isEmpty())
                return false;
        }
        return true;

    }

    /**
     * 2.0.14,
     *  Add a new option in order display, move finish order to font of queue;
     *  in normal station it will be when all items are marked as ready, and for expo will be all items ready( default is disable).
     * @return
     */
    public boolean isAllItemsFinished()
    {
        int ncount = this.getItems().getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if (item.getLocalBumped() ||
                (!item.getBumpedStationsString().isEmpty()) )
                continue;

            return false;
        }
        return true;
    }

    public void setTag(Object obj)
    {
        m_objTag = obj;
    }
    public Object getTag()
    {
        return m_objTag;
    }

    public void setFromPrimaryOfBackup(boolean bPrimary)
    {
        m_bFromPrimaryOfBackup = bPrimary;
    }
    public boolean getFromPrimaryOfBackup()
    {
        return m_bFromPrimaryOfBackup;
    }


//    public void setAllItemsVoidByXml(boolean bVoid)
//    {
//        int ncount = this.getItems().getCount();
//        for (int i=0; i< ncount; i++)
//        {
//            this.getItems().getItem(i).setDeleteByRemoteCommand(bVoid);
//        }
//
//    }

    public boolean is_schedule_process_order()
    {
        return false;
    }

    /************************************************************************/
/*
return:
get the total qty of all found items
*/
    /************************************************************************/
    public int find_no_condiments_not_ready_item(String strCategory, String strName, ArrayList<String> arFoundItemGuids)
    {

        //ArrayList<String> arItemGuid = new ArrayList<>();

        float nTotalQty = 0;
        for (int i=0; i< getItems().getCount(); i++)
        {
            KDSDataItem pitem = getItems().getDataItem(i);
            if (pitem.getLocalBumped()) continue;
            if (pitem.getCondiments().getCount() >0)
                continue;
            if (pitem.getCategory().equals( strCategory) &&
                    pitem.getDescription().equals(strName) )
            {
                arFoundItemGuids.add(pitem.getGUID());

                nTotalQty += pitem.getQty();
            }

        }
        return (int)nTotalQty;
    }
/************************************************************************/
/*
*/
    /************************************************************************/
//    int find_no_condiments_ready_item_qty(String strCategory, String strName, ArrayList<String> parFoundItemGUID)
//    {
//        float nTotalQty = 0;
//        for (int i=0; i< getItems().getCount(); i++)
//        {
//            KDSDataItem pitem = getItems().getDataItem(i);
//            if (!pitem.getLocalBumped()) continue;
//            //if (pitem->GetCondimentsPointer()->GetSize() >0)
//            //	continue;
//            if (pitem.getCategory().equals(strCategory )&&
//                    pitem.getDescription().equals( strName))
//            {
//                parFoundItemGUID.add(pitem.getGUID());
////                if (parFoundItemSerialID != NULL)
////                    parFoundItemSerialID->Add(pitem->GetSerialIDString());
////                if (parFoundItemID != NULL)
////                    parFoundItemID->Add(pitem->GetID());
//                nTotalQty += pitem.getQty();
//            }
//
//        }
//        return (int)nTotalQty;
//    }

    /**
     * for expo double bumping while Queue enabled
     * @param bReady
     */
    public void setQueueReady(boolean bReady)
    {
        m_bQueueReady = bReady;
        setQueueStateTime(new Date()); //2.0.34
    }
    public boolean getQueueReady()
    {
        return m_bQueueReady;
    }

    public void setDimColor(boolean bDim)
    {
        super.setDimColor(bDim);
        int ncount = m_items.getCount();
        for (int i=0; i< ncount; i++)
        {
            m_items.getItem(i).setDimColor(bDim);
        }
        ncount = m_messages.getCount();
        for (int i=0; i< ncount ; i++)
        {
            m_messages.get(i).setDimColor(bDim);
        }
    }

    public void setAlert1SoundFired(boolean bFired)
    {
        m_soundAlert1Fired = bFired;
    }
    public void setAlert2SoundFired(boolean bFired)
    {
        m_soundAlert2Fired = bFired;
    }
    public void setAlert3SoundFired(boolean bFired)
    {
        m_soundAlert3Fired = bFired;
    }

    public boolean getAlert1SoundFired()
    {
        return m_soundAlert1Fired ;
    }
    public boolean getAlert2SoundFired()
    {
        return m_soundAlert2Fired ;
    }
    public boolean getAlert3SoundFired()
    {
        return m_soundAlert3Fired ;
    }


//    public void setAllItemsToScreen(String stationID, int nScreen)
//    {
//        KDSToStation toStation = new KDSToStation();
//        toStation.setPrimaryStation(stationID);
//        toStation.setPrimaryScreen(nScreen);
//        String strToStation = toStation.getString();
//
//        int ncount = getItems().getCount();
//        for (int i=0; i< ncount; i++)
//        {
//
//            getItems().getItem(i).setToStationsString(strToStation);
//        }
//
//    }

    /**
     * Please make sure cal this function while items is loaded
     * Don't inlcude give item
     * @param itemGuid
     * @return
     */
    public int getItemPrevCount(String itemGuid)
    {
        int ncounter = 0;
        int nindex = m_items.getItemIndexByGUID(itemGuid);
        nindex--;
        for (int i=0;i <= nindex; i++)
        {
            if (m_items.getItem(i).isMarked() ||
                    m_items.getItem(i).isReady() ||
                    m_items.getItem(i).getLocalBumped())
                continue;
            ncounter ++;
        }
        return ncounter;
    }

    /**
     * don't include give item
     * @param itemGuid
     * @return
     */
    public int getItemNextCount(String itemGuid)
    {
        int ncounter = 0;
        int nindex = m_items.getItemIndexByGUID(itemGuid);
        nindex ++;
        for (int i=nindex; i< m_items.getCount(); i++)
        {
            if (m_items.getItem(i).isMarked() ||
                    m_items.getItem(i).isReady() ||
                    m_items.getItem(i).getLocalBumped())
                continue;
            ncounter ++;
        }
        return ncounter;

    }

    public KDSDataItem getNextActiveItem(int fromIndex)
    {
        for (int i=fromIndex; i< m_items.getCount(); i++)
        {
            if (m_items.getItem(i).isMarked() ||
                    m_items.getItem(i).isReady() ||
                    m_items.getItem(i).getLocalBumped())
                continue;
            return m_items.getItem(i);
        }
        return null;
    }

    public int getNextActiveItemsCount(int fromIndex)
    {
        int ncounter = 0;
        for (int i=fromIndex; i< m_items.getCount(); i++)
        {
            if (m_items.getItem(i) == null) continue;
            if (m_items.getItem(i).isMarked() ||
                    m_items.getItem(i).isReady() ||
                    m_items.getItem(i).getLocalBumped())
                continue;
            ncounter ++;

        }
        return ncounter;
    }

    public KDSDataItem getPrevActiveItem(int fromIndex)
    {
        if (fromIndex >= Integer.MAX_VALUE)
            fromIndex = m_items.getCount()-1;
        for (int i=fromIndex; i>=0; i--)
        {
            if (m_items.getItem(i).isMarked() ||
                    m_items.getItem(i).isReady() ||
                    m_items.getItem(i).getLocalBumped())
                continue;
            return m_items.getItem(i);
        }
        return null;
    }

    public int getPrevActiveItemsCount(int fromIndex)
    {
        int nCounter = 0;
        if (fromIndex >= Integer.MAX_VALUE)
            fromIndex = m_items.getCount()-1;
        for (int i=fromIndex; i>=0; i--)
        {
            if (m_items.getItem(i).isMarked() ||
                    m_items.getItem(i).isReady() ||
                    m_items.getItem(i).getLocalBumped())
                continue;
            nCounter ++;

        }
        return nCounter;
    }

    public int getSequenceIndexFromActiveIndext(int activeIndex)
    {

        int nActiveIndex = -1;
        for (int i=0; i<getItems().getCount(); i++)
        {
            if (m_items.getItem(i).isMarked() ||
                    m_items.getItem(i).isReady() ||
                    m_items.getItem(i).getLocalBumped())
                continue;
            nActiveIndex ++;
            if (nActiveIndex == activeIndex)
                return i;

        }
        return -1;
    }

    public boolean getTTReceiveExpoBumpNotification()
    {
        return m_bTTReceiveExpoBumpNotification;
    }

    public void setTTReceiveExpoBumpNotification(boolean bReceived)
    {
        m_bTTReceiveExpoBumpNotification = bReceived;
    }

    public Date getTTReceiveExpoBumpNotificationDate()
    {
        return m_dtTTReceiveExpoBumpNotification;
    }

    public void setTTReceiveExpoBumpNotificationDate(Date dt)
    {
        m_dtTTReceiveExpoBumpNotification = dt;
    }

    public boolean getTTAllItemsBumped()
    {
        return m_bTTAllItemsBumped;
    }

    public void setTTAllItemsBumped(boolean bBumped)
    {
        m_bTTAllItemsBumped = bBumped;
    }

    public Date getTTAllItemsBumpedDate()
    {
        return m_dtTTAllItemsBumped;
    }

    public void setTTAllItemsBumpedDate(Date dt)
    {
        m_dtTTAllItemsBumped = dt;
    }
    public boolean getTTFindMyTrackerID()
    {
        return m_bTTFindMyTrackerID;
    }

    public void setTTFindMyTrackerID(boolean bFind)
    {
        m_bTTFindMyTrackerID = bFind;
    }

    public void setItemHiddenOptionAfterGetNewOrder(String stationID)
    {
        for (int i=0;i <this.getItems().getCount(); i++)
        {
            this.getItems().getItem(i).setHiddenAccordingToHiddenStations(stationID);
        }
    }

    public PrepSorts prep_get_sorts()
    {
        return m_prepSorts;
    }

    public void prep_set_sorts(PrepSorts sorts)
    {
        m_prepSorts = sorts;
    }

    public boolean isAllItemsNotForNew()
    {
        for (int i=0;i <this.getItems().getCount(); i++)
        {
            if (this.getItems().getItem(i).getTransType() == TRANSTYPE_ADD)
                return false;
        }
        return  true;
    }

    /**
     * in smart mode the timer delay how many seconds.
     * @param nSmartTimerDelayShowing
     */
    public void smartSetTimerDelay(int nSmartTimerDelayShowing)
    {
        m_nSmartTimerDelayShowing = nSmartTimerDelayShowing;
    }

    public int smartGetTimerDelay()
    {
        return m_nSmartTimerDelayShowing;
    }

    public void setAutoBumpStartCountTime(Date dt)
    {
        m_dtAutoBumpStartCountTime = dt;
        if (dt == null)//2.0.16
            m_dtAutoBumpStartCountTime = new Date(m_dtStartTime.getTime());
    }
    public Date getAutoBumpStartCountTime()
    {
        if (m_dtAutoBumpStartCountTime == null)//2.0.16, make sure it is not null
            return new Date(m_dtStartTime.getTime());
        return m_dtAutoBumpStartCountTime;
    }

    /**
     * 2.0.25
     * @return
     */
    public int getActiveItemsCount()
    {
        int nCounter = 0;

        int  fromIndex = m_items.getCount()-1;
        for (int i=fromIndex; i>=0; i--)
        {
            if (m_items.getItem(i).isMarked() ||
                    m_items.getItem(i).isReady() ||
                    m_items.getItem(i).getLocalBumped())
                continue;
            nCounter ++;

        }
        return nCounter;
    }

    /**
     * 2.0.25

     * @return
     */
    public KDSDataItem getFirstActiveItem()
    {
        for (int i=0; i< m_items.getCount(); i++)
        {
            if (m_items.getItem(i).isMarked() ||
                    m_items.getItem(i).isReady() ||
                    m_items.getItem(i).getLocalBumped())
                continue;
            return m_items.getItem(i);
        }
        return null;
    }

    public void setQueueStateTime(Date dt)
    {
        m_dtQueueStateTime = dt;
    }

    public Date getQueueStateTime()
    {
        return m_dtQueueStateTime;
    }
}
