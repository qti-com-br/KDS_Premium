package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class KDSPreferenceTheme extends ListPreference {
    public KDSPreferenceTheme(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KDSPreferenceTheme(Context context) {
        super(context);

    }

    String m_newValue = "";
    public void setValue(String value) {
        if (value == null || getValue() == null)
            super.setValue(value);
        else {
            if (!getValue().equals(value)) {
                m_newValue = value;
                showConfirmDialog();
            }
        }
    }
    private void showConfirmDialog()
    {
        AlertDialog d = new AlertDialog.Builder(this.getContext())
                .setTitle(this.getContext().getApplicationContext().getString(R.string.confirm))
                .setMessage(getContext().getString(R.string.theme_confirm))
                .setPositiveButton(getContext().getString(R.string.str_continue), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                KDSPreferenceTheme.super.setValue(m_newValue);
                            }
                        }
                )
                .setNegativeButton(getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        d.setCanceledOnTouchOutside(false);
        d.setCancelable(false);

        d.show();
    }
}
