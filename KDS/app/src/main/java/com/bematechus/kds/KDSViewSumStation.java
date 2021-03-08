package com.bematechus.kds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSLog;

import java.util.ArrayList;

public class KDSViewSumStation //extends KDSView
{
    static public String TAG = "KDSViewSumStn";
    public View m_viewParent = null;
    Paint m_paint = new Paint();
    ArrayList<KDSViewSumStnPanel> m_arPanels = new ArrayList<>();

    int mMaxPanels = 4;
    int mMaxItemsEachPanel = 2;

    /*************************************/

//    static public final int ROUND_CORNER_DX = 10;
//    static public final int ROUND_CORNER_DY = 10;
    static public final int INSET_DY = 10;
    static public final int INSET_DX = 10;
    static public final int BORDER_INSET_DX = 5;
    static public final int BORDER_INSET_DY = 5;

    /**************************************/

    public void setMaxPanels(int n)
    {
        mMaxPanels = n;
    }

    public int getMaxPanels()
    {
        return mMaxPanels;
    }

//    public KDSViewSumStation(Context context) {
//        super(context);
//    }
//
//    public KDSViewSumStation(Context context, AttributeSet attrs) {
//        super(context, attrs);
//
//
//    }
//
//    public KDSViewSumStation(Context context, AttributeSet attrs, int defaultStyle) {
//        super(context, attrs, defaultStyle);
//
//
//    }
//
    public KDSViewSumStation(View parent)
    {
        m_viewParent = parent;
        init();
    }
    public  void init()
    {
        m_paint.setAntiAlias(true);
    }

    //@Override
    public void onDraw(Canvas canvas) {

    //    if (m_bDrawing) return;
     //   m_bDrawing = true;
        try {

            drawMe_DoubleBuffer(canvas);
       //     m_bForceFullDrawing = false;
        } catch (Exception err) {

            KDSLog.e(TAG, KDSLog._FUNCLINE_(), err);
        }
       // m_bDrawing = false;
    }

    public int panelsGetCount() {
        return m_arPanels.size();
    }

    protected void drawMe_DoubleBuffer(Canvas canvas) {

        Canvas g = canvas;// get_double_buffer();
        if (g == null) return;


        if (getEnv().getSettings() == null) return;
        int bg = getEnv().getSettings().getInt(KDSSettings.ID.Panels_View_BG);
        g.drawColor(bg);
        Rect screenDataRect = sumstn_getDataArea();
        int ncount = panelsGetCount();
        for (int i = 0; i < ncount; i++) {
            m_arPanels.get(i).onDraw(g, getEnv(), screenDataRect, i);
        }


        //commit_double_buffer(canvas);

    }

    public KDSViewSettings getEnv()
    {
        return ((KDSView)m_viewParent).getEnv();
    }


    public void sumstn_clear() {
        m_arPanels.clear();

    }

    public boolean clear() {
        sumstn_clear();;
        //m_viewParent.invalidate();
        return true;
    }

    public Rect getBounds()
    {
        Rect rc = new Rect();

        this.m_viewParent.getDrawingRect(rc);
        // rc.inset(2,2);

        return rc;
    }

    public Rect sumstn_getDataArea() {
        Rect rt = this.getBounds();

        rt.top += INSET_DY;
        rt.bottom -= INSET_DY;
        rt.left += INSET_DX;
        rt.right -= INSET_DX;

        return rt;

    }

    Handler m_refreshHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            KDSViewSumStation.this.m_viewParent.invalidate();
            return true;
        }
    });

    public void refresh()
    {
        Message m = new Message();
        m.what = 0;
        m_refreshHandler.sendMessage(m);
    }

//    static public Point convertAbsoluteOrderViewPoint(Point pt, Rect rtOrderView) {
//        Point ptRelative = new Point(pt);
//        ptRelative.x = pt.x + rtOrderView.left;
//        ptRelative.y = pt.y + rtOrderView.top;
//        return ptRelative;
//    }

    private int getBlockAverageWidth()
    {
        Rect rt = this.getBounds();
        int n = rt.width() / mMaxPanels;
        return n;
    }
    private Rect getPanelRect(Rect screenDataRect, int nPanelIndex)
    {
        int w = getBlockAverageWidth();
        int h = screenDataRect.height();

        int x = nPanelIndex * w;
        int y = 0;
        Rect rt = new Rect(x, y, x + w, y + h);
        return rt;
    }
    /**
     * call it from external. This is main interface.
     * @param group
     * @return
     */
    private boolean showSumGroup(KDSViewSumStnSumGroup group) {
        Rect screenDataRect = sumstn_getDataArea();
        if (screenDataRect.width() <= 0) return false;

        Rect rtPanel = getPanelRect(screenDataRect, m_arPanels.size());
        if (rtPanel == null) return false;

        KDSViewSumStnPanel panel = KDSViewSumStnPanel.createNew(group);
        panel.setRect(rtPanel);
        if (! KDSViewSumStnPanel.build( group, panel, rtPanel))
            return false;
        m_arPanels.add(panel);

        return true;

    }


//    public KDSViewPanelBase getLastPanel() {
//        if (m_arPanels.size() <= 0)
//            return null;
//        return m_arPanels.get(m_arPanels.size() - 1);
//
//    }
//
//    public int getPanelsCount() {
//        return m_arPanels.size();
//    }

    /**
     *  call this function from external.
     * @param arSummaryItems
     */
    public void showSummary(ArrayList<KDSSummaryItem> arSummaryItems)
    {
        this.clear();
        int ncount = 0;
        KDSViewSumStnSumGroup group = new KDSViewSumStnSumGroup();
        for (int i = 0; i< arSummaryItems.size(); i++)
        {

            group.items().add(arSummaryItems.get(i));
            ncount ++;
            if (ncount >= mMaxItemsEachPanel || (i == arSummaryItems.size() -1) )
            {
                showSumGroup(group);
                ncount = 0;
                group = new KDSViewSumStnSumGroup();
            }
        }
    }

    public void updateSettings(KDSSettings settings)
    {

    }

    public void refreshSummary(KDSDBCurrent db)
    {
        ArrayList<KDSSummaryItem> arData = db.summaryItems("", 0, null, false, true);
        showSummary(arData);
    }

}