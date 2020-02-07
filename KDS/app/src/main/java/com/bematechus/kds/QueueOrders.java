package com.bematechus.kds;

import android.content.Context;
import android.graphics.Rect;

import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by Administrator on 2016/12/23.
 */
public class QueueOrders {

    public enum QueueStatus
    {
        Received,//(a) "Order Received" (When KDS receive the order);
        Preparation,//(b) "In Preparation" (When one of the items is bumped or Start cooking is pressed),
        Ready,//(c) (All items are bumped from all stations or from Expo station).
        Pickup,//"Ready for Pickup"
    }

    public enum QueueSort //2.0.35
    {
        Default,
        State_ascend,
        State_descend,
    }
//    KDSDataOrders m_ordersA = null;
//    KDSDataOrders m_ordersB = null;

    String m_strFocusedOrderGUID = "";


    ArrayList<Rect> m_rects = new ArrayList();

    Hashtable<String, QueueStatus> m_ordersStatus = new Hashtable<>();

    public String getFocusedOrderGUID()
    {
        return m_strFocusedOrderGUID;
    }
    public void setFocusedOrderGuid(String focusedGuid)
    {
        if (focusedGuid.equals(KDSConst.RESET_ORDERS_LAYOUT))
            return;
        m_strFocusedOrderGUID = focusedGuid;
    }
//    public void setOrders(KDSDataOrders ordersA, KDSDataOrders ordersB)
//    {
//        m_ordersA = ordersA;
//        m_ordersB = ordersB;
//        refreshOrdersStatus();
//    }
//    public KDSDataOrders getOrders2()
//    {
//        KDSDataOrders orders = new KDSDataOrders();
//        if (m_ordersA != null)
//            orders.getComponents().addAll(m_ordersA.getComponents());
//        if (m_ordersB != null)
//            orders.getComponents().addAll(m_ordersB.getComponents());
//        if (m_ordersA != null)
//            orders.setSortMethod(KDSConst.OrderSortBy.Waiting_Time, KDSConst.SortSequence.Descend, m_ordersA.getMoveRushToFront(), m_ordersA.getMoveFinishedToFront());
//        orders.sortOrders();
//        return orders;
//    }
    public  ArrayList<Rect> getCoordinates()
    {
        return m_rects;
    }
    public void setOrderCoordinate(int nOrderIndex, Rect rt)
    {
        if (nOrderIndex <0) return;
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
        int nindex = getOrders().getIndex(order);
        setOrderCoordinate(nindex, rt);

    }

    public String getNextOrderGUID(String fromGuid)
    {
        Vector ar = this.getOrders().getComponents();
        if (ar.size() <=0) return "";
        int nIndex =  this.getOrders().getOrderIndexByGUID(fromGuid);
        nIndex ++;

        if (nIndex >= getCoordinates().size())
            nIndex =0;
        KDSDataOrder c = (KDSDataOrder)ar.get(nIndex) ;
        return c.getGUID();
    }

    public String getPrevOrderGUID(String fromGuid)
    {
        Vector ar = this.getOrders().getComponents();
        if (ar.size() <=0)  return "";
        int nIndex = this.getOrders().getOrderIndexByGUID(fromGuid);
        nIndex --;
        if (nIndex < 0 )
            nIndex =getCoordinates().size() -1;
        KDSDataOrder c = (KDSDataOrder)ar.get(nIndex) ;
        return c.getGUID();
    }
    public void resetCoordinates()
    {
        m_rects.clear();
    }

//    public String getQueueStatusString(Context context, QueueStatus status)
//    {
//        switch (status)
//        {
//
//            case Received:
//                return context.getString(R.string.queue_status_received);// "Order Received";
//
//            case Preparation:
//                return context.getString(R.string.queue_status_preparation);//
//
//            case Ready:
//                return context.getString(R.string.queue_status_ready);//
//            case Pickup:
//                return context.getString(R.string.queue_status_pickup);//
//
//        }
//        return "";
//    }

    static public QueueStatus getOrderQueueStatus( KDSDataOrder order)
    {


        QueueStatus status = QueueStatus.Received;
        if (order == null) return status;
        if (order.getQueueReady())
            return QueueStatus.Pickup;
        //int ncount = order.getItems().getCount();

        int nReadyItemsCounter = order.getFinishedItemsCount();
//        for (int i=0; i< ncount; i++)
//        {
//            if (order.getItems() == null)
//                return status;
//            if (order.getItems().getItem(i) == null)
//                return status;
//            KDSDataItem item = order.getItems().getItem(i);
//            if (item.isMarked() ||
//                    item.getLocalBumped() ||
//                    item.isReady()||
//                    (!item.getBumpedStationsString().isEmpty()))
//            {
//                nReadyItemsCounter ++;
//            }
//        }
        if (nReadyItemsCounter!=0 && nReadyItemsCounter == order.getItems().getCount())
        {
            status = QueueStatus.Ready;
        }
        else if (nReadyItemsCounter > 0)
            status  = QueueStatus.Preparation;

        return status;
    }

//    public void testData()
//    {
//        m_orders = new KDSDataOrders();
//        int nTestCount = 0;
//
//        for (int i=0; i< 5; i++) {
//            nTestCount ++;
//            String strOrderName = "Order #" + KDSUtil.convertIntToString(nTestCount);
//
//            int nItems = 2;// m_randomItems.nextInt(5);
//
//            //nItems = Math.abs(m_randomItems.nextInt() % 5) +1;
//            KDSDataOrder order = KDSDataOrder.createTestOrder(strOrderName, nItems, "1"); // rows = (i+2) * 6  +3 +titlerows;
//            m_orders.addComponent(order);
//        }
//
//    }

    private Vector<KDSDataOrder> getStatusOrders(Vector<KDSDataOrder> orders, QueueStatus status)
    {

        Vector<KDSDataOrder> arOrders = new Vector<>();
        try {
            for (int i = 0; i < orders.size(); i++) {


                KDSDataOrder order = orders.get(i);
                if (order == null) break;
                if (getOrderQueueStatus(order) == status)
                    arOrders.add(order);
            }
            return arOrders;
        }
        catch (Exception e)
        {

        }
        return arOrders;
    }
    private void removeOrderInArray(Vector<KDSDataOrder> arOrders, Vector<KDSDataOrder> arOrdersRemove)
    {
        for (int i=0; i< arOrdersRemove.size(); i++)
        {
            arOrders.remove(arOrdersRemove.get(i));
        }
    }

    /**
     * 2.0.36
     * @param status1Sort
     * @param status2Sort
     * @param status3Sort
     * @param status4Sort
     */
    public void sortByStateTime(QueueSort status1Sort,QueueSort status2Sort,QueueSort status3Sort,QueueSort status4Sort)
    {
        if (getOrders() == null) return;
        Vector<KDSDataOrder> arOrders = getOrders().getComponents();

        Vector<KDSDataOrder> arStatus1 = getStatusOrders(arOrders, QueueStatus.Received);
        Vector<KDSDataOrder> arStatus2 = getStatusOrders(arOrders, QueueStatus.Preparation);
        Vector<KDSDataOrder> arStatus3 = getStatusOrders(arOrders, QueueStatus.Ready);
        Vector<KDSDataOrder> arStatus4 = getStatusOrders(arOrders, QueueStatus.Pickup);

        if (sortByStateTime(arStatus1, status1Sort))
        {
            removeOrderInArray(arOrders, arStatus1);
            arOrders.addAll(arStatus1);
        }
        if (sortByStateTime(arStatus2, status2Sort))
        {
            removeOrderInArray(arOrders, arStatus2);
            arOrders.addAll(arStatus2);
        }
        if (sortByStateTime(arStatus3, status3Sort))
        {
            removeOrderInArray(arOrders, arStatus3);
            arOrders.addAll(arStatus3);
        }
        if (sortByStateTime(arStatus4, status4Sort))
        {
            removeOrderInArray(arOrders, arStatus4);
            arOrders.addAll(arStatus4);
        }

    }

    public boolean sortByStateTime(Vector<KDSDataOrder> arOrders ,QueueSort sortMode)
    {
        //ArrayList<KDSDataOrder> arOrders = getOrders().getComponents();
        if (arOrders == null) return false;
        if (arOrders.size()<=0) return false;
        if (sortMode == QueueSort.Default) return false;
        boolean bAscend = (sortMode== QueueSort.State_ascend);

        if (bAscend) { //ascend mode
            Collections.sort(arOrders, new Comparator() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            KDSDataOrder c1 = (KDSDataOrder) o1;
                            KDSDataOrder c2 = (KDSDataOrder) o2;
                            long dt1 = c1.getQueueStateTime().getTime();
                            long dt2 = c2.getQueueStateTime().getTime();

                            int nresult = 0;//name1.compareTo( name2 ) ;
                            if (dt1 > dt2)
                                nresult = 1;
                            else if (dt1 < dt2)
                                nresult = -1;
                            return nresult;


                        }
                    }
            );
        }
        else
        {//descend mode
            Collections.sort(arOrders, new Comparator() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            KDSDataOrder c1 = (KDSDataOrder) o1;
                            KDSDataOrder c2 = (KDSDataOrder) o2;
                            long dt1 = c1.getQueueStateTime().getTime();
                            long dt2 = c2.getQueueStateTime().getTime();

                            int nresult = 0;//name1.compareTo( name2 ) ;
                            if (dt1 > dt2)
                                nresult = 1;
                            else if (dt1 < dt2)
                                nresult = -1;
                            return (-1) * nresult;

                        }
                    }
            );
        }

        return true;

    }

    private void refreshOrdersStatus()
    {

        m_ordersStatus.clear();
        for (int i=0; i< getOrders().getCount(); i++)
        {
            KDSDataOrder order = getOrders().get(i);
            if (order == null) break;
            QueueStatus status = getOrderQueueStatus(order);
            m_ordersStatus.put(order.getGUID(), status);
        }

    }

    public QueueOrders.QueueStatus getStatus(KDSDataOrder order)
    {
        if (order == null) return QueueStatus.Received;
        String guid = order.getGUID();
        if (m_ordersStatus.containsKey(guid))
            return m_ordersStatus.get(guid);
        else {
            QueueOrders.QueueStatus status = QueueOrders.getOrderQueueStatus(order);
            m_ordersStatus.put(guid, status);
            return status;
        }
    }

    //kpp1-288
    KDSUsers m_users = null;
    public void setOrders(KDSUsers users)
    {
        m_users = users;
        refreshOrdersStatus();
    }

    public KDSDataOrders getOrders()
    {
        if (m_users == null) return null;
        if (m_users.getUsersCount() == 1)
        {
            return m_users.getUserA().getOrders();
        }
        KDSDataOrders orders = new KDSDataOrders();


        if (m_users.getUserA() != null)
            orders.getComponents().addAll(m_users.getUserA().getOrders().getComponents());
        if (m_users.getUserB() != null)
            orders.getComponents().addAll(m_users.getUserB().getOrders().getComponents());
        if (m_users.getUserB() != null)//just multiple users do sort
        {
            orders.setSortMethod(KDSConst.OrderSortBy.Waiting_Time, KDSConst.SortSequence.Descend, m_users.getUserA().getOrders().getMoveRushToFront(), m_users.getUserA().getOrders().getMoveFinishedToFront());

        }
        return orders;
    }

    public boolean isReady()
    {
        return (m_users != null);
    }
    public void removeAll(ArrayList<KDSDataOrder> ar)
    {
        if (m_users.getUserA() != null)
            m_users.getUserA().getOrders().getComponents().removeAll(ar);
        if (m_users.getUserB() != null)
            m_users.getUserB().getOrders().getComponents().removeAll(ar);

    }
    public int getCount()
    {
        int n = 0;
        if (m_users.getUserA() != null)
            n += m_users.getUserA().getOrders().getCount();
        if (m_users.getUserB() != null)
            n += m_users.getUserB().getOrders().getCount();
        return n;
    }
}
