package com.bematechus.kds;

import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Environment;
import android.provider.Contacts;

import com.bematechus.kdslib.KDSUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Play sound
 */
public class KDSSound {

    //Ringtone m_ringTone = null;
    String m_description = "";
    String m_uri = "";

    public String getDescription()
    {
        return m_description;
    }

    public void setDescription(String strDescription)
    {
        m_description = strDescription;
    }
    public void setUri(String uri)
    {
        m_uri = uri;
    }
    public String getUri()
    {
        return m_uri;
    }

    @Override
    public String toString()
    {
        if (getUri().isEmpty())
            return "";
        return getDescription()+"|" + getUri();
    }

    static public KDSSound parseString(String sound)
    {

        if (sound == null)
            return new KDSSound();
        int n = sound.indexOf("|");
        KDSSound s = new KDSSound();
        if (n<0)
        {
            s.setUri(sound);
        }
        else
        {
            s.setDescription(sound.substring(0, n));
            s.setUri(sound.substring(n+1));
        }
        return s;
    }
    static public ArrayList<KDSSound> getRingtoneList(Context context){//}, int type){


        ArrayList<KDSSound> resArr = new ArrayList<>();

        RingtoneManager manager = new RingtoneManager(context);

        manager.setType(RingtoneManager.TYPE_ALARM);

        Cursor cursor = manager.getCursor();

        int count = cursor.getCount();

        for(int i = 0 ; i < count ; i ++){

            //resArr.add(manager.getRingtone(i));
            KDSSound sound = new KDSSound();
            sound.m_description = manager.getRingtone(i).getTitle(context);
            sound.m_uri = manager.getRingtoneUri(i).toString();
            resArr.add(sound);



        }

        return resArr;

    }

    static public ArrayList<KDSSound> getMusicFolderList() {
        //打开指定目录，显示项目说明书列表，供用户选择
        String strPATH = Environment.getExternalStorageDirectory() + "/";


        ArrayList<KDSSound> ar = new ArrayList<>();

        File musicDir = new File(strPATH + "Music");
        if (!musicDir.exists()) {
            musicDir.mkdir();
        }

        if (!musicDir.exists()) {
            return ar;
        } else {
            //取出文件列表：
            final File[] files = musicDir.listFiles();
            for(File music : files){
               KDSSound sound = new KDSSound();
                sound.m_description = music.getName();
                sound.m_uri = music.getAbsolutePath();
                ar.add(sound);
            }

        }
        return ar;
    }
        Object m_tag = null;
    public void setTag(Object obj)
    {
        m_tag = obj;
    }
    public Object getTag()
    {
        return m_tag;
    }

    public  boolean isEqual(KDSSound sound)
    {
        return this.toString().equals(sound.toString());
    }
}
