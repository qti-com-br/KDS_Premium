package com.bematechus.kdsstatistic;

import android.graphics.Color;

import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXML;

/**
 * Created by David.Wong on 2018/5/15.
 * Rev:
 */
public class SOSRealViewProperties {

    static public final int DEFAULT_TARGET_SECONDS = 180; //3mins

    String m_stationID = "";
    String m_strTitle = "";

    int m_nTargetSeconds = DEFAULT_TARGET_SECONDS;


    final int DEFAULT_BG = 0xff404040;// Color.DKGRAY;
    final int DEFAULT_REAL_BG = 0xffb8cce4;// Color.BLUE;
    final int DEFAULT_PERCENT_FG = 0xfff1f11f;
    final int DEFAULT_REAL_FG = Color.BLACK;
    final int DEFAULT_COUNT_FG = 0xff00ffa1;

    final int DEFAULT_BORDER_BG = Color.BLACK;
    final int DEFAULT_BORDER_FG =  Color.WHITE;

    final int DEFAULT_ALERT_BG = Color.RED;
    final int DEFAULT_ALERT_FG = Color.BLACK;



    KDSBGFG m_borderColor = new KDSBGFG(DEFAULT_BORDER_BG, DEFAULT_BORDER_FG);
    KDSBGFG m_percentColor = new KDSBGFG(DEFAULT_BG, DEFAULT_PERCENT_FG);
    KDSBGFG m_realColor = new KDSBGFG(DEFAULT_REAL_BG, DEFAULT_REAL_FG);

    KDSBGFG m_alertColor = new KDSBGFG(DEFAULT_ALERT_BG, DEFAULT_ALERT_FG);

    KDSBGFG m_countColor = new KDSBGFG(DEFAULT_BG, DEFAULT_COUNT_FG);



//    public void copyFrom(SOSRealViewProperties p)
//    {
//        m_stationID = p.m_stationID;
//        m_strTitle = p.m_strTitle;
//        m_borderColor.copyFrom(p.m_percentColor);
//        m_percentColor.copyFrom(p.m_realColor);
//        m_realColor.copyFrom(p.m_countColor);
//        m_countColor.copyFrom(p.m_countColor);
//
//    }

    public void outputToXml(KDSXML xml)
    {
        xml.setAttribute("StationID", m_stationID);
        xml.setAttribute("Title", m_strTitle);

        xml.setAttribute("TargetSec",KDSUtil.convertIntToString( m_nTargetSeconds));


        xml.setAttribute("BorderBG", KDSUtil.convertIntToString( m_borderColor.getBG()));
        xml.setAttribute("BorderFG", KDSUtil.convertIntToString( m_borderColor.getFG()));

        xml.setAttribute("PercentBG", KDSUtil.convertIntToString( m_percentColor.getBG()));
        xml.setAttribute("PercentFG",KDSUtil.convertIntToString( m_percentColor.getFG()));

        xml.setAttribute("RealBG",KDSUtil.convertIntToString( m_realColor.getBG()));
        xml.setAttribute("RealFG",KDSUtil.convertIntToString( m_realColor.getFG()));

        xml.setAttribute("AlertBG",KDSUtil.convertIntToString( m_alertColor.getBG()));
        xml.setAttribute("AlertFG",KDSUtil.convertIntToString( m_alertColor.getFG()));

        xml.setAttribute("CountBG",KDSUtil.convertIntToString( m_countColor.getBG()));
        xml.setAttribute("CountFG", KDSUtil.convertIntToString( m_countColor.getFG()));




    }
    public void parseXml(KDSXML xml)
    {
        String s = "";
        m_stationID = xml.getAttribute("StationID", "");
        m_strTitle = xml.getAttribute("Title", "");

        s = xml.getAttribute("TargetSec", KDSUtil.convertIntToString(DEFAULT_TARGET_SECONDS));
        m_nTargetSeconds = KDSUtil.convertStringToInt(s, DEFAULT_TARGET_SECONDS);//

        s = xml.getAttribute("BorderBG", KDSUtil.convertIntToString(DEFAULT_BORDER_BG ));
        m_borderColor.setBG( KDSUtil.convertStringToInt(s, DEFAULT_BORDER_BG) );

        s = xml.getAttribute("BorderFG", KDSUtil.convertIntToString(DEFAULT_BORDER_FG ));
        m_borderColor.setFG( KDSUtil.convertStringToInt(s, DEFAULT_BORDER_FG) );



        s = xml.getAttribute("PercentBG", KDSUtil.convertIntToString(DEFAULT_BG ));
        m_percentColor.setBG( KDSUtil.convertStringToInt(s, DEFAULT_BG) );

        s = xml.getAttribute("PercentFG", KDSUtil.convertIntToString(DEFAULT_PERCENT_FG ));
        m_percentColor.setFG( KDSUtil.convertStringToInt(s, DEFAULT_PERCENT_FG) );


        s = xml.getAttribute("RealBG", KDSUtil.convertIntToString(DEFAULT_REAL_BG ));
        m_realColor.setBG( KDSUtil.convertStringToInt(s, DEFAULT_REAL_BG) );

        s = xml.getAttribute("RealFG", KDSUtil.convertIntToString(DEFAULT_REAL_FG ));
        m_realColor.setFG( KDSUtil.convertStringToInt(s, DEFAULT_REAL_FG) );


        s = xml.getAttribute("AlertBG", KDSUtil.convertIntToString(DEFAULT_ALERT_BG ));
        m_alertColor.setBG( KDSUtil.convertStringToInt(s, DEFAULT_ALERT_BG) );

        s = xml.getAttribute("AlertFG", KDSUtil.convertIntToString(DEFAULT_ALERT_FG ));
        m_alertColor.setFG( KDSUtil.convertStringToInt(s, DEFAULT_ALERT_FG) );

        s = xml.getAttribute("CountBG", KDSUtil.convertIntToString(DEFAULT_BG ));
        m_countColor.setBG( KDSUtil.convertStringToInt(s, DEFAULT_BG) );

        s = xml.getAttribute("CountFG", KDSUtil.convertIntToString(DEFAULT_COUNT_FG ));
        m_countColor.setFG( KDSUtil.convertStringToInt(s, DEFAULT_COUNT_FG) );

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
