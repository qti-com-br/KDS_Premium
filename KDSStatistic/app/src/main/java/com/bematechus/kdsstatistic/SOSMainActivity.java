package com.bematechus.kdsstatistic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSSocketManager;
import com.bematechus.kdslib.KDSStationConnection;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSTimer;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.SOSReportOneStation;
import com.bematechus.kdslib.SettingsBase;
import com.bematechus.kdslib.TimeDog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SOSMainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener, SOSKDSSOS.KDSSOSEvents, KDSTimer.KDSTimerInterface, SOSKDSSOS.StationAnnounceEvents, SOSMainActivityMessageHandler.OnMainActivityMessage {

    final static private String TAG = "MainActivity";

    enum Report_Type
    {
        Order,
        Item,
    }
    private SOSService m_service = null;
    ServiceConnection m_serviceConn = null;

    //TextView m_txtError = null;

    ListView m_lstStations = null;
    //AdapterStation m_itemAdapterStation = null;
    KDSTimer m_timer = new KDSTimer();

    //title
    ImageView m_imgMenu = null;
    ImageView m_imgState= null;
    TextView m_txtTime = null;
    TextView m_txtDate = null;
    PowerManager.WakeLock m_wakeLock = null;
    TextView m_txtTitle = null;
//    KDSKbdRecorder m_kbdRecorder = new KDSKbdRecorder();

    //ListView m_lstFunc = null;
    SOSMainActivityMessageHandler m_msgHandler = new SOSMainActivityMessageHandler(this);

    ProgressDialog m_progressDlg = null;

    SOSReportShowing m_reportShowing = new SOSReportShowing();
    SOSLinearLayout m_sosLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!isMacMatch()) {
            showErrorMac();
            return;
        }
        lockAndroidWakeMode(true);

        SettingsBase.Language language =  SOSSettings.loadLanguageOption(this.getApplicationContext());
        KDSUtil.setLanguage(this.getApplicationContext(), language);


        setContentView(R.layout.sos_activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        //if (isRouterEnabled())
        explicitStartService();
        bindKDSSOSService();

        //m_txtError = (TextView)findViewById(R.id.txtError);
        m_imgMenu  = (ImageView)findViewById(R.id.imgMenu);
        m_imgState= (ImageView)this.findViewById(R.id.imgState);
        m_txtTime = (TextView)this.findViewById(R.id.txtTime);
        m_txtDate = (TextView)this.findViewById(R.id.txtDate);
        m_txtTitle = (TextView)this.findViewById(R.id.txtTitle);

        m_lstStations = (ListView)findViewById(R.id.lstStations);
        //List<String> data = new ArrayList<String>();
        //data.add(this.getString(R.string.main_title));//
        m_lstStations.setAdapter(new SOSAdapterStation(this.getApplicationContext(), new ArrayList<SOSKDSStationSOSInfo>()));
        //m_itemAdapterStation = new AdapterStation(this, data);
        //m_lstInfo.setAdapter(m_infoAdapter);
        //m_lstInfo.setItemsCanFocus(false);
        m_lstStations.setFocusable(false);

        m_sosLayout = (SOSLinearLayout)findViewById(R.id.sosLayout);

       // m_reportShowing.setParentLayout( (LinearLayout) findViewById(R.id.layoutData));
        m_reportShowing.setParentLayout(m_sosLayout);



        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        pref.registerOnSharedPreferenceChangeListener(this);
        m_timer.start(this, this, 1000);

        updateTitle();


        View t = this.findViewById(R.id.toggleActiveStations);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleActiveStationsView();
            }
        });
        toggleActiveStationsView(); //hide it

        //init_report_gui();
//        String strLayout =  pref.getString(KDSSOSSettings.SOS_VIEW_LAYOUT_KEY, "");
//        m_sosLayout.setLayoutMode(SOSLinearLayout.LayoutMode.Running);
//        m_sosLayout.parseString(strLayout, this);




    }

    protected void onDestroy() {
        super.onDestroy();
        explicitStopService();
    }

//    public void doReport(Report_Type r)
//    {
//        switch (r)
//        {
//
//            case Order:
//            {
////                Intent intent = new Intent(MainActivity.this, ActivityOrderReport.class);
////
////                startActivityForResult(intent,r.ordinal());
//            }
//            break;
//            case Item:
////                Intent intent = new Intent(MainActivity.this, ActivityItemReport.class);
////
////                startActivityForResult(intent,r.ordinal());
//                break;
//            default:
//                break;
//        }
//    }
    private void explicitStartService()
    {
        Intent intent = new Intent(this, SOSService.class);
        startService(intent);
    }
    private void explicitStopService()
    {
        Intent intent = new Intent(this, SOSService.class);
        stopService(intent);
    }
    public void onBindServiceFinished()
    {
        if (m_service == null) return;

        SOSKDSGlobalVariables.setKDSSOS(m_service.getKDSSOS());
        m_service.getKDSSOS().setEventReceiver(this);

        m_service.getKDSSOS().setStationAnnounceEventsReceiver(this);



        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                getKDSSOS().broadcastRequireStationsUDPInThread();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //m_service.getKDSSOS().removeOldData();
        m_clearDbTimeDog.reset();
        m_reportShowing.updateSettings(getKDSSOS().getSettings());

        init_report_gui();

        //m_reportShowing.initViews();
        test();
    }

    SOSKDSSOS getKDSSOS()
    {
        return m_service.getKDSSOS();
    }

    private void bindKDSSOSService()
    {
        m_serviceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                m_service = ((SOSService.MyBinder)service).getService();
                SOSMainActivity.this.onBindServiceFinished();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                m_service = null;
            }
        };

        Intent intent = new Intent(this, SOSService.class);
        bindService(intent, m_serviceConn, Context.BIND_AUTO_CREATE);
    }
    final String LS8000_MAC_FLAG =  "000EC3";
    /**
     * check mac address, the LS8000 is "000ec3"
     * @return
     */
    public boolean isMacMatch()
    {
        return true;
//        ArrayList<String>  ar = KDSSocketManager.getLocalIpAddressWithMac();
//        String strMac = "";
//
//        if (ar.size() != 2) {
//            strMac = KDSSocketManager.getMacAddressFromFile();
//            //return false;
//        }
//        else {
//            strMac = ar.get(1);
//
//        }
//        strMac = strMac.toUpperCase();
//        if (strMac.isEmpty()) return false;
//        if (strMac.length()<6) return false;
//        //for test
//        String[] arTest = new String[]{  "000000000039", "FC64BAB87E4A", "000000000002"};
//        for (int i=0; i< arTest.length; i++)
//            if (strMac.equals(arTest[i])) return true;
//
//        String pre = strMac.substring(0, 6);
//        return (pre.equals(LS8000_MAC_FLAG));

    }
    public void killMe()
    {
        this.finish();
//        ActivityManager am = (ActivityManager)getSystemService (Context.ACTIVITY_SERVICE);
//        am.killBackgroundProcesses(getPackageName());
        //System.exit(0);
        //android.os.Process.killProcess(android.os.Process.myPid());
    }
    public void showErrorMac()
    {
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.error))
                .setMessage(this.getString(R.string.error_match_mac))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SOSMainActivity.this.killMe();
                    }
                })
                .setCancelable(false)

                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        SOSMainActivity.this.killMe();
                    }
                })
                .create();
        d.setCanceledOnTouchOutside(false);
        d.show();
    }


    public void updateTitle()
    {
//        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean bEnabled = pre.getBoolean("general_router_enabled", false);
//        if (bEnabled)
//        {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        String strCustomized = pref.getString("kds_general_title","");


        String strTitle = strCustomized;
        if (strCustomized.isEmpty()) {

            String strVer =  getVersionName();
            String s = getString(R.string.sos_main_title);
            s = s + " " + strVer;
            strTitle = s;
            //m_txtTitle.setText(strTitle);
            //getTextView(R.id.txtTitle).setText(strTitle);

        }
        m_txtTitle.setTextColor(this.getResources().getColor(R.color.kds_title_fg));

//        String strVer =  getVersionName();
//        String s = getString(R.string.main_title);
//        s = s + " " + strVer;
        m_txtTitle.setText(strTitle);


    }
    /**
     * interface implements
     * @param prefs
     * @param key
     */
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
    {

//        if (key.equals(KDSTCPStation.getKey(KDSTCPStation.Stations_Type.For_Exp) )||
//                key.equals(KDSTCPStation.getKey(KDSTCPStation.Stations_Type.For_Mirror)) ||
//                        key.equals(KDSTCPStation.getKey(KDSTCPStation.Stations_Type.For_Slave)) )

        if (key.equals("kds_general_language"))
            return;
        if (m_service != null)
            m_service.updateSettings();
        updateTitle();


        m_reportShowing.updateSettings(getKDSSOS().getSettings());
        //if (key.equals(KDSSOSSettings.SOS_VIEW_LAYOUT_KEY))
            init_report_gui();
//        else
//        {
//            onMessageTimeForCreateAutoReport();
//        }


        //init_title();

    }

    public void lockAndroidWakeMode(boolean bLock)
    {
        if (m_wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            m_wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
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
    TimeDog m_clearDbTimeDog = new TimeDog();
    public void onTime()
    {
        if (m_service != null)
            m_service.onTimer();

        updateTime();

        checkNetworkState();

        checkCreateAutoReport();

        removeInactiveStations();

        checkLogFilesDeleting();
    }

    public void checkCreateAutoReport()
    {
        if (m_service == null) return;
        if (getKDSSOS().isTimeToCreateAutoReport()) {
//            getKDSSOS().createAutoReport();
            Message m = new Message();
            m.what = SOSMainActivityMessageHandler.MSG_AUTO_REPORT;
            m_msgHandler.sendMessage(m);
        }
        //   getKDSSOS().createAutoReport();
    }
    /**
     *
     * @param strError
     */
    public void showError(String strError)
    {
//        if (m_txtError != null)
//            m_txtError.setText(strError);
    }

    public void clearErrorMsg()
    {
        showError("");
    }
    public  boolean showInfo(String s)
    {
//        String lastInfo = "";
//        //don't show too many informations, memory lost
//        if (m_itemAdapterStation.m_listData.size() >KDSConst.MAX_INFORMATION_COUNT)
//        {
//            int ncount =m_infoAdapter.m_listData.size() - KDSConst.MAX_INFORMATION_COUNT;
//            for (int i=0; i< ncount; i++)
//            {
//                m_infoAdapter.m_listData.remove(0);
//            }
//
//        }
//
//
//        if (m_infoAdapter.m_listData.size() >0) {
//            lastInfo = m_infoAdapter.m_listData.get(m_infoAdapter.m_listData.size()-1);
//
//        }
//        if (lastInfo.equals(s))
//            return false;
//        m_infoAdapter.m_listData.add(s);
//        m_infoAdapter.notifyDataSetChanged();
//        m_lstInfo.setSelection(m_infoAdapter.getCount());
        return true;
        // return false;
    }

    public void onStationConnected(String ip, KDSStationConnection conn)
    {
        SOSKDSStationSOSInfo station =  getStation(ip);
        if (station != null) {
            if (station.getStatus().ordinal() <SOSKDSStationSOSInfo.StationSOSStatus.Connected.ordinal()) {
                station.setStatus(SOSKDSStationSOSInfo.StationSOSStatus.Connected);
                redrawList();
                m_reportShowing.updateStationStatus(station.getID(), SOSKDSStationSOSInfo.StationSOSStatus.Connected);
            }
        }
//        if (conn != null)
//            showInfo("Station ["+conn.toString() +"] connected.");
//        else
//            showInfo("Station ["+ip +"] connected.");
    }

    public void setStationStateWaitingReport(String stationID)
    {

        SOSKDSStationSOSInfo station =  getStationByID(stationID);
        if (station != null) {
            if (station.getStatus().ordinal() <SOSKDSStationSOSInfo.StationSOSStatus.Updating.ordinal()) {
                station.setStatus(SOSKDSStationSOSInfo.StationSOSStatus.Updating);
                redrawList();
                m_reportShowing.updateStationStatus(station.getID(), SOSKDSStationSOSInfo.StationSOSStatus.Updating);
            }
        }
    }

    public void setStationStateReceivedReport(String stationID)
    {
        SOSKDSStationSOSInfo station =  getStationByID(stationID);
        if (station != null) {
           // if (station.getStatus().ordinal() <KDSStationSOSInfo.StationSOSStatus.Active.ordinal()) {
                station.setStatus(SOSKDSStationSOSInfo.StationSOSStatus.Active);
                redrawList();
            m_reportShowing.updateStationStatus(station.getID(), SOSKDSStationSOSInfo.StationSOSStatus.Active);
            //}
        }
    }

    public void onStationDisconnected(String ip)
    {

        Log.d(TAG,KDSLog._FUNCLINE_()+ "Station ["+ip +"] disconnected.");
        String s = getString(R.string.station_disconnected);
        s = s.replace("#", ip);
        showInfo(s);//"Station ["+ip +"] disconnected.");
    }

    android.os.Handler m_handler = new android.os.Handler(new android.os.Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MSG_REFRESH_REPORT_VIEW:
                {
                    String stationID = (String)msg.obj;
                    setStationStateReceivedReport(stationID);
                    refreshReportView(stationID);

                }
                break;
                case MSG_REFRESH_WAITING_VIEW:
                {
                    String stationID = (String)msg.obj;
                    setStationStateWaitingReport(stationID);
                }
                break;
            }
            return true;
        }
    });
    final int MSG_REFRESH_REPORT_VIEW = 1;
    final int MSG_REFRESH_WAITING_VIEW = 2;
    public void onReceiveReport(String stationID,SOSReportOneStation report)
    {
        Message m = new Message();
        m.what = MSG_REFRESH_REPORT_VIEW;
        m.obj = stationID;
        m_handler.sendMessage(m);

    }

    public void onWaitingReport(String stationID)
    {
        Message m = new Message();
        m.what = MSG_REFRESH_WAITING_VIEW;
        m.obj = stationID;
        m_handler.sendMessage(m);
    }
    public void refreshReportView(String stationID)
    {


        m_reportShowing.refreshReportView(stationID, getKDSSOS().getReportCreators() );
    }

//    public void onAcceptIP(String ip)
//    {
//        String s = getString(R.string.pos_connected);
//        s = s.replace("#", ip);
//        showInfo(s);//"POS ["+ip +"] connected.");
//    }
//    // void onRefreshView(KDSUser.USER userID, KDSDataOrders orders);
//

    public void showAlertMessage(String title, String msg)
    {

        KDSUIDialogBase dlg = new KDSUIDialogBase();
        dlg.createInformationDialog(this,title,msg, false  );
        dlg.show();
//
//        AlertDialog d = new AlertDialog.Builder(this)
//                .setTitle(title)
//                .setMessage(msg)
//                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                            }
//                        }
//                )
//                .create();
//        d.show();
    }

    public void onShowMessage(String message)
    {
//        if (message.equals( KDSSMBDataSource.PATH_OK))
//        {
//            if (m_txtError != null)
//                m_txtError.setText("");
//
//        }
//        else if (message.equals(KDSSMBDataSource.PATH_LOST))
//        {
//            if (m_txtError != null) {
//                String s = this.getString(R.string.smb_folder_lost);
//                String folder = this.getKDSRouter().getSettings().getString(KDSRouterSettings.ID.KDSRouter_Data_Folder);
//                KDSSMBPath path = KDSSMBPath.parseString(folder);
//                folder = path.toDisplayString();
//                s = s.replace("#", folder);
//                m_txtError.setText(s);
//            }
//
//        }
//        else
        this.showInfo(message);
    }
    public void showToastMessage(String message) {
        int duration = Toast.LENGTH_SHORT;
        Toast t = Toast.makeText(this, message, duration);
        t.show();
    }
    public void onShowToastMessage(String message)
    {
        showToastMessage(message);
    }

    public boolean isAllUpdateFinished()
    {
        int ncount = m_lstStations.getCount();
        for (int i = 0; i < ncount; i++) {


            SOSKDSStationSOSInfo station =   ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().get(i);
            if (station.getStatus() != SOSKDSStationSOSInfo.StationSOSStatus.Updated)
                return false;


        }
        return true;
    }
//    //void onRefreshSummary(KDSUser.USER userID);
//    public void onAskOrderState(Object objSource, String orderName)
//    {
//
//    }



    SimpleDateFormat m_formatDate = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat m_formatTime = new SimpleDateFormat("HH:mm:ss");
    public void updateTime()
    {
//        if (!this.getSettings().getBoolean(KDSSettings.ID.Screen_Show_Time))
//            return;
        Date dt = new Date();
        String s = m_formatDate.format(dt);
        m_txtDate.setText(s);
        s = m_formatTime.format(dt);
        m_txtTime.setText(s);

    }

    private void checkNetworkState()
    {
        if (KDSSocketManager.isNetworkActived(this.getApplicationContext())) {
            m_imgState.setImageResource(R.drawable.online);
            if (m_imgState.getTag() != null &&
                    (int) m_imgState.getTag() == 0)
                // onNetworkRestored();
                m_imgState.setTag(1);
        }
        else{
            m_imgState.setImageResource(R.drawable.offline);
            if (m_imgState.getTag() == null ||
                    (int) m_imgState.getTag() == 1)
                //onNetworkLost();
                m_imgState.setTag(0);
        }
    }

    public void onBtnRefreshClicked(View v)
    {
        ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().clear();
        ((SOSAdapterStation) m_lstStations.getAdapter()).notifyDataSetChanged();
    }

//    public void onBtnUpdateDataClicked(View v)
//    {
//        if (m_lstStations.getCount()<=0)
//        {
//            showToastMessage(this.getString(R.string.no_station));
//            return;
//        }
//        //ProgressDialog dlg = ProgressDialog.show(this, "Waiting...", "Loading...");
//        showProgressDlg(this.getString(R.string.waiting_collecting), this.getString(R.string.collecting));
//
//        resetAllStations();
//        m_service.getKDSSOS().updateSOSDatabaseOneDay();
//    }

    public void onBtnLogoClicked(View v)
    {
        showPopupMenu(v);
    }
    private void showPopupMenu(View v)
    {
        PopupMenu popup = new PopupMenu(this, v);  //建立PopupMenu对象
        popup.getMenuInflater().inflate(R.menu.sos_menu_main,   //压入XML资源文件
                popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return SOSMainActivity.this.onOptionsItemSelected(item);
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

            //ListPopupWindow lw = null;
            // android.support.v7.internal.view.menu.MenuPopupHelper mHelper = ( MenuPopupHelper) field.get(popup);
            //    ((MenuPopupHelper) field.get(popup)).getPopup().getListView().setOnKeyListener(new View.OnKeyListener() {
            lw.getListView().setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

//                    if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT ||
//                            keyCode == KeyEvent.KEYCODE_CTRL_RIGHT))
                    if (event.getAction() == KeyEvent.ACTION_DOWN)
                    {
                        if (event.getRepeatCount()==0)
                            KDSKbdRecorder.convertKeyEvent(keyCode, event);

                    }

                    return false;
                }
            });
            //mHelper.setForceShowIcon(true);
        } catch (Exception err) {
            err.printStackTrace();
        }

    }
    private boolean findStation(String ip)
    {


        int ncount = m_lstStations.getCount();
        for (int i=0; i< ncount; i++) {

            String strIP = ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().get(i).getIP();
            if (strIP.equals(ip))
                return true;


        }
        return false;

    }

    private boolean findActiveStation(String ip)
    {
        ArrayList<KDSStationIP> ar = getKDSSOS().getStationsConnections().getAllActiveStations();

        int ncount = ar.size();
        for (int i=0; i< ncount; i++) {

            if (ar.get(i).getIP().equals(ip)) return true;

        }
        return false;

    }

    private SOSKDSStationSOSInfo getStation(String stationID, String ip) {


        int ncount = m_lstStations.getCount();
        for (int i = 0; i < ncount; i++) {

            String strID = ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().get(i).getID();
            String strIP = ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().get(i).getIP();

            if (strIP.equals(ip) &&
                    strID.equals(stationID))
                return ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().get(i);


        }
        return null;
    }

    private SOSKDSStationSOSInfo getStation( String ip) {


        int ncount = m_lstStations.getCount();
        for (int i = 0; i < ncount; i++) {


            String strIP = ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().get(i).getIP();

            if (strIP.equals(ip) )
                return ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().get(i);


        }
        return null;
    }

    private SOSKDSStationSOSInfo getStationByID( String stationID) {


        int ncount = ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().size();// m_lstStations.getCount();
        for (int i = 0; i < ncount; i++) {

            String strID = ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().get(i).getID();

            if (strID.equals(stationID) )
                return ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().get(i);


        }
        return null;
    }

    private void removeInactiveStations()
    {
        ArrayList<SOSKDSStationSOSInfo> arRemove = new ArrayList<>();
        int count =  ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().size();
        int ncount = m_lstStations.getCount();

        for (int i=0; i< count; i++)
        {
            String ip = ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().get(i).getIP();
            if (findActiveStation(ip))
                continue;
            arRemove.add(((SOSAdapterStation) m_lstStations.getAdapter()).getListData().get(i));

        }
        for (int i=0; i< arRemove.size(); i++)
        {
            ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().remove(arRemove.get(i));
            m_reportShowing.updateStationStatus(arRemove.get(i).getID(), SOSKDSStationSOSInfo.StationSOSStatus.Unknown);
        }
        if (arRemove.size() >0)
            ((SOSAdapterStation) m_lstStations.getAdapter()).notifyDataSetChanged();
    }
    public void onReceivedStationAnnounce(String stationID, String ip, String port, String mac)
    {
        removeInactiveStations();
        if (!isStationEnabled(stationID)) {
            SOSKDSStationSOSInfo info =  getStationByID(stationID);
            if (info!= null)
                info.setStatus(SOSKDSStationSOSInfo.StationSOSStatus.Disabled);
            m_reportShowing.updateStationStatus(stationID, SOSKDSStationSOSInfo.StationSOSStatus.Disabled);
        }
        //getKDSSOS().getStationsConnections().getAllActiveStations()
        if (findStation(ip)) {
            if (!isStationEnabled(stationID)) {
                SOSKDSStationSOSInfo info =  getStationByID(stationID);
                if (info!= null)
                   info.setStatus(SOSKDSStationSOSInfo.StationSOSStatus.Disabled);
                m_reportShowing.updateStationStatus(stationID, SOSKDSStationSOSInfo.StationSOSStatus.Disabled);
            }
            return;
        }

        // m_lstIPs.add(stationID + ":" + ip + ":" + port);
        SOSKDSStationSOSInfo station = new SOSKDSStationSOSInfo();
        station.setID(stationID);
        station.setIP(ip);
        station.setPort(port);
        //String timestamp = m_service.getKDSSOS().getSOSDB().getStationLastTimeStamp(stationID);
        //station.setLastUpdateDate(timestamp);
        if (!isStationEnabled(stationID)) {
            SOSKDSStationSOSInfo info =  getStationByID(stationID);
            if (info!= null)
                info.setStatus(SOSKDSStationSOSInfo.StationSOSStatus.Disabled);
            m_reportShowing.updateStationStatus(stationID, SOSKDSStationSOSInfo.StationSOSStatus.Disabled);
        }
        else {
            station.setStatus(SOSKDSStationSOSInfo.StationSOSStatus.Active);
            m_reportShowing.updateStationStatus(stationID, SOSKDSStationSOSInfo.StationSOSStatus.Active);
        }

        ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().add(station);

        //((AdapterStation) m_lstStations.getAdapter()).getListData().add(station);
        ((SOSAdapterStation) m_lstStations.getAdapter()).notifyDataSetChanged();


        //String str = this.getDialog().getContext().getString(R.string.active_stations_list);
        //m_txtTitle.setText(str);
    }

//    public void onListViewFuncClicked(View v)
//    {
//        //showPopupMenu(v);
//    }


    public boolean showSettingsDialog()
    {
        // m_bEnableRefreshTimer = false;

        Intent i = new Intent(SOSMainActivity.this, SOSSettingsActivity.class);
        startActivityForResult(i, KDSConst.SHOW_PREFERENCES);
        //startActivity(i, KDSConst.SHOW_PREFERENCES);
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


            return showSettingsDialog();

        }
        else if (id == R.id.action_about) {
            DialogAbout.showAbout(this, getVersionName());
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

            e.printStackTrace();
        }
        return appVersion;
    }

//    public void onBtnSaveClicked(View v)
//    {
//
//    }



    public void onMessageTimeForCreateAutoReport()
    {
        getKDSSOS().createAutoReport(m_reportShowing.getAllEnabledStations());

    }

    public void showProgressDlg(String title, String message)
    {

        //m_progressDlg = ProgressDialog.show(this, title, message);
        m_progressDlg = new ProgressDialog(this);
        m_progressDlg.setTitle(title);
        m_progressDlg.setMessage(message);
        //m_progressDlg.setCancelable(true);
        m_progressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelRefreshData();
            }
        });
        m_progressDlg.show();

    }

    public void hideProgressDlg()
    {
        if (m_progressDlg != null)
            m_progressDlg.hide();
        m_progressDlg = null;
    }

    public void cancelRefreshData()
    {
//        this.getKDSSOS().stopSOSDatabaseUpdating();
//        resetAllStations();
    }

    public void resetAllStations()
    {
        int ncount = m_lstStations.getCount();
        for (int i = 0; i < ncount; i++) {

            SOSKDSStationSOSInfo station = ((SOSAdapterStation) m_lstStations.getAdapter()).getListData().get(i);
            station.setStatus(SOSKDSStationSOSInfo.StationSOSStatus.Active);
        }
        ((SOSAdapterStation) m_lstStations.getAdapter()).notifyDataSetChanged();
    }

    public void redrawList()
    {
        ((SOSAdapterStation) m_lstStations.getAdapter()).notifyDataSetChanged();
    }

    public void restartMe()
    {
        Intent intent = new Intent(KDSApplication.getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // 杀掉进程
        //android.os.Process.killProcess(android.os.Process.myPid());
        //System.exit(0);
    }


    public void test()
    {
//        SOSReportCondition c = new SOSReportCondition();
//        c.setReportID("1234567");
//        c.setGraphPrepTimeDuration(300);
//        c.setGraphPrepTimeInterval(30);
//        c.setDeadlineToNow();
//        c.setRealPrepTimePeriodSeconds(60);
//
//
//        getKDSSOS().getReportCreators().createReport(c);
//
//
//        getKDSSOS().commandReturnSOSReport(null, null);
        checkCreateAutoReport();
    }


    public void toggleActiveStationsView()
    {
        View v = this.findViewById(R.id.layoutActiveStations);
        TextView toggle = (TextView) this.findViewById(R.id.toggleActiveStations);

        if (v.getVisibility() == View.GONE)
        {
            v.setAnimation(SOSAnimationUtil.getVisibleAnimationFromLeftToRight());
            v.setVisibility(View.VISIBLE);
            toggle.setText("<<<<");
        }
        else
        {
            v.setAnimation(SOSAnimationUtil.getGoneAnimationFromRightToLeft());
            v.setVisibility(View.GONE);
            toggle.setText(">>>>");
        }
    }


    public void init_report_gui()
    {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        String strLayout =  pref.getString(SOSSettings.SOS_VIEW_LAYOUT_KEY, "");

        m_sosLayout.setLayoutMode(SOSLinearLayout.LayoutMode.Running);
        m_sosLayout.clearAll();
        m_sosLayout.parseString(strLayout, this);
        m_reportShowing.initViews();

        onMessageTimeForCreateAutoReport();
//        String s = getKDSSOS().getSettings().getString(KDSSOSSettings.ID.SOS_Stations);
//        ArrayList<SOSStationConfig> arStations =  KDSSOSSettings.parseStations(s);
//        m_reportShowing.addStation(KDSSOSSettings.OVERALL_STATION_ID); //
//        for (int i=0; i< arStations.size(); i++)
//        {
//            if (arStations.get(i).getShowIndividual())
//                m_reportShowing.addStation(arStations.get(i).getStationID());
//        }


    }

    public boolean isStationEnabled(String stationID)
    {

        return ( m_reportShowing.isStationEnabled(stationID));

//            return true;
//        else {

//            String s = getKDSSOS().getSettings().getString(KDSSOSSettings.ID.SOS_Stations);
//            ArrayList<SOSStationConfig> arStations =  KDSSOSSettings.parseStations(s);
//
//            for (int i=0; i< arStations.size(); i++)
//            {
//                if (arStations.get(i).getStationID().equals(stationID)) {
//                    return (arStations.get(i).getShowIndividual() || arStations.get(i).getIncludedInOverall());
//
//                }
//            }
//            return false;
    //}

    }

    TimeDog m_logDog = new TimeDog();
    private void checkLogFilesDeleting()
    {
        if (m_service == null)
            return;
        if (m_service.getKDSSOS() == null)
            return;
        if (!m_logDog.is_timeout(30*1000)) //30 seconds
            return;
        int n = this.getKDSSOS().getSettings().getInt(SOSSettings.ID.Log_mode);
        KDSLog.LogLevel l = KDSLog.LogLevel.values()[n];
        if (l == KDSLog.LogLevel.None) return;

        int nDays = this.getKDSSOS().getSettings().getInt(SOSSettings.ID.Log_days);
        KDSLog.removeLogFiles(nDays);

    }

}
