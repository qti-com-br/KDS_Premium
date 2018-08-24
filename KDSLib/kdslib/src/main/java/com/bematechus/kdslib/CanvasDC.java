package com.bematechus.kdslib;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.util.ArrayList;

/**
 * drawing method is in this class.
 */
public class CanvasDC {
    Canvas m_canvas;
    public CanvasDC(Canvas g)
    {
        m_canvas = g;
    }
    public void attach(Canvas g)
    {
        m_canvas = g;
    }
    public Canvas getCanvas()
    {
        return m_canvas;
    }

    public  void fillRect(int color, Rect rect)
    {
        fillRect(this.getCanvas(), color, rect);
    }

    static public  void fillRect(Canvas g, int color, Rect rect)
    {

        Paint paint = new Paint();
        paint.setColor(color);
        g.drawRect(rect, paint);
    }

    /**
     * In android the text was drawing under base line.
     * @param g
     * @param ff
     * @param rect
     * @param text
     *
     * @param align
     */
    static public  int drawText(Canvas g, KDSViewFontFace ff, Rect rect, String text, Paint.Align align)
    {

        Paint paint = new Paint();
        paint.setColor(ff.getFG());
        paint.setTypeface(ff.getTypeFace());
        paint.setTextSize(ff.getFontSize());
        paint.setAntiAlias(true);

        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();

        int baseline = rect.top + (rect.bottom - rect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        g.save();
        g.clipRect(rect);
        //
        fillRect(g,ff.getBG(), getTextRectWithAlign(paint, rect, text, align));
        //
        paint.setColor(ff.getFG());
        //
        if (align == Paint.Align.CENTER)
        { //horizontal center
            paint.setTextAlign(Paint.Align.CENTER);
            g.drawText(text, rect.centerX(), baseline, paint);
        }
        else if (align == Paint.Align.LEFT)
        {
            paint.setTextAlign(Paint.Align.LEFT);
            g.drawText(text, rect.left, baseline, paint);
        }
        else if (align == Paint.Align.RIGHT)
        {
            paint.setTextAlign(Paint.Align.RIGHT);
            g.drawText(text, rect.right, baseline, paint);
        }

        g.restore();
        return getTextPixelsWidth(paint, text);
    }

    static public  int drawText_without_clear_bg(Canvas g, KDSViewFontFace ff, Rect rect, String text, Paint.Align align)
    {

        Paint paint = new Paint();
        paint.setColor(ff.getFG());
        paint.setTypeface(ff.getTypeFace());
        paint.setTextSize(ff.getFontSize());
        paint.setAntiAlias(true);

        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();

        int baseline = rect.top + (rect.bottom - rect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        g.save();
        g.clipRect(rect);
        //
        //fillRect(g,ff.getBG(), getTextRectWithAlign(paint, rect, text, align));
        //
        paint.setColor(ff.getFG());
        //
        if (align == Paint.Align.CENTER)
        { //horizontal center
            paint.setTextAlign(Paint.Align.CENTER);
            g.drawText(text, rect.centerX(), baseline, paint);
        }
        else if (align == Paint.Align.LEFT)
        {
            paint.setTextAlign(Paint.Align.LEFT);
            g.drawText(text, rect.left, baseline, paint);
        }
        else if (align == Paint.Align.RIGHT)
        {
            paint.setTextAlign(Paint.Align.RIGHT);
            g.drawText(text, rect.right, baseline, paint);
        }

        g.restore();
        return getTextPixelsWidth(paint, text);
    }

    /**
     *
     * @param g
     * @param ff
     * @param rect
     * @param text
     * @return
     *  the text pixels width
     */
    static public  int drawText(Canvas g, KDSViewFontFace ff, Rect rect, String text)
    {

        return drawText(g, ff, rect, text, Paint.Align.LEFT);

    }

    static public void drawBox(Canvas g, Rect rect, int color, int lineStrokeWidth)
    {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(lineStrokeWidth);
        g.drawRect(rect, paint);

    }

    static public void drawBoxLine(Canvas g, Rect rect, int color, int lineStrokeWidth, boolean bLeft, boolean bTop, boolean bRight, boolean bBottom)
    {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(lineStrokeWidth);
        if (bTop) //draw top and bottom first
            g.drawLine(rect.left, rect.top, rect.right, rect.top, paint);
        if (bBottom) //draw top and bottom first
            g.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, paint);

        if (bLeft) //draw top and bottom first
            g.drawLine(rect.left, rect.top, rect.left, rect.bottom, paint);

        if (bBottom) //draw top and bottom first
            g.drawLine(rect.right, rect.top, rect.right, rect.bottom, paint);

    }
    static public  void drawCircle(Canvas g, int color, Rect rect)
    {

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        int x = rect.left + rect.width()/2;
        int y = rect.top + rect.height()/2;
        int r = (rect.width()>rect.height()?rect.height():rect.width() );

        g.drawCircle(x, y, r, paint);
    }

    static public Paint createPaint(KDSViewFontFace ff)
    {
        Paint paint = new Paint();
        paint.setTypeface(ff.getTypeFace());
        paint.setTextSize(ff.getFontSize());
        paint.setColor(ff.getBG());
        return paint;
    }

    public static int getBestMaxFontSize(Rect rc, KDSViewFontFace ff, String str)
    {
        Paint p = createPaint(ff);
        Rect r = new Rect();

        for (int i=0; i< 100; i++)
        {
            p.getTextBounds(str, 0, str.length(), r);
            if (rc.width()>r.width() && rc.height() > r.height())
                return (int)p.getTextSize();
            else
                p.setTextSize(p.getTextSize()-1);
        }

         return ff.getFontSize();


    }

    public static int getBestMaxFontSize_increase(Rect rc, KDSViewFontFace ff, String str)
    {
        Paint p = createPaint(ff);
        Rect r = new Rect();
        int maxH = rc.height();
        int maxW = rc.width();

        for (int i=0; i< 100; i++)
        {
            p.getTextBounds(str, 0, str.length(), r);
            int w = r.width();
            int h = r.height()*2;
            if (w>maxW || h >maxH)
                return (int)p.getTextSize()-1;
            else
                p.setTextSize(p.getTextSize()+1);
            if (i == 99)
                return (int)p.getTextSize()-1;
        }

        return ff.getFontSize();


    }


    static int getTextPixelsWidth(Paint paint, String strText)
    {
        Rect r = new Rect();

        paint.getTextBounds(strText, 0, strText.length(), r);
        return r.width();
    }

    public static int getTextPixelsWidth(KDSViewFontFace ff,String strText)
    {
        Paint paint = new Paint();
        //paint.setColor(ff.getFG());
        paint.setTypeface(ff.getTypeFace());
        paint.setTextSize(ff.getFontSize());
        //paint.setAntiAlias(true);
        Rect r = new Rect();

        paint.getTextBounds(strText, 0, strText.length(), r);
        return r.left + r.width();
    }

    static Rect getTextRectWithAlign(Paint paint, Rect rcBorder, String strText, Paint.Align align )
    {
        int nWidth = getTextPixelsWidth(paint, strText);
        Rect rc = new Rect(rcBorder);
        switch (align)
        {

            case CENTER:

                rc.left = rc.left + (rc.width()-nWidth)/2 -1;
                rc.right = rc.left + nWidth+2;
                break;
            case LEFT:
                rc.right = rc.left + nWidth;
                break;
            case RIGHT:
                rc.left = rc.right - nWidth;
                break;
        }
        return rc;
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
    static public  void drawWrapString(Canvas g,KDSViewFontFace ft, Rect rt,String string, Paint.Align align )
    {
        g.save();
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(ft.getFontSize());
        textPaint.setTypeface(ft.getTypeFace());
        textPaint.setColor( ft.getFG());
        textPaint.setAntiAlias(true);

        g.clipRect(rt);
        Layout.Alignment al =  Layout.Alignment.ALIGN_CENTER;
        if (align == Paint.Align.RIGHT)
            al = Layout.Alignment.ALIGN_OPPOSITE;
        else if (align == Paint.Align.LEFT)
            al = Layout.Alignment.ALIGN_NORMAL;
        //StaticLayout sl = new StaticLayout(data,textPaint,getWidth(), Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,true);
        StaticLayout sl = new StaticLayout(string,textPaint,rt.width(), al,1.0f,0.0f,true);
//        int n = sl.getLineCount();
//        for (int i=0; i< n; i++)
//        {
//            int nstart = sl.getLineStart(i);
//            int nend = sl.getLineEnd(i);
////            Log.d("a", "b");
//        }

        int x = rt.left;
        int y = rt.top + (rt.height() - sl.getHeight())/2;
        g.translate(x,y);
        sl.draw(g);
        g.restore();
    }

    static public ArrayList<Point> getWrapStringRows(KDSViewFontFace ft, Rect rt, String string, Paint.Align align )
    {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(ft.getFontSize());
        textPaint.setTypeface(ft.getTypeFace());
       // textPaint.setColor( ft.getFG());
        textPaint.setAntiAlias(true);


        Layout.Alignment al =  Layout.Alignment.ALIGN_CENTER;
        if (align == Paint.Align.RIGHT)
            al = Layout.Alignment.ALIGN_OPPOSITE;
        else if (align == Paint.Align.LEFT)
            al = Layout.Alignment.ALIGN_NORMAL;
        //StaticLayout sl = new StaticLayout(data,textPaint,getWidth(), Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,true);
        StaticLayout sl = new StaticLayout(string,textPaint,rt.width(), al,1.0f,0.0f,true);
        int n = sl.getLineCount();
        ArrayList<Point> ar = new ArrayList<>();
        for (int i=0; i< n; i++)
        {
            Point pt = new Point();
            pt.x = sl.getLineStart(i);
            pt.y = sl.getLineEnd(i);
            ar.add(pt);
        }
        return ar;



    }

    static public Rect getWrapStringRect(KDSViewFontFace ft, Rect rt, String string, Paint.Align align, int nTextPadding )
    {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(ft.getFontSize());
        textPaint.setTypeface(ft.getTypeFace());
        // textPaint.setColor( ft.getFG());
        textPaint.setAntiAlias(true);


        Layout.Alignment al =  Layout.Alignment.ALIGN_CENTER;
        if (align == Paint.Align.RIGHT)
            al = Layout.Alignment.ALIGN_OPPOSITE;
        else if (align == Paint.Align.LEFT)
            al = Layout.Alignment.ALIGN_NORMAL;
        //StaticLayout sl = new StaticLayout(data,textPaint,getWidth(), Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,true);
        int nWidth = rt.width()-nTextPadding*2;
        if (nWidth <=0) return new Rect();

        StaticLayout sl = new StaticLayout(string,textPaint,nWidth, al,1.0f,0.0f,true);
        Rect rtReturn = new Rect(rt);
        rtReturn.bottom =rt.top+ sl.getHeight();

        return rtReturn;



    }


}
