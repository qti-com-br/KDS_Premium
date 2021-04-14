package com.bematechus.kds;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSSmbFile;
import com.bematechus.kdslib.KDSUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageUtil {

    static public String TAG = "ImageUtil";

    static public boolean isLocalFile(String fileName) {
        if (fileName.indexOf("/") == 0)
            return true;
        return false;

    }

    static public boolean isSmbFile(String fileName) {
        if (fileName.indexOf("smb://") == 0)
            return true;
        return false;

    }

    static public boolean isInternetFile(String fileName)
    {
        if (!isSmbFile(fileName) && !isLocalFile(fileName))
            return true;
        return false;
    }

    static public boolean createTempFolder() {
        String targetFolder = getTempFolder();
        return KDSUtil.createFolder(targetFolder);

    }

    static final private String DEFAULT_TEMP_FOLDER = "KDSTemp";

    static public String getTempFolder() {

        return KDSUtil.getBaseDirCanUninstall() + "/" + DEFAULT_TEMP_FOLDER;
    }

    static public String convertSmbFileToLocalTempFileName(String smbFileName)
    {
        int n = smbFileName.lastIndexOf("/");
        String s = smbFileName.substring(n);
        String folder = getTempFolder();
        String filePath = folder + File.separator + s;
        return filePath;
    }

    static public boolean isSmbFileExistedInTempFolder(String smbFileName)
    {

        return KDSUtil.fileExisted(convertSmbFileToLocalTempFileName(smbFileName));
    }

    /**
     * Load lcoal image
     *
     * @param url e.g: /aa/bb/c.jpg
     * @return
     */
    static public Bitmap getLocalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),  e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return null;
        }
    }

    static MediaHandler m_handler = new MediaHandler(null);

    static public void downloadSmbFile(String smbFileName, MediaHandler.MediaEventReceiver receiver) {

        //setPauseImageSlipTimer(true);
        m_handler.setReceiver(receiver);
        if (isSmbFileExistedInTempFolder(smbFileName))
        {
            m_handler.sendSmbDownloadedMessage(convertSmbFileToLocalTempFileName(smbFileName));
            return;
        }
        //m_txtInfo.setText(this.getView().getContext().getString(R.string.downloading));
        Object objs[] = new Object[]{smbFileName};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String name = (String) params[0];
                getSmbFile(name);
                return null;
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,objs);
    }

    /**
     * Download ethernet smb file to local
     *
     * @param url
     * @return
     */
    static public String getSmbFile(String url) {

        try {
            createTempFolder();
            // KDSSmbFile.readFromSmb()
            KDSSmbFile.smb_setEnableSmbV2(true);
            String filename = KDSSmbFile.smb_readFromSmbToLocal(url, getTempFolder());
            if (!filename.isEmpty())
                m_handler.sendSmbDownloadedMessage(filename);
            return filename;
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        return "";

    }

    /**
     * Load image from http server
     *
     * @param url e.g: http://blog.3gstdy.com/wp-content/themes/twentyten/images/headers/path.jpg
     * @return
     */
    public static Bitmap getHttpBitmap(String url, MediaHandler.MediaEventReceiver receiver) {

        m_handler.setReceiver(receiver);

        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            KDSLog.d(TAG,KDSLog._FUNCLINE_()+ url);
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),  e);
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setConnectTimeout(20000);
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        m_handler.sendHttpBitmapDownloadedMessage(bitmap);
        return bitmap;
    }

    static public boolean isImage(String path) {
        if (path.toLowerCase().endsWith(".jpg")
                || path.toLowerCase().endsWith(".gif")
                || path.toLowerCase().endsWith(".png")
                || path.toLowerCase().endsWith(".bmp")
                || path.toLowerCase().endsWith(".jpeg")) {

            return true;

        }
        return false;
    }

    static public boolean isVideo(String path) {
        if (path.toLowerCase().endsWith(".mov")
                || path.toLowerCase().endsWith(".mkv")
                || path.toLowerCase().endsWith(".mp4")
                || path.toLowerCase().endsWith(".avi")) {

            return true;

        }
        return false;
    }


    static public Bitmap getInternetImage(String path , MediaHandler.MediaEventReceiver receiver) throws Exception
    {
        m_handler.setReceiver(receiver);
        URL url =  new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setReadTimeout( 30*1000);
        conn.setConnectTimeout(30*1000);
        conn.setRequestMethod( "GET");
        InputStream is= null;
        if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
            is=conn.getInputStream();
        } else{
            is = null;
        }
        if ( is ==  null){
            return null;
        }else {
            try{
                byte[] data=readStream( is);
                if(data!= null){
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                    m_handler.sendHttpBitmapDownloadedMessage(bitmap);
                    return bitmap;
                }
            } catch(Exception e){
                e.printStackTrace();
            }
            is.close();
            return null;
        }
    }

    /*
     * get image buffer data
     *  */
    public static byte[] readStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream =  new ByteArrayOutputStream();
        byte[]buffer= new byte[102400];
        int len =  0;
        while( (len=inStream.read(buffer)) != -1){
            outStream.write(buffer,0,len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

}
