package com.bematechus.kds;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class KDSUIDialogBrowseInDB extends KDSUIDialogBase{


    ListView m_lstData = null;

    String mSelectedDescription = "";
    SumStationEntry.EntryType m_entryType = SumStationEntry.EntryType.Item;

    public SumStationEntry.EntryType getEntryType()
    {
        return m_entryType;
    }

    @Override
    public void onOkClicked()
    {//save data here
        int ncount = m_lstData.getCount();
        for (int i=0; i< ncount; i++) {
            if ( m_lstData.isItemChecked(i))
            {
                //mSelectedDescription = m_lstData. KDSUIDialogMore.FunctionMore.values()[i]; //there is one "manually" in it
                mSelectedDescription = (String)m_lstData.getAdapter().getItem(i);
                return;
            }
        }
    }

    /**
     * it will been overrided by child
     * @return
     */
    @Override
    public Object getResult()
    {
        return mSelectedDescription;


    }

    public KDSUIDialogBrowseInDB(final Context context, SumStationEntry.EntryType mode,String strFilter, KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.kdsgui_dlg_browse_entries_in_db, "");

        m_entryType = mode;

        m_lstData =(ListView) this.getView().findViewById(R.id.lstData);
        m_lstData.setAdapter(new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_single_choice, getArray(mode, strFilter)));
        m_lstData.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (m_lstData.getCount() >0)
            m_lstData.setItemChecked(0, true);
    }

    /**
     * The array sequence should same as the FunctionMore.
     * @return
     */
    ArrayList<String> getArray(SumStationEntry.EntryType mode, String strFilter)
    {
        ArrayList<String> ar = new ArrayList<>();


        String s = strFilter;// m_txtText.getText().toString();
        ArrayList<String> arCurrent = null;
        ArrayList<String> arStatistic = null;
        if (mode == SumStationEntry.EntryType.Item) {
            arCurrent = KDSGlobalVariables.getKDS().getCurrentDB().getUniqueItems(s);
            if (KDSConst.ENABLE_FEATURE_STATISTIC)
                arStatistic = KDSGlobalVariables.getKDS().getStatisticDB().getUniqueItems(s);
        }
        else if (mode == SumStationEntry.EntryType.Condiment)
        {
            arCurrent = KDSGlobalVariables.getKDS().getCurrentDB().getUniqueCondiments(s);
            if (KDSConst.ENABLE_FEATURE_STATISTIC)
                arStatistic = KDSGlobalVariables.getKDS().getStatisticDB().getUniqueCondiments(s);
        }
        if (KDSConst.ENABLE_FEATURE_STATISTIC) {
            for (int i = 0; i < arStatistic.size(); i++) {
                if (KDSUtil.isExistedInArray(arCurrent, arStatistic.get(i)))
                    continue;
                else
                    arCurrent.add(arStatistic.get(i));

            }
        }
        ar.clear();
        ar.addAll(arCurrent);

        Collections.sort(ar, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        //((ArrayAdapter)(m_lstData.getAdapter())).notifyDataSetChanged();

        if (ar.size() <=0)
            showNoDataWarning();

        return ar;
    }

    private void showNoDataWarning()
    {
        String s = this.getDialog().getContext().getString(R.string.no_data_in_db);

        Toast.makeText(this.getDialog().getContext(), s, Toast.LENGTH_SHORT).show();

    }

}
