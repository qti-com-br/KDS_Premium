package com.bematechus.kdsrouter;

import com.bematechus.kdslib.KDSXMLParserCommand;

/**
 * Created by Administrator on 2015/12/29 0029.
 */
public class KDSRouterXMLParserCommand extends KDSXMLParserCommand {


    static public String createAskDBStatus(String strStationID, String ipAddress, String macAddress)
    {
        return createCommandXmlString(KDSCommand.ROUTER_ASK_DB_STATUS.ordinal(),
                strStationID, ipAddress, macAddress, "");
    }

    static public String createAskDatabaseData(String strStationID, String ipAddress, String macAddress)
    {
        return createCommandXmlString(KDSCommand.ROUTER_ASK_DB_DATA.ordinal(),
                strStationID, ipAddress, macAddress, "");
    }



    static  public String createRouterDBStatusNotification(String strStationID, String ipAddress, String macAddress,String strChangesFlag)
    {
        java.util.ArrayList ar = new java.util.ArrayList();
        ar.add(strChangesFlag);


        return createShortCommandXmlString(KDSCommand.ROUTER_FEEDBACK_DB_STATUS.ordinal(),
                strStationID, ipAddress, macAddress, ar);

    }

    static  public String createUpdateRouterChangesGUID(String strStationID, String ipAddress, String macAddress,String strChangesGUID)
    {
        java.util.ArrayList ar = new java.util.ArrayList();
        ar.add(strChangesGUID);


        return createShortCommandXmlString(KDSCommand.ROUTER_UPDATE_CHANGES_FLAG.ordinal(),
                strStationID, ipAddress, macAddress, ar);

    }

    /**
     * If primary router changed the database, we send this sql to slave router.
     *
     * @param strStationID
     * @param ipAddress
     * @param macAddress
     * @param sql
     * @param changedsGuidBeforeSql
     *  Before run this sql, the changes guid value
     *  Use this guid to find if the database is same before this sql.
     * @param changesGuidAfterSql
     * After exe this sql, the changes guid value
     * @return
     */
    static  public String createRouterSyncSqlCommand(String strStationID, String ipAddress, String macAddress
                                               ,String sql, String changedsGuidBeforeSql ,String changesGuidAfterSql)
    {
        java.util.ArrayList ar = new java.util.ArrayList();
        ar.add(sql);
        ar.add(changedsGuidBeforeSql);
        ar.add(changesGuidAfterSql);

        return createShortCommandXmlString(KDSCommand.ROUTER_SQL_SYNC.ordinal(),
                strStationID, ipAddress, macAddress, ar);

    }

    /**
     * for transfering the database to primary/slave.
     * @param strStationID
     * @param ipAddress
     * @param macAddress
     * @param sql
     * @return
     */
    static  public String createSqlRouterDB(String strStationID, String ipAddress, String macAddress, String sql)
    {

        return createCommandXmlString(KDSCommand.ROUTER_DB_SQL.ordinal(),
                strStationID, ipAddress, macAddress, sql);

    }

}
