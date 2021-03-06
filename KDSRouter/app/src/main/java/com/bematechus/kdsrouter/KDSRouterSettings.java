package com.bematechus.kdsrouter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSStationsRelation;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXML;
import com.bematechus.kdslib.SettingsBase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2015/8/24 0024.
 */
public class KDSRouterSettings extends SettingsBase {
    static public String TAG = "KDSRouterSettings";
    static final public int DEFAULT_STATIONS_DATASOURCE_TCPIP_PORT = KDSApplication.getContext().getResources().getInteger(R.integer.default_stations_datasource_tcpip_port);// 3000;//please make it same as the default values in strings xml file.
    //static final public int DEFAULT_STATIONS_INTERNAL_TCPIP_PORT =KDSApplication.getContext().getResources().getInteger(R.integer.default_stations_internal_tcpip_port);// 3001;

    static final public int UDP_STATIONS_ANNOUNCER_UDP_PORT = KDSApplication.getContext().getResources().getInteger(R.integer.default_stations_announce_udp_port);// 5000; //for normal stations
    static final public int UDP_ROUTER_ANNOUNCER_UDP_PORT = KDSApplication.getContext().getResources().getInteger(R.integer.default_router_announce_udp_port);// 5001; //write data to router. Use two port is for KDS and KDSRouter running in same station.


    static final public int DEFAULT_ROUTER_DATASOURCE_TCPIP_PORT = KDSApplication.getContext().getResources().getInteger(R.integer.default_router_datasource_tcpip_port);//4000;
    static final public int DEFAULT_ROUTER_BACKUP_TCP_PORT =KDSApplication.getContext().getResources().getInteger(R.integer.default_router_backup_tcpip_port);// 4001;



    public enum ID
    {
        NULL,
        KDSRouter_ID,

        KDSRouter_Enabled,
        KDSRouter_Data_Source,
        KDSRouter_Data_POS_IPPort,
        KDSRouter_Data_Folder,
        KDSRouter_Backup_IPPort,
        KDSRouter_Connect_Station_IPPort,
        KDSRouter_Default_Station,
        KDSRouter_Primary_Router,
        KDSRouter_Slave_Router,
        Bumpbar_OK,
        Bumpbar_Cancel,
        Settings_password_enabled,
        Settings_password,
        KDSRouter_Backup,
        Language,
        Modifier_auto_add,
        Item_auto_add,
        //for log
        Log_mode,
        Log_days,
        Log_orders,

        Order_ack, //2.0.14
        notification_minutes,
        Enable_smbv2, //2.0.20
        Enable_3rd_party_order,
    }


//    public enum StationFunc
//    {
//        Normal,
//        Expeditor,
//        Queue,// "Order Queue Display",
//        Mirror,
//        Backup,
//        Workload,
//        Duplicate,
//        TableTracker,
//        Queue_Expo, //sos(Queue) work with expo
//        MAX_COUNT,
//    }
//
//    public enum SlaveFunc
//    {
//
//        Unknown,
//        Backup,
//        Mirror,
//        Automatic_work_loan_distribution,
//        Duplicate_station,
//        Order_Queue_Display,
//    }

//    public enum StationStatus
//    {
//        Enabled,
//        Disabled
//    }
//    public enum KDSDataSource
//    {
//        TCPIP,
//        Folder,
//
//    }
//    public enum Language
//    {
//        English,
//        Chinese,
//    }


    //all data saved in this buffer
    protected HashMap<ID, Object> m_mapSettings = new HashMap<ID, Object>();

    //from ID to pref data
    private HashMap<ID, String> m_mapPrefID = new HashMap<ID,String>();
    /********************************************************************************************/
    public KDSRouterSettings(Context context)
    {
        setDefaultValues(context);
        init_pref_map();
    }

    /**
     * the map for setting id to pref saved string
     * data type + preference key
     */
    private void init_pref_map()
    {

        //general settings
        m_mapPrefID.put(ID.KDSRouter_ID,"string_general_station_id");
        m_mapPrefID.put(ID.KDSRouter_Backup,"bool_general_router_backup");

        m_mapPrefID.put(ID.KDSRouter_Enabled,"bool_general_router_enabled");
        m_mapPrefID.put(ID.KDSRouter_Data_Source,"string_general_data_source");
        m_mapPrefID.put(ID.KDSRouter_Data_POS_IPPort,"string_general_pos_ipport");
        m_mapPrefID.put(ID.KDSRouter_Data_Folder,"string_general_remote_folder");
        m_mapPrefID.put(ID.KDSRouter_Backup_IPPort,"string_general_backup_ipport");
        m_mapPrefID.put(ID.KDSRouter_Connect_Station_IPPort,"string_general_connect_station_ipport");
        m_mapPrefID.put(ID.KDSRouter_Default_Station,"string_general_default_tostation");
        m_mapPrefID.put(ID.KDSRouter_Primary_Router,"string_general_router_primary");
        m_mapPrefID.put(ID.KDSRouter_Slave_Router,"string_general_router_slave");
        m_mapPrefID.put(ID.Settings_password, "string_kds_general_password");
        m_mapPrefID.put(ID.Settings_password_enabled, "bool_kds_general_enable_password");
        m_mapPrefID.put(ID.Language,"string_kds_general_language");

        m_mapPrefID.put(ID.Modifier_auto_add,"bool_modifier_auto_add");
        m_mapPrefID.put(ID.Modifier_auto_add,"bool_item_auto_add");

        m_mapPrefID.put(ID.Bumpbar_OK,"");
        m_mapPrefID.put(ID.Bumpbar_Cancel,"");

        init_option(ID.Log_mode,"string_log_mode",KDSUtil.convertIntToString( KDSLog.LogLevel.Basic.ordinal()));
        init_option(ID.Log_days,"string_log_days", "3");
        init_option(ID.Log_orders,"bool_log_orders",false);
        init_option(ID.Order_ack,"bool_notification_order_acknowledgement",false);
        init_option(ID.notification_minutes,"string_notification_minutes","10");

        init_option(ID.Enable_smbv2,"bool_general_enable_smbv2",true);
        init_option(ID.Enable_3rd_party_order,"bool_general_enable_3rd_order",true);

    }

    /**
     *
     * @param id
     * @param tag
     *      The pref key, and data type.
     * @param defaultVal
     */
    public void init_option(ID id, String tag, Object defaultVal)
    {
        m_mapPrefID.put(id, tag);
        set(id, defaultVal);

    }
    Context m_contextTmp = null;
    int getColor(  int nID)
    {
        return m_contextTmp.getResources().getColor(nID);
    }
    /**
     *
     */
    private void setDefaultValues(Context context)
    {
        m_contextTmp = context;
        //general settings
        set(ID.KDSRouter_ID,"");
        set(ID.KDSRouter_Backup,false);
        set(ID.KDSRouter_Enabled, false);
        set(ID.KDSRouter_Default_Station, "1");
        set(ID.KDSRouter_Data_Source, KDSDataSource.TCPIP.ordinal());
        set(ID.KDSRouter_Data_POS_IPPort, KDSUtil.convertIntToString(DEFAULT_ROUTER_DATASOURCE_TCPIP_PORT));// "4000");
        set(ID.KDSRouter_Data_Folder, "");
        set(ID.KDSRouter_Backup_IPPort,  KDSUtil.convertIntToString(DEFAULT_ROUTER_BACKUP_TCP_PORT));//"4001"); //for router backup,
        set(ID.KDSRouter_Connect_Station_IPPort, KDSUtil.convertIntToString(DEFAULT_STATIONS_DATASOURCE_TCPIP_PORT));// "3000");
        set(ID.KDSRouter_Default_Station,"kdsrouter");
        set(ID.KDSRouter_Primary_Router, "");
        set(ID.KDSRouter_Slave_Router, "");
        set(ID.Bumpbar_OK, "8"); //KeyEvent
        set(ID.Bumpbar_Cancel, "7");
        set(ID.Settings_password_enabled, false);
        set(ID.Settings_password, "");

        set(ID.Language,"0");
        set(ID.Modifier_auto_add,true);
        set(ID.Item_auto_add, true);

    }

//    private Object getPrefValue(SharedPreferences pref, String strTag, Object objDef)
//    {
//        if (strTag.isEmpty())
//            return null;
//        String s = strTag;
//        int n = s.indexOf("_", 0);
//        s = s.substring(0, n);
//        String tag = strTag.substring(n + 1);
//
//        if (s.equals("int"))
//        {
//            int ndef = 0;
//            if (objDef instanceof  Integer)
//                ndef = (int)objDef;
//            try {
//                return pref.getInt(tag, ndef);
//            }
//            catch (Exception e)
//            {
//                Log.e(TAG,KDSLog._FUNCLINE_() + e.toString());
//                Log.e(TAG, KDSUtil.error( e));
//                String strInt = pref.getString(tag, "");
//                return KDSUtil.convertStringToInt(strInt, ndef);
//            }
//        }
//        else if (s.equals("string"))
//        {
//            String strdef = "";
//
//            if (objDef instanceof  String)
//                strdef = (String)objDef;
//            else if (objDef instanceof Integer)
//                strdef = objDef.toString();
//            return pref.getString(tag, strdef);
//        }
//        else if (s.equals("bool"))
//        {
//            boolean bdef = false;
//            if (objDef instanceof Boolean)
//                bdef = (boolean)objDef;
//
//            return pref.getBoolean(tag, bdef);
//        }
//        return null;
//
//    }

//    /**
//     * save to preferences
//     * @param pref
//     * @param strTag
//     * @param objVal
//     * @return
//     */
//    private void setPrefValue(SharedPreferences pref, String strTag, Object objVal)
//    {
//        if (strTag.isEmpty())
//            return ;
//        String s = strTag;
//        int n = s.indexOf("_", 0);
//        s = s.substring(0, n);
//        String tag = strTag.substring(n + 1);
//        SharedPreferences.Editor editor = pref.edit();
//        if (s.equals("int"))
//        {
//            editor.putInt(tag, (int) objVal);
//        }
//        else if (s.equals("string"))
//        {
//            if (objVal instanceof String)
//                editor.putString(tag, (String) objVal);
//            else
//                editor.putString(tag, objVal.toString());
//        }
//        else if (s.equals("bool"))
//        {
//            editor.putBoolean(tag, (boolean)objVal);
//
//        }
//
//        editor.commit();
//        editor.apply();
//
//
//    }
//


    public void loadSettings(Context appContext)
    {
        Context c =appContext;//

        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
        for (Map.Entry<ID, String> entry : m_mapPrefID.entrySet()) {

            ID id = entry.getKey();
            String tag = entry.getValue();
            if (tag.isEmpty()) continue;
            Object objdef = this.get(id);
            Object obj = getPrefValue(pre, tag, objdef);
            if (obj != null)
                this.set(id, obj);

        }

    }


    public void set(ID config, Object obj)
    {
        m_mapSettings.put(config, obj);
    }
    public Object get(ID config)
    {
         return m_mapSettings.get(config);
    }
    public boolean getBoolean(ID config)
    {
        return (boolean) get(config);
    }

    public int getInt(ID config)
    {
        Object obj = get(config);
        if (obj instanceof Integer)
            return (int)obj;
        else if (obj instanceof  String)
        {
            String s = (String)obj;
            if (s.isEmpty())
                return 0;
            return Integer.parseInt((String )obj);
        }
        return 0;
    }

    public String getString(ID config)
    {

        return (String) get(config);
    }


//    static ArrayList<KDSStationsRelation> loadStationsRelation(Context context )
//    {
//
//        ArrayList<KDSStationsRelation> ar = new ArrayList<>();
//
//
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
//        HashSet<String> setStations = null;
//        Set<String> set =  pref.getStringSet("StationsRelation", null);
//        if (set == null)
//            return ar;
//        setStations = new HashSet<>( set);
//
//        Iterator it =setStations.iterator();
//
//        while(it .hasNext())
//        {
//            String s= (String) it.next();
//            KDSStationsRelation station =  KDSStationsRelation.parseString(s);
//            if (station != null)
//                ar.add(station);
//        }
//        return ar;
//
//    }

//    /**
//     *
//     * @param context
//     * @return
//     */
//    public static String loadStationsRelationString(Context context )
//    {
//
//        ArrayList<KDSStationsRelation> ar = loadStationsRelation(context);
//        int ncount = ar.size();
//        String str = "";
//        for (int i=0; i< ncount; i++)
//        {
//            str += ar.get(i).toString();
//            if (i < ncount-1)
//                str += ":";
//        }
//
//        return str;
//
//    }
//
//    static public ArrayList<KDSStationsRelation> parseStationsRelations(String strRelations)
//    {
//        ArrayList<KDSStationsRelation> arRelations = new  ArrayList<KDSStationsRelation>();
//        ArrayList<String> ar = KDSUtil.spliteString(strRelations, ":");
//        int ncount = ar.size();
//        for (int i=0; i< ncount; i++)
//        {
//            String s = ar.get(i);
//            if (s.isEmpty()) continue;
//            KDSStationsRelation relation = KDSStationsRelation.parseString(s);
//            arRelations.add(relation);
//
//        }
//        return arRelations;
//    }

//    /**
//     *
//     * @param context
//     * @param strSettings
//     */
//    static public void saveStationsRelation(Context context, String strSettings)
//    {
//        ArrayList<KDSStationsRelation> arRelations = parseStationsRelations(strSettings);
//        saveStationsRelation(context, arRelations);
//
//    }
//
//    static public void saveStationsRelation(Context context, ArrayList<KDSStationsRelation> ar)
//    {
//
//
//        int ncount = ar.size();
//       // if (ncount <=0) return;
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
//        HashSet<String> setStations = new HashSet<>();
//
//        for (int i=0; i< ncount; i++)
//        {
//            setStations.add(ar.get(i).toString());
//        }
//
//        SharedPreferences.Editor editor =  pref.edit();
//        editor.putStringSet("StationsRelation", setStations);
//        editor.commit();
//
//    }

//    private String getConfigValTypeStrng(Object obj)
//    {
//        if (obj instanceof Integer)
//        {
//            return "int";
//        }
//        else if (obj instanceof String)
//        {
//            return "str";
//        }
//        else if (obj instanceof  Boolean)
//        {
//            return "bool";
//        }
//        else
//            return "";
//
//
//    }
//    private String convertConfigValToString(Object obj)
//    {
//        if (obj instanceof Integer)
//        {
//            return KDSUtil.convertIntToString((int)obj);
//
//        }
//        else if (obj instanceof String)
//        {
//            return (String)obj;
//        }
//        else if (obj instanceof  Boolean)
//        {
//            boolean b = (boolean)obj;
//            if (b) return "1";
//            return "0";
//        }
//        else
//            return obj.toString();
//    }

    /**
     * Output all settings to a xml text string. For sending to others station.
     * Format:
     *  <Settings>
     *      <config id=## >val</>
     *      <config id=## >val</>
     *  <Settings/>
     * @return
     */
    public String outputXmlText(Context appContex)
    {
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root("Settings");

        for (Map.Entry<ID, Object> entry : m_mapSettings.entrySet()) {

            ID id = entry.getKey();
            if (id == ID.Bumpbar_Cancel ||
                    id == ID.Bumpbar_OK) continue;

            Object obj = entry.getValue();
            String strid = KDSUtil.convertIntToString(id.ordinal());
            String strVal = convertConfigValToString(obj);
            String strType = getConfigValTypeStrng(obj);
            xml.newGroup("config", true);
            xml.newAttribute("id", strid);
            xml.newAttribute("ty", strType);
            xml.setGroupValue(strVal);
            xml.back_to_parent();

        }

        xml.newGroup("StationsRelation", true);
        String strRelations = loadStationsRelationString(appContex, true);
        xml.setGroupValue(strRelations);
        xml.back_to_parent();
        return xml.get_xml_string();


    }
//
//    private Object convertConfigStringToType(String strVal, String toType)
//    {
//        if (toType.equals("int"))
//        {
//            if (strVal.isEmpty())
//                 return 0;
//            return Integer.parseInt(strVal);
//        }
//        else if (toType.equals("str"))
//        {
//            return strVal;
//        }
//        else if (toType.equals("bool"))
//        {
//            if (strVal.equals("0"))
//                return false;
//            else if (strVal.equals("1"))
//                return true;
//            else
//                return false;
//
//        }
//        else
//        {
//            return strVal;
//        }
//    }

    public void parseXmlText(Context appContext, String strXml)
    {
        KDSXML xml = new KDSXML();
        xml.loadString(strXml);

        xml.back_to_root();

        if (xml.getFirstGroup("config"))
        {
            do {
                String strid = xml.getAttribute("id", "");
                String strval = xml.getCurrentGroupValue();
                String strType = xml.getAttribute("ty", "");


                int n = KDSUtil.convertStringToInt(strid, -1);
                if (n <0) continue;
                ID id = ID.values()[n];
                if (id == ID.KDSRouter_ID) continue;
                if (id == ID.KDSRouter_Backup) continue;
                if (id == ID.KDSRouter_Primary_Router) continue;
                if (id == ID.KDSRouter_Slave_Router) continue;
                Object objVal = convertConfigStringToType(strval, strType);
                this.set(id, objVal);

            }while(xml.getNextGroup("config"));
        }
        xml.back_to_root();
        if (!xml.getFirstGroup("StationsRelation"))
            return;
        String strRelations = xml.getCurrentGroupValue();
        saveStationsRelation(appContext, strRelations);



    }

    /**
     * save all data to local preference settings
     */
    public void save(Context appContext)
    {
        Context c = appContext;//app.getApplicationContext();

        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
        for (Map.Entry<ID, String> entry : m_mapPrefID.entrySet()) {

            ID id = entry.getKey();
            String tag = entry.getValue();
            Object objVal = this.get(id);
            setPrefValue(pre, tag, objVal);

        }

    }

    /**
     * KW-155
     *
     */
    public void setToDefault()
    {
        init_pref_map();
        setDefaultValues(KDSApplication.getContext());

        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pre.edit();
        editor.clear();
        editor.commit();


    }
//    static Language loadLanguageOption(Context c)
//    {
//        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
//        String str = pre.getString("kds_general_language", "0");
//        int n = KDSUtil.convertStringToInt(str, 0);
//        return Language.values()[n];
//
//    }
//
//    static String getLanguageString(Language lan)
//    {
//        switch (lan)
//        {
//
//            case English:
//                return KDSApplication.getContext().getString(R.string.english);// "English";
//
//            case Chinese:
//                return KDSApplication.getContext().getString(R.string.chinese);//"Chinese";
//
//            default:
//                return KDSApplication.getContext().getString(R.string.english);//"English";
//        }
//    }

    /**
     * check if the settings are default values
     * @return
     */
    public boolean isDefaultSettings()
    {
        if (!loadStationsRelationString(KDSApplication.getContext(), true).isEmpty())
            return false;
        KDSRouterSettings settings = new KDSRouterSettings(KDSApplication.getContext()); //default one.
        for (Map.Entry<ID, Object> entry : settings.m_mapSettings.entrySet())
        {
            ID id = entry.getKey();
            Object obj = entry.getValue();
            String defaultVal = convertConfigValToString(obj);
            String myVal = convertConfigValToString(this.get(id));
            if (!defaultVal.equals(myVal))
                return false;

        }
        return true;
    }

    public static boolean loadEnable3rdOrder(Context c)
    {
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
        boolean bEnable = pre.getBoolean("general_enable_3rd_order", true);

        return bEnable;

    }
    static public String MIN_FCM_TIME = "min_fcm_time";
    public static void saveFCMTime(Context c, long tm)
    {
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = pre.edit();
        editor.putLong(MIN_FCM_TIME, tm);
        editor.commit();
    }

    static public long loadFCMTime(Context c)
    {
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
        long l = pre.getLong(MIN_FCM_TIME, 0);

        return l;
    }
}
