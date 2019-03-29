package com.bematechus.kds;

import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/**
 * Created by Administrator on 2017/5/12.
 */
public class PagerManager implements Runnable {

    ArrayList<PagerCall> m_arWaitingPage = new ArrayList<>();
    KDSPager m_pagerDevice = new KDSPager();

    Object m_locker = new Object();

    public void addPagerID(String pagerID)
    {
        if (pagerID.isEmpty()) return;

        if (!getSettings().getBoolean(KDSSettings.ID.Pager_enabled))
            return;
        if (isExisted(pagerID)) return;
        PagerCall p = new PagerCall(pagerID);
        synchronized (m_locker) {
            m_arWaitingPage.add(p);
        }
    }

    public boolean isExisted(String pagerID)
    {
        synchronized (m_locker) {
           for (int i=0; i< m_arWaitingPage.size(); i++)
           {
               if (m_arWaitingPage.get(i).equals(pagerID))
                   return true;
           }
        }
        return false;
    }
//    public PagerCall popPagerID()
//    {
//        synchronized (m_locker) {
//            if (m_arWaitingPage.size() < 0)
//                return null;
//            PagerCall p = m_arWaitingPage.get(0);
//            m_arWaitingPage.remove(0);
//            return p;
//
//        }
//    }

    boolean m_bIsRunning = false;
    public void onTime()
    {
        if (m_bIsRunning) return;
        Thread t =  new Thread(this, "Pager");
        t.start();
        //doPage();
    }
    private void doPage()
    {
        if (!getSettings().getBoolean(KDSSettings.ID.Pager_enabled))
            return;
        if (m_arWaitingPage.size() <=0) return;
        int nTimeoutSeconds = getSettings().getInt(KDSSettings.ID.Pager_delay);

        ArrayList<PagerCall> arFired = new ArrayList<>();

        for (int i=0; i< m_arWaitingPage.size(); i++)
        {
            if (m_arWaitingPage.get(i).isTimeout(nTimeoutSeconds)) {
                if (m_arWaitingPage.get(i).callPage(m_pagerDevice) ||
                        m_arWaitingPage.get(i).isTimeToRemove() )
                    arFired.add(m_arWaitingPage.get(i));
            }
        }

        for (int i=0; i<arFired.size(); i++ )
        {
            m_arWaitingPage.remove(arFired.get(i));
        }
        arFired.clear();
    }

    private KDSSettings getSettings()
    {
        return KDSGlobalVariables.getKDS().getSettings();
    }

    public void run()
    {
        m_bIsRunning = true;
        doPage();
        m_bIsRunning = false;
    }


    class PagerCall
    {
        String m_pagerID = "";
        Date m_dtStart = new Date();
        public PagerCall(String pageID)
        {
            m_pagerID = pageID;
        }
        public boolean isTimeout(int timeoutSeconds)
        {
            TimeDog td = new TimeDog(m_dtStart);
            return (td.is_timeout(timeoutSeconds * 1000));

        }
        final int REMOVE_TIMEOUT = 600000;//10 minutes
        public boolean isTimeToRemove()
        {
            TimeDog td = new TimeDog(m_dtStart);
            return (td.is_timeout(REMOVE_TIMEOUT));

        }
        public boolean callPage(KDSPager pagerDev)
        {
            return pagerDev.page(m_pagerID);
        }
    }
}
