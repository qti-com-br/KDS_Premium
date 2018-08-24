package com.bematechus.kdsrouter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 *
 */
public class WeekEvtView extends View {

    public interface WeekEventViewEvents
    {
        void onScheduleAddNewItem(int nWeekDay, WeekEvent.FloatTime initTime);
        void onScheduleDeleteItem(WeekEvent item);
        void onScheduleClearAllItems();
        void onScheduleEditItem(WeekEvent item);
    }
    public final int COLOR_SCHEDULE_BG = Color.rgb(227, 239,255);
    public final int COLOR_SCHEDULE_DAY_LINE  = Color.rgb(112,139,167);

    public final int COLOR_SCHEDULE_HALF_HOUR_LINE  = Color.rgb(169,191,232);// Color.rgb(234,233,249);

    public final int COLOR_SCHEDULE_HOUR_DIVIDOR  = Color.rgb(103,147,210);
    public final int COLOR_SCHEDULE_HOUR_TEXT  = Color.rgb(92,151,193);

    static public final int HOUR_DEFAULT_HEIGHT = 40;

    final static int GAP_BAND_WIDTH = 6;
    final static double HALF_HOUR_DIVIDER = 0.7;


    ArrayList<WeekEvent> m_items = new ArrayList<>();
    WeekEvent m_focusedItem = null;

    int m_iHourHeight = HOUR_DEFAULT_HEIGHT;;
    //CWeekEvtCtrl* m_pCalenderObj;
    boolean m_bUseMilitaryTime = true;
    Date m_dtLastClickTime = new Date(); //record the last click what time.
    //


    GestureDetector m_gesture = null;//

    TextView m_menuAnchor = new TextView(this.getContext());
    WeekEventViewEvents m_receiver = null;

    public void setEventsReceiver(WeekEventViewEvents receiver)
    {
        m_receiver = receiver;
    }

    public WeekEvtView(Context context)
    {
        super(context);
        init_gesture();
        init();

    }

    public WeekEvtView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init_gesture();
        init();
    }

    public WeekEvtView(Context context, AttributeSet attrs, int defaultStyle)
    {
        super(context, attrs, defaultStyle);
        init_gesture();
        init();
    }


    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            // Log.d(DEBUG_TAG,"onDown: " + event.toString());
            WeekEvtView.this.onTouchDown(event);
            return true;
        }
        public void onLongPress(MotionEvent e) {
            WeekEvtView.this.onLongTouchDown(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            WeekEvtView.this.onDoubleClick(e);
            return true;
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event)
    {

        if(m_gesture.onTouchEvent(event))
        {
            event.setAction(MotionEvent.ACTION_CANCEL);
        }
        return super.dispatchTouchEvent(event);
    }

    private void init_gesture()
    {
        m_gesture = new GestureDetector(this.getContext(), new MyGestureListener());
    }


    public void hideMenu()
    {
        if (m_popupMenu != null)
        {
            m_popupMenu.dismiss();
            m_popupMenu = null;
        }
    }
    static final int DEFAULT_MENU_ITEM_HEIGHT = 40;
    static final int DEFAULT_MENU_WIDTH = 150;
    private LinearLayout createMenuLayout()
    {
        LinearLayout l = new LinearLayout(this.getContext());
        l.setOrientation(LinearLayout.VERTICAL);
        l.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        l.setBackgroundColor(Color.GRAY);
        return l;
    }
    private TextView addMenuItem(LinearLayout l, String strText)
    {
        TextView t =  new TextView(this.getContext());
        t.setTextColor(Color.WHITE);
        t.setText(strText);// this.getContext().getString(R.string.edit));//"Edit");
        t.setWidth(DEFAULT_MENU_WIDTH);
        t.setHeight(DEFAULT_MENU_ITEM_HEIGHT);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(3, 0,0,0);
        l.addView(t);
        return t;
    }

    private TextView addMenuSeparator(LinearLayout l)
    {
        TextView v = new TextView(this.getContext());
        v.setText("");
        v.setHeight(1);
        v.setPadding(2,0,0,2);
        v.setBackgroundColor(Color.WHITE);
        l.addView(v);
        return v;
    }
    public void showItemMenu(Point pt)
    {
        LinearLayout l = createMenuLayout();
        TextView t = addMenuItem(l,this.getContext().getString(R.string.edit) );
        t.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMenu();
                onEdit();
            }
        });

        addMenuSeparator(l);

        TextView t1 = addMenuItem(l,this.getContext().getString(R.string.delete) );
        t1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMenu();
                onDelete();
            }
        });
        showMenu(l,pt);

    }
    private void showMenu(LinearLayout l, Point pt)
    {
        m_popupMenu = new PopupWindow(l, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        m_popupMenu.setFocusable(true);

        m_popupMenu.setOutsideTouchable(true);
        m_popupMenu.setBackgroundDrawable(new BitmapDrawable());


        m_popupMenu.update();
        int[] location = new int[2];
        this.getLocationOnScreen(location);

        m_popupMenu.showAtLocation(this, Gravity.NO_GRAVITY, pt.x+location[0],pt.y+location[1]);


    }

    public void onClearAll()
    {
        if (m_receiver != null)
            m_receiver.onScheduleClearAllItems();
    }
    public void onAddNew()
    {
        int nWeekDay = getWeekDayFromPoint(m_lastClickedPoint);
        WeekEvent.FloatTime tm = getTimeFromPoint(m_lastClickedPoint);
        if (m_receiver != null)
            m_receiver.onScheduleAddNewItem(nWeekDay, tm);

    }

    public void onEdit()
    {

        if (m_receiver != null)
            m_receiver.onScheduleEditItem(m_lastClickedItem);
    }
    public void onDelete()
    {
        if (m_receiver != null)
            m_receiver.onScheduleDeleteItem(m_lastClickedItem);
    }
    PopupWindow m_popupMenu = null;
    Point m_ptShowMenu = null;
    public void showViewMenu(Point pt)
    {
        m_ptShowMenu = pt;

        LinearLayout l = createMenuLayout();
        TextView t = addMenuItem(l, this.getContext().getString(R.string.add));
        t.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMenu();
                onAddNew();
            }
        });

        addMenuSeparator(l);

        TextView t1 = addMenuItem(l, this.getContext().getString(R.string.clear));
        t1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMenu();
                onClearAll();
            }
        });

        showMenu(l, pt);
    }

    public void onTouchDown(MotionEvent e)
    {
        m_lastClickedItem = null;
        m_lastClickedPoint = new Point((int)e.getX(), (int)e.getY());

        hideMenu();

    }

    WeekEvent m_lastClickedItem = null;
    Point m_lastClickedPoint = null;
    public void onLongTouchDown(MotionEvent e)
    {

        m_lastClickedItem = null;

        Point pt = new Point( (int)e.getX(),(int) e.getY());//
        m_lastClickedPoint = pt;
        m_dtLastClickTime = new Date();
        WeekEvent pressedItem = HitTest(pt);
        if (pressedItem!= null)
        {
            m_lastClickedItem = pressedItem;
            showItemMenu(pt);
        }
        else
        {
            showViewMenu(pt);
        }



    }

    public void onDoubleClick(MotionEvent e)
    {

    }

    private void init()
    {
        m_bUseMilitaryTime = true;
        m_iHourHeight    = HOUR_DEFAULT_HEIGHT; //hour text row height
        m_menuAnchor.setText("");

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
        int result = 500;
        if (specMode == MeasureSpec.AT_MOST)
        {
            result = (24 * m_iHourHeight)+1;
        }
        else if (specMode == MeasureSpec.EXACTLY)
        {
            result = (24 * m_iHourHeight)+1;

        }
        return result;


    }

    protected int measureWidth(int widthMeasureSpec)
    {
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        int result = 500;
        if (specMode == MeasureSpec.AT_MOST)
        {
            result = specSize;//
        }
        else if (specMode == MeasureSpec.EXACTLY)
        {
            result = specSize;//

        }
        return result;
    }

    public void add(WeekEvent evtItem)
    {
        if (evtItem == null) return ;
        m_items.add(evtItem);
        refresh();
    }

    public void clearAll()
    {
        m_items.clear();
        refresh();
    }

    public void delete(WeekEvent item)
    {
        m_items.remove(item);
        refresh();
    }
    public void setItems(ArrayList<WeekEvent> ar)
    {
        m_items = ar;
    }
    public ArrayList<WeekEvent> getItems()
    {
        return m_items;
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



    int m_nLeftTitleColWidth = 0;
    protected void drawBackground(Canvas g)
    {
        Rect rcClient = getBounds();
        g.drawColor(Color.WHITE);

        int iLeftTitleColWidth = (int)((float)m_iHourHeight*1.5); //the hour text col width
        m_nLeftTitleColWidth = iLeftTitleColWidth; //save for global uses
        //set the hours column rect
        Rect rcLeftTitleCol = new Rect(0,0,0,0);

        rcLeftTitleCol.bottom = rcClient.height();
        rcLeftTitleCol.right = iLeftTitleColWidth;

        //---------------------------------------------------------------------------
        //  fill hours column
        //---------------------------------------------------------------------------
        Paint p = new Paint();
        p.setColor(COLOR_SCHEDULE_BG);

        g.drawRect(rcLeftTitleCol, p);

        //---------------------------------------------------------------------------
        // the minimum appointment area size
        //---------------------------------------------------------------------------
        Rect rcApptArea = rcLeftTitleCol;
        rcApptArea.left = rcApptArea.right;
        rcApptArea.right = rcClient.right+1;
        rcApptArea.bottom ++;

        //---------------------------------------------------------------------------
        // Draw all hours separates + half hour separators
        //---------------------------------------------------------------------------// use thin pen

        draw_hours_lines(g, rcApptArea, m_iHourHeight,iLeftTitleColWidth);
    }
    @Override
    protected void onDraw(Canvas canvas)
    {
        Canvas g = get_double_buffer();
        drawBackground(g);
        int ncount = m_items.size();
        for (int i=0; i< ncount; i++)
        {
            //drawItem(g, m_items.get(i),WeekEvent.EVENT_COLOR_BG);
            calculateItemRect(g, m_items.get(i));
        }
        for (int i=0; i< ncount; i++)
        {
            drawItem(g, m_items.get(i),WeekEvent.EVENT_COLOR_BG);
        }
        //drawOverlappingArea(g);
        commit_double_buffer(canvas);

    }


    ArrayList<WeekEvent> findOverlapItems(WeekEvent evt)
    {
        ArrayList<WeekEvent> arOverlapped = new ArrayList<>();
        arOverlapped.add(evt);
        for (int i=0; i< m_items.size(); i++)
        {
            if (m_items.get(i).isOverLapping(evt))
            {
                arOverlapped.add(m_items.get(i));
            }
        }

        if (arOverlapped.size() >1)
        {
            Collections.sort(arOverlapped, new Comparator() {

                @Override

                public int compare(Object o1, Object o2) {

                    WeekEvent evt1 = (WeekEvent)o1;
                    WeekEvent evt2 = (WeekEvent)o2;

                    Float flt1 =  evt1.getTimeFrom().get();
                    Float flt2 =  evt2.getTimeFrom().get();
                    return flt1.compareTo(flt2);
                }
            });
        }

       return arOverlapped;
    }

    static public  int INTFRAC(int i, float fraction)
    {
        return (int)((float)i*fraction);
    }


    int getWeekDayFromPoint(Point pt)
    {
        int nLeft = m_nLeftTitleColWidth;
        for (int i=0; i< 7; i++) {
            Rect rc = this.get_week_day_rect(i);
            if (rc.contains(pt.x, pt.y))
                return i;
        }
        return -1;
    }
    WeekEvent.FloatTime getTimeFromPoint(Point pt)
    {
        int y = pt.y;
        int h = this.getBounds().height();
        float f = (float)y/(float) h;
        WeekEvent.FloatTime t = new WeekEvent.FloatTime(f);
        return t;
    }
    WeekEvent HitTest(Point point)
    {

        for (int i=0; i< m_items.size(); i++)
        {
            if (m_items.get(i).getDrawRect().contains(point.x, point.y))
                return m_items.get(i);
        }
        return null;
    }

    int getMinimumHeight(Canvas g)
    {

        Paint paint = new Paint();
        Paint.FontMetrics fr = paint.getFontMetrics();
        return (int) Math.ceil(fr.descent - fr.top) + 2;  //ceil() 函数向上舍入为最接近的整数。


    }
    void draw_hours_lines(Canvas g, Rect rcEdge, int nHourHeight, int nLeftTitleColWidth)
    {
        //---------------------------------------------------------------------------
        // Draw all hours separates + half hour separators
        //---------------------------------------------------------------------------// use thin pen

        int iLeftTitleWidth =nLeftTitleColWidth;// CUtil::INTFRAC(nHourHeight,1.5); //the hour text col width
        int iHalfHour = nHourHeight/2;
        int iPos=0;
        int iHalfHourOffset = (int)(((float)(iLeftTitleWidth) * HALF_HOUR_DIVIDER)/*-iLeftGutterBorder*/);

        Paint paintHour = new Paint();
        paintHour.setColor(COLOR_SCHEDULE_HOUR_DIVIDOR);

        Paint paintHalfHour = new Paint();
        paintHalfHour.setColor(COLOR_SCHEDULE_HALF_HOUR_LINE);


        Paint paintHourText = new Paint();
        paintHourText.setColor(COLOR_SCHEDULE_HOUR_TEXT);
        paintHourText.setTextSize(15);

        Paint paintHourSmallText = new Paint();
        paintHourSmallText.setColor(COLOR_SCHEDULE_HOUR_TEXT);
        paintHourSmallText.setTextSize(10);


        Paint paintHalfHourBar = new Paint();
        paintHalfHourBar.setColor(COLOR_SCHEDULE_BG);

        int nTextHeight = getMinimumHeight(g);
        //draw time lines in time hours col
        for (int iIndex=0 ; iIndex<24*2 ; iIndex++)
        {
            iPos = ((iIndex*nHourHeight)/2);
            int iTextTop = iPos+2;
            if ((iIndex%2)==0)
            {// whole hours

                g.drawLine(0, iPos,iLeftTitleWidth, iPos , paintHour);

                String strTime = "";
                if (m_bUseMilitaryTime)
                {
                    if (iIndex/2<=9)
                        strTime = String.format("0%d", iIndex/2);
                    else
                        strTime = String.format("%d", iIndex/2);
                }
                else
                {
                    int iTime=iIndex/2;

                    if (iTime<=12)
                    {
                        strTime = String.format("%d", iTime);
                    }
                    else
                    {
                        strTime = String.format("%d", iTime-12);
                    }
                }

                Rect rcTxt = new Rect(0,iTextTop,iHalfHourOffset - 3,iPos+nHourHeight);
                g.drawText(strTime, rcTxt.left, rcTxt.top+nTextHeight*3/2, paintHourText);
                rcTxt.left = rcTxt.right;
                rcTxt.right = iLeftTitleWidth -2;
                if (m_bUseMilitaryTime)
                    g.drawText("00",rcTxt.left, rcTxt.top+nTextHeight, paintHourSmallText );

                else
                {
                    int iTime=iIndex/2;
                    if (iTime == 12)
                    {
                        g.drawText("pm",rcTxt.left, rcTxt.top+nTextHeight, paintHourSmallText );

                    }
                    else
                        g.drawText("00",rcTxt.left, rcTxt.top+nTextHeight, paintHourSmallText );

                }
                g.drawRect(iLeftTitleWidth, iPos, rcEdge.right, iPos +nHourHeight/2, paintHalfHourBar);
                g.drawLine(iLeftTitleWidth, iPos, rcEdge.right,iPos, paintHalfHour);

            }
            else
            {
                g.drawLine(iHalfHourOffset, iPos,iLeftTitleWidth, iPos, paintHalfHour );
                g.drawLine(iHalfHourOffset, iPos,rcEdge.right, iPos, paintHalfHour );

            }
        }
        draw_week_day_lines(g,iLeftTitleWidth, rcEdge);
    }

    void draw_week_day_lines(Canvas g, int nLeftTitleColWidth, Rect rcEdge)
    {


        int iLeftGutterWidth =nLeftTitleColWidth;// CUtil::INTFRAC(m_iHourHeight,1.5); //the hour text col width
        Paint paint = new Paint();
        paint.setColor(COLOR_SCHEDULE_DAY_LINE);

        paint.setStrokeWidth(2);

        for (int i=0; i< 7; i++)
        {
            Rect rcDay = get_week_day_rect(i);
            g.drawLine(rcDay.left, 0,rcDay.left, rcDay.bottom, paint);
        }
    }

    /**
     *
     * nWeekday: base is 0
     * */
    public Rect get_week_day_rect(int nWeekDay)
    {
        return get_week_day_rect(nWeekDay, this.getBounds(), m_nLeftTitleColWidth);

    }

    static public void drawTextCenter(Canvas g, Rect rc, String text, Paint paint)
    {

        Paint.FontMetrics fr = paint.getFontMetrics();
        int nHeight =  (int) Math.ceil(fr.descent - fr.top) ;  //ceil() 函数向上舍入为最接近的整数。
        int nWidth =(int) paint.measureText(text)+1;

        int x = rc.left + (rc.width() - nWidth)/2;
        int y = rc.bottom- (rc.height()-nHeight)/2-3;
        g.drawText(text, x, y, paint);

    }

    /**
     *
     * nWeekday: base is 0
     * */
    static public Rect get_week_day_rect(int nWeekDay, Rect rcBounds, int nLeftTitleColWidth)
    {
        Rect rc =rcBounds;// this.getBounds();//  m_pCalenderObj->GetHeader()->GetWeekDayColRect(nWeekDay);
        rc.left += nLeftTitleColWidth;

        rc.top = 0;
        int nWidth = rc.width();
        int average = nWidth / 7;
        int nleft = nWidth%7;

        int[] arWidths = new int[7];
        for (int i=0; i< 7 ; i++) {
            if (i < nleft) {
                nWidth = average + 1;
            } else {
                nWidth = average;
            }
            arWidths[i] = nWidth;
        }
        int nBeforeDayWidth = 0;
        for (int i=0; i< nWeekDay; i++)
        {
            nBeforeDayWidth += arWidths[i];
        }
        rc.left =nLeftTitleColWidth+ nBeforeDayWidth;

        rc.right = rc.left + arWidths[nWeekDay];//nWidth;

        return rc;
    }

    void SetHourHeight(int nHeight)
    {
        m_iHourHeight = nHeight;
    }
    int getHourHeight()
    {
        return m_iHourHeight;
    }

    private int findIndexInArray(ArrayList<WeekEvent> ar, WeekEvent evt)
    {
        for (int i=0;i< ar.size(); i++)
        {
            if (ar.get(i) == evt)
                return i;
        }
        return 0;
    }

    public void drawItem(Canvas g, WeekEvent item, int nEventBG)
    {

        //calculate the height
        int iFullHeight = getHourHeight()*24;
        float from =item.getTimeFrom().get();// transformDateToTime(m_From);
        int iStart = INTFRAC(iFullHeight, from);

        //int iEnd = INTFRAC(iFullHeight, m_To);
        int iEnd = iStart+  WeekEvtView.INTFRAC(iFullHeight, item.getEndTime().get() -item.getEndTime().get());

        //ver4.0
        int nMiniH = getMinimumHeight(g);
        Rect rc =item.calculatDrawRectTopBottom(nMiniH, iFullHeight);



        //get week day width
        Rect rt = get_week_day_rect( item.getWeekDay());
        rc.left = rt.left;
        rc.right = rt.right;


        int noverlappedIndex = 0;
        ArrayList<WeekEvent> arOverlapped = findOverlapItems(item);
        noverlappedIndex = findIndexInArray(arOverlapped, item);

        int iOverlapped= arOverlapped.size();// 0;// FindOverlapCount(&noverlappedIndex);

        int iFrac = 0;
        if (iOverlapped>1)
        {//it count myself
            iFrac = rc.width()/iOverlapped;
            if (noverlappedIndex >=0)
            {
                rc.left = rc.left + iFrac * noverlappedIndex;
                rc.right = rc.left + iFrac-1;
            }
        }

        //---------------------------------------------------------------------------
        // Draw the shade of the box
        //---------------------------------------------------------------------------
        Rect rcBox = new Rect(rc);
        //bg brush, and font
        int clrInfoBG = nEventBG;// WeekEvent.EVENT_COLOR_BG;
        int clrInfoFG = Color.rgb(0,0,0);
        //draw content box
        Paint paint = new Paint();
        paint.setColor(clrInfoBG);
        g.drawRect(rcBox, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(clrInfoFG);
        g.drawRect(rcBox, paint);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        //---------------------------------------------------------------------------
        // Draw text
        //---------------------------------------------------------------------------

        item.m_rcDraw = rc;

        Rect rcTxt = new Rect(rc);
        rcTxt.left += GAP_BAND_WIDTH;
        paint.setColor(clrInfoFG);
        Paint.FontMetrics fr = paint.getFontMetrics();
        int noffset = (int) Math.ceil(fr.descent - fr.top) ;  //ceil() 函数向上舍入为最接近的整数。

        g.drawText(item.getSubject(), rcTxt.left+5, rcTxt.top+noffset, paint);
        //---------------------------------------------------------------------------
        // Draw the title bar
        //---------------------------------------------------------------------------
        Rect rcTitle = new Rect(rc);
        rcTitle.right = rcTitle.left + GAP_BAND_WIDTH + 1;
        //-----------------------------------
        //if the duration too small, draw a green rect in title
        //-----------------------------------
        int nDurationH = item.getDurationHeight(iFullHeight, item.getDurationFloatTime());
        if (nDurationH < rc.height())//ver4.0
        {//draw it
            rt = new Rect(rcTitle);
            rt.top = iStart;
            int nMinDuration = 3;
            rt.bottom = iStart+ (nDurationH>nMinDuration?nDurationH:nMinDuration);
            if (rt.bottom > iFullHeight)
            {
                rt.top -= (rt.bottom - iFullHeight);
                rt.bottom -= (rt.bottom - iFullHeight);
            }
            rcTitle = rt;
        }


        Paint paintTitle = new Paint();
        paintTitle.setColor(Color.BLUE);
        g.drawRect(rcTitle, paintTitle);

    }

    public void calculateItemRect(Canvas g, WeekEvent item)
    {
        //calculate the height
        int iFullHeight = getHourHeight()*24;
        float from =item.getTimeFrom().get();// transformDateToTime(m_From);
        int iStart = INTFRAC(iFullHeight, from);
        //int iEnd = INTFRAC(iFullHeight, m_To);
        int iEnd = iStart+  WeekEvtView.INTFRAC(iFullHeight, item.getEndTime().get() -item.getEndTime().get());
        //ver4.0
        int nMiniH = getMinimumHeight(g);
        Rect rc =item.calculatDrawRectTopBottom(nMiniH, iFullHeight);

        //get week day width
        Rect rt = get_week_day_rect( item.getWeekDay());
        rc.left = rt.left;
        rc.right = rt.right;


        int noverlappedIndex = 0;
        ArrayList<WeekEvent> arOverlapped = findOverlapItems(item);
        noverlappedIndex = findIndexInArray(arOverlapped, item);

        int iOverlapped= arOverlapped.size();// 0;// FindOverlapCount(&noverlappedIndex);

        int iFrac = 0;
        if (iOverlapped>1)
        {//it count myself
            iFrac = rc.width()/iOverlapped;
            if (noverlappedIndex >=0)
            {
                rc.left = rc.left + iFrac * noverlappedIndex;
                rc.right = rc.left + iFrac-1;
            }
        }

        item.m_rcDraw = rc;

    }


    public void checkMoreOrLessInViewPort(int nScrollY,int nHeight, ArrayList<Integer> arMoreDay,ArrayList<Integer> arLessDay)
    {
        Point pt = new Point(0, nScrollY);
        WeekEvent.FloatTime tm = getTimeFromPoint(pt);

        Point ptEnd = new Point(0, nScrollY + nHeight);
        WeekEvent.FloatTime tmEnd = getTimeFromPoint(ptEnd);

        for (int i=0; i< m_items.size(); i++)
        {
            WeekEvent evt =m_items.get(i);
            if (evt.getTimeFrom().get() >=tmEnd.get())
                arMoreDay.add(evt.getWeekDay());
            //if (evt.getEndTime().get() <= tm.get())
            if (evt.getTimeFrom().get() < tm.get())
                arLessDay.add(evt.getWeekDay());
        }


    }
}
