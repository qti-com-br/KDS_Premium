package com.bematechus.kds;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataCategoryIndicator;
import com.bematechus.kdslib.KDSDataCondiment;
import com.bematechus.kdslib.KDSDataCondiments;
import com.bematechus.kdslib.KDSDataFromPrimaryIndicator;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataMessage;
import com.bematechus.kdslib.KDSDataMessages;
import com.bematechus.kdslib.KDSDataModifier;
import com.bematechus.kdslib.KDSDataModifiers;
import com.bematechus.kdslib.KDSDataMoreIndicator;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSDataVoidItemIndicator;
import com.bematechus.kdslib.KDSDataVoidItemQtyChanged;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSViewFontFace;
import com.bematechus.kdslib.ScheduleProcessOrder;
import com.bematechus.kdslib.SettingsBase;

import java.util.ArrayList;

/**
 *
 * arrange orders to panel
 */
public class KDSLayout implements KDSView.KDSViewEventsInterface, LineItemViewer.LineItemViewerEvents{

    final String TAG = "KDSLayout";
    public interface KDSLayoutEvents
    {
        public  void onViewPanelDoubleClicked(KDSLayout layout);
        public  void onViewPanelClicked(KDSLayout layout);

    }


    private KDSLayoutEvents m_eventsReceiver = null;
    private KDSView m_view = null;
    private KDSDataOrders m_orders;


    public KDSLayout(KDSView view) {
        m_view = view;
        m_view.setEventsReceiver(this);
        m_view.getLineItemsViewer().setEventReceiver(this);//2.0.27
    }

    public KDSView getView() {
        return m_view;
    }

    public KDSLayoutEvents getEventsReceiver()
    {
        return m_eventsReceiver ;
    }
    public void setEventsReceiver(KDSLayoutEvents receiver)
    {
        m_eventsReceiver = receiver;
    }
    public KDSViewSettings getEnv() {
        return m_view.getEnv();
    }



    public boolean isLineItemsMode()
    {
        return  (m_view.getOrdersViewMode() == KDSView.OrdersViewMode.LineItems);
    }


    public boolean showOrders(KDSDataOrders orders) {

        //TimeDog t = new TimeDog();
        m_orders = orders;
        m_nRedrawRequestCounter ++;
        startShowOrdersThread();
        return true;
        /*
        //m_view.clear();
        m_view.getPanels().clear();

        if (orders == null || orders.getCount() <= 0) {
            this.getView().refresh();
            //m_view.clear();
            return true;
        }

        if (m_view.getOrdersViewMode() == KDSView.OrdersViewMode.Normal) {
            int nBlockRows = m_view.getAverageRowsInBlock();
            int nStartOrderIndex = 0;
            if (getEnv().getStateValues().getFirstShowingOrderGUID().isEmpty())
                getEnv().getStateValues().setFirstShowingOrderGUID(orders.get(0).getGUID());
            else {
                nStartOrderIndex = orders.getIndex(getEnv().getStateValues().getFirstShowingOrderGUID());
            }
            //check if the focused order is hidden at left side.
            int nFocusedOrderIndex = orders.getOrderIndexByGUID(getEnv().getStateValues().getFocusedOrderGUID());
            if (nFocusedOrderIndex >=0)
            {
                if (nFocusedOrderIndex<nStartOrderIndex) //adjust it
                {
                    getEnv().getStateValues().setFirstShowingOrderGUID(getEnv().getStateValues().getFocusedOrderGUID());
                    nStartOrderIndex = nFocusedOrderIndex;
                }
            }

            //t.debug_print_Duration("showOrders2");
            if (nStartOrderIndex < 0)
                nStartOrderIndex = 0;
            int ncount = orders.getCount();
            for (int i = nStartOrderIndex; i < ncount; i++) {
                // t.debug_print_Duration("showOrders1");
                KDSDataOrder order = orders.get(i);
                // t.debug_print_Duration("showOrders2");
                if (!showOrder(order, nBlockRows))
                    break;
                //t.debug_print_Duration("showOrders3");
            }
            //t.debug_print_Duration("showOrders3");
            this.getView().refresh();//.invalidate();
            //t.debug_print_Duration("showOrders4");
            return true;
        }
        else if (m_view.getOrdersViewMode() == KDSView.OrdersViewMode.LineItems)
        {
            if (getEnv().getStateValues().getFirstShowingOrderGUID().isEmpty())
                getEnv().getStateValues().setFirstShowingOrderGUID(orders.get(0).getGUID());
            if (getEnv().getStateValues().getFocusedOrderGUID().isEmpty()) {
                getEnv().getStateValues().setFocusedOrderGUID(orders.get(0).getGUID());

            }
            if (getEnv().getStateValues().getFocusedItemGUID().isEmpty()) {
                //getEnv().getStateValues().setFocusedItemGUID(orders.get(0).getItems().getItem(0).getGUID());
                getEnv().getStateValues().setFocusedItemGUID(orders.get(0).getItems().getFirstUnbumpedItemGuid());

            }
            if (getEnv().getStateValues().getFirstItemGuid().isEmpty()) {
                //getEnv().getStateValues().setFirstItemGuid(orders.get(0).getItems().getItem(0).getGUID());
                getEnv().getStateValues().setFirstItemGuid(orders.get(0).getItems().getFirstUnbumpedItemGuid());
            }

            showOrdersInLineItemsMode(orders);

            //m_view.getLineItemsViewer().showOrders(orders);
            this.getView().refresh();//.invalidate();
        }
        return true;
        */
    }

    public void adjustFocusOrderLayoutFirstShowingOrder()
    {
        if (m_orders == null) return;
        String focusedGuid = getEnv().getStateValues().getFocusedOrderGUID();
        if (focusedGuid.isEmpty()) return;
        synchronized (m_orders.m_locker) {
            if (m_orders.getOrderByGUID(focusedGuid) == null) return;
            for (int i = 0; i < m_orders.getCount(); i++) {
                String fromGuid = m_orders.get(i).getGUID();
                if (checkOrdersCanShowFocus(m_orders, fromGuid, focusedGuid)) {
                    getEnv().getStateValues().setFirstShowingOrderGUID(fromGuid);
                    return;
                }
            }
        }
    }


    private boolean checkOrdersCanShowFocus(KDSDataOrders orders, String firstOrderGuid, String focusedOrderGuid) {


        if (orders == null || orders.getCount() <= 0) {
            return true;
        }


        //t.debug_print_Duration("showOrders1");
        int nBlockRows = m_view.getAverageRowsInBlock();
        int nStartOrderIndex = 0;
        if (!firstOrderGuid.isEmpty())
        {
            nStartOrderIndex = orders.getIndex(firstOrderGuid);
        }
        //t.debug_print_Duration("showOrders2");
        if (nStartOrderIndex <0)
            nStartOrderIndex = 0;
        int ncount = orders.getCount();
        int nCounter = 0;
        int nMax = getMaxBlocksOrRows();

        for (int i = nStartOrderIndex; i < ncount; i++) {
            // t.debug_print_Duration("showOrders1");
            KDSDataOrder order = orders.get(i);
            // t.debug_print_Duration("showOrders2");
            int nNeeded= checkOrderShowing(order, nBlockRows);
            if (nNeeded<=0) return false;
            if (nCounter + nNeeded>nMax)
            {
                return false;
            }
            else {
                nCounter += nNeeded;
                if ( order.getGUID().equals(focusedOrderGuid))
                    return true;
            }
        }

        return false;

    }

    public  boolean isTextWrap()
    {
        return this.getEnv().getSettings().getBoolean(KDSSettings.ID.Text_wrap);

    }

    public boolean showOrder(KDSDataOrder order, int nBlockRows) {

        //TimeDog t = new TimeDog();

        KDSLayoutOrder dressedOrder = createLayoutOrder(order);
        //t.debug_print_Duration("showOrder1");
        if (dressedOrder == null)
            return true; //"The "showing paid order" items showing method maybe return null
        //this.getEnv().getSettings().getBoolean()
        boolean bEnableAddon = true;
        boolean bNeedVoidRow = isNeedToAddVoidRow();
        //int nNeedRowsWithoutTitleFooter = dressedOrder.get_need_how_many_rows_without_title_footer(bEnableAddon, bNeedVoidRow);
        int nNeedRowsWithoutTitleFooter = get_need_how_many_rows_without_title_footer(dressedOrder,bEnableAddon, bNeedVoidRow);
        //int nTitleRows = this.getSettings().getOrderTitleRows();

        //nNeedRowsWithTitleWithoutFooter += (nTitleRows -1);
        int nTitleRows = getEnv().getSettings().getInt(KDSSettings.ID.Order_Title_Rows);
        int nBlockDataRows = nBlockRows - nTitleRows - getEnv().getSettings().getInt(KDSSettings.ID.Order_Footer_Rows);//.getFooterRows();
        if (nBlockDataRows <=0) return false;
        KDSViewPanel panel = null;
        KDSSettings.LayoutFormat layoutFormat = getEnv().getSettingLayoutFormat();
        if (layoutFormat == KDSSettings.LayoutFormat.Horizontal) {
            int n = nNeedRowsWithoutTitleFooter;
            int nBlocks = n / nBlockDataRows;
            if ((n % nBlockDataRows) > 0)
                nBlocks++;
            panel = m_view.panelAdd();
            if (panel == null) return false;
            if (!(panel instanceof KDSViewPanelHorizontal))
                return false;
            KDSViewPanelHorizontal hpanel = (KDSViewPanelHorizontal) panel;
            hpanel.setCols(nBlocks);
            // return true;
            return showOrderInHorizontalPanel(hpanel, nBlockRows, dressedOrder);

        } else if (layoutFormat == KDSSettings.LayoutFormat.Vertical) {
            int nRows = nNeedRowsWithoutTitleFooter;
            nRows += nTitleRows;//this.getEnv().getTitleRows();
            nRows += this.getEnv().getSettings().getInt(KDSSettings.ID.Order_Footer_Rows);
            panel = m_view.panelAdd();
            if (panel == null) return false;
            if (!(panel instanceof KDSViewPanelVertical))
                return false;
            KDSViewPanelVertical vpanel = (KDSViewPanelVertical) panel;
            vpanel.setRows(nRows);
            return showOrderInVerticalPanel(vpanel, dressedOrder);
        }
        //t.debug_print_Duration("showOrder2");
        return true;

    }

    private int checkOrderShowing(KDSDataOrder order, int nBlockRows) {

        //TimeDog t = new TimeDog();

        KDSLayoutOrder dressedOrder = createLayoutOrder(order);
        //t.debug_print_Duration("showOrder1");
        if (dressedOrder == null)
            return -1; //"The "showing paid order" items showing method maybe return null
        //this.getEnv().getSettings().getBoolean()
        boolean bEnableAddon = true;
        boolean bAddVoidRow = isNeedToAddVoidRow();
        //int nNeedRowsWithoutTitleFooter = dressedOrder.get_need_how_many_rows_without_title_footer(bEnableAddon, bAddVoidRow);
        int nNeedRowsWithoutTitleFooter = get_need_how_many_rows_without_title_footer(dressedOrder, bEnableAddon, bAddVoidRow);
        //int nTitleRows = this.getSettings().getOrderTitleRows();

        //nNeedRowsWithTitleWithoutFooter += (nTitleRows -1);
        int nTitleRows = getEnv().getSettings().getInt(KDSSettings.ID.Order_Title_Rows);
        int nBlockDataRows = nBlockRows - nTitleRows - getEnv().getSettings().getInt(KDSSettings.ID.Order_Footer_Rows);//.getFooterRows();
        KDSViewPanel panel = null;
        KDSSettings.LayoutFormat layoutFormat = getEnv().getSettingLayoutFormat();
        if (layoutFormat== KDSSettings.LayoutFormat.Horizontal) {
            int n = nNeedRowsWithoutTitleFooter;
            int nBlocks = n / nBlockDataRows;
            if ((n % nBlockDataRows) > 0)
                nBlocks++;
            return nBlocks;


        } else if (layoutFormat == KDSSettings.LayoutFormat.Vertical) {
            int nRows = nNeedRowsWithoutTitleFooter;
            nRows += nTitleRows;//this.getEnv().getTitleRows();
            nRows += this.getEnv().getSettings().getInt(KDSSettings.ID.Order_Footer_Rows);
            return nRows;
        }
        //t.debug_print_Duration("showOrder2");
        return -1;

    }

    public int getMaxBlocksOrRows()
    {
        KDSSettings.LayoutFormat layoutFormat = getEnv().getSettingLayoutFormat();
        if (layoutFormat == KDSSettings.LayoutFormat.Horizontal) {
            return m_view.getBlocksRows() * m_view.getBlocksCols();

        } else if (layoutFormat == KDSSettings.LayoutFormat.Vertical) {
             return m_view.getAverageRowsInBlock() * m_view.getBlocksCols();
        }
        return 0;
    }



//    private KDSLayoutCell addCellToPanel(KDSViewPanel panel, Object obj) {
//        return addCellToPanel(panel, obj, KDSLayoutCell.CellSubType.Unknown, 1);
//
//    }

    /**
     *
     * @param panel
     * @param obj

     *  this obj need how many rows to show. default is 1.
     * @return
     */
    private KDSLayoutCell addCellToPanel(KDSViewPanel panel, Object obj) {
        return addCellToPanel(panel, obj, KDSLayoutCell.CellSubType.Unknown);

    }
//    private KDSLayoutCell addCellToPanel(KDSViewPanel panel, Object obj, KDSLayoutCell.CellSubType subtype) {
//        return addCellToPanel(panel, obj, subtype, 1);
//    }

    /**
     *
     * @param panel
     * @param obj
     * @param subtype


     * @return
     *  Return first cell, as we just draw first combined cell.
     */
    private KDSLayoutCell addCellToPanel(KDSViewPanel panel, Object obj, KDSLayoutCell.CellSubType subtype) {
        KDSLayoutCell c = null;

        c = new KDSLayoutCell();
        c.setData(obj);
        c.setCellSubType(subtype);
        c.m_nTextWrapRowIndex = 0;
        if (panel.addCell(c) == null)
            return null;

        return c;

    }
//    private KDSLayoutCell addCellToPanel(KDSViewPanel panel, Object obj, KDSLayoutCell.CellSubType subtype, int nOccupiedCells) {
//        KDSLayoutCell c = null;
//        KDSLayoutCell cellReturn = null;
//        KDSLayoutCell cellPrev = null;
//
//        for (int i=0; i< nOccupiedCells; i++) {
//            c = new KDSLayoutCell();
//            c.setData(obj);
//            c.setCellSubType(subtype);
//            c.m_nTextWrapRowIndex = i;
//            if (panel.addCell(c) == null)
//                return null;
//
//            if (i ==0) {
//                cellReturn = c;
//                cellPrev = c;
//            }
//            else
//            {
//                if (cellPrev!= null) {//combine them
//                    cellPrev.m_nextCell = c;
//                    c.m_prevCell = cellPrev;
//                    cellPrev = c;
//                }
//            }
//
//
//
//        }
//        return cellReturn;
//
//    }

//    private KDSLayoutCell addCellToPanel(KDSViewPanel panel, Object obj, KDSLayoutCell.CellSubType subtype) {
//            KDSLayoutCell c = null;
//            c = new KDSLayoutCell();
//            c.setData(obj);
//            c.setCellSubType(subtype);
//            if (panel.addCell(c) == null)
//                return null;
//            else
//                return c;
//
//
//    }

//    private KDSLayoutCell addCellToPanelLastRow(KDSViewPanel panel, Object obj, KDSLayoutCell.CellSubType subtype) {
//        KDSLayoutCell c = null;
//        c = new KDSLayoutCell();
//        c.setData(obj);
//        c.setCellSubType(subtype);
//        if (panel.addCellToLastRow(c) == null)
//            return null;
//        else
//            return c;
//
//    }

    private KDSViewFontFace getTitleFontFace(KDSLayoutOrder dressedOrder) {

            return getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Normal_FontFace);//.getOrderTitleSettings().getFontFaceTitleNormal();

        //}
    }

    /**
     * @param panel
     * @param dressedOrder
     * @param bExpendTitle This title is not in main block
     * @return
     */
    private boolean addOrderTitle(KDSViewPanel panel, KDSLayoutOrder dressedOrder, boolean bExpendTitle) {
        KDSLayoutCell.CellSubType ct = KDSLayoutCell.CellSubType.Unknown;
        if (bExpendTitle)
            ct = KDSLayoutCell.CellSubType.OrderTitle_Expand;
        KDSLayoutCell cell = addCellToPanel(panel, dressedOrder, ct);
        if (cell == null)
            return false;
        KDSViewFontFace ff = getTitleFontFace(dressedOrder);
        cell.getFont().copyFrom(ff);
        int nTitleRows = getEnv().getSettings().getInt(KDSSettings.ID.Order_Title_Rows);
        if (nTitleRows == 2) {
            ct = KDSLayoutCell.CellSubType.OrderTitle_Second;
            if (bExpendTitle)
                ct = KDSLayoutCell.CellSubType.OrderTitle_Second_Expand;
            cell = addCellToPanel(panel, dressedOrder, ct);
            if (cell == null)
                return false;
            cell.getFont().copyFrom(ff);


        }
        return true;
    }

    /**
     * @param panel
     * @param nBlockRows
     * @param dressedOrder
     * @param nCurrentCounter
     * @return return new counter
     */
    private int checkReachLastHorizontalBlockRow(KDSViewPanelHorizontal panel, int nBlockRows, KDSLayoutOrder dressedOrder, int nCurrentCounter) {
        if ( (nCurrentCounter%nBlockRows) == nBlockRows - 1) {
            if (getEnv().getSettings().getInt(KDSSettings.ID.Order_Footer_Rows) > 0) //add footer
            {
                KDSViewBlockCell cell = null;
                int ntotal =  panel.getTotalRows();
                if (nCurrentCounter > ntotal - nBlockRows) //last block col
                {
                    cell =  addCellToPanel(panel, dressedOrder, KDSLayoutCell.CellSubType.OrderFooter_Last);
                }
                else
                    cell = addCellToPanel(panel, dressedOrder, KDSLayoutCell.CellSubType.OrderFooter);
                nCurrentCounter++;
                cell.setFontFace(getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Footer_FontFace));

            }
        }
        return nCurrentCounter;

    }

    private int checkReachFirstHorizontalBlockRow(KDSViewPanelHorizontal panel, int nBlockRows, KDSLayoutOrder dressedOrder, int nCurrentCounter) {
        if ((nCurrentCounter % nBlockRows) == 0) {
            addOrderTitle(panel, dressedOrder, true);
            int nTitleRows = getEnv().getSettings().getInt(KDSSettings.ID.Order_Title_Rows);
            nCurrentCounter += nTitleRows;
        }
        return nCurrentCounter;

    }
    private KDSLayoutCell addPremessage(KDSViewPanel panel,KDSDataMessage data)
    {
        KDSLayoutCell cell = addCellToPanel(panel, data);
        if (cell == null)
            return cell;
        //set font
        cell.setFontFace(this.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Message_Default_FontFace));
        if (data.isColorValid())
        {
            cell.getFont().setBG(data.getBG());
            cell.getFont().setFG(data.getFG());

        }
        return cell;
    }

    /**
     *
     * @param panel
     * @param data
     * @return
     *  How many lines this item occupied
     */
    private KDSLayoutCell addItem(KDSViewPanel panel,KDSDataItem data)
    {
        KDSLayoutCell cell = null;
//        if (isTextWrap())
//            cell = addCellToPanel(panel, data, data.m_tempShowMeNeedBlockLines.size());
//        else
            cell = addCellToPanel(panel, data);
        if (cell == null)
            return cell;
        //set font
        cell.setFontFace(this.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Item_Default_FontFace));
        if (data.isAssignedColor())
        {
            cell.getFont().setBG( data.getBG());
            cell.getFont().setFG(data.getFG());
        }
        return cell;
    }

    private KDSLayoutCell addModifier(KDSViewPanel panel,KDSDataModifier data)
    {
        KDSLayoutCell cell = addCellToPanel(panel, data);
        if (cell == null)
            return cell;
        //set font
        cell.setFontFace(this.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Condiment_Default_FontFace));
        if (data.isAssignedColor())
        {
            cell.getFont().setBG( data.getBG());
            cell.getFont().setFG(data.getFG());
        }
        return cell;
    }

    private KDSLayoutCell addCondiment(KDSViewPanel panel,KDSDataCondiment data)
    {
        KDSLayoutCell cell = addCellToPanel(panel, data);
        if (cell == null)
            return cell;
        //set font
        cell.setFontFace(this.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Condiment_Default_FontFace));
        if (data.isAssignedColor())
        {
            cell.getFont().setBG( data.getBG());
            cell.getFont().setFG(data.getFG());
        }
        return cell;
    }
    private  int checkReachHorizontalFirstOrLastRow(KDSViewPanelHorizontal panel, int nBlockRows, KDSLayoutOrder dressedOrder, int nCurrentCounter)
    {
        int ncounter = nCurrentCounter;
        ncounter = checkReachFirstHorizontalBlockRow(panel, nBlockRows, dressedOrder, ncounter);
        ncounter = checkReachLastHorizontalBlockRow(panel, nBlockRows, dressedOrder, ncounter);
        ncounter = checkReachFirstHorizontalBlockRow(panel, nBlockRows, dressedOrder, ncounter);
        return ncounter;
    }

    private boolean addAddOnString(KDSViewPanel panel )
    {
        KDSDataMessage m = new KDSDataMessage("");
        m.setForComponentType(KDSDataMessage.FOR_Order);
        m.setMessage("Add-on");

        KDSViewBlockCell cell = addCellToPanel(panel, m);
        if (cell == null)
            return false;
        //set font
        cell.setFontFace(this.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Message_Default_FontFace));
        return true;
    }

    private boolean addVoidItemIndicator(KDSViewPanel panel, KDSDataItem item )
    {
        KDSDataVoidItemIndicator m = new KDSDataVoidItemIndicator();
        m.setParent(item);
        m.setDescription(this.getEnv().getSettings().getString(KDSSettings.ID.Void_add_message));

        KDSViewBlockCell cell = addCellToPanel(panel, m);
        if (cell == null)
            return false;
        //set font
        cell.setFontFace(this.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Message_Default_FontFace));
        return true;
    }

    private boolean addVoidItemQtyChange(KDSViewPanel panel, KDSDataItem item )
    {
        KDSDataVoidItemQtyChanged m = new KDSDataVoidItemQtyChanged();
        m.setParent(item);
        item.copyTo(m);



        KDSViewBlockCell cell = addCellToPanel(panel, m);
        if (cell == null)
            return false;
        //set font
        cell.setFontFace(this.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Message_Default_FontFace));
        if (getEnv().getSettings().getBoolean(KDSSettings.ID.Void_qty_line_color_enabled))
        {
            String s = getEnv().getSettings().getString(KDSSettings.ID.Void_qty_line_color);
            KDSBGFG c = KDSBGFG.parseString(s);

            cell.getFont().setBG(c.getBG());
            cell.getFont().setFG(c.getFG());

        }


        return true;
    }

    /**
     * this is for
     * @return
     */
    public int getBgThroughMyOrderState( KDSLayoutOrder dressedOrder)
    {
        return getEnv().getSettings().getExpAlertBgColor(dressedOrder.isItemsAllBumpedInExp());


    }

    private boolean isNeedToAddVoidRow()
    {
        int n = getEnv().getSettings().getInt(KDSSettings.ID.Void_showing_method);
        KDSSettings.VoidShowingMethod method = KDSSettings.VoidShowingMethod.values()[n];
        switch (method)
        {

            case Direct_Qty:
                if (getEnv().getSettings().getBoolean(KDSSettings.ID.Void_add_message_enabled))
                {

                    return true;
                }
                break;
            case Add_void:
            {

                return true;
            }

        }
        return false;
    }

    private boolean addVoid(KDSViewPanel panel,  KDSLayoutOrder dressedOrder, KDSDataItem item)
    {
        int n = getEnv().getSettings().getInt(KDSSettings.ID.Void_showing_method);
        KDSSettings.VoidShowingMethod method = KDSSettings.VoidShowingMethod.values()[n];
        switch (method)
        {

            case Direct_Qty:
                if (getEnv().getSettings().getBoolean(KDSSettings.ID.Void_add_message_enabled))
                {
                    addVoidItemIndicator(panel, item);
                    return true;
                }
                break;
            case Add_void:
            {
                addVoidItemQtyChange(panel, item);
                return true;
            }

        }
        return false;
    }

    /**
     *
     * @param messages
     * @param panel
     * @param nBlockRows
     * @param dressedOrder
     * @param nCurrentCounter
     * @return
     *  the new ncounter value.
     *  -1: error.
     */
    private int horizontalPanelShowOrderMessages(KDSDataMessages messages, KDSViewPanelHorizontal panel, int nBlockRows, KDSLayoutOrder dressedOrder, int nCurrentCounter)
    {
        int ncounter = nCurrentCounter;

        int ncount = messages.getCount();
        for (int i = 0; i < ncount; i++) {
            // if (!addPremessage(panel, (KDSDataMessage) dressedOrder.getOrderMessages().get(i)))
            //     return false;
            KDSDataMessage msg = messages.getMessage(i);
            int nTextRows = msg.m_tempShowMeNeedBlockLines.size();
            if (!isTextWrap()) nTextRows = 1;
            for (int r = 0; r < nTextRows; r++) {
                KDSLayoutCell cell = addPremessage(panel, msg);
                if (cell == null) return -1;
                cell.m_nTextWrapRowIndex = r;
                ncounter++;
                ncounter = checkReachHorizontalFirstOrLastRow(panel, nBlockRows, dressedOrder, ncounter);
            }
        }
        return ncounter;
    }

    private int horizontalPanelShowItemMessages(KDSDataItem item,
                                                  KDSDataMessages messages,
                                                  KDSViewPanelHorizontal panel,
                                                  int nBlockRows,
                                                  KDSLayoutOrder dressedOrder,
                                                  int nCurrentCounter,
                                                  int nCurrentContentIndex)
    {
        int ncounter = nCurrentCounter;

        int ncount = messages.getCount();
        for (int i = 0; i < ncount; i++) {
            // if (!addPremessage(panel, (KDSDataMessage) dressedOrder.getOrderMessages().get(i)))
            //     return false;
            KDSDataMessage msg = messages.getMessage(i);
            int nTextRows = msg.m_tempShowMeNeedBlockLines.size();
            if (!isTextWrap()) nTextRows = 1;
            if (nCurrentContentIndex == 0 && item.getHidden())
                msg.setFocusTag(item);

            for (int r = 0; r < nTextRows; r++) {
                KDSLayoutCell cell = addPremessage(panel, msg);
                if (cell == null) return -1;
                cell.m_nTextWrapRowIndex = r;
                ncounter++;
                ncounter = checkReachHorizontalFirstOrLastRow(panel, nBlockRows, dressedOrder, ncounter);
            }
            nCurrentContentIndex ++;
        }
        return ncounter;
    }

    /**
     * for modifier and condiments.
     * @param item
     * @param modifiers
     * @param panel
     * @param nBlockRows
     * @param dressedOrder
     * @param nCurrentCounter
     * @param nCurrentContentIndex
     * @return
     */
    private int horizontalPanelShowCondiments(KDSDataItem item,
                                              KDSDataCondiments modifiers,
                                              KDSViewPanelHorizontal panel,
                                              int nBlockRows,
                                              KDSLayoutOrder dressedOrder,
                                              int nCurrentCounter,
                                              int nCurrentContentIndex)
    {
        int ncounter = nCurrentCounter;
        int modifiersCount = modifiers.getCount();
        for (int j = 0; j < modifiersCount; j++) {
            KDSDataCondiment c = modifiers.getCondiment(j);
            if (c.getHiden())
                continue;//for hidden condiments

            if (nCurrentContentIndex == 0 && item.getHidden())
                c.setFocusTag(item);

            int nTextRows = c.m_tempShowMeNeedBlockLines.size();
            if (!isTextWrap()) nTextRows = 1;
            for (int r=0; r< nTextRows ; r++)
            {

                KDSLayoutCell cell = null;
                if (c instanceof KDSDataModifier)
                    cell = addModifier(panel, (KDSDataModifier) c);
                else
                    cell = addCondiment(panel, c);
                if (cell == null) return -1;
                cell.m_nTextWrapRowIndex = r;
                ncounter ++;
                ncounter = checkReachHorizontalFirstOrLastRow(panel, nBlockRows, dressedOrder, ncounter);
            }

        }
        return ncounter;
    }

    /**
     * @param panel
     * @param nBlockRows How many rows in one block
     * @return
     */
    private boolean showOrderInHorizontalPanel(KDSViewPanelHorizontal panel, int nBlockRows, KDSLayoutOrder dressedOrder) {

        int ncounter = 0;
        int ncount = 0;
        boolean bShowAddon = true;
        panel.setBG(getBgThroughMyOrderState(dressedOrder));
        //title
        if (!addOrderTitle(panel, dressedOrder, false))
            return false;
        int nTitleRows = getEnv().getSettings().getInt(KDSSettings.ID.Order_Title_Rows);
        ncounter += nTitleRows; //title rows
        boolean bShowMessageUnderOrder = getEnv().getSettings().getBoolean(KDSSettings.ID.Message_order_bottom);
        //order messages
        if (!bShowMessageUnderOrder) {
            ncounter = horizontalPanelShowOrderMessages(dressedOrder.getOrderMessages(), panel, nBlockRows, dressedOrder, ncounter);
            if (ncounter <0) return false;
        }

        //items
        boolean bShowMessageAboveItem = getEnv().getSettings().getBoolean(KDSSettings.ID.Message_item_above);

        int nLastGroupID = -1;
        ncount = dressedOrder.getItems().getCount();
        for (int i = 0; i < ncount; i++) {
            KDSDataItem item = dressedOrder.getItems().getItem(i);
            if (bShowAddon) {
                if (item.getAddOnGroup() != nLastGroupID) {
                    nLastGroupID = item.getAddOnGroup();
                    //show add-on
                    addAddOnString(panel);

                    ncounter++;
                    ncounter = checkReachHorizontalFirstOrLastRow(panel, nBlockRows, dressedOrder, ncounter);
                }
            }

            //show message above.
            int nContentIndex = 0; //as show data at top of item, this data will don't know which item it belong.
            if (bShowMessageAboveItem)
            {
                ncounter = horizontalPanelShowItemMessages(item, item.getMessages(),panel, nBlockRows, dressedOrder, ncounter, nContentIndex );
                if (ncounter <0) return false;
                nContentIndex += item.getMessages().getCount();

            }

            if (!item.getHidden()) { //if hidden item, don't draw it.

               // if (addItem(panel, item) == null)
               //     return false;
                int nTextRows = item.m_tempShowMeNeedBlockLines.size();
                if (!isTextWrap()) nTextRows = 1;
                for (int r=0; r< nTextRows ; r++)
                {
                    KDSLayoutCell cell = addItem(panel, item);
                    if (cell == null) return false;
                    cell.m_nTextWrapRowIndex = r;
                    ncounter ++;
                    ncounter = checkReachHorizontalFirstOrLastRow(panel, nBlockRows, dressedOrder, ncounter);
                }

                //ncounter++;
                //ncounter += item.m_tempShowMeNeedBlockLines.size();
                //ncounter = checkReachHorizontalFirstOrLastRow(panel, nBlockRows, dressedOrder, ncounter);
                if (item.isQtyChanged())
                {
                    if (addVoid(panel, dressedOrder, item)) {
                        ncounter++;
                        ncounter = checkReachHorizontalFirstOrLastRow(panel, nBlockRows, dressedOrder, ncounter);
                    }
                }
            }

            if (!bShowMessageAboveItem) {

                ncounter = horizontalPanelShowItemMessages(item, item.getMessages(),panel, nBlockRows, dressedOrder, ncounter, nContentIndex );
                if (ncounter <0) return false;
                nContentIndex += item.getMessages().getCount();

            }

            //show modifiers. As the modifiers is same with condiments, use same code
            ncounter = horizontalPanelShowCondiments(item, item.getModifiers(), panel, nBlockRows, dressedOrder, ncounter, nContentIndex);
            if (ncounter <0) return false;
            nContentIndex += item.getModifiers().getCount();

            //show condiments
            ncounter = horizontalPanelShowCondiments(item, item.getCondiments(), panel, nBlockRows, dressedOrder, ncounter, nContentIndex);
            if (ncounter <0) return false;
            nContentIndex += item.getCondiments().getCount();


        }

        if (bShowMessageUnderOrder) {
            ncounter = horizontalPanelShowOrderMessages(dressedOrder.getOrderMessages(), panel, nBlockRows, dressedOrder, ncounter);
            if (ncounter <0) return false;

        }

        if (getEnv().getSettings().getInt(KDSSettings.ID.Order_Footer_Rows) > 0)
        {
            //add to last row
            int ntotalrows = panel.getTotalRows();
            int nleftover = ntotalrows - ncounter-1;
            for (int i=0; i< nleftover; i++) {
                KDSViewBlockCell c = addCellToPanel(panel, null);

                c.setFontFace(getEnv().getSettings().getViewBlockFont());//.getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace));
            }
            KDSViewBlockCell cell = addCellToPanel(panel, dressedOrder, KDSLayoutCell.CellSubType.OrderFooter_Last);
            if (cell != null)
                cell.setFontFace(getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Footer_FontFace));
        }

        return true;

    }

    private boolean showOrderInVerticalPanel(KDSViewPanelVertical panel, KDSLayoutOrder dressedOrder) {

        //KDSLayoutCell c = null;
        int ncount = 0;
        boolean bShowAddon = true;
        panel.setBG(getBgThroughMyOrderState(dressedOrder));
        //title
        if (!addOrderTitle(panel, dressedOrder, false))
            return false;
        //order messages
        ncount = dressedOrder.getOrderMessages().getCount();
        for (int i = 0; i < ncount; i++) {
            //if (addPremessage(panel, (KDSDataMessage) dressedOrder.getOrderMessages().get(i)) == null)
            //    return false;
            KDSDataMessage msg = dressedOrder.getOrderMessages().getMessage(i);
            int nTextRows = msg.m_tempShowMeNeedBlockLines.size();
            if (!isTextWrap()) nTextRows = 1;
            for (int r=0; r< nTextRows ; r++)
            {
                KDSLayoutCell cell = addPremessage(panel, msg);
                if (cell == null) return false;
                cell.m_nTextWrapRowIndex = r;
            }
        }
        int nLastGroupID = -1;
        //items
        ncount = dressedOrder.getItems().getCount();
        for (int i = 0; i < ncount; i++) {

            KDSDataItem item = dressedOrder.getItems().getItem(i);
            if (bShowAddon) {
                if (item.getAddOnGroup() != nLastGroupID) {
                    nLastGroupID = item.getAddOnGroup();
                    //show add-on
                    addAddOnString(panel);
                }
            }

            if (!item.getHidden()) {
//                if (addItem(panel, item)== null)
//                    return false;
                int nTextRows = item.m_tempShowMeNeedBlockLines.size();
                if (!isTextWrap()) nTextRows = 1;
                for (int r=0; r< nTextRows ; r++)
                {
                    KDSLayoutCell cell = addItem(panel, item);
                    if (cell == null) return false;
                    cell.m_nTextWrapRowIndex = r;
                }
                if (item.isQtyChanged())
                {
                    addVoid(panel,dressedOrder, item );
                }
            }

            int nContentIndex = 0;

            int messages = item.getMessages().getCount();
            for (int j = 0; j < messages; j++) {

                KDSDataMessage m = (KDSDataMessage)item.getMessages().get(j);
                if ( (nContentIndex == 0) && item.getHidden())
                    m.setFocusTag(item);
                //if (addPremessage(panel,m ) == null)
                //    return false;

                int nTextRows = m.m_tempShowMeNeedBlockLines.size();
                if (!isTextWrap()) nTextRows = 1;
                for (int r=0; r< nTextRows ; r++)
                {
                    KDSLayoutCell cell = addPremessage(panel, m);
                    if (cell == null) return false;
                    cell.m_nTextWrapRowIndex = r;
                }
                nContentIndex ++;
            }

            int nmodifiers = item.getModifiers().getCount();
            for (int j = 0; j < nmodifiers; j++) //j=2
            {
                KDSDataModifier c = dressedOrder.getItems().getItem(i).getModifiers().getModifier(j);
                ;
                if ( c.getHiden())
                    continue;
                if ( (nContentIndex == 0) && item.getHidden())
                    c.setFocusTag(item);//just first content has item tag
//                if (!addModifier(panel,c ))
//                    return false;
                int nTextRows = c.m_tempShowMeNeedBlockLines.size();
                if (!isTextWrap()) nTextRows = 1;
                for (int r=0; r< nTextRows ; r++)
                {
                    KDSLayoutCell cell = addModifier(panel, c);
                    if (cell == null) return false;
                    cell.m_nTextWrapRowIndex = r;

                }
                nContentIndex ++;

            }


            int condiments = item.getCondiments().getCount();
            for (int j = 0; j < condiments; j++) //j=2
            {
                KDSDataCondiment c = dressedOrder.getItems().getItem(i).getCondiments().getCondiment(j);
                ;
                if ( c.getHiden())
                    continue;
                if ( (nContentIndex == 0) && item.getHidden())
                    c.setFocusTag(item);//just first content has item tag
                //if (!addCondiment(panel,c ))
                //    return false;
                int nTextRows = c.m_tempShowMeNeedBlockLines.size();
                if (!isTextWrap()) nTextRows = 1;
                for (int r=0; r< nTextRows ; r++)
                {
                    KDSLayoutCell cell = addCondiment(panel, c);
                    if (cell == null) return false;
                    cell.m_nTextWrapRowIndex = r;

                }
                nContentIndex ++;

            }

        }

        if (getEnv().getSettings().getInt(KDSSettings.ID.Order_Footer_Rows) > 0)
            addCellToPanel(panel, dressedOrder, KDSLayoutCell.CellSubType.OrderFooter);

        return true;
    }

    //create the order will been show
    public KDSLayoutOrder createLayoutOrder(KDSDataOrder order) {
        if (order == null)
            return null;
        KDSLayoutOrder dressedOrder = new KDSLayoutOrder();
        order.copyTo(dressedOrder);
        dressedOrder.setOriginalOrder(order);

        //smart showing
//        boolean bSmartEnabled = (KDSSettings.SmartMode.values()[ this.getEnv().getSettings().getInt(KDSSettings.ID.Smart_mode)] == KDSSettings.SmartMode.Normal );
//        //if (this.getEnv().getSettings().getBoolean(KDSSettings.ID.Smart_Order_Enabled))
//        if (bSmartEnabled)
//        {
//            int n = this.getEnv().getSettings().getInt(KDSSettings.ID.Smart_Order_Showing);
//            KDSSettings.SmartOrderShowing m = KDSSettings.SmartOrderShowing.values()[n];
//            if (m == KDSSettings.SmartOrderShowing.Gray)
//            {
//
//                int bg = this.getEnv().getSettings().getInt(KDSSettings.ID.Panels_BG);//
//                int fg = Color.LTGRAY;//GRAY;
//                dressedOrder.smartOrderGrayColorShowing(bg,fg);
//
//            }
//            else if (m == KDSSettings.SmartOrderShowing.Hide)
//            {
//                dressedOrder.smartOrderHideShowing();
//            }
//
//        }

        if (this.getEnv().getSettings().getBoolean(KDSSettings.ID.Item_group_category))
        {
            KDSLayoutOrder.buildGroupCategory(dressedOrder);
        }

        //2.0.9, remove qty changed item.
        dressedOrder.buildRemoveQtyChangedItemsThatAddInLineItemsMode();
        //Preparation mode showing 20180104
        //boolean bPrepEnabled = (KDSSettings.SmartMode.values()[ this.getEnv().getSettings().getInt(KDSSettings.ID.Smart_mode)] == KDSSettings.SmartMode.Advanced );
        boolean bSmartEnabled = this.getEnv().getSettings().getBoolean(KDSSettings.ID.Smart_Order_Enabled);
        //if (this.getEnv().getSettings().getBoolean(KDSSettings.ID.Prep_mode_enabled))
        if (bSmartEnabled)//bPrepEnabled)
        {
            int n = this.getEnv().getSettings().getInt(KDSSettings.ID.Smart_Order_Showing);
            KDSSettings.SmartOrderShowing m = KDSSettings.SmartOrderShowing.values()[n];
            if (m == KDSSettings.SmartOrderShowing.Gray)
            {

                int bg = this.getEnv().getSettings().getInt(KDSSettings.ID.Panels_BG);//
                int fg = Color.LTGRAY;//GRAY;
                dressedOrder.prepOrderGrayColorShowing(bg,fg);

            }
            else if (m == KDSSettings.SmartOrderShowing.Hide)
            {
                dressedOrder.prepOrderHideShowing();
            }

        }

        if (this.getEnv().getSettings().getBoolean(KDSSettings.ID.Item_Consolidate))
            dressedOrder.consolidateItems();


        if (this.getEnv().getSettings().getBoolean(KDSSettings.ID.Queue_double_bump_expo_order))
            dressedOrder.buildDimColorForQueue();

        //for "one item behind" showing method.
        int nItemShowingMethod = this.getEnv().getSettings().getInt(KDSSettings.ID.Item_showing_method);
        KDSSettings.ItemShowingMethod itemShowingMethod = KDSSettings.ItemShowingMethod.values()[nItemShowingMethod];

        //for items showing method.
        dressedOrder = dressedOrder.buildItemShowingMethod(itemShowingMethod);
        //if (unpaid), in (paid showing method), it will hide
        if (dressedOrder ==  null) return null;

        if (KDSGlobalVariables.getKDS().getStationsConnections().getRelations().isBackupStation())
        {
            if (order.getFromPrimaryOfBackup())
            {
                dressedOrder.addFromPrimaryIndicators();
            }
        }

        if (order instanceof ScheduleProcessOrder)
        {
            dressedOrder = dressedOrder.buildScheduleOrder((ScheduleProcessOrder) order);
        }
        return dressedOrder;
    }

    public void onSizeChanged()
    {
        this.refresh();
    }


    /**
     * interface
     * @param panel
     * @param block
     * @param cell
     */
    public  void onViewPanelClicked(KDSView view, KDSViewPanel panel, KDSViewBlock block, KDSViewBlockCell cell)
    {
        if (m_eventsReceiver != null)
            m_eventsReceiver.onViewPanelClicked(this);
        if (panel == null) {

            return;
        }
        if (panel.getFirstBlock() == null) return;//KPP1-60
        KDSViewBlockCell firstcell =  panel.getFirstBlock().getCell(0);
        if (firstcell == null) return;
        Object obj = firstcell.getData();
        if (obj == null) return;
        if (!(obj instanceof  KDSDataOrder))
            return ; //some error, it should never happpen.
        KDSDataOrder order = (KDSDataOrder)obj;
        if (m_orders.getOrderByGUID(order.getGUID())== null) return; //2.0.12
        this.getEnv().getStateValues().setFocusedOrderGUID(order.getGUID());
        //check clicked what component
        //for item focusing
        if (cell == null) {
            this.getEnv().getStateValues().setFocusedItemGUID("");
            //return;
        }
        else {
            obj = cell.getData();
            if (obj == null) {
                this.getEnv().getStateValues().setFocusedItemGUID("");
               // return;
            }
            else if (obj instanceof  KDSLayoutOrder)
            { //
                this.getEnv().getStateValues().setFocusedItemGUID("");
            }
            else if (obj instanceof KDSDataMoreIndicator)
            { //this must at begining of kdsdataitem.

            }
            else if (obj instanceof KDSDataCategoryIndicator)
            { //2.0.47

            }
            else if (obj instanceof KDSDataFromPrimaryIndicator)
            {

            }
            else if (obj instanceof KDSDataVoidItemIndicator)
            {
                this.getEnv().getStateValues().setFocusedItemGUID( ((KDSDataVoidItemIndicator)obj).getParent().getGUID());
            }
            else if (obj instanceof KDSDataVoidItemQtyChanged)
            {
                this.getEnv().getStateValues().setFocusedItemGUID( ((KDSDataVoidItemQtyChanged)obj).getParent().getGUID());
            }
            else if (obj instanceof KDSDataItem) {
                this.getEnv().getStateValues().setFocusedItemGUID( ((KDSDataItem)obj).getGUID());
            } else if (obj instanceof KDSDataCondiment) {
                this.getEnv().getStateValues().setFocusedItemGUID(((KDSDataCondiment) obj).getItemGUID());

            } else if (obj instanceof KDSDataMessage) {
                KDSDataMessage msg = (KDSDataMessage)obj;
                int ntype =  msg.getForComponentType();
                if (ntype ==  KDSDataMessage.FOR_Order)
                {
                    this.getEnv().getStateValues().setFocusedItemGUID("");
                }
                else if (ntype == KDSDataMessage.FOR_Item)
                {
                    this.getEnv().getStateValues().setFocusedItemGUID(msg.getComponentGUID());
                }
                else if (ntype == KDSDataMessage.FOR_Condiment)
                { //TODO: should not have this previouse message.

                }
            }
        }
        if (m_orders != null)
            this.showOrders(m_orders);


    }

    /**
     * interface
     * @param panel
     * @param block
     * @param cell
     */
    public  void onViewPanelDoubleClicked(KDSView view, KDSViewPanel panel, KDSViewBlock block, KDSViewBlockCell cell)
    {

        if (panel == null) return;
        onViewPanelClicked(view ,panel, block, cell);
        //double click is in certain panel.
        //fire event
        if (m_eventsReceiver != null)
            m_eventsReceiver.onViewPanelDoubleClicked(this);


    }
    /**
     * check if this order is visible in view
     * @param orderGuid
     * @return
     */
    private boolean isOrderVisible(String orderGuid)
    {
        int ncount = this.getView().getPanels().size();
        for (int i=0; i< ncount; i++)
        {
            KDSViewPanel panel = this.getView().getPanels().get(i);
            if (panel == null)
                continue;
            KDSViewBlock block = panel.getFirstBlock();
            if (block == null)
                continue;
            String guid = ((KDSLayoutOrder)block.getCells().get(0).getData()).getGUID();
            if (guid == orderGuid)
                return true;
        }
        return false;
    }


    public void focusPanel(int nPanel)
    {

        String guid = getPanelOrderGuid(nPanel);
        if (guid.isEmpty()) return;
        focusOrder(guid);

    }

    public String getPanelOrderGuid(int nPanel)
    {
        int ncount = this.getView().getPanels().size();
        if (ncount <=0) return "";
        if (nPanel >= ncount) return "";

        KDSViewPanel panel = this.getView().getPanels().get(nPanel);
        if (panel == null)
            return "";
        KDSViewBlock block = panel.getFirstBlock();
        if (block == null)
            return "";
        String guid = ((KDSLayoutOrder)block.getCells().get(0).getData()).getGUID();
        return guid;
    }

    public String focusNext()
    {
        if (isLineItemsMode())
            return focusNextLineItem();
        else
            return focusNextOrder();

    }

    private String focusNextOrder()
    {


        String guid = this.getEnv().getStateValues().getFocusedOrderGUID();
        if (m_orders == null)
            return "";
        int nindex = m_orders.getIndex(guid);
        nindex++;

        KDSDataOrder order = null;
        int n = this.getEnv().getSettings().getInt(KDSSettings.ID.Item_showing_method);
        KDSSettings.ItemShowingMethod itemShowingMethod = KDSSettings.ItemShowingMethod.values()[n];
        if (itemShowingMethod == KDSSettings.ItemShowingMethod.When_order_is_paid)
            order = m_orders.getNextPaidOrderFrom(nindex);
        else
            order = m_orders.get(nindex);
        guid = "";
        if (order != null)
        {//return to first
            guid = order.getGUID();
        }
        else
        {
            if (itemShowingMethod == KDSSettings.ItemShowingMethod.When_order_is_paid)
                order = m_orders.getNextPaidOrderFrom(0);
            else
                order = m_orders.get(0);
            //order = m_orders.get(0);
            if (order != null)
                guid = order.getGUID();
        }
        focusOrder(guid);


        return guid;
    }

    public String focusPrev()
    {
        if (isLineItemsMode())
            return focusPrevLineItem();
        else
            return focusPrevOrder();
    }

    public String focusPrevOrder()
    {
        String guid = this.getEnv().getStateValues().getFocusedOrderGUID();
        if (m_orders == null)
            return "";
        int nindex = m_orders.getIndex(guid);
        nindex--;

        KDSDataOrder order = null;
        int n = this.getEnv().getSettings().getInt(KDSSettings.ID.Item_showing_method);
        KDSSettings.ItemShowingMethod itemShowingMethod = KDSSettings.ItemShowingMethod.values()[n];
        if (itemShowingMethod == KDSSettings.ItemShowingMethod.When_order_is_paid)
            order = m_orders.getPrevPaidOrderFrom(nindex);
        else
            order = m_orders.get(nindex);
        guid = "";
        if (order != null)
        {//return to first
            guid = order.getGUID();
        }
        else
        {
            if (itemShowingMethod == KDSSettings.ItemShowingMethod.When_order_is_paid)
                order = m_orders.getPrevPaidOrderFrom(m_orders.getCount() -1);
            else
                order = m_orders.get(m_orders.getCount() -1);
            //order = m_orders.get(m_orders.getCount() -1 );
            if (order != null)
                guid = order.getGUID();
        }
        focusOrder(guid);


        return guid;
    }
    public boolean focusOrder(String orderGuid)
    {
        if (m_orders == null) return false;
        if (orderGuid.equals(KDSConst.RESET_ORDERS_LAYOUT))
        {//try best to show previous orders
            adjustFocusOrderLayoutFirstShowingOrder();
            return true;
        }
        if (m_orders.getIndex(orderGuid) <0)
            return false;
        String beforeFocusedItem = this.getEnv().getStateValues().getFocusedItemGUID();
        this.getEnv().getStateValues().setFocusedOrderGUID(orderGuid);
        if (!isOrderVisible(orderGuid))
        {
            this.getEnv().getStateValues().setFirstShowingOrderGUID(orderGuid);
            this.showOrders(m_orders);
        }
        else
        {

            if (!beforeFocusedItem.isEmpty())
                this.showOrders(m_orders);

        }

        return true;
    }


    /**
     * onTime interface.
     */
    public void refreshTimer()
    {

        if (this.getView().getOrdersViewMode() == KDSView.OrdersViewMode.Normal) {
            //boolean bSmartEnabled = this.getEnv().getSettings().getBoolean(KDSSettings.ID.Smart_Order_Enabled);
           // boolean bSmartEnabled = (KDSSettings.SmartMode.values()[ this.getEnv().getSettings().getInt(KDSSettings.ID.Smart_mode)] == KDSSettings.SmartMode.Normal );
            //boolean bPrepModeEnabled = this.getEnv().getSettings().getBoolean(KDSSettings.ID.Prep_mode_enabled);
            //boolean bPrepModeEnabled = (KDSSettings.SmartMode.values()[ this.getEnv().getSettings().getInt(KDSSettings.ID.Smart_mode)] == KDSSettings.SmartMode.Advanced );
            boolean bSmartEnabled =  this.getEnv().getSettings().getBoolean(KDSSettings.ID.Smart_Order_Enabled);
            if (bSmartEnabled){// || bPrepModeEnabled) {

                this.refresh();
                this.getView().invalidate();
                return;
            }

            this.getView().setJustRedrawTimer();
            this.getView().invalidate();
        }
        else
        {
            this.getView().getLineItemsViewer().onTimer();
        }

    }

    public void checkAlertSound()
    {
        if (m_orders == null) return;
        if (!getEnv().getSettings().getBoolean(KDSSettings.ID.Sound_enabled))
            return;
        for (int i=0; i<  m_orders.getCount(); i++)
        {
            KDSSettings.TimeAlertLevel alertLevel = this.getEnv().getSettings().checkAlertSound(m_orders.get(i));
            soundAlert(m_orders.get(i), alertLevel);
        }
    }
    private void soundAlert(KDSDataOrder order, KDSSettings.TimeAlertLevel alertLevel)
    {
        switch (alertLevel)
        {

            case None:
                break;
            case Alert1:
                KDSGlobalVariables.getKDS().getSoundManager().playSound(KDSSettings.ID.Sound_timer_alert1);
                order.setAlert1SoundFired(true);
                break;
            case Alert2:
                KDSGlobalVariables.getKDS().getSoundManager().playSound(KDSSettings.ID.Sound_timer_alert2);
                order.setAlert2SoundFired(true);
                break;
            case Alert3:
                KDSGlobalVariables.getKDS().getSoundManager().playSound(KDSSettings.ID.Sound_timer_alert3);
                order.setAlert3SoundFired(true);
                break;
        }
    }



    /**
     * if the items showing method changed, it can cause current focused order hidden.
     * So, I have to reset the focus order again.
     */
    public void checkPreferenceChangeItemShowingMethod()
    {
        int n = this.getEnv().getSettings().getInt(KDSSettings.ID.Item_showing_method);
        KDSSettings.ItemShowingMethod itemShowingMethod = KDSSettings.ItemShowingMethod.values()[n];


        if (itemShowingMethod == KDSSettings.ItemShowingMethod.When_order_is_paid)
        {//paid way, it will hiden some unpaid order
            String strFocusedOrderGuid = getEnv().getStateValues().getFocusedOrderGUID();
            if (m_orders == null) return;
            KDSDataOrder order =  m_orders.getOrderByGUID(strFocusedOrderGuid);
            if (order == null  ||
                    !order.isPaid())
            {
                String firstGuid = m_orders.getFirstPaidOrderGuid();
                this.getEnv().getStateValues().setFocusedOrderGUID(firstGuid);
               // this.showOrders(m_orders);
            }

        }
    }
    public void refresh()
    {
        if (m_orders != null)
            this.showOrders(m_orders);


    }

    public String getLastShowingOrderGuid()
    {
        try {
            synchronized (this.getView().m_panelsLocker) {
                if (this.getView().getPanels().size() <= 0)
                    return "";
                if (this.getView().getLastPanel() == null) return "";

                Object obj = this.getView().getLastPanel().getFirstBlockFirstRowData();
                if (obj == null)
                    return "";
                if (!(obj instanceof KDSDataOrder))
                    return "";
                String guid = ((KDSDataOrder) obj).getGUID();
                return guid;
            }
        }
        catch (Exception e)
        {
            //KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
            return "";
        }
    }
    public int getNextCount()
    {
//        if (this.getView().getPanels().size()<=0)
//            return 0;
//        Object obj = this.getView().getLastPanel().getFirstBlockFirstRowData();
//        if (obj == null)
//            return 0;
//        if (!(obj instanceof  KDSDataOrder ))
//            return 0;
//
//
//        String guid = ((KDSDataOrder)obj).getGUID();
//
        String guid = getLastShowingOrderGuid();
        if (guid.isEmpty()) {
            if (m_orders != null && m_orders.getCount() >0)
            {//as drawing in thread, maybe ui don't refresh. So change nothing.

                return -1;
            }
            return 0;
        }

        if (!this.isLayoutFull())
        {
            if (m_orders.getCount() >= getMaxBlocksOrRows()) {
                String firstGuid = this.getEnv().getStateValues().getFirstShowingOrderGUID();
                if (!firstGuid.isEmpty()) {
                    int nFirst = m_orders.getIndex(firstGuid);
                    if (nFirst == 0)
                        return -1;//In different thread drawing, we have to check this.
                }


            }

            return 0;
        }
        int n = m_orders.getIndex(guid);

        return m_orders.getCount() - n -1;

    }
    public int getPrevCount()
    {
        if (this.getView().getPanels().size()<=0)
            return 0;
        String guid = this.getEnv().getStateValues().getFirstShowingOrderGUID();
        if (guid.isEmpty()) {
            if (m_orders!= null &&
                m_orders.getCount()>0)
                return -1;
            return 0;
        }
        if (m_orders == null)
            return 0;
        int n = m_orders.getIndex(guid);
        return n;
    }

    public void debug_me()
    {
        KDSDataOrders orders = new KDSDataOrders();
        for (int i=0; i< 10; i++)
        {
            KDSDataOrder order = KDSDataOrder.createTestOrder("Order"+Integer.toString(i), i+2, "1"); // rows = (i+2) * 6  +3 +titlerows;
            orders.addComponent(order);
        }
        this.showOrders(orders);
    }

    public void showOrdersInLineItemsMode(KDSDataOrders orders)
    {

        m_view.getLineItemsViewer().showOrders(orders);//, firstOrderGuid, firstItemGuid);
    }

    public KDSDataItem getNextActiveLineItem(String fromOrderGuid, String fromItemGuid)
    {
        int nOrderIndex = m_orders.getIndex(fromOrderGuid);
        if (nOrderIndex <0) return null;
        
        int nItemIndex = m_orders.getOrderByGUID(fromOrderGuid).getItems().getItemIndexByGUID(fromItemGuid);
        nItemIndex ++;
        for (int i=nOrderIndex; i< m_orders.getCount(); i++)
        {
            KDSDataOrder order = m_orders.get(i);
            if (order == null) continue;
            KDSDataItem item = order.getNextActiveItem(nItemIndex);
            nItemIndex = 0;
            if (item == null)
                continue;
            return item;
        }
        return null;
    }

//    /**
//     *
//     * @param fromOrderGuid
//     * @param fromItemGuid
//     * @param nDistance
//     * @return
//     */
//    public KDSDataItem getNextActiveLineItem(String fromOrderGuid, String fromItemGuid, int nDistance)
//    {
//        int nOrderIndex = m_orders.getIndex(fromOrderGuid);
//        if (nOrderIndex <0) return null;
//
//        int nItemIndex = m_orders.getOrderByGUID(fromOrderGuid).getItems().getItemIndexByGUID(fromItemGuid);
//        nItemIndex ++;
//        for (int i=nOrderIndex; i< m_orders.getCount(); i++)
//        {
//            KDSDataOrder order = m_orders.get(i);
//            if (order == null) continue;
//            KDSDataItem item = order.getNextActiveItem(nItemIndex);
//            nItemIndex = 0;
//            if (item == null)
//                continue;
//            return item;
//        }
//        return null;
//    }

    public KDSDataItem getPrevActiveLineItem(String fromOrderGuid, String fromItemGuid)
    {
        int nOrderIndex = m_orders.getIndex(fromOrderGuid);
        int nItemIndex = m_orders.getOrderByGUID(fromOrderGuid).getItems().getItemIndexByGUID(fromItemGuid);
        nItemIndex --;
        for (int i=nOrderIndex; i>=0; i--)
        {
            KDSDataOrder order = m_orders.get(i);
            if (order == null) continue;
            KDSDataItem item = order.getPrevActiveItem(nItemIndex);
            nItemIndex = Integer.MAX_VALUE;
            if (item == null)
                continue;
            return item;
        }
        return null;
    }

    /**
     * 2.0.25
     * @param fromOrderGuid
     * @param fromItemGuid
     * @param nDistance
     * @return
     */
    public KDSDataItem getPrevActiveLineItem(String fromOrderGuid, String fromItemGuid, int nDistance)
    {
        int nOrderIndex = m_orders.getIndex(fromOrderGuid);
        int nItemIndex = m_orders.getOrderByGUID(fromOrderGuid).getItems().getItemIndexByGUID(fromItemGuid);
        nItemIndex --;
        int nCounter = 0;
        for (int i=nOrderIndex; i>=0; i--)
        {
            KDSDataOrder order = m_orders.get(i);
            if (order == null) continue;
            int nActiveItemsCount = 0;
            if (i == nOrderIndex)
                nActiveItemsCount = order.getPrevActiveItemsCount(nItemIndex);
            else
                nActiveItemsCount = order.getActiveItemsCount();

            if (nCounter + nActiveItemsCount >=nDistance)
            {
                return order.getFirstActiveItem();
            }
            nCounter += nActiveItemsCount;

        }
        return null;
    }

    public KDSDataItem getFirstActiveLineItem()
    {
        int nOrderIndex = 0;
        int nItemIndex = 0;
        //nItemIndex --;

        for (int i=nOrderIndex; i<m_orders.getCount(); i++)
        {
            KDSDataOrder order = m_orders.get(i);
            if (order == null) continue;
            return order.getFirstActiveItem();

        }
        return null;
    }


    public String focusNextLineItem()
    {
        String orderGUID = this.getEnv().getStateValues().getFocusedOrderGUID();
        String itemGUID = this.getEnv().getStateValues().getFocusedItemGUID();

        if (m_orders == null)
            return "";
        if (orderGUID.isEmpty() || itemGUID.isEmpty()) {
            return focusFirstShowingLineItem();
        }



        KDSDataItem nextItem = getNextActiveLineItem(orderGUID, itemGUID);
        if (nextItem == null)
            return "";
        this.getEnv().getStateValues().setFocusedOrderGUID(nextItem.getOrderGUID());
        this.getEnv().getStateValues().setFocusedItemGUID(nextItem.getGUID());

        refreshLineItemsView();
        return itemGUID;
    }

    public String focusFirstShowingLineItem()
    {
        String orderGUID = this.getEnv().getStateValues().getFirstShowingOrderGUID();
        String itemGUID = this.getEnv().getStateValues().getFirstItemGuid();
        this.getEnv().getStateValues().setFocusedOrderGUID(orderGUID);
        this.getEnv().getStateValues().setFocusedItemGUID(itemGUID);
        refreshLineItemsView();
        return itemGUID;
    }

    public void refreshLineItemsView()
    {

        m_view.getLineItemsViewer().showOrders(m_orders);//, orderGUID, itemGUID);
        m_view.refresh();
    }

    public String focusPrevLineItem()
    {
        String orderGUID = this.getEnv().getStateValues().getFocusedOrderGUID();
        String itemGUID = this.getEnv().getStateValues().getFocusedItemGUID();

        if (m_orders == null)
            return "";
        if (orderGUID.isEmpty() || itemGUID.isEmpty()) {
            focusFirstShowingLineItem();
            return "";
        }

        int nOrderIndex = m_orders.getIndex(orderGUID);
        int nItemIndex = m_orders.getOrderByGUID(orderGUID).getItems().getItemIndexByGUID(itemGUID);

        KDSDataItem prevItem = getPrevActiveLineItem(orderGUID, itemGUID);
        if (prevItem == null)
            return focusFirstShowingLineItem();
        this.getEnv().getStateValues().setFocusedOrderGUID(prevItem.getOrderGUID());
        this.getEnv().getStateValues().setFocusedItemGUID(prevItem.getGUID());


        refreshLineItemsView();
        return itemGUID;
    }


    public void updateSettings(KDSSettings settings)
    {
        if (m_view != null)
        m_view.updateSettings(settings);
    }

    KDSViewFontFace getMessageFF()
    {
        return this.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Message_Default_FontFace);
    }


    KDSViewFontFace getItemFF()
    {
        return this.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Item_Default_FontFace);
    }

    KDSViewFontFace getCondimentFF()
    {
        return this.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Condiment_Default_FontFace);
    }

    final int TEXT_WRAP_fault_tolerant = 3;
    /**
     * we just consider the left align case.
     *
     * @param nAverageBlockWidth
     * @param item
     * @return
     */
    public int cal_item_need_lines(KDSDataItem item, int nAverageBlockWidth )
    {
        if (nAverageBlockWidth <=0) return 0;
        nAverageBlockWidth = getBlockColValidDataWidth(nAverageBlockWidth);

        //String s = KDSLayoutCell.getItemPrefix();
        float qty = item.getShowingQty();
        //String strDescription = item.getDescription();
//        s += Integer.toString((int) qty);
//        s += KDSLayoutCell.getItemQtySuffix();
        //s += strDescription;
//        Paint pt = new Paint();
//        pt.setTypeface(getItemFF().getTypeFace());
        int nPrefixWidth = KDSLayoutCell.getItemQtyPixelsWidth(getItemFF(), (int)qty);// CanvasDC.getTextPixelsWidth(pt, s);
//        int nDescriptionRoomWidth = nAverageBlockWidth - nPrefixWidth;
//        int nDescriptionWidth = CanvasDC.getTextPixelsWidth(pt, item.getDescription());
        Rect rect = new Rect(0,0,nAverageBlockWidth - nPrefixWidth, nAverageBlockWidth * 10);//
        ArrayList<Point> lines = CanvasDC.getWrapStringRows(getItemFF(), rect, item.getDescription(), Paint.Align.LEFT);

//        int nLines = nDescriptionWidth / nDescriptionRoomWidth;
//        if (nDescriptionWidth % nDescriptionRoomWidth >TEXT_WRAP_fault_tolerant)
//            nLines ++;
        item.m_tempShowMeNeedBlockLines = lines;
        return lines.size();
    }

    public int getBlockColValidDataWidth(int nAverageBlockWidth)
    {
        int n = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);
        n += getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);
        nAverageBlockWidth -= 2*n;
        return nAverageBlockWidth;
    }

    public int cal_message_need_lines(KDSDataMessage message, int nAverageBlockWidth )
    {
        if (nAverageBlockWidth <=0) return 0;
        nAverageBlockWidth = getBlockColValidDataWidth(nAverageBlockWidth);


        int nPrefixWidth = KDSLayoutCell.getMessagePrefixPixelsWidth(getMessageFF());

        Rect rect = new Rect(0,0,nAverageBlockWidth - nPrefixWidth, nAverageBlockWidth * 10);//
        ArrayList<Point> lines = CanvasDC.getWrapStringRows(getItemFF(), rect, message.getMessage(), Paint.Align.LEFT);


        message.m_tempShowMeNeedBlockLines = lines;
        return lines.size();
    }

    public int cal_condiment_need_lines(KDSDataCondiment condiment, int nAverageBlockWidth )
    {
        if (nAverageBlockWidth <=0) return 0;
        nAverageBlockWidth = getBlockColValidDataWidth(nAverageBlockWidth);


        int nPrefixWidth = KDSLayoutCell.getCondimentPrefixPixelsWidth(getCondimentFF(), getEnv());

        Rect rect = new Rect(0,0,nAverageBlockWidth - nPrefixWidth, nAverageBlockWidth * 10);//
        ArrayList<Point> lines = CanvasDC.getWrapStringRows(getItemFF(), rect, condiment.getDescription(), Paint.Align.LEFT);


        condiment.m_tempShowMeNeedBlockLines = lines;
        return lines.size();
    }

    public int get_need_how_many_rows_without_title_footer(KDSLayoutOrder order,boolean bEnableAddonString, boolean bAddVoidRow)
    {
        int ncount = order.getItems().getCount();
        int ncounter = 0;
        int nLastGroupID = -1;
        boolean bTextWrap = isTextWrap();
        //ncounter ++; //order title.
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = order.getItems().getItem(i);
            if (!item.getHidden()) {
                if (!bTextWrap)
                    ncounter++;//item
                else
                    ncounter += cal_item_need_lines(item, m_view.getBlockAverageWidth());
                if (item.isQtyChanged())
                {
                    if (bAddVoidRow)
                        ncounter ++;
                }
            }
            if (bEnableAddonString) { //add add-on string
                int nGrp = item.getAddOnGroup();
                if (nGrp != nLastGroupID) {
                    ncounter++;
                    nLastGroupID = nGrp;
                }
            }

            //item messages
            //ncounter += item.getMessages().getCount();
            if (!bTextWrap)
                ncounter += item.getMessages().getCount();//
            else {
                for (int j = 0; j< item.getMessages().getCount(); j++) {
                    KDSDataMessage msg = item.getMessages().getMessage(j);
                    ncounter += cal_message_need_lines(msg, m_view.getBlockAverageWidth());
                }
            }
            //count the condiments;
            int ncondiments = item.getCondiments().getCount();
            for (int j = 0; j< ncondiments; j++)
            {
                KDSDataCondiment condiment = item.getCondiments().getCondiment(j);
                if (!condiment.getHiden()) {
                    if (!bTextWrap)
                         ncounter++;//condiment
                    else
                    {
                        ncounter += cal_condiment_need_lines(condiment, m_view.getBlockAverageWidth());
                    }
                }
                //condiment message
                //item.getCondiments().getCondiment(j).getMessages().getCount();
            }

            //count the condiments;
            int nmodifiers = item.getModifiers().getCount();
            for (int j = 0; j< nmodifiers; j++)
            {
                KDSDataModifier modifier = item.getModifiers().getModifier(j);
                if (!modifier.getHiden()) {
                    if (!bTextWrap)
                        ncounter++;//condiment
                    else
                    {
                        ncounter += cal_condiment_need_lines(modifier, m_view.getBlockAverageWidth());
                    }
                }
                //condiment message
                //item.getModifiers().getModifier(j).getMessages().getCount();
            }
        }
        //order messages
        //ncounter += order.getOrderMessages().getCount();
        if (!bTextWrap)
            ncounter += order.getOrderMessages().getCount();//
        else {
            for (int j = 0; j< order.getOrderMessages().getCount(); j++) {
                KDSDataMessage msg = order.getOrderMessages().getMessage(j);
                ncounter += cal_message_need_lines(msg, m_view.getBlockAverageWidth());
            }
        }
        return ncounter;
    }

    /**
     * 2.0.15
     * @return
     */
    public boolean isFocusOrderVisible()
    {
        if (this.getView().getPanels().size()<=0)
            return true;
        Object obj = this.getView().getLastPanel().getFirstBlockFirstRowData();
        if (obj == null)
            return true;
        if (!(obj instanceof  KDSDataOrder ))
            return true;
        String guid = ((KDSDataOrder)obj).getGUID();
        int nLastOrderIndex = m_orders.getIndex(guid);

        String focusOrderGUID = getEnv().getStateValues().getFocusedOrderGUID();
        int nFocuseOrderIndex = m_orders.getIndex(focusOrderGUID);

        return (nFocuseOrderIndex<= nLastOrderIndex);

    }

    /**
     * 2.0.25
     * @return
     */
    public String focusNextPage()
    {
        if (isLineItemsMode())
            return focusNextPageLineItem();
        else
            return focusNextPageOrder();

    }

    /**
     * 2.0.25
     * @return
     */
    public String focusPrevPage()
    {
        if (isLineItemsMode())
            return focusPrevPageLineItem();
        else
            return focusPrevPageOrder();
    }

    /**
     * 2.0.25
     * @return
     */
    private String focusNextPageOrder()
    {
        if (m_orders == null)
            return "";
        String guid = getLastShowingOrderGuid();
        if (guid.isEmpty()) return "";

        int nindex = m_orders.getIndex(guid);
        nindex++;

        KDSDataOrder order = null;
        int n = this.getEnv().getSettings().getInt(KDSSettings.ID.Item_showing_method);
        KDSSettings.ItemShowingMethod itemShowingMethod = KDSSettings.ItemShowingMethod.values()[n];
        if (itemShowingMethod == KDSSettings.ItemShowingMethod.When_order_is_paid)
            order = m_orders.getNextPaidOrderFrom(nindex);
        else
            order = m_orders.get(nindex);
        guid = "";
        if (order != null)
        {//return to first
            guid = order.getGUID();
        }
        else
        {
            return "";
//            if (itemShowingMethod == KDSSettings.ItemShowingMethod.When_order_is_paid)
//                order = m_orders.getNextPaidOrderFrom(0);
//            else
//                order = m_orders.get(0);
//            //order = m_orders.get(0);
//            if (order != null)
//                guid = order.getGUID();
        }
        focusOrder(guid);

        return guid;
    }

    /**
     * 2.0.25
     * @return
     */
    public String focusNextPageLineItem()
    {
        if (m_orders == null)
            return "";

        String orderGUID = m_view.getLineItemsViewer().getLastOrderGuid();
        String itemGUID = m_view.getLineItemsViewer().getLastItemGuid();


        if (orderGUID.isEmpty() || itemGUID.isEmpty()) {
            return focusFirstShowingLineItem();
        }
        int nPageRows = m_view.getLineItemsViewer().getMaxDataRows();
        KDSDataItem nextItem = getNextActiveLineItem(orderGUID, itemGUID);
        if (nextItem == null) {
            refreshLineItemsView();
            return "";
        }
        this.getEnv().getStateValues().setFirstShowingOrderGUID(nextItem.getOrderGUID());
        this.getEnv().getStateValues().setFirstItemGuid(nextItem.getGUID());
        this.getEnv().getStateValues().setFocusedOrderGUID(nextItem.getOrderGUID());
        this.getEnv().getStateValues().setFocusedItemGUID(nextItem.getGUID());



        refreshLineItemsView();
        return itemGUID;
    }


    /**
     * 2.0.25
     * @return
     */
    public String focusPrevPageOrder()
    {
        String guid =  this.getEnv().getStateValues().getFirstShowingOrderGUID();
        if (m_orders == null)
            return "";
        int nindex = m_orders.getIndex(guid);
        int nPanelsCount = m_view.getMaxPanelsCount();
        nindex-=nPanelsCount;

        KDSDataOrder order = null;
        int n = this.getEnv().getSettings().getInt(KDSSettings.ID.Item_showing_method);
        KDSSettings.ItemShowingMethod itemShowingMethod = KDSSettings.ItemShowingMethod.values()[n];
        if (itemShowingMethod == KDSSettings.ItemShowingMethod.When_order_is_paid)
            order = m_orders.getPrevPaidOrderFrom(nindex);
        else
            order = m_orders.get(nindex);
        guid = "";
        if (order != null)
        {//return to first
            guid = order.getGUID();
        }
        else
        {

            if (itemShowingMethod == KDSSettings.ItemShowingMethod.When_order_is_paid)
                guid  = m_orders.getFirstPaidOrderGuid();//.getPrevPaidOrderFrom(m_orders.getCount() -1);
            else
                guid = m_orders.getFirstOrderGuid();//.get(m_orders.getCount() -1);
//            //order = m_orders.get(m_orders.getCount() -1 );
//            if (order != null)
//                guid = order.getGUID();
        }
        focusOrder(guid);


        return guid;
    }

    /**
     * 2.0.25
     * @return
     */
    public String focusPrevPageLineItem()
    {
        String orderGUID = this.getEnv().getStateValues().getFirstShowingOrderGUID();
        String itemGUID = this.getEnv().getStateValues().getFirstItemGuid();//

        if (m_orders == null)
            return "";
        if (orderGUID.isEmpty() || itemGUID.isEmpty()) {
            focusFirstShowingLineItem();
            return "";
        }

        int nOrderIndex = m_orders.getIndex(orderGUID);
        int nItemIndex = m_orders.getOrderByGUID(orderGUID).getItems().getItemIndexByGUID(itemGUID);
        int nDistance =  m_view.getLineItemsViewer().getMaxDataRows();

        KDSDataItem prevItem = getPrevActiveLineItem(orderGUID, itemGUID, nDistance);
        if (prevItem == null)
            return focusFirstActiveLineItem();//2.0.27
        this.getEnv().getStateValues().setFocusedOrderGUID(prevItem.getOrderGUID());
        this.getEnv().getStateValues().setFocusedItemGUID(prevItem.getGUID());

        refreshLineItemsView();
        return itemGUID;
    }
    private String focusFirstActiveLineItem()
    {
        KDSDataItem item =  getFirstActiveLineItem();
        if (item == null) return "";
        String orderGUID = item.getOrderGUID();//
        String itemGUID = item.getGUID();
        this.getEnv().getStateValues().setFocusedOrderGUID(orderGUID);
        this.getEnv().getStateValues().setFocusedItemGUID(itemGUID);
        refreshLineItemsView();
        return itemGUID;
    }

    public void onLineItemViewerPrevPageClicked()
    {
        focusPrevPageLineItem();
    }
    public void onLineItemViewerNextPageClicked()
    {
        focusNextPageLineItem();
    }

    Thread m_threadShowOrders = null;
    int m_nRedrawRequestCounter = 0;
    /**
     * Move some timer functions to here.
     * Just release main UI.
     * All feature in this thread are no ui drawing request.
     * And, in checkautobumping function, it use message to refresh UI.
     */
    public void startShowOrdersThread()
    {
        if (m_threadShowOrders == null ||
                !m_threadShowOrders.isAlive())
        {
            m_threadShowOrders = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true)
                    {
                        if (m_threadShowOrders != Thread.currentThread())
                            return;
                        if (m_nRedrawRequestCounter <=0)
                        {
                            try {
                                Thread.sleep(200);
                                continue;
                            } catch (Exception e) {

                            }
                        }
                        try {
                            synchronized (m_orders.m_locker) {
                                showOrdersWithoutUIRefresh(m_orders);
                            }
                            refreshThroughMessage();
                        }
                        catch ( Exception e)
                        {
                            KDSLog.e(TAG,KDSLog._FUNCLINE_(), e);
                            //e.printStackTrace();
                        }
                        m_nRedrawRequestCounter = 0;
                    }
                }
            });
            m_threadShowOrders.setName("ShowOrders");
            m_threadShowOrders.start();
        }
    }

    public boolean showOrdersWithoutUIRefresh(KDSDataOrders orders) {

        if (orders == null) return false;
        //TimeDog t = new TimeDog();
        //m_orders = orders;

        //m_view.clear();
        //synchronized (m_view.m_panelsLocker) {
            m_view.clearPanels();

        //}

        if (orders == null || orders.getCount() <= 0) {
            //this.getView().refresh();
            //m_view.clear();
            return true;
        }
        synchronized (m_view.m_panelsLocker) {
            if (m_view.getOrdersViewMode() == KDSView.OrdersViewMode.Normal) {
                int nBlockRows = m_view.getAverageRowsInBlock();
                int nStartOrderIndex = 0;
                if (getEnv().getStateValues().getFirstShowingOrderGUID().isEmpty())
                    getEnv().getStateValues().setFirstShowingOrderGUID(orders.get(0).getGUID());
                else {
                    nStartOrderIndex = orders.getIndex(getEnv().getStateValues().getFirstShowingOrderGUID());
                }
                //check if the focused order is hidden at left side.
                int nFocusedOrderIndex = orders.getOrderIndexByGUID(getEnv().getStateValues().getFocusedOrderGUID());
                if (nFocusedOrderIndex >= 0) {
                    if (nFocusedOrderIndex < nStartOrderIndex) //adjust it
                    {
                        getEnv().getStateValues().setFirstShowingOrderGUID(getEnv().getStateValues().getFocusedOrderGUID());
                        nStartOrderIndex = nFocusedOrderIndex;
                    }
                }

                //t.debug_print_Duration("showOrders2");
                if (nStartOrderIndex < 0)
                    nStartOrderIndex = 0;
                int ncount = orders.getCount();
                for (int i = nStartOrderIndex; i < ncount; i++) {
                    // t.debug_print_Duration("showOrders1");
                    KDSDataOrder order = orders.get(i);
                    if (order == null) continue;
                    // t.debug_print_Duration("showOrders2");
                    try {
                        if (!showOrder(order, nBlockRows))
                            break;
                    }
                    catch (Exception e)
                    {
                        KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
                    }
                    //t.debug_print_Duration("showOrders3");
                }

                //t.debug_print_Duration("showOrders3");
                //this.getView().refresh();//.invalidate();
                //t.debug_print_Duration("showOrders4");
                return true;
            } else if (m_view.getOrdersViewMode() == KDSView.OrdersViewMode.LineItems) {
                if (getEnv().getStateValues().getFirstShowingOrderGUID().isEmpty())
                    getEnv().getStateValues().setFirstShowingOrderGUID(orders.get(0).getGUID());
                if (getEnv().getStateValues().getFocusedOrderGUID().isEmpty()) {
                    getEnv().getStateValues().setFocusedOrderGUID(orders.get(0).getGUID());

                }
                if (getEnv().getStateValues().getFocusedItemGUID().isEmpty()) {
                    //getEnv().getStateValues().setFocusedItemGUID(orders.get(0).getItems().getItem(0).getGUID());
                    getEnv().getStateValues().setFocusedItemGUID(orders.get(0).getItems().getFirstUnbumpedItemGuid());

                }
                if (getEnv().getStateValues().getFirstItemGuid().isEmpty()) {
                    //getEnv().getStateValues().setFirstItemGuid(orders.get(0).getItems().getItem(0).getGUID());
                    getEnv().getStateValues().setFirstItemGuid(orders.get(0).getItems().getFirstUnbumpedItemGuid());
                }

                showOrdersInLineItemsMode(orders);

                //m_view.getLineItemsViewer().showOrders(orders);
                //this.getView().refresh();//.invalidate();
            }
        }
        return true;
    }

    public void refreshThroughMessage()
    {
        Message m = new Message();
        m.what = 0;
        m_refreshHandler.sendMessage(m);

    }
    Handler m_refreshHandler = new Handler()
    {
        public void handleMessage(Message msg) {
            KDSLayout.this.getView().refresh();
        }
    };


    public boolean isLayoutFull()
    {
        return this.getView().isFull();


    }
}
