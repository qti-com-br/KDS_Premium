package com.bematechus.kds;

import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXMLParserCommand;
import com.bematechus.kdslib.PrepSorts;
import com.bematechus.kdslib.TimeDog;

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
//    public void prep_other_station_item_bumped2(String orderName,String itemName)
//    {
//
//
//        KDSDataOrder order = m_ordersDynamic.getOrderByName(orderName);
//        if (order == null) return;
////        String lastCategory = "";
////        if (getKDS().isRunnerStation())
////        {
////            PrepSorts.PrepItem prepItem = order.smart_get_sorts().findItem(itemName);
////            lastCategory = prepItem.Category;
////        }
//        PrepSorts.PrepItem maxItem = PrepSorts.prep_other_station_item_bumped(order, itemName);
//        if (maxItem != null) {
//            getCurrentDB().prep_set_real_started_time(order.getGUID(), maxItem.ItemName, maxItem.RealStartTime);
//        }
//        getCurrentDB().prep_set_item_finished(order.getGUID(), itemName, true);
//
//        //kp1-25, notify runner's child, a new category started.
//        if (getKDS().isRunnerStation()) //I am a Runner
//        {
//            String categoryDescription = maxItem.Category;
//            if (!order.smart_get_sorts().runnerCategoryIsShowing(categoryDescription))
//            {
//                String lastCategory = order.smart_get_sorts().runnerGetLastShowingCategory();
//                ArrayList<String> allSameCatDelayCategories = order.smart_get_sorts().runnerGetAllSameCatDelayCategories(lastCategory);
//                boolean bFitFinishedCondition = false;
//                if (getKDS().getSettings().getBoolean(KDSSettings.ID.Runner_confirm_bump))
//                { //the remote prep station must bump item first
////                    bFitFinishedCondition = (order.smartCategoryItemsLocalFinished(lastCategory) &&
////                                                order.smartCategoryItemsRemoteFinished(lastCategory) );
//                    bFitFinishedCondition = (order.smartCategoryItemsLocalFinished(allSameCatDelayCategories) &&
//                                            order.smart_get_sorts().allCategoriesItemsFinished(allSameCatDelayCategories));
//                                                //order.smartCategoryItemsRemoteFinished(allSameCatDelayCategories) );
//                }
//                else
//                { //don't care remote station bumping
//                    //bFitFinishedCondition = order.smartCategoryItemsLocalFinished(lastCategory);
//                    //bFitFinishedCondition = (order.smartCategoryItemsLocalFinished(allSameCatDelayCategories) ||
//                    //                        order.smartCategoryItemsRemoteFinished(allSameCatDelayCategories) );
//                    //check smart items, as local order was not updated when this function called.
//                    bFitFinishedCondition = order.smart_get_sorts().allCategoriesItemsFinished(allSameCatDelayCategories);
//                }
//                if (lastCategory.isEmpty() || bFitFinishedCondition )
//                {
//                    //order.smart_get_sorts().runnerGetShowingCategory().add(categoryDescription);
//                    allSameCatDelayCategories = order.smart_get_sorts().runnerGetAllSameCatDelayCategories(categoryDescription);
//
//                    //order.smart_get_sorts().runnerAddShowingCategory(categoryDescription);
//                    order.smart_get_sorts().runnerAddShowingCategories(allSameCatDelayCategories);
//                    //getCurrentDB().smartRunnerCategoryAddShowingCategory(order.getGUID(), categoryDescription);
//                    getCurrentDB().smartRunnerCategoryAddShowingCategories(order.getGUID(), allSameCatDelayCategories);
//                    //KDSStationFunc.sync_with_stations_use_me_as_expo(getKDS(), KDSXMLParserCommand.KDSCommand.Runner_show_category, order, null, categoryDescription);
//                    KDSStationFunc.sync_with_stations_use_me_as_expo(getKDS(),
//                                    KDSXMLParserCommand.KDSCommand.Runner_show_category,
//                                    order, null,
//                                    categoriesToString(allSameCatDelayCategories));
//                }
//            }
//        }
//
//
////        PrepSorts.PrepItem prepItem = order.smart_get_sorts().findItem(itemName);
////        if (prepItem == null) return;
////        prepItem.setFinished(true);//, order.getDurationSeconds());
////        if (prepItem.PrepTime >0 || prepItem.ItemDelay >0) { //kpp1-322, add this condition
////            if (order.smart_get_sorts().isMaxCategoryTimeItem(itemName)) {
////                PrepSorts.PrepItem maxItem = order.smart_get_sorts().sort();
////                if (maxItem != null) {
////                    maxItem.RealStartTime = order.getDurationSeconds() + (int)(maxItem.ItemDelay * 60); //kpp1-417, make delay time must been done.
////                    getCurrentDB().prep_set_real_started_time(order.getGUID(), maxItem.ItemName, maxItem.RealStartTime);
////                }
////            }
////        }
////        getCurrentDB().prep_set_item_finished(order.getGUID(), itemName, true);
//
//
//    }

    public void smart_other_station_item_bumped(String orderName,String itemName)
    {


        KDSDataOrder order = m_ordersDynamic.getOrderByName(orderName);
        if (order == null) return;
        PrepSorts.PrepItem maxItem = PrepSorts.smart_other_station_item_bumped(order, itemName);
        if (maxItem != null) {
            getCurrentDB().smart_set_real_started_time(order.getGUID(), maxItem.ItemName, maxItem.RealStartTime);
        }
        getCurrentDB().smart_set_item_finished(order.getGUID(), itemName, true, order.getStartTime());

        if (maxItem == null) return ; //kp-90
        //kp1-25, notify runner's child, a new category started.
        if (getKDS().isRunnerStation()) //I am a Runner
        {
            //String categoryDescription = maxItem.Category;
            //if (!order.smart_get_sorts().runnerCategoryIsShowing(categoryDescription))
            if (!order.smart_get_sorts().runnerIsShowingCatDelay(maxItem.CategoryDelay))
            {
                //String lastCatDelay = order.smart_get_sorts().runnerGetLastShowingCatDelay();
                //float fltLastCatDelay = KDSUtil.convertStringToFloat(lastCatDelay, 0);
                float fltLastCatDelay = order.smart_get_sorts().runnerGetLastShowingCatDelay();
                ArrayList<PrepSorts.PrepItem> allSameCatDelayItems = order.smart_get_sorts().runnerGetAllSameCatDelayItems(fltLastCatDelay);
                boolean bFitFinishedCondition = false;
                if (getKDS().getSettings().getBoolean(KDSSettings.ID.Runner_confirm_bump))
                { //the remote prep station must bump item first
//                    bFitFinishedCondition = (order.smartCategoryItemsLocalFinished(lastCategory) &&
//                                                order.smartCategoryItemsRemoteFinished(lastCategory) );
                    bFitFinishedCondition = (order.smartRunnerSameCatDelayItemsLocalFinished(allSameCatDelayItems) &&
                            order.smart_get_sorts().allSameCatDelayItemsFinished(allSameCatDelayItems));
                    //order.smartCategoryItemsRemoteFinished(allSameCatDelayCategories) );
                }
                else
                { //don't care remote station bumping
                    //bFitFinishedCondition = order.smartCategoryItemsLocalFinished(lastCategory);
                    //bFitFinishedCondition = (order.smartCategoryItemsLocalFinished(allSameCatDelayCategories) ||
                    //                        order.smartCategoryItemsRemoteFinished(allSameCatDelayCategories) );
                    //check smart items, as local order was not updated when this function called.
                    bFitFinishedCondition = order.smart_get_sorts().allSameCatDelayItemsFinished(allSameCatDelayItems);
                }
                //if (lastCatDelay.isEmpty() || bFitFinishedCondition )
                if ( bFitFinishedCondition )
                {
                    //order.smart_get_sorts().runnerGetShowingCategory().add(categoryDescription);
                    float nextCatDelay = maxItem.CategoryDelay;

                    //allSameCatDelayCategories = order.smart_get_sorts().runnerGetAllSameCatDelayCategories(categoryDescription);

                    //order.smart_get_sorts().runnerAddShowingCategory(categoryDescription);
                    TimeDog td = new TimeDog(order.getStartTime());
                    long ms = td.duration();
                    order.smart_get_sorts().runnerSetLastShowingCatDelay(nextCatDelay, ms);
                    //getCurrentDB().smartRunnerCategoryAddShowingCategory(order.getGUID(), categoryDescription);
                    getCurrentDB().runnerSetLastShowingCatDelay(order.getGUID(), nextCatDelay, ms);
                    //KDSStationFunc.sync_with_stations_use_me_as_expo(getKDS(), KDSXMLParserCommand.KDSCommand.Runner_show_category, order, null, categoryDescription);
                    KDSStationFunc.sync_with_stations_use_me_as_expo(getKDS(),
                            KDSXMLParserCommand.KDSCommand.Runner_show_category,
                            order, null,
                            KDSUtil.convertFloatToString(nextCatDelay));
                            //categoriesToString(allSameCatDelayCategories));
                }
            }
        }


    }


    private String categoriesToString(ArrayList<String> categories)
    {
        String s = "";
        for (int i=0; i< categories.size(); i++)
        {
            if (!s.isEmpty())
                s += "\n";
            s += categories.get(i);

        }
        return s;
    }

    /**
     * rev.:
     *  kpp1-417, move code to prepsorts class
     * @param orderName
     * @param itemName
     */
    public void smart_other_station_item_unbumped(String orderName,String itemName)
    {
        KDSDataOrder order = m_ordersDynamic.getOrderByName(orderName);
        if (order == null) return;
        ArrayList<PrepSorts.PrepItem> ar = PrepSorts.smart_other_station_item_unbumped(order, itemName);
        if (ar != null) {
            for (int i = 0; i < ar.size(); i++) {
                getCurrentDB().smart_set_real_started_time(order.getGUID(), ar.get(i).ItemName, 0);
            }
        }
        getCurrentDB().smart_set_item_finished(order.getGUID(), itemName, false, order.getStartTime());

//        PrepSorts.PrepItem prepItem = order.smart_get_sorts().findItem(itemName);
//        if (prepItem == null) return;
//        prepItem.setFinished(false);//.finished = false;
//        //if (order.smart_get_sorts().isMaxCategoryTimeItem(itemName))
//        PrepSorts.PrepItem maxItem = order.smart_get_sorts().sort();
//        if (maxItem != null && maxItem == prepItem)
//        {//we just restore old max item
//            ArrayList<PrepSorts.PrepItem> ar = order.smart_get_sorts().reset_real_start_time(maxItem);
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

    public ArrayList<String> getAutoUnparkOrdersGuid()
    {
        ArrayList<String> ar = new ArrayList<>();
        for(int i=0; i< getParkedOrders().getCount(); i++)
        {
            if (getParkedOrders().get(i).isUnparkTime())
                ar.add(getParkedOrders().get(i).getGUID());
        }
        return ar;
    }

    /**
     * KP-121 manually start cook

     * @param orderGuid
     * @param itemGuid
     * @return
     *  True: item changed to cook.
     *  false: do nothing.
     */
    public boolean runnerStartItemManually( String orderGuid,String itemGuid)
    {
        KDSDataOrder order =  this.getKDS().getUsers().getOrderByGUID(orderGuid);
        if (order == null)
            return false;
        KDSDataItem item = null;
        if (itemGuid.isEmpty())
        {
            PrepSorts.PrepItem smartNextItem = order.smart_get_sorts().sort();
            if (smartNextItem != null)
            {
                item = order.getItems().getItemByName(smartNextItem.ItemName);
            }
            else
                return false;
        }
        else {
            item = order.getItems().getItemByGUID(itemGuid);
            if (item == null) return false;
        }
        String itemName = item.getItemName();

        PrepSorts.PrepItem smartItem = order.smart_get_sorts().findItem(itemName);
        if (smartItem.ItemStartedManually)
            return false;
        String startedItemNames = "";
        if (itemGuid.isEmpty()) {
            //start next group
            ArrayList<PrepSorts.PrepItem> ar = order.smart_get_sorts().runnerGetAllSameCatDelayItems(smartItem.CategoryDelay);

            for (int i = 0; i < ar.size(); i++) {
                smartItem = ar.get(i);
                smartItem.ItemStartedManually = true;
                this.getKDS().getCurrentDB().smart_set_item_started(orderGuid, smartItem.ItemName, true, order.getStartTime());
                if (!startedItemNames.isEmpty())
                    startedItemNames += ",";
                startedItemNames += smartItem.ItemName;

            }
        }
        else
        {
            smartItem.ItemStartedManually = true;
            startedItemNames = smartItem.ItemName;
        }
        KDSStationFunc.sync_with_stations_use_me_as_expo(getKDS(),
                KDSXMLParserCommand.KDSCommand.Runner_start_cook_item,
                order, item, startedItemNames);

        return true;
    }
}
