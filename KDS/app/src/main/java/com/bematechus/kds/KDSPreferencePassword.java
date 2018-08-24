package com.bematechus.kds;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

/**
 * For password configuration
 */
public class KDSPreferencePassword extends DialogPreference implements KDSUIDialogBase.KDSDialogBaseListener {

    private  String m_strKey = "";


    public KDSPreferencePassword(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        m_strKey = this.getKey();

    }

    public KDSPreferencePassword(Context context, AttributeSet attrs) {
        super(context, attrs);

        m_strKey = this.getKey();


    }

    @Override
    public boolean isPersistent() {
         return true;
    }

    @Override
    protected void showDialog (Bundle state)
    {

        KDSUIDlgInputPassword dlg = new KDSUIDlgInputPassword(this.getContext(),this, true );
        dlg.show();
        dlg.enableOKButton(false);
    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        String str = (String)obj;

        SharedPreferences.Editor editor = this.getSharedPreferences().edit();
        editor.putString(m_strKey, str);
        editor.apply();
        editor.commit();
        this.notifyChanged();
    }

}
