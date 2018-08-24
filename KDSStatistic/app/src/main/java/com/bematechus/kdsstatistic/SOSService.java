package com.bematechus.kdsstatistic;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class SOSService extends Service {

    SOSKDSSOS m_kdsSOS = null;
    private final IBinder binder = new MyBinder();

    public SOSService() {

    }

    @Override
    public IBinder onBind(Intent intent) {

        //throw new UnsupportedOperationException("Not yet implemented");
        return binder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (m_kdsSOS == null)
            m_kdsSOS = new SOSKDSSOS(this.getApplicationContext());
        //if (m_kdsRouter.getSettings().getBoolean(KDSRouterSettings.ID.KDSRouter_Enabled))
            this.start();
        return START_STICKY;
    }

    public boolean start()
    {
        return m_kdsSOS.start();
    }

    public void stop()
    {
        m_kdsSOS.stop();
    }


    public void updateSettings()
    {
        //boolean bEnabledOld = m_kdsRouter.getSettings().getBoolean(KDSRouterSettings.ID.KDSRouter_Enabled);
        m_kdsSOS.updateSettings();
//        boolean bEnabledNew = m_kdsRouter.getSettings().getBoolean(KDSRouterSettings.ID.KDSRouter_Enabled);
//        if (bEnabledNew != bEnabledOld)
//        {
//            if (bEnabledNew)
//            {
        m_kdsSOS.start();
//            }
//            else
//                m_kdsRouter.stop();
//        }

    }

    public void checkStationsSettingChanged()
    {
        m_kdsSOS.checkStationsSettingChanged(this.getApplicationContext());
    }
    public SOSKDSSOS getKDSSOS()
    {
        return  m_kdsSOS;
    }

    public class MyBinder extends Binder
    {
        SOSService getService()
        {
            return SOSService.this;
        }
    }

    public void onTimer()
    {
        m_kdsSOS.on1sTimer();
    }
}
