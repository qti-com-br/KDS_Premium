package com.bematechus.kds;

import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

public class SumStationFilterEntry extends SumStationEntry {

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
       // s += SEPARATOR;
       // s += KDSUtil.convertIntToString(getAlertValue());
        return s;
    }

    static public SumStationFilterEntry parsePrefString(String s)
    {
        ArrayList<String> ar = KDSUtil.spliteString(s, SEPARATOR);
        if (ar.size() !=2) return null;
        SumStationFilterEntry entry = new SumStationFilterEntry();
        entry.setDescription(ar.get(0));
        entry.setDisplayText(ar.get(1));

        return entry;

    }
}
