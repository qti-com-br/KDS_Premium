
package com.bematechus.kdslib;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

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
    protected int m_nStatus = ORDER_STATUS_UNKNOWN;//kpp1-425, use this as default;   ORDER_STATUS_PAID;
    protected int m_nSortIdx = -1;
    protected float m_fltOrderDelay = 0; //smart order
    protected  boolean m_bFromPrimaryOfBackup = false;

    protected KDSDataMessages m_messages = new KDSDataMessages();
    protected KDSDataItems m_items = new KDSDataItems();
    protected Object m_objTag = null;

    protected KDSDataCustomer m_customer = new KDSDataCustomer();

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

    //2.0.50, sms feature
    // see https://bematech.atlassian.net/browse/KPP1-15
    public static final int SMS_STATE_UNKNOWN = -1;
    public static final int SMS_STATE_NEW = 0;
    public static final int SMS_STATE_PREPARED = 1;
    public static final int SMS_STATE_DONE = 2;

//    String m_smsCustomerID = "";
//    String m_smsCustomerPhone = "";
    int m_smsLastState = SMS_STATE_UNKNOWN; //this state has been send to server.

    //for SMS. If no expo existed, use it to record which has bumped/(items bumped).
    //format:
    //stationID\nAllDone, stationID\nAllDone
    //save to database order table, r4.
    String m_smsOriginalOrderGoToStations = "";


    /**
     * When auto-bump is enable, after the time reaches, it is impossible to ???unbump??? an order to
     * current screen anymore since it has time > auto bump time. So for this, when auto ??? bump is enable,
     * the time to ???auto - bump??? again will be when this order reappear on the screen to set auto bump time,
     * note: you don???t need to reset the timer.
     * E.G.: Order 1 time start from 0, Auto bump time = 10;
     * after 15 min, recall order 1,
     * then ???Auto bump??? order 1 will be at 25 min,
     * and time for order 1 = 25:00.
     */
    Date m_dtAutoBumpStartCountTime = new Date();
    /***************************************************************************/
    // for modiy, delete ...
    protected int m_nTransactionType = TRANSTYPE_ADD; //see above definition, don't save it to database

    //kpp1-75
    protected String m_kdsGUID = KDSUtil.createNewGUID();; //for backoffice. KPP1-75. Use it to identify same order in all stations.


    protected String mHeaderFooterMessage = ""; //KP-48 Allergen xml tags.

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
        HeaderFooterMessage,
        AutoUnpark,
        //2.0.50
//        SMS_Customer_ID,
//        SMS_Customer_Phone,

        Count
    };


    protected boolean[] m_arValidFields;

    Date m_autoUnparkDate = KDSUtil.createInvalidDate();

    String m_strInputMessage = ""; //kp-114, Input a number in Order

    //KP-171, course time.
    //long m_courseStartMs = 0; //when course time started after order start time. Unit MS.

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

        //2.0.50
        //obj.m_smsCustomerID = m_smsCustomerID;
        //obj.m_smsCustomerPhone = m_smsCustomerPhone;
        obj.m_smsLastState = m_smsLastState;
        //2.1.15
        obj.m_smsOriginalOrderGoToStations = m_smsOriginalOrderGoToStations;
        //
        obj.m_dtQueueStateTime = m_dtQueueStateTime;

        obj.m_autoUnparkDate = m_autoUnparkDate;

        this.getOrderMessages().copyTo(obj.getOrderMessages());
        this.getCustomer().copyTo(obj.getCustomer());

        obj.m_kdsGUID = m_kdsGUID;
        obj.mHeaderFooterMessage = mHeaderFooterMessage;
        obj.m_strInputMessage = m_strInputMessage;//kp-114
        //obj.m_courseStartMs = m_courseStartMs;//kp-171

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
            + "Status,SortIdx,OrderDelay,fromprimary,bumpedtime,r0,r1,r2,r3,r4,r5,r6,r7,r8)"
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
            + ",0" //2.0.34, use it for queue status sort.
            + ",'" + m_customer.getID() +"'" //2.0.50, for sms customer id
            +",'" + m_customer.getPhone() + "'" //2.0.50 for sms customer phone number
            + "," +  KDSUtil.convertFloatToString(m_smsLastState)  ////2.0.50 for sms state.//-1=unknown, 0 = new, 1 = prepared, 2 = done
            + ",'" + m_smsOriginalOrderGoToStations +"'" //r4
            +",'" + m_customer.getName() + "'" //r5
            +",'" + getKDSGuid() + "'" //r6
            + ",'" + getHeaderFooterMessage() + "'"
            + ",'" + getAutoUnparkDateString() + "'"
            +  ")";
        return sql;
         
        
    }

    public String sqlAddNewForStatistic(String tblName)
    {
        if (tblName.isEmpty())
            tblName = "orders";
        if (KDSConst.ENABLE_FEATURE_STATISTIC) {
            String sql = "insert into "
                    + tblName
                    + " ("
                    + "GUID,Name,Waiter,Start,ToTbl,"
                    + "Station,Screen,POS,OrderType,Dest,"
                    + "CustMsg,QueueMsg,Parked,IconIdx,EvtFired,PrepStart,"
                    + "Status,SortIdx,OrderDelay,fromprimary)"
                    + " values ("
                    + "'" + getGUID() + "'"
                    + ",'" + fixSqliteSingleQuotationIssue(getOrderName()) + "'"
                    + ",'" + fixSqliteSingleQuotationIssue(getWaiterName()) + "'"
                    + ",'" + KDSUtil.convertDateToString(getStartTime()) + "'"
                    + ",'" + fixSqliteSingleQuotationIssue(getToTable()) + "'"

                    + ",'" + fixSqliteSingleQuotationIssue(getPCKDSNumber()) + "'"
                    + "," + KDSUtil.convertIntToString(getScreen())
                    + ",'" + fixSqliteSingleQuotationIssue(getFromPOSNumber()) + "'"
                    + ",'" + fixSqliteSingleQuotationIssue(getOrderType()) + "'"
                    + ",'" + fixSqliteSingleQuotationIssue(getDestination()) + "'"

                    + ",'" + fixSqliteSingleQuotationIssue(getCustomMsg()) + "'"
                    + ",'" + fixSqliteSingleQuotationIssue(getQueueMessage()) + "'"

                    + "," + KDSUtil.convertIntToString(getParked())
                    + "," + KDSUtil.convertIntToString(getIconIdx())
                    + "," + KDSUtil.convertIntToString(getOrderEvtFired())
                    + ",'" + KDSUtil.convertDateToString(getPreparationStartTime()) + "'"
                    + "," + KDSUtil.convertIntToString(getStatus())
                    + "," + KDSUtil.convertIntToString(getSortIdx())
                    + "," + KDSUtil.convertFloatToString(getOrderDelay())
                    + ",0 )";
            return sql;
        }
        else
        {
            Date dt = new Date();
            String strDt = KDSUtil.convertDateToString(dt);

            String sql = "insert into "
                    + tblName
                    + " ( GUID,Screen,Start,finishedtime)"
                    + " values ("
                    + "'" + getGUID() + "'"
                    + "," + KDSUtil.convertIntToString(getScreen())
                    + ",'" + KDSUtil.convertDateToString(getStartTime()) + "'"
                    + ",'" + strDt +"' )";
            return sql;
        }

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

        //if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.Waiter_Name))
            sql +=  "DBTimeStamp='" + KDSUtil.convertDateToString(getTimeStamp()) + "',";

        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.HeaderFooterMessage))
            sql +=  "r7='" + getHeaderFooterMessage() + "', ";
        if (this.getXmlFieldValid(VALID_ORDER_XML_FIELD.AutoUnpark))
            sql +=  "r8='" + getAutoUnparkDateString() + "', ";
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
    public static KDSDataOrder createTestOrder2(String orderName, int nItemsCount, String toStations, int toScreen)
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
        c.setScreen(toScreen);
        c.setWaiterName("David Wong");
        c.setSortIdx(-1);
        c.setStartTime(new Date());
        c.setStatus(0);
        c.setToTable("Tbl #4");
        c.setTrackerID("2");
        c.setPagerID("12");
        c.setIconIdx(1);
        //
        c.setParked(true);
        c.setAutoUnparkDate(new Date());

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
        
        for (int i=0; i< nItemsCount; i++) {
            KDSDataItem item = new KDSDataItem(c.getGUID());
            item.setAddOnGroup(-1);
//            if (i ==2)
//                item.setHidden(true); //test
            //item.setBG(Color.white);
            //item.setFG(Color.BLACK);
            item.setCategory("Category #2");
            //item.setDescription("item #" + KDSUtil.convertIntToString(i));
            item.setDescription("item " + KDSUtil.convertIntToString(i));
            item.setItemName("itemname" + KDSUtil.convertIntToString(i));
            item.setToStationsString(toStations);
            item.setOrderID(-1);
            item.setQty(2);
            for (int n = 0; n < 1; n++)
            {
                KDSDataModifier m = new KDSDataModifier();
                m.setDescription("Modifier $" + KDSUtil.convertIntToString(n));
                m.setCondimentName("Modifier $" + KDSUtil.convertIntToString(n));
                m.setItemGUID(item.getGUID());
                item.getModifiers().addComponent(m);
            }
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
            item.setPreModifiers(msgs);

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
            item.setPreModifiers(msgs);

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
            item.setPreModifiers(msgs);

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
            item.setPreModifiers(msgs);

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
            item.setPreModifiers(msgs);

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
        if (orderReceived.getXmlFieldValid((VALID_ORDER_XML_FIELD.HeaderFooterMessage)))
        {
            this.setHeaderFooterMessage(orderReceived.getHeaderFooterMessage());
            bResult = true;
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
        //TimeDog td = new TimeDog();
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root( KDSConst.KDS_Str_Transaction);
        outputOrderInformationToXML(xml, true);
        //td.debug_print_Duration("createXml-1");
        this.getItems().outputXml(xml);
        //td.debug_print_Duration("createXml-2");
        String s = xml.get_xml_string();
        //td.debug_print_Duration("createXml");
        return s;
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
    /**
     * rev.:
     *  kpp1-425, don't output empty values. If so, it will modify existed data when transtype is "modify"
     * @param pxml
     * @param bHasTransType
     * @return
     */
    public boolean outputOrderInformationToXML(KDSXML pxml, boolean bHasTransType)
    {
            if (pxml == null) return false;

            pxml.back_to_root();
            pxml.getFirstGroup(KDSConst.KDS_Str_Transaction);

            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ORDER, true);
        //2.0.51
            pxml.newAttribute(KDSXMLParserOrder.DBXML_ELEMENT_QUEUE_READY, getQueueReady()?"1":"0");

            //pxml->xmj_getFrstGroup(_T("Order"));
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_KDSGUID,this.getKDSGuid(), false);
            //
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ID,this.getOrderName(), false);
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_GUID,this.getGUID(), false);

            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_HEADERFOOTERMESSAGE,this.getHeaderFooterMessage(), false);

            String s;
            
            s =this.getFromPOSNumber();
            if (!s.isEmpty()) //kpp1-425
                pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TERMINAL,s, false);
            if (bHasTransType)
            {
                String transType = KDSUtil.convertIntToString( this.getTransType());
                if (this.getItems().getCount()==0)
                {
                    //kpp1-409
                    if (this.getTransType() != TRANSTYPE_DELETE)//don't change delete
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
            if (this.getStatus() != ORDER_STATUS_UNKNOWN)//kpp1-425
                pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ORDERSTATUS, KDSUtil.convertIntToString(this.getStatus() ), false);
            if (!this.getOrderType().isEmpty())//kpp1-425
                pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ORDERTYPE, this.getOrderType() , false);
            if (!this.getWaiterName().isEmpty())//kpp1-425
                pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_OPERATOR, this.getWaiterName(), false);

            if (!this.getDestination().isEmpty())//kpp1-425
                pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_DESTINATION,this.getDestination(), false);
            if (!this.getToTable().isEmpty())//kpp1-425
                pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TABLE,this.getToTable(), false);

            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TOSCREEN,KDSUtil.convertIntToString(this.getScreen()), false);

           // pxml.newGroup("UserInfo",this.getCustomMsg(), false);
            if (!this.getCustomMsg().isEmpty())//kpp1-425
                pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_USERINFO,this.getCustomMsg(), false);
            if (!this.getQueueMessage().isEmpty())//kpp1-425
                pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_QUEUEMSG,this.getQueueMessage(), false);
            if (getParked() == 1) {

                pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_PARKED,KDSUtil.convertIntToString( this.getParked()), true);
                if (!KDSUtil.isInvalidDate(m_autoUnparkDate))
                    pxml.newAttribute(KDSXMLParserOrder.DBXML_ELEMENT_AUTOUNPARK, KDSUtil.convertDateToString(this.getAutoUnparkDate()));
                pxml.back_to_parent();
            }
            //remove these feature.
            //pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TRACKERID,this.getTrackerID(), false);
            //pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_PAGERID,this.getPagerID(), false);

            //2.0.50 SMS feature
            //rev.: kpp1-425, just output valid data.
            if (!this.getCustomer().getID().isEmpty() ||
                    !this.getCustomer().getPhone().isEmpty() ||
                    !this.getCustomer().getName().isEmpty())
            {
                pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_CUSTOMER, true);
                if (!this.getCustomer().getID().isEmpty())//kpp1-425
                    pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ID, this.getCustomer().getID(), false);
                if (!this.getCustomer().getPhone().isEmpty())//kpp1-425
                    pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_PHONE, this.getCustomer().getPhone(), false);
                if (!this.getCustomer().getName().isEmpty())//kpp1-425
                    pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_NAME, this.getCustomer().getName(), false);
                pxml.back_to_parent(); //kpp1-222
            }
            //

            //2.5.4.19 add received time and restore time
            s = KDSUtil.convertDateToString(this.getStartTime());
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_RECEIVE_DATE,s, false);

            Date dtNow = new Date();
            
            s = KDSUtil.convertDateToString(dtNow);
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_BUMPED_DATE,s, false);
            //2.0.51

            //////
            if (this.getOrderMessages().getCount()>0) //kpp1-425
                outputKDSMessages(pxml, this.getOrderMessages(), KDSXMLParserOrder.DBXML_ELEMENT_ORDER_MESSAGES);

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
//        long between=(end.getTime()-begin.getTime())/1000;//??????1000?????????????????????
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

        long between=(end.getTime()-begin.getTime())/1000;//??????1000?????????????????????
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

        long between=(end.getTime()-begin.getTime())/1000;//??????1000?????????????????????
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
    public boolean smartItemIsTimeToCook(KDSDataItem item)
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

    public PrepSorts smart_get_sorts()
    {
        return m_prepSorts;
    }

    public void smart_set_sorts(PrepSorts sorts)
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

    /**
     * KPP1-7
     * Queue display order stuck
     * @return
     */
    public int getBumpedItemsCount()
    {

        int nCount = 0;
        for (int i=0; i< m_items.getCount(); i++)
        {
            KDSDataItem item = m_items.getItem(i);
            if (item.isFinished())
                nCount ++;
        }
        return nCount;
    }

    /**
     * KPP1-7
     * Queue display order stuck
     * @return
     */
    public ArrayList<String> getBumpedItemsID()
    {

        ArrayList<String> ar = new ArrayList<>();
        int nCount = 0;
        for (int i=0; i< m_items.getCount(); i++)
        {
            KDSDataItem item = m_items.getItem(i);
            if (item.isFinished())
                ar.add(item.getItemName());

        }
        return ar;
    }

    public String getBumpedItemsIDString()
    {
        ArrayList<String> ar = getBumpedItemsID();
        String s = "";


        for (int i=0; i< ar.size(); i++)
        {
            if (i >0)
                s += ",";
            s += ar.get(i);


        }
        return s;
    }



//    /**
//     * 2.0.50
//     * @return
//     */
//    public String getSMSCustomerID()
//    {
//        return m_smsCustomerID;
//    }

//    /**
//     * 2.0.50
//     * @param customerID
//     */
//    public void setSMSCustomerID(String customerID)
//    {
//        m_smsCustomerID = customerID;
//    }

//    /**
//     * 2.0.50 SMS feature
//     * @return
//     */
//    public String getSMSCustomerPhone()
//    {
//        return m_smsCustomerPhone;
//    }

//    /**
//     * 2.0.50 SMS feature
//     * @param phone
//     */
//    public void setSMSCustomerPhone(String phone)
//    {
//        m_smsCustomerPhone = phone;
//    }

    /**
     * 2.0.50 SMS feature
     * @return
     */
    public int getSMSLastSendState()
    {
        return m_smsLastState;
    }

    /**
     * 2.0.50 SMS feature
     * @param nSMSState
     */
    public void setSMSLastSendState(int nSMSState)
    {
        m_smsLastState = nSMSState;
    }

    public int getFinishedItemsCount()
    {
        int nFinished = 0;
        try {
            int ncount = this.getItems().getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataItem item = this.getItems().getItem(i);
                if (item.getLocalBumped() ||
                        (!item.getBumpedStationsString().isEmpty()) ||
                        item.isReady() ||
                        item.isMarked())
                    nFinished++;


            }
        }
        catch ( Exception e)
        {

        }
        return nFinished;

    }

    public int getExpoBumpedItemsCount()
    {
        int nBumped = 0;
        int ncount = this.getItems().getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if ( item.getLocalBumped() )
                nBumped ++;


        }
        return nBumped;

    }

    public int getExpoReadyItemsCount()
    {
        int nReady = 0;
        int ncount = this.getItems().getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if ((!item.getBumpedStationsString().isEmpty()) )
                nReady ++;


        }
        return nReady;

    }

    /**
     *
     * @return
     */
    public int getSMSCurrentState(boolean bExpoStation, boolean bOrderBumped)
    {
        int nFinished = this.getFinishedItemsCount();
        int nItemsCount = this.getItems().getCount();

        if (!bExpoStation) {
            if (nFinished == nItemsCount ||
                    bOrderBumped)
                return SMS_STATE_PREPARED;
            else
                return SMS_STATE_NEW;
        }
        else
        { //expo station
            if (bOrderBumped)
                return SMS_STATE_DONE;
            int nReady = getExpoReadyItemsCount();
            int nBumped = getExpoBumpedItemsCount();

            if (nBumped == nItemsCount)
                return SMS_STATE_DONE;
            else if (nReady == nItemsCount)
                return SMS_STATE_PREPARED;
            else
                return SMS_STATE_NEW;

        }

    }

    public boolean isSMSStateChanged(boolean bIsExpoStation, boolean bOrderBumped)
    {
        int nSMSState = this.getSMSCurrentState(bIsExpoStation, bOrderBumped);
        int nSendState = getSMSLastSendState();
        return (nSendState < nSMSState);
    }

    static public ArrayList<KDSToStation> getOrderTargetStations(KDSDataOrder order)
    {

        ArrayList<KDSToStation> ar = new ArrayList<>();


        for (int i=0; i< order.getItems().getCount(); i++)
        {
            KDSDataItem item = order.getItems().getItem(i);
            KDSToStations toStations = item.getToStations();
            for (int j=0; j< toStations.getCount(); j++)
            {
                KDSToStation toStation = toStations.getToStation(j);
                if (isExistedInArrary(ar, toStation))
                    continue;
                ar.add(toStation);
            }
        }
        return ar;
    }
    static public boolean isExistedInArrary(ArrayList<KDSToStation> ar, KDSToStation toStation)
    {
        for (int i=0; i< ar.size(); i++)
        {
            if ( ar.get(i).getPrimaryStation().equals(toStation.getPrimaryStation()) &&
                    ar.get(i).getSlaveStation().equals(toStation.getSlaveStation()) )
                return true;
        }
        return false;
    }

    public String getSmsOriginalOrderGoToStations()
    {
        return m_smsOriginalOrderGoToStations;
    }
    public void setSmsOriginalToStations(String goToStations)
    {
        m_smsOriginalOrderGoToStations = goToStations;
    }

    public void setSmsOriginalToStations(ArrayList<KDSToStation> arTargetStations)
    {
        ArrayList<SmsStationsState> ar = new ArrayList<>();

        for (int i=0; i< arTargetStations.size(); i++)
        {
            KDSToStation toStation = arTargetStations.get(i);
            String stationID = toStation.getPrimaryStation();
            SmsStationsState state = new SmsStationsState();
            state.m_stationID = stationID;
            ar.add(state);

        }
        String s = smsStationsStateToString(ar);
        setSmsOriginalToStations(s);

    }

    /**
     * change m_smsOriginalOrderGoToStations
     * //for SMS. If no expo existed, use it to record which has bumped/(items bumped).
     //format:
     //stationID\nAllDone, stationID\nAllDone
     //save to database order table, r4.
     * @param stationID
     * @param bDone
     *  the order was bumped or all items is done.
     */
    public void setSmsOriginalToStationState(String stationID, boolean bDone)
    {
        ArrayList<SmsStationsState> ar = parseSmsOriginalToStations();
        SmsStationsState state = findSmsStationState(ar, stationID);
        if (state == null) return;
        state.m_bDone = bDone;
        m_smsOriginalOrderGoToStations = smsStationsStateToString(ar);
    }



    private String smsStationsStateToString(ArrayList<SmsStationsState> arStates )
    {
        String s = "";
        for (int i=0; i< arStates.size(); i++)
        {
            SmsStationsState state = arStates.get(i);
            if (!s.isEmpty())
                s += ",";
            s += state.toString();
        }
        return s;
    }


    private SmsStationsState findSmsStationState(ArrayList<SmsStationsState> arStates, String stationID)
    {
        for (int i=0; i< arStates.size(); i++)
        {
            SmsStationsState state = arStates.get(i);
            if (state.m_stationID.equals(stationID))
                return state;
        }
        return null;
    }
    private ArrayList<SmsStationsState> parseSmsOriginalToStations()
    {
        ArrayList<String> ar = KDSUtil.spliteString(m_smsOriginalOrderGoToStations, ",");

        ArrayList<SmsStationsState> arStates = new ArrayList<>();

        for (int i=0; i< ar.size(); i++)
        {
            String s = ar.get(i);
            SmsStationsState state = parseSmsStationState(s);
            if (state != null)
            {
                arStates.add(state);
            }
        }
        return arStates;

    }
    private SmsStationsState parseSmsStationState(String smsStationState)
    {
        ArrayList<String> ar = KDSUtil.spliteString(smsStationState, "\n");
        if (ar.size() !=2) return null;

        String stationID =  ar.get(0);
        String strDone = ar.get(1);

        SmsStationsState state = new SmsStationsState();
        state.m_stationID = stationID;
        state.m_bDone = KDSUtil.convertStringToBool(strDone, false);
        return state;

    }

    public  String findSMSMinStationID()
    {
        if (m_smsOriginalOrderGoToStations.isEmpty())
            return "";
        String minStationID = "";
        ArrayList<SmsStationsState> ar =  parseSmsOriginalToStations();

        for (int i=0; i< ar.size(); i++)
        {
            String stationID = ar.get(i).m_stationID;
            if (minStationID.isEmpty())
                minStationID = stationID;
            else
            {
                if (stationID.compareTo(minStationID)<0)
                    minStationID = stationID;

            }
        }
        return minStationID;
    }

    public boolean isSMSAllOtherStationsDone(String myStationIDException)
    {
        ArrayList<SmsStationsState> ar =  parseSmsOriginalToStations();
        boolean bAllOthersDone = true;
        for (int i=0; i<ar.size(); i++)
        {
            if (ar.get(i).m_stationID.equals(myStationIDException))
                continue;

            if (!ar.get(i).m_bDone)
                bAllOthersDone = false;

        }
        return bAllOthersDone;
    }

    public boolean isSMSStationsDone(String myStationIDException)
    {
        ArrayList<SmsStationsState> ar =  parseSmsOriginalToStations();

        for (int i=0; i<ar.size(); i++)
        {
            if (!ar.get(i).m_stationID.equals(myStationIDException))
                continue;

            return (ar.get(i).m_bDone);


        }
        return false;
    }

    class SmsStationsState
    {
        public String m_stationID = "";
        public  boolean m_bDone = false;
        public String toString()
        {
            String s = m_stationID;
            s += "\n";
            s += m_bDone?"1":"0";
            return s;
        }
    }

    /**
     * Just contains ID in it. Order ID, item ID. Without condiments ID.
     * It is for order bump notification. Make the data flow shortly
     * format:
     *  <Order>
     *      <ID><ID/>
     *      <Item>
     *          <ID></ID>
     *      </Item>
     *      <Item>
     *         <ID></ID>
     *      </Item>
     *  </Order>
     * @return
     */
    public String createIDXml()
    {
        //TimeDog td = new TimeDog();
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root( KDSConst.KDS_Str_Transaction);
        xml.back_to_root();
        xml.getFirstGroup(KDSConst.KDS_Str_Transaction);

        xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ORDER, true);

        xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ID,this.getOrderName(), false);
        //xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_GUID,this.getGUID(), false);

        for (int i=0; i< getItems().getCount(); i++)
        {
            xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ITEM, true);
            xml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ID,getItems().getItem(i).getItemName(), false);
            xml.back_to_parent();
        }

        String s = xml.get_xml_string();

        return s;
    }

    public String makeQueueDurationString() {
        Date begin = getQueueStateTime();
        return makeDurationString(begin);
    }

    public void setItemsTransferedFromStationID(String fromStationID)
    {
        for (int i=0; i< getItems().getCount(); i++)
        {
            getItems().getItem(i).setTransferedFromStationID(fromStationID);
        }
    }

    public KDSDataCustomer getCustomer()
    {
        return m_customer;
    }

    public void setKDSGuid(String guid)
    {
        m_kdsGUID = guid;
    }
    public String getKDSGuid()
    {
        return m_kdsGUID;
    }

    static public String getSMSStateString(int nState)
    {
        switch (nState)
        {
            case SMS_STATE_NEW:
                return "New";
            case SMS_STATE_PREPARED:
                return "Prepared";
            case SMS_STATE_DONE:
                return "Done";
            case SMS_STATE_UNKNOWN:
                return "Unknown";
            default:
                return "Unknown";
        }
    }

    public boolean isAllItemsBumpedInLocal()
    {
        int ncount = this.getItems().getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if (!item.getLocalBumped())
                return false;
        }
        return true;
    }

    public static KDSDataOrder createTestOrder(String orderName, int nItemsCount, String toStations, int toScreen)
    {

        String[] arItemsDescription = {"Cheese burger;Without cheese",         "Zinger burger",            "Extra tasty crispy",       "Fries;10 tomato ketchup",            "Chikcen Loaf",
                                        "Original Recipe",      "Sundae;With strawberry;With blueberry","Orange juice",    "7-Up",                     "Mirinda Orange",
                                        "Roast Chicken Wings", "Fresh Grade Legs",          "Chicken Popcorn",          "Mashed Potatoes", "Corn Salad" ,
                                       "Egg tart",              "Ice-cream cone",           "Coffee;Without sugar",                       "Black Tea;With milk",        "Pepsi-Cola;Without ice" };

        KDSDataOrder c = new KDSDataOrder();
        c.setCustomMsg("Customer message");
        c.setQueueMessage("Queue message");
        c.setDestination("Fast food");
        c.setFromPOSNumber("5");
        c.setOrderName(orderName);
        //c.setOrderType("RUSH");
        c.setPCKDSNumber("1");
        c.setPreparationStartTime(new Date());
        c.setScreen(toScreen);
        c.setWaiterName("Jack");
        c.setSortIdx(-1);
        c.setStartTime(new Date());
        c.setStatus(0);
        c.setToTable("Tbl #4");
        //c.setTrackerID("2");
        c.setPagerID("12");
        c.setIconIdx(1);

        KDSDataMessages msg = new KDSDataMessages();
        for (int n=0; n<1; n++)
        {
            KDSDataMessage m = new KDSDataMessage();
            m.setComponentGUID(c.getGUID());
            m.setForComponentType(KDSDataMessage.FOR_Order);

            m.setMessage("VIP customer");//Order Message " + KDSUtil.convertIntToString(n));
            msg.addComponent(m);
        }
        c.setOrderMessages(msg);
        Random r = new Random();
        for (int i=0; i< nItemsCount; i++) {
            KDSDataItem item = new KDSDataItem(c.getGUID());
            item.setAddOnGroup(-1);
//            if (i ==2)
//                item.setHidden(true); //test
            //item.setBG(Color.white);
            //item.setFG(Color.BLACK);
            item.setCategory("Category #2");

            int index = r.nextInt(arItemsDescription.length);
            String s = arItemsDescription[index];
            ArrayList<String> names = KDSUtil.spliteString(s, ";");

            //item.setDescription("item #" + KDSUtil.convertIntToString(i));
            item.setDescription(names.get(0));
            item.setItemName("itemname" + KDSUtil.convertIntToString(i));
            item.setToStationsString(toStations);
            item.setOrderID(-1);
            item.setQty(2);
//            for (int n = 0; n < 1; n++)
//            {
//                KDSDataModifier m = new KDSDataModifier();
//                m.setDescription("Modifier $" + KDSUtil.convertIntToString(n));
//                m.setCondimentName("Modifier $" + KDSUtil.convertIntToString(n));
//                m.setItemGUID(item.getGUID());
//                item.getModifiers().addComponent(m);
//            }
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
//            item.setPreModifiers(msgs);

            //condiments
            if (names.size() >1)
            for (int j=1; j<names.size(); j++)
            {
                KDSDataCondiment d = new KDSDataCondiment(item.getGUID());
                d.setCondimentName("condiment $" + KDSUtil.convertIntToString(j));
                d.setDescription(names.get(j));
                item.getCondiments().addComponent(d);
            }
            c.getItems().addComponent(item);

        }
        return c;
    }

    /**
     * kpp1-343, expo contains itself items
     * @return
     */
    public boolean isExpoAllItemsFinished(String expoStatonID)
    {
        int ncount = this.getItems().getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if (item.getLocalBumped() ||
                    (!item.getBumpedStationsString().isEmpty()) )
                continue;
            //kpp1-343, if item send to expo, treat it as finished.
            if (item.getToStations().findStation(expoStatonID) != KDSToStations.PrimarySlaveStation.Unknown)
                continue;
            return false;
        }
        return true;
    }


    /**
     * kp1-25
     * @param category
     * @return
     */
    public boolean smartCategoryItemsLocalFinished(String category)
    {
        int ncount = this.getItems().getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if (!item.getCategory().equals(category)) continue;
            if (!item.getLocalBumped())
                return false;
        }
        return true;
    }

    public boolean smartCategoryItemsLocalFinished(ArrayList<String> categories)
    {
        int ncount = this.getItems().getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if (!KDSUtil.isExistedInArray(categories, item.getCategory()))
                continue;
            //if (!item.getCategory().equals(category)) continue;
            if (!item.getLocalBumped())
                return false;
        }
        return true;
    }

    /**
     * 
     * @param category
     * @return
     */
    public boolean smartCategoryItemsRemoteFinished(String category)
    {

        int ncount = this.getItems().getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if (!item.getCategory().equals(category)) continue;
            if (item.getBumpedStationsString().isEmpty() )
               return false;
        }
        return true;
    }

    public boolean smartCategoryItemsRemoteFinished(ArrayList<String> categories)
    {

        int ncount = this.getItems().getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            //if (!item.getCategory().equals(category)) continue;
            if (!KDSUtil.isExistedInArray(categories, item.getCategory()))
                continue;;
            if (item.getBumpedStationsString().isEmpty() )
                return false;
        }
        return true;
    }

    public ArrayList<String> getAllCategories()
    {
        ArrayList<String> ar = new ArrayList<>();

        for (int i=0; i< m_items.getCount(); i++)
        {
            if (KDSUtil.isExistedInArray(ar, m_items.getItem(i).getCategory() ) )
                continue;
            else
                ar.add(m_items.getItem(i).getCategory());
        }

        return ar;
    }

    public boolean isAllItemsFinishedForSumStation()
    {
        int ncount = this.getItems().getCount();
//        if (ncount<=0)
//        {
//            if (this.getTag() instanceof Integer)
//            {
//                ncount =(int) this.getTag();
//            }
//           // Log.d("Order", "Items count =0");
//
//        }
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

    public boolean smartRunnerSameCatDelayItemsLocalFinished(ArrayList<PrepSorts.PrepItem> smartItems)
    {
        int ncount = smartItems.size();
        for (int i=0; i< ncount; i++)
        {
            PrepSorts.PrepItem smartItem = smartItems.get(i);
            KDSDataItem item =  this.getItems().getItemByName(smartItem.ItemName);
            if (item == null ) continue;
            if (!item.getLocalBumped())
                return false;
        }
        return true;
    }

    /**
     * kp-48 Allergen xml tags.
     * @param s
     */
    public void setHeaderFooterMessage(String s)
    {
        mHeaderFooterMessage = s;
    }

    public String getHeaderFooterMessage()
    {
        return mHeaderFooterMessage;
    }


    public void setAutoUnparkDate(Date dt)
    {
        m_autoUnparkDate = dt;
    }

    public Date getAutoUnparkDate()
    {
        return m_autoUnparkDate;
    }

    public String getAutoUnparkDateString()
    {
        if (KDSUtil.isInvalidDate(m_autoUnparkDate))
            return "";
        return KDSUtil.convertDateToString(m_autoUnparkDate);
    }

    public boolean isUnparkTime()
    {
        if (getParked() != 1) return false;
        if (KDSUtil.isInvalidDate( getAutoUnparkDate()))
            return false;
        TimeDog td = new TimeDog(getAutoUnparkDate());
        return (td.is_timeout(1));



    }

    /**
     * kp-114, Input a number in Order
     * @param msg
     */
    public void setInputMessage(String msg)
    {
        m_strInputMessage = msg;
    }

    public String getInputMessage()
    {
        return m_strInputMessage;
    }

    public String recreateGUID()
    {
        m_strGUID = KDSUtil.createNewGUID();
        //kp-160 update all items with new order guid, otherwise all items lost.
        for (int i=0; i< m_items.getCount(); i++)
        {
            m_items.getDataItem(i).setOrderGUID(m_strGUID);
        }
        return m_strGUID;
    }

    public float getMaxPreparationTime()
    {
        float nMax = 0;
        for (int i=0; i< m_items.getCount(); i++)
        {
            float flt = m_items.getDataItem(i).getTotalPrepTime();
            if (flt > nMax)
                nMax = flt;
        }
        return nMax;

    }

    /**
     * KP-171 course time
     * @param nms
     */
//    public void setCourseStartTime(long nms)
//    {
//        m_courseStartMs = nms;
//    }

    /**
     * KP-171 course time
     *
     * @return
     */
//    public long getCourseStartTime()
//    {
//        return m_courseStartMs;
//    }

}
