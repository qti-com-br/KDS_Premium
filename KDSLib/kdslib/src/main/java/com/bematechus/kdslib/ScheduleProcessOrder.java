package com.bematechus.kdslib;

import android.graphics.Paint;

/**
 * Created by Administrator on 2016/12/8.
 */
public class ScheduleProcessOrder extends KDSDataOrder {


    static final public String PREP_ITEM_TARGET_ID = "Target";
    static final public String PREP_ITEM_TOBE_ID = "ToBe";
    static final public String PREP_ITEM_READY_ID = "Ready";
    static final public String PREP_ITEM_LINE_ID = "Line";

    static final public String STR_PREP_ITEM_TARGET_TEXT = "Target:";
    static final public String STR_PREP_ITEM_TOBE_TEXT = "To Be Prepared:";
    static final public String STR_PREP_ITEM_READY_TEXT = "Ready:";

   // int m_nReady = 0;
    int m_nNotReadyQty = 0; //the items that is showing, but it is not ready status.


    int get_target_qty() {
        KDSDataItem pitem = get_prepare_item(this);
        if (pitem == null) return 0;
        return (int) pitem.getQty();
    }

    public int get_to_be_prepared_qty() {
        int n = get_target_qty();
        if (n == 0) return 0; //this item should been ended now.
        return get_target_qty() - get_ready_qty() + m_nNotReadyQty;
    }

    public void set_ready_qty(int nQty) {
        //m_nReady = nQty;
        KDSDataItem item =  this.getItems().getItem(0);
        if (item == null) return ;
        item.setScheduleProcessReadyQty(nQty);
        //update_items_array();
    }

    public int get_ready_qty() {
        KDSDataItem item =  this.getItems().getItem(0);
        if (item == null) return 0;
        return item.getScheduleProcessReadyQty();

        //return m_nReady;
    }

    static public  KDSDataItem get_prepare_item(KDSDataOrder order) {
        KDSDataItems pItems = order.getItems();
        int ncount = pItems.getCount();
        for (int i = 0; i < ncount; i++) {
            KDSDataItem pitem = pItems.getDataItem(i);
            String strID = pitem.getItemName();
            if (!strID.equals(PREP_ITEM_TARGET_ID) &&
                    !strID.equals(PREP_ITEM_TOBE_ID) &&
                    !strID.equals(PREP_ITEM_READY_ID) &&
                    !strID.equals(PREP_ITEM_LINE_ID))
                return pitem;

        }
        return null;

    }

    public boolean create_from_order_item(KDSDataOrder order, KDSDataItem item) {
        if (!create_order(order)) return false;
        getItems().addComponent(item);
        this.setOrderName(createScheduleOrderName(order.getOrderName(), item.getItemName()));
        item.setOrderGUID(this.getGUID());
        return true;
    }

    static public ScheduleProcessOrder createFromOrder(KDSDataOrder order)
    {
        ScheduleProcessOrder scheduleOrder = new ScheduleProcessOrder();

        if (!scheduleOrder.create_order(order)) return scheduleOrder;

        for (int i=0; i< order.getItems().getCount(); i++) {
            scheduleOrder.getItems().addComponent(order.getItems().getDataItem(i));
            scheduleOrder.getItems().getItem(i).setOrderGUID(scheduleOrder.getGUID());
        }

        return scheduleOrder;
    }

    static public String createScheduleOrderName(String originalOrderID, String itemID)
    {
        String s = "-"+itemID;
        if (originalOrderID.indexOf(s) == originalOrderID.length()-s.length())
            return originalOrderID;
        return originalOrderID + "-"+itemID;
    }
    private boolean create_order(KDSDataOrder order) {

        order.copyTo(this);
        this.getItems().clear();

        this.createNewGuid();
        return true;
    }
/************************************************************************/
/*
add target, to be prepared, and ready line to this order.

*/
    /************************************************************************/
    public static boolean init_schedule_items_array(KDSDataOrder order) //after prepared/ready qty changed, we will rebuild the items array.
    {
        if (get_attached_ready_item(order) != null) return true;

        KDSDataItem indicator = null;
        if (order.getItems().getCount()>0)
        {
            if (order.getItems().getItem(order.getItems().getCount()-1) instanceof  KDSDataFromPrimaryIndicator) {
                indicator = order.getItems().getItem(order.getItems().getCount() - 1);
                order.getItems().removeComponent(indicator);
            }

        }

        KDSDataItem pitem = new KDSDataItem();
        int nQty = 0;

        //target item
        //id.SetNormalItemID(PREP_ITEM_TARGET_ID);
        pitem.setOrderGUID(order.getGUID());

        //pitem->SetSerialIDString(id.GetSerialIDString());
        pitem.setItemName(PREP_ITEM_TARGET_ID);

        pitem.setDescription(STR_PREP_ITEM_TARGET_TEXT);
        pitem.setAlign(Paint.Align.RIGHT);
        //pitem->set_item_text_align(Item_Text_Align_Right);
        KDSDataItem pPrepare = get_prepare_item(order);
        if (pPrepare==null)
            nQty = 0;
        else
            nQty = (int)pPrepare.getQty();
        pitem.setQty(nQty);
        order.getItems().addComponent(pitem);


        //to be prepared
        pitem = new KDSDataItem();
        pitem.setItemName(PREP_ITEM_TOBE_ID);
        //pitem->SetSerialIDString(id.GetSerialIDString());
        pitem.setDescription(STR_PREP_ITEM_TOBE_TEXT);
        pitem.setQty(nQty);
        pitem.setAlign(Paint.Align.RIGHT);
        //pitem->set_item_text_align(Item_Text_Align_Right);
        order.getItems().addComponent(pitem);
        //AppendItem(pitem);

        pitem = new KDSDataItem();
        pitem.setItemName(PREP_ITEM_READY_ID);
        //pitem->SetSerialIDString(id.GetSerialIDString());
        pitem.setDescription(STR_PREP_ITEM_READY_TEXT);
        pitem.setQty(0);
        pitem.setAlign(Paint.Align.RIGHT);
        order.getItems().addComponent(pitem);
        //AppendItem(pitem);

        if (indicator != null)
            order.getItems().addComponent(indicator);
        //m_nTarget = nQty;
        //m_nReady = 0;
        return true;
    }

    @Override
    public boolean is_schedule_process_order()
    {
        return true;
    }

    static public boolean update_items_array(KDSDataOrder orderWillShow, ScheduleProcessOrder orderSchedule)
    {
        KDSDataItem pItem = get_attached_tobe_item(orderWillShow);
        if (pItem!= null)
            pItem.setQty(orderSchedule.get_to_be_prepared_qty());

        pItem = get_attached_ready_item(orderWillShow);
        if (pItem != null)
            pItem.setQty(orderSchedule.get_ready_qty());

        pItem = get_attached_target_item(orderWillShow);
        if (pItem != null)
            pItem.setQty(orderSchedule.get_target_qty());
        return true;
    }

    public static KDSDataItem get_attached_target_item(KDSDataOrder order)
    {
        KDSDataItems pItems = order.getItems();
        int ncount = pItems.getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem pitem = pItems.getDataItem(i);
            if (pitem == null) continue;
            String strID = pitem.getItemName();
            if (strID.equals(PREP_ITEM_TARGET_ID ))
                return pitem;
        }
        return null;
    }
    public static KDSDataItem get_attached_tobe_item(KDSDataOrder order)
    {
        KDSDataItems pItems = order.getItems();
        int ncount = pItems.getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem pitem = pItems.getDataItem(i);
            if (pitem == null) continue;
            String strID = pitem.getItemName();
            if (strID.equals(PREP_ITEM_TOBE_ID ))
                return pitem;

        }
        return null;
    }
    static public KDSDataItem get_attached_ready_item(KDSDataOrder order)
    {
        KDSDataItems pItems = order.getItems();
        int ncount = pItems.getCount();
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem pitem = pItems.getItem(i);
            String strID = pitem.getItemName();
            if (strID.equals(PREP_ITEM_READY_ID ))
                return pitem;

        }
        return null;
    }

    void set_ready_qty_add(int nQty) //plus this number
    {
        int n =  get_ready_qty() + nQty;
        set_ready_qty(n);
    }
    public int get_not_ready_qty()
    {
        return m_nNotReadyQty;
    }
    public boolean set_not_ready_qty(int nQty)
    {
        int nold = m_nNotReadyQty;
        m_nNotReadyQty = nQty;
        if (nold != nQty)
            return true;
        return false;
        //update_items_array();
    }

    public boolean is_finsihed()
    {
        return (get_to_be_prepared_qty() <=0);
    }
}
