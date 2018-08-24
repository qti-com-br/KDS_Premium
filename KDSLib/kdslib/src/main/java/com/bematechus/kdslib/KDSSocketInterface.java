/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//package pckds;
package com.bematechus.kdslib;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 *
 * @author David.Wong
 * All async channel use this as the base
 */
public interface  KDSSocketInterface {
    //common functions
    public boolean interface_addToSocketManager(KDSSocketManager manager);
    public boolean interface_isUDP();
    public boolean interface_isTCPListen();
    public boolean interface_isTCPClient();
    //UDP functions
    public void interface_OnUDPRead(DatagramChannel channel);
    public void interface_OnUDPWrite(DatagramChannel channel);
    //TCP functions
    public KDSSocketInterface interface_OnTCPServerAccept(ServerSocketChannel channel);
    public void interface_OnTCPClientWrite(SocketChannel channel);
    public void interface_OnTCPClientRead(SocketChannel channel);
    public void interface_OnTCPClientConnected(SocketChannel channel);
    public void interface_OnSockFreeTime();
    public void interface_OnSocketDisconnected(SocketChannel channel);

}
