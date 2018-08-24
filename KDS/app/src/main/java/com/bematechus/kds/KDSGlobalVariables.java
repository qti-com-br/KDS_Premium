package com.bematechus.kds;

import android.content.Context;

import com.bematechus.kdslib.TimeDog;

/**
 * Global variables
 */
public class KDSGlobalVariables {
    static private KDS m_kds = null;
    static private MainActivity m_mainActivity = null;

    static boolean m_bBlinkingOn = false; //for blinking
    static TimeDog m_blinkTimeDog = new TimeDog();

    static public void createKDS(Context contextApp) {

        if (KDSGlobalVariables.m_kds == null)
            KDSGlobalVariables.m_kds = new KDS(contextApp);
        else
            KDSGlobalVariables.m_kds.setContext(contextApp);

    }

    static public void setKDS(KDS kds) {
        m_kds = kds;
    }

    static public void setMainActivity(MainActivity a) {
        KDSGlobalVariables.m_mainActivity = a;
    }

    static public KDS getKDS() {
        return m_kds;
    }

    static public MainActivity getMainActivity() {
        return KDSGlobalVariables.m_mainActivity;
    }

    static boolean getBlinkingStep() {
        return m_bBlinkingOn;
    }

    static void toggleBlinkingStep()
    {
        if (m_blinkTimeDog.is_timeout(1500)) {
            m_bBlinkingOn = (!m_bBlinkingOn);
            m_blinkTimeDog.reset();
        }
    }


}
