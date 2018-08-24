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
import com.bematechus.kdslib.KDSUtil;

/**
 * For configuration bump bar key settings.
 */
public class KDSPreferenceKeySelection  extends DialogPreference implements KDSUIDialogBase.KDSDialogBaseListener{

    private PreferenceActivity parent;

    private  KDSBumpBarKeyFunc m_bumpbarKey = new KDSBumpBarKeyFunc();
    //private ImageView preview_img;

    String m_strSavePath = "";
    String m_strKeyboardTypePath = "";
    String m_strDefault = "";
    Context m_context = null;
    public KDSPreferenceKeySelection(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);

    }

    public KDSPreferenceKeySelection(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);


    }

    private void init(Context context)
    {
        m_context = context;
        m_strSavePath = this.getKey();
        m_strKeyboardTypePath = this.getDialogMessage().toString(); //sav ethe keyboard type perference path to this message.
        m_strDefault = this.getDialogTitle().toString(); //save the default string to the dialog title.
        refresh_summary();//context);
    }

    public void refresh_summary()//Context context)
   // public void refresh_summary()
    {
        if (m_context == null)
            return;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());

        String s = pref.getString(m_strSavePath, "");
        if (s.isEmpty())
            s = m_strDefault;
        m_bumpbarKey = KDSBumpBarKeyFunc.parseString(s);
        this.setSummary(m_bumpbarKey.getSummaryString(getKbType()));
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
        String s = this.getSharedPreferences().getString(m_strSavePath,"" );
        if (s.isEmpty())
            s = m_strDefault;
        m_bumpbarKey = KDSBumpBarKeyFunc.parseString(s);


        this.setSummary(m_bumpbarKey.getSummaryString(getKbType()));

    }

    private KDSBumpBarKeyFunc.KeyboardType getKbType()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        String strKbType = pref.getString(m_strKeyboardTypePath, "0"); //default standard kb
        KDSBumpBarKeyFunc.KeyboardType kbType = KDSBumpBarKeyFunc.KeyboardType.values()[KDSUtil.convertStringToInt(strKbType, 0)];
        return kbType;
    }

    @Override
    protected void showDialog (Bundle state)
    {


        KDSUIDialogBumpBarKey dlg = new KDSUIDialogBumpBarKey(this.getContext(), m_bumpbarKey, this,getKbType() );
        dlg.show();

    }



    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        KDSBumpBarKeyFunc key = (KDSBumpBarKeyFunc)obj;
        m_bumpbarKey = key;
        this.setSummary(m_bumpbarKey.getSummaryString(getKbType()));

        SharedPreferences.Editor editor = this.getSharedPreferences().edit();
        editor.putString(m_strSavePath, m_bumpbarKey.toString());
        editor.apply();
        editor.commit();
    }

}
