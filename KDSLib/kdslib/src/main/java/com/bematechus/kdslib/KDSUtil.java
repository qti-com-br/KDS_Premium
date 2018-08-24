package com.bematechus.kdslib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

/**
 *
 */
public class KDSUtil {

    static public String TAG = "KDSUtil";

    static public  void showMsg(Context context, String str)
    {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder( context);
        dlgAlert.setTitle(context.getString(R.string.message));//"Message");
        dlgAlert.setMessage(str);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
//    static public String convertBytesToString(byte[] ar)
//    {
//        String s = new String(ar);
//        return s;
//    }
    static public byte[] convertStringToUtf8Bytes(String str)
    {
        try
        {
            return str.getBytes("UTF-8");
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return null;
        }
    }
//    static public ByteBuffer convertStringToByteBuffer(String str)
//    {
//        byte[] bytes = convertStringToUtf8Bytes(str);
//        return ByteBuffer.wrap(bytes);
//
//    }
    static public String convertUtf8BytesToString(byte[] bytesUtf8)
    {
        try {
            String strutf8 = new String(bytesUtf8, "UTF-8");
            return strutf8;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return "";
        }
    }

    static public String convertUtf8BytesToString(byte[] bytesUtf8, int noffset, int ncount)
    {
        try {
            String strutf8 = new String(bytesUtf8,noffset, ncount, "UTF-8");
            return strutf8;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return "";
        }
    }

    static public String convertIntToString(long nVal)
    {
        String s = String.format("%1$d", nVal);
        return s;
    }
    static public String convertIntToString2(int nVal)
    {
        return Integer.toString(nVal);

    }

    static public String convertBoolToString(boolean bVal)
    {
        if (bVal)
            return "1";
        return "0";


    }
    static public String convertFloatToString(float fltVal)
    {


        String s = String.format(Locale.ENGLISH,"%.2f", fltVal);
        return s;
    }


    static public  int convertStringToInt(String strID, int nDefault)
    {
        try
        {
            if (strID.isEmpty()) return nDefault;
            int nID = Integer.parseInt(strID);
            return nID;
        }
        catch (Exception e)
        {
            KDSLog.d(TAG,KDSLog._FUNCLINE_() + strID + " " + e.toString());
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// KDSUtil.error( e));
            return nDefault;
        }
    }
    static public  long convertStringToLong(String strID, long nDefault)
    {
        try
        {
            long nID = Long.parseLong(strID);
            return nID;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return nDefault;
        }
    }

    static public  float convertStringToFloat(String strVal, float fltDef)
    {
        try
        {
            float flt = Float.parseFloat(strVal);
            return flt;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return fltDef;
        }
    }
    /***
     * 63a8ea9a-82ff-47ad-8089-3a3ee8da1c15
     * @return
     * 36 bytes length string
     */
    static public String createNewGUID()
    {

        String s = UUID.randomUUID().toString();//create new GUID
        s = s.replaceAll("-", "");
        return s;
    }



    static public String getCurrentDateString()
    {
        Date dt = new Date();
        return convertDateToString(dt);
    }
    static public String convertDateToString(Date dt)
    {

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String str=sdf.format(dt);
        return str;

    }

    static public Date convertDbStringToDate(String strDate)
    {
        String s = strDate;
        SimpleDateFormat sdf = null;
        if (strDate.indexOf("-") >=0)
            sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        else if (strDate.indexOf("/")>=0)
            sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try
        {
            Date date = sdf.parse(s, new ParsePosition(0));// null).parse(s);
            return date;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return (new Date());
        }
    }

    static public Date convertStringToDate(String strDate)
    {
        String s = strDate;
        SimpleDateFormat sdf = null;
        if (strDate.indexOf("-") >=0)
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        else if (strDate.indexOf("/")>=0)
            sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
        try
        {
            Date date = sdf.parse(s, new ParsePosition(0));// null).parse(s);
            return date;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return (new Date());
        }
    }

    static public Calendar g_calender = null;
    /**
     * the format is fixed:
     *  "yyyy-MM-dd HH:mm:ss"
     * @param strDate
     * @return
     */
    static public Date convertStringToDateSelf(String strDate)
    {

        String s = strDate;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        //SimpleDateFormat sdf = null;
        if (strDate.indexOf("-") >=0)
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        else if (strDate.indexOf("/")>=0)
            sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
        //t.debug_print_Duration("load time");
        try
        {
            Date date = sdf.parse(s, new ParsePosition(0));// null).parse(s);
            //t.debug_print_Duration("load parsetime");
            return date;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
            return (new Date());
        }
    }
    static public Date convertStringToDate(String strDate , Date dtDefault)
    {
        String s = strDate;
        if (s == null) return dtDefault;
        if (s.isEmpty()) return  dtDefault;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try
        {
            Date date = sdf.parse(s, new ParsePosition(0));// null).parse(s);
            return date;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + KDSLog.getStackTrace(e));

            return dtDefault;
        }
    }

    static public Date createInvalidDate()
    {
        if (g_calender == null)
            g_calender = Calendar.getInstance(TimeZone.getDefault());
        g_calender.set(1999,9,9,0,0,0);
        return g_calender.getTime();
        //Calendar c = Calendar.getInstance();
        //c.set(1999,9,9,0,0,0);
        //return c.getTime();

    }

    static public boolean isInvalidDate(Date dt)
    {
        Date dtInvalid =  createInvalidDate();
        return (dtInvalid.equals(dt));

    }

    static public boolean isExistedInArray(ArrayList intArray, int nVal)
    {
        int ncount = intArray.size();
        for (int i=0; i< ncount; i++)
        {
            int n = (int)intArray.get(i);
            if (n == nVal)
                return true;
        }
        return false;
    }

    static public boolean isExistedInArray(ArrayList stringArray, String strVal)
    {
        int ncount = stringArray.size();
        for (int i=0; i< ncount; i++)
        {
            String s = (String)stringArray.get(i);
            if (s.equals(strVal))
                return true;
        }
        return false;
    }

    static public int findMaxValue(ArrayList intArray)
    {
        int nMax = -1;
        int ncount = intArray.size();
        for (int i=0; i< ncount; i++)
        {
            int n = (int)intArray.get(i);
            if (n > nMax)
                nMax = n;
        }
        return nMax;
    }
    public static String fixSqliteSingleQuotationIssue(String strOriginal)
    {
        if (strOriginal == null)
            return strOriginal;
        String s = strOriginal;
        s = s.replaceAll("'", "''");
        return s;
    }


    static private final int  LS256_WEB_SAFE_COLORS_COUNT = 216;
    static private final int  LS256_GREY_SCALE_BASIC = 224;
    static private final int  LS256_GREY_SCALE_COLORS_COUNT = 16;

    /**
     * In android , the color is a int value
     * @param nWebColor
     * @return
     */
    public static int convertWebColor2RGB(int nWebColor)
    {
        int nLsColorIndex = nWebColor;
        //Color clr = new Color(0,0,0);
        int clr = 0;
        //clr.SetRGB(0,0,0);
        if (nLsColorIndex <0) return clr;
        int r, g, b;
//	int rr, gg, bb;
        b = nLsColorIndex%6;
        g = (nLsColorIndex/6) %6;
        r = (nLsColorIndex/36) %6;

        int topb = (int)0x00ff;
        int stepb = 0x33;

        if (nLsColorIndex < LS256_WEB_SAFE_COLORS_COUNT)
        {
            clr = Color.rgb( (int)(topb - r*stepb),
                    (int)(topb - g*stepb),
                    (int)(topb - b*stepb));
            return clr;
        }
        else if (nLsColorIndex > LS256_GREY_SCALE_BASIC-1 && nLsColorIndex < LS256_GREY_SCALE_BASIC + LS256_GREY_SCALE_COLORS_COUNT )
        {
            int nIndex = nLsColorIndex - LS256_GREY_SCALE_BASIC;

            clr = Color.rgb((int)(nIndex*0x11),
                    (int)(nIndex*0x11),
                    (int)(nIndex*0x11));
            return clr;
        }
        else if  (nLsColorIndex >=216 && nLsColorIndex <= 223)
        {
            return Attr256_216_223(nLsColorIndex);
        }
        else if	(nLsColorIndex >=240 && nLsColorIndex <= 247)
        {
            return Attr256_240_247(nLsColorIndex);
        }
        else if (nLsColorIndex >=248 && nLsColorIndex <= 255)
        {
            return Attr256_248_255(nLsColorIndex);
        }
        else
            return clr;
    }

    static protected int Attr256_216_223(int nattr)
    {
        //Color c = new Color(0,0,0);
        int c = 0;
        switch (nattr)
        {
            case 216:
                c = Color.rgb(48,48,48);
                break;
            case 217:
                c= Color.rgb(0, 0, 192);
                break;
            case 218:
                c= Color.rgb(0,192,0);
                break;
            case 219:
                c= Color.rgb(0, 192, 192);
                break;
            case 220:
                c= Color.rgb(208, 0, 0);
                break;
            case 221:
                c= Color.rgb(208, 0, 208);
                break;
            case 222:
                c= Color.rgb(208, 188, 0);
                break;
            case 223:
                c= Color.rgb(208,208,208);
                break;
            default:
                break;
        }
        return c;
    }
    static protected int Attr256_240_247(int nattr)
    {
        //Color c = new Color(0,0,0);
        int c = 0;
        switch (nattr)
        {
            case 240:
                c = Color.rgb(0, 0, 0);
                break;
            case 241:
                c = Color.rgb(0,0,160);
                break;
            case 242:
                c = Color.rgb(0, 160, 0);
                break;
            case 243:
                c = Color.rgb(0,160,160);
                break;
            case 244:
                c = Color.rgb(160, 0, 0);
                break;
            case 245:
                c = Color.rgb(160,0,160);
                break;
            case 246:
                c = Color.rgb(160, 140, 0);
                break;
            case 247:
                c = Color.rgb(160,160,160);
                break;
            default:
                break;
        }
        return c;
    }
    static protected int Attr256_248_255(int nattr)
    {
        //Color c = new Color(0,0,0);
        int c = 0;
        switch (nattr)
        {
            case 248:
                c = Color.rgb(96,96,96);
                break;
            case 249:
                c = Color.rgb(0, 0, 255);
                break;
            case 250:
                c = Color.rgb(0,255,0);
                break;
            case 251:
                c = Color.rgb(0, 255, 255);
                break;
            case 252:
                c = Color.rgb(255,0,0);
                break;
            case 253:
                c = Color.rgb(255, 0, 255);
                break;
            case 254:
                c = Color.rgb(255,255,0);
                break;
            case 255:
                c = Color.rgb(255,255,255);
                break;
            default:
                break;
        }
        return c;
    }



    static public void debug(String str)
    {

        KDSLog.w("Info", str);
    }

    static public void debug(String str, long val)
    {
        System.out.println(str + convertIntToString(val));
    }

    /**
     *
     * @param folderName
     *      With last "\\".
     * @return
     */
    static public boolean createFolder(String folderName)
    {

        if (folderName.isEmpty()) return false;
        String s = folderName;
        if (s.charAt(folderName.length()-1) == '/')
            s = s.substring(0, s.length()-1);
        File f = new File(s);
        if (f.exists())
            return true;
        return f.mkdirs();

    }

    public static boolean copyFile(String sourceFile, String destFile)
    {
        try
        {
            copyFile (new File(sourceFile), new File(destFile));
            return true;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }

    }
    //
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            //
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

            //
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

            //
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            //
            outBuff.flush();
        } finally {
            //
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }

//    //
//    public static void copyDirectiory(String sourceDir, String targetDir) throws IOException {
//        //
//        (new File(targetDir)).mkdirs();
//        //
//        File[] file = (new File(sourceDir)).listFiles();
//        for (int i = 0; i < file.length; i++) {
//            if (file[i].isFile()) {
//                //
//                File sourceFile = file[i];
//                //
//                File targetFile = new File(new File(targetDir).getAbsolutePath() + File.separator + file[i].getName());
//                copyFile(sourceFile, targetFile);
//            }
//            if (file[i].isDirectory()) {
//                //
//                String dir1 = sourceDir + "/" + file[i].getName();
//                //
//                String dir2 = targetDir + "/" + file[i].getName();
//                copyDirectiory(dir1, dir2);
//            }
//        }
//    }
//    /***************************************************************************
//     *
//     * @return
//     *   with the last "/"
//     */
//    public static String getApplicationFolder()
//    {
//        String path = System.getProperty("user.dir");
//        //String path = System.getProperty("java.class.path");
//        return path + "/";
//
//
//
//
//    }

    public static boolean fileExisted(String fileName)
    {
        File f = new File(fileName);

        return f.exists();
    }
    public static boolean remove(String fileName)
    {
        File f = new File(fileName);
        try
        {
            return f.delete();

        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }

    }
    public static String readUtf8TextFile(String fileName)
    {
        return readTextFile(fileName, "utf-8");
    }

    public static String readTextFile(String fileName, String encode)
    {

        File file = new File(fileName);
        try
        {
            FileInputStream in = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(in,encode);// "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String s1 = null;
            String content = "";

            while ((s1 = br.readLine()) != null)
            {
                content += s1 + "\n";
            }
            br.close();
            isr.close();
            return content;
        }
        catch (Exception ex)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,ex);//+ ex.toString());
            //KDSLog.e(TAG, KDSUtil.error( ex));
            return "";
        }

    }

//    /**
//     *
//     * @param fileName
//     *          file name
//     * @param nStartIndex
//     *             read from where
//     * @param buffer
//     *          data buffer
//     * @param nLength
//     *          read how many data
//     * @return
//     *      read length
//     */
//    public static int readBytesFile(String fileName,long nStartIndex, ByteBuffer buffer,int nToBufferIndex, int nLength)
//    {
//        int n  = 0;
//        RandomAccessFile randomFile = null;
//        try {
//
//            randomFile = new RandomAccessFile(fileName, "r");
//
//            randomFile.seek(nStartIndex);
//            n = randomFile.read(buffer.array(), nToBufferIndex, nLength);
//            randomFile.close();
//            buffer.limit(n + nToBufferIndex);
//
//        } catch (Exception e)
//        {
//            Log.e(TAG,KDSLog._FUNCLINE_() + e.toString());
//            Log.e(TAG, KDSUtil.error( e));
//
//        } finally {
//            if (randomFile != null) {
//                try {
//                    randomFile.close();
//
//                } catch (Exception e1) {
//                    Log.e(TAG,KDSLog._FUNCLINE_() + e1.toString());
//                    Log.e(TAG, KDSUtil.error( e1));
//                }
//            }
//        }
//        return n;
//
//    }

//    static public int appendFile(String fileName, ByteBuffer buf, int nDataLen)
//    {
//        RandomAccessFile randomFile = null;
//        try {
//
//            randomFile = new RandomAccessFile(fileName, "rw");
//
//            randomFile.seek(randomFile.length());
//            randomFile.write(buf.array(), 0, nDataLen);
//            randomFile.close();
//            return nDataLen;
//
//        } catch (Exception e) {
//            Log.e(TAG,KDSLog._FUNCLINE_() + e.toString());
//            Log.e(TAG, KDSUtil.error( e));
//            return 0;
//        }
//
//    }

//    /*
//     * Get the extension of a file.
//     */
//    public static String getExtension(File f) {
//        String ext = null;
//        String s = f.getName();
//        int i = s.lastIndexOf('.');
//
//        if (i > 0 &&  i < s.length() - 1) {
//            ext = s.substring(i+1).toLowerCase();
//        }
//        return ext;
//    }


    public static  byte[] image2Bytes(String imagePath)
    {
        File file = new File(imagePath);
        byte[] buf = new byte[(int)file.length()];

        InputStream in = null;
        int i = 0;
        try {

            in = new FileInputStream(file);
            int tempbyte;
            while ((tempbyte = in.read()) != -1) {
                buf[i] = (byte)tempbyte;
                i++;
                //System.out.write(tempbyte);
            }
            in.close();
            return buf;
        } catch (IOException e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return null;
        }

    }

    public static String image2BytesString(String imagePath)
    {
        byte[]info=image2Bytes(imagePath);
        String s = "";
        for(int i=0;i<info.length;i++){
            s += KDSUtil.convertIntToString(info[i]);
            s += ",";
            if ( (i+1)%16 ==0)
                s += "\n";

        }
        return s;
    }

    public static long getFileLength(String fileName)
    {
        File f = new File(fileName);
        return f.length();
    }

    static public ArrayList<String> spliteString(String s, String splitor) {
        String[] ar = s.split(splitor);
        //get there are how many splitor in string.
        int nstart = 0;
        int ncount = 0;

        while (true) {
            nstart = s.indexOf(splitor, nstart);
            if (nstart >= 0) {
                ncount++;
                nstart++;
            }
            else
                break;

        }



        ArrayList<String> arRet = new ArrayList<String>();

        for (int i = 0; i < ar.length; i++) {
            arRet.add(ar[i]);

        }
        //add more space to ncount
        ncount ++; //this string should contain those substrings
        if (arRet.size() < ncount)
        {
            int n = ncount - arRet.size();
            for (int i=0; i<  n ;i ++)
            {
                arRet.add("");
            }
        }
        return arRet;
    }

    /************************************************************************/
/*
just 16bits value
*/
    /************************************************************************/
   static public byte hex_string_to_hex(String s)
    {

        String str;
        int val0 =-1;
        int val1 = -1;
        int val;
        s = s.trim();
        s.toUpperCase();


        boolean berror = false;
        for (int i=0; i< s.length(); i++)
        {
            if (i>1)
            {
                break;
            }
            str = "";
            str +=s.charAt(i);//.GetAt(i);
            val = hex_char_to_hex(str);// GetHexValue(str);

            if (i==0)
                val0 = val;
            else
                val1 = val;
        }
        if (val0 == -1 && val1 ==-1) return (byte)0xff;
        if (val0 != -1 && val1 == -1) return (byte)val0;
        if (val0 == -1 && val1 != -1) return (byte)val1;
        if (val0 != -1 && val1 != -1) return (byte)((val0 <<4) + val1);

        return (byte)0xff;
    }
    static public byte hex_char_to_hex(String s)
    {

        if (s.equals("0")) return 0;
        if (s.equals("1")) return 1;
        if (s.equals("2")) return 2;
        if (s.equals("3")) return 3;
        if (s.equals("4")) return 4;
        if (s.equals("5")) return 5;
        if (s.equals("6")) return 6;
        if (s.equals("7")) return 7;
        if (s.equals("8")) return 8;
        if (s.equals("9")) return 9;
        if (s.equals("A") || s.equals("a")) return 10;
        if (s.equals("B") || s.equals("b")) return 11;
        if (s.equals("C") || s.equals("c")) return 12;
        if (s.equals("D") || s.equals("d")) return 13;
        if (s.equals("E") || s.equals("e")) return 14;
        if (s.equals("F") || s.equals("f")) return 15;

        return 0;
    }


//    static public String runLinuxCmd(String command)
//    {
//        String result = "";
//        try {
//            Process p = Runtime.getRuntime().exec(command); // 10.83.50.111  m_strForNetAddress
//            int status = p.waitFor();
//
//            BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
//
//            String str = "";//new String();
//            String strInfo = "";
//
//            while ((str = buf.readLine()) != null) {
//                str = str + "\r\n";
//                strInfo+=str;
//            }
//            return strInfo;
//        }
//        catch (Exception e)
//        {
//            Log.e(TAG,KDSLog._FUNCLINE_() + e.toString());
//            Log.e(TAG, KDSUtil.error( e));
//            return e.getMessage();
//        }
//
//    }

    /*********************************************************************************************/
    /**
     *
     */
    static public boolean fileCopy(String fileOriginal, String fileTarget)
    {
        if (!fileExisted(fileOriginal))
            return false;

        if (fileExisted(fileTarget))
        {
            remove(fileTarget);
        }

        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(fileOriginal);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(fileOriginal); //读入原文件
                FileOutputStream fs = new FileOutputStream(fileTarget);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
            return true;
        }
        catch (Exception e) {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);//+e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
            //System.out.println("Error while copy file");
            //e.printStackTrace();

        }
        return true;
    }

    //    /**
//     * 复制整个文件夹内容
//     * @param oldPath String 原文件路径 如：c:/fqf
//     * @param newPath String 复制后路径 如：f:/fqf/ff
//     * @return boolean
//     */
//    static public boolean copyFolder(String oldPath, String newPath) {
//
//        try {
//            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
//            File a = new File(oldPath);
//            String[] file = a.list();
//            File temp = null;
//            for (int i = 0; i < file.length; i++) {
//                if (oldPath.endsWith(File.separator)) {
//                    temp = new File(oldPath + file[i]);
//                } else {
//                    temp = new File(oldPath + File.separator + file[i]);
//                }
//
//                if (temp.isFile()) {
//                    FileInputStream input = new FileInputStream(temp);
//                    FileOutputStream output = new FileOutputStream(newPath + "/" +
//                            (temp.getName()).toString());
//                    byte[] b = new byte[1024 * 5];
//                    int len;
//                    while ((len = input.read(b)) != -1) {
//                        output.write(b, 0, len);
//                    }
//                    output.flush();
//                    output.close();
//                    input.close();
//                }
//                if (temp.isDirectory()) {//如果是子文件夹
//                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
//                }
//            }
//        } catch (Exception e) {
//
//            Log.e(TAG, KDSLog._FUNCLINE_()+e.toString());
//            Log.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
//            return false;
//        }
//        return true;
//    }


    /**
     * write string to file.
     * @param filePathName
     * @param strContent
     * @return
     */
    static public boolean fileWrite(String filePathName, String strContent )
    {
        try{

            FileOutputStream fout =  new FileOutputStream(filePathName);


            byte [] bytes = convertStringToUtf8Bytes(strContent);// strContent.getBytes();

            fout.write(bytes);

            fout.close();
        }

        catch(Exception e){
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    static public String readFile(String fileName) {
        String res="";
        try{
            FileInputStream fin = new FileInputStream(fileName);//   context.openFileInput(fileName);
            int length = fin.available();
            byte [] buffer = new byte[length];
            fin.read(buffer);
            res = convertUtf8BytesToString(buffer);// EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
        }
        catch(Exception e){
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
            //e.printStackTrace();
        }
        return res;

    }

    static public String readFileText(String fileName) {
        String res="";
        try{
            FileInputStream fin = new FileInputStream(fileName);// context.openFileInput(fileName);
            int length = fin.available();
            byte [] buffer = new byte[length];
            fin.read(buffer);
            res = convertUtf8BytesToString(buffer);// EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
        }
        catch(Exception e){
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + KDSLog.getStackTrace(e));
            //e.printStackTrace();
        }
        return res;

    }
//    /**
//     *  In internal app storage folder.
//     * @param folderName
//     *    folderName/
//     * @return
//     */
//    static public boolean createInternalFolder(Context context, String folderName)
//    {
//        //String s = folderName + "tmp.tmp";
//        if (folderName.isEmpty()) return false;
//        String s = folderName;
//        if (s.charAt(folderName.length()-1) == '/')
//            s = s.substring(0, s.length()-1);
//
//        File mydir = context.getDir(s, Context.MODE_PRIVATE); //Creating an internal dir;
//        if(!mydir.exists())
//        {
//            return mydir.mkdirs();
//        }
//        return true;
//
//
//    }

    /**
     * http://stackoverflow.com/questions/4672271/reverse-opposing-colors
     * I found that the best solution for me is to convert the RGB values into YIQ values.
     * As we are only interested in the brightness value (represented by Y),
     * there is one single calculation to be done: Y = (299*R + 587*G + 114*B)/1000.
     * The Java code for that would look like this:
     * @param color
     * @return
     */
//    public static int getContrastColor(int color) {
//        double y = (299 * Color.red(color) + 587 *Color.green(color) + 114 * Color.blue(color)) / 1000;
//        return y >= 128 ? Color.BLACK : Color.WHITE;
//    }
    public static int getContrastVersionForColor(int color) {
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color),
                hsv);
        if (hsv[2] < 0.5) {
            hsv[2] = 0.7f;
        } else {
            hsv[2] = 0.3f;
        }
        hsv[1] = hsv[1] * 0.2f;
        return Color.HSVToColor(hsv);
    }


    static public boolean enableSystemVirtualBar(View v, boolean bEnabled)
    {
        if (v == null) return false;
        if (!bEnabled) {
           // v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            //v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            v.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        else
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE
                       // |View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                     //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                     | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        return true;
    }



    public static String _FUNCLINE_() {

        StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
        String funcName =  traceElement.getMethodName();
        int line =  traceElement.getLineNumber();
        return funcName + "(" + line + ") ";


    }

    public static String correctFileName(String fileName)
    {
        String s = fileName;
        s = s.replace("/", "_");
        s = s.replace("\\", "_");
        s = s.replace(":", "_");
        s = s.replace("*", "_");
        s = s.replace("?", "_");
        s = s.replace("\"", "_");
        s = s.replace(">", "_");
        s = s.replace("<", "_");
        s = s.replace("|", "_");
        return s;


    }

    public static int byteToUnsignedInt(byte b) {
        return 0x00 << 24 | b & 0xff;
    }


    public static boolean isArrayContainsSameStrings(ArrayList<String> ar0, ArrayList<String> ar1)
    {
        if (ar0.size() != ar1.size()) return false;
        for (int i=0; i< ar0.size(); i++)
        {
            if (!isExistedInArray(ar1, ar0.get(i)))
                return false;
        }
        return true;
    }

    public static String error(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String s =  "\r\n" + sw.toString() + "\r\n";
            pw.close();
            sw.close();
            return s;

        } catch (Exception e2) {

            Log.e(TAG, e2.toString()); //use sys log

            return "bad get Error Info From Exception";
        }
    }

    static public void setLanguage(Context context, SettingsBase.Language lan) {
        Locale locale = Locale.ENGLISH;
        switch (lan)
        {
            case English:
                locale = Locale.ENGLISH;
                break;
            case Chinese:
                locale = Locale.CHINESE;
                break;
        }
        Resources resources = context.getResources();// 获得res资源对象
        Configuration config = resources.getConfiguration();// 获得设置对象

        DisplayMetrics dm = resources.getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。
        config.locale = locale; // 简体中文
        resources.updateConfiguration(config, dm);
    }

    static public String getCurrentTimeForLog()
    {

        Calendar c = Calendar.getInstance();

        String s = String.format("%02d-%02d %02d:%02d:%02d.%03d",
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND),
                c.get(Calendar.MILLISECOND));
        return s;
    }

    static public String convertDateToDbString(Date dt) {

        // TimeDog d = new TimeDog();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String str = sdf.format(dt);

        return str;
    }
    static public String convertTimeToDbString(Date dt) {

        // TimeDog d = new TimeDog();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        String str = sdf.format(dt);

        return str;
    }

    static public String convertTimeToShortString(Date dt) {

        // TimeDog d = new TimeDog();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        String str = sdf.format(dt);

        return str;
    }

    /**
     *
     * @param strDate
     *  Format: yyyy/mm/dd
     * @return
     */
    static public Date convertShortStringToDate(String strDate)
    {
        //TimeDog t = new TimeDog();

        String s = strDate;
        //SimpleDateFormat sdf = null;

       // sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdf = null;
        if (strDate.indexOf("-") >=0)
            sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        else if (strDate.indexOf("/")>=0)
            sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

        try
        {
            Date date = sdf.parse(s, new ParsePosition(0));// null).parse(s);

            return date;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + KDSLog.getStackTrace(e));
            return (new Date());
        }
    }


    /**
     *
     * @param strDate
     *  Format: h:m
     * @return
     */
    static public Date convertShortStringToTime(String strDate)
    {
        //TimeDog t = new TimeDog();

        String s = strDate;
        SimpleDateFormat sdf = null;

        sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());


        try
        {
            Date date = sdf.parse(s, new ParsePosition(0));// null).parse(s);

            return date;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
            return (new Date());
        }
    }

    static public String convertTimeToFileNameString(Date dt) {

        // TimeDog d = new TimeDog();

        SimpleDateFormat sdf = new SimpleDateFormat("HHmm");

        String str = sdf.format(dt);

        return str;
    }

    static public String convertFloatToShortString(float fltVal)
    {


        String s = String.format(Locale.ENGLISH,"%.2f", fltVal);
        return s;
    }

    static public String convertDateToShortString(Date dt) {

        // TimeDog d = new TimeDog();

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        String str = sdf.format(dt);

        return str;
    }

    static public String getDateFromString(String strDateTime)
    {
        Date dt = convertStringToDate(strDateTime);
        return convertDateToDbString(dt);
    }

    static public String getTimeFromString(String strDateTime)
    {
        Date dt = convertStringToDate(strDateTime);
        return convertTimeToDbString(dt);
    }

    static public boolean convertStringToBool(String strVal, boolean bDefault)
    {
        if (strVal.isEmpty()) return bDefault;
        if (strVal.equals("1"))
            return true;
        return false;


    }


    static public boolean isSameDate(Date dt1, Date dt2)
    {
        String s1 = KDSUtil.convertDateToShortString(dt1);
        String s2 =  KDSUtil.convertDateToShortString(dt2);

        return s1.equals(s2);
    }

    static public boolean isSameTimeHM(Date dt1, Date dt2)
    {
        if ( dt1.equals(dt2))
            return true;
        String s1=  convertTimeToShortString(dt1);
        String s2=  convertTimeToShortString(dt2);
        return s1.equals(s2);
    }

    static public float convertSecondsToMins(int nSeconds)
    {
        float flt = nSeconds;
        flt /=60;
        return flt;
    }
    static public int convertMinsToSecondss(float nMins)
    {
        float flt = nMins;
        flt *=60;
        return Math.round( flt);
    }

    /**
     *
     * @param strColor
     * color_name: e.g: red
     * hex_number: #ff10fb
     * rgb_number: rgb(255,0,0)"
     * int value: 12356
     * @return
     */
    static public int convertHtmlString2Color(String strColor)
    {
        strColor =  strColor.trim();
        if (strColor.isEmpty()) return 0;
        strColor = strColor.toLowerCase();
        if (strColor.indexOf("rgb")==0) //rgb(0,23,9) format color
        {
            String regRepRgb = "(rgb|\\(|\\)|RGB)*";
            String ar[] = regex_replace(regex_replaceSpace(strColor), regRepRgb, "").split(",");
            if (ar.length!=3) return 0;
            int r = KDSUtil.convertStringToInt(ar[0], 0);
            int g = KDSUtil.convertStringToInt(ar[1], 0);
            int b = KDSUtil.convertStringToInt(ar[2], 0);
            return Color.argb(255, r, g, b);

        }
//        else if (strColor.indexOf("#") ==0 ||//#FFFFFF format color
//                ( !Character.isDigit(strColor.charAt(0))) ) //"red","blue" ... format color
        else if (isHtmlColorString(strColor))
        {
            return Color.parseColor(strColor);
        }

        else if (Character.isDigit(strColor.charAt(0)) ||
                strColor.charAt(0)=='-') //int value. 2.0.14, fix negtive value bug
        {
            return (int)KDSUtil.convertStringToLong(strColor, 0);
        }
        else
            return 0;

    }

    static public boolean isHtmlColorString(String strColor)
    {
        if (regex_is_hex_color_string(strColor)) return true;

        String ar[] = new String[]{ "red", "blue", "green", "black", "white", "gray", "cyan", "magenta",
         "yellow", "lightgray", "darkgray", "grey", "lightgrey", "darkgrey",
         "aqua", "fuchsia", "lime", "maroon", "navy", "olive", "purple",
         "silver", "teal"};
        strColor = strColor.toLowerCase();
        for (int i=0; i< ar.length; i++)
        {
            if (ar[i].equals(strColor)) return true;

        }
        return false;
    }
    /**
    * 字符串替换
    * 通过正则表达式匹配，匹配的部分将被替换
    * @param str
    *        需要替换的源字符串
    * @param regex
    *        正则表达式，匹配需要被替换的部分
    * @param replacement
    *        替换后的字符串
    * @return String
    */
    public static String regex_replace(String str, String regex, String replacement)
    {
         String strReplace = "";

         try {
                 Pattern pattern = Pattern.compile(regex);
                 Matcher matcher = pattern.matcher(str);
                 strReplace = matcher.replaceAll(replacement);
             } catch (PatternSyntaxException pse) {
                Log.e(TAG, pse.toString());

             }
         return strReplace;
    }

    public static String regex_replaceSpace(String str) {
        String regRepSpace = "\\s*|\t|\r|\n";
        return regex_replace(str, regRepSpace, "");
    }

    /**
     * 将十六进制 颜色代码 转换为 int
     *
     * @return color
     */
    public static boolean regex_is_hex_color_string(String color) {
        // #00000000 - #ffffffff
        String reg = "#[a-f0-9A-F]{8}";
        if  (Pattern.matches(reg, color))
            return true;
        reg = "#[a-f0-9A-F]{6}";
        if  (Pattern.matches(reg, color))
            return true;
        return false;
// {
//            color = "#ffffffff";
//        }
//        return Color.parseColor(color);
    }

    public static boolean sdcardExisted()
    {
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        Boolean isSDSupportedDevice = Environment.isExternalStorageRemovable();

        if(isSDSupportedDevice && isSDPresent)
        {
            return true;
            // yes SD-card is present
        }
        else
        {
            return false;
            // Sorry
        }
    }

    /**
     * 2.0.20
     *
     * without last /
     *
     * @return
     */
    public static String getBaseDirCanUninstall()
    {

        String s = KDSApplication.getContext().getExternalFilesDir(null).getAbsolutePath();
        //String s = Environment.getExternalStorageDirectory().getAbsolutePath();
        return s;
    }

    /**
     * 2.0.20
     * @return
     */
    public static String getBaseDirCanNotUninstall()
    {

        //String s = KDSApplication.getContext().getExternalFilesDir(null).getAbsolutePath();
        String s = Environment.getExternalStorageDirectory().getAbsolutePath();
        return s;
    }

    static public String seconds2HMS(int seconds)
    {
        int h = (seconds / 3600);
        int m = ((seconds % 3600)/ 60);
        int s = (seconds % 60);
        return String.format("%02d:%02d:%02d", h, m, s);


    }


}
