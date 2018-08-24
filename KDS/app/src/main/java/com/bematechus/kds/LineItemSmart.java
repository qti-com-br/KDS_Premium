package com.bematechus.kds;

import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Administrator on 2018/2/10.
 * Show smart order mode in line display view.
 */
public class LineItemSmart {

    static class OrderItemPair
    {
        public KDSDataOrder m_order = null;
        public KDSDataItem m_item = null;
    }

    ArrayList<OrderItemPair> m_arSort = new ArrayList<>();
    public boolean m_bHiddenExisted = false;//identify if there are hidden items existed when showing method is hidden.

    public ArrayList<OrderItemPair> getSortedItems()
    {
        return m_arSort;
    }

    /**
     *
     * @param orders
     * @param startOrderGuid
     * @param nMaxCount
     */
    public void sortOrdersForSmart(KDSViewSettings env, KDSDataOrders orders,String startOrderGuid, int nMaxCount)
    {
        m_bHiddenExisted = false;
        orders.setSortMethod(KDSConst.OrderSortBy.Waiting_Time, KDSConst.SortSequence.Descend, false, false);
        orders.sortOrders();
        int n = env.getSettings().getInt(KDSSettings.ID.Smart_Order_Showing);
        KDSSettings.SmartOrderShowing showingMethod = KDSSettings.SmartOrderShowing.values()[n];

        int nIndex = orders.getOrderIndexByGUID(startOrderGuid);
        if (nIndex <0) nIndex = 0;
        m_arSort.clear();
        int nAddItems = 0;
        //get nMaxCount items
        for (int i=0; i< nMaxCount; i++)
        {
            KDSDataOrder order = orders.get(nIndex);
            if (order == null) break;
            for (int j=0; j< order.getItems().getCount(); j++)
            {
                KDSDataItem item = order.getItems().getItem(j);
                if (item.getLocalBumped() ||item.isMarked() ||
                    item.isReady())
                    continue;
                if (showingMethod == KDSSettings.SmartOrderShowing.Hide)
                {//hidden item showing, hide un-cooking item.
                    if (!order.prep_get_sorts().is_cooking_time(item.getItemName(), order.getStartTime(), order.getOrderDelay())) {
                        m_bHiddenExisted = true;
                        continue;
                    }
                }
                OrderItemPair p = new OrderItemPair();
                p.m_order = order;
                p.m_item = item;
                m_arSort.add(p);
                nAddItems ++;
                //if (nAddItems >= nMaxCount) break;
            }
            if (m_arSort.size() >= nMaxCount) break;
            nIndex ++;
        }
        //sort them by preparation time.
        sortItems(m_arSort);
    }

    private Date getItemStartCookTime(OrderItemPair pair)
    {
        KDSDataOrder order = pair.m_order;
        KDSDataItem item = pair.m_item;
        return order.prep_get_sorts().item_start_cook_time(item.getItemName(), order.getStartTime(), order.getOrderDelay());
    }

    private void sortItems(ArrayList<OrderItemPair> arSmart)
    {
        if (arSmart.size() <=1 )
            return;
        Collections.sort(arSmart, new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        OrderItemPair c1 = (OrderItemPair) o1;
                        OrderItemPair c2 = (OrderItemPair) o2;
                        Date dt1 = getItemStartCookTime(c1);
                        Date dt2 = getItemStartCookTime(c2);
                        return dt1.compareTo(dt2);
                    }
                }
        );

    }
}
