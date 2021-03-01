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
 *
 * 1.       Runner mode.
 * If station’s expo station is a runner, station will run in this mode.
 * It is same as current “Runner” station. Smart station will show items category by category. If one category finished, next category will show.
 * The categories will use <CatDelay> tag value to sort.
 * 2.       Category delay mode.
 * If any item’s <CatDelay> value was set, prep will run in this mode.
 * a.       Same category items will wait <CatDelay> miniutes, then start to show them.
 * b.       If current items in same category were all bumped(finished) before "delay" timeout,  the next category will show.
 * 3.       Item Delay mode.
 * If any item’s <ItemDelay> was set, station will run in this mode. <CatDelay> must been empty.
 * Item will show according to this <ItemDelay> value. The less delay item will show first.
 * 4.       Item preparation time mode
 * If <ItemDelay>, <CatDelay> is 0. And its expo is not runner. And, any item’s <PrepTime> value >0, station runs in this mode.
 * Station will show item according the <PrepTime> value.  Items will sort by <PrepTime> value descend. The max <PrepTime> value item will been shown first.
 *
 * And, for <PrepTime> value.
 * a.       If order xml file contains it, use it.
 * b.       If order xml file do not set it,
 * (1)     Router database has set this item’s preparation item. Use it.
 * (2)     If router does not set item’s preparation time value, use the preparation time value of category.
 */
public class PrepSorts {

    static String TAG = "PrepSorts";

    static public boolean m_bSmartCategoryEnabled = false; //for kp1-25

    /**
     *
     */
    public enum SmartMode
    {
        Unknown,
        Category_Runner, //if there are runner for my expo station.
        Category_Delay, //use <catdeley> delay time.  any item’s <CatDelay> value was set, prep will run in this mode.
        Item_Delay, //If any item’s <ItemDelay> was set, station will run in this mode. <CatDelay> must been empty.
        Item_Preparation, //use preparation time. <ItemDelay>, <CatDelay> is 0. And its expo is not runner. And, any item’s <PrepTime> value >0, station runs in this mode.
    }

    public ArrayList<PrepItem> m_arItems = new ArrayList<>();

    public void add(PrepItem item)
    {
        m_arItems.add(item);
    }

    public int count()
    {
        return m_arItems.size();
    }

    public ArrayList<String> m_arSmartShowingCategory = new ArrayList<>(); //kpp1-456


    /**
     * return the max item
     * @return
     */
    public PrepItem sort()
    {
        PrepItem maxItem = findNextShowingItem(m_arItems);
        if (maxItem == null) return null;
        for (int i=0; i< m_arItems.size(); i++)
        {
            PrepItem item =m_arItems.get(i);
            item.MaxItemName = maxItem.ItemName;
        }
        return maxItem;
    }

//    /**
//     *sort preparation time items.
//     * 1. sort item in each category
//     */
//    public void sort1()
//    {
//
//        ArrayList<String> arCategories = getAllCategories(m_arItems);
//        for (int i=0; i< arCategories.size(); i++)
//        {
//            String category = arCategories.get(i);
//            sortCategory(category);
//        }
//
//    }
//    private void sortCategory(String category)
//    {
//        PrepItem maxItem = findMaxCategoryPreparationTime(m_arItems,category );
//        if (maxItem == null)
//            return;
//        for (int i=0; i< m_arItems.size(); i++)
//        {
//            PrepItem item =m_arItems.get(i);
//            if (item.Category.equals(category))
//            {
//                item.MaxItemName = maxItem.ItemName;
//                //item.WaitSecsToStart = maxItem.getTotalMinsToCook() - item.getTotalMinsToCook();
//            }
//        }
//
//    }

//    public PrepItem findMaxCategoryPreparationTime(ArrayList<PrepItem> items , String category)
//    {
//        PrepItem maxPrepTime = null;
//        for (int i=0; i< items.size(); i++)
//        {
//            PrepItem item = items.get(i);
//            if (item.finished) continue;
//            if (item.Category.equals(category))
//            {
//                if (maxPrepTime == null) {
//                    maxPrepTime = item;
//                    continue;
//                }
//                if (item.PrepTime> maxPrepTime.PrepTime)
//                    maxPrepTime = item;
//            }
//        }
//        return maxPrepTime;
//    }

    /**
     * who will been shown next.
     * @param items
     * @return
     */
    public PrepItem findNextShowingItem(ArrayList<PrepItem> items)
    {
        SmartMode mode = getSmartMode();

        PrepItem maxPrepTime = null;
        for (int i=0; i< items.size(); i++)
        {
            PrepItem item = items.get(i);
            if (item.finished) continue;

            if (maxPrepTime == null) {
                maxPrepTime = item;
                continue;
            }
//            //if (item.PrepTime> maxPrepTime.PrepTime)
//            //if ( (item.PrepTime-item.ItemDelay)> (maxPrepTime.PrepTime-maxPrepTime.ItemDelay)) //kpp1-417, delay time.
//            if ( (item.PrepTime-item.ItemDelay - item.CategoryDelay)>
//                    (maxPrepTime.PrepTime-maxPrepTime.ItemDelay - maxPrepTime.CategoryDelay)) //kpp1-417, delay time.
//                maxPrepTime = item;

            switch (mode)
            {

                case Unknown:
                    return null;

                case Category_Runner:
                case Category_Delay:
                {
                    if ( item.CategoryDelay < maxPrepTime.CategoryDelay) //less delay item will show first
                        maxPrepTime = item;
                }
                break;

                case Item_Delay: {
                    if (item.ItemDelay < maxPrepTime.ItemDelay) //less delay item will show first
                        maxPrepTime = item;
                }
                break;
                case Item_Preparation:
                {
                    if (item.PrepTime > maxPrepTime.PrepTime) //more preparation item will show first
                        maxPrepTime = item;
                }
                break;
            }

        }
        return maxPrepTime;
    }

//    public ArrayList<String> getAllCategories(ArrayList<PrepItem> items)
//    {
//        ArrayList<String> ar = new ArrayList<>();
//
//        for (int i=0; i< items.size(); i++)
//        {
//            if (isExistedInArrary(ar, items.get(i).Category) )
//                continue;
//            else
//                ar.add(items.get(i).Category);
//        }
//
//        return ar;
//
//    }


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

//    private boolean isExistedInArrary(ArrayList<String> ar, String str)
//    {
//        for (int i=0; i< ar.size(); i++)
//        {
//            if (ar.get(i).equals(str))
//                return true;
//        }
//        return false;
//    }

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
     *  0: item should been shown now.
     */
    private int item_start_cooking_time_seconds2( String itemName, Date dtOrderStart, float orderDelay)
    {

        //kpp1-441
        if (m_bSmartCategoryEnabled)
            return category_runner_item_start_cooking_time_seconds(itemName, dtOrderStart, orderDelay);
        //
        PrepItem prep = findItem(itemName);
        if (prep == null) return 0;
        if (prep.finished) return 0; //kpp1-431-1, finished item should cooked.

        String maxItemName = prep.MaxItemName;

        int secs = 0;

        PrepItem maxItem = findItem(maxItemName);
        if (maxItem != null) {
            //the real start time has problem. We need to handle case that the max item don't set real start time.
            if (maxItem.RealStartTime <= 0)
                maxItem.RealStartTime = convertMinutes2Secs(orderDelay + maxItem.ItemDelay + maxItem.CategoryDelay);

            secs = maxItem.RealStartTime;
            //kpp1-417
            if (prep != maxItem) // it is not checking max item.
            {
                //kpp1-431-2, same category should started in same time. This ask us just use "delay", remove preparation time.
                //kp-17, just category delay feature use it.
                if (prep.CategoryDelay>0 ||
                    maxItem.CategoryDelay >0) {
                    if (prep.Category.equals(maxItem.Category)) {
                        //if (prep.ItemDelay == maxItem.ItemDelay)
                        if (prep.CategoryDelay == maxItem.CategoryDelay) {
                            return secs;
                        }
                    }
                }
                //
                //Add the difference of preparation time for max and normal item.
                // Add the normal item delay time.
                int nDelay = (int)(convertMinutes2Secs(orderDelay + prep.ItemDelay + prep.CategoryDelay) - secs);
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
            secs = convertMinutes2Secs(orderDelay + prep.ItemDelay+prep.CategoryDelay); //kpp1-417
        }
        // secs +=  convertMinutes2Secs(itemDelay);
        //if (maxItem != null) //kpp1-417
        //    secs += convertMinutes2Secs(maxItem.PrepTime -  prep.PrepTime);
        return secs;


    }

    public int item_start_cooking_time_seconds( String itemName, Date dtOrderStart, float orderDelay)
    {
        SmartMode mode = getSmartMode();


        if (mode == SmartMode.Category_Runner)
            return category_runner_item_start_cooking_time_seconds(itemName, dtOrderStart, orderDelay);

        //
        PrepItem prep = findItem(itemName);
        if (prep == null) return 0;
        if (prep.finished) return 0; //kpp1-431-1, finished item should cooked.

        String maxItemName = prep.MaxItemName;

        int secs = 0;

        PrepItem maxItem = findItem(maxItemName);
        if (maxItem != null) {
            //the real start time has problem. We need to handle case that the max item don't set real start time.
            if (maxItem.RealStartTime <= 0)
                maxItem.RealStartTime = convertMinutes2Secs(orderDelay + maxItem.ItemDelay + maxItem.CategoryDelay);

            secs = maxItem.RealStartTime;
            //kpp1-417
            if (prep != maxItem) // it is not checking max item.
            {
                //kpp1-431-2, same category should started in same time. This ask us just use "delay", remove preparation time.
                //kp-17, just category delay feature use it.
                switch (mode)
                {

                    case Unknown:
                        break;
                    case Category_Delay:
                    {
                       if (prep.Category.equals(maxItem.Category)) {
                           return secs;
                       }
                       int nDelay = (int)(convertMinutes2Secs(orderDelay + prep.CategoryDelay) - secs);
                       if (nDelay<0) nDelay = 0;
                       secs += nDelay;
                    }
                    break;
                    case Item_Delay:
                    {
                        int nDelay = (int)(convertMinutes2Secs(orderDelay + prep.ItemDelay) - secs);
                        if (nDelay<0) nDelay = 0;
                        secs += nDelay;
                    }
                        break;
                    case Item_Preparation:
                    {
                        float prepWait = maxItem.PrepTime - prep.PrepTime;
                        if (prepWait <0) prepWait = 0;
                        secs += convertMinutes2Secs(prepWait);
                    }
                        break;
                }

//                //
//                //Add the difference of preparation time for max and normal item.
//                // Add the normal item delay time.
//                int nDelay = (int)(convertMinutes2Secs(orderDelay + prep.ItemDelay + prep.CategoryDelay) - secs);
//                if (nDelay<0) nDelay = 0;
//                float prepWait = maxItem.PrepTime - prep.PrepTime;
//                if (prepWait <0) prepWait = 0;
//                secs += convertMinutes2Secs(prepWait) + nDelay;
//                //secs += convertMinutes2Secs(maxItem.PrepTime - prep.PrepTime) + nDelay;
//                //secs += convertMinutes2Secs(maxItem.PrepTime - prep.PrepTime + prep.ItemDelay - maxItem.ItemDelay);
            }
        }
        else
        {
            //secs = convertMinutes2Secs(orderDelay);
            secs = convertMinutes2Secs(orderDelay + prep.ItemDelay+prep.CategoryDelay); //kpp1-417
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
    static public PrepSorts.PrepItem prep_other_station_item_bumped2( KDSDataOrder order, String itemName)
    {

        if (m_bSmartCategoryEnabled)
            return category_runner_prep_other_station_item_bumped(order, itemName);

        //KDSDataOrder order = m_ordersDynamic.getOrderByName(orderName);
        if (order == null) return null;
        PrepSorts.PrepItem prepItem = order.prep_get_sorts().findItem(itemName);
        if (prepItem == null) return null;
        prepItem.setFinished(true);//, order.getDurationSeconds());
        PrepSorts.PrepItem maxItem = null;
        if (prepItem.PrepTime >0
                || prepItem.ItemDelay >0
                ||order.prep_get_sorts().areThereDelayItemUnfinished()
                || prepItem.CategoryDelay>0 )
        { //kpp1-322, add this condition
            //kpp1-431-2,Smart items:  if there is 0 delay item, and it is bumped, next category items should active.
            if (order.prep_get_sorts().isMaxCategoryTimeItem(itemName)) {
                boolean bAllCategoryFinished = isAllMyCategoryItemsFinished(order.prep_get_sorts(), prepItem);

                //the interal max item was not changed, so same old cooking state.
                PrepItem nextMaxItem = order.prep_get_sorts().findNextShowingItem(order.prep_get_sorts().m_arItems);
                boolean nextMaxItemShouldStarted = false;
                if (nextMaxItem != null)
                    nextMaxItemShouldStarted = order.prep_get_sorts().is_cooking_time(nextMaxItem.ItemName, order.getStartTime(), order.getOrderDelay());
                //change max item to new one.
                maxItem = order.prep_get_sorts().sort();
                if (maxItem != null) {
                    int startSeconds = order.getDurationSeconds();
                    int delaySeconds = (int)((maxItem.ItemDelay + maxItem.CategoryDelay)* 60);
                    if (!nextMaxItemShouldStarted) {
                        //if the max order has started, don't update its real start time.
                        if (bAllCategoryFinished)
                        {
                            maxItem.RealStartTime = startSeconds;
                            order.prep_get_sorts().setAllSameCategoryItemsStarted(maxItem);
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

    static public PrepSorts.PrepItem prep_other_station_item_bumped( KDSDataOrder order, String itemName)
    {
        if (order == null) return null;
        SmartMode mode = order.prep_get_sorts().getSmartMode();

        if (mode == SmartMode.Unknown)
            return null;

        if (mode == SmartMode.Category_Runner)
            return category_runner_prep_other_station_item_bumped(order, itemName);


        //KDSDataOrder order = m_ordersDynamic.getOrderByName(orderName);

        PrepSorts.PrepItem prepItem = order.prep_get_sorts().findItem(itemName);
        if (prepItem == null) return null;
        prepItem.setFinished(true);//, order.getDurationSeconds());
        PrepSorts.PrepItem maxItem = null;

        switch (mode)
        {

            case Category_Delay:
            {
                if (order.prep_get_sorts().areThereDelayItemUnfinished() )
                { //kpp1-322, add this condition
                    //kpp1-431-2,Smart items:  if there is 0 delay item, and it is bumped, next category items should active.
                    if (order.prep_get_sorts().isMaxCategoryTimeItem(itemName)) {
                        boolean bAllCategoryFinished = isAllMyCategoryItemsFinished(order.prep_get_sorts(), prepItem);

                        //the internal max item was not changed, so save old cooking state.
                        PrepItem nextMaxItem = order.prep_get_sorts().findNextShowingItem(order.prep_get_sorts().m_arItems);
                        boolean nextMaxItemShouldStarted = false;
                        if (nextMaxItem != null)
                            nextMaxItemShouldStarted = order.prep_get_sorts().is_cooking_time(nextMaxItem.ItemName, order.getStartTime(), order.getOrderDelay());
                        //change max item to new one.
                        maxItem = order.prep_get_sorts().sort();
                        if (maxItem != null) {
                            int startSeconds = order.getDurationSeconds();
                            int delaySeconds = (int)(( maxItem.CategoryDelay)* 60);
                            if (!nextMaxItemShouldStarted) {//we cook this item before its delay timeout.
                                //if the max order has started, don't update its real start time.
                                if (bAllCategoryFinished)
                                {
                                    maxItem.RealStartTime = startSeconds;
                                    order.prep_get_sorts().setAllSameCategoryItemsStarted(maxItem);
                                }
                                else {
                                    maxItem.RealStartTime = (startSeconds > delaySeconds ? startSeconds : delaySeconds); //Math.abs(order.getDurationSeconds() - (int)(maxItem.ItemDelay * 60)); //kpp1-417, make delay time must been done.
                                }
                            }

                        }
                    }
                }
            }
                break;
            case Item_Delay:
            {
                if (order.prep_get_sorts().areThereDelayItemUnfinished() )
                { //kpp1-322, add this condition
                    //kpp1-431-2,Smart items:  if there is 0 delay item, and it is bumped, next category items should active.
                    if (order.prep_get_sorts().isMaxCategoryTimeItem(itemName)) {
                        
                        //the internal max item was not changed, so save old cooking state.
                        PrepItem nextMaxItem = order.prep_get_sorts().findNextShowingItem(order.prep_get_sorts().m_arItems);
                        boolean nextMaxItemShouldStarted = false;
                        if (nextMaxItem != null)
                            nextMaxItemShouldStarted = order.prep_get_sorts().is_cooking_time(nextMaxItem.ItemName, order.getStartTime(), order.getOrderDelay());
                        //change max item to new one.
                        maxItem = order.prep_get_sorts().sort();
                        if (maxItem != null) {
                            int startSeconds = order.getDurationSeconds();
                            int delaySeconds = (int)(( maxItem.ItemDelay)* 60);
                            if (!nextMaxItemShouldStarted) {//we cook this item before its delay timeout.
                                maxItem.RealStartTime = (startSeconds < delaySeconds ? startSeconds : delaySeconds); //Math.abs(order.getDurationSeconds() - (int)(maxItem.ItemDelay * 60)); //kpp1-417, make delay time must been done.
                                
                            }

                        }
                    }
                }
            }
            break;
            case Item_Preparation:
            {
                if (order.prep_get_sorts().areThereDelayItemUnfinished() )
                { //kpp1-322, add this condition
                    //kpp1-431-2,Smart items:  if there is 0 delay item, and it is bumped, next category items should active.
                    if (order.prep_get_sorts().isMaxCategoryTimeItem(itemName)) {
                        maxItem = order.prep_get_sorts().sort();
                        if (maxItem != null) {
                            int startSeconds = order.getDurationSeconds();
                            maxItem.RealStartTime = startSeconds;
                        }

                        
                    }
                }
            }
                break;
        }

//        if (prepItem.PrepTime >0
//                || prepItem.ItemDelay >0
//                ||order.prep_get_sorts().areThereDelayItemUnfinished()
//                || prepItem.CategoryDelay>0 )
//        { //kpp1-322, add this condition
//            //kpp1-431-2,Smart items:  if there is 0 delay item, and it is bumped, next category items should active.
//            if (order.prep_get_sorts().isMaxCategoryTimeItem(itemName)) {
//                boolean bAllCategoryFinished = isAllMyCategoryItemsFinished(order.prep_get_sorts(), prepItem);
//
//                //the interal max item was not changed, so same old cooking state.
//                PrepItem nextMaxItem = order.prep_get_sorts().findNextShowingItem(order.prep_get_sorts().m_arItems);
//                boolean nextMaxItemShouldStarted = false;
//                if (nextMaxItem != null)
//                    nextMaxItemShouldStarted = order.prep_get_sorts().is_cooking_time(nextMaxItem.ItemName, order.getStartTime(), order.getOrderDelay());
//                //change max item to new one.
//                maxItem = order.prep_get_sorts().sort();
//                if (maxItem != null) {
//                    int startSeconds = order.getDurationSeconds();
//                    int delaySeconds = (int)((maxItem.ItemDelay + maxItem.CategoryDelay)* 60);
//                    if (!nextMaxItemShouldStarted) {
//                        //if the max order has started, don't update its real start time.
//                        if (bAllCategoryFinished)
//                        {
//                            maxItem.RealStartTime = startSeconds;
//                            order.prep_get_sorts().setAllSameCategoryItemsStarted(maxItem);
//                        }
//                        else {
//                            maxItem.RealStartTime = (startSeconds > delaySeconds ? startSeconds : delaySeconds); //Math.abs(order.getDurationSeconds() - (int)(maxItem.ItemDelay * 60)); //kpp1-417, make delay time must been done.
//                        }
//                        //maxItem.RealStartTime = startSeconds;// > delaySeconds ? startSeconds : delaySeconds);
//                    }
//                    //    getCurrentDB().prep_set_real_started_time(order.getGUID(), maxItem.ItemName, maxItem.RealStartTime);
//                }
//            }
//        }
        //getCurrentDB().prep_set_item_finished(order.getGUID(), itemName, true);
        return maxItem;


    }

    static public ArrayList<PrepSorts.PrepItem> prep_other_station_item_unbumped(KDSDataOrder order,String itemName)
    {
        if (m_bSmartCategoryEnabled)
            return category_runner_prep_other_station_item_unbumped(order, itemName);

        //KDSDataOrder order = m_ordersDynamic.getOrderByName(orderName);
        if (order == null) return null;
        PrepSorts.PrepItem prepItem = order.prep_get_sorts().findItem(itemName);
        if (prepItem == null) return null;
        prepItem.setFinished(false);//.finished = false;
        //if (order.prep_get_sorts().isMaxCategoryTimeItem(itemName))
        PrepSorts.PrepItem maxItem = order.prep_get_sorts().sort();
        if (maxItem != null && maxItem == prepItem && (!order.prep_get_sorts().areAllDifferentCategoryLessDelayTimeItemsFinished(maxItem)) )
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

    /**
     * kpp1-431-2
     * check all items, if its delay>0, and unfinished, return true;
     * @return
     */
    public boolean areThereDelayItemUnfinished()
    {
        for (int i=0; i< m_arItems.size(); i++)
        {
            PrepItem item = m_arItems.get(i);
            if ( (item.ItemDelay ==0) && (item.PrepTime ==0) && (item.CategoryDelay==0)) continue;
            if (item.finished) continue;
            return true;
        }
        return false;
    }

    /**
     * check if all items which delay time less than maxItem is finished.
     * This is for unbump operation.
     * Don't hide/gray current item when unbump, if prev items all bumped.
     * @param maxItem
     * @return
     */
    public boolean areAllDifferentCategoryLessDelayTimeItemsFinished(PrepItem maxItem)
    {

        for (int i=0; i< m_arItems.size(); i++)
        {
            PrepItem item = m_arItems.get(i);
            if (item.finished) continue;
            if (item == maxItem ) continue;
            if (item.Category.equals(maxItem.Category)) continue;
            //if ( item.ItemDelay<= maxItem.ItemDelay)
            if ( item.CategoryDelay<= maxItem.CategoryDelay) //kp-17
                return false;

        }
        return true;
    }

    /**
     * All same delay/prepitem/category items started
     * @param maxItem
     */
    private void setAllSameCategoryItemsStarted(PrepItem maxItem)
    {
        for (int i=0; i< m_arItems.size(); i++)
        {
            PrepItem item = m_arItems.get(i);
            if (item.finished) continue;
            if (item == maxItem ) continue;
            if (item.Category.equals(maxItem.Category) &&
                    item.CategoryDelay == maxItem.CategoryDelay)
                //item.ItemDelay == maxItem.ItemDelay)
                item.RealStartTime = maxItem.RealStartTime;



        }

    }

    /**
     * kpp1-456
     * @param itemName
     * @param dtOrderStart
     * @param orderDelay
     * @return
     */
    public int category_runner_item_start_cooking_time_seconds( String itemName, Date dtOrderStart, float orderDelay)
    {
        PrepItem prep = findItem(itemName);
        if (prep == null) return 0;
        if (prep.finished) return 0; //kpp1-431-1, finished item should cooked.

        String category = prep.Category;
        if (KDSUtil.isExistedInArray(this.m_arSmartShowingCategory, category))
            return 0;
        else
            return Integer.MAX_VALUE-999999999;



    }

    /**
     * kpp1-456
     * @param order
     * @param itemName
     * @return
     */
    static public PrepSorts.PrepItem category_runner_prep_other_station_item_bumped( KDSDataOrder order, String itemName)
    {
        //KDSDataOrder order = m_ordersDynamic.getOrderByName(orderName);
        if (order == null) return null;
        PrepSorts.PrepItem prepItem = order.prep_get_sorts().findItem(itemName);
        if (prepItem == null) return null;
        prepItem.setFinished(true);//, order.getDurationSeconds());
        
        boolean bAllCategoryFinished = isAllMyCategoryItemsFinished(order.prep_get_sorts(), prepItem);
        if (bAllCategoryFinished)
            return order.prep_get_sorts().sort();
        else
            return order.prep_get_sorts().findItem(prepItem.MaxItemName);




    }

    /**
     *kpp1-456
     * @param order
     * @param itemName
     * @return
     */
    static public ArrayList<PrepSorts.PrepItem> category_runner_prep_other_station_item_unbumped(KDSDataOrder order,String itemName)
    {
        //KDSDataOrder order = m_ordersDynamic.getOrderByName(orderName);
//        if (order == null) return null;
//        PrepSorts.PrepItem prepItem = order.prep_get_sorts().findItem(itemName);
//        if (prepItem == null) return null;
//        prepItem.setFinished(false);//.finished = false;
//        //if (order.prep_get_sorts().isMaxCategoryTimeItem(itemName))
//        PrepSorts.PrepItem maxItem = order.prep_get_sorts().sort();
//        if (maxItem != null && maxItem == prepItem && (!order.prep_get_sorts().areAllDifferentCategoryLessDelayTimeItemsFinished(maxItem)) )
//        {//we just restore old max item
//            ArrayList<PrepSorts.PrepItem> ar = order.prep_get_sorts().reset_real_start_time(maxItem);
//            return ar;
//
//        }
        return null;

    }

    public void runnerSetShowingCategory(ArrayList<String> ar)
    {
        m_arSmartShowingCategory.clear();
        m_arSmartShowingCategory.addAll(ar);

    }
    public ArrayList<String> runnerGetShowingCategory()
    {
        return m_arSmartShowingCategory;
    }
    
    public boolean runnerCategoryIsShowing(String category)
    {
        return KDSUtil.isExistedInArray(runnerGetShowingCategory(), category);
    }

    public String runnerGetLastShowingCategory()
    {
        int ncount = m_arSmartShowingCategory.size();
        if (ncount<=0)
            return "";
        return m_arSmartShowingCategory.get(ncount-1);
    }

    public SmartMode getSmartMode()
    {
        if (m_bSmartCategoryEnabled)
            return SmartMode.Category_Runner;
        else
        {
            int ncount = m_arItems.size();
            for (int i=0; i< ncount; i++)
            {
                if (m_arItems.get(i).CategoryDelay >0 )
                    return SmartMode.Category_Delay;
            }

            for (int i=0; i< ncount; i++)
            {
                if (m_arItems.get(i).ItemDelay >0)
                    return SmartMode.Item_Delay;
            }

            for (int i=0; i< ncount; i++)
            {
                if (m_arItems.get(i).PrepTime >0)
                    return SmartMode.Item_Preparation;
            }
        }

        return SmartMode.Unknown;

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
        public float CategoryDelay = 0;
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
            s +=",CategoryDelay=" + KDSUtil.convertFloatToString(CategoryDelay);
            return s;

        }
        public String sqlNew()
        {

            String sql = "insert into prepsort( orderguid,ItemName,Category,PrepTime,MaxItemName,finished,RealStartTime,ItemDelay, r0) values(" ;
            sql += "'" + orderguid +"'";
            sql += ",'" + KDSUtil.fixSqliteSingleQuotationIssue( ItemName) +"'";
            sql += ",'" + Category + "'";
            sql += "," + KDSUtil.convertFloatToString( PrepTime);
            sql += ",'" + MaxItemName + "'";
            //sql += "," + KDSUtil.convertFloatToString(WaitSecsToStart);
            sql += "," + KDSUtil.convertIntToString( finished?1:0 );
            sql += "," + KDSUtil.convertIntToString(RealStartTime);
            sql += "," + KDSUtil.convertFloatToString( ItemDelay);
            sql += "," + KDSUtil.convertFloatToString( CategoryDelay);
            sql += ")";
            return sql;
        }

        public String sqlDelOrder()
        {
            String sql = "delete from prepsort where orderguid='" + orderguid + "'";
            return sql;
        }

//        public float getTotalMinsToCook()
//        {
//            return ItemDelay + PrepTime;
//        }

    }

}
