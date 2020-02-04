package com.bematechus.kds;

import android.content.Context;
import android.graphics.Rect;

import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Administrator on 2017/4/12.
 */
public class TTViewOrders {


    KDSDataOrders m_orders = null;
    String m_strFocusedOrderGUID = "";


    Vector<Rect> m_rects = new Vector();

    Vector<ExtraTTID> m_arExtraTrackerID = new Vector<>(); //those ID existed in gateway, but no order use them.

    public String getFocusedOrderGUID()
    {
        return m_strFocusedOrderGUID;
    }
    public void setFocusedOrderGuid(String focusedGuid)
    {
        m_strFocusedOrderGUID = focusedGuid;
    }
    public void setOrders(KDSDataOrders orders)
    {
        m_orders = orders;
        synchronized (m_orders.m_locker) {
            sortOrders();
        }
    }

    public void sortOrders()
    {
        if (m_orders == null) return;
        m_orders.setSortMethod(KDSConst.OrderSortBy.Order_Number, KDSConst.SortSequence.Ascend, false, false);
        m_orders.sortOrders();
    }
    public KDSDataOrders getOrders()
    {
        return m_orders;
    }

    public  Vector<Rect> getCoordinates()
    {
        return m_rects;
    }
    public void setOrderCoordinate(int nOrderIndex, Rect rt)
    {
        int ncount = 0;
        if (m_rects.size() <=nOrderIndex)
        {
            ncount = nOrderIndex + 1 - m_rects.size();
            for (int i=0; i< ncount; i++)
                m_rects.add(new Rect());
        }
        m_rects.set(nOrderIndex, rt);
    }

    public void setOrderCoordinate(KDSDataOrder order, Rect rt)
    {
        synchronized (m_orders.m_locker) {
            int nindex = m_orders.getIndex(order);
            setOrderCoordinate(nindex, rt);
        }

    }

    public String getNextOrderGUID(String fromGuid)
    {
        Vector ar = this.getOrders().getComponents();
        synchronized (getOrders().m_locker) {
            if (ar.size() <= 0) return "";
            int nIndex = this.getOrders().getOrderIndexByGUID(fromGuid);
            nIndex++;

            if (nIndex >= getCoordinates().size())
                nIndex = 0;
            KDSDataOrder c = (KDSDataOrder) ar.get(nIndex);
            return c.getGUID();
        }
    }

    public String getPrevOrderGUID(String fromGuid)
    {

        Vector ar = this.getOrders().getComponents();
        synchronized (getOrders().m_locker) {
            if (ar.size() <= 0) return "";
            int nIndex = this.getOrders().getOrderIndexByGUID(fromGuid);
            nIndex--;
            if (nIndex < 0)
                nIndex = getCoordinates().size() - 1;
            KDSDataOrder c = (KDSDataOrder) ar.get(nIndex);
            return c.getGUID();
        }
    }
    public void resetCoordinates()
    {
        m_rects.clear();
    }

//    public void testData()
//    {
//        m_orders = new KDSDataOrders();
//        int nTestCount = 0;
//
//        for (int i=0; i< 15; i++) {
//            nTestCount ++;
//            String strOrderName = "Order #" + KDSUtil.convertIntToString(nTestCount);
//
//            int nItems = 2;// m_randomItems.nextInt(5);
//
//            //nItems = Math.abs(m_randomItems.nextInt() % 5) +1;
//            KDSDataOrder order = KDSDataOrder.createTestOrder(strOrderName, nItems, "1"); // rows = (i+2) * 6  +3 +titlerows;
//
//            m_orders.addComponent(order);
//        }
//
//    }


    public  TTOrder findTTItem( ArrayList<TTOrder> ar, String trackerID)
    {
        for (int i=0; i< ar.size(); i++)
        {
            if (ar.get(i).m_name.equals(trackerID))
                return ar.get(i);
        }
        return null;
    }

    /**
     * The holder id is the table ID(one holder is place in table).
     * @param ar
     */
    public void updateOrdersHolderID(KDSDBCurrent db, ArrayList<TTOrder> ar,boolean bAutoAssign, int nAutoAssignTimeoutSeconds)
    {
        if (getOrders() == null) return;
        synchronized (getOrders().m_locker) {
            if (bAutoAssign)
                autoAssignOrdersTrackerID(db, ar, nAutoAssignTimeoutSeconds);

            ArrayList<KDSDataOrder> arWillRemoved = new ArrayList<>();

            for (int i = 0; i < getOrders().getCount(); i++) {
                String trackerID = getOrders().get(i).getTrackerID();
                if (findTTItem(ar, trackerID) != null)
                    getOrders().get(i).setTTFindMyTrackerID(true);

                String holderID = findHolderID(trackerID, ar);
                if (holderID.equals(HOLDER_ID_EMPTY))
                    getOrders().get(i).setToTable("");
                else if (holderID.equals(TRACKER_ID_NOT_EXISTED)) //it is not existed in TT server
                {
                    arWillRemoved.add(getOrders().get(i));
                } else
                    getOrders().get(i).setToTable(holderID);
            }

//        TimeDog td = new TimeDog();
            for (int i = 0; i < arWillRemoved.size(); i++) {
                if (arWillRemoved.get(i).getTTFindMyTrackerID())//.m_bTTFindMyTrackerID) //has existed in gateway before, not lost. That means the tt was removed.
                    KDSStationFunc.orderBump(KDSGlobalVariables.getKDS().getUsers().getUser(KDSUser.USER.USER_A), arWillRemoved.get(i).getGUID(), true);
            }
            arWillRemoved.clear();
        }
    }


    public boolean isAssignedTrackerID(String trackerID)
    {
        synchronized (getOrders().m_locker) {
            for (int i = 0; i < getOrders().getCount(); i++) {
                String ttID = getOrders().get(i).getTrackerID();
                if (ttID.isEmpty()) continue;
                if (ttID.equals(trackerID))
                    return true;

            }
            return false;
        }
    }

    public void autoAssignOrdersTrackerID(KDSDBCurrent db,  ArrayList<TTOrder> ar, int nAutoAssignTimeoutSeconds)
    {
        if (getOrders() == null) return;
        ArrayList<KDSDataOrder> arOrdersEmptyTrackerID = new ArrayList<>();

        synchronized (getOrders().m_locker) {
            for (int i = 0; i < getOrders().getCount(); i++) {
                String trackerID = getOrders().get(i).getTrackerID();
                if (trackerID.isEmpty())
                    arOrdersEmptyTrackerID.add(getOrders().get(i));

            }
        }
        TimeDog td = new TimeDog();

        ArrayList<TTOrder> arFreeTrackerID = new ArrayList<>();
        ArrayList<TTOrder> arTimeoutTrackerID = new ArrayList<>();
        for (int i=0; i< ar.size(); i++)
        {
            if (!isAssignedTrackerID(ar.get(i).m_name)) {
                if (ar.get(i).m_elapseTime <nAutoAssignTimeoutSeconds)
                    arFreeTrackerID.add(ar.get(i));
                else
                    arTimeoutTrackerID.add(ar.get(i));
            }
        }

        int freeTrackerIDCount = 0;
        for (int i=0; i< arOrdersEmptyTrackerID.size(); i++)
        {
            td.reset( arOrdersEmptyTrackerID.get(i).getStartTime());
            if (!td.is_timeout(nAutoAssignTimeoutSeconds * 1000)) {
                if (freeTrackerIDCount < arFreeTrackerID.size()) {
                    String trackerID = arFreeTrackerID.get(freeTrackerIDCount).m_name;
                    arOrdersEmptyTrackerID.get(i).setTrackerID(trackerID);
                    db.orderSetTrackerID(arOrdersEmptyTrackerID.get(i).getGUID(),trackerID);
                    freeTrackerIDCount ++;
                }
                else
                    break;


            }
        }

        //show extra tracker id.
        m_arExtraTrackerID.clear();
        //add timeout tracker id first
        for (int i=0; i< arTimeoutTrackerID.size(); i++)
        {
            m_arExtraTrackerID.add(new ExtraTTID(arTimeoutTrackerID.get(i)));
        }
        //add waiting tracker next
        if (freeTrackerIDCount < arFreeTrackerID.size())
        {
            for (int i=freeTrackerIDCount; i< arFreeTrackerID.size(); i++)
            {
                m_arExtraTrackerID.add( new ExtraTTID( arFreeTrackerID.get(i)) );
            }
        }

    }

    public int getExtraTTIDCount()
    {
        return m_arExtraTrackerID.size();
    }

    public ExtraTTID getExtraTTID(int nIndex)
    {
        if (nIndex < 0 || nIndex >= m_arExtraTrackerID.size())
             return null;
        return m_arExtraTrackerID.get(nIndex);
    }


    public void setExtraTTIDCoordinate(ExtraTTID ttid, Rect rect)
    {
        ttid.m_rect = new Rect(rect);

    }

    public void clearOrders()
    {
        for (int i=0; i< getOrders().getCount(); i++) {
            KDSStationFunc.orderBump(KDSGlobalVariables.getKDS().getUsers().getUser(KDSUser.USER.USER_A), getOrders().get(i).getGUID(), false);
        }
        synchronized (getOrders().m_locker) {
            getOrders().clear();
        }
        KDSGlobalVariables.getKDS().refreshView();
    }

    final String HOLDER_ID_EMPTY = "-1";
    final String TRACKER_ID_NOT_EXISTED = "-2";
    /**
     *
     * @param trackerID
     * @param ar
     * @return
     *  -1: unknow, but this trackerID is existed.
     */
    private String findHolderID(String trackerID, ArrayList<TTOrder> ar)
    {

        TTOrder tt = findTTItem(ar, trackerID);
        if (tt == null) return TRACKER_ID_NOT_EXISTED;


        String holderID = tt.m_locationName;
        holderID = holderID.trim();
        if (holderID.isEmpty())
            return HOLDER_ID_EMPTY;
        else
            return holderID;

    }


    public void moveUnassignedToEnd()
    {
        Vector<KDSDataOrder> ar = new Vector<>();
        if (m_orders == null) return;
        synchronized (m_orders.m_locker) {
            for (int i = 0; i < m_orders.getCount(); i++) {
                if (m_orders.get(i).getTrackerID().isEmpty())
                    ar.add(m_orders.get(i));
            }

//            for (int i = 0; i < ar.size(); i++)
//                m_orders.removeComponent(ar.get(i));
            m_orders.getComponents().removeAll(ar);

//            for (int i = 0; i < ar.size(); i++)
//                m_orders.addComponent(ar.get(i));
            m_orders.getComponents().addAll(ar);
        }
    }

    /**
     * The order has been delete(set bumped flag) in database, so here, just remove from array.
     * @param timeoutRemoveMinutes
     */
    public void  checkExpoRemovedOrder(float timeoutRemoveMinutes)
    {
        ArrayList<KDSDataOrder> ar = new ArrayList<>();
        if (m_orders == null) return;
        TimeDog td = new TimeDog();
        synchronized (m_orders.m_locker) {
            for (int i = 0; i < m_orders.getCount(); i++) {
                if (m_orders.get(i).getTTReceiveExpoBumpNotification()) {//.m_bTTReceiveExpoBumpNotification) {
                    td.reset(m_orders.get(i).getTTReceiveExpoBumpNotificationDate());//.m_dtTTReceiveExpoBumpNotification);
                    if (td.is_timeout((int) (timeoutRemoveMinutes * 60 * 1000)))
                        ar.add(m_orders.get(i));
                }
            }

            //m_orders.getComponents().removeAll(ar);
            //m_orders.sortOrders();
            m_orders.removeComponents(ar);
//        for (int i=0; i< ar.size(); i++)
//            m_orders.removeComponent(ar.get(i));
        }
       ar.clear();
    }

    /**
     *
     * @param timeoutRemoveMinutes
     * @return
     *  All bumped orders
     */
    public ArrayList<KDSDataOrder>  checkAllItemsBumpedOrder(float timeoutRemoveMinutes)
    {
        ArrayList<KDSDataOrder> ar = new ArrayList<>();
        if (m_orders == null) return ar;
        TimeDog td = new TimeDog();
        synchronized (m_orders.m_locker) {
            for (int i = 0; i < m_orders.getCount(); i++) {
                if (m_orders.get(i).getTTAllItemsBumped()) {//.m_bTTAllItemsBumped) {
                    td.reset(m_orders.get(i).getTTAllItemsBumpedDate());//.m_dtTTAllItemsBumped);
                    if (td.is_timeout((int) (timeoutRemoveMinutes * 60 * 1000)))
                        ar.add(m_orders.get(i));
                }
            }

//            for (int i = 0; i < ar.size(); i++)
//                m_orders.removeComponent(ar.get(i));
            m_orders.getComponents().removeAll(ar);
            ar.clear();
        }

        return ar;
    }

    class ExtraTTID
    {
        String m_trackerID = "";
        String m_location = "";
        Rect m_rect = new Rect();
        public ExtraTTID(String trackerID, String location)
        {
            m_trackerID = trackerID;
            m_location = location;
        }
        public ExtraTTID(TTOrder ttOrder)
        {
            m_trackerID = ttOrder.m_name;
            m_location = ttOrder.m_locationName;
        }
    }
}
