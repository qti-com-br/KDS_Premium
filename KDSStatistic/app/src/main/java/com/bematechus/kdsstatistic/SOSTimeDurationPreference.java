package com.bematechus.kdsstatistic;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import com.bematechus.kdslib.KDSUtil;

public class SOSTimeDurationPreference extends DialogPreference implements KDSUIDialogBase.KDSDialogBaseListener {
    private int lastDuration=0; //seconds


    private SOSDialogTimePicker picker = null;

    private int m_valueDefault = 0;

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        lastDuration = (int)obj;
    }

//    public static int getHour(String time) {
//        String[] pieces=time.split(":");
//
//        return(Integer.parseInt(pieces[0]));
//    }
//
//    public static int getMinute(String time) {
//        String[] pieces=time.split(":");
//
//        return(Integer.parseInt(pieces[1]));
//    }

    public SOSTimeDurationPreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);
        String s = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        m_valueDefault = KDSUtil.convertStringToInt(s, 0);
        lastDuration = m_valueDefault;

    }

    private String seconds2HMS(int seconds)
    {
        int h = (seconds / 3600);
        int m = ((seconds % 3600)/ 60);
        int s = (seconds % 60);
        return String.format("%02d:%02d:%02d", h, m, s);


    }

    @Override
    public CharSequence getSummary() {

        return seconds2HMS(lastDuration);

//        String s = "";
//        if (super.getSummary() != null)
//            s = super.getSummary().toString();
//
//        if (s.isEmpty())
//            return seconds2HMS(m_valueDefault);
//        else
//            return s;

    }
    @Override
    protected View onCreateDialogView() {

        picker=new SOSDialogTimePicker(getContext(),lastDuration, this);

        return(picker.getView());
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setInitDuration(lastDuration);

    }

    protected void onShowDialog()
    {

    }

    private void refreshSummary()
    {
        this.setSummary(seconds2HMS(lastDuration));
    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastDuration = picker.getSelectedDuration ();//int) picker.getResult();


            //String time= seconds2HMS(lastDuration);

            if (callChangeListener(lastDuration)) {
                persistInt(lastDuration);
            }
            refreshSummary();
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {

        return m_valueDefault;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) {
            lastDuration =  getPersistedInt(m_valueDefault);
        }
        else {
            lastDuration = m_valueDefault;
        }
    }
}