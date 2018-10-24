package com.bematechus.kdsrouter;

import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSPosNotificationFactory;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSToStation;
import com.bematechus.kdslib.KDSToStations;

import java.util.ArrayList;

/**
 * Created by David.Wong on 2018/7/31.
 * Rev:
 */
public class RouterAcks {

    ArrayList<RouterAck> m_arAcks = new ArrayList<>();
    Object m_locker = new Object();
    public ArrayList<RouterAck> getArray()
    {
        return m_arAcks;
    }

    public RouterAck add(KDSDataOrder order, String orderXml, String originalFileName)
    {
        RouterAck ack = new RouterAck();
        ack.setXmlText(orderXml);
        ack.setOrderName(order.getOrderName());
        ack.setOriginalFileName(originalFileName);
        setTargetStations(ack, order);
        synchronized (m_locker) {
            m_arAcks.add(ack);
        }
        return ack;


    }

    private void setTargetStations(RouterAck ack,  KDSDataOrder order)
    {
        ArrayList<KDSToStation> ar = getOrderTargetStations(order);
        ack.getTargetStations().clear();
        ack.getTargetStations().addAll(ar);
    }

    private ArrayList<KDSToStation> getOrderTargetStations(KDSDataOrder order)
    {
        return KDSDataOrder.getOrderTargetStations(order);

//
//        ArrayList<KDSToStation> ar = new ArrayList<>();
//
//
//        for (int i=0; i< order.getItems().getCount(); i++)
//        {
//            KDSDataItem item = order.getItems().getItem(i);
//            KDSToStations toStations = item.getToStations();
//            for (int j=0; j< toStations.getCount(); j++)
//            {
//                KDSToStation toStation = toStations.getToStation(j);
//                if (isExistedInArrary(ar, toStation))
//                    continue;
//                ar.add(toStation);
//            }
//        }
//        return ar;
    }

//    private boolean isExistedInArrary(ArrayList<KDSToStation> ar, KDSToStation toStation)
//    {
//        for (int i=0; i< ar.size(); i++)
//        {
//            if ( ar.get(i).getPrimaryStation().equals(toStation.getPrimaryStation()) &&
//                    ar.get(i).getSlaveStation().equals(toStation.getSlaveStation()) )
//                 return true;
//        }
//        return false;
//    }

    public RouterAck findAck(String orderName)
    {
        synchronized (m_locker) {
            for (int i = 0; i < m_arAcks.size(); i++) {
                if (m_arAcks.get(i).getOrderName().equals(orderName))
                    return m_arAcks.get(i);
            }
            return null;
        }
    }

    /**
     * 2.0.15
     * @param fromStation
     * @param orderID
     * @param errorCode
     * @param arActiveStations
     */
    public boolean receivedAckFromStation(String fromStation, String orderID,String errorCode, ArrayList<KDSStationIP> arActiveStations)
    {
        //checkTimeoutAck();
        RouterAck ack = findAck(orderID);
        if (ack == null) return false;
        return (ack.setAckResultAccordingToErrorCode(fromStation, errorCode, arActiveStations));



    }


    final int MAX_COUNT = 2;
    public ArrayList<RouterAck> checkTimeoutAck()
    {
        ArrayList<RouterAck> ar = new ArrayList<>();

        synchronized (m_locker) {
            int ncount = m_arAcks.size();
            for (int i = ncount - 1; i >= 0; i--) {
                if (m_arAcks.get(i).isTimeout()) {
                    ar.add(m_arAcks.get(i));
                    m_arAcks.remove(i);
                    if (ar.size() >= MAX_COUNT)
                        return ar;
                }
            }
            return ar;
        }
    }


}
