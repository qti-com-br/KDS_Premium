package com.bematechus.kdsstatistic;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSStationsRelation;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXML;
import com.bematechus.kdslib.SettingsBase;

/**
 * Created by Administrator on 2015/8/24 0024.
 */
public class STSettings extends SettingsBase {

    static final public int TCP_DEFAULT_REPORT_VIEWER_PORT = 6000;//please make it same as the default values in strings xml file.
    static final public int TCP_STATION_PORT_FOR_STATISTIC = 6001; //use it to connect to normal station.

    static final public int UDP_STATISTIC_RECEIVE_ALIVE_ANNOUNCER_PORT = 5002;
    static final public int UDP_STATION_RECEIVE_ALIVE_ANNOUNCER_PORT = 5000;


    public enum ID
    {
        NULL,
        KDSStatistic_Data_Viewer_IPPort,

        Enable_Auto_Report,
        Auto_Report_Type,
        Auto_Report_Time,//when create the auto report
        Keep_data_days,//unused
        Auto_Report_Timeslot_from,
        Auto_Report_Timeslot_to,
        Auto_Report_Station_From,
        Auto_Report_Station_To,

        Language,
        //for log
        Log_mode,
        Log_days,
        //
        Auto_report_save_to,
        Remote_folder,

        Bumpbar_Cancel,
        Bumpbar_OK,

    }
    public enum ReportSaveTo
    {
        Local,
        Remote,
    }

    //all data saved in this buffer
    private HashMap<ID, Object> m_mapSettings = new HashMap<ID, Object>();

    //from ID to pref data
    private HashMap<ID, String> m_mapPrefID = new HashMap<ID,String>();
    /********************************************************************************************/
    public STSettings(Context context)
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
        m_mapPrefID.put(ID.KDSStatistic_Data_Viewer_IPPort,"string_general_viewer_ipport");
        m_mapPrefID.put(ID.Enable_Auto_Report,"bool_general_auto_report");
        m_mapPrefID.put(ID.Auto_Report_Type,"string_general_auto_report_type");
        m_mapPrefID.put(ID.Keep_data_days,"int_database_keep"); //unused
        m_mapPrefID.put(ID.Auto_Report_Time,"string_general_auto_time");//when create the auto report
        m_mapPrefID.put(ID.Auto_Report_Timeslot_from,"string_general_auto_timeslot_from");
        m_mapPrefID.put(ID.Auto_Report_Timeslot_to,"string_general_auto_timeslot_to");
        m_mapPrefID.put(ID.Auto_Report_Station_From,"string_general_auto_station_from");
        m_mapPrefID.put(ID.Auto_Report_Station_To,"string_general_auto_station_to");

        m_mapPrefID.put(ID.Language,"string_kds_general_language");
        init_option(ID.Log_mode,"string_log_mode",KDSUtil.convertIntToString( KDSLog.LogLevel.Basic.ordinal()));
        init_option(ID.Log_days,"string_log_days", "3");

        init_option(ID.Auto_report_save_to,"string_general_auto_report_save_to", "0");
        init_option(ID.Remote_folder,"string_general_report_remote_folder", "");


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

    /**
     *
     */
    private void setDefaultValues(Context context)
    {
        m_contextTmp = context;
        //general settings

        set(ID.KDSStatistic_Data_Viewer_IPPort, KDSUtil.convertIntToString(TCP_DEFAULT_REPORT_VIEWER_PORT));
        set(ID.Enable_Auto_Report,false);
        set(ID.Auto_Report_Type,"0");
        set(ID.Keep_data_days,180);
        set(ID.Auto_Report_Time,"00:00");
        set(ID.Auto_Report_Timeslot_from,"00:00");
        set(ID.Auto_Report_Timeslot_to,"23:59");
        set(ID.Auto_Report_Station_From,"0");
        set(ID.Auto_Report_Station_To,"5");
        set(ID.Language, "0");
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
//            if (objDef instanceof Integer)
//                ndef = (int)objDef;
//            try {
//                return pref.getInt(tag, ndef);
//            }
//            catch (Exception e)
//            {
//                String strInt = pref.getString(tag, "");
//                return KDSUtil.convertStringToInt(strInt, ndef);
//            }
//        }
//        else if (s.equals("string"))
//        {
//            String strdef = "";
//
//            if (objDef instanceof String)
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
//
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
//
//        editor.commit();
//        editor.apply();
//
//
//    }

    public void loadSettings(Context appContext)
    {
        Context c =appContext;// app.getApplicationContext();

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
        else if (obj instanceof String)
        {
            String s = (String)obj;
            if (s.isEmpty())
                return 0;
            return Integer.parseInt((String)obj);
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

//    static public ArrayList<KDSStationsRelation> parseStationsRelations(String strRelations)
//    {
//        ArrayList<KDSStationsRelation> arRelations = new ArrayList<KDSStationsRelation>();
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
//        int ncount = ar.size();
//
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
//        else if (obj instanceof Boolean)
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
//        else if (obj instanceof Boolean)
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
        String strRelations = loadStationsRelationString(appContex);
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
//
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
                //if (id == ID.KDSRouter_ID) continue;
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
//
//    static Language loadLanguageOption(Context c)
//    {
//        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
//        String str = pre.getString("kds_general_language", "0");
//        int n = KDSUtil.convertStringToInt(str, 0);
//        return Language.values()[n];
//
//    }


}
