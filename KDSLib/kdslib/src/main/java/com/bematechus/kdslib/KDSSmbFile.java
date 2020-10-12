package com.bematechus.kdslib;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
//import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import jcifs.Configuration;
//import jcifs.smb.SmbFile;
//import jcifsng.Configuration;
//import jcifsng.smb.SmbFile;


/**
 * Created by David.Wong on 2018/9/4.
 * Rev:
 */
public class KDSSmbFile extends Handler {
    static final private String TAG = "KDSSmbFile";
    static protected int BUF_SIZE = 1024000; //10k;
    static final public int REFRESH_LIST = 1;
    static final public int SHOW_PROGRESS = 2;
    static final public int HIDE_PROGRESS = 3;
    static final public int REFRESH_DIRS = 4;
    static final public int ERROR_CREATE_FOLDER = 5;

    protected ProgressDialog m_progressDialog = null;
    protected boolean m_isNewFolderEnabled = true;
    protected String m_dir = "";

    public KDSSmbFile()
    {
        setSmbV1Config();
    }

    public interface ChosenDirectoryListener
    {
        public void onChosenDir(String chosenDir);
    }


    static private boolean m_bEnableSmbV2 = true ;//false; //default is false //kpp1-376
    static public void smb_setEnableSmbV2(boolean bEnabled)
    {
        m_bEnableSmbV2 = bEnabled;
        KDSSmbFile2.setEnableSmbV2(bEnabled);

    }
    static public boolean getEnabledSmbV2()
    {
        return m_bEnableSmbV2;
    }

    @Override
    public void handleMessage(Message msg) {

    }

    public static String smb_readFromSmbToLocal(String smbFileName, String localFolderWithoutLastSep)
    {
//        if (m_bEnableSmbV2) //kpp1-376, we will just use smbv2
            return KDSSmbFile2.readFromSmbToLocal(smbFileName, localFolderWithoutLastSep);
//        else //kpp1-376, we will just use smbv2
//            return KDSSmbFile1.readFromSmbToLocal(smbFileName, localFolderWithoutLastSep);

    }

    static public boolean smb_isValidPath(String remoteUriFolder)
    {
        //kpp1-373
        KDSSMBPath path = KDSSMBPath.parseString(remoteUriFolder);
        String ip = path.getPCName();
        if (!KDSSocketTCPSideBase.ping(ip, 2))
            return false;
        //
//        if (m_bEnableSmbV2) //kpp1-376, we will just use smbv2
            return KDSSmbFile2.isValidPath(remoteUriFolder);
//        else { //kpp1-376, we will just use smbv2
//            return KDSSmbFile1.isValidPath(remoteUriFolder);
//
////            if (!KDSSmbFile1.isValidPath(remoteUriFolder))
////                return KDSSmbFile2.isValidPath(remoteUriFolder);
////            else
////                return true;
//        }
    }

    static public int smb_checkFolderWritable(String smbFolder)
    {
//        if (m_bEnableSmbV2) //kpp1-376, we will just use smbv2
            return KDSSmbFile2.checkFolderWritable(smbFolder);
//        else ////kpp1-376, we will just use smbv2
//            return KDSSmbFile1.checkFolderWritable(smbFolder);
    }

    public void setNewFolderEnabled(boolean isNewFolderEnabled)
    {
        m_isNewFolderEnabled = isNewFolderEnabled;
    }

//    static public List<SmbFile> getFiles(String dir)
//    {
//        if (m_bEnableSmbV2)
//            return KDSSmbFile2.getFiles(dir);
//        else
//            return KDSSmbFile1.getFiles(dir);
//    }

    public boolean getNewFolderEnabled()
    {
        return m_isNewFolderEnabled;
    }


    public void chooseDirectory()
    {

    }


    public boolean createSubDirInThread(String newDir)
    {
        return false;
    }



    static protected boolean isAnonymous(String dir)
    {
        if (dir.indexOf("smb:// : @")>=0)
            return true;
        else if (dir.indexOf("@")<0)
            return true;
        return false;
    }

//    protected static byte[] removeHeaders(byte[] buf, int ncount)
//    {
//        int nlen = buf.length;
//        for (int i=ncount; i< nlen; i++)
//        {
//            buf[i-ncount] = buf[i];
//        }
//        for (int i=nlen-ncount; i< nlen; i++)
//        {
//            buf[i]= 0;
//        }
//        return buf;
//    }

    protected void showProgressDialog(Context context, String title, String message){

        if (context == null) return;
        // if (m_progressDialog == null)
        //{
        m_progressDialog = new ProgressDialog(context);
        m_progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//.STYLE_HORIZONTAL);

        //m_progressDlg.setCancelable(true);
        // m_progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,KDSApplication.getContext().getString(R.string.cancel), new OnClickListener() {
        //     @Override
        //     public void onClick(DialogInterface dialog, int which) {
        //         hideProgressDialog();
        //     }
        // });

        //}
//        m_progressDialog.setMax(100);
//        m_progressDialog.setProgress(0);
        m_progressDialog.setTitle(title);
        m_progressDialog.setMessage(message);
        m_progressDialog.show();

        Point size = new Point();
        m_progressDialog.getWindow().getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;//获取界面的宽度像素
        int height = size.y;
        WindowManager.LayoutParams params = m_progressDialog.getWindow().getAttributes();//一定要用mProgressDialog得到当前界面的参数对象，否则就不是设置ProgressDialog的界面了
        params.alpha = 0.8f;//设置进度条背景透明度
        params.height = height/8;//设置进度条的高度
        params.gravity = Gravity.CENTER;//设置ProgressDialog的重心
        params.width = 200;// width/5;//4*width/5;//设置进度条的宽度
        params.dimAmount = 0f;//设置半透明背景的灰度，范围0~1，系统默认值是0.5，1表示背景完全是黑色的,0表示背景不变暗，和原来的灰度一样
        m_progressDialog.getWindow().setAttributes(params);//把参数设置给进度条，注意，一定要先show出来才可以再设置，不然就没效果了，因为只有当界面显示出来后才可以获得它的屏幕尺寸及参数等一些信息

        //downloadApk();
    }
    protected void hideProgressDialog()
    {
        if (m_progressDialog != null)
            m_progressDialog.hide();
    }


    static public ArrayList<String> findAllXmlFiles(String remoteUriFolder, int nMaxFiles, ArrayList<String> ar)
    {
//        if (m_bEnableSmbV2)//kpp1-376, we will just use smbv2
            return KDSSmbFile2.findAllXmlFiles(remoteUriFolder, nMaxFiles, ar);
//        else//kpp1-376, we will just use smbv2
//            return KDSSmbFile1.findAllXmlFiles(remoteUriFolder, nMaxFiles, ar);
    }

    static public boolean isExistedSubFolder(String strRootFolder, String strSubFolder)
    {
        //if (m_bEnableSmbV2)//kpp1-376, we will just use smbv2
            return KDSSmbFile2.isExistedSubFolder(strRootFolder, strSubFolder);
//        else
//            return KDSSmbFile1.isExistedSubFolder(strRootFolder, strSubFolder);
    }

    static public boolean createSubDir(String newDir)
    {
        //if (m_bEnableSmbV2)//kpp1-376, we will just use smbv2
            return KDSSmbFile2.createSubDir(newDir);
//        else
//            return KDSSmbFile1.createSubDir(newDir);
    }

    static public Object[] findAllFiles(String remoteUriFolder)
    {

        //if (m_bEnableSmbV2)//kpp1-376, we will just use smbv2
            return KDSSmbFile2.findAllFiles(remoteUriFolder);
//        else
//            return KDSSmbFile1.findAllFiles(remoteUriFolder);
    }

    /**
     * KPP1-367
     * @param buffer
     * @return
     */
    static public String getBytesEncodingFormat(byte[] buffer)
    {
        if (buffer[0] ==(byte) 0xEF &&  //utf-8: EF, BB, BF
                buffer[1] == (byte)0xBB &&
                buffer[2] == (byte)0xBF )
            return "UTF-8";
        else if (buffer[0] == (byte)0xFF &&
                buffer[1] == (byte)0xFE   ) // UCS-2 LE BOM (Little Endian)
        {
            return "UTF-16LE";
        }
        else if (buffer[0] == (byte)0xFE &&
            buffer[1] == (byte)0xFF   ) // UCS-2 LE BOM (Big Endian)
        {
            return "UTF-16BE";
        }
        else
            return "";

    }

    /**
     *  use the jcifs.Config class which is the class that maintains this information internally however, again, properties must be set before jCIFS client classes are referenced:
     */
    static public void setSmbV1Config()
    {
        //kpp1-376, we will just use smbv2
//        //this can been removed.
//        jcifs.Config.setProperty( "jcifs.smb.client.responseTimeout", "3000" );//default 30000
//        //this must set
//        jcifs.Config.setProperty( "jcifs.smb.client.soTimeout", "3500" );// //default 35000
//        //following can been removed. But, just keep them.
//        jcifs.Config.setProperty( "jcifs.netbios.soTimeout", "3000" );//default 5000
//        jcifs.Config.setProperty( "jcifs.netbios.retryTimeout", "2000" );//default 3000




    }
}
