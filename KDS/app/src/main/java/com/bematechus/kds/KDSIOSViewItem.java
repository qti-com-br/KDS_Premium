package com.bematechus.kds;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextPaint;


import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSDataCategoryIndicator;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataMoreIndicator;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;

import java.util.ArrayList;

/**
 * Created by David.Wong on 2018/12/27.
 * Rev:
 */
public class KDSIOSViewItem {

    public static int m_itemTextHeight = 20;
    public static int m_messageHeight = 20;
    public static int m_condimentHeight = 20;

    KDSDataItem m_item = null;

    KDSIOSViewOrder.KDSSize m_size = new KDSIOSViewOrder.KDSSize(0,0);
    Rect m_rtRelativeToOrderView = new Rect();

    Point m_ptStartPoint = new Point();

    public KDSIOSViewItem(KDSDataItem item)
    {
        m_item = item;
    }

    public void setStartPointRelativeToOrderView(Point pt)
    {
        m_ptStartPoint.x = pt.x;
        m_ptStartPoint.y = pt.y;

    }

    public void setRectRelativeToOrderView(Rect rt)
    {
        m_rtRelativeToOrderView = rt;
    }

    public void setSize(KDSIOSViewOrder.KDSSize size)
    {
        m_size = size;
    }
    static public int calculateNeedHeight(KDSDataItem item)
    {
        int nmessages = item.getMessages().getCount();
        int nmodifiers = item.getModifiers().getCount();
        int nItem = 1;
        int nCondiments = item.getCondiments().getCount();

        int h =  nmessages * m_messageHeight + nmodifiers * m_messageHeight +  nItem * m_itemTextHeight + nCondiments * m_condimentHeight;
        return h;



    }

    private Rect getAbsoluteRect(Rect screenDataRect, ArrayList<Rect> arOrderView)
    {
        Rect rt = new Rect();
        if (arOrderView.size()<=0)
            return rt;
        rt.left =  arOrderView.get(0).left + m_ptStartPoint.x + screenDataRect.left;
        rt.top =   arOrderView.get(0).top + m_ptStartPoint.y + screenDataRect.top;
        rt.right = rt.left + m_size.width;
        rt.bottom = rt.top + m_size.height;
        return rt;
    }
    protected void drawString(Canvas g, KDSViewFontFace ff,Rect rcAbsolute , String str, boolean bBold)
    {

        if (str.isEmpty()) return;

        //if (order.isDimColor()) ff.setBG(KDSConst.DIM_BG);
        CanvasDC.drawText_without_clear_bg(g, ff, rcAbsolute, str, Paint.Align.LEFT, bBold);

    }
//    static KDSViewFontFace m_ffDescription = new KDSViewFontFace();
//    static KDSViewFontFace m_ffCondiment = new KDSViewFontFace();
//    static KDSViewFontFace m_ffMessage = new KDSViewFontFace();

    static Paint m_paintSeparator = new Paint();

    /**
     *
     * @param g
     * @param env
     * @param screenDataRect
     * @param arOrderView
     * @param bDrawSeparator
     * @param nLastGroupID
     * @return
     *  return last group id
     */
    public int onDraw(Canvas g, KDSViewSettings env, Rect screenDataRect, ArrayList<Rect> arOrderView, boolean bDrawSeparator, int nLastGroupID)
    {

        KDSViewFontFace ffDescription = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Item_Default_FontFace);
        KDSViewFontFace ffCondiment = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Condiment_Default_FontFace);
        KDSViewFontFace ffMessage = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Message_Default_FontFace);


        Rect rt = getAbsoluteRect(screenDataRect, arOrderView);
        //rt.inset(KDSIOSView.INSET_DX, 0);
        rt.left += KDSIOSView.INSET_DX;
        rt.right -= KDSIOSView.BORDER_INSET_DX;
        rt.right -= KDSIOSView.INSET_DX*2;
        Rect rtText = new Rect(rt);

        if (m_item instanceof KDSDataMoreIndicator)
        {
            rtText.bottom = rtText.top + m_itemTextHeight;
            KDSLayoutCell.drawMoreIndicator(g, rtText, env);
            return nLastGroupID;
        }
        else if (m_item instanceof KDSDataCategoryIndicator)
        {
            rtText.bottom = rtText.top + m_itemTextHeight;
            KDSLayoutCell.drawCategoryIndicator( g, rtText,env, (KDSDataCategoryIndicator)m_item);
            return nLastGroupID;
        }

        if (m_item.getAddOnGroup() != nLastGroupID) {
            nLastGroupID = m_item.getAddOnGroup();
            rtText = drawAddon(g, env, rtText, ffMessage);
            //show add-on
//            String addon = "Add-on";
//            rtText.bottom = rtText.top + m_messageHeight;
//            drawString(g, ffMessage, rtText,addon, false);
//            rtText.top = rtText.bottom;
//            rtText.bottom+= m_messageHeight;
        }

        //draw description
        if (!m_item.getHidden() ) {

            rtText.bottom = rtText.top + m_itemTextHeight;
            String str = String.format("%dx %s", (int) m_item.getShowingQty(), m_item.getDescription());

            KDSBGFG color = KDSLayoutCell.getStateColor(m_item, env, ffDescription.getBG(), ffDescription.getFG());

            CanvasDC.fillRect(g, color.getBG(), rtText);
            //CanvasDC.fillRect(g, Color.RED, rtText);
            rtText = KDSLayoutCell.drawItemState(g, m_item, rtText, env, color, m_itemTextHeight, ffDescription);

            int noldbg = ffDescription.getBG();
            int noldfg = ffDescription.getFG();

            ffDescription.setFG(color.getFG());
            ffDescription.setBG(color.getBG());
            drawString(g, ffDescription, rtText, str, true);
            if (m_item.isQtyChanged())
            {
                drawVoidMessage(g, env, rtText, ffMessage, color);
            }
            ffDescription.setFG(noldfg);
            ffDescription.setBG(noldbg);
        }
        //draw messages
        Rect rtMessage = new Rect(rtText);
        rtMessage.top+= m_itemTextHeight;
        rtMessage.bottom = rtMessage.top + m_messageHeight;

        for (int i=0; i< m_item.getMessages().getCount(); i++)
        {
            drawString(g, ffMessage, rtMessage, m_item.getMessages().getMessage(i).getMessage(), false);
            rtMessage.top+= m_messageHeight ;
            rtMessage.bottom = rtMessage.top + m_messageHeight;
        }

        //draw modifiers
        Rect rtModifier = new Rect(rtMessage);
        rtModifier.bottom = rtModifier.top + m_messageHeight;
        for (int i=0; i< m_item.getModifiers().getCount(); i++)
        {
            drawString(g, ffMessage, rtModifier, m_item.getModifiers().getModifier(i).getDescription(), false);
            rtModifier.top+= m_messageHeight ;
            rtModifier.bottom = rtModifier.top + m_messageHeight;
        }


        //draw condiments
        Rect rtCondiment = new Rect(rtModifier);
       // rtCondiment.top+= m_messageHeight;
        rtCondiment.bottom = rtCondiment.top + m_condimentHeight;
        for (int i=0; i< m_item.getCondiments().getCount(); i++)
        {
            drawString(g, ffCondiment, rtCondiment, m_item.getCondiments().getCondiment(i).getDescription(), false);
            rtCondiment.top+= m_condimentHeight ;
            rtCondiment.bottom = rtCondiment.top + m_condimentHeight;
        }
        if (bDrawSeparator) {
            m_paintSeparator.setColor(Color.GRAY);
            m_paintSeparator.setStrokeWidth(1);
            g.drawLine(rt.left + KDSIOSView.INSET_DX, rt.top, rt.right - KDSIOSView.INSET_DX*2, rt.top, m_paintSeparator);
        }
        //CanvasDC.drawBox( g, rt, Color.RED, 1);
        //CanvasDC.drawText(g, new KDSViewFontFace(), rt, m_item.getDescription());
        return nLastGroupID;
    }

    private Rect drawAddon(Canvas g, KDSViewSettings env, Rect rect, KDSViewFontFace ff )
    {
        //show add-on
        String addon = "Add-on";
        rect.bottom = rect.top + m_messageHeight;
        drawString(g, ff, rect,addon, false);
        rect.top = rect.bottom;
        rect.bottom+= m_messageHeight;
        return rect;
    }
    /**
     *
     * @param g
     * @param env
     * @param rect
     *  The pre-content occupy this rect.
     * @return
     */
    private Rect drawVoidMessage(Canvas g, KDSViewSettings env, Rect rect, KDSViewFontFace ff, KDSBGFG itemStateColor )
    {
        int n = env.getSettings().getInt(KDSSettings.ID.Void_showing_method);
        KDSSettings.VoidShowingMethod method = KDSSettings.VoidShowingMethod.values()[n];
        Rect rt = new Rect(rect);
        switch (method)
        {
            case Direct_Qty:
                if (env.getSettings().getBoolean(KDSSettings.ID.Void_add_message_enabled))
                {
                    rt.top = rt.bottom;
                    rt.bottom = rt.top + m_messageHeight;

                    CanvasDC.fillRect(g, ff.getBG(), rt);

                    String strDescription = env.getSettings().getString(KDSSettings.ID.Void_add_message);
                    String s =  KDSLayoutCell.getSpaces(2);
                    strDescription = s + strDescription;

                    CanvasDC.drawText(g, ff, rt, strDescription, Paint.Align.LEFT);

                }
                break;
            case Add_void:
            {
                rt.top = rt.bottom;
                rt.bottom = rt.top + m_messageHeight;
                CanvasDC.fillRect(g, itemStateColor.getBG(), rt);

                float qty = m_item.getChangedQty();
                String strDescription = m_item.getDescription();

                String s = KDSLayoutCell.getSpaces(2);
                String strQty = KDSLayoutCell.getVoidItemQtyString(env, qty);//  Integer.toString(Math.abs((int) qty));

                s += strQty;//Integer.toString((int) qty);
                s += KDSLayoutCell.getSpaces(3);
                s += strDescription;

                CanvasDC.drawText(g, ff, rt, s, Paint.Align.LEFT);

            }
            break;

        }
        return rt;
    }
    public Rect getRect()
    {
        Rect rt = new Rect(m_ptStartPoint.x, m_ptStartPoint.y, m_ptStartPoint.x + m_size.width, m_ptStartPoint.y+m_size.height);
        return rt;
    }


}
