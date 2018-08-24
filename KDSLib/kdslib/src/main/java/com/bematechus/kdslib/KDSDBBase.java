/**
 * Created by Administrator on 2015/7/29 0029.
 */
package com.bematechus.kdslib;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

/**
 * The database base class
 */
public class KDSDBBase extends SQLiteOpenHelper {

    private final String TAG = "KDSDBBase";

    public interface DBEvents
    {
        void onDBCorrupt();
        void onSDCardUnmount();
        void onDiskFull();
    }


    public static String SQL_SEPARATOR = "\f";

    private final int VERSION_UNKNOWN = -1;

    static public final String DB_FOLDER_NAME = "kdsdata";


    private DBEvents m_dbEventsReceiver = null;
    public void setDBEventsReceiver(DBEvents ev)
    {
        m_dbEventsReceiver = ev;
    }

    public KDSDBBase(Context context, String name, SQLiteDatabase.CursorFactory factory,
                     int version) {
        super(context, name, factory, version);
    }
    @Override
    public void onCreate(SQLiteDatabase db)
    {

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

    public int getVersion()
    {
        return VERSION_UNKNOWN;
    }
    public SQLiteDatabase getDB()
    {

        try {

            SQLiteDatabase db =  this.getWritableDatabase();
            if (db == null)
            {
                if (m_dbEventsReceiver != null)
                    m_dbEventsReceiver.onDBCorrupt();
            }
            return db;
        }
        catch (SQLiteDatabaseCorruptException ex)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , ex);
        }
        catch (Exception e)
        {
            //String s= Environment.getExternalStorageDirectory().toString();
           // boolean b = KDSUtil.sdcardExisted();

            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
        }
        if (m_dbEventsReceiver != null) {
            if (!KDSUtil.sdcardExisted())
            {
                m_dbEventsReceiver.onSDCardUnmount();
            }
            else {
                m_dbEventsReceiver.onDBCorrupt();
            }
        }
        return null;
    }



    public boolean exeBatchSql(SQLiteDatabase db, String[] sqls)
    {
        int ncount = sqls.length;
        String sql = "";
        try {
            for (int i = 0; i < ncount; i++) {
                sql = sqls[i];
                db.execSQL(sql);
            }
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(), e);
            return false;
        }
        return true;
    }


    /**
     * format:
     *      sql;\nsql;\nsql;\n ...\n
     * @param sqlBatch
     * seperator is the \n
     * @return
     */
    public boolean exeSqlBatchString( String sqlBatch)
    {
        ArrayList<String> ar = KDSUtil.spliteString(sqlBatch, SQL_SEPARATOR);
        if (ar.size() <=0)
            return false;
        String sql = "";
        boolean bStartedByMe = startTransaction();
        for (int i=0; i< ar.size(); i++) {
            sql = ar.get(i);
            if (sql.isEmpty()) continue;
            executeDML(sql);
        }
        finishTransaction(bStartedByMe);
        return true;
    }

    /**
     * 2.0.19
     * @param c
     * @param nField
     * @return
     */
    public String getString(Cursor c, int nField)
    {
        if (c.isNull(nField)) return "";
        return c.getString(nField);

    }

    /**
     * 2.0.19
     * @param c
     * @param nField
     * @return
     */
    public int getInt(Cursor c, int nField)
    {
        if (c.isNull(nField)) return 0;

        return c.getInt(nField);
    }

    /**
     * 2.0.19
     * @param c
     * @param nField
     * @param nDefault
     * @return
     */
    public int getInt(Cursor c, int nField, int nDefault)
    {
        if (c.isNull(nField)) return nDefault;

        return c.getInt(nField);
    }

    /**
     * 2.0.19
     * @param c
     * @param nField
     * @return
     */
    public float getFloat(Cursor c, int nField)
    {
        if (c.isNull(nField)) return 0;
        return c.getFloat(nField);
    }

    /**
     * 2.0.19
     * @param c
     * @param nField
     * @param fltDefault
     * @return
     */
    public float getFloat(Cursor c, int nField, float fltDefault)
    {
        if (c.isNull(nField)) return fltDefault;
        return c.getFloat(nField);
    }

    public Date getDate(Cursor sf, int nColIndex)
    {
        String s = getString(sf,nColIndex);
        if (s == null || s.isEmpty())
            return KDSUtil.createInvalidDate();
        return KDSUtil.convertStringToDateSelf(s);

    }
    public boolean executeDML(String sql)
    {
        try {
            if (getDB() != null)
                this.getDB().execSQL(sql);
            else
                return false;
            return true;
        }catch(Exception e)
        {
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + e.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e );
        }
        return false;

    }

    public int executeOneValue(String sql)
    {
        return executeOneValue(getDB(), sql);

//        try {
//            if (getDB() == null) return 0;
//            Cursor c = getDB().rawQuery(sql, null);
//            int nreturn = 0;
//            if (c.moveToNext())
//                nreturn = c.getInt(0);
//            c.close();
//            return nreturn;
//        }catch(Exception e)
//        {
//            KDSLog.e(TAG,KDSLog._FUNCLINE_() + e.toString());
//            KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
//        }
//        return 0;

    }

    public int executeOneValue(SQLiteDatabase db, String sql)
    {
        try {
            if (db == null) return 0;
            Cursor c = db.rawQuery(sql, null);
            int nreturn = 0;
            if (c.moveToNext())
                nreturn = getInt(c,0);
            c.close();
            return nreturn;
        }catch(Exception e)
        {
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + e.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e );
        }
        return 0;

    }


    public int executeOneValue(String sql, int nDefault)
    {
        try {
            if (getDB() == null) return 0;
            Cursor c = getDB().rawQuery(sql, null);
            int nreturn = nDefault;
            if (c.moveToNext())
                nreturn = getInt(c,0);
            c.close();
            return nreturn;
        }catch(Exception e)
        {
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + e.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e );
        }
        return nDefault;

    }

    public float executeOneFloat(String sql)
    {
        try {
            if (getDB() == null)
                return 0;
            Cursor c = getDB().rawQuery(sql, null);
            float nreturn = 0;
            if (c.moveToNext())
                nreturn = getFloat(c,0);
            c.close();
            return nreturn;
        }catch(Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            //Log.e("DB", e.getMessage());
        }
        return 0;

    }

    public boolean dropTable(SQLiteDatabase db , String tableName)
    {
        String sql = "drop table if exists "+tableName;
        db.execSQL(sql);
        return true;
    }

    public boolean dropIndex(SQLiteDatabase db , String indexName)
    {
        String sql = "drop index if exists "+indexName;
        db.execSQL(sql);
        return true;
    }
    /**
     *
     * @return
     *  return value for commit and end transaction function.
     *  true:
     *      This transaction started by me.
     *  false:
     *      it is in transaction state.
     */
    public boolean startTransaction()
    {
        if (getDB() == null) return false;
        if (getDB().inTransaction())
            return false;
        this.getDB().beginTransaction();
        return true;
    }

    /**
     *  call this before endtransaction
     * @param bStartedByMe
     *  Some process share same transaction.
     * @return
     */
    public boolean commitTransaction(boolean bStartedByMe)
    {
        if (!bStartedByMe) return false;
        if (getDB() != null)
            getDB().setTransactionSuccessful();
        else
            return false;
        return true;

    }

    /**
     * call this function after commitTransaction
     * History:
     *  2.0.18
     *      Use old method, just call db.endtransaction.. The original method is wrong. It cause call
     *      setTransactionSuccessful twice.
     * @param bStartedByMe
     * Some process share same transaction.
     * @return
     */
    public boolean endTransaction(boolean bStartedByMe)
    {
//        return finishTransaction(bStartedByMe);

        if (bStartedByMe) {
            if (getDB() != null)
                getDB().endTransaction();
            else
                return false;
            return true;
        }
        else
            return false;
    }

    public boolean finishTransaction( boolean bStartedByMe)
    {
        if (!bStartedByMe) return false;
        if (getDB() == null) return false;
        try {
            getDB().setTransactionSuccessful();
            getDB().endTransaction();
        }
        catch (SQLiteFullException e)
        {
            if (m_dbEventsReceiver != null) {
                m_dbEventsReceiver.onDiskFull();
            }
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_() , e);
        }
        return true;
    }
    /**
     * backup the database
     * Before call this function, the database should been closed
     */
    public void backup()
    {
//        String file = this.getDbFileName();
//        KDSUtil.backupFile(file);


    }
    /**
     * Before call this function, the database should been closed
     */
    public void restore()
    {
//        String file = this.getDbFileName();
//        String bck = file + ".bck";
//        KDSUtil.remove(file);
//        KDSUtil.copyFile(bck, file);
    }


    protected ArrayList<String> outputTableData(KDSDBBase db, String tblName, ArrayList<String> arGuid)
    {
        ArrayList<String> data = new ArrayList<>();

        if (db.getDB() == null) return data;
        for (int i=0; i< arGuid.size(); i++)
        {
            String guid = arGuid.get(i);
            String sql = String.format("select * from %s where guid='%s'", tblName, guid);

            Cursor c =db.getDB().rawQuery(sql, null);

            while (c.moveToNext())
                data.add(makeInsertSql(tblName, c));

        }
        return data;
    }

    protected ArrayList<String> outputTableData(KDSDBBase db, String tblName, String keyField, ArrayList<String> arGuid)
    {
        ArrayList<String> data = new ArrayList<>();
        if (db.getDB() == null) return data;
        for (int i=0; i< arGuid.size(); i++)
        {
            String guid = arGuid.get(i);
            String sql = String.format("select * from %s where %s='%s'", tblName,keyField, guid);
            Cursor c =db.getDB().rawQuery(sql, null);

            while (c.moveToNext())
                data.add(makeInsertSql(tblName, c));

        }
        return data;
    }

    /**
     * @param tblName
     * @param strWhereCondition The where condition
     * @return
     */
    public ArrayList<String> outputTableDataSql(KDSDBBase db, String tblName, String strWhereCondition) {
        ArrayList<String> arSql = new ArrayList<>();
        if (db.getDB() == null) return arSql;

        String sql = "";
        sql = "select * from " + tblName;
        if (!strWhereCondition.isEmpty())
            sql += " " + strWhereCondition;

        Cursor c = db.getDB().rawQuery(sql, null);
        String sqlInsert = "";
        sqlInsert = "insert into " + tblName;
        sqlInsert += "(";
        sqlInsert += makeInsertCols(c);
        sqlInsert += ") values(";
        //boolean bCopied = false;
        while (c.moveToNext()) {
            sql = sqlInsert;
            sql += makeInsertValues(c);
            sql += ");"+SQL_SEPARATOR;
            arSql.add(sql);
            //this.executeDML(sql);
            //bCopied = true;
        }
        return arSql;
    }

    public String makeInsertValues(Cursor c) {
        String s = "";
        int nColCount = c.getColumnCount();
        for (int i = 0; i < nColCount; i++) {
            if (i > 0) s += ",";
            s += "'";
            String strVal = getString(c,i);
            strVal = KDSUtil.fixSqliteSingleQuotationIssue(strVal);
            s += strVal;//

            s += "'";
        }
        return s;
    }
    protected String makeInsertCols(Cursor c) {
        String s = "";
        int nColCount = c.getColumnCount();
        for (int i = 0; i < nColCount; i++) {
            if (i > 0) s += ",";
            s += c.getColumnName(i);

        }
        return s;
    }

    protected String makeInsertSql(String tblName, Cursor c)
    {
        String sqlInsert = "";
        sqlInsert = "insert into " + tblName;
        sqlInsert += "(";
        sqlInsert += makeInsertCols(c);
        sqlInsert += ") values(";

        sqlInsert +=makeInsertValues(c);
        sqlInsert +=")";
        return sqlInsert;
    }
    public ArrayList<String> itemGetAllItemsGUID(KDSDBBase db, String orderGUID) {
        ArrayList<String> ar = new ArrayList<String>();
        String sql = "select guid from items where orderguid='" + orderGUID + "'";

        if (db.getDB() == null) return ar;
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
     * insert into items1 select *,1 from items;
     *  The targettable field equal or more src table.
     *   And, new field MUST at last.
     * @param targetTable
     * @param srcTable
     * @param extendFieldValus
     */
    public void copyData(SQLiteDatabase db, String targetTable, String srcTable, ArrayList<String> extendFieldValus)
    {
        //
        String sql = "";
        if (extendFieldValus == null)
            sql = String.format("insert into %s select * from %s",targetTable, srcTable);
        else
        {
            sql = "insert into " + targetTable;
            sql += " select *";
            for (int i=0; i<extendFieldValus.size(); i++)
            {
                sql += ",'" + extendFieldValus.get(i) +"' ";
            }
            sql += " from "+srcTable;
        }
        db.execSQL(sql);


    }

    /**
     * insert into items1 select *,1 from items;
     *  The targettable field equal or more src table.
     *   And, new field MUST at last.
     * @param targetTable
     * @param srcTable
     *
     */
    public boolean copyData(SQLiteDatabase db, String targetTable, String srcTable)
    {
        //

        if (!tableExisted(db, targetTable)) return false;
        if (!tableExisted(db, srcTable)) return false;

        int nTargetFieldsCount = tableFieldsCount(db, targetTable);
        int nSrcFieldsCount = tableFieldsCount(db, srcTable);

        int nDifferent = nTargetFieldsCount - nSrcFieldsCount;

        String sql = "";
        if (nDifferent == 0)
            sql = String.format("insert into %s select * from %s",targetTable, srcTable);
        else if (nDifferent >0)
        {
            sql = "insert into " + targetTable;
            sql += " select *";
            for (int i=0; i<nDifferent; i++)
            {
                sql += ",''";
            }
            sql += " from "+srcTable;
        }
        else
            return false;

        db.execSQL(sql);
        return true;


    }

    /**
     * ALTER TABLE items1 RENAME TO items2;
     * @param srcTableName
     * @param changeToName
     */
    public void changeTableName(SQLiteDatabase db, String srcTableName, String changeToName)
    {
        if (!tableExisted(db, srcTableName)) return;
        String sql = String.format("alter table %s rename to %s", srcTableName, changeToName);
        db.execSQL(sql);


    }

    /**
     * 2.0.20
     *      use external files dir.
     *
     * @return
     */
    static public String getSDDBFolderWithLastDividChar() {
        String s = KDSUtil.getBaseDirCanUninstall() + "/" + DB_FOLDER_NAME + "/";
        return s;
        //return Environment.getExternalStorageDirectory() + "/" + DB_FOLDER_NAME + "/";
    }

    static public String getAndroidDBPath(String dbName) {
        return KDSApplication.getContext().getDatabasePath(dbName).getAbsolutePath();
    }

    static public String getDBFullPath(String dbName) {
        boolean bUseSelfDefinedFolder = true; //if use android default folder, make this = false
        if (bUseSelfDefinedFolder)
            return getSDDBFolderWithLastDividChar() + dbName;
        else
            return getAndroidDBPath(dbName);
    }
    static public String getDBNameForOpen(String dbName)
    {
        boolean bUseSelfDefinedFolder = true; //if use android default folder, make this = false
        if (bUseSelfDefinedFolder) {
            KDSDBBase.createSdDBFolder();
            return getDBFullPath(dbName);
        }
        else
            return dbName;
    }
    static public boolean createSdDBFolder()
    {
        String folder = getSDDBFolderWithLastDividChar();
        return KDSUtil.createFolder(folder);
    }

    public boolean isEmpty()
    {
        return false;
    }

    public boolean tableExisted(SQLiteDatabase db, String tblName)
    {
        String sql = "select count(*) from sqlite_master where type='table' and upper(name)='";
        sql += tblName.toUpperCase();
        sql += "'";
        int n = executeOneValue(db, sql);

        return (n>0);
    }

    public int tableFieldsCount(SQLiteDatabase db, String tblName)
    {
        String sql = String.format( "pragma table_info([%s])", tblName);
        Cursor c = db.rawQuery(sql, null);
        int ncount = 0;
        while (c.moveToNext())
            ncount ++;
        c.close();
        return ncount;
    }

}
