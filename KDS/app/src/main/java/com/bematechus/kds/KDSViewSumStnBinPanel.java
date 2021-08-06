package com.bematechus.kds;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;

import java.util.ArrayList;

public class KDSViewSumStnBinPanel extends KDSViewSumStnPanel{

    static public KDSViewSumStnBinPanel createNew(KDSViewSumStnSumGroup group) {
        KDSViewSumStnBinPanel panel = new KDSViewSumStnBinPanel();
        panel.setSumGroup(group);
        return panel;
    }

    private Context getContext()
    {
        return KDSApplication.getContext();
    }
    public void onDraw(Canvas g, KDSViewSettings env, Rect screenDataRect,
                       int nOrderPanelIndex,
                       KDSViewFontFace ffCaption, KDSViewFontFace ffItem,
                       int nRowHeight) {
        if (m_arRects.size() <= 0) return;
        drawPanel(g, env, screenDataRect, nOrderPanelIndex, ffCaption, ffItem);

//        drawCaption(g, env, screenDataRect, nOrderPanelIndex, ffCaption, ffItem);
//
//        for (int i = 0; i < m_arRowEntries.size(); i++) {
//            m_arRowEntries.get(i).onDraw(g, env, screenDataRect, m_arRects, (i != 0), ffItem, nRowHeight);
//        }
    }

    protected void drawPanel(Canvas g, KDSViewSettings env, Rect screenDataRect, int nOrderPanelIndex, KDSViewFontFace ffCaption, KDSViewFontFace ffItem) {
        if (m_arRects.size() <= 0) return;
        int nbg = ffItem.getBG();// env.getSettings().getInt(KDSSettings.ID.Panels_BG);

        Rect rtReal =  m_arRects.get(0);//convertToAbsoluteRect(m_arRects.get(0), screenDataRect);
        //Rect rtReal = m_arRects.get(0);
        rtReal.inset(KDSViewSumStation.BORDER_INSET_DX, KDSViewSumStation.BORDER_INSET_DY);
        drawRoundRect(g, rtReal, nbg, false, false);

        float nQty = 0;
        String text = "";
        if (m_sumGroup.items().size()>0) {
            nQty = m_sumGroup.items().get(0).getQty();
            text = m_sumGroup.items().get(0).getDescription(false);
        }

        drawCount(g, getCountRect(rtReal),(int)nQty, ffItem );
        drawItemText(g, ffItem, getTextRect(rtReal), text);


    }

    private Rect getCountRect(Rect rtReal)
    {

        int nMinSize = rtReal.width() > rtReal.height()?rtReal.height():rtReal.width();
        int w = nMinSize/2;
        Rect rc = new Rect();
        rc.left = rtReal.left + (rtReal.width()-w)/2;
        rc.right = rc.left + w;
        rc.top = rtReal.top + (rtReal.height()-w)/2;
        rc.bottom = rc.top + w;
        return rc;

    }

    private Rect getTextRect(Rect rtReal)
    {
        Rect rc = new Rect();
        rc.left = rtReal.left;
        rc.right = rtReal.right;
        rc.top = rtReal.top + rtReal.height()/20;
        rc.bottom = rc.top + 50;
        return rc;
    }
    private void drawItemText(Canvas canvas,KDSViewFontFace ff,  Rect rect, String text)
    {
        CanvasDC.drawText(canvas, ff,rect, text, Paint.Align.CENTER );
    }

    final int BORDER_SIZE = 2;
    /**
     *
     * @param canvas
     * @param rect
     * @return
     *  The valid radius value of this view.
     */
    private int drawCount(Canvas canvas, Rect rect,int nCount, KDSViewFontFace ff)
    {

        int nMinSize = (rect.width()>rect.height()?rect.height():rect.width());
        int x = rect.left +  rect.width()/2;
        int y = rect.top + rect.height()/2;
        int radius = nMinSize / 2 ;

        canvas.save();

        Rect rtClip = new Rect(x - radius, y - radius, x + radius, y + radius);
        canvas.clipRect(rtClip);
        //draw circle with real time prep time color.
        Paint paintReal = new Paint();
        paintReal.setAntiAlias(true);
        int nColor = ff.getBG();
        int nBorderColor = ff.getFG();
        paintReal.setColor(nColor);

        canvas.drawCircle(x, y, radius - BORDER_SIZE, paintReal);

        drawBorder(canvas, rect, radius, x, y, nBorderColor);

        int nInset = BORDER_SIZE;

//        Path path = new Path();
//
//        path.addCircle(x, y, radius - nInset, Path.Direction.CCW);
//        canvas.clipPath(path);
//        //changed!!!!
        radius-= nInset;
        //draw text.
        //draw percent
        Rect rtPercent = rect;//new Rect(x - radius, y - radius, x + radius, y - radius/3);
        drawCountTextInRound(canvas, rtPercent, KDSUtil.convertIntToString(nCount),ff );

        canvas.restore();
        return (int)(radius - nInset);


    }


    private void drawBorder(Canvas canvas, Rect rect, int radius, int x, int y, int nColor)
    {
        //draw border
        Paint paintBorder = new Paint();
        // paintBorder.setColor(m_properties.m_borderColor.getBG());
        paintBorder.setAntiAlias(true);
        //paintBorder.setStrokeWidth(BORDER_SIZE);
        paintBorder.setStyle(Paint.Style.STROKE);
//        this.setLayerType(LAYER_TYPE_SOFTWARE, paintBorder);
//        paintBorder.setShadowLayer(SHADOW_SIZE, 0.0f, 2.0f,SHADOW_COLOR);
        paintBorder.setColor( nColor);// SHADOW_COLOR);
        paintBorder.setStrokeWidth(BORDER_SIZE);
        int r = (int)(radius- BORDER_SIZE);
        canvas.drawCircle(x, y, r, paintBorder);



    }


    private void drawCountTextInRound(Canvas canvas, Rect rect,String text, KDSViewFontFace ff )
    {


        //draw percent value
        Rect rt = new Rect(rect);
        //int nTargetTextHeight =  Math.round( ((float)rt.height())/2 ) ;

        //tring text = KDSUtil.convertIntToString(n) +"%";

        //rt.bottom = rt.bottom - nTargetTextHeight;
        int nSize = CanvasDC.getBestMaxFontSize_increase(rt,ff,  text);
        int noldSize = ff.getFontSize();
        ff.setFontSize(nSize);

        CanvasDC.drawText_without_clear_bg(canvas, ff, rt, text, Paint.Align.CENTER, false);
        ff.setFontSize(noldSize);

    }


}
