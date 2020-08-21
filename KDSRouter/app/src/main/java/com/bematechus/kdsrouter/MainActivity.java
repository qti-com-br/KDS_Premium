package com.bematechus.kdsrouter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bematechus.kdslib.Activation;
import com.bematechus.kdslib.ActivationRequest;
import com.bematechus.kdslib.ActivityLogin;
import com.bematechus.kdslib.DebugInfo;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSBase;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSLogOrderFile;
import com.bematechus.kdslib.KDSSMBDataSource;
import com.bematechus.kdslib.KDSSMBPath;
import com.bematechus.kdslib.KDSSmbFile;
import com.bematechus.kdslib.KDSSocketManager;
import com.bematechus.kdslib.KDSStationConnection;
import com.bematechus.kdslib.KDSTimer;
import com.bematechus.kdslib.KDSUIAboutDlg;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUIDlgAgreement;
import com.bematechus.kdslib.KDSUIDlgInputPassword;
import com.bematechus.kdslib.KDSUIIPSearchDialog;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.SettingsBase;
import com.bematechus.kdslib.TimeDog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener,
        KDSRouter.KDSEvents,
        KDSTimer.KDSTimerInterface,
        KDSUIDialogBase.KDSDialogBaseListener,
        Activation.ActivationEvents,
        KDSUIAboutDlg.AboutDlgEvent,
        KDSBackofficeNotification.BackofficeNotification_Event
        {

    final static private String TAG = "MainActivity";
    final static int SHOW_SCHEDULE = 1456;
    final static int SHOW_SETTINGS = 1;

    private KDSRouterService m_service = null;
    ServiceConnection m_serviceConn = null;

    TextView m_txtError = null;

    ListView m_lstInfo = null;
    InfoAdapter m_infoAdapter = null;
    KDSTimer m_timer = new KDSTimer();

    ImageView m_imgMenu = null;
    ImageView m_imgState= null;
    TextView m_txtTime = null;
    TextView m_txtDate = null;
    PowerManager.WakeLock m_wakeLock = null;
    TextView m_txtTitle = null;
    TextView m_txtRouterID = null;

    KDSKbdRecorder m_kbdRecorder = new KDSKbdRecorder();

    Activation m_activation = new Activation(this);
    KDSBackofficeNotification m_backofficeNotification = new KDSBackofficeNotification(this);


    public enum Confirm_Dialog {

        Clear_DB,
        Export_Data,
        Import_Data,
        Reset_TT_Authen,
        Restart_me,
        Logout,//kpp1-299

    }

    public void lockAndroidWakeMode(boolean bLock)
    {
        if (m_wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            m_wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "KDSRouter:MyWakeLock");
        }
        if (bLock) {
            m_wakeLock.acquire();
            this.getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else {

            m_wakeLock.release();
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public void onTime()
    {
        if (m_service != null)
            m_service.onTimer();

        updateTime();

        checkNetworkState();
        checkLogFilesDeleting();
        startCheckRemoteFolderNotificationThread();

        checkAutoActivation();

        connectBackofficeNotification();

    }
    SimpleDateFormat m_formatDate = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat m_formatTime = new SimpleDateFormat("HH:mm:ss");
    public void updateTime()
    {
        Date dt = new Date();
        String s = m_formatDate.format(dt);
        m_txtDate.setText(s);
        s = m_formatTime.format(dt);
        m_txtTime.setText(s);

    }

    private void checkNetworkState()
    {
        if (KDSSocketManager.isNetworkActived(this.getApplicationContext())) {
            m_imgState.setImageResource(com.bematechus.kdslib.R.drawable.online);
            if (m_imgState.getTag() != null &&
                    (int) m_imgState.getTag() == 0)

            m_imgState.setTag(1);
        }
        else{
            m_imgState.setImageResource(com.bematechus.kdslib.R.drawable.offline);
            if (m_imgState.getTag() == null ||
                    (int) m_imgState.getTag() == 1)
            m_imgState.setTag(0);
        }
    }


    final String LS8000_MAC_FLAG =  "000EC3";
    /**
     * check mac address, the LS8000 is "000ec3"
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

        String pre = strMac.substring(0, 6);
        return (pre.equals(LS8000_MAC_FLAG));
    }

    public void killMe()
    {
        this.finish();
    }
    public void showErrorMac()
    {
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.error))
                .setMessage(this.getString(R.string.error_match_mac))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        MainActivity.this.killMe();
                    }
                })
                .setCancelable(false)

                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        MainActivity.this.killMe();
                    }
                })
                .create();
        d.setCanceledOnTouchOutside(false);
        d.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, ">>>>>>Enter mainactivity oncreate");
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        m_activation.setEventsReceiver(this);
        Activation.setGlobalEventsReceiver(this); //for clear db after logout,KW-155

        if (!isMacMatch2()) {
            showErrorMac();
            return;
        }
        lockAndroidWakeMode(true);
        //kpp1-337
        //KDSSettings.Language language =  KDSSettings.loadLanguageOption(this.getApplicationContext());
        //KDSUtil.setLanguage(this.getApplicationContext(), language);

        setContentView(R.layout.activity_main);
        explicitStartService();
        bindKDSRouterService();

        m_txtError = (TextView)findViewById(R.id.txtError);
        m_imgMenu  = (ImageView)findViewById(R.id.imgMenu);
        m_imgState= (ImageView)this.findViewById(R.id.imgState);
        m_txtTime = (TextView)this.findViewById(R.id.txtTime);
        m_txtDate = (TextView)this.findViewById(R.id.txtDate);
        m_txtTitle = (TextView)this.findViewById(R.id.txtTitle);

        m_txtRouterID = (TextView)this.findViewById(R.id.txtRouterID);

        m_lstInfo = (ListView)findViewById(R.id.lstInfo);
        List<VisibleLogInfo> data = new ArrayList<>();
        data.add( new VisibleLogInfo( this.getString(R.string.main_title)) );//
        m_infoAdapter = new InfoAdapter(this, data);
        m_lstInfo.setAdapter(m_infoAdapter);

        m_lstInfo.setFocusable(false);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        pref.registerOnSharedPreferenceChangeListener(this);
        m_timer.start(this, this, 1000);

        updateTitle();


        KDSUIDlgAgreement.forceAgreementAgreed(this, this);

        connectBackofficeNotification();

    }


    android.os.Handler m_msgHandler = new android.os.Handler(new android.os.Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int n = (int)msg.obj;
            if (n == -3 || n == -2)
                showPermissionErrorAlert();
            return true;
        }
    });
    public void showPermissionErrorAlert()
    {
        KDSUIDialogBase dlg = new KDSUIDialogBase();
        dlg.createInformationDialog(this, KDSApplication.getContext().getString(R.string.error), KDSApplication.getContext().getString(R.string.error_folder_permission), true).show();

    }

    public void checkRemoteFolderPermission()
    {
        KDSRouterSettings.KDSDataSource source =KDSRouterSettings.KDSDataSource.values ()[getKDSRouter().getSettings().getInt(KDSRouterSettings.ID.KDSRouter_Data_Source)];
        if (source == KDSRouterSettings.KDSDataSource.Folder) {
            String remoteFolder = getKDSRouter().getSettings().getString(KDSRouterSettings.ID.KDSRouter_Data_Folder);
            if (remoteFolder.isEmpty()) return;
            int nError = KDSSmbFile.smb_checkFolderWritable(remoteFolder);
            if (nError != 0) {
                Message msg = new Message();
                msg.obj = nError;
                m_msgHandler.sendMessage(msg);
            }
        }
    }

    public void updateTitle()
    {
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
        boolean bEnabled = pre.getBoolean("general_router_enabled", false);
        boolean bBackupMode = pre.getBoolean("general_router_backup", false);
        String primaryRouterID = pre.getString("general_router_primary", "");

        if (bEnabled)
        {
            m_txtTitle.setTextColor(this.getResources().getColor(R.color.kds_title_fg));
            String title = getString(R.string.main_title);
            if (bBackupMode) {
                String str = getString(R.string.backup_of);
                str = str.replace("#", primaryRouterID);

                title += " " + str;
            }
            title += " " + getVersionName();

            if (KDSConst.ENABLE_FEATURE_ACTIVATION) { //2.0.48
                if (!m_activation.isActivationPassed()) {
                    title +=" " + getString(R.string.inactive);
                }
            }

            m_txtTitle.setText(title);

        }
        else
        {
            m_txtTitle.setTextColor(Color.RED);
            m_txtTitle.setText(getString(R.string.router_disabled));
        }

        String id = pre.getString("general_station_id", "");
        if (!Activation.getStoreName().isEmpty())
            id = Activation.getStoreName() + "-" + id;
        m_txtRouterID.setText(id);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    KDSRouter getKDSRouter()
    {
        if (m_service == null)
            return null;
        return m_service.getKDSRouter();
    }

    boolean m_bShowingSettingsActivity = false;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SHOW_SCHEDULE)
        {
            if  (getKDSRouter() != null)
                getKDSRouter().scheduleRefresh();
        }
        else if (requestCode == SHOW_SETTINGS)
        {
            m_bShowingSettingsActivity = false;

        }
        else if (requestCode == KDSConst.SHOW_LOGIN)
        {
            if (resultCode == ActivityLogin.Login_Result.Agreement_disagree.ordinal())
            {//kpp1-325
                this.setResult(0);
                this.finish();
            }
            //
        }

    }
    public boolean showSettingsDialog()
    {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        m_bShowingSettingsActivity = true;
        startActivityForResult(i, SHOW_SETTINGS);
        //startActivity(i);//SHOW_PREFERENCES);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {

            boolean bEnablePwd = this.getKDSRouter().getSettings().getBoolean(KDSSettings.ID.Settings_password_enabled);
            if (bEnablePwd)
            {
                KDSUIDlgInputPassword dlg = new KDSUIDlgInputPassword(this, this, false);
                dlg.show();
            }
            else
            {
                return showSettingsDialog();
            }
            return true;
        }
        else if (id == R.id.action_schedule)
        {
            Intent i = new Intent(MainActivity.this,ScheduleActivity.class);
            startActivityForResult(i, SHOW_SCHEDULE);//SHOW_PREFERENCES);
            return true;
        }
        else if (id == R.id.action_showip)
        {
            if (!isRouterEnabled()) {
                showAlertMessage(getString(R.string.error), getString(R.string.router_disabled_warning), false);

            }
            else {
                String ip = getKDSRouter().getLocalIpAddress();
                ip = "IP=" + ip;
                showToastMessage(ip);
            }
        }
        else if (id == R.id.action_stations)
        {
            if (!isRouterEnabled()) {
                showAlertMessage(getString(R.string.error), getString(R.string.router_disabled_warning), false);

            }
            else {
                if (m_service != null)
                    KDSGlobalVariables.setKDSRouter(m_service.getKDSRouter());
                KDSUIIPSearchDialog dlg = new KDSUIIPSearchDialog(this, KDSUIIPSearchDialog.IPSelectionMode.Zero, null, "");
                dlg.setKDSCallback(m_service.getKDSRouter());
                dlg.show();
            }

        }
        else if (id == R.id.action_about)
        {
            //KDSUIAboutDlg.showAbout(this, getVersionName() + "(" + KDSUtil.getVersionCodeString(this) + ")");//kpp1-179
            Drawable icon = this.getResources().getDrawable(R.mipmap.ic_launcher);
            String ver = getVersionName() + "(" + KDSUtil.getVersionCodeString(this) + ")";

            KDSUIAboutDlg.showAbout(this, ver, KDSConst.APP_NAME_KDS, icon, this);//kpp1-179
        }
        else if (id == R.id.action_clear_log)
        {
            clearLog();
        }
        else if (id == R.id.action_logout) //add this new. KPP1-185
        {
            showConfirmLogoutDialog();

//            Activation.resetUserNamePwd();
//            setToDefaultSettings(); //kpp1-299 Station Relationship remembered
//            onDoActivationExplicit();
        }
        return super.onOptionsItemSelected(item);
    }

    private String getVersionName()
    {
        String appVersion="";
        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            appVersion = info.versionName; //version name, set it in build.gradle file.
            //or [App properties(right click)]-->[open module settings]-->app-->flavors-->version name
        } catch (Exception e) {

            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        return appVersion;
    }

    Toast m_toast = null;
    public void showToastMessage(String message) {
        int duration = Toast.LENGTH_SHORT;
        if (m_toast == null)
            m_toast = Toast.makeText(this, message, duration);
        else
            m_toast.setText(message);
        m_toast.show();
    }
    public boolean isRouterEnabled()
    {
        SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        boolean b = sf.getBoolean("general_router_enabled", false);
        return b;


    }

    private void explicitStartService()
    {
        Intent intent = new Intent(this, KDSRouterService.class);
        startService(intent);
    }
    private void explicitStopService()
    {
        Intent intent = new Intent(this, KDSRouterService.class);
        stopService(intent);
    }

    public void onBtnLogoClicked(View v)
    {
        showPopupMenu(v);
    }
    private boolean m_bShowingMenu = false;
    private void showPopupMenu(View v)
    {
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

                    if (event.getAction() == KeyEvent.ACTION_DOWN)
                    {
                        if (event.getRepeatCount()==0)
                            KDSKbdRecorder.convertKeyEvent(keyCode, event);

                    }

                    return false;
                }
            });

        } catch (Exception err) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),err);// + err.toString());
            //KDSLog.e(TAG, KDSUtil.error( err));
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //Log.i("key pressed", String.valueOf(event.getKeyCode()));
        KDSLog.d(TAG, "dispatchKeyEvent event=" + event);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // Log.d(TAG, "dispatchKeyEvent down=" + event.getKeyCode() );
            if (event.getRepeatCount() == 0)
                m_kbdRecorder.onKeyDown(event.getKeyCode());
            return super.dispatchKeyEvent(event);
        }
        else if (event.getAction() == KeyEvent.ACTION_UP) {

            //Log.d(TAG, "dispatchKeyEvent up=" + event.getKeyCode() );
            boolean bevent = keyPressed(event.getKeyCode(), event);
            m_kbdRecorder.onKeyUp(event.getKeyCode());
            if (bevent) {
                m_kbdRecorder.clear();
                return true;
            }
            else
                return super.dispatchKeyEvent(event);

        }
        else
            return super.dispatchKeyEvent(event);

    }

    private boolean keyPressed(int keyCode, KeyEvent event)
    {

        if (!m_kbdRecorder.isAnyKeyDown()){
            return false;
        }

        KDSLog.d(TAG, "KeyPressed="+KDSUtil.convertIntToString(keyCode));
        m_kbdRecorder.debug("keyPressed");

        if (m_kbdRecorder.isDown(KeyEvent.KEYCODE_ENTER) &&
                (m_kbdRecorder.isDown(KeyEvent.KEYCODE_CTRL_LEFT) ||
                 m_kbdRecorder.isDown(KeyEvent.KEYCODE_CTRL_RIGHT)) )
        {
            showPopupMenu(m_imgMenu);
            return true;
        }


        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            showPopupMenu(m_imgMenu);
            return true;
        }

        return false;
    }
    public void onBindServiceFinished()
    {
        if (m_service == null) return;

        KDSGlobalVariables.setKDSRouter(m_service.getKDSRouter());
        m_service.getKDSRouter().setEventReceiver(this);

        if (!SettingsBase.isNoCheckRelationWhenAppStart(this)) {
            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    getKDSRouter().broadcastRequireRelationsCommand();
                    checkRemoteFolderPermission();//move this to thread
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        if (KDSConst.ENABLE_FEATURE_ACTIVATION) {
            boolean bSilent = Activation.hasDoRegister();//2.1.2
            doActivation(bSilent, false, "");
        }
    }

    private void bindKDSRouterService()
    {

        m_serviceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                m_service = ((KDSRouterService.MyBinder)service).getService();
                MainActivity.this.onBindServiceFinished();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                m_service = null;
            }
        };

        Intent intent = new Intent(this, KDSRouterService.class);
        bindService(intent, m_serviceConn, Context.BIND_AUTO_CREATE);
    }
    /**
     * interface implements
     * @param prefs
     * @param key
     */
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
    {
        if (key.equals("StationsRelation" ))
        {
            //check stations changes
            m_service.checkStationsSettingChanged();//(this.getApplicationContext()();
        }
        else if (key.equals("kds_general_language"))
        {
            return;
        }
        else {

            if (key.equals("isDirtyPrefs")) return;
            if (m_service != null)
                m_service.updateSettings();
            updateTitle();
            connectBackofficeNotification();
        }


    }

    /**
     *
     * @param strError
     */
    public void showError(String strError)
    {
        if (m_txtError != null)
            m_txtError.setText(strError);
    }

    private String getCurrentTimeForLog()
    {

        Calendar c = Calendar.getInstance();
        String s = String.format("%02d-%02d %02d:%02d:%02d.%03d",
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND),
                c.get(Calendar.MILLISECOND));
        return s;
    }

    private void checkLogCount()
    {
        if (m_infoAdapter.m_listData.size() > KDSConst.MAX_INFORMATION_COUNT)
        {
            int ncount =m_infoAdapter.m_listData.size() - KDSConst.MAX_INFORMATION_COUNT;
            for (int i=0; i< ncount; i++)
            {
                m_infoAdapter.m_listData.remove(0);
            }

        }
    }
    static final long LOG_INTERVAL = 10*60*1000; //10 mins
    private boolean isExistedLogInfo(String s)
    {
        checkLogCount();

//        Date dtNow = new Date();
//        for (int i=0; i< m_infoAdapter.m_listData.size(); i++)
//        {
//            VisibleLogInfo info = m_infoAdapter.m_listData.get(i);
//            if (dtNow.getTime() - info.getTime() > LOG_INTERVAL)
//                continue;
//            if (info.getInfo().equals(s))
//                return true;
//        }

        VisibleLogInfo lastInfo = new VisibleLogInfo();
        if (m_infoAdapter.m_listData.size() >0) {
            lastInfo = m_infoAdapter.m_listData.get(m_infoAdapter.m_listData.size()-1);

        }

        VisibleLogInfo info = new VisibleLogInfo(s);
        if (info.isEqual(lastInfo))
            return true;

        return false;
    }
    public  boolean showInfo(String s)
    {
//        VisibleLogInfo lastInfo = new VisibleLogInfo();
//        //don't show too many informations, memory lost
//        if (m_infoAdapter.m_listData.size() > KDSConst.MAX_INFORMATION_COUNT)
//        {
//            int ncount =m_infoAdapter.m_listData.size() - KDSConst.MAX_INFORMATION_COUNT;
//            for (int i=0; i< ncount; i++)
//            {
//                m_infoAdapter.m_listData.remove(0);
//            }
//
//        }
//
//        if (m_infoAdapter.m_listData.size() >0) {
//            lastInfo = m_infoAdapter.m_listData.get(m_infoAdapter.m_listData.size()-1);
//
//        }

//        VisibleLogInfo info = new VisibleLogInfo(s);
//        if (info.isEqual(lastInfo))
//            return false;
//        String tm = getCurrentTimeForLog();
//        if (lastInfo.length() > tm.length()+2)
//             lastInfo = lastInfo.substring(tm.length()+2);
//        lastInfo = lastInfo.trim();
//        if (lastInfo.equals(s))
//            return false;
//        s = tm + ": " + s;
        if (isExistedLogInfo(s)) return false;
        VisibleLogInfo info = new VisibleLogInfo(s);
        m_infoAdapter.m_listData.add(info);
        m_infoAdapter.notifyDataSetChanged();
        m_lstInfo.setSelection(m_infoAdapter.getCount());
        return true;

    }

    public void onStationConnected(String ip, KDSStationConnection conn)
    {
        String s = this.getString(R.string.station_connected);

        if (conn != null)
            s = s.replace("#",conn.toString() );

        else
            s = s.replace("#",ip );

    }
    public void onStationDisconnected(String ip)
    {
        String s = this.getString(R.string.station_disconnected);
        s = s.replace("#", ip);

        KDSLog.d(TAG,new DebugInfo() + s);
        showInfo(s);
    }
    public void onAcceptIP(String ip)
    {
        String s = this.getString(R.string.station_disconnected);
        s = s.replace("#", ip);
        showInfo(s);
    }

    public void onRetrieveNewConfigFromOtherStation()
    {

    }

    public void onReceiveNewRelations()
    {

    }

    public void showAlertMessage(String title, String msg, boolean bAutoClose)
    {

        KDSUIDialogBase dlg = new KDSUIDialogBase();
        dlg.createInformationDialog(this,title,msg, false  );
        dlg.show();
        if (bAutoClose)
            dlg.setAutoCloseTimeout(KDSConst.DIALOG_AUTO_CLOSE_TIMEOUT);

    }
    public void onReceiveRelationsDifferent()
    {
        showAlertMessage(this.getString(R.string.error),this.getString(R.string.error_different_relations) , true);
    }
    public void onShowMessage(String message)
    {
        if (message.equals( KDSSMBDataSource.PATH_OK))
        {
            if (m_txtError != null)
                m_txtError.setText("");

        }
        else if (message.equals(KDSSMBDataSource.PATH_LOST))
        {
            if (m_txtError != null) {
                String s = this.getString(R.string.smb_folder_lost);
                String folder = this.getKDSRouter().getSettings().getString(KDSRouterSettings.ID.KDSRouter_Data_Folder);
                KDSSMBPath path = KDSSMBPath.parseString(folder);
                folder = path.toDisplayString();
                s = s.replace("#", folder);
                m_txtError.setText(s);
            }

        }
        else if (message.equals(KDSSMBDataSource.PATH_PERMISSION))
        {
            if (m_txtError != null) {
                String s = this.getString(R.string.error_folder_permission);

                m_txtError.setText(s);
                showToastMessage(s);
            }

        }
        else if (message.equals(KDSRouter.ROUTER_UNIQUE))
        {
            if (m_bShowingSettingsActivity) return;
            if (getKDSRouter().isEnabled())
                showOtherRouterEnabledError();
        }

        else
            this.showInfo(message);
    }

    public void onShowStationStateMessage(String stationID, int nState)
    {

        for (int i= m_infoAdapter.m_listData.size()-1; i>=0; i--)
        {
            VisibleLogInfo info = m_infoAdapter.m_listData.get(i);

            if (info.m_stationID.equals(stationID))
            {
                if (nState == info.m_nStationState)
                    return; //don't add it.
                else
                   break;
            }

        }
        checkLogCount();

        String s = "";
        if (nState == 0)
        {
            s =  this.getString(R.string.station_lost);// m_context.getString(R.string.station_lost);// "Station #" + stationID + " restored";
            s = s.replace("#", "#" + stationID);
        }
        else
        {
            s = this.getString(R.string.station_restored);// "Station #" + stationID + " restored";
            s = s.replace("#", "#" + stationID);
        }
        VisibleLogInfo info = new VisibleLogInfo(s);
        info.m_stationID = stationID;
        info.m_nStationState = nState;
        m_infoAdapter.m_listData.add(info);
        m_infoAdapter.notifyDataSetChanged();
    }
    public void  showOtherRouterEnabledError()
    {
        try {
            String strError = KDSApplication.getContext().getString(R.string.error_other_router_enabled);

            String strOK = this.getString(R.string.ok);
            AlertDialog d = new AlertDialog.Builder(this)
                    .setTitle(KDSApplication.getContext().getString(R.string.error))
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
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
        }
    }

    public void onAskOrderState(Object objSource, String orderName)
    {

    }

    public void onBtnClearClicked(View v)
    {

    }

    private void clearLog()
    {
        m_infoAdapter.m_listData.clear();
        m_infoAdapter.notifyDataSetChanged(); //20160105
    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {
        if (dialog instanceof KDSUIDlgAgreement)
        {//kpp1-325
            KDSUIDlgAgreement.setAgreementAgreed(false);
            this.finish();
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

        if (dlg instanceof KDSUIDlgInputPassword) {
            String pwd = (String) dlg.getResult();

            String settingsPwd = getKDSRouter().getSettings().getString(KDSSettings.ID.Settings_password);
            if (settingsPwd.isEmpty())
                settingsPwd = KDSConst.DEFAULT_PASSWORD;// "123";
            if (pwd.isEmpty() || (!pwd.equals(settingsPwd))) {
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
        else if (dlg instanceof KDSUIDlgAgreement)
        {//kpp1-325
            KDSUIDlgAgreement.setAgreementAgreed(true);
        }
        else if (dlg instanceof  KDSUIDialogBase)
        {
            if (dlg.getTag() == null) return;
            Confirm_Dialog confirm = (Confirm_Dialog) dlg.getTag();
            switch (confirm) {
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

    public boolean isKDSValid()
    {
        if (m_service == null ) return false;
        return (getKDSRouter() != null);
    }
    private KDSRouterSettings getSettings() {
        if (!isKDSValid()) return new KDSRouterSettings(this);
        return getKDSRouter().getSettings();
        //return this.getLayout().getEnv().getSettings();
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

    public void onActivationSuccess()
    {
        //Toast.makeText(this, "Activation is done", Toast.LENGTH_LONG).show();
        updateTitle();
        m_backofficeNotification.updateStoreGuidToBackOfficeAfterLogin(); //
    }
    public void onActivationFail(ActivationRequest.COMMAND stage, ActivationRequest.ErrorType errType, String failMessage)
    {
        // Toast.makeText(this, "Activation failed: " +stage.toString()+" - " + failMessage, Toast.LENGTH_LONG).show();
//        if (ActivationRequest.needResetUsernamePassword(errType))
//            m_activation.resetUserNamePassword();

        checkActivationResult(stage, errType);
        updateTitle();
    }
    public void onSMSSendSuccess(String orderGuid, int smsState)
    {

    }

    public void onSyncWebReturnResult(ActivationRequest.COMMAND stage, String orderGuid, Activation.SyncDataResult result)
    {

    }
    public void onDoActivationExplicit()
    {
        doActivation(false, true, "");
    }

    public void onForceClearDataBeforeLogin()
    { //kw-155
        this.getKDSRouter().clearAll();
        this.getKDSRouter().getRouterDB().clear();
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        this.getSettings().setToDefault();

        onSharedPreferenceChanged(pre, "");

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


    private void checkAutoActivation()
    {
        if (!KDSConst.ENABLE_FEATURE_ACTIVATION)
            return;
        if (m_activationDog.is_timeout(Activation.HOUR_MS))// * Activation.ACTIVATION_TIMEOUT_HOURS))
        //if (m_activationDog.is_timeout(5000))// * Activation.ACTIVATION_TIMEOUT_HOURS))
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
            showToastMessage(getString(R.string.internal_doing_activation));//"Internal activation is in process, please logout again later.");
            return; //kpp1-304, maybe this cause kds can not logout issue.
        }
        if (!isKDSValid()) return;
        m_activation.setStationID(getKDSRouter().getStationID());
        m_activation.setStationFunc(Activation.KDSROUTER);
        ArrayList<String> ar = KDSSocketManager.getLocalAllMac();
        if (ar.size()<=0) {
            showToastMessage(getString(R.string.no_network_detected));//"No network interface detected");
            return;//kpp1-304, maybe this cause kds can not logout issue.
        }
        m_activation.setMacAddress(ar.get(0));
        //  m_activation.setMacAddress("BEMA0000011");//test
        m_activation.startActivation(bSilent,bForceShowNamePwdDlg, this, showErrorMessage);
    }

    /**
     * KPP1-299,Station Relationship remembered
     * Clear everything when logout
     */
    public void setToDefaultSettings()
    {
        this.getSettings().setToDefault();
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    public final class ViewHolder {
        public TextView m_txtInfo;

    }

    public class InfoAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        List<VisibleLogInfo> m_listData;

        public InfoAdapter(Context context, List<VisibleLogInfo> data) {
            this.mInflater = LayoutInflater.from(context);
            m_listData = data;
        }
        public int getCount() {

            return m_listData.size();
        }
        public Object getItem(int arg0) {

            return m_listData.get(arg0);
        }
        public long getItemId(int arg0) {

            return arg0;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.information_listview, null);
                holder.m_txtInfo = (TextView) convertView.findViewById(R.id.txtInfo);


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.m_txtInfo.setText((String) m_listData.get(position).toString());

            return convertView;
        }

    }
    TimeDog m_notificationDog = new TimeDog();

    Thread m_threadCheckingNotification = null;
    final int NOTIFICATION_INTERVAL = 60000;// 1800000;
    /**
     * Move some timer functions to here.
     * Just release main UI.
     * All feature in this thread are no ui drawing request.
     * And, in checkautobumping function, it use message to refresh UI.
     */
    public void startCheckRemoteFolderNotificationThread()
    {
        if (m_threadCheckingNotification == null ||
                !m_threadCheckingNotification.isAlive())
        {
            m_threadCheckingNotification = new Thread(new Runnable() {
                @Override
                public void run() {
                    //while (getKDSRouter().isThreadRunning())
                    while (true)
                    {
                        if (getKDSRouter() == null) {
                            try {
                                Thread.sleep(10000);
                            } catch (Exception e) { }
                            continue;

                        }
                        if (!getKDSRouter().isThreadRunning())
                            break;
                        try {
                            if (!m_notificationDog.is_timeout(NOTIFICATION_INTERVAL)) //30 minutes
                            {
                                try {
                                    Thread.sleep(10000);
                                } catch (Exception e) { }
                                continue;
                            }
                            m_notificationDog.reset();
                            getKDSRouter().removeNotifications();


                        }
                        catch ( Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            });
            m_threadCheckingNotification.setName("RemoveNotification");
            m_threadCheckingNotification.start();
        }
    }


    /**
     * 2.0.12
     *
     */
    public class VisibleLogInfo
    {
        Date m_dtLog= new Date();
        String m_strInfo = "";
        public String m_stationID = "";
        public int m_nStationState = 0; //0: lost, 1: restore.

        public VisibleLogInfo()
        {

        }
        public VisibleLogInfo(String info)
        {
            m_strInfo = info;
        }
        public VisibleLogInfo(Date dt, String info)
        {
            m_dtLog = dt;
            m_strInfo = info;

        }
        public String getInfo()
        {
            return m_strInfo;
        }
        public long getTime()
        {
            return m_dtLog.getTime();
        }
        public String toString()
        {
            return getTimeForLog() + ":" + m_strInfo;
        }

        private String getTimeForLog()
        {

            Calendar c = Calendar.getInstance();
            c.setTime(m_dtLog);
            String s = String.format("%02d-%02d %02d:%02d:%02d.%03d",
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH),
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    c.get(Calendar.SECOND),
                    c.get(Calendar.MILLISECOND));
            return s;
        }

        public boolean isEqual(VisibleLogInfo info)
        {
            return (m_strInfo.equals(info.getInfo()));
        }
    }

    public void aboutDlgCallActivation()
    {
        doActivation(false, true, "");
    }

    // KDSRouter does not used KDSEvents function
    public void onRefreshSummary(int nUserID){}
    public void onShowMessage(KDSBase.MessageType msgType, String message){}
    public void onRefreshView(int nUserID, KDSDataOrders orders, KDSBase.RefreshViewParam nParam){}
    public void onSetFocusToOrder(String orderGuid){}

    /**
     * KPP1-305.Remove license restriction from Router
     * While network restored, check activation again.
     *  Use this function to get network restored event,
     *  I don't want to add new event function in router app.
     * @param orderName
     */
    public void onTTBumpOrder(String orderName){
        doActivation(true, false, "");
    }

    public void onXmlCommandBumpOrder(String orderGuid){}

    /**
     * check if the settings changed.
     * @return
     */
    public boolean isAppContainsOldData()
    {
        if (!this.getSettings().isDefaultSettings())
            return true;
        if (!this.getKDSRouter().isDbEmpty())
            return true;
        return false;

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
     */
    private void doLogout()
    {
        Activation.resetUserNamePwd();
        setToDefaultSettings(); //kpp1-299 Station Relationship remembered
        Activation.resetOldLoginUser(); //kpp1-299
        getKDSRouter().clearAll(); //clear database too.

        onDoActivationExplicit();
    }

            /**
             * In kpp1-312, use it to show tcp/ip port occupied error.
             * @param evt
             * @param arParams
             * @return
             */
    public Object onKDSEvent(KDSBase.KDSEventType evt, ArrayList<Object> arParams)
    {
        switch (evt)
        {
            case Received_rush_order:
                break;
            case TCP_listen_port_error:
            {
                String s = (String) arParams.get(0);
                showToastMessage(s);
            }
            break;
        }
        return null;
    }

    /**
     * Send download orders http request to backoffice
     */
    private void downloadBackofficeOrders()
    {
        if (!getSettings().getBoolean(KDSRouterSettings.ID.Enable_3rd_party_order))
            return;
        long l = KDSRouterSettings.loadFCMTime(this);
        //debug firebase
//
//        Calendar c = Calendar.getInstance();
//        c.set(Calendar.HOUR_OF_DAY,5 );
//        c.set(Calendar.MINUTE, 0);
//        c.set(Calendar.SECOND,0);
//
//        Date dt = c.getTime();//new Date(2020, 3, 10, 0,0,0);
//        l = dt.getTime();

        m_activation.postGetOrdersRequest(l);

        //test firebase
        if (KDSBackofficeNotification.ENABLE_DEBUG) {
            String s = KDSBackofficeNotification.getFCMTestString2();
            ArrayList<Object> ar = new ArrayList<>();
            ar.add(s);
            onActivationEvent(Activation.ActivationEvent.Get_orders, ar);
        }

    }

    /**
     * Backoffice return FCM orders!
     * @param evt
     *
     * @param arParams
     *  Get_orders: 0: the string JSON data.
     * @return
     */
    public Object onActivationEvent(Activation.ActivationEvent evt, ArrayList<Object> arParams)
    {
        if (evt == Activation.ActivationEvent.Get_orders)
        {
            if (arParams.size() >0)
                receiveBackofficeOrders((String)arParams.get(0));
        }
        return null;
    }

    /**
     *  I get the orders JSON string.
     * @param strData
     */
    private void receiveBackofficeOrders(String strData)
    {
        KDSDataOrders orders =  KDSBackofficeNotification.parseFirebaseJson(strData);
        if (orders == null) return;
        getKDSRouter().onFCMReceivedOrders(orders);
        KDSRouterSettings.saveFCMTime(this, System.currentTimeMillis());
    }

    private void connectBackofficeNotification()
    {
        if (!getSettings().getBoolean(KDSRouterSettings.ID.Enable_3rd_party_order)) {
            m_backofficeNotification.close();
            return;
        }
        m_backofficeNotification.connectBackOffice();
    }

    public void onBackofficeNotifyEvent(String evt)
    {
        downloadBackofficeOrders();
    }
}

