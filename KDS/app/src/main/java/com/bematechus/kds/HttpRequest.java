package com.bematechus.kds;

import com.bematechus.kdslib.KDSUtil;

/**
 * For Table tracker feature.
 * this create he http request string.
 *
 */
public class HttpRequest {
    static final String GET = "GET";
    static final String DELETE = "DELETE";
    static final String POST = "POST";
    String m_method = "";

    String m_params = "";
    String m_url = "";
    String m_authen = "";
    String m_result = "";
    int m_httpResponseCode = 0;
    TableTracker.TT_Command m_ttCommand = TableTracker.TT_Command.None;

    public HttpRequest()
    {
        reset();
    }

    public void reset()
    {
        m_ttCommand = TableTracker.TT_Command.None;
        m_method = GET;
        //m_command = "";
        m_params = "";
        m_url = "";
        m_authen = "";
        m_result = "";
        m_httpResponseCode = 0;
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
//    public String getMethod()
//    {
//        return m_command;
//    }
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
    public String toString()
    {
        String s = "TTCommand:"+m_ttCommand.toString();
        s += ",Method:" + m_method;
        s += ",Params:" + m_params;
        s +=",url:" + m_url ;
        s += ",Authen:" + m_authen ;
        s += ",httpresponsecode:"+ KDSUtil.convertIntToString(m_httpResponseCode );
        s += ",result:" +m_result ;
        return s;
    }
}
