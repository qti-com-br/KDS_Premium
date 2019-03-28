package com.bematechus.kds;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSUtil;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/20 0020.
 * The UI control that build the user operations.
 */
public class KDSUserUI {

    KDSLayout m_layout = null;

    HorizontalListView m_lstTouchPad = null;
    ListView m_lstVerticalTouchPad = null;

    FrameLayout m_sumFrame = null;
    TextView m_txtPrev = null;
    TextView m_txtNext = null;
    TextView m_txtParked = null;
    View m_linear = null;

    View m_viewTopSum = null;

    MainActivityFragmentSum m_summaryFragment = null;

    MainActivityFragment m_ordersFragment = null;

    View m_viewFocusIndicator = null;

    View m_layoutScreenTitle = null;

    TextView m_txtAvgPrepTime = null; //2.0.25
    int m_nScreenTitlePrevID = 0;
    int m_nScreenTitleNextID = 0;
    int m_nScreenTitleTextID = 0;
    int m_nScreenTitleParkID = 0;
    int m_nScreenTitleAvgPrepID = 0;

    public void setScreenTitleLayout(View v)
    {
        m_layoutScreenTitle = v;
    }
    public  View getScreenTitleLayout()
    {
        return m_layoutScreenTitle;
    }

    public void showScreenTitle(boolean bShow)
    {
        m_layoutScreenTitle.setVisibility( (bShow?View.VISIBLE:View.GONE) );
    }
    public void setLayout(KDSLayout layout) {
        m_layout = layout;
    }
    public KDSLayout getLayout() {
        return m_layout;
    }

    public void setLinear(View linear) {
        m_linear = linear;
    }
    public View getLinear() {
        return m_linear;
    }



    public void setTouchHorizontalList(HorizontalListView lst) {
        m_lstTouchPad = lst;
    }

    public HorizontalListView getTouchHorizontalList() {
        return m_lstTouchPad ;
    }

    public void setTouchVerticalList(ListView lst) {
        m_lstVerticalTouchPad = lst;
    }

    public ListView getTouchVerticalList() {
        return m_lstVerticalTouchPad ;
    }





    public void setSumFrame(FrameLayout c) {
        m_sumFrame = c;
    }

    public void setFocusIndicator(View v)
    {
        m_viewFocusIndicator = v;
    }
    public View getFocusIndicator()
    {
        return m_viewFocusIndicator;
    }

    public void focusMe(boolean bFocus)
    {
        if (bFocus) {
            int color = KDSGlobalVariables.getKDS().getSettings().getInt(KDSSettings.ID.Focused_BG);
            m_viewFocusIndicator.setBackgroundColor(color);
        }
        else
            m_viewFocusIndicator.setBackgroundColor(Color.TRANSPARENT);
    }

    public void enableFocusIndicator(boolean bEnable)
    {
        if (m_viewFocusIndicator == null) return;
        if (bEnable)
            m_viewFocusIndicator.setVisibility(View.VISIBLE);
        else
            m_viewFocusIndicator.setVisibility(View.GONE);

    }
    public void setTopSum(View v)
    {
        m_viewTopSum = v;
    }
    public View getTopSum()
    {
        return m_viewTopSum;
    }

    public FrameLayout getSumFrame() {
        return m_sumFrame;
    }

    public void setSumFragment(MainActivityFragmentSum c) {
        m_summaryFragment = c;
    }

    public MainActivityFragmentSum getSumFragment() {
        return m_summaryFragment;
    }


    public void setOrdersFragment(MainActivityFragment c) {
        m_ordersFragment = c;
    }

    public MainActivityFragment getOrdersFragment() {
        return m_ordersFragment;
    }

    public void setPrevTextView(TextView c) {
        if (m_txtPrev != null)
        {
            if (c == null)
                showPrevCount(m_txtPrev, "");
        }
        m_txtPrev = c;
    }

    public TextView getPrevTextView() {
        return m_txtPrev;
    }

    public void setNextTextView(TextView c) {
        if (m_txtNext != null)
        {
            if (c == null) {
                showNextCount(m_txtNext, "");

            }
        }
        m_txtNext = c;
    }

    public TextView getNextTextView() {
        return m_txtNext;
    }

    public void setParkedTextView(TextView v)
    {
        if (m_txtParked != null)
        {
            if (v == null)
                m_txtParked.setText("");
        }
        m_txtParked = v;
    }
    public TextView getParkedTextView()
    {
        return m_txtParked;
    }

    public void show(boolean bShow) {

        KDSSettings.ScreenOrientation orientation = KDSSettings.ScreenOrientation.Left_Right;
        if (KDSGlobalVariables.getKDS().isMultpleUsersMode()) {
            int n = KDSSettings.getEnumIndexValues(KDSGlobalVariables.getKDS().getSettings(), KDSSettings.ScreenOrientation.class, KDSSettings.ID.Screens_orientation);
            orientation = KDSSettings.ScreenOrientation.values()[n];
        }

        int n = View.VISIBLE;
        if (!bShow)
            n = View.GONE;
       // if (m_linear!=null)
        //    m_linear.setVisibility(n);
//        if (m_viewTopSum != null)
//            m_viewTopSum.setVisibility(n);
        if (m_layout == null)
            return;
        m_layout.getView().setVisibility(n);

        if (n == View.VISIBLE) {

            if (KDSGlobalVariables.getKDS().isMultpleUsersMode()) {
                if (orientation == KDSSettings.ScreenOrientation.Left_Right) {
                    m_lstVerticalTouchPad.setVisibility(View.GONE);
                    m_lstTouchPad.setVisibility(View.VISIBLE);
                } else { //top-bottom
                    m_lstTouchPad.setVisibility(View.VISIBLE);
                    m_lstVerticalTouchPad.setVisibility(View.GONE);
                }
            }
            else
            {
                m_lstTouchPad.setVisibility(View.VISIBLE);
                m_lstVerticalTouchPad.setVisibility(View.GONE);
            }
        }
        else
        {
            m_lstTouchPad.setVisibility(View.GONE);
            m_lstVerticalTouchPad.setVisibility(View.GONE);
        }
        if (m_linear!=null) {
            m_linear.setVisibility(n);

        }
        //m_touchFrame.setVisibility(n);
        //m_sumFragment.setVisibility(n);
        if (m_txtPrev != null)
            m_txtPrev.setVisibility(n);
        if (m_txtNext != null)
            m_txtNext.setVisibility(n);
        if (m_txtParked != null)
            m_txtParked.setVisibility(n);
        if (m_txtAvgPrepTime != null)
            m_txtAvgPrepTime.setVisibility(n); //2.0.25

    }

    public void showSum(KDSUser.USER userID, KDSSettings.SumPosition position, boolean bShow) {
        int n = View.VISIBLE;
        if (!bShow)
            n = View.GONE;
        if (position == KDSSettings.SumPosition.Side)
            m_sumFrame.setVisibility(n);
        else
            m_viewTopSum.setVisibility(n);
        if (bShow)
            refreshSum(userID, position);
    }

    public void showTouchPad(boolean bShow) {
        int n = View.VISIBLE;
        if (!bShow)
            n = View.GONE;

        //m_touchFrame.setVisibility(n);
        m_lstTouchPad.setVisibility(n);
    }

    public boolean isVisibleTouchPad() {
        //return (m_touchFrame.getVisibility() != View.GONE);
        return (m_lstTouchPad.getVisibility() != View.GONE);
    }


    public void showVerticalTouchPad(boolean bShow) {
        int n = View.VISIBLE;
        if (!bShow)
            n = View.GONE;

        //m_touchFrame.setVisibility(n);
        m_lstVerticalTouchPad.setVisibility(n);
    }

    public boolean isVisibleVerticalTouchPad() {
        //return (m_touchFrame.getVisibility() != View.GONE);
        return (m_lstVerticalTouchPad.getVisibility() != View.GONE);
    }

    public boolean isVisibleSum(KDSSettings.SumPosition position) {
        if (position == KDSSettings.SumPosition.Side) {
            if (m_sumFrame == null) return false;
            return (m_sumFrame.getVisibility() != View.GONE);
        }
        else {
            if (m_viewTopSum == null) return false;
            return (m_viewTopSum.getVisibility() != View.GONE);
        }
    }

    public void refreshSum(KDSUser.USER userID, KDSSettings.SumPosition position) {

        if (!isVisibleSum(position)) return;
        if (position == KDSSettings.SumPosition.Side) {
            if (m_summaryFragment != null)
                m_summaryFragment.refreshSummary(userID);
        }
        else
        {
            refreshTopSum(userID);
        }

    }
   // ArrayList<Map<String,Object>> m_arTopSumDataA= new ArrayList<Map<String,Object>>();
    ArrayList<Map<String,Object>> m_arTopSumData= new ArrayList<Map<String,Object>>();
    public void init_top_sum(Context context)
    {
        // Inflate the layout for this fragment
        //View view = inflater.inflate(R.layout.fragment_summary, container, false);

        GridView gv = (GridView) m_viewTopSum;
        SumSimpleAdapter adapter = new SumSimpleAdapter(context,m_arTopSumData,R.layout.listitem_top_sum,//. simple_list_item_2,
                new String[]{"qty","name"},new int[]{R.id.text1,R.id.text2});
        gv.setAdapter(adapter);



    }
    final int SUM_ROW_HEIGHT = 20;

    public void refreshTopSum(KDSUser.USER userID)
    {
        m_arTopSumData.clear();
        KDS kds = KDSGlobalVariables.getKDS();

        String bgfg = kds.getSettings().getString(KDSSettings.ID.Sum_bgfg);
        KDSBGFG bf = KDSBGFG.parseString(bgfg);
        m_viewTopSum.setBackgroundColor(bf.getBG());
        boolean advSumEnabled = kds.getSettings().getBoolean(KDSSettings.ID.AdvSum_enabled);
        if (advSumEnabled) {
            int nRows = kds.getSettings().getInt(KDSSettings.ID.AdvSum_rows);
            int nCols = kds.getSettings().getInt(KDSSettings.ID.AdvSum_cols);
            ((GridView)m_viewTopSum).setNumColumns(nCols);
            LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) m_viewTopSum.getLayoutParams(); //取控件textView当前的布局参数
            linearParams.height = SUM_ROW_HEIGHT * nRows;// 控件的高强制设成20
            m_viewTopSum.setLayoutParams(linearParams);

        }
        else
        {
            int nRows = 4;
            int nCols = 3;
            ((GridView)m_viewTopSum).setNumColumns(nCols);
            LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) m_viewTopSum.getLayoutParams(); //取控件textView当前的布局参数
            linearParams.height = SUM_ROW_HEIGHT * nRows;// 控件的高强制设成20
            m_viewTopSum.setLayoutParams(linearParams);
        }

        ArrayList<KDSSummaryItem> arSumItems = kds.summary(userID);


        if (arSumItems == null) return;

        boolean bSmartEnabled = kds.getSettings().getBoolean(KDSSettings.ID.Smart_Order_Enabled);
        //boolean bSmartEnabled = (KDSSettings.SmartMode.values()[ this.getKDS().getSettings().getInt(KDSSettings.ID.Smart_mode)] == KDSSettings.SmartMode.Normal );
        for (int i=0; i< arSumItems.size(); i++)
        {
            Map<String,Object> item = new HashMap<String,Object>();
            if (bSmartEnabled && advSumEnabled)
            //if (advSumEnabled)
                item.put("qty", arSumItems.get(i).getAdvSumQtyString());
            else
                item.put("qty", arSumItems.get(i).getQtyString());
            item.put("name", arSumItems.get(i).getDescription(true));
            m_arTopSumData.add(item);
        }
        if (m_viewTopSum!= null) {
            if (((GridView) m_viewTopSum).getAdapter() != null)
                ((SimpleAdapter) ((GridView) m_viewTopSum).getAdapter()).notifyDataSetChanged();
        }

    }
    public String getPrevCountString()
    {
        if (m_layout == null) return "";
        int nprev = m_layout.getPrevCount();
        if (nprev<0)
            return "-1";

        String strPrev = "";
        if (nprev >0)
            strPrev = Integer.toString(nprev) + " ";//<--";
        return strPrev;
    }

    public String getNextCountString()
    {
        if (m_layout == null) return "";

        int nnext =m_layout.getNextCount();

        if (nnext <0)
        {//unknown. Need to refresh UI, then we can not this number.
            return "-1";
        }
        String strNext = "";
        if (nnext >0)
            strNext = " " + Integer.toString(nnext) ;
            //strNext = "--> " + Integer.toString(nnext) ;
        return strNext;
    }



    private void showPrevCount(TextView txtView, String strCount)
    {
        if (txtView != null) {
            if (!strCount.isEmpty()) {
                Drawable imgPrev = KDSApplication.getContext().getResources().getDrawable(R.drawable.prevcount);
                imgPrev.setBounds(0, 0, imgPrev.getMinimumWidth()/2, imgPrev.getMinimumHeight()/2);//必须设置图片大小，否则不显示
                txtView.setCompoundDrawables(null,null, imgPrev, null);
            }
            else
            {
                txtView.setCompoundDrawables(null, null, null, null);
            }


            txtView.setText(strCount);
        }
    }

    private void showNextCount(TextView txtView, String strCount)
    {
        if (txtView != null) {
            if (!strCount.isEmpty()) {

                Drawable imgNext = KDSApplication.getContext().getResources().getDrawable(R.drawable.nextcount);
                imgNext.setBounds(0, 0, imgNext.getMinimumWidth()/2, imgNext.getMinimumHeight()/2);//必须设置图片大小，否则不显示
                txtView.setCompoundDrawables(imgNext, null, null, null);
            }
            else
            {
                txtView.setCompoundDrawables(null, null, null, null);
            }
            txtView.setText(strCount);

        }
    }


    public void setScreenTitleIDs(int prevID, int nextID, int titleID, int parkID,int avgID)
    {
        m_nScreenTitlePrevID = prevID;
        m_nScreenTitleNextID = nextID;
        m_nScreenTitleTextID = titleID;
        m_nScreenTitleParkID = parkID;

        m_nScreenTitleAvgPrepID = avgID;

    }
    public void refreshPrevNext()
    {
        String strPrev =getPrevCountString();// this.m_layout.getPrevCountString();
        String strNext =getNextCountString();
        if (KDSGlobalVariables.getKDS().isMultpleUsersMode())
        {
            TextView t =  (TextView)m_layoutScreenTitle.findViewById(m_nScreenTitleNextID);
            if (!strNext.equals("-1"))
                showNextCount(t, strNext);
            TextView tv =  (TextView)m_layoutScreenTitle.findViewById(m_nScreenTitlePrevID);
            if (!strPrev.equals("-1"))
                showPrevCount(tv, strPrev);
        }
        else {
            if (!strNext.equals("-1"))
                showNextCount(m_txtNext, strNext);
            if (!strPrev.equals("-1"))
                showPrevCount(m_txtPrev, strPrev);
        }

    }

    public void refresh()
    {
        if (m_layout!= null)
            m_layout.refresh();
        else
            return;

    }

    public void checkIfFocusedOrderLostAfterItemsShowingMethodChanged()
    {
        if (m_layout!= null)
            m_layout.checkPreferenceChangeItemShowingMethod();
    }

    public  void refreshParkOrdersCount(int ncount)
    {
        String strText = "";
        if (ncount >0)
        {
            String s = KDSApplication.getContext().getString(R.string.park_count);
            //strText = "Park:" + KDSUtil.convertIntToString(ncount);
            strText = s + KDSUtil.convertIntToString(ncount);
        }

        if (KDSGlobalVariables.getKDS().isMultpleUsersMode())
        {
            TextView t =  (TextView)m_layoutScreenTitle.findViewById(m_nScreenTitleParkID);
            t.setText(strText);

        }
        else {
            if (m_txtParked != null) {
                m_txtParked.setText(strText);
            }
        }


    }

    public  void refreshAvgPrepTime(String strText)
    {


        if (KDSGlobalVariables.getKDS().isMultpleUsersMode())
        {
            TextView t =  (TextView)m_layoutScreenTitle.findViewById(m_nScreenTitleAvgPrepID);
            t.setText(strText);

        }
        else {
            if (m_txtAvgPrepTime != null) {
                m_txtAvgPrepTime.setText(strText);
            }
        }


    }

    public KDS getKDS()
    {
        return KDSGlobalVariables.getKDS();
    }

    public void updateSettings(KDSSettings settings)
    {
        if (m_layout != null)
        m_layout.updateSettings(settings);
    }

    public void showSummaryAlways( KDSUser.USER userID)
    {
        if (getKDS().getSettings().getBoolean(KDSSettings.ID.AdvSum_enabled) &&
                getKDS().getSettings().getBoolean(KDSSettings.ID.AdvSum_always_visible)) {
            int n = getKDS().getSettings().getInt(KDSSettings.ID.Sum_position);
            KDSSettings.SumPosition pos = KDSSettings.SumPosition.values()[n];

            showSum(userID, pos, true );
        }
    }


    class SumSimpleAdapter extends SimpleAdapter
    {
        public SumSimpleAdapter(Context context, List<? extends Map<String, ?>> data,
                             int resource, String[] from, int[] to)
        {
            super(context, data, resource, from, to);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            String bgfg = KDSGlobalVariables.getKDS().getSettings().getString(KDSSettings.ID.Sum_bgfg);
            KDSBGFG bf = KDSBGFG.parseString(bgfg);
            //m_viewTopSum.setBackgroundColor(bf.getBG());
            ((TextView)v.findViewById(R.id.text1)).setTextColor(bf.getFG());
            ((TextView)v.findViewById(R.id.text2)).setTextColor(bf.getFG());
            ((TextView)v.findViewById(R.id.txtSeparator)).setTextColor(bf.getFG());
            ((LinearLayout)v.findViewById(R.id.linearItem)).setGravity(Gravity.NO_GRAVITY);

            //Drawable drawable = Resources.getSystem().getDrawable(R.drawable.listview_border, null);
            //KDSApplication.getContext().getDrawable(R.drawable.listview_border);
            Drawable drawable = ContextCompat.getDrawable(KDSApplication.getContext(), R.drawable.listview_border);
            //Drawable drawable = Drawable.createFromXml( KDSApplication.getContext().getResources(), xp);
            ((LinearLayout)v.findViewById(R.id.linearItem)).setBackground(drawable);

            if (KDSGlobalVariables.getKDS().getSettings().getBoolean(KDSSettings.ID.AdvSum_enabled)) {
                ((LinearLayout)v.findViewById(R.id.linearItem)).setBackground(null);
                if (KDSGlobalVariables.getKDS().getSettings().getInt(KDSSettings.ID.LineItems_cols) == 1)
                    ((LinearLayout) v.findViewById(R.id.linearItem)).setGravity(Gravity.CENTER);
            }



            return v;
        }

    }

    /**
     * 2.0.25
     * @param v
     */
    public void setAvgPrepTimeView(TextView v)
    {
        if (m_txtAvgPrepTime != null)
        {
            if ( v == null)
                m_txtAvgPrepTime.setText("");
        }
        m_txtAvgPrepTime = v;

    }

    /**
     * 2.0.25
     * @return
     */
    public TextView getAvgPrepTimeView()
    {
        return m_txtAvgPrepTime;
    }

    /**
     * 2.0.25
     * @param seconds
     * @return
     */
    private String seconds2HMS(int seconds)
    {
        int h = (seconds / 3600);
        int m = ((seconds % 3600)/ 60);
        int s = (seconds % 60);
        m += (h*60);
        return String.format("%02d:%02d", m, s);


    }

    /**
     * 2.0.25
     * @param db
     * @param userID
     * @param nTimePeriod
     */
    public  void refreshAvgPrepTime(KDSDBStatistic db, KDSUser.USER userID, int nTimePeriod)
    {
        String strText = "";
        strText = KDSApplication.getContext().getString(R.string.avg_prep_time);

        int nAvgSeconds = db.getAvgPrepTimeForLocalShowing(userID, nTimePeriod);

        String s = seconds2HMS(nAvgSeconds);
        strText += s;
        hideAvgPrepTimeView(false);
        refreshAvgPrepTime(strText);
        //getAvgPrepTimeView().setText(strText);


    }

    public void hideAvgPrepTimeView(boolean bHide)
    {
        if (m_txtAvgPrepTime != null) {
            if (bHide)
                m_txtAvgPrepTime.setVisibility(View.GONE);
            else
                m_txtAvgPrepTime.setVisibility(View.VISIBLE);
        }
    }

}
