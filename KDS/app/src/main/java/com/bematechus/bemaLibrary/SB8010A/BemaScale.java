//package com.bematechus.bemaLibrary.SB8010A;
//
//import android.util.Log;
//
//import com.bematechus.bemaUtils.CommunicationException;
//import com.bematechus.bemaUtils.PortInfo;
//import com.bematechus.bemaUtils.WatchDog;
//
//import java.io.IOException;
//import java.util.Arrays;
//
///**
// * Created by long.jiao on 7/16/2015.
// */
//public class BemaScale {
//    private static String TAG = "BemaScale";
//    static public final int OK = 0;
//    static public final int GENERIC_ERROR = -100;
//    public static final byte[] INIT = {0x05}; // initiate communication
////    public static final byte[] REQ = {0x12}; // request of weight data of type-1
//    public static final byte[] REQ = {0x57,0x0D}; // request of weight data
//    public static final byte[] ZERO = {0x38,0x38,0x38,0x38}; // request of weight data
//    private static final int ScaleDataLength = 15;
//
//    private PortInfo portInfo;
//
//    public SerialPort getPort() {
//        return port;
//    }
//
//    public void setPort(SerialPort port) {
//        this.port = port;
//    }
//
//    private SerialPort port;
//    public BemaScale(PortInfo portInfo){
//        this.portInfo = portInfo;
//    }
//
//    static public PortInfo scalePortInfo() {
//        PortInfo port = new PortInfo();
//        port.setPortName("COM2");
//        port.setBaudRate(9600);
//        port.setDataBits(7);
//        port.setParity(PortInfo.Parity.Even);
//        port.setStopBits(1);
//        port.setFlow(PortInfo.FlowControl.NoFlowControl);
//        return port;
//    }
//
//    public synchronized int open ()
//    {
//        if ( portInfo != null) {
//            try {
//                if (port != null)
//                    port.close();
//
//                port = new SerialPort();
//
//                if (port.open(portInfo)) {
//                    return getStatus();
//                }
//
//
//            } catch (CommunicationException ex) {
//
//                Log.d(TAG, ex.getMessage());
//                return ex.getErr().getValue();
//            }
//        }
//        else
//        {
//            return CommunicationException.ErrorCode.ServiceNotInitialized.getValue();
//        }
//        return GENERIC_ERROR;
//    }
//
//    public synchronized int open (PortInfo info)
//    {
//        portInfo = new PortInfo(info);
//        return open();
//    }
//
//
//    public synchronized int close ()
//    {
//        try
//        {
//            if ( port != null)
//                port.close();
//
//        }
//        catch (CommunicationException Ex)
//        {
//            return Ex.getErr().getValue();
//        }
//
//        return OK;
//    }
//
//    public synchronized int write (final byte [] data)
//    {
//        int result = GENERIC_ERROR;
//        if ( port != null && data != null)
//        {
//            try
//            {
//                result = port.write(data.clone());
//
//            }
//            catch (CommunicationException ex)
//            {
//                Log.d(TAG, ex.getMessage());
//
//            }
//        }
//        return result;
//    }
//
//    public synchronized int getStatus(){
//        if ( port != null)
//            try {
//                if (port.write(REQ) > 0) {
//                    port.setReadTimeout(2000);
//                    byte[] bytes = new byte[ScaleDataLength];
//                    int bytesRead = 0;
//                    WatchDog wd = new WatchDog();
//                    wd.Start((long) port.getReadTimeout());
//                    while (bytesRead < ScaleDataLength && wd.isTimeOut() == false) {
//                        int ret = port.read(bytes, bytesRead, ScaleDataLength - bytesRead);
//                        if (ret < 0)
//                            break;
//                        bytesRead += ret;
//                    }
//                    return Integer.parseInt(new String(Arrays.copyOfRange(bytes,11,13)));
//                    //ToDo: Toast different error if necessary
//                }
//            } catch (CommunicationException ex) {
//                Log.d(TAG, ex.getMessage());
//            }
//        return -1;
//    }
//
//    public synchronized String readScale(){
//        if ( port != null)
//            try {
//                if (port.write(REQ) > 0) {
//                    Thread.sleep(200);
//                    if (port.getInputStream().available() == 0)
//                        return "0.00";
//                    port.setReadTimeout(2000);
//                    byte[] bytes = new byte[ScaleDataLength];
//                    int bytesRead = 0;
//                    WatchDog wd = new WatchDog();
//                    wd.Start((long) port.getReadTimeout());
//                    while (bytesRead < ScaleDataLength && wd.isTimeOut() == false) {
//                        int ret = port.read(bytes, bytesRead, ScaleDataLength - bytesRead);
//                        if (ret < 0)
//                            break;
//                        bytesRead += ret;
//                    }
////                    return Arrays.toString(bytes);
//                    // check status
//                    if (bytes[11] == 0x30 && bytes[12] == 0x30)
//                        return covertBytesToScale(bytes);
//                    //ToDo: Toast different error if necessary
//                }
//            } catch (CommunicationException ex) {
//                Log.d(TAG, ex.getMessage());
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        return "0.00";
//    }
//
//
//    private String covertBytesToScale(byte[] bytes) {
//        StringBuilder sb = new StringBuilder();
//        sb.append(new String(bytes).substring(1,6));
//        return sb.toString().replaceAll("^0+(?!$)", ""); // remove leading 0
//    }
//
//}
//
