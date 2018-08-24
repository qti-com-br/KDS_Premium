package com.bematechus.kds;

import com.bematechus.kdslib.KDSUtil;

/**
 * Created by Administrator on 2017/3/30.
 */
public class TTOrder {
    String m_name;// "name": "102",
    String m_uuid;//        "uuid": "f8853c42-3c10-4636-81e3-a3aeb65d9219",
    String m_orderType;//"orderType": "ON_PREMISE",
    String m_locationName;//        "locationName": "",
    String m_state;//        "state": "started",
    String m_created;//        "created": "2016-09-22T19:08:52",
    String m_statueChanged;//        "stateChanged": "2016-09-22T19:08:52",
    boolean m_paged;//        "paged": false,
    int m_elapseTime;//        "elapsedTime": 0
    String m_notificationType = "";
    public TTOrder()
    {
        reset();
    }
    public void reset()
    {
        m_name = "";// "name": "102",
        m_uuid = "";//        "uuid": "f8853c42-3c10-4636-81e3-a3aeb65d9219",
        m_orderType = "";//"orderType": "ON_PREMISE",
        m_locationName = "";//        "locationName": "",
        m_state = "";//        "state": "started",
        m_created = "";//        "created": "2016-09-22T19:08:52",
        m_statueChanged = "";//        "stateChanged": "2016-09-22T19:08:52",
        m_paged = false;//        "paged": false,
        m_elapseTime = 0;//        "elapsedTime": 0
        m_notificationType = "";
    }

    public boolean isValid()
    {
        return (!m_name.isEmpty());
    }

    public String toString()
    {
        String s = m_name +"," + m_uuid+","+m_orderType + ","+ m_locationName+","+ m_state;
        s += ","+ m_created;//        "created": "2016-09-22T19:08:52",
        s += ","+ m_statueChanged;//        "stateChanged": "2016-09-22T19:08:52",
        s += ","+ KDSUtil.convertBoolToString( m_paged);//        "paged": false,
        s += ","+ KDSUtil.convertIntToString( m_elapseTime);//        "elapsedTime": 0
        s +="," + m_notificationType;
        return s;
    }

    public boolean isStartedStatus()
    {
        String s = m_state;
        s = s.toUpperCase();
        return (s.equals("STARTED"));
    }
    public boolean isLocatedStatus()
    {
        String s = m_state;
        s = s.toUpperCase();
        return (s.equals("LOCATED"));
    }
}
