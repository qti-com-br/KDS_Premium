package com.bematechus.kds;

import com.bematechus.kdslib.TimeDog;

import java.util.Date;

/**
 * Check bump bar double pressed event
 */
public class KDSKbdDoublePress {

    int m_nLastKeyCode = 0;
    Date m_dtLastPressTime = new Date();

    final int DOUBLE_INTERVAL = 500; //ms
    public boolean checkDoublePressed(int nKeyCode) {

        boolean bReturn = false;
        if (m_nLastKeyCode == nKeyCode && m_nLastKeyCode != 0) {
            TimeDog t = new TimeDog((m_dtLastPressTime));
            if (!t.is_timeout(DOUBLE_INTERVAL))
                bReturn = true;

        }
        m_nLastKeyCode = nKeyCode;
        m_dtLastPressTime.setTime(System.currentTimeMillis());
        return bReturn;

    }


}
