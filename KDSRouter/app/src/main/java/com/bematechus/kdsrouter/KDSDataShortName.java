package com.bematechus.kdsrouter;

import android.graphics.Point;

/**
 * Created by Administrator on 2016/11/19.
 */
public class KDSDataShortName {
    String m_strGuid = "";
    String m_strDescription = "";
    String m_strToStation = "";

    public KDSDataShortName(String guid, String description, String toStation)
    {
        m_strGuid = guid;
        m_strDescription = description;
        m_strToStation = toStation;

    }

    public void setGuid(String guid)
    {
        m_strGuid = guid;
    }
    public String getGuid()
    {
        return m_strGuid;
    }

    public void setToStation(String toStation)
    {
        m_strToStation = toStation;
    }
    public String getToStation()
    {
        return m_strToStation;
    }

    public void setDescription(String strDescription)
    {
        m_strDescription = strDescription;
    }
    public String getDescription()
    {
        return m_strDescription;
    }
    public String toString()
    {
        return getDescription();
    }
}
