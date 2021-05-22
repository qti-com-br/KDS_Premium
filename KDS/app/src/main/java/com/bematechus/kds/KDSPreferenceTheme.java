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
                .setMessage("theme changed, all existed will been overwrite")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                KDSPreferenceTheme.super.setValue(m_newValue);
                            }
                        }
                )
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
