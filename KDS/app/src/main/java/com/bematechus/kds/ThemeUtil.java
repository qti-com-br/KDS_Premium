package com.bematechus.kds;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;

import java.util.prefs.Preferences;

public class ThemeUtil {

    enum KDSTheme
    {
        Default,
        Dark,
        Light
    }

    public void changeTheme(Context context, KDSTheme theme, KDSSettings settings)
    {
        int nStyle = R.style.AppTheme_Dark;
        switch (theme)
        {

            case Default:
            case Dark:
                nStyle = R.style.AppTheme_Dark;
                break;
            case Light:
                nStyle = R.style.AppTheme_Light;
                break;
        }
        context.setTheme(nStyle);
        changeSettingsAfterNewTheme(context, settings);
    }

    /**
     * change settings and pref after new theme set
     * @param c
     * @param settings
     */
    public void changeSettingsAfterNewTheme( Context c, KDSSettings settings )
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        for (int i=0; i< mThemeItems.length; i++)
        {
            mThemeItems[i].doThemeChange(c, prefs, settings);
        }


    }

    static int THEME_AUTO = 0;
    static int THEME_FONT_FG = 1;
    static int THEME_FONT_BG = 2;
    static int THEME_FONT_SIZE = 3;
    static int THEME_COLOR_BG = 4;
    static int THEME_COLOR_FG = 5;


    class ThemeItem
    {
       int mFlag = 0;
       KDSSettings.ID mID = KDSSettings.ID.NULL;
       int mAttrID = 0;
        /**
         *
         * @param id
         * @param attrID
         * @param flag
         *  Change what
         */
       public ThemeItem(KDSSettings.ID id, int attrID , int flag)
       {
           mID = id;
           mAttrID = attrID;
           mFlag = flag;
       }
        public ThemeItem(KDSSettings.ID id, int attrID )
        {
            mID = id;
            mAttrID = attrID;
            mFlag = THEME_AUTO;
        }

       public void doThemeChange(Context c, SharedPreferences prefs,KDSSettings settings)
       {
           String s =  settings.getTypeString(mID);
           if (s.equals("int"))
           {
               changeInt(c, prefs, settings);
           }
           else if (s.equals("string"))
           {
               changeString(c, prefs, settings);

           }
           else if (s.equals("bool"))
           {
               changeBool(c, prefs, settings);


           }
           else if (s.equals("fontface"))
           {
               changeFontColor(c, prefs, settings);

           }
       }

       private void changeInt(Context c, SharedPreferences prefs, KDSSettings settings)
       {
           TypedValue tv = new TypedValue();
           boolean b = c.getTheme().resolveAttribute(mAttrID, tv, true );
           int n = tv.data;

           settings.savePrefValue(prefs,mID, n );

       }
        private void changeString(Context c, SharedPreferences prefs, KDSSettings settings)
        {
            TypedValue tv = new TypedValue();
            boolean b = c.getTheme().resolveAttribute(mAttrID, tv, true );

            if (mFlag == THEME_AUTO) {
                String s = tv.string.toString();
                settings.savePrefValue(prefs, mID, s);
            }
            else if (mFlag == THEME_COLOR_BG)
            {
                String strSettings =  settings.getString(mID);
                KDSBGFG bf = KDSBGFG.parseString(strSettings);
                bf.setBG(tv.data);
                settings.savePrefValue(prefs,mID, bf.toString() );
            }
            else if (mFlag == THEME_COLOR_FG)
            {
                String strSettings =  settings.getString(mID);
                KDSBGFG bf = KDSBGFG.parseString(strSettings);
                bf.setFG(tv.data);
                settings.savePrefValue(prefs,mID, bf.toString() );
            }

        }
        private void changeBool(Context c, SharedPreferences prefs, KDSSettings settings)
        {
            TypedValue tv = new TypedValue();
            boolean b = c.getTheme().resolveAttribute(mAttrID, tv, true );
            String s = tv.string.toString();
            boolean bool = Boolean.parseBoolean(s);

            settings.savePrefValue(prefs,mID, bool );

        }

        private void changeFontColor(Context c, SharedPreferences prefs, KDSSettings settings)
        {
            KDSViewFontFace ff = settings.getKDSViewFontFace(mID);
            TypedValue tv = new TypedValue();
            boolean b = c.getTheme().resolveAttribute(mAttrID, tv, true );
            int color = tv.data;
            if (mFlag == THEME_FONT_BG)
                ff.setBG(color);
            else if (mFlag == THEME_FONT_FG)
                ff.setFG(color);
            settings.savePrefValue(prefs,mID, ff );

        }
    }

    ThemeItem[] mThemeItems = new ThemeItem[]{
            new ThemeItem(KDSSettings.ID.Screen_title_fontface,R.attr.kds_title_bg, THEME_FONT_BG ),
            new ThemeItem(KDSSettings.ID.Panels_View_BG,R.attr.view_bg ),
            new ThemeItem(KDSSettings.ID.Panels_BG,R.attr.panel_bg),
            new ThemeItem(KDSSettings.ID.Panels_Block_Border_Color,R.attr.border_bg),
            new ThemeItem(KDSSettings.ID.Panels_Panel_Number_BGFG,R.attr.panelnum_bg, THEME_COLOR_BG),
            new ThemeItem(KDSSettings.ID.Panels_Panel_Number_BGFG,R.attr.panelnum_fg, THEME_COLOR_FG),
            new ThemeItem(KDSSettings.ID.Order_Normal_FontFace,R.attr.caption_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Order_Normal_FontFace,R.attr.caption_fg, THEME_FONT_FG),
            new ThemeItem(KDSSettings.ID.Focused_BG,R.attr.focus_bg),

    };

}
