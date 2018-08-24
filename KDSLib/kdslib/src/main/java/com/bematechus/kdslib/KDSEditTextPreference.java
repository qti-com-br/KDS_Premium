package com.bematechus.kdslib;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.KeyEvent;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

/**
 * Created by Administrator on 2015/11/30 0030.
 */
public class KDSEditTextPreference extends EditTextPreference {

    public static final String TAG ="KDSEditTextPreference";
    public KDSEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KDSEditTextPreference(Context context) {
        super(context);

    }

    /**
     * Shows the dialog associated with this Preference. This is normally initiated
     * automatically on clicking on the preference. Call this method if you need to
     * show the dialog on some other event.
     *
     * @param state Optional instance state to restore on the dialog
     */
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        this.getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP)
                {
                    KDSKbdRecorder.convertKeyEvent(keyCode, event);
                }
                return false;
            }
        });

    }

    public void onClick()
    {
        super.onClick();
    }


    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        boolean bInt = false;
        try
        {
            setText(restoreValue ? getPersistedString(getText()) : (String) defaultValue);
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            if (e instanceof ClassCastException)
                bInt = true;

        }

        try {
            if (bInt) {
                int n = getPersistedInt(KDSUtil.convertStringToInt(getText(), 0));

                setText(restoreValue ? KDSUtil.convertIntToString(n) : (String) defaultValue);
            }
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            setText("");
        }


    }

}
