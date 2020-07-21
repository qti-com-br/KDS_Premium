package com.bematechus.kds;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSUIDialogBase;

/**
 * Sound settings
 */
public class KDSPreferenceSound extends DialogPreference implements KDSUIDialogBase.KDSDialogBaseListener {
    private PreferenceActivity parent;

    private String m_strSoundUri = "";
    private  String m_strKey = "";
    private String m_strDefaultValue = "";

    public KDSPreferenceSound(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
//        String defaultVal = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
//        m_strDefaultValue = defaultVal;
//        m_strKey = this.getKey();
//        init_summary(context);
    }

    public KDSPreferenceSound(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
//        m_strKey = this.getKey();
//        init_summary(context);

    }
    private void init(Context context, AttributeSet attrs)
    {

//        int ncount = attrs.getAttributeCount();
//        for (int i=0; i< ncount; i++)
//        {
//            String name = attrs.getAttributeName(i);
//            String value = attrs.getAttributeValue(i);
//            String s = name + value;
//
//        }
        int nResID = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "defaultValue", -1);
        String defaultVal = "";
        if (nResID!= -1)
            defaultVal = context.getString(nResID);


        m_strDefaultValue = defaultVal;
        m_strKey = this.getKey();
        init_summary(context);
    }


    private void init_summary(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());

        //m_strSoundUri = pref.getString(m_strKey, "");
        m_strSoundUri = pref.getString(m_strKey,m_strDefaultValue);

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
