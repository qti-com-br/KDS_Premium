package com.bematechus.kdsstatistic;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSSocketManager;
import com.bematechus.kdslib.KDSStationConnection;
import com.bematechus.kdslib.KDSTimer;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.TimeDog;

public class STMainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener, STKDSStatistic.KDSStatisticEvents, KDSTimer.KDSTimerInterface, STKDSStatistic.StationAnnounceEvents {

    final static private String TAG = "MainActivity";

    enum Report_Type
    {
        Order,
        Item,
    }
    private STService m_service = null;
    ServiceConnection m_serviceConn = null;

    TextView m_txtError = null;

    ListView m_lstStations = null;

    KDSTimer m_timer = new KDSTimer();

    //title
    ImageView m_imgMenu = null;
    ImageView m_imgState= null;
    TextView m_txtTime = null;
    TextView m_txtDate = null;
    PowerManager.WakeLock m_wakeLock = null;
    TextView m_txtTitle = null;
    ListView m_lstFunc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!isMacMatch()) {
            showErrorMac();
            return;
        }
        lockAndroidWakeMode(true);

        STSettings.Language language =  STSettings.loadLanguageOption(this.getApplicationContext());
        KDSUtil.setLanguage(this.getApplicationContext(), language);


        setContentView(R.layout.st_activity_main);
        explicitStartService();
        bindKDSRouterService();

        m_txtError = (TextView)findViewById(R.id.txtError);
        m_imgMenu  = (ImageView)findViewById(R.id.imgMenu);
        m_imgState= (ImageView)this.findViewById(R.id.imgState);
        m_txtTime = (TextView)this.findViewById(R.id.txtTime);
        m_txtDate = (TextView)this.findViewById(R.id.txtDate);
        m_txtTitle = (TextView)this.findViewById(R.id.txtTitle);

        m_lstStations = (ListView)findViewById(R.id.lstStations);
        m_lstStations.setAdapter(new STAdapterStation(this.getApplicationContext(), new ArrayList<STStationStatisticInfo>()));
        m_lstStations.setFocusable(false);

        m_lstFunc = (ListView)findViewById(R.id.lstFunc);
        initFuncListView(m_lstFunc);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        pref.registerOnSharedPreferenceChangeListener(this);
        m_timer.start(this, this, 1000);

        updateTitle();
    }

    protected void onDestroy() {
        super.onDestroy();
        explicitStopService();
    }

    ArrayList<STReportTypeDescription> getArray()
    {
        ArrayList<STReportTypeDescription> ar = new ArrayList<>();

        STReportTypeDescription rptOrder = new STReportTypeDescription();
        rptOrder.m_nIconID = R.drawable.order_report;
        rptOrder.m_strReport = this.getString(R.string.order_report);
        rptOrder.m_strReportDescription = getString(R.string.order_report_description);

        ar.add(rptOrder);// "Order Report");

        STReportTypeDescription rptItem = new STReportTypeDescription();
        rptItem.m_nIconID = R.drawable.item_report;
        rptItem.m_strReport = this.getString(R.string.item_report);
        rptItem.m_strReportDescription = getString(R.string.item_report_description);
        ar.add(rptItem);//"Item Report");

        return ar;
    }
    private void initFuncListView(ListView lst)
    {

        lst.setAdapter(new STAdapterReportType(this, getArray()));
        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Report_Type report =  Report_Type.values()[position];
                doReport(report);
            }
        });
    }
    public void doReport(Report_Type r)
    {
        switch (r)
        {

            case Order:
            {
                Intent intent = new Intent(STMainActivity.this, STActivityOrderReport.class);

                startActivityForResult(intent,r.ordinal());
            }
            break;
            case Item:
                Intent intent = new Intent(STMainActivity.this, STActivityItemReport.class);

                startActivityForResult(intent,r.ordinal());
                break;
            default:
                break;
        }
    }
    private void explicitStartService()
    {
        Intent intent = new Intent(this, STService.class);
        startService(intent);
    }
    private void explicitStopService()
    {
        Intent intent = new Intent(this, STService.class);
        stopService(intent);
    }
    public void onBindServiceFinished()
    {
        if (m_service == null) return;

        STGlobalVariables.setKDSStatistic(m_service.getKDSStatistic());
        m_service.getKDSStatistic().setEventReceiver(this);

        m_service.getKDSStatistic().setStationAnnounceEventsReceiver(this);

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                getKDSStatistic().broadcastStatisticRequireStationsUDP();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        m_clearDbTimeDog.reset();
    }

    STKDSStatistic getKDSStatistic()
    {
        if (m_service == null) return null;
        return m_service.getKDSStatistic();
    }

    private void bindKDSRouterService()
    {
        m_serviceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                m_service = ((STService.MyBinder)service).getService();
                STMainActivity.this.onBindServiceFinished();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                m_service = null;
            }
        };

        Intent intent = new Intent(this, STService.class);
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

                        STMainActivity.this.killMe();
                    }
                })
                .setCancelable(false)

                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        STMainActivity.this.killMe();
                    }
                })
                .create();
        d.setCanceledOnTouchOutside(false);
        d.show();
    }

    public void updateTitle()
    {
        m_txtTitle.setTextColor(this.getResources().getColor(R.color.kds_title_fg));
        String s = getString(R.string.main_title);
        s += " " + getVersionName();

        m_txtTitle.setText(s);
    }
    /**
     * interface implements
     * @param prefs
     * @param key
     */
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
    {

        if (key.equals("kds_general_language"))
            return;
        if (m_service != null)
            m_service.updateSettings();
        updateTitle();
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
        if (m_clearDbTimeDog.is_timeout(1800000)) //30x60x1000
        {

            m_clearDbTimeDog.reset();
        }
        checkCreateAutoReport();

        checkLogFilesDeleting();
    }

    public void checkCreateAutoReport()
    {
        if (getKDSStatistic() == null) return;
        if (getKDSStatistic().isTimeToCreateAutoReport()) {
            getKDSStatistic().createAutoReport();
        }

    }


    public void onStationConnected(String ip, KDSStationConnection conn)
    {
        STStationStatisticInfo station =  getStation(ip);
        if (station != null) {
            if (station.getStatus().ordinal() <STStationStatisticInfo.StationStatisticStatus.Connected.ordinal()) {
                station.setStatus(STStationStatisticInfo.StationStatisticStatus.Connected);
                redrawList();
            }
        }
    }

    public void onStationDisconnected(String ip)
    {

        STStationStatisticInfo station =  getStation(ip);
        if (station != null) {

                station.setStatus(STStationStatisticInfo.StationStatisticStatus.Active);
                redrawList();

        }

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
            m_imgState.setImageResource(R.drawable.online);
            if (m_imgState.getTag() != null &&
                    (int) m_imgState.getTag() == 0)

                m_imgState.setTag(1);
        }
        else{
            m_imgState.setImageResource(R.drawable.offline);
            if (m_imgState.getTag() == null ||
                    (int) m_imgState.getTag() == 1)

                m_imgState.setTag(0);
        }
    }

    public void onBtnRefreshClicked(View v)
    {
        ((STAdapterStation) m_lstStations.getAdapter()).getListData().clear();
        ((STAdapterStation) m_lstStations.getAdapter()).notifyDataSetChanged();
    }


    public void onBtnLogoClicked(View v)
    {
        showPopupMenu(v);
    }
    private void showPopupMenu(View v)
    {
        PopupMenu popup = new PopupMenu(this, v);  //建立PopupMenu对象
        popup.getMenuInflater().inflate(R.menu.st_menu_main,   //压入XML资源文件
                popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return STMainActivity.this.onOptionsItemSelected(item);
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
            err.printStackTrace();
        }

    }
    private boolean findStation(String ip)
    {


        int ncount = m_lstStations.getCount();
        for (int i=0; i< ncount; i++) {

            String strIP = ((STAdapterStation) m_lstStations.getAdapter()).getListData().get(i).getIP();
            if (strIP.equals(ip))
                return true;


        }
        return false;
    }

    private STStationStatisticInfo getStation( String ip) {


        int ncount = m_lstStations.getCount();
        for (int i = 0; i < ncount; i++) {


            String strIP = ((STAdapterStation) m_lstStations.getAdapter()).getListData().get(i).getIP();

            if (strIP.equals(ip) )
                return ((STAdapterStation) m_lstStations.getAdapter()).getListData().get(i);


        }
        return null;
    }

    public void onReceivedStationAnnounce(String stationID, String ip, String port, String mac)
    {
        if (findStation(ip))
            return;
        STStationStatisticInfo station = new STStationStatisticInfo();
        station.setID(stationID);
        station.setIP(ip);
        station.setPort(port);
        station.setStatus(STStationStatisticInfo.StationStatisticStatus.Active);

        ((STAdapterStation) m_lstStations.getAdapter()).getListData().add(station);

        ((STAdapterStation) m_lstStations.getAdapter()).notifyDataSetChanged();

    }

    public boolean showSettingsDialog()
    {
        Intent i = new Intent(STMainActivity.this, STActivityPreference.class);
        startActivityForResult(i, KDSConst.SHOW_PREFERENCES);

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

        } catch (Exception e) {

            e.printStackTrace();
        }
        return appVersion;
    }


    public void redrawList()
    {
        ((STAdapterStation) m_lstStations.getAdapter()).notifyDataSetChanged();
    }


    TimeDog m_logDog = new TimeDog();
    private void checkLogFilesDeleting()
    {
        if (!m_logDog.is_timeout(30*1000)) //30 seconds
            return;
        int n = this.getKDSStatistic().getSettings().getInt(STKDSSettings.ID.Log_mode);
        KDSLog.LogLevel l = KDSLog.LogLevel.values()[n];
        if (l == KDSLog.LogLevel.None) return;

        int nDays = this.getKDSStatistic().getSettings().getInt(STKDSSettings.ID.Log_days);
        KDSLog.removeLogFiles(nDays);

    }
}
