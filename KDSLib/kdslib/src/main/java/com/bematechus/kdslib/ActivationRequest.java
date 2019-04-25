package com.bematechus.kdslib;

//import com.bematechus.kdslib.KDSUtil;

import com.bematechus.kdslib.BuildVer;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.sql.Time;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TimeZone;
import java.util.UUID;

/**
 * For Table tracker feature.
 * this create he http request string.
 *
 *
 * The command samples:

 [{"tok":"c0a6r1l1o9sL6t2h4gjhak7hf3uf9h2jnkjdq37qh2jk3fbr1706"},{"req":"LOGIN","password":"123456","username":"david2"}]

 [{"tok":"c0a6r1l1o9sL6t2h4gjhak7hf3uf9h2jnkjdq37qh2jk3fbr1706"},{"req":"GET_SETTINGS","store_guid":"bae3a89e-c521-46eb-8196-c63d2b16baa7"}]

 [{"tok":"c0a6r1l1o9sL6t2h4gjhak7hf3uf9h2jnkjdq37qh2jk3fbr1706"},{"req":"GET_DEVICES","store_guid":"bae3a89e-c521-46eb-8196-c63d2b16baa7"}]

 [{"tok":"c0a6r1l1o9sL6t2h4gjhak7hf3uf9h2jnkjdq37qh2jk3fbr1706"},{"data":[{"bump_transfer_device_id":"0","xml_order":"2","screen_id":"1","screen_size":"0","enable":"1","split_screen_child_device_id":"0","split_screen_parent_device_id":"0","function":"'EXPEDITOR'","id":"1","guid":"'e7b6b9bb-5f82-4511-97c1-6eb21f900e0f'","is_deleted":"0","update_time":"1534305312","store_guid":"'bae3a89e-c521-46eb-8196-c63d2b16baa7'","name":"'1'","login":"0","license":"1","serial":"'000000000039'","line_display":"0","parent_id":"0","update_device":"''"}],"req":"SYNC","entity":"devices"}]
 * Remote database https://dev.mysql.com/downloads/workbench/5.2.html.
 * Database connection:
 * Host: kds-dev.cz2l6cajeudq.us-west-2.rds.amazonaws.com
 * User: Bematech
 * Pass: %Bematech11714%
 * Database: KDSDevPremium
 */
public class ActivationRequest extends HttpBase.HttpRequestBase {

    static final String TAG = "ActivationRequest";
    /**
     *
     * Management
     * Web domain: http://kitchengous.com
     Login: bematech@kitchengous.com
     Password:098765

     //test store
     storename: lci test
     user: david
     pwd: 123456
     */

    //test server api
    static final String API_URL_TEST = "http://kitchengous.com/api/apiKDS";// "http://kdsgo.com/api/apiKDS";//http://kdsgo.com/login"; //fixed
    //production server api
    static final String API_URL_PRODUCTION = "https://kdsgo.com/api/apiKDS";

    //test db url
    static final String DB_URL_TEST = "http://54.70.214.221/api/apiKDS/Premium";

    static String API_URL = API_URL_TEST;

    //Web database sync feature.
    //DEV:http://54.70.214.221/api/apiKDS/Premium
    //STAGE: http://kitchengous.com/api/apiKDS/Premium
    //PROD: https://kdsgo.com/api/apiKDS/Premium
    static String DB_URL = DB_URL_TEST;
    //
    public static final String TOKEN = "c0a6r1l1o9sL6t2h4gjhak7hf3uf9h2jnkjdq37qh2jk3fbr1706"; //fixed

    //private Object m_tag = null;
    private TARGET m_target = TARGET.api;


    public enum TARGET
    {
        api, //for api data sync, see api_url
        db, //for database sync. see db_url
    }

    public enum SyncDataFromOperation
    {
        Unknown,
        New,
        Bump,
        Unbump,


    }

    public enum COMMAND
    {
        Unknown,
        Login,
        Sync_devices,
        Get_settings,
        Get_devices,
        Replace,
        SMS,
        Sync_orders, //KPP1-41
        Sync_items,
        Sync_condiments,
        Sync_item_bumps,
        Sync_item_bump,

    }

    public enum ErrorType
    {
        OK,
        UserName_Password,
        Http_error_code,
        Http_exception,
        Sync_error,
        Get_Settings_error,
        Get_Devices_error,
        No_valid_license,
        License_disabled,
        No_selected_license_to_replace,
        Cancel_license_options,
        Replace_error,
    }

    COMMAND m_command = COMMAND.Unknown;
    /**********************************************************************************************/

    public void setCommand(COMMAND cmd)
    {
        m_command = cmd;
    }

    public COMMAND getCommand()
    {
        return m_command;
    }

    public ActivationRequest()
    {
        //if (KDSConst._DEBUG)
//        if (BuildVer.isDebug())
//            this.URL = URL_TEST;
//        else
//            this.URL = URL_PRODUCTION;
        this.API_URL = KDSApplication.getContext().getString(R.string.api_url);// URL_TEST;
        this.DB_URL = KDSApplication.getContext().getString(R.string.db_url);
        reset();
    }



    /**
     *
     {
     {
     “tok” : <String>
     },
     {
     "req" : "LOGIN",
     "username" : <String>,
     "password" : <String>
     }
     }
     */
    static public ActivationRequest createLoginRequest( String name, String password )
    {
        String auth = TOKEN;
        String userName = name;//USER_NAME;
        String pwd = password;//PASSWORD;

        JSONArray arJson = new JSONArray();
        arJson.put(getJsonObj("tok", auth) );
        JSONObject json = getJsonObj("req", "LOGIN");
        try {

            json.put("username", userName);
            json.put("password", pwd);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        arJson.put(json);
        String str = arJson.toString();

        ActivationRequest r = new ActivationRequest();
        r.setParams( str );
        r.setCommand( COMMAND.Login );
        return r;


    }


    static public ActivationRequest createGetSettingsRequest( String store_guid)
    {
        String auth = TOKEN;
        JSONArray arJson = new JSONArray();
        arJson.put(getJsonObj("tok", auth) );
        JSONObject json = getJsonObj("req", "GET_SETTINGS");
        try {

            json.put("store_guid",store_guid );
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        arJson.put(json);
        String str = arJson.toString();

        ActivationRequest r = new ActivationRequest();
        r.setParams( str );
        r.setCommand( COMMAND.Get_settings );
        return r;


    }

    static public ActivationRequest createGetDevicesRequest( String store_guid)
    {
        String auth = TOKEN;
        JSONArray arJson = new JSONArray();
        arJson.put(getJsonObj("tok", auth) );
        JSONObject json = getJsonObj("req", "GET_DEVICES");
        try {

            json.put("store_guid",store_guid );
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        arJson.put(json);
        String str = arJson.toString();

        ActivationRequest r = new ActivationRequest();
        r.setParams( str );
        r.setCommand( COMMAND.Get_devices );
        return r;


    }


    static public JSONObject getJsonObj(String name, String value)
    {
        JSONObject jsonobj = new JSONObject(new LinkedHashMap());
        try {
            jsonobj.put(name, value);
            return jsonobj;
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }

    static public JSONObject parseString(String s)
    {
        try {
            JSONObject obj = new JSONObject(s);
            return obj;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }


    /**
     *
     * @param store_guid
     * @param stationID
     * @param licenseGuid
     * @param macAddress
     * @param dev
     *  2.1.4 add for update_time value.
     *          If it is update sql, I need its value to do update.
     * @return
     */
    static public ActivationRequest createSyncNewMacRequest( String store_guid,String stationID,String stationFunc, String licenseGuid, String macAddress,Activation.StoreDevice dev)
    {
        long lastUpdateTime = -1;
        if (dev != null)
            lastUpdateTime = dev.getUpdateTime();
        ActivationRequest r = createSyncRequest(COMMAND.Sync_devices,"devices",createNewMacJson(store_guid,stationID,stationFunc, licenseGuid, macAddress, lastUpdateTime) );
        return r;
//       // String strJson = "[{\"tok\":\"c0a6r1l1o9sL6t2h4gjhak7hf3uf9h2jnkjdq37qh2jk3fbr1706\"},{ \"entity\":\"devices\",\"req\":\"SYNC\",\"data\":";
//
//        String auth = TOKEN;
//        JSONArray arJson = new JSONArray();
//        arJson.put(getJsonObj("tok", auth) );
//        JSONObject json = getJsonObj("req", "SYNC");
//        try {
//
//            json.put("entity","devices" );
//            long lastUpdateTime = -1;
//            if (dev != null)
//                lastUpdateTime = dev.getUpdateTime();
//
//            json.put("data", createSyncMacJson(store_guid,stationID, licenseGuid, macAddress, lastUpdateTime));
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//
////        JSONArray j = createSyncMacJson(store_guid, licenseGuid, macAddress);
////        strJson += j.toString();
////        strJson +="}]";
//
//        arJson.put(json);
//        String str = arJson.toString();
//        //str = strJson;
////        str = "[{\n" +
////                "  \"tok\" : \"c0a6r1l1o9sL6t2h4gjhak7hf3uf9h2jnkjdq37qh2jk3fbr1706\"\n" +
////                "},{\n" +
////                "    \"entity\": \"devices\",\n" +
////                "    \"req\": \"SYNC\",\n" +
////                "    \"data\": [{\n" +
////                "        \"bump_transfer_device_id\" : \"0\",\n" +
////                "        \"create_time\" : \"1530217280\",\n" +
////                "        \"enable\" : \"1\",\n" +
////                "        \"function\" : \"'EXPEDITOR'\",\n" +
////                "        \"guid\" : \"'4220e7ee-dcdf-46d9-ae6b-565d228d620'\",\n" +
////                "        \"id\" : \"2\",\n" +
////                "        \"is_deleted\" : \"0\",\n" +
////                "        \"license\" : \"1\",\n" +
////                "        \"line_display\" : \"0\",\n" +
////                "        \"login\" : \"0\",\n" +
////                "        \"name\" : \"'hello22'\",\n" +
////                "        \"parent_id\" : \"0\",\n" +
////                "        \"screen_id\" : \"1\",\n" +
////                "        \"screen_size\" : \"0\",\n" +
////                "        \"serial\" : \"'1223'\",\n" +
////                "        \"split_screen_child_device_id\" : \"0\",\n" +
////                "        \"split_screen_parent_device_id\" : \"0\",\n" +
////                "        \"store_guid\" : \"'4220e7ee-dcdf-46d9-ae6b-565d228d62d'\",\n" +
////                "        \"update_device\" : \"''\",\n" +
////                //"        \"update_time\" : \"1531428990.865947\",\n" +
////                "        \"update_time\" : \"1530217280\",\n" +
////                "        \"xml_order\" : \"2\"\n" +
////                "    }]\n" +
////                "\n" +
////                "}]";
//
//        ActivationRequest r = new ActivationRequest();
//        r.setParams( str );
//        r.setCommand( COMMAND.Sync );
//        return r;


    }

    /**
     * New command
     * @param store_guid
     * @param licenseGuid
     * @param macAddress
     * @return
     */
    static public ActivationRequest createSyncReplaceMacRequest( String store_guid, String licenseGuid, String macAddress)
    {

        String auth = TOKEN;
        JSONArray arJson = new JSONArray();
        arJson.put(getJsonObj("tok", auth) );
        JSONObject json = getJsonObj("req", "DEVICE_REPLACE");

        try {
            json.put("store_guid", store_guid );
            json.put("device_guid", licenseGuid );
            json.put("device_serial", macAddress );

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        arJson.put(json);
        String str = arJson.toString();

        ActivationRequest r = new ActivationRequest();
        r.setParams( str );
        r.setCommand( COMMAND.Replace );
        return r;


    }


//    static public String createNewGUID()
//    {
//
//        String s = UUID.randomUUID().toString();//create new GUID
//        //s = s.replaceAll("-", "");
//        return s;
//    }

    /**
     * JSON for SYNC device: comment after// in each one, all field are requires and fields that are highlight are the one we need to set otherwise, leave it as defult;
     [{
     "tok" : "c0a6r1l1o9sL6t2h4gjhak7hf3uf9h2jnkjdq37qh2jk3fbr1706"
     },{
     "entity": "devices",
     "req": "SYNC",
     "data": [{
     "bump_transfer_device_id" : "0",  // feature option, leave it as 0.
     "create_time" : "1530217280", //store create time in ms which is upload time using SYNC
     "enable" : "1",                           //feature option, leave as 1.
     "function" : "'EXPEDITOR'", //feature option, function of the station; we don't need this now.
     "guid" : "'13-113'",                   //unique ID to this store, kds need to remember this id, primary key in device table
     "id" : "2",                                     //Station ID; we don't need this now.
     "is_deleted" : "0",                   //Mark this device as deleted, leave it as 0.
     "license" : "1",                           //License status, leave it as 1.
     "line_display" : "0",                 //feature option, leave it as 0.
     "login" : "0",                               //feature option, leave it as 0.
     "name" : "'hello22'",               //store name, use station #,eg: station1, station2
     "parent_id" : "0",                     //feature option, leave it as 0.
     "screen_id" : "1",                     //feature option, leave it as 1.
     "screen_size" : "0",                 //feature option, leave it as 0.
     "serial" : "'1223'",                     //serial, in our case it is Mac_address
     "split_screen_child_device_id" : "0",              //feature option, leave it as 0.
     "split_screen_parent_device_id" : "0",          //feature option, leave it as 0.
     "store_guid" : "'4220e7ee-dcdf-46d9-ae6b-565d228d62d'",                  //store id from Login API
     "update_device" : "''",                           //feature option, leave it as null.
     "update_time" : "1531428990.865947", // time in ms which calling SYNC
     "xml_order" : "2"                     //feature option, leave it as 2.

     }]

     }]

     /////////////////////get devices return this
     {"bump_transfer_device_id":0,
     "last_connection_time":0,
     "screen_id":1,
     "xml_order":2,
     "screen_size":0,
     "enable":1,
     "split_screen_child_device_id":0,
     "split_screen_parent_device_id":0,
     "function":"EXPEDITOR",
     "id":2,
     "guid":"123-113",
     "expeditor":null,
     "is_deleted":0,
     "update_time":1531410791,
     "store_guid":"4220e7ee-dcdf-46d9-ae6b-565d228d6e2d",
     "name":"hello1",
     "create_time":1530217280,
     "login":0,
     "license":1,
     "serial":"1223",
     "line_display":0,
     "update_device":"",
     "parent_id":0},

     ////////////this ok
     [{
     "tok" : "c0a6r1l1o9sL6t2h4gjhak7hf3uf9h2jnkjdq37qh2jk3fbr1706"
     },{
     "entity": "devices",
     "req": "SYNC",
     "data": [{
     "bump_transfer_device_id" : "0",
     "create_time" : "1530217280",
     1531697520723
     "enable" : "1",
     "function" : "'EXPEDITOR'",
     "guid" : "'13-113'",
     "id" : "2",
     "is_deleted" : "0",
     "license" : "1",
     "line_display" : "0",
     "login" : "0",
     "name" : "'hello22'",
     "parent_id" : "0",
     "screen_id" : "1",
     "screen_size" : "0",
     "serial" : "'1223'",
     "split_screen_child_device_id" : "0",
     "split_screen_parent_device_id" : "0",
     "store_guid" : "'4220e7ee-dcdf-46d9-ae6b-565d228d62d'",
     "update_device" : "''",
     "update_time" : "1531428990.865947",
     "xml_order" : "2"
     }]

     }]

     Devices table definitions
     DROP TABLE IF EXISTS `devices`;

//    CREATE TABLE `devices` (
//            `guid` varchar(36) NOT NULL,
//  `serial` varchar(36) DEFAULT NULL,
//  `store_guid` varchar(36) DEFAULT NULL,
//  `id` int(11) NOT NULL,
//  `name` varchar(45) NOT NULL,
//  `function` varchar(45) NOT NULL,
//  `parent_id` int(11) DEFAULT NULL,
//  `expeditor` varchar(191) DEFAULT NULL,
//  `xml_order` int(11) DEFAULT NULL,
//  `enable` tinyint(4) NOT NULL,
//  `bump_transfer_device_id` int(11) DEFAULT NULL,
//  `line_display` tinyint(4) DEFAULT NULL,
//  `screen_id` tinyint(4) DEFAULT NULL,
//  `screen_size` int(11) DEFAULT NULL,
//  `split_screen_child_device_id` int(11) DEFAULT NULL,
//  `split_screen_parent_device_id` int(11) DEFAULT NULL,
//  `create_time` int(10) NOT NULL DEFAULT '0',
//            `update_time` int(10) NOT NULL DEFAULT '0',
//            `is_deleted` tinyint(4) NOT NULL DEFAULT '0',
//            `update_device` varchar(36) DEFAULT NULL,
//  `login` tinyint(4) NOT NULL DEFAULT '0',
//            `license` tinyint(4) NOT NULL DEFAULT '0',
//            `last_connection_time` int(10) NOT NULL DEFAULT '0',
//            `printer_address` varchar(191) DEFAULT NULL,
//  `printer_ethernet_enable` tinyint(4) DEFAULT '0',
//            `printer_name` varchar(191) DEFAULT NULL,
//  `printer_port` int(11) DEFAULT '0',
//            `printer_print_bumps` tinyint(4) DEFAULT '0',
//            `printer_print_receives` tinyint(4) DEFAULT '0',
//            `upload_time` int(10) DEFAULT '1',
//            `create_local_time` int(10) DEFAULT '0',
//            `app_version` varchar(45) DEFAULT NULL,
//  `app_version_code` int(11) NOT NULL DEFAULT '0',
//    PRIMARY KEY (`guid`)
//) ENGINE=InnoDB DEFAULT CHARSET=utf8;
     * @param store_guid
     * @param licenseGuid
     * @param macAddress
     * @return
     */
    static private JSONArray createNewMacJson(String store_guid, String stationID,String stationFunc, String licenseGuid, String macAddress, long lastUpdateTime)
    {
        //Date dt = getUTCTime();// new Date();
        long updateTime = getUTCTimeSeconds();//dt.getTime()/1000;
        if (updateTime<lastUpdateTime)
            updateTime = lastUpdateTime +1;
//        String stationFunc = "EXPEDITOR";
//        if (KDSApplication.isRouterApp())
//            stationFunc = "KDSRouter";
        return createDeviceJson(store_guid,licenseGuid, stationID, stationFunc, macAddress, updateTime);

//        JSONArray ar = new JSONArray();
//
//        String guid = licenseGuid;
//        if (licenseGuid.isEmpty())
//            guid = createNewGUID() ;
//
//        JSONObject json = getJsonObj( "guid" , "'"+guid+"'");
//
//
////        if (serverDbID.isEmpty())
////            serverDbID = "0";
////        JSONObject json = getJsonObj( "id" , serverDbID);
//
//
//        try {
//
//            Date dt = getUTCTime();// new Date();
//            long updateTime = dt.getTime()/1000;
//            if (updateTime<lastUpdateTime)
//                updateTime = lastUpdateTime +1;
//            //updateTime = lastUpdateTime -1; //debug
//
//            //the data setup by me.
////            if (licenseGuid.isEmpty())
////                json.put("guid", "'" +createNewGUID() +"'");
////
////            else
////                json.put("guid", "'" +licenseGuid +"'");
//            int n = KDSUtil.convertStringToInt(stationID, 0);
//
//            json.put("id", KDSUtil.convertIntToString(n));
//            json.put("store_guid","'" + store_guid + "'" );
//            json.put("serial", "'"+macAddress+"'");
//            json.put("update_device" , "''");
//            json.put("function" , "'EXPEDITOR'");
//            json.put("name" , "'"+stationID +"'"); //2.1.2
//            if (licenseGuid.isEmpty())
//                json.put("create_time" ,Long.toString( updateTime)); //seconds
//            json.put("update_time" ,Long.toString( updateTime)); //seconds
//            //data unused, but must have them.
//            json.put("bump_transfer_device_id", "0");
//            json.put("enable", "1" );
//            json.put("is_deleted" , "0");
//            json.put("license", "1");
//            json.put("line_display" , "0");
//            json.put("login" , "0");
//            json.put("parent_id" , "0");
//            json.put("screen_id" , "1");
//            json.put("screen_size" , "0");
//            json.put("split_screen_child_device_id" , "0");
//            json.put("split_screen_parent_device_id" , "0");
//
//            json.put("xml_order" , "2");
//
//            //KPP1-55
//            json.put("app_version" , "'" + KDSUtil.getVersionName(KDSApplication.getContext()) + "'");
//            json.put("app_version_code" , KDSUtil.convertIntToString(KDSUtil.getVersionCode(KDSApplication.getContext())));
//
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        ar.put(json);
//        return ar;
    }
    /**
     * 得到UTC时间，类型为字符串，格式为"yyyy-MM-dd HH:mm"<br />
     * 如果获取失败，返回null
     * The UTC
     * @return
     */
    private static long getUTCTimeSeconds() {
//        //StringBuffer UTCTimeBuffer = new StringBuffer();
//        // 1、取得本地时间：
//        Calendar cal = Calendar.getInstance() ;
//        // 2、取得时间偏移量：
//        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
//        // 3、取得夏令时差：
//        int dstOffset = cal.get(Calendar.DST_OFFSET);
//        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
//        //cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        //long tm = (new Date()).getTime()/1000; //date.getTime() always returns UTC time only. but date.toString() will convert that date to local timezone and will display.
        long tm = System.currentTimeMillis()/1000;
        return tm;
        //return (cal.getTime().getTime() - (zoneOffset + dstOffset))/1000;


//        TimeZone tz  = TimeZone.getDefault() ;
//        TimeZone utc = TimeZone.getTimeZone("UTC") ;
//        TimeZone.setDefault(utc);
//
//        Calendar cal = Calendar.getInstance(utc);
//        Date dt = cal.getTime();
//        TimeZone.setDefault(tz);
//        return dt;

//
//        int year = cal.get(Calendar.YEAR);
//        int month = cal.get(Calendar.MONTH)+1;
//        int day = cal.get(Calendar.DAY_OF_MONTH);
//        int hour = cal.get(Calendar.HOUR_OF_DAY);
//        int minute = cal.get(Calendar.MINUTE);
//        UTCTimeBuffer.append(year).append("-").append(month).append("-").append(day) ;
//        UTCTimeBuffer.append(" ").append(hour).append(":").append(minute) ;
//        try{
//            format.parse(UTCTimeBuffer.toString()) ;
//            return UTCTimeBuffer.toString() ;
//        }catch(Exception e)
//        {
//            e.printStackTrace() ;
//        }
//        return null ;
    }

    static public boolean needResetUsernamePassword(ErrorType errType)
    {
        switch (errType)
        {

            case OK:
            case Http_error_code:
            case Http_exception:
            case Sync_error:
            case Get_Settings_error:
            case Get_Devices_error:
            case No_valid_license:
            case License_disabled:
            case No_selected_license_to_replace:
            case Cancel_license_options:
            case Replace_error:
                return false;


            case UserName_Password:
                return true;

            default:
                return false;

        }
    }


    /**
     *
     * @param store_guid
     * @param storeName
     * @param customerPhone
     * @param orderGuid
     * @param orderSmsState
     * @return
     */
    static public ActivationRequest createSMSRequest( String store_guid,String storeName,String customerPhone, String orderGuid,int orderSmsState )
    {

        String auth = TOKEN;
        JSONArray arJson = new JSONArray();
        arJson.put(getJsonObj("tok", auth) );
        JSONObject json = getJsonObj("req", "SMS_ORDER");

        try {
            json.put("store_guid", store_guid);
            json.put("store_name", storeName);
            json.put("order_guid", orderGuid);
            json.put("order_status",KDSUtil.convertIntToString(orderSmsState));
            json.put("order_phone", customerPhone);

        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(), e );
            e.printStackTrace();
        }

        arJson.put(json);
        String str = arJson.toString();


        ActivationRequest r = new ActivationRequest();
        r.setParams( str );
        r.setCommand( COMMAND.SMS );
        return r;


    }

    /**
     * Database connection:
     * Host: kds-dev.cz2l6cajeudq.us-west-2.rds.amazonaws.com
     * User: Bematech
     * Pass: %Bematech11714%
     * Database: KDSDevPremium
     * @param command
     * @param tblName
     * @param data
     * @return
     */
    static private ActivationRequest createSyncRequest(COMMAND command, String tblName, JSONArray data)
    {

        String auth = TOKEN;
        JSONArray arJson = new JSONArray();
        arJson.put(getJsonObj("tok", auth) );
        JSONObject json = getJsonObj("req", "SYNC");
        try {

            json.put("entity",tblName );

            json.put("data", data);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        arJson.put(json);
        String str = arJson.toString();

        ActivationRequest r = new ActivationRequest();
        r.setParams( str );
        r.setCommand( command);//COMMAND.Sync );
        return r;


    }

    /**
     * sync order data to api back office.
     * Sample:
     *  Request:
     *
     * [
     *     {
     *         "tok":"c0a6r1l1o9sL6t2h4gjhak7hf3uf9h2jnkjdq37qh2jk3fbr1706"
     *     },
     *     {
     *         "data":[
     *             {
     *                 "preparation_time":"0",
     *                 "items_count":"1",
     *                 "external_id":"''",
     *                 "phone":"''",
     *                 "guest_table":"'Tbl #4'",
     *                 "is_priority":"0",
     *                 "done":"2",
     *                 "customer_guid":"''",
     *                 "pos_terminal":"'5'",
     *                 "destination":"'Fast food'",
     *                 "upload_time":"1550018592",
     *                 "server_name":"'David Wong'",
     *                 "guid":"'b39c2d4390b041be96a6b62b7e80154c'",
     *                 "is_deleted":"0",
     *                 "create_local_time":"1550018592",
     *                 "update_time":"1550018592",
     *                 "store_guid":"''",
     *                 "is_hidden":"0",
     *                 "create_time":"1550018592",
     *                 "smart_order_start_time":"0",
     *                 "update_device":"''",
     *                 "order_type":"'RUSH'"
     *             }
     *         ],
     *         "req":"SYNC",
     *         "entity":"orders"
     *     }
     * ]
     *
     *  Response:
     *
     * [
     *     {
     *         "guid":"b39c2d4390b041be96a6b62b7e80154c",
     *         "store_guid":"",
     *         "destination":"Fast food",
     *         "external_id":"",
     *         "guest_table":"Tbl #4",
     *         "is_priority":0,
     *         "items_count":1,
     *         "order_type":"RUSH",
     *         "pos_terminal":"5",
     *         "server_name":"David Wong",
     *         "user_info":null,
     *         "done":2,
     *         "create_time":1550018592,
     *         "update_time":1550018592,
     *         "upload_time":1550047392,
     *         "is_deleted":0,
     *         "update_device":"",
     *         "phone":"",
     *         "create_local_time":1550018592,
     *         "is_hidden":0,
     *         "customer_guid":"",
     *         "smart_order_start_time":0,
     *         "preparation_time":0
     *     }
     * ]
     * @param store_guid
     * @param order
     * @return
     */
    static public ActivationRequest createSyncOrderRequest( String store_guid,KDSDataOrder order, iOSOrderState state)
    {

        ActivationRequest r = createSyncRequest(COMMAND.Sync_orders,"orders",createOrderJson(store_guid, order,  state) );
        r.setTag(order);
        r.setDbTarget();
        return r;
    }

    static public ActivationRequest createSyncItemsRequest(String stationID,  KDSDataOrder order)
    {

        ActivationRequest r = createSyncRequest(COMMAND.Sync_items,"items",createItemsJson(stationID,order, order.getItems()) );
        r.setTag(order);
        r.setDbTarget();
        return r;
    }

    static public ActivationRequest createSyncCondimentsRequest(String stationID,  KDSDataOrder order)
    {
        KDSDataCondiments condiments = new KDSDataCondiments();
        for (int i=0; i< order.getItems().getCount(); i++)
        {
            KDSDataItem item = order.getItems().getItem(i);
            condiments.getComponents().addAll(item.getCondiments().getComponents());
        }


        ActivationRequest r = createSyncRequest(COMMAND.Sync_condiments,"condiments",createCondimentsJson(stationID, order,  condiments) );
        r.setTag(order);
        r.setDbTarget();
        return r;
    }


    /**
     * From iOS kds app definition
     */
    public enum iOSOrderState
    {
        New,
        Preparation,
        Done,
    }

    /**
     *
     * @param store_guid
     * @param order
     *    send this order information to web database.
     * @param state
     * @return
     * The order data json.
     */
    static private JSONArray createOrderJson(String store_guid,  KDSDataOrder order, iOSOrderState state)
    {
        JSONArray ar = new JSONArray();

        JSONObject json = getJsonObj( "guid" , "'"+order.getGUID()+"'");
        try {

            //Date dt = getUTCTime();// new Date();
            long utcTime = getUTCTimeSeconds();// dt.getTime()/1000;
            //long localTime = getLocalTime().getTime()/1000;
            long localTime = getLocalTimeSeconds();
            json.put("guid", "'" + order.getGUID() + "'");
            json.put("store_guid","'" + store_guid + "'" );
            json.put("destination", "'" + order.getDestination()+"'");
            json.put("external_id", "'"+order.getOrderName() +"'"); //KPP1-44,  The External ID should be filled in with the Order ID number
            json.put("guest_table", "'" + order.getToTable()+"'");
            json.put("is_priority", "0");
            Object obj = order.getTag();
            int nItemsCount = order.getItems().getCount();
            if (obj != null)
            {
                nItemsCount = (int)obj;
            }
            json.put("items_count", KDSUtil.convertIntToString( nItemsCount ));
            json.put("order_type", "'" + order.getOrderType()+"'");
            json.put("pos_terminal", "'" + order.getFromPOSNumber()+"'");
            json.put("server_name", "'" + order.getWaiterName()+"'");
            json.put("user_info", "'"+ order.getCustomMsg() +"'"); //KPP1-45, User info needs to be in the user_info column on the orders table
            json.put("done",   KDSUtil.convertIntToString( state.ordinal() ));
            json.put("create_time" ,Long.toString( getUTCTimeSeconds(order.getStartTime()))); //seconds
            json.put("update_time" ,Long.toString( utcTime)); //seconds
            json.put("upload_time" ,Long.toString( utcTime)); //seconds
            json.put("is_deleted", "0");
            json.put("update_device" , "'"+Activation.getMySerialNumber() + "'"); //https://bematech.atlassian.net/browse/KPP1-63
            json.put("phone", "'" + order.getSMSCustomerPhone()+"'");
            json.put("create_local_time", Long.toString( order.getStartTime().getTime()/1000)); //seconds
            json.put("is_hidden", "0");
            json.put("customer_guid", "''");
            json.put("smart_order_start_time", "0");
            json.put("preparation_time", "0");


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        ar.put(json);
        return ar;
    }

    static private JSONObject createItemJson(String stationID,KDSDataOrder order, KDSDataItem item)
    {
        JSONObject json = getJsonObj( "guid" , "'" + item.getGUID() +"'" );
        try {

            //Date dt = getUTCTime();// new Date();
            long updateTime =getUTCTimeSeconds();// dt.getTime()/1000;
            //long localTime = getLocalTime().getTime()/1000;
            long localTime = getLocalTimeSeconds();
//            if (updateTime<lastUpdateTime)
//                updateTime = lastUpdateTime +1;

            //json.put("guid", "'" + item.getGUID() + "'");
            json.put("order_guid","'" + item.getOrderGUID() + "'" );
            json.put("name", "'" + item.getDescription()+"'");
            //json.put("device_id", "0");//KPP1-48, The device_id should show the station id the item came from
            json.put("device_id", "'" + stationID + "'");//KPP1-48, The device_id should show the station id the item came from. Note: in table, it is a integer, I use string. Need to try.
            json.put("external_id", "'"+ item.getItemName()+"'");//order.getOrderName() + "'"); //KPP1-49,  External ID needs to show the item ID that item came from
            json.put("is_priority", "0");
            json.put("condiments_count", KDSUtil.convertIntToString( item.getCondiments().getCount()));
            json.put("pre_modifier", "'" + item.getModifiers().toEachLineString() +"'");//KPP1-50, The field needs to be filled in with the premodifier that is in the XML
            json.put("preparation_time", KDSUtil.convertFloatToShortString(item.getPreparationTime() )); // KPP1-51, he number should show what the time for preparation is for that item
            //json.put("recall_time", Long.toString( updateTime)); //seconds //KPP1-70
            json.put("training_video", "''");
            //json.put("transfer_from_device_id",  "'" + item.getTransferedFromStationID() +"'");//KPP1-53
//            if (!item.getTransferedFromStationID().isEmpty()) //comment it. IN KDS, we don't need it.
//                json.put("transfer_from_device_id",    item.getTransferedFromStationID() );//KPP1-53
            //json.put("transfer_time" ,Long.toString( updateTime)); //seconds //KPP1-70
            //json.put("untransfer_time" ,Long.toString( updateTime)); //seconds //KPP1-70
            json.put("beeped",  '0');
            json.put("build_card", "''");
            json.put("create_time" ,Long.toString( updateTime)); //seconds
            json.put("update_time" ,Long.toString( updateTime)); //seconds
            json.put("upload_time" ,Long.toString( updateTime)); //seconds
            json.put("is_deleted", "0");
            json.put("update_device" , "'"+Activation.getMySerialNumber() +"'");
            json.put("printed_status", "0");
            json.put("item_bump_guid", "'" + item.getGUID() +"'" ); //https://bematech.atlassian.net/browse/KPP1-64
            json.put("create_local_time", Long.toString( localTime)); //seconds
            json.put("is_hidden", "0");
            json.put("ready_since_local_time", "0");




        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return json;
    }

    static private JSONArray createItemsJson(String stationID,KDSDataOrder order, KDSDataItems items)
    {
        JSONArray ar = new JSONArray();
        int ncount = items.getCount();

        for (int i=0; i< ncount; i++)
        {
            ar.put(createItemJson(stationID,order, items.getItem(i)));
        }
        return ar;


    }


    static private JSONArray createCondimentsJson(String stationID, KDSDataOrder order, KDSDataCondiments condiments)
    {
        JSONArray ar = new JSONArray();

        for (int i=0; i< condiments.getCount(); i++)
        {
            ar.put(createCondimentJson(stationID, order, condiments.getCondiment(i)));
        }
        return ar;


    }

    static private JSONObject createCondimentJson(String stationID, KDSDataOrder order,  KDSDataCondiment condiment)
    {
        JSONObject json = getJsonObj( "guid" ,"'" +  condiment.getGUID() +"'");
        try {

            //Date dt = getUTCTime();// new Date();
            long updateTime = getUTCTimeSeconds();//dt.getTime()/1000;
            //long localTime = getLocalTime().getTime()/1000;
            long localTime = getLocalTimeSeconds();
//            if (updateTime<lastUpdateTime)
//                updateTime = lastUpdateTime +1;
            //json.put("guid", "'" + item.getGUID() + "'");
            json.put("item_guid","'" + condiment.getItemGUID() + "'" );
            json.put("external_id", "'"+condiment.getCondimentName() +"'"); //KPP1-57//https://bematech.atlassian.net/browse/KPP1-57
            json.put("name", "'" + condiment.getDescription()+"'");
            json.put("pre_modifier", "''");
            json.put("create_time" ,Long.toString( updateTime)); //seconds
            json.put("update_time" ,Long.toString( updateTime)); //seconds
            json.put("upload_time" ,Long.toString( updateTime)); //seconds
            json.put("is_deleted", "0");
            json.put("update_device" , "'"+Activation.getMySerialNumber() + "'");//KPP1-58
            json.put("create_local_time", Long.toString( localTime)); //seconds
            json.put("preparation_time",  Long.toString( updateTime));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return json;
    }



//    public void setTag(Object obj)
//    {
//        m_tag = obj;
//    }
//    public Object getTag()
//    {
//        return m_tag;
//    }

    public void setDbTarget()
    {
        m_target = TARGET.db;
    }
    public String getUrl()
    {
        switch (m_target)
        {

            case api:
                return API_URL;

            case db:
                return DB_URL;
            default:
                return API_URL;
        }
    }

    Date m_dtFailedTime = new Date();
    public void resetFailedTime()
    {
        m_dtFailedTime = new Date();
    }
    public Date getFailedTime()
    {
        return m_dtFailedTime;
    }
    int m_nRetryCount = 0;
    public void updateRetryCount()
    {
        m_nRetryCount ++;
    }
    public int getRetryCount()
    {
        return m_nRetryCount;
    }

    SyncDataFromOperation m_syncDataFromOperations = SyncDataFromOperation.Unknown;
    public void setSyncDataFromOperation(SyncDataFromOperation op)
    {
        m_syncDataFromOperations = op;
    }
    public SyncDataFromOperation getSyncDataFromOperation()
    {
        return m_syncDataFromOperations;
    }

    public void clear()
    {
        this.reset();

    }

    static public ActivationRequest createSyncDeviceInfoRequest( String store_guid,String stationID, String stationFunc, Activation.StoreDevice dev)
    {

        ActivationRequest r = createSyncRequest(COMMAND.Sync_devices,"devices",createDeviceInfoJson(store_guid,stationID, stationFunc, dev) );
        return r;
//       // String strJson = "[{\"tok\":\"c0a6r1l1o9sL6t2h4gjhak7hf3uf9h2jnkjdq37qh2jk3fbr1706\"},{ \"entity\":\"devices\",\"req\":\"SYNC\",\"data\":";
//
//        String auth = TOKEN;
//        JSONArray arJson = new JSONArray();
//        arJson.put(getJsonObj("tok", auth) );
//        JSONObject json = getJsonObj("req", "SYNC");
//        try {
//
//            json.put("entity","devices" );
//            long lastUpdateTime = -1;
//            if (dev != null)
//                lastUpdateTime = dev.getUpdateTime();
//
//            json.put("data", createSyncMacJson(store_guid,stationID, licenseGuid, macAddress, lastUpdateTime));
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//
////        JSONArray j = createSyncMacJson(store_guid, licenseGuid, macAddress);
////        strJson += j.toString();
////        strJson +="}]";
//
//        arJson.put(json);
//        String str = arJson.toString();
//        //str = strJson;
////        str = "[{\n" +
////                "  \"tok\" : \"c0a6r1l1o9sL6t2h4gjhak7hf3uf9h2jnkjdq37qh2jk3fbr1706\"\n" +
////                "},{\n" +
////                "    \"entity\": \"devices\",\n" +
////                "    \"req\": \"SYNC\",\n" +
////                "    \"data\": [{\n" +
////                "        \"bump_transfer_device_id\" : \"0\",\n" +
////                "        \"create_time\" : \"1530217280\",\n" +
////                "        \"enable\" : \"1\",\n" +
////                "        \"function\" : \"'EXPEDITOR'\",\n" +
////                "        \"guid\" : \"'4220e7ee-dcdf-46d9-ae6b-565d228d620'\",\n" +
////                "        \"id\" : \"2\",\n" +
////                "        \"is_deleted\" : \"0\",\n" +
////                "        \"license\" : \"1\",\n" +
////                "        \"line_display\" : \"0\",\n" +
////                "        \"login\" : \"0\",\n" +
////                "        \"name\" : \"'hello22'\",\n" +
////                "        \"parent_id\" : \"0\",\n" +
////                "        \"screen_id\" : \"1\",\n" +
////                "        \"screen_size\" : \"0\",\n" +
////                "        \"serial\" : \"'1223'\",\n" +
////                "        \"split_screen_child_device_id\" : \"0\",\n" +
////                "        \"split_screen_parent_device_id\" : \"0\",\n" +
////                "        \"store_guid\" : \"'4220e7ee-dcdf-46d9-ae6b-565d228d62d'\",\n" +
////                "        \"update_device\" : \"''\",\n" +
////                //"        \"update_time\" : \"1531428990.865947\",\n" +
////                "        \"update_time\" : \"1530217280\",\n" +
////                "        \"xml_order\" : \"2\"\n" +
////                "    }]\n" +
////                "\n" +
////                "}]";
//
//        ActivationRequest r = new ActivationRequest();
//        r.setParams( str );
//        r.setCommand( COMMAND.Sync );
//        return r;


    }
    static private JSONArray createDeviceInfoJson(String store_guid, String stationID, String stationFunc, Activation.StoreDevice dev)
    {
        //Date dt = getUTCTime();// new Date();
        long updateTime = getUTCTimeSeconds();//dt.getTime()/1000;
        if (updateTime<dev.getUpdateTime())
            updateTime = dev.getUpdateTime() +1;

        return createDeviceJson(store_guid,dev.m_guid, stationID, stationFunc, dev.m_serial, updateTime);

//        JSONArray ar = new JSONArray();
//
//        String guid = dev.m_guid;
//        if (guid.isEmpty()) //must been licensed
//           return ar;
//
//        JSONObject json = getJsonObj( "guid" , "'"+guid+"'");
//
//        try {
//
//            Date dt = getUTCTime();// new Date();
//            long updateTime = dt.getTime()/1000;
//            if (updateTime<dev.getUpdateTime())
//                updateTime = dev.getUpdateTime() +1;
//
//            json.put("id", stationID);
//            json.put("store_guid","'" + store_guid + "'" );
//            json.put("serial", "'"+dev.m_serial+"'"); //mac address
//            json.put("update_device" , "''");
//            json.put("function" , "'" + stationFunc + "'");
//            json.put("name" , "'"+stationID +"'"); //2.1.2
//            json.put("update_time" ,Long.toString( updateTime)); //seconds
//            //data unused, but must have them.
//            json.put("bump_transfer_device_id", "0");
//            json.put("enable", "1" );
//            json.put("is_deleted" , "0");
//            json.put("license", "1");
//            json.put("line_display" , "0");
//            json.put("login" , "0");
//            json.put("parent_id" , "0");
//            json.put("screen_id" , "1");
//            json.put("screen_size" , "0");
//            json.put("split_screen_child_device_id" , "0");
//            json.put("split_screen_parent_device_id" , "0");
//            json.put("xml_order" , "2");
//            //KPP1-55
//            json.put("app_version" , "'" + KDSUtil.getVersionName(KDSApplication.getContext()) + "'");
//            json.put("app_version_code" , KDSUtil.convertIntToString(KDSUtil.getVersionCode(KDSApplication.getContext())));
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        ar.put(json);
//        return ar;
    }

    /**
     * item_bumps fields
     * ----------
     CREATE TABLE `item_bumps` (
     `guid` varchar(36) NOT NULL,
     `status` tinyint(4) NOT NULL,
     `last_status` tinyint(4) DEFAULT NULL,
     `create_time` int(10) NOT NULL,
     `update_time` int(10) NOT NULL,
     `upload_time` int(10) DEFAULT '1',
     `is_deleted` tinyint(4) NOT NULL DEFAULT '0',
     `update_device` varchar(36) DEFAULT NULL,
     `create_local_time` int(10) DEFAULT '0',
     `prepared_local_time` int(10) DEFAULT '0',
     `done_local_time` int(10) DEFAULT '0',
     `prepared_device_id` int(11) DEFAULT '0',
     `done_device_id` int(11) DEFAULT '0',
     PRIMARY KEY (`guid`)
     ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
     */
    /**
     *
     * @param stationID
     * @param item
     * @param bExpoStation
     * @param bBumped
     *  True: bump this item
     *  False: unbump it.
     * @return
     */
    static private JSONObject createItemBumpJson(String stationID, KDSDataItem item, boolean bExpoStation, boolean bBumped)
    {


        JSONObject json = getJsonObj( "guid" , "'"+item.getGUID()+"'");

        try {

            //Date dt = getUTCTime();// new Date();
            long updateTime = getUTCTimeSeconds();//dt.getTime()/1000;
            //long localTime = (new Date()).getTime()/1000;
            //long localTime = getLocalTime().getTime()/1000;
            long localTime = getLocalTimeSeconds();
            String status = "2";//done
            if (!bBumped) status = "0";//new
            json.put("status",status );

            json.put("last_status", status); //mac address
            json.put("create_time" , Long.toString( updateTime));
            json.put("update_time" ,Long.toString( updateTime)); //seconds
            json.put("upload_time" ,Long.toString( updateTime)); //seconds
            json.put("is_deleted" , "0");
            json.put("update_device" , "'"+Activation.getMySerialNumber() +"'"); //2.1.2

            //data unused, but must have them.
            json.put("create_local_time", Long.toString( localTime));
            json.put("prepared_local_time", Long.toString( localTime) );
            json.put("done_local_time" , Long.toString( localTime));
            if (bExpoStation)
                json.put("done_device_id" , stationID);
            else
                json.put("prepared_device_id", stationID);



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return json;
    }

    static private JSONArray createDeviceJson(String store_guid,String devGuid, String stationID, String stationFunc,String mac, long updateTime)
    {
        JSONArray ar = new JSONArray();

        String guid = devGuid;
        if (guid.isEmpty()) //must been licensed. Comment it:  allow add new
            guid = createNewGUID();
//            return ar;

        JSONObject json = getJsonObj( "guid" , "'"+guid+"'");

        try {

            if (stationID.isEmpty()) stationID = "0";

            int n = KDSUtil.convertStringToInt(stationID, 0);
            //for router id
            if (stationFunc.equals(Activation.KDSROUTER))
                n = 1000;
            json.put("id", KDSUtil.convertIntToString(n));
            json.put("store_guid","'" + store_guid + "'" );
            json.put("serial", "'"+mac+"'"); //mac address
            json.put("update_device" , "''");
            json.put("function" , "'" + stationFunc + "'");
            if (stationFunc.equals(Activation.KDSROUTER))
                json.put("name" , "'"+Activation.KDSROUTER +"'"); //2.1.2
            else
                json.put("name" , "'"+stationID +"'"); //2.1.2
            json.put("update_time" ,Long.toString( updateTime)); //seconds
            //data unused, but must have them.
            json.put("bump_transfer_device_id", "0");
            json.put("enable", "1" );
            json.put("is_deleted" , "0");
            json.put("license", "1");
            json.put("line_display" , "0");
            json.put("login" , "0");
            json.put("parent_id" , "0");
            json.put("screen_id" , "1");
            json.put("screen_size" , "0");
            json.put("split_screen_child_device_id" , "0");
            json.put("split_screen_parent_device_id" , "0");
            json.put("xml_order" , "2");
            //KPP1-55//https://bematech.atlassian.net/browse/KPP1-55
            //json.put("app_version" , "'" + KDSUtil.getVersionName(KDSApplication.getContext()) + "'");
            //json.put("app_version_code" , KDSUtil.convertIntToString(KDSUtil.getVersionCode(KDSApplication.getContext())));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        ar.put(json);
        return ar;
    }

    static private JSONArray createItemBumpsJson(String stationID,KDSDataOrder order, KDSDataItems items, boolean bExpoStation, boolean bBumped)
    {
        JSONArray ar = new JSONArray();
        int ncount = items.getCount();

        for (int i=0; i< ncount; i++)
        {
            ar.put(createItemBumpJson(stationID,items.getItem(i), bExpoStation, bBumped) );
        }
        return ar;


    }

    static public ActivationRequest createSyncItemBumpsRequest(String stationID,  KDSDataOrder order, boolean bExpoStation, boolean bBumped)
    {

        ActivationRequest r = createSyncRequest(COMMAND.Sync_item_bumps,"item_bumps",createItemBumpsJson(stationID,order, order.getItems() , bExpoStation, bBumped));
        r.setTag(order);
        r.setDbTarget();
        return r;
    }

    static public ActivationRequest createSyncItemBumpRequest(String stationID,KDSDataOrder order,  KDSDataItem item, boolean bExpoStation, boolean bBumped)
    {

        ActivationRequest r = createSyncRequest(COMMAND.Sync_item_bump,"item_bumps",createSingleItemBumpJson(stationID,item , bExpoStation, bBumped));
        r.setTag(order);
        r.setDbTarget();
        return r;
    }

    static private JSONArray createSingleItemBumpJson(String stationID,KDSDataItem item, boolean bExpoStation, boolean bBumped)
    {
        JSONArray ar = new JSONArray();

        ar.put(createItemBumpJson(stationID,item, bExpoStation, bBumped) );

        return ar;


    }

    /**
     * this local time zone is from backoffice setting.
     * It is the kds local station time zone.
     * @return
     */
    private static long getLocalTimeSeconds() {

//        TimeZone tz = TimeZone.getTimeZone(Activation.getTimeZone());
//        Calendar cal = Calendar.getInstance(tz) ;

        //return cal.getTime();
        long utc = getUTCTimeSeconds();
        TimeZone tz = TimeZone.getTimeZone(Activation.getTimeZone());

        Calendar cal = Calendar.getInstance(tz) ;
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);

        int dstOffset = cal.get(Calendar.DST_OFFSET);

        return utc + (zoneOffset + dstOffset)/1000;
    }

    private static long getUTCTimeSeconds(Date dt) {

        return dt.getTime()/1000;

//        Calendar cal = Calendar.getInstance() ;
//
//        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
//
//        int dstOffset = cal.get(Calendar.DST_OFFSET);
//
//        return (dt.getTime() - (zoneOffset + dstOffset))/1000;

    }
}