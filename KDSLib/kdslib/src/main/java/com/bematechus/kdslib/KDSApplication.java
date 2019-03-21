package com.bematechus.kdslib;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import org.acra.*;
import org.acra.annotation.*;


/**
 * Use this to save the context.
 */
@AcraMailSender(mailTo = "david.wong@bematechus.com")
@AcraCore(buildConfigClass = BuildConfig.class)
public class KDSApplication extends Application {

    public final String TAG = "KDSApplication";
    private static Context instance;
    private static Thread.UncaughtExceptionHandler m_osUncaughtExceptionHandler = null;
    @Override
    public void onCreate() {
        super.onCreate();
        //debug memory
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        ///////////////////////////////////////////
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

    static public boolean isRouterApp()
    {
        if (getContext() == null) return false;
        String s = getContext().getString(R.string.app_name);

        s = s.toUpperCase();
        return (s.indexOf("ROUTER")>=0);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }

}