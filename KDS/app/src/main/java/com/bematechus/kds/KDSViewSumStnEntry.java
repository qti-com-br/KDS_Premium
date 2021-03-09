package com.bematechus.kds;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSDataCategoryIndicator;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataMoreIndicator;
import com.bematechus.kdslib.KDSViewFontFace;

import java.util.ArrayList;

/**
 *
 * Summary station feature.
 * The rows in panel.
 */

public class KDSViewSumStnEntry {
    public static int m_itemTextHeight = 20;
    public static int m_messageHeight = 20;
    public static int m_condimentHeight = 20;

    KDSSummaryItem m_item = null;

    KDSViewSumStnPanel.KDSSize m_size = new KDSViewSumStnPanel.KDSSize(0,0);
    Point m_ptStartPoint = new Point();

    public KDSViewSumStnEntry(KDSSummaryItem item)
    {
        m_item = item;
    }

    public void setStartPointRelativeToOrderView(Point pt)
    {
        m_ptStartPoint.x = pt.x;
        m_ptStartPoint.y = pt.y;

    }

    public void setSize(KDSViewSumStnPanel.KDSSize size)
    {
        m_size = size;
    }
    static public int calculateNeedHeight(KDSSummaryItem item)
    {

        int nItem = 1;
        int nCondiments = item.getCondiments().size();//.getCount();
        int h =   nItem * m_itemTextHeight + nCondiments * m_condimentHeight;
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
    protected void drawString(Canvas g, KDSViewFontFace ff, Rect rcAbsolute , String str, boolean bBold)
    {

        if (str.isEmpty()) return;
        CanvasDC.drawText_without_clear_bg(g, ff, rcAbsolute, str, Paint.Align.LEFT, bBold);

    }


    static Paint m_paintSeparator = new Paint();

    /**
     *
     * @param g
     * @param env
     * @param screenDataRect
     * @param arPanelRect
     * @param bDrawSeparator
     */
    public void onDraw(Canvas g, KDSViewSettings env, Rect screenDataRect, ArrayList<Rect> arPanelRect, boolean bDrawSeparator, KDSViewFontFace ff)
    {

        //KDSViewFontFace ffDescription = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Item_Default_FontFace);
        //KDSViewFontFace ffCondiment = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Condiment_Default_FontFace);



        Rect rt = getAbsoluteRect(screenDataRect, arPanelRect);

        rt.left += KDSViewSumStation.INSET_DX;
        rt.right -= KDSViewSumStation.BORDER_INSET_DX;

        Rect rtText = new Rect(rt);
        rtText.bottom = rtText.top + m_itemTextHeight;
        String str = String.format("%dx %s", (int) m_item.getQty(), m_item.getItemDescription());

        //KDSBGFG color = KDSLayoutCell.getStateColor(m_item, env, ffDescription.getBG(), ffDescription.getFG());
        KDSBGFG color = new KDSBGFG(ff.getBG(), ff.getFG());
        CanvasDC.fillRect(g, ff.getBG(), rtText);
        //CanvasDC.fillRect(g, Color.RED, rtText);

        drawString(g, ff, rtText, str, true);

        //draw condiments
        Rect rtCondiment = new Rect(rtText);
        // rtCondiment.top+= m_messageHeight;
        rtCondiment.top += m_itemTextHeight;
        rtCondiment.bottom = rtCondiment.top + m_condimentHeight;
        for (int i=0; i< m_item.getCondiments().size(); i++)
        {
            int nCondimentQty = m_item.getCondiments().get(i).getQty();
            if (nCondimentQty <=0)
                nCondimentQty = 1;
            nCondimentQty *= m_item.getQty();

            str = String.format("        %dx %s", (int) nCondimentQty, m_item.getCondiments().get(i).getDescription());
            drawString(g, ff, rtCondiment, str, false);
            rtCondiment.top+= m_condimentHeight ;
            rtCondiment.bottom = rtCondiment.top + m_condimentHeight;
        }
        if (bDrawSeparator) {
            m_paintSeparator.setColor(Color.GRAY);
            m_paintSeparator.setStrokeWidth(1);
            g.drawLine(rt.left + KDSIOSView.INSET_DX, rt.top, rt.right - KDSIOSView.INSET_DX*2, rt.top, m_paintSeparator);
        }

    }

    public Rect getRect()
    {
        Rect rt = new Rect(m_ptStartPoint.x, m_ptStartPoint.y, m_ptStartPoint.x + m_size.width, m_ptStartPoint.y+m_size.height);
        return rt;
    }


}
