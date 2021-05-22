package com.bematechus.kds;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;

public class KDSTheme {
    enum MyTheme
    {
        Default,
        Dark,
        Light
    }

    //record if need to update settings after change theme.
    static public boolean m_updateSettings = false;

    static public MyTheme loadMyThemeValue(Context c)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String s = prefs.getString("theme_mode", "0");
        int n = KDSUtil.convertStringToInt(s, 0);
        MyTheme t = MyTheme.values()[n];
        return t;

    }

    static public int loadTheme(Context c)
    {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
//        String s = prefs.getString("theme_mode", "0");
//        int n = KDSUtil.convertStringToInt(s, 0);
//        MyTheme t = MyTheme.values()[n];
        MyTheme t = loadMyThemeValue(c);

        return convertKDSThemeValue(t);
    }

    static public int convertKDSThemeValue(MyTheme theme)
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
        return  nStyle;
    }
    public void changeTheme(Context context, MyTheme theme, KDSSettings settings)
    {
        int nStyle = convertKDSThemeValue(theme);
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
        SharedPreferences.Editor editor =  prefs.edit();
        for (int i=0; i< mThemeItems.length; i++)
        {
            mThemeItems[i].doThemeChange(c, editor, settings);
        }
        editor.commit();
        editor.apply();


    }

    static final String FLAG_KEY ="theme_update_setting";
    static public void saveChangeSettingsFlag(Context c, boolean bUpdate)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor =  prefs.edit();
        editor.putBoolean(FLAG_KEY, bUpdate);
        editor.commit();
        editor.apply();


    }
    static public boolean loadChangeSettingsFlag(Context c)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(FLAG_KEY, false );
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

        public void doThemeChange(Context c, SharedPreferences.Editor editor, KDSSettings settings)
        {
            String s =  settings.getTypeString(mID);
            if (s.equals("int"))
            {
                changeInt(c, editor, settings);
            }
            else if (s.equals("string"))
            {
                changeString(c, editor, settings);

            }
            else if (s.equals("bool"))
            {
                changeBool(c, editor, settings);


            }
            else if (s.equals("fontface"))
            {
                changeFontColor(c, editor, settings);

            }
        }

        private void changeInt(Context c, SharedPreferences.Editor editor, KDSSettings settings)
        {
            TypedValue tv = new TypedValue();
            boolean b = c.getTheme().resolveAttribute(mAttrID, tv, true );
            int n = tv.data;

            settings.savePrefValue(editor,mID, n );

        }
        private void changeString(Context c, SharedPreferences.Editor editor, KDSSettings settings)
        {
            TypedValue tv = new TypedValue();
            boolean b = c.getTheme().resolveAttribute(mAttrID, tv, true );

            if (mFlag == THEME_AUTO) {
                String s = tv.string.toString();
                settings.savePrefValue(editor, mID, s);
            }
            else if (mFlag == THEME_COLOR_BG)
            {
                String strSettings =  settings.getString(mID);
                KDSBGFG bf = KDSBGFG.parseString(strSettings);
                bf.setBG(tv.data);
                settings.savePrefValue(editor,mID, bf.toString() );
            }
            else if (mFlag == THEME_COLOR_FG)
            {
                String strSettings =  settings.getString(mID);
                KDSBGFG bf = KDSBGFG.parseString(strSettings);
                bf.setFG(tv.data);
                settings.savePrefValue(editor,mID, bf.toString() );
            }

        }
        private void changeBool(Context c, SharedPreferences.Editor editor, KDSSettings settings)
        {
            TypedValue tv = new TypedValue();
            boolean b = c.getTheme().resolveAttribute(mAttrID, tv, true );
            String s = tv.string.toString();
            boolean bool = Boolean.parseBoolean(s);

            settings.savePrefValue(editor,mID, bool );

        }

        private void changeFontColor(Context c, SharedPreferences.Editor editor, KDSSettings settings)
        {
            KDSViewFontFace ff = settings.getKDSViewFontFace(mID);
            TypedValue tv = new TypedValue();
            //Resources.Theme t = c.getTheme();

            boolean b = c.getTheme().resolveAttribute(mAttrID, tv, true );
            int color = tv.data;
            if (mFlag == THEME_FONT_BG)
                ff.setBG(color);
            else if (mFlag == THEME_FONT_FG)
                ff.setFG(color);
            settings.savePrefValue(editor,mID, ff );

        }
    }

    /**
     * All configuration need to been changed.
     * These are the variables that can been changed in settings page.
     *
     */
    ThemeItem[] mThemeItems = new ThemeItem[]{
            new ThemeItem(KDSSettings.ID.Screen_title_fontface,R.attr.kds_title_bg, THEME_FONT_BG ),
            new ThemeItem(KDSSettings.ID.Screen_title_fontface,R.attr.kds_title_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Panels_View_BG,R.attr.view_bg ),
            new ThemeItem(KDSSettings.ID.Panels_BG,R.attr.panel_bg),
            new ThemeItem(KDSSettings.ID.Panels_Block_Border_Color,R.attr.border_bg),
            new ThemeItem(KDSSettings.ID.Panels_Panel_Number_BGFG,R.attr.panelnum_bg, THEME_COLOR_BG),
            new ThemeItem(KDSSettings.ID.Panels_Panel_Number_BGFG,R.attr.panelnum_fg, THEME_COLOR_FG),
            new ThemeItem(KDSSettings.ID.Order_Normal_FontFace,R.attr.caption_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Order_Normal_FontFace,R.attr.caption_fg, THEME_FONT_FG),
            new ThemeItem(KDSSettings.ID.Focused_BG,R.attr.focus_bg),
            new ThemeItem(KDSSettings.ID.Order_Timer_Stage0_Color,R.attr.stage0_bg),
            new ThemeItem(KDSSettings.ID.Order_Timer_Stage1_Color,R.attr.stage1_bg),
            new ThemeItem(KDSSettings.ID.Order_Timer_Stage2_Color,R.attr.stage2_bg),

            new ThemeItem(KDSSettings.ID.Item_Default_FontFace,R.attr.item_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Item_Default_FontFace,R.attr.item_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Condiment_Default_FontFace,R.attr.condiment_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Condiment_Default_FontFace,R.attr.condiment_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Message_Default_FontFace,R.attr.premsg_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Message_Default_FontFace,R.attr.premsg_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Order_Footer_FontFace,R.attr.focus_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Order_Footer_FontFace,R.attr.footer_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Tab_bgfg,R.attr.tab_display_bg, THEME_COLOR_BG),
            new ThemeItem(KDSSettings.ID.Tab_bgfg,R.attr.tab_display_fg, THEME_COLOR_FG),

            new ThemeItem(KDSSettings.ID.Highlight_rush_bgfg,R.attr.rush_bg, THEME_COLOR_BG),
            new ThemeItem(KDSSettings.ID.Highlight_rush_bgfg,R.attr.rush_fg, THEME_COLOR_FG),

            new ThemeItem(KDSSettings.ID.Highlight_fire_bgfg,R.attr.fire_bg, THEME_COLOR_BG),
            new ThemeItem(KDSSettings.ID.Highlight_fire_bgfg,R.attr.fire_fg, THEME_COLOR_FG),


            new ThemeItem(KDSSettings.ID.Highlight_dest_bgfg,R.attr.dest_bg, THEME_COLOR_BG),
            new ThemeItem(KDSSettings.ID.Highlight_dest_bgfg,R.attr.dest_fg, THEME_COLOR_FG),

            new ThemeItem(KDSSettings.ID.Exp_Alert_Color_BG,R.attr.expalert_bg),



            new ThemeItem(KDSSettings.ID.Queue_order_ID_font,R.attr.queue_order_id_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Queue_order_ID_font,R.attr.queue_order_id_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Queue_order_timer_font,R.attr.queue_order_timer_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Queue_order_timer_font,R.attr.queue_order_timer_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Queue_customer_name_font,R.attr.queue_cusomer_name_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Queue_customer_name_font,R.attr.queue_cusomer_name_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Queue_custom_message_font,R.attr.queue_cusom_message_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Queue_custom_message_font,R.attr.queue_cusom_message_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Queue_order_received_font,R.attr.queue_order_status_received_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Queue_order_received_font,R.attr.queue_order_status_received_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Queue_order_preparation_font,R.attr.queue_order_status_preparation_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Queue_order_preparation_font,R.attr.queue_order_status_preparation_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Queue_order_ready_font,R.attr.queue_order_status_ready_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Queue_order_ready_font,R.attr.queue_order_status_ready_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Queue_order_pickup_font, R.attr.queue_order_status_pickup_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Queue_order_pickup_font,R.attr.queue_order_status_pickup_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Queue_view_bg,R.attr.queue_view_bg),

            new ThemeItem(KDSSettings.ID.Queue_separator_color,R.attr.queue_separator_color),

            new ThemeItem(KDSSettings.ID.Screen_subtitle_font,R.attr.subtitle_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Screen_subtitle_font,R.attr.subtitle_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.LineItems_font,R.attr.lineitems_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.LineItems_font,R.attr.lineitems_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.LineItems_caption_font,R.attr.lineitems_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.LineItems_caption_font,R.attr.lineitems_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.Touch_fontface,R.attr.touch_button_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Touch_fontface,R.attr.touch_button_fg, THEME_FONT_FG),

            new ThemeItem(KDSSettings.ID.LineItems_view_bg,R.attr.lineitems_viewer_bg),

            new ThemeItem(KDSSettings.ID.Sum_font,R.attr.sum_bg, THEME_FONT_BG),
            new ThemeItem(KDSSettings.ID.Sum_font,R.attr.sum_fg, THEME_FONT_FG),

    };

}
