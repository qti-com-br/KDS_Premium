package com.bematechus.kdsstatistic;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSUIColorPickerDialog;

/**
 * Created by David.Wong on 2018/5/17.
 * Rev:
 */
public class SOSDialogLayoutProperties extends SOSDialogPropertiesBase implements KDSUIColorPickerDialog.OnColorPickerDlgListener {

    SOSLinearLayout m_layout = null;
    static public final int DEFAULT_BG = Color.WHITE;
    static public final int DEFAULT_FG = Color.BLACK;

    public void onCancel(KDSUIColorPickerDialog dialog)
    {

    }

    public void onOk(KDSUIColorPickerDialog dialog, int color)
    {
        ((Button) dialog.getTag()).setBackgroundColor(color);
    }
    public void onOkClicked()
    {

        saveProperties();

    }

    public SOSDialogLayoutProperties(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener, SOSLinearLayout layout) {
        this.int_dialog(context, listener, R.layout.sos_dialog_layout_properties, "");
        m_layout = layout;

        init();
        this.setTitle(KDSApplication.getContext().getString(R.string.layout_prop));

        showProperties();

    }

    public void init()
    {
        Button btn = (Button) this.getView().findViewById(R.id.btnColor);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseColor(v);
            }
        });

        CheckBox chk = (CheckBox) this.getView().findViewById(R.id.chkShowBorder);
        chk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        btn = (Button) this.getView().findViewById(R.id.btnCellBG);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseBGColor(v);
            }
        });

        chk = (CheckBox) this.getView().findViewById(R.id.chkCellColor);
        chk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) SOSDialogLayoutProperties.this.getView().findViewById(R.id.btnCellBG);

                CheckBox c = (CheckBox)v;

                btn.setEnabled(c.isChecked());


            }
        });

    }

    private void chooseBGColor(View v)
    {
        KDSUIColorPickerDialog dlg = new KDSUIColorPickerDialog(this.getView().getContext(), getButtonBG(v, Color.TRANSPARENT), this);
        dlg.setTag(v);
        dlg.show();
    }



    private void showProperties()
    {
        Button btn = (Button) this.getView().findViewById(R.id.btnColor);
        int nBG =  getButtonBG( m_layout,DEFAULT_BG);
        int nFG = m_layout.getBorderColor();
        showButtonColor(btn, nBG,nFG );
        CheckBox chk = (CheckBox) this.getView().findViewById(R.id.chkShowBorder);
        chk.setChecked(m_layout.getShowBorder());


        chk = (CheckBox) this.getView().findViewById(R.id.chkCellColor);
        if (m_layout.getSelectedBlock() != null)
        {
            nBG = m_layout.getSelectedBlock().getBG();

            chk.setChecked((nBG != Color.TRANSPARENT));
            btn = (Button) this.getView().findViewById(R.id.btnCellBG);
            btn.setEnabled(chk.isChecked());
            showButtonColor(btn, nBG,DEFAULT_BG);

        }

    }

    private void saveProperties()
    {

        Button btn = (Button) this.getView().findViewById(R.id.btnColor);
        int nbg =  getButtonBG(btn, DEFAULT_BG);
        int nfg =  getButtonFG(btn, DEFAULT_FG);

        m_layout.setBackgroundColor(nbg);
        m_layout.setBorderColor(nfg);

        CheckBox chk = (CheckBox) this.getView().findViewById(R.id.chkShowBorder);
        m_layout.setShowBorder(chk.isChecked());


        if (m_layout.getSelectedBlock() != null)
        {
            btn = (Button) this.getView().findViewById(R.id.btnCellBG);
            nbg =  getButtonBG(btn, Color.TRANSPARENT);
            chk = (CheckBox) this.getView().findViewById(R.id.chkCellColor);
            if (!chk.isChecked())
                nbg = Color.TRANSPARENT;
            m_layout.getSelectedBlock().setBG(nbg);
        }




    }
}
