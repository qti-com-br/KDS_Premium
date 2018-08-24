package com.bematechus.kdsstatistic;

import android.content.Context;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2016/8/5.
 */
public class STDialogDatePicker extends KDSUIDialogBase {

    DatePicker m_dpDate = null;

    Date m_dtSelected = null;
    Object m_tag = null;

    public void setTag(Object obj)
    {
        m_tag = obj;
    }
    public Object getTag()
    {
        return m_tag;
    }
    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return m_dtSelected;
    }
    public STDialogDatePicker(final Context context, Date dt, KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.st_dialog_date_picker, "");
        this.setTitle(context.getString(R.string.dialog_date_picker));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt);

        m_dpDate = (DatePicker)this.getView().findViewById(R.id.datePicker);
        m_dpDate.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH), null);


    }

    public void onOkClicked()
    {
        Calendar c = Calendar.getInstance();
        c.set(m_dpDate.getYear(), m_dpDate.getMonth(), m_dpDate.getDayOfMonth(), 0, 0, 0);
        m_dtSelected = c.getTime();



    }
}
