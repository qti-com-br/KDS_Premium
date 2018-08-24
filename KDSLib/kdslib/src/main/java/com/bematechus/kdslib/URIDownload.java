package com.bematechus.kdslib;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

/**
 * Created by Administrator on 2016/11/7.
 */
public class URIDownload {

    public static final String TAG = "URIDownload";

    public interface URIDownloadEvent
    {
        public void onDownloadProgressUpdate(String uriFileName, String localFileName, int nPercent);
        public void onDownloadFinished(String uriFileName, String localFileName);
        public void onDownloadError(String uriFileName, Exception e);
    }
    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
    private static final int DOWN_ERROR = 3;
    //downing which file.
    private String m_downingFileName = "";
    private String m_downingFileSaveToFileName = "";// savePath + "UpdateDemoRelease.apk";
    private String m_localFolder = "kdsupdate/";

    private int m_progressPercent = 0;

    private Thread downLoadThread = null;

    private boolean m_bCanceledFlag = false;

    private URIDownloadEvent m_uriDownloadEventReceiver = null;
    public URIDownload(URIDownloadEvent eventReceiver, String localFolder)
    {
        m_uriDownloadEventReceiver = eventReceiver;
       m_localFolder = localFolder;
    }

    private String buildLocalUpdateLogFolder()
    {
        String s =  Environment.getExternalStorageDirectory() + "/" + m_localFolder;
        return s;

    }

    private Handler m_Handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    if (m_uriDownloadEventReceiver != null)
                        m_uriDownloadEventReceiver.onDownloadProgressUpdate(m_downingFileName, m_downingFileSaveToFileName, m_progressPercent);

//                    if (m_progressDialog != null)
//                        m_progressDialog.setProgress(m_progressPercent);
//                    //mProgress.setProgress(progress);
                    break;
                case DOWN_OVER:
                    if (m_uriDownloadEventReceiver!= null)
                        m_uriDownloadEventReceiver.onDownloadFinished(m_downingFileName, m_downingFileSaveToFileName);

                    //afterDownloadFinished();
                    //installApk();
                    break;
                case DOWN_ERROR:
                {
                    if (m_uriDownloadEventReceiver != null)
                    {
                        m_uriDownloadEventReceiver.onDownloadError(m_downingFileName, (Exception) msg.obj);
                    }
                }
                break;
                default:
                    break;
            }
        };
    };

    static final int READ_TIMEOUT = 5000;

    private Runnable m_downFileRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(m_downingFileName);

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(buildLocalUpdateLogFolder());
                if(!file.exists()){
                    file.mkdir();
                }
                String toFileName = m_downingFileSaveToFileName;
                File localFile = new File(toFileName);
                FileOutputStream fos = new FileOutputStream(localFile);

                int count = 0;
                byte buf[] = new byte[10240];

                do{
                    int numread = is.read(buf);
                    count += numread;
                    m_progressPercent =(int)(((float)count / (float) length) * 100);
                    //更新进度
                    m_Handler.sendEmptyMessage(DOWN_UPDATE);
                    if(numread <= 0){
                        //下载完成通知安装
                        m_Handler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf,0,numread);
                }while(!m_bCanceledFlag);//点击取消就停止下载.

                fos.close();
                is.close();

            }
             catch(Exception e){
                 KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
                Message msg = new Message();
                msg.what = DOWN_ERROR;
                msg.obj = e;
                m_Handler.sendMessage(msg);
                e.printStackTrace();
            }

        }
    };

    public void downloadFile(String strUriFilePathName, String strLocalFilePathName)
    {
        m_bCanceledFlag = false;
        m_downingFileName = strUriFilePathName;
        m_downingFileSaveToFileName= strLocalFilePathName;
        m_progressPercent = 0;
        downLoadThread = new Thread(m_downFileRunnable);
        downLoadThread.start();

    }

    public void cancel()
    {
        m_bCanceledFlag = true;
        try {

            downLoadThread.join();
            downLoadThread = null;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ KDSLog.getStackTrace(e));
            //e.printStackTrace();
        }
    }

}
