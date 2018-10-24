package com.bematechus.kds;

import android.content.Context;

import com.bematechus.kdslib.KDSApplication;

/**
 * Created by Administrator on 2017/2/3.
 */
public class SoundManager {

    AudioPlayer m_player = new AudioPlayer();
    //Context m_context = null;
    static public Context getConext()
    {
        return KDSApplication.getContext();

    }

    static public KDSSettings getSettings()
    {
        return KDSGlobalVariables.getKDS().getSettings();
    }

    static public boolean isEnabled()
    {
        return getSettings().getBoolean(KDSSettings.ID.Sound_enabled);

    }
    static public int getDurationSeconds()
    {
        return getSettings().getInt(KDSSettings.ID.Sound_duration);
    }
    final  int SOUND_DURATION = 3000;
    public void playSound(KDSSettings.ID soundID)
    {
        if (!isEnabled()) return;

        String s = getSettings().getString(soundID);
        if (s.isEmpty()) return;
        KDSSound sound = KDSSound.parseString(s);
        //m_player.playWithoutBind( sound.getUri(), getDurationSeconds() *1000);
        playSoundByThread(sound);

    }
    public void stopSound()
    {
        m_player.stopWithoutBind();
    }

    private void playSoundByThread(KDSSound sound)
    {
        SoundRunnable s = new SoundRunnable(sound);

        Thread t = new Thread(s);

        t.start();

    }
    class SoundRunnable implements Runnable
    {
        KDSSound m_sound = null;
        public SoundRunnable(KDSSound sound )
        {
            this.setSound(sound);
        }
        public void setSound(KDSSound sound)
        {
            m_sound = sound;
        }
        @Override
        public void run() {
            m_player.playWithoutBind( m_sound.getUri(), getDurationSeconds() *1000);
        }
    }
}
