package com.bematechus.kds;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by David.Wong on 2018/7/2.
 * Rev:
 */
public class ActivationHttp  extends Handler implements Runnable {

    public static final String TAG = "KDSHttp";

    public interface ActivationHttpEvent
    {
        public void onHttpResponse(ActivationHttp http, ActivationRequest request);
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
    ActivationHttpEvent m_receiver = null;


    Object m_locker = new Object();
    ArrayList<ActivationRequest> m_arRequests = new ArrayList<>();

    private void addRequest(ActivationRequest r)
    {
        synchronized (m_locker)
        {
            m_arRequests.add(r);
        }
    }
    private ActivationRequest popRequest()
    {
        synchronized (m_locker)
        {
            if (m_arRequests.size() >0)
            {
                ActivationRequest r = m_arRequests.get(0);
                m_arRequests.remove(0);
                return r;
            }
            else
                return null;
        }
    }


    public void setReceiver(ActivationHttpEvent receiver)
    {
        m_receiver = receiver;
    }



    public void request(ActivationRequest request)
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
        (new Thread(this)).start();
    }

    @Override
    public void handleMessage(Message msg) {

        switch (msg.what)
        {
            case MSG_HTTP_GET_RESPONSE:
            {

                ActivationRequest r = (ActivationRequest) msg.obj;
                //TableTracker.log2File(KDSLog._FUNCLINE_()+ "HTTP Response=" + r.toString());
                int nHttpResponseCode = msg.arg1;


                if (m_receiver != null)
                    m_receiver.onHttpResponse(this,r);

            }
            break;
            case MSG_HTTP_GET_ERROR:
            {
//                int errcode = (int)msg.obj;
//                String s = String.format("error=%d", errcode);
//
//                //Toast.makeText(KDSApplication.getContext(),s, Toast.LENGTH_SHORT).show();
                ActivationRequest r = (ActivationRequest) msg.obj;
                if (m_receiver != null)
                    m_receiver.onHttpResponse(this, r);
            }
            break;
            case MSG_HTTP_EXCEPTION:
            {
                int errocode = msg.arg1;

                ActivationRequest r = (ActivationRequest) msg.obj;
                //TableTracker.log2File(KDSLog._FUNCLINE_()+ "HTTP Exception=" + r.toString());
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


//    private void debug_process()
//    {
//        String result = getDebugResult(m_debugID);
//        if (m_debugID == Debug_Response.Authen) { //authen
//            Message msg = new Message();
//            msg.what = MSG_HTTP_GET_RESPONSE;
//
//            ActivationRequest r = new ActivationRequest();
//            r.m_result = result;
//            r.m_ttCommand = TableTracker.TT_Command.Authentication;
//            //r.m_command = TableTracker.TT_Authentication;
//            r.m_httpResponseCode = 200;
//            msg.obj = r;
//            msg.arg1 = 200;
//            KDSHttp.this.sendMessage(msg);
//        }
//        else if (m_debugID == Debug_Response.Orders) //orders
//        {
//            Message msg = new Message();
//            msg.what = MSG_HTTP_GET_RESPONSE;
//
//            ActivationRequest r = new ActivationRequest();
//            r.m_result = result;
//            //r.m_command = TableTracker.TT_Active_Orders;
//            r.m_ttCommand = TableTracker.TT_Command.Active_Orders;
//            r.m_httpResponseCode = 200;
//            msg.obj = r;
//            KDSHttp.this.sendMessage(msg);
//        }
//        else if (m_debugID == Debug_Response.Order) //orders
//        {
//            Message msg = new Message();
//            msg.what = MSG_HTTP_GET_RESPONSE;
//
//            ActivationRequest r = new ActivationRequest();
//            r.m_result = result;
//            r.m_httpResponseCode = 200;
//            //r.m_command = TableTracker.TT_Active_Order;
//            r.m_ttCommand = TableTracker.TT_Command.Active_Order;
//            msg.obj = r;
//            KDSHttp.this.sendMessage(msg);
//        }
//    }
    boolean m_bRunning = false;
    public void run()
    {
        //m_debugID = Debug_Response.Authen;
        //m_debugID = Debug_Response.Orders;
        //debug_process();

        m_bRunning = true;
        ActivationRequest r = popRequest();


        //if (m_debugID == Debug_Response.No_Debug)
        //    getResultFromHttpGet(m_uri);
        if (r != null) {
            //TableTracker.log2File(KDSLog._FUNCLINE_()+ "HTTP Request=" + r.toString());
           // if (r.isPOSTmethod())
            {
                getJsonResultFromHttpPost2(r);
            }
//            else {
//                getResultFromHttpGet2(r);
//            }
        }
        //else
        //    debug_process();


        m_bRunning = false;

    }
    public boolean isRunning()
    {
        return  m_bRunning;
    }



//    private HttpURLConnection createHttpConnectionObj2(ActivationRequest request)
//    {
//        URL url = null;
//        String result = "";
//        try {
//            url = new URL(request.m_url);
//            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
//            urlConn.setRequestMethod(request.m_method);
//            // 设置字符集
//            urlConn.setRequestProperty("Charset", "UTF-8");
//            if (!request.m_authen.isEmpty()) {
//                urlConn.setRequestProperty("Authorization", "Bearer " + request.m_authen);
//                urlConn.setRequestProperty("Content-Type", "application/json");
//            }
//
//            return urlConn;
//        }
//        catch (Exception e)
//        {
//            //KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
//        }
//        return null;
//    }


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
//    private String getResultFromHttpGet(HttpURLConnection urlConn, ActivationRequest request)
//    {
//
//        URL url = null;
//        String result = "";
//        try{
//
//            int responseCode = urlConn.getResponseCode();
//
//            InputStreamReader in = new InputStreamReader(urlConn.getInputStream());
//
//            BufferedReader bufferedReader = new BufferedReader(in);
//
//            String readlLine = null;
//            while ((readlLine = bufferedReader.readLine()) != null) {
//                result += readlLine;
//            }
//            in.close();
//            urlConn.disconnect();
//
//            request.m_result = result;
//            request.m_httpResponseCode = responseCode;
//            Message msg = new Message();
//            msg.what = MSG_HTTP_GET_RESPONSE;
//
//            msg.obj = request;
//            msg.arg1 = responseCode;
//            ActivationHttp.this.sendMessage(msg);
//
//        }
//        catch (Exception e)
//        {
//            request.m_result = e.toString();
//            request.m_httpResponseCode = HTTP_Exception;
//            Message msg = new Message();
//            msg.what = MSG_HTTP_EXCEPTION;
//
//            msg.obj = request;// e.toString();
//
//            msg.arg1 = HTTP_Exception;
//            ActivationHttp.this.sendMessage(msg);
//
//            //KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
//            e.printStackTrace();
//        }
//
//
//
//        return result;
//    }
//

//    private String getResultFromHttpGet2(ActivationRequest request)
//    {
//        String strUrl = request.m_url;// strUri;// String.format("http://%s:%d", ip, port,command );
//        URL url = null;
//        String result = "";
//        try {
//            HttpURLConnection urlConn = null;
//
//            urlConn = createHttpConnectionObj2(request);
//
//            if (urlConn == null) return "";
//
//            return getResultFromHttpGet(urlConn, request);
//        }
//        catch (Exception e)
//        {
//           // KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
//        }
//        return "";
//
//    }
//

//    private String getResultFromHttpPost(ActivationRequest request)
//    {
//
//        URL url = null;
//        String strUrl = request.m_url;
//        String result = "";
//        try{
//            url = new URL(strUrl);
//            HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
//            urlConn.setRequestMethod("POST");
//            urlConn.setDoInput(true);
//            urlConn.setDoOutput(true);
//            urlConn.setUseCaches(false);
//            urlConn.setRequestProperty("Content-Type", "application/json");
//            urlConn.setRequestProperty("Authorization", "Bearer " + request.m_authen);
//            // 设置字符集
//            urlConn.setRequestProperty("Charset", "UTF-8");
//            urlConn.connect();
//            //send
//            DataOutputStream dop = new DataOutputStream(urlConn.getOutputStream());
//
//            JSONObject obj = new JSONObject();
//            obj.put("tracking", true);
//            dop.writeBytes(obj.toString());
//
//            dop.flush();
//            dop.close();
//            //read
//            int responseCode = urlConn.getResponseCode();
//
//            InputStreamReader in = new InputStreamReader(urlConn.getInputStream());
//
//            BufferedReader bufferedReader = new BufferedReader(in);
//
//            String readlLine = null;
//            while ((readlLine = bufferedReader.readLine()) != null) {
//                result += readlLine;
//            }
//            in.close();
//            urlConn.disconnect();
//            Message msg = new Message();
//            msg.what = MSG_HTTP_GET_RESPONSE;
//            request.m_result = result;
//            request.m_httpResponseCode = responseCode;
//            msg.obj = request;
//            ActivationHttp.this.sendMessage(msg);
//
//        }
//        catch (Exception e)
//        {
//            request.m_result = e.toString();
//            request.m_httpResponseCode = HTTP_Exception;
//            Message msg = new Message();
//            msg.what = MSG_HTTP_EXCEPTION;
//
//            msg.obj = request;// e.toString();
//
//            msg.arg1 = HTTP_Exception;
//            ActivationHttp.this.sendMessage(msg);
//
//           // KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
//            e.printStackTrace();
//        }
//
//
//
//        return result;
//    }

    static private String readUrlConnToString(HttpURLConnection urlConn)
    {
        String result = "";
        try {
            InputStreamReader in = new InputStreamReader(urlConn.getInputStream());

            BufferedReader bufferedReader = new BufferedReader(in);

            String readlLine = null;
            while ((readlLine = bufferedReader.readLine()) != null) {
                result += readlLine;
            }
            in.close();

            return result;
        }
        catch (Exception e)
        {
            return "";
        }
    }
    private String getJsonResultFromHttpPost2(ActivationRequest request) {
        InputStream inputStream = null;
        HttpURLConnection urlConn = null;

        try {


            URL url = new URL(request.URL);

            urlConn = (HttpURLConnection) url.openConnection();


           urlConn.setRequestProperty("Accept", "application/json");
           urlConn.setRequestProperty("Charset", "UTF-8");
           //urlConn.setRequestProperty("Authorization", request.TOKEN);
            urlConn.setDoOutput( true );
            urlConn.setInstanceFollowRedirects( false );
            urlConn.setRequestMethod( "POST" );
            urlConn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConn.setRequestProperty( "Content-Length", Integer.toString( request.m_params.length() ));
            urlConn.setConnectTimeout(50000);
            urlConn.setReadTimeout(50000);
            urlConn.setUseCaches( false );
           // urlConn.setUseCaches(false);
            //urlConn.setRequestProperty("X-Auth-Token", request.TOKEN);
            //urlConn.setRequestProperty("Authorization", "Bearer " + request.TOKEN);
            //urlConn.setDoOutput(true);
            //urlConn.setDoInput(true);

            urlConn.connect();
            DataOutputStream wr = new DataOutputStream(urlConn.getOutputStream());
//            Gson gson = new Gson();

            //String jsonString =toUtf8( request.m_params);// gson.toJson(dto);
            String jsonString =  request.m_params;// gson.toJson(dto);
            wr.writeBytes(jsonString);
            wr.flush();
            wr.close();
            // try to get response
            int statusCode = urlConn.getResponseCode();
            if (statusCode == HTTP_OK) {

                String response = readUrlConnToString(urlConn);
                Message msg = new Message();
                msg.what = MSG_HTTP_GET_RESPONSE;
                request.m_result = response;
                request.m_httpResponseCode = statusCode;
                msg.obj = request;
                ActivationHttp.this.sendMessage(msg);
                return response;
            }
            else
            {
                String response = readUrlConnToString(urlConn);
                Message msg = new Message();
                msg.what = MSG_HTTP_GET_ERROR;
                request.m_result = response;
                request.m_httpResponseCode = statusCode;
                msg.obj = request;
                ActivationHttp.this.sendMessage(msg);
                return response;
            }
        }
        catch (UnknownHostException err)
        {//2.1.5

            request.m_result = "No internet connection is detected.";
            request.m_httpResponseCode = HTTP_Exception;
            Message msg = new Message();
            msg.what = MSG_HTTP_EXCEPTION;

            msg.obj = request;// e.toString();

            msg.arg1 = HTTP_Exception;
            ActivationHttp.this.sendMessage(msg);

//            Message msg = new Message();
//            msg.what = MSG_HTTP_GET_ERROR;
//            request.m_result = "Internet error";
//            request.m_httpResponseCode = 404;
//            msg.obj = request;
//            ActivationHttp.this.sendMessage(msg);
//            return "";
        }
        catch(Exception e)
        {
            request.m_result = e.toString();
            request.m_httpResponseCode = HTTP_Exception;
            Message msg = new Message();
            msg.what = MSG_HTTP_EXCEPTION;

            msg.obj = request;// e.toString();

            msg.arg1 = HTTP_Exception;
            ActivationHttp.this.sendMessage(msg);

            // KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            e.printStackTrace();

        }
        finally
        {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }
        return "";
    }

    public static String toUtf8(String str) {
         String result = null;
         try {
                result = new String(str.getBytes("UTF-8"), "UTF-8");
            } catch (UnsupportedEncodingException e) {

                e.printStackTrace();
            }
        return result;
    }

}
