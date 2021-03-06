package com.bematechus.kdslib;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/11/10.
 */
public class SettingsBase {
    static final private String TAG = "SettingsBase";
    public enum StationFunc
    {
        Prep,
        Expeditor,
        Queue,// "Order Queue Display",
        Mirror,
        Backup,
        Workload,
        Duplicate,
        TableTracker,
        Queue_Expo, //sos(Queue) work with expo
        Runner, //kpp1-456
        Summary, //Summary Station. KP-21
        MAX_COUNT,
    }

    public enum SlaveFunc
    {

        Unknown,
        Backup,
        Mirror,
        Automatic_work_loan_distribution,
        Duplicate_station,
        Order_Queue_Display,
    }

    public enum StationStatus
    {
        Enabled,
        Disabled
    }
    public enum KDSDataSource
    {
        TCPIP,
        Folder,

    }
    public enum Language
    {
        English,
        Chinese,
		Spanish,
		Portuguese
    }

    public enum KDSUserMode
    {
        Single,
        Multiple,
    }

    public static ArrayList<KDSStationsRelation> loadStationsRelation(Context context, boolean bNeedNoCheckOption )
    {

        ArrayList<KDSStationsRelation> ar = new ArrayList<>();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        HashSet<String> setStations = null;
        Set<String> set =  pref.getStringSet("StationsRelation", null);
        if (set == null)
            return ar;
        setStations = new HashSet<>( set);

        Iterator it =setStations.iterator();

        while(it .hasNext())
        {
            String s= (String) it.next();
            KDSStationsRelation station =  KDSStationsRelation.parseString(s);
            if (station != null)
                ar.add(station);
        }

        if (!bNeedNoCheckOption)
        {
            removeRelationNoCheckOptionStation(ar);
        }

        return ar;

    }

    public static final String NO_CHECK_TRUE = "NoCheck=1";
    public static final String NO_CHECK_FALSE = "NoCheck=0";


    /**
     * I save this option as a station.
     * Station ID is "NoCheck=1" or NoCheck=0.
     * @param ar
     */
    static public KDSStationsRelation removeRelationNoCheckOptionStation( ArrayList<KDSStationsRelation> ar)
    {
        KDSStationsRelation optionRelation = null;
        for (int i=0; i< ar.size(); i++)
        {
            if (ar.get(i).getID().indexOf(NO_CHECK_FALSE) == 0 ||
                    ar.get(i).getID().indexOf(NO_CHECK_TRUE) == 0   )
            {
                optionRelation = ar.get(i);
                ar.remove(i);
                break;
            }
        }
        return optionRelation;
    }

    static public KDSStationsRelation findRelationNoCheckOptionStation( ArrayList<KDSStationsRelation> ar)
    {

        for (int i=0; i< ar.size(); i++)
        {
            if (ar.get(i).getID().indexOf(NO_CHECK_FALSE) == 0 ||
                    ar.get(i).getID().indexOf(NO_CHECK_TRUE) == 0   )
            {

                return ar.get(i);

            }
        }
        return null;
    }

    static public void addRelationNoCheckOptionStation(ArrayList<KDSStationsRelation> arsave, boolean bNoCheckRelations)
    {
        KDSStationsRelation noCheckRelation = new KDSStationsRelation();
        noCheckRelation.setID( (bNoCheckRelations?NO_CHECK_TRUE:NO_CHECK_FALSE) );
        arsave.add(noCheckRelation);

    }
    static final String RELATION_STATION_SEPERATOR = ":";
    /**
     *
     * @param context
     * @return
     */
    public static String loadStationsRelationString(Context context, boolean bNeedNoCheckOption )
    {

        ArrayList<KDSStationsRelation> ar = loadStationsRelation(context, bNeedNoCheckOption);
        int ncount = ar.size();
        String str = "";
        for (int i=0; i< ncount; i++)
        {
            str += ar.get(i).toString();
            if (i < ncount-1)
                str += RELATION_STATION_SEPERATOR;
        }

        return str;

    }

    /**
     *
     * @param strRelations
     * @return
     */
    static public ArrayList<KDSStationsRelation> parseStationsRelations(String strRelations)
    {
        ArrayList<KDSStationsRelation> arRelations = new  ArrayList<>();
        ArrayList<String> ar = KDSUtil.spliteString(strRelations, RELATION_STATION_SEPERATOR);
        int ncount = ar.size();
        for (int i=0; i< ncount; i++)
        {
            String s = ar.get(i);
            if (s.isEmpty()) continue;
            KDSStationsRelation relation = KDSStationsRelation.parseString(s);
            if (relation != null)
                arRelations.add(relation);

        }
        return arRelations;
    }
    /**
     *
     * @param context
     * @param strSettings
     */
    static public void saveStationsRelation(Context context, String strSettings)
    {

        ArrayList<KDSStationsRelation> arRelations = parseStationsRelations(strSettings);
        saveStationsRelation(context, arRelations);

    }

    /**
     *
     * @param context
     * @param ar

     */
    static public void saveStationsRelation(Context context, ArrayList<KDSStationsRelation> ar)
    {
        int ncount = ar.size();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        HashSet<String> setStations = new HashSet<>();

        for (int i=0; i< ncount; i++)
        {
            KDSStationsRelation r = ar.get(i);
            if (r != null)
            {
                setStations.add(r.toString());
            }
        }


        SharedPreferences.Editor editor =  pref.edit();
        editor.putStringSet("StationsRelation", setStations);
        editor.commit();

    }

    public Object getPrefValue(SharedPreferences pref, String strTag, Object objDef)
    {
        if (strTag.isEmpty())
            return null;
        String s = strTag;
        int n = s.indexOf("_", 0);
        s = s.substring(0, n);
        String tag = strTag.substring(n + 1);

        Object objVal = pref.getAll().get(tag);
        if (objVal == null) return objDef;


        if (s.equals("int"))
        {

            int ndef = 0;
            if (objDef instanceof  Integer)
                ndef = (int)objDef;

            if (objVal instanceof Integer)
                return objVal;
            else if (objVal instanceof String)
                return KDSUtil.convertStringToInt((String)objVal, ndef);
            else
                return ndef;
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
        }
        else if (s.equals("string"))
        {

            String strdef = "";

            if (objDef instanceof  String)
                strdef = (String)objDef;
            else if (objDef instanceof Integer)
                strdef = objDef.toString();

            if (objVal instanceof String)
                return (String)objVal;
            else if (objVal instanceof Integer)
                return KDSUtil.convertIntToString((int)objVal);
            else
                return strdef;
//            try {
//
//                return pref.getString(tag, strdef);
//            }
//            catch (Exception e)
//            {
//                Log.e(TAG, tag);
//                Log.e(TAG,KDSLog._FUNCLINE_() + e.toString());
//                Log.e(TAG, KDSUtil.error( e));
//                int nint = pref.getInt(tag, KDSUtil.convertStringToInt(strdef, 0));
//                return KDSUtil.convertIntToString(nint);
//
//            }
        }
        else if (s.equals("bool"))
        {
            boolean bdef = false;
            if (objDef instanceof Boolean)
                bdef = (boolean)objDef;

            if (objVal instanceof Boolean)
                return (Boolean)objVal;
            else
                return bdef;
            //return pref.getBoolean(tag, bdef);
        }
        else if (s.equals("fontface"))
        {
            if (!(objDef instanceof KDSViewFontFace))
                return new KDSViewFontFace();
            if (objVal instanceof String) {
                String strdef = ((KDSViewFontFace) objDef).toString();
                String strfont = pref.getString(tag, strdef);
                return KDSViewFontFace.parseString(strfont);
            }
            else
                return new KDSViewFontFace();
        }
        return null;

    }
    /**
     * save to preferences
     * @param pref
     * @param strTag
     * @param objVal
     * @return
     */
    public void setPrefValue(SharedPreferences pref, String strTag, Object objVal)
    {
        SharedPreferences.Editor editor = pref.edit();
        setPrefValue(editor, strTag, objVal);
        editor.commit();
        editor.apply();
//        if (strTag.isEmpty())
//            return ;
//        if (objVal == null)
//        {
//            KDSLog.d(TAG, KDSLog._FUNCLINE_() + " Error, val=null: tag=" + strTag );
//            return;
//        }
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
//            if (objVal instanceof String)
//            {
//                int nVal = KDSUtil.convertStringToInt((String)objVal, 0);
//                editor.putBoolean(tag, ( nVal == 1));
//            }
//            else
//                editor.putBoolean(tag, (boolean)objVal);
//
//        }
//        else if (s.equals("fontface"))
//        {
//
//            String str = ((KDSViewFontFace)objVal).toString();
//            editor.putString(tag, (String) str);
//
//        }
//
//        editor.commit();
//        editor.apply();
//

    }


    public static Language loadLanguageOption(Context c)
    {
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
        String str = pre.getString("kds_general_language", "0");
        int n = KDSUtil.convertStringToInt(str, 0);
        return Language.values()[n];

    }

    public static KDSLog.LogLevel loadLogLevel(Context c)
    {
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
        String str = pre.getString("log_mode", "0");
        int n = KDSUtil.convertStringToInt(str, 0);
        return KDSLog.LogLevel.values()[n];

    }

    public static String getLanguageString(Language lan)
    {
        switch (lan)
        {

            case English:
                return KDSApplication.getContext().getString(R.string.english);// "English";

            case Chinese:
                return KDSApplication.getContext().getString(R.string.chinese);//"Chinese";

			case Spanish:
				return KDSApplication.getContext().getString(R.string.spanish);

			case Portuguese:
				return KDSApplication.getContext().getString(R.string.portuguese);

            default:
                return KDSApplication.getContext().getString(R.string.english);//"English";
        }
    }

    protected String getConfigValTypeStrng(Object obj)
    {
        if (obj instanceof Integer)
        {
            return "int";
        }
        else if (obj instanceof String)
        {
            return "str";
        }
        else if (obj instanceof  Boolean)
        {
            return "bool";
        }
        else if (obj instanceof KDSViewFontFace)
        {
            return "fontface";
        }
        else
            return "";


    }
    protected String convertConfigValToString(Object obj)
    {
        if (obj instanceof Integer)
        {
            return KDSUtil.convertIntToString((int)obj);

        }
        else if (obj instanceof String)
        {
            return (String)obj;
        }
        else if (obj instanceof  Boolean)
        {
            boolean b = (boolean)obj;
            if (b) return "1";
            return "0";
        }
        else if (obj instanceof KDSViewFontFace)
        {
            KDSViewFontFace f = (KDSViewFontFace)obj;
            return f.toString();
        }
        else
            return obj.toString();
    }

    protected Object convertConfigStringToType(String strVal, String toType)
    {
        if (toType.equals("int"))
        {
            if (strVal.isEmpty())
                return 0;
            return Integer.parseInt(strVal);
        }
        else if (toType.equals("str"))
        {
            return strVal;
        }
        else if (toType.equals("bool"))
        {
            if (strVal.equals("0"))
                return false;
            else if (strVal.equals("1"))
                return true;
            else
                return false;

        }
        else if (toType.equals("fontface"))
        {
            KDSViewFontFace ff = KDSViewFontFace.parseString(strVal);
            return ff;
        }
        else
        {
            return strVal;
        }
    }

    /**
     * A quick function to load "No check relation option" value.
     * @param context
     * @return
     */
    public static boolean isNoCheckRelationWhenAppStart(Context context )
    {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        HashSet<String> setStations = null;
        Set<String> set =  pref.getStringSet("StationsRelation", null);
        if (set == null)
            return false;
        setStations = new HashSet<>( set);

        Iterator it =setStations.iterator();

        while(it .hasNext())
        {
            String s= (String) it.next();
            if (s.indexOf(NO_CHECK_TRUE) >=0)
                return true;

        }

        return false;
    }


    static public void saveTouchPadVisible(Context context, boolean bVisible)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);


        SharedPreferences.Editor editor =  pref.edit();
        editor.putBoolean("TouchPadVisible", bVisible);
        editor.commit();
    }

    static public boolean loadTouchPadVisible(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        return pref.getBoolean("TouchPadVisible", true);


    }
    public boolean setPrefValue(SharedPreferences.Editor editor, String strTag, Object objVal)
    {
        if (strTag.isEmpty())
            return false;
        if (objVal == null)
        {
            KDSLog.d(TAG, KDSLog._FUNCLINE_() + " Error, val=null: tag=" + strTag );
            return false;
        }
        String s = strTag;
        int n = s.indexOf("_", 0);
        s = s.substring(0, n);
        String tag = strTag.substring(n + 1);

        if (s.equals("int"))
        {
            editor.putInt(tag, (int) objVal);
        }
        else if (s.equals("string"))
        {
            if (objVal instanceof String)
                editor.putString(tag, (String) objVal);
            else
                editor.putString(tag, objVal.toString());
        }
        else if (s.equals("bool"))
        {
            if (objVal instanceof String)
            {
                int nVal = KDSUtil.convertStringToInt((String)objVal, 0);
                editor.putBoolean(tag, ( nVal == 1));
            }
            else
                editor.putBoolean(tag, (boolean)objVal);

        }
        else if (s.equals("fontface"))
        {

            String str = ((KDSViewFontFace)objVal).toString();
            editor.putString(tag, (String) str);

        }
        return true;
//        editor.commit();
//        editor.apply();


    }
}
