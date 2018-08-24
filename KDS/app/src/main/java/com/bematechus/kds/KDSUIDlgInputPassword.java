package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/4/8 0008.
 */
public class KDSUIDlgInputPassword  extends KDSUIDialogBase  {

    TextView m_txtText = null;
    String m_strPassword = "";

    TextView m_txtConfirm = null;
    boolean m_bNeedConfirm = false;

    boolean m_bDisableCancel = false;

    public void setDisableCancelButton(boolean bDisable)
    {
        m_bDisableCancel = bDisable;
    }
    public boolean getNeedConfirm()
    {
        return m_bNeedConfirm;
    }
    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return m_strPassword;
    }
    public void onOkClicked()
    {

        m_strPassword = m_txtText.getText().toString();
    }



    public KDSUIDlgInputPassword(final Context context,KDSUIDialogBase.KDSDialogBaseListener listener, boolean bNeedConfirm) {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_input_password, "");
        this.setTitle(context.getString(R.string.password));

        m_txtText = (TextView)this.getView().findViewById(R.id.txtText);
        m_txtConfirm = (TextView)this.getView().findViewById(R.id.txtConfirm);
        m_bNeedConfirm = bNeedConfirm;
        if (!bNeedConfirm)
        {
            this.getView().findViewById(R.id.linearConfirm).setVisibility(View.GONE);
        }
        else {

            m_txtText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    checkConfirm();
                }
            });

            m_txtConfirm.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    checkConfirm();
                }
            });
        }


    }

    private void checkConfirm()
    {
        String strConfirm =  m_txtConfirm.getText().toString();
        String strPwd =m_txtText.getText().toString();
        boolean b = strConfirm.equals(strPwd);
        if (strPwd.isEmpty())
            b = false;
        if (!m_bNeedConfirm)
            b = true;
        this.getDialog().getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(b);
        TextView t = (TextView) this.getView().findViewById(R.id.txtError);

        if (strPwd.isEmpty() && strConfirm.isEmpty())
            b = true;

        if (!b)
        {
            t.setText(getView().getContext().getString( R.string.pwd_do_not_match));
        }
        else
            t.setText("");




    }
    protected String makeButtonText2(Context context, int nResID, KDSSettings.ID funcKey )
    {
        return makeCtrlEnterButtonText(context, nResID, funcKey);

    }

    protected void init_dialog_events(final AlertDialog dlg)
    {
        init_dialog_ctrl_enter_events(dlg);

    }

    /**
     * 2.0.12,
     */
    public void show() {
        super.show();
        if (m_bDisableCancel && (m_bNeedConfirm))
            this.getDialog().getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        checkConfirm();
    }



}
