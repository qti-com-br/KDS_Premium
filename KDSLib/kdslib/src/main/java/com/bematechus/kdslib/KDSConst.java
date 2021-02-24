
package com.bematechus.kdslib;

import android.graphics.Color;

/**
 *
 * @author David.Wong
 */
public class KDSConst {

    static public final String DEFAULT_PASSWORD = "123";

    static public final String SMB_FOLDER_ORDER_INFO = "OrderInfo";
    static public final String SMB_FOLDER_NOTIFICATION = "Notification";
    static public final String SMB_FOLDER_ACKNOWLEDGEMENT = "Ack"; //2.0.39

    static public final int SHOW_UNBUMP_DLG = 1;
    static public final int SHOW_UNPARK_DLG = 2;
    static public final int SHOW_PREFERENCES = 3;
    static public final int SHOW_MEDIA_PLAYER = 4;
    static public final int SHOW_UTILITY = 5;
    static public final int SHOW_LOGIN = 6;

    //timeout values

    public static final int KB = 1024;

    public static final int CONNECTING_TIMEOUT = 10000; //10 secs
    public static final int ACTIVE_PLUS_TIMEOUT = 20000;// 10000; //10 secs //in old router, this value is 5000
    public static final int ACTIVE_PLUS_FREQUENCE = 2000;//1000; //2 secs
    public static final int PING_THREAD_SLEEP = 1000; //1 secs

    public static final int MAX_INFORMATION_COUNT = 20; //in old router, this value is 15
//    public static final int STATION_UNKNOWN = 0;
//    public static final int STATION_PRIMARY = 1;
//    public static final int STATION_SLAVE = 2;
 
    public static final String KDS_Transaction_Type_New = "1";
    public static final String KDS_Transaction_Type_Del = "2";
    public static final String KDS_Transaction_Type_Modify = "3";
    public static final String KDS_Transaction_Type_Tranfer = "4";
    public static final String KDS_Transaction_Type_Ask_Status = "5";
    
    public static final String KDS_Str_Transaction = "Transaction";
    public static final String KDS_Str_Color = "Color";
    public static final String KDS_Str_RGBColor = "RGBColor";
    public static final String KDS_Str_IP = "IP";
    public static final String KDS_Str_Station = "Station";
    public static final String KDS_Str_MAC = "MAC";
    public static final String KDS_Str_PSize = "PSize";
    public static final String KDS_Str_Param = "Param";

    public static final String RESET_ORDERS_LAYOUT = "RESET_LAYOUT";

    public static final int INT_TRUE = 1;
    public static final int INT_FALSE = 0;

    public static final long DIALOG_AUTO_CLOSE_TIMEOUT = 5000; //1 mins




    public enum Screen
    {
        SCREEN_A,
        SCREEN_B
    }



    public enum OrderSortBy
    {
        Unknown,
        Waiting_Time,
        Order_Number,
        Items_Count,
        Preparation_Time,
    }
    public enum SortSequence
    {
        Ascend,
        Descend,
    }

    /**
     * for feature enable/disable
     * note:
     *  Latest Activation version: 2.1.12
     *  Latest normal version: 2.0.52
     */
    //2.0.38
    public static final boolean ENABLE_FEATURE_ACTIVATION = true;//for multiple version.
    public static final boolean ENABLE_FEATURE_ORDER_ACKNOWLEDGEMENT = true;//

    //public static final boolean _DEBUG = false;
    //public static final boolean _DEBUG_HIDE_LOGIN_DLG = false;
    public static final boolean _DEBUG = false;
    public static final boolean _DEBUG_HIDE_LOGIN_DLG = false;

    static public final  int DIM_BG = Color.GRAY;
    static public final int DIM_FG = Color.DKGRAY;
    static public final int IMAGE_GAP =1;

    //statistic is useless in 2.3 version.
    //it just is for average preparing time.
    public static final boolean ENABLE_FEATURE_STATISTIC = false;

    static public String APP_NAME_KDS = "kds";
    static public String APP_NAME_ROUTER = "kdsrouter";

    static public int MIN_STATION_ID = 1;
    static public int MAX_STATION_ID = 99;

    //kpp1-409
    static public String ORDER_GUID_FOR_API_ITEM_CHANGES = "9e09b816-2502-42bd-bfee-a69a1cce1cd6";

    static public String LINE_ITEMS_DEFAULT_COLS = "20,10,30,30,10";
    static public String LINE_ITEMS_OLD_COLS = "25,25,25,25";

    //write to premium station, this will let premium know router app existed.
    static public String APP_ID_START = "<AppSockID>";
    static public String APP_ID_END = "</AppSockID>";
    static public String ROUTER_SOCKET_ID = "e91d9563-efc3-4b7e-9c5e-01c2b9b19374";
}
