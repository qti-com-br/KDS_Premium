package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.MySpinnerArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Administrator on 2017/12/18.
 */
public class KDSUIDlgInputSortedMode  extends KDSUIDialogBase {


    Spinner m_spinnerSortMode = null;
    TextView m_txtDisplayText = null;

    //String m_strDisplayText = "";
    KDSSortModeItem m_sortItem = null;

    boolean m_bNew = false;



    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return  m_sortItem;

    }
    public void onOkClicked()
    {

        m_sortItem.setSortMethod( KDSSettings.OrdersSort.values()[ m_spinnerSortMode.getSelectedItemPosition() ]);
        m_sortItem.setDisplayText(m_txtDisplayText.getText().toString());
    }

    public boolean isNew()
    {
        return m_bNew;
    }


    public KDSUIDlgInputSortedMode(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener, KDSSortModeItem item) {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_input_sorted_mode, "");
        this.setTitle("Input sort mode");

        m_txtDisplayText = (TextView)this.getView().findViewById(R.id.txtDisplayText);
        m_spinnerSortMode = (Spinner) this.getView().findViewById(R.id.spinnerSortedMethod);

        MySpinnerArrayAdapter adapter = new MySpinnerArrayAdapter(context, getArray(context));
        m_spinnerSortMode.setAdapter(adapter);
        if(item != null)
            m_sortItem = item.clone();
        else
        {
            m_bNew = true;
            m_sortItem = new KDSSortModeItem();
        }

        m_spinnerSortMode.setSelection(m_sortItem.getSortMethod().ordinal());
        m_txtDisplayText.setText(m_sortItem.getDisplayText());


    }

    ArrayList<String> getArray(Context context)
    {
        String[] sorts= context.getResources().getStringArray(R.array.pref_kds_orders_sort_method_titles);
        ArrayList<String> ar = new ArrayList<>();

        for (int i=0; i< sorts.length; i++)
            ar.add(sorts[i]);

        return ar;
    }

//    protected String makeButtonText2(Context context, int nResID, KDSSettings.ID funcKey )
//    {
//        return makeButtonText(context, nResID, funcKey);
//
//    }
//
//    protected void init_dialog_events(final AlertDialog dlg)
//    {
//        init_dialog_ctrl_enter_events(dlg);
//
//    }

}
