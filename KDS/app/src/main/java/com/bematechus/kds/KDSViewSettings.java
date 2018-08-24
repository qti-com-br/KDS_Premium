package com.bematechus.kds;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;

/**
 *
 * @author David.Wong
 * record panel current environment variables
 * e.g: default color, font ...
 * 
 */
public class KDSViewSettings {


    KDSSettings m_settings = null;//from KDS
    KDSSettingsState m_currentViewerState = new KDSSettingsState();
    KDSView m_viewer = null;
    private KDSUser.USER m_forUser = KDSUser.USER.USER_A;

    public void setForUser(KDSUser.USER user)
    {
        m_forUser = user;
    }
    public KDSUser.USER getForUser()
    {
        return m_forUser;
    }
    public KDSViewSettings(KDSView v)
    {
        m_viewer = v;
        m_settings = new KDSSettings(v.getContext()); //don't make the settings null. it will replaced by KDS class settings variable
    }

    public void setSettings(KDSSettings settings)
    {
        m_settings = settings;
    }

    public KDSSettingsState getStateValues()
    {
        return m_currentViewerState;
    }

    public KDSSettings getSettings()
    {
        return m_settings;
    }

    public KDSSettings.LayoutFormat getSettingLayoutFormat()
    {
        KDSSettings.LayoutFormat layoutFormat = this.getSettings().getLayoutFormat(KDSSettings.ID.Panels_Layout_Format);

        if ( this.getForUser() == KDSUser.USER.USER_B) {
            layoutFormat = this.getSettings().getLayoutFormat(KDSSettings.ID.Screen1_Panels_Layout_Format);

        }
        return layoutFormat;
    }
    public int getSettingsRows()
    {


        int rows = this.getSettings().getInt(KDSSettings.ID.Panels_Blocks_Rows);
        if (this.getForUser()  == KDSUser.USER.USER_B) {


            rows = this.getSettings().getInt(KDSSettings.ID.Screen1_Panels_Blocks_Rows);
        }
        return rows;
    }

    public int getSettingsCols()
    {

        int cols = this.getSettings().getInt(KDSSettings.ID.Panels_Blocks_Cols);

        if (this.getForUser()  == KDSUser.USER.USER_B) {

            cols = this.getSettings().getInt(KDSSettings.ID.Screen1_Panels_Blocks_Cols);

        }
        return cols;
    }

}
