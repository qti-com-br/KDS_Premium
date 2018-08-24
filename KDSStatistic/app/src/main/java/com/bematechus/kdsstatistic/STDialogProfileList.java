package com.bematechus.kdsstatistic;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.bematechus.kdslib.ConditionStatistic;
import com.bematechus.kdslib.KDSUtil;

/**
 *
 */
public class STDialogProfileList extends KDSUIDialogBase {

    static public String TAG = "DialogProfileList";
    ListView m_lstFiles = null;
    String m_strSelected = "";

    String m_strFolder = "";
    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return m_strSelected;
    }
    public STDialogProfileList(final Context context,String strFolder, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.st_dialog_profile_list, "");
        this.setTitle(context.getString(R.string.dialog_profile_select));
        m_strFolder = strFolder;
        m_lstFiles =  (ListView)this.getView().findViewById(R.id.lstFiles);

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this.getView().getContext(),android.R.layout.simple_list_item_single_choice,findAllFiles(strFolder));
        m_lstFiles.setAdapter(adapter);
        if (m_lstFiles.getCount()>0)
            m_lstFiles.setItemChecked(m_lstFiles.getCount()-1, true);

        Button btn = (Button) this.getView().findViewById(R.id.btnRemove);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnRemoveClicked(v);
            }
        });

    }



    public void onBtnRemoveClicked(View v)
    {
        String s = getSelectFile();
        String filePath = ConditionStatistic.getProfileFolderFullPath() +s;
        KDSUtil.remove(filePath);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this.getView().getContext(),android.R.layout.simple_list_item_single_choice,findAllFiles(m_strFolder));
        m_lstFiles.setAdapter(adapter);
        if (m_lstFiles.getCount()>0)
            m_lstFiles.setItemChecked(m_lstFiles.getCount()-1, true);
        ((ArrayAdapter<String>)m_lstFiles.getAdapter()).notifyDataSetChanged();
    }

    public String getSelectFile()
    {
        int ncount = m_lstFiles.getCount();
        for (int i=0; i< ncount; i++) {
            if ( m_lstFiles.isItemChecked(i))
            {
                Object obj = m_lstFiles.getAdapter().getItem(i);
                String s = (String)obj;
                return s;

            }
        }
        return "";
    }
    public void onOkClicked()
    {
        m_strSelected = getSelectFile();


    }

    static public List<String> findAllFiles(String strFolder)
    {
        ArrayList<String> ar = new ArrayList<String>();
        File file;
        File[] files = new File[0];

        try {
            file = new File(strFolder);
            if (file == null) return ar;
        }
        catch (Exception err)
        {
            Log.e(TAG, KDSUtil._FUNCLINE_() + err.toString());
            Log.e(TAG, KDSUtil.error( err));
            return ar;
        }
        try {
            files = file.listFiles();
        } catch (Exception e) {
            Log.e(TAG, KDSUtil._FUNCLINE_() + e.toString());
            Log.e(TAG, KDSUtil.error( e));

        }

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().indexOf(".xml") != -1) {
                    ar.add(files[i].getName());
                }
            }
        }
        return ar;

    }
}
