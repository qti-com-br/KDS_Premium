package com.bematechus.kds;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/4/24.
 */
public class KDSUIDialogTrackerBump extends  KDSUIDialogBase {


    public enum TrackerBumpMethod {

       // Clear,
        Remove,
        Page,
        TrackerID,
    }


    ListView m_lstData = null;

    TrackerBumpMethod m_selectedMethod = TrackerBumpMethod.Remove;
    String m_strOrderGuid = "";

    public void setOrderGuid(String orderGuid)
    {
        m_strOrderGuid = orderGuid;
    }

    public String getOrderGuid()
    {
        return m_strOrderGuid;
    }
    @Override
    public void onOkClicked() {//save data here
        int ncount = m_lstData.getCount();
        for (int i = 0; i < ncount; i++) {
            if (m_lstData.isItemChecked(i)) {
                m_selectedMethod = TrackerBumpMethod.values()[i ]; //there is one "manually" in it
                return;
            }
        }
    }

    /**
     * it will been overrided by child
     *
     * @return
     */
    @Override
    public Object getResult() {
        return m_selectedMethod;


    }

    public KDSUIDialogTrackerBump(final Context context, KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.kdsui_tracker_bump, "");
        m_lstData = (ListView) this.getView().findViewById(R.id.lstData);
        m_lstData.setAdapter(new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_single_choice, getArray()));
        m_lstData.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        int n = 0;
        m_lstData.setItemChecked(n, true);
    }

    ArrayList<String> getArray() {
        ArrayList<String> ar = new ArrayList<>();
        //No manually.
        //ar.add(this.getView().getContext().getString(R.string.tt_bump_clear));

        ar.add(this.getView().getContext().getString(R.string.tt_bump_current));
        ar.add(this.getView().getContext().getString(R.string.tt_bump_page));
        ar.add(this.getView().getContext().getString(R.string.tt_tracker_id));
        return ar;
    }
}