package com.bematechus.kdsstatistic;

import android.content.Context;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/9/21.
 */
public class SOSDlgInputStationID extends KDSUIDialogBase {


    TextView m_txtText = null;

    String m_strStationID = "";


    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return m_strStationID;
    }
    public void onOkClicked()
    {
        m_strStationID = m_txtText.getText().toString();
    }

    public SOSDlgInputStationID(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.sos_dlg_input_station_id, "");


        this.setTitle(context.getString(R.string.station_id) );

        m_txtText = (TextView)this.getView().findViewById(R.id.txtText);
        this.getDialog().setCanceledOnTouchOutside(false);
        this.getDialog().setCancelable(false);

    }


}
