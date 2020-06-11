package com.bematechus.kds;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bematechus.kdslib.KDSTimer;
import com.bematechus.kdslib.KDSUtil;

import java.util.Date;

/**
 * Created by David.Wong on 2020/5/6.
 * Rev:
 */
public class DlgCleaningBumpbar  extends Dialog implements KDSTimer.KDSTimerInterface {
    Context context = null;
    int m_nCountDownSeconds = 0;
    KDSTimer m_timer = new KDSTimer();
    DlgCleaningAlarm.CleaningHabitsEvents m_receiver = null;
    static DlgCleaningBumpbar m_instance = null;

    public void setReceiver(DlgCleaningAlarm.CleaningHabitsEvents r)
    {
        m_receiver = r;
    }

    static public DlgCleaningBumpbar instance(Context c, int nCountDownSeconds)
    {
        if (m_instance != null) {
            if (m_instance.isShowing())
                return m_instance;
            else
                m_instance.hide();
        }
        m_instance = new DlgCleaningBumpbar(c, nCountDownSeconds);
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

    public DlgCleaningBumpbar(Context context, int nCountDownSeconds) {
        super(context);
        this.context = context;
        m_nCountDownSeconds = nCountDownSeconds;
    }
    TextView m_txtCountDown = null;
    Date m_dtStart = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View v = View.inflate(this.context, R.layout.dlg_cleaning_bumpbar, null);
        setContentView(v);
        m_txtCountDown = v.findViewById(R.id.txtCountDown);
//        String s = getContext().getString(R.string.will_return_in_seconds);
//        s = s.replace("#", Long.toString(m_nCountDownSeconds));
//        m_txtCountDown.setText(s);
        updateCountDown(m_nCountDownSeconds);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        KDSUtil.enableSystemVirtualBar(getWindow().getDecorView(), false);
        m_dtStart = new Date();
        m_timer.start(null, this, 1000);

        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);

    }

    public void onTime()
    {
        long n = System.currentTimeMillis();
        long l = n - m_dtStart.getTime();
        l = Math.round(l/1000);
        long c = m_nCountDownSeconds - l;
        if (c <=0)
        {
            if (m_receiver != null)
                m_receiver.onCleaningHabitsEvent(DlgCleaningAlarm.CleaningEventType.Bumpbar_Timeout, null);
            m_timer.stop();
            this.dismiss();
        }
        else
        {
            updateCountDown(c);

//            String s = getContext().getString(R.string.will_return_in_seconds);
//            int nStart = s.indexOf("#");
//            s = s.replace("#", Long.toString(c));
//            int nLen = Long.toString(c).length();
//
//            SpannableString spannableString = new SpannableString(s);
//            RelativeSizeSpan sizeSpan04 = new RelativeSizeSpan(1.8f);
//            spannableString.setSpan(sizeSpan04, nStart, nStart + nLen, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//
//            m_txtCountDown.setText(spannableString);
        }
    }

    private void updateCountDown(long c)
    {
        String s = getContext().getString(R.string.will_return_in_seconds);
        int nStart = s.indexOf("#");
        s = s.replace("#", Long.toString(c));
        int nLen = Long.toString(c).length();

        SpannableString spannableString = new SpannableString(s);
        RelativeSizeSpan sizeSpan04 = new RelativeSizeSpan(1.8f);
        spannableString.setSpan(sizeSpan04, nStart, nStart + nLen, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        m_txtCountDown.setText(spannableString);
    }
}
