/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bematechus.kdslib;

import android.app.Activity;

import java.util.Timer;
import java.util.TimerTask;

//import javax.swing.JLabel;

/**
 *
 * @author David.Wong
 */
public class KDSTimer {

    //the interface
    public interface KDSTimerInterface {
        public void onTime();
    }

    Activity m_activity = null;
    KDSTimerInterface m_receiver = null;

    private Timer m_timer = new Timer();
    
    public void setReceiver(KDSTimerInterface c)
    {
        m_receiver = c;
    }

    
    public void start(Activity activityCallback, KDSTimerInterface receiver, int msInterval) {
        int nInterval = msInterval; //ms
        m_activity = activityCallback;
        m_receiver = receiver;//(KDSTimerInterface) activity;
        m_timer = new Timer();
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() { 
                updateTime(); 
                //m_timer.cancel(); 
            } 
            private void updateTime() { 
                //javax.swing.SwingUtilities.invokeLater(new Runnable() {
                if (m_activity == null) return;
                m_activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (m_receiver != null)
                            m_receiver.onTime();
                        //refreshTime();
                        }
                });

            }
        },nInterval, nInterval);

    }

    public void stop()
    {
        
        m_timer.cancel();
    }

}
