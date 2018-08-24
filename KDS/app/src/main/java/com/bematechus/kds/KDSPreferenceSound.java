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
 * Sound settings
 */
public class KDSPreferenceSound extends DialogPreference implements KDSUIDialogBase.KDSDialogBaseListener {
    private PreferenceActivity parent;

    private String m_strSoundUri = "";
    private  String m_strKey = "";


    public KDSPreferenceSound(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        m_strKey = this.getKey();
        init_summary(context);
    }

    public KDSPreferenceSound(Context context, AttributeSet attrs) {
        super(context, attrs);

        m_strKey = this.getKey();
        init_summary(context);

    }
    private void init_summary(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());

        m_strSoundUri = pref.getString(m_strKey, "");

        KDSSound sound = KDSSound.parseString(m_strSoundUri);
        this.setSummary(sound.getDescription());


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

        KDSUIDialogChooseSound dlg = new KDSUIDialogChooseSound(this.getContext(),m_strSoundUri,this );
        dlg.show();


    }


    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        String str = (String)obj;
        KDSSound sound = KDSSound.parseString(str);
        this.setSummary(sound.getDescription());
        SharedPreferences.Editor editor = this.getSharedPreferences().edit();
        editor.putString(m_strKey, str);
        editor.apply();
        editor.commit();
    }
}
