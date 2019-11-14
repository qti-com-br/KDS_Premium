package com.bematechus.kds;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUIBGFGPickerDialog;
import com.bematechus.kdslib.KDSUIDialogBase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/10.
 */
public class PreferenceFragmentTabDisplay extends KDSUIConfiguration.KDSPreferenceFragment implements  KDSUIDialogBase.KDSDialogBaseListener{

    private static final String TAG = "TabDispPref";

    //////////////////////
    ListView m_lstTab = null;
    ListView m_lstDest = null;
    ListView m_lstSorted = null;

    CheckBox m_chkEnableTab = null;
    View m_view = null;
    TextView m_btnBG = null;


    ArrayList<TabDisplay.TabButtonData> m_arDataTab =  new ArrayList<>();
    ArrayList<String> m_arDataDest =  new ArrayList<>();
    ArrayList<KDSSortModeItem> m_arDataSorted =  new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.preui_tab_display, container, false);
        view.setBackgroundColor(this.getResources().getColor(R.color.settings_page_bg));
        init_variables(view);

        return view;

    }

    private void onTabItemClicked()
    {
        ArrayList<Integer> ar = new ArrayList<>();
        ar.add(R.id.btnTabUp);
        ar.add(R.id.btnTabDown);
        ar.add(R.id.btnDisplayText);
        TabDisplay.TabButtonData data =((MyAdapter) m_lstTab.getAdapter()).getSelected();//.getSelectedItemPosition();//.getCheckedItemPosition();

        enableButtons( ar, (data != null));

    }
    private void onDestItemClicked()
    {
        ArrayList<Integer> ar = new ArrayList<>();
        ar.add(R.id.btnDestUp);
        ar.add(R.id.btnDestDown);
        ar.add(R.id.btnDestEdit);
        ar.add(R.id.btnDestRemove);

        enableButtons( ar, (m_lstDest.getCheckedItemPosition() >=0));
    }

    private void onSortedItemClicked()
    {
        ArrayList<Integer> ar = new ArrayList<>();
        ar.add(R.id.btnSortedUp);
        ar.add(R.id.btnSortedDown);
        ar.add(R.id.btnSortedEdit);
        ar.add(R.id.btnSortedRemove);

        enableButtons( ar, (m_lstSorted.getCheckedItemPosition() >=0));
    }

    private void enableButtons(ArrayList<Integer> ids, boolean bEnable)
    {
        for (int i=0; i< ids.size(); i++)
        {
            View v = m_view.findViewById(ids.get(i));
            v.setEnabled(bEnable);
        }
    }
    protected void init_variables(View view) {

        m_view = view;
        m_chkEnableTab = (CheckBox)view.findViewById(R.id.chkEnableTab);


        m_lstTab = (ListView)view.findViewById(R.id.lstTab);
        //ArrayAdapter<TabDisplay.TabButtonData> adapter = new ArrayAdapter<>(KDSApplication.getContext(),R.layout.listitem_tab_display_mode, m_arDataTab);
        MyAdapter adapter = new MyAdapter(KDSApplication.getContext(), m_arDataTab);
        //ArrayAdapter<String> adapter = new ArrayAdapter<>(KDSApplication.getContext(),R.layout.simple_list_item_mul_choice, m_arData);
        m_lstTab.setAdapter(adapter);
        m_lstTab.setFastScrollAlwaysVisible(true);

        m_lstDest = (ListView)view.findViewById(R.id.lstDest);
        ArrayAdapter<String> adapterDest = new ArrayAdapter<String>(KDSApplication.getContext(),R.layout.simple_list_item_single_choice, m_arDataDest);
        m_lstDest.setAdapter(adapterDest);
        m_lstDest.setFastScrollAlwaysVisible(true);

        m_lstDest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onDestItemClicked();
            }
        });


        Button btn =  (Button)view.findViewById(R.id.btnTabUp);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTabUp();
            }
        });

        btn =  (Button)view.findViewById(R.id.btnTabDown);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTabDown();
            }
        });


        btn = (Button)view.findViewById(R.id.btnDestNew);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestNew(v);
            }
        });

        Button btnDel =  (Button)view.findViewById(R.id.btnDestRemove);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestDel(v);
            }
        });

        m_chkEnableTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });



        btn = (Button)view.findViewById(R.id.btnDestUp);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestMoveUp();
            }
        });


        btn = (Button)view.findViewById(R.id.btnDestDown);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestMoveDown();
            }
        });


        btn = (Button)view.findViewById(R.id.btnDestEdit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestEdit();
            }
        });

/////////////
        m_lstSorted = (ListView)view.findViewById(R.id.lstSorted);
        ArrayAdapter<KDSSortModeItem> adapterSorted = new ArrayAdapter<KDSSortModeItem>(KDSApplication.getContext(),R.layout.simple_list_item_single_choice, m_arDataSorted);
        m_lstSorted.setAdapter(adapterSorted);
        m_lstSorted.setFastScrollAlwaysVisible(true);

        m_lstSorted.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onSortedItemClicked();
            }
        });

        btn = (Button)view.findViewById(R.id.btnSortedNew);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSortedNew(v);
            }
        });

        btn =  (Button)view.findViewById(R.id.btnSortedRemove);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSortedDel(v);
            }
        });


        btn = (Button)view.findViewById(R.id.btnSortedUp);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSortedMoveUp();
            }
        });


        btn = (Button)view.findViewById(R.id.btnSortedDown);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSortedMoveDown();
            }
        });


        btn = (Button)view.findViewById(R.id.btnSortedEdit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSortedEdit();
            }
        });

        /////////////////////////////////

        TextView t = (TextView)view.findViewById(R.id.btnBG);
        m_btnBG = t;
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBgClicked();
            }
        });


        btn = (Button) view.findViewById(R.id.btnDisplayText);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDisplayText();
            }
        });

        load();
        adapter.notifyDataSetChanged();
        adapterDest.notifyDataSetChanged();

        adapterSorted.notifyDataSetChanged();

        resetTabButtonsChecked();

        onTabItemClicked();
        onDestItemClicked();
    }

    KDSBGFG m_valueBGFG = new KDSBGFG();
    private void onBgClicked()
    {

        new KDSUIBGFGPickerDialog(getActivity(), m_valueBGFG, new KDSUIBGFGPickerDialog.OnBGFGPickerDlgListener() {
            @Override
            public void onCancel(KDSUIBGFGPickerDialog dialog) {

            }

            @Override
            public void onOk(KDSUIBGFGPickerDialog dialog, KDSBGFG ff) {


                m_valueBGFG.copyFrom(ff);
                m_btnBG.setTextColor(m_valueBGFG.getFG());
                m_btnBG.setBackgroundColor(m_valueBGFG.getBG());
                save();

            }
        }).show();

    }
    private void resetTabButtonsChecked()
    {
    }

    private void onTabUp()
    {
        TabDisplay.TabButtonData data =((MyAdapter) m_lstTab.getAdapter()).getSelected();//.getSelectedItemPosition();//.getCheckedItemPosition();
        if (data == null) return;
        int nindex = m_arDataTab.indexOf(data);
        if (nindex <0) return;
        if (nindex>m_arDataTab.size()-1) return;
        if (nindex ==0) return;

        m_arDataTab.remove(data);
        m_arDataTab.add(nindex-1, data);

        ((ArrayAdapter)m_lstTab.getAdapter()).notifyDataSetChanged();
        m_lstTab.setItemChecked(nindex-1, true);
        save();
    }

    private void onTabDown()
    {
        TabDisplay.TabButtonData data =((MyAdapter) m_lstTab.getAdapter()).getSelected();//.getSelectedItemPosition();//.getCheckedItemPosition();
        if (data == null) return;
        int nindex = m_arDataTab.indexOf(data);

        if (nindex <0) return;
        if (nindex>=m_arDataTab.size()-1) return;

        m_arDataTab.remove(nindex);
        m_arDataTab.add(nindex+1, data);
        ((ArrayAdapter)m_lstTab.getAdapter()).notifyDataSetChanged();
        m_lstTab.setItemChecked(nindex+1, true);
        save();
    }

    public void onDestMoveUp()
    {
        int nindex = m_lstDest.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>m_arDataDest.size()-1) return;
        if (nindex ==0) return;
        String s= m_arDataDest.get(nindex);
        m_arDataDest.remove(nindex);
        m_arDataDest.add(nindex-1, s);

        ((ArrayAdapter)m_lstDest.getAdapter()).notifyDataSetChanged();
        m_lstDest.setItemChecked(nindex-1, true);
        save();
    }
    public void onDestMoveDown()
    {
        int nindex = m_lstDest.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>=m_arDataDest.size()-1) return;


        String s= m_arDataDest.get(nindex);
        m_arDataDest.remove(nindex);
        m_arDataDest.add(nindex+1, s);
        ((ArrayAdapter)m_lstDest.getAdapter()).notifyDataSetChanged();
        m_lstDest.setItemChecked(nindex+1, true);
        save();
    }
    public void onDestDel(View v)
    {

        int nindex = m_lstDest.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>m_arDataDest.size()-1) return;

        m_arDataDest.remove(nindex);
        save();
        ((ArrayAdapter)m_lstDest.getAdapter()).notifyDataSetChanged();

    }

    public void onDestNew(View v)
    {
        if (this.getActivity() == null) return;
        KDSUIDlgInputDest dlg = new KDSUIDlgInputDest(this.getActivity(), this, "");
        dlg.show();


    }

    public void  onDestEdit()
    {
        if (this.getActivity() == null) return;
        int nindex = m_lstDest.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>m_arDataDest.size()-1) return;
        String s = m_arDataDest.get(nindex);
        KDSUIDlgInputDest dlg = new KDSUIDlgInputDest(this.getActivity(), this,s);
        dlg.show();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void onSortedMoveUp()
    {
        int nindex = m_lstSorted.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>m_arDataSorted.size()-1) return;
        if (nindex ==0) return;
        KDSSortModeItem item= m_arDataSorted.get(nindex);
        m_arDataSorted.remove(nindex);
        m_arDataSorted.add(nindex-1, item);

        ((ArrayAdapter)m_lstSorted.getAdapter()).notifyDataSetChanged();
        m_lstSorted.setItemChecked(nindex-1, true);
        save();
    }
    public void onSortedMoveDown()
    {
        int nindex = m_lstSorted.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>=m_arDataSorted.size()-1) return;


        KDSSortModeItem s= m_arDataSorted.get(nindex);
        m_arDataSorted.remove(nindex);
        m_arDataSorted.add(nindex+1, s);
        ((ArrayAdapter)m_lstSorted.getAdapter()).notifyDataSetChanged();
        m_lstSorted.setItemChecked(nindex+1, true);
        save();
    }
    public void onSortedDel(View v)
    {

        int nindex = m_lstSorted.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>m_arDataSorted.size()-1) return;

        m_arDataSorted.remove(nindex);
        save();
        ((ArrayAdapter)m_lstSorted.getAdapter()).notifyDataSetChanged();

    }

    public void onSortedNew(View v)
    {
        if (this.getActivity() == null) return;
        KDSUIDlgInputSortedMode dlg = new KDSUIDlgInputSortedMode(this.getActivity(), this, null);
        dlg.show();


    }

    public void  onSortedEdit()
    {
        if (this.getActivity() == null) return;
        int nindex = m_lstSorted.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>m_arDataSorted.size()-1) return;
        KDSSortModeItem s = m_arDataSorted.get(nindex);
        KDSUIDlgInputSortedMode dlg = new KDSUIDlgInputSortedMode(this.getActivity(), this,s);
        dlg.show();
    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {

        if (dialog instanceof KDSUIDlgInputDest) {

            KDSUIDlgInputDest dlg = (KDSUIDlgInputDest) dialog;
            String s = (String) dlg.getResult();
            if (findItem(m_arDataDest, s)) return;
            int nExisted = findDestination(m_arDataDest, dlg.getDescription());
            if (nExisted < 0) {
                m_arDataDest.add(s);
            } else {
                m_arDataDest.remove(nExisted);
                m_arDataDest.add(nExisted, s);
            }
            ((ArrayAdapter) m_lstDest.getAdapter()).notifyDataSetChanged();
            save();
        }
        else if (dialog instanceof KDSUIDlgInputTabDisplayText)
        {
            KDSUIDlgInputTabDisplayText dlg = (KDSUIDlgInputTabDisplayText) dialog;
            if (dlg.getButtonData() == null) return;
            dlg.getButtonData().setDisplayText((String)dlg.getResult());
            ((ArrayAdapter)m_lstTab.getAdapter()).notifyDataSetChanged();

            save();
        }
        else if (dialog instanceof  KDSUIDlgInputSortedMode)
        {
            KDSUIDlgInputSortedMode dlg = (KDSUIDlgInputSortedMode) dialog;
            KDSSortModeItem s = (KDSSortModeItem) dlg.getResult();
            boolean bSame = findSortItem(m_arDataSorted, s);
            if (bSame) return;
            int nExisted = findSortItemBySortMethod(m_arDataSorted, s.getSortMethod());

            if (nExisted < 0) {
                m_arDataSorted.add(s);
            } else {

                m_arDataSorted.remove(nExisted);
                m_arDataSorted.add(nExisted, s);
            }
            ((ArrayAdapter) m_lstSorted.getAdapter()).notifyDataSetChanged();
            save();
        }
    }

    boolean findItem(ArrayList<String> lst, String description)
    {
        int ncount = lst.size();
        for (int i=0; i< ncount; i++)
        {
            if (lst.get(i).equals(description))
                return true;
        }
        return false;
    }

    boolean findSortItem(ArrayList<KDSSortModeItem> lst, KDSSortModeItem item)
    {
        int ncount = lst.size();
        for (int i=0; i< ncount; i++)
        {
            if (lst.get(i).isSame(item))
                return true;
        }
        return false;
    }

    int findSortItemBySortMethod(ArrayList<KDSSortModeItem> lst, KDSSettings.OrdersSort sortMethod)
    {
        int ncount = lst.size();
        for (int i=0; i< ncount; i++)
        {
            if (lst.get(i).getSortMethod() == sortMethod)
                return i;
        }
        return -1;
    }

    int findDestination(ArrayList<String> lst, String dest)
    {
        int ncount = lst.size();
        for (int i=0; i< ncount; i++)
        {
            String s = lst.get(i);
            ArrayList<String> ar = TabDisplay.parseDestinationSaveString(s);
            if (ar.get(0).equals(dest))
                return i;

        }
        return -1;
    }

    static public final String TABDISP_KEY_ENABLE = "tabdisp_enabled";
    static public final String TABDISP_KEY_BUTTONS = "tabdisp_buttons";

    static public final String TABDISP_SEPARATOR = "\n";
    static public final String TABDISP_KEY_DEST = "tabdisp_dest";
    static public final String TABDISP_KEY_BG = "tabdisp_bg";
    static public final String TABDISP_KEY_SORT = "tabdisp_sort";

    public boolean load()
    {
        SharedPreferences pref = this.getPreferenceManager().getSharedPreferences();
        m_chkEnableTab.setChecked(pref.getBoolean(TABDISP_KEY_ENABLE, false));
        String s = pref.getString(TABDISP_KEY_BUTTONS, "");

        ArrayList<TabDisplay.TabButtonData> ar = parseTabButtons(s);
        m_arDataTab.clear();
        if (ar.size() !=0)
            m_arDataTab.addAll(ar);
        resetButtonDescription();


        s = pref.getString(TABDISP_KEY_DEST, "");
        ArrayList<String> arDest = parseDest(s);
        m_arDataDest.clear();
        m_arDataDest.addAll(arDest);

        s = pref.getString(TABDISP_KEY_BG, "-1,-16777216");
        m_valueBGFG = KDSBGFG.parseString(s);

        m_btnBG.setTextColor(m_valueBGFG.getFG());
        m_btnBG.setBackgroundColor(m_valueBGFG.getBG());

        s = pref.getString(TABDISP_KEY_SORT, "");
        ArrayList<KDSSortModeItem> arSorts = parseSortModes(s);
        m_arDataSorted.clear();
        m_arDataSorted.addAll(arSorts);

        return true;
    }
    public boolean save()
    {
        SharedPreferences pref = this.getPreferenceManager().getSharedPreferences();
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(TABDISP_KEY_ENABLE, m_chkEnableTab.isChecked());

        String tabButtons = getTabButtonsString();
        editor.putString(TABDISP_KEY_BUTTONS, tabButtons);

        String dests = getDestinationsString();
        editor.putString(TABDISP_KEY_DEST, dests);

        editor.putString(TABDISP_KEY_BG, m_valueBGFG.toString());

        String sorts = getSortModesString();
        editor.putString(TABDISP_KEY_SORT, sorts);

        KDSGlobalVariables.getKDS().getSettings().set(KDSSettings.ID.Tab_Enabled, m_chkEnableTab.isChecked());
        KDSGlobalVariables.getKDS().getSettings().set(KDSSettings.ID.Tab_buttons, tabButtons);
        KDSGlobalVariables.getKDS().getSettings().set(KDSSettings.ID.Tab_destinations, dests);
        KDSGlobalVariables.getKDS().getSettings().set(KDSSettings.ID.Tab_sort_modes, sorts);

        editor.commit();


        if (KDSGlobalVariables.getMainActivity() != null)
        {
            KDSGlobalVariables.getMainActivity().onSharedPreferenceChanged(null, TABDISP_KEY_BUTTONS);
        }
        return true;
    }


    public static ArrayList<TabDisplay.TabButtonData> parseTabButtons(String s)
    {

        return TabDisplay.parseTabButtons(s);

    }

    private void saveButtonChecked()
    {

    }
    public String getTabButtonsString()
    {
        saveButtonChecked();
        return TabDisplay.getTabButtonsString(m_arDataTab);

    }

    public String getSortModesString()
    {
        return KDSSortModeItem.getSaveString(m_arDataSorted);
    }

    public ArrayList<KDSSortModeItem> parseSortModes(String s)
    {

        return KDSSortModeItem.parseSortModeSaveString(s);

    }

    public String getDestinationsString()
    {

        return TabDisplay.getDestinationsString(m_arDataDest);
    }

    public ArrayList<String> parseDest(String s)
    {

        return TabDisplay.parseDestination(s);

    }

    public void resetButtonDescription()
    {
        TabDisplay.resetButtonDescription(m_arDataTab, true);


    }


    public void onDisplayText()
    {
        TabDisplay.TabButtonData data =((MyAdapter) m_lstTab.getAdapter()).getSelected();//.getSelectedItemPosition();//.getCheckedItemPosition();
        if (data == null) return;

        KDSUIDlgInputTabDisplayText dlg = new KDSUIDlgInputTabDisplayText(this.getActivity(),data.getDisplayText(), this );
        dlg.setButtonData(data);
        dlg.show();
    }

    private class MyAdapter extends ArrayAdapter<TabDisplay.TabButtonData> {


        TabDisplay.TabButtonData m_selected = null;
        private LayoutInflater mInflater;
        public List<TabDisplay.TabButtonData> m_listData; //KDSStationsRelation class array

        public MyAdapter(Context context, List<TabDisplay.TabButtonData> data) {
            super(context, R.layout.listitem_tab_display_mode,R.id.txtMode, data);
            this.mInflater = LayoutInflater.from(context);
            m_listData = data;
        }

        public  void setSelected(TabDisplay.TabButtonData data)
        {
            m_selected = data;
        }
        public TabDisplay.TabButtonData getSelected()
        {
            return m_selected;
        }

        public List<TabDisplay.TabButtonData> getListData()
        {
            return m_listData;
        }
        public void setListData(List<TabDisplay.TabButtonData> lst)
        {
            m_listData = lst;
        }
        public int getCount() {

            return m_listData.size();
        }
        public TabDisplay.TabButtonData getItem(int arg0) {

            return m_listData.get(arg0);
        }
        public long getItemId(int arg0) {

            return arg0;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            //ViewHolder holder = null;
            //View v = super.getView(position, convertView, parent);
            View v = convertView;
            if ( v == null) {

                v = mInflater.inflate(R.layout.listitem_tab_display_mode, null);


            }
            TabDisplay.TabButtonData r =  m_listData.get(position);

            v.setTag(r);


            ((TextView) v.findViewById(R.id.txtMode)).setText(r.toString());
            CheckBox chk =  ((CheckBox) v.findViewById(R.id.chkChecked));
            chk.setChecked(r.getEnabled());
            chk.setTag(r);



            Drawable drawable = ContextCompat.getDrawable( KDSApplication.getContext(),R.drawable.tab_checkbox_selector);
            drawable.setBounds(0,0,16,16);
            chk.setCompoundDrawables(drawable,null,null,null);

            ((CheckedTextView) v.findViewById(R.id.txtMode)).setChecked((r == m_selected));
            ((CheckedTextView) v.findViewById(R.id.txtMode)).setTag(r);
            ((CheckedTextView) v.findViewById(R.id.txtMode)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    setSelected((TabDisplay.TabButtonData) v.getTag());
                    notifyDataSetChanged();
                    onTabItemClicked();

                }
            });



            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_selected = (TabDisplay.TabButtonData) v.getTag();
                    notifyDataSetChanged();
                }
            });

           ((CheckBox) v.findViewById(R.id.chkChecked)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object obj = v.getTag();
                    if (obj == null) return;
                    ((TabDisplay.TabButtonData)obj).setEnabled(((CheckBox)v).isChecked());
                    save();
                }
            });

            return v;
        }

    }




}
