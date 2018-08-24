package com.bematechus.kdslib;

/**
 * Created by Administrator on 2017/12/7.
 */
public class KDSDataVoidItemIndicator extends KDSDataItem {

    public KDSDataItem m_myParent = null;
    public void setParent(KDSDataItem item)
    {
        m_myParent = item;
    }
    public KDSDataItem getParent()
    {
        return m_myParent;
    }
}
