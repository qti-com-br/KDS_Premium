package com.bematechus.kdslib;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSSMBPath;
import com.bematechus.kdslib.KDSSmbExplorerHandler;
//import com.bematechus.kdslib.KDSSmbFile1;
import com.bematechus.kdslib.KDSSmbFile2;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.OpenFileDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import jcifs.smb.SmbFile;

/**
 * Created by Administrator on 2016/9/19.
 */
public class OpenSmbFileDialog extends KDSUIDialogBase implements AdapterView.OnItemClickListener, KDSSmbExplorerHandler.interfaceSmbGotAllFiles{

    public static String TAG = "OpenSmbFileDialog";


    static final public String STRING_ROOT = "/";
    static final public String STRING_PARENT = "..";
    static final public String STRING_FOLDER = ".";
    static final public String STRING_EMPTY = "";

    ListView m_lstFiles = null;
    TextView m_txtSelected = null;
    String m_strSelectedFile = "";

    TextView m_txtIP = null;
    TextView m_txtUserID = null;
    TextView m_txtPwd = null;
    CheckBox m_chkAnonymous = null;

    Button m_btnLogin = null;

    private String m_path = STRING_ROOT;
    private List<Map<String, Object>> m_listData = null;

    private String m_strExtension = "";
    private String m_strRootPath = "";

    private Map<String, Integer> m_mapImages = null;
    KDSSmbExplorerHandler m_handler = new KDSSmbExplorerHandler(this);

    public Object getResult() {
        return m_strSelectedFile;
    }

    public OpenSmbFileDialog(final Context context,String strExtension, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.setUseCtrlEnterKey(true);
        this.int_dialog(context, listener, R.layout.kdsui_dlg_smb_explorer, "");
        this.setTitle(context.getString(R.string.open_file));//"Open file");
        m_strExtension = strExtension;
        m_lstFiles = (ListView) this.getView().findViewById(R.id.lstFiles);

        m_txtSelected = (TextView) this.getView().findViewById(R.id.txtSelected);

        m_txtIP = (TextView)this.getView().findViewById(R.id.txtIP);
        m_txtUserID = (TextView)this.getView().findViewById(R.id.txtUserID);
        m_txtPwd = (TextView)this.getView().findViewById(R.id.txtPwd);
        m_chkAnonymous = (CheckBox)this.getView().findViewById(R.id.chkAnonymous);
        m_chkAnonymous.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onAnonymousChanged();
            }
        });
        m_btnLogin = (Button)this.getView().findViewById(R.id.btnLogin);

        m_mapImages = new HashMap<String, Integer>();
        // ????????????????????????????????????????????? ?????????????????????????????????????????????
        m_mapImages.put(OpenFileDialog.STRING_ROOT, R.drawable.root);   // ???????????????
        m_mapImages.put(OpenFileDialog.STRING_PARENT, R.drawable.folder_up);    //????????????????????????
        m_mapImages.put(OpenFileDialog.STRING_FOLDER, R.drawable.folder);   //???????????????
        m_mapImages.put("file", R.drawable.file);   //wav????????????
        m_mapImages.put(OpenFileDialog.STRING_EMPTY, R.drawable.file);
        m_lstFiles.setOnItemClickListener(this);

        m_btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        m_listData = new ArrayList<Map<String, Object>>();
        //SimpleAdapter adapter = new SimpleAdapter(this.getView().getContext(), m_listData, R.layout.listitem_explorer, new String[]{"img", "name", "path"}, new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path});
        SimpleAdapter adapter = new SimpleAdapter(this.getView().getContext(), m_listData, R.layout.listitem_explorer, new String[]{"img", "name", "path"}, new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path})
        {
            public void setViewText(TextView v, String text) {
                if (v.getId() == R.id.filedialogitem_path)
                {
                    text = removeUserIDPasswordForShowing(text);
                }
                v.setText(text);
            }
        };
        m_lstFiles.setAdapter(adapter);

        loadLastSmbSetting();
    }

    private void saveLastSmbSetting(){


        Context ctx = this.getView().getContext();
        SharedPreferences sp =ctx.getSharedPreferences("smblastpath", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        editor.putString("ip",m_txtIP.getText().toString());
        editor.putString("user",m_txtUserID.getText().toString());
        editor.putString("pwd",m_txtPwd.getText().toString());
        editor.putBoolean("anonymous",m_chkAnonymous.isChecked());

        editor.commit();


    }
    private void loadLastSmbSetting(){


        Context ctx = this.getView().getContext();
        SharedPreferences sp =ctx.getSharedPreferences("smblastpath", Context.MODE_PRIVATE);

        m_txtIP.setText(sp.getString("ip",""));
        m_txtUserID.setText(sp.getString("user",""));
        m_txtPwd.setText(sp.getString("pwd",""));
        m_chkAnonymous.setChecked(sp.getBoolean("anonymous", false));

    }

//    static public String getFuncKeyName(KDSRouterSettings.ID funcKey)
//    {
//        String strFunc = "";
//        if (funcKey == KDSRouterSettings.ID.Bumpbar_OK)
//            strFunc = "[Enter]";// KDSRouterSettings.getOkKeyString(context);
//        else if (funcKey == KDSRouterSettings.ID.Bumpbar_Cancel)
//            strFunc = "[Ctrl]";// KDSRouterSettings.getCancelKeyString(context);
//
//        return strFunc;
//    }

    public void onAnonymousChanged()
    {
        boolean bEnable =(!m_chkAnonymous.isChecked());

        m_txtUserID.setEnabled(bEnable);
        m_txtPwd.setEnabled(bEnable);

    }
//    public KDSRouterSettings.ID checkKdbEvent(KeyEvent event)
//    {
//        if (event.getKeyCode() == KeyEvent.KEYCODE_CTRL_LEFT)
//            return KDSRouterSettings.ID.Bumpbar_Cancel;
//        else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
//            return KDSRouterSettings.ID.Bumpbar_OK;
//        return KDSRouterSettings.ID.NULL;
//    }

    private void login()
    {


        String ip = m_txtIP.getText().toString();
        String user = m_txtUserID.getText().toString();
        String pwd = m_txtPwd.getText().toString();
        boolean banonymous = m_chkAnonymous.isChecked();

        hideKeyboard(m_txtPwd);


        KDSSMBPath smbPath = new KDSSMBPath();
        smbPath.setPCName(ip);

        if (banonymous) {
            smbPath.setUserID(" ");
            smbPath.setPwd(" ");
        }
        else {
            smbPath.setUserID(user);
            smbPath.setPwd(pwd);
        }
        m_path = smbPath.toString();
        m_strRootPath = m_path;
        refreshFileList();


        saveLastSmbSetting();
    }

    private String getSuffix(String filename) {
        int dix = filename.lastIndexOf('.');
        if (dix < 0) {
            return "";
        } else {
            return filename.substring(dix + 1);
        }
    }

    private int getImageId(String s) {
        if (m_mapImages == null) {
            return 0;
        } else if (m_mapImages.containsKey(s)) {
            return m_mapImages.get(s);
        } else if (m_mapImages.containsKey(STRING_EMPTY)) {
            return m_mapImages.get(STRING_EMPTY);
        } else {
            return 0;
        }
    }

    //List<jcifs.smb.SmbFile> m_smb1Files = null; //kpp1-376
    //List<jcifsng.smb.SmbFile> m_smb2Files = null;//kpp1-376
    List<jcifs.smb.SmbFile> m_smb2Files = null;

    public void getAllFiles(String smbFolder)
    {


//        if (m_listData != null) {
//            m_listData.clear();
//        }
        KDSSmbFile2.setEnableSmbV2(true);
        Object objs[] = new Object[]{smbFolder};


        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    String folder = (String) objects[0];
                    KDSSMBPath path = KDSSMBPath.parseString(folder);
                    boolean isRoot = false;
                    if (path.getFolder().equals(""))
                    {
                        isRoot = true;
                    }
//                    if (KDSSmbFile.getEnabledSmbV2())//kpp1-376
                    {
                        if (isRoot)
                        {
                            String s = KDSSmbFile2.isValidLoginParameters(folder);
                            if (!s.isEmpty()){
                                m_handler.sendLoginError(s);
                                return null;
                            }
                        }
                        m_smb2Files = KDSSmbFile2.getFiles(folder);
                    }
//                    else { //kpp1-376
//                        if (isRoot)
//                        { //check if the login paramter is correct.
//                            String s = KDSSmbFile1.isValidLoginParameters(folder);
//                            if (!s.isEmpty()){
//                                m_handler.sendLoginError(s);
//                                return null;
//                            }
//                        }
//                        m_smb1Files = KDSSmbFile1.getFiles(folder);
//                    }
                    m_handler.sendRefreshMessage();

                } catch(Exception e) {
                    KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);//, e.toString());
                }


                return null;

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, objs);


    }

    //private int addFilesToList(List<jcifs.smb.SmbFile> arSmb1Files,List<jcifsng.smb.SmbFile> arSmb2Files ) //kpp1-376
    private int addFilesToList(List<jcifs.smb.SmbFile> arSmb2Files )
    {

        if (m_listData != null) {
            m_listData.clear();
        } else {
            m_listData = new ArrayList<Map<String, Object>>();
        }
//        if (KDSSmbFile.getEnabledSmbV2()) //kpp1-376
        {
            if (arSmb2Files == null) return 0;
        }
//        else //kpp1-376
//        {
//            if (arSmb1Files == null) return 0;
//        }


        // ???????????????????????????????????????????????????
        ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String, Object>>();
        ArrayList<Map<String, Object>> lfiles = new ArrayList<Map<String, Object>>();

        if (!this.m_path.equals(STRING_ROOT)) {
            // ??????????????? ??? ???????????????
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", STRING_ROOT);
            map.put("path", STRING_ROOT);
            map.put("img", getImageId(STRING_ROOT));
            m_listData.add(map);

            map = new HashMap<String, Object>();
            map.put("name", STRING_PARENT);
            map.put("path", m_path);
            map.put("img", getImageId(STRING_PARENT));
            m_listData.add(map);
        }

        try {
            //if (KDSSmbFile.getEnabledSmbV2())//kpp1-376
            {
                for (int i = 0; i < arSmb2Files.size(); i++) {
                    //jcifsng.smb.SmbFile file = arSmb2Files.get(i); //kpp1-376
                    //addToList(lfolders, lfiles, file.getName(), file.getPath(), file.isDirectory(), file.isFile());

                    jcifs.smb.SmbFile file = arSmb2Files.get(i);
                    addToList(lfolders, lfiles, file.getName(), file.getPath(), file.isDirectory(), file.isFile());
//                    if (file.isDirectory()) {
//                        // ???????????????
//                        Map<String, Object> map = new HashMap<String, Object>();
//                        map.put("name", file.getName());
//                        map.put("path", file.getPath());
//                        map.put("img", getImageId(STRING_FOLDER));
//                        lfolders.add(map);
//                    } else if (file.isFile()) {
//                        // ????????????
//                        String extension = getSuffix(file.getName()).toLowerCase();
//                        if (m_strExtension == null || m_strExtension.length() == 0 || (extension.length() > 0 && m_strExtension.indexOf("." + extension + ";") >= 0)) {
//                            Map<String, Object> map = new HashMap<String, Object>();
//                            map.put("name", file.getName());
//                            map.put("path", file.getPath());
//                            map.put("img", getImageId(extension));
//                            lfiles.add(map);
//                        }
//                    }
                }
            }
//            else //kpp1-376
//            {
//                for (int i = 0; i < arSmb1Files.size(); i++) {
//                    jcifs.smb.SmbFile file = arSmb1Files.get(i);
//                    addToList(lfolders, lfiles, file.getName(), file.getPath(), file.isDirectory(), file.isFile());
//
////                    if (file.isDirectory()) {
////                        // ???????????????
////                        Map<String, Object> map = new HashMap<String, Object>();
////                        map.put("name", file.getName());
////                        map.put("path", file.getPath());
////                        map.put("img", getImageId(STRING_FOLDER));
////                        lfolders.add(map);
////                    } else if (file.isFile()) {
////                        // ????????????
////                        String extension = getSuffix(file.getName()).toLowerCase();
////                        if (m_strExtension == null || m_strExtension.length() == 0 || (extension.length() > 0 && m_strExtension.indexOf("." + extension + ";") >= 0)) {
////                            Map<String, Object> map = new HashMap<String, Object>();
////                            map.put("name", file.getName());
////                            map.put("path", file.getPath());
////                            map.put("img", getImageId(extension));
////                            lfiles.add(map);
////                        }
////                    }
//                }
//            }
        }
        catch (Exception err)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),err);// + KDSLog.getStackTrace(err));
        }
        m_listData.addAll(lfolders); // ???????????????????????????????????????????????????
        m_listData.addAll(lfiles);    //???????????????

        ((SimpleAdapter)m_lstFiles.getAdapter()).notifyDataSetChanged();
        //if (KDSSmbFile.getEnabledSmbV2())//kpp1-376
            return arSmb2Files.size();
//        else //kpp1-376
//            return arSmb1Files.size();

    }

    private void addToList( ArrayList<Map<String, Object>> lfolders, ArrayList<Map<String, Object>> lfiles, String fileName, String filePath, boolean bIsDir, boolean bIsFile )
    {

        if (bIsDir) {
            // ???????????????
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", fileName);
            map.put("path", filePath);
            map.put("img", getImageId(STRING_FOLDER));
            lfolders.add(map);
        } else if (bIsFile) {
            // ????????????
            String extension = getSuffix(fileName).toLowerCase();
            if (m_strExtension == null || m_strExtension.length() == 0 || (extension.length() > 0 && m_strExtension.indexOf("." + extension + ";") >= 0)) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("name", fileName);
                map.put("path", filePath);
                map.put("img", getImageId(extension));
                lfiles.add(map);
            }
        }
    }

    private void refreshFileList() {
        // ??????????????????

        getAllFiles(m_path);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // ????????????
        String filepath = (String) m_listData.get(position).get("path");
        String filename = (String) m_listData.get(position).get("name");
        try {
            if (filename.equals(STRING_ROOT))
            {
                m_path =m_strRootPath;
            }
            else if ( filename.equals(STRING_PARENT)) {
                // ?????????????????????????????????
                String ppt = "";
                //if (KDSSmbFile.getEnabledSmbV2())//kpp1-376
                {
                    //jcifsng.smb.SmbFile fl = new jcifsng.smb.SmbFile(filepath); //kpp1-376
                    jcifs.smb.SmbFile fl = new jcifs.smb.SmbFile(filepath);
                    ppt = fl.getParent();
                }
//                else //kpp1-376
//                {
//                    jcifs.smb.SmbFile fl = new jcifs.smb.SmbFile(filepath);
//                    ppt = fl.getParent();
//                }

//                SmbFile fl = new SmbFile(filepath);
//                String ppt = fl.getParent();

                if (ppt != null) {
                    // ???????????????
                    m_path = ppt;
                    if (m_path.equals( "smb://"))
                        m_path =m_strRootPath;
                } else {
                    // ???????????????
                    m_path =m_strRootPath;
                    // "";//STRING_ROOT;
                }
            } else {
                //SmbFile fl = new SmbFile(filepath);
                boolean bIsDir = false;
                boolean bIsFile = false;
                int nImg  = (int) m_listData.get(position).get("img");

                bIsDir = (nImg == getImageId(STRING_FOLDER));
                bIsFile = (!bIsDir);

//                if (KDSSmbFile.getEnabledSmbV2())
//                {
//                    int nImg  = (int) m_listData.get(position).get("img");
//
//                    bIsDir = (nImg == getImageId(STRING_FOLDER));
//                    bIsFile = (!bIsDir);
//
//
////                    jcifsng.smb.SmbFile fl = new jcifsng.smb.SmbFile(filepath);
////                    bIsDir = fl.isDirectory();
////                    bIsFile = fl.isFile();
//
//                }
//                else
//                {
//                    jcifs.smb.SmbFile fl = new jcifs.smb.SmbFile(filepath);
//                    bIsDir = fl.isDirectory();
//                    bIsFile = fl.isFile();
//                }

                if (bIsFile) {
                    // ???????????????
                    //((Activity)getContext()).dismissDialog(this.dialogid); // ???????????????????????????
                    m_strSelectedFile = filepath;// + "/" +filename;
                    m_txtSelected.setText(removeUserIDPasswordForShowing(m_strSelectedFile));
                    enableOKButton(true);
                    return;
                } else if (bIsDir) {
                    m_path = filepath;

                }
            }
        }
        catch(Exception err)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_() ,err);//+ KDSLog.getStackTrace(err));
        }
        this.refreshFileList();
    }
    public void onSmbGetAllFiles()
    {
        //addFilesToList(m_smb1Files, m_smb2Files); //kpp1-376
        addFilesToList( m_smb2Files);
        m_txtSelected.setText(removeUserIDPasswordForShowing(m_path));
    }

    public void show() {
        dialog.show();
        enableOKButton(false);
    }

    public static void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private String removeUserIDPasswordForShowing(String text)
    {
        int n0 = text.indexOf("//");
        int n1 = text.indexOf("@");
        if (n0 <0 || n1<0 ) return text;
        String s = text.substring(0, n0+2);
        String s1 = text.substring(n1+1);
        return s + s1;
    }

    public void onSmbErrorLogin(String errorMessage)
    {
        Toast.makeText(this.getView().getContext(),  errorMessage, Toast.LENGTH_LONG).show();
    }
}

//package com.bematechus.kdsrouter;
//
//import android.app.Instrumentation;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.ListView;
//import android.widget.SimpleAdapter;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.bematechus.kdslib.KDSLog;
//import com.bematechus.kdslib.KDSSMBPath;
//import com.bematechus.kdslib.KDSSmbFile;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import jcifs.smb.SmbFile;
//
///**
// * Created by Administrator on 2016/9/19.
// */
//public class OpenSmbFileDialog extends KDSUIDialogBase  implements AdapterView.OnItemClickListener, KDSSmbExplorerHandler.interfaceSmbGotAllFiles{
//
//    public static String TAG = "OpenSmbFileDialog";
//
//
//    static final public String STRING_ROOT = "/";
//    static final public String STRING_PARENT = "..";
//    static final public String STRING_FOLDER = ".";
//    static final public String STRING_EMPTY = "";
//
//    ListView m_lstFiles = null;
//    TextView m_txtSelected = null;
//    String m_strSelectedFile = "";
//
//    TextView m_txtIP = null;
//    TextView m_txtUserID = null;
//    TextView m_txtPwd = null;
//    CheckBox m_chkAnonymous = null;
//
//    Button m_btnLogin = null;
//
//    private String m_path = STRING_ROOT;
//    private List<Map<String, Object>> m_listData = null;
//
//    private String m_strExtension = "";
//    private String m_strRootPath = "";
//
//    private Map<String, Integer> m_mapImages = null;
//    KDSSmbExplorerHandler m_handler = new KDSSmbExplorerHandler(this);
//
//    public Object getResult() {
//        return m_strSelectedFile;
//    }
//
//    public OpenSmbFileDialog(final Context context,String strExtension, KDSUIDialogBase.KDSDialogBaseListener listener) {
//        this.int_dialog(context, listener, R.layout.kdsui_dlg_smb_explorer, "");
//        this.setTitle(context.getString(R.string.open_file));//"Open file");
//        m_strExtension = strExtension;
//        m_lstFiles = (ListView) this.getView().findViewById(R.id.lstFiles);
//
//        m_txtSelected = (TextView) this.getView().findViewById(R.id.txtSelected);
//
//        m_txtIP = (TextView)this.getView().findViewById(R.id.txtIP);
//        m_txtUserID = (TextView)this.getView().findViewById(R.id.txtUserID);
//        m_txtPwd = (TextView)this.getView().findViewById(R.id.txtPwd);
//        m_chkAnonymous = (CheckBox)this.getView().findViewById(R.id.chkAnonymous);
//        m_chkAnonymous.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                onAnonymousChanged();
//            }
//        });
//        m_btnLogin = (Button)this.getView().findViewById(R.id.btnLogin);
//
//        m_mapImages = new HashMap<String, Integer>();
//        // ????????????????????????????????????????????? ?????????????????????????????????????????????
//        m_mapImages.put(OpenFileDialog.STRING_ROOT, R.drawable.root);   // ???????????????
//        m_mapImages.put(OpenFileDialog.STRING_PARENT, R.drawable.folder_up);    //????????????????????????
//        m_mapImages.put(OpenFileDialog.STRING_FOLDER, R.drawable.folder);   //???????????????
//        m_mapImages.put("file", R.drawable.file);   //wav????????????
//        m_mapImages.put(OpenFileDialog.STRING_EMPTY, R.drawable.file);
//        m_lstFiles.setOnItemClickListener(this);
//
//        m_btnLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                login();
//            }
//        });
//
//        m_listData = new ArrayList<Map<String, Object>>();
//        SimpleAdapter adapter = new SimpleAdapter(this.getView().getContext(), m_listData, R.layout.listitem_explorer, new String[]{"img", "name", "path"}, new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path});
//        m_lstFiles.setAdapter(adapter);
//
//        loadLastSmbSetting();
//    }
//
//    private void saveLastSmbSetting(){
//
//
//        Context ctx = this.getView().getContext();
//        SharedPreferences sp =ctx.getSharedPreferences("smblastpath", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor=sp.edit();
//        editor.putString("ip",m_txtIP.getText().toString());
//        editor.putString("user",m_txtUserID.getText().toString());
//        editor.putString("pwd",m_txtPwd.getText().toString());
//        editor.putBoolean("anonymous",m_chkAnonymous.isChecked());
//
//        editor.commit();
//
//
//    }
//    private void loadLastSmbSetting(){
//
//
//        Context ctx = this.getView().getContext();
//        SharedPreferences sp =ctx.getSharedPreferences("smblastpath", Context.MODE_PRIVATE);
//
//        m_txtIP.setText(sp.getString("ip",""));
//        m_txtUserID.setText(sp.getString("user",""));
//        m_txtPwd.setText(sp.getString("pwd",""));
//        m_chkAnonymous.setChecked(sp.getBoolean("anonymous", false));
//
//    }
//
//    static public String getFuncKeyName(KDSRouterSettings.ID funcKey)
//    {
//        String strFunc = "";
//        if (funcKey == KDSRouterSettings.ID.Bumpbar_OK)
//            strFunc = "[Enter]";// KDSRouterSettings.getOkKeyString(context);
//        else if (funcKey == KDSRouterSettings.ID.Bumpbar_Cancel)
//            strFunc = "[Ctrl]";// KDSRouterSettings.getCancelKeyString(context);
//
//        return strFunc;
//    }
//
//    public void onAnonymousChanged()
//    {
//        boolean bEnable =(!m_chkAnonymous.isChecked());
//
//        m_txtUserID.setEnabled(bEnable);
//        m_txtPwd.setEnabled(bEnable);
//
//    }
//    public KDSRouterSettings.ID checkKdbEvent(KeyEvent event)
//    {
//        if (event.getKeyCode() == KeyEvent.KEYCODE_CTRL_LEFT)
//            return KDSRouterSettings.ID.Bumpbar_Cancel;
//        else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
//            return KDSRouterSettings.ID.Bumpbar_OK;
//        return KDSRouterSettings.ID.NULL;
//    }
//
//    private void login()
//    {
//        String ip = m_txtIP.getText().toString();
//        String user = m_txtUserID.getText().toString();
//        String pwd = m_txtPwd.getText().toString();
//        boolean banonymous = m_chkAnonymous.isChecked();
//
//        KDSSMBPath smbPath = new KDSSMBPath();
//        smbPath.setPCName(ip);
//
//        if (banonymous) {
//            smbPath.setUserID(" ");
//            smbPath.setPwd(" ");
//        }
//        else {
//            smbPath.setUserID(user);
//            smbPath.setPwd(pwd);
//        }
//        m_path = smbPath.toString();
//        m_strRootPath = m_path;
//        refreshFileList();
//
//
//        saveLastSmbSetting();
//    }
//
//    private String getSuffix(String filename) {
//        int dix = filename.lastIndexOf('.');
//        if (dix < 0) {
//            return "";
//        } else {
//            return filename.substring(dix + 1);
//        }
//    }
//
//    private int getImageId(String s) {
//        if (m_mapImages == null) {
//            return 0;
//        } else if (m_mapImages.containsKey(s)) {
//            return m_mapImages.get(s);
//        } else if (m_mapImages.containsKey(STRING_EMPTY)) {
//            return m_mapImages.get(STRING_EMPTY);
//        } else {
//            return 0;
//        }
//    }
//
//    List<SmbFile> m_smbFiles = null;
//    public void getAllFiles(String smbFolder)
//    {
//
//
////        if (m_listData != null) {
////            m_listData.clear();
////        }
//
//        Object objs[] = new Object[]{smbFolder};
//
//
//        new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] objects) {
//                try {
//                    String folder = (String) objects[0];
//                    m_smbFiles =  KDSSmbFile.getFiles(folder);
//                    m_handler.sendRefreshMessage();
//
//                } catch(Exception e) {
//                    KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);//, e.toString());
//                }
//
//
//                return null;
//
//            }
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, objs);
//
//
//    }
//
//    private int addFilesToList(List<SmbFile> arSmbFiles)
//    {
//
//        if (m_listData != null) {
//            m_listData.clear();
//        } else {
//            m_listData = new ArrayList<Map<String, Object>>();
//        }
//        if (arSmbFiles == null) return 0;
//
//        // ???????????????????????????????????????????????????
//        ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String, Object>>();
//        ArrayList<Map<String, Object>> lfiles = new ArrayList<Map<String, Object>>();
//
//        if (!this.m_path.equals(STRING_ROOT)) {
//            // ??????????????? ??? ???????????????
//            Map<String, Object> map = new HashMap<String, Object>();
//            map.put("name", STRING_ROOT);
//            map.put("path", STRING_ROOT);
//            map.put("img", getImageId(STRING_ROOT));
//            m_listData.add(map);
//
//            map = new HashMap<String, Object>();
//            map.put("name", STRING_PARENT);
//            map.put("path", m_path);
//            map.put("img", getImageId(STRING_PARENT));
//            m_listData.add(map);
//        }
//
//        try {
//            for (int i = 0; i < arSmbFiles.size(); i++) {
//                SmbFile file = arSmbFiles.get(i);
//                if (file.isDirectory()) {
//                    // ???????????????
//                    Map<String, Object> map = new HashMap<String, Object>();
//                    map.put("name", file.getName());
//                    map.put("path", file.getPath());
//                    map.put("img", getImageId(STRING_FOLDER));
//                    lfolders.add(map);
//                } else if (file.isFile()) {
//                    // ????????????
//                    String extension = getSuffix(file.getName()).toLowerCase();
//                    if (m_strExtension == null || m_strExtension.length() == 0 || (extension.length() > 0 && m_strExtension.indexOf("." + extension + ";") >= 0)) {
//                        Map<String, Object> map = new HashMap<String, Object>();
//                        map.put("name", file.getName());
//                        map.put("path", file.getPath());
//                        map.put("img", getImageId(extension));
//                        lfiles.add(map);
//                    }
//                }
//            }
//        }
//        catch (Exception err)
//        {
//            KDSLog.e(TAG, KDSLog._FUNCLINE_(),err);// + KDSLog.getStackTrace(err));
//        }
//        m_listData.addAll(lfolders); // ???????????????????????????????????????????????????
//        m_listData.addAll(lfiles);    //???????????????
//
//        ((SimpleAdapter)m_lstFiles.getAdapter()).notifyDataSetChanged();
//
//        return arSmbFiles.size();
//    }
//
//    private void refreshFileList() {
//        // ??????????????????
//
//        getAllFiles(m_path);
//
//    }
//
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//        // ????????????
//        String filepath = (String) m_listData.get(position).get("path");
//        String filename = (String) m_listData.get(position).get("name");
//        try {
//            if (filename.equals(STRING_ROOT))
//            {
//                m_path =m_strRootPath;
//            }
//            else if ( filename.equals(STRING_PARENT)) {
//                // ?????????????????????????????????
//
//                SmbFile fl = new SmbFile(filepath);
//
//                String ppt = fl.getParent();
//                if (ppt != null) {
//                    // ???????????????
//                    m_path = ppt;
//                    if (m_path.equals( "smb://"))
//                        m_path =m_strRootPath;
//                } else {
//                    // ???????????????
//                    m_path =m_strRootPath;
//                   // "";//STRING_ROOT;
//                }
//            } else {
//                SmbFile fl = new SmbFile(filepath);
//                if (fl.isFile()) {
//                    // ???????????????
//                    //((Activity)getContext()).dismissDialog(this.dialogid); // ???????????????????????????
//                    m_strSelectedFile = filepath;// + "/" +filename;
//                    m_txtSelected.setText(m_strSelectedFile);
//                    enableOKButton(true);
//                    return;
//                } else if (fl.isDirectory()) {
//                    m_path = filepath;
//
//                }
//            }
//        }
//            catch(Exception err)
//            {
//                KDSLog.e(TAG, KDSLog._FUNCLINE_() ,err);//+ KDSLog.getStackTrace(err));
//            }
//        this.refreshFileList();
//    }
//    public void onSmbGetAllFiles()
//    {
//        addFilesToList(m_smbFiles);
//        m_txtSelected.setText(m_path);
//    }
//
//    public void show() {
//        dialog.show();
//        enableOKButton(false);
//    }
//
//}
