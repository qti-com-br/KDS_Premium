package com.bematechus.kdslib;

import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXML;
import com.bematechus.kdslib.KDSXMLParserOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2016/7/8.
 */
public class KDSPosNotificationFactory {
    /************************************************************************/
/* when bump order/item, write a notification xml file
to given folder.
     1 – kitchen station item bump
     2 – kitchen station order bump
     3 – expeditor station item bump
     4 – expeditor station order bump
     5 – kitchen station item unbump
     6 – kitchen station order unbump
     7 – expeditor station item unbump
     8 – expeditor station order unbump
     9 - order finished, all items finished.
     10 -  order was transfer to other station.
     11 - order status changed event
     12 - item qty changed event
                                                     */

/************************************************************************/

    public enum BumpUnbumpType {
        Unknown,
        BUMP_ITEM,
        BUMP_ORDER,
        BUMP_EXPEDITOR_ITEM,
        BUMP_EXPEDITOR_ORDER,
        UNBUMP_ITEM,
        UNBUMP_ORDER,
        UNBUMP_EXPEDITOR_ITEM,
        UNBUMP_EXPEDITOR_ORDER, //8
        BUMP_AND_ORDER_FINISHED, //order fini
        BUMP_BY_ORDER_TRNANSFER,
        //
        Order_status_changed, //order status changed event
        Item_qty_changed, //item qty changed event
    };

    public enum OrderParamError
    {
        OK,
        OrderName,
        TransType,

    }

    static final private String NOTIFY_IPADDR = ("IPAddr");
    static final private String  NOTIFY_ROOT = "Transaction";//"Notification";// ("Transaction");
    static final private String  NOTIFY_ORDER = ("Order");
    static final private String  NOTIFY_TYPE = ("NotifyType");
    static final private String NOTIFY_FILE_NAME = "FileName";
    static final private String  NOTIFY_ID = ("ID");
    static final private String  NOTIFY_STATION = ("KitchenStation");
    static final private String  NOTIFY_STATUS = ("KitchenStatus");
    static final private String  NOTIFY_ITEM = ("ItemText");
    static final private String  NOTIFY_BUMP_TYPE = ("BumpType");
    static final private String  NOTIFY_ORDER_STATUS = ("0");
    static final private String  NOTIFY_BUMP_OFF = ("1");
    static final private String  BUMPOFF_TIME = "Bumpoff_Time";
    static final private String  RESTORED_TIME = "Restored_Time";

    //2.0.15 (router)
    static final public String ERROR = "Error";
    static final public String STATION = "Station";
    static final public String ORDER_ID = "OrderID";
    static final public String NO_ID = "No_ID";

    /**
     * for order ack feature
     */
    static final public String PREF_err = "err_";
    static final public String PREF_ack = "ack_";
    static final public String ACK_ERR_OK = "0"; //for ack
    static final public String ACK_ERR_BAD = "1"; //the order xml file structure bad.
    static final public String ACK_ERR_TIMEOUT = "2"; //some stations don't return ack in 10 seconds
    static final public String ACK_ERR_Name = "3"; //order xml parameters error
    static final public String ACK_ERR_TransType = "4"; //order xml parameters error

    /**
     *
     */
    static final private String  ACKNOWLEDGEMENT_ROOT = "Acknowledgement";//"Notification";// ("Transaction");

    /************************************************************************/
/* when kds station choose the order status,
and pressed key,
	KDS will create a order status file to "Order status" folder,
And tell POS the required order status.
file format see next function
 <Transaction>
     <Order>
		<ID>**<ID>              //Order ID
		<KitchenStation>1</KitchenStation>   //For request order status
		<KitchenStatus>Delivering to Counter</KitchenStatus> //for request order status

	</Order>
</Transaction>
                                                                    */

    static public String createOrderStatusNotification(String stationID, String orderName, String orderStatus)
    {
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root(NOTIFY_ROOT);
        xml.newGroup(NOTIFY_ORDER, true);
        xml.newGroup(NOTIFY_ID, orderName, false);
        xml.newGroup(NOTIFY_STATION, stationID, false);
        xml.newGroup(NOTIFY_STATUS, orderStatus, false);
        return xml.get_xml_string();


    }


    static public String createOrderAcknowledgementNotification(String stationID, String orderXml, String orderID,String errorCode, String fileName)
    {
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root(KDSXMLParserOrder.DBXML_ELEMENT_ORDER_ACK);
        xml.newAttribute(ERROR,errorCode);
        xml.newAttribute(STATION,stationID);
        xml.newAttribute(ORDER_ID,orderID);
        xml.newAttribute(NOTIFY_FILE_NAME, fileName);
        xml.setGroupValue( orderXml);

        return xml.get_xml_string();


    }

    /************************************************************************/
/* Order/Item bump off, KDS will create a bump notification xml file
to "Order status" folder.
 <Notification>
     <Order>
		<NotifyType></NotifyType> //0-- Return order status, 1-- Bump order/item, POS can use this to
								//distinguish notification file type.
		<ID>**<ID>              //Order ID
		<KitchenStation>1</KitchenStation>   //For request order status
		<KitchenStatus>Delivering to Counter</KitchenStatus> //for request order status

		<BumpType>Integer or string</BumpType>  //Bump type, item/order bump , from kitchen station or from expeditor
		<ItemText>Ice Cream</ItemText> //bump item text
	</Order>
</Notification>                                                                    */
    /************************************************************************/
    static public String createBumpNotification(String ipaddress, KDSDataOrder pOrder, KDSDataItem pItem, BumpUnbumpType nBumpType, String fileName)
    {

        if (pOrder == null)	return "";

        KDSXML xml = new KDSXML();//, *pxml=NULL;

        xml.new_doc_with_root(NOTIFY_ROOT);
        xml.newAttribute(NOTIFY_TYPE, KDSUtil.convertIntToString(nBumpType.ordinal()));
        xml.newAttribute(NOTIFY_FILE_NAME, fileName);
        String orderID="";
        String itemID="";
        orderID = pOrder.getOrderName();
        switch (nBumpType)
        {
            case BUMP_ITEM:
            case BUMP_EXPEDITOR_ITEM:
            case UNBUMP_ITEM:
            case UNBUMP_EXPEDITOR_ITEM:
            {
                if (pItem == null)
                    return "";
                pOrder.outputOrderInformationToXML(xml, true);
                //use the consolidate qty to notification.
                pItem.outputXml(xml);//, true);

                itemID = pItem.getItemName();//->GetID();
                xml.back_to_root();//->xmj_backRoot();
                xml.getFirstGroup(NOTIFY_ROOT);
                xml.getFirstGroup(NOTIFY_ORDER);
            }
            break;
            case BUMP_ORDER:
            case BUMP_EXPEDITOR_ORDER:
            case UNBUMP_ORDER:
            case UNBUMP_EXPEDITOR_ORDER:
            {
                pOrder.outputDataToXml(xml);

            }
            break;
            case BUMP_AND_ORDER_FINISHED: //20140429, for order completed notification
            case BUMP_BY_ORDER_TRNANSFER:
            {
                pOrder.outputOrderInformationToXML(xml, true);
            }
            break;
            case Order_status_changed:
            {
                pOrder.outputOrderInformationToXML(xml, false);
            }
            break;
            case Item_qty_changed:
            {
                if (pItem == null)
                    return "";
                pOrder.outputOrderInformationToXML(xml, true);
                //use the consolidate qty to notification.
                pItem.outputXml(xml);//, true);

                itemID = pItem.getItemName();//->GetID();
                xml.back_to_root();//->xmj_backRoot();
                xml.getFirstGroup(NOTIFY_ROOT);
                xml.getFirstGroup(NOTIFY_ORDER);
            }
            break;
            default:
                return "";
        }

        if (!addBumpNotificationTypeToXML(xml, nBumpType)) return "";

        if (!addIpAddressToXML(xml, ipaddress)) return "";
        switch(nBumpType)
        {
            case UNBUMP_ITEM:
            case UNBUMP_EXPEDITOR_ITEM:
            case UNBUMP_ORDER:
            case UNBUMP_EXPEDITOR_ORDER:
                adjust_xml(xml, false);
                break;
            case BUMP_ITEM:
            case BUMP_EXPEDITOR_ITEM:
            case BUMP_ORDER:
            case BUMP_EXPEDITOR_ORDER:
            case BUMP_AND_ORDER_FINISHED: //20140429, for order completed notification
            case BUMP_BY_ORDER_TRNANSFER:
                adjust_xml(xml, true);
                break;
            case Order_status_changed:
            case Item_qty_changed:
                break;
            default:
                break;
        }
        return xml.get_xml_string();


    }

    static boolean addBumpNotificationTypeToXML(KDSXML pxml, BumpUnbumpType nBumpType)
    {
        if (pxml == null) return false;
        pxml.back_to_root();//->xmj_backRoot();
        pxml.getFirstGroup(NOTIFY_ORDER);
        String s;
        s = KDSUtil.convertIntToString(nBumpType.ordinal());

        //s = String.format("%d", nBumpType.ordinal() );
        return pxml.newGroup(NOTIFY_BUMP_TYPE,s, false);

    }

    /************************************************************************/
/*
0014633: add ip address info on bump notification
*/
    /************************************************************************/
    static boolean addIpAddressToXML(KDSXML pxml, String ipaddress)
    {

        if (pxml == null) return false;
        pxml.back_to_root();//->xmj_backRoot();
        pxml.getFirstGroup(NOTIFY_ORDER);


        return pxml.newGroup(NOTIFY_IPADDR,ipaddress, false);
    }

    /************************************************************************/
/* @bBumpoff:
true: 		 for bumpoff order/item
false: unbump order/item

*/
    /************************************************************************/
    static void adjust_xml(KDSXML pxml, boolean bBump)
    {
        //if (bBump) return;
        pxml.back_to_root();//->xmj_backRoot();
        pxml.getFirstGroup(NOTIFY_ROOT);//->xmj_getFrstGroup(_T("Transaction"));
        pxml.getFirstGroup(NOTIFY_ORDER);//->xmj_getFrstGroup(_T("Order"));
        if (!bBump) //restore it
            pxml.delSubGroup(BUMPOFF_TIME);//->xmj_delGroup(_T("Bumpoff_Time"));
        else //bump it
            pxml.delSubGroup(RESTORED_TIME);//->xmj_delGroup(_T("Restored_Time"));
    }

    public static String createNewBumpNotifyFileName(String ipaddress, String orderID, String itemID, BumpUnbumpType nBumpType)
    {
        //create a new file name
        Date dt = new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

        String s =sdf.format(dt);// dt.Format(_T("%Y_%m_%d_%H_%M_%S"));
        String filename;

        if (itemID.isEmpty())
        {
            filename = String.format("%s_%s_%d_%s.xml", s, orderID, nBumpType.ordinal(), ipaddress);
        }
        else
        {
            filename = String.format("%s_%s_%s_%d_%s.xml", s, orderID,itemID, nBumpType.ordinal(), ipaddress);

        }
        filename = KDSUtil.correctFileName(filename);

        return filename;
    }

    static public String smbFullPathToFileName(String smbFileName)
    {
        String s = smbFileName;

        int n = s.lastIndexOf("/");
        if (n<0) return smbFileName;
        String f = s.substring(n+1);
        return f;
    }

    static public String createOrderAckFileName( KDSDataOrder order, int nAcceptItemsCount,String smbFileName, String orderName,boolean bGoodOrder)
    {
        String fileName = "";
        if (bGoodOrder) {
            fileName += PREF_ack;// "ack_";
            if (order.getTransType() == KDSDataOrder.TRANSTYPE_ADD ||
                    order.getTransType() == KDSDataOrder.TRANSTYPE_MODIFY)
            {
                if (nAcceptItemsCount <= 0)
                {
                    fileName = PREF_err ;//"err_";
                }
            }

        }
        else
            fileName += PREF_err ;//"err_";
        if (!smbFileName.isEmpty()) {
            String f = smbFullPathToFileName(smbFileName);
            fileName += f;
        }
        else {
            //from tcp/ip
            fileName += "Order" + orderName + ".xml";
        }

        fileName = fileName.toLowerCase();
        fileName = fileName.replace(".xml", "");
        fileName += "_" +KDSUtil.createNewGUID();
        fileName += ".xml";
        return fileName;
    }
    static public String createOrderAcknowledgement(String stationID, KDSDataOrder order, int nAcceptItemsCount,String smbFileName, String xmlData,String orderName,String errorCode, boolean bGoodOrder, String fileName)
    {

        String orderID = orderName;
        if (!bGoodOrder)
            orderID = NO_ID;// "No_ID";
        String s = createOrderAcknowledgementNotification(stationID, xmlData,orderID, errorCode, fileName);

        return s;

    }

    /**
     * Please check values in KDSDataOrder class
     * public static final int TRANSTYPE_ADD = 1;
     public static final int TRANSTYPE_DELETE = 2;
     public static final int TRANSTYPE_MODIFY = 3;
     public static final int TRANSTYPE_TRANSFER = 4;
     public static final int TRANSTYPE_ASK_STATUS = 5;
     public static final int TRANSTYPE_UPDATE_ORDER = 6; //20160712, use new data to modify existed order.
     * @param nTransType
     * @return
     */
    static public boolean isValidTransType(int nTransType)
    {
        if (nTransType >=1 &&
                nTransType <=6 )
            return true;
        return false;
    }
    /**
     * After parse order xml file, check if its parameters is correct.
     * @param order
     * @return
     */
    static public OrderParamError checkOrderParameters(KDSDataOrder order)
    {
        if (order.getOrderName().isEmpty())
            return OrderParamError.OrderName;
        int nTransType = order.getTransType();
        if (!isValidTransType(nTransType))
            return OrderParamError.TransType;
        for (int i=0; i< order.getItems().getCount(); i++)
        {
            KDSDataItem item = order.getItems().getItem(i);
            if (item.getItemName().isEmpty())
                return OrderParamError.OrderName;
            nTransType = item.getTransType();
            if (!isValidTransType(nTransType))
                return  OrderParamError.TransType;
        }
        return OrderParamError.OK;

    }

    static public String getOrderParamErrorCode(OrderParamError err)
    {
        switch (err)
        {

            case OK:
                return ACK_ERR_OK;

            case OrderName:
                return ACK_ERR_Name;
            case TransType:
                return ACK_ERR_TransType;
        }
        return ACK_ERR_OK;
    }

    static public ArrayList<KDSToStation> getOrderTargetStations(KDSDataOrder order)
    {

        ArrayList<KDSToStation> ar = new ArrayList<>();


        for (int i=0; i< order.getItems().getCount(); i++)
        {
            KDSDataItem item = order.getItems().getItem(i);
            KDSToStations toStations = item.getToStations();
            for (int j=0; j< toStations.getCount(); j++)
            {
                KDSToStation toStation = toStations.getToStation(j);
                if (isExistedInArrary(ar, toStation))
                    continue;
                ar.add(toStation);
            }
        }
        return ar;
    }
    static public boolean isExistedInArrary(ArrayList<KDSToStation> ar, KDSToStation toStation)
    {
        for (int i=0; i< ar.size(); i++)
        {
            if ( ar.get(i).getPrimaryStation().equals(toStation.getPrimaryStation()) &&
                    ar.get(i).getSlaveStation().equals(toStation.getSlaveStation()) )
                return true;
        }
        return false;
    }
}
