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
import android.widget.RadioButton;
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
public class KDSUIDlgInputSumStationFilterEntry  extends KDSUIDialogBase  implements  KDSUIDialogBase.KDSDialogBaseListener{

    TextView m_txtText = null;
    SumStationFilterEntry mEntryOriginal = null;
    SumStationFilterEntry mEntryEdit = null;


    //String m_strDescription = "";

    //SumStationFilterEntry.EntryType m_mode = SumStationFilterEntry.EntryType.Item;

    public SumStationFilterEntry getOriginalEntry()
    {
        return mEntryOriginal;
    }



    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return mEntryEdit;
    }
    public void onOkClicked()
    {
        mEntryEdit = new SumStationFilterEntry();
        mEntryEdit.setDescription(m_txtText.getText().toString());
        String s = ((TextView)this.getView().findViewById(R.id.txtDisplay)).getText().toString();
        mEntryEdit.setDisplayText(s);
        if ( ((RadioButton)this.getView().findViewById(R.id.rbItem)).isChecked())
            mEntryEdit.setEntryType(SumStationEntry.EntryType.Item);
        else
            mEntryEdit.setEntryType(SumStationEntry.EntryType.Condiment);
        //m_strDescription = m_txtText.getText().toString();
    }

//    public SumStationEntry.EntryType getEntryType()
//    {
//        return m_mode;
//    }


    private SumStationEntry.EntryType getSelectedEntryType()
    {
        if ( ((RadioButton)this.getView().findViewById(R.id.rbItem)).isChecked())
            return SumStationEntry.EntryType.Item;
        else
            return SumStationEntry.EntryType.Condiment;
    }

    public KDSUIDlgInputSumStationFilterEntry(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener, SumStationFilterEntry entry) {
        this.setUseCtrlEnterKey(true);
        this.int_dialog(context, listener, R.layout.kdsui_dlg_input_sumstn_entry, "");
        //m_mode = mode;
        mEntryOriginal = entry;

        this.setTitle(context.getString(R.string.input_item_description));
        //if (m_mode == SumStationEntry.EntryType.Condiment)
        //    this.setTitle(context.getString(R.string.input_condiment_description));
        m_txtText = (TextView)this.getView().findViewById(R.id.txtText);
        //m_lstData = (ListView)this.getView().findViewById(R.id.lstData);

        //ArrayAdapter<String> adapter = new ArrayAdapter<>(KDSApplication.getContext(),R.layout.array_adapter, m_arData);
        //m_lstData.setAdapter(adapter);

        //m_lstData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        //    @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String s = m_arData.get(position);
//                m_txtText.setText(s);
//            }
//        });


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
        KDSUIDialogBrowseInDB dlg = new KDSUIDialogBrowseInDB(this.getView().getContext(), getSelectedEntryType(),s, this );
        dlg.show();
    }



    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }


    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {

        if (!(dialog instanceof KDSUIDialogBrowseInDB))
            return;

        KDSUIDialogBrowseInDB dlg = (KDSUIDialogBrowseInDB)dialog;
        String s = (String) dlg.getResult();
        afterSelectedEntryFromDB(s);
        //if (findItem(m_arData, s)) return;
        //if (dlg.getMode() == KDSUIDlgInputItemDescription.Mode.Condiment)
        //    s += KDSSummaryItem.CONDIMENT_TAG;

//        m_arData.add(s);
//        ((ArrayAdapter)m_lstData.getAdapter()).notifyDataSetChanged();
//        save();
    }

    public void afterSelectedEntryFromDB(String description)
    {
        ((TextView)this.getView().findViewById(R.id.txtText)).setText(description);
        ((TextView)this.getView().findViewById(R.id.txtDisplay)).setText(description);

    }


}
