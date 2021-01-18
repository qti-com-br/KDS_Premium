package com.bematechus.kdslib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;


import com.bematechus.kdslib.BuildVer;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by David.Wong on 2018/7/10.
 * Rev:
 */
public class Activation implements ActivationHttp.HttpEvent , Runnable {

    static final String TAG = "ACTIVATION";
    static public final String PREF_KEY_ACTIVATION_GUID = "activation_guid";
    static public final String PREF_KEY_ACTIVATION_DATE = "activation_date";
    static public final String PREF_KEY_ACTIVATION_LOST_COUNT = "activation_lost";
    static public final String PREF_KEY_ACTIVATION_FAILED_DATE = "failed_date";
    static public final String PREF_KEY_ACTIVATION_FAILED_REASON = "activation_fail_reason";
    static public final String PREF_KEY_ACTIVATION_USER_NAME = "activation_user_name";
    static public final String PREF_KEY_ACTIVATION_PWD = "activation_password";
    static public final String PREF_KEY_STORE_GUID = "store_guid";
    static public final String PREF_KEY_STORE_NAME = "store_name";

    static public final String PREF_KEY_ACTIVATION_OLD_USER_NAME = "activation_old_user_name";

    static final public String KDSROUTER = "KDSRouter";

    /**
     *
     * Management
     * Web domain: http://kitchengous.com
     Login: bematech@kitchengous.com
     Password:098765

     //test store
     user: david
     pwd: 123456
     user: kds
     pwd: bematech
     */
//    static final String USER_NAME =  "androidkds";// "bematech@kitchengous.com";
//    static final String PASSWORD ="123456";// "098765";

    //final boolean _DEBUG = true;

//    static final int HOUR_MS = 3600000;
//    static int MAX_LOST_COUNT = 120;

    public static final int HOUR_MS = 3600000;
    public static int MAX_LOST_COUNT = 120; //kpp1-301,
    public static final int INACTIVE_TIMEOUT = 300000; //5 minutes
//
    public static long LOST_COUNT_INTERVAL =Activation.HOUR_MS;// 3600000L; //1 hour
    public static String PREMIUM_APP = "bc68f95c-1af5-47b1-a76b-e469f151ec3f";

    public enum SyncDataResult
    {
        OK,
        Fail_Http_exception,
        Fail_reponse_error,
    }

    public enum ItemJobFromOperations
    {
        Local_new_order,
        Local_bump_order,
        Local_unbump_order,
        Local_bump_item,
        Local_unbump_item,
        Expo_sync_prep_new_order,
        Expo_sync_prep_bump_order,
        Expo_sync_prep_unbump_order,
        Expo_sync_prep_bump_item,
        Expo_sync_prep_unbump_item,
    }
    public enum ActivationEvent
    {
        Get_orders,
    }

    public interface ActivationEvents
    {
        public void onActivationSuccess();
        public void onActivationFail(ActivationRequest.COMMAND stage, ActivationRequest.ErrorType errType, String failMessage);
        public void onSMSSendSuccess(String orderGuid, int smsState);
        public void onSyncWebReturnResult(ActivationRequest.COMMAND stage, String orderGuid, SyncDataResult result);
        public void onDoActivationExplicit();
        public void onForceClearDataBeforeLogin();
        public boolean isAppContainsOldData();
        public Object onActivationEvent(ActivationEvent evt, ArrayList<Object> arParams);
    }

    ActivationHttp m_http = new ActivationHttp();

    static private String m_storeGuid = "";


    static public String m_myMacAddress = "";
    static private String m_stationID = "1";
    static private String m_stationFuncName = SettingsBase.StationFunc.Prep.toString();

    private int m_nMaxLicenseCount = 0;
    ActivationEvents m_receiver = null;

    boolean m_bSilent = false;
    static private ArrayList<StoreDevice> m_devices = new ArrayList<>();//share in all instance

    Context m_context = null;

    static boolean m_bDoLicensing = false;

    int m_nSyncGetDevicesTries = 0; //record the loop count. Prevent dead loop.
    static private String m_storeName = ""; //2.0.50
    static private String m_storeKey = "";

    static private String m_timeZone = "";

    //for clear database when logout
    static public ActivationEvents m_globalEventsReceiver = null;

    static public void setGlobalEventsReceiver(ActivationEvents rec)
    {
        m_globalEventsReceiver = rec;
    }
    static public ActivationEvents getGlobalEventsReceiver()
    {
        return m_globalEventsReceiver;
    }

    public boolean isDoLicensing()
    {

        return m_bDoLicensing;
    }
    public void setDoLicensing(boolean bDoing)
    {
        m_bDoLicensing = bDoing;
    }
    public Activation(Context context)
    {
        m_context = context;
        m_http.setReceiver(this);
        loadStoreGuid();
        loadStoreName();
    }

    public void setStationID(String stationID)
    {
        m_stationID = stationID;
    }
    public void setStationFunc(SettingsBase.StationFunc func)
    {
        m_stationFuncName = func.toString();
    }
    public void setStationFunc(String funcName)
    {
        m_stationFuncName = funcName;
    }
    public void setMacAddress(String mac)
    {
        m_myMacAddress = mac;
        //if (_DEBUG)
            //m_myMacAddress = "19.ABCdef";//test	000ec3310238

        //m_myMacAddress = "000ec33102389";
    }

    /**
     * use it as device serial number.
     * @return
     */
    static public String getMySerialNumber()
    {
        // >= Android 5.0 (API 21): try using Serial Number
        // Otherwise: use MAC Address
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String device_serial = Build.SERIAL;
            if (!device_serial.isEmpty() && !device_serial.equals(Build.UNKNOWN)) {
                return device_serial;
            }
        }
        return m_myMacAddress;
    }
    public void setEventsReceiver(ActivationEvents receiver)
    {
        m_receiver = receiver;
    }
    public Activation()
    {
        m_http.setReceiver(this);
        loadStoreGuid();
        loadStoreName();
    }
    public void onHttpResponse(HttpBase httpBase, HttpBase.HttpRequestBase r)
    {

        ActivationRequest request = (ActivationRequest)r;
        ActivationHttp http = (ActivationHttp) httpBase;

        if (request.m_httpResponseCode == ActivationHttp.HTTP_OK) {
            switch (request.m_command) {
                case Unknown:
                    return;
                case Login:
                    onActivationResponseLogin(http, request);
                    break;
                case Sync_devices:
                    onActivationResponseSync(http, request);
                    break;
                case Get_settings:
                    onActivationResponseGetSettings(http, request);
                    break;
                case Get_devices:
                    onActivationResponseGetDevices(http, request);
                    break;
                case Replace:
                    onActivationResponseReplace(http, request);
                    break;
                case SMS:
                    onSMSResponse(http, request);
                    break;
                case Sync_orders:
                    onSyncOrderResponse(http, request);
                    break;
                case Sync_items:
                    onSyncItemsResponse(http, request);
                    break;
                case Sync_condiments:
                    onSyncCondimentsResponse(http, request);
                    break;
                case Sync_item_bumps:
                    onSyncItemBumpsResponse(http, request);
                    break;
                case Sync_item_bump:
                    onSyncItemBumpResponse(http, request);
                    break;
                case Sync_customer:
                    onSyncCustomerResponse(http, request);
                    break;
                case Cleaning:
                    onCleaningHttpResponse(http, request);
                    break;
                case Get_orders:
                    onActivationResponseGetOrders(http, request);
                    break;
                case Get_server_time:
                    onActivationResponseServerTime(http, request);
                    break;

            }
        }
        else if (request.m_httpResponseCode == ActivationHttp.HTTP_Exception)
        {//activatioinhttp code error/exception
            if (request.getCommand() == ActivationRequest.COMMAND.Sync_orders ||
                    request.getCommand() == ActivationRequest.COMMAND.Sync_items ||
                    request.getCommand() == ActivationRequest.COMMAND.Sync_condiments ||
                    request.getCommand() == ActivationRequest.COMMAND.Sync_item_bumps ||
                    request.getCommand() == ActivationRequest.COMMAND.Sync_item_bump
                        )
            {
                onSyncDataHttpException(http, request);
            }
            else {
                onActivationHttpException(http, request);
            }
        }
        else
        {//http server return error code
            if (request.getCommand() == ActivationRequest.COMMAND.Sync_orders ||
                    request.getCommand() == ActivationRequest.COMMAND.Sync_items ||
                    request.getCommand() == ActivationRequest.COMMAND.Sync_condiments ||
                    request.getCommand() == ActivationRequest.COMMAND.Sync_item_bumps ||
                    request.getCommand() == ActivationRequest.COMMAND.Sync_item_bump )
            {
                onSyncDataResponseError(http, request);
            }
            else {
                onActivationResponseError(http, request);
            }
        }
    }
    static public String getErrorMessage(String strResponse)
    {
        try {
            JSONArray jsons = new JSONArray(strResponse);
            if (jsons.length() >0)
                return ((JSONObject)jsons.get(0)).getString("error");
            else
                return "Unknown error";
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }
    static public boolean isResponseError(String response)
    {
        try {
//            JSONObject json = new JSONObject(response);
//            String errorInfo = json.getString("error");
//            if (errorInfo.isEmpty() ||
//                    errorInfo.toUpperCase().equals("NULL"))
//                return false;
            String s = response.toUpperCase();
            if (s.indexOf("\"ERROR\":NULL") >= 0)
                return false;
            if (s.indexOf("\"ERROR\":\"\"") >= 0)
                return false;
            return (s.indexOf("\"ERROR\":") >= 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return true;
    }
    /**
     *
     * [{"store_guid":"80c82eaf-d052-4e35-96a8-03a2b2ec838a","store_name":"rest","store_key":"223c2db3"}]
     * @param http
     * @param request
     */
    public void onActivationResponseLogin(ActivationHttp http, ActivationRequest request)
    {
        showProgressDialog(false, "");
        String response = request.m_result;
        if (isResponseError(response))
        {
            fireActivationFailEvent(request.getCommand(), ActivationRequest.ErrorType.UserName_Password, getErrorMessage(response));
            resetUserNamePassword();
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(response);
            if (jsonArray.length()<=0) return;
            JSONObject json = (JSONObject) jsonArray.get(0);

            String store_guid = json.getString(PREF_KEY_STORE_GUID);
            m_storeGuid = store_guid;

            m_storeName =  json.getString(PREF_KEY_STORE_NAME);//2.0.50
            m_storeKey =  json.getString("store_key");//2.0.50

            //System.out.println(m_storeGuid);

            //postSyncMac(m_licenseGuid, m_myMacAddress);
            postGetSettingsRequest();


        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void updateDevices(StoreDevice dev)
    {
        for (int i=0; i< m_devices.size(); i++)
        {
            if (m_devices.get(i).getGuid().equals(dev.getGuid())) {
                m_devices.remove(i);
                break;
            }
        }
        m_devices.add(dev);
    }

    public void onActivationResponseSync(ActivationHttp http, ActivationRequest request)
    {
        showProgressDialog(false, "");
        if (isResponseError(request.m_result))
        {
            fireActivationFailEvent(request.getCommand(), ActivationRequest.ErrorType.Sync_error, getErrorMessage(request.m_result));
            return;
        }
        try
        {
            m_nSyncGetDevicesTries ++;
            if (m_nSyncGetDevicesTries > MAX_TRY_COUNT)
            {
                fireActivationFailEvent(request.getCommand(), ActivationRequest.ErrorType.Sync_error, m_context.getString(R.string.cannot_sync_license_data));
                return;
            }
            postGetDevicesRequest();
//            JSONArray ar = new JSONArray(request.m_result);
//            if (ar.length() <=0)
//                return;
//            JSONObject json =(JSONObject) ar.get(0);
//
//            StoreDevice dev = parseJsonDevice(json);
//            updateDevices(dev);
//            if (findMyLicense()!= null)
//                fireSuccessEvent();
//            else
//                fireFailEvent(request.getCommand(), ActivationRequest.ErrorType.Sync_error, "Can not sync data with server.");
//            System.out.println(ar.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onActivationResponseReplace(ActivationHttp http, ActivationRequest request)
    {
        showProgressDialog(false, "");
        if (isResponseError(request.m_result))
        {
            fireActivationFailEvent(request.getCommand(), ActivationRequest.ErrorType.Replace_error, getErrorMessage(request.m_result));
            return;
        }
        try
        {
            m_nSyncGetDevicesTries ++;
            if (m_nSyncGetDevicesTries > MAX_TRY_COUNT)
            {
                fireActivationFailEvent(request.getCommand(), ActivationRequest.ErrorType.Replace_error, m_context.getString(R.string.cannot_replace_license_data));
                return;
            }
            //kp1-173
            //after replacing, its old station_id and function were not changed to mine if this station has set them.
            if (request.getNextStepData().size()>0) {
                if (!m_stationID.isEmpty())
                    postNewStationInfo2Web((String)request.getNextStepData().get(0), m_stationID, m_stationFuncName); //kpp1-173
            }
            postGetDevicesRequest();

        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(), e);
            //e.printStackTrace();
        }
    }


    /**
     * [
     *  {
     *      "guid":"708f92bb-2d68-4679-a800-bcfee5fbe2d2",
     *      "server_address":null,
     *      "server_username":"bematech@kitchengous.com",
     *      "server_password":"098765",
     *      "socket_port":1111,
     *      "auto_done_order_hourly":0,
     *      "auto_done_order_time":1530918000,
     *      "timezone":"America\/New_York",
     *      "smart_order":0,
     *      "licenses_quantity":7
     *   }
     * ]
     * @param http
     * @param request
     */
    public void onActivationResponseGetSettings(ActivationHttp http, ActivationRequest request)
    {
        showProgressDialog(false, "");
        if (isResponseError(request.m_result))
        {
            fireActivationFailEvent(request.getCommand(), ActivationRequest.ErrorType.Get_Settings_error, getErrorMessage(request.m_result));
            return;
        }
        try
        {
            JSONArray ar = new JSONArray(request.m_result);

            if (ar.length()<=0) return;
            JSONObject json = (JSONObject) ar.get(0);
            int ncount = json.getInt("licenses_quantity");
            m_timeZone = json.getString("timezone");
            String app = json.getString("store_app");
            if (!app.equals(PREMIUM_APP))
            {//kpp1-211 Only allow stores with Premium plan to log into Premium.
                fireActivationFailEvent(ActivationRequest.COMMAND.Get_settings, ActivationRequest.ErrorType.App_type_error, m_context.getString(R.string.only_premium_plan_login));
                return;
            }
            m_nMaxLicenseCount = ncount;

            //System.out.println(ar.toString());
            if (KDSApplication.isRouterApp()) //kpp1-305, Remove license restriction from Router
                fireSuccessEvent();
            else
                postGetDevicesRequest();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private StoreDevice parseJsonDevice(JSONObject json)
    {


        StoreDevice device = new StoreDevice();
        device.setEnabled(isLicenseEnabled(json));
        device.setGuid(getGuid(json));
        device.setSerial(getLicenseSerial(json));
        device.setID( getLicenseID(json) );
        device.setUpdateTime(getUpdateTime(json));
        device.setStationFunc(getStationFunc(json));
        device.setDeleted(isDeletedDevice(json));
        device.setStationName(getStationName(json));

        return device;

    }
    /**
     *[
     * {
     * "guid":"BE32E7FA-5B6E-427C-80AA-8103E8691792",
     * "serial":"D4376CF7-2E7A-407E-8CC0-252C0F4422E1",
     * "store_guid":"4220e7ee-dcdf-46d9-ae6b-565d228d6e2d",
     * "id":1,
     * "name":"Device 1",
     * "function":"EXPEDITOR",
     * "parent_id":0,
     * "expeditor":"",
     * "xml_order":1,
     * "enable":1,
     * "bump_transfer_device_id":0,
     * "line_display":0,
     * "screen_id":0,
     * "screen_size":768,
     * "split_screen_child_device_id":0,
     * "split_screen_parent_device_id":0,
     * "create_time":1530826269,
     * "update_time":1530902916,
     * "is_deleted":0,
     * "update_device":"D4376CF7-2E7A-407E-8CC0-252C0F4422E1",
     * "login":1,
     * "license":1
     * }
     * ]
     *
     *  Find my license serial
     * @param http
     * @param request
     */
    public void onActivationResponseGetDevices(ActivationHttp http, ActivationRequest request)
    {

        showProgressDialog(false, "");
        if (isResponseError(request.m_result))
        {
            fireActivationFailEvent(request.getCommand(), ActivationRequest.ErrorType.Get_Devices_error, getErrorMessage(request.m_result));
            return;
        }

        try
        {
            JSONArray ar = new JSONArray(request.m_result);
            //System.out.println(ar.toString());
            m_devices.clear();
            for (int i=0; i< ar.length() ; i++)
            {
                JSONObject json =(JSONObject) ar.get(i);
                StoreDevice device =parseJsonDevice(json);
                if (device == null) //KPP1-27
                    continue;
                m_devices.add(device);
            }

            checkMyActivation();

        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(), e);
            //e.printStackTrace();
        }
    }

    static private StoreDevice findMyLicense()
    {
        for (int i=0; i< m_devices.size(); i++)
        {
            if (m_devices.get(i).isDeleted()) continue;
            if (!m_devices.get(i).getEnabled()) continue;
            StoreDevice dev =m_devices.get(i);
            String serial = dev.getSerial();
            serial = serial.toUpperCase();
            if (serial.equals(getMySerialNumber().toUpperCase())) {
                //the router and kds can run in same device, and they have to register individually.
                if (KDSApplication.isRouterApp())
                {
                    if (dev.getStationFunc().equals(Activation.KDSROUTER))
                        return dev;
                    else
                        continue;
                }
                else
                {
                    if (dev.getStationFunc().equals(Activation.KDSROUTER))
                        continue;
                    else
                        return dev;
                }


            }

        }
        return null;
    }

    private StoreDevice findDeletedLicense()
    {
        for (int i=0; i< m_devices.size(); i++)
        {
            if (!m_devices.get(i).isDeleted()) continue;
            StoreDevice dev =m_devices.get(i);
            String serial = dev.getSerial();
            serial = serial.toUpperCase();
            if (serial.equals(getMySerialNumber().toUpperCase())) {
                //the router and kds can run in same device, and they have to register individually.
                if (KDSApplication.isRouterApp()) {
                    if (dev.getStationFunc().equals(Activation.KDSROUTER))
                        return dev;
                    else
                        continue;
                } else {
                    if (dev.getStationFunc().equals(Activation.KDSROUTER))
                        continue;
                    else
                        return dev;
                }
               // return m_devices.get(i);
            }
        }
        return null;
    }

    private int MAX_TRY_COUNT = 3;
    private void checkMyActivation()
    {
        //I have been registered
        StoreDevice device = findMyLicense();

        if (device != null)
        {
            if (!device.getEnabled())
            {
                fireActivationFailEvent(ActivationRequest.COMMAND.Get_devices, ActivationRequest.ErrorType.License_disabled,m_context.getString(R.string.license_deactivated));
                return;
            }
            else {
                fireSuccessEvent();
                return;
            }
        }

        //no valid
        if (m_nMaxLicenseCount <=0 ||
                (getRegisteredDevicesCount() >= m_nMaxLicenseCount ))//2.1.2
        {
            fireActivationFailEvent(ActivationRequest.COMMAND.Get_devices, ActivationRequest.ErrorType.No_valid_license,  m_context.getString(R.string.no_license_available));
            return;
        }

        if (findDeletedLicense() != null)
        {//I have been deleted
            if (!ActivityLogin.isShowing())
            {
                if (m_receiver != null) {
                    setDoLicensing(false);//m_bDoLicensing = false;
                    m_receiver.onDoActivationExplicit();
                    return;
                }
            }
        }
        //register me now
        if (getDevicesCount()<=0)
        {//add new
            if (m_nSyncGetDevicesTries > MAX_TRY_COUNT) {
                fireActivationFailEvent(ActivationRequest.COMMAND.Sync_devices, ActivationRequest.ErrorType.Sync_error,m_context.getString(R.string.cannot_sync_license_data));
                return;
            }
            postSyncNewMac("",m_stationID,m_stationFuncName, getMySerialNumber(), null);
            return;
        }

        if (m_nSyncGetDevicesTries > MAX_TRY_COUNT)
        {
            fireActivationFailEvent(ActivationRequest.COMMAND.Sync_devices, ActivationRequest.ErrorType.Sync_error,m_context.getString(R.string.cannot_sync_license_data));
            return;
        }
        if (m_nSyncGetDevicesTries>0)
            fireActivationFailEvent(ActivationRequest.COMMAND.Sync_devices, ActivationRequest.ErrorType.Sync_error,"Sync data error, try again!");

        showRegisterOptionDlg();

    }

    private boolean isLicenseEnabled(JSONObject json)
    {
        try {
            int n = json.getInt("license");
            return (n ==1);
        }
        catch ( Exception e)
        {

        }
        return false;

    }

    private boolean isDeletedDevice(JSONObject json)
    {
        try {
            int n = json.getInt("is_deleted");
            return (n ==1);
        }
        catch ( Exception e)
        {

        }
        return false;

    }

    private boolean isLicenseUsed(JSONObject json)
    {
        try {
            String s = json.getString("name");

            s = s.toUpperCase();
            if (s.equals("DEVICE 1"))
                return false;
            return (!s.isEmpty());

        }
        catch ( Exception e)
        {

        }
        return false;
    }

    private boolean isLicenseUsedByMe(JSONObject json, String macAddress)
    {
        try {
            String s = json.getString("name");

            s = s.toUpperCase();
            macAddress = macAddress.toUpperCase();
            return (s.equals(macAddress));
        }
        catch ( Exception e)
        {

        }
        return false;
    }

    private String getLicenseSerial(JSONObject json)
    {
        try {
            String s = json.getString("serial");
            return s;
        }
        catch ( Exception e)
        {

        }
        return "";
    }

    private String getLicenseID(JSONObject json)
    {
        try {
            String s = json.getString("id");
            return s;
        }
        catch ( Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    private long getUpdateTime(JSONObject json)
    {
        try {
            long l = json.getLong("update_time");
            return l;
        }
        catch ( Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }


    private String getGuid(JSONObject json)
    {
        try {
            String s = json.getString("guid");
            return s;
        }
        catch ( Exception e)
        {

        }
        return "";
    }

    public void postLoginRequest(String userName, String pwd)
    {

        ActivationRequest r = ActivationRequest.requestLogin(userName, pwd);
        m_http.request(r);
        showProgressDialog(true, m_context.getString(R.string.logining));
    }

    public void postGetSettingsRequest()
    {
        ActivationRequest r = ActivationRequest.requestGetSettings(m_storeGuid);
        m_http.request(r);
        showProgressDialog(true, m_context.getString(R.string.retrieve_store_settings));
    }

    public void postGetDevicesRequest()
    {
        ActivationRequest r = ActivationRequest.requestGetDevices(m_storeGuid);
        m_http.request(r);
        showProgressDialog(true, m_context.getString(R.string.retrieve_licenses_data));
    }

    /**
     * update the database in server.
     * @param licenseGuid
     * @param macAddress
     * rev.
     *  2.1.4
     *      dev parameters, for update time value. This update_time must < "new one".
     *
     *      samples:
     *      [{"tok":"c0a6r1l1o9sL6t2h4gjhak7hf3uf9h2jnkjdq37qh2jk3fbr1706"},{"data":[{"bump_transfer_device_id":"0","xml_order":"2","screen_id":"1","screen_size":"0","enable":"1","split_screen_child_device_id":"0","split_screen_parent_device_id":"0","function":"'EXPEDITOR'","id":"1","guid":"'c6ad5b2d-4d72-4ab1-a66a-f8d49a927603'","is_deleted":"0","update_time":"1537313373","store_guid":"'7dc418db-25a1-4b0c-aa41-b357acec2033'","name":"'1'","create_time":"1537313373","login":"0","license":"1","serial":"'5.123456789'","line_display":"0","parent_id":"0","update_device":"''"}],"req":"SYNC","entity":"devices"}]
     */
    public void postSyncNewMac(String licenseGuid,String stationID,String stationFunc, String macAddress, StoreDevice dev)
    {
        ActivationRequest r = ActivationRequest.requestNewMac(m_storeGuid, stationID,stationFunc, licenseGuid, macAddress, dev);
        m_http.request(r);
        showProgressDialog(true, m_context.getString(R.string.updating_license_data));
    }

    public void postReplaceMac(String licenseGuid,String macAddress)
    {
        ActivationRequest r = ActivationRequest.requestReplaceMac(m_storeGuid, licenseGuid, macAddress);
        r.getNextStepData().add(licenseGuid); //kpp1-173, use it to upload latest station id
        m_http.request(r);
        showProgressDialog(true, m_context.getString(R.string.updating_license_data));
    }

    public void onActivationResponseError(ActivationHttp http, ActivationRequest request)
    {
        showProgressDialog(false, "");
        //System.out.println(request.m_httpResponseCode);
        if (request.m_httpResponseCode == 301)
            resetUserNamePassword();
        if (request.m_httpResponseCode == 404)
        {
            //int ncount = recordLostInternetCount();

        }

        fireActivationFailEvent(request.getCommand(), ActivationRequest.ErrorType.Http_error_code, "Http response error code =" + request.m_httpResponseCode);

    }

    public void onActivationHttpException(ActivationHttp http, ActivationRequest request)
    {
        showProgressDialog(false, "");
        //System.out.println(request.m_httpResponseCode);
        fireActivationFailEvent(request.getCommand(), ActivationRequest.ErrorType.Http_exception, request.getResult());

    }

    public void fireSuccessEvent()
    {
        setDoLicensing(false);//m_bDoLicensing = false;
        StoreDevice dev = findMyLicense();

        String guid = "";
        if (dev != null)
            dev.getGuid();
        saveActivationGuid(guid);
        resetFailedCount(); //reset this counter!
        saveLastFailedReason(ActivationRequest.ErrorType.OK);

        postGetServerTimeRequest(); //kpp1-397

        if (m_receiver != null)
            m_receiver.onActivationSuccess();
    }
    public void fireActivationFailEvent(ActivationRequest.COMMAND stage,ActivationRequest.ErrorType errType, String strMessage)
    {
        setDoLicensing(false);//m_bDoLicensing = false;
        updateFailedCount();//record failed count
        saveLastFailedReason(errType);

        if (m_receiver != null)
            m_receiver.onActivationFail(stage,errType, strMessage);
    }

    private void buildRegisterOptionDlgGui(View view, boolean bAddNew)
    {
        View v = view.findViewById(R.id.layoutDevices);
        if (bAddNew)
            v.setVisibility(View.GONE);
        else {
            v.setVisibility(View.VISIBLE);
            showDevicesInList(view, true); //KPP1-173
//            if (getEnabledDevicesCount() >= m_nMaxLicenseCount)
//                showDevicesInList(view, false);
//            else
//                showDevicesInList(view, false);

        }

    }


    private int getDisabledDevicesCount()
    {
        int ncount = 0;
        for (int i=0; i< m_devices.size(); i++)
        {
            if (!m_devices.get(i).getEnabled())
                ncount ++;

        }
        return ncount;
    }

    private int getEnabledDevicesCount()
    {
        int ncount = 0;
        for (int i=0; i< m_devices.size(); i++)
        {
            if (m_devices.get(i).isDeleted()) continue;
            if (m_devices.get(i).getEnabled())
                ncount ++;

        }
        return ncount;
    }

    private int getDevicesCount()
    {
        int ncount = 0;
        for (int i=0; i< m_devices.size(); i++)
        {
            if (m_devices.get(i).isDeleted()) continue;

            ncount ++;

        }
        return ncount;
    }


//    private void showDisabledDevicesInList(View view)
//    {
//        ListView lst = (ListView) view.findViewById(R.id.lstData);
//
//        ArrayList<StoreDevice> ar = new ArrayList<>();
//        for (int i=0; i< m_devices.size(); i++)
//        {
//            if (!m_devices.get(i).getEnabled())
//                ar.add(m_devices.get(i));
//        }
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(m_context, R.layout.activation_list_item,(List) ar);//m_devices);
//        lst.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
//
//    }

    private void showDevicesInList(View view, boolean bEnabledDevice)
    {
        ListView lst = (ListView) view.findViewById(R.id.lstData);

        ArrayList<StoreDevice> ar = new ArrayList<>();
        for (int i=0; i< m_devices.size(); i++)
        {
            if (bEnabledDevice)
            {
                if (m_devices.get(i).getEnabled())
                    ar.add(m_devices.get(i));
            }
            else
            {
                if (!m_devices.get(i).getEnabled()) {
                    if (!m_devices.get(i).getGuid().isEmpty())
                        ar.add(m_devices.get(i));
                }
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(m_context, R.layout.activation_list_item,(List) ar);//m_devices);
        lst.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    AlertDialog m_dlgRegisterOptions = null;
    /**
     * choose how to register this station
     */
    private void showRegisterOptionDlg()
    {
        //Log.i(TAG, "reg: showRegisterOptionDlg");
        if (m_storeName.isEmpty() || m_storeGuid.isEmpty()) return;

        Context context = m_context;// KDSApplication.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.dlg_register_option, null);
        RadioButton btnAddNew = (RadioButton) view.findViewById(R.id.rbAddNew);
        btnAddNew.setTag(view);

        btnAddNew.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buildRegisterOptionDlgGui((View) buttonView.getTag(), isChecked);
            }
        });
        RadioButton btnReplace = (RadioButton) view.findViewById(R.id.rbReplace);
        btnReplace.setTag(view);
        btnReplace.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                buildRegisterOptionDlgGui((View) buttonView.getTag(), !isChecked);
            }
        });


        m_dlgRegisterOptions = new AlertDialog.Builder(context)
                .setPositiveButton(context.getString(R.string.str_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        AlertDialog dlg = (AlertDialog)dialog;

                        RadioButton txtAddNew = (RadioButton) dlg.findViewById(R.id.rbAddNew);

                        //RadioButton txtReplace = (RadioButton) dlg.findViewById(R.id.rbReplace);

                        afterSelectedRegisterOption((View)txtAddNew.getTag());
                        m_dlgRegisterOptions = null;
                    }
                })
                .setTitle( context.getString(R.string.activation))
                .setNegativeButton(context.getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        afterCancelRegisterOptionDlg();
                        m_dlgRegisterOptions = null;
                    }
                })
                .create();
        m_dlgRegisterOptions.setView(view);
        m_dlgRegisterOptions.show();
        m_dlgRegisterOptions.setCancelable(false);
        m_dlgRegisterOptions.setCanceledOnTouchOutside(false);
        //init gui
        if (getRegisteredDevicesCount() >= m_nMaxLicenseCount) {
            btnAddNew.setEnabled(false);
            btnAddNew.setChecked(false);
            btnReplace.setChecked(true);
        }
        else
        {
            btnAddNew.setEnabled(true);
            btnAddNew.setChecked(true);
        }

    }

    private void afterSelectedRegisterOption(View view)
    {
        RadioButton btnAddNew = (RadioButton) view.findViewById(R.id.rbAddNew);
        if (btnAddNew.isChecked())
        {
            postSyncNewMac("", m_stationID,m_stationFuncName, getMySerialNumber(), null);
        }
        else
        {
            ListView lst = (ListView) view.findViewById(R.id.lstData);
            StoreDevice dev = findSelectedLicense(lst);
            if (dev == null) {
                //Toast.makeText(KDSApplication.getContext(), "No selected item", Toast.LENGTH_LONG).show();
                fireActivationFailEvent(ActivationRequest.COMMAND.Sync_devices, ActivationRequest.ErrorType.No_selected_license_to_replace,  m_context.getString(R.string.no_selected_license_to_replace));
            }
            else
                postReplaceMac(dev.getGuid(), getMySerialNumber());

                //postSyncMac(dev.getGuid(),m_stationID, m_myMacAddress, dev);
        }
    }

    private StoreDevice findSelectedLicense(ListView lst)
    {
        ArrayAdapter ar =(ArrayAdapter) lst.getAdapter();

        for (int i=0; i< lst.getCount(); i++)
        {
            if (lst.isItemChecked(i))
                return (StoreDevice) ar.getItem(i);
        }
        return null;
    }

    private void afterCancelRegisterOptionDlg()
    {
        fireActivationFailEvent(ActivationRequest.COMMAND.Sync_devices, ActivationRequest.ErrorType.Cancel_license_options, "Canceled");
    }


//    public void showInputNamePasswordDlg(Context context)
//    {
//        View view = LayoutInflater.from(context).inflate(R.layout.dlg_input_name_pwd, null);
//        AlertDialog d = new AlertDialog.Builder(context)
//                .setTitle("Activation")
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        AlertDialog dlg = (AlertDialog)dialog;
//                        TextView txtName = (TextView) dlg.findViewById(R.id.txtName);
//                        String name = txtName.getText().toString();
//
//                        TextView txtPwd = (TextView) dlg.findViewById(R.id.txtPwd);
//                        String pwd = txtPwd.getText().toString();
//                        afterInputNamePwd(name, pwd);
//
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        afterCancelInputNamePwd();
//                    }
//                })
//                .create();
//        d.setView(view);
//        d.show();
//        d.setCancelable(false);
//        d.setCanceledOnTouchOutside(false);
//
//
//        WindowManager.LayoutParams layoutParams = d.getWindow().getAttributes();
//        layoutParams.gravity= Gravity.BOTTOM;
//        layoutParams.width= WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.height= WindowManager.LayoutParams.MATCH_PARENT;
//        d.getWindow().getDecorView().setPadding(0, 0, 0, 0);
//        d.getWindow().setAttributes(layoutParams);
//
////        Window window = d.getWindow();
////        window.getDecorView().setPadding(0, 0, 0, 0);
////        //window.setGravity(Gravity.CENTER);
////        //window.setContentView(R.layout.abc);
////        WindowManager.LayoutParams lp = window.getAttributes();
////        lp.width = WindowManager.LayoutParams.FILL_PARENT;
////        lp.height = WindowManager.LayoutParams.FILL_PARENT;
////        window.setAttributes(lp);
//    }
//    private void afterInputNamePwd(String name, String pwd)
//    {
//        saveUserNamePwd(name, pwd);
//
//        postLoginRequest(name, pwd);
//    }
//
//    private void afterCancelInputNamePwd()
//    {
//        fireFailEvent(ActivationRequest.COMMAND.Login, "No username and password");
//
//    }

    public void resetUserNamePassword()
    {

        //saveUserNamePwd("", "");
    }

    static public boolean hasDoRegister()
    {
        return (!loadUserName().isEmpty());
    }
    static public String loadUserName()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        String s = pref.getString(PREF_KEY_ACTIVATION_USER_NAME, "");
        return s;
    }

    public void saveUserNamePwd(String userName, String pwd)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_KEY_ACTIVATION_USER_NAME, userName);
        editor.putString(PREF_KEY_ACTIVATION_PWD, pwd);
        editor.putString(PREF_KEY_STORE_GUID, m_storeGuid);
        editor.putString(PREF_KEY_STORE_NAME, m_storeName);

        editor.putString(PREF_KEY_ACTIVATION_OLD_USER_NAME, userName); //kpp1-299, save current as old one.

        editor.apply();
        editor.commit();


    }
    static public String loadPassword()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        String s = pref.getString(PREF_KEY_ACTIVATION_PWD, "");
        return s;
    }

//    private void savePassword(String pwd)
//    {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
//        SharedPreferences.Editor editor = pref.edit();
//        editor.putString("activation_password", pwd);
//        editor.apply();
//        editor.commit();
//
//
//    }

    static public ActivationRequest.ErrorType loadLastFailedReason()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        int n = pref.getInt(PREF_KEY_ACTIVATION_FAILED_REASON, ActivationRequest.ErrorType.OK.ordinal());
        ActivationRequest.ErrorType e = ActivationRequest.ErrorType.values()[n];
        return e;


    }

    public void saveLastFailedReason(ActivationRequest.ErrorType e)
    {
        int n = e.ordinal();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_KEY_ACTIVATION_FAILED_REASON, n);
        editor.apply();
        editor.commit();

    }


    static public boolean isActivationFailedEnoughToLock()
    {

//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
//        int ncount = pref.getInt("activation_lost", 0);
//
        int ncount = loadFailedCount();
        return (ncount > MAX_LOST_COUNT);

    }

    static public int loadFailedCount()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        int ncount = pref.getInt(PREF_KEY_ACTIVATION_LOST_COUNT, 0);
        return ncount;
    }


    public int updateFailedCount()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        int ncount = pref.getInt(PREF_KEY_ACTIVATION_LOST_COUNT, 0);
        long nTime = pref.getLong(PREF_KEY_ACTIVATION_FAILED_DATE, 0);

        Date dt = new Date();
        long nDelay = dt.getTime() - nTime;
        nDelay = Math.abs(nDelay);
        if (nDelay <LOST_COUNT_INTERVAL)
            return ncount;
        if (nTime ==0) //first time
            ncount ++;
        else {
            long nn =  (nDelay / LOST_COUNT_INTERVAL);
            ncount += nn;
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_KEY_ACTIVATION_LOST_COUNT, ncount);
        editor.putLong(PREF_KEY_ACTIVATION_FAILED_DATE, dt.getTime());

        editor.apply();
        editor.commit();
        return ncount;
    }

    public int resetFailedCount()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_KEY_ACTIVATION_LOST_COUNT, 0);
        editor.putLong(PREF_KEY_ACTIVATION_FAILED_DATE, 0);

        //editor.putString(PREF_KEY_ACTIVATION_OLD_USER_NAME, "");//kpp1-299, remove it. Keep old one always.

        editor.apply();
        editor.commit();
        return 0;
    }

    public void saveActivationGuid(String guid)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_KEY_ACTIVATION_GUID, guid);
        Date dt = new Date();

        editor.putLong(PREF_KEY_ACTIVATION_DATE,dt.getTime());
        editor.apply();
        editor.commit();

    }


    static public boolean isActivationPassed()
    {
        String userName = loadUserName();
        String password = loadPassword();
        ActivationRequest.ErrorType e = loadLastFailedReason();
        int nFailedCount = loadFailedCount();
        if (!userName.isEmpty() &&
                !password.isEmpty() &&
                e == ActivationRequest.ErrorType.OK &&
                nFailedCount == 0)
            return true;
        return false;
    }
    /**
     * start from here.
     */
    public void startActivation(boolean bSilent,boolean bForceShowNamePwdDlg, Activity caller, String showErrorMessage)
    {

        if (isDoLicensing()) return;// (m_bDoLicensing) return;
        setDoLicensing(true);//m_bDoLicensing = true;
        m_nSyncGetDevicesTries = 0;
        //Log.i(TAG, "reg: startActivation, bSilent=" + (bSilent?"true":"false"));

        m_bSilent = bSilent;
        String userName = loadUserName();
        String password = loadPassword();
        KDSLog.e(TAG, "startActivation, bSilent=" + (bSilent?"true":"false") + ",name="+userName+",pwd="+password);

//        userName = USER_NAME;
//        password = PASSWORD;
        if (userName.isEmpty() || password.isEmpty()) {
            if (m_bSilent) {
                updateFailedCount();
                setDoLicensing(false);//m_bDoLicensing = false;
                KDSLog.e(TAG, KDSLog._FUNCLINE_() + "fireActivationFailEvent fired");
                fireActivationFailEvent(ActivationRequest.COMMAND.Login,  ActivationRequest.ErrorType.UserName_Password, "No valid username and password");
                return;
            }
            KDSLog.e(TAG, KDSLog._FUNCLINE_() + "showLoginActivity called");
            showLoginActivity(caller, showErrorMessage);

            //showInputNamePasswordDlg(m_context);
        }
        else
        {

//            ActivationRequest.ErrorType lastError = loadLastFailedReason();
//            if (lastError == ActivationRequest.ErrorType.UserName_Password)
//            {
//                if (!m_bSilent) {
//                    showLoginActivity(caller);
//                    return;
//                }
//            }
            if ( !bForceShowNamePwdDlg) {
                KDSLog.e(TAG, KDSLog._FUNCLINE_() + "postLoginRequest called");
                postLoginRequest(userName, password);
                setDoLicensing(false); //kpp1-368
            }
            else {
                KDSLog.e(TAG, KDSLog._FUNCLINE_() + "showLoginActivity called 2");
                showLoginActivity(caller, showErrorMessage);
            }
        }
    }
    ProgressDialog m_progressDlg = null;
    private void showProgressDialog(boolean bShow, String message)
    {
        if (m_bSilent)
            return ;
        if (m_progressDlg == null)
            m_progressDlg = new ProgressDialog(m_context);
        m_progressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //m_progressDlg.setTitle("Communicating to server");
        m_progressDlg.setMessage(message);
        if (bShow) {
            m_progressDlg.show();
            Window window = m_progressDlg.getWindow();
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
        else
            m_progressDlg.hide();

    }

    public void showLoginActivity(Activity caller, String showErrorMessage)
    {
        //debug
        //return;

        if (KDSConst._DEBUG) {
            if (KDSConst._DEBUG_HIDE_LOGIN_DLG)
                return;
        }
        if (ActivityLogin.isShowing()) return; //kpp1-434

        KDSLog.e(TAG,KDSLog._FUNCLINE_() + "Enter");
        setDoLicensing(true);//m_bDoLicensing = true;
        Intent intent = new Intent(caller, ActivityLogin.class);

        intent.putExtra("func", KDSConst.SHOW_LOGIN);
        intent.putExtra("id", m_stationID);
        intent.putExtra("mac", getMySerialNumber());
        intent.putExtra("errmsg", showErrorMessage);

        caller.startActivityForResult(intent, KDSConst.SHOW_LOGIN);

        KDSLog.e(TAG,KDSLog._FUNCLINE_() + "Exit");

    }


    /**
     * 2.0.50

     *  Local orderGuid value
     *
     * @param nSMSState
     *  See SMS_STATE_UNKNOWN ... in KDSDataOrder
     */
    public void postSMS(KDSDataOrder order,  int nSMSState)
    {
        ActivationRequest r = ActivationRequest.requestSMS(m_storeGuid,m_storeName, order, nSMSState );
        m_http.request(r);

    }


    public void onSMSResponse(ActivationHttp http, ActivationRequest request)
    {

        if (isResponseError(request.m_result))
        {
            //fireFailEvent(request.getCommand(), ActivationRequest.ErrorType.Replace_error, getErrorMessage(request.m_result));
            KDSLog.i(TAG, KDSLog._FUNCLINE_() + "SMS sending failed");
            return;
        }
        else
        {
            String s = request.getParams();
            try {
                JSONArray jsonar = new JSONArray(s);
                if (jsonar.length()!=2)
                    return;
                JSONObject json = (JSONObject) jsonar.get(1);

                String orderguid = json.getString("order_guid");
                String smsState = json.getString("order_status");
                if (orderguid.isEmpty())
                {
                    json = (JSONObject) jsonar.get(0);
                    orderguid = json.getString("order_guid");
                    smsState = json.getString("order_status");
                }
                if (m_receiver != null)
                    m_receiver.onSMSSendSuccess(orderguid, KDSUtil.convertStringToInt( smsState, KDSDataOrder.SMS_STATE_NEW) );
            }
            catch (Exception e)
            {
                KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
            }

        }

    }


    static public boolean needShowInactiveTitle(ActivationRequest.ErrorType errType)
    {
        boolean bShowInactive = false;
        switch (errType)
        {
            case OK:
            case Http_error_code:
            case Http_exception:
            case Cancel_license_options:
            case Replace_error:
            case Sync_error:
            case Get_Settings_error:
            case Get_Devices_error:
            case No_selected_license_to_replace:
                bShowInactive = false;
                break;
            case UserName_Password:
            case No_valid_license:
            case License_disabled:
                bShowInactive = true;
                break;
            default:
                bShowInactive = false;

        }

        if (!hasDoRegister())
        {
            bShowInactive = true;
        }
        return bShowInactive;
    }


    static public String getStoreGuid()
    {
        if (KDSConst._DEBUG)
            return "123456789";
        else
            return m_storeGuid;

    }

    static public String loadStoreGuid()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        String s = pref.getString(PREF_KEY_STORE_GUID, "");
        m_storeGuid = s;
        return s;
    }

    static public String getStoreName()
    {
        return m_storeName;
    }
    static public String loadStoreName()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        String s = pref.getString(PREF_KEY_STORE_NAME, "");
        m_storeName = s;
        return s;
    }
    static public class StoreDevice
    {
        String m_guid = "";
        String m_id = "";
        String m_serial = "";
        boolean m_bEnabled = true;
        long m_updateTime = 0;//UTC seconds, 2.1.4, for update sql.
        String m_stationFunc = "";
        boolean m_bDeleted = false;
        String m_stationName = "";


        public void setDeleted(boolean bDeleted)
        {
            m_bDeleted = bDeleted;
        }

        public boolean isDeleted()
        {
            return m_bDeleted;
        }

        /**
         * 2.1.4
         * @return
         */
        public void setUpdateTime(long n)
        {
            m_updateTime = n;
        }

        /**
         * 2.1.4
         * @return
         */
        public long getUpdateTime()
        {
            return m_updateTime;
        }

        public void setEnabled(boolean bEnabled)
        {
            m_bEnabled = bEnabled;
        }

        public  boolean getEnabled()
        {
            return m_bEnabled;
        }
        public void setGuid(String guid)
        {
            m_guid = guid;
        }
        public String getGuid()
        {
            return m_guid;
        }

        public void setSerial(String serial)
        {
            m_serial = serial;
        }
        public String getSerial()
        {
            return m_serial;
        }
        public String toString()
        {
            String s =  m_id + " - " + m_serial;
            s += "                                                                     ";
            s += "                                                                     ";
            s += "                                                                     ";
            s = s.substring(0, 150);
            return s;
        }

        public void  setID(String id)
        {
            m_id = id;
        }
        public String getID()
        {
            return m_id;
        }
        public void  setStationFunc(String func)
        {
            m_stationFunc = func;
        }
        public String getStationFunc()
        {
            return m_stationFunc;
        }

        public void setStationName(String name)
        {
            m_stationName = name;
        }
        public String getStationName()
        {
            return m_stationName;
        }
    }


    static public boolean isActivationPrefKey(String key)
    {
        switch (key) {
            case PREF_KEY_ACTIVATION_GUID:
            case PREF_KEY_ACTIVATION_DATE:
            case PREF_KEY_ACTIVATION_LOST_COUNT:
            case PREF_KEY_ACTIVATION_FAILED_DATE:
            case PREF_KEY_ACTIVATION_FAILED_REASON :
            case PREF_KEY_ACTIVATION_USER_NAME:
            case PREF_KEY_ACTIVATION_PWD:
            case PREF_KEY_STORE_GUID:// = "store_guid";
            case PREF_KEY_STORE_NAME:// = "store_name";
            case PREF_KEY_ACTIVATION_OLD_USER_NAME:// = "activation_old_user_name";
                return true;
        }
        return false;
    }
    /**
     * The order sync request return OK.
     * Then, we start to send items and condiments in two request.
     * @param http
     * @param request
     */
    public void onSyncOrderResponse(ActivationHttp http, ActivationRequest request)
    {

        if (isResponseError(request.m_result))
        {
            Object obj = request.getTag();
            if (obj == null) return;
            KDSDataOrder order = (KDSDataOrder) obj;
            //fireFailEvent(request.getCommand(), ActivationRequest.ErrorType.Replace_error, getErrorMessage(request.m_result));
            //KDSUtil.showMsg(KDSApplication.getContext(),"Order sending failed: " + getErrorMessage(request.m_result) );
            m_receiver.onSyncWebReturnResult(ActivationRequest.COMMAND.Sync_orders, order.getOrderName(), SyncDataResult.Fail_reponse_error);
            KDSLog.i(TAG, KDSLog._FUNCLINE_() + "Order sending failed");
            return;
        }
        else
        {
            Object obj = request.getTag();
            if (obj == null) return;

            try {
                ArrayList ar = request.getNextStepData();
                if (ar.size() >0)
                {
                    for (int i=0; i< ar.size(); i++)
                    {
                        m_http.request((ActivationRequest)ar.get(i));
                    }
                }
                KDSDataOrder order = (KDSDataOrder) obj;
//                ActivationRequest.SyncDataFromOperation syncOp = request.getSyncDataFromOperation();
//                switch (syncOp)
//                {
//
//                    case Unknown:
//                        break;
//                    case New:
//                        postItemsRequest(m_stationID, order);
//                        postCondimentsRequest(m_stationID, order);
//                        postItemBumpsRequest(m_stationID, order,(m_stationFuncName.equals(SettingsBase.StationFunc.Expeditor.toString())), false );
//                        postCustomerRequest(m_stationID, order);
//                        break;
//                    case Bump:
//                        postItemBumpsRequest(m_stationID, order,(m_stationFuncName.equals(SettingsBase.StationFunc.Expeditor.toString())), true );
//                        break;
//                    case Unbump:
//                        postItemBumpsRequest(m_stationID, order,(m_stationFuncName.equals(SettingsBase.StationFunc.Expeditor.toString())), false );
//                        break;
//                }

                if (m_receiver != null)
                    m_receiver.onSyncWebReturnResult(ActivationRequest.COMMAND.Sync_orders, order.getOrderName(), SyncDataResult.OK);
            }
            catch (Exception e)
            {
                KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
            }

        }

    }

    /**
     * Items sync ok.
     * Send message to receiver.
     * @param http
     * @param request
     */
    public void onSyncItemsResponse(ActivationHttp http, ActivationRequest request) {
        Object obj = request.getTag();
        if (obj == null) return;
        KDSDataOrder order = (KDSDataOrder)obj;
        if (m_receiver != null)
            m_receiver.onSyncWebReturnResult(ActivationRequest.COMMAND.Sync_items, order.getOrderName(), SyncDataResult.OK);

    }

    public void onSyncCondimentsResponse(ActivationHttp http, ActivationRequest request) {
        Object obj = request.getTag();
        if (obj == null) return;
        KDSDataOrder order = (KDSDataOrder)obj;
        if (m_receiver != null)
            m_receiver.onSyncWebReturnResult(ActivationRequest.COMMAND.Sync_condiments, order.getOrderName(), SyncDataResult.OK);

    }

    /**
     * post order to web database.
     * This is local operations
     * @param order
     * @param state
     */
    public void postOrderRequest(KDSDataOrder order, ActivationRequest.iOSOrderState state, ActivationRequest.SyncDataFromOperation fromOperation)
    {
        ActivationRequest r = ActivationRequest.requestOrderSync(m_storeGuid,  order, state);
        r.setSyncDataFromOperation(fromOperation);
        ActivationRequest.SyncDataFromOperation syncOp = fromOperation;
        switch (syncOp)
        {

            case Unknown:
                break;
            case New:
                r.getNextStepData().add( ActivationRequest.requestItemsSync(m_stationID,  order ) );
                //postItemsRequest(m_stationID, order);
                //postCondimentsRequest(m_stationID, order);
                ActivationRequest req =  ActivationRequest.requestCondimentsSync(m_stationID,  order );
                if (req != null)
                r.getNextStepData().add( req );
                //postItemBumpsRequest(m_stationID, order,(m_stationFuncName.equals(SettingsBase.StationFunc.Expeditor.toString())), false );

                r.getNextStepData().add(ActivationRequest.requestItemBumpsSync(m_stationID, order,(m_stationFuncName.equals(SettingsBase.StationFunc.Expeditor.toString())), false, ItemJobFromOperations.Local_new_order ));
                //postCustomerRequest(m_stationID, order);
                r.getNextStepData().add(ActivationRequest.requestCustomerSync(m_storeGuid, order));

                break;
            case Bump:
                if (!order.isAllItemsBumpedInLocal())
                    r.getNextStepData().add(ActivationRequest.requestItemBumpsSync(m_stationID, order,(m_stationFuncName.equals(SettingsBase.StationFunc.Expeditor.toString())), true, ItemJobFromOperations.Local_bump_order ));
                //postItemBumpsRequest(m_stationID, order,(m_stationFuncName.equals(SettingsBase.StationFunc.Expeditor.toString())), true );
                break;
            case Unbump:
                if (!order.isAllItemsBumpedInLocal())
                    r.getNextStepData().add(ActivationRequest.requestItemBumpsSync(m_stationID, order,(m_stationFuncName.equals(SettingsBase.StationFunc.Expeditor.toString())), false, ItemJobFromOperations.Local_unbump_order ));
                //postItemBumpsRequest(m_stationID, order,(m_stationFuncName.equals(SettingsBase.StationFunc.Expeditor.toString())), false );
                break;
        }

        m_http.request(r);

    }

    public void postItemsRequest(String stationID, KDSDataOrder order)
    {
        ActivationRequest r = ActivationRequest.requestItemsSync(stationID,  order );
        m_http.request(r);

    }

    public void postCondimentsRequest(String stationID, KDSDataOrder order)
    {
        ActivationRequest r = ActivationRequest.requestCondimentsSync(stationID,  order );
        if (r != null)
            m_http.request(r);

    }

    public void onSyncDataHttpException(ActivationHttp http, ActivationRequest request)
    {
        System.out.println(request.m_httpResponseCode);
        //prepare for next try
        request.resetFailedTime();
        addRetryRequest(request);

        if (m_receiver != null)
        {
            Object obj = request.getTag();
            if (obj == null) return;
            KDSDataOrder order = (KDSDataOrder) obj;
            m_receiver.onSyncWebReturnResult(request.getCommand(), order.getOrderName(), SyncDataResult.Fail_Http_exception);

        }

    }
    public void onSyncDataResponseError(ActivationHttp http, ActivationRequest request)
    {
        System.out.println(request.m_httpResponseCode);
        if (m_receiver != null)
        {
            Object obj = request.getTag();
            if (obj == null) return;
            KDSDataOrder order = (KDSDataOrder) obj;
            m_receiver.onSyncWebReturnResult(request.getCommand(), order.getOrderName(), SyncDataResult.Fail_reponse_error);
        }


    }

    ArrayList<ActivationRequest> m_arFailedRequest = new ArrayList<>();
    Thread m_retryThread = null;
    Object m_locker = new Object();

    private void addRetryRequest(ActivationRequest r)
    {
        synchronized(m_locker)
        {
            m_arFailedRequest.add(r);
        }
        start();
    }
    public void start()
    {
        if (m_retryThread == null ||
            !m_retryThread.isAlive())
        {
            m_retryThread = new Thread(this);
            m_retryThread.start();
        }

    }
    public void run()
    {
        while (true)
        {
            int ncount = 0;
            synchronized (m_locker)
            {
                ncount = m_arFailedRequest.size();
                if (ncount <=0)
                    return;
            }
            ArrayList<ActivationRequest> ar = new ArrayList<>();

            for (int i=0; i< ncount; i++) {
                ActivationRequest r = m_arFailedRequest.get(i);
                if (retryFailedRequest(r))
                    ar.add(r);
            }
            for (int i=0; i< ar.size(); i++)
            {
                m_arFailedRequest.remove(ar.get(i));
            }
            ar.clear();
            try {
                Thread.sleep(RETRY_TIMEOUT);
            }
            catch (Exception e)
            {

            }

        }
    }

    final int RETRY_TIMEOUT = 5000;
    final int RETRY_MAX_COUNT = 50;
    /**
     *
     * @param r
     * @return
     *  True: try it again.
     *  false: keep it for next loop
     */
    private boolean retryFailedRequest(ActivationRequest r)
    {
        Date dtStart = r.getFailedTime();
        if (r.getRetryCount() > RETRY_MAX_COUNT)
            return true;
        TimeDog td = new TimeDog(dtStart);
        if (td.is_timeout(RETRY_TIMEOUT))
        {
            r.updateRetryCount();
            m_http.request(r);
            return true;
        }
        return false;
    }

    public boolean postNewStationInfo2Web(String stationID, String stationFunc)
    {
        StoreDevice devLicense = findMyLicense();
        if (devLicense == null)
            return false;
        if (stationID.isEmpty()) return false; //kpp1-309 Expeditor and Queue deleted at logout on premium
        ActivationRequest r = ActivationRequest.requestDeviceSync(m_storeGuid,stationID, stationFunc,devLicense);
        m_http.request(r);
        showProgressDialog(true, m_context.getString(R.string.updating_license_data));
        return true;
    }

    private String getStationFunc(JSONObject json)
    {
        try {
            String s = json.getString("function");
            return s;
        }
        catch ( Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

//    public void postItemBumpsRequest(String stationID, KDSDataOrder order, boolean bExpoStation, boolean bBumped)
//    {
//        ActivationRequest r = ActivationRequest.requestItemBumpsSync(stationID,  order, bExpoStation , bBumped);
//        m_http.request(r);
//
//    }

    public void onSyncItemBumpsResponse(ActivationHttp http, ActivationRequest request) {
        Object obj = request.getTag();
        if (obj == null) return;
        KDSDataOrder order = (KDSDataOrder)obj;
        if (m_receiver != null)
            m_receiver.onSyncWebReturnResult(ActivationRequest.COMMAND.Sync_item_bumps, order.getOrderName(), SyncDataResult.OK);

    }

    /**
     * sync single item bumped/unbumped event to backoffice
     * @param stationID
     * @param order
     * @param item
     * @param bExpoStation
     * @param bBumped

     *  In expo. when it receive prep bump order, exp has to update all its items preparation time.
     */
    public void postItemBumpRequest(String stationID,KDSDataOrder order, KDSDataItem item, boolean bExpoStation, boolean bBumped, ItemJobFromOperations fromOperation)
    {
        ActivationRequest r = ActivationRequest.requestItemBumpSync(stationID,order,  item, bExpoStation , bBumped,  fromOperation);
        m_http.request(r);
    }

    public void onSyncItemBumpResponse(ActivationHttp http, ActivationRequest request) {
        Object obj = request.getTag();
        if (obj == null) return;
        KDSDataOrder order = (KDSDataOrder)obj;
        if (m_receiver != null)
            m_receiver.onSyncWebReturnResult(ActivationRequest.COMMAND.Sync_item_bump, order.getOrderName(), SyncDataResult.OK);

    }

    static public String getTimeZone()
    {
        return TimeZone.getDefault().getID();
        //use device timezone, don't use backoffice store timezone
        //return m_timeZone;
    }

    public void postCustomerRequest(String stationID, KDSDataOrder order)
    {
        ActivationRequest r = ActivationRequest.requestCustomerSync(m_storeGuid, order );
        m_http.request(r);

    }

    private void onSyncCustomerResponse(ActivationHttp http, ActivationRequest request)
    {
        Object obj = request.getTag();
        if (obj == null) return;
        KDSDataOrder order = (KDSDataOrder)obj;
        if (m_receiver != null)
            m_receiver.onSyncWebReturnResult(ActivationRequest.COMMAND.Sync_customer, order.getOrderName(), SyncDataResult.OK);
    }

    public void fireClearDataEvent()
    {
        if (m_globalEventsReceiver != null)
            m_globalEventsReceiver.onForceClearDataBeforeLogin();
    }

    /**
     * for kpp1-173
     *
     * @param licenseGuid
     * @param stationID
     * @param stationFunc
     * @return
     */
    public boolean postNewStationInfo2Web(String licenseGuid, String stationID, String stationFunc)
    {
        if (stationID.isEmpty()) return false; //kpp1-309 Expeditor and Queue deleted at logout on premium
        StoreDevice dev = new StoreDevice();
        dev.setGuid(licenseGuid);
        dev.m_serial = getMySerialNumber();
        ActivationRequest r = ActivationRequest.requestDeviceSync(m_storeGuid,stationID, stationFunc,dev);
        m_http.request(r);
        showProgressDialog(true, m_context.getString(R.string.updating_license_data));
        return true;
    }

    static public void resetUserNamePwd()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        String oldUserName = pref.getString(PREF_KEY_ACTIVATION_USER_NAME, "");

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_KEY_ACTIVATION_USER_NAME, "");
        editor.putString(PREF_KEY_ACTIVATION_PWD, "");
        //editor.putString(PREF_KEY_STORE_GUID, ""); //keep it for comparing changes
        editor.putString(PREF_KEY_STORE_NAME, "");
        editor.putString(PREF_KEY_ACTIVATION_OLD_USER_NAME, oldUserName);

        editor.apply();
        editor.commit();
        m_storeGuid = "";
        m_storeName = "";



    }
    static boolean m_bStoreChanged = false;
    static public void checkStoreChanged()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        String s = pref.getString(PREF_KEY_STORE_GUID, "");
        String oldStoreGuid =  s;
        m_bStoreChanged = (!oldStoreGuid.equals(m_storeGuid));

    }
    static public boolean isStoreChanged()
    {
        return m_bStoreChanged;
    }
    static public void restStoreChangedFlag()
    {
        m_bStoreChanged = false;
    }

    private String getStationName(JSONObject json)
    {
        try {
            String s = json.getString("name");
            return s;
        }
        catch ( Exception e)
        {

        }
        return "";
    }

    public boolean postNewStationName2Web(String stationID, String stationName)
    {
        if (stationID.isEmpty()) return false; //kpp1-309 Expeditor and Queue deleted at logout on premium
        StoreDevice dev = findMyLicense();
        if (dev == null)
            return false;
        dev.setStationName(stationName);
        ActivationRequest r = ActivationRequest.requestDeviceSync(m_storeGuid,stationID, dev.getStationFunc(),dev);
        m_http.request(r);
        showProgressDialog(true, m_context.getString(R.string.updating_license_data));
        return true;
    }

    public boolean postNewStationInfoToWeb(String stationID, String stationFunc, String stationName)
    {
        if (stationID.isEmpty()) return false; //kpp1-309 Expeditor and Queue deleted at logout on premium
        StoreDevice devLicense = findMyLicense();
        if (devLicense == null)
            return false;
        devLicense.setStationName(stationName);
        ActivationRequest r = ActivationRequest.requestDeviceSync(m_storeGuid,stationID, stationFunc,devLicense);
        m_http.request(r);
        showProgressDialog(true, m_context.getString(R.string.updating_license_data));
        return true;
    }

    static public ArrayList<StoreDevice> getDevices()
    {
        return m_devices;
    }

    /**
     * check if this station id has been regiested in backoffice.
     *
     * @param stationNewID
     * @return
     *  true: It has been used.
     *  false: new one.
     *
     */
    static public boolean findDuplicatedDeviceIDAfterSetNewID(String stationNewID)
    {
        for (int i=0; i< m_devices.size(); i++)
        {
            if (m_devices.get(i).isDeleted()) continue;
            StoreDevice dev =m_devices.get(i);
            if (dev.getID().equals(stationNewID)) {
                String serial = dev.getSerial();
                serial = serial.toUpperCase();
                if (!serial.equals(getMySerialNumber().toUpperCase())) {
                    //the router and kds can run in same device, and they have to register individually.
                    if (KDSApplication.isRouterApp()) {
                        continue;
                    } else {
                       return true;
                    }


                }
            }

        }
        return false;

    }

    static public boolean findStation(String stationID)
    {
        for (int i=0; i< m_devices.size(); i++)
        {
            if (m_devices.get(i).isDeleted()) continue;
            StoreDevice dev =m_devices.get(i);
            if (dev.getID().equals(stationID)) {
                return true;
            }

        }
        return false;
    }

    /**
     * KPP1-248
     * The router should not occupy licences
     * @return
     */
    private int getRegisteredDevicesCount()
    {
        int ncount = 0;
        for (int i=0; i< m_devices.size(); i++)
        {
            if (m_devices.get(i).isDeleted()) continue;
            if (m_devices.get(i).getStationFunc().equals(Activation.KDSROUTER) ) continue;
            if (m_devices.get(i).getEnabled())
                ncount ++;

        }
        return ncount;
    }

    static public String loadOldUserName()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        String s = pref.getString(PREF_KEY_ACTIVATION_OLD_USER_NAME, "");
        return s;
    }

    /**
     * KPP1-296
     * 1) Syncing error while bumping/unbumping orders
     * 2) App is sending serial number instead of device guid in Sync json
     *
     * Root cause for (1) is found: app is sending an unknown column called 'kdsguid'.
     * Please remove it from the json.
     *
     * For (2), wrong data may cause reporting issues.
     * @return
     */
    static String getMyDeviceGuid()
    {

        StoreDevice dev = findMyLicense();
        if (dev == null) return "";
        return dev.getGuid();

    }

    static public void resetOldLoginUser()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(PREF_KEY_ACTIVATION_OLD_USER_NAME, "");

        editor.apply();
        editor.commit();

    }

    public enum CleaningResponse
    {
        CLEAN,
        SNOOZE,
        DISMISS,
    }
    /**

     * @param nResponse
     *      0: cleaning
     *      1: snooze
     *      2: dismiss
     */
    public void postCleaningResultResponse(CleaningResponse nResponse)
    {

        String s = nResponse.toString();

        ActivationRequest r = ActivationRequest.requestCleaningResponse(m_storeGuid,getMyDeviceGuid(), s);
        r.setCommand(ActivationRequest.COMMAND.Cleaning);
        r.setTag(s);
        m_http.request(r);

    }

    private void onCleaningHttpResponse(ActivationHttp http, ActivationRequest request)
    {
        Object obj = request.getTag();
        if (obj == null) return;
       String str = (String)obj;
        if (m_receiver != null)
            m_receiver.onSyncWebReturnResult(ActivationRequest.COMMAND.Cleaning, str, SyncDataResult.OK);
    }
    public static final int NEW_STATION_ID = 9999;
    /**
     * check if this station has been registered
     * kpp1-340
     * @return
     */
    static public String findMyRegisteredID()
    {
        StoreDevice dev =  findMyLicense();
        if (dev == null)
            return "";
        String id = dev.getID();
        if (id.equals(KDSUtil.convertIntToString(NEW_STATION_ID)))
            return "";

        return dev.getID();
    }



    /**
     * for firebase
     */
    public void postGetOrdersRequest(long minFCMTime)
    {
        Date dt = new Date();
        dt.setTime(minFCMTime);
        ActivationRequest r = ActivationRequest.requestGetOrders(m_storeGuid, dt);
        m_http.request(r);
        //showProgressDialog(true, m_context.getString(R.string.retrieve_licenses_data));
    }

    /**
     * [
     *     [
     *         "guid": 08d2dbc0-20f9-4fa6-9e87-3bd249274dc7,
     *         "update_device": <null>,
     *         "items_count": 3,
     *         "user_info": Carlos,
     *         "create_time": 1584301854,
     *         "guest_table": Uber Eats,
     *         "store_guid": 6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd,
     *         "pos_terminal": <null>,
     *         "destination": Delivery,
     *         "update_time": 1584301855,
     *         "done": 0,
     *         "smart_order_start_time": 0,
     *         "create_local_time": 1584301854,
     *         "upload_time": 0,
     *         "order_type": ONLINE,
     *         "phone": 0,
     *         "server_name": Logic Controls,
     *         "is_hidden": 0,
     *         "preparation_time": 0,
     *         "items": {
     *             beeped = 0;
     *             "build_card" = "<null>";
     *             category = Hot;
     *             condiments =     (
     *                         {
     *                     "create_local_time" = 1584301854;
     *                     "create_time" = 1584301854;
     *                     "external_id" = 1602;
     *                     guid = "be1f50f4-aa70-44cb-9f4f-95011be091ef";
     *                     "is_deleted" = 0;
     *                     "item_guid" = "a6263f24-092c-42e4-8cc6-53faed7e3e1e";
     *                     name = Sauce;
     *                     "pre_modifier" = "<null>";
     *                     "store_guid" = "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd";
     *                     "update_device" = 0;
     *                     "update_time" = 1584301855;
     *                     "upload_time" = 0;
     *                 },
     *                         {
     *                     "create_local_time" = 1584301854;
     *                     "create_time" = 1584301854;
     *                     "external_id" = 1603;
     *                     guid = "f42c7b4a-86b8-4ba1-89c2-1119033e493c";
     *                     "is_deleted" = 0;
     *                     "item_guid" = "a6263f24-092c-42e4-8cc6-53faed7e3e1e";
     *                     name = Mayonnaise;
     *                     "pre_modifier" = "<null>";
     *                     "store_guid" = "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd";
     *                     "update_device" = 0;
     *                     "update_time" = 1584301855;
     *                     "upload_time" = 0;
     *                 }
     *             );
     *             "condiments_count" = 2;
     *             "create_local_time" = 1584301854;
     *             "create_time" = 1584301854;
     *             "device_id" = 1;
     *             "external_id" = 1502;
     *             guid = "a6263f24-092c-42e4-8cc6-53faed7e3e1e";
     *             "is_deleted" = 0;
     *             "is_hidden" = 0;
     *             "is_priority" = 0;
     *             "item_bump_guid" = "3de0282a-76e4-4666-be69-43c582c77c9c";
     *             name = Hotdog;
     *             "order_guid" = "08d2dbc0-20f9-4fa6-9e87-3bd249274dc7";
     *             "pre_modifier" = "<null>";
     *             "preparation_time" = "<null>";
     *             "printed_status" = 0;
     *             quantity = 2;
     *             "ready_since_local_time" = 0;
     *             "recall_time" = "<null>";
     *             "store_guid" = "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd";
     *             "training_video" = "<null>";
     *             "transfer_from_device_id" = "<null>";
     *             "transfer_time" = "<null>";
     *             "untransfer_time" = "<null>";
     *             "update_device" = "<null>";
     *             "update_time" = 1584301855;
     *             "upload_time" = 0;
     *         },
     *         {
     *             beeped = 0;
     *             "build_card" = "<null>";
     *             category = Hot;
     *             condiments =     (
     *                         {
     *                     "create_local_time" = 1584301854;
     *                     "create_time" = 1584301854;
     *                     "external_id" = 1602;
     *                     guid = "fb2c7dbe-bfa5-485b-9599-8a25c76e0640";
     *                     "is_deleted" = 0;
     *                     "item_guid" = "3c59e7e3-8f1f-4dcf-b85b-3ff034d0ef6e";
     *                     name = Sauce;
     *                     "pre_modifier" = "<null>";
     *                     "store_guid" = "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd";
     *                     "update_device" = 0;
     *                     "update_time" = 1584301855;
     *                     "upload_time" = 0;
     *                 },
     *                         {
     *                     "create_local_time" = 1584301854;
     *                     "create_time" = 1584301854;
     *                     "external_id" = 1603;
     *                     guid = "f8c42c67-7132-479c-afbc-b5b0a2656962";
     *                     "is_deleted" = 0;
     *                     "item_guid" = "3c59e7e3-8f1f-4dcf-b85b-3ff034d0ef6e";
     *                     name = Mayonnaise;
     *                     "pre_modifier" = "<null>";
     *                     "store_guid" = "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd";
     *                     "update_device" = 0;
     *                     "update_time" = 1584301855;
     *                     "upload_time" = 0;
     *                 }
     *             );
     *             "condiments_count" = 2;
     *             "create_local_time" = 1584301854;
     *             "create_time" = 1584301854;
     *             "device_id" = 1;
     *             "external_id" = 1502;
     *             guid = "3c59e7e3-8f1f-4dcf-b85b-3ff034d0ef6e";
     *             "is_deleted" = 0;
     *             "is_hidden" = 0;
     *             "is_priority" = 0;
     *             "item_bump_guid" = "3de0282a-76e4-4666-be69-43c582c77c9c";
     *             name = Hotdog;
     *             "order_guid" = "08d2dbc0-20f9-4fa6-9e87-3bd249274dc7";
     *             "pre_modifier" = "<null>";
     *             "preparation_time" = "<null>";
     *             "printed_status" = 0;
     *             quantity = 2;
     *             "ready_since_local_time" = 0;
     *             "recall_time" = "<null>";
     *             "store_guid" = "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd";
     *             "training_video" = "<null>";
     *             "transfer_from_device_id" = "<null>";
     *             "transfer_time" = "<null>";
     *             "untransfer_time" = "<null>";
     *             "update_device" = "<null>";
     *             "update_time" = 1584301855;
     *             "upload_time" = 0;
     *         },
     *         {
     *             beeped = 0;
     *             "build_card" = "<null>";
     *             category = Hot;
     *             condiments =     (
     *                         {
     *                     "create_local_time" = 1584301854;
     *                     "create_time" = 1584301854;
     *                     "external_id" = 1601;
     *                     guid = "7b5bf755-8873-40db-8e70-6294c277fb58";
     *                     "is_deleted" = 0;
     *                     "item_guid" = "8706f1ec-c586-4f71-b5bf-fbce11876b04";
     *                     name = Medium;
     *                     "pre_modifier" = "<null>";
     *                     "store_guid" = "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd";
     *                     "update_device" = 0;
     *                     "update_time" = 1584301855;
     *                     "upload_time" = 0;
     *                 }
     *             );
     *             "condiments_count" = 1;
     *             "create_local_time" = 1584301854;
     *             "create_time" = 1584301854;
     *             "device_id" = 1;
     *             "external_id" = 1501;
     *             guid = "8706f1ec-c586-4f71-b5bf-fbce11876b04";
     *             "is_deleted" = 0;
     *             "is_hidden" = 0;
     *             "is_priority" = 0;
     *             "item_bump_guid" = "5bd96b19-bcfb-4f3f-a437-1440fc647ec8";
     *             name = "French Fries";
     *             "order_guid" = "08d2dbc0-20f9-4fa6-9e87-3bd249274dc7";
     *             "pre_modifier" = "<null>";
     *             "preparation_time" = "<null>";
     *             "printed_status" = 0;
     *             quantity = 1;
     *             "ready_since_local_time" = 0;
     *             "recall_time" = "<null>";
     *             "store_guid" = "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd";
     *             "training_video" = "<null>";
     *             "transfer_from_device_id" = "<null>";
     *             "transfer_time" = "<null>";
     *             "untransfer_time" = "<null>";
     *             "update_device" = "<null>";
     *             "update_time" = 1584301855;
     *             "upload_time" = 0;
     *         }
     *         )
     *         , "is_deleted": 0,
     *         "is_priority": 0,
     *         "external_id": 839,
     *         "customer_guid": <null>
     *     ]
     * ]
     * @param http
     * @param request
     */
    public void onActivationResponseGetOrders(ActivationHttp http, ActivationRequest request)
    {

        //showProgressDialog(false, "");
        if (isResponseError(request.m_result))
        {
            fireActivationFailEvent(request.getCommand(), ActivationRequest.ErrorType.Get_Devices_error, getErrorMessage(request.m_result));
            return;
        }

        try
        {
            if (m_receiver != null) {
                ArrayList<Object> ar = new ArrayList<>();
                ar.add(request.m_result);
                m_receiver.onActivationEvent(ActivationEvent.Get_orders, ar);
            }
            //JSONArray ar = new JSONArray(request.m_result);
            //System.out.println(ar.toString());
            //m_devices.clear();
//            for (int i=0; i< ar.length() ; i++)
//            {
//                JSONObject json =(JSONObject) ar.get(i);
//                StoreDevice device =parseJsonDevice(json);
//                if (device == null) //KPP1-27
//                    continue;
//                m_devices.add(device);
//            }
//
//            checkMyActivation();

        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(), e);
            //e.printStackTrace();
        }
    }

    static long mServerTimeDifference = 0;

    /**
     * Unit is seconds!!!!
     * @return
     */
    static public long getServerTimeDifference()
    {
        return mServerTimeDifference;
    }
    static public void setServerTimeDifference(long secs)
    {
        mServerTimeDifference = secs;
    }
    /**
     * kpp1-397
     */
    public void postGetServerTimeRequest()
    {

        ActivationRequest r = ActivationRequest.requestGetServerTime();
        m_http.request(r);

    }

    /**
     * [{"server_time":1605103898}]
     * @param http
     * @param request
     */
    private void onActivationResponseServerTime(ActivationHttp http, ActivationRequest request)
    {
        if (isResponseError(request.m_result))
        {
            fireActivationFailEvent(request.getCommand(), ActivationRequest.ErrorType.Get_server_time_error, getErrorMessage(request.m_result));
            return;
        }

        try
        {
            JSONArray ar = new JSONArray(request.m_result);


            for (int i=0; i< ar.length() ; i++)
            {
                JSONObject json =(JSONObject) ar.get(i);
                long tm = json.getLong("server_time");
                Date t = new Date();
                setServerTimeDifference( tm -(long) (t.getTime()/1000) );
                break;
            }

        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(), e);
            //e.printStackTrace();
        }

    }

    public void startActivationNoEmptyUserNameAllowed(boolean bSilent,boolean bForceShowNamePwdDlg, Activity caller, String errMessage)
    {

        if (isDoLicensing()) return;// (m_bDoLicensing) return;
        setDoLicensing(true);//m_bDoLicensing = true;
        m_nSyncGetDevicesTries = 0;

        m_bSilent = bSilent;
        String userName = loadUserName();
        String password = loadPassword();
        if (userName.isEmpty() || password.isEmpty()) {
            showLoginActivity(caller, errMessage);
        }
        else
        {
            if ( !bForceShowNamePwdDlg) {
                postLoginRequest(userName, password);
                setDoLicensing(false); //kpp1-368
            }
            else
                showLoginActivity(caller, errMessage);
        }
    }

}
