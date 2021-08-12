package com.bematechus.kds;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class KDSViewSumStation //extends KDSView
{
    static public String TAG = "KDSViewSumStn";
    public View m_viewParent = null;
    Paint m_paint = new Paint();
    ArrayList<KDSViewSumStnPanel> m_arPanels = new ArrayList<>();

    HashMap<String, Float> mLastAlertedQty;

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

    KDSSettings.SumStationMode m_nDisplayMode = KDSSettings.SumStationMode.Summary;

    /*************************************/

    static public final int INSET_DY = 5;
    static public final int INSET_DX = 5;
    static public final int BORDER_INSET_DX = 5;
    static public final int BORDER_INSET_DY = 5;

    /**************************************/

    public KDSViewSumStation(View parent) {
        m_viewParent = parent;
        mLastAlertedQty = new HashMap<String, Float>();
        init();
    }

    public void init() {
        m_paint.setAntiAlias(true);
    }


    public void onDraw(Canvas canvas) {


        try {

            drawMe_DoubleBuffer(canvas);

        } catch (Exception err) {

            KDSLog.e(TAG, KDSLog._FUNCLINE_(), err);
        }

    }

    private boolean isScreenEmpty()
    {
        return (m_arPanels.size()==0);
    }
    protected void drawScreenLogo(Canvas canvas)
    {
        ScreenLogoDraw.drawScreenLogo(this.m_viewParent, this.getBounds(), canvas, getEnv().getSettings(), isScreenEmpty(), 0);
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
        drawScreenLogo(canvas);
        Rect screenDataRect = getDataArea();
        int ncount = panelsGetCount();
        int nRowHeight = getTextPixelsHeight(mItemFont, "pPyYqQ");
        for (int i = 0; i < ncount; i++) {
            m_arPanels.get(i).onDraw(g, getEnv(), screenDataRect, i, mCaptionFont, mItemFont, nRowHeight);
        }

    }

    public KDSViewSettings getEnv() {
        return ((KDSView) m_viewParent).getEnv();
    }


    public void clear() {
        m_arPanels.clear();

    }


    public Rect getBounds() {
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

    public void refresh() {
        Message m = new Message();
        m.what = 0;
        m_refreshHandler.sendMessage(m);
    }

    /**
     * this should been relatve rect
     *
     * @param screenDataRect
     * @param nPanelIndex
     * @return
     */
    private Rect getPanelRect(Rect screenDataRect, int nPanelIndex) {
        int w = screenDataRect.width() / mMaxPanels;
        int h = screenDataRect.height();

        int x = nPanelIndex * w;
        int y = 0;
        Rect rt = new Rect(x, y, x + w, y + h);
        return rt;
    }

    /**
     * call it from external. This is main interface.
     *
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
        int nCaptionHeight = getTextPixelsHeight(mCaptionFont, "pPyYqQ") + mCaptionFont.getFontSize() / 2;
        KDSViewSumStnPanel.m_orderCaptionHeight = nCaptionHeight;
        KDSViewSumStnPanel panel = KDSViewSumStnPanel.createNew(group);
        panel.setRect(rtPanel);
        panel.setRowHeight(nRowHeight);
        if (!KDSViewSumStnPanel.build(group, panel, rtPanel, nRowHeight))
            return false;
        m_arPanels.add(panel);

        return true;

    }

    private boolean showBinSumGroup(KDSViewSumStnSumGroup group) {
        if (m_arPanels.size() >= mMaxPanels)
            return false;
        Rect screenDataRect = getDataArea();
        if (screenDataRect.width() <= 0) return false;

        Rect rtPanel = getBinPanelRect(screenDataRect, m_arPanels.size());
        if (rtPanel == null) return false;
        //int nRowHeight = getTextPixelsHeight(mItemFont, "pPyYqQ");
        //int nCaptionHeight = getTextPixelsHeight(mCaptionFont, "pPyYqQ") + mCaptionFont.getFontSize()/2;
        //KDSViewSumStnPanel.m_orderCaptionHeight = nCaptionHeight;
        KDSViewSumStnBinPanel panel = KDSViewSumStnBinPanel.createNew(group);
        panel.setRect(rtPanel);
        //panel.setRowHeight(nRowHeight);
        //if (! KDSViewSumStnBinPanel.build( group, panel, rtPanel))
        //    return false;
        m_arPanels.add(panel);

        return true;

    }

    final int DOUBLE_LINES_MIN_PANELS = 6;

    private Rect getBinPanelRect(Rect screenDataRect, int nPanelIndex) {
        int w = 0;
        int h = 0;
        int nrow = 0;
        int nrowIndex = nPanelIndex;
        if (mMaxPanels < DOUBLE_LINES_MIN_PANELS) {
            w = screenDataRect.width() / mMaxPanels;
            h = screenDataRect.height();
        } else {
            int n = mMaxPanels/2;
            if (mMaxPanels%2 !=0)
                n ++;
            w = screenDataRect.width() / n;//(mMaxPanels / 2);
            h = screenDataRect.height() / 2;
            nrow = nPanelIndex / n;//(MAX_PANELS / 2);
            //nPanelIndex %= n;//(MAX_PANELS / 2);
            nrowIndex %= n;//(MAX_PANELS / 2);
        }


        int x = screenDataRect.left + nrowIndex * w;
        int y = screenDataRect.top + nrow * h;
        int hh = h;
        int ww = w;
//        if (mMaxPanels%2 !=0) {
//            if (nPanelIndex == (mMaxPanels-1))
//            ww += w; //last double width
//        }
        Rect rt = new Rect(x, y, x + ww, y + hh);
        return rt;
    }


    private SumStationFilterEntry filterCheck(KDSSummaryItem sumData) {
        String description = sumData.m_description;
        for (int i = 0; i < mFilters.size(); i++) {
            if (mFilters.get(i).getDescription().equals(description))
                return mFilters.get(i);
        }
        return null;
    }

    /**
     * call this function from external.
     *
     * @param arSummaryItems
     */
    private void showSummaryInSumStation(ArrayList<KDSSummaryItem> arSummaryItems) {
        switch (m_nDisplayMode) {

            case Summary: {
                showSummaryInSumStationSummaryMode(arSummaryItems);
            }
            break;
            case Bin: {
                showSummaryInSumStationBinMode(arSummaryItems);
            }
            break;
        }
    }

    private void showSummaryInSumStationSummaryMode(ArrayList<KDSSummaryItem> arSummaryItems) {
        this.clear();
        int ncount = 0;
        KDSViewSumStnSumGroup group = new KDSViewSumStnSumGroup();
        for (int i = 0; i < arSummaryItems.size(); i++) {
            KDSSummaryItem sumData = arSummaryItems.get(i);
            if (mFilterEnabled) {//check filter
                SumStationFilterEntry entry = filterCheck(sumData);
                if (entry == null)
                    continue;
                if (!entry.getDisplayText().isEmpty())
                    sumData.setDescription(entry.getDisplayText());
            }
            group.items().add(sumData);
            ncount++;
            if (ncount >= mMaxItemsEachPanel)//|| (i == arSummaryItems.size() -1) )
            {
                showSumGroup(group);
                ncount = 0;
                group = new KDSViewSumStnSumGroup();
            }
        }
        if (group.items().size() > 0) {
            showSumGroup(group);
        }
        refresh();
    }

    private void showSummaryInSumStationBinMode(ArrayList<KDSSummaryItem> arSummaryItems) {

        if (mFilterEnabled)
            showSummaryInSumStationBinModeFilterEnabled(arSummaryItems);
        else
            showSummaryInSumStationBinModeFilterDisabled(arSummaryItems);

//        this.clear();
//        int ncount = 0;
//        KDSViewSumStnSumGroup group = new KDSViewSumStnSumGroup();
//        ArrayList<Integer> arColors = new ArrayList<>();
//        for (int i = 0; i< arSummaryItems.size(); i++)
//        {
//            KDSSummaryItem sumData = arSummaryItems.get(i);
//            String description = sumData.getDescription();
//            arColors.clear();;
//            arColors =  mDB.itemGetColor(description);
//            if (arColors.size()>0)
//            {
//                group.setBG(arColors.get(0));
//                group.setBG(arColors.get(1));
//            }
//            if (mFilterEnabled)
//            {//check filter
//                SumStationFilterEntry entry = filterCheck(sumData);
//                if (entry == null)
//                    continue;
//                if (!entry.getDisplayText().isEmpty())
//                    sumData.setDescription(entry.getDisplayText());
//            }
//            group.items().add(sumData);
//
//            showBinSumGroup(group);
//
//            group = new KDSViewSumStnSumGroup();
//
//        }
//        if (group.items().size() >0)
//        {
//            showBinSumGroup(group);
//        }
//        refresh();
    }



    private void showSummaryInSumStationBinModeFilterDisabled(ArrayList<KDSSummaryItem> arSummaryItems)
    {
        this.clear();
        mDB.summaryItemsSortByQty(arSummaryItems, true);

        KDSViewSumStnSumGroup group = new KDSViewSumStnSumGroup();

        for (int i = 0; i< arSummaryItems.size(); i++)
        {
            KDSSummaryItem sumData = arSummaryItems.get(i);
            String description = sumData.getDescription();
            setColors(description, group);
            group.items().add(sumData);

            showBinSumGroup(group);

            group = new KDSViewSumStnSumGroup();

        }
        if (group.items().size() >0)
        {
            showBinSumGroup(group);
        }
        refresh();
    }

    private void setColors(String description, KDSViewSumStnSumGroup group)
    {

        ArrayList<Integer> arColors =  mDB.summaryGetColor(description, false);
        if (arColors.size()<=0)
            arColors =  mDB.summaryGetColor(description, true);
        if (arColors.size()>0)
        {
            group.setBG(arColors.get(0));
            group.setFG(arColors.get(1));
        }
    }
    private KDSSummaryItem findSumItem(ArrayList<KDSSummaryItem> arSummaryItems, String itemDescription)
    {

        for (int i = 0; i< arSummaryItems.size(); i++)
        {
            if (arSummaryItems.get(i).getDescription().equals(itemDescription))
            {
                return arSummaryItems.get(i);
            }
        }
        return null;
    }
    private void showSummaryInSumStationBinModeFilterEnabled(ArrayList<KDSSummaryItem> arSummaryItems)
    {
        this.clear();
        //int ncount = 0;
        KDSViewSumStnSumGroup group = new KDSViewSumStnSumGroup();
        //ArrayList<Integer> arColors = new ArrayList<>();

        for (int i = 0; i< mFilters.size(); i++)
        {
            SumStationFilterEntry entry = mFilters.get(i);
            KDSSummaryItem sumData = findSumItem(arSummaryItems, entry.getDescription());
            if (sumData == null)
            {
                sumData = new KDSSummaryItem();
                sumData.setDescription(entry.getDescription());
                sumData.setQty(0);

            }

            String description = sumData.getDescription();
            setColors(description, group);
            if (entry.isColorValid()) {
                group.setBG(entry.getBG());
                group.setFG(entry.getFG());
            }

            if (!entry.getDisplayText().isEmpty())
                sumData.setDescription(entry.getDisplayText());

            group.items().add(sumData);

            showBinSumGroup(group);

            group = new KDSViewSumStnSumGroup();

        }
        if (group.items().size() >0)
        {
            showBinSumGroup(group);
        }
        refresh();
    }


    public void updateSettings(KDSSettings settings)
    {
        mCaptionFont = settings.getKDSViewFontFace(KDSSettings.ID.SumStn_caption_font);
        mItemFont = settings.getKDSViewFontFace(KDSSettings.ID.SumStn_font);
        mMaxPanels = settings.getInt(KDSSettings.ID.SumStn_panels_count);
        mMaxItemsEachPanel = settings.getInt(KDSSettings.ID.SumStn_items_count);



        int n = settings.getInt(KDSSettings.ID.SumStn_sum_method);
        mSumType = KDSSettings.SumType.values()[n];

        n = settings.getInt(KDSSettings.ID.SumStn_mode);
        m_nDisplayMode = KDSSettings.SumStationMode.values()[n];

        mFilterEnabled = settings.getBoolean(KDSSettings.ID.SumStn_filter_enabled);
        mAlertEnabled = settings.getBoolean(KDSSettings.ID.SumStn_alert_enabled);
        String s = settings.getString(KDSSettings.ID.SumStn_filters);
        mFilters.clear();
        mFilters.addAll( KDSUIDialogSumStationFilter.parseSumItems(s));

        n = settings.getInt(KDSSettings.ID.Sumstn_order_by);
        mOrderBy = KDSSettings.SumOrderBy.values()[n];

        s = settings.getString(KDSSettings.ID.SumStn_alerts);
        ArrayList<SumStationAlertEntry> arNewAlerts = KDSUIDialogSumStnAlerts.parseSumItems(s);
        saveCurrentAlertStateToNewAlerts(arNewAlerts);
        mAlerts.clear();
        mAlerts.addAll(arNewAlerts);
        updateAlertStateFromPref();
        initLastAlertQty();
        saveAlertStateToPref();
    }

    private void saveCurrentAlertStateToNewAlerts(ArrayList<SumStationAlertEntry> arNewAlerts)
    {
        for (int i=0; i< arNewAlerts.size(); i++)
        {
            SumStationAlertEntry entryExisted = getExistedAlert(arNewAlerts.get(i));
            if (entryExisted!= null)
            {
                arNewAlerts.get(i).setTimeAlertDone(entryExisted.getTimeAlertDone());
                arNewAlerts.get(i).setTimeAlertFiredTime(entryExisted.getTimeAlertFiredTime());

                arNewAlerts.get(i).setQtyAlertDone(entryExisted.getQtyAlertDone());
                arNewAlerts.get(i).setQtyAlertFiredTime(entryExisted.getQtyAlertFiredTime());
                if (entryExisted.getAlertQty() != arNewAlerts.get(i).getAlertQty())
                    arNewAlerts.get(i).setAlertQtyChanged(true); //for reset the "last alert qty".

            }
        }
    }

    private SumStationAlertEntry getExistedAlert(SumStationAlertEntry entry)
    {
        return findAlertByGuid(entry.getGuid());
//        for (int i=0; i< mAlerts.size(); i++)
//        {
//            if (mAlerts.get(i).isEqual(entry))
//                return mAlerts.get(i);
//        }
//        return null;
    }

    ArrayList<KDSSummaryItem> mSummaryData = new ArrayList<>();
    Object mLocker = new Object();
    KDSDBCurrent mDB = null;
    int mRefreshCounter = 0;
    Thread mThread = null;
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            for ( int i=0; i< 1000; i++) {
                mRefreshCounter = 0;
                refreshSumStation(mDB);
                if (mRefreshCounter<=0)
                    break;
                else {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (Exception e)
                    {

                    }
                }

            }
        }
    };
    public void refreshInThread()
    {

        if (mThread == null || (!mThread.isAlive()) ) {
            mThread = new Thread(mRunnable);

            mThread.start();
        }
    }

    Handler mRefreshHandler = new Handler()
    {
        public void handleMessage(Message msg) {
            ArrayList<KDSSummaryItem> arData = (ArrayList<KDSSummaryItem>)msg.obj;
            showSummaryInSumStation(arData);
            synchronized (mLocker) { //save data for alert
                mSummaryData.clear();
                mSummaryData.addAll(arData);
            }
        }
    };

    private void refreshSumStation(KDSDBCurrent db)
    {
        switch (m_nDisplayMode)
        {

            case Summary:
            {
                refreshSumStationSummaryMode(db);
            }
                break;
            case Bin:
            {
                refreshSumStationBinMode(db);
            }
                break;
        }

    }

    private void refreshSumStationSummaryMode(KDSDBCurrent db)
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
        Message m = new Message();
        m.obj = arData;
        m.what = 1;
        mRefreshHandler.sendMessage(m);
//        showSummaryInSumStation(arData);
//        synchronized (mLocker) { //save data for alert
//            mSummaryData.clear();
//            mSummaryData.addAll(arData);
//        }
    }

    private void refreshSumStationBinMode(KDSDBCurrent db)
    {
        ArrayList<KDSSummaryItem> arData = null;

        boolean bAscend = (mOrderBy== KDSSettings.SumOrderBy.Ascend);

        arData = db.summaryItems("", 0, true, false, bAscend );
        ArrayList<KDSSummaryItem> arCondimentsData = db.summaryOnlyCondiments(0, bAscend, true);
        arData.addAll(arCondimentsData);

        Message m = new Message();
        m.obj = arData;
        m.what = 1;
        mRefreshHandler.sendMessage(m);

    }

    public void refreshSummaryInSumStation(KDSDBCurrent db)
    {
        mRefreshCounter ++;
        mDB = db;
        refreshInThread();


    }


    public void refreshSummaryInSumStation2(KDSDBCurrent db)
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
        boolean bChanged = false;
        for (int i=0; i< mAlerts.size(); i++)
        {
            SumStationAlertEntry entry = mAlerts.get(i);
            if (checkAlert(entry))
                bChanged = true;

        }
        if (bChanged)
            saveAlertStateToPref();
    }

    private boolean processAlert2(String entryDescription, float currentQty, SumStationAlertEntry entry){//float alertQty) {
		// KP-56 2021-03-15 Marcus
		float lastAlertedQty = 0;
		if (mLastAlertedQty.containsKey(entryDescription)) {
			lastAlertedQty = mLastAlertedQty.get(entryDescription);
		}
		boolean shouldAlert = currentQty - lastAlertedQty >= entry.getAlertQty();//alertQty;
		if (shouldAlert) {
			mLastAlertedQty.put(entryDescription, currentQty);
			//
			entry.setLastAlertQty(currentQty);
			entry.setQtyAlertDone(false);
		}
		return shouldAlert;
	}

    private boolean processAlert(String entryDescription, float currentQty, SumStationAlertEntry entry){//float alertQty) {
        // KP-56 2021-03-15 Marcus
        if (entry.getAlertQty() <=0)
            return false;
        float lastAlertedQty = 0;
        if (mLastAlertedQty.containsKey(entryDescription)) {
            lastAlertedQty = mLastAlertedQty.get(entryDescription);
        }
        int n = (int)(currentQty/entry.getAlertQty());

        boolean shouldAlert = (n > lastAlertedQty);// currentQty - lastAlertedQty >= entry.getAlertQty();//alertQty;
        if (shouldAlert) {
            mLastAlertedQty.put(entryDescription,(float) n);
            //
            entry.setLastAlertQty(n);
            entry.setQtyAlertDone(false);
        }
        else
        {
            if (n != lastAlertedQty) {
                mLastAlertedQty.put(entryDescription,(float) n);
                entry.setLastAlertQty(n);
                entry.setDirty(true);
            }

        }
        return shouldAlert;
    }

    private boolean qtyAlertFitCondition(SumStationAlertEntry entry)
    {
        boolean bFit = false;
        synchronized (mLocker)
        {
            if (entry.getEntryType() == SumStationEntry.EntryType.Item ||
                    (entry.getEntryType() == SumStationEntry.EntryType.Condiment &&
                        mSumType == KDSSettings.SumType.CondimentsOnly)) {
                for (int i = 0; i < mSummaryData.size(); i++) {
                    String entryDescription = entry.getDescription();
                    if (mSummaryData.get(i).getItemDescription().equals(entryDescription)) {
                        bFit = processAlert(entry.getLastAlertQtyDescription(), mSummaryData.get(i).getQty(), entry);//.getAlertQty());
                        break;
                    }
                }
//                } else if (mSummaryData.get(i).getCondiments().size() > 0) {
//                    if (entry.getEntryType() == SumStationEntry.EntryType.Condiment)
//                    {
//                        bFit = processAlert("cond:" + entryDescription, qtyAlertSumCondiments(entry), entry);//.getAlertQty());
//                    }
//                	for (KDSSummaryCondiment condiment : mSummaryData.get(i).getCondiments()) {
//                		if (condiment.mDescription.equals(entryDescription)) {
//							bFit = processAlert("cond:" + entryDescription, mSummaryData.get(i).getQty(), entry);//.getAlertQty());
//							break;
//						}
//					}
             //   }
//            }
            }
            else if (entry.getEntryType() == SumStationEntry.EntryType.Condiment)
            {
                bFit = processAlert(entry.getLastAlertQtyDescription(), qtyAlertSumCondiments(entry), entry);//.getAlertQty());
            }
        }

        return bFit;
    }

    private int qtyAlertSumCondiments(SumStationAlertEntry entry)
    {
        if (entry.getEntryType() != SumStationEntry.EntryType.Condiment)
            return -1;
        int ntotal = 0;
        for (int i=0; i< mSummaryData.size(); i++)
        {
            if (mSummaryData.get(i).getCondiments().size() > 0) {
                for (KDSSummaryCondiment condiment : mSummaryData.get(i).getCondiments()) {
                    if (condiment.mDescription.equals(entry.getDescription())) {

                        ntotal += (condiment.getQty()*mSummaryData.get(i).getQty());

                    }
                }
            }
        }
        return ntotal;



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
        c.set(Calendar.SECOND, 0);
        Date dtAlert = c.getTime();

        Date dt = new Date();
        long l = dt.getTime() - dtAlert.getTime();
        return ( l>0 && l<18000000 ); //0 -- 30 minutes




    }

    /**
     *
     * @param entry
     * @return
     *  true: the alert state changed.
     */
    private boolean checkAlert(SumStationAlertEntry entry)
    {
        boolean bChanged = false;
        int nAlertQty = entry.getAlertQty();
        String alertTime = entry.getAlertTime();
        if (nAlertQty >0)
        {
            if (qtyAlertFitCondition(entry))
            {
                bChanged = true;//save the last alert qty.
                if (!entry.getQtyAlertDone()) {
                    if (showAlert(entry)) {
                        entry.setQtyAlertDone(true);
                        entry.setQtyAlertFiredTime(new Date());
                        bChanged = true;
                    }
                }
                else
                {
                    Date dt = entry.getQtyAlertFiredTime();
                    TimeDog td = new TimeDog(dt);
                    if (td.is_timeout(1 * 60 *60 *1000))
                    //if (td.is_timeout(60 *1000))//test
                    {//reset. Remind it again.
                        bChanged = true;
                        entry.setQtyAlertDone(false);
                    }
                }

            }
            else
            { //qty is enough. Reset it.
                if (entry.getQtyAlertDone() ||
                    entry.isDirty()) {
                    bChanged = true;
                    entry.setDirty(false);
                }
                entry.setQtyAlertDone(false);
            }
        }

        if (!alertTime.isEmpty())
        {
            if (timeAlertFit(alertTime))
            {
                if (!entry.getTimeAlertDone())
                {
                    if (showAlert(entry)) {
                        bChanged = true;
                        entry.setTimeAlertDone(true);
                        entry.setTimeAlertFiredTime(new Date());
                    }
                }

            }
            else
            {
                if (entry.getTimeAlertDone())
                    bChanged = true;
                entry.setTimeAlertDone(false);
            }
        }
        return bChanged;
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

    private String getAlertsStateString()
    {
        String s = "";
        for (int i=0; i< mAlerts.size(); i++)
        {
            SumStationAlertEntry alert = mAlerts.get(i);
            if (!s.isEmpty())
                s += "\n";
            s += alert.toStateString();
        }
        return s;
    }
    final String PREF_NAME = "sumalert";
    final String PREF_KEY = "sumstn_time_alerts_state";
    private void saveAlertStateToPref()
    {
        String s = getAlertsStateString();
        Context c = m_viewParent.getContext();
        SharedPreferences pref = c.getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        SharedPreferences.Editor editor =  pref.edit();
        editor.putString(PREF_KEY, s);
        editor.apply();
        editor.commit();
    }

    private SumStationAlertEntry findAlertByGuid(String guid)
    {
        for (int i=0; i< mAlerts.size(); i++)
        {
            if (mAlerts.get(i).getGuid().equals(guid))
                return mAlerts.get(i);
        }
        return null;
    }
    private void updateAlertStateFromPref()
    {

        Context c = m_viewParent.getContext();
        SharedPreferences pref = c.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String s = pref.getString(PREF_KEY, "");

        ArrayList<String> ar = KDSUtil.spliteString(s, "\n");
        for (int i=0; i< ar.size(); i++)
        {
            SumStationAlertEntry alert =  SumStationAlertEntry.parseStateString(ar.get(i));
            if (alert != null)
            {
                SumStationAlertEntry existedAlert = findAlertByGuid(alert.getGuid());
                if (existedAlert != null)
                {
                    existedAlert.setQtyAlertDone(alert.getQtyAlertDone());
                    existedAlert.setQtyAlertFiredTime(alert.getQtyAlertFiredTime());
                    existedAlert.setTimeAlertDone(alert.getTimeAlertDone());
                    existedAlert.setTimeAlertFiredTime(alert.getTimeAlertFiredTime());
                    if (existedAlert.getAlertQtyChanged()) {
                        existedAlert.setLastAlertQty(0);
                        existedAlert.setAlertQtyChanged(false); //reset it.
                    }
                    else
                    {
                        existedAlert.setLastAlertQty(alert.getLastAlertQty());
                    }
                }
            }
        }


    }

    private void initLastAlertQty()
    {
        mLastAlertedQty.clear();
        for (int i=0; i< mAlerts.size(); i++)
        {
            SumStationAlertEntry entry =  mAlerts.get(i);
            String name = entry.getLastAlertQtyDescription();
            mLastAlertedQty.put(name, entry.getLastAlertQty());
        }
    }

}
