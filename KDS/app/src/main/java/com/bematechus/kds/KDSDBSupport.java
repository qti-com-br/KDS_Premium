package com.bematechus.kds;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.bematechus.kdslib.KDSDBBase;

/**
 * Created by Administrator on 2015/7/29 0029.
 */
public class KDSDBSupport extends KDSDBCurrent {


    static public final String DB_NAME = "support.db";
    public KDSDBSupport(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
        super(context, name, factory, version);
    }

    public int getVersion()
    {
        return DB_VERSION;
    }
    static public KDSDBSupport open(Context context)
    {


        String dbName =  KDSDBBase.getDBNameForOpen(KDSDBSupport.DB_NAME); //use sd card path
        //

        KDSDBSupport d = new KDSDBSupport(context,dbName, null, KDSDBSupport.DB_VERSION);
        return d;

    }

}
