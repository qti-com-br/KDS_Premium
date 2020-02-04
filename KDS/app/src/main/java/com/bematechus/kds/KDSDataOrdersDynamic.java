package com.bematechus.kds;

import android.util.Log;

import com.bematechus.kdslib.KDSDataItems;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/3/1 0001.
 */
public class KDSDataOrdersDynamic extends KDSDataOrders {

    static final String TAG = "KDSDataOrdersDynamic";
    private KDSUser.USER m_userID = KDSUser.USER.USER_A;

    public void setUserID(KDSUser.USER userID)
    {
        m_userID = userID;
    }
    public KDSDBCurrent getCurrentDB()
    {
        return KDSGlobalVariables.getKDS().getCurrentDB();
    }
    @Override
    public float getTotalQty()
    {
        return getCurrentDB().screenGetTotalQty(m_userID.ordinal());
    }
    public void copyDataPointer(KDSDataOrders orders)
    {
        this.clear();
        for (int i=0; i< orders.getCount(); i++)
            this.addComponent(orders.get(i));

    }

    @Override
    public KDSDataOrder getOrderByGUID(String orderGUID)
    {
       // Log.d(TAG, "getOrderByGUID="+ orderGUID);
        KDSDataOrder c = super.getOrderByGUID(orderGUID);
        if (c == null) return null;
        if (c.getItems().getCount()<=0) {
            KDSLog.d(TAG, KDSLog._FUNCLINE_()+ "getOrderByGUID="+ orderGUID);
            getCurrentDB().orderLoadData(c);
        }
        return c;
    }

    public KDSDataOrder getOrderByName(String orderName)
    {
        //Log.d(TAG, "getOrderByName="+ orderName);
        KDSDataOrder c = super.getOrderByName(orderName);
        if (c == null) return null;
        if (c.getItems().getCount()<=0) {
            KDSLog.d(TAG, KDSLog._FUNCLINE_()+"getOrderByName="+ orderName);
            getCurrentDB().orderLoadData(c);
        }
        return c;
    }

    public KDSDataOrder getOrderByIndexWithoutLoadData(int nIndex)
    {
        KDSDataOrder c = super.get(nIndex);

        return c;
    }


    public KDSDataItems getItems(String orderGuid)
    {
        KDSDataOrder c =  getOrderByGUID(orderGuid);
        if (c == null) return null;
        return c.getItems();

    }
    @Override
    public KDSDataOrder get(int nIndex)
    {
        //Log.d(TAG, "get="+ KDSUtil.convertIntToString(nIndex));
        KDSDataOrder c = super.get(nIndex);
        if (c == null ) return null;

        if (c.getItems().getCount()<=0) {
            KDSLog.d(TAG, KDSLog._FUNCLINE_()+"get="+ KDSUtil.convertIntToString(nIndex));
            getCurrentDB().orderLoadData(c);
            if (c.getItems().getCount() <=0) {
                KDSLog.e(TAG, KDSLog._FUNCLINE_() + "loaditems count=" + KDSUtil.convertIntToString(c.getItems().getCount()));
            }
        }
        return c;

    }
    @Override
    public ArrayList<String> getAllOrderGUID()
    {
        ArrayList<String> ar = new ArrayList<String>();
        int ncount = m_arComponents.size();
        for (int i=0; i< ncount; i++)
        {
            KDSDataOrder order =  super.get(i);
            ar.add(order.getGUID());

        }
        return ar;
    }


    /**
     * call this function after get new order
     * @param order
     */
    static public void resetNewOrderItemsForSaveMemory(KDSDataOrder order)
    {
        order.setTag(order.getItems().getCount());
        order.getItems().clear();

    }
}
