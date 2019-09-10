package com.bematechus.kds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSDataCondiments;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;

import java.util.ArrayList;
import java.util.Date;

/**
 * For show Line items view
 */
public class LineItemViewer {

    /**
     * 2.0.27
     */
    public interface LineItemViewerEvents
    {
        public void onLineItemViewerPrevPageClicked();
        public void onLineItemViewerNextPageClicked();
    }
    LineItemViewerEvents m_eventsReveiver = null;

    private float TOP_AREA_PERCENT = (float) 0.40;// (float) 0.25;
    public View m_viewParent = null;

    KDSDataOrders m_orders = new KDSDataOrders();
    LineItemGrid m_gridTop = new LineItemGrid();
    LineItemGrid m_gridBottom = new LineItemGrid();

    boolean m_bShowBottomGrid = false; //it was updated by settings.


    KDSViewFontFace m_fontViewer = new KDSViewFontFace();

    Paint m_paint = new Paint();


    Object m_locker = new Object();

    LineItemsSettings m_colsSettings = new LineItemsSettings();


    LineItemSmart m_smartItemsRows = new LineItemSmart();

    public void setEventReceiver(LineItemViewerEvents receiver)
    {
        m_eventsReveiver = receiver;
    }
    //boolean m_bSmartSortEnabled = true;//show smart mode in line item display

    public KDSViewSettings getEnv()
    {
        return ((KDSView)m_viewParent).getEnv();
    }

    public boolean smartSortEnabled()
    {
        boolean bSmartEnabled = getEnv().getSettings().getBoolean(KDSSettings.ID.Smart_Order_Enabled);
        boolean bLineItemSmart =getEnv().getSettings().getBoolean(KDSSettings.ID.LineItem_sort_smart);
        return (bSmartEnabled && bLineItemSmart);


    }

    public LineItemViewer(View parent)
    {
        m_viewParent = parent;
        init();

    }


    public int getCols()
    {
        String s =  getSettings().getString(KDSSettings.ID.LineItems_cols);
        ArrayList<String> ar = KDSUtil.spliteString(s, ",");
        return ar.size();
    }

    public void buildGrids()
    {
        Rect rect = getBounds();
        m_fontViewer = getSettings().getKDSViewFontFace(KDSSettings.ID.LineItems_font);
        Rect rtTop = new Rect(rect);
        Rect rtBottom = new Rect(rect);
        if (m_bShowBottomGrid)
        {
            rtBottom.top = (int)((float)rect.height() *(TOP_AREA_PERCENT));
            rtTop.bottom = rtBottom.top;
            m_gridTop.setVisible(true);
            m_gridBottom.setVisible(true);
            m_gridTop.setShowTitle(true);
            m_gridBottom.setShowTitle(true);
            m_gridTop.initGrid(m_fontViewer,rtTop, getCols() );
            m_gridBottom.initGrid(m_fontViewer,rtBottom, getCols() );
        }
        else
        {
            m_gridTop.setVisible(true);
            m_gridBottom.setVisible(false);
            m_gridTop.setShowTitle(true);

            m_gridTop.initGrid(m_fontViewer,rtTop, getCols() );

        }


    }


    public void adjustFirstItemForShowingFocus(LineItemGrid grid)
    {


        String fromOrderGuid = getEnv().getStateValues().getFirstShowingOrderGUID();
        String fromItemGuid = getEnv().getStateValues().getFirstItemGuid();

        String focusOrderGuid = getEnv().getStateValues().getFocusedOrderGUID();
        String focusItemGuid = getEnv().getStateValues().getFocusedItemGUID();

        int ndistance = m_orders.getLineItemsDistance(fromOrderGuid, fromItemGuid, focusOrderGuid, focusItemGuid);

        int nShowingRows = grid.getDataActualShowingRows();

        if (ndistance >0) {
            if (Math.abs(ndistance) <= nShowingRows)// grid.getDataRows())
                return;
            else
            {
                adjustFirstItemForMoveDown(grid);
            }
        }
        else if (ndistance<0)
        {
            adjustFirstItemForMoveUp(grid);
        }


    }

    /**
     *
     * @param grid
     */
    private void adjustFirstItemForMoveDown(LineItemGrid grid)
    {
        String fromOrderGuid = getEnv().getStateValues().getFirstShowingOrderGUID();
        String fromItemGuid = getEnv().getStateValues().getFirstItemGuid();

        String focusOrderGuid = getEnv().getStateValues().getFocusedOrderGUID();
        String focusItemGuid = getEnv().getStateValues().getFocusedItemGUID();
        int fromOrderIndex = -1;
        int fromItemIndex = -1;
        //the focused is the last
        int focusedOrderIndex = m_orders.getIndex(focusOrderGuid);
        int focusedItemIndex = m_orders.get(focusedOrderIndex).getItems().getItemIndexByGUID(focusItemGuid);

        int nCurrentShowingRows = grid.getDataActualShowingRows();
        if (focusedItemIndex +1 >= nCurrentShowingRows){// grid.getDataRows()) { //it is in same order
            //fromItemIndex = grid.getDataRows() - focusedItemIndex - 1;
            fromItemIndex =  nCurrentShowingRows - focusedItemIndex - 1;
            fromOrderIndex = focusedOrderIndex;
        }
        else
        {
            int startIndex = focusedOrderIndex;
            int ncounter = 0;
            while (startIndex >=0)
            {
                if (startIndex==focusedOrderIndex)
                    ncounter += m_orders.get(focusedOrderIndex).getPrevActiveItemsCount( focusedItemIndex );
                else
                {
                    int n = m_orders.get(startIndex).getItems().getActiveItemsCount();
                    //if (ncounter + n <grid.getDataRows())
                    if (ncounter + n <nCurrentShowingRows)
                        ncounter += n;
                    else
                    {
                        fromOrderIndex = startIndex;
                        //int activeItemIndex = n - (grid.getDataRows() - ncounter);
                        int activeItemIndex = n - (nCurrentShowingRows - ncounter);
                        fromItemIndex = m_orders.get(startIndex).getSequenceIndexFromActiveIndext(activeItemIndex);
                        break;
                    }
                }
                startIndex --;
            }
        }

        if (fromOrderIndex <0) return;
        if (fromItemIndex<0) return;
        getEnv().getStateValues().setFirstShowingOrderGUID( m_orders.get(fromOrderIndex).getGUID());

        getEnv().getStateValues().setFirstItemGuid( m_orders.get(fromOrderIndex).getItems().getItem(fromItemIndex).getGUID());

    }

    private void adjustFirstItemForMoveUp(LineItemGrid grid)
    {


        String focusOrderGuid = getEnv().getStateValues().getFocusedOrderGUID();
        String focusItemGuid = getEnv().getStateValues().getFocusedItemGUID();
        getEnv().getStateValues().setFirstShowingOrderGUID(focusOrderGuid);
        getEnv().getStateValues().setFirstItemGuid(focusItemGuid);

    }

    /**
     * show order in smart mode.
     * @param orders
     * @param fromOrderGuid
     * @param fromItemGuid
     */
    public void showOrdersSmart(KDSDataOrders orders, String fromOrderGuid, String fromItemGuid)
    {
        int nRows = m_gridTop.getRows();
        m_smartItemsRows.sortOrdersForSmart(getEnv(), m_orders, fromOrderGuid, nRows);
        int nRowCounter = 1;
        boolean bEachLineCondiment = getSettings().getBoolean(KDSSettings.ID.Lineitems_modifier_condiment_each_line);

        for (int i=0; i< m_smartItemsRows.getSortedItems().size(); i++)
        {
            KDSDataOrder order = m_smartItemsRows.getSortedItems().get(i).m_order;
            KDSDataItem item  = m_smartItemsRows.getSortedItems().get(i).m_item;
            int nNeedRows = setGridRowItem(m_gridTop, m_gridTop.getRow(nRowCounter), order, item);
            if ((bEachLineCondiment || isTextWrap()) && nNeedRows >1)
            {//make the same rows
                for (int r = nRowCounter+ 1; r < nRowCounter + nNeedRows; r ++)
                {
                    if (r >= nRows) break;
                    setGridRowItem(m_gridTop, m_gridTop.getRow(r), order, item);

                }
            }
            nRowCounter += nNeedRows;
            if (nRowCounter >= nRows) break;
        }

        for (int i=nRowCounter; i< nRows ; i++)
        {
            m_gridTop.getRow(i).m_bDrawMorIcon = false;
        }

        // draw more icon
        if (smartSortEnabled() && m_smartItemsRows.m_bHiddenExisted) {
            int n = getEnv().getSettings().getInt(KDSSettings.ID.Smart_Order_Showing);
            KDSSettings.SmartOrderShowing showingMethod = KDSSettings.SmartOrderShowing.values()[n];
            if (showingMethod == KDSSettings.SmartOrderShowing.Hide) {
                if (nRowCounter < nRows)
                {
                    LineItemGridRow row = m_gridTop.getRow(nRowCounter);
                    row.m_bDrawMorIcon = true;
                }
            }

        }
        showPrevNextCountInSingleGridMode();
    }

    /**
     * just show order. No any hidden or gray items
     * @param orders
     * @param fromOrderGuid
     * @param fromItemGuid
     */
    public void showOrdersNormal(KDSDataOrders orders, String fromOrderGuid, String fromItemGuid)
    {
        int nMaxRows = m_gridTop.getRows();

        int nOrdersCount = m_orders.getCount();
        int nRowCounter = 1;
        int fromOrderIndex = m_orders.getIndex(fromOrderGuid);
        if (fromOrderIndex <0) return;
        boolean bEachLineCondiment = getSettings().getBoolean(KDSSettings.ID.Lineitems_modifier_condiment_each_line);

        for (int i=fromOrderIndex; i< nOrdersCount; i++)
        {
            KDSDataOrder order =  m_orders.get(i);
            int fromItemIndex  =0;
            if (order.getGUID().equals(fromOrderGuid))
            {
                fromItemIndex = order.getItems().getItemIndexByGUID(fromItemGuid);
                if (fromItemIndex <0)
                    fromItemIndex = 0;
            }
            for (int j=fromItemIndex; j<order.getItems().getCount(); j++)
            {
                if (nRowCounter >= nMaxRows) break;
                KDSDataItem item =order.getItems().getItem(j);

                if (item.isMarked() ||
                        item.getLocalBumped() ||
                        item.isReady())
                    continue;
                int nNeedRows = setGridRowItem(m_gridTop, m_gridTop.getRow(nRowCounter), order, item);
                if ( (bEachLineCondiment || isTextWrap()) && nNeedRows >1)
                {//make the same rows

                    for (int r = nRowCounter+ 1; r < nRowCounter + nNeedRows; r ++)
                    {
                        if (r >= nMaxRows) break;
                        setGridRowItem(m_gridTop, m_gridTop.getRow(r), order, item);

                    }
                }
                nRowCounter += nNeedRows;
            }
            if (nRowCounter >= nMaxRows) break;
        }
        showPrevNextCountInSingleGridMode();
    }

    public void showOrders(KDSDataOrders orders)//, String fromOrderGuid, String fromItemGuid)
    {
        if (!m_gridTop.isInitialed())
            return;
            //buildGrids();
        m_orders = orders;
        adjustFirstItemForShowingFocus(m_gridTop);
        m_gridTop.clear();
        buildGridTitles(m_gridTop.getRow(0));

        String fromOrderGuid = getEnv().getStateValues().getFirstShowingOrderGUID();
        String fromItemGuid = getEnv().getStateValues().getFirstItemGuid();


        if (m_bShowBottomGrid)
        {

        }
        else
        {
            if (smartSortEnabled())
                showOrdersSmart(m_orders, fromOrderGuid, fromItemGuid);
            else
                showOrdersNormal(m_orders, fromOrderGuid, fromItemGuid);

//
//            int nRows = m_gridTop.getRows();
//
//            int nOrdersCount = m_orders.getCount();
//            int nRowCounter = 1;
//            int fromOrderIndex = m_orders.getIndex(fromOrderGuid);
//            if (fromOrderIndex <0) return;
//
//            for (int i=fromOrderIndex; i< nOrdersCount; i++)
//            {
//                KDSDataOrder order =  m_orders.get(i);
//                int fromItemIndex  =0;
//                if (order.getGUID().equals(fromOrderGuid))
//                {
//                    fromItemIndex = order.getItems().getItemIndexByGUID(fromItemGuid);
//                    if (fromItemIndex <0)
//                        fromItemIndex = 0;
//                }
//                for (int j=fromItemIndex; j<order.getItems().getCount(); j++)
//                {
//                    if (nRowCounter >= nRows) break;
//                    if (order.getItems().getItem(j).isMarked() ||
//                            order.getItems().getItem(j).getLocalBumped() ||
//                            order.getItems().getItem(j).isReady())
//                        continue;
//                    int nNeedRows = setGridRowItem(m_gridTop, m_gridTop.getRow(nRowCounter), order, order.getItems().getItem(j));
//                    if (isTextWrap() && nNeedRows >1)
//                    {//make the same rows
//                        for (int r = nRowCounter+ 1; r < nRowCounter + nNeedRows; r ++)
//                        {
//                            if (r >= nRows) break;
//                            setGridRowItem(m_gridTop, m_gridTop.getRow(r), order, order.getItems().getItem(j));
//
//                        }
//                    }
//                    nRowCounter += nNeedRows;
//                }
//                if (nRowCounter >= nRows) break;
//            }
//            showPrevNextCountInSingleGridMode();
        }


    }
    private boolean isTextWrap()
    {
//        return  ( this.getEnv().getSettings().getBoolean(KDSSettings.ID.Text_wrap) ||
//                this.getEnv().getSettings().getBoolean(KDSSettings.ID.Lineitems_modifier_condiment_each_line) );
        return  ( this.getEnv().getSettings().getBoolean(KDSSettings.ID.Text_wrap));

    }
    private boolean isEachLineCondimentModifier()
    {
        return  this.getEnv().getSettings().getBoolean(KDSSettings.ID.Lineitems_modifier_condiment_each_line) ;
    }
    /**
     * just show top grid
     */
    private void showPrevNextCountInSingleGridMode()
    {

        int nPrev = getPrevItemsCount(m_gridTop);
        int nNext = getNextItemsCount(m_gridTop);
        if (nPrev <=0)
            m_gridTop.getCaption().setText(0, "");
        else {
            String strPrev = KDSUtil.convertIntToString(nPrev);
            strPrev = strPrev + " <--";
            m_gridTop.getCaption().setText(0, strPrev);
        }

        if (nNext <=0)
            m_gridTop.getCaption().setText(2, "");
        else {
            String strNext = KDSUtil.convertIntToString(nNext);
            strNext = "--> " + strNext ;
            m_gridTop.getCaption().setText(2, strNext);
        }
    }

    public KDS getKDS()
    {
        return KDSGlobalVariables.getKDS();
    }

    /**
     *
     * @param grid
     * @return
     */
    private int getPrevItemsCount(LineItemGrid grid)
    {
        String orderGuid = grid.getFirstOrderGuid();
        String itemGuid = grid.getFirstItemGuid();
        int nindex =m_orders.getIndex(orderGuid);

//        if (grid.getFirstEmptyRow()>=0) //2.0.27
//            return 0;
        int ncounter = 0;
        for (int i=0; i<= nindex; i++)
        {
            KDSDataOrder order =  m_orders.get(i);
            if (order.getGUID().equals(orderGuid))
            {
                if (order.getItems().getCount() >0)
                {
                    ncounter += order.getItemPrevCount(itemGuid);
                }
                else
                {
                    String orderID = order.getGUID();
                    ncounter += getKDS().getCurrentDB().orderGetItemsPrevCount(orderID, itemGuid);
                }
            }
            else {
                if (order.getItems().getCount() > 0)
                    ncounter += order.getItems().getActiveItemsCount();
                else {
                    String orderID = order.getGUID();
                    ncounter += getKDS().getCurrentDB().orderGetItemsCount(orderID);
                }
            }
        }
        return ncounter;
    }

    /**
     * 2.0.25
     * @return
     */
    public String getLastOrderGuid()
    {
        return m_gridTop.getLastOrderGuid();
    }

    /**
     * 2.0.25
     * @return
     */
    public String getLastItemGuid()
    {
        return m_gridTop.getLastItemGuid();
    }

    public int getMaxDataRows()
    {
        return m_gridTop.getDataRows();
    }

    /**
     *
     * @param grid
     * @return
     */
    private int getNextItemsCount(LineItemGrid grid)
    {
        String orderGuid = grid.getLastOrderGuid();
        String itemGuid = grid.getLastItemGuid();
        int nindex =m_orders.getIndex(orderGuid);
        if (grid.getFirstEmptyRow()>=0)
            return 0;
        int ncounter = 0;
        for (int i=nindex; i< m_orders.getCount(); i++)
        {
            KDSDataOrder order =  m_orders.get(i);
            if (order == null) continue;
            if (order.getGUID().equals(orderGuid))
            {
                if (order.getItems().getCount() >0)
                {
                    ncounter += order.getItemNextCount(itemGuid);
                }
                else
                {
                    String orderID = order.getGUID();
                    ncounter += getKDS().getCurrentDB().orderGetItemsNextCount(orderID, itemGuid);
                }
            }
            else {
                if (order.getItems().getCount() > 0)
                    ncounter += order.getItems().getActiveItemsCount();
                else {
                    String orderID = order.getGUID();
                    ncounter += getKDS().getCurrentDB().orderGetItemsCount(orderID);
                }
            }
        }
        return ncounter;
    }


    private void buildGridTitles(LineItemGridRow gridRow)
    {
        int nCols = m_colsSettings.getCols();
        for (int i=0; i< nCols; i++) {
            gridRow.getCells().get(i).setText(m_colsSettings.getCol(i).m_title);
            gridRow.getCells().get(i).setAlign(Paint.Align.CENTER);
        }


    }

    /**
     *
     * @param grid
     * @param gridRow
     * @param nCell
     *  The cell index
     * @param order
     * @param item
     * @return
     *  need how many rows to show this data.
     */
    private int setCellData(LineItemGrid grid, LineItemGridRow gridRow,int nCell, KDSDataOrder order, KDSDataItem item)
    {
        LineItemGridCell cell = gridRow.getCells().get(nCell);
        //for show the changed icon.
        if (nCell == 0) //first cell
        {
            if (item.isQtyChanged())
            {
                cell.setChangedIcon(getSettings().getItemChangedImage());
            }

        }
        else
        {
            cell.setChangedIcon(null);
        }
        switch (m_colsSettings.getCol(nCell).m_content)
        {


            case Order_ID:
                return cell.setText(order.getOrderName());

            case Order_status: {
                String strPaid = KDSApplication.getContext().getString(R.string.paid);
                String strUnpaid = KDSApplication.getContext().getString(R.string.unpaid);
                return cell.setText((order.getStatus() == KDSDataOrder.ORDER_STATUS_PAID ? strPaid : strUnpaid));
            }


            case Order_type:
                return cell.setText(order.getOrderType());

            case Order_table:
                return cell.setText(order.getToTable());

            case Item_description:
                return cell.setText(item.getDescription());

            case Condiments:
                if (getSettings().getBoolean(KDSSettings.ID.Lineitems_modifier_condiment_each_line))
                    return cell.setText(item.getCondiments().toEachLineString());
                else
                    return cell.setText(item.getCondiments().toString());

            case Quantity:
                if (item.getChangedQty() <=0)
                    return cell.setText(KDSUtil.convertIntToString((int) item.getShowingQty()));
                else
                    return cell.setText(KDSUtil.convertIntToString((int) item.getQty()));

            case Waiting_time: {

                Date dt = new Date();
                dt.setTime(order.getStartTime().getTime());
                if (item.getTimerDelay()>0)
                    dt.setTime(dt.getTime() + item.getTimerDelay() * 1000);

                if (getSettings().getBoolean(KDSSettings.ID.Smart_Order_Enabled) &&
                        getSettings().getBoolean(KDSSettings.ID.Smart_timer_from_item_visible))
                {//show timer from items start to show
                    int nDelaySeconds =  order.prepItemGetStartTime(item);

                    dt = new Date( dt.getTime() + nDelaySeconds *1000);
                    if (order.prepItemIsTimeToCook(item))
                        return cell.setText(KDSDataOrder.makeDurationString(dt));
                    else
                        return cell.setText("");

                }
                else
                    return cell.setText(KDSDataOrder.makeDurationString(dt));


            }

            case Modifiers:
                if (getSettings().getBoolean(KDSSettings.ID.Lineitems_modifier_condiment_each_line))
                    return cell.setText(item.getModifiers().toEachLineString());
                else
                    return cell.setText(item.getModifiers().toString());

            case Modifiers_and_Condiments:
                String modifiers = "";
                String condiments = "";
                boolean bEachLine =getSettings().getBoolean(KDSSettings.ID.Lineitems_modifier_condiment_each_line);
                if (bEachLine) {
                    modifiers = item.getModifiers().toEachLineString();
                    condiments = item.getCondiments().toEachLineString();
                }
                else {
                    modifiers = item.getModifiers().toString();
                    condiments = item.getCondiments().toString();
                }
                String s = modifiers;
                if (!condiments.isEmpty())
                {
                    if (!s.isEmpty()) {
                        if (bEachLine)
                            s += KDSDataCondiments.STRINGS_EACH_LINE_SEPARATOR;// ",";
                        else
                            s += KDSDataCondiments.STRINGS_SEPARATOR;// ",";
                    }
                    s += condiments;
                }
                return cell.setText(s);

        }
        return 1;
    }


    private boolean isCondimentOrModifierCol(int nCell)
    {
        switch (m_colsSettings.getCol(nCell).m_content)
        {
            case Order_ID:
            case Order_status:
            case Order_type:
            case Order_table:
            case Item_description:
            case Quantity:
            case Waiting_time:
                return false;
            case Condiments:
            case Modifiers:
            case Modifiers_and_Condiments:
                return true;

        }
        return false;
    }

    /**
     *
     * @param grid
     * @param gridRow
     * @param order
     * @param item
     * @return
     *  occupied rows
     */
    private int setGridRowItem(LineItemGrid grid, LineItemGridRow gridRow,KDSDataOrder order, KDSDataItem item)
    {
        gridRow.setOrderGuid(order.getGUID());
        gridRow.setItemGuid(item.getGUID());
        boolean bEachLineEnabled = getSettings().getBoolean(KDSSettings.ID.Lineitems_modifier_condiment_each_line);


        int maxRows = 0;

        int nCondimentColLines = 0;//max conidment/modifers need lines
        for (int i=0; i< m_colsSettings.getCols(); i++) {
            int nNeedRows =  setCellData(grid, gridRow, i, order, item);
            if (nNeedRows > maxRows)
                maxRows = nNeedRows;

            if (bEachLineEnabled)
            {
                if (isCondimentOrModifierCol(i))
                {
                    if (nCondimentColLines < nNeedRows)
                        nCondimentColLines = nNeedRows;
                }
            }
        }

        if (item.getGUID().equals(getEnv().getStateValues().getFocusedItemGUID()))
        {
            grid.setFocusedRow(gridRow);
        }
        //only the each line is disabled, and text wrap is disabled, we use single line.
//        if (!getSettings().getBoolean(KDSSettings.ID.Lineitems_modifier_condiment_each_line)) {
//            if (!getSettings().getBoolean(KDSSettings.ID.Text_wrap))
//                maxRows = 1;
//        }

//        if (!isTextWrap() && bEachLineEnabled)
//            maxRows = 1;
        if (!isTextWrap())
        {
            if (bEachLineEnabled) {
                if (nCondimentColLines >0)
                    maxRows = nCondimentColLines;
                else
                    maxRows = 1;
            }
            else
                maxRows = 1;
        }
        return maxRows;
    }
    public  void init()
    {

        m_paint.setAntiAlias(true);



    }


    private void setFocusToRow(LineItemGrid grid, int nRow)
    {
        grid.setFocusedRowIndex(nRow);
        LineItemGridRow r = grid.getRow(nRow);
        getEnv().getStateValues().setFocusedOrderGUID(r.getOrderGuid());
        getEnv().getStateValues().setFocusedItemGUID(r.getItemGuid());
    }
    public void onTouchXY(int x, int y)
    {
        if (m_bShowBottomGrid)
        {

        }
        else
        {
            if (!m_gridTop.getRect().contains(x, y))
                return;

            int nRow = m_gridTop.getTouchedRow(x, y);
            if (nRow <0) {
                if (nRow == -1) //caption
                    onTouchTitle(x, y);
                return;
            }
            if (m_gridTop.isTitleRow(nRow)) {

                return;
            }

            if (!m_gridTop.isEmptyRow(nRow)) {


                setFocusToRow(m_gridTop, nRow);
                //m_gridTop.setFocusedRowIndex(nRow);
            }

            this.refresh();
        }
    }

    private void onTouchTitle(int x, int y)
    {
        if (m_bShowBottomGrid) return;

        if (m_gridTop.getCaption().getCells().size()<3) return;
        if (m_gridTop.getCaption().getCells().get(0).containXY(x,y))
        {
            firePrevPageEvent();
        }
        else if (m_gridTop.getCaption().getCells().get(2).containXY(x,y))
        {
            fireNextPageEvent();
        }
    }

    private void firePrevPageEvent()
    {
        if (m_eventsReveiver != null)
            m_eventsReveiver.onLineItemViewerPrevPageClicked();
    }

    private void fireNextPageEvent()
    {
        if (m_eventsReveiver != null)
            m_eventsReveiver.onLineItemViewerNextPageClicked();
    }

    Handler m_refreshHanlder = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            LineItemViewer.this.m_viewParent.invalidate();
            return true;
        }
    });
    public void refresh()
    {

        //this.invalidate();
        Message m = new Message();
        m.what = 0;
        m_refreshHanlder.sendMessage(m);
    }
    public Rect getBounds()
    {
        Rect rc = new Rect();

        this.m_viewParent.getDrawingRect(rc);
        // rc.inset(2,2);

        return rc;
    }


    public void onDraw(Canvas canvas)
    {
        onDrawGrids(canvas);


    }



    private void onDrawGrids(Canvas canvas)
    {
        if (canvas == null) return;
        Rect rect = this.getBounds();

        Canvas g = canvas;
        if (g == null) return;
        drawBackground(g);
        synchronized (m_locker) {
            drawGrids(g, rect);
        }


    }


    private void drawGrids(Canvas g,Rect rect)
    {
        m_gridTop.onDraw(g);
        if (m_bShowBottomGrid)
        {
            m_gridBottom.onDraw(g);
        }
//        if (smartSortEnabled() && m_smartItemsRows.m_bHiddenExisted) {
//
//            int n = getEnv().getSettings().getInt(KDSSettings.ID.Smart_Order_Showing);
//            KDSSettings.SmartOrderShowing showingMethod = KDSSettings.SmartOrderShowing.values()[n];
//            if (showingMethod == KDSSettings.SmartOrderShowing.Hide) {
//                int nEmptyRow = m_gridTop.getFirstEmptyRow();
//                if (nEmptyRow >=0)
//                {
//                    LineItemGridRow row = m_gridTop.getRow(nEmptyRow);
//                    drawHiddenItemsIcon(g, row);
//                }
//            }
//        }
    }

    public void drawHiddenItemsIcon(Canvas g, LineItemGridRow row)
    {
        Rect rect = row.getRect();
        KDSLayoutCell.drawMoreIndicator(g, rect, getEnv());


    }


    protected void drawBackground(Canvas g)
    {

        int nColor = getSettings().getInt(KDSSettings.ID.LineItems_view_bg);

        g.drawColor(nColor);
    }


    private int getFG(KDSViewFontFace ft, boolean bReverse)
    {
        if (!bReverse)
            return ft.getFG();
        else
            return ft.getBG();
    }


    public void refreshTimer()
    {
        showOrders(m_orders);
        this.refresh();
    }


    public void updateSettings(KDSSettings settings)
    {

        m_gridTop.updateSettings(settings);
        m_gridBottom.updateSettings(settings);



        TOP_AREA_PERCENT= ((float) settings.getInt(KDSSettings.ID.Queue_panel_ratio_percent) /100);
        m_colsSettings.updateSettings(settings);
        buildGrids();
    }

    private KDSSettings getSettings()
    {
        return KDSGlobalVariables.getKDS().getSettings();
    }

    public void onTimer()
    {
        refreshTimer();
        //checkPageCounter();
    }
    public KDSDataOrders getOrders()
    {
        return m_orders;
    }



    /**********************************************************************************************
     *
     */

    class LineItemGrid
    {
        private int ITEM_AVERAGE_HEIGHT = 30;
        private ArrayList<LineItemGridRow> m_arRows = new ArrayList<>();
        private ArrayList<Integer> m_arColSizePercent = new ArrayList<>();
        private int m_nCols = 0;
        private Rect m_rect = new Rect();
        KDSViewFontFace m_fontDefault = new KDSViewFontFace();
        KDSViewFontFace m_fontCaption = new KDSViewFontFace();
        KDSViewFontFace m_fontTitle = new KDSViewFontFace();
        private int m_nRows = 0; //this is internal variable.
        boolean m_bVisible = true;
        boolean m_bShowCaption = true; //show caption
        LineItemGridRow m_Caption = new LineItemGridRow();

        boolean m_bShowTitle = true;

        int m_nFocusedRow = -1;
        int m_nFocusedColor = Color.YELLOW;

        LineItemsColSettings m_colSettings = null;
        Object m_locker = new Object();

        ///////////////////////////////////////
        public LineItemGrid()
        {

        }

        public void clear()
        {
            synchronized (m_locker) {
                for (int i = 0; i < m_arRows.size(); i++) {
                    m_arRows.get(i).clear();
                }
            }

        }
        public void setCaptionFont(KDSViewFontFace ff)
        {
            m_fontCaption = ff;
        }

        public void setShowTitle(boolean bShow)
        {
            m_bShowTitle = bShow;
        }

        public boolean getShowTitle()
        {
            return m_bShowTitle;
        }

        public boolean isTitleRow(int nRow)
        {
            if (nRow == 0 && m_bShowTitle)
                return true;
            return false;
        }

        /**
         *
         * @param x
         * @param y
         * @return
         * -3: Error
         *  -2: out of grid
         *  -1: in caption
         *  >=0: row
         */
        public int getTouchedRow(int x, int y)
        {
            if (!m_rect.contains(x,y))
                return -2;
            if (m_Caption.getRect().contains(x, y)) return -1;
            synchronized (m_locker) {
                for (int i = 0; i < m_arRows.size(); i++) {
                    if (m_arRows.get(i).getRect().contains(x, y))
                        return i;
                }
            }
            return -3;
        }

        public Rect getRect()
        {
            return m_rect;
        }


        public void setFocusedRowIndex(int nIndex)
        {
            //if (getEnv().getSettings().getBoolean(KDSSettings.ID.Text_wrap)) {
            if (isTextWrap() || isEachLineCondimentModifier()){
                if (m_gridTop.isCombinedRow(m_gridTop.getRow(nIndex))) {
                    LineItemGridRow combineToRow = m_gridTop.getCombinedToWhichRow(m_gridTop.getRow(nIndex));
                    nIndex = m_gridTop.getRowIndex(combineToRow);
                }
            }

            m_nFocusedRow = nIndex;
        }
        public int getFocusedRowIndex()
        {
            return m_nFocusedRow;
        }

        public int getRowIndex(LineItemGridRow row)
        {
            synchronized (m_locker) {
                for (int i = 0; i < m_arRows.size(); i++) {
                    if (m_arRows.get(i) == row)
                        return i;
                }
            }
            return -1;
        }
        public void setFocusedRow(LineItemGridRow row)
        {
            int n = getRowIndex(row);
            setFocusedRowIndex(n);

        }


        public int getFirstNoEmptyRow()
        {
            synchronized (m_locker) {
                for (int i = 0; i < m_arRows.size(); i++) {
                    if (m_bShowTitle) {
                        if (i == 0) continue;
                    }
                    if (!m_arRows.get(i).isEmptyRow())
                        return i;
                }
            }
            return -1;
        }

        public int getLastNoEmptyRow()
        {
            synchronized (m_locker) {
                for (int i = m_arRows.size() - 1; i >= 0; i--) {
                    if (m_arRows.get(i).isEmptyRow())
                        return i;
                }
            }
            return -1;
        }

        public int getFirstEmptyRow()
        {
            synchronized (m_locker) {
                for (int i = 1 ;i<  m_arRows.size();i++) {
                    if (m_arRows.get(i).isEmptyRow())
                        return i;
                }
            }
            return -1;
        }

        public void setFocusedColor(int nColor)
        {
            m_nFocusedColor = nColor;
        }
        public LineItemGridRow getCaption()
        {
            return m_Caption;
        }

        private int getFirstDataRowIndex()
        {
            int ntoppest = 0;
            if (m_bShowTitle)
                ntoppest = 1;
            return ntoppest;
        }
        public String getFirstOrderGuid()
        {
            synchronized (m_locker) {
                if (m_arRows.isEmpty()) return "";

                if (!isValidIndex(getFirstDataRowIndex())) return "";

                return m_arRows.get(getFirstDataRowIndex()).getOrderGuid();
            }
        }

        public boolean isValidIndex(int nIndex)
        {
            synchronized (m_locker) {
                if (nIndex < 0 ||
                        nIndex >= m_arRows.size())
                    return false;
                return true;
            }
        }
        public String getFirstItemGuid()
        {
            synchronized (m_locker) {
                if (m_arRows.isEmpty()) return "";
                if (!isValidIndex(getFirstDataRowIndex())) return "";
                return m_arRows.get(getFirstDataRowIndex()).getItemGuid();
            }
        }
        public String getLastOrderGuid()
        {
            synchronized (m_locker) {
                if (m_arRows.isEmpty()) return "";
                int ncount = m_arRows.size();
                for (int i = ncount - 1; i >= 0; i--) {
                    if (m_arRows.get(i).isEmptyRow())
                        continue;
                    return m_arRows.get(i).getOrderGuid();
                }
                return "";
            }
        }

        public String getLastItemGuid()
        {
            synchronized (m_locker) {
                if (m_arRows.isEmpty()) return "";
                int ncount = m_arRows.size();
                for (int i = ncount - 1; i >= 0; i--) {
                    if (m_arRows.get(i).isEmptyRow())
                        continue;
                    return m_arRows.get(i).getItemGuid();
                }
                return "";
            }
        }

        public LineItemGridRow getRow(int nRow)
        {
            synchronized (m_locker) {
                return m_arRows.get(nRow);
            }
        }
        public boolean isInitialed()
        {
            return (!m_rect.isEmpty());
        }
        public void initGrid(KDSViewFontFace ff, Rect rect, int cols)
        {
            if (ff != null)
                setFont(ff);
            this.setRect(rect);
            this.setCols(cols);
            rebuildGrid();
        }



        /**
         * include the title row
         * @return
         */
        public int getRows()
        {
            synchronized (m_locker) {
                return m_arRows.size();
            }
        }

        public void setText(int nRow, int nCol, String strText)
        {
            synchronized (m_locker) {
                m_arRows.get(nRow).getCells().get(nCol).setText(strText);
            }

        }

        public void setVisible(boolean bVisible)
        {
            m_bVisible = bVisible;
        }
        public void setFont(KDSViewFontFace ff)
        {
            this.m_fontDefault = ff;
        }


        public void setRect(Rect rect)
        {
            m_rect = new Rect(rect);

        }

        public void setCols(int nCols)
        {
            m_nCols = nCols;

            m_arColSizePercent.clear();
            int n = 0;
            if (nCols >0)
                n = 100/nCols;

            for (int i=0; i< m_nCols; i++)
            {

                m_arColSizePercent.add( m_colsSettings.getColPercent( i));

            }
        }



        final int CAPTION_HEIGHT = 30;

        private Rect getCaptionRect()
        {
            Rect r = new Rect(m_rect);
            r.bottom = r.top + CAPTION_HEIGHT;
            return r;
        }

        private void build_caption(Rect rtGrid)
        {
            m_Caption.getCells().clear();
            Rect rect = new Rect(rtGrid);

            m_Caption.setRect(getCaptionRect());
            m_Caption.setFont(m_fontCaption);
            int nWidth = rect.width();
            //get the real size
            ArrayList<Integer> arColsSize = new ArrayList<>();

            int nCol0 =  nWidth * 10/100;
            arColsSize.add(nCol0);
            int nCol1 =  nWidth * 80/100;
            arColsSize.add(nCol1);
            int nCol2 =  nWidth * 10/100;
            arColsSize.add(nCol2);

            m_Caption.buildCols(arColsSize);
            m_Caption.setAlign(0, Paint.Align.LEFT);
            m_Caption.setAlign(1, Paint.Align.CENTER);
            m_Caption.setAlign(2, Paint.Align.RIGHT);
            m_Caption.setText(1, getSettings().getString(KDSSettings.ID.LineItems_caption_text));
            m_Caption.getCells().get(0).setBorderSide(true, true, false, true);
            m_Caption.getCells().get(1).setBorderSide(false, true, false, true);
            m_Caption.getCells().get(2).setBorderSide(false, true, true, true);

        }

        private void rebuildGrid()
        {
            if (m_nCols <=0) return;

            Rect rect = new Rect(m_rect);

            if (m_bShowCaption) {
                rect.top += CAPTION_HEIGHT;
                build_caption(rect);
            }
            int nWidth = rect.width()+1;
            m_nRows = calculateRows(rect, ITEM_AVERAGE_HEIGHT);
            //get the real size
            ArrayList<Integer> arColsSize = new ArrayList<>();
            for (int i=0; i< m_arColSizePercent.size(); i++)
            {
                float flt = ((float)nWidth * (float)m_arColSizePercent.get(i)/100f);
                int nColSize = Math.round(flt);
                arColsSize.add(nColSize);
            }
            //adjust the left over
            int nTotalWidth = 0;
            for (int i=0; i< arColsSize.size(); i++)
                nTotalWidth += arColsSize.get(i);
            if (nTotalWidth < nWidth) {
                int nLastCol = arColsSize.get(arColsSize.size() - 1 );
                nLastCol += (nWidth - nTotalWidth);
                arColsSize.set(arColsSize.size() - 1, nLastCol );
            }
            synchronized (m_locker) {
                //calculate each row
                m_arRows.clear();
                for (int i = 0; i < m_nRows; i++) {

                    LineItemGridRow r = new LineItemGridRow();
                    Rect rt = new Rect();
                    rt.left = rect.left;
                    rt.top = rect.top + getRowRelativeY(rect, m_nRows, i);
                    rt.right = rect.right;
                    rt.bottom = rt.top + getRowHeight(rect, m_nRows, i);
                    r.setRect(rt);
                    r.setFont(m_fontDefault);
                    r.buildCols(arColsSize);
                    m_arRows.add(r);
                }
            }

        }

        /**
         *
         * @param row
         * @return
         */
        public Rect getCombinedDrawingRect(LineItemGridRow row)
        {
           if (isCombinedRow(row) ||
                   row.getItemGuid().isEmpty())
               return new Rect(0,0,0,0);
            Rect rt = new Rect(row.getRect());
            int n = getRowIndex(row);
            for (int i=n; i< m_arRows.size(); i++)
            {
                if (getRow(i).getItemGuid().equals(row.getItemGuid()))
                {
                    rt.bottom = getRow(i).getRect().bottom;
                }

            }
            return rt;
        }
        public boolean isCombinedRow(LineItemGridRow row)
        {
            //if (!getSettings().getBoolean(KDSSettings.ID.Text_wrap)) return false;
            if (!isTextWrap() && !isEachLineCondimentModifier()) return false;
            if (row.getItemGuid().isEmpty()) return false;
            int n = getRowIndex(row);
            String guid = row.getItemGuid();
            for (int i=n-1 ;i >=0; i--)
            {
                if (getRow(i).getItemGuid().equals(guid))
                    return true;
            }
            return false;
        }

        public LineItemGridRow getCombinedToWhichRow(LineItemGridRow row)
        {
            if (row.getItemGuid().isEmpty()) return null;
            int n = getRowIndex(row);
            String guid = row.getItemGuid();
            int nLastSame = n;
            for (int i=n-1 ;i >=0; i--)
            {
                if (!getRow(i).getItemGuid().equals(guid))
                    return getRow(nLastSame);
                else
                    nLastSame = i;
            }
            return getRow(nLastSame);
        }
        public void onDraw(Canvas g)
        {
            if (m_bShowCaption)
                m_Caption.onDraw(g);
            boolean bTextWrap = isTextWrap();// getSettings().getBoolean(KDSSettings.ID.Text_wrap);

            synchronized (m_locker) {
                for (int i = 0; i < m_arRows.size(); i++) {
                    LineItemGridRow row = m_arRows.get(i);

                    if (isEmptyRow(row))
                    {
                        if (row.m_bDrawMorIcon) drawHiddenItemsIcon(g, row);
                        break;
                    }
                    if (bTextWrap || isEachLineCondimentModifier()) {
                        if (isCombinedRow(row)) continue;
                        Rect rt = getCombinedDrawingRect(row);
                        row.setDrawingRect(rt);
                    }
                    else
                        row.restoreDrawingRect();
                    m_arRows.get(i).onDraw(g);
                }
            }
            drawFocusBorder(g);
        }

        final int FOCUS_SIZE = 2;
        public void drawFocusBorder(Canvas g)
        {
            if (m_nFocusedRow <0) return;
            if (getEnv().getSettings().getBoolean(KDSSettings.ID.Blink_focus)) {
                if (!KDSGlobalVariables.getBlinkingStep()) return;
            }
            synchronized (m_locker) {
                LineItemGridRow row = m_arRows.get(m_nFocusedRow);

                if (row.isEmptyRow()) return;
                Rect rt = null;
                //if (getSettings().getBoolean(KDSSettings.ID.Text_wrap))
                if (isEachLineCondimentModifier() || isTextWrap())
                    rt = new Rect(row.getDrawingRect());
                else
                    rt = new Rect(row.getRect());
                //rt.inset(2,2);
                rt.left += FOCUS_SIZE + 1;
                rt.top += FOCUS_SIZE + 1;
                rt.right -= FOCUS_SIZE;
                rt.bottom -= FOCUS_SIZE;

                CanvasDC.drawBox(g, rt, m_nFocusedColor, 2);
            }
        }

        public boolean isEmptyRow(LineItemGridRow row)
        {
            return row.isEmptyRow();

        }

        public boolean isEmptyRow(int  nRow)
        {
            return isEmptyRow( getRow(nRow) );
        }


        public int calculateRows(Rect rect, int averagePanelHeight)
        {
            Rect r =  rect;//this.getBounds();
            //m_nRows = r.height() / ITEM_AVERAGE_HEIGHT;
            return r.height() /averagePanelHeight;// ITEM_AVERAGE_HEIGHT;

        }

        public int getColWidth(Rect rect, int nCols)
        {
            Rect r = rect;// this.getBounds();
            return r.width()/nCols;
        }
        public int getRowHeight(Rect rect, int nRows, int nRow)
        {
            Rect r = rect;// this.getBounds();
            //calculateRows();
            int n =( r.height() % nRows);// getRows());// ITEM_AVERAGE_HEIGHT);
            if (nRows == 0) return 0;
            int nAverageH = r.height()/nRows;

            if (n >0)
            {
                if (nRow >= nRows - n )
                    return nAverageH + 1;
                else
                    return nAverageH;
            }
            else
                return nAverageH;

        }

        /**
         * relative to the getBounds
         * @param nRow
         * @return
         */
        private int getRowRelativeY(Rect rect, int nRows, int nRow)
        {
            Rect r =  rect;// this.getBounds();
            //calculateRows();
            int n = (r.height() % nRows);//getRows());//ITEM_AVERAGE_HEIGHT);
            int nAverageH = r.height()/ nRows;//getRows();
            if (n >0)
            {
                if (nRow >= nRows - n )
                    return nAverageH * (nRows - n) +(nAverageH + 1) *( nRow - (nRows - n));
                else
                    return nAverageH*nRow;
            }
            else
                return nAverageH * nRow;
        }

        final int MIN_ROW_HEIGHT = 30;
        public void updateSettings(KDSSettings settings)
        {

            ITEM_AVERAGE_HEIGHT = settings.getInt(KDSSettings.ID.Panels_Row_Height);

            ITEM_AVERAGE_HEIGHT = getBestRowHeight();
            if (ITEM_AVERAGE_HEIGHT<MIN_ROW_HEIGHT)
                ITEM_AVERAGE_HEIGHT = MIN_ROW_HEIGHT;

            m_nFocusedColor = settings.getInt(KDSSettings.ID.Focused_BG);
            m_fontCaption = settings.getKDSViewFontFace(KDSSettings.ID.LineItems_caption_font);
            m_fontTitle =  settings.getKDSViewFontFace(KDSSettings.ID.LineItems_font);
            m_fontViewer = settings.getKDSViewFontFace(KDSSettings.ID.LineItems_font);


        }
        public int getBestRowHeight() {
            //KDSViewFontFace ff = this.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace);
            //Typeface tf = this.getEnv().getSettings().getViewBlockFont();
            KDSViewFontFace ff = m_fontDefault;//this.getEnv().getSettings().getViewBlockFont();//.getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace);

            Paint paint = new Paint();
            paint.setTypeface(ff.getTypeFace());
            // paint.setTypeface(tf);
            int nsize =ITEM_AVERAGE_HEIGHT;// this.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Row_Height);
            //paint.setTextSize(ff.getFontSize());
            paint.setTextSize(nsize);

            //paint.getFontMetrics().a
            return (int) ((paint.getFontMetrics().descent - paint.getFontMetrics().ascent) + 1);

        }

        public int getDataRows()
        {
            if (m_bShowTitle)
                return m_arRows.size()-1;
            else
                return m_arRows.size();
        }

        /**
         * for text wrap
         *
         * @return
         */
        public int getDataActualShowingRows()
        {
            int nStart = 0;
            if (m_bShowTitle) {
                nStart = 1;
                //return m_arRows.size() - 1;
            }
            else
                nStart = 0;
                //return m_arRows.size();
            int nCount = 0;
            String prevRowItemGuid = "";
            for (int i = nStart ; i< m_arRows.size(); i++)
            {
                LineItemGridRow row = m_arRows.get(i);
                if (isEmptyRow(row))
                    return nCount;
                if (prevRowItemGuid.isEmpty()) {
                    prevRowItemGuid = row.getItemGuid();
                }
                else
                {
                    if (prevRowItemGuid.equals(row.getItemGuid()))
                    {
                        continue;
                    }
                    else
                    {
                        prevRowItemGuid = row.getItemGuid();
                    }

                }
                nCount ++;
            }
            return nCount;
        }

    }

    /**********************************************************************************************
     *
     */


    class LineItemGridRow
    {
        private ArrayList<LineItemGridCell> m_arCells = new ArrayList<>();
        private Rect m_rect = new Rect();
        KDSViewFontFace m_fontRow = new KDSViewFontFace();
        String m_strOrderGuid = ""; //it is showing which order
        String m_strItemGuid = "";  //it is showing which item
        private  Rect m_rectDrawing = new Rect(); //for combine rows
        boolean m_bDrawMorIcon = false;
        public void restoreDrawingRect()
        {
            if (m_rectDrawing.equals(m_rect)) return;
            m_rectDrawing = new Rect(m_rect);
            setDrawingRect(m_rectDrawing);
        }
        public void setDrawingRect(Rect rt)
        {
            m_rectDrawing = rt;
            for (int i=0; i< m_arCells.size(); i++)
            {
                m_arCells.get(i).setDrawingRectByRowRect(rt);

            }
        }
        public Rect getDrawingRect()
        {
            return m_rectDrawing;
        }
        public void clear()
        {
            restoreDrawingRect();
            for (int i=0; i< m_arCells.size(); i++)
            {
                m_arCells.get(i).setText("");


            }
            setOrderGuid("");
            setItemGuid("");

        }
        public boolean isEmptyRow()
        {
            for (int i=0; i< m_arCells.size(); i++)
            {
                if (!m_arCells.get(i).getText().isEmpty())
                    return false;
            }
            return true;
        }

        public void setText(int nCol, String strText)
        {
            m_arCells.get(nCol).setText(strText);
        }

        public void setAlign(int nCol, Paint.Align align)
        {
            m_arCells.get(nCol).setAlign(align);
        }

        public String getText(int nCol)
        {
            return m_arCells.get(nCol).getText();
        }

        public void setOrderGuid(String orderGuid)
        {
            m_strOrderGuid = orderGuid;
        }

        public String getOrderGuid()
        {
            return m_strOrderGuid;
        }

        public void setItemGuid(String itemGuid)
        {
            m_strItemGuid = itemGuid;
        }
        public String getItemGuid()
        {
            return m_strItemGuid;
        }

        public void setRect(Rect rt)
        {
            m_rect = rt;
            restoreDrawingRect();
        }
        public void setFont(KDSViewFontFace ff)
        {
            if (m_fontRow == null)
                m_fontRow = new KDSViewFontFace();
            m_fontRow.copyFrom(ff);

        }
        public int getCols()
        {
            return m_arCells.size();
        }


        public void buildCols(ArrayList<Integer> arColSizesValue)
        {
            for (int i=0; i< arColSizesValue.size(); i++)
            {
                LineItemGridCell c = new LineItemGridCell();
                Rect rt = new Rect();
                rt.left = m_rect.left + getRelativeX(arColSizesValue, i);
                rt.right = rt.left + arColSizesValue.get(i);
                rt.top = m_rect.top;
                rt.bottom = m_rect.bottom;
                c.setRect(rt);
                c.setFont(m_fontRow);
                m_arCells.add(c);
            }
        }

        private int getRelativeX(ArrayList<Integer> arColSizesValue, int nCol)
        {
            int n=0;
            for (int i=0; i< nCol; i++)
                n += arColSizesValue.get(i);
            return n;
        }


        public ArrayList<LineItemGridCell> getCells()
        {
            return m_arCells;
        }
        public Rect getRect()
        {
            return m_rect;
        }

        public void onDraw(Canvas g)
        {
            if (smartSortEnabled())
                smartSetColor();
            for (int i=0; i< m_arCells.size(); i++)
                m_arCells.get(i).onDraw(g, isCondimentOrModifierCol(i));
        }


        /**
         * grey un-cook item
         */
        public void smartSetColor()
        {
            if (m_strOrderGuid.isEmpty()) return;
            if (getOrders() == null) return;

            KDSDataOrder order =  getOrders().getOrderByGUID(m_strOrderGuid);
            if (order == null) return;
            KDSDataItem item = order.getItems().getItemByGUID(m_strItemGuid);
            if (item == null) return;
            if (order.prep_get_sorts().is_cooking_time( item.getItemName(), order.getStartTime(), order.getOrderDelay()))
            {
                this.m_fontRow.copyFrom(m_gridTop.m_fontDefault);
            }
            else
            {
//                int defaultFG = m_gridTop.m_fontDefault.getFG();
//                int grayFG = grayDegree(defaultFG);
//                int grayBG = grayDegree(m_gridTop.m_fontDefault.getBG());
                int g = 200;
                int bg = 0xff000000 | (g << 16) | (g << 8) | g;
                this.m_fontRow.setBG( bg );
                g = 224;
                int fg = 0xff000000 | (g << 16) | (g << 8) | g;
                this.m_fontRow.setFG( fg );


            }
        }

        public int grayDegree( int rgb) {
            int a = rgb & 0xff000000;//将最高位（24-31）的信息（alpha通道）存储到a变量
            int r = (rgb >> 16) & 0xff;//取出次高位（16-23）红色分量的信息
            int g = (rgb >> 8) & 0xff;//取出中位（8-15）绿色分量的信息
            int b = rgb & 0xff;//取出低位（0-7）蓝色分量的信息
            rgb = (r * 77 + g * 151 + b * 28) >> 8;    // NTSC luma，算出灰度值
            return rgb;
//
//            if (rgb ==0 )
//                rgb = 128;
//            else if (rgb == 255)
//                rgb = 164;
//            else {
//
////                rgb += ( (255 -rgb)/2);
////                if (rgb >255 ) rgb  =255;
////                if (rgb <128) rgb += 128;
//            }
//
//            return a | (rgb << 16) | (rgb << 8) | rgb;//将灰度值送入各个颜色分量
        }

        public int grayColor( int rgb) {
            int a = rgb & 0xff000000;//将最高位（24-31）的信息（alpha通道）存储到a变量
            int r = (rgb >> 16) & 0xff;//取出次高位（16-23）红色分量的信息
            int g = (rgb >> 8) & 0xff;//取出中位（8-15）绿色分量的信息
            int b = rgb & 0xff;//取出低位（0-7）蓝色分量的信息
            rgb = (r * 77 + g * 151 + b * 28) >> 8;    // NTSC luma，算出灰度值
            if (rgb ==0 )
                rgb = 128;
            else if (rgb == 255)
                rgb = 164;
            else {

//                rgb += ( (255 -rgb)/2);
//                if (rgb >255 ) rgb  =255;
//                if (rgb <128) rgb += 128;
            }

            return a | (rgb << 16) | (rgb << 8) | rgb;//将灰度值送入各个颜色分量
        }
    }
    /////////////////////////////////////////////////
    /**********************************************************************************************
     *
     */

    class LineItemGridCell
    {
        private Rect m_rect = new Rect();
        KDSViewFontFace m_fontCell = new KDSViewFontFace();
        Paint.Align m_align = Paint.Align.LEFT;
        String m_strText = "";
        boolean m_bBorderLeft = true;
        boolean m_bBorderRight = true;
        boolean m_bBorderTop = true;
        boolean m_bBorderBottom = true;

        private Rect m_rectDrawing = new Rect();

        private Drawable m_changedIcon = null;

        public void setChangedIcon(Drawable drawable)
        {
            m_changedIcon = drawable;
        }
        public  Drawable getChangedIcon()
        {
            return m_changedIcon;
        }

        public void restoreDrawingRect()
        {
            m_rectDrawing = m_rect;
        }

        public void setDrawingRect(Rect rt)
        {
            m_rectDrawing = rt;
        }
        public void setDrawingRectByRowRect(Rect rtRow)
        {
            m_rectDrawing = new Rect(m_rect) ;
            m_rectDrawing.bottom = rtRow.bottom;
        }

        public boolean containXY(int x, int y)
        {
            return (m_rect.contains(x,y));
        }
        /**
         *
         * @param strText
         * @return
         *  need how many rows to show me.
         */
        public int setText(String strText)
        {
            m_strText = strText;
            Rect rt = CanvasDC.getWrapStringRect(m_fontCell,m_rect, m_strText, m_align, TEXT_PADDING);
            int n = rt.height();
            int rows = n/m_rect.height();
            if (n % m_rect.height() >0)
                rows ++;
            if (rows <=0) rows = 1;
            return  rows;

        }

        public void setAlign(Paint.Align align)
        {
            m_align = align;
        }

        public String getText()
        {
            return m_strText;
        }
        public void setRect(Rect rt)
        {
            m_rect = rt;
        }
        public void setFont(KDSViewFontFace ff)
        {
            m_fontCell = ff;
        }
        public void onDraw(Canvas g, boolean isCondimentOrModifierCell)
        {
            drawCell(g, isCondimentOrModifierCell);

        }

        public void setBorderSide(boolean bLeft, boolean bTop, boolean bRight, boolean bBottom)
        {
            m_bBorderLeft = bLeft;
            m_bBorderTop = bTop;
            m_bBorderRight = bRight;
            m_bBorderBottom = bBottom;
        }
        private Rect drawCellBorder(Canvas g, Rect rect)
        {

            if (m_bBorderBottom && m_bBorderLeft && m_bBorderRight && m_bBorderTop)
                CanvasDC.drawBox(g, rect,m_fontCell.getFG(), 1 );
            else
            {
                CanvasDC.drawBoxLine(g, rect,m_fontCell.getFG(), 1,m_bBorderLeft,m_bBorderTop,m_bBorderRight,m_bBorderBottom );
            }

            return rect;

        }

        final int TEXT_PADDING = 4;
        /**
         *      ----------------------------------
         *      order | Status string
         *      126   |
         *      ----------------------------------
         *
         *
         * @param g
         */
        public void drawCell(Canvas g, boolean isCondimentOrModifierCell)
        {
            //Rect rtCell = m_rect;
            Rect rtCell = new Rect(m_rectDrawing);
            if (rtCell.isEmpty()) {
                rtCell = m_rect;
                m_rectDrawing = new Rect(m_rect); //reset it.
            }
            if (rtCell.isEmpty()) return;

            g.save();
            rtCell = drawCellBorder(g, rtCell);
            g.clipRect(rtCell);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(m_fontCell.getBG());
            paint.setTypeface(m_fontCell.getTypeFace());
            paint.setTextSize(m_fontCell.getFontSize());

            //m_paintOrderTitle.setColor( getBG(m_ftOrderID, bReverseReadyColorForFlash)); //m_ftOrderID.getBG());
            //m_paintOrderTitle.setColor( getBG(m_ftOrderID, false));//bReverseReadyColorForFlash)); //m_ftOrderID.getBG());
            g.drawRect(rtCell, paint); //draw whole bg

            //draw icon
            if (m_changedIcon != null)
            {
                Drawable drawable = m_changedIcon;
                int nsize = rtCell.height() > 16?16:rtCell.height();
                int nleft = rtCell.left + 2 ;
                int nright = nleft + nsize;
                int ntop = rtCell.top + (rtCell.height() - nsize)/2;
                int nbottom = ntop + nsize;
                drawable.setBounds(nleft, ntop, nright, nbottom);//, rcAbsolute.height());

                drawable.draw(g);
                rtCell.left = nright;//
            }

            paint.setColor(m_fontCell.getFG());
            paint.setTypeface(m_fontCell.getTypeFace());

            drawString(g,paint, rtCell, m_align, m_strText, isCondimentOrModifierCell );
            rtCell = drawCellBorder(g, m_rectDrawing);
            g.restore();
            //     g.clipRect(rect);
        }





        private void drawString(Canvas g,Paint pt, Rect rt, Paint.Align align, String string, boolean isCondimentOrModifierCell )
        {
            Rect rtText = new Rect();
            pt.getTextBounds(string, 0,string.length(), rtText );

            int textHeight = rtText.height ();
            int offset = (rt.height() - textHeight)/2;
            int y = rt.top + offset + textHeight;
            int textWidth = rtText.width();

            int x =0;// rt.left + (rt.width() - textWidth)/2;
            switch (align)
            {

                case CENTER:
                    x =  rt.left + (rt.width() - textWidth)/2;
                    break;
                case LEFT:
                    x = rt.left+1;
                    x += TEXT_PADDING;
                    break;
                case RIGHT:
                    x = rt.right- textWidth-1 - TEXT_PADDING;
                    break;
            }

            // x = adjustToTab(x, 6);
            //if (getSettings().getBoolean(KDSSettings.ID.Text_wrap)) {
            if ((isEachLineCondimentModifier() && isCondimentOrModifierCell) || isTextWrap()){
                Rect rtDraw = new Rect(rt);//m_rectDrawing);
                rtDraw.inset(TEXT_PADDING, 0);
                CanvasDC.drawWrapString(g, m_fontCell, rtDraw, string, align);
            }
            else
                g.drawText(string, x, y, pt);

        }


    }


    /**********************************************************************************************
     *
     */
    class LineItemsColSettings
    {
        public String m_title = "";
        public int m_widthPercent = 10;
        public KDSSettings.LineItemsContent m_content =  KDSSettings.LineItemsContent.Item_description;
        public void updateSettings(KDSSettings settings, KDSSettings.ID colTextID,String strSizePercent, KDSSettings.ID colContentID)
        {
            m_title = settings.getString(colTextID);
            m_widthPercent = KDSUtil.convertStringToInt(strSizePercent, 0);// //settings.getInt(colSizeID);
            int n = KDSSettings.getEnumIndexValues(settings,  KDSSettings.LineItemsContent.class, colContentID);
            m_content =  KDSSettings.LineItemsContent.values()[n];

        }

    }

    /**********************************************************************************************
     *
     */
    class LineItemsSettings
    {
        ArrayList<LineItemsColSettings> m_colsSettings = new ArrayList<>();
        public LineItemsColSettings getCol(int nCol)
        {
            return m_colsSettings.get(nCol);
        }
        private ArrayList<KDSSettings.ID> getColsTextID()
        {
            ArrayList<KDSSettings.ID> ar = new ArrayList<>();
            ar.add(KDSSettings.ID.LineItems_col0_text);
            ar.add(KDSSettings.ID.LineItems_col1_text);
            ar.add(KDSSettings.ID.LineItems_col2_text);
            ar.add(KDSSettings.ID.LineItems_col3_text);
            ar.add(KDSSettings.ID.LineItems_col4_text);
            ar.add(KDSSettings.ID.LineItems_col5_text);
            return ar;

        }



        private ArrayList<KDSSettings.ID> getColsContentID()
        {
            ArrayList<KDSSettings.ID> ar = new ArrayList<>();
            ar.add(KDSSettings.ID.LineItems_col0_content);
            ar.add(KDSSettings.ID.LineItems_col1_content);
            ar.add(KDSSettings.ID.LineItems_col2_content);
            ar.add(KDSSettings.ID.LineItems_col3_content);
            ar.add(KDSSettings.ID.LineItems_col4_content);
            ar.add(KDSSettings.ID.LineItems_col5_content);
            return ar;

        }

        public int getCols()
        {
            return m_colsSettings.size();
        }
        public void updateSettings(KDSSettings settings)
        {

            String s = settings.getString(KDSSettings.ID.LineItems_cols);
            ArrayList<String> arSize = KDSUtil.spliteString(s, ",");
            int nCols = arSize.size();

            ArrayList<KDSSettings.ID> arColsText = getColsTextID();
            //ArrayList<KDSSettings.ID> arColsSize = getColsSizeID();
            ArrayList<KDSSettings.ID> arColsContent = getColsContentID();

            m_colsSettings.clear();
            for (int i=0; i< nCols; i++)
            {
                LineItemsColSettings c = new LineItemsColSettings();
                c.updateSettings(settings, arColsText.get(i),  arSize.get(i),  arColsContent.get(i)) ;
                m_colsSettings.add(c);
            }


        }

        private int getTotal()
        {
            int n = 0;
            for (int i=0; i< m_colsSettings.size(); i++)
                n += m_colsSettings.get(i).m_widthPercent;
            return n;
        }

        public int getColPercent(int nCol)
        {
            int nPercent = m_colsSettings.get(nCol).m_widthPercent;
            int nTotal = getTotal();
            if (nTotal <=0) return nPercent;
            return (int)((float)nPercent*100f/(float)nTotal);

        }

    }
}
