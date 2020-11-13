package com.bematechus.kdsrouter;

import android.os.Handler;
import android.os.Message;

import com.bematechus.kdslib.Activation;
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
    static final String BACKOFFICE_URI = "ws://dev.kdsgo.com:9205";//

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
        void onBackofficeNotifyEvent(String evt);
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
            //String strUrl = String.format("ws://%s:8000/api/v2/websocket", "logiccontrols.com");
            String strUrl = BACKOFFICE_URI;
            URI url = new URI(strUrl);
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
                if (m_webSocket.isConnecting()) return true;
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
        if (m_webSocket.isConnecting()) return true;
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
     * @param strNotification
     * @return
     */
    public boolean onNotification(String strNotification)
    {
        if (m_receiver == null) return false;
        if (strNotification.indexOf(MSG_KEY_MESSAGE)>=0)
        {

        }
        else if (strNotification.indexOf(MSG_KEY_SYNC) >=0)
        {
            String ar[] = new String[]{API_EVENT_ORDER_CREATED,API_EVENT_ORDER_UPDATED ,API_EVENT_ORDER_VOIDED,
                                       API_EVENT_ITEM_CREATED, API_EVENT_ITEM_UPDATED, API_EVENT_ITEM_VOIDED};

            for (int i=0; i< ar.length; i++)
            {
                if (strNotification.indexOf(ar[i]) >=0) {
                    m_receiver.onBackofficeNotifyEvent(ar[i]);
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
     * @param json
     * @return
     */
    static KDSDataOrder parseFirebaseOrderJson(JSONObject json)
    {
        try {

            KDSDataOrder order = new KDSDataOrder();

            order.setGUID(json.getString("guid"));
            order.setOrderName(json.getString("external_id"));

            order.setStartTime(json.getLong("create_time"));
            order.setCustomMsg(json.getString("user_info"));
            order.setToTable(json.getString("guest_table"));
            order.setFromPOSNumber(json.getString("pos_terminal"));
            order.setDestination(json.getString("destination"));
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

                KDSDataItem item = parseItemJson(order.getGUID(), jsonItem);
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
     *
     * @param strData
     *  It can contain multiple orders in it.
     * @return
     */
    static KDSDataOrders parseFirebaseJson(String strData)
    {
        try {
            KDSDataOrders orders = new KDSDataOrders();
            JSONArray ar = new JSONArray(strData);
            if (ar == null)
                return null;
            if (ar.length() <= 0) return null;
            for (int i=0; i< ar.length(); i++) {
                JSONObject json = (JSONObject) ar.get(i);//order data.
                KDSDataOrder order = parseFirebaseOrderJson(json);
                if (order != null)
                    orders.addOrderWithoutSort(order);
            }
            return orders;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(), e);
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
     * @param orderGuid
     * @param jsonItem
     * @return
     */
    static private KDSDataItem parseItemJson(String orderGuid, JSONObject jsonItem)
    {
        try {
            KDSDataItem item = new KDSDataItem();
            item.setOrderGUID(orderGuid);
            item.setLocalBumped(false);
            item.setBuildCard(jsonItem.getString("build_card"));
            item.setCategory(jsonItem.getString("category"));
            item.setItemName(jsonItem.getString("external_id"));
            if (!ENABLE_DEBUG) {
                if (jsonItem.has("device_id"))
                    item.setToStationsString(jsonItem.getString("device_id")); //get to stations from "device_id"
                //item.setToStationsString("4"); //debug firebase

            }
            item.setGUID(jsonItem.getString("guid"));
            item.setDescription(jsonItem.getString("name"));
            item.setItemBumpGuid(jsonItem.getString("item_bump_guid"));
            item.setQty(jsonItem.getInt("quantity"));
            String preModifiers = jsonItem.getString("pre_modifier");
            if (!isNullStringValue(preModifiers)) {
                KDSDataMessages messages = KDSDataMessages.parseString(preModifiers, item.getGUID(), KDSDataMessage.FOR_Item);
                item.setPreModifiers(messages);
            }
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
     *
     * @param itemGuid
     * @param jsonCondiment
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
            String condimentPremodifiers = jsonCondiment.getString("pre_modifier");
            if (!isNullStringValue(condimentPremodifiers)) {
                KDSDataMessages messages = KDSDataMessages.parseString(condimentPremodifiers, c.getGUID(), KDSDataMessage.FOR_Condiment);
                c.setMessages(messages);
            }

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
