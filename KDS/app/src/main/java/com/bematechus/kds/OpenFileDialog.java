package com.bematechus.kds;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bematechus.kdslib.KDSLog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/19.
 */
public class OpenFileDialog extends KDSUIDialogBase  implements AdapterView.OnItemClickListener {
    public static String TAG = "OpenFileDialog";
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
    private boolean m_bJustFolder = false;

    public Object getResult() {
        return m_strSelectedFile;
    }

    /**
     *
     * @param context
     * @param strExtension
     *  use ";" seperate them, please add ";" at last.
     *      e.g: .xml;.png;
     * @param listener
     */
    public OpenFileDialog(final Context context, String strExtension, KDSDialogBaseListener listener, boolean bFolder) {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_explorer, "");
        this.setTitle(context.getString(R.string.open_file));//"Open file");
        m_bJustFolder = bFolder;
        m_strExtension = strExtension;
        m_lstFiles = (ListView) this.getView().findViewById(R.id.lstFiles);

        m_txtSelected = (TextView) this.getView().findViewById(R.id.txtSelected);

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
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
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
                if (m_bJustFolder) continue;
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

            if (m_bJustFolder)
            {
                m_strSelectedFile = filepath;// + "/" +filename;
                m_txtSelected.setText(m_strSelectedFile);
                m_lastPath = m_path;
                enableOKButton(true);
            }

        } else {
            File fl = new File(filepath);
            if (fl.isFile()) {
                if (m_bJustFolder) return;
                // 如果是文件
                //((Activity)getContext()).dismissDialog(this.dialogid); // 让文件夹对话框消失
                m_strSelectedFile = filepath;// + "/" +filename;
                m_txtSelected.setText(m_strSelectedFile);
                m_lastPath = m_path;
                enableOKButton(true);
                return;
            } else if (fl.isDirectory()) {
                m_path = filepath;
                if (m_bJustFolder)
                {
                    m_strSelectedFile = filepath;// + "/" +filename;
                    m_txtSelected.setText(m_strSelectedFile);
                    m_lastPath = m_path;
                    enableOKButton(true);
                }
            }
        }
        this.refreshFileList();
    }


    public void show() {
        dialog.show();
        enableOKButton(false);
    }


}

