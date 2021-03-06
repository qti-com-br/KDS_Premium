package com.bematechus.kds;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
//import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;
import com.bematechus.kdslib.SettingsBase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;


/**
 *
 * @author David.Wong
  * use the KDSGUIPanel to build order viewer
 * This panels is fixed height, same as old KDS fixed panel viewer.
 * One viewer contains multiple panels, as the order data will been broken 
 */
public class KDSView extends View {

    public static final String TAG = "KDSView";
    public enum SlipDirection
    {
        Left2Right,
        Right2Left,
        Top2Bottom,
        Bottom2Top,
    }
    public enum SlipInBorder
    {
        None,
        Left,
        Right,
        Top,
        Bottom,
    }
    public interface KDSViewEventsInterface {
        public  void onViewPanelClicked(KDSView view, KDSViewPanel panel, KDSViewBlock block, KDSViewBlockCell cell);
        public  void onViewPanelDoubleClicked(KDSView view, KDSViewPanel panel, KDSViewBlock block, KDSViewBlockCell cell);
        public void onSizeChanged();
        public void onViewDrawFinished();
        //public boolean onViewSlipLeftRight(boolean bSlipToLeft, boolean bInBorder);
        public void onViewLongPressed();
        //public boolean onViewSlipUpDown(boolean bSlipToUp, boolean bInBorder);
        //Please notice: e1, e2 maybe null value.
        public boolean onViewSlipping(MotionEvent e1, MotionEvent e2,SlipDirection slipDirection, SlipInBorder slipInBorder);

    }

    public enum OrdersViewMode
    {
        Normal,
        LineItems,
        Summary

    }


    public Object m_panelsLocker = new Object(); //lock panels

    GestureDetector m_gesture = null;//new GestureDetector(this);

    boolean m_bHighLight = false;

    boolean m_bJustRedrawTimer = false;



    //the panels
    Vector<KDSViewPanel> m_arPanels = new Vector<KDSViewPanel>(); //KDSGUIPanel array


    KDSViewSettings m_env = new KDSViewSettings(this);

    KDSViewEventsInterface m_eventsReceiver = null;


    LineItemViewer m_lineItemsViewer = new LineItemViewer(this);

    //summary station feature, kp-21
    KDSViewSumStation m_sumStationViewer = new KDSViewSumStation(this);

    /***************************************************************************************/
    public OrdersViewMode getOrdersViewMode()
    {
        if (isLineItemsDisplayMode())
            return OrdersViewMode.LineItems;
        else if (isSummaryStationMode())
            return OrdersViewMode.Summary;
        else
            return OrdersViewMode.Normal;

    }



    public boolean isLineItemsDisplayMode()
    {
        return this.getEnv().getSettings().getLineItemsViewEnabled();// getBoolean(KDSSettings.ID.LineItems_Enabled);
    }
    public LineItemViewer getLineItemsViewer()
    {
        return m_lineItemsViewer;
    }

    public boolean isSummaryStationMode()
    {
        return (this.getEnv().getSettings().getStationFunc() == SettingsBase.StationFunc.Summary);
    }
    public KDSViewSumStation getSumStnViewer()
    {
        return m_sumStationViewer;
    }
    /*********************************************************************************************/

    public void setLayoutFormat(KDSSettings.LayoutFormat format)
    {
        getSettings().set(KDSSettings.ID.Panels_Layout_Format, format);
    }

    protected KDSSettings getSettings()
    {
        return getEnv().getSettings();
    }

    public  Vector<KDSViewPanel> getPanels()
    {
        return m_arPanels;
    }

    public int getPanelsCount()
    {
        return m_arPanels.size();
    }
    public KDSView(Context context)
    {
        super(context);
        init_gesture();

    }

    public KDSView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init_gesture();

    }

    public KDSView(Context context, AttributeSet attrs, int defaultStyle)
    {
        super(context, attrs, defaultStyle);
        init_gesture();

    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        //private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
           // Log.d(DEBUG_TAG,"onDown: " + event.toString());
            KDSView.this.onTouchDown(event);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            KDSView.this.onDoubleClick(e);

            return true;
        }
//        public boolean onScroll(MotionEvent e1, MotionEvent e2,
//                                float distanceX, float distanceY) {
//            return false;
//            //KDSView.this.onScroll(e1, e2, distanceX, distanceY);
////
////            View v =(View) KDSView.this.getParent();
////            v.setBackground(new BitmapDrawable( m_bitmapBuffer));
////
////            TranslateAnimation slide = new TranslateAnimation(KDSView.this.getX(), KDSView.this.getX()  + KDSView.this.getWidth(), KDSView.this.getY(), KDSView.this.getY()  );
////            slide.setDuration(1000);
////            KDSView.this.startAnimation(slide);
//            //return true;
//        }
        final int SLIP_MIN_DISTANCE = 20;
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            float xDistance = Math.abs(e1.getX() - e2.getX());
            float yDistance = Math.abs(e1.getY() - e2.getY());

            boolean bLeftRight = false;
            if (xDistance > yDistance)
                bLeftRight = true;

            if (bLeftRight) {
                if (xDistance < SLIP_MIN_DISTANCE) return false;
                KDSView.this.onSlipLeftRight(e1, e2, velocityX, velocityY);
                return  true;
            }
            else
            {
                if (yDistance < SLIP_MIN_DISTANCE) return false;
                KDSView.this.onSlipUpDown(e1, e2, velocityX, velocityY);
                return  true;
            }

        }
        public void onLongPress(MotionEvent e) {
            //KDSView.this.showContextMenu();
            if (m_eventsReceiver != null)
                m_eventsReceiver.onViewLongPressed();
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event)
    {
        //m_gesture.onTouchEvent(event);
        if(m_gesture.onTouchEvent(event))
        {
            event.setAction(MotionEvent.ACTION_CANCEL);
        }

        if (m_scaleGesture.onTouchEvent(event))
            event.setAction(MotionEvent.ACTION_CANCEL);

        return super.dispatchTouchEvent(event);
    }
    private void init_gesture()
    {
        m_gesture = new GestureDetector (this.getContext(), new MyGestureListener());

        m_scaleGesture = new ScaleGestureDetector(this.getContext(), new MyScaleGestureListener());


    }
    public boolean onDoubleClick(MotionEvent event)
    {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            {
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
                return doubleClickXY(x, y);
                //Log.d("TAG", "ACTION_DOWN.............");

            }
            //break;

            case MotionEvent.ACTION_MOVE:
                //Log.d("TAG", "ACTION_MOVE.............");
                return true;
            //break;
            case MotionEvent.ACTION_CANCEL:
                //Log.d("TAG", "ACTION_CANCEL.............");
                return true;
            //break;
            case MotionEvent.ACTION_UP:
                return true;
            //Log.d("TAG", "ACTION_UP.............");

            //break;
            default:
                break;
        }
        return false;
    }
    public void setEventsReceiver(KDSViewEventsInterface receiver)
    {
        m_eventsReceiver = receiver;
    }
    public KDSViewEventsInterface getEventReceiver()
    {
        return m_eventsReceiver;
    }

    public void setHightLight(boolean bHighLight)
    {
        m_bHighLight = bHighLight;
    }


    public KDSViewSettings getEnv()
    {
        return m_env;
    }


    /**
     * this function is for order preview in KDSActivityUnbump.java.
     *
     * @param nRows
     * @param nCols
     */
    public void setRowsCols(int nRows, int nCols)
    {

        getSettings().set(KDSSettings.ID.Panels_Blocks_Rows, nRows);
        getSettings().set(KDSSettings.ID.Panels_Blocks_Cols, nCols);//.setBlocksCols(nCols);

    }
    /**
     * Blocks rows, equal panels rows
     * @return
     */
    public int getBlocksRows()
    {
        return getEnv().getSettingsRows();// getSettings().getInt(KDSSettings.ID.Panels_Blocks_Rows);
    }
    public int getBlocksCols()
    {
        return getEnv().getSettingsCols();// getSettings().getInt(KDSSettings.ID.Panels_Blocks_Cols);
    }
    public int getMaxPanelsCount()
    {
        return getBlocksRows() * getBlocksCols();
    }


    public boolean clear()
    {
        clearPanels();
        //m_arPanels.clear();
        this.invalidate();
        return true;
    }

    public void clearPanels()
    {
        synchronized (m_panelsLocker)
        {
            for (int i=0; i< m_arPanels.size(); i++)
            {
                m_arPanels.get(i).clear();
            }
            m_arPanels.clear();
        }

    }

    public Rect getBounds()
    {
        Rect rc = new Rect();

        this.getDrawingRect(rc);


        return rc;
    }


    public void panelAdd(int nCount)
    {
        for (int i=0; i< nCount; i++) {
            if (panelAdd() == null)
                break;
        }
    }

    public KDSViewPanel panelAdd()
    {
        Rect rcLastBlock = getLastPanelLastBlockBounds();

        Point pt = getNextPanelStartPoint(rcLastBlock);
        if (pt == null)
            return null;
        KDSSettings.LayoutFormat layoutFormat = getEnv().getSettingLayoutFormat();
        if (layoutFormat == KDSSettings.LayoutFormat.Horizontal)
        {
            KDSViewPanelHorizontal panel = new KDSViewPanelHorizontal(this);
            panel.setStartPoint(pt);
            panel.setCols(1);
            m_arPanels.add(panel);
            return panel;
        }
        else if (layoutFormat == KDSSettings.LayoutFormat.Vertical)
        {
            KDSViewPanelVertical panel = new KDSViewPanelVertical(this);
            panel.setStartPoint(pt);
            panel.setRows(2);
            m_arPanels.add(panel);
            return panel;

        }
        return null;
    }
    public int panelsGetCount()
    {
        return m_arPanels.size();
    }
//    public boolean panelsClear()
//    {
//        m_arPanels.clear();
//        return true;
//    }
    private Rect getLastPanelLastBlockBounds()
    {
        try {


            int ncount = panelsGetCount();
            if (ncount <= 0)
                return null;
            KDSViewPanel panel = m_arPanels.get(ncount - 1);
            KDSViewBlock block = panel.getLastBlock();
            if (block == null)
                return null;
            return block.getBounds();//.getBounds();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    protected Rect getValidRect()
    {
        Rect rc = new Rect(this.getBounds());
        int nInset = getSettings().getInt(KDSSettings.ID.View_Margin);//.getViewBorderInsets();
        rc.left += nInset;
        rc.top += nInset;
        rc.bottom -= nInset;
        rc.right -= nInset;
        return rc;
    }
    protected  boolean isRectInParentRect(Rect rc)
    {
        Rect rcParent = this.getValidRect();

        return rcParent.contains(rc);
    }

    /**
     * get the height of block bounds
     * @return
     */
    public int getBlockAverageHeight()
    {
        KDSSettings.LayoutFormat layoutFormat = getEnv().getSettingLayoutFormat();
        int nrows = getEnv().getSettingsRows();
        if (layoutFormat == KDSSettings.LayoutFormat.Vertical)
            nrows = 1;//kpp1-324
        return (this.getValidRect().height() ) / nrows;//getEnv().getSettingsRows();// getSettings().getInt(KDSSettings.ID.Panels_Blocks_Rows);// - getEnv().getPanelsRowsGap();
    }

    /**
     * get valid data area height of block
     * @return
     */
    public int getBlockAverageValidHeight()
    {
        int nHeight = this.getBlockAverageHeight();
        nHeight -= (getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset) * 2);
        nHeight -= (getSettings().getInt(KDSSettings.ID.Panels_Block_Inset) * 2);
        int nTitleRows = getSettings().getInt(KDSSettings.ID.Order_Title_Rows);
        if (nTitleRows >0)
            nHeight -= getSettings().getInt(KDSSettings.ID.Panels_Block_Zone_Gap);//.getBlockZoneGap();
        if (getSettings().getInt(KDSSettings.ID.Order_Footer_Rows) >0)
            nHeight -= getSettings().getInt(KDSSettings.ID.Panels_Block_Zone_Gap);
        return nHeight;


    }

    public int getBlockAverageWidth()
    {
        return (this.getValidRect().width() ) / getEnv().getSettingsCols();// getSettings().getInt(KDSSettings.ID.Panels_Blocks_Cols);// - getEnv().getPanelsColsGap();
    }
    protected  boolean isPointInParent(Point pt)
    {
        return this.getBounds().contains(pt.x, pt.y);
    }

    protected Point getViewerPanelStartPoint()
    {
        int x = getSettings().getInt(KDSSettings.ID.View_Margin);
        int y = x;
        return new Point(x, y);
    }
    protected  Point getNextPanelStartPoint(Rect rcLastBlock)
    {
        Point ptNext = new Point(0,0);
        if (rcLastBlock == null)
            return this.getViewerPanelStartPoint();//getEnv().getViewrPanelStartPoint();

        int panelInset = getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);
        int borderInset = getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);
        KDSSettings.LayoutFormat layoutFormat = getEnv().getSettingLayoutFormat();

        if (layoutFormat == KDSSettings.LayoutFormat.Horizontal)
        {
            ptNext.x = rcLastBlock.left + rcLastBlock.width();
            ptNext.y = rcLastBlock.top;
            //check if this new point will go outside of parent.
            int n = ptNext.x + this.getBlockAverageWidth();
            if (n > this.getBounds().right)
            {//outside, go to next row.
                ptNext.y += this.getBlockAverageHeight();
                ptNext.x =  this.getViewerPanelStartPoint().x;//getEnv().getViewrPanelStartPoint().x;
                //
                n = ptNext.y + this.getBlockAverageHeight();
                if (n > this.getBounds().bottom)
                { //can not find new point
                    return null;
                }

            }
            return ptNext;
        }
        else if (layoutFormat == KDSSettings.LayoutFormat.Vertical)
        {
            int rowHeight = 0;

            ptNext.x = rcLastBlock.left;
            ptNext.y = rcLastBlock.bottom;
            if (!isPointInParent(ptNext))
            {//go to next col
                ptNext.x = rcLastBlock.left + this.getBlockAverageWidth();
                ptNext.y = getSettings().getInt(KDSSettings.ID.View_Margin);//.getViewerPanelStartY();
                if (!isPointInParent(ptNext)) return null;

            }
            else
            {
                //check if this point can contain one row.
                int y =  ptNext.y + getBestBlockRowHeight() + panelInset*2 + borderInset*2 ;
                if (y > this.getValidRect().bottom)
                {//go to next col
                    int nBlockWidth = this.getBlockAverageWidth();
                    ptNext.x = rcLastBlock.left + nBlockWidth;
                    ptNext.y = getSettings().getInt(KDSSettings.ID.View_Margin);//getViewerPanelStartY();
                    //2.0.25
                    Point pt = new Point(ptNext);
                    pt.x += nBlockWidth; //check if the right side is in room
                    //if (!isPointInParent(ptNext)) return null;//there are bugs in it.
                    if (!isPointInParent(pt)) return null;
                }

            }
        }
        return ptNext;
    }
    protected  int getBestBlockRowHeight()
    {
        KDSViewBlock block = new KDSViewBlock(this);
        return block.getBestRowHeight();
    }



    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.getEventReceiver() != null)
            this.getEventReceiver().onSizeChanged();
        OrdersViewMode mode =  getOrdersViewMode();
        if (mode == OrdersViewMode.LineItems) {
            m_lineItemsViewer.updateSettings(getSettings());
            m_lineItemsViewer.buildGrids();
        }
        else if (mode == OrdersViewMode.Summary)
        {
            m_sumStationViewer.updateSettings(getSettings());

        }

    }

    public void setJustRedrawTimer()
    {

        m_bJustRedrawTimer = true;

    }

    protected   boolean m_bForceFullDrawing = false;
    public void refresh()
    {

        m_bForceFullDrawing = true;
        m_bJustRedrawTimer = false;
        this.invalidate();
    }

    protected boolean m_bDrawing = false;


    @Override
    protected void onDraw(Canvas canvas) {




        if (m_bDrawing) return;
        m_bDrawing = true;
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        synchronized (m_panelsLocker) {
            try {


//        m_canvasOld = canvas;
//        if (m_bJustRedrawTimer) return;
                //drawMe_DoubleBuffer(canvas);
                OrdersViewMode mode = getOrdersViewMode();
                if (mode == OrdersViewMode.Normal) {
                    if (m_bJustRedrawTimer && (!m_bForceFullDrawing)) {

                        Canvas g = get_double_buffer();
                        try {


                            int ncount = panelsGetCount();
                            for (int i = 0; i < ncount; i++) {
                                if (i >= panelsGetCount()) break;
                                m_arPanels.get(i).onJustDrawCaptionAndFooter(g, i);
                            }
                            redrawAllPanelNumberInReverseSequence(g);
                        } catch (Exception e) {
                            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
                        }
                        commit_double_buffer(canvas);
                        m_bJustRedrawTimer = false;
                    } else {
                        drawMe_DoubleBuffer(canvas);
                        m_bForceFullDrawing = false;
                    }
                } else if (mode == OrdersViewMode.LineItems ){
                    Canvas g = get_double_buffer();
                    m_lineItemsViewer.onDraw(g);
                    //redrawAllPanelNumberInReverseSequence(g); //remove it. kpp1-353
                    commit_double_buffer(canvas);
                }
                else if (mode == OrdersViewMode.Summary)
                {
                    Canvas g = get_double_buffer();
                    m_sumStationViewer.onDraw(g);
                    commit_double_buffer(canvas);
                }
            } catch (Exception err) {
                //KDSLog.e(TAG, err.toString());
                KDSLog.e(TAG, KDSLog._FUNCLINE_(), err);
            }

        }

        m_bDrawing = false;
        //fireViewAfterDrawing();


    }


    Bitmap m_bitmapBuffer = null;
    Canvas m_bufferCanvas = null;
    protected Canvas get_double_buffer()
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
    protected void commit_double_buffer(Canvas canvas)
    {
        canvas.drawBitmap(m_bitmapBuffer, 0, 0, null);
    }
    protected void drawMe_DoubleBuffer(Canvas canvas)
    {

        Canvas g = get_double_buffer();
        try {


            if (g == null) return;


            if (getSettings() == null) return;
            int bg = getSettings().getInt(KDSSettings.ID.Panels_View_BG);
            g.drawColor(bg);
            //kpp1-293
            drawScreenLogo(g);

            int ncount = panelsGetCount();
            for (int i = 0; i < ncount; i++) {
                if (i >= panelsGetCount())
                    break;
                m_arPanels.get(i).onDraw(g, i);
            }

            if (m_bHighLight) {//this view is hightlight in multiple users mode
                // KDSViewFontFace ff =  getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Focused_FontFace);
                //int hightlightBg = ff.getBG();
                int hightlightBg = getSettings().getInt(KDSSettings.ID.Focused_BG);

                Rect rtHightLight = new Rect(0, g.getHeight() - 3, g.getWidth(), g.getHeight());
                CanvasDC.fillRect(g, hightlightBg, rtHightLight);
                //g.drawRect(rtHightLight, );
            }
        }
        catch ( Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
        }
        redrawAllPanelNumberInReverseSequence(g);
        commit_double_buffer(canvas);

    }

    /**
     * get normally the rows in one block
     * @return
     */
    public  int getAverageRowsInBlock()
    {
        int nHeight = this.getBlockAverageValidHeight();

        KDSViewBlock block = new KDSViewBlock(this);
        int nRowH = block.getBestRowHeight();

        return nHeight/nRowH;
    }
    protected boolean touchXY(int x, int y)
    {
        OrdersViewMode mode = getOrdersViewMode();
        if (mode == OrdersViewMode.Normal) {
            firePanelClicked(null, null, null);
            int ncount = m_arPanels.size();
            for (int i = 0; i < ncount; i++) {
                KDSViewPanel panel = m_arPanels.get(i);
                if (!panel.pointInMe(x, y))
                    continue;
                KDSViewBlock block = panel.getClickedBlock(x, y);
                KDSViewBlockCell cell = block.getClickedCell(x, y);
                firePanelClicked(panel, block, cell);
                return false;
            }
            return false;
        }
        else if (mode == OrdersViewMode.LineItems){

            m_lineItemsViewer.onTouchXY(x, y);
        }
        else if (mode == OrdersViewMode.Summary)
        {

        }
        return false;
    }

    public void updateSettings(KDSSettings settings)
    {
        OrdersViewMode mode = getOrdersViewMode();
        if (mode == OrdersViewMode.LineItems)
            m_lineItemsViewer.updateSettings(settings);
        else if (mode == OrdersViewMode.Summary)
        {
            m_sumStationViewer.updateSettings(settings);
        }

        //ScreenLogoDraw.m_logoFilesManager.updateSettings(settings);
    }
    private boolean doubleClickXY(int x, int y)
    {
        OrdersViewMode mode = getOrdersViewMode();
        if (mode == OrdersViewMode.Normal) {
            int ncount = m_arPanels.size();
            for (int i = 0; i < ncount; i++) {
                KDSViewPanel panel = m_arPanels.get(i);
                if (!panel.pointInMe(x, y))
                    continue;
                KDSViewBlock block = panel.getClickedBlock(x, y);
                KDSViewBlockCell cell = block.getClickedCell(x, y);
                firePanelDoubleClicked(panel, block, cell);
                return false;
            }
            return false;
        }
        else if (mode == OrdersViewMode.LineItems)
        {
            m_lineItemsViewer.onDoubleClickXY(x, y);
            return true;
        }
        else if (mode == OrdersViewMode.Summary)
        {

        }
        return false;
    }

    protected void firePanelClicked(KDSViewPanel panel, KDSViewBlock block, KDSViewBlockCell cell)
    {
        if (this.getEventReceiver() != null)
            this.getEventReceiver().onViewPanelClicked(this, panel, block, cell);
    }
    private void firePanelDoubleClicked(KDSViewPanel panel, KDSViewBlock block, KDSViewBlockCell cell)
    {
        if (this.getEventReceiver() != null)
            this.getEventReceiver().onViewPanelDoubleClicked(this, panel, block, cell);
    }

    private void fireViewAfterDrawing()
    {
        if (!m_bMustDrawOnce) return;

        if (this.getEventReceiver() != null)
            this.getEventReceiver().onViewDrawFinished();
        m_bMustDrawOnce = false;
    }

    private boolean m_bMustDrawOnce = false;
    public void setNeedDrawOnce()
    {
        m_bMustDrawOnce = true;
        //this.invalidate();

    }

    public boolean onTouchDown(MotionEvent event)
    {
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
        boolean b =  touchXY(x, y);
        //m_gesture.onTouchEvent(event);
        return b;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {



        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            {
                return onTouchDown(event);


            }
            //break;

            case MotionEvent.ACTION_MOVE:
                //Log.d("TAG", "ACTION_MOVE.............");

                //break;
            case MotionEvent.ACTION_CANCEL:
                //Log.d("TAG", "ACTION_CANCEL.............");

                //break;
            case MotionEvent.ACTION_UP: {
               // m_gesture.onTouchEvent(event);
                return true;
            }
                //Log.d("TAG", "ACTION_UP.............");

                //break;
            default:
               // m_gesture.onTouchEvent(event);
                break;
        }
        return false;

    }

    public KDSViewPanelBase getLastPanel()
    {
        if (m_arPanels.size() <=0)
            return null;
        return m_arPanels.get(m_arPanels.size() - 1);

    }


//    public Bitmap decodeBitmapFromRes(Context context, int resourseId) {
//        BitmapFactory.Options opt = new BitmapFactory.Options();
//        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        opt.inPurgeable = true;
//        opt.inInputShareable = true;
//
//        InputStream is = context.getResources().openRawResource(resourseId);
//        return BitmapFactory.decodeStream(is, null, opt);
//    }


//    private int measureWidth(int measureSpec) {
//        int result = 0;
//        int specMode = MeasureSpec.getMode(measureSpec);
//        int specSize = MeasureSpec.getSize(measureSpec);
//
//        if (specMode == MeasureSpec.EXACTLY) {
//            // We were told how big to be
//            result = specSize;
//        } else {
//            result = 640;
//
//        }
//
//        return result;
//    }
//
//    private int measureHeight(int measureSpec) {
//        int result = 0;
//        int specMode = MeasureSpec.getMode(measureSpec);
//        int specSize = MeasureSpec.getSize(measureSpec);
//
//       // mAscent = (int) mPaint.ascent();
//        if (specMode == MeasureSpec.EXACTLY) {
//            // We were told how big to be
//            result = specSize;
//        } else {
//            result = 480;
//
//        }
//        return result;
//    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
      //  showInfo(KDSUtil.convertIntToString(keyCode));
        return super.onKeyDown(keyCode, event);
    }

    public KDSViewPanel getBlockPanel(KDSViewBlock block)
    {
        for (int i=0; i<m_arPanels.size(); i++)
        {
            if (m_arPanels.get(i).containsBlock(block))
                return m_arPanels.get(i);
        }
        return null;
    }

    /**
     * check if whole screen full blocks.
     * @return
     */
    public boolean isFull()
    {
        Rect rtLast = getLastPanelLastBlockBounds();
        Point pt = getNextPanelStartPoint(rtLast);
        return (pt == null);
	}
    static public int getOrderCaptionBackgroundColor(KDSDataOrder order, KDSViewSettings env, KDSViewFontFace font)
    {
        //get the background color according to the time.
        int nBG = env.getSettings().getOrderTimeColorAccordingWaitingTime(order.getStartToCookTime(), font.getBG());
        //exp alert
        if (env.getSettings().isExpeditorStation() ||
                env.getSettings().isRunnerStation())
        { //the exp aler color
            if (env.getSettings().getBoolean(KDSSettings.ID.Exp_Alert_Enabled)) { //20190723, there is bug here, I add this "if" condition.
                if (order.isItemsAllBumpedInExp()) {
                    nBG = env.getSettings().getExpAlertTitleBgColor(true, font.getBG());
                }
            }
        }

        if (order.isDimColor())
            nBG = KDSConst.DIM_BG;
        return nBG;
    }

    /**
     * check if this order is visible in view
     * @param orderGuid
     * @return
     */
    protected boolean isOrderVisible(String orderGuid)
    {
        int ncount = this.getPanels().size();
        for (int i=0; i< ncount; i++)
        {
            KDSViewPanel panel = this.getPanels().get(i);
            if (panel == null)
                continue;
            KDSViewBlock block = panel.getFirstBlock();
            if (block == null)
                continue;
            String guid = ((KDSLayoutOrder)block.getCells().get(0).getData()).getGUID();
            if (guid.equals(orderGuid))
                return true;
        }
        return false;
    }

    private void redrawAllPanelNumberInReverseSequence(Canvas g)
    {
        int ncount = panelsGetCount();
        for (int i = ncount-1; i >=0; i--) {
            m_arPanels.get(i).drawPanelNumber(g, i);
        }
    }

    //boolean m_bSliping = false;
    final int SLIPPING_DURATION = 800;
    final int SLIPPING_GAP = 10;
    final int SLIPPING_BORDER_SIZE = 50;
    private void onSlipLeftRight(MotionEvent e1, MotionEvent e2 , float velocityX, float velocityY)
    {
        if (getOrdersViewMode() != OrdersViewMode.Normal)
            return;
        //if (m_bSliping) return;
        View viewParent =(View) KDSView.this.getParent();
        Bitmap bmpBG =  m_bitmapBuffer.copy(m_bitmapBuffer.getConfig(),m_bitmapBuffer.isMutable() );
        viewParent.setBackground(new BitmapDrawable( bmpBG));

        SlipDirection slipDirect = SlipDirection.Right2Left;
        float n = e2.getX() - e1.getX();
        if (n == 0) return;
        if (n >0)
            slipDirect = SlipDirection.Left2Right;
            //bSlipToLeft = false;

//        Rect rt = new Rect( this.getBounds());
//        rt.inset(SLIPPING_BORDER_SIZE, SLIPPING_BORDER_SIZE);
//        boolean bSlipInBorder = false;
        SlipInBorder slipInBorder = checkPointBorderPosition(e1);// SlipInBorder.None;
        if (slipInBorder == SlipInBorder.None)
            slipInBorder = checkPointBorderPosition(e2);
//        if (!rt.contains((int)e1.getX(), (int)e1.getY()) ||
//                !rt.contains((int)e2.getX(), (int)e2.getY()))
//        {
//            if (e1.getY() > SLIPPING_BORDER_SIZE && e1.getY() < (rt.width()-SLIPPING_BORDER_SIZE) &&
//                    e2.getY() > SLIPPING_BORDER_SIZE && e2.getY() < (rt.width()-SLIPPING_BORDER_SIZE))
//                bSlipInBorder = true;
//        }



        boolean bSlipWorked = false;
        if (m_eventsReceiver != null)
            bSlipWorked = m_eventsReceiver.onViewSlipping(e1, e2, slipDirect, slipInBorder);
            //bSlipWorked = m_eventsReceiver.onViewSlipLeftRight(bSlipToLeft, bSlipInBorder);
        if (slipInBorder!= SlipInBorder.None)
        {
            if (bSlipWorked) return ; //it slip the summary.
        }

        float fromX = 0;
        float toX = 0;
        if (slipDirect == SlipDirection.Right2Left) {
            if (bSlipWorked) {
                fromX = this.getX() + this.getWidth();
                toX = this.getX();
            }
            else
            {
                fromX = this.getX();
                toX = this.getX() - SLIPPING_GAP;
                viewParent.setBackground(null);
            }

        }
        else
        { //to right
            if (bSlipWorked) {
                fromX = this.getX() - this.getWidth();
                toX = this.getX();
            }
            else
            {
                fromX = this.getX();
                toX = this.getX() + SLIPPING_GAP;
                viewParent.setBackground(null);
            }
        }
        playAnimation(bSlipWorked,fromX, toX);
    }

    private void playAnimation(boolean bSlippingWorked, float fromX, float toX)
    {
        TranslateAnimation slide = new TranslateAnimation(fromX, toX ,
                this.getY(), this.getY());
        if (bSlippingWorked)
            slide.setDuration(SLIPPING_DURATION);
        else
            slide.setDuration(SLIPPING_DURATION/2);
//        if (bSlippingWorked) {
//            AnimationSet as = new AnimationSet(false);
//            as.addAnimation(slide);
//
//            // ?????????????????????????????????????????????????????????????????????????????????????????????????????????
//            AlphaAnimation alphaAni = new AlphaAnimation(0.5f, 1);
//            alphaAni.setDuration(SLIPPING_DURATION);
//
//            as.addAnimation(alphaAni);
//            this.startAnimation(as);
//        }
//        else
        {
            this.startAnimation(slide);
        }
    }

    private void onSlipUpDown(MotionEvent e1, MotionEvent e2 , float velocityX, float velocityY)
    {
        //boolean bSlipToUP = true;
        SlipDirection slipDirection = SlipDirection.Bottom2Top;
        float n = e2.getY() - e1.getY();
        if (n == 0) return;
        if (n >0)
            slipDirection = SlipDirection.Top2Bottom;
            //bSlipToUP = false;
        SlipInBorder slipInBorder = checkPointBorderPosition(e1);// SlipInBorder.None;
        if (slipInBorder == SlipInBorder.None)
            slipInBorder = checkPointBorderPosition(e2);

        if (m_eventsReceiver != null)
            m_eventsReceiver.onViewSlipping(e1, e2, slipDirection, slipInBorder);
    }

    private SlipInBorder checkPointBorderPosition(MotionEvent e)
    {
        Rect rt = new Rect( this.getBounds());
        rt.inset(SLIPPING_BORDER_SIZE, SLIPPING_BORDER_SIZE);
        if (rt.contains((int)e.getX(), (int)e.getY()))
            return SlipInBorder.None;

        rt = new Rect( this.getBounds());
        int x = (int)e.getX();
        int y = (int)e.getY();
        if (x < SLIPPING_BORDER_SIZE)
            return SlipInBorder.Left;
        if (x > rt.width() - SLIPPING_BORDER_SIZE)
            return SlipInBorder.Right;
        if (y < SLIPPING_BORDER_SIZE)
            return SlipInBorder.Top;
        if ( y>rt.height() - SLIPPING_BORDER_SIZE)
            return SlipInBorder.Bottom;

//        rt.right = rt.left + SLIPPING_BORDER_SIZE;
//        if (rt.contains((int)e.getX(), (int)e.getY()))
//            return SlipInBorder.Left;
//
//        rt = new Rect( this.getBounds());
//        rt.left = rt.right - SLIPPING_BORDER_SIZE;
//        if (rt.contains((int)e.getX(), (int)e.getY()))
//            return SlipInBorder.Right;
//
//        rt = new Rect( this.getBounds());
//        rt.bottom = rt.top + SLIPPING_BORDER_SIZE;
//        if (rt.contains((int)e.getX(), (int)e.getY()))
//            return SlipInBorder.Top;
//
//        rt = new Rect( this.getBounds());
//        rt.top = rt.bottom - SLIPPING_BORDER_SIZE;
//        if (rt.contains((int)e.getX(), (int)e.getY()))
//            return SlipInBorder.Bottom;
        return SlipInBorder.None;
    }

    public KDSViewPanel findTouchPanel(int x, int y)
    {
        int ncount = m_arPanels.size();
        for (int i = 0; i < ncount; i++) {
            KDSViewPanel panel = m_arPanels.get(i);
            if (panel.pointInMe(x, y))
                return panel;
        }
        return null;

    }

    ScaleGestureDetector m_scaleGesture = null;//new GestureDetector(this);
    class MyScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector)
        {

            //???????????????true????????????onScale()????????????
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector)
        {

            Log.i(TAG, "scale end");

            if (m_eventsReceiver != null)
                m_eventsReceiver.onViewSlipping(null, null, SlipDirection.Bottom2Top, SlipInBorder.None);
        }
    }

    /**
     * kpp1-293
     * @return
     */
    private boolean isScreenEmpty()
    {
        return  (this.getPanels().size() == 0 );
    }

    /**
     * kpp1-293
     * @param canvas
     */
    protected void drawScreenLogo(Canvas canvas)
    {
        ScreenLogoDraw.drawScreenLogo(this, this.getBounds(), canvas, getSettings(), isScreenEmpty(), 0);
    }

    /**
     * kp-2. Vertical Expand-Going to previous page takes you to the first page.
     * @return
     */
    public int getBlockBorderOccupyHeight()
    {
        int nValidHeight = getBlockAverageValidHeight();
        int nFullHeight = this.getBlockAverageHeight();
        return nFullHeight - nValidHeight;
    }


}

