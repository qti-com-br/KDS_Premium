package com.bematechus.kds;

import android.graphics.Bitmap;
import android.os.FileUtils;

import com.bematechus.kdslib.CSVStrings;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;
import java.util.Date;

public class ScreenLogoFiles {

    boolean m_bFilesEnabled = false;
    ArrayList<String> m_arFiles = new ArrayList<>();
    int m_nSeconds = 60;

    int m_nCurrentIndex = -1;
    Date m_dtStartIndex = new Date();

    public void reset()
    {
        m_nCurrentIndex = -1;
        m_dtStartIndex = KDSUtil.createInvalidDate();
    }

    public void updateSettings(KDSSettings settings)
    {
        reset();
        m_bFilesEnabled = settings.getBoolean(KDSSettings.ID.Background_enable_multiple_images);


        ArrayList<String> oldFiles = new ArrayList<>();
        oldFiles.addAll(m_arFiles);

        if (m_bFilesEnabled) {
            String files = settings.getString(KDSSettings.ID.Background_images);

                CSVStrings csv = CSVStrings.parse(files);

                m_arFiles.clear();
                m_arFiles.addAll(csv.getArray());
                if (m_arFiles.size() == 0) {
                    String s = settings.getString(KDSSettings.ID.Screen_logo_file);
                    if (!s.isEmpty())
                        m_arFiles.add(s);

            }

        }
        else
        {
            m_arFiles.clear();
            String s = settings.getString(KDSSettings.ID.Screen_logo_file);
            if (!s.isEmpty())
                m_arFiles.add(s);
        }
        m_nSeconds = settings.getInt(KDSSettings.ID.Background_rotate_seconds);

        if (!KDSUtil.isArrayContainsSame(m_arFiles, oldFiles))
        {
            startDownloadAllFiles();
        }
    }

    public void refreshTime()
    {
        m_dtStartIndex.setTime(System.currentTimeMillis());
    }
    public String getNextFileName()
    {
        if (!m_bFilesEnabled ) {
            if (m_arFiles.size() >0) {
                refreshTime();
                m_nCurrentIndex = 0;
                return m_arFiles.get(0);
            }
            else
                return "";
        }
        TimeDog td = new TimeDog(m_dtStartIndex);
        int nindex = m_nCurrentIndex;
        if (td.is_timeout(m_nSeconds*1000))
        {
            nindex ++;
        }

        if (nindex >= m_arFiles.size() || nindex<0)
            nindex = 0;

        String file =  getFile(nindex);
        if (!file.isEmpty())
        {
            if (m_nCurrentIndex != nindex)
                refreshTime();
            m_nCurrentIndex = nindex;
        }
        return file;

    }

    private String getFile(int nIndex)
    {
        if (nIndex >=0 && nIndex < m_arFiles.size())
        {
            return m_arFiles.get(nIndex);
        }
        else
            return "";
    }

    private void startDownloadAllFiles()
    {
        ImageUtil c = new ImageUtil(); //don't remove it, it will create handle in main thread.

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                downloadFiles();
            }
        });
        t.start();
    }

    private void downloadFiles()
    {
        KDSFileUtils.deleteFolder(getImagesFolder());
        KDSUtil.createFolder(getImagesFolder());

        for (int i=0; i< m_arFiles.size(); i++)
        {
            String file = m_arFiles.get(i);
            if (ImageUtil.isLocalFile(file))
            {
                m_arMap.add(new FileMap(file, file));
            }
            else if (ImageUtil.isSmbFile(file))
            {
                String localFileName = ImageUtil.getSmbFile(file, getImagesFolder());

                m_arMap.add(new FileMap(file, localFileName));
            }
            else if (ImageUtil.isInternetFile(file))
            {
                Bitmap bmp = ImageUtil.getHttpBitmap(file, null);
                String fileName = getImagesFolder()+"/" + KDSUtil.createNewGUID()+".png";
                ImageUtil.bitmap2PNGPath(bmp, fileName);
                m_arMap.add(new FileMap(file, fileName));
            }
        }
    }
    static String FOLDER_NAME = "bgimages";
    static public String getImagesFolder() {

        return KDSUtil.getBaseDirCanUninstall() + "/" + FOLDER_NAME;
    }



    static public String getLocalFileName(String fileName)
    {
        if (ImageUtil.isLocalFile(fileName))
            return fileName;
        for (int i=0; i< m_arMap.size(); i++)
        {
            if (m_arMap.get(i).remoteFileName.equals(fileName))
                return m_arMap.get(i).localFileName;
        }
        return "";

    }
    static ArrayList<FileMap> m_arMap = new ArrayList<>();

    class FileMap
    {
        String remoteFileName = "";
        String localFileName = "";
        public FileMap(String remote, String local)
        {
            remoteFileName = remote;
            localFileName = local;
        }
    }
}
