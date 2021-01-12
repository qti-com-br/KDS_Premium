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
            //if (item.PrepTime> maxPrepTime.PrepTime)
            if ( (item.PrepTime-item.ItemDelay)> (maxPrepTime.PrepTime-maxPrepTime.ItemDelay)) //kpp1-417, delay time.
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
     *  Unit is minutes
     * @return
     *  How many seconds later, this item can sart to cook (from order start time).
     */
    public int item_start_cooking_time_seconds( String itemName, Date dtOrderStart, float orderDelay)
    {
        PrepItem prep = findItem(itemName);
        if (prep == null) return 0;
        if (prep.finished) return 0; //kpp1-431-1, finished item should cooked.

        String maxItemName = prep.MaxItemName;

        int secs = 0;

        PrepItem maxItem = findItem(maxItemName);
        if (maxItem != null) {
            //the real start time has problem. We need to handle case that the max item don't set real start time.
            if (maxItem.RealStartTime <= 0)
                maxItem.RealStartTime = convertMinutes2Secs(orderDelay + maxItem.ItemDelay);

            secs = maxItem.RealStartTime;
            //kpp1-417
            if (prep != maxItem) // it is not checking max item.
            {

                //Add the difference of preparation time for max and normal item.
                // Add the normal item delay time.
                int nDelay = (int)(convertMinutes2Secs(prep.ItemDelay) - secs);
                if (nDelay<0) nDelay = 0;
                float prepWait = maxItem.PrepTime - prep.PrepTime;
                if (prepWait <0) prepWait = 0;
                secs += convertMinutes2Secs(prepWait) + nDelay;
                //secs += convertMinutes2Secs(maxItem.PrepTime - prep.PrepTime) + nDelay;
                //secs += convertMinutes2Secs(maxItem.PrepTime - prep.PrepTime + prep.ItemDelay - maxItem.ItemDelay);
            }
        }
        else
        {
            //secs = convertMinutes2Secs(orderDelay);
            secs = convertMinutes2Secs(orderDelay + prep.ItemDelay); //kpp1-417
        }
        // secs +=  convertMinutes2Secs(itemDelay);
        //if (maxItem != null) //kpp1-417
        //    secs += convertMinutes2Secs(maxItem.PrepTime -  prep.PrepTime);
        return secs;


    }

    public Date item_start_cook_time( String itemName, Date dtOrderStart, float orderDelay)
    {
        int secs = item_start_cooking_time_seconds(itemName, dtOrderStart, orderDelay);
        if (secs <=0) //kpp1-322
            return new Date(dtOrderStart.getTime());
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

    /**
     * Rev.
     *  1. If all items preparation time is 0, this function will cause line items display move order up/up automaticly. (Smart sort enabled).
     *  2. kpp1-431, Category Delay- When bumping all items in a category the next category's items should show up
     * @param order
     * @param itemName
     * @return
     *  New max item
     */
    static public PrepSorts.PrepItem prep_other_station_item_bumped( KDSDataOrder order, String itemName)
    {
        //KDSDataOrder order = m_ordersDynamic.getOrderByName(orderName);
        if (order == null) return null;
        PrepSorts.PrepItem prepItem = order.prep_get_sorts().findItem(itemName);
        if (prepItem == null) return null;
        prepItem.setFinished(true);//, order.getDurationSeconds());
        PrepSorts.PrepItem maxItem = null;
        if (prepItem.PrepTime >0 || prepItem.ItemDelay >0) { //kpp1-322, add this condition
            if (order.prep_get_sorts().isMaxCategoryTimeItem(itemName)) {
                boolean bAllCategoryFinished = isAllMyCategoryItemsFinished(order.prep_get_sorts(), prepItem);

                //the interal max item was not changed, so same old cooking state.
                PrepItem nextMaxItem = order.prep_get_sorts().findMaxPreparationTime(order.prep_get_sorts().m_arItems);
                boolean nextMaxItemShouldStarted = order.prep_get_sorts().is_cooking_time(nextMaxItem.ItemName, order.getStartTime(), order.getOrderDelay());
                //change max item to new one.
                maxItem = order.prep_get_sorts().sort();
                if (maxItem != null) {
                    int startSeconds = order.getDurationSeconds();
                    int delaySeconds = (int)(maxItem.ItemDelay * 60);
                    if (!nextMaxItemShouldStarted) {
                        //if the max order has started, don't update its real start time.
                        if (bAllCategoryFinished)
                        {
                            maxItem.RealStartTime = startSeconds;
                        }
                        else {
                            maxItem.RealStartTime = (startSeconds > delaySeconds ? startSeconds : delaySeconds); //Math.abs(order.getDurationSeconds() - (int)(maxItem.ItemDelay * 60)); //kpp1-417, make delay time must been done.
                        }
                        //maxItem.RealStartTime = startSeconds;// > delaySeconds ? startSeconds : delaySeconds);
                    }
                //    getCurrentDB().prep_set_real_started_time(order.getGUID(), maxItem.ItemName, maxItem.RealStartTime);
                }
            }
        }
        //getCurrentDB().prep_set_item_finished(order.getGUID(), itemName, true);
        return maxItem;


    }

    static public ArrayList<PrepSorts.PrepItem> prep_other_station_item_unbumped(KDSDataOrder order,String itemName)
    {
        //KDSDataOrder order = m_ordersDynamic.getOrderByName(orderName);
        if (order == null) return null;
        PrepSorts.PrepItem prepItem = order.prep_get_sorts().findItem(itemName);
        if (prepItem == null) return null;
        prepItem.setFinished(false);//.finished = false;
        //if (order.prep_get_sorts().isMaxCategoryTimeItem(itemName))
        PrepSorts.PrepItem maxItem = order.prep_get_sorts().sort();
        if (maxItem != null && maxItem == prepItem)
        {//we just restore old max item
            ArrayList<PrepSorts.PrepItem> ar = order.prep_get_sorts().reset_real_start_time(maxItem);
            return ar;
//            for (int i=0; i< ar.size(); i++)
//            {
//                getCurrentDB().prep_set_real_started_time(order.getGUID(), ar.get(i).ItemName, 0);
//            }
        }
        return null;
        //getCurrentDB().prep_set_item_finished(order.getGUID(), itemName, false);
    }

    /**
     * kpp1-431 Category Delay- When bumping all items in a category the next category's items should show up
     * @param smartItem
     * @return
     */
    static private boolean isAllMyCategoryItemsFinished(PrepSorts smartItems,  PrepSorts.PrepItem smartItem)
    {
        String category = smartItem.Category;
        for (int i=0; i< smartItems.m_arItems.size(); i++)
        {
            PrepItem item = smartItems.m_arItems.get(i);
            if (item == smartItem) continue;
            if (item.Category.equals(category))
            {
                if (!item.finished)
                    return false;
            }

        }
        return true;
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
