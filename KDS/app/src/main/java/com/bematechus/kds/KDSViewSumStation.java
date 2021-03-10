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
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;
import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class KDSViewSumStation //extends KDSView
{
    static public String TAG = "KDSViewSumStn";
    public View m_viewParent = null;
    Paint m_paint = new Paint();
    ArrayList<KDSViewSumStnPanel> m_arPanels = new ArrayList<>();

    //settings
    KDSViewFontFace mItemFont = new KDSViewFontFace();
    KDSViewFontFace mCaptionFont = new KDSViewFontFace();

    KDSSettings.SumType mSumType = KDSSettings.SumType.ItemWithoutCondiments;
    ArrayList<SumStationFilterEntry> mFilters = new ArrayList<>();
    ArrayList<SumStationAlertEntry> mAlerts = new ArrayList<>();

    int mMaxPanels = 4;
    int mMaxItemsEachPanel = 2;
    boolean mFilterEnabled = false;
    boolean mAlertEnabled = false;
    KDSSettings.SumOrderBy mOrderBy = KDSSettings.SumOrderBy.Ascend;

    /*************************************/

    static public final int INSET_DY = 5;
    static public final int INSET_DX = 5;
    static public final int BORDER_INSET_DX = 5;
    static public final int BORDER_INSET_DY = 5;

    /**************************************/

    public KDSViewSumStation(View parent)
    {
        m_viewParent = parent;
        init();
    }
    public  void init()
    {
        m_paint.setAntiAlias(true);
    }


    public void onDraw(Canvas canvas) {


        try {

            drawMe_DoubleBuffer(canvas);

        } catch (Exception err) {

            KDSLog.e(TAG, KDSLog._FUNCLINE_(), err);
        }

    }

    public int panelsGetCount() {
        return m_arPanels.size();
    }

    protected void drawMe_DoubleBuffer(Canvas canvas) {

        Canvas g = canvas;// get_double_buffer();
        if (g == null) return;


        if (getEnv().getSettings() == null) return;
        int bg = getEnv().getSettings().getInt(KDSSettings.ID.SumStn_screen_bg);//.Panels_View_BG);
        g.drawColor(bg);
        Rect screenDataRect = getDataArea();
        int ncount = panelsGetCount();
        int nRowHeight = getTextPixelsHeight(mItemFont, "pPyYqQ");
        for (int i = 0; i < ncount; i++) {
            m_arPanels.get(i).onDraw(g, getEnv(), screenDataRect, i, mCaptionFont, mItemFont, nRowHeight);
        }

    }

    public KDSViewSettings getEnv()
    {
        return ((KDSView)m_viewParent).getEnv();
    }


    public void clear() {
        m_arPanels.clear();

    }


    public Rect getBounds()
    {
        Rect rc = new Rect();

        this.m_viewParent.getDrawingRect(rc);
        // rc.inset(2,2);

        return rc;
    }

    public Rect getDataArea() {
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

    /**
     * this should been relatve rect
     * @param screenDataRect
     * @param nPanelIndex
     * @return
     */
    private Rect getPanelRect(Rect screenDataRect, int nPanelIndex)
    {
        int w = screenDataRect.width()/mMaxPanels;
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
        if (m_arPanels.size() >= mMaxPanels)
            return false;
        Rect screenDataRect = getDataArea();
        if (screenDataRect.width() <= 0) return false;

        Rect rtPanel = getPanelRect(screenDataRect, m_arPanels.size());
        if (rtPanel == null) return false;
        int nRowHeight = getTextPixelsHeight(mItemFont, "pPyYqQ");
        int nCaptionHeight = getTextPixelsHeight(mCaptionFont, "pPyYqQ") + mCaptionFont.getFontSize()/2;
        KDSViewSumStnPanel.m_orderCaptionHeight = nCaptionHeight;
        KDSViewSumStnPanel panel = KDSViewSumStnPanel.createNew(group);
        panel.setRect(rtPanel);
        panel.setRowHeight(nRowHeight);
        if (! KDSViewSumStnPanel.build( group, panel, rtPanel, nRowHeight))
            return false;
        m_arPanels.add(panel);

        return true;

    }


    private SumStationFilterEntry filterCheck(KDSSummaryItem sumData)
    {
        String description = sumData.m_description;
        for (int i = 0; i< mFilters.size(); i++)
        {
            if (mFilters.get(i).getDescription().equals(description))
                return mFilters.get(i);
        }
        return null;
    }

    /**
     *  call this function from external.
     * @param arSummaryItems
     */
    public void showSummaryInSumStation(ArrayList<KDSSummaryItem> arSummaryItems)
    {
        this.clear();
        int ncount = 0;
        KDSViewSumStnSumGroup group = new KDSViewSumStnSumGroup();
        for (int i = 0; i< arSummaryItems.size(); i++)
        {
            KDSSummaryItem sumData = arSummaryItems.get(i);
            if (mFilterEnabled)
            {//check filter
                SumStationFilterEntry entry = filterCheck(sumData);
                if (entry == null)
                    continue;
                if (!entry.getDisplayText().isEmpty())
                    sumData.setDescription(entry.getDisplayText());
            }
            group.items().add(sumData);
            ncount ++;
            if (ncount >= mMaxItemsEachPanel )//|| (i == arSummaryItems.size() -1) )
            {
                showSumGroup(group);
                ncount = 0;
                group = new KDSViewSumStnSumGroup();
            }
        }
        if (group.items().size() >0)
        {
            showSumGroup(group);
        }
    }

    public void updateSettings(KDSSettings settings)
    {
        mCaptionFont = settings.getKDSViewFontFace(KDSSettings.ID.SumStn_caption_font);
        mItemFont = settings.getKDSViewFontFace(KDSSettings.ID.SumStn_font);
        mMaxPanels = settings.getInt(KDSSettings.ID.SumStn_panels_count);
        mMaxItemsEachPanel = settings.getInt(KDSSettings.ID.SumStn_items_count);
        int n = settings.getInt(KDSSettings.ID.SumStn_sum_method);
        mSumType = KDSSettings.SumType.values()[n];
        mFilterEnabled = settings.getBoolean(KDSSettings.ID.SumStn_filter_enabled);
        mAlertEnabled = settings.getBoolean(KDSSettings.ID.SumStn_alert_enabled);
        String s = settings.getString(KDSSettings.ID.SumStn_filters);
        mFilters =  KDSUIDialogSumStationFilter.parseSumItems(s);

        s = settings.getString(KDSSettings.ID.SumStn_alerts);
        mAlerts = KDSUIDialogSumStnAlerts.parseSumItems(s);

        n = settings.getInt(KDSSettings.ID.Sumstn_order_by);
        mOrderBy = KDSSettings.SumOrderBy.values()[n];

    }

    ArrayList<KDSSummaryItem> mSummaryData = new ArrayList<>();
    Object mLocker = new Object();

    public void refreshSummaryInSumStation(KDSDBCurrent db)
    {
        ArrayList<KDSSummaryItem> arData = null;

        boolean bAscend = (mOrderBy== KDSSettings.SumOrderBy.Ascend);
        switch (mSumType)
        {

            case ItemWithoutCondiments: {
                arData = db.summaryItems("", 0, true, false, bAscend );
            }
            break;
            case ItemWithCondiments:
            {
                arData = db.summaryItems("", 0, true, true, bAscend);
            }
            break;
            case CondimentsOnly:
            {
                arData = db.summaryOnlyCondiments(0, bAscend, true);
            }
            break;
        }
        //if (mSumType == KDSSettings.SumType.ItemWithCondiments)
        //    bCheckCondiments = true;

        //ArrayList<KDSSummaryItem> arData = db.summaryItems("", 0, true, bCheckCondiments, true);
        showSummaryInSumStation(arData);
        synchronized (mLocker) { //save data for alert
            mSummaryData.clear();
            mSummaryData.addAll(arData);
        }
    }

    public void onTimer()
    {
        if (!mAlertEnabled) return;
        checkSumStationAlert();
    }

    private void checkSumStationAlert()
    {
        for (int i=0; i< mAlerts.size(); i++)
        {
            SumStationAlertEntry entry = mAlerts.get(i);
            checkAlert(entry);

        }
    }

    private boolean qtyAlertFitCondition(String description, int qty)
    {
        synchronized (mLocker)
        {
            for (int i=0; i< mSummaryData.size(); i++)
            {
                if (mSummaryData.get(i).getDescription().equals(description))
                {
                    return (mSummaryData.get(i).getQty() < qty );
                }
            }
        }
        return false;
    }

    /**
     *
     * @param alertTime
     *  format: HH:mm
     * @return
     */
    private boolean timeAlertFit(String alertTime)
    {
        ArrayList<String> ar = KDSUtil.spliteString(alertTime, ":");
        if (ar.size() <2) return false;
        int h = KDSUtil.convertStringToInt( ar.get(0), -1);
        int m = KDSUtil.convertStringToInt( ar.get(1), -1);
        if ((h ==-1) || (m == -1) )
            return false;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.MINUTE, m);
        Date dtAlert = c.getTime();

        Date dt = new Date();
        long l = dt.getTime() - dtAlert.getTime();
        return ( l>0 && l<60000 );




    }

    private void checkAlert(SumStationAlertEntry entry)
    {
        int nAlertQty = entry.getAlertQty();
        String alertTime = entry.getAlertTime();
        if (nAlertQty >0)
        {
            if (qtyAlertFitCondition(entry.getDescription(), nAlertQty))
            {
                if (!entry.getQtyAlertDone()) {
                    if (showAlert(entry))
                        entry.setQtyAlertDone(true);
                }
            }
            else
            {
                entry.setQtyAlertDone(false);
            }
        }

        if (!alertTime.isEmpty())
        {
            if (timeAlertFit(alertTime))
            {
                if (!entry.getTimeAlertDone())
                {
                    if (showAlert(entry))
                        entry.setTimeAlertDone(true);
                }

            }
            else
            {
                entry.setTimeAlertDone(false);
            }
        }
    }

    KDSUIDlgSumStnAlert mAlertDlg = null;
    private boolean showAlert(SumStationAlertEntry entry)
    {
        if (mAlertDlg != null) {
            if (mAlertDlg.isVisible())
                return false;
            mAlertDlg.hide();
        }
        mAlertDlg = new KDSUIDlgSumStnAlert(m_viewParent.getContext(), entry);
        mAlertDlg.show();
        return true;
    }

    public static int getTextPixelsHeight(KDSViewFontFace ff,String strText)
    {
        Paint paint = new Paint();
        //paint.setColor(ff.getFG());
        paint.setTypeface(ff.getTypeFace());
        paint.setTextSize(ff.getFontSize());
        //paint.setAntiAlias(true);
        Rect r = new Rect();

        paint.getTextBounds(strText, 0, strText.length(), r);
        return r.height() + ff.getFontSize()/2;// .left + r.width();
    }
}