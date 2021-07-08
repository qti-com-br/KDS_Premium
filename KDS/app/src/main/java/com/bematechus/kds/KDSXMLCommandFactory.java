package com.bematechus.kds;

import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSXMLParserCommand;

/**
 * Created by Administrator on 2015/9/17 0017.
 *
 * Build all sync xml text
 *
 */
public class KDSXMLCommandFactory {


    /**
     * send sync information to expeditor station
     * @param syncCmd
     *      What type information
     * @param order
     *      Which order
     *      It can been null
     * @param item
     *      Which item.
     *      It can been null
     */
    static public String sync_with_others(String strStationID, String ip, String mac, KDSXMLParserCommand.KDSCommand syncCmd, KDSDataOrder order, KDSDataItem item, String xmlData )
    {

        switch(syncCmd)
        {
            case Station_Add_New_Order:
                return KDSXMLParserCommand.createNewOrderNotification(strStationID, ip, mac, xmlData.isEmpty()?order.createXml():xmlData);


            case Station_Bump_Order:

                return KDSXMLParserCommand.createOrderBumpNotification(strStationID, ip, mac,xmlData.isEmpty()?order.createIDXml():xmlData);// order.createXml());
            case Expo_Bump_Order:
                return KDSXMLParserCommand.createCommandXmlString(syncCmd, strStationID, ip, mac, xmlData.isEmpty()?order.createXml():xmlData);//order.createXml());

            case Station_Unbump_Order:

                return KDSXMLParserCommand.createOrderUnbumpNotification(strStationID, ip, mac, xmlData.isEmpty()?order.createXml():xmlData);//order.createXml());
            case Expo_Unbump_Order:
                return KDSXMLParserCommand.createCommandXmlString(syncCmd, strStationID, ip, mac, xmlData.isEmpty()?order.createXml():xmlData);//order.createXml());

            case Station_Cancel_Order:
                return KDSXMLParserCommand.createOrderCanceledNotification(strStationID, ip, mac, xmlData.isEmpty()?order.createXml():xmlData);//order.createXml());
            case Station_Transfer_Order:
                return KDSXMLParserCommand.createOrderTransferNotification(strStationID, ip, mac, xmlData.isEmpty()?order.createXml():xmlData);//order.createXml());
            case Station_Modify_Order:
                return KDSXMLParserCommand.createOrderModifiedNotification(strStationID, ip, mac, xmlData.isEmpty()?order.createXml():xmlData);//order.createXml());

            case Station_Bump_Item:

                return KDSXMLParserCommand.createItemBumpNotification(strStationID, ip, mac, order.getOrderName(), item.getItemName());
            case Expo_Bump_Item:
                return KDSXMLParserCommand.createItemNotification(syncCmd,strStationID, ip, mac, order.getOrderName(), item.getItemName());

            case Station_Unbump_Item:

                return KDSXMLParserCommand.createItemUnbumpNotification(strStationID, ip, mac, order.getOrderName(), item.getItemName());
            case Expo_Unbump_Item:
                return KDSXMLParserCommand.createItemNotification(syncCmd, strStationID, ip, mac, order.getOrderName(), item.getItemName());


            case Station_Modified_Item:
                return KDSXMLParserCommand.createItemModifiedNotification(strStationID, ip, mac, order.getOrderName(), item.createXml());
            case Schedule_Item_Ready_Qty_Changed:
                return KDSXMLParserCommand.createScheduleItemReadyQtyChangedNotification(strStationID, ip, mac, order.getOrderName(), item.createXml());
            case Queue_Ready:
            case Queue_Unready:
            case Queue_Pickup:
                return KDSXMLParserCommand.createOrderNotification(syncCmd, strStationID, ip, mac, order.getOrderName());
            case Station_Cook_Started:
            {
                return KDSXMLParserCommand.createOrderNotification(syncCmd, strStationID, ip, mac, order.getOrderName());
            }

            case Runner_show_category:
            {
                return KDSXMLParserCommand.createRunnerUpdateCategory(strStationID, ip, mac, order.getOrderName(),xmlData);
            }
            case Runner_start_cook_item:
            {
                return KDSXMLParserCommand.createRunnerStartCookItem(strStationID, ip, mac, order.getOrderName(),item.getItemName(), xmlData);
            }
            default:
                return "";
        }
    }

    static String createOrderTransferXml(String strStationID, String ip, String mac, KDSDataOrder order)
    {
        return KDSXMLParserCommand.createOrderTransferNotification(strStationID, ip, mac, order.createXml());
    }

    /**
     * Transfer Prep -> Transfer Expo
     * Send this xml to expo. Expo will delete this order's content.
     * @param strStationID
     * @param ip
     * @param mac
     * @param order
     * @return
     */
    static String createOrderTransferPrepExpoXml(String strStationID, String ip, String mac, KDSDataOrder order)
    {
        return KDSXMLParserCommand.createOrderTransferPrepExpoNotification(strStationID, ip, mac, order.createXml());
    }
}
