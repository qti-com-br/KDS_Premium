package com.bematechus.kdslib;

/**
 * Created by David.Wong on 2019/11/19.
 * Use it to call KDS functions
 * Rev:
 */
public interface KDSCallback {
    public void call_setStationAnnounceEventsReceiver(StationAnnounceEvents receiver);
    public void call_broadcastRequireStationsUDP();
    public String call_getStationID();
}
