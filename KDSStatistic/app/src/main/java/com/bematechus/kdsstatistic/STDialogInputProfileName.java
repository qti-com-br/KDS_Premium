package com.bematechus.kdsstatistic;

import android.content.Context;
import android.widget.TextView;

import com.bematechus.kdslib.ConditionStatistic;
/**
 *
 */
public class STDialogInputProfileName extends KDSUIDialogBase {
    TextView m_txtName = null;
    String m_strInput = "";

    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return m_strInput;
    }
    public STDialogInputProfileName(final Context context, ConditionStatistic condition, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.st_dialog_input_profile_name, "");
        this.setTitle(context.getString(R.string.dialog_profile_name));


        m_txtName =  (TextView)this.getView().findViewById(R.id.txtFileName);
        m_txtName.setText(condition.getProfileNewName());

    }



    public void onOkClicked()
    {
        m_strInput = m_txtName.getText().toString();
    }
}
