package com.bematechus.kds;

import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;
import java.util.Date;

public class SumStationAlertEntry extends SumStationEntry {

    int mAlertQty = -1;
    String mAlertTime = "";
    String mAlertMessage = "";
    String mAlertImageFile = "";

    /////////////////////
    //
    boolean mQtyAlertDone = false; //for mem variable. don't save it.
    boolean mTimeAlertDone = false; //for mem variable. don't save it.
    //kp-54
    String mGuid = KDSUtil.createNewGUID(); //for save state;
    Date mDtTimeAlertFired = new Date();
    Date mDtQtyAlertFired = new Date();
    //KP-58
    //mem variable. After alert, this is the reserved_qty = total-alert_qty
    //for next alert.
    float mLastAlertedQty = 0;
    boolean mAlertQtyChanged = false; //mem variable. If alert qty changed, mReservedQtyAfterAlert will been reset.
    ///////////
    boolean mDirty = false;

    public void setAlertQtyChanged(boolean bChanged)
    {
        mAlertQtyChanged = bChanged;
    }

    public boolean getAlertQtyChanged()
    {
        return mAlertQtyChanged;
    }

    public void setLastAlertQty(float nqty)
    {
        mLastAlertedQty = nqty;
    }

    public float getLastAlertQty()
    {
        return mLastAlertedQty;
    }

    public void setQtyAlertDone(boolean bDone)
    {
        mQtyAlertDone = bDone;
    }
    public boolean getQtyAlertDone()
    {
        return mQtyAlertDone;
    }

    public void setTimeAlertDone(boolean bDone)
    {
        mTimeAlertDone = bDone;
    }
    public boolean getTimeAlertDone()
    {
        return mTimeAlertDone;
    }

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
        s += SEPARATOR;
        s += getGuid();
        s += SEPARATOR;
        s += KDSUtil.convertIntToString(getEntryType().ordinal());
        return s;
    }

    static public SumStationAlertEntry parsePrefString(String s)
    {
        ArrayList<String> ar = KDSUtil.spliteString(s, SEPARATOR);
        if (ar.size() <6) return null;
        SumStationAlertEntry entry = new SumStationAlertEntry();
        entry.setDescription(ar.get(0));
        entry.setDisplayText(ar.get(1));
        entry.setAlertQty( KDSUtil.convertStringToInt( ar.get(2), -1));
        entry.setAlertTime(ar.get(3));
        entry.setAlertMessage(ar.get(4));
        entry.setAlertImageFile(ar.get(5));
        if (ar.size() >6)
            entry.setGuid(ar.get(6));
        if (ar.size() >7) {
            int n = KDSUtil.convertStringToInt(ar.get(7), 0);
            entry.setEntryType(EntryType.values()[n]);
        }
        return entry;

    }

    /**
     * Just copy the data, don't copy its guid!!!
     *
     * @param alert
     */
    public void copy(SumStationAlertEntry alert)
    {
        //this.setGuid(alert.getGuid());
        this.setDescription(alert.getDescription());
        this.setDisplayText(alert.getDisplayText());
        this.setAlertQty( alert.getAlertQty());
        this.setAlertTime(alert.getAlertTime());
        this.setAlertMessage(alert.getAlertMessage());
        this.setAlertImageFile(alert.getAlertImageFile());
        this.setEntryType(alert.getEntryType());


    }

    public boolean isEqual(SumStationAlertEntry entry)
    {
        if (this.getDescription().equals(entry.getDescription())
            && this.getDisplayText().equals(entry.getDisplayText())
            && this.getAlertQty() == entry.getAlertQty()
            && this.getAlertTime().equals(entry.getAlertTime())
            && this.getAlertMessage().equals(entry.getAlertMessage())
            && this.getAlertImageFile().equals(entry.getAlertImageFile()))
            return true;
        return false;
    }

    public void setGuid(String guid)
    {
        mGuid = guid;
    }
    public String getGuid()
    {
        return mGuid;
    }

    public void setQtyAlertFiredTime(Date dt)
    {
        mDtQtyAlertFired = dt;
    }

    public Date getQtyAlertFiredTime()
    {
        return mDtQtyAlertFired;
    }

    public void setTimeAlertFiredTime(Date dt)
    {
        mDtTimeAlertFired = dt;
    }

    public Date getTimeAlertFiredTime()
    {
        return mDtTimeAlertFired;
    }

    public String toStateString()
    {
        String s = getGuid();
        s += SEPARATOR;
        s += KDSUtil.convertBoolToString(getQtyAlertDone());
        s += SEPARATOR;
        s += KDSUtil.convertDateToString(getQtyAlertFiredTime());
        s += SEPARATOR;
        s += KDSUtil.convertBoolToString(getTimeAlertDone());
        s += SEPARATOR;
        s +=  KDSUtil.convertDateToString(getTimeAlertFiredTime());;
        s += SEPARATOR; //kp-58
        s += KDSUtil.convertIntToString((int)getLastAlertQty());

        return s;
    }

    static SumStationAlertEntry parseStateString(String s)
    {
        ArrayList<String> ar = KDSUtil.spliteString(s, SEPARATOR);
        if (ar.size() !=6) return null;
        SumStationAlertEntry entry = new SumStationAlertEntry();
        entry.setGuid(ar.get(0));
        entry.setQtyAlertDone(KDSUtil.convertStringToBool(ar.get(1), false));
        entry.setQtyAlertFiredTime(KDSUtil.convertStringToDate( ar.get(2), new Date()));
        entry.setTimeAlertDone( KDSUtil.convertStringToBool( ar.get(3), false));
        entry.setTimeAlertFiredTime( KDSUtil.convertStringToDate( ar.get(4), new Date()));
        entry.setLastAlertQty(KDSUtil.convertStringToInt(ar.get(5), 0));


        return entry;
    }

    public String getLastAlertQtyDescription()
    {
        switch (getEntryType())
        {

            case Item:
                return "item:" + this.getDescription();

            case Condiment:
                return "cond:" + this.getDescription();

        }
        return "";
    }

    public void setDirty(boolean bDirty)
    {
        mDirty = bDirty;
    }

    public boolean isDirty()
    {
        return mDirty;
    }

}