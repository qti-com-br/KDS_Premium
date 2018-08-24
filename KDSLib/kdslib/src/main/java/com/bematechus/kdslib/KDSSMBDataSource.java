package com.bematechus.kdslib;

import android.util.Log;

import java.util.ArrayList;

/**
 *
 * The xml data comes from remote folder.
 * We use this to handle this type data source
 */
public class KDSSMBDataSource implements Runnable {
    private static final String TAG = "KDSSMBDataSource";
    final static public String PATH_LOST = "SMBLOST";
    final static public String PATH_PERMISSION = "SMBPERMISSION";
    public static final String TAG_SMBERROR_START =  "<SMBError>";
    public static final String TAG_SMBERROR_END =  "</SMBError>";

    final static public String PATH_OK = "SMBOK";
    //use same handler pass thread data to main thread.
    private KDSSocketMessageHandler m_sockEventsMessageHandler = null;

    private String m_strRemoteFolder = ""; //with the last "/"
    private boolean m_bThreadRunning = false;
    private Thread m_thread = null;
    public KDSSMBDataSource(KDSSocketMessageHandler handler)
    {
        this.setMessageHandler(handler);
    }

    public void setMessageHandler(KDSSocketMessageHandler handler)
    {
        m_sockEventsMessageHandler = handler;

    }
    public void setRemoteFolder(String folder)
    {
        m_strRemoteFolder = folder;
    }

    public boolean start()
    {
        if (m_thread != null)
            return true;
        m_bThreadRunning = true;

        m_thread = new Thread(this, "SocketPing");
        m_thread.start();

        return true;
    }
    public void stop()
    {
        if (m_thread != null) {
            m_bThreadRunning = false;
            try {
                m_thread.join(1000);
            } catch (Exception e) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            }
        }
        m_thread = null;

        //for uploading thread
        if (m_uploadRunnable != null)
            m_uploadRunnable.stopThread();
        if (m_threadUploading != null)
        {
            try {
                m_threadUploading.join(1000);
            }
            catch (Exception e)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            }
            m_threadUploading = null;

        }

        m_uploadRunnable = null;
    }

    private void sleep(int ms)
    {
        try {
            Thread.sleep(ms);
        }catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
        }
    }

    private void informSmbPermissionError()
    {
        if (this.m_sockEventsMessageHandler != null)
            this.m_sockEventsMessageHandler.sendInformation(PATH_PERMISSION);

    }
    private void informSmbPermissionOK()
    {
        if (this.m_sockEventsMessageHandler != null)
            this.m_sockEventsMessageHandler.sendInformation(PATH_OK);
    }

    TimeDog m_tdLastCheckPermission = new TimeDog();
    boolean m_bPermissionError = false;
    final int CHECK_PERMISSION_TIMEOUT_NO_ERROR = 15000;
    final int CHECK_PERMISSION_TIMEOUT_HAVE_ERROR = 3000;
    private boolean isRemoteFolderPermissionError()
    {
        boolean bIsTimeForCheckPermission = false;

        if (m_bPermissionError) {

            if (m_tdLastCheckPermission.is_timeout(CHECK_PERMISSION_TIMEOUT_HAVE_ERROR)) {
                bIsTimeForCheckPermission = true;
                m_tdLastCheckPermission.reset();
            }
        }
        else {//if all is ok, 5 mins check once.
            if (m_tdLastCheckPermission.is_timeout(CHECK_PERMISSION_TIMEOUT_NO_ERROR)) {
                bIsTimeForCheckPermission = true;
                m_tdLastCheckPermission.reset();
            }
        }
        if (bIsTimeForCheckPermission){
            int nError = KDSSmbFile.checkFolderWritable(m_strRemoteFolder);
            if (nError !=0) {
                m_bPermissionError = true;
                informSmbPermissionError(nError);
                sleep(3000);
               // return false;
            } else {
                m_bPermissionError = false;
                informSmbPermissionOK();
            }
        }
        return m_bPermissionError;
    }

    private void informSmbOK()
    {
        if (this.m_sockEventsMessageHandler != null)
            this.m_sockEventsMessageHandler.sendInformation(PATH_OK);

    }

    private void informSmbLostError()
    {
        if (this.m_sockEventsMessageHandler != null)
            this.m_sockEventsMessageHandler.sendInformation(PATH_LOST);

    }

    /**
     *
     */
    public void run()
    {
        m_tdLastCheckPermission.reset(KDSUtil.createInvalidDate());
        m_bPermissionError = true;
        while(m_bThreadRunning)
        {
            if (m_strRemoteFolder.isEmpty()) {
                sleep(500);
                continue;
            }
            try {
                if (KDSSmbFile.isValidPath(m_strRemoteFolder)) {
                    if (isRemoteFolderPermissionError())
                    {
                        continue;
                    }
                    informSmbOK();
                }
                else
                {
                    informSmbLostError();
                    sleep(500);
                    continue;
                }

                //read data
                ArrayList<String> ar = KDSSmbFile.findAllXmlFiles(m_strRemoteFolder,MAX_ORDERS_COUNT);
                if (ar.size() <= 0) {
                    sleep(500);
                    continue;
                }
                checkXmlFiles(ar);
            }
            catch (Exception e)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
                //KDSLog.e(TAG, KDSUtil.error( e));
            }

        }
    }

    final int MAX_ORDERS_COUNT = 10;
    private void checkXmlFiles(ArrayList<String> arFiles) {
        int ncount = arFiles.size();
        if (ncount >MAX_ORDERS_COUNT)
            ncount = MAX_ORDERS_COUNT;
        for (int i = 0; i < ncount; i++)
        {
            String smbFileName = arFiles.get(i);
            smbFileName = m_strRemoteFolder + smbFileName;
            String text = readFileContent(smbFileName);
            if (text.isEmpty()) continue;
            doReceivedXmlText(smbFileName,text);
            //remove this file.
            removeSmbFile(smbFileName);
            if (i < ncount-1)
                sleep(500); //delay, Too many orders will lock the router.
        }
    }

    /**
     * read text from the smb server
     *
     * @param smbFileName
     * @return
     */
    private String readFileContent(String smbFileName)
    {
        try {


            String text = KDSSmbFile.readFromUtf8SmbText(smbFileName);
            return text;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return "";
        }
    }


    private void informSmbPermissionError(int nError)
    {
        if (this.m_sockEventsMessageHandler != null)
            this.m_sockEventsMessageHandler.sendInformation(PATH_PERMISSION);

    }


    private void doReceivedXmlText(String smbFileName, String xmlContent)
    {
        if (this.m_sockEventsMessageHandler != null)
            this.m_sockEventsMessageHandler.sendReceiveSmbXmlMessage(this,smbFileName, xmlContent);
    }


    /**
     *
     * @param smbFileName
     */
    private boolean removeSmbFile(String smbFileName)
    {
        try {
            if (!KDSSmbFile.deleteUriFile(smbFileName))
            {
                String strErr = String.format(KDSApplication.getContext().getString(R.string.can_not_remove_smb_file));
                KDSSMBPath path = KDSSMBPath.parseString(smbFileName);
                strErr = strErr.replace("#", path.toDisplayString());
                strErr = TAG_SMBERROR_START + strErr +  TAG_SMBERROR_END;
                if (this.m_sockEventsMessageHandler != null)
                    this.m_sockEventsMessageHandler.sendReceiveSmbXmlMessage(this,"", strErr);
                return false;
            }
            return true;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }

    Thread m_threadUploading = null;
    UploadRunnable m_uploadRunnable = null;
    public boolean uploadSmbFile(String strSubFolder, String smbFileName, String strFileContent)
    {

        if (m_uploadRunnable == null)
            m_uploadRunnable = new UploadRunnable();
        m_uploadRunnable.add(m_strRemoteFolder, strSubFolder, strFileContent, smbFileName);
        if (m_threadUploading == null) {
            m_threadUploading = new Thread(m_uploadRunnable);
            m_threadUploading.start();
        }
        return true;


    }
    class UploadData
    {
        String m_strRemoteFolder = "";
        String m_strSubFolder = "";
        String m_fileContent = "";
        String m_fileName = "";
        boolean m_bResult = false;
    }

    class UploadRunnable implements Runnable
    {

        ArrayList<UploadData> m_arData = new ArrayList<>();

        private Object m_locker = new Object();
        private boolean m_bRunning = true;
        public void stopThread()
        {
            m_bRunning = false;
        }
        public void add(String remoteFolder, String subFolder, String fileContent, String toFileName)
        {
            UploadData d = new UploadData();
            d.m_strRemoteFolder = remoteFolder;
            d.m_strSubFolder = subFolder;
            d.m_fileContent = fileContent;
            d.m_fileName = toFileName;
            synchronized (m_locker) {
                m_arData.add(d);
            }
        }

        public void writeDataToFile()
        {
            if (m_arData.size() <=0)
            {
                try {
                    Thread.sleep(1000);
                }
                catch (Exception e)
                {
                    KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
                }
            }
            else {
                int ncount = 0;
                synchronized (m_locker) {
                    ncount = m_arData.size();
                }
                ArrayList<UploadData> arFinished = new ArrayList<>();

                for (int i=0; i< ncount ; i++) {
                    UploadData d = null;
                    synchronized (m_locker) {
                        d = m_arData.get(i);
                    }
                    String remoteFolder = d.m_strRemoteFolder;
                    if (!KDSSmbFile.isExistedSubFolder(d.m_strRemoteFolder, d.m_strSubFolder)) {

                        if (!KDSSmbFile.createSubDir(remoteFolder+d.m_strSubFolder)) {
                            d.m_bResult = false;
                            continue;
                        }
                    }

                    remoteFolder += (d.m_strSubFolder +"/");
                    boolean b = KDSSmbFile.smbPut(remoteFolder, d.m_fileName, d.m_fileContent);
                    d.m_bResult = b;
                    if (b)
                    {
                        arFinished.add(d);
                    }

                }
                //remove finished
                synchronized (m_locker) {
                    for (int i= 0; i< arFinished.size(); i++)
                    {
                        m_arData.remove(arFinished.get(i));
                    }
                }
                arFinished.clear();
            }
        }

        @Override
        public void run() {

            while (m_bRunning) {
                writeDataToFile();
            }
        }
    }


}
