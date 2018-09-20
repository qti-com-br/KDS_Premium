package com.bematechus.kds;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;


import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by David.Wong on 2018/7/10.
 * Rev:
 */
public class Activation implements ActivationHttp.ActivationHttpEvent {

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
    static final String TAG = "ACTIVATION";
    static final int HOUR_MS =3600000;
    public interface ActivationEvents
    {
        public void onActivationSuccess();
        public void onActivationFail(ActivationRequest.COMMAND stage,ActivationRequest.ErrorType errType, String failMessage);
    }

    ActivationHttp m_http = new ActivationHttp();

    private String m_storeGuid = "";


    private String m_myMacAddress = "";
    private String m_stationID = "1";

    private int m_nMaxLicenseCount = 0;
    ActivationEvents m_receiver = null;

    boolean m_bSilent = false;
    private ArrayList<StoreDevice> m_devices = new ArrayList<>();

    Context m_context = null;

    boolean m_bDoLicensing = false;

    int m_nSyncGetDevicesCount = 0; //record the loop count. Prevent dead loop.

    public Activation(Context context)
    {
        m_context = context;
        m_http.setReceiver(this);
    }

    public void setStationID(String stationID)
    {
        m_stationID = stationID;
    }
    public void setMacAddress(String mac)
    {
        m_myMacAddress = mac;
       // m_myMacAddress = "11.123456789";//test
    }
    public void setEventsReceiver(ActivationEvents receiver)
    {
        m_receiver = receiver;
    }
    public Activation()
    {
        m_http.setReceiver(this);
    }
    public void onHttpResponse(ActivationHttp http, ActivationRequest request)
    {
        if (request.m_httpResponseCode == ActivationHttp.HTTP_OK) {
            switch (request.m_command) {
                case Unknown:
                    return;
                case Login:
                    onActivationResponseLogin(http, request);
                    break;
                case Sync:
                    onActivationResponseSync(http, request);
                    break;
                case Get_settings:
                    onActivationResponseGetSettings(http, request);
                    break;
                case Get_devices:
                    onActivationResponseGetDevices(http, request);
                    break;
            }
        }
        else if (request.m_httpResponseCode == ActivationHttp.HTTP_Exception)
        {//activatioinhttp code error/exception
            onActivationHttpException(http, request);
        }
        else
        {//http server return error code
            onActivationResponseError(http, request);
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
        String s = response.toUpperCase();
        if ( s.indexOf("\"ERROR\":NULL") >=0)
            return false;

        return (s.indexOf("\"ERROR\":")>=0);
    }
    /**
     * [{"store_guid":"4220e7ee-dcdf-46d9-ae6b-565d228d6e2d"}]
     * @param http
     * @param request
     */
    public void onActivationResponseLogin(ActivationHttp http, ActivationRequest request)
    {
        showProgressDialog(false, "");
        String response = request.m_result;
        if (isResponseError(response))
        {
            fireFailEvent(request.getCommand(), ActivationRequest.ErrorType.UserName_Password, getErrorMessage(response));
            resetUserNamePassword();
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(response);
            if (jsonArray.length()<=0) return;
            JSONObject json = (JSONObject) jsonArray.get(0);

            String store_guid = json.getString("store_guid");
            m_storeGuid = store_guid;
            System.out.println(m_storeGuid);

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
            fireFailEvent(request.getCommand(), ActivationRequest.ErrorType.Sync_error, getErrorMessage(request.m_result));
            return;
        }
        try
        {
            m_nSyncGetDevicesCount ++;
            if (m_nSyncGetDevicesCount > MAX_TRY_COUNT)
            {
                fireFailEvent(request.getCommand(), ActivationRequest.ErrorType.Sync_error, m_context.getString(R.string.cannot_sync_license_data));
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
            fireFailEvent(request.getCommand(), ActivationRequest.ErrorType.Get_Settings_error, getErrorMessage(request.m_result));
            return;
        }
        try
        {
            JSONArray ar = new JSONArray(request.m_result);

            if (ar.length()<=0) return;
            JSONObject json = (JSONObject) ar.get(0);
            int ncount = json.getInt("licenses_quantity");
            m_nMaxLicenseCount = ncount;

            System.out.println(ar.toString());

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
            fireFailEvent(request.getCommand(), ActivationRequest.ErrorType.Get_Devices_error, getErrorMessage(request.m_result));
            return;
        }

        try
        {
            JSONArray ar = new JSONArray(request.m_result);
            System.out.println(ar.toString());
            m_devices.clear();
            for (int i=0; i< ar.length() ; i++)
            {
                JSONObject json =(JSONObject) ar.get(i);
                StoreDevice device =parseJsonDevice(json);
                m_devices.add(device);
            }

            checkMyActivation();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private StoreDevice findMyLicense()
    {
        for (int i=0; i< m_devices.size(); i++)
        {
            String serial = m_devices.get(i).getSerial();
            serial = serial.toUpperCase();
            if (serial.equals(m_myMacAddress))
                return m_devices.get(i);

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
                fireFailEvent(ActivationRequest.COMMAND.Get_devices, ActivationRequest.ErrorType.License_disabled,m_context.getString(R.string.license_deactivated));
                return;
            }
            else {
                fireSuccessEvent();
                return;
            }
        }

        //no valid
        if (m_nMaxLicenseCount <=0 ||
                (getEnabledDevicesCount() >= m_nMaxLicenseCount ))//2.1.2
        {
            fireFailEvent(ActivationRequest.COMMAND.Get_devices, ActivationRequest.ErrorType.No_valid_license,  m_context.getString(R.string.no_license_available));
            return;
        }

        //register me now
        if (m_devices.size()<=0)
        {
            if (m_nSyncGetDevicesCount > MAX_TRY_COUNT) {
                fireFailEvent(ActivationRequest.COMMAND.Sync, ActivationRequest.ErrorType.Sync_error,m_context.getString(R.string.cannot_sync_license_data));
                return;
            }
            postSyncMac("",m_stationID, m_myMacAddress, null);
            return;
        }

        if (m_nSyncGetDevicesCount>0)
            fireFailEvent(ActivationRequest.COMMAND.Sync, ActivationRequest.ErrorType.Sync_error,"Sync data error, try again!");

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

        ActivationRequest r = ActivationRequest.createLoginRequest(userName, pwd);
        m_http.request(r);
        showProgressDialog(true, m_context.getString(R.string.logining));
    }

    public void postGetSettingsRequest()
    {
        ActivationRequest r = ActivationRequest.createGetSettingsRequest(m_storeGuid);
        m_http.request(r);
        showProgressDialog(true, m_context.getString(R.string.retrieve_store_settings));
    }

    public void postGetDevicesRequest()
    {
        ActivationRequest r = ActivationRequest.createGetDevicesRequest(m_storeGuid);
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
    public void postSyncMac(String licenseGuid,String stationID, String macAddress, StoreDevice dev)
    {
        ActivationRequest r = ActivationRequest.createSyncMacRequest(m_storeGuid, stationID, licenseGuid, macAddress, dev);
        m_http.request(r);
        showProgressDialog(true, m_context.getString(R.string.updating_license_data));
    }

    public void onActivationResponseError(ActivationHttp http, ActivationRequest request)
    {
        showProgressDialog(false, "");
        System.out.println(request.m_httpResponseCode);
        if (request.m_httpResponseCode == 301)
            resetUserNamePassword();
        if (request.m_httpResponseCode == 404)
        {
            //int ncount = recordLostInternetCount();

        }

        fireFailEvent(request.getCommand(), ActivationRequest.ErrorType.Http_error_code, "Http response error code =" + request.m_httpResponseCode);

    }

    public void onActivationHttpException(ActivationHttp http, ActivationRequest request)
    {
        showProgressDialog(false, "");
        System.out.println(request.m_httpResponseCode);
        fireFailEvent(request.getCommand(), ActivationRequest.ErrorType.Http_exception, request.getResult());

    }

    public void fireSuccessEvent()
    {
        m_bDoLicensing = false;
        StoreDevice dev = findMyLicense();

        String guid = "";
        if (dev != null)
            dev.getGuid();
        saveActivationGuid(guid);
        resetFailedCount(); //reset this counter!
        saveLastFailedReason(ActivationRequest.ErrorType.OK);
        if (m_receiver != null)
            m_receiver.onActivationSuccess();
    }
    public void fireFailEvent(ActivationRequest.COMMAND stage,ActivationRequest.ErrorType errType, String strMessage)
    {
        m_bDoLicensing = false;
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
            if (getEnabledDevicesCount() >= m_nMaxLicenseCount)
                showDevicesInList(view, false);
            else
                showDevicesInList(view, false);

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
            if (m_devices.get(i).getEnabled())
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
                if (!m_devices.get(i).getEnabled())
                    ar.add(m_devices.get(i));
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(m_context, R.layout.activation_list_item,(List) ar);//m_devices);
        lst.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * choose how to register this station
     */
    private void showRegisterOptionDlg()
    {
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


        AlertDialog d = new AlertDialog.Builder(context)
                .setPositiveButton(context.getString(R.string.str_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        AlertDialog dlg = (AlertDialog)dialog;

                        RadioButton txtAddNew = (RadioButton) dlg.findViewById(R.id.rbAddNew);

                        //RadioButton txtReplace = (RadioButton) dlg.findViewById(R.id.rbReplace);

                        afterSelectedRegisterOption((View)txtAddNew.getTag());

                    }
                })
                .setTitle( context.getString(R.string.activation))
                .setNegativeButton(context.getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        afterCancelRegisterOptionDlg();
                    }
                })
                .create();
        d.setView(view);
        d.show();
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);
        //init gui
        if (getEnabledDevicesCount() >= m_nMaxLicenseCount) {
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
            postSyncMac("", m_stationID, m_myMacAddress, null);
        }
        else
        {
            ListView lst = (ListView) view.findViewById(R.id.lstData);
            StoreDevice dev = findSelectedLicense(lst);
            if (dev == null) {
                //Toast.makeText(KDSApplication.getContext(), "No selected item", Toast.LENGTH_LONG).show();
                fireFailEvent(ActivationRequest.COMMAND.Sync, ActivationRequest.ErrorType.No_selected_license_to_replace,  m_context.getString(R.string.no_selected_license_to_replace));
            }
            else
                postSyncMac(dev.getGuid(),m_stationID, m_myMacAddress, dev);
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
        fireFailEvent(ActivationRequest.COMMAND.Sync, ActivationRequest.ErrorType.Cancel_license_options, "Canceled");
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
        String s = pref.getString("activation_user_name", "");
        return s;
    }

    public void saveUserNamePwd(String userName, String pwd)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("activation_user_name", userName);
        editor.putString("activation_password", pwd);
        editor.apply();
        editor.commit();


    }
    static public String loadPassword()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        String s = pref.getString("activation_password", "");
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
        int n = pref.getInt("activation_fail_reason", ActivationRequest.ErrorType.OK.ordinal());
        ActivationRequest.ErrorType e = ActivationRequest.ErrorType.values()[n];
        return e;


    }

    public void saveLastFailedReason(ActivationRequest.ErrorType e)
    {
        int n = e.ordinal();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("activation_fail_reason", n);
        editor.apply();
        editor.commit();

    }

    static int MAX_LOST_COUNT = 120;
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
        int ncount = pref.getInt("activation_lost", 0);
        return ncount;
    }

    static long LOST_COUNT_INTERVAL =Activation.HOUR_MS;// 3600000L; //1 hour
    public int updateFailedCount()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        int ncount = pref.getInt("activation_lost", 0);
        long nTime = pref.getLong("failed_date", 0);

        Date dt = new Date();
        long nDelay = dt.getTime() - nTime;
        nDelay = Math.abs(nDelay);
        if (nDelay <LOST_COUNT_INTERVAL)
            return ncount;
        if (nTime ==0) //first time
            ncount ++;
        else
            ncount += (nDelay/LOST_COUNT_INTERVAL);

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("activation_lost", ncount);
        editor.putLong("failed_date", dt.getTime());

        editor.apply();
        editor.commit();
        return ncount;
    }

    public int resetFailedCount()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("activation_lost", 0);
        editor.putLong("failed_date", 0);
        editor.apply();
        editor.commit();
        return 0;
    }

    public void saveActivationGuid(String guid)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("activation_guid", guid);
        Date dt = new Date();

        editor.putLong("activation_date",dt.getTime());
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
    public void startActivation(boolean bSilent,boolean bForceShowNamePwdDlg, Activity caller)
    {
        if (m_bDoLicensing) return;
        m_bDoLicensing = true;
        m_nSyncGetDevicesCount = 0;

        m_bSilent = bSilent;
        String userName = loadUserName();
        String password = loadPassword();
//        userName = USER_NAME;
//        password = PASSWORD;
        if (userName.isEmpty() || password.isEmpty()) {
            if (m_bSilent) {
                updateFailedCount();
                m_bDoLicensing = false;
                fireFailEvent(ActivationRequest.COMMAND.Login,  ActivationRequest.ErrorType.UserName_Password, "No valid username and password");
                return;
            }
            showLoginActivity(caller);

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
            if ( !bForceShowNamePwdDlg)
                postLoginRequest(userName, password);
            else
                showLoginActivity(caller);
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

    public void showLoginActivity(Activity caller)
    {
        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Enter");
        m_bDoLicensing = true;
        Intent intent = new Intent(caller, ActivityLogin.class);

        intent.putExtra("func", KDSConst.SHOW_LOGIN);
        intent.putExtra("id", m_stationID);
        intent.putExtra("mac", m_myMacAddress);
        caller.startActivityForResult(intent, KDSConst.SHOW_LOGIN);

        KDSLog.i(TAG,KDSLog._FUNCLINE_() + "Exit");
    }

    class StoreDevice
    {
        String m_guid = "";
        String m_id = "";
        String m_serial = "";
        boolean m_bEnabled = true;
        long m_updateTime = 0;//UTC seconds, 2.1.4, for update sql.

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
    }



}
