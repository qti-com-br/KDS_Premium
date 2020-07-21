package com.bematechus.kds;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.bematechus.kdslib.Activation;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSTimer;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by David.Wong on 2020/5/7.
 * Rev:
 */
public class CleaningHabitsManager implements DlgCleaningAlarm.CleaningHabitsEvents, KDSTimer.KDSTimerInterface {

    final String TAG = "Cleaning";
    static public final boolean _DEBUG = false;

    FloatingActionButton m_fab = null;
    Context m_context = null;
    Activation m_activation = null;

    public KDSSettings getSettings() {
        return KDSGlobalVariables.getKDS().getSettings();
    }

    public void init(Context context, FloatingActionButton btn, Activation activation) {
        m_context = context;
        m_fab = btn;
        m_activation = activation;
        initCleaningFloatButton();

    }

    public void initCleaningFloatButton() {
        //FloatingActionButton fab = findViewById(R.id.fabCleaning);

        m_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cleaningShowAlarmDlg();
                cleaningShowFloatButton(false);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });
        //m_fab.setVisibility(View.INVISIBLE);
    }

    public void cleaningShowFloatButton(boolean bShow) {
        //FloatingActionButton fab = findViewById(R.id.fabCleaning);
        if (bShow) {
            startFlashFloatButton();
            m_fab.setVisibility(View.VISIBLE);

        }
        else {
            stopFlashFloatButton();
            m_fab.setVisibility(View.INVISIBLE);

        }

    }

    public void cleaningShowPinchOutDlg() {
        DlgCleaningPinchout d = DlgCleaningPinchout.instance(m_context);
        d.setReceiver(this);
        d.show();
    }
    final int ALERT_DLG_COUNTDOWN = 10;

    public void cleaningShowBumpbarDlg() {
        DlgCleaningBumpbar d = DlgCleaningBumpbar.instance(m_context, ALERT_DLG_COUNTDOWN);
        d.setReceiver(this);
        d.show();
    }

    public void cleaningShowAlarmDlg() {
        DlgCleaningAlarm d = DlgCleaningAlarm.instance(m_context, getSettings().getFloat(KDSSettings.ID.cleaning_snooze_time));
        d.setEventReceiver(this);
        d.show();
    }

    public void resetTimer() {
        m_dtLastCleaning = new Date();
    }

    Date m_dtLastCleaning = new Date();
    boolean m_bAppInitializingAlertShown = false;

    public void checkCleaningHabits() {
        if (!getSettings().getBoolean(KDSSettings.ID.cleaning_enable_alert))
            return;
        if (getSettings().getBoolean(KDSSettings.ID.cleaning_startup_alert)) {
            if (!KDSApplication.m_bCleanedAfterAppStarted){//m_bAppInitializingAlertShown) {
                cleaningShowAlert();
                //m_bAppInitializingAlertShown = true;
                KDSApplication.m_bCleanedAfterAppStarted = true;
                return;
            }
        }
        //Log.d(TAG, "------ 1 -----");
        if (isAlertShowing()) return;
        //Log.d(TAG, "------ 2 -----");
        int nms = 0; //ms

        if (m_bRemindLaterEnabled) {
            int n = getSettings().getInt(KDSSettings.ID.cleaning_snooze_time);//minutes
            if (_DEBUG)
                nms = Math.round(n * 1000);
            else
                nms = Math.round(n * 60 * 1000);
        } else {
            //float nInterval = getSettings().getFloat(KDSSettings.ID.cleaning_reminder_interval);//hours
            String s = getSettings().getString(KDSSettings.ID.cleaning_reminder_interval);
            int nInterval = 0;
            if (s.indexOf(KDSPreferenceCleaningInterval.SUFFIX_MINUTE) >= 0) {
                s = s.replace(KDSPreferenceCleaningInterval.SUFFIX_MINUTE, "");
                int nIntervalM = Integer.parseInt(s);
                nInterval = nIntervalM * 60;//seconds
            } else {
                s = s.replace(KDSPreferenceCleaningInterval.SUFFIX_HOUR, "");
                int nIntervalH = Integer.parseInt(s);
                nInterval = nIntervalH * 60 * 60;//seconds
            }
            if (_DEBUG)
                nInterval = 2; //debug
            nms = Math.round(nInterval * 1000);
            ;//Math.round( nInterval * 60 * 60 * 1000);
        }
        //Log.d(TAG, "------ 3 ----- " + Long.toString(nms));
        long nNow = System.currentTimeMillis();
        long n = nNow - m_dtLastCleaning.getTime();
        //Log.d(TAG, "------ 3a ----- " + Long.toString(n));
        if (n < nms)
            return;
        //Log.d(TAG, "------ 4 -----");
        if (m_bRemindLaterEnabled)
            m_bRemindLaterEnabled = false; //disable it.

        cleaningShowAlert();
        //Log.d(TAG, "------ 5 -----");
    }

    public void cleaningShowAlert() {
        int nMode = getSettings().getInt(KDSSettings.ID.cleaning_alert_type);

        KDSSettings.CleaningAlertType nType = KDSSettings.CleaningAlertType.values()[nMode];
        switch (nType) {
            case FloatButton:
                cleaningShowFloatButton(true);
                break;
            case ShowDialog:
                cleaningShowAlarmDlg();
                break;
            case CleanScreen:
                cleaningShowPinchOutDlg();
                m_activation.postCleaningResultResponse(Activation.CleaningResponse.CLEAN);
                break;
        }
    }

    public boolean isAlertShowing() {
        if (DlgCleaningAlarm.isVisible()) return true;
        if (DlgCleaningBumpbar.isVisible()) return true;
        if (DlgCleaningPinchout.isVisible()) return true;
        if (m_fab.getVisibility() == View.VISIBLE) return true;
        return false;
    }

    boolean m_bRemindLaterEnabled = false;

    public void onCleaningHabitsEvent(DlgCleaningAlarm.CleaningEventType evt, ArrayList<Object> arParams) {
        switch (evt) {

            case Alarm_Freeze_Screen_Now_By_Touch_Screen:
                cleaningShowPinchOutDlg();
                m_activation.postCleaningResultResponse(Activation.CleaningResponse.CLEAN);
                break;
            case Alarm_Freeze_Screen_Now_By_BumpBar:
                m_activation.postCleaningResultResponse(Activation.CleaningResponse.CLEAN);
                cleaningShowBumpbarDlg();
                break;
            case Alarm_Remind_Me_Later:
                m_activation.postCleaningResultResponse(Activation.CleaningResponse.SNOOZE);
                m_bRemindLaterEnabled = true;
                resetTimer();
                break;
            case Alarm_Dismiss_Alert:
                m_activation.postCleaningResultResponse(Activation.CleaningResponse.DISMISS);
                resetTimer();
                break;
            case PinchOut_Pinched:
                resetTimer();
                break;
            case Bumpbar_Timeout:
                resetTimer();
                break;
        }
    }

    public void resetAll() {
        resetTimer();
        m_bRemindLaterEnabled = false;
        cleaningShowFloatButton(false);
        DlgCleaningAlarm.closeInstance();
        DlgCleaningPinchout.closeInstance();
        DlgCleaningBumpbar.closeInstance();
    }

    KDSTimer m_flashTimer = new KDSTimer();

    private void startFlashFloatButton() {
        m_oldColors =  m_fab.getBackgroundTintList().withAlpha(255);
        m_flashTimer.start(null, this, 800);
    }
    private void stopFlashFloatButton()
    {
        m_flashTimer.stop();
        m_fab.setBackgroundTintList(m_oldColors);
        //m_fab.setBackgroundTintMode(m_oldMode);
    }
    boolean m_bFlashToggle = false;
    ColorStateList m_oldColors = null;

    final int ALPHA_GRAY = 200;
    final int ALPHA_FULL = 255;
    public void onTime()
    {
        if (m_fab.getVisibility() != View.VISIBLE) return ;


        m_fab.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
        //m_oldMode = m_fab.getBackgroundTintMode();

        if (m_bFlashToggle) {
            ColorStateList list = m_oldColors.withAlpha(ALPHA_GRAY);
            m_fab.setBackgroundTintList(list);
        //    m_fab.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
        }
        else {
            ColorStateList list = m_oldColors.withAlpha(ALPHA_FULL);
            m_fab.setBackgroundTintList(list);
            //  m_fab.setBackgroundTintMode(m_oldMode);
            //m_fab.setRippleColor(0);
        }

        m_bFlashToggle = !m_bFlashToggle;

    }
}
