package com.bematechus.kds;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSConst;

/**
 * Created by Administrator on 2018/4/24.
 */
public class KDSPreferenceEnablePassword extends CheckBoxPreference implements  KDSUIDialogBase.KDSDialogBaseListener {
    public KDSPreferenceEnablePassword(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public KDSPreferenceEnablePassword(Context context, AttributeSet attrs) {
        super(context, attrs);

    }
    @Override
    protected void onClick() {
       // super.onClick();

        final boolean newValue = !isChecked();
        //if (callChangeListener(newValue)) {
        //    setChecked(newValue);
        //}
        //if (newValue)
        {//checked, enable password
            if (this.getContext() == null) return;
            KDSUIDlgInputPassword dlg = new KDSUIDlgInputPassword(this.getContext(), this, newValue);
            dlg.getDialog().setCancelable(false);//.setFinishOnTouchOutside(false);
            dlg.getDialog().setCanceledOnTouchOutside(false);
            //dlg.setDisableCancelButton(true); //2.0.12,
            // 2.0.14 if cancel, disable the password
            dlg.show();
        }
    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog) {
        if (dialog instanceof KDSUIDlgInputPassword ) {

        }
        else
        {

        }
    }

    public void onKDSDialogOK(KDSUIDialogBase dlg, Object obj)// ArrayList<String> stations)
    {
        if (dlg instanceof KDSUIDlgInputPassword) {
            String pwd = (String) dlg.getResult();
            if (((KDSUIDlgInputPassword) dlg).getNeedConfirm()) //enable password.
            {//just input new password, save it
                setPassword(pwd);
                boolean newValue = !isChecked();
                if (callChangeListener(newValue)) {
                    setChecked(newValue);
                }
            }
            else {//input password then start next operation. Disable password
                String settingsPwd = getPassword();
                if (settingsPwd.isEmpty())
                    settingsPwd = KDSConst.DEFAULT_PASSWORD;// "123";
                if (pwd.isEmpty() || (!pwd.equals(settingsPwd))) {//input a wrong password
                    KDSUIDialogBase errordlg = new KDSUIDialogBase();
                    errordlg.createInformationDialog(this.getContext(), this.getContext().getString(R.string.error), this.getContext().getString(R.string.password_incorrect), false);
                    errordlg.show();

                    //KDSUtil.showErrorMessage(this, this.getString(R.string.password_incorrect));
                } else if (pwd.equals(settingsPwd)) {//input a correct password, it is for disable password.
                    //disable
                    setPassword("");
                    boolean newValue = !isChecked();
                    if (callChangeListener(newValue)) {
                        setChecked(newValue);
                    }

                }
            }
        }


    }
    public void setPassword(String strpwd)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("kds_general_password", strpwd);
        editor.apply();
        editor.commit();
    }

    public String getPassword()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        return pref.getString("kds_general_password", "");
    }
    public void setEnablePassword(boolean bEnabled)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("kds_general_enable_password", bEnabled);
        editor.apply();
        editor.commit();
    }

}
