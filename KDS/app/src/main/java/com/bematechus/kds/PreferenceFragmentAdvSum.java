package com.bematechus.kds;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSPreferenceFragment;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.SettingsBase;
import com.bematechus.kdslib.ThemeUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/9/21.
 */
public class PreferenceFragmentAdvSum extends KDSPreferenceFragment implements  KDSUIDialogBase.KDSDialogBaseListener{

    private static final String TAG = "AdvSumPref";

    //////////////////////
    GridView m_lstData = null;
    CheckBox m_chkEnableAdvSum = null;
    CheckBox m_chkEnableSumAlwaysVisible = null;

    Spinner m_spinnerRows = null;
    Spinner m_spinnerCols = null;

    ArrayList<String>  m_arData =  new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =  inflater.inflate(R.layout.prefui_advanced_sum_filter, container, false);
        view.setBackgroundColor(ThemeUtil.getAttrColor(KDSApplication.getContext(), R.attr.settings_page_bg));// this.getResources().getColor(R.color.settings_page_bg));
        init_variables(view);

        return view;
    }

    boolean m_bSuspendChangedEvent  =false;

    protected void init_variables(View view) {


        m_lstData = (GridView)view.findViewById(R.id.lstData);
        m_chkEnableAdvSum = (CheckBox)view.findViewById(R.id.chkEnableAdvSum);
        m_chkEnableSumAlwaysVisible = (CheckBox)view.findViewById(R.id.chkEnableSumAlwaysVisible);

        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(KDSApplication.getContext(),android.R.layout.simple_list_item_single_choice, m_arData);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(KDSApplication.getContext(),R.layout.array_sum_edit_adapter, m_arData);

        m_lstData.setAdapter(adapter);
        m_lstData.setEnabled(true);

        m_spinnerCols = (Spinner) view.findViewById(R.id.spinnerCols);
        m_spinnerRows = (Spinner) view.findViewById(R.id.spinnerRows);

        m_bSuspendChangedEvent = true;
        Button btnNew =  (Button)view.findViewById(R.id.btnAdd);
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceFragmentAdvSum.this.onNewDescription(v);
            }
        });

        Button btnAddCondiment =(Button)view.findViewById(R.id.btnAddCondiment);
        btnAddCondiment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceFragmentAdvSum.this.onNewCondimentDescription(v);
            }
        });

        Button btnDel =  (Button)view.findViewById(R.id.btnRemove);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceFragmentAdvSum.this.onDel(v);
            }
        });




        load(); //prevent it fire events
        buildList();
        m_chkEnableAdvSum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        m_chkEnableSumAlwaysVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });


        m_spinnerRows.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (m_bSuspendChangedEvent) return;
                save();
                buildList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        m_spinnerCols.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (m_bSuspendChangedEvent) return;
                save();
                buildList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        Button btn = (Button)view.findViewById(R.id.btnUp);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveUp();
            }
        });

        btn = (Button)view.findViewById(R.id.btnDown);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveDown();
            }
        });

        //load();
        //buildList();
        adapter.notifyDataSetChanged();

        m_bSuspendChangedEvent = false;

    }
    final int SUM_ROW_HEIGHT = 30;
    public  void buildList()
    {
        m_lstData.setNumColumns(m_spinnerCols.getSelectedItemPosition() + 1);
        int nrows = m_spinnerRows.getSelectedItemPosition() + 1;
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) m_lstData.getLayoutParams(); //取控件textView当前的布局参数
        linearParams.height = SUM_ROW_HEIGHT * nrows;// 控件的高强制设成20
        m_lstData.setLayoutParams(linearParams);
        m_lstData.invalidate();
    }
    public void onDestroy() {
        super.onDestroy();

    }

    public void moveUp()
    {
        int nindex = m_lstData.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>m_arData.size()-1) return;
        if (nindex ==0) return;
        String s= m_arData.get(nindex);
        m_arData.remove(nindex);
        m_arData.add(nindex-1, s);

        ((ArrayAdapter)m_lstData.getAdapter()).notifyDataSetChanged();
        m_lstData.setItemChecked(nindex-1, true);
        save();
    }
    public void moveDown()
    {
        int nindex = m_lstData.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>=m_arData.size()-1) return;


        String s= m_arData.get(nindex);
        m_arData.remove(nindex);
        m_arData.add(nindex+1, s);
        ((ArrayAdapter)m_lstData.getAdapter()).notifyDataSetChanged();
        m_lstData.setItemChecked(nindex+1, true);
        save();
    }
    public void onDel(View v)
    {

        int nindex = m_lstData.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>m_arData.size()-1) return;

        m_arData.remove(nindex);
        save();
        ((ArrayAdapter)m_lstData.getAdapter()).notifyDataSetChanged();

    }

    public void onNewDescription(View v)
    {
        if (this.getActivity() == null) return;
        KDSUIDlgInputItemDescription dlg = new KDSUIDlgInputItemDescription(this.getActivity(), this, KDSUIDlgInputItemDescription.Mode.Item);
        dlg.show();

    }
    public void onNewCondimentDescription(View v)
    {
        if (this.getActivity() == null) return;
        KDSUIDlgInputItemDescription dlg = new KDSUIDlgInputItemDescription(this.getActivity(), this, KDSUIDlgInputItemDescription.Mode.Condiment);
        dlg.show();

    }


    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }


    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {

        if (!(dialog instanceof KDSUIDlgInputItemDescription))
            return;

        KDSUIDlgInputItemDescription dlg = (KDSUIDlgInputItemDescription)dialog;
        String s = (String) dlg.getResult();
        if (findItem(m_arData, s)) return;
        if (dlg.getMode() == KDSUIDlgInputItemDescription.Mode.Condiment)
            s += KDSSummaryItem.CONDIMENT_TAG;
        m_arData.add(s);
        ((ArrayAdapter)m_lstData.getAdapter()).notifyDataSetChanged();
        save();
    }

    boolean findItem(ArrayList<String> lst, String description)
    {
        int ncount = lst.size();
        for (int i=0; i< ncount; i++)
        {
            if (lst.get(i).equals(description) ||
                    lst.get(i).equals(description + KDSSummaryItem.CONDIMENT_TAG)    )
                return true;
        }
        return false;
    }

    static public final String ADVSUM_KEY_ITEMS = "advsum_items";
    static public final String ADVSUM_SEPARATOR = "\n";
    static public final String ADVSUM_KEY_ENABLE = "advsum_enabled";
    static public final String ADVSUM_KEY_SUM_ALWAYS = "advsum_always_visible";

    static public final String ADVSUM_KEY_ROWS = "advsum_rows";
    static public final String ADVSUM_KEY_COLS = "advsum_cols";

    /**
     * 2.0.24
     * @param pref
     * @param strKey
     * @param nDefault
     * @return
     */
    private int getPrefInt(SharedPreferences pref, String strKey, int nDefault)
    {
        String strDefault = KDSUtil.convertIntToString(nDefault);

        String str = "";
        int nReturn = 0;
        try {
             nReturn = pref.getInt(strKey, nDefault);
            return nReturn;
        }
        catch (Exception e)
        {
            try {
                str = pref.getString(strKey, strDefault);
                return KDSUtil.convertStringToInt(str, nDefault);

            }
            catch (Exception err)
            {
                return 0;
            }
        }
    }

    public boolean load()
    {
        SharedPreferences pref = this.getPreferenceManager().getSharedPreferences();


        m_chkEnableAdvSum.setChecked(pref.getBoolean(ADVSUM_KEY_ENABLE, false));
        m_chkEnableSumAlwaysVisible.setChecked(pref.getBoolean(ADVSUM_KEY_SUM_ALWAYS, false));

        //String str = pref.getString(ADVSUM_KEY_ROWS, "4");

        //int n = KDSUtil.convertStringToInt(str, 4);// pref.getInt(ADVSUM_KEY_ROWS, 4);
        int n = getPrefInt(pref, ADVSUM_KEY_ROWS, 4);

        m_spinnerRows.setSelection(n-1);

        //str = pref.getString(ADVSUM_KEY_COLS, "4");
        //n = KDSUtil.convertStringToInt(str, 4);// pref.getInt(ADVSUM_KEY_COLS, 4);
        n = getPrefInt(pref,ADVSUM_KEY_COLS, 4 );
        m_spinnerCols.setSelection(n-1);

        String s = pref.getString(ADVSUM_KEY_ITEMS, "");

        ArrayList<String> ar = parseSumItems(s);
        m_arData.clear();
        m_arData.addAll(ar);
        return true;
    }
    public boolean isChanged()
    {
        SharedPreferences pref = this.getPreferenceManager().getSharedPreferences();


        if (m_chkEnableAdvSum.isChecked() != (pref.getBoolean(ADVSUM_KEY_ENABLE, false)) )
            return true;
        if (m_chkEnableSumAlwaysVisible.isChecked() != (pref.getBoolean(ADVSUM_KEY_SUM_ALWAYS, false)))
            return true;

        //String str = pref.getString(ADVSUM_KEY_ROWS, "4");

        //int n = KDSUtil.convertStringToInt(str, 4);// pref.getInt(ADVSUM_KEY_ROWS, 4);
        //int n = pref.getInt(ADVSUM_KEY_ROWS, 4);
        int n = getPrefInt(pref,ADVSUM_KEY_ROWS, 4 );
        if (m_spinnerRows.getSelectedItemPosition() != (n-1))
            return true;

        //n = pref.getInt(ADVSUM_KEY_COLS, 4);
        //str = pref.getString(ADVSUM_KEY_COLS, "4");
        //n = KDSUtil.convertStringToInt(str, 4);// pref.getInt(ADVSUM_KEY_COLS, 4);
        n = getPrefInt(pref,ADVSUM_KEY_COLS, 4 );
        if (m_spinnerCols.getSelectedItemPosition() != (n-1))
            return true;

        String s = pref.getString(ADVSUM_KEY_ITEMS, "");
        if (!getSumItemsString().equals(s))
            return true;
        return false;
    }
    public boolean save()
    {
        if (!isChanged()) return true;
        SharedPreferences pref = this.getPreferenceManager().getSharedPreferences();
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(ADVSUM_KEY_ENABLE, m_chkEnableAdvSum.isChecked());
        editor.putBoolean(ADVSUM_KEY_SUM_ALWAYS, m_chkEnableSumAlwaysVisible.isChecked());

        editor.putString(ADVSUM_KEY_ROWS,KDSUtil.convertIntToString(  m_spinnerRows.getSelectedItemPosition()+1));
        editor.putString(ADVSUM_KEY_COLS,KDSUtil.convertIntToString( m_spinnerCols.getSelectedItemPosition()+1));

        String items = getSumItemsString();
        editor.putString(ADVSUM_KEY_ITEMS, items);

        KDSGlobalVariables.getKDS().getSettings().set(KDSSettings.ID.AdvSum_enabled, m_chkEnableAdvSum.isChecked());
        KDSGlobalVariables.getKDS().getSettings().set(KDSSettings.ID.AdvSum_always_visible, m_chkEnableSumAlwaysVisible.isChecked());
        KDSGlobalVariables.getKDS().getSettings().set(KDSSettings.ID.AdvSum_items, items);

        editor.commit();


        if (KDSGlobalVariables.getMainActivity() != null)
        {
            KDSGlobalVariables.getMainActivity().onSharedPreferenceChanged(null, ADVSUM_KEY_ITEMS);
        }
        return true;
    }

    /**
     * format:
     *  item\nitem\n
     * @param s
     * @return
     */
    public static ArrayList<String> parseSumItems(String s)
    {
        ArrayList<String> ar = KDSUtil.spliteString(s, ADVSUM_SEPARATOR);
        for (int i=ar.size()-1; i>=0; i--)
        {
            if (ar.get(i).isEmpty())
                ar.remove(i);
        }
        return ar;

    }

    public String getSumItemsString()
    {
        String s = "";
        for (int i=0; i< m_arData.size(); i++)
        {
            if (!s.isEmpty())
            {
                s += ADVSUM_SEPARATOR;
            }
            s += m_arData.get(i);

        }
        return s;


    }

}

