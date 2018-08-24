package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.bematechus.kdslib.KDSUtil;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2015/11/30 0030.
 */
public class KDSUIDlgInputTemplate extends KDSUIDialogBase{


    TextView m_txtTemplate = null;

    String m_strTemplate = "";

    @Override
    public void onOkClicked()
    {//save data here
        m_strTemplate = m_txtTemplate.getText().toString();

    }

    /**
     * it will been overrided by child
     * @return
     */
    @Override
    public Object getResult()
    {
        return m_strTemplate;

    }

    public KDSUIDlgInputTemplate(final Context context, String strOriginal, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.int_dialog(context, listener,R.layout.kdsui_input_print_template, context.getString( R.string.str_default));

        //get all widgets
        m_txtTemplate =(TextView) this.getView().findViewById(R.id.txtTemplate);
        m_strTemplate = strOriginal;

        m_txtTemplate.setText(m_strTemplate);

        Button btnExport = (Button) this.getView().findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportTemplate();
            }
        });

        Button btnImport = (Button) this.getView().findViewById(R.id.btnImport);
        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importTemplate();
            }
        });
    }

    public void loadDefault()
    {
        m_txtTemplate.setText(R.string.pref_kds_printer_template_default);
    }

    @Override
    public void show()
    {
        super.show();
        Button btn =dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                KDSUIDlgInputTemplate.this.loadDefault();
            }
        });


    }

    protected String makeButtonText2(Context context, int nResID, KDSSettings.ID funcKey )
    {
        return context.getString(nResID);
        //return makeCtrlEnterButtonText(context, nResID, funcKey); //2.0.33


    }



    protected void init_dialog_events(final AlertDialog dlg)
    {
        //init_dialog_ctrl_enter_events(dlg); //2.0.33

    }
    OpenLocalFileDialog m_openFileDlg = null;
    private void exportTemplate()
    {
        if (m_openFileDlg != null ) return;
        m_openFileDlg = new OpenLocalFileDialog(this.getView().getContext(),"", new KDSUIDialogBase.KDSDialogBaseListener() {
            public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj) {
                String s = (String)obj;
                saveTemplateToFile(s);
                m_openFileDlg = null;

            }

            public void onKDSDialogCancel(KDSUIDialogBase dialog) {
                // nothing to do
                m_openFileDlg = null;
            }
        }, OpenLocalFileDialog.Mode.Save_2_File);
        m_openFileDlg.show();
    }
    private void importTemplate()
    {
        if (m_openFileDlg != null ) return;
        m_openFileDlg = new OpenLocalFileDialog(this.getView().getContext(),"", new KDSUIDialogBase.KDSDialogBaseListener() {
            public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj) {
                String s = (String)obj;
                loadFromTemplateFile(s);
                m_openFileDlg = null;

            }

            public void onKDSDialogCancel(KDSUIDialogBase dialog) {
                // nothing to do
                m_openFileDlg = null;
            }
        }, OpenLocalFileDialog.Mode.Choose_File);
        m_openFileDlg.show();
    }

   private void saveTemplateToFile(String fileName)
   {
       KDSUtil.fileWrite(fileName, m_txtTemplate.getText().toString());
   }
    private void loadFromTemplateFile(String fileName)
    {
        String s = KDSUtil.readFile(fileName);
        m_txtTemplate.setText(s);
    }


}
