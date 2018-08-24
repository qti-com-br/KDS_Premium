package com.bematechus.kds;

import android.graphics.drawable.Drawable;

/**
 * Record current KDS focus state.
 */
public class KDSSettingsState {
    private String m_strFocusedOrderGUID = "";
    private String m_strFocusedItemGUID = "";
    private String m_strFirstOrderGUID = "";

    private String m_strFirstItemGUID = ""; //for line items display mode


    public void setFirstItemGuid(String guid)
    {
        m_strFirstItemGUID = guid;
    }
    public String getFirstItemGuid()
    {
        return m_strFirstItemGUID;
    }
    public void setFocusedOrderGUID(String guid)
    {
       // if (!m_strFirstOrderGUID.equals(guid))
        if (!m_strFocusedOrderGUID.equals(guid))
            m_strFocusedItemGUID = "";
        m_strFocusedOrderGUID = guid;
    }
    public String getFocusedOrderGUID()
    {
        return m_strFocusedOrderGUID;
    }

    public void setFocusedItemGUID(String guid)
    {
        m_strFocusedItemGUID = guid;
    }
    public String getFocusedItemGUID()
    {
        return m_strFocusedItemGUID;
    }

    public void setFirstShowingOrderGUID(String guid)
    {
        m_strFirstOrderGUID = guid;
    }
    public String getFirstShowingOrderGUID()
    {
        return m_strFirstOrderGUID;
    }
    public boolean isFocusedOrderGUID(String guid)
    {
        if (m_strFocusedOrderGUID.isEmpty() ||
                guid.isEmpty())
            return false;
        return m_strFocusedOrderGUID.equals(guid);
    }
    public boolean isFocusedItemGUID(String guid)
    {
        if (m_strFocusedItemGUID.isEmpty() ||
                guid.isEmpty())
            return false;
        return m_strFocusedItemGUID.equals(guid);
    }



}
