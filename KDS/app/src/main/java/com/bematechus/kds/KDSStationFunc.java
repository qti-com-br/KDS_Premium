package com.bematechus.kds;

import android.util.Log;

import com.bematechus.kdslib.BuildVer;
import com.bematechus.kdslib.KDSBase;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDBBase;
import com.bematechus.kdslib.KDSDataCondiment;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSPosNotificationFactory;
import com.bematechus.kdslib.KDSSocketInterface;
import com.bematechus.kdslib.KDSSocketTCPSideBase;
import com.bematechus.kdslib.KDSStationActived;
import com.bematechus.kdslib.KDSStationConnection;
import com.bematechus.kdslib.KDSStationDataBuffered;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSStationsConnection;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXMLParserCommand;
import com.bematechus.kdslib.ScheduleProcessOrder;
import com.bematechus.kdslib.SettingsBase;
import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2015/10/14 0014.
 */
public class KDSStationFunc {

    static String TAG = "StationFunc";
    /**
     * sync with mirror, backup stations ..
     * check if mirror, backup ...
     * @param syncMode
     * @param order
     * @param item
     */
    static public  void sync_with_backup(KDS kds, KDSXMLParserCommand.KDSCommand syncMode, KDSDataOrder order, KDSDataItem item )
    {
        if (kds.getStationsConnections().getRelations().getBackupStations().size() <=0)
            return; //for speed
        String strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, "");
        kds.getStationsConnections().writeToBackups(kds.getStationID(), strXml);

    }

    static public void sync_with_mirror(KDS kds, KDSXMLParserCommand.KDSCommand syncMode, KDSDataOrder order, KDSDataItem item )
    {
        if (kds.getStationsConnections().getRelations().getMirrorStations().size() <=0)
            return;//for speed
        String strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, "");
        kds.getStationsConnections().writeToMirrors(kds.getStationID(), strXml);

    }
    static boolean is_send_to_duplicated(KDSXMLParserCommand.KDSCommand syncMode)
    {

        switch (syncMode)
        {

            case Nothing:
            case Require_Station_Statistic_DB:
            case Require_Station_Daily_DB:
            case Require_Station_Configuration:
            case Broadcast_Station_Configuration:
            case Broadcast_Station_Active:
            case Broadcast_All_Configurations:
            case Station_Add_New_Order:
            case Station_Modify_Order:
            case Station_Modified_Item:
            case Station_Transfer_Order:
            case Station_Transfer_Order_ACK:
            case DBSupport_Sql:
            case DBCurrent_Sql:
            case DBSync_Broadcast_Current_Db_Updated:
            case DBSync_Broadcast_Station_Sql_Sync_Updated:
            case Broadcast_Ask_Active_Info:
            case DB_Ask_Status:
            case DB_Return_Status:
            case DB_Copy_Current_To_Support:
            case Station_Add_New_Park_Order:
            case ROUTER_ASK_DB_STATUS:
            case ROUTER_FEEDBACK_DB_STATUS:
            case ROUTER_DB_SQL:
            case ROUTER_ASK_DB_DATA:
            case ROUTER_UPDATE_CHANGES_FLAG:
            case ROUTER_SQL_SYNC:
            case Station_Update_Order:
            case Statistic_Ask_DB_Data:
            case DBStatistic_Sql:
            case Queue_Ready:
            case Queue_Unready:
            case Queue_Pickup:
            case Sync_Settings_Queue_Expo_Double_Bump:
                return true;

            case Station_Bump_Order:
            case Station_Unbump_Order:
            case Station_Cancel_Order:
            case Station_Bump_Item:
            case Station_Unbump_Item:
            case Station_Order_Parked:
            case Station_Order_Unpark:
            case Expo_Bump_Order:
            case Expo_Unbump_Order:
            case Expo_Bump_Item:
            case Expo_Unbump_Item:
            case Schedule_Item_Ready_Qty_Changed:
                return false;

        }
        return false;
    }
    static public void sync_with_stations(KDS kds,KDSXMLParserCommand.KDSCommand syncMode, KDSDataOrder order, KDSDataItem item, String xmlData )
    {
        sync_with_stations(kds, syncMode, order, item, xmlData, true);
        /*
        String strXml = "";//KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item);
        //writeToStations(m_stationsConnection.getExpStations(), strXml);
        if (kds.getStationsConnections().getRelations().getExpStations().size()>0) {
            if (strXml.isEmpty())
                strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
            kds.getStationsConnections().writeToExps(kds.getStationID(), strXml);
        }
        if (kds.getStationsConnections().getRelations().getMirrorStations().size()>0) {
            if (strXml.isEmpty())
                strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);//);
            kds.getStationsConnections().writeToMirrors(kds.getStationID(), strXml);
        }
        if (kds.getStationsConnections().getRelations().getBackupStations().size()>0) {
            if (strXml.isEmpty())
                strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
            kds.getStationsConnections().writeToBackups(kds.getStationID(), strXml);
        }
        if (kds.getStationsConnections().getRelations().getPrimaryStationsWhoUseMeAsMirror().size()>0) {
            if (strXml.isEmpty())
                strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
            kds.getStationsConnections().writeToPrimaryMirror(kds.getStationID(), strXml);
        }
        if (is_send_to_duplicated(syncMode)) {
            if (kds.getStationsConnections().getRelations().getDuplicatedStations().size() >0) {
                if (strXml.isEmpty())
                    strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
                kds.getStationsConnections().writeToDuplicated(kds.getStationID(), strXml);
            }
        }
        if (kds.getStationsConnections().getRelations().getQueueStations().size() >0) {
            if (strXml.isEmpty())
                strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
            kds.getStationsConnections().writeToQueue(kds.getStationID(), strXml);
        }
        if (kds.getStationsConnections().getRelations().getTTStations().size() >0) {
            if (strXml.isEmpty()) {
                //TimeDog td = new TimeDog();
                strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
                //td.debug_print_Duration("KDSXMLCommandFactory.sync_with_others");
            }
            kds.getStationsConnections().writeToTT(kds.getStationID(), strXml);
        }
        //if the backup station find its primary is offline, send data to primary's mirror.
        if (kds.getStationsConnections().isBackupOfOthers())
        {
            ArrayList<KDSStationIP> primaryBackups = kds.getAllActiveConnections().getRelations().getPrimaryStationsWhoUseMeAsBackup();
            if (strXml.isEmpty())
                strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
            //If my primary backup station is the primary mirror of others,
            //check if this primary station is offline. If so, write data to the slave mirror of primary.
            for (int i=0; i< primaryBackups.size();i ++)
                kds.getStationsConnections().writeToMirrorOfPrimaryBackup(kds.getStationID(),primaryBackups.get(i).getID(),  strXml);
        }
        else if (kds.getStationsConnections().isWorkLoadOfOthers())
        {

        }
        else if (kds.getStationsConnections().isDuplicatedOfOthers())
        {

        }
        else if (kds.getStationsConnections().isMirrorOfOthers())
        {

        }

*/

    }

    /**
     * send sync information to expeditor station
     * @param syncMode
     *      What type information
     * @param order
     *      Which order
     *      It can been null
     * @param item
     *      Which item.
     *      It can been null
     */
    static private void sync_with_exp(KDS kds, KDSXMLParserCommand.KDSCommand syncMode, KDSDataOrder order, KDSDataItem item )
    {
        if (kds.getStationsConnections().getRelations().getExpStations().size() <=0)
            return;//for speed
        String strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, "");
        //writeToStations(m_stationsConnection.getExpStations(), strXml);
        kds.getStationsConnections().writeToExps(kds.getStationID(),strXml);
    }






   static  public boolean orderCancel(KDS kds, KDSDBCurrent db,  KDSDataOrder order)
    {
        kds.getUsers().orderCancel(order);

        return true;

    }




    /**
     *
     * @param kdsuser
     * @param order
     * @param bCheckAddonTime
     * @param bAutoSyncWithOthers
     * @param bAutoSyncWithExpo
     *  It depend on bAutoSyncWithOthers.
     * @return
     *  the order added
     */
    static public  KDSDataOrder func_orderAdd(KDSUser kdsuser, KDSDataOrder order,String xmlData, boolean bCheckAddonTime, boolean bAutoSyncWithOthers, boolean bAutoSyncWithExpo, boolean bRefreshView)
    {
        KDSDataOrder orderReturn = null;
        //1. check if this order is existed in array.
        //
        if (order.getItems().getCount() <=0)
            return null;//orderReturn; //no item for me.
        order.setScreen(kdsuser.getUserID().ordinal());

        if (order.getItems().existedKDSStationChangedToBackupItem())
        {
            order.setFromPrimaryOfBackup(true);
        }
//        if (kdsuser.getKDS().getStationsConnections().getRelations().isBackupStation())
//        {
//            order.setFromPrimaryOfBackup(true);
//            kdsuser.getCurrentDB().orderSetAllFromPrimaryOfBackup(true);
//        }

        KDSDataOrder orderExisted = kdsuser.getOrders().getOrderByName(order.getOrderName());
        //kpp1-393
        boolean bInParkedOrdersList = false;
        if (orderExisted == null) {
            orderExisted = kdsuser.getParkedOrders().getOrderByName(order.getOrderName());
            if (orderExisted != null) bInParkedOrdersList = true;
        }
        //TimeDog t = new TimeDog();
        if (orderExisted == null) {
            //kpp1-393, support parked order
            if (order.getParked()==KDSConst.INT_TRUE) {
                //Log.d(TAG, "Add to parked list");
                kdsuser.getParkedOrders().addComponent(order);
            }//
            else {
                //Log.d(TAG, "Add to orders list");
                kdsuser.getOrders().addComponent(order);
            }
            //t.debug_print_Duration("func-orderAdd1");
            //Log.d(TAG, "Add to database");
            kdsuser.getCurrentDB().orderAdd(order);
            //t.debug_print_Duration("func-orderAdd2");
            if (bRefreshView)
                kdsuser.refreshView();
            //t.debug_print_Duration("func-orderAdd3");
            orderReturn = order;

        }
        else
        {
            //change all new items's orderid to existed order
            order.getItems().setOrderGUID (orderExisted.getGUID());
            if (orderExisted.isTimeForAddon() && bCheckAddonTime)
            {
                orderExisted.appendAddon(order);
            }
            else
            {
                orderExisted.appendItems(order);
            }
            if (order.getItems().getCount() >0)
                kdsuser.getCurrentDB().orderAppendAddon(orderExisted, order);
            orderReturn = orderExisted;
            //kpp1-393
            if (order.getParked() == KDSConst.INT_TRUE)
            {//it is a parked order.
                if (!bInParkedOrdersList)
                {//existed order is not in orders list, move it to parked list.
                    kdsuser.getOrders().removeComponent(orderExisted);
                    kdsuser.getParkedOrders().addComponent(orderExisted);
                }
            }
            else
            {//it is a normal order
                if (bInParkedOrdersList)
                {//move to orders list.
                    kdsuser.getParkedOrders().removeComponent(orderExisted);
                    kdsuser.getOrders().addComponent(orderExisted);
                }
            }
        }
        //TimeDog td = new TimeDog();
        //20190403 IMPORTANT
        //remove the station_add_new_order sync command.
        //This will cause the expo very busy, and this order should been send to expo directly.
        //So, please make sure order was send to each station when kds get orders from SMB.
        //And, if the kdsrouter existed, this is OK.


        if (bAutoSyncWithOthers) {
            if (order.getItems().getCount() > 0) {
                sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Station_Add_New_Order, order, null, xmlData, bAutoSyncWithExpo);
            }
        }

        //td.debug_print_Duration("sync_with_stations");
        //t.debug_print_Duration("func-orderAdd4");
        if (order.getItems().getCount() >0)
            return orderReturn;
        else
            return null;
    }


    /**
     * 2.0.8
     * Append a new item
     * @param order
     *  Which order
     * @param originalItem
     *  This item was changed. Its qty changed.
     *      In lineItems mode, we need to show change qty item with a new line.
     *
     * @return
     *  The item.
     */
    static public KDSDataItem orderAddChangedItemEntry(KDSUser kdsuser,KDSDataOrder order, KDSDataItem originalItem)
    {
        KDSDataItem item = new KDSDataItem();
        originalItem.copyTo(item);
        item.createNewGuid();
        long n = (new Date()).getTime() - order.getStartTime().getTime();
        int nn = (int)(n/1000);
        if ((n % 1000)>0) nn ++;

        item.setTimerDelay( nn);


        item.updateCondimentsModifersMessagesParentGuid();

        //item.setGUID(KDSDataItem.TAG_QTY_CHANGED_APPEND_ITEM + item.getGUID());
        item.setParentItemGuid(originalItem.getGUID());
        item.setQty(item.getChangedQty());
        item.setChangedQty(0);

        orderRemoveQtyChangeItemEntry(kdsuser, order, originalItem);

        //kdsuser.getCurrentDB().removeLineItemOfQtyChange(originalItem.getGUID());
        kdsuser.getCurrentDB().itemAdd(item);

        //order.getItems().removeLineItemOfQtyChanged(originalItem.getGUID());
        order.getItems().addComponent(item);

        return item;
    }

    static public void orderRemoveQtyChangeItemEntry(KDSUser kdsuser,KDSDataOrder order, KDSDataItem originalItem)
    {
        kdsuser.getCurrentDB().removeLineItemOfQtyChange(originalItem.getGUID());

        order.getItems().removeLineItemOfQtyChanged(originalItem.getGUID());
    }
    /**
     * update this order by xml command file.
     *
     * @param kdsuser
     * @param orderReceived
     * @param bAutoSyncWithOthers
     * @return
     */
    static public  KDSDataOrder orderUpdate(KDSUser kdsuser, KDSDataOrder orderReceived,boolean bAutoSyncWithOthers)
    {
        KDSDataOrder orderReturn = null;
        KDSDataOrder orderExisted = kdsuser.getOrders().getOrderByName(orderReceived.getOrderName());

        if (orderExisted == null) return null;
        //1. check if this order is existed in array.
        //
        if (orderReceived.getItems().getCount() <=0) {
            return null;
//            orderExisted.setAllItemsVoidByXml(true);
//            return orderExisted; //
        }
        orderReceived.setScreen(kdsuser.getUserID().ordinal());

        ArrayList<String> arDone = new ArrayList<>();

        //check existed items first.
        int ncount = orderReceived.getItems().getCount();
        for (int i=0; i< ncount; i++)
        {
            if (orderReceived.getItems().getCount() <=0) break;
            KDSDataItem receivedItem = orderReceived.getItems().getItem(i);
            if (existedInStringArray(arDone, receivedItem.getDescription()))
                continue;

            arDone.add(receivedItem.getDescription());

            ArrayList<KDSDataItem> receivedItems = orderReceived.getItems().findSameItemByDescription(receivedItem);

            ArrayList<KDSDataItem> existedItems = orderExisted.getItems().findSameItemByDescription(receivedItem);
            if (existedItems.size()<= 0)
            {
                KDSDataItem duplicatedItem = new KDSDataItem();
                receivedItem.copyTo(duplicatedItem);
                duplicatedItem.setOrderGUID(orderExisted.getGUID());
                orderExisted.getItems().addComponent(duplicatedItem);
                kdsuser.getCurrentDB().itemAdd(receivedItem);
            }
            else
            {//modify the qty
                float existedQty = getItemsQty(existedItems);
                float receivedQty = getItemsQty(receivedItems);
                if (receivedQty != existedQty)
                {
                    float nToQty = receivedQty;//receivedItem.getQty();
                    existedItems.get(0).setChangedQty(nToQty - existedItems.get(0).getQty());
                    kdsuser.getCurrentDB().itemUpdate(existedItems.get(0));

                    //for lineitems mode, qty changed item
                    if (existedItems.get(0).getChangedQty() >0)
                    {
                        if (kdsuser.getKDS().getSettings().isLineItemsEnabled())
                            orderAddChangedItemEntry(kdsuser, orderExisted, existedItems.get(0));

                    }
                    else
                    {
                        orderRemoveQtyChangeItemEntry(kdsuser, orderExisted, existedItems.get(0));
                    }

                    for (int j=1; j< existedItems.size(); j++)
                    {
                        existedItems.get(j).setChangedQty(0 - existedItems.get(j).getQty());
                        kdsuser.getCurrentDB().itemUpdate(existedItems.get(j));

                        //for lineitems mode, qty changed item
                        if (existedItems.get(j).getChangedQty() >0)
                        {
                            if (kdsuser.getKDS().getSettings().isLineItemsEnabled())
                                orderAddChangedItemEntry(kdsuser, orderExisted, existedItems.get(j));
                        }
                        else
                        {
                            orderRemoveQtyChangeItemEntry(kdsuser, orderExisted, existedItems.get(j));
                        }

                    }
                    //existedItem.setChangedQty(receivedItem.getQty() -existedItem.getQty() );
                    //kdsuser.getCurrentDB().itemUpdate(existedItem);
                }
                else
                {
                    for (int j=0; j< existedItems.size(); j++)
                    {
                        existedItems.get(j).setChangedQty(0 );
                        kdsuser.getCurrentDB().itemUpdate(existedItems.get(j));
                    }
                }
            }

        }
        //check removed items
        ncount = orderExisted.getItems().getCount();
        for (int i=0; i< ncount; i++)
        {

            KDSDataItem existedItem = orderExisted.getItems().getItem(i);//.findSameItem(receivedItem);
            ArrayList<KDSDataItem> receivedItems = orderReceived.getItems().findSameItemByDescription(existedItem);

            if (receivedItems.size()<=0)
            {
                existedItem.setDeleteByRemoteCommand(true);
                kdsuser.getCurrentDB().itemSetRemovedByXmlCommand(existedItem);
            }
        }

        if (bAutoSyncWithOthers) {
            // if (orderExisted.getItems().getCount() >0)
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Station_Update_Order, orderReceived, null, "");
        }
        return orderExisted;
//        if (order.getItems().getCount() >0)
//            return orderReturn;
//        else
//            return null;
    }

    static public boolean existedInStringArray(ArrayList<String> ar, String str)
    {
        for (int i=0; i<ar.size(); i++)
        {
            if (ar.get(i).equals(str))
                return true;
        }
        return false;
    }

    static public float getItemsQty(ArrayList<KDSDataItem> arItems)
    {
        float flt = 0;
        for (int i=0; i< arItems.size(); i++)
        {
            flt += arItems.get(i).getQty();
        }
        return flt;
    }

    static public boolean orderPark(KDSUser kdsuser, KDSDataOrder order)
    {
        kdsuser.getOrders().removeComponent(order);
        kdsuser.getParkedOrders().addComponent(order);
        kdsuser.getCurrentDB().orderSetParked(order.getGUID(), true);
        order.setParked(true);
        return true;

    }
    static public KDSDataOrder orderUnpark(KDSUser kdsuser, String orderGuid)
    {
        KDSDataOrder order =  kdsuser.getParkedOrders().getOrderByGUID(orderGuid);
        if (order == null) return order;
        kdsuser.getParkedOrders().removeComponent(order);
        kdsuser.getOrders().addComponent(order);

        kdsuser.getCurrentDB().orderSetParked(order.getGUID(), false);
        order.setParked(false);
        return order;
    }

    static public void itemCondimentsModify(KDSUser kdsuser, KDSDataOrder orderExisted, KDSDataOrder orderReceived, KDSDataItem itemExisted, KDSDataItem itemReceived)
    {
        int ncondiments = itemReceived.getCondiments().getCount();
        for (int j=0; j< ncondiments; j++)
        {
            KDSDataCondiment condiment = itemReceived.getCondiments().getCondiment(j);
            if (condiment.getTransType() == KDSDataOrder.TRANSTYPE_MODIFY)
            {
                KDSDataCondiment condimentExisted = itemExisted.getCondiments().getCondimentByName(condiment.getCondimentName());
                if (condimentExisted == null) continue;
                if (condimentExisted.modifyCondiment(condiment))
                    kdsuser.getCurrentDB().condimentUpdate(condimentExisted);
            }
            else if (condiment.getTransType() == KDSDataOrder.TRANSTYPE_ADD)
            {
                //check if existed same condiment ID
                if (itemExisted.getCondiments().getCondimentByName(condiment.getCondimentName())!= null)
                    continue; //kpp1-414, don't add same id condiment.

                itemExisted.getCondiments().addComponent(condiment);
                condiment.setItemGUID(itemExisted.getGUID());
                kdsuser.getCurrentDB().condimentAdd(condiment);
            }
            else if (condiment.getTransType() == KDSDataOrder.TRANSTYPE_DELETE)
            {
                KDSDataCondiment condimentExisted =  itemExisted.getCondiments().getCondimentByName(condiment.getCondimentName());
                if (condimentExisted == null) continue;
                itemExisted.getCondiments().removeComponent(condimentExisted);
                kdsuser.getCurrentDB().condimentDelete(condimentExisted);

            }

        }
    }
    /**
     * change item information.
     * history:
     *  20171206: add modify flag in item, for void item.
     * @param kdsuser
     * @param orderExisted
     * @param orderReceived
     */
    static public void itemsModify(KDSUser kdsuser, KDSDataOrder orderExisted, KDSDataOrder orderReceived, boolean bSyncWithOthers)
    {
        int ncount = orderReceived.getItems().getCount();
        int ngroupID = -1;
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item =  orderReceived.getItems().getItem(i);
            if (item.getTransType() == KDSDataOrder.TRANSTYPE_MODIFY)
            {
                KDSDataItem itemExisted = null;
                if (item.getItemName().isEmpty()) //kpp1-409
                    itemExisted = orderExisted.getItems().getItemByGUID(item.getGUID());
                else
                    itemExisted = orderExisted.getItems().getItemByName(item.getItemName());
                if (itemExisted == null)
                    continue;
                float nOldQty = itemExisted.getShowingQty();
                if (itemExisted.modifyItem(item)) {
                    kdsuser.getCurrentDB().itemUpdate(itemExisted);
                    if (nOldQty != itemExisted.getShowingQty())
                    {
                        kdsuser.getKDS().firePOSNotification_OrderItem(orderExisted, itemExisted, KDSPosNotificationFactory.BumpUnbumpType.Item_qty_changed);
                        if (itemExisted.getChangedQty() >0) {
                            if (kdsuser.getKDS().getSettings().isLineItemsEnabled()) {
                                orderAddChangedItemEntry(kdsuser, orderExisted, itemExisted);
                            }
                        }
                        else
                        {
                            orderRemoveQtyChangeItemEntry(kdsuser, orderExisted, itemExisted);
                        }
                    }

                }
                //check condiments
                itemCondimentsModify(kdsuser, orderExisted, orderReceived, itemExisted,item );


//                int ncondiments = item.getCondiments().getCount();
//                for (int j=0; j< ncondiments; j++)
//                {
//                    KDSDataCondiment condiment = item.getCondiments().getCondiment(j);
//                    if (condiment.getTransType() == KDSDataOrder.TRANSTYPE_MODIFY)
//                    {
//                        KDSDataCondiment condimentExisted = itemExisted.getCondiments().getCondimentByName(condiment.getCondimentName());
//                        if (condimentExisted == null) continue;
//                        if (condimentExisted.modifyCondiment(condiment))
//                            kdsuser.getCurrentDB().condimentUpdate(condimentExisted);
//                    }
//                }
            }
            else if (item.getTransType() == KDSDataOrder.TRANSTYPE_ADD)
            {
                orderReceived.getItems().setOrderGUID(orderExisted.getGUID());
                KDSDataItem itemExisted =orderExisted.getItems().getItemByName(item.getItemName()) ;
                if (itemExisted == null ) {
                    boolean bAddedItem = false;
                    if (orderExisted.isTimeForAddon()) {
                        if (ngroupID < 0) ngroupID = orderExisted.getItems().createNewAddonGroup();
                        bAddedItem = orderExisted.appendAddonNoCheckingSame(ngroupID, item);
                    } else {
                        bAddedItem = orderExisted.appendItemNoCheckingSame(item);
                    }
                    if (bAddedItem)
                        kdsuser.getCurrentDB().orderAppendAddon(orderExisted, item);
                }
                else
                {//if existed same name item, just add its qty
                    itemExisted.setQty(itemExisted.getQty() + item.getQty());
                    kdsuser.getCurrentDB().itemUpdate(itemExisted);
                    kdsuser.getKDS().firePOSNotification_OrderItem(orderExisted, itemExisted, KDSPosNotificationFactory.BumpUnbumpType.Item_qty_changed);
                }
            }
            else if (item.getTransType() == KDSDataOrder.TRANSTYPE_DELETE)
            {
                KDSDataItem itemExisted = null;
                if (item.getItemName().isEmpty()) //kpp1-409
                    itemExisted = orderExisted.getItems().getItemByGUID(item.getGUID());
                else
                    itemExisted = orderExisted.getItems().getItemByName(item.getItemName());
                //KDSDataItem itemExisted = orderExisted.getItems().getItemByName(item.getItemName());
                if (itemExisted == null)
                    continue;

                //keep removed item, and changed its qty

                itemExisted.setChangedQty((-1) * itemExisted.getQty());

                //itemExisted.setDeleteByRemoteCommand(true); //2.0.46, use qty=0 to remove item, see kdslayoutcell

                kdsuser.getCurrentDB().itemUpdate(itemExisted);
                kdsuser.getKDS().firePOSNotification_OrderItem(orderExisted, itemExisted, KDSPosNotificationFactory.BumpUnbumpType.Item_qty_changed);
                //orderExisted.getItems().removeComponent(itemExisted);
                //kdsuser.getCurrentDB().deleteItem(itemExisted.getOrderGUID());
            }

        }
        kdsuser.refreshView();
        if (bSyncWithOthers)
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Station_Modify_Order, orderReceived, null, "");
    }

    /**
     * modify my order
     * @param order
     *  The order received
     * @param bSyncWithOthers
     *  for modify/delete transtype, these type xml send to every station,
     *      Don't sync again, otherwise it will cause double operations.
     */
    static public void orderInfoModify(KDSUser kdsuser, KDSDataOrder order, boolean bSyncWithOthers)
    {
        //kpp1-409, use the guid to load order.
        KDSDataOrder orderExisted = null;
        if (order.getGUID().equals(KDSConst.ORDER_GUID_FOR_API_ITEM_CHANGES))
        {//use item guid to find order. This just happen in api order and void/update event.
            if (order.getItems().getCount()<=0) return;
            String itemGuid = order.getItems().getItem(0).getGUID();
            String orderguid = kdsuser.getOrders().getCurrentDB().itemGetOrderGuid(itemGuid);
            orderExisted = kdsuser.getOrders().getOrderByGUID(orderguid);
        }
        else {
            if (order.getOrderName().isEmpty())
                orderExisted = kdsuser.getOrders().getOrderByGUID(order.getGUID());
            else
                orderExisted = kdsuser.getOrders().getOrderByName(order.getOrderName());
        }
        //kpp1-393
        boolean bInParkedList = false;
        if (orderExisted == null)
        {
            orderExisted = kdsuser.getParkedOrders().getOrderByName(order.getOrderName());
            if (orderExisted == null) return;
            bInParkedList = true;
        }

        if (orderExisted == null) {

            return;
        }
        //kp-64 When adding items to an order- no catdelay
        kdsuser.getCurrentDB().prep_change_modify_order_guid_to_existed_guid(order.getGUID(), orderExisted);


        int oldOrderStatus = orderExisted.getStatus();

        boolean bmodified = orderExisted.modify(order);
        if (bmodified)
        {
            //kdsuser.getCurrentDB().orderInfoModify(orderExisted);
            kdsuser.getCurrentDB().orderInfoModify(order);
            if (oldOrderStatus != orderExisted.getStatus())
                kdsuser.getKDS().firePOSNotification_OrderItem(orderExisted, null, KDSPosNotificationFactory.BumpUnbumpType.Order_status_changed);
        }
        itemsModify(kdsuser, orderExisted, order,bSyncWithOthers);

        //kpp1-393
        if (order.getParked() == KDSConst.INT_TRUE)
        {//it is a parked order.
            if (!bInParkedList)
            {//existed order is not in orders list, move it to parked list.
                kdsuser.getOrders().removeComponent(orderExisted);
                kdsuser.getParkedOrders().addComponent(orderExisted);
                kdsuser.getCurrentDB().orderSetParked(orderExisted.getGUID(), true);
            }
        }
        else
        {//it is a normal order
            if (bInParkedList)
            {//move to orders list.
                kdsuser.getParkedOrders().removeComponent(orderExisted);
                kdsuser.getOrders().addComponent(orderExisted);
                kdsuser.getCurrentDB().orderSetParked(orderExisted.getGUID(), false);
            }
        }
    }

    static  public void itemModify(KDSUser kdsuser, KDSDataItem itemNew, boolean bAutoSync)
    {
        KDSDataOrder order = kdsuser.getOrders().getOrderByGUID(itemNew.getOrderGUID());
        if (order == null)
            return;
        KDSDataItem item = order.getItems().getItemByName(itemNew.getItemName());
        if (item == null) return;
        item.modifyItem(itemNew);

        //item.setLocalBumped(true);

        kdsuser.getCurrentDB().itemModify(itemNew);

        kdsuser.refreshView();

        if (bAutoSync)
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Station_Modified_Item, order, item, "");
    }

    static  public void schedule_process_item_ready_qty_changed(KDSUser kdsuser, KDSDataItem itemNew, boolean bAutoSync)
    {
        KDSDataOrder order = kdsuser.getOrders().getOrderByGUID(itemNew.getOrderGUID());
        if (order == null)
            return;
        KDSDataItem item = order.getItems().getItemByName(itemNew.getItemName());
        if (item == null) return;
        item.setScheduleProcessReadyQty(itemNew.getScheduleProcessReadyQty());

        if (order instanceof ScheduleProcessOrder)
            kdsuser.getCurrentDB().schedule_process_set_item_ready_qty((ScheduleProcessOrder) order);

        kdsuser.refreshView();

        if (bAutoSync)
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Schedule_Item_Ready_Qty_Changed, order, item, "");
    }

    static  public boolean itemBump(KDSUser kdsuser, String orderGuid, String itemGuid)
    {
        KDSDataOrder order = kdsuser.getOrders().getOrderByGUID(orderGuid);
        if (order == null)
            return false;
        KDSDataItem item = order.getItems().getItemByGUID(itemGuid);
        if (item == null) return false;
        if (item.getLocalBumped()) return false;
        //toggle it
        //item.setLocalBumped(true);
        item.setLocalBumped(true);//!item.getLocalBumped());
        kdsuser.getCurrentDB().itemSetLocalBumped(item);
        //2.0.14
        if (kdsuser.getOrders().getMoveFinishedToFront())
        {
            if (order.isAllItemsFinished())
                kdsuser.getOrders().sortOrders();
        }
        kdsuser.refreshView();
//        if (kdsuser.getKDS().isExpeditorStation() ||
//                kdsuser.getKDS().isTrackerStation() ||
//                kdsuser.getKDS().isQueueExpo() ||
//                kdsuser.getKDS().isRunnerStation())
        if (isExpoTypeStation(kdsuser.getKDS().getStationFunction()))
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Expo_Bump_Item, order, item, "");
        else
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Station_Bump_Item, order, item, "");


        return true;
    }

    static  public void itemUnbump(KDSUser kdsuser,String orderGuid, String itemGuid)
    {
        KDSDataOrder order = kdsuser.getOrders().getOrderByGUID(orderGuid);
        if (order == null)
            orderUnbump(kdsuser, orderGuid);
        order = kdsuser.getOrders().getOrderByGUID(orderGuid);
        if (order == null)
            return;
        KDSDataItem item = order.getItems().getItemByGUID(itemGuid);
        if (item == null) return;
        item.setLocalBumped(false);

        kdsuser.getCurrentDB().itemSetLocalBumped(item);

        if (kdsuser.getOrders().getMoveFinishedToFront())
        {
            kdsuser.getOrders().sortOrders();
        }
        kdsuser.refreshView();

//        if (kdsuser.getKDS().isExpeditorStation() ||
//                kdsuser.getKDS().isQueueExpo() ||
//                kdsuser.getKDS().isRunnerStation())
        if (isExpoTypeStation(kdsuser.getKDS().getStationFunction()))
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Expo_Unbump_Item, order, item, "");
        else
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Station_Unbump_Item, order, item, "");
    }

    /**
     * The operation from gui
     * @param kdsuser
     * @param orderGuid
     */
    static public void orderBump(KDSUser kdsuser, String orderGuid, boolean bRefreshView)
    {
        //TimeDog td = new TimeDog();
        KDSDataOrder order = kdsuser.getOrders().getOrderByGUID(orderGuid);
        if (order == null)
            return;
        kdsuser.getOrders().removeComponent(order);
        kdsuser.getCurrentDB().orderSetBumped(order.getGUID(), true);

        //update the statistic database
        if (order instanceof ScheduleProcessOrder)
        {
            kdsuser.getCurrentDB().schedule_order_finished(order.getGUID());
        }
        else {
             kdsuser.getStatisticDB().orderAdd(order);
        }
        if (bRefreshView)
            kdsuser.refreshView();
        //td.debug_print_Duration("func-1");
        //TimeDog td = new TimeDog();
//        if (kdsuser.getKDS().isExpeditorStation()||
//                kdsuser.getKDS().isQueueExpo())
        if (isExpoTypeStation(kdsuser.getKDS().getStationFunction()))
        {
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Expo_Bump_Order, order, null, "");
            //kpp1-202
            sync_with_stations_use_me_as_expo(kdsuser.getKDS(),KDSXMLParserCommand.KDSCommand.Expo_Bump_Order, order, null, "" );
        }
        else
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Station_Bump_Order, order, null, "");
        //td.debug_print_Duration("orderBump-->sync_with_stations");
        //td.debug_print_Duration("func-2");
        //20180314
        //td.reset();
        //TimeDog td = new TimeDog();
        if (bRefreshView)
            kdsuser.getCurrentDB().clearExpiredBumpedOrders( kdsuser.getKDS().getSettings().getBumpReservedCount());
        //td.debug_print_Duration("clearExpiredBumpedOrders");
    }

    /**
     * The operation from gui
     * @param kdsuser
     * @param orderGuid
     */
    static public void orderParkedBump(KDSUser kdsuser, String orderGuid)
    {
        KDSDataOrder order = kdsuser.getParkedOrders().getOrderByGUID(orderGuid);
        if (order == null)
            return;
        kdsuser.getParkedOrders().removeComponent(order);
        kdsuser.getCurrentDB().orderDelete(order.getGUID());
        //update the statistic database

        //kdsuser.getStatisticDB().orderAdd(order);
        kdsuser.refreshView();

    }

    static public void orderCookStarted(KDSUser kdsuser, String orderGuid)
    {
        KDSDataOrder order = kdsuser.getOrders().getOrderByGUID(orderGuid);
        if (order == null)
            return;
       order.setCookState(KDSDataOrder.CookState.Started);
        kdsuser.getCurrentDB().orderSetCookState(orderGuid, KDSDataOrder.CookState.Started);
        //update the statistic database

        kdsuser.refreshView();

        sync_with_exp(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Station_Cook_Started, order, null);
    }

    static public void orderCookStartedByName(KDSUser kdsuser, String orderName)
    {
        KDSDataOrder order = kdsuser.getOrders().getOrderByName(orderName);
        if (order == null) return;
        orderCookStarted(kdsuser, order.getGUID());
    }
    static public void orderTrackerBump(KDSUser kdsuser, String orderName)
    {
        KDSDataOrder order = kdsuser.getOrders().getOrderByName(orderName);
        if (order == null)
            return;
        kdsuser.getOrders().removeComponent(order);
        kdsuser.getCurrentDB().orderSetBumped(order.getGUID(), true);
        //update the statistic database
        if (order instanceof ScheduleProcessOrder)
        {
            kdsuser.getCurrentDB().schedule_order_finished(order.getGUID());
        }
        else {

            kdsuser.getStatisticDB().orderAdd(order);
        }

        kdsuser.refreshView();


//        if (kdsuser.getKDS().isExpeditorStation() ||
//                kdsuser.getKDS().isQueueExpo())
        if (isExpoTypeStation(kdsuser.getKDS().getStationFunction()))
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Expo_Bump_Order, order, null, "");
        else
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Station_Bump_Order, order, null, "");
    }

    static public boolean orderUnbump(KDSUser kdsuser, String orderGuid)
    {

        kdsuser.getCurrentDB().orderSetBumped(orderGuid, false);
        KDSDataOrder order =  kdsuser.getCurrentDB().orderGet(orderGuid);
        if (order == null)
            return false;
        order.setAutoBumpStartCountTime(new Date());

        kdsuser.getOrders().addComponent(order);
        //update the statistic database
        kdsuser.getStatisticDB().orderDelete(orderGuid);
        //kdsuser.refreshView();
//        if (kdsuser.getKDS().isExpeditorStation() ||
//                kdsuser.getKDS().isQueueExpo())
        if (isExpoTypeStation(kdsuser.getKDS().getStationFunction()))
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Expo_Unbump_Order, order, null, "");
        else
            sync_with_stations(kdsuser.getKDS(), KDSXMLParserCommand.KDSCommand.Station_Unbump_Order, order, null, "");
        return true;
    }

    public enum TransferingStatus {
        Success,
        Connecting,
        Connected_Sending,
        Failed,
        Invalid_Order,
        Error_Station,
    }


    static public TransferingStatus itemTransfer(KDSUser kdsuser, KDSDataOrder orderWithTransferingItem, String toStationID, int toScreen)
    {
        KDSDataOrder orderLocal = kdsuser.getOrders().getOrderByGUID(orderWithTransferingItem.getGUID());
        if (orderLocal == null)
            return TransferingStatus.Invalid_Order;

        if (toStationID.isEmpty())
            return TransferingStatus.Error_Station;
        String itemGuid = orderWithTransferingItem.getItems().getItem(0).getGUID();
        //KDSTCPStation station = this.findIPConnectionByID(toStationID);
        KDSStationConnection station = kdsuser.getKDS().getStationsConnections().findConnectionByID(toStationID);


        String strDone = "The item [" + orderWithTransferingItem.getItems().getItem(0).getDescription();
        strDone += "] was send to "+toStationID;
//        KDSDataOrder transferOrder = new KDSDataOrder();
//        order.copyTo(transferOrder);
//        transferOrder.setScreen(toScreen);
        orderWithTransferingItem.setScreen(toScreen);
        //transferOrder.setAllItemsToScreen(toStationID, toScreen);

        String strXml = KDSXMLCommandFactory.createOrderTransferXml(kdsuser.getKDS().getStationID(), kdsuser.getKDS().getLocalIpAddress(), "", orderWithTransferingItem);
        KDSStationActived activeStation = kdsuser.getKDS().getStationsConnections().findActivedStationByID(toStationID);
        if (activeStation == null)
            return TransferingStatus.Error_Station;
        kdsuser.getKDS().getStationsConnections().writeDataToStationOrItsSlave(activeStation, strXml);

//        if (station == null) {
//            KDSStationActived activeStation = kdsuser.getKDS().getStationsConnections().findActivedStationByID(toStationID);
//            if (activeStation == null)
//                return TransferingStatus.Error_Station;
//            KDSStationConnection connecting = kdsuser.getKDS().getStationsConnections().connectToStation(activeStation);
//            if (connecting.getSock().isConnected()) {
//                if (!connecting.getSock().writeXmlTextCommand(strXml))
//                    return TransferingStatus.Failed;
//
//            }
//            else
//            {
//                //add the message after send
//
//                kdsuser.getKDS().getStationsConnections().getNoConnectionBuffer().add(toStationID,
//                                                                                    strXml,
//                                                                                    orderWithTransferingItem.getGUID(),
//                                                                                    itemGuid,orderWithTransferingItem.getItems().getItem(0).getDescription(),
//                                                                                    KDSStationsConnection.MAX_BACKUP_DATA_COUNT);
//                //KDSStationDataBuffered data = connecting.addBufferedData(strXml, strDone);
////                KDSStationDataBuffered data = kdsuser.getKDS().getStationsConnections().getNoConnectionBuffer().add(toStationID, strXml, KDSStationsConnection.MAX_BACKUP_DATA_COUNT);
////                if (data != null) {
////                    data.setOrderGuid(orderWithTransferingItem.getGUID());
////                    data.setItemGuid(itemGuid);
////                    data.setDescription(orderWithTransferingItem.getItems().getItem(0).getDescription());
////                }
//
//            }
//
//            return TransferingStatus.Connecting;
//        }
//        if (station.getSock() == null)
//            return TransferingStatus.Failed;
//        if (!station.getSock().isConnected())
//            return TransferingStatus.Connecting;

//        if (!station.getSock().writeXmlTextCommand(strXml))
//            return TransferingStatus.Failed;




        int n =  orderLocal.getItems().getItemIndexByGUID(itemGuid);
        orderLocal.getItems().removeComponent(n);

        //kdsuser.getOrders().get.removeComponent(order);
        kdsuser.getCurrentDB().deleteItem(itemGuid);//.orderDelete(order.getGUID());
        kdsuser.refreshView();
        kdsuser.getKDS().showMessage(strDone);
        return TransferingStatus.Success;
    }
   static public TransferingStatus orderTransfer(KDSUser kdsuser, String orderGuid, String toStationID, int toScreen)
    {
        KDSDataOrder order = kdsuser.getOrders().getOrderByGUID(orderGuid);
        if (order == null)
            return TransferingStatus.Invalid_Order;

        if (toStationID.isEmpty())
            return TransferingStatus.Error_Station;
        //KDSTCPStation station = this.findIPConnectionByID(toStationID);
        //KDSStationConnection station = kdsuser.getKDS().getStationsConnections().findConnectionByID(toStationID);

        String strDone = "The order [" + order.getOrderName();
        strDone += "] was send to "+toStationID;
        KDSDataOrder transferOrder = new KDSDataOrder();
        order.copyTo(transferOrder);
        transferOrder.setScreen(toScreen);
        Log.i(TAG, "start transfer");
        //transferOrder.setAllItemsToScreen(toStationID, toScreen);

        String strXml = KDSXMLCommandFactory.createOrderTransferXml(kdsuser.getKDS().getStationID(), kdsuser.getKDS().getLocalIpAddress(), "", transferOrder);
        KDSStationActived activeStation = kdsuser.getKDS().getStationsConnections().findActivedStationByID(toStationID);
        if (activeStation == null)
            return TransferingStatus.Error_Station;
        //kdsuser.getKDS().getStationsConnections().writeDataToStationOrItsSlave(activeStation, strXml);
        //kp-116, Transfer Prep -> Transfer Expo
        if (kdsuser.getKDS().getSettings().getBoolean(KDSSettings.ID.Transfer_prep_expo))
        {//we need to tell my expo this order was transferred.
            String tranferedXml = KDSXMLCommandFactory.createOrderTransferPrepExpoXml(kdsuser.getKDS().getStationID(),
                                                                                        kdsuser.getKDS().getLocalIpAddress(),
                                                                                        "", transferOrder);
            kdsuser.getKDS().getStationsConnections().writeToExps(kdsuser.getKDS().getStationID(), tranferedXml);

        }
        kdsuser.getKDS().getStationsConnections().writeDataToStationOrItsSlave(activeStation, strXml);
//        if (station == null) {
//            KDSStationActived activeStation = kdsuser.getKDS().getStationsConnections().findActivedStationByID(toStationID);
//            if (activeStation == null)
//                return TransferingStatus.Error_Station;

//            KDSStationConnection connecting = kdsuser.getKDS().getStationsConnections().connectToStation(activeStation);
//            if (connecting.getSock().isConnected()) {
//                if (!connecting.getSock().writeXmlTextCommand(strXml))
//                    return TransferingStatus.Failed;
//
//            }
//            else
//            {
//                //add the message after send
//                kdsuser.getKDS().getStationsConnections().getNoConnectionBuffer().add(activeStation.getID(), strXml,orderGuid, "", "", KDSStationsConnection.MAX_BACKUP_DATA_COUNT);
////                KDSStationDataBuffered data = kdsuser.getKDS().getStationsConnections().getNoConnectionBuffer().add(activeStation.getID(), strXml, KDSStationsConnection.MAX_BACKUP_DATA_COUNT);
////                //KDSStationDataBuffered data = connecting.addBufferedData(strXml, strDone);
////                if (data != null)
////                    data.setOrderGuid(orderGuid);
//
//            }
//
//            return TransferingStatus.Connecting;
//        }
//        if (station.getSock() == null)
//            return TransferingStatus.Failed;
//        if (!station.getSock().isConnected())
//            return TransferingStatus.Connecting;
//
//        if (!station.getSock().writeXmlTextCommand(strXml))
//            return TransferingStatus.Failed;


        kdsuser.getOrders().removeComponent(order);
        kdsuser.getCurrentDB().orderDelete(order.getGUID());
        kdsuser.refreshView();
        kdsuser.getKDS().showMessage(strDone);
        return TransferingStatus.Success;

        //sync_with_exp(KDSXMLCommandFactory.SyncInfo.Order_Transfer, .Order_Bumped, order, null);
    }

    /**
     *
     * @param kds
     * @param command
     * @param strOrinalData
     * @return
     *  order guid
     */
    static public String doSyncCommandItemBumped(KDS kds, KDSXMLParserCommand command, String strOrinalData, ArrayList<KDSDataItem> arChangedItems)
    {
        switch (kds.getStationFunction())
        {

            case Prep:
                return KDSStationNormal.normal_sync_item_bumped(kds, command, arChangedItems);

            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                return KDSStationExpeditor.exp_sync_item_bumped(kds, command, arChangedItems);

            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
        return "";
    }

    static public void doSyncCommandScheduleItemQtyChanged(KDS kds, KDSXMLParserCommand command, String strOrinalData)
    {
        switch (kds.getStationFunction())
        {

            case Prep:
                KDSStationNormal.normal_sync_schedule_item_ready_qty_changed(kds, command);
                break;
            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                KDSStationExpeditor.exp_sync_schedule_item_ready_qty_changed(kds, command);
                break;
            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
    }

    /**
     *
     * @param kds
     * @param command
     * @param strOrinalData
     * @return
     */
    static public String doSyncCommandItemUnbumped(KDS kds,  KDSXMLParserCommand command, String strOrinalData, ArrayList<KDSDataItem> arChangedItems)
    {
        switch (kds.getStationFunction())
        {

            case Prep:
                return KDSStationNormal.normal_sync_item_unbumped(kds, command, arChangedItems);

            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                return KDSStationExpeditor.exp_sync_item_unbumped(kds, command, arChangedItems);

            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
        return "";
    }

     static public void doSyncCommandItemModified(KDS kds,  KDSXMLParserCommand command, String strOrinalData)
    {
        switch (kds.getStationFunction())
        {

            case Prep:
                KDSStationNormal.normal_sync_item_modified(kds, command);
                break;
            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                KDSStationExpeditor.exp_sync_item_modified(kds, command);
                break;
            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
    }

    /**
     *
     * @param kds
     * @param command
     * @param strOrinalData
     * @return
     *  order guid
     */
    static public String doSyncCommandOrderBumped(KDS kds, KDSXMLParserCommand command, String strOrinalData, ArrayList<KDSDataItem> arChangedItems)
    {
        switch (kds.getStationFunction())
        {

            case Prep:
            case Summary://otherwise, the orders will never removed.
                return KDSStationNormal.normal_sync_order_bumped(kds, command);

            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            //case Summary:
                String s = KDSStationExpeditor.exp_sync_order_bumped(kds, command, arChangedItems);
                return s;


            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
        return "";

    }

    static public void doSyncCommandExpoOrderBumped(KDS kds, KDSXMLParserCommand command, String strOrinalData)
    {
        switch (kds.getStationFunction())
        {

            case Prep:
                KDSStationNormal.normal_sync_order_bumped(kds, command);//kpp1-202.

                break;
            case Queue:
            case Queue_Expo: {
                if (kds.getSettings().getBoolean(KDSSettings.ID.Queue_only_auto_bump))
                    return;
                KDSStationExpeditor.exp_sync_expo_order_bumped(kds, command);
            }
                break;
            case Expeditor:
            case TableTracker:
            case Runner:
            case Summary:
                //KDSStationExpeditor.exp_sync_station_cook_started(kds, command); //why
                KDSStationExpeditor.exp_sync_expo_order_bumped(kds, command);
                break;
            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }

    }

    static public void doSyncCommandCookStarted(KDS kds, KDSXMLParserCommand command)
    {
        String strOrderName = command.getParam();
        switch (kds.getStationFunction())
        {

            case Prep:
                KDSStationNormal.orderCookStartedByName(kds.getUsers().getUserA(),strOrderName);
                break;
            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                KDSStationExpeditor.exp_sync_station_cook_started(kds, command);
                break;
            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
    }

    static public void doSyncCommandTrackerOrderBumped(KDS kds, KDSXMLParserCommand command)
    {
        String strOrderName = command.getParam("P0", "");
        switch (kds.getStationFunction())
        {

            case Prep:
            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                KDSStationNormal.orderTrackerBump(kds.getUsers().getUserA(),strOrderName);
                break;
            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }

    }

    static  public void doSyncCommandExpoOrderUnbumped(KDS kds,  KDSXMLParserCommand command, String strOrinalData)
    {
        switch (kds.getStationFunction())
        {

            case Prep:

                break;
            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                KDSStationExpeditor.exp_sync_expo_order_unbumped(kds, command);
                break;
            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
    }

    static public void doSyncCommandExpoItemBumped(KDS kds, KDSXMLParserCommand command, String strOrinalData)
    {
        switch (kds.getStationFunction())
        {

            case Prep:

                break;
            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                KDSStationExpeditor.exp_sync_expo_item_bumped(kds, command);
                break;
            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
    }

    static public void doSyncCommandExpoItemUnbumped(KDS kds,  KDSXMLParserCommand command, String strOrinalData)
    {
        switch (kds.getStationFunction())
        {

            case Prep:

                break;
            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                KDSStationExpeditor.exp_sync_expo_item_unbumped(kds, command);
                break;
            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
    }

    static public void doSyncCommandQueueOrderReady(KDS kds,  KDSXMLParserCommand command, String strOrinalData)
    {
        switch (kds.getStationFunction())
        {

            case Prep:

                break;
            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                KDSStationExpeditor.queue_sync_expo_order_ready_unready(kds, command);
                break;
            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
    }

    static public void doSyncCommandQueueOrderUnready(KDS kds,  KDSXMLParserCommand command, String strOrinalData)
    {
        switch (kds.getStationFunction())
        {

            case Prep:

                break;
            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                KDSStationExpeditor.queue_sync_expo_order_ready_unready(kds, command);
                break;
            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
    }
    static public void sync_settings_queue_expo_double_bump(KDS kds,  KDSXMLParserCommand command, String strOrinalData)
    {

    }



    static  public void doSyncCommandOrderCanceled(KDS kds, KDSXMLParserCommand command, String strOrinalData)
    {
        switch (kds.getStationFunction())
        {

            case Prep:
                KDSStationNormal.normal_sync_order_canceled(kds, command);
                break;
            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                KDSStationExpeditor.exp_sync_order_canceled(kds, command);
                break;
            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
    }
    static public void doSyncCommandOrderModified(KDS kds,  KDSXMLParserCommand command, String strOrinalData)
    {
        switch (kds.getStationFunction())
        {

            case Prep:
                KDSStationNormal.normal_sync_order_modified(kds, command);
                break;
            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                KDSStationExpeditor.exp_sync_order_modified(kds, command);
                break;
            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
    }

    /**
     * receive sync command, do it.
     * @param command
     * @return
     * expo: return changed order
     * prep: null;
     */
    static public KDSDataOrder doSyncCommandOrderNew(KDS kds, KDSXMLParserCommand command, String strOrinalData, ArrayList<Boolean> ordersExisted, ArrayList<KDSDataOrder> ordersChanged)
    {
        switch (kds.getStationFunction())
        {

            case Prep:
                KDSStationNormal.normal_sync_order_new(kds, command);
                kds.refreshView();
                break;
            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            case Summary:
                KDSDataOrder order = KDSStationExpeditor.exp_sync_order_new(kds, command, ordersExisted, ordersChanged);
                kds.refreshView();
                return order;
                //break;

            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
        }
        return null;

    }

    /**
     * order comes in by transfering order command.
     * @param kds
     * @param command
     * @param strOrinalData
     */
    static public KDSDataOrder doSyncCommandOrderTransfer(KDS kds, KDSXMLParserCommand command, String strOrinalData, String fromStationID)
    {
        KDSDataOrder order = KDSStationFunc.order_transfered_in(kds, command, fromStationID);

        kds.refreshView();
        return order;


    }
    static  public void doSyncCommandOrderUnbumped(KDS kds,  KDSXMLParserCommand command, String strOrinalData)
    {
        switch (kds.getStationFunction())
        {

            case Prep:
            case Summary:
                KDSStationNormal.normal_sync_order_unbumped(kds,command);
                break;
            case Expeditor:
            case Queue:
            case TableTracker:
            case Queue_Expo:
            case Runner:
            //case Summary:
                KDSStationExpeditor.exp_sync_order_unbumped(kds, command);
                break;
            case Mirror:
                break;
            case Backup:
                break;
            case Workload:
                break;
            case Duplicate:
                break;
            default:
                break;
        }
    }

    static public KDSDataOrder order_transfered_in(KDS kds,KDSXMLParserCommand command, String fromStationID)
    {
        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strXml.isEmpty())
            return null;
        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(kds.getStationID(), strXml);
        //
        //order.recreateGUID(); //KP-131, transfer in order need new guid, don't use that from original station.

        KDSUser.USER userID = KDSUser.USER.values()[ order.getScreen()];
        if (order == null)
            return null;

        order.setItemsTransferedFromStationID(fromStationID);//KPP1-53.

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.
                kds.getSupportDB().orderAdd(order);
                //don't show them
            }
            else
            { //primary is offline now, svae to current database.
                func_orderAdd(kds.getUsers().getUser(userID), order,"", false, false, false,true); //don't check add-on
            }
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station

            func_orderAdd(kds.getUsers().getUser(userID), order,"", false, false, false,true);

        }
        else
        { //I am common station,
            //check if current database contains this order.
            func_orderAdd(kds.getUsers().getUser(userID), order, "",false, false, false, true);
        }

        //sync to others

        if (kds.getSettings().getBoolean(KDSSettings.ID.Transfer_prep_expo))
            sync_with_expo(kds, command.getCode(), order, null); //kp-116 Transfer Prep -> Transfer Expo
        //
        sync_with_mirror(kds, command.getCode(), order, null);
        sync_with_backup(kds, command.getCode(), order, null);
        return order;
    }

    /**
     * run this command in support database.
     * Sql format:
     *      insert ...; delete ...;
     * @param kds
     * @param command
     * @param strOrinalData
     */

    static public void doSqlSupportDB(KDS kds,KDSXMLParserCommand command, String strOrinalData)
    {
        String sql = command.getParam(KDSConst.KDS_Str_Param, "");
        if (sql.isEmpty())
            return;
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        kds.getSupportDB().exeSqlBatchString(sql);

        //DEBUG
        ArrayList<String> ar = KDSUtil.spliteString(sql, "\n");
        kds.showMessage("Run support sql count=" + KDSUtil.convertIntToString(ar.size()));


    }


    /**
     * run this command in "current" database.
     * Sql format:
     *      insert ...; delete ...;
     * @param kds
     * @param command
     * @param strOrinalData
     */

    static public void doSqlCurrentDB(KDS kds,KDSXMLParserCommand command, String strOrinalData)
    {
        String sql = command.getParam(KDSConst.KDS_Str_Param, "");
        if (sql.isEmpty())
            return;
        if (sql.equals(DB_END))
        {
            kds.loadAllActiveOrders();
            kds.refreshView();
            return;
        }
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        //kds.getCurrentDB().executeDML(sql);
        kds.getCurrentDB().exeSqlBatchString(sql);
        //exeSqlBatchString(kds.getCurrentDB(), sql);

    }

    /**
     *
     * @param orders
     * @return
     *   Format:
     *      Order name \n
     *      order name \n
     *      ...
     */
    static public String buildShowingOrderString(KDSDataOrders orders)
    {
        String strReturn = "";
        int ncount = orders.getCount();
        for (int i=0; i< ncount; i++)
        {
            String orderName = orders.get(i).getOrderName();
            if (!strReturn.isEmpty())
                strReturn += DBSTATUS_SEPARATOR;
            strReturn += orderName;

        }
        return strReturn;
    }

    /**
     *
     * @return
     */
    static public String buildOrderGuidString( KDSDBCurrent db )
    {
        String strReturn = "";
        ArrayList<String> ar =  db.ordersGetAllGuid();
        int ncount = ar.size();
        for (int i=0; i< ncount; i++)
        {
            String orderGuid = ar.get(i);
            if (!strReturn.isEmpty())
                strReturn += DBSTATUS_SEPARATOR;
            strReturn += orderGuid;

        }
        return strReturn;
    }
    static public String DBSTATUS_SEPARATOR = "\n";
    /**
     * remote station ask my current and support database status, for restoreing.
     * return support database active orders count and current database active orders count
     * @param kds
     * @param command
     * @param strOrinalData
     */
    static  public void doAskDBStatus(KDS kds,KDSXMLParserCommand command, String strOrinalData)
    {
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");

        int nCurrentCount = kds.getCurrentDB().orderGetActiveCount();
        int nSupportCount = kds.getSupportDB().orderGetActiveCount();

        String currentDBTimeStamp = kds.getCurrentDB().getDbTimeStamp();
        String supportDBTimeStamp = kds.getSupportDB().getDbTimeStamp();

        String currentDBGuids = buildOrderGuidString( kds.getCurrentDB());
        String supportDBGuids = buildOrderGuidString(kds.getSupportDB());

        String showingOrders = buildShowingOrderString(kds.getUsers().getUserA().getOrders());
        if (kds.isMultpleUsersMode())
        {
            String orderNames = buildShowingOrderString(kds.getUsers().getUserB().getOrders());
            if (!showingOrders.isEmpty()) {
                showingOrders += DBSTATUS_SEPARATOR;
            }
            showingOrders += orderNames;

        }

        String strXml = KDSXMLParserCommand.createDatabaseStatusNotification(kds.getStationID(), kds.getLocalIpAddress(), kds.getLocalMacAddress(),
                                                                             nCurrentCount, nSupportCount, currentDBTimeStamp, supportDBTimeStamp, showingOrders,
                                                                            currentDBGuids, supportDBGuids );
        KDSStationConnection conn = kds.getStationsConnections().findConnectionByIP(fromIP);
        if (conn == null)
            return;
        conn.getSock().writeXmlTextCommand(strXml);

    }

    /**
     *
     * @param kds
     * @param command
     * @param strOrinalData
     * @return
     *  True: My orders database changed.
     *  false: do nothing
     */
    static  public boolean doStationReturnDbStatus(KDS kds,KDSXMLParserCommand command, String strOrinalData)
    {
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromStationIP = command.getParam(KDSConst.KDS_Str_IP, "");
        if (kds.getStationsConnections().isMyBackupStation(fromStationID)) //backup station restored
            return doMyBackupReturnDbStatus(kds, command, strOrinalData);
        else if (kds.getStationsConnections().isMyMirrorStation(fromStationID)) //my mirror station restored
            return doMyMirrorReturnDbStatus(kds, command, strOrinalData);
        return false;
    }

    /**
     * while my mirror restored, check mirror station database status,
     * the do sth according to its status.
     * P0: currentDB orders count
     * p1: supportDB order count
     * P2:currentDB last timestamp
     * P3: support DB last timestamp
     * p4:showing orders names.
     * p5: currentDB guids
     * p6: support DB guids
     * @param kds
     * @param command
     * @param strOrinalData
     */
    static  public boolean doMyMirrorReturnDbStatus(KDS kds,KDSXMLParserCommand command, String strOrinalData)
    {
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromStationIP = command.getParam(KDSConst.KDS_Str_IP, "");
        String strCurrent = command.getParam("P0", "0");
        String strSupport = command.getParam("P1", "0");
        String strShowingOrdersNames = command.getParam("P4", "");
        ArrayList arShowingOrdersNames = KDSUtil.spliteString(strShowingOrdersNames, DBSTATUS_SEPARATOR);
        String strCurrentGuids = command.getParam("P5", "");
        ArrayList arMirrorCurrentGuids = KDSUtil.spliteString(strCurrentGuids, DBSTATUS_SEPARATOR);

        int nMyMirrorCurrent = KDSUtil.convertStringToInt(strCurrent, 0);
        int nBackpSupport = KDSUtil.convertStringToInt(strSupport, 0);

        int nMyCurrent = kds.getCurrentDB().orderGetActiveCount();

        boolean bSendDbMirror = false;

        if ( nMyMirrorCurrent!= nMyCurrent ) bSendDbMirror = true;
        if (!bSendDbMirror)
        {
            ArrayList arMyCurrentDbGuids = kds.getCurrentDB().ordersGetAllGuid();
            if (!KDSUtil.isArrayContainsSameStrings(arMyCurrentDbGuids, arMirrorCurrentGuids))
                bSendDbMirror  = true;
        }
        if (!bSendDbMirror) return false;
        //{//copy all my current database data to backup station support.
            KDSStationConnection conn = kds.getStationsConnections().findConnectionByIP(fromStationIP);
            if (conn == null) return false;
            kds.showMessage("copy to mirror current, backup wrong");
            copyMyCurrentToMirrorCurrentDB(kds, conn);

        //}
        return false;

    }

    static public boolean isSameDbTimeStamp(String t1, String t2)
    {
        Date d1 = KDSUtil.convertStringToDate(t1);
        Date d2 = KDSUtil.convertStringToDate(t2);

        long n = d1.getTime() - d2.getTime();
        n = Math.abs(n);
        return  ( n <=3000);


    }

    /**
     *
     * @param strShowingOrders
     *    format:
     *          ordername\n
     *          ordername\n
     *          ...
     * @return
     */
    static public ArrayList<String> parseShowingOrdersString(String strShowingOrders)
    {
        return KDSUtil.spliteString(strShowingOrders, "\n");

    }
    /**
     * This function was called after backup station restored.
     * Main ask backup station database status, backup station return this.
     * @param kds
     * @param command
     * @param strOrinalData
     * return:
     *  True, changed.
     */
    static  public boolean doMyBackupReturnDbStatus(KDS kds,KDSXMLParserCommand command, String strOrinalData)
    {
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromStationIP = command.getParam(KDSConst.KDS_Str_IP, "");
        String strCurrentCount = command.getParam("P0",  "0");
        String strSupportCount = command.getParam("P1", "0");
        String strCurrentTimeStamp = command.getParam("P2", KDSUtil.convertDateToString(new Date()));// "0");
        String strSupportTimeStamp = command.getParam("P3", KDSUtil.convertDateToString(new Date()));
        String strShowingOrders =  command.getParam("P4", "");
        ArrayList<String> arBackupShowingOrders = parseShowingOrdersString(strShowingOrders);

        int nBackupCurrent = KDSUtil.convertStringToInt(strCurrentCount, 0);
        int nBackpSupport = KDSUtil.convertStringToInt(strSupportCount, 0);



        int nMyCurrent = kds.getCurrentDB().orderGetActiveCount();
        String strMyCurrentTimeStamp = kds.getCurrentDB().getDbTimeStamp();

        kds.getCurrentDB().orderDelete(arBackupShowingOrders);

        if (nBackpSupport == 0 &&  !isSameDbTimeStamp(strMyCurrentTimeStamp, strCurrentTimeStamp))//nBackupCurrent!= nMyCurrent )
        {//something changed, clear my current.
            //kds.getCurrentDB().clear();
            //kds.loadAllActiveOrders();
            //kds.refreshView();
            KDSStationConnection conn = kds.getStationsConnections().findConnectionByIP(fromStationIP);
            if (conn == null) return false;
            kds.showMessage("copy to backup support, I am newer");
            copyMyCurrentToBackupSupportDB(kds, conn);
        }
        else if ( !isSameDbTimeStamp(strMyCurrentTimeStamp, strCurrentTimeStamp))//(nBackpSupport != nMyCurrent)
        {//copy all my current database data to backup station support.
            KDSStationConnection conn = kds.getStationsConnections().findConnectionByIP(fromStationIP);
            if (conn == null) return false;
            kds.showMessage("copy to backup support, backup wrong");
            copyMyCurrentToBackupSupportDB(kds, conn);

        }
        return false;


    }
    static final int _3KB = 3072;
    static final int _30KB = 30720;
    static final int _10KB = 10240;
    static final int _100KB = 102400;
    static final int _20KB = 20480;
    static public boolean copyMyCurrentToBackupSupportDB(KDS kds, KDSStationConnection connBackup)
    {
        ArrayList<String> arSql = kds.getCurrentDB().outputActiveOrdersSqlStrings();
        String sql = "";
        int counter = 0;
        for (int i=0; i< arSql.size(); i++)
        {
            sql += arSql.get(i);
            counter++;
            if (sql.length() > _10KB || (i == (arSql.size()-1)) )
            {

                String strXml = KDSXMLParserCommand.createSqlSupportDB(kds.getStationID(), kds.getLocalIpAddress(), kds.getLocalMacAddress(), sql);
                connBackup.getSock().writeXmlTextCommand(strXml);
                sql = "";
                kds.showMessage("Support sql total="+ KDSUtil.convertIntToString(arSql.size()) +" send=" + KDSUtil.convertIntToString(counter));
                counter = 0;
            }
        }

        return true;
    }

    /**
     * copy all data in current database to other station current db
     * @param kds
     * @param connBackup
     * @return
     */
    static public boolean copyMyCurrentToMirrorCurrentDB(KDS kds, KDSStationConnection connBackup)
    {
        ArrayList<String> arSql = new ArrayList<>();//kds.getCurrentDB().outputActiveOrdersSqlStrings();
        arSql.add("delete from orders;"+ KDSDBBase.SQL_SEPARATOR);
        arSql.add("delete from items;"+KDSDBBase.SQL_SEPARATOR);
        arSql.add("delete from condiments;"+KDSDBBase.SQL_SEPARATOR);
        arSql.add("delete from messages;"+KDSDBBase.SQL_SEPARATOR);

        ArrayList<String> arDbSql = kds.getCurrentDB().outputActiveOrdersSqlStrings();
        arSql.addAll(arDbSql);
        String sql = "";
        int counter = 0;
        for (int i=0; i< arSql.size(); i++)
        {
            sql += arSql.get(i);
            counter++;
            if (sql.length() > _10KB || (i == (arSql.size()-1)) )
            {

                String strXml = KDSXMLParserCommand.createSqlCurrentDB(kds.getStationID(), kds.getLocalIpAddress(), kds.getLocalMacAddress(), sql);
                connBackup.getSock().writeXmlTextCommand(strXml);
                sql = "";
                kds.showMessage("Current sql total="+ KDSUtil.convertIntToString(arSql.size()) +" send=" + KDSUtil.convertIntToString(counter));
                counter = 0;
            }
        }
        String strXml = KDSXMLParserCommand.createSqlCurrentDB(kds.getStationID(), kds.getLocalIpAddress(), kds.getLocalMacAddress(), DB_END);
        connBackup.getSock().writeXmlTextCommand(strXml);
        return true;
    }

    static public String DB_END = "-1";
//    static public boolean doStatisticAppAskData(KDSSocketInterface sock, KDS kds,KDSXMLParserCommand command, String strOrinalData, String strAfterTimeStamp)
//    {
//        ArrayList<String> arSql = kds.getStatisticDB().outputOrdersSqlStrings(strAfterTimeStamp);
//        long totalSize = 0;
//        long finishedSize = 0;
//        for (int i=0; i< arSql.size(); i++)
//        {
//             totalSize += arSql.get(i).length();
//        }
//        String sql = "";
//        int counter = 0;
//        for (int i=0; i< arSql.size(); i++)
//        {
//            sql += arSql.get(i);
//            counter++;
//            if (sql.length() > _3KB || (i == (arSql.size()-1)) )
//            {
//                finishedSize += sql.length();
//                String strXml = KDSXMLParserCommand.createSqlStatisticDB(kds.getStationID(), kds.getLocalIpAddress(), kds.getLocalMacAddress(), sql,totalSize, finishedSize );
//                //connBackup.getSock().writeXmlTextCommand(strXml);
//                if (sock instanceof KDSSocketTCPSideBase)
//                    ((KDSSocketTCPSideBase)sock).writeXmlTextCommand(strXml);
//                sql = "";
//                kds.showMessage("Statistic sql total="+ KDSUtil.convertIntToString(arSql.size()) +" send=" + KDSUtil.convertIntToString(counter));
//                counter = 0;
//            }
//        }
//
//        //send finished flag
//        String strXml = KDSXMLParserCommand.createSqlStatisticDB(kds.getStationID(), kds.getLocalIpAddress(), kds.getLocalMacAddress(), DB_END,totalSize, finishedSize);
//        //connBackup.getSock().writeXmlTextCommand(strXml);
//        if (sock instanceof KDSSocketTCPSideBase)
//            ((KDSSocketTCPSideBase)sock).writeXmlTextCommand(strXml);
//        return true;
//    }

    static public int stringArraySize(ArrayList<String> ar)
    {
        int totalSize = 0;
        for (int i=0; i< ar.size(); i++)
        {
            totalSize += ar.get(i).length();
        }
        return totalSize;
    }
    static public String convertSql(ArrayList<String> ar)
    {
        String s = "";
        for (int i=0; i< ar.size(); i++)
        {
            s += ar.get(i);
        }
        return s;
    }

    static public ArrayList<String> getUploadGuidForStatisticUploading(ArrayList<String> arInLocal, ArrayList<String> arInStatistic)
    {
        ArrayList<String> ar = new ArrayList<>();
        for (int i=0; i< arInLocal.size(); i++)
        {
            if (!KDSUtil.isExistedInArray(arInStatistic, arInLocal.get(i)))
                ar.add(arInLocal.get(i));
        }
        return ar;
    }

    static public ArrayList<String> getUploadRemoveGuidForStatisticUploading(ArrayList<String> arInLocal, ArrayList<String> arInStatistic)
    {
        ArrayList<String> ar = new ArrayList<>();
        for (int i=0; i< arInStatistic.size(); i++)
        {
            if (!KDSUtil.isExistedInArray(arInLocal, arInStatistic.get(i)))
                ar.add(arInStatistic.get(i));
        }
        return ar;
    }
    static public String makeRemvoeStringForStatistic(ArrayList<String> arWillDel)
    {

        String s = "";
        for (int i=0; i< arWillDel.size(); i++)
        {
            if (!s.isEmpty())
                s +=",";
            s += arWillDel.get(i);
        }
        return s;
    }
    final static String DEL_ORDER = "#@$DEL$@#";
    static public boolean doStatisticAppAskData3(KDSSocketInterface sock, KDS kds, KDSXMLParserCommand command, String strOrinalData, String strAfterTimeStamp, String existedInStatistic)
    {
        if (!KDSConst.ENABLE_FEATURE_STATISTIC)
            return true;
        ArrayList<String> arSql;// = kds.getStatisticDB().outputOrdersSqlStrings(strAfterTimeStamp);

        ArrayList<String> localOrderGuids = kds.getStatisticDB().orderGetOrdersByTimeStamp(strAfterTimeStamp);
        ArrayList<String> arInStatistic = KDSUtil.spliteString(existedInStatistic, ",");

        ArrayList<String> arOrderGuids = getUploadGuidForStatisticUploading(localOrderGuids, arInStatistic);
        ArrayList<String> arDelInStatistic = getUploadRemoveGuidForStatisticUploading(localOrderGuids, arInStatistic);
        arInStatistic.clear();//release memory
        localOrderGuids.clear();

        long totalSize = arOrderGuids.size();
        if (arDelInStatistic.size()>0)
            totalSize ++;
        long finishedSize = 0;
//        for (int i=0; i< arSql.size(); i++)
//        {
//            totalSize += arSql.get(i).length();
//        }
        String sql = "";
        int counter = 0;
        ArrayList<String> willBeSend = new ArrayList<>();

        for (int i=0; i< arOrderGuids.size(); i++)
        {
            arSql = kds.getStatisticDB().outputOrderSqlStrings(arOrderGuids.get(i));
            willBeSend.addAll(arSql);

            counter++;
            if (stringArraySize(willBeSend) > _20KB || (i == (arOrderGuids.size()-1)) )
            {
                finishedSize += counter;
                sql = convertSql(willBeSend);
                String strXml = KDSXMLParserCommand.createSqlStatisticDB(kds.getStationID(), kds.getLocalIpAddress(), kds.getLocalMacAddress(), sql,totalSize, finishedSize );
                //connBackup.getSock().writeXmlTextCommand(strXml);
                if (sock instanceof KDSSocketTCPSideBase)
                    ((KDSSocketTCPSideBase)sock).writeXmlTextCommand(strXml);
                sql = "";
                willBeSend.clear();
                kds.showMessage("Statistic sql total="+ KDSUtil.convertIntToString(arSql.size()) +" send=" + KDSUtil.convertIntToString(counter));
                counter = 0;
            }
        }
        String willDel = makeRemvoeStringForStatistic(arDelInStatistic);
        if (!willDel.isEmpty())
        {
            finishedSize ++;
            sql = willDel;
            sql = DEL_ORDER + sql;
            String strXml = KDSXMLParserCommand.createSqlStatisticDB(kds.getStationID(), kds.getLocalIpAddress(), kds.getLocalMacAddress(), sql,totalSize, finishedSize );
            //connBackup.getSock().writeXmlTextCommand(strXml);
            if (sock instanceof KDSSocketTCPSideBase)
                ((KDSSocketTCPSideBase)sock).writeXmlTextCommand(strXml);

            kds.showMessage("Statistic sql total="+ KDSUtil.convertIntToString(totalSize) +" send=" + KDSUtil.convertIntToString(finishedSize));

        }



        //send finished flag
        String strXml = KDSXMLParserCommand.createSqlStatisticDB(kds.getStationID(), kds.getLocalIpAddress(), kds.getLocalMacAddress(), DB_END,totalSize, finishedSize);
        //connBackup.getSock().writeXmlTextCommand(strXml);
        if (sock instanceof KDSSocketTCPSideBase)
            ((KDSSocketTCPSideBase)sock).writeXmlTextCommand(strXml);
        return true;
    }

    /**
     * sync with the order queue display stations.
     * @param kds
     * @param syncMode
     * @param order
     * @param item
     */
    static public void sync_with_queue(KDS kds, KDSXMLParserCommand.KDSCommand syncMode, KDSDataOrder order, KDSDataItem item , String xmlData)
    {
        if (kds.getStationsConnections().getRelations().getQueueStations().size() >0 ||
                kds.getStationsConnections().getRelations().getQueueExpoStations().size() >0) {
            String strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
            kds.getStationsConnections().writeToQueue(kds.getStationID(), strXml);
        }

    }

    static public void sync_with_tt(KDS kds, KDSXMLParserCommand.KDSCommand syncMode, KDSDataOrder order, KDSDataItem item )
    {
        if (kds.getStationsConnections().getRelations().getTTStations().size() <=0)
            return;//for speed
        String strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, "");
        kds.getStationsConnections().writeToTT(kds.getStationID(), strXml);

    }

    /**
     *
     * @param kds
     * @param syncMode
     * @param order
     * @param item
     * @param xmlData
     * @param bToExpoTypeStation
     *  Just for save data when do communication.
     *   If router running, the order has been send to expo/queue-expo/tt
     */
    static public void sync_with_stations(KDS kds,KDSXMLParserCommand.KDSCommand syncMode, KDSDataOrder order, KDSDataItem item, String xmlData, boolean bToExpoTypeStation )
    {
        String strXml = "";//KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item);
        //writeToStations(m_stationsConnection.getExpStations(), strXml);
        if (bToExpoTypeStation) {
            if (kds.getStationsConnections().getRelations().getExpStations().size() > 0) {
                if (strXml.isEmpty())
                    strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
                kds.getStationsConnections().writeToExps(kds.getStationID(), strXml);
            }
        }
        if (kds.getStationsConnections().getRelations().getMirrorStations().size()>0) {
            if (strXml.isEmpty())
                strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);//);
            kds.getStationsConnections().writeToMirrors(kds.getStationID(), strXml);
        }
        if (kds.getStationsConnections().getRelations().getBackupStations().size()>0) {
            if (strXml.isEmpty())
                strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
            kds.getStationsConnections().writeToBackups(kds.getStationID(), strXml);
        }
        if (kds.getStationsConnections().getRelations().getPrimaryStationsWhoUseMeAsMirror().size()>0) {
            if (strXml.isEmpty())
                strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
            kds.getStationsConnections().writeToPrimaryMirror(kds.getStationID(), strXml);
        }
        if (is_send_to_duplicated(syncMode)) {
            if (kds.getStationsConnections().getRelations().getDuplicatedStations().size() >0) {
                if (strXml.isEmpty())
                    strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
                kds.getStationsConnections().writeToDuplicated(kds.getStationID(), strXml);
            }
        }
        if (kds.getStationsConnections().getRelations().getQueueStations().size() >0) {
            if (strXml.isEmpty())
                strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
            kds.getStationsConnections().writeToQueue(kds.getStationID(), strXml);
        }
        if (bToExpoTypeStation) {
            if (kds.getStationsConnections().getRelations().getTTStations().size() > 0) {
                if (strXml.isEmpty()) {
                    //TimeDog td = new TimeDog();
                    strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
                    //td.debug_print_Duration("KDSXMLCommandFactory.sync_with_others");
                }
                kds.getStationsConnections().writeToTT(kds.getStationID(), strXml);
            }
        }

        //kpp1-449, KP-13 Bumped Item on expo doesn't show on prep station
        //KP-41, Expo and Runner - orders going to incorrect station
        if (isCommandNeedBroadcastToExpoChildren(syncMode)) {
            if (kds.getStationsConnections().getRelations().getPrepStationsWhoUseMeAsExpo(kds.getStationID()).size() > 0) {
                if (strXml.isEmpty())
                    strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
                kds.getStationsConnections().writeToPrimaryOfExpo(kds.getStationID(), strXml);
            }
        }

        //if the backup station find its primary is offline, send data to primary's mirror.
        if (kds.getStationsConnections().isBackupOfOthers())
        {
            ArrayList<KDSStationIP> primaryBackups = kds.getAllActiveConnections().getRelations().getPrimaryStationsWhoUseMeAsBackup();
            if (strXml.isEmpty())
                strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);
            //If my primary backup station is the primary mirror of others,
            //check if this primary station is offline. If so, write data to the slave mirror of primary.
            for (int i=0; i< primaryBackups.size();i ++)
                kds.getStationsConnections().writeToMirrorOfPrimaryBackup(kds.getStationID(),primaryBackups.get(i).getID(),  strXml);
        }
        else if (kds.getStationsConnections().isWorkLoadOfOthers())
        {

        }
        else if (kds.getStationsConnections().isDuplicatedOfOthers())
        {

        }
        else if (kds.getStationsConnections().isMirrorOfOthers())
        {

        }



    }

    /**
     * when expo bump order, it will inform this operation to its prep stations
     * kpp1-202
     * @param kds
     * @param syncMode
     * @param order
     * @param item
     * @param xmlData
     */
    static public void sync_with_stations_use_me_as_expo(KDS kds,KDSXMLParserCommand.KDSCommand syncMode, KDSDataOrder order, KDSDataItem item, String xmlData)
    {
//        if (!kds.isExpeditorStation() &&
//                (!kds.isRunnerStation()) &&
//                (!kds.isSummaryStation())) return;
        if (!isExpoTypeStation(kds.getStationFunction()))
            return;
        String strXml = "";

        ArrayList<KDSStationIP> arPrepStations = kds.getStationsConnections().getRelations().getPrepStationsWhoUseMeAsExpo(kds.getStationID());
        if (arPrepStations.size()>0) {
            strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, xmlData);//);
            kds.getStationsConnections().writeToStations(kds.getStationID(), arPrepStations, strXml);
        }



    }

    /**
     * kpp1-456
     * @param func
     * @return
     */
    static public boolean isExpoTypeStation(SettingsBase.StationFunc func)
    {
        return KDSBase.isExpoTypeStation(func);

//        if (func == SettingsBase.StationFunc.Expeditor ||
//                func == SettingsBase.StationFunc.Queue_Expo ||
//                func == SettingsBase.StationFunc.Runner ||
//                func == SettingsBase.StationFunc.Summary
//        )
//            return true;
//        else
//            return false;
    }

    /**
     * KP-41 Expo and Runner - orders going to incorrect station
     * Solution:
     *  Expo will just send two command to prep station.
     *
     * @param command
     * @return
     */
    static public boolean isCommandNeedBroadcastToExpoChildren(KDSXMLParserCommand.KDSCommand command)
    {
        if (command == KDSXMLParserCommand.KDSCommand.Expo_Bump_Item ||
            command == KDSXMLParserCommand.KDSCommand.Expo_Unbump_Item)
            return true;
        return false;
    }

    /**
     * summary station is expo type
     * @param kds
     * @param syncMode
     * @param order
     * @param item
     */
    static public void sync_with_expo(KDS kds, KDSXMLParserCommand.KDSCommand syncMode, KDSDataOrder order, KDSDataItem item )
    {
        if (kds.getStationsConnections().getRelations().getExpStations().size() <=0)
            return;//for speed
        String strXml = KDSXMLCommandFactory.sync_with_others(kds.getStationID(), kds.getLocalIpAddress(), "", syncMode, order, item, "");
        kds.getStationsConnections().writeToExps(kds.getStationID(), strXml);

    }
}
