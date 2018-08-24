package com.bematechus.kdsstatistic;

import android.content.Context;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2016/8/9.
 */
public class STDialogTimePicker extends KDSUIDialogBase{

    TimePicker m_tpTime = null;
    Date m_dtSelected = null;

    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return m_dtSelected;
    }
    public STDialogTimePicker(final Context context, Date dt, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.st_dialog_time_picker, "");
        this.setTitle(context.getString(R.string.dialog_time_picker));


        m_tpTime =  (TimePicker)this.getView().findViewById(R.id.timePicker);

    }

    public void setInitHourMinutes(int nHour, int nMinute)
    {
        if (m_tpTime != null) {
            m_tpTime.setCurrentHour(nHour);
            m_tpTime.setCurrentMinute(nMinute);
        }

    }

    public int getCurrentHour()
    {
        if (m_tpTime!= null)
            return m_tpTime.getCurrentHour();
        else
            return 0;
    }
    public int getCurrentMinute()
    {
        if (m_tpTime!= null)
            return m_tpTime.getCurrentMinute();
        else
            return 0;
    }


    public void onOkClicked()
    {
        Calendar c = Calendar.getInstance();
        c.set(1999, 1,1,m_tpTime.getCurrentHour(),m_tpTime.getCurrentMinute(), 0);
        m_dtSelected = c.getTime();

    }
}
