package com.bematechus.kdsrouter;

import com.bematechus.kdslib.KDSStationActived;

/**
 * Used in Router app
 */
public class RouterStation extends KDSStationActived {
    protected boolean m_bEnabled = false;
    protected boolean m_bBackupMode = false;
    public void setEnabled(boolean bEnabled)
    {
        m_bEnabled = bEnabled;
    }
    public boolean getEnabled()
    {
        return m_bEnabled;
    }
    public void setBackupMode(boolean bBackupMode)
    {
        m_bBackupMode = bBackupMode;
    }
    public boolean getBackupMode()
    {
        return m_bBackupMode;
    }
}
