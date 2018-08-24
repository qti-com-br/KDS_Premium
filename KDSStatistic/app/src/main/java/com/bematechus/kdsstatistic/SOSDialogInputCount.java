package com.bematechus.kdsstatistic;

import android.content.Context;
import android.widget.TextView;

import com.bematechus.kdslib.KDSUtil;

/**
 * Created by David.Wong on 2018/5/22.
 * Rev:
 */
public class SOSDialogInputCount extends KDSUIDialogBase {


    TextView m_txtText = null;

    int m_nCount = 0;


    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return m_nCount;
    }
    public void onOkClicked()
    {
        String s = m_txtText.getText().toString();
        if (s.isEmpty())
            s = "0";

        m_nCount = KDSUtil.convertStringToInt(s, 0);

    }

    public SOSDialogInputCount(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.sos_dialog_input_count, "");


        this.setTitle(context.getString(R.string.input_count));

        m_txtText = (TextView)this.getView().findViewById(R.id.txtText);


    }


}