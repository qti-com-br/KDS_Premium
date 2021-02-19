package com.bematechus.kdsrouter;

import android.graphics.Color;
import com.bematechus.kdslib.CSVStrings;
import com.bematechus.kdslib.KDSData;
import com.bematechus.kdslib.KDSDataSumNames;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/16 0016.
 */
public class KDSRouterDataItem extends KDSData {


    String m_strCategoryGuid = "";
    int m_nCategoryID = 0;
    String m_strDescription= "";
    float m_fltPreparationTime=0;
    float m_fltDelay=0; //unused

    int m_nBG=0;
    int m_nFG=0;
    String m_strToStation= "";
    String m_strToScreen= "";
    boolean m_bPrintable=true;

    boolean m_bSumTranslate = false;

    KDSDataSumNames m_sumNames = new KDSDataSumNames();

    boolean m_bModifierEnabled = false;
    KDSRouterDataItemModifiers m_modifiers = new KDSRouterDataItemModifiers();


    protected CSVStrings m_arBuildCard = new CSVStrings();
    protected CSVStrings m_arVideo = new CSVStrings();

    public void setModifierEnabled(boolean bEnabled)
    {
        m_bModifierEnabled = bEnabled;
    }
    public boolean getModifierEnabled()
    {
        return m_bModifierEnabled;
    }

    public void setSumTranslateEnabled(boolean bEnabled)
    {
        m_bSumTranslate = bEnabled;
    }
    public boolean getSumTranslateEnabled()
    {
        return m_bSumTranslate;
    }

   public String getCategoryGuid()
   {
      return m_strCategoryGuid;
   }
   public void setCategoryGuid(String guid)
   {
      m_strCategoryGuid = guid;
   }

    public void setSumNames(KDSDataSumNames names)
    {
       m_sumNames = names;
    }
    public KDSDataSumNames getSumNames()    {
      return m_sumNames;
    }

    public void setModifiers(KDSRouterDataItemModifiers names)
    {
        m_modifiers = names;
    }
    public KDSRouterDataItemModifiers getModifiers()    {
        return m_modifiers;
    }

    public String getSumTranslateText()
    {
        if (m_sumNames.getCount()<=0) return "";
        return m_sumNames.getSumName(0).getDescription();
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
    public float getPreparationTime()
    {
        return m_fltPreparationTime;
    }

    public int getPreparationTimeMins()
    {
        float f = m_fltPreparationTime  *60;
        int m =(int)( f/60);
        return m;
    }

    public int getPreparationTimeSecs()
    {
        float f = m_fltPreparationTime  *60;
        int m =(int)( f/60);
        int sec =Math.round( f % 60);
        return sec;
    }

    public String getPreparationTimeFormated()
    {
        float f = m_fltPreparationTime  *60;
        int m =(int)( f/60);
        int sec =Math.round( f % 60);
        if (m_fltPreparationTime != 0)
            return String.format("%d:%02d", m, sec);
        else
            return "";
    }

    public void setPreparationTime(float fltDelay)
    {
       m_fltPreparationTime = fltDelay;
    }

    public CSVStrings getBuildCard()
    {
        return m_arBuildCard;
    }
    public CSVStrings getTrainingVideo()
    {
        return m_arVideo;
    }

    public void setBuildCard(String csvString)
    {
        m_arBuildCard = CSVStrings.parse(csvString);
    }

    public void setTrainingVideo(String csvString)
    {
        m_arVideo = CSVStrings.parse(csvString);
    }

    public void updateSumNamesGuid()
    {
        for (int i=0; i< m_sumNames.getCount(); i++)
        {
            m_sumNames.getSumName(i).setItemGuid(this.getGUID());
        }
    }

    public void updateModifiersGuid()
    {
        for (int i=0; i< m_modifiers.getCount(); i++)
        {
            m_modifiers.getModifier(i).setItemGuid(this.getGUID());
        }
    }
    /**
     * Descriptin, ToStations, ToScreen,Delay,Printable,BG,FG,SumTranslated,SumNames,BuildCards,Videos
     * @return
     */
    public String toCSV()
    {
        String s = String.format("%s,%s,%s,%f,%s,%s,%s",
                m_strDescription.replace(",",KDSDataSumNames.CSV_INTERNAL_SEPARATOR),
                m_strToStation.replace(",",KDSDataSumNames.CSV_INTERNAL_SEPARATOR),
                m_strToScreen.replace(",",KDSDataSumNames.CSV_INTERNAL_SEPARATOR),
                m_fltDelay,
                m_bPrintable?"1":"0",
                KDSUtil.convertIntToString(m_nBG),
                KDSUtil.convertIntToString(m_nFG)
        );
        s +=","+ (m_bSumTranslate?"1":"0");
        s += "," + m_sumNames.toStringForCSV();//.replace("\n",KDSDataSumNames.CSV_INTERNAL_SEPARATOR );
        s +="," + m_arBuildCard.toCSV().replace(",",KDSDataSumNames.CSV_INTERNAL_SEPARATOR );
        s +="," + m_arVideo.toCSV().replace(",",KDSDataSumNames.CSV_INTERNAL_SEPARATOR );

        return s;

    }
    public boolean fromCSV(String strCSV)
    {
        if (strCSV.isEmpty()) return false;
       // CSVStrings csv = CSVStrings.parse(strCSV);
        ArrayList<String> csv = KDSUtil.spliteString(strCSV, ",");
        if (csv.size()<11) return false;
        m_strDescription = csv.get(0);
        m_strToStation = csv.get(1).replace(KDSDataSumNames.CSV_INTERNAL_SEPARATOR,",");
        m_strToScreen = csv.get(2).replace(KDSDataSumNames.CSV_INTERNAL_SEPARATOR,",");
        m_fltDelay = KDSUtil.convertStringToFloat(csv.get(3), 0);
        m_bPrintable = KDSUtil.convertStringToBool(csv.get(4), true);
        m_nBG = KDSUtil.convertStringToInt(csv.get(5), 0);
        m_nFG = KDSUtil.convertStringToInt(csv.get(6), 0);

        m_bSumTranslate = KDSUtil.convertStringToBool(csv.get(7), true);
        m_sumNames = KDSDataSumNames.parseStringForCSV(csv.get(8));
        m_arBuildCard = CSVStrings.parse(csv.get(9));
        m_arVideo = CSVStrings.parse(csv.get(10));
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
     String sql = "delete from items where guid='" + strGUID + "'";
     return sql;
    }
    public String sqlAddNew()
    {

     String sql = "insert into items("
             + "GUID,CategoryGUID, Description,PreparationTime,Delay, bg,fg,ToStation,"
             + "ToScreen,Printable,SumTrans,BuildCards,TrainingVideo,r0) "
             + " values ("
             + "'" + getGUID() + "','"
             + getCategoryGuid() + "','"
             + fixSqliteSingleQuotationIssue( getDescription()) + "',"
             + KDSUtil.convertFloatToString(getPreparationTime()) + ","
             + KDSUtil.convertFloatToString(getDelay()) + ","
             + KDSUtil.convertIntToString(getBG()) +","
             + KDSUtil.convertIntToString(getFG()) +",'"
             + fixSqliteSingleQuotationIssue( getToStation()) + "','"
             + fixSqliteSingleQuotationIssue(  getToScreen()) +"',"
             + KDSUtil.convertBoolToString(getPrintable()) +","
             + KDSUtil.convertBoolToString(getSumTranslateEnabled()) +",'"
             + getBuildCard().toCSV() +"','"
             + getTrainingVideo().toCSV() +"'"
            +",'" + (m_bModifierEnabled?"1":"0") +"' )";

     return sql;
    }
    public String sqlUpdate()
    {
     String sql = "update items set "
             + "CategoryGUID='" +fixSqliteSingleQuotationIssue(  getCategoryGuid()) + "',"
             + "Description='" +fixSqliteSingleQuotationIssue(  getDescription()) + "',"
             + "PreparationTime="  + KDSUtil.convertFloatToString(getPreparationTime())  +","
             + "Delay="  + KDSUtil.convertFloatToString(getDelay())  +","
             + "bg="+  KDSUtil.convertIntToString(getBG()) + ","
             + "fg="+  KDSUtil.convertIntToString(getFG()) + ","
             + "ToStation='" + fixSqliteSingleQuotationIssue(  getToStation()) + "',"
             + "ToScreen='"+  fixSqliteSingleQuotationIssue(getToScreen()) +"',"
             + "Printable=" + KDSUtil.convertBoolToString(getPrintable()) +","
             + "SumTrans=" + KDSUtil.convertBoolToString(getSumTranslateEnabled()) +","
             + "BuildCards='"+getBuildCard().toCSV() +"',"
             + "TrainingVideo='"+getTrainingVideo().toCSV() +"',"
             + "r0='"+(m_bModifierEnabled?"1":"0") +"',"
             + "DBTimeStamp='" + KDSUtil.convertDateToString(getTimeStamp())
             + "' where guid='" + getGUID() + "'";
     return sql;
    }

//    public int getMinutesFromMinsFloat(float mins)
//    {
//        float f = mins  *60;
//        int m =(int)( f/60);
//        return m;
//    }
//
//    public int getSecondsFromMinsFloat(float mins)
//    {
//        float f = mins  *60;
//        int m =(int)( f/60);
//        int sec =Math.round( f % 60);
//        return sec;
//    }

    public int getDelayMins()
    {
        return KDSUtil.getMinutesFromMinsFloat(m_fltDelay);
//        float f = m_fltPreparationTime  *60;
//        int m =(int)( f/60);
//        return m;
    }

    public int getDelaySecs()
    {
        return KDSUtil.getSecondsFromMinsFloat(m_fltDelay);

//        float f = m_fltPreparationTime  *60;
//        int m =(int)( f/60);
//        int sec =Math.round( f % 60);
//        return sec;
    }

    public String getDelayTimeFormated()
    {
        float f = m_fltDelay  *60;
        int m =(int)( f/60);
        int sec =Math.round( f % 60);
        if (m_fltDelay != 0)
            return String.format("%d:%02d", m, sec);
        else
            return "";
    }
}
