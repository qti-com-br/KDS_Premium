package com.bematechus.kds;

import java.util.ArrayList;

/**
 * Summary station will group some summary items to one group
 */
public class KDSViewSumStnSumGroup {
    int mBG = 0;
    int mFG = 0;

    ArrayList<KDSSummaryItem> mItems = new ArrayList<>();

    public ArrayList<KDSSummaryItem> items()
    {
        return mItems;
    }

    public int getBG()
    {
        return mBG;
    }
    public void setBG(int nColor)
    {
        mBG = nColor;
    }

    public int getFG()
    {
        return mFG;
    }
    public void setFG(int nColor)
    {
        mFG = nColor;
    }

}
