package com.bematechus.kds;

import android.util.Log;

/**
 *
 * Use this thread to print data to USB port printer.
 */
public class UsbPrinterThread {

    static String TAG = "USBPrinterThread";
    String mData = "";
    KDSPrinter m_kdsPrinter = null;
    static Object m_locker = new Object();

    public static void start(String data)
    {
        UsbPrinterThread t = new UsbPrinterThread();
        t.mData = data;
        t.startThread();

    }

    /**
     *
     * @param kdsPrinter
     *  Use it to print logo.
     * @param data
     */
    public static void start(KDSPrinter kdsPrinter, String data)
    {
        UsbPrinterThread t = new UsbPrinterThread();
        t.mData = data;
        t.m_kdsPrinter = kdsPrinter;
        t.startThread();

    }

    public void startThread()
    {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    //if (true)//TEST
                    if (Printer.status == Printer.PRINTER_STATUS.OK)
                    {
                        Log.d(TAG, "--->>>USB Printer start printing thread");
                        String sOrder = "";
                        sOrder = mData;
                        try {
                            synchronized (m_locker) {
                                //Printer.printOrder(sOrder.getBytes());
                                Printer.printerOrder(sOrder, m_kdsPrinter);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Log.d(TAG, "<<<USB Printer exit printing thread");
                        break;
                    }
                    else {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {

                        }
                    }
                }
            }

        };
        thread.setName("USBPrn");
        thread.start();
    }

}
