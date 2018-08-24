package com.bematechus.kds;

import android.widget.Toast;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/29.
 * use this to test:
 * https://www.juhe.cn/docs/api/id/33/aid/79
 */
public class TableTrackerJSON {

    static public String TAG = "TableTrackerJSON->";
    // see https://table-tracker.readme.io/docs/api-code-glossary
    static public final int TTRC_API_MAX = 999; //for api
    static public final int TTRC_Token_deleted = 1001; //
    static public final int TTRC_Paging_Canceled = 1002; //
    static public final int TTRC_No_Tracker_Tp_Cancel = 1003; //
    static public final int TTRC_Settings_Changed = 1004; //
    static public final int TTRC_Registered_WebHook = 1005; //
    static public final int TTRC_Repeaters_Cleared = 1006; //
    static public final int TTRC_Inventory_Cleared = 1007; //
    static public final int TTRC_Activated_Device_Code = 1008; //
    static public final int TTRC_Order_Already_Started = 1009; //
    static public final int TTRC_Order_Already_Located = 1010; //
    static public final int TTRC_Activated_With_Cloud = 1011; //
    static public final int TTRC_Pairing_Enabled = 1012; //
    static public final int TTRC_Pairing_Disabled = 1013; //

    static public final int TTRC_ERR_Malformed_Auth_Header = -1; //
    static public final int TTRC_ERR_Invalid_Access_Token = -2; //
    static public final int TTRC_ERR_Time_Expired  = -3; //
    static public final int TTRC_ERR_Token_Name = -4; //
    static public final int TTRC_ERR_Unavailable_API = -5; //
    static public final int TTRC_ERR_Order_Not_Found = -6; //
    static public final int TTRC_ERR_Invalid_Order_Id = -7; //
    static public final int TTRC_ERR_No_Order_Name_Defined = -8; //
    static public final int TTRC_ERR_Invalid_Order_Name = -9; //
    static public final int TTRC_ERR_Invalid_Location_Name = -10; //
    static public final int TTRC_ERR_Order_Type_Omitted = -11; //
    static public final int TTRC_ERR_Invalid_or_Malformed_Order_Type = -12; //
    static public final int TTRC_ERR_paging_unavailable = -13; //
    static public final int TTRC_ERR_paging_impossible = -14; //
    static public final int TTRC_ERR_Invalid_boolean_value = -15; //
    static public final int TTRC_ERR_Invalid_timeout_parameter = -16; //
    static public final int TTRC_ERR_Incorrectly_formated_device_code = -17; //
    static public final int TTRC_ERR_Device_Code_not_provided = -18; //
    static public final int TTRC_ERR_Invalid_device_code = -19; //
    static public final int TTRC_ERR_Invalid_or_non_existent_setting = -20; //
    static public final int TTRC_ERR_Invalid_value_for_setting = -21; //
    static public final int TTRC_ERR_Invalid_target_time_parameter = -22; //
    static public final int TTRC_ERR_Invalid_limit_parameter = -23; //
    static public final int TTRC_ERR_Invalid_offset_parameter = -24; //
    static public final int TTRC_ERR_Invalid_date_parameter = -25; //
    //static public final int TTRC_ERR_Invalid_date_parameter = -26; //
    static public final int TTRC_ERR_Invalid_Order_UUID = -27; //
    static public final int TTRC_ERR_No_http_client_property = -28; //
    static public final int TTRC_ERR_Invalid_callback_URL = -29; //
    static public final int TTRC_ERR_Client_provided_does_not_exist = -30; //
    static public final int TTRC_ERR_Invalid_JSON_payload = -31; //
    static public final int TTRC_ERR_Timeout_while_waiting_for_token = -32; //
    static public final int TTRC_ERR_Generic_timeout_parameter_invalid = -33; //
    static public final int TTRC_ERR_Out_of_bound_timeout_parameter = -34; //


    static public boolean isSuccessHttpResultCode(int nCode)
    {

        return KDSHttp.isSuccessResponseCode(nCode);

    }
    /**
     *
     * {
     "status": 201,
     "token": {
     "name": "default",
     "token": "2f1f4339c7b77fa3474bf8ba8852349273e928bb8ad9d186",
     "creation_date": "2016-12-24T08:04:34",
     "last_used": null,
     "ip_address": null
     }
     }
     * @param strJSON
     * @return
     *  The authentication value
     */
    static public String parseAuthenticationJSON(String strJSON)
    {
        //strJSON = "{\"status\": 201, \"token\": {\"last_used\": null, \"token\": \"c1a418e76baeaa2d1ec27f9fd591bf498668cf6e75ad4f53\", \"ip_address\": null, \"name\": \"default\", \"creation_date\": \"2017-03-30T14:52:31\"}, \"returnCode\": 2}";

        if (strJSON.isEmpty()) return "";
        if (strJSON == null) return "";

        try {
            JSONObject jsonObject = new JSONObject(strJSON);
            int resultCode = getJsonInt( jsonObject, "status");
            if (isSuccessHttpResultCode(resultCode)) {
                //JSONArray resultJsonArray = jsonObject.getJSONArray("token");
                //JSONObject resultJsonObject = resultJsonArray.getJSONObject(0);
                JSONObject t =(JSONObject)  jsonObject.get("token");
                if (t == null) return "";
                String token =getJsonString( t, "token");

                return token;


            } else {
                String strErr = buildErrorMessage(jsonObject);
                Toast.makeText(KDSApplication.getContext(), "failed: "+strErr,
                        Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {

            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //TableTracker.log2File(TAG + KDSLog._FUNCLINE_()+"Error:"+e.toString());
            //e.printStackTrace();
        }

        return "";
    }

    static String getJsonString(JSONObject jsonObj, String name)
    {
        if (jsonObj.isNull( name))
        {
            return "";
        }
        else {
            try {
                return jsonObj.getString(name);
            }
            catch (Exception e)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
                //TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Error:"+e.toString());
                //e.printStackTrace();
                return "";
            }

        }
    }

    static int getJsonInt(JSONObject jsonObj, String name)
    {
        if (jsonObj.isNull( name))
        {
            return 0;
        }
        else {
            try {
                return jsonObj.getInt(name);
            }
            catch (Exception e)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
                //TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Error:"+e.toString());
                //e.printStackTrace();
                return 0;
            }

        }
    }

    static boolean getJsonBoolean(JSONObject jsonObj, String name)
    {
        if (jsonObj.isNull( name))
        {
            return false;
        }
        else {
            try {
                return jsonObj.getBoolean(name);
            }
            catch (Exception e)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
                //TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Error:"+e.toString());
                //e.printStackTrace();
                return false;
            }

        }
    }

    static public String buildErrorMessage(JSONObject jsonErr)
    {
        try {
            int resultcode = getJsonInt( jsonErr, "status");
            int returncode =getJsonInt( jsonErr, "returnCode");
            String msg = getJsonString(jsonErr,"message");
            String s = String.format("Status=%d, ReturnCode=%d, Message=%s", resultcode, resultcode, msg);
            return s;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Error:"+e.toString());
            //e.printStackTrace();
        }
        return "";
    }

    /**
     * {
     "status": 200,
     "returnCode": 5,
     "count": 2,
     "items": [
     {
     "name": "102",
     "uuid": "f8853c42-3c10-4636-81e3-a3aeb65d9219",
     "orderType": "ON_PREMISE",
     "locationName": "",
     "state": "started",
     "created": "2016-09-22T19:08:52",
     "stateChanged": "2016-09-22T19:08:52",
     "paged": false,
     "elapsedTime": 0
     },
     {
     "name": "6",
     "uuid": "98a3c77c-9aa0-4eea-a17a-93b2f905a6f1",
     "orderType": "ON_PREMISE",
     "locationName": "",
     "state": "started",
     "created": "2016-09-22T19:08:56",
     "stateChanged": "2016-09-22T19:08:56",
     "paged": false,
     "elapsedTime": 0
     }
     ]
     }
     * @param strJSON
     * @return
     */
    static public ArrayList<TTOrder> parseOrdersJSON(String strJSON)
    {


        ArrayList<TTOrder> ar = new ArrayList<>();

        if (strJSON.isEmpty()) return ar;
        if (strJSON == null) return ar;

        try {
            JSONObject jsonObject = new JSONObject(strJSON);
            int resultCode = getJsonInt(jsonObject, "status");
            if (isSuccessHttpResultCode(resultCode)) {

                int ncount =getJsonInt(jsonObject, "count");
                JSONArray resultJsonArray = jsonObject.getJSONArray("items");

                for (int i=0; i< ncount; i++)
                {
                    JSONObject t =resultJsonArray.getJSONObject(i);
                    TTOrder order = convertJSON2Order(t);
                    ar.add(order);
                }
               return ar;


            } else {
                String strErr = buildErrorMessage(jsonObject);
                Toast.makeText(KDSApplication.getContext(), "failed: "+strErr,
                        Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Error:"+e.toString());
            //e.printStackTrace();
        }

        return ar;
    }


    /**
     * {
     "status": 200,
     "returnCode": 6,
     "activeorder": {
     "name": "27",
     "uuid": "66565bd2-5c3e-4b2d-80ff-82cfc1dd2f76",
     "orderType": "ON_PREMISE",
     "locationName": "207",
     "state": "located",
     "stateChanged": "2016-06-10T15:10:08",
     "created": "2016-06-10T15:08:43",
     "paged": false,
     "elapsedTime": 0
     }
     }
     * @param strJSON
     * @return
     */
    static public TTOrder parseOrderJSON(String strJSON)
    {

//        strJSON = "{\n" +
//                "     \"status\": 200,\n" +
//                "     \"returnCode\": 6,\n" +
//                "     \"activeorder\": {\n" +
//                "     \"name\": \"27\",\n" +
//                "     \"uuid\": \"66565bd2-5c3e-4b2d-80ff-82cfc1dd2f76\",\n" +
//                "     \"orderType\": \"ON_PREMISE\",\n" +
//                "     \"locationName\": \"207\",\n" +
//                "     \"state\": \"located\",\n" +
//                "     \"stateChanged\": \"2016-06-10T15:10:08\",\n" +
//                "     \"created\": \"2016-06-10T15:08:43\",\n" +
//                "     \"paged\": false,\n" +
//                "     \"elapsedTime\": 0\n" +
//                "     }\n" +
//                "     }";
        TTOrder order  = new TTOrder();

        if (strJSON.isEmpty()) return order;
        if (strJSON == null) return order;

        try {
            JSONObject jsonObject = new JSONObject(strJSON);
            int resultCode =getJsonInt( jsonObject, "status");
            if (isSuccessHttpResultCode(resultCode)) {

                JSONObject t =(JSONObject)  jsonObject.get("activeorder");
                if (t != null)
                    order = convertJSON2Order(t);

            } else {
                String strErr = buildErrorMessage(jsonObject);
                Toast.makeText(KDSApplication.getContext(), "failed: "+strErr,
                        Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Error:"+e.toString());
            //e.printStackTrace();
        }

        return order;
    }

    /**
     *  {
     "name": "6",
     "uuid": "98a3c77c-9aa0-4eea-a17a-93b2f905a6f1",
     "orderType": "ON_PREMISE",
     "locationName": "",
     "state": "started",
     "created": "2016-09-22T19:08:56",
     "stateChanged": "2016-09-22T19:08:56",
     "paged": false,
     "elapsedTime": 0
     }
     * @param jsonOrder
     * @return
     */
    static private TTOrder convertJSON2Order(JSONObject jsonOrder)
    {
        TTOrder ttOrder = new TTOrder();
        try {
            ttOrder.m_name = getJsonString( jsonOrder, "name");
            ttOrder.m_uuid = getJsonString( jsonOrder,"uuid");
            ttOrder.m_orderType = getJsonString( jsonOrder,"orderType");
            ttOrder.m_locationName = getJsonString( jsonOrder,"locationName");
            ttOrder.m_state = getJsonString( jsonOrder,"state");
            ttOrder.m_created = getJsonString( jsonOrder,"created");
            ttOrder.m_statueChanged = getJsonString( jsonOrder,"stateChanged");
            ttOrder.m_paged = getJsonBoolean(jsonOrder, "paged");
            ttOrder.m_elapseTime =getJsonInt( jsonOrder,"elapsedTime");
            return ttOrder;

        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);
            //TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Error:"+e.toString());
            //e.printStackTrace();
        }

        return ttOrder;
    }

    public enum NotificationType
    {
        Unknown,
        Orders,
        Created,
        Modified,
        Paged,
        Error,

    }
    static public NotificationType parseNotificationType(String strNotification)
    {
        if (strNotification.indexOf("\"message\":")>=0)
            return NotificationType.Error;
        try {
            JSONObject jsonObject = new JSONObject(strNotification);
            String notificationType =getJsonString( jsonObject, "type");
            if (notificationType.equals("ACTIVE_ORDERS"))
            {
                return NotificationType.Orders;
            }
            else if (notificationType.equals("ORDER_CREATED"))
            {
                return NotificationType.Created;
            }
            else if (notificationType.equals("ORDER_MODIFIED"))
            {
                return NotificationType.Modified;
            }
            else if (notificationType.equals("ORDER_PAGED"))
            {
                return NotificationType.Paged;
            }
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Error:"+e.toString());
            //e.printStackTrace();
        }
        return NotificationType.Unknown;
    }

    /**
     * {
     "count": 2,
     "type": "ACTIVE_ORDERS",
     "items": [
     {
     "name": "102",
     "uuid": "f8853c42-3c10-4636-81e3-a3aeb65d9219",
     "orderType": "ON_PREMISE",
     "locationName": "",
     "state": "started",
     "created": "2016-09-22T19:08:52",
     "stateChanged": "2016-09-22T19:08:52",
     "paged": false,
     "elapsedTime": 0
     },
     {
     "name": "6",
     "uuid": "98a3c77c-9aa0-4eea-a17a-93b2f905a6f1",
     "orderType": "ON_PREMISE",
     "locationName": "",
     "state": "started",
     "created": "2016-09-22T19:08:56",
     "stateChanged": "2016-09-22T19:08:56",
     "paged": false,
     "elapsedTime": 0
     }
     ]
     }
     * @param strJSON
     * @return
     */
    static public ArrayList<TTOrder> parseNotificationOrdersJSON(String strJSON)
    {


        ArrayList<TTOrder> ar = new ArrayList<>();

        if (strJSON.isEmpty()) return ar;
        if (strJSON == null) return ar;

        try {
            JSONObject jsonObject = new JSONObject(strJSON);

            int ncount =getJsonInt( jsonObject, "count");
            JSONArray resultJsonArray = jsonObject.getJSONArray("items");
            if (resultJsonArray == null) return ar;
            for (int i=0; i< ncount; i++)
            {
                JSONObject t =resultJsonArray.getJSONObject(i);
                if (t == null) continue;
                TTOrder order = convertJSON2Order(t);
                ar.add(order);
            }
            return ar;



        } catch (Exception e) {
            //TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Error:"+e.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //e.printStackTrace();
        }

        return ar;
    }

    /**
     * {
     "stateChanged": "2016-12-09T09:00:23",
     "seq": 84,
     "paged": false,
     "name": "23",
     "elapsedTime": 0,
     "uuid": "ac3efe43-c9ac-4a4d-8ea5-38bd816ec52f",
     "created": "2016-12-09T09:00:23",
     "orderType": "ON_PREMISE",
     "locationName": "8",
     "state": "located",
     "type": "ORDER_CREATED"
     }
     * @param strJSON
     * @return
     */
    static public TTOrder parseNotificationOrderJSON(String strJSON)
    {

        TTOrder order  = new TTOrder();

        if (strJSON.isEmpty()) return order;
        if (strJSON == null) return order;

        try {
            JSONObject jsonObject = new JSONObject(strJSON);

            order = convertJSON2Order(jsonObject);
            order.m_notificationType = getJsonString( jsonObject, "type");

            return order;

        } catch (Exception e) {

            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);
            //TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Error:"+e.toString());
            //e.printStackTrace();
        }

        return order;
    }

    /**
     * see https://table-tracker.readme.io/docs/api-code-glossary
     * @param strJSON
     * @return
     */
    static public TTReturnCode parseTTReturnCode(String strJSON)
    {
        //strJSON = "{\"status\": 201, \"token\": {\"last_used\": null, \"token\": \"c1a418e76baeaa2d1ec27f9fd591bf498668cf6e75ad4f53\", \"ip_address\": null, \"name\": \"default\", \"creation_date\": \"2017-03-30T14:52:31\"}, \"returnCode\": 2}";


        if (strJSON == null) return null;
        if (strJSON.isEmpty()) return null;


        try {
            JSONObject jsonObject = new JSONObject(strJSON);
            int status = getJsonInt( jsonObject, "status");
            int returnCode = getJsonInt( jsonObject, "returnCode");
            String msg = getJsonString(jsonObject, "message");
            if (msg.isEmpty()) return null; //it is not message json.
            if (returnCode == 0) return null;
            if (status == 0) return null;
            TTReturnCode r = new TTReturnCode();
            r.m_status = status;
            r.m_returnCode = returnCode;
            r.m_message = msg;

            return r;

        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);
            //TableTracker.log2File(TAG +KDSLog._FUNCLINE_()+"Error:"+e.toString());
            //e.printStackTrace();
        }

        return null;
    }

}
