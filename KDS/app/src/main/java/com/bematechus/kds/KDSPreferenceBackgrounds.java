package com.bematechus.kds;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSUIDialogBase;

public class KDSPreferenceBackgrounds extends Preference implements KDSUIDialogBase.KDSDialogBaseListener {


    public KDSPreferenceBackgrounds(Context context, AttributeSet attrs) {
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
        if (dlg instanceof KDSUIDialogBGImages)
        {
            KDSUIDialogBGImages d = (KDSUIDialogBGImages)dlg;
            String s = (String) d.getResult();
            save(s);
        }
    }

    @Override
    protected void onClick() {
        KDSUIDialogBGImages d = new  KDSUIDialogBGImages(this.getContext(),this, load());
        d.show();
    }

    static public final String BG_IMAGES_KEY_ITEMS = "general_bg_images";

    public boolean save(String files)
    {
        if (!isChanged(files)) return true;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();

        String items = files;
        editor.putString(BG_IMAGES_KEY_ITEMS, items);

        KDSGlobalVariables.getKDS().getSettings().set(KDSSettings.ID.SumStn_filters, items);

        editor.commit();

//        if (KDSGlobalVariables.getMainActivity() != null)
//        {
//            KDSGlobalVariables.getMainActivity().onSharedPreferenceChanged(null, SUM_STATION_KEY_ITEMS);
//        }
        return true;
    }

    public boolean isChanged(String files)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());

        String s = pref.getString(BG_IMAGES_KEY_ITEMS, "");
        if (!files.equals(s))
            return true;
        return false;
    }

    public String load()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());


        String s = pref.getString(BG_IMAGES_KEY_ITEMS, "");

        return s;
    }

}
