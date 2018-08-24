package com.bematechus.kds;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.bematechus.bemaLibrary.BemaPrinter;
import com.bematechus.bemaUtils.PortInfo;
import com.bematechus.kdslib.KDSLog;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 *
 */
public class KDSPager {


    final int COM_PORT = 4;
    final int COM_BAUDRATE = 9600;

    private BemaPrinter m_bemaPrinter = null;// new BemaPrinter();


    public KDSPager()
    {
        initBemaPrinter();
    }

    /**
     * don't call showmsg function in this.
     * This was called by thread, maybe. The showmsg cause crush!! while thread call it.
     * @return
     */
    public boolean open(int nComPortNumber, int nBaudRate)
    {
        PortInfo portInfo = getPortInfo(nComPortNumber, nBaudRate);

        if (m_bemaPrinter.isOpened())
            m_bemaPrinter.close();
        int nResult = m_bemaPrinter.open(portInfo);
        boolean bOpen = (nResult == BemaPrinter.OK);
        return bOpen;
    }
    public boolean close()
    {
        return (m_bemaPrinter.close() == BemaPrinter.OK);
    }
    public int write(String s)
    {
        String willPrint = s;
         return   m_bemaPrinter.printText_pager(willPrint);
    }
    public int write(byte[] buffer)
    {
        return m_bemaPrinter.write(buffer);
    }

    public int read(byte[] buffer)
    {
        return m_bemaPrinter.read(buffer);
    }

    public boolean page (int i)
    {
        String station = String.valueOf(i);

        return page(station);

    }

    private String makeCommand(String pagerID)
    {
        String s = "";
        s = "CPG,"+pagerID+",67,4\n";
        return s;
    }
    public boolean page(String pagerID)
    {
        //To transmit to a coaster (or guest pager), use the command format CPG,23,67,4.

        //In the example above, we paged coaster 23, mode is 43h, at power level 4.
        if (!open(COM_PORT,COM_BAUDRATE))
            return false;
        //write("CPG,"+pagerID+",67,4\n");
        if (write(makeCommand(pagerID)) <=0)
            return false;

        byte[] buffer = new byte[3];
        read(buffer);
        close();

        //create a byte [] to compare read back byte, if read back byte = CPG, then send to pager is OK
        byte [] compare = new byte[]{(byte)0x43, 0x50, 0x47};
        if(!Arrays.equals(buffer, compare))
        {
            // Do something to tell user send fail
            KDSLog.i("Warning", "send fail");
            return false;
        }
        return true;
    }

    private void initBemaPrinter()
    {

        //m_PermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        m_bemaPrinter = new BemaPrinter(null, null, null);
    }

    /**
     *
     * @param ncomNumber
     * 1,2,3,4
     * @param baudRate
     *  9600,19200,38400,57600,115200
     * @return
     */
    private PortInfo getPortInfo(int ncomNumber, int baudRate)
    {

        String comNumber = String.format("%d", ncomNumber);

        PortInfo portInfo = null;// new PortInfo();
        portInfo = new PortInfo("COM"+comNumber, ncomNumber);

        int baudrate = baudRate;//
        portInfo.setBaudRate(baudRate);
        return portInfo;


    }

}
