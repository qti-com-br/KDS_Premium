package com.bematechus.kds;

import android.graphics.Canvas;
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


//    /**
//     * Return order occupy rect
//     *
//     * @param ptStartShowThisOrderInScreenDataArea From which point to draw this order.Relative to screenDataRect
//     * @param nColWidth
//     * @param nColHeight
//     * @return The rects for draw this order
//     */
    static public boolean build(KDSViewSumStnSumGroup group,KDSViewSumStnPanel panel, Rect rtPanel) {


        ArrayList<KDSViewSumStnPanel.KDSSize> arSizes = new ArrayList<>();
        int nPanelInset = KDSViewSumStation.INSET_DY + KDSViewSumStation.BORDER_INSET_DY;
        int h = m_orderCaptionHeight;
        int w = rtPanel.width();
        arSizes.add(new KDSViewSumStnPanel.KDSSize(w, h)); //for caption
        Rect rtCaption = panel.getCaptionRect();
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
//        //after calculate all size data,
//        //we will check how to layout them.
//        ArrayList<Rect> ar = new ArrayList<>(); //the panel rect.
////
//        Point ptCurrentPointInScreenDataArea = new Point(ptStartShowThisOrderInScreenDataArea);
//        int nPanelInset = KDSViewSumStation.INSET_DY + KDSViewSumStation.BORDER_INSET_DY;// Math.round( (float) KDSIOSView.INSET_DY*1.5f);
//
//        Point pt = ptStartShowThisOrderInScreenDataArea;
//
//        Rect rect = new Rect();
//        rect.set(pt.x, pt.y, pt.x + nColWidth, pt.y + nColHeight);
//        ar.add(rect);

//        for (int i = 0; i < arSizes.size(); i++) {
//            KDSViewSumStnPanel.KDSSize size = arSizes.get(i);
//            KDSIOSViewItem itemView = null;
//            if (i >= nItemsStart)
//                itemView = orderView.getItem(i - nItemsStart);
//
//            if (!checkArea(size, ar, nPanelInset, ptCurrentPointInScreenDataArea, screenDataRect, nColWidth, nColHeight, itemView)) {//just adjust last rect position or size, do check again.
//                checkArea(size, ar, nPanelInset, ptCurrentPointInScreenDataArea, screenDataRect, nColWidth, nColHeight, itemView);
//
//            }
//
//        }
//
//        ptStartShowThisOrderInScreenDataArea.x = ptCurrentPointInScreenDataArea.x;
//        ptStartShowThisOrderInScreenDataArea.y = ptCurrentPointInScreenDataArea.y;
//        //merge all full height area.
//        for (int i = 1; i < ar.size(); i++) {
//            if (ar.get(i).height() >= screenDataRect.height()) {
//                ar.get(0).right = ar.get(i).right;
//            } else
//                break;
//        }
//        for (int i = ar.size() - 1; i > 0; i--) {
//            if (ar.get(i).height() >= screenDataRect.height()) {
//                ar.remove(i);
//            }
//        }
//
//        //add last rect inset
//        if (ar.size() > 0) {
//            Rect rt = ar.get(ar.size() - 1);
//            if (rt.height() != screenDataRect.height()) {
//                rt.bottom += nPanelInset;//KDSIOSView.INSET_DY;
//                if (rt.bottom > screenDataRect.height())
//                    rt.bottom = screenDataRect.height();
//                if (ptStartShowThisOrderInScreenDataArea.y != 0)
//                    ptStartShowThisOrderInScreenDataArea.y += nPanelInset;// KDSIOSView.INSET_DY;
//                if (ptStartShowThisOrderInScreenDataArea.y >= screenDataRect.height()) {
//                    ptCurrentPointInScreenDataArea.y = 0;
//                    ptCurrentPointInScreenDataArea.x += nColWidth;
//                }
//
//            }
//        }
////
////
//        return ar;


    }

//    static public Point convertRelativeOrderViewPoint(Point pt, Rect rtOrderView) {
//        Point ptRelative = new Point(pt);
//        ptRelative.x = pt.x - rtOrderView.left;
//        ptRelative.y = pt.y - rtOrderView.top;
//        return ptRelative;
//    }
//
//    static private Rect createRect(Point ptStart, KDSViewSumStnPanel.KDSSize size) {
//        return new Rect(ptStart.x, ptStart.y, ptStart.x + size.width, ptStart.y + size.height);
//
//    }

    /**
     * @param size
     * @param arAreas
     * @param nInset                         inset at top and bottom of rects
     * @param ptCurrentPointInScreenDataArea relative to screenDataRect.
     * @param screenDataRect                 The absolute rect.
     * @param nColWidth
     * @param nColHeight
     * @param itemView
     * @return
     */
//    static private boolean checkArea(KDSIOSViewOrder.KDSSize size, ArrayList<Rect> arAreas, int nInset, Point ptCurrentPointInScreenDataArea, Rect screenDataRect, int nColWidth, int nColHeight, KDSIOSViewItem itemView) {
//
//        int w = nColWidth;
//
//        if (arAreas.size() <= 0) { //no area in it. This can been sure it is not item, it is caption.
//            //ptCurrentPointInScreenDataArea.y += nInset;
//            arAreas.add(new Rect(ptCurrentPointInScreenDataArea.x, ptCurrentPointInScreenDataArea.y, ptCurrentPointInScreenDataArea.x + size.width, ptCurrentPointInScreenDataArea.y + size.height + nInset));
//            ptCurrentPointInScreenDataArea.y += size.height + nInset;
//            return true;
//        } else {
//            //there is one area existed
//            if (ptCurrentPointInScreenDataArea.y + size.height <= screenDataRect.height()) {
//                //there is room for this data.
//                Rect rtData = arAreas.get(arAreas.size() - 1);
//                rtData.bottom += size.height;
//                if (itemView != null) {
//                    Point pt = new Point(ptCurrentPointInScreenDataArea);
//                    pt.x += KDSIOSView.INSET_DX;
//
//                    itemView.setStartPointRelativeToOrderView(convertRelativeOrderViewPoint(pt, arAreas.get(0)));
//                }
//                ptCurrentPointInScreenDataArea.y += size.height;
//                return true;
//
//            } else {
//                //the room is not enough for this data
//                Rect rtData = arAreas.get(arAreas.size() - 1); //last area
//                if (rtData.top == 0) //it from toppest point
//                {//no room for this data. Expand to bottom last one, add new area
//
//                    rtData.bottom = screenDataRect.height(); //it last one is full height, rtData is relative to screenDataRect
//                    //add new rect for next.
//                    ptCurrentPointInScreenDataArea.x += w;//+ KDSIOSView.INSET_DX;
//                    ptCurrentPointInScreenDataArea.y = 0;//m_orderCaptionHeight; //new column
//                    arAreas.add(new Rect(ptCurrentPointInScreenDataArea.x, ptCurrentPointInScreenDataArea.y, ptCurrentPointInScreenDataArea.x + w, ptCurrentPointInScreenDataArea.y + size.height + m_orderCaptionHeight + nInset));
//                    //set current point to new position.
//                    ptCurrentPointInScreenDataArea.y += m_orderCaptionHeight + nInset;
//                    if (itemView != null) {
//                        Point pt = new Point(ptCurrentPointInScreenDataArea);
//                        pt.x += KDSIOSView.INSET_DX;
//                        itemView.setStartPointRelativeToOrderView(convertRelativeOrderViewPoint(pt, arAreas.get(0)));
//                    }
//                    ptCurrentPointInScreenDataArea.y += size.height;
//                    return true;
//                } else {//it is partial height area, full height last one, add a new area
//                    //1. move ptcurrent to new position
//                    ptCurrentPointInScreenDataArea.y -= rtData.top;
//                    ptCurrentPointInScreenDataArea.x += w;//+ KDSIOSView.INSET_DX;
//                    //move last one to new col
//                    int h = rtData.height();
//                    rtData.left += w;//+ KDSIOSView.INSET_DX;
//                    rtData.right = rtData.left + w;
//                    rtData.top = 0;
//                    rtData.bottom = h;//screenDataRect.height();
//                    return false; //need to check again.
//
//                }
//
//            }
//        }
//    }


    static int SHADOW_COLOR = 0xFFDDDDDD;

    static public void drawRoundRect(Canvas g, Rect rc, int color, boolean bRoundCorner, boolean bShadow) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(color);

        RectF rt = new RectF(rc);
        if (bRoundCorner) {
            if (bShadow) {

                RectF rtShadow = new RectF(rt);
                rtShadow.left += 5;
                rtShadow.right += 5;
                rtShadow.top += 5;
                rtShadow.bottom += 5;
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

    private void drawPanel(Canvas g, KDSViewSettings env, Rect screenDataRect, int nOrderPanelIndex) {
        if (m_arRects.size() <= 0) return;
        int nbg = env.getSettings().getInt(KDSSettings.ID.Panels_BG);
        if (m_arRects.size() == 1) { //it is only one rect.
            Rect rtReal = convertToAbsoluteRect(m_arRects.get(0), screenDataRect);
            rtReal.inset(KDSViewSumStation.BORDER_INSET_DX, KDSViewSumStation.BORDER_INSET_DY);
            drawRoundRect(g, rtReal, nbg, true, true);
        } else {
//            for (int i = 0; i < m_arRects.size(); i++) {
//                Rect rtReal = convertToAbsoluteRect(m_arRects.get(i), screenDataRect);
//                rtReal.inset(KDSIOSView.BORDER_INSET_DX, KDSIOSView.BORDER_INSET_DY);
//                if (i == m_arRects.size() - 1) //last one
//                {
//
//                    rtReal.left = rtReal.left - 2 * KDSIOSView.BORDER_INSET_DX - 2 * CanvasDC.ROUND_CORNER_DX;
//                }
//
//                drawRoundRect(g, rtReal, nbg, true, true);
//            }
//            //fill last round corner
//
//            Rect rt = m_arRects.get(m_arRects.size() - 1);
//            Rect r = new Rect(rt);
//            //r.inset(KDSIOSView.BORDER_INSET_DX, KDSIOSView.BORDER_INSET_DY);
////            //left-top corner
////
////            r.left -= KDSIOSView.ROUND_CORNER_DX *2;
////            r.right = r.left + KDSIOSView.ROUND_CORNER_DX *3;
////            r.bottom = r.top + KDSIOSView.ROUND_CORNER_DX *2;
////            r = convertToAbsoluteRect(r, screenDataRect);
////            drawRoundRect(g, r,nbg, false );
//            //left-bottom corner
//            r.right = r.left - KDSIOSView.BORDER_INSET_DX;
//            r.left = rt.left - 2 * KDSIOSView.BORDER_INSET_DX - 2 * CanvasDC.ROUND_CORNER_DX;
//
//            r.top = rt.bottom - CanvasDC.ROUND_CORNER_DX * 2;
//            r.bottom = rt.bottom;//-KDSIOSView.BORDER_INSET_DX ;
//            r = convertToAbsoluteRect(r, screenDataRect);
//            drawRoundRect(g, r, nbg, false, false);
//
////            //draw border for out of screen part. As the order were out screen, we draw something.
////            Rect rtLast = m_arRects.get(m_arRects.size()-1);
////            if (!screenDataRect.contains(rtLast.left, rtLast.top) )
////            {
////                rtLast = new Rect ( m_arRects.get(0) );
////                rtLast.right = screenDataRect.right;
////                int nViewBg = env.getSettings().getInt(KDSSettings.ID.Panels_View_BG);
////                rtLast.top += m_premessageHeight + (rtLast.height()-m_premessageHeight)/3;
////                drawRoundRect(g, rtLast);
////
////            }

        }

    }

    private int getColWidth() {
        if (m_arRects.size() <= 0)
            return 0;
        //if (m_arRects.size() == 1)
            return m_arRects.get(0).width();
        //else
        //    return m_arRects.get(m_arRects.size() - 1).width();

    }

//    public Rect getEntryRect(Rect screenDataRect, int nEntryIndex)
//    {
//        Rect rtCaption = getCaptionRect(screenDataRect);
//        for (int i=0; i< nEntryIndex; i++)
//        {
//            m_arRowEntries.get(i).getRect()
//        }
//
//    }
    //static KDSViewFontFace m_ffMessage = new KDSViewFontFace();

    public void onDraw(Canvas g, KDSViewSettings env, Rect screenDataRect, int nOrderPanelIndex) {
//        for (int i=0;i < m_arRects.size(); i++) {
//
//            CanvasDC.fillRect(g, color,convertToAbsoluteRect(m_arRects.get(i), screenDataRect) );
//        }
        if (m_arRects.size() <= 0) return;
        drawPanel(g, env, screenDataRect, nOrderPanelIndex);

        drawCaption(g, env, screenDataRect, nOrderPanelIndex);
        //drawMessages(g, env, screenDataRect, nOrderPanelIndex);

        int nLastGroupID = -1;
        for (int i = 0; i < m_arRowEntries.size(); i++) {
            nLastGroupID = m_arRowEntries.get(i).onDraw(g, env, screenDataRect, m_arRects, (i != 0), nLastGroupID);
        }
    }

//    protected boolean drawMessages(Canvas g, KDSViewSettings env, Rect rtScreenDataArea, int nOrderPanelIndex) {
//        if (m_arRects.size() <= 0) return false;
//        Rect rtCaption = getCaptionRect(rtScreenDataArea);
//        Rect rtMessage = new Rect(rtCaption);
//
//        //rtMessage.left += KDSIOSView.INSET_DX;
//        rtMessage.right = rtMessage.left + getColWidth() - KDSIOSView.INSET_DX - KDSIOSView.BORDER_INSET_DX;
//        rtMessage.top = rtCaption.bottom;
//        rtMessage.bottom = rtMessage.top + m_premessageHeight;
//
//        rtMessage = convertToAbsoluteRect(rtMessage, m_arRects.get(0));
//        rtMessage = convertToAbsoluteRect(rtMessage, rtScreenDataArea);
//
//        for (int i = 0; i < m_order.getOrderMessages().getCount(); i++) {
//
//            drawString(g, rtMessage, m_ffMessage, m_order.getOrderMessages().getMessage(i).getMessage(), Paint.Align.LEFT, false);
//            rtMessage.top = rtMessage.bottom;
//            rtMessage.bottom = rtMessage.top + m_premessageHeight;
//        }
//        return true;
//    }

    protected boolean drawCaptionBG(Canvas g, KDSViewSettings env, Rect rtScreenDataArea, int nBG, int nOrderPanelIndex) {
        Rect rcBG = getCaptionBGRect(rtScreenDataArea);
        rcBG = convertToAbsoluteRect(rcBG, m_arRects.get(0));
        rcBG = convertToAbsoluteRect(rcBG, rtScreenDataArea);
        drawRoundRect(g, rcBG, nBG, true, false);
        //full bottom corner
        Rect rt = new Rect(rcBG);
        rt.top += CanvasDC.ROUND_CORNER_DY * 2;
        drawRoundRect(g, rt, nBG, false, false);
        return true;
    }

    protected boolean drawCaption(Canvas g, KDSViewSettings env, Rect rtScreenDataArea, int nOrderPanelIndex) {
        Rect rtCaption = getCaptionRect();

        //KDSDataOrder order = m_order;//

        this.getFont().copyFrom(env.getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Normal_FontFace));

        KDSViewFontFace ff = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Normal_FontFace);

        int nCaptionBG = ff.getBG();// KDSView.getOrderCaptionBackgroundColor(order, env, this.getFont());
        int nBG = nCaptionBG;
        //
//        if (env.getStateValues().isFocusedOrderGUID(order.getGUID())) {
//
//            int focusBorderBG = env.getSettings().getInt(KDSSettings.ID.Focused_BG);//.getKDSViewFontFace(KDSSettings.ID.Order_Focused_FontFace).getBG();// this.getFont().getBG();
//            if (!env.getSettings().getBoolean(KDSSettings.ID.Blink_focus))
//                nBG = focusBorderBG;
//            else {
//                if (KDSGlobalVariables.getBlinkingStep())
//                    nBG = focusBorderBG;
//                else
//                    nBG = nCaptionBG;
//            }
//        } else {
//            nBG = nCaptionBG;

//        }

        //Rect rcAbsolute = new Rect(rtCaption);

        Rect rcAbsolute = convertToAbsoluteRect(rtCaption, m_arRects.get(0));
        if (rcAbsolute.right > rtScreenDataArea.width())
            rcAbsolute.right = rtScreenDataArea.width() - 2 * KDSViewSumStation.INSET_DX;// - 2*+KDSIOSView.BORDER_INSET_DX;

        rcAbsolute = convertToAbsoluteRect(rcAbsolute, rtScreenDataArea);

        drawCaptionBG(g, env, rtScreenDataArea, nBG, nOrderPanelIndex);

//        Object first_l = KDSLayoutCell.getOrderContentObject(order, KDSSettings.TitlePosition.Left, KDSLayoutCell.CellSubType.Unknown, env);
//        Object first_r = KDSLayoutCell.getOrderContentObject(order, KDSSettings.TitlePosition.Right, KDSLayoutCell.CellSubType.Unknown, env);
//        Object first_c = KDSLayoutCell.getOrderContentObject(order, KDSSettings.TitlePosition.Center, KDSLayoutCell.CellSubType.Unknown, env);
//
//        Object second_l = KDSLayoutCell.getOrderContentObject(order, KDSSettings.TitlePosition.Left, KDSLayoutCell.CellSubType.OrderTitle_Second, env);
//        Object second_r = KDSLayoutCell.getOrderContentObject(order, KDSSettings.TitlePosition.Right, KDSLayoutCell.CellSubType.OrderTitle_Second, env);
//        Object second_c = KDSLayoutCell.getOrderContentObject(order, KDSSettings.TitlePosition.Center, KDSLayoutCell.CellSubType.OrderTitle_Second, env);
//
//        KDSViewFontFace fontDef = new KDSViewFontFace();
//        fontDef.copyFrom(this.getFont());
//        fontDef.setBG(nBG);

//        if (order.getCookState() == KDSDataOrder.CookState.Started) {
//
//            Drawable drawable = env.getSettings().getOrderCookStartedImage();
//            drawable.setBounds(rcAbsolute.left + KDSConst.IMAGE_GAP, rcAbsolute.top + KDSConst.IMAGE_GAP, rcAbsolute.left + rcAbsolute.height() - KDSConst.IMAGE_GAP, rcAbsolute.bottom - KDSConst.IMAGE_GAP);//, rcAbsolute.height());
//
//            drawable.draw(g);
//            rcAbsolute.left += rcAbsolute.height() + KDSConst.IMAGE_GAP;
//        }
//        if (order.isDimColor())
//            fontDef.setBG(KDSConst.DIM_BG);
        Rect rcTop = new Rect(rcAbsolute);
        rcTop.bottom = rcTop.top + rcTop.height() / 2;

        Rect rcBottom = new Rect(rcAbsolute);
        rcBottom.top = rcBottom.top + rcBottom.height() / 2;
        drawString(g, rcTop, ff, getCaptionText(), Paint.Align.LEFT, false);

//        if (first_l != null && (first_l instanceof String))
//            drawString(g, rcTop, fontDef, (String) first_l, Paint.Align.LEFT, false);
//        if (first_c != null && (first_c instanceof String))
//            drawString(g, rcTop, fontDef, (String) first_c, Paint.Align.CENTER, false);
//        if (first_r != null && (first_r instanceof String))
//            drawString(g, rcTop, fontDef, (String) first_r, Paint.Align.RIGHT, false);
//
//        if (second_l != null && (second_l instanceof String))
//            drawString(g, rcBottom, fontDef, (String) second_l, Paint.Align.LEFT, false);
//
//        if (second_c != null && (second_c instanceof String))
//            drawString(g, rcBottom, fontDef, (String) second_c, Paint.Align.CENTER, false);
//
//        if (second_r != null && (second_r instanceof String))
//            drawString(g, rcBottom, fontDef, (String) second_r, Paint.Align.RIGHT, false);

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

    protected Rect getCaptionBGRect(Rect rtScreenDataArea) {
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
            Rect rt = new Rect(m_arRects.get(0));
            rt.left = KDSViewSumStation.INSET_DX + KDSViewSumStation.BORDER_INSET_DY;
            rt.right = m_arRects.get(0).width() - KDSViewSumStation.INSET_DX - KDSViewSumStation.BORDER_INSET_DX;
            rt.top = KDSViewSumStation.INSET_DY + KDSViewSumStation.BORDER_INSET_DY;
            rt.bottom = rt.top + m_orderCaptionHeight;
            return rt;
        }
//        else {
//            Rect rt = new Rect(m_arRects.get(0));
//            rt.top = KDSIOSView.INSET_DY + KDSIOSView.BORDER_INSET_DY;
//            rt.bottom = rt.top + m_orderCaptionHeight;
//            rt.left = KDSIOSView.INSET_DX + KDSIOSView.BORDER_INSET_DX;
//            Rect last = m_arRects.get(m_arRects.size() - 1);
//            Rect first = m_arRects.get(0);
//            rt.right = rt.left + last.right - first.left;// - 2 * KDSIOSView.INSET_DX - 2*+KDSIOSView.BORDER_INSET_DX;
////            if (rt.right > rtScreenDataArea.width())
////                rt.right = rtScreenDataArea.width();
//            rt.right = rt.right - 2 * KDSIOSView.INSET_DX - 2 * +KDSIOSView.BORDER_INSET_DX;
//
//            return rt;
//        }
    }

    public KDSViewSumStnEntry addItem(KDSViewSumStnEntry entry) {
        m_arRowEntries.add(entry);
        return entry;
    }

    public KDSViewSumStnEntry getItem(int nIndex) {
        return m_arRowEntries.get(nIndex);
    }

//    /**
//     * @param x absolute point
//     * @param y
//     * @return
//     */
//    public boolean pointInMe(Rect rtScreenDataRect, int x, int y) {
//        if (!rtScreenDataRect.contains(x, y))
//            return false;
//        for (int i = 0; i < m_arRects.size(); i++) {
//            Rect rt = m_arRects.get(i);
//            rt = convertToAbsoluteRect(rt, rtScreenDataRect);
//            if (rt.contains(x, y))
//                return true;
//        }
//        return false;
//    }

//    public KDSIOSViewItem getTouchedItem(Rect rtScreenDataRect, int x, int y) {
//        if (!rtScreenDataRect.contains(x, y))
//            return null;
//        for (int i = 0; i < m_arItemsPanels.size(); i++) {
//            KDSIOSViewItem itemView = m_arItemsPanels.get(i);
//            Rect rt = itemView.getRect();
//            rt = convertToAbsoluteRect(rt, m_arRects.get(0));
//            rt = convertToAbsoluteRect(rt, rtScreenDataRect);
//            if (rt.contains(x, y))
//                return itemView;
//        }
//        return null;
//    }

//    public Object getFirstBlockFirstRowData() {
//        return m_order;
//    }

    static class KDSSize {
        int width = 0;
        int height = 0;

        public KDSSize(int w, int h) {
            width = w;
            height = h;
        }
    }

}