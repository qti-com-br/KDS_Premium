package com.bematechus.kdslib;

/**
 * Created by Administrator on 2015/12/16 0016.
 */
public class KDSDataSumName extends KDSData {

    static final String TAG = "KDSDataSumName";

    String m_strItemGUID="";
    String m_strDescription="";
    float m_fltQty=0;

    public String getItemGuid()
    {
        return m_strItemGUID;
    }
    public void setItemGuid(String guid)
    {
        m_strItemGUID = guid;
    }

    public void setSumQty(float qty)
    {
        m_fltQty = qty;
    }
    public float getSumQty()
    {
        return m_fltQty;
    }

    public void setDescription(String description)    {
        m_strDescription = description;
    }
    public String getDescription()    {
        return m_strDescription;
    }

    public String toString()
    {
        return getDescription() + " (" + KDSUtil.convertFloatToString(getSumQty()) + ")";
    }
    /**
     * format:
     * @param sumName
     *  format: description (qty).
     *        e.g: beef medium(0.00)
     * @return
     */
    static public KDSDataSumName parseString(String sumName)
    {
        if (sumName.isEmpty()) return null;
        try {
            int n = sumName.lastIndexOf("(");
            String name = sumName.substring(0, n);
            String qty = sumName.substring(n + 1);
            qty = qty.replace("(", "");
            qty = qty.replace(")", "");
            KDSDataSumName sum = new KDSDataSumName();
            sum.setSumQty(KDSUtil.convertStringToFloat(qty, 0));
            sum.setDescription(name);
            return sum;
        }
        catch (Exception e)
        {
            KDSLog.d(TAG, sumName);
            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);

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
        String sql = "delete from sumnames where guid='" + strGUID + "'";
        return sql;
    }
    public String sqlAddNew()
    {
        String sql = "insert into sumnames("
                + "GUID,ItemGUID, Description,qty)"
                + " values ("
                + "'" + getGUID() + "','"
                + getItemGuid() + "','"
                + fixSqliteSingleQuotationIssue( getDescription()) + "',"
                + KDSUtil.convertFloatToString(getSumQty())
                + ")";
        return sql;
    }
    public String sqlUpdate()
    {
        String sql = "update sumnames set "
                + "ItemGUID='" +fixSqliteSingleQuotationIssue(  getItemGuid()) + "',"
                + "Description='" +fixSqliteSingleQuotationIssue(  getDescription()) + "',"
                + "qty="  + KDSUtil.convertFloatToString(getSumQty())  +","
                + "DBTimeStamp=" + KDSUtil.convertDateToString(getTimeStamp())
                + " where guid='" + getGUID() + "'";
        return sql;
    }


}
