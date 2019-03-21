package com.bematechus.kds;

import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.widget.Toast;

import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

/**
 * For Table Tracker
 */
public class KDSHttp extends Handler implements Runnable {

    public static final String TAG = "KDSHttp";
    public enum TT_Command
    {
        TT_Authentication,
        TT_Online,
        TT_Active_Orders,
        TT_Active_Order,
        TT_Clear_Orders,
        TT_Page_Order,
    }
    public interface KDSHttpEvent
    {
        public void onHttpResponse(KDSHttp http, HttpRequest request);
    }

    //see http response code: https://httpstatuses.com/
    public static final int HTTP_Exception = -6000;

    public static final int HTTP_OK = 200;
    public static final int HTTP_Created = 201;
    public static final int HTTP_Accepted = 202;

    public static final int HTTP_Non_authoritative_Information = 203;
    public static final int HTTP_No_Content= 204;
    public static final int HTTP_Reset_Content= 205;
    public static final int HTTP_Partial_Content= 206;
    public static final int HTTP_Multi_Status= 207;
    public static final int HTTP_Already_Reported= 208;
    public static final int HTTP_IM_Used= 226;


    final int MSG_HTTP_GET_RESPONSE  =1;
    final int MSG_HTTP_GET_ERROR = 2;
    final int MSG_HTTP_EXCEPTION = 3;
    KDSHttpEvent m_receiver = null;


    Object m_locker = new Object();
    ArrayList<HttpRequest> m_arRequests = new ArrayList<>();

    private void addRequest(HttpRequest r)
    {
        synchronized (m_locker)
        {
            m_arRequests.add(r);
        }
    }
    private HttpRequest popRequest()
    {
        synchronized (m_locker)
        {
            if (m_arRequests.size() >0)
            {
                HttpRequest r = m_arRequests.get(0);
                m_arRequests.remove(0);
                return r;
            }
            else
                return null;
        }
    }


    public void setReceiver(KDSHttpEvent receiver)
    {
        m_receiver = receiver;
    }



    public void request(HttpRequest request)
    {
        addRequest(request);

        doRequest();
    }

    private void doRequest()
    {
        this.start();
    }
    private void start()
    {
        (new Thread(this, "KDSHttp")).start();
    }

    @Override
    public void handleMessage(Message msg) {

        switch (msg.what)
        {
            case MSG_HTTP_GET_RESPONSE:
            {

                HttpRequest r = (HttpRequest) msg.obj;
                TableTracker.log2File(KDSLog._FUNCLINE_()+ "HTTP Response=" + r.toString());
                int nHttpResponseCode = msg.arg1;


                if (m_receiver != null)
                    m_receiver.onHttpResponse(this,r);

            }
            break;
//            case MSG_HTTP_GET_ERROR:
//            {
//                int errcode = (int)msg.obj;
//                String s = String.format("error=%d", errcode);
//                TableTracker.log2File(KDSLog._FUNCLINE_()+ "HTTP code=" + KDSUtil.convertIntToString(nHttpResponseCode)+ ", Get =" + r.m_result);
//                //Toast.makeText(KDSApplication.getContext(),s, Toast.LENGTH_SHORT).show();
//                if (m_receiver != null)
//                    m_receiver.onHttpResponse(this, errcode, "");
//            }
//            break;
            case MSG_HTTP_EXCEPTION:
            {
                int errocode = msg.arg1;

                HttpRequest r = (HttpRequest) msg.obj;
                TableTracker.log2File(KDSLog._FUNCLINE_()+ "HTTP Exception=" + r.toString());
                //TableTracker.log2File(KDSLog._FUNCLINE_()+ "Exception HTTP code=" + KDSUtil.convertIntToString(r.m_httpResponseCode)+ ", Get =" + r.m_result);
                if (m_receiver != null)
                    m_receiver.onHttpResponse(this,r);
            }
            break;
            default:
                break;
        }
    }
    public Debug_Response m_debugID = Debug_Response.No_Debug;
    public enum Debug_Response
    {
        No_Debug,
        Authen,
        Orders,
        Order,

    }
    public void setDebug(Debug_Response debug)
    {
        m_debugID = debug;
    }

    /**
     * For test !!!
     * @param d
     * @return
     */
    private String getDebugResult(Debug_Response d)
    {
        if (m_debugID == Debug_Response.Authen) { //authen

            String result = "{\"status\": 201, \"token\": {\"last_used\": null, \"token\": \"c1a418e76baeaa2d1ec27f9fd591bf498668cf6e75ad4f53\", \"ip_address\": null, \"name\": \"default\", \"creation_date\": \"2017-03-30T14:52:31\"}, \"returnCode\": 2}";
            return result;
        }
        else if (m_debugID == Debug_Response.Orders) //orders
        {
            String result = "";
           // Date dt = new Date();

//            if (dt.getTime() %2 ==0)
//                result = "{\"status\": 200, \"count\": 1, \"returnCode\": 5, \"items\": [{\"locationName\": \"110\", \"elapsedTime\": 9, \"state\": \"started\", \"stateChanged\": \"2017-04-26T14:37:57\", \"uuid\": \"606771b8-a7af-437d-a01f-ad5f1250491f\", \"created\": \"2017-04-26T14:37:57\", \"orderType\": \"ON_PREMISE\", \"paged\": false, \"name\": \"1\"}]}";
//            else
                result = "{\n" +
                    "     \"status\": 200,\n" +
                    "     \"returnCode\": 5,\n" +
                    "     \"count\": 4,\n" +
                    "     \"items\": [\n" +
                    "     {\n" +
                    "     \"name\": \"102\",\n" +
                    "     \"uuid\": \"f8853c42-3c10-4636-81e3-a3aeb65d9219\",\n" +
                    "     \"orderType\": \"ON_PREMISE\",\n" +
                    "     \"locationName\": \"\",\n" +
                    "     \"state\": \"started\",\n" +
                    "     \"created\": \"2016-09-22T19:08:52\",\n" +
                    "     \"stateChanged\": \"2016-09-22T19:08:52\",\n" +
                    "     \"paged\": false,\n" +
                    "     \"elapsedTime\": 0\n" +
                    "     },\n" +
                    "     {\n" +
                    "     \"name\": \"103\",\n" +
                    "     \"uuid\": \"f8853c42-3c10-4636-81e3-a3aeb65d9219\",\n" +
                    "     \"orderType\": \"ON_PREMISE\",\n" +
                    "     \"locationName\": \"\",\n" +
                    "     \"state\": \"started\",\n" +
                    "     \"created\": \"2016-09-22T19:08:52\",\n" +
                    "     \"stateChanged\": \"2016-09-22T19:08:52\",\n" +
                    "     \"paged\": false,\n" +
                    "     \"elapsedTime\": 0\n" +
                    "     },\n" +
                    "     {\n" +
                    "     \"name\": \"12\",\n" +
                    "     \"uuid\": \"f8853c42-3c10-4636-81e3-a3aeb65d9219\",\n" +
                    "     \"orderType\": \"ON_PREMISE\",\n" +
                    "     \"locationName\": \"\",\n" +
                    "     \"state\": \"started\",\n" +
                    "     \"created\": \"2016-09-22T19:08:52\",\n" +
                    "     \"stateChanged\": \"2016-09-22T19:08:52\",\n" +
                    "     \"paged\": false,\n" +
                    "     \"elapsedTime\": 20\n" +
                    "     },\n" +
                    "     {\n" +
                    "     \"name\": \"6\",\n" +
                    "     \"uuid\": \"98a3c77c-9aa0-4eea-a17a-93b2f905a6f1\",\n" +
                    "     \"orderType\": \"ON_PREMISE\",\n" +
                    "     \"locationName\": \"\",\n" +
                    "     \"state\": \"started\",\n" +
                    "     \"created\": \"2016-09-22T19:08:56\",\n" +
                    "     \"stateChanged\": \"2016-09-22T19:08:56\",\n" +
                    "     \"paged\": false,\n" +
                    "     \"elapsedTime\": 0\n" +
                    "     }\n" +
                    "     ]\n" +
                    "     }";
            return result;
        }
        else if (m_debugID == Debug_Response.Order) //orders
        {

            String result = "{\n" +
                    "     \"status\": 200,\n" +
                    "     \"returnCode\": 6,\n" +
                    "     \"activeorder\": {\n" +
                    "     \"name\": \"27\",\n" +
                    "     \"uuid\": \"66565bd2-5c3e-4b2d-80ff-82cfc1dd2f76\",\n" +
                    "     \"orderType\": \"ON_PREMISE\",\n" +
                    "     \"locationName\": \"207\",\n" +
                    "     \"state\": \"located\",\n" +
                    "     \"stateChanged\": \"2016-06-10T15:10:08\",\n" +
                    "     \"created\": \"2016-06-10T15:08:43\",\n" +
                    "     \"paged\": false,\n" +
                    "     \"elapsedTime\": 0\n" +
                    "     }\n" +
                    "     }";
            return result;
        }

        return "";
    }

    private void debug_process()
    {
        String result = getDebugResult(m_debugID);
        if (m_debugID == Debug_Response.Authen) { //authen
            Message msg = new Message();
            msg.what = MSG_HTTP_GET_RESPONSE;

            HttpRequest r = new HttpRequest();
            r.m_result = result;
            r.m_ttCommand = TableTracker.TT_Command.Authentication;
            //r.m_command = TableTracker.TT_Authentication;
            r.m_httpResponseCode = 200;
            msg.obj = r;
            msg.arg1 = 200;
            KDSHttp.this.sendMessage(msg);
        }
        else if (m_debugID == Debug_Response.Orders) //orders
        {
            Message msg = new Message();
            msg.what = MSG_HTTP_GET_RESPONSE;

            HttpRequest r = new HttpRequest();
            r.m_result = result;
            //r.m_command = TableTracker.TT_Active_Orders;
            r.m_ttCommand = TableTracker.TT_Command.Active_Orders;
            r.m_httpResponseCode = 200;
            msg.obj = r;
            KDSHttp.this.sendMessage(msg);
        }
        else if (m_debugID == Debug_Response.Order) //orders
        {
            Message msg = new Message();
            msg.what = MSG_HTTP_GET_RESPONSE;

            HttpRequest r = new HttpRequest();
            r.m_result = result;
            r.m_httpResponseCode = 200;
            //r.m_command = TableTracker.TT_Active_Order;
            r.m_ttCommand = TableTracker.TT_Command.Active_Order;
            msg.obj = r;
            KDSHttp.this.sendMessage(msg);
        }
    }
    boolean m_bRunning = false;
    public void run()
    {
        //m_debugID = Debug_Response.Authen;
        //m_debugID = Debug_Response.Orders;
        //debug_process();

        m_bRunning = true;
        HttpRequest r = popRequest();


        //if (m_debugID == Debug_Response.No_Debug)
        //    getResultFromHttpGet(m_uri);
        if (r != null) {
            TableTracker.log2File(KDSLog._FUNCLINE_()+ "HTTP Request=" + r.toString());
            if (r.isPOSTmethod())
            {
                getResultFromHttpPost(r);
            }
            else {
                getResultFromHttpGet2(r);
            }
        }
        //else
        //    debug_process();


        m_bRunning = false;

    }
    public boolean isRunning()
    {
        return  m_bRunning;
    }



    private HttpURLConnection createHttpConnectionObj2(HttpRequest request)
    {
        URL url = null;
        String result = "";
        try {
            url = new URL(request.m_url);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod(request.m_method);
            // 设置字符集
            urlConn.setRequestProperty("Charset", "UTF-8");
            if (!request.m_authen.isEmpty()) {
                urlConn.setRequestProperty("Authorization", "Bearer " + request.m_authen);
                urlConn.setRequestProperty("Content-Type", "application/json");
            }

            return urlConn;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
        }
        return null;
    }


    static public boolean isSuccessResponseCode(int nHttpResponseCode)
    {
        switch (nHttpResponseCode)
        {
            case HTTP_OK:
            case HTTP_Created:
            case HTTP_Accepted:
                return true;
            case HTTP_Non_authoritative_Information:
            case HTTP_No_Content:
            case HTTP_Reset_Content:
            case HTTP_Partial_Content:
            case HTTP_Multi_Status:
            case HTTP_Already_Reported:
            case HTTP_IM_Used:
                return false;
            default:
                return false;
        }
    }
    private String getResultFromHttpGet(HttpURLConnection urlConn, HttpRequest request)
    {

        URL url = null;
        String result = "";
        try{

            int responseCode = urlConn.getResponseCode();

            InputStreamReader in = new InputStreamReader(urlConn.getInputStream());

            BufferedReader bufferedReader = new BufferedReader(in);

            String readlLine = null;
            while ((readlLine = bufferedReader.readLine()) != null) {
                result += readlLine;
            }
            in.close();
            urlConn.disconnect();

            request.m_result = result;
            request.m_httpResponseCode = responseCode;
            Message msg = new Message();
            msg.what = MSG_HTTP_GET_RESPONSE;

            msg.obj = request;
            msg.arg1 = responseCode;
            KDSHttp.this.sendMessage(msg);

        }
        catch (Exception e)
        {
            request.m_result = e.toString();
            request.m_httpResponseCode = HTTP_Exception;
            Message msg = new Message();
            msg.what = MSG_HTTP_EXCEPTION;

            msg.obj = request;// e.toString();

            msg.arg1 = HTTP_Exception;
            KDSHttp.this.sendMessage(msg);

            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            e.printStackTrace();
        }



        return result;
    }


    private String getResultFromHttpGet2(HttpRequest request)
    {
        String strUrl = request.m_url;// strUri;// String.format("http://%s:%d", ip, port,command );
        URL url = null;
        String result = "";
        try {
            HttpURLConnection urlConn = null;

            urlConn = createHttpConnectionObj2(request);

            if (urlConn == null) return "";

            return getResultFromHttpGet(urlConn, request);
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
        }
        return "";

    }


    private String getResultFromHttpPost(HttpRequest request)
    {

        URL url = null;
        String strUrl = request.m_url;
        String result = "";
        try{
            url = new URL(strUrl);
            HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setRequestProperty("Content-Type", "application/json");
            urlConn.setRequestProperty("Authorization", "Bearer " + request.m_authen);
            // 设置字符集
            urlConn.setRequestProperty("Charset", "UTF-8");
            urlConn.connect();
            //send
            DataOutputStream dop = new DataOutputStream(urlConn.getOutputStream());

            JSONObject obj = new JSONObject();
            obj.put("tracking", true);
            dop.writeBytes(obj.toString());

            dop.flush();
            dop.close();
            //read
            int responseCode = urlConn.getResponseCode();

            InputStreamReader in = new InputStreamReader(urlConn.getInputStream());

            BufferedReader bufferedReader = new BufferedReader(in);

            String readlLine = null;
            while ((readlLine = bufferedReader.readLine()) != null) {
                result += readlLine;
            }
            in.close();
            urlConn.disconnect();
            Message msg = new Message();
            msg.what = MSG_HTTP_GET_RESPONSE;
            request.m_result = result;
            request.m_httpResponseCode = responseCode;
            msg.obj = request;
            KDSHttp.this.sendMessage(msg);

        }
        catch (Exception e)
        {
            request.m_result = e.toString();
            request.m_httpResponseCode = HTTP_Exception;
            Message msg = new Message();
            msg.what = MSG_HTTP_EXCEPTION;

            msg.obj = request;// e.toString();

            msg.arg1 = HTTP_Exception;
            KDSHttp.this.sendMessage(msg);

            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            e.printStackTrace();
        }



        return result;
    }


}
