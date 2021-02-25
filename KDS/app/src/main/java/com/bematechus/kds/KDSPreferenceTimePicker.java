package com.bematechus.kds;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
//import android.preference.DialogPreference;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.preference.DialogPreference;

public class KDSPreferenceTimePicker  extends DialogPreference {
//    public KDSPreferenceTimePicker(Context context) {
//        super(context);
//    }
//
//    public KDSPreferenceTimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    String m_strDefaultValue = "";
    public KDSPreferenceTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public KDSPreferenceTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        String defaultVal = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        m_strDefaultValue = defaultVal;
    }

    protected void showDialog(Bundle state)
    {
        setDialogTitle(this.getTitle());

        setPositiveButtonText(getContext().getString(R.string.str_ok));
        setNegativeButtonText(getContext().getString(R.string.str_cancel));
        super.showDialog(state);
    }
    @Override
    protected View onCreateDialogView() {

        setDialogLayoutResource(R.layout.dlg_time_picker);

        View v = super.onCreateDialogView();
        return v;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
//      Dialog dialog = getDialog();
        chkEnabled = (view.findViewById(R.id.chkEnabled));

        timePicker = (view.findViewById(R.id.time_picker));
        timePicker.setIs24HourView(false);
        timePicker.setDrawingCacheEnabled(true);

        String s = this.getPersistedString(m_strDefaultValue);
        if (s.indexOf(":")<0)
            s = m_strDefaultValue;
        //String[] s = SettingHelper.getIntervalStr().split(":");//old
        if (s.isEmpty())
        {
            chkEnabled.setChecked(false);
            timePicker.setCurrentHour(0);//
            timePicker.setCurrentMinute(0);
            timePicker.setEnabled(false);
        }
        else {
            chkEnabled.setChecked(true);
            String[] ar = s.split(":");
            timePicker.setCurrentHour(Integer.parseInt(ar[0]));//
            timePicker.setCurrentMinute(Integer.parseInt(ar[1]));
            timePicker.setEnabled(true);
        }

        chkEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                timePicker.setEnabled(isChecked);
            }
        });

    }

    private TimePicker timePicker;
    private CheckBox chkEnabled = null;
    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                //OK
                String intervalStr = String.format("%02d:%02d", timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                if (!chkEnabled.isChecked())
                    intervalStr = "";
//                String intervalStr = timePicker.getCurrentHour() + ":" +
//                        timePicker.getCurrentMinute();
                this.setSummary(intervalStr);
                this.persistString(intervalStr);
                //LogHelper.d(intervalStr);
//                long interval = Utils.interval2Mills(intervalStr);
//                if (interval < 0) {
//                    //MyApplication.getApplication().getSettingsActivity().showSnack(R.string.time_format_error);
//                } else {
//                    SettingHelper.setInterval(intervalStr);
//                    new AlarmHelper(MyApplication.getApplication().getSettingsActivity()).startAlarmForActivityWithInterval(
//                            AlarmActivity.ACTION_ALARM_SET_WALLPAPER, interval,
//                            AlarmHelper.REQUEST_CODE_ALARM_SET_WALLPAPER);
//                    //MyApplication.getApplication().getSettingsActivity().showSnack(R.string.update_setting);
//                    this.setSummary(intervalStr);
//                }
                //LogHelper.d("点击OK");
                //dialog.dismiss();//关闭,不再触发onPreferenceChange

                break;
            case Dialog.BUTTON_NEGATIVE:
                //do something
                break;
            case Dialog.BUTTON_NEUTRAL:
                //dosomething
                break;
        }
        super.onClick(dialogInterface, which);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return super.onCreateView(parent);
    }
}
