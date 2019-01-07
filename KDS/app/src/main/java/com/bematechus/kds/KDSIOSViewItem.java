package com.bematechus.kds;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextPaint;


import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSDataItem;
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
        int nItem = 1;
        int nCondiments = item.getCondiments().getCount();
        int h =  nmessages * m_messageHeight + nItem * m_itemTextHeight + nCondiments * m_condimentHeight;
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
    static KDSViewFontFace m_ffDescription = new KDSViewFontFace();
    static KDSViewFontFace m_ffCondiment = new KDSViewFontFace();
    static KDSViewFontFace m_ffMessage = new KDSViewFontFace();

    static Paint m_paintSeparator = new Paint();
    public void onDraw(Canvas g, KDSViewSettings env, Rect screenDataRect, ArrayList<Rect> arOrderView, boolean bDrawSeparator)
    {
        Rect rt = getAbsoluteRect(screenDataRect, arOrderView);
        rt.inset(KDSIOSView.INSET_DX, 0);
        Rect rtText = new Rect(rt);
        rtText.bottom = rtText.top + m_itemTextHeight;
        m_ffDescription.setFontSize(14);


        drawString(g, m_ffDescription, rtText, m_item.getDescription(), true);

        //draw messages
        Rect rtMessage = new Rect(rtText);
        rtMessage.top+= m_itemTextHeight;
        rtMessage.bottom = rtMessage.top + m_messageHeight;

        for (int i=0; i< m_item.getMessages().getCount(); i++)
        {
            drawString(g, m_ffMessage, rtMessage, m_item.getMessages().getMessage(i).getMessage(), false);
            rtMessage.top+= m_messageHeight ;
            rtMessage.bottom = rtMessage.top + m_messageHeight;
        }
        //draw condiments
        Rect rtCondiment = new Rect(rtMessage);
       // rtCondiment.top+= m_messageHeight;
        rtCondiment.bottom = rtCondiment.top + m_condimentHeight;
        for (int i=0; i< m_item.getCondiments().getCount(); i++)
        {
            drawString(g, m_ffCondiment, rtCondiment, m_item.getCondiments().getCondiment(i).getDescription(), false);
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
    }

    public Rect getRect()
    {
        Rect rt = new Rect(m_ptStartPoint.x, m_ptStartPoint.y, m_ptStartPoint.x + m_size.width, m_ptStartPoint.y+m_size.height);
        return rt;
    }


}
