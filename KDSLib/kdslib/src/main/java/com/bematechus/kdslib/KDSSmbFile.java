

package com.bematechus.kdslib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jcifs.smb.ACE;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

import android.app.ProgressDialog;

/**
 *
 *smb://[[[domain;]username[:password]@]server[:port]/[[share/[dir/]file]]][?param=value[param2=value2[...]]]
 * https://jcifs.samba.org/
 */
public class KDSSmbFile extends Handler implements Runnable {
    //SmbFile smbFile;
    static final public String TAG = "KDSSmbFile";
    static final public int REFRESH_LIST = 1;
    static final public int SHOW_PROGRESS = 2;
    static final public int HIDE_PROGRESS = 3;

    private boolean m_isNewFolderEnabled = true;
    private String m_sdcardDirectory = "";
    private Context m_context;
    private TextView m_titleView;

    private String m_dir = "";
    private List<String> m_subdirs = new ArrayList<>();
    private ChosenDirectoryListener m_chosenDirectoryListener = null;
    private ArrayAdapter<String> m_listAdapter = null;

    //boolean m_bEnableProcessDialog = false;
    private ProgressDialog m_progressDialog = null;

    /**
     * https://jcifs.samba.org/src/docs/api/
     * @param remoteUriFolder
     *  smb://[[[domain;]username[:password]@]server[:port]/[[share/[dir/]file]]][?param=value[param2=value2[...]]]
     * @return
     */
    static public ArrayList<String> findAllXmlFiles(String remoteUriFolder, int nMaxFiles)
    {
        ArrayList<String> ar = new ArrayList<String>();
        SmbFile file;
        SmbFile[] files = new SmbFile[0];

        try {
            file = openSmbUri(remoteUriFolder);
            if (file == null) return ar;

//            if (isAnonymous(remoteUriFolder))
//            {
//                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, null, null);
//                file = new SmbFile(remoteUriFolder, auth);
//            }
//            else {
//                file = new SmbFile(remoteUriFolder);
//            }
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return ar;
        }

        //long t1 = System.currentTimeMillis();
        try {
            files = file.listFiles();
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        //long t2 = System.currentTimeMillis() - t1;

        for( int i = 0; i < files.length; i++ ) {
            if (nMaxFiles >0)
                if (ar.size() >= nMaxFiles) break;
            String fileName =files[i].getName();
            fileName = fileName.toUpperCase();
            //if(files[i].getName().indexOf(".xml")!=-1)//fix bug, if the xml is XML, we lost this file.
            if (fileName.indexOf(".XML") != -1)
            {
                ar.add(files[i].getName());
            }
        }
        return ar;
        //System.out.println();
        //System.out.println( files.length + " files in " + t2 + "ms"
    }

    static public boolean isValidPath(String remoteUriFolder)
    {

        try {
            SmbFile file = openSmbUri(remoteUriFolder);
            if (file == null) return false;
//            if (isAnonymous(remoteUriFolder))
//            {
//                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, null, null);
//                file = new SmbFile(remoteUriFolder, auth);
//            }
//            else {
//                file = new SmbFile(remoteUriFolder);
//            }
            return file.exists();

        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,e);//+ e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }

    public static File readFromSmb(String smbMachine, String localpath)
    {
        File localfile=null;
        InputStream bis=null;
        OutputStream bos=null;
        List<File> files = new ArrayList<>();
        try {
            SmbFile rmifile = openSmbUri(smbMachine);
            if (rmifile == null) return localfile;

            String filename=rmifile.getName();
            bis=new BufferedInputStream(new SmbFileInputStream(rmifile));
            localfile=new File(localpath+ File.separator+filename);
            bos=new BufferedOutputStream(new FileOutputStream(localfile));
            int length=rmifile.getContentLength();
            byte[] buffer=new byte[length];
            bis.read(buffer);
            bos.write(buffer);
            try {
                bos.close();
                bis.close();
            } catch (Exception ex) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_(),ex);// + ex.toString());
                //KDSLog.e(TAG, KDSUtil.error( ex));
            }
            files.add(localfile);
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        return localfile;
    }

    static public int BUF_SIZE = 1024000; //10k;
    public static String readFromSmbToLocal(String smbFileName, String localFolderWithoutLastSep)
    {
        File localfile=null;
        InputStream bis=null;
        OutputStream bos=null;
        List<File> files = new ArrayList<>();
        String localFileName = "";
        try {
            SmbFile smbfile = openSmbUri(smbFileName);
            if (smbfile == null) return "";

            String filename=smbfile.getName();
            bis=new BufferedInputStream(new SmbFileInputStream(smbfile));
            localFileName = localFolderWithoutLastSep+ File.separator+filename;
            localfile=new File(localFileName);
            bos=new BufferedOutputStream(new FileOutputStream(localfile));
            int length=smbfile.getContentLength();
            byte[] buffer=new byte[BUF_SIZE];
            int nTotalReceived = 0;
            int nReceived = 0;
            while (nReceived < length)
            {
                nReceived = bis.read(buffer);
                if (nReceived <=0) break;
                bos.write(buffer);
            }
            bos.close();
            bis.close();


        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        return localFileName;
    }



    public static byte[] removeHeaders(byte[] buf, int ncount)
    {
        int nlen = buf.length;
        for (int i=ncount; i< nlen; i++)
        {
            buf[i-ncount] = buf[i];
        }
        for (int i=nlen-ncount; i< nlen; i++)
        {
            buf[i]= 0;
        }
        return buf;
    }
    /**
     * read the smb text file,
     * @param smbFileName
     *      The full path smb file name
     * @return
     *      the file contents.
     */
    public static String readFromUtf8SmbText(String smbFileName)
    {

        InputStream bis=null;
        String text = "";
        try {
            SmbFile rmifile = openSmbUri(smbFileName);
            if (rmifile == null) return "";
//            if (isAnonymous(smbFileName))
//            {
//                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, null, null);
//                rmifile = new SmbFile(smbFileName, auth);
//            }
//            else {
//                rmifile = new SmbFile(smbFileName);
//            }
            //String filename=rmifile.getName();
            bis=new BufferedInputStream(new SmbFileInputStream(rmifile));

            int length=rmifile.getContentLength();
            byte[] buffer=new byte[length];
            bis.read(buffer); //utf8 bytes
            int noffset = 0;
            int ncount = buffer.length;
            if (buffer.length >3)
            {
                //BOM: EF BB BF, start for UTF-8 file.
                if (buffer[0] ==(byte) 0xEF &&
                        buffer[1] == (byte)0xBB &&
                        buffer[2] == (byte)0xBF )
                {
                    noffset = 3;
                    ncount = length - 3;
                    //buffer = removeHeaders(buffer, 3);

                }
            }
            text = KDSUtil.convertUtf8BytesToString(buffer, noffset, ncount);

            try {

                bis.close();
            } catch (IOException e) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
                //KDSLog.e(TAG, KDSUtil.error( e));
            }

        } catch (Exception e) {

            KDSLog.e(TAG, KDSLog._FUNCLINE_()+"Error file:" + smbFileName);
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }

        //BOM: EF BB BF, start for UTF-8 file.
//
//        if (text.length() >1) {
//            if (text.charAt(0) == 0xfeff)
////            if (text.charAt(0) == 0xEF ||
////                    text.charAt(1) == 0xBB ||
////                    text.charAt(2) == 0xBF )
//            {
//                text = text.substring(1);
//            }
//
//        }

        return text;
    }

    static public SmbFile openSmbUri(String uri)
    {
        SmbFile f = null;

        try {


            if (isAnonymous(uri)) {
                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, null, null);
                f = new SmbFile(uri, auth);
            } else {
                f = new SmbFile(uri);
            }
            return f;
        }
        catch ( Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        return null;
    }
    static public boolean deleteUriFile(String uri)
    {
        try
        {
            SmbFile f = openSmbUri(uri);
            if (f == null) return false;
//
//            if (isAnonymous(uri))
//            {
//                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, null, null);
//                rmifile = new SmbFile(uri, auth);
//            }
//            else {
//                SmbFile f = new SmbFile(uri);
//            }
            f.delete();
            return true;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);//+ e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }

    /**
     *
     * @param smbFolder
     * @return
     *  0: OK
     *   -1: can not open folder
     *   -2: can not write.
     *   -3: can not delete
     *
     */
    static public int checkFolderWritable(String smbFolder)
    {
        String subFolderName = KDSUtil.createNewGUID();// "6f1dfc4dc08948859eaf298fa17d7e83";
        SmbFile folderFile = openSmbUri(smbFolder);
        try {
            if (!folderFile.exists())
                return -1;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            return -1;
        }
        if (folderFile == null)
            return -1;
        subFolderName = smbFolder + subFolderName;
        if (!createSubDir(subFolderName))
            return -2;
        subFolderName +="/";
        if (!deleteUriFile(subFolderName))
            return -3;
        return 0;
    }

    /**
     *
     * @param uri
     *  The root uri, e.g: smb://
     * @return
     */
    public ArrayList<String> listAllValidComputers(String uri)
    {
        ArrayList<String> ar = new ArrayList<String>();
        try {
            SmbFile smbf = openSmbUri(uri);//  new SmbFile(uri);
            if (smbf == null) return ar;
            String[] files = new String[0];
            files = smbf.list();

            for( int i = 0; i < files.length; i++ ) {
                ar.add(files[i]);
            }

            return ar;

        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
        return ar;

        //SmbFile file = new SmbFile()

    }

    // 向共享目录上传文件
    public static void smbPut(String remoteUrl, String localFilePath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File localFile = new File(localFilePath);

            String fileName = localFile.getName();
            // SmbFile remoteFile = new SmbFile(remoteUrl + "/" + fileName);
            SmbFile remoteFile = openSmbUri(remoteUrl + "/" + fileName);
            if (remoteFile == null) return;
            in = new BufferedInputStream(new FileInputStream(localFile));
            out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
                //KDSLog.e(TAG, KDSUtil.error( e));
            }
        }
    }


    /**
     * write the contents to remote folder given file.
     * @param remoteUrl
     * with the last "/"
     * @param fileName
     * @param localFileContent
     */
    public static boolean smbPut(String remoteUrl, String fileName, String localFileContent) {
        InputStream in = null;
        OutputStream out = null;
        try {
            //File localFile = new File(localFilePath);

            //String fileName = localFile.getName();
            //SmbFile remoteFile = new SmbFile(remoteUrl + fileName);
            SmbFile remoteFile = openSmbUri(remoteUrl + fileName);
            if (remoteFile == null) return false;
            //in = new BufferedInputStream(new FileInputStream(localFile));
            out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
            byte[] buffer = KDSUtil.convertStringToUtf8Bytes(localFileContent);
            //byte[] buffer = new byte[1024];
            //while (in.read(buffer) != -1) {
            out.write(buffer);

            //  buffer = new byte[1024];
            //}
        } catch (Exception e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        } finally {
            try {
                out.close();
                return true;
                //  in.close();
            } catch (Exception e) {
                KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
                //KDSLog.e(TAG, KDSUtil.error( e));
            }
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    //dialog


    //////////////////////////////////////////////////////
    // Callback interface for selected directory
    //////////////////////////////////////////////////////
    public interface ChosenDirectoryListener
    {
        public void onChosenDir(String chosenDir);
    }

    public KDSSmbFile(Context context, ChosenDirectoryListener chosenDirectoryListener)
    {
        m_context = context;
        m_sdcardDirectory = "smb://";// Environment.getExternalStorageDirectory().getAbsolutePath();
        m_chosenDirectoryListener = chosenDirectoryListener;

        try
        {
            // m_sdcardDirectory = new SmbFile(m_sdcardDirectory).getCanonicalPath();
            m_sdcardDirectory = openSmbUri(m_sdcardDirectory).getCanonicalPath();
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // setNewFolderEnabled() - enable/disable new folder button
    ///////////////////////////////////////////////////////////////////////

    public void setNewFolderEnabled(boolean isNewFolderEnabled)
    {
        m_isNewFolderEnabled = isNewFolderEnabled;
    }

    public boolean getNewFolderEnabled()
    {
        return m_isNewFolderEnabled;
    }

    ///////////////////////////////////////////////////////////////////////
    // chooseDirectory() - load directory chooser dialog for initial
    // default sdcard directory
    ///////////////////////////////////////////////////////////////////////

    public void chooseDirectory()
    {
        // Initial directory is sdcard directory
        chooseDirectory(m_sdcardDirectory);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // chooseDirectory(String dir) - load directory chooser dialog for initial
    // input 'dir' directory
    ////////////////////////////////////////////////////////////////////////////////

    public void chooseDirectory(String dir)
    {
//        try {
//            SmbFile dirFile = new SmbFile(dir);
//
//            if (!dirFile.exists() || !dirFile.isDirectory()) {
//                dir = m_sdcardDirectory;
//            }
//        }
//        catch (Exception e)
//        {
//
//        }
//
//        try
//        {
//            dir = new SmbFile(dir).getCanonicalPath();
//        }
//        catch (IOException ioe)
//        {
//            return;
//        }

        m_dir = dir;
        refreshDirectories();
        //m_subdirs = getDirectories(dir);

        class DirectoryOnClickListener implements OnClickListener
        {
            public void onClick(DialogInterface dialog, int item)
            {
                // Navigate into the sub-directory
                m_dir +=  ((AlertDialog) dialog).getListView().getAdapter().getItem(item);// + "/";
                updateDirectory();
            }
        }

        AlertDialog.Builder dialogBuilder =
                createDirectoryChooserDialog(dir, m_subdirs, new DirectoryOnClickListener());

        dialogBuilder.setPositiveButton(R.string.ok, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Current directory chosen
                if (m_chosenDirectoryListener != null)
                {
                    // Call registered listener supplied with the chosen directory
                    m_chosenDirectoryListener.onChosenDir(m_dir);
                }
            }
        }).setNegativeButton(R.string.cancel, null);

        final AlertDialog dirsDialog = dialogBuilder.create();

        dirsDialog.setOnKeyListener(new OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    return upDir();
//                    // Back button pressed
//                    if ( m_dir.equals(m_sdcardDirectory) )
//                    {
//                        // The very top level directory, do nothing
//                        return false;
//                    }
//                    else
//                    {
//                        try {
//                            // Navigate back to an upper directory
//                            m_dir = new SmbFile(m_dir).getParent();
//                        }
//                        catch (Exception e)
//                        {
//                            return false;
//                        }
//                        updateDirectory();
//                    }
//
//                    return true;
                }
                else if (keyCode == KeyEvent.KEYCODE_CTRL_RIGHT || keyCode == KeyEvent.KEYCODE_CTRL_LEFT)
                {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                }
                else
                {
                    return false;
                }
            }
        });

        // Show directory chooser dialog
        dirsDialog.show();
    }

    static public boolean createSubDir(String newDir)
    {
        try {
            SmbFile newDirFile = openSmbUri(newDir);// new SmbFile(newDir);
            if (!newDirFile.exists()) {
                newDirFile.mkdir();
                return true;
            }
            else
                return true;
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }
    }

    private void refreshDirectories()
    {
        Message m = new Message();
        m.what = SHOW_PROGRESS;
        sendMessage(m);
        new Thread(this, "checkdir").start();


    }

    static public boolean isAnonymous(String dir)
    {
        return (dir.indexOf("smb:// : @")>=0);
    }
    /**
     *
     * @param dir
     * The root dir
     *  smb://[[[domain;]username[:password]@]server[:port]/[[share/[dir/]file]]][?param=value[param2=value2[...]]]
     * @return
     *  The all dirs in this root folder
     *  Format:
     *      AAA/
     *      BBB/
     *      Downloads/
     */
    static public List<String> getDirectories(String dir, ArrayList<String> errors)
    {
        List<String> dirs = new ArrayList<String>();

        try
        {
            SmbFile dirFile = openSmbUri(dir);
//            if (isAnonymous(dir)) {
//                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, null, null);
//                dirFile = new SmbFile(dir, auth);
//            }
//            else {
//                dirFile = new SmbFile(dir);//, auth);
//            }
            if (! dirFile.exists() || ! dirFile.isDirectory())
            {
                if (errors != null) {

                    errors.add(KDSApplication.getContext().getString(R.string.folder_not_existed));//"The folder isn't existed.");
                }
                return dirs;
            }
//            if (dir.equals("smb://"))
//            {
//                for (String file : dirFile.list()) {
//                    //if (file.isDirectory()) {
//                        dirs.add(file);
//                 }
//                //}
//            }
//            else
            {
                SmbFile files[] = dirFile.listFiles();
                //for (SmbFile file : dirFile.listFiles()) {
                for (SmbFile file : files) {
                    if (file.isDirectory()) {
                        dirs.add(file.getName());
                    }
                }
            }
        }
        catch (Exception e)
        {
            if (errors != null)
                errors.add(KDSApplication.getContext().getString(R.string.failed_to_open_remote_folder));

            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG,KDSLog._FUNCLINE_() + KDSUtil.error(e) );
        }

        Collections.sort(dirs, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        return dirs;
    }

    /**
     *
     * @param dir
     * The root dir
     *  smb://[[[domain;]username[:password]@]server[:port]/[[share/[dir/]file]]][?param=value[param2=value2[...]]]
     * @return
     *  The all dirs in this root folder
     *  Format:
     *      AAA/
     *      BBB/
     *      Downloads/
     */
    static public List<SmbFile> getFiles(String dir)
    {
        //List<String> files = new ArrayList<String>();
        List<SmbFile> returnFiles = new ArrayList<>();

        try
        {
            SmbFile dirFile = openSmbUri(dir);

            if (! dirFile.exists() || ! dirFile.isDirectory())
            {
//                if (errors != null) {
//                    errors.add("The folder isn't existed.");
//                }
                return returnFiles;
            }
            SmbFile files[] = dirFile.listFiles();
            //for (SmbFile file : dirFile.listFiles()) {
            for (SmbFile file : files) {
                returnFiles.add(file);
            }

        }
        catch (Exception e)
        {
//            if (errors != null)
//                errors.add(e.getMessage());
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }

//        Collections.sort(returnFiles, new Comparator<SmbFile>() {
//            public int compare(String o1, String o2) {
//                return o1.compareTo(o2);
//            }
//        });

        return returnFiles;
    }

    static public SmbFile getDirFile(String dir)
    {

        try
        {
            SmbFile dirFile = openSmbUri(dir);

            if (! dirFile.exists() || ! dirFile.isDirectory())
            {
//                if (errors != null) {
//                    errors.add("The folder isn't existed.");
//                }
                return null;
            }

            return dirFile;

        }
        catch (Exception e)
        {

            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }


        return null;
    }

    static public ACE getDirACE(String dir)
    {

        SmbFile f = getDirFile(dir);
        if (f == null)
            return null;
        try {
            ACE ar[] = f.getSecurity(true);
            KDSSMBPath path = KDSSMBPath.parseString(dir);
            ACE myAccountACE = null;
            for (int i=0; i< ar.length; i++)
            {
                ACE a = ar[i];
                if (a.getSID().getAccountName().equals(path.getUserID()))
                {
                    myAccountACE = a;
                    break;
                }
            }
            return myAccountACE ;

        }
        catch ( Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + KDSLog.getStackTrace(e));
            //e.printStackTrace();
        }

        return null;

    }


    /**
     *
     * @param strSubFolder
     *  The sub folder name, with last "/"
     * @return
     */
    static public boolean isExistedSubFolder(String strRootFolder, String strSubFolder)
    {
        List<String> arFolders =  getDirectories(strRootFolder, null); //format:  "downloads/"
        String str = strSubFolder + "/";
        for (int i=0; i< arFolders.size(); i++)
        {
            if (arFolders.get(i).equals(str))
                return true;
        }
        return false;
    }

    private void refreshTitle(String dir)
    {
        KDSSMBPath smb = KDSSMBPath.parseString(dir);
        m_titleView.setText(smb.toFolderString());
    }

    private boolean upDir()
    {
        KDSSMBPath smb = KDSSMBPath.parseString(m_dir);
        if (smb.getFolder().isEmpty()) return false;
        if ( m_dir.equals(m_sdcardDirectory) )
        {
            // The very top level directory, do nothing
            return false;
        }
        else
        {
            try {
                // Navigate back to an upper directory
                //m_dir = new SmbFile(m_dir).getParent();
                m_dir = openSmbUri(m_dir).getParent();
            }
            catch (Exception e)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
                //KDSLog.e(TAG, KDSUtil.error( e));
                return false;
            }
            updateDirectory();
        }
        return true;
    }

    private AlertDialog.Builder createDirectoryChooserDialog(String title, List<String> listItems,
                                                             OnClickListener onClickListener)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(m_context);

        // Create custom view for AlertDialog title containing
        // current directory TextView and possible 'New folder' button.
        // Current directory TextView allows long directory path to be wrapped to multiple lines.
        LinearLayout titleLayout = new LinearLayout(m_context);
        titleLayout.setOrientation(LinearLayout.VERTICAL);

        m_titleView = new TextView(m_context);
        m_titleView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        m_titleView.setTextAppearance(m_context, android.R.style.TextAppearance_Large);
        m_titleView.setTextColor( m_context.getResources().getColor(android.R.color.black) );
        m_titleView.setTextColor(m_context.getResources().getColor(android.R.color.holo_blue_light));//.black) );
        m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        //m_titleView.setText(title);
        refreshTitle(title);

        Button newDirButton = new Button(m_context);
        newDirButton.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        newDirButton.setText(m_context.getString(R.string.new_folder));
        newDirButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final EditText input = new EditText(m_context);

                // Show new folder name input dialog
                new AlertDialog.Builder(m_context).
                        setTitle(m_context.getString(R.string.new_folder)).//"New folder name").
                        setView(input).setPositiveButton(R.string.ok, new OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        Editable newDir = input.getText();
                        String newDirName = newDir.toString();
                        // Create new directory
                        if ( createSubDir(m_dir + "/" + newDirName) )
                        {
                            // Navigate into the new directory
                            m_dir += "/" + newDirName;
                            updateDirectory();
                        }
                        else
                        {
                            String strErr = m_context.getString(R.string.fail_to_create_folder);
                            strErr = strErr.replace("#", newDirName);
                            Toast.makeText(
                                    m_context, strErr, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(R.string.cancel, null).show();
            }
        });

        if (! m_isNewFolderEnabled)
        {
            newDirButton.setVisibility(View.GONE);
        }


        ////////////////////
        Button backDirButton = new Button(m_context);
        backDirButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        backDirButton.setText(m_context.getString(R.string.up));
        backDirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Back button pressed
                upDir();
            }
        });


        //     newDirButton.setVisibility(View.GONE);

        ////////////////////

        titleLayout.addView(m_titleView);
        titleLayout.addView(newDirButton);
        titleLayout.addView(backDirButton);

        dialogBuilder.setCustomTitle(titleLayout);

        m_listAdapter = createListAdapter(listItems);

        dialogBuilder.setSingleChoiceItems(m_listAdapter, -1, onClickListener);
        dialogBuilder.setCancelable(false);

        return dialogBuilder;
    }



    private void updateDirectory()
    {
        m_subdirs.clear();
        refreshDirectories();
        //m_subdirs.addAll( getDirectories(m_dir) );
        //m_titleView.setText(m_dir);
        refreshTitle(m_dir);
        m_listAdapter.notifyDataSetChanged();
    }



    private ArrayAdapter<String> createListAdapter(List<String> items)
    {
        return new ArrayAdapter<String>(m_context,
                android.R.layout.select_dialog_item, android.R.id.text1, items)
        {
            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent)
            {
                View v = super.getView(position, convertView, parent);

                if (v instanceof TextView)
                {
                    // Enable list item (directory) text wrapping
                    TextView tv = (TextView) v;
                    tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                    tv.setEllipsize(null);
                }
                return v;
            }
        };
    }

    ArrayList<String> m_arLastError = new ArrayList<>();

    public void run()
    {

        m_arLastError.clear();
        m_subdirs.clear();
        m_subdirs.addAll(getDirectories(m_dir, m_arLastError));
        Message m = new Message();
        m.what =  REFRESH_LIST;

        this.sendMessage(m);
        //Looper.prepare();
        //threadSelect();
        //Looper.loop();
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case REFRESH_LIST: {
                hideProgressDialog();
                m_listAdapter.notifyDataSetChanged();
                //if (m_subdirs.size() <=0)
                if (m_arLastError.size() >0)
                {
                    Toast.makeText(   m_context, m_arLastError.get(0), Toast.LENGTH_LONG).show();
                }
                else if (m_subdirs.size() <=0)
                {
                    Toast.makeText(   m_context, m_context.getString(R.string.empty) , Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(   m_context, m_context.getString(R.string.find_folders) , Toast.LENGTH_SHORT).show();
            }
            break;
            case SHOW_PROGRESS:
            {
                showProgressDialog(m_context, "", m_context.getString(R.string.waiting));
            }
            break;
            case HIDE_PROGRESS:
            {
                hideProgressDialog();
            }
            break;
            default:
                break;
        }
    }

    private void showProgressDialog(Context context, String title, String message){

        if (context == null) return;
        // if (m_progressDialog == null)
        //{
            m_progressDialog = new ProgressDialog(context);
            m_progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//.STYLE_HORIZONTAL);

            //m_progressDlg.setCancelable(true);
           // m_progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,KDSApplication.getContext().getString(R.string.cancel), new OnClickListener() {
           //     @Override
           //     public void onClick(DialogInterface dialog, int which) {
           //         hideProgressDialog();
           //     }
           // });

        //}
//        m_progressDialog.setMax(100);
//        m_progressDialog.setProgress(0);
        m_progressDialog.setTitle(title);
        m_progressDialog.setMessage(message);
        m_progressDialog.show();

        Point size = new Point();
        m_progressDialog.getWindow().getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;//获取界面的宽度像素
        int height = size.y;
        WindowManager.LayoutParams params = m_progressDialog.getWindow().getAttributes();//一定要用mProgressDialog得到当前界面的参数对象，否则就不是设置ProgressDialog的界面了
        params.alpha = 0.8f;//设置进度条背景透明度
        params.height = height/8;//设置进度条的高度
        params.gravity = Gravity.CENTER;//设置ProgressDialog的重心
        params.width = 200;// width/5;//4*width/5;//设置进度条的宽度
        params.dimAmount = 0f;//设置半透明背景的灰度，范围0~1，系统默认值是0.5，1表示背景完全是黑色的,0表示背景不变暗，和原来的灰度一样
        m_progressDialog.getWindow().setAttributes(params);//把参数设置给进度条，注意，一定要先show出来才可以再设置，不然就没效果了，因为只有当界面显示出来后才可以获得它的屏幕尺寸及参数等一些信息

        //downloadApk();
    }
    private void hideProgressDialog()
    {
        if (m_progressDialog != null)
            m_progressDialog.hide();
    }

}


