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
import android.widget.TextView;
import android.widget.Toast;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Administrator on 2017/10/10.
 */
public class KDSUIDlgInputDest  extends KDSUIDialogBase {


    TextView m_txtText = null;
    TextView m_txtDisplayText = null;
    ListView m_lstData = null;
    String m_strDestination = "";
    String m_strDisplayText = "";
    ArrayList<String> m_arData = new ArrayList<>();

    boolean m_bNew = false;
    boolean m_bSyncText = true;


    public static String createDestinationString(String destination, String displayText)
    {
        return TabDisplay.createDestinationSaveString(destination, displayText);


    }
    public static ArrayList<String> parseDestinationString(String destString)
    {
        return TabDisplay.parseDestinationSaveString(destString);

    }
    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return createDestinationString(m_strDestination, m_strDisplayText);
        
    }
    public void onOkClicked()
    {
        
        m_strDestination = m_txtText.getText().toString();
        m_strDisplayText = m_txtDisplayText.getText().toString();
    }

    public String getDescription()
    {
        return m_strDestination;
    }
    public String getDisplayText()
    {
        return m_strDisplayText;
    }


    public KDSUIDlgInputDest(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener, String strInit) {
        this.setUseCtrlEnterKey(true);
        this.int_dialog(context, listener, R.layout.kdsui_dlg_input_dest, "");
        this.setTitle(context.getString(R.string.input_destination));

        m_txtText = (TextView)this.getView().findViewById(R.id.txtText);
        m_bSyncText = false;
//        if (strInit.isEmpty())
//            m_bNew = true;
        m_txtText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (m_bSyncText )
                    m_txtDisplayText.setText(m_txtText.getText());

            }
        });

        m_txtDisplayText = (TextView)this.getView().findViewById(R.id.txtDisplayText);
        m_lstData = (ListView)this.getView().findViewById(R.id.lstData);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(KDSApplication.getContext(),R.layout.array_adapter, m_arData);
        m_lstData.setAdapter(adapter);

        m_lstData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = m_arData.get(position);
                m_txtText.setText(s);
                m_txtDisplayText.setText(s);
            }
        });


        Button btn =  (Button) this.getView().findViewById(R.id.btnFind);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFindClicked();
            }
        });
        if (!strInit.isEmpty()) {
            ArrayList<String> ar = TabDisplay.parseDestinationSaveString(strInit);
            m_txtText.setText(ar.get(0));
            m_txtDisplayText.setText(ar.get(1));
        }

        m_bSyncText = (m_txtText.getText().toString().equals(m_txtDisplayText.getText().toString()));

        m_txtDisplayText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                m_bSyncText = (m_txtText.getText().toString().equals(m_txtDisplayText.getText().toString()));
            }
        });

    }

    public void onFindClicked()
    {
        String s = m_txtText.getText().toString();
        ArrayList<String> arCurrent = KDSGlobalVariables.getKDS().getCurrentDB().getUniqueDestinations(s);
        if (KDSConst.ENABLE_FEATURE_STATISTIC) {
            ArrayList<String> arStatistic = KDSGlobalVariables.getKDS().getStatisticDB().getUniqueDestinations(s);
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
