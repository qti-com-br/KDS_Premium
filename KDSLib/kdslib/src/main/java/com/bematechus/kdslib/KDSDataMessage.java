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
public class KDSDataMessage extends KDSData {
    static public final int FOR_Order = 0;
    static public final int FOR_Item = 1;
    static public final int FOR_Condiment = 2;
    
    //protected int m_nComponentID;
    protected String m_strComponentGUID=""; //use the GUID as id
    protected int m_nForComponentType=0; //0: order
                          //1: item
                         // 2: condiment
    protected String m_strMessage = "";
    protected  int m_bg = -1;
    protected  int m_fg = -1;

    Object m_tag = null; //use it to save my parent;

    public void setFocusTag(Object obj)
    {
        m_tag = obj;
    }
    public Object getFocusTag()
    {
        return m_tag;
    }

    public KDSDataMessage()
    {
        
    }

    public KDSDataMessage(String strComponentGUID)
    {
        m_strComponentGUID = strComponentGUID;
    }

    public KDSDataMessage(String strComponentGUID, String myGuid)
    {
        super(myGuid);
        m_strComponentGUID = strComponentGUID;
    }
    

    
     public String getComponentGUID()
    {
        return m_strComponentGUID;
        
    }
    public void setComponentGUID(String strGUID)
    {
        m_strComponentGUID = strGUID;
    }
    public int getForComponentType()
    {
        return m_nForComponentType;
    }
    public void setForComponentType(int nType)
    {
        m_nForComponentType = nType;
    }
    
    public void setMessage(String msg)
    {
        m_strMessage = msg;
    }
    
    public String getMessage()
    {
        return m_strMessage;
    }

    public void setBG(int color)
    {
        m_bg = color;
    }
    public int getBG()
    {
        return m_bg;
    }
    public void setFG(int color)
    {
        m_fg = color;
    }
    public int getFG()
    {
        return m_fg;
    }

    public boolean isColorValid()
    {
        return (!(m_bg ==-1 && m_fg == -1));
    }
    /***************************************************************************
     * 
     * @param component 
     */
    public void copyTo(KDSData component)
    {
        KDSDataMessage obj = (KDSDataMessage) component;
        super.copyTo(obj); 
        obj.m_strComponentGUID = m_strComponentGUID;
        obj.m_nForComponentType = m_nForComponentType;
        //obj.m_nComponentType = m_nComponentType;
        obj.m_strMessage = m_strMessage;
        obj.setBG( obj.getBG());
        obj.setFG(obj.getFG());

        
    }
    public KDSData clone()
    {
        KDSDataMessage msg = new KDSDataMessage();
        copyTo(msg);
        return msg;
    }
    
  public String sqlAddNew()
    {
        String sql = "insert into messages ("
                //+ "ComponentID ,GUID,ComponentGUID, ComponentType ,MessageGUID ,Description, BG, FG  ) values ("
                + "GUID,ObjGUID, ObjType ,Description, BG, FG) values ("
                //+ "" + getComponentID() + ","
                + "'" + getGUID() + "'," //message id
                + "'" + getComponentGUID() + "',"
                + KDSUtil.convertIntToString(getForComponentType()) +","
                //+ "'" + getGUID() + "',"
                + "'" +fixSqliteSingleQuotationIssue(  getMessage()) + "',"
                +  "0,"
                +  "0)";
        
        return sql;


    }
    public String sqlUpdate()
    {
         String sql = "update condiments set "
               // + "ComponentID='" + getComponentID() + "',"
                + "ObjGUID='" + getComponentGUID() + "',"
                + "ObjType='"+KDSUtil.convertIntToString(getForComponentType()) + ","
               // + "MessageGUID='"+ getGUID() + "',"
                + "Description='" + fixSqliteSingleQuotationIssue( getMessage()) + "',"
                + "DBTimeStamp='"+KDSUtil.convertDateToString(getTimeStamp())
                //+"' where id=" + Common.KDSUtil.ConvertIntToString(getID());
                 +"' where guid='" +  getGUID()+"'";

         return sql;

    }
    public String sqlDelete()
    {
        //String sql = sqlDelete(getID());
        String sql = sqlDelete(getGUID());
        return sql;
    }


    
    public static String sqlDelete(String strGUID)
    {
        String sql = "delete from messages where guid='"+ strGUID + "'";
        return sql;
    }
    
    public boolean isEqual(KDSDataMessage m)
    {
        return (!this.getMessage().equals(m.getMessage()));
            
    }
    
    
    
    
}
