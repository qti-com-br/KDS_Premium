/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bematechus.kdslib;

import android.util.Log;

import java.util.Date;

/**
 *
 * @author David.Wong
 */
public class TimeDog {
    private Date m_dtStart = new Date();
    public TimeDog()
    {
        m_dtStart.setTime(System.currentTimeMillis());// = new Date();
    }
    public TimeDog(Date dtStart)
    {
        m_dtStart = dtStart;
    }
    public boolean is_timeout(int nms)
    {
        Date dtNow = new Date(System.currentTimeMillis());
        long msNow = dtNow.getTime();
        long msStart = m_dtStart.getTime();
        if (msNow - msStart >nms)
            return true;
        return false;
    }
    public void reset()
    {
        //m_dtStart = null;
        m_dtStart.setTime(System.currentTimeMillis());
        //m_dtStart = new Date(System.currentTimeMillis());
    }
    public void reset(Date dtStart)
    {
        //m_dtStart = null;
        m_dtStart = dtStart;
    }
    public void debug_print_Duration(String strTitle)
    {
        Date dtNow = new Date(System.currentTimeMillis());
        long msNow = dtNow.getTime();
        long msStart = m_dtStart.getTime();
        long ms = msNow - msStart ;
        //System.out.println(strTitle + " Duration="+KDSUtil.convertIntToString(ms)+"ms");
        KDSLog.d(strTitle, KDSLog._FUNCLINE_()+" Duration="+KDSUtil.convertIntToString(ms)+"ms");
    }
    public String startTimeString()
    {
        long msStart = m_dtStart.getTime();
        return KDSUtil.convertIntToString(msStart);
    }

    public int currentTimeOutSeconds()
    {
        Date dtNow = new Date(System.currentTimeMillis());
        long msNow = dtNow.getTime();
        long msStart = m_dtStart.getTime();
        long l = (msNow - msStart);
        l = l/1000;
        return (int)l;

    }
}
