package com.bematechus.kdslib;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */
import android.app.Application;
import android.content.Context;
import android.content.Intent;

/**
 * Use this to save the context.
 */
public class KDSApplication extends Application {

    public final String TAG = "KDSApplication";
    private static Context instance;
    private static Thread.UncaughtExceptionHandler m_osUncaughtExceptionHandler = null;
    @Override
    public void onCreate() {
        super.onCreate();

        instance = getApplicationContext();
        KDSLog.logAppStarted();
        m_osUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException (thread, e);
            }
        });
    }

    public static Context getContext() {
        return instance;
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        String strError = KDSLog.getStackTrace(e);
        KDSLog.e(TAG,strError);

        //send email
        //String appInfo = KDSLog.getAppNameAndVersion();
        //KDSEmail.sendTo(KDSLog.getAppName(), appInfo, strError);

        m_osUncaughtExceptionHandler.uncaughtException(thread, e);

        KDSLog.e(TAG,"\n************ KDS was exited with errors ************\n");



//        Intent intent = new Intent ();
//        intent.setAction ("com.mydomain.SEND_LOG"); // see step 5.
//        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
//        startActivity (intent);

        System.exit(0); // kill off the crashed app
    }
}