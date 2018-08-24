package com.bematechus.kdsrouter;

import android.graphics.Color;
import android.graphics.Paint;

import com.bematechus.kdslib.CSVStrings;
import com.bematechus.kdslib.KDSData;
import com.bematechus.kdslib.KDSDataSumNames;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/16 0016.
 */
public class KDSRouterDataCategory extends KDSData {


    String m_strDescription="";
    int m_nBG = 0;
    int m_nFG = 0;
    String m_strToStation="";
    String m_strToScreen="";
    boolean m_bPrintable = true;
    float m_fltDelay = 0;
    KDSRouterDataItems m_items = null;


    public void setToStation(String strStationID)
    {
        m_strToStation = strStationID;
    }
    public String getToStation()
    {
        return m_strToStation;
    }

    public void setToScreen(String strScreen)
    {
        m_strToScreen = strScreen;
    }
    public String getToScreen()
    {
        return m_strToScreen;
    }

    public void setPrintable(boolean bPrintable)
    {
        m_bPrintable = bPrintable;
    }
    public boolean getPrintable()
    {
        return m_bPrintable;
    }

    public void setDelay(float fltDelay)
    {
        m_fltDelay = fltDelay;
    }
    public float getDelay()
    {
        return m_fltDelay;
    }

    public int getDelayMins()
    {
        float f = m_fltDelay  *60;
        int m =(int)( f/60);
        return m;
    }
    public int getDelaySecs()
    {
        float f = m_fltDelay  *60;
        int m =(int)( f/60);
        int sec =Math.round( f % 60);
        return sec;
    }
    public String getDelayFormated()
    {
        float f = m_fltDelay  *60;
        int m =(int)( f/60);
        int sec =Math.round((f % 60));
        if (m_fltDelay != 0)
            return String.format("%d:%02d", m, sec);
        else
            return "";

    }

    public void setItems(KDSRouterDataItems items)
    {
        m_items = items;
    }
    public KDSRouterDataItems getItems()    {
        return m_items;
    }

    public void setDescription(String description)    {
        m_strDescription = description;
    }
    public String getDescription()    {
        return m_strDescription;
    }
    public int getBG()
    {
        return m_nBG;
    }
    public void setBG(int nBG)
    {
        if (!isAssignedColor())
        {
            m_nFG = Color.BLACK;
        }
        m_nBG = nBG;
    }
    public int getFG()    {        return m_nFG;    }
    public void setFG(int nFG)
    {
        if (!isAssignedColor())
        {
            m_nBG = Color.WHITE;
        }
        m_nFG = nFG;
    }
    public boolean isAssignedColor()
    {
        if ( (m_nFG != 0) || (m_nBG != 0))
            return true;
        return false;
    }

    static final String CSV_TAG = "Category:";

    public String toCSV()
    {
        String s = String.format("%s%s,%s,%s,%f,%s,%s,%s",
                                CSV_TAG,
                                m_strDescription.replace(",",KDSDataSumNames.CSV_INTERNAL_SEPARATOR),
                                m_strToStation.replace(",", KDSDataSumNames.CSV_INTERNAL_SEPARATOR),
                                m_strToScreen.replace(",",KDSDataSumNames.CSV_INTERNAL_SEPARATOR),
                                m_fltDelay,
                                m_bPrintable?"1":"0",
                                KDSUtil.convertIntToString(m_nBG),
                                KDSUtil.convertIntToString(m_nFG)
                                );
        return s;
    }

    static public boolean csvIsCategory(String s)
    {
        return (s.indexOf(CSV_TAG) >=0);
    }

    public boolean fromCSV(String strCSV)
    {
        if (strCSV.indexOf(CSV_TAG)<0)
             return false;
        strCSV = strCSV.replace(CSV_TAG, "");
        //CSVStrings csv = CSVStrings.parse(strCSV);
        ArrayList<String> csv = KDSUtil.spliteString(strCSV, ",");
        if (csv.size()<7) return false;
        m_strDescription = csv.get(0);
        m_strToStation = csv.get(1).replace(KDSDataSumNames.CSV_INTERNAL_SEPARATOR,",");
        m_strToScreen = csv.get(2).replace(KDSDataSumNames.CSV_INTERNAL_SEPARATOR,",");
        m_fltDelay = KDSUtil.convertStringToFloat(csv.get(3), 0);
        m_bPrintable = KDSUtil.convertStringToBool(csv.get(4), true);
        m_nBG = KDSUtil.convertStringToInt(csv.get(5), 0);
        m_nFG = KDSUtil.convertStringToInt(csv.get(6), 0);
        return true;


    }

    /**
     * use the guid to control it
     * @return
     */
    public String sqlDelete()
    {
        return sqlDelete(getGUID());
    }

    public static String sqlDelete( String strGUID)
    {
        String sql = "delete from category where guid='" + strGUID + "'";
        return sql;
    }
    public String sqlAddNew()
    {

        String sql = "insert into category("
                + "GUID,Description,bg,fg,ToStation,"
                + "ToScreen,Printable,Delay) "
                + " values ("
                + "'" + getGUID() + "','"
                + fixSqliteSingleQuotationIssue( getDescription()) + "',"
                + KDSUtil.convertIntToString(getBG()) +","
                + KDSUtil.convertIntToString(getFG()) +",'"
                + fixSqliteSingleQuotationIssue( getToStation()) + "','"
                + fixSqliteSingleQuotationIssue(  getToScreen()) +"',"
                + KDSUtil.convertBoolToString(getPrintable()) +","
                + KDSUtil.convertFloatToString(getDelay())
                + ")";
        return sql;
    }
    public String sqlUpdate()
    {
        String sql = "update category set "
                + "Description='" +fixSqliteSingleQuotationIssue(  getDescription()) + "',"
                + "bg="+  KDSUtil.convertIntToString(getBG()) + ","
                + "fg="+  KDSUtil.convertIntToString(getFG()) + ","
                + "ToStation='" + fixSqliteSingleQuotationIssue(  getToStation()) + "',"
                + "ToScreen='"+  fixSqliteSingleQuotationIssue(getToScreen()) +"',"
                + "Printable=" + KDSUtil.convertBoolToString(getPrintable()) +","
                + "Delay="  + KDSUtil.convertFloatToString(getDelay())  +","
                + "DBTimeStamp='" + KDSUtil.convertDateToString(getTimeStamp())
                + "' where guid='" + getGUID() + "'";
        return sql;
    }

}
