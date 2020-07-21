package com.bematechus.kds;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bematechus.kdslib.KDSBumpBarKeyFunc;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by David.Wong on 2020/5/6.
 * Rev:
 */
public class DlgCleaningAlarm extends Dialog {

    public enum CleaningEventType
    {
        Alarm_Freeze_Screen_Now_By_Touch_Screen,
        Alarm_Freeze_Screen_Now_By_BumpBar,
        Alarm_Remind_Me_Later,
        Alarm_Dismiss_Alert,
        PinchOut_Pinched,
        Bumpbar_Timeout,

    }


    public interface  CleaningHabitsEvents
    {
        public void onCleaningHabitsEvent(CleaningEventType evt, ArrayList<Object> arParams);
    }

    static DlgCleaningAlarm m_instance = null;
    Context context = null;
    float m_fltSnoozeTime = 0;
    CleaningHabitsEvents m_receiver = null;

    KDSBumpBarKeyFunc m_bumpbarClean = null;
    KDSBumpBarKeyFunc m_bumpbarSnooze = null;
    KDSBumpBarKeyFunc m_bumpbarDismiss = null;
    ///////////////////////////////////////////////////////////////////////////////////////////////
    //

    public void setEventReceiver(CleaningHabitsEvents r)
    {
        m_receiver = r;
    }
    static public DlgCleaningAlarm instance(Context c, float fltSnoozeTime)
    {
        if (m_instance != null) {
            if (m_instance.isShowing())
                return m_instance;
            else
                m_instance.hide();
        }
        m_instance = new DlgCleaningAlarm(c, fltSnoozeTime);
        return m_instance;

    }

    static public boolean isVisible()
    {
        if (m_instance == null)
            return false;
        return m_instance.isShowing();
    }

    static public void closeInstance()
    {
        if (m_instance == null)
            return ;
        try {
            m_instance.dismiss();
        }
        catch ( Exception e)
        {

        }
        finally {
            m_instance = null;
        }
    }

    public DlgCleaningAlarm(Context context, float fltSnoozeTime) {
        super(context);
        this.context = context;
        m_fltSnoozeTime = fltSnoozeTime;

        String strKey = KDSGlobalVariables.getKDS().getSettings().getString(KDSSettings.ID.Bumpbar_Clean);
        m_bumpbarClean = KDSBumpBarKeyFunc.parseString(strKey);

        strKey = KDSGlobalVariables.getKDS().getSettings().getString(KDSSettings.ID.Bumpbar_Snooze);
        m_bumpbarSnooze = KDSBumpBarKeyFunc.parseString(strKey);

        strKey = KDSGlobalVariables.getKDS().getSettings().getString(KDSSettings.ID.Bumpbar_Dismiss);
        m_bumpbarDismiss = KDSBumpBarKeyFunc.parseString(strKey);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View v = View.inflate(this.context, R.layout.dlg_cleaning_alarm, null);
        setContentView(v);
        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);

        int ntype = KDSGlobalVariables.getKDS().getSettings().getInt(KDSSettings.ID.Bumpbar_Kbd_Type);
        KDSBumpBarKeyFunc.KeyboardType kbdType = KDSBumpBarKeyFunc.KeyboardType.values()[ntype];

        TextView t = v.findViewById(R.id.btnFreeze);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCleaningNow();
            }
        });

        String ss =  this.getContext().getString(R.string.clean_freeze_screen_now);
        ss = ss.replace("[0]", m_bumpbarClean.getSummaryString(kbdType));
        t.setText(ss);

        if (m_fltSnoozeTime >0) {
            t = v.findViewById(R.id.btnRemindMe);
            String s = this.getContext().getString(R.string.remind_me_in_minutes);
            s = s.replace("#", Integer.toString(Math.round(m_fltSnoozeTime)));
            s = s.replace("[1]", m_bumpbarSnooze.getSummaryString(kbdType));

            t.setText(s);
            t.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickRemindLater();
                }
            });
        }
        else
        { //kpp1-330
            t = v.findViewById(R.id.btnRemindMe);
            t.setVisibility(View.GONE);
        }
        //////////
        t = v.findViewById(R.id.btnDismiss);

        ss =  this.getContext().getString(R.string.dismiss_clert);
        ss = ss.replace("[9]", m_bumpbarDismiss.getSummaryString(kbdType));
        t.setText(ss);


        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDismiss();
            }
        });
        boolean bEnableDismiss = KDSGlobalVariables.getKDS().getSettings().getBoolean(KDSSettings.ID.cleaning_enable_dismiss_button);
        if (!bEnableDismiss)
            t.setVisibility(View.GONE);

        this.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (m_bumpbarClean.getKeyCode() == keyCode)
                {
                    onKeycodeCleaningNow();
                }
                else if (keyCode == m_bumpbarSnooze.getKeyCode())
                {
                    onClickRemindLater();
                }
                else if (keyCode == m_bumpbarDismiss.getKeyCode())
                {
                    onClickDismiss();
                }
                return false;
            }
        });
    }

    /**
     * show pinch out screen.
     */
    public void onClickCleaningNow()
    {
        if (m_receiver != null)
            m_receiver.onCleaningHabitsEvent(CleaningEventType.Alarm_Freeze_Screen_Now_By_Touch_Screen, null);
        this.dismiss();
    }

    public void onClickRemindLater()
    {
        if (m_receiver != null)
            m_receiver.onCleaningHabitsEvent(CleaningEventType.Alarm_Remind_Me_Later, null);
        this.dismiss();
    }

    public void onClickDismiss()
    {
        if (m_receiver != null)
            m_receiver.onCleaningHabitsEvent(CleaningEventType.Alarm_Dismiss_Alert, null);
        this.dismiss();
    }

    public void onKeycodeCleaningNow()
    {
        if (m_receiver != null)
            m_receiver.onCleaningHabitsEvent(CleaningEventType.Alarm_Freeze_Screen_Now_By_BumpBar, null);
        this.dismiss();
    }
}
