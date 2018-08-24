package com.bematechus.kdsrouter;

import com.bematechus.kdslib.KDSPosNotificationFactory;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSToStation;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by David.Wong on 2018/7/30.
 *  For order acknowledgement
 * Rev:
 *
 */
public class RouterAck {

    Date m_dtCreated = new Date();
    String m_strOriginalFileName = "";
    String m_strOrderName = "";
    String m_strReceivedXml = "";

    ArrayList<KDSToStation> m_arTargetStations = new ArrayList<>();
    ArrayList<String> m_arGoodAckStations = new ArrayList<>();
    ArrayList<String> m_arErrorAckStations = new ArrayList<>();

    final int TIMEOUT = 5000; //10secs
    public boolean isTimeout()
    {
        TimeDog t = new TimeDog(m_dtCreated);
        return (t.is_timeout(TIMEOUT));

    }

    public void setOriginalFileName(String fileName)
    {
        m_strOriginalFileName = fileName;
    }

    public String getOriginalFileName()
    {
        return m_strOriginalFileName;
    }

    public boolean setAckResultAccordingToErrorCode(String stationID, String errorCode, ArrayList<KDSStationIP> activeStations)
    {
        if (errorCode.equals(KDSPosNotificationFactory.ACK_ERR_BAD))
            m_arErrorAckStations.add(stationID);
        else
            m_arGoodAckStations.add(stationID);

        return isAllStationReturnAck(activeStations);

    }

    public boolean isAllStationReturnAck( ArrayList<KDSStationIP> activeStations)
    {
        boolean bResult = true;
        for (int i=0; i< m_arTargetStations.size(); i++)
        {
            String stationID = m_arTargetStations.get(i).getPrimaryStation();
            String stationIDSlave = m_arTargetStations.get(i).getSlaveStation();

            if (!isExistedInArray(m_arGoodAckStations, stationID))
            {
                if (!isExistedInArray(m_arErrorAckStations, stationID))
                {
                    if (!stationIDSlave.isEmpty()) {
                        if (!isExistedInArray(m_arGoodAckStations, stationIDSlave)) {
                            if (!isExistedInArray(m_arErrorAckStations, stationIDSlave)) {
                                bResult = false;
                            }
                        }
                    }
                    else
                        bResult = false;
                }
            }
        }
        //2.0.17, comment it. I feel this is wrong.
//        if (!bResult)
//        {//check if all active station have return ack
//            for (int i=0; i< activeStations.size(); i++)
//            {
//                String stationID = activeStations.get(i).getID();
//                if (isExistedInArray(m_arGoodAckStations, stationID) ||
//                    isExistedInArray(m_arErrorAckStations, stationID)
//                        )
//                    bResult = true;
//            }
//        }


        return bResult;
    }

    static public boolean isExistedInArray(ArrayList<String> ar, String stationID)
    {
        if (stationID.isEmpty()) return true;
        for (int i=0; i< ar.size(); i++)
        {
            if (ar.get(i).equals(stationID))
                return true;
        }
        return false;
    }

    public void setOrderName(String orderName)
    {
        m_strOrderName = orderName;
    }

    public String getOrderName()
    {
        return m_strOrderName;
    }

    public void setXmlText(String text)
    {
        m_strReceivedXml = text;
    }

    public String getXmlText()
    {
        return m_strReceivedXml;
    }

    public ArrayList<KDSToStation> getTargetStations()
    {
        return m_arTargetStations;
    }

    public ArrayList<String> getGoodAckStations()
    {
        return m_arGoodAckStations;
    }

    public ArrayList<String> getErrorAckStations()
    {
        return m_arErrorAckStations;
    }

    public String createRouterAck(String routerID, RouterAck returnFileName)
    {
        String errorCode = KDSPosNotificationFactory.ACK_ERR_OK;//"0";
        if (m_arErrorAckStations.size()>0 )
            errorCode = KDSPosNotificationFactory.ACK_ERR_BAD;// "1";
        if (isTimeout())
            errorCode = KDSPosNotificationFactory.ACK_ERR_TIMEOUT;// "1";

        boolean bgood = errorCode.equals(KDSPosNotificationFactory.ACK_ERR_OK);

        String fileName =KDSPosNotificationFactory.smbFullPathToFileName( m_strOriginalFileName);

        if (fileName.isEmpty())
        {
            fileName = createAckFileName(fileName, m_strOrderName, errorCode.equals("1"));
        }
        else
        {
            if (bgood)
            {
                fileName = KDSPosNotificationFactory.PREF_ack +fileName;
            }
            else
            {
                fileName = KDSPosNotificationFactory.PREF_err + fileName;
            }
        }
        String s = KDSPosNotificationFactory.createOrderAcknowledgementNotification(routerID, m_strReceivedXml,m_strOrderName, errorCode, fileName);
        returnFileName.setXmlText(fileName);

        return s;
    }

    static public String createAckFileName(String smbFileName, String orderName,boolean bGoodOrder)
    {
        String fileName = "";
        if (bGoodOrder) {
            fileName +=KDSPosNotificationFactory.PREF_ack;// "ack_";
        }
        else
            fileName +=KDSPosNotificationFactory.PREF_err;// "err_";
        if (!smbFileName.isEmpty()) {
            String f = KDSPosNotificationFactory.smbFullPathToFileName(smbFileName);
            fileName += f;
        }
        else {
            //from tcp/ip
            fileName += "Order" + orderName + ".xml";
        }

        fileName = fileName.toLowerCase();
        fileName = fileName.replace(".xml", "");
        fileName += "_" + KDSUtil.createNewGUID();
        fileName += ".xml";
        return fileName;
    }

    public ArrayList<String> getSendToStations()
    {
        ArrayList<String> ar = new ArrayList<>();

        for (int i=0; i< m_arTargetStations.size(); i++)
        {
            KDSToStation station = m_arTargetStations.get(i);
            String primary = station.getPrimaryStation();
            String slave = station.getSlaveStation();
            if (!isExistedInArray(ar, primary))
                ar.add(primary);
            if (!isExistedInArray(ar, slave))
                ar.add(slave);

        }
        return ar;
    }
}
