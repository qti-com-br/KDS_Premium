package com.bematechus.kds;

import android.util.Log;

import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataItems;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXMLParserCommand;
import com.bematechus.kdslib.SettingsBase;
import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;

/**
 *
 */
public class KDSUsers {

    static final String TAG = "KDSUsers";
    ArrayList<KDSUser> m_users = new ArrayList<>();
    KDS m_kds = null;


    KDSUser.USER m_focusedUser = KDSUser.USER.USER_A;



    public KDSUsers(KDS kds)
    {
        m_kds = kds;
    }

    /**
     *
     * @param bSetAllOrderToUserA
     *      In queue, we need keep all users original user setting.
     *
     */
    public void setSingleUserMode(boolean bSetAllOrderToUserA)
    {
        if (m_users.size() == 1)
            return;
        else if (m_users.size() >1)
            m_users.remove(getUserB());//kpp1-288
        else {
            //m_users.clear();//kpp1-288
            KDSUser user = new KDSUser(KDSUser.USER.USER_A);
            user.setKDS(m_kds);
            m_users.add(user);
        }
        if (bSetAllOrderToUserA) //kpp1-272
            m_kds.getCurrentDB().setAllActiveOrdersToUserA();//KPP1-195
    }
    public void setTwoUserMode()
    {
        if (m_users.size() == 2)
            return;
        m_users.clear();

        KDSUser userA = new KDSUser(KDSUser.USER.USER_A);
        userA.setKDS(m_kds);
        m_users.add(userA);
        KDSUser userB = new KDSUser(KDSUser.USER.USER_B);
        userB.setKDS(m_kds);
        m_users.add(userB);

    }
    public KDSUser getUserA()
    {
        int ncount = m_users.size();
        for (int i=0; i< ncount; i++)
        {
            if (m_users.get(i).getUserID() == KDSUser.USER.USER_A)
                return m_users.get(i);
        }
        return null;
    }

    public KDSUser getUserB()
    {
        int ncount = m_users.size();
        for (int i=0; i< ncount; i++)
        {
            if (m_users.get(i).getUserID() == KDSUser.USER.USER_B)
                return m_users.get(i);
        }
        return null;
    }

    public  void updateSettings(KDSSettings settings)
    {

        int ncount = m_users.size();
        for (int i=0; i< ncount; i++)
        {
            m_users.get(i).updateSettings(settings);

        }

    }

    KDSUser getUser(KDSUser.USER user)
    {
        if (user == KDSUser.USER.USER_A)
            return getUserA();
        else
            return getUserB();
    }


    private ArrayList<KDSDataOrder> filterOrderToUsers(KDSDataOrder orderOriginal)
    {
        ArrayList<KDSDataOrder> ar = new ArrayList<KDSDataOrder>();
        try {

            if (m_users.size() == 1) {//||
//            m_kds.getStationFunction() == SettingsBase.StationFunc.Queue || //kpp1-288
//                m_kds.getStationFunction() == SettingsBase.StationFunc.Queue_Expo) {//single users, kpp1-288
                orderOriginal.setScreen(KDSConst.Screen.SCREEN_A.ordinal());
                ar.add(orderOriginal);
                return ar;
            } else { //multiple users
                String myStationID = m_kds.getStationID();
                KDSDataOrder orderNoScreenAssignment = orderOriginal;
                //
                KDSDataOrder orderUserA = new KDSDataOrder();
                orderOriginal.copyOrderInfoTo(orderUserA);
                orderUserA.createNewGuid();
                orderUserA.setScreen(KDSUser.USER.USER_A.ordinal());
                //
                KDSDataOrder orderUserB = new KDSDataOrder();
                orderOriginal.copyOrderInfoTo(orderUserB);
                orderUserB.createNewGuid();
                orderUserB.setScreen(KDSUser.USER.USER_B.ordinal());
                //check each item.
                KDSDataItems items = orderNoScreenAssignment.getItems();
                int ncount = items.getCount();
                int nindex = 0;
                for (int i = 0; i < ncount; i++) {
                    if (nindex >= items.getCount())
                        break;
                    KDSDataItem item = items.getItem(nindex);
                    if (item.getToStations().isAssigned()) {
                        if (item.getToStations().findScreen(myStationID, KDSConst.Screen.SCREEN_A.ordinal()) != 0) {
                            orderUserA.getItems().addComponent(item);
                            item.setOrderGUID(orderUserA.getGUID());
                            items.removeComponent(item);
                            continue;
                            //nindex ++;
                        } else if (item.getToStations().findScreen(myStationID, KDSConst.Screen.SCREEN_B.ordinal()) != 0) {
                            orderUserB.getItems().addComponent(item);
                            item.setOrderGUID(orderUserB.getGUID());
                            items.removeComponent(item);
                            continue;
                            //nindex ++;
                        }
                    }
                    nindex++;
                }
                //we have find user A, B assigned items, then dispatch the unassigned items
                if (items.getCount() > 0) {//put them to the less busy user.
                    float qtyA = getUserA().getOrders().getTotalQty();
                    float qtyB = getUserB().getOrders().getTotalQty();
                    if (qtyB < qtyA)
                        orderUserB.appendItems(orderNoScreenAssignment);
                    else
                        orderUserA.appendItems(orderNoScreenAssignment);
                }
                ar.add(orderUserA);
                ar.add(orderUserB);

            }

        }
        catch (Exception e)
        {
            //KDSLog.e(TAG, KDSLog._FUNCLINE_() + e.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e );
        }
        return ar;
    }

    /**
     *
     * @param orderOriginal
     * @param bAutoSyncWithOthers
     * @return
     *   return it for printing.
     *   The orders was added.
     */
    /**
     *
     * @param orderOriginal
     * @param xmlData
     * @param bAutoSyncWithOthers
     *  If deliver order to my slave station
     * @param bAutoSyncWithExpo
     *  If deliver order to my expo/tt/queue(Expo type station).
     * @param bRefreshView
     * @return
     * First is user A
     * Second is user B.
     */
    public  ArrayList<KDSDataOrder> users_orderAdd(KDSDataOrder orderOriginal,String xmlData, boolean bAutoSyncWithOthers,boolean bAutoSyncWithExpo, boolean bRefreshView)
    {
        //TimeDog t = new TimeDog();
        ArrayList<KDSDataOrder> usersOrder = filterOrderToUsers(orderOriginal);
        //t.debug_print_Duration("orderAdd1");
        ArrayList<KDSDataOrder> ordersReturn = new  ArrayList<>();
        if (usersOrder.size() == 1) { //kpp1-288
//        if (usersOrder.size() == 1 ||
//            m_kds.getStationFunction() == SettingsBase.StationFunc.Queue ||
//                m_kds.getStationFunction() == SettingsBase.StationFunc.Queue_Expo) {
            KDSDataOrder order =  getUserA().user_orderAdd(usersOrder.get(0),xmlData, bAutoSyncWithOthers,bAutoSyncWithExpo, bRefreshView);
            if (order != null)
                ordersReturn.add(order);
            //t.debug_print_Duration("orderAdd2");
        }
        else if (usersOrder.size() >1)
        {
            KDSDataOrder orderA = getUserA().user_orderAdd(usersOrder.get(0),xmlData, bAutoSyncWithOthers, bAutoSyncWithExpo,bRefreshView);
            KDSDataOrder orderB = getUserB().user_orderAdd(usersOrder.get(1),xmlData, bAutoSyncWithOthers,bAutoSyncWithExpo, bRefreshView);
            if (orderA != null)
                ordersReturn.add(orderA);

            if (orderB != null)
                ordersReturn.add(orderB);
        }
        return ordersReturn;

    }

    public  ArrayList<KDSDataOrder> orderUpdate(KDSDataOrder orderReceived, boolean bAutoSyncWithOthers)
    {
        //TimeDog t = new TimeDog();
        ArrayList<KDSDataOrder> usersOrder = filterOrderToUsers(orderReceived);
        //t.debug_print_Duration("orderAdd1");
        ArrayList<KDSDataOrder> ordersReturn = new  ArrayList<KDSDataOrder>();
        if (usersOrder.size() == 1) {
            KDSDataOrder order =  getUserA().orderUpdate(usersOrder.get(0), bAutoSyncWithOthers);
            if (order != null)
                ordersReturn.add(order);
            //t.debug_print_Duration("orderAdd2");
        }
        else if (usersOrder.size() >1)
        {
            KDSDataOrder orderA = getUserA().orderUpdate(usersOrder.get(0), bAutoSyncWithOthers);
            KDSDataOrder orderB = getUserB().orderUpdate(usersOrder.get(1), bAutoSyncWithOthers);
            if (orderA != null)
                ordersReturn.add(orderA);

            if (orderB != null)
                ordersReturn.add(orderB);
        }
        return ordersReturn;


    }



    public KDSUser.USER orderUnbump(String orderGuid)
    {
        if (getUserA() != null) {
            if (KDSStationFunc.orderUnbump(getUserA(), orderGuid))
                return KDSUser.USER.USER_A;

        }
        if (getUserB() != null)
        {
            if (KDSStationFunc.orderUnbump(getUserB(), orderGuid))
                return KDSUser.USER.USER_B;

        }
        return KDSUser.USER.USER_A;

    }



    public KDSDataOrder getOrderByGUID(String orderGuid)
    {
        if (getUserA() != null) {

            KDSDataOrder order =  getUserA().getOrders().getOrderByGUID(orderGuid);
            if (order != null) return order;
        }
        if (getUserB() != null)
        {
            KDSDataOrder order =  getUserB().getOrders().getOrderByGUID(orderGuid);
            if (order != null) return order;
        }
        return null;
    }

    public KDSDataOrder getOrderByName(String orderName)
    {
        if (getUserA() != null) {

            KDSDataOrder order =  getUserA().getOrders().getOrderByName(orderName);
            if (order != null) return order;
        }
        if (getUserB() != null)
        {
            KDSDataOrder order =  getUserB().getOrders().getOrderByName(orderName);
            if (order != null) return order;
        }
        return null;
    }

    public boolean orderRemove(KDSDataOrder order)
    {
        if (getUserA() != null) {

            getUserA().getOrders().removeComponent(order);
            return true;
        }
        if (getUserB() != null)
        {
            getUserB().getOrders().removeComponent(order);
            return true;
        }
        return false;
    }

    public void ordersClear()
    {
        if (getUserA() != null) {

            getUserA().getOrders().clear();

        }
        if (getUserB() != null)
        {
            getUserB().getOrders().clear();

        }
    }


    public void orderCancel( KDSDataOrder order)
    {
        String orderName = order.getOrderName();
        if (getUserA() != null) {
            KDSDataOrder orderExisted = getUserA().getOrders().getOrderByName(orderName);

            if (orderExisted != null) {

                getUserA().getOrders().removeComponent(orderExisted);
                getUserA().getCurrentDB().orderDelete(orderExisted.getGUID());
            }
        }
        if (getUserB() != null) {
            KDSDataOrder orderExisted = getUserB().getOrders().getOrderByName(orderName);

            if (orderExisted != null) {
                getUserB().getOrders().removeComponent(orderExisted);
                getUserB().getCurrentDB().orderDelete(orderExisted.getGUID());
            }
        }

        KDSStationFunc.sync_with_stations(m_kds, KDSXMLParserCommand.KDSCommand.Station_Cancel_Order, order, null, "");


    }

    /**
     *
     * @param order
     * @param bSyncWithOthers
     * 2010301
     *  As the transtype=modify/delete xml file was send to every station,
     *      stations don't need to sync with others.
     */
    public void orderInfoModify(KDSDataOrder order, boolean bSyncWithOthers)
    {
        if (getUserA() != null) {
          KDSStationFunc.orderInfoModify(getUserA(), order, bSyncWithOthers);

        }
        if (getUserB() != null) {
            KDSStationFunc.orderInfoModify(getUserB(), order,bSyncWithOthers);
        }
    }

    public void switchUser()
    {
        if (getUsersCount() == 1)
            m_focusedUser = KDSUser.USER.USER_A;
        else
        {
            if (m_focusedUser == KDSUser.USER.USER_A)
                m_focusedUser = KDSUser.USER.USER_B;
            else if (m_focusedUser == KDSUser.USER.USER_B)
                m_focusedUser = KDSUser.USER.USER_A;
        }
    }

    public void setFocusedUser(KDSUser.USER userID)
    {
        m_focusedUser = userID;
    }

    public KDSUser getFocusedUser()
    {
        return getUser(m_focusedUser);
    }
    public KDSUser.USER getFocusedUserID()
    {
        if (getUsersCount() == 1)
            return KDSUser.USER.USER_A;
        return m_focusedUser;
    }

    public int getUsersCount()
    {
        return m_users.size();
    }

    public void stop()
    {
        m_users.clear();
       // m_kds = null;

    }

    public KDSDataOrder getOrderByNameIncludeParked(String orderName)
    {
        if (getUserA() != null) {

            KDSDataOrder order =  getUserA().getOrders().getOrderByName(orderName);
            if (order != null) return order;
            order =  getUserA().getParkedOrders().getOrderByName(orderName);
            if (order != null) return order;
        }
        if (getUserB() != null)
        {
            KDSDataOrder order =  getUserB().getOrders().getOrderByName(orderName);
            if (order != null) return order;
            order =  getUserB().getParkedOrders().getOrderByName(orderName);
            if (order != null) return order;
        }
        return null;
    }
}
