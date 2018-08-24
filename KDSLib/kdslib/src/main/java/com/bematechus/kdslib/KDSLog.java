package com.bematechus.kdslib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 *
 */
public class KDSLog {

    static final String TAG = "KDSLOG";
    static final String DEFAULT_LOG_FOLDER = "kdsdata/log";
    static ArrayList<Exception> m_arLoggedException = new ArrayList<>();

    /**
     * VERBOSE 类型调试信息，verbose啰嗦的意思
     DEBUG 类型调试信息, debug调试信息
     INFO  类型调试信息, 一般提示性的消息information
     WARN  类型调试信息，warning警告类型信息
     ERROR 类型调试信息，错误信息
     */
    public enum LogLevel
    {
        None,
        Basic, //ERROR, WARN,
        Detail, //INFO,DEBUG,VERBOSE

    }
    static public LogLevel m_logLevel = LogLevel.Detail;

    public KDSLog() {
    }

    static public void setLogLevel(LogLevel l)
    {
        m_logLevel = l;
    }
    static public void setLogLevel(int n)
    {
        m_logLevel = LogLevel.values()[n];
    }
    static private String getOsLogTypeString(int nOsLogType)
    {
        switch (nOsLogType)
        {
            case Log.VERBOSE:// = 2;
            {
                return "V";
            }
            case Log.DEBUG:// = 3;
            {
                return "D";
            }
            case Log.INFO:// = 4;
            {
                return "I";
            }
            case Log.WARN:// = 5;
            {
                return "W";
            }
            case Log.ERROR:// = 6;
            {
                return "E";
            }
            case Log.ASSERT:// = 7;
            {
                return "A";
            }

        }
        return "";
    }
    static private boolean logIt(int nOsLogType)
    {
        switch (m_logLevel)
        {
            case None:
                return false;

            case Basic:
                if (nOsLogType == Log.ERROR ||
                        nOsLogType == Log.WARN)
                    return true;
                return false;

            case Detail:
                if (
                        nOsLogType == Log.ERROR ||
                        nOsLogType == Log.WARN ||
                        nOsLogType ==Log.INFO ||
                        nOsLogType == Log.DEBUG ||
                        nOsLogType == Log.VERBOSE)
                    return true;
                return false;

        }
        return false;
    }
    /**
     * Send a {VERBOSE} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     *

     */
    public static int v(String tag, String msg) {
        if (!logIt(Log.VERBOSE))
            return 0;
        log2File(Log.VERBOSE,tag, msg);
        return Log.v(tag, msg);

    }

    /**
     * Send a {VERBOSE} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int v(String tag, String msg, Throwable tr) {
        if (!logIt(Log.VERBOSE))
            return 0;
        log2File(Log.VERBOSE,tag, msg+"\n" + tr.toString());
        return Log.v(tag, msg, tr);

    }

    /**
     * Send a {DEBUG} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int d(String tag, String msg) {
        if (!logIt(Log.DEBUG))
            return 0;
        log2File(Log.DEBUG,tag, msg);
        return Log.d(tag, msg);

    }

    /**
     * Send a {DEBUG} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int d(String tag, String msg, Throwable tr) {
        if (!logIt(Log.DEBUG))
            return 0;
        log2File(Log.DEBUG,tag, msg+"\n" + tr.toString());
        return Log.d(tag, msg, tr);

    }

    /**
     * Send an {INFO} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int i(String tag, String msg) {
        if (!logIt(Log.INFO))
            return 0;
        log2File(Log.INFO,tag, msg);
        return Log.i(tag, msg);

    }

    /**
     * Send a {INFO} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int i(String tag, String msg, Throwable tr) {
        if (!logIt(Log.INFO))
            return 0;
        log2File(Log.INFO,tag, msg + "\n" + tr.toString());
        return Log.i(tag, msg, tr);

    }

    /**
     * Send a {WARN} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int w(String tag, String msg) {
        if (!logIt(Log.WARN))
            return 0;
        log2File(Log.WARN,tag, msg);
        return Log.w(tag, msg);

    }

    /**
     * Send a {WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int w(String tag, String msg, Throwable tr) {
        if (!logIt(Log.WARN))
            return 0;
        log2File(Log.WARN,tag, msg+"\n" + tr.toString());
        return Log.w(tag, msg, tr);

    }

    /*
     * Send a {WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param tr An exception to log
     */
    public static int w(String tag, Throwable tr) {
        if (!logIt(Log.WARN))
            return 0;
        log2File(Log.WARN,tag, tr.toString());
        return Log.w(tag, tr);

    }

    /**
     * Send an {ERROR} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int e(String tag, String msg) {
//        if (!logIt(Log.ERROR))
//            return 0;
        log2File(Log.ERROR,tag, msg);
        return Log.e(tag, msg);

    }

    public static int e(String tag, String strLocation, Exception err) {
//        if (!logIt(Log.ERROR))
//            return 0;
        if (findSameException(err))
        {
            log2File(Log.ERROR, tag, strLocation + " Duplicated");
        }
        else {
            log2File(Log.ERROR, tag, strLocation + getStackTrace(err));//.toString());
            saveException(err);
        }
        return Log.e(tag, strLocation + err.toString());


    }

    /**
     * Send a {ERROR} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int e(String tag, String msg, Throwable tr) {
//        if (!logIt(Log.ERROR))
//            return 0;
        log2File(Log.ERROR,tag, msg+"\n" + tr.toString());
        return Log.e(tag, msg, tr);

    }


    private String buildSimpleMessage(String format, Object... args) {
        return (args == null) ? format : String.format(Locale.US, format, args);
    }
    private String buildMessage(String format, Object... args) {
        String msg = buildSimpleMessage(format, args);
        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();

        String caller = "<unknown>";
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(KDSLog.class)) {
                String callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
                callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);

                caller = callingClass + "." + trace[i].getMethodName();
                break;
            }
        }
        return String.format(Locale.US, "[%d] %s: %s",
                Thread.currentThread().getId(), caller, msg);
    }

    static public String getAppName()
    {
        try {
            PackageInfo pkg = KDSApplication.getContext().getPackageManager().getPackageInfo(KDSApplication.getContext().getPackageName(), 0);

            String appName = pkg.applicationInfo.loadLabel(KDSApplication.getContext().getPackageManager()).toString();
            return appName;
           // String versionName = pkg.versionName;
           // return appName + "(" + versionName +")";
        }catch (Exception e)
        {

        }
        return "";
    }

    static public String getAppNameAndVersion()
    {
        try {
            PackageInfo pkg = KDSApplication.getContext().getPackageManager().getPackageInfo(KDSApplication.getContext().getPackageName(), 0);

            String appName = pkg.applicationInfo.loadLabel(KDSApplication.getContext().getPackageManager()).toString();

            String versionName = pkg.versionName;
            return appName + "(" + versionName +")";
        }catch (Exception e)
        {

        }
        return "";
    }
    /**
     * without last /
     * @return
     */
    public static String getLogDir()
    {
        //String strSaveLogPath = Environment.getExternalStorageDirectory() + "/" + DEFAULT_LOG_FOLDER +"/"+getAppName();
        String strSaveLogPath = KDSUtil.getBaseDirCanUninstall() + "/" + DEFAULT_LOG_FOLDER +"/"+getAppName();
        return strSaveLogPath;
    }
    static public String getLogFileName(Date dt)
    {
        String strSaveLogPath =getLogDir();// Environment.getExternalStorageDirectory() + "/" + DEFAULT_LOG_FOLDER;
        String strDateTimeFileName =getDateForFileName(dt);// new SimpleDateFormat("yyyy-MM-dd").format(dt);
        strDateTimeFileName = getAppName() + "_" +strDateTimeFileName;
        return strSaveLogPath + "/" + strDateTimeFileName+".log";
    }

    protected static String getDateForFileName(Date dt)
    {
        String strDateTimeFileName = new SimpleDateFormat("yyyy-MM-dd").format(dt);
        return strDateTimeFileName;
    }
    static final long MAX_LOG_SIZE = 50*1024*1024; //50M.
    //static final long MAX_LOG_SIZE = 5*1024; //5K.
    private static File createSelfLogFile()
    {
        do
        {
            String state = Environment.getExternalStorageState();
            // 未安装 SD 卡
            if (true != Environment.MEDIA_MOUNTED.equals(state)) {
                android.util.Log.d(TAG, "Not mount SD card!");
                break;
            }
            // SD 卡不可写
            if (true == Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                android.util.Log.d(TAG, "Not allow write SD card!");
                break;
            }
            // 只有存在外部 SD 卡且可写入的情况下才允许保存日志文件到指定目录路径下
            // 没有指定日志文件存放位置的话，就写到默认位置，即 SD 卡根目录下的 kdsdata/log 目录中
            if (true == m_strLogFolderPath.trim().equals("")) {
                String strSaveLogPath = getLogDir();// Environment.getExternalStorageDirectory() + "/" + DEFAULT_LOG_FOLDER;
                File fileSaveLogFolderPath = new File(strSaveLogPath);
                // 保存日志文件的路径不存在的话，就创建它
                if (true != fileSaveLogFolderPath.exists()) {
                    fileSaveLogFolderPath.mkdirs();
                }
                // 如果这里保存日志文件的路径还不存在的话，则要提醒用户了
                if (true != fileSaveLogFolderPath.exists()) {
                    android.util.Log.d(TAG, "Create log folder failed!");
                    break;
                }
                // 指定日志文件保存的路径，文件名由内部按日期时间形式
                m_strLogFolderPath = strSaveLogPath;
            }
            // 得到当前日期时间的指定格式字符串
            //use this can create a unhandled exception !!!!!!!!!!!!!!!!!!
            //String strDateTimeFileName = new SimpleDateFormat(getAppNameAndVersion()+"_yyyy-MM-dd").format(new Date());
            //String strDateTimeFileName =getDateForFileName(new Date()) ;//new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            //strDateTimeFileName = getAppName() + "_" +strDateTimeFileName;
            //File fileLogFilePath = new File(m_strLogFolderPath, strDateTimeFileName+ ".log");
            File fileLogFilePath = new File(getLogFileName(new Date()));//m_strLogFolderPath, strDateTimeFileName+ ".log");
            // 如果日志文件不存在，则创建它
            if (true != fileLogFilePath.exists()) {
                try {
                    fileLogFilePath.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            else
            {//check its size
                if (fileLogFilePath.length()>MAX_LOG_SIZE)
                {
                    resetLogFile(fileLogFilePath);

                }

            }

            // 如果执行到这步日志文件还不存在，就不写日志到文件了
            if (true != fileLogFilePath.exists()) {
                android.util.Log.d(TAG, "Create log file failed!");
                break;
            }
            return fileLogFilePath;
        }
        while (false);
        return null;


    }

    static private void resetLogFile(File fileLogFilePath)
    {
        try {
            fileLogFilePath.delete();

            fileLogFilePath.createNewFile();
            if (true != fileLogFilePath.exists()) return;
            for (int i=0; i< m_arLoggedException.size(); i++)
            {
                log2File(fileLogFilePath, Log.ERROR,"RESET", KDSLog.getStackTrace(m_arLoggedException.get(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    // 存放日志文件的目录全路径
    public static String m_strLogFolderPath = "";

    private static void  log2File(int nOsLogType, String tag, String strMsg )
    {

        File fileLogFilePath = createSelfLogFile();
        log2File(fileLogFilePath,nOsLogType, tag, strMsg);

//        FileWriter objFilerWriter = null;
//        BufferedWriter objBufferedWriter = null;
//        do // 非循环，只是为了减少分支缩进深度
//        {
//            File fileLogFilePath = createSelfLogFile();
//            if (fileLogFilePath == null)
//                break;
//
//            try
//            {
//                objFilerWriter = new FileWriter( fileLogFilePath, //
//                        true );          // 续写不覆盖
//            }
//            catch (IOException e1)
//            {
//                android.util.Log.d( TAG, "New FileWriter Instance failed" );
//                e1.printStackTrace();
//                break;
//            }
//            objBufferedWriter = new BufferedWriter( objFilerWriter );
//            // 得到当前日期时间的指定格式字符串
//            String strDateTimeLogHead = new SimpleDateFormat( "MM-dd HH:mm:ss.S" ).format( new Date() );
//            // 将日期时间头与日志信息体结合起来
//            strMsg =strDateTimeLogHead +" " +getOsLogTypeString(nOsLogType)+ "/" + tag + ": "  + strMsg + "\n";
//
//
//            try
//            {
//
//                objBufferedWriter.write( strMsg );
//                objBufferedWriter.flush();
//            }
//            catch (IOException e)
//            {
//                android.util.Log.d( TAG, "objBufferedWriter.write or objBufferedWriter.flush failed" );
//                e.printStackTrace();
//            }
//
//        }while( false );
//
//        if ( null != objBufferedWriter )
//        {
//            try
//            {
//                objBufferedWriter.close();
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }
//        }
//
//        if ( null != objFilerWriter )
//        {
//            try
//            {
//                objFilerWriter.close();
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }
//        }
    }

    private static void  log2File(File fileLogFilePath, int nOsLogType, String tag, String strMsg )
    {
        FileWriter objFilerWriter = null;
        BufferedWriter objBufferedWriter = null;
        do // 非循环，只是为了减少分支缩进深度
        {
            //File fileLogFilePath = createSelfLogFile();
            if (fileLogFilePath == null)
                break;

            try
            {
                objFilerWriter = new FileWriter( fileLogFilePath, //
                        true );          // 续写不覆盖
            }
            catch (IOException e1)
            {
                android.util.Log.d( TAG, "New FileWriter Instance failed" );
                e1.printStackTrace();
                break;
            }
            objBufferedWriter = new BufferedWriter( objFilerWriter );
            // 得到当前日期时间的指定格式字符串
            String strDateTimeLogHead = new SimpleDateFormat( "MM-dd HH:mm:ss.S" ).format( new Date() );
            // 将日期时间头与日志信息体结合起来
            strMsg =strDateTimeLogHead +" " +getOsLogTypeString(nOsLogType)+ "/" + tag + ": "  + strMsg + "\n";


            try
            {

                objBufferedWriter.write( strMsg );
                objBufferedWriter.flush();
            }
            catch (IOException e)
            {
                android.util.Log.d( TAG, "objBufferedWriter.write or objBufferedWriter.flush failed" );
                e.printStackTrace();
            }

        }while( false );

        if ( null != objBufferedWriter )
        {
            try
            {
                objBufferedWriter.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        if ( null != objFilerWriter )
        {
            try
            {
                objFilerWriter.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    public static String getStackTrace(Throwable e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String s =  "\r\n" + sw.toString() + "\r\n";
            pw.close();
            sw.close();
            return s;

        } catch (Exception e2) {
            KDSLog.e(TAG, e2.toString());

            return "bad get Error Info From Exception";
        }
    }

    static public int removeLogFiles(int nDays)
    {

        Date dt = new Date();
        long lDtNow = dt.getTime();
        lDtNow -= (nDays * 24 * 60 * 60 * 1000);
        dt.setTime(lDtNow);

        File dir = new File(getLogDir());
        File[] files = dir.listFiles();
        if (files == null) return 0;
        int ncounter = 0;
        for (int i=0; i< files.length; i++)
        {
            File f = files[i];
            if (isExpiredFile(f, dt))
            {
                f.delete();
                ncounter ++;
            }

        }
        return ncounter;
    }

    static public boolean isExpiredFile(File f, Date dtLastDate)
    {
        if (f.isDirectory()) return false;
        String s = f.getName();
        int n = s.indexOf("_");

        s = s.substring(n+1, n+1 + 10 );
        String last = getDateForFileName(dtLastDate);
        if (s.compareTo(last) <0)
            return true;
        else
            return false;

    }

    static public void logAppStarted()
    {
        i(TAG, "*************** "+getAppNameAndVersion() + " Started" + " ***************");
    }

    public static String _FUNCLINE_() {

        StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
        String funcName =  traceElement.getMethodName();
        int line =  traceElement.getLineNumber();
        return funcName + "(" + line + ") ";


    }

    static private boolean findSameException(Exception ex)
    {
        for (int i=0; i< m_arLoggedException.size(); i++)
        {
            String s0 = KDSLog.getStackTrace( m_arLoggedException.get(i));//.toString();//.getMessage();
            String s1 = KDSLog.getStackTrace(ex);//.toString();//.getMessage();
            if (s0.equals(s1))
                return true;

        }
        return false;
    }
    static int MAX_BUFFERED_SIZE = 100;
    static private void saveException(Exception ex)
    {
        m_arLoggedException.add(ex);
        if (m_arLoggedException.size() > MAX_BUFFERED_SIZE)
        {
            int n = m_arLoggedException.size() - MAX_BUFFERED_SIZE;
            for (int i=0; i< n; i++)
                m_arLoggedException.remove(0);
        }
    }

}
