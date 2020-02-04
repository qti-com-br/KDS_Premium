package com.bematechus.kds;

import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Vector;

/**
 * Created by Administrator on 2017/5/12.
 */
public class PagerManager implements Runnable {

    Vector<PagerCall> m_arWaitingPage = new Vector<>();
    KDSPager m_pagerDevice = new KDSPager();

    Object m_locker = new Object();
    final int MAX_WAITING_COUNT = 100;

    public void addPagerID(String pagerID)
    {
        if (pagerID.isEmpty()) return;

        if (!getSettings().getBoolean(KDSSettings.ID.Pager_enabled))
            return;
        if (isExisted(pagerID)) return;

        synchronized (m_locker) {
            if (m_arWaitingPage.size() >MAX_WAITING_COUNT)
                return;
            PagerCall p = new PagerCall(pagerID);
            m_arWaitingPage.add(p);
        }
        startThread();
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
    Thread m_thread = null;
    public void onTime()
    {
//        if (!getSettings().getBoolean(KDSSettings.ID.Pager_enabled))
//            return;
//        if (m_bIsRunning) return;


        //doPage();
    }
    public void startThread()
    {
        if (m_thread == null ||
                !m_thread.isAlive()) {
            m_thread = new Thread(this, "Pager");
            m_thread.start();
        }
    }
    final int MAX_BATCH_COUNT = 5;
    private void doPage()
    {
        if (!getSettings().getBoolean(KDSSettings.ID.Pager_enabled))
            return;
        //synchronized (m_locker)
        {
            if (m_arWaitingPage.size() <= 0) return;
            int nTimeoutSeconds = getSettings().getInt(KDSSettings.ID.Pager_delay);

            ArrayList<PagerCall> arFired = new ArrayList<>();
            int nCount = m_arWaitingPage.size();
            if (nCount > MAX_BATCH_COUNT) nCount = MAX_BATCH_COUNT;
            for (int i = 0; i < nCount; i++) {
                if (m_arWaitingPage.get(i).isTimeout(nTimeoutSeconds)) {
                    if (m_arWaitingPage.get(i).callPage(m_pagerDevice) ||
                            m_arWaitingPage.get(i).isTimeToRemove())
                        arFired.add(m_arWaitingPage.get(i));
                }
            }

            m_arWaitingPage.removeAll(arFired);

//        for (int i=0; i<arFired.size(); i++ )
//        {
//            m_arWaitingPage.remove(arFired.get(i));
//        }
            arFired.clear();
            try
            {
                Thread.sleep(10);
            }
            catch (Exception e)
            {

            }
        }
    }

    private KDSSettings getSettings()
    {
        return KDSGlobalVariables.getKDS().getSettings();
    }

    public void run()
    {
        m_bIsRunning = true;
        while (true) {
            if (m_thread != Thread.currentThread())
                break;
            if (m_arWaitingPage.size() <=0)
            {
                try
                {
                    Thread.sleep(1000);
                    continue;
                }
                catch (Exception e)
                {

                }
            }
            doPage();
        };
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
            try {
                return pagerDev.page(m_pagerID);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }
    }
}
