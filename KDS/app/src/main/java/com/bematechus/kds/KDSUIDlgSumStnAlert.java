package com.bematechus.kds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSSmbFile;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class KDSUIDlgSumStnAlert extends KDSUIDialogBase implements MediaHandler.MediaEventReceiver  {

    static public String TAG = "SumStnAlert";
    ImageView m_imageView = null;
    TextView m_txtInfo = null;
    SumStationAlertEntry mAlertentry = null;
    /**********************************************************************************/

    public KDSUIDlgSumStnAlert(final Context context, SumStationAlertEntry entry) {
       this.int_information_dialog(context, R.layout.kdsui_dlg_sumstn_alert);
        ((TextView)this.getView().findViewById(R.id.txtText)).setText(entry.getAlertMessage());
        m_imageView = (ImageView)this.getView().findViewById(R.id.imgImage);
        m_txtInfo = (TextView)this.getView().findViewById(R.id.txtInfo);
        mAlertentry = entry;

        String s = entry.getDescription();
        if (!entry.getDisplayText().isEmpty())
            s = entry.getDisplayText();
        ((TextView)this.getView().findViewById(R.id.txtTitle)).setText(s);
        if (!entry.getAlertImageFile().isEmpty())
            showImage(entry.getAlertImageFile());
        //((ImageView)this.getView().findViewById(R.id.imgImage)).setText(entry.getAlertMessage());
    }

    private void clearInfo()
    {
        m_txtInfo.setText("");;
    }

    private void setInfo(String s)
    {
        m_txtInfo.setText(s);
    }

    private String getResString(int resid)
    {
        return this.getView().getContext().getString(resid);
    }
    public void medaievent_onHttpBitmapFileDownloaded(Bitmap bmp)
    {
        clearInfo();
        m_internetBmp = bmp;
        //setPauseImageSlipTimer(false);
        m_imageView.setImageBitmap(m_internetBmp);
        if (m_internetBmp == null)
        {

            setInfo(this.getView().getContext().getString(R.string.invalid_image_file) );
        }
    }



    public void medaievent_onSmbFileDownloaded(String localFileName)
    {
        clearInfo();
        if (ImageUtil.isImage(localFileName)) {
            //setPauseImageSlipTimer(false);
            Bitmap bmp = ImageUtil.getLocalBitmap(localFileName);
            if (bmp == null) {
                setInfo(this.getView().getContext().getString(R.string.invalid_image_file) + localFileName);
                return;
            }
            m_imageView.setImageBitmap(bmp);
        }
    }


    Bitmap m_internetBmp = null;

    private boolean showImage(String fileName)
    {
        //m_txtInfo.setText("");
        //enableVideoGui(false);
        //setShortCut(KDSActivityMedia.MEDIA.image);
        Bitmap bmp = null;
        if (ImageUtil.isLocalFile(fileName)) {
            //setPauseImageSlipTimer(false);
            bmp = ImageUtil.getLocalBitmap(fileName);
        }
        else if (ImageUtil.isSmbFile(fileName))
        {
            //pauseAllProgress(true);
            //setPauseImageSlipTimer(true);
            setInfo(getResString(R.string.downloading));
            ImageUtil.downloadSmbFile(fileName, this);
            return true;
        }
        else { //http
            // m_internetFile = fileName;
            //setPauseImageSlipTimer(true);
            m_internetBmp = null;
            Object[] objs = new Object[]{fileName};
            setInfo(getResString(R.string.downloading));
            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    String httpFileName = (String)params[0];
                    //m_internetBmp = ImageUtil.getHttpBitmap(httpFileName, KDSUIDlgSumStnAlert.this);
                    try {
                        m_internetBmp = ImageUtil.getInternetImage(httpFileName, KDSUIDlgSumStnAlert.this);

                    }
                    catch (Exception e)
                    {

                    }

                    //ImageUtil.m_handler.sendHttpBitmapDownloadedMessage();
                    return null;
                }
            };
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, objs);
            return true;

        }

        m_imageView.setImageBitmap(bmp);
        return (bmp != null);
    }

    public void hide()
    {
        this.getDialog().hide();
    }

    public boolean isVisible()
    {
        return this.getDialog().isShowing();
    }

}
