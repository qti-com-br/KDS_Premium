package com.bematechus.kdsstatistic;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bematechus.kdslib.KDSApplication;

/**
 * Created by David.Wong on 2018/5/11.
 * Rev:
 */
public class SOSPreferenceFragmentLayout extends SOSSettingsActivity.KDSPreferenceFragment implements  KDSUIDialogBase.KDSDialogBaseListener,SOSLinearLayout.OnSOSLinearLayoutEvents {


    SOSLinearLayout m_sosLayout = null;
    TextView m_tips = null;

    public void onKDSDialogCancel(KDSUIDialogBase dialog) {

    }


    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj) {

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =  inflater.inflate(R.layout.sos_dialog_report_layout, container, false);
        view.setBackgroundColor(this.getResources().getColor(R.color.settings_page_bg));
        init_variables(view);
        load();
        m_sosLayout.setEventReceiver(this);
        m_tips =(TextView) view.findViewById(R.id.txtTips);
        showTips(KDSApplication.getContext().getString(R.string.tips_call_menu));
        return view;
    }
    private void init_variables(View v)
    {
        m_sosLayout =(SOSLinearLayout) v.findViewById(R.id.sosLayout);
//        Button btn =(Button) v.findViewById(R.id.btnBG);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

//        btn =(Button) v.findViewById(R.id.btnClearAll);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                m_sosLayout.clearAll();
//            }
//        });

    }

    public boolean load()
    {
        SharedPreferences pref = this.getPreferenceManager().getSharedPreferences();

        String s = pref.getString(SOSSettings.SOS_VIEW_LAYOUT_KEY, "");
        m_sosLayout.parseString(s, KDSApplication.getContext());


        return true;
    }
    public boolean isChanged()
    {
        SharedPreferences pref = this.getPreferenceManager().getSharedPreferences();

        String s = pref.getString(SOSSettings.SOS_VIEW_LAYOUT_KEY, "");
        if (!m_sosLayout.outputToString().equals(s))
            return true;
        return false;
    }
    public boolean save()
    {
        if (!isChanged()) return true;
        return forceSave();

    }

    public boolean forceSave()
    {


        SharedPreferences pref = this.getPreferenceManager().getSharedPreferences();
        SharedPreferences.Editor editor = pref.edit();

        String items = m_sosLayout.outputToString();
        editor.putString(SOSSettings.SOS_VIEW_LAYOUT_KEY, items);

        //KDSGlobalVariables.getKDS().getSettings().set(KDSSOSSettings.ID.SOS_Stations, items);

        editor.commit();
        return true;
    }

    public void onSOSLinearLayoutChanged()
    {
        save();
    }
    public void onShowTips(String str)
    {
        showTips(str);
    }

    public void showTips(String strTips)
    {
        m_tips.setText(strTips);
    }

}
