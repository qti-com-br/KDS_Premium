package com.bematechus.kdsrouter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2016/11/11.
 */
public class WeekEvtHeader extends View {

    static final int  SCHEDULE_COLOR_HEADER_BG = Color.rgb(227, 239,255);
    static final int  SCHEDULE_COLOR_HEADER_TEXT_BG = Color.rgb(222,230,243);
    static final int  SCHEDULE_COLOR_HEADER_BAR_BG = Color.rgb(164,190,223);


    ArrayList<Integer> m_arMoreIcon = new ArrayList<>();
    ArrayList<Integer> m_arPrevIcon = new ArrayList<>();


    public WeekEvtHeader(Context context)
    {
        super(context);


    }

    public WeekEvtHeader(Context context, AttributeSet attrs)
    {
        super(context, attrs);


    }

    public WeekEvtHeader(Context context, AttributeSet attrs, int defaultStyle)
    {
        super(context, attrs, defaultStyle);


    }

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
        int result = WeekEvtView.HOUR_DEFAULT_HEIGHT;

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

    public Rect getBounds()
    {
        Rect rc = new Rect();

        this.getDrawingRect(rc);


        return rc;
    }
    int GetHourHeight()
    {
        return WeekEvtView.HOUR_DEFAULT_HEIGHT;
    }

    @Override
    protected void onDraw(Canvas g)
    {
        Rect rc = this.getBounds();
        g.drawColor(SCHEDULE_COLOR_HEADER_BG);

        int iLeftTitleColWidth = WeekEvtView.INTFRAC(GetHourHeight(),(float) 1.5);
        rc.left += iLeftTitleColWidth;
        ArrayList<String> arNames = new ArrayList<>();
        GetWeekDayNames(this.getContext(),arNames);
        ////////////////////
        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        int nCurrentWeekDay = c.get(Calendar.DAY_OF_WEEK)-1;

        Paint paintBG = new Paint();
        paintBG.setColor(SCHEDULE_COLOR_HEADER_BG);

        Paint paintFG = new Paint();
        paintFG.setColor(SCHEDULE_COLOR_HEADER_TEXT_BG);

        Paint paintBar = new Paint();
        paintBar.setColor(SCHEDULE_COLOR_HEADER_BAR_BG);

        Paint paintBarActive = new Paint();
        paintBarActive.setColor(Color.BLUE);

        Paint paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setAntiAlias(true);

        for (int i=0; i< arNames.size(); i++)
        {
            Rect rcWeek = GetWeekDayColRect( i);
            //draw border
            g.drawRect(rcWeek, paintFG); //for draw line
            rcWeek.inset(1,1);
            g.drawRect(rcWeek, paintBG); //for draw line

            //draw text
            Rect rcText = new Rect( rcWeek);
            rcText.bottom = rcText.bottom/2;
            WeekEvtView.drawTextCenter(g, rcText, arNames.get(i), paintText);

            //draw a bar under text
            Rect rcBar = new Rect(rcWeek);
            rcBar.top = rcText.bottom;
            paintBar.setAntiAlias(true);
            if (i == nCurrentWeekDay)
            {
                g.drawRect(rcBar, paintBarActive);

            }
            else
                g.drawRect(rcBar,paintBar);

            if (hasMore(i))
            {

                Bitmap bmp = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.down);
                Rect rcIcon = new Rect(rcBar);
                rcIcon.left = rcIcon.right - rcIcon.height();
                rcIcon.top ++;
                g.drawBitmap(bmp,rcIcon.left, rcIcon.top,paintBar);
            }
            if (hasPrev(i))
            {
                Bitmap bmp = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.up);
                Rect rcIcon = new Rect(rcBar);
                rcIcon.top ++;
                rcIcon.right = rcIcon.left + rcIcon.height();
                g.drawBitmap(bmp,rcIcon.left, rcIcon.top,paintBar);
               // g.drawBitmap(bmp,null,  rcIcon, paintBar);
            }

            //draw a line between text and bar
            g.drawLine(rcBar.left, rcBar.top,rcBar.right, rcBar.top, paintFG);

        }
        rc = this.getBounds();
        g.drawLine(rc.left, rc.bottom-1, rc.right, rc.bottom-1, paintFG);
    }




    static boolean GetWeekDayNames(Context context, ArrayList<String> arNames)
    {
        arNames.clear();

        arNames.add(context.getString(R.string.sun));
        arNames.add(context.getString(R.string.mon));
        arNames.add(context.getString(R.string.tue));
        arNames.add(context.getString(R.string.wed));
        arNames.add(context.getString(R.string.thu));
        arNames.add(context.getString(R.string.fri));
        arNames.add(context.getString(R.string.sat));
        return true;

    }

    public int getLeftTitleColWidth()
    {
        return WeekEvtView.INTFRAC(WeekEvtView.HOUR_DEFAULT_HEIGHT,(float) 1.5);
    }
    Rect GetWeekDayColRect(int nWeekDay)
    {

        return WeekEvtView.get_week_day_rect(nWeekDay, this.getBounds(), getLeftTitleColWidth());

    }

    public boolean hasMore(int nWeekDay)
    {
        for (int i=0; i< m_arMoreIcon.size(); i++)
        {
            if (m_arMoreIcon.get(i).equals(nWeekDay))
                return true;
        }
        return false;
    }
    public boolean hasPrev(int nWeekDay)
    {
        for (int i=0; i< m_arPrevIcon.size(); i++)
        {
            if (m_arPrevIcon.get(i).equals(nWeekDay))
                return true;
        }
        return false;
    }
    public void refreshPrevNextIcon(ArrayList<Integer> arMore, ArrayList<Integer> arPrev)
    {
        m_arPrevIcon.clear();
        m_arMoreIcon.clear();
        m_arMoreIcon.addAll(arMore);
        m_arPrevIcon.addAll(arPrev);
        this.invalidate();
    }
}
