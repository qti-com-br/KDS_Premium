package com.bematechus.kdslib;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by David.Wong on 2019/6/10.
 * Rev:
 */
public class KDSDBOffline extends KDSDBBase {

    static public final int DB_VERSION = 4; //

    static public final String DB_NAME = "offline.db";
    static private final String TAG = "KDSDBOffline";

    static KDSDBOffline g_database = null;
    static public KDSDBOffline open(Context context) {
        if (g_database == null)
            g_database = new KDSDBOffline(context, KDSDBOffline.DB_NAME, null, KDSDBOffline.DB_VERSION);

        return g_database;

    }

    public KDSDBOffline(Context context, String name, SQLiteDatabase.CursorFactory factory,
                       int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String ar[] = new String[]{
                Table_Offline,
                Table_ACK,
        };
        exeBatchSql(db, ar);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        dropTable(db, "offlinedata");
        dropTable(db, "ackdata");
        onCreate(db);
    }
    public int getVersion() {
        return DB_VERSION;
    }



    public boolean offlineAdd(String stationID, String data)
    {
        data = KDSUtil.fixSqliteSingleQuotationIssue(data);
        String sql = String.format("insert into offlinedata(stationid,data) values('%s','%s')",
                                         stationID, data);
        return this.executeDML(sql);
    }

    public boolean offlineAdd(String stationID, String data, String orderGuid, String itemGuid, String strDescription)
    {
        data = KDSUtil.fixSqliteSingleQuotationIssue(data);
        String sql = String.format("insert into offlinedata(stationid,data,orderguid,itemguid,description) values('%s','%s','%s','%s','%s')",
                stationID, data,orderGuid,itemGuid, strDescription);
        return this.executeDML(sql);
    }

    public NoConnectionDataBuffer offlineGet(String stationID)
    {
        String sql =String.format( "select data,orderguid,itemguid,description from offlinedata where stationid='%s' order by id",
                                          stationID);

        NoConnectionDataBuffer buffer = new NoConnectionDataBuffer(stationID);

        Cursor c = getDB().rawQuery(sql, null);

        while (c.moveToNext())
        {

            String data = getString(c,0);
            String orderguid = getString(c,1);
            String itemguid = getString(c,2);
            String description = getString(c,3);
            KDSStationDataBuffered offline = buffer.addBufferedData(data, -1);
            offline.setOrderGuid(orderguid);
            offline.setItemGuid(itemguid);
            offline.setDescription(description);


        }
        c.close();
        return buffer;
    }

    public boolean ackAdd(String stationID, String ackGuid, String data, Date dtSend)
    {

        data = KDSUtil.fixSqliteSingleQuotationIssue(data);
        String sql = String.format("insert into ackdata(stationid,ackguid,data,senddt) values('%s','%s','%s','%s')",
                stationID, ackGuid,data,KDSUtil.convertDateToString(dtSend));
        return this.executeDML(sql);
    }

    public boolean ackAdd(String stationID, AckData ack)
    {
        return ackAdd(stationID, ack.getGuid(), ack.getXmlData(), ack.getSendDate());

//        String data = ack.getXmlData();
//        data = KDSUtil.fixSqliteSingleQuotationIssue(data);
//        String sql = String.format("insert into ackdata(stationid,ackguid,data,senddt) values('%s','%s','%s','%s')",
//                stationID,ack.getGuid(), data, KDSUtil.convertDateToString(ack.getSendDate() ));
//        return this.executeDML(sql);
    }
    public boolean ackRemove(String ackGuid)
    {
        String sql =String.format("delete from ackdata where ackguid='%s'",
                                    ackGuid);
        return this.executeDML(sql);
    }

//    public AckDataStation getStationAckData(String stationID)
//    {
//        String sql =String.format( "select ackguid,data,senddt from ackdata where stationid='%s' order by id",
//                stationID);
//
//
//        AckDataStation station = new AckDataStation(stationID);
//
//        Cursor c = getDB().rawQuery(sql, null);
//
//        if (c.moveToNext())
//        {
//
//            String guid = getString(c,0);
//            String data = getString(c,1);
//            Date dt = getDate(c,2);
//            AckData a = new AckData(guid, data, dt);
//
//            station.getData().add(a);
//
//        }
//        c.close();
//        return station;
//    }

    public ArrayList<String> ackGetStations()
    {
        String sql = "select stationid from ackdata group by stationid";
        Cursor c = getDB().rawQuery(sql, null);
        ArrayList<String> stations = new ArrayList<>();

        while (c.moveToNext())
        {
            String stationid = getString(c,0);
            stations.add(stationid);
        }
        c.close();
        return stations;
    }
    public boolean ackIsWaiting()
    {
        String sql = "select count(*) from ackdata";
        int n = this.executeOneValue(sql);
        return (n>0);
    }

    /**
     *
     * @param stationID
     * @param nTimeoutMs
     * @param nMaxCount
     *  Get how many rows
     * @return
     */
    public AckDataStation ackGetTimeout(String stationID, int nTimeoutMs, int nMaxCount)
    {
        Date dtDeadline = new Date();
        long l = dtDeadline.getTime();
        l -= nTimeoutMs;
        dtDeadline.setTime(l);

        String sql = String.format("select ackguid, data, senddt from ackdata where stationid='%s' and senddt<'%s' order by id",
                                    stationID, KDSUtil.convertDateToString(dtDeadline));
        if (nMaxCount >0)
            sql += " limit " + KDSUtil.convertIntToString(nMaxCount);
        Cursor c = getDB().rawQuery(sql, null);

        AckDataStation station = new AckDataStation(stationID);

        while (c.moveToNext())
        {
            String guid = getString(c,0);
            String data = getString(c,1);
            Date dt = getDate(c,2);
            AckData a = new AckData(guid, data, dt);

            station.getData().add(a);
        }
        c.close();
        return station;

    }

    public boolean ackResetTime(String ackGuid)
    {
        Date dt = new Date();
        String sql =String.format("update ackdata set senddt='%s' where ackguid='%s'",
                                 KDSUtil.convertDateToString(dt), ackGuid);
        return this.executeDML(sql);

    }

    public int offlineDataCount(String stationID)
    {
        String sql = String.format("select count(*) from offlinedata where stationid='%s'", stationID);
        return this.executeOneValue(sql);
    }

    public int ackCount(String stationID)
    {
        String sql = String.format("select count(*) from ackdata where stationid='%s'", stationID);
        return this.executeOneValue(sql);
    }

    public boolean offlineRemove(String stationID)
    {
        String sql = String.format("delete from offlinedata where stationid='%s'", stationID);
        return this.executeDML(sql);
    }

    public ArrayList<String> offlineGetStations()
    {
        String sql = "select stationid from offlinedata group by stationid";
        Cursor c = getDB().rawQuery(sql, null);
        ArrayList<String> stations = new ArrayList<>();

        while (c.moveToNext())
        {
            String stationid = getString(c,0);
            stations.add(stationid);
        }
        c.close();
        return stations;
    }

    public ArrayList<String> offlineAckGetStations()
    {
        ArrayList<String> stations = offlineGetStations();
        ArrayList<String> stationsack = ackGetStations();
        stations.addAll(stationsack);
        return stations;


    }

    public void clearAck()
    {
        String sql = "delete from ackdata";
        this.executeDML(sql);

    }
    public void clearOffline()
    {
        String sql = "delete from offlinedata";
        this.executeDML(sql);
    }
    /*****************************************************************************************/
    public static final String Table_Offline = "Create table offlinedata ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "stationid text(36),"
            + "data text(256),"
            + "orderguid text(16),"//for order transfer
            + "itemguid text(16)," //for item transfer
            + "description text(64)," //for transfer done message
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

    public static final String Table_ACK = "Create table ackdata ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "stationid text(36),"
            + "ackguid text(36),"
            + "data text(256),"
            + "senddt date," //start time
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
}
