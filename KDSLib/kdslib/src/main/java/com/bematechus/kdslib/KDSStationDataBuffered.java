package com.bematechus.kdslib;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */
/**
 * Created by Administrator on 2015/10/15 0015.
 * The data buffered, while we build the tcp/ip connection.
 * We have to save them, then send them after the connection build.
 */
public class KDSStationDataBuffered {

    String m_strData = "";
    String m_strDescription = "";
    String m_strOrderGuid = "";
    String m_strItemGuid = "";
    public void setItemGuid(String guid)
    {
        m_strItemGuid = guid;
    }
    public String getItemGuid()
    {
        return m_strItemGuid;
    }

    KDSStationDataBuffered(String data)
    {
        m_strData = data;
        m_strDescription = "";
    }

    KDSStationDataBuffered(String data, String description)
    {
        m_strData = data;
        m_strDescription = description;

    }

    public String getOrderGuid()
    {
        return m_strOrderGuid;
    }
    public void setOrderGuid(String guid)
    {
        m_strOrderGuid = guid;
    }

    public String getData()
    {
        return m_strData;
    }
    public void setData(String data)
    {
        m_strData = data;
    }
    public String getDescription()
    {
        return m_strDescription;
    }


    public  void setDescription(String description)
    {
        m_strDescription = description;
    }


    static KDSStationDataBuffered create(String data)
    {
        return new KDSStationDataBuffered(data);
    }

    static KDSStationDataBuffered create(String data, String description)
    {
        return new KDSStationDataBuffered(data, description);
    }


}
