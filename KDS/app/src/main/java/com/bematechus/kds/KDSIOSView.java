package com.bematechus.kds;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Size;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSLog;

import java.util.ArrayList;

/**
 * Created by David.Wong on 2018/12/27.
 * Rev:
 */
public class KDSIOSView extends KDSView {

    ArrayList<KDSIOSViewOrder> m_arIOSOrdersView = new ArrayList<>();
    Point m_ptNextStartPointInScreenDataArea = new Point(0,0);

    /*************************************/

    static public final int ROUND_CORNER_DX = 10;
    static public final int ROUND_CORNER_DY = 10;
    static public final int INSET_DY = 10;
    static public final int INSET_DX = 10;
    static public final int BORDER_INSET_DX = 5;
    static public final int BORDER_INSET_DY = 5;

    /**************************************/

    public KDSIOSView(Context context)
    {
        super(context);
    }
    public KDSIOSView(Context context, AttributeSet attrs)
    {
        super(context, attrs);


    }

    public KDSIOSView(Context context, AttributeSet attrs, int defaultStyle)
    {
        super(context, attrs, defaultStyle);


    }
    @Override
    protected void onDraw(Canvas canvas) {

        if (useSupperFunction()) {
            super.onDraw(canvas);
            return;
        }

        if (m_bDrawing) return;
        m_bDrawing = true;
        try {

//            if (getOrdersViewMode() == OrdersViewMode.iOS_Like) {
//                if (m_bJustRedrawTimer && (!m_bForceFullDrawing)) {
//
//                    Canvas g = get_double_buffer();
//                    //int ncount = panelsGetCount();
//                    for (int i = 0; i < m_arOrdersPanels.size(); i++) {
//                        m_arOrdersPanels.get(i).onJustDrawCaptionAndFooter(g, i);
//                        //m_arPanels.get(i).onJustDrawCaptionAndFooter(g, i);
//                    }
//                    commit_double_buffer(canvas);
//                    m_bJustRedrawTimer = false;
//                } else {
                    drawMe_DoubleBuffer(canvas);
                    m_bForceFullDrawing = false;
                //}
//            }
        }
        catch(Exception err)
        {
            //KDSLog.e(TAG, err.toString());
            KDSLog.e(TAG, KDSLog._FUNCLINE_() , err);
        }



        m_bDrawing = false;
        //fireViewAfterDrawing();


    }

    private boolean useSupperFunction()
    {
//        if (KDSConst._DEBUG)
//            return false;
//        else {

            if ( getOrdersViewMode() == OrdersViewMode.LineItems) {
                return true;
            }

            KDSSettings.LayoutFormat layoutFormat = getEnv().getSettingLayoutFormat();
            if (layoutFormat != KDSSettings.LayoutFormat.iOS_Like)
            {
                return true;
            }
            return false;
//        }
    }

    public int panelsGetCount()
    {
        if (useSupperFunction())
            return super.panelsGetCount();
        else
            return m_arIOSOrdersView.size();
    }
    protected void drawMe_DoubleBuffer(Canvas canvas)
    {
        if (useSupperFunction()) {
            super.drawMe_DoubleBuffer(canvas);
            return;
        }


        Canvas g = get_double_buffer();
        if (g == null) return;


        if (getSettings() == null) return;
        int bg =  getSettings().getInt(KDSSettings.ID.Panels_View_BG );
        g.drawColor(bg);
        Rect screenDataRect = ios_getDataArea();
        int ncount = panelsGetCount();
        for (int i=0; i< ncount; i++)
        {
            m_arIOSOrdersView.get(i).onDraw(g,getEnv(),screenDataRect, i);
        }

        if (m_bHighLight)
        {//this view is hightlight in multiple users mode
            // KDSViewFontFace ff =  getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Focused_FontFace);
            //int hightlightBg = ff.getBG();
            int hightlightBg =getSettings().getInt(KDSSettings.ID.Focused_BG);

            Rect rtHightLight = new Rect(0, g.getHeight()-3, g.getWidth(), g.getHeight());
            CanvasDC.fillRect(g, hightlightBg, rtHightLight);
            //g.drawRect(rtHightLight, );
        }

        commit_double_buffer(canvas);

    }

    public void ios_clear()
    {
        m_arIOSOrdersView.clear();
        m_ptNextStartPointInScreenDataArea.x = 0;
        m_ptNextStartPointInScreenDataArea.y = 0;

    }
    public boolean clear()
    {
        if (useSupperFunction())
            super.clear();
        else {
            ios_clear();
            this.invalidate();
        }
        return true;
    }

    public Rect ios_getDataArea()
    {
        Rect rt =  this.getBounds();

        rt.top += 30;
        rt.bottom -= KDSIOSView.INSET_DY;
        rt.left += KDSIOSView.INSET_DX;
        rt.right -= KDSIOSView.INSET_DX;

        return rt;

    }
    static public Point convertAbsoluteOrderViewPoint(Point pt, Rect rtOrderView)
    {
        Point ptRelative = new Point(pt);
        ptRelative.x = pt.x + rtOrderView.left;
        ptRelative.y = pt.y + rtOrderView.top;
        return ptRelative;
    }

    public boolean showIOSOrder(KDSIOSViewOrder iosOrder)
    {
        Rect screenDataRect = ios_getDataArea();
        Point pt = convertAbsoluteOrderViewPoint(m_ptNextStartPointInScreenDataArea, screenDataRect);

        if (!screenDataRect.contains(pt.x + INSET_DX, pt.y+INSET_DY))
            return false;
        if (screenDataRect.width()<=0) return false;
        ArrayList<Rect> arRects = KDSIOSViewOrder.calculateOrderSize(iosOrder, screenDataRect, m_ptNextStartPointInScreenDataArea,getBlockAverageWidth(), screenDataRect.height() );
        if (arRects.size() <=0 ) return false;
        Rect rtLast = arRects.get(0);

        if (!screenDataRect.contains(rtLast.right - INSET_DX, rtLast.bottom - INSET_DY))
            return false;
        iosOrder.setRects(arRects);
        m_arIOSOrdersView.add(iosOrder);


        return true;

    }

    protected KDSIOSViewOrder getTouchedOrderView(int x, int y)
    {
        Rect rtDataArea  = ios_getDataArea();

        for (int i = 0; i< m_arIOSOrdersView.size(); i++)
        {
            if (m_arIOSOrdersView.get(i).pointInMe(rtDataArea, x,y))
            {
                return m_arIOSOrdersView.get(i);
            }
        }
        return null;
    }
    protected boolean touchXY(int x, int y)
    {
        if (useSupperFunction())
            return super.touchXY(x, y);

        KDSIOSViewOrder orderView = getTouchedOrderView(x,y);
        if (orderView == null)
            return false;
        KDSIOSViewItem itemView = orderView.getTouchedItem(ios_getDataArea(), x,y);
        getEnv().getStateValues().setFocusedOrderGUID(orderView.getData().getGUID());
        getEnv().getStateValues().setFocusedItemGUID("");
        if (itemView != null)
        {
            getEnv().getStateValues().setFocusedItemGUID(itemView.m_item.getGUID());

        }

       this.invalidate();
        return true;


    }

    public KDSViewPanelBase getLastPanel()
    {
        if (useSupperFunction())
            return super.getLastPanel();
        if (m_arIOSOrdersView.size() <=0)
            return null;
        return m_arIOSOrdersView.get(m_arIOSOrdersView.size() - 1);

    }

    public int getPanelsCount()
    {
        if (useSupperFunction())
            return super.getPanelsCount();
        return m_arIOSOrdersView.size();
    }


    /**
     * check if this order is visible in view
     * @param orderGuid
     * @return
     */
    protected boolean isOrderVisible(String orderGuid)
    {
        if (useSupperFunction())
            return super.isOrderVisible(orderGuid);

        int ncount = m_arIOSOrdersView.size();
        for (int i=0; i< ncount; i++)
        {
            KDSIOSViewOrder panel = this.m_arIOSOrdersView.get(i);
            if (panel == null)
                continue;
            String guid = panel.getData().getGUID();

            if (guid.equals(orderGuid) )
                return true;
        }
        return false;
    }

}
