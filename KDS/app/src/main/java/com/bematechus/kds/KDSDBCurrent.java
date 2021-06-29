package com.bematechus.kds;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.bematechus.kdslib.KDSDBBase;
import com.bematechus.kdslib.KDSDataCondiment;
import com.bematechus.kdslib.KDSDataCondiments;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataItems;
import com.bematechus.kdslib.KDSDataMessage;
import com.bematechus.kdslib.KDSDataMessages;
import com.bematechus.kdslib.KDSDataModifier;
import com.bematechus.kdslib.KDSDataModifiers;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSDataSumNames;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.PrepSorts;
import com.bematechus.kdslib.ScheduleProcessOrder;
import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Vector;

/**
 * Created by Administrator on 2015/7/29 0029.
 */
public class KDSDBCurrent extends KDSDBBase {

    /**History:
     * 11:
     * 12:20161208: add table : schedule process
     * 13: Add queuemsg to orders table
     * 14: add trackerID to orders table.20170328
     * 15: add pagerID to orders table.20170515
     * 16: add cookstate field in orders table 20170517
     * 17: add sosready feild for expo double bump in Queue mode.20170525,
     *      20171030, please notice, the sosread is for queue-ready.
     * 18: add prepsort table, for preparation time mode.
     * 19: add modifiers table
     * 20: add inputmsg, and r10 -- r19
     */

    static public final int DB_VERSION = 20;//20210527
    //static public final int DB_VERSION = 6;//20160407
    static public final String DB_NAME = "current.db";
    static public final String TAG = "KDSDBCurrent";
    public enum DB_STATE {
        Normal,
        Record_Sql, //record every sql DML

    }

    private DB_STATE m_dbState = DB_STATE.Normal;


    public void setDbState(DB_STATE dbState) {
        m_dbState = dbState;
    }

    public DB_STATE getDbState() {
        return m_dbState;
    }

    public KDSDBCurrent(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String ar[] = new String[]{


                Table_Orders,
                Table_Items,
                Table_Condiments,
                Table_Messages,
                Table_Sql,
                Table_ScheduleProcess,
                Table_PrepSort,
                Table_Modifiers,
                CreateInx_Orders_Guid,
                CreateInx_Items_Guid,

                CreateInx_Condiments_Guid,

                CreateInx_Messages_Guid,
                CreateInx_PrepSort,



        };
        exeBatchSql(db, ar);
    }

    /**
     * PLEASE NOTICE:
     *  Add new table field to end of table sql definition.
     *  The use copyData to copy old data to new db.
     * @param db
     * @param oldVersion
     * @param newVersion
     */
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        changeTableName(db,"orders", "orders1");
//        changeTableName(db,"items", "items1");
//        changeTableName(db,"condiments", "condiments1");
//        changeTableName(db,"messages", "messages1");
//        changeTableName(db,"BufferedSql", "BufferedSql1");
//        if (oldVersion>11)
//            changeTableName(db, "scheduleprocess", "scheduleprocess1");
//
//        dropIndex(db, "guidorders");
//        dropIndex(db, "guiditems");
//        dropIndex(db, "guidcondiments");
//        dropIndex(db, "guidmsg");
//        dropTable(db, "prepsort");
//
//        onCreate(db);
//        ArrayList<String> arNewFieldsValuesOfOrdersTable = new ArrayList<>();
//
//        if (oldVersion >=12)
//        {
//            if (oldVersion == 17 || oldVersion == 18) //in 18, we just add a new table.
//            {
//
//            }
//            else {
//                int n = newVersion - oldVersion;
//                for (int i = 0; i < n; i++)
//                    arNewFieldsValuesOfOrdersTable.add("");
//            }
//        }
//        else if (oldVersion <=11)
//        {
//            int n = newVersion - 12; //from 12 version
//            for (int i=0; i< n; i++)
//                arNewFieldsValuesOfOrdersTable.add("");
//        }
//
//
//
//        copyData(db,"orders", "orders1", arNewFieldsValuesOfOrdersTable);
//        copyData(db,"items", "items1", null);
//        copyData(db,"condiments", "condiments1", null);
//        copyData(db,"messages", "messages1", null);
//        copyData(db,"BufferedSql", "BufferedSql1", null);
//        if (oldVersion >11)
//            copyData(db,"scheduleprocess", "scheduleprocess1", null);
//        dropTable(db, "orders1");
//        dropTable(db, "items1");
//        dropTable(db, "condiments1");
//        dropTable(db, "messages1");
//        dropTable(db, "BufferedSql1");
//        if (oldVersion >11)
//            dropTable(db, "scheduleprocess1");
//
//
//    }

    /**
     * rev:
     *  2.0.19, change it.
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        changeTableName(db,"orders", "orders1");
        changeTableName(db,"items", "items1");
        changeTableName(db,"condiments", "condiments1");
        changeTableName(db,"messages", "messages1");
        changeTableName(db,"BufferedSql", "BufferedSql1");
        changeTableName(db, "scheduleprocess", "scheduleprocess1");
        changeTableName(db, "modifiers", "modifiers1");



        dropIndex(db, "guidorders");
        dropIndex(db, "guiditems");
        dropIndex(db, "guidcondiments");
        dropIndex(db, "guidmsg");
        dropTable(db, "prepsort");
        onCreate(db);
        loadOldData(db);

    }

    public void loadOldData(SQLiteDatabase db)
    {
        copyData(db,"orders", "orders1");
        copyData(db,"items", "items1");
        copyData(db,"condiments", "condiments1");
        copyData(db,"messages", "messages1");
        copyData(db,"BufferedSql", "BufferedSql1");
        copyData(db,"scheduleprocess", "scheduleprocess1");
        copyData(db, "modifiers", "modifiers1");

        clearOldData(db);


    }

    public void clearOldData(SQLiteDatabase db)
    {
        dropTable(db, "orders1");
        dropTable(db, "items1");
        dropTable(db, "condiments1");
        dropTable(db, "messages1");
        dropTable(db, "BufferedSql1");
        dropTable(db, "scheduleprocess1");
        dropTable(db, "modifiers1");

    }



    public int getVersion() {
        return DB_VERSION;
    }

    static public KDSDBCurrent open(Context context) {
        try {


            String dbName =  KDSDBBase.getDBNameForOpen(KDSDBCurrent.DB_NAME); //use sd card path
            //
            KDSDBCurrent d = new KDSDBCurrent(context, dbName, null, KDSDBCurrent.DB_VERSION);
            return d;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;

    }

    final int ORDER_FIELDS_COUNT = 32; //it should equal field in following function.

    /**
     * see function #orderGet() and  #ordersLoadAllJustInfo
     * @return
     */
    private String getOrderFields()
    {
        String sql = "select orders.GUID, " + //0
                "orders.Name," +
                "orders.Waiter,"+
                "datetime(orders.Start) as stime, " +
                "orders.ToTbl," +
                "orders.Station," + //5
                "orders.Screen," +
                "orders.POS,"+
                "orders.OrderType, " +
                "orders.Dest," +
                "orders.CustMsg," + //10
                "orders.Parked,"+
                "orders.EvtFired," +
                "datetime(PrepStart) as ptime," +
                "orders.Status,"+
                "orders.fromprimary," + //15
                "orders.QueueMsg," +
                "orders.TrackerID," +
                "orders.PagerID,"+
                "orders.CookState," +
                "orders.SosReady," + //20
                "orders.iconidx," +  //21
                "orders.BumpedTime,"+//22
                "orders.r0," + //23
                "orders.r1," + //24
                "orders.r2," +//25
                "orders.r3," +//26
                "orders.r4," + //27
                "orders.r5," + //28
                "orders.r6," + //29
                "orders.r7," + //30
                "orders.r8," + //31, auto unpark.
                "orders.inputmsg ";//32 //input message, kp-114

        //**********************************************************************
        //Please change ORDER_FIELDS_COUNT value, after add new field!!!!!
        //Please keep last SPACE character!!!!!
        //**********************************************************************
        return sql;
    }
    /***************************************************************************
     * @param
     * @return
     */
    public KDSDataOrder orderGet(String guid) {

        if (getDB() == null) return null;

        String sql = getOrderFields() + " from orders where guid='" + guid + "'";

        Cursor c = getDB().rawQuery(sql, null);

        if (!c.moveToNext())
            return null;


        return orderGet(c);
    }


    /**
     * load order information.
     * Notes: the fields count maybe is not same "GetOrderFields".
     *        While load all orders basic information, I append  "Items count" to last field
     *        See ordersLoadAllJustInfo function.
     * rev.:
     *      2.0.19 use getString, getInt and getFloat. Prevent null field
     * @param sf
     * @return
     */
    private KDSDataOrder orderGetInfo(Cursor sf) {



        KDSDataOrder c = null;
        String orderType = getString(sf,8);
        if (orderType.equals(KDSDataOrder.ORDER_TYPE_SCHEDULE))
            c = new ScheduleProcessOrder();
        else
            c = new KDSDataOrder("");

        c.setOrderType(getString(sf,8));

        String guid = getString(sf,0);
        c.setGUID(guid);
        //t.debug_print_Duration("load order1:");
        c.setOrderName(getString(sf,1));
        // t.debug_print_Duration("load order2:");
        c.setWaiterName(getString(sf,2));
        // t.debug_print_Duration("load order3:");
        //TimeZone.getDefault().getRawOffset()
       // c.setStartTime(sf.getLong(3) * 1000 - TimeZone.getDefault().getRawOffset());
        c.setStartTime(getDate(sf, 3));
        // t.debug_print_Duration("load order4:");
        c.setToTable(getString(sf,4));
        //  t.debug_print_Duration("load order5:");
        c.setPCKDSNumber(getString(sf,5));
        //  t.debug_print_Duration("load order6:");
        c.setScreen(getInt(sf,6));
        //  t.debug_print_Duration("load order7:");
        c.setFromPOSNumber(getString(sf,7));
        //  t.debug_print_Duration("load order8:");
        c.setOrderType(getString(sf,8));
        //   t.debug_print_Duration("load order9:");
        c.setDestination(getString(sf,9));
        //  t.debug_print_Duration("load order10:");
        c.setCustomMsg(getString(sf,10));
        c.setParked((getInt(sf,11) == 1));
        //c.setIconIdx(sf.getInt(12));
        c.setOrderEvtFired(getInt(sf,12));
        //   t.debug_print_Duration("load order11:");
        //c.setPreparationStartTime(sf.getLong(13) * 1000 - TimeZone.getDefault().getRawOffset());
        c.setPreparationStartTime(getDate(sf, 13));
        c.setStatus(getInt(sf,14));
        c.setFromPrimaryOfBackup( (getInt(sf,15)==1) );
        c.setQueueMessage(getString(sf,16));
        c.setTrackerID(getString(sf,17));
        c.setPagerID(getString(sf,18));

        c.setCookState(getInt(sf,19));
        c.setQueueReady( (getInt(sf,20)==1) );
        c.setIconIdx(getInt(sf,21,-1));

        //2.0.16
        Date dtAutoBumpTime = getDate(sf, 22);
        if (dtAutoBumpTime == null)
            dtAutoBumpTime = new Date(c.getStartTime().getTime());
        c.setAutoBumpStartCountTime(dtAutoBumpTime);

        //2.0.35
        Date dtQueueStateChangeTime = getDate(sf, 23);
        if (dtQueueStateChangeTime == null)
            dtQueueStateChangeTime = new Date(c.getStartTime().getTime());
        c.setQueueStateTime(dtQueueStateChangeTime);

        //2.0.50
        //c.setSMSCustomerID( getString(sf, 24) );
        c.getCustomer().setID(getString(sf, 24));

        //c.setSMSCustomerPhone( getString(sf, 25) );
        c.getCustomer().setPhone(getString(sf, 25));

        c.setSMSLastSendState( getInt(sf, 26, KDSDataOrder.SMS_STATE_UNKNOWN) );
        //2.1.15
        c.setSmsOriginalToStations(getString(sf, 27));
        c.getCustomer().setName(getString(sf, 28));
        //kpp1-75
        c.setKDSGuid(getString(sf, 29));

        c.setHeaderFooterMessage(getString(sf, 30));

        //kp-103
        String s = getString(sf, 31);
        Date dt = KDSUtil.createInvalidDate();
        if (!s.isEmpty())
            dt = KDSUtil.convertStringToDate(s, dt);
        c.setAutoUnparkDate(dt);

        s = getString(sf,32);//kp-114 input message
        c.setInputMessage(s);

        //15, if there are 15, it should been the items count
        //see ordersLoadAllJustInfo
        if (sf.getColumnCount() > ORDER_FIELDS_COUNT) //save the items count.,for :ordersLoadAllJustInfo function
            c.setTag(getInt(sf,ORDER_FIELDS_COUNT));

        return c;

    }


    private KDSDataOrder orderGet(Cursor sf) {



        KDSDataOrder c = orderGetInfo(sf);
        orderLoadData(c);
        return c;

    }

    public boolean schedule_order_update_not_ready_qty(ScheduleProcessOrder order)
    {
        int all_not_ready = 0;
        for (int i=0; i< order.getItems().getCount(); i++) {
            all_not_ready += schedule_process_get_all_normal_item_unready_qty(order.getItems().getItem(i).getCategory(), order.getItems().getItem(i).getDescription());
        }
        return order.set_not_ready_qty(all_not_ready);
    }


    /**
     * The items fields while load data.
     * History:
     *  2.0.8: add r0 for timer delay.
     *  2.0.9: use r1 as parent item guid, this is line items mode.
     *  2.0.47: category priority r2
     *  KPP1-53 use r3 to save transfered from station id.
     *  KPP1-64, use r4 as item_bump_guid
     */
    static private String ITEMS_FIELDS = "GUID,Name,Description,Qty,Category,BG,FG,Grp,Marked,DeleteByRemote,LocalBumped,BumpedStations," +
                                            "ToStations,Ready,Hiden,QtyChanged,ItemType,ItemDelay,PreparationTime,BuildCard,TrainingVideo," +
                                            "SumTransEnable,SumTrans,r0,r1,r2,r3,r4,r5,r6 ";

    private KDSDataItems itemsGet(String orderGUID)// int nOrderID)
    {

        KDSDataItems items = new KDSDataItems();
        if (getDB() == null) return items;

        String sql = String.format("select %s from items where orderguid='%s' order by id",ITEMS_FIELDS, orderGUID);
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {

            KDSDataItem item = itemGet(c);

            item.setOrderGUID(orderGUID);
            items.addComponent(item);

        }
        c.close();


        return items;


    }

    public KDSDataItem itemGet(String guid)//int nID)
    {

        String sql = String.format("select %s from items where guid='%s'", ITEMS_FIELDS, guid);

        if (getDB() == null) return new KDSDataItem();

        Cursor c = getDB().rawQuery(sql, null);

        if (!c.moveToNext())
            return null;
        return itemGet(c);

    }


    /**
     * Load data from cursor.
     * The fields definition see ITEMS_FIELDS define.
     * @param sf
     * @return
     *
     * History:
     *  2.0.8: use r0 as timer delay
     */
    private KDSDataItem itemGetInfo(Cursor sf) {

        String guid = getString(sf,0);
        //d.debug_print_Duration("load item0");
        KDSDataItem c = new KDSDataItem("", guid);

        c.setItemName(getString(sf,1));
        c.setDescription(getString(sf,2));
        c.setQty(getFloat(sf,3));
        c.setCategory(getString(sf,4));
        c.setBG(getInt(sf,5));
        c.setFG(getInt(sf,6));
        // d.debug_print_Duration("load item2");
        c.setAddOnGroup(getInt(sf,7));
        c.setMarked(getInt(sf,8));
        c.setDeleteByRemoteCommand((getInt(sf,9) != 0));
        c.setLocalBumped((getInt(sf,10) != 0));
        c.setBumpedStationString(getString(sf,11));
        c.setToStationsString(getString(sf,12));
        c.setReady(getInt(sf,13));
        c.setHidden((getInt(sf,14) != 0));

        c.setChangedQty(getFloat(sf,15));
        c.setItemType(KDSDataItem.ITEM_TYPE.values()[getInt(sf,16)]);
        c.setItemDelay(getFloat(sf,17));
        c.setPreparationTime(getFloat(sf,18));
        c.setBuildCard(getString(sf,19));
        c.setTrainingVideo(getString(sf,20));
        c.setSumNamesEnabled(getInt(sf,21)!=0);
        c.setSumNames(KDSDataSumNames.parseString(getString(sf,22)));
        //2.0.8
        String s = getString(sf,23);
        if (s ==null || s.isEmpty())
            s = "0";
        c.setTimerDelay(KDSUtil.convertStringToInt(s, 0));
        //2.0.9

        s = getString(sf,24);
        if (s == null)//2.0.17
            s = "";
        c.setParentItemGuid(s);

        //2.0.47
        s = getString(sf,25);
        if (s ==null || s.isEmpty())
            s = "-1";
        c.setCategoryPriority(KDSUtil.convertStringToInt(s, -1));

        //KPP1-53
        s = getString(sf,26);
        if (s ==null || s.isEmpty())
            s = "";
        c.setTransferedFromStationID(s);

        //KPP1-64
        s = getString(sf,27);
        if (s ==null || s.isEmpty())
            s = "";
        c.setItemBumpGuid(s);

        int n = getInt(sf, 28);
        c.setPrintable((n !=0));

        n = getInt(sf, 29);//printed when bump
        c.setPrinted((n!=0));

        return c;
    }

    private KDSDataItem itemGet(Cursor sf) {





        KDSDataItem c = itemGetInfo(sf);
        //get messages
        c.setPreModifiers(messagesItemGet(c.getGUID()));
        c.setCondiments(condimentsGet(c.getGUID()));
        c.setModifiers(modifiersGet(c.getGUID()));

        return c;
    }

    private KDSDataMessages messagesItemGet(String itemGUID)//int nItemID)
    {
        return messagesGet(KDSDataMessage.FOR_Item, itemGUID);

    }

    /**
     * load component message
     * @param nMessageType
     * @param guid
     *      The component guid.
     * @return
     */
    private KDSDataMessages messagesGet(int nMessageType, String guid)// int nID)
    {

        KDSDataMessages messages = new KDSDataMessages();
        if (getDB() == null) return messages;

        String sql = String.format("select GUID,Description,bg, fg from messages where ObjGUID='%s' and ObjType=%d  order by id", guid, nMessageType);

       // d.debug_print_Duration("load msgstart");
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {

            KDSDataMessage msg = messageGet(c);
            msg.setComponentGUID(guid);
            msg.setForComponentType(nMessageType);
            messages.addComponent(msg);
        }
        c.close();

        return messages;


    }

    private KDSDataMessages messagesOrderGet(String orderGUID)
    {
        return messagesGet(KDSDataMessage.FOR_Order, orderGUID);


    }

    private KDSDataMessages messagesCondimentGet(String condimentGUID)
    {
        return messagesGet(KDSDataMessage.FOR_Condiment, condimentGUID);

    }

    private String getCondimentFieldSql()
    {

        return "GUID,Name,Description,BG, FG,Qty ";//kpp1-414 add qty
    }
    public KDSDataCondiments condimentsGet(String itemGUID)// int nItemID)
    {

        KDSDataCondiments condiments = new KDSDataCondiments();
        if (getDB() == null) return condiments;

        //Rev.:
        // kpp1-414, add qty
        String sql = String.format("select GUID,Name,Description,BG,FG,Qty from condiments where itemguid='%s' order by id", itemGUID);
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {

            KDSDataCondiment condiment = condimentGet(c);
            condiment.setItemGUID(itemGUID);
            condiments.addComponent(condiment);
        }
        c.close();
        return condiments;
    }


    private KDSDataCondiment condimentGet(Cursor sf) {

        String guid = getString(sf,0);
        KDSDataCondiment c = new KDSDataCondiment("", guid);

        c.setCondimentName(getString(sf,1));
        c.setDescription(getString(sf,2));
        c.setBG(getInt(sf,3));
        c.setFG(getInt(sf,4));
        c.setQty(getFloat(sf, 5));//kpp1-414

        //<<<<<<IMPORTANT>>>>>> Now don't support messages for condiments.
       // c.setMessages(this.messagesGet(KDSDataMessage.FOR_Condiment, guid));//nID));
        return c;
    }




    private KDSDataMessage messageGet(Cursor sf) {
        KDSDataMessage c = new KDSDataMessage("", "");
        c.setGUID(getString(sf,0));
        c.setMessage(getString(sf,1));
        c.setBG(getInt(sf,2));
        c.setFG(getInt(sf,3));
        return c;
    }

    public boolean orderAddInfo(KDSDataOrder order) {

        String sql = order.sqlAddNew("orders");
        boolean bTransactionByMe = false;
        try {
            bTransactionByMe = startTransaction();

            executeDML(sql);
            KDSDataMessages messages = order.getOrderMessages();
            messagesAdd(messages);


            return true;
        } catch (Exception e) {
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + e.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e );
            return false;
        } finally {
            this.finishTransaction(bTransactionByMe);
            updateDbTimeStamp();
        }

    }


    public boolean itemAdd(KDSDataItem item) {

        String sql = item.sqlAddNew("items");
        boolean bTransactionByMe = false;
        try {
            bTransactionByMe = this.startTransaction();
            executeDML(sql);
            KDSDataCondiments condiments = item.getCondiments();

            condimentsAdd(condiments);//, stmt);
            modifiersAdd(item.getModifiers());

            //add messages
            KDSDataMessages messages = item.getPreModifiers();
            messagesAdd(messages);
            //this.finishTransaction(bTransactionByMe);
            return true;
        } catch (Exception e) {
            //KDSLog.e(TAG, KDSLog._FUNCLINE_() + e.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e );
            return false;
        } finally {
            this.finishTransaction(bTransactionByMe);
        }

    }

    /***************************************************************************
     * save data
     */
    public boolean orderAdd(KDSDataOrder order) {
        boolean bTransactionByMe = false;
        if (getDB() == null) return false;

        try {

            if (!orderAddInfo(order)) {
                //this.finishTransaction(bTransactionByMe);
                //getDB().endTransaction();
                KDSLog.e(TAG, KDSLog._FUNCLINE_() + " Can not save order to database");
                return false;
            }
            bTransactionByMe = this.startTransaction();
            KDSDataItems items = order.getItems();
            itemsAdd(items);
            //this.finishTransaction(bTransactionByMe);
            return true;
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        } finally {
            if (bTransactionByMe)
                this.finishTransaction(bTransactionByMe);
            updateDbTimeStamp();

        }


    }

    public boolean parkOrderAdd(KDSDataOrder order) {
        if (getDB() == null) return false;
        if (!this.orderAdd(order))
            return false;
        String sql = "update orders set parked=1 where guid='" + order.getGUID() + "'";
        this.getDB().execSQL(sql);
        updateDbTimeStamp();
        return true;
    }


    /**
     * append order items to given order,
     *
     * @param orderExisted The order existed
     * @param order        The new append items
     * @return the addon items count
     */
    public int orderAppendAddon(KDSDataOrder orderExisted, KDSDataOrder order) {
        if (!itemsAdd(order.getItems()))
            return 0;
        updateDbTimeStamp();
        return order.getItems().getCount();
    }

    public int orderAppendAddon(KDSDataOrder orderExisted, KDSDataItem item) {

        KDSDataItems items = new KDSDataItems();
        items.addComponent(item);

        if (!itemsAdd(items))
            return 0;
        updateDbTimeStamp();
        return 1;
    }


    private boolean itemsAdd(KDSDataItems items) {
        int ncount = items.getCount();
        String sql = "";
        try {
            for (int i = 0; i < ncount; i++) {
                KDSDataItem item = (KDSDataItem) items.getComponent(i);
                sql = item.sqlAddNew("items");

                if (!this.executeDML(sql))
                    break;
                //add condiments
                KDSDataCondiments condiments = item.getCondiments();
                condimentsAdd(condiments);//, stmt);

                modifiersAdd(item.getModifiers());
                //add messages
                KDSDataMessages messages = item.getPreModifiers();
                messagesAdd(messages);//, stmt);

            }
            return true;
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }

    private boolean condimentsAdd(KDSDataCondiments condiments) {
        int ncount = condiments.getCount();
        String sql = "";
        try {
            for (int i = 0; i < ncount; i++) {
                KDSDataCondiment condiment = (KDSDataCondiment) condiments.getComponent(i);
                sql = condiment.sqlAddNew("condiments");
                if (!this.executeDML(sql)) return false;

                messagesAdd(condiment.getMessages());

            }
            return true;
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }


    private boolean messagesAdd(KDSDataMessages messages) {
        int ncount = messages.getCount();
        String sql = "";
        try {
            for (int i = 0; i < ncount; i++) {
                KDSDataMessage message = (KDSDataMessage) messages.getComponent(i);
                sql = message.sqlAddNew();
                if (!this.executeDML(sql))
                    return false;

            }
            return true;
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            updateDbTimeStamp();
            return false;
        }
    }

    public boolean orderInfoModify(KDSDataOrder order) {
        String sql = order.sqlModify();
        boolean b = this.executeDML(sql);
        if (b)
            updateDbTimeStamp();
        return b;
    }

    public boolean itemUpdate(KDSDataItem item) {
        String sql = item.sqlUpdate();
        boolean b = this.executeDML(sql);
        if (b)
            updateDbTimeStamp();
        return b;
    }

    public boolean itemModify(KDSDataItem item) {
        String sql = item.sqlModify();
        boolean b= this.executeDML(sql);
        if (b)
            updateDbTimeStamp();
        return b;
    }

    public boolean condimentUpdate(KDSDataCondiment condiment) {
        String sql = condiment.sqlUpdate();
        boolean b= this.executeDML(sql);
        if (b)
            updateDbTimeStamp();
        return b;
    }

    public boolean condimentAdd(KDSDataCondiment condiment) {
        String sql = condiment.sqlAddNew("condiments");
        boolean b= this.executeDML(sql);
        if (b)
            updateDbTimeStamp();
        return b;
    }

    public boolean condimentDelete(KDSDataCondiment condiment) {
        String sql = condiment.sqlDelete();
        boolean b= this.executeDML(sql);
        if (b)
            updateDbTimeStamp();
        return b;
    }

    public boolean orderDelete(String guid)
    {
        return orderDeleteQuick(guid);
//
//        if (getDB() == null) return false;
//
//        String sql = KDSDataOrder.sqlDelete("orders", guid);
//        if (!this.executeDML(sql))
//            return false;
//
//        sql = "select guid from items where orderguid='" + guid + "'";// Common.KDSUtil.ConvertIntToString(nID);
//
//        Cursor c = getDB().rawQuery(sql, null);
//
//        while (c.moveToNext()) {
//            String itemguid = getString(c,0);
//            deleteItem(itemguid);
//        }
//        c.close();
//        //remove order messages.
//        sql = "delete from messages where ObjType=0 and ObjGUID='" + guid + "'";
//        if (!this.executeDML(sql))
//            return false;
////        //remove modifiers
////        sql = String.format("delete from modifiers where modifiers.ItemGUID in (select guid from items where items.orderguid='%s')", guid);
////        if (!this.executeDML(sql)) return false;
////
////        //remove condiments
////        sql = String.format("delete from condiments where condiments.itemguid in (select guid from items where items.orderguid='%s')", guid);
////        if (!this.executeDML(sql))
////            return false;
////        //remove condiments messages
////        sql = String.format("delete from messages where ObjType=1 and messages.ObjGUID in (select guid from items where items.orderguid='%s')", guid);
////        if (!this.executeDML(sql))
////            return false;
////        //remove items messages
////        sql = String.format("delete from messages where ObjType=1 and messages.ObjGUID in (select guid from items where items.orderguid='%s')", guid);
////        if (!this.executeDML(sql))
////            return false;
////        //remove items
////        sql = String.format("delete from items where items.orderguid='%s'", guid);
////        if (!this.executeDML(sql))
////            return false;
//
//        updateDbTimeStamp();
//
//        prep_remove(guid);
//
//        return true;
    }

    /**
     * remove the visible orders
     * @param arOrderNames
     * @return
     */
    public boolean orderDelete(ArrayList<String> arOrderNames)
    {

        int ncount = arOrderNames.size();
        if (ncount <=0) return true;
        boolean bstartbyme = this.startTransaction();
        for (int i=0; i< ncount ;i++) {
           String guid = orderGetGuidFromName(arOrderNames.get(i));
            if (guid.isEmpty()) continue;
            orderDelete(guid);
        }
        this.finishTransaction(bstartbyme);

        return true;
    }

    /**
     * Set the removed mark, don't delete it from database.
     *
     * @param guid
     * @return
     */
    public boolean orderRemove(String guid)//int nID)
    {
        if (getDB() == null) return false;
        String sql = KDSDataOrder.sqlDelete("orders", guid);
        if (!this.executeDML(sql))
            return false;
        sql = "select guid from items where orderguid='" + guid + "'";// Common.KDSUtil.ConvertIntToString(nID);

        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {
            String itemguid = getString(c ,0);
            deleteItem(itemguid);
        }
        c.close();
        //remove order messages.
        sql = "delete from messages where ObjType=0 and ObjGUID='" + guid + "'";
        if (!this.executeDML(sql))
            return false;
        updateDbTimeStamp();

        return true;
    }


    public boolean deleteItem(String itemGUID)//int nID)
    {
        if (getDB() == null) return false;

        String sql = KDSDataItem.sqlDelete("items", itemGUID);
        if (!this.executeDML(sql)) return false;
        sql = "delete from messages where ObjType=1 and ObjGUID='" + itemGUID + "'";// Common.KDSUtil.ConvertIntToString(nID);
        if (!this.executeDML(sql)) return false;

        sql = "select guid from condiments where itemguid='" + itemGUID + "'";// Common.KDSUtil.ConvertIntToString(nID);

        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {

            String condimentguid = getString(c,0);
            deleteCondiment(condimentguid);
        }
        c.close();
        sql = "delete from condiments where ItemGUID='" + itemGUID + "'";// Common.KDSUtil.ConvertIntToString(nID);
        if (!this.executeDML(sql)) return false;

        sql = "delete from modifiers where ItemGUID='" + itemGUID + "'";// Common.KDSUtil.ConvertIntToString(nID);
        if (!this.executeDML(sql)) return false;

        return true;
    }

    public boolean deleteCondiment(String condimentGUID) {
        String sql = "delete from condiments where guid='" + condimentGUID + "'";
        if (!this.executeDML(sql)) return false;
        sql = "delete from messages where ObjType=2 and ObjGUID='" + condimentGUID + "'";//
        if (!this.executeDML(sql)) return false;
        return true;
    }



    public ArrayList ordersGetAllGuid() {
        String sql = "select guid from orders";
        ArrayList ar = new ArrayList();
        if (getDB() == null) return ar;
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {
            String guid = getString(c,0);
            ar.add(guid);
        }
        c.close();
        return ar;
    }

    public String orderGetGuidFromName(String orderName) {
        String sql = "select guid from orders where name='" + orderName + "'";

        if (getDB() == null) return "";

        Cursor c = getDB().rawQuery(sql, null);
        String guid = "";
        if (c.moveToNext()) {
            guid = getString(c,0);

        }
        c.close();
        return guid;
    }

    public String orderGetUnbumpedGuidFromName(String orderName) {
        if (getDB() == null) return "";
        String sql = "select guid from orders where bumped<>1 and  name='" + orderName + "'";
        ArrayList ar = new ArrayList();

        Cursor c = getDB().rawQuery(sql, null);
        String guid = "";
        if (c.moveToNext()) {
            guid = getString(c,0);

        }
        c.close();
        return guid;
    }
    /**
     * @param orderName
     * @return if the name is not unique, just return the first one.
     */
    public String orderGetBumpedGuidFromName(String orderName) {
        String sql = "select guid from orders where bumped=1 and name='" + orderName + "'";
        ArrayList ar = new ArrayList();

        if (getDB() == null) return "";

        Cursor c = getDB().rawQuery(sql, null);
        String guid = "";
        if (c.moveToNext()) {
            guid = getString(c,0);

        }
        c.close();
        return guid;
    }

    public KDSDataOrders ordersLoadAllJustInfo(String stationID, int nScreen, boolean bParked) {
        ArrayList<String> ar = new ArrayList<>();
        ar.add(stationID);
        return ordersLoadAllJustInfo(ar, nScreen, bParked);
    }

    /**
     * @param nScreen The screen number, same as Screen enum definition.
     * @return
     */
    public KDSDataOrders ordersLoadAll(String stationID, int nScreen, boolean bParked) {
        ArrayList<String> ar = new ArrayList<>();
        ar.add(stationID);
        return ordersLoadAll(ar, nScreen, bParked);
    }

    public ArrayList<String> orderGetAllActiveGUID(KDSDBCurrent db) {
        ArrayList<String> ar = new ArrayList<String>();
        if (db.getDB() == null) return ar;

        String sql = "select guid from orders where bumped<>1 and Parked=0";
        Cursor c = db.getDB().rawQuery(sql, null);

        while (c.moveToNext()) {

            String guid = getString(c,0);
            ar.add(guid);


        }
        c.close();
        return ar;
    }



    public ArrayList<String> itemGetAllItemsGUID(KDSDBCurrent db, String orderGUID) {
        ArrayList<String> ar = new ArrayList<String>();
        if (db.getDB() == null) return ar;
        String sql = "select guid from items where orderguid='" + orderGUID + "'";

        Cursor c = db.getDB().rawQuery(sql, null);

        while (c.moveToNext()) {
            //int nID = r.getRow(i).getInt(0);
            String guid = getString(c,0);
            ar.add(guid);


        }
        c.close();
        return ar;
    }

    /**
     * @param arStationID It is useless, it is for future.
     * @param nScreen
     * @param bParked
     * @return
     */
    public KDSDataOrders ordersLoadAll(ArrayList<String> arStationID, int nScreen, boolean bParked) {

        //TimeDog t = new TimeDog();
        KDSDataOrders orders = ordersLoadAllJustInfo(arStationID, nScreen, bParked);
        //t.debug_print_Duration("load after info");
        if (orders.getCount()<=0) return orders;
        ordersLoadAllData(orders);
        //t.debug_print_Duration("load end");
        return orders;


    }

    /**
     * Just load all order info first, then use thread to load items.
     * @param arStationID It is useless, it is for future.
     * @param nScreen
     *  -1: all order
     * @param bParked
     * @return
     */
    public KDSDataOrders ordersLoadAllJustInfo(ArrayList<String> arStationID, int nScreen, boolean bParked) {

        KDSDataOrders orders = new KDSDataOrders();
        if (getDB() == null) return orders;

        String sql = String.format("%s,count(*) as itemsc from orders left join items on orders.guid=items.orderguid where orders.bumped<>1 and orders.screen=%d and orders.parked%s  group by orders.guid order by orders.id", getOrderFields(), nScreen, bParked ? "<>0" : "=0");
        if (nScreen <0)
            sql = String.format("%s,count(*) as itemsc from orders left join items on orders.guid=items.orderguid where orders.bumped<>1 and orders.parked%s  group by orders.guid order by orders.id", getOrderFields(), bParked ? "<>0" : "=0");

        Cursor c = getDB().rawQuery(sql, null);
        while (c.moveToNext()) {

            orders.addOrderWithoutSort(orderGetInfo(c));
        }
        c.close();
        return orders;


    }

    public boolean ordersLoadAllData(KDSDataOrders orders)
    {
        for (int i=0; i< orders.getCount(); i++)
        {
            KDSDataOrder order = orders.get(i);
            orderLoadData(order);
        }
        return true;
    }

    public boolean orderLoadData(KDSDataOrder order)
    {
        order.setOrderMessages(messagesOrderGet(order.getGUID()));
        //t.debug_print_Duration("load ordermsg");
        order.setItems(itemsGet(order.getGUID()));
        //for schedule
        if (order.getOrderType().equals(KDSDataOrder.ORDER_TYPE_SCHEDULE))
        {//load schedule ready qty

            for (int i=0; i< order.getItems().getCount(); i++) {
                int n = (int)schedule_process_get_item_ready_qty(order.getItems().getItem(i).getGUID());
                order.getItems().getItem(i).setScheduleProcessReadyQty(n);
            }
            schedule_order_update_not_ready_qty((ScheduleProcessOrder) order);
        }

        order.prep_set_sorts( prep_get_sort_items(order.getGUID()));

        return true;
    }

//    static final int PARTIAL_COUNT = 20;
//    public boolean ordersLoadPartialData(KDSDataOrders orders, int nFromIndex)
//    {
//
//        for (int i=nFromIndex; i< nFromIndex + PARTIAL_COUNT; i++)
//        {
//            if (i >= orders.getCount()) return true;
//            KDSDataOrder order = orders.get(i);
//            if (order.getItems().getCount()>0) continue;
//
//            order.setOrderMessages(messagesOrderGet(order.getGUID()));
//            order.setItems(itemsGet(order.getGUID()));
//
//        }
//        return true;
//    }
//    public boolean ordersLoadPartialData(KDSDataOrders orders, String strFromOrderGuid)
//    {
//        int nFromIndex = 0;
//        for (int i=0; i< orders.getCount(); i++)
//        {
//            if (orders.get(i).getGUID().equals(strFromOrderGuid))
//                nFromIndex = i;
//        }
//        ordersLoadPartialData(orders, nFromIndex);
//
//        return true;
//    }

//    /**
//     * Update database item "marked" tag value.
//     *
//     * @param item the item will been updated.
//     */
//    public void itemSetMarked(KDSDataItem item) {
//        String sql = "update items set marked=" + KDSUtil.convertIntToString(item.getMarked());
//        sql += " where guid='";
//        sql += item.getGUID() + "'";// KDSUtil.ConvertIntToString(item.getID());
//        this.executeDML(sql);
//        updateDbTimeStamp();
//    }

    public void itemSetLocalBumped(KDSDataItem item) {

        String sql = "update items set LocalBumped=1,dbtimestamp=datetime('now','localtime')";
        if (!item.getLocalBumped())
            sql = "update items set LocalBumped=0,dbtimestamp=datetime('now','localtime')";

        sql += " where guid='";
        sql += item.getGUID() + "'";
        this.executeDML(sql);
        updateDbTimeStamp();

    }


    public void orderSetParked(String orderGUID, boolean bParked) {
        String sql = "update orders set parked=0";
        if (bParked)
            sql = "update orders set parked=1";
        sql += " where guid='";
        sql += orderGUID + "'";//
        this.executeDML(sql);
        updateDbTimeStamp();
    }


    public void orderSetTrackerID(String orderGUID, String trackerID) {
        String sql = String.format("update orders set trackerid='%s' where guid='%s'", trackerID, orderGUID);
        this.executeDML(sql);
        updateDbTimeStamp();
    }

    public void orderSetCookState(String orderGUID, KDSDataOrder.CookState state) {
        String sql = String.format("update orders set cookstate=%d where guid='%s'", state.ordinal(), orderGUID);
        this.executeDML(sql);
        updateDbTimeStamp();
    }

    /**
     * As old design reason, I still use the sosready for "queueready"
     * Change database is too huge work
     * @param orderGUID
     * @param bReady
     */
    public void orderSetQueueReady(String orderGUID, boolean bReady) {
        String sql = String.format("update orders set sosready=%d where guid='%s'", (bReady?1:0), orderGUID);
        this.executeDML(sql);
        updateDbTimeStamp();
        orderUpdateQueueStateTime(orderGUID);
    }

//    public void orderSetPagerID(String orderGUID, String pagerID) {
//        String sql = String.format("update orders set pagerid='%s' where guid='%s'", pagerID, orderGUID);
//        this.executeDML(sql);
//        updateDbTimeStamp();
//    }


    public void orderSetBumped(String orderGUID, boolean bBumped) {

        String sql = "update orders set bumped=0, bumpedtime='" + KDSUtil.getCurrentDateString() + "'";
        if (bBumped)
            sql = "update orders set bumped=1, bumpedtime='" + KDSUtil.getCurrentDateString() + "'";
        sql += " where guid='";
        sql += orderGUID + "'";//KDSUtil.ConvertIntToString(orderID);
        this.executeDML(sql);
        updateDbTimeStamp();
        if (bBumped)
        {
            //restore the order double bump state
            orderSetQueueReady(orderGUID, false);
        }
    }

    public boolean orderGetBumped(String orderGUID) {

        String sql = "select bumped from orders where guid='" + orderGUID +"'";

        int n= this.executeOneValue(sql);
        return (n==1);


    }
    public Date orderGetBumpedTime(String orderGUID) {

        String sql = "select bumpedtime from orders where guid='" + orderGUID +"'";

        Date dtReturn = KDSUtil.createInvalidDate();
        if (getDB() == null) return dtReturn;

        Cursor c = getDB().rawQuery(sql, null);
        String guid = "";
        if (c.moveToNext()) {
            dtReturn = getDate(c, 0);


        }
        c.close();

        return dtReturn;

    }

    public void orderSetBumpedTimeToNow(String orderGUID, Date dt) {

        String sql =String.format("update orders set bumpedtime='%s' where guid='%s", KDSUtil.convertDateToString(dt),  orderGUID );

        getDB().execSQL(sql);

    }

    /**
     * update the item remote bumped stations string.
     * This is for expeditor stations
     *
     * @param item
     */
    public void itemSetRemoteBumpedStations(KDSDataItem item) {
        String sql = "update items set BumpedStations='" + item.getBumpedStationsString() + "'";
        sql += " where guid='" + item.getGUID() + "'";
        this.executeDML(sql);
        updateDbTimeStamp();

    }

    public void itemSetRemovedByXmlCommand(KDSDataItem item) {
        String sql ="";

        sql =String.format("update items set DeleteByRemote=%d where guid='%s'", item.getDeleteByRemoteCommand()?1:0, item.getGUID() );

        this.executeDML(sql);
        updateDbTimeStamp();
    }

    public void clear() {

        boolean bStartedByMe = this.startTransaction();
        String sql = "delete from orders";
        this.executeDML(sql);
        sql = "delete from items";
        this.executeDML(sql);
        sql = "delete from condiments";
        this.executeDML(sql);
        sql = "delete from messages";
        this.executeDML(sql);
        sql = "delete from bufferedsql";
        this.executeDML(sql);
        sql = "delete from scheduleprocess";
        this.executeDML(sql);
        sql = "delete from prepsort";//preparation time mode
        this.executeDML(sql);
        sql = "delete from modifiers";//preparation time mode
        this.executeDML(sql);
        this.finishTransaction(bStartedByMe);


    }

//    public void clearStation(int nStation) {
//        String sql = "delete from orders";
//        this.executeDML(sql);
//        sql = "delete from items";
//        this.executeDML(sql);
//        sql = "delete from condiments";
//        this.executeDML(sql);
//        sql = "delete from messages";
//        this.executeDML(sql);
//
//    }

//    private final int BUMPED_HOURS = 1;
//
//    public KDSDataOrders ordersLoadRecentBumped(int nScreen) {
//        KDSDataOrders orders = new KDSDataOrders();
//        if (getDB() == null) return orders;
//
//        //String sql = "select guid from orders where bumped=1 and Parked=0 and screen=" +KDSUtil.convertIntToString(nScreen);
//        String sql = "select guid from orders where bumped=1 and screen=" + KDSUtil.convertIntToString(nScreen);
//        sql += " and BumpedTime>='" + KDSUtil.getSomeHoursAgoTimeString(BUMPED_HOURS) + "'";
//
//
////        if (bParked)
////            sql = "select guid from orders where bumped<>1 and Parked<>0 and screen=" +KDSUtil.convertIntToString(nScreen);
////        sql += " and Station='" +KDSUtil.convertIntToString( nStation )+"'";
//
//
//        //debug
//        //sql = "select guid from orders";
//        Cursor c = getDB().rawQuery(sql, null);
//
//        while (c.moveToNext()) {
//            //int nID = r.getRow(i).getInt(0);
//            String guid = c.getString(0);
//            KDSDataOrder order = this.orderGet(guid);
//            orders.addComponent(order);
//        }
//        c.close();
//        return orders;
//
//
//    }


//    public LinkedHashMap<String, String> ordersLoadRecentBumpedOrderName(String stationID, int nScreen) {
//        ArrayList<String> ar = new ArrayList<>();
//        ar.add(stationID);
//        return ordersLoadRecentBumpedOrderName(ar, nScreen);
//
//
//    }

//    protected String buildStationsCondition(String sql, ArrayList<String> arStationID) {
//        sql += " (";
//        for (int i = 0; i < arStationID.size(); i++) {
//            if (i == 0)
//                sql += " Station='" + arStationID.get(i) + "'";//KDSUtil.convertIntToString( nStation )+"'";
//            else
//                sql += " or Station='" + arStationID.get(i) + "'";
//        }
//        sql += ") ";
//        return sql;
//    }

    public LinkedHashMap<String, String> ordersLoadRecentBumpedOrderName(ArrayList<String> arStationID, int nScreen) {

        String sql = "select guid, name,BumpedTime from orders where bumped=1 and screen=" + KDSUtil.convertIntToString(nScreen);
        sql += " order by BumpedTime desc";
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        if (getDB() == null) return m;

        Cursor c = getDB().rawQuery(sql, null);


        while (c.moveToNext()) {

            String guid = getString(c,0);
            String name = getString(c,1);
            String t = getString(c,2);
            name +=( " (" + t +")");

            m.put(guid, name);

        }
        c.close();
        return m;
    }

    /**
     * In lineitems, if one of item is bumped, we will show it.
     * @param arStationID
     * @param nScreen
     * @return
     */
    public LinkedHashMap<String, String> ordersLoadRecentBumpedOrderNameForLineItems(ArrayList<String> arStationID, int nScreen) {

        //String sql = "select guid, name,BumpedTime from orders where  bumped=1 and  screen=" + KDSUtil.convertIntToString(nScreen);
        //sql += " order by BumpedTime desc";
        String sql = "select orders.guid, orders.name,orders.BumpedTime from orders where orders.screen=" +  KDSUtil.convertIntToString(nScreen);
        sql += " and orders.bumped=1 or ";
        sql += " (EXISTS (SELECT orders.guid FROM items WHERE orders.bumped<>1 and orders.guid=items.orderguid and items.localbumped=1))";
        sql += " group by orders.guid";
        sql += " order by orders.BumpedTime desc";
        sql += " limit 0,100";


        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        if (getDB() == null) return m;

        Cursor c = getDB().rawQuery(sql, null);


        while (c.moveToNext()) {

            String guid = getString(c,0);
            String name = getString(c,1);
            String t = getString(c,2);
            name +=( " (" + t +")");

            m.put(guid, name);

        }
        c.close();
        return m;
    }

    public String ordersLoadRecentBumpedLastOrder(int nScreen) {


        if (getDB() == null) return "";

        String sql = "select guid from orders where bumped=1 and screen=" + KDSUtil.convertIntToString(nScreen);
        sql += " order by BumpedTime desc limit 1";

        Cursor c = getDB().rawQuery(sql, null);
        String guid = "";
        while (c.moveToNext()) {

            guid = getString(c,0);
        }
        c.close();
        return guid;
    }

    public String lineItemsLoadRecentBumpedLastItem(int nScreen) {


        if (getDB() == null) return "";
        String sql = String .format("select items.guid from items,orders where items.LocalBumped=1 and orders.screen=%d and orders.guid=items.orderguid " +
                                    "order by items.DBTimeStamp desc limit 1",
                                    nScreen);

        Cursor c = getDB().rawQuery(sql, null);
        String guid = "";
        while (c.moveToNext()) {

            guid = getString(c,0);
        }
        c.close();
        return guid;
    }

    public String lineItemsGetOrderGuidFromItemGuid(String itemGuid) {


        if (getDB() == null) return "";
        String sql = String .format("select items.orderguid from items where guid='%s'",
                                    itemGuid);


        Cursor c = getDB().rawQuery(sql, null);
        String guid = "";
        while (c.moveToNext()) {

            guid = getString(c,0);
        }
        c.close();
        return guid;
    }
    /**
     *
     * @param arStationID
     *  Unused
     * @param nScreen
     * @return
     */
    public LinkedHashMap<String, String> ordersLoadParkedOrdersName(ArrayList<String> arStationID, int nScreen) {

        String sql = "select guid, name from orders where parked=1 and screen=" + KDSUtil.convertIntToString(nScreen);

        LinkedHashMap<String, String> m = new LinkedHashMap<>();

        if (getDB() == null) return m;

        Cursor c = getDB().rawQuery(sql, null);


        while (c.moveToNext()) {

            String guid = getString(c,0);
            String name = getString(c,1);
            m.put(guid, name);
        }
        c.close();
        return m;
    }

    public int getParkedCount(int nScreen)
    {
        String sql = "select count(*) as c from orders where parked=1 and screen=" + KDSUtil.convertIntToString(nScreen);
        return this.executeOneValue(sql);

    }

    /********** SUMMARY **************************************************************/


    /************************************************************************/
    public ArrayList<KDSSummaryItem> summaryItems(String stationID, int nUser, boolean bSummaryStation, boolean bCheckCondiments, boolean bAscend)//, boolean bEnableSummaryTranslation)
    {
        //TimeDog t = new TimeDog();
        ArrayList<KDSSummaryItem> arSums = new ArrayList<>();
        if (getDB() == null) return arSums;


        if (bCheckCondiments) {
            //return summaryItemsWithCondiments(stationID, nUser, arValidOrderGUID, bAscend);//bEnableSummaryTranslation);
            //kpp1-415
            return summaryItemsWithCondiments(stationID, nUser, bSummaryStation, bAscend);//bEnableSummaryTranslation);
        }
        String sql = "";

        String s = "";

        //20160712 CHANGED, add qtychanged field.
        sql = String.format( "select a.description, sum(a.qty+ifnull(a.qtychanged,0)) as s from items as a, orders as b where LocalBumped=0 and hiden=0 and b.parked<>1 and b.bumped<>1 and a.orderguid=b.guid and screen=%d group by description order by s DESC",
                            nUser);
        if (bSummaryStation)
            sql = "select description, sum(qty+ifnull(qtychanged,0)) as s from items where LocalBumped=0 and hiden=0 and bumpedstations='' group by description order by s DESC";
            //sql = String.format( "select a.description, sum(a.qty+ifnull(a.qtychanged,0)) as s from items as a, orders as b where LocalBumped=0 and hiden=0 and b.parked<>1 and b.bumped<>1 and a.orderguid=b.guid and screen=%d and a.bumpedstations='' group by description order by s DESC",
//                    nUser);

        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {

            KDSSummaryItem sumItem = new KDSSummaryItem();
            sumItem.setDescription( getString(c,0));

            sumItem.setQty(getFloat(c,1));
            arSums.add(sumItem);

        }
        c.close();

        summaryItemsSortByQty(arSums, !bAscend);
        summaryRebuildNamesAndQty(arSums);
        //t.debug_print_Duration("load sumend");
        return arSums;

    }

    /************************************************************************/
    public ArrayList<KDSSummaryItem> summaryItemsAdvanced(String stationID, int nUser, ArrayList<String> arValidOrderGUID, boolean bCheckCondiments, boolean bAscend, ArrayList<String> arItemsFilter, boolean bCheckSmartQty)//, boolean bEnableSummaryTranslation)
    {
        //TimeDog t = new TimeDog();
        ArrayList<KDSSummaryItem> arSums = new ArrayList<>();
        if (getDB() == null) return arSums;
        String sql = "";

        String s = "";

        sql = String.format( "select a.description, sum(a.qty+ifnull(a.qtychanged,0)) as s from items as a, orders as b where LocalBumped=0 and hiden=0 and b.parked<>1 and b.bumped<>1 and a.orderguid=b.guid and screen=%d",
                nUser);

        for (int i=0; i< arItemsFilter.size(); i++)
        {
            if (i==0)
                sql += " and (";
            else
                sql += " or ";
            sql += "upper(a.description)='" + KDSUtil.fixSqliteSingleQuotationIssue(arItemsFilter.get(i).toUpperCase()) + "' ";
            if (i == arItemsFilter.size()-1)
            {
                sql += ") ";
            }
        }

        sql += " group by description order by s DESC";

        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {

            KDSSummaryItem sumItem = new KDSSummaryItem();
            sumItem.setDescription( getString(c,0));

            sumItem.setQty(getFloat(c,1));
            arSums.add(sumItem);

        }
        c.close();

        //check condiemtns
        ArrayList<String> condimentsFilter = getCondimentsFilter(arItemsFilter);
        if (condimentsFilter.size()>0)
        {
            ArrayList<KDSSummaryItem> condimentsSum =summaryCondimentsAdvanced(nUser, condimentsFilter);
            arSums.addAll(condimentsSum);
        }
        //
        summaryItemsSortByQty(arSums, !bAscend);
        if (bCheckSmartQty) {
             summaryItemsAdvancedAddSmartQty(arSums);
        }

        summaryRebuildNamesAndQty(arSums);

        //t.debug_print_Duration("load sumend");
        return arSums;

    }

    private ArrayList<String> getCondimentsFilter( ArrayList<String> arItemsFilter)
    {
        ArrayList<String> condimentsFilter = new ArrayList<>();
        for (int i=0; i< arItemsFilter.size(); i++)
        {
            if (arItemsFilter.get(i).indexOf(KDSSummaryItem.CONDIMENT_TAG) >=0)
                condimentsFilter.add(arItemsFilter.get(i).replace(KDSSummaryItem.CONDIMENT_TAG, ""));

        }
        return condimentsFilter;
    }

    /**
     *
     * @param nUser
     * @param condimentsFilter
     *  It is without the condiment flag
     * @return
     */
    public ArrayList<KDSSummaryItem> summaryCondimentsAdvanced(int nUser,ArrayList<String> condimentsFilter)
    {
        //TimeDog t = new TimeDog();
        ArrayList<KDSSummaryItem> arSums = new ArrayList<>();
        if (getDB() == null) return arSums;


        if (condimentsFilter.size()<=0) return arSums;
        String sql = "";

        String s = "";
//count(c.description)*
        sql = String.format( "select c.description, sum(b.qty) as s " +
                            "from orders as a, items as b, condiments as c " +
                            "where b.LocalBumped=0 and b.hiden=0 and a.parked<>1 and a.bumped<>1 and c.itemGUID=b.GUID and b.orderguid=a.guid and a.screen=%d",
                nUser);

        for (int i=0; i< condimentsFilter.size(); i++)
        {
            if (i==0)
                sql += " and (";
            else
                sql += " or ";
            sql += "upper(c.description)='" + KDSUtil.fixSqliteSingleQuotationIssue(condimentsFilter.get(i).toUpperCase()) + "' ";
            if (i == condimentsFilter.size()-1)
            {
                sql += ") ";
            }
        }

        sql += " group by c.description order by s DESC";

        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {

            KDSSummaryItem sumItem = new KDSSummaryItem();
            sumItem.setSumSrcType(KDSSummaryItem.SumSrcMode.Condiment);
            sumItem.setDescription( getString(c,0));

            sumItem.setQty(getFloat(c,1));
            arSums.add(sumItem);

        }
        c.close();

        //summaryItemsSortByQty(arSums, !bAscend);
//        if (bCheckSmartQty)
//            summaryItemsAdvancedAddSmartQty(arSums);

        //summaryRebuildNamesAndQty(arSums);
        //t.debug_print_Duration("load sumend");
        return arSums;

    }

    /**
     * check the smart items hiden items
     * @param arSums
     */
    private void summaryItemsAdvancedAddSmartQty(ArrayList<KDSSummaryItem> arSums)
    {
        for (int i=0; i< arSums.size(); i++)
        {
            if (arSums.get(i).getSumSrcType() == KDSSummaryItem.SumSrcMode.Item)
                summaryItemsAdvancedAddSmartQty(arSums.get(i));
            else if (arSums.get(i).getSumSrcType() == KDSSummaryItem.SumSrcMode.Condiment)
            {
                summaryCondimentsAdvancedAddSmartQty(arSums.get(i));
            }
            //String sql = sql1 + arSums.get(i).getDescription().toUpperCase()+"'";
            //summaryItemsAdvancedAddSmartQty(arSums.get(i), sql);
        }

    }
    //private final String adv_sum_sql = "select items.itemdelay,items.preparationtime,orders.orderdelay,orders.start,items.qty,items.name,orders.guid";
    private final String adv_sum_sql = "select orders.orderdelay,orders.start,items.qty,items.name,orders.guid";
    private void summaryItemsAdvancedAddSmartQty(KDSSummaryItem sumItem)
    {


        //String sql1 =  "select items.itemdelay,items.preparationtime,orders.orderdelay,orders.start,items.qty,items.name,orders.guid from items,orders ";
        String sql1 = adv_sum_sql + " from items,orders ";
        sql1 += "where items.orderguid=orders.guid and orders.bumped<>1 and items.localbumped<>1 and  upper(items.description)='";


        //for (int i=0; i< arSums.size(); i++)
        //{
            String sql = sql1 + sumItem.getDescription().toUpperCase()+"'";
            summaryItemsAdvancedAddSmartQty(sumItem, sql);
        //}

    }

    /**
     * Advanced summary, get the "hidden/gray" smart condiment(from its item) qty.
     * @param sumItem
     */
    private void summaryCondimentsAdvancedAddSmartQty(KDSSummaryItem sumItem)
    {


        //String sql1 =  "select items.itemdelay,items.preparationtime,orders.orderdelay,orders.start,items.qty,items.name,orders.guid from condiments,items,orders ";
        String sql1 = adv_sum_sql + " from condiments,items,orders ";
        sql1 += "where condiments.itemguid=items.guid and items.orderguid=orders.guid and orders.bumped<>1 and items.localbumped<>1 and  upper(condiments.description)='";


        //for (int i=0; i< arSums.size(); i++)
        //{
        String sql = sql1 + sumItem.getDescription().toUpperCase()+"' order by items.preparationtime desc";
        summaryItemsAdvancedAddSmartQty(sumItem, sql);
        //}

    }

    /**
     * In smart order mode, some items is hidden, we need to show these items qty
     * @param sumItem
     * @param sql
     */
    private void summaryItemsAdvancedAddSmartQty(KDSSummaryItem sumItem, String sql)
    {
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {

            //float fltItemDelay = c.getFloat(0);
            //float fltItemPreparation = c.getFloat(1);
            float fltOrderDelay = getFloat(c,0);
            Date dtStart =getDate(c, 1);
            float fltQty = getFloat(c,2);
            String itemName = getString(c,3);
            String orderGuid = getString(c,4);
            PrepSorts prepItems = this.prep_get_sort_items(orderGuid);

            if (!prepItems.is_cooking_time(itemName, dtStart, fltOrderDelay))
                sumItem.setSmartHidenQty(fltQty + sumItem.getSmartHidenQty());

            //if (!KDSDataOrder.smartItemIsTimeToCook(fltItemDelay, fltOrderDelay, fltItemPreparation, dtStart, null))
            //    sumItem.setSmartHidenQty(fltQty + sumItem.getSmartHidenQty());

        }
        c.close();
    }
/************************************************************************/
/*                                                                      */

    /************************************************************************/
    private ArrayList<KDSSummaryItem> summaryItemsWithCondiments(String stationID, int nUser, boolean bSummaryStation, boolean bAscend) {
        ArrayList<KDSSummaryItem> arSums = new ArrayList<>();
        ArrayList<String> arUniqueItems = new ArrayList<>();
        //TimeDog td = new TimeDog();

        arUniqueItems = getAllUniqueItems(stationID, nUser, bSummaryStation);
        if (arUniqueItems.size() <= 0) return arSums;
        String itemDescription = "";
//        td.debug_print_Duration(" sum with condiments ------------------------------------- ");
//        td.debug_print_Duration("sum with condiments 10=");
//        td.reset();
        //make sql orderguid string
        //String sql = " and (";
        String sql =String.format(" and items.orderguid in (select orders.guid from orders where orders.bumped=0 and orders.parked=0 and orders.screen=%d)",
                                    nUser);
//        if (bSummaryStation)
//            sql =String.format(" and items.orderguid in (select orders.guid from orders where orders.bumped=0 and orders.parked=0 and orders.screen=%d)",
//                    nUser);
        //kpp1-415, use new code for orderguid

//        String s = "";
//        int count = arValidOrderGUID.size();
//        if (count <= 0) sql = "";
//        String str = "";
//
//        for (int i = 0; i < count; i++) {
//            str = arValidOrderGUID.get(i);
//            if (i != count - 1)
//                s = String.format("orderguid='%s' or ", str);
//            else
//                s = String.format("orderguid='%s')", str);
//            sql += s;
//        }


        for (int i = 0; i < arUniqueItems.size(); i++) {
            itemDescription = arUniqueItems.get(i);
            if (itemDescription.isEmpty()) continue;
            if (sql.isEmpty()) continue;
            Cursor c = getSameItems(stationID, nUser, bSummaryStation, itemDescription, sql);
//            td.debug_print_Duration("sum with condiments 101=");
//            td.reset();
            if (c == null) continue;
            ArrayList<String> sameItemsGuid = new ArrayList<>();
            ArrayList<Float> sameItemsQty = new ArrayList<>();
            ArrayList<Float> sameItemsQtyChg = new ArrayList<>();
            while (c.moveToNext())
            {
                sameItemsGuid.add(c.getString(0));
                sameItemsQty.add(getFloat(c,1));
                sameItemsQtyChg.add(getFloat(c,2));
            }
            c.close();
            createItemsSummaryWidthCondiments(stationID, nUser, itemDescription, sameItemsGuid,sameItemsQty,sameItemsQtyChg, arSums);

//            td.debug_print_Duration("sum with condiments 102=");
//            td.reset();

        }

//        td.debug_print_Duration("sum with condiments 11=");
//        td.reset();

        summaryItemsSortByQty(arSums,!bAscend);
        //td.debug_print_Duration("sum with condiments 12=");
        //td.reset();
        summaryRebuildNamesAndQty(arSums);
        //td.debug_print_Duration("sum with condiments 13=");
        //td.reset();
        return arSums;


    }

    /************************************************************************/
/*
2.5.3.6, use the summary name to replace original item name,
	and recalculate the quantity.
3.1: if there are empty summary name, exclude it from summary list

*/
    /************************************************************************/
    boolean summaryRebuildNamesAndQty( ArrayList<KDSSummaryItem> arSumItems) //use the summary items
    {

        ArrayList<KDSSummaryItem> ar = new ArrayList<>();

        ar.addAll(arSumItems); //use this array to loop
        arSumItems.clear(); //clear old one, build it again.

        int count = ar.size();

        String description;

        String categoryName;



        for (int i=0; i< count; i++)
        {
            KDSSummaryItem p = ar.get(i);
            description = p.getDescription();
            //qty = p.getQty();
            categoryName = p.getCategory();

            //get all summary item name for this this item
            KDSDataSumNames sumNames = findSumNamesFromItemsTable(categoryName, description);


            if (!sumNames.getEnabled()) //use original name
            {
                arSumItems.add(p);
            }
            else
            {
                if (sumNames.getCount()<=0) continue; //if enabled, and empty translate, don't show this item.
                //check if there are any empty summary name,
                //if true, don't summary it.
                boolean bexclude = false;
                if (sumNames.getCount()<=0 ||sumNames.isAllEmpty())
                    bexclude = true;

                if (bexclude) continue;
                //////////////////////////////////
                for (int j = 0; j< sumNames.getCount(); j++)
                {
                    KDSSummaryItem sumItem = new KDSSummaryItem();
                    //recalculate the qty, change name.
                    sumItem.setDescription( sumNames.getSumName(j).getDescription());
                    sumItem.setQty(sumNames.getSumName(j).getSumQty()*p.getQty());
                    sumItem.setOriginalDescription(p.getDescription());

                    if (sumItem.getQty() == 0) sumItem.setQty( p.getQty());

                    boolean bexistedsameitem = false;

                    //check if existed same summary item
                    for (int k = 0; k < arSumItems.size(); k++)
                    {

                        KDSSummaryItem existedItem = arSumItems.get(k);
                        if (existedItem.getDescription().equals( sumItem.getDescription()))
                        {
                            existedItem.setQty( existedItem.getQty() + sumItem.getQty());
                            //delete pitem;
                            bexistedsameitem = true;
                        }
                    }
                    //add it to array
                    if (!bexistedsameitem)
                        arSumItems.add(sumItem);
                }


            }

        }
        ar.clear();

        return true;
    }

    /**
     * Translate the summary item to other description
     * @param category
     * @param itemDescription
     * @return
     */
    public KDSDataSumNames findSumNamesFromItemsTable(String category, String itemDescription)
    {
        if (getDB() == null) return new KDSDataSumNames();

        String sql = "";
        if (category.isEmpty())
            sql =String.format("select SumTransEnable,SumTrans from items where description='%s' order by dbtimestamp desc limit 1", KDSUtil.fixSqliteSingleQuotationIssue(itemDescription));
        else
            sql =String.format("select SumTransEnable,SumTrans from items where category='%s' and description='%s' order by dbtimestamp desc limit 1 ",KDSUtil.fixSqliteSingleQuotationIssue(category),KDSUtil.fixSqliteSingleQuotationIssue( itemDescription));

        Cursor c = getDB().rawQuery(sql, null);
        //t.debug_print_Duration("load sum2");
        int nEnabled = 0;
        String sumNames = "";
        if (c.moveToNext()) {
            nEnabled = getInt(c,0);
            sumNames = getString(c,1);


        }
        c.close();
        KDSDataSumNames sum = KDSDataSumNames.parseString(sumNames);

        sum.setEnabled((nEnabled!=0));

        return sum;

    }

    boolean summaryItemsSortByQty(ArrayList<KDSSummaryItem> sumItems, boolean bDescend) {

        if (bDescend) {
            //   Collections.sort(sumItems, new SortByQtyAscend());
            Collections.sort(sumItems, new Comparator() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            KDSSummaryItem s1 = (KDSSummaryItem) o1;
                            KDSSummaryItem s2 = (KDSSummaryItem) o2;
                            if (s1.getQty() > s2.getQty())
                                return -1;
                            else if ((s1.getQty() == s2.getQty()))
                                return 0;
                            else if ((s1.getQty() < s2.getQty()))
                                return 1;
                            return 0;
                        }
                    }
            );
        } else {
            //Collections.sort(sumItems, new SortByQtyDescend());
            Collections.sort(sumItems, new Comparator() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            KDSSummaryItem s1 = (KDSSummaryItem) o1;
                            KDSSummaryItem s2 = (KDSSummaryItem) o2;
                            if (s1.getQty() > s2.getQty())
                                return 1;
                            else if ((s1.getQty() == s2.getQty()))
                                return 0;
                            else if ((s1.getQty() < s2.getQty()))
                                return -1;
                            return 0;
                        }
                    }
            );
        }
        return true;

    }
    /************************************************************************/
    //all same items name is in recordset
    /************************************************************************/
    /*

    nStation: which station,
    nUser: which user
    itemName: the item name for summary
    pRS: the item table.
        The fields:
            guid, qty,  qtychanged
    pItemsSum: return data save to this parameter

    */
    /************************************************************************/
    int createItemsSummaryWidthCondiments(String stationID, int nUser, String itemName, ArrayList<String> sameItemsGuid,ArrayList<Float> itemQty,ArrayList<Float> itemQtyChanged, ArrayList<KDSSummaryItem> pItemsSum) {

        //TimeDog td = new TimeDog();
        //ArrayList<String> orderGUIDs = new ArrayList<>();
        //ArrayList<String> itemGUIDs = new ArrayList<>();
        //ArrayList<String> itemCategories = new ArrayList<>();

        //ArrayList<Float> itemQty = new ArrayList<>();
        //ArrayList<Float> itemQtyChanged = new ArrayList<>();
        //load all data to buffer
        //while (pRs.moveToNext()) {
//        for (int i=0; i< sameItemsGuid.size(); i++)
//            String s = getString(pRs,0);
//            itemGUIDs.add(s);
//            //s = getString(pRs,1);
//            //orderGUIDs.add(s);
//
//            float qty = 0;
//
//            qty = getFloat(pRs,1);
//            itemQty.add(qty);
//
//            //s = getString(pRs,2);
//            //itemCategories.add(s);
//
//            qty = getFloat(pRs,2);
//            itemQtyChanged.add(qty);
//
//
//        }
        //td.debug_print_Duration("sum with condiments 1=");
        ArrayList<KDSSummaryCondiment> curcondiments = new ArrayList<>(); //kpp1-421
        ArrayList<KDSSummaryCondiment> checkcondiments = new ArrayList<>(); //kpp1-421

        int curindex = 0;
        int checkindex = 1;
        //String orderGUID = "";
        String itemGUID = "";


        String curItemGUID = "";//, curCategory = "";

        //td.reset();

        while (true) {
            //curindex = -1;
            //if (curindex >= sameItemsGuid.size()-1 ) break;
            boolean bAllDone = true;
            for (int i=curindex; i< sameItemsGuid.size(); i++)
            {
                if (!sameItemsGuid.get(i).isEmpty()) {
                    curindex = i;
                    bAllDone = false;
                    break;
                }
            }
            if (bAllDone) break;

           // if (curindex >= sameItemsGuid.size() || curindex<0) break;
            //curOrderGUID = orderIDs.get(curindex);
            curItemGUID = sameItemsGuid.get(curindex);
            //curCategory = itemCategories.get(curindex);

            float curqty = itemQty.get(curindex) + itemQtyChanged.get(curindex);
            curcondiments.clear();
            curcondiments = getItemCondimentStrings(curItemGUID); //load this item's condiments
            KDSSummaryItem sumItem = new KDSSummaryItem();
            sumItem.setDescription(itemName);
            sumItem.setQty(curqty);
            //sumItem.setCategory(curCategory);
            sumItem.setCondiments(curcondiments);

            pItemsSum.add(sumItem);

            checkindex = curindex + 1;
            sameItemsGuid.set(curindex, "");

            //check all others, find same item with condiments
            //if same, add qty, and remove it.
            // it is not same, reserve it for next check.
            while (true) {
                if (checkindex >= sameItemsGuid.size()) break;
                //orderGUID = orderGUIDs.get(checkindex);
                itemGUID = sameItemsGuid.get(checkindex);
                if (itemGUID.isEmpty()) {
                    checkindex ++;
                    continue;
                }
                checkcondiments.clear();
                checkcondiments = getItemCondimentStrings(itemGUID, checkcondiments);
                if (checkcondiments.size() != sumItem.getCondiments().size()) {
                    checkindex++;
                    continue;
                }
                //compare each condiments, it has sure the condiments count is same.
                boolean bsame = true;
                for (int i = 0; i < checkcondiments.size(); i++) {
                    //if (!sumItem.getCondiments().get(i).equals(checkcondiments.get(i))) {
                    if (!sumItem.getCondiments().get(i).isEqual(checkcondiments.get(i))) {
                        bsame = false;
                        break;
                    }

                }
//                if (condimentGetCount(itemGUID) != sumItem.getCondiments().size())
//                {
//                    checkindex++;
//                    continue;
//                }
//                boolean bsame = true;
//                if (!isSameCondiments(itemGUID,curItemGUID, sumItem.getCondiments().size() ))
//                    bsame = false;

                if (bsame) {
                    sumItem.setQty(sumItem.getQty() + itemQty.get(checkindex)+itemQtyChanged.get(checkindex));
                    //orderGUIDs.remove(checkindex);
//                    sameItemsGuid.remove(checkindex);
//                    itemQty.remove(checkindex);
//                    itemQtyChanged.remove(checkindex);
                    sameItemsGuid.set(checkindex, "");
                    //itemQty.remove(checkindex);
                    //itemQtyChanged.remove(checkindex);
                    //itemCategories.remove(checkindex);
                    checkindex ++;
                } else
                    checkindex++;

            }

           // curindex++;

        }
        //td.debug_print_Duration("sum with condiments 2=");
        return pItemsSum.size();
    }

    /************************************************************************/
/*                                                                      */

    /************************************************************************/
    /**
     * rev.:
     *  //kpp1-421
     * @param itemGUID
     * @return
     */
    private ArrayList<KDSSummaryCondiment> getItemCondimentStrings(String itemGUID) {

        ArrayList<KDSSummaryCondiment> arCondiments = new ArrayList<>();
        return getItemCondimentStrings(itemGUID, arCondiments);

//        if (getDB() == null) return arCondiments;
//        String sql = "";
//        sql = String.format("select description from condiments where itemguid='%s' order by description", itemGUID);
//
//        Cursor c = getDB().rawQuery(sql, null);
//        String s = "";
//
//        while (c.moveToNext()) {
//            s = getString(c,0);
//            arCondiments.add(s);
//        }
//        c.close();
//        return arCondiments;

    }

    private ArrayList<KDSSummaryCondiment> getItemCondimentStrings(String itemGUID, ArrayList<KDSSummaryCondiment> arCondiments) {

        //ArrayList<String> arCondiments = new ArrayList<String>();
        if (getDB() == null) return arCondiments;
        String sql = "";
        //kpp1-414
        sql = String.format("select description,qty from condiments where itemguid='%s' order by description", itemGUID);
        //sql = String.format("select description from condiments where itemguid='%s' order by description", itemGUID);

        Cursor c = getDB().rawQuery(sql, null);
        String s = "";
        int qty = 0;
        while (c.moveToNext()) {
            s = getString(c,0);
            qty = getInt(c, 1); //kpp1-414
            //if (qty >1) //kpp1-421
            //    s = Integer.toString(qty) + "x " + s;
            arCondiments.add(new KDSSummaryCondiment(qty, s));// s); //kpp1-421
        }
        c.close();
        return arCondiments;

    }

    static public String replaceSingleQuotation(String str) {
        String s = str;
        s = s.replace("'", "''");
        return s;
    }
    /************************************************************************/
/* for summary with condiments function
return:
	all items information with same item name and in valid_order_id array.
	the *pRS is return value.
                                                                     */

    /************************************************************************/
    Cursor getSameItems(String stationID, int nUser, boolean bSummaryStation, String itemDescription, String sqlOrdersGuid) {


        itemDescription = replaceSingleQuotation(itemDescription);
        String sql = "";
//	sql.Format( "select * from items where station=%d and user= %d and name='%s' ", nStation, nUser, itemName);
        //2008-07-17 add marked filter
        //2.0.28, fix bug
//        sql = String.format("select items.guid,items.qty,items.qtychanged " +
//                "from items,orders " +
//                "where items.description='%s' and items.marked=0 and orders.guid=items.orderguid and items.LocalBumped=0 " +
//                "and ( items.orderguid in (select orders.guid from orders where orders.bumped=0 and orders.screen=%d) )", itemDescription, nUser);

        //sql = String.format("select items.guid,items.orderguid,items.qty,items.category,items.qtychanged from items,orders where description='%s' and marked=0 and orders.screen=%d and orders.guid=items.orderguid ",  itemDescription, nUser);
        //sql = String.format("select items.guid,items.qty,items.qtychanged from items,orders where description='%s' and marked=0 and orders.screen=%d and orders.guid=items.orderguid ",  itemDescription, nUser);
        sql = String.format("select guid,qty,qtychanged from items where LocalBumped=0 and marked=0 and description='%s' ",  itemDescription);
        sql += sqlOrdersGuid;
        if (bSummaryStation)
            sql = String.format("select guid,qty,qtychanged from items where LocalBumped=0 and bumpedstations='' and marked=0 and description='%s' ",  itemDescription);

//        sql += " and (";
//        String s = "";
//        int count = arValidOrderGUID.size();
//        if (count <= 0) return null;
//        String str = "";
//        int i = 0;
//        for (i = 0; i < count; i++) {
//            str = arValidOrderGUID.get(i);
//            if (i != count - 1)
//                s = String.format("orderguid='%s' or ", str);
//            else
//                s = String.format("orderguid='%s')", str);
//            sql += s;
//        }
        Cursor c = getDB().rawQuery(sql, null);
        return c;

    }

    /**
     *
     * @param stationID
     * @param nUser
     * @param bSummaryStation
     * @return
     */
    private ArrayList<String> getAllUniqueItems(String stationID, int nUser, boolean bSummaryStation) {

        ArrayList<String> arItems = new ArrayList<>();

        if (getDB() == null) return arItems;

        String sql = "";
//        sql = String.format("select items.description from items,orders where items.hiden=0 and items.LocalBumped=0 and ( items.orderguid in (select orders.guid from orders where orders.bumped=0 and orders.screen=%d) ) group by items.description",
//                            nUser);
        sql = String.format("select items.description from items  where items.hiden=0 and items.LocalBumped=0  group by items.description");
        if (bSummaryStation)
            sql = String.format("select items.description from items  where items.hiden=0 and items.LocalBumped=0 and items.bumpedstations='' group by items.description");

//        //3.1.0.18 add hide
//        sql = String.format("select description from items where hiden=0 ", stationID, nUser);//3.1.0.18
//        sql += "and (";
//        String s = "";
//        int count = arValidOrderGUID.size();
//        if (count <= 0) return arItems;
//        String str = "";
//        int i = 0;
//        for (i = 0; i < count; i++) {
//            str = arValidOrderGUID.get(i);
//            if (i != count - 1)
//                s = String.format("orderguid='%s' or ", str);
//            else
//                s = String.format("orderguid='%s')", str);
//            sql += s;
//        }
//        sql += " group by description";

        Cursor c = getDB().rawQuery(sql, null);


        while (c.moveToNext()) {
//            String s = getString(c,0);
//            arItems.add(s);
            arItems.add( getString(c,0));
        }
        c.close();

        return arItems;

    }

    @Override
    public boolean executeDML(String sql) {

        if (getDB() == null) return false;
        if (getDbState() == DB_STATE.Record_Sql) {
            String s = String.format("insert into BufferedSql (sql) values ('%s')", sql);
            this.getDB().execSQL(sql);

        }

        return super.executeDML(sql);

    }

    /**
     * copy all data in db to me
     *
     * @param db the source database.
     * @return
     */
    public boolean copyDB(KDSDBCurrent db) {

        boolean bStartedByMe = this.startTransaction();
        if (copyTableData(db, "orders")) {
            copyTableData(db, "items");
            copyTableData(db, "condiments");
            copyTableData(db, "messages");

        }
        finishTransaction(bStartedByMe);
        return true;


    }


    /**
     * combine these two database data.
     *
     * @param db the source database.
     * @return
     */
    public boolean combineDbOrders(KDSDBCurrent db) {

        boolean bStartedByMe = this.startTransaction();
        combineDatabaseOrders(db);
        finishTransaction(bStartedByMe);
        return true;


    }

    private void orderSetFromPrimaryOfBackup(ArrayList<String> arOrdersGuid)
    {
        for (int i=0; i< arOrdersGuid.size(); i++)
        {
            String guid = arOrdersGuid.get(i);
            String sql = String.format("update orders set fromprimary=1 where guid='%s'",guid );
            this.executeDML(sql);
        }
    }
    /**
     *
     * @param db
     * @return
     *     the order guid was combined in "db".
     */
    public boolean combineDatabaseOrders(KDSDBCurrent db)
    {
        if (db.getDB() == null) return false;
        String sql = "select guid, name,bumped from orders";
        ArrayList<String> arCombinedOrders = new ArrayList<>();
        ArrayList<GuidName> arSameNameOrders = new ArrayList<>(); //same order maybe splite to two or more station. We need to combine them too.
                                                                //this allow backup station work as normal station.

        Cursor c = db.getDB().rawQuery(sql, null);
        while (c.moveToNext())
        {
            String guid = getString(c,0);
            String name = getString(c,1);
            int nBumped = getInt(c,2);
            String existedGuid = this.orderExisted(name, nBumped);
            if (existedGuid.isEmpty())//don't existed
                arCombinedOrders.add(guid); //db guid
            else {
                GuidName g = new GuidName();
                g.bumped = nBumped;
                g.guid = existedGuid; //the guid is the destination guid
                g.name = name;
                arSameNameOrders.add(g);
            }
        }
        ArrayList<String> orders = outputTableData(db, "orders", arCombinedOrders);
        insertData(orders);
        orderSetFromPrimaryOfBackup(arCombinedOrders);
        orders.clear();

        ArrayList<String> itemguids = itemGetAllGuidByOrder(db, arCombinedOrders);
        ArrayList<String> items = outputTableData(db, "items", itemguids);
        insertData(items);
        items.clear();

        ArrayList<String> condimentguids = condimentsGetAllGuidByItems(db, itemguids);
        ArrayList<String> condiments = outputTableData(db, "condiments", condimentguids);
        insertData(condiments);
        condiments.clear();
        condimentguids.clear();

        arCombinedOrders.addAll(itemguids);
        itemguids.clear();

        ArrayList<String> messagesguids = messagesGetAllGuidByGuid(db, arCombinedOrders);

        ArrayList<String> messages = outputTableData(db, "messages", messagesguids);
        insertData(messages);
        messages.clear();

        //check same orders names
        //

        for (int i=0;i<arSameNameOrders.size(); i++)
        {
            GuidName ordername = arSameNameOrders.get(i);
            ArrayList<GuidName> myItemsName = this.itemGetNames(ordername.name, ordername.bumped);
            ArrayList<GuidName> dbItemsName = db.itemGetNames(ordername.name, ordername.bumped);
            ArrayList<GuidName> itemsWillAppend = guidnameItemsWillAppend(myItemsName, dbItemsName);
            for (int j=0; j< itemsWillAppend.size(); j++)
            {
                GuidName guidname = itemsWillAppend.get(j);
                KDSDataItem item= db.itemGet(guidname.guid);
                if (item == null) continue;
                item.setOrderGUID(ordername.guid);
                itemAdd(item);
            }
            if (itemsWillAppend.size()>0)
                orderSetFromPrimaryOfBackup(ordername.guid);
        }

        //combine the schedule ready qty table.
        schedule_process_combine(db);
        //orderSetAllFromPrimaryOfBackup(true);
        return true;
    }

    /**
     *
     * @param db
     *  copy data from this db to myself.
     */
    private void schedule_process_combine(KDSDBCurrent db)
    {

        ArrayList<String> arSourceDB =  schedule_process_get_all_itemguid(db);
        ArrayList<String> arMyDB =  schedule_process_get_all_itemguid(this);

        ArrayList<String> arWillCopyToMe =  new ArrayList<>();
        for (int i=0; i< arSourceDB.size(); i++)
        {
            String sourceGuid = arSourceDB.get(i);
            if (KDSUtil.isExistedInArray(arMyDB, sourceGuid))
                continue;
            arWillCopyToMe.add(sourceGuid);
        }

        ArrayList<String> processes = outputTableData(db, "scheduleprocess","itemguid", arWillCopyToMe);
        insertData(processes);

    }

    private ArrayList<String> schedule_process_get_all_itemguid(KDSDBCurrent db)
    {
        String sql = "select itemguid from scheduleprocess";
        ArrayList<String> ar = new ArrayList<>();
        if (db.getDB() == null) return ar;

        Cursor c = db.getDB().rawQuery(sql, null);
        while (c.moveToNext()) {
            String id = getString(c,0);
            ar.add(id);
        }
        c.close();
        return ar;
    }
    private ArrayList<GuidName> guidnameItemsWillAppend( ArrayList<GuidName> arDest, ArrayList<GuidName> arAppend)
    {
        ArrayList<GuidName> ar = new ArrayList<>();

        for (int i=0; i< arAppend.size(); i++)
        {
            if (!guidnameExisted(arDest, arAppend.get(i)))
            {
                GuidName g = arAppend.get(i);

                ar.add(arAppend.get(i));
            }
        }
        return ar;
    }

    private boolean guidnameExisted(ArrayList<GuidName> arDest, GuidName guidName)
    {
        for (int i=0; i<arDest.size(); i++)
        {
            if (arDest.get(i).name.equals(guidName.name))
                return true;
        }
        return false;
    }

    /**
     *
     * @param
     * @return
     */
    ArrayList<GuidName> itemGetNames(String ordername, int nbumped)
    {
        ArrayList<GuidName> ar = new  ArrayList<>();
        if (getDB() == null) return ar;

        String orderGuid = "";
        if (nbumped != 1)
            orderGuid = this.orderGetUnbumpedGuidFromName(ordername);
        else
            orderGuid = this.orderGetBumpedGuidFromName(ordername);
        if (orderGuid.isEmpty()) return ar;
        String sql = "select guid,name from items where orderguid='" + orderGuid +"'";
        Cursor c = getDB().rawQuery(sql, null);
        while (c.moveToNext()) {
            String guid = getString(c,0);
            String name = getString(c,1);
            GuidName g = new GuidName();
            g.name = name;
            g.guid = guid;
            g.bumped = nbumped;
            ar.add(g);
        }
        return ar;

    }

    private void insertData(ArrayList<String> arSql)
    {
        for (int i = 0; i < arSql.size(); i++) {
            this.executeDML(arSql.get(i));
        }
    }
    private ArrayList<String> itemGetAllGuidByOrder(KDSDBCurrent db,ArrayList<String> arOrderGuid )
    {
        ArrayList<String> aritems = new ArrayList<String>();
        if (db.getDB() == null) return aritems;

        for (int i=0; i< arOrderGuid.size(); i++) {
            String orderguid = arOrderGuid.get(i);
            String sql = String.format("select guid from items where orderguid='%s'", orderguid);
            Cursor c = db.getDB().rawQuery(sql, null);
            while (c.moveToNext()) {
                aritems.add(getString(c,0));
            }
            c.close();
        }
        return aritems;

    }

    private ArrayList<String> condimentsGetAllGuidByItems(KDSDBCurrent db,ArrayList<String> arItemGuid )
    {
        ArrayList<String> arcondiments = new ArrayList<String>();
        if (db.getDB() == null) return arcondiments;

        for (int i=0; i< arItemGuid.size(); i++) {
            String itemguid = arItemGuid.get(i);
            String sql = String.format("select guid from condiments where itemguid='%s'", itemguid);
            Cursor c = db.getDB().rawQuery(sql, null);
            while (c.moveToNext()) {
                arcondiments.add(getString(c,0));
            }
            c.close();
        }
        return arcondiments;

    }

    private ArrayList<String> messagesGetAllGuidByGuid(KDSDBCurrent db,ArrayList<String> arGuid )
    {
        ArrayList<String> armessages = new ArrayList<String>();

        if (db.getDB() == null) return armessages;

        for (int i=0; i< arGuid.size(); i++) {
            String guid = arGuid.get(i);
            String sql = String.format("select guid from messages where objguid='%s' order by id", guid);
            Cursor c = db.getDB().rawQuery(sql, null);
            while (c.moveToNext()) {
                armessages.add(getString(c,0));
            }
            c.close();
        }
        return armessages;

    }


    public String orderExisted(String orderName, int nBumped)
    {
        if (getDB() == null) return "";

        String sql = String.format("select guid from orders where name='%s' and bumped=%d", orderName, nBumped);
        Cursor c = getDB().rawQuery(sql, null);
        String guid = "";
        if (c.moveToNext()) {
            guid = getString(c,0);

        }
        c.close();
        return guid;
    }

    public boolean copyTableData(KDSDBCurrent db, String tblName) {
        ArrayList<String> arSql = outputTableDataSql(db, tblName, "");
        for (int i = 0; i < arSql.size(); i++) {
            this.executeDML(arSql.get(i));
        }
        return (arSql.size() > 0);

    }



    public ArrayList<String> outputActiveOrdersSqlStrings() {
        ArrayList<String> arsql = new ArrayList<>();
        ArrayList<String> arOrdersGuid = orderGetAllActiveGUID(this);
        for (int i = 0; i < arOrdersGuid.size(); i++) {
            String orderGuid = arOrdersGuid.get(i);
            String whereCondition = "where guid='" + orderGuid + "'";
            ArrayList<String> arOrders = outputTableDataSql(this, "orders", whereCondition);
            arsql.addAll(arOrders);

            whereCondition = "where orderguid='" + orderGuid + "'";
            ArrayList<String> arItems = outputTableDataSql(this, "items", whereCondition);
            arsql.addAll(arItems);

            whereCondition = "where objguid='" + orderGuid + "'";
            ArrayList<String> arOrderMessages = outputTableDataSql(this, "messages", whereCondition);
            arsql.addAll(arOrderMessages);

            ArrayList<String> arItemsGuid = itemGetAllItemsGUID(this, orderGuid);
            for (int j = 0; j < arItemsGuid.size(); j++) {
                String itemguid = arItemsGuid.get(j);
                whereCondition = "where itemguid='" + itemguid + "'";
                ArrayList<String> arCondiments = outputTableDataSql(this, "condiments", whereCondition);
                arsql.addAll(arCondiments);

                whereCondition = "where objguid='" + itemguid + "'";
                ArrayList<String> arMessages = outputTableDataSql(this, "messages", whereCondition);
                arsql.addAll(arMessages);
            }

        }
        return arsql;
    }

    public int orderGetActiveCount()
    {
        String sql = "select count(*) as c from orders where bumped<>1 and Parked=0";
        int ncount = this.executeOneValue(sql);
        return ncount;
    }


    /**
     * clear the expired bumped orders.

     * @return
     *  How many order were deleted
     */
    public int clearExpiredBumpedOrders( int nMaxCount)
    {

       // int nBumpedByDays = clearExpiredBumpedOrdersByDays(nBumpedDays);
        int nBumpedByCount = clearExpiredBumpedOrdersByCount(nMaxCount);
        return nBumpedByCount;

        //return (nBumpedByCount > nBumpedByDays) ? nBumpedByCount: nBumpedByDays;
    }

    public int clearExpiredBumpedOrdersByDays(int nBumpedDays)
    {
        if (getDB() == null) return 0;
        Date dt = new Date();
        long lDtNow = dt.getTime();
        lDtNow -= (nBumpedDays * 24 * 60 * 60 * 1000);
        dt.setTime(lDtNow);

        String sql = "select guid from orders where bumped=1 and bumpedTime<='" + KDSUtil.convertDateToString(dt) + "'";
        Cursor c = getDB().rawQuery(sql, null);
        int ncounter = 0;
        while (c.moveToNext()) {
            String orderGuid = getString(c,0);
            this.orderDelete(orderGuid);
            ncounter ++;
        }
        return ncounter;
    }


    public int clearExpiredBumpedOrdersByCount(int nMaxCount)
    {

        if (getDB() == null) return 0;

        //check count first.
        String sql = "select count(*) from orders where bumped=1";

        //2.0.37
        int nBumped = this.executeOneValue(sql);
        //int nMax = Math.round(nMaxCount * 1.5f);
        int nMax = Math.round(nMaxCount ); //KPP1-207, just bump according to setting value.
        if (nBumped<=nMax ) return 0; //kpp1-412
        int nNeedBumped = nBumped - nMaxCount;
        //TimeDog td = new TimeDog();
        sql = "select guid from orders where bumped=1 order by bumpedTime asc limit " + KDSUtil.convertIntToString(nNeedBumped);
        this.orderDeleteQuickBatch(sql);

//        Cursor c = getDB().rawQuery(sql, null);
//        //int ncounter = -1;
//
//        int nBumpedCount = 0;
//        while (c.moveToNext()) {
//            String orderGuid = getString(c,0);
//            TimeDog t = new TimeDog();
//            this.orderDeleteQuick(orderGuid);
//            t.debug_print_Duration("orderDeleteQuick");
////            nBumpedCount ++;
////            if (nBumpedCount >=nNeedBumped) break;
//        }


//        while (c.moveToNext()) {
//            ncounter ++;
//            if (ncounter <nMaxCount) continue;
//            String orderGuid = getString(c,0);
//            this.orderDeleteQuick(orderGuid);
//            nBumpedCount ++;
//        }
//        c.close();
        //td.debug_print_Duration("clearExpiredBumpedOrdersByCount");
        return nNeedBumped;
    }


    public float screenGetTotalQty(int nScreen)
    {
        String sql = String.format("select sum(items.qty) from items,orders where orders.guid=items.orderguid and orders.parked<>1 and orders.bumped<>1 and screen=%d", nScreen);
        return this.executeOneFloat(sql);
    }

    public boolean updateDbTimeStamp()
    {
        String sql = "select count(*) from bufferedsql";
        int n = this.executeOneValue(sql);
        if (n>0)
            sql = String.format("update bufferedsql set dbtimestamp=datetime('now','localtime')");
        else
            sql = String.format("insert into bufferedsql(dbtimestamp) values (datetime('now','localtime'))" );

        return this.executeDML(sql);
    }

    public String getDbTimeStamp()
    {
        String sql = "select dbtimestamp from bufferedsql";
        if (getDB() == null) return  KDSUtil.convertDateToString(KDSUtil.createInvalidDate());
        Cursor c = getDB().rawQuery(sql, null);
        String tm = KDSUtil.convertDateToString(new Date());// "";
        while (c.moveToNext()) {
            tm = getString(c,0);
            break;
        }
        c.close();
        return tm;
    }

    public void orderSetAllFromPrimaryOfBackup(boolean bFromPrimary)
    {
        String sql = String.format("update orders set FromPrimary=%d", bFromPrimary ? 1 : 0);
        this.executeDML(sql);
    }

    private void orderSetFromPrimaryOfBackup(String orderGuid)
    {

            String guid = orderGuid;
            String sql = String.format("update orders set fromprimary=1 where guid='%s'",guid );
            this.executeDML(sql);

    }

    public int orderGetAllItemsCount()
    {
        String sql = "select count(items.guid) from items,orders where orders.guid==items.orderguid and orders.bumped<>1;";
        return this.executeOneValue(sql);
    }


    public int orderGetItemsCount(String orderGuid)
    {
        String sql =String.format( "select count(items.guid) from items,orders where items.orderguid='%s' and orders.bumped<>1;", orderGuid);
        return this.executeOneValue(sql);
    }

    /**
     * for line items display mode
     * @param orderGuid
     * @param itemGuid
     * @return
     */
    public int orderGetItemsPrevCount(String orderGuid, String itemGuid)
    {
        if (itemGuid.isEmpty())
            return 0;
        String sql =String.format( "select items.guid from items,orders where items.orderguid='%s' and orders.bumped<>1;", orderGuid);

        int ncounter = 0;
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext())
        {
            String s = getString(c,0);
            if (s.equals(itemGuid))
                break;
            ncounter++;

        }
        c.close();
        return ncounter;
    }
    /**
     * for line items display mode
     * @param orderGuid
     * @param itemGuid
     * @return
     */
    public int orderGetItemsNextCount(String orderGuid, String itemGuid)
    {
        if (itemGuid.isEmpty())
            return 0;
        String sql =String.format( "select items.guid from items,orders where items.orderguid='%s' and orders.bumped<>1;", orderGuid);

        int ncounter = 0;
        boolean bfindItem = false;
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext())
        {
            String s = getString(c,0);
            if (!bfindItem) {
                if (!s.equals(itemGuid))
                    continue;
                else
                    bfindItem = true;
            }
            else
            {
                ncounter++;
            }


        }
        c.close();
        return ncounter;
    }
    /************************************************************************/
/*
update the schedule item ready qty
*/
    /************************************************************************/
    boolean schedule_process_set_item_ready_qty( String itemGUID, float nQty)
    {
        if (itemGUID.isEmpty()) return false;
        String sql = String.format(("select id from scheduleprocess where itemguid='%s'"), itemGUID);
        int nIndex = this.executeOneValue(sql, -1);

        if (nIndex <0)
        {
            sql = String.format("insert into scheduleprocess(itemguid, ready) values('%s', %d)" , itemGUID, (int)nQty );

        }
        else
            sql = String.format("update scheduleprocess set ready=%d where itemguid='%s'", (int)nQty, itemGUID );
        this.executeDML(sql);
        return true;

    }

    boolean schedule_process_set_item_ready_qty( ScheduleProcessOrder order)
    {
        KDSDataItem item =  order.getItems().getItem(0);
        if (item == null) return false;
        return schedule_process_set_item_ready_qty(item.getGUID(), item.getScheduleProcessReadyQty());

    }

    float schedule_process_get_item_ready_qty(String itemGUID)
    {
        if (itemGUID.isEmpty()) return 0;
        String sql = String.format("select ready from scheduleprocess where itemguid='%s'", itemGUID);
        int n = this.executeOneValue(sql, 0);

        return n;
    }

    protected String orderGetTypeString(String orderGUID)
    {
        if (getDB() == null) return "";
        String sql;
        sql = String.format("select ordertype from orders where guid='%s'", orderGUID);
        Cursor c = getDB().rawQuery(sql, null);

        if (c.moveToNext())
        {
            String s = getString(c,0);
            c.close();
            if (s == null) return "";
            return s;
        }
        c.close();
        return "";

    }
    protected boolean is_schedule_order(String orderGUID)
    {
        String ordertype = orderGetTypeString(orderGUID);
        return (ordertype.equals(KDSDataOrder.ORDER_TYPE_SCHEDULE));
    }


    /**
     *
     * @param itemGUID
     * @return
     */
    int condimentGetCount(String itemGUID)
    {

        String sql = String.format("select count(*) from condiments where itemguid='%s'",itemGUID);
        return this.executeOneValue(sql);

    }

    int schedule_process_get_all_normal_item_unready_qty(String strCategory, String itemDescription)
    {

        if (getDB() == null) return 0;
        //String sql = String.format("select guid,orderguid,LocalBumped,qty,qtychanged from items where category='%s' and description='%s' " ,strCategory, itemDescription);
        String sql = String.format("select items.guid,items.orderguid,items.LocalBumped,items.qty,items.qtychanged from items,orders where category='%s' and description='%s' and orders.guid=items.orderguid and orders.bumped<>1 and items.BumpedStations=''",
                                    strCategory, itemDescription);

        Cursor c = getDB().rawQuery(sql, null);
        int ntotal = 0;
        ArrayList<String> arCounted = new ArrayList<>();//record counted item.

        while (c.moveToNext())
        {
            int nready = getInt(c,2); //ready boolean
            if (nready == 1) //item was marked , don't check again.
            {
                continue;
            }

            String orderGUID = getString(c, 1);
            if (!is_schedule_order(orderGUID))
            {
                String itemGUID = getString(c,0);// GetDaoRecordsetString(&q, _T("itemid"), _T(""));

                //check if there are condiments
                if (condimentGetCount(itemGUID)==0)
                {
                    int nqty = (int)getFloat(c,3);
                    int nqtychanged =(int) getFloat(c,4);
                    ntotal += nqty;
                    ntotal += nqtychanged;
                }
            }
        }
        c.close();
        return ntotal;

    }
    boolean schedule_process_item_delete(String itemGUID)
    {
        String sql = "delete from scheduleprocess where itemguid='" + itemGUID + "'";
        return this.executeDML(sql);
    }

    public boolean schedule_process_item_ready_qty_changed(KDSDataItem itemLatest)
    {
        String sql = String.format("update scheduleprocess set ready=%d where itemguid='%s'", itemLatest.getScheduleProcessReadyQty(), itemLatest.getGUID());
        return this.executeDML(sql);
    }

    public boolean schedule_process_item_ready_qty_changed(String orderName, String itemName, int nReadyQty)
    {
        if (getDB() == null) return false;
        String sql = String.format( "select items.guid from items,orders where orderguid=orders.guid and orders.name='%s' and items.name='%s'", orderName, itemName);
        Cursor c = getDB().rawQuery(sql, null);
        String guid = "";
        while (c.moveToNext())
        {
            guid = getString(c, 0);
        }
        if (guid.isEmpty()) return false;
        return schedule_process_set_item_ready_qty(guid, nReadyQty);
        //sql = String.format("update scheduleprocess set ready=%d where itemguid='%s'", nReadyQty,guid);
        //return this.executeDML(sql);
    }

    public void schedule_order_finished(String orderGuid)
    {
        ArrayList<String> arItems =  itemGetAllItemsGUID(this, orderGuid);
        orderDelete(orderGuid);
        for (int i=0; i< arItems.size(); i++)
        {
            schedule_process_item_delete(arItems.get(i));
        }

    }

    public boolean isEmpty()
    {
        String sql = "select count(*) from orders";
        return (this.executeOneValue(sql)==0);
    }

    public ArrayList<String> getUniqueItems(String strFilter)
    {
        String sql = "";
        if (strFilter.isEmpty())
            sql = "select description from items group by description order by description";
        else
        {
            sql = "select description from items where description like '%" + strFilter + "%' group by description order by description";
        }
        ArrayList<String> ar = new ArrayList<>();
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext())
        {
            ar.add(getString(c,0));
        }
        c.close();
        return ar;
    }

    public ArrayList<String> getUniqueCondiments(String strFilter)
    {
        String sql = "";
        if (strFilter.isEmpty())
            sql = "select description from condiments group by description order by description";
        else
        {
            sql = "select description from condiments where description like '%" + strFilter + "%' group by description order by description";
        }
        ArrayList<String> ar = new ArrayList<>();
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext())
        {
            ar.add(getString(c,0));
        }
        c.close();
        return ar;
    }

    public ArrayList<String> getUniqueDestinations(String strFilter)
    {
        String sql = "";
        if (strFilter.isEmpty())
            sql = "select dest from orders group by dest order by dest";
        else
        {
            sql = "select dest from orders where dest like '%" + strFilter + "%' group by dest order by dest";
        }
        ArrayList<String> ar = new ArrayList<>();
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext())
        {
            ar.add(getString(c,0));
        }
        c.close();
        return ar;
    }

    /**
     * NOTICE:
     *      The itemdelay value is wrong. It put the categorydelay and max prep item time together.
     *          this is for smart order.
     *      But, in preparation mode, we don't need to do this. So, will add new items, I use category delay to replace item delay value.
     * rev.
     *  Above notice revmoed: Save item/category delay independently.
     * @param order
     */
    public void prep_add_order_items(KDSDataOrder order)
    {
        int ncount = order.getItems().getCount();
        boolean b = this.startTransaction();
        for (int i = 0; i< ncount; i++)
        {
            KDSDataItem item = order.getItems().getItem(i);
            String sql = "insert into prepsort( orderguid,ItemName,Category,PrepTime,MaxItemName,finished,RealStartTime,ItemDelay, r0,r1) values(" ;
            sql += "'" + order.getGUID() +"'";
            sql += ",'" + KDSUtil.fixSqliteSingleQuotationIssue(item.getItemName()) +"'";
            sql += ",'" + item.getCategory() + "'";
            sql += "," + KDSUtil.convertFloatToString(item.getPreparationTime() + item.getModifiersTotalPrepTimeAsMins());
            sql += ",''";
            sql += ",0";
            sql += ",-1";
            //sql += "," + KDSUtil.convertFloatToString(item.getCategoryDelay()); //See notice
            sql += "," + KDSUtil.convertFloatToString(item.getItemDelay()); //
            sql += "," + KDSUtil.convertFloatToString(item.getCategoryDelay()); //
            sql += ",0";//started manullay: false
            sql += ")";
            this.executeDML(sql);
        }
        this.finishTransaction(b);

        PrepSorts prepSorts = prep_get_sort_items(order.getGUID());
        prep_save_sort_result(prepSorts);

        order.prep_set_sorts(prepSorts);

        //

    }

    /**
     * Rev.
     *  20200308:
     *      KP-50, In runner mode, we allow same catdelay value in different cateogry.
     *             Same catdelay will show in same time.
     *
     * kp1-25
     * @param order
     * @param smartItems
     */
//    public void smart_runner_category_init2(KDSDataOrder order, PrepSorts smartItems)
//    {
//        //kpp1-456, we init the first category here.
//        PrepSorts.PrepItem smartMaxItem = smartItems.findNextShowingItem(smartItems.m_arItems);
//        if (smartMaxItem == null) return;
//        //String category = smartMaxItem.Category;
//        String orderguid = order.getGUID();
//
//        //kp-50, same catdelay
//
//        ArrayList<String> arWillShowingCategory = smartItems.runnerGetAllSameCatDelayCategories(smartMaxItem.CategoryDelay);
//
//        smartRunnerCategoryAddShowingCategories(orderguid, arWillShowingCategory);
//        //
//        smartItems.runnerSetShowingCategory( smartCategoryGetShowingCategories(orderguid));
//    }

    public void smart_runner_category_init(KDSDataOrder order, PrepSorts smartItems)
    {
        //kpp1-456, we init the first category here.
        PrepSorts.PrepItem smartMaxItem = smartItems.findNextShowingItem(smartItems.m_arItems);
        if (smartMaxItem == null) return;
        //String category = smartMaxItem.Category;
        String orderguid = order.getGUID();

        //kp-50, same catdelay

        //ArrayList<String> arWillShowingCategory = smartItems.runnerGetAllSameCatDelayCategories(smartMaxItem.CategoryDelay);

        runnerSetLastShowingCatDelay(orderguid, smartMaxItem.CategoryDelay);
        //
        smartItems.runnerSetLastShowingCatDelay( smartMaxItem.CategoryDelay);
    }

    public void prep_set_real_started_time(String orderGuid, String itemName, float seconds)
    {
        String sql = String.format("update prepsort set RealStartTime=%d where orderguid='%s' and itemname='%s'", (int)seconds, orderGuid, itemName);
        this.executeDML(sql);
    }
    public void prep_set_item_finished(String orderGuid, String itemName, boolean bFinished)
    {
        String sql = String.format("update prepsort set finished=%d where orderguid='%s' and itemname='%s'", bFinished?1:0, orderGuid, itemName);
        this.executeDML(sql);
    }

    public PrepSorts prep_get_sort_items2(String orderGuid)
    {
        String sql = "";

        sql = "select orderguid,ItemName,Category,PrepTime,MaxItemName,finished,RealStartTime,ItemDelay,r0 from prepsort where orderguid='" + orderGuid +"'";


        PrepSorts prep = new PrepSorts();
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext())
        {
            PrepSorts.PrepItem item = new PrepSorts.PrepItem();
            item.orderguid = getString(c,0);
            item.ItemName = getString(c,1);
            item.Category = getString(c,2);
            item.PrepTime = getFloat(c,3);
            item.MaxItemName = getString(c,4);
            //item.WaitSecsToStart = c.getInt(5);
            item.finished = (getInt(c,5)==1);
            item.RealStartTime = getInt(c,6);
            item.ItemDelay = getFloat(c,7);
            item.CategoryDelay = getFloat(c,8);

            prep.add(item);

        }
        c.close();
        prep.sort();
        //kpp1-456
        //prep.runnerSetShowingCategory(smartCategoryGetShowingCategories(orderGuid));
        prep.runnerSetLastShowingCatDelay(runnerGetLastShowingCatDelay(orderGuid));
        return prep;
    }

    public PrepSorts prep_get_sort_items(String orderGuid)
    {
        String sql = "";

        sql = "select orderguid,ItemName,Category,PrepTime,MaxItemName,finished,RealStartTime,ItemDelay,r0,r1 from prepsort where orderguid='" + orderGuid +"'";


        PrepSorts prep = new PrepSorts();
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext())
        {
            PrepSorts.PrepItem item = new PrepSorts.PrepItem();
            item.orderguid = getString(c,0);
            item.ItemName = getString(c,1);
            item.Category = getString(c,2);
            item.PrepTime = getFloat(c,3);
            item.MaxItemName = getString(c,4);
            //item.WaitSecsToStart = c.getInt(5);
            item.finished = (getInt(c,5)==1);
            item.RealStartTime = getInt(c,6);
            item.ItemDelay = getFloat(c,7);
            item.CategoryDelay = getFloat(c,8);
            item.ItemStartedManually = (getInt(c, 9) == 1);
            prep.add(item);

        }
        c.close();
        prep.sort();
        //kpp1-456
        prep.runnerSetLastShowingCatDelay(smartRunnerGetCatDelay(orderGuid));
        return prep;
    }

    public boolean prep_save_sort_result(PrepSorts sorts)
    {
        if (sorts.count() <=0) return true;
        PrepSorts.PrepItem item = sorts.m_arItems.get(0);
        String sql = item.sqlDelOrder();
        this.executeDML(sql);
        boolean b = this.startTransaction();
        for (int i=0; i< sorts.m_arItems.size(); i++)
        {
            item = sorts.m_arItems.get(i);
            sql = item.sqlNew();
            this.executeDML(sql);
        }
        this.finishTransaction(b);

        return true;


    }

    public void prep_remove(String orderGuid)
    {
        String sql = String.format("delete from prepsort where orderguid='%s'", orderGuid);
        this.executeDML(sql);
    }


    public KDSDataModifiers modifiersGet(String itemGUID)// int nItemID)
    {

        KDSDataModifiers modifiers = new KDSDataModifiers();
        if (getDB() == null) return modifiers;

        String sql = String.format("select GUID,Name,Description,BG,FG,PrepTime from modifiers where itemguid='%s' order by id", itemGUID);
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {

            KDSDataModifier m = modifierGet(c);
            m.setItemGUID(itemGUID);
            modifiers.addComponent(m);
        }
        c.close();
        return modifiers;
    }

    private KDSDataModifier modifierGet(Cursor sf) {

        String guid = getString(sf,0);
        KDSDataModifier  c = new KDSDataModifier();
        c.setGUID(guid);

        c.setCondimentName(getString(sf,1));
        c.setDescription(getString(sf,2));
        c.setBG(getInt(sf,3));
        c.setFG(getInt(sf,4));
        c.setPrepTime(getInt(sf,5));

        return c;
    }

    int modifiersGetCount(String itemGUID)
    {

        String sql = String.format("select count(*) from modifiers where itemguid='%s'",itemGUID);
        return this.executeOneValue(sql);

    }

    private boolean modifiersAdd(KDSDataModifiers modifiers) {
        int ncount = modifiers.getCount();
        String sql = "";
        try {
            for (int i = 0; i < ncount; i++) {
                KDSDataModifier modifier = (KDSDataModifier) modifiers.getComponent(i);
                sql = modifier.sqlAddNew("modifiers");
                if (!this.executeDML(sql)) return false;

                messagesAdd(modifier.getMessages());

            }
            return true;
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }

    public boolean modifierUpdate(KDSDataModifier modifier) {
        String sql = modifier.sqlUpdate();
        boolean b= this.executeDML(sql);
        if (b)
            updateDbTimeStamp();
        return b;
    }


    private  class GuidName
    {
        public String guid = "";
        public String name = "";
        public  int bumped = 0;

    }

    private ArrayList<String> modifiersGetAllGuidByItems(KDSDBCurrent db,ArrayList<String> arItemGuid )
    {
        ArrayList<String> arcondiments = new ArrayList<String>();
        if (db.getDB() == null) return arcondiments;

        for (int i=0; i< arItemGuid.size(); i++) {
            String itemguid = arItemGuid.get(i);
            String sql = String.format("select guid from modifiers where itemguid='%s'", itemguid);
            Cursor c = db.getDB().rawQuery(sql, null);
            while (c.moveToNext()) {
                arcondiments.add(getString(c,0));
            }
            c.close();
        }
        return arcondiments;

    }

    public void removeLineItemOfQtyChange(String parentGuid)
    {
        String sql = String.format("delete from items where r1='%s'",parentGuid);
        getDB().execSQL(sql);

    }

    /**
     * 2.0.34
     *      We have a customer want to use queue display with multiple prep stations without using the Expo. We suggest them to use queue-expo in ID mode with auto bump. They accept it but with one change: ability to sort order by Status update time in which the order sort by when this order arrive this status column. E.G.: right all order is sort by status update time descend, with order 1,2,3,4,5. If order 2 complete first then 5 then 1, the ready will be 2,5,1.
     *      If user change the sorting to sort by status update ascend, then the ready will be 1,5,2 with first arrive on the bottom. So we will need to add a new sorting option in ID mode in each column.
     *
     *      I use r2 to save this time.
     * @param orderGuid
     */
    public void orderUpdateQueueStateTime(String orderGuid)
    {
        String sql = String.format("update orders set r0=datetime('now','localtime') where guid='%s' ", orderGuid);

        getDB().execSQL(sql);

    }

    /**
     * 2.0.34
     * @param orderGuid
     * @return
     */
    public Date orderGetQueueStateTime(String orderGuid)
    {
        String sql = String.format("select r0 from orders where guid='%s'", orderGuid);
        Cursor c = getDB().rawQuery(sql, null);

        String s = "";
        while (c.moveToNext()) {
            s = c.getString(0);
        }
        c.close();

        return KDSUtil.convertStringToDate(s);

    }

    public String itemGetOrderGuid(String itemGuid)
    {
        String sql = String.format("select orderguid from items where guid='%s'", itemGuid);
        Cursor c = getDB().rawQuery(sql, null);

        String s = "";
        while (c.moveToNext()) {
            s = c.getString(0);
        }
        c.close();

        return s;
    }

    /**
     * 2.0.50
     * save customer information for sms feature
     * use R1, R2 field
     *
     * @param customerID
     * @param customerPhone
     */
    public void setSMSInfo(String orderGuid, String customerID, String customerPhone)
    {
        String sql = String.format("update orders set r1='%s',r2='%s' where guid='%s'",customerID, customerPhone, orderGuid);

        getDB().execSQL(sql);

    }

    /**
     * 2.0.50
     * record last order sms state to db
     * User R3 field
     *
     * @param orderGuid
     * @param nState
     *  Record this state has been send to server.
     */
    public void setSMSState(String orderGuid, int nState)
    {
        String sql = String.format("update orders set r3='%s' where guid='%s'",KDSUtil.convertIntToString(nState), orderGuid);

        getDB().execSQL(sql);
    }

    /**
     *     //for SMS. If no expo existed, use it to record which has bumped/(items bumped).
     //format:
     //stationID\nAllDone, stationID\nAllDone
     //save to database order table, r4.
     * @param orderGuid
     * @param strStationsState
     */
    public void setSMSStationsState(String orderGuid, String strStationsState)
    {
        String sql = String.format("update orders set r4='%s' where guid='%s'",strStationsState, orderGuid);

        getDB().execSQL(sql);
    }

    /**
     * use sql to delete order quickly.
     * @param guid
     * @return
     */
    public boolean orderDeleteQuick(String guid)
    {

        if (getDB() == null) return false;

        String sql = KDSDataOrder.sqlDelete("orders", guid);
        if (!this.executeDML(sql))
            return false;

        //remove order messages.
        sql = "delete from messages where ObjType=0 and ObjGUID='" + guid + "'";
        if (!this.executeDML(sql))
            return false;

//        //remove items modifiers
        sql = String.format("delete from modifiers where modifiers.ItemGUID in (select guid from items where items.orderguid='%s')", guid);
        if (!this.executeDML(sql)) return false;
        //remove items messages
        sql = String.format("delete from messages where ObjType=1 and messages.ObjGUID in (select guid from items where items.orderguid='%s')", guid);
        if (!this.executeDML(sql))
            return false;
//        //remove condiments messages
        sql = String.format("delete from messages where ObjType=2 and messages.ObjGUID in (select condiments.guid from condiments,items where condiments.itemguid=items.guid and items.orderguid='%s')", guid);
        if (!this.executeDML(sql))
            return false;
//
//        //remove condiments
        sql = String.format("delete from condiments where condiments.itemguid in (select guid from items where items.orderguid='%s')", guid);
        if (!this.executeDML(sql))
            return false;

        //remove items
        sql = String.format("delete from items where items.orderguid='%s'", guid);
        if (!this.executeDML(sql))
            return false;

        updateDbTimeStamp();

        prep_remove(guid);

        return true;
    }

    /**
     * use sql to delete order quickly.
     * @param sqlOrderGuid
     *  The guid what fit to delete condition
     * @return
     */
    public boolean orderDeleteQuickBatch(String sqlOrderGuid)
    {

        if (getDB() == null) return false;
        String sql = "";
        //move them to bottom.
//        String sql = "delete from orders where guid in ("+sqlOrderGuid +")";// KDSDataOrder.sqlDelete("orders", guid);
//        if (!this.executeDML(sql))
//            return false;

        //remove order messages.
        //sql = "delete from messages where ObjType=0 and ObjGUID='" + guid + "'";
        sql = String.format("delete from messages where ObjType=0 and (ObjGUID in (%s))", sqlOrderGuid );
        if (!this.executeDML(sql))
            return false;

//        //remove items modifiers
        //sql = String.format("delete from modifiers where modifiers.ItemGUID in (select guid from items where items.orderguid='%s')", guid);
        sql = String.format("delete from modifiers where modifiers.ItemGUID in (select items.guid from items where items.orderguid in (%s))", sqlOrderGuid);
        if (!this.executeDML(sql)) return false;
        //remove items messages
        sql = String.format("delete from messages where ObjType=1 and messages.ObjGUID in (select guid from items where items.orderguid in (%s))", sqlOrderGuid);
        if (!this.executeDML(sql))
            return false;
//        //remove condiments messages
        sql = String.format("delete from messages where ObjType=2 and messages.ObjGUID in (select condiments.guid from condiments,items where condiments.itemguid=items.guid and (items.orderguid in (%s) ))", sqlOrderGuid);
        if (!this.executeDML(sql))
            return false;
//
//        //remove condiments
        sql = String.format("delete from condiments where condiments.itemguid in (select guid from items where items.orderguid in (%s))", sqlOrderGuid);
        if (!this.executeDML(sql))
            return false;

        //remove items
        sql = String.format("delete from items where items.orderguid in (%s)", sqlOrderGuid);
        if (!this.executeDML(sql))
            return false;

        updateDbTimeStamp();
        sql = String.format("delete from prepsort where orderguid in (%s)", sqlOrderGuid);
        if (!this.executeDML(sql))
            return false;

        // MUST delete orders at last.
        sql = "delete from orders where guid in ("+sqlOrderGuid +")";// KDSDataOrder.sqlDelete("orders", guid);
        if (!this.executeDML(sql))
            return false;
        return true;
    }


  /**
     *
     * @param arChangedOrders
     *  format:
     *      guid,order name, queue_ready, bumped_item_name,...
     *      guid,order name, queue_ready, bumped_item_name,...
     *      guid,order name, queue_ready, bumped_item_name,...
     *      guid,order name, queue_ready, bumped_item_name,...
     * @return
     */
    public void queueSetOrderItemsBumped(ArrayList<String> arChangedOrders)
    {

        boolean bFromMe =  this.startTransaction();
        try {


            for (int i = 0; i < arChangedOrders.size(); i++) {
                String s = arChangedOrders.get(i);
                if (s.isEmpty()) continue;
                queueSetSingleOrderItemsBumped(s);
            }
        }
        catch (Exception e)
        {

        }
        finally {
            this.finishTransaction(bFromMe);
        }




    }


    /**
     *
     * @param strOrderItemsBumpedInfo
     * Format:
     *      guid,order_id,queue_double_bump_ready,bumped_item_id,bumped_item_id /n
     * @return
     *  Order guid value.
     */
    private String queueSetSingleOrderItemsBumped(String strOrderItemsBumpedInfo)
    {
        ArrayList<String> ar = KDSUtil.spliteString(strOrderItemsBumpedInfo, ",");
        if (ar.size() <3) return "";
        String guid = ar.get(0);
        if (guid.isEmpty()) return "";
        String orderName = ar.get(1);
        String queueReady = ar.get(2);

        ar.remove(2);
        ar.remove(1);
        ar.remove(0);

        this.orderSetQueueReady(guid,queueReady.equals("1"));

        //left over is items id
        for (int i=0; i< ar.size(); i++)
        {
            String itemName = ar.get(i);
            if (itemName.isEmpty()) continue;
            if (itemName.equals("-1"))
            {//all bumped
                String sql = "update items set localbumped=1 where orderguid='" + guid +"'";

                this.executeDML(sql);

            }
            else {
                String sql = "update items set localbumped=1 where orderguid='" + guid + "' and name='" + itemName + "'";
                this.executeDML(sql);

            }
        }
        return guid;

    }

    /**
     *
     * @param itemCompare
     *  guid will compare
     * @param itemBase
     *  guid compare to this one.
     * @return
     */
    private boolean isSameCondiments(String itemCompare, String itemBase, int nCondimentsCount) {


        if (getDB() == null) return false;
        String sql = "";
        sql = String.format("select count(*) from condiments where itemguid='%s' and description in (select description from condiments where itemguid='%s')",
                            itemCompare, itemBase );

        int nCount = this.executeOneValue(sql);
        return (nCondimentsCount == nCount);

    }

    private int condimentsCount(String itemGuid)
    {
        if (getDB() == null) return 0;
        String sql = "";
        sql = String.format("select count(*) from condiments where itemguid='%s'",
                            itemGuid);

        int nCount = this.executeOneValue(sql);
        return nCount;
    }

    /**
     * Set all USER_B to USER_A, as the split screen settings changed.
     * KPP1-195
     * @return
     */
    public boolean setAllActiveOrdersToUserA()
    {
        String sql = "";
        sql = String.format("update orders set screen=0 where screen=1 and bumped=0" );
        return this.executeDML(sql);
    }


    /**
     * KPP1-415
     * Condiment only summary
     *
     * @param nUser
     *
     * @param bAscend
     * @return
     */
    public ArrayList<KDSSummaryItem> summaryOnlyCondiments(int nUser, boolean bAscend, boolean bSummaryStation)//, boolean bEnableSummaryTranslation)
    {

        String sql = String.format("select condiments.description,sum(ifnull(condiments.qty,1)*(items.qty+ifnull(items.qtychanged,0))) as q,count(condiments.description) as samecount " +
                "from condiments,items " +
                "where condiments.itemguid=items.guid and items.localbumped=0 and items.marked=0 and condiments.itemguid in (select items.guid from items where items.orderguid in (select orders.guid from orders where orders.bumped=0 and orders.parked=0 and screen=%d)) " +
                "group by condiments.description " +
                "order by condiments.description %s ",
                nUser,  bAscend?"asc":"desc");
        if (bSummaryStation)
            sql = String.format("select condiments.description,sum(ifnull(condiments.qty,1)*(items.qty+ifnull(items.qtychanged,0))) as q,count(condiments.description) as samecount " +
                            "from condiments,items " +
                            "where condiments.itemguid=items.guid and items.localbumped=0 and items.bumpedstations='' and items.marked=0 "+//and condiments.itemguid in (select items.guid from items where items.orderguid in (select orders.guid from orders where orders.bumped=0 and orders.parked=0 and screen=%d)) " +
                            "group by condiments.description " +
                            "order by condiments.description %s ",
                            bAscend?"asc":"desc");

        ArrayList<KDSSummaryItem> ar = new ArrayList<>();

        Cursor c = getDB().rawQuery(sql, null);

        String description = "";
        int qty = 0;
        int count = 0;
        while (c.moveToNext()) {
            description = c.getString(0);
            qty = getInt(c, 1, 0);
            count = getInt(c, 2, 0);
            KDSSummaryItem item = new KDSSummaryItem();
            item.setDescription(description);
            qty = (qty>0?qty:count);
            item.setQty( qty );
            ar.add(item);
        }
        c.close();
        return ar;
    }

//    final String SMART_CATEGORY_SEPERATOR = "\n";
//    /**
//     * kpp1-456
//     * @param orderGuid
//     * @param categoryName
//     */
//    public void smartRunnerCategoryAddShowingCategory(String orderGuid, String categoryName)
//    {
//        ArrayList<String> ar = smartCategoryGetShowingCategories(orderGuid);
//        if (KDSUtil.isExistedInArray(ar, categoryName))
//            return;
//        ar.add(categoryName);
//        String s = KDSUtil.stringArrayToString(ar, SMART_CATEGORY_SEPERATOR);
//        String sql = String.format("update orders set trackerid='%s' where guid='%s'", s, orderGuid);
//        this.executeDML(sql);
//
//    }

//    /**
//     * kpp1-456
//     * @param orderGuid
//     * @return
//     */
//    public ArrayList<String> smartCategoryGetShowingCategories(String orderGuid)
//    {
//        String sql = String.format("select trackerid from orders where guid='%s'", orderGuid);
//        ArrayList<String> ar = new ArrayList<>();
//
//        Cursor c = getDB().rawQuery(sql, null);
//
//        String s = "";
//
//
//        while (c.moveToNext()) {
//            s = c.getString(0);
//        }
//        c.close();
//        if (s.isEmpty()) return ar;
//        ar = KDSUtil.spliteString(s, SMART_CATEGORY_SEPERATOR);
//
//        return ar;
//    }

    public float runnerGetLastShowingCatDelay(String orderGuid)
    {
        String sql = String.format("select trackerid from orders where guid='%s'", orderGuid);
        ArrayList<String> ar = new ArrayList<>();

        Cursor c = getDB().rawQuery(sql, null);

        String s = "";


        while (c.moveToNext()) {
            s = c.getString(0);
        }
        c.close();
        if (s.isEmpty()) return 0;
        return KDSUtil.convertStringToFloat(s, 0);
//        ar = KDSUtil.spliteString(s, SMART_CATEGORY_SEPERATOR);
//
//        return ar;
    }

//
//    /**
//     * KP-50
//     * @param orderGuid
//     * @param categoriesName
//     */
//    public void smartRunnerCategoryAddShowingCategories(String orderGuid, ArrayList<String> categoriesName)
//    {
//        ArrayList<String> ar = smartCategoryGetShowingCategories(orderGuid);
//        boolean bChanged = false;
//        for (int i=0; i< categoriesName.size(); i++) {
//            if (KDSUtil.isExistedInArray(ar, categoriesName.get(i)))
//                continue;
//            ar.add(categoriesName.get(i));
//            bChanged = true;
//        }
//        if (!bChanged) return;
//
//        String s = KDSUtil.stringArrayToString(ar, SMART_CATEGORY_SEPERATOR);
//        String sql = String.format("update orders set trackerid='%s' where guid='%s'", s, orderGuid);
//        this.executeDML(sql);
//
//    }

    public int removeOrdersForSumStation(Vector<Object> arRemovedOrders)
    {
        if (arRemovedOrders.size() <=0) return 0;
        //boolean b = this.startTransaction();
        for (int i=0; i< arRemovedOrders.size(); i++)
        {
            this.orderDeleteQuick( ((KDSDataOrder)arRemovedOrders.get(i)).getGUID());

        }
        //this.commitTransaction(b);
        return arRemovedOrders.size();
    }

    public float smartRunnerGetCatDelay(String orderGuid)
    {
        String sql = String.format("select trackerid from orders where guid='%s'", orderGuid);
        //ArrayList<String> ar = new ArrayList<>();

        Cursor c = getDB().rawQuery(sql, null);

        String s = "";


        while (c.moveToNext()) {
            s = c.getString(0);
        }
        c.close();
        if (s.isEmpty()) return 0;
        return KDSUtil.convertStringToFloat(s, 0);

    }
    public void runnerSetLastShowingCatDelay(String orderGuid, float fltCatDelay)
    {
        //ArrayList<String> ar = smartCategoryGetShowingCategories(orderGuid);
        //boolean bChanged = false;
        //for (int i=0; i< categoriesName.size(); i++) {
//        //    if (KDSUtil.isExistedInArray(ar, categoriesName.get(i)))
//                continue;
//            ar.add(categoriesName.get(i));
//            bChanged = true;
//        }
//        if (!bChanged) return;

        String s = KDSUtil.convertFloatToString(fltCatDelay);
        String sql = String.format("update orders set trackerid='%s' where guid='%s'", s, orderGuid);
        this.executeDML(sql);

    }


    /**
     * KP-64 When adding items to an order- no catdelay
     * we add smart items to table before do modifying.
     *  So, change its guid to existed order after modifying.
     * @param guidReceivedOrder
     * @param orderExisted
     */
    public void prep_change_modify_order_guid_to_existed_guid(String guidReceivedOrder, KDSDataOrder orderExisted)
    {

        String sql = String.format( "update prepsort set orderguid='%s' where orderguid='%s'", orderExisted.getGUID(),
                                        guidReceivedOrder);


        this.executeDML(sql);
        PrepSorts prepSorts = prep_get_sort_items(orderExisted.getGUID());
        prep_save_sort_result(prepSorts);

        orderExisted.prep_set_sorts(prepSorts);

        //

    }

    public void orderSetInputMessage(String orderGuid, String msg)
    {
        String sql = String.format("update orders set inputmsg='%s' where guid='%s'", msg, orderGuid);
        this.executeDML(sql);
        updateDbTimeStamp();
    }

    public String orderGetInputMessage(String orderGuid)
    {
        String sql = "select inputmsg from orders where guid='" + orderGuid +"'";

        if (getDB() == null) return "";

        Cursor c = getDB().rawQuery(sql, null);
        String msg = "";
        if (c.moveToNext()) {
            msg = getString(c,0);

        }
        c.close();
        return msg;
    }

    public void smart_set_item_started(String orderGuid, String itemName, boolean bStarted)
    {
        String sql = String.format("update prepsort set r1=%d where orderguid='%s' and itemname='%s'", bStarted?1:0, orderGuid, itemName);
        this.executeDML(sql);
    }

    public boolean itemDelete(String itemGuid)
    {
        String sql = String.format("delete from items where guid='%s", itemGuid);
        this.executeDML(sql);
        sql = String.format("delete from condiments where itemguid='%s", itemGuid);
        this.executeDML(sql);

        sql = String.format("delete from messages where objguid='%s", itemGuid);
        this.executeDML(sql);

        sql = String.format("delete from modifiers where itemguid='%s", itemGuid);
        this.executeDML(sql);

        return true;

    }

    /**
     * kp-126
     * print item when item was bumped.
     * Save its printed state.
     * @param itemGuid
     */
    public void itemSetPrinted(String itemGuid, boolean bPrinted)
    {
        String sql = String.format("update items set r6=%d where guid='%s'",bPrinted?1:0, itemGuid);
        this.executeDML(sql);
    }

    /**
     * kp-126 Print bumped item.
     * @param itemGuid
     * @return
     */
    public boolean itemGetPrinted(String itemGuid)
    {
        String sql = String.format("select r6 from items where guid='%s'", itemGuid);
        if (getDB() == null) return false;

        Cursor c = getDB().rawQuery(sql, null);
        int n = 0;
        if (c.moveToNext()) {
             n = getInt(c,0);

        }
        c.close();
        return (n==1);
    }
    /***************************************************************************
     * SQL definitions
     *
     * NOTICE:
     *      Please add new field to the end of table.!!!!!!!!
     */
    //without the last ")", for statistic db sql.
    public static final String Table_Orders_Fields = "Create table Orders ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "GUID text(36)," //76895313-839E-4E89-BAFC-B253BFF3173F, the only GUID in whole system, assign it while receive this order.
            + "Name text(256),"
            + "Waiter text(128),"
            + "Start date," //start time
            + "ToTbl text(64),"
            + "Station text(16),"
            + "Screen int,"
            + "POS text(16),"
            + "OrderType text(64),"
            + "Dest text(128),"
            + "CustMsg text(256),"
            + "Parked int,"
            + "IconIdx int,"
            + "EvtFired int,"
            + "PrepStart date," //when start to cook this order, for smart order
            + "OrderDelay float," //delay some time after receive this order.
            + "Status int,"
            + "SortIdx int,"
            + "Bumped int default 0," //bumped or not
            + "BumpedTime date default '',"
            + "FromPrimary int default 0,"//in backup station, this order is from primary station. 1, 0.

            +"r0 text(16)," //2.0.34, use it for queue status sort.
            +"r1 text(16)," //2.0.50, for sms customer id
            +"r2 text(16)," //2.0.50 for sms customer phone number
            +"r3 text(16)," //2.0.50 for sms state.//-1=unknown, 0 = new, 1 = prepared, 2 = done
            +"r4 text(16)," //2.1.15, for sms, save original order go to which stations.
            +"r5 text(16)," //for customer, same the customer name
            +"r6 text(16)," //kdsguid, identify same order in whole KDS.
            +"r7 text(16)," //kp-48, Allergen xml tags. <HeaderFooterMessage>
            +"r8 text(16)," //kp-103, auto unpark order date value
            +"r9 text(16),"
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')),"
            + "QueueMsg text(256), "// )";
            + "TrackerID text(16)," //As TT was removed, I use this to do kpp1-456, "Runner" station. Save showing cateogry name.
            + "PagerID text(16),"//for table-tracker
            + "CookState int default 0,"
            + "SosReady int default 0, "
            + "inputmsg text(16)," //kp-114 add fields
            + "r10 text(16),"
            + "r11 text(16),"
            + "r12 text(16),"
            + "r13 text(16),"
            + "r14 text(16),"
            + "r15 text(16),"
            + "r16 text(16),"
            + "r17 text(16),"
            + "r18 text(16),"
            + "r19 text(16)";



    private static final String Table_Orders = Table_Orders_Fields + ")";
//
//            "Create table Orders ("
//            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
//            + "GUID text(36)," //76895313-839E-4E89-BAFC-B253BFF3173F, the only GUID in whole system, assign it while receive this order.
//            + "Name text(256),"
//            + "Waiter text(128),"
//            + "Start date," //start time
//            + "ToTbl text(64),"
//            + "Station text(16),"
//            + "Screen int,"
//            + "POS text(16),"
//            + "OrderType text(64),"
//            + "Dest text(128),"
//            + "CustMsg text(256),"
//            + "Parked int,"
//            + "IconIdx int,"
//            + "EvtFired int,"
//            + "PrepStart date," //when start to cook this order, for smart order
//            + "OrderDelay float," //delay some time after receive this order.
//            + "Status int,"
//            + "SortIdx int,"
//            + "Bumped int default 0," //bumped or not
//            + "BumpedTime date default '',"
//            +"r0 text(16),"
//            +"r1 text(16),"
//            +"r2 text(16),"
//            +"r3 text(16),"
//            +"r4 text(16) ,"
//            +"r5 text(16),"
//            +"r6 text(16),"
//            +"r7 text(16),"
//            +"r8 text(16),"
//            +"r9 text(16) ,"
//            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')) )";


    public static final String Table_Items = "Create table Items ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "OrderGUID text(36),"
            + "GUID text(36),"
            + "Name text(256),"
            + "Description text(256),"
            + "Qty float,"
            + "QtyChanged float," //modify original qty by this value
            + "Category text(128),"
            + "BG long,"
            + "FG long,"
            + "Grp int,"
            + "Marked int," //unused field
            + "DeleteByRemote int," //xml command delete this item
            + "LocalBumped int," //local operator bump this item.
            + "BumpedStations text(256)," //for expeditor station
            + "ToStations text(256)," //tostations
            + "Ready int,"
            + "Hiden int," //it is hidden in this station.
                            // <HideStation>1,3,4</HideStation>
                            //HideStation:don't show this item description in those station. But show condiments
            + "PreparationTime float," //how soon chef can finish this item after cook. For smart order
            + "ItemDelay float," //delay some time after order start to cook.. For smart order
            + "ItemType int," //20160106 for exp item
            + "BuildCard text(128),"
            + "TrainingVideo text(128),"
            + "SumTransEnable int,"//for summary translate
            + "SumTrans text(128),"
            +"r0 text(16)," //item timer delay.add increase qty to new line(LineItems mode). And, the timer from qty changed.
            +"r1 text(16)," //parent item guid, this is for lineitem mode. After add new line item of qty change, I use this field to record its parent.
            +"r2 text(16)," //2.0.47, category priority,
            +"r3 text(16),"  //KPP1-53, This needs to show the number of the station the order/item was transferred from. IN the case above it would be 1.
            +"r4 text(16) ," //KPP1-64, item_bump_guid, 1. This is almost correct. The guid from item_bumps should be unique. This should be a random guid. This should not be the same guid as the items table guid.
                                //2. The items guid should be a random guid. The item_bump table guid should be a random guid.
                                //3. The random guid from the item_bumps table should be inserted into the correct Linked item in the items table column "item_bump_guid"
            +"r5 text(16)," //printable
            +"r6 text(16)," // save if item has been printed.
            +"r7 text(16),"
            +"r8 text(16),"
            +"r9 text(16) ,"
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')) )";


    public static final String Table_Condiments = "Create table Condiments ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "ItemGUID text(36),"
            + "GUID text(36),"
            + "Name text(256),"
            + "Description text(256),"
            + "Qty float,"
            + "BG long,"
            + "FG long,"
            + "Hiden int,"
            + "Bumped int,"
            +"r0 text(16),"
            +"r1 text(16),"
            +"r2 text(16),"
            +"r3 text(16),"
            +"r4 text(16) ,"
            +"r5 text(16),"
            +"r6 text(16),"
            +"r7 text(16),"
            +"r8 text(16),"
            +"r9 text(16) ,"
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')) )";

    //
    public static final String Table_Messages = "Create table Messages ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "GUID text(36)," //it can been item or order id
            + "ObjGUID text(36),"
            + "ObjType int," //0 order, 1 item, 2 condiment
            + "Description text(256),"
            + "BG long,"
            + "FG long,"
            +"r0 text(16),"
            +"r1 text(16),"
            +"r2 text(16),"
            +"r3 text(16),"
            +"r4 text(16) ,"
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')) )";


    //for backup the database chanages.
    //While the primary restore, we will send all these sql to primary stations.
    private static final String Table_Sql = "Create table BufferedSql ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "sql text(256),"
            +"r0 text(16),"
            +"r1 text(16),"
            +"r2 text(16),"
            +"r3 text(16),"
            +"r4 text(16),"
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')) )";


    private static final String Table_ScheduleProcess = "create table scheduleprocess(" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "itemguid text(36), " +
            "ready float, " +
            "r0 text(20)," +
            "r1 text(20), " +
            "r2 text(20)," +
            "r3 text(20), " +
            "r4 text(20) )";


    /**
     * Full order is here. Include other stations item.
     * It is for preparation time mode.
     */
    private static final String Table_PrepSort = "create table prepsort(" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "orderguid text(36), " +
            "ItemName text(256)," +
            "Category text(256)," +
            "PrepTime float," +
            "ItemDelay float," +
            "MaxItemName text(256)," + //the maxitem name
            "finished int," + //identify if this item finihsed
            "RealStartTime int," +//the real time that this item start to cook. (seconds from order started).
            "r0 text(20)," + //Save category delay here.
            "r1 text(20), " + //kp-121, runner start it manually.
            "r2 text(20)," +
            "r3 text(20), " +
            "r4 text(20) )";


    public static final String Table_Modifiers = "Create table Modifiers ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "ItemGUID text(36),"
            + "GUID text(36),"
            + "Name text(256),"
            + "Description text(256),"
            + "Qty float,"
            + "BG long,"
            + "FG long,"
            + "Hiden int,"
            + "Bumped int,"
            + "PrepTime int,"
            +"r0 text(16),"
            +"r1 text(16),"
            +"r2 text(16),"
            +"r3 text(16),"
            +"r4 text(16) ,"
            +"r5 text(16),"
            +"r6 text(16),"
            +"r7 text(16),"
            +"r8 text(16),"
            +"r9 text(16) ,"
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')) )";


    private static final String CreateInx_Orders_Guid = "create index guidorders on orders(guid)";
    private static final String CreateInx_Items_Guid = "create index guiditems on Items(orderguid,guid)";

    private static final String CreateInx_Condiments_Guid = "create index guidcondiments on Condiments(itemguid,guid)";

    private static final String CreateInx_Messages_Guid = "create index guidmsg on Messages(objguid,guid)";
    private static final String CreateInx_PrepSort = "create index prepinx on PrepSort(orderguid,ItemName)";




}
