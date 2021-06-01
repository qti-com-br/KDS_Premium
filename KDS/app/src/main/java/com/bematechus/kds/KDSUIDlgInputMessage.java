package com.bematechus.kds;

import android.content.Context;
import android.widget.TextView;

import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.ScheduleProcessOrder;

public class KDSUIDlgInputMessage   extends KDSUIDialogBase {
    TextView m_txtText = null;
    String  m_strMessage = "";

    //KDSDataOrder m_order = null;
    String mOrderGuid = "";
    KDSUser.USER m_userID = KDSUser.USER.USER_A;

    public void setOrderGuid(String orderGuid)
    {
        mOrderGuid = orderGuid;
    }
    public String getOrderGuid()
    {
        return mOrderGuid;
    }
    public void setUserID(KDSUser.USER userID)
    {
        m_userID = userID;
    }
    public KDSUser.USER getUserID()
    {
        return m_userID;
    }


    /**
     * it will been overrided by child
     *
     * @return
     */
    public Object getResult() {
        return m_strMessage;
    }

    public void onOkClicked() {
        String s = m_txtText.getText().toString();
        m_strMessage = s;

    }


    public KDSUIDlgInputMessage(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.setUseCtrlEnterKey(true);
        this.int_dialog(context, listener, R.layout.dlg_input_message, "");
        //this.setTitle(context.getString(R.string.input_ready_qty));

        m_txtText = (TextView) this.getView().findViewById(R.id.txtMessage);

    }



}
