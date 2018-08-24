
package com.bematechus.kdsstatistic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSSMBPath;

/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

/**
 * For remote folder settings
 */
public class STPreferenceSmbFolder extends DialogPreference implements STDlgInputSMBFolder.OnKDSUIDlgInputSMBFolderListener{
    private PreferenceActivity parent;

    private String m_strUri = "";
    private  String m_strKey = "";


    public STPreferenceSmbFolder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        m_strKey = this.getKey();

        init_summary(context);
    }

    public STPreferenceSmbFolder(Context context, AttributeSet attrs) {
        super(context, attrs);

        m_strKey = this.getKey();

        init_summary(context);

    }
    private void init_summary(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( KDSApplication.getContext());

        m_strUri = pref.getString(m_strKey,"" );
        refreshSummary(m_strUri);

    }

    private void refreshSummary(String val)
    {
        KDSSMBPath p = KDSSMBPath.parseString(val);
        this.setSummary(p.toDisplayString());// m_strUri);
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
        m_strUri = this.getSharedPreferences().getString(m_strKey,"" );
        refreshSummary(m_strUri);

    }



    @Override
    protected void showDialog (Bundle state)
    {

        KDSSMBPath smb = KDSSMBPath.parseString(m_strUri);


        STDlgInputSMBFolder dlg = new STDlgInputSMBFolder(this.getContext(),smb,this );
        dlg.show();


    }

    /**
     *
     * @param dialog
     * @param station
     */
    public void onSMBOk(STDlgInputSMBFolder dialog, KDSSMBPath station)
    {
        m_strUri = station.toString();
        refreshSummary(m_strUri);

        SharedPreferences.Editor editor = this.getSharedPreferences().edit();
        editor.putString(m_strKey, m_strUri);
        editor.apply();
        editor.commit();

    }
}

