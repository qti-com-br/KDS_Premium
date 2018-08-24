package com.bematechus.kdsrouter;

import com.bematechus.kdslib.KDSData;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;

/**
 * Created by Administrator on 2018/1/29.
 * The item modifier data.
 *
 */
public class KDSRouterDataItemModifier extends KDSData {
    static final String TAG = "KDSDataItemModifier";

    String m_strItemGUID="";
    String m_strDescription="";
    int m_nPreparationTimeSeconds=0;

    public String getItemGuid()
    {
        return m_strItemGUID;
    }
    public void setItemGuid(String guid)
    {
        m_strItemGUID = guid;
    }

    public String getDescription()
    {
        return m_strDescription;
    }
    public void setDescription(String strDescription)
    {
        m_strDescription = strDescription;
    }
    public int getPrepTime()
    {
        return m_nPreparationTimeSeconds;
    }
    public void setPrepTime(int nSeconds)
    {
        m_nPreparationTimeSeconds = nSeconds;
    }
    public int getPrepTimeMins()
    {
        return (int)( m_nPreparationTimeSeconds /60);
    }
    public int getPrepTimeSeconds()
    {
        return (int)( m_nPreparationTimeSeconds %60);

    }

    public String toString()
    {
        return getDescription() + " (" + KDSUtil.convertIntToString(getPrepTime()) + ")";
    }

    /**
     * format:
     * @param strModifier
     *  format: description (qty).
     *        e.g: beef medium(0.00)
     * @return
     */
    static public KDSRouterDataItemModifier parseString(String strModifier)
    {
        if (strModifier.isEmpty()) return null;
        try {
            int n = strModifier.lastIndexOf("(");
            String name = strModifier.substring(0, n);
            String secs = strModifier.substring(n + 1);
            secs = secs.replace("(", "");
            secs = secs.replace(")", "");
            KDSRouterDataItemModifier modifer = new KDSRouterDataItemModifier();
            modifer.setPrepTime(KDSUtil.convertStringToInt(secs, 0));
            modifer.setDescription(name);
            return modifer;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, strModifier);
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);// KDSLog.getStackTrace(e));

        }
        return null;



    }
    /**
     * use the guid to control it
     * @return
     */
    public String sqlDelete()
    {
        return sqlDelete(getGUID());
    }

    public static String sqlDelete(String strGUID)
    {
        String sql = "delete from modifiers where guid='" + strGUID + "'";
        return sql;
    }
    public String sqlAddNew()
    {
        String sql = "insert into modifiers("
                + "GUID,ItemGUID, Description,preptime)"
                + " values ("
                + "'" + getGUID() + "','"
                + getItemGuid() + "','"
                + fixSqliteSingleQuotationIssue( getDescription()) + "',"
                + KDSUtil.convertIntToString(getPrepTime())
                + ")";
        return sql;
    }
    public String sqlUpdate()
    {
        String sql = "update modifiers set "
                + "ItemGUID='" +fixSqliteSingleQuotationIssue(  getItemGuid()) + "',"
                + "Description='" +fixSqliteSingleQuotationIssue(  getDescription()) + "',"
                + "preptime="  + KDSUtil.convertIntToString(getPrepTime())  +","
                + "DBTimeStamp=" + KDSUtil.convertDateToString(getTimeStamp())
                + " where guid='" + getGUID() + "'";
        return sql;
    }


}
