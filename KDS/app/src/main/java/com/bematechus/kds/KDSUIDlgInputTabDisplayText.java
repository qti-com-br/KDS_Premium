package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/12/15.
 */
public class KDSUIDlgInputTabDisplayText extends KDSUIDialogBase {

    TextView m_txtText = null;
    String m_strDisplayText = "";
    TabDisplay.TabButtonData m_data = null;


    public void setButtonData( TabDisplay.TabButtonData data) {
        m_data = data;
    }

    public TabDisplay.TabButtonData getButtonData() {
        return m_data;
    }

    /**
     * it will been overrided by child
     *
     * @return
     */
    public Object getResult() {
        return m_strDisplayText;
    }

    public void onOkClicked() {
        m_strDisplayText = m_txtText.getText().toString();
    }


    public KDSUIDlgInputTabDisplayText(final Context context, String strDisplayText, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_input_trackerid, "");
        this.setTitle(context.getString(R.string.tab_display_text));// context.getString(R.string.tracker_id));

        m_txtText = (TextView) this.getView().findViewById(R.id.txtText);
        m_txtText.setText(strDisplayText);
        m_strDisplayText = strDisplayText;


    }

    protected String makeButtonText2(Context context, int nResID, KDSSettings.ID funcKey) {
        return makeCtrlEnterButtonText(context, nResID, funcKey);


    }

    protected void init_dialog_events(final AlertDialog dlg) {
        init_dialog_ctrl_enter_events(dlg);

    }
}