package com.bematechus.kdsrouter;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.AttributeSet;

import com.bematechus.kdslib.KDSToast;
import com.bematechus.kdslib.KDSUtil;

/**
 * Created by Administrator on 2018/1/21.
 */
public class KDSPreferenceDb2CSV extends Preference implements KDSUIDialogBase.KDSDialogBaseListener {

    private boolean m_bSave = false;//open or save
    public KDSPreferenceDb2CSV(Context context, AttributeSet attrs) {
        super(context, attrs);

        String defaultVal = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        if (defaultVal.equals("1"))
            m_bSave =  true;

    }

    /**
     * @param dialog
     */
    public void onKDSDialogCancel(KDSUIDialogBase dialog) {

    }

    /**
     * ip selection dialog
     *
     * @param dlg
     * @param obj
     */
    public void onKDSDialogOK(KDSUIDialogBase dlg, Object obj)// ArrayList<String> stations)
    {
        String fileName = (String)obj;
        if (m_bSave)
            exportToCSV(fileName);
        else
            importFromCSV(fileName);

    }

    @Override
    protected void onClick() {

        OpenFileDialog.Mode m = OpenFileDialog.Mode.Save_2_File;
        if (!m_bSave)
            m = OpenFileDialog.Mode.Choose_File;

        OpenFileDialog d = new  OpenFileDialog(this.getContext(),"", this,m);
        d.show();


    }

    private void exportToCSV(String fileName)
    {
        String s = KDSGlobalVariables.getKDSRouter().getRouterDB().export2CSV();
        KDSUtil.fileWrite(fileName, s);
        KDSToast.showMessage(this.getContext(), this.getContext().getString(R.string.export_db_done) +" "+ fileName );
    }
    private void importFromCSV(String fileName)
    {
        String s = KDSUtil.readFile(fileName);
        if (s.isEmpty()) return;
        KDSGlobalVariables.getKDSRouter().getRouterDB().importFromCSV(s);
        KDSToast.showMessage(this.getContext(),this.getContext().getString(R.string.import_csv_done) +" "+ fileName);
    }
}