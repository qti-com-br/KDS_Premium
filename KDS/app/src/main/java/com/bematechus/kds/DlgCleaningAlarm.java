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

import java.util.ArrayList;

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
        m_instance.dismiss();
    }

    public DlgCleaningAlarm(Context context, float fltSnoozeTime) {
        super(context);
        this.context = context;
        m_fltSnoozeTime = fltSnoozeTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View v = View.inflate(this.context, R.layout.dlg_cleaning_alarm, null);
        setContentView(v);
        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);

        TextView t = v.findViewById(R.id.btnFreeze);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCleaningNow();
            }
        });
        if (m_fltSnoozeTime >0) {
            t = v.findViewById(R.id.btnRemindMe);
            String s = this.getContext().getString(R.string.remind_me_in_minutes);
            s = s.replace("#", Integer.toString(Math.round(m_fltSnoozeTime)));
            t.setText(s);
            t.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickRemindLater();
                }
            });
        }

        t = v.findViewById(R.id.btnDismiss);
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
                if (keyCode == KeyEvent.KEYCODE_0)
                {
                    onKeycodeCleaningNow();
                }
                else if (keyCode == KeyEvent.KEYCODE_1)
                {
                    onClickRemindLater();
                }
                else if (keyCode == KeyEvent.KEYCODE_9)
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
