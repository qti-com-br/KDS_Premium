package com.bematechus.kdsstatistic;

import android.content.Context;
import android.widget.NumberPicker;

import com.bematechus.kdslib.KDSUtil;

/**
 * Created by Administrator on 2016/8/9.
 */
public class SOSDialogTimePicker extends KDSUIDialogBase{

    //TimePicker m_tpTime = null;
    //Date m_dtSelected = null;
    int m_nDuration = 0; //seconds
    NumberPicker m_npH = null;
    NumberPicker m_npM = null;
    NumberPicker m_npS = null;
    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return m_nDuration;
    }
    public SOSDialogTimePicker(final Context context, int nSeconds, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.sos_dialog_time_picker, "");
        this.setTitle(context.getString(R.string.dialog_time_picker));


        //m_tpTime =  (TimePicker)this.getView().findViewById(R.id.timePicker);
        m_npH = (NumberPicker)this.getView().findViewById(R.id.npH);
        m_npM = (NumberPicker)this.getView().findViewById(R.id.npM);
        m_npS = (NumberPicker)this.getView().findViewById(R.id.npS);
        initHoursNumberPicker(m_npH);
        initMinutesNumberPicker(m_npM);
        initMinutesNumberPicker(m_npS);

        m_nDuration = nSeconds;
        showSeconds(m_nDuration);

    }

    private void initHoursNumberPicker(NumberPicker np)
    {
        final int MAX_COUNT = 48;
        String [] ar = new String[MAX_COUNT];
        for (int i=0;i< MAX_COUNT; i++ )
        {
            ar[i] = KDSUtil.convertIntToString(i);
        }
        np.setDisplayedValues(ar);//设置需要显示的数组
        np.setMinValue(0);
        np.setMaxValue(ar.length - 1);//这两行不能缺少,不然只能显示第一个，关联到format方法
    }

    private void initMinutesNumberPicker(NumberPicker np)
    {
        final int MAX_COUNT = 60;
        String [] ar = new String[MAX_COUNT];
        for (int i=0;i< MAX_COUNT; i++ )
        {
            ar[i] = KDSUtil.convertIntToString(i);
        }
        np.setDisplayedValues(ar);//设置需要显示的数组
        np.setMinValue(0);
        np.setMaxValue(ar.length - 1);//这两行不能缺少,不然只能显示第一个，关联到format方法
    }


    private void showSeconds(int nSeconds)
    {
        int h = (nSeconds / 3600);
        int m = ((nSeconds % 3600)/ 60);
        int s = (nSeconds % 60);
        m_npH.setValue(h);
        m_npM.setValue(m);
        m_npS.setValue(s);


    }

    private int getSeconds()
    {
        int h = m_npH.getValue();
        int m = m_npM.getValue();
        int s = m_npS.getValue();
        return h * 3600 + m * 60 + s;
    }

    public int getSelectedDuration()
    {
        return getSeconds();
    }

    public void onOkClicked()
    {
        m_nDuration = getSeconds();


    }

    public void setInitDuration(int nDuration)
    {
        m_nDuration = nDuration;
        showSeconds(m_nDuration);
    }
}
