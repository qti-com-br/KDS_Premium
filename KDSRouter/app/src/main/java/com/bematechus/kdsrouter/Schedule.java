package com.bematechus.kdsrouter;

import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;

/**
 *
 */
public class Schedule {
    ArrayList<WeekEvent> m_arEvents = new ArrayList<>();
    TimeDog m_dog = new TimeDog();

    static final int TIMEOUT_SECONDS = 30;
    public  boolean isTimeToCheckEvent()
    {
        if (m_dog.is_timeout(TIMEOUT_SECONDS*1000))
        {
            m_dog.reset();
            return true;
        }
        return false;

    }
    public void refresh()
    {
        m_arEvents.clear();
        m_arEvents =  KDSGlobalVariables.getKDS().getRouterDB().scheduleGetAll();

    }
    public void resetTimeoutEvents()
    {
        for (int i=0; i< m_arEvents.size(); i++)
        {
            if (m_arEvents.get(i).isTimeout()) {
                m_arEvents.get(i).resetState();
                KDSGlobalVariables.getKDS().getRouterDB().setScheduleEventFiredTime(m_arEvents.get(i).getGUID(),m_arEvents.get(i).getFiredTime() );
            }
        }
    }
    public ArrayList<String> checkEventToFire()
    {
        resetTimeoutEvents();
        ArrayList<String> ar = new ArrayList<String>();
        for (int i=0; i< m_arEvents.size(); i++)
        {
            boolean bIsTime = m_arEvents.get(i).isTimeToFire();
            boolean bFired = m_arEvents.get(i).isFired();
            if (bIsTime && (!bFired) )
            {
                m_arEvents.get(i).setFired();
                KDSGlobalVariables.getKDS().getRouterDB().setScheduleEventFiredTime(m_arEvents.get(i).getGUID(),m_arEvents.get(i).getFiredTime() );
                ar.add(m_arEvents.get(i).toXmlString());

            }

        }
        return ar;
    }
}
