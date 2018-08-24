package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.widget.TextView;

import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.ScheduleProcessOrder;

/**
 * Created by Administrator on 2016/12/12.
 */
public class KDSUIDialogInputReadyQty extends KDSUIDialogBase {

    TextView m_txtText = null;
    int m_nReady = 0;

    ScheduleProcessOrder m_order = null;
    KDSUser.USER m_userID = KDSUser.USER.USER_A;
    public void setOrder(ScheduleProcessOrder order)
    {
        m_order = order;
    }
    public ScheduleProcessOrder getOrder()
    {
        return m_order;
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
        return m_nReady;
    }

    public void onOkClicked() {
        String s = m_txtText.getText().toString();
        int n = KDSUtil.convertStringToInt(s, 0);
        m_nReady = n;
    }


    public KDSUIDialogInputReadyQty(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_input_ready_qty, "");
        this.setTitle(context.getString(R.string.input_ready_qty));

        m_txtText = (TextView) this.getView().findViewById(R.id.txtReady);

    }
    protected String makeButtonText2(Context context, int nResID, KDSSettings.ID funcKey )
    {
        String s = context.getString(nResID);
        return KDSUIDialogBase.makeCtrlEnterButtonText(context, s, funcKey);

    }

    protected void init_dialog_events(final AlertDialog dlg)
    {
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                KDSSettings.ID evID = KDSUIDialogInputReadyQty.checkCtrlEnterEvent(keyCode, event);
                if (evID == KDSSettings.ID.Bumpbar_OK) {
                    onOkClicked();
                    dialog.dismiss();
                    if (KDSUIDialogInputReadyQty.this.listener != null)
                        KDSUIDialogInputReadyQty.this.listener.onKDSDialogOK(KDSUIDialogInputReadyQty.this, getResult());
                    return true;
                } else if (evID == KDSSettings.ID.Bumpbar_Cancel) {
                    dialog.cancel();
                    if (KDSUIDialogInputReadyQty.this.listener != null)
                        KDSUIDialogInputReadyQty.this.listener.onKDSDialogCancel(KDSUIDialogInputReadyQty.this);
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    KDSKbdRecorder.convertKeyEvent(keyCode, event);
                }
                return false;
            }
        });
    }


}