package com.bematechus.kdslib;

/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */
/**
 * Created by Administrator on 2015/10/30 0030.
 */
public class KDSState {

    boolean m_bRunning = false;
    boolean m_bPrimaryBackupLost = true;

    public boolean getPrimaryOfBackupLost()
    {
        return m_bPrimaryBackupLost;
    }
    public void setPrimaryOfBackupLost(boolean bLost)
    {
        m_bPrimaryBackupLost = bLost;
    }
    public boolean getRunning()
    {
        return m_bRunning;
    }
    public  void setRunning(boolean bRunning)
    {
        m_bRunning = bRunning;
    }
}
