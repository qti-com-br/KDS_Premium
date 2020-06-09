package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Administrator on 2017/9/21.
 */
public class KDSUIDlgInputItemDescription  extends KDSUIDialogBase {


    public enum Mode
    {
        Item,
        Condiment,
    }
    TextView m_txtText = null;
    ListView  m_lstData = null;
    String m_strDescriptioin = "";
    ArrayList<String> m_arData = new ArrayList<>();
    Mode m_mode = Mode.Item;
    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return m_strDescriptioin;
    }
    public void onOkClicked()
    {
        m_strDescriptioin = m_txtText.getText().toString();
    }

    public Mode getMode()
    {
        return m_mode;
    }

    
    public KDSUIDlgInputItemDescription(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener, Mode mode) {
        this.setUseCtrlEnterKey(true);
        this.int_dialog(context, listener, R.layout.kdsui_dlg_input_item_description, "");
        m_mode = mode;
        
        this.setTitle(context.getString(R.string.input_item_description));
        if (m_mode == Mode.Condiment)
            this.setTitle(context.getString(R.string.input_condiment_description));
        m_txtText = (TextView)this.getView().findViewById(R.id.txtText);
        m_lstData = (ListView)this.getView().findViewById(R.id.lstData);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(KDSApplication.getContext(),R.layout.array_adapter, m_arData);
        m_lstData.setAdapter(adapter);

        m_lstData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = m_arData.get(position);
                m_txtText.setText(s);
            }
        });


        Button btn =  (Button) this.getView().findViewById(R.id.btnFind);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFindClicked();
            }
        });

    }

    public void onFindClicked()
    {
        String s = m_txtText.getText().toString();
        ArrayList<String> arCurrent = null;
        ArrayList<String> arStatistic = null;
        if (m_mode == Mode.Item) {
            arCurrent = KDSGlobalVariables.getKDS().getCurrentDB().getUniqueItems(s);
            if (KDSConst.ENABLE_FEATURE_STATISTIC)
                arStatistic = KDSGlobalVariables.getKDS().getStatisticDB().getUniqueItems(s);
        }
        else if (m_mode == Mode.Condiment)
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
        m_arData.clear();
        m_arData.addAll(arCurrent);

        Collections.sort(m_arData, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        ((ArrayAdapter)(m_lstData.getAdapter())).notifyDataSetChanged();

        if (m_arData.size() <=0)
            showNoDataWarning();
    }

    private void showNoDataWarning()
    {
        String s = this.getDialog().getContext().getString(R.string.no_data_in_db);

        Toast.makeText(this.getDialog().getContext(), s, Toast.LENGTH_SHORT).show();

    }

//    public String makeOKButtonText(Context context)
//    {
//        return makeCtrlEnterButtonText(context, DialogEvent.OK);
//
//    }
//    public String makeCancelButtonText(Context context)
//    {
//        return makeCtrlEnterButtonText(context, DialogEvent.Cancel);
//
//    }
//    protected String makeButtonText2(Context context, int nResID, KDSSettings.ID funcKey )
//    {
//        return makeButtonText(context, nResID, funcKey);
//
//    }

//    protected void init_dialog_events(final AlertDialog dlg)
//    {
//        init_dialog_ctrl_enter_events(dlg);
//
//    }

}
