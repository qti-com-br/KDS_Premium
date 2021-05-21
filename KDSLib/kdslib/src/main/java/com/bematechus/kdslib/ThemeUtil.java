package com.bematechus.kdslib;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;

import java.util.prefs.Preferences;

public class ThemeUtil {

    static public int getAttrColor(Context c, int nResID)
    {
        TypedValue tv = new TypedValue();
        boolean b = c.getTheme().resolveAttribute(nResID, tv, true);
        int n = tv.data;
        return n;
    }

}
