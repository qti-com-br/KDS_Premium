/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bematechus.kdslib;

import java.nio.ByteBuffer;

/**
 *
 * @author David.Wong
 */
public interface KDSSocketEventReceiver {


    //TCP event
    public void sockevent_onReceiveData(KDSSocketInterface sock, String remoteIP, ByteBuffer buffer, int nLength);
    public void sockevent_onTCPConnected(KDSSocketInterface sock);

    public void sockevent_onTCPDisconnected(KDSSocketInterface sock);
    public void sockevent_onTCPReceiveXml(KDSSocketInterface sock, String xmlData);
    public void sockevent_onTCPAccept(KDSSocketInterface sock, Object sockClient);

    //UDP
    public void sockevent_onUDPReceiveXml(KDSSocketInterface sock, String xmlData);

    //smb
    public void smbevent_onSMBReceiveXml(KDSSMBDataSource smb,String smbFileName, String xmlData);

    public void announce_lost_pulse(String stationID, String stationIP);
    public void one_socket_information(String strInformation);
    public void sockevent_onWroteDataDone(KDSSocketInterface sock, String remoteIP, int nLength);

    
}
