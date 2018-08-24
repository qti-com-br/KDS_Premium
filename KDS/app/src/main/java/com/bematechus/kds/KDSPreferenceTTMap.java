package com.bematechus.kds;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

import com.bematechus.kdslib.KDSApplication;

/**
 * Table tracker map
 */
public class KDSPreferenceTTMap   extends DialogPreference implements KDSUIDialogBase.KDSDialogBaseListener{
    private PreferenceActivity parent;

    private String m_strMaps = "";
    private  String m_strKey = "";


    public KDSPreferenceTTMap(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        m_strKey = this.getKey();
        init_summary(context);
    }

    public KDSPreferenceTTMap(Context context, AttributeSet attrs) {
        super(context, attrs);

        m_strKey = this.getKey();
        init_summary(context);

    }
    private void init_summary(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());

        m_strMaps = pref.getString(m_strKey, "");

        this.setSummary(m_strMaps);
    }

    void setActivity(PreferenceActivity parent) {
        this.parent = parent;
    }

    @Override
    public boolean isPersistent() {

        return true;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        init_summary(this.getContext());
    }

    @Override
    protected void showDialog (Bundle state)
    {

        KDSUIDlgTTMap dlg = new KDSUIDlgTTMap(this.getContext(),m_strMaps,this );
        dlg.show();


    }


    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        String str = (String)obj;

        this.setSummary(str);
        SharedPreferences.Editor editor = this.getSharedPreferences().edit();
        editor.putString(m_strKey, str);
        editor.apply();
        editor.commit();
    }
}
