package com.bematechus.kds;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSPreferenceFragment;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

/**
 *
 */
public class KDSUIDialogSumStationFilter extends KDSUIDialogBase implements  KDSUIDialogBase.KDSDialogBaseListener{

    private static final String TAG = "AdvSumPref";


    //////////////////////
    ListView m_lstData = null;
    ArrayList<SumStationFilterEntry>  m_arData =  new ArrayList<>();

    String mFilter = "";

    @Override
    public Object getResult() {
        return getSumItemsString();
    }

    //    boolean m_bSuspendChangedEvent  =false;
    public KDSUIDialogSumStationFilter(final Context context, String strFilter, KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.sumstn_filter, "");
        mFilter = strFilter;
       init_variables(this.getView());
    }

    protected void init_variables(View view) {


        m_lstData = (ListView)view.findViewById(R.id.lstData);

        ArrayAdapter<SumStationFilterEntry> adapter = new ArrayAdapter<>(KDSApplication.getContext(),R.layout.array_sum_edit_adapter, m_arData);

        m_lstData.setAdapter(adapter);
        m_lstData.setEnabled(true);

        Button btnNew =  (Button)view.findViewById(R.id.btnAdd);
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNewDescription(v);
            }
        });

        Button btnDel =  (Button)view.findViewById(R.id.btnRemove);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDel(v);
            }
        });

        load(); //prevent it fire events
        //buildList();

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

//        m_bSuspendChangedEvent = false;

    }


    public void moveUp()
    {
        int nindex = m_lstData.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>m_arData.size()-1) return;
        if (nindex ==0) return;
        SumStationFilterEntry s= m_arData.get(nindex);
        m_arData.remove(nindex);
        m_arData.add(nindex-1, s);

        ((ArrayAdapter)m_lstData.getAdapter()).notifyDataSetChanged();
        m_lstData.setItemChecked(nindex-1, true);
        //save();
    }
    public void moveDown()
    {
        int nindex = m_lstData.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>=m_arData.size()-1) return;


        SumStationFilterEntry s= m_arData.get(nindex);
        m_arData.remove(nindex);
        m_arData.add(nindex+1, s);
        ((ArrayAdapter)m_lstData.getAdapter()).notifyDataSetChanged();
        m_lstData.setItemChecked(nindex+1, true);
        //save();
    }
    public void onDel(View v)
    {

        int nindex = m_lstData.getCheckedItemPosition();

        if (nindex <0) return;
        if (nindex>m_arData.size()-1) return;

        m_arData.remove(nindex);
        //save();
        ((ArrayAdapter)m_lstData.getAdapter()).notifyDataSetChanged();

    }

    public void onNewDescription(View v)
    {

        KDSUIDlgInputSumStationFilterEntry dlg = new KDSUIDlgInputSumStationFilterEntry(this.getView().getContext(), this, null);
        dlg.show();

    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }


    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {

        if (!(dialog instanceof KDSUIDlgInputSumStationFilterEntry))
            return;

        KDSUIDlgInputSumStationFilterEntry dlg = (KDSUIDlgInputSumStationFilterEntry)dialog;
        SumStationFilterEntry s = (SumStationFilterEntry) dlg.getResult();
        if (findItem(m_arData, s)) return;
        //if (dlg.getEntryType() == SumStationEntry.EntryType.Condiment)
        //    s.getDescription() += KDSSummaryItem.CONDIMENT_TAG;
        m_arData.add(s);
        ((ArrayAdapter)m_lstData.getAdapter()).notifyDataSetChanged();
        //save();
    }

    boolean findItem(ArrayList<SumStationFilterEntry> lst, SumStationEntry entry)
    {
        int ncount = lst.size();
        for (int i=0; i< ncount; i++)
        {
            if (lst.get(i).getDescription().equals(entry.getDescription()) )
          //          lst.get(i).getDescription().equals(entry.getDescription() + KDSSummaryItem.CONDIMENT_TAG)    )
                return true;
        }
        return false;
    }

//    static public final String SUM_STATION_KEY_ITEMS = "sumstn_items";
//    static public final String SUM_STATION_SEPARATOR = "\n";
//    //static public final String ADVSUM_KEY_ENABLE = "advsum_enabled";
//    //static public final String ADVSUM_KEY_SUM_ALWAYS = "advsum_always_visible";
//
//    //static public final String ADVSUM_KEY_ROWS = "advsum_rows";
//    static public final String SUM_STATION_KEY_COLS = "sumstn_cols";

//    /**
//     * 2.0.24
//     * @param pref
//     * @param strKey
//     * @param nDefault
//     * @return
//     */
//    private int getPrefInt(SharedPreferences pref, String strKey, int nDefault)
//    {
//        String strDefault = KDSUtil.convertIntToString(nDefault);
//
//        String str = "";
//        int nReturn = 0;
//        try {
//            nReturn = pref.getInt(strKey, nDefault);
//            return nReturn;
//        }
//        catch (Exception e)
//        {
//            try {
//                str = pref.getString(strKey, strDefault);
//                return KDSUtil.convertStringToInt(str, nDefault);
//
//            }
//            catch (Exception err)
//            {
//                return 0;
//            }
//        }
//    }

    public boolean load()
    {
        //SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());


        String s = mFilter;//pref.getString(SUM_STATION_KEY_ITEMS, "");

        ArrayList<SumStationFilterEntry> ar = parseSumItems(s);
        m_arData.clear();
        m_arData.addAll(ar);
        return true;
    }
//    public boolean load()
//    {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
//
//
//        String s = pref.getString(SUM_STATION_KEY_ITEMS, "");
//
//        ArrayList<SumStationFilterEntry> ar = parseSumItems(s);
//        m_arData.clear();
//        m_arData.addAll(ar);
//        return true;
//    }
//    public boolean isChanged()
//    {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
//
//        String s = pref.getString(SUM_STATION_KEY_ITEMS, "");
//        if (!getSumItemsString().equals(s))
//            return true;
//        return false;
//    }
//    public boolean save()
//    {
//        if (!isChanged()) return true;
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
//        SharedPreferences.Editor editor = pref.edit();
//
//        //editor.putBoolean(ADVSUM_KEY_ENABLE, m_chkEnableAdvSum.isChecked());
//        //editor.putBoolean(ADVSUM_KEY_SUM_ALWAYS, m_chkEnableSumAlwaysVisible.isChecked());
//
//        //editor.putString(ADVSUM_KEY_ROWS,KDSUtil.convertIntToString(  m_spinnerRows.getSelectedItemPosition()+1));
//        //editor.putString(SUM_STATION_KEY_COLS,KDSUtil.convertIntToString( m_spinnerCols.getSelectedItemPosition()+1));
//
//        String items = getSumItemsString();
//        editor.putString(SUM_STATION_KEY_ITEMS, items);
//
//        //KDSGlobalVariables.getKDS().getSettings().set(KDSSettings.ID.AdvSum_enabled, m_chkEnableAdvSum.isChecked());
//        //KDSGlobalVariables.getKDS().getSettings().set(KDSSettings.ID.AdvSum_always_visible, m_chkEnableSumAlwaysVisible.isChecked());
//        KDSGlobalVariables.getKDS().getSettings().set(KDSSettings.ID.SumStn_entries, items);
//
//        editor.commit();
//
//
//        if (KDSGlobalVariables.getMainActivity() != null)
//        {
//            KDSGlobalVariables.getMainActivity().onSharedPreferenceChanged(null, SUM_STATION_KEY_ITEMS);
//        }
//        return true;
//    }

    /**
     * format:
     *  item\nitem\n
     * @param s
     * @return
     */
    public static ArrayList<SumStationFilterEntry> parseSumItems(String s)
    {
        ArrayList<SumStationFilterEntry> entries = new ArrayList<>();

        ArrayList<String> ar = KDSUtil.spliteString(s, SumStationEntry.SUM_STATION_SEPARATOR);
        for (int i=0; i< ar.size(); i++)
        {
            if (ar.get(i).isEmpty()) continue;
            String str = ar.get(i);
            SumStationFilterEntry entry = SumStationFilterEntry.parsePrefString(str);
            if (entry != null)
                entries.add(entry);
        }
        return entries;

    }

    public String getSumItemsString()
    {
        String s = "";
        for (int i=0; i< m_arData.size(); i++)
        {
            if (!s.isEmpty())
            {
                s += SumStationFilterEntry.SUM_STATION_SEPARATOR;
            }
            s += m_arData.get(i).toPrefString();

        }
        return s;


    }

}


