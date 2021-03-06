package com.bematechus.kdslib;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import jcifs.smb.SmbFile;

/**
 *
 * The xml data comes from remote folder.
 * We use this to handle this type data source
 */
public class KDSSMBDataSource implements Runnable {
    private static final String TAG = "KDSSMBDataSource";
    //for sock stuck issue
    public interface BufferStateChecker {
        boolean bufferCheckerIsTooManyDataBuffered();
    }

    final static public String PATH_LOST = "SMBLOST";
    final static public String PATH_PERMISSION = "SMBPERMISSION";
    public static final String TAG_SMBERROR_START =  "<SMBError>";
    public static final String TAG_SMBERROR_END =  "</SMBError>";

    final static public String PATH_OK = "SMBOK";
    //use same handler pass thread data to main thread.
    private KDSSocketMessageHandler m_sockEventsMessageHandler = null;

    private String m_strRemoteFolder = ""; //with the last "/"
    private boolean m_bThreadRunning = false;
    private boolean m_bThreadSuspend = false;


    private Thread m_thread = null;
    //for buffer full when smb read data.
    BufferStateChecker m_bufferStateChecker = null;
    public void setBufferStateChecker(BufferStateChecker checker)
    {
        m_bufferStateChecker = checker;
    }
    public BufferStateChecker getBufferStateChecker()
    {
        return m_bufferStateChecker;
    }

    public KDSSMBDataSource(KDSSocketMessageHandler handler)
    {
        this.setMessageHandler(handler);
    }

    public KDSSMBDataSource(KDSSocketMessageHandler handler, BufferStateChecker checker)
    {
        this.setMessageHandler(handler);
        this.setBufferStateChecker(checker);
        KDSSmbFile.setSmbV1Config();
    }

    public void setMessageHandler(KDSSocketMessageHandler handler)
    {
        m_sockEventsMessageHandler = handler;

    }
    public void setRemoteFolder(String folder)
    {

        m_strRemoteFolder = folder;
        KDSLog.e(TAG, KDSLog._FUNCLINE_() + "m_strRemoteFolder=" + m_strRemoteFolder);
    }

    public boolean start()
    {
        KDSLog.e(TAG, KDSLog._FUNCLINE_() +  "enter");
        if (m_thread != null) {
            KDSLog.e(TAG, KDSLog._FUNCLINE_() +  "restore thread");
            m_bThreadRunning = true;
            m_bThreadSuspend = false;
            return true;
        }
        KDSLog.e(TAG, KDSLog._FUNCLINE_() +  "new thread started");
        m_bThreadRunning = true;
        m_bThreadSuspend = false;
        m_thread = new Thread(this, "SMBSrc");
        m_thread.start();

        //startWatchDogThread();
        KDSLog.e(TAG, KDSLog._FUNCLINE_() +  "exit");
        return true;
    }

    /**
     * don't stop the retrieve remote folder files thread.
     * Just suspend it. Prevent don't get files issues.
     */
    public void stop()
    {

        KDSLog.e(TAG, KDSLog._FUNCLINE_() +  "enter");
        if (m_thread != null) {
            KDSLog.e(TAG, KDSLog._FUNCLINE_() +  "suspend thread");
            m_bThreadSuspend = true;
            //this.setRemoteFolder("");

//            m_bThreadRunning = false;
//            try {
//                m_thread.join(1000);
//            } catch (Exception e) {
//                KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
//            }
        }

        // m_thread = null;

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

        KDSLog.e(TAG, KDSLog._FUNCLINE_() +  "exit");

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

    TimeDog m_tdLastCheckPermission = new TimeDog(KDSUtil.createInvalidDate());
    boolean m_bPermissionError = false;
    final int CHECK_PERMISSION_TIMEOUT_NO_ERROR = 3600000;//60 minutes
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
        else {//if all is ok, 60 mins check once.
            if (m_tdLastCheckPermission.is_timeout(CHECK_PERMISSION_TIMEOUT_NO_ERROR)) {
                bIsTimeForCheckPermission = true;
                m_tdLastCheckPermission.reset();
            }
        }
        if (bIsTimeForCheckPermission){
            int nError = KDSSmbFile2.checkFolderWritable(m_strRemoteFolder);
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

    /**
     * if the remote folder is empty, this function was called too.
     * But, while use tcp/ip datasource, I set remote folder value to empty.
     * So, we need check the data source type in message receiver.
     */
    private void informSmbLostError()
    {
        if (this.m_sockEventsMessageHandler != null)
            this.m_sockEventsMessageHandler.sendInformation(PATH_LOST);

    }

    //save them for next loop
    ArrayList<String> m_arExistedFiles = new ArrayList<>();
    final int BUFFER_FILES_COUNT = -1;
    /**
     *
     */
    public void run()
    {
        KDSLog.e(TAG,  KDSLog._FUNCLINE_() + "run() enter: name=" + Thread.currentThread().getName() +
                            ",id=" + KDSUtil.convertIntToString(Thread.currentThread().getId()) +
                            ", m_thread id=" + KDSUtil.convertIntToString(m_thread.getId()));

        m_tdLastCheckPermission.reset(KDSUtil.createInvalidDate());
        m_bPermissionError = true;
        List<String> ar = new ArrayList<>();
        while(m_bThreadRunning)
        {
            //m_timeDog.reset();
            try
            {
                if (m_thread != Thread.currentThread())
                    break;
                if (m_bThreadSuspend)
                {
                    KDSLog.e(TAG, KDSLog._FUNCLINE_() + "suspend");
                    sleep(1000);
                    continue;
                }
                if (m_strRemoteFolder.isEmpty()) {
                    informSmbLostError();
                    sleep(1000);
                    //m_arExistedFiles.clear();
                    //m_timeDog.reset();
                    continue;
                }

                //check buffer, KPP1-Coke
                if (m_bufferStateChecker != null)
                {
                    if (m_bufferStateChecker.bufferCheckerIsTooManyDataBuffered())
                    {
                        sleep(100);
                        //m_timeDog.reset();
                        continue;
                    }
                }
                if (!m_bThreadRunning) break;
                if (KDSSmbFile.smb_isValidPath(m_strRemoteFolder)) {
                    //m_timeDog.reset();
                    if (isRemoteFolderPermissionError()) {
                        //m_arExistedFiles.clear();
                        continue;
                    }
                    informSmbOK();
                } else {
                    informSmbLostError();
                    sleep(500);
                    m_arExistedFiles.clear();
                    continue;
                }
                //m_timeDog.reset();
                if (!m_bThreadRunning) break;

                //read data
                //ArrayList<String> ar = KDSSmbFile.findAllXmlFiles(m_strRemoteFolder,MAX_ORDERS_COUNT);
                //ar.clear();
                if (m_arExistedFiles.size() <=0) {
                    //m_arExistedFiles.clear();

                    m_arExistedFiles = KDSSmbFile.findAllXmlFiles(m_strRemoteFolder, BUFFER_FILES_COUNT, m_arExistedFiles);
                }
                //m_timeDog.reset();
                if (!m_bThreadRunning) break;
                if (m_arExistedFiles.size() >0)
                {
                    int ncount = MAX_ORDERS_COUNT>m_arExistedFiles.size()?m_arExistedFiles.size():MAX_ORDERS_COUNT;
                    ar = m_arExistedFiles.subList(0, ncount);

//                    for (int i=0; i< ncount; i++) {
//                        ar.add(m_arExistedFiles.get(i));
//                        //m_arExistedFiles.remove(0);
//                    }
//                    m_arExistedFiles.removeAll(ar);
                }
                //m_timeDog.reset();
                if (!m_bThreadRunning) break;

                if (ar.size() <= 0) {
                    //m_timeDog.reset();
                    sleep(500);
                    continue;
                }
                //if (!KDSConst._DEBUG)
                int ndone = checkXmlFiles(ar);

                //m_timeDog.reset();
                if (!m_bThreadRunning) break;
                if (ndone != ar.size()) {
                    m_arExistedFiles.clear();
                    Log.d(TAG, "Reset buffered file names");
                }
                else
                    ar.clear();

                //m_timeDog.reset();
                if (!m_bThreadRunning) break;
                sleep(500); //slow down.
            }
            catch (Exception e)
            {
                if (!m_bThreadRunning) break;
                KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
                //KDSLog.e(TAG, KDSUtil.error( e));
            }

        }
        KDSLog.e(TAG,  KDSLog._FUNCLINE_() + "KDSSMBDataSource() thread exit");
    }

    final int MAX_ORDERS_COUNT = 20;
    private int checkXmlFiles(List<String> arFiles) {

        int ncount = arFiles.size();
//        if (ncount >MAX_ORDERS_COUNT)
//            ncount = MAX_ORDERS_COUNT;
        for (int i = 0; i < ncount; i++)
        {
            if (!m_bThreadRunning) return i;
            String smbFileName = m_strRemoteFolder +arFiles.get(i);
            //smbFileName =  smbFileName;
            if (m_strRemoteFolder.isEmpty()) return i;
            String text = readFileContent(smbFileName);

            if (text.isEmpty()) continue;

            doReceivedXmlText(smbFileName, text);
            if (!m_bThreadRunning) return i;
            //remove this file.
            removeSmbFile(smbFileName);
            if (!m_bThreadRunning) return i;
            if (i < ncount-1)
                sleep(500); //delay, Too many orders will lock the router.
        }
        //arFiles.clear();
        return ncount;
    }

    //byte[] m_readTextBuffer = new byte[1024*1024*5];
    /**
     * read text from the smb server
     *
     * @param smbFileName
     * @return
     */
    private String readFileContent(String smbFileName)
    {
        try {


            String text = KDSSmbFile2.readFromUtf8SmbText2(smbFileName);
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
            if (!KDSSmbFile2.deleteUriFile(smbFileName))
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
            m_threadUploading = new Thread(m_uploadRunnable, "SMBUpload");
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

    final  int  MAX_UPLOAD_WAITING_COUNT = 100;
    final int MAX_BATCH_COUNT = 5;
    class UploadRunnable implements Runnable
    {

        Vector<UploadData> m_arData = new Vector<>();
        String m_lastExistedRemoteSubFolder = ""; //last passed check

        private Object m_locker = new Object();
        private boolean m_bRunning = true;
        public void stopThread()
        {
            m_bRunning = false;
        }
        public void add(String remoteFolder, String subFolder, String fileContent, String toFileName)
        {

            synchronized (m_locker) {
                if (m_arData.size() >= MAX_UPLOAD_WAITING_COUNT)
                    return; //ignore this one.
            }
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
                //Log.i(TAG, "notification waiting =" + KDSUtil.convertIntToString(ncount));
                Vector<UploadData> arFinished = new Vector<>();
                UploadData d = null;
                ncount = ncount > MAX_BATCH_COUNT?MAX_BATCH_COUNT:ncount;
                for (int i=0; i< ncount ; i++) {

                    synchronized (m_locker) {
                        d = m_arData.get(i);
                    }
                    String remoteRootFolder = d.m_strRemoteFolder;
                    if (remoteRootFolder.isEmpty())
                        remoteRootFolder = KDSSMBDataSource.this.m_strRemoteFolder;

                    if (remoteRootFolder.isEmpty()) {
                        d.m_bResult = false;
                        continue;
                    }
                    //if (!checkRemoteSubfolder(d.m_strRemoteFolder, d.m_strSubFolder)) {
                    if (!checkRemoteSubfolder(remoteRootFolder, d.m_strSubFolder)) {
                        d.m_bResult = false;
                        continue;
                    }
                    //String remoteFolder = d.m_strRemoteFolder + (d.m_strSubFolder +"/");;
                    String remoteFolder = remoteRootFolder + (d.m_strSubFolder +"/");;

                    boolean b = KDSSmbFile2.smbPut(remoteFolder, d.m_fileName, d.m_fileContent);
                    d.m_bResult = b;
                    if (!b)
                    {
                        m_lastExistedRemoteSubFolder = "";
                        checkRemoteSubfolder(d.m_strRemoteFolder, d.m_strSubFolder);
                    }
                    arFinished.add(d);
                    try {
                        Thread.sleep(10);
                    }
                    catch (Exception e)
                    {

                    }
                }
                //remove finished
                synchronized (m_locker) {
                    m_arData.removeAll(arFinished);
                }
                arFinished.clear();
            }
        }

        private boolean checkRemoteSubfolder(String remoteFolder, String subFolder)
        {
            String remoteSubfolder = remoteFolder+subFolder;
            if (!remoteSubfolder.equals(m_lastExistedRemoteSubFolder) ) {
                if (!KDSSmbFile.isExistedSubFolder(remoteFolder, subFolder)) {

                    if (!KDSSmbFile.createSubDir(remoteSubfolder)) {

                        m_lastExistedRemoteSubFolder = "";
                        return false;
                    }
                    else
                    {
                        m_lastExistedRemoteSubFolder = remoteSubfolder;
                        return true;
                    }
                }
                else
                {
                    m_lastExistedRemoteSubFolder = remoteSubfolder;
                    return true;
                }
            }
            return true;
        }
        @Override
        public void run() {

            while (m_bRunning) {
                if (m_threadUploading != Thread.currentThread())
                    return;
                writeDataToFile();
            }
        }
    }

    public int removeTimeoutNotificationFiles(String subFolder, int nMinutes)
    {
        return removeTimeoutFiles(m_strRemoteFolder, subFolder, nMinutes);
    }
    public int removeTimeoutFiles(String remoteFolder, String subFolder, int nMinutes)
    {
        Object[] files = null;
        files = KDSSmbFile.findAllFiles(remoteFolder + subFolder+"/");
        if (files == null ||
                files.length<=0)
            return 0;
        int ncounter = 0;
        //TimeDog td = new TimeDog();
        long timeout = nMinutes * 60 * 1000; //ms
        for (int i=0; i< files.length; i++)
        {

            try {
                Object obj = files[i];
                if (obj == null)
                    return ncounter;
                long creatTime = 0;// file.lastModified();
                if (obj instanceof jcifs.smb.SmbFile) {
                    jcifs.smb.SmbFile file = (jcifs.smb.SmbFile) obj;
                    creatTime =  file.lastModified();
                    long now = System.currentTimeMillis();
                    long d = now - creatTime;
                    if (d >timeout)
                    {
                        file.delete();
                        ncounter ++;
                    }
                }
                //else if (obj instanceof jcifsng.smb.SmbFile)//kpp1-376
                else if (obj instanceof jcifs.smb.SmbFile)//kpp1-376
                {
                    //jcifsng.smb.SmbFile file = (jcifsng.smb.SmbFile) obj;//kpp1-376
                    jcifs.smb.SmbFile file = (jcifs.smb.SmbFile) obj;
                    creatTime =  file.lastModified();
                    long now = System.currentTimeMillis();
                    long d = now - creatTime;
                    if (d >timeout)
                    {
                        file.delete();
                        ncounter ++;
                    }
                }




                sleep(10);

            }
            catch ( Exception e){}

        }
        files = null;

        return ncounter;
    }

//    Thread m_threadWatchDog = null;
//    boolean m_bWatchDogThreadRunning = true;
//    TimeDog m_timeDog = new TimeDog();
//    final int THREAD_DEAD_TIMEOUT = 10000;
//
//    private void startWatchDogThread()
//    {
//        if (m_threadWatchDog != null) return;
//        m_threadWatchDog = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (m_bWatchDogThreadRunning)
//                {
//                    if (m_timeDog.is_timeout(THREAD_DEAD_TIMEOUT)) //10seconds
//                    {
//                        restartDataSourceThread();
//                        m_timeDog.reset();
//                    }
//                    sleep(5000);
//                }
//            }
//        }, "SmbWatchDog");
//        m_threadWatchDog.start();
//    }
//
//    private void stopWatchDogThread()
//    {
//        m_bWatchDogThreadRunning = false;
//        if (m_threadWatchDog == null) return;
//        try {
//            m_threadWatchDog.join(1000);
//        } catch (Exception e) {
//            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
//        }
//
//    }
//
//    private void restartDataSourceThread()
//    {
//        //stop();
//
//        try {
//            m_thread.join(1000);
//        } catch (Exception e) {
//            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
//        }
//
//        m_bThreadRunning = true;
//
//        m_thread = new Thread(this, "SMBSrc");
//        m_thread.start();
//    }

}
