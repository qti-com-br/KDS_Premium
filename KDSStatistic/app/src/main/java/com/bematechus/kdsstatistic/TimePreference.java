package com.bematechus.kdsstatistic;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.util.Date;

public class TimePreference extends DialogPreference implements KDSUIDialogBase.KDSDialogBaseListener {
    private int lastHour=0;
    private int lastMinute=0;

    private STDialogTimePicker picker = null;

    private String m_valueDefault = "00:00";

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {

    }

    public static int getHour(String time) {
        String[] pieces=time.split(":");

        return(Integer.parseInt(pieces[0]));
    }

    public static int getMinute(String time) {
        String[] pieces=time.split(":");

        return(Integer.parseInt(pieces[1]));
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);
        m_valueDefault = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");

    }

    @Override
    public CharSequence getSummary() {
        String s = super.getSummary().toString();
        if (s.isEmpty())
            return m_valueDefault;
        else
            return s;

    }
    @Override
    protected View onCreateDialogView() {
        picker=new STDialogTimePicker(getContext(),new Date(), this);

        return(picker.getView());
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setInitHourMinutes(lastHour, lastMinute);

    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastHour=picker.getCurrentHour();
            lastMinute=picker.getCurrentMinute();


            String time=String.format("%02d:%02d", lastHour, lastMinute);

            if (callChangeListener(time)) {
                persistString(time);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {

        return m_valueDefault;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time=null;

        if (restoreValue) {
            time=m_valueDefault;
        }
        else {
            time=defaultValue.toString();
        }

        lastHour=getHour(time);
        lastMinute=getMinute(time);
    }
}