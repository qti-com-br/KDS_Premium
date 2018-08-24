package com.bematechus.kds;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.bematechus.kdslib.ConditionBase;
import com.bematechus.kdslib.ConditionOneTime;
import com.bematechus.kdslib.ConditionStatistic;
import com.bematechus.kdslib.DateSlots;
import com.bematechus.kdslib.KDSDBBase;
import com.bematechus.kdslib.KDSDataCondiment;
import com.bematechus.kdslib.KDSDataCondiments;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataItems;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.ReportOrderDaily;
import com.bematechus.kdslib.ReportOrderMonthly;
import com.bematechus.kdslib.ReportOrderOneTime;
import com.bematechus.kdslib.ReportOrderWeekly;
import com.bematechus.kdslib.SOSReportCondition;
import com.bematechus.kdslib.SOSReportOneStation;
import com.bematechus.kdslib.SOSReportTimeSlotData;
import com.bematechus.kdslib.TimeSlotEntry;
import com.bematechus.kdslib.TimeSlotEntryDetail;
import com.bematechus.kdslib.TimeSlotOrderReport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2015/7/29 0029.
 */
public class KDSDBStatistic extends KDSDBBase {

    static private final String TAG = "KDSDBStatistic";
    static public final int DB_VERSION = 8;
    /** History
     *  8: add index to this database.
     */
    static public final String DB_NAME = "statistic.db";
    //static public final String SQL_SEPERATOR = "\f";

    /* >>>>>>>>>>>>>>IMPORTANT<<<<<<<<<<<<<<<<
    * Please make sure this stirng is same as statistic app string.
    * I will not send fields in sql
    */
    final static String INSERT_INTO_ORDERS_FIELDS = "insert into orders(GUID,Name,Waiter,Start,ToTbl,Station,Screen,POS,OrderType,Dest,CustMsg,Parked,IconIdx,EvtFired,PrepStart,OrderDelay,Status,SortIdx,Bumped,BumpedTime,FromPrimary,FinishedTime,r0,r1,r2,r3,r4,r5,r6,r7,r8,r9,StationTimeStamp,FromStation) values(";
    final static String INSERT_INTO_ORDERS = "#@$ORDERS$@#";
    final static String INSERT_INTO_ITEMS_FIELDS = "insert into items(OrderGUID,GUID,Name,Description,Qty,QtyChanged,Category,BG,FG,Grp,Marked,DeleteByRemote,LocalBumped,BumpedStations,ToStations,Ready,Hiden,PreparationTime,ItemDelay,ItemType,r0,r1,r2,r3,r4,r5,r6,r7,r8,r9,DBTimeStamp) values(";
    final static String INSERT_INTO_ITEMS = "#@$ITEMS$@#";


    public KDSDBStatistic(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
        super(context, name, factory, version);
    }
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String ar[] = new String[]{
                Table_Orders,
                Table_Items,
                Table_Condiments,
                Table_Messages,
                CreateInx_Orders_Guid,
                CreateInx_Items_Guid,
                CreateInx_Condiments_Guid,
                CreateInx_Messages_Guid,
                CreateInx_Order_Finished_Time
        };
        exeBatchSql(db, ar);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

        changeTableName(db,"orders", "orders1");
        changeTableName(db,"items", "items1");
        changeTableName(db,"condiments", "condiments1");
        changeTableName(db,"messages", "messages1");

        dropIndex(db, "guidorders");
        dropIndex(db, "guiditems");
        dropIndex(db, "guidcondiments");
        dropIndex(db, "guidmsg");


        onCreate(db);

        loadOldData(db);

    }


    public void loadOldData(SQLiteDatabase db)
    {
        copyData(db,"orders", "orders1");
        copyData(db,"items", "items1");
        copyData(db,"condiments", "condiments1");
        copyData(db,"messages", "messages1");

        clearOldData(db);

    }
    public void clearOldData(SQLiteDatabase db)
    {
        dropTable(db, "orders1");
        dropTable(db, "items1");
        dropTable(db, "condiments1");
        dropTable(db, "messages1");
    }

    public int getVersion()
    {
        return DB_VERSION;
    }

    static public KDSDBStatistic open(Context context)
    {


        String dbName =  KDSDBBase.getDBNameForOpen(KDSDBStatistic.DB_NAME); //use sd card path
        //

        KDSDBStatistic d = new KDSDBStatistic(context, dbName, null, KDSDBStatistic.DB_VERSION);
        return d;

    }

    /**************************************************************************/
    //statistic
    /***************************************************************************
     * save data
     */
    public boolean orderAdd(KDSDataOrder order)
    {
        String sql = "";
        sql = order.sqlAddNewForStatistic("orders");
        boolean bTransactionByMe = false;
        try
        {
            String guid = order.getGUID();

            bTransactionByMe = this.startTransaction();

            this.executeDML(sql);
            //set the finished time
            java.util.Date dt = new java.util.Date();
            sql = "update orders set finishedtime='" + KDSUtil.convertDateToString(dt) + "' where guid='" + guid + "'";//+ KDSUtil.ConvertIntToString(nOrderID);
            this.executeDML(sql);

            KDSDataItems items = order.getItems();

            itemsAdd(items);

            //this.finishTransaction(bTransactionByMe);
            this.commitTransaction(bTransactionByMe);
            return true;
        }
        catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
        finally {
            this.endTransaction(bTransactionByMe);

        }

    }
    private boolean itemsAdd(KDSDataItems items)
    {
        int ncount = items.getCount();
        String sql = "";
        try
        {
            for (int i=0; i< ncount; i++)
            {
                KDSDataItem item = (KDSDataItem)items.getComponent(i);
                sql = item.sqlAddNew("items");
                //stmt.addBatch(sql);
                this.executeDML(sql);


            }
            return true;
        }
        catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }
    private boolean condimentsAdd(KDSDataCondiments condiments)
    {
        int ncount = condiments.getCount();
        String sql = "";
        try
        {
            for (int i=0; i< ncount; i++)
            {
                KDSDataCondiment condiment = (KDSDataCondiment)condiments.getComponent(i);
                sql = condiment.sqlAddNew("condiments");
                this.executeDML(sql);

            }
            return true;
        }
        catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }

    public boolean orderDelete(String guid)
    {
        if (getDB() == null) return false;
        String sql = KDSDataOrder.sqlDelete("orders",guid);
        if (!this.executeDML(sql))
            return false;
        sql = "select guid from items where orderguid='" +guid+"'";// + Common.KDSUtil.ConvertIntToString(nID);

        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext())
        {
            String itemguid = getString(c,0);
            itemDelete(itemguid);
        }
        return true;
    }
    public boolean itemDelete(String itemguid)//int nID)
    {
        String sql = KDSDataItem.sqlDelete("items",itemguid);
        if (!this.executeDML(sql)) return false;
        sql = "delete from messages where ObjType=1 and ObjGUID='"+ itemguid +"'";// +Common.KDSUtil.ConvertIntToString(nID);
        if (!this.executeDML(sql)) return false;
        sql = "delete from condiments where ItemGUID='"+itemguid+"'";// +Common.KDSUtil.ConvertIntToString(nID);
        if (!this.executeDML(sql)) return false;
        return true;
    }

    public ArrayList<String> orderGetOrdersByTimeStamp(String strAfterTimeStamp)
    {
        String sql = String.format("select guid from orders where dbtimestamp>'%s'", strAfterTimeStamp);
        ArrayList<String> ar = new ArrayList<>();
        if (getDB() == null) return ar;

        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext())
        {

            String guid = getString(c,0);
            ar.add(guid);

        }
        return ar;
    }

    public boolean isEmpty()
    {
        String sql = "select count(*) from orders";
        return (this.executeOneValue(sql)==0);
    }

    /**

    /**
     * @param tblName
     * @param strWhereCondition The where condition
     * @return
     */
    public ArrayList<String> outputOrdersTableDataSql(KDSDBBase db, String tblName, String strWhereCondition) {
        ArrayList<String> arSql = new ArrayList<>();

        if (db.getDB() == null) return arSql;

        String fields = "GUID,Name,Waiter,Start,ToTbl,Station,Screen,POS,OrderType,Dest,CustMsg,Parked,IconIdx,EvtFired,PrepStart,"
                        + "OrderDelay,Status,SortIdx,Bumped,BumpedTime,FromPrimary,FinishedTime,"
                         +"r0,r1,r2,r3,r4,r5,r6,r7,r8,r9,"
                + "DBTimeStamp as StationTimeStamp";
        String sql = "";
        sql = "select "+fields+" from " + tblName;
        if (!strWhereCondition.isEmpty())
            sql += " " + strWhereCondition;

        Cursor c = db.getDB().rawQuery(sql, null);
        String sqlInsert = "";
        sqlInsert = INSERT_INTO_ORDERS; //use symbol to replace fields, just for decrease the sql string length.

        while (c.moveToNext()) {
            sql = sqlInsert;
            sql += makeInsertOrdersValues(c);
            sql += ");" + SQL_SEPARATOR;
            arSql.add(sql);

        }
        return arSql;
    }

    public String makeInsertOrdersValues(Cursor c) {
        String s = "";

        s = makeInsertValues(c);
        s +=",'";
        s +=KDSGlobalVariables.getKDS().getStationID();
        s += "'";
        return s;
    }


    /**
     * >>>>>>>>>>>>>IMPORTANT<<<<<<<<<<<<<
     * Please make sure this stirng is same as statistic app.
     */

    /**
     *
     * @param db
     * @param tblName
     * @param strWhereCondition
     * @return
     */
    public ArrayList<String> outputItemsTableDataSql(KDSDBBase db, String tblName, String strWhereCondition) {
        ArrayList<String> arSql = new ArrayList<>();

        if (db.getDB() == null) return arSql;

       String fields = "OrderGUID,GUID,Name,Description,Qty,QtyChanged,Category,BG,FG,Grp,Marked,DeleteByRemote,"
                    + "LocalBumped,BumpedStations,ToStations,Ready,Hiden,PreparationTime,"
                    + "ItemDelay,ItemType,"
                    +"r0,r1,r2,r3,r4,r5,r6,r7,r8,r9,"
                    + "DBTimeStamp";
        String sql = "";
        sql = "select "+fields +" from " + tblName;
        if (!strWhereCondition.isEmpty())
            sql += " " + strWhereCondition;

        Cursor c = db.getDB().rawQuery(sql, null);
        String sqlInsert = "";
        sqlInsert = INSERT_INTO_ITEMS;

        while (c.moveToNext()) {
            sql = sqlInsert;
            sql += makeInsertValues(c);
            sql += ");"+ SQL_SEPARATOR;
            arSql.add(sql);

        }
        return arSql;
    }


    /**
     * output the data with the timestamp > nAfterTimeStamp.
     * for statistic app
     * @param strAfterTimeStamp
     * @return
     */
    public ArrayList<String> outputOrdersSqlStrings(String strAfterTimeStamp)
    {
        ArrayList<String> arsql = new ArrayList<>();
        ArrayList<String> arOrdersGuid = orderGetOrdersByTimeStamp(strAfterTimeStamp);
        for (int i = 0; i < arOrdersGuid.size(); i++) {
            String orderGuid = arOrdersGuid.get(i);
            String whereCondition = "where guid='" + orderGuid + "'";
            ArrayList<String> arOrders = outputOrdersTableDataSql(this, "orders", whereCondition);
            arsql.addAll(arOrders);

            whereCondition = "where orderguid='" + orderGuid + "'";
            ArrayList<String> arItems = outputItemsTableDataSql(this, "items", whereCondition);
            arsql.addAll(arItems);


        }
        return arsql;
    }


    public ArrayList<String> outputOrderSqlStrings(String orderGuid)
    {
        ArrayList<String> arsql = new ArrayList<>();

        String whereCondition = "where guid='" + orderGuid + "'";
        ArrayList<String> arOrders = outputOrdersTableDataSql(this, "orders", whereCondition);
        arsql.addAll(arOrders);

        /**
         * >>>>>>>>>>>>>>>> IMPORTANT <<<<<<<<<<<<<<<<<<<<
         * Now, we will discard the items data, as we don't need it in statistic app
         */
//        whereCondition = "where orderguid='" + orderGuid + "'";
//        ArrayList<String> arItems = outputItemsTableDataSql(this, "items", whereCondition);
//        arsql.addAll(arItems);

        return arsql;
    }


    public TimeSlotOrderReport createOrderReport(ConditionStatistic condition)
    {
        if (condition.getReportMode() == ConditionBase.ReportMode.Item)
        {
            return createItems_Report(condition);
        }
        else {
            switch (condition.getReportType()) {

                case Daily:
                    return createOrderDailyReport(condition);

                case Weekly:
                    return createOrderWeeklyReport(condition);

                case Monthly:
                    return createOrderMonthlyReport(condition);

                case OneTime:
                    return createOrderOneTimeReport(condition);

            }
        }
        return null;
    }

    public ReportOrderOneTime createOrderOneTimeReport(ConditionStatistic condition)
    {
        ConditionOneTime oneTime =  condition.getOneTimeCondition();
        switch (oneTime.getReportArrangement())
        {

            case FullDate:
                return createOneTime_FullDate_Report(condition);

            case PerMonth:
                return createOneTime_PerMonth(condition);

            case PerWeek:
                return createOneTime_PerWeek(condition);

        }
        return null;
    }

    public ReportOrderOneTime createOneTime_PerMonth(ConditionStatistic condition)
    {
        if (condition.getOneTimeCondition().getEnableDayOfWeek())
        {
            return createOneTime_PerMonth_DayOfWeek(condition);
        }
        else
        {
            return createOneTime_PerMonth_Report(condition);
        }
    }

    public ReportOrderOneTime createOneTime_PerMonth_DayOfWeek(ConditionStatistic condition)
    {
        int nStationFrom = KDSUtil.convertStringToInt( condition.getStationFrom(), 0);
        int nStationTo = KDSUtil.convertStringToInt( condition.getStationTo(), 0);
        //ArrayList<String> dateFrom = new ArrayList<>();
        //ArrayList<String> dateTo = new ArrayList<>();
        ArrayList<DateSlots> dateSlots = new ArrayList<>();
        condition.getOneTimeCondition().getDateSlots_PerMonth_DayOfWeek(dateSlots);
        //condition.getOneTimeCondition().getDateSlots_PerMonth(dateFrom, dateTo);
        ReportOrderOneTime report = new ReportOrderOneTime();
        report.setCondition(condition);

        String tmFrom = KDSUtil.convertDateToString( condition.getOneTimeCondition().getTimeFrom());
        String tmTo = KDSUtil.convertDateToString( condition.getOneTimeCondition().getTimeTo());

        for (int i = 0; i<dateSlots.size(); i++)
        {

            TimeSlotEntry entry = createOneTime_PerMonth_ReportForTimeSlot(dateSlots.get(i),tmFrom, tmTo, condition, nStationFrom, nStationTo);
            report.add(entry);
        }
        //addTotal(report);
        return report;
    }

    public TimeSlotEntry createOneTime_PerMonth_ReportForTimeSlot(DateSlots dateSlots,String tmFrom, String tmTo, ConditionStatistic condition, int nFromStation,int nToStation)
    {

        TimeSlotEntry entry = createOrderReportTimeSlotEntry(dateSlots,tmFrom, tmTo, condition, nFromStation, nToStation);
        Date dt = KDSUtil.convertStringToDate(dateSlots.getDateStart());
        String text = KDSUtil.convertDateToShortString(dt);
        entry.setFixedText(text);
        return entry;

    }


    public ReportOrderOneTime createOneTime_PerMonth_Report(ConditionStatistic condition)
    {
        int nStationFrom = KDSUtil.convertStringToInt( condition.getStationFrom(), 0);
        int nStationTo = KDSUtil.convertStringToInt( condition.getStationTo(), 0);
        ArrayList<String> dateFrom = new ArrayList<>();
        ArrayList<String> dateTo = new ArrayList<>();

        condition.getOneTimeCondition().getDateSlots_PerMonth(dateFrom, dateTo);
        ReportOrderOneTime report = new ReportOrderOneTime();
        report.setCondition(condition);

        String tmFrom = KDSUtil.convertDateToString( condition.getOneTimeCondition().getTimeFrom());
        String tmTo = KDSUtil.convertDateToString( condition.getOneTimeCondition().getTimeTo());

        for (int i = 0; i<dateFrom.size(); i++)
        {

            TimeSlotEntry entry = createOneTime_PerMonth_ReportForTimeSlot(dateFrom.get(i),dateTo.get(i),tmFrom, tmTo, condition, nStationFrom, nStationTo);
            report.add(entry);
        }

        return report;
    }

    public TimeSlotEntry createOneTime_PerMonth_ReportForTimeSlot(String dtFrom, String dtTo,String tmFrom, String tmTo, ConditionStatistic condition, int nFromStation,int nToStation)
    {

        TimeSlotEntry entry = createOrderReportTimeSlotEntry(dtFrom, dtTo,tmFrom, tmTo, condition, nFromStation, nToStation);
        Date dt = KDSUtil.convertStringToDate(dtFrom);
        String text = KDSUtil.convertDateToShortString(dt);
        entry.setFixedText(text);
        return entry;

    }
    public ReportOrderOneTime createOneTime_PerWeek(ConditionStatistic condition)
    {
        if (condition.getOneTimeCondition().getEnableDayOfWeek())
        {
            return createOneTime_PerWeek_DayOfWeek(condition);
        }
        else
        {
            return createOneTime_PerWeek_Report(condition);
        }
    }

    public ReportOrderOneTime createOneTime_PerWeek_Report(ConditionStatistic condition)
    {
        int nStationFrom = KDSUtil.convertStringToInt( condition.getStationFrom(), 0);
        int nStationTo = KDSUtil.convertStringToInt( condition.getStationTo(), 0);
        ArrayList<String> dateFrom = new ArrayList<>();
        ArrayList<String> dateTo = new ArrayList<>();
        condition.getOneTimeCondition().getDateSlots_PerWeek(dateFrom, dateTo);

        ReportOrderOneTime report = new ReportOrderOneTime();
        report.setCondition(condition);

        String tmFrom = KDSUtil.convertDateToString( condition.getOneTimeCondition().getTimeFrom());
        String tmTo = KDSUtil.convertDateToString( condition.getOneTimeCondition().getTimeTo());

        for (int i = 0; i<dateFrom.size(); i++)
        {

            TimeSlotEntry entry = createOneTime_PerWeek_ReportForTimeSlot(dateFrom.get(i),dateTo.get(i),tmFrom, tmTo, condition, nStationFrom, nStationTo);
            report.add(entry);
        }
        //addTotal(report);
        return report;
    }

    public TimeSlotEntry createOneTime_PerWeek_ReportForTimeSlot(String dtFrom, String dtTo,String tmFrom, String tmTo, ConditionStatistic condition, int nFromStation,int nToStation)
    {

        TimeSlotEntry entry = createOrderReportTimeSlotEntry(dtFrom, dtTo,tmFrom, tmTo, condition, nFromStation, nToStation);
        Date dt = KDSUtil.convertStringToDate(dtFrom);
        String text = KDSUtil.convertDateToShortString(dt);
        entry.setFixedText(text);
        return entry;

    }

    public ReportOrderOneTime createOneTime_PerWeek_DayOfWeek(ConditionStatistic condition)
    {
        int nStationFrom = KDSUtil.convertStringToInt( condition.getStationFrom(), 0);
        int nStationTo = KDSUtil.convertStringToInt( condition.getStationTo(), 0);

        ArrayList<DateSlots> dateSlots = new ArrayList<>();
        condition.getOneTimeCondition().getDateSlots_PerWeek_DayOfWeek(dateSlots);

        ReportOrderOneTime report = new ReportOrderOneTime();
        report.setCondition(condition);

        String tmFrom = KDSUtil.convertDateToString( condition.getOneTimeCondition().getTimeFrom());
        String tmTo = KDSUtil.convertDateToString( condition.getOneTimeCondition().getTimeTo());

        for (int i = 0; i<dateSlots.size(); i++)
        {

            TimeSlotEntry entry = createOneTime_PerWeek_ReportForTimeSlot(dateSlots.get(i),tmFrom, tmTo, condition, nStationFrom, nStationTo);
            report.add(entry);
        }

        return report;
    }

    public TimeSlotEntry createOneTime_PerWeek_ReportForTimeSlot(DateSlots dateSlots,String tmFrom, String tmTo, ConditionStatistic condition, int nFromStation,int nToStation) {

        TimeSlotEntry entry = createOrderReportTimeSlotEntry(dateSlots, tmFrom, tmTo, condition, nFromStation, nToStation);
        Date dt = KDSUtil.convertStringToDate(dateSlots.getDateStart());
        String text = KDSUtil.convertDateToShortString(dt);
        entry.setFixedText(text);
        return entry;
    }



    public ReportOrderMonthly createOrderMonthlyReport(ConditionStatistic condition)
    {
        int nStationFrom = KDSUtil.convertStringToInt( condition.getStationFrom(), 0);
        int nStationTo = KDSUtil.convertStringToInt( condition.getStationTo(), 0);
        ArrayList<String> dateFrom = new ArrayList<>();
        ArrayList<String> dateTo = new ArrayList<>();
        condition.getMonthlyCondition().getMonthDaySlots(dateFrom, dateTo);

        ReportOrderMonthly report = new ReportOrderMonthly();
        report.setCondition(condition);

        String tmFrom = KDSUtil.convertTimeToDbString(condition.getMonthlyCondition().getTimeFrom());
        String tmTo = KDSUtil.convertTimeToDbString( condition.getMonthlyCondition().getTimeTo());


        //build the empty report
        for (int i = 0; i<dateFrom.size(); i++)
        {
            TimeSlotEntry entry = new TimeSlotEntry();//createOrderMonthlyReportForTimeSlot(dateFrom.get(i), dateTo.get(i),tmFrom, tmTo, condition, nStationFrom, nStationTo);
            Date dt = KDSUtil.convertStringToDate(dateFrom.get(i));
            String text = ConditionBase.getMonthDayString(dt);
            entry.setFixedText(text);

            entry.add( condition.getStationFrom(),0,0 );
            report.add(entry);
        }

        String sql = String.format( "select date(FinishedTime) as d, count(*) as c, total(julianday(finishedtime)-julianday(start)) as t from orders " +
                                    "where date(FinishedTime)>='%s' and date(FinishedTime)<='%s' and time(FinishedTime)>='%s' and time(FinishedTime)<='%s' group by d order by d",
                                    dateFrom.get(0), dateTo.get(dateTo.size()-1),
                                    tmFrom, tmTo);

        Cursor c = getDB().rawQuery(sql, null);
        String strDt = "";
        float ndays = 0;
        int ntotal = 0;
        while (c.moveToNext()) {
            strDt = getString(c,0);
            ntotal = getInt(c,1);
            ndays = getFloat(c,2);
            float fltSeconds = ndays * 86400;//24 * 60 * 60;
            int nIndex =findDateIndexInTimeSlots(dateFrom, strDt);
            if (nIndex >=0)
            {
                report.getData().get(nIndex).getData().set(0, new TimeSlotEntryDetail(condition.getStationFrom(), ntotal, (int)fltSeconds));
            }
        }
        c.close();

        // addTotal(report);
        return report;
    }

    public int findDateIndexInTimeSlots(ArrayList<String> dateFrom, String dt)
    {
        for (int i=0; i< dateFrom.size(); i++)
        {
            if (dateFrom.get(i).indexOf(dt)>=0)
                return i;
        }
        return -1;
    }



    public ReportOrderWeekly createOrderWeeklyReport(ConditionStatistic condition)
    {

        int nStationFrom = KDSUtil.convertStringToInt( condition.getStationFrom(), 0);
        int nStationTo = KDSUtil.convertStringToInt( condition.getStationTo(), 0);
        ArrayList<String> dateFrom = new ArrayList<>();
        ArrayList<String> dateTo = new ArrayList<>();
        condition.getWeeklyCondition().getWeekDaySlots(dateFrom, dateTo);

        ReportOrderWeekly report = new ReportOrderWeekly();
        report.setCondition(condition);
        String tmFrom = KDSUtil.convertTimeToDbString( condition.getWeeklyCondition().getTimeFrom());
        String tmTo = KDSUtil.convertTimeToDbString( condition.getWeeklyCondition().getTimeTo());

        //build the empty report
        for (int i = 0; i<dateFrom.size(); i++)
        {
            TimeSlotEntry entry = new TimeSlotEntry();//createOrderMonthlyReportForTimeSlot(dateFrom.get(i), dateTo.get(i),tmFrom, tmTo, condition, nStationFrom, nStationTo);
            Date dt = KDSUtil.convertStringToDate(dateFrom.get(i));
            String text = ConditionBase.getWeekDayString(dt);
            entry.setFixedText(text);

            entry.add( condition.getStationFrom(),0,0 );
            report.add(entry);
        }

        String sql = String.format( "select date(FinishedTime) as d, count(*) as c, total(julianday(finishedtime)-julianday(start)) as t from orders " +
                        "where date(FinishedTime)>='%s' and date(FinishedTime)<='%s' and time(FinishedTime)>='%s' and time(FinishedTime)<='%s' group by d order by d",
                dateFrom.get(0), dateTo.get(dateTo.size()-1),
                tmFrom, tmTo);

        Cursor c = getDB().rawQuery(sql, null);
        String strDt = "";
        float ndays = 0;
        int ntotal = 0;
        while (c.moveToNext()) {
            strDt = getString(c,0);
            ntotal = getInt(c,1);
            ndays = getFloat(c,2);
            float fltSeconds = ndays * 86400;//24 * 60 * 60;
            int nIndex =findDateIndexInTimeSlots(dateFrom, strDt);
            if (nIndex >=0)
            {
                report.getData().get(nIndex).getData().set(0, new TimeSlotEntryDetail(condition.getStationFrom(), ntotal, (int)fltSeconds));
            }
        }
        c.close();


        return report;


    }


    public ReportOrderOneTime createOneTime_FullDate_Report(ConditionStatistic condition)
    {

        int nStationFrom = KDSUtil.convertStringToInt( condition.getStationFrom(), 0);
        int nStationTo = KDSUtil.convertStringToInt( condition.getStationTo(), 0);
        ConditionOneTime.WeekDay nWeekKay = condition.getOneTimeCondition().getConditionWeekDay();

        ArrayList<String> dateFrom = new ArrayList<>();
        ArrayList<String> dateTo = new ArrayList<>();
        condition.getOneTimeCondition().getDateSlots_FullDate(dateFrom, dateTo);

        ReportOrderOneTime report = new ReportOrderOneTime();
        report.setCondition(condition);
        String tmFrom = KDSUtil.convertDateToString( condition.getOneTimeCondition().getTimeFrom());
        String tmTo = KDSUtil.convertDateToString( condition.getOneTimeCondition().getTimeTo());
        for (int i = 0; i< dateFrom.size(); i++)
        {

            TimeSlotEntry entry = createOneTime_FullDate_ReportForTimeSlot(dateFrom.get(i),dateTo.get(i),tmFrom, tmTo, condition, nStationFrom, nStationTo);
            report.add(entry);
        }
        //addTotal(report);
        return report;

    }

    public TimeSlotEntry createOneTime_FullDate_ReportForTimeSlot(String dtFrom, String dtTo,String tmFrom, String tmTo, ConditionStatistic condition, int nFromStation,int nToStation)
    {
        TimeSlotEntry entry = createOrderReportTimeSlotEntry(dtFrom, dtTo,tmFrom, tmTo, condition, nFromStation, nToStation);
        Date dt = KDSUtil.convertStringToDate(dtFrom);
        String text = KDSUtil.convertDateToShortString(dt);
        entry.setFixedText(text);
        return entry;

    }

    public ReportOrderDaily createOrderDailyReport(ConditionStatistic condition)
    {
        int nStationFrom = KDSUtil.convertStringToInt( condition.getStationFrom(), 0);
        int nStationTo = KDSUtil.convertStringToInt( condition.getStationTo(), 0);
        ReportOrderDaily report = new ReportOrderDaily();
        report.setCondition(condition);

        ArrayList<String> timeslotFrom = new ArrayList<>();
        ArrayList<String> timeslotTo = new ArrayList<>();
        condition.getDailyReportCondition().getTimeSlots(timeslotFrom, timeslotTo);
        String tmFrom = KDSUtil.convertDateToString( condition.getDailyReportCondition().getTimeFrom());
        String tmTo = KDSUtil.convertDateToString( condition.getDailyReportCondition().getTimeTo());

        for (int i = 0; i< timeslotFrom.size(); i++)
        {
            //int station = i;
            TimeSlotEntry entry = createOrderDailyReportForTimeSlot(timeslotFrom.get(i),
                    timeslotTo.get(i),
                    tmFrom,
                    tmTo,
                    condition,nStationFrom,nStationTo);
            report.add(entry);
        }


        return report;
    }

    public TimeSlotEntry createOrderDailyReportForTimeSlot(String dtFrom, String dtTo,String tmFrom, String tmTo, ConditionStatistic condition, int nFromStation,int nToStation)
    {

        TimeSlotEntry entry = createOrderReportTimeSlotEntry(dtFrom, dtTo, condition, nFromStation, nToStation);
        Date dt = KDSUtil.convertStringToDate(dtFrom);
        String text =  KDSUtil.convertTimeToShortString(dt);
        entry.setFixedText(text);
        return entry;

    }

    public TimeSlotEntry createOrderReportTimeSlotEntry(String timeslotFrom, String timeslotTo, ConditionStatistic condition, int nFromStation,int nToStation)
    {

        TimeSlotEntry entry = new TimeSlotEntry();
        entry.setTimeSlotsFrom(timeslotFrom);


        for (int i=nFromStation; i<= nToStation; i++)
        {


            StatisticData d = statisticGetOrderCountAndBumpedSeconds(i, timeslotFrom, timeslotTo );
            int ordersCount = d.m_nP0;
            int bumpSeconds = d.m_nP1;
            entry.add(KDSUtil.convertIntToString(i), ordersCount, bumpSeconds);
        }
        return entry;
    }

    public TimeSlotEntry createOrderReportTimeSlotEntry(DateSlots dateSlots,String tmFrom, String tmTo, ConditionStatistic condition, int nFromStation,int nToStation)
    {

        TimeSlotEntry entry = new TimeSlotEntry();
        entry.setTimeSlotsFrom(dateSlots.getDateStart());


        for (int i=nFromStation; i<= nToStation; i++)
        {

            int ordersCount = statisticGetOrderCount(i, dateSlots, tmFrom, tmTo);
            int bumpSeconds = getTotalBumpSeconds(i,dateSlots, tmFrom, tmTo);
            entry.add(KDSUtil.convertIntToString(i), ordersCount, bumpSeconds);
        }
        return entry;
    }

    /**
     *
     * @param dtFrom
     *  yyyy-mm-dd hh:mm:ss
     * @param dtTo
     * @param tmFrom
     * yyyy-mm-dd hh:mm:ss
     * @param tmTo
     * @param condition
     * @param nFromStation
     * @param nToStation
     * @return
     */
    public TimeSlotEntry createOrderReportTimeSlotEntry(String dtFrom, String dtTo,String tmFrom, String tmTo, ConditionStatistic condition, int nFromStation,int nToStation)
    {

        TimeSlotEntry entry = new TimeSlotEntry();
        entry.setTimeSlotsFrom(dtFrom);
        int ordersCount = 0;
        int bumpSeconds = 0;
        for (int i=nFromStation; i<= nToStation; i++)
        {
            StatisticData d = getTotalCountAndBumpSeconds(i, dtFrom, dtTo, tmFrom, tmTo);
            entry.add(KDSUtil.convertIntToString(i), d.m_nP0,d.m_nP1);

        }
        return entry;
    }

    private int statisticGetOrderCount(int nStation, DateSlots dateSlots,String tmFrom, String tmTo)
    {

        String timeFrom = KDSUtil.getTimeFromString(tmFrom);
        String timeTo = KDSUtil.getTimeFromString(tmTo);


        String sql = String.format("select count(*) from orders where FromStation='%d' and time(FinishedTime)>='%s' and time(FinishedTime)<='%s' and (",
                nStation, timeFrom, timeTo);
        String strCondition = "";
        for (int i=0; i< dateSlots.getSize(); i++)
        {
            String dateFrom = KDSUtil.getDateFromString(dateSlots.getDateFrom(i));
            String dateTo = KDSUtil.getDateFromString(dateSlots.getDateTo(i));
            strCondition = String.format("(date(FinishedTime)>='%s' and date(FinishedTime)<='%s') ", dateFrom, dateTo);
            if (i>0)
                sql += " or ";
            sql += strCondition;


        }
        sql += ")";

        return this.executeOneValue(sql);
    }

    private int getTotalBumpSeconds(int nStation,  DateSlots dateSlots, String tmFrom, String tmTo)
    {

        String timeFrom = KDSUtil.getTimeFromString(tmFrom);
        String timeTo = KDSUtil.getTimeFromString(tmTo);
        String sql = String.format("select total(julianday(finishedtime)-julianday(start)) as a orders where FromStation='%d' and time(FinishedTime)>='%s' and time(FinishedTime)<='%s' and (",
                nStation, timeFrom, timeTo);
        String strCondition = "";
        for (int i=0; i< dateSlots.getSize(); i++)
        {
            String dateFrom = KDSUtil.getDateFromString(dateSlots.getDateFrom(i));
            String dateTo = KDSUtil.getDateFromString(dateSlots.getDateTo(i));
            strCondition = String.format("(date(FinishedTime)>='%s' and date(FinishedTime)<='%s') ", dateFrom, dateTo);
            if (i>0)
                sql += " or ";
            sql += strCondition;


        }
        sql += ")";


        float flt = this.executeOneFloat(sql); //this is the days
        flt = flt * 86400;//24 * 60 * 60;
        return (int)flt;
    }




    private StatisticData getTotalCountAndBumpSeconds(int nStation, String dtFrom, String dtTo, String tmFrom, String tmTo)
    {

        StatisticData d = new StatisticData();

        String dateFrom = KDSUtil.getDateFromString(dtFrom);
        String dateTo = KDSUtil.getDateFromString(dtTo);
        String timeFrom = KDSUtil.getTimeFromString(tmFrom);
        String timeTo = KDSUtil.getTimeFromString(tmTo);

        String sql = String.format("select count(*) as c,total(julianday(finishedtime)-julianday(start)) as t from orders where date(FinishedTime)>='%s' and date(FinishedTime)<='%s' and time(FinishedTime)>='%s' and time(FinishedTime)<='%s'",
                dateFrom, dateTo, timeFrom, timeTo);

        try {
            Cursor c = getDB().rawQuery(sql, null);
            float ndays = 0;
            int ntotal = 0;
            if (c.moveToNext()) {
                ntotal = getInt(c,0);
                ndays = getFloat(c,1);
            }
            c.close();
            ndays = ndays * 86400;//24 * 60 * 60;
            d.m_nP0 = ntotal;
            d.m_nP1 =(int) ndays;
            return d;
        }catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //Log.e("DB", e.getMessage());
        }
        return d;


    }



    private StatisticData statisticGetOrderCountAndBumpedSeconds(int nStation, String dtFrom, String dtTo)
    {

        String sql = String.format("select count(*) as a,total(julianday(finishedtime)-julianday(start)) as b from orders where FinishedTime>='%s' and FinishedTime<'%s'", dtFrom, dtTo);
        StatisticData d = new StatisticData();
        try {

            Cursor c = getDB().rawQuery(sql, null);
            int  ncount = 0;
            float nseconds = 0;
            if (c.moveToNext()) {
                ncount = getInt(c,0);
                nseconds = getFloat(c,1); //get the days, need to convert to seconds
            }
            c.close();
            nseconds = nseconds * 86400;//24 * 60 * 60;
            d.m_nP0 = ncount;
            d.m_nP1 =(int) nseconds;
            return d;
        }catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //Log.e("DB", e.getMessage());
        }
        return d;

    }


    TimeSlotOrderReport createItems_FullItemsReport(ConditionStatistic condition)
    {

        String sql = "";
        String sql1 = "";
        String strItemDecription = condition.getItemDescription();
        if (strItemDecription.isEmpty())
            sql1 = "select total(julianday(orders.finishedtime)-julianday(orders.start)) as duration, count(*) as c, items.description from orders, items " +
                    "where items.orderguid=orders.guid ";
        else
            sql1 = "select total(julianday(orders.finishedtime)-julianday(orders.start)) as duration, count(*) as c, items.description from orders, items " +
                    "where items.orderguid=orders.guid and items.description like '%" + strItemDecription +"%'";
        String sql2 =   "group by items.description " +
                    "order by c desc";


        float ndays = 0;
        int ncount = 0;
        String description = "";
        TimeSlotOrderReport report = null;
        switch (condition.getReportType()) {

            case Daily:
                report = new ReportOrderDaily();
                sql = String.format("%s and  FinishedTime>='%s' and FinishedTime<='%s' %s",
                                    sql1,
                                    condition.getDailyReportCondition().getDateTimeFrom(),
                                    condition.getDailyReportCondition().getDateTimeTo(),
                                    sql2);
                break;

            case Weekly:
                report = new ReportOrderWeekly();
                sql = String.format("%s and  FinishedTime>='%s' and FinishedTime<'%s' and time(finishedtime)>='%s' and time(finishedtime)<='%s' %s",
                                    sql1,
                                    condition.getWeeklyCondition().getWeekFirstDayDateString(),
                                    condition.getWeeklyCondition().getWeekLastDayDateString(),
                                    condition.getWeeklyCondition().getTimeFromString(),
                                    condition.getWeeklyCondition().getTimeToString(),
                                    sql2);
                break;
            case Monthly:
                report = new ReportOrderMonthly();
                sql = String.format("%s and  FinishedTime>='%s' and FinishedTime<'%s' and time(finishedtime)>='%s' and time(finishedtime)<='%s' %s",
                        sql1,
                        condition.getMonthlyCondition().getMonthFirstDayString(),
                        condition.getMonthlyCondition().getMonthLastDayString(),
                        condition.getMonthlyCondition().getTimeFromString(),
                        condition.getMonthlyCondition().getTimeToString(),
                        sql2);
                break;

            case OneTime:
                report = new ReportOrderOneTime();

                sql = String.format("%s and  FinishedTime>='%s' and FinishedTime<'%s' and time(finishedtime)>='%s' and time(finishedtime)<='%s' %s",
                        sql1,
                        condition.getOneTimeCondition().getDateFromString(),
                        condition.getOneTimeCondition().getDateToString(),
                        condition.getOneTimeCondition().getTimeFromString(),
                        condition.getOneTimeCondition().getTimeToString(),
                        sql2);
                break;

        }
        report.setCondition(condition);

        Cursor c = getDB().rawQuery(sql, null);
        while (c.moveToNext()) {
            ndays = getFloat(c,0);
            ncount = getInt(c,1);
            description = getString(c,2);
            ReportItemEntry entry = new ReportItemEntry();

            entry.setFixedText(description);
            int nseconds =(int)( ndays * 86400 );//24 * 60 * 60;
            entry.addItem(condition.getStationFrom(),description, ncount, nseconds);
            report.add( entry);
        }
        c.close();
        if (!strItemDecription.isEmpty())
        {
            if (report.getData().size() == 0)
            {
                ReportItemEntry entry = new ReportItemEntry();

                entry.setFixedText("");

                entry.addItem(condition.getStationFrom(),"", 0, 0);
                report.add( entry);
            }
        }
        return report;
    }



    /*
@pReturn: the CStatisticItem array
*/
    /************************************************************************/
    TimeSlotOrderReport createItems_Report(ConditionStatistic condition)
    {

        String sql = "";

        if (condition.getReportMode() != ConditionBase.ReportMode.Item)
            return null;
        //String itemDescription = condition.getItemDescription();

        return createItems_FullItemsReport(condition);



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

    public void removeData(int nDaysBefore)
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, (-1) * nDaysBefore);
        Date dt = c.getTime();

        String sql = String.format("select guid from orders where date(finishedtime)<='%s'", KDSUtil.convertDateToDbString(dt));
        Cursor q = getDB().rawQuery(sql, null);
        ArrayList<String> ar = new ArrayList<>();

        while (q.moveToNext())
        {

            String orderguid = getString(q,0);
            ar.add(orderguid);
        }
        q.close();
       if (ar.size() <=0) return;
        boolean bstartedbyme = this.startTransaction();
        for (int i=0; i< ar.size(); i++)
        {
            removeOrder(ar.get(i));
        }
        this.finishTransaction(bstartedbyme);


    }
    public boolean removeOrder(String orderGuid)
    {
        String sql = String.format("delete from orders where guid='%s'", orderGuid);
        this.executeDML(sql);
        sql = String.format("delete from items where orderguid='%s'", orderGuid);
        this.executeDML(sql);
        return true;
    }


    /**
     * 2.0.10 for sos
     * The date is sequence date
     * @param dtFrom
     *  format: 2018-02-02 12:12:10
     * @param dtTo
     * @return
     */
    private StatisticData getTotalCountAndBumpSeconds(String dtFrom, String dtTo)
    {

        StatisticData d = new StatisticData();


        String sql = String.format("select count(*) as c,total(julianday(finishedtime)-julianday(start)) as t from orders where datetime(FinishedTime)>='%s' and datetime(FinishedTime)<'%s'",
                                    dtFrom, dtTo);

        try {
            Cursor c = getDB().rawQuery(sql, null);
            float ndays = 0;
            int ntotal = 0;
            if (c.moveToNext()) {
                ntotal = getInt(c,0);
                ndays = getFloat(c,1);
            }
            c.close();
            ndays = ndays * 86400;//24 * 60 * 60;
            d.m_nP0 = ntotal;
            d.m_nP1 =(int) ndays;//to seconds
            return d;
        }catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //Log.e("DB", e.getMessage());
        }
        return d;


    }

    //2.0.23

    /**
     *
     * @param dtFrom
     * @param dtTo
     * @param nTargetSeconds
     * @return
     */
    private StatisticData getSOSTotalCountOverTarget(String dtFrom, String dtTo, int nTargetSeconds)
    {

        StatisticData d = new StatisticData();
        //select count(*) as c from orders where ((julianday(bumpedtime)-julianday(start))*86400)>3 and datetime(bumpedtime)>='2014-1-1 12:12:12' and datetime(bumpedtime)<'2018-1-1 12:12:12' and name='Order #1'
        String sql = String.format("select count(*) as c,total(julianday(finishedtime)-julianday(start)) as t from orders where (julianday(finishedtime)-julianday(start))*86400>%d and datetime(FinishedTime)>='%s' and datetime(FinishedTime)<'%s'",
                nTargetSeconds,dtFrom, dtTo);

        try {
            Cursor c = getDB().rawQuery(sql, null);
            int ntotal = 0;
            float ndays = 0;
            if (c.moveToNext()) {
                ntotal = getInt(c,0);
                ndays = getFloat(c,1);
            }
            c.close();
            ndays = ndays * 86400;//24 * 60 * 60;
            d.m_nP0 = ntotal;
            d.m_nP1 =(int) ndays;//to seconds
            return d;

        }catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //Log.e("DB", e.getMessage());
        }
        return d;


    }

    /**
     * 2.0.25
     * @param dtFrom
     * @param dtTo
     * @return
     */
    private StatisticData getTotalCountAndBumpSecondsForUser(KDSUser.USER userID, String dtFrom, String dtTo)
    {

        StatisticData d = new StatisticData();


        String sql = String.format("select count(*) as c,total(julianday(finishedtime)-julianday(start)) as t from orders where datetime(FinishedTime)>='%s' and datetime(FinishedTime)<'%s' and screen=%d",
                dtFrom, dtTo, userID.ordinal());

        try {
            Cursor c = getDB().rawQuery(sql, null);
            float ndays = 0;
            int ntotal = 0;
            if (c.moveToNext()) {
                ntotal = getInt(c,0);
                ndays = getFloat(c,1);
            }
            c.close();
            ndays = ndays * 86400;//24 * 60 * 60;
            d.m_nP0 = ntotal;
            d.m_nP1 =(int) ndays;//to seconds
            return d;
        }catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //Log.e("DB", e.getMessage());
        }
        return d;


    }

    /**
     * 2.0.25
     * @param nTimePeriod
     * @return
     */
    public int getAvgPrepTimeForLocalShowing(KDSUser.USER userID, int nTimePeriod)
    {
        int realPeriod = nTimePeriod;

        Date dtRealEnd = new Date();//from now to period

        Date dtStart = new Date();
        dtStart.setTime(dtRealEnd.getTime() - realPeriod*1000);

        StatisticData data = getTotalCountAndBumpSecondsForUser(userID, KDSUtil.convertDateToString(dtStart), KDSUtil.convertDateToString(dtRealEnd) );

        int nCount = data.m_nP0;
        int nSeconds = data.m_nP1;
        if (nCount <=0) return 0;

        float flt = ((float) nSeconds)/((float) nCount);
        return Math.round(flt);

    }
    /**
     * 2.0.10 create the sos report
     *
     * @param condition
     * @return
     */
    public SOSReportOneStation createSOSReport(String stationID, SOSReportCondition condition)
    {
        //getTotalCountAndBumpSeconds

        //1, get real time peraptation time

        int realPeriod = condition.getRealPrepTimePeriodSeconds();

        Date dtRealEnd = new Date();//from now to period

        Date dtStart = new Date();
        dtStart.setTime(dtRealEnd.getTime() - realPeriod*1000);

        SOSReportOneStation report = new SOSReportOneStation();
        report.setReportID(condition.getReportID());
        report.setStationID(stationID);

        StatisticData data = getTotalCountAndBumpSeconds( KDSUtil.convertDateToString(dtStart), KDSUtil.convertDateToString(dtRealEnd) );
        report.getRealTime().setCount(data.m_nP0);
        report.getRealTime().setBumpTimeSeconds(data.m_nP1);

        int nTargetSeconds = condition.getTargetSeconds();
        data = getSOSTotalCountOverTarget(KDSUtil.convertDateToString(dtStart), KDSUtil.convertDateToString(dtRealEnd),nTargetSeconds );
        report.getRealTime().setOverTargetCount(data.m_nP0);
        report.getRealTime().setOverTargetSeconds(data.m_nP1);

        //2, get graph data
        int nDuration = condition.getGraphPrepTimeDuration();
        int nInterval = condition.getGraphPrepTimeInterval();
        int nCount = nDuration / nInterval;
        if ((nDuration % nInterval)>0)
            nCount ++; //more is better than less

        Date dtEnd = new Date();
        dtEnd.setTime(condition.getDeadline().getTime());
        dtStart.setTime( condition.getDeadline().getTime() );

        for (int i=0; i< nCount; i++)
        {
            dtStart.setTime(dtEnd.getTime() - nInterval *1000);
            data = getTotalCountAndBumpSeconds( KDSUtil.convertDateToString(dtStart), KDSUtil.convertDateToString(dtEnd) );
            SOSReportTimeSlotData ts = new SOSReportTimeSlotData(data.m_nP0, data.m_nP1);
            report.getTimeslots().add(ts);
            dtEnd.setTime(dtStart.getTime());
        }
        return  report;

    }



    /**********************************************************************
     * statistic database
     *
     */

    private static final String Table_Orders = KDSDBCurrent.Table_Orders_Fields
                + ",FinishedTime date)";


    private static final String Table_Items = KDSDBCurrent.Table_Items;
    private static final String Table_Condiments =KDSDBCurrent.Table_Condiments;
    private static final String Table_Messages = KDSDBCurrent.Table_Messages;

    private static final String CreateInx_Orders_Guid = "create index guidorders on orders(guid)";
    private static final String CreateInx_Items_Guid = "create index guiditems on Items(orderguid,guid)";
    private static final String CreateInx_Condiments_Guid = "create index guidcondiments on Condiments(itemguid,guid)";
    private static final String CreateInx_Messages_Guid = "create index guidmsg on Messages(objguid,guid)";
    private static final String CreateInx_Order_Finished_Time = "create index orderfinishtime on orders(FinishedTime)";



}
