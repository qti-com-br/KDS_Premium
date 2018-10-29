package com.bematechus.kdslib;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by David.Wong on 2018/10/28.
 * Rev:
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class KDSPreferenceFragment extends PreferenceFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
        View v = super.onCreateView(inflater, root, savedInstanceState);

        v.setBackgroundColor(this.getResources().getColor(R.color.settings_page_bg));
        v.setPadding(0,0,0,0);


        return v;
    }


}