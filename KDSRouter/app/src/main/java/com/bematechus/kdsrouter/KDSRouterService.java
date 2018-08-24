package com.bematechus.kdsrouter;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class KDSRouterService extends Service {

    KDSRouter m_kdsRouter = null;
    private final IBinder binder = new MyBinder();

    public KDSRouterService() {

    }

    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (m_kdsRouter == null)
            m_kdsRouter = new KDSRouter(this.getApplicationContext());
        if (m_kdsRouter.getSettings().getBoolean(KDSRouterSettings.ID.KDSRouter_Enabled)) {
            m_kdsRouter.setContext( this.getApplicationContext());
            this.start();
        }
        return START_STICKY;
    }

    public boolean start()
    {
        return m_kdsRouter.start();
    }

    public void stop()
    {
        m_kdsRouter.stop();
    }


    public void updateSettings()
    {
        boolean bEnabledOld = m_kdsRouter.getSettings().getBoolean(KDSRouterSettings.ID.KDSRouter_Enabled);
        m_kdsRouter.updateSettings();
        boolean bEnabledNew = m_kdsRouter.getSettings().getBoolean(KDSRouterSettings.ID.KDSRouter_Enabled);
        if (bEnabledNew != bEnabledOld)
        {
            if (bEnabledNew)
            {
                m_kdsRouter.start();
            }
            else
                m_kdsRouter.stop();
        }
        if (!bEnabledNew)
            m_kdsRouter.stop();
    }

    public void checkStationsSettingChanged()
    {
        m_kdsRouter.checkStationsSettingChanged(this.getApplicationContext());
    }
    public KDSRouter getKDSRouter()
    {
        return  m_kdsRouter;
    }

    public class MyBinder extends Binder
    {
        KDSRouterService getService()
        {
            return KDSRouterService.this;
        }
    }

    public void onTimer()
    {
        m_kdsRouter.on1sTimer();
    }
}
