package com.bematechus.kds;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.Shape;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/29.
 */
public class TabDisplay {

    public interface TabDisplayEvents
    {
        void onTabClicked(TabButtonData btnData);
    }

    private TabDisplayEvents m_eventsReceiver = null;

    HorizontalListView m_lstTab = null;
    View m_layoutTab = null;

    //ArrayList<Map<String,Object>> m_arData= new ArrayList<Map<String,Object>>();
    ArrayList<TabButtonData> m_arData = new ArrayList<>();

    public void setEventsReceiver(TabDisplayEvents receiver)
    {
        m_eventsReceiver = receiver;
    }

    public void setLinearLayout(View v)
    {
        m_layoutTab = v;
        m_lstTab = (HorizontalListView)v.findViewById(R.id.lstTab);
        init_tab_list();
    }
    public void hide()
    {
        m_layoutTab.setVisibility(View.GONE);
    }

    KDSBGFG m_bgfg = new KDSBGFG();
    public void show()
    {
        m_layoutTab.setVisibility(View.VISIBLE);
        String s =  getSettings().getString(KDSSettings.ID.Tab_bgfg);
        m_bgfg = KDSBGFG.parseString(s);
        m_layoutTab.setBackgroundColor(m_bgfg.getBG());
        resetButtons();
        setDefault();
    }
    private void setDefault()
    {
        for (int i=0; i< m_arData.size(); i++)
        {
            if (m_arData.get(i).getFunc() == KDSSettings.TabFunction.Orders) {
                m_eventsReceiver.onTabClicked(m_arData.get(i));
                ((MyAdapter)m_lstTab.getAdapter()).setSelected(m_arData.get(i));
                changeDisplayMode(m_arData.get(i));//kpp1-288
                return ;
            }
        }
    }
    private void init_tab_list()
    {
        initArray();
       // ArrayAdapter adapter =new ArrayAdapter<>(KDSApplication.getContext(),R.layout.list_item_tab, m_arData);
        ArrayAdapter adapter =new MyAdapter(KDSApplication.getContext(), m_arData);
        m_lstTab.setAdapter(adapter);
        m_lstTab.setDrawSplitLine(true);
        adapter.notifyDataSetChanged();

        m_lstTab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                changeDisplayMode(m_arData.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public KDSSettings getSettings()
    {
        return  KDSGlobalVariables.getKDS().getSettings();
    }
    public void resetButtons()
    {
        String buttons = getSettings().getString(KDSSettings.ID.Tab_buttons);
        String destinations = getSettings().getString(KDSSettings.ID.Tab_destinations);
        String sortsMode =  getSettings().getString(KDSSettings.ID.Tab_sort_modes);

        ArrayList<TabButtonData> arButtons = parseTabButtons(buttons);
        removeDisabledButtons(arButtons);
        resetButtonDescription(arButtons, false);

        //replace the dest
        int nDestinationIndex = findDestinationIndex(arButtons);
        if (nDestinationIndex != -1) {

            ArrayList<String> arDest = parseDestination(destinations);
            ArrayList<TabButtonData> arDestButtons = new ArrayList<>();
            for (int i = 0; i < arDest.size(); i++) {
                String s = arDest.get(i);
                ArrayList<String> ar = parseDestinationSaveString(s);
                TabButtonData data = new TabButtonData(ar.get(1), KDSSettings.TabFunction.Destination, true);
                data.setStringParam(ar.get(0));
                arDestButtons.add(data);
            }
            arButtons.remove(nDestinationIndex);
            arButtons.addAll(nDestinationIndex, arDestButtons);
        }
        //replace the sort mode
        int nSortModeIndex = findSortModeIndex(arButtons);
        if (nSortModeIndex != -1) {

            ArrayList<KDSSortModeItem> arSorts = KDSSortModeItem.parseSortModeSaveString(sortsMode);
            ArrayList<TabButtonData> arSortButtons = new ArrayList<>();
            for (int i = 0; i < arSorts.size(); i++) {
                KDSSortModeItem sort = arSorts.get(i);

                TabButtonData data = new TabButtonData(sort.getShowingText(), KDSSettings.TabFunction.Sort_orders, true);
                data.setStringParam(KDSUtil.convertIntToString( sort.getSortMethod().ordinal()));
                arSortButtons.add(data);
            }
            arButtons.remove(nSortModeIndex);
            arButtons.addAll(nSortModeIndex, arSortButtons);
        }

        m_arData.clear();
        m_arData.addAll(arButtons);
        ((ArrayAdapter)m_lstTab.getAdapter()).notifyDataSetChanged();
    }
    private int findDestinationIndex( ArrayList<TabButtonData> arButtons)
    {
        for (int i=0; i< arButtons.size(); i++)
        {
            if (!arButtons.get(i).getEnabled()) continue;
            if (arButtons.get(i).getFunc() == KDSSettings.TabFunction.Destination)
                return i;
        }
        return -1;
    }

    private int findSortModeIndex(ArrayList<TabButtonData> arButtons)
    {
        for (int i=0; i< arButtons.size(); i++)
        {
            if (!arButtons.get(i).getEnabled()) continue;
            if (arButtons.get(i).getFunc() == KDSSettings.TabFunction.Sort_orders)
                return i;
        }
        return -1;
    }
    /**
     * Expeditor tabs - Orders, Destination, Order Queue, Tracker, Pager
     */
    public void initArray()
    {
        resetButtonDescription(m_arData, false);



    }




    static public String getTabFuncDescription(KDSSettings.TabFunction func)
    {
        switch (func)
        {

            case Orders:
                return "Orders";
                //break;
            case Destination:
                return "Destination";

            case Queue:
                return "Queue";

//            case TableTracker://kpp1-406, remove it.
//                return "Table Tracker";

            case LineItems:
                return "Line items";
            case Sort_orders:
                return "Sorted orders";

        }
        return "";
    }

    public void changeDisplayMode(TabButtonData btnData)
    {
        if (m_eventsReceiver != null)
            m_eventsReceiver.onTabClicked(btnData);
    }

    static public final String TABDISP_SEPARATOR = "\n";

    static public String DEST_SEPARATOR = "-->";

    public static String createDestinationSaveString(String destination, String displayText)
    {
        return destination + DEST_SEPARATOR + displayText;
    }
    public static ArrayList<String> parseDestinationSaveString(String destString)
    {
        ArrayList<String> ar = new ArrayList<>();
        return KDSUtil.spliteString(destString, DEST_SEPARATOR);
    }
    public void removeDisabledButtons(ArrayList<TabDisplay.TabButtonData> arButtons)
    {
        for (int i=arButtons.size()-1; i>=0;  i--)
        {
            if (!arButtons.get(i).getEnabled())
                arButtons.remove(i);
        }
    }
    static public void resetButtonDescription(ArrayList<TabDisplay.TabButtonData> ar, boolean bAddDefault)
    {
        if (ar.size() <=0)
        {
            if (!bAddDefault) return;
            ar.add(new TabButtonData(KDSSettings.TabFunction.Orders, true));

            ar.add(new TabButtonData( KDSSettings.TabFunction.Destination, false));
            ar.add(new TabButtonData( KDSSettings.TabFunction.Queue,false));
            //ar.add(new TabButtonData( KDSSettings.TabFunction.TableTracker,false)); //kpp1-406, remove it.
            ar.add(new TabButtonData( KDSSettings.TabFunction.LineItems,false));
            ar.add(new TabButtonData( KDSSettings.TabFunction.Sort_orders,false));
        }
        else
        {
            for (int i=0; i< ar.size(); i++)
            {
                TabDisplay.TabButtonData data = ar.get(i);
                data.setDescription(TabDisplay.getTabFuncDescription(data.getFunc()));
            }
            //I forget why I do this, it seems this is a bug. Just remove it.
//            if (ar.size() == (KDSSettings.TabFunction.MAX_COUNT.ordinal()-1)) //we add a new "sort order" view
//            {
//                ar.add(new TabButtonData( KDSSettings.TabFunction.Sort_orders,false));
//                TabDisplay.TabButtonData data = ar.get(ar.size()-1);
//                data.setDescription(TabDisplay.getTabFuncDescription(data.getFunc()));
//            }
        }
    }

    static public String getDestinationsString( ArrayList<String> ar)
    {
        String s = "";
        for (int i=0; i< ar.size(); i++)
        {
            String dest = ar.get(i);

            if (!s.isEmpty())
            {
                s += TABDISP_SEPARATOR;
            }
            s += dest;


        }
        return s;
    }

    static public ArrayList<String> parseDestination(String s)
    {

        ArrayList<String> ar = KDSUtil.spliteString(s, TABDISP_SEPARATOR);
        for (int i=ar.size()-1; i>=0; i--)
        {
            if (ar.get(i).isEmpty())
                ar.remove(i);
        }

        return ar;

    }

    static public String getTabButtonsString(ArrayList<TabDisplay.TabButtonData> ar)
    {

        String s = "";
        for (int i=0; i< ar.size(); i++)
        {
            TabDisplay.TabButtonData tab = ar.get(i);

            if (!s.isEmpty())
            {
                s += TABDISP_SEPARATOR;
            }
            s += tab.toSaveString();
        }
        return s;

    }

    static public ArrayList<TabDisplay.TabButtonData> parseTabButtons(String s)
    {
        ArrayList<String> ar = KDSUtil.spliteString(s, TABDISP_SEPARATOR);
        for (int i=ar.size()-1; i>=0; i--)
        {
            if (ar.get(i).isEmpty())
                ar.remove(i);
        }

        ArrayList<TabDisplay.TabButtonData> arButtons = new ArrayList<>();
        for (int i=0; i< ar.size(); i++)
        {
            TabDisplay t = new TabDisplay();
            TabDisplay.TabButtonData btn = new TabButtonData("", KDSSettings.TabFunction.Orders);
            btn.parseSaveString(ar.get(i));
            if (btn.getFunc() == KDSSettings.TabFunction.TableTracker) continue; //kpp1-406, remove table tracker
            arButtons.add(btn);

        }
        return arButtons;

    }

    public TabButtonData getNextTabDisplayMode()
    {
        return ((MyAdapter) m_lstTab.getAdapter()).getNext();

    }

    public void setFocus(TabButtonData data)
    {
        ((MyAdapter) m_lstTab.getAdapter()).setSelected(data);

    }

    public static class TabButtonData
    {
        static public final String PARAM_SEPARATOR = ":";

        String m_strDescription = "";
        KDSSettings.TabFunction m_tabFunc = KDSSettings.TabFunction.Orders;
        boolean m_bEnabled = true;
        String m_strDisplayText = ""; //display text in button. Show this while tab enabled

        int m_nParam = 0;
        String m_strParam = "";

        public void setStringParam(String s)
        {
            m_strParam = s;
        }
        public String getStringParam()
        {
            return m_strParam;
        }

        public TabButtonData( KDSSettings.TabFunction func)
        {
            m_strDescription = getTabFuncDescription(func);// strDescription;
            m_tabFunc = func;
            m_bEnabled = true;
            m_strDisplayText = "";
        }

        public TabButtonData( KDSSettings.TabFunction func, boolean bEnabled)
        {
            m_strDescription = getTabFuncDescription(func);// strDescription;
            m_tabFunc = func;
            m_bEnabled = bEnabled;

        }

        public TabButtonData(String strDescription, KDSSettings.TabFunction func)
        {
            m_strDescription = strDescription;
            m_tabFunc = func;
        }

        public TabButtonData(String strDescription, KDSSettings.TabFunction func, boolean bEnabled)
        {
            m_strDescription = strDescription;
            m_tabFunc = func;
            m_bEnabled = bEnabled;
        }

        public String getShowingText()
        {
            if (m_strDisplayText.isEmpty())
                return m_strDescription;
            else
                return  m_strDisplayText;
        }
        public void setDisplayText(String displayText)
        {
            m_strDisplayText = displayText;
        }

        public String getDisplayText()
        {
            return m_strDisplayText;
        }

        public void setDescription(String strDescription)
        {
            m_strDescription = strDescription;
        }
        public KDSSettings.TabFunction getFunc()
        {
            return m_tabFunc;
        }

        public void setEnabled(boolean bEnabled)
        {
            m_bEnabled = bEnabled;
        }
        public boolean getEnabled()
        {
            return m_bEnabled;
        }

        public String toString()
        {
            if (m_strDisplayText.isEmpty())
                return m_strDescription;
            else
                return m_strDescription + " --> " + m_strDisplayText;
        }

        public String toSaveString()
        {
//            String s = m_strDescription;
//            s += PARAM_SEPARATOR;
            String s = KDSUtil.convertIntToString(m_tabFunc.ordinal());
            s += PARAM_SEPARATOR;
            s += m_bEnabled?"1":"0";
            s += PARAM_SEPARATOR;
            s += m_strDisplayText;
            return s;
        }
        public void parseSaveString(String s)
        {
            ArrayList<String> ar = KDSUtil.spliteString(s, PARAM_SEPARATOR);
            if (ar.size()<2) return;
            String str = ar.get(0);
            int n = KDSUtil.convertStringToInt(str, 0);
            m_tabFunc = KDSSettings.TabFunction.values()[n];

            str = ar.get(1);

            m_bEnabled = str.equals("1");

            if (ar.size() >2)
                m_strDisplayText = ar.get(2);
            else
                m_strDisplayText = "";

        }
    }

    private class MyAdapter extends ArrayAdapter<TabDisplay.TabButtonData> {


        TabDisplay.TabButtonData m_selected = null;
        //private LayoutInflater mInflater;
        //public List<Map<String, Object>> m_listData; //KDSStationsRelation class array
        public List<TabDisplay.TabButtonData> m_listData; //KDSStationsRelation class array


        public MyAdapter(Context context, List<TabDisplay.TabButtonData> data) {
            super(context, R.layout.list_item_tab,android.R.id.text1, data);
            // this.mInflater = LayoutInflater.from(context);
            m_listData = data;
        }

        public  void setSelected(TabDisplay.TabButtonData data)
        {
            if (m_selected != data) {
                m_selected = data;
                this.notifyDataSetChanged();
            }
        }
        public TabDisplay.TabButtonData getSelected()
        {
            return m_selected;
        }
        public int getSelectedIndex()
        {
            for (int i=0; i< m_listData.size(); i++)
            {
                if (m_listData.get(i) == m_selected)
                    return i;
            }
            return -1;
        }

        public TabButtonData getNext()
        {
            int n = getSelectedIndex();
            n ++;
            if (n>=m_listData.size())
                n = 0;
            if (m_listData.size() >0)
                return m_listData.get(n);
            else
                return null;
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            //ViewHolder holder = null;
            View v = super.getView(position, convertView, parent);

            TabDisplay.TabButtonData r =  m_listData.get(position);


            v.setTag(r);

            TextView t = ((TextView) v.findViewById(android.R.id.text1));
            t.setPadding(25, 0, 25, 0);
            //t.setText(r.toString());
            t.setText(r.getShowingText());

            //t.setBackground(null);

            if (r == m_selected)
            {
                t.setBackgroundColor(m_bgfg.getFG());
                t.setTextColor(m_bgfg.getBG());

                //t.setBackgroundColor(Color.RED);
//                GradientDrawable d= (GradientDrawable) ContextCompat.getDrawable(getContext(), R.drawable.tab_button_selected);
//                d.setColors(new int[] {m_bgfg.getBG(),m_bgfg.getFG()});
//                t.setBackground(d);
                //t.invalidate();
            }
            else
            {
                t.setTextColor(m_bgfg.getFG());
                t.setBackgroundColor(m_bgfg.getBG());
//                t.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.tab_button_normal));
                //t.invalidate();
            }
            //t.setText(r.getShowingText());
            //t.setTextColor(m_bgfg.getFG());

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_selected = (TabDisplay.TabButtonData) v.getTag();
                    notifyDataSetChanged();
                }
            });



            return v;
        }

    }




}
