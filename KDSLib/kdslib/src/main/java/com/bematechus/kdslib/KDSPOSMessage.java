/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bematechus.kdslib;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

import java.util.Date;

/**
 *
 * @author David.Wong
 */
public class KDSPOSMessage {
    private boolean mDeleteIt = false;
    private String mID = "";
    private String mStation = "";
    private String mScreen = "";
    private String m_strMessage = "";
    private Date mCreatedDate = new Date();


    public enum VALID_POSMSG_XML_FIELD
    {
        Station,
        Screen,
        Message,        
        Count
    };
    protected boolean[] m_arValidFields = new boolean[VALID_POSMSG_XML_FIELD.Count.ordinal()];
    
    public KDSPOSMessage()
    {
        resetXmlFieldsValidFlag();
    }
    
     public void resetXmlFieldsValidFlag()
    {

         for (int i = 0; i< VALID_POSMSG_XML_FIELD.Count.ordinal(); i++ )
        {
            m_arValidFields[i] = false;
        }
    }
    public void setXmlFieldValid(VALID_POSMSG_XML_FIELD field)
    {
        m_arValidFields[field.ordinal()] = true;
    }
    public boolean getXmlFieldValid(VALID_POSMSG_XML_FIELD field)
    {
        return m_arValidFields[field.ordinal()];
    }
    public String getStation()
    {
        return mStation;
    }
    public void setStation(String station)
    {
        mStation = station;
    }
    public String getScreen()
    {
        return mScreen;
    }
    public void setScreen(String strScreen)
    {
        mScreen = strScreen;
    }
    public String getMessage()
    {
        return m_strMessage;
    }
    public void setMessage(String strMsg)
    {
        m_strMessage = strMsg;
    }

    public void setID(String id)
    {
        mID = id;
    }
    public String getID()
    {
        return mID;
    }

    public void setDeleteMe(boolean bDelete)
    {
        mDeleteIt = bDelete;
    }
    public boolean getDeleteMe()
    {
        return mDeleteIt;
    }

    public Date getDate()
    {
        return mCreatedDate;
    }

    /**
     *
     * @param timeout
     *  ms
     * @return
     */
    public boolean isTimeout(int timeout)
    {
        TimeDog t = new TimeDog(getDate());
        return t.is_timeout(timeout);
    }
}
