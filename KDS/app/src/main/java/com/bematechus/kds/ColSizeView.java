package com.bematechus.kds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

/**
 * For line items feature settings.
 *  It will show each line items column size. and user can change its size directly.
 */
public class ColSizeView extends View {

    static final String TAG = "ColSizeView";
    int m_nBkColor = Color.WHITE;

    public interface ColsSizeViewEvents
    {
        void onColsSizeViewDataChanged();
        void onColsSizeViewTouchDown();
        void onColsSizeViewTouchUp();

    }

    public enum SizeDrawMode
    {
        Demo,
        Change,
    }
    int MIN_WIDTH = 8;
    final int MAX_COLS = 6;

    ColsSizeViewEvents m_eventsReceiver = null;



    SizeDrawMode m_drawMode = SizeDrawMode.Change;

    ColBlocks m_colBlocks = new ColBlocks();



    public void setBkColor(int nColor)
    {
        m_nBkColor = nColor;
    }

    public void setSizeDrawMode(SizeDrawMode mode)
    {
        m_drawMode = mode;
        this.invalidate();
    }

    public void setEventsReceiver(ColsSizeViewEvents receiver)
    {
        m_eventsReceiver = receiver;
    }

    public ColSizeView(Context context)
    {
        super(context);
        init();

    }

    public ColSizeView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public ColSizeView(Context context, AttributeSet attrs, int defaultStyle)
    {
        super(context, attrs, defaultStyle);
        init();
    }

    public Rect getColsHandRect()
    {
        Rect rt = new Rect(getBounds());
        rt.bottom = rt.top + HAND_SIZE;
        return rt;
    }

    private void init()
    {

        m_colBlocks.addColByPercent(25);
        m_colBlocks.addColByPercent(25);
        m_colBlocks.addColByPercent(25);
        m_colBlocks.addColByPercent(25);
        m_colBlocks.calculateColsAfterPercentChanged();

    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (m_drawMode == SizeDrawMode.Demo)
            return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            {
                KDSLog.i(TAG, "ACTION_DOWN.............");
                 onTouchDown(event);
                if (m_eventsReceiver!= null)
                    m_eventsReceiver.onColsSizeViewTouchDown();
                return true;

            }
           // break;
            case MotionEvent.ACTION_MOVE:
                KDSLog.i(TAG, "ACTION_MOVE.............");
                onHandMove(event);
                return true;
                //break;
            case MotionEvent.ACTION_CANCEL:
                KDSLog.i(TAG, "ACTION_CANCEL.............");
                if (m_eventsReceiver!= null)
                    m_eventsReceiver.onColsSizeViewTouchUp();
                break;
            case MotionEvent.ACTION_UP: {

                KDSLog.i(TAG, "ACTION_DOWN.............");
                onTouchUp(event);
                if (m_eventsReceiver!= null)
                    m_eventsReceiver.onColsSizeViewTouchUp();
                m_nTouchCol = -1;
                if (m_eventsReceiver != null)
                    m_eventsReceiver.onColsSizeViewDataChanged();
                return true;
            }
            //Log.d("TAG", "ACTION_UP.............");

            //break;
            default:

                break;
        }
        return true;

    }

    int m_nTouchCol = -1;

    public boolean onTouchUp(MotionEvent  event)
    {

        return true;
    }

    public boolean onTouchDown(MotionEvent event)
    {
        int nColHand = getTouchCol(event);
        m_nTouchCol = nColHand;
        if (m_nTouchCol == -1)
        {
            Rect rt = getColsHandRect();
            if (rt.contains((int)event.getX(),(int) event.getY()))
                addCol(event.getX());
        }
        return true;
    }


    private void addCol(float x)
    {
        if (m_colBlocks.getCount()>= MAX_COLS) return;
        m_colBlocks.addColByPosition(x);
        this.invalidate();
    }

    private int getTouchCol(MotionEvent event)
    {
        int x = (int)event.getX();
        int y = (int)event.getY();
        for (int i=0; i< m_colBlocks.getCount(); i++)
        {
            Rect rt = getColHandRect(getBounds(), i);
            if (rt.contains(x, y))
                return i;
        }
        return -1;
    }

    private boolean onHandMove(MotionEvent event)
    {
        if (m_nTouchCol <=0 ) return false;

        int x =(int) event.getX();

        boolean b = m_colBlocks.changeColX(m_nTouchCol, x);

        int y = (int)event.getY();

        KDSLog.d(TAG, KDSLog._FUNCLINE_() + String.format("y=%d",y));
        int deadLine = HAND_SIZE*2;
        if (y >0)
            deadLine = HAND_SIZE * 3;
        y  = Math.abs(y);

        if (y>deadLine)
        {
            removeCol(m_nTouchCol);
            m_nTouchCol = -1;
        }

        this.invalidate();
        if (!b)
        {
            m_nTouchCol = -1;
        }
        return b;

    }

    private void removeCol(int nCol)
    {
        m_colBlocks.removeCol(nCol);
    }

    public void refresh()
    {
        this.invalidate();
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

    @Override
    protected void onDraw(Canvas canvas)
    {
        m_colBlocks.setRect(getBounds());
        int n =Math.round( getBounds().width() * 2f/100f);
        MIN_WIDTH = n;
        onDrawCols(canvas);

    }

    private void onDrawCols(Canvas canvas)
    {
        if (canvas == null) return;
        Rect rect = this.getBounds();

        Canvas g = get_double_buffer();
        if (g == null) return;
        drawBackground(g);

        drawCols(g, rect);

        commit_double_buffer(canvas);
    }

    protected void drawBackground(Canvas g)
    {
        g.drawColor(m_nBkColor);

    }

    final int HAND_SIZE = 20;
    final int LINE_SIZE = 1;
    protected void drawCols(Canvas g, Rect rect)
    {
        Rect rtBody  = new Rect(rect);
        rtBody.right -= LINE_SIZE;
        rtBody.bottom -= LINE_SIZE;

        //CanvasDC.drawBox(g, rtBody,Color.BLACK, 1 );

        rtBody.top += HAND_SIZE;
        CanvasDC.drawBox(g, rtBody, Color.BLACK, 1 );
        for (int i=0; i< m_colBlocks.getCount(); i++)
        {

            drawCol(g, rtBody,m_colBlocks.get(i), i);//  getColX(rect, i), getColWidth(rect, i));
        }

    }

    protected int getColX(Rect rect, int nCol)
    {
        return m_colBlocks.get(nCol).m_nX;

    }

    protected Rect getColHandRect(Rect rectWhole, int nCol)
    {
        int x = getColX(rectWhole, nCol);
        Rect rt = new Rect();
        rt.left = x - HAND_SIZE/2;
        rt.top = rectWhole.top;
        rt.right = rt.left + HAND_SIZE;
        rt.bottom = rt.top + HAND_SIZE;
        return rt;
    }

    protected void drawCol(Canvas g, Rect rtBody, ColBlock block, int nIndex)
    {
        Paint pt = new Paint();
        pt.setColor(Color.BLACK);
        pt.setStrokeWidth(1);
        g.drawLine(block.m_nX, rtBody.top, block.m_nX, rtBody.bottom, pt);
        drawString(g, pt, block.getRect(rtBody), Paint.Align.CENTER, String.format("%d%%", (int)block.m_fltPercent));
        pt.setPathEffect(new DashPathEffect(new float[]{1, 2}, 0));
        Rect rt = block.getRect(rtBody);

        if (m_drawMode == SizeDrawMode.Change) {
            if (block.m_nX != rtBody.left) //don't draw first one
            {
                drawColHand(g, rtBody, block.m_nX, block.m_nWidth);
            }

        }
    }

    /**
     *      -
     *     \./
     *
     * @param g
     * @param rect
     * @param centerPoint
     * @param nWidth
     */
    private void drawColHand(Canvas g, Rect rect, int centerPoint, int nWidth)
    {
        Paint pt = new Paint();
        pt.setColor(Color.BLACK);
        pt.setStrokeWidth(1);
        pt.setAntiAlias(true);

        int bottomX = centerPoint;
        int bottomY = rect.top;// - HAND_SIZE;

        int topY = rect.top - HAND_SIZE+1;
        //draw \
        int x = bottomX;//centerPoint;
        int y = bottomY;// rect.top - HAND_SIZE;

        int xx = centerPoint - HAND_SIZE/2;
        int yy =topY;// rect.top;
        g.drawLine(xx, yy, x, y, pt);
        //draw -
        x = centerPoint + HAND_SIZE/2;
        y = topY;//rect.top;
        g.drawLine(xx, yy, x, y, pt);

        //draw /
        xx =bottomX;// centerPoint;
        yy =bottomY;// rect.top - HAND_SIZE;

        g.drawLine(xx, yy, x, y, pt);
    }


    private void drawString(Canvas g, Paint pt, Rect rt, Paint.Align align, String string )
    {
        Rect rtText = new Rect();
        pt.getTextBounds(string, 0,string.length(), rtText );
        pt.setAntiAlias(true);
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
     * comma splited string.
     * @param colsPercent
     */
    public void setColsPercentString(String colsPercent)
    {
        ArrayList<String> ar = spliteString(colsPercent, ",");
        m_colBlocks.clear();
        for (int i=0; i< ar.size(); i++)
        {
            m_colBlocks.addColByPercent(Float.parseFloat(ar.get(i)));
        }
        this.invalidate();
    }

    public String getColsPercentString()
    {
        String s = "";
        for (int i=0; i< m_colBlocks.getCount(); i++)
        {
            if (!s.isEmpty())
                s += ",";
            s += String.format("%d",(int) m_colBlocks.get(i).m_fltPercent);

        }
        return s;
    }
    static public ArrayList<String> spliteString(String s, String splitor) {
        String[] ar = s.split(splitor);
        //get there are how many splitor in string.
        int nstart = 0;
        int ncount = 0;

        while (true) {
            nstart = s.indexOf(splitor, nstart);
            if (nstart >= 0) {
                ncount++;
                nstart++;
            }
            else
                break;

        }



        ArrayList<String> arRet = new ArrayList<String>();

        for (int i = 0; i < ar.length; i++) {
            arRet.add(ar[i]);

        }
        //add more space to ncount
        ncount ++; //this string should contain those substrings
        if (arRet.size() < ncount)
        {
            int n = ncount - arRet.size();
            for (int i=0; i<  n ;i ++)
            {
                arRet.add("");
            }
        }
        return arRet;
    }

    /***********************************************************************************************/
    class ColBlock
    {
        int m_nX;
        int m_nWidth;
        float m_fltPercent;
        public Rect getRect(Rect rtColBody)
        {
            Rect rt = new Rect();
            rt.left = m_nX;
            rt.top = rtColBody.top;
            rt.right = rt.left + m_nWidth;
            rt.bottom = rtColBody.bottom;
            return rt;
        }
    }

    class ColBlocks
    {
        Rect m_rect = new Rect();

        ArrayList<ColBlock> m_arColBlock = new ArrayList<>();
        public void clear()
        {
            m_arColBlock.clear();
        }
        public void setRect(Rect rect)
        {
            m_rect = new Rect(rect);
            calculateColsAfterPercentChanged();
        }
        public int getCount()
        {
            return m_arColBlock.size();
        }
        public void addColByPercent(float nPercent)
        {

            ColBlock c = new ColBlock();
            c.m_fltPercent = nPercent;
            m_arColBlock.add(c);

        }

        public void addColByPosition(float x)
        {
            ColBlock colNew = new ColBlock();
            colNew.m_nX = (int)x;
            int nCurrentCol = -1;
            for (int i=0; i< getCount(); i++)
            {
                if (get(i).m_nX>x)
                    break;
                else
                    nCurrentCol = i;

            }
            m_arColBlock.add(nCurrentCol+1, colNew);


            ColBlock current = get(nCurrentCol);
            int nOldWidth = current.m_nWidth;
            current.m_nWidth =(int)( x - current.m_nX +1);
            colNew.m_nWidth = nOldWidth - current.m_nWidth;
            float oldPercent = current.m_fltPercent;
            current.m_fltPercent = (float)current.m_nWidth/(m_rect.width()+1)*100f;

            current.m_fltPercent = Math.round(current.m_fltPercent );
            colNew.m_fltPercent = oldPercent - current.m_fltPercent;

            //m_arColBlock.add(c);
        }

        public ColBlock get(int nIndex)
        {
            if (nIndex<0 || nIndex >=getCount())
                return null;
            return m_arColBlock.get(nIndex);
        }


        public void removeCol(int nCol)
        {
            ColBlock block = get(nCol);
            get(nCol-1).m_fltPercent += block.m_fltPercent;
            m_arColBlock.remove(nCol);
        }

        public boolean changeColX(int nCol, int xPosition)
        {
            if (nCol ==0) return false;
            if (nCol >getCount()-1) return false;
            int nOldX = get(nCol).m_nX;
            get(nCol).m_nX = xPosition;
            int nChanged =  nOldX - xPosition;

            int nOldWidth = get(nCol).m_nWidth;// += nChanged;
            if (nOldWidth + nChanged<= MIN_WIDTH ) {
                //removeCol(nCol);
                return true;
            }

            nOldWidth = get(nCol-1).m_nWidth;// += nChanged;
            if (nOldWidth - nChanged<= MIN_WIDTH )
                return true;
            get(nCol).m_nWidth += nChanged;
            get(nCol-1).m_nWidth -= nChanged;
            //update percent
            int nWidth = m_rect.width()+1;
            float nOldTotalPercent = get(nCol).m_fltPercent + get(nCol-1).m_fltPercent;


            float n =( (float)get(nCol).m_nWidth /nWidth *100);
            get(nCol).m_fltPercent = Math.round(n);

            get(nCol-1).m_fltPercent =nOldTotalPercent - get(nCol).m_fltPercent;

            calculateColsAfterPercentChanged();
            return  true;
        }

        private void calculateColsAfterPercentChanged()
        {
            int nWidth =  m_rect.width() +1;
            for (int i=0; i< getCount(); i++)
            {
                float flt = (float) nWidth * (float)get(i).m_fltPercent/100f;

                get(i).m_nWidth = Math.round(flt);
                if (i == getCount()-1)
                {
                    get(i).m_nWidth =nWidth - getWidthBeforeMe(i);
                }
                get(i).m_nX = getX(i);
            }
        }
        public int getWidthBeforeMe(int nCol)
        {
            int nTotal = 0;
            for (int i=0; i< nCol; i++)
            {
                nTotal +=  get(i).m_nWidth;


            }
            return nTotal;
        }
        public int getX(int nCol)
        {
            return m_rect.left + getWidthBeforeMe(nCol);
        }
    }
}
