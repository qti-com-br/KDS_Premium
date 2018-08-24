package com.bematechus.kdslib;

/**
 *
 * This item represent a item that will been consolidate to others.
 */
public class KDSDataConsolidatedItem {

    String m_strGuid;
    float m_fltQty;
    public KDSDataConsolidatedItem(String guid, float qty)
    {
        m_strGuid = guid;
        m_fltQty = qty;
    }
    public String getGuid()
    {
        return m_strGuid;
    }
    public float getQty()
    {
        return m_fltQty;
    }
}
