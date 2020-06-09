package com.bematechus.kdslib;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.lang.reflect.Method;

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
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showScrollbar();
    }

    public void showScrollbar()
    {
        try {


//            if (this instanceof PreferenceFragmentStations)
//                return;
            Class<PreferenceFragment> c = PreferenceFragment.class;
            Method method = c.getMethod("getListView");
            method.setAccessible(true);
            Object obj = method.invoke(this);
            if (obj != null) {
                ListView listView = (ListView) obj;// method.invoke(this);//, null);
                listView.setScrollBarFadeDuration(0);
                listView.setScrollbarFadingEnabled(false);
                //listView.setFastScrollAlwaysVisible(true);
                //listView.setNestedScrollingEnabled(false);
            }
//                }
        }
        catch (Exception err)
        {//don't care this bug.
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + err.toString());
            //KDSLog.e(TAG, KDSUtil.error( err));
        }
    }

}