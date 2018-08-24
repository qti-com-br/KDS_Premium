package com.bematechus.kds;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/1/27.
 * Please audio.
 *  e.g: the alert sound
 *  There are two way to play auto
 *      1. Bind service to context
 *      2. Without bind
 */
public class AudioPlayer  extends Handler implements Runnable {

    Context m_context = null;
    private SoundService m_service = null;
    ServiceConnection m_serviceConn = null;
    final int MSG_STOP = 1;
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_STOP: {
                if (m_bPlayByWithoutBind)
                    this.stopWithoutBind();
                this.stop();

                //m_receiver.threadrefresh_FocusFirst();
            }
            break;
        }
    }
    public void bindKDSService(Context context)
    {
        m_context = context;
        m_serviceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                m_service = ((SoundService.MyBinder)service).getService();
                AudioPlayer.this.onBindServiceFinished();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                m_service = null;
            }
        };

        Intent intent = new Intent(context, SoundService.class);
        context.bindService(intent, m_serviceConn, Context.BIND_AUTO_CREATE);
    }

    public void unbindKDSService()
    {
        if (m_serviceConn != null) {
            {
                if (m_context != null)
                    m_context.unbindService(m_serviceConn);
            }
        }
        m_serviceConn = null;
    }

    public void onBindServiceFinished()
    {
        if (m_service == null) return;

    }
    public void play(String uri)
    {
        if (m_service == null) return;
        m_service.play(uri);
    }
    int m_nDuration = 0;
    public  void play(String uri, int msDuration)
    {
        m_bPlayByWithoutBind = false;
        m_nDuration = msDuration;
        m_service.playLoop(uri);
        this.start();
    }

    boolean m_bPlayByWithoutBind = false;
    public  void playWithoutBind( String uri, int msDuration)
    {
        m_bPlayByWithoutBind = true;
        m_nDuration = msDuration;
        SoundService.playSound(KDSApplication.getContext(), uri);
        //m_service.playLoop(uri);
        this.start();
    }
    public void stop(){
        if (m_service == null) return;
        m_service.stop();
        m_nDuration =0;
    }

    public void stopWithoutBind(){

        m_bPlayByWithoutBind = false;
        SoundService.stopSound(KDSApplication.getContext());
        m_nDuration =0;
    }
    public  void pause()
    {
        if (m_service == null) return;
        m_service.pause();
    }

    public void setSoundEvent(SoundService.SoundEventsReceiver receiver)
    {
        if (m_service == null) return;
        m_service.setEventReceiver(receiver);
    }

    public boolean isPlaying()
    {
        if (m_service == null) return false;
        return m_service.isPlaying();
    }

    ArrayList<KDSSound> getAudioArray(Context context) {
        ArrayList<KDSSound> ar = KDSSound.getRingtoneList(context);// new ArrayList<>();
        ArrayList<KDSSound> arMusic = KDSSound.getMusicFolderList();// new ArrayList<>();
        arMusic.addAll(ar);
        KDSSound none = new KDSSound();

        arMusic.add(0, none);
        return arMusic;
    }
    TimeDog m_timeDog = new TimeDog();
    public void start()
    {
        m_timeDog.reset();
        (new Thread(this)).start();
    }
    @Override
    public void run() {

       while (!m_timeDog.is_timeout(m_nDuration))
       {

       }
        Message msg = new Message();
        msg.what = MSG_STOP;
        this.sendMessage(msg);

    }
}
