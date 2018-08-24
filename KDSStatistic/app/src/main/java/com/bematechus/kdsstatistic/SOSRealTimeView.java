package com.bematechus.kdsstatistic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;
import com.bematechus.kdslib.KDSXML;

/**
 * Created by David.Wong on 2018/5/10.
 * Rev:
 */
public class SOSRealTimeView extends View {


    SOSRealViewProperties m_properties = new SOSRealViewProperties();

    float m_fltPercent = 0.93f;
    int m_nTargetPrepTimeSeconds = 135; //seconds
    int m_nRealTimeSeconds = 150;
    int m_nOverTargetCount = 5;


    int m_nLastNoZeroRealTimeSeconds = 0; //1.1.8 use this to replace zero showing.

    String m_strBottomText = "";

    final int MIN_FONT_SIZE = 5;
    final int BORDER_SIZE = 10;
    final int SHADOW_COLOR = Color.GRAY;
    final float SHADOW_SIZE = 4.0f;



    public SOSRealTimeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public SOSRealTimeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public SOSRealTimeView(Context context)
    {
        super(context);
    }

    public void setBottomText(String strText)
    {
        if (m_properties.m_stationID.equals(SOSSettings.OVERALL_STATION_ID)) {
            m_strBottomText = "";
            this.invalidate();
            return;
        }

        if (!m_strBottomText.equals(strText)) {
            m_strBottomText = strText;
            this.invalidate();
        }

    }

    public void onDraw(Canvas canvas)
    {

        Rect rt = new Rect();
        this.getDrawingRect(rt);
       // Canvas g = get_double_buffer();
        //drawBox(canvas, rt, Color.GREEN, 1);
        drawBackground(canvas, rt);
        //commit_double_buffer(canvas);
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


    //int m_nBorderColor = Color.BLACK;
    //int m_nBackgroundColor = 0xff404040;// Color.DKGRAY;
    //int m_nRealPrepTimeBG = 0xffb8cce4;// Color.BLUE;

    private void drawBorder(Canvas canvas, Rect rect, int radius, int x, int y)
    {
        //draw border
        Paint paintBorder = new Paint();
       // paintBorder.setColor(m_properties.m_borderColor.getBG());
        paintBorder.setAntiAlias(true);
        //paintBorder.setStrokeWidth(BORDER_SIZE);
        paintBorder.setStyle(Paint.Style.STROKE);
//        this.setLayerType(LAYER_TYPE_SOFTWARE, paintBorder);
//        paintBorder.setShadowLayer(SHADOW_SIZE, 0.0f, 2.0f,SHADOW_COLOR);
        paintBorder.setColor( m_properties.m_percentColor.getBG());// SHADOW_COLOR);
        paintBorder.setStrokeWidth(SHADOW_SIZE);
        int r = (int)(radius- SHADOW_SIZE);
        canvas.drawCircle(x, y, r, paintBorder);

        paintBorder.setStrokeWidth(BORDER_SIZE);
        paintBorder.setColor(m_properties.m_borderColor.getBG());
        r = (int)(radius- SHADOW_SIZE-BORDER_SIZE);
        canvas.drawCircle(x, y, r, paintBorder);

        Paint titlePaint = new Paint();
        titlePaint.setAntiAlias(true);
        titlePaint.setStyle(Paint.Style.STROKE);
        titlePaint.setTextSize(BORDER_SIZE);
        titlePaint.setStrokeWidth(1);
        titlePaint.setColor(m_properties.m_borderColor.getFG());



        //draw title
        Rect rtText = new Rect();
        String s = m_properties.m_strTitle;// "Station #1";//23456789012345678901234567890";
        if (s.isEmpty()) {
            if (m_properties.m_stationID.equals(SOSSettings.OVERALL_STATION_ID))
                s = getContext().getString(R.string.overall);
            else
                s = getContext().getString(R.string.station_number) + m_properties.m_stationID;

        }


        titlePaint.getTextBounds(s, 0, s.length(), rtText);
        int nLen = rtText.width();//*3/2;
        int degree =(int)Math.round( 180 * nLen/Math.PI/r);
        degree = Math.round( degree/2);
        Path path = new Path();
        path.addArc(new RectF(x -r, y-r, x + r, y + r), 270-degree, degree*2+5);

        canvas.drawTextOnPath(s, path, 0, 3, titlePaint);

        //draw bottom text
        if (!m_strBottomText.isEmpty()) {
            rtText = new Rect();
            s = m_strBottomText;// "Station #1";//23456789012345678901234567890";

            titlePaint.getTextBounds(s, 0, s.length(), rtText);
            nLen = rtText.width();//*3/2;
            degree = (int) Math.round(180 * nLen / Math.PI / r);
            degree = Math.round(degree / 2);
            path = new Path();
            path.addArc(new RectF(x - r, y - r, x + r, y + r), 90 + degree, -1*(degree * 2 + 5));
            //path.addOval(new RectF(x - r, y - r, x + r, y + r), Path.Direction.CCW);

            canvas.drawTextOnPath(s, path, 0, 3, titlePaint);
        }

    }
    /**
     *
     * @param canvas
     * @param rect
     * @return
     *  The valid radius value of this view.
     */
    private int drawBackground(Canvas canvas, Rect rect )
    {

        int nMinSize = (rect.width()>rect.height()?rect.height():rect.width());
        int x = rect.width()/2;
        int y = rect.height()/2;
        int radius = nMinSize / 2 ;

        canvas.save();

        Rect rtClip = new Rect(x - radius, y - radius, x + radius, y + radius);
        canvas.clipRect(rtClip);
        //draw circle with real time prep time color.
        Paint paintReal = new Paint();
        paintReal.setAntiAlias(true);
        if (m_nRealTimeSeconds > m_properties.getTargetSeconds())
        {
            paintReal.setColor(m_properties.m_alertColor.getBG());
        }
        else
            paintReal.setColor(m_properties.m_realColor.getBG());
        canvas.drawCircle(x, y, radius - SHADOW_SIZE-BORDER_SIZE, paintReal);


        RectF rf = new RectF(rtClip);
        int nInset = (int) SHADOW_SIZE + BORDER_SIZE;
        rf.inset(  nInset, nInset);
        //draw percent bg //141 degree
        Paint paintNormal = new Paint();
        paintNormal.setAntiAlias(true);
        paintNormal.setColor(m_properties.m_percentColor.getBG());
        canvas.drawArc(rf, (float) (180+19.5f),141, false, paintNormal);

        //draw over count bg
        canvas.drawArc(rf, (float) (19.5f),141, false, paintNormal);
        //draw border
//        Paint paintBorder = new Paint();
//        paintBorder.setColor(m_properties.m_borderColor.getBG());
//        paintBorder.setAntiAlias(true);
//        paintBorder.setStrokeWidth(BORDER_SIZE);
//        paintBorder.setStyle(Paint.Style.STROKE);
//        this.setLayerType(LAYER_TYPE_SOFTWARE, paintBorder);
//        paintBorder.setShadowLayer(SHADOW_SIZE, 0.0f, 2.0f,SHADOW_COLOR);
//
//        canvas.drawCircle(x, y, radius - SHADOW_SIZE-BORDER_SIZE, paintBorder);

        drawBorder(canvas, rect, radius, x, y);

        Path path = new Path();

        path.addCircle(x, y, radius - nInset, Path.Direction.CCW);
        canvas.clipPath(path);
        //changed!!!!
        radius-= nInset;
        //draw text.
        //draw percent
        Rect rtPercent = new Rect(x - radius, y - radius, x + radius, y - radius/3);
        drawPercentText(canvas, rtPercent);
        //draw real
        Rect rtReal = new Rect(x - radius, y - radius/3, x + radius, y + radius/3);
        drawRealText(canvas, rtReal);

        //draw count
        Rect rtCount = new Rect(x - radius, y + radius/3, x + radius, y + radius);
        drawCountText(canvas, rtCount);

        canvas.restore();
        return (int)(radius - nInset);


    }
//    static public void drawBox(Canvas g, Rect rect, int color, int lineStrokeWidth)
//    {
//        Paint paint = new Paint();
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setColor(color);
//        paint.setStrokeWidth(lineStrokeWidth);
//        g.drawRect(rect, paint);
//
//    }
    //int m_nPercentFG = 0xfff1f11f;

    private void drawPercentText(Canvas canvas, Rect rect)
    {
        KDSViewFontFace ff = new KDSViewFontFace();
        ff.setBG(m_properties.m_percentColor.getBG());
        ff.setFG(m_properties.m_percentColor.getFG());
        ff.setFontSize(MIN_FONT_SIZE);
        int n =(int)( m_fltPercent * 100);

        //draw percent value
        Rect rt = new Rect(rect);
        int nTargetTextHeight =  Math.round( ((float)rt.height())/5*2 ) ;

        String text = KDSUtil.convertIntToString(n) +"%";

        rt.bottom = rt.bottom - nTargetTextHeight;
        int nSize = CanvasDC.getBestMaxFontSize_increase(rt,ff,  text);
        ff.setFontSize(nSize);

        CanvasDC.drawText_without_clear_bg(canvas, ff, rt, text, Paint.Align.CENTER);

        //target 0:0 text.
        String strTarget =getContext().getString(R.string.within) +" " + secondsToMinsString(m_properties.getTargetSeconds());
        strTarget += " " +getContext().getString(R.string.mins);
        rt = new Rect(rect);

        rt.top = rt.bottom -  nTargetTextHeight;
        ff.setFontSize( MIN_FONT_SIZE);
        nSize = CanvasDC.getBestMaxFontSize_increase(rt,ff,  strTarget);
        ff.setFontSize(nSize);

        CanvasDC.drawText_without_clear_bg(canvas, ff, rt, strTarget, Paint.Align.CENTER);


        //draw "within" text
//        rt.top = rt.bottom -  nTargetTextHeight*2;
//        strTarget = getContext().getString(R.string.within);
//        ff.setFontSize(Math.round( ((float) nSize) * (0.9f) )) ;
//        CanvasDC.drawText_without_clear_bg(canvas, ff, rt, strTarget, Paint.Align.CENTER);

    }

    private String secondsToMinsString(int nSeconds)
    {
        int h = (nSeconds / 3600);
        int m = ((nSeconds % 3600)/ 60);
        int s = (nSeconds % 60);

        m += (h*60);

        String str = String.format("%02d:%02d", m, s);
        return str;


    }


    //int m_nRealPrepTimeFG = Color.BLACK;

    private void drawRealText(Canvas canvas, Rect rect)
    {

        int nSeconds = m_nRealTimeSeconds;

        if (nSeconds  <=0) {
            int n = SOSKDSGlobalVariables.getKDSSOS().getSettings().getInt(SOSSettings.ID.Zero_value_show);
            SOSSettings.ZeroValueShow zeroShowing = SOSSettings.ZeroValueShow.values()[n];
            if (zeroShowing == SOSSettings.ZeroValueShow.Last_data) {
                nSeconds = m_nLastNoZeroRealTimeSeconds;
            }
        }


        String text =  secondsToMinsString(nSeconds);
        Rect rt = new Rect(rect);

        KDSViewFontFace ff = new KDSViewFontFace();
        if (nSeconds > m_properties.getTargetSeconds())
        {
            ff.setBG(m_properties.m_alertColor.getBG());
            ff.setFG(m_properties.m_alertColor.getFG());
        }
        else {
            ff.setBG(m_properties.m_realColor.getBG());
            ff.setFG(m_properties.m_realColor.getFG());
        }
        ff.setFontSize( MIN_FONT_SIZE);
        int nSize = CanvasDC.getBestMaxFontSize_increase(rt,ff,  text);
        ff.setFontSize(nSize);
        CanvasDC.drawText_without_clear_bg(canvas, ff, rt, text, Paint.Align.CENTER);


        //draw mins
        Paint paint = new Paint();
        paint.setTextSize(nSize);
        Rect r = new Rect();
        paint.getTextBounds(text, 0, text.length(), r);

        int nwidth = r.width();

        r = new Rect(rect);
        r.left = r.right - (r.width()-nwidth)/2 + 2;
        r.top = r.bottom - r.height()/3;
        text = getContext().getString(R.string.mins);
        ff.setFontSize(nSize/3);
        CanvasDC.drawText_without_clear_bg(canvas, ff, r, text, Paint.Align.LEFT);

        //draw "Current AVG time"
        r = new Rect(rect);
        //r.left += 15;
        //r.right = r.left - (r.width()-nwidth)/2 - 2;
        r.bottom = r.top + r.height()/4;
        text = getContext().getString(R.string.current_avg_time);
        ff.setFontSize(nSize/4);
        CanvasDC.drawText_without_clear_bg(canvas, ff, r, text, Paint.Align.CENTER);
    }


    private void drawCountText(Canvas canvas, Rect rect)
    {
        String text = getContext().getString(R.string.orders_over_target);
        Rect rt = new Rect(rect);
        rt.bottom = rt.top + rt.height()/4;
        KDSViewFontFace ff = new KDSViewFontFace();
        ff.setBG( m_properties.m_countColor.getBG());
        ff.setFG(m_properties.m_countColor.getFG());
        ff.setFontSize(MIN_FONT_SIZE);
        int nSize = CanvasDC.getBestMaxFontSize_increase(rt,ff,  text);
        ff.setFontSize(nSize);
        CanvasDC.drawText_without_clear_bg(canvas, ff, rt, text, Paint.Align.CENTER);

        rt = new Rect(rect);
        rt.top = rt.top + rt.height()/4;
        ff.setFontSize(MIN_FONT_SIZE);
        text = KDSUtil.convertIntToString(m_nOverTargetCount);
        nSize = CanvasDC.getBestMaxFontSize_increase(rt,ff,  text);
        ff.setFontSize(nSize);
        CanvasDC.drawText_without_clear_bg(canvas, ff, rt, text, Paint.Align.CENTER);

    }

//    private void drawTitle(Canvas canvas, Rect rectCircle, int nSize)
//    {
//        Paint citePaint = new Paint();
//        citePaint.setAntiAlias(true);
//        citePaint.setStyle(Paint.Style.STROKE);
//        citePaint.setTextSize(14);
//        citePaint.setStrokeWidth(1);
//        Path path = new Path();
//        path.addArc(new RectF(rectCircle), -180, 180);
//
//        canvas.drawTextOnPath("Station #1", path, 28, 0, citePaint);
//        canvas.restore();
//    }

    public void outputToXml(KDSXML xml)
    {

        xml.setAttribute("Type", "0");
        m_properties.outputToXml(xml);

    }
    public void parseXml(KDSXML xml)
    {
        m_properties.parseXml(xml);
    }

    public boolean isForStation(String stationID)
    {
        return m_properties.m_stationID.equals(stationID);
    }

    public void setRealTimeSeconds(int nSeconds)
    {
        m_nRealTimeSeconds = nSeconds;
        if (nSeconds >0)
            m_nLastNoZeroRealTimeSeconds = nSeconds;
        if (nSeconds  <=0) {
            int n = SOSKDSGlobalVariables.getKDSSOS().getSettings().getInt(SOSSettings.ID.Zero_value_show);
            SOSSettings.ZeroValueShow zeroShowing = SOSSettings.ZeroValueShow.values()[n];
            if (zeroShowing == SOSSettings.ZeroValueShow.Last_data) {
                m_nRealTimeSeconds = m_nLastNoZeroRealTimeSeconds;
            }
        }
        this.invalidate();
    }

//    public void setTartTimeSeconds(int nSeconds)
//    {
//        m_nTargetPrepTimeSeconds = nSeconds;
//        this.invalidate();
//    }
//


    public void init_for_running()
    {
        m_fltPercent = 0f;
        //m_nTargetPrepTimeSeconds = 0; //seconds
        m_nRealTimeSeconds = 0;
        m_nOverTargetCount = 0;

        m_nLastNoZeroRealTimeSeconds = 0; //1.1.8

        setBottomText(m_strBottomText = SOSKDSStationSOSInfo.getStatusInfo(SOSKDSStationSOSInfo.StationSOSStatus.Unknown));
    }

    public void setOverTargetCount(int nCount)
    {
        m_nOverTargetCount = nCount;
    }

    public void setPercent(float fltPercent)
    {
        m_fltPercent = fltPercent;
    }
}
