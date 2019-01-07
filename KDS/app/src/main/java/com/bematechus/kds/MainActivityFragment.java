package com.bematechus.kds;

import android.content.Context;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSViewFontFace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public interface OnTouchPadEventListener {

        public void onFragmentInteraction(KDSUser.USER userID, KDSTouchPadButton.TouchPadID id);
    }

    OnTouchPadEventListener m_listener = null;

    KDSLayout m_layoutA = null ;
    KDSLayout m_layoutB = null ;
    View m_linearA = null; // the linear for A
    View m_linearB = null;
    View m_topSumA = null; // the linear for A
    View m_topSumB = null;

    View m_viewFocusIndicatorUserA = null;
    View m_viewFocusIndicatorUserB = null;

    //TextView m_txtPrev;
    //TextView m_txtNext;

    ListView m_lstInfo = null;
    InfoAdapter m_infoAdapter = null;
    HorizontalListView m_touchPadA = null;
    HorizontalListView m_touchPadB = null;

    ListView m_touchPadVerticalA = null;
    ListView m_touchPadVerticalB = null;

    View m_layoutScreenATitle = null;
    View m_layoutScreenBTitle = null;

    ArrayList<Map<String,Object>> m_arData= new ArrayList<Map<String,Object>>();

    public MainActivityFragment() {
    }

    public void setListener(OnTouchPadEventListener listener)
    {
        m_listener = listener;
    }

    HorizontalListView getTouchPad(KDSUser.USER userID)
    {
        if (userID == KDSUser.USER.USER_A)
            return m_touchPadA;
        else
            return m_touchPadB;
    }

    ListView getVerticalTouchPad(KDSUser.USER userID)
    {
        if (userID == KDSUser.USER.USER_A)
            return m_touchPadVerticalA;
        else
            return m_touchPadVerticalB;
    }

    View getScreenTitleLayout(KDSUser.USER userID)
    {
        if (userID == KDSUser.USER.USER_A)
            return m_layoutScreenATitle;
        else
            return m_layoutScreenBTitle;
    }


    public void initButtons()
    {
        // m_arData.clear();
        KDS kds = KDSGlobalVariables.getKDS();

        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_prev))
            addButton(KDSTouchPadButton.TouchPadID.Prev, R.string.touchpad_prev,R.drawable.left_arrow,R.drawable.left_arrow_us);

        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_next))
             addButton(KDSTouchPadButton.TouchPadID.Next, R.string.touchpad_next, R.drawable.right_arrow, R.drawable.right_arrow_us);

        //2.0.25

        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_prev_page))
            addButton(KDSTouchPadButton.TouchPadID.Prev_Page, R.string.touchpad_prev_page,R.drawable.previous_page,R.drawable.previous_page_us); //2.0.28

        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_next_page))
            addButton(KDSTouchPadButton.TouchPadID.Next_Page, R.string.touchpad_next_page, R.drawable.next_page, R.drawable.next_page_us); //2.0.28


        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_up))
            addButton(KDSTouchPadButton.TouchPadID.Up, R.string.touchpad_up, R.drawable.up_arrow, R.drawable.up_arrow_us);
        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_down))
            addButton(KDSTouchPadButton.TouchPadID.Down, R.string.touchpad_down,R.drawable.down_arrow,R.drawable.down_arrow_us);
        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_bump))
            addButton(KDSTouchPadButton.TouchPadID.Bump, R.string.touchpad_bump,R.drawable.bump2_32px,R.drawable.bump2_32px);

        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_unbump_last))
            addButton(KDSTouchPadButton.TouchPadID.UnbumpLast, R.string.touchpad_unbump_last, R.drawable.unbump_last, R.drawable.unbump_last_us );

        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_unbump))
            addButton(KDSTouchPadButton.TouchPadID.Unbump, R.string.touchpad_unbump, R.drawable.unbump, R.drawable.unbump_us);


        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_sum))
            addButton(KDSTouchPadButton.TouchPadID.Sum, R.string.touchpad_sum, R.drawable.summary, R.drawable.summary_us);
        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_transfer))
            addButton(KDSTouchPadButton.TouchPadID.Transfer, R.string.touchpad_transfer,R.drawable.transfer,R.drawable.transfer_us);
        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_sort))
            addButton(KDSTouchPadButton.TouchPadID.Sort, R.string.touchpad_sort, R.drawable.sort, R.drawable.sort_us);


        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_park))
            addButton(KDSTouchPadButton.TouchPadID.Park, R.string.touchpad_park, R.drawable.park_32px, R.drawable.park_32px );
        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_unpark))
            addButton(KDSTouchPadButton.TouchPadID.Unpark, R.string.touchpad_unpark, R.drawable.unpark_32px, R.drawable.unpark_32px);
        //addButton(KDSTouchPadButton.TouchPadID.ActiveStations, R.string.touchpad_active, KDSSettings.ID.Bumpbar_More,kbdtype );
        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_print))
            addButton(KDSTouchPadButton.TouchPadID.Print, R.string.touchpad_print, R.drawable.ticket_print, R.drawable.ticket_print_us);
        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_more))
            addButton(KDSTouchPadButton.TouchPadID.More, R.string.touchpad_more,R.drawable.more_32px,R.drawable.more_32px);

        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_BuildCard))
            addButton(KDSTouchPadButton.TouchPadID.BuildCard, R.string.touchpad_buildcard,R.drawable.buildcard,R.drawable.buildcard_us);

        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_Training))
            addButton(KDSTouchPadButton.TouchPadID.Training, R.string.touchpad_training,R.drawable.training_video,R.drawable.training_video_us);

        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_page))
            addButton(KDSTouchPadButton.TouchPadID.Page, R.string.touchpad_page, R.drawable.guest_paging, R.drawable.guest_paging_us);

        if (kds.getSettings().getBoolean(KDSSettings.ID.Touch_test))
            addButton(KDSTouchPadButton.TouchPadID.Test, R.string.touchpad_test, R.drawable.testing, R.drawable.testing_us );
        //((SimpleAdapter) listView.getAdapter()).notifyDataSetChanged();


    }


    public void addButton(KDSTouchPadButton.TouchPadID id, int nStringID, int imgUp, int imgDown )
    {
        Map<String,Object> item = new HashMap<>();
        item.put("icon", imgUp);//.getDrawable(id);   this.getActivity().getDrawable(imgID));
        item.put("icondown", imgDown);
        item.put("btn", new KDSTouchPadButton(id, this.getString(nStringID)) );

        m_arData.add(item);
    }


    public void showButtons()
    {
        if (m_arData.size() <=0)
            initButtons();
        ((BaseAdapter) m_touchPadA.getAdapter()).notifyDataSetChanged();
        ((BaseAdapter) m_touchPadB.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        m_layoutA = new KDSLayout((KDSIOSView)v.findViewById(R.id.viewOrdersA));
        m_layoutB = new KDSLayout((KDSIOSView)v.findViewById(R.id.viewOrdersB));

        m_linearA = v.findViewById(R.id.linearA);
        m_linearB = v.findViewById(R.id.linearB);

        m_topSumA = v.findViewById(R.id.sumTopA);
        m_topSumB = v.findViewById(R.id.sumTopB);

        m_viewFocusIndicatorUserA = v.findViewById(R.id.viewUserAFocus);
        m_viewFocusIndicatorUserB = v.findViewById(R.id.viewUserBFocus);

        m_layoutScreenATitle = v.findViewById(R.id.layoutScrATitle);
        m_layoutScreenBTitle = v.findViewById(R.id.layoutScrBTitle);

        m_touchPadA =(HorizontalListView) v.findViewById(R.id.touchPadA);
        m_touchPadB =(HorizontalListView) v.findViewById(R.id.touchPadB);

        TouchAdapter adapter = new TouchAdapter(this.getActivity().getApplicationContext(), m_arData);
        m_touchPadA.setAdapter(adapter);
        m_touchPadB.setAdapter(adapter);


        m_touchPadVerticalA =(ListView) v.findViewById(R.id.lstTouchPadSideA);
        m_touchPadVerticalB =(ListView) v.findViewById(R.id.lstTouchPadSideB);
        m_touchPadVerticalA.setAdapter(adapter);
        m_touchPadVerticalB.setAdapter(adapter);

        init_touchpad_events();

        enableUserB(false);

        m_lstInfo = (ListView)v.findViewById(R.id.lstInfo);

        List<String> data = new ArrayList<String>();
        String strFirstInfo = this.getString(R.string.info_first_message);
        data.add(strFirstInfo);//




        m_infoAdapter = new InfoAdapter(this.getActivity(), data);
        m_lstInfo.setAdapter(m_infoAdapter);

        m_lstInfo.setFocusable(false);

        return v;
    }

    public void init_touchpad_events()
    {
        m_touchPadA.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (null != m_listener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    HashMap<String, Object> map = (HashMap<String, Object>) parent.getAdapter().getItem(position);
                    KDSTouchPadButton btn = (KDSTouchPadButton) map.get("btn");
                    m_listener.onFragmentInteraction(KDSUser.USER.USER_A, btn.getID());

                }
            }
        });

        m_touchPadB.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (null != m_listener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    HashMap<String, Object> map = (HashMap<String, Object>) parent.getAdapter().getItem(position);
                    KDSTouchPadButton btn = (KDSTouchPadButton) map.get("btn");
                    m_listener.onFragmentInteraction(KDSUser.USER.USER_B, btn.getID());

                }
            }
        });


        m_touchPadVerticalA.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (null != m_listener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    HashMap<String, Object> map = (HashMap<String, Object>) parent.getAdapter().getItem(position);
                    KDSTouchPadButton btn = (KDSTouchPadButton) map.get("btn");
                    m_listener.onFragmentInteraction(KDSUser.USER.USER_A, btn.getID());

                }
            }
        });

        m_touchPadVerticalB.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (null != m_listener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    HashMap<String, Object> map = (HashMap<String, Object>) parent.getAdapter().getItem(position);
                    KDSTouchPadButton btn = (KDSTouchPadButton) map.get("btn");
                    m_listener.onFragmentInteraction(KDSUser.USER.USER_B, btn.getID());

                }
            }
        });

        //for touch down/up events
        init_vertical_touch_button_down_up(m_touchPadVerticalA);

        init_horizontal_touch_button_down_up(m_touchPadA);
        init_horizontal_touch_button_down_up(m_touchPadB);


    }

    private void init_vertical_touch_button_down_up(ListView lst)
    {
        lst.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_DOWN: // 单击
                        int childCount = ((ListView)v).getChildCount();
                        for (int i = 0; i < childCount; i++) {
                            View child = ((ListView)v).getChildAt(i);
                            if (isEventWithinView(event, child)) {
                                HashMap<String, Object> map = (HashMap<String, Object>) ((ListView)v).getAdapter().getItem(i);
                                KDSTouchPadButton btn = (KDSTouchPadButton) map.get("btn");
                                btn.setPressDown( (event.getAction()==MotionEvent.ACTION_DOWN));
                                updateTouchButtonImage((ListView)v, i, map, btn);

                                break;
                            }

                        }
                        break;
                    case MotionEvent.ACTION_MOVE:

                        break;
                }


                return false;
            }
        });
    }


    private boolean isEventWithinView(MotionEvent e, View child) {
        Rect viewRect = new Rect();
        int[] childPosition = new int[2];
        child.getLocationOnScreen(childPosition);
        int left = childPosition[0];
        int right = left + child.getWidth();
        int top = childPosition[1];
        int bottom = top + child.getHeight();
        viewRect.set(left, top, right, bottom);
        return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
    }

    private void init_horizontal_touch_button_down_up(HorizontalListView v)
    {
        v.setOnItemClickDownListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                    HashMap<String, Object> map = (HashMap<String, Object>) parent.getAdapter().getItem(position);
                    KDSTouchPadButton btn = (KDSTouchPadButton) map.get("btn");
                    btn.setPressDown(true);
                updateTouchButtonImage(parent, position, map, btn);
            }
        });

        v.setOnItemClickUpListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                    HashMap<String, Object> map = (HashMap<String, Object>) parent.getAdapter().getItem(position);
                    KDSTouchPadButton btn = (KDSTouchPadButton) map.get("btn");
                    btn.setPressDown(false);
                updateTouchButtonImage(parent, position, map, btn);
            }
        });
    }

    private void updateTouchButtonImage(AdapterView<?> parent, int position,HashMap<String, Object> map, KDSTouchPadButton btn )
    {
        View v = parent.getChildAt(position);
        if (v == null) return;
        ImageView img = (ImageView) v.findViewById(R.id.imgTouch);
        if (img == null) return;
        TextView t = (TextView) v.findViewById(R.id.txtText);
        if (t == null) return;

        if (btn.m_bIsDown) {
            img.setImageResource((int) map.get("icondown"));
            t.setTextColor( getResources().getColor( R.color.touch_button_down_text_color));
        }
        else {
            img.setImageResource((int) map.get("icon"));
            t.setTextColor( getResources().getColor( R.color.touch_button_fg));
        }
    }

    public void enableUserB(boolean bEnable)
    {

        if (!bEnable) {
            m_layoutB.getView().setVisibility(View.GONE);

            m_layoutA.getView().setPadding(0, 0, 0, 0);

        }
        else
        {
            m_layoutB.getView().setVisibility(View.VISIBLE);
        }

    }
    public void refreshPreNextCount()
    {
        ((MainActivity)this.getActivity()).refreshPrevNext(KDSUser.USER.USER_A);//.updateTitleForPrevNext(strPrev, strNext);
        if (m_layoutB.getView().getVisibility() == View.VISIBLE)
        {
            ((MainActivity)this.getActivity()).refreshPrevNext(KDSUser.USER.USER_B);//.updateTitleForPrevNext(strPrev, strNext);
        }
    }



    public KDSLayout getLayout(KDSUser.USER userID)
    {
        if (userID == KDSUser.USER.USER_A)
            return getLayoutA();
        else
            return getLayoutB();
    }

    public View getLinear(KDSUser.USER userID)
    {
        if (userID == KDSUser.USER.USER_A)
            return getLinearA();
        else
            return getLinearB();
    }

    public View getTopSum(KDSUser.USER userID)
    {
        if (userID == KDSUser.USER.USER_A)
            return getTopSumA();
        else
            return getTopSumB();
    }

    public View getFocusIndicator(KDSUser.USER userID)
    {
        if (userID == KDSUser.USER.USER_A)
            return getFocusIndicatorA();
        else
            return getFocusIndicatorB();
    }



    public KDSLayout getLayoutA()
    {
        return m_layoutA;
    }

    public View getLinearA()
    {
        return m_linearA;
    }
    public View getLinearB()
    {
        return m_linearB;
    }

    public View getTopSumA()
    {
        return m_topSumA;
    }
    public View getTopSumB()
    {
        return m_topSumB;
    }

    public View getFocusIndicatorA()
    {
        return m_viewFocusIndicatorUserA;
    }
    public View getFocusIndicatorB()
    {
        return m_viewFocusIndicatorUserB;
    }

    public ListView getInfoListView()
    {
        return m_lstInfo;
    }

    public KDSLayout getLayoutB()
    {
        return m_layoutB;
    }
    public void focusNext(KDSUser.USER userID)
    {
        getLayout(userID).focusNext();
        refreshPreNextCount();

    }
    public void focusPrev(KDSUser.USER userID)
    {
        getLayout(userID).focusPrev();
        refreshPreNextCount();

    }



    public void clearInfo()
    {
        m_infoAdapter.m_listData.clear();
        m_infoAdapter.notifyDataSetChanged();
    }

    public  boolean showInfo(String s)
    {
        String lastInfo = "";
        //don't show too many informations, memory lost
        if (m_infoAdapter.m_listData.size() > KDSConst.MAX_INFORMATION_COUNT)
        {
            int ncount =m_infoAdapter.m_listData.size() - KDSConst.MAX_INFORMATION_COUNT;
            for (int i=0; i< ncount; i++)
            {
                m_infoAdapter.m_listData.remove(0);
            }

        }


        if (m_infoAdapter.m_listData.size() >0) {
            lastInfo = m_infoAdapter.m_listData.get(m_infoAdapter.m_listData.size()-1);

        }
        if (lastInfo.equals(s))
            return false;
        m_infoAdapter.m_listData.add(s);

        m_lstInfo.setSelection(m_infoAdapter.getCount());
        m_infoAdapter.notifyDataSetChanged();
        return true;
       // return false;
    }

    public void updateTimer(KDSUser.USER userID)
    {
        if (userID == KDSUser.USER.USER_A) {
            m_layoutA.refreshTimer();
            m_layoutA.checkAlertSound();
        }
        else {

            m_layoutB.refreshTimer();
            m_layoutB.checkAlertSound();
        }
    }


    /*************************************************************************************************/
    public final class ViewHolder {
        public TextView m_txtInfo;

    }

    public class InfoAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        List<String> m_listData;

        public InfoAdapter(Context context, List<String> data) {
            this.mInflater = LayoutInflater.from(context);
            m_listData = data;
        }
        public int getCount() {

            return m_listData.size();
        }
        public Object getItem(int arg0) {

            return m_listData.get(arg0);
        }
        public long getItemId(int arg0) {

            return arg0;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.information_listview, null);
                holder.m_txtInfo = (TextView) convertView.findViewById(R.id.txtInfo);


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.m_txtInfo.setText((String) m_listData.get(position));
            return convertView;
        }
    }
    @Override
    public void onResume()
    {
        super.onResume();

    }
    public void onStart()
    {
        super.onStart();

    }

    public void resetButtons()
    {

        m_arData.clear();
        initButtons();
    }

    public void updateSettings(KDSSettings settings)
    {
        KDSViewFontFace ff = settings.getKDSViewFontFace(KDSSettings.ID.Touch_fontface);
        m_touchPadA.setBackgroundColor(ff.getBG());
        m_touchPadB.setBackgroundColor(ff.getBG());
        resetButtons();
        ((BaseAdapter)m_touchPadA.getAdapter()).notifyDataSetChanged();
        ((BaseAdapter)m_touchPadB.getAdapter()).notifyDataSetChanged();
        updateSubtitle(settings);
        m_layoutA.updateSettings(settings);
        m_layoutB.updateSettings(settings);
    }

    public void updateSubtitle(KDSSettings settings)
    {
        if (KDSGlobalVariables.getKDS().isMultpleUsersMode()) {
            KDSViewFontFace ff = settings.getKDSViewFontFace(KDSSettings.ID.Screen_subtitle_font);
            int[] ar = new int[]{
                    R.id.txtScrAPrev,R.id.txtScrATitle,R.id.txtScrAParked,R.id.txtScrANext,R.id.txtScrAAvgPrep,
                    R.id.txtScrBPrev,R.id.txtScrBTitle,R.id.txtScrBParked,R.id.txtScrBNext,R.id.txtScrBAvgPrep,

            };
            for (int i = 0; i < ar.length; i++) {
                TextView v = (TextView) this.getView().findViewById(ar[i]);

                v.setTextColor(ff.getFG());
                v.setTypeface(ff.getTypeFace());
                v.setTextSize(ff.getFontSize());
            }

            TextView v = (TextView) this.getView().findViewById(R.id.txtScrATitle);
            v.setText(settings.getString(KDSSettings.ID.Screen_subtitle_a_text));

            TextView tvB = (TextView) this.getView().findViewById(R.id.txtScrBTitle);
            tvB.setText(settings.getString(KDSSettings.ID.Screen_subtitle_b_text));

            View tvALayout = this.getView().findViewById(R.id.layoutScrATitle);
            tvALayout.setBackgroundColor(ff.getBG());

            View tvBLayout = this.getView().findViewById(R.id.layoutScrBTitle);
            tvBLayout.setBackgroundColor(ff.getBG());

        }
    }


    public class TouchAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        ArrayList<Map<String,Object>> m_listData= null;

        public TouchAdapter(Context context,  ArrayList<Map<String,Object>> data) {
            this.mInflater = LayoutInflater.from(context);
            m_listData = data;
        }
        public int getCount() {

            return m_listData.size();
        }
        public Object getItem(int arg0) {

            return m_listData.get(arg0);
        }
        public long getItemId(int arg0) {

            return arg0;
        }
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.listitem_horizontal_touchpad, null);

            } else {

            }
            KDS kds = KDSGlobalVariables.getKDS();

            TextView t = (TextView) convertView.findViewById(R.id.txtText);

            GradientDrawable drawable = (GradientDrawable)convertView.getBackground();
            drawable.setStroke(1,kds.getSettings().getInt(KDSSettings.ID.Panels_BG)) ;

            KDSViewFontFace ff = kds.getSettings().getKDSViewFontFace(KDSSettings.ID.Touch_fontface);

           // convertView.setBackgroundColor(ff.getBG());
            //t.setTextColor(ff.getFG());
            t.setTextColor(ff.getFG());
            t.setTypeface(ff.getTypeFace());
            t.setTextSize(ff.getFontSize());
            Map<String, Object> map =( Map<String, Object>) this.getItem(position);
            KDSTouchPadButton btn = (KDSTouchPadButton) map.get("btn");
            t.setText( btn.getText());
            ImageView img = (ImageView) convertView.findViewById(R.id.imgTouch);
            if (btn.m_bIsDown) {
                img.setImageResource((int) map.get("icondown"));
                t.setTextColor( getResources().getColor( R.color.touch_button_down_text_color));
            }
            else {
                img.setImageResource((int) map.get("icon"));
                t.setTextColor( getResources().getColor( R.color.touch_button_fg));
            }

            return convertView;
        }
    }
}
