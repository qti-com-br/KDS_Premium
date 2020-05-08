package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Created by David.Wong on 2020/5/7.
 * Rev:
 */
public class KDSPreferenceCleaningInterval extends DialogPreference {

    private View m_view;
    @Override
    protected View onCreateDialogView() {

        m_view=super.onCreateDialogView();
        //spinner1 = (Spinner) view.findViewById(R.id.spinnerbase);
        String s = getPersistedString("2h");
        String s1 = s;
        int id = R.id.rb2;
        int ar[] = new int[]{R.id.rb1, R.id.rb2, R.id.rb3, R.id.rb4, R.id.rb5, R.id.rb6, R.id.rbCustom};
        if (s1.indexOf("m") >=0)
            id = R.id.rbCustom;
        else {
            s1 = s1.replace("h", "");
            int n = Integer.parseInt(s1) - 1;


            id = ar[n];
        }

        ((RadioButton)m_view.findViewById(id)).setChecked(true);

        for (int i=0;i < ar.length; i++)
        {
            ((RadioButton)m_view.findViewById(ar[i])).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    KDSPreferenceCleaningInterval.this.getDialog().hide();
                    KDSPreferenceCleaningInterval.this.getDialog().dismiss();
                    saveNewValue();
                }
            });
        }

//        RadioGroup rg =  m_view.findViewById(R.id.rgInterval);
//        rg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                KDSPreferenceCleaningInterval.this.getDialog().hide();
//                KDSPreferenceCleaningInterval.this.getDialog().dismiss();
//            }
//        });
        return m_view;
    }

    public KDSPreferenceCleaningInterval(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        super.setDialogLayoutResource(R.layout.pref_cleaning_interval);

        //super.setDialogIcon(R.drawable.ic);
    }

    public KDSPreferenceCleaningInterval(Context context, AttributeSet attrs) {
        super(context, attrs);

        super.setDialogLayoutResource(R.layout.pref_cleaning_interval);

        //super.setDialogIcon(R.drawable.ic);
    }

    public void saveNewValue()
    {
        RadioGroup rg =  m_view.findViewById(R.id.rgInterval);
        int id = rg.getCheckedRadioButtonId();
        String s = "2h";

        switch (id)
        {
            case R.id.rb1:
                s = "1h";
                break;
            case R.id.rb2:
                s = "2h";
                break;
            case R.id.rb3:
                s = "3h";
                break;
            case R.id.rb4:
                s = "4h";
                break;
            case R.id.rb5:
                s = "5h";
                break;
            case R.id.rb6:
                s = "6h";
                break;
            case R.id.rbCustom:
            {
                showCustomDlg();
            }
            default:
                break;
        }
        if (id != R.id.rbCustom) {
            persistString(s);
            updateSummary(s);


        }
    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

    }

    public void updateSummary()
    {
        String s = getPersistedString("2h");
        updateSummary(s);
    }
    /**
     *
     * @param val
     *  "m", minutes
     *  "h": hours
     */
    public void updateSummary(String val)
    {
        String s = val;
        s = s.replace("m", " minutes");
        s = s.replace("h", " hours");
        this.setSummary(s);
    }

    View m_viewCustom = null;
    AlertDialog m_dlg = null;

    private void showCustomDlg()
    {
        String strOK = getContext().getString(R.string.str_ok);// "OK";
        m_dlg = new AlertDialog.Builder(this.getContext())
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText t =  m_viewCustom.findViewById(R.id.txtInterval);
                        String s= t.getText().toString();
                        if (!s.isEmpty()) {
                            s += "m";
                            persistString(s);
                            updateSummary(s);
                        }
                    }
                })
                .create();
        m_viewCustom = LayoutInflater.from(getContext()).inflate(R.layout.dlg_custom_cleaning_interval, null);
       // EditText t =  m_viewCustom.findViewById(R.id.txtInterval);

        // kill all padding from the dialog window
        m_dlg.setView(m_viewCustom, 0, 0, 0, 0);
        m_dlg.show();

    }
}
