package com.bematechus.kds;

import android.app.ProgressDialog;

import com.bematechus.kdslib.BuildVer;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSStationsRelation;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXMLParserCommand;
import com.bematechus.kdslib.KDSXMLParserOrder;
import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2015/10/14 0014.
 * do expeditor functions
 */
public class KDSStationExpeditor extends KDSStationNormal {


    /**
     *
     * Rev.
     *  2.0.35 queue state sort
     * @param kds
     * @param db
     * @param orders
     *  My orders
     * @param fromStationID
     * @param fromIP
     * @param order
     *     My local order
     * @param item
     *     my local item
     * @return
     */
    static public  boolean exp_item_bumped_in_other_station(KDS kds, KDSDBCurrent db, KDSDataOrders orders, String fromStationID, String fromIP, KDSDataOrder order, KDSDataItem item)
    {
        if (order == null)
            return false ;

        if (item == null)
            return false;

        //save old queue status;
        QueueOrders.QueueStatus queueStatus = QueueOrders.QueueStatus.Received;
        if (kds.isQueueStation() || kds.isQueueExpo())
        {//2.0.35, queue state time sort.
            queueStatus = QueueOrders.getOrderQueueStatus(order);//
        }

        String itemName = item.getItemName();

        boolean bStartedByMe = db.startTransaction();

        KDSDataItem expItem = item;// orderExisted.getItems().getItemByName(itemName);

        expItem.addRemoteBumpedStation(fromStationID);
        db.itemSetRemoteBumpedStations(expItem);
        db.commitTransaction(bStartedByMe);
        db.endTransaction(bStartedByMe);
        tt_checkAllItemsBumped(kds, order);


        kds.refreshView();

        //check if the queue status changed.
        if (kds.isQueueStation() || kds.isQueueExpo())
        {//2.0.35, queue state time sort.
            QueueOrders.QueueStatus currentQueueStatus = QueueOrders.getOrderQueueStatus(order);//
            if (queueStatus != currentQueueStatus)
            {
                db.orderUpdateQueueStateTime(order.getGUID());
                order.setQueueStateTime(new Date());
            }
        }

        return true;
    }

    /**
     * for backup/mirror of expo
     * @param kds
     * @param db
     * @param orders
     * @param fromStationID
     * @param fromIP
     * @param order
     * @param item
     * @return
     */
    static public  boolean exp_item_bumped_in_other_expo_station(KDS kds, KDSDBCurrent db,KDSDataOrders orders,  String fromStationID, String fromIP, KDSDataOrder order, KDSDataItem item)
    {
        if (order == null)
            return false ;

        if (item == null)
            return false;

        String itemName = item.getItemName();

        boolean bStartedByMe = db.startTransaction();

        KDSDataItem expItem = item;// orderExisted.getItems().getItemByName(itemName);
        expItem.setLocalBumped(true);//!item.getLocalBumped());
        db.itemSetLocalBumped(item);

        db.finishTransaction(bStartedByMe);

        tt_checkAllItemsBumped(kds, order);
        kds.refreshView();

        return true;
    }

    /**
     *
     * @param db
     * @param fromStationID
     * @param fromIP
     * @param order
     * @param item
     */
    static public  boolean exp_item_unbumped_in_other_station(KDS kds, KDSDBCurrent db,KDSDataOrders orders,  String fromStationID, String fromIP, KDSDataOrder order, KDSDataItem item)
    {
        if (order == null)
            return false ;

        if (item == null)
            return false;

        String itemName = item.getItemName();

        boolean bStartedByMe = db.startTransaction();

        KDSDataItem expItem = item;// orderExisted.getItems().getItemByName(itemName);

        expItem.removeRemoteBumpedStation(fromStationID);
        db.itemSetRemoteBumpedStations(expItem);
        db.commitTransaction(bStartedByMe);
        db.endTransaction(bStartedByMe);
        tt_checkAllItemsBumped(kds, order);
        kds.refreshView();
        return true;

    }

    static public  boolean exp_item_unbumped_in_other_expo_station(KDS kds, KDSDBCurrent db,KDSDataOrders orders,  String fromStationID, String fromIP, KDSDataOrder order, KDSDataItem item)
    {
        if (order == null)
            return false ;

        if (item == null)
            return false;

        String itemName = item.getItemName();

        boolean bStartedByMe = db.startTransaction();

        KDSDataItem expItem = item;// orderExisted.getItems().getItemByName(itemName);
        expItem.setLocalBumped(false);//!item.getLocalBumped());
        expItem.removeRemoteBumpedStation(fromStationID);
        db.itemSetRemoteBumpedStations(item);
        db.itemSetLocalBumped(item);

        db.finishTransaction(bStartedByMe);
        tt_checkAllItemsBumped(kds, order);
        kds.refreshView();
        return true;

    }

    /**
     * In expeditor station, get information that given order was bumped by others station.
     * we should set expeditor satation.
     * Mark all items to "bumped" in exp
     *
     * rev.
     *  2.0.35
     *
     *
     * @param db
     *  Save to which db, this is for support and current
     * @param fromStationID
     * @param fromIP
     * @param order
     */
    static public String exp_order_bumped_in_other_station(KDS kds,KDSDBCurrent db,KDSDataOrders orders,  String fromStationID, String fromIP,
                                                           KDSDataOrder order, ArrayList<KDSDataItem> arChangedItems)
    {
        KDSDataOrder orderExisted = orders.getOrderByName(order.getOrderName());

        if (orderExisted == null) {
            return "";
        }
        //save old queue status;
        QueueOrders.QueueStatus queueStatus = QueueOrders.QueueStatus.Received;
        if (kds.isQueueStation() || kds.isQueueExpo())
        {//2.0.35, queue state time sort.
            queueStatus = QueueOrders.getOrderQueueStatus(orderExisted);//
        }

        int ncount = order.getItems().getCount();
        boolean bStartedByMe = db.startTransaction();
        for (int i=0; i< ncount; i++)
        {
            String itemName = order.getItems().getItem(i).getItemName();
            KDSDataItem expItem =  orderExisted.getItems().getItemByName(itemName);
            if (expItem == null) continue;

            if (expItem.addRemoteBumpedStation(fromStationID)) { //this item is not bump in prep, it will mark bumped here.
                db.itemSetRemoteBumpedStations(expItem);
                if (arChangedItems != null)
                    arChangedItems.add(expItem);
            }

        }
        db.finishTransaction(bStartedByMe);//2.0.15

//        db.commitTransaction(bStartedByMe);
//        db.endTransaction(bStartedByMe);

        tt_checkAllItemsBumped(kds, orderExisted);

        //check if the queue status changed.
        if (kds.isQueueStation() || kds.isQueueExpo())
        {//2.0.35, queue state time sort.
            QueueOrders.QueueStatus currentQueueStatus = QueueOrders.getOrderQueueStatus(orderExisted);//
            if (queueStatus != currentQueueStatus)
            {
                db.orderUpdateQueueStateTime(orderExisted.getGUID());
                orderExisted.setQueueStateTime(new Date());
            }
        }

        kds.refreshView();
        return orderExisted.getGUID();
    }

    /**
     *
     * @param kds
     * @param db
     * @param orders
     * @param fromStationID
     * @param fromIP
     * @param order
     * @return
     *  order guid
     */
    static public  String exp_order_unbumped_in_other_station(KDS kds,KDSDBCurrent db,KDSDataOrders orders, String fromStationID, String fromIP, KDSDataOrder order)
    {
        KDSDataOrder orderExisted = orders.getOrderByName(order.getOrderName());

        if (orderExisted == null) {
            //2.0.18
            if (kds.isQueueExpo() ||
                    kds.isQueueStation() )
            {
                String orderName = order.getOrderName();
                String bumpedGuid = db.orderGetBumpedGuidFromName(orderName);
                if (bumpedGuid.isEmpty())
                    return "";

                db.orderSetBumped(bumpedGuid, false);
                //if (bUpdateShowingOrders)
                //{
                    orderExisted = db.orderGet(bumpedGuid);
                    orders.addComponent(orderExisted);
                    //kds.refreshView();
                //}
            }
            else
                return "";
        }
        int ncount = order.getItems().getCount();
        boolean bStartedByMe = db.startTransaction();
        for (int i=0; i< ncount; i++)
        {
            String itemName = order.getItems().getItem(i).getItemName();
            KDSDataItem expItem =  orderExisted.getItems().getItemByName(itemName);
            if (expItem == null) continue;
            if (!order.getItems().getItem(i).getLocalBumped()) { //20190723 if this item has been bumped in prep station, don't reset its bumped_stations.
                expItem.removeRemoteBumpedStation(fromStationID);

                db.itemSetRemoteBumpedStations(expItem);
            }

        }
        db.finishTransaction(bStartedByMe); //2.0.15
        //db.commitTransaction(bStartedByMe);
        //db.endTransaction(bStartedByMe);
        tt_checkAllItemsBumped(kds, orderExisted);
        kds.refreshView();
        return orderExisted.getGUID();
    }

    static public void exp_sync_order_unbumped(KDS kds,KDSXMLParserCommand command)
    {
        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strXml.isEmpty())
            return ;
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(kds.getStationID(), strXml);

        if (order == null)
            return ;

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.

                exp_order_unbumped_in_other_station(kds, kds.getSupportDB(),kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
                if (kds.isMultpleUsersMode())
                    exp_order_unbumped_in_other_station(kds, kds.getSupportDB(),kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);
                //don't show them
            }
            else
            { //primary is offline now, svae to current database.
                exp_order_unbumped_in_other_station(kds, kds.getCurrentDB(),kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
                if (kds.isMultpleUsersMode())
                    exp_order_unbumped_in_other_station(kds, kds.getCurrentDB(),kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);
            }
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryWhoUseMeAsMirrorActive())
            {
                exp_order_unbumped_in_other_station(kds, kds.getCurrentDB(),kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
                if (kds.isMultpleUsersMode())
                    exp_order_unbumped_in_other_station(kds, kds.getCurrentDB(),kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);
            }
            else
            {
                exp_order_unbumped_in_other_station(kds, kds.getCurrentDB(),kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
                if (kds.isMultpleUsersMode())
                    exp_order_unbumped_in_other_station(kds, kds.getCurrentDB(),kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);
            }

        }
        else
        { //I am common station
            //check if current database contains this order.
            exp_order_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
            if (kds.isMultpleUsersMode())
                exp_order_unbumped_in_other_station(kds, kds.getCurrentDB(),kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);

        }
        tt_checkAllItemsBumped(kds, order);
        //sync to others
        sync_with_mirror(kds, command.getCode(), order, null);
        sync_with_backup(kds, command.getCode(), order, null);
        sync_with_queue(kds, command.getCode(), order, null, strXml);



    }

    /**
     * Item was bumped in other station.
     * The other stations maybe is normal or exp
     * @param kds
     * @param command
     * @return
     */
    static public  boolean exp_sync_expo_item_bumped(KDS kds,  KDSXMLParserCommand command)
    {
        String strOrderName = command.getParam("P0", "");
        String strItemName = command.getParam("P1", "");

        if (strOrderName.isEmpty() ||
                strItemName.isEmpty()    )
            return false;
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");

        //check user A
        KDSDataOrder orderA = kds.getUsers().getUserA().getOrders().getOrderByName(strOrderName);

        KDSDataItem itemA = null;
        if (orderA != null)
            itemA = orderA.getItems().getItemByName(strItemName);
        //check user B
        KDSDataOrder orderB = null;
        KDSDataItem itemB = null;
        if (kds.isMultpleUsersMode())
        {
            orderB = kds.getUsers().getUserA().getOrders().getOrderByName(strOrderName);
            if (orderB != null)
                itemB = orderB.getItems().getItemByName(strItemName);
        }

        if (itemA == null && itemB == null)
            return false;

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
//            if (kds.getStationsConnections().isPrimaryBackupActive())
//            {//actived, save order to "support" database.
                if (itemA != null)
                {
                    if (!exp_item_bumped_in_other_expo_station(kds, kds.getSupportDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                        return false;
                    if (!exp_item_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                        return false;
                }
                if (itemB != null)
                {
                    if (!exp_item_bumped_in_other_expo_station(kds, kds.getSupportDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                        return false;
                    if (!exp_item_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                        return false;
                }

//                //don't show them
//            //}
////            else
////            { //primary is offline now, svae to current database.
//                if (itemA != null) {
//                    if (!exp_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
//                        return false;
//                }
//                if (itemB != null)
//                {
//                    if (!exp_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
//                        return false;
//                }
//            //}
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary backup is online or offline.
//            if (kds.getStationsConnections().isPrimaryMirrorActive())
//            {
                if (itemA != null) {
                    if (!exp_item_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                        return false;
                }
                if (itemB != null)
                {
                    if (!exp_item_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                        return false;
                }
//            }
//            else
//            {
//                if (itemA != null) {
//                    if (!exp_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
//                        return false;
//                }
//                if (itemB != null)
//                {
//                    if (!exp_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
//                        return false;
//                }
//            }

        }
        else if (kds.isQueueStation() || kds.isTrackerStation() ||kds.isQueueExpo())
        {
            if (itemA != null) {
                if (!exp_item_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                    return false;
            }
            if (itemB != null)
            {
                if (!exp_item_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                    return false;
            }
        }

        else if (kds.isExpeditorStation() ||kds.isRunnerStation() || kds.isSummaryStation()) //kpp1-286
        {
            //mirror do same work, kpp1-286
            if ( kds.getStationsConnections().getRelations().isMyMirrorStation(fromStationID))
            {
                if (itemA != null) {
                    if (!exp_item_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                        return false;
                }
                if (itemB != null)
                {
                    if (!exp_item_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                        return false;
                }
            }

        }

        else
        { //I am common station

        }

        //sync to others
        if (itemA != null) {
            tt_checkAllItemsBumped(kds, orderA);
            sync_with_mirror(kds, command.getCode(), orderA, itemA);
            sync_with_backup(kds, command.getCode(), orderA, itemA);
            sync_with_queue(kds, command.getCode(), orderA, itemA, "");
        }
        else if (itemB != null)
        {
            tt_checkAllItemsBumped(kds, orderB);
            sync_with_mirror(kds, command.getCode(), orderB, itemB);
            sync_with_backup(kds, command.getCode(), orderB, itemB);
            sync_with_queue(kds, command.getCode(), orderB, itemB, "");
        }

        return true;

    }

    /**
     *
     * @param kds
     * @param command
     * @return
     *  order guid
     */
    static public  String exp_sync_item_unbumped(KDS kds, KDSXMLParserCommand command, ArrayList<KDSDataItem> arChangedItems)
    {
        String strOrderName = command.getParam("P0", "");
        String strItemName = command.getParam("P1", "");

        if (strOrderName.isEmpty() ||
                strItemName.isEmpty()    )
            return "";
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");

        //user A
        KDSDataOrder orderA = kds.getUsers().getUserA().getOrders().getOrderByName(strOrderName);
        KDSDataItem itemA = null;
        if (orderA != null)
            itemA = orderA.getItems().getItemByName(strItemName);

        //user B
        KDSDataOrder orderB =null; //kds.getUsers().getUserB().getOrders().getOrderByName(strOrderName);
        KDSDataItem itemB = null;
        if (kds.isMultpleUsersMode()) {
            orderB = kds.getUsers().getUserB().getOrders().getOrderByName(strOrderName);
            if (orderB != null)
                itemB = orderB.getItems().getItemByName(strItemName);
        }

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.
                if (itemA != null)
                    exp_item_unbumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA);
                if (itemB != null)
                    exp_item_unbumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB);
                //don't show them
            }
            else
            { //primary is offline now, svae to current database.
                if (itemA != null)
                    exp_item_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA);
                if (itemB != null)
                    exp_item_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB);
            }
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryWhoUseMeAsMirrorActive())
            {
                if (itemA != null)
                    exp_item_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA);
                if (itemB != null)
                    exp_item_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB);
            }
            else
            {
                //exp_item_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getOrders(), fromStationID, fromIP, order, item);
                if (itemA != null)
                    exp_item_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA);
                if (itemB != null)
                    exp_item_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB);
            }

        }
        else if (kds.isQueueStation()|| kds.isTrackerStation()||
                kds.isQueueExpo() || kds.isExpeditorStation() ||
                kds.isRunnerStation() || kds.isSummaryStation()) //2.0.14, add expo
        {
            if (itemA != null)
                exp_item_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA);
            if (itemB != null)
                exp_item_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB);
        }
        else
        { //I am common station

        }

        //sync to others
        if (itemA != null) {
            tt_checkAllItemsBumped(kds, orderA);
            sync_with_mirror(kds, command.getCode(), orderA, itemA);
            sync_with_backup(kds, command.getCode(), orderA, itemA);
            sync_with_queue(kds, command.getCode(), orderA, itemA, "");

            arChangedItems.add(itemA);//kpp1-407

            return orderA.getGUID();
        }
        else if (itemB != null)
        {
            tt_checkAllItemsBumped(kds, orderB);
            sync_with_mirror(kds, command.getCode(), orderB, itemB);
            sync_with_backup(kds, command.getCode(), orderB, itemB);
            sync_with_queue(kds, command.getCode(), orderB, itemB, "");

            arChangedItems.add(itemB); //kpp1-407

            return orderB.getGUID();
        }

        return "";

    }

    /**
     *
     * @param kds
     * @param command
     *   P0: order name
     *   P1: item xml.
     */
    static public  void exp_sync_item_modified(KDS kds,  KDSXMLParserCommand command)
    {
        String strOrderName = command.getParam("P0", "");
        if (strOrderName.isEmpty()) return;
        String strItemXml = command.getParam("P1", "");
        if (strItemXml.isEmpty()) return;

        //set item's order name
        KDSDataOrder orderA = null;
        KDSDataOrder orderB = null;
        orderA = kds.getUsers().getUserA().getOrders().getOrderByName(strOrderName);
//        if (orderA == null)
//            return;
        if (kds.isMultpleUsersMode())
            orderB = kds.getUsers().getUserB().getOrders().getOrderByName(strOrderName);
        if (orderA == null && orderB == null)
            return; //order don't existed

        KDSDataItem item =(KDSDataItem) KDSXMLParserOrder.parseItem( strItemXml);
        if (item == null)
            return; //xml data error

        KDSUser.USER userID = KDSUser.USER.USER_A;
        KDSDataItem itemOriginal = null;

        //check user A
        if (orderA != null) {
            itemOriginal = orderA.getItems().getItemByName(item.getItemName());
            if (itemOriginal != null)
            {
                userID = KDSUser.USER.USER_A;
                item.setOrderGUID(orderA.getGUID());
                item.setGUID(itemOriginal.getGUID());
            }
        }
        //Check User B
        if (itemOriginal == null && orderB != null) {
            itemOriginal = orderB.getItems().getItemByName(item.getItemName());
            if (itemOriginal != null)
            {
                userID = KDSUser.USER.USER_B;
                item.setOrderGUID(orderB.getGUID());
                item.setGUID(itemOriginal.getGUID());
            }
        }

        if (itemOriginal == null)
            return;

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.
                kds.getSupportDB().itemModify(item);
                //don't show them
            }
            else
            { //primary is offline now, svae to current database.

                itemModify(kds.getUsers().getUser(userID), item, false); //
            }
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station

            itemModify(kds.getUsers().getUser(userID), item, false);

        }
        else
        { //I am common station, I am a expeditor.
            //check if current database contains this order.
            itemModify(kds.getUsers().getUser(userID),item, false);
        }

        //sync to others
        if (userID == KDSUser.USER.USER_A) {
            sync_with_mirror(kds, command.getCode(), orderA, item);
            sync_with_backup(kds, command.getCode(), orderA, item);
            sync_with_queue(kds, command.getCode(), orderA, item, "");
        }
        else
        {
            sync_with_mirror(kds, command.getCode(), orderB, item);
            sync_with_backup(kds, command.getCode(), orderB, item);
            sync_with_queue(kds, command.getCode(), orderB, item, "");
        }

    }



    static public void tt_checkAllItemsBumped(KDS kds,KDSDataOrder order)
    {
        if (order == null) return;
        if (order.isItemsAllBumpedInExp())
        {
            if (order.getTTAllItemsBumped())
                return;
            else
            {
                order.setTTAllItemsBumped( true);
                order.setTTAllItemsBumpedDate( new Date());
            }
        }
        else
        {
            order.setTTAllItemsBumped( false);
        }
    }
    /**
     *
     * @param command
     * return:
     *  order guid
     */
    /**
     *
     * @param kds
     * @param command
     * @param arChangedItems
     *  use it to update backoffice item_bumps table "preparation_time" and "done_time".
     * @return
     *  order guid
     */
    static public  String exp_sync_order_bumped(KDS kds, KDSXMLParserCommand command, ArrayList<KDSDataItem> arChangedItems)
    {
        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strXml.isEmpty())
            return "";
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(kds.getStationID(), strXml);//It just contains ID

        if (order == null)
            return "";
        //if (BuildVer.isDebug())
        //    System.out.println("from="+fromStationID + ",orderid=" + order.getOrderName());
        String orderGuid = "";

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.

                if (kds.getStationsConnections().isMyPrimaryBackupStation(fromStationID))
                {
                    orderGuid = normal_order_bumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
                    if (kds.isMultpleUsersMode()) {
                       normal_order_bumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);

                    }
                }
                else {
                    orderGuid = exp_order_bumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order,arChangedItems);
                    if (kds.isMultpleUsersMode())
                        exp_order_bumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order,arChangedItems);
                }
                //don't show them
            }
            else
            { //primary is offline now, svae to current database.
                if (kds.getStationsConnections().isMyPrimaryBackupStation(fromStationID))
                {
                    orderGuid = normal_order_bumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
                    if (kds.isMultpleUsersMode())
                        normal_order_bumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);
                }
                else {
                    orderGuid =  exp_order_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order,arChangedItems);
                    if (kds.isMultpleUsersMode()) {
                        String guid= exp_order_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order,arChangedItems);
                        if (!orderGuid.isEmpty())
                            orderGuid += ",";
                        orderGuid += guid;
                    }
                }
            }

            tt_checkAllItemsBumped(kds, order);

            return orderGuid;
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryWhoUseMeAsMirrorActive())
            {
                orderGuid = exp_order_bumped_in_other_station(kds, kds.getCurrentDB(),  kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order,arChangedItems);
                if (kds.isMultpleUsersMode())
                    exp_order_bumped_in_other_station(kds, kds.getCurrentDB(),  kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order,arChangedItems);
            }
            else
            {
                orderGuid = exp_order_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order,arChangedItems);
                if (kds.isMultpleUsersMode())
                    exp_order_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order,arChangedItems);
            }

        }
        else
        { //I am common station
            //check if current database contains this order.
            orderGuid = exp_order_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order,arChangedItems);
            if (kds.isMultpleUsersMode())
                exp_order_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order,arChangedItems);
        }

        tt_checkAllItemsBumped(kds, order);
        //sync to others
        sync_with_mirror(kds, command.getCode(), order, null);
        sync_with_backup(kds, command.getCode(), order, null);
        sync_with_queue(kds, command.getCode(), order, null, strXml);

        return orderGuid;
//        if (order != null)
//            return order.getGUID();
//        else
//            return "";

    }

    static public String exp_order_bumped_in_other_expo_station(KDS kds,KDSDBCurrent db,KDSDataOrders orders,  String fromStationID, String fromIP, KDSDataOrder orderReceived)
    {

        return exp_order_bumped_in_other_expo_station(kds, db,orders, fromStationID, fromIP, orderReceived.getOrderName());

//        KDSDataOrder orderExisted = orders.getOrderByName(orderReceived.getOrderName());
//
//        if (orderExisted == null) {
//            return "";
//        }
//        orders.removeComponent(orderExisted);
//       db.orderSetBumped(orderExisted.getGUID(), true);
//        //update the statistic database
//        tt_checkAllItemsBumped(kds, orderExisted);
//        kds.refreshView();
//        return orderExisted.getGUID();


    }

    static public String exp_order_bumped_in_other_expo_station(KDS kds,KDSDBCurrent db,KDSDataOrders orders,  String fromStationID, String fromIP, String orderNameReceived)
    {

        KDSDataOrder orderExisted = orders.getOrderByName(orderNameReceived);

        if (orderExisted == null) {
            return "";
        }
        //kpp1-286
        kds.fireOrderBumpedInOther(orderExisted.getGUID());

        orders.removeComponent(orderExisted);
        db.orderSetBumped(orderExisted.getGUID(), true);
        //update the statistic database
        tt_checkAllItemsBumped(kds, orderExisted);

        kds.refreshView();
        return orderExisted.getGUID();


    }

    /**
     *
     * @param kds
     * @param db
     * @param orders
     * @param fromStationID
     * @param fromIP
     * @param orderReceived
     * @return
     */
    static public String tt_receive_exp_order_bumped_notification(KDS kds,KDSDBCurrent db,KDSDataOrders orders,  String fromStationID, String fromIP, KDSDataOrder orderReceived)
    {

        KDSDataOrder orderExisted = orders.getOrderByName(orderReceived.getOrderName());

        if (orderExisted == null) {
            return "";
        }

        orderExisted.setTTReceiveExpoBumpNotification(true);
        orderExisted.setTTReceiveExpoBumpNotificationDate( new Date() );

        //orders.removeComponent(orderExisted);
        db.orderSetBumped(orderExisted.getGUID(), true);
        //update the statistic database

        kds.refreshView();
        return orderExisted.getGUID();


    }

    /**
     *
     * @param command
     */
    static public  void exp_sync_expo_order_bumped(KDS kds, KDSXMLParserCommand command)
    {
        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strXml.isEmpty())
            return;
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(kds.getStationID(), strXml);

        if (order == null)
            return;

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            exp_order_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
                if (kds.isMultpleUsersMode())
                    exp_order_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);

            exp_order_bumped_in_other_expo_station(kds, kds.getSupportDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
                if (kds.isMultpleUsersMode())
                    exp_order_bumped_in_other_expo_station(kds, kds.getSupportDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);

           // }
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary backup is online or offline.
            exp_order_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
            if (kds.isMultpleUsersMode())
                exp_order_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);


        }
        else if (kds.isQueueStation() || kds.isQueueExpo())
        {
            exp_order_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
        }
        else if (kds.isTrackerStation())
        {
            tt_receive_exp_order_bumped_notification(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
        }
        else if (kds.isExpeditorStation() || kds.isRunnerStation() || kds.isSummaryStation()) //kpp1-286
        {
            //mirror do same work, kpp1-286
            if ( kds.getStationsConnections().getRelations().isMyMirrorStation(fromStationID))
            {
                exp_order_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
                if (kds.isMultpleUsersMode())
                    exp_order_bumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);

            }

        }

        else
        { //I am common expo, it is impossible geting this message.


        }
        tt_checkAllItemsBumped(kds, order);
        //sync to others
        sync_with_mirror(kds, command.getCode(), order, null);
        sync_with_backup(kds, command.getCode(), order, null);
        sync_with_queue(kds, command.getCode(), order, null, strXml);



    }

    static public  boolean exp_order_unbumped_in_other_expo_station(KDS kds, KDSDBCurrent db,KDSDataOrders orders,  String fromStationID, String fromIP, KDSDataOrder order, boolean bUpdateShowingOrders)
    {

        KDSDataOrder orderExisted = orders.getOrderByName(order.getOrderName());
        //it this order is existed, don't do anyting
        if (orderExisted != null) {
            return true;
        }
        String orderName = order.getOrderName();
        String bumpedGuid = db.orderGetBumpedGuidFromName(orderName);
        if (bumpedGuid.isEmpty())
            return false;

        db.orderSetBumped(bumpedGuid, false);

        //update its items remote bumped field
        exp_order_unbumped_in_other_station(kds, db, orders, fromStationID, fromIP, order);

        tt_checkAllItemsBumped(kds, order);

        if (bUpdateShowingOrders) {
            orderExisted = db.orderGet(bumpedGuid);

            orders.addComponent(orderExisted);
            kds.refreshView();
        }
        return true;

    }
    static public void exp_sync_expo_order_unbumped(KDS kds,KDSXMLParserCommand command)
    {



        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strXml.isEmpty())
            return;
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(kds.getStationID(), strXml);

        if (order == null)
            return;

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station

            exp_order_unbumped_in_other_expo_station(kds, kds.getSupportDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order, false);
            exp_order_unbumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order, true);
            if (kds.isMultpleUsersMode()) {
                exp_order_unbumped_in_other_expo_station(kds, kds.getSupportDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order, false);
                exp_order_unbumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order, true);
            }

        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary backup is online or offline.
            exp_order_unbumped_in_other_expo_station(kds, kds.getCurrentDB(),kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order, true);
            if (kds.isMultpleUsersMode())
                exp_order_unbumped_in_other_expo_station(kds, kds.getCurrentDB(),kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order, true);


        }
        else if (kds.isQueueStation() || kds.isTrackerStation() || kds.isQueueExpo())
        {
            exp_order_unbumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order, true);
        }

        else if (kds.isExpeditorStation() ||kds.isRunnerStation()  || kds.isSummaryStation()) //kpp1-286
        {
            //mirror do same work, kpp1-286
            if ( kds.getStationsConnections().getRelations().isMyMirrorStation(fromStationID))
            {
                exp_order_unbumped_in_other_expo_station(kds, kds.getCurrentDB(),kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order, true);
                if (kds.isMultpleUsersMode())
                    exp_order_unbumped_in_other_expo_station(kds, kds.getCurrentDB(),kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order, true);

            }

        }

        else
        { //I am common station

        }
        tt_checkAllItemsBumped(kds, order);
        //sync to others
        sync_with_mirror(kds, command.getCode(), order, null);
        sync_with_backup(kds, command.getCode(), order, null);
        sync_with_queue(kds, command.getCode(), order, null, strXml);



    }


    /**
     * Item was bumped in other station.
     * The other stations maybe is normal or exp
     * @param kds
     * @param command
     * @return
     *  order guid
     */
    static public  String exp_sync_item_bumped(KDS kds,  KDSXMLParserCommand command, ArrayList<KDSDataItem> arChangedItems)
    {
        String strOrderName = command.getParam("P0", "");
        String strItemName = command.getParam("P1", "");

        if (strOrderName.isEmpty() ||
                strItemName.isEmpty()    )
            return "";
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");

        //check user A
        KDSDataOrder orderA = kds.getUsers().getUserA().getOrders().getOrderByName(strOrderName);

        KDSDataItem itemA = null;
        if (orderA != null)
            itemA = orderA.getItems().getItemByName(strItemName);
        //check user B
        KDSDataOrder orderB = null;
        KDSDataItem itemB = null;
        if (kds.isMultpleUsersMode())
        {
            orderB = kds.getUsers().getUserA().getOrders().getOrderByName(strOrderName);
            if (orderB != null)
                itemB = orderB.getItems().getItemByName(strItemName);
        }

        if (itemA == null && itemB == null)
            return "";

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.
                if (itemA != null)
                {
                    if (!exp_item_bumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                        return "";
                }
                if (itemB != null)
                {
                    if (!exp_item_bumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                        return "";
                }

                //don't show them
            }
            else
            { //primary is offline now, svae to current database.
                if (itemA != null) {
                    if (!exp_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                        return "";
                }
                if (itemB != null)
                {
                    if (!exp_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                        return "";
                }
            }
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryWhoUseMeAsMirrorActive())
            {
                if (itemA != null) {
                    if (!exp_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                        return "";
                }
                if (itemB != null)
                {
                    if (!exp_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                        return "";
                }
            }
            else
            {
                if (itemA != null) {
                    if (!exp_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                        return "";
                }
                if (itemB != null)
                {
                    if (!exp_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                        return "";
                }
            }

        }
        else
        { //I am common station
            //check if current database contains this order.
//            if (! exp_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getOrders(), fromStationID, fromIP, order, item))
//                return false;
            if (itemA != null) {
                if (!exp_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                    return "";
                if (arChangedItems != null)
                    arChangedItems.add(itemA);
            }
            if (itemB != null)
            {
                if (!exp_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                    return "";
                if (arChangedItems != null)
                    arChangedItems.add(itemB);
            }
        }

        //sync to others
        if (itemA != null) {
            tt_checkAllItemsBumped(kds, orderA);
            sync_with_mirror(kds, command.getCode(), orderA, itemA);
            sync_with_backup(kds, command.getCode(), orderA, itemA);
            sync_with_queue(kds, command.getCode(), orderA, itemA, "");
        }
        else if (itemB != null)
        {
            tt_checkAllItemsBumped(kds, orderB);
            sync_with_mirror(kds, command.getCode(), orderB, itemB);
            sync_with_backup(kds, command.getCode(), orderB, itemB);
            sync_with_queue(kds, command.getCode(), orderB, itemB, "");
        }

        if (kds.isExpeditorStation() || kds.isRunnerStation() || kds.isSummaryStation())
        {
            if (orderA != null && orderA.isItemsAllBumpedInExp())
                kds.getSoundManager().playSound(KDSSettings.ID.Sound_expo_order_complete);
            if (orderB != null && orderB.isItemsAllBumpedInExp())
                kds.getSoundManager().playSound(KDSSettings.ID.Sound_expo_order_complete);
        }
        if (orderA != null)
            return orderA.getGUID();
        else
        {
            if (orderB != null)
                return orderB.getGUID();
        }
        return "";

    }


    static public  void exp_sync_expo_item_unbumped(KDS kds, KDSXMLParserCommand command)
    {
        String strOrderName = command.getParam("P0", "");
        String strItemName = command.getParam("P1", "");

        if (strOrderName.isEmpty() ||
                strItemName.isEmpty()    )
            return;
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");

        //user A
        KDSDataOrder orderA = kds.getUsers().getUserA().getOrders().getOrderByName(strOrderName);
        KDSDataItem itemA = null;
        if (orderA != null)
            itemA = orderA.getItems().getItemByName(strItemName);

        //user B
        KDSDataOrder orderB =null; //kds.getUsers().getUserB().getOrders().getOrderByName(strOrderName);
        KDSDataItem itemB = null;
        if (kds.isMultpleUsersMode()) {
            orderB = kds.getUsers().getUserB().getOrders().getOrderByName(strOrderName);
            if (orderB != null)
                itemB = orderB.getItems().getItemByName(strItemName);
        }

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
//            if (kds.getStationsConnections().isPrimaryBackupActive())
//            {//actived, save order to "support" database.
                if (itemA != null) {
                    exp_item_unbumped_in_other_expo_station(kds, kds.getSupportDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA);
                    exp_item_unbumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA);
                }
                if (itemB != null) {
                    exp_item_unbumped_in_other_expo_station(kds, kds.getSupportDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB);
                    exp_item_unbumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB);
                }

        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary backup is online or offline.
//            if (kds.getStationsConnections().isPrimaryMirrorActive())
//            {
                if (itemA != null)
                    exp_item_unbumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA);
            if (itemB != null)
                exp_item_unbumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB);


        }
        else if (kds.isQueueStation()|| kds.isTrackerStation() || kds.isQueueExpo())
        {
            if (itemA != null)
                exp_item_unbumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA);
            if (itemB != null)
                exp_item_unbumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB);
        }

        else if (kds.isExpeditorStation() || kds.isRunnerStation() || kds.isSummaryStation()) //kpp1-286
        {
            //mirror do same work, kpp1-286
            if ( kds.getStationsConnections().getRelations().isMyMirrorStation(fromStationID))
            {
                if (itemA != null)
                    exp_item_unbumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA);
                if (itemB != null)
                    exp_item_unbumped_in_other_expo_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB);

            }

        }

        else
        { //I am common station

        }

        //sync to others
        if (itemA != null) {
            tt_checkAllItemsBumped(kds, orderA);
            sync_with_mirror(kds, command.getCode(), orderA, itemA);
            sync_with_backup(kds, command.getCode(), orderA, itemA);
            sync_with_queue(kds, command.getCode(), orderA, itemA, "");
        }
        else if (itemB != null)
        {
            tt_checkAllItemsBumped(kds, orderB);
            sync_with_mirror(kds, command.getCode(), orderB, itemB);
            sync_with_backup(kds, command.getCode(), orderB, itemB);
            sync_with_queue(kds, command.getCode(), orderB, itemB, "");
        }

    }

    /**
     * order was canceled by the xml command.
     * @param command
     */
    static public  void exp_sync_order_canceled(KDS kds, KDSXMLParserCommand command)
    {


        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strXml.isEmpty())
            return;
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(kds.getStationID(), strXml);

        if (order == null)
            return;

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.

                orderCancel(kds, kds.getSupportDB(), order);
                //don't show them
            }
            else
            { //primary is offline now, svae to current database.
                orderCancel(kds, kds.getCurrentDB(), order);
            }
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryWhoUseMeAsMirrorActive())
            {
                orderCancel(kds, kds.getCurrentDB(), order);
            }
            else
            {
                orderCancel(kds, kds.getCurrentDB(), order);
            }

        }
        else
        { //I am common station
            //check if current database contains this order.
            orderCancel(kds, kds.getCurrentDB(), order);
        }
        tt_checkAllItemsBumped(kds, order);
        kds.refreshView();


    }

    static public  void exp_sync_order_modified(KDS kds, KDSXMLParserCommand command)
    {
        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strXml.isEmpty())
            return;
        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(kds.getStationID(), strXml);
        if (order == null)
            return;

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.
                kds.getSupportDB().orderInfoModify(order);//.orderModify(order);
                //don't show them
            }
            else
            { //primary is offline now, svae to current database.
                orderInfoModify(kds.getUsers().getUserA(), order, true); //don't check add-on
                if (kds.isMultpleUsersMode())
                    orderInfoModify(kds.getUsers().getUserB(), order, true); //don't check add-on
            }
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary backup is online or offline.
//            if (kds.getStationsConnections().isPrimaryMirrorActive())
//            {
//                orderAdd(kds, order, false);
//            }
//            else
//            {
//                orderAdd(kds, order, false);
//            }
            orderInfoModify(kds.getUsers().getUserA(), order, true);
            if (kds.isMultpleUsersMode())
                orderInfoModify(kds.getUsers().getUserB(), order, true);

        }
        else
        { //I am common station, I am a expeditor.
            //check if current database contains this order.
            orderInfoModify(kds.getUsers().getUserA(), order, true);
            if (kds.isMultpleUsersMode())
                orderInfoModify(kds.getUsers().getUserB(), order, true);
        }

        tt_checkAllItemsBumped(kds, order);
        //sync to others
        sync_with_mirror(kds, command.getCode(), order, null);
        sync_with_backup(kds, command.getCode(), order, null);
        sync_with_queue(kds, command.getCode(), order, null, "");
    }

    /**
     * format:
     *  <KDSCommand>
     *      <Code></Code>
     *      <Param Station="1" IP="192.168.1.1" MAC="092341823-48">
     *             Order xml....
     *      </Param>
     *  <KDSCommand>
     *
     *      *      */
    /**
     * receive the sync command, check if I am backup/mirror station,
     * @param command
     */
    static public KDSDataOrder exp_sync_order_new(KDS kds, KDSXMLParserCommand command, ArrayList<Boolean> ordersExisted, ArrayList<KDSDataOrder> ordersChanged)
    {
        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strXml.isEmpty())
            return null;
        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(kds.getStationID(), strXml);

        if (order == null)
            return null;
        ordersExisted.add( (kds.getUsers().getOrderByName(order.getOrderName()) != null));


        KDSDataOrder  changedOrder = null;
        ArrayList<KDSDataOrder> changedOrders = null;

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station


            { //primary is offline now, svae to current database.
                if (kds.isSingleUserMode()) {
                    changedOrder = func_orderAdd(kds.getUsers().getUserA(), order, strXml, false, false, false, true); //don't check add-on
                    ordersChanged.add(changedOrder);
                }
                else
                {
                    changedOrders = kds.getUsers().users_orderAdd(order, strXml,false, false, true);
                    ordersChanged.addAll(changedOrders);
                }
                kds.getCurrentDB().orderSetAllFromPrimaryOfBackup(true);
            }

        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station

            if (kds.isSingleUserMode()) {
                changedOrder = func_orderAdd(kds.getUsers().getUserA(), order, strXml, false, false, false, true);
                ordersChanged.add(changedOrder);
            }
            else {
                changedOrders = kds.getUsers().users_orderAdd(order, strXml, false, false, true);
                ordersChanged.addAll(changedOrders);
            }

        }
        else
        { //I am common station, I am a expeditor.
            //check if current database contains this order.
//            if (isThisOrderJustBeenAutoBump(kds, order.getOrderName()))
//                return; //in one hour, we don't accept same order name. When auto bump enabled, if expo has bump given order,this station_add_new will cause add a new same name one.
//                          //I have to fix this bug, in 24 stations "coke" branch.

            if (kds.isSingleUserMode()) {
                changedOrder = func_orderAdd(kds.getUsers().getUserA(), order, strXml, false, false, false, true);
                ordersChanged.add(changedOrder);
            }
            else {
                changedOrders = kds.getUsers().users_orderAdd(order, strXml, false, false, true);
                ordersChanged.addAll(changedOrders);
            }
        }

        tt_checkAllItemsBumped(kds, order);
        //sync to others
        sync_with_mirror(kds, command.getCode(), order, null);
        sync_with_backup(kds, command.getCode(), order, null);
        sync_with_queue(kds, command.getCode(), order, null, strXml);
        //change order guid and start time for expo web backoffice usage.
        //kpp1-267, don't need these code. New order has been added to expo before call this function.
//        KDSDataOrder orderChanged = null;
//        if (changedOrder != null)
//            orderChanged = changedOrder;
//        else
//        {
//            if (changedOrders != null)
//            {
//                if (changedOrders.size() >0)
//                    orderChanged = changedOrders.get(0);
//            }
//        }
//        order.setGUID(orderChanged.getGUID());
//        order.setStartTime(orderChanged.getStartTime());

        return order;
    }

    /**
     * changed in others station, expo get this message now.
     * @param kds
     * @param command
     */
    static public void exp_sync_schedule_item_ready_qty_changed(KDS kds, KDSXMLParserCommand command)
    {
        KDSStationNormal.normal_sync_schedule_item_ready_qty_changed(kds, command);


    }

    /**
     *
     * @param command
     */
    static public  void queue_sync_expo_order_ready_unready(KDS kds, KDSXMLParserCommand command)
    {
        String orderName = command.getParam(KDSConst.KDS_Str_Param, "");
        if (orderName.isEmpty())
            return;
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");

        KDSDataOrders orders = kds.getUsers().getUserA().getOrders();

        KDSDataOrder order = orders.getOrderByName(orderName);
        if (order != null)
        {
            if (command.getCode() == KDSXMLParserCommand.KDSCommand.Queue_Ready) {
                kds.getCurrentDB().orderSetQueueReady(order.getGUID(), true);
                order.setQueueReady(true);
                kds.getCurrentDB().orderUpdateQueueStateTime(order.getGUID());//2.0.35
            }
            else {
                order.setQueueReady(false);
                kds.getCurrentDB().orderSetQueueReady(order.getGUID(), false);
                kds.getCurrentDB().orderUpdateQueueStateTime(order.getGUID());//2.0.35
            }
            kds.refreshView();
        }
        if (order == null) return;

        //sync to others
        sync_with_mirror(kds, command.getCode(), order, null);
        sync_with_backup(kds, command.getCode(), order, null);
        sync_with_queue(kds, command.getCode(), order, null, "");



    }

    /**
     *
     * @param kds
     * @param command
     */
    static public void exp_sync_station_cook_started(KDS kds, KDSXMLParserCommand command)
    {
        String orderName = command.getParam(KDSConst.KDS_Str_Param, "");
        KDSDataOrders orders = kds.getUsers().getUserA().getOrders();

        KDSDataOrder order = orders.getOrderByName(orderName);
        if (order == null) return;
        if (order != null)
        {
            order.setCookState(KDSDataOrder.CookState.Started);
            kds.getCurrentDB().orderSetCookState(order.getGUID(), KDSDataOrder.CookState.Started);
        }

        if (kds.isMultpleUsersMode())
        {
            KDSDataOrders ordersB = kds.getUsers().getUserB().getOrders();

            KDSDataOrder orderB = ordersB.getOrderByName(orderName);
            if (orderB == null) return;
            if (orderB != null)
            {
                orderB.setCookState(KDSDataOrder.CookState.Started);
                kds.getCurrentDB().orderSetCookState(orderB.getGUID(), KDSDataOrder.CookState.Started);
            }
        }
        kds.refreshView();


        //sync to others
        sync_with_mirror(kds, command.getCode(), order, null);
        sync_with_backup(kds, command.getCode(), order, null);
        sync_with_queue(kds, command.getCode(), order, null, "");
    }
    /**
     *
     * @param command
     */
    static public  void queue_sync_expo_order_pickup(KDS kds, KDSXMLParserCommand command)
    {
        String orderName = command.getParam(KDSConst.KDS_Str_Param, "");
        if (orderName.isEmpty())
            return;
        if (kds.getSettings().getBoolean(KDSSettings.ID.Queue_only_auto_bump))
        {//only the auto bumping can bump order in queue station.
            return;
        }
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");

        KDSDataOrders orders = kds.getUsers().getUserA().getOrders();

        KDSDataOrder order = orders.getOrderByName(orderName);
        if (order == null) return;
        if (order != null)
        {
            orderBump(kds.getUsers().getUserA(), order.getGUID(), false);

            kds.refreshView();
        }


        //sync to others
        sync_with_mirror(kds, command.getCode(), order, null);
        sync_with_backup(kds, command.getCode(), order, null);
        sync_with_queue(kds, command.getCode(), order, null, "");



    }

//    /*
//       in one hour, we don't accept same order name.
//       It is for Station_add_new_order command.
//    */
//    static private boolean isThisOrderJustBeenAutoBump(KDS kds, String orderName)
//    {
//        String guid =  kds.getCurrentDB().orderGetBumpedGuidFromName(orderName);
//        if (guid.isEmpty()) return false;
//        Date dt = kds.getCurrentDB().orderGetBumpedTime(guid);
//        TimeDog td = new TimeDog(dt);
//        return (!td.is_timeout(1 * 60 * 60 * 1000) );//one hour
//
//
//    }

}
