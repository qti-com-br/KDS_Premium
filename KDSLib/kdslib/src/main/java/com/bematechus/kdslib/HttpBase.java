package com.bematechus.kdslib;

import android.os.Handler;
import android.os.Message;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by David.Wong on 2019/3/29.
 * Rev:
 */
public class HttpBase extends Handler implements Runnable {
    public static final String TAG = "KDSHttp";

    public interface HttpEvent
    {
        public void onHttpResponse(HttpBase http, HttpRequestBase request);
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


    public final int MSG_HTTP_GET_RESPONSE  =1;
    public final int MSG_HTTP_GET_ERROR = 2;
    public final int MSG_HTTP_EXCEPTION = 3;
    protected HttpEvent m_receiver = null;


    Object m_locker = new Object();
    Vector<HttpRequestBase> m_arRequests = new Vector<>();
    final int MAX_WAITING_COUNT = 100;
    private boolean addRequest(HttpRequestBase r)
    {
        synchronized (m_locker)
        {
            if (m_arRequests.size() > MAX_WAITING_COUNT)
                return false; //prevent exhaust all resources.

            m_arRequests.add(r);
            return true;
        }
    }
    protected HttpRequestBase popRequest()
    {
        synchronized (m_locker)
        {
            if (m_arRequests.size() >0)
            {
                HttpRequestBase r = m_arRequests.get(0);
                m_arRequests.remove(0);
                return r;
            }
            else
                return null;
        }
    }

    public void setReceiver(HttpEvent receiver)
    {
        m_receiver = receiver;
    }

    public void request(HttpRequestBase request)
    {
        addRequest(request);

        doRequest();
    }

    private void doRequest()
    {
        this.start();
    }
    protected Thread m_httpThread = null;
    private void start()
    {
        if (m_httpThread == null ||
                !m_httpThread.isAlive()) {
            m_httpThread = (new Thread(this));//.start();
            m_httpThread.setName("HttpThread");
            m_httpThread.start();
        }
    }

    @Override
    public void handleMessage(Message msg) {

        doHandleMessage(msg);
    }

    public void doHandleMessage(Message msg)
    {

    }

    public void run()
    {
        doRun();
    }
    public void doRun()
    {

    }

    protected String getJsonResultFromHttpPost2(HttpRequestBase request) {
        InputStream inputStream = null;
        HttpURLConnection urlConn = null;

        try {


            URL url = new URL(request.getUrl());

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
                HttpBase.this.sendMessage(msg);
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
                HttpBase.this.sendMessage(msg);
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
            HttpBase.this.sendMessage(msg);

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
            HttpBase.this.sendMessage(msg);

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

    protected String getResultFromHttpGet(HttpURLConnection urlConn, HttpRequestBase request)
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
            HttpBase.this.sendMessage(msg);

        }
        catch (Exception e)
        {
            request.m_result = e.toString();
            request.m_httpResponseCode = HTTP_Exception;
            Message msg = new Message();
            msg.what = MSG_HTTP_EXCEPTION;

            msg.obj = request;// e.toString();

            msg.arg1 = HTTP_Exception;
            HttpBase.this.sendMessage(msg);

            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            e.printStackTrace();
        }



        return result;
    }


    protected String getResultFromHttpGet2(HttpRequestBase request)
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


    protected String getResultFromHttpPost(HttpRequestBase request)
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
            HttpBase.this.sendMessage(msg);

        }
        catch (Exception e)
        {
            request.m_result = e.toString();
            request.m_httpResponseCode = HTTP_Exception;
            Message msg = new Message();
            msg.what = MSG_HTTP_EXCEPTION;

            msg.obj = request;// e.toString();

            msg.arg1 = HTTP_Exception;
            HttpBase.this.sendMessage(msg);

            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            e.printStackTrace();
        }



        return result;
    }
    private HttpURLConnection createHttpConnectionObj2(HttpRequestBase request)
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

    public static String toUtf8(String str) {
        String result = null;
        try {
            result = new String(str.getBytes("UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
        return result;
    }

    static public class HttpRequestBase
    {
        public String m_url = "";
        public String m_authen = "";
        public String m_params = "";
        public String m_result = "";
        public int m_httpResponseCode = 0;
        public static final String GET = "GET";
        public static final String DELETE = "DELETE";
        public static final String POST = "POST";
        public String m_method = "";
        public Object m_tag = null;

        public void setTag(Object obj)
        {
            m_tag = obj;
        }
        public Object getTag()
        {
            return m_tag;
        }
        public void reset()
        {

            m_method = POST;
            m_url = "";
            m_authen = "";
            m_params = "";
            m_result = "";
            m_httpResponseCode = 0;
            m_tag = null;
        }
        public void methodGET()
        {
            m_method = GET;
        }
        public void methodDELETE()
        {
            m_method = DELETE;
        }
        public void methodPOST()
        {
            m_method = POST;
        }
        public boolean isGETmethod()
        {
            return m_method.equals(GET);
        }

        public boolean isDELETEmethod()
        {
            return m_method.equals(DELETE);
        }
        public boolean isPOSTmethod()
        {
            return m_method.equals(POST);
        }
        public void setHttpCode(int nCode)
        {
            m_httpResponseCode = nCode;
        }
        public int getHttpCode()
        {
            return m_httpResponseCode;
        }

        public void setParams(String str)
        {
            m_params = str;
        }
        public String getParams()
        {
            return m_params;
        }

        public void setResult(String str)
        {
            m_result = str;
        }
        public String getResult()
        {
            return m_result;
        }

        public String toString()
        {
            String s = "";
            s += ",Method:" + m_method;
            s += ",Params:" + m_params;
            s +=",url:" + m_url ;
            s += ",Authen:" + m_authen ;
            s += ",httpresponsecode:"+ KDSUtil.convertIntToString(m_httpResponseCode );
            s += ",result:" +m_result ;
            return s;
        }
        public String getUrl()
        {
           return m_url;
        }

        static public String createNewGUID()
        {

            String s = UUID.randomUUID().toString();//create new GUID
            //s = s.replaceAll("-", "");
            return s;
        }

    }
}
