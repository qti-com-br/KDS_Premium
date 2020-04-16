package com.bematechus.kdslib;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by David.Wong on 2020/4/16.
 * Rev:
 */
public class KDSUIDlgAgreement extends KDSUIDialogBase {

    static private KDSUIDlgAgreement m_dlg = null;
    static public KDSUIDlgAgreement instance(Context context, KDSUIDialogBase.KDSDialogBaseListener listener)
    {

        if (m_dlg != null)
        {
            m_dlg.getDialog().hide();
            m_dlg = null;

        }
        m_dlg = new KDSUIDlgAgreement(context, listener);
        return m_dlg;

    }
    public KDSUIDlgAgreement(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener)
    {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_agreement, "");
        this.setCancelByClickOutside(false);
        //this.setTitle("Agreement");

    }

    public String makeOKButtonText(Context context)
    {
        String s = context.getString(R.string.agree);
        String bumpbar = getBumpbarOKKeyText(context);
        return s + bumpbar;
    }

    public String makeCancelButtonText(Context context)
    {
        String s = context.getString(R.string.quit);
        String bumpbar = getBumpbarCancelKeyText(context);
        return s + bumpbar;
    }

    static public void setAgreementAgreed(boolean bAgree)
    {
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pre.edit();
        if (bAgree)
            editor.putString("agreement", "1");
        else
            editor.putString("agreement", "0");
        editor.apply();
        editor.commit();
    }

    static public boolean isAgreementAgreed()
    {
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        String s = pre.getString("agreement", "0");

        return  (s.equals("1"));
    }

    static public void forceAgreementAgreed( Context context, KDSUIDialogBase.KDSDialogBaseListener listener)
    {
        //debug
        //KDSUIDlgAgreement.setAgreementAgreed(false);
        //
        if (KDSUIDlgAgreement.isAgreementAgreed())
            return;

        //KDSUIDlgAgreement dlg = new KDSUIDlgAgreement(this, this);
        KDSUIDlgAgreement dlg =KDSUIDlgAgreement.instance(context, listener);
        dlg.show();
    }
}
