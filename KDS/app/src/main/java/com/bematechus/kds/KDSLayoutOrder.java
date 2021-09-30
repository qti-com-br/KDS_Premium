package com.bematechus.kds;

import android.graphics.Color;

import com.bematechus.kdslib.KDSDataCategoryIndicator;
import com.bematechus.kdslib.KDSDataCondiment;
import com.bematechus.kdslib.KDSDataFromPrimaryIndicator;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataItems;
import com.bematechus.kdslib.KDSDataMessage;
import com.bematechus.kdslib.KDSDataMessages;
import com.bematechus.kdslib.KDSDataMoreIndicator;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.PrepSorts;
import com.bematechus.kdslib.ScheduleProcessOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Administrator on 2015/8/20 0020.
 * The formated order, it will been shown
 */
public class KDSLayoutOrder extends KDSDataOrder {

    public boolean mAllSmartItemsHidden = false; //kp-87, record if all items is in hidden state.
    public KDSDataOrder m_originalOrder = null;
    public void setOriginalOrder(KDSDataOrder order)
    {
        m_originalOrder = order;
    }
    public KDSDataOrder getOriginalOrder()
    {
        return m_originalOrder;
    }
    /**
     * don't care in horizontal/vertical layout , just count the rows number.
     * @return
     */
    public int get_need_how_many_rows_without_title_footer(boolean bEnableAddonString, boolean bAddVoidRow)
    {
        int ncount = this.getItems().getCount();
        int ncounter = 0;
        int nLastGroupID = -1;
        //ncounter ++; //order title.
        for (int i=0; i< ncount; i++)
        {
            ncounter ++ ;//item
            KDSDataItem item = this.getItems().getItem(i);
            if (bEnableAddonString) { //add add-on string
                int nGrp = item.getAddOnGroup();
                if (nGrp != nLastGroupID) {
                    ncounter++;
                    nLastGroupID = nGrp;
                }
            }
            if (item.getHidden())
                ncounter --;
            else
            {
                if (item.isQtyChanged())
                {
                    if (bAddVoidRow)
                        ncounter ++;
                }
            }
            //item messages
            ncounter += item.getPreModifiers().getCount();
            //count the condiments;
            int ncondiments = item.getCondiments().getCount();
            for (int j = 0; j< ncondiments; j++)
            {
                if (!item.getCondiments().getCondiment(j).getHiden())
                    ncounter++;//condiment
                //condiment message
                item.getCondiments().getCondiment(j).getMessages().getCount();
            }

            //count the condiments;
            int nmodifiers = item.getModifiers().getCount();
            for (int j = 0; j< nmodifiers; j++)
            {
                if (!item.getModifiers().getModifier(j).getHiden())
                    ncounter++;//condiment
                //condiment message
                item.getModifiers().getModifier(j).getMessages().getCount();
            }
        }
        //order messages
        ncounter += this.getOrderMessages().getCount();
        return ncounter;
    }


    /**
     * rev.:
     *  KP-97, if group by category enabled while consolidate items we will just consolidate same category items
     * @return
     */
    public boolean consolidateItems(boolean bGroupCategory)
    {
        KDSDataItems items = this.getItems();
        return items.consolidateItems(bGroupCategory);

    }

    protected void smartItemGray(KDSDataItem item, int grayBG, int grayFG )
    {

        item.setBG(grayBG);
        item.setFG(grayFG);
        //item messages

        smartMessagesGray(item.getPreModifiers(), grayBG, grayFG);

        //the condiments;
        int ncondiments = item.getCondiments().getCount();
        for (int i = 0; i< ncondiments; i++)
        {
            smartCondimentGray(item.getCondiments().getCondiment(i), grayBG, grayFG);
        }

    }

    protected void smartCondimentGray(KDSDataCondiment condiment, int grayBG, int grayFG) {
        condiment.setBG(grayBG);
        condiment.setFG(grayFG);
        smartMessagesGray(condiment.getMessages(), grayBG, grayFG);

    }

    protected void smartMessagesGray(KDSDataMessages msgs, int grayBG, int grayFG )
    {
        int nmsgs = msgs.getCount();
        for (int i = 0; i< nmsgs; i++)
        {
            //condiment message
            KDSDataMessage msg = msgs.getMessage(i);
            msg.setBG(grayBG);
            msg.setFG(grayFG);
        }
    }

    public KDSDataItem smartOrderFindMaxPreparationTimeItem()
    {
        int ncount = this.getItems().getCount();

        KDSDataItem maxItem = null;
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if (item.getLocalBumped()) continue;
            if (maxItem == null)
                maxItem = item;
            else
            {
                if (maxItem.getTotalPrepTime() < item.getTotalPrepTime())
                    maxItem = item;
            }

        }
        return maxItem;
    }
//    /**
//     * smart order showing method is the "gray" color showing.
//     * @return
//     */
//    public boolean smartOrderGrayColorShowing(int grayBG, int grayFG)
//    {
//        int ncount = this.getItems().getCount();
//        KDSDataItem maxPrepTimeItem = smartOrderFindMaxPreparationTimeItem();
//
//        boolean bItemCooked = false;
//        for (int i=0; i< ncount; i++)
//        {
//            KDSDataItem item = this.getItems().getItem(i);
//            if (!smartItemIsTimeToCook(item, maxPrepTimeItem))
//            {
//                smartItemGray(item, grayBG,grayFG);
//            }
//            else
//            {
//                bItemCooked = true;
//            }
//        }
//        //order messages
//        if (!this.smartOrderIsTimeToStartCook() && !bItemCooked)
//        {
//            smartMessagesGray(this.getOrderMessages(), grayBG, grayFG);
//        }
//        return true;
//
//    }
//    public boolean smartOrderHideShowing()
//    {
//        int ncount = this.getItems().getCount();
//
//        ArrayList<KDSDataItem> arWillHide = new ArrayList<>();
//        KDSDataItem maxPrepTimeItem = smartOrderFindMaxPreparationTimeItem();
//        for (int i=0; i< ncount; i++)
//        {
//            KDSDataItem item = this.getItems().getItem(i);
//            if (!smartItemIsTimeToCook(item, maxPrepTimeItem))
//            {
//                arWillHide.add(item);
//            }
//        }
//        ncount =  arWillHide.size();
//        for (int i=0; i< ncount; i++)
//        {
//            this.getItems().removeComponent(arWillHide.get(i));
//        }
//        arWillHide.clear();
//
//        if (ncount >0)
//        {
//            this.getItems().addComponent(new KDSDataMoreIndicator());
//        }
//
//        return true;
//    }


    public boolean addFromPrimaryIndicators()
    {
        this.getItems().insertComponent(0, new KDSDataFromPrimaryIndicator());
        this.getItems().addComponent(new KDSDataFromPrimaryIndicator());
        return true;
    }

    public KDSLayoutOrder buildItemShowingMethod(KDSSettings.ItemShowingMethod showingMethod)
    {
        if (showingMethod == KDSSettings.ItemShowingMethod.One_item_behind)
        {//20160706
            if (!this.isPaid())
            {
                int ncount = this.getItems().getCount();
                if (ncount >0)
                    this.getItems().deleteComponent(ncount-1); //hide last one.
            }
            return this;
        }
        else if (showingMethod == KDSSettings.ItemShowingMethod.When_order_is_paid)
        {//20160706
            if (!this.isPaid())
                return null;
        }

        return this;
    }

    public KDSLayoutOrder buildScheduleOrder(ScheduleProcessOrder scheduleOrder)
    {
        if (!scheduleOrder.is_schedule_process_order())
            return this;
        if (this.getItems().getItemsCountExceptAttached()<2)
        {
            ScheduleProcessOrder.init_schedule_items_array(this);
        }
        ScheduleProcessOrder.update_items_array(this, scheduleOrder);
        KDSGlobalVariables.getKDS().getCurrentDB().schedule_order_update_not_ready_qty(scheduleOrder);

        return this;

    }

    public void buildDimColorForQueue()
    {
        //2.0.11, enable prep queue
        if (!KDSGlobalVariables.getKDS().isExpeditorStation() && (!KDSGlobalVariables.getKDS().isQueueExpo())
                && (!KDSGlobalVariables.getKDS().isPrepStation()) &&
                (!KDSGlobalVariables.getKDS().isRunnerStation()) )
            return ;
        if (!KDSGlobalVariables.getKDS().getSettings().getBoolean(KDSSettings.ID.Queue_double_bump_expo_order))
            return ;

        this.setDimColor(this.getQueueReady());
        return ;
    }

    public void buildHiddenItemCondiments()
    {

    }

    public boolean smartOrderGrayColorShowing(int grayBG, int grayFG)
    {
        int ncount = this.getItems().getCount();

        boolean bItemCooked = false;
        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if (!smartItemIsTimeToCook(item))
            {
                smartItemGray(item, grayBG,grayFG); //use smart function
            }
            else
            {
                bItemCooked = true;
            }
        }
        //order messages
        if (!this.smartOrderIsTimeToStartCook() && !bItemCooked)
        {
            smartMessagesGray(this.getOrderMessages(), grayBG, grayFG);
        }
        return true;

    }

    public boolean smartOrderHideShowing()
    {
        int ncount = this.getItems().getCount();

        ArrayList<KDSDataItem> arWillHide = new ArrayList<>();

        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if (!smartItemIsTimeToCook(item))
            {
                arWillHide.add(item);
            }
        }
        ncount =  arWillHide.size();
        for (int i=0; i< ncount; i++)
        {
            this.getItems().removeComponent(arWillHide.get(i));
        }
        arWillHide.clear();

        if (this.getItems().getCount() == 1) //first one to showing
        {//set the time to start.
            int ndelay = this.prepItemGetStartTime(this.getItems().getItem(0));
            this.smartSetTimerDelay(ndelay);
            this.getOriginalOrder().smartSetTimerDelay(ndelay);
        }

        if (ncount >0)
        {
            this.getItems().addComponent(new KDSDataMoreIndicator());
        }


        return true;
    }

    public void buildRemoveQtyChangedItemsThatAddInLineItemsMode()
    {
        int ncount = this.getItems().getCount();

        ArrayList<KDSDataItem> arWillHide = new ArrayList<>();

        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if (item.isQtyChangeLineItem())
            {
                arWillHide.add(item);
            }
        }

        ncount =  arWillHide.size();
        for (int i=0; i< ncount; i++)
        {
            this.getItems().removeComponent(arWillHide.get(i));
        }
        arWillHide.clear();
    }


    /**
     * 2.0.47
     * We have a new request – group items by its category. I have create a ticket in JIRA, please let me know how long do you need to finish this and start working on this feature first.
     */
    static public void buildGroupCategory(KDSDataOrder order)
    {
        int ncount = order.getItems().getCount();

        ArrayList<KDSDataCategoryIndicator> arCategoryGroup = new ArrayList<>();

        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = order.getItems().getItem(i);
            KDSDataCategoryIndicator c =  findCategory(arCategoryGroup, item.getCategory());
            if (c == null )
            {
                c = new KDSDataCategoryIndicator();
                c.setCategoryDescription(item.getCategory());
                c.setPriority(item.getCategoryPriority());
                arCategoryGroup.add(c);
            }
//            else
//            {
//                c = findCategory(arCategoryGroup, item.getCategory());
//                if (c == null)
//                {
//                    c = new KDSDataCategoryIndicator();
//                    c.setCategoryDescription(item.getCategory());
//                    arCategoryGroup.add(c);
//                }
//
//            }
            c.getItems().add(item);

        }

        sortCategoryGroup(arCategoryGroup);
        //reset orders array
        order.getItems().clear();
        for (int i=0; i< arCategoryGroup.size(); i++)
        {
            KDSDataCategoryIndicator c = arCategoryGroup.get(i);
            order.getItems().addComponent(c);
            order.getItems().getComponents().addAll(c.getItems());

        }

    }

    static private void sortCategoryGroup(ArrayList<KDSDataCategoryIndicator> arCategoryGroup )
    {
        Collections.sort(arCategoryGroup, new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        KDSDataCategoryIndicator c1 = (KDSDataCategoryIndicator) o1;
                        KDSDataCategoryIndicator c2 = (KDSDataCategoryIndicator) o2;

                        int p1 = c1.getPriority();
                        int p2 = c2.getPriority();
                        int n = 0;
                        if (p1>p2)
                            n = 1;
                        else if (p1== p2)
                            n = 0;
                        else
                            n = -1;
                        return  n;
                    }
                }
        );
    }
    static private KDSDataCategoryIndicator findCategory(ArrayList<KDSDataCategoryIndicator> arCategoryGroup, String category)
    {
        for (int i=0; i< arCategoryGroup.size(); i++)
        {
            if (arCategoryGroup.get(i).getCategoryDescription().equals(category))
                return arCategoryGroup.get(i);
        }
        return null;
    }

    /**
     * kpp1-428
     * remove the hidden items in dressed order.
     *
     * @return
     *  The hidden items
     */
    public int removeHiddenItems()
    {
        int ncount = this.getItems().getCount();

        ArrayList<KDSDataItem> arWillHide = new ArrayList<>();

        for (int i=0; i< ncount; i++)
        {
            KDSDataItem item = this.getItems().getItem(i);
            if (item.getHidden())
            {
                arWillHide.add(item);
            }
        }

        ncount =  arWillHide.size();
        for (int i=0; i< ncount; i++)
        {
            this.getItems().removeComponent(arWillHide.get(i));
        }
        arWillHide.clear();
        return ncount;
    }

//    /**
//     * kp1-25
//     */
//    public void smartRunnerHideFinishedCategories()
//    {
//
//        ArrayList<String> arCategories = getAllCategories();
//        ArrayList<String> arHide = new ArrayList<>();
//        for (int i=0; i< arCategories.size(); i++)
//        {
//            if (smartCategoryItemsLocalFinished(arCategories.get(i)))
//            {
//                arHide.add(arCategories.get(i));
//            }
//        }
//
//        for (int i=0; i< arHide.size(); i++)
//        {
//            smartHideCategory(arHide.get(i));
//        }
//        arHide.clear();
//
//    }

    /**
     * KP-66 Runner hide finished category group -> Hide finished CatDelays
     */
    public void smartRunnerHideFinishedSameCatDelayItems()
    {
        ArrayList<KDSDataItem> arHide = new ArrayList<>();
        for (int i=0; i< m_items.getCount(); i++)
        {
            KDSDataItem item = m_items.getItem(i);
            if (item.getLocalBumped())
            {
                if (isAllSameCatDelayItemsFinished(item.getCategoryDelay()))
                    arHide.add(item);
            }
        }

        for (int i=0; i< arHide.size(); i++)
        {
            m_items.removeComponent(arHide.get(i));
        }
        arHide.clear();

    }

    private boolean isAllSameCatDelayItemsFinished(float catDelay)
    {
        ArrayList<PrepSorts.PrepItem> allSameCatDelayItems = smart_get_sorts().runnerGetAllSameCatDelayItems(catDelay);
        boolean bFinihsed = this.smart_get_sorts().allSameCatDelayItemsFinished(allSameCatDelayItems);
        if (bFinihsed)//Other station bumped it, but we need to check if local bumped too.
        {//check if local bumped. This is for Runner station.
            for (int i=0; i< allSameCatDelayItems.size(); i++)
            {
                String itemName = allSameCatDelayItems.get(i).ItemName;
                KDSDataItem item = this.m_items.getItemByName(itemName);
                if (item == null) continue;
                if (!item.getLocalBumped())
                    return false;
            }
        }
        return bFinihsed;
    }

    public void setAllSmartItemsWereHidden(boolean bAllHidden)
    {
        mAllSmartItemsHidden = bAllHidden;
    }

    public boolean getAllSmartItemsWereHidden()
    {
        return mAllSmartItemsHidden;
    }
//    private void smartHideCategory(String category)
//    {
//        ArrayList<KDSDataItem> arHide = new ArrayList<>();
//        for (int i=0; i< m_items.getCount(); i++)
//        {
//            if (m_items.getItem(i).getCategory().equals(category))
//                arHide.add(m_items.getItem(i));
//        }
//
//        for (int i=0; i< arHide.size(); i++)
//        {
//            m_items.removeComponent(arHide.get(i));
//        }
//        arHide.clear();
//    }
}
