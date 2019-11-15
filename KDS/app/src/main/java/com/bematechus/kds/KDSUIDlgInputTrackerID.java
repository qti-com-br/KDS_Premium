package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.widget.TextView;

import com.bematechus.kdslib.KDSUIDialogBase;

/**
 * Created by Administrator on 2017/5/9.
 */
public class KDSUIDlgInputTrackerID extends KDSUIDialogBase {

    TextView m_txtText = null;
    String m_strTrackerID = "";
    String m_orderGuid = "";


    public void setOrderGuid(String guid)
    {
        m_orderGuid = guid;
    }
    public String getOrderGuid()
    {
        return m_orderGuid;
    }
    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return m_strTrackerID;
    }
    public void onOkClicked()
    {
        m_strTrackerID = m_txtText.getText().toString();
    }



    public KDSUIDlgInputTrackerID(final Context context,String strTrackerID, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.setUseCtrlEnterKey(true);
        this.int_dialog(context, listener, R.layout.kdsui_dlg_input_trackerid, "");
        this.setTitle(context.getString(R.string.tracker_id));

        m_txtText = (TextView)this.getView().findViewById(R.id.txtText);
        m_txtText.setText(strTrackerID);
        m_strTrackerID = strTrackerID;


    }

//    public String makeOKButtonText(Context context)
//    {
//        return makeCtrlEnterButtonText(context, DialogEvent.OK);
//
//    }
//    public String makeCancelButtonText(Context context)
//    {
//        return makeCtrlEnterButtonText(context, DialogEvent.Cancel);
//
//    }
//    protected String makeButtonText2(Context context, int nResID, KDSSettings.ID funcKey )
//    {
//        return makeCtrlEnterButtonText(context, nResID, funcKey);
//
//
//    }
//
//    protected void init_dialog_events(final AlertDialog dlg)
//    {
//        init_dialog_ctrl_enter_events(dlg);
//
//    }


}
