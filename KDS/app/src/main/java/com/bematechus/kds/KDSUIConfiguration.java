package com.bematechus.kds;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSPreferenceFragment;
import com.bematechus.kdslib.KDSSmbFile2;
import com.bematechus.kdslib.KDSStationsRelation;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUIDlgInputPassword;
import com.bematechus.kdslib.KDSUIRetriveConfig;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.PreferenceFragmentStations;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;



public class KDSUIConfiguration extends PreferenceActivity {

    private static final String TAG = "KDSUIConfiguration";
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);

    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);

        //
      //  KDSGlobalVariables.setConfigUI(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       // requestWindowFeature(Window.FEATURE_LEFT_ICON);
        super.onCreate(savedInstanceState);
        int color = this.getResources().getColor(R.color.settings_page_bg);//.getDrawable(R.drawable.bkcolor);
        ColorDrawable c = new ColorDrawable(color);
        this.getWindow().setBackgroundDrawable(c);

        PreferenceFragmentStations.setKDSCallback(KDSGlobalVariables.getKDS());

//        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
//                R.drawable.lci);



    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(this.getResources().getString(R.string.settings));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //
     //   KDSGlobalVariables.setConfigUI(null);
    }

    private boolean isDirty()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//.getSharedPreferences(SyncStateContract.Constants.PREFS_NAME, Context.MODE_PRIVATE);

        boolean isdirty = pref.getBoolean("isDirtyPrefs", false);
        return isdirty;
    }
    private void setPrefFlag()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//.getSharedPreferences(SyncStateContract.Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isDirtyPrefs", false);
        editor.apply();
        editor.commit();
    }


    static final private int HEADERS_WIDTH = 200;
    @Override
    public void onResume()
    {
        super.onResume();
        if (isDirty()) {
            //this.recreate();
            setPrefFlag();
           // this.finish();
        }
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
       // this.getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DisplayMetrics dm = null;// new DisplayMetrics();
        dm = getResources().getDisplayMetrics();


        int screenWidth  = dm.widthPixels;      // 屏幕宽（像素，如：480px）
        if (isXLargeTablet(this)){
            Resources mResources = Resources.getSystem();  //getResources()测试也可以
            int id = mResources.getIdentifier("headers", "id", "android");

            LinearLayout layoutHeaders = (LinearLayout) this.findViewById(id);//com.android.internal. R.id.headers);// com.android.internal.R.id.headers);
            layoutHeaders.setBackgroundColor(this.getResources().getColor(R.color.settings_headers_bg));

            layoutHeaders.setLayoutParams(new LinearLayout.LayoutParams(HEADERS_WIDTH, LinearLayout.LayoutParams.MATCH_PARENT));
            id = mResources.getIdentifier("prefs_frame", "id", "android");
            LinearLayout layoutPrefs = (LinearLayout) this.findViewById(id);//com.android.internal.R.id.prefs_frame);
            layoutPrefs.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            layoutPrefs.setBackgroundColor(this.getResources().getColor(R.color.settings_page_bg));
        }
        this.getListView().setScrollBarFadeDuration(0);
        this.getListView().setScrollbarFadingEnabled(false);
        try {
            boolean bHide = (KDSGlobalVariables.getKDS().getSettings().getBoolean(KDSSettings.ID.Hide_navigation_bar));
            hideNavigationBar(bHide);
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
        }

    }

    private void hideNavigationBar(boolean bHide)
    {
        if (bHide) {
            View view = this.getWindow().getDecorView();
            KDSUtil.enableSystemVirtualBar(view, false);
            view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    KDSUtil.enableSystemVirtualBar(KDSUIConfiguration.this.getWindow().getDecorView(), false);
                }
            });
        }
        else
        {
            View view = this.getWindow().getDecorView();
            KDSUtil.enableSystemVirtualBar(view, true);
            view.setOnSystemUiVisibilityChangeListener(null);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (PreferenceFragmentStations.m_stationsRelations != null)
        {
            if (PreferenceFragmentStations.m_stationsRelations.onKeyDown(keyCode, event))
                return true;
        }
        boolean b = super.onKeyDown(keyCode, event);

        return b;
        //this.getFragmentManager().f
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {

        boolean b= super.onKeyUp(keyCode, event);
        KDSKbdRecorder.convertKeyEvent(keyCode, event);
        return b;

    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }
        @Override
    public boolean isValidFragment(String fragmentName)
    {
        return true;
    }
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    //preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.

                    preference.setSummary(stringValue);
            }
            return true;
        }
    };

    protected static void suspendOnSharedPreferencesChangedEvent(boolean bSuspend)
    {
        if (KDSGlobalVariables.getMainActivity() == null) return;
        KDSGlobalVariables.getMainActivity().suspendChangedEvent(bSuspend);
    }
    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        if (preference == null)
            return;
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public static class KDSPreferenceFragment extends PreferenceFragment  {
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
//            View v = super.onCreateView(inflater, root, savedInstanceState);
//
//            v.setBackgroundColor(this.getResources().getColor(R.color.settings_page_bg));
//            v.setPadding(0,0,0,0);
//
//
//            return v;
//        }
//        @Override
//        public void onActivityCreated(Bundle savedInstanceState) {
//            super.onActivityCreated(savedInstanceState);
//            showScrollbar();
//        }
//
//        public void showScrollbar()
//        {
//            try {
//
//
//                    if (this instanceof PreferenceFragmentStations)
//                        return;
//                    Class<PreferenceFragment> c = PreferenceFragment.class;
//                    Method method = c.getMethod("getListView");
//                    method.setAccessible(true);
//                    Object obj = method.invoke(this);
//                    if (obj != null) {
//                        ListView listView = (ListView) obj;// method.invoke(this);//, null);
//                        listView.setScrollBarFadeDuration(0);
//                        listView.setScrollbarFadingEnabled(false);
//                        //listView.setFastScrollAlwaysVisible(true);
//                        //listView.setNestedScrollingEnabled(false);
//                    }
////                }
//            }
//            catch (Exception err)
//            {//don't care this bug.
//                //KDSLog.e(TAG,KDSLog._FUNCLINE_() + err.toString());
//                //KDSLog.e(TAG, KDSUtil.error( err));
//            }
//        }
//    }
    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends KDSPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener , KDSUIDialogBase.KDSDialogBaseListener {
        boolean m_bDisableChangedEvent = false;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_general);
            suspendOnSharedPreferencesChangedEvent(false);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("kds_general_id"));
            bindPreferenceSummaryToValue(findPreference("kds_general_datasrc"));
            bindPreferenceSummaryToValue(findPreference("kds_general_tcpport"));
          //  bindPreferenceSummaryToValue(findPreference("kds_general_remote_folder"));
            bindPreferenceSummaryToValue(findPreference("kds_general_stationsport"));
            bindPreferenceSummaryToValue(findPreference("kds_general_stationfunc"));
//            bindPreferenceSummaryToValue(findPreference("kds_general_users"));
            bindPreferenceSummaryToValue(findPreference("kds_general_language"));

            bindPreferenceSummaryToValue(findPreference("kds_general_auto_backup_hours"));
//            bindPreferenceSummaryToValue(findPreference("kds_general_users_orientation"));
//            bindPreferenceSummaryToValue(findPreference("kds_general_title"));
            bindPreferenceSummaryToValue(findPreference("statistic_db_keep")); //2.0.25
            bindPreferenceSummaryToValue(findPreference("kds_general_auto_refresh_screen")); //2.0.25
            //            bindPreferenceSummaryToValue(findPreference("kds_general_users_ratio"));
//
//            bindPreferenceSummaryToValue(findPreference("kds_general_subtitle_a_title"));
//            bindPreferenceSummaryToValue(findPreference("kds_general_subtitle_b_title"));

            KDSSettings.KDSDataSource dataType =getDataSourceType(pref);

            setupGuiByDataSourceType(dataType);

            m_bDisableChangedEvent = false;
            //
            KDSUIRetriveConfig.setKDSCallback(KDSGlobalVariables.getKDS());
//            KDSSettings.KDSUserMode userMode = getScreenMode(pref);
//            enableSplitScreenOptions(userMode == KDSSettings.KDSUserMode.Multiple);



        }
        @Override
        public void onDestroy()
        {
            super.onDestroy();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.unregisterOnSharedPreferenceChangeListener(this);
        }

        private KDSSettings.KDSDataSource getDataSourceType(SharedPreferences prefs)
        {
            String key = ("kds_general_datasrc");

            String strDataType = prefs.getString(key, "0");//.getInt(key, 0);
            int nDataType = KDSUtil.convertStringToInt(strDataType, 0);
            KDSSettings.KDSDataSource dataType =  KDSSettings.KDSDataSource.values()[nDataType];
            return dataType;
        }

        private void doLanguageChanged(SharedPreferences prefs, String key)
        {
            String info = KDSApplication.getContext().getString(R.string.restart_kds_as_language);
            String s = prefs.getString(key, "0");
            int n = KDSUtil.convertStringToInt(s, 0);
            KDSSettings.Language lan = KDSSettings.Language.values()[n];
            String strLan = KDSSettings.getLanguageString(lan);

            info = info.replace("#", strLan);
            KDSUIDialogBase d = new KDSUIDialogBase();
            d.createOkCancelDialog(this.getActivity(),
                    MainActivity.Confirm_Dialog.Restart_me,
                    this.getString(R.string.confirm),
                    info, false, this);
            d.setCancelByClickOutside(false);
            d.show();
        }
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {
            if (m_bDisableChangedEvent) return;
            if (key.equals("kds_general_datasrc"))
            {

                KDSSettings.KDSDataSource portType =getDataSourceType(prefs);
                setupGuiByDataSourceType(portType);


            }
            else if (key.equals("kds_general_language"))
            {
                int n = KDSGlobalVariables.getKDS().getSettings().getInt(KDSSettings.ID.Language);
                String s = prefs.getString(key, "0");
                int nNew = KDSUtil.convertStringToInt(s, 0);
                if (n == nNew) return;
                doLanguageChanged(prefs, key);
                return;
            }
            else if (key.equals("general_enable_smbv2"))
            {
                boolean bEnableSmbV2 =  prefs.getBoolean(key, false);
                KDSSmbFile2.smb_setEnableSmbV2(bEnableSmbV2);
            }

//            else if (key.equals("kds_general_enable_password"))
//            {
//                //String keyid = ("kds_general_enable_password");
//
//                boolean bEnabled  = prefs.getBoolean(key, true);//.getInt(key, 0);
//
//                if (this.getActivity() == null) return;
//                //if (!bEnabled)
//                {
//                    if (this.getActivity() == null) return;
//                    KDSUIDlgInputPassword dlg = new KDSUIDlgInputPassword(this.getActivity(), this, bEnabled);
//                    dlg.getDialog().setCancelable(false);//.setFinishOnTouchOutside(false);
//                    dlg.getDialog().setCanceledOnTouchOutside(false);
//                    //dlg.setDisableCancelButton(true); //2.0.12,
//                    // 2.0.14 if cancel, disable the password
//                    dlg.show();
//                }
//
//            }


        }




        public void onKDSDialogCancel(KDSUIDialogBase dialog) {
            if (dialog instanceof KDSUIDlgInputPassword) {
//                m_bDisableChangedEvent = true;
//
//                if (KDSGlobalVariables.getKDS().getSettings().getBoolean(KDSSettings.ID.Settings_password_enabled))
//                    ((CheckBoxPreference) findPreference("kds_general_enable_password")).setChecked(true); //2.0.14
//                else {
//                    ((CheckBoxPreference) findPreference("kds_general_enable_password")).setChecked(false); //2.0.14
//                    setPassword("");
//                }
//                m_bDisableChangedEvent = false;
            }
            else
            {
                if (dialog.getTag() == null) return;
                MainActivity.Confirm_Dialog confirm = (MainActivity.Confirm_Dialog) dialog.getTag();
                if (confirm == MainActivity.Confirm_Dialog.Restart_me)
                {
                    int n = KDSGlobalVariables.getKDS().getSettings().getInt(KDSSettings.ID.Language);

                    ((ListPreference) findPreference("kds_general_language")).setValueIndex(n);
                    ((ListPreference) findPreference("kds_general_language")).setSummary(KDSSettings.getLanguageString(KDSSettings.Language.values()[n]));



                }
            }
        }

//        public void setPassword(String strpwd)
//        {
//            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
//            SharedPreferences.Editor editor = pref.edit();
//            editor.putString("kds_general_password", strpwd);
//            editor.apply();
//            editor.commit();
//        }
//
//        public void setEnablePassword(boolean bEnabled)
//        {
//            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
//            SharedPreferences.Editor editor = pref.edit();
//            editor.putBoolean("kds_general_enable_password", bEnabled);
//            editor.apply();
//            editor.commit();
//        }
        public void onKDSDialogOK(KDSUIDialogBase dlg, Object obj)// ArrayList<String> stations)
        {
//            if (dlg instanceof KDSUIDlgInputPassword) {
//                String pwd = (String) dlg.getResult();
//                if (((KDSUIDlgInputPassword) dlg).getNeedConfirm())
//                {//just input new password, save it
//                    setPassword(pwd);
//
////                    KDSGlobalVariables.getKDS().getSettings().set(KDSSettings.ID.Settings_password, pwd);
////                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
////                    SharedPreferences.Editor editor = pref.edit();
////                    editor.putString("kds_general_password", pwd);
////                    editor.apply();
////                    editor.commit();
//                }
//                else {//input password then start next operation
//                    String settingsPwd = KDSGlobalVariables.getKDS().getSettings().getString(KDSSettings.ID.Settings_password);
//                    if (settingsPwd.isEmpty())
//                        settingsPwd = KDSConst.DEFAULT_PASSWORD;// "123";
//                    if (pwd.isEmpty() || (!pwd.equals(settingsPwd))) {
//                        KDSUIDialogBase errordlg = new KDSUIDialogBase();
//                        errordlg.createInformationDialog(this.getActivity(), this.getActivity().getString(R.string.error), this.getActivity().getString(R.string.password_incorrect), false);
//                        errordlg.show();
//                        m_bDisableChangedEvent = true;
//                        ((CheckBoxPreference) findPreference("kds_general_enable_password")).setChecked(true);
//                        m_bDisableChangedEvent =  false;
//
//                        //KDSUtil.showErrorMessage(this, this.getString(R.string.password_incorrect));
//                    } else if (pwd.equals(settingsPwd)) {
//                        if (!((CheckBoxPreference) findPreference("kds_general_enable_password")).isChecked())
//                            setPassword(""); //2.0.14
//                        //((KDSPreferencePassword) findPreference("kds_general_password")).setText("");//.setChecked(true);
//                    }
//                }
//            }
//            else
            if (dlg instanceof KDSUIDlgInputPassword) {

            }
            else
            {
                if (dlg.getTag() == null) return;
                MainActivity.Confirm_Dialog confirm = (MainActivity.Confirm_Dialog) dlg.getTag();
                if (confirm == MainActivity.Confirm_Dialog.Restart_me)
                {
                    restartApp();
                }
            }

        }

        public void restartApp2()
        {
            //this.getActivity().finish();

//            Intent intentConfig = new Intent(KDSApplication.getContext(), MainActivity.class);
//            intentConfig.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intentConfig);

            Intent intent = new Intent(KDSApplication.getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity(intent);
//            Intent intent = new Intent(KDSApplication.getContext(), MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);


//            this.getActivity().setResult(RESULT_CANCELED);
//            this.getActivity().finish();
            //android.os.Process.killProcess( this.getActivity().getTaskId() .getId() android.os.Process.myPid());
            //KDSUIConfiguration.this.finish();

            //this.getActivity().finish();
            // 杀掉进程
            //android.os.Process.killProcess(android.os.Process.myPid());
            //System.exit(0);
        }

        public void restartApp()
        {
            Intent intent =KDSApplication.getContext().getPackageManager()
                    .getLaunchIntentForPackage(KDSApplication.getContext().getPackageName());
            PendingIntent restartIntent = PendingIntent.getActivity(KDSApplication.getContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager mgr = (AlarmManager)this.getActivity().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
            System.exit(0);
        }

        private void setupGuiByDataSourceType( KDSSettings.KDSDataSource srcType)
        {

            if (srcType ==  KDSSettings.KDSDataSource.TCPIP) {
                findPreference("kds_general_tcpport").setEnabled(true);
                findPreference("kds_general_remote_folder").setEnabled(false);
                //findPreference("general_enable_smbv2").setEnabled(false);

            }
            else if (srcType ==  KDSSettings.KDSDataSource.Folder)
            {
                findPreference("kds_general_tcpport").setEnabled(false);
                findPreference("kds_general_remote_folder").setEnabled(true);
                //findPreference("general_enable_smbv2").setEnabled(true);
            }

        }
    }



        /**
         * This fragment shows notification preferences only. It is used when the
         * activity is showing a two-pane settings UI.
         */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PanelsDisplayPreferenceFragment extends KDSPreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());

            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_orders);
            suspendOnSharedPreferencesChangedEvent(false);
            pref.registerOnSharedPreferenceChangeListener(this);
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
           bindPreferenceSummaryToValue(findPreference("panels_mode"));
            bindPreferenceSummaryToValue(findPreference("panels_rows"));
            bindPreferenceSummaryToValue(findPreference("panels_cols"));
            bindPreferenceSummaryToValue(findPreference("panelnum_base"));
            bindPreferenceSummaryToValue(findPreference("orders_sort_method"));
            bindPreferenceSummaryToValue(findPreference("panel_text_line_height"));
            bindPreferenceSummaryToValue(findPreference("from_primary_text"));
            bindPreferenceSummaryToValue(findPreference("panel_border_width"));

            bindPreferenceSummaryToValue(findPreference("icon_0"));
            bindPreferenceSummaryToValue(findPreference("icon_1"));
            bindPreferenceSummaryToValue(findPreference("icon_2"));
            bindPreferenceSummaryToValue(findPreference("icon_3"));
            bindPreferenceSummaryToValue(findPreference("icon_4"));
            bindPreferenceSummaryToValue(findPreference("icon_5"));
            bindPreferenceSummaryToValue(findPreference("icon_6"));
            bindPreferenceSummaryToValue(findPreference("icon_7"));
            bindPreferenceSummaryToValue(findPreference("icon_8"));
            bindPreferenceSummaryToValue(findPreference("icon_9"));
            bindPreferenceSummaryToValue(findPreference("icon_10"));
            bindPreferenceSummaryToValue(findPreference("icon_11"));
            bindPreferenceSummaryToValue(findPreference("icon_12"));
            bindPreferenceSummaryToValue(findPreference("icon_13"));
            bindPreferenceSummaryToValue(findPreference("icon_14"));
            bindPreferenceSummaryToValue(findPreference("icon_15"));
            bindPreferenceSummaryToValue(findPreference("icon_16"));
            bindPreferenceSummaryToValue(findPreference("icon_17"));
            bindPreferenceSummaryToValue(findPreference("icon_18"));
            bindPreferenceSummaryToValue(findPreference("icon_19"));
            bindPreferenceSummaryToValue(findPreference("icon_20"));
            bindPreferenceSummaryToValue(findPreference("icon_21"));
            bindPreferenceSummaryToValue(findPreference("icon_22"));
            bindPreferenceSummaryToValue(findPreference("icon_23"));
            bindPreferenceSummaryToValue(findPreference("icon_24"));
            bindPreferenceSummaryToValue(findPreference("icon_25"));
            bindPreferenceSummaryToValue(findPreference("icon_26"));
            bindPreferenceSummaryToValue(findPreference("icon_27"));
            bindPreferenceSummaryToValue(findPreference("icon_28"));
            bindPreferenceSummaryToValue(findPreference("icon_29"));
            bindPreferenceSummaryToValue(findPreference("icon_30"));
            bindPreferenceSummaryToValue(findPreference("icon_31"));
            bindPreferenceSummaryToValue(findPreference("icon_32"));

           // bindPreferenceSummaryToValue(findPreference("icon_folder"));

            pref.registerOnSharedPreferenceChangeListener(this);
            boolean b= pref.getBoolean("icon_folder_enabled", false);
            findPreference("icon_folder").setEnabled(b);

        }
            @Override
            public void onDestroy()
            {
                super.onDestroy();
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
                pref.unregisterOnSharedPreferenceChangeListener(this);
            }
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
            {

                if (key.equals("hide_navigation_bar"))
                {

                  boolean bhide = prefs.getBoolean(key, false);

                    if (this.getActivity() != null)
                        ((KDSUIConfiguration) this.getActivity()).hideNavigationBar(bhide);


                }
                if (key.equals("icon_folder_enabled"))
                {
                    boolean b= prefs.getBoolean(key, false);
                    findPreference("icon_folder").setEnabled(b);
                }

                String ar[] = new String[]{
                       // "icon_enabled",
                        "icon_folder_enabled",
                        "icon_folder",
                        "icon_0", "icon_1", "icon_2", "icon_3", "icon_4", "icon_5", "icon_6", "icon_7", "icon_8", "icon_9",
                        "icon_10", "icon_11", "icon_12", "icon_13", "icon_14", "icon_15", "icon_16", "icon_17", "icon_18", "icon_19",
                        "icon_20", "icon_21", "icon_22", "icon_23", "icon_24", "icon_25", "icon_26", "icon_27", "icon_28", "icon_29",
                        "icon_30", "icon_31", "icon_32",

                };
                for (int i=0; i< ar.length; i++)
                {
                    if (key.equals(ar[i]) )
                    {
                        KDSGlobalVariables.getKDS().getSettings().resetBufferedIcons();
                        break;
                    }
                }

            }
    }


    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ScreenBPanelsDisplayPreferenceFragment extends KDSPreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());

            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_screen1);
            suspendOnSharedPreferencesChangedEvent(false);
            pref.registerOnSharedPreferenceChangeListener(this);
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("screenb_panels_mode"));
            bindPreferenceSummaryToValue(findPreference("screenb_panels_rows"));
            bindPreferenceSummaryToValue(findPreference("screenb_panels_cols"));

            bindPreferenceSummaryToValue(findPreference("kds_general_users"));
            bindPreferenceSummaryToValue(findPreference("kds_general_users_ratio"));
            bindPreferenceSummaryToValue(findPreference("kds_general_users_orientation"));
            bindPreferenceSummaryToValue(findPreference("kds_general_subtitle_a_title"));
            bindPreferenceSummaryToValue(findPreference("kds_general_subtitle_b_title"));
            //bindPreferenceSummaryToValue(findPreference("screenb_panelnum_base"));
            //bindPreferenceSummaryToValue(findPreference("screenb_orders_sort_method"));
            //bindPreferenceSummaryToValue(findPreference("screenb_panel_text_line_height"));
            //bindPreferenceSummaryToValue(findPreference("screenb_from_primary_text"));
            //bindPreferenceSummaryToValue(findPreference("screenb_panel_border_width"));
            KDSSettings.KDSUserMode userMode = getScreenMode(pref);
            enableSplitScreenOptions(userMode == KDSSettings.KDSUserMode.Multiple);

        }
        @Override
        public void onDestroy()
        {
            super.onDestroy();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.unregisterOnSharedPreferenceChangeListener(this);
        }
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {
            if (key.equals("kds_general_users"))
            {
                KDSSettings.KDSUserMode userMode = getScreenMode(prefs);
                enableSplitScreenOptions(userMode == KDSSettings.KDSUserMode.Multiple);
            }


        }
        private KDSSettings.KDSUserMode getScreenMode(SharedPreferences prefs)
        {
            String key = ("kds_general_users");

            String strDataType = prefs.getString(key, "0");//.getInt(key, 0);
            int nDataType = KDSUtil.convertStringToInt(strDataType, 0);
            KDSSettings.KDSUserMode userMode =  KDSSettings.KDSUserMode.values()[nDataType];
            return userMode;
        }
        public void enableSplitScreenOptions(boolean bEnable)
        {
            ArrayList<String> ar = new ArrayList<>();
            ar.add("kds_general_users_orientation");
            ar.add("kds_general_users_ratio");
            ar.add("kds_general_subtitle_a_title");
            ar.add("kds_general_subtitle_b_title");
            ar.add("kds_general_subtitle_font");

            ar.add("screenb_panels_mode");
            ar.add("screenb_panels_rows");
            ar.add("screenb_panels_cols");
            //ar.add("kds_general_users_orientation");

            for (int i=0; i< ar.size(); i++)
            {
                String s = ar.get(i);
                Preference p = findPreference(s);
                if (p != null)
                    p.setEnabled(bEnable);
            }

        }

    }


    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CaptionPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_caption);
            suspendOnSharedPreferencesChangedEvent(false);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("sync_frequency"));
            bindPreferenceSummaryToValue(findPreference("caption_stage0_time"));
            bindPreferenceSummaryToValue(findPreference("caption_stage1_time"));
            bindPreferenceSummaryToValue(findPreference("caption_stage2_time"));
            bindPreferenceSummaryToValue(findPreference("caption_left"));
            bindPreferenceSummaryToValue(findPreference("caption_center"));
            bindPreferenceSummaryToValue(findPreference("caption_right"));
            bindPreferenceSummaryToValue(findPreference("caption2_left"));
            bindPreferenceSummaryToValue(findPreference("caption2_center"));
            bindPreferenceSummaryToValue(findPreference("caption2_right"));
        }
    }
    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ItemPreferenceFragment extends KDSPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_item);
            suspendOnSharedPreferencesChangedEvent(false);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);

            bindPreferenceSummaryToValue(findPreference("item_focused_mark"));
            bindPreferenceSummaryToValue(findPreference("item_bumped"));
            bindPreferenceSummaryToValue(findPreference("item_exp_bumped_in_others"));
            bindPreferenceSummaryToValue(findPreference("item_showing_method"));
            bindPreferenceSummaryToValue(findPreference("item_void"));
            bindPreferenceSummaryToValue(findPreference("item_changed"));

            bindPreferenceSummaryToValue(findPreference("void_showing_method"));
            bindPreferenceSummaryToValue(findPreference("void_dcq_message"));
            bindPreferenceSummaryToValue(findPreference("void_addline_qty_mark"));

            updateGUI(getShowingMethod(pref));

        }
        @Override
        public void onDestroy()
        {
            super.onDestroy();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.unregisterOnSharedPreferenceChangeListener(this);
        }
        private KDSSettings.VoidShowingMethod getShowingMethod( SharedPreferences pref )
        {
            String s = pref.getString("void_showing_method", "0");
            int n = KDSUtil.convertStringToInt(s, 0);
            KDSSettings.VoidShowingMethod m = KDSSettings.VoidShowingMethod.values()[n];
            return m;
        }

        private void updateGUI(KDSSettings.VoidShowingMethod method)
        {
            String[] arDcq = new String[]{

                    "void_dcq_add_message_enabled",
                    "void_dcq_message",
            };
            String[] arVoidLine = new String[]{

                    "void_addline_line_color_enabled",
                    "void_addline_line_bgfg",
                    "void_addline_qty_mark",
            };

            boolean bDCP = (method == KDSSettings.VoidShowingMethod.Direct_Qty);
            enableGui(arDcq, bDCP);
            enableGui(arVoidLine, !bDCP);

        }

        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {
            if (key.equals("void_showing_method"))
            {

                String s = prefs.getString(key, "0");
                int n = KDSUtil.convertStringToInt(s, 0);
                KDSSettings.VoidShowingMethod m = KDSSettings.VoidShowingMethod.values()[n];

                updateGUI(m);

            }
        }
        private void enableGui(String[] ar, boolean bEnabled)
        {
            for(int i=0; i< ar.length; i++) {
                String idkey = ar[i];
                Preference p = findPreference(idkey);
                p.setEnabled(bEnabled);
            }
        }
    }
    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CondimentPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_condiment);
            suspendOnSharedPreferencesChangedEvent(false);


             bindPreferenceSummaryToValue(findPreference("condiment_start_position"));

        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MessagePreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_premsg);
            suspendOnSharedPreferencesChangedEvent(false);


        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class FooterPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_footer);
            suspendOnSharedPreferencesChangedEvent(false);
            bindPreferenceSummaryToValue(findPreference("footer_left"));
            bindPreferenceSummaryToValue(findPreference("footer_center"));
            bindPreferenceSummaryToValue(findPreference("footer_right"));

            // bindPreferenceSummaryToValue(findPreference("item_consolidate"));

        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class BumpingPreferenceFragment extends KDSPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_bumping);
            suspendOnSharedPreferencesChangedEvent(false);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);

             bindPreferenceSummaryToValue(findPreference("bumping_days"));
            bindPreferenceSummaryToValue(findPreference("bumping_by_panelnumber"));
            bindPreferenceSummaryToValue(findPreference("bumping_auto_minutes"));
            bindPreferenceSummaryToValue(findPreference("bumping_max_count"));

            bindPreferenceSummaryToValue(findPreference("bumping_auto_park_minutes"));

        }

        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {

            if (key.equals("bump_double_queue") )
            {
                Boolean b = prefs.getBoolean(key, true);
                KDSGlobalVariables.getKDS().getBroadcaster().broadcastQueueExpoDoubleBumpValue(b);
                if (!b)
                {
                    showDoubleBumpDisabledAlert(); //2.0.12
                }
            }
        }
        /**
         *  In Queue option, when people disable the “double bump”,
         *  show a warning “If you disable this option in Queue station, a Bumpbar is required to bump the order off Queue display”.
         */
        private  void showDoubleBumpDisabledAlert()
        {
            String strOK = KDSUIDialogBase.makeOKButtonText2(KDSApplication.getContext());//.makeButtonText(KDSApplication.getContext(),R.string.ok, KDSSettings.ID.Bumpbar_OK );
            //String strCancel = KDSUIDialogBase.makeButtonText(KDSApplication.getContext(),R.string.cancel, KDSSettings.ID.Bumpbar_Cancel );
            if (this.getActivity() == null) return;
            AlertDialog d = new AlertDialog.Builder(this.getActivity())
                    .setTitle(this.getString(R.string.confirm))
                    .setMessage(this.getString(R.string.alert_disable_double_bump_queue))
                    .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // PreferenceFragmentStations.this.broadcastUpdate();
                                }
                            }
                    )
//                    .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                        }
//                    })
                    .create();
            d.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);

                    if (evID == KDSSettings.ID.Bumpbar_OK)
                    {
                        dialog.dismiss();
                        // PreferenceFragmentStations.this.broadcastUpdate();
                        return true;
                    }
                    else if (evID == KDSSettings.ID.Bumpbar_Cancel)
                    {
                        dialog.cancel();
                        return true;
                    }
                    return false;
                }
            });
            d.setCancelable(false);
            d.setCanceledOnTouchOutside(false);
            d.show();
        }

    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class BumpbarPreferenceFragment extends KDSPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_bumpbar);
            suspendOnSharedPreferencesChangedEvent(false);



            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);
            bindPreferenceSummaryToValue(findPreference("bumpbar_type"));
            // bindPreferenceSummaryToValue(findPreference("item_consolidate"));

        }
        @Override
        public void onDestroy()
        {
            super.onDestroy();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.unregisterOnSharedPreferenceChangeListener(this);
        }
//        @Override
//        public void onActivityCreated(Bundle savedInstanceState) {
//            super.onActivityCreated(savedInstanceState);
//            showScrollbar();
//        }

        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {

            if (key.equals("bumpbar_type"))
            {
                String[] ar = new String[]{

                        "bumpbar_func_ok",
                        "bumpbar_func_cancel",
                        "bumpbar_func_bump",
                        "bumpbar_func_unbump",
                        "bumpbar_func_next",
                        "bumpbar_func_prev",
                        "bumpbar_func_down",
                        "bumpbar_func_up",
                        "bumpbar_func_sum",
                        "bumpbar_func_transfer",
                        "bumpbar_func_sort",
                        "bumpbar_func_park",
                        "bumpbar_func_unpark",
                        "bumpbar_func_print",
                        "bumpbar_func_more",
                        "bumpbar_switch_user"

                };
                for(int i=0; i< ar.length; i++) {
                    String idkey = ar[i];
                    KDSPreferenceKeySelection ks = (KDSPreferenceKeySelection) findPreference(idkey);
                    if (ks != null)
                        ks.refresh_summary();//this.getActivity());
                }
            }

        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrinterPreferenceFragment extends KDSPreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_printer);
            suspendOnSharedPreferencesChangedEvent(false);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);


            bindPreferenceSummaryToValue(findPreference("printer_type"));
            bindPreferenceSummaryToValue(findPreference("printer_port"));
            bindPreferenceSummaryToValue(findPreference("printer_ip"));
            bindPreferenceSummaryToValue(findPreference("printer_ipport"));
            bindPreferenceSummaryToValue(findPreference("printer_serial"));
            bindPreferenceSummaryToValue(findPreference("printer_baudrate"));
            bindPreferenceSummaryToValue(findPreference("printer_width"));
            bindPreferenceSummaryToValue(findPreference("printer_copies"));
            bindPreferenceSummaryToValue(findPreference("printer_codepage"));
            bindPreferenceSummaryToValue(findPreference("printer_howtoprint"));
            bindPreferenceSummaryToValue(findPreference("printer_logo"));

            //bindPreferenceSummaryToValue(findPreference("printer_template"));
            KDSPrinter.PrinterPortType portType =getPortType(pref);
            setupGuiByPortType(portType);

        }
        @Override
        public void onDestroy()
        {
            super.onDestroy();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.unregisterOnSharedPreferenceChangeListener(this);
        }
        private void setupGuiByPortType( KDSPrinter.PrinterPortType portType)
        {
            PreferenceGroup portconfig = ((PreferenceGroup) findPreference("portconfig"));
            if (portType == KDSPrinter.PrinterPortType.USB) {
                findPreference("printer_ip").setEnabled(false);
                findPreference("printer_ipport").setEnabled(false);
                findPreference("printer_serial").setEnabled(false);
                findPreference("printer_baudrate").setEnabled(false);
            }
            else if (portType == KDSPrinter.PrinterPortType.Socket)
            {
                findPreference("printer_serial").setEnabled(false);
                findPreference("printer_baudrate").setEnabled(false);
                findPreference("printer_ip").setEnabled(true);
                findPreference("printer_ipport").setEnabled(true);
            }
            else if (portType == KDSPrinter.PrinterPortType.Serial) {
                findPreference("printer_ip").setEnabled(false);
                findPreference("printer_ipport").setEnabled(false);
                findPreference("printer_serial").setEnabled(false);//make it fixed
                findPreference("printer_baudrate").setEnabled(true);
            }
        }

        private KDSPrinter.PrinterPortType getPortType(SharedPreferences prefs)
        {
            String key = ("printer_port");

            String strPortType = prefs.getString(key, "0");//.getInt(key, 0);
            int nPortType = KDSUtil.convertStringToInt(strPortType, 0);
            KDSPrinter.PrinterPortType portType = KDSPrinter.PrinterPortType.values()[nPortType];
            return portType;
        }

        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {

            if (key.equals("printer_port"))
            {

                KDSPrinter.PrinterPortType portType =getPortType(prefs);
                setupGuiByPortType(portType);


            }
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SmartOrderPreferenceFragment extends KDSPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_smartorder);
            suspendOnSharedPreferencesChangedEvent(false);

            bindPreferenceSummaryToValue(findPreference("smartorder_showing"));
            //bindPreferenceSummaryToValue(findPreference("smart_mode"));

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);

        }
        @Override
        public void onDestroy()
        {
            super.onDestroy();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.unregisterOnSharedPreferenceChangeListener(this);
        }
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {


            //if (key.equals("prepmode_enabled") ) //it is global settings
//            if (key.equals("smart_mode") ) //it is global settings
//            {
//                String s = prefs.getString(key, "0");
//                int n = KDSUtil.convertStringToInt(s, 0);
//                KDSGlobalVariables.getKDS().getBroadcaster().broadcastPrepModeEnabled(n);
//            }
            if (key.equals("smartorder_enabled") ) //it is global settings
            {
                boolean bSmartEnabled = prefs.getBoolean(key, false);
                //int n = KDSUtil.convertStringToInt(s, 0);
                KDSGlobalVariables.getKDS().getBroadcaster().broadcastSmartOrderEnabled(bSmartEnabled);
            }
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LogPreferenceFragment extends KDSPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_log);
            suspendOnSharedPreferencesChangedEvent(false);

            bindPreferenceSummaryToValue(findPreference("log_mode"));
            bindPreferenceSummaryToValue(findPreference("log_days"));

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);

        }
        @Override
        public void onDestroy()
        {
            super.onDestroy();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.unregisterOnSharedPreferenceChangeListener(this);
        }
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {

        }
    }
    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class TitlePreferenceFragment extends KDSPreferenceFragment{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_title);
            suspendOnSharedPreferencesChangedEvent(false);

            bindPreferenceSummaryToValue(findPreference("real_time_period"));
            bindPreferenceSummaryToValue(findPreference("kds_general_title"));

        }

    }
    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class BeeperPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_beeper);
            suspendOnSharedPreferencesChangedEvent(false);

            bindPreferenceSummaryToValue(findPreference("beeper_type"));

        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_notification);
            suspendOnSharedPreferencesChangedEvent(false);



        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class TrackerPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_tracker);
            suspendOnSharedPreferencesChangedEvent(false); //KPP1-138, change from true to false.

            bindPreferenceSummaryToValue(findPreference("tracker_title"));
            bindPreferenceSummaryToValue(findPreference("tracker_cols"));
            bindPreferenceSummaryToValue(findPreference("tracker_cell_height"));
            bindPreferenceSummaryToValue(findPreference("tracker_auto_switch_duration"));
            bindPreferenceSummaryToValue(findPreference("tracker_more_orders_message"));
            bindPreferenceSummaryToValue(findPreference("tracker_alert_not_bump_timeout"));
            bindPreferenceSummaryToValue(findPreference("tracker_auto_remove_after_expo_bump_timeout"));
            bindPreferenceSummaryToValue(findPreference("tracker_auto_assign_timeout"));
            bindPreferenceSummaryToValue(findPreference("tracker_number_from_userinfo_guesttable"));


        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PagerPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_pager);
            suspendOnSharedPreferencesChangedEvent(false);
            bindPreferenceSummaryToValue(findPreference("pager_delay"));
            bindPreferenceSummaryToValue(findPreference("pager_number_from_userinfo_guesttable"));

        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class HighlightPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_highlight);
            suspendOnSharedPreferencesChangedEvent(false);


            //bindPreferenceSummaryToValue(findPreference("beeper_type"));

        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ExpeditorPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_expeditor);
            suspendOnSharedPreferencesChangedEvent(false);


            //bindPreferenceSummaryToValue(findPreference("beeper_type"));

        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SumPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_sum);
            suspendOnSharedPreferencesChangedEvent(false);


            bindPreferenceSummaryToValue(findPreference("sum_type"));
            bindPreferenceSummaryToValue(findPreference("sum_position"));
            bindPreferenceSummaryToValue(findPreference("sum_order_by"));
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MediaPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_media);
            suspendOnSharedPreferencesChangedEvent(false);
            bindPreferenceSummaryToValue(findPreference("media_auto_delay"));
            bindPreferenceSummaryToValue(findPreference("media_default_vol"));
        }
    }


    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class TouchPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_touch);
            suspendOnSharedPreferencesChangedEvent(false);

        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class TransferPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_transfer);
            suspendOnSharedPreferencesChangedEvent(false);

            bindPreferenceSummaryToValue(findPreference("transfer_default_station"));

        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class QueuePreferenceFragment extends KDSPreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_queue);
            suspendOnSharedPreferencesChangedEvent(false);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);
            bindPreferenceSummaryToValue(findPreference("queue_cols"));
            bindPreferenceSummaryToValue(findPreference("queue_cell_height"));
            bindPreferenceSummaryToValue(findPreference("queue_more_orders_message"));
            bindPreferenceSummaryToValue(findPreference("queue_cols"));
            bindPreferenceSummaryToValue(findPreference("queue_order_received_status"));
            bindPreferenceSummaryToValue(findPreference("queue_order_preparation_status"));
            bindPreferenceSummaryToValue(findPreference("queue_order_ready_status"));
            bindPreferenceSummaryToValue(findPreference("queue_order_pickup_status"));


            bindPreferenceSummaryToValue(findPreference("queue_title"));
            bindPreferenceSummaryToValue(findPreference("queue_panel_ratio"));
            bindPreferenceSummaryToValue(findPreference("queue_mode"));
            bindPreferenceSummaryToValue(findPreference("queue_auto_switch_duration"));

            bindPreferenceSummaryToValue(findPreference("queue_simple_combine_status1"));
            bindPreferenceSummaryToValue(findPreference("queue_simple_combine_status2"));
            bindPreferenceSummaryToValue(findPreference("queue_simple_combine_status3"));
            bindPreferenceSummaryToValue(findPreference("queue_simple_combine_status4"));
            bindPreferenceSummaryToValue(findPreference("queue_order_id_length"));

            bindPreferenceSummaryToValue(findPreference("queue_status1_sort"));
            bindPreferenceSummaryToValue(findPreference("queue_status2_sort"));
            bindPreferenceSummaryToValue(findPreference("queue_status3_sort"));
            bindPreferenceSummaryToValue(findPreference("queue_status4_sort"));

            //bindPreferenceSummaryToValue(findPreference("queue_auto_bump_timeout"));

            boolean bEnabled = isEnabled();

            enableWholeQueueSettings(bEnabled);
            if (bEnabled) {
                KDSSettings.QueueMode mode = getQueueMode(pref);
                setupModeGui(mode);
            }

        }
        @Override
        public void onDestroy()
        {
            super.onDestroy();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.unregisterOnSharedPreferenceChangeListener(this);
        }
        private boolean isEnabled()
        {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());

            String key = ("kds_general_id");
            String myStationID = pref.getString(key, "");//.getInt(key, 0);
            if (myStationID.isEmpty())
                return true;

            ArrayList<KDSStationsRelation>  ar = KDSSettings.loadStationsRelation(KDSApplication.getContext(), false);
            for (int i=0; i< ar.size(); i++)
            {
                KDSStationsRelation r = ar.get(i);
                if (r.getID().equals(myStationID))
                {
                    if (r.getFunction() == KDSSettings.StationFunc.Expeditor ||
                            r.getFunction() == KDSSettings.StationFunc.Queue ||
                            r.getFunction() == KDSSettings.StationFunc.Queue_Expo)
                        return true;
                }
            }
            return false;
        }

        private void enablePanelModeSettings(boolean bEnabled) {
            ArrayList<String> ar = new ArrayList<>();
            ar.add("queue_show_finished_at_right");
            ar.add("queue_move_ready_to_front");
            ar.add("queue_panel_ratio");
            for (int i = 0; i < ar.size(); i++) {
                findPreference(ar.get(i)).setEnabled(bEnabled);
            }
        }

        private void enableSimpleModeSettings(boolean bEnabled) {
            ArrayList<String> ar = new ArrayList<>();
            ar.add("queue_simple_show_received_col");
            ar.add("queue_simple_show_preparation_col");
            ar.add("queue_simple_show_ready_col");
            ar.add("queue_simple_show_pickup_col");
            ar.add("queue_simple_separator_color");
            for (int i = 0; i < ar.size(); i++) {
                findPreference(ar.get(i)).setEnabled(bEnabled);
            }
        }

        private void enableWholeQueueSettings(boolean bEnabled)
        {
            ArrayList<String> ar = new ArrayList<>();

            ar.add("queue_title");
            ar.add("queue_mode");
            ar.add("queue_view_bg");
            ar.add("queue_cols");
            ar.add("queue_cell_height");
            ar.add("queue_auto_switch_duration");
            //ar.add("bump_double_queue");
            ar.add("queue_show_order_id");
            ar.add("queue_order_id_font");
            ar.add("queue_show_customer_name");
            ar.add("queue_customer_name_font");
            ar.add("queue_show_order_timer");
            ar.add("queue_order_timer_font");
            ar.add("queue_show_custom_message");
            ar.add("queue_custome_message_font");
            ar.add("queue_order_received_status");
            ar.add("queue_order_received_font");
            ar.add("queue_order_preparation_status");
            ar.add("queue_order_preparation_font");
            ar.add("queue_order_ready_status");
            ar.add("queue_order_ready_font");
            ar.add("queue_order_pickup_status");
            ar.add("queue_order_pickup_font");
            ar.add("queue_flash_ready_order");
            ar.add("queue_more_orders_message");


            ar.add("queue_show_finished_at_right");
            ar.add("queue_move_ready_to_front");
            ar.add("queue_panel_ratio");
            ar.add("queue_simple_show_received_col");
            ar.add("queue_simple_show_preparation_col");
            ar.add("queue_simple_show_ready_col");
            ar.add("queue_simple_show_pickup_col");
            ar.add("queue_simple_separator_color");

            for (int i = 0; i < ar.size(); i++) {
                findPreference(ar.get(i)).setEnabled(bEnabled);
            }
        }
        private KDSSettings.QueueMode getQueueMode(SharedPreferences prefs)
        {
            String key = ("queue_mode");

            String strPortType = prefs.getString(key, "0");//.getInt(key, 0);
            int nMode = KDSUtil.convertStringToInt(strPortType, 0);
            KDSSettings.QueueMode mode = KDSSettings.QueueMode.values()[nMode];
            return mode;
        }
        private void setupModeGui(KDSSettings.QueueMode mode)
        {
            switch (mode)
            {

                case Panels:
                    enablePanelModeSettings(true);
                    enableSimpleModeSettings(false);
                    break;
                case Simple:
                    enablePanelModeSettings(false);
                    enableSimpleModeSettings(true);
                    break;
            }
        }

        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {

            if (key.equals("queue_mode"))
            {

                KDSSettings.QueueMode mode =getQueueMode(prefs);
                setupModeGui(mode);


            }
            else if (key.equals("bump_double_queue") )
            {
                Boolean b = prefs.getBoolean(key, true);
                KDSGlobalVariables.getKDS().getBroadcaster().broadcastQueueExpoDoubleBumpValue(b);
                if (!b)
                {
                    showDoubleBumpDisabledAlert(); //2.0.12
                }
            }
        }

        /**
         *  In Queue option, when people disable the “double bump”,
         *  show a warning “If you disable this option in Queue station, a Bumpbar is required to bump the order off Queue display”.
         */
        private  void showDoubleBumpDisabledAlert()
        {
            String strOK = KDSUIDialogBase.makeOKButtonText2(KDSApplication.getContext());// .makeButtonText(KDSApplication.getContext(),R.string.ok, KDSSettings.ID.Bumpbar_OK );
            //String strCancel = KDSUIDialogBase.makeButtonText(KDSApplication.getContext(),R.string.cancel, KDSSettings.ID.Bumpbar_Cancel );
            if (this.getActivity() == null) return;
            AlertDialog d = new AlertDialog.Builder(this.getActivity())
                    .setTitle(this.getString(R.string.confirm))
                    .setMessage(this.getString(R.string.alert_disable_double_bump_queue))
                    .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                   // PreferenceFragmentStations.this.broadcastUpdate();
                                }
                            }
                    )
//                    .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                        }
//                    })
                    .create();
            d.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);

                    if (evID == KDSSettings.ID.Bumpbar_OK)
                    {
                        dialog.dismiss();
                       // PreferenceFragmentStations.this.broadcastUpdate();
                        return true;
                    }
                    else if (evID == KDSSettings.ID.Bumpbar_Cancel)
                    {
                        dialog.cancel();
                        return true;
                    }
                    return false;
                }
            });
            d.setCancelable(false);
            d.setCanceledOnTouchOutside(false);
            d.show();
        }

    }


       /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     * Queue expo configuration
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class QExpoPreferenceFragment extends KDSPreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_qexpo);
            suspendOnSharedPreferencesChangedEvent(false);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);
            bindPreferenceSummaryToValue(findPreference("bumpbar_type"));


            boolean bEnabled = isEnabled();

            enableWholeQueueSettings(bEnabled);


        }
           @Override
           public void onDestroy()
           {
               super.onDestroy();
               SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
               pref.unregisterOnSharedPreferenceChangeListener(this);
           }
        private boolean isEnabled()
        {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());

            String key = ("kds_general_id");
            String myStationID = pref.getString(key, "");//.getInt(key, 0);
            if (myStationID.isEmpty())
                return true;

            ArrayList<KDSStationsRelation>  ar = KDSSettings.loadStationsRelation(KDSApplication.getContext(), false );
            for (int i=0; i< ar.size(); i++)
            {
                KDSStationsRelation r = ar.get(i);
                if (r.getID().equals(myStationID))
                {
                    if (  r.getFunction() == KDSSettings.StationFunc.Queue_Expo)
                        return true;
                }
            }
            return false;
        }



        private void enableWholeQueueSettings(boolean bEnabled)
        {
            ArrayList<String> ar = new ArrayList<>();

            ar.add("bumpbar_func_bump");
            ar.add("bumpbar_func_unbump");
            ar.add("bumpbar_func_qexpo_ready");
            ar.add("bumpbar_func_qexpo_unready");


            for (int i = 0; i < ar.size(); i++) {
                findPreference(ar.get(i)).setEnabled(bEnabled);
            }
        }


        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {
            if (key.equals("bumpbar_type"))
            {
                String[] ar = new String[]{


                        "bumpbar_func_bump",
                        "bumpbar_func_unbump",
                        "bumpbar_func_qexpo_ready",
                        "bumpbar_func_qexpo_unready",


                };
                for(int i=0; i< ar.length; i++) {
                    String idkey = ar[i];
                    KDSPreferenceKeySelection ks = (KDSPreferenceKeySelection) findPreference(idkey);
                    if (ks != null)
                        ks.refresh_summary();//this.getActivity());
                }
            }

        }
    }


    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     * Queue expo configuration
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LineItemsPreferenceFragment extends KDSPreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_lineitems);
            suspendOnSharedPreferencesChangedEvent(false);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);
            bindPreferenceSummaryToValue(findPreference("lineitems_caption_text"));
            bindPreferenceSummaryToValue(findPreference("lineitems_cols_count"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col0_text"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col0_size"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col0_content"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col1_text"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col1_size"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col1_content"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col2_text"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col2_size"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col2_content"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col3_text"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col3_size"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col3_content"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col4_text"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col4_size"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col4_content"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col5_text"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col5_size"));
            bindPreferenceSummaryToValue(findPreference("lineitems_col5_content"));
            bindPreferenceSummaryToValue(findPreference("lineitems_line_height"));

            enableColsSettings(getColsCount());

        }
        @Override
        public void onDestroy()
        {
            super.onDestroy();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.unregisterOnSharedPreferenceChangeListener(this);
        }
        private int getColsCount()
        {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());

            String key = ("lineitems_cols_size");
            String strColsSize = pref.getString(key, "");//.getInt(key, 0);
            ArrayList<String> ar = KDSUtil.spliteString(strColsSize, ",");
            return ar.size();


        }


        private void enableColsSettings(int nColsCount)
        {
            ArrayList<String> arCol = new ArrayList<>();

            arCol.add("lineitems_col1_text");

            arCol.add("lineitems_col1_content");

            arCol.add("lineitems_col2_text");

            arCol.add("lineitems_col2_content");

            arCol.add("lineitems_col3_text");

            arCol.add("lineitems_col3_content");

            arCol.add("lineitems_col4_text");

            arCol.add("lineitems_col4_content");

            arCol.add("lineitems_col5_text");

            arCol.add("lineitems_col5_content");

            int n = nColsCount - 1;
            n *= 2;
            for (int i =  0; i< arCol.size(); i++)
            {

                findPreference(arCol.get(i)).setEnabled(i < n);
            }


        }

        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {
            if (key.equals("lineitems_cols_size"))
            {
                int ncols = getColsCount();
                enableColsSettings(ncols);
            }

        }
    }



    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SoundPreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_sound);
            suspendOnSharedPreferencesChangedEvent(false);
            bindPreferenceSummaryToValue(findPreference("sound_duration"));


        }
    }

    /**
     * UNUSED
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class IconPreferenceFragment extends KDSPreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            suspendOnSharedPreferencesChangedEvent(true);
            addPreferencesFromResource(R.xml.pref_icon);
            suspendOnSharedPreferencesChangedEvent(false);
            bindPreferenceSummaryToValue(findPreference("icon_0"));
            bindPreferenceSummaryToValue(findPreference("icon_1"));
            bindPreferenceSummaryToValue(findPreference("icon_2"));
            bindPreferenceSummaryToValue(findPreference("icon_3"));
            bindPreferenceSummaryToValue(findPreference("icon_4"));
            bindPreferenceSummaryToValue(findPreference("icon_5"));
            bindPreferenceSummaryToValue(findPreference("icon_6"));
            bindPreferenceSummaryToValue(findPreference("icon_7"));
            bindPreferenceSummaryToValue(findPreference("icon_8"));
            bindPreferenceSummaryToValue(findPreference("icon_9"));
            bindPreferenceSummaryToValue(findPreference("icon_10"));
            bindPreferenceSummaryToValue(findPreference("icon_11"));
            bindPreferenceSummaryToValue(findPreference("icon_12"));
            bindPreferenceSummaryToValue(findPreference("icon_13"));
            bindPreferenceSummaryToValue(findPreference("icon_14"));
            bindPreferenceSummaryToValue(findPreference("icon_15"));
            bindPreferenceSummaryToValue(findPreference("icon_16"));
            bindPreferenceSummaryToValue(findPreference("icon_17"));
            bindPreferenceSummaryToValue(findPreference("icon_18"));
            bindPreferenceSummaryToValue(findPreference("icon_19"));
            bindPreferenceSummaryToValue(findPreference("icon_20"));
            bindPreferenceSummaryToValue(findPreference("icon_21"));
            bindPreferenceSummaryToValue(findPreference("icon_22"));
            bindPreferenceSummaryToValue(findPreference("icon_23"));
            bindPreferenceSummaryToValue(findPreference("icon_24"));
            bindPreferenceSummaryToValue(findPreference("icon_25"));
            bindPreferenceSummaryToValue(findPreference("icon_26"));
            bindPreferenceSummaryToValue(findPreference("icon_27"));
            bindPreferenceSummaryToValue(findPreference("icon_28"));
            bindPreferenceSummaryToValue(findPreference("icon_29"));
            bindPreferenceSummaryToValue(findPreference("icon_30"));
            bindPreferenceSummaryToValue(findPreference("icon_31"));
            bindPreferenceSummaryToValue(findPreference("icon_32"));
            bindPreferenceSummaryToValue(findPreference("icon_folder"));
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);
            boolean b= pref.getBoolean("icon_folder_enabled", false);
            findPreference("icon_folder").setEnabled(b);

        }
        @Override
        public void onDestroy()
        {
            super.onDestroy();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.unregisterOnSharedPreferenceChangeListener(this);
        }
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {
            if (key.equals("icon_folder_enabled"))
            {
                boolean b= prefs.getBoolean(key, false);
                findPreference("icon_folder").setEnabled(b);
            }

            String ar[] = new String[]{
                    "icon_enabled",
                    "icon_folder_enabled",
                    "icon_folder",
                    "icon_0", "icon_1", "icon_2", "icon_3", "icon_4", "icon_5", "icon_6", "icon_7", "icon_8", "icon_9",
                    "icon_10", "icon_11", "icon_12", "icon_13", "icon_14", "icon_15", "icon_16", "icon_17", "icon_18", "icon_19",
                    "icon_20", "icon_21", "icon_22", "icon_23", "icon_24", "icon_25", "icon_26", "icon_27", "icon_28", "icon_29",
                    "icon_30", "icon_31", "icon_32",

            };
            for (int i=0; i< ar.length; i++)
            {
                 if (key.equals(ar[i]) )
                {
                    KDSGlobalVariables.getKDS().getSettings().resetBufferedIcons();
                    break;
                }
            }

        }
    }

    /*****************************************************************************8
     *   >>>>>>>>>>>>>>>>>>>>>. IMPORTANT <<<<<<<<<<<<<<<<<<<<<<<<<
     *   This functions replace the existed headers.
     *    The category headers:
     *      Just set the fragment to null (don't set it).
     *
     * @param adapter
     */
    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (adapter == null) {
            super.setListAdapter(null);
        } else {
            try {
                Class<KDSUIConfiguration> c = KDSUIConfiguration.class;
                Method method = c.getMethod("getHeaders");
                method.setAccessible(true);
                Object objParam = null;
                List<Header> obj = (List<Header>) method.invoke(this);//, null);

                super.setListAdapter(new HeaderAdapter(this, obj));//, mAuthenticatorHelper, dpm));
            }
            catch (Exception err)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() , err);
                //KDSLog.e(TAG, KDSUtil.error( err));
            }
        }
    }

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        static final int HEADER_TYPE_CATEGORY = 0;
        static final int HEADER_TYPE_NORMAL = 1;
        //static final int HEADER_TYPE_SWITCH = 2;
        //static final int HEADER_TYPE_BUTTON = 3;
        private static final int HEADER_TYPE_COUNT = HEADER_TYPE_NORMAL + 1;

        //        private final WifiEnabler mWifiEnabler;
//        private final BluetoothEnabler mBluetoothEnabler;
//        private AuthenticatorHelper mAuthHelper;
        private DevicePolicyManager mDevicePolicyManager;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView title;
            TextView summary;
            Switch switch_;
            ImageButton button_;
            View divider_;
        }

        private LayoutInflater mInflater;

        static int getHeaderType(Header header) {
            if (header.fragment == null && header.intent == null) {
                return HEADER_TYPE_CATEGORY;
            }
//            else if (header.id == R.id.wifi_settings || header.id == R.id.bluetooth_settings) {
//                return HEADER_TYPE_SWITCH;
//            }
//            else if (header.id == R.id.security_settings) {
//                return HEADER_TYPE_BUTTON;
//            }
            else {
                return HEADER_TYPE_NORMAL;
            }
        }

        @Override
        public int getItemViewType(int position) {
            Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false; // because of categories
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != HEADER_TYPE_CATEGORY;
        }

        @Override
        public int getViewTypeCount() {
            return HEADER_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public HeaderAdapter(Context context, List<Header> objects){
            // AuthenticatorHelper authenticatorHelper, DevicePolicyManager dpm) {
            super(context, 0, objects);

            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
            int headerType = getHeaderType(header);
            View view = null;

            if (convertView == null) {
                holder = new HeaderViewHolder();
                switch (headerType) {
                    case HEADER_TYPE_CATEGORY:
                        view = new TextView(getContext(), null,
                                android.R.attr.listSeparatorTextViewStyle);
                        holder.title = (TextView) view;
                        holder.title.setTextColor(Color.BLACK);
                        break;



                    case HEADER_TYPE_NORMAL:
                        view = mInflater.inflate(
                                R.layout.preference_header_item, parent,
                                false);
                        holder.icon = (ImageView) view.findViewById(R.id.icon);
                        holder.title = (TextView)
                                view.findViewById(R.id.title);
                        holder.summary = (TextView)
                                view.findViewById(R.id.summary);
                        break;
                }
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }

            // All view fields must be updated every time, because the view may be recycled
            switch (headerType) {
                case HEADER_TYPE_CATEGORY:
                    holder.title.setText(header.getTitle(getContext().getResources()));

                    break;


                case HEADER_TYPE_NORMAL:
                    updateCommonHeaderView(header, holder);
                    break;
            }

            return view;
        }

        private void updateCommonHeaderView(Header header, HeaderViewHolder holder) {

            {
                holder.icon.setImageResource(header.iconRes);
            }
            holder.title.setText(header.getTitle(getContext().getResources()));
            CharSequence summary = header.getSummary(getContext().getResources());
            if (!TextUtils.isEmpty(summary)) {
                holder.summary.setVisibility(View.VISIBLE);
                holder.summary.setText(summary);
            } else {
                holder.summary.setVisibility(View.GONE);
            }
        }




    }

}
