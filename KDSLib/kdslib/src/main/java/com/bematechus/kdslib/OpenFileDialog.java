package com.bematechus.kdslib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */
/**
 * Created by Administrator on 2016/9/19.
 */
public class OpenFileDialog extends KDSUIDialogBase implements AdapterView.OnItemClickListener {
    public static String TAG = "OpenFileDialog";

    public enum Mode
    {
        Choose_Folder,
        Choose_File,
        Save_2_File,
    }

    static final public String STRING_ROOT = "/";
    static final public String STRING_PARENT = "..";
    static final public String STRING_FOLDER = ".";
    static final public String STRING_EMPTY = "";
    //static final private String STRING_ERROR_MSG = "No rights to access!";

    ListView m_lstFiles = null;
    TextView m_txtSelected = null;
    String m_strSelectedFile = "";

    private String m_path = STRING_ROOT;
    private List<Map<String, Object>> m_listData = null;
    //private int dialogid = 0;

    private String m_strExtension = "";

    private Map<String, Integer> m_mapImages = null;

    private static String m_lastPath = STRING_ROOT;
   // private boolean m_bJustFolder = false;

    private Mode m_nMode = Mode.Choose_Folder;

    private View m_layoutFileName = null;
    private EditText m_txtFileName = null;

    public Object getResult() {
        return m_strSelectedFile;
    }

    /**
     *
     * @param context
     * @param strExtension
     *  use ";" seperate them, please add ";" at last.
     * @param listener
     */
    public OpenFileDialog(final Context context, String strExtension, KDSDialogBaseListener listener, Mode nMode) {
        this.setUseCtrlEnterKey(true);
        this.int_dialog(context, listener, R.layout.kdsui_dlg_explorer, "");
        this.setTitle(context.getString(R.string.open_file));//"Open file");
        //m_bJustFolder = bFolder;
        m_nMode = nMode;
        m_strExtension = strExtension;

        m_txtFileName =(EditText) this.getView().findViewById(R.id.txtFileName);
        m_layoutFileName =  this.getView().findViewById(R.id.layoutFileName);

        m_lstFiles = (ListView) this.getView().findViewById(R.id.lstFiles);

        m_txtSelected = (TextView) this.getView().findViewById(R.id.txtSelected);

        setupGui(nMode);
        m_mapImages = new HashMap<String, Integer>();
        // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
        m_mapImages.put(OpenFileDialog.STRING_ROOT, R.drawable.root);   // 根目录图标
        m_mapImages.put(OpenFileDialog.STRING_PARENT, R.drawable.folder_up);    //返回上一层的图标
        m_mapImages.put(OpenFileDialog.STRING_FOLDER, R.drawable.folder);   //文件夹图标
        m_mapImages.put("file", R.drawable.file);   //wav文件图标
        m_mapImages.put(OpenFileDialog.STRING_EMPTY, R.drawable.file);
        m_lstFiles.setOnItemClickListener(this);

        m_path = m_lastPath;
        refreshFileList();
    }

    private void setupGui(Mode nMode)
    {
        switch (nMode)
        {

            case Choose_Folder:
                m_layoutFileName.setVisibility(View.GONE);
                break;
            case Choose_File:
                m_layoutFileName.setVisibility(View.GONE);
                break;
            case Save_2_File:
                m_layoutFileName.setVisibility(View.VISIBLE);
                break;
        }
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

    private int refreshFileList() {
        // 刷新文件列表
        File[] files = null;
        try {
            files = new File(m_path).listFiles();
        } catch (Exception e) {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);// + KDSLog.getStackTrace(e));
            files = null;
        }
        if (files == null) {
            // 访问出错
            Toast.makeText(this.getView().getContext(),this.getView().getContext().getString(R.string.no_rights_to_access), Toast.LENGTH_SHORT).show();
            return -1;
        }
        if (m_listData != null) {
            m_listData.clear();
        } else {
            m_listData = new ArrayList<Map<String, Object>>(files.length);
        }

        // 用来先保存文件夹和文件夹的两个列表
        ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String, Object>>();
        ArrayList<Map<String, Object>> lfiles = new ArrayList<Map<String, Object>>();

        if (!this.m_path.equals(STRING_ROOT)) {
            // 添加根目录 和 上一层目录
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

        for (File file : files) {
            if (file.isDirectory() && file.listFiles() != null) {
                // 添加文件夹
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("name", file.getName());
                map.put("path", file.getPath());
                map.put("img", getImageId(STRING_FOLDER));
                lfolders.add(map);
            } else if (file.isFile()) {
                //if (m_bJustFolder) continue;
                if (m_nMode == Mode.Choose_Folder )
                    continue;
                // 添加文件
                String extension = getSuffix(file.getName()).toLowerCase();
                if (m_strExtension == null || m_strExtension.length() == 0 || (extension.length() > 0 && m_strExtension.indexOf("." + extension + ";") >= 0)) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("name", file.getName());
                    map.put("path", file.getPath());
                    map.put("img", getImageId(extension));
                    lfiles.add(map);
                }
            }
        }
        m_listData.addAll(lfolders); // 先添加文件夹，确保文件夹显示在上面
        m_listData.addAll(lfiles);    //再添加文件

        SimpleAdapter adapter = new SimpleAdapter(this.getView().getContext(), m_listData, R.layout.listitem_explorer, new String[]{"img", "name", "path"}, new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path});
        m_lstFiles.setAdapter(adapter);
        return files.length;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // 条目选择
        String filepath = (String) m_listData.get(position).get("path");
        String filename = (String) m_listData.get(position).get("name");
        if (filename.equals(STRING_ROOT) || filename.equals(STRING_PARENT)) {
            // 如果是更目录或者上一层
            File fl = new File(filepath);
            String ppt = fl.getParent();
            if (ppt != null) {
                // 返回上一层
                m_path = ppt;
            } else {
                // 返回更目录
                m_path = STRING_ROOT;
            }

            if (m_nMode == Mode.Choose_Folder ||
                    m_nMode == Mode.Save_2_File)
            {
                m_strSelectedFile = filepath;// + "/" +filename;
                m_txtSelected.setText(m_strSelectedFile);
                m_lastPath = m_path;
                enableOKButton(true);
            }


        } else {
            File fl = new File(filepath);
            if (fl.isFile()) {
                if (m_nMode == Mode.Choose_Folder) return;
                // 如果是文件
                //((Activity)getContext()).dismissDialog(this.dialogid); // 让文件夹对话框消失
                m_strSelectedFile = filepath;// + "/" +filename;
                m_txtSelected.setText(m_strSelectedFile);
                showSaveToFileText();
                m_lastPath = m_path;
                enableOKButton(true);
                return;
            } else if (fl.isDirectory()) {
                m_path = filepath;
                if (m_nMode == Mode.Choose_Folder ||
                        m_nMode == Mode.Save_2_File)
                {
                    m_strSelectedFile = filepath;// + "/" +filename;
                    m_txtSelected.setText(m_strSelectedFile);
                    m_lastPath = m_path;
                    showSaveToFileText();
                    enableOKButton(true);
                }
            }
        }
        this.refreshFileList();
    }

    private void showSaveToFileText()
    {
        if (m_nMode != Mode.Save_2_File ) return;

        File f = new File (m_strSelectedFile);
        if (!f.isDirectory()) {
            m_txtFileName.setText(f.getName());
            String s = f.getPath().replace("/"+f.getName(), "");
            m_txtSelected.setText(s);
            m_strSelectedFile = s;
        }
        else
        {
            m_txtSelected.setText(f.getPath());
        }

    }

    public void show() {
        dialog.show();
        if (m_strSelectedFile.isEmpty())
            enableOKButton(false);
        if (m_nMode == Mode.Save_2_File) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (OpenFileDialog.this.onSaveToFileOKButtonClicked())
                        dialog.dismiss();

                }
            });
        }
    }

    /**
     * check if the file name is same
     * @return
     */
    private boolean onSaveToFileOKButtonClicked()
    {
        if (m_nMode != Mode.Save_2_File) return true;
        File f = new File(m_strSelectedFile + "/" + m_txtFileName.getText().toString());
        if (f.exists())
        {
            showConfirmOverwrite();
            return false;
        }
        else {
            saveSelectedSaveToFile();
            if (OpenFileDialog.this.listener != null) {
                OpenFileDialog.this.listener.onKDSDialogOK(OpenFileDialog.this, getResult());
            }
            return true;
        }
    }
    private void showConfirmOverwrite()
    {
        AlertDialog d = new AlertDialog.Builder(this.getDialog().getContext())
                .setTitle(this.getDialog().getContext().getString(R.string.confirm))
                .setMessage(this.getDialog().getContext().getString(R.string.file_existed_overwrite_confirm))//this.getString(R.string.confirm_import_db))
                .setPositiveButton(this.getDialog().getContext().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                OpenFileDialog.this.onConfirmOverwriteYes();
                            }
                        }
                )
                .setNegativeButton(this.getDialog().getContext().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OpenFileDialog.this.onConfirmOverwriteNo();
                    }
                })
                .create();
        d.show();
    }
    private void onConfirmOverwriteYes()
    {
        saveSelectedSaveToFile();
        if (OpenFileDialog.this.listener != null) {
            OpenFileDialog.this.listener.onKDSDialogOK(OpenFileDialog.this, getResult());
        }
        this.dialog.dismiss();
    }
    private void onConfirmOverwriteNo()
    {

    }
    private void saveSelectedSaveToFile()
    {
        m_strSelectedFile += "/" + m_txtFileName.getText().toString();
    }

    public void setDefaultFolderFileName(String folder, String fileName)
    {
        if (m_nMode == Mode.Choose_File)
            m_strSelectedFile = folder + "/" + fileName;
        else
            m_strSelectedFile = folder;
        m_lastPath = folder;
        m_path = folder;
       showDefault(folder + "/" + fileName);
        refreshFileList();
    }

    private void showDefault(String filePath)
    {
        if (!filePath.isEmpty())
        {
            if (m_nMode == Mode.Choose_File)
                m_txtSelected.setText(filePath);
            else
                m_txtSelected.setText(KDSUtil.fileGetFolderFromPath(filePath));
            m_txtFileName.setText(KDSUtil.fileGetFileNameFromPath(filePath));
            m_lastPath = KDSUtil.fileGetFolderFromPath(filePath);

        }
    }

}

