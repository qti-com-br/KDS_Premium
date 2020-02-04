package com.bematechus.kdsrouter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.bematechus.kdslib.KDSData;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Administrator on 2016/11/11.
 */
public class WeekEvent extends KDSData {


    static final int EVENT_COLOR_BG = Color.rgb(186,230,171);
    static final int SHADOW_WIDTH = 3;
    final static int GAP_BAND_WIDTH = 6;
    final static double HALF_HOUR_DIVIDER = 0.7;

    public Rect m_rcDraw = new Rect();
    String m_strSubject = "";
    String m_strOrderID = "";

    FloatTime m_To = new FloatTime();
    FloatTime m_From = new FloatTime();
    int m_nWeekDay = 0;
    Object m_pData = null;
    FloatTime m_dispFrom = new FloatTime(); //the start and end can been change for min size
    FloatTime m_dispTo = new FloatTime();

    ArrayList<KDSDataItem> m_arItems = new ArrayList<>();

    Date m_lastFireTime = KDSUtil.createInvalidDate();

    public WeekEvent()
    {

    }
    public WeekEvent(WeekEvent item)
    {
        copyFrom(item);
    }

    public boolean isFired()
    {
        return (!KDSUtil.isInvalidDate(m_lastFireTime));
    }
    public void resetState()
    {
        m_lastFireTime = KDSUtil.createInvalidDate();
    }
    public void setFired()
    {
        m_lastFireTime = new Date();
    }
    public Date getFiredTime()
    {
        return m_lastFireTime;
    }

    public void setFiredTime(Date dt)
    {
        m_lastFireTime = dt;
    }

    public void copyFrom(WeekEvent item)
    {
        setGUID(item.getGUID());
        m_strSubject = item.getSubject();
        m_strOrderID = item.getOrderID();

        m_To = new FloatTime(item.getEndTime().get());
        m_From = new FloatTime(item.getTimeFrom().get());
        m_nWeekDay = item.getWeekDay();
        m_arItems = item.getItems();
        m_pData = item.getData();
    }

    public ArrayList<KDSDataItem> getItems()
    {
        return m_arItems;
    }

    public void setItems(ArrayList<KDSDataItem> ar)
    {
        m_arItems = ar;
    }


    public String getOrderID()
    {
        return m_strOrderID;
    }
    public void setOrderID(String orderID)
    {
        m_strOrderID = orderID;
    }
    //ver4.0
    boolean isOverLapping(WeekEvent item)
    {
        if (m_nWeekDay != item.m_nWeekDay) return false;
        if (item == this) return false;


        if ( item.m_dispTo.get() < m_dispFrom.get() ||
            item.m_dispFrom.get() > m_dispTo.get() )
            return false;
        return true;
    }

    Rect getDrawRect()
    {
        return m_rcDraw;
    }

    void setSubject(String strSubject)
    {
        m_strSubject = strSubject;
    }
    String getSubject()
    {
        return m_strSubject;
    }
    void setTime(Date dtFrom, Date dtTo)
    {
        setTimeFrom(dtFrom);
        setEndTime(dtTo);

    }
    void setTimeFrom(Date dtFrom)
    {
        m_From.set( dtFrom);
    }

    /**
     *
     * @param hourmins
     *      format: hh:mm
     */
    void setTimeFrom(String hourmins)
    {
        String s = "1999-9-9 " + hourmins + ":00";
        Date dt = KDSUtil.convertStringToDate(s);
        setTimeFrom(dt);


    }

    void setTimeFrom(float fltFrom)
    {
        m_From.set( fltFrom);
    }


    void setDuration(int nminutes)
    {
        float f = (float)nminutes*60/(float)( 24*60*60);
        m_To.set(m_From.get() + f);

    }
    void setEndTime(Date dtTo)
    {
        m_To.set( dtTo);
    }
    FloatTime getTimeFrom()
    {
        return m_From;
    }

    void setEndTime(float fltTime)
    {
        m_To.set(fltTime);
    }
    FloatTime getEndTime()
    {
        return m_To;
    }
    void setData(Object pData)
    {
        m_pData = pData;
    }
    Object getData()
    {
        return m_pData;
    }
    void setWeekDay(int nDay)
    {
        m_nWeekDay = nDay;
    }
    int getWeekDay()
    {
        return m_nWeekDay;
    }



    long getDurationSeconds()
    {
        return m_To.toSeconds() - m_From.toSeconds();
    }

    float getDurationFloatTime()
    {
        return m_To.get() - m_From.get();
    }

    int getDurationFloatTimeAsMinutes()
    {
        float flt = getDurationFloatTime();

        FloatTime ft = new FloatTime(flt);
        return ft.toMinutes();
    }

    int getDurationHeight(int nFullDayHeight, float durationTime)//ver4.0
    {
        float from = m_From.get();
        int iStart = WeekEvtView.INTFRAC(nFullDayHeight, from);
        int nh =  WeekEvtView.INTFRAC(nFullDayHeight, durationTime);
        int n = iStart + nh;
        if ( n >nFullDayHeight  )
        {
            nh =nh -( n - nFullDayHeight);
        }
        return nh;
    }

    Rect calculatDrawRectTopBottom(int nMiniHeight, int nFullDayHeight )//ver4.0
    {
        int iFullHeight = nFullDayHeight;//
        float from = m_From.get();
        int iStart = WeekEvtView.INTFRAC(iFullHeight, from);

        int iEnd = iStart+ WeekEvtView.INTFRAC(iFullHeight,m_To.get() - m_From.get());
        //record it where draw it
        m_dispFrom = m_From;//(double)iStart/(double)iFullHeight;
        m_dispTo = m_To;// (double)iEnd/(double)iFullHeight;
        //ver4.0
        int nMiniH = nMiniHeight;//GetMinimumHeight(pDC);
        int nDurationH = getDurationHeight(iFullHeight, getDurationSeconds());
        float dblMiniTime = ((float)nMiniH)/((float)iFullHeight);

        Rect rc= new Rect(0,0,0,0);//=rcArea;
        rc.top = iStart;
        rc.bottom = iEnd;

        if (nMiniH > nDurationH)
        {//make mini h
            rc.bottom = rc.top + nMiniH;
            m_dispTo.set(m_dispFrom.get() + dblMiniTime);

        }
        if (rc.bottom >nFullDayHeight)
        {//go out the data area, move up
            int n = rc.bottom - nFullDayHeight;
            rc.bottom -= n;
            rc.top -= n;
            float d = (float)n/(float)iFullHeight;
            m_dispFrom.set(m_dispFrom.get() - d);
            m_dispTo.set(m_dispTo.get() - d);

        }
        ////////////////
        return rc;
    }

    public boolean isTimeToFire()
    {
        Calendar c = Calendar.getInstance();
        int nWeekDay = c.get(Calendar.DAY_OF_WEEK)-1;
        if (nWeekDay != m_nWeekDay) return false;
        FloatTime tmNow = new FloatTime();
        tmNow.set(new Date());
        if (tmNow.get() >= m_From.get() &&
                tmNow.get() <= m_To.get())
            return true;
        return false;
    }

    public boolean isTimeout()
    {
        Calendar c = Calendar.getInstance();
        int nWeekDay = c.get(Calendar.DAY_OF_WEEK)-1;
        if (nWeekDay != m_nWeekDay) return true;
        FloatTime tmNow = new FloatTime();
        tmNow.set(new Date());
        if (tmNow.get() < m_From.get() ||
                tmNow.get() > m_To.get())
            return true;
        return false;
    }

    public String toXmlString()
    {
        KDSDataOrder order = new KDSDataOrder();
        order.setOrderName(this.getOrderID());
        order.setOrderType("PREP");

        for (int i=0; i< m_arItems.size(); i++) {
            m_arItems.get(i).setItemName(KDSUtil.convertIntToString(i));//assign the item ID first
            order.getItems().addComponent(m_arItems.get(i));
        }
        return order.createXml();
    }

    public String sqlAddNew()
    {
        String sql = "insert into schedule("
                + "GUID,Description,OrderID,weekday,starttime,endtime) "
                + " values ("
                + "'" + getGUID() + "'"
                + ",'" + fixSqliteSingleQuotationIssue( this.getSubject()) + "'"
                + ",'"+ fixSqliteSingleQuotationIssue( this.getOrderID()) + "'"
                + "," + KDSUtil.convertIntToString(getWeekDay())
                + "," + getTimeFrom().toString()
                + "," + getEndTime().toString()
                + ")";
        return sql;
    }
    public String sqlUpdate()
    {
        String sql = "update category set "
                + "Description='" +fixSqliteSingleQuotationIssue(  getSubject()) + "',"
                + "orderid='"+  fixSqliteSingleQuotationIssue(getOrderID()) + "',"
                + "weekday="+  KDSUtil.convertIntToString(getWeekDay()) + ","
                + "starttime=" + getTimeFrom().toString() + ","
                + "endtime="+  getEndTime().toString() + ","
                + "DBTimeStamp='" + KDSUtil.convertDateToString(getTimeStamp()) + "'"
                + " where guid='" + getGUID() + "'";
        return sql;
    }

    static public String sqlDelete(String strGUID)
    {
        String sql = "delete from schedule where guid='" + strGUID + "'";
        return sql;
    }

    static public String sqlItemAddNew(KDSDataItem item)
    {
        String sql = "insert into scheduleitems("
                + "GUID,schguid,Description,category,qty,tostation) "
                + " values ("
                + "'" + item.getGUID() + "'"
                + ",'" + item.getOrderGUID() + "'"
                + ",'" + fixSqliteSingleQuotationIssue(item.getDescription()) + "'"
                + ",'" + fixSqliteSingleQuotationIssue(item.getCategory()) + "'"
                + "," + KDSUtil.convertFloatToString( item.getQty())
                + ",'"+ item.getToStations().getString() + "'"

                + ")";
        return sql;
    }

    static public String sqlDeleteItems(String strScheduleGUID)
    {
        String sql = "delete from scheduleitems where schguid='" + strScheduleGUID + "'";
        return sql;
    }


    /**
     * use the float to represent time.
     * No date value, just time value
     */
    static public class FloatTime
    {
        float m_time = 0;

        public FloatTime()
        {

        }
        public FloatTime(float nval)
        {
            set(nval);
        }

        public float get() {
            return m_time;
        }
        public void set(float flt)
        {
            m_time = flt;
        }
        public void set(Date dt)
        {
            Calendar c = Calendar.getInstance();
            c.setTime(dt);
            int h = c.get(Calendar.HOUR_OF_DAY);
            int m = c.get(Calendar.MINUTE);
            int s = c.get(Calendar.SECOND);
            set(h, m, s);



        }
        public int toSeconds()
        {
            float f = (float)(24*60*60) * m_time;
            return (int) Math.ceil(f);
        }

        public int toMinutes()
        {
            float f = (float)(24*60) * m_time;
            return (int) Math.ceil(f);
        }

        public void set(int hour, int minutes, int seconds)
        {
            int s = seconds + hour * 60*60 + minutes *60;

            float f = (float) s /(24*60*60);
            set(f);
        }
        public int getHour()
        {
            int n = toMinutes();
            return (int)((float)n/(float)60);

        }

        public int getMinute()
        {
            int n = toMinutes();
            return (n%60);

        }

        public String toHourMinsString()
        {
            int h = getHour();
            int m = getMinute();
            return String.format("%02d:%02d", h, m);
        }
        public String toString()
        {
            String s = String.format(Locale.ENGLISH, "%f",get());
            return s;
        }

    }

}
