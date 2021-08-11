package com.bematechus.kds;

import com.bematechus.kdslib.KDSUIBGFGPickerDialog;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

public class SumStationFilterEntry extends SumStationEntry {

    int mBG = 0;
    int mFG = 0;
    public void setBG(int nColor)
    {
        mBG = nColor;
    }

    public int getBG()
    {
        return mBG;
    }

    public void setFG(int nColor)
    {
        mFG = nColor;
    }

    public int getFG()
    {
        return mFG;
    }

    @Override
    public String toString()
    {
        return getDescription() + "--> " + getDisplayText();
    }

    //static private String SEPARATOR = "\f";
    public String toPrefString()
    {
        String s = getDescription();
        s += SEPARATOR;
        s += getDisplayText();
        s += SEPARATOR;
        s += KDSUtil.convertIntToString(mBG);
        s += SEPARATOR;
        s += KDSUtil.convertIntToString(mFG);

       // s += SEPARATOR;
       // s += KDSUtil.convertIntToString(getAlertValue());
        return s;
    }

    static public SumStationFilterEntry parsePrefString(String s)
    {
        ArrayList<String> ar = KDSUtil.spliteString(s, SEPARATOR);
        if (ar.size() <2) return null;
        SumStationFilterEntry entry = new SumStationFilterEntry();
        entry.setDescription(ar.get(0));
        entry.setDisplayText(ar.get(1));
        if (ar.size()>1)
        {
            entry.setBG(KDSUtil.convertStringToInt(ar.get(2), 0));
        }
        if (ar.size()>2)
        {
            entry.setFG(KDSUtil.convertStringToInt(ar.get(3), 0));
        }
        return entry;

    }

    public SumStationFilterEntry clone()
    {
        SumStationFilterEntry entry = new SumStationFilterEntry();
        entry.setBG(this.getBG());
        entry.setFG(this.getFG());
        entry.setDescription(this.getDescription());
        entry.setDisplayText(this.getDisplayText());
        entry.setEntryType(this.getEntryType());
        return entry;
    }
    public boolean isColorValid()
    {
        if (getBG() == getFG())
        {
            if (getBG() == 0)
                return false;
        }
        return true;
    }
}
