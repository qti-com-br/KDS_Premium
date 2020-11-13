package com.bematechus.kds;

import android.app.Activity;
import android.app.AlertDialog;
//import android.content.ComponentName;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.ServiceConnection;
import android.content.IntentFilter;
import android.content.SharedPreferences;
//import android.graphics.drawable.GradientDrawable;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbManager;
//import android.net.Uri;
import android.os.AsyncTask;
//import android.os.Environment;
import android.os.Build;
import android.os.Handler;
//import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.os.Bundle;
//import android.util.Log;
import android.support.annotation.MainThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;


//import android.view.ViewGroup;
import android.view.WindowManager;

import android.widget.FrameLayout;

import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.ListPopupWindow;

import android.widget.PopupMenu;

import android.widget.TextView;
import android.widget.Toast;

import com.bematechus.kdslib.Activation;
import com.bematechus.kdslib.ActivationRequest;
import com.bematechus.kdslib.ActivityLogin;
import com.bematechus.kdslib.CSVStrings;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSBase;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDBBase;
import com.bematechus.kdslib.KDSDataCategoryIndicator;
import com.bematechus.kdslib.KDSDataFromPrimaryIndicator;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataMoreIndicator;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSLogOrderFile;
import com.bematechus.kdslib.KDSPosNotificationFactory;
import com.bematechus.kdslib.KDSSMBDataSource;
import com.bematechus.kdslib.KDSSMBPath;
import com.bematechus.kdslib.KDSSmbFile;
import com.bematechus.kdslib.KDSSocketManager;
import com.bematechus.kdslib.KDSStationActived;
import com.bematechus.kdslib.KDSStationConnection;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSStationsRelation;
import com.bematechus.kdslib.KDSTimer;
import com.bematechus.kdslib.KDSToStations;
import com.bematechus.kdslib.KDSUIAboutDlg;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUIDialogConfirm;
import com.bematechus.kdslib.KDSUIDlgAgreement;
import com.bematechus.kdslib.KDSUIDlgInputPassword;
import com.bematechus.kdslib.KDSUIIPSearchDialog;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;
import com.bematechus.kdslib.KDSXMLParserCommand;
import com.bematechus.kdslib.ScheduleProcessOrder;
import com.bematechus.kdslib.SettingsBase;
import com.bematechus.kdslib.TimeDog;
//import com.google.android.gms.appindexing.Action;
//import com.google.android.gms.appindexing.AppIndex;
//import com.google.android.gms.common.api.GoogleApiClient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static com.bematechus.kdslib.KDSApplication.getContext;
import static com.bematechus.kdslib.KDSUtil.showMsg;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;


public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener,
        KDSTimer.KDSTimerInterface,
        KDS.KDSEvents,
        KDSUIDialogBase.KDSDialogBaseListener,
        KDSLayout.KDSLayoutEvents,
        MainActivityFragment.OnTouchPadEventListener,
        KDSRefreshHandler.KDSRefreshEventReceiver,
        TTView.TTView_Event,
        KDSDBBase.DBEvents,
        TabDisplay.TabDisplayEvents,
        Activation.ActivationEvents,
        SysTimeChangedReceiver.sysTimeChangedEvent,
        KDSUIAboutDlg.AboutDlgEvent,
        KDSContextMenu.OnContextMenuItemClickedReceiver,
        KDSDlgOrderZoom.ZoomViewEvents

{
    public enum GUI_MODE
    {
        KDS,
        Queue,
        Tracker,

    }
    final static String TAG = "MainActivity";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    SysTimeChangedReceiver m_sysTimeChangedReceiver = new SysTimeChangedReceiver(this);

    KDSRefreshHandler m_refreshHandler = new KDSRefreshHandler(this);

    @Override
    public void onStart() {
        super.onStart();
        init_user_screen_gui_variables();
/*
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web guest_paging content that matches this app activity's content,
                // make sure this auto-generated web guest_paging URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.bematechus.kds/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
        */
    }

    @Override
    public void onStop() {
        super.onStop();

        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Enter");
        /*
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web guest_paging content that matches this app activity's content,
                // make sure this auto-generated web guest_paging URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.bematechus.kds/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
*/
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Exit");
    }

    public enum Confirm_Dialog {

        Clear_DB,
        Export_Data,
        Import_Data,
        Reset_TT_Authen,
        Restart_me,
        Load_Old_Data,
        CONFIRM_BUMP,
        Logout,
    }


    KDSMainUIMessageHandler m_handlerMessage = new KDSMainUIMessageHandler();

    KDSTimer m_timer = new KDSTimer();
    FrameLayout m_flSummary = null;
    FrameLayout m_flSummaryA = null;

    boolean m_bPaused = false;

    //View m_layoutTitle = null;
    //TextView m_txtPrev = null;
    //TextView m_txtNext = null;
    //TextView m_txtTitle = null;
    //TextView m_imgLogo = null;

    //TextView m_txtPrevB = null;
    //TextView m_txtNextB = null;

    //TextView m_txtParkA = null;
    //TextView m_txtParkB = null;

    //TextView m_txtAvgTimeA = null;
    //TextView m_txtAvgTimeB = null;


    KDSUserUI m_uiUserA = new KDSUserUI();
    KDSUserUI m_uiUserB = new KDSUserUI();

    //ImageView m_imgState = null;

    TextView m_txtTime = null;
    TextView m_txtDate = null;
    //ImageView m_imgMenu = null;
    boolean m_bEnableRefreshTimer = true;

    KDSKbdRecorder m_kbdRecorder = new KDSKbdRecorder();

    View m_layoutKDSContainer = null;
    View m_layoutQueue = null;
    QueueView m_queueView = null;

    View m_layoutTT = null;
    TTView m_ttView = null;

    TabDisplay m_tabDisplay = new TabDisplay();


    TimeDog m_tdAutoBackup = new TimeDog();

    LinearLayout m_layoutKdsViews = null;

    Activation m_activation = new Activation(this);

    CleaningHabitsManager m_cleaning = new CleaningHabitsManager();
    /**
     * the interface of timer
     */
    public void onTime() {


        //record last time.KPP1-192
        m_dtLastUpdateTime.setTime(System.currentTimeMillis());

        KDSGlobalVariables.toggleBlinkingStep();
        if (!m_bEnableRefreshTimer) return;

        updateTime();

        if (isKDSValid()) {
            getKDS().on1sTimer();
            SettingsBase.StationFunc funcView = getSettings().getFuncView(); //current use what view to show orders.
            if  (funcView == SettingsBase.StationFunc.Queue ||
                    funcView == SettingsBase.StationFunc.Queue_Expo)//(getKDS().isQueueStation() || getKDS().isQueueExpo())
            {
                m_queueView.onTimer();
            }
            else if (funcView == SettingsBase.StationFunc.TableTracker)//(getKDS().isTrackerStation())
            {
                m_ttView.onTimer();
            }
            else {
                MainActivityFragment f = getMainFragment();
                if (f != null) {
                    f.updateTimer(KDSUser.USER.USER_A);
                    if (getKDS().isValidUser(KDSUser.USER.USER_B))
                        f.updateTimer(KDSUser.USER.USER_B);
                }
//                this.refreshPrevNext(KDSUser.USER.USER_A);
//
//                if (getKDS().isValidUser(KDSUser.USER.USER_B))
//                    this.refreshPrevNext(KDSUser.USER.USER_B);
            }
        }
        checkNetworkState();

        //move it to thread
//        checkAutoBumping();
//        //move it to thread
//        checkAutoBackup();
//        //move it to thread
//        checkLogFilesDeleting();

        refreshAvgPrepTime();

        checkAutoActivation();
        startCheckingThread();
        //auto refresh screen, as the indian station hide orders!!!!
        auto_refresh_screen();
        //testException();
        //checkMyAttachedStationsOffline(); //kpp1-290
        checkOfflineStations(); //kpp1-290

        m_cleaning.checkCleaningHabits();

    }

    SimpleDateFormat m_formatDate = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat m_formatTime = new SimpleDateFormat("HH:mm:ss");

    public void updateTime() {
        if (!this.getSettings().getBoolean(KDSSettings.ID.Screen_Show_Time))
            return;
        Date dt = new Date();
        String s = m_formatDate.format(dt);
        m_txtDate.setTextColor(this.getSettings().getKDSViewFontFace(KDSSettings.ID.Screen_title_fontface).getFG());
        m_txtDate.setText(s);
        s = m_formatTime.format(dt);
        m_txtTime.setTextColor(this.getSettings().getKDSViewFontFace(KDSSettings.ID.Screen_title_fontface).getFG());
        m_txtTime.setText(s);

    }

    private void checkNetworkState() {
        ImageView imgState = (ImageView) this.findViewById(R.id.imgState);
        if (KDSSocketManager.isNetworkActived(this.getApplicationContext())) {
            imgState.setImageResource(com.bematechus.kdslib.R.drawable.online);
            if (isKDSValid() && (!getKDS().isNetworkRunning()))
                onNetworkRestored();
        } else {
            imgState.setImageResource(com.bematechus.kdslib.R.drawable.offline);

            if (isKDSValid() && getKDS().isNetworkRunning())
                onNetworkLost();

        }
    }

    private void onNetworkRestored() {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return;
        getKDS().startNetwork();
        String msg = this.getString(R.string.network_restore);// "Network restored";
        showInfo(msg);
        showToastMessage(msg);
        doActivation(true, false, "");
        m_activationDog.reset();

        KDSLog.i(TAG, "Network restored");
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void onNetworkLost() {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return;
        getKDS().stopNetwork();
        String msg = this.getString(R.string.network_lost);// "Network lost";
        showInfo(msg);
        showToastMessage(msg);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void init_information_list_gui() {
        if (!isKDSValid()) return;
        boolean benabled = getKDS().getSettings().getBoolean(KDSSettings.ID.Information_List_Enabled);
        MainActivityFragment f = getMainFragment();
        if (f == null) return;
        if (f.getInfoListView() != null) {
            int n = View.VISIBLE;
            if (!benabled)
                n = View.GONE;

            f.getInfoListView().setVisibility(n);

        }
    }



    public void showErrorMac() {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        KDSUIDialogBase dlg = new KDSUIDialogBase();
        dlg.createInformationDialog(this, this.getString(R.string.error), this.getString(R.string.error_match_mac), true);
        dlg.show();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void startKDSWithoutService()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (KDSGlobalVariables.getKDS() == null)
            KDSGlobalVariables.createKDS(this.getApplicationContext());


        getKDS().setDBEventsReceiver(this);
        getKDS().setEventReceiver(this); //kpp1-312, move this function to top of getKDS().start();
        getKDS().start();


        //

        if (getKDS().isQueueStation() || getKDS().isQueueExpo())
        {
            initQueueStationAfterKDSServiceConnected();
        }
        else if (getKDS().isTrackerStation())
        {
            initTrackerStationAfterKDSServiceConnected();
        }
        else {

            setupGuiByMode(GUI_MODE.KDS);
            initAfterKDSServiceConnected();
        }

        refreshView();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "exit");
    }

    /**
     * The overided function,
     *  Do all initial works.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KDSLog.setLogLevel(KDSSettings.loadLogLevel(this.getApplicationContext()));

        KDSLog.i(TAG, KDSLog._FUNCLINE_()+"Enter");
        //test. Create an unhandled exception.
//        KDSDataOrder wrongOrder = null;
//        String s = wrongOrder.getOrderName();
///
        m_activation.setEventsReceiver(this);
        Activation.setGlobalEventsReceiver(this); //for clear db after logout

        if (!isMacMatch2()) {
            //make sure this app runs in Bematech devices.
            showErrorMac();
            return;
        }
        lockAndroidWakeMode(true); //the android device will not sleep while app is running
        //kpp1-337, remove language settings, just use os language settings.
        //KDSSettings.Language language =  KDSSettings.loadLanguageOption(this.getApplicationContext());
        //KDSUtil.setLanguage(this.getApplicationContext(), language);


        setContentView(R.layout.activity_main);
        //m_txtPrev = (TextView) this.findViewById(R.id.txtPrev);
        //m_txtNext = (TextView) this.findViewById(R.id.txtNext);
        //m_txtTitle = (TextView) this.findViewById(R.id.txtTitle);
        //m_imgLogo = (TextView) this.findViewById(R.id.imgLCI);

        //m_txtPrevB = (TextView) this.findViewById(R.id.txtPrevB);
        //m_txtNextB = (TextView) this.findViewById(R.id.txtNextB);

        //m_txtParkA = (TextView) this.findViewById(R.id.txtParkedA);
        //m_txtParkB = (TextView) this.findViewById(R.id.txtParkedB);

        //2.0.25
        //m_txtAvgTimeA = (TextView) this.findViewById(R.id.txtAvgTime);
        //m_txtAvgTimeB = (TextView) this.findViewById(R.id.txtAvgTimeB);

        //m_imgState = (ImageView) this.findViewById(R.id.imgState);

        //m_layoutTitle = (View) this.findViewById(R.id.layoutTitle);
        m_txtTime = (TextView) this.findViewById(R.id.txtTime);
        m_txtDate = (TextView) this.findViewById(R.id.txtDate);
        //m_imgMenu = (ImageView) this.findViewById(R.id.imgMenu);

        m_layoutKDSContainer = this.findViewById(R.id.main_container);
        m_layoutQueue = this.findViewById(R.id.queuelayout);
        m_queueView = (QueueView) this.findViewById(R.id.queueviewer);

        m_layoutTT = this.findViewById(R.id.trackerLayout);
        m_ttView = (TTView) this.findViewById(R.id.ttviewer);
        m_ttView.seteventReceiver(this);


        m_layoutKdsViews = (LinearLayout) this.findViewById(R.id.layoutKdsViews);


        m_flSummary = (FrameLayout) this.findViewById(R.id.fragmentlayout_sum);
        m_flSummary.setVisibility(View.GONE);

        m_flSummaryA = (FrameLayout) this.findViewById(R.id.fragmentlayout_sumA);
        m_flSummaryA.setVisibility(View.GONE);

        View vTabLinear =  this.findViewById(R.id.linearTab);
        m_tabDisplay.setLinearLayout(vTabLinear);
        m_tabDisplay.setEventsReceiver(this);


        Context c = getApplicationContext();

        KDSGlobalVariables.createKDS(c);
        KDSGlobalVariables.getKDS().setDBEventsReceiver(this);

        KDSGlobalVariables.setMainActivity(this);

        KDSBeeper.setMaxVol(c);


        register_settings_changed_events_listener();

        init_user_screen_gui_variables();

        startKDSWithoutService();

        checkRemoteFolderPermissionInThread();

        init_next_prev_view_events(); //2.0.26

        m_activation.setStationID(getKDS().getStationID());
        if (KDSConst.ENABLE_FEATURE_ACTIVATION) {
            boolean bSilent = Activation.hasDoRegister();//2.1.2
            doActivation(bSilent, false, "");
        }
        checkRelationshipBuild();
        //this.registerForContextMenu(getUserUI(KDSUser.USER.USER_A).getLayout().getView());

       // this.registerForContextMenu(getUserUI(KDSUser.USER.USER_B).getLayout().getView());

        // kpp1-325
        forceAgreementAgreed();


        KDSLog.i(TAG, KDSLog._FUNCLINE_()+"Exit");

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        //initCleaningFloatButton();
        m_cleaning.init(this, this.findViewById(R.id.fabCleaning), m_activation);


        // Using action USB Device to detect printers attached/detached
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        // Using USB Device to detect user permission action
        IntentFilter filterPerm = new IntentFilter();
        filterPerm.addAction(ACTION_USB_PERMISSION);
        registerReceiver(mUsbPermissionReceiver, filterPerm);

        // Find connected printer with the new class "Printer"
        configurePrinter();

        showBuildTypes(); //kpp1-394
    }

    public static final String ACTION_USB_PERMISSION = "com.bematechus.kds.USB_PERMISSION";
    public static UsbManager usbManager = null;
    public static Printer printer = null;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.d("##Printer", "USB Connected");
                configurePrinter();

            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.d("##Printer", "USB Disconnected");
                configurePrinter();
            }
        }
    };


    private final BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    try {
                        configurePrinter();
                    } catch (Exception e) {
                        Log.d("##Printer", e.getMessage());
                    }
                } else {
                    Log.d("##Printer", "USB Permission Denied");
                }
            }
        }
    };


    static void configurePrinter() {
        usbManager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);
        printer = new Printer(getContext(), usbManager);
    }


    /**
     *
     */
    private void init_next_prev_view_events()
    {
        //2.0.26


        TextView txtPrev = (TextView) this.findViewById(R.id.txtPrev);
        txtPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opPrevPage(getFocusedUserID());
            }
        });
        TextView txtNext = (TextView) this.findViewById(R.id.txtNext);
        txtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opNextPage(getFocusedUserID());
            }
        });

        TextView txtScrAPrev = (TextView) this.findViewById(R.id.txtScrAPrev);
        txtScrAPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opPrevPage(KDSUser.USER.USER_A);
            }
        });
        TextView txtScrANext = (TextView) this.findViewById(R.id.txtScrANext);
        txtScrANext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opNextPage(KDSUser.USER.USER_A);
            }
        });

        TextView txtScrBPrev = (TextView) this.findViewById(R.id.txtScrBPrev);
        txtScrBPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opPrevPage(KDSUser.USER.USER_B);
            }
        });
        TextView txtScrBNext = (TextView) this.findViewById(R.id.txtScrBNext);
        txtScrBNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opNextPage(KDSUser.USER.USER_B);
            }
        });

        //
        TextView txtParked = (TextView) this.findViewById(R.id.txtParked);
        txtParked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opUnpark(getFocusedUserID());
            }
        });

        TextView txtAParked = (TextView) this.findViewById(R.id.txtScrAParked);
        txtAParked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opUnpark(KDSUser.USER.USER_A);
            }
        });

        TextView txtBParked = (TextView) this.findViewById(R.id.txtScrBParked);
        txtBParked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opUnpark(KDSUser.USER.USER_B);
            }
        });

    }
    private void register_settings_changed_events_listener()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        pref.registerOnSharedPreferenceChangeListener(this);
    }

    public void checkRemoteFolderPermission()
    {
        KDSSettings.KDSDataSource source =KDSSettings.KDSDataSource.values ()[getKDS().getSettings().getInt(KDSSettings.ID.KDS_Data_Source)];
        if (source == KDSSettings.KDSDataSource.Folder) {
            String remoteFolder = getKDS().getSettings().getString(KDSSettings.ID.KDS_Data_Folder);
            if (!KDSSmbFile.smb_isValidPath(remoteFolder)) {
                //m_handlerMessage.sendPermissionError();
                return;
            }
            if (KDSSmbFile.smb_checkFolderWritable(remoteFolder)!=0) {
                m_handlerMessage.sendPermissionError();

            }
        }
    }

    public void showPermissionErrorDialog()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        KDSUIDialogBase dlg = new KDSUIDialogBase();
        dlg.createInformationDialog(this, getContext().getString(R.string.error), getContext().getString(R.string.error_folder_permission), true).show();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

    }
    public void checkRemoteFolderPermissionInThread()
    {

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
               MainActivity.this.checkRemoteFolderPermission();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    KDSUIDialogInputID m_inputStationIDDlg = null;

    private void inputStationID() {
        if (!isKDSValid()) return;
        if (m_inputStationIDDlg != null) return;
        m_inputStationIDDlg = new KDSUIDialogInputID(this, getString(R.string.input_id_title), getString(R.string.input_station_id), "", this);
        getKDS().setStationAnnounceEventsReceiver(m_inputStationIDDlg);

        m_inputStationIDDlg.show();
    }

    private void afterInputStationID(String strStationID) {
        if (strStationID.isEmpty()) return;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("kds_general_id", strStationID);
        editor.commit();
        editor.apply();
        onSharedPreferenceChanged(null, "");
        if (!isKDSValid()) return;
        getKDS().updateSettings(this);

        checkRelationshipBuild();

    }

    private void askIfLoadOldDB()
    {
        if (!getKDS().getSettings().getString(KDSSettings.ID.KDS_ID).isEmpty())
            return;

        if (!m_activation.isActivationPassed())
            return;

        if (!getKDS().isDbEmpty())
        {
            KDSUIDialogBase d = new KDSUIDialogBase();
            d.createOkCancelDialog(this,Confirm_Dialog.Load_Old_Data, getString(R.string.yes), getString(R.string.no),getString(R.string.question), getString(R.string.load_old_db_or_not),true, this );
            d.show();
        }
        else
        {
            inputStationID();
        }
    }

    /**
     * set the focus to first order
     * In others place can not set it, as all controls was not created now.
     */
    private void init_default_focus() {
        if (!isKDSValid()) return;
        if (getKDS().getUsers().getUserA().getOrders().getCount() > 0) {
            String orderGuid = getKDS().getUsers().getUserA().getOrders().get(0).getGUID();
            if (isUserLayoutReady(KDSUser.USER.USER_A))
                getUserUI(KDSUser.USER.USER_A).getLayout().getEnv().getStateValues().setFocusedOrderGUID(orderGuid);


        }
        if (getKDS().isMultpleUsersMode()) {
            if (getKDS().getUsers().getUserB().getOrders().getCount() > 0) {
                String orderGuid = getKDS().getUsers().getUserB().getOrders().get(0).getGUID();
                if (isUserLayoutReady(KDSUser.USER.USER_B))
                    getUserUI(KDSUser.USER.USER_B).getLayout().getEnv().getStateValues().setFocusedOrderGUID(orderGuid);


            }
        }
    }

    /**
     * Rev.
     *  2.0.26 change it, remove all unused view
     */
    private void init_userA() {

        if (!isKDSValid()) return;
        if (this.getLayout(KDSUser.USER.USER_A) == null) return;

        this.getLayout(KDSUser.USER.USER_A).getEnv().setSettings(getKDS().getSettings());
        if (getKDS().isMultpleUsersMode()) {//.isValidUser(KDSUser.USER.USER_B)) {

            KDSLayout layout = getLayout(KDSUser.USER.USER_A);
            layout.getEnv().setForUser(KDSUser.USER.USER_A);
            m_uiUserA.setLayout(layout);
            if (layout != null)
                layout.setEventsReceiver(this);
            m_uiUserA.setLinear(getLinear(KDSUser.USER.USER_A));
            m_uiUserA.setTopSum(getTopSum(KDSUser.USER.USER_A));
            MainActivityFragment f = (MainActivityFragment) (getFragmentManager().findFragmentById(R.id.fragmentMain));
            m_uiUserA.setSumFrame(m_flSummaryA);
            //m_uiUserA.setTouchFragment(m_flTouchPadA);
            m_uiUserA.setTouchHorizontalList(f.getTouchPad(KDSUser.USER.USER_A));
            m_uiUserA.setTouchVerticalList(f.getVerticalTouchPad(KDSUser.USER.USER_A));
            m_uiUserA.setScreenTitleLayout(f.getScreenTitleLayout(KDSUser.USER.USER_A));
            m_uiUserA.setScreenTitleIDs(R.id.txtScrAPrev, R.id.txtScrANext,R.id.txtScrATitle, R.id.txtScrAParked , R.id.txtScrAAvgPrep);

            //m_uiUserA.setPrevTextView(m_txtPrev);
            m_uiUserA.setPrevTextView(null);//getTextView(R.id.txtPrev));//2.0.26
            //m_uiUserA.setNextTextView(m_txtPrevB);
            m_uiUserA.setNextTextView(null);//getTextView(R.id.txtPrevB));//2.0.26
            //m_uiUserA.setParkedTextView(m_txtParkA);
            m_uiUserA.setParkedTextView(null);//getTextView(R.id.txtParkedA));//2.0.26

            //2.0.25
            //m_uiUserA.setAvgPrepTimeView(m_txtAvgTimeA);
            m_uiUserA.setAvgPrepTimeView(null);

            MainActivityFragmentSum fm = (MainActivityFragmentSum) (getFragmentManager().findFragmentById(R.id.fragmentSummaryA));
            m_uiUserA.setSumFragment(fm);

            m_uiUserA.setFocusIndicator(getFocusIndicator(KDSUser.USER.USER_A));
            m_uiUserA.enableFocusIndicator(true);

        } else {//single user mode, just USER_A existed
            KDSLayout layout = getLayout(KDSUser.USER.USER_A);

            m_uiUserA.setLayout(layout);
            if (layout != null)
                layout.setEventsReceiver(this);
            m_uiUserA.setLinear(getLinear(KDSUser.USER.USER_A));
            m_uiUserA.setTopSum(getTopSum(KDSUser.USER.USER_A));
            m_uiUserA.setLayout(layout);
            m_uiUserA.setSumFrame(m_flSummary);
            //m_uiUserA.setTouchFragment(m_flTouchPad);
            MainActivityFragment f = (MainActivityFragment) (getFragmentManager().findFragmentById(R.id.fragmentMain));
            m_uiUserA.setTouchHorizontalList(f.getTouchPad(KDSUser.USER.USER_A));
            m_uiUserA.setTouchVerticalList(f.getVerticalTouchPad(KDSUser.USER.USER_A));
            m_uiUserA.setScreenTitleLayout(f.getScreenTitleLayout(KDSUser.USER.USER_A));
            m_uiUserA.setScreenTitleIDs(R.id.txtScrAPrev, R.id.txtScrANext,R.id.txtScrATitle, R.id.txtScrAParked, R.id.txtScrAAvgPrep );
            //m_uiUserA.setPrevTextView(m_txtPrev);
            m_uiUserA.setPrevTextView(getTextView(R.id.txtPrev));
            //m_uiUserA.setNextTextView(m_txtNext);
            m_uiUserA.setNextTextView(getTextView(R.id.txtNext));
            //m_uiUserA.setParkedTextView(m_txtParkB);
            m_uiUserA.setParkedTextView(getTextView(R.id.txtParked));

            //2.0.25
            //m_uiUserA.setAvgPrepTimeView(m_txtAvgTimeA);
            m_uiUserA.setAvgPrepTimeView(getTextView(R.id.txtAvgTime));

            MainActivityFragmentSum fm = (MainActivityFragmentSum) (getFragmentManager().findFragmentById(R.id.fragmentSummary));
            m_uiUserA.setSumFragment(fm);
            m_uiUserA.setFocusIndicator(getFocusIndicator(KDSUser.USER.USER_A));
            m_uiUserA.enableFocusIndicator(false);

        }
    }


    /**
     * rev.
     *  2.0.26 remove all unused view.
     */
    private void init_userB() {
        if (!isKDSValid()) return;
        if (this.getLayout(KDSUser.USER.USER_B) == null)
            return;
        this.getLayout(KDSUser.USER.USER_B).getEnv().setSettings(getKDS().getSettings());
        if (getKDS().isMultpleUsersMode()) {//.isValidUser(KDSUser.USER.USER_B)) {

            this.getMainFragment().enableUserB(true);
            KDSLayout layout = getLayout(KDSUser.USER.USER_B);
            layout.getEnv().setForUser(KDSUser.USER.USER_B);

            m_uiUserB.setLayout(layout);
            if (layout != null)
                layout.setEventsReceiver(this);
            m_uiUserB.setLinear(getLinear(KDSUser.USER.USER_B));
            m_uiUserB.setTopSum(getTopSum(KDSUser.USER.USER_B));
            //m_uiUserB.setLayout(getLayout(KDSUser.USER.USER_B));
            m_uiUserB.setSumFrame(m_flSummary);
            MainActivityFragment f = (MainActivityFragment) (getFragmentManager().findFragmentById(R.id.fragmentMain));
            //m_uiUserB.setTouchFragment(m_flTouchPad);
            m_uiUserB.setTouchHorizontalList(f.getTouchPad(KDSUser.USER.USER_B));
            f.getTouchPad(KDSUser.USER.USER_B).setDrawSplitLine(true);
            m_uiUserB.setTouchVerticalList(f.getVerticalTouchPad(KDSUser.USER.USER_B));
            m_uiUserB.setScreenTitleLayout(f.getScreenTitleLayout(KDSUser.USER.USER_B));
            m_uiUserB.setScreenTitleIDs(R.id.txtScrBPrev, R.id.txtScrBNext,R.id.txtScrBTitle, R.id.txtScrBParked , R.id.txtScrBAvgPrep);

            //m_uiUserB.setPrevTextView(m_txtNextB);
            m_uiUserB.setPrevTextView(null);//getTextView(R.id.txtNextB));//2.0.26
            //m_uiUserB.setNextTextView(m_txtNext);
            m_uiUserB.setNextTextView(null);//getTextView(R.id.txtNext)); //2.0.26
            //m_uiUserB.setParkedTextView(m_txtParkB);
            m_uiUserB.setParkedTextView(null);//getTextView(R.id.txtParkedB));//2.0.26

            //2.0.25
            //m_uiUserB.setAvgPrepTimeView(m_txtAvgTimeB);
            m_uiUserB.setAvgPrepTimeView(null);//getTextView(R.id.txtAvgTimeB));
            //MainActivityTouchPadFragment f =(MainActivityTouchPadFragment) (getFragmentManager().findFragmentById(R.id.fragmentTouchpad));
            // f.setUserID(KDSUser.USER.USER_B);

            MainActivityFragmentSum fm = (MainActivityFragmentSum) (getFragmentManager().findFragmentById(R.id.fragmentSummary));
            m_uiUserB.setSumFragment(fm);

            m_uiUserB.setFocusIndicator(getFocusIndicator(KDSUser.USER.USER_B));

            //this.getLayout(KDSUser.USER.USER_B).getView().setVisibility(View.VISIBLE);
        } else { //those should been hide
            this.getMainFragment().enableUserB(false);
            KDSLayout layout = getLayout(KDSUser.USER.USER_B);
            m_uiUserB.setLayout(layout);
            if (layout != null)
                layout.setEventsReceiver(this);
            m_uiUserB.setLinear(getLinear(KDSUser.USER.USER_B));
            m_uiUserB.setTopSum(getTopSum(KDSUser.USER.USER_B));
            //m_uiUserB.setLayout(getLayout(KDSUser.USER.USER_B));
            m_uiUserB.setSumFrame(m_flSummaryA);
            //m_uiUserB.setTouchFragment(m_flTouchPadA);
            MainActivityFragment f = (MainActivityFragment) (getFragmentManager().findFragmentById(R.id.fragmentMain));
            m_uiUserB.setTouchHorizontalList(f.getTouchPad(KDSUser.USER.USER_B));
            m_uiUserB.setTouchVerticalList(f.getVerticalTouchPad(KDSUser.USER.USER_B));
            m_uiUserB.setScreenTitleLayout(f.getScreenTitleLayout(KDSUser.USER.USER_B));
            m_uiUserB.setScreenTitleIDs(R.id.txtScrBPrev, R.id.txtScrBNext,R.id.txtScrBTitle, R.id.txtScrBParked  , R.id.txtScrBAvgPrep);

            //m_uiUserB.setPrevTextView(m_txtPrevB);
            m_uiUserB.setPrevTextView(null);//getTextView(R.id.txtPrevB));//2.0.26

            //m_uiUserB.setNextTextView(m_txtNextB);
            m_uiUserB.setNextTextView(null);//getTextView(R.id.txtNextB));//2.0.26

            //m_uiUserB.setParkedTextView(m_txtParkA);
            m_uiUserB.setParkedTextView(null);//getTextView(R.id.txtParkedA));//2.0.26

            //2.0.25
            //m_uiUserB.setAvgPrepTimeView(m_txtAvgTimeB);
            m_uiUserB.setAvgPrepTimeView(null);//getTextView(R.id.txtAvgTimeB));

            MainActivityFragmentSum fm = (MainActivityFragmentSum) (getFragmentManager().findFragmentById(R.id.fragmentSummaryA));
            m_uiUserB.setSumFragment(fm);
            m_uiUserB.setFocusIndicator(getFocusIndicator(KDSUser.USER.USER_B));

        }
    }

    private KDSSettings getSettings() {
        if (!isKDSValid()) return new KDSSettings(this);
        return getKDS().getSettings();
        //return this.getLayout().getEnv().getSettings();
    }

    private String getDefaultTitleText() {

        //String strID = this.getSettings().getString(KDSSettings.ID.KDS_ID);

        String strTitle = this.getString(R.string.main_title);
        if (!isKDSValid()) return strTitle;
        if (getKDS().isQueueStation())
            strTitle = getSettings().getString(KDSSettings.ID.Queue_title);// this.getString(R.string.order_queue_display_title);
        else if (getKDS().isTrackerStation())
            strTitle = getSettings().getString(KDSSettings.ID.Tracker_title);// this.getString(R.string.order_queue_display_title);
        else if (getKDS().isQueueExpo())
            strTitle = getString(R.string.queue_expo_title);// getSettings().getString(KDSSettings.ID.Tracker_title);// this.getString(R.string.order_queue_display_title);
        if (!getKDS().isQueueStation() &&
                !getKDS().isTrackerStation() &&
                !getKDS().isQueueExpo()) //Queue don't need the version number
            strTitle += " - " + getVersionName();
        if (getKDS().getStationFunction() == KDSSettings.StationFunc.Expeditor)
            strTitle += " (EXPO)";
        //strTitle = "[#"+strID +"] "+ strTitle;
        if (getKDS().getStationsConnections().getRelations().isBackupStation())
        {
            ArrayList<KDSStationIP> arPrimary = getKDS().getStationsConnections().getRelations().getPrimaryStationsWhoUseMeAsBackup();
            strTitle += makeTitleStationsString(arPrimary, R.string.backup_of);
        }
        else if (getKDS().getStationsConnections().isMirrorOfOthers())
        {
            ArrayList<KDSStationIP> arPrimary = getKDS().getStationsConnections().getRelations().getPrimaryStationsWhoUseMeAsMirror();
            strTitle += makeTitleStationsString(arPrimary, R.string.mirror_of);
        }
        else if (getKDS().getStationsConnections().isDuplicatedOfOthers())
        {
            ArrayList<KDSStationIP> arPrimary = getKDS().getStationsConnections().getRelations().getPrimaryStationsWhoUseMeAsDuplicated();
            strTitle += makeTitleStationsString(arPrimary, R.string.duplicated_of);

        }
        else if (getKDS().getStationsConnections().isWorkLoadOfOthers())
        {
            ArrayList<KDSStationIP> arPrimary = getKDS().getStationsConnections().getRelations().getPrimaryStationsWhoUseMeAsWorkLoad();
            strTitle += makeTitleStationsString(arPrimary, R.string.workloan_of);

        }
        return strTitle;
    }

    private String makeTitleStationsString(ArrayList<KDSStationIP> arPrimary, int strID)
    {
        String strTitle = "";
        strTitle += " ("+getString(strID) +" ";
        for (int i = 0; i < arPrimary.size(); i++) {
            String id = arPrimary.get(i).getID();
            id = "#" + id;
            if (i >0) strTitle += ",";
            strTitle += id;
        }
        strTitle += ")";
        return strTitle;
    }

    private void updateTitle() {
        //KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        String strCustomized = getSettings().getString(KDSSettings.ID.General_customized_title);

        String strTitle = strCustomized;
        if (strCustomized.isEmpty()) {
            strTitle = getDefaultTitleText();
            //m_txtTitle.setText(strTitle);
            //getTextView(R.id.txtTitle).setText(strTitle);

        }
        if (KDSConst.ENABLE_FEATURE_ACTIVATION) { //2.0.48
            if (!m_activation.isActivationPassed()) {
                strTitle +=" " + getString(R.string.inactive);
            }
        }
//        else
//        {
//            getTextView(R.id.txtTitle).setText(strCustomized);
//            //m_txtTitle.setText(strCustomized);
//        }

        getTextView(R.id.txtTitle).setText(strTitle);

        String strID = this.getSettings().getString(KDSSettings.ID.KDS_ID);
        String s = "#" + strID;

        if (!Activation.getStoreName().isEmpty())
            s = Activation.getStoreName() + "-" + s;
        //m_imgLogo.setText(s);
        getTextView(R.id.imgLCI).setText(s);
        //KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void setIconEnable(Menu menu, boolean enable) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        try {
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);
            //下面传入参数
            m.invoke(menu, enable);

        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void init_title() {

        if (this.getSettings().getBoolean(KDSSettings.ID.Screen_Show_Time)) {
            this.findViewById(R.id.linearTime).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.linearTime).setVisibility(View.GONE);
        }
        updateTitle();
        KDSViewFontFace ff = this.getSettings().getKDSViewFontFace(KDSSettings.ID.Screen_title_fontface);

        View layoutTitle = (View) this.findViewById(R.id.layoutTitle);

        layoutTitle.setBackgroundColor(ff.getBG());

        TextView[] ar = new TextView[]{
                //m_txtNext,
                getTextView(R.id.txtNext),
                //m_txtPrev,
                getTextView(R.id.txtPrev),
                //m_txtNextB,
                //getTextView(R.id.txtNextB),
                //m_txtPrevB,
                //getTextView(R.id.txtPrevB),//2.0.26
                //m_txtTitle,
                getTextView(R.id.txtTitle),
                //m_imgLogo,
                getTextView(R.id.imgLCI),
                //m_txtAvgTimeA,
                getTextView(R.id.txtAvgTime),
                //m_txtAvgTimeB
                //getTextView(R.id.txtAvgTimeB),
        };
        for (int i = 0; i < ar.length; i++) {
            ar[i].setTextColor(ff.getFG());
            ar[i].setTypeface(ff.getTypeFace());
            ar[i].setTextSize(ff.getFontSize());
        }

        if (getKDS().isMultpleUsersMode())
        {

            MainActivityFragment f = (MainActivityFragment) (getFragmentManager().findFragmentById(R.id.fragmentMain));
            f.updateSubtitle(getSettings());

        }

        //kpp1-377
        if (getSettings().getBoolean(KDSSettings.ID.Hide_station_title))
        {
            SetTitleVisible(false);
        }
    }



    private String getVersionName() {
        return KDSUtil.getVersionName(getContext());

//        String appVersion = "";
//        PackageManager manager = this.getPackageManager();
//        try {
//            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
//            appVersion = info.versionName; //version name, set it in build.gradle file.
//            //or [App properties(right click)]-->[open module settings]-->app-->flavors-->version name
//        } catch (Exception e) {
//
//            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
//            //KDSLog.e(TAG, KDSUtil.error( e));
//        }
//        return appVersion;
    }

    private void initSummaryFragment() {



        MainActivityFragment fmain = (MainActivityFragment) (getFragmentManager().findFragmentById(R.id.fragmentMain));
        fmain.setListener(this);
        fmain.showButtons();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        setIconEnable(menu, true);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    /**
     *
     * bugs:
     *  kpp1-359, new device login, it will clear all stations data.
     *  @param bClearAllStations
     *      true: clear all other stations data.
     *      false: just clear my data.
     */
    public void doClearDB(boolean bClearAllStations) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        MainActivity.this.getKDS().clearAll();

        if (bClearAllStations) {
            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    KDSGlobalVariables.getKDS().getBroadcaster().broadcastClearDBCommand();
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public boolean showSettingsDialog()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        m_bEnableRefreshTimer = false;

        KDSGlobalVariables.setMainActivity(this);

        Intent i = new Intent(MainActivity.this, KDSUIConfiguration.class);
        startActivityForResult(i, KDSConst.SHOW_PREFERENCES);
        //startActivity(i, KDSConst.SHOW_PREFERENCES);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            boolean bEnablePwd = this.getSettings().getBoolean(KDSSettings.ID.Settings_password_enabled);
            if (bEnablePwd)
            {
                KDSUIDlgInputPassword dlg = new KDSUIDlgInputPassword(this, this, false);
                dlg.show();
            }
            else
            {
                return showSettingsDialog();
            }

        }
        else if (id == R.id.action_showip) {
            if (!isKDSValid()) return false;
            String ip = getKDS().getLocalIpAddress();
            ip = "IP=" + ip;
            showToastMessage(ip);

        } else {
            if (id == R.id.action_touchpad) {
//                if (!isKDSValid()) return false;
//                if (getKDS().isSingleUserMode()) {
//                    m_uiUserA.showTouchPad(!m_uiUserA.isVisibleTouchPad());
//                }
//                else {
//                    int n = KDSSettings.getEnumIndexValues(getSettings(), KDSSettings.ScreenOrientation.class, KDSSettings.ID.Screens_orientation);
//                    KDSSettings.ScreenOrientation orientation = KDSSettings.ScreenOrientation.values()[n];
//                    switch (orientation) {
//                        case Left_Right:
//                            m_uiUserA.showTouchPad(!m_uiUserA.isVisibleTouchPad());
//                            m_uiUserB.showTouchPad(!m_uiUserB.isVisibleTouchPad());
//
//                            //m_uiUserA.showVerticalTouchPad(!m_uiUserA.isVisibleVerticalTouchPad());
//                            //m_uiUserB.showVerticalTouchPad(!m_uiUserB.isVisibleVerticalTouchPad());
//
//                            break;
//                        case Up_Down:
//                            m_uiUserA.showTouchPad(!m_uiUserA.isVisibleTouchPad());
//                            m_uiUserB.showTouchPad(!m_uiUserB.isVisibleTouchPad());
//                            break;
//                    }
//
//                }
                if (!showTouchButtonsBar(!m_uiUserA.isVisibleTouchPad())) return false;
                this.m_bSuspendChangedEvent = true;
                SettingsBase.saveTouchPadVisible(this, m_uiUserA.isVisibleTouchPad());
                this.m_bSuspendChangedEvent = false;
;
            } else if (id == R.id.action_export) {
                String info = this.getString(R.string.confirm_export_db);
                info += " ";
                info += getBackupFolderFullPathName();
                info += " ?";

                KDSUIDialogBase d = new KDSUIDialogBase();
                d.createOkCancelDialog(this,
                        Confirm_Dialog.Export_Data,
                        this.getString(R.string.confirm),
                        info, false, this);
                d.show();

            } else if (id == R.id.action_import) {
                String info = this.getString(R.string.confirm_import_db);
                info += " ";
                info += getBackupFolderFullPathName();
                info += " ?";

                KDSUIDialogImportSettings d = new KDSUIDialogImportSettings();
                d.create3ButtonsDialog(this, null, this.getString(R.string.confirm), info, getString(R.string.browse), this);
                d.show();

            } else if (id == R.id.action_stations) {
                opShowActiveStations(KDSUser.USER.USER_A);
            } else if (id == R.id.action_about) {
                if (!isKDSValid()) return false;
                //KDSUIAboutDlg.showAbout(this, getKDS().getUsers().getUserA(), getVersionName() + "(" + KDSUtil.getVersionCodeString(this) + ")");//kpp1-179
                Drawable icon = this.getResources().getDrawable(R.mipmap.ic_launcher);
                String ver = getVersionName() + "(" + KDSUtil.getVersionCodeString(this) + ")";

                KDSUIAboutDlg.showAbout(this, ver, KDSConst.APP_NAME_KDS, icon, this);//kpp1-179
            }
//            else if (id == R.id.action_restore) { //kpp1-1083
//                startKDSUtility();
//            }
            else if (id == R.id.action_remote_support)
            {
                startTeamViewer();
            }
            else if (id == R.id.action_logout)
            {
                showConfirmLogoutDialog();
//                Activation.resetUserNamePwd();
//                resetStationID();
//                setToDefaultSettings(); //kpp1-299 Station Relationship remembered
//                onDoActivationExplicit();
                //m_activation.cancelActivation();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void startTeamViewer()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        //Launch teamviewer from KDS
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.teamviewer.quicksupport.market");
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }
    private void startKDSUtility()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        Intent i = new Intent(MainActivity.this, KDSActivityUtility.class);
        //i.putExtra("main", this);

        startActivityForResult(i, KDSConst.SHOW_UTILITY);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    static final public String DEFAULT_BACKUP_FOLDER = "KDSBackup";
    static final public String AUTO_BACKUP_FOLDER = "KDSBackup/auto";


//    static private String getBaseFolder()
//    {
//        String folder = Environment.getExternalStorageDirectory().getAbsolutePath();
//        return folder;
//
//    }
    private boolean createSDFolder(String strFolderNameWithoutLastDivid) {

        //String folder = Environment.getExternalStorageDirectory() + "/" + strFolderNameWithoutLastDivid;
        String folder = KDSUtil.getBaseDirCanNotUninstall() + "/" + strFolderNameWithoutLastDivid;
        return KDSUtil.createFolder(folder);

    }

    /**
     * for backup/restore
     * @param strFolderNameWithoutLastDivid
     * @return
     */
    static public String getSDFolderFullPathWithLastDivid(String strFolderNameWithoutLastDivid) {

        //String folder = Environment.getExternalStorageDirectory() + "/" + strFolderNameWithoutLastDivid + "/";
        String folder = KDSUtil.getBaseDirCanNotUninstall() + "/" + strFolderNameWithoutLastDivid + "/";
        return folder;

    }

    private String getBackupFolder() {
        return  KDSUtil.getBaseDirCanNotUninstall() + "/" + DEFAULT_BACKUP_FOLDER;
    }

    /**
     * with the last /
     *
     * @return
     */
    private String getBackupFolderFullPathName() {

        return getBackupFolder() + "/";


    }

    private boolean export_settings(String toFolder) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!export_data(DEFAULT_BACKUP_FOLDER))
            return false;
        String strInfo = this.getString(R.string.data_backup_to) + getBackupFolderFullPathName();;//"The data have been backup to " + targetFolder;
        this.showToastMessage(strInfo);
        this.showInfo(strInfo);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
        return true;

    }


    /**
     *
     * @param toSDFolderName
     *  without last /
     * @return
     */
    private boolean export_data(String toSDFolderName) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");

        String dbCurrentPath = KDSDBBase.getDBFullPath(KDSDBCurrent.DB_NAME);
        String dbSupportPath = KDSDBBase.getDBFullPath(KDSDBSupport.DB_NAME);//this.getApplicationContext().getDatabasePath(KDSDBSupport.DB_NAME).getAbsolutePath();
        String dbStatisticPath = KDSDBBase.getDBFullPath(KDSDBStatistic.DB_NAME);//this.getApplicationContext().getDatabasePath(KDSDBStatistic.DB_NAME).getAbsolutePath();

        if (!createSDFolder(toSDFolderName))
            return false;
        String targetFolder = getSDFolderFullPathWithLastDivid(toSDFolderName);// getBackupFolderFullPathName();// this.getApplicationContext().getDir(targetFolder, Context.MODE_PRIVATE).getAbsolutePath()+"/";


        String targetFile = targetFolder + KDSDBCurrent.DB_NAME;
        KDSUtil.fileCopy(dbCurrentPath, targetFile);

        targetFile = targetFolder + KDSDBSupport.DB_NAME;
        KDSUtil.fileCopy(dbSupportPath, targetFile);

        targetFile = targetFolder + KDSDBStatistic.DB_NAME;
        KDSUtil.fileCopy(dbStatisticPath, targetFile);

        if (isKDSValid())
            getKDS().getSettings().exportToFolder(this.getApplicationContext(),targetFolder );

        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
        return true;

    }


    private void import_settings(String fromFolder, boolean bIsAbsolutePath) {
        if (!import_data(fromFolder, bIsAbsolutePath)) {
            String strInfo = this.getString(R.string.data_restore_failed);// "The data have been restored from " + srcFolder;
            strInfo = strInfo.replace("#", getSDFolderFullPathWithLastDivid(fromFolder));
            this.showToastMessage(strInfo);
            this.showInfo(strInfo);
            return;
        }

        String strInfo = this.getString(R.string.data_restored);// "The data have been restored from " + srcFolder;
        strInfo = strInfo.replace("#", getSDFolderFullPathWithLastDivid(fromFolder));
        this.showToastMessage(strInfo);
        this.showInfo(strInfo);
    }


    /**
     * Rev:
     *  Kpp1-358  Only exported settings should be retrieved, not previous orders from when settings were exported.
     * @param fromFolder
     *  without last /
     * @return
     */
    public boolean import_data(String fromFolder, boolean bIsAbsolutePath) {
        /* //kpp1-358
        String dbCurrentPath = KDSDBBase.getDBFullPath(KDSDBCurrent.DB_NAME);
        String dbSupportPath = KDSDBBase.getDBFullPath(KDSDBSupport.DB_NAME);//this.getApplicationContext().getDatabasePath(KDSDBSupport.DB_NAME).getAbsolutePath();
        String dbStatisticPath = KDSDBBase.getDBFullPath(KDSDBStatistic.DB_NAME);//this.getApplicationContext().getDatabasePath(KDSDBStatistic.DB_NAME).getAbsolutePath();
*/
        String srcFolder = fromFolder;// "/KDSBackup/";
        if (!bIsAbsolutePath)
            srcFolder = getSDFolderFullPathWithLastDivid(fromFolder);// getBackupFolderFullPathName();// this.getApplicationContext().getDir(fromFolder, Context.MODE_PRIVATE).getAbsolutePath()+"/";
/*//kpp1-358
        String srcFile = srcFolder + KDSDBCurrent.DB_NAME;
        if (!isKDSValid()) return false;
        if (KDSUtil.fileExisted(srcFile)) {
            getKDS().getCurrentDB().close();
            KDSUtil.copyFile(srcFile, dbCurrentPath);
            getKDS().reopenCurrentDB(this.getApplicationContext());
        }


        srcFile = srcFolder + KDSDBSupport.DB_NAME;
        if (KDSUtil.fileExisted(srcFile)) {
            getKDS().getSupportDB().close();
            KDSUtil.copyFile(srcFile, dbSupportPath);
            getKDS().reopenSupportDB(this.getApplicationContext());
        }


        srcFile = srcFolder + KDSDBStatistic.DB_NAME;
        if (KDSUtil.fileExisted(srcFile)) {
            getKDS().getStatisticDB().close();
            KDSUtil.copyFile(srcFile, dbStatisticPath);
            getKDS().reopenStatisticDB(this.getApplicationContext());
        }
        */
        if (!isKDSValid()) return false;
        return import_kds_setting(srcFolder, true);
    }


    public boolean import_kds_setting(String fromFolder, boolean bIsAbsolutePath)
    {
        String srcFolder = fromFolder;// "/KDSBackup/";
        if (!bIsAbsolutePath)
            srcFolder = getSDFolderFullPathWithLastDivid(fromFolder);// getBackupFolderFullPathName();// this.getApplicationContext().getDir(fromFolder, Context.MODE_PRIVATE).getAbsolutePath()+"/";
        String srcFile = srcFolder + KDSSettings.SETTINGS_FILE_NAME;// "settings.xml";
        if (!KDSUtil.fileExisted(srcFile))
            return false;

        String settings = KDSUtil.readFileText(srcFile);
        if (settings.isEmpty()) return  false;
        m_bSuspendChangedEvent = true;
        getKDS().loadSettingsXmlAll(settings);
        m_bSuspendChangedEvent = false;
        getKDS().stopWithoutDBClose();
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        getKDS().getPrinter().initBemaPrinter(this.getBaseContext(), usbManager);

        getKDS().start();
        onSharedPreferenceChanged(null, "");

        refreshWithNewDbData();

        return true;
    }

   static public boolean import_data2(String fromFolder, UsbManager usbManager) {
        String dbCurrentPath = KDSDBBase.getDBFullPath(KDSDBCurrent.DB_NAME);
        String dbSupportPath = KDSDBBase.getDBFullPath(KDSDBSupport.DB_NAME);//this.getApplicationContext().getDatabasePath(KDSDBSupport.DB_NAME).getAbsolutePath();
        String dbStatisticPath = KDSDBBase.getDBFullPath(KDSDBStatistic.DB_NAME);//this.getApplicationContext().getDatabasePath(KDSDBStatistic.DB_NAME).getAbsolutePath();

        String srcFolder = fromFolder;// "/KDSBackup/";
        srcFolder = getSDFolderFullPathWithLastDivid(fromFolder);// getBackupFolderFullPathName();// this.getApplicationContext().getDir(fromFolder, Context.MODE_PRIVATE).getAbsolutePath()+"/";

        String srcFile = srcFolder + KDSDBCurrent.DB_NAME;
        if (KDSGlobalVariables.getKDS() == null) return false;
        KDSGlobalVariables.getKDS().getCurrentDB().close();
        KDSUtil.copyFile(srcFile, dbCurrentPath);
        KDSGlobalVariables.getKDS().reopenCurrentDB(getContext());


       KDSGlobalVariables.getKDS().getSupportDB().close();
        srcFile = srcFolder + KDSDBSupport.DB_NAME;
        KDSUtil.copyFile(srcFile, dbSupportPath);
       KDSGlobalVariables.getKDS().reopenSupportDB(getContext());

       KDSGlobalVariables.getKDS().getStatisticDB().close();
       srcFile = srcFolder + KDSDBStatistic.DB_NAME;
       KDSUtil.copyFile(srcFile, dbStatisticPath);
       KDSGlobalVariables.getKDS().reopenStatisticDB(getContext());


        //String settings = getKDS().getSettings().outputXmlText(this.getApplicationContext());

        srcFile = srcFolder + KDSSettings.SETTINGS_FILE_NAME;// "settings.xml";
        if (!KDSUtil.fileExisted(srcFile))
            return false;

        String settings = KDSUtil.readFileText(srcFile);
        if (settings.isEmpty()) return  false;
       KDSGlobalVariables.getKDS().loadSettingsXml(settings);

       KDSGlobalVariables.getKDS().stopWithoutDBClose();
       // UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
       KDSGlobalVariables.getKDS().getPrinter().initBemaPrinter(getContext(), usbManager);
       KDSGlobalVariables.getKDS().start();
        //refreshWithNewDbData();
        return true;

//        String strInfo = this.getString(R.string.data_restored);// "The data have been restored from " + srcFolder;
//        strInfo = strInfo.replace("#", srcFolder);
//        this.showToastMessage(strInfo);
//        this.showInfo(strInfo);
    }



    public KDSUser.USER getFocusedUserID() {
        if (!isKDSValid()) return KDSUser.USER.USER_A;
        return getKDS().getUsers().getFocusedUserID();
    }


    private void opFocusNext(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isUserLayoutReady(userID)) return;

        getUserUI(userID).getLayout().focusNext();
        //getUserUI(userID).refreshPrevNext(); //roll back code.
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private boolean isUserLayoutReady(KDSUser.USER userID)
    {
        if (getUserUI(userID) == null) return false;
        if (getUserUI(userID).getLayout() == null) return false;
        return true;
    }


    /**
     * Move focus prev
     * @param userID
     */
    private void opFocusPrev(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        MainActivityFragment f = getMainFragment();
        if (f != null)
            f.focusPrev(userID);
        //getUserUI(userID).refreshPrevNext(); //roll back code.
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }


    private void opSummary(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        int n = getKDS().getSettings().getInt(KDSSettings.ID.Sum_position);
        KDSSettings.SumPosition pos = KDSSettings.SumPosition.values()[n];
        showSummary(userID,  !this.getUserUI(userID).isVisibleSum(pos));
//        if (!isKDSValid()) return ;
//        int n = getKDS().getSettings().getInt(KDSSettings.ID.Sum_position);
//        KDSSettings.SumPosition pos = KDSSettings.SumPosition.values()[n];
//        if (getKDS().getSettings().getBoolean(KDSSettings.ID.AdvSum_enabled) &&
//                getKDS().getSettings().getBoolean(KDSSettings.ID.AdvSum_always_visible))
//        {
//            this.getUserUI(userID).showSum(userID, pos, true);
//        }
//        else
//            this.getUserUI(userID).showSum(userID, pos, !this.getUserUI(userID).isVisibleSum(pos));
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public boolean isSummaryVisible(KDSUser.USER userID) {
        if (!isKDSValid()) return false;
        int n = getKDS().getSettings().getInt(KDSSettings.ID.Sum_position);
        KDSSettings.SumPosition pos = KDSSettings.SumPosition.values()[n];
        return this.getUserUI(userID).isVisibleSum(pos);
    }


    private void opUp(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        if (isLineItemsMode())
        {
            opFocusPrev(userID);
            return;
        }

        String guid = getPrevItemGuid(userID);

        // if (guid.isEmpty()) return;

        if (!isUserLayoutReady(userID)) return;

//        MainActivityFragment f = getMainFragment();
//        if (f == null) return;
//
//        f.getLayout(userID).getEnv().getStateValues().setFocusedItemGUID(guid);
        getUserUI(userID).getLayout().getEnv().getStateValues().setFocusedItemGUID(guid);
        getKDS().refreshView(userID, KDS.RefreshViewParam.None);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private String getFocusedOrderGUID(KDSUser.USER userID)
    {
        SettingsBase.StationFunc funcView = getSettings().getFuncView();
        if (funcView == SettingsBase.StationFunc.TableTracker)// (getKDS().isTrackerStation())
        {
            return m_ttView.getFocusedGuid();
        }
        else if (funcView == SettingsBase.StationFunc.Queue ||
                funcView== SettingsBase.StationFunc.Queue_Expo) //(getKDS().isQueueStation() || getKDS().isQueueExpo())
        {
            return m_queueView.getFocusedGuid();
        }
        else {
//            MainActivityFragment f = getMainFragment();
//            if (f == null) return "";
            if (!isUserLayoutReady(userID)) return "";
            return getUserUI(userID).getLayout().getEnv().getStateValues().getFocusedOrderGUID();
            //return f.getLayout(userID).getEnv().getStateValues().getFocusedOrderGUID();
        }
    }

    private String getFocusedItemGUID(KDSUser.USER userID)
    {
//        MainActivityFragment f = getMainFragment();
//        if (f == null) return "";
        if (!isUserLayoutReady(userID)) return "";
        return getUserUI(userID).getLayout().getEnv().getStateValues().getFocusedItemGUID();
        //return f.getLayout(userID).getEnv().getStateValues().getFocusedItemGUID();
    }


    private String getNextItemGuid(KDSUser.USER userID) {
        if (!isKDSValid()) return "";

//        MainActivityFragment f = getMainFragment();
//        if (f == null) return "";
        if (!isUserLayoutReady(userID)) return "";

        String currentItem = getSelectedItemGuid(userID);
        KDSDataOrder order = getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(getSelectedOrderGuid(userID));
        if (order == null) return "";
        KDSLayout layout = getUserUI(userID).getLayout();// f.getLayout(userID);
        KDSLayoutOrder layoutOrder = layout.createLayoutOrder(order);

        int index = -1;
        if (!currentItem.isEmpty())
            index = layoutOrder.getItems().getItemIndexByGUID(currentItem);
        KDSDataItem item = null;
        for (int i = 0; i < layoutOrder.getItems().getCount(); i++) {
            index++;
            item = layoutOrder.getItems().getItem(index);
            if (item == null) break;
            if (item instanceof KDSDataFromPrimaryIndicator) {
                item = null;
                continue;

            }
            if (item instanceof KDSDataMoreIndicator) {
                item = null;
                continue;
            }

            if (item instanceof KDSDataCategoryIndicator) { //2.0.47
                item = null;
                continue;
            }
            if (item != null) break;
        }

        if (item == null)
            return "";
        return item.getGUID();


    }

    private String getPrevItemGuid(KDSUser.USER userID) {
        if (!isKDSValid()) return "";
//        MainActivityFragment f = getMainFragment();
//        if (f == null) return "";
        if (!isUserLayoutReady(userID)) return "";
        String currentItem = getSelectedItemGuid(userID);
        KDSDataOrder order = getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(getSelectedOrderGuid(userID));
        if (order == null) return "";
        KDSLayout layout = getUserUI(userID).getLayout();// f.getLayout(userID);
        KDSLayoutOrder layoutOrder = layout.createLayoutOrder(order);

        int index = layoutOrder.getItems().getCount();
        if (!currentItem.isEmpty())
            index = layoutOrder.getItems().getItemIndexByGUID(currentItem);
        KDSDataItem item = null;
        for (int i = 0; i < layoutOrder.getItems().getCount(); i++) {
            index--;
            item = layoutOrder.getItems().getItem(index);
            if (item instanceof KDSDataMoreIndicator) {
                item = null;
                continue;
            }
            if (item instanceof KDSDataFromPrimaryIndicator) {
                item = null;
                continue;
            }

            if (item instanceof KDSDataCategoryIndicator) { //2.0.47
                item = null;
                continue;
            }

            if (item != null) break;

        }
        if (item == null)
            return "";
        return item.getGUID();


    }


    private void opDown(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        if (isLineItemsMode())
        {
            opFocusNext(userID);
            return;
        }
        // String guid = getKDS().getNextItemGuid(userID, getSelectedOrderGuid(userID), getSelectedItemGuid(userID));
        String guid = getNextItemGuid(userID);
        // if (guid.isEmpty()) return;
//        MainActivityFragment f = getMainFragment();
//        if (f == null) return;
        if (!isUserLayoutReady(userID)) return;
        getUserUI(userID).getLayout().getEnv().getStateValues().setFocusedItemGUID(guid);
        //f.getLayout(userID).getEnv().getStateValues().setFocusedItemGUID(guid);
        getKDS().refreshView(userID, KDS.RefreshViewParam.None);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }




    public void opPrint(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;

        String orderGuid = getSelectedOrderGuid(userID);// f.getLayout().getEnv().getStateValues().getFocusedOrderGUID();
        if (orderGuid.isEmpty()) return;
        KDSDataOrder order = getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        getKDS().getPrinter().printOrder(order);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");


    }

    //static final int CONFIRM_BUMP = 1;

    private void confirmBumpFocusedOrder(KDSUser.USER userID, String orderGuid) {
        if (!isKDSValid()) return ;
//        String orderGuid = getSelectedOrderGuid(userID);// f.getLayout().getEnv().getStateValues().getFocusedOrderGUID();
//        if (orderGuid.isEmpty()) return;

        KDSDataOrder order = getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        if (order == null) return;
        String orderName = order.getOrderName();
        String strBump = getString(R.string.confirm_bump_normal);
        if (getKDS().getStationFunction() == KDSSettings.StationFunc.Expeditor) {
            if (!order.isItemsAllBumpedInExp()) {
                strBump = getString(R.string.confirm_bump_expo_outstanding);
            }
            //It should show a confirmation dialog even if it is finished.
            //But, I can not remember why I use following code. Just keep it.
            else {
                afterConfirmBumpOrder(userID, orderGuid);//2.0.51
                return;
            }

        }
        strBump = strBump.replace("#", "#" + orderName);

        KDSUIDialogConfirm dlg = new KDSUIDialogConfirm(this, strBump, this);
        dlg.setTag(Confirm_Dialog.CONFIRM_BUMP);// CONFIRM_BUMP);
        dlg.setTag("userid", userID);
        dlg.setTag("orderguid", orderGuid);
        dlg.show();

    }
    private void confirmBumpFocusedOrderUnpaid(KDSUser.USER userID, String orderGuid)
    {
        if (!isKDSValid()) return ;
        KDSDataOrder order = getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        if (order == null) return;
        String orderName = order.getOrderName();
        String strBump = getString(R.string.confirm_bump_unpaid_text);

        strBump = strBump.replace("#", "#" + orderName);

        KDSUIDialogConfirm dlg = new KDSUIDialogConfirm(this, strBump, this);
        dlg.setTag(Confirm_Dialog.CONFIRM_BUMP);// CONFIRM_BUMP);
        dlg.setTag("userid", userID);
        dlg.setTag("orderguid", orderGuid);
        dlg.show();

    }
    private void confirmBumpFocusedOrderOutstanding(KDSUser.USER userID, String orderGuid)
    {
        if (!isKDSValid()) return ;
        KDSDataOrder order = getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        if (order == null) return;
        String orderName = order.getOrderName();
        String strBump = getString(R.string.confirm_bump_outstanding_text);

        strBump = strBump.replace("#", "#" + orderName);

        KDSUIDialogConfirm dlg = new KDSUIDialogConfirm(this, strBump, this);
        dlg.setTag(Confirm_Dialog.CONFIRM_BUMP);// CONFIRM_BUMP);
        dlg.setTag("userid", userID);
        dlg.setTag("orderguid", orderGuid);
        dlg.show();

    }

    private void afterConfirmBumpOrder(KDSUser.USER userID, String orderGuid) {
        onBumpOrder(userID);
    }

    private void opSwitchUser() {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        getKDS().getUsers().switchUser();

        focusUser(getKDS().getUsers().getFocusedUserID());
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void focusUser(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        if (!getKDS().isMultpleUsersMode())
            return;

        getKDS().getUsers().setFocusedUser(userID);
        this.getUserUI(userID).focusMe(true);
        if (userID == KDSUser.USER.USER_A)
            this.getUserUI(KDSUser.USER.USER_B).focusMe(false);
        else if (userID == KDSUser.USER.USER_B)
            this.getUserUI(KDSUser.USER.USER_A).focusMe(false);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    /**
     * 2.0.11
     *      Change its name from isExpoDoubleBumpForQueue to isDoubleBumpForQueue
     * @return
     */
    private boolean isDoubleBumpForQueue()
    {
        //2.0.11
        if ( (this.getKDS().isExpeditorStation() || this.getKDS().isPrepStation() ) &&
                getKDS().getSettings().getBoolean(KDSSettings.ID.Queue_double_bump_expo_order) &&
                getKDS().getStationsConnections().isMyQueueDisplayStationsExisted())
        {
            return true;
        }
        return false;
    }


    /**
     * 2.0.25
     *     Add new option in Bumping setting: Expo confirmation bump.
     *     This only works in Expo station, when Prep stations’ item is not bump, Expo cannot bump the order when this option is enable.
     *     Show warning when user try to bump. And add this explanation under the option
     *     “
     *     Expo cannot bump the order unless all its prep station bump the items ”
     * rev.
     *  1. fix kpp1-9 bug
     *  2. 2020/8/10 fix kpp1-343   should allow for items that were sent to the expo and not prep to be bumped.
     *                      should also be able to bump items that were already bumped by prep.
     *  3
     *rev.
     * default return false;
     * @param orderGuid
     * @return
     *  true: this order was handled by expo
     *  false: do next work
     */
    private boolean checkExpoConfirmationBump(KDSUser.USER userID,String orderGuid, boolean bForAutoBumping)
    {

        if (!getKDS().isExpeditorStation())
            return false;
        if (!getSettings().getBoolean(KDSSettings.ID.Bumping_expo_confirmation))
            return false;

        KDSDataOrder order =  getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        //if (!order.isAllItemsFinished())
        if (!order.isExpoAllItemsFinished(getKDS().getStationID())) //kpp1-343
        {
            if (bForAutoBumping) {
                String orderName = order.getOrderName();
                Message m = new Message();
                m.what = ExpoAutoFrom.AutoBumpingThread.ordinal();
                m.obj = orderName;
                m_expoBumpingConfirmHandler.sendMessage(m);//.send.sendEmptyMessage(ExpoAutoFrom.AutoBumpingThread.ordinal()); //kpp1-380, auto expo bumping. As auto bump is in thread, we have to use this handler.
            }
            else
                m_expoBumpingConfirmHandler.sendEmptyMessage(ExpoAutoFrom.MainThread.ordinal()); //kpp1-380, auto expo bumping. As auto bump is in thread, we have to use this handler.
//            if (m_expoBumpConfirmDlg != null) {
//                m_expoBumpConfirmDlg.hide();
//                m_expoBumpConfirmDlg = null;
//            }
//            //AlertDialog d = new AlertDialog.Builder(this) //kpp1-380
//            m_expoBumpConfirmDlg = new AlertDialog.Builder(this)
//                    .setTitle(this.getString(R.string.message))
//                    .setMessage(this.getString(R.string.expo_cannot_bump_unless_prep_bump_all))
//                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    m_expoBumpConfirmDlg = null;
//                                }
//                            }
//                    )
//
//                    .create();
//            //d.show();//kpp1-380
//            m_expoBumpConfirmDlg.show();
            return true;
        }
        return false;
    }

    private void opBump(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        String orderGuid = getSelectedOrderGuid(userID);// f.getLayout().getEnv().getStateValues().getFocusedOrderGUID();
        if (orderGuid.isEmpty()) return;

        //2.0.25
        //kpp1-343, comment it here. Move to below
       // if (checkExpoConfirmationBump(userID, orderGuid))
       //     return;

        SettingsBase.StationFunc funcView = getSettings().getFuncView();

        if (funcView == SettingsBase.StationFunc.TableTracker) //(getKDS().isTrackerStation())
        {
            bumpTrackerOrder(userID, orderGuid);
        }
        else {
            String itemGuid = getSelectedItemGuid(userID);

            if (itemGuid.isEmpty()) {//bump order
                //kpp1-343, allow expo bump itself items . So, move this check here.
                if (checkExpoConfirmationBump(userID, orderGuid, false))
                    return;

                if (getKDS().getSettings().getBoolean(KDSSettings.ID.Bumping_confirm)) {
                    confirmBumpFocusedOrder(userID, orderGuid);
                }
                else if (getKDS().getSettings().getBoolean(KDSSettings.ID.Confirm_bump_unpaid) && isOrderUnpaid(userID, orderGuid))
                {
                    confirmBumpFocusedOrderUnpaid(userID, orderGuid);
                }
                else if (getKDS().getSettings().getBoolean(KDSSettings.ID.Confirm_bump_outstanding) && isOrderOutstanding(userID, orderGuid))
                {
                    confirmBumpFocusedOrderOutstanding(userID, orderGuid);
                }
                else
                    onBumpOrder(userID);
            } else {
                if (checkExpoCanBumpItem(userID, orderGuid, itemGuid))
                onBumpItem(userID);
            }
        }
        getKDS().schedule_process_update_to_be_prepare_qty(true);
        getKDS().getCurrentDB().clearExpiredBumpedOrders( getSettings().getBumpReservedCount());
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    /**
     * 2.0.36
     * @param userID
     * @param orderGuid
     * @return
     */
    private boolean isOrderUnpaid(KDSUser.USER userID, String orderGuid)
    {
        KDSDataOrder order =  getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        if (order == null) return false;
        return (order.getStatus() == KDSDataOrder.ORDER_STATUS_UNPAID);
    }

    /**
     * 2.0.36
     * @param userID
     * @param orderGuid
     * @return
     */
    private boolean isOrderOutstanding(KDSUser.USER userID, String orderGuid)
    {
        KDSDataOrder order =  getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        if (order == null) return false;
        return (!order.isAllItemsFinished());

    }
//    BumpThread m_bumpThread = new BumpThread();
//    public void bumpOrderInThread(final KDSUser.USER userID,  ArrayList<String> arOrderGuid)
//    {
//        //BumpThread t = new BumpThread();
//        if (m_bumpThread.setOrderGuids(arOrderGuid) >0 ) {
//            m_bumpThread.m_userID = userID;
//
//            try {
//                m_bumpThread.start();
//            }catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//        }
//
//    }
    public KDSDataOrder bumpOrder(KDSUser.USER userID, String orderGuid, boolean bRefreshView) {
        if (!isKDSValid()) return new KDSDataOrder();
        if (!isUserLayoutReady(userID)) return new KDSDataOrder();
        //save it for printing.
        KDSDataOrder order = getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        if (order == null) return new KDSDataOrder();
        if (isDoubleBumpForQueue())
        {
            if (!order.getQueueReady()) {
                order.setQueueReady(true);
                getKDS().getCurrentDB().orderSetQueueReady(order.getGUID(), true);
                KDSStationFunc.sync_with_queue(getKDS(), KDSXMLParserCommand.KDSCommand.Queue_Ready, order, null, "" );
                //guest_paging it, add it at 20130313
                /**
                 * One “bug” to fix: The queue display mode has option to enable double bump from expo which works well.
                 * Its first bump turns the order to grey(as regular food ready for bump), and 2nd bump to remove order.
                 * However there is a “bug” when queue display work with pager, the expo only guest_paging the pager after 2nd bump.
                 * Please change it to expo guest_paging the pager at its first bump as the food is already ready.
                 */
            //2.0.11, enable prep queue
                if (getKDS().isExpeditorStation() || getKDS().isPrepStation())
                {
                    notifyPOSOrderBump(userID, order);//2.0.21, please make sure when double bump is enable for queue display,  the expo first bump sends out notification instead of the 2nd one.
                    if (getSettings().getBoolean(KDSSettings.ID.Queue_double_bump_expo_order)) //20180313
                        getKDS().getPagerManager().addPagerID(order.getPagerID());
                }
                return null;
            }
        }

        //if (!getKDS().isQueueStation() &&
        //        !getKDS().isTrackerStation() && !getKDS().isQueueExpo()) {

        if (!isFixedSingleScreenView()){
            String firstOrderGuid = getUserUI(userID).getLayout().getEnv().getStateValues().getFirstShowingOrderGUID();
            if (orderGuid.equals( firstOrderGuid) ) {
                    String nextFirstOrder = getNextOrderGuidToFocus(userID, firstOrderGuid);//  getKDS().getUsers().getUser(userID).getOrders().getNextOrderGUID(firstOrderGuid);
                    if (nextFirstOrder.isEmpty())
                        nextFirstOrder = getFirstOrderGuidToFocus(userID);// getKDS().getUsers().getUser(userID).getOrders().getFirstOrderGuid();
                    getUserUI(userID).getLayout().getEnv().getStateValues().setFirstShowingOrderGUID(nextFirstOrder);

            }
        }
        //TimeDog td = new TimeDog();
        KDSStationFunc.orderBump(getKDS().getUsers().getUser(userID), orderGuid,bRefreshView );
        //if (bPageUpOperation)
            //getUserUI(userID).getLayout().focusOrder(KDSConst.RESET_ORDERS_LAYOUT);
        //td.debug_print_Duration("Func-orderBump");
        //notification

        if (!isFixedSingleScreenView()){
            //
            if (!isDoubleBumpForQueue()) //2.0.21,double bump has been done above., please make sure when double bump is enable for queue display,  the expo first bump sends out notification instead of the 2nd one.
                notifyPOSOrderBump(userID, order);
            if (isDoubleBumpForQueue()) {
                if (order.getQueueReady()) {

                    KDSStationFunc.sync_with_queue(getKDS(), KDSXMLParserCommand.KDSCommand.Queue_Pickup, order, null, "");
                    return order;
                }
            }
        }



        return order;

    }

    private void notifyPOSOrderBump(KDSUser.USER userID, KDSDataOrder order)
    {

        //notification
        if (!isKDSValid()) return ;
        if (getKDS().isExpeditorStation())
            getKDS().firePOSNotification(order, null, KDSPosNotificationFactory.BumpUnbumpType.BUMP_EXPEDITOR_ORDER );
        else
            getKDS().firePOSNotification(order, null,KDSPosNotificationFactory.BumpUnbumpType.BUMP_ORDER );


        //preparation time mode.
        getKDS().getBroadcaster().broadcastItemBumpUnbump(order, true);
    }

    public void printOrder(KDSDataOrder order) {
        //print it
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        KDSPrinter.HowToPrintOrder howtoprint = KDSPrinter.HowToPrintOrder.values()[(getKDS().getSettings().getInt(KDSSettings.ID.Printer_howtoprint))];
        if (howtoprint == KDSPrinter.HowToPrintOrder.WhileBump ||
             howtoprint == KDSPrinter.HowToPrintOrder.WhileTransfer) {
            getKDS().getPrinter().printOrder(order);
        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private String getFirstOrderGuidToFocus(KDSUser.USER userID)
    {
        if (!isKDSValid()) return "";
        String firstGuid = "";
        int n = getKDS().getSettings().getInt(KDSSettings.ID.Item_showing_method);
        KDSSettings.ItemShowingMethod itemShowingMethod = KDSSettings.ItemShowingMethod.values()[n];
        if (itemShowingMethod == KDSSettings.ItemShowingMethod.When_order_is_paid)
        {
            firstGuid = getKDS().getUsers().getUser(userID).getOrders().getFirstPaidOrderGuid();
        }
        else
            firstGuid = getKDS().getUsers().getUser(userID).getOrders().getFirstOrderGuid();
        return firstGuid;
    }

    private boolean isScheduleOrder(KDSUser.USER userID, String orderGuid)
    {
        if (!isKDSValid()) return false;
        return getKDS().getUsers().getUser(userID).getOrders().isScheduleOrder(orderGuid);//.getNextPaidOrderGUID(guid);

    }


    private String getNextOrderGuidToFocus(KDSUser.USER userID, String orderGuid)
    {
        if (!isKDSValid()) return "";
        SettingsBase.StationFunc funcView = getSettings().getFuncView();
        if (funcView == SettingsBase.StationFunc.Queue ||
                funcView== SettingsBase.StationFunc.Queue_Expo) //(getKDS().isQueueStation() || getKDS().isQueueExpo())
        {
            return m_queueView.getNextGuid(orderGuid);
        }
        if (funcView == SettingsBase.StationFunc.TableTracker)// (getKDS().isTrackerStation())
        {
            return m_ttView.getNextGuid(orderGuid);
        }
        else {
            String guid = orderGuid;
            int n = getKDS().getSettings().getInt(KDSSettings.ID.Item_showing_method);
            KDSSettings.ItemShowingMethod itemShowingMethod = KDSSettings.ItemShowingMethod.values()[n];
            String nextGuid = "";
            if (itemShowingMethod == KDSSettings.ItemShowingMethod.When_order_is_paid) {
                nextGuid = getKDS().getUsers().getUser(userID).getOrders().getNextPaidOrderGUIDNoLoop(guid);
                if (nextGuid.isEmpty())//KPP1-186
                    nextGuid = getKDS().getUsers().getUser(userID).getOrders().getPrevPaidOrderGUID(guid);
            } else {
                nextGuid = getKDS().getUsers().getUser(userID).getOrders().getNextOrderGUIDNoLoop(guid);
                if (nextGuid.isEmpty()) //KPP1-186
                    nextGuid = getKDS().getUsers().getUser(userID).getOrders().getPreviousOrderGUID(guid);
            }

            if (nextGuid.isEmpty()) {
                nextGuid = getFirstOrderGuidToFocus(userID);

            }
            return nextGuid;
        }
    }


    /**
     * bump selected order
     */
    public void onBumpOrder(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;

        //prevent queue stuck
        if (suspendBumpWhenQueueRecovering()) {
            getKDS().showToastMessage(getString(R.string.suspend_bump_while_queue_recover));
            return;
        }

        String guid = getSelectedOrderGuid(userID);// f.getLayout().getEnv().getStateValues().getFocusedOrderGUID();
        if (guid.isEmpty()) return;
        bumpOrderOperation(userID, guid, true);
        refreshSum();//kpp1-320
        getKDS().getSoundManager().playSound(KDSSettings.ID.Sound_bump_order);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    /**
     * after select option in KDSUIDialogTrackerBump dialog,
     * go into this function, do the real bump operation.
     * @param orderGuid
     * @param method
     */
    public void bumpTrackerOrderOperation(String orderGuid,KDSUIDialogTrackerBump.TrackerBumpMethod method)
    {
        switch (method)
        {

            case Page:
            {
                m_ttView.askTTPageOrder(orderGuid);
            }
            break;
            case Remove:
            {
                String ordername = m_ttView.removeOrder(orderGuid);
                getKDS().getBroadcaster().broadcastTrackerBump(ordername);
            }
            break;
            case TrackerID:
            {
                KDSDataOrder order =  m_ttView.getOrders().getOrderByGUID(orderGuid);
                if (order == null) return;
                KDSUIDlgInputTrackerID dlg = new KDSUIDlgInputTrackerID(this, order.getTrackerID(), this);
                dlg.setOrderGuid(orderGuid);
                dlg.show();
            }
            default:
                break;
        }
    }
    /**
     *  Add Clear order and Page support where user can select the order using bump bar and when Bump key is pressed,
     *  bring up two option – Clear & Page; https://table-tracker.readme.io/v2.0/docs/clearing-orders
     *  https://table-tracker.readme.io/v2.0/docs/paging-order
     * @param userID
     * @param orderGuid
     */
    public void bumpTrackerOrder(KDSUser.USER userID, String orderGuid)
    {
        if (orderGuid.isEmpty()) return;
        KDSUIDialogTrackerBump dlg = new KDSUIDialogTrackerBump(this,  this);
        dlg.setOrderGuid(orderGuid);
        dlg.show();

    }

    public boolean isFixedSingleScreenView()
    {
        SettingsBase.StationFunc funcView = getSettings().getFuncView(); //current use what view to show orders.
        if (funcView == SettingsBase.StationFunc.Queue ||
                funcView == SettingsBase.StationFunc.Queue_Expo ||
                funcView == SettingsBase.StationFunc.TableTracker)
            return true;
        return false;
    }

    public void bumpOrderOperation(KDSUser.USER userID, String orderGuid, boolean bRefresView) {

        if (!bRefresView) { //it is from auto bumping
            boolean isScheduleOrder = isScheduleOrder(userID, orderGuid);
            if (isScheduleOrder) {

                if (onBumpScheduleOrder(userID, orderGuid))
                    return;
            }
        }
        String guid = getSelectedOrderGuid(userID);// f.getLayout().getEnv().getStateValues().getFocusedOrderGUID();
        boolean bIsFocusedOrder = orderGuid.equals(guid);
        //get next for focus
        String nextGuid = "";
        String firstOrderGuid = getUserUI(userID).getLayout().getEnv().getStateValues().getFirstShowingOrderGUID();
        KDSDataOrder order = null;
        //synchronized (getKDS().getUsers().getUser(userID).getOrders().m_locker)
        //{
            if (bIsFocusedOrder) {
                nextGuid = getNextOrderGuidToFocus(userID, orderGuid);//"";
            }
            //save it for printing.
            //TimeDog td = new TimeDog();
            order = bumpOrder(userID, orderGuid, bRefresView);
            //td.debug_print_Duration("bumpOrder");
            if (order == null) return;
        //}
        if (isFixedSingleScreenView())
//        if (getKDS().isQueueStation() || getKDS().isQueueExpo())
//        {
//            setSelectedOrderGuid(KDSUser.USER.USER_A, nextGuid);
//            refreshView();
//
//        }
//        else if (getKDS().isTrackerStation())
        {
            setSelectedOrderGuid(KDSUser.USER.USER_A, nextGuid);
            //kpp1-389
            this.getLayout(userID).adjustFocusOrderLayoutFirstShowingOrder(false);

            if (bRefresView)
                getKDS().refreshView();
                //refreshView();//20180314
        }
        else
        {
//            if (this.getSummaryFragment() != null)
//                this.getSummaryFragment().refreshSummary(); //remove this. It do UI drawing in thread. Thre refreshView has refresh sum.
            if (bIsFocusedOrder) {
                setSelectedOrderGuid(userID, nextGuid);
                setSelectedItemGuid(userID, "");

                if (!getUserUI(userID).getPrevCountString().isEmpty() &&
                        getUserUI(userID).getLayout().getLastShowingOrderGuid().equals(firstOrderGuid) && //comment it as KPP1-237, use it as kpp1-254
                        orderGuid.equals(firstOrderGuid) && //KPP1-254 Scroll to the previous page of orders when the last order is bumped off the last page
                        getUserUI(userID).getNextCountString().isEmpty()) {//kpp1-237
                    String estimateFirstOrderGuid = getUserUI(userID).getLayout().getPrevPageOrderGuid(nextGuid);
                    getUserUI(userID).getLayout().getEnv().getStateValues().setFirstShowingOrderGUID(estimateFirstOrderGuid);
                    getUserUI(userID).getLayout().focusOrder(KDSConst.RESET_ORDERS_LAYOUT);

                }
                //kpp1-389
                this.getLayout(userID).adjustFocusOrderLayoutFirstShowingOrder(false);

            }
            if (bRefresView)
                getKDS().refreshView();
            //refreshView(); //20180314
            //2.0.11, enable prep queue
            if (getKDS().isExpeditorStation() || getKDS().isPrepStation())
            {
                if (!getSettings().getBoolean(KDSSettings.ID.Queue_double_bump_expo_order)) //20180313
                    getKDS().getPagerManager().addPagerID(order.getPagerID());
            }
            //print it
            KDSPrinter.HowToPrintOrder howtoprint = KDSPrinter.HowToPrintOrder.values()[(getKDS().getSettings().getInt(KDSSettings.ID.Printer_howtoprint))];
            if (howtoprint == KDSPrinter.HowToPrintOrder.WhileBump )
                printOrder(order);
        }
        //SMS feature
        //if (getKDS().isExpeditorStation())
        getKDS().checkSMS(order, true, null);
        getKDS().checkBroadcastSMSStationStateChanged(orderGuid, order.getOrderName(),order.isAllItemsFinished(), true);
        //KPP1-41
        getKDS().syncOrderToWebDatabase(order, ActivationRequest.iOSOrderState.Done, ActivationRequest.SyncDataFromOperation.Bump);
    }

    /**
     *
     * @param userID
     * @param guid
     * @return
     *  false: don't handle it.
     *  true: handle it.
     */
    public boolean onBumpScheduleOrder(KDSUser.USER userID, String guid)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        KDSDataOrder order = this.getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(guid);
        ScheduleProcessOrder scheduleOrder = (ScheduleProcessOrder)order;
        if (scheduleOrder.is_finsihed())
        {
            return false;
        }
        KDSUIDialogInputReadyQty dlg = new KDSUIDialogInputReadyQty(this, this);
        dlg.setUserID(userID);

        dlg.setOrder((ScheduleProcessOrder) order);

        dlg.show();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
        return true;
    }

    final int MAX_AUTO_BUMP_COUNT = 3;
    //int MAX_AUTO_BUMP_COUNT = 2;

    /**
     * call it in thread
     * @return
     * true: bumped some orders
     */
    public boolean checkAutoBumping()
    {

        if (!isKDSValid()) return false;
        if (getKDS().isQueueView() ||
                getKDS().isQueueExpo()) {
            m_queueView.checkAutoBump();
            return false;
        }
        if (getKDS().isTrackerView())
        {
            m_ttView.checkAutoBumping();//
            return false;
        }

        boolean bEnabled = this.getSettings().getBoolean(KDSSettings.ID.Auto_bump_enabled);

        if (!bEnabled) return false;

        //there is one bug here, this function auto bump queue orders too.
        //add following code.
        if (getKDS().isQueueView() ||
                getKDS().isQueueExpo()) {
            m_queueView.checkAutoBump();
            return false;
        }

        //prevent queue stuck
        if (suspendBumpWhenQueueRecovering()) {
            //getKDS().showToastMessage(getString(R.string.suspend_bump_while_queue_recover));
            return false;
        }

        int nminutes = this.getSettings().getInt(KDSSettings.ID.Auto_bump_minutes);

        //limit its size
        //TimeDog td = new TimeDog();

        ArrayList<String> ar =  getKDS().getUsers().getUserA().getOrders().findTimeoutOrders(nminutes, MAX_AUTO_BUMP_COUNT, false);
        //Log.i(TAG, "Auto bumping=" + KDSUtil.convertIntToString(ar.size()));
        //td.debug_print_Duration("findTimeoutOrders");
        //
        boolean bReturn = false;
        if (ar.size() >0)
        {
            synchronized (getKDS().getUsers().getUserA().getOrders().m_locker) {
                //td.debug_print_Duration("synchronized");
                for (int i = 0; i < ar.size(); i++) {
                    //TimeDog td = new TimeDog();
                    //prevent queue stuck
                    if (suspendBumpWhenQueueRecovering()) {
                        //getKDS().showToastMessage(getString(R.string.suspend_bump_while_queue_recover));
                        return false;
                    }
                    if (checkExpoConfirmationBump(KDSUser.USER.USER_A, ar.get(i), true)) {

                        //break; //kpp1-380, expo confirmation
                    }
                    else
                        bumpOrderOperation(KDSUser.USER.USER_A, ar.get(i), false);

                    //td.debug_print_Duration("bump order time:");
                }
                //td.debug_print_Duration("bumpOrderOperation");
            }

            getKDS().refreshView(); //use message
            bReturn = true;

        }
        if (getKDS().isMultpleUsersMode())
        {
            ar =  getKDS().getUsers().getUserB().getOrders().findTimeoutOrders(nminutes, MAX_AUTO_BUMP_COUNT, false);
            if (ar.size() >0)
            {
                //synchronized (getKDS().getUsers().getUserB().getOrders().m_locker) {
                    for (int i = 0; i < ar.size(); i++) {
                        String guid = ar.get(i);
                        if (checkExpoConfirmationBump(KDSUser.USER.USER_B, guid, true)) {
                            //break; //kpp1-380, expo confirmation
                        }
                        else
                            bumpOrderOperation(KDSUser.USER.USER_B, guid, false);
                    }
                //}
                getKDS().refreshView();
                //bumpOrderInThread(KDSUser.USER.USER_A, ar);
                bReturn = true;

            }

        }
        if (ar.size() >0) {
            //TimeDog td = new TimeDog();
            getKDS().getCurrentDB().clearExpiredBumpedOrders(getKDS().getSettings().getBumpReservedCount());
            //td.debug_print_Duration("checkAutoBumping->clearExpiredBumpedOrders");
        }
        ar.clear();
        checkAutoBumpParkOrders();

        return bReturn;
    }

    public boolean checkAutoBumpParkOrders()
    {
        if (!isKDSValid()) return false;
        boolean bEnabled = this.getSettings().getBoolean(KDSSettings.ID.Auto_bump_enabled);

        if (!bEnabled) return false;
        int nminutes = this.getSettings().getInt(KDSSettings.ID.Auto_bump_park_order_mins);

        int nUserACount = getKDS().getUsers().getUserA().autoBumpParkOrder(nminutes);
        int nUserBCount = 0;
        if (getKDS().isMultpleUsersMode())
        {
            nUserBCount = getKDS().getUsers().getUserB().autoBumpParkOrder(nminutes);
        }

        if (nUserACount>0 ||
                nUserBCount>0)
            return true;
        return false;
    }

    public boolean isLineItemsMode()
    {
        return getKDS().getSettings().getLineItemsViewEnabled();

    }

    private void lineItemsFocusNextAfterBump(KDSUser.USER userID,String focusedOrderGuid, String focusedItemGuid)
    {
        if (!getKDS().getSettings().getLineItemsViewEnabled())//.getBoolean(KDSSettings.ID.LineItems_Enabled))
            return;
        if (!isUserLayoutReady(userID)) return;
        KDSUser user =  getKDS().getUsers().getUser(userID);
        KDSDataOrder order =  user.getOrders().getOrderByGUID(focusedOrderGuid);
        if (order == null)
        {
//            MainActivityFragment f = getMainFragment();
//            if (f == null) return ;

            getUserUI(userID).getLayout().getEnv().getStateValues().setFocusedOrderGUID("");
            getUserUI(userID).getLayout().getEnv().getStateValues().setFocusedItemGUID("");
            return;

        }
        if (order.getItems().getItemByGUID(focusedItemGuid) == null)
            return;

        //user.getOrders().isNoOtherActiveItemsBehindMe(focusedItemGuid);//kpp1-322
        if (getUserUI(userID).getLayout().focusNext().isEmpty())
            opFocusPrev(userID);

        if (order.getItems().isLastActiveItem(focusedItemGuid))
        {
            bumpOrder(userID, focusedOrderGuid, true);
        }


    }

    /**
     * bump selected item
     */
    public void onBumpItem(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        itemBump(userID,getSelectedOrderGuid(userID), getSelectedItemGuid(userID) );
//        if (!isKDSValid()) return ;
//
//        //prevent queue stuck
//        if (suspendBumpWhenQueueRecovering()) {
//            getKDS().showToastMessage(getString(R.string.suspend_bump_while_queue_recover));
//            return;
//        }
//
//        String orderGuid = getSelectedOrderGuid(userID);//
//        if (orderGuid.isEmpty()) return;
//
//        KDSDataOrder order = getKDS().getUsers().getOrderByGUID(orderGuid);//KPP1-129, keep order here
//
//        String itemGuid = getSelectedItemGuid(userID);
//        if (itemGuid.isEmpty()) return;
//
//        if (!KDSStationFunc.itemBump(getKDS().getUsers().getUser(userID), orderGuid, itemGuid))
//            return;
//
//        onRefreshSummary(userID);
//        //this.getSummaryFragment().refreshSummary();
//        //notification
//        notifyPOSItemBumpUnbump(userID, orderGuid, itemGuid);
//        if (getKDS().isExpeditorStation())
//        {
//            if (getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid).isItemsAllBumpedInExp())
//                getKDS().getSoundManager().playSound(KDSSettings.ID.Sound_expo_order_complete);
//        }
//
//        lineItemsFocusNextAfterBump(userID, orderGuid, itemGuid);
//        refreshView(userID);
//
//        getKDS().checkSMS(orderGuid, false); //2.1.10, fix KPP1-23
//        //KDSDataOrder order = getKDS().getUsers().getOrderByGUID(orderGuid); //KPP1-129, move it to above
//        if (order != null) {
//            getKDS().checkBroadcastSMSStationStateChanged(orderGuid, "",order.isAllItemsFinished(), false);
//        }
//        //
//        //https://bematech.atlassian.net/browse/KPP1-62
//        if (order != null) { //if I continue bump order, show crash, KPP1-129
//            KDSDataItem item = order.getItems().getItemByGUID(itemGuid);
//            getKDS().syncItemBumpUnbumpToWebDatabase(order, item, true);
//        }
//        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

    }
    private void notifyPOSItemBumpUnbump(KDSUser.USER userID,  String orderGuid, String itemGuid)
    {
        if (!isKDSValid()) return ;
        KDSDataOrder order  =  getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        if (order == null) return;
        KDSDataItem item = order.getItems().getItemByGUID(itemGuid);
        if (item == null) return;
        KDSPosNotificationFactory.BumpUnbumpType t = KDSPosNotificationFactory.BumpUnbumpType.BUMP_ITEM;
        if (item.getLocalBumped()) {
            if (getKDS().isExpeditorStation())
                t = KDSPosNotificationFactory.BumpUnbumpType.BUMP_EXPEDITOR_ITEM;
            else
                t = KDSPosNotificationFactory.BumpUnbumpType.BUMP_ITEM;
        }
        else
        {
            if (getKDS().isExpeditorStation())
                t = KDSPosNotificationFactory.BumpUnbumpType.UNBUMP_EXPEDITOR_ITEM;
            else
                t = KDSPosNotificationFactory.BumpUnbumpType.UNBUMP_ITEM;
        }
        getKDS().firePOSNotification(order, item, t);

        //for preparation time mode.
        getKDS().getBroadcaster().broadcastItemBumpUnbump(order.getOrderName(), item.getItemName(), item.getLocalBumped());
    }

    private void notifyPOSItemQtyChanged(KDSUser.USER userID,  String orderGuid, String itemGuid)
    {
        if (!isKDSValid()) return ;
        KDSDataOrder order  =  getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        KDSDataItem item = order.getItems().getItemByGUID(itemGuid);
        KDSPosNotificationFactory.BumpUnbumpType t = KDSPosNotificationFactory.BumpUnbumpType.BUMP_ITEM;
        if (item.getLocalBumped()) {
            if (getKDS().isExpeditorStation())
                t = KDSPosNotificationFactory.BumpUnbumpType.BUMP_EXPEDITOR_ITEM;
            else
                t = KDSPosNotificationFactory.BumpUnbumpType.BUMP_ITEM;
        }
        else
        {
            if (getKDS().isExpeditorStation())
                t = KDSPosNotificationFactory.BumpUnbumpType.UNBUMP_EXPEDITOR_ITEM;
            else
                t = KDSPosNotificationFactory.BumpUnbumpType.UNBUMP_ITEM;
        }
        getKDS().firePOSNotification(order, item, t);
    }

    private String getSelectedOrderGuid(KDSUser.USER userID) {
        SettingsBase.StationFunc funcView = getSettings().getFuncView();
        if  (funcView == SettingsBase.StationFunc.Queue ||
                funcView== SettingsBase.StationFunc.Queue_Expo) //(getKDS().isQueueStation() || getKDS().isQueueExpo())
        {
            return m_queueView.getFocusedGuid();
        }
        else if (funcView == SettingsBase.StationFunc.TableTracker)//(getKDS().isTrackerStation())
        {
            return m_ttView.getFocusedGuid();
        }
        else {
//            MainActivityFragment f = getMainFragment();
//            if (f == null) return "";
            if (!isUserLayoutReady(userID)) return "";
            String guid = getUserUI(userID).getLayout().getEnv().getStateValues().getFocusedOrderGUID();
            return guid;
        }
    }

    private String getSelectedItemGuid(KDSUser.USER userID) {

        SettingsBase.StationFunc funcView = getSettings().getFuncView();
        if (funcView == SettingsBase.StationFunc.Queue ||
                funcView== SettingsBase.StationFunc.Queue_Expo) //(getKDS().isQueueStation() || getKDS().isQueueExpo())
            return "";
        else if (funcView == SettingsBase.StationFunc.TableTracker)// (getKDS().isTrackerStation())
            return "";
        else {
//            MainActivityFragment f = getMainFragment();
//            if (f == null) return "";
            if (!isUserLayoutReady(userID)) return "";
            String guid = getUserUI(userID).getLayout().getEnv().getStateValues().getFocusedItemGUID();
            return guid;
        }
    }

    private void setSelectedOrderGuid(KDSUser.USER userID, String guid) {
        SettingsBase.StationFunc funcView = getSettings().getFuncView();
        //if (getKDS().isQueueStation() || getKDS().isQueueExpo())
        if (funcView == SettingsBase.StationFunc.Queue ||
                funcView == SettingsBase.StationFunc.Queue_Expo)
        {
            m_queueView.focusOrder(guid);
        }
        else if ( funcView == SettingsBase.StationFunc.TableTracker)//(getKDS().isTrackerStation())
        {
            m_ttView.focusOrder(guid);
        }
        else {
//            MainActivityFragment f = getMainFragment();
//            if (f == null) return;
//            if (f.getLayout(userID) == null)
//                return;
            if (!isUserLayoutReady(userID)) return ;
            getUserUI(userID).getLayout().getEnv().getStateValues().setFocusedOrderGUID(guid);
        }

    }

    private void setSelectedItemGuid(KDSUser.USER userID, String guid) {
//        MainActivityFragment f = getMainFragment();
//        if (f == null) return;
//        if (f.getLayout(userID) == null)
//            return;
        if (!isUserLayoutReady(userID)) return;
        getUserUI(userID).getLayout().getEnv().getStateValues().setFocusedItemGUID(guid);
    }



    private void opShowActiveStations(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        KDSUIIPSearchDialog dlg = new KDSUIIPSearchDialog(this, KDSUIIPSearchDialog.IPSelectionMode.Zero, null, "");
        dlg.setKDSCallback(getKDS());//
        dlg.setKDSUser(getKDS().getUsers().getUser(userID));
        dlg.show();
        dlg.setKDSUser(getKDS().getUsers().getUser(userID));
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }



    private void opUnbump(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        String orderGuid = getSelectedOrderGuid(userID);// f.getLayout().getEnv().getStateValues().getFocusedOrderGUID();
        //if (orderGuid.isEmpty()) return;

        String itemGuid = getSelectedItemGuid(userID);

        if (isLineItemsMode())
        {
            onUnbumpOrder(userID);
        }
        else {
            if (itemGuid.isEmpty() || orderGuid.isEmpty()) {
                onUnbumpOrder(userID);
            } else {
                onUnbumpItem(userID);
            }
        }
        getKDS().schedule_process_update_to_be_prepare_qty(true);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void opUnbumpLast(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        onUnbumpLastOrder(userID);
        getKDS().getSoundManager().playSound(KDSSettings.ID.Sound_unbump_order);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void onUnbumpLastOrder(KDSUser.USER userID)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        if (!getKDS().getSettings().getLineItemsViewEnabled()){//.getBoolean(KDSSettings.ID.LineItems_Enabled)) {
            String orderGuid = getKDS().getCurrentDB().ordersLoadRecentBumpedLastOrder(userID.ordinal());
            if (orderGuid.isEmpty()) {
                showToastMessage(getString(R.string.no_more_unbump_order));
                return;
            }
            restoreOrder(orderGuid);
            getKDS().schedule_process_update_to_be_prepare_qty(true);
        }
        else
        {
            onUnbumpLastLineItem(userID);
        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void onUnbumpLastLineItem(KDSUser.USER userID)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        if (!getKDS().getSettings().getLineItemsViewEnabled())// .getBoolean(KDSSettings.ID.LineItems_Enabled))
            return;

        String itemGuid = getKDS().getCurrentDB().lineItemsLoadRecentBumpedLastItem(userID.ordinal());
        if (itemGuid.isEmpty()) {
            showToastMessage(getString(R.string.no_more_unbump_item));
            return;
        }
        String orderGuid = getKDS().getCurrentDB().lineItemsGetOrderGuidFromItemGuid(itemGuid);
        if (orderGuid.isEmpty()) {
            showToastMessage(getString(R.string.no_more_unbump_item));
            return;
        }
        KDSDataOrder order =getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        if (order == null)
        {
            restoreOrder(orderGuid);
            order =getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);

        }

        unbumpItem(userID, orderGuid, itemGuid);

        getKDS().schedule_process_update_to_be_prepare_qty(true);
        refreshView(userID);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void onUnbumpOrder(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        Intent intent = new Intent(MainActivity.this, KDSActivityUnbump.class);
        String stations = getKDS().getStationID();

        intent.putExtra("func", KDSConst.SHOW_UNBUMP_DLG);
        intent.putExtra("station", stations);
        intent.putExtra("screen", userID.ordinal());// getKDS().getScreen());
        startActivityForResult(intent, KDSConst.SHOW_UNBUMP_DLG);//SHOW_PREFERENCES);
        //startActivity(i);//SHOW_PREFERENCES);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void onUnbumpItem(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        itemUnbump(userID,getSelectedOrderGuid(userID), getSelectedItemGuid(userID) );
//        if (!isKDSValid()) return ;
//        String orderGuid = getSelectedOrderGuid(userID);// f.getLayout().getEnv().getStateValues().getFocusedOrderGUID();
//        if (orderGuid.isEmpty()) return;
//
//        String itemGuid = getSelectedItemGuid(userID);
//
//        unbumpItem(userID, orderGuid, itemGuid);
//
//        getKDS().checkSMS(orderGuid, false); //2.1.10
//        //https://bematech.atlassian.net/browse/KPP1-62
//        KDSDataOrder order = getKDS().getUsers().getOrderByGUID(orderGuid);
//        getKDS().syncItemBumpUnbumpToWebDatabase(order,order.getItems().getItemByGUID(itemGuid), false );
//        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

    }

    private void unbumpItem(KDSUser.USER userID,String orderGuid, String itemGuid)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!itemGuid.isEmpty()) {
            KDSStationFunc.itemUnbump(getKDS().getUsers().getUser(userID), orderGuid, itemGuid);

        }
        notifyPOSItemBumpUnbump(userID, orderGuid, itemGuid);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case KDSConst.SHOW_UNBUMP_DLG: {
                if (resultCode == RESULT_OK) {
                    String guid = data.getStringExtra("guid");
                    String itemguid = "";//data.getStringExtra("itemguid");
                    if (isLineItemsMode())
                    {
                        itemguid = data.getStringExtra("itemguid");
                        int screen = data.getIntExtra("screen", 0);
                        unbumpItem(KDSUser.USER.values()[screen], guid, itemguid);
                    }
                    else
                        restoreOrder(guid);
                    getKDS().getSoundManager().playSound(KDSSettings.ID.Sound_unbump_order);
                }
            }
            break;
            case KDSConst.SHOW_UNPARK_DLG: {
                if (resultCode == RESULT_OK) {
                    String guid = data.getStringExtra("guid");
                    int screen = data.getIntExtra("screen", 0);

                        unparkOrder(KDSUser.USER.values()[screen], guid);
                }
            }
            break;
            case KDSConst.SHOW_PREFERENCES: {
                if (!isKDSValid()) return ;
                m_bEnableRefreshTimer = true;
                getKDS().updateSettings(this.getApplicationContext());
                updateUISettings();


            }
            break;
            case KDSConst.SHOW_UTILITY:
            {
                if (!isKDSValid()) return ;
                refreshWithNewDbData();
            }
            break;
            case KDSConst.SHOW_LOGIN:
            {
                if (!isKDSValid()) return ;
                if (resultCode == ActivityLogin.Login_Result.Agreement_disagree.ordinal())
                {//kpp1-325
                   this.setResult(0);
                   this.finish();
                }
                //
                m_activation.setDoLicensing( false );
                if (m_activation.isStoreChanged())
                {
                    m_activation.restStoreChangedFlag();
                    doClearDB(false);

                }
                String registeredStationID = m_activation.findMyRegisteredID(); //kpp1-340
                if (registeredStationID.isEmpty()) {
                    if (getKDS().getStationID().isEmpty())
                        inputStationID();
                }
                else
                    afterInputStationID(registeredStationID);


            }
            default:
                break;
        }

    }

    /**
     * restore bumped order
     *
     * @param orderGuid
     */

    private void restoreOrder(String orderGuid) {

        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        if (orderGuid.isEmpty()) return;
        KDSUser.USER userID = getKDS().getUsers().orderUnbump(orderGuid);

        //refreshWithNewDbDataAndFocusFirst(); //kpp1-251, use below line code
        this.getUserUI(userID).getLayout().adjustFocusOrderLayoutFirstShowingOrder(false);

        notifiyPOSOrderUnbump(userID, orderGuid);
        //KPP1-41
        KDSDataOrder order = getKDS().getUsers().getOrderByGUID(orderGuid);
        if (order != null) {
            ActivationRequest.iOSOrderState iosstate = ActivationRequest.iOSOrderState.New;
            if (order.getFinishedItemsCount()>0)
                iosstate = ActivationRequest.iOSOrderState.Preparation;
            getKDS().syncOrderToWebDatabase(order, iosstate, ActivationRequest.SyncDataFromOperation.Unbump);
        }
        refreshPrevNext(userID); //kpp1-251, while bump then unbump last one, "next" count is not correct.
        this.refreshView();//kpp1-316
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void notifiyPOSOrderUnbump(KDSUser.USER userID, String orderGuid)
    {
        if (!isKDSValid()) return ;
        KDSDataOrder order = getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        if (getKDS().isExpeditorStation())
            getKDS().firePOSNotification(order, null, KDSPosNotificationFactory.BumpUnbumpType.UNBUMP_EXPEDITOR_ORDER);
        else
            getKDS().firePOSNotification(order, null, KDSPosNotificationFactory.BumpUnbumpType.UNBUMP_ORDER);

        //preparation time mode.
        getKDS().getBroadcaster().broadcastItemBumpUnbump(order, false);
    }

    private void unparkOrder(KDSUser.USER userID, String orderGuid) {

        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        KDSStationFunc.orderUnpark(getKDS().getUsers().getUser(userID), orderGuid);
        refreshWithNewDbData();
        String guid = getFirstOrderGuidToFocus(userID);// getKDS().getUsers().getUser(userID).getOrders().getFirstOrderGuid();
        this.onSetFocusToOrder(guid);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

    }

    /**
     * ip selection dialog
     *
     * @param dlg
     */
    public void onKDSDialogCancel(KDSUIDialogBase dlg) {
         if (dlg instanceof KDSUIDlgAgreement)
        {//kpp1-325
            KDSUIDlgAgreement.setAgreementAgreed(false);
            this.finish();
        }
        else if (dlg instanceof KDSUIDialogBase) {
            if (dlg.getTag() == null) return;
            Confirm_Dialog confirm = (Confirm_Dialog) dlg.getTag();
            switch (confirm) {
                case Clear_DB: {
                }
                break;
                case Export_Data: {
                }
                break;
                case Import_Data: {
                }
                break;
                case Restart_me:
                {
                }
                break;
                case Load_Old_Data:
                { //don't need old data.
                    MainActivity.this.doClearDB(false);
                    String stationID = getKDS().getSettings().getString(KDSSettings.ID.KDS_ID);
                    if (stationID.isEmpty()) {
                        inputStationID();
                    }
                    //import_kds_setting(KDSDBBase.DB_FOLDER_NAME, false);
                }
                break;
                case CONFIRM_BUMP:
                    break;
                default: {
                    break;
                }
            }

        }
    }

   // static final String DEFAULT_PASSWORD = "123";
    /**
     * ip selection dialog
     *
     * @param dlg
     * @param obj
     */
    public void onKDSDialogOK(KDSUIDialogBase dlg, Object obj)// ArrayList<String> stations)
    {
        if (!isKDSValid()) return ;
        if (dlg instanceof KDSUIIPSearchDialog) {
            KDSUIIPSearchDialog d = (KDSUIIPSearchDialog) dlg;

            onSearchIpDialogOk((KDSUser)(d.getKdsUser()), d.getSelectedStation());//obj);
        } else if (dlg instanceof KDSUIDialogSort) {
            KDSUIDialogSort d = (KDSUIDialogSort) dlg;


            //20171221
            if (isUserLayoutReady(KDSUser.USER.USER_A))
                getUserUI(KDSUser.USER.USER_A).getLayout().getEnv().getStateValues().setFirstShowingOrderGUID("");
            if (getKDS().isMultpleUsersMode()) {
                if (isUserLayoutReady(KDSUser.USER.USER_B))
                    getUserUI(KDSUser.USER.USER_B).getLayout().getEnv().getStateValues().setFirstShowingOrderGUID("");
            }
            //
            sortOrders(((KDSUser)(d.getKdsUser())).getUserID(), (KDSSettings.OrdersSort) ((KDSUIDialogSort) dlg).getResult());
            //20171221
            threadrefresh_FocusFirst();
            //refreshView();

        } else if (dlg instanceof KDSUIDialogOrderStatus) {//user selected the order status, write it to remote folder.
            KDSUIDialogOrderStatus d = (KDSUIDialogOrderStatus) dlg;
            KDSUIDialogOrderStatus.OrderState state = (KDSUIDialogOrderStatus.OrderState) d.getResult();
            String strStatus = KDSUIDialogOrderStatus.getStatusString(this.getApplicationContext(), state);
           // String strXml = KDSXMLParserOrder.createOrderStatusXmlString(getKDS().getStationID(), d.getOrderName(), strStatus);
            String strXml = KDSPosNotificationFactory.createOrderStatusNotification(getKDS().getStationID(), d.getOrderName(), strStatus);

            Object objSrc = d.getData();

            String fileName = getKDS().getStationID() + "_" + d.getOrderName() + "_" + strStatus + ".xml";
            getKDS().writeXmlToPOSOrderInfo(objSrc, strXml, fileName);



        } else if (dlg instanceof KDSUIDialogInputID) {//input the ID
            m_inputStationIDDlg = null;
            getKDS().setStationAnnounceEventsReceiver(null);
            String s = (String) dlg.getResult();
            afterInputStationID(s);

        } else if (dlg instanceof KDSUIDialogConfirm) {
            KDSUIDialogConfirm d = (KDSUIDialogConfirm) dlg;
            if ((Confirm_Dialog) d.getTag() ==Confirm_Dialog.CONFIRM_BUMP) {
                KDSUser.USER userID = (KDSUser.USER) d.getTage("userid");
                String orderGuid = (String) d.getTage("orderguid");
                afterConfirmBumpOrder(userID, orderGuid);
            }

        } else if (dlg instanceof KDSUIDialogMore) {
            KDSUIDialogMore d = (KDSUIDialogMore) dlg;
            KDSUIDialogMore.FunctionMore func = (KDSUIDialogMore.FunctionMore) d.getResult();
            KDSUser.USER userID = d.getUserID();
            doMoreFunction(userID, func);

        }
        else if (dlg instanceof KDSUIDlgInputPassword)
        {
            String pwd =(String) dlg.getResult();

            String settingsPwd = getKDS().getSettings().getString(KDSSettings.ID.Settings_password);
            if (settingsPwd.isEmpty())
                settingsPwd =KDSConst.DEFAULT_PASSWORD;// "123";
            if (pwd.isEmpty() || ( !pwd.equals(settingsPwd)) )
            {
                KDSUIDialogBase errordlg = new KDSUIDialogBase();
                errordlg.createInformationDialog(this, getString(com.bematechus.kdslib.R.string.error), this.getString(com.bematechus.kdslib.R.string.password_incorrect), false);
                errordlg.show();
                //KDSUtil.showErrorMessage(this, this.getString(R.string.password_incorrect));
            }
            else if (pwd.equals(settingsPwd))
            {
                showSettingsDialog();
            }
        }
        else if (dlg instanceof KDSUIDialogInputReadyQty)
        {//schedule order
            KDSUIDialogInputReadyQty dialog = (KDSUIDialogInputReadyQty)dlg;

            ScheduleProcessOrder order =dialog.getOrder();
            KDSUser.USER userID = dialog.getUserID();
            int nQty = (int)dialog.getResult();
            schedule_order_ready_qty_changed(userID, order, nQty);

        }
        else if (dlg instanceof KDSUIDialogTrackerBump)
        {//tracker bump
            KDSUIDialogTrackerBump d = (KDSUIDialogTrackerBump) dlg;
            String orderGuid = d.getOrderGuid();
            KDSUIDialogTrackerBump.TrackerBumpMethod method =(KDSUIDialogTrackerBump.TrackerBumpMethod) d.getResult();
            bumpTrackerOrderOperation(orderGuid, method);
        }
        else if (dlg instanceof KDSUIDlgInputTrackerID)
        {
            KDSUIDlgInputTrackerID d = (KDSUIDlgInputTrackerID) dlg;
            m_ttView.changeTrackerID(d.getOrderGuid(), (String)d.getResult());
        }
        else if (dlg instanceof KDSUIDlgDbCorrupt)
        {
            KDSUIDlgDbCorrupt d = (KDSUIDlgDbCorrupt)dlg;
            KDSUIDlgDbCorrupt.DB_Corrupt_Operation op = (KDSUIDlgDbCorrupt.DB_Corrupt_Operation) d.getResult();
            doDbCorruptOperations(op);

        }
        else if (dlg instanceof KDSUIDialogImportSettings)
        {
            KDSUIDialogImportSettings d = (KDSUIDialogImportSettings)dlg;
            String path = d.getSelectedPath();
            MainActivity.this.import_settings(path, true);

        }
        else if (dlg instanceof KDSUIDlgAgreement)
        {
            KDSUIDlgAgreement.setAgreementAgreed(true);
        }
        else if (dlg instanceof KDSUIDialogBase) {
            if (dlg.getTag() == null) return;
            Confirm_Dialog confirm = (Confirm_Dialog) dlg.getTag();
            switch (confirm) {
                case Clear_DB: {
                    MainActivity.this.doClearDB(true);
                }
                break;
                case Export_Data: {
                    MainActivity.this.export_settings(DEFAULT_BACKUP_FOLDER);
                }
                break;
                case Import_Data: {
                    MainActivity.this.import_settings(DEFAULT_BACKUP_FOLDER, false);
                }
                break;
                case Restart_me:
                {
                  //  restartMe();
                }
                break;
                case Load_Old_Data:
                {//keep old database data, and load settings from kdsdata folder.
                    m_bSuspendChangedEvent = true;
                    import_kds_setting(KDSDBBase.DB_FOLDER_NAME, false);
                    m_bSuspendChangedEvent = false;
                    onSharedPreferenceChanged(null, "StationsRelation");
                    onSharedPreferenceChanged(null, "");
                    if (!isKDSValid()) return;
                    getKDS().updateSettings(this);
                    String stationID = getKDS().getSettings().getString(KDSSettings.ID.KDS_ID);
                    if (stationID.isEmpty()) {
                        inputStationID();
                    }
                }
                break;
                case Logout:
                {
                    doLogout();
                }
                break;
                default: {
                    break;
                }
            }

        }



    }

    private void updateUISettings()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        m_uiUserA.updateSettings(getSettings());
        if (getKDS().isMultpleUsersMode())
            m_uiUserB.updateSettings(getSettings());
        refreshPrevNext(KDSUser.USER.USER_A);
        refreshPrevNext(KDSUser.USER.USER_B);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void sortOrders(KDSUser.USER userID, KDSSettings.OrdersSort orderSort) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        KDSConst.OrderSortBy sortBy = KDSSettings.getOrderSortBy(orderSort);
        KDSConst.SortSequence sortSequence = KDSSettings.getOrderSortSequence(orderSort);
        boolean bRushFront = getKDS().getSettings().getBoolean(KDSSettings.ID.Orders_sort_rush_front);
        boolean bFinishedFront = getKDS().getSettings().getBoolean(KDSSettings.ID.Orders_sort_finished_front);

        getKDS().getUsers().getUser(userID).getOrders().setSortMethod(sortBy, sortSequence, bRushFront, bFinishedFront);


        this.refreshView(userID);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    /**
     * In current kds, just the transfer order feature need the "OK" button in ip searching dialog.
     *
     * @param user
     * @param obj
     *  The selected kdsstationip object
     */
    private void onSearchIpDialogOk(KDSUser user, Object obj) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (obj == null) return;
        //ArrayList<String> stations = (ArrayList<String>) obj;
        KDSStationIP station = (KDSStationIP)obj;
        if (getSelectedItemGuid(user.getUserID()).isEmpty())
            transferFocusedOrderToStations(user, station);
        else
            transferFocusedItemToStations(user, station);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }


    private void transferFocusedOrderToStations(KDSUser user, KDSStationIP station) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;

        KDSUser.USER userID = user.getUserID();
        if (!isUserLayoutReady(userID)) return;

        String toStation = "";

        String ip = "";
        String port = "";
        toStation = station.getID();
        int toScreen = station.getScreen();

        String guid = getLayout(userID).getEnv().getStateValues().getFocusedOrderGUID();

        //get next for focus
        String nextGuid = getNextOrderGuidToFocus(userID, guid);// getKDS().getUsers().getUser(userID).getOrders().getNextOrderGUID(guid);
        if (nextGuid.isEmpty())
            nextGuid = getFirstOrderGuidToFocus(userID);// getKDS().getUsers().getUser(userID).getOrders().getFirstOrderGuid();
        setSelectedOrderGuid(userID, nextGuid);
        setSelectedItemGuid(userID, "");
        //save it for notification
        KDSDataOrder order = user.getOrders().getOrderByGUID(guid);
        //transfer
        getKDS().operationTransferSelectedOrder(user.getUserID(), toStation,toScreen, guid);
        //print it. if set it print order when transfer, print it.
        KDSPrinter.HowToPrintOrder howtoprint = KDSPrinter.HowToPrintOrder.values()[(getKDS().getSettings().getInt(KDSSettings.ID.Printer_howtoprint))];
        if ( howtoprint == KDSPrinter.HowToPrintOrder.WhileTransfer) {
            printOrder(order);
        }
        this.getSummaryFragment().refreshSummary();
        notifyPOSOrderBumpByTransfer(order);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    /**
     *
     * transfer focused item to given station.
     * */
    private void transferFocusedItemToStations(KDSUser user, KDSStationIP station) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        if (!isUserLayoutReady(user.getUserID())) return;

        // ArrayList<String> stations = stationsIP;
        KDSUser.USER userID = user.getUserID();
        String itemGuid = getLayout(userID).getEnv().getStateValues().getFocusedItemGUID();
        String orderGuid = getFocusedOrderGUID(userID);
        if (orderGuid.isEmpty()) return;
        if (itemGuid.isEmpty()) return;



        String toStation = "";

        String ip = "";
        String port = "";
        toStation = station.getID();
        int toScreen = station.getScreen();


        KDSDataOrder order =  getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        if (order.getItems().getCount() == 1) {
            transferFocusedOrderToStations(user, station);
            return;
        }
        KDSDataOrder orderTransfer = new KDSDataOrder();
        order.copyOrderInfoTo(orderTransfer);
        KDSDataItem item =  order.getItems().getItemByGUID(itemGuid);
        orderTransfer.getItems().addComponent(item);

        //get next for focus
        String nextGuid = getNextItemGuid(userID);// getKDS().getUsers().getUser(userID).getOrders().getNextOrderGUID(guid);

        setSelectedItemGuid(userID, nextGuid);
        //transfer
        getKDS().operationTransferItem(user.getUserID(), toStation,toScreen, orderTransfer);

        this.getSummaryFragment().refreshSummary();
        notifyPOSOrderBumpByTransfer(order);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }


    private void notifyPOSOrderBumpByTransfer(KDSDataOrder order)
    {
        if (!isKDSValid()) return ;
        getKDS().firePOSNotification(order, null, KDSPosNotificationFactory.BumpUnbumpType.BUMP_BY_ORDER_TRNANSFER);
    }


    public boolean isAnyOrderSelected(KDSUser.USER userID) {
        if (!isKDSValid()) return  false;
        if (this.getSelectedOrderGuid(userID).isEmpty()) {

            return false;
        }
        if (getKDS().getUsers().getUser(userID).getOrders().getCount() <= 0) {

            return false;
        }
        return true;
    }

    private void opTransfer(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        if (!isAnyOrderSelected(userID)) {
            showToastMessage(getString(R.string.transfer_select_order_warn));
            return;
        }

        String stationID = getSettings().getString(KDSSettings.ID.Transfer_default_station);
        if (getSettings().getBoolean(KDSSettings.ID.Transfer_auto_to_default))
        {

            if (getKDS().getStationsConnections().findActivedStationByID(stationID) != null) {
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Transfer to default station #" + stationID);
                KDSStationIP toStation = new KDSStationIP();
                toStation.setID(stationID);
                onSearchIpDialogOk(getKDS().getUsers().getUser(userID),toStation);
            }
            else
            {
                String s = getString(R.string.transfer_default_station_offline);
                s = s.replace("#", stationID);
                showToastMessage(s);
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + s);
            }

        }
        else {
            KDSUIIPSearchDialog dlg = new KDSUIIPSearchDialog(this, KDSUIIPSearchDialog.IPSelectionMode.Single, this, this.getString(R.string.transfer_select_station_title));
            dlg.setKDSCallback(getKDS());//
            dlg.setSelf(false); //hide self now. //Also on transfer the station you are on shows up. You should not be able to transfer a station to its own station.
            dlg.setShowMultipleUsers(true);
            dlg.setDefaultStationID(stationID);
            //dlg.setTag(v);
            dlg.setKDSUser(getKDS().getUsers().getUser(userID));
            dlg.show();
            dlg.setKDSUser(getKDS().getUsers().getUser(userID));
        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }


    final static int TEST_COUNT = 1;
    private void opTest(KDSUser.USER userID) {

        //KDSPrintImage prn = new KDSPrintImage();
        //prn.test();


        //KDSEmail.sendErrorTo("kds", "kds version 1.02", "error .........err");
        //KDSEmail.sendTo("kds", "kds version 1.02", "error .........err");
//        String ss = null;
//        ss.toString(); //email test

        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        for (int i = 0; i < TEST_COUNT; i++)
            opAddNewOrder(userID);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

        //test
        //getKDS().getStatisticDB().outputOrdersTableDataSql(getKDS().getStatisticDB(), "orders", "");
        //kpp1-335
        //DlgCleaningPinchout d = new DlgCleaningPinchout(this);
        //d.show();
    }


    private void opSort(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        if (this.getSettings().getInt(KDSSettings.ID.Order_Sort) != KDSSettings.OrdersSort.Manually.ordinal())
            return;
        KDSConst.OrderSortBy sortBy = getKDS().getUsers().getUser(userID).getOrders().getSortBy();
        KDSConst.SortSequence sortSequence = getKDS().getUsers().getUser(userID).getOrders().getSortSequence();
        KDSSettings.OrdersSort orderSort = KDSSettings.convertSortOption(sortBy, sortSequence);


        KDSUIDialogSort dlg = new KDSUIDialogSort(this, orderSort, this);
        dlg.setKDSUser(getKDS().getUsers().getUser(userID));
        dlg.show();
        dlg.setKDSUser(getKDS().getUsers().getUser(userID));
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void opPageOrder(KDSUser.USER userID)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        String orderGuid = getSelectedOrderGuid(userID);
        if (orderGuid.isEmpty()) return;
        KDSUser user = getKDS().getUsers().getUser(userID);

        KDSDataOrder order = user.getOrders().getOrderByGUID(orderGuid);
        if (order == null) return;
        getKDS().getPagerManager().addPagerID(order.getPagerID());
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

    }

    private void opPark(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        String orderGuid = getSelectedOrderGuid(userID);
        if (orderGuid.isEmpty()) return;
        KDSUser user = getKDS().getUsers().getUser(userID);

        KDSDataOrder order = user.getOrders().getOrderByGUID(orderGuid);
        if (order == null) return;
        //get next for focus
        String nextGuid =getNextOrderGuidToFocus(userID, orderGuid);// getKDS().getUsers().getUser(userID).getOrders().getNextOrderGUID(orderGuid);
        if (nextGuid.isEmpty())
            nextGuid =getFirstOrderGuidToFocus(userID);// getKDS().getUsers().getUser(userID).getOrders().getFirstOrderGuid();


        KDSStationFunc.orderPark(user, order);
        setSelectedOrderGuid(userID, nextGuid);
        setSelectedItemGuid(userID, "");
        user.refreshView();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void opUnpark(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        Intent intent = new Intent(MainActivity.this, KDSActivityUnbump.class);
        String stations = getKDS().getStationID();

        intent.putExtra("func", KDSConst.SHOW_UNPARK_DLG);
        intent.putExtra("station", stations);
        intent.putExtra("screen", userID.ordinal());// getKDS().getScreen());
        startActivityForResult(intent, KDSConst.SHOW_UNPARK_DLG);//SHOW_PREFERENCES);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private boolean m_bShowingMenu = false;
    private void showPopupMenu(View v) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (m_bShowingMenu)
            return;
        m_bShowingMenu = true;
        PopupMenu popup = new PopupMenu(this, v);  //建立PopupMenu对象
        popup.getMenuInflater().inflate(R.menu.menu_main,   //压入XML资源文件
                popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return MainActivity.this.onOptionsItemSelected(item);
            }
        });

        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                m_bShowingMenu = false;
            }
        });
        popup.show();

        //>>>>>>>>>>>>>>>>>>>>>>>>>> IMPORTANT <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        //this will map the keycode, please check it if use this KDS in new android API.
        try {
            Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Method method = field.get(popup).getClass().getMethod("getPopup");
            ListPopupWindow lw = (ListPopupWindow) method.invoke(field.get(popup));


            lw.getListView().setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (event.getRepeatCount() == 0)
                            KDSKbdRecorder.convertKeyEvent(keyCode, event);

                    }

                    return false;
                }
            });
            boolean bhide = (this.getSettings().getBoolean(KDSSettings.ID.Hide_navigation_bar));
            KDSUtil.enableSystemVirtualBar(lw.getListView(), !bhide);
            //mHelper.setForceShowIcon(true);
        } catch (Exception err) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , err);
            //KDSLog.e(TAG, KDSUtil.error( err));
        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

    }

    public void opMore(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        KDSUIDialogMore d = new KDSUIDialogMore(this, getFocusedUserID(), this);
        d.show();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    int m_nTestCount = 0;


    public KDSUserUI getUserUI(KDSUser.USER userID) {
        if (userID == KDSUser.USER.USER_A)
            return m_uiUserA;
        else
            return m_uiUserB;
    }

    Random m_randomItems = new Random();
    private void opAddNewOrder(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        //TimeDog t = new TimeDog();
        m_nTestCount++;
        String strOrderName = "Order #" + KDSUtil.convertIntToString(m_nTestCount);

        int nItems = m_randomItems.nextInt(5);
        nItems = Math.abs(m_randomItems.nextInt() % 5) +1;
//        if (m_nTestCount%2 ==1)
//            nItems =15;// Math.abs(m_randomItems.nextInt() % 5) +1;
//        else
//            nItems = 1;
        KDSDataOrder order = KDSDataOrder.createTestOrder(strOrderName, nItems, getKDS().getStationID(), userID.ordinal()); // rows = (i+2) * 6  +3 +titlerows;
        //KDSDataOrder order = KDSDataOrder.createTestSmartOrder(strOrderName, nItems, getKDS().getStationID()); // rows = (i+2) * 6  +3 +titlerows;
       // KDSDataOrder order = KDSDataOrder.createTestPrepOrder(strOrderName, nItems, getKDS().getStationID()); // rows = (i+2) * 6  +3 +titlerows;
        //preparation, 20180104
        getKDS().getCurrentDB().prep_add_order_items(order);


        getKDS().doOrderFilter(order, "",false,true, true);
        //t.debug_print_Duration("opAddNewOrder2");
        getKDS().refreshView(KDSUser.USER.USER_A, KDS.RefreshViewParam.None);
        getKDS().refreshView(KDSUser.USER.USER_B, KDS.RefreshViewParam.None);
        //t.debug_print_Duration("opAddNewOrder3");
        int n = getKDS().getSettings().getInt(KDSSettings.ID.Sum_position);
        KDSSettings.SumPosition pos = KDSSettings.SumPosition.values()[n];

        this.getUserUI(KDSUser.USER.USER_A).refreshSum(KDSUser.USER.USER_A, pos);
        this.getUserUI(KDSUser.USER.USER_B).refreshSum(KDSUser.USER.USER_B, pos);
        //getKDS().syncOrderToWebDatabase(order, ActivationRequest.iOSOrderState.New, ActivationRequest.SyncDataFromOperation.New);
        // t.debug_print_Duration("opAddNewOrder4");
        //this.getSummaryFragment().refreshSummary();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }


    public void onBtnLogoClicked(View v) {

        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        showPopupMenu(v);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

    }

    private MainActivityFragment getMainFragment() {
        MainActivityFragment f = (MainActivityFragment) (getFragmentManager().findFragmentById(R.id.fragmentMain));
        return f;
    }

    private MainActivityFragmentSum getSummaryFragment() {

        MainActivityFragmentSum f = (MainActivityFragmentSum) (getFragmentManager().findFragmentById(R.id.fragmentSummary));
        return f;
    }

    private KDSLayout getLayout(KDSUser.USER userID) {
        MainActivityFragment f = getMainFragment();
        if (f == null) return null;
        return f.getLayout(userID);
    }

    private View getLinear(KDSUser.USER userID) {
        MainActivityFragment f = getMainFragment();
        if (f == null) return null;
        return f.getLinear(userID);
    }

    private View getTopSum(KDSUser.USER userID) {
        MainActivityFragment f = getMainFragment();
        if (f == null) return null;
        return f.getTopSum(userID);
    }

    private View getFocusIndicator(KDSUser.USER userID) {
        MainActivityFragment f = getMainFragment();
        if (f == null) return null;
        return f.getFocusIndicator(userID);
    }


    boolean m_bSuspendChangedEvent = false;

    public void suspendChangedEvent(boolean bSuspend)
    {
        m_bSuspendChangedEvent = bSuspend;
    }

    private void onStationFunctionChanged(KDSSettings.StationFunc funcNew)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");

        if (funcNew != KDSSettings.StationFunc.Queue &&
                funcNew != KDSSettings.StationFunc.Queue_Expo)
        {
            initAfterKDSServiceConnected();
            refreshView();
        }
        if (funcNew == KDSSettings.StationFunc.Queue ||
                funcNew == KDSSettings.StationFunc.Queue_Expo )
        {
            initQueueStationAfterKDSServiceConnected();
            refreshView(); //2.0.11

        }
        else if (funcNew == KDSSettings.StationFunc.TableTracker)
        {
            initTrackerStationAfterKDSServiceConnected();
            refreshView(); //2.0.11

        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }
    /**
     * interface implements
     *
     * @param prefs
     * @param key
     *  If it == "", just refresh all.
     */
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (!isKDSValid()) return ;
        //backup all new settings to kdsdata folder.
        if (m_bSuspendChangedEvent) return;
        if (Activation.isActivationPrefKey(key))
            return;

        KDSSettings settingsBackup = new KDSSettings(this.getApplicationContext());
        settingsBackup.loadSettings(this.getApplicationContext());
        settingsBackup.exportToFolder(this.getApplicationContext(), KDSDBBase.getSDDBFolderWithLastDividChar());

        String presentStationID = getKDS().getStationID();

        SettingsBase.StationFunc funcView = getSettings().getFuncView();

        if (key.equals("tabdisp_enabled"))
        {
            boolean b = prefs.getBoolean(key, false);
            if (!b)
            {
                TabDisplay.TabButtonData btnData = new TabDisplay.TabButtonData("", KDSSettings.TabFunction.MAX_COUNT);

                onTabClicked(btnData);
            }
        }



        //if (getKDS().isQueueStation() || getKDS().isQueueExpo())
        if (funcView == SettingsBase.StationFunc.Queue ||
                funcView == SettingsBase.StationFunc.Queue_Expo)
        {
            queueOnSharedPreferenceChanged(prefs, key);
            return;
        }
        else if (funcView == SettingsBase.StationFunc.TableTracker)// (getKDS().isTrackerStation())
        {
            ttOnSharedPreferenceChanged(prefs, key);
            return;
        }
        if (key.equals("StationsRelation")) {
            SettingsBase.StationFunc oldFunc = getSettings().getStationFunc();
            getKDS().checkStationsSettingChanged(this.getApplicationContext());
            updateTitle();
            onStationFunctionChanged(getKDS().getStationFunction());
            SettingsBase.StationFunc newFunc = getSettings().getStationFunc();
            if (oldFunc != newFunc) {
                getKDS().onMyFunctionChanged(oldFunc, newFunc);
            }
        }
        else if (key.equals("kds_general_language") ||
                key.equals("agreement"))
        {
            return;
        }
        else if (key.equals(PreferenceFragmentAdvSum.ADVSUM_KEY_ITEMS))
        {
            onRefreshSummary(KDSUser.USER.USER_A);
            if (getKDS().isMultpleUsersMode())
                onRefreshSummary(KDSUser.USER.USER_B);
        }
        else if (key.equals("icon_folder_enabled"))
        {
            KDSGlobalVariables.getKDS().getSettings().resetBufferedIcons();
            refreshView();
        }
        else if (key.equals("log_mode"))
        {
            String s = prefs.getString(key, "0");
            int n = KDSUtil.convertStringToInt(s,0);
            KDSLog.setLogLevel(n);
            this.getSettings().set(KDSSettings.ID.Log_mode, s);

        }
        else if (key.equals("cleaning_alert_type") || key.equals("cleaning_reminder_interval") || key.equals("cleaning_enable_alerts"))
        {
            m_cleaning.resetAll();
        }
        else if (key.equals("hide_station_title")) //kpp1-377
        {
            boolean b = prefs.getBoolean(key, false);
            getSettings().set(KDSSettings.ID.Hide_station_title, b);
            SetTitleVisible(!b);

        }
        else if (key.equals("clear_db_schedule")) //kpp1-386
        { //
            return;
        }
        else {

            if (key.equals("isDirtyPrefs")) return;

            KDSSettings settings = this.getSettings();//getLayout().getEnv().getSettings();
            KDSSettings.SumPosition oldSumPosition = KDSSettings.SumPosition.values()[settings.getInt(KDSSettings.ID.Sum_position)];
            int nIndex = KDSSettings.getEnumIndexValues(settings,KDSSettings.SumOrderBy.class, KDSSettings.ID.Sum_order_by );

            KDSSettings.SumOrderBy oldSumOrderBy = KDSSettings.SumOrderBy.values()[nIndex];//[settings.getInt(KDSSettings.ID.Sum_order_by)];

            settings.loadSettings(this.getApplication());

            nIndex = KDSSettings.getEnumIndexValues(settings,KDSSettings.SumPosition.class, KDSSettings.ID.Sum_position );
            KDSSettings.SumPosition newSumPosition = KDSSettings.SumPosition.values()[nIndex];//settings.getInt(KDSSettings.ID.Sum_position)];

            nIndex = KDSSettings.getEnumIndexValues(settings,KDSSettings.SumOrderBy.class, KDSSettings.ID.Sum_order_by );
            KDSSettings.SumOrderBy newSumOrderBy = KDSSettings.SumOrderBy.values()[nIndex];//settings.getInt(KDSSettings.ID.Sum_order_by)];

            getKDS().updateSettings(settings);

            init_information_list_gui();

            nIndex = KDSSettings.getEnumIndexValues(settings,KDSSettings.KDSUserMode.class, KDSSettings.ID.Users_Mode );
            KDSSettings.KDSUserMode m = KDSSettings.KDSUserMode.values()[nIndex];//settings.getInt(KDSSettings.ID.Users_Mode)];
            //setupMultipleUserScreenOrientation();
            init_user_screen_gui_variables();

            updateUISettings();
            build_gui_according_to_user_mode(m);


            if (oldSumOrderBy != newSumOrderBy ||
                    key.equals("sum_type") || key.equals("sum_bgfg") ||
                    key.equals("sum_font"))//kpp1-320, kpp1-391
            {
                refreshSum();
            }

            if (oldSumPosition != newSumPosition) {
                boolean b = m_uiUserA.isVisibleSum(oldSumPosition);
                m_uiUserA.showSum(KDSUser.USER.USER_A, oldSumPosition, false);
                m_uiUserA.showSum(KDSUser.USER.USER_A, newSumPosition, b);

                b = m_uiUserB.isVisibleSum(oldSumPosition);
                m_uiUserB.showSum(KDSUser.USER.USER_B, oldSumPosition, false);
                m_uiUserB.showSum(KDSUser.USER.USER_B, newSumPosition, b);
            }

            if (key.equals("smartorder_enabled"))
            {
                onRefreshSummary(KDSUser.USER.USER_A);
                if (getKDS().isMultpleUsersMode())
                    onRefreshSummary(KDSUser.USER.USER_B);
            }



        }

        MainActivityFragment f = (MainActivityFragment) (getFragmentManager().findFragmentById(R.id.fragmentMain));
        if (f != null)
            f.updateSettings(getKDS().getSettings());
        init_common_in_create_and_pref_changed();

        if (key.equals("kds_general_id") )
        {
            m_activation.setStationID(getKDS().getStationID());
            if (!getKDS().getStationID().isEmpty()) { //kpp1-236. If station id is empty, backoffice will remove station.

                String name = getKDS().getSettings().getString(KDSSettings.ID.General_customized_title);
                m_activation.postNewStationInfoToWeb(getKDS().getStationID(), getKDS().getStationFunction().toString(), name);

                //station id changed. Change the relation table at here.
                changeRelationTableWithNewStationID(presentStationID, getKDS().getStationID());
            }
        }
        if (key.equals("kds_general_title"))
        { //kpp1-233
            String name = prefs.getString(key, "");
            m_activation.postNewStationName2Web(getKDS().getStationID(), name);
        }

    }

    private void build_gui_according_to_user_mode(KDSSettings.KDSUserMode userMode)
    {
        if (userMode == KDSSettings.KDSUserMode.Single) {
            buildUserScreenGuiForSingleMode();
        } else {
            buildUserScreenGuiForMultipleMode();
            focusUser(KDSUser.USER.USER_A);
            //this.getMainFragment().enableUserB(true);
        }
    }

    private void init_user_screen_gui_variables()
    {
        init_userA();
        init_userB();
    }

    private void refreshSum()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        onRefreshSummary(KDSUser.USER.USER_A);
        onRefreshSummary(KDSUser.USER.USER_B);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void buildUserScreenGuiForSingleMode()
    {
        setupUserScreenOrientation();

        getUserUI(KDSUser.USER.USER_B).show(false);
        m_uiUserA.checkIfFocusedOrderLostAfterItemsShowingMethodChanged();

        m_uiUserA.showScreenTitle(false);
        m_uiUserA.showVerticalTouchPad(false);
        boolean bTouchPadVisible = SettingsBase.loadTouchPadVisible(this);
        m_uiUserA.showTouchPad(bTouchPadVisible);
        m_uiUserA.showSummaryAlways(KDSUser.USER.USER_A);

        m_uiUserA.refresh();
    }

    private void buildUserScreenGuiForMultipleMode()
    {
        getUserUI(KDSUser.USER.USER_A).show(true);
        getUserUI(KDSUser.USER.USER_B).show(true);

        getUserUI(KDSUser.USER.USER_A).checkIfFocusedOrderLostAfterItemsShowingMethodChanged();
        getUserUI(KDSUser.USER.USER_B).checkIfFocusedOrderLostAfterItemsShowingMethodChanged();
        getUserUI(KDSUser.USER.USER_A).showScreenTitle(true);
        getUserUI(KDSUser.USER.USER_B).showScreenTitle(true);
        m_uiUserA.showSummaryAlways( KDSUser.USER.USER_A);
        m_uiUserB.showSummaryAlways( KDSUser.USER.USER_B);
        int n =  KDSSettings.getEnumIndexValues(getKDS().getSettings(), KDSSettings.ScreenOrientation.class, KDSSettings.ID.Screens_orientation);
        KDSSettings.ScreenOrientation orientation = KDSSettings.ScreenOrientation.values()[n];
        boolean bTouchPadVisible = SettingsBase.loadTouchPadVisible(this);

        switch (orientation)
        {

            case Left_Right: //show all buttons at bottom, see email:
                                //20170822:   Check touch position in attached picture, in left and right mode, the position should be in blue position.
                getUserUI(KDSUser.USER.USER_A).showVerticalTouchPad(false);
                getUserUI(KDSUser.USER.USER_B).showVerticalTouchPad(false);
                getUserUI(KDSUser.USER.USER_A).showTouchPad(bTouchPadVisible);
                getUserUI(KDSUser.USER.USER_B).showTouchPad(bTouchPadVisible);
                getUserUI(KDSUser.USER.USER_B).getLinear().setPadding(2, 0,0,0);

                break;
            case Up_Down:
                getUserUI(KDSUser.USER.USER_A).showVerticalTouchPad(false);
                getUserUI(KDSUser.USER.USER_B).showVerticalTouchPad(false);
                getUserUI(KDSUser.USER.USER_A).showTouchPad(bTouchPadVisible);
                getUserUI(KDSUser.USER.USER_B).showTouchPad(bTouchPadVisible);
                getUserUI(KDSUser.USER.USER_B).getLinear().setPadding(0, 0,0,0);
                break;
        }
        setupUserScreenOrientation();
        getUserUI(KDSUser.USER.USER_A).refresh();
        getUserUI(KDSUser.USER.USER_B).refresh();


    }

    public GUI_MODE getGuiMode()
    {
        SettingsBase.StationFunc funcView = getSettings().getFuncView();

        if (funcView == SettingsBase.StationFunc.Queue ||
                funcView == SettingsBase.StationFunc.Queue_Expo) //(getKDS().isQueueStation() || getKDS().isQueueExpo())
            return GUI_MODE.Queue;
        else if (funcView == SettingsBase.StationFunc.TableTracker) // (getKDS().isTrackerStation())
            return GUI_MODE.Tracker;
        else
            return GUI_MODE.KDS;
    }


    public  void ttOnSharedPreferenceChanged(SharedPreferences prefs, String key)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;

        if (key.equals("StationsRelation")) {
            //check stations changes
            KDSSettings.StationFunc funcOld = getKDS().getStationFunction();
            getKDS().checkStationsSettingChanged(this.getApplicationContext());
            updateTitle();
            KDSSettings.StationFunc funcNew = getKDS().getStationFunction();
            if (funcOld != funcNew &&
                    (funcNew == KDSSettings.StationFunc.TableTracker ||
                            funcOld ==KDSSettings.StationFunc.TableTracker)  ) {


                setupGuiByMode(getGuiMode());
                onStationFunctionChanged(funcNew);
                getKDS().onMyFunctionChanged(funcOld, funcNew);
            }

        }
        else if (key.equals(KDSSettings.TRACKER_AUTHEN_KEY))
        {
            m_ttView.loadSavedAuthen();
        }
        else {

            if (key.equals("isDirtyPrefs")) return;
            KDSSettings settings = this.getSettings();//getLayout().getEnv().getSettings();
            settings.loadSettings(this.getApplication());

            getKDS().updateSettings(settings);

            getKDS().getUsers().setSingleUserMode(true);
            m_queueView.updateSettings(settings);
            m_queueView.refresh();
            m_ttView.updateSettings(settings);
            m_ttView.refresh();


        }
        init_common_in_create_and_pref_changed();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public  void queueOnSharedPreferenceChanged(SharedPreferences prefs, String key)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter, key=" + key);
        if (!isKDSValid()) return ;

        if (key.equals("StationsRelation")) {
            //check stations changes
            KDSSettings.StationFunc funcOld = getKDS().getStationFunction();
            getKDS().checkStationsSettingChanged(this.getApplicationContext());
            updateTitle();
            KDSSettings.StationFunc funcNew = getKDS().getStationFunction();
            if (funcOld != funcNew &&
                    (funcNew == KDSSettings.StationFunc.Queue ||
                    funcOld ==KDSSettings.StationFunc.Queue)  ||
                    funcNew == KDSSettings.StationFunc.Queue_Expo ||
                    funcOld == KDSSettings.StationFunc.Queue_Expo) {


                setupGuiByMode(getGuiMode());

                onStationFunctionChanged(funcNew);
                getKDS().onMyFunctionChanged(funcOld, funcNew);

            }

        } else {

            if (key.equals("isDirtyPrefs")) return;
            KDSSettings settings = this.getSettings();//getLayout().getEnv().getSettings();
            settings.loadSettings(this.getApplication());

            getKDS().updateSettings(settings);

            getKDS().getUsers().setSingleUserMode(true);
            m_queueView.updateSettings(settings);
            m_queueView.refresh();
            m_ttView.updateSettings(settings);
            m_ttView.refresh();


        }
        init_common_in_create_and_pref_changed();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public boolean showInfo(String s) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter,s=" + s);
        m_handlerMessage.sendInformation(s);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
        return true;


    }

    public void onStationConnected(String ip, KDSStationConnection conn) {
        if (!isKDSValid()) return ;
        KDSStationActived activeStation = getKDS().getStationsConnections().findActivedStationByIP(ip);
        if (activeStation == null)
            showInfo("Connect to [" + ip + "] successfully.");
        else
            showInfo("Connect to #" + activeStation.getID() + " successfully");
    }

    public void onStationDisconnected(String ip) {
        if (!isKDSValid()) return ;
        KDSStationActived activeStation = getKDS().getStationsConnections().findActivedStationByIP(ip);
        if (activeStation == null)
            showInfo("Connection of [" + ip + "] was closed");
        else
            showInfo("Connection of #" + activeStation.getID() + " was closed");
    }

    /**
     * use xml command delete one order.
     *
     * @param ordersGuid
     * csv guids: first is user-a, second is userb
     */
    public void onXmlCommandBumpOrder(String ordersGuid) {
        if (!isKDSValid()) return ;
        ArrayList<String> ar = KDSUtil.spliteString(ordersGuid, ",");
        if (ar.size()<=0) return;
        String orderGuidUserA = ar.get(0);
        String orderGuidUserB ="";
        if (ar.size() >1)
            orderGuidUserB = ar.get(1);
        String focusedUserA =  this.getFocusedOrderGUID(KDSUser.USER.USER_A);
        if (focusedUserA.equals(orderGuidUserA))
            opFocusNext(KDSUser.USER.USER_A);
        if (getKDS().isMultpleUsersMode())
        {
            String focusedUserB = this.getFocusedOrderGUID(KDSUser.USER.USER_B);
            if (focusedUserB.equals(orderGuidUserB))
                opFocusNext(KDSUser.USER.USER_B);
        }
    }

    public void onTTBumpOrder(String orderName) {

        KDSDataOrder order =  getKDS().getUsers().getUser(KDSUser.USER.USER_A).getOrders().getOrderByName(orderName);
        if (order != null) {
            this.bumpOrderOperation(KDSUser.USER.USER_A, order.getGUID(), true);
        }
        if (getKDS().isMultpleUsersMode()) {
            order = getKDS().getUsers().getUser(KDSUser.USER.USER_B).getOrders().getOrderByName(orderName);
            if (order != null) {
                this.bumpOrderOperation(KDSUser.USER.USER_B, order.getGUID(), true);
            }
        }
    }

    public void onReceiveNewRelations() {

    }

    public void onReceiveRelationsDifferent() {
        if (!isKDSValid()) return ;
        if (getKDS().getStationID().isEmpty()) return;
        KDSUIDialogBase dlg = new KDSUIDialogBase();
        dlg.createInformationDialog(this, this.getString(R.string.error), this.getString(R.string.error_different_relations), false);
        dlg.show();
        dlg.setAutoCloseTimeout(KDSConst.DIALOG_AUTO_CLOSE_TIMEOUT);

    }

    public void onAskOrderState(Object objSource, String orderName) {

        KDSUIDialogOrderStatus dlg = new KDSUIDialogOrderStatus(this, this, orderName);
        dlg.setData(objSource); //which data source ask order status.
        dlg.show();

    }


    /**
     * @param orderGuid
     */
    public void onSetFocusToOrder(String orderGuid) {
        if (!isKDSValid()) return ;
        if (!isUserLayoutReady(KDSUser.USER.USER_A)) return;
        if (getKDS().isMultpleUsersMode())
        {
            if (!isUserLayoutReady(KDSUser.USER.USER_B)) return;
        }
        SettingsBase.StationFunc funcView = getSettings().getFuncView();
        //if (getKDS().isQueueStation() || getKDS().isQueueExpo())
        if (funcView == SettingsBase.StationFunc.Queue || funcView == SettingsBase.StationFunc.Queue_Expo)
        {
            queueSetFocusToOrder(orderGuid);
            return;
        }
        else if (funcView == SettingsBase.StationFunc.TableTracker)// (getKDS().isTrackerStation())
        {
            ttSetFocusToOrder(orderGuid);
            return;
        }
        if (!orderGuid.isEmpty()) {
            getUserUI(KDSUser.USER.USER_A).getLayout().focusOrder(orderGuid);
            if (getKDS().isMultpleUsersMode())
                getUserUI(KDSUser.USER.USER_B).getLayout().focusOrder(orderGuid);
        }
        else
        {//set to default order
            String focusedOrderGuid = getSelectedOrderGuid(KDSUser.USER.USER_A);
            if (focusedOrderGuid.isEmpty())
                getUserUI(KDSUser.USER.USER_A).getLayout().focusOrder( getFirstOrderGuidToFocus(KDSUser.USER.USER_A));//getKDS().getUsers().getUserA().getOrders().getFirstOrderGuid());
            else
            {
                if (getKDS().getUsers().getUserA().getOrders().getOrderByGUID(focusedOrderGuid) == null)
                    getUserUI(KDSUser.USER.USER_A).getLayout().focusOrder( getFirstOrderGuidToFocus(KDSUser.USER.USER_A));//getKDS().getUsers().getUserA().getOrders().getFirstOrderGuid());
            }
            if (getKDS().isMultpleUsersMode())
            {
                focusedOrderGuid = getSelectedOrderGuid(KDSUser.USER.USER_B);
                if (focusedOrderGuid.isEmpty())
                    getUserUI(KDSUser.USER.USER_B).getLayout().focusOrder( getFirstOrderGuidToFocus(KDSUser.USER.USER_B));//getKDS().getUsers().getUserB().getOrders().getFirstOrderGuid());
                else
                {
                    if (getKDS().getUsers().getUserB().getOrders().getOrderByGUID(focusedOrderGuid) == null)
                        getUserUI(KDSUser.USER.USER_B).getLayout().focusOrder(getFirstOrderGuidToFocus(KDSUser.USER.USER_B));//getKDS().getUsers().getUserB().getOrders().getFirstOrderGuid());
                }
            }
        }
    }

    public void queueSetFocusToOrder(String orderGuid)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!orderGuid.isEmpty()) {
            m_queueView.focusOrder(orderGuid);

        }
        else
        {//set to default order
            String focusedOrderGuid = m_queueView.getFocusedGuid();
            if (focusedOrderGuid.isEmpty())
                m_queueView.focusFirst();

            else
            {

                if (getKDS().getUsers().getUserA().getOrders().getOrderByGUID(focusedOrderGuid) == null)
                    m_queueView.focusFirst();
            }

        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void ttSetFocusToOrder(String orderGuid)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!orderGuid.isEmpty()) {
            m_ttView.focusOrder(orderGuid);

        }
        else
        {//set to default order
            String focusedOrderGuid = m_ttView.getFocusedGuid();
            if (focusedOrderGuid.isEmpty())
                m_ttView.focusFirst();

            else
            {

                if (getKDS().getUsers().getUserA().getOrders().getOrderByGUID(focusedOrderGuid) == null)
                    m_ttView.focusFirst();
            }

        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void onAcceptIP(String ip) {
        if (!isKDSValid()) return ;
        KDSStationActived activeStation = getKDS().getStationsConnections().findActivedStationByIP(ip);
        if (activeStation != null)
            showInfo("Accept #" + activeStation.getID() + " connection");
        else
            showInfo("Accept " + "[" + ip + "] connection");
    }

    public void onShowMessage(KDS.MessageType msgType,String message) {
        //showToastMessage(message);
        switch (msgType)
        {
            case Normal:
                showInfo(message);
                break;
            case Toast:
                showToastMessage(message);
                break;
        }

    }

    Toast m_toast = null;
    public void showToastMessage(String message) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter,s="+message);

        int duration = Toast.LENGTH_SHORT;
        showToastMessage(message, duration);

//        if (m_toast == null)
//            m_toast = Toast.makeText(this, message, duration);
//        else
//            m_toast.setText(message);
//        m_toast.setDuration(duration);
//
//        m_toast.show();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void onRefreshView(int  nUserID, KDSDataOrders orders, KDS.RefreshViewParam nParam) {

        KDSUser.USER userID = KDSUser.USER.values()[nUserID];
        SettingsBase.StationFunc funcView = getSettings().getFuncView();
        //if (getKDS().isQueueStation() || getKDS().isQueueExpo())
        if (funcView == SettingsBase.StationFunc.Queue ||
                funcView == SettingsBase.StationFunc.Queue_Expo)
        {
              refreshQueueView();
        }
        else if (funcView == SettingsBase.StationFunc.TableTracker)//(getKDS().isTrackerStation())
        {
            refreshTTView();
        }
        else {
            //if (this.getUserUI(userID).getLayout() != null) {
            if (isUserLayoutReady(userID)){
                // TimeDog t = new TimeDog();
                this.getUserUI(userID).getLayout().showOrders(orders);
                // t.debug_print_Duration("onRefreshView1");
                if (nParam == KDS.RefreshViewParam.Focus_First || getFocusedOrderGUID(userID).isEmpty()) {
                    String orderGuid = getFirstOrderGuidToFocus(userID);// orders.getFirstOrderGuid();
                    this.getUserUI(userID).getLayout().focusOrder(orderGuid);
                }
                refreshPrevNext(userID); //it also shows the park count, so use this function
                //this.getUserUI(userID).refreshPrevNext();
                // t.debug_print_Duration("onRefreshView2");
                //kpp1-393
                refreshParkedCount(userID);
            }
        }
    }

    public void onRetrieveNewConfigFromOtherStation() {
        refreshView();
    }

    public void onRefreshSummary(KDSUser.USER userID) {
        if (!isKDSValid()) return ;
        if (!isSummaryVisible(userID)) return; //kpp1-382, just refresh sum when summary panel visiable.
        int n = getKDS().getSettings().getInt(KDSSettings.ID.Sum_position);
        KDSSettings.SumPosition pos = KDSSettings.SumPosition.values()[n];
        this.getUserUI(userID).refreshSum(userID, pos);
        //this.getSummaryFragment().refreshSummary();
    }

    @Override
    public void onResume() {

        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Enter");
        m_bPaused = false;
        super.onResume();
        if (!isKDSValid()) return ;
        if (!getKDS().isDataLoaded())
            refreshWithNewDbData();


        getKDS().checkPingThreadAfterResume();
        //showInfo("onResume");
        hideNavigationBar();

        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Exit");
    }

    private void hideNavigationBar() {
        boolean bHide = getKDS().getSettings().getBoolean(KDSSettings.ID.Hide_navigation_bar);

        if (bHide) {
           // hideNavigationBar();
            View view = this.getWindow().getDecorView();
            KDSUtil.enableSystemVirtualBar(view, false);
            view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    KDSUtil.enableSystemVirtualBar(MainActivity.this.getWindow().getDecorView(), false);
                }
            });
        } else {
            View view = this.getWindow().getDecorView();
            KDSUtil.enableSystemVirtualBar(view, true);
            view.setOnSystemUiVisibilityChangeListener(null);
        }
    }

    AsyncTask m_taskRefresh = null;

    public void refreshWithNewDbData() {
        if (!isKDSValid()) return ;
        //      refreshWithNewDbData2();

        if (m_taskRefresh != null) return;
        m_taskRefresh = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                getKDS().loadAllActiveOrders();
                //   refreshView();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                refreshView();
                m_taskRefresh = null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


    }

    public void refreshWithNewDbDataAndFocusFirst() {

        if (m_taskRefresh != null) return;
        if (!isKDSValid()) return ;
        m_taskRefresh = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                getKDS().loadAllActiveOrders();
                //   refreshView();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                m_refreshHandler.sendFocusFirstMessage();
                //String firstOrderGuid  = getFirstOrderGuidToFocus(getFocusedUserID());
                //onSetFocusToOrder(firstOrderGuid);
                //refreshView();
                m_taskRefresh = null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


    }




    public void refreshQueueView()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (m_queueView == null) return;
        KDSDataOrders ordersA = this.getKDS().getUsers().getUserA().getOrders();

        KDSDataOrders ordersB = null;
        if (getKDS().getUsers().getUserB() != null)
            ordersB = this.getKDS().getUsers().getUserB().getOrders();
        m_queueView.showOrders(getKDS().getUsers());//ordersA, ordersB);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void refreshTTView()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (m_ttView == null) return;
        m_ttView.showOrders(this.getKDS().getUsers().getUserA().getOrders());
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void refreshView() {
        if (!isKDSValid()) return ;


        getKDS().refreshView();

//        if (getKDS().getUsers().getUsersCount() == 0)
//            return;
//        SettingsBase.StationFunc funcView = getSettings().getFuncView(); //current use what view to show orders.
//        //if (getKDS().isQueueStation() || getKDS().isQueueExpo())
//        if (funcView == SettingsBase.StationFunc.Queue ||
//                funcView == SettingsBase.StationFunc.Queue_Expo)
//            refreshQueueView();
//        else if (funcView == SettingsBase.StationFunc.TableTracker)// (getKDS().isTrackerStation())
//            refreshTTView();
//        else {
//            refreshView(KDSUser.USER.USER_A);
//            if (getKDS().isValidUser(KDSUser.USER.USER_B))
//                refreshView(KDSUser.USER.USER_B);
//        }
    }

    public void refreshView(KDSUser.USER userID) {
        if (!isKDSValid()) return ;
        getKDS().refreshView(userID, KDS.RefreshViewParam.None);

//        if (getSelectedOrderGuid(userID).isEmpty()) {
//            String nextGuid =getFirstOrderGuidToFocus(userID);// getKDS().getUsers().getUser(userID).getOrders().getFirstOrderGuid();
//            setSelectedOrderGuid(userID, nextGuid);
//        }
//        getKDS().refreshView(userID, KDS.RefreshViewParam.None);
//        refreshPrevNext(userID);
    }

    public void refreshPrevNext(KDSUser.USER userID) {
        //KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");

        this.getUserUI(userID).refreshPrevNext();
        refreshParkedCount(userID);
        //KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

    }

    public void refreshParkedCount(KDSUser.USER userID) {

        //KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        try {
            if (!isKDSValid()) return;
            if (getKDS().getUsers() == null) return;
            if (getKDS().getUsers().getUser(userID) == null) return;

            int ncount = getKDS().getUsers().getUser(userID).getParkedCount();
            this.getUserUI(userID).refreshParkOrdersCount(ncount);
        }catch ( Exception e)
        {
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + e.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e );
        }
        //KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void onPause() {
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Enter");
        m_bPaused = true;
        super.onPause();
        //showInfo("Paused");
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Exit");
    }

    protected void onDestroy() {
        super.onDestroy();
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Enter");
        if (!isKDSValid()) return ;
        //in android, if unplug/plug usb port device, this function will been fired.
        m_timer.stop();
        this.getKDS().stop();
        m_cleaning.resetAll(); //kpp1-344
        //stopService();
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"Exit");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //Log.i("key pressed", String.valueOf(event.getKeyCode()));
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"dispatchKeyEvent event=" + event);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // Log.d(TAG, "dispatchKeyEvent down=" + event.getKeyCode() );
            if (event.getRepeatCount() == 0)
                m_kbdRecorder.onKeyDown(event.getKeyCode());
            return super.dispatchKeyEvent(event);
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            boolean b = super.dispatchKeyEvent(event);
            //Log.d(TAG, "dispatchKeyEvent up=" + event.getKeyCode() );
            m_kbdRecorder.onKeyUp(event.getKeyCode());
            return b;
        } else
            return super.dispatchKeyEvent(event);

    }

    KDSKbdDoublePress m_doublePressChecker = new KDSKbdDoublePress();

    /**
     * @param keyCode
     * @param event
     * @return True: don't pass it to others.
     * False: pass it to next
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);

    }

    private boolean keyPressed(int keyCode, KeyEvent event) {
        if (!isKDSValid()) return false;
        // if (!m_kbdRecorder.isReadyForEvent()) {
        if (!m_kbdRecorder.isAnyKeyDown()) {
//            if (m_kbdRecorder.isKeyupTimeout())
//                m_kbdRecorder.clear();
//            else
            return false;
        }
        //showInfo(KDSUtil.convertIntToString(keyCode));
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"KeyPressed=" + KDSUtil.convertIntToString(keyCode));
        m_kbdRecorder.debug("keyPressed");
        boolean b = false;
//        if (getKDS().isQueueStation())
//        {
//
//        }
//        else {
        KDSSettings.ID eventID =  KDSSettings.ID.NULL;
        if (getKDS().isQueueExpo())
            eventID = getKDS().checkQExpoKbdEvent(event, m_kbdRecorder);
        else
            eventID = getKDS().checkKDSKbdEvent(event, m_kbdRecorder);

        if (eventID != KDSSettings.ID.NULL) {
            KDSLog.d(TAG, KDSLog._FUNCLINE_() + "FireEvent=" + eventID);
            m_kbdRecorder.reset();
            doKbdEvent(eventID);
            b = true;
            m_kbdRecorder.setWaitAllKeysUp(true);
        }
       // }
        if (m_doublePressChecker.checkDoublePressed(keyCode)) {
            onDoublePressKeyCode(event);
        }

        //if (eventID == KDSSettings.ID.NULL )
        //{
            if (getKDS().isQueueExpo())
                m_queueView.onKeyPressed( keyCode,  event, eventID);
        //}

        return b;
    }

    /**
     * 2.0.15,
     * make sure focus visible.
     * @return
     */
    public boolean checkFocusVisibleInOrdersModeView()
    {
        if (getGuiMode() != GUI_MODE.KDS)
            return true;
        if (!getUserUI(getFocusedUserID()).getLayout().isFocusOrderVisible()) {

            threadrefresh_FocusFirst();
            return false;
        }
        //
        return true;

    }
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (!checkFocusVisibleInOrdersModeView()) return true;//2.0.15
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            ImageView v =  (ImageView) this.findViewById(R.id.imgMenu);
            showPopupMenu(v);
            return true;
        }



        boolean b = keyPressed(keyCode, event);
        //m_kbdRecorder.onKeyUp(keyCode);
        KDSLog.d(TAG,KDSLog._FUNCLINE_()+ "KeyUp=" + keyCode);
        return b;
        //return false;
    }

    public void onDoublePressKeyCode(KeyEvent ev) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        SettingsBase.StationFunc funcView = getSettings().getFuncView();
        //if (getKDS().isQueueStation() || getKDS().isTrackerStation() || getKDS().isQueueExpo())
        if (funcView == SettingsBase.StationFunc.Queue ||
                funcView == SettingsBase.StationFunc.Queue_Expo ||
                funcView == SettingsBase.StationFunc.TableTracker)
        {

        }
        else {
            if (getSettings().getBoolean(KDSSettings.ID.Transfer_by_double_click))
            {
                doDoublePressPanelNumberTransfer(KDSUser.USER.USER_A, ev);
            }
            else {
                int nBumpByKdbMode = this.getSettings().getInt(KDSSettings.ID.Bumping_PanelNum_Mode);
                if (nBumpByKdbMode != KDSSettings.BumpingByPanelNum.Double_Click.ordinal())
                    return;
                doDoublePressPanelNumberBump(KDSUser.USER.USER_A, ev);
            }
        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void doDoublePressPanelNumberBump(KDSUser.USER user, KeyEvent ev) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        int nKeyCode = ev.getKeyCode();
        if (nKeyCode == KeyEvent.KEYCODE_0 ||
                nKeyCode == KeyEvent.KEYCODE_1 ||
                nKeyCode == KeyEvent.KEYCODE_2 ||
                nKeyCode == KeyEvent.KEYCODE_3 ||
                nKeyCode == KeyEvent.KEYCODE_4 ||
                nKeyCode == KeyEvent.KEYCODE_5 ||
                nKeyCode == KeyEvent.KEYCODE_6 ||
                nKeyCode == KeyEvent.KEYCODE_7 ||
                nKeyCode == KeyEvent.KEYCODE_8 ||
                nKeyCode == KeyEvent.KEYCODE_9) {
            if (!isUserLayoutReady(user)) return;
            int nPanel = nKeyCode - KeyEvent.KEYCODE_0;
            String orderGuid = getUserUI(user).getLayout().getPanelOrderGuid(nPanel);
            if (orderGuid.isEmpty()) return;
            if (getSelectedOrderGuid(user).equals(orderGuid)) {
                onBumpOrder(user);
            } else {
                KDSDataOrder bumpedOrder = bumpOrder(user, orderGuid, true);
                if (bumpedOrder == null) return;
                this.getSummaryFragment().refreshSummary();
                refreshView();
                printOrder(bumpedOrder);
            }

        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void doKbdEvent(KDSSettings.ID evID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "EventID=" +evID.toString());
        SettingsBase.StationFunc funcView = getSettings().getFuncView(); //current use what view to show orders.

        switch (evID) {
            //bump bar
            case Bumpbar_OK:
                break;
            case Bumpbar_Cancel:
                break;
            case Bumpbar_Next:
                if (funcView == SettingsBase.StationFunc.Queue)//(getKDS().isQueueStation())
                    queueFocusNextOrder();
                else if ( funcView == SettingsBase.StationFunc.TableTracker)// (getKDS().isTrackerStation())
                    ttFocusNextOrder();
                else if (funcView == SettingsBase.StationFunc.Queue_Expo)//(getKDS().isQueueExpo())
                {

                }
                else
                    opFocusNext(getFocusedUserID());
                //onBtnNextClicked(null);
                break;
            case Bumpbar_Prev:
                if (funcView == SettingsBase.StationFunc.Queue)//(getKDS().isQueueStation())
                    queueFocusPrevOrder();
                else if ( funcView == SettingsBase.StationFunc.TableTracker)//(getKDS().isTrackerStation())
                    ttFocusPrevOrder();
                else if (funcView == SettingsBase.StationFunc.Queue_Expo)//(getKDS().isQueueExpo())
                {

                }
                else
                    opFocusPrev(getFocusedUserID());
                //onBtnPrevClicked(null);
                break;
            case Bumpbar_Up:
//                if (getKDS().isQueueStation())
//                {
//                }
//                else if (getKDS().isTrackerStation())
//                {
//
//                }
//                else if (getKDS().isQueueExpo())
//                {
//
//                }
                if (isFixedSingleScreenView())
                {

                }
                else
                    opUp(getFocusedUserID());
                //onBtnUpClicked(null);
                break;
            case Bumpbar_Down:
//                if (getKDS().isQueueStation())
//                {
//                }
//                else if (getKDS().isTrackerStation())
//                {
//
//                }
//                else if (getKDS().isQueueExpo())
//                {
//
//                }
                if (isFixedSingleScreenView())
                {

                }
                else
                    opDown(getFocusedUserID());
                //onBtnDownClicked(null);
                break;
            case Bumpbar_Bump:
                if (funcView == SettingsBase.StationFunc.Queue)//( (getKDS().isQueueStation())
                {
                    queueBumpOrder();
                }
                else if  (funcView == SettingsBase.StationFunc.TableTracker)//(getKDS().isTrackerStation())
                {
                    bumpTrackerOrder(KDSUser.USER.USER_A, getFocusedOrderGUID(KDSUser.USER.USER_A));
                }
                else if (funcView == SettingsBase.StationFunc.Queue_Expo)//(getKDS().isQueueExpo())
                {
                    qexpoBumpOrder();
                }
                else
                    opBump(getFocusedUserID());
                //onBtnBumpClicked(null);
                break;
            case Bumpbar_Unbump:
                if (funcView == SettingsBase.StationFunc.Queue)//(getKDS().isQueueStation())
                {
                    opUnbump(KDSUser.USER.USER_A);
                }
                else if (funcView == SettingsBase.StationFunc.TableTracker)//(getKDS().isTrackerStation())
                {
                    opUnbump(KDSUser.USER.USER_A);
                }
                else if (funcView == SettingsBase.StationFunc.Queue_Expo)//(getKDS().isQueueExpo())
                {
                    opUnbumpLast(KDSUser.USER.USER_A);
                    m_queueView.focusOrder("");
                }
                else
                    opUnbump(getFocusedUserID());
                // onBtnUnbumpClicked(null);
                break;
            case Bumpbar_Unbump_Last:
                opUnbumpLast(getFocusedUserID());

                break;
            case Bumpbar_Sum:
//                if (getKDS().isQueueStation())
//                {
//                }
//                else if (getKDS().isTrackerStation())
//                {
//                }
//                else if (getKDS().isQueueExpo())
//                {
//
//                }
                if (isFixedSingleScreenView())
                {

                }
                else
                    opSummary(getFocusedUserID());
                //onBtnSumClicked(null);
                break;
            case Bumpbar_Transfer:
//                if (getKDS().isQueueStation())
//                {
//                }
//                else if (getKDS().isTrackerStation())
//                {
//                }
//                else if (getKDS().isQueueExpo())
//                {
//
//                }
                if (isFixedSingleScreenView())
                {

                }
                else
                    opTransfer(getFocusedUserID());

                break;
            case Bumpbar_Sort:
//                if (getKDS().isQueueStation())
//                {
//                }
//                else if (getKDS().isTrackerStation())
//                {
//                }
//                else if (getKDS().isQueueExpo())
//                {
//
//                }
                if (isFixedSingleScreenView())
                {

                }
                else
                    opSort(getFocusedUserID());

                break;
            case Bumpbar_Park:
//                if (getKDS().isQueueStation())
//                {
//                }
//                else if (getKDS().isTrackerStation())
//                {
//                }
//                else if (getKDS().isQueueExpo())
//                {
//
//                }
                if (isFixedSingleScreenView())
                {

                }
                else
                    opPark(getFocusedUserID());
                break;
            case Bumpbar_Unpark:
//                if (getKDS().isQueueStation())
//                {
//                }
//                else if (getKDS().isTrackerStation())
//                {
//                }
//                else if (getKDS().isQueueExpo())
//                {
//
//                }
                if (isFixedSingleScreenView())
                {

                }
                else
                    opUnpark(getFocusedUserID());
                break;
            case Bumpbar_Print:
//                if (getKDS().isQueueStation())
//                {
//                }
//                else if (getKDS().isTrackerStation())
//                {
//                }
//                else if (getKDS().isQueueExpo())
//                {
//
//                }
                if (isFixedSingleScreenView())
                {

                }
                else
                    opPrint(getFocusedUserID());
                break;
            case Bumpbar_More:
//                if (getKDS().isQueueStation())
//                {
//                }
//                else if (getKDS().isTrackerStation())
//                {
//                }
//                else if (getKDS().isQueueExpo())
//                {
//
//                }
                if (isFixedSingleScreenView())
                {

                }
                else
                    opMore(getFocusedUserID());
                //onBtnMoreClicked(null);
                break;
            case Bumpbar_BuildCard:
//                if (getKDS().isQueueStation())
//                {
//                }
//                else if (getKDS().isTrackerStation())
//                {
//                }
//                else if (getKDS().isQueueExpo())
//                {
//
//                }
                if (isFixedSingleScreenView())
                {

                }
                else
                    doMoreFunc_BuildCard(getFocusedUserID());
                break;
            case Bumpbar_Training:
//                if (getKDS().isQueueStation())
//                {
//                }
//                else if (getKDS().isTrackerStation())
//                {
//                }
//                else if (getKDS().isQueueExpo())
//                {
//
//                }
                if (isFixedSingleScreenView())
                {

                }
                else
                    doMoreFunc_Training_Video(getFocusedUserID());
                break;
            case Bumpbar_Page:
                if (getKDS().isExpeditorStation())
                {
                    opPageOrder(getFocusedUserID());
                }
                break;
            case Bumpbar_Menu:
                opToggleMenu();
                break;
            case Bumpbar_Switch_User:
//                if (getKDS().isQueueStation())
//                {
//                }
//                else if (getKDS().isTrackerStation())
//                {
//                }
//                else if (getKDS().isQueueExpo())
//                {
//
//                }
                if (isFixedSingleScreenView())
                {

                }
                else
                    opSwitchUser();
                break;
            case Bumpbar_QExpo_Pickup:
                qexpoPickup();
                break;
            case Bumpbar_QExpo_Unpickup:
                qexpoUnpickup();
                break;
            case Bumpbar_Focus_0:
            case Bumpbar_Focus_1:
            case Bumpbar_Focus_2:
            case Bumpbar_Focus_3:
            case Bumpbar_Focus_4:
            case Bumpbar_Focus_5:
            case Bumpbar_Focus_6:
            case Bumpbar_Focus_7:
            case Bumpbar_Focus_8:
            case Bumpbar_Focus_9:
//                if (getKDS().isQueueStation())
//                {
//                }
//                else if (getKDS().isTrackerStation())
//                {
//                }
//                else if (getKDS().isQueueExpo())
//                {
//
//                }
                if (isFixedSingleScreenView())
                {

                }
                else
                    opFocusPanel(getFocusedUserID(), evID.ordinal() - KDSSettings.ID.Bumpbar_Focus_0.ordinal());
                break;
            case Bumpbar_tab_next:
                opTabNextDisplayMode();
                break;
            case Bumpbar_Clean: //kpp1-339
                opCleanByBumpbar();
                break;
            default:
                break;


        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void opToggleMenu() {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        ImageView v =  (ImageView) this.findViewById(R.id.imgMenu);
        showPopupMenu(v);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }


    //touch pad interface
    public void onFragmentInteraction(KDSUser.USER userID, KDSTouchPadButton.TouchPadID id) {
        switch (id) {

            case NULL:
                break;
            case Next:
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->next");
                opFocusNext(userID);

                break;
            case Prev:
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->prev");
                opFocusPrev(userID);
                break;
            case Up:
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->up");
                opUp(userID);
                break;
            case Down:
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->down");
                opDown(userID);
                break;
            case Bump:
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->bump");
                opBump(userID);

                break;
            case Unbump:
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->unbump");
                opUnbump(userID);

                break;
            case Sum:
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->sum");
                opSummary(userID);

                break;
            case Transfer:
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->transfer");
                opTransfer(userID);
                break;
            case Sort:
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->sort");
                opSort(userID);
                break;
            case Test:
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->test");
                opTest(userID);
                break;
            case ActiveStations:
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->activestations");
                opShowActiveStations(userID);
                break;
            case Park: {
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->park");
                opPark(userID);
            }
            break;
            case Unpark: {
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->unpark");
                opUnpark(userID);
            }
            break;
            case More:
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->more");
                opMore(userID);

                break;
            case Print: {
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->print");
                opPrint(userID);
            }
            break;
            case BuildCard:
            {
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->buildcard");
                doMoreFunc_BuildCard(userID);
            }
            break;
            case Training:
            {
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->training_video");
                doMoreFunc_Training_Video(userID);
            }
            break;
            case UnbumpLast:
            {
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->unbumplast");
                opUnbumpLast(userID);
            }
            break;
            case Page:
            {
                KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Touch-->guest_paging");
                opPageOrder(userID);
            }
            break;
            //2.0.25
            case Next_Page:
            {
                opNextPage(userID);
            }
            break;
            case Prev_Page:
            {
                opPrevPage(userID);
            }
            break;
        }
    }

    /**
     * @param nPanel The panel index
     */
    public void opFocusPanel(KDSUser.USER userID, int nPanel) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isUserLayoutReady(userID)) return;
        getUserUI(userID).getLayout().focusPanel(nPanel);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public KDSUser.USER getUserFromLayout(KDSLayout layout) {
        KDSUser.USER user = KDSUser.USER.USER_A;

        if (getKDS().isMultpleUsersMode()) {
            if (getUserUI(KDSUser.USER.USER_A).getLayout() == layout) {
                user = KDSUser.USER.USER_A;
            } else
                user = KDSUser.USER.USER_B;
        }
        return user;
    }

    public void onViewPanelClicked(KDSLayout layout) {
        KDSUser.USER user = getUserFromLayout(layout);
        focusUser(user);
    }

    //boolean m_bWatingRefreshViewAsDblClick = false;
    TimeDog m_doubleClickIntervalTimeout = new TimeDog(); //prevent the double click too quick.
    final int DOUBLE_CLICK_BUMP_TIMEOUT = 1000;
    public void onViewPanelDoubleClicked(KDSLayout layout) {
        if (!m_doubleClickIntervalTimeout.is_timeout(DOUBLE_CLICK_BUMP_TIMEOUT))
            return;
        m_doubleClickIntervalTimeout.reset();
        if (!isKDSValid()) return ;
        //if (m_bWatingRefreshViewAsDblClick ) return;

        KDSUser.USER user = getUserFromLayout(layout);// KDSUser.USER.USER_A;



        if (getSettings().getBoolean(KDSSettings.ID.Transfer_by_double_click))
            opTransfer(user);//do transfer
        else
            opBump(user);
        if (layout.getView() != null)
            layout.getView().setNeedDrawOnce();
        //m_bWatingRefreshViewAsDblClick = true;

    }

    public boolean onViewSlipLeftRight(KDSLayout layout,MotionEvent e1, MotionEvent e2,  KDSView.SlipDirection slipDirection, KDSView.SlipInBorder slipInBorder)
    {
        KDSUser.USER user = getUserFromLayout(layout);
        if (slipInBorder == KDSView.SlipInBorder.Left ||
                slipInBorder == KDSView.SlipInBorder.Right) {
            int n = getKDS().getSettings().getInt(KDSSettings.ID.Sum_position);
            KDSSettings.SumPosition pos = KDSSettings.SumPosition.values()[n];
            if  (pos== KDSSettings.SumPosition.Side ) {

                if (getKDS().isSingleUserMode()) {
                        if (slipInBorder == KDSView.SlipInBorder.Right) {
                            showSummary(user,(slipDirection == KDSView.SlipDirection.Right2Left) );
                            //this.getUserUI(user).showSum(user, pos, (slipDirection == KDSView.SlipDirection.Right2Left) );
                            return true;
                        }

                } else {
                    if (user == KDSUser.USER.USER_A)
                    {
                        if (slipInBorder == KDSView.SlipInBorder.Left) {
                            showSummary(user,(slipDirection == KDSView.SlipDirection.Left2Right) );
                            //this.getUserUI(user).showSum(user, pos, (slipDirection == KDSView.SlipDirection.Left2Right));
                            return true;
                        }
                    }
                    else if (user == KDSUser.USER.USER_B)
                    {
                        if (slipInBorder == KDSView.SlipInBorder.Right) {
                            showSummary(user,(slipDirection == KDSView.SlipDirection.Right2Left) );
                            //this.getUserUI(user).showSum(user, pos, (slipDirection == KDSView.SlipDirection.Right2Left));
                            return true;
                        }
                    }
                }
            }
        }
        if (slipDirection == KDSView.SlipDirection.Right2Left)
        {
            return opNextPage(user);
        }
        else
        {
            return opPrevPage(user);
        }
    }
//    public void onRedrawLayout(KDSLayout layout)
//    {
//        getKDS().refreshView();
//    }
    /**
     *
     * @param layout
     */
    public void onViewDrawingFinished(KDSLayout layout)
    {
        //m_bWatingRefreshViewAsDblClick = false;
    }

    PowerManager.WakeLock m_wakeLock = null;

    public void lockAndroidWakeMode(boolean bLock) {
        if (m_wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            m_wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "kds:MyWakeLock");
        }
        if (bLock) {
            m_wakeLock.acquire();
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {

            m_wakeLock.release();
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }


    public void doMoreFunction(KDSUser.USER userID, KDSUIDialogMore.FunctionMore func) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        switch (func) {
            case Clear_Messages: {
                doMoreFunc_ClearMessages(userID);

            }
            break;
            case Cook_Started:
            {
                doMoreFunc_CookStarted(userID);
            }
            break;
        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public KDSDataItem getSelectItem(KDSUser.USER userID)
    {
        if (!isKDSValid()) return new KDSDataItem();
        String itemGuid = getFocusedItemGUID(userID);
        if (itemGuid.isEmpty()) {
            showToastMessage(this.getString(R.string.must_select_item));
            return null;
        }
        String orderGuid = getFocusedOrderGUID(userID);
        if (orderGuid.isEmpty()) return null;
        KDSDataOrder order = getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        if (order == null) return null;
        KDSDataItem item = order.getItems().getItemByGUID(itemGuid);
        if (item == null) {
            showToastMessage(this.getString(R.string.must_select_item));
            return null;
        }
        return item;
    }
    /**
     * http://d.hiphotos.baidu.com/image/h%3D200/sign=72b32dc4b719ebc4df787199b227cf79/58ee3d6d55fbb2fb48944ab34b4a20a44723dcd7.jpg
     * @param userID
     */
    public void  doMoreFunc_BuildCard(KDSUser.USER userID)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        KDSDataItem item = getSelectItem(userID);
        if (item == null) return;
        CSVStrings files = item.getBuildCard();

        if (files.getCount() <=0) {
            showToastMessage(this.getString(R.string.no_build_card));// "No build card files for selected item");
            if (!KDSConst._DEBUG)
                return;
        }

        Intent intent = new Intent(MainActivity.this, KDSActivityMedia.class);

        intent.putExtra("files", files.toCSV());
        startActivityForResult(intent, KDSConst.SHOW_MEDIA_PLAYER);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    /**
     * http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4
     * @param userID
     */
    public void  doMoreFunc_Training_Video(KDSUser.USER userID)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");

        KDSDataItem item = getSelectItem(userID);
        if (item == null) return;
        CSVStrings files = item.getTrainingVideo();
        //files.add("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"); //test video
        if (files.getCount() <=0) {
            showToastMessage(this.getString(R.string.no_training_video));//"No training_video video files for selected item");
            return;
        }

        Intent intent = new Intent(MainActivity.this, KDSActivityMedia.class);

        intent.putExtra("files",files.toCSV());
        startActivityForResult(intent, KDSConst.SHOW_MEDIA_PLAYER);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }


    public void doMoreFunc_ClearMessages(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        MainActivityFragment f = getMainFragment();
        if (f != null)
            f.clearInfo();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void doMoreFunc_CookStarted(KDSUser.USER userID)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        String orderGuid = getFocusedOrderGUID(userID);
        if (orderGuid.isEmpty()) return;

        KDSStationFunc.orderCookStarted(getKDS().getUsers().getUser(userID), orderGuid);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }


    final String LS8000_MAC_FLAG = "000EC3";

    /**
     * check mac address, the LS8000 is "000ec3"
     *
     * @return
     */
    public boolean isMacMatch2() {
        if (KDSConst.ENABLE_FEATURE_ACTIVATION)
            return true;
        
        ArrayList<String> ar = KDSSocketManager.getLocalAllMac();
        String strMac = "";

        if (ar.size() <=0) {
            strMac = KDSSocketManager.getMacAddressFromFile();
            return isAcceptMac(strMac);
            //return false;
        } else {
            for (int i=0; i< ar.size(); i++) {
                strMac = ar.get(i);
                if (isAcceptMac(strMac))
                    return true;
            }
        }

        return false;
    }

    private boolean isAcceptMac(String strMac)
    {
        strMac = strMac.toUpperCase();
        if (strMac.isEmpty()) return false;
        if (strMac.length() < 6) return false;
        //for test
        String[] arTest = new String[]{"000000000039", "FC64BAB87E4A", "000000000002"};
        for (int i = 0; i < arTest.length; i++)
            if (strMac.equals(arTest[i])) return true;

        String strMacPreFix = strMac.substring(0, 6);

        return (strMacPreFix.equals(LS8000_MAC_FLAG));
    }

    public boolean showKdsInfo(String str) {

        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter,s = " + str);
        String message = str;
        if (message.equals(KDSSMBDataSource.PATH_LOST))
        {
            String s = this.getString(R.string.smb_folder_lost);
            String folder = this.getKDS().getSettings().getString(KDSSettings.ID.KDS_Data_Folder);
            KDSSMBPath path = KDSSMBPath.parseString(folder);
            folder = path.toDisplayString();
            s = s.replace("#", folder);
            showToastMessage(s);
            return true;

        }
        else if (message.equals(KDSSMBDataSource.PATH_PERMISSION))
        {
            String s = this.getString(R.string.error_folder_permission);
            showToastMessage(s);
            return true;

        }
        else {

            MainActivityFragment f = getMainFragment();
            if (f != null)
                return f.showInfo(str);
            return false;
        }
    }

    public class KDSMainUIMessageHandler extends Handler {
        static final public int KDS_INFO = 100;
        static final public int PERMISSION_ERROR = 101;
        public void sendInformation(String objStr) {
            Message m = new Message();
            m.what = KDS_INFO;
            m.obj = objStr;
            this.sendMessage(m);
        }

        public void sendPermissionError() {
            Message m = new Message();
            m.what = PERMISSION_ERROR;

            this.sendMessage(m);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KDS_INFO: {
                    String info = (String) (msg.obj);

                    MainActivity.this.showKdsInfo(info);
                }
                break;
                case PERMISSION_ERROR:
                {
                    MainActivity.this.showPermissionErrorDialog();
                }
                break;
            }
        }
    }


    public KDS getKDS()
    {

        return KDSGlobalVariables.getKDS();
    }


    private void setScreensRatio()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (getKDS().isMultpleUsersMode())
        {
            int nr = KDSSettings.getEnumIndexValues(getKDS().getSettings(), KDSSettings.ScreensRatio.class, KDSSettings.ID.General_screens_ratio);
            KDSSettings.ScreensRatio ratio = KDSSettings.ScreensRatio.values()[nr];

            m_layoutKdsViews.setWeightSum(2);
            float flt = 1f/2f;
            switch (ratio)
            {
                case R1d2:
                    flt =1f/2f;
                    break;
                case R1d3:
                    flt = 1f/3f;
                    break;
                case R2d3:
                    flt = 2f/3f;
                    break;
                case R1d4:
                    flt = 1f/4f;
                    break;
                case R3d4:
                    flt = 3f/4f;
                    break;
            }

            LinearLayout.LayoutParams paramB = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,2f * flt);
            ((LinearLayout)getLinear(KDSUser.USER.USER_B)).setLayoutParams(paramB);

            LinearLayout.LayoutParams paramA = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 2f*(1f-flt) );
            getLinear(KDSUser.USER.USER_A).setLayoutParams(paramA);



        }
        else
        {
            m_layoutKdsViews.setWeightSum(1); //fix the outside issue when show the touch buttons
            LinearLayout.LayoutParams paramA = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            View v =getLinear(KDSUser.USER.USER_A);
            if (v != null)
                v.setLayoutParams(paramA);


        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }
    private void setupUserScreenOrientation()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (getKDS().isMultpleUsersMode())
        {


            int n =  KDSSettings.getEnumIndexValues(getKDS().getSettings(), KDSSettings.ScreenOrientation.class, KDSSettings.ID.Screens_orientation);
            KDSSettings.ScreenOrientation orientation = KDSSettings.ScreenOrientation.values()[n];
            switch (orientation)
            {

                case Left_Right:
                    m_layoutKdsViews.setOrientation(LinearLayout.HORIZONTAL);
                    break;
                case Up_Down:
                    m_layoutKdsViews.setOrientation(LinearLayout.VERTICAL);
                    break;
            }
            m_layoutKdsViews.setWeightSum(2);

        }
        else
        {


            m_layoutKdsViews.setOrientation(LinearLayout.VERTICAL);
            m_layoutKdsViews.setWeightSum(1); //fix the outside issue when show the touch buttons

        }
        setScreensRatio();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void init_summary_gui()
    {
        initSummaryFragment();
        m_uiUserA.init_top_sum(this);
        m_uiUserB.init_top_sum(this);
        this.getTopSum(KDSUser.USER.USER_A).setVisibility(View.GONE);
        this.getTopSum(KDSUser.USER.USER_B).setVisibility(View.GONE);
    }

    private void show_summary_always()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        m_uiUserA.showSummaryAlways(KDSUser.USER.USER_A);
        if (getKDS().isMultpleUsersMode())
            m_uiUserB.showSummaryAlways(KDSUser.USER.USER_B);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void reinitKDSPrep()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        init_information_list_gui();

        //
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        getKDS().getPrinter().initBemaPrinter(this.getBaseContext(), usbManager);
        //we must init it.
        //if (this.getLayout(KDSUser.USER.USER_A) == null) return;
        if (!isUserLayoutReady(KDSUser.USER.USER_A)) return;
        if (this.getLayout(KDSUser.USER.USER_A) != null)
            this.getLayout(KDSUser.USER.USER_A).getEnv().setSettings(getKDS().getSettings());

        init_user_screen_gui_variables();
        init_summary_gui();

        build_gui_according_to_user_mode(getKDS().isSingleUserMode()? KDSSettings.KDSUserMode.Single: KDSSettings.KDSUserMode.Multiple);

        show_summary_always();

        init_focus();

        init_default_focus();

        askIfLoadOldDB();


        MainActivityFragment f = (MainActivityFragment) (getFragmentManager().findFragmentById(R.id.fragmentMain));
        f.updateSettings(getKDS().getSettings());

        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

    }
    private void initAfterKDSServiceConnected()
    {
        if (!isKDSValid()) return ;

        //2.1.10
        getKDS().setSMSActivation(m_activation);


        initStationGeneralSteps();

        init_information_list_gui();

        reinitKDSPrep();

    }

    private void init_focus()
    {
        if (getKDS().isSingleUserMode()) {
            opFocusNext(KDSUser.USER.USER_A);
        } else {
            opFocusNext(KDSUser.USER.USER_A);
            opFocusNext(KDSUser.USER.USER_B);
            focusUser(KDSUser.USER.USER_A);
        }
    }

    private void reinitTT()
    {

        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        setupGuiByMode(GUI_MODE.Tracker);
        m_ttView.updateSettings(getKDS().getSettings());

        m_uiUserA.showSummaryAlways( KDSUser.USER.USER_A);

        m_ttView.startTT();
        m_ttView.refresh();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }
    private void initTrackerStationAfterKDSServiceConnected()
    {
        if (!isKDSValid()) return ;
        initStationGeneralSteps();
        reinitTT();

    }

    private void init_common_in_create_and_pref_changed()
    {
        if (getKDS().getSettings().getBoolean(KDSSettings.ID.Tab_Enabled)) {
            m_tabDisplay.show();
        }
        else {
            m_tabDisplay.hide();
            getKDS().getUsers().getUser(KDSUser.USER.USER_A).tabDisplayDestinationRestore();
            if (getKDS().isMultpleUsersMode())
                getKDS().getUsers().getUser(KDSUser.USER.USER_B).tabDisplayDestinationRestore();
            getKDS().getSettings().setTabDestinationFilter("");
            getKDS().getSettings().setTabCurrentFunc(KDSSettings.StationFunc.MAX_COUNT);
        }
        init_title();
        hideNavigationBar();
    }

    private void initStationGeneralSteps()
    {
        if (!isKDSValid()) return ;
        getKDS().start();//getKDS().getSettings());

        m_timer.start(this, this, 1000);
        //init_title();
        if (!SettingsBase.isNoCheckRelationWhenAppStart(this)) {
            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    getKDS().getBroadcaster().broadcastRequireRelationsCommand();
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (!getKDS().isDataLoaded())
            refreshWithNewDbData();

        getKDS().checkPingThreadAfterResume();
        init_common_in_create_and_pref_changed();

    }

    /**
     *
     * @param bSetAllOrdersToUserAInDatabase
     *      KPP1-272
     *      In tab display and multiple user mode, the orders just need to load to userA temporary.
     *      True: just all orders to user A in database, it will happen we change user mode.
     *      false: Just load to user A, but don't change database. This just happened in Tab mode.
     */
    private void reinitQueue(boolean bSetAllOrdersToUserAInDatabase)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        setupGuiByMode(GUI_MODE.Queue);

        //kpp1-288, I start to pass two user orders to queue view, so don't need following function.
        //getKDS().getUsers().setSingleUserMode(bSetAllOrdersToUserAInDatabase);
        m_queueView.updateSettings(getKDS().getSettings());

        m_uiUserA.showSummaryAlways( KDSUser.USER.USER_A);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

    }
    //the queue stations
    private void initQueueStationAfterKDSServiceConnected()
    {
        if (!isKDSValid()) return ;
        initStationGeneralSteps();
        reinitQueue(true);
        //call this function again, as the queue conflict with tab
        init_common_in_create_and_pref_changed(); //kpp1-288, the screen is not shown when tab enabled and queue enabled

    }

    public void threadrefresh_FocusFirst()
    {
        if (!getKDS().isQueueExpo()) { //queue-expo don't need to focus first order
            String firstOrderGuid = getFirstOrderGuidToFocus(getFocusedUserID());
            onSetFocusToOrder(firstOrderGuid);
        }
        refreshView();
    }
    public boolean isKDSValid()
    {
        return (getKDS() != null);
    }

    public void schedule_order_ready_qty_changed(KDSUser.USER userID, ScheduleProcessOrder order, int nInputQty)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        int nQty = order.get_ready_qty() + nInputQty;
        order.set_ready_qty(nQty);
        getKDS().schedule_process_ready_qty_changed(userID, order);

        refreshView(userID);

        KDSStationFunc.sync_with_stations(getKDS(), KDSXMLParserCommand.KDSCommand.Schedule_Item_Ready_Qty_Changed, order, ScheduleProcessOrder.get_prepare_item(order), "");
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void setupGuiByMode(GUI_MODE guiMode)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter, mode=" + guiMode.toString());
        switch (guiMode)
        {

            case KDS:
                m_layoutTT.setVisibility(View.GONE);
                m_layoutQueue.setVisibility(View.GONE);

                m_layoutKDSContainer.setVisibility(View.VISIBLE);
                break;
            case Queue:
                m_layoutKDSContainer.setVisibility(View.GONE);
                m_layoutTT.setVisibility(View.GONE);

                m_layoutQueue.setVisibility(View.VISIBLE);
                break;
            case Tracker:
                m_layoutKDSContainer.setVisibility(View.GONE);
                m_layoutQueue.setVisibility(View.GONE);

                m_layoutTT.setVisibility(View.VISIBLE);

                break;
        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void queueFocusNextOrder()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        m_queueView.focusNext();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }
    public void ttFocusNextOrder()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        m_ttView.focusNext();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void queueFocusPrevOrder()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        m_queueView.focusPrev();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void ttFocusPrevOrder()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        m_ttView.focusPrev();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }
    public void queueBumpOrder()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        String orderGuid = m_queueView.getFocusedGuid();
        if (orderGuid.isEmpty()) return;

        if (getKDS().getSettings().getBoolean(KDSSettings.ID.Bumping_confirm)) {
            confirmBumpFocusedOrder(KDSUser.USER.USER_A, orderGuid);
        }
        else if (getKDS().getSettings().getBoolean(KDSSettings.ID.Confirm_bump_unpaid) && isOrderUnpaid(KDSUser.USER.USER_A, orderGuid))
        {
            confirmBumpFocusedOrderUnpaid(KDSUser.USER.USER_A, orderGuid);
        }
        else if (getKDS().getSettings().getBoolean(KDSSettings.ID.Confirm_bump_outstanding) && isOrderOutstanding(KDSUser.USER.USER_A, orderGuid))
        {
            confirmBumpFocusedOrderOutstanding(KDSUser.USER.USER_A, orderGuid);
        }
        else
            queueBumpOrderOperation(orderGuid);

        getKDS().getCurrentDB().clearExpiredBumpedOrders(getSettings().getBumpReservedCount());
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void queueBumpOrderOperation( String orderGuid) {

        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        String guid = m_queueView.getFocusedGuid();
        boolean bIsFocusedOrder = orderGuid.equals(guid);

        //get next for focus
        String nextGuid = "";
        if (bIsFocusedOrder)
            nextGuid = m_queueView.getNextGuid(guid);// getNextOrderGuidToFocus(userID, orderGuid);//"";

        //save it for printing.
        KDSDataOrder order = queueBumpOrder(KDSUser.USER.USER_A, orderGuid);
        if (order == null) return;

        if (bIsFocusedOrder) {
            m_queueView.focusOrder(nextGuid);
        }
        refreshQueueView();

        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public KDSDataOrder queueBumpOrder(KDSUser.USER userID, String orderGuid) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return new KDSDataOrder();

        //save it for printing.
        KDSDataOrder order = m_queueView.getOrders().getOrderByGUID(orderGuid);// getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);

        KDSStationFunc.orderBump(getKDS().getUsers().getUser(userID), orderGuid, true);
        //notification
        notifyPOSOrderBump(userID, order);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
        return order;

    }

    public void onDoubleClicked(MotionEvent e)
    {
        bumpTrackerOrder(KDSUser.USER.USER_A, getFocusedOrderGUID(KDSUser.USER.USER_A));
    }


    private void checkAutoBackup()
    {
        if (!this.getSettings().getBoolean(KDSSettings.ID.Enable_auto_backup))
            return;

        int nHours = this.getSettings().getInt(KDSSettings.ID.Auto_backup_hours);
        int ms = nHours * 60 * 60 * 1000;
        //int ms = 10000; // for test
        if (m_tdAutoBackup.is_timeout(ms)) {
            export_data(AUTO_BACKUP_FOLDER);
            m_tdAutoBackup.reset();
        }


    }

    TimeDog m_logDog = new TimeDog();
    private void checkLogFilesDeleting()
    {
        if (!m_logDog.is_timeout(30*1000)) //30 seconds
            return;
        int n = this.getSettings().getInt(KDSSettings.ID.Log_mode);
        KDSLog.LogLevel l = KDSLog.LogLevel.values()[n];
        if (l == KDSLog.LogLevel.None) return;

        int nDays = this.getSettings().getInt(KDSSettings.ID.Log_days);
        KDSLog.removeLogFiles(nDays);
        KDSLogOrderFile.removeLogFiles(nDays);

    }

    KDSUIDlgDbCorrupt m_dbCorruptDlg = null;
    public void onDBCorrupt()
    {
        if (m_dbCorruptDlg != null) return;
          m_dbCorruptDlg = new KDSUIDlgDbCorrupt(this, this);
        m_dbCorruptDlg.show();
    }

    /**
     * the SD card lost
     * 2.0.13
     */
    public void onSDCardUnmount()
    {
        if (m_sdcardLostDialog != null)
            return;
        showSdCardLostDialog();
    }

    /**
     * 2.0.14
     */
    public void onDiskFull()
    {
        if (m_sdcardFullDialog != null)
            return;
        showSdCardFullDialog();
    }


    /**
     *2.0.14
     */
    AlertDialog m_sdcardFullDialog = null;
    public void showSdCardFullDialog()
    {

        String strOK = KDSUIDialogBase.makeOKButtonText2(this);// .makeButtonText(this,R.string.ok, KDSSettings.ID.Bumpbar_OK );


        m_sdcardFullDialog = new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.message))
                .setMessage(this.getString(R.string.sd_disk_full))
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                m_sdcardFullDialog = null;
                            }
                        }
                )

                .create();
        m_sdcardFullDialog.setCancelable(false);//.setFinishOnTouchOutside(false);
        m_sdcardFullDialog.setCanceledOnTouchOutside(false);
        m_sdcardFullDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);

                if (evID == KDSSettings.ID.Bumpbar_OK) {
                    dialog.dismiss();
                    m_sdcardFullDialog = null;
                    return true;
                } else if (evID == KDSSettings.ID.Bumpbar_Cancel) {
                    dialog.cancel();
                    m_sdcardFullDialog = null;
                    return true;
                }
                return false;
            }
        });
        m_sdcardFullDialog.show();
    }

    /**
     *2.0.13
     */
    AlertDialog m_sdcardLostDialog = null;
    public void showSdCardLostDialog()
    {

        String strOK = KDSUIDialogBase.makeOKButtonText2(this);// .makeButtonText(this,R.string.ok, KDSSettings.ID.Bumpbar_OK );
        //String strCancel = KDSUIDialogBase.makeButtonText(this,R.string.cancel, KDSSettings.ID.Bumpbar_Cancel );

        m_sdcardLostDialog = new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.message))
                .setMessage(this.getString(R.string.nand_lost))
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                m_sdcardLostDialog = null;
                            }
                        }
                )
//                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        PreferenceFragmentStations.this. reloadRelations();
//                    }
//                })
                .create();
        m_sdcardLostDialog.setCancelable(false);//.setFinishOnTouchOutside(false);
        m_sdcardLostDialog.setCanceledOnTouchOutside(false);
        m_sdcardLostDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);

                if (evID == KDSSettings.ID.Bumpbar_OK) {
                    dialog.dismiss();
                    m_sdcardLostDialog = null;
                    return true;
                } else if (evID == KDSSettings.ID.Bumpbar_Cancel) {
                    dialog.cancel();
                    m_sdcardLostDialog = null;
                    return true;
                }
                return false;
            }
        });
        m_sdcardLostDialog.show();
    }

    private void  doDbCorruptOperations(KDSUIDlgDbCorrupt.DB_Corrupt_Operation op)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        switch (op)
        {

            case Unknown:
                break;
            case Run_Util:
                startKDSUtility();
                break;
            case Ignore:
                break;
            case Reset:
                doClearDB(false);
                break;
            case Abort:
                this.finish();
                break;
        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void qexpoPickup()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        String orderGuid = m_queueView.getFocusedGuid();
        if (orderGuid.isEmpty()) return;

        KDSDataOrder order =  m_queueView.getOrders().getOrderByGUID(orderGuid);
        if (order == null) {
            m_queueView.focusOrder("");
            return;
        }
        boolean bPickup = true;
        String keycode = getKDS().getSettings().getString(KDSSettings.ID.Bumpbar_QExpo_Unpickup);
        if (keycode.isEmpty() ||
                keycode.equals("0,0,0,0"))
        {
            bPickup = order.getQueueReady();
            bPickup = (!bPickup);
        }
        order.setQueueReady(bPickup);
        getKDS().getCurrentDB().orderSetQueueReady(order.getGUID(), bPickup);
        refreshQueueView();
        m_queueView.focusOrder("");
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }
    private void qexpoUnpickup()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        String orderGuid = m_queueView.getFocusedGuid();
        if (orderGuid.isEmpty()) return;
        KDSDataOrder order =  m_queueView.getOrders().getOrderByGUID(orderGuid);

        order.setQueueReady(false);
        getKDS().getCurrentDB().orderSetQueueReady(order.getGUID(), false);
        refreshQueueView();
        m_queueView.focusOrder("");
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }
    private void qexpoBumpOrder()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        String orderGuid = m_queueView.getFocusedGuid();
        if (orderGuid.isEmpty()) {
            String keycode =getKDS().getSettings().getString(KDSSettings.ID.Bumpbar_Unbump);
            if (keycode.isEmpty() ||
                    keycode.equals("0,0,0,0")) {
                opUnbumpLast(KDSUser.USER.USER_A);
                m_queueView.focusOrder("");
            }

        }
        else
        {
            queueBumpOrder();
        }
        m_queueView.focusOrder("");
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }


    /**
     * Tab display, the button was clicked.
     * @param btnData
     */
    public void onTabClicked(TabDisplay.TabButtonData btnData)
    {
        init_user_screen_gui_variables();

        updateUISettings();

        switch (btnData.getFunc())
        {

            case Orders:

                getKDS().getSettings().setTabCurrentFunc(KDSSettings.StationFunc.Prep);
                if (KDSSettings.isMultipleMode())
                    getKDS().getUsers().setTwoUserMode();
                getKDS().getSettings().setTabDestinationFilter("");
                getKDS().getSettings().setTabEnableLineItemsView(false);
                getKDS().getSettings().restoreOrdersSortToDefault();
                getKDS().getUsers().getUser(KDSUser.USER.USER_A).tabDisplayDestinationRestore();
                getKDS().getSettings().setLineItemsViewEnabled(false); //kpp1-353
                if (getKDS().isMultpleUsersMode())
                    getKDS().getUsers().getUser(KDSUser.USER.USER_B).tabDisplayDestinationRestore();
                getKDS().clearAllBufferedOrders(); //kpp1-353
                getKDS().loadAllActiveOrders();
                reinitKDSPrep();
                refreshTabSort();

                break;
            case Destination:

                getKDS().getSettings().setTabCurrentFunc(KDSSettings.StationFunc.Prep);
                if (KDSSettings.isMultipleMode())
                    getKDS().getUsers().setTwoUserMode();
                getKDS().getSettings().setTabEnableLineItemsView(false);
                getKDS().getSettings().restoreOrdersSortToDefault();
                getKDS().getSettings().setLineItemsViewEnabled(false); //kpp1-353
                getKDS().clearAllBufferedOrders(); //kpp1-353
                getKDS().loadAllActiveOrders();
                String strDest = btnData.getStringParam();

                boolean bRestore = false;
                if (!getKDS().getSettings().getTabDestinationFilter().equals(strDest))
                    bRestore = true;
                getKDS().getSettings().setTabDestinationFilter(strDest);
                if (bRestore) {
                    getKDS().getUsers().getUser(KDSUser.USER.USER_A).tabDisplayDestinationRestore();
                    if (getKDS().isMultpleUsersMode())
                        getKDS().getUsers().getUser(KDSUser.USER.USER_B).tabDisplayDestinationRestore();
                }
                getKDS().getUsers().getUser(KDSUser.USER.USER_A).tabDisplayDestinationFilter(strDest);
                if (getKDS().isMultpleUsersMode())
                    getKDS().getUsers().getUser(KDSUser.USER.USER_B).tabDisplayDestinationFilter(strDest);
                reinitKDSPrep();
                refreshTabSort();

                break;
            case Queue:
                //boolean isMultipleUserMode = getKDS().isMultpleUsersMode();
                getKDS().getSettings().setTabCurrentFunc(KDSSettings.StationFunc.Queue);
                getKDS().getSettings().setTabDestinationFilter("");
                getKDS().getSettings().setTabEnableLineItemsView(false);
                getKDS().getSettings().setLineItemsViewEnabled(false); //kpp1-353
                getKDS().getSettings().restoreOrdersSortToDefault();
                getKDS().getUsers().getUser(KDSUser.USER.USER_A).tabDisplayDestinationRestore();
                reinitQueue(false);
                //if (isMultipleUserMode)
                //    getKDS().loadAllActiveOrdersNoMatterUsers();//kpp1-272
                refreshTabSort();

                break;
            case TableTracker:

                getKDS().getSettings().setTabCurrentFunc(KDSSettings.StationFunc.TableTracker);
                getKDS().getSettings().setTabDestinationFilter("");
                getKDS().getSettings().setTabEnableLineItemsView(false);
                getKDS().getSettings().setLineItemsViewEnabled(false); //kpp1-353
                getKDS().getSettings().restoreOrdersSortToDefault();
                getKDS().getUsers().getUser(KDSUser.USER.USER_A).tabDisplayDestinationRestore();
                reinitTT();
                refreshTabSort();
                break;
            case LineItems:

                //getKDS().getSettings().setStationFunc(KDSSettings.StationFunc.Normal);
                getKDS().getSettings().setTabCurrentFunc(KDSSettings.StationFunc.Prep);
                if (KDSSettings.isMultipleMode())
                    getKDS().getUsers().setTwoUserMode();
                getKDS().getSettings().setTabDestinationFilter("");
                getKDS().getSettings().setTabEnableLineItemsView(true);
                getKDS().getSettings().restoreOrdersSortToDefault();
                getKDS().getUsers().getUser(KDSUser.USER.USER_A).tabDisplayDestinationRestore();
                getKDS().getSettings().setLineItemsViewEnabled(true);
                getKDS().clearAllBufferedOrders(); //kpp1-353
                getKDS().loadAllActiveOrders();
                reinitKDSPrep();
                refreshTabSort();
                //onStationFunctionChanged(KDSSettings.StationFunc.Normal);
                break;
            case Sort_orders:
            {
                getKDS().getSettings().setTabCurrentFunc(KDSSettings.StationFunc.Prep);
                if (KDSSettings.isMultipleMode())
                    getKDS().getUsers().setTwoUserMode();
                getKDS().getSettings().setTabDestinationFilter("");
                getKDS().getSettings().setTabEnableLineItemsView(false);
                getKDS().getSettings().setLineItemsViewEnabled(false); //kpp1-353
                String strSort = btnData.getStringParam();
                int n = KDSUtil.convertStringToInt(strSort, 0);
                KDSSettings.OrdersSort sort = KDSSettings.OrdersSort.values()[n];
                getKDS().getSettings().setCurrentOrdersSort(sort);
                getKDS().clearAllBufferedOrders(); //kpp1-353
                getKDS().loadAllActiveOrders();

                getKDS().getUsers().getUser(KDSUser.USER.USER_A).tabDisplayDestinationRestore();
                if (getKDS().isMultpleUsersMode())
                    getKDS().getUsers().getUser(KDSUser.USER.USER_B).tabDisplayDestinationRestore();


                reinitKDSPrep();
                refreshTabSort();


            }
            break;
            case MAX_COUNT:

                getKDS().getSettings().tabDisabled();
                break;
        }

        int nIndex = KDSSettings.getEnumIndexValues(getSettings(),KDSSettings.KDSUserMode.class, KDSSettings.ID.Users_Mode );
        KDSSettings.KDSUserMode m = KDSSettings.KDSUserMode.values()[nIndex];//settings.getInt(KDSSettings.ID.Users_Mode)];
        build_gui_according_to_user_mode(m);
        setupGuiByMode(getGuiMode());
        init_title();
        refreshView();

    }

    private void refreshTabSort()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        sortOrders(KDSUser.USER.USER_A, getKDS().getSettings().getCurrentOrdersSort());
        if (!isUserLayoutReady(KDSUser.USER.USER_A)) return;
        getUserUI(KDSUser.USER.USER_A).getLayout().getEnv().getStateValues().setFirstShowingOrderGUID("");
        String firstOrderGuid = getFirstOrderGuidToFocus(KDSUser.USER.USER_A);
        onSetFocusToOrder(firstOrderGuid);
        if (getKDS().isMultpleUsersMode())
        {
            if (!isUserLayoutReady(KDSUser.USER.USER_B)) return;
            sortOrders(KDSUser.USER.USER_B, getKDS().getSettings().getCurrentOrdersSort());
            getUserUI(KDSUser.USER.USER_B).getLayout().getEnv().getStateValues().setFirstShowingOrderGUID("");
            String orderGuid = getFirstOrderGuidToFocus(KDSUser.USER.USER_B);
            onSetFocusToOrder(orderGuid);
        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

    }
    private void opTabNextDisplayMode()
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        TabDisplay.TabButtonData data = m_tabDisplay.getNextTabDisplayMode();
        if (data == null)
            return;
        onTabClicked(data);
        m_tabDisplay.setFocus(data);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    /**
     * 2.0.25
     * Add two more button to touch button, Next guest_paging/ Prev guest_paging which go to next guest_paging directory; also apply this to Bumpbar key assignment.
     * @param userID
     * @return
     *  true: do next page.
     *  false; No more data
     */
    private boolean opNextPage(KDSUser.USER userID)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isUserLayoutReady(userID)) return false;
        int ncount = getUserUI(userID).getLayout().getNextCount();
        getUserUI(userID).getLayout().focusNextPage();

        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
        return (ncount >0);
    }

    /**
     * 2.0.25
     * Add two more button to touch button, Next guest_paging/ Prev guest_paging which go to next guest_paging directory; also apply this to Bumpbar key assignment.
     * @param userID
     */
    private boolean opPrevPage(KDSUser.USER userID)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isUserLayoutReady(userID)) return false;
        int ncount = getUserUI(userID).getLayout().getPrevCount();
        getUserUI(userID).getLayout().focusPrevPage();


        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
        return (ncount >0);
    }

    TimeDog m_avgTimeDog = new TimeDog();

    private void refreshAvgPrepTime()
    {
        if (!m_avgTimeDog.is_timeout(5*1000)) return;
        m_avgTimeDog.reset();
        if (!isUserLayoutReady(KDSUser.USER.USER_A)) return;
        if (getKDS() == null) return;
        if (getKDS().isMultpleUsersMode()) {
            if (!isUserLayoutReady(KDSUser.USER.USER_B)) return;
        }
        if (!getSettings().getBoolean(KDSSettings.ID.Show_avg_prep_time)) {
            getUserUI(KDSUser.USER.USER_A).hideAvgPrepTimeView(true);
            if (getKDS().isMultpleUsersMode())
                getUserUI(KDSUser.USER.USER_B).hideAvgPrepTimeView(true);
            return;
        }
        int nPeriod = getSettings().getInt(KDSSettings.ID.Avg_prep_period);
        getUserUI(KDSUser.USER.USER_A).refreshAvgPrepTime(getKDS().getStatisticDB(), KDSUser.USER.USER_A,nPeriod );
        if (getKDS().isMultpleUsersMode())
            getUserUI(KDSUser.USER.USER_B).refreshAvgPrepTime(getKDS().getStatisticDB(), KDSUser.USER.USER_B,nPeriod );
    }

//    public void onItemQtyChanged(KDSDataOrder order, KDSDataItem item)
//    {
//
//    }
//    public void onOrderStatusChanged(KDSDataOrder order, int nOldStatus)
//    {
//
//    }
//    public class BumpThread extends Thread
//    {
//        public KDSUser.USER m_userID = KDSUser.USER.USER_A;
//        public  ArrayList<String> m_arOrderGuid = new ArrayList<>();
//        Object m_locker = new Object();
//
//        public int setOrderGuids( ArrayList<String> ar)
//        {
//            if (m_arOrderGuid.size() >0) return 0;
//            synchronized (m_locker) {
//                m_arOrderGuid.addAll(ar);
//            }
//            return ar.size();
//        }
//
//        @Override
//        public void run() {
//          //  super.run();
//            synchronized (m_locker) {
//                for (int i = 0; i < m_arOrderGuid.size(); i++)
//                    bumpOrderOperation(m_userID, m_arOrderGuid.get(i),false);
//            }
//            getKDS().refreshView();
//        }
//    }
    private TextView getTextView(int viewID)
    {
        return (TextView) this.findViewById(viewID);
    }

    private void testException()
    {
        try
        {
            int n = 0;
            int nn = 30;
            int ff = nn/n;

            Log.d(TAG, "Test exception" + KDSUtil.convertIntToString(ff));

        }
        catch (Exception err)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(), err);
        }
    }

    public void onActivationSuccess()
    {
        //Toast.makeText(this, "Activation is done", Toast.LENGTH_LONG).show();

        checkRemovedStationsFromBackofficeAfterRegister();
        updateTitle();
    }
    public void onActivationFail(ActivationRequest.COMMAND stage, ActivationRequest.ErrorType errType, String failMessage)
    {
       // Toast.makeText(this, "Activation failed: " +stage.toString()+" - " + failMessage, Toast.LENGTH_LONG).show();
//        if (ActivationRequest.needResetUsernamePassword(errType))
//            m_activation.resetUserNamePassword();

        checkActivationResult(stage, errType);
        if (Activation.needShowInactiveTitle(errType))
            updateTitle();
    }
    private void checkActivationResult(ActivationRequest.COMMAND stage,ActivationRequest.ErrorType errType)
    {
        if (m_activation.isActivationFailedEnoughToLock() || errType == ActivationRequest.ErrorType.License_disabled)
        {
            //this.finish();
            doActivation(false, true, this.getString(R.string.license_deactivated));
            //m_activation.showLoginActivity(this);
        }
    }

    TimeDog m_activationDog = new TimeDog();

    /**
     * If it is inactive, check activation in every 5 minutes.
     * Otherwise, 1 hour.
     */
    private void checkAutoActivation()
    {
        if (!KDSConst.ENABLE_FEATURE_ACTIVATION)
            return;
        if (ActivityLogin.isShowing()) return;
        int nTimeout = Activation.HOUR_MS;
        if (!m_activation.isActivationPassed())
            nTimeout = Activation.INACTIVE_TIMEOUT; //5 minutes
        if (m_activationDog.is_timeout(nTimeout))// * Activation.ACTIVATION_TIMEOUT_HOURS))
        //if (m_activationDog.is_timeout(5000))// * Activation.ACTIVATION_TIMEOUT_HOURS)) //DEBUG
        {

            doActivation(true, false, "");
            m_activationDog.reset();
        }
    }

    /**
     *
     * @param bSilent
     * @param bForceShowNamePwdDlg
     * @param showErrorMessage
     *      If it is "", show nothing,
     *      Otherwise, show this error message to login dialog.
     */
    public void doActivation(boolean bSilent,boolean bForceShowNamePwdDlg, String showErrorMessage)
    {
        if (!KDSConst.ENABLE_FEATURE_ACTIVATION)
            return;
        if (m_activation.isDoLicensing()) {
            showToastMessage(getString(R.string.internal_doing_activation));// "Internal activation is in process, please logout again later.");
            return; //kpp1-304, maybe this cause kds can not logout issue.
        }

        m_activation.setStationID(getKDS().getStationID());
        ArrayList<String> ar = KDSSocketManager.getLocalAllMac();
        //kpp1-399, allow mac is empty.
        //if (ar.size()<=0)
//        {
//            showToastMessage(getString(R.string.no_network_detected));//"No network interface detected");
//            return;//kpp1-304, maybe this cause kds can not logout issue.
//        }
        if (ar.size() >0)//kpp1-399
            m_activation.setMacAddress(ar.get(0));

      //  m_activation.setMacAddress("BEMA0000011");//test
        //Log.i(TAG, "reg: doActivation,bSlient="+ (bSilent?"true":"false"));
        m_activation.startActivation(bSilent,bForceShowNamePwdDlg, this, showErrorMessage);
    }

    public void onSMSSendSuccess(String orderGuid, int smsState)
    {
        getKDS().onSMSSuccess(orderGuid, smsState);
    }

    public void onSyncWebReturnResult(ActivationRequest.COMMAND stage, String orderGuid, Activation.SyncDataResult result)
    {
        switch (result)
        {

            case OK:
                this.showToastMessage("Sync OK:"+stage.name()+ " " + orderGuid);
                //Toast.makeText(this, "Sync OK:"+stage.name()+ " " + orderGuid, Toast.LENGTH_SHORT).show();
                break;
            case Fail_Http_exception:
                this.showToastMessage("Sync failed(Internet error): " +stage.name()+ " "+ orderGuid);
                //Toast.makeText(this, "Sync failed(Internet error): " +stage.name()+ " "+ orderGuid, Toast.LENGTH_LONG).show();
                break;
            case Fail_reponse_error:
                this.showToastMessage("Sync failed(Server error): " + stage.name()+ " " + orderGuid);
                //Toast.makeText(this, "Sync failed(Server error): " + stage.name()+ " " + orderGuid, Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     *
     */
    public void onDoActivationExplicit()
    {
       doActivation(false, true, "");
    }

    public void onForceClearDataBeforeLogin()
    {
        MainActivity.this.getKDS().clearAll();
        getKDS().clearStatisticData();
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(getContext());
        //pre.registerOnSharedPreferenceChangeListener(this);
        setToDefaultSettings();
        Activation.resetOldLoginUser(); //kpp1-299
        onSharedPreferenceChanged(pre, "");
        inputStationID();
        //pre.unregisterOnSharedPreferenceChangeListener(this);
    }
    Thread m_threadChecking = null;

    /**
     * Move some timer functions to here.
     * Just release main UI.
     * All feature in this thread are no ui drawing request.
     * And, in checkautobumping function, it use message to refresh UI.
     */
    public void startCheckingThread()
    {
        if (m_threadChecking == null ||
                !m_threadChecking.isAlive())
        {
            m_threadChecking = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (getKDS().isThreadRunning())
                    {
                        try {
                            if (m_threadChecking != Thread.currentThread())
                                return;
                            //TimeDog td = new TimeDog();
                            checkAutoBumping();
                            //td.debug_print_Duration("checkAutoBumping");
                            //move it to thread
                            checkAutoBackup();
                            //td.debug_print_Duration("checkAutoBackup");
                            //move it to thread
                            checkLogFilesDeleting();
                            //td.debug_print_Duration("checkLogFilesDeleting");
                            //remove statistic data
                            getKDS().checkRemovingStatisticExpiredData();
                            //td.debug_print_Duration("checkRemovingStatisticExpiredData");
                            //KPP1-192
                            checkSystemTimeChanged();
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {

                            }
                        }
                        catch ( Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            });
            m_threadChecking.setName("Checking");
            m_threadChecking.start();
        }
    }

    public void setToDefaultSettings()
    {
        this.getSettings().setToDefault();

    }

    TimeDog m_tdAutoRefreshScreen = new TimeDog();
    private void auto_refresh_screen()
    {
        int nSeconds = this.getSettings().getInt(KDSSettings.ID.Auto_refresh_screen_freq);
        if (nSeconds <=0) return;

        if (m_tdAutoRefreshScreen.is_timeout(nSeconds * 1000))
        {
            m_tdAutoRefreshScreen.reset();
            getKDS().doRefreshView();
        }

    }

    /**
     *  try to add an empty order to orders, just for testing!!!!
     *  The Indian bug!!!!
     * @param userID
     */
    private void testEmptyNewOrder(KDSUser.USER userID) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        //TimeDog t = new TimeDog();
        m_nTestCount++;
        String strOrderName = "Order #" + KDSUtil.convertIntToString(m_nTestCount);

        int nItems = m_randomItems.nextInt(5);

        nItems = Math.abs(m_randomItems.nextInt() % 5) +1;
        KDSDataOrder order = KDSDataOrder.createTestOrder(strOrderName, nItems, getKDS().getStationID(), userID.ordinal()); // rows = (i+2) * 6  +3 +titlerows;
        order.getItems().clear();

        //KDSDataOrder order = KDSDataOrder.createTestSmartOrder(strOrderName, nItems, getKDS().getStationID()); // rows = (i+2) * 6  +3 +titlerows;
        // KDSDataOrder order = KDSDataOrder.createTestPrepOrder(strOrderName, nItems, getKDS().getStationID()); // rows = (i+2) * 6  +3 +titlerows;
        //preparation, 20180104
        getKDS().getCurrentDB().prep_add_order_items(order);

        getKDS().getUsers().getUserA().getOrders().addComponent(order);

        //t.debug_print_Duration("opAddNewOrder2");
        getKDS().refreshView(KDSUser.USER.USER_A, KDS.RefreshViewParam.None);
        getKDS().refreshView(KDSUser.USER.USER_B, KDS.RefreshViewParam.None);
        //t.debug_print_Duration("opAddNewOrder3");
        int n = getKDS().getSettings().getInt(KDSSettings.ID.Sum_position);
        KDSSettings.SumPosition pos = KDSSettings.SumPosition.values()[n];

        this.getUserUI(KDSUser.USER.USER_A).refreshSum(KDSUser.USER.USER_A, pos);
        this.getUserUI(KDSUser.USER.USER_B).refreshSum(KDSUser.USER.USER_B, pos);
        // t.debug_print_Duration("opAddNewOrder4");
        //this.getSummaryFragment().refreshSummary();
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    /**
     * suspend the bumping when queue station is restoring data.
     * if my queue is online, and there are offline data, suspend bumping.
     * @return
     */
    public boolean suspendBumpWhenQueueRecovering()
    {
        if (getKDS().getStationsConnections().isMyQueueDisplayStationsExisted())
        {
            if (getKDS().getStationsConnections().isMyQueueOnline())
            {
                KDSStationIP station = getKDS().getStationsConnections().getMyQueueStation();
                return (getKDS().getStationsConnections().isThereOfflineData(station.getID()));

            }
        }
        return false;
    }

    //long m_nFlashTitleCounter = 0;
    public void checkMyAttachedStationsOffline()
    {
        ArrayList<KDSStationIP> ar = getKDS().getStationsConnections().getRelations().getAllAttachedStations();
        if (ar.size() > 0) {
            boolean anyOffline = false;
            ArrayList<String> arOffline = new ArrayList<>();//use it to show offline stations.
            for (int i = 0; i < ar.size(); i++) {
                KDSStationActived station = getKDS().getStationsConnections().findActivedStationByID(ar.get(i).getID());
                if (station == null) {
                    anyOffline = true;
                    arOffline.add(ar.get(i).getID());
                    //break;
                }
            }
            KDSViewFontFace ff = this.getSettings().getKDSViewFontFace(KDSSettings.ID.Screen_title_fontface);
            int bg = ff.getBG();
            int fg = ff.getFG();
            if (anyOffline) {
                //m_nFlashTitleCounter++;
                //if ((m_nFlashTitleCounter % 2) == 1) {
                if (KDSGlobalVariables.getBlinkingStep()) {
                    bg = ff.getFG();
                    fg = ff.getBG();
                }
            }
            String text = "";
            for (int i=0; i< arOffline.size(); i++)
            {
                text += "#" + arOffline.get(i);
                if (i < arOffline.size()-1)
                    text += ",";

            }
            if (arOffline.size()>0 ) {
                text += " " + getString(R.string.offline_warning);
                getTextView(R.id.txtTitle).setText(text);

            }
            else
            {
                updateTitle();
            }
            getTextView(R.id.txtTitle).setTextColor(fg);
            getTextView(R.id.txtTitle).setBackgroundColor(bg);
        }
        else
        {
            KDSViewFontFace ff = this.getSettings().getKDSViewFontFace(KDSSettings.ID.Screen_title_fontface);
            int bg = ff.getBG();
            int fg = ff.getFG();
            getTextView(R.id.txtTitle).setTextColor(fg);
            getTextView(R.id.txtTitle).setBackgroundColor(bg);
            //updateTitle();
        }

    }
    public void doDoublePressPanelNumberTransfer(KDSUser.USER user, KeyEvent ev) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        int nKeyCode = ev.getKeyCode();
        if (nKeyCode == KeyEvent.KEYCODE_0 ||
                nKeyCode == KeyEvent.KEYCODE_1 ||
                nKeyCode == KeyEvent.KEYCODE_2 ||
                nKeyCode == KeyEvent.KEYCODE_3 ||
                nKeyCode == KeyEvent.KEYCODE_4 ||
                nKeyCode == KeyEvent.KEYCODE_5 ||
                nKeyCode == KeyEvent.KEYCODE_6 ||
                nKeyCode == KeyEvent.KEYCODE_7 ||
                nKeyCode == KeyEvent.KEYCODE_8 ||
                nKeyCode == KeyEvent.KEYCODE_9) {
            if (!isUserLayoutReady(user)) return;
            int nPanel = nKeyCode - KeyEvent.KEYCODE_0;
            String orderGuid = getUserUI(user).getLayout().getPanelOrderGuid(nPanel);
            if (orderGuid.isEmpty()) return;
            setSelectedOrderGuid(user, orderGuid);
            opTransfer(user);

//            if (getSelectedOrderGuid(user).equals(orderGuid)) {
//                onBumpOrder(user);
//            } else {
//                KDSDataOrder bumpedOrder = bumpOrder(user, orderGuid, true);
//                if (bumpedOrder == null) return;
//                this.getSummaryFragment().refreshSummary();
//                refreshView();
//                printOrder(bumpedOrder);
//            }

        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void onSysTimeChanged()
    {

    }

    public void resetTimerAfterSystemTimeChanged()
    {
        m_doubleClickIntervalTimeout.reset();
        m_timer.stop();
        m_timer = new KDSTimer();
        m_timer.start(this, this, 1000);
        m_dtLastUpdateTime.setTime(System.currentTimeMillis());
    }

    Date m_dtLastUpdateTime = new Date();
    final int SYS_TIME_CHANGED= 2000;//2 seconds
    private void checkSystemTimeChanged()
    {
        long l = System.currentTimeMillis();
        l = l - m_dtLastUpdateTime.getTime();
        if (l <0) //changed backward
        {
            resetTimerAfterSystemTimeChanged();
        }
    }

    private void resetStationID()
    {
        KDSSettings.resetStationID();
        this.getSettings().set(KDSSettings.ID.KDS_ID, "");
    }

    /**
     * KPP1-219
     * Description
     *
     * 1. Setup Station 1 as Prep and Station 2 as Expo
     * 2. Set Station 2 as the Expo for Station 1 in Settings>Station Relationship
     * 3. On Station 2 (Expo), go to General Settings
     * 4. Change 'Station number' to 3
     *
     * AR: Prep station detects expo as offline
     * ER: Update the prep station with new expo ID
     * @param oldStationID
     * @param newStationID
     */
    private void changeRelationTableWithNewStationID(String oldStationID, String newStationID)
    {
        if (oldStationID.equals(newStationID)) return;

        ArrayList<KDSStationsRelation> ar = getKDS().getStationsConnections().getRelations().getRelationsSettings();
        boolean bChanged = false;
        for (int i=0; i< ar.size(); i++)
        {
            KDSStationsRelation r = ar.get(i);
            if (r.getID().equals(oldStationID)) {
                r.setID(newStationID);
                bChanged = true;
            }

            String expo = KDSStationsRelation.replaceStation(r.getExpStations(), oldStationID, newStationID);
            if (!r.getExpStations().equals(expo)) {
                bChanged = true;
                r.setExpStations(expo);
            }
            String slaves = KDSStationsRelation.replaceStation(r.getSlaveStations(), oldStationID, newStationID);
            if (!r.getSlaveStations().equals(slaves)) {
                r.setSlaveStations(slaves);
                bChanged = true;

            }
        }
        if (bChanged)
        {
            KDSSettings.saveStationsRelation(getContext(), ar);
            getKDS().getStationsConnections().getRelations().refreshRelations(getContext(), newStationID);
            KDS.broadcastStationsRelations();
            try {
                Thread.sleep(200);
            }
            catch (Exception e)
            {

            }
            //try again, make sure new settings were send to everyone.
            KDS.broadcastStationsRelations();

        }
    }

    /**
     * KPP1-200
     * It will change local relationship table according registered stations in backoffice.
     * If table changed, send it to all others.
     */
    private void checkRemovedStationsFromBackofficeAfterRegister()
    {
        ArrayList<KDSStationsRelation> ar = getKDS().getStationsConnections().getRelations().getRelationsSettings();
        boolean bChanged = false;
        ArrayList<KDSStationsRelation> arWillRemove = new ArrayList<>();

        for (int i=0; i< ar.size(); i++)
        {
            KDSStationsRelation r = ar.get(i);
            if (Activation.findStation(r.getID()))
                continue; //this station is existed
            arWillRemove.add(r);
        }
        if (arWillRemove.size() > 0)
        {
            bChanged = true;
            for (int i=0; i< arWillRemove.size(); i++) {
                KDSStationsRelation r = arWillRemove.get(i);
                ar.remove(r);

                for (int j=0; j< ar.size(); j++)
                {
                    KDSStationsRelation q = ar.get(j);
                    if (q.getID().equals(r.getID())) continue;
                    String expo = KDSStationsRelation.removeStation(q.getExpStations(), r.getID());
                    if (!q.getExpStations().equals(expo)) {
                        bChanged = true;
                        q.setExpStations(expo);
                    }
                    String slaves = KDSStationsRelation.removeStation(q.getSlaveStations(), r.getID());
                    if (!q.getSlaveStations().equals(slaves)) {
                        q.setSlaveStations(slaves);
                        bChanged = true;

                    }
                }

            }
        }
        if (bChanged)
        {
            KDSSettings.saveStationsRelation(getContext(), ar);
            getKDS().getStationsConnections().getRelations().refreshRelations(getContext(), getKDS().getStationID());
            KDS.broadcastStationsRelations();
            try {
                Thread.sleep(200);
            }
            catch (Exception e)
            {

            }
            //try again, make sure new settings were send to everyone.
            KDS.broadcastStationsRelations();

        }
    }

    public void aboutDlgCallActivation()
    {
        doActivation(false, true, "");
    }

    /**
     * The interface of kdsevent.
     * rev.:
     *  kpp1-382.
     * @param userID
     */
    public void onRefreshSummary(int userID){

        //kpp1-382
        if (userID <0) return;
        if (userID >= KDSUser.USER.values().length) return;
        KDSUser.USER user =  KDSUser.USER.values()[userID];
        if (isSummaryVisible(user))
            this.onRefreshSummary( user );
        //
    }

    public void onShowStationStateMessage(String stationID, int nState){}
    public void onShowMessage(String message){}

//    /**
//     * for kdsview context menu
//     * @param menu
//     * @param v
//     * @param menuInfo
//     */
//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//
//        super.onCreateContextMenu(menu, v, menuInfo);
//        int id =  v.getId();
//        KDSUser.USER user = KDSUser.USER.USER_A;
//        if (id == R.id.viewOrdersB)
//            user = KDSUser.USER.USER_B;
//        String focusedItem = getFocusedItemGUID(user);
//        if (focusedItem.isEmpty())
//            getMenuInflater().inflate(R.menu.order_menu,menu);
//        else
//            getMenuInflater().inflate(R.menu.item_menu,menu);
//
//
//
//    }

//    @Override
//    public boolean  onContextItemSelected(MenuItem item) {
//
//        switch(item.getItemId()){
//            case R.id.order_bump:
//                opBump(getFocusedUserID());
//            break;
//            case R.id.order_unbump:
//                opUnbump(getFocusedUserID());
//                break;
//            case R.id.order_unbump_last:
//                opUnbumpLast(getFocusedUserID());
//                break;
//            case R.id.order_park:
//                opPark(getFocusedUserID());
//                break;
//            case R.id.order_unpark:
//                opUnpark(getFocusedUserID());
//                break;
//            case R.id.order_print:
//                opPrint(getFocusedUserID());
//                break;
//            case R.id.order_transfer:
//                opTransfer(getFocusedUserID());
//                break;
//            case R.id.item_bump:
//                opBump(getFocusedUserID());
//                break;
//            case R.id.item_unbump:
//                opUnbump(getFocusedUserID());
//                break;
//
//        }
//
//        return super.onOptionsItemSelected(item);
//
//    }
    KDSContextMenu m_contextMenu = new KDSContextMenu();

    public void onViewLongPressed(KDSLayout layout)
    {
        if (getSettings().getStationFunc() == SettingsBase.StationFunc.Queue ||
                getSettings().getStationFunc() == SettingsBase.StationFunc.Queue_Expo ||
                getSettings().getStationFunc() == SettingsBase.StationFunc.TableTracker
        )
            return;

        KDSUser.USER user = getUserFromLayout(layout);
        if (getUserUI(user).getLayout().getView().getOrdersViewMode() != KDSView.OrdersViewMode.Normal)
            return;

        String focusedItem = getFocusedItemGUID(user);
        int nBG = getSettings().getKDSViewFontFace(KDSSettings.ID.Touch_fontface).getBG();
        int nFG = getSettings().getKDSViewFontFace(KDSSettings.ID.Touch_fontface).getFG();
        KDSContextMenu.ContextMenuType menuType = KDSContextMenu.ContextMenuType.Order;
        if (!focusedItem.isEmpty())
            menuType = KDSContextMenu.ContextMenuType.Item;


        m_contextMenu.showContextMenu(this, this, menuType, getSettings());

    }
    public void onContextMenuItemClicked(KDSContextMenu.ContextMenuItemID nItemID)
    {
        switch(nItemID){
            case order_bump:
                opBump(getFocusedUserID());
                break;
            case order_unbump:
                opUnbump(getFocusedUserID());
                break;
            case order_unbump_last:
                opUnbumpLast(getFocusedUserID());
                break;
            case order_park:
                opPark(getFocusedUserID());
                break;
            case order_unpark:
                opUnpark(getFocusedUserID());
                break;
            case order_print:
                opPrint(getFocusedUserID());
                break;
            case order_sum:
                opSummary(getFocusedUserID());
                break;
            case order_transfer:
                opTransfer(getFocusedUserID());
                break;
            case order_sort:
                opSort(getFocusedUserID());
                break;
            case order_more:
                opMore(getFocusedUserID());
                break;
            case order_page:
                opPageOrder(getFocusedUserID());
                break;
            case order_test:
                opTest(getFocusedUserID());
                break;
            case item_bump:
                opBump(getFocusedUserID());
                break;
            case item_unbump:
                opUnbump(getFocusedUserID());
                break;
            case item_transfer:
                opTransfer(getFocusedUserID());
                break;
            case item_buildcard:
                doMoreFunc_BuildCard(getFocusedUserID());
                break;
            case item_training:
                doMoreFunc_Training_Video(getFocusedUserID());
                break;

        }
    }

    public boolean onViewSlipUpDown(KDSLayout layout,MotionEvent e1, MotionEvent e2,  KDSView.SlipDirection slipDirection, KDSView.SlipInBorder slipInBorder)
    {

        //kpp1-377
        if (getSettings().getBoolean(KDSSettings.ID.Hide_station_title))
        {
            if (slipDirection == KDSView.SlipDirection.Bottom2Top)
            {
                SetTitleVisible(false);
            }
            else if (slipDirection == KDSView.SlipDirection.Top2Bottom)
            {
                SetTitleVisible(true);
            }
        }

        KDSUser.USER user = getUserFromLayout(layout);
        if (slipInBorder == KDSView.SlipInBorder.Top ) {
            int n = getKDS().getSettings().getInt(KDSSettings.ID.Sum_position);
            KDSSettings.SumPosition pos = KDSSettings.SumPosition.values()[n];
            if  (pos== KDSSettings.SumPosition.Top ) {
                showSummary(user,(slipDirection == KDSView.SlipDirection.Top2Bottom) );
                //this.getUserUI(user).showSum(user, pos, (slipDirection == KDSView.SlipDirection.Top2Bottom) );
                return true;
            }
        }
        if (slipInBorder == KDSView.SlipInBorder.Bottom)
            return showTouchButtonsBar( (slipDirection == KDSView.SlipDirection.Bottom2Top));

        if (slipInBorder == KDSView.SlipInBorder.None)
        {
            if (e1==null || layout.getView().findTouchPanel((int)e1.getX(),(int) e1.getY()) != null)
            {
                if (slipDirection == KDSView.SlipDirection.Bottom2Top)
                    showOrderZoom(getSelectedOrderGuid(user));
            }


        }
        return false;


    }

    private boolean showTouchButtonsBar(boolean bVisible)
    {
        if (!isKDSValid()) return false;
        if (getKDS().isSingleUserMode()) {
            m_uiUserA.showTouchPad(bVisible);
        }
        else {
            int n = KDSSettings.getEnumIndexValues(getSettings(), KDSSettings.ScreenOrientation.class, KDSSettings.ID.Screens_orientation);
            KDSSettings.ScreenOrientation orientation = KDSSettings.ScreenOrientation.values()[n];
            switch (orientation) {
                case Left_Right:
                    m_uiUserA.showTouchPad(bVisible);
                    m_uiUserB.showTouchPad(bVisible);
                    break;
                case Up_Down:
                    m_uiUserA.showTouchPad(bVisible);
                    m_uiUserB.showTouchPad(bVisible);
                    break;
            }

        }
        return  true;
    }

    public boolean onViewSlipping(KDSLayout layout,MotionEvent e1, MotionEvent e2, KDSView.SlipDirection slipDirection, KDSView.SlipInBorder slipInBorder)
    {
        switch (slipDirection)
        {

            case Left2Right:
            case Right2Left:
                return onViewSlipLeftRight(layout, e1, e2, slipDirection, slipInBorder);

            case Top2Bottom:
            case Bottom2Top:
                return onViewSlipUpDown(layout,e1, e2,  slipDirection, slipInBorder);

        }
        return false;
    }

    private void showSummary(KDSUser.USER userID, boolean bVisible) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        int n = getKDS().getSettings().getInt(KDSSettings.ID.Sum_position);
        KDSSettings.SumPosition pos = KDSSettings.SumPosition.values()[n];
        if (getKDS().getSettings().getBoolean(KDSSettings.ID.AdvSum_enabled) &&
                getKDS().getSettings().getBoolean(KDSSettings.ID.AdvSum_always_visible))
        {
            this.getUserUI(userID).showSum(userID, pos, true);
        }
        else
            this.getUserUI(userID).showSum(userID, pos, bVisible);
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    private void checkRelationshipBuild()
    {
        if (getKDS().getStationsConnections().getRelations().getRelationsSettings().size()==0)
        {
            showToastMessage(getString(R.string.build_relationship_first));
        }
    }

    private void showOrderZoom(String orderGuid)
    {
        if (orderGuid.isEmpty()) return; //kpp1-317
        KDSDataOrder order = getKDS().getUsers().getOrderByGUID(orderGuid);
        if (order == null) return ; //kpp1-317
        KDSDlgOrderZoom dlg = KDSDlgOrderZoom.instance();
        dlg.setReceiver(this);
        dlg.setUser(getFocusedUserID());

        dlg.showOrder(this, getKDS().getSettings(), order);
    }

    public boolean zoomViewItemOperations(KDSDlgOrderZoom.ZoomViewItemOperation operation,KDSUser.USER userID, String orderGuid, String itemGuid)
    {
        if (operation == KDSDlgOrderZoom.ZoomViewItemOperation.Bump)
        {
            itemBump(userID,  orderGuid, itemGuid);
            //refreshView(userID);
            return true;
        }
        else if (operation == KDSDlgOrderZoom.ZoomViewItemOperation.Unbump)
        {
            itemUnbump(userID, orderGuid, itemGuid);
            //refreshView(userID);
            return true;
        }
        return false;

    }

    /**
     * rev.:
     *  kpp1-343 allow expo bump itself items.
     * @param userID
     * @param orderGuid
     * @param itemGuid
     */
    private void itemBump(KDSUser.USER userID, String orderGuid, String itemGuid)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;

        //prevent queue stuck
        if (suspendBumpWhenQueueRecovering()) {
            getKDS().showToastMessage(getString(R.string.suspend_bump_while_queue_recover));
            return;
        }

        //String orderGuid = getSelectedOrderGuid(userID);//
        if (orderGuid.isEmpty()) return;

        KDSDataOrder order = getKDS().getUsers().getOrderByGUID(orderGuid);//KPP1-129, keep order here

        //String itemGuid = getSelectedItemGuid(userID);
        if (itemGuid.isEmpty()) return;

        if (!KDSStationFunc.itemBump(getKDS().getUsers().getUser(userID), orderGuid, itemGuid))
            return;

        onRefreshSummary(userID);
        //this.getSummaryFragment().refreshSummary();
        //notification
        notifyPOSItemBumpUnbump(userID, orderGuid, itemGuid);
        if (getKDS().isExpeditorStation())
        {
            if (getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid).isItemsAllBumpedInExp())
                getKDS().getSoundManager().playSound(KDSSettings.ID.Sound_expo_order_complete);
        }

        lineItemsFocusNextAfterBump(userID, orderGuid, itemGuid);
        refreshView(userID);

        getKDS().checkSMS(orderGuid, false); //2.1.10, fix KPP1-23
        //KDSDataOrder order = getKDS().getUsers().getOrderByGUID(orderGuid); //KPP1-129, move it to above
        if (order != null) {
            getKDS().checkBroadcastSMSStationStateChanged(orderGuid, "",order.isAllItemsFinished(), false);
        }
        //
        //https://bematech.atlassian.net/browse/KPP1-62
        if (order != null) { //if I continue bump order, show crash, KPP1-129
            KDSDataItem item = order.getItems().getItemByGUID(itemGuid);
            getKDS().syncItemBumpUnbumpToWebDatabase(order, item, true);
        }
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

    }

    public void itemUnbump(KDSUser.USER userID, String orderGuid, String itemGuid) {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        if (!isKDSValid()) return ;
        //String orderGuid = getSelectedOrderGuid(userID);// f.getLayout().getEnv().getStateValues().getFocusedOrderGUID();
        if (orderGuid.isEmpty()) return;

        //String itemGuid = getSelectedItemGuid(userID);

        unbumpItem(userID, orderGuid, itemGuid);

        getKDS().checkSMS(orderGuid, false); //2.1.10
        //https://bematech.atlassian.net/browse/KPP1-62
        KDSDataOrder order = getKDS().getUsers().getOrderByGUID(orderGuid);
        getKDS().syncItemBumpUnbumpToWebDatabase(order,order.getItems().getItemByGUID(itemGuid), false );
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");

    }

    /**
     * KPP1-290,Showing offline status of station
     * It will  check all offline stations.
     */
    public void checkOfflineStations()
    {
        ArrayList<KDSStationIP> ar = getKDS().getStationsConnections().getRelations().getAllValidStations();//.getAllAttachedStations();
        if (ar.size() > 0) {
            boolean anyOffline = false;
            ArrayList<String> arOffline = new ArrayList<>();//use it to show offline stations.
            for (int i = 0; i < ar.size(); i++) {
                KDSStationActived station = getKDS().getStationsConnections().findActivedStationByID(ar.get(i).getID());
                if (station == null) {
                    anyOffline = true;
                    arOffline.add(ar.get(i).getID());
                    //break;
                }
            }
            KDSViewFontFace ff = this.getSettings().getKDSViewFontFace(KDSSettings.ID.Screen_title_fontface);
            int bg = ff.getBG();
            int fg = ff.getFG();
            if (anyOffline) {
                //m_nFlashTitleCounter++;
                //if ((m_nFlashTitleCounter % 2) == 1) {
                if (KDSGlobalVariables.getBlinkingStep()) {
                    bg = ff.getFG();
                    fg = ff.getBG();
                }
            }
            String text = "";
            for (int i=0; i< arOffline.size(); i++)
            {
                text += "#" + arOffline.get(i);
                if (i < arOffline.size()-1)
                    text += ",";

            }
            if (arOffline.size()>0 ) {
                text += " " + getString(R.string.offline_warning);
                getTextView(R.id.txtTitle).setText(text);

            }
            else
            {
                updateTitle();
            }
            getTextView(R.id.txtTitle).setTextColor(fg);
            getTextView(R.id.txtTitle).setBackgroundColor(bg);
        }
        else
        {
            KDSViewFontFace ff = this.getSettings().getKDSViewFontFace(KDSSettings.ID.Screen_title_fontface);
            int bg = ff.getBG();
            int fg = ff.getFG();
            getTextView(R.id.txtTitle).setTextColor(fg);
            getTextView(R.id.txtTitle).setBackgroundColor(bg);
            //updateTitle();
        }

    }

    /**
     * kpp1-299
     */
    private void showConfirmLogoutDialog()
    {
        KDSUIDialogBase d = new KDSUIDialogBase();
        d.createOkCancelDialog(this,Confirm_Dialog.Logout, getString(R.string.yes), getString(R.string.no),getString(R.string.confirm), getString(R.string.confirm_logout),true, this );
        d.show();
    }

    /**
     * kpp1-299
     * rev.:
     *  kpp1-299-1, change it back. Ask clearing data when login.
     *
     */
    private void doLogout()
    {
        Activation.resetUserNamePwd();
        resetStationID();
        /* //kpp1-299-1
        setToDefaultSettings(); //kpp1-299 Station Relationship remembered
        Activation.resetOldLoginUser(); //kpp1-299
        getKDS().clearAll(); //clear database too.
        getKDS().clearStatisticData();
        */

        getKDS().clearRelationshipSettings(); //kpp1-299-1

        onDoActivationExplicit();
    }

    /**
     * kpp1-299
     * @return
     * default value: false;
     */
    public boolean isAppContainsOldData()
    {
        if (!this.getSettings().isDefaultSettings())
            return true;
        if (!this.getKDS().isDbEmpty())
            return true;
        return false;
    }

    /**
     *  kpp1-310
     * @param evt
     * @param arParams
     * @return
     */
    public Object onKDSEvent(KDSBase.KDSEventType evt, ArrayList<Object> arParams)
    {
        switch (evt)
        {
            case Received_rush_order:
            {
                /* arParams:
                    The orders added to users. Index 0: userA, index 1: userB order.
                 */
                getUserUI(KDSUser.USER.USER_A).getLayout().adjustFocusOrderLayoutFirstShowingOrder(true);
                if (arParams.size() >1)
                    getUserUI(KDSUser.USER.USER_B).getLayout().adjustFocusOrderLayoutFirstShowingOrder(true);


            }
            break;
            case TCP_listen_port_error: //kp1-312
            {
                String s = (String) arParams.get(0);
                showToastMessage(s, Toast.LENGTH_LONG);
            }
            break;
            case Order_Bumped_By_Other_Expo_Or_Station:
            {//check if it is the focused order. if so, focus next.

                String orderGuid = (String) arParams.get(0);
                if (getSelectedOrderGuid(KDSUser.USER.USER_A) == orderGuid)
                {
                    getUserUI(KDSUser.USER.USER_A).getLayout().focusNext();
                }
                if (getKDS().isMultpleUsersMode())
                {
                    if (getSelectedOrderGuid(KDSUser.USER.USER_B) == orderGuid)
                    {
                        getUserUI(KDSUser.USER.USER_B).getLayout().focusNext();
                    }
                }

            }
            break;
            default:
            {
                break;
            }

        }
        return null;
    }

    /**
     *
     *     *  see https://stackoverflow.com/questions/51956971/illegalstateexception-of-toast-view-on-android-p-preview
     *      *      *  It will show IllegalStateException of toast View on Android P
     * @param message
     * @param duration
     */

    public void showToastMessage(String message, int duration) {
        //KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter,s="+message);
        KDSBase.showToastMessage(message, duration);

//        if (m_toast == null)
//            m_toast = Toast.makeText(this, message, duration);
//        else
//            m_toast.setText(message);
//        m_toast.setDuration(duration);
//
//        m_toast.show();
        //KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    public void forceAgreementAgreed()
    {
        KDSUIDlgAgreement.forceAgreementAgreed(this, this);
//
//        //debug
//        KDSUIDlgAgreement.setAgreementAgreed(false);
//        //
//        if (KDSUIDlgAgreement.isAgreementAgreed())
//            return;
//
//        //KDSUIDlgAgreement dlg = new KDSUIDlgAgreement(this, this);
//        KDSUIDlgAgreement dlg =KDSUIDlgAgreement.instance(this, this);
//        dlg.show();
    }

    /**
     * while press [clean] key in bumpbar,
     * kds will lock screen. And start count down.
     */
    public void opCleanByBumpbar()
    {
        m_cleaning.onCleaningHabitsEvent(DlgCleaningAlarm.CleaningEventType.Alarm_Freeze_Screen_Now_By_BumpBar, null);
    }

    private boolean checkExpoCanBumpItem(KDSUser.USER userID,String orderGuid, String itemGuid)
    {

        if (!getKDS().isExpeditorStation())
            return true;
        if (!getSettings().getBoolean(KDSSettings.ID.Bumping_expo_confirmation))
            return true;

        KDSDataOrder order =  getKDS().getUsers().getUser(userID).getOrders().getOrderByGUID(orderGuid);
        if (order == null) return true;
        KDSDataItem item =  order.getItems().getItemByGUID(itemGuid);

        //if (!order.isAllItemsFinished())
        //one prep bumped item, and expo itself item can been bumped.
        if ( item.getToStations().findStation(getKDS().getStationID()) != KDSToStations.PrimarySlaveStation.Unknown)
            return true;
        //prep station item
        if (item.getBumpedStationsString().isEmpty())
              return false;
//        {
////            AlertDialog d = new AlertDialog.Builder(this)
////                    .setTitle(this.getString(R.string.message))
////                    .setMessage(this.getString(R.string.expo_cannot_bump_unless_prep_bump_all))
////                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
////                                @Override
////                                public void onClick(DialogInterface dialog, int which) {
////                                }
////                            }
////                    )
////
////                    .create();
////            d.show();
//            return true;
//        }
        return true;
    }
    public Object onActivationEvent(Activation.ActivationEvent evt, ArrayList<Object> arParams)
    {
        return null;
    }
    enum ExpoAutoFrom
    {
        MainThread,
        AutoBumpingThread,
    }

    Handler m_expoBumpingConfirmHandler = new Handler()
    {
        public void handleMessage(Message msg) {
            if (msg.what == ExpoAutoFrom.MainThread.ordinal())
                showExpoBumpingConfirmationDlg();
            else if (msg.what == ExpoAutoFrom.AutoBumpingThread.ordinal()) {
                String orderName = (String)msg.obj;
                showToastMessage("#"+orderName+":"+ KDSApplication.getContext().getString(R.string.expo_cannot_bump_unless_prep_bump_all), Toast.LENGTH_SHORT);
            }

        }
    };

    AlertDialog m_expoBumpConfirmDlg = null;
    private void showExpoBumpingConfirmationDlg()
    {
        if (m_expoBumpConfirmDlg != null) {
            return;
        }
        //AlertDialog d = new AlertDialog.Builder(this) //kpp1-380
        m_expoBumpConfirmDlg = new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.message))
                .setMessage(this.getString(R.string.expo_cannot_bump_unless_prep_bump_all))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                m_expoBumpConfirmDlg = null;
                            }
                        }
                )

                .create();
        m_expoBumpConfirmDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                m_expoBumpConfirmDlg = null;
            }
        });
        //d.show();//kpp1-380
        m_expoBumpConfirmDlg.show();
    }

    private void SetTitleVisible(boolean bShow)
    {
        View v = this.findViewById(R.id.layoutTitle);
        if (v == null) return;
        if (bShow) {
            if (v.getVisibility() != View.VISIBLE)
                v.setVisibility(View.VISIBLE);
//            v.getParent().requestLayout();
//            v.requestLayout();
//            v.forceLayout();
        }
        else
            v.setVisibility(View.GONE);

    }

    private void showBuildTypes()
    {
        TextView t = this.findViewById(R.id.txtBuildType);
        KDSUtil.showBuildTypes(this, t);
    }

}

