/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bematechus.kdslib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author David.Wong
 */
public class KDSDataOrders extends KDSDataArray {


    KDSConst.OrderSortBy m_sortBy = KDSConst.OrderSortBy.Unknown;//.Waiting_Time;
    KDSConst.SortSequence m_sortSequence = KDSConst.SortSequence.Ascend;
    public boolean m_bMoveRushFront = true;
    public boolean m_bMoveFinishedFront = false;

     public void copyTo(KDSDataOrders objs)
    {
        synchronized (m_locker) {
            ArrayList ar = this.getComponents();
            for (int i = 0; i < ar.size(); i++) {
                KDSDataOrder c = new KDSDataOrder();
                KDSDataOrder original = (KDSDataOrder) ar.get(i);
                original.copyTo(c);
                objs.addComponent(c);
            }
        }
    }
     public KDSDataOrder get(int nIndex)
     {
         synchronized (m_locker) {
             if (nIndex < 0) return null;
             if (nIndex >= this.getCount())
                 return null;

             KDSDataOrder c = (KDSDataOrder) this.getComponents().get(nIndex);

             return c;
         }
     }

    /**
     * find the paid order from give index.
     * For "Show paid order" items showing method.
     * @param nIndex
     * @return
     */
    public KDSDataOrder getNextPaidOrderFrom(int nIndex)
    {
        synchronized (m_locker) {
            if (nIndex < 0) return null;
            if (nIndex >= this.getCount())
                return null;
            for (int i = nIndex; i < this.getCount(); i++) {
                KDSDataOrder c = (KDSDataOrder) this.getComponents().get(i);
                if (c.isPaid()) return c;
            }
            return null;
        }

    }

    public KDSDataOrder getPrevPaidOrderFrom(int nIndex)
    {
        synchronized (m_locker) {
            if (nIndex < 0) return null;
            if (nIndex >= this.getCount())
                return null;
            for (int i = nIndex; i >= 0; i--) {
                KDSDataOrder c = (KDSDataOrder) this.getComponents().get(i);
                if (c.isPaid()) return c;
            }
            return null;
        }
    }

     public KDSDataOrder getOrderByName(String orderName)
     {
         synchronized (m_locker) {
             ArrayList ar = this.getComponents();
             for (int i = 0; i < ar.size(); i++) {
                 KDSDataOrder c = (KDSDataOrder) ar.get(i);
                 if (c.getOrderName().equals(orderName))
                     return c;

             }
             return null;
         }
     }
     
     public KDSDataOrder getOrderByGUID(String orderGUID)
     {
         synchronized (m_locker) {
             ArrayList ar = this.getComponents();
             for (int i = 0; i < ar.size(); i++) {
                 KDSDataOrder c = (KDSDataOrder) ar.get(i);
                 if (c.getGUID().equals(orderGUID))
                     return c;

             }
             return null;
         }
     }
     
     public int getIndex(KDSDataOrder order)
     {
         synchronized (m_locker) {
             ArrayList ar = this.getComponents();
             return ar.indexOf(order);
         }
     }
     
    public int getIndex(String orderGUID)
    {
        synchronized (m_locker) {
            KDSDataOrder order = this.getOrderByGUID(orderGUID);
            if (order == null)
                return -1;

            return getIndex(order);
        }
    }
     
     
//     public boolean deleteItem(String orderName, String itemName)
//     {
//         synchronized (m_locker) {
//             KDSDataOrder order = getOrderByName(orderName);
//             if (order == null)
//                 return true;
//             return order.getItems().deleteItem(itemName);
//         }
//     }
     
     public  int getOrderIndexByGUID(String orderGUID)
     {
         synchronized (m_locker) {
             ArrayList ar = this.getComponents();
             for (int i = 0; i < ar.size(); i++) {
                 KDSDataOrder c = (KDSDataOrder) ar.get(i);
                 if (c.getGUID().equals(orderGUID))
                     return i;

             }
             return -1;
         }
     }

    public String getFirstOrderGuid()
    {
        synchronized (m_locker) {
            if (getCount() <= 0) return "";
            return get(0).getGUID();
        }
    }

    public String getFirstPaidOrderGuid()
    {
        synchronized (m_locker) {
            if (getCount() <= 0) return "";
            int nIndex = 0;
            ArrayList ar = this.getComponents();
            for (int i = 0; i < ar.size(); i++) {
                KDSDataOrder c = (KDSDataOrder) ar.get(i);
                if (c.isPaid()) {
                    String str = c.getGUID();
                    return str;
                }
            }
            return "";
        }
    }

    public boolean isScheduleOrder(String orderGuid)
    {
        synchronized (m_locker) {
            KDSDataOrder order = this.getOrderByGUID(orderGuid);
            if (order == null) return false;
            return order.is_schedule_process_order();
        }
    }
     public String getNextOrderGUID(String orderGUID)
     {
         synchronized (m_locker) {
             ArrayList ar = this.getComponents();
             if (ar.size() <= 0) return "";
             int nIndex = getOrderIndexByGUID(orderGUID);
             nIndex++;

             if (nIndex >= ar.size())
                 nIndex = 0;
             KDSDataOrder c = (KDSDataOrder) ar.get(nIndex);
             return c.getGUID();
         }
        
     }

    /**
     * for "show paid order" items showing method.
     * @param orderGUID
     * @return
     */
    public String getNextPaidOrderGUID(String orderGUID)
    {
        synchronized (m_locker) {
            ArrayList ar = this.getComponents();
            if (ar.size() <= 0) return "";
            int nIndex = getOrderIndexByGUID(orderGUID);
            for (int i = 0; i < ar.size(); i++) {
                nIndex++;

                if (nIndex >= ar.size())
                    nIndex = 0;
                KDSDataOrder c = (KDSDataOrder) ar.get(nIndex);
                if (c.isPaid()) {
                    String str = c.getGUID();
                    if (!str.equals(orderGUID))
                        return str;
                }
            }
            return "";
        }

    }

    public String getPreviousOrderGUID(String orderGUID)
    {
         synchronized (m_locker) {
             ArrayList ar = this.getComponents();
             if (ar.size() <= 0) return "";
             int nIndex = getOrderIndexByGUID(orderGUID);
             nIndex--;
             if (nIndex < 0)
                 nIndex = ar.size() - 1;
             KDSDataOrder c = (KDSDataOrder) ar.get(nIndex);
             return c.getGUID();
         }
    }

    public String getPrevPaidOrderGUID(String orderGUID)
    {
        synchronized (m_locker) {
            ArrayList ar = this.getComponents();
            if (ar.size() <= 0) return "";
            int nIndex = getOrderIndexByGUID(orderGUID);
            for (int i = 0; i < ar.size(); i++) {
                nIndex--;
                if (nIndex < 0)
                    nIndex = ar.size() - 1;
                KDSDataOrder c = (KDSDataOrder) ar.get(nIndex);
                if (!c.getGUID().equals(orderGUID))
                    return c.getGUID();
            }
            return "";
        }

    }
     public void setSortMethod(KDSConst.OrderSortBy sortBy,KDSConst.SortSequence sortSequence, boolean bMoveRushFront, boolean bMoveFinishedFront)
     {
         m_sortBy = sortBy;
         m_sortSequence = sortSequence;
         m_bMoveRushFront = bMoveRushFront;
         m_bMoveFinishedFront = bMoveFinishedFront;

         sortOrders();
     }
    public KDSConst.OrderSortBy getSortBy()
    {
        return m_sortBy;
    }
    public KDSConst.SortSequence getSortSequence()
    {
        return m_sortSequence;
    }
    public boolean getMoveRushToFront()
    {
        return m_bMoveRushFront;
    }
    public boolean getMoveFinishedToFront()
    {
        return m_bMoveFinishedFront;
    }

    /**
     * 2.0.14, add move finished to front of queue feature.
     */
    public void sortOrders()
    {
        synchronized (m_locker) {
            if (!m_bMoveRushFront && !m_bMoveFinishedFront) {
                sortOrders(this.getComponents(), m_sortBy, m_sortSequence);
            }
            else if (m_bMoveRushFront && (!m_bMoveFinishedFront))
            {
                ArrayList arRush = new ArrayList();
                ArrayList arNormal = new ArrayList();
                int ncount = this.getCount();
                for (int i = 0; i < ncount; i++) {
                    KDSDataOrder order = this.get(i);
                    if (order.isRush())
                        arRush.add(order);
                    else
                        arNormal.add(order);
                }
                sortOrders(arRush, m_sortBy, m_sortSequence);
                sortOrders(arNormal, m_sortBy, m_sortSequence);
                ArrayList arOrders = this.getComponents();
                arOrders.clear();
                arOrders.addAll(arRush);
                arOrders.addAll(arNormal);
            }
            else if (m_bMoveFinishedFront && (!m_bMoveRushFront))
            {//2.0.14
                ArrayList arFinished = new ArrayList();
                ArrayList arNormal = new ArrayList();
                int ncount = this.getCount();
                for (int i = 0; i < ncount; i++) {
                    KDSDataOrder order = this.get(i);
                    if (order.isAllItemsFinished())
                        arFinished.add(order);
                    else
                        arNormal.add(order);
                }
                sortOrders(arFinished, m_sortBy, m_sortSequence);
                sortOrders(arNormal, m_sortBy, m_sortSequence);
                ArrayList arOrders = this.getComponents();
                arOrders.clear();
                arOrders.addAll(arFinished);
                arOrders.addAll(arNormal);
            }
            else
            {//2.0.14
                ArrayList arRush = new ArrayList();
                ArrayList arFinished = new ArrayList();
                ArrayList arNormal = new ArrayList();
                int ncount = this.getCount();
                for (int i = 0; i < ncount; i++) {
                    KDSDataOrder order = this.get(i);
                    if (order.isAllItemsFinished())
                        arFinished.add(order);
                    else if (order.isRush())
                        arRush.add(order);
                    else
                        arNormal.add(order);
                }
                sortOrders(arFinished, m_sortBy, m_sortSequence);
                sortOrders(arNormal, m_sortBy, m_sortSequence);
                sortOrders(arRush, m_sortBy, m_sortSequence);

                ArrayList arOrders = this.getComponents();
                arOrders.clear();

                arOrders.addAll(arFinished);
                arOrders.addAll(arRush);
                arOrders.addAll(arNormal);
            }
        }
        
    }
    /**
     * 
     * @param sortBy
     * @param sortSequence 
     */
    private void sortOrders(ArrayList arOrders, KDSConst.OrderSortBy sortBy, KDSConst.SortSequence sortSequence)
    {
        if (arOrders.size() <=1 )
            return;
        if (sortBy == KDSConst.OrderSortBy.Unknown)
            return;
       // KDSOrders orders = this.getOrders();
        if (sortBy == KDSConst.OrderSortBy.Waiting_Time)
        {
            if (sortSequence ==KDSConst.SortSequence.Ascend) {
                Collections.sort(arOrders, new Comparator() {
                            @Override
                            public int compare(Object o1, Object o2) {
                                KDSDataOrder c1 = (KDSDataOrder) o1;
                                KDSDataOrder c2 = (KDSDataOrder) o2;
                                String dt1 = c1.makeDurationString();
                                String dt2 = c2.makeDurationString();
                                return dt1.compareTo(dt2);
                            }
                        }
                );
            }
            else
            {
                Collections.sort(arOrders, new Comparator() {
                            @Override
                            public int compare(Object o1, Object o2) {
                                KDSDataOrder c1 = (KDSDataOrder) o1;
                                KDSDataOrder c2 = (KDSDataOrder) o2;
                                String dt1 = c1.makeDurationString();
                                String dt2 = c2.makeDurationString();
                                int nresult = dt1.compareTo(dt2);
                                return (-1) * nresult;
                            }
                        }
                );
            }
        }
        else if (sortBy == KDSConst.OrderSortBy.Order_Number)
        {
            if (sortSequence ==KDSConst.SortSequence.Ascend) {
                Collections.sort(arOrders, new Comparator() {
                            @Override
                            public int compare(Object o1, Object o2) {
                                KDSDataOrder c1 = (KDSDataOrder) o1;
                                KDSDataOrder c2 = (KDSDataOrder) o2;
                                String name1 = c1.getOrderName();//.makeDurationString();
                                String name2 = c2.getOrderName();
                                return name1.compareTo(name2);
                            }
                        }
                );
            }
            else
            {
                Collections.sort(arOrders, new Comparator() {
                            @Override
                            public int compare(Object o1, Object o2) {
                                KDSDataOrder c1 = (KDSDataOrder) o1;
                                KDSDataOrder c2 = (KDSDataOrder) o2;
                                String name1 = c1.getOrderName();//.makeDurationString();
                                String name2 = c2.getOrderName();
                                int nresult = name1.compareTo(name2);
                                return (-1) * nresult;
                            }
                        }
                );
            }
        }
        else if (sortBy == KDSConst.OrderSortBy.Items_Count)
        {
            if (sortSequence ==KDSConst.SortSequence.Ascend) {
                Collections.sort(arOrders, new Comparator() {
                            @Override
                            public int compare(Object o1, Object o2) {
                                KDSDataOrder c1 = (KDSDataOrder) o1;
                                KDSDataOrder c2 = (KDSDataOrder) o2;
                                int count1 = c1.getItems().getCount();//
                                if (count1<=0) {
                                   if ( c1.getTag() !=null)
                                       count1 = (int)c1.getTag();
                                }
                                int count2 = c2.getItems().getCount();
                                if (count2<=0) {
                                    if ( c2.getTag() !=null)
                                        count2 = (int)c2.getTag();
                                }

                                int nresult = 0;//name1.compareTo( name2 ) ;
                                if (count1 > count2)
                                    nresult = 1;
                                else if (count1 < count2)
                                    nresult = -1;
                                return nresult;
                            }
                        }
                );
            }
            else
            {
                Collections.sort(arOrders, new Comparator() {
                            @Override
                            public int compare(Object o1, Object o2) {
                                KDSDataOrder c1 = (KDSDataOrder) o1;
                                KDSDataOrder c2 = (KDSDataOrder) o2;
                                int count1 = c1.getItems().getCount();//
                                if (count1<=0) {
                                    if ( c1.getTag() !=null)
                                        count1 = (int)c1.getTag();
                                }
                                int count2 = c2.getItems().getCount();
                                if (count2<=0) {
                                    if ( c2.getTag() !=null)
                                        count2 = (int)c2.getTag();
                                }

                                int nresult = 0;//name1.compareTo( name2 ) ;
                                if (count1 > count2)
                                    nresult = 1;
                                else if (count1 < count2)
                                    nresult = -1;

                                 return (-1) * nresult;
                            }
                        }
                );
            }
        }
        else if (sortBy == KDSConst.OrderSortBy.Preparation_Time)
        {
            //TODO
        }
    }
    
    public String getOrderName(int nIndex)
    {
        synchronized (m_locker) {
            KDSDataOrder order = this.get(nIndex);
            if (order == null)
                return "";
            return order.getOrderName();
        }
    }

    public ArrayList<String> getAllOrderGUID()
    {
        synchronized (m_locker) {
            ArrayList<String> ar = new ArrayList<String>();
            int ncount = m_arComponents.size();
            for (int i = 0; i < ncount; i++) {
                KDSDataOrder order = this.get(i);
                ar.add(order.getGUID());

            }
            return ar;
        }
    }

    public float getTotalQty()
    {
        synchronized (m_locker) {
            float flt = 0;
            int ncount = m_arComponents.size();
            for (int i = 0; i < ncount; i++) {
                KDSDataOrder order = this.get(i);
                flt += order.getItems().getTotalQty();


            }
            return flt;
        }
    }

    public void addComponent(KDSData obj)
    {
        synchronized (m_locker) {
            super.addComponent(obj);
            sortOrders();
        }

    }
    public void addOrderWithoutSort(KDSData obj)
    {
        super.addComponent(obj);
    }
    public boolean removeComponent(int nIndex)
    {
        synchronized (m_locker) {
            super.removeComponent(nIndex);
            sortOrders();
            return true;
        }
    }

    public boolean removeComponent(Object obj)
    {
        synchronized (m_locker) {
            super.removeComponent(obj);
            sortOrders();
            return true;
        }
    }

    /**
     * find the waiting timeout orders
     * @param nTimeoutMinutes
     * @param nMaxCount
     *  -1: all
     *  >0: max return count
     * @return
     */
    public ArrayList<String> findTimeoutOrders(int nTimeoutMinutes, int nMaxCount)
    {
        synchronized (m_locker) {
            ArrayList<String> ar = new ArrayList<>();

            TimeDog td = new TimeDog();
            int nms = nTimeoutMinutes * 60 * 1000; //ms
            int ncount = this.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataOrder order = this.get(i);
                //td.reset(order.getStartTime());
                td.reset(order.getAutoBumpStartCountTime());
                if (td.is_timeout(nms)) {
                    ar.add(order.getGUID());
                    if (nMaxCount >0)
                    {
                        if (ar.size() >= nMaxCount)
                            break;
                    }
                }
            }
            return ar;
        }
    }

    public int getLineItemsDistance(String fromOrderGuid, String fromItemGuid, String toOrderGuid, String toItemGuid)
    {
        KDSDataOrder fromOrder = getOrderByGUID(fromOrderGuid);
        if (fromOrder == null) return 0;
        int fromItemIndex = fromOrder.getItems().getItemIndexByGUID(fromItemGuid);
        int ncounter = 0;//fromOrder.getItems().getCount() - fromItemIndex;

        int fromOrderIndex = getIndex(fromOrderGuid);

        int toOrderIndex = getIndex(toOrderGuid);
        KDSDataOrder toOrder = getOrderByGUID(toOrderGuid);
        if (toOrder == null) return 0;
        int toItemIndex = toOrder.getItems().getItemIndexByGUID(toItemGuid);
        if (fromOrderIndex == toOrderIndex)
            return (toItemIndex - fromItemIndex);

        boolean breversed = false;
        if (fromOrderIndex>toOrderIndex)
        {
            //reverse from/to
            int ntemp = fromOrderIndex;
            fromOrderIndex = toOrderIndex;
            toOrderIndex = ntemp;
            ntemp = fromItemIndex;
            fromItemIndex = toItemIndex;
            toItemIndex = ntemp;
            breversed = true;
        }


        for (int i=fromOrderIndex; i<= toOrderIndex; i++)
        {
            if (i == fromOrderIndex) {
                ncounter += get(i).getNextActiveItemsCount(fromItemIndex);
            }
            else if (i == toOrderIndex)
            {
                ncounter += get(i).getPrevActiveItemsCount(toItemIndex );
            }
            else {
                ncounter += get(i).getItems().getActiveItemsCount();
            }
        }
        if (!breversed)
            return ncounter;
        else
            return (-1) * ncounter;
    }


    public boolean isNoOtherActiveItemsBehindMe(String itemGuid)
    {
        int ncount = this.getCount();
        boolean bFindActiveItem = false;
        for (int i=ncount-1; i>=0; i--)
        {
            KDSDataOrder order = this.get(i);
            for (int j=order.getItems().getCount()-1; j>=0; j--)
            {
                if (order.getItems().getItem(j).getGUID().equals(itemGuid))
                    return (!bFindActiveItem);
                if (!order.getItems().getItem(j).getLocalBumped())
                    bFindActiveItem = true;

            }
        }
        return (!bFindActiveItem);
    }
}
