package com.bematechus.kds;

import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.PrepSorts;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/16 0016.
 */
public class KDSUser {

    public enum USER
    {
        USER_A,
        USER_B,
        USER_ALL,
    }

    //KDSDataOrders m_orders = new KDSDataOrders();
    //
    KDSDataOrdersDynamic m_ordersDynamic = new KDSDataOrdersDynamic();

    KDSDataOrdersDynamic m_ordersTabHidden = new KDSDataOrdersDynamic(); //in tab display mode, we hide these orders.

    KDSDataOrders m_parkedOrders = new KDSDataOrders();


    KDS m_kds = null;

    USER m_user = USER.USER_A;



    public KDSUser(USER user)
    {
        m_user = user;

        m_ordersDynamic.setUserID(user);

    }

    public USER getUserID()
    {
        return m_user;
    }
    public void setKDS(KDS kds)
    {
        m_kds = kds;
    }

    public KDS getKDS()
    {
        return m_kds;
    }

    public void updateSettings(KDSSettings settings)
    {
    //    m_bumpbarFunctions.updateSettings(this.getUserID(), settings);

        //init the sorts
        KDSSettings.OrdersSort orderSort = KDSSettings.OrdersSort.values()[ settings.getInt(KDSSettings.ID.Order_Sort)];

        KDSConst.OrderSortBy sortBy = KDSSettings.getOrderSortBy(orderSort);
        KDSConst.SortSequence sortSequence = KDSSettings.getOrderSortSequence(orderSort);

        boolean bRushFront = settings.getBoolean(KDSSettings.ID.Orders_sort_rush_front);

        //2.0.14
        boolean bFinishedFront = settings.getBoolean(KDSSettings.ID.Orders_sort_finished_front);

        boolean bchanged = false;
        if (sortBy != getOrders().getSortBy() ||
                sortSequence != getOrders().getSortSequence() ||
                bRushFront != getOrders().getMoveRushToFront())
            bchanged = true;
        getOrders().setSortMethod(sortBy, sortSequence, bRushFront, bFinishedFront); //2.0.14
        if (bchanged) {

            this.refreshView( KDS.RefreshViewParam.Focus_First);
        }
    }

//    public KDSBumpBarFunctions getKeyFunctions()
//    {
//        return m_bumpbarFunctions;
//    }

    /**
     *
     * @param order
     * @param bAutoSyncWithOthers
     * @return
     * order added
     */
    public KDSDataOrder user_orderAdd(KDSDataOrder order,String xmlData, boolean bAutoSyncWithOthers,boolean bAutoSyncWithExpo, boolean bRefreshView)
    {
        if (order.getItems().getCount() <=0)
            return null;
        order.setScreen(this.getUserID().ordinal());
        return KDSStationFunc.func_orderAdd(this, order, xmlData,true, bAutoSyncWithOthers,bAutoSyncWithExpo, bRefreshView);
    }

    public KDSDataOrder orderUpdate(KDSDataOrder orderReceived, boolean bAutoSyncWithOthers)
    {
        orderReceived.setScreen(this.getUserID().ordinal());
        return KDSStationFunc.orderUpdate(this, orderReceived, bAutoSyncWithOthers);
    }


    public KDSDataOrdersDynamic getOrders()
    {
        return m_ordersDynamic;
    }

    public void  setOrders(KDSDataOrders orders)
    {
        orders.setSortMethod(m_ordersDynamic.getSortBy(), m_ordersDynamic.getSortSequence(), m_ordersDynamic.getMoveRushToFront(), m_ordersDynamic.getMoveFinishedToFront());


        m_ordersDynamic.copyDataPointer(orders);
    }


    public KDSDBStatistic getStatisticDB()
    {
        return m_kds.getStatisticDB();
    }
    public KDSDBCurrent getCurrentDB()
    {
        if (m_kds == null) return null;
        return m_kds.getCurrentDB();
    }
    public void refreshView()
    {
        m_kds.refreshView(this.getUserID(), KDS.RefreshViewParam.None);
    }

    public void refreshView(KDS.RefreshViewParam nParam)
    {
        m_kds.refreshView(this.getUserID(), nParam);
    }

    public KDSDataOrders getParkedOrders()
    {
        return m_parkedOrders;
    }

    public void setParkedOrders(KDSDataOrders orders)
    {
        m_parkedOrders = orders;
    }

    public int getParkedCount()
    {
        if (this.getCurrentDB() == null) return 0;
        return this.getCurrentDB().getParkedCount(this.getUserID().ordinal());
    }

    public void tabDisplayDestinationFilter(String destShowing)
    {
        for (int i= m_ordersDynamic.getCount()-1; i>=0; i--)
        {
            if (!m_ordersDynamic.get(i).getDestination().equals(destShowing))
            {
                m_ordersTabHidden.addComponent(m_ordersDynamic.get(i));
                m_ordersDynamic.removeComponent(i);
            }
        }

    }

    public void tabDisplayDestinationRestore()
    {

        for (int i= 0; i< m_ordersTabHidden.getCount(); i++)
        {
            if (m_ordersDynamic.getOrderByGUID(m_ordersTabHidden.get(i).getGUID()) == null) //kpp1-319 Destination Orders Duplicating
                m_ordersDynamic.addComponent(m_ordersTabHidden.get(i));

        }
        if (m_ordersTabHidden.getCount() >0) {

            m_ordersTabHidden.clear();
            m_ordersDynamic.sortOrders();
        }

    }

    public int autoBumpParkOrder(int nTimeoutMins)
    {
        ArrayList<String> ar =  m_parkedOrders.findTimeoutOrders(nTimeoutMins, -1, false);

        for (int i=0; i< ar.size(); i++)
        {
            KDSStationFunc.orderParkedBump(this, ar.get(i));
        }
        return ar.size();
    }

    /**
     * Rev.
     *  1. If all items preparation time is 0, this function will cause line items display move order up/up automaticly. (Smart sort enabled).
     *  2. kpp1-417, move code to prepsorts class
     * @param orderName
     * @param itemName
     */
    public void prep_other_station_item_bumped(String orderName,String itemName)
    {


        KDSDataOrder order = m_ordersDynamic.getOrderByName(orderName);
        if (order == null) return;
        PrepSorts.PrepItem maxItem = PrepSorts.prep_other_station_item_bumped(order, itemName);
        if (maxItem != null) {
            getCurrentDB().prep_set_real_started_time(order.getGUID(), maxItem.ItemName, maxItem.RealStartTime);
        }
        getCurrentDB().prep_set_item_finished(order.getGUID(), itemName, true);

//        PrepSorts.PrepItem prepItem = order.prep_get_sorts().findItem(itemName);
//        if (prepItem == null) return;
//        prepItem.setFinished(true);//, order.getDurationSeconds());
//        if (prepItem.PrepTime >0 || prepItem.ItemDelay >0) { //kpp1-322, add this condition
//            if (order.prep_get_sorts().isMaxCategoryTimeItem(itemName)) {
//                PrepSorts.PrepItem maxItem = order.prep_get_sorts().sort();
//                if (maxItem != null) {
//                    maxItem.RealStartTime = order.getDurationSeconds() + (int)(maxItem.ItemDelay * 60); //kpp1-417, make delay time must been done.
//                    getCurrentDB().prep_set_real_started_time(order.getGUID(), maxItem.ItemName, maxItem.RealStartTime);
//                }
//            }
//        }
//        getCurrentDB().prep_set_item_finished(order.getGUID(), itemName, true);


    }

    /**
     * rev.:
     *  kpp1-417, move code to prepsorts class
     * @param orderName
     * @param itemName
     */
    public void prep_other_station_item_unbumped(String orderName,String itemName)
    {
        KDSDataOrder order = m_ordersDynamic.getOrderByName(orderName);
        if (order == null) return;
        ArrayList<PrepSorts.PrepItem> ar = PrepSorts.prep_other_station_item_unbumped(order, itemName);
        if (ar != null) {
            for (int i = 0; i < ar.size(); i++) {
                getCurrentDB().prep_set_real_started_time(order.getGUID(), ar.get(i).ItemName, 0);
            }
        }
        getCurrentDB().prep_set_item_finished(order.getGUID(), itemName, false);

//        PrepSorts.PrepItem prepItem = order.prep_get_sorts().findItem(itemName);
//        if (prepItem == null) return;
//        prepItem.setFinished(false);//.finished = false;
//        //if (order.prep_get_sorts().isMaxCategoryTimeItem(itemName))
//        PrepSorts.PrepItem maxItem = order.prep_get_sorts().sort();
//        if (maxItem != null && maxItem == prepItem)
//        {//we just restore old max item
//            ArrayList<PrepSorts.PrepItem> ar = order.prep_get_sorts().reset_real_start_time(maxItem);
//            for (int i=0; i< ar.size(); i++)
//            {
//                getCurrentDB().prep_set_real_started_time(order.getGUID(), ar.get(i).ItemName, 0);
//            }
//        }
//
//        getCurrentDB().prep_set_item_finished(order.getGUID(), itemName, false);
    }

    public void clearBufferedOrders()
    {
        m_ordersDynamic.clear();
    }
}
