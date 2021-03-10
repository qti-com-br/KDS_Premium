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
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;

import java.util.ArrayList;

/**
 *
 * Summary station feature.
 * The rows in panel.
 */

public class KDSViewSumStnEntry {
//    public static int m_itemTextHeight = 20;
//    public static int m_messageHeight = 20;
//    public static int m_condimentHeight = 20;

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
    static public int calculateNeedHeight(KDSSummaryItem item, int nRowHeight)
    {

        int nItem = 1;
        int nCondiments = item.getCondiments().size();//.getCount();
        int h =   nItem * nRowHeight + nCondiments * nRowHeight;
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
    public void onDraw(Canvas g, KDSViewSettings env, Rect screenDataRect,
                       ArrayList<Rect> arPanelRect, boolean bDrawSeparator, KDSViewFontFace ff,
                       int nRowHeight)
    {

        Rect rt = getAbsoluteRect(screenDataRect, arPanelRect);

        rt.left += KDSViewSumStation.INSET_DX;
        rt.right -= KDSViewSumStation.BORDER_INSET_DX;

        Rect rtText = new Rect(rt);
        rtText.bottom = rtText.top + nRowHeight;// m_itemTextHeight;
        boolean bRightQty = env.getSettings().getBoolean(KDSSettings.ID.Sumstn_right_qty);

        String str = String.format("%dx %s", (int) m_item.getQty(), m_item.getItemDescription());
        if (bRightQty)
            str = String.format("%s", m_item.getItemDescription());

        CanvasDC.fillRect(g, ff.getBG(), rtText);
        rtText.left += KDSViewSumStation.INSET_DX;
        rtText.right -= KDSViewSumStation.INSET_DX;
        drawString(g, ff, rtText, str, true);
        if (bRightQty)
            drawRightQtyString(g, ff, rtText, " x" + KDSUtil.convertIntToString((long)m_item.getQty()), true);

        //draw condiments
        Rect rtCondiment = new Rect(rtText);
        // rtCondiment.top+= m_messageHeight;
        rtCondiment.top += nRowHeight;//m_itemTextHeight;
        rtCondiment.bottom = rtCondiment.top + nRowHeight;// m_condimentHeight;
        String condimentPrefix = "        ";
        for (int i=0; i< m_item.getCondiments().size(); i++)
        {
            int nCondimentQty = m_item.getCondiments().get(i).getQty();
            if (nCondimentQty <=0)
                nCondimentQty = 1;
            nCondimentQty *= m_item.getQty();

            str = String.format(condimentPrefix+"%dx %s", (int) nCondimentQty, m_item.getCondiments().get(i).getDescription());
            if (bRightQty)
                str = String.format(condimentPrefix + "%s", m_item.getCondiments().get(i).getDescription());
            drawString(g, ff, rtCondiment, str, false);
            if (bRightQty)
                drawRightQtyString(g, ff, rtCondiment, " x" + KDSUtil.convertIntToString((long)nCondimentQty), false);
            rtCondiment.top+= nRowHeight;//m_condimentHeight ;
            rtCondiment.bottom = rtCondiment.top + nRowHeight;// m_condimentHeight;
        }
        if (bDrawSeparator) {
            m_paintSeparator.setColor(Color.GRAY);
            m_paintSeparator.setStrokeWidth(1);
            g.drawLine(rt.left + KDSIOSView.INSET_DX, rt.top, rt.right - KDSIOSView.INSET_DX*2, rt.top, m_paintSeparator);
        }

    }

    protected void drawRightQtyString(Canvas g, KDSViewFontFace ff, Rect rcAbsolute , String str, boolean bBold)
    {

        if (str.isEmpty()) return;
        int nWidth = CanvasDC.getTextPixelsWidth(ff, str);
        Rect rt = new Rect(rcAbsolute);
        rt.left = rt.right - nWidth - KDSViewSumStation.INSET_DX;
        CanvasDC.fillRect(g, ff.getBG(), rt );
        CanvasDC.drawText_without_clear_bg(g, ff, rcAbsolute, str, Paint.Align.RIGHT, bBold);

    }
//    public Rect getRect()
//    {
//        Rect rt = new Rect(m_ptStartPoint.x, m_ptStartPoint.y, m_ptStartPoint.x + m_size.width, m_ptStartPoint.y+m_size.height);
//        return rt;
//    }


}
