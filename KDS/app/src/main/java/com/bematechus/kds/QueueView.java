package com.bematechus.kds;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.bematechus.kdslib.BuildVer;
import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;
import com.bematechus.kdslib.TimeDog;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Administrator on 2016/12/23.
 */
public class QueueView  extends View {

    final String TAG = "QueueView";
    final int DEFAULT_SWITCH_PAGE_TIMEOUT_MS = 5000;
    private int ITEM_AVERAGE_HEIGHT = 80;
    final int BORDER_GAP = 4;
    private float ORDERID_AREA_PERCENT = (float) 0.40;// (float) 0.25;

    int COLOR_VIEW_BG = Color.DKGRAY;//.BLACK;

    int m_nCols = 3; //how many columns
    int m_nRows = 1; //this was calculated

    GestureDetector m_gesture = null;//
    QueueOrders m_queueOrders = new QueueOrders();

    boolean m_bMoveReadyFront = false;
    boolean m_bFlashReadyOrder = false;

    String m_strMoreOrders = "";

    KDSViewFontFace m_ftOrderID = new KDSViewFontFace();
    KDSViewFontFace m_ftCustomerName = new KDSViewFontFace();
    KDSViewFontFace m_ftOrderTimer = new KDSViewFontFace();

    KDSViewFontFace m_ftStatusReceived = new KDSViewFontFace();
    KDSViewFontFace m_ftStatusPreparation = new KDSViewFontFace();
    KDSViewFontFace m_ftStatusReady = new KDSViewFontFace();
    KDSViewFontFace m_ftStatusPickup = new KDSViewFontFace();
    KDSViewFontFace m_ftStatusCustomMessage = new KDSViewFontFace();

    int m_colorFocused = Color.YELLOW;

    String m_statusReceived = "";
    String m_statusPreparation = "";
    String m_statusReady = "";
    String m_statusPickup = "";

    boolean m_bShowFinishedRight = false;

    //

    Paint m_paintOrderTitle = new Paint();
    Paint m_paintOrderStatus = new Paint();

    KDSSettings.QueueMode m_nViewMode = KDSSettings.QueueMode.Simple;//.Panels;

    ArrayList<Integer> m_arPageCounter = new ArrayList<>();

    //int m_nPageCounter = 0;
    TimeDog m_tdPage = new TimeDog();
    int PAGE_TIMEOUT = DEFAULT_SWITCH_PAGE_TIMEOUT_MS;

    Object m_locker = new Object();

    QueueOrders.QueueSort m_status1Sort = QueueOrders.QueueSort.Default;
    QueueOrders.QueueSort m_status2Sort = QueueOrders.QueueSort.Default;
    QueueOrders.QueueSort m_status3Sort = QueueOrders.QueueSort.Default;
    QueueOrders.QueueSort m_status4Sort = QueueOrders.QueueSort.Default;


    long m_nAutoBumpTimeoutMs = 0;

    public QueueView(Context context)
    {
        super(context);
        init_gesture();
        init();

    }

    public QueueView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init_gesture();
        init();
    }

    public QueueView(Context context, AttributeSet attrs, int defaultStyle)
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
        Rect r =  rect;

        return r.height() /averagePanelHeight;

    }

    public int getColWidth(Rect rect, int nCols)
    {
        Rect r = rect;// this.getBounds();
        return r.width()/nCols;
    }
    public int getRowHeight(Rect rect, int nRows, int nRow)
    {
        Rect r = rect;//
        //calculateRows();
        int n =( r.height() % nRows);//
        if (nRows == 0) return 0;
        int nAverageH = r.height()/nRows;

        if (n >0)
        {
            if (nRow >= m_nRows - n )
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
        for (int i=0; i< QueueOrders.QueueStatus.values().length; i++)
        {
            m_arPageCounter.add(0);
        }

        resetAllPaint();

    }
    public void onLongTouchDown(MotionEvent e)
    {


    }

    public void onDoubleClick(MotionEvent e)
    {

    }


    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onDown(MotionEvent event) {
            // Log.d(DEBUG_TAG,"onDown: " + event.toString());
            QueueView.this.onTouchDown(event);
            return true;
        }
        public void onLongPress(MotionEvent e) {
            QueueView.this.onLongTouchDown(e);
        }


        @Override
        public boolean onDoubleTap(MotionEvent e) {
            QueueView.this.onDoubleClick(e);

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

            default:
                // m_gesture.onTouchEvent(event);
                break;
        }
        return false;

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

        return b;
    }

    private boolean touchXY(int x, int y)
    {

        synchronized (m_locker) {
            KDSDataOrders queueOrders =  m_queueOrders.getOrders();
            int ncount = m_queueOrders.getCoordinates().size();// m_arPanels.size();
            for (int i = 0; i < ncount; i++) {
                Rect rt = m_queueOrders.getCoordinates().get(i);


                if (!rt.contains(x, y))
                    continue;

                fireQueueItemClicked(queueOrders.get(i));
                return false;
            }
            return false;
        }
    }

    private void fireQueueItemClicked(KDSDataOrder order)
    {
        //focusOrder(order.getGUID()); /don't need focus in queue
    }

    private void init_gesture()
    {
        m_gesture = new GestureDetector(this.getContext(), new MyGestureListener());


    }
    Handler m_refreshHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            m_refreshHandler.removeMessages(0);

            QueueView.this.invalidate();
            return true;
        }
    });
    public void refresh()
    {

        //this.invalidate();
        Message m = new Message();
        m.what = 0;
        m_refreshHandler.sendMessage(m);
    }
    public Rect getBounds()
    {
        Rect rc = new Rect();

        this.getDrawingRect(rc);
       // rc.inset(2,2);

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
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        return specSize;



    }

    protected int measureWidth(int widthMeasureSpec)
    {
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
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
//        if (m_sortMode != QueueOrders.QueueSort.Default )
//        {//2.0.35
//            m_queueOrders.sortByStateTime(m_sortMode == QueueOrders.QueueSort.State_descend);
//        }
        boolean bReverseReadyColorForFlash = false;
        Calendar c = Calendar.getInstance();
        int second = c.get(Calendar.SECOND);
        if ( (second%2)==0)
            bReverseReadyColorForFlash = true;

        try {
            //drawBackground(canvas);
            //synchronized (m_queueOrders.getOrders().m_locker)
            {
                switch (m_nViewMode) {
                    case Panels:
                        onDrawPanelMode(canvas, bReverseReadyColorForFlash);
                        break;
                    case Simple:
                        //m_queueOrders.sortByStateTime(m_status1Sort, m_status2Sort, m_status3Sort, m_status4Sort);//2.0.36
                        onDrawSimpleMode(canvas, bReverseReadyColorForFlash);
                        break;
                }
            }
            if (!m_strInputOrderID.isEmpty()) {

                drawInputingIDIcon(canvas);
            }
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);

        }

    }

    private final int ICON_SIZE = 24;
    private void drawInputingIDIcon(Canvas canvas)
    {
        Drawable drawable = this.getContext().getResources().getDrawable(com.bematechus.kdslib.R.drawable.edit_24px_16);
        Rect rect = this.getBounds();
        int nsize =canvas.getHeight();// thisrcAbsolute.height();

        //drawable.setBounds(rcAbsolute.left, rcAbsolute.top + nsize / 2, rcAbsolute.left + nsize, rcAbsolute.bottom - nsize / 2);//, rcAbsolute.height());
        drawable.setBounds(rect.right - ICON_SIZE, rect.bottom - ICON_SIZE, rect.right, rect.bottom);//, rcAbsolute.height());

        drawable.draw(canvas);
    }

    private void onDrawPanelMode(Canvas canvas, boolean bReverseColorForFlush)
    {
        if (canvas == null) return;
        Rect rect = this.getBounds();

        Canvas g = get_double_buffer();
        if (g == null) return;
        drawBackground(g);
        drawScreenLogo(g); //kpp1-293

        //if (m_queueOrders.getOrders() == null) return;
        if (!m_queueOrders.isReady()) return;

        synchronized (m_locker) {
            m_queueOrders.resetCoordinates();

            if (!m_bMoveReadyFront)
                drawNormalQueue(g, rect, bReverseColorForFlush);
            else
                drawReadyMoveFrontQueue2(g, rect, bReverseColorForFlush);
        }


        commit_double_buffer(canvas);
    }


    private QueueOrders.QueueStatus convertStatusAccordingVisible(QueueOrders.QueueStatus status)
    {
        ArrayList<Boolean> arVisible = new ArrayList<>();
        arVisible.add(m_bSimpleModeShowReceived);
        arVisible.add(m_bSimpleModeShowPreparation);
        arVisible.add(m_bSimpleModeShowReady);
        arVisible.add(m_bSimpleModeShowPickup);
        int n = status.ordinal();
        for (int i=n; i>=0; i--)
        {
            if (arVisible.get(i))
                return QueueOrders.QueueStatus.values()[i];
            else
                n--;
        }
        if (n<0)
        {
            for (int i=n+1; i<= QueueOrders.QueueStatus.Pickup.ordinal(); i++)
            {
                if (arVisible.get(i))
                    return QueueOrders.QueueStatus.values()[i];
                else
                    n++;
            }
        }
        if (n> QueueOrders.QueueStatus.Pickup.ordinal())
            n =0;
        return QueueOrders.QueueStatus.values()[n];

    }
    boolean m_bSimpleModeShowReceived = true;
    boolean m_bSimpleModeShowPreparation = true;
    boolean m_bSimpleModeShowReady = true;
    boolean m_bSimpleModeShowPickup = true;

    QueueOrders.QueueStatus m_receivedCombinedToStatus = QueueOrders.QueueStatus.Preparation;
    QueueOrders.QueueStatus m_preparationCombinedToStatus = QueueOrders.QueueStatus.Ready;
    QueueOrders.QueueStatus m_readyCombinedToStatus = QueueOrders.QueueStatus.Pickup;
    QueueOrders.QueueStatus m_pickupCombinedToStatus = QueueOrders.QueueStatus.Ready;

    private ArrayList<QueueOrders.QueueStatus> getSimpleColStatus(QueueOrders.QueueStatus willShowThisStatus)
    {
        ArrayList<QueueOrders.QueueStatus> ar = new  ArrayList<>();
        ar.add(willShowThisStatus);
        switch (willShowThisStatus)
        {

            case Received:
                if (!m_bSimpleModeShowPreparation && (m_preparationCombinedToStatus == QueueOrders.QueueStatus.Received))
                    ar.add(QueueOrders.QueueStatus.Preparation);

                if (!m_bSimpleModeShowReady && (m_readyCombinedToStatus == QueueOrders.QueueStatus.Received))
                    ar.add(QueueOrders.QueueStatus.Ready);

                if (!m_bSimpleModeShowPickup && (m_pickupCombinedToStatus == QueueOrders.QueueStatus.Received))
                    ar.add(QueueOrders.QueueStatus.Pickup);

                break;
            case Preparation:
                if (!m_bSimpleModeShowReceived && (m_receivedCombinedToStatus == QueueOrders.QueueStatus.Preparation))
                    ar.add(QueueOrders.QueueStatus.Received);


                if (!m_bSimpleModeShowReady && (m_readyCombinedToStatus == QueueOrders.QueueStatus.Preparation))
                    ar.add(QueueOrders.QueueStatus.Ready);

                if (!m_bSimpleModeShowPickup && (m_pickupCombinedToStatus == QueueOrders.QueueStatus.Preparation))
                    ar.add(QueueOrders.QueueStatus.Pickup);

                break;
            case Ready:
                if (!m_bSimpleModeShowReceived && (m_receivedCombinedToStatus == QueueOrders.QueueStatus.Ready))
                    ar.add(QueueOrders.QueueStatus.Received);

                if (!m_bSimpleModeShowPreparation && (m_preparationCombinedToStatus == QueueOrders.QueueStatus.Ready))
                    ar.add(QueueOrders.QueueStatus.Preparation);


                if (!m_bSimpleModeShowPickup && (m_pickupCombinedToStatus == QueueOrders.QueueStatus.Ready))
                    ar.add(QueueOrders.QueueStatus.Pickup);
                break;
            case Pickup:

                if (!m_bSimpleModeShowReceived && (m_receivedCombinedToStatus == QueueOrders.QueueStatus.Pickup))
                    ar.add(QueueOrders.QueueStatus.Received);

                if (!m_bSimpleModeShowPreparation && (m_preparationCombinedToStatus == QueueOrders.QueueStatus.Pickup))
                    ar.add(QueueOrders.QueueStatus.Preparation);

                if (!m_bSimpleModeShowReady && (m_readyCombinedToStatus == QueueOrders.QueueStatus.Pickup))
                    ar.add(QueueOrders.QueueStatus.Ready);


                break;
        }
        return ar;
    }



    public boolean isQueueExpo()
    {
        return KDSGlobalVariables.getKDS().isQueueExpo();
    }
    public boolean isExistedPrepForQueueExpo()
    {
        String stationID =  KDSGlobalVariables.getKDS().getStationID();
        return (KDSGlobalVariables.getKDS().getStationsConnections().getWhoUseMeAsExpo(stationID) >0);
    }
    /**
     * 4 column for 4 status.
     * Just show order number
     * @param canvas
     */
    private void onDrawSimpleMode(Canvas canvas, boolean bReverseReadyColorForFlash)
    {
        if (canvas == null) {
            //System.out.println("onDrawSimpleMode if (canvas == null)" );
            return;
        }
        Rect rect = this.getBounds();

        Canvas g = get_double_buffer();
        if (g == null) {
            //System.out.println("onDrawSimpleMode if (g == null)" );
            return;
        }
        drawBackground(g);
        drawScreenLogo(g);//kpp1-293
        //if (m_queueOrders.getOrders() == null) {
        if (!m_queueOrders.isReady()) {
            //System.out.println("onDrawSimpleMode if (m_queueOrders.getOrders() == null)" );

            return;
        }
        m_queueOrders.resetCoordinates();

        if (isQueueExpo())
        {
            //force show this two col
            if (!isExistedPrepForQueueExpo()) {
                m_bSimpleModeShowReceived = true;
                m_bSimpleModeShowPickup = true;
                m_bSimpleModeShowPreparation = false;
                m_bSimpleModeShowReady = false;
            }
        }

        int nCount = 0;
        if (m_bSimpleModeShowReceived)
            nCount ++;
        if (m_bSimpleModeShowPreparation)
            nCount ++;
        if (m_bSimpleModeShowReady)
            nCount ++;
        if (m_bSimpleModeShowPickup)
            nCount ++;
        if (nCount == 0) {
            nCount = 1;
            m_bSimpleModeShowReceived = true;
        }
        int nColWidth = rect.width()/nCount;
        int nGap = 0;
        nColWidth -= nGap;
        int nIndex = 0;
        if (m_bSimpleModeShowReceived) {
            Rect rt = new Rect(rect);
            rt.right = rt.left + nColWidth;
            drawSimpleModeCol(g, rt, getSimpleColStatus(QueueOrders.QueueStatus.Received), bReverseReadyColorForFlash);
            nIndex ++;
        }

        if (m_bSimpleModeShowPreparation) {
            Rect rt = new Rect(rect);
            rt.left = rt.left + (nColWidth+nGap) *nIndex;
            rt.right = rt.left + nColWidth;
            drawSimpleModeCol(g, rt, getSimpleColStatus(QueueOrders.QueueStatus.Preparation), bReverseReadyColorForFlash);
            nIndex ++;
        }
        if (m_bSimpleModeShowReady) {
            Rect rt = new Rect(rect);
            rt.left = rt.left + (nColWidth+nGap) *nIndex;
            rt.right = rt.left + nColWidth;
            drawSimpleModeCol(g, rt,getSimpleColStatus( QueueOrders.QueueStatus.Ready), bReverseReadyColorForFlash);
            nIndex ++;
        }
        if (m_bSimpleModeShowPickup) {
            Rect rt = new Rect(rect);
            rt.left = rt.left + (nColWidth+nGap) *nIndex;
            rt.right = rt.left + nColWidth;
            drawSimpleModeCol(g, rt, getSimpleColStatus(QueueOrders.QueueStatus.Pickup), bReverseReadyColorForFlash);
            nIndex++;
        }




        commit_double_buffer(canvas);
    }

    private void drawNormalQueue(Canvas g,Rect rect, boolean bReverseColorForFlush)
    {
        KDSDataOrders queueOrders =  m_queueOrders.getOrders();
        int ncount = queueOrders.getCount();
        int nRows = calculateRows(rect, ITEM_AVERAGE_HEIGHT);
        int nTotalPanels = nRows * m_nCols;
        for (int i=0; i< ncount; i++)
        {
            int nMoreOrders = 0;
            if (i>= nTotalPanels-1)
            {
                nMoreOrders = ncount -i-1;
            }
            if (nMoreOrders >0)
            {
                drawMoreItems(g, rect, nRows, m_nCols,nMoreOrders , i);
                break;
            }
            else {
                drawItem(g, rect, nRows, m_nCols, queueOrders.get(i), i, bReverseColorForFlush);// m_items.get(i),WeekEvent.EVENT_COLOR_BG);
            }
        }
    }

    final int NOT_READY_COLS = 1;
    final float READY_RECT_PERCENT = (float)0.75;


    private void drawReadyMoveFrontQueue2(Canvas g,Rect rect, boolean bReverseColorForFlush) {
        KDSDataOrders queueOrders = m_queueOrders.getOrders();
        movePreparationFront(queueOrders);
        moveReadyFront(queueOrders);

        //split the rect
        Rect rtReady = new Rect(rect);
        // Rect rtPreparation = new Rect(rect);
        Rect rtNotReady = new Rect(rect);



        if (m_bShowFinishedRight) {
            rtReady.left = rtReady.right - (int) ((float) rect.width() * READY_RECT_PERCENT);
            rtNotReady.right = rtReady.left - 2;
        }
        else
        {
            rtReady.right = rtReady.left + (int) ((float) rect.width() * READY_RECT_PERCENT);
            rtNotReady.left = rtReady.right + 2;
        }


        ArrayList<Integer> arPages = new ArrayList<>();

        drawReadyInMoveFrontMode2(g, rtReady, m_nCols, arPages, bReverseColorForFlush);

        drawNotReadyInMoveFrontMode2(g, rtNotReady, bReverseColorForFlush);

    }

    private void drawNotReadyInMoveFrontMode2(Canvas g, Rect rtNotReady, boolean bReverseColorForFlush)
    {
        Rect rtData = new Rect(rtNotReady);
        rtData.bottom -= PAGE_NUMBER_ROW_HEIGHT;
        int nRows = calculateRows(rtData,ITEM_AVERAGE_HEIGHT);
        KDSDataOrders queueOrders =  m_queueOrders.getOrders();
        int ncount = queueOrders.getCount();
        int nPanelIndex = 0;
        int nTotalPanels = nRows * NOT_READY_COLS;
        ArrayList<QueueOrders.QueueStatus> status = new ArrayList<>();
        status.add(QueueOrders.QueueStatus.Received);
      //  status.add(QueueOrders.QueueStatus.Preparation);
        int nPagesCount = getPageCount(status, nRows, NOT_READY_COLS);
        int nPageIndex = getCurrentPageIndex(nPagesCount, QueueOrders.QueueStatus.Received.ordinal());
        int nItemsStartIndex = nPageIndex * nTotalPanels;
        int nCurrentItemIndex = -1;

        for (int i=0; i< ncount; i++)
        {
            KDSDataOrder order = queueOrders.get(i);
            if (m_queueOrders.getStatus(order) == QueueOrders.QueueStatus.Received)
            {
                nCurrentItemIndex ++;
                if (nCurrentItemIndex < nItemsStartIndex) continue;


                if (nPanelIndex>= nTotalPanels)
                {
                    break;
                }
                drawItem(g, rtData, nRows, NOT_READY_COLS, order, nPanelIndex, bReverseColorForFlush);// m_items.get(i),WeekEvent.EVENT_COLOR_BG);
                nPanelIndex++;

            }
        }

        Rect rtPage = new Rect(rtNotReady);
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
            pt.setColor(m_ftOrderID.getFG());
            String strPage = String.format("%d/%d",nPageIndex+1, nPagesCount );
            drawString(g, pt, rtPage, Paint.Align.CENTER, strPage);
        }
    }

    private void drawReadyInMoveFrontMode2(Canvas g, Rect rtReady, int nMaxCols, ArrayList<Integer> arPages, boolean bReversColorForFlush)
    {
        Rect rtData = new Rect(rtReady);
        rtData.bottom -= PAGE_NUMBER_ROW_HEIGHT;
        int nRows = calculateRows(rtData,ITEM_AVERAGE_HEIGHT);
        KDSDataOrders queueOrders =  m_queueOrders.getOrders();
        int ncount = queueOrders.getCount();
        int nPanelIndex = 0;
        int nTotalPanels = nRows * nMaxCols;

        ArrayList<QueueOrders.QueueStatus> status = new ArrayList<>();
        status.add(QueueOrders.QueueStatus.Pickup);
        status.add(QueueOrders.QueueStatus.Ready);
        status.add(QueueOrders.QueueStatus.Preparation);
        int nPagesCount = getPageCount(status, nRows, nMaxCols);
        int nPageIndex = getCurrentPageIndex(nPagesCount, QueueOrders.QueueStatus.Preparation.ordinal());
        int nItemsStartIndex = nPageIndex * nTotalPanels;

        int nCurrentItemIndex = -1;

        for (int i=0; i< ncount; i++)
        {
            KDSDataOrder order = queueOrders.get(i);
            if (isReadyOrder(order) || isPickupOrder(order) ||
                    isPreparationOrder(order)) {
                nCurrentItemIndex ++;
                if (nCurrentItemIndex <nItemsStartIndex)
                    continue;
                //int nMoreOrders = 0;
                if (nPanelIndex>= nTotalPanels)
                {
                    break;
                }

                drawItem(g, rtData, nRows, nMaxCols, order, nPanelIndex, bReversColorForFlush);// m_items.get(i),WeekEvent.EVENT_COLOR_BG);
                nPanelIndex++;

            }
        }
        arPages.clear();
        arPages.add(nPageIndex);
        arPages.add(nPagesCount);
        Rect rtPage = new Rect(rtReady);
        rtPage.top = rtPage.bottom - PAGE_NUMBER_ROW_HEIGHT;
        drawPagesForPanelsMode(g ,rtPage, nPageIndex, nPagesCount);

    }


//    /**
//     *
//     * @param ordersA
//     * @param ordersB
//     *  set two orders to me
//     */
//    public void showOrders(KDSDataOrders ordersA, KDSDataOrders ordersB)
//    {
//        synchronized (m_locker) {
//            m_queueOrders.setOrders(ordersA, ordersB);
//            if (m_nViewMode == KDSSettings.QueueMode.Simple)
//            {//move from ondraw to here.
//                m_queueOrders.sortByStateTime(m_status1Sort, m_status2Sort, m_status3Sort, m_status4Sort);//2.0.36
//            }
//            m_nRedrawRequestCounter++;
//        }
//        startShowOrdersThread();
//        /*
//        synchronized (m_locker) {
//            m_queueOrders.setOrders(orders);
////            if (m_sortMode != QueueOrders.QueueSort.Default )
////            {//2.0.35
////                m_queueOrders.sortByStateTime(m_sortMode == QueueOrders.QueueSort.State_descend);
////            }
//            if (m_nViewMode == KDSSettings.QueueMode.Simple)
//                m_queueOrders.sortByStateTime(m_status1Sort,m_status2Sort,m_status3Sort,m_status4Sort );
//
//            if (m_bMoveReadyFront)
//                moveReadyFront(orders);
//
//            if (m_queueOrders.getFocusedOrderGUID().isEmpty()) {
//                if (orders.getCount() > 0) {
//                    if (!isQueueExpo())
//                        m_queueOrders.setFocusedOrderGuid(orders.getFirstOrderGuid());
//                }
//            }
//        }
//        refresh();
//        */
//    }

    private void moveReadyFront(KDSDataOrders orders)
    {
        if (!m_bMoveReadyFront) return;

        synchronized (m_locker) {
            ArrayList<KDSDataOrder> arReady = new ArrayList<>();
            int ncount = orders.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataOrder order = orders.get(i);
                if (isReadyOrder(order) ||
                        isPickupOrder(order)) {
                    arReady.add(order);
                }
            }
            if (arReady.size() > 0) {
                for (int i = 0; i < arReady.size(); i++) {
                    orders.removeComponent(arReady.get(i));
                }
                for (int i = arReady.size() - 1; i >= 0; i--) {
                    orders.insertComponent(0, arReady.get(i));
                }
            }

            movePickupFront(orders);
        }
    }

    private void movePickupFront(KDSDataOrders orders)
    {
        if (!m_bMoveReadyFront) return;
        synchronized (m_locker) {
            ArrayList<KDSDataOrder> ar = new ArrayList<>();
            int ncount = orders.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataOrder order = orders.get(i);
                if (isPickupOrder(order)) {
                    ar.add(order);
                }
            }
            if (ar.size() > 0) {
                for (int i = 0; i < ar.size(); i++) {
                    orders.removeComponent(ar.get(i));
                }
                for (int i = ar.size() - 1; i >= 0; i--) {
                    orders.insertComponent(0, ar.get(i));
                }
            }
        }

    }



    private void movePreparationFront(KDSDataOrders orders)
    {
        if (!m_bMoveReadyFront) return;
        synchronized (m_locker) {
            ArrayList<KDSDataOrder> arPrepartion = new ArrayList<>();
            int ncount = orders.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataOrder order = orders.get(i);
                if (isPreparationOrder(order)) {
                    arPrepartion.add(order);
                }
            }
            if (arPrepartion.size() > 0) {
                for (int i = 0; i < arPrepartion.size(); i++) {
                    orders.removeComponent(arPrepartion.get(i));
                }
                for (int i = arPrepartion.size() - 1; i >= 0; i--) {
                    orders.insertComponent(0, arPrepartion.get(i));
                }
            }
        }

    }




    protected void drawBackground(Canvas g)
    {

        g.drawColor(COLOR_VIEW_BG);
    }


    private void resetAllPaint()
    {

        m_paintOrderTitle.setAntiAlias(true);
        m_paintOrderStatus.setAntiAlias(true);


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
        //if (m_bExpoBumpEnabled) //if expo bump queue order, we will not show focus border
        if (!KDSGlobalVariables.getKDS().isQueueExpo()) {
            if (getExpoBumpEnabled())
                bFocused = false;
        }
        if (KDSGlobalVariables.getKDS().isQueueExpo())
            bFocused = false; //don't show border focus, just focus order ID
        if (!bFocused)
        {
            rect.inset(BORDER_GAP, BORDER_GAP);

            return rect;
        }
        else
        {
            m_paintOrderTitle.setColor(m_colorFocused);
            rect.inset(-1*FOCUS_BOX_SIZE, -1*FOCUS_BOX_SIZE);
            g.drawRect(rect, m_paintOrderTitle); //draw whole bg
            rect.inset(FOCUS_BOX_SIZE, FOCUS_BOX_SIZE);
            //draw rect for line
            rect.inset(BORDER_GAP - FOCUS_BOX_SIZE, BORDER_GAP - FOCUS_BOX_SIZE);
            m_paintOrderTitle.setColor(COLOR_VIEW_BG);
            g.drawRect(rect, m_paintOrderTitle); //draw whole bg
            rect.inset(FOCUS_BOX_SIZE, FOCUS_BOX_SIZE);
            return rect;
        }
    }

    private boolean isReadyOrder(KDSDataOrder order)
    {
        if (order == null) return false;
        synchronized (m_locker) {
            QueueOrders.QueueStatus orderStatus = m_queueOrders.getStatus(order);

            if (orderStatus == QueueOrders.QueueStatus.Ready)//||
            //orderStatus == QueueOrders.QueueStatus.Preparation ||
            //orderStatus ==QueueOrders.QueueStatus.Pickup  )
            {
                return true;
            }
            return false;
        }
    }

    private boolean isPickupOrder(KDSDataOrder order)
    {
        synchronized (m_locker) {
            QueueOrders.QueueStatus orderStatus = m_queueOrders.getStatus(order);
            if (
                    orderStatus == QueueOrders.QueueStatus.Pickup) {
                return true;
            }
            return false;
        }
    }

    private boolean isPreparationOrder(KDSDataOrder order)
    {
        QueueOrders.QueueStatus orderStatus =m_queueOrders.getStatus(order);
        return ( orderStatus == QueueOrders.QueueStatus.Preparation );

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
    /**
     *      ----------------------------------
     *      order | Status string
     *      126   |
     *      ----------------------------------
     *
     *
     * @param g
     */
    public void drawItem(Canvas g,Rect rect, int nRows,int nCols,  KDSDataOrder order, int nPanelIndex, boolean bReverseReadyColor)
    {
        Rect rtCell = getItemRect(rect, nRows,nCols,nPanelIndex);
        if (rtCell.isEmpty()) return;
        synchronized (m_locker) {
            m_queueOrders.setOrderCoordinate(order, rtCell);
        }

        if (order == null) return;

       g.save();
        //set the drawing area
        try {


            rtCell = drawItemBorder(g, rtCell, order.getGUID().equals(m_queueOrders.getFocusedOrderGUID()));
            g.clipRect(rtCell);

            boolean bReverseReadyColorForFlash = false;
            if (m_bFlashReadyOrder) {
                if (isReadyOrder(order))//||
                //isPreparationOrder(order))
                {
                    bReverseReadyColorForFlash = bReverseReadyColor;
//                Calendar c = Calendar.getInstance();
//                int second = c.get(Calendar.SECOND);
//                if ( (second%2)==0)
//                    bReverseReadyColorForFlash = true;
                }
            }
            // Paint paintBG = new Paint();
            m_paintOrderTitle.setColor(getBG(m_ftOrderID, bReverseReadyColorForFlash)); //m_ftOrderID.getBG());
            //m_paintOrderTitle.setColor( getBG(m_ftOrderID, false));//bReverseReadyColorForFlash)); //m_ftOrderID.getBG());
            g.drawRect(rtCell, m_paintOrderTitle); //draw whole bg

            int nTitleAreaWidth = (int) ((float) rtCell.width() * ORDERID_AREA_PERCENT);

            Rect rtTitle = new Rect(rtCell);
            rtTitle.right = rtTitle.left + nTitleAreaWidth;
            drawTitle(g, rtTitle, order, false);// bReverseReadyColorForFlash);

            Rect rtStatus = new Rect(rtCell);
            rtStatus.left = rtStatus.left + nTitleAreaWidth;
            drawStatus(g, rtStatus, order, bReverseReadyColorForFlash);
        }
        catch ( Exception e)
        {
            e.printStackTrace();
        }
        g.restore();
   //     g.clipRect(rect);
    }

    private String makeMoreItemsString(int nMoreItemsCount)
    {
        String s =String.format("%s --> %d", m_strMoreOrders , nMoreItemsCount);
        return s;
    }

    public void drawMoreItems(Canvas g,Rect rect, int nRows,int nCols,  int nMoreItemsCount, int nPanelIndex)
    {
        Rect rtCell = getItemRect(rect, nRows,nCols,nPanelIndex);
        if (rtCell.isEmpty()) return;

        g.save();
        try {


            rtCell = drawItemBorder(g, rtCell, false);
            g.clipRect(rtCell);


            m_paintOrderTitle.setColor(m_ftOrderID.getBG());
            g.drawRect(rtCell, m_paintOrderTitle); //draw whole bg

            m_paintOrderTitle.setColor(m_ftOrderID.getFG());
            String s = makeMoreItemsString(nMoreItemsCount);
            drawString(g, m_paintOrderTitle, rtCell, Paint.Align.CENTER, s);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

       // x = adjustToTab(x, 6);

        g.drawText(string, x, y, pt);

    }

    /**
     * 20170804 Change it to just focus foreground text color.
     * Don't need the background color.
     * @param g
     * @param pt
     * @param rt
     * @param align
     * @param string
     * @param nBG
     */
    private void drawStringWithBG(Canvas g,Paint pt, Rect rt, Paint.Align align, String string, int nBG )
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


        pt.setColor(nBG);

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
    private Rect drawTitle(Canvas g, Rect rt, KDSDataOrder order, boolean bReverseReadyColorForFlash)
    {

        if (getSettings().getBoolean(KDSSettings.ID.Queue_show_customer_name)){
            rt = drawTitleDetail(g, rt,m_paintOrderTitle, m_ftCustomerName, order.getToTable(), false,bReverseReadyColorForFlash);
        }
        //if (m_bShowCustomMessage)
        if (getSettings().getBoolean(KDSSettings.ID.Queue_show_custom_message))
            rt = drawTitleDetail(g, rt,m_paintOrderTitle, m_ftStatusCustomMessage, order.getQueueMessage(), false,bReverseReadyColorForFlash);
        //if (m_bShowOrderTimer)
        if (getSettings().getBoolean(KDSSettings.ID.Queue_show_order_timer))
            rt = drawTitleDetail(g, rt,m_paintOrderTitle, m_ftOrderTimer, order.makeQueueDurationString(), false,bReverseReadyColorForFlash);
            //rt = drawTitleDetail(g, rt,m_paintOrderTitle, m_ftOrderTimer, order.makeDurationString(), false,bReverseReadyColorForFlash);
        //if (m_bShowOrderID)
        if (getSettings().getBoolean(KDSSettings.ID.Queue_show_order_ID)) {
            int n = getSettings().getInt(KDSSettings.ID.Queue_order_id_length);
            String strOrderName = order.getOrderName();
            if (n >0 ) //it is not all(0)
            {
                if (strOrderName.length() >n)
                    strOrderName = strOrderName.substring(strOrderName.length()-n);

            }
            if (isQueueExpo()) {
                boolean bFocused = false;
                if (order.getGUID().equals(getFocusedGuid()))
                    bFocused = true;


                if (!bFocused)
                    rt = drawTitleDetail(g, rt, m_paintOrderTitle, m_ftOrderID, strOrderName, true, bReverseReadyColorForFlash);
                else {

                    rt = drawTitleDetailForFocus(g, rt, m_paintOrderTitle, m_ftOrderID, strOrderName, true, bReverseReadyColorForFlash, m_colorFocused);


                }
            }
            else
                rt = drawTitleDetail(g, rt, m_paintOrderTitle, m_ftOrderID, strOrderName, true, bReverseReadyColorForFlash);

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
        if (ft == null) return rt;
        String title  =  strTitle;
        if (title.isEmpty()) return rt;
        Rect rtText = new Rect();
        pt.setTypeface(ft.getTypeFace());
        pt.setTextSize(ft.getFontSize());
        pt.getTextBounds(title, 0, title.length() - 1, rtText);
      //  Paint.FontMetrics fm =  pt.getFontMetrics();
       // int n =(int)( Math.abs(fm.bottom) + Math.abs(fm.top));

        rtText.set(rt.left, rt.top, rt.right, rt.top + rtText.height() +4);// + (int)fm.descent);
        if (bMiddle)
            rtText = rt;
        pt.setColor( getBG(ft, bReverseReadyColorForFlash));// ft.getBG());
        //pt.setAlpha(0);
        g.drawRect(rtText, pt);
        //pt.setAlpha(255);
        pt.setColor(getFG(ft, bReverseReadyColorForFlash));//ft.getFG());
        //pt.setColor(getFG(ft, false));//ft.getFG());
        drawString(g, m_paintOrderTitle, rtText, Paint.Align.CENTER, title);
        rt.top = rtText.bottom;
        return rt;
    }

    private Rect drawTitleDetailForFocus(Canvas g, Rect rt,Paint pt,  KDSViewFontFace ft, String strTitle, boolean bMiddle,boolean bReverseReadyColorForFlash, int focusBG)
    {
        String title  =  strTitle;
        if (title.isEmpty()) return rt;
        Rect rtText = new Rect();
        pt.setTypeface(ft.getTypeFace());
        pt.setTextSize(ft.getFontSize());
        pt.getTextBounds(title, 0, title.length() - 1, rtText);
        int nTextH = rtText.height();
        int nTextW = rtText.width();

        //  Paint.FontMetrics fm =  pt.getFontMetrics();
        // int n =(int)( Math.abs(fm.bottom) + Math.abs(fm.top));

        rtText.set(rt.left, rt.top, rt.right, rt.top + rtText.height() +4);// + (int)fm.descent);
        if (bMiddle)
            rtText = rt;
        pt.setColor( getBG(ft, bReverseReadyColorForFlash));// ft.getBG());
        //pt.setAlpha(0);
        g.drawRect(rtText, pt);

        pt.setColor(getFG(ft, bReverseReadyColorForFlash));//ft.getFG());


        drawStringWithBG(g, m_paintOrderTitle, rtText, Paint.Align.CENTER, title, focusBG);
        rt.top = rtText.bottom;
        return rt;
    }

    private String getStatusString(QueueOrders.QueueStatus status)
    {
        switch (status)
        {

            case Received:
                return m_statusReceived;

            case Preparation:
                return m_statusPreparation;

            case Ready:
                return m_statusReady;

            case Pickup:
                return m_statusPickup;

        }
        return "";
    }

    private void drawStatus(Canvas g, Rect rt, KDSDataOrder order, boolean bReverseReadyColorForFlash)
    {
        QueueOrders.QueueStatus status =  m_queueOrders.getStatus( order);
        drawStatus(g, rt, status, bReverseReadyColorForFlash);

    }

    private KDSViewFontFace getStatusFontFace(QueueOrders.QueueStatus status)
    {
        KDSViewFontFace ft = null;
        switch (status)
        {

            case Received:
                ft = m_ftStatusReceived;

                break;
            case Preparation:
                ft = m_ftStatusPreparation;

                break;
            case Ready:
                ft = m_ftStatusReady;

                break;
            case Pickup:
                ft = m_ftStatusPickup;
                break;

            default:
                break;
        }
        return ft;
        //if (ft == null) return null;

    }
    private void drawStatus(Canvas g, Rect rt,QueueOrders.QueueStatus status, boolean bReverseReadyColorForFlash)
    {

        String statusString =getStatusString(status);//


        KDSViewFontFace ft = getStatusFontFace(status);

        if (ft == null) return;

        m_paintOrderStatus.setColor( getBG(ft, false));// ft.getBG());
        g.drawRect(rt, m_paintOrderStatus);

        drawWrapString(g, ft, bReverseReadyColorForFlash, rt, Paint.Align.CENTER, statusString);


    }

    private void drawPages(Canvas g, Rect rt,QueueOrders.QueueStatus status, ArrayList<Integer> arPages)
    {

        boolean bClearBG = false;
        if (arPages.size()<=0)
        {
            bClearBG = true;
        }

        int nCurrentPage = arPages.get(0) + 1; //base 1
        int nPages = arPages.get(1);
        if (nPages <= 1)
        {
            bClearBG = true;
        }
        if (bClearBG )
        {
            m_paintOrderStatus.setColor(  COLOR_VIEW_BG );
            g.drawRect(rt, m_paintOrderStatus);
            return;
        }
        String pagesString = String.format("%d/%d", nCurrentPage, nPages);//


        KDSViewFontFace ft = getStatusFontFace(status);
        if (ft == null) return;

        m_paintOrderStatus.setColor( getBG(ft, false));// ft.getBG());
        g.drawRect(rt, m_paintOrderStatus);

        drawWrapString(g, ft, false, rt, Paint.Align.CENTER, pagesString);
    }

    private void drawWrapString(Canvas g,KDSViewFontFace ft,boolean bReverseColor, Rect rt, Paint.Align align, String string )
    {
        g.save();
        try {


            TextPaint textPaint = new TextPaint();
            textPaint.setTextSize(ft.getFontSize());
            textPaint.setTypeface(ft.getTypeFace());
            textPaint.setColor(getFG(ft, bReverseColor));// ft.getFG());
            textPaint.setAntiAlias(true);

            Layout.Alignment al = Layout.Alignment.ALIGN_CENTER;
            if (align == Paint.Align.RIGHT)
                al = Layout.Alignment.ALIGN_OPPOSITE;
            else if (align == Paint.Align.LEFT)
                al = Layout.Alignment.ALIGN_NORMAL;
            //StaticLayout sl = new StaticLayout(data,textPaint,getWidth(), Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,true);
            StaticLayout sl = new StaticLayout(string, textPaint, rt.width(), al, 1.0f, 0.0f, true);

            int x = rt.left;
            int y = rt.top + (rt.height() - sl.getHeight()) / 2;
            g.translate(x, y);
            sl.draw(g);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        g.restore();
    }


    public void updateSettings(KDSSettings settings)
    {
        int ncols = settings.getInt(KDSSettings.ID.Queue_cols);
        this.setCols(ncols);
        ITEM_AVERAGE_HEIGHT = settings.getInt(KDSSettings.ID.Queue_panel_height);

        ORDERID_AREA_PERCENT= ((float) settings.getInt(KDSSettings.ID.Queue_panel_ratio_percent) /100);


        m_ftOrderID = settings.getKDSViewFontFace(KDSSettings.ID.Queue_order_ID_font);

        m_ftCustomerName = settings.getKDSViewFontFace(KDSSettings.ID.Queue_customer_name_font);
        m_ftOrderTimer = settings.getKDSViewFontFace(KDSSettings.ID.Queue_order_timer_font);


        m_ftStatusCustomMessage = settings.getKDSViewFontFace(KDSSettings.ID.Queue_custom_message_font);


        m_ftStatusReceived = settings.getKDSViewFontFace(KDSSettings.ID.Queue_order_received_font);
        m_ftStatusPreparation = settings.getKDSViewFontFace(KDSSettings.ID.Queue_order_preparation_font);
        m_ftStatusReady = settings.getKDSViewFontFace(KDSSettings.ID.Queue_order_ready_font);
        m_ftStatusPickup = settings.getKDSViewFontFace(KDSSettings.ID.Queue_order_pickup_font);
        m_bMoveReadyFront = settings.getBoolean(KDSSettings.ID.Queue_move_ready_to_front);

        m_bFlashReadyOrder = settings.getBoolean(KDSSettings.ID.Queue_flash_ready_order);
        m_strMoreOrders = settings.getString(KDSSettings.ID.Queue_more_orders_message);
        COLOR_VIEW_BG = settings.getInt(KDSSettings.ID.Queue_view_bg);

        m_statusReceived = settings.getString(KDSSettings.ID.Queue_received_status);
        m_statusPreparation = settings.getString(KDSSettings.ID.Queue_preparation_status);
        m_statusReady = settings.getString(KDSSettings.ID.Queue_ready_status);
        m_statusPickup = settings.getString(KDSSettings.ID.Queue_pickup_status);
        m_bShowFinishedRight = settings.getBoolean(KDSSettings.ID.Queue_show_finished_at_right);

        m_nViewMode = KDSSettings.QueueMode.values()[settings.getInt(KDSSettings.ID.Queue_mode)];
        m_bSimpleModeShowReceived = settings.getBoolean(KDSSettings.ID.Queue_simple_show_received_col);
        m_bSimpleModeShowPreparation = settings.getBoolean(KDSSettings.ID.Queue_simple_show_preparation_col);
        m_bSimpleModeShowReady = settings.getBoolean(KDSSettings.ID.Queue_simple_show_ready_col);
        m_bSimpleModeShowPickup = settings.getBoolean(KDSSettings.ID.Queue_simple_show_pickup_col);

        int n = KDSSettings.getEnumIndexValues(settings, QueueOrders.QueueStatus.class, KDSSettings.ID.Queue_combine_status1_to);
        m_receivedCombinedToStatus = QueueOrders.QueueStatus.values()[n];

        n = KDSSettings.getEnumIndexValues(settings, QueueOrders.QueueStatus.class, KDSSettings.ID.Queue_combine_status2_to);
        m_preparationCombinedToStatus = QueueOrders.QueueStatus.values()[n];

        n = KDSSettings.getEnumIndexValues(settings, QueueOrders.QueueStatus.class, KDSSettings.ID.Queue_combine_status3_to);
        m_readyCombinedToStatus = QueueOrders.QueueStatus.values()[n];

        n = KDSSettings.getEnumIndexValues(settings, QueueOrders.QueueStatus.class, KDSSettings.ID.Queue_combine_status4_to);
        m_pickupCombinedToStatus = QueueOrders.QueueStatus.values()[n];



        PAGE_TIMEOUT = settings.getInt(KDSSettings.ID.Queue_auto_switch_duration) * 1000;
        if (PAGE_TIMEOUT<=0)
            PAGE_TIMEOUT = DEFAULT_SWITCH_PAGE_TIMEOUT_MS;


        //2.0.35
        n = settings.getInt(KDSSettings.ID.Queue_status1_sort_mode);
        QueueOrders.QueueSort queueSort = QueueOrders.QueueSort.values()[n];
        m_status1Sort = queueSort;

        n = settings.getInt(KDSSettings.ID.Queue_status2_sort_mode);
        queueSort = QueueOrders.QueueSort.values()[n];
        m_status2Sort = queueSort;

        n = settings.getInt(KDSSettings.ID.Queue_status3_sort_mode);
        queueSort = QueueOrders.QueueSort.values()[n];
        m_status3Sort = queueSort;

        n = settings.getInt(KDSSettings.ID.Queue_status4_sort_mode);
        queueSort = QueueOrders.QueueSort.values()[n];
        m_status4Sort = queueSort;

        //for combine status
        if (!m_bSimpleModeShowReceived)
        {
            m_status1Sort = getStatusSort( m_receivedCombinedToStatus);
        }
        if (!m_bSimpleModeShowPreparation)
        {
            m_status2Sort = getStatusSort( m_preparationCombinedToStatus);
        }
        if (!m_bSimpleModeShowReady)
        {
            m_status3Sort = getStatusSort( m_readyCombinedToStatus);
        }
        if (!m_bSimpleModeShowPickup)
        {
            m_status4Sort = getStatusSort( m_pickupCombinedToStatus);
        }

        //String s = settings.getString(KDSSettings.ID.Queue_auto_bump_timeout);
        //m_nAutoBumpTimeoutMs = KDSUtil.convertStringToInt(s, 0) *60000;
        n = settings.getInt(KDSSettings.ID.Auto_bump_minutes);
        m_nAutoBumpTimeoutMs = n * 60000;
        if (!settings.getBoolean(KDSSettings.ID.Auto_bump_enabled))
            m_nAutoBumpTimeoutMs = -1;


    }

    private QueueOrders.QueueSort getStatusSort( QueueOrders.QueueStatus status)
    {
        switch (status)
        {

            case Received:
                return m_status1Sort;

            case Preparation:
                return m_status2Sort;
            case Ready:
                return m_status3Sort;
            case Pickup:
                return m_status4Sort;
            default:
                return m_status1Sort;
        }

    }
    public boolean getExpoBumpEnabled()
    {
        return getSettings().getBoolean(KDSSettings.ID.Queue_double_bump_expo_order);
    }
    private KDSSettings getSettings()
    {
        return KDSGlobalVariables.getKDS().getSettings();
    }

    public void focusOrder(String orderGuid)
    {
        //remove focus
//        if (orderGuid.equals(KDSConst.RESET_ORDERS_LAYOUT))
//            return;
//        synchronized (m_locker) {
//            m_queueOrders.setFocusedOrderGuid(orderGuid);
//            if (isQueueExpo()) {//if multiple pages, move to focused order guest_paging.
//                if (!orderGuid.isEmpty()) {
//                    int nPage = checkFocusedOrderInWhichPage();
//                    if (nPage >= 0) {
//                        for  (int i=0;i< m_arPageCounter.size(); i++)
//                            m_arPageCounter.set(i, nPage);
//                        //m_nPageCounter = nPage;
//                    }
//                }
//            }
//        }
//        this.refresh();
    }

    private int checkFocusedOrderInWhichPage()
    {
        switch (m_nViewMode)
        {

            case Panels:
                return checkPanelModeFocusedOrderPage();

            case Simple:
                return checkSimpleModeFocusedOrderPage();

        }
        return -1;
    }

    private int checkPanelModeFocusedOrderPage()
    {
        Rect rect = this.getBounds();
        if (!m_bMoveReadyFront)
            return -1;
        else
            return checkReadyMoveFrontQueueFocusedOrderPage(rect);
        //return -1;
    }

    private int checkReadyMoveFrontQueueFocusedOrderPage(Rect rect) {
        KDSDataOrders queueOrders = m_queueOrders.getOrders();
        movePreparationFront(queueOrders);
        moveReadyFront(queueOrders);

        //split the rect
        Rect rtReady = new Rect(rect);
        // Rect rtPreparation = new Rect(rect);
        Rect rtNotReady = new Rect(rect);


        if (m_bShowFinishedRight) {
            rtReady.left = rtReady.right - (int) ((float) rect.width() * READY_RECT_PERCENT);
            rtNotReady.right = rtReady.left - 2;
        }
        else
        {
            rtReady.right = rtReady.left + (int) ((float) rect.width() * READY_RECT_PERCENT);
            rtNotReady.left = rtReady.right + 2;
        }


        ArrayList<Integer> arPages = new ArrayList<>();

        int nPage = checkReadyInMoveFrontModeFocusedOrderPage( rtReady, m_nCols);
        if (nPage >=0)
            return nPage;

        return checkNotReadyInMoveFrontModeFocusedOrderPage( rtNotReady);
        //return -1;
    }

    private int checkNotReadyInMoveFrontModeFocusedOrderPage( Rect rtNotReady)
    {
        Rect rtData = new Rect(rtNotReady);
        rtData.bottom -= PAGE_NUMBER_ROW_HEIGHT;
        int nRows = calculateRows(rtData,ITEM_AVERAGE_HEIGHT);
        KDSDataOrders queueOrders = m_queueOrders.getOrders();
        int ncount = queueOrders.getCount();
        int nPanelIndex = 0;
        int nTotalPanels = nRows * NOT_READY_COLS;
        ArrayList<QueueOrders.QueueStatus> status = new ArrayList<>();
        status.add(QueueOrders.QueueStatus.Received);
        //  status.add(QueueOrders.QueueStatus.Preparation);
        int nPagesCount = getPageCount(status, nRows, NOT_READY_COLS);
        int nPageIndex = 0;// getCurrentPageIndex(nPagesCount);
        int nItemsStartIndex = 0;//nPageIndex * nTotalPanels;
        //int nCurrentItemIndex = -1;

        for (int i=0; i< ncount; i++)
        {
            KDSDataOrder order = queueOrders.get(i);
            if (m_queueOrders.getStatus(order) == QueueOrders.QueueStatus.Received)

            {
//                nCurrentItemIndex ++;
//                if (nCurrentItemIndex < nItemsStartIndex) continue;
                if (order.getGUID().equals(getFocusedGuid()))
                    return nPageIndex;

                if (nPanelIndex>= nTotalPanels)
                {
                    nPageIndex ++;
                    nPanelIndex = -1;

                }

                nPanelIndex++;

            }
        }

        return -1;
    }

    private int checkReadyInMoveFrontModeFocusedOrderPage( Rect rtReady, int nMaxCols)
    {
        synchronized (m_locker) {
            Rect rtData = new Rect(rtReady);
            rtData.bottom -= PAGE_NUMBER_ROW_HEIGHT;
            int nRows = calculateRows(rtData, ITEM_AVERAGE_HEIGHT);
            KDSDataOrders queueOrders = m_queueOrders.getOrders();
            int ncount = queueOrders.getCount();
            int nPanelIndex = 0;
            int nTotalPanels = nRows * nMaxCols;

            ArrayList<QueueOrders.QueueStatus> status = new ArrayList<>();
            status.add(QueueOrders.QueueStatus.Pickup);
            status.add(QueueOrders.QueueStatus.Ready);
            status.add(QueueOrders.QueueStatus.Preparation);
            int nPagesCount = getPageCount(status, nRows, nMaxCols);
            int nPageIndex = 0;// getCurrentPageIndex(nPagesCount);
            int nItemsStartIndex = 0;// nPageIndex * nTotalPanels;

            //int nCurrentItemIndex = -1;

            for (int i = 0; i < ncount; i++) {
                KDSDataOrder order = queueOrders.get(i);
                if (isReadyOrder(order) || isPickupOrder(order) ||
                        isPreparationOrder(order)) {
                    if (order.getGUID().equals(getFocusedGuid()))
                        return nPageIndex;
                    //int nMoreOrders = 0;
                    if (nPanelIndex >= nTotalPanels) {
                        nPageIndex++;
                        nPanelIndex = -1;

                    }

                    nPanelIndex++;

                }
            }

            return -1;
        }

    }


    private int checkFocusedOrderPage(Rect rtData, ArrayList<QueueOrders.QueueStatus> status)
    {
        synchronized (m_locker) {
            int nRows = calculateRows(rtData, ITEM_AVERAGE_HEIGHT);
            KDSDataOrders queueOrders = m_queueOrders.getOrders();
            int ncount = queueOrders.getCount();
            int nPanelIndex = 0;
            int nTotalPanels = nRows * getCols();
            //for guest_paging
            //int nPagesCount = getPageCount(status, nRows, getCols());
            int nPageIndex = 0;//getCurrentPageIndex(nPagesCount);

            for (int i = 0; i < ncount; i++) {
                KDSDataOrder order = queueOrders.get(i);
                if (isEqualToAnyStatus(status, m_queueOrders.getStatus(order))) {
//                nItemCurrentIndex ++;
//                if (nItemCurrentIndex < nItemsStartIndex) continue;
                    if (order.getGUID().equals(getFocusedGuid()))
                        return nPageIndex;
                    if (nPanelIndex >= nTotalPanels) {
                        nPageIndex++;
                        nPanelIndex = -1;
                    }

                    //drawSimpleItem(g, rtData, nRows, getCols(), order, nPanelIndex);// m_items.get(i),WeekEvent.EVENT_COLOR_BG);
                    nPanelIndex++;

                }
            }
            return -1;
        }
    }

    private int checkSimpleModeFocusedOrderPage()
    {
        int nCount = 0;
        if (m_bSimpleModeShowReceived)
            nCount ++;
        if (m_bSimpleModeShowPreparation)
            nCount ++;
        if (m_bSimpleModeShowReady)
            nCount ++;
        if (m_bSimpleModeShowPickup)
            nCount ++;
        if (nCount == 0) {
            nCount = 1;
            m_bSimpleModeShowReceived = true;
        }

        Rect rect = this.getBounds();
        rect.top += SIMPLE_HEADER_HEIGHT +2;
        rect.bottom -= PAGE_NUMBER_ROW_HEIGHT;
        int nColWidth = rect.width()/nCount;
        int nGap = 0;
        nColWidth -= nGap;
        int nIndex = 0;
        if (m_bSimpleModeShowReceived) {

            Rect rt = new Rect(rect);
            rt.right = rt.left + nColWidth;
            int nPage = checkFocusedOrderPage( rt,getSimpleColStatus(QueueOrders.QueueStatus.Received));
            nIndex ++;
            if (nPage >=0) return nPage;
        }

        if (m_bSimpleModeShowPreparation) {
            Rect rt = new Rect(rect);
            rt.left = rt.left + (nColWidth+nGap) *nIndex;
            rt.right = rt.left + nColWidth;
            int nPage = checkFocusedOrderPage( rt, getSimpleColStatus(QueueOrders.QueueStatus.Preparation));
            nIndex ++;
            if (nPage >=0) return nPage;
        }
        if (m_bSimpleModeShowReady) {
            Rect rt = new Rect(rect);
            rt.left = rt.left + (nColWidth+nGap) *nIndex;
            rt.right = rt.left + nColWidth;
            int nPage = checkFocusedOrderPage( rt,getSimpleColStatus( QueueOrders.QueueStatus.Ready));
            nIndex ++;
            if (nPage >=0) return nPage;
        }
        if (m_bSimpleModeShowPickup) {
            Rect rt = new Rect(rect);
            rt.left = rt.left + (nColWidth+nGap) *nIndex;
            rt.right = rt.left + nColWidth;
            int nPage = checkFocusedOrderPage( rt, getSimpleColStatus(QueueOrders.QueueStatus.Pickup));
            nIndex++;
            if (nPage >=0) return nPage;
        }

        return -1;

    }

    public void focusNext()
    {

//        synchronized (m_locker) {
//            String s = m_queueOrders.getNextOrderGUID(m_queueOrders.getFocusedOrderGUID());
//
//            focusOrder(s);
//        }
    }
    public void focusPrev()
    {
//        synchronized (m_locker) {
//
//            String s = m_queueOrders.getPrevOrderGUID(m_queueOrders.getFocusedOrderGUID());
//            focusOrder(s);
//        }
    }

    public void focusFirst()
    {
//        synchronized (m_locker) {
//            String s = "";
//            if (m_queueOrders.getOrders() == null) return;
//            if (m_queueOrders.getOrders().getCount() > 0)
//                s = m_queueOrders.getOrders().get(0).getGUID();
//            focusOrder(s);
//        }
    }

    public String getFocusedGuid()
    {
        return "";
//        synchronized (m_locker) {
//            return m_queueOrders.getFocusedOrderGUID();
//        }
    }

    public String getFirstOrderGuid()
    {
        synchronized (m_locker) {
            if (!m_queueOrders.isReady()) return "";
            if (m_queueOrders.getCount() <= 0) return "";
            return m_queueOrders.getOrders().get(0).getGUID();
        }
    }

    public String getNextGuid(String guid)
    {
        synchronized (m_locker) {
            String s = m_queueOrders.getOrders().getNextOrderGUID(guid);
            return s;
        }
    }
    public void onTimer()
    {
        try {


            refreshTimer();
            checkPageCounter();
            //checkAutoBump(); //move it to "MainActivity.java -->startCheckingThread()
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
        }
    }
    public KDSDataOrders getOrders()
    {
        return m_queueOrders.getOrders();
    }

    private boolean isEqualToAnyStatus(ArrayList<QueueOrders.QueueStatus> statusArray, QueueOrders.QueueStatus status) {
        for (int i = 0; i < statusArray.size(); i++)
        {
            if (statusArray.get(i) == status)
                return true;
        }
        return false;

    }

    private int getStatusOrderCount(ArrayList<QueueOrders.QueueStatus> status)
    {
        synchronized (m_locker) {
            if (!m_queueOrders.isReady()) return 0;
            KDSDataOrders queueOrders = m_queueOrders.getOrders();
            int ncount = queueOrders.getCount();
            int ncounter = 0;
            for (int i = 0; i < ncount; i++) {
                KDSDataOrder order = queueOrders.get(i);
                if (order == null) continue;
                if (isEqualToAnyStatus(status, m_queueOrders.getStatus(order))) {
                    ncounter++;
                }
            }
            return ncounter;
        }
    }

    private int getPageCount(ArrayList<QueueOrders.QueueStatus> status, int nRows, int nCols)
    {
        int ncount = getStatusOrderCount(status);
        int nTotalPanels = nRows * nCols;//getCols();
        int n = ncount/nTotalPanels;
        if ( (ncount%nTotalPanels) >0)
            n ++;
        //Log.i(TAG, "pages count=" + n);
        return n;
    }

    private int getCurrentPageIndex(int nPagesCount, int nStatus)
    {
        if (nPagesCount <= 0)
            return 0;
        int counter = m_arPageCounter.get(nStatus);
        int n = (counter% nPagesCount);
        if (n == 0)
            m_arPageCounter.set(nStatus, 0); //reset it.
        return n;

//        int n =  (m_nPageCounter % nPagesCount);
//        Log.i(TAG, "pages counter =" + m_nPageCounter);
//        Log.i(TAG, "index pages count =" + nPagesCount);
//        Log.i(TAG, "-------pages index=" + n);
//        return n;
    }

    /**
     *
     * @param g
     * @param rect
     * @param status
     * @param arPages
     *  return this values
     */
    private void drawSimpleModeWithGiveOrderStatus2(Canvas g, Rect rect,ArrayList<QueueOrders.QueueStatus> status, ArrayList<Integer> arPages, boolean bReverseColorForFlush )
    {
        Rect rtData = new Rect(rect);
        //rtData.bottom -= PAGE_NUMBER_ROW_HEIGHT;
        synchronized (m_locker) {
            KDSDataOrders queueOrders = m_queueOrders.getOrders();
            int nRows = calculateRows(rtData,ITEM_AVERAGE_HEIGHT);
            int ncount = queueOrders.getCount();
            int nPanelIndex = 0;
            int nTotalPanels = nRows * getCols();
            //for page
            int nPagesCount = getPageCount(status, nRows, getCols());
            int nPageIndex = getCurrentPageIndex(nPagesCount, status.get(0).ordinal());
            int nItemsStartIndex = nPageIndex * nTotalPanels;

            int nItemCurrentIndex = -1;
            try {

                for (int i = 0; i < ncount; i++) {
                    KDSDataOrder order = queueOrders.get(i);
                    if (order == null) break;
                    if (isEqualToAnyStatus(status, m_queueOrders.getStatus(order))) {
                        nItemCurrentIndex++;
                        if (nItemCurrentIndex < nItemsStartIndex) continue;

                        if (nPanelIndex >= nTotalPanels) {
                            break;
                        }

                        drawSimpleItem(g, rtData, nRows, getCols(), order, nPanelIndex, bReverseColorForFlush);// m_items.get(i),WeekEvent.EVENT_COLOR_BG);
                        nPanelIndex++;

                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            arPages.clear();
            arPages.add(nPageIndex);
            arPages.add(nPagesCount);
        }
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
    public void drawSimpleItem(Canvas g,Rect rect, int nRows,int nCols,  KDSDataOrder order, int nPanelIndex, boolean bReverseReadyColor )
    {
        Rect rtCell = getItemRect(rect, nRows,nCols,nPanelIndex);
        if (rtCell.isEmpty()) return;
        m_queueOrders.setOrderCoordinate(order, rtCell);

        if (order == null) return;
        //       Rect rtClip = g.getClipBounds();
        g.save();
        try {


            //set the drawing area
            // rtCell.inset(BORDER_GAP, BORDER_GAP);
            rtCell = drawItemBorder(g, rtCell, order.getGUID().equals(m_queueOrders.getFocusedOrderGUID()));
            g.clipRect(rtCell);

            boolean bReverseReadyColorForFlash = false;
            if (m_bFlashReadyOrder) {
                if (isReadyOrder(order))//||
                //isPreparationOrder(order))
                {
                    bReverseReadyColorForFlash = bReverseReadyColor;
//                Calendar c = Calendar.getInstance();
//                int second = c.get(Calendar.SECOND);
//                if ( (second%2)==0)
//                    bReverseReadyColorForFlash = true;
                }
            }
            // Paint paintBG = new Paint();
            m_paintOrderTitle.setColor(getBG(m_ftOrderID, bReverseReadyColorForFlash)); //m_ftOrderID.getBG());
            //m_paintOrderTitle.setColor( getBG(m_ftOrderID, false));//bReverseReadyColorForFlash)); //m_ftOrderID.getBG());
            //m_paintOrderTitle.setAlpha(0);
            g.drawRect(rtCell, m_paintOrderTitle); //draw whole bg
            //m_paintOrderTitle.setAlpha(255);
            int nTitleAreaWidth = (int) ((float) rtCell.width() * ORDERID_AREA_PERCENT);

            Rect rtTitle = new Rect(rtCell);
            //rtTitle.right = rtTitle.left + nTitleAreaWidth;
            drawTitle(g, rtTitle, order, bReverseReadyColorForFlash);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //make sure "restore" was called
        g.restore();

    }


    final int SIMPLE_HEADER_HEIGHT = 35;
    final int PAGE_NUMBER_ROW_HEIGHT = 30;
    private void drawSimpleModeCol(Canvas canvas, Rect rect, ArrayList<QueueOrders.QueueStatus> status, boolean bReverseColorForFlush)
    {
        Rect rt = new Rect(rect);


        rt.bottom = rt.top + SIMPLE_HEADER_HEIGHT;
        drawStatus(canvas, rt, status.get(0), false);
        rt = new Rect(rect);
        rt.top += SIMPLE_HEADER_HEIGHT +2;
        rt.bottom -= PAGE_NUMBER_ROW_HEIGHT;

        ArrayList<Integer> arPages = new ArrayList<>();
        drawSimpleModeWithGiveOrderStatus2(canvas,rt, status, arPages ,bReverseColorForFlush);

        rt.top =rect.bottom - PAGE_NUMBER_ROW_HEIGHT+5;
        rt.bottom = rect.bottom;
        drawPages(canvas,rt, status.get(0), arPages);

        if (rt.left >=10) {

            Rect rtLine = new Rect(rect);
            rtLine.bottom += PAGE_NUMBER_ROW_HEIGHT;
            //KDSViewFontFace ft = getStatusFontFace(status.get(0));
            Paint pt = new Paint();
            int ncolor = getSettings().getInt(KDSSettings.ID.Queue_separator_color);
            pt.setColor(ncolor);
            pt.setStrokeWidth(3);
            canvas.drawLine(rtLine.left, rtLine.top, rtLine.left, rtLine.bottom, pt);
        }
    }


    private void checkPageCounter()
    {
        if (isQueueExpo())
        {
            if (!getFocusedGuid().isEmpty())
                return;
        }
        if (m_tdPage.is_timeout(PAGE_TIMEOUT)) {
            for (int i=0;i< m_arPageCounter.size(); i++)
            {
                m_arPageCounter.set(i, m_arPageCounter.get(i)+1);
                if (m_arPageCounter.get(i) > Integer.MAX_VALUE-1000)
                    m_arPageCounter.set(i, 0);
            }
            //m_nPageCounter++;
            m_tdPage.reset();
        }
//        if (m_nPageCounter >Integer.MAX_VALUE-1000)
//            m_nPageCounter = 0;
    }

    String m_strInputOrderID = "";

    /**
     * for queue-expo mode
     * @param keyCode
     * @param event
     * @param eventID
     */
    public void onKeyPressed(int keyCode, KeyEvent event, KDSSettings.ID eventID)
    {
//        if (!isQueueExpo()) return;
//        if (eventID != KDSSettings.ID.NULL ) {
//            m_strInputOrderID = "";
//            refresh();
//            return;
//        }
//        if (keyCode >= KeyEvent.KEYCODE_0 &&
//                keyCode <= KeyEvent.KEYCODE_9)
//        {
//            int n = keyCode - KeyEvent.KEYCODE_0;
//            m_strInputOrderID += KDSUtil.convertIntToString(n);
//            refreshFocusAfterInputing();
//            return;
//        }
//        else
//        {
//            m_strInputOrderID = "";
//            refresh();
//        }

    }

    private void refreshFocusAfterInputing()
    {
//        String orderName = m_strInputOrderID;
//        String partialFitGuid = "";
//        int nMaxNameLength = 0;
//        synchronized (m_locker) {
//            for (int i = 0; i < m_queueOrders.getOrders().getCount(); i++) {
//                String dbOrderName = m_queueOrders.getOrders().get(i).getOrderName();
//                if (dbOrderName.length() > nMaxNameLength)
//                    nMaxNameLength = dbOrderName.length();
//                if (dbOrderName.equals(orderName)) {
//                    partialFitGuid = m_queueOrders.getOrders().get(i).getGUID();
//                    //this.focusOrder(m_queueOrders.getOrders().get(i).getGUID());
//                    //return;
//                    break;
//                } else if (dbOrderName.indexOf(orderName) == 0) {
//                    if (partialFitGuid.isEmpty())
//                        partialFitGuid = m_queueOrders.getOrders().get(i).getGUID();
//                }
//            }
//
//        }
//        this.focusOrder(partialFitGuid);
//        if (m_strInputOrderID.length() >= nMaxNameLength)
//            m_strInputOrderID = "";
//        if (partialFitGuid.isEmpty())
//            m_strInputOrderID = "";
//        refresh();

    }

    /**
     * auto bump order if order is in status=3 or 4.
     *
     */
    public void checkAutoBump()
    {
        checkAutoBump(QueueOrders.QueueStatus.Ready,QueueOrders.QueueStatus.Pickup , false);
        //remove it, We need to try to get this to work. Because this will cause many issues. Can you take a look at this.
        //We need to see if we can find a solution for this.
        //checkAutoBump(QueueOrders.QueueStatus.Received,QueueOrders.QueueStatus.Preparation , true);
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
                        int nOld = m_nRedrawRequestCounter;
                        try {
                            showOrdersWithoutUIRefresh();
                            refreshThroughMessage();
                        }
                        catch ( Exception e)
                        {
                            e.printStackTrace();
                        }
                        m_nRedrawRequestCounter -= nOld;
                        if (m_nRedrawRequestCounter<0)
                            m_nRedrawRequestCounter = 0;
                    }
                }
            });
            m_threadShowOrders.setName("QueueShowOrders");
            m_threadShowOrders.start();
        }
    }


    public void showOrdersWithoutUIRefresh()
    {

        synchronized (m_locker) {

            KDSDataOrders orders = m_queueOrders.getOrders();
            if (m_nViewMode == KDSSettings.QueueMode.Simple)
                m_queueOrders.sortByStateTime(m_status1Sort,m_status2Sort,m_status3Sort,m_status4Sort );

            if (m_bMoveReadyFront)
                moveReadyFront(orders);
            //remove focus
            m_queueOrders.setFocusedOrderGuid("");
//            if (m_queueOrders.getFocusedOrderGUID().isEmpty()) {
//                if (orders.getCount() > 0) {
//                    if (!isQueueExpo())
//                        m_queueOrders.setFocusedOrderGuid(orders.getFirstOrderGuid());
//                }
//            }
        }
        //refresh();
    }

    public void refreshThroughMessage()
    {
        Message m = new Message();
        m.what = 0;
        m_refreshHandler.sendMessage(m);

    }



//    private void checkAutoBumpForReceivedState()
//    {
//        checkAutoBump(QueueOrders.QueueStatus.Received,QueueOrders.QueueStatus.Preparation );
//
//
//    }

    /**
     * check two status
     * @param status0
     * @param status1
     */
    public void checkAutoBump(QueueOrders.QueueStatus status0, QueueOrders.QueueStatus status1, boolean doubleTime)
    {
        if (m_nAutoBumpTimeoutMs <=0) return;

        try {

            KDSDataOrders queueOrders =  m_queueOrders.getOrders();
            int ncount = queueOrders.getCount();
            long dtNow = System.currentTimeMillis();

            ArrayList<KDSDataOrder> ar = new ArrayList<>();
            ArrayList<QueueOrders.QueueStatus> arReadyStatus = getSimpleColStatus(status0);
            ArrayList<QueueOrders.QueueStatus> arPickupStatus = getSimpleColStatus(status1);
            arReadyStatus.addAll(arPickupStatus);

            for (int i = 0; i < ncount; i++) {
                if (i >= queueOrders.getCount())
                    break;
                try {


                    KDSDataOrder order = queueOrders.get(i);
                    if (order == null) break;
                    QueueOrders.QueueStatus status = m_queueOrders.getStatus(order);
                    if (m_nViewMode == KDSSettings.QueueMode.Panels) {
                        if (status != status0 &&
                                status != status1)
                            continue;
                    } else if (m_nViewMode == KDSSettings.QueueMode.Simple) {
                        boolean bBumpIt = false;
                        for (int j = 0; j < arReadyStatus.size(); j++) {
                            if (arReadyStatus.get(j) == status) {
                                bBumpIt = true;
                                break;
                            }
                        }
                        if (!bBumpIt) continue;
                    }
                    Date dtStart = order.getQueueStateTime();//.getStartTime();
                    long nTimeout = m_nAutoBumpTimeoutMs;
                    if (doubleTime)
                    {
                        nTimeout *= 2;
                    }

                    if (dtNow - dtStart.getTime() > nTimeout) {
                        ar.add(order);
                    }

                }
                catch (Exception e)
                {
                    break;
                }
            }
            boolean bFromMe = false;
            if (ar.size()>0)
                bFromMe = KDSGlobalVariables.getKDS().getCurrentDB().startTransaction();
            for (int i = 0; i < ar.size(); i++) {
                //m_queueOrders.getOrders().removeComponent(ar.get(i));//move to end
                KDSGlobalVariables.getKDS().getCurrentDB().orderDelete(ar.get(i).getGUID());

            }
            if (ar.size()>0) {
                KDSGlobalVariables.getKDS().getCurrentDB().finishTransaction(bFromMe);
                synchronized (m_locker) {
                    //queueOrders.getComponents().removeAll(ar);//clear once
                    m_queueOrders.removeAll(ar); //kpp1-288. the queueOrders is one copy of data, so call this function to remove.
                }
                //if (ar.size() >0) {
                    //TimeDog td = new TimeDog();
                //KDSGlobalVariables.getKDS().getCurrentDB().clearExpiredBumpedOrders(KDSGlobalVariables.getKDS().getSettings().getBumpReservedCount());
                    //td.debug_print_Duration("checkAutoBumping->clearExpiredBumpedOrders");
                //}
                refresh();
                ar.clear();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    KDSUsers m_users = null;
    public void showOrders(KDSUsers users)
    {
        m_users = users;
        synchronized (m_locker) {
            m_queueOrders.setOrders(users);
            if (m_nViewMode == KDSSettings.QueueMode.Simple)
            {//move from ondraw to here.
                m_queueOrders.sortByStateTime(m_status1Sort, m_status2Sort, m_status3Sort, m_status4Sort);//2.0.36
            }
            m_nRedrawRequestCounter++;
        }
        startShowOrdersThread();
    }

    /**
     * kpp1-293
     * @return
     */
    private boolean isScreenEmpty()
    {
        return (this.m_queueOrders.getOrders().getCount() == 0 );
    }

    /**
     * kpp1-293
     * @param canvas
     */
    protected void drawScreenLogo(Canvas canvas)
    {
        ScreenLogoDraw.drawScreenLogo(this, this.getBounds(), canvas, getSettings(), isScreenEmpty(), PAGE_NUMBER_ROW_HEIGHT);
    }

}
