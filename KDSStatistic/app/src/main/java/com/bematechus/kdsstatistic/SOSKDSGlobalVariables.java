package com.bematechus.kdsstatistic;

import android.content.Context;

/**
 * Created by Administrator on 2015/9/20 0020.
 */
public class SOSKDSGlobalVariables {
    static private SOSKDSSOS m_kdsSOS = null;


    static public void createSOSKDS(Context contextApp)
    {
        SOSKDSGlobalVariables.m_kdsSOS = new SOSKDSSOS(contextApp);

    }

    static public SOSKDSSOS getKDSSOS()
    {
        return m_kdsSOS;
    }
    static public void setKDSSOS(SOSKDSSOS kdsSOS)
    {
        m_kdsSOS = kdsSOS;
    }

    static public SOSKDSSOS getKDS()
    {
        return m_kdsSOS;
    }




}
