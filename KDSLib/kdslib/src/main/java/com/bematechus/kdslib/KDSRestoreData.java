package com.bematechus.kdslib;

/**
 * Created by David.Wong on 2019/6/28.
 *  Use it to sort all restoring data, we must keep the sequence correct when this station send data to other (just recover from offline).
 * Rev:
 */
public class KDSRestoreData {
    protected long m_timeStamp = 0;

    public void setTimeStamp(long dt)
    {
        m_timeStamp = dt;
    }
    public long getTimeStamp()
    {
        return m_timeStamp;
    }


}
