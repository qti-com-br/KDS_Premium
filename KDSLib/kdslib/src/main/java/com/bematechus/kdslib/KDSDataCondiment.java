/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bematechus.kdslib;

/**
 *
 * @author David.Wong
 */
public class KDSDataCondiment extends KDSData {
    
   /****************************************************************************
    * variables
    */
    protected int m_nItemID = -1;//the itemID in database table
    protected String m_strItemGUID = "";

    protected String m_strCondimentName = ""; //condiment name
    protected String m_strDescription = ""; //condiment text
    protected int m_nBG = 0; //background color
    protected int m_nFG = 0; //foreground color;
    protected boolean m_bHiden = false;
    protected boolean m_bBumped = false;
    protected float m_fltQty = 1; //kpp1-414.

    //messages
    protected KDSDataMessages m_condimentMesseges = new KDSDataMessages();

    protected KDSToStations m_hiddenStations = new KDSToStations(); //just for parse xml. If this station is hidden, set "hidden" variable.

    Object m_tag = null; //use it to save my parent(item);


    /***************************************************************************/
    
    protected int m_nTransactionType = KDSDataOrder.TRANSTYPE_ADD;
    public enum VALID_CONDIMENT_XML_FIELD
    {
        Name,
        Description,
        BG,
        FG,
        Messages,
        PrepTime,
        Qty, //kpp1-414
        Count
    };
    protected boolean[] m_arValidFields; 



    public void setFocusTag(Object obj)
    {
        m_tag = obj;
    }
    public Object getFocusTag()
    {
        return m_tag;
    }
    /***************************************************************************
     * 
     */
    public KDSDataCondiment()
    {

        m_arValidFields = new boolean[VALID_CONDIMENT_XML_FIELD.Count.ordinal()];
        resetXmlFieldsValidFlag();
    }
    
    public KDSDataCondiment(String itemGUID)
    {
        this.setItemGUID(itemGUID);

        m_arValidFields = new boolean[VALID_CONDIMENT_XML_FIELD.Count.ordinal()];
        resetXmlFieldsValidFlag();
    }

    public KDSDataCondiment(String itemGUID, String myGuid)
    {
        super(myGuid);
        this.setItemGUID(itemGUID);

        m_arValidFields = new boolean[VALID_CONDIMENT_XML_FIELD.Count.ordinal()];

    }

    public void resetXmlFieldsValidFlag()
    {
         for (int i = 0; i< VALID_CONDIMENT_XML_FIELD.Count.ordinal(); i++ )
        {
            m_arValidFields[i] = false;
        }
    }
    
    public void setXmlFieldValid(VALID_CONDIMENT_XML_FIELD field)
    {
        m_arValidFields[field.ordinal()] = true;
    }
    public boolean getXmlFieldValid(VALID_CONDIMENT_XML_FIELD field)
    {
        return m_arValidFields[field.ordinal()];
    }
    
    public void setTransType(int nType)
     {
         m_nTransactionType = nType;
     }
     public int getTransType()
     {
         return m_nTransactionType;
     }
    
    public KDSDataMessages getMessages()
    {
        return m_condimentMesseges;
    }
    
    public void setMessages(KDSDataMessages msgs)
    {
        m_condimentMesseges = msgs;
    }
    
    public void setItemID(int nID)
    {
        m_nItemID = nID;
    }
    public int getItemID()
    {
        return m_nItemID;
    }
    
    public String getItemGUID()
    {
        return m_strItemGUID;
    }
    public void setItemGUID(String strGUID)
    {
        m_strItemGUID = strGUID;
    }
    

    public void setCondimentName(String strName)
    {
        m_strCondimentName = strName;
    }
    public String getCondimentName()
    {
        return m_strCondimentName;
    }
    public void setDescription(String description)
    {
        m_strDescription = description;
    }
    public String getDescription()
    {
        return m_strDescription;
    }
    
    public void setBG(int bg)
    {
        m_nBG = bg;
    }
    public int getBG()
    {
        return m_nBG;
    }
    public void setFG(int fg)
    {
        m_nFG = fg;
    }
    public int getFG()
    {
        return m_nFG;
    }
    public boolean isAssignedColor()
    {
        return isAssignedColor(m_nBG, m_nFG);
//        if (m_nFG != 0 || m_nBG != 0)
//            return true;
//        return false;
    }

    public void setHiden(boolean bHiden)
    {
        m_bHiden = bHiden;
    }
    public  boolean getHiden()
    {
        return m_bHiden;
    }
    public void setBumped(boolean bBumped)
    {
        m_bBumped = bBumped;
    }
    public  boolean getBumped()
    {
        return m_bBumped;
    }

    /***************************************************************************
     * 
     * @param obj 
     */
    public void copyTo(KDSData obj)
    {
        super.copyTo(obj);
        KDSDataCondiment c = (KDSDataCondiment)obj;
        c.m_nItemID = m_nItemID;
        c.m_strItemGUID = m_strItemGUID;
        c.m_strGUID = m_strGUID;
        c.m_strCondimentName = m_strCondimentName;
        c.m_strDescription = m_strDescription;
        c.m_nBG = m_nBG;
        c.m_nFG = m_nFG;
        c.m_bHiden = m_bHiden;
        c.m_bBumped = m_bBumped;
        c.m_fltQty = m_fltQty; //kpp1-414
        m_condimentMesseges.copyTo(c.m_condimentMesseges); 
        
        
    }
    public KDSData clone()
    {
        KDSDataCondiment obj = new KDSDataCondiment();
        this.copyTo(obj);
        return obj;
        
    }
    public String sqlAddNew(String tblName)
    {
        String sql = "insert into "
                + tblName
                + " ("
                + "GUID,ItemGUID,Name,Description,BG,FG,Hiden,Bumped,Qty) values (" //kpp1-414 add qty
                + "'" + getGUID() + "'"
                + ",'" + getItemGUID() + "'"
                + ",'" +fixSqliteSingleQuotationIssue(  getCondimentName()) + "'"
                + ",'" +fixSqliteSingleQuotationIssue(  getDescription()) + "'"
                + "," + KDSUtil.convertIntToString(getBG())
                + "," + KDSUtil.convertIntToString(getFG())
                + "," + KDSUtil.convertBoolToString(getHiden())
                + "," + KDSUtil.convertBoolToString(getBumped())
                + "," + KDSUtil.convertFloatToString(getQty()) //kpp1-414
                + ")";
        
                
        return sql;


    }
    public String sqlUpdate()
    {
         String sql = "update condiments set "
                + "ItemGUID='"+ getItemGUID() + "'"
                + ",Name='"+ getCondimentName() + "'"
                + ",Description='" + getDescription() + "'"
                + ",BG=" + KDSUtil.convertIntToString(getBG())
                + ",FG="+ KDSUtil.convertIntToString(getFG())
                + ",Hiden="+ KDSUtil.convertBoolToString(getHiden())
                + ",Bumped="+ KDSUtil.convertBoolToString(getBumped())
                + ",qty=" + KDSUtil.convertFloatToString(getQty()) //kpp1-414
                + ",DBTimeStamp='"+ KDSUtil.convertDateToString(getNow()) + "'"//TimeStamp())
                //+"' where id=" + Common.KDSUtil.ConvertIntToString(getID());
                +" where guid='" + getGUID() + "'";

         return sql;

    }
    public String sqlDelete()
    {

        String sql = sqlDelete(getGUID());
        return sql;
    }

//    public static String sqlDelete(int nID)
//    {
//        String sql = "delete from condiments where id="+ Common.KDSUtil.ConvertIntToString(nID);
//        return sql;
//    }
    
    public static String sqlDelete(String strGUID)
    {
        String sql = "delete from condiments where guid='"+ strGUID +"'";
        return sql;
    }
    
    public boolean isEqual(KDSDataCondiment condiment)
    {
        if (this.getBG() != condiment.getBG())
            return false;
        if (!this.getCondimentName().equals(condiment.getCondimentName()))
            return false;
        if (this.getDescription().equals(condiment.getDescription()))
            return false;
        if (this.getFG() != condiment.getFG())
            return false;

        //kpp1-414, add qty
        if (this.getQty() != condiment.getQty())
            return false;
        return true;
            
    }

    /**
     * Rev:
     *  Add qty support.
     * @param condimentReceived
     * @return
     */
    public boolean modifyCondiment(KDSDataCondiment condimentReceived)
    {
        boolean bResult = false;
        if (condimentReceived.getXmlFieldValid(VALID_CONDIMENT_XML_FIELD.BG))
        {
            this.setBG(condimentReceived.getBG());
            bResult = true;
        }
        if (condimentReceived.getXmlFieldValid(VALID_CONDIMENT_XML_FIELD.Description))
        {
            this.setDescription(condimentReceived.getDescription());
            bResult = true;
        }
        if (condimentReceived.getXmlFieldValid(VALID_CONDIMENT_XML_FIELD.FG))
        {
            this.setFG(condimentReceived.getFG());
            bResult = true;
        }

        //kpp1-414 add qty
        if (condimentReceived.getXmlFieldValid(VALID_CONDIMENT_XML_FIELD.Qty))
        {
            this.setQty(condimentReceived.getQty());
            bResult = true;
        }

        if (condimentReceived.getXmlFieldValid(VALID_CONDIMENT_XML_FIELD.Messages))
        {
            condimentReceived.getMessages().copyTo(this.getMessages());
            bResult = true;
        }
        
        return bResult;
    }

    /**
     * keep it while parse xml string. Don't save to database.
     * It is different with windows KDS.
     * After do filter, it will change to "hidden" variable.
     * @param hiddenStations
     */
    public void setHiddenStations(String hiddenStations)
    {

        m_hiddenStations.parseString(hiddenStations);
    }
    public boolean isHiddenStation(String stationID)
    {
        if (!m_hiddenStations.isAssigned()) return false;
        return (m_hiddenStations.findStation(stationID) != KDSToStations.PrimarySlaveStation.Unknown);
    }

    public void setHiddenAccordingToHiddenStations(String stationID)
    {
        setHiden(isHiddenStation(stationID));
    }

    /**
     * kpp1-414
     * @return
     */
    public float getQty()
    {
        return m_fltQty;
    }

    /**
     * KPP1-414
     * @param nQty
     */
    public void setQty(float nQty)
    {
        m_fltQty = nQty;
    }


}
