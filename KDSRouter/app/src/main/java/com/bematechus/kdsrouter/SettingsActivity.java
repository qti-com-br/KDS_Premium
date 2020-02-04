package com.bematechus.kdsrouter;


import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSEditTextPreference;
import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSSmbFile;
import com.bematechus.kdslib.KDSSmbFile2;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSTimer;
import com.bematechus.kdslib.KDSUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.SmbFile;

/**
 *
 */
public class SettingsActivity extends PreferenceActivity  implements SharedPreferences.OnSharedPreferenceChangeListener {
    final static String TAG = "SEttingsActivity";

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {

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

                    preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
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
        if (preference == null) return;
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setupActionBar();


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


    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
    {


        if (key.equals("general_router_enabled"))
        {
            setupGui();

        }
        else if (key.equals("general_remote_folder"))
        {
        }
        else if (key.equals("general_enable_smbv2"))
        {
            boolean bEnableSmbV2 =  prefs.getBoolean(key, false);
            KDSSmbFile.smb_setEnableSmbV2(bEnableSmbV2);
        }

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
        this.getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        DisplayMetrics dm = null;// new DisplayMetrics();
        dm = getResources().getDisplayMetrics();

        if (isXLargeTablet(this)){

            Resources mResources = Resources.getSystem();  //getResources()测试也可以
            int id = mResources.getIdentifier("headers", "id", "android");

            LinearLayout layoutHeaders = (LinearLayout) this.findViewById(id);//com.android.internal. R.id.headers);// com.android.internal.R.id.headers);
            layoutHeaders.setLayoutParams(new LinearLayout.LayoutParams(HEADERS_WIDTH, LinearLayout.LayoutParams.MATCH_PARENT));
            layoutHeaders.setBackgroundColor(this.getResources().getColor(R.color.settings_headers_bg));

            id = mResources.getIdentifier("prefs_frame", "id", "android");
            LinearLayout layoutPrefs = (LinearLayout) this.findViewById(id);//com.android.internal.R.id.prefs_frame);
            layoutPrefs.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));


            layoutPrefs.setBackgroundColor(this.getResources().getColor(R.color.settings_page_bg));

        }
        setupGui();
    }

//    Object m_objPrefFilter = null;
//    Object m_objPrefRelations = null;
//    Object m_objPrefLog = null;

    ArrayList<Object> m_pagesBuffered = new ArrayList<>();

//    final int FILTER_PAGE_INDEX = 1;
//    final int RELATION_PAGE_INDEX = 2;
//    final int LOG_PAGE_INDEX = 3;

    /**
     * rev.
     *  2.0.9, change the enable/disable way, don't care each page. save them to a array.
     */
    public void setupGui()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        boolean bEnabled = pref.getBoolean("general_router_enabled", false);


        if (bEnabled)
        {
            try {
                Class<SettingsActivity> c = SettingsActivity.class;
                Method method = c.getMethod("getHeaders");
                method.setAccessible(true);
                Object objParam = null;
                List<Header> list = (List<Header>) method.invoke(this);//, null);
                //2.0.9
                if (m_pagesBuffered.size() >0)
                {
                    for (int i=0; i< m_pagesBuffered.size(); i++)
                    {
                        if (m_pagesBuffered.get(i) != null)
                            list.add((Header) m_pagesBuffered.get(i));
                    }
                }
                m_pagesBuffered.clear();
//                if (m_objPrefFilter != null) {
//                    obj.add((Header) m_objPrefFilter);
//                    m_objPrefFilter = null;
//                }
//                if (m_objPrefRelations != null) {
//                    obj.add((Header) m_objPrefRelations);
//                    m_objPrefRelations = null;
//                }
                ((ArrayAdapter)this.getListView().getAdapter()).notifyDataSetChanged();

            }
            catch (Exception err)
            {
                KDSLog.e(TAG, KDSLog._FUNCLINE_(),err);// + err.toString());
                //KDSLog.e(TAG, KDSUtil.error( err));
            }
        }
        else
        {
            try {
                Class<SettingsActivity> c = SettingsActivity.class;
                Method method = c.getMethod("getHeaders");
                method.setAccessible(true);

                List<Header> list = (List<Header>) method.invoke(this);//, null);
                //2.0.9
                for (int i=1; i< list.size(); i++)
                {
                    m_pagesBuffered.add(list.get(i));
                }
//                m_objPrefFilter = list.get(FILTER_PAGE_INDEX);
//                m_objPrefRelations = list.get(RELATION_PAGE_INDEX);
//                m_objPrefLog = list.get(LOG_PAGE_INDEX);
                for (int i = list.size()-1; i>0; i--) {
                    list.remove(i);

                }
                ((ArrayAdapter)this.getListView().getAdapter()).notifyDataSetChanged();


            }
            catch (Exception err)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() ,err);//+ err.toString());
                //KDSLog.e(TAG, KDSUtil.error( err));
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (RelationsPreferenceFragment.m_stationsRelations != null)
        {
            if (RelationsPreferenceFragment.m_stationsRelations.onKeyDown(keyCode, event))
                return true;
        }
        boolean b = super.onKeyDown(keyCode, event);

        return b;

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {

        boolean b= super.onKeyUp(keyCode, event);
        KDSKbdRecorder.convertKeyEvent(keyCode, event);
        return b;

    }
    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return  true;
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends KDSPreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener, KDSUIDialogBase.KDSDialogBaseListener, KDSTimer.KDSTimerInterface {
        KDSTimer m_timer = new KDSTimer();
        boolean m_bDisableChangedEvent = false;

        public void onTime()
        {
            KDSGlobalVariables.getKDSRouter().broadcastAskRoutersInThread();
            checkIfOtherRouterEnabled();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
            pref.registerOnSharedPreferenceChangeListener(this);

            bindPreferenceSummaryToValue(findPreference("general_station_id"));
            bindPreferenceSummaryToValue(findPreference("general_data_source"));
            bindPreferenceSummaryToValue(findPreference("general_pos_ipport"));

            bindPreferenceSummaryToValue(findPreference("general_connect_station_ipport"));

            bindPreferenceSummaryToValue(findPreference("general_default_tostation"));
            bindPreferenceSummaryToValue(findPreference("general_router_primary"));
            bindPreferenceSummaryToValue(findPreference("general_router_slave"));
            bindPreferenceSummaryToValue(findPreference("kds_general_language"));
            bindPreferenceSummaryToValue(findPreference("notification_minutes"));

            KDSRouterSettings.KDSDataSource dataType =getDataSourceType(pref);

            setupGuiByDataSourceType(dataType);

            m_timer.setReceiver(this);
            m_timer.start(this.getActivity(), this, 2000);
            boolean m_bDisableChangedEvent = false;

        }
        @Override
        public void onDestroy()
        {
            super.onDestroy();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.unregisterOnSharedPreferenceChangeListener(this);
        }
        private KDSRouterSettings.KDSDataSource getDataSourceType(SharedPreferences prefs)
        {
            String key = ("general_data_source");

            String strDataType = prefs.getString(key, "0");//.getInt(key, 0);
            int nDataType = KDSUtil.convertStringToInt(strDataType, 0);
            KDSRouterSettings.KDSDataSource dataType =  KDSRouterSettings.KDSDataSource.values()[nDataType];
            return dataType;
        }
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {
            if ( m_bDisableChangedEvent ) return;

            if (key.equals("general_data_source"))
            {

                KDSRouterSettings.KDSDataSource portType =getDataSourceType(prefs);
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
//            else if (key.equals("kds_general_enable_password"))
//            {
//
//
//                boolean bEnabled  = prefs.getBoolean(key, true);//.getInt(key, 0);
//                //if (!bNewValue)
//                {
//                    if (this.getActivity() == null) return;
//                    KDSUIDlgInputPassword dlg = new KDSUIDlgInputPassword(this.getActivity(), this, bEnabled);
//                    dlg.getDialog().setCancelable(false);//.setFinishOnTouchOutside(false);
//                    dlg.getDialog().setCanceledOnTouchOutside(false);
//                    //dlg.setDisableCancelButton(true);
//                    dlg.show();
//                }
//
//            }
            else if (key.equals("general_router_enabled"))
            {
                boolean bNewValue  = prefs.getBoolean(key, false);


                    if (bNewValue) {
                        checkIfOtherRouterEnabled();
                    }

            }
            else if (key.equals("general_router_backup"))
            {
                if (prefs.getBoolean(key, true))
                {
                    ((KDSEditTextPreference)findPreference("general_router_primary")).onClick();;
                }
            }

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
            d.show();
        }

        public void checkIfOtherRouterEnabled()
        {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            boolean asBackup = pref.getBoolean("general_router_backup", false);
            if (asBackup) return;
            if (KDSGlobalVariables.getKDSRouter().routerOtherEnabled())
            {
                if (!this.isEnabled())
                    return;
                setEnabled(false);
                showOtherRouterEnabledError();


            }
        }
        public void  showOtherRouterEnabledError()
        {
            String strError = this.getString(R.string.error_other_router_enabled);
            String strOK = this.getString(R.string.ok);
            AlertDialog d = new AlertDialog.Builder(this.getActivity())
                    .setTitle(this.getString(R.string.error))
                    .setMessage(strError)
                    .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }
                    )

                    .create();
            d.show();
        }

        private void setEnabled(boolean bEnabled)
        {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());//.getSharedPreferences(SyncStateContract.Constants.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("general_router_enabled", bEnabled);
            editor.apply();
            editor.commit();
            ((SwitchPreference)findPreference("general_router_enabled")).setChecked(bEnabled);
        }
        public boolean isEnabled()
        {
            if (this == null) return false;
            if (this.getActivity() == null) return false;

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            boolean bEnabled = pref.getBoolean("general_router_enabled", false);
            return bEnabled;
        }

//        public void setPassword(String strpwd)
//        {
//            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
//            SharedPreferences.Editor editor = pref.edit();
//            editor.putString("kds_general_password", strpwd);
//            editor.apply();
//            editor.commit();
//        }

        public void onKDSDialogCancel(KDSUIDialogBase dialog) {
            if (dialog instanceof KDSUIDlgInputPassword ) {
//                m_bDisableChangedEvent = true;
//                ((CheckBoxPreference) findPreference("kds_general_enable_password")).setChecked(false);
//                setPassword("");
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

        public void onKDSDialogOK(KDSUIDialogBase dlg, Object obj)// ArrayList<String> stations)
        {
            if (dlg instanceof KDSUIDlgInputPassword) {
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
//                else {
//
//                    String settingsPwd = KDSGlobalVariables.getKDS().getSettings().getString(KDSSettings.ID.Settings_password);
//                    if (settingsPwd.isEmpty())
//                        settingsPwd = KDSConst.DEFAULT_PASSWORD;// "123";
//                    if (pwd.isEmpty() || (!pwd.equals(settingsPwd))) {
//                        KDSUIDialogBase errordlg = new KDSUIDialogBase();
//                        errordlg.createInformationDialog(this.getActivity(), this.getActivity().getString(R.string.error), this.getActivity().getString(R.string.password_incorrect), false);
//                        errordlg.show();
//                        m_bDisableChangedEvent = true;
//                        ((CheckBoxPreference) findPreference("kds_general_enable_password")).setChecked(true);
//                        m_bDisableChangedEvent = false;
//
//
//                        //KDSUtil.showErrorMessage(this, this.getString(R.string.password_incorrect));
//                    } else if (pwd.equals(settingsPwd)) {
//                        ((KDSEditTextPreference) findPreference("kds_general_password")).setText("");//.setChecked(true);
//                        if (!((CheckBoxPreference) findPreference("kds_general_enable_password")).isChecked())
//                            setPassword(""); //2.0.14
//                    }
//                }
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


        public void restartApp()
        {
            Intent intent =KDSApplication.getContext().getPackageManager()
                    .getLaunchIntentForPackage(KDSApplication.getContext().getPackageName());
            PendingIntent restartIntent = PendingIntent.getActivity(KDSApplication.getContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager mgr = (AlarmManager)this.getActivity().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
            System.exit(0);
        }

        private void setupGuiByDataSourceType( KDSRouterSettings.KDSDataSource srcType)
        {

            if (srcType ==  KDSRouterSettings.KDSDataSource.TCPIP) {
                findPreference("general_pos_ipport").setEnabled(true);
                findPreference("general_remote_folder").setEnabled(false);
                findPreference("general_enable_smbv2").setEnabled(false);

            }
            else if (srcType ==  KDSRouterSettings.KDSDataSource.Folder)
            {
                findPreference("general_pos_ipport").setEnabled(false);
                findPreference("general_remote_folder").setEnabled(true);
                findPreference("general_enable_smbv2").setEnabled(true);
            }

        }

    }

//    /**
//     * This fragment shows notification preferences only. It is used when the
//     * activity is showing a two-pane settings UI.
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public static class NotificationPreferenceFragment extends KDSPreferenceFragment {
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            addPreferencesFromResource(R.xml.pref_relations);
//            setHasOptionsMenu(true);
//
//
//        }
//
//        @Override
//        public boolean onOptionsItemSelected(MenuItem item) {
//            int id = item.getItemId();
//            if (id == android.R.id.home) {
//                startActivity(new Intent(getActivity(), SettingsActivity.class));
//                return true;
//            }
//            return super.onOptionsItemSelected(item);
//        }
//    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DatabasePreferenceFragment extends KDSPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_database);
            setHasOptionsMenu(true);

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

            addPreferencesFromResource(R.xml.pref_log);


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
     * PreferenceFragmentStations: is the class copy from KDS app.
     *                We just extent it, override the special function KDSRouter needed.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class RelationsPreferenceFragment extends PreferenceFragmentStations {
        @Override
        public void onReceivedStationAnnounce(KDSStationIP stationReceived){//String stationID, String ip, String port, String mac) {



            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());//this.getActivity().getApplicationContext());
            String routerRelationPort = pref.getString("general_backup_ipport", "4001");
            if (stationReceived.getPort().equals(routerRelationPort))
                return;

            super.onReceivedStationAnnounce(stationReceived);//stationID, ip, port, mac);

        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class KDSPreferenceFragment extends PreferenceFragment  {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
            View v = super.onCreateView(inflater, root, savedInstanceState);

            v.setBackgroundColor(this.getResources().getColor(R.color.settings_page_bg));
            v.setPadding(0,0,0,0);


            return v;
        }


    }


}
