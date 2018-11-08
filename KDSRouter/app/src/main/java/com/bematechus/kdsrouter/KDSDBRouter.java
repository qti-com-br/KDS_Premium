package com.bematechus.kdsrouter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bematechus.kdslib.KDSDBBase;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataModifier;
import com.bematechus.kdslib.KDSDataModifiers;
import com.bematechus.kdslib.KDSDataSumName;
import com.bematechus.kdslib.KDSDataSumNames;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2015/7/29 0029.
 */
public class KDSDBRouter extends KDSDBBase {

    /**
     * 7: the first release version
     * 8: add schedule
     * 9: add firedtime in schedule table
     * 10: add category in scheduleitems table.
     * 11: add modifiers table.
     */
    static public final int DB_VERSION = 11; //

    static public final String DB_NAME = "router.db";
    static private final String TAG = "KDSRouterDB"; //2015-12-29


    public KDSDBRouter(Context context, String name, SQLiteDatabase.CursorFactory factory,
                       int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String ar[] = new String[]{


                Table_Category,
                Table_Items,
                Table_SumName,
                Table_BuildCard,
                Table_Flag, //2015-12-29
                Table_Offline_Orders,
                Table_Schedule_Fields,
                Table_ScheduleItems_Fields,
                Table_Modifiers,

        };
        exeBatchSql(db, ar);
    }

//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//
//        if (oldVersion >= 9 ) //from 9 -->10
//        {
//            changeTableName(db, "category", "category1");
//            changeTableName(db, "items", "items1");
//            changeTableName(db, "sumnames", "sumnames1");
//            changeTableName(db, "buildcards", "buildcards1");
//            changeTableName(db, "statusflag", "statusflag1");
//            changeTableName(db, "offlineorders", "offlineorders1");
//            changeTableName(db, "schedule", "schedule1");
//            changeTableName(db, "scheduleitems", "scheduleitems1");
//
//            onCreate(db);
//            copyData(db, "category", "category1", null);
//            copyData(db, "items", "items1", null);
//            copyData(db, "sumnames", "sumnames1", null);
//            copyData(db, "buildcards", "buildcards1", null);
//            copyData(db, "statusflag", "statusflag1", null);
//            copyData(db, "offlineorders", "offlineorders1", null);
//
//            ArrayList<String> ar = new ArrayList<>();
//            ar.add("");
//            copyData(db, "schedule", "schedule1", null);
//            if (oldVersion == 9)
//                copyData(db, "scheduleitems", "scheduleitems1", ar);
//            else
//                copyData(db, "scheduleitems", "scheduleitems1", null);
//
//            dropTable(db, "category1");
//            dropTable(db, "items1");
//            dropTable(db, "sumnames1");
//            dropTable(db, "buildcards1");
//            dropTable(db, "statusflag1"); //2015-12-29
//            dropTable(db, "offlineorders1"); //
//            dropTable(db, "schedule1"); //
//            dropTable(db, "scheduleitems1"); //
//
//        }
//        //from 7-->8
//        else if (oldVersion == 8 ) //from 8 -->9
//        {
//            changeTableName(db, "category", "category1");
//            changeTableName(db, "items", "items1");
//            changeTableName(db, "sumnames", "sumnames1");
//            changeTableName(db, "buildcards", "buildcards1");
//            changeTableName(db, "statusflag", "statusflag1");
//            changeTableName(db, "offlineorders", "offlineorders1");
//            changeTableName(db, "schedule", "schedule1");
//            changeTableName(db, "scheduleitems", "scheduleitems1");
//
//            onCreate(db);
//            copyData(db, "category", "category1", null);
//            copyData(db, "items", "items1", null);
//            copyData(db, "sumnames", "sumnames1", null);
//            copyData(db, "buildcards", "buildcards1", null);
//            copyData(db, "statusflag", "statusflag1", null);
//            copyData(db, "offlineorders", "offlineorders1", null);
//
//            ArrayList<String> ar = new ArrayList<>();
//            ar.add("");
//            copyData(db, "schedule", "schedule1", ar);
//            copyData(db, "scheduleitems", "scheduleitems1", null);
//
//            dropTable(db, "category1");
//            dropTable(db, "items1");
//            dropTable(db, "sumnames1");
//            dropTable(db, "buildcards1");
//            dropTable(db, "statusflag1"); //2015-12-29
//            dropTable(db, "offlineorders1"); //
//            dropTable(db, "schedule1"); //
//            dropTable(db, "scheduleitems1"); //
//
//        }
//        else
//        {
//            changeTableName(db, "category", "category1");
//            changeTableName(db, "items", "items1");
//            changeTableName(db, "sumnames", "sumnames1");
//            changeTableName(db, "buildcards", "buildcards1");
//            changeTableName(db, "statusflag", "statusflag1");
//            changeTableName(db, "offlineorders", "offlineorders1");
//
//            onCreate(db);
//            copyData(db, "category", "category1", null);
//            copyData(db, "items", "items1", null);
//            copyData(db, "sumnames", "sumnames1", null);
//            copyData(db, "buildcards", "buildcards1", null);
//            copyData(db, "statusflag", "statusflag1", null);
//            copyData(db, "offlineorders", "offlineorders1", null);
//
//            dropTable(db, "category1");
//            dropTable(db, "items1");
//            dropTable(db, "sumnames1");
//            dropTable(db, "buildcards1");
//            dropTable(db, "statusflag1"); //2015-12-29
//            dropTable(db, "offlineorders1"); //
//        }
//
//    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        changeTableName(db, "category", "category1");
        changeTableName(db, "items", "items1");
        changeTableName(db, "sumnames", "sumnames1");
        changeTableName(db, "buildcards", "buildcards1");
        changeTableName(db, "statusflag", "statusflag1");
        changeTableName(db, "offlineorders", "offlineorders1");
        changeTableName(db, "schedule", "schedule1");
        changeTableName(db, "scheduleitems", "scheduleitems1");

        onCreate(db);

        loadOldData(db);


    }

    public void loadOldData(SQLiteDatabase db)
    {
        copyData(db, "category", "category1");
        copyData(db, "items", "items1");
        copyData(db, "sumnames", "sumnames1");
        copyData(db, "buildcards", "buildcards1");
        copyData(db, "statusflag", "statusflag1");
        copyData(db, "offlineorders", "offlineorders1");
        copyData(db, "schedule", "schedule1");
        copyData(db, "scheduleitems", "scheduleitems1");

        clearOldData(db);
    }

    public void clearOldData(SQLiteDatabase db)
    {
        dropTable(db, "category1");
        dropTable(db, "items1");
        dropTable(db, "sumnames1");
        dropTable(db, "buildcards1");
        dropTable(db, "statusflag1"); //2015-12-29
        dropTable(db, "offlineorders1"); //
        dropTable(db, "schedule1"); //
        dropTable(db, "scheduleitems1"); //
    }


    public int getVersion() {
        return DB_VERSION;
    }
    static KDSDBRouter g_database = null;
    static public KDSDBRouter open(Context context) {
        if (g_database == null)
            g_database = new KDSDBRouter(context, KDSDBRouter.DB_NAME, null, KDSDBRouter.DB_VERSION);
        //KDSDBRouter d = new KDSDBRouter(context, KDSDBRouter.DB_NAME, null, KDSDBRouter.DB_VERSION);
        return g_database;

    }



    /***************************************************************************
     * @param
     * @return
     */
    public KDSRouterDataCategory categoryGetInfo(String guid) {

        String sql = "select GUID, Description,bg,"
                + " fg, tostation,toscreen,printable,delay,"
                + "DBTimeStamp from category where guid='" + guid + "'";// Common.KDSUtil.ConvertIntToString(nID);
        Cursor c = getDB().rawQuery(sql, null);

        if (!c.moveToNext())
            return null;
        return categoryGet(c);
    }

    private KDSRouterDataCategory categoryGetInfo(Cursor sf) {

        KDSRouterDataCategory c = new KDSRouterDataCategory();
        String guid = getString(sf,0);
        c.setGUID(getString(sf,0));
        c.setDescription(getString(sf,1));
        c.setBG(getInt(sf,2));
        c.setFG(getInt(sf,3));
        c.setToStation(getString(sf,4));
        c.setToScreen(getString(sf,5));
        c.setPrintable((getInt(sf,6) == 1));
        c.setDelay(getFloat(sf,7));
        c.setTimeStamp(getDate(sf, 8));

        return c;

    }

    private KDSRouterDataCategory categoryGet(Cursor sf) {

        KDSRouterDataCategory c = categoryGetInfo(sf);//
        c.setItems(itemsGet(c.getGUID()));
        return c;

    }

    static final String ITEM_FIELDS = "GUID, CategoryGUID, Description,PreparationTime,Delay,BG, FG,ToStation, ToScreen,Printable,BuildCards,TrainingVideo,DBTimeStamp,SumTrans,r0 ";

    public KDSRouterDataItems itemsGet(String categoryGUID)// int nOrderID)
    {
        String sql = "select "
                + ITEM_FIELDS
                + " from items where categoryguid = '" + categoryGUID + "'";

        Cursor c = getDB().rawQuery(sql, null);
        KDSRouterDataItems items = new KDSRouterDataItems();

        while (c.moveToNext()) {
            KDSRouterDataItem item = itemGet(c);
            items.addComponent(item);
        }
        return items;

    }

    public ArrayList<KDSRouterDataItem> itemsGetByCategory(String categoryGUID)// int nOrderID)
    {
        ArrayList<KDSRouterDataItem> ar = new ArrayList<>();
        String sql = "select "
                +ITEM_FIELDS
                + "from items where categoryguid = '" + categoryGUID + "'";

        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {
            KDSRouterDataItem item = itemGet(c);
            ar.add(item);

        }
        return ar;


    }


    public String itemGetGuidFromDescription(String itemDescription) {
        itemDescription = replaceSingleQuotation(itemDescription);
        String sql = "select  GUID from items where description='" + itemDescription + "'";
        Cursor c = getDB().rawQuery(sql, null);
        String guid = "";
        if (c.moveToNext()) {
            guid = getString(c,0);
        }
        c.close();
        return guid;

    }

    public String itemGetGuidFromDescription(String category, String itemDescription) {

        category = replaceSingleQuotation(category);
        itemDescription = replaceSingleQuotation(itemDescription);
        String categoryGuid = categoryGetGuidFromDescription(category);
        String sql = "";
        if (categoryGuid.isEmpty())
            sql = "select  GUID from items where description='" + itemDescription + "'";
        else
            sql =String.format("select  GUID from items where categoryguid='%s' and description='%s'", categoryGuid, itemDescription);

        Cursor c = getDB().rawQuery(sql, null);
        String guid = "";
        if (c.moveToNext()) {
            guid = getString(c,0);
        }
        c.close();
        return guid;

    }

    public void itemAddToFilter(KDSDataItem item)
    {
        //category = replaceSingleQuotation(category);
        //itemDescription = replaceSingleQuotation(itemDescription);
        String category = item.getCategory();
        String itemDescription = item.getDescription();

        String strCategoryGuid ="";
        if (!categoryDescriptionExisted(category))
        {
            KDSRouterDataCategory categoryInfo = new KDSRouterDataCategory();
            categoryInfo.setDescription(category);
            categoryAddInfo(categoryInfo);
            strCategoryGuid = categoryInfo.getGUID();

        }
        else
        {
            strCategoryGuid = categoryGetGuidFromDescription(category);
        }
        if (itemGetGuidFromDescription(itemDescription).isEmpty()) {
            KDSRouterDataItem itemRouter = new KDSRouterDataItem();
            itemRouter.setDescription(itemDescription);
            itemRouter.setCategoryGuid(strCategoryGuid);
            itemRouter.setModifiers(convertDataModifiersToRouterData(item.getModifiers()));
            itemAdd(itemRouter);
        }
    }

    private KDSRouterDataItemModifiers convertDataModifiersToRouterData(KDSDataModifiers modifiers)
    {
        KDSRouterDataItemModifiers ms = new KDSRouterDataItemModifiers();
        for (int i=0; i< modifiers.getCount(); i++)
        {
            KDSRouterDataItemModifier m = new KDSRouterDataItemModifier();
            m.setDescription(modifiers.getModifier(i).getDescription());
            m.setPrepTime(modifiers.getModifier(i).getPrepTime());
            ms.addComponent(m);
        }
        return ms;
    }
    /**
     * check the item go to which stations
     * if item is not set the tostation field, check the category
     * @param category
     * @param description
     * @return
     * Station:Screen
     */
    public String itemGetToStationWithScreen(String category, String description, String strDefaultStation)
    {
        category = replaceSingleQuotation(category);
        description = replaceSingleQuotation(description);
        String sql = "select tostation, toscreen from items where description='" + description + "'";
//        this.getDB().execSQL(sql);
        Cursor c = getDB().rawQuery(sql, null);

        if (!c.moveToNext()) {
            c.close();
            //try to add it to database.
            String strCategoryGuid = "";
            if (!categoryDescriptionExisted(category))
            {
                KDSRouterDataCategory categoryInfo = new KDSRouterDataCategory();
                categoryInfo.setDescription(category);
                categoryAddInfo(categoryInfo);
                strCategoryGuid = categoryInfo.getGUID();
            }
            else
            {
                strCategoryGuid = categoryGetGuidFromDescription(category);
            }
            KDSRouterDataItem item = new KDSRouterDataItem();
            item.setDescription(description);
            item.setCategoryGuid(strCategoryGuid);
            itemAdd(item);

            return strDefaultStation;
        }
        String toStation = getString(c,0);
        String toScreen = getString(c,1);
        c.close();
        if (!toStation.isEmpty())
        {
            if (toScreen.isEmpty())
                return toStation;
            else
                return toStation + ":" + toScreen;
        }
        //check category
        return categoryGetToStationWithScreen(category, strDefaultStation);

    }


    /**
     *
     * @param category
     * @param description
     * @return

     */
    /**
     *
     * @param category
     * @param description
     * @param arReturn
     *      *  array index 0, build cards,
     *  array index 1, training
     * @return
     *  0: no settings.
     */
    public int itemGetBuildCardsTrainingVideo(String category, String description, ArrayList<String> arReturn)
    {
        category = replaceSingleQuotation(category);
        description = replaceSingleQuotation(description);
        String sql = "select buildcards,trainingvideo from items where description='" + description + "'";
        Cursor c = getDB().rawQuery(sql, null);

        if (!c.moveToNext()) {
            c.close();
           return 0;
        }
        String buildcards = getString(c,0);
        String trainingvideo = getString(c,1);
        c.close();

        arReturn.clear();
        arReturn.add(buildcards);
        arReturn.add(trainingvideo);
        return arReturn.size();


    }


    /**
     * check the item go to which stations
     * if item is not set the tostation field, check the category
     * @param category
     * @param category
     * @return
     * Station:Screen
     */
    public String categoryGetToStationWithScreen(String category, String defaultStation)
    {
        category = replaceSingleQuotation(category);
        String sql = "select tostation, toscreen from category where description='" + category + "'";
        Cursor c = getDB().rawQuery(sql, null);

        if (!c.moveToNext()) {
            c.close();
            KDSRouterDataCategory categoryInfo = new KDSRouterDataCategory();
            categoryInfo.setDescription(category);
            categoryAddInfo(categoryInfo);
            return defaultStation;
        }
        String toStation = getString(c,0);
        String toScreen = getString(c,1);
        c.close();
        if (!toStation.isEmpty())
        {

            if (toScreen.isEmpty())
                return toStation;
            else
                return toStation + ":" + toScreen;
        }
        return defaultStation;


    }

    //GUID, OrderGUID, Name,Description,Qty,Category,BG, FG,Grp, Marked,DeleteByRemote,LocalBumped,BumpedStations, ToStations,Ready,Hiden,DBTimeStamp
    private KDSRouterDataItem itemGet(Cursor sf) {
        KDSRouterDataItem c = new KDSRouterDataItem();
        String guid = getString(sf,0);
        c.setGUID(guid);
        c.setCategoryGuid(getString(sf,1));
        c.setDescription(getString(sf,2));
        c.setPreparationTime(getFloat(sf,3));
        c.setDelay(getFloat(sf,4));
        c.setBG(getInt(sf,5));
        c.setFG(getInt(sf,6));
        c.setToStation(getString(sf,7));
        c.setToScreen(getString(sf,8));
        c.setPrintable((getInt(sf,9) == 1));
        c.setBuildCard(getString(sf,10));
        c.setTrainingVideo(getString(sf,11));

        c.setTimeStamp(getDate(sf, 12));
        c.setSumTranslateEnabled( (getInt(sf,13) == 1) );
        String s = getString(sf,14);
        if (s == null) s= "";
        c.setModifierEnabled(s.equals("1"));

        c.setSumNames(sumnamesGet(guid));
        c.setModifiers(modifiersGet(guid));

        return c;
    }

    public boolean sumnameGetEnabled(String category, String description)
    {
        category = replaceSingleQuotation(category);
        description = replaceSingleQuotation(description);
        String categoryGuid = categoryGetGuidFromDescription(category);
        String sql = "";
        if (categoryGuid.isEmpty())
            sql =String.format( "select sumtrans from items where description='%s'", description );
        else
            sql =String.format( "select sumtrans from items where categoryguid='%s' and description='%s'", categoryGuid, description );

        int n = this.executeOneValue(sql);
        return (n==1);

    }

    public KDSDataSumNames sumnamesGet(String category, String description)
    {
        //category = replaceSingleQuotation(category);
        //description = replaceSingleQuotation(description);
        KDSDataSumNames sumNames = new KDSDataSumNames();
        if (!sumnameGetEnabled(category, description))
            return sumNames;
        String itemGuid = itemGetGuidFromDescription(category, description);
        if (itemGuid.isEmpty())
            return sumNames;
        return sumnamesGet(itemGuid);

    }

    public KDSDataSumNames sumnamesGet(String itemGUID)
    {

        String sql = "select GUID,ItemGUID,Description,Qty,DBTimeStamp from sumnames where itemguid='" + itemGUID + "'";// Common.KDSUtil.ConvertIntToString(nItemID);

        Cursor c = getDB().rawQuery(sql, null);

        KDSDataSumNames sumNames = new KDSDataSumNames();

        while (c.moveToNext()) {
            KDSDataSumName sumname = sunnameGet(c);
            sumNames.addComponent(sumname);
        }
        return sumNames;
    }


    // GUID,ItemGUID,Name,Description,Qty, BG, FG, Hiden, Bumped,DBTimeStamp
    private KDSDataSumName sunnameGet(Cursor sf) {
        KDSDataSumName c = new KDSDataSumName();

        String guid = getString(sf,0);
        c.setGUID(guid);
        c.setItemGuid(getString(sf,1));
        c.setDescription(getString(sf,2));
        c.setSumQty(getFloat(sf,3));
        c.setTimeStamp(getDate(sf, 4));
        return c;
    }


    public boolean categoryAddInfo(KDSRouterDataCategory category) {

        String sql = category.sqlAddNew();
        boolean bTransactionByMe = false;
        try {
            bTransactionByMe = startTransaction();

            executeDML(sql);

            commitTransaction(bTransactionByMe);
            return true;
        } catch (Exception e) {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        } finally {
            this.endTransaction(bTransactionByMe);
        }

    }

    public boolean itemAdd(KDSRouterDataItem item) {

        item.updateSumNamesGuid();
        item.updateModifiersGuid();
        String sql = item.sqlAddNew();
        boolean bTransactionByMe = false;
        try {
            bTransactionByMe = this.startTransaction();
            executeDML(sql);
            KDSDataSumNames sumNames = item.getSumNames();
            if (sumNames != null)
                sumnamesAdd(sumNames);//, stmt);
            KDSRouterDataItemModifiers modifiers = item.getModifiers();
            if (modifiers != null)
                modifiersAdd(modifiers);
            this.commitTransaction(bTransactionByMe);
            return true;
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        } finally {
            this.endTransaction(bTransactionByMe);

        }

    }


    private boolean sumnamesAdd(KDSDataSumNames sumNames) {
        int ncount = sumNames.getCount();
        String sql = "";
        try {
            for (int i = 0; i < ncount; i++) {
                KDSDataSumName sumname = (KDSDataSumName) sumNames.getComponent(i);
                sql = sumname.sqlAddNew();
                if (!this.executeDML(sql)) return false;

            }
            return true;
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }

    private boolean sumnamesClear(String itemGuid) {
        String sql = "delete from sumnames where itemguid='" + itemGuid + "'";
        return this.executeDML(sql);
    }



    public boolean categoryInfoUpdate(KDSRouterDataCategory category) {
        String sql = category.sqlUpdate();
        return this.executeDML(sql);
    }

    public boolean categoryExisted(String guid)
    {
        String sql = "select count(*) from category where guid='" + guid +"'";
        int n=  this.executeOneValue(sql);
        return (n >0);
    }

    public boolean categoryDescriptionExisted(String categoryDescription)
    {
        categoryDescription = replaceSingleQuotation(categoryDescription);

        String sql = "select count(*) from category where description='" + categoryDescription +"'";
        int n=  this.executeOneValue(sql);
        return (n >0);
    }


    public boolean itemExisted(String guid)
    {
        String sql = "select count(*) from items where guid='" + guid +"'";
        int n=  this.executeOneValue(sql);
        return (n >0);
    }


    public boolean itemUpdate(KDSRouterDataItem item) {
        item.updateSumNamesGuid();
        item.updateModifiersGuid();
        String sql = item.sqlUpdate();
        if (!this.executeDML(sql))
            return false;
        sumnamesClear(item.getGUID());
        sumnamesAdd(item.getSumNames());
        modifiersClear(item.getGUID());
        modifiersAdd(item.getModifiers());
        return true;
    }

    public boolean categoryDelete(String guid)//int nID)
    {
        String sql = KDSRouterDataCategory.sqlDelete( guid);
        if (!this.executeDML(sql))
            return false;
        sql = "select guid from items where categoryguid='" + guid + "'";// Common.KDSUtil.ConvertIntToString(nID);

        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {
            String itemguid = getString(c,0);
            itemDelete(itemguid);
        }
        c.close();
        return true;
    }


    public boolean itemDelete(String itemGUID)//int nID)
    {
        String sql = KDSRouterDataItem.sqlDelete(itemGUID);
        if (!this.executeDML(sql)) return false;
        sql = "delete from sumnames where ItemGUID='" + itemGUID + "'";// Common.KDSUtil.ConvertIntToString(nID);
        if (!this.executeDML(sql)) return false;

        return true;
    }

    public String categoryGetGuidFromDescription(String categoryDescription) {
        categoryDescription = replaceSingleQuotation(categoryDescription);
        String sql = "select guid from category where description='" + categoryDescription + "'";
        ArrayList ar = new ArrayList();

        Cursor c = getDB().rawQuery(sql, null);
        String guid = "";
        if (c.moveToNext()) {
            guid = getString(c,0);

        }
        c.close();
        return guid;
    }



    public ArrayList<String> categoryGetAllGUID(KDSDBRouter db) {
        ArrayList<String> ar = new ArrayList<String>();
        String sql = "select guid from category";
        Cursor c = db.getDB().rawQuery(sql, null);

        while (c.moveToNext()) {
            String guid = getString(c,0);
            ar.add(guid);


        }
        c.close();
        return ar;
    }

    public ArrayList<KDSDataShortName> categoryGetAllShortNames( ) {
        ArrayList<KDSDataShortName> ar = new ArrayList<>();

        String sql = "select guid,Description,ToStation from category";
        Cursor c = this.getDB().rawQuery(sql, null);

        while (c.moveToNext()) {
            String guid = getString(c,0);
            String name = getString(c,1);
            String toStation = getString(c,2);
            KDSDataShortName shortName = new KDSDataShortName(guid, name, toStation);
            ar.add(shortName);
        }
        c.close();
        return ar;
    }

    public ArrayList<String> itemGetAllItemsGUID( String categoryGUID) {
        ArrayList<String> ar = new ArrayList<String>();
        String sql = "select guid from items where categoryguid='" + categoryGUID + "'";

        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {
            String guid = getString(c,0);
            ar.add(guid);
        }
        c.close();
        return ar;
    }

    public ArrayList<KDSDataShortName> itemGetAllItemsShortNames( String categoryGUID) {
        ArrayList<KDSDataShortName> ar = new ArrayList<>();
        String sql = "select guid,description,ToStation from items where categoryguid='" + categoryGUID + "'";

        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {
            String guid = getString(c,0);
            String description = getString(c,1);
            String toStation = getString(c,2);
            KDSDataShortName name = new KDSDataShortName(guid, description, toStation);
            ar.add(name);
        }
        c.close();
        return ar;
    }



    public ArrayList<KDSRouterDataCategory> categoryLoadAllInfo() {

        ArrayList<KDSRouterDataCategory> ar = new ArrayList<>();

        String sql = "select guid from category";
        Cursor c = getDB().rawQuery(sql, null);
        while (c.moveToNext()) {

            String guid = getString(c,0);
            KDSRouterDataCategory category = this.categoryGetInfo(guid);
            ar.add(category);
        }
        c.close();
        return ar;
    }


    public void clear() {
        boolean bStartedByMe = this.startTransaction();
        String sql = "delete from category";
        this.executeDML(sql);
        sql = "delete from items";
        this.executeDML(sql);
        sql = "delete from sumnames";
        this.executeDML(sql);

        this.commitTransaction(bStartedByMe);
        this.endTransaction(bStartedByMe);

    }

    static public String replaceSingleQuotation(String str) {
        String s = str;
        s = s.replace("'", "''");
        return s;
    }

    public String makeInsertValues(Cursor c) {
        String s = "";
        int nColCount = c.getColumnCount();
        for (int i = 0; i < nColCount; i++) {
            if (i > 0) s += ",";
            s += "'";
            s += getString(c,i);
            s += "'";
        }
        return s;
    }

    public ArrayList<String> outputDatabaseSqlStrings() {
        ArrayList<String> arsql = new ArrayList<>();

        arsql.add( "delete from category");
        arsql.add("delete from items");
        arsql.add("delete from sumnames");
        ArrayList<String> arCategorysGuid = categoryGetAllGUID(this);
        for (int i = 0; i < arCategorysGuid.size(); i++) {
            String categoryGuid = arCategorysGuid.get(i);
            String whereCondition = "where guid='" + categoryGuid + "'";
            ArrayList<String> arOrders = outputTableDataSql(this, "category", whereCondition);
            arsql.addAll(arOrders);

            whereCondition = "where categoryguid='" + categoryGuid + "'";
            ArrayList<String> arItems = outputTableDataSql(this, "items", whereCondition);
            arsql.addAll(arItems);


            ArrayList<String> arItemsGuid =  itemGetAllItemsGUID( categoryGuid);
            for (int j = 0; j < arItemsGuid.size(); j++) {
                String itemguid = arItemsGuid.get(j);
                whereCondition = "where itemguid='" + itemguid + "'";
                ArrayList<String> arCondiments = outputTableDataSql(this, "sumnames", whereCondition);
                arsql.addAll(arCondiments);

            }

        }
        return arsql;
    }


    /**
     * 2015-12-26
     * for smart order feature, get the item preparation time
     *
     * @param strItemDescription
     * @return
     */
    public float itemGetPreparationTime(String strItemDescription)
    {
        String s = strItemDescription;
        s = KDSUtil.fixSqliteSingleQuotationIssue(s);
        String sql = "select PreparationTime from items where description='" + s + "'";
        Cursor c = getDB().rawQuery(sql, null);
        float flt  = 0;
        if (c.moveToNext()) {
            flt  =  getFloat(c,0);
            c.close();
        }

        return flt;

    }

    /**
     * find the category delay setting value
     * This is the "typedelay".
     * @param categoryDescription
     * @return
     */
    public float categoryGetDelay(String categoryDescription)
    {
        String s = categoryDescription;
        s = KDSUtil.fixSqliteSingleQuotationIssue(s);
        String sql = "select delay from Category where description='" + s + "'";
        Cursor c = getDB().rawQuery(sql, null);
        float flt = 0;
        if (c.moveToNext()) {
            flt  =  getFloat(c,0);
            c.close();
        }

        return flt;

    }

    /**
     * 2015-12-29
     * get a value according to the database rows,
     * It is for router database backup feature.
     * @return
     */
    public String getDbChangesFlag()
    {
        String sql = "select guid,dbtimestamp from statusflag";
        Cursor c = getDB().rawQuery(sql, null);
        String s = "";
        if (c.moveToNext()) {
            s =  getString(c,0);
            Date dt = getDate(c, 1);
            c.close();
            s+=("_"+KDSUtil.convertIntToString(dt.getTime()));
        }
        return s;
    }

    public String getChangesGUID()
    {
        String sql = "select guid from statusflag";
        Cursor c = getDB().rawQuery(sql, null);
        String s = "";
        if (c.moveToNext()) {
            s =  getString(c,0);

            c.close();

        }
        return s;
    }
    /**
     * 2015-12-29
     * Use this value to identify this database last changes.
     */
    public void updateDbChangesFlag()
    {
        String guid = KDSUtil.createNewGUID();
        String sql = "delete from statusflag";
        this.exeSqlWithoutFlagChange(sql);
        sql = "insert into statusflag(guid) values('"+guid+"')";
        this.exeSqlWithoutFlagChange(sql);
    }

    public void setChangesGuid(String strGuid)
    {
        String guid = strGuid;
        String sql = "delete from statusflag";
        this.exeSqlWithoutFlagChange(sql);
        sql = "insert into statusflag(guid) values('"+guid+"')";
        this.exeSqlWithoutFlagChange(sql);
    }
    /**
     * 2015-12-29
     * @param sql
     * @return
     */
    private boolean exeSqlWithoutFlagChange(String sql)
    {
        try {
            this.getDB().execSQL(sql);
            return true;
        }catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        return false;
    }

    /**
     * for update the status code.
     * @param sql
     * @return
     */
    @Override
    public boolean executeDML(String sql)
    {
        if (!super.executeDML(sql))
            return false;
        updateDbChangesFlag();
        return true;

    }

    public boolean addLostStationXmlOrder(String toStations, String orderXml)
    {
        String strSingleQuote = orderXml;
        strSingleQuote = KDSUtil.fixSqliteSingleQuotationIssue(strSingleQuote);
        String sql = String.format("insert into offlineorders(tostations,orderxml) values('%s','%s')", toStations, strSingleQuote);
        return this.executeDML(sql);
    }

    public int getLostStationsXmlOrdersCount()
    {
        String sql = "select count(*) from offlineorders";

        return this.executeOneValue(sql);
    }


    public int getLostStationsXmlOrders(String stationID, ArrayList<Integer> arIDs,ArrayList<String> arOrdersXml)
    {
        String sql = "select id,OrderXml from offlineorders where ToStations='" + stationID +"'";
        try {
            Cursor c = getDB().rawQuery(sql, null);
            String s = "";
            while (c.moveToNext()) {
                int n = getInt(c,0);
                arIDs.add(n);

                s = getString(c,1);
                arOrdersXml.add(s);
            }
            c.close();
            return arIDs.size();
        }catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        return 0;
    }

    public boolean deleteLostStationsXmlOrders(int nID)
    {
        String sql = String.format("delete from offlineorders where id=%d", nID);
        return this.executeDML(sql);
    }

    public boolean deleteLostStationsXmlOrders(ArrayList<Integer> arIDs)
    {
        boolean breturn = true;
        for (int i=0; i<arIDs.size(); i++) {
            if (!deleteLostStationsXmlOrders(arIDs.get(i)) )
                breturn = false;
        }
        return breturn;
    }

    /**
     *
     * @param nMaxCount
     * @return
     *    how many orderxml was deleted
     */
    public int keepMaxLostStationisXmlOrders(int nMaxCount)
    {
        if (getLostStationsXmlOrdersCount() <=nMaxCount)
            return 0;
        String sql = "select id from offlineorders order by id";
        Cursor c = getDB().rawQuery(sql, null);
        String s = "";
        ArrayList<Integer> ar = new ArrayList<>();
        while (c.moveToNext()) {

            int id = getInt(c,0);
            ar.add(id);
        }
        c.close();
        int ncount = ar.size() - nMaxCount;
        for (int i=0; i< ncount; i++)
        {
            deleteLostStationsXmlOrders(ar.get(i));
        }
        return ncount;
    }

    public void scheduleAdd(WeekEvent evt)
    {
        scheduleDelete(evt.getGUID());
        String sql = evt.sqlAddNew();
        this.getDB().execSQL(sql);
        for (int i=0; i< evt.getItems().size(); i++)
        {
            evt.getItems().get(i).setOrderGUID(evt.getGUID());
            sql = WeekEvent.sqlItemAddNew( evt.getItems().get(i));
            this.getDB().execSQL(sql);
        }

    }

    public void scheduleDelete(String evtGuid)
    {

        String sql = WeekEvent.sqlDelete(evtGuid);
        this.getDB().execSQL(sql);
        this.getDB().execSQL(WeekEvent.sqlDeleteItems(evtGuid));


    }

    public ArrayList<WeekEvent> scheduleGetAll()
    {
        ArrayList<WeekEvent> ar = new ArrayList<>();
        String sql = "select GUID, Description,OrderID,weekday,starttime,endtime,firedtime from schedule";
        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext()) {
            ar.add(scheduleGet(c));
        }
        c.close();
        return ar;
    }


    public boolean scheduleClearAll()
    {
        String sql = "delete from schedule";
        this.getDB().execSQL(sql);
        sql = "delete from scheduleitems";
        this.getDB().execSQL(sql);
        return true;
    }

    public WeekEvent scheduleGet(Cursor c)
    {
        WeekEvent evt = new WeekEvent();
        evt.setGUID(getString(c,0));
        evt.setSubject(getString(c,1));
        evt.setOrderID(getString(c,2));
        evt.setWeekDay(getInt(c,3));
        evt.setTimeFrom(getFloat(c,4));
        evt.setEndTime(getFloat(c,5));
        String s = getString(c,6);

        Date dt =  KDSUtil.convertStringToDate(s, KDSUtil.createInvalidDate());
        evt.setFiredTime(dt);

        String guid = evt.getGUID();
        evt.setItems(scheduleItemsGet(guid));
        return evt;
    }

    public ArrayList<KDSDataItem> scheduleItemsGet(String schGuid)
    {
        String sql = "select GUID, qty,Description,category,tostation from scheduleitems where schguid='"+schGuid +"'";
        Cursor c = getDB().rawQuery(sql, null);
        ArrayList<KDSDataItem> items = new ArrayList<>();

        while (c.moveToNext()) {
            KDSDataItem item = new KDSDataItem();
            item.setGUID(getString(c,0));
            item.setQty(getFloat(c,1));
            item.setDescription(getString(c,2));
            item.setCategory(getString(c,3));
            item.setToStationsString(getString(c,4));
            item.setOrderGUID(schGuid);
            items.add(item);
        }
        c.close();
        return items;
    }

    public void setScheduleEventFiredTime(String scheduleGuid, Date dt)
    {
        String sql = "update schedule set firedtime='" + KDSUtil.convertDateToString(dt) +"'";
        getDB().execSQL(sql);

    }

    /**
     *
     * @return
     */
    public String export2CSV()
    {
        String s = "";
        ArrayList<KDSRouterDataCategory> categories = categoryLoadAllInfo();
        for (int i=0; i< categories.size(); i++)
        {
            s += categories.get(i).toCSV();
            s += "\n";
            KDSRouterDataItems items = itemsGet(categories.get(i).getGUID());
            s += exportItems2CSV(items);
        }
        return s;

    }
    private String exportItems2CSV(KDSRouterDataItems items)
    {
        String s = "";
        for (int i=0; i< items.getCount(); i++)
        {
            s +=items.getItem(i).toCSV();
            s += "\n";
        }
        return s;

    }

    /**
     *
     * @return
     */
    public int importFromCSV(String strCSV)
    {
        if (strCSV.isEmpty()) return 0;
        ArrayList<String> ar = KDSUtil.spliteString(strCSV, "\n");
        KDSRouterDataCategory category = null;
        for (int i=0; i< ar.size(); i++)
        {
            String s = ar.get(i);
            if (KDSRouterDataCategory.csvIsCategory(s))
            {
                category = new KDSRouterDataCategory();

                if (category.fromCSV(s)) {
                    this.categoryAddInfo(category);
                }
            }
            else
            {
                KDSRouterDataItem item = new KDSRouterDataItem();
                if (item.fromCSV(s)) {
                    if (category != null) {
                        item.setCategoryGuid(category.getGUID());
                        this.itemAdd(item);
                    }
                }
            }
        }
        return 0;
    }


    private boolean modifiersAdd(KDSRouterDataItemModifiers modifiers) {
        int ncount = modifiers.getCount();
        String sql = "";
        try {
            for (int i = 0; i < ncount; i++) {
                KDSRouterDataItemModifier m = (KDSRouterDataItemModifier) modifiers.getComponent(i);
                sql = m.sqlAddNew();
                if (!this.executeDML(sql)) return false;

            }
            return true;
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }

    private boolean modifiersClear(String itemGuid) {
        String sql = "delete from modifiers where itemguid='" + itemGuid + "'";
        return this.executeDML(sql);
    }

    public boolean modifierExisted(String itemGUID, String modifierDescription)
    {
        modifierDescription = KDSUtil.fixSqliteSingleQuotationIssue(modifierDescription);
        String sql = "select count(*) from modifiers where itemguid='" + itemGUID + "' and upper(description)='"+modifierDescription.toUpperCase() + "'";
        int n = executeOneValue(sql);
        return (n>0);
    }
    public KDSRouterDataItemModifiers modifiersGet(String itemGUID)
    {

        String sql = "select GUID,ItemGUID,Description,preptime,DBTimeStamp from modifiers where itemguid='" + itemGUID + "'";

        Cursor c = getDB().rawQuery(sql, null);

        KDSRouterDataItemModifiers modifiers = new KDSRouterDataItemModifiers();

        while (c.moveToNext()) {
            KDSRouterDataItemModifier m = modifierGet(c);
            modifiers.addComponent(m);
        }
        return modifiers;
    }

    // GUID,ItemGUID,Name,Description,preptime,DBTimeStamp
    private KDSRouterDataItemModifier modifierGet(Cursor sf) {
        KDSRouterDataItemModifier c = new KDSRouterDataItemModifier();

        String guid = getString(sf,0);
        c.setGUID(guid);
        c.setItemGuid(getString(sf,1));
        c.setDescription(getString(sf,2));
        c.setPrepTime(getInt(sf,3));
        c.setTimeStamp(getDate(sf, 4));
        return c;
    }


    public KDSRouterDataItemModifiers modifiersGet(String category, String description)
    {

        KDSRouterDataItemModifiers sumNames = new KDSRouterDataItemModifiers();
        if (!modifiersGetEnabled(category, description))
            return sumNames;
        String itemGuid = itemGetGuidFromDescription(category, description);
        if (itemGuid.isEmpty())
            return sumNames;
        return modifiersGet(itemGuid);

    }

    public boolean modifierAdd(KDSRouterDataItemModifier modifier)
    {
        String sql = modifier.sqlAddNew();
        if (!this.executeDML(sql)) return false;
        return true;
    }
    public int modifierGetPrepTime(String category, String itemDescription, String modifierDescription, boolean bAutoAdd)
    {

        String itemGuid = itemGetGuidFromDescription(category, itemDescription);
        if (itemGuid.isEmpty()) return 0;

        if (!modifierExisted(itemGuid, modifierDescription))
        {
            if (bAutoAdd) {
                KDSRouterDataItemModifier m = new KDSRouterDataItemModifier();
                m.setPrepTime(0);
                m.setDescription(modifierDescription);
                modifierAdd(m);
            }
            return 0;


        }
        String sql = "";
        modifierDescription = replaceSingleQuotation(modifierDescription);
        sql =String.format( "select preptime from modifiers where itemguid='%s' and description='%s' ", itemGuid, modifierDescription );


        try {
            if (getDB() == null) return 0;
            int n = this.executeOneValue(sql);
            return n;


        }catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
        }
        return 0;

    }

    public boolean modifiersGetEnabled(String category, String description)
    {
        category = replaceSingleQuotation(category);
        description = replaceSingleQuotation(description);
        String categoryGuid = categoryGetGuidFromDescription(category);
        String sql = "";
        if (categoryGuid.isEmpty())
            sql =String.format( "select r0 from items where description='%s'", description );
        else
            sql =String.format( "select r0 from items where categoryguid='%s' and description='%s'", categoryGuid, description );

        try {
            if (getDB() == null) return false;
            Cursor c = getDB().rawQuery(sql, null);
           String s = "0";
            if (c.moveToNext())
                s = getString(c,0);
            c.close();
            return (s.equals("1"));

        }catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
        }
        return false;


    }


    public KDSRouterDataItem itemGetBGFG(String category, String strItemDescription)
    {

        String guid = itemGetGuidFromDescription(category, strItemDescription);
        int bg=0, fg=0;
        String sql = "";
        //check item
        if (!guid.isEmpty()) {
            sql = "select bg,fg from items where guid='" + guid + "'";
            Cursor c = getDB().rawQuery(sql, null);
            if (c.moveToNext()) {
                bg = getInt(c, 0);
                fg = getInt(c, 1);

                c.close();
            }
        }
        //check category
        if (bg ==0 && fg ==0)
        {
            guid = categoryGetGuidFromDescription(category);
            if (!guid.isEmpty()) {
                sql = "select bg,fg from category where guid='" + guid + "'";
                Cursor c = getDB().rawQuery(sql, null);
                if (c.moveToNext()) {
                    bg = getInt(c, 0);
                    fg = getInt(c, 1);
                    c.close();
                }

            }
        }

        KDSRouterDataItem item = new KDSRouterDataItem();
        item.setBG(bg);
        item.setFG(fg);

        return item;

    }


    /***************************************************************************
     * SQL definitions
     */
    //without the last ")", for statistic db sql.
    public static final String Table_Category_Fields = "Create table Category ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "GUID text(36),"
            + "Description text(256),"
            + "bg int,"
            + "fg int," //start time
            + "ToStation text(64),"
            + "ToScreen text(64),"
            + "Printable int default 0,"
            + "Delay float,"
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
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')) ";// )";


    private static final String Table_Category = Table_Category_Fields + ")";

    public static final String Table_Items = "Create table Items ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "GUID text(36),"
            + "CategoryID int,"
            + "CategoryGUID text(36),"
            + "Description text(256),"
            + "PreparationTime float,"
            + "Delay float," //modify original qty by this value
            + "BG long,"
            + "FG long,"
            + "ToStation text(64),"
            + "ToScreen text(64),"
            + "Printable int," //xml command delete this item
            + "SumTrans int," //sum translate enabled or not.
            + "BuildCards text(128),"
            + "TrainingVideo text(128),"
            +"r0 text(16)," //modifiers enabled or disabled
            +"r1 text(16),"
            +"r2 text(16),"
            +"r3 text(16),"
            +"r4 text(16),"
            +"r5 text(16),"
            +"r6 text(16),"
            +"r7 text(16),"
            +"r8 text(16),"
            +"r9 text(16) ,"
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')) )";


    public static final String Table_SumName = "Create table SumNames ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "GUID text(36),"
            + "ItemID int,"
            + "ItemGUID text(36),"
            + "Description text(256),"
            + "Qty float,"
            +"r0 text(16),"
            +"r1 text(16),"
            +"r2 text(16),"
            +"r3 text(16),"
            +"r4 text(16),"
            +"r5 text(16),"
            +"r6 text(16),"
            +"r7 text(16),"
            +"r8 text(16),"
            +"r9 text(16) ,"
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')) )";

    public static final String Table_Modifiers = "Create table Modifiers ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "GUID text(36),"
            + "ItemID int,"
            + "ItemGUID text(36),"
            + "Description text(256),"
            + "PrepTime int," //seconds
            +"r0 text(16),"
            +"r1 text(16),"
            +"r2 text(16),"
            +"r3 text(16),"
            +"r4 text(16),"
            +"r5 text(16),"
            +"r6 text(16),"
            +"r7 text(16),"
            +"r8 text(16),"
            +"r9 text(16) ,"
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')) )";

    //
    public static final String Table_BuildCard = "Create table BuildCards ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "GUID text(36),"
            + "ItemID int," //it can been item or order id
            + "ItemGUID text(36),"
            + "Description text(256),"
            + "Idx int,"
            + "File text(256),"
            +"r0 text(16),"
            +"r1 text(16),"
            +"r2 text(16),"
            +"r3 text(16),"
            +"r4 text(16) ,"
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')) )";
    //2015-12-29
    /**
     * use this table to compare database, check if they are same
     */
    public static final String Table_Flag = "Create table StatusFlag ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "GUID text(36),"
            +"r0 text(16),"
            +"r1 text(16),"
            +"r2 text(16),"
            +"r3 text(16),"
            +"r4 text(16) ,"
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')) )";


    /**
     * for station offline, save order to this table.
     */
    public static final String Table_Offline_Orders = "Create table OfflineOrders ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "ToStations text(10),"//record which station lost, format: 1,2,4,5 csv
            + "OrderXml text(256),"
            +"r0 text(16),"
            +"r1 text(16),"
            +"r2 text(16),"
            +"r3 text(16),"
            +"r4 text(16) ,"
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime')) )";


    public static final String Table_Schedule_Fields = "Create table Schedule ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "GUID text(36),"
            + "Description text(256),"
            + "OrderID text(256),"
            + "weekday int,"
            + "starttime float,"
            + "endtime float," //start time
            + "firedtime date,"
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
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime'))) ";// )";

    public static final String Table_ScheduleItems_Fields = "Create table ScheduleItems ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "GUID text(36),"
            + "SchGUID text(36),"
            + "Description text(256),"
            + "category text(256),"
            + "qty float,"
            + "ToStation text(64),"
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
            + "DBTimeStamp TimeStamp NOT NULL DEFAULT (datetime('now','localtime'))) ";// )";

}
