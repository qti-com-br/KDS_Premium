package com.bematechus.kdslib;

import java.util.ArrayList;

/**
 * Created by David.Wong on 2018/8/21.
 * Rev:
 */
public class KDSDataCategoryIndicator extends KDSDataItem {

    String m_strCategoryDescription = "";
    ArrayList<KDSDataItem> m_arItems = new ArrayList<>();//use it to save same category items.
    int m_nGroupPriority = -1;


    public void setPriority(int nPriority)
    {
        if (nPriority!=-1)
            m_nGroupPriority = nPriority;
    }

    public int getPriority()
    {
        return m_nGroupPriority;
    }
    public void setCategoryDescription(String str)
    {
        m_strCategoryDescription = str;
    }

    public String getCategoryDescription()
    {
        return m_strCategoryDescription;
    }

    public ArrayList<KDSDataItem> getItems()
    {
        return m_arItems;
    }

    static public String makeDisplayString(KDSDataCategoryIndicator c)
    {
        String strDescription = c.getCategoryDescription();
        strDescription = " -- " + strDescription + " --";
        return strDescription;
    }
}
