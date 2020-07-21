package com.bematechus.kdsrouter;

import android.content.Context;
import android.widget.TimePicker;

import com.bematechus.kdslib.KDSUIDialogBase;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2016/8/9.
 */
public class DialogTimePicker extends KDSUIDialogBase {

    TimePicker m_tpTime = null;
    Date m_dtSelected = null;

    /**
     * it will been override by child
     * @return
     */
    public Object getResult()
    {
        return m_dtSelected;
    }
    public DialogTimePicker(final Context context, Date dt, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.dialog_time_picker, "");
        this.setTitle(context.getString(R.string.dialog_time_picker));


        m_tpTime =  (TimePicker)this.getView().findViewById(R.id.timePicker);
        Calendar c = Calendar.getInstance();
        c.setTime(dt);

        m_tpTime.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
        m_tpTime.setCurrentMinute(c.get(Calendar.MINUTE));

    }


    public void onOkClicked()
    {
        Calendar c = Calendar.getInstance();
        c.set(1999, 1,1,m_tpTime.getCurrentHour(),m_tpTime.getCurrentMinute(), 0);
        m_dtSelected = c.getTime();





    }
}
