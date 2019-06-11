package com.bematechus.kdslib;

import android.util.Log;

import java.util.Date;

/**
 * Created by David.Wong on 2019/6/5.
 * Rev:
 */
public class AckData {
    private final String TAG = "AckData";

    static public final String ACKGUID = "<ACKGUID>";
    static public final String ACKGUID_END = "</ACKGUID>";


    //String m_strStationID = "";
    String m_strAckGUID = "";
    String m_strXML = "";
    Date m_dtSend = new Date();

    public AckData(String strXml)
    {
        m_strXML = strXml;
        m_strAckGUID = KDSUtil.createNewGUID();
        m_dtSend.setTime(System.currentTimeMillis());

    }
    public AckData(String guid, String data, Date dtSend)
    {
        m_strXML = data;
        m_strAckGUID = guid;
        m_dtSend.setTime(dtSend.getTime());
    }
    public String getGuid()
    {
        return m_strAckGUID;
    }

    public void setGuid(String guid)
    {
        m_strAckGUID = guid;
    }
    public boolean isTimeout()
    {
        long l = System.currentTimeMillis() - m_dtSend.getTime();
        Log.i(TAG, "timeout=" + l);
        return (l > AckManager.ACK_TIMEOUT);

//        TimeDog td = new TimeDog(m_dtSend);
//        return (td.is_timeout(ACK_TIMEOUT));

    }

    public String getXmlData()
    {
        return m_strXML;
    }

    public String getWithAckXmlData()
    {
        String s = ACKGUID + m_strAckGUID + ACKGUID_END;
        s += getXmlData();
        return s;
    }

    /**
     * <ACKGUID>92348529384592834598234</ACKGUID>
     * <Transaction></Transaction>
     * @param xmlData
     * @return
     *  ackguid
     */
    static public String parseAckGuid(String xmlData)
    {
        //<ACKGUID>92348529384592834598234</ACKGUID>
        int nend = xmlData.indexOf(AckData.ACKGUID_END);
        if (nend <0)
            return "";
        int nstart = xmlData.indexOf(AckData.ACKGUID);
        nend += AckData.ACKGUID_END.length();
        String ackguid = xmlData.substring(nstart, nend);
        //xmlData = xmlData.substring(nend);
        ackguid = ackguid.replace(AckData.ACKGUID, "");
        ackguid = ackguid.replace(AckData.ACKGUID_END, "");

        return ackguid;
    }

    static public String removeAckGuid(String xmlData, String ackGuid)
    {
        String s = ACKGUID + ackGuid + ACKGUID_END;
        xmlData = xmlData.replace(s, "");
        return xmlData;
    }
    public void resetTimer()
    {
        m_dtSend.setTime(System.currentTimeMillis());
    }

    public Date getSendDate()
    {
        return m_dtSend;
    }

}
