package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSSMBPath;
import com.bematechus.kdslib.KDSSmbFile;
import com.bematechus.kdslib.KDSSmbFile1;
import com.bematechus.kdslib.KDSSmbFile2;

/**
 * Created by Administrator on 2015/10/19 0019.
 */
public class KDSUIDlgInputSMBFolder   {
    public interface OnKDSUIDlgInputSMBFolderListener {
       // public void onSMBCancel(KDSUIDlgInputSMBFolder dialog);

        public void onSMBOk(KDSUIDlgInputSMBFolder dialog, KDSSMBPath station);
    }

    AlertDialog dialog = null;

    OnKDSUIDlgInputSMBFolderListener listener= null;

    TextView m_txtDomain = null;
    TextView m_txtPCName = null;
    TextView m_txtUser = null;
    TextView m_txtPwd = null;
    TextView m_txtPath = null;
    Button m_btnFind = null;

    CheckBox m_chkAnonymous = null;

    KDSSMBPath m_smbPath = new KDSSMBPath();

    /*****************************************************************************************/

    public KDSUIDlgInputSMBFolder(final Context context, KDSSMBPath ff, OnKDSUIDlgInputSMBFolderListener listener) {
        this.listener = listener;
        if (ff != null)
            m_smbPath.copyFrom( ff);

        final View view = LayoutInflater.from(context).inflate(R.layout.kdsui_input_remote_folder, null);

        m_txtDomain = (TextView)view.findViewById(R.id.txtDomain);
        m_txtPCName = (TextView)view.findViewById(R.id.txtPCName);
        m_txtUser = (TextView)view.findViewById(R.id.txtUserID);
        m_txtPwd = (TextView)view.findViewById(R.id.txtPwd);
        m_txtPath = (TextView)view.findViewById(R.id.txtFolder);

        m_btnFind = (Button)view.findViewById(R.id.btnFind);
        m_chkAnonymous = (CheckBox)view.findViewById(R.id.chkAnonymous);


        dialog = new AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveToSmb();
//                        m_smbPath.setPCName(m_txtPCName.getText().toString());
//                        m_smbPath.setUserID(m_txtUser.getText().toString());
//                        m_smbPath.setPwd(m_txtPwd.getText().toString());
//                        m_smbPath.setFolder(m_txtPath.getText().toString());

                        if (KDSUIDlgInputSMBFolder.this.listener != null) {
                            KDSUIDlgInputSMBFolder.this.listener.onSMBOk(KDSUIDlgInputSMBFolder.this, getSMBPath());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       // if (KDSUIDlgInputSMBFolder.this.listener != null) {
                       //     KDSUIDlgInputSMBFolder.this.listener.onSMBCancel(KDSUIDlgInputSMBFolder.this);
                       // }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
                     //   if (KDSUIDlgInputSMBFolder.this.listener != null) {
                     //       KDSUIDlgInputSMBFolder.this.listener.onSMBCancel(KDSUIDlgInputSMBFolder.this);
                     //   }

                    }
                })
                .create();


        // kill all padding from the dialog window
        dialog.setView(view, 0, 0, 0, 0);

        init_dialog_events(dialog);
        m_txtDomain.setText(m_smbPath.getDomain());
        m_txtPCName.setText(m_smbPath.getPCName());
        if (m_smbPath.getUserID().equals(" ") &&
                m_smbPath.getPwd().equals(" "))
        {

            m_txtUser.setText("");
            m_txtPwd.setText("");
            m_chkAnonymous.setChecked(true);
            m_txtUser.setEnabled(false);
            m_txtPwd.setEnabled(false);
        }
        else {
            m_txtUser.setText(m_smbPath.getUserID());
            m_txtPwd.setText(m_smbPath.getPwd());
            m_chkAnonymous.setChecked(false);
            m_txtUser.setEnabled(true);
            m_txtPwd.setEnabled(true);
        }
        m_txtPath.setText(m_smbPath.getFolder());

        m_chkAnonymous.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    m_txtUser.setEnabled(false);
                    m_txtPwd.setEnabled(false);
                }
                else
                {
                    m_txtUser.setEnabled(true);
                    m_txtPwd.setEnabled(true);
                }
            }
        });

        m_btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToSmb();
                if (KDSSmbFile.getEnabledSmbV2())
                    findSmbFile2();
                else
                    findSmbFile1();

            }
        });

    }

    private void findSmbFile2()
    {
        // Create DirectoryChooserDialog and register a callback
        KDSSmbFile2 directoryChooserDialog =
                new KDSSmbFile2(dialog.getContext(),
                        new KDSSmbFile2.ChosenDirectoryListener()
                        {
                            @Override
                            public void onChosenDir(String chosenDir)
                            {

                                KDSSMBPath smb = KDSSMBPath.parseString(chosenDir);
                                m_txtPath.setText(smb.getFolder());
                                m_smbPath = smb;
                                AsyncTask t = new AsyncTask() {
                                    @Override
                                    protected Object doInBackground(Object[] params) {
                                        if (KDSSmbFile2.checkFolderWritable(m_smbPath.toString())!=0)
                                        {
                                            showPermissionErrorDialog();

                                        }
                                        return null;
                                    }
                                };
                                t.execute();

//                                if (KDSSmbFile2.checkFolderWritable(m_smbPath.toString())!=0)
//                                {
//                                    showPermissionErrorDialog();
//
//                                }
                            }
                        });
        // Toggle new folder button enabling
        directoryChooserDialog.setNewFolderEnabled(false);
        // Load directory chooser dialog for initial 'm_chosenDir' directory.
        // The registered callback will be called upon final directory selection.
        //smb://Administrator:zwt1314521zw@192.168.6.138/
        // String str = "smb://Administrator:13188223394@192.168.1.197/";
        //String str = "smb://workgroup/";
        String str = m_smbPath.toString();

        directoryChooserDialog.chooseDirectory(str);
        //m_newFolderEnabled = ! m_newFolderEnabled;
    }

    private void findSmbFile1()
    {
        // Create DirectoryChooserDialog and register a callback
        KDSSmbFile1 directoryChooserDialog =
                new KDSSmbFile1(dialog.getContext(),
                        new KDSSmbFile1.ChosenDirectoryListener()
                        {
                            @Override
                            public void onChosenDir(String chosenDir)
                            {

                                KDSSMBPath smb = KDSSMBPath.parseString(chosenDir);
                                m_txtPath.setText(smb.getFolder());
                                m_smbPath = smb;
                                AsyncTask t = new AsyncTask() {
                                    @Override
                                    protected Object doInBackground(Object[] params) {
                                        if (KDSSmbFile1.checkFolderWritable(m_smbPath.toString())!=0)
                                        {
                                            showPermissionErrorDialog();

                                        }
                                        return null;
                                    }
                                };
                                t.execute();
//                                if (KDSSmbFile1.checkFolderWritable(m_smbPath.toString())!=0)
//                                {
//                                    showPermissionErrorDialog();
//
//                                }
                            }
                        });
        // Toggle new folder button enabling
        directoryChooserDialog.setNewFolderEnabled(false);
        // Load directory chooser dialog for initial 'm_chosenDir' directory.
        // The registered callback will be called upon final directory selection.
        //smb://Administrator:zwt1314521zw@192.168.6.138/
        // String str = "smb://Administrator:13188223394@192.168.1.197/";
        //String str = "smb://workgroup/";
        String str = m_smbPath.toString();

        directoryChooserDialog.chooseDirectory(str);
        //m_newFolderEnabled = ! m_newFolderEnabled;
    }

    private void onPermissionIgnoreAndSave()
    {

    }

    private void onPermissionCancelSetting()
    {
        m_txtPath.setText("");

        m_smbPath = new KDSSMBPath();
    }

    private void onPermissionModifySetting()
    {
        m_smbPath = new KDSSMBPath();
    }
    public void showPermissionErrorDialog()
    {
        String strOK = dialog.getContext().getString(R.string.ignore_and_save);
        String strNeutral =dialog.getContext().getString(R.string.modify_setting);
        String strCancel =dialog.getContext().getString(R.string.cancel_setting);

        AlertDialog d = new AlertDialog.Builder(dialog.getContext())
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPermissionIgnoreAndSave();

                    }
                })
                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPermissionCancelSetting();

                    }
                })
                .setNeutralButton(strNeutral, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPermissionModifySetting();

                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
                        onPermissionIgnoreAndSave();

                    }
                })
                .setTitle(dialog.getContext().getString(R.string.error))
                .setMessage(dialog.getContext().getString(R.string.error_folder_permission))
                .create();
        d.show();
    }

    static public KDSSettings.ID checkKdbEvent(KeyEvent event)
    {
        if (event.getKeyCode() == KeyEvent.KEYCODE_0)
            return KDSSettings.ID.Bumpbar_Cancel;
        else if (event.getKeyCode() == KeyEvent.KEYCODE_1)
            return KDSSettings.ID.Bumpbar_OK;
        return KDSSettings.ID.NULL;
    }
    private void init_dialog_events(final AlertDialog dlg)
    {
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
                if (event.getRepeatCount()>0) return false;
                if (event.getAction() != KeyEvent.ACTION_UP) return false;
                KDSSettings.ID evID = KDSSettings.ID.NULL;
                evID = checkKdbEvent(event);

//                if (KDSGlobalVariables.getKDS()!= null)
//                    evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
//                else
//                {
//                    KDSBumpBarFunctions funcs = new KDSBumpBarFunctions();
//                    KDSSettings settings = new KDSSettings(dlg.getContext().getApplicationContext());
//                    funcs.updateSettings(settings);
//                    evID = funcs.getKDSDlgEvent(event, null);
//                }
                if (evID == KDSSettings.ID.Bumpbar_OK)
                {
                    dialog.dismiss();
                    if (KDSUIDlgInputSMBFolder.this.listener != null)
                        KDSUIDlgInputSMBFolder.this.listener.onSMBOk(KDSUIDlgInputSMBFolder.this,getSMBPath());
                    return true;
                }
                else if (evID == KDSSettings.ID.Bumpbar_Cancel)
                {
                    dialog.cancel();

                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_UP)
                {
                    KDSKbdRecorder.convertKeyEvent(keyCode, event);
                }
                return false;
            }
        });
    }


    private void saveToSmb()
    {
        m_smbPath.setDomain(m_txtDomain.getText().toString());
        m_smbPath.setPCName(m_txtPCName.getText().toString());
        if (m_chkAnonymous.isChecked()) {
            m_smbPath.setUserID(" ");
            m_smbPath.setPwd(" ");
        }
        else {
            m_smbPath.setUserID(m_txtUser.getText().toString());
            m_smbPath.setPwd(m_txtPwd.getText().toString());
        }

        //m_smbPath.setPwd(m_txtPwd.getText().toString());
        m_smbPath.setFolder(m_txtPath.getText().toString());

    }

    public KDSSMBPath getSMBPath()
    {
        return m_smbPath;
    }
    public void setSMBPath(KDSSMBPath smbPath)
    {
        m_smbPath = smbPath;
    }

    public void show() {
        dialog.show();
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    KDSKbdRecorder.convertKeyEvent(keyCode, event);
                }
                return false;
            }
        });
    }

    public AlertDialog getDialog() {
        return dialog;
    }

//    public void onChosenDir(String chosenDir)
//    {
//
//    }

}
