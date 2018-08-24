package com.bematechus.kdslib;

/**
 * Created by Administrator on 2018/1/31.
 */
public class KDSDataModifier extends KDSDataCondiment {

    int m_nPreparationTimeSeconds=0;

    public KDSDataModifier()
    {

        super();

    }

    public KDSDataModifier(String itemGUID)
    {
        super(itemGUID);

    }

    public KDSDataModifier(String itemGUID, String myGuid)
    {
        super(itemGUID, myGuid);

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


    /***************************************************************************
     *
     * @param obj
     */
    public void copyTo(KDSData obj)
    {
        super.copyTo(obj);
        KDSDataModifier c = (KDSDataModifier)obj;
        c.m_nPreparationTimeSeconds = m_nPreparationTimeSeconds;


    }

    public String sqlAddNew(String tblName)
    {
        String sql = "insert into "
                + tblName
                + " ("
                + "GUID,ItemGUID,Name,Description,BG,FG,Hiden,Bumped,PrepTime) values ("
                + "'" + getGUID() + "',"
                + "'" + getItemGUID() + "',"
                + "'" +fixSqliteSingleQuotationIssue(  getCondimentName()) + "',"
                + "'" +fixSqliteSingleQuotationIssue(  getDescription()) + "',"
                + KDSUtil.convertIntToString(getBG()) + ","
                + KDSUtil.convertIntToString(getFG()) + ","
                + KDSUtil.convertBoolToString(getHiden())+ ","
                + KDSUtil.convertBoolToString(getBumped())
                + "," + KDSUtil.convertIntToString(getPrepTime()) + ")";


        return sql;


    }
    public String sqlUpdate()
    {
        String sql = "update modifiers set "
                + "ItemGUID='"+ getItemGUID() + "',"
                + "Name='"+ getCondimentName() + "',"
                + "Description='" + getDescription() + "',"
                + "BG=" + KDSUtil.convertIntToString(getBG()) + ","
                + "FG="+ KDSUtil.convertIntToString(getFG()) + ","
                + "Hiden="+ KDSUtil.convertBoolToString(getHiden()) + ","
                + "Bumped="+ KDSUtil.convertBoolToString(getBumped()) + ","
                + "PrepTime="+ KDSUtil.convertIntToString(getPrepTime()) + ","
                + "DBTimeStamp='"+ KDSUtil.convertDateToString(getNow())//TimeStamp())
                //+"' where id=" + Common.KDSUtil.ConvertIntToString(getID());
                +"' where guid='" + getGUID() + "'";

        return sql;

    }
    public String sqlDelete()
    {

        String sql = sqlDelete(getGUID());
        return sql;
    }


    public static String sqlDelete(String strGUID)
    {
        String sql = "delete from modifiers where guid='"+ strGUID +"'";
        return sql;
    }

    public boolean isEqual(KDSDataModifier modifier)
    {
        boolean b = super.isEqual(modifier);
        if (!b) return b;
        if (this.getPrepTime() != modifier.getPrepTime())
            return false;

        return true;

    }

    public boolean modifyModifier(KDSDataModifier modifierReceived)
    {
        return super.modifyCondiment(modifierReceived);


    }


}
