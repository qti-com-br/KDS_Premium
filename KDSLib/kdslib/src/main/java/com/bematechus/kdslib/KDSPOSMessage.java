/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bematechus.kdslib;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */
/**
 *
 * @author David.Wong
 */
public class KDSPOSMessage {
    private int m_nStation = -1;
    private int m_nScreen = -1;
    private String m_strMessage = "";
    
     public enum VALID_POSMSG_XML_FIELD
    {
        Station,
        Screen,
        Message,        
        Count
    };
    protected boolean[] m_arValidFields;
    
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
    public int getStation()
    {
        return m_nStation;
    }
    public void setStation(int nStation)
    {
        m_nStation = nStation;
    }
    public int getScreen()
    {
        return m_nScreen;
    }
    public void setScreen(int nScreen)
    {
        m_nScreen = nScreen;
    }
    public String getMessage()
    {
        return m_strMessage;
    }
    public void setMessage(String strMsg)
    {
        m_strMessage = strMsg;
    }
}
