package com.bematechus.kds;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

//import com.bematechus.bemaLibrary.CodePageCommand;
//import com.bematechus.bemaUtils.CommunicationException;

import com.bematechus.bemaLibrary.BemaPrinter;
//import com.bematechus.bemaLibrary.CodePageCommand;
import com.bematechus.bemaLibrary.CodePageCommand;
import com.bematechus.bemaUtils.CommunicationException;

import java.util.HashMap;
import java.util.Iterator;

import static com.bematechus.kds.MainActivity.ACTION_USB_PERMISSION;

public class Printer extends Activity {

    private UsbManager mManager;
    private Context mContext;
    private boolean isOpen;
    private static UsbDeviceConnection mConnection;
    private UsbInterface mInterface;
    private static UsbEndpoint mEndpoint;
    private Exception mLastError;

    public enum PRINTER_STATUS {
        OK,
        NOT_FOUND,
        NO_PERMISSION,
        HAS_PERMISSION,
        NO_COMM,
        UNKNOWN_ERROR
    }

    private static final int TIMEOUT = 3000;

    // LR1100 and LR2000 has the same Vendor Id and Product ID
    public static final int LR2000_VID = 0x0fe6;
    public static final int LR2000_PID = 0x811e;

    public static final int TML90_VID = 0x04B8;
    public static final int TML90_PID = 0x0202;

    public static PRINTER_STATUS status = PRINTER_STATUS.NOT_FOUND;

    private UsbDevice mDevice = null;

    public Printer(Context context, UsbManager manager) {
        mManager = manager;
        mContext = context;

        if(hasPermission() == PRINTER_STATUS.HAS_PERMISSION) {
            findPrinter();
        }
    }


    private PRINTER_STATUS hasPermission() {
        try {
            HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

            Log.d("##Printer", "deviceList count: " + deviceList.size());

            while (deviceIterator.hasNext()) {

                UsbDevice device = deviceIterator.next();

//                String manufac = device.getManufacturerName();
//                String model = device.getProductName();
                int vendorId = device.getVendorId();
                int productId = device.getProductId();

                Log.d("##Printer", "vendorId:" + vendorId +
                        " | productId:" + productId);

                if ((vendorId == LR2000_VID && productId == LR2000_PID) ||
                        (vendorId == TML90_VID && productId == TML90_PID)) {

                    mDevice = device;
                    status = PRINTER_STATUS.NO_PERMISSION;

                    if (mManager.hasPermission(device)) {
                        status = PRINTER_STATUS.HAS_PERMISSION;

                    } else {
                        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0,
                                new Intent(ACTION_USB_PERMISSION), 0);
                        mManager.requestPermission(device, pi);

                    }
                    return status;
                }
            }
        } catch (Exception e) {
            mLastError = e;
            e.printStackTrace();
            status = PRINTER_STATUS.NO_PERMISSION;
            return status;
        }
        status = PRINTER_STATUS.NO_PERMISSION;
        return status;
    }

    // @return true|false whether the device is found
    public PRINTER_STATUS findPrinter() {
        try {
             if(mDevice != null) {

                 mConnection = mManager.openDevice(mDevice);

                 if (mDevice.getInterfaceCount() > 0) {
                     mInterface = mDevice.getInterface(0);
                 }

                 if (mConnection != null) {
                     mConnection.claimInterface(mInterface, true);
                 }

                 int numEndPoints = mInterface.getEndpointCount();
                 for (int i = 0; i < numEndPoints; i++) {
                     UsbEndpoint endpoint = mInterface.getEndpoint(i);

                     if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                         if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                             mEndpoint = endpoint;
                         }
                     }
                 }

                 return this.open();
             } else {
                 return PRINTER_STATUS.NOT_FOUND;
             }
        } catch (Exception e) {
            mLastError = e;
            e.printStackTrace();
            status = PRINTER_STATUS.UNKNOWN_ERROR;
            return status;
        }
    }

    public boolean IsOpen() {
        return isOpen;
    }

    public Exception getLastError() {
        return mLastError;
    }

    // @return whether or not the data was successfully sent
    public PRINTER_STATUS open() {
        status = PRINTER_STATUS.NOT_FOUND;
        try {
            byte[] data = {27, 64};
            if(write(data, "open")) {
                status = PRINTER_STATUS.OK;
            } else {
                status = PRINTER_STATUS.NO_COMM;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public static boolean partialCut() {
        byte[] data = {27, 105};
        return write(data, "partialCut");
    }

    public static boolean printOrder(byte[] data) {
        if(write(data, "printOrder")) {
            return partialCut();
        }
        return false;
    }

    public static boolean write(byte[] data, String fun) {
        int result = -999;
        if(mConnection != null) {
            result = mConnection.bulkTransfer(mEndpoint, data, data.length, TIMEOUT);
        }
        Log.d("##Printer",fun + " -> data: " + data.length + "result: " + result);
        return result == data.length;
    }

    public void close() {
        isOpen = false;
    }

    /**
     * As the usb printer goes to this class,
     *  David add this function for parsing printer command.
     * @param data
     * @return
     */
    public static boolean printerOrder(String data, KDSPrinter kdsPrinter)
    {
        setCodePage(kdsPrinter.getCodePage());

        String willPrint = "";
        char ch = 0;
        for (int i=0; i< data.length(); i++)
        {
            ch = data.charAt(i);
            if (KDSPrinter.isPrinterCommandChar(ch))
            {
                if (!willPrint.isEmpty())
                    printText(willPrint, kdsPrinter);
                willPrint = "";
                writePrinterCommandByCommandChar(ch, kdsPrinter);
            }
            else
            {
                willPrint += ch;
            }
        }

        if (!willPrint.isEmpty())
            printText(willPrint, kdsPrinter);
        return true;
    }

    static public boolean printText (String text, KDSPrinter kdsPrinter)
    {
        byte[] bytes = CodePageCommand.convertFromUnicode(kdsPrinter.getCodePage(), text);
        return write(bytes, "printText");

    }

    static private void writePrinterCommandByCommandChar(char ch,KDSPrinter kdsPrinter)
    {
        switch (ch) {
            case KDSPrinter.CMD_START_BOLD://) //fix a bug. Old code is cmd_end_bold. see kpp1-146
            {
                write(KDSPrinter.LR2000_START_BOLD, "printerCmd");
            }
            break;
            case KDSPrinter.CMD_END_BOLD:
            {
                write(KDSPrinter.LR2000_END_BOLD, "printerCmd");
            }
            break;
            case KDSPrinter.CMD_PAPER_CUT:
            {
                write(KDSPrinter.LR2000_PAPER_CUT, "printerCmd");
            }
            break;
            case KDSPrinter.CMD_START_DBLW:
            {
                write(KDSPrinter.LR2000_START_DBLW, "printerCmd");
            }
            break;
            case KDSPrinter.CMD_START_DBLH:
            {
                write(KDSPrinter.LR2000_START_DBLH, "printerCmd");
            }
            break;
            case KDSPrinter.CMD_START_DBLWH:
            {
                write(KDSPrinter.LR2000_START_DBLWH, "printerCmd");
            }
            break;
            case KDSPrinter.CMD_END_DBLW:
            case KDSPrinter.CMD_END_DBLH:
            case KDSPrinter.CMD_END_DBLWH:
            {
                write(KDSPrinter.LR2000_END_DBLWH, "printerCmd");
            }
            break;
            case KDSPrinter.CMD_START_REVERSE:
            {
                write(KDSPrinter.LR2000_START_REVERSE, "printerCmd");
            }
            break;
            case KDSPrinter.CMD_END_REVERSE:
            {
                write(KDSPrinter.LR2000_END_REVERSE, "printerCmd");
            }
            break;
            case KDSPrinter.CMD_PRINT_LOGO:
            {
                if (kdsPrinter!= null)
                    kdsPrinter.sendLogoDataToPrinter(true);
            }
            break;
            default:
            {
            }
            break;
        }
    }
    static public boolean setCodePage(BemaPrinter.CodePage codePage) {

        return write(new CodePageCommand(codePage.getValue()).getBytes(), "setCodePage");

    }




}
