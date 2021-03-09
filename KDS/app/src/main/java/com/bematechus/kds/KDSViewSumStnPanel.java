package com.bematechus.kds;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSViewFontFace;

import java.util.ArrayList;

/**
 * Summary station feature,
 * Use this class to hold panels,
 */

public class KDSViewSumStnPanel extends KDSViewPanelBase {

    //static public int m_premessageHeight = 20;
    static public int m_orderCaptionHeight = 40;

    ArrayList<KDSViewSumStnEntry> m_arRowEntries = new ArrayList<>(); //all my rows
    ArrayList<Rect> m_arRects = new ArrayList<>(); //my location

    //KDSIOSViewOrder.KDSSize m_messagesSize = new KDSIOSViewOrder.KDSSize(0, 0);

    KDSViewSumStnSumGroup m_sumGroup = null; //all summary items

    KDSViewFontFace m_font = new KDSViewFontFace();
    String mCaptionText = "Summary";

    public KDSViewFontFace getFont() {
        return m_font;
    }

    public void setFont(KDSViewFontFace ff) {
        m_font.copyFrom(ff);
    }

    public void setRect(Rect rt) {
        m_arRects.clear();
        m_arRects.add(rt);//.addAll(ar);
    }

    static public KDSViewSumStnPanel createNew(KDSViewSumStnSumGroup group) {
        KDSViewSumStnPanel panel = new KDSViewSumStnPanel();
        panel.setSumGroup(group);
        return panel;
    }

    public void setSumGroup(KDSViewSumStnSumGroup group) {
        m_sumGroup = group;
    }

    public KDSViewSumStnSumGroup getData() {
        return m_sumGroup;
    }


    /**
     *
     * @param group
     * @param panel
     * @param rtPanel
     *  The relative rect to screen data rect.
     * @return
     */
    static public boolean build(KDSViewSumStnSumGroup group,KDSViewSumStnPanel panel, Rect rtPanel) {


        ArrayList<KDSViewSumStnPanel.KDSSize> arSizes = new ArrayList<>();
        //int nPanelInset = KDSViewSumStation.INSET_DY + KDSViewSumStation.BORDER_INSET_DY;
        int h = m_orderCaptionHeight;
        int w = rtPanel.width();
        arSizes.add(new KDSViewSumStnPanel.KDSSize(w, h)); //for caption
        Rect rtCaption = panel.getCaptionRect(); //relative to panel rect.
        int nStartY = rtCaption.bottom+1;
        for (int i = 0; i < group.items().size(); i++) {

            int itemh = KDSViewSumStnEntry.calculateNeedHeight(group.items().get(i));
            arSizes.add(new KDSViewSumStnPanel.KDSSize(w, itemh));
            KDSViewSumStnEntry rowView = panel.addItem(new KDSViewSumStnEntry(group.items().get(i)));
            rowView.setSize(arSizes.get(arSizes.size() - 1));

            rowView.setStartPointRelativeToOrderView(new Point(0, nStartY));
            nStartY += itemh;

        }
        return  true;
    }



    static int SHADOW_COLOR = 0xFFDDDDDD;

    static public void drawRoundRect(Canvas g, Rect rc, int color, boolean bRoundCorner, boolean bShadow) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(color);

        RectF rt = new RectF(rc);
        if (bRoundCorner) {
            if (bShadow) {

                RectF rtShadow = new RectF(rt);
                rtShadow.left += KDSViewSumStation.BORDER_INSET_DX;
                rtShadow.right += KDSViewSumStation.BORDER_INSET_DX;
                rtShadow.top += KDSViewSumStation.BORDER_INSET_DY;
                rtShadow.bottom += KDSViewSumStation.BORDER_INSET_DY;
                p.setColor(SHADOW_COLOR);//Color.LTGRAY);
                g.drawRoundRect(rtShadow, CanvasDC.ROUND_CORNER_DX, CanvasDC.ROUND_CORNER_DY, p);
                p.setColor(color);
                //p.setShader(shader);
                //p.setMaskFilter(filter);

            }
            g.drawRoundRect(rt, CanvasDC.ROUND_CORNER_DX, CanvasDC.ROUND_CORNER_DY, p);
        } else
            g.drawRect(rt, p);



    }

    private void drawPanel(Canvas g, KDSViewSettings env, Rect screenDataRect, int nOrderPanelIndex, KDSViewFontFace ff) {
        if (m_arRects.size() <= 0) return;
        int nbg = ff.getBG();// env.getSettings().getInt(KDSSettings.ID.Panels_BG);

        Rect rtReal = convertToAbsoluteRect(m_arRects.get(0), screenDataRect);
        //Rect rtReal = m_arRects.get(0);
        rtReal.inset(KDSViewSumStation.BORDER_INSET_DX, KDSViewSumStation.BORDER_INSET_DY);
        drawRoundRect(g, rtReal, nbg, true, true);


    }

    public void onDraw(Canvas g, KDSViewSettings env, Rect screenDataRect, int nOrderPanelIndex, KDSViewFontFace ff) {
        if (m_arRects.size() <= 0) return;
        drawPanel(g, env, screenDataRect, nOrderPanelIndex, ff);

        drawCaption(g, env, screenDataRect, nOrderPanelIndex, ff);

        for (int i = 0; i < m_arRowEntries.size(); i++) {
            m_arRowEntries.get(i).onDraw(g, env, screenDataRect, m_arRects, (i != 0), ff);
        }
    }


    protected boolean drawCaptionBG(Canvas g, KDSViewSettings env, Rect rtScreenDataArea, int nBG, int nOrderPanelIndex) {
        Rect rcBG = getCaptionBGRect();
        rcBG = convertToAbsoluteRect(rcBG, m_arRects.get(0));
        rcBG = convertToAbsoluteRect(rcBG, rtScreenDataArea);
        drawRoundRect(g, rcBG, nBG, true, false);
        //full bottom corner
        Rect rt = new Rect(rcBG);
        rt.top += CanvasDC.ROUND_CORNER_DY * 2;
        drawRoundRect(g, rt, nBG, false, false);
        return true;
    }

    protected boolean drawCaption(Canvas g, KDSViewSettings env, Rect rtScreenDataArea, int nOrderPanelIndex, KDSViewFontFace ff) {
        Rect rtCaption = getCaptionRect();

        //this.getFont().copyFrom(env.getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Normal_FontFace));

        KDSViewFontFace ffCaption = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Normal_FontFace);

        //int nCaptionBG = ff.getBG();// KDSView.getOrderCaptionBackgroundColor(order, env, this.getFont());
        int nBG = ffCaption.getBG();

        Rect rcAbsolute = convertToAbsoluteRect(rtCaption, m_arRects.get(0));
        if (rcAbsolute.right > rtScreenDataArea.width())
            rcAbsolute.right = rtScreenDataArea.width() - 2 * KDSViewSumStation.INSET_DX;// - 2*+KDSIOSView.BORDER_INSET_DX;

        rcAbsolute = convertToAbsoluteRect(rcAbsolute, rtScreenDataArea);
        drawCaptionBG(g, env, rtScreenDataArea, nBG, nOrderPanelIndex);
        Rect rcTop = new Rect(rcAbsolute);
        rcTop.bottom = rcTop.top + rcTop.height() / 2;

        drawString(g, rcTop, ff, getCaptionText(), Paint.Align.LEFT, false);

        return true;

    }

    public String getCaptionText()
    {
        return mCaptionText;
    }

    public void setCaptionText(String s)
    {
        mCaptionText = s;
    }

    protected void drawString(Canvas g, Rect rcAbsolute, KDSViewFontFace ff, String str, Paint.Align align, boolean bBold) {

        if (str.isEmpty()) return;

        //if (order.isDimColor()) ff.setBG(KDSConst.DIM_BG);
        CanvasDC.drawText_without_clear_bg(g, ff, rcAbsolute, str, align, bBold);

    }

    protected Rect convertToAbsoluteRect(Rect rtRelative, Rect rtAbsolute) {
        Rect rt = new Rect(rtRelative);

        int w = rtRelative.width();
        int h = rtRelative.height();
        rt.left += rtAbsolute.left;
        rt.top += rtAbsolute.top;
        rt.right = rt.left + w;
        rt.bottom = rt.top + h;
        return rt;

    }

    protected Rect getCaptionBGRect() {
        Rect rt = getCaptionRect();
        rt.left -= KDSViewSumStation.INSET_DX;
        rt.right += KDSViewSumStation.INSET_DX;
        rt.top -= KDSViewSumStation.INSET_DY;
        return rt;
    }

    /**
     * The relative rect to m_arRects
     *
     * @return
     */
    protected Rect getCaptionRect() {
        if (m_arRects.size() == 0) {
            return new Rect(0, 0, 0, 0);
        } else {//if (m_arRects.size() == 1) {
            Rect rt = new Rect();
            rt.left = KDSViewSumStation.INSET_DX + KDSViewSumStation.BORDER_INSET_DY;
            rt.right = m_arRects.get(0).width() - KDSViewSumStation.INSET_DX - KDSViewSumStation.BORDER_INSET_DX;
            rt.top = KDSViewSumStation.INSET_DY + KDSViewSumStation.BORDER_INSET_DY;
            rt.bottom = rt.top + m_orderCaptionHeight;
            return rt;
        }
    }

    public KDSViewSumStnEntry addItem(KDSViewSumStnEntry entry) {
        m_arRowEntries.add(entry);
        return entry;
    }

    public KDSViewSumStnEntry getItem(int nIndex) {
        return m_arRowEntries.get(nIndex);
    }

    static class KDSSize {
        int width = 0;
        int height = 0;

        public KDSSize(int w, int h) {
            width = w;
            height = h;
        }
    }

}