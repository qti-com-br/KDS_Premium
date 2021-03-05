package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUIDlgInternetFile;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.OpenFileDialog;
import com.bematechus.kdslib.OpenSmbFileDialog;

public class KDSUIDlgInputSumStationAlertEntry  extends KDSUIDialogBase implements  KDSUIDialogBase.KDSDialogBaseListener {

    TextView m_txtText = null;
    SumStationAlertEntry mEntryOriginal = null;
    SumStationAlertEntry mEntryEdit = null;


    public SumStationAlertEntry getOriginalEntry() {
        return mEntryOriginal;
    }


    /**
     * it will been overrided by child
     *
     * @return
     */
    public Object getResult() {
        return mEntryEdit;
    }

    public void onOkClicked() {
        mEntryEdit = new SumStationAlertEntry();
        mEntryEdit.setDescription(m_txtText.getText().toString());
        if (((RadioButton) this.getView().findViewById(R.id.rbItem)).isChecked())
            mEntryEdit.setEntryType(SumStationEntry.EntryType.Item);
        else
            mEntryEdit.setEntryType(SumStationEntry.EntryType.Condiment);
        mEntryEdit.setDisplayText(((TextView)this.getView().findViewById(R.id.txtDisplay)).getText().toString());
        String s = ((TextView)this.getView().findViewById(R.id.txtDisplay)).getText().toString();
        int n = KDSUtil.convertStringToInt(s, -1);
        mEntryEdit.setAlertQty(n);
        mEntryEdit.setAlertTime(((TextView)this.getView().findViewById(R.id.txtTime)).getText().toString());
        mEntryEdit.setAlertMessage(((TextView)this.getView().findViewById(R.id.txtMessage)).getText().toString());
        mEntryEdit.setAlertImageFile(((TextView)this.getView().findViewById(R.id.imageFileName)).getText().toString());

        //m_strDescription = m_txtText.getText().toString();
    }


    private SumStationEntry.EntryType getSelectedEntryType() {
        if (((RadioButton) this.getView().findViewById(R.id.rbItem)).isChecked())
            return SumStationEntry.EntryType.Item;
        else
            return SumStationEntry.EntryType.Condiment;
    }

    public KDSUIDlgInputSumStationAlertEntry(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener, SumStationAlertEntry entry) {
        this.setUseCtrlEnterKey(true);
        this.int_dialog(context, listener, R.layout.kdsui_dlg_sumstn_input_alert_item, "");
        //m_mode = mode;
        mEntryOriginal = entry;

        this.setTitle(context.getString(R.string.input_item_description));


        m_txtText = (TextView) this.getView().findViewById(R.id.txtText);
        showEntry(entry);

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


        Button btn = (Button) this.getView().findViewById(R.id.btnFind);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFindClicked();
            }
        });
        btn = (Button) this.getView().findViewById(R.id.btnResetTime);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetAlertTime();
            }
        });


        btn = (Button) this.getView().findViewById(R.id.btnChooseTime);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChooseAlertTime();
            }
        });
        btn = (Button) this.getView().findViewById(R.id.btnLocal);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLocalFile();
            }
        });
        btn = (Button) this.getView().findViewById(R.id.btnEthernet);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEthernetFile();
            }
        });
        btn = (Button) this.getView().findViewById(R.id.btnInternet);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInternetFile();
            }
        });
    }

    private void onResetAlertTime() {
        ((TextView) this.getView().findViewById(R.id.txtTime)).setText("");
    }
    AlertDialog mDlgChooseTime = null;
    private void onChooseAlertTime()
    {
        if (mDlgChooseTime != null)
            mDlgChooseTime.hide();
        mDlgChooseTime = new AlertDialog.Builder(this.getView().getContext())
                .setTitle("Alert time")
                //.setMessage(this.getString(R.string.alert_disable_double_bump_queue))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TextView t = (TextView)KDSUIDlgInputSumStationAlertEntry.this.getView().findViewById(R.id.txtTime);
                                TimePicker tp = mDlgChooseTime.findViewById(R.id.time_picker);
                                String tm = String.format("%02d:%02d", tp.getCurrentHour(), tp.getCurrentMinute());
                                t.setText(tm);
                            }
                        }
                )
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                .create();
        View v = LayoutInflater.from(this.getView().getContext()).inflate(R.layout.dlg_time_picker, null);
        mDlgChooseTime.setView(v);
        v.findViewById(R.id.chkEnabled).setVisibility(View.GONE);

        mDlgChooseTime.show();


    }
    private void onLocalFile()
    {
        OpenFileDialog dlg = new OpenFileDialog(this.getView().getContext(),getValidImageExtensions(), this, OpenFileDialog.Mode.Choose_File );
        //dlg.setTag(TAG_FOR_BUILDCARD);
        dlg.show();
    }

    private void onEthernetFile()
    {
        OpenSmbFileDialog dlg = new OpenSmbFileDialog(this.getView().getContext(),getValidImageExtensions(), this);
        //dlg.setTag(TAG_FOR_BUILDCARD);
        dlg.show();
    }

    private void onInternetFile()
    {
        KDSUIDlgInternetFile dlg = new KDSUIDlgInternetFile(this.getView().getContext(), getValidImageExtensions(), this);
        //dlg.setTag(TAG_FOR_BUILDCARD);
        dlg.show();
    }
    private String getValidImageExtensions()
    {
        String s = ".jpg;.gif;.png;.bmp;.jpeg;";
        return s;
    }
    /**
     *
     * @param entry
     */
    private void showEntry(SumStationAlertEntry entry)
    {
        if (entry != null) {
            ((TextView) this.getView().findViewById(R.id.txtText)).setText(entry.getDescription());
            ((TextView) this.getView().findViewById(R.id.txtDisplay)).setText(entry.getDisplayText());

            if (entry.getAlertQty() >0)
                ((TextView) this.getView().findViewById(R.id.txtQty)).setText(KDSUtil.convertIntToString(entry.getAlertQty()));
            else
                ((TextView) this.getView().findViewById(R.id.txtQty)).setText("");

            ((TextView) this.getView().findViewById(R.id.txtTime)).setText(entry.getAlertTime());
            ((TextView) this.getView().findViewById(R.id.txtMessage)).setText(entry.getAlertMessage());
            ((TextView) this.getView().findViewById(R.id.imageFileName)).setText(entry.getAlertImageFile());
        }
        else
        {
            ((TextView) this.getView().findViewById(R.id.txtText)).setText("");
            ((TextView) this.getView().findViewById(R.id.txtDisplay)).setText("");
            ((TextView) this.getView().findViewById(R.id.txtQty)).setText("");
            ((TextView) this.getView().findViewById(R.id.txtTime)).setText("");
            ((TextView) this.getView().findViewById(R.id.txtMessage)).setText("");
            ((TextView) this.getView().findViewById(R.id.imageFileName)).setText("");
        }


    }

    public void onFindClicked() {
        String s = m_txtText.getText().toString();
        KDSUIDialogBrowseInDB dlg = new KDSUIDialogBrowseInDB(this.getView().getContext(), getSelectedEntryType(), s, this);
        dlg.show();
    }


    public void onKDSDialogCancel(KDSUIDialogBase dialog) {

    }


    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj) {

        if ((dialog instanceof KDSUIDialogBrowseInDB)) {

            KDSUIDialogBrowseInDB dlg = (KDSUIDialogBrowseInDB) dialog;
            String s = (String) dlg.getResult();
            afterSelectedEntryFromDB(s);

        }
        else if ( (dialog instanceof OpenFileDialog) ||
                (dialog instanceof OpenSmbFileDialog) ||
                (dialog instanceof KDSUIDlgInternetFile) )
        {
            String s = (String)dialog.getResult();
            ((TextView) this.getView().findViewById(R.id.imageFileName)).setText(s);
        }

    }

    public void afterSelectedEntryFromDB(String description) {
        ((TextView) this.getView().findViewById(R.id.txtText)).setText(description);

    }
}