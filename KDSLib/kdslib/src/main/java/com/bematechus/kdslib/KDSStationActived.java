package com.bematechus.kdslib;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */
/**
 *
 * record the active station information
 */
public class KDSStationActived extends KDSStationIP {
    TimeDog m_timeLastPulse = new TimeDog();
    public void updatePulseTime()
    {
        m_timeLastPulse.reset();
    }

    public boolean isTimeout(int nms)
    {
        return (m_timeLastPulse.is_timeout(nms));
    }

}
