package com.bematechus.kdslib;

/**
 * Created by David.Wong on 2019/11/18.
 * Rev:
 */
public interface StationAnnounceEvents {
    void onReceivedStationAnnounce(KDSStationIP stationReceived);//String stationID, String ip, String port, String mac);
}
