package com.bematechus.kds;

public class KDSSummaryCondiment {
    int mQty = 1;
    String mDescription = "";

    public KDSSummaryCondiment(int nQty, String description)
    {
        mQty = nQty;
        mDescription = description;
    }
    public String getDescription()
    {
        return mDescription;
    }
    public int getQty()
    {
        return mQty;
    }

    public boolean isEqual(KDSSummaryCondiment c)
    {
        if (c == null) return false;
        if (mQty != c.getQty()) return false;
        if (!mDescription.equals(c.getDescription())) return false;
        return true;

    }
}
