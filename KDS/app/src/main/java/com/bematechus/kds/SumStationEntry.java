package com.bematechus.kds;

import com.bematechus.kdslib.KDSUtil;

import java.nio.file.attribute.AclEntryType;
import java.util.ArrayList;

/**
 *
 * For summary station feature.
 */
public class SumStationEntry {

    static public final String SUM_STATION_SEPARATOR = "\n";
    public enum  EntryType
    {
        Item,
        Condiment
    }

    EntryType mType = EntryType.Item;
    String mDescription = "";
    String mDisplayText = "";
    //int mAlertValue = -1;

    public void setEntryType(EntryType type)
    {
        mType = type;
    }

    public EntryType getEntryType()
    {
        return mType;
    }
    public String getDescription()
    {
        return mDescription;
    }
    public void setDescription(String s)
    {
        mDescription = s;
    }

    public String getDisplayText()
    {
        return mDisplayText;
    }
    public void setDisplayText(String s)
    {
        mDisplayText= s;
    }

//    public int getAlertValue()
//    {
//        return mAlertValue;
//    }
//    public void setAlertValue(int n)
//    {
//        mAlertValue = n;
//
//    }

    @Override
    public String toString()
    {
        return getDescription();
    }

    static protected String SEPARATOR = "\f";
    public String toPrefString()
    {
        return null;
//        String s = getDescription();
//        s += SEPARATOR;
//        s += getDisplayText();
//        s += SEPARATOR;
//        s += KDSUtil.convertIntToString(getAlertValue());
//        return s;
    }

//    static public SumStationEntry parsePrefString(String s)
//    {
//        return null;
////        ArrayList<String> ar = KDSUtil.spliteString(s, SEPARATOR);
////        if (ar.size() !=3) return null;
////        SumStationEntry entry = new SumStationEntry();
////        entry.setDescription(ar.get(0));
////        entry.setDisplayText(ar.get(1));
////        entry.setAlertValue( KDSUtil.convertStringToInt( ar.get(2), -1));
////        return entry;
//
//    }
}
