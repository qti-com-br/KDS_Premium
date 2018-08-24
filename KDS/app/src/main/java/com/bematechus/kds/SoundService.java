package com.bematechus.kds;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bematechus.kdslib.KDSLog;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class SoundService extends Service {
    private static final String TAG = "SoundService";

    public interface SoundEventsReceiver
    {
        public void SoundPlayFinished(String uri);
        public void SoundPlayStarted(String uri);
        public void SoundPlayStop(String uri);
    }

    public static final int PLAY_MSG = 1;
    public static final int PAUSE_MSG = 2;
    public static final int STOP_MSG = 3;


    private final IBinder binder = new MyBinder();
    private MediaPlayer mediaPlayer =  new MediaPlayer();
    private String path;
    private boolean isPause;

    SoundEventsReceiver m_eventsReceiver = null;

    public SoundService() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (m_eventsReceiver != null)
                    m_eventsReceiver.SoundPlayFinished(path);
            }
        });
    }

    public void setEventReceiver(SoundEventsReceiver receiver )
    {
        m_eventsReceiver = receiver;
    }




    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mediaPlayer.isPlaying()) {
            stop();
        }
        path = intent.getStringExtra("url");
        int msg = intent.getIntExtra("MSG", 0);
        if(msg == PLAY_MSG) {
            play(0);
        } else if(msg == PAUSE_MSG) {
            pause();
        } else if(msg == STOP_MSG) {
            stop();
        }
        return super.onStartCommand(intent, flags, startId);
    }
    public class MyBinder extends Binder
    {
        SoundService getService()
        {
            return SoundService.this;
        }
    }

    boolean m_bLoop = false;
    public void play(String uri)
    {
        m_bLoop = false;
        path = uri;
        play(0);
    }
    public void playLoop(String uri)
    {
        m_bLoop = true;
        path = uri;

        play(0);
    }
    /**
     * 播放音乐
     * @param position
     */
    private void play(int position) {
        try {
            mediaPlayer.reset();//把各项参数恢复到初始状态
            mediaPlayer.setDataSource(path);

            mediaPlayer.setLooping(m_bLoop);
            mediaPlayer.prepare();  //进行缓冲
            mediaPlayer.setOnPreparedListener(new PreparedListener(position));//注册一个监听器
        }
        catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //e.printStackTrace();
        }
    }

    /**
     * 暂停音乐
     */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
            if (m_eventsReceiver != null)
                m_eventsReceiver.SoundPlayStop(path);
        }
    }

    /**
     * 停止音乐
     */
    public void stop(){
        if(mediaPlayer != null) {


            mediaPlayer.stop();
            try {
               // mediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
            } catch (Exception e) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
                //e.printStackTrace();
            }
            if (m_eventsReceiver != null)
                m_eventsReceiver.SoundPlayStop(path);
        }
    }


    @Override
    public void onDestroy() {
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public boolean isPlaying()
    {
        return mediaPlayer.isPlaying();
    }
    static public void playSound(Context context, String urlSound)
    {
        KDSLog.d(TAG,urlSound);
        Intent intent = new Intent();
        intent.putExtra("url", urlSound);
        intent.putExtra("MSG", PLAY_MSG);
        intent.setClass(context, SoundService.class);
        context.startService(intent);       //启动服务
    }

    static public void stopSound(Context context)//, String urlSound)
    {
        //Log.d(TAG,urlSound);
        Intent intent = new Intent();
        intent.putExtra("url", "");
        intent.putExtra("MSG", STOP_MSG);
        intent.setClass(context, SoundService.class);
        context.startService(intent);       //启动服务
    }

    private final class PreparedListener implements MediaPlayer.OnPreparedListener {
        private int positon;

        public PreparedListener(int positon) {
            this.positon = positon;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer.start();    //开始播放
            if(positon > 0) {    //如果音乐不是从头播放
                mediaPlayer.seekTo(positon);
            }
            if (m_eventsReceiver != null)
                m_eventsReceiver.SoundPlayStarted(path);

        }
    }

}
