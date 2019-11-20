package com.bematechus.kdslib;

import android.widget.TextView;

/**
 * Created by David.Wong on 2019/11/19.
 * Use it to call KDS functions
 * Rev:
 */
public interface KDSCallback {
    public void call_setStationAnnounceEventsReceiver(StationAnnounceEvents receiver);
    public void call_broadcastRequireStationsUDP();
    public String call_getStationID();
    public void call_removeEventReceiver(KDSBase.KDSEvents receiver);
    public void call_setEventReceiver(KDSBase.KDSEvents receiver);
    public int call_retrieveConfigFromStation(String stationID, TextView txtInfo);
    public String call_getLocalMacAddress();
    public String call_getBackupRouterPort(); //just for router
    public void call_broadcastShowStationID();
    public void call_udpAskRelations(String stationID);
    public void call_broadcastRelations(String relationsData);
    public KDSStationActived call_findActivedStationByID(String stationID);
    public int call_findActivedStationCountByID(String stationID);
    public void call_broadcastStationsRelations();


}
