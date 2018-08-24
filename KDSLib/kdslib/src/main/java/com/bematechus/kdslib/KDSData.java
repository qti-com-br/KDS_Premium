

package com.bematechus.kdslib;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */
import android.graphics.Point;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author David.Wong
 * The base class for all data from database.
 */
public class KDSData {
    protected String m_strGUID = ""; //its guid for distingush it in whole PCKDS system.
    protected Date m_dtDBTimeStamp = new Date();
    protected  boolean m_bDimColor = false;
    Object m_objTag = null;

    public ArrayList<Point> m_tempShowMeNeedBlockLines = new ArrayList<>();//1; //for text wrap, saveing it here is for  efficiency.

    public KDSData()
    {
        autoAssignGUID();
        setDBTimeStampToNow();
    }

    public void setTag(Object obj)
    {
        m_objTag = obj;
    }
    public Object getTag()
    {
        return m_objTag;
    }

    public boolean isDimColor()
    {
        return m_bDimColor;
    }
    public void setDimColor(boolean bDim)
    {
        m_bDimColor = bDim;
    }


    static public Date getNow()
     {
         return new Date();
     }


    public KDSData(String guid)
    {

        m_strGUID = guid;

        setDBTimeStampToNow();
    }

    public void setDBTimeStampToNow()
    {

    }

     public Date getTimeStamp()
     {
         return m_dtDBTimeStamp;
     }
    public Date newTimeStamp()
    {
        return new Date();
    }
     public void setTimeStamp(Date dt)
     {
         m_dtDBTimeStamp = dt;

     }

    public void setTimeStamp(long ms)
    {
        if (ms != 0)
            m_dtDBTimeStamp.setTime(ms);

    }


    public String getGUID()
    {
        return m_strGUID;
    }
    
    public void setGUID(String strGUID)
    {
        m_strGUID = strGUID;
    }
    protected void autoAssignGUID()
    {
        m_strGUID = KDSUtil.createNewGUID();//  CreateNewGUID();
    }

    public void createNewGuid()
    {
        autoAssignGUID();
    }
    public void copyTo(KDSData obj)
    {

        obj.m_strGUID = m_strGUID;

        obj.m_dtDBTimeStamp=m_dtDBTimeStamp;

    }
    public KDSData clone()
    {
        return null;
    }
    public static String fixSqliteSingleQuotationIssue(String strOriginal)
    {
         String s = strOriginal;
         s = s.replaceAll("'", "''");
         return s;
    }
}
