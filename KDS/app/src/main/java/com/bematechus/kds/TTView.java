package com.bematechus.kds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;
import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Administrator on 2017/4/12.
 */
public class TTView  extends View implements TableTracker.TT_Event {

    static final String TAG = "TTView";
    public interface TTView_Event
    {
        void onDoubleClicked(MotionEvent e);
    }

    TTView_Event m_eventReceiver = null;
    private int ITEM_AVERAGE_HEIGHT = 80;
    final int BORDER_GAP = 4;
    private float ORDERID_AREA_PERCENT = (float) 0.40;// (float) 0.25;
    private TTHolderMaps m_holderIDMaps = new TTHolderMaps();

    int COLOR_VIEW_BG = Color.DKGRAY;//.BLACK;

    int m_nCols = 3; //how many columns


    GestureDetector m_gesture = null;//new GestureDetector(this);
    TTViewOrders m_ttOrders = new TTViewOrders();

    String m_strMoreOrders = "";

    KDSViewFontFace m_ftOrderName = new KDSViewFontFace();
    KDSViewFontFace m_ftOrderTimer = new KDSViewFontFace();

    KDSViewFontFace m_ftTableName = new KDSViewFontFace();
    KDSViewFontFace m_ftTrackerID = new KDSViewFontFace();
    KDSViewFontFace m_ftAlertFont = new KDSViewFontFace();
    TableTracker m_tt = new TableTracker();

    int m_colorFocused = Color.YELLOW;

    Paint m_paintOrderName = new Paint();

    Paint m_paintTableName = new Paint();

    int m_nPageCounter = 0;
    TimeDog m_tdPage = new TimeDog();
    int PAGE_TIMEOUT = 5000;

    //boolean m_bExpoBumpEnabled = true;

    public void seteventReceiver(TTView_Event receiver)
    {
        m_eventReceiver = receiver;
    }
    public TTView(Context context)
    {
        super(context);
        init_gesture();
        init();

    }

    public TTView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init_gesture();
        init();
    }

    public TTView(Context context, AttributeSet attrs, int defaultStyle)
    {
        super(context, attrs, defaultStyle);
        init_gesture();
        init();
    }

    public void setCols(int nCols)
    {
        m_nCols = nCols;
    }
    public int getCols()
    {
        return m_nCols;
    }

    public int calculateRows(Rect rect, int averagePanelHeight)
    {
        Rect r =  rect;//this.getBounds();

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
    public Rect getItemRect(Rect rect, int nRows,int nCols, int nRow, int nCol)
    {
        Rect r =  rect;// this.getBounds();
        int colWidth = getColWidth(rect, nCols);
        int x = colWidth * nCol + r.left;
        int y = r.top + getRowRelativeY(rect, nRows,  nRow);
        int w = colWidth;
        int h = getRowHeight(rect, nRows,nRow);

        return new Rect(x, y, x + w, y + h);
    }



    private void init()
    {

        resetAllPaint();
        m_tt.setTTReceiver(this);

    }
    public void onLongTouchDown(MotionEvent e)
    {


    }

    public void onDoubleClick(MotionEvent e)
    {
        KDSDataOrder order = getTouchedOrder(e);
        if (order == null) return;
        if (m_eventReceiver != null)
            m_eventReceiver.onDoubleClicked(e);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onDown(MotionEvent event) {
            // Log.d(DEBUG_TAG,"onDown: " + event.toString());
            TTView.this.onTouchDown(event);
            return true;
        }
        public void onLongPress(MotionEvent e) {
            TTView.this.onLongTouchDown(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            TTView.this.onDoubleClick(e);

            return true;
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event)
    {
        //m_gesture.onTouchEvent(event);
        if(m_gesture.onTouchEvent(event))
        {
            event.setAction(MotionEvent.ACTION_CANCEL);
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {



        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            {
                return onTouchDown(event);
            }

            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {

                return true;
            }

            default:
                // m_gesture.onTouchEvent(event);
                break;
        }
        return false;

    }

    public KDSDataOrder getTouchedOrder(MotionEvent event) {
        int action = event.getAction();
        int x = 0, y=0;

        if (event.getPointerCount() >1)
        {
            int index = action &MotionEvent.ACTION_POINTER_INDEX_MASK;
            x =(int) event.getX(index);
            y = (int)event.getY(index);

        }
        else
        {
            x =(int) event.getX();
            y =(int) event.getY();
        }
        int ncount = m_ttOrders.getCoordinates().size();// m_arPanels.size();
        for (int i =0; i< ncount; i++)
        {
            Rect rt =  m_ttOrders.getCoordinates().get(i);


            if (!rt.contains(x, y))
                continue;
            return m_ttOrders.getOrders().get(i);

        }
        return null;
    }

    public boolean onTouchDown(MotionEvent event)
    {
        KDSDataOrder order = getTouchedOrder(event);
        if (order == null) return false;
        fireTTItemClicked(order);
        return true;


    }



    private void fireTTItemClicked(KDSDataOrder order)
    {
        focusOrder(order.getGUID());
    }

    private void init_gesture()
    {
        m_gesture = new GestureDetector(this.getContext(), new MyGestureListener());


    }
    public void refresh()
    {
        Message m = new Message();
        m.what = 0;
        m_refreshHandler.sendMessage(m);
        //this.invalidate();
    }
    public Rect getBounds()
    {
        Rect rc = new Rect();

        this.getDrawingRect(rc);


        return rc;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int h = measureHeight(heightMeasureSpec);
        int w = measureWidth(widthMeasureSpec);
        setMeasuredDimension(w, h);

    }

    protected int measureHeight(int heightMeasureSpec)
    {
        int specMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int specSize = View.MeasureSpec.getSize(heightMeasureSpec);
        return specSize;


    }

    protected int measureWidth(int widthMeasureSpec)
    {
        int specMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int specSize = View.MeasureSpec.getSize(widthMeasureSpec);
        return specSize;

    }


    Bitmap m_bitmapBuffer = null;
    Canvas m_bufferCanvas = null;
    private Canvas get_double_buffer()
    {
        if (m_bufferCanvas == null)
            m_bufferCanvas = new Canvas();
        Rect rc = this.getBounds();
        if (rc.isEmpty())
            return null;
        if (m_bitmapBuffer == null) {
            m_bitmapBuffer = Bitmap.createBitmap(rc.width(), rc.height(), Bitmap.Config.RGB_565);//.ARGB_8888);
            m_bufferCanvas.setBitmap(m_bitmapBuffer);
        }
        else
        {
            if (m_bitmapBuffer.getWidth() != rc.width() ||
                    m_bitmapBuffer.getHeight() != rc.height() ) {
                m_bitmapBuffer = Bitmap.createBitmap(rc.width(), rc.height(), Bitmap.Config.ARGB_8888);
                m_bufferCanvas.setBitmap(m_bitmapBuffer);
            }

        }
        return m_bufferCanvas;
    }
    private void commit_double_buffer(Canvas canvas)
    {
        canvas.drawBitmap(m_bitmapBuffer, 0, 0, null);
    }


    public Rect getItemRect(Rect rect, int nRows,int nCols, int nOrderIndex)
    {
        int nCol = nOrderIndex / nRows;// getRows();
        int nRow = (nOrderIndex % nRows);//getRows());
        return getItemRect(rect, nRows,nCols, nRow, nCol);
    }



    @Override
    protected void onDraw(Canvas canvas)
    {
        try {

            synchronized (m_ttOrders.getOrders().m_locker) {
                m_ttOrders.moveUnassignedToEnd();
                onDrawTT(canvas);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    int checkStringWidth(KDSViewFontFace ft, String s) {
        if (ft == null) return 0;
        Rect rtText = new Rect();
        Paint pt = new Paint();
        pt.setTypeface(ft.getTypeFace());
        pt.setTextSize(ft.getFontSize());
        pt.getTextBounds(s, 0, s.length() - 1, rtText);
        return rtText.width();
    }
    static final int ERROR_TRACKER_ID_COL_WIDTH = 320;

    private void onDrawTT(Canvas canvas)
    {
        if (canvas == null) return;
        Rect rect = this.getBounds();

        Canvas g = get_double_buffer();
        if (g == null) return;
        drawBackground(g);
        if (m_ttOrders.getOrders() == null) return;


        m_ttOrders.resetCoordinates();

        int unassignedTrackerWidth = ERROR_TRACKER_ID_COL_WIDTH;
        String s = this.getContext().getString(R.string.tt_trackerid_error);
        int nerrorw = checkStringWidth(m_ftOrderName, s);
        if (nerrorw > unassignedTrackerWidth)
            unassignedTrackerWidth = nerrorw + 8;

        Rect rtOrders = new Rect(rect);
        rtOrders.right -= unassignedTrackerWidth ;//- UNASSIGNED_ORDERS_COL_WIDTH;
        //drawAssignedOrders(g,rtOrders);
        drawAssignedUnassignedOrders(g, rtOrders);


        Rect rtErrTrackerID = new Rect(rect);
        rtErrTrackerID.left = rtErrTrackerID.right - unassignedTrackerWidth;
        drawErrTrackerID(g, rtErrTrackerID);

        commit_double_buffer(canvas);
    }


    final int PAGE_NUMBER_ROW_HEIGHT = 30;


    private void drawAssignedUnassignedOrders(Canvas g, Rect rect)
    {
        Rect rtData = new Rect(rect);
        rtData.top += CAPTION_HEIGHT;
        rtData.bottom -= PAGE_NUMBER_ROW_HEIGHT;
        int nRows = calculateRows(rtData,ITEM_AVERAGE_HEIGHT);
        int ncount = m_ttOrders.getOrders().getCount();
        int nPanelIndex = 0;
        int nTotalPanels = nRows * getCols();

        int nPagesCount = getPageCount( nRows, getCols());
        int nPageIndex = getCurrentPageIndex(nPagesCount);
        int nItemsStartIndex = nPageIndex * nTotalPanels;
        int nCurrentItemIndex = -1;

        for (int i=0; i< ncount; i++)
        {

            KDSDataOrder order = m_ttOrders.getOrders().get(i);

            nCurrentItemIndex ++;
            if (nCurrentItemIndex < nItemsStartIndex) continue;
            if (order == null) continue;

            if (nPanelIndex>= nTotalPanels)
            {
                break;
            }
            if (nPanelIndex%nRows ==0)
            {
                drawAssignedCaption(g, rtData, nRows, getCols(),nPanelIndex/nRows);
            }
            drawAssignedItem(g, rtData, nRows, getCols(), order, nPanelIndex);// m_items.get(i),WeekEvent.EVENT_COLOR_BG);
            nPanelIndex++;


        }

        Rect rtPage = new Rect(rect);
        rtPage.top = rtPage.bottom - PAGE_NUMBER_ROW_HEIGHT;
        drawPagesForPanelsMode(g ,rtPage, nPageIndex, nPagesCount);
    }

    final int ERROR_TTID_COLUMNS = 1;

    /**
     * These tracker is not assigned to any order, but they are existed!!
     * History:
     *  20170813  right now the unassigned tracker Id column only has one column, please change it to 2 columns sames as the tracker column with left =Id and right = location.
     * @param g
     * @param rect
     */
    public void drawErrTrackerID(Canvas g, Rect rect)
    {
        Rect rtData = new Rect(rect);
        rtData.top += CAPTION_HEIGHT;
        rtData.bottom -= PAGE_NUMBER_ROW_HEIGHT;
        int nRows = calculateRows(rtData,ITEM_AVERAGE_HEIGHT);
        int ncount = m_ttOrders.getExtraTTIDCount();
        int nPanelIndex = 0;
        int nTotalPanels = nRows * ERROR_TTID_COLUMNS;

        int nPagesCount = getExtraTTIDPageCount( nRows, ERROR_TTID_COLUMNS);
        int nPageIndex = getCurrentPageIndex(nPagesCount);
        int nItemsStartIndex = nPageIndex * nTotalPanels;
        int nCurrentItemIndex = -1;

        for (int i=0; i< ncount; i++)
        {

            TTViewOrders.ExtraTTID ttid = m_ttOrders.getExtraTTID(i);

            nCurrentItemIndex ++;
            if (nCurrentItemIndex < nItemsStartIndex) continue;


            if (nPanelIndex>= nTotalPanels)
            {
                break;
            }
            if (nPanelIndex%nRows ==0)
            {
                drawExtraTTIDCaption(g, rtData, nRows,ERROR_TTID_COLUMNS,nPanelIndex/nRows);
            }
            drawExtraTTID(g, rtData, nRows, ERROR_TTID_COLUMNS, ttid, nPanelIndex);// m_items.get(i),WeekEvent.EVENT_COLOR_BG);
            nPanelIndex++;


        }

        Rect rtPage = new Rect(rect);
        rtPage.top = rtPage.bottom - PAGE_NUMBER_ROW_HEIGHT;
        drawPagesForPanelsMode(g ,rtPage, nPageIndex, nPagesCount);
    }


    private void drawPagesForPanelsMode(Canvas g, Rect rtPage, int nPageIndex, int nPagesCount)
    {
        if (nPagesCount<=1)
        {
            Paint pt = new Paint();
            pt.setAntiAlias(true);
            pt.setColor(COLOR_VIEW_BG);
            g.drawRect(rtPage, pt);
        }
        else {

            Paint pt = new Paint();
            pt.setAntiAlias(true);
            pt.setColor(m_ftOrderName.getFG());
            String strPage = String.format("%d/%d",nPageIndex+1, nPagesCount );
            drawString(g, pt, rtPage, Paint.Align.CENTER, strPage);
        }
    }




    public void showOrders(KDSDataOrders orders)
    {
        m_ttOrders.setOrders( orders);

        if (m_ttOrders.getFocusedOrderGUID().isEmpty()) {
            if (orders.getCount()>0)
                m_ttOrders.setFocusedOrderGuid(orders.getFirstOrderGuid());
        }
        refresh();
    }




    protected void drawBackground(Canvas g)
    {
        //Rect rcClient = getBounds();
        g.drawColor(COLOR_VIEW_BG);
    }

    private void resetAllPaint()
    {

        m_paintOrderName.setAntiAlias(true);
        m_paintTableName.setAntiAlias(true);


    }
    final int FOCUS_BOX_SIZE = 3; //it must < BORDER_GAP
    /**
     *
     * @param g
     * @param rect
     * @param bFocused
     * @return
     *  The data drawing area
     */
    private Rect drawItemBorder(Canvas g, Rect rect, boolean bFocused)
    {


        if (!bFocused)
        {
            rect.inset(BORDER_GAP, BORDER_GAP);

            return rect;
        }
        else
        {
            if (getSettings().getBoolean(KDSSettings.ID.Blink_focus))
            {
                if (!KDSGlobalVariables.getBlinkingStep())
                {
                    rect.inset(BORDER_GAP, BORDER_GAP);

                    return rect;
                }
            }

            m_paintOrderName.setColor(m_colorFocused);
            rect.inset(-1*FOCUS_BOX_SIZE, -1*FOCUS_BOX_SIZE);
            g.drawRect(rect, m_paintOrderName); //draw whole bg
            rect.inset(FOCUS_BOX_SIZE, FOCUS_BOX_SIZE);
            //draw rect for line
            rect.inset(BORDER_GAP - FOCUS_BOX_SIZE, BORDER_GAP - FOCUS_BOX_SIZE);
            m_paintOrderName.setColor(COLOR_VIEW_BG);
            g.drawRect(rect, m_paintOrderName); //draw whole bg
            rect.inset(FOCUS_BOX_SIZE, FOCUS_BOX_SIZE);
            return rect;
        }
    }


    private int getBG(KDSViewFontFace ft, boolean bReverse)
    {
        if (ft == null) return 0;
        if (!bReverse)
            return ft.getBG();
        else
            return ft.getFG();
    }
    private int getFG(KDSViewFontFace ft, boolean bReverse)
    {
        if (ft == null) return 0;
        if (!bReverse)
            return ft.getFG();
        else
            return ft.getBG();
    }
    final int CAPTION_HEIGHT = 30;
    public void drawAssignedCaption(Canvas g,Rect rect, int nRows,int nCols,int nCol)
    {
        int nPanelIndex = nRows * nCol;
        Rect rtCell = getItemRect(rect, nRows,nCols,nPanelIndex);
        rtCell.top-= CAPTION_HEIGHT;
        rtCell.bottom = rtCell.top + CAPTION_HEIGHT;
        rtCell.left += BORDER_GAP;
        rtCell.right -= BORDER_GAP;
        g.save();
        g.clipRect(rtCell);

        // Paint paintBG = new Paint();
        m_paintOrderName.setColor( getBG(m_ftOrderName, false)); //m_ftOrderID.getBG());
        //m_paintOrderName.setColor( getBG(m_ftOrderID, false));//bReverseReadyColorForFlash)); //m_ftOrderID.getBG());
        g.drawRect(rtCell, m_paintOrderName); //draw whole bg

        int nTitleAreaWidth =(int) ((float)rtCell.width() * ORDERID_AREA_PERCENT);

        Rect rtTitle = new Rect(rtCell);
        rtTitle.right = rtTitle.left + nTitleAreaWidth;
        drawTitleDetail(g, rtTitle,m_paintOrderName, m_ftOrderName, this.getContext().getString(R.string.tracker_caption_order_name), true,false);

        Rect rtTableName = new Rect(rtCell);
        rtTableName.left = rtTableName.left + nTitleAreaWidth;
        m_paintTableName.setColor( getBG(m_ftTableName, false));// ft.getBG());
        g.drawRect(rtTableName, m_paintTableName);

        drawWrapString(g, m_ftTableName, false, rtTableName, Paint.Align.CENTER,this.getContext().getString(R.string.tracker_caption_table_name));

        g.restore();
    }

    public void drawExtraTTIDCaption(Canvas g,Rect rect, int nRows,int nCols,int nCol)
    {

        Rect rtCell = new Rect(rect);
        rtCell.top-= CAPTION_HEIGHT;
        rtCell.bottom = rtCell.top + CAPTION_HEIGHT;
        g.save();
        g.clipRect(rtCell);

        m_paintOrderName.setColor( getBG(m_ftOrderName, false)); //m_ftOrderID.getBG());

        g.drawRect(rtCell, m_paintOrderName); //draw whole bg

        Rect rtTitle = new Rect(rtCell);
        rtTitle.right = rtTitle.left + rtCell.width() /2;

        drawTitleDetail(g, rtTitle,m_paintOrderName, m_ftOrderName, this.getContext().getString(R.string.tt_trackerid_error), true,false);

        Rect rtLocation = new Rect(rtCell);
        rtLocation.left = rtLocation.right - rtCell.width()/2;

        drawTitleDetail(g, rtLocation,m_paintOrderName, m_ftTrackerID, getContext().getString(R.string.tt_extra_id_location), true,false);

        g.restore();
    }



    /**
     *      ----------------------------------
     *      order | Status string
     *      126   |
     *      ----------------------------------
     *
     *
     * @param g
     */
    public void drawAssignedItem(Canvas g,Rect rect, int nRows,int nCols,  KDSDataOrder order, int nPanelIndex)
    {
        Rect rtCell = getItemRect(rect, nRows,nCols,nPanelIndex);
        if (rtCell.isEmpty()) return;
        m_ttOrders.setOrderCoordinate(order, rtCell);

        if (order == null) return;

        g.save();

        rtCell = drawItemBorder(g, rtCell, order.getGUID().equals(m_ttOrders.getFocusedOrderGUID()));
        g.clipRect(rtCell);



        boolean bReverseReadyColorForFlash = false;
        if (order.getTrackerID().isEmpty()) {
            if (getSettings().getBoolean(KDSSettings.ID.Tracker_reverse_color_ttid_empty))
                bReverseReadyColorForFlash = true;
        }
            m_paintOrderName.setColor(getBG(m_ftOrderName, bReverseReadyColorForFlash)); //m_ftOrderID.getBG());

            g.drawRect(rtCell, m_paintOrderName); //draw whole bg


        int nTitleAreaWidth =(int) ((float)rtCell.width() * ORDERID_AREA_PERCENT);

        Rect rtTitle = new Rect(rtCell);
        rtTitle.right = rtTitle.left + nTitleAreaWidth;
        if (getSettings().getBoolean(KDSSettings.ID.Tracker_show_timer))
        {
            rtTitle.bottom = rtTitle.top + rtTitle.height()/2;
        }
        drawOrderName(g, rtTitle, order, bReverseReadyColorForFlash);

        if (getSettings().getBoolean(KDSSettings.ID.Tracker_show_timer)) {
            Rect rtTimer = new Rect(rtCell);
            rtTimer.right = rtTimer.left + nTitleAreaWidth;

            rtTimer.top = rtTimer.top + rtTimer.height() / 2;

            drawOrderTimer(g, rtTimer, order,  bReverseReadyColorForFlash);
        }

        Rect rtTableName = new Rect(rtCell);
        rtTableName.left = rtTableName.left + nTitleAreaWidth;
        drawTableName(g, rtTableName, order, bReverseReadyColorForFlash);

        g.restore();

    }


    public void drawExtraTTID(Canvas g, Rect rect, int nRows, int nCols, TTViewOrders.ExtraTTID ttid, int nPanelIndex)
    {
        Rect rtCell = getItemRect(rect, nRows,nCols,nPanelIndex);
        if (rtCell.isEmpty()) return;
        m_ttOrders.setExtraTTIDCoordinate(ttid, rtCell);

        rtCell.inset(BORDER_GAP, BORDER_GAP);
        g.save();

        g.clipRect(rtCell);

        boolean bReverseReadyColorForFlash = false;
        m_paintOrderName.setColor( getBG(m_ftOrderName, bReverseReadyColorForFlash)); //m_ftOrderID.getBG());

        g.drawRect(rtCell, m_paintOrderName); //draw whole bg

        Rect rtTitle = new Rect(rtCell);
        rtTitle.right = rtTitle.right - rtCell.width()/2;
        drawTitleDetail(g, rtTitle, m_paintOrderName, m_ftOrderName, ttid.m_trackerID, true, false);

        Rect rtLocation = new Rect(rtCell);
        rtLocation.left = rtLocation.right - rtCell.width()/2;
        m_paintOrderName.setColor( getBG(m_ftTrackerID, bReverseReadyColorForFlash)); //m_ftOrderID.getBG());
        //m_paintOrderName.setColor( getBG(m_ftOrderID, false));//bReverseReadyColorForFlash)); //m_ftOrderID.getBG());
        g.drawRect(rtLocation, m_paintOrderName); //draw whole bg

        drawTitleDetail(g, rtLocation, m_paintOrderName, m_ftTrackerID, ttid.m_location, true, false);


        g.restore();

    }




    private void drawString(Canvas g,Paint pt, Rect rt, Paint.Align align, String string )
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
                break;
            case RIGHT:
                x = rt.right- textWidth-1;
                break;
        }

        g.drawText(string, x, y, pt);

    }


    /**
     * Draw following details:
     *  Customer name
     *  Custom message
     *  Order Timer,
     *  Order ID
     * @param g
     * @param rt
     * @param order
     */
    private Rect drawOrderName(Canvas g, Rect rt, KDSDataOrder order, boolean bReverseReadyColorForFlash)
    {

        if (order.getTrackerID().isEmpty()) {
            if (getSettings().getBoolean(KDSSettings.ID.Tracker_reverse_color_ttid_empty))
                bReverseReadyColorForFlash = true;
        }

        float alertTimeoutMinutes = getSettings().getFloat(KDSSettings.ID.Tracker_timeout_alert_not_bump);
        //m_paintOrderName.setColor( getBG(m_ftOrderID, false));//bReverseReadyColorForFlash)); //m_ftOrderID.getBG());
        TimeDog dt = new TimeDog(order.getStartTime());
        if (dt.is_timeout((int)( alertTimeoutMinutes * 60 * 1000)))
        {

            rt = drawTitleDetail(g, rt,m_paintOrderName, m_ftAlertFont, order.getOrderName(), true,bReverseReadyColorForFlash);

        }
        else {
            m_paintOrderName.setColor(getBG(m_ftOrderName, bReverseReadyColorForFlash)); //m_ftOrderID.getBG());

            rt = drawTitleDetail(g, rt,m_paintOrderName, m_ftOrderName, order.getOrderName(), true,bReverseReadyColorForFlash);
        }
        return rt;

    }

    private Rect drawOrderTimer(Canvas g, Rect rt, KDSDataOrder order, boolean bReverseReadyColorForFlash)
    {
        float alertTimeoutMinutes = getSettings().getFloat(KDSSettings.ID.Tracker_timeout_alert_not_bump);

        TimeDog dt = new TimeDog(order.getStartTime());
        if (dt.is_timeout((int)( alertTimeoutMinutes * 60 * 1000)))
        {


            rt = drawTitleDetail(g, rt,m_paintOrderName, m_ftAlertFont, order.makeDurationString(), true,bReverseReadyColorForFlash);
        }
        else {
            rt = drawTitleDetail(g, rt,m_paintOrderName, m_ftOrderTimer, order.makeDurationString(), true,bReverseReadyColorForFlash);

        }


        return rt;

    }

    public void refreshTimer()
    {
        this.refresh();
    }

    /**
     *
     * @param rt
     *  the area rect
     * @param ft
     * @param strTitle
     * @return
     *  the left area
     */
    private Rect drawTitleDetail(Canvas g, Rect rt,Paint pt,  KDSViewFontFace ft, String strTitle, boolean bMiddle,boolean bReverseReadyColorForFlash)
    {
        String title  =  strTitle;
        if (ft == null) return rt;
        if (title.isEmpty()) return rt;
        Rect rtText = new Rect();
        pt.setTypeface(ft.getTypeFace());
        pt.setTextSize(ft.getFontSize());
        pt.getTextBounds(title, 0, title.length() - 1, rtText);

        rtText.set(rt.left, rt.top, rt.right, rt.top + rtText.height() +4);//
        if (bMiddle)
            rtText = rt;
        pt.setColor( getBG(ft, bReverseReadyColorForFlash));//
        g.drawRect(rtText, pt);
        pt.setColor(getFG(ft, bReverseReadyColorForFlash));//
        drawString(g, m_paintOrderName, rtText, Paint.Align.CENTER, title);
        rt.top = rtText.bottom;
        return rt;
    }


    private void drawTableName(Canvas g, Rect rt, KDSDataOrder order, boolean bReverseReadyColorForFlash)
    {
        String holderID = order.getTrackerID();//.getToTable();

        String tableName = "";
        if (holderID.isEmpty())
            tableName = "";
        else
            m_holderIDMaps.getTableName(holderID);

        if (getSettings().getBoolean(KDSSettings.ID.Tracker_show_tracker_id) )
        {
            Rect rect = new Rect(rt);
            rect.bottom = rect.bottom - rect.height()/2;
            drawTrackerID(g, rect, order.getTrackerID(), bReverseReadyColorForFlash);
            rect.top = rect.bottom;
            rect.bottom = rt.bottom;
            drawTableName(g, rect, tableName, bReverseReadyColorForFlash);
        }
        else
            drawTableName(g, rt, tableName, bReverseReadyColorForFlash);


    }

    private void drawTableName(Canvas g, Rect rt,String tableName, boolean bReverseReadyColorForFlash)
    {

        KDSViewFontFace ft = m_ftTableName;// getStatusFontFace(status);

        if (ft == null) return;
        m_paintTableName.setColor( getBG(ft, bReverseReadyColorForFlash));// ft.getBG());

        g.drawRect(rt, m_paintTableName);

        drawWrapString(g, ft, bReverseReadyColorForFlash, rt, Paint.Align.CENTER, tableName);


    }

    private void drawTrackerID(Canvas g, Rect rt,String trackerID, boolean bReverseReadyColorForFlash)
    {
        KDSViewFontFace ft = m_ftTrackerID;// getStatusFontFace(status);

        if (ft == null) return;
        m_paintTableName.setColor( getBG(ft, bReverseReadyColorForFlash));// ft.getBG());
        //m_paintTableName.setColor( getBG(ft, false));// ft.getBG());
        g.drawRect(rt, m_paintTableName);

        drawWrapString(g, ft, bReverseReadyColorForFlash, rt, Paint.Align.CENTER, trackerID);


    }


    private void drawWrapString(Canvas g,KDSViewFontFace ft,boolean bReverseColor, Rect rt, Paint.Align align, String string )
    {
        g.save();
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(ft.getFontSize());
        textPaint.setTypeface(ft.getTypeFace());
        textPaint.setColor(getFG(ft, bReverseColor));// ft.getFG());
        textPaint.setAntiAlias(true);

        Layout.Alignment al =  Layout.Alignment.ALIGN_CENTER;
        if (align == Paint.Align.RIGHT)
            al = Layout.Alignment.ALIGN_OPPOSITE;
        else if (align == Paint.Align.LEFT)
            al = Layout.Alignment.ALIGN_NORMAL;
        //StaticLayout sl = new StaticLayout(data,textPaint,getWidth(), Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,true);
        StaticLayout sl = new StaticLayout(string,textPaint,rt.width(), al,1.0f,0.0f,true);

        int x = rt.left;
        int y = rt.top + (rt.height() - sl.getHeight())/2;
        g.translate(x,y);
        sl.draw(g);
        g.restore();
    }


    public void updateSettings(KDSSettings settings)
    {
        int ncols = settings.getInt(KDSSettings.ID.Tracker_viewer_cols);
        this.setCols(ncols);
        ITEM_AVERAGE_HEIGHT = settings.getInt(KDSSettings.ID.Tracker_cell_height);

        ORDERID_AREA_PERCENT= (float) 0.50;//((float) settings.getInt(KDSSettings.ID.Queue_panel_ratio_percent) /100);


        m_ftOrderName = settings.getKDSViewFontFace(KDSSettings.ID.Tracker_order_name_font);
        m_ftOrderTimer= settings.getKDSViewFontFace(KDSSettings.ID.Tracker_order_timer_font);

        m_ftTableName = settings.getKDSViewFontFace(KDSSettings.ID.Tracker_table_name_font);
        m_ftAlertFont = settings.getKDSViewFontFace(KDSSettings.ID.Tracker_alert_font);
        m_ftTrackerID = settings.getKDSViewFontFace(KDSSettings.ID.Tracker_tracker_id_font);



        m_strMoreOrders = settings.getString(KDSSettings.ID.Tracker_more_orders_message);
        COLOR_VIEW_BG = settings.getInt(KDSSettings.ID.Tracker_viewer_bg);


        PAGE_TIMEOUT = settings.getInt(KDSSettings.ID.Tracker_auto_switch_duration) * 1000;
        if (PAGE_TIMEOUT<=0)
            PAGE_TIMEOUT = 5000;

        String strMaps = settings.getString(KDSSettings.ID.Tracker_holder_map);

        m_holderIDMaps = TTHolderMaps.parseString(strMaps);


    }


    private KDSSettings getSettings()
    {
        return KDSGlobalVariables.getKDS().getSettings();
    }

    public void focusOrder(String orderGuid)
    {
        if (m_ttOrders.getOrders() == null) return ;
        m_ttOrders.setFocusedOrderGuid(orderGuid);
        this.refresh();
    }
    public void focusNext()
    {

        String s = m_ttOrders.getNextOrderGUID(m_ttOrders.getFocusedOrderGUID());
        focusOrder(s);
    }
    public void focusPrev()
    {

        String s = m_ttOrders.getPrevOrderGUID(m_ttOrders.getFocusedOrderGUID());
        focusOrder(s);
    }

    public void focusFirst()
    {
        String s = "";
        if (m_ttOrders.getOrders() == null) return;
        if (m_ttOrders.getOrders().getCount()>0)
            s =  m_ttOrders.getOrders().get(0).getGUID();
        focusOrder(s);
    }

    public String getFocusedGuid()
    {
        return m_ttOrders.getFocusedOrderGUID();
    }

    public String getFirstOrderGuid()
    {
        if (m_ttOrders.getOrders() == null) return "";
        if (m_ttOrders.getOrders().getCount()<=0) return "";
        return m_ttOrders.getOrders().get(0).getGUID();
    }

    public String getNextGuid(String guid)
    {
        if (m_ttOrders.getOrders() == null) return "";
        String s = m_ttOrders.getOrders().getNextOrderGUID(guid);
        return s;
    }
    public void onTimer()
    {
        try {


            refreshTimer();
            //checkExpoRemovedOrder(); //move to mainactivity.java
            //checkAllItemsBumpedOrder(); //move to mainactivity.java
            checkPageCounter();
            m_tt.onTimer();
            checkTTOrders();
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
            //e.printStackTrace();
        }
    }

    public void checkExpoRemovedOrder()
    {
        if (!getSettings().getBoolean(KDSSettings.ID.Tracker_enable_auto_bump)) return;
        m_ttOrders.checkExpoRemovedOrder(getSettings().getFloat(KDSSettings.ID.Tracker_timeout_auto_remove_after_expo));
    }

    public void checkAllItemsBumpedOrder()
    {
        if (!getSettings().getBoolean(KDSSettings.ID.Tracker_enable_auto_bump)) return;

        ArrayList<KDSDataOrder> ar = m_ttOrders.checkAllItemsBumpedOrder(getSettings().getFloat(KDSSettings.ID.Tracker_timeout_auto_remove_after_expo));
        for (int i=0; i< ar.size(); i++)
        {
            KDSDataOrder order = ar.get(i);
            KDSGlobalVariables.getKDS().getCurrentDB().orderSetBumped(order.getGUID(), true);
            KDSGlobalVariables.getKDS().getBroadcaster().broadcastTrackerBump(order.getOrderName());
        }
        ar.clear();
    }
    public KDSDataOrders getOrders()
    {
        return m_ttOrders.getOrders();
    }


    private int getPageCount( int nRows, int nCols)
    {
        int ncount = m_ttOrders.getOrders().getCount();
        int nTotalPanels = nRows * nCols;//getCols();
        int n = ncount/nTotalPanels;
        if ( (ncount%nTotalPanels) >0)
            n ++;
        return n;
    }

    private int getExtraTTIDPageCount( int nRows, int nCols)
    {
        int ncount = m_ttOrders.getExtraTTIDCount();
        int nTotalPanels = nRows * nCols;//getCols();
        int n = ncount/nTotalPanels;
        if ( (ncount%nTotalPanels) >0)
            n ++;
        return n;
    }

    private int getCurrentPageIndex(int nPagesCount)
    {
        if (nPagesCount <= 0)
            return 0;
        int n =  (m_nPageCounter % nPagesCount);
        return n;
    }




    private void checkPageCounter()
    {

        if (m_tdPage.is_timeout(PAGE_TIMEOUT)) {
            m_nPageCounter++;
            m_tdPage.reset();
        }
        if (m_nPageCounter >Integer.MAX_VALUE-1000)
            m_nPageCounter = 0;
    }

    public void startTT()
    {
        loadSavedAuthen();
        m_tt.discoverService();

    }
    public void loadSavedAuthen()
    {
        m_tt.loadSavedAuthen();
    }
    public void stopTT()
    {
        m_tt.stopNotification();
    }
    TimeDog m_checkTTOrdersTimeDog = new TimeDog();
    final int CHECK_TT_ORDERS_TIMEOUT = 3000; //old is 5 seconds,
                                                    //20170813, Qiu ask to change this value to 3 seconds
    public void checkTTOrders()
    {
        if (m_checkTTOrdersTimeDog.is_timeout(CHECK_TT_ORDERS_TIMEOUT)) {
            m_checkTTOrdersTimeDog.reset();
            m_tt.retrieveActiveOrders();
        }
    }


    /**
     *
     * @param ip
     * @param port
     */
    public void onTTFindTT(String ip, int port)
    {
        String s = String.format("TT IP=%s, port=%d", ip, port);
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+ s);
        m_tt.retrieveAuthentication();

    }
    public void onTTRetrievedAuthentication(String strAuthen)
    {
        KDSSettings.saveTrackerAuthen(strAuthen);
        KDSLog.d(TAG,KDSLog._FUNCLINE_()+ "TT Received Authen:"+strAuthen);

        String s = getContext().getString(R.string.tt_connect_successful);
        Toast.makeText(KDSApplication.getContext(), s, Toast.LENGTH_LONG).show();

    }
    public void onTTGatewayStatusChanged(boolean bOnline)
    {
        KDSLog.d(TAG,KDSLog._FUNCLINE_()+ "TT Status changed:"+ (bOnline?"online":"offline"));

    }
    public void onTTRetrievedActiveOrders(ArrayList<TTOrder> ar)
    {
        if (ar == null) //clear all, OK
        {
            m_ttOrders.clearOrders();
            return;
        }
        String s = "";
        for (int i=0;i< ar.size() ; i++)
        {
            s += ar.get(i).toString();
        }
        KDSLog.d(TAG , KDSLog._FUNCLINE_()+"  TT --------Retrieve orders-----------");
        KDSLog.d(TAG , KDSLog._FUNCLINE_()+s);

        boolean bAutoAssign = getSettings().getBoolean(KDSSettings.ID.Tracker_enable_auto_assign_id);

        m_ttOrders.updateOrdersHolderID(KDSGlobalVariables.getKDS().getCurrentDB(), ar,bAutoAssign, getSettings().getInt(KDSSettings.ID.Tracker_auto_assign_timeout));
        this.refresh();


    }

    public void askTTPageOrder(String orderGuid)
    {
        KDSDataOrder order =  m_ttOrders.getOrders().getOrderByGUID(orderGuid);
        if (order == null) return;
        String trackerID =  order.getTrackerID();
        if (trackerID.isEmpty()) return;
        m_tt.pageOrder(trackerID);
    }

    /**
     *
     * @param orderGuid
     * @return
     *  orderName
     */
    public String removeOrder(String orderGuid)
    {
        KDSDataOrder order =  m_ttOrders.getOrders().getOrderByGUID(orderGuid);
        if (order == null) return "";

        String trackerID =  order.getTrackerID();

        if (!trackerID.isEmpty())
            m_tt.removeOrder(trackerID);
        return order.getOrderName();

    }

    public void onTTRetrievedActiveOrder(TTOrder order)
    {
        KDSLog.d(TAG,  KDSLog._FUNCLINE_()+"TT Retrieve order:" +order.toString());
    }
    public void onTTNotifyActiveOrders(ArrayList<TTOrder> ar)
    {
        String s = "";
        for (int i=0;i< ar.size() ; i++)
        {
            s += ar.get(i).toString();
        }
        KDSLog.d(TAG , KDSLog._FUNCLINE_()+"  TT --------Notification orders-----------");
        KDSLog.d(TAG , KDSLog._FUNCLINE_()+s);
    }
    public void onTTNotifyOrderChanged(TTOrder order)
    {
        KDSLog.d(TAG,  KDSLog._FUNCLINE_()+"TT changed order:" +order.toString());
    }
    public void onTTError(String strError)
    {
        KDSLog.d(TAG,KDSLog._FUNCLINE_()+ strError);
    }
    public void onTTResponseHttpError(HttpRequest request, int nHttpResponseCode, String errorMsg)
    {
        KDSLog.d(TAG,KDSLog._FUNCLINE_()+ "TT http error " + KDSUtil.convertIntToString(nHttpResponseCode));

        if (request.m_ttCommand == TableTracker.TT_Command.Active_Orders)
        {
            String s = this.getContext().getString(R.string.tt_connect_error);
            Toast.makeText(KDSApplication.getContext(), s, Toast.LENGTH_LONG).show();
        }
        else {


        }
    }
    public void onTTReturnCode(HttpRequest request, TTReturnCode rc)
    {
        KDSLog.d(TAG,KDSLog._FUNCLINE_()+ "TT command:" + request.m_ttCommand.toString() + ", TT message:"+ rc.toString());
        showTTReturnCode(rc);
        if (request.m_ttCommand == TableTracker.TT_Command.Authentication)
        {
            if (rc.m_returnCode ==-1 || rc.m_returnCode == -2 || rc.m_returnCode == -3 ||rc.m_returnCode == -4) {
                String s = this.getContext().getString(R.string.tt_authen_press_button);
                Toast.makeText(KDSApplication.getContext(), s, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onTTShowMessage(String strMsg)
    {
        KDSLog.d(TAG,KDSLog._FUNCLINE_()+ "TT show message:"+strMsg);
    }

    public void onTTOrderPaged(HttpRequest request)
    {
        String s = "TrackerID=" + request.m_params + " paged";
        Toast.makeText(KDSApplication.getContext(), s, Toast.LENGTH_LONG).show();
    }
    public void onTTOrderRemoved(HttpRequest request)
    {
        String s = "TrackerID=" + request.m_params + " removed";
        Toast.makeText(KDSApplication.getContext(), s, Toast.LENGTH_LONG).show();
    }

    private void showTTReturnCode(TTReturnCode rc)
    {
        if (rc == null) return;
        String s = rc.toMessage();
        Toast.makeText(KDSApplication.getContext(), s, Toast.LENGTH_LONG).show();


    }

    public void changeTrackerID(String orderGuid, String trackerID)
    {
        KDSDataOrder order = getOrders().getOrderByGUID(orderGuid);
        order.setTrackerID(trackerID);
        KDSGlobalVariables.getKDS().getCurrentDB().orderSetTrackerID(orderGuid, trackerID);
    }

    public void doRefresh()
    {
        this.invalidate();
    }
    Handler m_refreshHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            m_refreshHandler.removeMessages(0);
            doRefresh();
            return false;
        }
    });

    /**
     * call it from mainactivity thread.
     * @return
     */
    public void checkAutoBumping()
    {
        synchronized (this.m_ttOrders.getOrders().m_locker) {
            checkExpoRemovedOrder();
            checkAllItemsBumpedOrder();
        }
    }

}
