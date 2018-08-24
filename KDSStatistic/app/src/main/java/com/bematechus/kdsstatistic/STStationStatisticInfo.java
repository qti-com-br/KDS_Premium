package com.bematechus.kdsstatistic;

import java.util.Date;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSUtil;

/**
 * Created by Administrator on 2016/8/4.
 */
public class STStationStatisticInfo extends KDSStationIP {


    enum StationStatisticStatus
    {
        Unknown,
        Active,
        Connected,
        Updating,
        Updated,

    }
    Date m_dtLastUpdated= KDSUtil.createInvalidDate();
    String m_strInfo="";
    long m_nTotalSize= 0;
    long m_nFinishedSize=0;

    StationStatisticStatus m_nStatus = StationStatisticStatus.Active;;


    public void setStatus(StationStatisticStatus status)
    {
        m_nStatus = status;
        if (status == StationStatisticStatus.Active)
        {
            m_nFinishedSize = 0;
            m_nTotalSize = 0;
        }
    }

    public StationStatisticStatus getStatus()
    {
        return m_nStatus;
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

    public String getStatusInfo()
    {
        switch (m_nStatus)
        {

            case Unknown:
                return KDSApplication.getContext().getString(R.string.unknown);// "Unknown";

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
