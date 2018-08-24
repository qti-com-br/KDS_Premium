package com.bematechus.kdsstatistic;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class STService extends Service {

    STKDSStatistic m_kdsStatistic = null;
    private final IBinder binder = new MyBinder();

    public STService() {

    }

    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (m_kdsStatistic == null)
            m_kdsStatistic = new STKDSStatistic(this.getApplicationContext());

        this.start();
        return START_STICKY;
    }

    public boolean start()
    {
        return m_kdsStatistic.start();
    }

    public void stop()
    {
        m_kdsStatistic.stop();
    }


    public void updateSettings()
    {

        m_kdsStatistic.updateSettings();
        m_kdsStatistic.start();
    }

    public STKDSStatistic getKDSStatistic()
    {
        return  m_kdsStatistic;
    }

    public class MyBinder extends Binder
    {
        STService getService()
        {
            return STService.this;
        }
    }

    public void onTimer()
    {
        m_kdsStatistic.on1sTimer();
    }
}
