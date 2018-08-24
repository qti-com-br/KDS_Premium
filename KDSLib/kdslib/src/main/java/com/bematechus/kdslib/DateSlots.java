package com.bematechus.kdslib;

import java.util.ArrayList;
import java.util.Date;

/**
 * SAME AS KDS FILE
 */

/**
 * For statistic report.
 */
public class DateSlots {

    String m_strDateStart = "";

    ArrayList<String> m_arDateFrom = new ArrayList<>();
    ArrayList<String> m_arDateTo = new ArrayList<>();

    public void setDateStart(String dt)
    {
        m_strDateStart = dt;
    }
    public void setDateStart(Date dt)
    {
        m_strDateStart = KDSUtil.convertDateToString(dt);
    }
    public String getDateStart()
    {
        return m_strDateStart;
    }
    public void add(String dtFrom, String dtTo)
    {
        m_arDateFrom.add(dtFrom);
        m_arDateTo.add(dtTo);
    }
    public int getSize()
    {
        return m_arDateFrom.size();
    }

    public String getDateFrom(int nIndex)
    {
        return m_arDateFrom.get(nIndex);
    }

    public String getDateTo(int nIndex)
    {
        return m_arDateTo.get(nIndex);
    }
    public void clear()
    {
        m_arDateFrom.clear();
        m_arDateTo.clear();
    }
}
