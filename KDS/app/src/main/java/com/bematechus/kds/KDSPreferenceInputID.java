package com.bematechus.kds;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import com.bematechus.kdslib.KDSEditTextPreference;
import com.bematechus.kdslib.KDSUIDialogBase;

/**
 * Created by David.Wong on 2020/2/26.
 * KPP1-302 Duplicate Station IDs
 * I use the KDSUIDialogInputID.java to input station ID. This will make same UI as activation.
 * Rev:
 */
public class KDSPreferenceInputID extends KDSEditTextPreference implements KDSUIDialogBase.KDSDialogBaseListener{
    public static final String TAG ="KDSPreferenceInputID";
    public KDSPreferenceInputID(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KDSPreferenceInputID(Context context) {
        super(context);
    }

    String m_strInputedID = "";
    /**
     *
     * @param state Optional instance state to restore on the dialog
     */
    protected void showDialog(Bundle state)
    {
        Context context = getContext();
        m_strInputedID = "";

        KDSUIDialogInputID dlg = new KDSUIDialogInputID(context,context.getString(R.string.input_id_title), "", this.getText(), this, true);
        dlg.show();
        KDSGlobalVariables.getKDS().setStationAnnounceEventsReceiver(dlg);

    }

    /**
     * KPP1-302 Duplicate Station IDs
     * @param positiveResult
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        //super.onDialogClosed(positiveResult); //use my self dialog

        if (positiveResult) {
            String value = m_strInputedID;//mEditText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }

    public void onKDSDialogOK(KDSUIDialogBase dlg, Object obj)// ArrayList<String> stations)
    {
        String s = (String) dlg.getResult();
        m_strInputedID = s;
        KDSGlobalVariables.getKDS().setStationAnnounceEventsReceiver(null);
        onDialogClosed(true);
    }

    public void onKDSDialogCancel(KDSUIDialogBase dlg) {
        KDSGlobalVariables.getKDS().setStationAnnounceEventsReceiver(null);
        onDialogClosed(false);
    }
}
