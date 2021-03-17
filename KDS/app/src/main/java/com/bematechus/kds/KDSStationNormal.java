package com.bematechus.kds;

import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSXMLParserCommand;
import com.bematechus.kdslib.KDSXMLParserOrder;
import com.bematechus.kdslib.ScheduleProcessOrder;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/10/14 0014.
 * To do sync function in normal station.
 */
public class KDSStationNormal extends KDSStationFunc {

    ///////////////////

    /**
     *
     * @param kds
     * @param db
     * @param orders
     * @param fromStationID
     * @param fromIP
     * @param order
     *     My local order
     * @param item
     *     my local item
     * @return
     */
    static public  boolean normal_item_bumped_in_other_station(KDS kds, KDSDBCurrent db, KDSDataOrders orders, String fromStationID, String fromIP, KDSDataOrder order, KDSDataItem item)
    {
        if (order == null)
            return false ;

        if (item == null)
            return false;

        String itemName = item.getItemName();

        boolean bStartedByMe = db.startTransaction();

        //KDSDataItem expItem = item;// orderExisted.getItems().getItemByName(itemName);
        item.setLocalBumped(true);
        //item.addRemoteBumpedStation(fromStationID);
        db.itemSetLocalBumped(item);
        db.commitTransaction(bStartedByMe);
        db.endTransaction(bStartedByMe);
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
    static public  boolean normal_item_unbumped_in_other_station(KDS kds, KDSDBCurrent db,KDSDataOrders orders,  String fromStationID, String fromIP, KDSDataOrder order, KDSDataItem item)
    {
        if (order == null)
            return false ;

        if (item == null)
            return false;

        String itemName = item.getItemName();

        boolean bStartedByMe = db.startTransaction();

        //KDSDataItem expItem = item;// orderExisted.getItems().getItemByName(itemName);
        item.setLocalBumped(false);
        //expItem.addRemoteBumpedStation(fromStationID);
        //db.itemSetRemoteBumpedStations(exp);
        db.itemSetLocalBumped(item);
        db.commitTransaction(bStartedByMe);
        db.endTransaction(bStartedByMe);
        kds.refreshView();
        return true;

    }

    /**
     * In normal station, get information that given order was bumped by others station.
     * we should set expeditor station.
     * Mark all items to "bumped" in exp
     * @param db
     *  Save to which db, this is for support and current
     * @param fromStationID
     * @param fromIP
     * @param orderReceived
     *  It just contains ID
     * return order GUID
     */
    static public String normal_order_bumped_in_other_station(KDS kds,KDSDBCurrent db,KDSDataOrders orders,  String fromStationID, String fromIP, KDSDataOrder orderReceived)
    {

        KDSDataOrder order = orders.getOrderByName(orderReceived.getOrderName());
        if (order != null)
        {
            //kpp1-286
            kds.fireOrderBumpedInOther(order.getGUID());
            //
            orders.removeComponent(order);
            db.orderSetBumped(order.getGUID(), true);
            kds.refreshView();
        }
        else
        {
            String orderName = orderReceived.getOrderName();
            String guid = db.orderGetUnbumpedGuidFromName(orderName);
            if (!guid.isEmpty())
                db.orderSetBumped(guid, true);
        }
        if (order != null)
            return order.getGUID();
        return "";


    }

    static public  void normal_order_unbumped_in_other_station(KDS kds,KDSDBCurrent db,KDSDataOrders orders, String fromStationID, String fromIP, KDSDataOrder orderReceived, boolean bUpdateShowingOrders)
    {
        //db.orderg
        KDSDataOrder orderExisted = orders.getOrderByName(orderReceived.getOrderName());
        //it this order is existed, don't do anyting
        if (orderExisted != null) {
            return;
        }
        String orderName = orderReceived.getOrderName();
        String bumpedGuid = db.orderGetBumpedGuidFromName(orderName);
        if (bumpedGuid.isEmpty())
            return;

        db.orderSetBumped(bumpedGuid, false);
        if (bUpdateShowingOrders) {
            orderExisted = db.orderGet(bumpedGuid);
            orders.addComponent(orderExisted);
            kds.refreshView();
        }

    }

    static public void normal_sync_order_new(KDS kds, KDSXMLParserCommand command)
    {
        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strXml.isEmpty())
            return;
        KDSDataOrder orderReceived =(KDSDataOrder) KDSXMLParser.parseXml(kds.getStationID(), strXml);

        if (orderReceived == null)
            return;
        if (orderReceived.getOrderType().equals(KDSDataOrder.ORDER_TYPE_SCHEDULE) )
        {
            ScheduleProcessOrder scheduleOrder = ScheduleProcessOrder.createFromOrder(orderReceived);
            scheduleOrder.setOrderName(orderReceived.getOrderName());//use "changed" name
            orderReceived = scheduleOrder;
        }
        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.
                kds.getSupportDB().orderAdd(orderReceived);
                //don't show them
            }
            else
            { //primary is offline now, svae to current database.
                if (kds.isSingleUserMode())
                    func_orderAdd(kds.getUsers().getUserA(), orderReceived, strXml,false, false, false,true); //don't check add-on
                else
                    kds.getUsers().users_orderAdd(orderReceived, strXml,false, false,true); //TODO: there are some issues !!!, it maybe is not same as primary
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
            if (kds.isSingleUserMode())
                func_orderAdd(kds.getUsers().getUserA(), orderReceived, strXml,false, false,false, true);
            else
                kds.getUsers().users_orderAdd(orderReceived, strXml,false, false, true);

        }
        else
        { //I am common station, I am a expeditor.
            //check if current database contains this order.
            if (kds.isSingleUserMode())
                func_orderAdd(kds.getUsers().getUserA(), orderReceived, strXml,false, false,false, true);
        }

        //sync to others
        sync_with_mirror(kds, command.getCode(), orderReceived, null);
        sync_with_backup(kds, command.getCode(), orderReceived, null);
    }

    static public void normal_sync_order_canceled(KDS kds, KDSXMLParserCommand command)
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
            //if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.

                orderCancel(kds, kds.getSupportDB(), order);
                //don't show them
            }
            //else
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

        //sync to others
        sync_with_mirror(kds, command.getCode(), order, null);
        sync_with_backup(kds, command.getCode(), order, null);

    }

    /**
     *
     * @param kds
     * @param command
     * @return
     *  order name
     */
    static public String normal_sync_order_bumped(KDS kds, KDSXMLParserCommand command)
    {
        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strXml.isEmpty())
            return "";
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        KDSDataOrder order =(KDSDataOrder) KDSXMLParser.parseXml(kds.getStationID(), strXml);

        if (order == null)
            return "";
        String orderGuid = "";
        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
            //the leftover orders maybe existed in current and support at same time.
            orderGuid = normal_order_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
            normal_order_bumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
            if (kds.isMultpleUsersMode())
            {
                String strGuid=  normal_order_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);
                if (!strGuid.isEmpty())
                    orderGuid += ",";
                orderGuid += strGuid;
                normal_order_bumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);
            }

        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary backup is online or offline.
            orderGuid = normal_order_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
            if (kds.isMultpleUsersMode()) {
                String strGuid = normal_order_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);
                if (!strGuid.isEmpty())
                    orderGuid += "," ;
                orderGuid += strGuid;
            }


        }
        else
        { //I am common station
            //check if current database contains this order.
            orderGuid = normal_order_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, order);
            if (kds.isMultpleUsersMode()) {
                String strGuid = normal_order_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, order);
                if (!strGuid.isEmpty())
                    orderGuid += ",";
                orderGuid += strGuid;
            }
        }

        //sync to others
        sync_with_mirror(kds, command.getCode(), order, null);
        sync_with_backup(kds, command.getCode(), order, null);
        //kp-60 Bumping  orders or items from expo not affecting summary
        sync_with_expo(kds, KDSXMLParserCommand.KDSCommand.Station_Bump_Order, order, null);
        return orderGuid;
    }
    static public void normal_sync_order_unbumped(KDS kds, KDSXMLParserCommand command)
    {
        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strXml.isEmpty())
            return;
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");
        KDSDataOrder orderReceived =(KDSDataOrder) KDSXMLParser.parseXml(kds.getStationID(), strXml);

        if (orderReceived == null)
            return;

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.

            normal_order_unbumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderReceived, false);
            normal_order_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderReceived, false);
            if (kds.isMultpleUsersMode()) {
                normal_order_unbumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderReceived, false);
                normal_order_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderReceived, false);
            }


        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary backup is online or offline.

            normal_order_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderReceived, true);
            if (kds.isMultpleUsersMode())
                normal_order_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderReceived, true);



        }
        else
        { //I am common station
            //check if current database contains this order.
            normal_order_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderReceived, true);
            if (kds.isMultpleUsersMode())
                normal_order_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderReceived, true);
            //normal_order_unbumped_in_other_station(kds, kds.getCurrentDB(), kds.getOrders(), fromStationID, fromIP, orderReceived);
        }

        //sync to others
        sync_with_mirror(kds, command.getCode(), orderReceived, null);
        sync_with_backup(kds, command.getCode(), orderReceived, null);
        //kp-60 Bumping  orders or items from expo not affecting summary
        sync_with_expo(kds, KDSXMLParserCommand.KDSCommand.Station_Unbump_Order, orderReceived, null);

    }
    static public void normal_sync_order_modified(KDS kds, KDSXMLParserCommand command)
    {
        String strXml = command.getParam(KDSConst.KDS_Str_Param, "");
        if (strXml.isEmpty())
            return;
        KDSDataOrder orderReceived =(KDSDataOrder) KDSXMLParser.parseXml(kds.getStationID(), strXml);
        if (orderReceived == null)
            return;

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
            //if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.
                kds.getSupportDB().orderInfoModify(orderReceived);//.orderModify(order);
                //don't show them
            }
            //else
            { //primary is offline now, svae to current database.

                orderInfoModify(kds.getUsers().getUserA(), orderReceived, true); //don't check add-on
                if (kds.isMultpleUsersMode())
                    orderInfoModify(kds.getUsers().getUserB(), orderReceived, true); //don't check add-on
            }
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station

            orderInfoModify(kds.getUsers().getUserA(), orderReceived, true);
            if (kds.isMultpleUsersMode())
                orderInfoModify(kds.getUsers().getUserB(), orderReceived, true);

        }
        else
        { //I am common station, I am a expeditor.
            //check if current database contains this order.
            orderInfoModify(kds.getUsers().getUserA(), orderReceived, true);
            if (kds.isMultpleUsersMode())
                orderInfoModify(kds.getUsers().getUserB(), orderReceived, true);
        }

        //sync to others
        sync_with_mirror(kds, command.getCode(), orderReceived, null);
        sync_with_backup(kds, command.getCode(), orderReceived, null);
    }

    /**
     *
     * @param kds
     * @param command
     * @return
     *  order guid
     */
    static public String normal_sync_item_bumped(KDS kds,  KDSXMLParserCommand command,  ArrayList<KDSDataItem> arChangedItems)
    {
        String strOrderName = command.getParam("P0", "");
        String strItemName = command.getParam("P1", "");

        if (strOrderName.isEmpty() ||
                strItemName.isEmpty()    )
            return "";
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");

        KDSDataOrder orderA = null;
        KDSDataItem itemA = null;
        orderA = kds.getUsers().getUserA().getOrders().getOrderByName(strOrderName);
        if (orderA != null)
            itemA = orderA.getItems().getItemByName(strItemName);
        KDSDataOrder orderB = null;
        KDSDataItem itemB = null;
        if (kds.isMultpleUsersMode())
        {
            orderB = kds.getUsers().getUserB().getOrders().getOrderByName(strOrderName);
            if (orderB != null)
                itemB= orderB.getItems().getItemByName(strItemName);
        }
        if (orderA == null && orderB == null)
            return "";
        if (itemA == null && itemB == null)
            return "";

        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
           // if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.

                if (itemA != null) {
                    if (!normal_item_bumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                        return "";
                }
                if (itemB != null) {
                    if (!normal_item_bumped_in_other_station(kds, kds.getSupportDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                        return "";
                }
                //don't show them
            }
           // else
            { //primary is offline now, svae to current database.

                if (itemA != null) {
                    if (!normal_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                        return "";
                }
                if (itemB != null) {
                    if (!normal_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                        return "";
                }
                //if (! normal_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getOrders(), fromStationID, fromIP, order, item))
                //    return false;
            }
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary backup is online or offline.
            if (kds.getStationsConnections().isPrimaryWhoUseMeAsMirrorActive())
            {
                if (itemA != null) {
                    if (!normal_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                        return "";
                }
                if (itemB != null) {
                    if (!normal_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                        return "";
                }
            }
            else
            {
                if (itemA != null) {
                    if (!normal_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                        return "";
                }
                if (itemB != null) {
                    if (!normal_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                        return "";
                }
            }

        }
        else
        { //I am common station
            //check if current database contains this order.
            if (itemA != null) {
                if (!normal_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserA().getOrders(), fromStationID, fromIP, orderA, itemA))
                    return "";
            }
            if (itemB != null) {
                if (!normal_item_bumped_in_other_station(kds, kds.getCurrentDB(), kds.getUsers().getUserB().getOrders(), fromStationID, fromIP, orderB, itemB))
                    return "";
            }
        }

        //sync to others
        if (itemA != null) {
            sync_with_mirror(kds, command.getCode(), orderA, itemA);
            sync_with_backup(kds, command.getCode(), orderA, itemA);
            arChangedItems.add(itemA); //kpp1-407
            return orderA.getGUID();
        }
        else if (itemB != null)
        {
            sync_with_mirror(kds, command.getCode(), orderB, itemB);
            sync_with_backup(kds, command.getCode(), orderB, itemB);
            arChangedItems.add(itemB); //kpp1-407
            return orderB.getGUID();
        }

        return "";
    }

    /**
     *
     * @param kds
     * @param command
     * @return
     *  order guid
     */
    static public String normal_sync_item_unbumped(KDS kds,  KDSXMLParserCommand command, ArrayList<KDSDataItem> arChangedItems)
    {
        String strOrderName = command.getParam("P0", "");
        String strItemName = command.getParam("P1", "");

        if (strOrderName.isEmpty() ||
                strItemName.isEmpty()    )
            return "";
        String fromStationID = command.getParam(KDSConst.KDS_Str_Station, "");
        String fromIP = command.getParam(KDSConst.KDS_Str_IP, "");

        KDSDataOrders orders = null;
        KDSDataOrder order = null;
        KDSDataItem item = null;

        KDSDataOrder orderA = null;
        KDSDataItem itemA = null;
        orderA = kds.getUsers().getUserA().getOrders().getOrderByName(strOrderName);
        if (orderA != null)
            itemA = orderA.getItems().getItemByName(strItemName);


        KDSDataOrder orderB = null;
        KDSDataItem itemB = null;
        if (kds.isMultpleUsersMode())
        {
            orderB = kds.getUsers().getUserB().getOrders().getOrderByName(strOrderName);
            if (orderB != null)
                itemB = orderB.getItems().getItemByName(strItemName);
        }

        if (itemA != null)
        {
            orders = kds.getUsers().getUserA().getOrders();
            order = orderA;
            item = itemA;
        }
        else if (itemB != null)
        {
            orders = kds.getUsers().getUserB().getOrders();
            order = orderB;
            item = itemB;
        }
        else {
            return "";
        }


        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
            //if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.

                normal_item_unbumped_in_other_station(kds, kds.getSupportDB(), orders, fromStationID, fromIP, order, item);
                //don't show them
            }
            //else
            { //primary is offline now, svae to current database.
                normal_item_unbumped_in_other_station(kds, kds.getCurrentDB(),orders, fromStationID, fromIP, order, item);
            }
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station
            //My primary mirror is online or offline.
            if (kds.getStationsConnections().isPrimaryWhoUseMeAsMirrorActive())
            {
                normal_item_unbumped_in_other_station(kds, kds.getCurrentDB(), orders, fromStationID, fromIP, order, item);
            }
            else
            {
                normal_item_unbumped_in_other_station(kds, kds.getCurrentDB(), orders, fromStationID, fromIP, order, item);
            }

        }
        else
        { //I am common station
            //check if current database contains this order.
            normal_item_unbumped_in_other_station(kds, kds.getCurrentDB(), orders, fromStationID, fromIP, order, item);
        }

        //kpp1-407
        arChangedItems.add(item);
        //sync to others
        sync_with_mirror(kds, command.getCode(), order, item);
        sync_with_backup(kds, command.getCode(), order, item);

        if (order != null)
            return order.getGUID();
        return "";
    }

    static public void normal_sync_item_modified(KDS kds,  KDSXMLParserCommand command)
    {
        String strOrderName = command.getParam("P0", "");
        String strItemXml = command.getParam("P1", "");
        if (strItemXml.isEmpty())
            return;
        KDSDataItem item =(KDSDataItem) KDSXMLParserOrder.parseItem( strItemXml);
        if (item == null)
            return;

        //set item's order name
        KDSUser user = null;
        KDSDataOrder order = null;
        KDSDataItem itemOriginal = null;
        KDSDataOrder orderA = null;
        KDSDataOrder orderB = null;
        KDSDataItem itemOriginalA = null;
        KDSDataItem itemOriginalB = null;


        orderA = kds.getUsers().getUserA().getOrders().getOrderByName(strOrderName);
        if (orderA != null) {
            itemOriginalA = orderA.getItems().getItemByName(item.getItemName());
        }

        if (kds.isMultpleUsersMode())
        {
            orderB = kds.getUsers().getUserB().getOrders().getOrderByName(strOrderName);
            if (orderB != null) {
                itemOriginalB = orderB.getItems().getItemByName(item.getItemName());
            }
        }

        if (itemOriginalA == null && itemOriginalB == null) return;
        if (itemOriginalA != null)
        {
            user = kds.getUsers().getUserA();
            order = orderA;
            itemOriginal = itemOriginalA;
        }
        else if (itemOriginalB != null)
        {
            user = kds.getUsers().getUserB();
            order = orderB;
            itemOriginal = itemOriginalB;
        }

        item.setOrderGUID(order.getGUID());
        //itemOriginal = order.getItems().getItemByName(item.getItemName());
        //if (itemOriginal == null)
        //    return;
        item.setGUID(itemOriginal.getGUID());


        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
           // if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.
                kds.getSupportDB().itemModify(item);
                //don't show them
            }
           // else
            { //primary is offline now, svae to current database.
                itemModify(user, item, false); //
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
            itemModify(user, item, false);

        }
        else
        { //I am common station, I am a expeditor.
            //check if current database contains this order.
            itemModify(user,item, false);
        }

        //sync to others
        sync_with_mirror(kds, command.getCode(), order, item);
        sync_with_backup(kds, command.getCode(), order, item);
    }

    static public void normal_sync_schedule_item_ready_qty_changed(KDS kds, KDSXMLParserCommand command)
    {
        String strOrderName = command.getParam("P0", "");
        String strItemXml = command.getParam("P1", "");
        if (strItemXml.isEmpty())
            return;
        KDSDataItem item =(KDSDataItem) KDSXMLParserOrder.parseItem( strItemXml);
        if (item == null)
            return;

        //set item's order name
        KDSUser user = null;
        KDSDataOrder order = null;
        KDSDataItem itemOriginal = null;
        KDSDataOrder orderA = null;
        KDSDataOrder orderB = null;
        KDSDataItem itemOriginalA = null;
        KDSDataItem itemOriginalB = null;


        orderA = kds.getUsers().getUserA().getOrders().getOrderByName(strOrderName);
        if (orderA != null) {
            itemOriginalA = orderA.getItems().getItemByName(item.getItemName());
        }

        if (kds.isMultpleUsersMode())
        {
            orderB = kds.getUsers().getUserB().getOrders().getOrderByName(strOrderName);
            if (orderB != null) {
                itemOriginalB = orderB.getItems().getItemByName(item.getItemName());
            }
        }

        //if (itemOriginalA == null && itemOriginalB == null) return;
        if (itemOriginalA != null)
        {
            user = kds.getUsers().getUserA();
            order = orderA;
            itemOriginal = itemOriginalA;
        }
        else if (itemOriginalB != null)
        {
            user = kds.getUsers().getUserB();
            order = orderB;
            itemOriginal = itemOriginalB;
        }
        if (order == null) {
            order = new KDSDataOrder();
            order.setOrderName(strOrderName);
        }
        //in backup mode, the order maybe is null
        item.setOrderGUID(order.getGUID());
        //itemOriginal = order.getItems().getItemByName(item.getItemName());
        //if (itemOriginal == null)
        //    return;
        if (itemOriginal!=null)
            item.setGUID(itemOriginal.getGUID());



        if (kds.getStationsConnections().getRelations().isBackupStation())
        { //I am backup slave station
            //My primary backup is online or offline.
            // if (kds.getStationsConnections().isPrimaryBackupActive())
            {//actived, save order to "support" database.
                //kds.getSupportDB().schedule_process_item_ready_qty_changed(item);
                kds.getSupportDB().schedule_process_item_ready_qty_changed(strOrderName, item.getItemName(), item.getScheduleProcessReadyQty());

                //don't show them
            }
            // else
            { //primary is offline now, svae to current database.
                if (user != null)
                    schedule_process_item_ready_qty_changed(user, item, false); //
            }
        }
        else if (kds.getStationsConnections().getRelations().isMirrorStation())
        { //I am mirror slave station

            schedule_process_item_ready_qty_changed(user, item, false);
        }
        else
        { //I am common station, I am a expeditor.
            //check if current database contains this order.
            schedule_process_item_ready_qty_changed(user,item, false);
        }

        //sync to others
        if (order != null) {
            sync_with_mirror(kds, command.getCode(), order, item);
            sync_with_backup(kds, command.getCode(), order, item);
        }
    }

}
