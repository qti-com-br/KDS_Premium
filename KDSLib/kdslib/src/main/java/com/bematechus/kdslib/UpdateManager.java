package com.bematechus.kdslib;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */
/**
 * Created by QA on 5/29/2015.
 * http://221.176.5.215?username=zj&password=123'
 1. There are 3 files in http server.
 This server link is fixed. I think we should have username and password for this http folder.
 All username and password are fixed too.
 (1) KDS.xml
 For KDS app version information
 (2) KDSRouter.xml
 For KDSRouter app version information
 (3) KDSStatistic.xml
 For KDSStatistic app version information
 2. file format
 <app name="kds/kdsrouter/kdsstatistic" ver="1.0" rate=5>
 <-- name: the app name
 ver: Last version
 rate: the recommended rate, max=5. --/>
 <applink>http://www.bematechus.com/androidapp/****.apk</applink>
 <applink>http://www.bematechus1.com/androidapp/****.apk</applink>
 ...
 <applink>http://www.bematechus2.com/androidapp/****.apk</applink>
 <-- The link where we can download apk file.
 There are multiple link that we can get our apk.
 --/>
 <features>
 <-- The new feature for this version --/>
 <feature brief="mirror stations">two station work as mirror mode</feature>
 <-- brief: the feature summary description
 text: the feature detail information --/>
 <feature brief="unbump last">unbump last bumped order</feature>
 ...
 <feature brief="print order">Print selected order</feature>


 <features/>
 <app/>

 */


import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class UpdateManager implements URIDownload.URIDownloadEvent {

    public interface UpdateEvents
    {
        public void onNoNewVersionApk();
        public void onNewVersionValid(String newVersion);
        public void onUpdateCanceled();
        public void onUpdateError(String strError);

    }
    static final String TAG = "UpdateManager";
    static final String UPDATE_LOG_URI_FOLDER ="http://www.bematechus.com/Logic_FTP/KDS/KitchenGo/";// "http://www.cnblogs.com/manongxiaojiang/archive/2012/10/13/";



    private String m_strAppApkName = "";//"2722068.html";

    private UpdateEvents m_eventsReceiver = null;

    /* 下载包安装路径 */
    private static final String m_saveLocalFolder = "kdsupdate/";

    private Context mContext = null;
    //提示语
   // private String updateMsg = "There is new version. Do you want to upgrade it now?";
    //返回的安装包url
    //private String apkUrl = "http://softfile.3g.qq.com:8080/msoft/179/24659/43549/qq_hd_mini_1.4.apk";
    private Dialog noticeDialog = null;
    private ProgressDialog m_progressDialog = null;
    private URIDownload m_uriDownload = new URIDownload(this, m_saveLocalFolder);

    UpdateAppInfo m_appUpdateInfo = null;

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    public void setEventsReceiver(UpdateEvents receiver)
    {
        m_eventsReceiver = receiver;
    }
    public void onDownloadProgressUpdate(String uriFileName, String localFileName, int nPercent)
    {
        if (m_progressDialog != null)
        {
            m_progressDialog.setProgress(nPercent);
        }
    }
    public void onDownloadFinished(String uriFileName, String localFileName)
    {
        String s = localFileName;

        //s +=".apk"; //for test

        s = s.toUpperCase();
        if (s.indexOf(".XML")>=0)
        {
            afterUpdateLogDownloaded(localFileName);
        }
        else if (s.indexOf(".APK")>=0)
        {
            if (checkApkValid(uriFileName, localFileName)) {
                afterUpdateApkDownloaded(localFileName);
                if (m_progressDialog != null) {
                    m_progressDialog.hide();
                    m_progressDialog = null;
                }
            }
            else
            {
                if (m_eventsReceiver != null)
                    m_eventsReceiver.onUpdateError(mContext.getString(R.string.update_error_apk_error));
                onApkFileError(uriFileName);
            }
        }
    }
    public void onDownloadError(String uriFileName, Exception e)
    {
        String s = uriFileName;

        //s +=".apk"; //for test

        s = s.toUpperCase();
        if (s.indexOf(".XML")>=0)
        {
            if (m_eventsReceiver != null)
                m_eventsReceiver.onUpdateError(mContext.getString(R.string.update_error_xml_error));
            KDSToast.showMessage(mContext, mContext.getString(R.string.update_error_xml_error));

        }
        else if (s.indexOf(".APK")>=0)
        {
            onApkFileError(uriFileName);
        }
    }

    private void onApkFileError(String uriFileName)
    {
        String nextAppLink = "";
        int nCurrentIndex = -1;

        m_progressDialog.hide();
        m_progressDialog = null;

        for (int i=0; i< m_appUpdateInfo.getAppLinks().size(); i++)
        {
            if (m_appUpdateInfo.getAppLinks().get(i).equals(uriFileName))
            {
                nCurrentIndex = i;
            }
        }
        if (nCurrentIndex < 0)
        {
            //KDSToast.showMessage(mContext, "Errors while downloading apk file");
            if (m_eventsReceiver != null)
                m_eventsReceiver.onUpdateError(mContext.getString(R.string.update_error_download_error));
        }
        else if (nCurrentIndex >= m_appUpdateInfo.getAppLinks().size()-1)
        {
            //KDSToast.showMessage(mContext, "Can not download apk file");
            if (m_eventsReceiver != null)
                m_eventsReceiver.onUpdateError(mContext.getString(R.string.update_error_apk_error) );
        }

        else
        {
            nCurrentIndex ++;
            if (nCurrentIndex < m_appUpdateInfo.getAppLinks().size()) {
                nextAppLink = m_appUpdateInfo.getAppLinks().get(nCurrentIndex);
                downloadFile(nextAppLink, buildLocalApkFilePathName(nextAppLink), true);
            }
        }
    }
    private boolean checkApkValid(String uriApkFileName, String localApkFileName)
    {
        long nlenght = KDSUtil.getFileLength(localApkFileName);
        if (nlenght<1024000)
            return false;
        return true;
    }

    private void afterUpdateLogDownloaded(String strLocalXmlFile)
    {
        m_appUpdateInfo = UpdateAppInfo.parseFile(strLocalXmlFile);
        if (!m_appUpdateInfo.isValid())
        {
            if (m_eventsReceiver != null)
                m_eventsReceiver.onUpdateError(mContext.getString(R.string.invalid_update_file));
            return ;
        }
        String currentVersionName = getVersionName();
        if (m_appUpdateInfo.getVersion().isOlderThanMine(currentVersionName))
        {
            if (m_eventsReceiver != null)
                m_eventsReceiver.onNewVersionValid(m_appUpdateInfo.getVersion().toString());
            showNoticeDialog(m_appUpdateInfo);
        }
        else
        {
            if (m_eventsReceiver != null)
                m_eventsReceiver.onNoNewVersionApk();
        }


    }
    private void afterUpdateApkDownloaded(String strLocalApkFile)
    {
        installApk(strLocalApkFile);
    }

    //外部接口让主Activity调用
    public void checkUpdateInfo(String appApkName){
        //hasRootPerssion();
        m_appUpdateInfo = null;
        m_strAppApkName = appApkName;
        //for test
        //m_strAppApkName = "2722068.html";
        checkIfNeedUpdate();
        //showNoticeDialog();
    }



    private String buildUpdateLogFileUriPathName()
    {
        String s = m_strAppApkName + ".xml";
        //for test
        //s = m_strAppApkName;

        s = UPDATE_LOG_URI_FOLDER + s;
        return s;
    }



    private String buildLocalUpdateLogFilePathName()
    {
        String s = m_strAppApkName + ".xml";
        //for test
       // s = m_strAppApkName;

        s =  buildLocalUpdateLogFolder() + s;
        //for test
        //s =  buildLocalUpdateLogFolder() + s+".xml";
        return s;
    }

    private String buildLocalUpdateLogFolder()
    {
       String s =  Environment.getExternalStorageDirectory() + "/" + m_saveLocalFolder;
        return s;

    }

    private void checkIfNeedUpdate()
    {
        downloadFile(buildUpdateLogFileUriPathName(),buildLocalUpdateLogFilePathName(), false);
    }
    private void downloadFile(String strUriFilePathName, String strLocalFilePathName, boolean showProgressDialog)
    {
        m_uriDownload.downloadFile(strUriFilePathName, strLocalFilePathName);

        if (showProgressDialog)
        {
            showProgressDialog(KDSApplication.getContext().getString(R.string.downloading), strUriFilePathName);
        }
    }

    public void cancel()
    {
       m_uriDownload.cancel();
        if (m_progressDialog!= null)
            m_progressDialog.hide();
    }


    private void showNoticeDialog(UpdateAppInfo info){
        Builder builder = new Builder(mContext);

        String title = mContext.getString(R.string.update_new_version);
        title = title.replace("#", info.getVersion().toString());
        title = title.replace("$", KDSUtil.convertIntToString(info.getRate()));
        builder.setTitle(title);

        //builder.setTitle(mContext.getString(R.string.update));
        String msg = mContext.getString(R.string.do_you_update);
        msg +="\n";
        msg += info.getNewFeaturesStrings();
        builder.setMessage(msg);
        builder.setPositiveButton(mContext.getString(R.string.update_now), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                downloadApk();
                //showProgressDialog();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.update_later), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (m_eventsReceiver != null)
                    m_eventsReceiver.onUpdateCanceled();
            }
        });
        noticeDialog = builder.create();
        noticeDialog.show();
    }

    private void downloadApk()
    {
        if (m_appUpdateInfo == null) return;
        String strAppLink = "";
        if (m_appUpdateInfo.getAppLinks().size() >0)
            strAppLink = m_appUpdateInfo.getAppLinks().get(0);

        downloadFile(strAppLink, buildLocalApkFilePathName(strAppLink), true);
    }
    private String buildLocalApkFilePathName(String uriApkLink)
    {
        String s = uriApkLink;
        int n = s.lastIndexOf("/");
        if (n<0)
            n = s.lastIndexOf("\\");
        String localFile = buildLocalUpdateLogFolder();
        if (n <0)
            localFile += m_strAppApkName + ".apk";
        else
            localFile += s.substring(n +1);
        return localFile;
    }
    private void showProgressDialog(String title, String message){

       // if (m_progressDialog == null)
        {
            m_progressDialog = new ProgressDialog(mContext);
            m_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            //m_progressDlg.setCancelable(true);
            m_progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,KDSApplication.getContext().getString(R.string.cancel), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancel();
                    if (m_eventsReceiver != null)
                        m_eventsReceiver.onUpdateCanceled();
                }
            });

        }
        m_progressDialog.setMax(100);
        m_progressDialog.setProgress(0);
        m_progressDialog.setTitle(title);
        m_progressDialog.setMessage(message);
        m_progressDialog.show();
        //downloadApk();
    }


    /**
     * 安装apk
     * @param
     */
    private void installApk(String apkFilePathName){
        if (m_eventsReceiver!= null)
            m_eventsReceiver.onUpdateError("");
        File apkfile = new File(apkFilePathName);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);

    }

    private String getVersionName() {
        String appVersion = "";
        PackageManager manager = mContext.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
            appVersion = info.versionName; //version name, set it in build.gradle file.
            //or [App properties(right click)]-->[open module settings]-->app-->flavors-->version name
        } catch (Exception e) {

            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        return appVersion;
    }

//    /**
//     * 判断手机是否有root权限
//     */
//    private static boolean hasRootPerssion(){
//        PrintWriter PrintWriter = null;
//        Process process = null;
//        try {
//            process = Runtime.getRuntime().exec("su");
//            PrintWriter = new PrintWriter(process.getOutputStream());
//            PrintWriter.flush();
//            PrintWriter.close();
//            int value = process.waitFor();
//            return returnResult(value);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }finally{
//            if(process!=null){
//                process.destroy();
//            }
//        }
//        return false;
//    }
    private static boolean returnResult(int value){
        // 代表成功
        if (value == 0) {
            return true;
        } else if (value == 1) { // 失败
            return false;
        } else { // 未知情况
            return false;
        }
    }
}
