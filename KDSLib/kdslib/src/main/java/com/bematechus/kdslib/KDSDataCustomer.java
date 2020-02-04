package com.bematechus.kdslib;

/**
 * Created by David.Wong on 2019/4/26.
 * <ID>1</ID> ID number that is unique under the customer group tag
 * <Name>John</Name> Customers name. (Not yet in use)
 * <Phone>15555555555</Phone> Phone number used for SMS messaging. Twilio integration.
 * <Address>123 Smith st</Address> Customers address. (Not yet in use)
 * <Address2>Suite 100</Address2> Customers building or suite Number. (Note yet in use)
 * <City>Albany</City> Customers city. (Not yet in use)
 * <State>NY</State> Customers state. (Not yet in use)
 * <Zip>11451</Zip> Customers Zip code. (Not yet in use)
 * Rev:
 */
public class KDSDataCustomer extends KDSData{
    String m_strID = "";
    String m_strName="";
    String m_strPhone = "";
    //following are not used by kds.
    String m_strAddress="";
    String m_strAddress2 = "";
    String m_strCity= "";
    String m_strState = "";
    String m_strZip = "";

    public void copyTo(KDSData component)
    {
        KDSDataCustomer c = (KDSDataCustomer)component;
        c.setID(getID());
        c.setName(getName());
        c.setPhone(getPhone());
        c.setAddress(getAddress());
        c.setAddress2(getAddress2());
        c.setCity(getCity());
        c.setState(getState());
        c.setZip(getZip());
    }
    public KDSData clone()
    {
        KDSDataCustomer c = new KDSDataCustomer();
        copyTo(c);
        return c;
    }
    public void setID(String id)
    {
        m_strID = id;
    }
    public String getID()
    {
        return m_strID;
    }

    public void setName(String strName)
    {
        m_strName = strName;
    }
    public String getName()
    {
        return m_strName;
    }
    public void setPhone(String strPhone)
    {
        m_strPhone = strPhone;
    }
    public String getPhone()
    {
        return m_strPhone;
    }

    public void setAddress(String addr)
    {
        m_strAddress = addr;
    }
    public String getAddress()
    {
        return m_strAddress;
    }

    public void setAddress2(String addr)
    {
        m_strAddress2 = addr;
    }
    public String getAddress2()
    {
        return m_strAddress2;
    }

    public void setCity(String city)
    {
        m_strCity = city;
    }
    public String getCity()
    {
        return m_strCity;
    }

    public void setState(String state)
    {
        m_strState = state;
    }
    public String getState()
    {
        return m_strState;
    }

    public void setZip(String zip)
    {
        m_strZip = zip;
    }
    public String getZip()
    {
        return m_strZip;
    }
}
