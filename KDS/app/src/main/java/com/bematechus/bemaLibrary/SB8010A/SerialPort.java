package com.bematechus.bemaLibrary.SB8010A;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Thread;


import android.util.Log;

import com.bematechus.bemaUtils.CommunicationException;
import com.bematechus.bemaUtils.CommunicationPort;
import com.bematechus.bemaUtils.PortInfo;
import com.bematechus.bemaUtils.WatchDog;

/**
 * Created by b1107005 on 5/23/2015.
 */

public class SerialPort extends CommunicationPort {

    private static final String TAG = "SerialPort";

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd = null;
    private InputStream mFileInputStream = null;
    private OutputStream mFileOutputStream = null;

    private final String pathCOM1 = "/dev/ttyS0";//"/dev/ttymxc0";
    private final String pathCOM2 = "/dev/ttyS1";// "/dev/ttymxc1";
    private final String pathCOM3 = "/dev/ttyS2";//"/dev/ttymxc2";
    private final String pathCOM4 = "/dev/ttyS3";
    private final String pathCOM5 = "/dev/ttyS4";
    private final String pathCOM6 = "/dev/ttyS5";
    private final String pathCOM7 = "/dev/ttyS6";
    private final String pathCOM8 = "/dev/ttyS7";
    private final String pathCOM9 = "/dev/ttyS8";
    private final String pathCOM10 = "/dev/ttyS9";


    private int baudrate = 9600;
    private int databits = 8;
    private int parity = 0;
    private int stopbits = 1;
    private int flowctl = 0;
    private android_serialport_api.SerialPort nativePort = null;

    private String getPathFromPath(String portName)
    {
        String upper = portName.toUpperCase();
        String path = portName;


        if ( upper.equals("COM1"))
        {
            path =   pathCOM1;
        }
        else if (upper.equals("COM2"))
        {
            path = pathCOM2;
        }
        else if (upper.equals("COM3"))
        {
            path = pathCOM3;
        }
        else if (upper.equals("COM4"))
        {
            path = pathCOM4;
        }
        else if (upper.equals("COM5"))
        {
            path = pathCOM5;
        }
        else if (upper.equals("COM6"))
        {
            path = pathCOM6;
        }
        else if (upper.equals("COM7"))
        {
            path = pathCOM7;
        }
        else if (upper.equals("COM8"))
        {
            path = pathCOM8;
        }
        else if (upper.equals("COM9"))
        {
            path = pathCOM9;
        }
        else if (upper.equals("COM10"))
        {
            path = pathCOM10;
        }

        return path;
    }


//    private String getPathFromPath(String portName)
//    {
//        String upper = portName.toUpperCase();
//        String path = portName;
//
//
//        if ( upper.matches("COM1"))
//        {
//            path =   pathCOM1;
//        }
//        else if (upper.matches("COM2"))
//        {
//            path = pathCOM2;
//        }
//        else if (upper.matches("COM3"))
//        {
//            path = pathCOM3;
//        }
//        else if (upper.matches("COM4"))
//        {
//            path = pathCOM4;
//        }
//        else if (upper.matches("COM5"))
//        {
//            path = pathCOM5;
//        }
//        else if (upper.matches("COM6"))
//        {
//            path = pathCOM6;
//        }
//        else if (upper.matches("COM7"))
//        {
//            path = pathCOM7;
//        }
//        else if (upper.matches("COM8"))
//        {
//            path = pathCOM8;
//        }
//        else if (upper.matches("COM9"))
//        {
//            path = pathCOM9;
//        }
//        else if (upper.matches("COM10"))
//        {
//            path = pathCOM10;
//        }
//
//        return path;
//    }

    static public String runLinuxCmd(String command)
    {
        //command = "id -un";
        String result = "";
        try {
            Process p = Runtime.getRuntime().exec(command); // 10.83.50.111  m_strForNetAddress
            int status = p.waitFor();

            BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String str = "";//new String();
            String strInfo = "";
//读出所有信息并显示
            while ((str = buf.readLine()) != null) {
                str = str + "\r\n";
                strInfo+=str;
            }
            return strInfo;
        }
        catch (Exception e)
        {
            return e.getMessage();
        }

    }

    static public String runLinuxCmd2(String command)
    {

        String result = "";
        try {
            Process sh;
            sh = Runtime.getRuntime().exec("/system/bin/sh");
            String cmd = command + "\n"
                    + "exit\n";
            sh.getOutputStream().write(cmd.getBytes());
            if ((sh.waitFor() != 0) ) {
                return "error";
            }
        }
        catch (Exception e)
        {
            return e.getMessage();
        }

        return "";
    }

    private File initialize (String portName) throws SecurityException
    {

        File device = new File( getPathFromPath(portName));
        if (device.canRead())
        {
            Log.d(TAG, "can read");
        }
        if (device.canWrite())
        {
            Log.d(TAG, "can write");
        }
        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
				/* Missing read/write permission, trying to chmod the file */
      /*          Process su;
                su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    throw new SecurityException();
                }
                */
                String strR = runLinuxCmd("/system/bin/chmod 666 "+device.getAbsolutePath());
                if (!device.canRead() || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG+"Initialze", e.getMessage());

                throw new SecurityException();
            }
        }
        return device;

    }

    public boolean open(String portName,  int baud, int dataBits, int parity, int stopBits, int flow) throws CommunicationException {

        try {
            File device = initialize(portName);

            if ( nativePort != null)
            {
                nativePort.close();

            }
            nativePort = new android_serialport_api.SerialPort(device, baud, dataBits,
                    parity, stopBits, flow);


            mFileInputStream = nativePort.getInputStream();
            mFileOutputStream = nativePort.getOutputStream();
        }
        catch ( SecurityException ex)
        {

            Log.d(TAG, "Access Denied");
            throw new CommunicationException(ex.getMessage(), CommunicationException.ErrorCode.AccessDenied);

        }
        catch (Exception ex)
        {
            Log.d(TAG, "Connection Refused");
            throw new CommunicationException(ex.getMessage(), CommunicationException.ErrorCode.ConnectionRefused);
        }


        return true;
    }

    @Override
    public boolean open(PortInfo info) throws CommunicationException {

        readTimeout = info.getReadTimeout();

        return open(info.getPortName(), info.getBaudRate(), info.getDataBits(),
                info.getParity().getValue(), info.getStopBits(), info.getFlow().getValue());


    }

    @Override
    public void close() throws CommunicationException {
        if ( isOpen())
        {
            try {
                if (mFileInputStream != null) {
                    mFileInputStream.close();

                }
                if (mFileOutputStream != null) {
                    mFileOutputStream.close();

                }

            }
            catch (IOException ex)
            {
                throw new CommunicationException(ex.getMessage(), CommunicationException.ErrorCode.CloseError);
            }
            finally {
                if (nativePort != null)
                {
                    nativePort.close();
                    nativePort = null;
                }
                mFd = null;
                mFileOutputStream = null;
                mFileInputStream = null;
            }

        }

    }



    @Override
    public Integer write(byte[] data, int sizeToWrite) throws CommunicationException {
        if ( isOpen() == false ||  mFileOutputStream == null)
            throw new CommunicationException("Port Not initialized", CommunicationException.ErrorCode.PortNotAvailable);

        try {
            mFileOutputStream.write(data, 0, sizeToWrite);
        }
        catch (IOException ex)
        {
            throw new CommunicationException(ex.getMessage(), CommunicationException.ErrorCode.WriteError);

        }
        //file outputstream does not inform number of bytes written
        //assume , if successful that everything has been written
        return  sizeToWrite;
    }

    @Override
    public Integer read(byte[] data, int sizeToRead) throws CommunicationException {
        if ( isOpen() == false ||  mFileInputStream == null)
            throw new CommunicationException("Port Not initialized", CommunicationException.ErrorCode.PortNotAvailable);

        int bytesRead = 0;
        int ret = 0;
        try {
            Thread.sleep(200);
            if (mFileInputStream.available() == 0)
                return 0;
            WatchDog wd = new WatchDog();
            wd.Start(readTimeout.longValue());
            while ( bytesRead < sizeToRead) {
                ret = mFileInputStream.read(data, bytesRead, sizeToRead-bytesRead);
                if (wd.isTimeOut() || ret < 0)
                    break;
                bytesRead += ret;
                Thread.yield();
            }
        }
        catch (IOException ex)
        {
            CommunicationException e =  new CommunicationException(ex.getMessage(), CommunicationException.ErrorCode.ReadError);
            e.setDataTransmitted(bytesRead);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return  bytesRead;
    }

    @Override
    public boolean isOpen() {
        return nativePort != null;
    }




    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }


}

