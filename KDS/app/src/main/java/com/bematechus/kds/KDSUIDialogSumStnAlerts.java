package com.bematechus.kds;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

public class KDSUIDialogSumStnAlerts extends KDSUIDialogBase implements  KDSUIDialogBase.KDSDialogBaseListener {

    private static final String TAG = "AdvSumPref";

    //////////////////////
    ListView m_lstData = null;
    ArrayList<SumStationAlertEntry> m_arData = new ArrayList<>();

    @Override
    public Object getResult() {
        return this.getSumItemsString();
    }

    public KDSUIDialogSumStnAlerts(final Context context, String alerts, KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_sumstn_alert_items, "");

        init_variables(this.getView(), alerts);
    }

    protected void init_variables(View view, String alerts) {


        m_lstData = (ListView) view.findViewById(R.id.lstData);

        ArrayAdapter<SumStationAlertEntry> adapter = new ArrayAdapter<>(KDSApplication.getContext(), R.layout.array_sum_edit_adapter, m_arData);

        m_lstData.setAdapter(adapter);
        m_lstData.setEnabled(true);

        Button btnNew = (Button) view.findViewById(R.id.btnAdd);
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNewDescription(v);
            }
        });

        Button btnDel = (Button) view.findViewById(R.id.btnRemove);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDel(v);
            }
        });

        load(alerts); //prevent it fire events
        //buildList();

        Button btn = (Button) view.findViewById(R.id.btnEdit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEdit(v);
            }
        });

        adapter.notifyDataSetChanged();



    }
    public void onDel(View v) {

        int nindex = m_lstData.getCheckedItemPosition();

        if (nindex < 0) return;
        if (nindex > m_arData.size() - 1) return;

        m_arData.remove(nindex);
        //save();
        ((ArrayAdapter) m_lstData.getAdapter()).notifyDataSetChanged();

    }

    public void onNewDescription(View v) {

        KDSUIDlgInputSumStationAlertEntry dlg = new KDSUIDlgInputSumStationAlertEntry(this.getView().getContext(), this, null);
        dlg.show();

    }


    public void onEdit(View v) {

        int nindex = m_lstData.getCheckedItemPosition();

        if (nindex < 0) return;
        if (nindex > m_arData.size() - 1) return;
        SumStationAlertEntry alert = m_arData.get(nindex);

        KDSUIDlgInputSumStationAlertEntry dlg = new KDSUIDlgInputSumStationAlertEntry(this.getView().getContext(), this, alert);
        dlg.show();

    }

//    public void onNewCondimentDescription(View v)
//    {
//        if (this.getActivity() == null) return;
//        KDSUIDlgInputSumStationEntry dlg = new KDSUIDlgInputSumStationEntry(this.getActivity(), this, null);
//        dlg.show();
//
//    }


    public void onKDSDialogCancel(KDSUIDialogBase dialog) {

    }


    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj) {

        if (!(dialog instanceof KDSUIDlgInputSumStationAlertEntry))
            return;

        KDSUIDlgInputSumStationAlertEntry dlg = (KDSUIDlgInputSumStationAlertEntry) dialog;
        if (dlg.getOriginalEntry() == null) { //add new
            SumStationAlertEntry s = (SumStationAlertEntry) dlg.getResult();
            if (findItem(m_arData, s)) return;
            m_arData.add(s);

        }
        else
        {
            SumStationAlertEntry edit = (SumStationAlertEntry) dlg.getResult();
            SumStationAlertEntry original = (SumStationAlertEntry) dlg.getOriginalEntry();
            original.copy(edit);
        }
        ((ArrayAdapter) m_lstData.getAdapter()).notifyDataSetChanged();

    }

    boolean findItem(ArrayList<SumStationAlertEntry> lst, SumStationAlertEntry entry) {
        int ncount = lst.size();
        for (int i = 0; i < ncount; i++) {
            if (lst.get(i).getDescription().equals(entry.getDescription()))
                //          lst.get(i).getDescription().equals(entry.getDescription() + KDSSummaryItem.CONDIMENT_TAG)    )
                return true;
        }
        return false;
    }


    static public final String SUM_STATION_SEPARATOR = "\n";



    public boolean load(String alerts) {
        //SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());


        String s = alerts;//pref.getString(SUM_STATION_KEY_ITEMS, "");

        ArrayList<SumStationAlertEntry> ar = parseSumItems(s);
        m_arData.clear();
        m_arData.addAll(ar);
        return true;
    }

    /**
     * format:
     * item\nitem\n
     *
     * @param s
     * @return
     */
    public static ArrayList<SumStationAlertEntry> parseSumItems(String s) {
        ArrayList<SumStationAlertEntry> entries = new ArrayList<>();

        ArrayList<String> ar = KDSUtil.spliteString(s, SUM_STATION_SEPARATOR);
        for (int i = 0; i < ar.size(); i++) {
            if (ar.get(i).isEmpty()) continue;
            String str = ar.get(i);
            SumStationAlertEntry entry = SumStationAlertEntry.parsePrefString(str);
            if (entry != null)
                entries.add(entry);
        }
        return entries;

    }

    public String getSumItemsString() {
        String s = "";
        for (int i = 0; i < m_arData.size(); i++) {
            if (!s.isEmpty()) {
                s += SUM_STATION_SEPARATOR;
            }
            s += m_arData.get(i).toPrefString();

        }
        return s;


    }
}