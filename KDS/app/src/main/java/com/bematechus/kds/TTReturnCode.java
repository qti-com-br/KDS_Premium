package com.bematechus.kds;

/**
 * Created by Administrator on 2017/4/7.
 */
public class TTReturnCode {
    int m_status = 0;
    int m_returnCode = 0;
    String m_message = "";

    public String toString()
    {
        return String.format("%d, %d, %s", m_status, m_returnCode, m_message);
    }
    public String toMessage()
    {
        return String.format("Status: %d\nReturnCode: %d\nMessage: %s",m_status, m_returnCode, m_message );
    }
}
