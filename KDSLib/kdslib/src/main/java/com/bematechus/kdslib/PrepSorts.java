package com.bematechus.kdslib;

import android.graphics.Canvas;
import android.util.Log;

import com.bematechus.kdslib.KDSUtil;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2017/12/29.
 */
public class PrepSorts {

    static String TAG = "PrepSorts";
    public ArrayList<PrepItem> m_arItems = new ArrayList<>();

    public void add(PrepItem item)
    {
        m_arItems.add(item);
    }

    public int count()
    {
        return m_arItems.size();
    }


    /**
     * return the max item
     * @return
     */
    public PrepItem sort()
    {
        PrepItem maxItem = findMaxPreparationTime(m_arItems);
        if (maxItem == null) return null;
        for (int i=0; i< m_arItems.size(); i++)
        {
            PrepItem item =m_arItems.get(i);
            item.MaxItemName = maxItem.ItemName;
        }
        return maxItem;
    }

    /**
     *sort preparation time items.
     * 1. sort item in each category
     */
    public void sort1()
    {

        ArrayList<String> arCategories = getAllCategories(m_arItems);
        for (int i=0; i< arCategories.size(); i++)
        {
            String category = arCategories.get(i);
            sortCategory(category);
        }

    }
    private void sortCategory(String category)
    {
        PrepItem maxItem = findMaxCategoryPreparationTime(m_arItems,category );
        if (maxItem == null)
            return;
        for (int i=0; i< m_arItems.size(); i++)
        {
            PrepItem item =m_arItems.get(i);
            if (item.Category.equals(category))
            {
                item.MaxItemName = maxItem.ItemName;
                //item.WaitSecsToStart = maxItem.getTotalMinsToCook() - item.getTotalMinsToCook();
            }
        }

    }

    public PrepItem findMaxCategoryPreparationTime(ArrayList<PrepItem> items , String category)
    {
        PrepItem maxPrepTime = null;
        for (int i=0; i< items.size(); i++)
        {
            PrepItem item = items.get(i);
            if (item.finished) continue;
            if (item.Category.equals(category))
            {
                if (maxPrepTime == null) {
                    maxPrepTime = item;
                    continue;
                }
                if (item.PrepTime> maxPrepTime.PrepTime)
                    maxPrepTime = item;
            }
        }
        return maxPrepTime;
    }

    public PrepItem findMaxPreparationTime(ArrayList<PrepItem> items)
    {
        PrepItem maxPrepTime = null;
        for (int i=0; i< items.size(); i++)
        {
            PrepItem item = items.get(i);
            if (item.finished) continue;

            if (maxPrepTime == null) {
                maxPrepTime = item;
                continue;
            }
            if (item.PrepTime> maxPrepTime.PrepTime)
                maxPrepTime = item;

        }
        return maxPrepTime;
    }

    public ArrayList<String> getAllCategories(ArrayList<PrepItem> items)
    {
        ArrayList<String> ar = new ArrayList<>();

        for (int i=0; i< items.size(); i++)
        {
            if (isExistedInArrary(ar, items.get(i).Category) )
                continue;
            else
                ar.add(items.get(i).Category);
        }

        return ar;

    }


    /**
     * set the real time to 0 after old max item unbumped.
     *
     * @param maxItem
     * @return
     */
    public ArrayList<PrepItem> reset_real_start_time(PrepItem maxItem)
    {
        ArrayList<PrepItem> ar = new ArrayList<>();

        for (int i=0; i< m_arItems.size(); i++)
        {
            PrepItem item = m_arItems.get(i);
            if (item == maxItem) continue;
            if (item.MaxItemName.equals(maxItem.ItemName))
            {
                item.RealStartTime = 0;
                ar.add(item);
            }


        }

        return ar;
    }

    private boolean isExistedInArrary(ArrayList<String> ar, String str)
    {
        for (int i=0; i< ar.size(); i++)
        {
            if (ar.get(i).equals(str))
                return true;
        }
        return false;
    }

    public PrepItem findItem(String itemName)
    {
        for (int i=0; i< m_arItems.size(); i++)
        {
            if (m_arItems.get(i).ItemName.equals(itemName))
                return m_arItems.get(i);
        }
        return null;
    }

    /**
     * check if it is time to cook this item.

     * @param orderDelay
     *  minutes

     * @return
     */
    public boolean is_cooking_time( String itemName, Date dtOrderStart, float orderDelay)
    {
        int secs = item_start_cooking_time_seconds(itemName, dtOrderStart, orderDelay);

//        PrepItem prep = findItem(itemName);
//        if (prep == null) return true;
//        String maxItemName = prep.MaxItemName;
//
//        int secs = 0;
//
//        PrepItem maxItem = findItem(maxItemName);
//        if (maxItem != null) {
//            //the real start time has problem. We need to handle case that the max item don't set real start time.
//            if (maxItem.RealStartTime <= 0)
//                maxItem.RealStartTime = convertMinutes2Secs(orderDelay) + convertMinutes2Secs(maxItem.ItemDelay);
//
//            secs = maxItem.RealStartTime;
//        }
//        else
//        {
//            secs = convertMinutes2Secs(orderDelay);
//        }
//       // secs +=  convertMinutes2Secs(itemDelay);
//        if (maxItem != null)
//            secs += convertMinutes2Secs(maxItem.PrepTime -  prep.PrepTime);
//        //in preparation mode, the itemdelay equals the category delay. The max item has handle the category delay
//        // so, don't add this item delay again.
//       // if (prep != maxItem)
//       //     secs += convertMinutes2Secs(prep.ItemDelay);
        TimeDog d = new TimeDog(dtOrderStart);
       // KDSLog.d(TAG,"MaxName=" + maxItemName +",ItemName=" + itemName + ",seconds=" + KDSUtil.convertIntToString(secs) );
       // KDSLog.d(TAG, prep.toString());
        return d.is_timeout(secs * 1000);

    }

    /**
     *
     * @param itemName
     * @param dtOrderStart
     * @param orderDelay
     * @return
     *  How many seconds later, this item can sart to cook (from order start time).
     */
    public int item_start_cooking_time_seconds( String itemName, Date dtOrderStart, float orderDelay)
    {
        PrepItem prep = findItem(itemName);
        if (prep == null) return 0;
        String maxItemName = prep.MaxItemName;

        int secs = 0;

        PrepItem maxItem = findItem(maxItemName);
        if (maxItem != null) {
            //the real start time has problem. We need to handle case that the max item don't set real start time.
            if (maxItem.RealStartTime <= 0)
                maxItem.RealStartTime = convertMinutes2Secs(orderDelay) + convertMinutes2Secs(maxItem.ItemDelay);

            secs = maxItem.RealStartTime;
        }
        else
        {
            secs = convertMinutes2Secs(orderDelay);
        }
        // secs +=  convertMinutes2Secs(itemDelay);
        if (maxItem != null)
            secs += convertMinutes2Secs(maxItem.PrepTime -  prep.PrepTime);
        return secs;


    }

    public Date item_start_cook_time( String itemName, Date dtOrderStart, float orderDelay)
    {
        int secs = item_start_cooking_time_seconds(itemName, dtOrderStart, orderDelay);
        Calendar c = Calendar.getInstance();
        c.setTime(dtOrderStart);
        c.add(Calendar.SECOND, secs);
        return c.getTime();

    }


    private int convertMinutes2Secs(float minutes)
    {
        float f = minutes * 60;
        return (int)f;
    }


    public boolean isMaxCategoryTimeItem(String itemName)
    {
        for (int i=0; i< m_arItems.size(); i++)
        {
            if (m_arItems.get(i).MaxItemName.equals(itemName))
                return true;
        }
        return false;
    }

    /********************************************************************************************/
    /********************************************************************************************/
    /********************************************************************************************/
    static public class PrepItem
    {
        public String orderguid = "";
        public String ItemName = "";
        public String Category = "";
        public float PrepTime = 0; //it includes the item preptime and modifiers preptime
        public float ItemDelay = 0;
        public String MaxItemName = "";
       // public float WaitSecsToStart = 0;
        public boolean finished = false;
        public int RealStartTime= 0; //seconds from order started
        public void setFinished(boolean bFinished)//, int nStartedSeconds)
        {
            finished = bFinished;
//            if (bFinished)
//            {
//                RealStartTime = nStartedSeconds;
//            }
        }
        public String toString()
        {
            String s ="ItemName=" + ItemName;
            s +=",MaxName=" + MaxItemName;

            s +=",ItemDelay=" + KDSUtil.convertFloatToString(ItemDelay);
            s +=",PrepTime=" + KDSUtil.convertFloatToString(PrepTime);

            return s;

        }
        public String sqlNew()
        {

            String sql = "insert into prepsort( orderguid,ItemName,Category,PrepTime,MaxItemName,finished,RealStartTime,ItemDelay) values(" ;
            sql += "'" + orderguid +"'";
            sql += ",'" + KDSUtil.fixSqliteSingleQuotationIssue( ItemName) +"'";
            sql += ",'" + Category + "'";
            sql += "," + KDSUtil.convertFloatToString( PrepTime);
            sql += ",'" + MaxItemName + "'";
            //sql += "," + KDSUtil.convertFloatToString(WaitSecsToStart);
            sql += "," + KDSUtil.convertIntToString( finished?1:0 );
            sql += "," + KDSUtil.convertIntToString(RealStartTime);
            sql += "," + KDSUtil.convertFloatToString( ItemDelay);
            sql += ")";
            return sql;
        }

        public String sqlDelOrder()
        {
            String sql = "delete from prepsort where orderguid='" + orderguid + "'";
            return sql;
        }

        public float getTotalMinsToCook()
        {
            return ItemDelay + PrepTime;
        }

    }

}
