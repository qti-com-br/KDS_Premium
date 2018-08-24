/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bematechus.kdslib;

import android.graphics.Color;

import java.util.ArrayList;

/**
 *
 * @author David.Wong
 */
public class KDSXMLParserOrder {


    //station infomation
    protected final static String DBXML_ELEMENT_STATIONINFO = ("StationInfo");

    public final static String DBXML_ELEMENT_ORDER_ACK = ("Acknowledgement");
    //order
    public final static String DBXML_ELEMENT_TRANSACTION = ("Transaction");
    public final static String DBXML_ELEMENT_ORDER	= ("Order");
    public final static String DBXML_ELEMENT_ID		= ("ID");
    public final static String DBXML_ELEMENT_GUID		= ("GUID");
    public final static String DBXML_ELEMENT_TERMINAL    = ("PosTerminal");
    public final static String DBXML_ELEMENT_TRANSTYPE   = ("TransType");
    public final static String DBXML_ELEMENT_ORDERSTATUS = ("OrderStatus");
    public final static String DBXML_ELEMENT_ORDERTYPE   = ("OrderType");
    public final static String DBXML_ELEMENT_OPERATOR    = ("ServerName");
    public final static String DBXML_ELEMENT_DESTINATION = ("Destination");
    public final static String DBXML_ELEMENT_TABLE       = ("GuestTable");
    public final static String DBXML_ELEMENT_USERINFO    = ("UserInfo");
    public final static String DBXML_ELEMENT_QUEUEMSG    = ("QueueMsg");
    public final static String DBXML_ELEMENT_TRACKERID    = "TrackerID";
    public final static String DBXML_ELEMENT_PAGERID    = "PagerID";

    public final static String DBXML_ELEMENT_ORDER_MSG	  = ("OrderMessages");
    public final static String DBXML_ELEMENT_PARKED	 = ("Parked"); //2.5.4.22
    public final static String DBXML_ELEMENT_TRANSFER	 = ("Transfer"); //3.0.0.6
    public final static String DBXML_ELEMENT_FROMSTATION	 = ("FromStation"); //3.0.0.6
    public final static String DBXML_ELEMENT_TOSTATION	 = ("ToStation"); //3.0.0.6
    public final static String DBXML_ELEMENT_SCHENDTIME	 = ("SchEndTime"); //3.1, when the schedule order ended

    public final static String DBXML_ELEMENT_ITEM        = ("Item");


    public final static String DBXML_ELEMENT_NAME        = ("Name");
    public final static String DBXML_ELEMENT_COLOR       = ("Color");
    public final static String DBXML_ELEMENT_RGBCOLOR       = ("RGBColor");
    public final static String DBXML_ELEMENT_CATEGORY    = ("Category");
    public final static String DBXML_ELEMENT_QTY         = ("Quantity");
    public final static String DBXML_ELEMENT_Schedule_Ready_QTY         = ("ScheduleReadyQty");
    public final static String DBXML_ELEMENT_ACTION      = ("Action");
    public final static String DBXML_ELEMENT_CONDIMENT   = ("Condiment");

    public final static String DBXML_ELEMENT_PREMOIDIFIER  = ("PreModifier");
    public final static String DBXML_ELEMENT_COLOR_BG	= ("BG");
    public final static String DBXML_ELEMENT_COLOR_FG	= ("FG");
    public final static String DBXML_ELEMENT_KDSSTATION	= ("KDSStation");

    public final static String DBXML_ELEMENT_TOSCREEN	= ("ToScreen"); //for transfer order

    public final static String DBXML_ELEMENT_COUNT	= ("Count");
    public final static String DBXML_ELEMENT_ICON	= ("IconIndex"); //2.5.4.22 , icon index
    public final static String DBXML_ELEMENT_DELAY = ("Delay");
    public final static String DBXML_ELEMENT_CATEGORY_DELAY	= ("CateDelay");


    public final static String DBXML_ELEMENT_MARKED = ("Marked");
    public final static String DBXML_ELEMENT_LOCAL_BUMPED = ("LocalBumped");

    //smartorder
    public final static String DBXML_ELEMENT_ORDER_DELAY = ("OrderDelay");
    public final static String DBXML_ELEMENT_ITEM_DELAY = ("ItemDelay");
    public final static String DBXML_ELEMENT_PREPARATION_TIME = ("PreparationTime");



    //order status

    public final static String DBXML_ELEMENT_KDS_STATION = "KitchenStation";
    public final static String DBXML_ELEMENT_FEEDBACK_ORDER_STATUS = "KitchenStatus";

    public final static String DBXML_IS_BACKUP_KDSSTATION = "isbackup";

    //
    public final static String DBXML_ELEMENT_BUILD_CARD = "BuildCard";
    public final static String DBXML_ELEMENT_TRAINING_VIDEO = "TrainingVideo";

    public final static String DBXML_ELEMENT_SUMMARY_TRANSLATE = "SumTrans";
    public final static String DBXML_ELEMENT_SUMMARY_NAME = "SumName";
    public final static String DBXML_ELEMENT_SUMMARY_ENABLED = "SumNameEnabled";

    // <HideStation>1,3,4</HideStation>
    public final static String DBXML_ELEMENT_HIDDEN_STATIONS = "HideStation";

    //2.0.47
    public final static String DBXML_ELEMENT_CATEGORY_PRIORITY = "SortPriority";
    /************************************************************************/
    /* 
    <CatDelay> 
    <category name="Appetizer" delay="2"></category>
    <category name="Subsalads" delay="4"></category>
    ..........
    </CatDelay>
    */
    /************************************************************************/
    public final static String DBXML_ELEMENT_CATEGORY_NEW_DELAY = ("CatDelay");

    /**
     * //20160106
     * For expeditor item
     * This item just for expeditor station
     */
    public final static String DBXML_ELEMENT_EXP_ITEM = ("ExpItem");

    public final static String DBXML_ELEMENT_ITEM_TYPE = ("ItemType");

    //for modifiers
    public final static String DBXML_ELEMENT_MODIFIER = ("Modifier");


    /**
     *
     * @param kdsStation
     *      My station number.
     * @param xml
     * @return
     */
    public static KDSDataOrder parseXmlOrder(String kdsStation, KDSXML xml)
    {
        if (!xml.back_to_root())
            return null;
        KDSDataOrder c = new KDSDataOrder();
        c.setPCKDSNumber(kdsStation);
        if (!xml.getFirstGroup(DBXML_ELEMENT_ORDER))
            return null;
        //go through the order xml file

        if (!xml.moveToFirstChild())
            return null;
        do
        {
            String name = xml.getCurrentName();
            doOrderSubGroup(xml, name, c);
        }
        while (xml.slidingNext());

        return c;
    }
    protected static void doOrderSubGroup(KDSXML xml, String grpName, KDSDataOrder order)
    {
        String strVal = xml.getCurrentGroupValue();
        switch (grpName)
        {
            case  DBXML_ELEMENT_ID:
            {
                order.setOrderName(strVal);
            }
            break;
            case  DBXML_ELEMENT_GUID:
            {
                order.setGUID(strVal);
            }
            break;
            case  DBXML_ELEMENT_TERMINAL:
            {
                order.setFromPOSNumber(strVal);
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.From_POS);
            }
            break;
            case  DBXML_ELEMENT_TRANSTYPE:
            {
                int n = KDSUtil.convertStringToInt(strVal, -1);

                order.setTransType(n);
            }
            break;
            case  DBXML_ELEMENT_ORDERSTATUS:
            {
                int n = KDSUtil.convertStringToInt(strVal, -1);
                order.setStatus(n);
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.Status);
            }
            break;
            case  DBXML_ELEMENT_ORDERTYPE:
            {
                order.setOrderType(strVal);
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.Order_Type);
            }
            break;
            case  DBXML_ELEMENT_OPERATOR:
            {
                order.setWaiterName(strVal);
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.Waiter_Name);
            }
            break;
            case  DBXML_ELEMENT_DESTINATION:
            {
                order.setDestination(strVal);
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.Destination);
            }
            break;
            case  DBXML_ELEMENT_TABLE:
            {
                order.setToTable(strVal);
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.To_Table);
            }
            break;
            case  DBXML_ELEMENT_USERINFO:
            {
                order.setCustomMsg(strVal);
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.Custom_Message);
            }
            break;
            case DBXML_ELEMENT_QUEUEMSG:
            {
                order.setQueueMessage(strVal);
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.Queue_Message);
            }
            break;
            case DBXML_ELEMENT_TRACKERID:
            {
                order.setTrackerID(strVal);
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.TrackerID);
            }
            break;
            case DBXML_ELEMENT_PAGERID:
            {
                order.setPagerID(strVal);
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.PagerID);
            }
            break;
            case DBXML_ELEMENT_PARKED:
            {
                int n = KDSUtil.convertStringToInt(strVal, 0);
                order.setParked( (n==1));
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.Parked);
            }
            break;
            case DBXML_ELEMENT_ICON:
            {
                int n = KDSUtil.convertStringToInt(strVal, 0);
                order.setIconIdx(n);
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.Icon_Index);
            }
            break;
            case DBXML_ELEMENT_ORDER_DELAY://smart order
            {
                float flt = KDSUtil.convertStringToFloat(strVal, 0);
                order.setOrderDelay(flt);
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.Order_Delay);

            }
            break;
            case DBXML_ELEMENT_ORDER_MSG:
            {
                doOrderMessages(xml, order);
                order.setXmlFieldValid(KDSDataOrder.VALID_ORDER_XML_FIELD.Messages);
            }
            break;
            case DBXML_ELEMENT_TOSCREEN: //just for transfer order
            {
                int n = KDSUtil.convertStringToInt(strVal, 0);
                order.setScreen(n);
            }
            break;

            case DBXML_ELEMENT_ITEM:
            {
                doItem(xml,  order);
            }
            break;
        }
    }
    /***
     * 	<OrderMessages>
     <!-- Append those message to the bottom of this order -->
     <Count>2</Count>
     <!--How many message -->
     <S0>Order Message 0</S0>
     <!--Sn- the message contents -->
     <S1>Order Message 1</S1>
     </OrderMessages>
     * @param xml
     * @param
     * @param order
     */
    protected static void doOrderMessages(KDSXML xml,  KDSDataOrder order)
    {
        ArrayList ar = getMessages(xml);
        int ncount = ar.size();
        for (int i=0; i< ncount; i++)
        {

            String strVal = (String)ar.get(i);
            if (!strVal.isEmpty())
            {
                KDSDataMessage msg = new KDSDataMessage();
                msg.setForComponentType(KDSDataMessage.FOR_Order);
                msg.setComponentGUID(order.getGUID());
                msg.setMessage(strVal);
                order.getOrderMessages().addComponent(msg);

            }
        }

    }

    /**
     * <Order>
     *     <Item>
     *         <ID><ID/>
     *         <Category></>
     *         ...
     *     <Item/>
     * </Order>
     * @param strModifyItemXml
     *  The modify item xml content.
     * @return
     *  Null: str error
     *  KDSDataItem: parsed item
     */
    public static KDSDataItem parseItem(String strModifyItemXml)
    {
        KDSXML xml = new KDSXML();
        xml.loadString(strModifyItemXml);
        xml.back_to_root();
        //if (!xml.getFirstGroup("Order"))
        //    return null;
        KDSDataOrder order = new KDSDataOrder();
        if (xml.getCurrentName().equals(DBXML_ELEMENT_ITEM) || xml.getFirstGroup(DBXML_ELEMENT_ITEM))
        {
            doItem(xml, order);
        }
        if (order.getItems().getCount()>0)
            return order.getItems().getItem(0);
        else
            return null;
    }

    /*************************************************************************
     *
     * @param xml
     * @param order
     */
    protected static void doItem(KDSXML xml,  KDSDataOrder order)
    {
        //check marked first, this is for transfer order information between stations
        int nmarked = 0;
        boolean bLocalBumped = false;
        String s = xml.getAttribute("Marked", "0");
        if (!s.equals("0"))
            nmarked = 1;

        s = xml.getAttribute("LocalBumped", "0");
        if (!s.equals("0"))
            bLocalBumped = true;


        //20160116
        s = xml.getAttribute(DBXML_ELEMENT_ITEM_TYPE, "0");
        int n = KDSUtil.convertStringToInt(s, 0);
        KDSDataItem.ITEM_TYPE itemType = KDSDataItem.ITEM_TYPE.values()[n];


        if (!xml.moveToFirstChild())
            return ;
        KDSDataItem item = new KDSDataItem(order.getGUID());
        item.setItemType(itemType);

        do
        {
            String name = xml.getCurrentName();
            doItemSubGroup(xml, name, order, item);
        }
        while (xml.slidingNext());
        item.setMarked(nmarked);
        item.setLocalBumped(bLocalBumped);
        order.getItems().addComponent(item);

        xml.back_to_parent();


    }
    /********************
     *
     * @param xml
     * @param grpName
     * @param order
     */
    protected static void doItemSubGroup(KDSXML xml,String grpName,
                                         KDSDataOrder order, KDSDataItem item)
    {
        String strVal = xml.getCurrentGroupValue();

        switch (grpName)
        {
            case  DBXML_ELEMENT_ID:
            {
                item.setItemName(strVal);
                item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.Name);
            }
            break;
            case  DBXML_ELEMENT_GUID:
            {
                item.setGUID(strVal);

            }
            break;
            case  DBXML_ELEMENT_NAME:
            {
                item.setDescription(strVal);
                item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.Description);

            }
            break;
            case  DBXML_ELEMENT_TRANSTYPE:
            {
                int n = KDSUtil.convertStringToInt(strVal, -1);
                item.setTransType(n);
            }
            break;
            case  DBXML_ELEMENT_CATEGORY:
            {
                item.setCategory(strVal);
                item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.Category);
                //2.0.47
                strVal = "-1";
                strVal = xml.getAttribute(DBXML_ELEMENT_CATEGORY_PRIORITY, strVal);
                if (!strVal.isEmpty())
                {
                    int n = (int)KDSUtil.convertStringToLong(strVal, 0);
                    item.setCategoryPriority( n );
                }
                else
                {
                    item.setCategoryPriority(-1);
                }
            }
            break;
            case  DBXML_ELEMENT_COLOR:
            {
                strVal = "";
                strVal = xml.getAttribute(DBXML_ELEMENT_COLOR_BG, strVal);
                if (!strVal.isEmpty())
                {
                    int nBG = (int)KDSUtil.convertStringToLong(strVal, 0);
                    int c = KDSUtil.convertWebColor2RGB(nBG);
                    item.setBG( c );// c.getRGB());
                    item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.BG);
                }
                else
                {
                    item.setBG(0);
                }
                strVal = "";
                strVal = xml.getAttribute(DBXML_ELEMENT_COLOR_FG, strVal);
                if (!strVal.isEmpty())
                {
                    int nFG = (int)KDSUtil.convertStringToLong(strVal, 0);
                    int c = KDSUtil.convertWebColor2RGB(nFG);
                    item.setFG( c);// c.getRGB());
                    item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.FG);
                }
                else
                {
                    item.setFG(0);
                }

            }
            break;
            case DBXML_ELEMENT_RGBCOLOR:
            {
                strVal = "";
                strVal = xml.getAttribute(DBXML_ELEMENT_COLOR_BG, strVal);
                if (!strVal.isEmpty())
                {
                    //int nBG = (int)KDSUtil.convertStringToLong(strVal, 0);
                    int nBG = KDSUtil.convertHtmlString2Color(strVal);
                    item.setBG(nBG);
                    item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.BG);
                }
                else
                {
                    item.setBG(0);
                }


                strVal = "";
                strVal = xml.getAttribute(DBXML_ELEMENT_COLOR_FG, strVal);
                if (!strVal.isEmpty())
                {
                    //int nFG = (int)KDSUtil.convertStringToLong(strVal, 0);
                    int nFG = KDSUtil.convertHtmlString2Color(strVal);
                    item.setFG(nFG);
                    item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.FG);
                }
                else
                {
                    item.setFG(0);
                }
            }
            break;
            case  DBXML_ELEMENT_QTY:
            {
                float n = KDSUtil.convertStringToFloat(strVal, 0);
                item.setQty(n);
                item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.Qty);
            }
            break;
            case DBXML_ELEMENT_Schedule_Ready_QTY:
            {
                float n = KDSUtil.convertStringToFloat(strVal, 0);
                item.setScheduleProcessReadyQty((int)n);
                item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.Schedule_Ready_Qty);
            }
            break;
            case  DBXML_ELEMENT_KDSSTATION:
            {
                item.setToStationsString(strVal);
                item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.ToStations);
                //the filter router change the kdsstation to item's backup station.
                String isbackup = xml.getAttribute(DBXML_IS_BACKUP_KDSSTATION, "0");

                item.setStationChangedToBackup(isbackup.equals("1"));



            }
            break;
            case DBXML_ELEMENT_ITEM_DELAY://smart order
            {
                float flt = KDSUtil.convertStringToFloat(strVal, 0);
                item.setItemDelay(flt);
                item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.ItemDelay);

            }
            break;
            case DBXML_ELEMENT_CATEGORY_DELAY://preparation time mode
            {
                float flt = KDSUtil.convertStringToFloat(strVal, 0);
                item.setCategoryDelay(flt);
                item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.CategoryDelay);

            }
            break;
            case DBXML_ELEMENT_PREPARATION_TIME://smart order
            {
                float flt = KDSUtil.convertStringToFloat(strVal, 0);
                item.setPreparationTime(flt);
                item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.PreparationTime);

            }
            break;
            case DBXML_ELEMENT_HIDDEN_STATIONS:
            {
                doItemHiddenStations(order,  item, strVal);
            }
            break;
            case  DBXML_ELEMENT_PREMOIDIFIER:
            {
                doItemPremodifier( xml, grpName,  order,  item);
                item.setXmlFieldValid(KDSDataItem.VALID_ITEM_XML_FIELD.Messages);

            }
            break;

            case DBXML_ELEMENT_BUILD_CARD:
            {
                item.setBuildCard(strVal);
            }
            break;
            case DBXML_ELEMENT_TRAINING_VIDEO:
            {
                item.setTrainingVideo(strVal);
            }
            break;
            case  DBXML_ELEMENT_CONDIMENT:
            {
                doCondiment( xml, grpName, order, item);


            }
            break;
            case DBXML_ELEMENT_MODIFIER:
            {
                doModifier( xml, grpName, order, item);
            }
            break;
            case DBXML_ELEMENT_SUMMARY_TRANSLATE:
            {
                String s= xml.getAttribute( DBXML_ELEMENT_SUMMARY_ENABLED, "0");
                item.setSumNamesEnabled(s.equals("1"));
                item.setSumNames(KDSDataSumNames.parseString(strVal));

            }
            break;




        }
    }

    protected static ArrayList getMessages(KDSXML xml)
    {
        ArrayList ar = new ArrayList();

        String strVal = xml.getSubGrouValue("Count", "");
        if (strVal.isEmpty())
            return ar;
        int ncount = KDSUtil.convertStringToInt(strVal, 0);
        if (ncount <=0)
            return ar;
        for (int i=0; i< ncount; i++)
        {
            String strID = "S" + KDSUtil.convertIntToString(i);
            strVal = xml.getSubGrouValue(strID, "");
            if (!strVal.isEmpty())
            {
                ar.add(strVal);
            }
        }
        return ar;
    }

    /*************************************************************************
     * <PreModifier>
     <!--Those message will be shown at top of item -->
     <Count>2</Count>
     <!-- Message count -->
     <S0>Pre-Modifier 0</S0>
     <!-- Sn message content-->
     <S1>Pre-Modifier 1</S1>
     </PreModifier>
     * @param xml
     * @param grpName
     * @param order
     * @param item
     */
    protected static void doItemPremodifier(KDSXML xml,String grpName,
                                            KDSDataOrder order, KDSDataItem item)
    {
        ArrayList ar = getMessages(xml);
        int ncount = ar.size();
        for (int i=0; i< ncount; i++)
        {

            String strVal = (String)ar.get(i);
            if (!strVal.isEmpty())
            {
                KDSDataMessage msg = new KDSDataMessage();
                msg.setForComponentType(KDSDataMessage.FOR_Item);
                msg.setComponentGUID(item.getGUID());
                msg.setMessage(strVal);
                item.getMessages().addComponent(msg);


            }
        }
    }

    protected static void doItemHiddenStations( KDSDataOrder order, KDSDataItem item, String strHiddenStations)
    {

        item.setHiddenStations(strHiddenStations);

    }

    protected static void doCondimentHiddenStations( KDSDataOrder order, KDSDataItem item, KDSDataCondiment condiment, String strHiddenStations)
    {

        condiment.setHiddenStations(strHiddenStations);

    }


    protected static void doCondiment(KDSXML xml,String grpName,
                                      KDSDataOrder order, KDSDataItem item)
    {
        if (!xml.moveToFirstChild())
            return ;
        KDSDataCondiment condiment = new KDSDataCondiment(item.getGUID());

        do
        {
            String name = xml.getCurrentName();
            doCondimentSubGroup(xml, name, order, item, condiment);
        }
        while (xml.slidingNext());
        item.getCondiments().addComponent(condiment);

        xml.back_to_parent();
    }

    /**
     * parse the modifier xml data
     *    <Modifier>
     <ID>1</ID>
     <TransType>1</TransType>
     <Name>Spicy2</Name>
     <Color BG="210" FG="55"></Color>
     <Action>-1</Action>
     <PrepTime></PrepTime>
     </Condiment>
     * @param xml
     * @param grpName
     * @param order
     * @param item
     */
    protected static void doModifier(KDSXML xml,String grpName,
                                      KDSDataOrder order, KDSDataItem item)
    {
        if (!xml.moveToFirstChild())
            return ;
        KDSDataModifier modifier = new KDSDataModifier(item.getGUID());

        do
        {
            String name = xml.getCurrentName();
            doModifierSubGroup(xml, name, order, item, modifier);
        }
        while (xml.slidingNext());
        item.getModifiers().addComponent(modifier);

        xml.back_to_parent();
    }


    /************************
     *
     *    <Condiment>
     <ID>1</ID>
     <TransType>1</TransType>
     <Name>Spicy2</Name>
     <Color BG="210" FG="55"></Color>
     <Action>-1</Action>
     </Condiment>
     * @param xml
     * @param grpName
     * @param order
     * @param item
     * @param condiment
     */
    protected static void doCondimentSubGroup(KDSXML xml,String grpName,
                                              KDSDataOrder order, KDSDataItem item,
                                              KDSDataCondiment condiment)
    {
        String strVal = xml.getCurrentGroupValue();

        switch (grpName)
        {
            case  DBXML_ELEMENT_ID:
            {
                condiment.setCondimentName(strVal);
                condiment.setXmlFieldValid(KDSDataCondiment.VALID_CONDIMENT_XML_FIELD.Name);
            }
            break;
            case  DBXML_ELEMENT_NAME:
            {
                condiment.setDescription(strVal);
                condiment.setXmlFieldValid(KDSDataCondiment.VALID_CONDIMENT_XML_FIELD.Description);


            }
            break;
            case  DBXML_ELEMENT_TRANSTYPE:
            {
                int n = KDSUtil.convertStringToInt(strVal, -1);
                condiment.setTransType(n);
            }
            break;

            case  DBXML_ELEMENT_COLOR:
            {
                strVal = "";
                strVal = xml.getAttribute(DBXML_ELEMENT_COLOR_BG, strVal);
                if (!strVal.isEmpty())
                {
                    int nBG = (int)KDSUtil.convertStringToLong(strVal, 0);
                    int c = KDSUtil.convertWebColor2RGB(nBG);
                    condiment.setBG(c);

                    condiment.setXmlFieldValid(KDSDataCondiment.VALID_CONDIMENT_XML_FIELD.BG);
                }
                else
                {
                    condiment.setBG(0);
                }

                strVal = "";
                strVal = xml.getAttribute(DBXML_ELEMENT_COLOR_FG, strVal);
                if (!strVal.isEmpty())
                {
                    int nFG = (int)KDSUtil.convertStringToLong(strVal, 0);
                    int c = KDSUtil.convertWebColor2RGB(nFG);
                    condiment.setFG(c);
                    //condiment.setFG(nFG);
                    condiment.setXmlFieldValid(KDSDataCondiment.VALID_CONDIMENT_XML_FIELD.FG);
                }
                else
                {
                    condiment.setFG(0);
                }

            }
            break;
            case DBXML_ELEMENT_RGBCOLOR:
            {
                strVal = "";
                strVal = xml.getAttribute(DBXML_ELEMENT_COLOR_BG, strVal);
                if (!strVal.isEmpty())
                {
                    //int nBG = (int)KDSUtil.convertStringToLong(strVal, 0);
                    int nBG = KDSUtil.convertHtmlString2Color(strVal);
                    condiment.setBG(nBG);
                    condiment.setXmlFieldValid(KDSDataCondiment.VALID_CONDIMENT_XML_FIELD.BG);
                }
                else
                {
                    condiment.setBG(0);
                }
                strVal = "";
                strVal = xml.getAttribute(DBXML_ELEMENT_COLOR_FG, strVal);
                if (!strVal.isEmpty())
                {
                    //int nFG = (int)KDSUtil.convertStringToLong(strVal, 0);
                    int nFG = KDSUtil.convertHtmlString2Color(strVal);
                    condiment.setFG(nFG);
                    condiment.setXmlFieldValid(KDSDataCondiment.VALID_CONDIMENT_XML_FIELD.FG);
                }
                else
                {
                    condiment.setFG(0);
                }
            }
            break;
            case  DBXML_ELEMENT_PREMOIDIFIER:
            {
                doCondimentPremodifier( xml, grpName,  order,  item, condiment);
                condiment.setXmlFieldValid(KDSDataCondiment.VALID_CONDIMENT_XML_FIELD.Messages);

            }
            break;
            case DBXML_ELEMENT_HIDDEN_STATIONS:
            {
                doCondimentHiddenStations(order,  item,condiment, strVal);
            }
            break;


        }
    }

    protected static void doCondimentPremodifier(KDSXML xml,String grpName,
                                                 KDSDataOrder order, KDSDataItem item, KDSDataCondiment condiment)
    {
        ArrayList ar = getMessages(xml);
        int ncount = ar.size();
        for (int i=0; i< ncount; i++)
        {

            String strVal = (String)ar.get(i);
            if (!strVal.isEmpty())
            {
                KDSDataMessage msg = new KDSDataMessage();
                msg.setForComponentType(KDSDataMessage.FOR_Condiment);
                msg.setComponentGUID(condiment.getGUID());
                msg.setMessage(strVal);
                condiment.getMessages().addComponent(msg);


            }
        }
    }


    /************************
     *
     *    <Modifier>
     <ID>1</ID>
     <TransType>1</TransType>
     <Name>Spicy2</Name>
     <Color BG="210" FG="55"></Color>
     <Action>-1</Action>
     <PreparationTime></PreparationTime>
     </Condiment>
     * @param xml
     * @param grpName
     * @param order
     * @param item
     * @param modifier
     */
    protected static void doModifierSubGroup(KDSXML xml,String grpName,
                                              KDSDataOrder order, KDSDataItem item,
                                              KDSDataModifier modifier)
    {
        String strVal = xml.getCurrentGroupValue();

        switch (grpName)
        {
            case  DBXML_ELEMENT_PREPARATION_TIME:
            {
                float fltMinutes = KDSUtil.convertStringToFloat(strVal, 0);
                int nseconds = KDSUtil.convertMinsToSecondss(fltMinutes);

                modifier.setPrepTime(nseconds);
                modifier.setXmlFieldValid(KDSDataCondiment.VALID_CONDIMENT_XML_FIELD.PrepTime);
            }
            break;
            default:
                doCondimentSubGroup(xml, grpName, order, item, modifier);

        }
    }


}
