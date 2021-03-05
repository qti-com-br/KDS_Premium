package com.bematechus.kds;

import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

public class SumStationAlertEntry extends SumStationEntry {

    int mAlertQty = -1;
    String mAlertTime = "";
    String mAlertMessage = "";
    String mAlertImageFile = "";

    public int getAlertQty()
    {
        return mAlertQty;
    }
    public void setAlertQty(int n)
    {
        mAlertQty = n;

    }
    public String getAlertTime()
    {
        return mAlertTime;
    }
    public void setAlertTime(String tm)
    {
        mAlertTime = tm;

    }

    public String getAlertMessage()
    {
        return mAlertMessage;
    }

    public void setAlertMessage(String msg)
    {
        mAlertMessage = msg;
    }

    public String getAlertImageFile()
    {
        return mAlertImageFile;
    }

    public void setAlertImageFile(String filePath)
    {
        mAlertImageFile = filePath;
    }

    @Override
    public String toString()
    {
        return getDescription();
    }

    static protected String SEPARATOR = "\f";
    public String toPrefString()
    {
        String s = getDescription();
        s += SEPARATOR;
        s += getDisplayText();
        s += SEPARATOR;
        s += KDSUtil.convertIntToString(getAlertQty());
        s += SEPARATOR;
        s += getAlertTime();
        s += SEPARATOR;
        s += getAlertMessage();
        s += SEPARATOR;
        s += getAlertImageFile();
        return s;
    }

    static public SumStationAlertEntry parsePrefString(String s)
    {
        ArrayList<String> ar = KDSUtil.spliteString(s, SEPARATOR);
        if (ar.size() !=6) return null;
        SumStationAlertEntry entry = new SumStationAlertEntry();
        entry.setDescription(ar.get(0));
        entry.setDisplayText(ar.get(1));
        entry.setAlertQty( KDSUtil.convertStringToInt( ar.get(2), -1));
        entry.setAlertTime(ar.get(3));
        entry.setAlertMessage(ar.get(4));
        entry.setAlertImageFile(ar.get(5));
        return entry;

    }

    public void copy(SumStationAlertEntry alert)
    {
        this.setDescription(alert.getDescription());
        this.setDisplayText(alert.getDisplayText());
        this.setAlertQty( alert.getAlertQty());
        this.setAlertTime(alert.getAlertTime());
        this.setAlertMessage(alert.getAlertMessage());
        this.setAlertImageFile(alert.getAlertImageFile());

    }
}