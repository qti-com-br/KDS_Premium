package com.bematechus.kds;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSToast;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.OpenFileDialog;

import java.util.ArrayList;

public class KDSPreferenceSumStnFilter extends Preference implements KDSUIDialogBase.KDSDialogBaseListener {


        public KDSPreferenceSumStnFilter(Context context, AttributeSet attrs) {
            super(context, attrs);

//            String defaultVal = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
//            if (defaultVal.equals("1"))
//                m_bSave =  true;

        }

        /**
         * @param dialog
         */
        public void onKDSDialogCancel(KDSUIDialogBase dialog) {

        }

        /**
         * ip selection dialog
         *
         * @param dlg
         * @param obj
         */
        public void onKDSDialogOK(KDSUIDialogBase dlg, Object obj)// ArrayList<String> stations)
        {
            if (dlg instanceof KDSUIDialogSumStationFilter)
            {
                KDSUIDialogSumStationFilter d = (KDSUIDialogSumStationFilter)dlg;
                String s = (String) d.getResult();
                save(s);
            }
        }

        @Override
        protected void onClick() {
            KDSUIDialogSumStationFilter d = new  KDSUIDialogSumStationFilter(this.getContext(), load(), this);
            d.show();
        }

    static public final String SUM_STATION_KEY_ITEMS = "sumstn_filters";

    public boolean save(String filter)
    {
        if (!isChanged(filter)) return true;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();

        String items = filter;
        editor.putString(SUM_STATION_KEY_ITEMS, items);

        KDSGlobalVariables.getKDS().getSettings().set(KDSSettings.ID.SumStn_filters, items);

        editor.commit();

//        if (KDSGlobalVariables.getMainActivity() != null)
//        {
//            KDSGlobalVariables.getMainActivity().onSharedPreferenceChanged(null, SUM_STATION_KEY_ITEMS);
//        }
        return true;
    }

    public boolean isChanged(String filter)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());

        String s = pref.getString(SUM_STATION_KEY_ITEMS, "");
        if (!filter.equals(s))
            return true;
        return false;
    }

    public String load()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());


        String s = pref.getString(SUM_STATION_KEY_ITEMS, "");

        return s;
    }

}