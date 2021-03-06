package com.bematechus.kdslib;

import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
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

        return drawText(g, ff, rect, text, align, false);

//        Paint paint = new Paint();
//        paint.setColor(ff.getFG());
//        paint.setTypeface(ff.getTypeFace());
//        paint.setTextSize(ff.getFontSize());
//        paint.setAntiAlias(true);
//
//        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
//
//        int baseline = rect.top + (rect.bottom - rect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
//        g.save();
//        g.clipRect(rect);
//        //
//        fillRect(g,ff.getBG(), getTextRectWithAlign(paint, rect, text, align));
//        //
//        paint.setColor(ff.getFG());
//        //
//        if (align == Paint.Align.CENTER)
//        { //horizontal center
//            paint.setTextAlign(Paint.Align.CENTER);
//            g.drawText(text, rect.centerX(), baseline, paint);
//        }
//        else if (align == Paint.Align.LEFT)
//        {
//            paint.setTextAlign(Paint.Align.LEFT);
//            g.drawText(text, rect.left, baseline, paint);
//        }
//        else if (align == Paint.Align.RIGHT)
//        {
//            paint.setTextAlign(Paint.Align.RIGHT);
//            g.drawText(text, rect.right, baseline, paint);
//        }
//
//        g.restore();
//        return getTextPixelsWidth(paint, text);
    }

    static public  int drawText_without_clear_bg(Canvas g, KDSViewFontFace ff, Rect rect, String text, Paint.Align align, boolean bBold)
    {

        TextPaint paint = new TextPaint();

        paint.setColor(ff.getFG());
        paint.setTypeface(ff.getTypeFace());
        paint.setTextSize(ff.getFontSize());
        paint.setAntiAlias(true);
        if (bBold)
            paint.setFakeBoldText(bBold);
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
        paint.setDither(true);
        //paint.setFilterBitmap(true);
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
        drawWrapString(g, ft, rt, string, align, false);
//        g.save();
//        TextPaint textPaint = new TextPaint();
//        textPaint.setTextSize(ft.getFontSize());
//        textPaint.setTypeface(ft.getTypeFace());
//        textPaint.setColor( ft.getFG());
//        textPaint.setAntiAlias(true);
//
//        g.clipRect(rt);
//        Layout.Alignment al =  Layout.Alignment.ALIGN_CENTER;
//        if (align == Paint.Align.RIGHT)
//            al = Layout.Alignment.ALIGN_OPPOSITE;
//        else if (align == Paint.Align.LEFT)
//            al = Layout.Alignment.ALIGN_NORMAL;
//        //StaticLayout sl = new StaticLayout(data,textPaint,getWidth(), Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,true);
//        StaticLayout sl = new StaticLayout(string,textPaint,rt.width(), al,1.0f,0.0f,true);
////        int n = sl.getLineCount();
////        for (int i=0; i< n; i++)
////        {
////            int nstart = sl.getLineStart(i);
////            int nend = sl.getLineEnd(i);
//////            Log.d("a", "b");
////        }
//
//        int x = rt.left;
//        int y = rt.top + (rt.height() - sl.getHeight())/2;
//        g.translate(x,y);
//        sl.draw(g);
//        g.restore();
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

//    static public final int ROUND_CORNER_DX = 10;
//    static public final int ROUND_CORNER_DY = 10;
    static int SHADOW_COLOR = 0xFFDDDDDD;
    static public void drawRoundRect(Canvas g, Rect rc, int color, boolean bRoundCorner, boolean bShadow)
    {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(color);

        RectF rt = new RectF(rc);
        if (bRoundCorner) {
            if (bShadow)
            {
//                MaskFilter filter = p.getMaskFilter();
//                p.setMaskFilter(new BlurMaskFilter(20, BlurMaskFilter.Blur.SOLID));


//                Shader shader = p.getShader();
//                LinearGradient backGradient = new LinearGradient(rt.left+5, rt.top+5, rt.right+5, rt.bottom+5, new int[]{Color.GRAY, Color.WHITE}, null, Shader.TileMode.CLAMP);
//                p.setShader(backGradient);
                RectF rtShadow = new RectF(rt);
                rtShadow.left += 5;
                rtShadow.right += 5;
                rtShadow.top += 5;
                rtShadow.bottom += 5;
                p.setColor(SHADOW_COLOR);//Color.LTGRAY);
                g.drawRoundRect(rtShadow, ROUND_CORNER_DX, ROUND_CORNER_DY, p);
                p.setColor(color);
                //p.setShader(shader);
                //p.setMaskFilter(filter);

            }
//            //else
            g.drawRoundRect(rt, ROUND_CORNER_DX, ROUND_CORNER_DY, p);
        }
        else
            g.drawRect(rt, p);


//        Resources res = KDSApplication.getContext().getResources();
//        Drawable myImage = res.getDrawable(R.drawable.ios_panel_shadow_border );
//        myImage.setBounds(rc);
//        myImage.draw(g);

    }
    static public final int ROUND_CORNER_DX = 10;
    static public final int ROUND_CORNER_DY = 10;

    static public void drawLeftUpArc(Canvas canvas, Rect rect, int nColor) {

        Path path = new Path();
        path.moveTo(rect.left, rect.top + ROUND_CORNER_DY);
        path.lineTo(rect.left, rect.top);
        path.lineTo(rect.left + ROUND_CORNER_DX, rect.top);
        //arcTo???????????????????????????????????????????????????????????????-90????????????????????????????????????????????????
        path.arcTo(new RectF(rect.left,rect.top,rect.left + ROUND_CORNER_DX*2,rect.top + ROUND_CORNER_DY*2),-90,-90);
        path.close();
        drawPath(canvas, path, nColor);
//        Paint p = new Paint();
//        p.setColor(nColor);
//        p.setAntiAlias(true);
//        p.setDither(true);
//        //canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
//        canvas.drawPath(path, p);

//        canvas.save();
//        Paint p = new Paint();
//        p.setColor(nColor);
//        p.setAntiAlias(true);
//        //p.setDither(true);
//        //p.setStyle(Paint.Style.STROKE);
//        p.setStrokeWidth(4);
//        canvas.clipPath(path);
//        canvas.drawRoundRect( new RectF(rect), ROUND_CORNER_DX,ROUND_CORNER_DY, p);
//        canvas.restore();
    }

    static public void drawLeftDownArc(Canvas canvas, Rect rect, int nColor) {
        Path path = new Path();
        path.moveTo(rect.left, rect.bottom - ROUND_CORNER_DY);
        path.lineTo(rect.left, rect.bottom);
        path.lineTo(rect.left+ROUND_CORNER_DX, rect.bottom);
        path.arcTo(new RectF(rect.left,rect.bottom -ROUND_CORNER_DY*2,rect.left+ROUND_CORNER_DX*2,rect.bottom),90,90);
        path.close();
        drawPath(canvas, path, nColor);
//        Paint p = new Paint();
//        p.setColor(nColor);
//        p.setAntiAlias(true);
//        p.setDither(true);
//        //canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
//        canvas.drawPath(path, p);
    }

    static public void drawRightDownArc(Canvas canvas, Rect rect, int nColor) {
        Path path = new Path();
        path.moveTo( rect.right -ROUND_CORNER_DX, rect.bottom);
        path.lineTo(rect.right, rect.bottom);
        path.lineTo(rect.right, rect.bottom-ROUND_CORNER_DY);
        path.arcTo(new RectF(rect.right-ROUND_CORNER_DX*2,rect.bottom-ROUND_CORNER_DY*2,rect.right,rect.bottom), 0, 90);
        path.close();
        drawPath(canvas, path, nColor);
//        Paint p = new Paint();
//        p.setColor(nColor);
//        p.setAntiAlias(true);
//        p.setDither(true);
//        //canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
//        canvas.drawPath(path, p);
    }

    static public void drawRightUpArc(Canvas canvas, Rect rect, int nColor) {
        Path path = new Path();
        path.moveTo(rect.right, rect.top + ROUND_CORNER_DY);
        path.lineTo(rect.right, rect.top);
        path.lineTo(rect.right-ROUND_CORNER_DX, rect.top);
        path.arcTo(new RectF(rect.right-ROUND_CORNER_DX*2,rect.top,rect.right,rect.top+ROUND_CORNER_DY*2),-90,90);
        path.close();
        drawPath(canvas, path, nColor);
//        Paint p = new Paint();
//        p.setColor(nColor);
//        p.setAntiAlias(true);
//        p.setDither(true);
//        //canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
//        canvas.drawPath(path, p);
    }

    static private void drawPath(Canvas canvas, Path path, int nColor)
    {
        Paint p = new Paint();
        p.setColor(nColor);
        p.setAntiAlias(true);
        p.setDither(true);
        //p.setFilterBitmap(true);
        //p.setStyle(Paint.Style.FILL_AND_STROKE);//.STROKE);
        //p.setStrokeWidth(4);
        //p.setPathEffect(new CornerPathEffect(ROUND_CORNER_DX));
        //canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.drawPath(path, p);
    }

    /**
     * rev.:
     *  kp-101 Text cut off (Item).
     *           check the rt if it can hold all text. If not, change font size to small one.
     *
     * @param g
     * @param ft
     * @param rt
     * @param string
     * @param align
     * @param bBold
     * @return
     *  Point: x: the font size
     *         y: the y point of drawing.
     */
    static public  Point drawWrapString(Canvas g,KDSViewFontFace ft, Rect rt,String string, Paint.Align align, boolean bBold )
    {
        g.save();
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(ft.getFontSize());
        textPaint.setTypeface(ft.getTypeFace());
        textPaint.setColor( ft.getFG());
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(bBold);

        Point ptReturn = new Point();

        g.clipRect(rt);
        Layout.Alignment al =  Layout.Alignment.ALIGN_CENTER;
        if (align == Paint.Align.RIGHT)
            al = Layout.Alignment.ALIGN_OPPOSITE;
        else if (align == Paint.Align.LEFT)
            al = Layout.Alignment.ALIGN_NORMAL;
        //StaticLayout sl = new StaticLayout(data,textPaint,getWidth(), Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,true);
        StaticLayout sl = new StaticLayout(string,textPaint,rt.width(), al,1.0f,0.0f,true);
        //kp-101 Text cut off (Item).

        if (sl.getHeight() > rt.height())
        {
            int nsize = (int) textPaint.getTextSize() + 2;
            for (int i=0; i< nsize; i++)
            {
                textPaint.setTextSize( textPaint.getTextSize()-1);

                sl = new StaticLayout(string,textPaint,rt.width(), al,1.0f,0.0f,true);
                if (sl.getHeight() <= rt.height())
                    break;
            }
        }
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

        ptReturn.x = (int)textPaint.getTextSize();
        ptReturn.y = y;
        return ptReturn;

    }

    static public  int drawText(Canvas g, KDSViewFontFace ff, Rect rect, String text, Paint.Align align, boolean bBold)
    {

        int bestFontSize = getBestMaxFontSizeHeight_decrease(rect, ff, text);
        TextPaint paint = new TextPaint();
        paint.setColor(ff.getFG());
        paint.setTypeface(ff.getTypeFace());
        //paint.setTextSize(ff.getFontSize());
        paint.setTextSize(bestFontSize); //kp-76,
        paint.setAntiAlias(true);
        paint.setFakeBoldText(bBold);

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


    static public  int drawText_without_clear_bg_for_draw_logo(Canvas g, KDSViewFontFace ff, Rect rect, String text, Paint.Align align, boolean bBold, int nAlpha)
    {

        TextPaint paint = new TextPaint();

        paint.setColor(ff.getFG());
        paint.setTypeface(ff.getTypeFace());
        paint.setTextSize(ff.getFontSize());
        paint.setAntiAlias(true);
        if (bBold)
            paint.setFakeBoldText(bBold);
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();

        int baseline = rect.top + (rect.bottom - rect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        g.save();
        g.clipRect(rect);
        //
        //fillRect(g,ff.getBG(), getTextRectWithAlign(paint, rect, text, align));
        //
        paint.setColor(ff.getFG());
        paint.setAlpha(nAlpha);
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
     * KP-76 Font size, line height and cut off for different screen resolutions
     * @param rc
     * @param ff
     * @param str
     * @return
     */
    public static int getBestMaxFontSizeHeight_decrease(Rect rc, KDSViewFontFace ff, String str)
    {
        Paint p = createPaint(ff);
        Rect r = new Rect();
        int maxH = rc.height();
        //int maxW = rc.width();
        int minSize = 8;
        for (int i=ff.getFontSize(); i>= minSize; i--)
        {
            p.getTextBounds(str, 0, str.length(), r);
            //int w = r.width();
            int h = (int)(r.height()*1.2);
            if ( h < maxH)
                return (int)p.getTextSize();
            else
                p.setTextSize(p.getTextSize() - 1);
            if (i == minSize)
                return (int)minSize;
        }

        return ff.getFontSize();


    }

    /**
     * top align
     * @param g
     * @param ff
     * @param rect
     * @param text
     * @param align
     * @param bBold
     * @return
     */
    static public  int drawQtyWrapText(Canvas g, KDSViewFontFace ff, Rect rect, String text, Paint.Align align, boolean bBold)
    {

        //int bestFontSize = getBestMaxFontSizeHeight_decrease(rect, ff, text);
        TextPaint paint = new TextPaint();
        paint.setColor(ff.getFG());
        paint.setTypeface(ff.getTypeFace());
        //paint.setTextSize(ff.getFontSize());
        paint.setTextSize(ff.getFontSize()); //kp-76,
        paint.setAntiAlias(true);
        paint.setFakeBoldText(bBold);

        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();

        int baseline = rect.top - fontMetrics.top;// + (rect.bottom - rect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
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

    static public  Point drawWrapStringTopAlign(Canvas g,KDSViewFontFace ft, Rect rt,String string, Paint.Align align, boolean bBold )
    {
        g.save();
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(ft.getFontSize());
        textPaint.setTypeface(ft.getTypeFace());
        textPaint.setColor( ft.getFG());
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(bBold);

        Point ptReturn = new Point();

        g.clipRect(rt);
        Layout.Alignment al =  Layout.Alignment.ALIGN_CENTER;
        if (align == Paint.Align.RIGHT)
            al = Layout.Alignment.ALIGN_OPPOSITE;
        else if (align == Paint.Align.LEFT)
            al = Layout.Alignment.ALIGN_NORMAL;
        //StaticLayout sl = new StaticLayout(data,textPaint,getWidth(), Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,true);
        StaticLayout sl = new StaticLayout(string,textPaint,rt.width(), al,1.0f,0.0f,true);
        //kp-101 Text cut off (Item).

        if (sl.getHeight() > rt.height())
        {
            int nsize = (int) textPaint.getTextSize() + 2;
            for (int i=0; i< nsize; i++)
            {
                textPaint.setTextSize( textPaint.getTextSize()-1);

                sl = new StaticLayout(string,textPaint,rt.width(), al,1.0f,0.0f,true);
                if (sl.getHeight() <= rt.height())
                    break;
            }
        }
//        int n = sl.getLineCount();
//        for (int i=0; i< n; i++)
//        {
//            int nstart = sl.getLineStart(i);
//            int nend = sl.getLineEnd(i);
////            Log.d("a", "b");
//        }

        int x = rt.left;
        int y = rt.top;// + (rt.height() - sl.getHeight())/2;
        g.translate(x,y);
        sl.draw(g);
        g.restore();

        ptReturn.x = (int)textPaint.getTextSize();
        ptReturn.y = y;
        return ptReturn;

    }
}
