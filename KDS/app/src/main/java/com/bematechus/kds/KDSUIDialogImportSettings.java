package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;

import com.bematechus.kdslib.KDSUIDialogBase;

/**
 * Created by Administrator on 2017/7/7.
 */
public class KDSUIDialogImportSettings extends KDSUIDialogBase implements KDSUIDialogBase.KDSDialogBaseListener {


    String m_selectedPath = "";

    @Override
    public  void create3ButtonsDialog(final Context context, Object objTag, String strTitle, String strInfo, String btnText, KDSDialogBaseListener listener)
    {

        this.listener = listener;
        setTag(objTag);

        String strOK = makeOKButtonText(context);// makeButtonText2(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);
        String strCancel = makeCancelButtonText(context);// makeButtonText2(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
        dialog = new AlertDialog.Builder(context)
                .setTitle(strTitle)
                .setMessage(strInfo)//this.getString(R.string.confirm_import_db))
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (KDSUIDialogImportSettings.this.listener != null)
                                    KDSUIDialogImportSettings.this.listener.onKDSDialogOK(KDSUIDialogImportSettings.this, getResult());
                            }
                        }
                )
                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSUIDialogImportSettings.this.listener != null)
                            KDSUIDialogImportSettings.this.listener.onKDSDialogCancel(KDSUIDialogImportSettings.this);
                    }
                })
                .setNeutralButton(btnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBrowseImportFolder();
                    }
                })
                .create();
        boolean noCancel = true;
        if (noCancel) {
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
        }
        init_dialog_events(dialog);


    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dlg, Object obj)
    {
        if (dlg instanceof OpenFileDialog)
        {

            String s = (String)dlg.getResult();



            if (s.isEmpty()) return;
            if (s != "/")
                s += "/";
            String info = dialog.getContext().getString(R.string.confirm_import_db);
            info += " ";
            info += s;
            info += " ?";
            this.dialog.setMessage(info);

            m_selectedPath = s;

        }
    }

    protected void onBrowseImportFolder()
    {
        //OpenFileDialog dlg = new OpenFileDialog(m_view.getContext(), "*.xml", this);
        OpenFileDialog dlg = new OpenFileDialog(dialog.getContext(), ".xml;", this, true);
        dlg.show();

    }
    public void show()
    {
        super.show();
        Button btn =  dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBrowseImportFolder();
            }
        });
    }

    public String getSelectedPath()
    {
        if (m_selectedPath.isEmpty())
            return MainActivity.getSDFolderFullPathWithLastDivid(MainActivity.DEFAULT_BACKUP_FOLDER);
        else {

            return m_selectedPath;
        }
    }
}
