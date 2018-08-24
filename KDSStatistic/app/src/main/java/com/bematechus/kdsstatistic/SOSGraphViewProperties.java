package com.bematechus.kdsstatistic;

import android.graphics.Color;

import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXML;

/**
 * Created by David.Wong on 2018/5/16.
 * Rev:
 */
public class SOSGraphViewProperties {

    String m_stationID = "";
    String m_strTitle = "";

    int m_nTargetSeconds = 0;

    String m_strTitleX = "";
    String m_strTitleY = "";

    final int DEFAULT_BG = Color.WHITE;
    final int DEFAULT_FG = Color.BLACK;

    final int DEFAULT_Y_BG =Color.WHITE;// 0xffb8cce4;// Color.BLUE;
    final int DEFAULT_Y_FG =Color.BLACK;// 0xfff1f11f;

    KDSBGFG m_defaultColor = new KDSBGFG(DEFAULT_BG, DEFAULT_FG);
    KDSBGFG m_yColor = new KDSBGFG(DEFAULT_Y_BG, DEFAULT_Y_FG);

//    public void copyFrom(SOSGraphViewProperties p)
//    {
//        m_stationID = p.m_stationID;
//        m_strTitle = p.m_strTitle;
//        m_defaultColor.copyFrom(p.m_defaultColor);
//        m_yColor.copyFrom(p.m_yColor);
//
//
//    }

    public void outputToXml(KDSXML xml)
    {
        xml.setAttribute("StationID", m_stationID);
        xml.setAttribute("Title", m_strTitle);

        xml.setAttribute("TargetSec",KDSUtil.convertIntToString( m_nTargetSeconds));

        xml.setAttribute("TitleX", m_strTitleX);
        xml.setAttribute("TitleY", m_strTitleY);

        xml.setAttribute("DefaultBG", KDSUtil.convertIntToString( m_defaultColor.getBG()));
        xml.setAttribute("DefaultFG", KDSUtil.convertIntToString( m_defaultColor.getFG()));

        xml.setAttribute("YBG", KDSUtil.convertIntToString( m_yColor.getBG()));
        xml.setAttribute("YFG",KDSUtil.convertIntToString( m_yColor.getFG()));


    }
    public void parseXml(KDSXML xml)
    {
        m_stationID = xml.getAttribute("StationID", "");
        m_strTitle = xml.getAttribute("Title", "");

        String s = "";

        s = xml.getAttribute("TargetSec", "0");
        m_nTargetSeconds = KDSUtil.convertStringToInt(s, 0);//

        m_strTitleX = xml.getAttribute("TitleX","" );
        m_strTitleY = xml.getAttribute("TitleY", "");

        s = xml.getAttribute("DefaultBG", KDSUtil.convertIntToString(DEFAULT_BG ));
        m_defaultColor.setBG( KDSUtil.convertStringToInt(s, DEFAULT_BG) );

        s = xml.getAttribute("DefaultFG", KDSUtil.convertIntToString(DEFAULT_FG ));
        m_defaultColor.setFG( KDSUtil.convertStringToInt(s, DEFAULT_FG) );

        s = xml.getAttribute("YBG", KDSUtil.convertIntToString(DEFAULT_Y_BG ));
        m_yColor.setBG( KDSUtil.convertStringToInt(s, DEFAULT_Y_BG) );

        s = xml.getAttribute("YFG", KDSUtil.convertIntToString(DEFAULT_Y_FG ));
        m_yColor.setFG( KDSUtil.convertStringToInt(s, DEFAULT_Y_FG) );


    }

    public void setTargetSeconds(int nSeconds)
    {
        m_nTargetSeconds = nSeconds;
    }
    public int getTargetSeconds()
    {
        return m_nTargetSeconds;
    }
    public float getTargetMinutes()
    {
        return (float) getTargetSeconds()/60;
    }

}
