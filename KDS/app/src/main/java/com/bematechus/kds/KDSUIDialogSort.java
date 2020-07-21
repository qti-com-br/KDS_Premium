package com.bematechus.kds;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bematechus.kdslib.KDSUIDialogBase;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/12 0012.
 */
public class KDSUIDialogSort extends KDSUIDialogBase {


    ListView m_lstData = null;
   // ArrayList<String> m_lstSortOptions = new ArrayList<>();
    KDSSettings.OrdersSort m_selectedSort = KDSSettings.OrdersSort.Waiting_Time_Ascend;


    @Override
    public void onOkClicked()
    {//save data here
        int ncount = m_lstData.getCount();
        for (int i=0; i< ncount; i++) {
            if ( m_lstData.isItemChecked(i))
            {
                m_selectedSort = KDSSettings.OrdersSort.values()[i+1]; //there is one "manually" in it
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
        return m_selectedSort;


    }

    public KDSUIDialogSort(final Context context, KDSSettings.OrdersSort initSort, KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.kdsui_sort_orders, "");
        m_lstData =(ListView) this.getView().findViewById(R.id.lstData);
        m_lstData.setAdapter(new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_single_choice, getArray()));
        m_lstData.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        int n = initSort.ordinal()-1;
        m_lstData.setItemChecked(n, true);
    }

    ArrayList<String> getArray()
    {
        ArrayList<String> ar = new ArrayList<>();
        //No manually.
        ar.add(this.getView().getContext().getString(R.string.sort_time_ascend));// "Waiting time ascend");
        ar.add(this.getView().getContext().getString(R.string.sort_time_descen));//"Waiting time decend");
        ar.add(this.getView().getContext().getString(R.string.sort_name_ascend));//"Order number ascend");
        ar.add(this.getView().getContext().getString(R.string.sort_name_descend));//"Order number decend");
        ar.add(this.getView().getContext().getString(R.string.sort_count_ascend));//"Items count ascend");
        ar.add(this.getView().getContext().getString(R.string.sort_count_descend));//"Items count decend");
        ar.add(this.getView().getContext().getString(R.string.sort_preptime_ascend));//"Preparation time ascend");
        ar.add(this.getView().getContext().getString(R.string.sort_preptime_descend));//"Preparation time decend");
        return ar;
    }
}
