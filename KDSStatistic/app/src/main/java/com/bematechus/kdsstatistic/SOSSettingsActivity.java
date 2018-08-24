package com.bematechus.kdsstatistic;


import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSUtil;

import java.lang.reflect.Method;
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
public class SOSSettingsActivity extends PreferenceActivity{// AppCompatPreferenceActivity {
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

            }
            else {
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
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setupActionBar();
        int color = this.getResources().getColor(R.color.settings_page_bg);//.getDrawable(R.drawable.bkcolor);
        ColorDrawable c = new ColorDrawable(color);
        this.getWindow().setBackgroundDrawable(c);
    }

    static final private int HEADERS_WIDTH = 200;
    @Override
    public void onResume()
    {
        super.onResume();
//        if (isDirty()) {
//            //this.recreate();
//            setPrefFlag();
//            // this.finish();
//        }
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

//        boolean bHide = (KDSGlobalVariables.getKDS().getSettings().getBoolean(KDSSettings.ID.Hide_navigation_bar));
//        hideNavigationBar(bHide);


    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            // Show the Up button in the action bar.
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
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
        loadHeadersFromResource(R.xml.sos_pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return true;
//        return PreferenceFragment.class.getName().equals(fragmentName)
//                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
////                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
////                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener , KDSUIDialogBase.KDSDialogBaseListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.sos_pref_general);
            setHasOptionsMenu(true);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("graph_axis_x_title"));
            bindPreferenceSummaryToValue(findPreference("graph_axis_y_title"));
            bindPreferenceSummaryToValue(findPreference("graph_duration"));
            bindPreferenceSummaryToValue(findPreference("real_time_period"));
//            bindPreferenceSummaryToValue(findPreference("kds_general_language"));
            bindPreferenceSummaryToValue(findPreference("kds_general_title"));
            bindPreferenceSummaryToValue(findPreference("last_data_replace_zero"));
//            bindPreferenceSummaryToValue(findPreference("example_list"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SOSSettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {


            if (key.equals("kds_general_language"))
            {
                int n = SOSKDSGlobalVariables.getKDS().getSettings().getInt(SOSSettings.ID.Language);
                String s = prefs.getString(key, "0");
                int nNew = KDSUtil.convertStringToInt(s, 0);
                if (n == nNew) return;
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



    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LogPreferenceFragment extends KDSPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.sos_pref_log);


            bindPreferenceSummaryToValue(findPreference("log_mode"));
            bindPreferenceSummaryToValue(findPreference("log_days"));

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
            pref.registerOnSharedPreferenceChangeListener(this);

        }
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {

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
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            showScrollbar();
        }
        public void showScrollbar()
        {
            try {

                Class<PreferenceFragment> c = PreferenceFragment.class;
                Method method = c.getMethod("getListView");
                method.setAccessible(true);
                Object obj = method.invoke(this);
                if (obj != null) {
                    ListView listView = (ListView) obj;// method.invoke(this);//, null);
                    listView.setScrollBarFadeDuration(0);
                    listView.setScrollbarFadingEnabled(false);
                    //listView.setFastScrollAlwaysVisible(true);
                    //listView.setNestedScrollingEnabled(false);
                }
//                }
            }
            catch (Exception err)
            {//don't care this bug.
                //KDSLog.e(TAG,KDSUtil._FUNCLINE_() + err.toString());
                //KDSLog.e(TAG, KDSUtil.error( err));
            }
        }
    }
}
