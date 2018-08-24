package com.bematechus.kdsstatistic;


import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSUtil;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class GenActivitySettings extends PreferenceActivity  implements SharedPreferences.OnSharedPreferenceChangeListener {
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
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
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


    }

    static final private int HEADERS_WIDTH = 200;
    @Override
    public void onResume()
    {
        super.onResume();
        if (isDirty()) {

            setPrefFlag();

        }
        this.getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        DisplayMetrics dm = null;// new DisplayMetrics();
        dm = getResources().getDisplayMetrics();

        int screenWidth  = dm.widthPixels;      // 屏幕宽（像素，如：480px）
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

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
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
        loadHeadersFromResource(R.xml.gen_pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return  true;
    }

//    /**
//     * This fragment shows general preferences only. It is used when the
//     * activity is showing a two-pane settings UI.
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public static class GeneralPreferenceFragment extends PreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener , KDSUIDialogBase.KDSDialogBaseListener{
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            addPreferencesFromResource(R.xml.st_pref_general);
//            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
//            pref.registerOnSharedPreferenceChangeListener(this);
//            bindPreferenceSummaryToValue(findPreference("general_auto_report_type"));
//            bindPreferenceSummaryToValue(findPreference("tcp_port"));
//            bindPreferenceSummaryToValue(findPreference("general_auto_time"));
//            bindPreferenceSummaryToValue(findPreference("general_auto_timeslot_from"));
//            bindPreferenceSummaryToValue(findPreference("general_auto_timeslot_to"));
//            bindPreferenceSummaryToValue(findPreference("general_station_from"));
//            bindPreferenceSummaryToValue(findPreference("general_station_to"));
//            bindPreferenceSummaryToValue(findPreference("kds_general_language"));
//
//
//        }
//        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
//        {
//
//            if (key.equals("kds_general_language"))
//            {
//                int n = STKDSGlobalVariables.getKDS().getSettings().getInt(STKDSStatisticSettings.ID.Language);
//                String s = prefs.getString(key, "0");
//                int nNew = KDSUtil.convertStringToInt(s, 0);
//                if (n == nNew) return;
//                doLanguageChanged(prefs, key);
//                return;
//            }
//
//        }
//        public void onKDSDialogCancel(KDSUIDialogBase dialog) {
//
//
//            int n = STKDSGlobalVariables.getKDS().getSettings().getInt(STKDSStatisticSettings.ID.Language);
//            ((ListPreference) findPreference("kds_general_language")).setValueIndex(n);
//            ((ListPreference) findPreference("kds_general_language")).setSummary(STKDSStatisticSettings.getLanguageString(STKDSStatisticSettings.Language.values()[n]));
//
//        }
//
//        public void onKDSDialogOK(KDSUIDialogBase dlg, Object obj)// ArrayList<String> stations)
//        {
//            restartApp();
//        }
//
//
//        public void restartApp()
//        {
//            Intent intent =KDSApplication.getContext().getPackageManager()
//                    .getLaunchIntentForPackage(KDSApplication.getContext().getPackageName());
//            PendingIntent restartIntent = PendingIntent.getActivity(KDSApplication.getContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
//            AlarmManager mgr = (AlarmManager)this.getActivity().getSystemService(Context.ALARM_SERVICE);
//            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
//            System.exit(0);
//        }
//
//        private void doLanguageChanged(SharedPreferences prefs, String key)
//        {
//            String info = KDSApplication.getContext().getString(R.string.restart_kds_as_language);
//            String s = prefs.getString(key, "0");
//            int n = KDSUtil.convertStringToInt(s, 0);
//            STKDSStatisticSettings.Language lan = STKDSStatisticSettings.Language.values()[n];
//            String strLan = STKDSStatisticSettings.getLanguageString(lan);
//
//            info = info.replace("#", strLan);
//            KDSUIDialogBase d = new KDSUIDialogBase();
//            d.createOkCancelDialog(this.getActivity(),
//                    0,
//                    this.getString(R.string.confirm),
//                    info, false, this);
//            d.show();
//        }
//    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LogPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  , KDSUIDialogBase.KDSDialogBaseListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.gen_pref_log);
            bindPreferenceSummaryToValue(findPreference("kds_general_language"));

            bindPreferenceSummaryToValue(findPreference("log_mode"));
            bindPreferenceSummaryToValue(findPreference("log_days"));

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);

        }
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {
            if (key.equals("kds_general_language"))
            {
//                int n = SOSKDSGlobalVariables.getKDS().getSettings().getInt(SOSSettings.ID.Language);
//                String s = prefs.getString(key, "0");
//                int nNew = KDSUtil.convertStringToInt(s, 0);
//                if (n == nNew) return;
                doLanguageChanged(prefs, key);
                return;
            }

        }

        private void doLanguageChanged(SharedPreferences prefs, String key)
        {
            String info = KDSApplication.getContext().getString(R.string.restart_kds_as_language);
            String s = prefs.getString(key, "0");
            int n = KDSUtil.convertStringToInt(s, 0);
            SOSSettings.Language lan = SOSSettings.Language.values()[n];
            String strLan = SOSSettings.getLanguageString(lan);

            info = info.replace("#", strLan);
            KDSUIDialogBase d = new KDSUIDialogBase();
            d.createOkCancelDialog( this.getActivity(),
                    null,
                    this.getString(R.string.confirm),
                    info, false, this);
            d.show();
        }

        public void onKDSDialogCancel(KDSUIDialogBase dialog) {

            int n = SOSKDSGlobalVariables.getKDS().getSettings().getInt(SOSSettings.ID.Language);

            ((ListPreference) findPreference("kds_general_language")).setValueIndex(n);
            ((ListPreference) findPreference("kds_general_language")).setSummary(SOSSettings.getLanguageString(SOSSettings.Language.values()[n]));

        }

        public void onKDSDialogOK(KDSUIDialogBase dlg, Object obj)
        {
            restartApp();

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

    }

//    /**
//     * This fragment shows data and sync preferences only. It is used when the
//     * activity is showing a two-pane settings UI.
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public static class DatabasePreferenceFragment extends PreferenceFragment {
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            addPreferencesFromResource(R.xml.pref_database);
//            setHasOptionsMenu(true);
//
//            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
//            // to their values. When their values change, their summaries are
//            // updated to reflect the new value, per the Android Design
//            // guidelines.
//            bindPreferenceSummaryToValue(findPreference("database_keep"));
//        }
//
//    }

}
