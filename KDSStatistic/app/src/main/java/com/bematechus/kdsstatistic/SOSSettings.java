package com.bematechus.kdsstatistic;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;
import com.bematechus.kdslib.KDSXML;
import com.bematechus.kdslib.SettingsBase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/8/24 0024.
 */
public class SOSSettings extends SettingsBase {

    static final public int TCP_DEFAULT_REPORT_VIEWER_PORT = 6000;//please make it same as the default values in strings xml file.
    static final public int TCP_STATION_PORT_FOR_STATISTIC = 6001; //use it to connect to normal station.

    static final public int UDP_STATISTIC_RECEIVE_ALIVE_ANNOUNCER_PORT = 5002;
    static final public int UDP_STATION_RECEIVE_ALIVE_ANNOUNCER_PORT = 5000;
    //static final public int UDP_ROUTER_ANNOUNCER_PORT = 5001; //write data to router. Use two port is for KDS and KDSRouter running in same station.
    static final public String OVERALL_STATION_ID = "-1";
    static public final String SOS_VIEW_LAYOUT_KEY = "view_layout";

    public enum ID
    {
        NULL,
        Data_Viewer_IPPort,
        //Auto_Report_Interval,
        Real_time_period,
        //Graph_interval,
        Graph_duration,
        SOS_Stations,
        Language,

//        Font_station_id,
//        Font_realtime_value,
//        Font_graph,

        Bumpbar_Cancel,
        Bumpbar_OK,

        //Real_time_alert_font,
        Graph_x_title,
        Graph_y_title,
        //Graph_y_color,

        View_layout,
        //for log
        Log_mode,
        Log_days,
        Customized_title,
        Hide_data_dot,
        Zero_value_show,

    }

    public enum ZeroValueShow
    {
        Zero,
        Last_data,
    }

    //all data saved in this buffer
    private HashMap<ID, Object> m_mapSettings = new HashMap<ID, Object>();

    //from ID to pref data
    private HashMap<ID, String> m_mapPrefID = new HashMap<ID,String>();
    /********************************************************************************************/
    public SOSSettings(Context context)
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
        m_mapPrefID.put(ID.Data_Viewer_IPPort,"string_general_viewer_ipport");
       // m_mapPrefID.put(ID.KDSStatistic_Connection_Station_IPPort, "string_general_station_ipport");

       // m_mapPrefID.put(ID.Auto_Report_Interval,"int_auto_report_interval");
        m_mapPrefID.put(ID.Real_time_period,"string_real_time_period");
        //m_mapPrefID.put(ID.Graph_interval,"int_graph_interval");
        m_mapPrefID.put(ID.Graph_duration,"string_graph_duration");

        m_mapPrefID.put(ID.SOS_Stations,"string_sos_stations");
        m_mapPrefID.put(ID.Language,"string_kds_general_language");

//        m_mapPrefID.put(ID.Font_station_id,"fontface_station_id_font");
//        m_mapPrefID.put(ID.Font_realtime_value,"fontface_real_time_font");
//        m_mapPrefID.put(ID.Font_graph,"fontface_graph_font");
//        m_mapPrefID.put(ID.Real_time_alert_font,"fontface_real_time_alert_font");

        m_mapPrefID.put(ID.Graph_x_title,"string_graph_axis_x_title");
        m_mapPrefID.put(ID.Graph_y_title,"string_graph_axis_y_title");

        //m_mapPrefID.put(ID.Graph_y_color,"int_graph_y_color");
        m_mapPrefID.put(ID.View_layout,"string_"+SOS_VIEW_LAYOUT_KEY);

        init_option(ID.Log_mode,"string_log_mode",KDSUtil.convertIntToString( KDSLog.LogLevel.Basic.ordinal()));
        init_option(ID.Log_days,"string_log_days", "3");
        init_option(ID.Customized_title,"string_kds_general_title", "");
        init_option(ID.Hide_data_dot,"bool_graph_hide_data_dot",false);

        init_option(ID.Zero_value_show,"string_last_data_replace_zero","0");



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

        set(ID.Data_Viewer_IPPort, KDSUtil.convertIntToString(TCP_DEFAULT_REPORT_VIEWER_PORT));
       // set(ID.KDSStatistic_Connection_Station_IPPort, KDSUtil.convertIntToString(TCP_STATION_PORT_FOR_STATISTIC));
        //set(ID.Auto_Report_Interval,5);
        set(ID.Real_time_period,60);
        //set(ID.Graph_interval,30); //30 secs
        set(ID.Graph_duration,3600); //60 mins
        set(ID.Language, "0");

//        set(ID.Font_station_id,new KDSViewFontFace(Color.BLACK,Color.WHITE,  KDSViewFontFace.DEFULT_FONT_FILE, 30));
//        set(ID.Font_realtime_value,new KDSViewFontFace( Color.BLACK,Color.WHITE, KDSViewFontFace.DEFULT_FONT_FILE, 40));
//        set(ID.Font_graph,new KDSViewFontFace(Color.BLACK,Color.WHITE,  KDSViewFontFace.DEFULT_FONT_FILE, 12));
//
//        set(ID.Real_time_alert_font,new KDSViewFontFace( Color.BLACK,Color.RED, KDSViewFontFace.DEFULT_FONT_FILE, 40));

        set(ID.Graph_x_title,KDSApplication.getContext().getString(R.string.graph_axis_x_title));
        set(ID.Graph_y_title,KDSApplication.getContext().getString(R.string.graph_axis_y_title));

       // set(ID.Graph_y_color,KDSApplication.getContext().getResources().getColor(R.color.axis_y_color));
        set(ID.View_layout,"");

    }




    public void loadSettings(Context appContext)
    {
        Context c =appContext;// app.getApplicationContext();
//
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
//
//        // SharedPreferences pre =  this.getSharedPreferences("P1", Activity.MODE_PRIVATE);
//        String str = pre.getString("kds_general_tcpport", "3000");
        for (Map.Entry<ID, String> entry : m_mapPrefID.entrySet()) {

            ID id = entry.getKey();
            String tag = entry.getValue();
            if (tag.isEmpty()) continue;
            Object objdef = this.get(id);
            Object obj = getPrefValue(pre, tag, objdef);
            if (obj != null)
                this.set(id, obj);
            //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }

//        //caption lines
//        boolean bEnableCaption2 = pre.getBoolean("caption_enable_caption2", false);
//        if (bEnableCaption2)
//            set(ID.Order_Title_Rows, 2);
//        else
//            set(ID.Order_Title_Rows, 1);
//
//        boolean bEnableFooter = pre.getBoolean("footer_enable", false);
//        if (bEnableFooter)
//            set(ID.Order_Footer_Rows, 1);
//        else
//            set(ID.Order_Footer_Rows, 0);


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
        Object obj = get(config);
        if (obj instanceof String)
        {
            int n = KDSUtil.convertStringToInt((String)obj, 0);
            return (n==1);
        }
        else if (obj instanceof Boolean) {
            return (boolean) get(config);
        }
        else
            return false;
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
            try {
                return Integer.parseInt((String) obj);
            }
            catch (Exception e)
            {
                //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSLog.getStackTrace(e));
                return 0;
            }
        }
//        else if (obj instanceof TitleContents)
//        {
//            return ((TitleContents)obj).ordinal();
//        }
        return 0;//(int)obj;
    }

    public String getString(ID config)
    {
        Object obj = get(config);
        if (obj == null) return "";
        if (obj instanceof String)
            return (String) get(config);
        else
            return "";
    }

//    public KDSViewFontFace getKDSViewFontFace(ID config)
//    {
//        //String s = getString(config);
//        //return KDSViewFontFace.parseString(s);
//
//        return (KDSViewFontFace) get(config);
//    }

    public float getFloat(ID config)
    {
        Object obj = get(config);
        if (obj instanceof Integer) {
            int n = (int)obj;
            float flt = n;
            return flt;
        }
        else if (obj instanceof  String)
        {
            String s = (String)obj;
            if (s.isEmpty())
                return 0;
            return Float.parseFloat((String )obj);
        }
        else if (obj instanceof Float)
        {
            return (float)obj;
        }

        return 0;//(int)obj;
    }







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

            Object obj = entry.getValue();
            String strid = KDSUtil.convertIntToString(id.ordinal());
            String strVal = convertConfigValToString(obj);
            String strType = getConfigValTypeStrng(obj);
            xml.newGroup("config", true);
            xml.newAttribute("id", strid);
            xml.newAttribute("ty", strType);
            xml.setGroupValue(strVal);
            xml.back_to_parent();
            //if (obj != null)
             //   this.set(id, obj);
            //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }


        return xml.get_xml_string();


    }



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


    }

    /**
     * save all data to local preference settings
     */
    public void save(Context appContext)
    {
        Context c = appContext;//app.getApplicationContext();
//
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
//
//        // SharedPreferences pre =  this.getSharedPreferences("P1", Activity.MODE_PRIVATE);
//        String str = pre.getString("kds_general_tcpport", "3000");
        for (Map.Entry<ID, String> entry : m_mapPrefID.entrySet()) {

            ID id = entry.getKey();
            String tag = entry.getValue();
            Object objVal = this.get(id);
            setPrefValue(pre, tag, objVal);

        }

    }
//    static public final String SOS_STATIONS_KEY_ITEMS = "sos_stations";
//    static public final String SOS_STATIONS_SEPARATOR = "\n";
//    /**
//     * format:
//     *  item\nitem\n
//     * @param s
//     * @return
//     */
//    public static ArrayList<SOSStationConfig> parseStations(String s)
//    {
//        ArrayList<String> ar = KDSUtil.spliteString(s, SOS_STATIONS_SEPARATOR);
//        for (int i=ar.size()-1; i>=0; i--)
//        {
//            if (ar.get(i).isEmpty())
//                ar.remove(i);
//        }
//        ArrayList<SOSStationConfig>  arStations = new ArrayList<>();
//
//        for (int i=0; i< ar.size(); i++)
//        {
//            SOSStationConfig c =  SOSStationConfig.parse(ar.get(i));
//            arStations.add(c);
//        }
//        return arStations;
//
//    }
    public KDSViewFontFace getKDSViewFontFace(ID config)
    {
        //String s = getString(config);
        //return KDSViewFontFace.parseString(s);
        Object obj = get(config);
        if (!(obj instanceof KDSViewFontFace))
            return new KDSViewFontFace();
        return (KDSViewFontFace) get(config);
    }
}
