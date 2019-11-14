package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bematechus.kdslib.KDSUIDialogBase;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/7/5.
 */
public class KDSUIDlgDbCorrupt extends KDSUIDialogBase implements KDSUIDialogBase.KDSDialogBaseListener {

    public enum DB_Corrupt_Operation
    {
        Unknown,
        Run_Util,
        Ignore,
        Reset,
        Abort,
    }

    private enum ConfirmOP
    {
        Ignore,
        Reset,

    }
    TextView m_txtInfo = null;
    DB_Corrupt_Operation m_corruptResult = DB_Corrupt_Operation.Unknown;


    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dlg, Object obj)
    {
        KDSUIDialogConfirm d = (KDSUIDialogConfirm)dlg;
        ConfirmOP op = (ConfirmOP) d.getTag();
        switch (op)
        {

            case Ignore:
                m_corruptResult = DB_Corrupt_Operation.Ignore;
                this.dialog.hide();
                if (this.listener != null) {
                    this.listener.onKDSDialogOK(this, getResult());
                }
                break;
            case Reset:
                m_corruptResult = DB_Corrupt_Operation.Reset;
                this.dialog.hide();
                if (this.listener != null) {
                    this.listener.onKDSDialogOK(this, getResult());
                }
                break;
        }

    }


    public Object getResult()
    {
        return m_corruptResult;
    }

    public KDSUIDlgDbCorrupt(final Context context,KDSDialogBaseListener listener) {



        this.listener = listener;
        m_view = LayoutInflater.from(context).inflate(R.layout.kdsui_dialog_db_corrupt, null);
        dialog = new AlertDialog.Builder(context)
                .create();

        // kill all padding from the dialog window
        dialog.setView(m_view, 0, 0, 0, 0);

        this.setTitle(context.getString(R.string.error));
        m_txtInfo = (TextView)this.getView().findViewById(R.id.txtInfo);
        m_txtInfo.setText(R.string.db_error_detected);

        Button btn = (Button)m_view.findViewById(R.id.btnRunUtil);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonRunUtilClicked();
            }
        });
        btn = (Button)m_view.findViewById(R.id.btnIgnore);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonIgnoreClicked();
            }
        });
        btn = (Button)m_view.findViewById(R.id.btnReset);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonResetClicked();
            }
        });
        btn = (Button)m_view.findViewById(R.id.btnAbort);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonAbortClicked();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onDlgCancel();
            }
        });

    }

    private void onButtonRunUtilClicked()
    {
        m_corruptResult = DB_Corrupt_Operation.Run_Util;
        dialog.hide();
        if (this.listener != null) {
            this.listener.onKDSDialogOK(this, getResult());
        }
    }

    private void onButtonIgnoreClicked()
    {
        KDSUIDialogConfirm dlg = new KDSUIDialogConfirm(m_view.getContext(),  m_view.getContext().getString(R.string.data_will_lost),this);
        dlg.setTag(ConfirmOP.Ignore);
        dlg.show();
        m_corruptResult = DB_Corrupt_Operation.Unknown;

    }

    private void onButtonResetClicked()
    {
        m_corruptResult = DB_Corrupt_Operation.Unknown;
        KDSUIDialogConfirm dlg = new KDSUIDialogConfirm(m_view.getContext(), m_view.getContext().getString(R.string.db_will_clear),this);
        dlg.setTag(ConfirmOP.Reset);
        dlg.show();

    }

    private void onButtonAbortClicked()
    {
        m_corruptResult = DB_Corrupt_Operation.Abort;
        dialog.hide();
        if (this.listener != null) {
            this.listener.onKDSDialogOK(this, getResult());
        }
    }

    private void onDlgCancel()
    {
        m_corruptResult = DB_Corrupt_Operation.Unknown;
        dialog.hide();
        if (this.listener != null) {
            this.listener.onKDSDialogCancel(this);
        }
    }





}
