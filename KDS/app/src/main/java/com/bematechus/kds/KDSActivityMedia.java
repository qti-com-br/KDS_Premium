package com.bematechus.kds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
//import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bematechus.kdslib.CSVStrings;
import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSSmbFile;
import com.bematechus.kdslib.KDSTimer;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.TimeDog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import jcifs.smb.SmbFile;

/**
 * Media player
 */
public class KDSActivityMedia extends Activity implements KDSTimer.KDSTimerInterface ,MediaHandler.MediaEventReceiver {

    static public String TAG = "KDSActivityMedia";
    private enum STATE
    {
        Playing,
        Pause,

    }

    private enum MEDIA
    {
        image,
        video,
    }

    ImageView m_imageView = null;
    VideoView m_videoView = null;
    Button m_btnPlayPause = null;
    Button m_btnStop = null;
    Button m_btnPrev = null;
    Button m_btnNext = null;

    Button m_btnBackward = null;
    Button m_btnForward = null;

    Button m_btnVolIncrease = null;
    Button m_btnVolDecrease = null;
    ProgressBar m_pbVol = null;
    ProgressBar m_pbProgress = null;
    LinearLayout m_llMain = null;

    TextView m_txtInfo = null;

    ArrayList<String> m_files = new ArrayList<>();
    int m_nCurrentFileIndex = 0;
    KDSTimer m_timer = new KDSTimer();
    boolean m_bPauseImageSlipTimer = false;
    boolean m_bPauseVideoProgressTimer = false;

    KDSKbdRecorder m_kbdRecorder = new KDSKbdRecorder();

    MediaHandler m_handler = new MediaHandler(this);

    int m_nAutoSlipDelay = 5;
    int m_nDefaultVol = 0;


    public void setPauseImageSlipTimer(boolean bPause) {
        m_bPauseImageSlipTimer = bPause;

        m_imageSlipTimeDog.reset();
    }

    public boolean getPauseImageSlipTimer() {
        return m_bPauseImageSlipTimer;
    }

    TimeDog m_imageSlipTimeDog = new TimeDog();
    TimeDog m_imageProgressTimeDog = new TimeDog();

    TimeDog m_videoProgressTimeDog = new TimeDog();

    TimeDog m_videoStartPlayingTimeDog = new TimeDog();

    public void onTime() {
        if (!getPauseImageSlipTimer()) {
            if (isPlayingImage()) {
                if (m_nAutoSlipDelay <= 0) return;
                if (m_imageSlipTimeDog.is_timeout(m_nAutoSlipDelay * 1000)) {
                    m_imageSlipTimeDog.reset();
                    next();
                } else {
                    if (m_imageProgressTimeDog.is_timeout(500)) {
                        m_imageProgressTimeDog.reset();
                        updateImageProgress();
                    }
                }
            }
        }
        if (!m_bPauseVideoProgressTimer) {
            if (isPlayingVideo()) {
                if (m_videoProgressTimeDog.is_timeout(500)) {
                    updateVideoProgress();
                    m_videoProgressTimeDog.reset();

                }
                checkVideoTimeout();
            }
        }
    }

    public void updateImageProgress() {
        if (m_nAutoSlipDelay <= 0) return;
        int n = m_imageSlipTimeDog.currentTimeOutSeconds();

        m_pbProgress.setMax(m_nAutoSlipDelay);//DEFAULT_NEXT_INTERVAL/1000);
        if (n + 1 <= m_nAutoSlipDelay)
            m_pbProgress.setProgress(n + 1);
        else
            m_pbProgress.setProgress(m_nAutoSlipDelay);


    }

    public void updateVideoProgress() {
        float flt = getVideoProgressPercent();
        m_pbProgress.setMax(100);
        m_pbProgress.setProgress((int) (flt * 100));

    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {


        boolean b = keyPressed(keyCode, event);
        //m_kbdRecorder.onKeyUp(keyCode);
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+ "KeyUp=" + keyCode);
        return b;
        //return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //Log.i("key pressed", String.valueOf(event.getKeyCode()));
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"dispatchKeyEvent event=" + event);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // Log.d(TAG, "dispatchKeyEvent down=" + event.getKeyCode() );
            if (event.getRepeatCount() == 0)
                m_kbdRecorder.onKeyDown(event.getKeyCode());
            return super.dispatchKeyEvent(event);
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            boolean b = super.dispatchKeyEvent(event);
            //Log.d(TAG, "dispatchKeyEvent up=" + event.getKeyCode() );
            m_kbdRecorder.onKeyUp(event.getKeyCode());
            return b;
        } else
            return super.dispatchKeyEvent(event);

    }

    private boolean keyPressed(int keyCode, KeyEvent event) {
        // if (!m_kbdRecorder.isReadyForEvent()) {
        if (!m_kbdRecorder.isAnyKeyDown()) {
//            if (m_kbdRecorder.isKeyupTimeout())
//                m_kbdRecorder.clear();
//            else
            return false;
        }
        //showInfo(KDSUtil.convertIntToString(keyCode));
        KDSLog.d(TAG, KDSLog._FUNCLINE_()+"KeyPressed=" + KDSUtil.convertIntToString(keyCode));
        m_kbdRecorder.debug("keyPressed");
        if (isPlayingVideo()) {
            return checkVideoKeycodeEvent(event);
        } else if (isPlayingImage()) {
            return checkImageKeycodeEvent(event);
        }
        return false;

    }

    private boolean checkVideoKeycodeEvent(KeyEvent event) {
        int nkeycode = event.getKeyCode();
        switch (nkeycode) {
            case KeyEvent.KEYCODE_1: {
                onBtnPlayPauseClicked(null);
                return true;
            }
            case KeyEvent.KEYCODE_2: {
                onBtnStopClicked(null);
                return true;
            }
            case KeyEvent.KEYCODE_3: {
                onBtnPrevClicked(null);
                return true;
            }
            case KeyEvent.KEYCODE_4: {
                onBtnBackwardClicked(null);
                return true;
            }
            case KeyEvent.KEYCODE_5: {
                onBtnForwardClicked(null);
                return true;
            }
            case KeyEvent.KEYCODE_6: {
                onBtnNextClicked(null);
                return true;
            }
            case KeyEvent.KEYCODE_7: {
                onBtnVolDecreaseClicked(null);
                return true;
            }
            case KeyEvent.KEYCODE_8: {
                onBtnVolIncreaseClicked(null);
                return true;
            }
        }
        return false;

    }

    private boolean checkImageKeycodeEvent(KeyEvent event) {
        int nkeycode = event.getKeyCode();
        switch (nkeycode) {
            case KeyEvent.KEYCODE_1: {
                onBtnPlayPauseClicked(null);
                return true;
            }
            case KeyEvent.KEYCODE_2: {
                onBtnStopClicked(null);
                return true;
            }
            case KeyEvent.KEYCODE_3: {
                onBtnPrevClicked(null);
                return true;
            }
//            case KeyEvent.KEYCODE_4:
//            {
//                onBtnBackwardClicked(null);
//                return true;
//            }
//            case KeyEvent.KEYCODE_5:
//            {
//                onBtnForwardClicked(null);
//                return true;
//            }
            case KeyEvent.KEYCODE_4: {
                onBtnNextClicked(null);
                return true;
            }
//            case KeyEvent.KEYCODE_7:
//            {
//                onBtnVolIncreaseClicked(null);
//                return true;
//            }
//            case KeyEvent.KEYCODE_8:
//            {
//                onBtnVolDecreaseClicked(null);
//                return true;
//            }
        }
        return false;

    }

    private boolean isPlayingVideo() {
        return (m_videoView.getVisibility() == View.VISIBLE);

    }

    private boolean isPlayingImage() {
        return (m_imageView.getVisibility() == View.VISIBLE);
    }

    protected void onPause() {
        super.onPause();
        setVol(volMax());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_media);


        int btnAlpha = 10;
        m_imageView = (ImageView) this.findViewById(R.id.imageView);
        m_videoView = (VideoView) this.findViewById(R.id.videoView);

        m_btnPlayPause = (Button) this.findViewById(R.id.btnPlayPause);
        m_btnPlayPause.getBackground().setAlpha(btnAlpha);

        m_btnStop = (Button) this.findViewById(R.id.btnStop);
        m_btnStop.getBackground().setAlpha(btnAlpha);

        m_btnPrev = (Button) this.findViewById(R.id.btnPrev);
        m_btnPrev.getBackground().setAlpha(btnAlpha);

        m_btnNext = (Button) this.findViewById(R.id.btnNext);
        m_btnNext.getBackground().setAlpha(btnAlpha);

        m_btnBackward = (Button) this.findViewById(R.id.btnBackward);
        ;
        m_btnBackward.getBackground().setAlpha(btnAlpha);
        m_btnForward = (Button) this.findViewById(R.id.btnForward);
        ;
        m_btnForward.getBackground().setAlpha(btnAlpha);

        m_btnVolIncrease = (Button) this.findViewById(R.id.btnVolIncrease);
        m_btnVolIncrease.getBackground().setAlpha(btnAlpha);

        m_btnVolDecrease = (Button) this.findViewById(R.id.btnVolDecrease);
        m_btnVolDecrease.getBackground().setAlpha(btnAlpha);

        m_pbVol = (ProgressBar) this.findViewById(R.id.pbVol);
        m_pbProgress = (ProgressBar) this.findViewById(R.id.pbProgress);

        m_llMain = (LinearLayout) this.findViewById(R.id.llMain);

        m_txtInfo = (TextView) this.findViewById(R.id.txtInfo);

        m_nAutoSlipDelay = KDSGlobalVariables.getKDS().getSettings().getInt(KDSSettings.ID.Media_auto_slip_interval);
        m_nDefaultVol = KDSGlobalVariables.getKDS().getSettings().getInt(KDSSettings.ID.Media_default_vol);

        //m_btnPlayPause..setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.pause));
        setState(STATE.Playing);
//        Drawable drawable = this.getResources().getDrawable(R.drawable.pause );
//
//        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
//        m_btnPlayPause.setCompoundDrawables( null, drawable, null, null);

        setVol(m_nDefaultVol);
        init_video_view();

        showByIntent();
    }

    public void showByIntent() {
        Intent intent = this.getIntent();
        String files = intent.getStringExtra("files");
        CSVStrings medias = CSVStrings.parse(files);

        playFiles(medias.getArray());
    }

    private boolean isVideo(String path) {
        if (path.toLowerCase().endsWith(".mov")
                || path.toLowerCase().endsWith(".mkv")
                || path.toLowerCase().endsWith(".mp4")
                || path.toLowerCase().endsWith(".avi")) {

            return true;

        }
        return false;
    }

    private boolean isImage(String path) {
        if (path.toLowerCase().endsWith(".jpg")
                || path.toLowerCase().endsWith(".gif")
                || path.toLowerCase().endsWith(".png")
                || path.toLowerCase().endsWith(".bmp")
                || path.toLowerCase().endsWith(".jpeg")) {

            return true;

        }
        return false;
    }

    private boolean enableVideoGui(boolean bEnableVideo) {

        int nVisiblityImage = View.GONE;
        int nVisiblityVideo = View.GONE;
        if (bEnableVideo) {
            //show video
            m_bPauseVideoProgressTimer = false;
            setPauseImageSlipTimer(true);
            //m_bPauseImageSlipTimer = true;
            nVisiblityVideo = View.VISIBLE;
            nVisiblityImage = View.GONE;
        } else {
            m_bPauseVideoProgressTimer = true;
            setPauseImageSlipTimer(false);
            //m_bPauseImageSlipTimer = false;
            nVisiblityVideo = View.GONE;
            nVisiblityImage = View.VISIBLE;
        }

        m_imageView.setVisibility(nVisiblityImage);
        m_videoView.setVisibility(nVisiblityVideo);
        m_btnPlayPause.setVisibility(View.VISIBLE);
        m_btnStop.setVisibility(View.VISIBLE);
        m_btnPrev.setVisibility(View.VISIBLE);
        m_btnNext.setVisibility(View.VISIBLE);
        m_btnVolIncrease.setVisibility(nVisiblityVideo);
        m_btnVolDecrease.setVisibility(nVisiblityVideo);
        m_btnForward.setVisibility(nVisiblityVideo);
        m_btnBackward.setVisibility(nVisiblityVideo);
        m_pbVol.setVisibility(nVisiblityVideo);
        m_pbProgress.setVisibility(View.VISIBLE);

        return true;

    }

    /**
     * Load lcoal image
     *
     * @param url e.g: /aa/bb/c.jpg
     * @return
     */
    public static Bitmap getLocalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),  e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return null;
        }
    }

    /**
     * Load image from http server
     *
     * @param url e.g: http://blog.3gstdy.com/wp-content/themes/twentyten/images/headers/path.jpg
     * @return
     */
    public static Bitmap getHttpBitmap(String url) {
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
            conn.setConnectTimeout(0);
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        return bitmap;
    }

    public static Bitmap getSmbBitmap(String url) {
        Bitmap bitmap = null;
        try {
            SmbFile f = new SmbFile(url);
            InputStream is = f.getInputStream();

            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),  e);
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        return bitmap;

    }

    private boolean createTempFolder() {
        String targetFolder = getTempFolder();
        return KDSUtil.createFolder(targetFolder);
//        String targetFolder = DEFAULT_BACKUP_FOLDER;
//        boolean breturn = KDSUtil.createInternalFolder(this.getApplicationContext(), targetFolder);
//        return breturn;

    }

    final private String DEFAULT_TEMP_FOLDER = "KDSTemp";

    private String getTempFolder() {
        //return Environment.getExternalStorageDirectory() + "/" + DEFAULT_TEMP_FOLDER;
        return KDSUtil.getBaseDirCanUninstall() + "/" + DEFAULT_TEMP_FOLDER;
    }

    /**
     * Download ethernet smb file to local
     *
     * @param url
     * @return
     */
    public String getSmbFile(String url) {

        try {
            createTempFolder();
            // KDSSmbFile.readFromSmb()
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


    public boolean isLocalFile(String fileName) {
        if (fileName.indexOf("/") == 0)
            return true;
        return false;

    }

    public boolean isSmbFile(String fileName) {
        if (fileName.indexOf("smb://") == 0)
            return true;
        return false;

    }

    public String convertSmbFileToLocalTempFileName(String smbFileName)
    {
        int n = smbFileName.lastIndexOf("/");
        String s = smbFileName.substring(n);
        String folder = getTempFolder();
        String filePath = folder + File.separator + s;
        return filePath;
    }
    public boolean isSmbFileExistedInTempFolder(String smbFileName)
    {

        return KDSUtil.fileExisted(convertSmbFileToLocalTempFileName(smbFileName));
    }
    public void downloadSmbFile(String smbFileName) {

        setPauseImageSlipTimer(true);
        if (isSmbFileExistedInTempFolder(smbFileName))
        {
            m_handler.sendSmbDownloadedMessage(convertSmbFileToLocalTempFileName(smbFileName));
            return;
        }
        m_txtInfo.setText(this.getString(R.string.downloading));
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

    private void pauseAllProgress(boolean bPause)
    {

         m_bPauseVideoProgressTimer = bPause;
         m_bPauseImageSlipTimer = bPause;

    }

    private void setShortCut(MEDIA media)
    {
        switch (media)
        {

            case image:
                m_btnNext.setText("[4]");
                break;
            case video:

                m_btnNext.setText("[6]");
                break;
        }
    }

    Bitmap m_internetBmp = null;
   // String m_internetFile = "";
    private boolean playImage(String fileName)
    {
        m_txtInfo.setText("");
        enableVideoGui(false);
        setShortCut(MEDIA.image);
        Bitmap bmp = null;
        if (isLocalFile(fileName)) {
            setPauseImageSlipTimer(false);
            bmp = getLocalBitmap(fileName);
        }
        else if (isSmbFile(fileName))
        {
            pauseAllProgress(true);
            setPauseImageSlipTimer(true);
            downloadSmbFile(fileName);
            return true;
        }
        else { //http
           // m_internetFile = fileName;
            setPauseImageSlipTimer(true);
            m_internetBmp = null;
            Object[] objs = new Object[]{fileName};
            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    String httpFileName = (String)params[0];
                    m_internetBmp = getHttpBitmap(httpFileName);
                    m_handler.sendHttpBitmapDownloadedMessage();
                    return null;
                }
            };
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, objs);
            return true;
//            for (int i=0; i< 1000; i++)
//            {
//                 if (m_internetBmp !=null ) {
//                     bmp = m_internetBmp;
//                     break;
//                 }
//                else
//                 {
//                     try {
//                         Thread.sleep(10);
//                     }
//                     catch (Exception e)
//                     {
//                         e.printStackTrace();
//                     }
//                 }
//            }
            //bmp = m_internetBmp;
        }

        m_imageView.setImageBitmap(bmp);
        return (bmp != null);
    }

    private void init_video_view()
    {
        //设置视频控制器
       // m_videoView.setMediaController(new MediaController(this));
        //播放完成回调
        m_videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next();
            }
        });

        m_videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                String strError = "";
                if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN)
                {
                    strError = "Unknown error.";
                }
                else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED)
                {
                    strError = "Player died";
                }
                if (extra == MediaPlayer.MEDIA_ERROR_IO)
                {
                    strError += " File or network related operation errors.";
                }
                else if (extra ==MediaPlayer.MEDIA_ERROR_MALFORMED)
                {
                    strError += " Bitstream is not conforming to the related coding standard or file spec";
                }
                else if (extra == MediaPlayer.MEDIA_ERROR_UNSUPPORTED)
                {
                    strError += "Ehe media does not support";
                }
                else if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT)
                {
                    strError += "Some operation takes too long to complete";
                }
                m_txtInfo.setText(strError);
                return true;
            }
        });




        //m_videoView.setOn.getHolder().setFixedSize(500, 500);//.setSizeFromLayout();
    }
    /**
     * http, or rtsp
     * @param fileNameUri
     * local:  file:///sdcard/test.mp4
     * http:   http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4
     */
    private boolean playVideo(String fileNameUri)
    {
        enableVideoGui(true);
        setShortCut(MEDIA.video);
        Uri uri = Uri.parse( fileNameUri );
        m_txtInfo.setText("");
        if (isSmbFile(fileNameUri))
        {
            pauseAllProgress(true);
            setPauseImageSlipTimer(true);
            downloadSmbFile(fileNameUri);
        }
        else {
            //设置视频路径
            m_videoView.setVideoURI(uri);
            m_videoView.start();
        }
        m_pbVol.setMax(volMax());
        m_pbVol.setProgress(volCurrent());
        m_videoStartPlayingTimeDog.reset();

        return true;
    }

    public boolean playFile(String uri)
    {
        m_txtInfo.setText("");
        if (isImage(uri)) {
            //setPauseImageSlipTimer(false);
            if (playImage(uri)) {

                return true;
            }
            else
            {
                m_txtInfo.setText(this.getString(R.string.invalid_image_file) + uri);
                return false;
            }


        }
        else if (isVideo(uri)) {
            //m_txtInfo.setText("");
            setPauseImageSlipTimer(true);
            return (playVideo(uri));

        }
        else
        {
            m_txtInfo.setText(getString(R.string.unknown_file_format) + uri);
        }
        return true;


    }
    public void playFiles(ArrayList<String> files)
    {
        m_files.clear();
        m_files.addAll(files);
        if (files.size() <=0) return;
        m_nCurrentFileIndex = -1;
        setPauseImageSlipTimer(true);
        m_bPauseVideoProgressTimer = true;
        m_timer.start(this, this, 500);
        next();
        //String fileName = files.get(0);
        //playFile(fileName);

    }

    public void playFileByIndex(int nIndex)
    {
        if (nIndex<0 || nIndex>= m_files.size())
            return;
        String file = m_files.get(nIndex);
        playFile(file);

    }

    public int getCount()
    {
        return m_files.size();
    }

    public void next()
    {
        m_nCurrentFileIndex++;
        if (m_nCurrentFileIndex >= m_files.size()) {

            m_nCurrentFileIndex = 0;
            pause();
            return;
        }
        playFileByIndex(m_nCurrentFileIndex);
        setState(STATE.Playing);
        //play();
    }
    public void prev()
    {
        m_nCurrentFileIndex--;
        if (m_nCurrentFileIndex <0)
            m_nCurrentFileIndex = m_files.size()-1;
        playFileByIndex(m_nCurrentFileIndex);
        setState(STATE.Playing);
        //play();
    }

    public String getCurrentFileName()
    {
        if (m_nCurrentFileIndex <0) return "";
        if (m_nCurrentFileIndex >= m_files.size()) return "";
        return m_files.get(m_nCurrentFileIndex);
    }

    static public int VOL_STEP = 1;
    public void volIncrease()
    {
        //
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //max
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //current
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        currentVolume+=VOL_STEP;
        if (currentVolume > maxVolume)
            currentVolume = maxVolume;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
        m_pbVol.setProgress(volCurrent());
    }

    public void setVol(int nVol)
    {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nVol, 0);

        m_pbVol.setProgress(volCurrent());
    }
    public void volDecrease()
    {
        //
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //max
        //int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //current
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        currentVolume-=VOL_STEP;
        if (currentVolume <0)
            currentVolume = 0;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);

        m_pbVol.setProgress(volCurrent());

    }
    public int volMax()
    {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //max
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        return maxVolume;
    }
    public int volCurrent()
    {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return currentVolume;
    }
    public int getVideoProgress()
    {
        return m_videoView.getCurrentPosition();
    }
    public int getVideoDuration()
    {
        return m_videoView.getDuration();
    }

    public float getVideoProgressPercent()
    {
        int duration = getVideoDuration();
        int progress = getVideoProgress();
        if (duration == 0) return 0;
        return ((float)progress/(float)duration);
    }
    static final int VIDEO_TIMEOUT = 10000;

    public void checkVideoTimeout()
    {
        int duration = getVideoDuration();
        if (duration <=0) {
            m_txtInfo.setText(getString(R.string.downloading));
            if (m_videoStartPlayingTimeDog.is_timeout(VIDEO_TIMEOUT))
            {
                pause();
                String s = this.getString(R.string.can_not_get_file);
                s = s.replace("#", getCurrentFileName());
                m_txtInfo.setText(s);//"Can not get : "+ getCurrentFileName());
                m_videoView.stopPlayback();

            }
        }
        else
            m_txtInfo.setText("");

    }

    public void pause()
    {
       // m_btnPlayPause.setText("Play");
        m_bIsPlayButtonFunction = false;
        if (m_imageView.getVisibility() == View.VISIBLE)
        {//images
            m_bPauseImageSlipTimer = true;
        }
        else
        {//video
            m_bPauseVideoProgressTimer = true;
            m_videoView.pause();

        }
        setState(STATE.Pause);

        //m_btnPlayPause.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.playpause));
    }

    private void setState(STATE state)
    {
        switch (state)
        {

            case Playing:
                Drawable drawable = this.getResources().getDrawable(R.drawable.pause );

                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                m_btnPlayPause.setCompoundDrawables( null, drawable, null, null);
                if (isPlayingVideo())
                    m_videoStartPlayingTimeDog.reset();
                //m_btnPlayPause.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.pause));
                break;
            case Pause:
                Drawable drawable2 = this.getResources().getDrawable(R.drawable.playpause );

                drawable2.setBounds(0, 0, drawable2.getMinimumWidth(), drawable2.getMinimumHeight());
                m_btnPlayPause.setCompoundDrawables( null, drawable2, null, null);

                //m_btnPlayPause.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.playpause));
                break;
        }
    }

    public void play()
    {
        m_bIsPlayButtonFunction = true;
        //m_btnPlayPause.setText("Pause");
        if (m_imageView.getVisibility() == View.VISIBLE)
        {//images
            m_imageSlipTimeDog.reset();
            setPauseImageSlipTimer(false);
            //m_bPauseImageSlipTimer = false;
        }
        else
        {//video
            m_bPauseVideoProgressTimer = false;
            m_videoView.start();

        }
        setState(STATE.Playing);
        //m_btnPlayPause.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.pause));
    }

    boolean m_bIsPlayButtonFunction = true;
    public void onBtnPlayPauseClicked(View v)
    {
        m_bIsPlayButtonFunction = (!m_bIsPlayButtonFunction);
        if (m_bIsPlayButtonFunction) {
            play();


        }
        else {
            pause();

        }
    }

    public void onBtnStopClicked(View v)
    {
        setResult(RESULT_CANCELED);
        this.finish();
    }

    public void onBtnPrevClicked(View v)
    {
        prev();
    }

    public void onBtnNextClicked(View v)
    {
        next();
    }
    static private int VIDEO_STEP = 5;
    public void onBtnForwardClicked(View v)
    {
        int n = m_videoView.getCurrentPosition();
        n += (VIDEO_STEP*1000);
        if (n >m_videoView.getDuration())
            n = m_videoView.getDuration();
        m_videoView.seekTo(n);

    }

    public void onBtnBackwardClicked(View v)
    {
        int n = m_videoView.getCurrentPosition();
        n -= (VIDEO_STEP*1000);
        if (n <0)
            n = 0;
        m_videoView.seekTo(n);
    }
    private void setInfo(String s)
    {
        m_txtInfo.setText(s);

    }
    private void clearInfo()
    {
        setInfo("");
    }
    public void onBtnVolIncreaseClicked(View v)
    {
        volIncrease();
    }

    public void onBtnVolDecreaseClicked(View v)
    {
        volDecrease();
    }

    public void medaievent_onSmbFileDownloaded(String localFileName)
    {
        clearInfo();
        if (isImage(localFileName)) {
            setPauseImageSlipTimer(false);
            Bitmap bmp = getLocalBitmap(localFileName);
            if (bmp == null) {
                setInfo(this.getString(R.string.invalid_image_file) + getCurrentFileName());
                return;
            }
            m_imageView.setImageBitmap(bmp);
        }
        else if (isVideo(localFileName))
        {

            setPauseImageSlipTimer(true);
            Uri uri = Uri.parse( localFileName );
            m_videoView.setVideoURI(uri);
            m_videoView.start();

        }
    }

    public void medaievent_onHttpBitmapFileDownloaded()
    {
        clearInfo();
        setPauseImageSlipTimer(false);
        m_imageView.setImageBitmap(m_internetBmp);
        if (m_internetBmp == null)
        {

            setInfo(this.getString(R.string.invalid_image_file) + getCurrentFileName());
        }
    }



}
