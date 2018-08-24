package com.bematechus.kdsstatistic;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSUtil;

import java.util.Date;

/**
 * Created by Administrator on 2016/8/4.
 */
public class SOSKDSStationSOSInfo extends KDSStationIP {


    enum StationSOSStatus
    {
        Unknown,
        Disabled,
        Active,
        Connected,
        Updating,
        Updated,

    }
    Date m_dtLastUpdated= KDSUtil.createInvalidDate();
    String m_strInfo="";
    long m_nTotalSize= 0;
    long m_nFinishedSize=0;

    StationSOSStatus m_nStatus = StationSOSStatus.Active;;

    public void KDSStationStatisticInfo()
    {

        m_dtLastUpdated = KDSUtil.createInvalidDate();
        m_strInfo = "";
        m_nFinishedSize = 0;
        m_nTotalSize = 0;
        m_nStatus = StationSOSStatus.Active;
    }

    public void KDSStationStatisticInfo(KDSStationIP station)
    {
        this.copyFrom(station);
        m_dtLastUpdated = KDSUtil.createInvalidDate();
        m_strInfo = "";
        m_nFinishedSize = 0;
        m_nTotalSize = 0;
        m_nStatus = StationSOSStatus.Active;
    }

    public void setStatus(StationSOSStatus status)
    {
        m_nStatus = status;
        if (status == StationSOSStatus.Active)
        {
            m_nFinishedSize = 0;
            m_nTotalSize = 0;
        }
    }

    public StationSOSStatus getStatus()
    {
        return m_nStatus;
    }

    public void setLastUpdatedDate(Date dt)
    {
        m_dtLastUpdated = dt;
    }
    public void setLastUpdateDate(String dt)
    {
        if (dt.isEmpty())
            m_dtLastUpdated = KDSUtil.createInvalidDate();
        else
            m_dtLastUpdated = KDSUtil.convertStringToDate(dt);
    }
    public Date getLastUpdateDate()
    {
        return m_dtLastUpdated;
    }

    public void setInfo(String info)
    {
        m_strInfo = info;

    }
    public String getInfo()
    {
        if (m_strInfo.isEmpty())
        {
            return getStatusInfo();
        }
        return m_strInfo;
    }
    public String getLastUpdateDateString()
    {
        if (KDSUtil.isInvalidDate(m_dtLastUpdated))
            return  KDSApplication.getContext().getString(R.string.unknown);//"Unknown";

        return KDSUtil.convertDateToString(m_dtLastUpdated);
    }
    public String getStatusInfo()
    {
        return getStatusInfo(m_nStatus);

//        switch (m_nStatus)
//        {
//
//            case Unknown:
//                return KDSApplication.getContext().getString(R.string.unknown);// "Unknown";
//            case Disabled:
//                return KDSApplication.getContext().getString(R.string.disabled);
//            case Active:
//                return KDSApplication.getContext().getString(R.string.active);//"Active";
//
//            case Connected:
//                return KDSApplication.getContext().getString(R.string.connected);//"Connected";
//            case Updating:
//                return KDSApplication.getContext().getString(R.string.updating);//"Updating";
//            case Updated:
//                return KDSApplication.getContext().getString(R.string.updated);//"Updated";
//            default:
//                return "";
//        }
    }

    static public String getStatusInfo(StationSOSStatus status)
    {
        switch (status)
        {

            case Unknown:
                return KDSApplication.getContext().getString(R.string.unknown);// "Unknown";
            case Disabled:
                return KDSApplication.getContext().getString(R.string.disabled);
            case Active:
                return KDSApplication.getContext().getString(R.string.active);//"Active";

            case Connected:
                return KDSApplication.getContext().getString(R.string.connected);//"Connected";
            case Updating:
                return KDSApplication.getContext().getString(R.string.updating);//"Updating";
            case Updated:
                return KDSApplication.getContext().getString(R.string.updated);//"Updated";
            default:
                return "";
        }
    }
    public void setTotalSize(long nSize)
    {
        m_nTotalSize = nSize;
    }
    public long getTotalSize()
    {
        return m_nTotalSize;
    }
    public void setFinishedSize(long nSize)
    {
        m_nFinishedSize = nSize;
    }
    public long getFinishedSize()
    {
        return m_nFinishedSize;
    }


}
