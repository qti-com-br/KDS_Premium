package com.bematechus.kdsrouter;

import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.bematechus.kdslib.Activation;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataCondiment;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataMessage;
import com.bematechus.kdslib.KDSDataMessages;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSLog;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * connect backoffice, and waiting for notification.
 * 1) Subscribe to the room related to 'store_guid'
 * After connecting (or reconnecting) to WebSocket, every time, join the store 'channel' by sending a subscription request using:
 * ["store_guid","<store guid>"]
 * Example:
 * ["store_guid","77e37375-bb83-4bc8-b813-4c5c1a7c4dcc"]
 * The acknowledgement should inform the last 4 digits of the given GUID:
 * ["message","Subscribed to ****4dcc"]
 *
 * 2) Events from server
 * Events will be sent to the corresponding store channel the app should have previously subscribed (store_guid). Possible messages:
 * ["api_event","order_created"]
 * ["api_event","order_updated"]
 * ["api_event","order_voided"]
 * ["api_event","item_created"]
 * ["api_event","item_updated"]
 * ["api_event","item_voided"]
 *
 * These messages require the app to re-sync its DB.
 * For now, there won't be any details on the messages. In case you need them, please tell me so.
 *
 * 3) What happens when user disconnects
 * All user subscriptions are removed. User will not receive any more messages unless subscribed again. Need to re-subscribe (see item #1).
 *
 * 4) What happens when server disconnects
 * I am not quite sure but I recommend re-subscribing to the store_guid room again (see item #1).
 */
public class KDSBackofficeNotification extends Handler{

    static final String TAG = "BackOfficeNotification";

    public static boolean ENABLE_DEBUG = false; //show debug orders!!

    //static final int SHOW_MSG = 1;
    static final int CONNECT_WEBSOCKET = 2;

    static final String MSG_KEY_MESSAGE = "message";
    static final String MSG_KEY_SYNC = "sync";

    static final String API_EVENT_ORDER_CREATED = "order_created";
    static final String API_EVENT_ORDER_UPDATED = "order_updated";
    static final String API_EVENT_ORDER_VOIDED = "order_voided";
    static final String API_EVENT_ITEM_CREATED = "item_created";
    static final String API_EVENT_ITEM_UPDATED = "item_updated";
    static final String API_EVENT_ITEM_VOIDED = "item_voided";


    BackOfficeWebSocketClient m_webSocket = null;

    public interface BackofficeNotification_Event
    {
        void onBackofficeNotifyEvent(String evt, String data);
    }

    BackofficeNotification_Event m_receiver = null;

    public KDSBackofficeNotification(BackofficeNotification_Event receiver)
    {
        setReceiver(receiver);
    }

    public void setReceiver(BackofficeNotification_Event receiver)
    {
        m_receiver = receiver;
    }
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what)
        {
//            case SHOW_MSG:
//            {
//                String s =(String) msg.obj;
//                //showMessage(s);
//            }
//            break;
            case CONNECT_WEBSOCKET:
            {
                this.connectNotification();
            }
            break;
            default:
                break;
        }

    }

    public boolean close()
    {
        if (m_webSocket != null)
        {
            m_webSocket.setStopMe(true);
            m_webSocket.close();
        }
        m_webSocket = null;
        return  true;
    }

    public boolean connectBackOffice()
    {
        if (Activation.getStoreGuid().isEmpty()) {
            if (m_webSocket != null) //kpp1-397
                m_webSocket.close();
            return false;
        }

        if (m_webSocket != null) {
            if (!Activation.getStoreGuid().equals(m_webSocket.getConnectedStoreGuid()))
                m_webSocket.close();
        }

        if (isConnected()) return true;


        this.close();
        return connectNotification();

    }
    private boolean connectNotification()
    {

        try {
            URI url = new URI(BuildConfig.WEBSOCKET_URL);
            if (m_webSocket != null)
            {
                m_webSocket.setStopMe(true);
                m_webSocket.close();
            }
            m_webSocket = null;
            if (m_webSocket == null) {
                m_webSocket = new BackOfficeWebSocketClient(url);

            }
            else
            {
                if (m_webSocket.isOpen()) return true;
                //if (m_webSocket.isConnecting()) return true;//kpp1-409
                if (m_webSocket.isClosing()) return false;

                m_webSocket.close();
            }

            m_webSocket.connect();
            m_webSocket.setConnectedStoreGuid( Activation.getStoreGuid());
            return true;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //log2File(TAG +KDSLog._FUNCLINE_()+"Error: "+e.toString());
            e.printStackTrace();
        }
        return false;
    }

    public boolean isConnected()
    {
        if (m_webSocket == null) return false;
        if (m_webSocket.isOpen()) return true;
        //if (m_webSocket.isConnecting()) return true; //kp1-409, new websocket version remove this function.
        if (m_webSocket.isClosing()) return false;
        if (m_webSocket.isClosed()) return false;
        if (m_webSocket.isFlushAndClose()) return false;
        return false;
    }

    public void updateStoreGuidToBackOfficeAfterLogin()
    {
        if (isConnected())
           m_webSocket.sendStoreGuid();
    }

    /**
     * ["sync", "order_created"]
     * ["sync", "order_updated"]
     * ["sync", "order_voided"]
     * ["sync", "item_created"]
     * ["sync", "item_updated"]
     * ["sync", "item_voided"]
     *
     * Create Order:
     * 42["order_created", "eyJPcmRlciI6eyJJRCI6Ik8tMjAwMTEwLTEyMzQiLCJEZXN0aW5hdGlvbiI6IkRpbmUgSW4iLCJHdWVzdFRhYmxlIjoiMTIiLCJDdXN0b21lciI6eyJVc2VySW5mbyI6IkpvaG4gRG9lIFwvIFNocmltcCBBbGxlcmd5In0sIkl0ZW1zIjpbeyJJRCI6IlAtQ0IiLCJOYW1lIjoiQ2hlZXNlIEJ1cmdlciIsIkNhdGVnb3J5IjoiQnVyZ2VycyIsIktEU1N0YXRpb24iOjEsIlF1YW50aXR5IjoxLCJDb25kaW1lbnRzIjpbXX0seyJJRCI6IlMtRlIiLCJOYW1lIjoiRnJpZXMiLCJDYXRlZ29yeSI6IlNpZGVzIiwiS0RTU3RhdGlvbiI6MSwiUXVhbnRpdHkiOjEsIkNvbmRpbWVudHMiOlt7IklEIjoiQy1OUyIsIk5hbWUiOiJOTyBzYWx0In0seyJJRCI6IkMtRUMiLCJOYW1lIjoiRVhUUkEgQ2hlZGRhciJ9XX1dLCJPcmRlckd1aWQiOiIwMjYyOTE5Mi05YTM3LTRmNGMtYjIzYy0wNTczNjEwMDVhYmYifX0="]
     *
     * Base64 decode of message (Order info):
     * {"Order":{"ID":"O-200110-1234","Destination":"Dine In","GuestTable":"12","Customer":{"UserInfo":"John Doe / Shrimp Allergy"},"Items":[{"ID":"P-CB","Name":"Cheese Burger","Category":"Burgers","KDSStation":1,"Quantity":1,"Condiments":[]},{"ID":"S-FR","Name":"Fries","Category":"Sides","KDSStation":1,"Quantity":1,"Condiments":[{"ID":"C-NS","Name":"NO salt"},{"ID":"C-EC","Name":"EXTRA Cheddar"}]}],"OrderGuid":"02629192-9a37-4f4c-b23c-057361005abf"}}
     *
     * Void Order:
     * 42["order_voided", "eyJPcmRlckd1aWQiOiIwMjYyOTE5Mi05YTM3LTRmNGMtYjIzYy0wNTczNjEwMDVhYmYifQ=="]
     *
     * Base64 decode of message (OrderGuid only):
     * {"OrderGuid":"02629192-9a37-4f4c-b23c-057361005abf"}
     *
     * Update Order:
     * 42["order_updated", "eyJDdXN0b21lciI6eyJVc2VySW5mbyI6IlVJLTExLTEwIn0sIkRlc3RpbmF0aW9uIjoiTmV3IERlc3RpbmF0aW9uIiwiT3JkZXJHdWlkIjoiYjUzNGQ3OTItYmI0MS00YjdmLTkyNmUtYzY5ZTE5MDA1NzJhIn0="]
     *
     * Base64 decode of message (OrderGuid + updated elements):
     * {"Customer":{"UserInfo":"UI-11-10"},"Destination":"New Destination","OrderGuid":"b534d792-bb41-4b7f-926e-c69e1900572a"}
     *
     * Create Item:
     * 42["item_created", "eyJJRCI6Ikl0ZW0tTkVXIiwiTmFtZSI6IkZyaWVzIiwiQ2F0ZWdvcnkiOiJTaWRlcyIsIktEU1N0YXRpb24iOjEsIlF1YW50aXR5IjoxLCJDb25kaW1lbnRzIjpbeyJJRCI6IkNvbmQtMSIsIk5hbWUiOiJOTyBzYWx0In0seyJJRCI6IkNvbmQtMiIsIk5hbWUiOiJFWFRSQSBDaGVkZGFyIn1dLCJJdGVtR3VpZCI6WyIyM2JjNWQxNS02NTU0LTRlYzctOTQ4NC1iOWVkNzhlMjQ3YTQiXX0="]
     *
     * Base64 decode of message (Notice that ItemGuid is an array of 1 element for Premium):
     * {"ID":"Item-NEW","Name":"Fries","Category":"Sides","KDSStation":1,"Quantity":1,"Condiments":[{"ID":"Cond-1","Name":"NO salt"},{"ID":"Cond-2","Name":"EXTRA Cheddar"}],"ItemGuid":["23bc5d15-6554-4ec7-9484-b9ed78e247a4"]}
     *
     * Update Item:
     * 42["item_updated", "eyJLRFNTdGF0aW9uIjoyLCJRdWFudGl0eSI6NSwiSXRlbUd1aWQiOiIyM2JjNWQxNS02NTU0LTRlYzctOTQ4NC1iOWVkNzhlMjQ3YTQifQ=="]
     *
     * Base64 decode of message (ItemGuid + updated elements):
     * {"KDSStation":2,"Quantity":5,"ItemGuid":"23bc5d15-6554-4ec7-9484-b9ed78e247a4"}
     *
     * Void Item:
     * 42["item_voided", "eyJJdGVtR3VpZCI6IjIzYmM1ZDE1LTY1NTQtNGVjNy05NDg0LWI5ZWQ3OGUyNDdhNCJ9"]
     *
     * Base64 decode of message (ItemGuid only):
     * {"ItemGuid":"23bc5d15-6554-4ec7-9484-b9ed78e247a4"}
     * @param strNotification
     * @return
     */
    public boolean onNotification(String strNotification)
    {
        if (m_receiver == null) return false;
        if (strNotification.indexOf(MSG_KEY_MESSAGE)>=0)
        {

        }
        else //if (strNotification.indexOf(MSG_KEY_SYNC) >=0)
        {
            String ar[] = new String[]{API_EVENT_ORDER_CREATED,API_EVENT_ORDER_UPDATED ,API_EVENT_ORDER_VOIDED,
                                       API_EVENT_ITEM_CREATED, API_EVENT_ITEM_UPDATED, API_EVENT_ITEM_VOIDED};

            for (int i=0; i< ar.length; i++)
            {
                if (strNotification.indexOf(ar[i]) >=0) {
                    String data = getNotifyData(strNotification);
                    m_receiver.onBackofficeNotifyEvent(ar[i], data);
                    break;
                }
            }



        }

        return true;
    }

    static public void log2File(String info)
    {

        KDSLog.d(TAG, info);
    }
    /**
     [{
     "guid": "08d2dbc0-20f9-4fa6-9e87-3bd249274dc7",
     "update_device": "<null>",
     "items_count": "3",
     "user_info": "Carlos",
     "create_time": "1584301854",
     "guest_table": "Uber Eats",
     "store_guid": "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd",
     "pos_terminal": "<null>",
     "destination": "Delivery",
     "update_time": "1584301855",
     "done": "0",
     "smart_order_start_time": "0",
     "create_local_time": "1584301854",
     "upload_time": "0",
     "order_type": "ONLINE",
     "phone": "0",
     "server_name": "Logic Controls",
     "is_hidden": "0",
     "preparation_time": "0",
     "items": [
     {
     "beeped": "0",
     "build_card": "<null>",
     "category": "Hot",
     "condiments":
     [
     {
     "create_local_time": 1584301854,
     "create_time": 1584301854,
     "external_id": 1602,
     "guid": "be1f50f4-aa70-44cb-9f4f-95011be091ef",
     "is_deleted": 0,
     "item_guid": "a6263f24-092c-42e4-8cc6-53faed7e3e1e",
     "name": "Sauce",
     "pre_modifier": "<null>",
     "store_guid": "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd",
     "update_device": 0,
     "update_time": 1584301855,
     "upload_time": 0
     },
     {
     "create_local_time": 1584301854,
     "create_time": 1584301854,
     "external_id": 1603,
     "guid": "f42c7b4a-86b8-4ba1-89c2-1119033e493c",
     "is_deleted": 0,
     "item_guid": "a6263f24-092c-42e4-8cc6-53faed7e3e1e",
     "name": " Mayonnaise",
     "pre_modifier": "<null>",
     "store_guid": "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd",
     "update_device": 0,
     "update_time": 1584301855,
     "upload_time": 0
     }
     ],
     "condiments_count": 2,
     "create_local_time": 1584301854,
     "create_time": 1584301854,
     "device_id": 1,
     "external_id": 1502,
     "guid": "a6263f24-092c-42e4-8cc6-53faed7e3e1e",
     "is_deleted": 0,
     "is_hidden": 0,
     "is_priority": 0,
     "item_bump_guid": "3de0282a-76e4-4666-be69-43c582c77c9c",
     "name": "Hotdog",
     "order_guid": "08d2dbc0-20f9-4fa6-9e87-3bd249274dc7",
     "pre_modifier": "<null>",
     "preparation_time": "<null>",
     "printed_status": 0,
     "quantity": 2,
     "ready_since_local_time": 0,
     "recall_time": "<null>",
     "store_guid": "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd",
     "training_video": "<null>",
     "transfer_from_device_id": "<null>",
     "transfer_time": "<null>",
     "untransfer_time": "<null>",
     "update_device": "<null>",
     "update_time": 1584301855,
     "upload_time": 0
     },
     {
     "beeped": 0,
     "build_card": "<null>",
     "category": "Hot",
     "condiments":
     [{
     "create_local_time": 1584301854,
     "create_time": 1584301854,
     "external_id": 1602,
     "guid": "fb2c7dbe-bfa5-485b-9599-8a25c76e0640",
     "is_deleted": 0,
     "item_guid": "3c59e7e3-8f1f-4dcf-b85b-3ff034d0ef6e",
     "name": "Sauce",
     "pre_modifier": "<null>",
     "store_guid": "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd",
     "update_device": 0,
     "update_time": 1584301855,
     "upload_time": 0
     },
     {
     "create_local_time": 1584301854,
     "create_time": 1584301854,
     "external_id": 1603,
     "guid": "f8c42c67-7132-479c-afbc-b5b0a2656962",
     "is_deleted": 0,
     "item_guid": "3c59e7e3-8f1f-4dcf-b85b-3ff034d0ef6e",
     "name": "Mayonnaise",
     "pre_modifier": "<null>",
     "store_guid": "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd",
     "update_device": 0,
     "update_time": 1584301855,
     "upload_time": 0
     }],
     "condiments_count": 2,
     "create_local_time": 1584301854,
     "create_time": 1584301854,
     "device_id": 1,
     "external_id": 1502,
     "guid": "3c59e7e3-8f1f-4dcf-b85b-3ff034d0ef6e",
     "is_deleted": 0,
     "is_hidden": 0,
     "is_priority": 0,
     "item_bump_guid": "3de0282a-76e4-4666-be69-43c582c77c9c",
     "name": "Hotdog",
     "order_guid": "08d2dbc0-20f9-4fa6-9e87-3bd249274dc7",
     "pre_modifier": "<null>",
     "preparation_time": "<null>",
     "printed_status": 0,
     "quantity": 2,
     "ready_since_local_time": 0,
     "recall_time": "<null>",
     "store_guid": "6f4c0f56-da8b-4008-a7e5-c1b6233b1dcd",
     "training_video": "<null>",
     "transfer_from_device_id": "<null>",
     "transfer_time": "<null>",
     "untransfer_time": "<null>",
     "update_device": "<null>",
     "update_time": 1584301855,
     "upload_time": 0
     } ],
     "is_deleted": 0,
     "is_priority": 0,
     "external_id": 839,
     "customer_guid": "< null >"
     }]
     ///////kpp1-409
     {
     "Order":{
     "ID":"O-200110-1234",
     "Destination":"Dine In",
     "GuestTable":"12",
     "Customer":{
     "UserInfo":"John Doe / Shrimp Allergy"
     },
     "Items":[
     {
     "ID":"P-CB",
     "Name":"Cheese Burger",
     "Category":"Burgers",
     "KDSStation":1,
     "Quantity":1,
     "Condiments":[

     ]
     },
     {
     "ID":"S-FR",
     "Name":"Fries",
     "Category":"Sides",
     "KDSStation":1,
     "Quantity":1,
     "Condiments":[
     {
     "ID":"C-NS",
     "Name":"NO salt"
     },
     {
     "ID":"C-EC",
     "Name":"EXTRA Cheddar"
     }
     ]
     }
     ],
     "OrderGuid":"02629192-9a37-4f4c-b23c-057361005abf"
     }
     }
     /////////////////////////////////
     {
     "guid":"8f16cd9f-5e9e-4fd5-a4f1-e0662faba329",
     "store_guid":"ca1646de-3a24-4ce5-a5b5-cbe4c5165422",
     "destination":"Dine In",
     "external_id":"8156",
     "guest_table":"12",
     "is_priority":0,
     "items_count":1,
     "order_type":"ONLINE",
     "preparation_time":0,
     "server_name":"Logic Controls",
     "user_info":"John Doe / Shrimp Allergy",
     "done":0,
     "create_time":1606267535,
     "update_time":1606267536,
     "upload_time":0,
     "is_deleted":0,
     "phone":0,
     "create_local_time":1606267535,
     "is_hidden":0,
     "smart_order_start_time":0,
     "items":[
     {
     "guid":"7ead204a-4505-455e-812d-7605a38ebe3e",
     "order_guid":"8f16cd9f-5e9e-4fd5-a4f1-e0662faba329",
     "item_bump_guid":"e191fb4a-d01a-4173-a40e-ad9656fd1d1c",
     "name":"Cheese Burger",
     "device_id":5,
     "external_id":"123",
     "is_priority":0,
     "condiments_count":0,
     "beeped":0,
     "create_time":1606267535,
     "update_time":1606267536,
     "upload_time":0,
     "is_deleted":0,
     "printed_status":0,
     "create_local_time":1606267535,
     "is_hidden":0,
     "ready_since_local_time":0,
     "category":"Burgers",
     "quantity":1,
     "condiments":[

     ]
     }
     ]
     }
     * @param json
     * @return
     */
    private static KDSDataOrder parseBackOfficeOrderJson(JSONObject json)
    {
        try {

            KDSDataOrder order = new KDSDataOrder();

            order.setGUID(json.getString("guid"));
            order.setOrderName(json.getString("external_id"));
            order.setDestination(json.getString("destination"));
            order.setToTable(json.getString("guest_table"));

            order.setStartTime(json.getLong("create_time")*1000);//kpp1-403, backoffice return seconds, we need ms.
            order.setCustomMsg(json.getString("user_info"));

            //order.setFromPOSNumber(json.getString("pos_terminal"));

            order.setOrderType(json.getString("order_type"));
            order.setWaiterName(json.getString("server_name"));
            //customer
            if (json.has("customer"))
            {
                JSONObject jsonCustomer = json.getJSONObject("customer");
                order.getCustomer().setName(jsonCustomer.getString("name"));
                order.getCustomer().setPhone(jsonCustomer.getString("phone"));
            }
            JSONArray arItems = (JSONArray) json.get("items");
            for (int i=0; i< arItems.length(); i++)
            {
                JSONObject jsonItem = (JSONObject) arItems.get(i);

                KDSDataItem item = parseJsonItemCreated(order.getGUID(), jsonItem);
                if (item == null) continue;
                order.getItems().addComponent(item);
            }

            return order;


        }catch (Exception e)
        {
            KDSLog.i(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * @param evt
     * @param strData
     *  It can contain multiple orders in it.
     * @return
     */
    public static KDSDataOrders parseApiJson(String evt, String strData)
    {
        try {
            KDSDataOrders orders = new KDSDataOrders();
//            JSONArray ar = new JSONArray(strData);
//            if (ar == null)
//                return null;
//            if (ar.length() <= 0) return null;
            switch (evt)
            {
                case API_EVENT_ORDER_CREATED:
                {

                    KDSDataOrder order = parseJsonOrderCreate(strData);
                    if (order != null)
                        orders.addOrderWithoutSort(order);
                }
                break;
                case API_EVENT_ORDER_UPDATED:
                {
                    KDSDataOrder order = parseJsonOrderUpdate(strData);
                    if (order != null)
                        orders.addOrderWithoutSort(order);
                }
                break;
                case API_EVENT_ORDER_VOIDED:
                {
                    KDSDataOrder order = parseJsonOrderVoid(strData);
                    if (order != null)
                        orders.addOrderWithoutSort(order);
                }
                break;
                case API_EVENT_ITEM_CREATED:
                {
                    JSONArray jsons = new JSONArray(strData);
                    KDSDataOrder order = new KDSDataOrder();
                    for (int i=0; i< jsons.length(); i++) {
                        JSONObject json = jsons.getJSONObject(i);
                        String orderGuid = json.getString("order_guid");
                        KDSDataItem item = parseJsonItemCreated(orderGuid, json);

                        order.setGUID(orderGuid);
                        order.setTransType(KDSDataOrder.TRANSTYPE_MODIFY);
                        order.getItems().addComponent(item);
                    }
                    if (jsons.length() >0)
                        orders.addOrderWithoutSort(order);

                }
                break;
                case API_EVENT_ITEM_UPDATED:
                {
                    String orderGuid = "";//KDSConst.ORDER_GUID_FOR_API_ITEM_CHANGES;
                    KDSDataItem item = parseJsonItemUpdated(orderGuid, strData);
                    orderGuid = item.getOrderGUID();
                    KDSDataOrder order = new KDSDataOrder();
                    order.setGUID(orderGuid);
                    order.setTransType(KDSDataOrder.TRANSTYPE_MODIFY);

                    order.getItems().addComponent(item);
                    orders.addOrderWithoutSort(order);
                }
                break;
                case API_EVENT_ITEM_VOIDED:
                {
                    String orderGuid = "";//KDSConst.ORDER_GUID_FOR_API_ITEM_CHANGES;
                    KDSDataItem item = parseJsonItemVoid(orderGuid, strData);
                    orderGuid = item.getOrderGUID();
                    KDSDataOrder order = new KDSDataOrder();
                    order.setGUID(orderGuid);
                    order.setTransType(KDSDataOrder.TRANSTYPE_MODIFY);

                    order.getItems().addComponent(item);
                    orders.addOrderWithoutSort(order);
                }
                break;
                default:
                    break;
            }
//            for (int i=0; i< ar.length(); i++) {
//                JSONObject json = (JSONObject) ar.get(i);//order data.
//                KDSDataOrder order = parseBackOfficeOrderJson(json);
//                if (order != null)
//                    orders.addOrderWithoutSort(order);
//            }
            return orders;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
        }
        return null;
    }

    /**
     *
     * @param strData
     * {"Order":{"ID":"O-200110-1234","Destination":"Dine In","GuestTable":"12","Customer":{"UserInfo":"John Doe / Shrimp Allergy"},"Items":[{"ID":"P-CB","Name":"Cheese Burger","Category":"Burgers","KDSStation":1,"Quantity":1,"Condiments":[]},{"ID":"S-FR","Name":"Fries","Category":"Sides","KDSStation":1,"Quantity":1,"Condiments":[{"ID":"C-NS","Name":"NO salt"},{"ID":"C-EC","Name":"EXTRA Cheddar"}]}],"OrderGuid":"02629192-9a37-4f4c-b23c-057361005abf"}}
     * @return
     */
    static KDSDataOrder parseJsonOrderCreate(String strData)
    {
        try {


            JSONObject json = new JSONObject(strData);
            //JSONObject jsonOrder =  json.getJSONObject("Order");
            KDSDataOrder order = parseBackOfficeOrderJson(json);
            return order;
        }
        catch (Exception e)
        {

        }
        return null;
    }

    /**
     *
     * @param strData
     *
     * {"guid":"6a029c37-7b72-4b89-9277-5c638b9f8bac"}
     * @return
     */
    static KDSDataOrder parseJsonOrderVoid(String strData)
    {
        try {
            JSONObject json = new JSONObject(strData);
            String orderGuid = json.getString("guid");
            KDSDataOrder order = new KDSDataOrder();
            order.setGUID(orderGuid);
            order.setTransType(KDSDataOrder.TRANSTYPE_DELETE);
            return order;
        }
        catch ( Exception e)
        {
            e.printStackTrace();

        }
        return null;
    }

    /**
     *
     * @param strData
     *
     *{
     *     "external_id":"O-200110-1239",
     *     "guest_table":"GT-10-47",
     *     "destination":"Dest-10-47",
     *     "user_info":"UI-10-47",
     *     "guid":"c6a9d6b2-94a5-4089-8d6d-fba6618bab03"
     * }
     *
     * @return
     */
    static KDSDataOrder parseJsonOrderUpdate(String strData)
    {
        try {
            JSONObject json = new JSONObject(strData);

            KDSDataOrder order = new KDSDataOrder();

            order.setGUID(json.getString("guid")); //this must existed.
            order.setTransType(KDSDataOrder.TRANSTYPE_MODIFY);

            if (json.has("external_id")) //kpp1-422
                order.setOrderName(json.getString("external_id"));

            //order.setStartTime(json.getLong("create_time") * 1000);//kpp1-403, backoffice return seconds, we need ms.
            if (json.has("user_info"))
                order.setCustomMsg(json.getString("user_info"));
            if (json.has("guest_table"))
                order.setToTable(json.getString("guest_table"));
            if (json.has("pos_terminal"))
                order.setFromPOSNumber(json.getString("pos_terminal"));
            if (json.has("destination"))
                order.setDestination(json.getString("destination"));
            if (json.has("order_type"))
                order.setOrderType(json.getString("order_type"));
            if (json.has("server_name"))
                order.setWaiterName(json.getString("server_name"));
            //customer
            if (json.has("customer")) {
                JSONObject jsonCustomer = json.getJSONObject("customer");
                if (jsonCustomer.has("name"))
                    order.getCustomer().setName(jsonCustomer.getString("name"));
                if (jsonCustomer.has("phone"))
                    order.getCustomer().setPhone(jsonCustomer.getString("phone"));
            }
            return order;
        }
        catch ( Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
//    public boolean isGooglePlayServicesAvailable(Context context)
//    {
//        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
//        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
//        return (resultCode == ConnectionResult.SUCCESS);
//    }

    /**
     *
     * @param strVal
     * @return
     */
    static private boolean isNullStringValue(String strVal)
    {
        if (strVal.isEmpty() ||
                strVal.equals("<null>") ||
                strVal.equals("null"))
            return true;
        return false;
    }

    /**
     *
     [
        {
             "guid":"819c9124-60a7-45cc-9762-0ba17e30e8fc",
             "order_guid":"1bb09597-de13-45f1-ad92-d7db34067e42",
             "item_bump_guid":"3f821cb4-4243-47b7-9700-8ef66b5b51ae",
             "name":"New Fries",
             "device_id":1,
             "external_id":"Item-NEW",
             "is_priority":0,
             "condiments_count":2,
             "beeped":0,
             "create_time":1606277129,
             "update_time":1606277130,
             "upload_time":0,
             "is_deleted":0,
             "printed_status":0,
             "create_local_time":1606277129,
             "is_hidden":0,
             "ready_since_local_time":0,
             "category":"Sides",
             "quantity":1,
             "condiments":[
             {
                 "guid":"f59c5fb3-2446-4d26-845c-bb6faa20cc68",
                 "item_guid":"819c9124-60a7-45cc-9762-0ba17e30e8fc",
                 "external_id":"Cond-1",
                 "name":"New NO salt",
                 "create_time":1606277129,
                 "update_time":1606277130,
                 "upload_time":0,
                 "is_deleted":0,
                 "update_device":0,
                 "create_local_time":1606277129
             },
             {
                 "guid":"6b5e79ee-7379-4d55-98bf-a8681149c60e",
                 "item_guid":"819c9124-60a7-45cc-9762-0ba17e30e8fc",
                 "external_id":"Cond-2",
                 "name":"New EXTRA Cheddar",
                 "create_time":1606277129,
                 "update_time":1606277130,
                 "upload_time":0,
                 "is_deleted":0,
                 "update_device":0,
                 "create_local_time":1606277129
             }
             ]
        }
     ]

     * @param orderGuid
     * @param jsonItem
     * @return
     */
    static private KDSDataItem parseJsonItemCreated(String orderGuid, JSONObject jsonItem)
    {
        try {
            KDSDataItem item = new KDSDataItem();
            item.setOrderGUID(orderGuid);
            item.setLocalBumped(false);
            item.setItemName(jsonItem.getString("external_id"));
            item.setDescription(jsonItem.getString("name"));
            item.setCategory(jsonItem.getString("category"));
            //item.setBuildCard(jsonItem.getString("build_card"));
            if (!ENABLE_DEBUG)
                item.setToStationsString(jsonItem.getString("device_id")); //get to stations from "device_id"

            item.setQty(jsonItem.getInt("quantity"));
            item.setGUID(jsonItem.getString("guid"));

            item.setItemBumpGuid(jsonItem.getString("item_bump_guid"));

//            String preModifiers = jsonItem.getString("pre_modifier");
//            if (!isNullStringValue(preModifiers)) {
//                KDSDataMessages messages = KDSDataMessages.parseString(preModifiers, item.getGUID(), KDSDataMessage.FOR_Item);
//                item.setPreModifiers(messages);
//            }
            JSONArray arCondiments = jsonItem.getJSONArray("condiments");
            for (int j = 0; j < arCondiments.length(); j++) {
                JSONObject jsonCondiment = (JSONObject) arCondiments.get(j);

                KDSDataCondiment condiment = parseCondimentJson(item.getGUID(), jsonCondiment);
                if (condiment == null) continue;
                item.getCondiments().addComponent(condiment);
            }
            return item;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
        }
        return null;
    }

    /**
     * {"KDSStation":2,"Quantity":5,"ItemGuid":"23bc5d15-6554-4ec7-9484-b9ed78e247a4"}
     *
     * @param orderGuid
     * @param strJson
     * {"quantity":6,"guid":"f0e9b98e-8e8e-4fcb-8965-8d90d9efb91c","order_guid":"62ee4e70-37f9-4c91-81c6-4c650a0e9eec"}
     * @return
     */
    static private KDSDataItem parseJsonItemUpdated(String orderGuid, String strJson)
    {
        try {
            JSONObject jsonItem = new JSONObject(strJson);


            KDSDataItem item = new KDSDataItem();
            if (orderGuid.isEmpty())
                orderGuid = jsonItem.getString("order_guid");

            item.setOrderGUID(orderGuid);
            item.setLocalBumped(false);
            item.setTransType(KDSDataOrder.TRANSTYPE_MODIFY);
            item.setGUID(jsonItem.getString("guid"));

            if (jsonItem.has("external_id"))
                item.setItemName(jsonItem.getString("external_id"));
            if (jsonItem.has("name"))
                item.setDescription(jsonItem.getString("name"));
            if (jsonItem.has("category"))
                item.setCategory(jsonItem.getString("category"));
            //item.setBuildCard(jsonItem.getString("build_card"));
            if (jsonItem.has("device_id"))
                item.setToStationsString(jsonItem.getString("device_id")); //get to stations from "device_id"
            if (jsonItem.has("quantity"))
                item.setQty(jsonItem.getInt("quantity"));
            return item;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }


    /**
     *
     * @param orderGuid
     * @param strJson
     * {"guid":"f0e9b98e-8e8e-4fcb-8965-8d90d9efb91c","order_guid":"62ee4e70-37f9-4c91-81c6-4c650a0e9eec"}
     * @return
     */
    static private KDSDataItem parseJsonItemVoid(String orderGuid, String strJson)
    {
        try {
            JSONObject jsonItem = new JSONObject(strJson);
            if (orderGuid.isEmpty())
                orderGuid = jsonItem.getString("order_guid");

            KDSDataItem item = new KDSDataItem();
            item.setOrderGUID(orderGuid);
            item.setLocalBumped(false);
            item.setTransType(KDSDataOrder.TRANSTYPE_DELETE);
            item.setGUID(jsonItem.getString("guid"));

            return item;
        }
        catch (Exception e)
        {

        }
        return null;
    }

    /**
     *
     * @param itemGuid
     * @param jsonCondiment
     *  "condiments":[
     *                 {
     *                     "guid":"a1144b56-8dbc-47fd-b3e0-5f038bf09a3c",
     *                     "item_guid":"533fa8db-6c44-4631-9648-5f3485d3311a",
     *                     "external_id":"C-NS",
     *                     "name":"NO salt",
     *                     "create_time":1606268135,
     *                     "update_time":1606268136,
     *                     "upload_time":0,
     *                     "is_deleted":0,
     *                     "update_device":0,
     *                     "create_local_time":1606268135
     *                 },
     *                 {
     *                     "guid":"f500ed3c-e1e6-4892-becc-7cdba477d0e3",
     *                     "item_guid":"533fa8db-6c44-4631-9648-5f3485d3311a",
     *                     "external_id":"C-EC",
     *                     "name":"EXTRA Cheddar",
     *                     "create_time":1606268135,
     *                     "update_time":1606268136,
     *                     "upload_time":0,
     *                     "is_deleted":0,
     *                     "update_device":0,
     *                     "create_local_time":1606268135
     *                 }
     *             ]
     * @return
     */
    static private KDSDataCondiment parseCondimentJson(String itemGuid, JSONObject jsonCondiment)
    {
        try {

            KDSDataCondiment c = new KDSDataCondiment();
            c.setItemGUID(itemGuid);
            c.setCondimentName(jsonCondiment.getString("external_id"));
            c.setGUID(jsonCondiment.getString("guid"));
            c.setDescription(jsonCondiment.getString("name"));
//            String condimentPremodifiers = jsonCondiment.getString("pre_modifier");
//            if (!isNullStringValue(condimentPremodifiers)) {
//                KDSDataMessages messages = KDSDataMessages.parseString(condimentPremodifiers, c.getGUID(), KDSDataMessage.FOR_Condiment);
//                c.setMessages(messages);
//            }

            return c;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
        }
        return null;
    }


    /**
     * for test
     * @return
     */
    public void doTestLoop()
    {
        Timer m_timer = new Timer();
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
//                if (mBroadcaster == null)
//                    mBroadcaster = LocalBroadcastManager.getInstance(KDSFirebaseMessagingService.this);
//                Intent intent = new Intent(BROADCAST_ID);
//                mBroadcaster.sendBroadcast(intent);
            }

        },5000, 5000);


    }
    static public String getFCMTestString2()
    {
        String s = "[\n" +
                "    {\n" +
                "        \"guid\": \"ba75857b-7dc7-4ff1-bdea-b0380026bcc5\",\n" +
                "        \"store_guid\": \"6979df6f-694a-4366-b8ff-4084592c8a95\",\n" +
                "        \"destination\": \"Delivery\",\n" +
                "        \"external_id\": \"845\",\n" +
                "        \"guest_table\": \"Uber Eats\",\n" +
                "        \"is_priority\": 0,\n" +
                "        \"items_count\": 3,\n" +
                "        \"order_type\": \"ONLINE\",\n" +
                "        \"pos_terminal\": null,\n" +
                "        \"preparation_time\": 0,\n" +
                "        \"server_name\": \"Logic Controls\",\n" +
                "        \"user_info\": \"Carlos\",\n" +
                "        \"done\": 0,\n" +
                "        \"create_time\": 1584630758,\n" +
                "        \"update_time\": 1584630759,\n" +
                "        \"upload_time\": 0,\n" +
                "        \"is_deleted\": 0,\n" +
                "        \"update_device\": null,\n" +
                "        \"phone\": \"0\",\n" +
                "        \"create_local_time\": 1584630758,\n" +
                "        \"is_hidden\": 0,\n" +
                "        \"customer_guid\": null,\n" +
                "        \"smart_order_start_time\": 0,\n" +
                "        \"items\": [\n" +
                "            {\n" +
                "                \"guid\": \"750f0a66-37c5-4fae-bb38-5aecb4768bca\",\n" +
                "                \"order_guid\": \"ba75857b-7dc7-4ff1-bdea-b0380026bcc5\",\n" +
                "                \"name\": \"Hotdog\",\n" +
                "                \"device_id\": 1,\n" +
                "                \"external_id\": \"1502\",\n" +
                "                \"is_priority\": 0,\n" +
                "                \"condiments_count\": 2,\n" +
                "                \"pre_modifier\": null,\n" +
                "                \"preparation_time\": null,\n" +
                "                \"recall_time\": null,\n" +
                "                \"training_video\": null,\n" +
                "                \"transfer_from_device_id\": null,\n" +
                "                \"transfer_time\": null,\n" +
                "                \"untransfer_time\": null,\n" +
                "                \"beeped\": 0,\n" +
                "                \"build_card\": null,\n" +
                "                \"create_time\": 1584630758,\n" +
                "                \"update_time\": 1584630759,\n" +
                "                \"upload_time\": 0,\n" +
                "                \"is_deleted\": 0,\n" +
                "                \"update_device\": null,\n" +
                "                \"printed_status\": 0,\n" +
                "                \"item_bump_guid\": \"b058c671-72a0-4f37-92fe-481659deaec8\",\n" +
                "                \"create_local_time\": 1584630758,\n" +
                "                \"is_hidden\": 0,\n" +
                "                \"ready_since_local_time\": 0,\n" +
                "                \"category\": \"Hot\",\n" +
                "                \"quantity\": 2,\n" +
                "                \"store_guid\": \"6979df6f-694a-4366-b8ff-4084592c8a95\",\n" +
                "                \"condiments\": [\n" +
                "                    {\n" +
                "                        \"guid\": \"ccbc44df-5870-46b4-a46e-4cf55dd67fd5\",\n" +
                "                        \"store_guid\": \"6979df6f-694a-4366-b8ff-4084592c8a95\",\n" +
                "                        \"item_guid\": \"750f0a66-37c5-4fae-bb38-5aecb4768bca\",\n" +
                "                        \"external_id\": \"1602\",\n" +
                "                        \"name\": \"Sauce\",\n" +
                "                        \"pre_modifier\": null,\n" +
                "                        \"create_time\": 1584630758,\n" +
                "                        \"update_time\": 1584630759,\n" +
                "                        \"upload_time\": 0,\n" +
                "                        \"is_deleted\": 0,\n" +
                "                        \"update_device\": \"0\",\n" +
                "                        \"create_local_time\": 1584630758\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"guid\": \"87946e10-bc9e-48f8-ad3d-e7d08e3512e9\",\n" +
                "                        \"store_guid\": \"6979df6f-694a-4366-b8ff-4084592c8a95\",\n" +
                "                        \"item_guid\": \"750f0a66-37c5-4fae-bb38-5aecb4768bca\",\n" +
                "                        \"external_id\": \"1603\",\n" +
                "                        \"name\": \"Mayonnaise\",\n" +
                "                        \"pre_modifier\": null,\n" +
                "                        \"create_time\": 1584630758,\n" +
                "                        \"update_time\": 1584630759,\n" +
                "                        \"upload_time\": 0,\n" +
                "                        \"is_deleted\": 0,\n" +
                "                        \"update_device\": \"0\",\n" +
                "                        \"create_local_time\": 1584630758\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"guid\": \"72df2118-053e-461c-bd93-e66896aec364\",\n" +
                "                \"order_guid\": \"ba75857b-7dc7-4ff1-bdea-b0380026bcc5\",\n" +
                "                \"name\": \"Hotdog\",\n" +
                "                \"device_id\": 1,\n" +
                "                \"external_id\": \"1502\",\n" +
                "                \"is_priority\": 0,\n" +
                "                \"condiments_count\": 2,\n" +
                "                \"pre_modifier\": null,\n" +
                "                \"preparation_time\": null,\n" +
                "                \"recall_time\": null,\n" +
                "                \"training_video\": null,\n" +
                "                \"transfer_from_device_id\": null,\n" +
                "                \"transfer_time\": null,\n" +
                "                \"untransfer_time\": null,\n" +
                "                \"beeped\": 0,\n" +
                "                \"build_card\": null,\n" +
                "                \"create_time\": 1584630758,\n" +
                "                \"update_time\": 1584630759,\n" +
                "                \"upload_time\": 0,\n" +
                "                \"is_deleted\": 0,\n" +
                "                \"update_device\": null,\n" +
                "                \"printed_status\": 0,\n" +
                "                \"item_bump_guid\": \"b058c671-72a0-4f37-92fe-481659deaec8\",\n" +
                "                \"create_local_time\": 1584630758,\n" +
                "                \"is_hidden\": 0,\n" +
                "                \"ready_since_local_time\": 0,\n" +
                "                \"category\": \"Hot\",\n" +
                "                \"quantity\": 2,\n" +
                "                \"store_guid\": \"6979df6f-694a-4366-b8ff-4084592c8a95\",\n" +
                "                \"condiments\": [\n" +
                "                    {\n" +
                "                        \"guid\": \"ad160df5-b0ec-43a6-b317-6455a1821cc6\",\n" +
                "                        \"store_guid\": \"6979df6f-694a-4366-b8ff-4084592c8a95\",\n" +
                "                        \"item_guid\": \"72df2118-053e-461c-bd93-e66896aec364\",\n" +
                "                        \"external_id\": \"1602\",\n" +
                "                        \"name\": \"Sauce\",\n" +
                "                        \"pre_modifier\": null,\n" +
                "                        \"create_time\": 1584630758,\n" +
                "                        \"update_time\": 1584630759,\n" +
                "                        \"upload_time\": 0,\n" +
                "                        \"is_deleted\": 0,\n" +
                "                        \"update_device\": \"0\",\n" +
                "                        \"create_local_time\": 1584630758\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"guid\": \"1e0f694e-09ff-4c5f-8f8e-45c46d037df4\",\n" +
                "                        \"store_guid\": \"6979df6f-694a-4366-b8ff-4084592c8a95\",\n" +
                "                        \"item_guid\": \"72df2118-053e-461c-bd93-e66896aec364\",\n" +
                "                        \"external_id\": \"1603\",\n" +
                "                        \"name\": \"Mayonnaise\",\n" +
                "                        \"pre_modifier\": null,\n" +
                "                        \"create_time\": 1584630758,\n" +
                "                        \"update_time\": 1584630759,\n" +
                "                        \"upload_time\": 0,\n" +
                "                        \"is_deleted\": 0,\n" +
                "                        \"update_device\": \"0\",\n" +
                "                        \"create_local_time\": 1584630758\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"guid\": \"af6056ad-5c65-479c-a6ac-ab8b2cec1e18\",\n" +
                "                \"order_guid\": \"ba75857b-7dc7-4ff1-bdea-b0380026bcc5\",\n" +
                "                \"name\": \"French Fries\",\n" +
                "                \"device_id\": 1,\n" +
                "                \"external_id\": \"1501\",\n" +
                "                \"is_priority\": 0,\n" +
                "                \"condiments_count\": 1,\n" +
                "                \"pre_modifier\": null,\n" +
                "                \"preparation_time\": null,\n" +
                "                \"recall_time\": null,\n" +
                "                \"training_video\": null,\n" +
                "                \"transfer_from_device_id\": null,\n" +
                "                \"transfer_time\": null,\n" +
                "                \"untransfer_time\": null,\n" +
                "                \"beeped\": 0,\n" +
                "                \"build_card\": null,\n" +
                "                \"create_time\": 1584630758,\n" +
                "                \"update_time\": 1584630759,\n" +
                "                \"upload_time\": 0,\n" +
                "                \"is_deleted\": 0,\n" +
                "                \"update_device\": null,\n" +
                "                \"printed_status\": 0,\n" +
                "                \"item_bump_guid\": \"f63c65f4-248a-44b8-bbf5-4f4b6939e7c6\",\n" +
                "                \"create_local_time\": 1584630758,\n" +
                "                \"is_hidden\": 0,\n" +
                "                \"ready_since_local_time\": 0,\n" +
                "                \"category\": \"Hot\",\n" +
                "                \"quantity\": 1,\n" +
                "                \"store_guid\": \"6979df6f-694a-4366-b8ff-4084592c8a95\",\n" +
                "                \"condiments\": [\n" +
                "                    {\n" +
                "                        \"guid\": \"fca60381-6ef8-4fa4-9126-8507cd8e8f57\",\n" +
                "                        \"store_guid\": \"6979df6f-694a-4366-b8ff-4084592c8a95\",\n" +
                "                        \"item_guid\": \"af6056ad-5c65-479c-a6ac-ab8b2cec1e18\",\n" +
                "                        \"external_id\": \"1601\",\n" +
                "                        \"name\": \"Medium\",\n" +
                "                        \"pre_modifier\": null,\n" +
                "                        \"create_time\": 1584630758,\n" +
                "                        \"update_time\": 1584630759,\n" +
                "                        \"upload_time\": 0,\n" +
                "                        \"is_deleted\": 0,\n" +
                "                        \"update_device\": \"0\",\n" +
                "                        \"create_local_time\": 1584630758\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"guid\": \"c2380a844f844ea68e9c13a3bd480281\",\n" +
                "        \"store_guid\": \"6979df6f-694a-4366-b8ff-4084592c8a95\",\n" +
                "        \"destination\": \"FastFood\",\n" +
                "        \"external_id\": \"5\",\n" +
                "        \"guest_table\": \"23\",\n" +
                "        \"is_priority\": 0,\n" +
                "        \"items_count\": 5,\n" +
                "        \"order_type\": \"RUSH\",\n" +
                "        \"pos_terminal\": \"2\",\n" +
                "        \"preparation_time\": 0,\n" +
                "        \"server_name\": \"Jack\",\n" +
                "        \"user_info\": \"userinfo\",\n" +
                "        \"done\": 0,\n" +
                "        \"create_time\": 1584635691,\n" +
                "        \"update_time\": 1584635691,\n" +
                "        \"upload_time\": 1584635530,\n" +
                "        \"is_deleted\": 0,\n" +
                "        \"update_device\": \"00668734-4aad-49d3-ad6f-d390c64e7c9d\",\n" +
                "        \"phone\": \"\",\n" +
                "        \"create_local_time\": 1584621291,\n" +
                "        \"is_hidden\": 0,\n" +
                "        \"customer_guid\": \"\",\n" +
                "        \"smart_order_start_time\": 0,\n" +
                "        \"items\": [\n" +
                "            {\n" +
                "                \"guid\": \"bae76a9c2546473db291fc445fb3eab3\",\n" +
                "                \"order_guid\": \"c2380a844f844ea68e9c13a3bd480281\",\n" +
                "                \"name\": \"Coffee\",\n" +
                "                \"device_id\": 1,\n" +
                "                \"external_id\": \"10\",\n" +
                "                \"is_priority\": 0,\n" +
                "                \"condiments_count\": 2,\n" +
                "                \"pre_modifier\": \"Seat 2\\nextra msg \",\n" +
                "                \"preparation_time\": 0,\n" +
                "                \"recall_time\": null,\n" +
                "                \"training_video\": \"\",\n" +
                "                \"transfer_from_device_id\": null,\n" +
                "                \"transfer_time\": null,\n" +
                "                \"untransfer_time\": null,\n" +
                "                \"beeped\": 48,\n" +
                "                \"build_card\": \"\",\n" +
                "                \"create_time\": 1584635691,\n" +
                "                \"update_time\": 1584635691,\n" +
                "                \"upload_time\": 1584635545,\n" +
                "                \"is_deleted\": 0,\n" +
                "                \"update_device\": \"00668734-4aad-49d3-ad6f-d390c64e7c9d\",\n" +
                "                \"printed_status\": 0,\n" +
                "                \"item_bump_guid\": \"5035d580db0341eeab496abcea850e89\",\n" +
                "                \"create_local_time\": 1584621291,\n" +
                "                \"is_hidden\": 0,\n" +
                "                \"ready_since_local_time\": 0,\n" +
                "                \"category\": \"Beverages\",\n" +
                "                \"quantity\": 1,\n" +
                "                \"store_guid\": null,\n" +
                "                \"condiments\": []\n" +
                "            },\n" +
                "            {\n" +
                "                \"guid\": \"762cf157631f46a08b983fa13e73df1a\",\n" +
                "                \"order_guid\": \"c2380a844f844ea68e9c13a3bd480281\",\n" +
                "                \"name\": \"Vege Soup\",\n" +
                "                \"device_id\": 1,\n" +
                "                \"external_id\": \"11\",\n" +
                "                \"is_priority\": 0,\n" +
                "                \"condiments_count\": 0,\n" +
                "                \"pre_modifier\": \"\",\n" +
                "                \"preparation_time\": 0,\n" +
                "                \"recall_time\": null,\n" +
                "                \"training_video\": \"\",\n" +
                "                \"transfer_from_device_id\": null,\n" +
                "                \"transfer_time\": null,\n" +
                "                \"untransfer_time\": null,\n" +
                "                \"beeped\": 48,\n" +
                "                \"build_card\": \"\",\n" +
                "                \"create_time\": 1584635691,\n" +
                "                \"update_time\": 1584635691,\n" +
                "                \"upload_time\": 1584635545,\n" +
                "                \"is_deleted\": 0,\n" +
                "                \"update_device\": \"00668734-4aad-49d3-ad6f-d390c64e7c9d\",\n" +
                "                \"printed_status\": 0,\n" +
                "                \"item_bump_guid\": \"54d45f56e3d94e188e61c71b63a83841\",\n" +
                "                \"create_local_time\": 1584621291,\n" +
                "                \"is_hidden\": 0,\n" +
                "                \"ready_since_local_time\": 0,\n" +
                "                \"category\": \"Soup\",\n" +
                "                \"quantity\": 2,\n" +
                "                \"store_guid\": null,\n" +
                "                \"condiments\": []\n" +
                "            },\n" +
                "            {\n" +
                "                \"guid\": \"d99001185c1b49efb3b4f7f31cd0e6ec\",\n" +
                "                \"order_guid\": \"c2380a844f844ea68e9c13a3bd480281\",\n" +
                "                \"name\": \"Apple Pie\",\n" +
                "                \"device_id\": 1,\n" +
                "                \"external_id\": \"12\",\n" +
                "                \"is_priority\": 0,\n" +
                "                \"condiments_count\": 2,\n" +
                "                \"pre_modifier\": \"\",\n" +
                "                \"preparation_time\": 0,\n" +
                "                \"recall_time\": null,\n" +
                "                \"training_video\": \"\",\n" +
                "                \"transfer_from_device_id\": null,\n" +
                "                \"transfer_time\": null,\n" +
                "                \"untransfer_time\": null,\n" +
                "                \"beeped\": 48,\n" +
                "                \"build_card\": \"\",\n" +
                "                \"create_time\": 1584635691,\n" +
                "                \"update_time\": 1584635691,\n" +
                "                \"upload_time\": 1584635546,\n" +
                "                \"is_deleted\": 0,\n" +
                "                \"update_device\": \"00668734-4aad-49d3-ad6f-d390c64e7c9d\",\n" +
                "                \"printed_status\": 0,\n" +
                "                \"item_bump_guid\": \"70dbbbc136f14dd9b157393c2049fbde\",\n" +
                "                \"create_local_time\": 1584621291,\n" +
                "                \"is_hidden\": 0,\n" +
                "                \"ready_since_local_time\": 0,\n" +
                "                \"category\": \"Desserts\",\n" +
                "                \"quantity\": 2,\n" +
                "                \"store_guid\": null,\n" +
                "                \"condiments\": []\n" +
                "            },\n" +
                "            {\n" +
                "                \"guid\": \"b17c20dbee034e499cc652fdbf69f98e\",\n" +
                "                \"order_guid\": \"c2380a844f844ea68e9c13a3bd480281\",\n" +
                "                \"name\": \"Garden salad\",\n" +
                "                \"device_id\": 1,\n" +
                "                \"external_id\": \"13\",\n" +
                "                \"is_priority\": 0,\n" +
                "                \"condiments_count\": 2,\n" +
                "                \"pre_modifier\": \"\",\n" +
                "                \"preparation_time\": 0,\n" +
                "                \"recall_time\": null,\n" +
                "                \"training_video\": \"\",\n" +
                "                \"transfer_from_device_id\": null,\n" +
                "                \"transfer_time\": null,\n" +
                "                \"untransfer_time\": null,\n" +
                "                \"beeped\": 48,\n" +
                "                \"build_card\": \"\",\n" +
                "                \"create_time\": 1584635691,\n" +
                "                \"update_time\": 1584635691,\n" +
                "                \"upload_time\": 1584635547,\n" +
                "                \"is_deleted\": 0,\n" +
                "                \"update_device\": \"00668734-4aad-49d3-ad6f-d390c64e7c9d\",\n" +
                "                \"printed_status\": 0,\n" +
                "                \"item_bump_guid\": \"b25cc9032fba41c5923a78f3678303ae\",\n" +
                "                \"create_local_time\": 1584621291,\n" +
                "                \"is_hidden\": 0,\n" +
                "                \"ready_since_local_time\": 0,\n" +
                "                \"category\": \"Salads\",\n" +
                "                \"quantity\": 2,\n" +
                "                \"store_guid\": null,\n" +
                "                \"condiments\": []\n" +
                "            },\n" +
                "            {\n" +
                "                \"guid\": \"091cb4f6a2d44440ab31371c226dd688\",\n" +
                "                \"order_guid\": \"c2380a844f844ea68e9c13a3bd480281\",\n" +
                "                \"name\": \"Cheese Sandwish\",\n" +
                "                \"device_id\": 1,\n" +
                "                \"external_id\": \"14\",\n" +
                "                \"is_priority\": 0,\n" +
                "                \"condiments_count\": 2,\n" +
                "                \"pre_modifier\": \"Pre-Modifier 0\\nPre-Modifier 1\",\n" +
                "                \"preparation_time\": 0,\n" +
                "                \"recall_time\": null,\n" +
                "                \"training_video\": \"\",\n" +
                "                \"transfer_from_device_id\": null,\n" +
                "                \"transfer_time\": null,\n" +
                "                \"untransfer_time\": null,\n" +
                "                \"beeped\": 48,\n" +
                "                \"build_card\": \"\",\n" +
                "                \"create_time\": 1584635691,\n" +
                "                \"update_time\": 1584635691,\n" +
                "                \"upload_time\": 1584635542,\n" +
                "                \"is_deleted\": 0,\n" +
                "                \"update_device\": \"00668734-4aad-49d3-ad6f-d390c64e7c9d\",\n" +
                "                \"printed_status\": 0,\n" +
                "                \"item_bump_guid\": \"c467ddc6c29c4ff7b690fb1a18e3856c\",\n" +
                "                \"create_local_time\": 1584621291,\n" +
                "                \"is_hidden\": 0,\n" +
                "                \"ready_since_local_time\": 0,\n" +
                "                \"category\": \"Sandwish\",\n" +
                "                \"quantity\": 2,\n" +
                "                \"store_guid\": null,\n" +
                "                \"condiments\": []\n" +
                "            }\n" +
                "        ]\n" +
                "    },";
        return s;
    }


    public static String encryptToSHA(String info) {
        byte[] digesta = null;
        try {
            MessageDigest alga = MessageDigest.getInstance("SHA-1");
            alga.update(info.getBytes());
            digesta = alga.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String rs = byte2hex(digesta);
        return rs;
    }

    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }


    /**
     *
     * @param strNotification
     *  format: ##["command", "poiejlksjdfpasoidjpwie"]
     * @return
     */
    public String getNotifyData(String strNotification)
    {
        String s = strNotification;
        int nStart = s.indexOf("[");
        if (nStart <0) return s;
        s = s.substring(nStart);
        try {
            JSONArray ar = new JSONArray(s);
            if (ar.length()<=1) return s;
            String command = ar.getString(0);
            String base64 = ar.getString(1);
            String data = new String(Base64.decode(base64.getBytes(), Base64.DEFAULT));
            return data;
        }
        catch (Exception e)
        {

        }
        return s;

    }

    public class BackOfficeWebSocketClient extends WebSocketClient {
        String m_strStoreGuidConnected = ""; //record the what store guid has been send
        boolean m_bStopMe = false;

        public void setConnectedStoreGuid(String storeGuid)
        {
            m_strStoreGuidConnected = storeGuid;
        }
        public String getConnectedStoreGuid()
        {
            return m_strStoreGuidConnected;
        }

        public BackOfficeWebSocketClient(URI serverUri, Draft draft) {
            super(serverUri, draft);
        }

        public BackOfficeWebSocketClient(URI serverURI) {
            super(serverURI);
        }

        final int RECEIVED_NOTIFY = 1;
        Handler m_handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RECEIVED_NOTIFY: {
                        KDSBackofficeNotification.this.onNotification((String) msg.obj);
                    }
                    break;
                    default:
                        break;
                }

            }
        };

        public void setStopMe(boolean bStop) {
            m_bStopMe = bStop;
        }



        @Override
        public void onOpen(ServerHandshake serverHandshake) {

            //sendStoreGuid();
            log2File(TAG + KDSLog._FUNCLINE_() + "BackOfficeWebSocketClient: onOpen: " + serverHandshake.toString());
        }

        @Override
        public void onMessage(String s) {
            if (s.indexOf("Connected")>=0)
                sendStoreGuid();
            else if (s.indexOf("error")>=0)
            {
                this.close();
            }
            else {
                log2File(TAG + KDSLog._FUNCLINE_() + "BackOfficeWebSocketClient: onMessage: \n" + s);
                Message m = new Message();
                m.what = RECEIVED_NOTIFY;
                m.obj = s;
                m_handler.sendMessage(m);
                //TableTracker.this.onNotification(s);
            }


        }


        @Override
        public void onClose(int i, String s, boolean b) {
            log2File(TAG + KDSLog._FUNCLINE_() + "BackOfficeWebSocketClient: onClose:" + s);
            this.setConnectedStoreGuid("");
            //I use mainactivity timer to check connection.
//            if (!m_bStopMe) {
//                log2File(TAG + KDSLog._FUNCLINE_() + "BackOfficeWebSocketClient: onClose: start to connect again");
//                //TableTracker.this.connectNotification();
//                try {
//                    Thread.sleep(5000);
//                } catch (Exception e) {
//                    KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
//                }
//                Message msg = new Message();
//                msg.what = CONNECT_WEBSOCKET;
//                KDSBackofficeNotification.this.sendMessage(msg);
//            }
        }

        @Override
        public void onError(Exception e) {
            log2File(TAG + KDSLog._FUNCLINE_() + "BackOfficeWebSocketClient: onError:" + e.toString());
            e.printStackTrace();
        }

        /**
         * Send ["subscribe", data], where data is
         * {"now":current_timestamp,"sig":sha1(timestamp + store_guid)}
         *
         * @param strStoreGuid
         */
        public void sendStoreGuid(String strStoreGuid) {
            Date dt = new Date();
            long timeStamp = dt.getTime()/1000;
            String sha1 = Long.toString(timeStamp) + strStoreGuid;
            sha1 = encryptToSHA(sha1);

            String s = String.format("[\"subscribe\", " +
                                            "{\"now\":%d, " +
                                              "\"sig\":\"%s\"} ]", timeStamp,sha1);
            try {
                this.send(s);
            }catch (Exception e)
            {
                return;
            }
            setConnectedStoreGuid(strStoreGuid);
        }

        private void sendStoreGuid() {
            //if (m_strStoreGuidSend.equals(Activation.getStoreGuid()))
            //    return;
            sendStoreGuid(Activation.getStoreGuid());
        }
    }
}
